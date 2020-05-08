 /**
  * Licensed under the Common Development and Distribution License,
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.sun.com/cddl/
  *   
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
  * implied. See the License for the specific language governing
  * permissions and limitations under the License.
  */
 
 package com.sun.facelets;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.Writer;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Locale;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.el.ELException;
 import javax.faces.FacesException;
 import javax.faces.application.StateManager;
 import javax.faces.application.ViewHandler;
 import javax.faces.component.UIComponent;
 import javax.faces.component.UIViewRoot;
 import javax.faces.context.ExternalContext;
 import javax.faces.context.FacesContext;
 import javax.faces.context.ResponseWriter;
 import javax.faces.render.RenderKit;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServletResponse;
 
 import com.sun.facelets.compiler.Compiler;
 import com.sun.facelets.compiler.SAXCompiler;
 import com.sun.facelets.compiler.TagLibraryConfig;
 import com.sun.facelets.impl.DefaultFaceletFactory;
 import com.sun.facelets.tag.TagDecorator;
 import com.sun.facelets.tag.TagLibrary;
 import com.sun.facelets.tag.ui.UIDebug;
 import com.sun.facelets.util.DevTools;
 import com.sun.facelets.util.FacesAPI;
 import com.sun.facelets.util.FastWriter;
 import com.sun.facelets.util.Resource;
 
 /**
  * ViewHandler implementation for Facelets
  * 
  * @author Jacob Hookom
 * @version $Id: FaceletViewHandler.java,v 1.49 2006/02/23 02:45:10 jhook Exp $
  */
 public class FaceletViewHandler extends ViewHandler {
 
     protected final static Logger log = Logger
             .getLogger("facelets.viewhandler");
 
     public final static long DEFAULT_REFRESH_PERIOD = 2;
 
     public final static String PARAM_REFRESH_PERIO = "facelets.REFRESH_PERIOD";
 
     public final static String PARAM_SKIP_COMMENTS = "facelets.SKIP_COMMENTS";
 
     /**
      * Context initialization parameter for defining what viewIds should be
      * handled by Facelets, and what should not. When left unset, all URLs will
      * be handled by Facelets. When set, it must be a semicolon separated list
      * of either extension mappings or prefix mappings. For example:
      * 
      * <pre>
      * 
      *  
      *   
      *     &lt;context-param&gt;
      *       &lt;param-name&gt;facelets.VIEW_MAPPINGS&lt;/param-name&gt;
      *       &lt;param-value&gt;/demos/*; *.xhtml&lt;/param-value&gt;
      *     &lt;/context-param&gt;
      *    
      *   
      *  
      * </pre>
      * 
      * would use Facelets for processing all viewIds in the "/demos" directory
      * or that end in .xhtml, and use the standard JSP engine for all other
      * viewIds.
      * <p>
      * <strong>NOTE</strong>: when using this parameter, you need to use
      * prefix-mapping for the <code>FacesServlet</code> (that is,
      * <code>/faces/*</code>, not <code>*.jsf</code>).
      * </p>
      */
     public final static String PARAM_VIEW_MAPPINGS = "facelets.VIEW_MAPPINGS";
 
     public final static String PARAM_LIBRARIES = "facelets.LIBRARIES";
 
     public final static String PARAM_DECORATORS = "facelets.DECORATORS";
 
     public final static String PARAM_DEVELOPMENT = "facelets.DEVELOPMENT";
 
     public final static String PARAM_BUFFER_SIZE = "facelets.BUFFER_SIZE";
 
     private final static String STATE_KEY = "~facelets.VIEW_STATE~";
     
     private final static int STATE_KEY_LEN = STATE_KEY.length();
     
     private final static Object STATE_NULL = new Object();
 
     private final ViewHandler parent;
 
     private boolean developmentMode = false;
 
     private int bufferSize;
 
     private String defaultSuffix;
     
     private FaceletFactory faceletFactory;
 
     // Array of viewId extensions that should be handled by Facelets
     private String[] extensionsArray;
 
     // Array of viewId prefixes that should be handled by Facelets
     private String[] prefixesArray;
 
     protected static void removeTransient(UIComponent c) {
         UIComponent d, e;
         if (c.getChildCount() > 0) {
             for (Iterator itr = c.getChildren().iterator(); itr.hasNext();) {
                 d = (UIComponent) itr.next();
                 if (d.getFacets().size() > 0) {
                     for (Iterator jtr = d.getFacets().values().iterator(); jtr
                             .hasNext();) {
                         e = (UIComponent) jtr.next();
                         if (e.isTransient()) {
                             jtr.remove();
                         } else {
                             removeTransient(e);
                         }
                     }
                 }
                 if (d.isTransient()) {
                     itr.remove();
                 } else {
                     removeTransient(d);
                 }
             }
         }
         if (c.getFacets().size() > 0) {
             for (Iterator itr = c.getFacets().values().iterator(); itr
                     .hasNext();) {
                 d = (UIComponent) itr.next();
                 if (d.isTransient()) {
                     itr.remove();
                 } else {
                     removeTransient(d);
                 }
             }
         }
     }
 
     /**
      * 
      */
     public FaceletViewHandler(ViewHandler parent) {
         this.parent = parent;
     }
 
     /**
      * Initialize the ViewHandler during its first request.
      */
     protected void initialize(FacesContext context) {
         synchronized (this) {
             if (this.faceletFactory == null) {
                 log.fine("Initializing");
                 Compiler c = this.createCompiler();
                 this.initializeCompiler(c);
                 this.faceletFactory = this.createFaceletFactory(c);
                 
                 this.initializeMappings(context);
                 this.initializeMode(context);
                 this.initializeBuffer(context);
 
                 log.fine("Initialization Successful");
             }
         }
     }
 
     private void initializeMode(FacesContext context) {
         ExternalContext external = context.getExternalContext();
         String param = external.getInitParameter(PARAM_DEVELOPMENT);
         this.developmentMode = (param != null && "true".equals(param));
     }
 
     private void initializeBuffer(FacesContext context) {
         ExternalContext external = context.getExternalContext();
         String param = external.getInitParameter(PARAM_BUFFER_SIZE);
         this.bufferSize = (param != null && !"".equals(param)) ? Integer
                 .parseInt(param) : -1;
     }
 
     /**
      * Initialize mappings, during the first request.
      */
     private void initializeMappings(FacesContext context) {
         ExternalContext external = context.getExternalContext();
         String viewMappings = external.getInitParameter(PARAM_VIEW_MAPPINGS);
         if ((viewMappings != null) && (viewMappings.length() > 0)) {
             String[] mappingsArray = viewMappings.split(";");
 
             List extensionsList = new ArrayList(mappingsArray.length);
             List prefixesList = new ArrayList(mappingsArray.length);
 
             for (int i = 0; i < mappingsArray.length; i++) {
                 String mapping = mappingsArray[i].trim();
                 int mappingLength = mapping.length();
                 if (mappingLength <= 1) {
                     continue;
                 }
 
                 if (mapping.charAt(0) == '*') {
                     extensionsList.add(mapping.substring(1));
                 } else if (mapping.charAt(mappingLength - 1) == '*') {
                     prefixesList.add(mapping.substring(0, mappingLength - 1));
                 }
             }
 
             extensionsArray = new String[extensionsList.size()];
             extensionsList.toArray(extensionsArray);
 
             prefixesArray = new String[prefixesList.size()];
             prefixesList.toArray(prefixesArray);
         }
     }
 
     protected FaceletFactory createFaceletFactory(Compiler c) {
         long refreshPeriod = DEFAULT_REFRESH_PERIOD;
         FacesContext ctx = FacesContext.getCurrentInstance();
         String userPeriod = ctx.getExternalContext().getInitParameter(
                 PARAM_REFRESH_PERIO);
         if (userPeriod != null && userPeriod.length() > 0) {
             refreshPeriod = Long.parseLong(userPeriod);
         }
         log.fine("Using Refresh Period: " + refreshPeriod + " sec");
         try {
             return new DefaultFaceletFactory(c,
                     Resource.getResourceUrl(ctx,"/"), refreshPeriod);
         } catch (MalformedURLException e) {
             throw new FaceletException("Error Creating FaceletFactory", e);
         }
     }
 
     protected Compiler createCompiler() {
         return new SAXCompiler();
     }
 
     protected void initializeCompiler(Compiler c) {
         FacesContext ctx = FacesContext.getCurrentInstance();
         ExternalContext ext = ctx.getExternalContext();
 
         // load libraries
         String libParam = ext.getInitParameter(PARAM_LIBRARIES);
         if (libParam != null) {
             libParam = libParam.trim();
             String[] libs = libParam.split(";");
             URL src;
             TagLibrary libObj;
             for (int i = 0; i < libs.length; i++) {
                 try {
                     src = ext.getResource(libs[i].trim());
                     if (src == null) {
                         throw new FileNotFoundException(libs[i]);
                     }
                     libObj = TagLibraryConfig.create(src);
                     c.addTagLibrary(libObj);
                     log.fine("Successfully Loaded Library: " + libs[i]);
                 } catch (IOException e) {
                     log.log(Level.SEVERE, "Error Loading Library: " + libs[i],
                             e);
                 }
             }
         }
 
         // load decorators
         String decParam = ext.getInitParameter(PARAM_DECORATORS);
         if (decParam != null) {
             decParam = decParam.trim();
             String[] decs = decParam.split(";");
             TagDecorator decObj;
             for (int i = 0; i < decs.length; i++) {
                 try {
                     decObj = (TagDecorator) Class.forName(decs[i])
                             .newInstance();
                     c.addTagDecorator(decObj);
                     log.fine("Successfully Loaded Decorator: " + decs[i]);
                 } catch (Exception e) {
                     log.log(Level.SEVERE,
                             "Error Loading Decorator: " + decs[i], e);
                 }
             }
         }
 
         // skip params?
         String skipParam = ext.getInitParameter(PARAM_SKIP_COMMENTS);
         if (skipParam != null && "false".equals(skipParam)) {
             c.setTrimmingComments(false);
         }
     }
 
     public UIViewRoot restoreView(FacesContext context, String viewId) {
         if (UIDebug.debugRequest(context)) {
             return new UIViewRoot();
         }
         return this.parent.restoreView(context, viewId);
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see javax.faces.application.ViewHandlerWrapper#getWrapped()
      */
     protected ViewHandler getWrapped() {
         return this.parent;
     }
     
     protected ResponseWriter createResponseWriter(FacesContext context)
             throws IOException, FacesException {
         ExternalContext extContext = context.getExternalContext();
         RenderKit renderKit = context.getRenderKit();
         ServletRequest request = (ServletRequest) extContext.getRequest();
         ServletResponse response = (ServletResponse) extContext.getResponse();
 
         // set the buffer for content
         if (this.bufferSize != -1) {
             response.setBufferSize(this.bufferSize);
         }
 
         // get our content type
         String contentType = null;
 
         // get the encoding
         String encoding = request.getCharacterEncoding();
 
         // Create a dummy ResponseWriter with a bogus writer,
         // so we can figure out what content type the ReponseWriter
         // is really going to ask for
         ResponseWriter writer = renderKit.createResponseWriter(
                 NullWriter.Instance, null, encoding);
 
         contentType = writer.getContentType();
         encoding = writer.getCharacterEncoding();
         
         // safety check
         if (contentType == null) {
             contentType = "text/html";
             if (log.isLoggable(Level.FINEST)) {
                 log.finest("ResponseWriter created had a null ContentType, defaulting to text/html");
             }
         }
         if (encoding == null) {
             encoding = "UTF-8";
             if (log.isLoggable(Level.FINEST)) {
                 log.finest("ResponseWriter created had a null CharacterEncoding, defaulting to UTF-8");
             }
         }
 
         // apply them to the response
         response.setContentType(contentType + "; charset=" + encoding);
         
         // removed 2005.8.23 to comply with J2EE 1.3
         //response.setCharacterEncoding(encoding);
 
         // Now, clone with the real writer
         writer = writer.cloneWithWriter(response.getWriter());
 
         return writer;
     }
 
     protected void buildView(FacesContext context, UIViewRoot viewToRender)
             throws IOException, FacesException {
         // setup our viewId
         String renderedViewId = this.getRenderedViewId(context, viewToRender
                 .getViewId());
         viewToRender.setViewId(renderedViewId);
 
         if (log.isLoggable(Level.FINE)) {
             log.fine("Building View: " + renderedViewId);
         }
 
         // grab our FaceletFactory and create a Facelet
         Facelet f = null;
         FaceletFactory.setInstance(this.faceletFactory);
         try {
             f = this.faceletFactory.getFacelet(viewToRender.getViewId());
         } finally {
             FaceletFactory.setInstance(null);
         }
 
         // populate UIViewRoot
         long time = System.currentTimeMillis();
         f.apply(context, viewToRender);
         time = System.currentTimeMillis() - time;
         if (log.isLoggable(Level.FINE)) {
             log.fine("Took " + time + "ms to build view: "
                     + viewToRender.getViewId());
         }
     }
 
     public void renderView(FacesContext context, UIViewRoot viewToRender)
             throws IOException, FacesException {
 
         // lazy initialize so we have a FacesContext to use
         if (this.faceletFactory == null) {
             this.initialize(context);
         }
 
         // exit if the view is not to be rendered
         if (!viewToRender.isRendered()) {
             return;
         }
 
         // if facelets is not supposed to handle this request
         if (!handledByFacelets(viewToRender)) {
             this.parent.renderView(context, viewToRender);
             return;
         }
 
         // log request
         if (log.isLoggable(Level.FINE)) {
             log.fine("Rendering View: " + viewToRender.getViewId());
         }
 
         try {
             // build view
             this.buildView(context, viewToRender);
 
             // setup writer and assign it to the context
             ResponseWriter origWriter = this.createResponseWriter(context);
             FastWriter stateWriter = new FastWriter(this.bufferSize != -1 ? this.bufferSize : 1024);
             ResponseWriter writer = origWriter.cloneWithWriter(stateWriter);
             context.setResponseWriter(writer);
 
             long time = System.currentTimeMillis();
 
             // render the view to the response
             writer.startDocument();
             if (FacesAPI.getVersion() >= 12) {
                 viewToRender.encodeAll(context);
             } else {
                 encodeRecursive(context, viewToRender);
             }
             writer.endDocument();
 
             // finish writing
             writer.close();
             
             // remove transients for older versions
             if (FacesAPI.getVersion() < 12) {
                 removeTransient(viewToRender);
             }
             
             // save state
             StateManager stateMgr = context.getApplication().getStateManager();
             Object stateObj = stateMgr.saveSerializedView(context);
             
             // flush to origWriter
             String content = stateWriter.toString();
             if ((stateMgr.isSavingStateInClient(context) || FacesAPI.getVersion() >= 12)) {
                 stateWriter.reset();
                 stateMgr.writeState(context, (StateManager.SerializedView) stateObj);
                 String stateStr = stateWriter.toString();
                 int start = 0;
                 int end = content.indexOf(STATE_KEY, start);
                 while (end != -1) {
                     origWriter.write(content, start, end - start);
                     origWriter.write(stateStr);
                     start = end + STATE_KEY_LEN;
                     end = content.indexOf(STATE_KEY, start);
                 }
                 origWriter.write(content, start, content.length() - start);
             } else {
                 origWriter.write(content);
             }
 
             time = System.currentTimeMillis() - time;
             if (log.isLoggable(Level.FINE)) {
                 log.fine("Took " + time + "ms to render view: "
                         + viewToRender.getViewId());
             }
 
         } catch (FileNotFoundException fnfe) {
             this.handleFaceletNotFound(context, viewToRender.getViewId());
         } catch (Exception e) {
             this.handleRenderException(context, e);
         }
     }
 
     protected void handleRenderException(FacesContext context, Exception e)
             throws IOException, ELException, FacesException {
         Object resp = context.getExternalContext().getResponse();
        
        // always log
        if (log.isLoggable(Level.SEVERE)) {
            UIViewRoot root = context.getViewRoot();
            StringBuffer sb = new StringBuffer(64);
            sb.append("Error Rendering View");
            if (root != null) {
                sb.append('[');
                sb.append(root.getViewId());
                sb.append(']');
            }
            log.log(Level.SEVERE, sb.toString(), e);
        }
        
        // handle dev response
         if (this.developmentMode && !context.getResponseComplete()
                 && resp instanceof HttpServletResponse) {
             HttpServletResponse httpResp = (HttpServletResponse) resp;
             httpResp.reset();
             httpResp.setContentType("text/html; charset=UTF-8");
             Writer w = httpResp.getWriter();
             DevTools.debugHtml(w, context, e);
             w.flush();
             context.responseComplete();
         } else if (e instanceof RuntimeException) {
             throw (RuntimeException) e;
         } else if (e instanceof IOException) {
             throw (IOException) e;
         } else {
             throw new FacesException(e.getMessage(), e);
         }
     }
 
     protected void handleFaceletNotFound(FacesContext context, String viewId)
             throws FacesException, IOException {
         String actualId = this.getActionURL(context, viewId);
         Object respObj = context.getExternalContext().getResponse();
         if (respObj instanceof HttpServletResponse) {
             HttpServletResponse respHttp = (HttpServletResponse) respObj;
             respHttp.sendError(HttpServletResponse.SC_NOT_FOUND, actualId);
             context.responseComplete();
         }
     }
 
     protected final static void encodeRecursive(FacesContext context,
             UIComponent viewToRender) throws IOException, FacesException {
         if (viewToRender.isRendered()) {
             viewToRender.encodeBegin(context);
             if (viewToRender.getRendersChildren()) {
                 viewToRender.encodeChildren(context);
             } else if (viewToRender.getChildCount() > 0) {
                 Iterator kids = viewToRender.getChildren().iterator();
                 while (kids.hasNext()) {
                     UIComponent kid = (UIComponent) kids.next();
                     encodeRecursive(context, kid);
                 }
             }
             viewToRender.encodeEnd(context);
         }
     }
 
     /**
      * Determine if Facelets needs to handle this request.
      */
     private boolean handledByFacelets(UIViewRoot viewToRender) {
         // If there's no extensions array or prefixes array, then
         // just make Facelets handle everything
         if ((extensionsArray == null) && (prefixesArray == null)) {
             return true;
         }
 
         String viewId = viewToRender.getViewId();
 
         if (extensionsArray != null) {
             for (int i = 0; i < extensionsArray.length; i++) {
                 String extension = extensionsArray[i];
                 if (viewId.endsWith(extension)) {
                     return true;
                 }
             }
         }
 
         if (prefixesArray != null) {
             for (int i = 0; i < prefixesArray.length; i++) {
                 String prefix = prefixesArray[i];
                 if (viewId.startsWith(prefix)) {
                     return true;
                 }
             }
         }
 
         return false;
     }
 
     public String getDefaultSuffix(FacesContext context) throws FacesException {
         if (this.defaultSuffix == null) {
             ExternalContext extCtx = context.getExternalContext();
             String viewSuffix = extCtx
                     .getInitParameter(ViewHandler.DEFAULT_SUFFIX_PARAM_NAME);
             this.defaultSuffix = (viewSuffix != null) ? viewSuffix
                     : ViewHandler.DEFAULT_SUFFIX;
         }
         return this.defaultSuffix;
     }
 
     protected String getRenderedViewId(FacesContext context, String actionId) {
         ExternalContext extCtx = context.getExternalContext();
         String viewId = actionId;
         if (extCtx.getRequestPathInfo() == null) {
             String facesSuffix = actionId.substring(actionId.lastIndexOf('.'));
             String viewSuffix = this.getDefaultSuffix(context);
             viewId = actionId.replaceFirst(facesSuffix, viewSuffix);
         }
         if (log.isLoggable(Level.FINE)) {
             log.fine("ActionId -> ViewId: " + actionId + " -> " + viewId);
         }
         return viewId;
     }
 
     public void writeState(FacesContext context) throws IOException {
         if (handledByFacelets(context.getViewRoot())) {
             StateManager stateMgr = context.getApplication().getStateManager();
             if (stateMgr.isSavingStateInClient(context) || FacesAPI.getVersion() >= 12) {
                 context.getResponseWriter().write(STATE_KEY);
             }
         } else {
             this.parent.writeState(context);
         }
     }
 
     public Locale calculateLocale(FacesContext context) {
         return this.parent.calculateLocale(context);
     }
 
     public String calculateRenderKitId(FacesContext context) {
         return this.parent.calculateRenderKitId(context);
     }
 
     public UIViewRoot createView(FacesContext context, String viewId) {
         if (UIDebug.debugRequest(context)) {
             return new UIViewRoot();
         }
         return this.parent.createView(context, viewId);
     }
 
     public String getActionURL(FacesContext context, String viewId) {
         return this.parent.getActionURL(context, viewId);
     }
 
     public String getResourceURL(FacesContext context, String path) {
         return this.parent.getResourceURL(context, path);
     }
 
     protected static class NullWriter extends Writer {
 
         static final NullWriter Instance = new NullWriter();
 
         public void write(char[] buffer) {
         }
 
         public void write(char[] buffer, int off, int len) {
         }
 
         public void write(String str) {
         }
 
         public void write(int c) {
         }
 
         public void write(String str, int off, int len) {
         }
 
         public void close() {
         }
 
         public void flush() {
         }
     }
 }
