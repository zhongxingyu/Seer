 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package net.java.dev.weblets.impl.misc;
 
 import net.java.dev.weblets.WebletContainer;
import net.java.dev.weblets.util.IWebletUtils;
 import net.java.dev.weblets.impl.WebletContainerImpl;
 
 /**
  * A small el function to ease
  * the use of weblets in misc
  * or jsf/faclets jsf 1.2 contexts
  * 
  * @author Werner Punz
  */
 public class WebletUtilsImpl implements IWebletUtils {
 
    public WebletUtilsImpl() {
     }
 
     /**
      * resolves into a uri relative
      * to the current application context
      * //pathInfo resolves into server absolue addresses
      * 
      * /pathInfo into context relative addresses
      * 
      * @param weblet
      * @param  pathInfo
      * @return  the resource path
      */
      public  String getResource(String weblet, String pathInfo) {
         /*lets be more tolerant regarding paths*/
         if(pathInfo == null || pathInfo.trim().equals(""))
            pathInfo = "/";
         WebletContainerImpl container = (WebletContainerImpl) WebletContainer.getInstance();
         return   container.getResourceUri(weblet, pathInfo);
     }
 
 
      public  String getURL(String weblet, String pathInfo) {
         /*lets be more tolerant regarding paths*/
         if(pathInfo == null || pathInfo.trim().equals(""))
            pathInfo = "/";
         WebletContainerImpl container = (WebletContainerImpl) WebletContainer.getInstance();
         return   container.getWebletContextPath()+container.getResourceUri(weblet, pathInfo);
     }
     
   
 }
