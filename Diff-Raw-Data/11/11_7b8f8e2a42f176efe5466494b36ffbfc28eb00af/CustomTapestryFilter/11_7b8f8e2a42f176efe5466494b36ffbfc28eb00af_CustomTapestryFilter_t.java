 package com.schlock.website;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.tapestry5.TapestryFilter;
 
 import javax.servlet.FilterChain;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServletRequest;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.List;
 
 public class CustomTapestryFilter extends TapestryFilter
 {
    private static final List<String> FILTER_FOLDERS = Arrays.asList("photo", "misc", "cpanel");
 
     public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException
     {
         if (request instanceof HttpServletRequest)
         {
             String url = ((HttpServletRequest) request).getRequestURL().toString();
             if (urlMatchesFilterFolder(url))
             {
                 chain.doFilter(request, response);
                 return;
             }
         }
         super.doFilter(request, response, chain);
     }
 
     private boolean urlMatchesFilterFolder(String url)
     {
         String[] parts = url.split("/");
         if (parts.length > 3)
         {
             String part = parts[3];
             for (String FILTER : FILTER_FOLDERS)
             {
                 if (StringUtils.equals(FILTER, part))
                 {
                     return true;
                 }
             }
         }
         return false;
     }
 }
