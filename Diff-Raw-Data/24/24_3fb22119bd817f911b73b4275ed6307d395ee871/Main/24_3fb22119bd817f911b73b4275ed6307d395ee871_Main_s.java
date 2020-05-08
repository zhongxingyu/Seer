 package org.expressme.openid;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.lang.reflect.InvocationHandler;
 import java.lang.reflect.Method;
 import java.lang.reflect.Proxy;
 import java.net.URLDecoder;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 
 /**
  * Sample application for OpenID.
  * 
  * @author Michael Liao (askxuefeng@gmail.com)
  */
 public class Main {
 
     public static void main(String[] args) throws Exception {
         // set proxy if needed:
 //        java.util.Properties props = System.getProperties();
 //        props.put("proxySet", "true");
 //        props.put("proxyHost", "host");
 //        props.put("proxyPort", "port");
 
         OpenIdManager manager = new OpenIdManager();
        manager.setReturnTo("http://www.openid-example.com/openId");
        manager.setRealm("http://*.openid-example.com");
         manager.setTimeOut(10000);
        Endpoint endpoint = manager.lookupEndpoint("Yahoo");
         System.out.println(endpoint);
         Association association = manager.lookupAssociation(endpoint);
         System.out.println(association);
         String url = manager.getAuthenticationUrl(endpoint, association);
         System.out.println("Copy the authentication URL in browser:\n" + url);
         System.out.println("After successfully sign on in browser, enter the URL of address bar in browser:");
         String ret = readLine();
         HttpServletRequest request = createRequest(ret);
         Authentication authentication = manager.getAuthentication(request, association.getRawMacKey(), endpoint.getAlias());
         System.out.println(authentication);
     }
 
     static String readLine() throws IOException {
         BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
         return r.readLine();
     }
 
     static HttpServletRequest createRequest(String url) throws UnsupportedEncodingException {
         int pos = url.indexOf('?');
         if (pos==(-1))
             throw new IllegalArgumentException("Bad url.");
         String query = url.substring(pos + 1);
         String[] params = query.split("[\\&]+");
         final Map<String, String> map = new HashMap<String, String>();
         for (String param : params) {
             pos = param.indexOf('=');
             if (pos==(-1))
                 throw new IllegalArgumentException("Bad url.");
             String key = param.substring(0, pos);
             String value = param.substring(pos + 1);
             map.put(key, URLDecoder.decode(value, "UTF-8"));
         }
         return (HttpServletRequest) Proxy.newProxyInstance(
                 Main.class.getClassLoader(),
                 new Class[] { HttpServletRequest.class },
                 new InvocationHandler() {
                     public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                         if (method.getName().equals("getParameter"))
                             return map.get((String)args[0]);
                         throw new UnsupportedOperationException(method.getName());
                     }
                 }
         );
     }
 }
