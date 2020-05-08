 /*
  * Copyright (c) 2011 Sergey Prilukin
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  */
 
 package anhttpserver;
 
 import com.sun.net.httpserver.HttpExchange;
 import com.sun.net.httpserver.HttpHandler;
 import com.sun.net.httpserver.HttpServer;
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.HttpURLConnection;
 import java.net.InetSocketAddress;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
 
 /**
  * Default implementation of {@link SimpleHttpServer}
  * Which has predefined host and port.
  *
  * {@link com.sun.net.httpserver.HttpServer} is used
  * at the backend.
  *
  * @author Sergey Prilukin
  */
 public final class DefaultSimpleHttpServer implements SimpleHttpServer {
 
     //HTTP request method HEAD
     public static final String HTTP_HEAD = "HEAD";
 
     //Server info
     public static final String SERVER_HEADER_NAME = "Server";
     public static final String SERVER_NAME = "anhttpserver";
     public static final String SERVER_VERSION = "0.2.3";
     public static final String FULL_SERVER_NAME = SERVER_NAME + "/" + SERVER_VERSION;
 
     //Default config
     public static final int DEFAULT_PORT = 8000;
     public static final String DEFAULT_HOST = "localhost";
     public static final int DEFAULT_MAX_THREADS_COUNT = 1;
 
     public static final String HTTP_PREFIX = "http://";
     public static final String PORT_DELIMITER = ":";
     public static final String PATH_DELIMITER = "/";
 
    public static final SimpleDateFormat REQUEST_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    public static final String REMOTE_HOST_REGEXP = "^/([^\\:]+)\\:[\\d]+$";
 
     private static final Log log = LogFactory.getLog(DefaultSimpleHttpServer.class);
 
     private HttpServer httpServer;
 
     private int port = DEFAULT_PORT;
     private String host = DEFAULT_HOST;
     private int maxThreads = DEFAULT_MAX_THREADS_COUNT;
 
     private Map<String, SimpleHttpHandler> handlers = new Hashtable<String, SimpleHttpHandler>();
     private Map<String, String> defaultHeaders = new Hashtable<String, String>();
 
     private HttpHandler defaultHandler = new HttpHandler() {
 
         private void logRequest(HttpExchange httpExchange, int responseCode, long responseLength) {
             if (log.isDebugEnabled()) {
                 //--Log request
                 log.debug(httpExchange.getRequestMethod() + " " + httpExchange.getRequestURI().toString() + " " + httpExchange.getProtocol());
 
                 //request headers
                 for (Map.Entry<String, List<String>> entry : httpExchange.getRequestHeaders().entrySet()) {
                     for (String value : entry.getValue()) {
                         log.debug(String.format("%s: %s", entry.getKey(), value));
                     }
                 }
 
                 log.debug("\r\n");
 
                 //--LOG reponse
                 log.debug(String.format("Response Code: %s", responseCode));
                 for (Map.Entry<String, List<String>> entry : httpExchange.getResponseHeaders().entrySet()) {
                     for (String value : entry.getValue()) {
                         log.debug(String.format("%s: %s", entry.getKey(), value));
                     }
                 }
 
                 log.debug("\r\n");
             } else if (log.isInfoEnabled()) {
                 //Short format

                String remoteHost = httpExchange.getRemoteAddress().toString();
                String remoteAddress = remoteHost;
                Matcher matcher = Pattern.compile(REMOTE_HOST_REGEXP).matcher(remoteHost);
                if (matcher.find()) {
                    remoteAddress = matcher.group(1);
                }

                 String date = REQUEST_DATE_FORMAT.format(new Date());
                 String method = httpExchange.getRequestMethod();
                 String path = httpExchange.getRequestURI().getPath();
                 String userAgent = httpExchange.getRequestHeaders().get("User-Agent").get(0);
 
                 log.info(String.format("%s [%s] %s %s %s %s %s", remoteAddress, date, method, path, responseCode, responseLength, userAgent));
             }
         }
 
         private void internalHandleRequest(SimpleHttpHandler handler, HttpExchange httpExchange) throws IOException {
             HttpRequestContext httpRequestContext = new HttpRequestContext(httpExchange);
             handler.cleanContext(httpRequestContext);
             InputStream response = null;
 
             try {
                 //Call getReponse of passed handler
                 response = handler.getResponseAsStream(httpRequestContext);
 
                 //Add default headers
                 for (Map.Entry<String, String> entry: defaultHeaders.entrySet()) {
                     httpExchange.getResponseHeaders().add(entry.getKey(), entry.getValue());
                 }
 
                 //Add headers from handler
                 Map<String, String> responseHeaders = handler.getResponseHeaders(httpRequestContext);
                 if (responseHeaders != null && responseHeaders.size() > 0) {
                     for (Map.Entry<String, String> entry: responseHeaders.entrySet()) {
                         httpExchange.getResponseHeaders().add(entry.getKey(), entry.getValue());
                     }
                 }
 
                 //Do not write response body for HTTP HEAD request
                 long responseLength = response != null && !HTTP_HEAD.equals(httpExchange.getRequestMethod())
                         ? handler.getResponseSize(httpRequestContext) : 0L;
 
                 int responseCode = handler.getResponseCode(httpRequestContext);
                 httpExchange.sendResponseHeaders(responseCode, responseLength);
 
                 logRequest(httpExchange, responseCode, responseLength);
                 if (responseLength != 0) {
                     IOUtils.copyLarge(response, httpExchange.getResponseBody());
                 }
             } finally {
                 if (response != null) {
                     response.close();
                 }
             }
         }
 
         private SimpleHttpHandler findHandler(String path) {
             StringBuilder sb = new StringBuilder(path);
             while (sb.lastIndexOf(PATH_DELIMITER) > -1) {
                 if (handlers.containsKey(sb.toString())) {
                     return handlers.get(sb.toString());
                 }
 
                 sb.delete(sb.lastIndexOf(PATH_DELIMITER), sb.length());
                 if (sb.length() == 0) {
                     sb.append(PATH_DELIMITER);
                 }
             }
 
             return null;
         }
 
         public void handle(HttpExchange httpExchange) throws IOException {
             String path = httpExchange.getRequestURI().getPath();
 
             try {
                 SimpleHttpHandler handler = findHandler(path);
                 if (handler != null) {
                     internalHandleRequest(handler, httpExchange);
                 } else {
                     httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_NOT_FOUND, 0);
                 }
             } catch (Exception e) {
                 if (log.isDebugEnabled()) {
                     log.debug(e.getMessage(), e);
                 }
             } finally {
                 httpExchange.close();
             }
         }
     };
 
     public DefaultSimpleHttpServer() {
         defaultHeaders.put(SERVER_HEADER_NAME, FULL_SERVER_NAME);
     }
 
     private void createHttpServer() {
         if (httpServer == null) {
             synchronized (this) {
                 if (httpServer == null) {
                     try {
                         httpServer = HttpServer.create();
                         httpServer.setExecutor(Executors.newFixedThreadPool(maxThreads));
                         httpServer.bind(new InetSocketAddress(host, port), 0);
                     } catch (IOException e) {
                         throw new RuntimeException(e);
                     }
                 }
             }
         }
     }
 
     public String getBaseUrl() {
         return (new StringBuilder()).append(HTTP_PREFIX).append(host).append(PORT_DELIMITER).append(port).toString();
     }
 
     public void start() {
         createHttpServer();
         httpServer.start();
     }
 
     public void stop() {
         if (httpServer != null) {
             httpServer.stop(0);
         }
     }
 
     public void setPort(int port) {
         if (port <= 0) {
             throw new IllegalArgumentException("Port should be a positive number");
         }
 
         this.port = port;
     }
 
     public int getPort() {
         return port;
     }
 
     public String getHost() {
         return host;
     }
 
     public void setHost(String host) {
         if (host == null || host.isEmpty()) {
             throw new IllegalArgumentException("Host should be a non-empty string");
         }
 
         this.host = host;
     }
 
     public int getMaxThreads() {
         return maxThreads;
     }
 
     public void setMaxThreads(int maxThreads) {
         if (maxThreads <= 0) {
             throw new IllegalArgumentException("maxThreads should be a positive number");
         }
 
         this.maxThreads = maxThreads;
     }
 
     public void addHandler(String path, SimpleHttpHandler httpHandler) {
         createHttpServer();
         handlers.put(path, httpHandler);
         httpServer.createContext(path, defaultHandler);
     }
 
     public void setDefaultResponseHeaders(Map<String, String> defaultHeaders) {
         this.defaultHeaders.putAll(defaultHeaders);
     }
 
     public void addResponseHeader(String name, String value) {
         this.defaultHeaders.put(name, value);
     }
 }
