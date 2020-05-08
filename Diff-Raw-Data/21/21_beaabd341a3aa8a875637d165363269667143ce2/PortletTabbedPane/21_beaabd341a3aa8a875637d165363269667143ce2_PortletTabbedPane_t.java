 /*
 * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
 * @author <a href="mailto:wehrens@aei.mpg.de">Oliver Wehrens</a>
 * @version $Id$
 */
 
 package org.gridlab.gridsphere.layout;
 
 import org.gridlab.gridsphere.layout.event.PortletComponentEvent;
 import org.gridlab.gridsphere.layout.event.PortletTabEvent;
 import org.gridlab.gridsphere.layout.event.PortletTabListener;
 import org.gridlab.gridsphere.portlet.PortletRequest;
 import org.gridlab.gridsphere.portlet.PortletResponse;
 import org.gridlab.gridsphere.portlet.PortletRole;
 import org.gridlab.gridsphere.portlet.User;
 import org.gridlab.gridsphere.portletcontainer.GridSphereEvent;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.Serializable;
 import java.util.*;
 
 /**
  * The <code>PortletTabbedPane</code> represents the visual portlet tabbed pane interface
  * and is a container for a {@link PortletTab}.
  */
 public class PortletTabbedPane extends BasePortletComponent implements Serializable, PortletTabListener, Cloneable {
 
     private List tabs = new ArrayList();
     private int startIndex = 0;
     private String style = "menu";
 
     /**
      * Constructs an instance of PortletTabbedPane
      */
     public PortletTabbedPane() {
     }
 
     /**
      * Sets the tabbed pane style. Currently supported styles are "menu"
      * and "sub-menu"
      *
      * @param style the tabbed pane style
      */
     public void setStyle(String style) {
         this.style = style;
     }
 
     /**
      * Returns the tabbed pane style. Currently supported styles are "menu"
      * and "sub-menu"
      *
      * @return the tabbed pane style
      */
     public String getStyle() {
         return style;
     }
 
     /**
      * Returns the selected tab if none exists, return null
      *
      * @return the selected portlet tab
      */
     public PortletTab getSelectedTab() {
         for (int i = 0; i < tabs.size(); i++) {
             PortletTab tab = (PortletTab)tabs.get(i);
             if (tab.isSelected()) {
                 return tab;
             }
         }
         return null;
     }
 
     /**
      * Sets the selected portlet tab in this tabbed pane
      *
      * @param tab the selected portlet tab
      */
     public void setSelectedPortletTab(PortletTab tab) {
         PortletTab portletTab;
         List stabs = Collections.synchronizedList(tabs);
         synchronized (stabs) {
             for (int i = 0; i < stabs.size(); i++) {
                 portletTab = (PortletTab) stabs.get(i);
                 if (portletTab.getComponentID() == tab.getComponentID()) {
                     portletTab.setSelected(true);
                 } else {
                     portletTab.setSelected(false);
                 }
             }
         }
     }
 
     /**
      * Returns the tab with the supplied title
      *
      * @param label the tab label
      * @return the tab associated with this title
      */
     public PortletTab getPortletTab(String label) {
         Iterator it = tabs.iterator();
         while (it.hasNext()) {
             PortletTab tab = (PortletTab)it.next();
             if (tab.getLabel().equals(label)) return tab;
         }
         return null;
     }
 
     /**
      * Return the tab contained by this tabbed pane by index
      *
      * @param index the tab index
      * @return the portlet tab
      */
     public PortletTab getPortletTabAt(int index) {
         if (index > tabs.size()) return null;
         return (PortletTab) tabs.get(index);
     }
 
     public int getTabCount() {
         return tabs.size();
     }
 
     public void insertTab(PortletTab tab, int index) {
         tabs.add(index, tab);
     }
 
     /**
      * Adds a new portlet tab to the tabbed pane
      *
      * @param tab a portlet tab to add
      */
     public void addTab(PortletTab tab) {
         tabs.add(tab);
     }
 
     /**
      * Removes a portlet tab from the tabbed pane
      *
      * @param tab the portlet tab to remove
      */
     public void removeTab(PortletTab tab) {
         Iterator it = tabs.iterator();
         while (it.hasNext()) {
             PortletTab atab = (PortletTab)it.next();
             if (tab.getLabel().equals(atab.getLabel())) it.remove();
         }
     }
 
     /**
      * Removes a portlet tab from the tabbed pane at the specified index
      *
      * @param index the index of the tab to remove
      */
     public synchronized void removeTabAt(int index) {
         tabs.remove(index);
     }
 
     /**
      * Removes all portlet tabs from the tabbed pane
      */
     public synchronized void removeAll() {
         for (int i = 0; i < tabs.size(); i++) {
             tabs.remove(i);
         }
     }
 
     /**
      * Sets the portlet tabs in the tabbed pane
      *
      * @param tabs an ArrayList containing the portlet tabs to add
      */
     public void setPortletTabs(ArrayList tabs) {
         this.tabs = tabs;
 
     }
 
     /**
      * Returns a list containing the portlet tabs
      *
      * @return a list containing the portlet tabs
      */
     public List getPortletTabs() {
         return tabs;
     }
 
     /**
      * Initializes the portlet tabbed pane component. Since the components are isolated
      * after Castor unmarshalls from XML, the ordering is determined by a
      * passed in List containing the previous portlet components in the tree.
      *
      * @param list a list of component identifiers
      * @return a list of updated component identifiers
      * @see ComponentIdentifier
      */
     public List init(PortletRequest req, List list) {
         list = super.init(req, list);
         PortletTab tab = null;
 
         List stabs = Collections.synchronizedList(tabs);
 
         synchronized (stabs) {
             Iterator it = stabs.iterator();
 
             while (it.hasNext()) {
                 tab = (PortletTab)it.next();
                 tab.setTheme(theme);
                 list = tab.init(req, list);
                 tab.addComponentListener(this);
                 tab.setParentComponent(this);
             }
         }
 
         tab = this.getSelectedTab();
 
         if (tab == null) {
             tab = this.getPortletTabAt(0);
             this.setSelectedPortletTab(tab);
         }
 
         return list;
     }
 
     /**
      * Gives notification that a portlet tab event has occured
      *
      * @param event the portlet tab event
      */
     public void handlePortletTabEvent(PortletTabEvent event) {
         if (event.getAction() == PortletTabEvent.TabAction.TAB_SELECTED) {
             PortletTab selectedTab = (PortletTab)event.getPortletComponent();
             this.setSelectedPortletTab(selectedTab);
         }
     }
 
     /**
      * Gives notification that a portlet tab event has occured
      *
      * @param event the portlet tab event
      */
     public void actionPerformed(GridSphereEvent event) throws PortletLayoutException, IOException {
 
         super.actionPerformed(event);
 
         PortletComponentEvent compEvt = event.getLastRenderEvent();
         if ((compEvt != null) && (compEvt instanceof PortletTabEvent)) {
             PortletTabEvent tabEvent = (PortletTabEvent)compEvt;
             handlePortletTabEvent(tabEvent);
         }
         List l = Collections.synchronizedList(listeners);
         synchronized(l) {
             Iterator it = l.iterator();
             PortletComponent comp;
 
             while (it.hasNext()) {
                 comp = (PortletComponent) it.next();
                 event.addNewRenderEvent(compEvt);
                 comp.actionPerformed(event);
             }
         }
 
     }
 
     /**
      * Creates the portlet tab link URIs that are used to send events to
      * the portlet tabs.
      *
      * @param event the gridsphere event
      */
     protected String[] createTabLinks(GridSphereEvent event) {
         // Make tab links
         String[] tabLinks = new String[tabs.size()];
         List stabs = Collections.synchronizedList(tabs);
         synchronized(stabs) {
             for (int i = 0; i < stabs.size(); i++) {
                 PortletTab tab = (PortletTab) stabs.get(i);
                 tabLinks[i] = tab.createTabTitleLink(event);
             }
         }
         //req.setAttribute(LayoutProperties.TABLINKS, tabLinks);
         return tabLinks;
     }
 
     /**
      * Replace blank spaces in title with '&nbsp;'
      *
      * @param title the tab title
      * @return a title without blank spaces
      */
     private String replaceBlanks(String title) {
         String result = new String();
         StringTokenizer st = new StringTokenizer(title);
         while(st.hasMoreTokens()) {
             result += st.nextToken()+"&nbsp;";
         }
         return result;
     }
 
     /**
      * Performs the rendering of a top-level tabbed pane for the "menu" style
      *
      * @param event the gridsphere event
      * @param links an array of URI links for the tabs
      */
     protected void doRenderMenu(GridSphereEvent event, String[] links) throws PortletLayoutException, IOException {
         PortletRequest req = event.getPortletRequest();
         PortletResponse res = event.getPortletResponse();
         PrintWriter out = res.getWriter();
 
         PortletRole userRole = req.getRole();
 
         // Render tabs titles
         out.println("<img height=\"3\" src=\"themes/" + theme + "/images/spacer.gif\"/>");
         out.println("<table class=\"tab-pane\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">");
         out.println("<tr >");
         out.println("<td class=\"tab-empty\">&nbsp;</td>");
 
         PortletTab tab;
         List stabs = Collections.synchronizedList(tabs);
         synchronized (stabs) {
             for (int i = 0; i < stabs.size(); i++) {
 
                 tab = (PortletTab) stabs.get(i);
 
                 PortletRole tabRole = tab.getRequiredRole();
 
                 if (userRole.compare(userRole, tabRole) >= 0) {
 
                     String lang = req.getLocale().getLanguage();
 
                     String title = tab.getTitle(lang);
 
                     if (tab.isSelected()) {
                         out.println("<td><img src=\"themes/" + theme + "/images/tab-active-left.gif\"/></td>");
                         out.println("<td class=\"tab-active\">" + replaceBlanks(title) + "</td>");
                         out.println("<td><img src=\"themes/" + theme + "/images/tab-active-right.gif\"/></td>");
                     } else {
                         out.println("<td><img src=\"themes/" + theme + "/images/tab-inactive-left.gif\"/></td>");
                         out.println("<td class=\"tab-inactive\"><a class=\"tab-menu\" href=\"" + links[i] + "\"" + " onClick=\"this.href='" + links[i] + "&JavaScript=enabled'\"/>" + replaceBlanks(title) + "</a>");
                         out.println("<td><img src=\"themes/" + theme + "/images/tab-inactive-right.gif\"/></td>");
                     }
                     out.println("<td class=\"tab-empty\">&nbsp;</td>");
                 }  else {
                     // if role is < required role we try selecting the next possible tab
                    //System.err.println("in PortletTabbedPane menu: role is < required role we try selecting the next possible tab");
                     if (tab.isSelected()) {
                        int index = (i + 1);
                         PortletTab newtab = (PortletTab) stabs.get(index);
                        if (index < tabs.size()) {
                            this.setSelectedPortletTab(newtab);
                        }
                     }
                 }
             }
             out.println("<td class=\"tab-fillup\">&nbsp;</td></tr></table>");
 
             if (!stabs.isEmpty()) {
                 PortletTab selectedTab = getSelectedTab();
                 if (selectedTab != null) {
                     selectedTab.doRender(event);
                 }
             }
         }
     }
 
     /**
      * Performs the rendering of a sub-level tabbed pane for the "sub-menu" style
      *
      * @param event the gridsphere event
      * @param links an array of URI links for the tabs
      */
     protected void doRenderSubMenu(GridSphereEvent event, String[] links) throws PortletLayoutException, IOException {
         PortletRequest req = event.getPortletRequest();
         PortletResponse res = event.getPortletResponse();
         PrintWriter out = res.getWriter();
         PortletRole userRole = req.getRole();
 
         // Render tabs titles
         out.println("<table border=\"0\" class=\"tab-sub-pane\" width=\"100%\"><tr><td>");
         out.println("<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\"><tr>");
 
         PortletTab tab;
         List stabs = Collections.synchronizedList(tabs);
         synchronized (stabs) {
             for (int i = 0; i < stabs.size(); i++) {
                 tab = (PortletTab) stabs.get(i);
                 PortletRole requiredRole = tab.getRequiredRole();
                 if (userRole.compare(userRole, requiredRole) >= 0) {
 
                     String lang = req.getLocale().getLanguage();
                     String title = tab.getTitle(lang);
 
                     if (tab.isSelected()) {
                         out.println("<td> <span class=\"tab-sub-active\">");
                         out.println("<a class=\"tab-sub-menu-active\" href=\""+links[i]+ "\"" + " onClick=\"this.href='" + links[i] + "&JavaScript=enabled'\"/>" + title + "</a></span></td>");
                     } else {
                         out.println("<td> <span class=\"tab-sub-inactive\">");
                         out.println("<a class=\"tab-sub-menu\" href=\"" + links[i] + "\"" + " onClick=\"this.href='" + links[i] + "&JavaScript=enabled'\"/>" + title + "</a>");
                         out.println("</span></td>");
                     }
                 } else {
                     //System.err.println("in PortletTabbedPane submenu: role is < required role we try selecting the next possible tab");
                     if (tab.isSelected()) {
                        int index = (i + 1);
                         PortletTab newtab = (PortletTab) stabs.get(index);
                        if (index < tabs.size()) {
                            this.setSelectedPortletTab(newtab);
                        }
                     }
                 }
             }
 
             out.println("</tr></table>");
             out.println("</td></tr></table>");
 
             PortletTab selectedTab = getSelectedTab();
             if (selectedTab != null)
                 selectedTab.doRender(event);
         }
     }
 
     /**
      * Renders the portlet frame component
      *
      * @param event a gridsphere event
      * @throws PortletLayoutException if a layout error occurs during rendering
      * @throws IOException if an I/O error occurs during rendering
      */
     public void doRender(GridSphereEvent event) throws PortletLayoutException, IOException {
         super.doRender(event);
 
         String[] links = createTabLinks(event);
         if (style.equals("sub-menu")) {
             doRenderSubMenu(event, links);
         } else {
             doRenderMenu(event, links);
         }
     }
 
     public Object clone() throws CloneNotSupportedException {
         PortletTabbedPane t = (PortletTabbedPane)super.clone();
         t.style = this.style;
         t.startIndex = this.startIndex;
         //t.selectedIndex = this.selectedIndex;
         List stabs = Collections.synchronizedList(tabs);
         synchronized (stabs) {
             t.tabs = new ArrayList(stabs.size());
             for (int i = 0; i < stabs.size(); i++) {
                 PortletTab tab = (PortletTab)stabs.get(i);
                 t.tabs.add(tab.clone());
             }
         }
         return t;
     }
 }
