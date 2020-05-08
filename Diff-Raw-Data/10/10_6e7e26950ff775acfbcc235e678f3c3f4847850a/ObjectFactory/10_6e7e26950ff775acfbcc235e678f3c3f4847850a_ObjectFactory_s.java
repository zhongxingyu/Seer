 package org.jboss.as.quickstarts.helloworld;
 
 import javax.enterprise.inject.Produces;
 import javax.enterprise.context.RequestScoped;
import javax.servlet.ServletRequest;
 import javax.servlet.http.HttpServletRequest;
 
 /**
  * @author <a href="mailto:mstrukel@redhat.com>Marko Strukelj</a>
  */
 public class ObjectFactory {
    private static final ThreadLocal<HttpServletRequest> requestTL = new ThreadLocal<HttpServletRequest>();
 
    @Produces @RequestScoped
   public HttpServletRequest getRequest(ServletRequest req) {
      if (req instanceof HttpServletRequest == false)
         throw new IllegalStateException("Not in Servlet environment - req not HttpServletRequest: " + req);
       System.out.println("ObjectFactory.getRequest()");
      //return requestTL.get();
      return (HttpServletRequest) req;
    }
 
    public static void startRequest(HttpServletRequest req) {
       requestTL.set(req);
    }
 
    public static void endRequest() {
       requestTL.remove();
    }
 }
