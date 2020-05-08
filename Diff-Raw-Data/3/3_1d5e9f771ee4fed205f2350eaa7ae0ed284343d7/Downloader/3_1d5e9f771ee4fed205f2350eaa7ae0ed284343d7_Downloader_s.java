 package tv.nilsson.dnsync;
 
 import android.net.Uri;
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.protocol.ClientContext;
 import org.apache.http.cookie.Cookie;
 import org.apache.http.impl.client.BasicCookieStore;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.HttpConnectionParams;
 import org.apache.http.params.HttpParams;
 import org.apache.http.protocol.BasicHttpContext;
 import org.apache.http.protocol.HTTP;
 import org.apache.http.protocol.HttpContext;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.net.URI;
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class Downloader {
   private static String SERVICE_ENDPOINT = "http://kund.dn.se/service/pdf/";
   private static String LOGIN_ENDPOINT = "http://kund.dn.se/";
   public static final int TIMEOUT_MS = 30000;
   private String customerNr;
   private String firstName;
   private String lastName;
   private HttpContext httpContext;
   private final BasicCookieStore cookieStore;
 
   public static class DownloadInfo {
     public Uri uri;
     public String filename;
 
     public DownloadInfo(Uri uri, String filename) {
       this.uri = uri;
       this.filename = filename;
     }
   }
 
   public Downloader(String customerNr, String firstName, String lastName) {
     this.customerNr = customerNr;
     this.firstName = firstName;
     this.lastName = lastName;
 
     this.httpContext = new BasicHttpContext();
     this.cookieStore = new BasicCookieStore();
     this.httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
   }
 
   public DownloadInfo obtainDownloadInfo() throws DownloadException {
     try {
       doLogin();
       HttpClient httpClient = newHttpClient();
 
       HttpGet request = new HttpGet(SERVICE_ENDPOINT);
       HttpResponse response = httpClient.execute(request, httpContext);
 
       if (response.getStatusLine().getStatusCode() != 200) throw new DownloadUnexpectedResponseException();
 
       String s = extractEntity(response);
 
       Pattern pattern = Pattern.compile("/service/download/(\\d\\d\\d\\d)(\\d\\d)(\\d\\d)/DN.pdf");
 
       Matcher matcher = pattern.matcher(s);
       if (matcher.find()) {
         String filename = MessageFormat.format("{0}{1}{2}_DN.pdf", matcher.group(1), matcher.group(2), matcher.group(3));
         Uri downloadUri = Uri.parse(SERVICE_ENDPOINT).buildUpon().path(matcher.group(0)).build();
 
         return new DownloadInfo(downloadUri, filename);
       }
       else {
         throw new DownloadUnexpectedResponseException();
       }
     }
     catch (IOException e) {
       throw new DownloadIOException();
     }
     catch(RuntimeException e) {
       throw new DownloadInternalErrorException(e);
     }
   }
 
   private boolean isLoggedIn() {
     for (Cookie cookie : cookieStore.getCookies()) {
       if (cookie.getName().equals("sessionid")) return true;
     }
 
     return false;
   }
 
   private String fetchCsrfToken() throws IOException, DownloadException {
     HttpClient httpClient = newHttpClient();
 
     HttpResponse response = httpClient.execute(new HttpGet(LOGIN_ENDPOINT), httpContext);
 
     if (response.getStatusLine().getStatusCode() != 200) throw new DownloadUnexpectedResponseException();
 
     String responseString = extractEntity(response);
     Pattern pattern = Pattern.compile("name='csrfmiddlewaretoken' value='(.*?)'");
 
     Matcher matcher = pattern.matcher(responseString);
     boolean found = matcher.find();
     if (!found) throw new DownloadUnexpectedResponseException();
 
     return matcher.group(1);
   }
 
   private void doLogin() throws IOException, DownloadException {
     HttpClient httpClient = newHttpClient();
 
     String csrfToken = fetchCsrfToken();
 
     List<NameValuePair> params = new ArrayList<NameValuePair>();
     params.add(new BasicNameValuePair("csrfmiddlewaretoken", csrfToken));
     params.add(new BasicNameValuePair("plugin_template", "first_page"));
     params.add(new BasicNameValuePair("redirect_to", ""));
     params.add(new BasicNameValuePair("customer_number", customerNr));
    params.add(new BasicNameValuePair("first_name", firstName));
    params.add(new BasicNameValuePair("last_name", lastName));
 
     HttpPost request = new HttpPost(LOGIN_ENDPOINT);
     request.addHeader("Referer", LOGIN_ENDPOINT);
     request.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
     HttpResponse response = httpClient.execute(request, httpContext);
 
     int status = response.getStatusLine().getStatusCode();
     if (status != 200) {
       throw new AuthenticationFailedException();
     }
 
     if (!isLoggedIn()) throw new AuthenticationFailedException();
   }
 
   private String extractEntity(HttpResponse response) throws DownloadException {
     try {
       ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
       response.getEntity().writeTo(byteArrayOutputStream);
       return new String(byteArrayOutputStream.toByteArray());
     }
     catch(IOException e) {
       throw new DownloadUnexpectedResponseException();
     }
   }
 
   public static HttpClient newHttpClient() {
     HttpParams httpParameters = new BasicHttpParams();
 
     HttpConnectionParams.setConnectionTimeout(httpParameters, TIMEOUT_MS);
     HttpConnectionParams.setSoTimeout(httpParameters, TIMEOUT_MS);
 
     return new DefaultHttpClient(httpParameters);
   }
 
   /**
    * Open a URI using the context of the downloader (auth, cookies etc)
    *
    * @param uri
    * @return Entity of response
    * @throws IOException
    */
   public HttpEntity openUri(Uri uri) throws IOException {
     HttpClient httpClient = Downloader.newHttpClient();
     HttpResponse response = httpClient.execute(new HttpGet(URI.create(uri.toString())), httpContext);
     return response.getEntity();
   }
 
 }
