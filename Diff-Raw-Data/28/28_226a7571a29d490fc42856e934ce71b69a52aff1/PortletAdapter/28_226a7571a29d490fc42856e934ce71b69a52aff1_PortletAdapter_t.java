 /*
  * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
  * @version $Id$
  */
 package org.gridlab.gridsphere.portlet;
 
 import org.gridlab.gridsphere.portlet.impl.SportletLog;
 import org.gridlab.gridsphere.portlet.impl.PortletProperties;
 import org.gridlab.gridsphere.portlet.impl.SportletSettings;
 import org.gridlab.gridsphere.portlet.impl.SportletSession;
 import org.gridlab.gridsphere.portletcontainer.GridSphereProperties;
 
 import javax.servlet.UnavailableException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
import javax.servlet.jsp.JspException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSessionEvent;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.Vector;
 import java.util.Map;
 
 /**
  * The PortletAdapter provides a default implementation for the PortletInfo interface.
  * It is recommended not to extend the portlet interface directly.
  * Rather, a portlet should derive from this or any other derived class, because changes in
  * the PortletInfo interface are then mostly likely to be catched by the default implementation,
  * rather than breaking your portlet implementation.
  *
  * The virtual instance is created and destroyed with the login() and logout() methods, respectively.
  * If a portlet provides personalized views these methods should be implemented.
  */
 public abstract class PortletAdapter extends Portlet implements PortletSessionListener {
 
     protected Hashtable storeVars = new Hashtable();
 
     private static PortletLog log = org.gridlab.gridsphere.portlet.impl.SportletLog.getInstance(PortletAdapter.class);
 
     /* keep track of all PortletSettings per concrete portlet (concreteID, PortletSettings) */
     private Map allPortletSettings = new Hashtable();
 
     public PortletAdapter() {
         log.info("in PortletAdaptor constructor");
     }
 
     /**
      * Called by the portlet container to indicate to this portlet that it is put into service.
      *
      * The portlet container calls the init() method for the whole life-cycle of the portlet.
      * The init() method must complete successfully before concrete portlets are created through
      * the initConcrete() method.
      *
      * The portlet container cannot place the portlet into service if the init() method
      *
      * 1. throws UnavailableException
      * 2. does not return within a time period defined by the portlet container.
      *
      * @param config the portlet configuration
      * @throws UnavailableException if an exception has occurrred that interferes with the portlet's
      * normal initialization
      */
     public void init(PortletConfig config) throws UnavailableException {
         log.info("in init(PortletConfig)");
         this.portletConfig = config;
     }
 
     /**
      * Called by the portlet container to indicate to this portlet that it is taken out of service.
      * This method is only called once all threads within the portlet's service() method have exited
      * or after a timeout period has passed. After the portlet container calls this method,
      * it will not call the service() method again on this portlet.
      *
      * This method gives the portlet an opportunity to clean up any resources that are
      * being held (for example, memory, file handles, threads).
      *
      * @param config the portlet configuration
      */
     public void destroy(PortletConfig config) {
         log.info("in destroy(PortletConfig)");
         this.portletConfig = null;
     }
 
     /**
      * Called by the portlet container to indicate that the concrete portlet is put into service.
      * The portlet container calls the initConcrete() method for the whole life-cycle of the portlet.
      * The initConcrete() method must complete successfully before concrete portlet instances can be
      * created through the login() method.
      *
      * The portlet container cannot place the portlet into service if the initConcrete() method
      *
      * 1. throws UnavailableException
      * 2. does not return within a time period defined by the portlet container.
      *
      * @param settings the portlet settings
      */
     public void initConcrete(PortletSettings settings) throws UnavailableException {
         log.info("in initConcrete(PortletSettings)");
         allPortletSettings.put(settings.getConcretePortletID(), settings);
     }
 
     /**
      * Called by the portlet container to indicate that the concrete portlet is taken out of service.
      * This method is only called once all threads within the portlet's service() method have exited
      * or after a timeout period has passed. After the portlet container calls this method,
      * it will not call the service() method again on this portlet.
      *
      * This method gives the portlet an opportunity to clean up any resources that are being
      * held (for example, memory, file handles, threads).
      *
      * @param settings the portlet settings
      */
     public void destroyConcrete(PortletSettings settings) {
         log.info("in destroyConcrete(PortletSettings)");
         allPortletSettings.remove(settings.getConcretePortletID());
     }
 
     /**
      * Called by the portlet container to ask this portlet to generate its markup using the given
      * request/response pair. Depending on the mode of the portlet and the requesting client device,
      * the markup will be different. Also, the portlet can take language preferences and/or
      * personalized settings into account.
      *
      * @param request the portlet request
      * @param response the portlet response
      *
      * @throws PortletException if the portlet has trouble fulfilling the rendering request
      * @throws IOException if the streaming causes an I/O problem
      */
     public void service(PortletRequest request, PortletResponse response) throws PortletException, IOException {
         log.info("in PortletAdapter: service(PortletRequest, PortletResponse)");
 
         // set the proper PortletSettings
         String portletID = (String)request.getAttribute(GridSphereProperties.PORTLETID);
         if (portletID == null) {
             portletID = (String)request.getParameter(GridSphereProperties.PORTLETID);
         }
 
         portletSettings = (PortletSettings)allPortletSettings.get(portletID);
         request.setAttribute(GridSphereProperties.PORTLETSETTINGS, portletSettings);
 
         String method = (String)request.getAttribute(PortletProperties.PORTLET_ACTION_METHOD);
         if (method != null) return;
 
         Portlet.Mode mode = request.getMode();
        if (mode == null) mode = Portlet.Mode.VIEW;
 
         if (mode != null) {
             switch (mode.getMode()) {
                 case Portlet.Mode.VIEW_MODE:
                     doView(request, response);
                     break;
                 case Portlet.Mode.EDIT_MODE:
                     doEdit(request, response);
                     break;
                 case Portlet.Mode.CONFIGURE_MODE:
                     doConfigure(request, response);
                     break;
                 case Portlet.Mode.HELP_MODE:
                     doHelp(request, response);
                     break;
                 default:
                     log.error("Received invalid PortletMode command : " + mode);
                     throw new PortletException("Received invalid PortletMode command: " + mode);
             }
            //JspException jspException = (JspException)request.getAttribute("javax.servlet.jsp.jspException");

         } else {
             log.error("Received NULL PortletMode command");
             throw new PortletException("Received NULL PortletMode command");
         }
     }
 
     /**
      * Description copied from interface: PortletSessionListener
      * Called by the portlet container to ask the portlet to initialize a personalized user experience.
      * In addition to initializing the session this method allows the portlet to initialize the
      * concrete portlet instance, for example, to store attributes in the session.
      *
      * @param request the portlet request
      */
     public void login(PortletRequest request) {
         // XXX: FILL ME IN
     }
 
     /**
      * Description copied from interface: PortletSessionListener
      * Called by the portlet container to indicate that a concrete portlet instance is being removed.
      * This method gives the concrete portlet instance an opportunity to clean up any resources
      * (for example, memory, file handles, threads), before it is removed.
      * This happens if the user logs out, or decides to remove this portlet from a page.
      *
      * @param session the portlet session
      */
     public void logout(PortletSession session) {
         // XXX: FILL ME IN
 
     }
 
     /**
      * Returns the time the response of the PortletInfo  object was last modified, in milliseconds since midnight
      * January 1, 1970 GMT. If the time is unknown, this method returns a negative number (the default).
      *
      * Portlets that can quickly determine their last modification time should override this method.
      * This makes browser and proxy caches work more effectively, reducing the load on server and network resources.
      *
      * @param request the portlet request
      * @return long a long integer specifying the time the response of the PortletInfo
      * object was last modified, in milliseconds since midnight, January 1, 1970 GMT, or -1 if the time is not known
      */
     public long getLastModified(PortletRequest request) {
         // XXX: FILL ME IN
         return 0;
     }
 
     /**
      * Returns the PortletConfig object of the portlet
      *
      * @return the PortletConfig object
      */
     public PortletConfig getPortletConfig() {
         return portletConfig;
     }
 
     /**
      * Returns the portlet log
      */
     public PortletLog getPortletLog() {
         return log;
     }
 
     /**
      * Helper method to serve up the CONFIGURE mode.
      *
      * @param request the portlet request
      * @param response the portlet response
      *
      * @throws PortletException if an error occurs during processing
      * @throws IOException if an I/O error occurs
      */
     protected void doErrorMessage(PortletRequest request, PortletResponse response, String message)
             throws PortletException, IOException {
         PrintWriter out = response.getWriter();
         out.println("<b>" + message + "</b>");
     }
 
     /**
      * Helper method to serve up the CONFIGURE mode.
      *
      * @param request the portlet request
      * @param response the portlet response
      *
      * @throws PortletException if an error occurs during processing
      * @throws IOException if an I/O error occurs
      */
     public void doConfigure(PortletRequest request, PortletResponse response)
             throws PortletException, IOException {
         // default is doView
         doView(request, response);
     }
 
     /**
      * Helper method to serve up the EDIT mode.
      *
      * @param request the portlet request
      * @param response the portlet response
      *
      * @throws PortletException if an error occurs during processing
      * @throws IOException if an I/O error occurs
      */
     public void doEdit(PortletRequest request, PortletResponse response)
             throws PortletException, IOException {
         // default is doView
         doView(request, response);
     }
 
     /**
      * Helper method to serve up the HELP mode.
      *
      * @param request the portlet request
      * @param response the portlet response
      *
      * @throws PortletException if an error occurs during processing
      * @throws IOException if an I/O error occurs
      */
     public void doHelp(PortletRequest request, PortletResponse response)
             throws PortletException, IOException {
         // default is doView()
         doView(request, response);
     }
 
     /**
      * Helper method to serve up the VIEW mode.
      *
      * @param request the portlet request
      * @param response the portlet response
      *
      * @throws PortletException if an error occurs during processing
      * @throws IOException if an I/O error occurs
      */
     public void doView(PortletRequest request, PortletResponse response)
             throws PortletException, IOException {
         PrintWriter out = response.getWriter();
         out.println("<center>Blank Portlet</center>");
     }
 
     /**
      * Returns a transient variable of the concrete portlet.
      *
      * @param name the variable name
      * @return the variable or null if it doesn't exist
      *
      * @throws AccessDeniedException if the method is called outside of a concrete portlet
      */
     public Object getVariable(String name) throws AccessDeniedException {
         return storeVars.get(name);
     }
 
     /**
      * Removes a transient variable of the concrete portlet.
      *
      * @param name the variable name
      *
      * @throws AccessDeniedException if the method is called outside of a concrete portlet
      */
     public void removeVariable(String name) throws AccessDeniedException {
         if (storeVars.containsKey(name)) {
             storeVars.remove(name);
         }
     }
 
     /**
      * Sets a transient variable of the concrete portlet.
      *
      * @param name the variable name
      * @param the variable value
      *
      * @throws AccessDeniedException if the method is called outside of a concrete portlet
      */
     public void setVariable(String name, Object value) throws AccessDeniedException {
         if ((name != null) && (value != null))
             storeVars.put(name, value);
     }
 
 }
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
