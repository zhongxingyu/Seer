 package com.hoccer.tools;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 import java.net.SocketException;
 import java.net.SocketTimeoutException;
 import java.net.URI;
 import java.net.URLEncoder;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.http.Header;
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.ProtocolException;
 import org.apache.http.StatusLine;
 import org.apache.http.client.methods.HttpDelete;
 import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpHead;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpPut;
 import org.apache.http.client.methods.HttpRequestBase;
 import org.apache.http.client.params.HttpClientParams;
 import org.apache.http.entity.FileEntity;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.impl.client.DefaultRedirectHandler;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.HttpConnectionParams;
 import org.apache.http.params.HttpParams;
 import org.apache.http.protocol.HttpContext;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.sun.jndi.toolkit.url.Uri;
 
 public class HttpHelper {
 
     // Constants ---------------------------------------------------------
 
     private static final int    PUT_TIMEOUT  = 15 * 1000;
     private static final int    POST_TIMEOUT = 40 * 1000;
     private static final int    GET_TIMEOUT  = 60 * 1000;
     private static final String LOG_TAG      = "HttpHelper";
 
     public static HttpResponse putXML(String uri, String body) throws IOException,
             HttpClientException, HttpServerException {
         HttpPut put = new HttpPut(uri);
         insertXML(body, put);
         return executeHTTPMethod(put, PUT_TIMEOUT);
     }
 
     public static HttpResponse putUrlEncoded(String pUrl, Map<String, String> pData)
             throws IOException, HttpClientException, HttpServerException {
 
         HttpPut put = new HttpPut(pUrl);
         String body = urlEncodeKeysAndValues(pData);
         insertUrlEncodedAcceptingJson(body, put);
 
         return executeHTTPMethod(put, PUT_TIMEOUT);
     }
 
     public static String putText(String pUri, String pData) throws IOException,
             HttpClientException, HttpServerException {
         HttpPut put = new HttpPut(pUri);
         insert(pData, "text/xml", "text/xml", put);
         StatusLine statusLine = executeHTTPMethod(put, PUT_TIMEOUT).getStatusLine();
         return statusLine.getStatusCode() + " " + statusLine.getReasonPhrase();
     }
 
     public static String putAsString(String pUri, String pData) throws IOException,
             HttpClientException, HttpServerException {
         HttpPut put = new HttpPut(pUri);
         insert(pData, "text/plain", "text/plain", put);
         StatusLine statusLine = executeHTTPMethod(put, PUT_TIMEOUT).getStatusLine();
         return statusLine.getStatusCode() + " " + statusLine.getReasonPhrase();
     }
 
     public static String putAsString(String pUri, String pData, String pMimeType)
             throws IOException, HttpClientException, HttpServerException {
         HttpPut put = new HttpPut(pUri);
         insert(pData, pMimeType, "text/plain", put);
         StatusLine statusLine = executeHTTPMethod(put, PUT_TIMEOUT).getStatusLine();
         return statusLine.getStatusCode() + " " + statusLine.getReasonPhrase();
     }
 
     public static HttpResponse putFile(String pUri, File pFile, String pContentType, String pAccept)
             throws IOException, HttpClientException, HttpServerException {
         HttpPut put = new HttpPut(pUri);
         FileEntity entity = new FileEntity(pFile, pContentType);
         put.setEntity(entity);
         put.addHeader("Content-Type", pContentType);
         put.addHeader("Accept", pAccept);
         return executeHTTPMethod(put, PUT_TIMEOUT);
     }
 
     public static String postXML(String uri, String body) throws IOException, HttpClientException,
             HttpServerException {
         HttpPost post = new HttpPost(uri);
         insertXML(body, post);
         HttpResponse result = executeHTTPMethod(post, POST_TIMEOUT);
         return extractBodyAsString(result.getEntity());
     }
 
     public static String postString(String uri, String body) throws IOException,
             HttpClientException, HttpServerException {
         HttpPost post = new HttpPost(uri);
         insertXML(body, post);
         HttpResponse result = executeHTTPMethod(post, POST_TIMEOUT);
         return extractBodyAsString(result.getEntity());
     }
 
     public static HttpResponse getPostXMLResponse(String uri, String body) throws IOException,
             HttpClientException, HttpServerException {
         HttpPost post = new HttpPost(uri);
         insert(body, "text/plain", "text/plain", post);
         return executeHTTPMethod(post, POST_TIMEOUT, false);
     }
 
     public static String post(String uri, String body, String pContentType, String pAcceptMimeType)
             throws IOException, HttpClientException, HttpServerException {
 
         HttpResponse response = post(uri, body, pContentType, pAcceptMimeType, POST_TIMEOUT);
         return extractBodyAsString(response.getEntity());
     }
 
     public static HttpResponse post(String uri, String body, String pContentType,
             String pAcceptMimeType, int pTimeout) throws IOException, HttpClientException,
             HttpServerException {
 
         HttpPost post = new HttpPost(uri);
         insert(body, pContentType, pAcceptMimeType, post);
         return executeHTTPMethod(post, pTimeout);
     }
 
     public static InputStream getAsInStream(String uri_string) throws IOException,
             HttpClientException, HttpServerException {
 
         HttpGet get = new HttpGet(uri_string);
         HttpEntity entity = executeHTTPMethod(get).getEntity();
 
         ByteArrayOutputStream ostream = new ByteArrayOutputStream();
         entity.writeTo(ostream);
 
         ByteArrayInputStream istream = new ByteArrayInputStream(ostream.toByteArray());
         return istream;
     }
 
     public static HttpResponse delete(String uri) throws IOException, HttpClientException,
             HttpServerException {
         HttpDelete del = new HttpDelete(uri);
         return executeHTTPMethod(del);
     }
 
     public static void deleteAndIgnoreNotFound(String uri) throws IOException, HttpClientException,
             HttpServerException {
         try {
             delete(uri);
         } catch (HttpClientException e) {
             if (e.getHttpResponse().getStatusLine().getStatusCode() != 404) {
                 throw e;
             }
         }
     }
 
     @Deprecated
     public static String getAsString(Uri uri) throws IOException, HttpClientException,
             HttpServerException {
         return getAsString(uri.toString());
     }
 
     public static String getAsString(String uri) throws IOException, HttpClientException,
             HttpServerException {
         HttpEntity result = getAsHttpEntity(uri);
         return extractBodyAsString(result);
     }
 
     @Deprecated
     public static byte[] getAsByteArray(Uri uri) throws IllegalStateException, IOException,
             HttpClientException, HttpServerException {
         return getAsByteArray(uri.toString());
     }
 
     public static byte[] getAsByteArray(String uri) throws IllegalStateException, IOException,
             HttpClientException, HttpServerException {
 
         HttpEntity result = getAsHttpEntity(uri);
         return extractBodyAsByteArray(result);
     }
 
     public static HttpResponse get(String pUri) throws IOException, HttpClientException,
             HttpServerException {
 
         HttpGet get = new HttpGet(pUri);
         return executeHTTPMethod(get);
     }
 
     public static HttpResponse fetchUriToFile(String uriString, String filename)
             throws IOException, HttpClientException, HttpServerException {
 
         HttpGet get = new HttpGet(uriString);
         HttpResponse response = executeHTTPMethod(get);
         HttpEntity entity = response.getEntity();
 
         FileOutputStream fstream = new FileOutputStream(filename);
         entity.writeTo(fstream);
         fstream.flush();
         fstream.close();
         return response;
     }
 
     public static JSONObject getJson(String pUrl) throws JSONException, IOException,
             HttpClientException, HttpServerException {
 
         String url = toJsonUrl(pUrl);
         String result = getAsString(url);
 
         return new JSONObject(result);
     }
 
     public static String getLocationHeader(String url) throws IOException, HttpClientException,
             HttpServerException {
 
         HttpHead head = new HttpHead(url);
         HttpResponse response = executeHTTPMethod(head);
 
         if (!response.containsHeader("Location")) {
 
             throw new RuntimeException("Could not retrieve location header.");
         }
 
         Header locationHeader = response.getFirstHeader("Location");
         return locationHeader.getValue();
     }
 
     public static long getSize(String url) throws IOException, HttpClientException,
             HttpServerException {
 
         HttpHead head = new HttpHead(url);
         HttpResponse response = executeHTTPMethod(head);
 
         if (!response.containsHeader("Content-Length")) {
             throw new RuntimeException("Could not retrieve content-length header.");
         }
 
         return new Long(response.getFirstHeader("Content-Length").getValue());
     }
 
     public static int getStatusCode(String url) throws IOException {
         return getStatusCode(url, GET_TIMEOUT);
     }
 
     public static int getStatusCode(String url, int timeout) throws IOException {
 
         HttpHead head = new HttpHead(url);
         try {
 
             HttpResponse response = executeHTTPMethod(head, timeout);
             return response.getStatusLine().getStatusCode();
 
         } catch (HttpException ex) {
             return ex.getStatusCode();
         }
     }
 
     public static HttpResponse postUrlEncoded(String pUrl, Map<String, String> pData)
             throws IOException, HttpClientException, HttpServerException {
 
         String body = urlEncodeKeysAndValues(pData);
         return postUrlEncoded(pUrl, body);
     }
 
     public static HttpResponse postUrlEncoded(String pUrl, String pUrlEncodedData)
             throws IOException, HttpClientException, HttpServerException {
 
         HttpPost post = new HttpPost(pUrl);
 
         insertUrlEncodedAcceptingJson(pUrlEncodedData, post);
         return executeHTTPMethod(post);
     }
 
     public static String urlEncodeKeysAndValues(Map<String, String> pData) {
 
         StringBuffer tmp = new StringBuffer();
         Set keys = pData.keySet();
         int idx = 0;
         for (Object key : keys) {
             tmp.append(URLEncoder.encode(String.valueOf(key)));
             tmp.append("=");
             tmp.append(URLEncoder.encode(String.valueOf(pData.get(key))));
 
             idx += 1;
             if (idx < keys.size()) {
                 tmp.append("&");
             }
         }
 
         return tmp.toString();
     }
 
     public static String urlEncodeValues(Map<String, String> pData) {
 
         StringBuffer tmp = new StringBuffer();
         Set keys = pData.keySet();
         int idx = 0;
         for (Object key : keys) {
             tmp.append(String.valueOf(key));
             tmp.append("=");
             tmp.append(URLEncoder.encode(String.valueOf(pData.get(key))));
 
             idx += 1;
             if (idx < keys.size()) {
                 tmp.append("&");
             }
         }
 
         return tmp.toString();
     }
 
     public static String extractBodyAsString(HttpEntity entity) throws IOException {
 
         return extractBody(entity).toString();
     }
 
     public static byte[] extractBodyAsByteArray(HttpEntity entity) throws IllegalStateException,
             IOException {
 
         return extractBody(entity).toByteArray();
     }
 
     static HttpEntity getAsHttpEntity(String uri) throws IOException, HttpClientException,
             HttpServerException {
 
         HttpGet get = new HttpGet(uri);
         HttpResponse response = executeHTTPMethod(get);
         return response.getEntity();
     }
 
     static ByteArrayOutputStream extractBody(HttpEntity entity) throws IOException {
 
         ByteArrayOutputStream ostream = new ByteArrayOutputStream();
         entity.writeTo(ostream);
         // Logger.v(LOG_TAG, ostream.toString());
         return ostream;
     }
 
     static void insertXML(String body, HttpEntityEnclosingRequestBase method) {
 
         insert(body, "text/xml", "text/xml", method);
     }
 
     static void insertUrlEncodedAcceptingJson(String pEncodedBody,
             HttpEntityEnclosingRequestBase method) {
 
         insert(pEncodedBody, "application/x-www-form-urlencoded", "application/json", method);
     }
 
     static void insert(String pBody, String pContentType, String pAcceptMimeType,
             HttpEntityEnclosingRequestBase pMethod) {
 
         insert(pBody, pContentType, pMethod);
         pMethod.addHeader("Accept", pAcceptMimeType);
     }
 
     static void insert(String pBody, String pContentType, HttpEntityEnclosingRequestBase pMethod) {
 
         StringEntity entity;
         try {
             entity = new StringEntity(pBody);
             pMethod.setEntity(entity);
             pMethod.addHeader("Content-Type", pContentType);
         } catch (UnsupportedEncodingException e) {
             throw new RuntimeException(e);
         }
     }
 
     private static HttpResponse executeHTTPMethod(HttpRequestBase pMethod) throws IOException,
             HttpClientException, HttpServerException {
         return executeHTTPMethod(pMethod, GET_TIMEOUT);
     }
 
     private static HttpResponse executeHTTPMethod(HttpRequestBase pMethod, int pConnectionTimeout)
             throws IOException, HttpClientException, HttpServerException {
 
         HttpParams httpParams = new BasicHttpParams();
         HttpConnectionParams.setConnectionTimeout(httpParams, pConnectionTimeout);
         HttpConnectionParams.setSoTimeout(httpParams, pConnectionTimeout);
        HttpClientParams.setRedirecting(httpParams, true);
 
         DefaultHttpClient httpclient = new DefaultHttpClient(httpParams);
 
         // Log redirects
         httpclient.setRedirectHandler(new DefaultRedirectHandler() {
             @Override
             public URI getLocationURI(HttpResponse response, HttpContext context)
                     throws ProtocolException {
                 URI uri = super.getLocationURI(response, context);
                 return uri;
             }
         });
 
         HttpResponse response;
         try {
             response = httpclient.execute(pMethod);
         } catch (SocketTimeoutException e) {
             e = new SocketTimeoutException(e.getMessage() + ": " + pMethod.getURI());
             e.fillInStackTrace();
             throw e;
         } catch (SocketException e) {
             e = new SocketException(e.getMessage() + ": " + pMethod.getURI());
             e.fillInStackTrace();
             throw e;
         }
         HttpException.throwIfError(pMethod.getURI().toString(), response);
         return response;
     }
 
     private static HttpResponse executeHTTPMethod(HttpRequestBase pMethod, int pConnectionTimeout,
             Boolean pRedirect) throws IOException, HttpClientException, HttpServerException {
 
         HttpParams httpParams = new BasicHttpParams();
         HttpConnectionParams.setConnectionTimeout(httpParams, pConnectionTimeout);
         HttpConnectionParams.setSoTimeout(httpParams, pConnectionTimeout);
         if (!pRedirect) {
             HttpClientParams.setRedirecting(httpParams, false);
         }
         DefaultHttpClient httpclient = new DefaultHttpClient(httpParams);
 
         // Log redirects
         httpclient.setRedirectHandler(new DefaultRedirectHandler() {
             @Override
             public URI getLocationURI(HttpResponse response, HttpContext context)
                     throws ProtocolException {
                 URI uri = super.getLocationURI(response, context);
                 return uri;
             }
         });
 
         HttpResponse response;
         try {
             response = httpclient.execute(pMethod);
         } catch (SocketException e) {
             e = new SocketException(e.getMessage() + ": " + pMethod.getURI());
             e.fillInStackTrace();
             throw e;
         }
         HttpException.throwIfError(pMethod.getURI().toString(), response);
         return response;
     }
 
     private static String toJsonUrl(String pUrl) {
 
         if (pUrl.endsWith(".xml")) {
             // an xml uri means that someone is using this method in a wrong way
             // --> fail fast
             throw new IllegalArgumentException("HttpHelper was passed a Uri which explicitly "
                     + "asked for a different format: '" + pUrl + "'!");
         }
 
         // remove trailing slashes
         if (pUrl.endsWith("/")) {
 
             pUrl = pUrl.substring(0, pUrl.length() - 1);
         }
 
         // gracefully accept format-agnostic Uris
         if (!pUrl.endsWith(".json")) {
             pUrl = pUrl + ".json";
         }
 
         return pUrl;
     }
 
     public static boolean isReachable(String pUri) {
 
         HttpHead head = new HttpHead(pUri);
         try {
             executeHTTPMethod(head);
         } catch (HttpException e) {
             return false;
         } catch (IOException e) {
             return false;
         }
 
         return true;
     }
 
     public static String getHeadersAsString(Header[] pHeaders) {
         String headers = "";
 
         for (Header header : pHeaders) {
             headers += header.getName() + ":" + header.getValue() + "\n";
         }
 
         return headers;
     }
 }
