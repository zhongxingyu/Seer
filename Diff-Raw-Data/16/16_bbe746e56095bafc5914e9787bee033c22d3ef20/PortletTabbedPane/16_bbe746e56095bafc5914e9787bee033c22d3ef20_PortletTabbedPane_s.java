 /*
  * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
  * @author <a href="mailto:wehrens@aei.mpg.de">Oliver Wehrens</a>
  * @version $Id$
  */
 
 package org.gridlab.gridsphere.layout;
 
 import org.gridlab.gridsphere.layout.event.PortletTabEvent;
 import org.gridlab.gridsphere.layout.event.PortletTabListener;
 import org.gridlab.gridsphere.portlet.PortletResponse;
 import org.gridlab.gridsphere.portlet.PortletRole;
 import org.gridlab.gridsphere.portlet.PortletRequest;
 import org.gridlab.gridsphere.portletcontainer.GridSphereEvent;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.StringTokenizer;
 
 /**
  * The <code>PortletTabbedPane</code> represents the visual portlet tabbed pane interface
  * and is a container for a {@link PortletTab}.
  */
 public class PortletTabbedPane extends BasePortletComponent implements Serializable, PortletTabListener, Cloneable {
 
     private List tabs = new ArrayList();
     private int startIndex = 0;
     private int selectedIndex = 0;
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
 
     public void setSelectedIndex(int index) {
         this.selectedIndex = index;
     }
 
     public int getSelectedIndex() {
         return selectedIndex;
     }
 
     public PortletTab getSelectedTab() {
         return (PortletTab) tabs.get(selectedIndex);
     }
 
     public PortletTab getPortletTabAt(int index) {
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
      * @param title a tab title
      * @param tab a portlet tab to add
      */
     public void addTab(String title, PortletTab tab) {
         tabs.add(tab);
     }
 
     /**
      * Removes a portlet tab from the tabbed pane
      *
      * @param tab the portlet tab to remove
      */
     public void removeTab(PortletTab tab) {
         if (tabs.contains(tab)) tabs.remove(tab);
     }
 
     /**
      * Removes a portlet tab from the tabbed pane at the specified index
      *
      * @param index the index of the tab to remove
      */
     public void removeTabAt(int index) {
         tabs.remove(index);
     }
 
     /**
      * Removes all portlet tabs from the tabbed pane
      */
     public void removeAll() {
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
      * Sets the selected portlet tab in this tabbed pane
      *
      * @param tab the selected portlet tab
      */
     public void setSelectedPortletTab(PortletTab tab) {
         PortletTab portletTab;
 
         for (int i = 0; i < tabs.size(); i++) {
             portletTab = (PortletTab) tabs.get(i);
             if (portletTab.getComponentID() == tab.getComponentID()) {
                 selectedIndex = i;
                 portletTab.setSelected(true);
             } else {
                 portletTab.setSelected(false);
             }
         }
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
     public List init(List list) {
         if (selectedIndex < 0) selectedIndex = 0;
         PortletTab tab = null;
         for (int i = 0; i < getTabCount(); i++) {
             tab = getPortletTabAt(i);
             if (selectedIndex == i) tab.setSelected(true);
             tab.addPortletTabListener(this);
             list = tab.init(list);
         }
         return list;
     }
 
     /**
      * Gives notification that a portlet tab event has occured
      *
      * @param event the portlet tab event
      */
     public void handlePortletTabEvent(PortletTabEvent event) {
         if (event.getAction() == PortletTabEvent.Action.TAB_SELECTED) {
             PortletTab selectedTab = event.getPortletTab();
             setSelectedPortletTab(selectedTab);
         }
     }
 
     /**
      * Creates the portlet tab link URIs that are used to send events to
      * the portlet tabs.
      *
      * @param event the gridspher event
      */
     protected String[] createTabLinks(GridSphereEvent event) {
         // Make tab links
         String[] tabLinks = new String[tabs.size()];
         for (int i = 0; i < tabs.size(); i++) {
             PortletTab tab = (PortletTab) tabs.get(i);
             tabLinks[i] = tab.createTabTitleLink(event);
         }
         //req.setAttribute(LayoutProperties.TABLINKS, tabLinks);
         return tabLinks;
     }
 
     private String replaceBlanks(String title) {
 
         String result = new String();
 
         /*String[] splittedresult = title.split(" ");
 
         for (int i=0;i<splittedresult.length;i++) {
 
             result = result +splittedresult[i];
             if (i<splittedresult.length-1) {
                 result = result +"&nbsp;";
             }
         }
         */
         StringTokenizer st = new StringTokenizer(title);
         while(st.hasMoreTokens()) {
             result = result + st.nextToken()+"&nbsp;";
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
         for (int i = 0; i < tabs.size(); i++) {
 
             tab = (PortletTab) tabs.get(i);
 
             PortletRole requiredRole = tab.getRequiredRole();
 
             if (userRole.compare(userRole, requiredRole) >= 0) {
 
                 String title = tab.getTitle();
                 if (tab.isSelected()) {
                     out.println("<td><img src=\"themes/" + theme + "/images/tab-active-left.gif\"/></td>");
                     out.println("<td class=\"tab-active\">" + replaceBlanks(title) + "</td>");
                     out.println("<td><img src=\"themes/" + theme + "/images/tab-active-right.gif\"/></td>");
                 } else {
                     out.println("<td><img src=\"themes/" + theme + "/images/tab-inactive-left.gif\"/></td>");
                     out.println("<td class=\"tab-inactive\"><a class=\"tab-menu\" href=\"" + links[i] + "\" >" + replaceBlanks(title) + "</a>");
                     out.println("<td><img src=\"themes/" + theme + "/images/tab-inactive-right.gif\"/></td>");
                 }
                 out.println("<td class=\"tab-empty\">&nbsp;</td>");
             }
         }
         out.println("<td class=\"tab-fillup\">&nbsp</td></tr></table>");
 
         if (!tabs.isEmpty()) {
             PortletTab selectedTab = (PortletTab) tabs.get(selectedIndex);
             if (selectedTab != null) {
                 selectedTab.doRender(event);
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
         for (int i = 0; i < tabs.size(); i++) {
             tab = (PortletTab) tabs.get(i);
             PortletRole requiredRole = tab.getRequiredRole();
             if (userRole.compare(userRole, requiredRole) >= 0) {
 
                 String title = tab.getTitle();
                 if (tab.isSelected()) {
                     out.println("<td> <span class=\"tab-sub-active\">");
                     out.println("<a class=\"tab-sub-menu-active\" href=\""+links[i]+ "\" >"+ title + "</a></span></td>");
                 } else {
                     out.println("<td> <span class=\"tab-sub-inactive\">");
                     out.println("<a class=\"tab-sub-menu\" href=\"" + links[i] + "\" >" + title + "</a>");
                     out.println("</span></td>");
                 }
             }
         }
 
         out.println("</tr></table>");
         out.println("</td></tr></table>");
 
         PortletTab selectedTab = (PortletTab) tabs.get(selectedIndex);
         if (selectedTab != null)
             selectedTab.doRender(event);
     }
 
     /**
      * Renders the portlet frame component
      *
      * @param event a gridsphere event
      * @throws PortletLayoutException if a layout error occurs during rendering
      * @throws IOException if an I/O error occurs during rendering
      */
     public void doRender(GridSphereEvent event) throws PortletLayoutException, IOException {
 
         // This insures that if no portlet component id (cid)
         // is set, then the selected tab index is the startIndex = 0
         if (event.getPortletComponentID() < 0) {
             this.setSelectedPortletTab((PortletTab)tabs.get(selectedIndex));
         }
 
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
         t.selectedIndex = this.selectedIndex;
         t.tabs = new ArrayList(this.tabs.size());
         for (int i = 0; i < this.tabs.size(); i++) {
             PortletTab tab = (PortletTab)this.tabs.get(i);
             t.tabs.add(tab.clone());
         }
         return t;
     }
 }
