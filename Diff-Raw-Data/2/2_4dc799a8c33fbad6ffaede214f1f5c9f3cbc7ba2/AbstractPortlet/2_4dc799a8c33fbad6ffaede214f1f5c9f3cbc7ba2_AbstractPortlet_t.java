 /*
  * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
  * @version $Id$
  */
 package org.gridlab.gridsphere.portlet;
 
 import org.gridlab.gridsphere.event.*;
 import org.gridlab.gridsphere.event.impl.ActionEventImpl;
 import org.gridlab.gridsphere.event.impl.MessageEventImpl;
 import org.gridlab.gridsphere.portlet.impl.SportletProperties;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 
 
 /**
  * The <code>AbstractPortlet</code> provides default implementations of the
  * portlet event listeners and the <code>PortletTitleListener</code> to allow
  * portlets to receive portlet events and generate title bar markup.
  */
 public class AbstractPortlet extends PortletAdapter implements ActionListener, MessageListener, WindowListener, PortletTitleListener {
 
     /**
      * Constructs an instance of AbstractPortlet
      */
     public AbstractPortlet() {
         super();
     }
 
     /**
      * Returns the portlet configuration.
      *
      * @return the portlet config
      */
     public PortletConfig getConfig() {
         return super.getPortletConfig();
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
     public void service(PortletRequest request, PortletResponse response)
             throws PortletException, IOException {
         super.service(request, response);
         String method = (String) request.getAttribute(SportletProperties.PORTLET_ACTION_METHOD);
         if (method != null) {
             try {
                 log.info("Received ACTION_METHOD: " + method);
                 if (method.equals(SportletProperties.DO_TITLE)) {
                     doTitle(request, response);
                 } else if (method.equals(SportletProperties.WINDOW_EVENT)) {
                     WindowEvent winEvent = (WindowEvent) request.getAttribute(SportletProperties.WINDOW_EVENT);
                     switch (winEvent.getEventId()) {
                         case WindowEvent.WINDOW_MAXIMIZED:
                             windowMaximized(winEvent);
                             break;
                         case WindowEvent.WINDOW_MINIMIZED:
                             windowMinimized(winEvent);
                             break;
                         case WindowEvent.WINDOW_RESTORED:
                             windowRestored(winEvent);
                             break;
                         default:
                             doError(request, response, "Received invalid WindowEvent : " + winEvent.getEventId());
                             log.error("Received invalid WindowEvent : " + winEvent.getEventId());
                     }
                 } else if (method.equals(SportletProperties.ACTION_PERFORMED)) {
                     // Set the appropriate portlet action
                     DefaultPortletAction action = (DefaultPortletAction) request.getAttribute(SportletProperties.ACTION_EVENT);
                     ActionEvent evt = new ActionEventImpl(action, request, response);
                     actionPerformed(evt);
                 } else if (method.equals(SportletProperties.MESSAGE_RECEIVED)) {
                     // Set the appropriate portlet message
                     DefaultPortletMessage msg = (DefaultPortletMessage) request.getAttribute(SportletProperties.MESSAGE_EVENT);
                     MessageEvent evt = new MessageEventImpl(request, msg);
                     messageReceived(evt);
                 }
             } catch (Exception e) {
                 log.error("in PortletAdapter: service()", e);
                request.setAttribute(SportletProperties.PORTLETERROR + getPortletSettings().getConcretePortletID(), "Error performing method:" + method + e.getMessage());
 
                 //doError(request, response, e);
             }
         }
         request.removeAttribute(SportletProperties.PORTLET_ACTION_METHOD);
     }
 
     /**
      * Called by the portlet container to ask this portlet to perform the required operational logic
      * using the given portlet request.
      * Notifies this listener that the action which the listener is watching for has been performed.
      *
      * @param event the action event
      * @throws PortletException if the listener has trouble fulfilling the request
      */
     public void actionPerformed(ActionEvent event) throws PortletException {
         log.debug("Calling actionPerformed()");
     }
 
     /**
      * Notifies this listener that the message which the listener is watching for has been performed.
      *
      * @param event the message event
      *
      * @throws PortletException if the listener has trouble fulfilling the request
      */
     public void messageReceived(MessageEvent event) throws PortletException {
     }
 
     /**
      * Called by the portlet container to render the portlet title.
      * The information in the portlet request (like locale, client, and session information) can
      * but doesn't have to be considered to render dynamic titles.. Examples are
      *
      * language-dependant titles for multi-lingual portals
      * shorter titles for WAP phones
      * the number of messages in a mailbox portlet
      * The session may be null, if the user is not logged in.
      *
      * @param request the portlet request
      * @param response the portlet response
      *
      * @throws PortletException if the portlet title has trouble fulfilling the rendering request
      * @throws IOException if the streaming causes an I/O problem
      */
     public void doTitle(PortletRequest request, PortletResponse response) throws PortletException, IOException {
         PortletSettings settings = request.getPortletSettings();
         String title = settings.getTitle(request.getLocale(), request.getClient());
         PrintWriter out = response.getWriter();
         out.println(title);
     }
 
     /**
      * Notifies this listener that a portlet window has been detached.
      *
      * @param event the window event
      */
     public void windowDetached(WindowEvent event) throws PortletException {
     }
 
     /**
      * Notifies this listener that a portlet window has been maximized.
      *
      * @param event the window event
      */
     public void windowMaximized(WindowEvent event) throws PortletException {
     }
 
     /**
      * Notifies this listener that a portlet window has been minimized.
      *
      * @param event the window event
      */
     public void windowMinimized(WindowEvent event) throws PortletException {
     }
 
     /**
      * Notifies this listener that a portlet window is about to be closed.
      *
      * @param event the window event
      */
     public void windowClosing(WindowEvent event) throws PortletException {
     }
 
     /**
      * Notifies this listener that a portlet window has been closed.
      *
      * @param event the window event
      */
     public void windowClosed(WindowEvent event) throws PortletException {
     }
 
     /**
      * Notifies this listener that a portlet window has been restored from being minimized or maximized, respectively.
      *
      * @param event the window event
      */
     public void windowRestored(WindowEvent event) throws PortletException {
     }
 
 }
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
