 package com.fatwire.cs.profiling.concurrent.filter;
 
 import java.io.IOException;
 
 import javax.servlet.FilterChain;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.fatwire.cs.profiling.concurrent.RequestCounter;
import com.fatwire.gst.web.servlet.profiling.servlet.filter.RunOnceFilter;
 
 public class RequestCounterFilter extends RunOnceFilter {
     private RequestCounter requestCounter;
 
     public void destroy() {
         requestCounter = null;
         super.destroy();
 
     }
 
     @Override
     protected void doFilterOnce(HttpServletRequest request,
             HttpServletResponse response, FilterChain chain)
             throws IOException, ServletException {
         requestCounter.start(request);
         try {
             chain.doFilter(request, response);
         } finally {
             requestCounter.end(request);
         }
     }
 
     /**
      * @return the requestCounter
      */
     public RequestCounter getRequestCounter() {
         return requestCounter;
     }
 
     /**
      * @param requestCounter the requestCounter to set
      */
     public void setRequestCounter(RequestCounter requestCounter) {
         this.requestCounter = requestCounter;
     }
 
 }
