 package jpaoletti.jpm;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import javax.servlet.*;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import jpaoletti.jpm.core.PMContext;
 import jpaoletti.jpm.core.PMCoreConstants;
 import jpaoletti.jpm.core.PMSession;
 import jpaoletti.jpm.core.PresentationManager;
 import jpaoletti.jpm.struts.PMEntitySupport;
 import jpaoletti.jpm.struts.PMStrutsConstants;
 import jpaoletti.jpm.struts.PMStrutsContext;
 import org.apache.commons.fileupload.FileItem;
 import org.apache.commons.fileupload.FileItemFactory;
 import org.apache.commons.fileupload.FileUploadException;
 import org.apache.commons.fileupload.disk.DiskFileItemFactory;
 import org.apache.commons.fileupload.servlet.ServletFileUpload;
 
 public class GeneralFilter implements Filter, PMCoreConstants, PMStrutsConstants {
 
     @Override
     public void destroy() {
     }
 
     @Override
     public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
         final HttpServletRequest req = (HttpServletRequest) request;
         req.setCharacterEncoding("UTF-8");
         /**
          * Optimization to avoid unused connections to data source.
          */
         if (isIgnored(req.getRequestURI())) {
             chain.doFilter(request, response);
         } else {
             final PMStrutsContext ctx = initJPMContext(req, (HttpServletResponse) response);
             try {
                 if (ctx != null) {
                     ctx.getPersistenceManager().init(ctx.getPersistenceManager().newConnection());
                     chain.doFilter(request, response);
                 }
             } catch (ServletException e) {
                 error(ctx, e);
                 throw e;
             } catch (Exception e) {
                 error(ctx, e);
             } finally {
                 try {
                     if (ctx != null) {
                         ctx.getPersistenceManager().finish(ctx);
                     }
                 } catch (Exception e) {
                     error(ctx, e);
                 }
             }
         }
     }
 
     protected void error(PMStrutsContext ctx, Exception e) {
         if (PresentationManager.getPm() != null) {
             PresentationManager.getPm().error(e);
         }
     }
 
     @Override
     public void init(FilterConfig arg0) throws ServletException {
     }
 
     protected List<String> getSecurityExceptions() {
         final List<String> exceptions = new ArrayList<String>();
         exceptions.add("/login.do");
         return exceptions;
     }
 
     protected boolean isIgnored(String s) {
         if (PresentationManager.getPm() != null) {
             final String[] ignoredExtensions = PresentationManager.getPm().getCfg().getProperty("struts-ignored-extensions", "css,gif,png,jpg,js").split(",");
             for (String ext : ignoredExtensions) {
                 if (s.endsWith("." + ext.trim())) {
                     return true;
                 }
             }
             return false;
         } else {
             return true;
         }
     }
 
     protected PMStrutsContext initJPMContext(HttpServletRequest request, HttpServletResponse response) throws IOException {
         final PresentationManager pm = PresentationManager.getPm();
         request.setAttribute("pm", pm);
         if (pm == null) {
             return null;
         }
         final String servletPath = request.getServletPath();
         //Login is the only action that can be accessed directly
         if (servletPath.endsWith(".do")) {
             final List<String> exceptions = getSecurityExceptions();
             if (!exceptions.contains(servletPath)) {
                 if (pm.getCfg().getBool("secure-urls", true)) {
                     response.sendError(403);
                     return null;
                 } else {
                     pm.warn("Insecure access to '" + servletPath + "'");
                 }
             }
         }
 
         final PMEntitySupport o = (PMEntitySupport) request.getSession().getAttribute(ENTITY_SUPPORT);
         if (o == null) {
             PMEntitySupport es = PMEntitySupport.getInstance();
             es.setContext_path(request.getContextPath());
             request.getSession().setAttribute(ENTITY_SUPPORT, es);
         }
         final PMSession pmsession = PMEntitySupport.getPMSession(request);
         final String sessionId = (pmsession != null) ? pmsession.getId() : "";
         final PMStrutsContext ctx = new PMStrutsContext(sessionId);
         request.setAttribute("ctx", ctx);
         ctx.setRequest(request);
         ctx.setResponse((HttpServletResponse) response);
         ctx.getRequest().setAttribute(PM_CONTEXT, ctx);
         parseParameters(request, ctx);
         return ctx;
     }
 
     protected void parseParameters(HttpServletRequest request, final PMStrutsContext ctx) {
         boolean isMultipart = ServletFileUpload.isMultipartContent(request);
         if (!isMultipart) {
             for (Object object : request.getParameterMap().entrySet()) {
                 Map.Entry entry = (Map.Entry) object;
                 ctx.put(PMContext.PARAM_PREFIX + entry.getKey(), entry.getValue());
             }
         } else {
             final FileItemFactory factory = new DiskFileItemFactory();
             final ServletFileUpload upload = new ServletFileUpload(factory);
             try {
                 final List<FileItem> items = upload.parseRequest(request);
                 for (FileItem item : items) {
                     if (item.isFormField()) {
                        ctx.put(PMContext.PARAM_PREFIX + item.getFieldName(), item.getString());
                     } else {
                         ctx.put(PMContext.PARAM_PREFIX + item.getFieldName(), item);
                     }
                 }
             } catch (FileUploadException ex) {
                 ctx.getPresentationManager().error(ex);
             }
         }
     }
 }
