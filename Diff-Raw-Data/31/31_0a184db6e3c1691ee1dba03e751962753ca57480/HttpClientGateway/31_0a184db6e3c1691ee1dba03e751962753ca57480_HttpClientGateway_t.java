 package com.shaubert.blogadapter.client;
 
 import org.apache.http.*;
 import org.apache.http.client.CookieStore;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpUriRequest;
 import org.apache.http.client.protocol.ClientContext;
 import org.apache.http.conn.scheme.PlainSocketFactory;
 import org.apache.http.conn.scheme.Scheme;
 import org.apache.http.conn.scheme.SchemeRegistry;
 import org.apache.http.conn.ssl.SSLSocketFactory;
 import org.apache.http.cookie.Cookie;
 import org.apache.http.entity.ByteArrayEntity;
 import org.apache.http.entity.HttpEntityWrapper;
 import org.apache.http.impl.client.BasicCookieStore;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.HttpParams;
 import org.apache.http.params.HttpProtocolParams;
 import org.apache.http.protocol.BasicHttpContext;
 import org.apache.http.protocol.HttpContext;
 
 import java.io.IOException;
 import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URI;
 import java.util.Map;
 import java.util.zip.GZIPInputStream;
 
 public class HttpClientGateway implements HttpGateway {
 
     public class HttpClientGatewayException extends IOException {
 
         private final int statusCode;
 
         public HttpClientGatewayException(int statusCode) {
             this((String) null, statusCode);
         }
 
         public HttpClientGatewayException(String detailMessage, int statusCode) {
             this(detailMessage, null, statusCode);
         }
 
         public HttpClientGatewayException(Throwable cause, int statusCode) {
             this(null, cause, statusCode);
         }
 
         public HttpClientGatewayException(String message, Throwable cause, int statusCode) {
             super(message, cause);
             this.statusCode = statusCode;
         }
 
         public int getStatusCode() {
             return statusCode;
         }
     }
 
     public static class GzipDecompressingEntity extends HttpEntityWrapper {
 
         public GzipDecompressingEntity(final HttpEntity entity) {
             super(entity);
         }
 
         @Override
         public InputStream getContent()
             throws IOException, IllegalStateException {
 
             // the wrapped entity's getContent() decides about repeatability
             InputStream wrappedin = wrappedEntity.getContent();
 
             return new GZIPInputStream(wrappedin);
         }
 
         @Override
         public long getContentLength() {
             // length of ungzipped content is not known
             return -1;
         }
 
     }
     
     private HttpClient httpClient;
     private CookieStore cookieStore;
 
     public HttpClientGateway(CookieStore cookieStore) {
         this.cookieStore = cookieStore;
         this.httpClient = createClient();
     }
 
     protected HttpClient createClient() {
         HttpParams params = new BasicHttpParams();
         HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
         HttpProtocolParams.setContentCharset(params, "utf-8");
         params.setBooleanParameter("http.protocol.expect-continue", false);
         
         SchemeRegistry registry = new SchemeRegistry();
         registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
         final SSLSocketFactory sslSocketFactory = SSLSocketFactory.getSocketFactory();
         sslSocketFactory.setHostnameVerifier(SSLSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);
         registry.register(new Scheme("https", sslSocketFactory, 443));
         
         ThreadSafeClientConnManager manager = new ThreadSafeClientConnManager(params, registry);
         DefaultHttpClient httpClient = new DefaultHttpClient(manager, params);
         
         httpClient.addRequestInterceptor(new HttpRequestInterceptor() {
             public void process(
                     final HttpRequest request,
                     final HttpContext context) throws HttpException, IOException {
                 if (!request.containsHeader("Accept-Encoding")) {
                     request.addHeader("Accept-Encoding", "gzip");
                 }
             }
         });
         
         httpClient.addResponseInterceptor(new HttpResponseInterceptor() {
             public void process(
                     final HttpResponse response,
                     final HttpContext context) throws HttpException, IOException {
                 HttpEntity entity = response.getEntity();
                 Header ceheader = entity.getContentEncoding();
                 if (ceheader != null) {
                     HeaderElement[] codecs = ceheader.getElements();
                     for (HeaderElement codec : codecs) {
                         if (codec.getName().equalsIgnoreCase("gzip")) {
                             response.setEntity(new GzipDecompressingEntity(response.getEntity()));
                             return;
                         }
                     }
                 }
             }
 
         });
         return httpClient;
     }
 
     @Override
     public InputStream loadData(HttpDataLoaderRequest requestParams) throws IOException {
         HttpUriRequest request = null;
        String url = requestParams.getUrl();
         switch (requestParams.getHttpMethod()) {
         case GET:
        	request = new HttpGet(url);
         	break;
         case POST:
        	request = new HttpPost(url);
         	break;
         }

        URI requestURI = request.getURI();
        if (requestURI.getHost() == null) {
            int schemeStart = url.indexOf("//");
            if (schemeStart >= 0) {
                int schemeEnd =  url.indexOf("/", schemeStart + 2);
                if (schemeEnd > 0) {
                    String host = url.substring(schemeStart + 2, schemeEnd);
                    try {
                        Field hostField = URI.class.getDeclaredField("host");
                        hostField.setAccessible(true);
                        hostField.set(requestURI, host);
                    } catch (NoSuchFieldException ignored) {
                    } catch (IllegalAccessException ignored) {
                    }
                }
            }
        }

         if (requestParams.getEntity() != null) {
         	ByteArrayEntity entity = new ByteArrayEntity(requestParams.getEntity());
         	((HttpEntityEnclosingRequest) request).setEntity(entity);
         	if (requestParams.getEntityMimeType() != null) {
         		request.addHeader("Content-Type", requestParams.getEntityMimeType());
         	}
         }
 
         for (Map.Entry<String, String> header : requestParams.getHeaders().entrySet()) {
             request.addHeader(header.getKey(), header.getValue());
         }
 
         final HttpResponse response;
         CookieStore requestCookieStore = cookieStore == null ? new BasicCookieStore() : cookieStore;
         for (Cookie cookie : requestParams.getCookies()) {
             requestCookieStore.addCookie(cookie);
         }
         HttpContext localContext = new BasicHttpContext();
         localContext.setAttribute(ClientContext.COOKIE_STORE, requestCookieStore);
         response = httpClient.execute(request, localContext);
 
         int statusCode = response.getStatusLine().getStatusCode();
         if (statusCode == 200) {
             return response.getEntity().getContent();
         } else {
             throw new HttpClientGatewayException("Bad status code = " + statusCode, statusCode);
         }
     }
 
 }
