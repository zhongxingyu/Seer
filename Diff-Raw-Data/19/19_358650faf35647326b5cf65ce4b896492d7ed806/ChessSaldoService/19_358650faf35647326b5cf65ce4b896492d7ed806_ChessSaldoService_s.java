 package com.chess.saldo.service;
 
 import com.chess.saldo.service.entities.Saldo;
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.conn.ClientConnectionManager;
 import org.apache.http.conn.scheme.PlainSocketFactory;
 import org.apache.http.conn.scheme.Scheme;
 import org.apache.http.conn.scheme.SchemeRegistry;
 import org.apache.http.conn.ssl.SSLSocketFactory;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.impl.client.DefaultRedirectHandler;
 import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.HttpConnectionParams;
 import org.apache.http.protocol.HTTP;
 import org.apache.http.protocol.HttpContext;
 import org.apache.http.util.EntityUtils;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.security.KeyStore;
 import java.util.ArrayList;
 import java.util.List;
 
 public class ChessSaldoService {
    private static final int CONN_TIMEOUT = 6 * 1000;
    private static final int SOCKET_TIMEOUT = 20 * 1000;
 
 
     private String username;
     private String password;
     private final DefaultHttpClient client;
 
     public ChessSaldoService(String username, String password) {
         this();
         this.username = username;
         this.password = password;
     }
 
     public ChessSaldoService() {
         this.client = createHttpClient();
         client.setRedirectHandler(new EnhancedRedirectHandler());
     }
 
     public synchronized Saldo fetchSaldo() throws IOException, ServiceException {
         HttpPost request = createRequest();
         HttpResponse response = client.execute(request);
 
         return parseResponse(response);
     }
 
     private Saldo parseResponse(HttpResponse response) throws IOException, ServiceException {
         String htmlText = EntityUtils.toString(response.getEntity(), HTTP.UTF_8);
         response.getEntity().consumeContent();
         return SaldoParser.parsePage(htmlText);
     }
 
     private HttpPost createRequest() {
         try {
             HttpPost httpPost = new HttpPost("https://www.chess.no/");
             List<NameValuePair> params = new ArrayList<NameValuePair>();
             params.add(new BasicNameValuePair("c_", "CHS_MyPageController"));
             params.add(new BasicNameValuePair("m_", "submitMyPageForm"));
             params.add(new BasicNameValuePair("submitLogin", "Logg+inn"));
             params.add(new BasicNameValuePair("cid", "403"));
             params.add(new BasicNameValuePair("username", username));
             params.add(new BasicNameValuePair("password", password));
             UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, HTTP.UTF_8);
             httpPost.setEntity(entity);
             return httpPost;
         } catch (UnsupportedEncodingException e) {
             throw new RuntimeException(e);
         }
     }
 
     public String getUsername() {
         return username;
     }
 
     public void setUsername(String username) {
         this.username = username;
     }
 
     public String getPassword() {
         return password;
     }
 
     public void setPassword(String password) {
         this.password = password;
     }
 
     private static DefaultHttpClient createHttpClient() {
         try {
             KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
             trustStore.load(null, null);
 
             SSLSocketFactory sf = new CarelessSSLSocketFactory(trustStore);
             sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
 
             SchemeRegistry registry = new SchemeRegistry();
             registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
             registry.register(new Scheme("https", sf, 443));
 
             BasicHttpParams basicHttpParams = new BasicHttpParams();
             HttpConnectionParams.setConnectionTimeout(basicHttpParams, CONN_TIMEOUT);
             HttpConnectionParams.setSoTimeout(basicHttpParams, SOCKET_TIMEOUT);
 
             ClientConnectionManager ccm = new ThreadSafeClientConnManager(basicHttpParams, registry);
             return new DefaultHttpClient(ccm, basicHttpParams);
         } catch (Exception ex) {
             throw new RuntimeException(ex);
         }
     }
 
     private class EnhancedRedirectHandler extends DefaultRedirectHandler {
 
         @Override
         public boolean isRedirectRequested(HttpResponse response, HttpContext context) {
             boolean isRedirect = super.isRedirectRequested(response, context);
             if (!isRedirect) {
                 int responseCode = response.getStatusLine().getStatusCode();
                 if (responseCode == 301 || responseCode == 302) {
                     return true;
                 }
             }
             return isRedirect;
         }
     }
 
 }
