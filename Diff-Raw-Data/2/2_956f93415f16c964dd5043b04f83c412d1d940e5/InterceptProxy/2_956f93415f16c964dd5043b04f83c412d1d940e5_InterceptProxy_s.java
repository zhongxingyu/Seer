 package intercept.proxy;
 
 import intercept.configuration.ProxyConfig;
 import intercept.logging.ApplicationLog;
 import intercept.utils.Block;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Use InterceptProxy to instantiate a proxy server. InterceptProxy maintains a list of running proxies. These proxies
 * can be closed one at a time or all running prozies can be closed by calling shutdown().
  */
 public class InterceptProxy {
     private static List<ProxyServer> proxies = new ArrayList<ProxyServer>();
 
     public static ProxyServer startProxy(ProxyConfig proxyConfig, ApplicationLog applicationLog) {
         ProxyServer proxyServer = new InterceptProxyServer(proxyConfig, applicationLog);
         proxyServer.start();
         synchronized (proxies) {
             proxies.add(proxyServer);
         }
         return proxyServer;
     }
 
     public static void stopProxy(ProxyServer proxyServer) {
         synchronized (proxies) {
             for (ProxyServer proxy : proxies) {
                 if (proxy == proxyServer) {
                     proxy.stop();
                 }
             }
             proxies.remove(proxyServer);
         }
     }
 
     public static void eachProxy(Block<ProxyServer> block) {
         for (ProxyServer proxy : proxies) {
             block.yield(proxy);
         }
     }
 
     public static void shutdown() {
         synchronized (proxies) {
             for (ProxyServer proxy : proxies) {
                 proxy.stop();
             }
             proxies.clear();
         }
     }
 }
