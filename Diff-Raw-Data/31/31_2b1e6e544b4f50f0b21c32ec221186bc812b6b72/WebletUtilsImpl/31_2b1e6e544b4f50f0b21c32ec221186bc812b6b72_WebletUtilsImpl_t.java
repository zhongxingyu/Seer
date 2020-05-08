 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package net.java.dev.weblets.impl.misc;
 
 import net.java.dev.weblets.WebletContainer;
 import net.java.dev.weblets.impl.WebletContainerImpl;
import net.java.dev.weblets.util.IWebletUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
 
 /**
  * A small el function to ease
  * the use of weblets in misc
  * or jsf/faclets jsf 1.2 contexts
  * 
  * @author Werner Punz
  */
 public class WebletUtilsImpl implements IWebletUtils {
 
    private static final String WEBLETS_NOT_INITIALIZED = "weblets not initialized, please check the logs";


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
        if(container == null) {
        	initError();
        	
        	return WEBLETS_NOT_INITIALIZED;
        }
         return   container.getResourceUri(weblet, pathInfo);
     }
 
	private void initError() {
		Log log = LogFactory.getLog(getClass());
		log.error("The weblet container is null");
		log.error("This is an indication that weblets is not initialized");
		log.error("You might have to add   <listener><listener-class>net.java.dev.weblets.WebletsContextListener</listener-class></listener> to your web.xml!!!!");
	}

 
      public  String getURL(String weblet, String pathInfo) {
         /*lets be more tolerant regarding paths*/
         if(pathInfo == null || pathInfo.trim().equals(""))
            pathInfo = "/";
         WebletContainerImpl container = (WebletContainerImpl) WebletContainer.getInstance();
        if(container == null) {
           	initError();
            return WEBLETS_NOT_INITIALIZED;
        }
         return   container.getWebletContextPath()+container.getResourceUri(weblet, pathInfo);
     }
     
   
 }
