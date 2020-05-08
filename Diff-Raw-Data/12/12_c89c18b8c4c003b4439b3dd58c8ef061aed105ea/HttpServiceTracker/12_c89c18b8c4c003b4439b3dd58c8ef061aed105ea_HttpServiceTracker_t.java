 /*
  * Copyright © 2011 jbundle.org. All rights reserved.
  */
 package com.tourapp.config.web.other;
 
 import java.util.Dictionary;
 import java.util.Hashtable;
 
 import javax.servlet.Servlet;
 
 import org.jbundle.base.screen.control.servlet.html.BaseServlet;
 import org.jbundle.base.util.DBConstants;
 import org.jbundle.base.util.Utility;
 import org.jbundle.util.osgi.finder.ClassServiceUtility;
 import org.jbundle.util.webapp.osgi.BaseOsgiServlet;
 import org.jbundle.util.webapp.osgi.OSGiFileServlet;
 import org.jbundle.util.webapp.redirect.RedirectServlet;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.ServiceReference;
 import org.osgi.service.http.HttpContext;
 import org.osgi.service.http.HttpService;
 import org.osgi.util.tracker.ServiceTracker;
 
 /**
  * HttpServiceTracker - Wait for the http service to come up to add servlets.
  * 
  * @author don
  *
  */
 public class HttpServiceTracker extends ServiceTracker {
 
 	// Set this param to change root URL
 	public static final String WEB_CONTEXT = "org.jbundle.web.webcontext";
 	String webContextPath = null;
 	
 	/**
 	 * Constructor - Listen for HttpService.
 	 * @param context
 	 */
     public HttpServiceTracker(BundleContext context) {
         super(context, HttpService.class.getName(), null);
     }
     
     /**
      * Http Service is up, add my servlets.
      */
     public Object addingService(ServiceReference reference) {
         HttpService httpService = (HttpService) context.getService(reference);
         
         this.addServices(httpService);
         
         return httpService;
     }
     
     public static final String JBUNDLE = "jbundle";
     public static final String AWSTATS = "awstats";
     public static final String DOWNLOAD = "download";
     public static final String GIT_WEB = "git-web";
 
     public static final String CALENDARPANEL = "calendarpanel";
 	public static final String CALENDARPANELJNLP = "calendarpaneljnlp";
 
 	public static final String WEBAPP_CGI = "jbundle-util-webapp-cgi";
     public static final String WEBAPP_FILES = "jbundle-util-webapp-files";
     public static final String WEBAPP_PROXY = "jbundle-util-webapp-proxy";
     public static final String WEBAPP_REDIRECT = "jbundle-util-webapp-redirect";
     public static final String WEBAPP_UPLOAD = "jbundle-util-webapp-upload";
     public static final String WEBAPP_WEBDAV = "jbundle-util-webapp-webdav";
     public static final String WEBAPP_WEBSITE = "jbundle-util-webapp-website";
     public static final String WEBAPP_WEBSTART = "jbundle-util-webapp-webstart-jnlp";
 
     public static final String JCALENDARBUTTON = "jcalendarbutton";
 	public static final String NOTES = "notes";
 	public static final String PICTURES = "pictures";
 	public static final String SIMPLESERVLETS = "simpleservlets";
 	public static final String UPLOAD = "upload";
 
 	String[] paths = {
     		JBUNDLE,
     		AWSTATS,
     		CALENDARPANEL,
     		CALENDARPANELJNLP,
 //x    		"demo",
     		DOWNLOAD,
     		GIT_WEB,
     		WEBAPP_CGI,
     		WEBAPP_FILES,
     		WEBAPP_PROXY,
     		WEBAPP_REDIRECT,
     		WEBAPP_UPLOAD,
     		WEBAPP_WEBDAV,
     		WEBAPP_WEBSITE,
     		WEBAPP_WEBSTART,
     		JCALENDARBUTTON,
     		NOTES,
     		PICTURES,
     		SIMPLESERVLETS,
     		UPLOAD,
     };
     
     /**
      * Http Service is up, add my servlets.
      */
     public void addServices(HttpService httpService) {
     	for (String path : paths)
     	{
     		this.addService(path, httpService);
     	}
     }
     public String STATIC_WEB_FILES = "staticWebFiles";
     public String DEFAULT_STATIC_WEB_FILES = "file:/space/web/";
     /**
      * Http Service is up, add my servlets.
      */
     public void addService(String name, HttpService httpService) {
         try {
             Servlet servlet = null;
             Dictionary<String,String> dictionary = new Hashtable<String,String>();
             dictionary.put(BaseServlet.PATH, name);
         	webContextPath = context.getProperty(WEB_CONTEXT);
             String alias = Utility.addURLPath(webContextPath, name);
             HttpContext httpContext = null;	// Default
 
             if (AWSTATS.equalsIgnoreCase(name))
     		{
             	// Add code here
     		}
             else if (CALENDARPANELJNLP.equalsIgnoreCase(name))
     		{
             	// Add code here
     		}
             else if (WEBAPP_PROXY.equalsIgnoreCase(name))
             {
 //	            servlet = new org.jbundle.util.webapp.proxy.ProxyServlet();
 //	            dictionary.put("remotehost", "localhost");	// Default value
 //	            httpService.registerServlet(alias, servlet, dictionary, httpContext);
             }
             else if (WEBAPP_CGI.equalsIgnoreCase(name))
             {
 //	            servlet = new org.apache.catalina.servlets.CGIServlet();
 //	            dictionary.put("remotehost", "localhost");	// Default value
 //	            httpService.registerServlet(alias, servlet, dictionary, httpContext);
             }
             else if (WEBAPP_FILES.equalsIgnoreCase(name))
             {
 //	            servlet = new org.apache.catalina.servlets.DefaultServlet();
 //	            httpService.registerServlet(alias, servlet, dictionary, httpContext);
             }
             else if (WEBAPP_REDIRECT.equalsIgnoreCase(name))
             {
 	            servlet = new org.jbundle.util.webapp.redirect.RegexRedirectServlet();
 	            dictionary.put("remotehost", "localhost");	// Default value
 	            httpService.registerServlet(alias, servlet, dictionary, httpContext);
             }
             else if (WEBAPP_UPLOAD.equalsIgnoreCase(name))
             {
 	            servlet = new org.jbundle.util.webapp.upload.UploadServlet();
 	            dictionary.put("remotehost", "localhost");	// Default value
 	            httpService.registerServlet(alias, servlet, dictionary, httpContext);
             }
             else if (WEBAPP_WEBDAV.equalsIgnoreCase(name))
             {
 //	            servlet = new org.apache.catalina.servlets.WebdavServlet();
 //	            httpService.registerServlet(alias, servlet, dictionary, httpContext);
             }
             else if (WEBAPP_WEBSITE.equalsIgnoreCase(name))
             {
 //	            servlet = new org.apache.catalina.servlets.DefaultServlet();
 //	            httpService.registerServlet(alias, servlet, dictionary, httpContext);
             }
             else if (WEBAPP_WEBSTART.equalsIgnoreCase(name))
             {
 //	            servlet = new org.jbundle.base.webapp.jnlpservlet.JnlpServlet();
 	//            dictionary.put("remotehost", "localhost");	// Default value
 	  //          httpService.registerServlet(alias, servlet, dictionary, httpContext);
             }
             else if (DOWNLOAD.equalsIgnoreCase(name))
     		{
             	// Add code here
     		}
             else if (GIT_WEB.equalsIgnoreCase(name))
     		{
             	// Add code here
     		}
             else if (NOTES.equalsIgnoreCase(name))
     		{
             	// Add code here
     		}
             else if (UPLOAD.equalsIgnoreCase(name))
     		{
             	// Add code here
     		}
             else if ((JBUNDLE.equalsIgnoreCase(name))
                     || (JCALENDARBUTTON.equalsIgnoreCase(name))
                     || (CALENDARPANEL.equalsIgnoreCase(name))
                     || (SIMPLESERVLETS.equalsIgnoreCase(name))
                     || (PICTURES.equalsIgnoreCase(name))
             			)
             {	// Everything else is a pointer to a static resource
             	String servicePid = DBConstants.BLANK;
                 servlet = (Servlet)ClassServiceUtility.getClassService().makeObjectFromClassName(RedirectServlet.class.getName());
                dictionary.put(RedirectServlet.MATCH_PARAM, DBConstants.BLANK);
                 dictionary.put(RedirectServlet.TARGET, Utility.addURLPath(name, "index.html"));
     	        String urlCodeBase = context.getProperty(STATIC_WEB_FILES);
                 if (urlCodeBase == null)
                 	urlCodeBase = DEFAULT_STATIC_WEB_FILES;
                if (!"/".equals(name))
                	urlCodeBase = Utility.addURLPath(urlCodeBase, name) + "/";	// Should have trailing '/'
                 dictionary.put(OSGiFileServlet.BASE_URL, urlCodeBase);
                 ((BaseOsgiServlet)servlet).init(context, servicePid, dictionary);
     	        httpService.registerServlet(alias, servlet, dictionary, httpContext);
             }
 
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 
     /**
      * Http Service is down, remove my servlets.
      */
     public void removedService(ServiceReference reference, Object service) {
         HttpService httpService = (HttpService) service;
     	for (String path : paths)
     	{
             String fullPath = Utility.addURLPath(webContextPath, path);
            try {
				httpService.unregister(fullPath);
			} catch (IllegalArgumentException e) {
				// Ignore
			}
     	}
         super.removedService(reference, service);
     }
     
 }
