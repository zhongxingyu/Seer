 /* Copyright 2004-2005 the original author or authors.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *      http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.codehaus.groovy.grails.web.pages;
 
 import groovy.lang.*;
 import groovy.text.Template;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.codehaus.groovy.control.CompilationFailedException;
 import org.codehaus.groovy.grails.commons.GrailsApplication;
 import org.codehaus.groovy.grails.web.metaclass.ControllerDynamicMethods;
 import org.codehaus.groovy.grails.web.metaclass.GetParamsDynamicProperty;
 import org.codehaus.groovy.grails.web.metaclass.GetSessionDynamicProperty;
 import org.codehaus.groovy.grails.web.servlet.GrailsApplicationAttributes;
 import org.codehaus.groovy.runtime.InvokerHelper;
 import org.springframework.context.ApplicationContext;
 
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.*;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.*;
 
 /**
  * A GroovyPagesTemplateEngine based on (but not extending) the existing TemplateEngine implementations
  * within Groovy. It allows GSP pages to be re-used in different context using code like the below:
  *
  * <code>
  *      Template t = new GroovyPagesTemplateEngine()
  *                          .createTemplate(context,request,response);
  *      t.make()
  *       .writeTo(out);
  * </code>
  *
  * @author Graeme Rocher
  * @since 12-Jan-2006
  */
 public class GroovyPagesTemplateEngine {
 
     private static final Log LOG = LogFactory.getLog(GroovyPagesTemplateEngine.class);
     private static Map pageCache = Collections.synchronizedMap(new HashMap());
     private boolean showSource;
     private GroovyClassLoader classLoader;
 
     public void setClassLoader(GroovyClassLoader classLoader) {
         this.classLoader = classLoader;
     }
 
     /**
      * Set whether to show the compiled source in the response
      * @param showSource
      */
     public void setShowSource(boolean showSource) {
         this.showSource = showSource;
     }
 
     /**
      * Retrieves a line number matrix for the specified page that can be used
      * to retrieve the actual line number within the GSP page if the line number within the
      * compiled GSP is known
      *
      * @param context
      * @param url
      * @return An array where the index is the line number witin the compiled GSP and the value is the line number within the source
      */
     public int[] getLineNumbersForPage(ServletContext context,String url) {
         URL pageUrl;
         try {
             pageUrl = getPageUrl(context, url);
             if(pageUrl != null) {
                 PageMeta pm = getPage(url,context,pageUrl,false);
                 if(pm != null) {
                     return pm.lineNumbers;
                 }
             }
         } catch (Exception e) {
             // ignore, non critical method used for retrieving debug info
             LOG.warn("Exception retrieving line numbers from GSP: " + url + ", message: " + e.getMessage());
             LOG.debug("Full stack trace of error", e);
         }
 
         return new int[0];  //To change body of created methods use File | Settings | File Templates.
     }
 
 
     private static class PageMeta {
         private Class servletScriptClass;
         private long lastModified;
         private Map dependencies = new HashMap();
         private InputStream groovySource;
         public String contentType;
         public int[] lineNumbers;
     } // PageMeta
 
 
     /**
      * Create a template for the current request
      *
      * @param context
      * @param request
      * @param response
      * @return  The created template or null if the page was not found
      * @throws IOException
      * @throws ServletException
      */
     public Template createTemplate(ServletContext context, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
         ClassLoader parent = Thread.currentThread().getContextClassLoader();
         if (parent == null) parent = getClass().getClassLoader();
 
         String uri = getPageId(request);
         return createTemplate(uri,context,request,response);
     }
 
     /**
      * Creates a template for the specified uri
      *
      * @param uri
      * @param context
      * @param request
      * @param response
      * @return The created template or null if the page was not found for the specified uri
      * @throws IOException
      * @throws ServletException
      */
    public Template createTemplate(String uri, ServletContext context, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
         URL pageUrl = getPageUrl(context,uri);
         if (pageUrl == null) {
             context.log("GroovyPagesServlet:  \"" + pageUrl + "\" not found");
             return null;
         }
         boolean spillGroovy = showSource && request.getParameter("showSource") != null;
 
         PageMeta pageMeta = getPage(uri, context,pageUrl, spillGroovy);
         return new GroovyPagesTemplate(context,request,response,pageMeta,spillGroovy);
     }
 
     /**
      * Return the page identifier.
      * @param request
      * @return The page id
      */
     protected String getPageId(HttpServletRequest request) {
         // Get the name of the Groovy script (intern the name so that we can
         // lock on it)
         int contextLength = request.getContextPath().length();
         return request.getRequestURI().substring(contextLength).intern();
     } // getPageId()
 
     /**
      * Return the page URL from the request path.
      * @param pageId
      * @return
      * @throws java.net.MalformedURLException
      */
     protected URL getPageUrl(ServletContext context, String pageId) throws MalformedURLException {
         // Check to make sure that the file exists in the web application
         if(LOG.isDebugEnabled()) {
             LOG.debug("Loading GSP for url ["+pageId+"]");
         }
         return context.getResource(pageId);
     } // getPageUrl()
 
     /**
      * Lookup page class or load new one if needed.
      * @param uri
      * @param pageUrl
      * @param spillGroovy
      * @return
      * @throws IOException
      * @throws javax.servlet.ServletException
      */
     protected PageMeta getPage(String uri, ServletContext context,URL pageUrl, boolean spillGroovy)
             throws IOException, ServletException  {
             // Lock on the uri to ensure that only one compile occurs for any script
         synchronized (uri) {
                 // Get the URLConnection
             URLConnection groovyScriptConn = pageUrl.openConnection();
                 // URL last modified
             long lastModified = groovyScriptConn.getLastModified();
                 // Check the cache for the script
             PageMeta pageMeta = (PageMeta)pageCache.get(uri);
                 // If the pageMeta isn't null check all the dependencies
             boolean dependencyOutOfDate = false;
             if (pageMeta != null && !spillGroovy) {
                 isPageNew(pageMeta);
             }
             if (pageMeta == null || pageMeta.lastModified < lastModified || dependencyOutOfDate || spillGroovy) {
                 pageMeta = newPage(uri, context,groovyScriptConn, lastModified, spillGroovy);
             }
             return pageMeta;
         }
     } // getPage()
 
    /**
      * Load and compile new page.
      * @param uri
      * @param groovyScriptConn
      * @param lastModified
      * @param spillGroovy
      * @return
      * @throws IOException
      * @throws ServletException
      */
     private PageMeta newPage(String uri, ServletContext context,URLConnection groovyScriptConn, long lastModified,
                              boolean spillGroovy) throws IOException, ServletException {
         Parse parse = new Parse(uri, groovyScriptConn.getInputStream());
         InputStream in = parse.parse();
         // Make a new pageMeta
         PageMeta pageMeta = new PageMeta();
         pageMeta.contentType = parse.getContentType();
         pageMeta.lineNumbers = parse.getLineNumberMatrix();
 
             // just return groovy and don't compile if asked
         if (spillGroovy) {
             pageMeta.groovySource = in;
             return pageMeta;
         }
             // Compile the script into an object
 
         Loader loader = new Loader(classLoader, context, uri, pageMeta.dependencies);
         Class scriptClass;
         try {
             scriptClass =
                 loader.parseClass(in, uri.substring(1));
         } catch (CompilationFailedException e) {
             throw new ServletException("Could not parse script: " + uri, e);
         }
         pageMeta.servletScriptClass = scriptClass;
         pageMeta.lastModified = lastModified;
         pageCache.put(uri, pageMeta);
         return pageMeta;
     } // newPage()
 
     /**
      * Is page new or changed?
      * @param pageMeta page data
      * @return true if compile needed
      */
     private boolean isPageNew(PageMeta pageMeta) {
         for (Iterator i = pageMeta.dependencies.keySet().iterator(); i.hasNext(); ) {
             URLConnection urlc;
             URL url = (URL)i.next();
             try {
                 urlc = url.openConnection();
                 urlc.setDoInput(false);
                 urlc.setDoOutput(false);
                 long dependentLastModified = urlc.getLastModified();
                 if (dependentLastModified > ((Long)pageMeta.dependencies.get(url)).longValue()) {
                     return true;
                 }
             } catch (IOException ioe) {
                 return true;
             }
         }
         return false;
     } // isPageNew()
 
     /**
      * An instance of the groovy.text.Template interface that knows how to
      * make in instance of GroovyPageTemplateWritable
      *
      * @author Graeme Rocher
      * @since 12-Jan-2006
      */
     protected static class GroovyPagesTemplate implements Template {
         private HttpServletResponse response;
         private HttpServletRequest request;
         private ServletContext context;
         private boolean showSource = false;
         private PageMeta pageMeta;
 
 
         public GroovyPagesTemplate(ServletContext context,
                                    HttpServletRequest request,
                                    HttpServletResponse response,
                                    PageMeta pageMeta,
                                    boolean showSource) {
             this.request = request;
             this.response = response;
             this.context = context;
 
             this.showSource = showSource;
             this.pageMeta = pageMeta;
         }
 
         public Writable make() {
             return new GroovyPageTemplateWritable(context,request,response,pageMeta,showSource);
         }
 
         public Writable make(Map binding) {
             GroovyPageTemplateWritable gptw = new GroovyPageTemplateWritable(context,request,response,pageMeta,showSource);
             gptw.setBinding(binding);
             return gptw;
         }
     }
 
     /**
      * An instance of groovy.lang.Writable that writes itself to the specified
      * writer, typically the response writer
      * 
      * @author Graeme Rocher
      * @since 12-Jan-2006
      */
     protected static class GroovyPageTemplateWritable implements Writable {
         private HttpServletResponse response;
         private HttpServletRequest request;
         private PageMeta pageMeta;
         private boolean showSource;
         private ServletContext context;
         private Map additionalBinding = new HashMap();
 
         public GroovyPageTemplateWritable(ServletContext context,
                                           HttpServletRequest request,
                                           HttpServletResponse response,
                                           PageMeta pageMeta,
                                           boolean showSource) {
             this.request = request;
             this.response = response;
             this.pageMeta = pageMeta;
             this.showSource = showSource;
             this.context = context;
         }
 
         public void setBinding(Map binding) {
             if(binding != null)
                 this.additionalBinding = binding;
         }
 
         public Writer writeTo(Writer out) throws IOException {
             if (showSource) {
                     // Set it to TEXT
                 response.setContentType("text/plain"); // must come before response.getOutputStream()
                 send(pageMeta.groovySource, out);
                 pageMeta.groovySource = null;
             } else {
                 // Set it to HTML by default
                 if(LOG.isDebugEnabled()) {
                     LOG.debug("Writing response with content type: " + pageMeta.contentType);
                 }
                 response.setContentType(pageMeta.contentType); // must come before response.getWriter()
                 Binding binding = getBinding(request, response, out);
                 Script page = InvokerHelper.createScript(pageMeta.servletScriptClass, binding);
                 page.run();
             }
             return out;
         }
 
         /**
          * Copy all of input to output.
          * @param in
          * @param out
          * @throws IOException
          */
         public static void send(InputStream in, Writer out) throws IOException {
             try {
                 Reader reader = new InputStreamReader(in);
                 char[] buf = new char[8192];
                 for (;;) {
                     int read = reader.read(buf);
                     if (read <= 0) break;
                     out.write(buf, 0, read);
                 }
             } finally {
                 out.close();
                 in.close();
             }
         } // send()
         /**
          * Prepare Bindings before instantiating page.
          * @param request
          * @param response
          * @param out
          * @return the Bindings
          * @throws IOException
          */
         protected Binding getBinding(HttpServletRequest request, HttpServletResponse response, Writer out)
                 throws IOException {
             // Set up the script context
             Binding binding = new Binding();
             
             GroovyObject controller = (GroovyObject)request.getAttribute(GrailsApplicationAttributes.CONTROLLER);
             binding.setVariable(GroovyPage.REQUEST, controller.getProperty(ControllerDynamicMethods.REQUEST_PROPERTY));
             binding.setVariable(GroovyPage.RESPONSE, controller.getProperty(ControllerDynamicMethods.RESPONSE_PROPERTY));
             binding.setVariable(GroovyPage.FLASH, controller.getProperty(ControllerDynamicMethods.FLASH_SCOPE_PROPERTY));
             binding.setVariable(GroovyPage.SERVLET_CONTEXT, context);
             ApplicationContext appContext = (ApplicationContext)context.getAttribute(GrailsApplicationAttributes.APPLICATION_CONTEXT);
             binding.setVariable(GroovyPage.APPLICATION_CONTEXT, appContext);
             binding.setVariable(GrailsApplication.APPLICATION_ID, appContext.getBean(GrailsApplication.APPLICATION_ID));
             binding.setVariable(GrailsApplicationAttributes.CONTROLLER, controller);
             binding.setVariable(GroovyPage.SESSION, controller.getProperty(GetSessionDynamicProperty.PROPERTY_NAME));
             binding.setVariable(GroovyPage.PARAMS, controller.getProperty(GetParamsDynamicProperty.PROPERTY_NAME));
             binding.setVariable(GroovyPage.OUT, out);
 
 
             // Go through request attributes and add them to the binding as the model
             for (Enumeration attributeEnum =  request.getAttributeNames(); attributeEnum.hasMoreElements();) {
                 String key = (String) attributeEnum.nextElement();
                 try {
                     binding.getVariable(key);
                 }
                 catch(MissingPropertyException mpe) {
                     binding.setVariable( key, request.getAttribute(key) );
                 }
             }
             for (Iterator i = additionalBinding.keySet().iterator(); i.hasNext();) {
                 String key =  (String)i.next();
                 binding.setVariable(key, additionalBinding.get(key));
             }
             return binding;
         } // getBinding()
     }
 
 }
