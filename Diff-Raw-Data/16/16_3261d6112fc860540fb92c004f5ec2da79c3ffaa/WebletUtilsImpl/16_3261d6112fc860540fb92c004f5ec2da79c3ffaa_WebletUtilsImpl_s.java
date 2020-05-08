 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package net.java.dev.weblets.impl.misc;
 
 import java.io.InputStream;
 import java.util.Map;
 
 import net.java.dev.weblets.Weblet;
 import net.java.dev.weblets.WebletContainer;
 import net.java.dev.weblets.WebletException;
 import net.java.dev.weblets.WebletRequest;
 import net.java.dev.weblets.impl.WebletContainerImpl;
 import net.java.dev.weblets.impl.Const;
 import net.java.dev.weblets.impl.servlets.WebletRequestImpl;
 import net.java.dev.weblets.util.IWebletUtils;
 import net.java.dev.weblets.util.StringUtils;
 import net.java.dev.weblets.util.AttributeUtils;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 /**
  * A small el function to ease the use of weblets in misc or jsf/faclets jsf 1.2 contexts
  *
  * @author Werner Punz
  */
 public class WebletUtilsImpl implements IWebletUtils {
     private static final String WEBLETS_NOT_INITIALIZED = "weblets not initialized, please check the logs";
     private static final String RES_SERVED = "RES_SERVED";
     static final String URLREGEXP = "^[a-zA-Z]+:.*$";
 
     public WebletUtilsImpl() {
     }
 
     /**
      * resource loading with duplicate
      * resource suppression
      *
      * @param requestSingletonHolder a request singleton object implenting set and get attribute
      * @param weblet                 the weblet name
      * @param pathInfo               the pathinfo
      * @param suppressDuplicates     if set to true duplicates are suppressed!
      * @return
      */
     public String getResource(Object requestSingletonHolder, String weblet, String pathInfo, boolean suppressDuplicates) {
         if (!suppressDuplicates) {
             //touch(requestSingletonHolder, weblet, pathInfo);
             return getResource(weblet, pathInfo);
         }
         String url = getResource(weblet, pathInfo);
         if (AttributeUtils.getAttribute(requestSingletonHolder, RES_SERVED + url) != null) {
             return "";
         } else {
             AttributeUtils.setAttribute(requestSingletonHolder, RES_SERVED + url, Boolean.TRUE);
         }
         return url;
     }
 
     /**
      * a request singleton object implementing get and sett attribute!
      *
      * @param requestSingletonHolder
      * @param weblet
      * @param pathInfo
      * @param suppressDuplicates
      * @return
      */
     public String getURL(Object requestSingletonHolder, String weblet, String pathInfo, boolean suppressDuplicates) {
         if (!suppressDuplicates) {
             //touch(requestSingletonHolder, weblet, pathInfo); 
             return getURL(weblet, pathInfo);
         }
         String url = getResource(weblet, pathInfo);
         if (AttributeUtils.getAttribute(requestSingletonHolder, RES_SERVED + url) != null) {
             return "";
         } else {
             AttributeUtils.setAttribute(requestSingletonHolder, RES_SERVED + url, Boolean.TRUE);
         }
         return getURL(weblet, pathInfo);
     }
 
     private void touch(Object requestSingletonHolder, String weblet, String pathInfo) {
         String url = getResource(weblet, pathInfo);
         AttributeUtils.setAttribute(requestSingletonHolder, RES_SERVED + url, Boolean.TRUE);
     }
 
     /**
      * contractual method returns true if the resource is loaded
      *
      * @param requestSingletonHolder
      * @param weblet
      * @param pathInfo
      * @return
      */
     public boolean isResourceLoaded(Object requestSingletonHolder, String weblet, String pathInfo) {
         String url = getResource(weblet, pathInfo);
         return AttributeUtils.getAttribute(requestSingletonHolder, RES_SERVED + url) != null;
     }
 
     /**
      * resolves into a uri relative to the current application context //pathInfo resolves into server absolue addresses <p/> /pathInfo into context relative
      * addresses
      *
      * @param weblet
      * @param pathInfo
      * @return the resource path
      */
     public String getResource(String weblet, String pathInfo) {
         /* lets be more tolerant regarding paths */
         if (StringUtils.isBlank(pathInfo))
             pathInfo = Const.SLASH;
         WebletContainerImpl container = (WebletContainerImpl) WebletContainer.getInstance();
         if (container == null) {
             initError();
             return WEBLETS_NOT_INITIALIZED;
         }
         return container.getResourceUri(weblet, pathInfo);
     }
 
     /**
      * Reporting case for weblets
      * this methid must be able to deal with requests being null
      * (Special testcases have to be applied to check this!
      *
      * @param weblet   the weblet name
      * @param pathInfo the pathinfo
      * @param mimeType the mimetype
      * @return
      */
     public InputStream getResourceStream(String weblet, String pathInfo, String mimeType) {
         String resourcePath = getResource(weblet, pathInfo);
         WebletRequest request = getRequestFromPath(null, resourcePath);
         return getResourceAsStream(request, mimeType);
     }
 
     private void initError() {
         Log log = LogFactory.getLog(getClass());
         log.error("The weblet container is null");
         log.error("This is an indication that weblets is not initialized");
         log
                 .error("You might have to add   <listener><listener-class>net.java.dev.weblets.WebletsContextListener</listener-class></listener> to your web.xml!!!!");
     }
 
     /**
      * tries a backparsing of a given request String into a given backpath note, use this api call with care, it might fail on nested canonical mappings if two
      * weblets have canonical mappings in common the wrong one might trigger
      *
      * @param request    external request if given
      * @param accessPath the access path
      * @return a valid WebletRequest object or null if the backparsing fails
      */
     public WebletRequest getRequestFromPath(Object request, String accessPath) {
         WebletContainerImpl container = (WebletContainerImpl) WebletContainer.getInstance();
         String contextPath = container.getWebletContextPath();
         Map weblets = container.getRegisteredWeblets();
         if (StringUtils.isBlank(contextPath))
             contextPath = "/";
         accessPath = accessPath.trim();
         if (!accessPath.matches(URLREGEXP) && !accessPath.startsWith(contextPath))
             accessPath = contextPath + accessPath;
         if (accessPath.matches(URLREGEXP) && accessPath.indexOf(contextPath) == -1)
             return null;
         String[] parsed = container.parseWebletRequest(contextPath, accessPath, -1l);
         // parsing failed
         if (parsed == null || parsed.length == 0)
             return null;
         String webletName = parsed[0];
         String webletPath = parsed[1];
         // we preinit the weblet in case it is not there
         Weblet weblet = container.getWeblet(webletName);
         if (weblet == null)
             return null;
         String webletPathInfo = parsed[2];
         WebletRequest webRequest = new WebletRequestImpl(webletName, webletPath, contextPath, webletPathInfo, -1l, request);
         return webRequest;
     }
 
     public InputStream getResourceAsStream(WebletRequest request, String mimetype) throws WebletException {
         WebletContainerImpl container = (WebletContainerImpl) WebletContainer.getInstance();
         return container.getResourceStream(request, mimetype);
     }
 
     public String getURL(String weblet, String pathInfo) {
         /* lets be more tolerant regarding paths */
         if (pathInfo == null || pathInfo.trim().equals(""))
             pathInfo = "/";
         WebletContainerImpl container = (WebletContainerImpl) WebletContainer.getInstance();
         if (container == null) {
             initError();
             return WEBLETS_NOT_INITIALIZED;
         }
         return container.getWebletContextPath() + container.getResourceUri(weblet, pathInfo);
     }
 }
