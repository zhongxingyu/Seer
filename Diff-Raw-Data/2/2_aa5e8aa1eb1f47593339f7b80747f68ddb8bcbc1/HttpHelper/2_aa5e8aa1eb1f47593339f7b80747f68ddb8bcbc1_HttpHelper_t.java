 package de.fiz.ddb.aas.test.util.http;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpDelete;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpPut;
 import org.apache.http.client.utils.URLEncodedUtils;
 import org.apache.http.conn.scheme.PlainSocketFactory;
 import org.apache.http.conn.scheme.Scheme;
 import org.apache.http.conn.scheme.SchemeRegistry;
 import org.apache.http.entity.ContentType;
 import org.apache.http.entity.InputStreamEntity;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.impl.conn.PoolingClientConnectionManager;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.HttpConnectionParams;
 import org.apache.http.params.HttpParams;
 import org.apache.http.util.EntityUtils;
 
 import de.fiz.ddb.aas.test.Constants;
 import de.fiz.ddb.aas.test.exceptions.HttpClientException;
 import de.fiz.ddb.aas.test.exceptions.HttpException;
 import de.fiz.ddb.aas.test.exceptions.HttpServerException;
 
 /**
  * Static Utility class for initializing http-connection, requesting resources with GET/POST/PUT/DELETE
  * and generation of Exceptions from Http-Resonse
  * 
  * @author mih
  * 
  */
 public class HttpHelper {
 
     private static HttpClient httpClient;
 
     private static HttpHelper instance;
 
     static {
         try {
             instance = new HttpHelper();
         }
         catch (final Exception e) {
         }
     }
 
     /**
      *  Private Constructor initializing HttpClient.
      */
     private HttpHelper() {
         final SchemeRegistry sr = new SchemeRegistry();
         final Scheme http = new Scheme("http", 80, PlainSocketFactory.getSocketFactory());
 
         // Schema für SSL Verbindungen
         // SSLSocketFactory sf = new
         // SSLSocketFactory(SSLContext.getInstance("TLS"));
         // sf.setHostnameVerifier(SSLSocketFactory.STRICT_HOSTNAME_VERIFIER);
         // Scheme https = new Scheme("https", sf, 443);
 
         sr.register(http);
         // sr.register(https);
 
         HttpParams httpParams = new BasicHttpParams();
        if (Constants.HTTP_CONNECTION_TIMEOUT >= 0) {
             HttpConnectionParams.setConnectionTimeout(httpParams, Constants.HTTP_CONNECTION_TIMEOUT);
         }
 
         final PoolingClientConnectionManager cm = new PoolingClientConnectionManager(sr);
         cm.setMaxTotal(Constants.HTTP_MAX_TOTAL_CONNECTIONS);
         cm.setDefaultMaxPerRoute(Constants.HTTP_MAX_CONNECTIONS_PER_HOST);
 
         httpClient = new DefaultHttpClient(cm, httpParams);
 
     }
 
     /**
      * get instance.
      * 
      * @return HttpHelper
      */
     public HttpHelper getInstance() {
         return instance;
     }
 
     /**
      * Create url-String from given parameters.
      * 
      * @param protocol protocol
      * @param host host
      * @param port port
      * @param context servlet-context-name
      * @param uri uri
      * @return String url
      */
     public static String createUrl(
         final String protocol, final String host, final String port, final String context, final String uri) {
         StringBuffer url = new StringBuffer();
         url.append(protocol);
         if (protocol.indexOf("://") == -1) {
             url.append("://");
         }
         url.append(host);
         if (port != null) {
             url.append(port);
         }
         if (context != null) {
             if (!context.startsWith("/")) {
                 url.append("/");
             }
             url.append(context);
         }
         if (uri != null) {
             if ((context == null || !context.endsWith("/")) && !uri.startsWith("/")) {
                 url.append("/");
             }
             url.append(uri);
         }
         return url.toString();
     }
 
     /**
      * create url to AAS from parameters stored in Constants
      * 
      * @param uri uri
      * @return String AAS-Url
      */
     public static String createAasUrl(final String uri) {
         StringBuffer url = new StringBuffer();
         url.append(Constants.PROTOCOL);
         if (Constants.PROTOCOL.indexOf("://") == -1) {
             url.append("://");
         }
         url.append(Constants.HOST);
         if (Constants.PORT != null) {
             url.append(":").append(Constants.PORT);
         }
         if (Constants.CONTEXT != null) {
             if (!Constants.CONTEXT.startsWith("/")) {
                 url.append("/");
             }
             url.append(Constants.CONTEXT);
         }
         if (uri != null) {
             if ((Constants.CONTEXT == null || !Constants.CONTEXT.endsWith("/")) && !uri.startsWith("/")) {
                 url.append("/");
             }
             url.append(uri);
         }
         return url.toString();
     }
 
     /**
      * Execute an http method.
      * Return Exception if HttpStatus >= 300.
      * 
      * @param method
      *            The http method.
      * @param url
      *            The url.
      * @param parameters
      *            The request parameters.
      * @param body
      *            The request body.
      * @param mimeType
      *            The MIME type.
      * @return The http Response.
      * @throws Exception
      *             If anything fails.
      */
     public static String executeHttpRequest(
         final String method, final String url, final Map<String, String[]> parameters, final Object body,
         final ContentType mimeType) throws HttpException {
         HttpResponse result = null;
         ContentType selectedMimeType = mimeType;
         if (selectedMimeType == null) {
             selectedMimeType = ContentType.APPLICATION_XML;
         }
 
         if (method != null) {
             try {
                 if (method.toUpperCase().equals(Constants.HTTP_METHOD_DELETE)) {
                     result = doDelete(url, selectedMimeType);
                 }
                 else if (method.toUpperCase().equals(Constants.HTTP_METHOD_GET)) {
                     result = doGet(url, parameters, selectedMimeType);
                 }
                 else if (method.toUpperCase().equals(Constants.HTTP_METHOD_POST)) {
                     result = doPost(url, body, selectedMimeType);
                 }
                 else if (method.toUpperCase().equals(Constants.HTTP_METHOD_PUT)) {
                     result = doPut(url, body, selectedMimeType);
                 }
             }
             catch (Exception e) {
                 throw new HttpException(e.getMessage(), 500);
             }
 
         }
         String resultStr = null;
         try {
             if (result.getStatusLine().getStatusCode() >= HttpStatus.SC_MULTIPLE_CHOICES) {
                 if (result.getStatusLine().getStatusCode() >= HttpStatus.SC_NOT_MODIFIED
                     && result.getStatusLine().getStatusCode() < HttpStatus.SC_INTERNAL_SERVER_ERROR) {
                     throw new HttpClientException(getErrorMessage(result), result.getStatusLine().getStatusCode());
                 }
                 else if (result.getStatusLine().getStatusCode() >= HttpStatus.SC_INTERNAL_SERVER_ERROR) {
                     throw new HttpServerException(getErrorMessage(result), result.getStatusLine().getStatusCode());
                 }
                 else {
                     throw new HttpException(getErrorMessage(result), result.getStatusLine().getStatusCode());
                 }
             }
             resultStr = "";
             if (result.getEntity() != null) {
                 resultStr = EntityUtils.toString(result.getEntity(), Constants.HTTP_DEFAULT_CHARSET);
             }
         }
         catch (IOException e) {
             throw new HttpException(e.getMessage(), 500);
         }
         return resultStr;
     }
 
     /**
      * Execute a http delete request on the given url.<br>
      * If Authentication-Object holds userId and password, set it as Auth-Header.
      * 
      * @param url
      *            The url.
      * @param contentType
      *            contentType (APPLICATION_XML or APPLICATION_JSON)
      * @return The http resonse.
      * @throws Exception
      *             If anything fails.
      */
     public static HttpResponse doDelete(final String url, final ContentType contentType) throws Exception {
 
         final HttpDelete method = new HttpDelete(url);
         method.setHeader("Accept", contentType.getMimeType());
         if (Authentication.getUserId() != null) {
             method.addHeader(Authentication.getAuthHeader());
         }
         final HttpResponse httpRes = httpClient.execute(method);
         return httpRes;
     }
 
     /**
      * Execute a http get request on the given url.<br>
      * If Authentication-Object holds userId and password, set it as Auth-Header.
      * 
      * @param url
      *            The url.
      * @param contentType
      *            contentType (APPLICATION_XML or APPLICATION_JSON)
      * @return The http resonse.
      * @throws Exception
      *             If anything fails.
      */
     public static HttpResponse doGet(
         final String url, final Map<String, String[]> parameters, final ContentType contentType) throws Exception {
 
         final HttpGet httpGet;
 
         if (parameters != null) {
 
             final List<NameValuePair> queryParameters = new ArrayList<NameValuePair>();
 
             for (final String parameter : parameters.keySet()) {
                 for (final String value : parameters.get(parameter)) {
                     queryParameters.add(new BasicNameValuePair(parameter, value));
                 }
             }
 
             final String formatted = URLEncodedUtils.format(queryParameters, "UTF-8");
 
             httpGet = new HttpGet(url + "?" + formatted);
         }
         else {
             httpGet = new HttpGet(url);
         }
         httpGet.setHeader("Accept", contentType.getMimeType());
         if (Authentication.getUserId() != null) {
             httpGet.addHeader(Authentication.getAuthHeader());
         }
 
         final HttpResponse httpRes = httpClient.execute(httpGet);
         return httpRes;
     }
 
     /**
      * Execute a http post request on the given url.<br>
      * If Authentication-Object holds userId and password, set it as Auth-Header.
      * 
      * @param url
      *            The url.
      * @param body
      *            The request body.
      * @param contentType
      *            contentType (APPLICATION_XML or APPLICATION_JSON)
      * @return The http resonse.
      * @throws Exception
      *             If anything fails.
      */
     public static HttpResponse doPost(final String url, final Object body, final ContentType contentType)
         throws Exception {
         final HttpPost httpPost = new HttpPost(url);
         httpPost.setHeader("Accept", contentType.getMimeType());
         if (Authentication.getUserId() != null) {
             httpPost.addHeader(Authentication.getAuthHeader());
         }
         HttpEntity requestEntity = null;
         if (body instanceof String) {
             requestEntity =
                 new StringEntity((String) body, ContentType.create(contentType.getMimeType(),
                     Constants.HTTP_DEFAULT_CHARSET));
         }
         else if (body instanceof InputStream) {
             requestEntity = new InputStreamEntity((InputStream) body, -1);
             httpPost.setHeader("Content-Type", contentType.getMimeType());
         }
         httpPost.setEntity(requestEntity);
 
         final HttpResponse httpRes = httpClient.execute(httpPost);
         return httpRes;
     }
 
     /**
      * Execute a http put request on the given url.<br>
      * If Authentication-Object holds userId and password, set it as Auth-Header.
      * 
      * @param url
      *            The url.
      * @param body
      *            The request body.
      * @param contentType
      *            contentType (APPLICATION_XML or APPLICATION_JSON)
      * @return The http resonse.
      * @throws Exception
      *             If anything fails.
      */
     public static HttpResponse doPut(final String url, final Object body, final ContentType contentType)
         throws Exception {
         final HttpPut httpPut = new HttpPut(url);
         httpPut.setHeader("Accept", contentType.getMimeType());
         if (Authentication.getUserId() != null) {
             httpPut.addHeader(Authentication.getAuthHeader());
         }
         HttpEntity requestEntity = null;
         if (body != null) {
             if (body instanceof String) {
                 requestEntity =
                     new StringEntity((String) body, ContentType.create(contentType.getMimeType(),
                         Constants.HTTP_DEFAULT_CHARSET));
 
             }
             else if (body instanceof InputStream) {
                 requestEntity = new InputStreamEntity(((InputStream) body), -1);
                 httpPut.setHeader("Content-Type", contentType.getMimeType());
             }
         }
         httpPut.setEntity(requestEntity);
         final HttpResponse httpRes = httpClient.execute(httpPut);
         return httpRes;
     }
 
     /**
      * Get Error Message from HttpResponse as String.
      * 
      * @param result HttpResponse
      * @return String Error Message from HttpResponse
      * @throws IOException
      */
     private static String getErrorMessage(HttpResponse result) throws IOException {
         StringBuilder errorMessage = new StringBuilder();
         errorMessage.append(result.getStatusLine()).append(" ");
         if (result.getEntity() != null) {
             errorMessage.append(EntityUtils.toString(result.getEntity(), Constants.HTTP_DEFAULT_CHARSET));
         }
 
         return errorMessage.toString();
     }
 
 }
