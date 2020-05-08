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
import org.osgi.service.http.HttpService;
 
 /**
  * HttpServiceTracker - Wait for the http service to come up to add servlets.
  * 
  * @author don
  *
  */
 public class HttpServiceTracker extends org.jbundle.util.webapp.osgi.HttpServiceTracker {
     
     public static final String JBUNDLE = "jbundle";
     public static final String AWSTATS = "awstats";
     public static final String DOWNLOAD = "download";
     public static final String GIT_WEB = "git-web";
 
     public static final String CALENDARPANEL = "calendarpanel";
 
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
 //x    		CALENDARPANELJNLP,
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
     
     public String STATIC_WEB_FILES = "staticWebFiles";
     public String DEFAULT_STATIC_WEB_FILES = "file:/space/web/";
 
 	/**
 	 * Constructor - Listen for HttpService.
 	 * @param context
 	 */
     public HttpServiceTracker(BundleContext context) {
         super(context, null, null);
     }
     
     /**
      * Get all the web paths to add.
      * @return
      */
    public String[] getServletNames()
     {
     	return paths;
     }
     
     /**
     * Http Service is up, add this servlet.
      */
    public Servlet addService(String name, HttpService httpService) {
         Servlet servlet = null;
         try {
            Dictionary<String,String> dictionary = this.getDictionary();    // Get persistent configuration
            dictionary.put(BaseServlet.PATH, name);
            String alias = this.getPathFromName(name);
        	String servicePid = DBConstants.BLANK;

             if (AWSTATS.equalsIgnoreCase(name))
     		{
                 servlet = new org.apache.catalina.servlets.CGIServlet();
     		}
             else if (WEBAPP_PROXY.equalsIgnoreCase(name))
             {
 	            servlet = new org.jbundle.util.webapp.proxy.ProxyServlet();
             }
             else if (WEBAPP_CGI.equalsIgnoreCase(name))
             {
 	            servlet = new org.apache.catalina.servlets.CGIServlet();
             }
             else if (WEBAPP_FILES.equalsIgnoreCase(name))
             {
 	            servlet = new org.jbundle.util.webapp.osgi.OSGiFileServlet();
             }
             else if (WEBAPP_REDIRECT.equalsIgnoreCase(name))
             {
 	            servlet = new org.jbundle.util.webapp.redirect.RegexRedirectServlet();
             }
             else if (WEBAPP_UPLOAD.equalsIgnoreCase(name))
             {
 	            servlet = new org.jbundle.util.webapp.upload.UploadServlet();
             }
             else if (WEBAPP_WEBDAV.equalsIgnoreCase(name))
             {
 	            servlet = new org.apache.catalina.servlets.WebdavServlet();
             }
             else if (WEBAPP_WEBSITE.equalsIgnoreCase(name))
             {
                 servlet = new org.jbundle.util.webapp.osgi.OSGiFileServlet();
             }
             else if (WEBAPP_WEBSTART.equalsIgnoreCase(name))
             {
 	            servlet = new org.jbundle.util.webapp.jnlpservlet.JnlpServlet();
             }
             else if (DOWNLOAD.equalsIgnoreCase(name))
     		{
                 servlet = new org.jbundle.util.webapp.osgi.OSGiFileServlet();
     		}
             else if (GIT_WEB.equalsIgnoreCase(name))
     		{
                 servlet = new org.apache.catalina.servlets.CGIServlet();
     		}
             else if (NOTES.equalsIgnoreCase(name))
     		{
                 servlet = new org.apache.catalina.servlets.WebdavServlet();
     		}
             else if (UPLOAD.equalsIgnoreCase(name))
     		{
                 servlet = new org.jbundle.util.webapp.upload.UploadServlet();
     		}
             else if ((JBUNDLE.equalsIgnoreCase(name))
                     || (JCALENDARBUTTON.equalsIgnoreCase(name))
                     || (CALENDARPANEL.equalsIgnoreCase(name))
                     || (SIMPLESERVLETS.equalsIgnoreCase(name))
                     || (PICTURES.equalsIgnoreCase(name))
             			)
             {	// Everything else is a pointer to a static resource
                 servlet = (Servlet)ClassServiceUtility.getClassService().makeObjectFromClassName(RedirectServlet.class.getName());
                 dictionary.put(RedirectServlet.MATCH_PARAM, DBConstants.BLANK);
                 dictionary.put(RedirectServlet.TARGET, Utility.addURLPath(name, "index.html"));
     	        String urlCodeBase = context.getProperty(STATIC_WEB_FILES);
                 if (urlCodeBase == null)
                 	urlCodeBase = DEFAULT_STATIC_WEB_FILES;
                 if (!"/".equals(name))
                 	urlCodeBase = Utility.addURLPath(urlCodeBase, name) + "/";	// Should have trailing '/'
                 dictionary.put(OSGiFileServlet.BASE_URL, urlCodeBase);
             }
            if (servlet instanceof BaseOsgiServlet)
                ((BaseOsgiServlet) servlet).init(context, servicePid, dictionary);
            if (servlet != null)
                httpService.registerServlet(alias, servlet, dictionary, httpContext);

         } catch (Exception e) {
             e.printStackTrace();
         }
         return servlet;
     }
 
 }
