 /*
  * Copyright (c) 2008 Mysema Ltd.
  * All rights reserved.
  * 
  */
 package com.mysema.webmin.html;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.servlet.*;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpServletResponseWrapper;
 
 /**
  * JspMinifierFilter provides
  *
  * @author tiwe
  * @version $Id$
  */
 public class HTMLMinifierFilter implements Filter {
     
     private final Set<String> skipList = new HashSet<String>(Arrays.asList("js","css","gif","png","jpg","rss"));
     
     public void destroy() {
     }
 
     public void doFilter(ServletRequest request, ServletResponse response,
             FilterChain chain) throws IOException, ServletException {
         String url = ((HttpServletRequest)request).getRequestURI();
         String suffix = url.substring(url.lastIndexOf('.')+1);
         if (skipList.contains(suffix)){
             chain.doFilter(request, response);
             return;
         }
         
         final HttpServletResponse original = (HttpServletResponse)response;
         StringWriter targetWriter = new StringWriter(20 * 1024);
         final PrintWriter writer = new PrintWriter(targetWriter);
         response = new HttpServletResponseWrapper((HttpServletResponse) response){
             public PrintWriter getWriter() throws IOException {
                 String ct = original.getContentType();
                if (ct.equals("text/html") || ct.equals("application/xhtml+xml")){
                     return writer;    
                 }else{
                     return original.getWriter();
                 }                
             }
         };
         
         chain.doFilter(request, response);
                 
         if (targetWriter.getBuffer().length() > 0){
             original.getWriter().write(HTMLMinifier.minify(targetWriter.toString()));    
         }        
     }
 
     public void init(FilterConfig filterConfig) throws ServletException {        
     }
 
 }
