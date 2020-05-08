 /*
  * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
  * @version $Id$
  */
 package org.gridlab.gridsphere.portlets.examples;
 
 import org.gridlab.gridsphere.event.ActionEvent;
 import org.gridlab.gridsphere.event.WindowEvent;
 import org.gridlab.gridsphere.portlet.*;
 import org.gridlab.gridsphere.portlet.service.PortletServiceUnavailableException;
 import org.gridlab.gridsphere.portlet.service.PortletServiceNotFoundException;
 import org.gridlab.gridsphere.services.core.user.UserManagerService;
 
 import javax.servlet.UnavailableException;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.Enumeration;
 import java.util.List;
 
 public class DemoPortlet extends AbstractPortlet {
 
     public void actionPerformed(ActionEvent evt) throws PortletException {
         DefaultPortletAction action = (DefaultPortletAction)evt.getAction();
         String actionName = action.getName();
     }
 
     public void windowMinimized(WindowEvent evt) {
         System.err.println("in DemoPortlet-- I've been MINIMIZED!");
     }
 
     public void windowMaximized(WindowEvent evt) {
         System.err.println("in DemoPortlet-- I've been MAXIMIZED!");
     }
 
     public void doHelp(PortletRequest request, PortletResponse response) throws PortletException, IOException {
         PrintWriter out = response.getWriter();
         out.println("The DemoPortlet displays information about various Portlet classes");
     }
 
 
     public void doView(PortletRequest request, PortletResponse response) throws PortletException, IOException {
         PrintWriter out = response.getWriter();
 
         out.println("The Demo Portlet displays Portlet API object information:");
 
         String portletName = getPortletConfig().getName();
         out.println("Portlet name: <b>" + portletName + "</b><br>");
         Client client = request.getClient();
         out.println("Client info: <br><ul>");
         out.println("<li>Manufacturer: " + client.getManufacturer() + "</li>");
         out.println("<li>MarkupName: " + client.getMarkupName() + "</li>");
         out.println("<li>MimeType: " + client.getMimeType() + "</li>");
         out.println("<li>Model: " + client.getModel() + "</li>");
         out.println("<li>Version: " + client.getVersion() + "</li>");
         out.println("<li>User-agent: " + client.getUserAgent() + "</li></ul>");
 
         PortletData data = request.getData();
 
 
         PortletSettings settings = request.getPortletSettings();
         PortletApplicationSettings appSettings = settings.getApplicationSettings();
         Enumeration attrEnum = appSettings.getAttributeNames();
         out.println("PortletApplicationSettings: <br><ul>");
         while (attrEnum.hasMoreElements()) {
             String attrName = (String)attrEnum.nextElement();
            String attrValue = settings.getAttribute(attrName);
             out.println("<li>" + "name: " + attrName + " value: " + attrValue + "</li>");
         }
         out.println("</ul>PortletSettings: <br><ul>");
         attrEnum = settings.getAttributeNames();
         while (attrEnum.hasMoreElements()) {
             String attrName = (String)attrEnum.nextElement();
             String attrValue = settings.getAttribute(attrName);
             out.println("<li>" + "name: " + attrName + " value: " + attrValue + "</li>");
         }
         out.println("</ul>");
         String concPortletID = settings.getConcretePortletID();
 
 
         Portlet.Mode mode = request.getMode();
         Portlet.Mode prev = request.getPreviousMode();
 
         List groups = request.getGroups();
         List roles = request.getRoles(null);
 
         PortletWindow window = request.getWindow();
 
         User user = request.getUser();
 
         PortletContext ctx = portletConfig.getContext();
         ctx.getContainerInfo();
         int majVer = ctx.getMajorVersion();
         int minVer = ctx.getMinorVersion();
 
 
         boolean supports = portletConfig.supports(PortletWindow.State.MINIMIZED);
         boolean s = portletConfig.supports(Portlet.Mode.EDIT);
 
         String value = portletSettings.getApplicationSettings().getAttribute("foobar");
         out.println(value);
         //getPortletConfig().getContext().include("/jsp/snoop/view.jsp", request, response);
     }
 
     public void doEdit(PortletRequest request, PortletResponse response) throws PortletException, IOException {
         getPortletConfig().getContext().include("/jsp/snoop/edit.jsp", request, response);
     }
 
     public void doConfigure(PortletRequest request, PortletResponse response) throws PortletException, IOException {
         getPortletConfig().getContext().include("/jsp/snoop/configure.jsp", request, response);
     }
 
 }
