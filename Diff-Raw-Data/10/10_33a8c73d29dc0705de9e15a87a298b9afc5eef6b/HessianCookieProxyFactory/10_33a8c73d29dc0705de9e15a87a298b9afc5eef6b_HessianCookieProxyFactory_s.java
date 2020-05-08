 package com.caucho.hessian.client;
 
 import java.lang.reflect.InvocationHandler;
 import java.lang.reflect.Proxy;
import java.net.MalformedURLException;
 import java.net.URL;
 
 import com.caucho.hessian.io.HessianRemoteObject;
 
 public class HessianCookieProxyFactory extends HessianProxyFactory {
     /**
      * Creates a new proxy with the specified URL.  The returned object
      * is a proxy with the interface specified by api.
      *
      * <pre>
      * String url = "http://localhost:8080/ejb/hello");
      * HelloHome hello = (HelloHome) factory.create(HelloHome.class, url);
      * </pre>
      *
      * @param api the interface the proxy class needs to implement
      * @param url the URL where the client object is located.
      *
      * @return a proxy to the object with the specified interface.
      */
     public Object create(Class<?> api, URL url, ClassLoader loader)
     {
       if (api == null)
         throw new NullPointerException("api must not be null for HessianProxyFactory.create()");
       InvocationHandler handler = null;
 
       handler = new HessianCookieProxy(url, this, api);
 
       return Proxy.newProxyInstance(loader,
                                     new Class[] { api,
                                                   HessianRemoteObject.class },
                                     handler);
     }
 
 
 }
