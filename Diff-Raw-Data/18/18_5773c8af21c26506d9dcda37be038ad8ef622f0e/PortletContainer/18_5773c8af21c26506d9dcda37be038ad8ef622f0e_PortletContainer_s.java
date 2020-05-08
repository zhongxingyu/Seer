 /*
  * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
  * @author <a href="mailto:wehren@aei.mpg.de">Oliver Wehrens</a>
  * @version $Id$
  */
 package org.gridlab.gridsphere.layout;
 
 import org.gridlab.gridsphere.layout.event.PortletFrameEvent;
 import org.gridlab.gridsphere.portlet.PortletRequest;
 import org.gridlab.gridsphere.portlet.impl.SportletProperties;
 import org.gridlab.gridsphere.portletcontainer.GridSphereEvent;
 
 import java.io.IOException;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 
 
 /**
 * The abstract <code>PortletFrameLayout</code> acts a container for the layout of portlet frame
 * components and handles PortletFrame events.
 * <p/>
 * The <code>PortletTableLayout</code> is a concrete implementation of the <code>PortletFrameLayout</code>
 * that organizes portlets into a grid with a provided number of rows and columns.
 *
 * @see PortletFrame
 * @see PortletFrameEvent
  */
 public class PortletContainer extends BasePortletComponent implements
         Serializable, Cloneable {
 
     protected List components = new ArrayList();
    //protected StringBuffer container = new StringBuffer();
 
     /**
      * Initializes the portlet component. Since the components are isolated
      * after Castor unmarshalls from XML, the ordering is determined by a
      * passed in List containing the previous portlet components in the tree.
      *
      * @param list a list of component identifiers
      * @return a list of updated component identifiers
      * @see ComponentIdentifier
      */
     public List init(PortletRequest req, List list) {
         list = super.init(req, list);
         List scomponents = Collections.synchronizedList(components);
         synchronized (scomponents) {
             Iterator it = scomponents.iterator();
             PortletComponent p;
             while (it.hasNext()) {
                 p = (PortletComponent) it.next();
                 // all the components have the same theme
                 p.setTheme(theme);
                 p.setRenderKit(renderKit);
                 // invoke init on each component
                 list = p.init(req, list);
                 p.setParentComponent(this);
             }
         }
         return list;
     }
 
     /**
      * Destroys this portlet component
      */
     public void destroy() {
         components = null;
     }
 
     /**
      * Performs an action on this portlet component
      *
      * @param event a gridsphere event
      */
     public void actionPerformed(GridSphereEvent event) {
         super.actionPerformed(event);
     }
 
     /**
      * Renders the portlet components in the frame layout
      *
      * @param event a gridsphere event
      */
     public void doRender(GridSphereEvent event) {
         super.doRender(event);
         List scomponents = Collections.synchronizedList(components);
         synchronized (scomponents) {
             Iterator it = scomponents.iterator();
             PortletComponent comp;
             while (it.hasNext()) {
                 comp = (PortletComponent) it.next();
                 comp.doRender(event);
                 event.getPortletRequest().setAttribute(SportletProperties.RENDER_OUTPUT + componentIDStr, comp.getBufferedOutput(event.getPortletRequest()));
             }
         }
     }
 
 
     /**
      * Adds a new portlet component to the layout
      *
      * @param component a portlet component
      */
     public void addPortletComponent(PortletComponent component) {
         components.add(component);
     }
 
     /**
      * Removes a new portlet component to the layout
      *
      * @param component a portlet component
      */
     public void removePortletComponent(PortletComponent component) {
         components.remove(component);
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
 
     public Object clone() throws CloneNotSupportedException {
         PortletContainer f = (PortletContainer) super.clone();
         List scomponents = Collections.synchronizedList(components);
         synchronized (scomponents) {
             f.components = new ArrayList(scomponents.size());
             for (int i = 0; i < scomponents.size(); i++) {
                 PortletComponent comp = (PortletComponent) scomponents.get(i);
                 f.components.add(comp.clone());
             }
         }
         return f;
     }
 
 }
 
 
