 package autobahn.android;
 
 import android.content.Context;
 import android.util.Log;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.lang.reflect.Type;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.List;
 import com.example.autobahn.R;
 
 import com.google.gson.Gson;
 import com.google.gson.JsonParseException;
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.CookieStore;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.protocol.ClientContext;
 import org.apache.http.cookie.Cookie;
 import org.apache.http.impl.client.BasicCookieStore;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.protocol.BasicHttpContext;
 import org.apache.http.protocol.HttpContext;
 
 public class AutobahnClient {
 
     private static String LOGIN_URL = "/autobahn-gui/j_spring_security_check";
     private static String IDMS_URL = "/autobahn-gui/portal/secure/android/idms";
 
     private HttpClient httpclient;
     private String scheme;
     private String host;
     private int port;
     boolean isLogIn;
     private String userName;
     private String password;
     private HttpContext localContext;
     private Context context = null;
     private List<String> idms = new ArrayList();
     private List<Circuit> circuits = new ArrayList();
 
     private String TAG = "WARN";
 
     static AutobahnClient instance = null;
 
     public static AutobahnClient getInstance() {
         if(instance == null)
             instance = new AutobahnClient();
 
         return instance;
     }
 
     public AutobahnClient() {
         httpclient = new DefaultHttpClient();
         scheme = "http";
         isLogIn = false;
         CookieStore cookieStore = new BasicCookieStore();
         localContext = new BasicHttpContext();
         localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
         //TODO get the host from a property
         host = "62.217.125.174";
     }
 
     public void setContext(Context context) {
         this.context = context;
     }
 
     public void setHost(String s) {
         host = s;
     }
 
     public void setPort(int p) {
         port = p;
     }
 
     public void setUserName(String s) {
         userName = s;
     }
 
     public String getUserName() {
         return userName;
     }
 
     public void setPassword(String pass) {
         password = pass;
     }
 
     public String getPassword() {
         return password;
     }
 
     public boolean hasAuthenticate() {
         return isLogIn;
     }
 
     public void logIn() throws AutobahnClientException {
         String query = "j_username=" + userName + "&j_password=" + password + "&_spring_security_remember_me=true";
         URI url = null;
         HttpPost httppost = null;
         try {
             url = new URI(scheme, null , host ,port, LOGIN_URL ,query,null);
             httppost = new HttpPost(url);
             HttpResponse response = httpclient.execute(httppost, localContext);
 
         } catch (URISyntaxException e) {
             String error = context.getString(R.string.net_error);
             AutobahnClientException ex = new AutobahnClientException(error);
             throw ex;
         } catch (ClientProtocolException e) {
             String error = context.getString(R.string.net_error);
             AutobahnClientException ex = new AutobahnClientException(error);
             throw ex;
         } catch (IOException e) {
             String error = context.getString(R.string.net_error);
             AutobahnClientException ex = new AutobahnClientException(error);
             throw ex;
         }
 
         CookieStore cookieStore = (CookieStore) localContext.getAttribute(ClientContext.COOKIE_STORE);
         for (Cookie c : cookieStore.getCookies()) {
             if (c.getName().equals("SPRING_SECURITY_REMEMBER_ME_COOKIE"))
                 isLogIn = true;
         }
         if (!isLogIn) {
             String error = context.getString(R.string.login_failed);
             AutobahnClientException ex = new AutobahnClientException(error);
             throw ex;
         }
     }
 
     /*
         returns the track circuit than have been fetched previously from fetchTrackCircuit
      */
     public List<Circuit> getTrackCircuits() {
         return circuits;
     }
 
     public void fetchTrackCircuit(String idm) {
 
           //TODO
     }
 
     public void fetchIdms() throws AutobahnClientException {
 
         idms.clear();
         URI url= null;
         HttpGet httpget=null;
         HttpResponse response=null;
 
         try {
             String s="DF"+IDMS_URL;
             url = new URI(scheme, null , host ,port,IDMS_URL, null,null);
             httpget = new HttpGet(url);
             response = httpclient.execute(httpget,localContext);
 
         } catch (URISyntaxException e) {
             String error=context.getString(R.string.net_error);
             AutobahnClientException ex=new AutobahnClientException(error);
             throw ex;
         } catch (ClientProtocolException e) {
             String error=context.getString(R.string.net_error);
             AutobahnClientException ex=new AutobahnClientException(error);
             throw ex;
         } catch (IOException e) {
             String error=context.getString(R.string.net_error);
             AutobahnClientException ex=new AutobahnClientException(error);
             throw ex;
         }
 
         String json=null;
         try {
             json =getASCIIContentFromEntity(response.getEntity());
         } catch (IOException e) {
             String error=context.getString(R.string.net_error);
             AutobahnClientException ex=new AutobahnClientException(error);
             throw ex;
         }
 
         Gson gson = new Gson();
         ArrayList<String> l = new ArrayList<String>();
 
         try {
             l=gson.fromJson(json,l.getClass());
         }catch (JsonParseException e) {
             String error=context.getString(R.string.net_error);
             AutobahnClientException ex=new AutobahnClientException(error);
             throw ex;
         }
 
         idms=l;
     }
 
 
     /*
     public void fetchTrackCircuit(String idm) {
         circuits.clear();
 
         Circuit c = new Circuit();
         c.id = 1;
         c.capacity = 10000;
         c.mtu = -1;
         c.state = Circuit.ReservationState.ACTIVE;
         c.startTime = Calendar.getInstance();
         c.endTime = Calendar.getInstance();
         c.startVlan = 0;
         c.endVlan = 0;
         c.startPort = new Port("Port_1","GARR");
         c.endPort = new Port("Port_2","GARR");
         circuits.add(c);
 
         c=new Circuit();
         c.id=2;
         c.capacity=1000;
         c.mtu=500;
         c.state=Circuit.ReservationState.FAILED;
         c.startTime= Calendar.getInstance();
         c.endTime=Calendar.getInstance();
         c.startPort=new Port("GARR.Port_1","GARR","client_1");
         c.endPort=new Port("GRNET.Port_2","GRNET");
         c.startVlan=10;
         c.endVlan=4000;
         circuits.add(c);
     }
      */
 
     public List<String> getIdms() {
         return idms;
     }
 
     /*
     public void fetchIdms() {
         idms.clear();
         idms.add(new String("GARR"));
         idms.add(new String("GRNET"));
     }
       */
     /*
 
     public void fetchIdms() {
 
         Log.d(TAG,"idms");
         HttpClient httpClient = new DefaultHttpClient();
         //HttpContext localContext = new BasicHttpContext();
                    ///autobahn-gui/j_spring_security_check
         String text = "";
         try {
             URI url= new URI("http" , null , "62.217.125.174" ,8080, "/autobahn-gui/portal/secure/rest/idms" ,null,null);
             HttpGet httpGet = new HttpGet(url);
             Header uname=new BasicHeader("username","demoadmin");
            Header pass=new BasicHeader("password","demoadmin");
            httpGet.addHeader(uname);
           httpGet.addHeader(pass);
             HttpResponse response = httpClient.execute(httpGet);
             org.apache.http.HttpEntity entity = response.getEntity();
 
             text = getASCIIContentFromEntity(entity);
 
 
         } catch (Exception e) {
             Log.e(TAG, e.getLocalizedMessage(), e);
             return ;
         }
         Log.d(TAG,text);
         JSONArray json = null;
         try {
             json = new JSONArray(text);
             for(int i = 0; i < json.length(); i++){
                 String s  = (String) json.get(i);
                 Log.d(TAG,"S "+s);
                 idms.add(s);
             }
         } catch (JSONException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
 
 
 
 
     }
     */
 
 
    /*
     private String getASCIIContentFromEntity(HttpEntity entity) throws IllegalStateException, IOException {
         InputStream in = entity.getContent();
         StringBuffer out = new StringBuffer();
         int n = 1;
         while (n>0) {
             byte[] b = new byte[4096];
             n =  in.read(b);
             if (n>0) out.append(new String(b, 0, n));
         }
         return out.toString();
     }
      */
 }
