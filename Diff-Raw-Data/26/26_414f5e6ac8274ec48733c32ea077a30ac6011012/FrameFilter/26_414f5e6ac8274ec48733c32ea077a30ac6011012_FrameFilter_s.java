 package com.psddev.dari.util;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 
 import javax.servlet.FilterChain;
 import javax.servlet.ServletException;
 import javax.servlet.ServletOutputStream;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpServletResponseWrapper;
 
 public class FrameFilter extends AbstractFilter {
 
     private static final String PARAMETER_PREFIX = "_frame.";
     public static final String PATH_PARAMETER = PARAMETER_PREFIX + "path";
     public static final String NAME_PARAMETER = PARAMETER_PREFIX + "name";
     public static final String LAZY_PARAMETER = PARAMETER_PREFIX + "lazy";
 
     private static final String ATTRIBUTE_PREFIX = FrameFilter.class.getName() + ".";
     private static final String DISCARDING_RESPONSE_ATTRIBUTE = ATTRIBUTE_PREFIX + "discardingResponse";
     private static final String DISCARDING_DONE_ATTRIBUTE = ATTRIBUTE_PREFIX + "discardingDone";
     public static final String BODY_ATTRIBUTE = ATTRIBUTE_PREFIX + "frameBody";
 
     @Override
     protected void doRequest(
             final HttpServletRequest request,
             HttpServletResponse response,
             FilterChain chain)
             throws IOException, ServletException {
 
         String path = request.getParameter(PATH_PARAMETER);
 
         if (ObjectUtils.isBlank(path)) {
             chain.doFilter(request, response);
 
         } else {
             DiscardingResponse discarding = new DiscardingResponse(response, path);
 
             request.setAttribute(DISCARDING_RESPONSE_ATTRIBUTE, discarding);
             chain.doFilter(request, discarding);
 
             ServletResponse headerResponse = JspUtils.getHeaderResponse(request, response);
 
             if (headerResponse instanceof HeaderResponse) {
                 String location = ((HeaderResponse) headerResponse).getHeader("Location");
 
                 if (location != null) {
                    response.setHeader("Location", StringUtils.addQueryParameters(location,
                            PATH_PARAMETER, path,
                            NAME_PARAMETER, request.getParameter(NAME_PARAMETER)));
                     return;
                 }
             }
 
             String body = (String) request.getAttribute(BODY_ATTRIBUTE);
 
             if (body != null) {
                response.setContentType("text/plain");
                response.getWriter().write(body);
             }
         }
     }
 
     @Override
     protected void doInclude(
             HttpServletRequest request,
             HttpServletResponse response,
             FilterChain chain)
             throws Exception {
 
         if (Boolean.TRUE.equals(request.getAttribute(DISCARDING_DONE_ATTRIBUTE))) {
             return;
         }
 
         try {
             chain.doFilter(request, response);
 
         } finally {
             DiscardingResponse discarding = (DiscardingResponse) request.getAttribute(DISCARDING_RESPONSE_ATTRIBUTE);
 
             if (discarding != null &&
                     JspUtils.getCurrentServletPath(request).equals(discarding.donePath)) {
                 request.setAttribute(DISCARDING_DONE_ATTRIBUTE, Boolean.TRUE);
             }
         }
     }
 
     private static class DiscardingResponse extends HttpServletResponseWrapper {
 
         public final String donePath;
 
         private final ServletOutputStream output = new ServletOutputStream() {
             @Override
             public void write(int b) {
             }
         };
 
         private final PrintWriter writer = new PrintWriter(output);
 
         public DiscardingResponse(HttpServletResponse response, String donePath) {
             super(response);
             this.donePath = donePath;
         }
 
         @Override
         public ServletOutputStream getOutputStream() throws IOException {
             return output;
         }
 
         @Override
         public PrintWriter getWriter() throws IOException {
             return writer;
         }
     }
 }
