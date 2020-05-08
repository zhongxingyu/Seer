 package net.java.dev.weblets;
 
 import javax.faces.context.FacesContext;
 
 /**
  * @author Werner Punz Weblet Utils class which gives the interfaces for the JSF part of weblets
  */
 public class FacesWebletUtils {
 
     /**
      * returns the absolute url with the context path
      *
      * @param context  the current faces context
      * @param weblet   the weblet name
      * @param pathInfo the path info
      * @return a url with the current web-app context path to the weblet
      */
     public static String getURL(FacesContext context, String weblet, String pathInfo) {
        return WebletUtils.getResource(weblet, pathInfo);
     }
 
     /**
      * internal api which can be used by component
      * sets to suppress double includes
      * for jsf components!
      *
      * @param context
      * @param weblet
      * @param pathInfo
      * @param suppressDoubleIncludes
      * @return
      */
     public static String getURL(FacesContext context, String weblet, String pathInfo, boolean suppressDoubleIncludes) {
         return WebletUtils.getURL(context.getExternalContext().getRequest(), weblet, pathInfo, suppressDoubleIncludes);
     }
 
     /**
      * getResource which also suppresses double includes
      * for internal handling
      * comes in handy with the subbundles which always deliver the bundle id as resource!
      *
      * @param context
      * @param weblet
      * @param pathInfo
      * @param suppressDoubleIncludes
      * @return
      */
     public static String getResource(FacesContext context, String weblet, String pathInfo, boolean suppressDoubleIncludes) {
         return WebletUtils.getResource(context.getExternalContext(), weblet, pathInfo, suppressDoubleIncludes);
     }
 
     /**
      * returns the relative url without the context path
      *
      * @param context  the current faces context
      * @param weblet   the weblet name
      * @param pathInfo the path info to the resource
      * @return a url with the current web-app context path to the weblet
      */
     public static String getResource(FacesContext context, String weblet, String pathInfo) {
         return WebletUtils.getResource(weblet, pathInfo);
     }
 
     /**
      * isResourceLoaded check
      *
      * @param context  the current facesContext
      * @param weblet   the weblet
      * @param pathInfo the pathInfo
      * @return
      */
     public static boolean isResourceLoaded(FacesContext context, String weblet, String pathInfo) {
         return WebletUtils.isResourceLoaded(context.getExternalContext().getRequest(), weblet, pathInfo);
     }
 }
