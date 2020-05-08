 /*
  * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
  * @version $Id$
  */
 package org.gridlab.gridsphere.layout;
 
 import org.gridlab.gridsphere.portlet.PortletException;
 import org.gridlab.gridsphere.portlet.PortletRequest;
 import org.gridlab.gridsphere.portlet.PortletResponse;
 import org.gridlab.gridsphere.portletcontainer.GridSphereEvent;
 import org.gridlab.gridsphere.portletcontainer.PortletInvoker;
 import org.gridlab.gridsphere.portletcontainer.GridSphereConfig;
 import org.gridlab.gridsphere.portletcontainer.GridSphereConfigProperties;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 /**
  * The <code>PortletContainer</code> is the generic container for a collection of
  * concrete portlet components and provides lifecycle methods for traversing
  * the tree of components and handling actions and performing rendering.
  */
 public class PortletContainer implements Cloneable {
 
     protected int COMPONENT_ID = 0;
 
     // The actual portlet layout components
     protected List components = new ArrayList();
 
     // The component ID's of each of the layout components
     protected List componentIdentifiers = new ArrayList();
 
     // The list of portlets a user has-- generally contained within a PortletFrame/PortletTitleBar combo
     protected List portlets = new ArrayList();
 
     protected String name = "";
     protected String theme = GridSphereConfig.getProperty(GridSphereConfigProperties.DEFAULT_THEME);
 
     /**
      * Constructs an instance of PortletContainer
      */
     public PortletContainer() {
     }
 
     /**
      * Sets the portlet container name
      *
      * @param name the portlet container name
      */
     public void setContainerName(String name) {
         this.name = name;
     }
 
     /**
      * Returns the portlet container name
      *
      * @return the portlet container name
      */
     public String getContainerName() {
         return name;
     }
 
     /**
      * Sets the theme of this portlet component
      *
      * @param theme the theme of this portlet component
      */
     public void setTheme(String theme) {
         this.theme = theme;
     }
 
     /**
      * Return the theme of this portlet component
      *
      * @return the theme of this portlet component
      */
     public String getTheme() {
         return theme;
     }
 
     /**
      * Sets the list of new portlet component to the layout
      *
      * @param components an ArrayList of portlet components
      */
     public void setPortletComponents(ArrayList components) {
         this.components = components;
     }
 
     /**
      * Returns a list containing the portlet components in this layout
      *
      * @return a list of portlet components
      */
     public List getPortletComponents() {
         return components;
     }
 
     /**
      * Returns the list of portlet component identifiers
      *
      * @return the list of portlet component identifiers
      * @see ComponentIdentifier
      */
     public List getComponentIdentifierList() {
         return componentIdentifiers;
     }
 
     /**
      * Sets the list of portlet component identifiers
      *
      * @param componentIdentifiers a list of portlet component identifiers
      * @see ComponentIdentifier
      */
     public void setComponentIdentifierList(List componentIdentifiers) {
         this.componentIdentifiers = componentIdentifiers;
     }
 
     /**
      * Returns the associated portlet component id
      *
      * @return the portlet component id
      */
     public int getComponentID() {
         return COMPONENT_ID;
     }
 
     /**
      * Initializes the portlet component. Since the components are isolated
      * after Castor unmarshalls from XML, the ordering is determined by a
      * passed in List containing the previous portlet components in the tree.
      *
      * @param list a list of component identifiers
      * @return a list of updated component identifiers
      * @see ComponentIdentifier
      */
     public List init(List list) {
         Iterator it = components.iterator();
         PortletComponent comp;
         while (it.hasNext()) {
             comp = (PortletComponent) it.next();
             comp.setTheme(theme);
             list = comp.init(list);
         }
         System.err.println("Made a components list!!!! " + list.size());
         for (int i = 0; i < list.size(); i++) {
             ComponentIdentifier c = (ComponentIdentifier) list.get(i);
             System.err.println("id: " + c.getComponentID() + " : " + c.getClassName() + " : " + c.hasPortlet());
 
         }
         componentIdentifiers = list;
         return componentIdentifiers;
     }
 
     /**
      * Performs {@link org.gridlab.gridsphere.portlet.Portlet#login(PortletRequest) login}
      * on all the portlets conatined by this PortletContainer
      *
      * @param event a gridsphere event
      * @throws PortletException if an error occurs while invoking login on the portlets
      * @see <a href="org.gridlab.gridsphere.portlet.Portlet#login">Portlet.login(PortletRequest)</a>
      */
     public void loginPortlets(GridSphereEvent event) throws PortletException {
         Iterator it = componentIdentifiers.iterator();
         ComponentIdentifier cid = null;
         PortletFrame f = null;
         while (it.hasNext()) {
             cid = (ComponentIdentifier) it.next();
             System.err.println(cid.getClassName());
             if (cid.getClassName().equals("org.gridlab.gridsphere.layout.PortletFrame")) {
                 f = (PortletFrame) cid.getPortletComponent();
                 portlets.add(f.getPortletClass());
             }
         }
     }
 
     /**
      * Performs {@link org.gridlab.gridsphere.portlet.Portlet#logout}
      * on all the portlets conatined by this PortletContainer
      *
      * @param event a gridsphere event
      * @throws PortletException if an error occurs while invoking login on the portlets
      * @see <a href="org.gridlab.gridsphere.portlet.Portlet#logout">Portlet.logout(PortletSession)</a>
      */
     public void logoutPortlets(GridSphereEvent event) throws IOException, PortletException {
         Iterator it = componentIdentifiers.iterator();
         ComponentIdentifier cid = null;
         PortletFrame f = null;
         PortletRequest req = event.getPortletRequest();
         PortletResponse res = event.getPortletResponse();
         while (it.hasNext()) {
             cid = (ComponentIdentifier) it.next();
             if (cid.getPortletClass().equals("org.gridlab.gridsphere.layout.PortletFrame")) {
                 f = (PortletFrame) cid.getPortletComponent();
                 PortletInvoker.logout(f.getPortletClass(), req, res);
             }
         }
     }
 
     /**
      * Destroys this portlet container
      */
     public void destroy() {
         Iterator it = components.iterator();
         while (it.hasNext()) {
             PortletComponent comp = (PortletComponent) it.next();
             comp.destroy();
         }
     }
 
     /**
      * Performs an action by performing an action on the appropriate portlet component
      * contained by this PortletContainer
      *
      * @param event a gridsphere event
      * @throws PortletLayoutException if a layout error occurs during rendering
      * @throws IOException if an I/O error occurs during rendering
      */
     public void actionPerformed(GridSphereEvent event) throws PortletLayoutException, IOException {
         // if there is a layout action do it!
         if (event.getPortletComponentID() != -1) {
             // the component id determines where in the list the portlet component is
             ComponentIdentifier compId = (ComponentIdentifier) componentIdentifiers.get(event.getPortletComponentID());
             PortletComponent comp = compId.getPortletComponent();
             // perform an action if the component is non null
             if (comp != null) {
                 System.err.println("performing an event in :"+ compId.getClassName() + "  " + compId.getComponentID());
                 comp.actionPerformed(event);
             }
         }
     }
 
     /**
      * Renders the portlet cotainer by performing doRender on all portlet components
      *
      * @param event a gridsphere event
      * @throws PortletLayoutException if a layout error occurs during rendering
      * @throws IOException if an I/O error occurs during rendering
      */
     public void doRender(GridSphereEvent event) throws PortletLayoutException, IOException {
         PortletResponse res = event.getPortletResponse();
         PrintWriter out = res.getWriter();
 
         out.println("<html>");
         out.println("<head>");
         out.println("<meta http-equiv=\"content-type\" content=\"text/html; charset=iso-8859-1\">");
         out.println("  <title>" + name + "</title>");
         out.println("  <link type=\"text/css\" href=\"themes/" + theme + "/css" +
                 "/default.css\" rel=\"STYLESHEET\"/>");
         out.println("<script language=\"JavaScript\" src=\"javascript/gridsphere.js\"></script>");
         out.println("</head>\n<body>");
 
         Iterator it = components.iterator();
         while (it.hasNext()) {
             PortletComponent comp = (PortletComponent) it.next();
             comp.doRender(event);
         }
         out.println("</body></html>");
     }
 
 
     public Object clone() throws CloneNotSupportedException {
         int i;
         PortletContainer c = (PortletContainer)super.clone();	// clone the stack
         c.COMPONENT_ID = this.COMPONENT_ID;
         c.theme = this.theme;
         c.componentIdentifiers = new ArrayList(this.componentIdentifiers.size());
         for (i = 0; i < this.componentIdentifiers.size(); i++) {
             ComponentIdentifier cid = (ComponentIdentifier)this.componentIdentifiers.get(i);
             c.componentIdentifiers.add(cid.clone());
         }
 
         c.name = this.name;
         c.portlets = new ArrayList(this.portlets.size());
         for (i = 0; i < this.portlets.size(); i++) {
             String pclass = (String)this.portlets.get(i);
             c.portlets.add(pclass);
         }
         c.components = new ArrayList(this.components.size());
         for (i = 0; i < this.components.size(); i++) {
             PortletComponent comp = (PortletComponent)this.components.get(i);
             PortletComponent newcomp = (PortletComponent)comp.clone();
             c.components.add(newcomp);
         }
         return c;
     }
 }
