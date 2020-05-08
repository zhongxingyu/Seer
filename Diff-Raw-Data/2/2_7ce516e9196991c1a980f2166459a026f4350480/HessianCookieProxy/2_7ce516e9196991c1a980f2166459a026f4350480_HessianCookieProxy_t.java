 package com.caucho.hessian.client;
 
 import java.net.URL;
 
 import org.eclipse.jetty.http.HttpHeaders;
 
 public class HessianCookieProxy extends HessianProxy {
 
    /** */
    private static final long serialVersionUID = 3744566286788170542L;
     private String _cookie;
 
     protected HessianCookieProxy(URL url, HessianProxyFactory factory, Class<?> type) {
         super(url, factory, type);
     }
 
     protected HessianCookieProxy(URL url, HessianProxyFactory factory) {
         super(url, factory);
     }
 
     /**
      * Method that allows subclasses to add request headers such as cookies.
      * Default implementation is empty.
      */
     @Override
     protected void addRequestHeaders(HessianConnection conn) {
         conn.addHeader("Content-Type", "x-application/hessian");
 
         if (_cookie != null) {
             conn.addHeader(HttpHeaders.COOKIE, _cookie + ";");
         }
         String basicAuth = _factory.getBasicAuth();
 
         if (basicAuth != null)
             conn.addHeader("Authorization", basicAuth);
     }
 
     /**
      * Sets the cookie that will be set for all connections with this proxy.
      * 
      * @param cookie The cookie to use, set to null to remove previously set cookie.
      */
     public void setCookie(String cookie) {
         _cookie = cookie;
     }
 
 }
