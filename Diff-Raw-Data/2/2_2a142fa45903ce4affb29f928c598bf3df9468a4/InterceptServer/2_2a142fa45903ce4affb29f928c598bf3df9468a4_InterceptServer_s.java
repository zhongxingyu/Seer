 package intercept.server;
 
 import com.sun.net.httpserver.HttpExchange;
 import com.sun.net.httpserver.HttpHandler;
 import com.sun.net.httpserver.HttpServer;
 import intercept.configuration.DefaultProxyConfig;
 import intercept.configuration.InterceptConfiguration;
 import intercept.configuration.ProxyConfig;
 import intercept.framework.Command;
 import intercept.framework.WebServer;
 import intercept.logging.ApplicationLog;
 import intercept.proxy.InterceptProxy;
 import intercept.proxy.ProxyServer;
 import intercept.server.components.ClasspathContentPresenter;
 import intercept.server.components.HomePagePresenter;
 import intercept.server.components.NewProxyCommand;
 import intercept.server.components.NewProxyPresenter;
 import intercept.utils.Block;
 import intercept.utils.Utils;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.net.InetSocketAddress;
 import java.net.Socket;
 import java.util.ArrayList;
 import java.util.List;
 
 import static intercept.server.UriMatchers.simpleMatcher;
 
 
 public class InterceptServer implements HttpHandler, WebServer {
     private HttpServer server;
     private InterceptConfiguration configuration;
     private final ApplicationLog applicationLog;
 
     public InterceptServer(ApplicationLog applicationLog) {
         this.applicationLog = applicationLog;
     }
 
     Block<ProxyConfig> startProxies = new Block<ProxyConfig>() {
         public void yield(ProxyConfig item) {
            applicationLog.trace("Starting proxy server " + item.getName());
 
             ProxyServer proxy = InterceptProxy.startProxy(item, applicationLog);
 
             applicationLog.trace("Created web context for /" + proxy.getName());
             server.createContext("/" + proxy.getName(), new ProxyConfigurationHttpHandler(proxy, applicationLog));
         }
     };
 
     public void start(InterceptConfiguration configuration) {
         try {
             this.configuration = configuration;
             applicationLog.log("Starting configuration server on port " + configuration.getConfigurationPort());
             server = HttpServer.create(new InetSocketAddress(configuration.getConfigurationPort()), 0);
             server.createContext("/", this);
             server.setExecutor(null);
             server.start();
 
             startProxyServers(configuration);
 
             waitUntilServerAcceptingConnections();
 
         } catch (IOException e) {
             throw new RuntimeException("Failed to start intercept server ", e);
         }
     }
 
     private void waitUntilServerAcceptingConnections() {
         while (true) {
             Socket socket = null;
             try {
                 socket = new Socket("localhost", configuration.getConfigurationPort());
             } catch (IOException e) {
             }
             if (socket != null && socket.isConnected()) {
                 Utils.sleep(200);
                 return;
             }
             Utils.close(socket);
         }
     }
 
 
     private void startProxyServers(InterceptConfiguration configuration) {
         configuration.eachProxy(startProxies);
     }
 
     private void stopProxyServers() {
         InterceptProxy.shutdown();
         ;
     }
 
     public void handle(HttpExchange httpExchange) {
         try {
             String method = httpExchange.getRequestMethod();
 
             Dispatcher dispatcher = new Dispatcher();
             dispatcher.register(simpleMatcher("/"), new HomePagePresenter(configuration));
             dispatcher.register(simpleMatcher("/proxy/new"), new NewProxyPresenter(), new NewProxyCommand());
             dispatcher.register(UriMatchers.classpathMatcher(), new ClasspathContentPresenter());
             dispatcher.register(simpleMatcher("/stop"), new Command() {
                 public void executeCommand(WebContext context) {
                     stopProxyServers();
                     server.stop(0);
                 }
             });
 
             if (method.equalsIgnoreCase("GET")) {
                 dispatcher.dispatchGetRequest(new WebContext(this, httpExchange));
             }
 
             if (method.equalsIgnoreCase("POST")) {
                 dispatcher.dispatchPostRequest(new WebContext(this, httpExchange));
             }
         } catch (NoRouteException nre) {
             send404(httpExchange);
         }
         catch (Exception e) {
             send404(httpExchange);
             System.err.println("Error processing request");
             e.printStackTrace();
         }
     }
 
     private void send404(HttpExchange httpExchange) {
         try {
             httpExchange.sendResponseHeaders(404, -1);
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
     public ProxyConfig getConfig() {
         return null;
     }
 
     public List<ProxyConfig> getRunningProxies() {
         final List<ProxyConfig> proxies = new ArrayList<ProxyConfig>();
         configuration.eachProxy(new Block<ProxyConfig>() {
             public void yield(ProxyConfig item) {
                 proxies.add(item);
             }
         });
         return proxies;
     }
 
     public void startNewProxy(String name, int port) {
         ProxyConfig proxyConfig = new DefaultProxyConfig();
         proxyConfig.setName(name);
         proxyConfig.setPort(port);
         configuration.add(proxyConfig);
         ProxyServer proxy = InterceptProxy.startProxy(proxyConfig, applicationLog);
 
         applicationLog.trace("Created web context for /" + proxyConfig.getName());
         server.createContext("/" + proxyConfig.getName(), new ProxyConfigurationHttpHandler(proxy, this.applicationLog));
 
     }
 
     public void stop(InterceptConfiguration configuration) {
 
         try {
             Socket socket = new Socket("localhost", configuration.getConfigurationPort());
             OutputStream outputStream = socket.getOutputStream();
             String message = "POST /stop HTTP1.1\r\n\r\n";
             outputStream.write(message.getBytes());
             outputStream.close();
         } catch (IOException e) {
             System.err.println("Failed to stop intercept server: " + e.getMessage());
         }
 
         server.stop(200);
         applicationLog.log("Configuration server stopped");
     }
 
     public String uri(String path) {
         return "http://localhost:" + configuration.getConfigurationPort() + path;
     }
 }
