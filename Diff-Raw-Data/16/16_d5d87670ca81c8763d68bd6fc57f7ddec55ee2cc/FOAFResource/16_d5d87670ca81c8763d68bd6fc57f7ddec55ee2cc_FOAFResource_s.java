 /*
  * Copyright 2007 Wyona
  */
 
 package org.wyona.yanel.impl.resources.foaf;
 
 import org.wyona.yanel.core.Resource;
 import org.wyona.yanel.core.api.attributes.ViewableV2;
 import org.wyona.yanel.core.attributes.viewable.View;
 import org.wyona.yanel.core.attributes.viewable.ViewDescriptor;
 import org.wyona.yanel.impl.resources.BasicXMLResource;
 
 import org.apache.log4j.Category;
 
 import java.net.URL;
 
 /**
  *
  */
 public class FOAFResource extends BasicXMLResource implements ViewableV2 {
 //public class FOAFResource extends Resource implements ViewableV2 {
 
     private static Category log = Category.getInstance(FOAFResource.class);
 
     /**
      *
      */
     public FOAFResource() {
     }
 
     /**
      *
      */
     public boolean exists() {
         log.error("Implementation not finished yet! Needs to check existence of local or remote FOAF.");
         return true;
     }
 
     /**
      *
      */
     public String getMimeType(String viewId) {
         return "application/xml";
     }
 
     /**
      *
      */
     public long getSize() {
         log.error("Implementation not done yet!");
         return -1;
     }
 
     /**
      *
      */
     public View getView(String viewId) {
         View view = new View();
         try {
            URL url = new URL(getRequest().getParameter("href"));
 /*
            view.setInputStream(url.openConnection().getInputStream());
            view.setMimeType(getMimeType(viewId));
 */
            return getXMLView(viewId, url.openConnection().getInputStream());
         } catch (java.io.FileNotFoundException e) {
             log.error(e);
             view.setInputStream(new java.io.StringBufferInputStream(new StringBuffer("No such file: " + e.getMessage()).toString()));
             view.setMimeType("text/plain");
         } catch (Exception e) {
             log.error(e);
             view.setInputStream(new java.io.StringBufferInputStream(new StringBuffer("Exeception: " + e.getMessage()).toString()));
             view.setMimeType("text/plain");
         }
         return view;
     }
 
     /**
      *
      */
     public ViewDescriptor[] getViewDescriptors() {
         return null;
     }
 }
