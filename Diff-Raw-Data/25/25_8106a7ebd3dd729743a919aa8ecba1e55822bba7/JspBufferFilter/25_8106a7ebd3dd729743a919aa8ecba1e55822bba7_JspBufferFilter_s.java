 package com.psddev.dari.util;
 
 import javax.servlet.Servlet;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.jsp.PageContext;
 
 /**
  * Allows per-thread JSP buffer override.
  *
 * @see Static#setBuffer
 * @see Static#removeBuffer
  */
 public class JspBufferFilter extends AbstractFilter {
 
    private static final ThreadLocal<Integer> BUFFER_OVERRIDE = new ThreadLocal<Integer>();
 
     @Override
     protected void doInit() {
         JspUtils.wrapDefaultJspFactory(ThreadLocalBufferJspFactory.class);
     }
 
     @Override
     protected void doDestroy() {
         JspUtils.unwrapDefaultJspFactory(ThreadLocalBufferJspFactory.class);
     }
 
     private static class ThreadLocalBufferJspFactory extends JspFactoryWrapper {
 
         @Override
         public PageContext getPageContext(Servlet servlet, ServletRequest request, ServletResponse response, String errorPageUrl, boolean needsSession, int buffer, boolean autoflush) {
             Integer override = BUFFER_OVERRIDE.get();
 
             return super.getPageContext(servlet, request, response, errorPageUrl, needsSession, override != null ? override : buffer, autoflush);
         }
     }
 
     /** {@link JspBufferFilter} utility methods. */
     public static final class Static {
 
         private Static() {
         }
 
        /** Sets the JSP buffer override for this thread. */
         public static void setBufferOverride(int buffer) {
             BUFFER_OVERRIDE.set(buffer);
         }
 
        /** Removes the JSP buffer override for this thread. */
         public static void removeBufferOverride() {
             BUFFER_OVERRIDE.remove();
         }
     }
 }
