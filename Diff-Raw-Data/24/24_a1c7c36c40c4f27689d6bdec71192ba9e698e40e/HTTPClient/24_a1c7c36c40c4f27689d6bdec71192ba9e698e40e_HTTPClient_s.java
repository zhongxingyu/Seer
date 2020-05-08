 package org.stringtree.http;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.stringtree.codec.Base64;
 
 public class HTTPClient {
 
     public static final String HEAD = "HEAD";
     public static final String DELETE = "DELETE";
     public static final String TRACE = "TRACE";
     public static final String OPTIONS = "OPTIONS";
     public static final String POST = "POST";
     public static final String PUT = "PUT";
     public static final String GET = "GET";
     
     public static final String HTTP_DESTINATION_FILE = "http.destination.file";
 	public static final String HTTP_RESPONSE_CODE = "http.response.code";
 
     public static final String REDIRECTION = "3\\d\\d";
 
     private List<NameValue> headers;
     private Map<String, String> cookies;
     
     protected HTTPClient(List<NameValue> headers, Map<String, String> cookies) {
         this.headers = headers;
         this.cookies = cookies;
     }
     
     public HTTPClient() {
         this(new ArrayList<NameValue>(), new HashMap<String, String>());
     }
     
     public HTTPClient(String userAgent) {
         this();
         setHeader(Document.USER_AGENT, userAgent);
     }
 
     private void addHeader(NameValue header) {
         headers.add(header);
     }
     
     public void addHeader(String name, String value) {
         addHeader(new NameValue(name, value));
     }
     
     public void setHeader(String name, String value) {
         NameValue header = new NameValue(name, value);
         
         if (headers.contains(header)) {
             headers.remove(header);
         }
         
         addHeader(header);
     }
     
     public void setCookie(String name, String value) {
        cookies.put(name, "$Version=0; " + name + "=" + value);
     }
     
     public void setBasicAuthentication(String user, String password) {
     	addHeader(new NameValue("Authorization", "Basic " + Base64.encode((user + ":" + password))));
     }
     
     public Document request(String method, String url, Payload data) throws IOException {
         return request(method, url, data, HttpURLConnection.getFollowRedirects());
     }
     
     public Document request(String method, String url, Payload data, boolean followRedirects) throws IOException {
         return request(method, url, data, followRedirects, 0);
     }
     
     private Document request(String method, String url, Payload data, int timeout) throws IOException {
         return request(method, url, data, HttpURLConnection.getFollowRedirects(), timeout);
     }
     
     public Document request(String method, String url, Payload data, boolean followRedirects, int timeout) throws IOException {
 		URL remote = new URL(url);
 		HttpURLConnection connection = (HttpURLConnection) remote.openConnection();
         connection.setRequestMethod(method);
         connection.setDoOutput(true);
         connection.setDoInput(true);
         connection.setAllowUserInteraction(false);
         connection.setInstanceFollowRedirects(followRedirects);
         connection.setConnectTimeout(timeout);
         connection.setReadTimeout(timeout);
 
         for (String text : cookies.values()) {
             connection.addRequestProperty("Cookie", text);
         }
 
         for (NameValue entry : headers) {
             connection.addRequestProperty(entry.name, entry.value);
         }
         
 		if (null != data) {
 			for (NameValue header : data.getAllHeaders()) {
                 connection.addRequestProperty(header.name, header.value);
 			}
 
             byte[] bytes = data.getContentAsBytes();
             if (null != bytes && bytes.length > 0) {
                 connection.addRequestProperty(Document.CONTENT_LENGTH, Integer.toString(bytes.length));
                 connection.getOutputStream().write(bytes);
             }
 		} else {
             connection.addRequestProperty(Document.CONTENT_LENGTH, "0");
 		}
    
         Document ret = new Document();
         ret.addHeader(HTTP_RESPONSE_CODE, Integer.toString(connection.getResponseCode()));
         
         for (Map.Entry<String, List<String>> entry : connection.getHeaderFields().entrySet()) {
             String key = (String)entry.getKey();
             String value = (String)(entry.getValue()).get(0);
             if (null != key) {
                 ret.addHeader(key, value);
             }
         }
         
         InputStream in;
         try {
             in = connection.getInputStream();
         } catch (Exception e) {
             in = connection.getErrorStream();
         }
         try {
             int length = Integer.parseInt(connection.getHeaderField(Document.CONTENT_LENGTH));
             String dest = null==data ? null : data.getHeader(HTTP_DESTINATION_FILE);
             if (null != dest) {
                 copyStream(in, new FileOutputStream(dest));
             } else {
                 ret.setContent(readStream(in, length));
             }
         } catch (NumberFormatException nfe) {
             if (!isRedirection(ret.getHeader(HTTP_RESPONSE_CODE)))
                 ret.setContent(readStream(in));
         }
 
         return ret;
 	}
 
     public static boolean isRedirection(String responseCode) {
         return null == responseCode ? false : responseCode.matches(REDIRECTION);
     }
 
     public Document request(String method, String url, String type, String value) throws IOException {
         return request(method, url, contentDocument(type, value));
     }
     
     public Document request(String method, String url, String type, byte[] value) throws IOException {
         return request(method, url, contentDocument(type, value));
     }
     
 
 	public Document get(String url, Document data) throws IOException {
 		return request(GET, url, data);
 	}
 	
 	public Document get(String url) throws IOException {
 		return request(GET, url, null);
 	}
     
     public Document get(String url, boolean followRedirects) throws IOException {
         return request(GET, url, null, followRedirects);
     }
     
     public Document get(String url, int timeout) throws IOException {
         return request(GET, url, null, timeout);
     }
 
     public Document get(String url, boolean followRedirects, int timeout) throws IOException {
         return request(GET, url, null, followRedirects, timeout);
     }
     
     public Document getFile(String url, String filename) throws IOException {
         Document data = new Document();
         data.addHeader(HTTP_DESTINATION_FILE, filename);
         return request(GET, url, data);
     }
 	
 	public Document delete(String url, Payload data) throws IOException {
 		return request(DELETE, url, data);
 	}
 	
 	public Document delete(String url) throws IOException {
 		return request(DELETE, url, null);
 	}
 	
 	public Document head(String url, Payload data) throws IOException {
 		return request(HEAD, url, data);
 	}
 	
 	public Document head(String url) throws IOException {
 		return request(HEAD, url, null);
 	}
 	
 	public Document put(String url, Payload data) throws IOException {
 		return request(PUT, url, data);
 	}
     
     public Document put(String url, String type, String value) throws IOException {
         return request(PUT, url, type, value);
     }
     
     public Document put(String url, String type, byte[] value) throws IOException {
         return request(PUT, url, type, value);
     }
     
     public Document post(String url) throws IOException {
         return request(POST, url, null);
     }
 	
 	public Document post(String url, Payload data) throws IOException {
 		return request(POST, url, data);
 	}
     
     public Document post(String url, String type, String value) throws IOException {
         return request(POST, url, type, value);
     }
     
     public Document post(String url, String type, byte[] value) throws IOException {
         return request(POST, url, type, value);
     }
 	
 	public Document options(String url, Payload data) throws IOException {
 		return request(OPTIONS, url, data);
 	}
 	
 	public Document trace(String url, Payload data) throws IOException {
 		return request(TRACE, url, data);
 	}
 
 	
 	private Document contentDocument(String type, byte[] bytes) {
         Document data = new Document(bytes);
        data.addHeader(Document.CONTENT_TYPE, type);
         return data;
     }
     
     private Document contentDocument(String type, String value) {
         return contentDocument(type, value.getBytes());
     }
     
     private byte[] readStream(InputStream in, int len) {
         if (null == in) return new byte[0];
         
         byte[] ret = new byte[len];
 
         try {
             if (!(in instanceof BufferedInputStream)) {
                 in = new BufferedInputStream(in); 
             }
             for (int i = 0; i < len; ++i) {
                 int c = in.read();
                 if (-1 == c) break;
                 ret[i] = (byte)c;
             }
         } catch (IOException e) {
             e.printStackTrace();
         }
 
         return ret;
     }
     
     private byte[] readStream(InputStream in) {
         if (null == in) return new byte[0];
         
         ByteArrayOutputStream buf = new ByteArrayOutputStream();
 
         try {
             if (!(in instanceof BufferedInputStream)) {
                 in = new BufferedInputStream(in); 
             }
             
             for (int c = in.read(); -1 != c; c = in.read()) {
                 buf.write(c);
             }
         } catch (IOException e) {
             e.printStackTrace();
         }
 
         return buf.toByteArray();
     }
 
     private void copyStream(InputStream in, FileOutputStream out) throws IOException {
         OutputStream bout = new BufferedOutputStream(out);
         BufferedInputStream bin = new BufferedInputStream(in);
 
         for (int c = bin.read(); c >= 0; c = bin.read()) {
             bout.write(c);
         }
 
         bout.flush();
     }
 }
