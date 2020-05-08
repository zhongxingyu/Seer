 package org.jbundle.web.httpservice;
 
 import java.util.Dictionary;
 import java.util.Hashtable;
 
 import javax.servlet.Servlet;
 
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
 public class HttpServiceTracker extends ServiceTracker{
 
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
     String ROOT = "/";
     String INDEX = "/index.html";
     String IMAGES = "/images";
     String LIB = "/lib";
     String DOCS = "/docs";
     String PROXY = "/proxy";
     String TOURAPP = "/tourapp";
     String TABLE = TOURAPP + "/table";
     String IMAGE = TOURAPP + "/image";
     String JNLP = TOURAPP + "/jnlp";
     String TOURAPP_WSDL = TOURAPP + "/wsdl";
     String WSDL = "/wsdl";
     String HTML = "/HTMLServlet";
     String TOURAPP_JNLP = TOURAPP + ".jnlp";
     String XML = TOURAPP + "xml";
     String XSL = TOURAPP + "xsl";
     String XHTML = TOURAPP + "xhtml";
     String JNLP_DOWNLOAD = "/docs/jnlp";	// "*.jnlp";
     String AJAX = "/ajax";
     String MESSAGE = "/message";
     String WS = "/ws";
     String XMLWS = "/xmlws";
     String[] paths = {
     		IMAGES,
     		LIB,
     		DOCS,
     		PROXY,
     		TABLE,
     	    IMAGE,
     	    JNLP,
     	    TOURAPP_WSDL,
     	    HTML,
     	    AJAX,
     	    TOURAPP_JNLP,
     	    TOURAPP,
     	    XML,
     	    XSL,
     	    XHTML,
     	    JNLP_DOWNLOAD,
     	    MESSAGE,
 //    	    WS,
     	    XMLWS,
             ROOT,
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
     /**
      * Http Service is up, add my servlets.
      */
     public void addService(String path, HttpService httpService) {
         try {
             Servlet servlet = null;
             Dictionary<String,String> dictionary = new Hashtable<String,String>();
         	HttpContext httpContext = null;	// new MyHttpContext(context.getBundle());
         	webContextPath = context.getProperty(WEB_CONTEXT);
             String fullPath = addURLPath(webContextPath, path);
 
             if ((IMAGES.equalsIgnoreCase(path)) 
             	|| (LIB.equalsIgnoreCase(path))
                 || (DOCS.equalsIgnoreCase(path)))
             {
             	httpService.registerResources(fullPath, path, httpContext);
             }
             if (PROXY.equalsIgnoreCase(path))
             {
 	            servlet = new org.jbundle.base.remote.proxy.ProxyServlet();
 	            dictionary.put("remotehost", "localhost");	// Default value
 	            httpService.registerServlet(fullPath, servlet, dictionary, httpContext);
             }
             if ((TABLE.equalsIgnoreCase(path)) 
             		|| (IMAGE.equalsIgnoreCase(path))
             		|| (JNLP.equalsIgnoreCase(path))
     	    		|| (TOURAPP_WSDL.equalsIgnoreCase(path))
     	    		|| (HTML.equalsIgnoreCase(path))
     	    		|| (TOURAPP_JNLP.equalsIgnoreCase(path))
             		|| (TOURAPP.equalsIgnoreCase(path)))
             {
             	servlet = new org.jbundle.base.screen.control.servlet.html.HTMLServlet();
                 dictionary.put("remotehost", "localhost");	// Default value
             	httpService.registerServlet(fullPath, servlet, dictionary, httpContext);
             }
             if (XML.equalsIgnoreCase(path))
             {
 	            servlet = new org.jbundle.base.screen.control.servlet.xml.XMLServlet();
 //x	            dictionary.put("stylesheet-path", "docs/styles/xsl/flat/base/");	// Since stylesheets are in resources
 	            dictionary.put("remotehost", "localhost");
 	            httpService.registerServlet(fullPath, servlet, dictionary, httpContext);
             }
             if ((XSL.equalsIgnoreCase(path)) 
             	|| (XHTML.equalsIgnoreCase(path)))
             {
 	            servlet = new org.jbundle.base.screen.control.xslservlet.XSLServlet();
 //x	            dictionary.put("stylesheet-path", "docs/styles/xsl/flat/base/");	// Since stylesheets are in resources
 	            httpService.registerServlet(fullPath, servlet, dictionary, httpContext);
             }
             if (JNLP_DOWNLOAD.equalsIgnoreCase(path))
             {
 	          servlet = new org.jbundle.util.webapp.jnlpservlet.JnlpServlet();
 //	          servlet = new jnlp.sample.servlet.JnlpDownloadServlet();
 	          httpContext = new JnlpHttpContext(context.getBundle());
 //	          httpService.registerServlet(addURLPath(webContextPath, "*.jnlp"), servlet, dictionary, httpContext);
 	          httpService.registerServlet(fullPath, servlet, dictionary, httpContext);
             }
             if (AJAX.equalsIgnoreCase(path))
             {
             	servlet = new org.jbundle.base.remote.proxy.AjaxServlet();
             	dictionary.put("remotehost", "localhost");
 	            dictionary.put("stylesheet-path", "docs/styles/xsl/flat/base/");	// Since webkit still can't handle import
             	httpService.registerServlet(fullPath, servlet, dictionary, httpContext);
             }
             if ((ROOT.equalsIgnoreCase(path)) 
                 	|| (INDEX.equalsIgnoreCase(path)))
             {
 	            dictionary.put("regex", "www.+.tourgeek.com");
 	            dictionary.put("regexTarget", "demo/index.html");
 	            dictionary.put("ie", "tourappxsl");
 	            dictionary.put("firefox", "tourappxsl");
 	            dictionary.put("chrome", "tourappxsl");
 	            dictionary.put("safari", "tourappxsl");
 	            dictionary.put("webkit", "tourappxsl");
 	            dictionary.put("java", "tourappxhtml");
 	            servlet = new org.jbundle.util.webapp.redirect.RegexRedirectServlet();
 	            httpService.registerServlet(fullPath, servlet, dictionary, httpContext);
             }
             if (MESSAGE.equalsIgnoreCase(path))
             {
 	            servlet = new org.jbundle.base.message.trx.transport.html.MessageServlet();
 	            dictionary.put("remotehost", "localhost");
 	            httpService.registerServlet(fullPath, servlet, dictionary, httpContext);
             }
             if (WS.equalsIgnoreCase(path))
             {
 	//+            servlet = new org.jbundle.base.message.trx.transport.soap.MessageReceivingServlet();
 	//+            dictionary.put("remotehost", "localhost");
 	//+            httpService.registerServlet(fullPath, servlet, dictionary, httpContext);
             }
             if (XMLWS.equalsIgnoreCase(path))
             {
 	            servlet = new org.jbundle.base.message.trx.transport.xml.XMLMessageReceivingServlet();
 	            dictionary.put("remotehost", "localhost");
 	            httpService.registerServlet(fullPath, servlet, dictionary, httpContext);
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
             String fullPath = addURLPath(webContextPath, path);
             httpService.unregister(fullPath);
     	}
         super.removedService(reference, service);
     }
     
     /**
      * Add to http path (**Move this to Util**)
      * @param basePath
      * @param path
      * @return
      */
     public static String addURLPath(String basePath, String path)
     {
     	if (basePath == null)
     		basePath = "";
     	if ((!basePath.endsWith("/")) && (!path.startsWith("/")))
     		path = "/" + path;
     	if (basePath.length() > 0)
     		path = basePath + path;
      	if (path.length() == 0)
     		path = "/";
      	else if ((path.length() > 1) && (path.endsWith("/")))
      		path = path.substring(0, path.length() -1);
     	return path;
     }
 }
