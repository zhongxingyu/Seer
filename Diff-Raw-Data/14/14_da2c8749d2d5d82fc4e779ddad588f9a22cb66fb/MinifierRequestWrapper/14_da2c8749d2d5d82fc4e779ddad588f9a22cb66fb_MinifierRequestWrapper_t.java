 package com.mysema.webmin.impl;
 
import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Enumeration;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletRequestWrapper;
 
import org.apache.commons.collections15.IteratorUtils;

 /**
  * MinifierRequestWrapper provides
  *
  * @author tiwe
  * @version $Id$
  */
 public class MinifierRequestWrapper extends HttpServletRequestWrapper{
 
     private static final List<String> SKIPPED = Arrays.asList("If-Modified-Since", "If-None-Match");
     
     public MinifierRequestWrapper(HttpServletRequest request) {
         super(request);
     }
     
     @Override
     public String getHeader(String name) {
         if (SKIPPED.contains(name)){
             return null;
         }else{
             return super.getHeader(name);
         }        
     }
     
     @Override
     public long getDateHeader(String name) {
         if (SKIPPED.contains(name)){
             return -1;
         }else{
             return super.getDateHeader(name);
         }
     }
     
    @SuppressWarnings("unchecked")
     @Override
     public Enumeration<String> getHeaderNames() {
         Enumeration<String> headerNames = super.getHeaderNames();
        List<String> rv = new ArrayList<String>();
         while (headerNames.hasMoreElements()){
             String name = headerNames.nextElement();
             if (!SKIPPED.contains(name)){
                 rv.add(name);
             }
         }
        return IteratorUtils.asEnumeration(rv.iterator());
     }
 
 }
