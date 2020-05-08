 /* - Coeus web framework -------------------------
  *
  * Licensed under the Apache License, Version 2.0.
  *
  * Author: Spiros Tzavellas
  */
 package com.coeusweb.http.multipart;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletRequestWrapper;
 import java.util.Enumeration;
 import java.util.Iterator;
 import java.util.Map;
 
 public class MultipartHttpServletRequest extends HttpServletRequestWrapper{
   
   private Map<String, String[]> parameters;
   private Map<String, FormFile[]> files;
   
   
   public MultipartHttpServletRequest(
       HttpServletRequest request,
       Map<String, String[]> parameters,
       Map<String, FormFile[]> files
   ) {
     super(request);
     this.parameters = parameters;
     this.files = files;
   }
   
   @Override
   public String getParameter(String name) {
     String[] values = parameters.get(name);
     return values == null ? null : values[0];
   }
   
   @Override
  @SuppressWarnings("unchecked")
  public Enumeration getParameterNames() {
    return new Enumeration() {
       Iterator<String> names = parameters.keySet().iterator();
       public boolean hasMoreElements() { return names.hasNext(); }
      public Object nextElement() { return names.next(); }
     };
   }
   
   @Override
   public String[] getParameterValues(String name) {
     return parameters.get(name);
   }
   
   @Override
  @SuppressWarnings("unchecked")
  public Map getParameterMap() {
     return parameters;
   }
   
   public FormFile getFile(String name) {
     FormFile[] values = files.get(name);
     return values == null ? null : values[0];
   }
   
   public Iterator<String> getFileNames() {
     return files.keySet().iterator();
   }
   
   public FormFile[] getFiles(String name) {
     return files.get(name);
   }
   
   public Map<String, FormFile[]> getFileMap() {
     return files;
   }
 }
