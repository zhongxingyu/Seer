 package ca.carleton.ccsl.cubalance;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.net.ssl.SSLPeerUnverifiedException;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpVersion;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.CookieStore;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.protocol.ClientContext;
 import org.apache.http.conn.scheme.Scheme;
 import org.apache.http.conn.scheme.SchemeRegistry;
 import org.apache.http.cookie.Cookie;
 import org.apache.http.impl.client.BasicCookieStore;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.HttpParams;
 import org.apache.http.params.HttpProtocolParams;
 import org.apache.http.protocol.BasicHttpContext;
 import org.apache.http.protocol.HTTP;
 import org.apache.http.protocol.HttpContext;
 import org.thoughtcrime.ssl.pinning.PinningSSLSocketFactory;
 
 import android.content.Context;
 import android.os.AsyncTask;
 
 public class CUBalanceFetcher extends AsyncTask<Void, Void, CUBalanceResult>
 {
   private static final String FF_USER_AGENT         = "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:11.0) Gecko/20100101 Firefox/11.0";
   private static final String CARLETON_LOGIN_URL    = "https://ccsccl01.carleton.ca/student/local_login.php";
   private static final String CARLETON_LOGIN_REF    = "https://ccsccl01.carleton.ca/student/local_login.php";
   private static final String CARLETON_BALANCE_URL  = "https://ccsccl01.carleton.ca/student/welcome.php";
   private static final String CU_HTTPS_PIN          = "322d6fcac22d947b0cd640b3512f29be89439276";
   
   private static final String CARLETON_SESH_COOKIE  = "defaultlang";
   private static final String CARLETON_USER_PARAM   = "user";
   private static final String CARLETON_PIN_PARAM    = "pass";
   
   private static final String  HTML_BALANCE_REGEXP  = ".*<td align=center>\\$([\\d]+.[\\d]{2})</td>.*";
   private static final Pattern HTML_BALANCE_PATTERN = Pattern.compile(HTML_BALANCE_REGEXP);
   
   private static final String HTML_CONVENIENCE_REGEXP = ".*Convenience.*";
   private static final Pattern HTML_CONVENIENCE_PATTERN = Pattern.compile(HTML_CONVENIENCE_REGEXP);
   
   private static final String HTML_LOGINFAILED_REGEXP = "Login failed for user.*";
   private static final Pattern HTML_LOGINFAILED_PATTERN = Pattern.compile(HTML_LOGINFAILED_REGEXP);
    
   private final Context ctx;
   private final CookieStore cookieStore   = new BasicCookieStore();
   private final HttpContext localContext  = new BasicHttpContext();
   private final HttpClient  httpClient;
   private final String      user;
   private final String      pin;
   
   private final CUCampusCardBalanceActivity mainUI;
   
   public CUBalanceFetcher(String user, String pin, CUCampusCardBalanceActivity mainUI) throws Exception
   { 
     this.ctx        = mainUI.getApplicationContext();
     this.httpClient = setupClient();
     this.user       = user;
     this.pin        = pin;
     this.mainUI     = mainUI;
   }
   
   @Override
   protected CUBalanceResult doInBackground(Void... params)
   {
     CUBalanceResult result = new CUBalanceResult();
     
     try
     {     
       if(!submitLogin(user, pin))
         result.setError("Incorrect username or password");
       else
         result.setBalance(Float.parseFloat(getBalance()));
     } catch(NumberFormatException e) {
       result.setError("Non-numeric balance returned by Carleton servers.");
     } catch(IllegalStateException e) {
       result.setError("Unable to connect over HTTPS. Try connecting from a different network (Wi-Fi or 3G)");      
     } catch(SSLPeerUnverifiedException e) {
       result.setError("Unable to verify the identity of Carleton's servers. Try connecting from a different network (Wi-Fi or 3G) or updating this app");      
     } catch (ClientProtocolException e) {
       result.setError("Protocol exception connecting to Carleton servers.");
     } catch (IOException e) {
       result.setError("Unable to connect to Carleton servers.");
     }
     
     return result;
   }
   
   protected void onPostExecute(CUBalanceResult result) 
   {
     mainUI.updateBalance(result);
   }
   
   private HttpClient setupClient() throws Exception
   {
     String[] pins = new String[] {CU_HTTPS_PIN};
     final SchemeRegistry schemeRegistry = new SchemeRegistry();
     
     schemeRegistry.register(new Scheme("https", new PinningSSLSocketFactory(ctx, pins, 0), 443));
    
     final HttpParams params = new BasicHttpParams();
     HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
     HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
            
     final ThreadSafeClientConnManager cm = new ThreadSafeClientConnManager(params, schemeRegistry);
     localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
     
     return new DefaultHttpClient(cm, params);
 
   }
 
   public boolean submitLogin(final String sid, final String pin) throws ClientProtocolException, IllegalStateException, IOException
   {       
     HttpPost loginPost = new HttpPost(CARLETON_LOGIN_URL);
     
     //loginPost.setHeader("Referer",     CARLETON_LOGIN_REF);
     //loginPost.setHeader("Cookie",      CARLETON_LOGIN_COOKIE); 
     loginPost.setHeader("User-Agent",  FF_USER_AGENT);
 
     List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
     nameValuePairs.add(new BasicNameValuePair(CARLETON_USER_PARAM, sid));
     nameValuePairs.add(new BasicNameValuePair(CARLETON_PIN_PARAM, pin));
     loginPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
     
     HttpResponse response = httpClient.execute(loginPost, localContext);
     
     InputStream       input = response.getEntity().getContent();
     InputStreamReader isr   = new InputStreamReader(input);
     BufferedReader    br    = new BufferedReader(isr);
     String            line  = null;
     boolean failed = false;		
     
     while((line = br.readLine()) != null) 
     {
       Matcher m = HTML_LOGINFAILED_PATTERN.matcher(line);
       //Check to see if the page includes a "Login failed" message
       if(m.matches())
     	  failed=true;
       
     }
     
	  List<Cookie> cookies = cookieStore.getCookies();
	  
	  //If we managed to get a non-zero session cookie and didn't get a login failed message, we're good.
	  for(Cookie c : cookies)
		  if(c.getName().equals(CARLETON_SESH_COOKIE) && !c.getValue().equals("") && !failed)
			  return true;
  
     //Otherwise there was a problem with logging in.
     return false;
   }
   
   public String getBalance() throws ClientProtocolException, IOException
   {
     String balance     = "";
     HttpGet balanceGet = new HttpGet(CARLETON_BALANCE_URL);
     
     balanceGet.setHeader("Referer",     CARLETON_LOGIN_REF);
     balanceGet.setHeader("User-Agent",  FF_USER_AGENT);
 
     HttpResponse response = httpClient.execute(balanceGet, localContext);
     
     InputStream       input = response.getEntity().getContent();
     InputStreamReader isr   = new InputStreamReader(input);
     BufferedReader    br    = new BufferedReader(isr);
     String            line  = null;
     
     while((line = br.readLine()) != null) 
     {
       Matcher m = HTML_CONVENIENCE_PATTERN.matcher(line);
 
       //Find the line preceding the correct balance line (may be multiple balances)
       if(m.matches())
       {
         //Skip ahead two lines and try to match the balance value
         line = br.readLine();
         line = br.readLine();
         line = br.readLine();
         Matcher newm = HTML_BALANCE_PATTERN.matcher(line);
         
         if(newm.matches())
         	balance = newm.group(1);
       }
     }
 
     return balance;
   }
 }
