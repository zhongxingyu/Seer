 package com.nyteshade;
 
 import com.sun.net.httpserver.*;
 
 import java.io.*;
 import java.net.InetSocketAddress;
 import java.net.URI;
 import java.net.URLDecoder;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.Executor;
 
 /**
  * EmbedWeb is a convenience class that wraps the HttpServer Sun, now Oracle,
  * bundles with JDK6+. This provides an incredibly easy way to implement a
  * HTTP listener in any Java based application, be it a desktop app or an app
  * server like Tomcat.
  *
  * Combined with a JSON encoder/decoder it can be a powerful solution to
  * providing an API for your application. Creating an HttpServer on port 7070
  * that listens to the root (/) path is as simple as:
  *
  * <pre><code>
  *   EmbedWeb.WEB.addHandler(7070, "/", new HttpHandler() {
  *     public void handle(HttpExchange httpExchange) throws IOException {
  *       Map&lt;String, Object> params = EmbedWeb.getParameters(httpExchange);
  *       StringBuilder buffer = new StringBuilder();
  *
  *       if (params.containsKey("name")) {
  *         buffer.append("Hello " + params.get("name") + "");
  *       } else {
  *         buffer.append("Whats up punk?!");
  *       }
  *
  *       EmbedWeb.respondWith(httpExchange, 200, buffer, "text/html");
  *     }
  *   });
  *
  *   EmbedWeb.WEB.startServer(7070);
  * </code></pre>
  *
  * @author Gabriel Harrison <nyteshade@gmail.com>
  * @version 12-FEB-2013
  */
 public enum EmbedWeb {
   WEB;
 
   protected HashMap<Integer, HttpServer> servers;
   protected HashMap<Integer, HttpsServer> secure_servers;
   protected HashMap<HttpServer, HashMap<String, HttpContext>> contexts;
 
   EmbedWeb() {
     servers = new HashMap<Integer, HttpServer>();
     secure_servers = new HashMap<Integer, HttpsServer>();
     contexts = new HashMap<HttpServer, HashMap<String, HttpContext>>();
   }
 
   /**
    * Called on {@link #WEB}; if no server exists at the port supplied, one
    * will be created. Only in the case of an IOException will a null server
    * be returned.
    *
    * @param port an integer detailing which port on this server the web listener
    *             should act on.
    * @return an instance of {@link HttpServer} or null if an {@link IOException}
    *    is thrown.
    */
   protected HttpServer serverOn(int port, boolean https) {
     HttpServer server;
 
     if (https && !secure_servers.containsKey(port)
        || !https && !servers.containsKey(port)) {
       try {
         server = https
             ? HttpsServer.create(new InetSocketAddress(port), 0)
             : HttpServer.create(new InetSocketAddress(port), 0);
 
         if (https) {
           secure_servers.put(port, (HttpsServer)server);
         }
         else {
           servers.put(port, server);
         }
 
       }
       catch (IOException e) {
         server = null;
         e.printStackTrace();
       }
     }
     else {
       server = https ? secure_servers.get(port) : servers.get(port);
     }
 
     return server;
   }
 
   /**
    * Used to add a new endpoint on a server at the specified port. If a server
    * doesn't exist at that port, one will be created. The handler and contexts
    * created will automatically have the {@link ParameterFilter} applied.
    *
    * @param port the numerical port on which the server to have a new endpoint
    *             added should run on
    * @param path a path such as "/home" or "/login" or whatever else you wish to
    *             use to serve data.
    * @param handler an instance of {@link HttpHandler} which is used to handle
    *                what happens when the endpoint is accessed.
    * @return an instance of {@link HttpContext} which has already had the
    *    {@link ParameterFilter} and the supplied handler applied to it.
    */
   public HttpContext addHandler(int port, String path, HttpHandler handler) {
     return addHandler(port, path, handler, false);
   }
 
   /**
    * Used to add a new endpoint on a server at the specified port. If a server
    * doesn't exist at that port, one will be created. The handler and contexts
    * created will automatically have the {@link ParameterFilter} applied.
    *
    * @param port the numerical port on which the server to have a new endpoint
    *             added should run on
    * @param path a path such as "/home" or "/login" or whatever else you wish to
    *             use to serve data.
    * @param handler an instance of {@link HttpHandler} which is used to handle
    *                what happens when the endpoint is accessed.
    * @param https true if the server is secure; false otherwise
    * @return an instance of {@link HttpContext} which has already had the
    *    {@link ParameterFilter} and the supplied handler applied to it.
    */
   public HttpContext addHandler(int port, String path, HttpHandler handler,
                                   boolean https) {
     HttpServer server = WEB.serverOn(port, https);
     if (server == null) {
       return null;
     }
 
     HttpContext context = server.createContext(path, handler);
     context.getFilters().add(new ParameterFilter());
     if (contexts.get(server) == null) {
       contexts.put(server, new HashMap<String, HttpContext>());
     }
     contexts.get(server).put(path, context);
 
     return context;
   }
 
   /**
    * Get the context running on a server on port {@code port} with the supplied
    * path or null if there is none or there is no server running on that port.
    *
    * @param port the numerical port on which the server to have a new endpoint
    *             added should run on
    * @param path a path such as "/home" or "/login" or whatever else you wish to
    *             use to serve data.
    * @return the instance of {@link HttpContext} for that path on that port
    */
   public HttpContext getContext(int port, String path) {
     return getContext(port, path, false);
   }
 
   /**
    * Get the context running on a server on port {@code port} with the supplied
    * path or null if there is none or there is no server running on that port.
    *
    * @param port the numerical port on which the server to have a new endpoint
    *             added should run on
    * @param path a path such as "/home" or "/login" or whatever else you wish to
    *             use to serve data.
    * @param https true if the server is secure; false otherwise
    * @return the instance of {@link HttpContext} for that path on that port
    */
   public HttpContext getContext(int port, String path, boolean https) {
     HttpServer server = WEB.serverOn(port, https);
     if (server == null || contexts.get(server) == null) {
       return null;
     }
 
     return contexts.get(server).get(path);
   }
 
   /**
    * Overrides the context at a given port and path. If there is no server
    * running at the supplied port then false is returned. Otherwise the given
    * context is set on the server at the supplied port and true is returned.
    *
    * @param port the numerical port on which the server to have a new endpoint
    *             added should run on
    * @param path a path such as "/home" or "/login" or whatever else you wish to
    *             use to serve data.
    * @param context an instance of {@link HttpContext} to set on the server
    * @return true if setting was successful; false otherwise
    */
   public boolean setContext(int port, String path, HttpContext context) {
     return setContext(port, path, context, false);
   }
 
   /**
    * Overrides the context at a given port and path. If there is no server
    * running at the supplied port then false is returned. Otherwise the given
    * context is set on the server at the supplied port and true is returned.
    *
    * @param port the numerical port on which the server to have a new endpoint
    *             added should run on
    * @param path a path such as "/home" or "/login" or whatever else you wish to
    *             use to serve data.
    * @param context an instance of {@link HttpContext} to set on the server
    * @param https true if the server is secure; false otherwise
    * @return true if setting was successful; false otherwise
    */
   public boolean setContext(int port, String path, HttpContext context,
                             boolean https) {
     HttpServer server = WEB.serverOn(port, https);
     if (server == null) {
       return false;
     }
 
     contexts.get(server).put(path, context);
     return true;
   }
 
   /**
    * Obtain the {@link HttpHandler} that gets applied whenever the supplied
    * path is connected to on a server at the supplied port.
    *
    * @param port the numerical port on which the server to have a new endpoint
    *             added should run on
    * @param path a path such as "/home" or "/login" or whatever else you wish to
    *             use to serve data.
    * @return an instance of {@link HttpHandler}
    */
   public HttpHandler getHandler(int port, String path) {
     HttpContext context = getContext(port, path);
     return context != null ? context.getHandler() : null;
   }
 
   /**
    * Replaces the {@link HttpHandler} that gets applied whenever the supplied
    * path is connected to on a server at the supplied port.
    *
    * @param port the numerical port on which the server to have a new endpoint
    *             added should run on
    * @param path a path such as "/home" or "/login" or whatever else you wish to
    *             use to serve data.
    * @param handler an instance of {@link HttpHandler} to set
    * @return true if the value was set; false otherwise
    */
   public boolean setHandler(int port, String path, HttpHandler handler) {
     HttpContext context = getContext(port, path);
     if (context == null) {
       context = addHandler(port, path, handler);
       return context != null;
     }
 
     context.setHandler(handler);
     return true;
   }
 
   /**
    * Another helper method, respondWith takes all the annoying content length
    * calculation, response header sending and stream closing as long as the
    * user can provide the desired responseCode and a {@link StringBuilder} with
    * all the content to write out to the response.
    *
    * @param exchange an instance of {@link HttpExchange} that pertains to the
    *                 current handler.
    * @param responseCode a WEB response code (i.e. 200, 304, 500, etc...)
    * @param buffer a {@link StringBuilder} with the output content to respond
    *               with.
    * @throws IOException a {@link IOException} may possibly be thrown by the
    *    execution of this method.
    */
   public static void respondWith(HttpExchange exchange, int responseCode,
       StringBuilder buffer) throws IOException {
     OutputStream out = exchange.getResponseBody();
     String data = buffer.toString();
 
     exchange.sendResponseHeaders(responseCode, data.length());
     out.write(data.getBytes());
     out.close();
   }
 
   /**
    * Identical to {@link #respondWith(com.sun.net.httpserver.HttpExchange,
    * int, StringBuilder)} with the exception that you may also specify the
    * desired mime type of the response. Examples are "text/html", "text/plain",
    * "text/javascript" or the more desired "application/javascript", etc...
    *
    * @param exchange an instance of {@link HttpExchange} that pertains to the
    *                 current handler.
    * @param responseCode a WEB response code (i.e. 200, 304, 500, etc...)
    * @param buffer a {@link StringBuilder} with the output content to respond
    *               with.
    * @param mimeType a WEB mime-type see http://en.wikipedia.org/wiki/MIME_type
    *                 for more information.
    * @throws IOException a {@link IOException} may possibly be thrown by the
    *    execution of this method.
    */
   public static void respondWith(HttpExchange exchange, int responseCode,
       StringBuilder buffer, String mimeType) throws IOException {
     exchange.getResponseHeaders().set("Content-Type", mimeType);
     respondWith(exchange, responseCode, buffer);
   }
 
   /**
    * Starts the server on the specified port
    *
    * @param port the port denoting which server to start. Nothing happens if
    *             there is no server at the port specified.
    */
   public void startServer(int port) {
     startServer(port, false);
   }
 
   /**
    * Starts the server on the specified port
    *
    * @param port the port denoting which server to start. Nothing happens if
    *             there is no server at the port specified.
    * @param https true if the server is secure; false otherwise
    */
   public void startServer(int port, boolean https) {
     HttpServer server = serverOn(port, https);
     if (server != null) {server.start();}
   }
 
   /**
    * Stops the server on the specified port
    *
    * @param port the port denoting which server to stop. Nothing happens if
    *             there is no server at the port specified.
    */
   public void stopServer(int port, int delay) {
     stopServer(port, delay, false);
   }
 
   /**
    * Stops the server on the specified port
    *
    * @param port the port denoting which server to stop. Nothing happens if
    *             there is no server at the port specified.
    * @param https true if the server is secure; false otherwise
    */
   public void stopServer(int port, int delay, boolean https) {
     HttpServer server = serverOn(port, https);
     if (server != null) {server.stop(delay);}
   }
 
   /**
    * Obtains the {@link InetSocketAddress} of the server executing on the port
    * specified. Null is returned if there is no server at the port in question.
    *
    * @param port the port denoting which server to query.
    * @return an instance of {@link InetSocketAddress} or null.
    */
   public InetSocketAddress getAddress(int port) {
     return getAddress(port, false);
   }
 
   /**
    * Obtains the {@link InetSocketAddress} of the server executing on the port
    * specified. Null is returned if there is no server at the port in question.
    *
    * @param port the port denoting which server to query.
    * @param https true if the server is secure; false otherwise
    * @return an instance of {@link InetSocketAddress} or null.
    */
   public InetSocketAddress getAddress(int port, boolean https) {
     HttpServer server = serverOn(port, https);
     return server != null ? server.getAddress() : null;
   }
 
   /**
    * Obtains the {@link Executor} of the server executing on the port specified.
    * Null is returned if there is no server at the port in question.
    *
    * @param port the port denoting which server to query.
    * @return an instance of {@link Executor} or null
    */
   public Executor getExecutor(int port) {
     return getExecutor(port, false);
   }
 
   /**
    * Obtains the {@link Executor} of the server executing on the port specified.
    * Null is returned if there is no server at the port in question.
    *
    * @param port the port denoting which server to query.
    * @param https true if the server is secure; false otherwise
    * @return an instance of {@link Executor} or null
    */
   public Executor getExecutor(int port, boolean https) {
     HttpServer server = serverOn(port, https);
     return server != null ? server.getExecutor() : null;
   }
 
   /**
    * Sets the {@link Executor} of the server executing on the port specified.
    *
    * @param port the port denoting which server to query.
    * @param executor an instance of {@link Executor} to set
    */
   public void setExecutor(int port, Executor executor) {
     setExecutor(port, executor, false);
   }
 
   /**
    * Sets the {@link Executor} of the server executing on the port specified.
    *
    * @param port the port denoting which server to query.
    * @param executor an instance of {@link Executor} to set
    * @param https true if the server is secure; false otherwise
    */
   public void setExecutor(int port, Executor executor, boolean https) {
     HttpServer server = serverOn(port, https);
     if (server != null) {
       server.setExecutor(executor);
     }
   }
 
   public void setHttpsConfigurator(int port, HttpsConfigurator configurator) {
     HttpsServer server = (HttpsServer)serverOn(port, true);
     if (server != null) {
       server.setHttpsConfigurator(configurator);
     }
     else {
       System.err.println(
           "setHttpsConfigurator() only works on HTTPS servers and there isn't "
         + "one registered on port " + port);
     }
   }
 
   /**
    * Rather than the long type cast and needing to know the name of the
    * attribute that the parameters for the request are stored under, this
    * static method will perform that work for you if you only supply the
    * {@link HttpExchange} object in question.
    *
    * @param exchange an instance of {@link HttpExchange} that is relevant to
    *                 the request in question
    * @return a {@link Map} of {@link String}, {@link Object} pairs denoting
    *    the parameters for the request.
    */
   public static Map<String,Object> getParameters(HttpExchange exchange) {
     Map<String, Object> params =
            (Map<String, Object>)exchange.getAttribute(ParameterFilter.PARAMS);
     return params;
   }
 
   /**
    * A nice filter which takes the dirty work out of obtaining parameters in
    * a given {@link HttpHandler}. This bit of code was written by Leonardo
    * Marcelino. The site from which it was referenced is leonardom.wordpress.com
    * with the path /2009/08/06/getting-parameters-from-httpexchange.
    *
    * @author Leonardo Marcelino <leonardo.marcelino@gmail.com>
    * @version 06-AUG-2009
    */
   public static class ParameterFilter extends Filter {
 
     public static final String PARAMS = "parameters";
 
     @Override
     public String description() {
       return "Parses the requested URI for parameters";
     }
 
     @Override
     public void doFilter(HttpExchange exchange, Chain chain)
       throws IOException {
       parseGetParameters(exchange);
       parsePostParameters(exchange);
       chain.doFilter(exchange);
     }
 
     private void parseGetParameters(HttpExchange exchange)
       throws UnsupportedEncodingException {
 
       Map<String, Object> parameters = new HashMap<String, Object>();
       URI requestedUri = exchange.getRequestURI();
       String query = requestedUri.getRawQuery();
       parseQuery(query, parameters);
       exchange.setAttribute(PARAMS, parameters);
     }
 
     private void parsePostParameters(HttpExchange exchange)
       throws IOException {
 
       if ("post".equalsIgnoreCase(exchange.getRequestMethod())) {
         @SuppressWarnings("unchecked")
         Map<String, Object> parameters =
           (Map<String, Object>)exchange.getAttribute(PARAMS);
         InputStreamReader isr =
           new InputStreamReader(exchange.getRequestBody(),"utf-8");
         BufferedReader br = new BufferedReader(isr);
         String query = br.readLine();
         parseQuery(query, parameters);
       }
     }
 
      @SuppressWarnings("unchecked")
      private void parseQuery(String query, Map<String, Object> parameters)
        throws UnsupportedEncodingException {
 
        if (query != null) {
          String pairs[] = query.split("[&]");
 
          for (String pair : pairs) {
            String param[] = pair.split("[=]");
 
            String key = null;
            String value = null;
            if (param.length > 0) {
              key = URLDecoder.decode(param[0],
                System.getProperty("file.encoding"));
            }
 
            if (param.length > 1) {
              value = URLDecoder.decode(param[1],
                System.getProperty("file.encoding"));
            }
 
            if (parameters.containsKey(key)) {
              Object obj = parameters.get(key);
              if(obj instanceof List<?>) {
                List<String> values = (List<String>)obj;
                values.add(value);
              } else if(obj instanceof String) {
                List<String> values = new ArrayList<String>();
                values.add((String)obj);
                values.add(value);
                parameters.put(key, values);
              }
            } else {
              parameters.put(key, value);
            }
          }
        }
     }
   }
 }
 
