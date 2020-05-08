 /*
  * Created on Sep 21, 2005
  */
 package uk.org.ponder.rsac.servlet;
 
 import java.io.IOException;
 
 import javax.servlet.Filter;
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.springframework.web.context.WebApplicationContext;
 import org.springframework.web.context.support.WebApplicationContextUtils;
 
 import uk.org.ponder.rsac.RSACBeanLocator;
 import uk.org.ponder.util.Logger;
 
 /**
  * @author andrew
  * 
  */
 public class RSACFilter implements Filter {
 
   private RSACBeanLocator rsacbg;
 
   public void init(FilterConfig filterConfig)  {
     WebApplicationContext wac = WebApplicationContextUtils
         .getWebApplicationContext(filterConfig.getServletContext());
    rsacbg = (RSACBeanLocator) wac.getBean("rsacbeanlocator");
    if (rsacbg == null) {
      Logger.log.fatal("Unable to load RSACBeanLocator from application context");
     }
   }
   
   public void doFilter(ServletRequest request, ServletResponse response,
       FilterChain chain) throws IOException, ServletException {
     try {
       RSACUtils.startServletRequest((HttpServletRequest)request, (HttpServletResponse) response, 
           rsacbg, RSACUtils.HTTP_SERVLET_FACTORY);
       chain.doFilter(request, response);
     }
     catch (Exception e) {
       // Catch and log this here because Tomcat's stack rendering is non-standard and crummy.
       Logger.log.error("Error servicing RSAC request: ", e);
     }
     finally {
       rsacbg.endRequest();
     }
   }
 
   public void destroy() {
     this.rsacbg = null;
   }
 
 }
