 /**
  * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
  * @version $Id$
  */
 package org.gridlab.gridsphere.layout;
 
 import org.gridlab.gridsphere.portlet.*;
 import org.gridlab.gridsphere.portlet.impl.SportletLog;
 import org.gridlab.gridsphere.portlet.impl.SportletProperties;
 import org.gridlab.gridsphere.portletcontainer.GridSphereEvent;
 
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.ServletContext;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.Vector;
 import java.util.List;
 
 /**
  * The <code>PortletLayoutEngine</code> is a singleton that is responsible for managing
  * user's layouts. It also manages portlet web application default layout
  * configurations that can be potentially added to a user layout
  * via the PortletLayout Service.
  * <p/>
  * The portlet layout engine is a higher level manager of portlet containers
  * that represent a users customized layout. The portlet layout engine is used
  * by the {@link org.gridlab.gridsphere.servlets.GridSphereServlet}
  * Expect the PortletLayoutEngine methods to change possibly....
  */
 public class PortletLayoutEngine {
 
     protected static PortletLog log = SportletLog.getInstance(PortletLayoutEngine.class);
     private static PortletLayoutEngine instance = new PortletLayoutEngine();
 
     private PortletPageFactory pageFactory = PortletPageFactory.getInstance();
 
     private PortletFrameRegistry registry = PortletFrameRegistry.getInstance();
 
 
     /**
      * Constructs a concrete instance of the PortletLayoutEngine
      */
     private PortletLayoutEngine() {
     }
 
     public void init(ServletContext ctx) throws PortletException {
         try {
             pageFactory.init(ctx);
         } catch (Exception e) {
             String error = "Unable to initialize PortletPageFactory!";
             log.error(error, e);
             throw new PortletException(error, e);
         }
     }
 
     /**
      * Returns the single instance of the PortletLayoutEngine
      *
      * @return the PortletLayoutEngine instance
      */
     public static PortletLayoutEngine getInstance() {
         return instance;
     }
 
     public PortletPage getPortletPage(GridSphereEvent event) {
 
         PortletRequest req = event.getPortletRequest();
 
         // Check for framework errors
         Exception portletException = (Exception) req.getAttribute(SportletProperties.ERROR);
         if (portletException != null) {
             return pageFactory.createErrorPage(req);
         }
 
         return pageFactory.createPortletPage(req);
     }
 
    public void setHeaders(HttpServletResponse res) {
         res.setContentType("text/html; charset=utf-8"); // Necessary to display UTF-8 encoded characters
         res.setHeader("Cache-Control","no-cache"); //Forces caches to obtain a new copy of the page from the origin server
         res.setHeader("Cache-Control","no-store"); //Directs caches not to store the page under any circumstance
         res.setHeader("Expires", "0"); //Causes the proxy cache to see the page as "stale"
         res.setHeader("Pragma","no-cache"); //HTTP 1.0 backward compatibility
     }
 
     /**
      * Services a portlet container instance by rendering its presentation
      *
      * @param event the gridsphere event
      * @throws IOException if an I/O error occcurs during processing
      */
     public void service(GridSphereEvent event) throws IOException {
        log.debug("in service()");
         PortletPage page = getPortletPage(event);
 
        setHeaders(event.getPortletResponse());
         //int numcomps = page.getComponentIdentifierList().size();
         if (event.getPortletRequest().getParameter("ajax") != null) {
             PortletComponent comp = page.getActiveComponent(event);
             if (comp != null) {
                 comp.doRender(event);
                 StringBuffer sb = comp.getBufferedOutput(event.getPortletRequest());
                 event.getPortletResponse().setContentType("text/html");
                 event.getPortletResponse().getWriter().print(sb);
             }
         } else {
             page.doRender(event);
         }
 
     }
 
     /**
      * Invoked by the GridSphereServlet to perform portlet login of a users layout
      *
      * @param event the gridsphere event
      * @see org.gridlab.gridsphere.layout.PortletPage#loginPortlets
      */
     public void loginPortlets(GridSphereEvent event) {
         log.debug("in loginPortlets()");
         PortletRequest req = event.getPortletRequest();
         try {
             PortletPage page = getPortletPage(event);
             page.loginPortlets(event);
 
             List pages = pageFactory.getAllCustomPages(req);
             for (int i = 0; i < pages.size(); i++) {
                 PortletPage p = (PortletPage)pages.get(i);
                 p.logoutPortlets(event);
             }
         } catch (Exception e) {
             log.error("Unable to loadUserLayout for user: " + req.getUser().getUserName(), e);
         }
     }
 
     /**
      * Invoked by the GridSphereServlet to perform portlet logout of a users layout
      *
      * @param event the gridsphere event
      * @see PortletPage#logoutPortlets
      */
     public void logoutPortlets(GridSphereEvent event) {
         log.debug("in logoutPortlets()");
         PortletRequest req = event.getPortletRequest();
         try {
             PortletPage page = getPortletPage(event);
             page.logoutPortlets(event);
 
             List pages = pageFactory.getAllCustomPages(req);
             for (int i = 0; i < pages.size(); i++) {
                 PortletPage p = (PortletPage)pages.get(i);
                 p.logoutPortlets(event);
             }
             registry.removeAllPortletFrames(event);
             pageFactory.logStatistics();
         } catch (Exception e) {
             log.error("Unable to logout portlets", e);
         }
     }
 
     public void destroy() {
         pageFactory.destroy();
     }
 
     public void doAction(GridSphereEvent event) {
          String cid = event.getPortletComponentID();
          if (!cid.equals("")) {
              PortletFrame frame = registry.getPortletFrame(cid, null, event);
              if (frame != null)  {
                  try {
                      frame.actionPerformed(event);
                  } catch (Exception e) {
                      e.printStackTrace();
                  }
              }
          }
      }
 
     /**
      * Performs an action on the portlet container referenced by the
      * gridsphere event
      *
      * @param event a gridsphere event
      * @throws IOException if an I/O error occurs during rendering
      */
     public void actionPerformed(GridSphereEvent event) throws IOException {
         log.debug("in actionPerformed()");
         PortletPage page = getPortletPage(event);
         //int numcomps = page.getComponentIdentifierList().size();
         /*
         if (event.getPortletComponentID() < 0 || event.getPortletComponentID() > numcomps) {
         event.getPortletRequest().setAttribute(SportletProperties.COMPONENT_ID, "-1");
         }
         */
         if (!event.getPortletComponentID().equals("")) {
             page.actionPerformed(event);
 
             // sometimes the page needs reinitializing
             if (event.getPortletRequest().getAttribute(SportletProperties.INIT_PAGE) != null) {
                 log.info("\n\n\n\n\nreiniting and saving page!!!!!\n\n\n\n\n\n");
                 page.init(event.getPortletRequest(), new Vector());
                 PortletTabbedPane pane = pageFactory.getUserTabbedPane(event.getPortletRequest());
                 if (pane != null) pane.save();
             }
         }
 
         log.debug("Exiting actionPerformed()");
     }
 
     public void doRenderError(PortletRequest req, PortletResponse res, Throwable t) {
         PrintWriter out = null;
         try {
             out = res.getWriter();
         } catch (IOException e) {
             log.error("in doRenderError: ", e);
         }
         out.println("<html><body>");
         out.println("<b>An error occurred: " + t.getMessage() + "</b>");
         t.printStackTrace(out);
         out.println("</body></html>");
     }
 
     public void doRenderError(GridSphereEvent event, Throwable t) {
         PortletRequest req = event.getPortletRequest();
         PortletPage errorpage = pageFactory.createErrorPage(req);
         req.setAttribute("error", t);
         try {
             errorpage.doRender(event);
         } catch (Exception e) {
             log.error("in doRenderError: ", e);
         }
 
     }
 
     /**
      * Delivers a message to a specified concrete portlet on the current portlet page.
      * The method delegates the message delivery to the PortletPage implementation.
      *
      * @param concPortletID The concrete portlet ID of the target portlet
      * @param msg           The message to deliver
      * @param event         The event associated with the delivery
      */
     public void messageEvent(String concPortletID, PortletMessage msg, GridSphereEvent event) {
         log.debug("in messageEvent()");
         PortletPage page = getPortletPage(event);
         page.messageEvent(concPortletID, msg, event);
         log.debug("Exiting messageEvent()");
 
     }
 
 }
