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
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import java.io.IOException;
 import java.net.HttpURLConnection;
 import java.net.InetSocketAddress;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.Executors;
 
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
     public static final String SERVER_VERSION = "0.2.1";
     public static final String FULL_SERVER_NAME = SERVER_NAME + "/" + SERVER_VERSION;
 
     //Default config
     public static final int DEFAULT_PORT = 8000;
     public static final String DEFAULT_HOST = "localhost";
     public static final int DEFAULT_MAX_THREADS_COUNT = 1;
 
     public static final String HTTP_PREFIX = "http://";
     public static final String PORT_DELIMITER = ":";
     public static final String PATH_DELIMITER = "/";
 
     private static final Log log = LogFactory.getLog(DefaultSimpleHttpServer.class);
 
     private HttpServer httpServer;
 
     private int port = DEFAULT_PORT;
     private String host = DEFAULT_HOST;
     private int maxThreads = DEFAULT_MAX_THREADS_COUNT;
 
     private Map<String, SimpleHttpHandler> handlers = new HashMap<String, SimpleHttpHandler>();
     private Map<String, String> defaultHeaders = new HashMap<String, String>();
 
     private final Object handlersMonitor = new Object();
     private final Object headersMonitor = new Object();
 
     private HttpHandler defaultHandler = new HttpHandler() {
 
         private void logRequest(HttpExchange httpExchange) {
             if (log.isDebugEnabled()) {
                 //request URI
                 log.debug(String.format("%s. request URI: %s", SERVER_NAME, httpExchange.getRequestURI().toString()));
 
                 //request headers
                 log.debug(String.format("%s. request headers:", SERVER_NAME));
                 for (Map.Entry<String, List<String>> entry : httpExchange.getRequestHeaders().entrySet()) {
                     for (String value : entry.getValue()) {
                         log.debug(String.format("[%s: %s]", entry.getKey(), value));
                     }
                 }
 
                 //request method
                 log.debug(String.format("%s. request method: %s", SERVER_NAME, httpExchange.getRequestMethod()));
             }
         }
 
         private void internalHandleRequest(SimpleHttpHandler handler, HttpExchange httpExchange) throws IOException {
             HttpRequestContext httpRequestContext = new HttpRequestContext(httpExchange);
 
             //Call getReponse of passed handler
             byte[] response = handler.getResponse(httpRequestContext);
 
             //Add default headers
             for (Map.Entry<String, String> entry: defaultHeaders.entrySet()) {
                 httpExchange.getResponseHeaders().add(entry.getKey(), entry.getValue());
             }
 
             //Add headers from handler
             if (handler.getResponseHeaders() != null && handler.getResponseHeaders().size() > 0) {
                 for (Map.Entry<String, String> entry: handler.getResponseHeaders().entrySet()) {
                     httpExchange.getResponseHeaders().add(entry.getKey(), entry.getValue());
                 }
             }
 
            //Do not write response body for HTTP HEAD request
            int responseLength =
                    response != null && !HTTP_HEAD.equals(httpExchange.getRequestMethod()) ? response.length : 0;
 
            httpExchange.sendResponseHeaders(handler.getResponseCode(httpRequestContext), responseLength);
 
            if (responseLength > 0) {
                 httpExchange.getResponseBody().write(response);
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
             logRequest(httpExchange);
 
             String path = httpExchange.getRequestURI().getPath();
             synchronized (handlersMonitor) {
                 try {
                     SimpleHttpHandler handler = findHandler(path);
                     if (handler != null) {
                         internalHandleRequest(handler, httpExchange);
                     } else {
                         httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_NOT_FOUND, 0);
                     }
                 } finally {
                     httpExchange.close();
                 }
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
         this.port = port;
     }
 
     public int getPort() {
         return port;
     }
 
     public String getHost() {
         return host;
     }
 
     public void setHost(String host) {
         this.host = host;
     }
 
     public int getMaxThreads() {
         return maxThreads;
     }
 
     public void setMaxThreads(int maxThreads) {
         this.maxThreads = maxThreads;
     }
 
     public void addHandler(String path, SimpleHttpHandler httpHandler) {
         createHttpServer();
 
         synchronized (handlersMonitor) {
             handlers.put(path, httpHandler);
         }
 
         httpServer.createContext(path, defaultHandler);
     }
 
     public void setDefaultResponseHeaders(Map<String, String> defaultHeaders) {
         synchronized (headersMonitor) {
             this.defaultHeaders.putAll(defaultHeaders);
         }
     }
 
     public void addResponseHeader(String name, String value) {
         synchronized (headersMonitor) {
             this.defaultHeaders.put(name, value);
         }
     }
 }
