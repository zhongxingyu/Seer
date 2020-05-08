 package org.eclipse.swordfish.core.test.util.domain;
 
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.methods.PostMethod;
 import org.apache.commons.httpclient.methods.RequestEntity;
 import org.apache.commons.httpclient.methods.StringRequestEntity;
 import org.apache.cxf.interceptor.LoggingInInterceptor;
 import org.apache.cxf.interceptor.LoggingOutInterceptor;
 import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
 
 public class ClientUtil {
     public static String sendRequest(String uri, String request) {
         PostMethod post = new PostMethod(uri);
         try {
         RequestEntity entity = new StringRequestEntity(request, "text/xml","UTF8");
         post.setRequestEntity(entity);
         // consult documentation for your web service
         post.setRequestHeader("SOAPAction", "");
         // Get HTTP client
         HttpClient httpclient = new HttpClient();
 
             int result = httpclient.executeMethod(post);
             System.out.println("Response status code: " + result);
             System.out.println("Response body: ");
             return post.getResponseBodyAsString();
         } catch (Exception ex) {
            throw new IllegalStateException("Could not send request to address " + uri, ex);
         } finally {
             post.releaseConnection();
         }
     }

    public static <E extends Object> E createWebServiceStub(Class<E> interfaceClass, String wsdlUri) {
         JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
         factory.getInInterceptors().add(new LoggingInInterceptor());
         factory.getOutInterceptors().add(new LoggingOutInterceptor());
         factory.setServiceClass(interfaceClass);
         factory.setWsdlLocation(wsdlUri);
         E serviceStub = (E) factory.create();
         return serviceStub;
     }
 }
