 package com.thoughtworks.lirenlab.interfaces.common.filters;
 
 import javax.servlet.Filter;
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import java.io.IOException;
 
 import static com.google.common.base.Preconditions.checkState;
 
 public class SetCharacterEncodingFilter implements Filter {
 
     public static final String ENCODING = "encoding";
     private String encoding;
 
     @Override
     public void init(final FilterConfig filterConfig) throws ServletException {
        String encoding = filterConfig.getInitParameter(ENCODING);
         checkState(encoding != null, "encoding must be set");
     }
 
     @Override
     public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
         request.setCharacterEncoding(encoding);
         chain.doFilter(request, response);
     }
 
     @Override
     public void destroy() {
         encoding = null;
     }
 }
