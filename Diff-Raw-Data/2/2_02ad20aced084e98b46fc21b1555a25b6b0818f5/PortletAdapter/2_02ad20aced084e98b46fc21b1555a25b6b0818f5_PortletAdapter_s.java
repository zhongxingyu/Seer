 /*
  * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
  * @version $Id$
  */
 package org.gridlab.gridsphere.portlet;
 
 import org.gridlab.gridsphere.core.persistence.PersistenceManagerException;
 import org.gridlab.gridsphere.portlet.impl.SportletProperties;
 import org.gridlab.gridsphere.portletcontainer.PortletDataManager;
 import org.gridlab.gridsphere.portletcontainer.impl.SportletDataManager;
 
 import javax.servlet.UnavailableException;
 import java.io.IOException;
 import java.util.Hashtable;
 import java.util.Map;
 
 /**
  * The <code>PortletAdapter</code> provides a default implementation for the
  * <code>Portlet</code> interfaces as well as providing a default handler
  * for the <code>PortletSessionListener</code> to allow <it>user portlet
  * instances</it> to be created and removed via the {@link #login} and
  * {@link #logout} methods.
  */
 public abstract class PortletAdapter extends Portlet {
 
     protected Hashtable storeVars = new Hashtable();
 
     /* keep track of all PortletSettings per concrete portlet (concreteID, PortletSettings) */
     private Map allPortletSettings = new Hashtable();
 
     /* the datamanger injects PortletData into the request */
     private transient PortletDataManager dataManager = null;
 
     public PortletAdapter() {
     }
 
     /**
      * Called by the portlet container to indicate to this portlet that it is put into service.
      * <p/>
      * The portlet container calls the init() method for the whole life-cycle of the portlet.
      * The init() method must complete successfully before concrete portlets are created through
      * the initConcrete() method.
      * <p/>
      * The portlet container cannot place the portlet into service if the init() method
      * <p/>
      * 1. throws UnavailableException
      * 2. does not return within a time period defined by the portlet container.
      *
      * @param config the portlet configuration
      * @throws UnavailableException if an exception has occurrred that interferes with the portlet's
      *                              normal initialization
      */
     public void init(PortletConfig config) throws UnavailableException {
         this.portletConfig = config;
         dataManager = SportletDataManager.getInstance();
     }
 
     /**
      * Called by the portlet container to indicate to this portlet that it is taken out of service.
      * This method is only called once all threads within the portlet's service() method have exited
      * or after a timeout period has passed. After the portlet container calls this method,
      * it will not call the service() method again on this portlet.
      * <p/>
      * This method gives the portlet an opportunity to clean up any resources that are
      * being held (for example, memory, file handles, threads).
      *
      * @param config the portlet configuration
      */
     public void destroy(PortletConfig config) {
         this.portletConfig = null;
     }
 
     /**
      * Called by the portlet container to indicate that the concrete portlet is put into service.
      * The portlet container calls the initConcrete() method for the whole life-cycle of the portlet.
      * The initConcrete() method must complete successfully before concrete portlet instances can be
      * created through the login() method.
      * <p/>
      * The portlet container cannot place the portlet into service if the initConcrete() method
      * <p/>
      * 1. throws UnavailableException
      * 2. does not return within a time period defined by the portlet container.
      *
      * @param settings the portlet settings
      */
     public void initConcrete(PortletSettings settings) throws UnavailableException {
         allPortletSettings.put(settings.getConcretePortletID(), settings);
     }
 
     /**
      * Called by the portlet container to indicate that the concrete portlet is taken out of service.
      * This method is only called once all threads within the portlet's service() method have exited
      * or after a timeout period has passed. After the portlet container calls this method,
      * it will not call the service() method again on this portlet.
      * <p/>
      * This method gives the portlet an opportunity to clean up any resources that are being
      * held (for example, memory, file handles, threads).
      *
      * @param settings the portlet settings
      */
     public void destroyConcrete(PortletSettings settings) {
         allPortletSettings.remove(settings.getConcretePortletID());
     }
 
     /**
      * Called by the portlet container to ask this portlet to generate its markup using the given
      * request/response pair. Depending on the mode of the portlet and the requesting client device,
      * the markup will be different. Also, the portlet can take language preferences and/or
      * personalized settings into account.
      *
      * @param request  the portlet request
      * @param response the portlet response
      * @throws PortletException if the portlet has trouble fulfilling the rendering request
      * @throws IOException      if the streaming causes an I/O problem
      */
     public void service(PortletRequest request, PortletResponse response) throws PortletException, IOException {
         // There must be a portlet ID to know which portlet to service
         String portletID = (String) request.getAttribute(SportletProperties.PORTLETID);
         if (portletID == null) {
             // it may be in the request parameter
             portletID = request.getParameter(SportletProperties.PORTLETID);
             if (portletID == null) {
                 log.error("in PortletAdapter: No PortletID found in request attribute");
                 return;
             }
         }
 
         PortletData data = null;
         User user = request.getUser();
         if (!(user instanceof GuestUser)) {
             try {
                 data = dataManager.getPortletData(user, portletID);
                 request.setAttribute(SportletProperties.PORTLET_DATA, data);
             } catch (PersistenceManagerException e) {
                log.error("in PortletAdapter: Unable to obtain PortletData for user");
             }
         }
 
         portletSettings = (PortletSettings) allPortletSettings.get(portletID);
         if (portletSettings != null) {
             request.setAttribute(SportletProperties.PORTLET_SETTINGS, portletSettings);
         }
 
 
         user = request.getUser();
 
         String method = (String) request.getAttribute(SportletProperties.PORTLET_ACTION_METHOD);
         if (method != null) return;
 
         Portlet.Mode mode = request.getMode();
         if (mode == null) {
             mode = Portlet.Mode.VIEW;
             request.setMode(mode);
         }
         log.debug("in PortletAdapter: Displaying mode: " + mode + " for portlet: " + portletID);
         try {
 
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
                     throw new IllegalArgumentException("Received invalid PortletMode command: " + mode);
             }
         } catch (Exception e) {
             log.error("in PortletAdapter: service()", e);
             request.setAttribute(SportletProperties.PORTLETERROR + getPortletSettings().getConcretePortletID(), e);
             throw new PortletException(e);
         }
     }
 
     /**
      * Called by the portlet container to ask the portlet to initialize a personalized user experience.
      * In addition to initializing the session this method allows the portlet to initialize the
      * concrete portlet instance, for example, to store attributes in the session.
      *
      * @param request the portlet request
      */
     public void login(PortletRequest request) {
     }
 
 
     /**
      * Called by the portlet container to indicate that a concrete portlet instance is being removed.
      * This method gives the concrete portlet instance an opportunity to clean up any resources
      * (for example, memory, file handles, threads), before it is removed.
      * This happens if the user logs out, or decides to remove this portlet from a page.
      *
      * @param session the portlet session
      */
     public void logout(PortletSession session) {
     }
 
     /**
      * Returns the time the response of the PortletInfo  object was last modified, in milliseconds since midnight
      * January 1, 1970 GMT. If the time is unknown, this method returns a negative number (the default).
      * <p/>
      * Portlets that can quickly determine their last modification time should override this method.
      * This makes browser and proxy caches work more effectively, reducing the load on server and network resources.
      *
      * @param request the portlet request
      * @return long a long integer specifying the time the response of the PortletInfo
      *         object was last modified, in milliseconds since midnight, January 1, 1970 GMT, or -1 if the time is not known
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
      *
      * @return the portlet log
      */
     public PortletLog getPortletLog() {
         return log;
     }
 
     /**
      * Helper method to serve up the CONFIGURE mode.
      *
      * @param request  the portlet request
      * @param response the portlet response
      * @throws PortletException if an error occurs during processing
      * @throws IOException      if an I/O error occurs
      */
     public void doConfigure(PortletRequest request, PortletResponse response)
             throws PortletException, IOException {
         // default is doView
         doView(request, response);
     }
 
     /**
      * Helper method to serve up the EDIT mode.
      *
      * @param request  the portlet request
      * @param response the portlet response
      * @throws PortletException if an error occurs during processing
      * @throws IOException      if an I/O error occurs
      */
     public void doEdit(PortletRequest request, PortletResponse response)
             throws PortletException, IOException {
         // default is doView
         doView(request, response);
     }
 
     /**
      * Helper method to serve up the HELP mode.
      *
      * @param request  the portlet request
      * @param response the portlet response
      * @throws PortletException if an error occurs during processing
      * @throws IOException      if an I/O error occurs
      */
     public void doHelp(PortletRequest request, PortletResponse response)
             throws PortletException, IOException {
         // default doView
         doView(request, response);
     }
 
     /**
      * Helper method to serve up the VIEW mode.
      *
      * @param request  the portlet request
      * @param response the portlet response
      * @throws PortletException if an error occurs during processing
      * @throws IOException      if an I/O error occurs
      */
     public void doView(PortletRequest request, PortletResponse response)
             throws PortletException, IOException {
         throw new PortletException("doView method not implemented!");
     }
 
     /**
      * Returns a transient variable of the concrete portlet.
      *
      * @param name the variable name
      * @return the variable or null if it doesn't exist
      * @throws AccessDeniedException if the method is called outside of a concrete portlet
      */
     public Object getVariable(String name) throws AccessDeniedException {
         return storeVars.get(name);
     }
 
     /**
      * Removes a transient variable of the concrete portlet.
      *
      * @param name the variable name
      */
     public void removeVariable(String name) {
         if (storeVars.containsKey(name)) {
             storeVars.remove(name);
         }
     }
 
     /**
      * Sets a transient variable of the concrete portlet.
      *
      * @param name  the variable name
      * @param value the variable value
      */
     public void setVariable(String name, Object value) {
         if ((name != null) && (value != null))
             storeVars.put(name, value);
     }
 
 }
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
