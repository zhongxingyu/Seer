 /* Copyright 2009 Jeremy Chone - Licensed under the Apache License, Version 2.0
  * http://www.apache.org/licenses/LICENSE-2.0
  */
 package org.snowfk.web;
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileReader;
 import java.io.OutputStream;
 import java.io.PrintWriter;
 import java.io.Reader;
 import java.io.StringReader;
 import java.io.StringWriter;
 import java.io.Writer;
 import java.lang.reflect.InvocationTargetException;
 import java.util.Map;
 import java.util.Set;
 
 import javax.servlet.ServletContext;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.fileupload.disk.DiskFileItemFactory;
 import org.apache.commons.fileupload.servlet.ServletFileUpload;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.snowfk.util.FileUtil;
 import org.snowfk.util.MapUtil;
 import org.snowfk.web.auth.Auth;
 import org.snowfk.web.auth.AuthService;
 import org.snowfk.web.db.hibernate.HibernateHandler;
 import org.snowfk.web.part.ContextModelBuilder;
 import org.snowfk.web.part.CustomFramePriPath;
 import org.snowfk.web.part.Part;
 import org.snowfk.web.part.HttpPriResolver;
 import org.snowfk.web.part.PriUtil;
 import org.snowfk.web.renderer.freemarker.PartCacheManager;
 
 import com.google.inject.Inject;
 import com.google.inject.Singleton;
 import com.google.inject.internal.Nullable;
 
 @Singleton
 public class WebController {
     static private Logger logger = LoggerFactory.getLogger(WebController.class);
     
     private static final String         CHAR_ENCODING               = "UTF-8";
     private static final String         MODEL_KEY_REQUEST           = "r";
     public static int                   BUFFER_SIZE                 = 2048 * 2;
 
     private WebApplication              webApplication;
 
     private ServletFileUpload           fileUploader;
     private ServletContext              servletContext;
     private HibernateHandler            hibernateHandler;
     private PartCacheManager            partCacheManager;
     
     
 
     private ThreadLocal<RequestContext> requestContextTl            = new ThreadLocal<RequestContext>();
 
     private CurrentRequestContextHolder currentRequestContextHolder = new CurrentRequestContextHolder() {
                                                                         @Override
                                                                         public RequestContext getCurrentRequestContext() {
 
                                                                             return requestContextTl.get();
                                                                         }
                                                                     };
 
     @Inject
     public WebController(WebApplication webApplication, @Nullable ServletContext servletContext,
                          @Nullable HibernateHandler hibernateHandler, PartCacheManager partCacheManager) {
 
         this.webApplication = webApplication;
         this.servletContext = servletContext;
         this.hibernateHandler = hibernateHandler;
         this.partCacheManager = partCacheManager;
 
     }
 
     public CurrentRequestContextHolder getCurrentRequestContextHolder() {
         return currentRequestContextHolder;
     }
 
     public void init() {
         webApplication.init();
 
         /* --------- Initialize the FileUploader --------- */
         // Create a factory for disk-based file items
         DiskFileItemFactory factory = new DiskFileItemFactory();
         // Set factory constraints
         // factory.setSizeThreshold(yourMaxMemorySize);
         // factory.setRepository(yourTempDirectory);
 
         fileUploader = new ServletFileUpload(factory);
         /* --------- /Initialize the FileUploader --------- */
     }
 
     public void destroy() {
         webApplication.shutdown();
     }
 
     public void service(HttpServletRequest request, HttpServletResponse response) throws Exception {
         request.setCharacterEncoding(CHAR_ENCODING);
         response.setCharacterEncoding(CHAR_ENCODING);
 
         RequestContext rc = new RequestContext(request, response, servletContext, fileUploader);
         requestContextTl.set(rc);
         try {
             // --------- Open HibernateSession --------- //
             if (hibernateHandler != null) {
                 hibernateHandler.openSessionInView();
             }
 
             // --------- /Open HibernateSession --------- //
 
             // --------- Auth --------- //
 
             AuthService authService = webApplication.getDefaultWebModule().getAuthService();
             if (authService != null) {
                 Auth<?> auth = authService.authRequest(rc);
                 rc.setAuth(auth);
             }
             // --------- /Auth --------- //
 
             // --------- RequestLifeCycle Start --------- //
             for (WebModule webModule : webApplication.getWebModules()){
                 RequestLifeCycle rlc = webModule.getRequestLifeCycle();
                 if (rlc != null){
                     rlc.start(rc);
                 }
             }
             // --------- /RequestLifeCycle Start --------- //            
 
             // --------- Processing the Post (if any) --------- //
 
             if ("POST".equals(request.getMethod())) {
                 String ari = rc.getParam("action");
                 if (ari == null) {
                     ari = rc.getParam("method");
                 }
                 if (ari != null) {
                     WebActionResponse webActionResponse = null;
                     try {
                         webActionResponse = webApplication.processWebAction(ari, rc);
 
                     } catch (Throwable e) {
                         if (e instanceof InvocationTargetException){
                             e = e.getCause();
                         }
                         // TODO Need to handle exception
                         logger.error(getLogErrorString(e));
                         webActionResponse = new WebActionResponse(e);
                     }
                     rc.setWebActionResponse(webActionResponse);
                 }
             }
             // --------- /Processing the Post (if any) --------- //
 
             String[] priPair = HttpPriResolver.getPriPairFromRequest(rc, webApplication.getDefaultWebModuleName());
 
             String pagePri = priPair[0];
             String framePri = priPair[1];
 
             // --------- Check if we have a CustomFrameProvider --------- //
             if (framePri != null) {
                 WebModule webModule = webApplication.getWebModule(PriUtil.getModuleNameFromPri(pagePri));
                 rc.pushCurrentWebModule(webModule);
                 CustomFramePriPath cfpri = webModule.getCustomeFramePriPath();
                 if (cfpri != null) {
                     String pagePriPath = PriUtil.getPathFromPri(pagePri);
                     String framePriPath = PriUtil.getPathFromPri(framePri);
                     String newFramePriPath = cfpri.getFramePriPath(rc, pagePriPath, framePriPath);
                     if (newFramePriPath != null) {
                         framePri = PriUtil.updatePriPath(framePri, newFramePriPath);
                     }
                 }
                 rc.pollCurrentWebModule();
             }
             // --------- /Check if we have a CustomFrameProvider --------- //
 
             String pathInfo = rc.getPathInfo();
 
             if (HttpPriResolver.isTemplateContent(pathInfo) || HttpPriResolver.isJsonContent(pathInfo)) {
                 Part part = webApplication.getPart(pagePri, framePri);
                 serviceTemplateOrJson(part, rc);
 
             } else {
 
                 //// if the content is cache (could be the result of a [@links...) then just include the content
                 String contextPath = request.getContextPath();
                 String href = new StringBuilder(contextPath).append(pathInfo).toString();
                 String content = partCacheManager.getContentForHref(href);
 
                 if (content != null) {
                     serviceStatic(rc.getRes(), href, content, null);
                 }
                 //// Otherwise, service the part
                 else {
                     Part part = webApplication.getPart(pagePri, framePri);
                     serviceStatic(part, rc);
                 }
             }
 
         } catch (Throwable e) {
             if (e instanceof InvocationTargetException){
                 e = e.getCause();
             }
 
             logger.error(getLogErrorString(e));            
         } finally {
             // --------- RequestLifeCycle end --------- //
             for (WebModule webModule : webApplication.getWebModules()){
                 RequestLifeCycle rlc = webModule.getRequestLifeCycle();
                 if (rlc != null){
                     rlc.end(rc);
                 }
             }
             // --------- /RequestLifeCycle end --------- //   
             
             // Remove the requestContext from the threadLocal 
             // NOTE: might want to do that after the closeSessionInView. 
             requestContextTl.remove();
             
             // --------- /Close HibernateSession --------- //
             if (hibernateHandler != null) {
                 hibernateHandler.closeSessionInView();
             }
             // --------- /Close HibernateSession --------- //
 
         }
 
     }
 
     @SuppressWarnings("unchecked")
     public void serviceTemplateOrJson(Part part, RequestContext rc) throws Throwable {
         HttpServletRequest req = rc.getReq();
         HttpServletResponse res = rc.getRes();
 
         req.setCharacterEncoding(CHAR_ENCODING);
         Map rootModel = rc.getRootModel();
 
         rootModel.put(MODEL_KEY_REQUEST, ContextModelBuilder.buildRequestModel(rc));
 
         /* --------- Set Headers --------- */
         res.setContentType("text/html;charset=" + CHAR_ENCODING);
         // if not cachable, then, set the appropriate headers.
 
         res.setHeader("Pragma", "No-cache");
         res.setHeader("Cache-Control", "no-cache,no-store,max-age=0");
         res.setDateHeader("Expires", 1);
         /* --------- /Set Headers --------- */
 
         if (part.getFormatType() == Part.FormatType.json) {
             webApplication.processJsonPart(part, rc);
         } else {
             webApplication.processFreemarkerPart(part, rc);
         }
 
         rc.getWriter().close();
 
     }
 
     public void serviceStatic(Part part, RequestContext rc) throws Exception {
         rc.setCurrentPart(part);
         //first, try to process the part with the WebFile
         boolean webFilePart = webApplication.processWebFilePart(part, rc);
         //if it was not processed by a webFile, then, do the standard process.
         if (!webFilePart) {
             HttpServletResponse res = rc.getRes();
 
             File resourceFile = part.getResourceFile();
             String resourceFullPath = resourceFile.getAbsolutePath();
             if (resourceFile.exists()) {
                 serviceStatic(res, resourceFullPath, null, resourceFile);
             } else {
                 res.sendError(404, "Page does not exists: " + rc.getPathInfo() + " : " + resourceFile.getAbsolutePath());
             }
         }
     }
 
     private void serviceStatic(HttpServletResponse res, String fullPath, String resourceContent, File resourceFile)
                             throws Exception {
 
         // SystemOutUtil.printValue("RouterServlet serviceStatic pri", pri + " > " + part.getPri());
 
         String contentType = servletContext.getMimeType(fullPath);
         //if the servletContext (server) could not fine the mimeType, then, give a little help
         if (contentType == null){
         	contentType = FileUtil.getExtraMimeType(fullPath);
         }
         // long contentLength = -1L;
         int realLength = 0;
 
         /* --------- /Set Headers --------- */
 
         // TODO: need to fix this with something more generic
         if (isCachable(fullPath)) {
             /*
              * NOTE: for now we remove this, in the case of a CSS, we do not
              * know the length, since it is a template contentLength =
              * resourceFile.length();
              * 
              * if (contentLength < Integer.MAX_VALUE) {
              * res.setContentLength((int) contentLength); } else {
              * res.setHeader("content-length", "" + contentLength); }
              */
             // This content will expire in 1 hours.
             final int CACHE_DURATION_IN_SECOND = 60 * 60 * 1; // 1 hours
             final long CACHE_DURATION_IN_MS = CACHE_DURATION_IN_SECOND * 1000;
             long now = System.currentTimeMillis();
 
             res.addHeader("Cache-Control", "max-age=" + CACHE_DURATION_IN_SECOND);
             res.addHeader("Cache-Control", "must-revalidate");// optional
             res.setDateHeader("Last-Modified", now);
             res.setDateHeader("Expires", now + CACHE_DURATION_IN_MS);
         } else {
             res.setHeader("Pragma", "No-cache");
             res.setHeader("Cache-Control", "no-cache,no-store,max-age=0");
             res.setDateHeader("Expires", 1);
         }
 
         /* --------- Set Headers --------- */
         res.setContentType(contentType);
         // Text based content
         if (contentType != null && (contentType.startsWith("text") || contentType.indexOf("javascript") != -1)) {
 
             Writer ow = null;
             Reader reader = null;
             if (resourceContent != null) {
                 reader = new StringReader(resourceContent);
             } else {
                 reader = new FileReader(resourceFile);
             }
             try {
 
                 // create the reader/writer
                 ow = res.getWriter();
 
                 char[] buffer = new char[BUFFER_SIZE];
                 int readLength = reader.read(buffer);
 
                 while (readLength != -1) {
                     realLength += readLength;
                     ow.write(buffer, 0, readLength);
                     readLength = reader.read(buffer);
                 }
             } catch (Exception e) {
                 logger.error(e.getMessage());
             } finally {
                 reader.close();
                 ow.close();
             }
         }
         // binary based content (for now, suppoert only from resourceFile)
         else {
             OutputStream os = res.getOutputStream();
             FileInputStream fis = new FileInputStream(resourceFile);
             BufferedInputStream bis = new BufferedInputStream(fis);
 
             try {
                 byte[] buffer = new byte[BUFFER_SIZE];
                 int len = buffer.length;
                 while (true) {
                     len = bis.read(buffer);
                     realLength += len;
                     if (len == -1)
                         break;
                     os.write(buffer, 0, len);
                 }
 
             } catch (Exception e) {
                 logger.error(e.getMessage());
             } finally {
                 os.close();
                 fis.close();
                 bis.close();
             }
 
         }
 
     }
 
     static Set cachableExtension = MapUtil.setIt(".css", ".js", ".png", ".gif", ".jpeg");
 
     static final private boolean isCachable(String pathInfo) {
         String ext = FileUtil.getFileNameAndExtension(pathInfo)[1];
         return cachableExtension.contains(ext);
     }
     
     static final private String getLogErrorString(Throwable e){
         StringBuilder errorSB = new StringBuilder();
         errorSB.append(e.getMessage());
         StringWriter sw = new StringWriter();
         PrintWriter pw = new PrintWriter(sw);
         e.printStackTrace(pw);
         errorSB.append("\n-- StackTrace:\n").append(sw.toString()).append("\n-- /StackTrace");
         return errorSB.toString();
     }
 
 }
