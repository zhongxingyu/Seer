 package com.psddev.dari.util;
 
 import javax.servlet.Servlet;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.jsp.PageContext;
 
 /**
  * Allows per-thread JSP buffer override.
  *
 * @see Static#overrideBuffer
 * @see Static#restoreBuffer
  */
 public class JspBufferFilter extends AbstractFilter {
 
    private static final ThreadLocalStack<Integer> BUFFER_OVERRIDE = new ThreadLocalStack<Integer>();
 
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
 
        /** Overrides the current JSP buffer value for this thread. */
        public static void overrideBuffer(int buffer) {
            BUFFER_OVERRIDE.push(buffer);
        }

        /** Restores the last JSP buffer value for this thread. */
        public static Integer restoreBuffer() {
            return BUFFER_OVERRIDE.pop();
        }

        /** @deprecated Use {@link #pushBufferOverride} instead. */
        @Deprecated
         public static void setBufferOverride(int buffer) {
             BUFFER_OVERRIDE.set(buffer);
         }
 
        /** @deprecated Use {@link #popBufferOverride} instead. */
        @Deprecated
         public static void removeBufferOverride() {
             BUFFER_OVERRIDE.remove();
         }
     }
 }
