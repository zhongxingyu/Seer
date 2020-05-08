 /*
 * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
 * @author <a href="mailto:wehrens@aei.mpg.de">Oliver Wehrens</a>
 * @version $Id$
 */
 
 package org.gridlab.gridsphere.layout;
 
 import org.gridlab.gridsphere.core.persistence.PersistenceManagerException;
 import org.gridlab.gridsphere.layout.event.PortletComponentEvent;
 import org.gridlab.gridsphere.layout.event.PortletTabEvent;
 import org.gridlab.gridsphere.layout.event.PortletTabListener;
 import org.gridlab.gridsphere.portlet.PortletRequest;
 import org.gridlab.gridsphere.portlet.PortletResponse;
 import org.gridlab.gridsphere.portlet.PortletRole;
 import org.gridlab.gridsphere.portlet.impl.SportletProperties;
 import org.gridlab.gridsphere.portletcontainer.GridSphereConfig;
 import org.gridlab.gridsphere.portletcontainer.GridSphereEvent;
 
 import java.io.File;
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
     private String layoutDescriptor = null;
 
     //protected StringBuffer pane = new StringBuffer();
 
     /**
      * Constructs an instance of PortletTabbedPane
      */
     public PortletTabbedPane() {
     }
 
     public void setLayoutDescriptor(String layoutDescriptor) {
         this.layoutDescriptor = layoutDescriptor;
     }
 
     public String getLayoutDescriptor() {
         return layoutDescriptor;
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
             PortletTab tab = (PortletTab) tabs.get(i);
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
             PortletTab tab = (PortletTab) it.next();
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
         if (index >= tabs.size()) return null;
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
             PortletTab atab = (PortletTab) it.next();
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
     public void setPortletTabs(List tabs) {
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
 
     public PortletTab getLastPortletTab() {
         return (PortletTab) tabs.get(tabs.size() - 1);
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
                 tab = (PortletTab) it.next();
                 tab.setTheme(theme);
                 list = tab.init(req, list);
                 tab.addComponentListener(this);
                 tab.setParentComponent(this);
             }
         }
 
         tab = this.getSelectedTab();
 
         if (tab == null) {
             tab = this.getPortletTabAt(0);
             if (tab != null) this.setSelectedPortletTab(tab);
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
             PortletTab selectedTab = (PortletTab) event.getPortletComponent();
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
             PortletTabEvent tabEvent = (PortletTabEvent) compEvt;
             handlePortletTabEvent(tabEvent);
         }
         List l = Collections.synchronizedList(listeners);
         synchronized (l) {
             Iterator it = l.iterator();
             PortletComponent comp;
 
             while (it.hasNext()) {
                 comp = (PortletComponent) it.next();
                 event.addNewRenderEvent(compEvt);
                 comp.actionPerformed(event);
             }
         }
 
     }
 
 
     public void setTheme(String theme) {
         this.theme = theme;
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
         synchronized (stabs) {
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
         String result = "&nbsp;";
         StringTokenizer st = new StringTokenizer(title);
         while (st.hasMoreTokens()) {
             result += st.nextToken() + "&nbsp;";
         }
         return result;
     }
     /**
      * Performs the rendering of a top-level tabbed pane for the "menu" style
      *
      * @param event the gridsphere event
      * @param links an array of URI links for the tabs
      */
     protected void doRenderMenuWML(GridSphereEvent event, String[] links) throws PortletLayoutException, IOException {
         PortletRequest req = event.getPortletRequest();
         PortletResponse res = event.getPortletResponse();
         PrintWriter out = res.getWriter();
 
         PortletRole userRole = req.getRole();
 
         // put a spacing if a gfx theme
         //out.println("<img height=\"3\" src=\"themes/" + theme + "/images/spacer.gif\"/>");
         out.println("<table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">");
         //      out.println("<table border=\"0\" cellspacing=\"0\" cellpadding=\"0\">");
         out.println("<tr >");
 
         PortletTab tab;
         for (int i = 0; i < tabs.size(); i++) {
 
             tab = (PortletTab) tabs.get(i);
             PortletRole tabRole = tab.getRequiredRole();
             if (userRole.compare(userRole, tabRole) >= 0) {
 
                 String lang = req.getLocale().getLanguage();
                 String title = tab.getTitle(lang);
 
                 String path = "themes" + File.separator + theme + File.separator + "images" + File.separator;
                 if (tab.isSelected()) {
                     /*out.println("<td height=\"24\" width=\"6\" background=\"" + path + "tab-active-left.gif\">");
                     //out.println("&nbsp;");
                     out.println("</td>");*/
                     out.println("<td class=\"tabCell\">");
                     out.println( replaceBlanks(title));
                     //out.println("<td height=\"24\" width=\"6\" background=\"" + path + "tab-active-right.gif\">");
                     //out.println("&nbsp;");
                     out.println("</td>");
 
                 } else {
                    // out.println("<td height=\"24\" width=\"6\" background=\"" + path + "tab-inactive-left.gif\">");
                    // out.println("&nbsp;");
                   // out.println("</td>");
                     out.println("<td class=\"tabCellDeact\">");
                     out.println("<a href=\"" + links[i] + "\">" + replaceBlanks(title) + "</a>");
                   //  out.println("<td height=\"24\" width=\"6\" background=\"" + path + "tab-inactive-right.gif\">");
                   //  out.println("&nbsp;");
                     out.println("</td>");
                 }
 
                 //out.println("<td class=\"tab-empty\"></td>");
             } else {
                 // if role is < required role we try selecting the next possible tab
                 //System.err.println("in PortletTabbedPane menu: role is < required role we try selecting the next possible tab");
                 if (tab.isSelected()) {
                     int index = (i + 1);
                     PortletTab newtab = (PortletTab) tabs.get(index);
                     if (index < tabs.size()) {
                         this.setSelectedPortletTab(newtab);
                     }
                 }
             }
         }
 
         //out.println("<td>&nbsp;</td>");
         out.println("</tr></table>");
 
         if (!tabs.isEmpty()) {
             PortletTab selectedTab = getSelectedTab();
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
     protected void doRenderSubMenuWML(GridSphereEvent event, String[] links) throws PortletLayoutException, IOException {
         PortletRequest req = event.getPortletRequest();
         PortletResponse res = event.getPortletResponse();
         PrintWriter out = res.getWriter();
         PortletRole userRole = req.getRole();
 
         //PortletTab parentTab = (PortletTab)this.getParentComponent();
         //PortletThemeRegistry themeReg = PortletThemeRegistry.getInstance();
         String path = "themes" + File.separator + theme + File.separator + "images" + File.separator;
 
         // Render tabs titles get always the same componenttheme as the upper menu
         out.println("<table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"100%\"><tr>");
         //out.println("<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" bgcolor=\"#EEEEEE\"><tr>");
 
 
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
                     	out.println("<td class=\"tabCellSubm\">");
                         //out.println("<span class=\"tab-sub-active\">");
                         out.println("<a href=\"" + links[i] + "\">" + replaceBlanks(title) + "</a>");
                         out.println("</td>");
                     } else {
                     	out.println("<td class=\"tabCellDeactSubm\">");
                         //out.println("<span class=\"tab-sub-inactive\">");
                         out.println("<a href=\"" + links[i] + "\">" + replaceBlanks(title) + "</a>");
                         out.println("</td>");
                         //out.println("</span>");
                     }
                     out.println("</td>");
 
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
             //out.println("<td>"); //width=100
             //out.println("&nbsp;</td></td></tr></table>");
             PortletTab selectedTab = getSelectedTab();
             if (selectedTab != null)
                 selectedTab.doRender(event);
         }
     }
     /**
      * Performs the rendering of a top-level tabbed pane for the "menu" style
      *
      * @param event the gridsphere event
      * @param links an array of URI links for the tabs
      */
     protected void doRenderMenuHTML(GridSphereEvent event, String[] links) throws PortletLayoutException, IOException {
         PortletRequest req = event.getPortletRequest();
 
         PortletRole userRole = req.getRole();
         StringBuffer pane = new StringBuffer();
         // put a spacing if a gfx theme
         //out.println("<img height=\"3\" src=\"themes/" + theme + "/images/spacer.gif\"/>");
         // pane.append("<table><tr><td height=\"600px\">");
         pane.append("<!-- start tabbed pane --><table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">");
         //      out.println("<table border=\"0\" cellspacing=\"0\" cellpadding=\"0\">");
 
         pane.append("<tr >");
 
 
         PortletTab tab;
         for (int i = 0; i < tabs.size(); i++) {
 
             tab = (PortletTab) tabs.get(i);
             PortletRole tabRole = tab.getRequiredRole();
             if (userRole.compare(userRole, tabRole) >= 0) {
 
                 String lang = req.getLocale().getLanguage();
                 String title = tab.getTitle(lang);
 
                 String path = "themes" + File.separator + theme + File.separator + "images" + File.separator;
                 if (tab.isSelected()) {
                    pane.append("<td height=\"24\" width=\"6\" background=\"" + path + "tab-active-left.gif\">");
                    pane.append("&nbsp;");
                    pane.append("</td>");
                     pane.append("<td height=\"24\" nowrap=\"nowrap\" background=\"" + path + "tab-active-middle.gif\">");
                     pane.append("<span class=\"tab-active\">" + replaceBlanks(title) + "</span>");
                    pane.append("<td height=\"24\" width=\"6\" background=\"" + path + "tab-active-right.gif\">");
                    pane.append("&nbsp;");
                    pane.append("</td>");

                 } else {
                    pane.append("<td height=\"24\" width=\"6\" background=\"" + path + "tab-inactive-left.gif\">");
                    pane.append("&nbsp;");
                    pane.append("</td>");
                     pane.append("<td height=\"24\" nowrap=\"nowrap\" background=\"" + path + "tab-inactive-middle.gif\">");
                     pane.append("<a class=\"tab-menu\" href=\"" + links[i] + "\"" + " onClick=\"this.href='" + links[i] + "&JavaScript=enabled'\">" + replaceBlanks(title) + "</a>");
                    pane.append("<td height=\"24\" width=\"6\" background=\"" + path + "tab-inactive-right.gif\">");
                    pane.append("&nbsp;");
                    pane.append("</td>");
                 }
 
                 //out.println("<td class=\"tab-empty\"></td>");
             } else {
                 // if role is < required role we try selecting the next possible tab
                 //System.err.println("in PortletTabbedPane menu: role is < required role we try selecting the next possible tab");
                 if (tab.isSelected()) {
                     int index = (i + 1);
                     PortletTab newtab = (PortletTab) tabs.get(index);
                     if (index < tabs.size()) {
                         this.setSelectedPortletTab(newtab);
                     }
                 }
             }
         }
 
         pane.append("<td class=\"tab-fillup\">&nbsp;</td>");
         pane.append("</tr></table>");
 
         if (!tabs.isEmpty()) {
             PortletTab selectedTab = getSelectedTab();
             if (selectedTab != null) {
                 selectedTab.doRender(event);
                 pane.append(selectedTab.getBufferedOutput(req));
             }
         }
         req.setAttribute(SportletProperties.RENDER_OUTPUT + componentIDStr, pane);
         //pane.append("</td></tr></table>");
     }
 
     /**
      * Performs the rendering of a sub-level tabbed pane for the "sub-menu" style
      *
      * @param event the gridsphere event
      * @param links an array of URI links for the tabs
      */
     protected void doRenderSubMenuHTML(GridSphereEvent event, String[] links) throws PortletLayoutException, IOException {
         PortletRequest req = event.getPortletRequest();
 
         PortletRole userRole = req.getRole();
 
         //PortletTab parentTab = (PortletTab)this.getParentComponent();
         //PortletThemeRegistry themeReg = PortletThemeRegistry.getInstance();
         String path = "themes" + File.separator + theme + File.separator + "images" + File.separator;
         StringBuffer pane = new StringBuffer();
         // Render tabs titles get always the same componenttheme as the upper menu
         pane.append("<table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" class=\"tab-sub-pane" + theme + "\" width=\"100%\"><tr><td>");
         pane.append("<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\"><tr>");
 
 
         PortletTab tab;
 
         List stabs = Collections.synchronizedList(tabs);
         synchronized (stabs) {
             for (int i = 0; i < stabs.size(); i++) {
                 tab = (PortletTab) stabs.get(i);
                 PortletRole requiredRole = tab.getRequiredRole();
                 if (userRole.compare(userRole, requiredRole) >= 0) {
 
                     String lang = req.getLocale().getLanguage();
                     String title = tab.getTitle(lang);
 
                     pane.append("<td nowrap=\"nowrap\" background=\"" + path + "subtab-middle.gif\" height=\"24\">");
                     if (tab.isSelected()) {
                         pane.append("<span class=\"tab-sub-active\">");
                         pane.append("<a class=\"tab-sub-menu-active\" href=\"" + links[i] + "\"" + " onClick=\"this.href='" + links[i] + "&JavaScript=enabled'\">" + replaceBlanks(title) + "</a></span>");
                     } else {
                         pane.append("<span class=\"tab-sub-inactive\">");
                         pane.append("<a class=\"tab-sub-menu\" href=\"" + links[i] + "\"" + " onClick=\"this.href='" + links[i] + "&JavaScript=enabled'\">" + replaceBlanks(title) + "</a>");
                         pane.append("</span>");
                     }
                     pane.append("</td>");
 
                 } else {
                     //System.err.println("in PortletTabbedPane submenu: role is < required role we try selecting the next possible tab");
                     if (tab.isSelected()) {
                         int index = (i + 1);                        
                         if (index < tabs.size()) {
                             PortletTab newtab = (PortletTab) stabs.get(index);
                             this.setSelectedPortletTab(newtab);
                         }
                     }
                 }
             }
 
             pane.append("</tr></table>");
             pane.append("<td background=\"" + path + "subtab-middle.gif\" style=\"width:100%\">&nbsp;</td>");
 
             // playing around to get rightmost forward icon
             //pane.append("<td class=\"window-icon-right\"><a href=\"http://127.0.0.1:8080/gridsphere/gridsphere?gs_state=MINIMIZED&cid=profileTB\"><img border=\"0\" src=\"themes/default/images/window_minimize.gif\" title=\"Minimize\"/></a></td>");
 
             pane.append("</tr></table><!-- end tabbed pane -->");
             PortletTab selectedTab = getSelectedTab();
             if (selectedTab != null) {
                 selectedTab.doRender(event);
                 pane.append(selectedTab.getBufferedOutput(req));
             }
             req.setAttribute(SportletProperties.RENDER_OUTPUT + componentIDStr, pane);
         }
     }
 
     /**
      * Renders the portlet frame component
      *
      * @param event a gridsphere event
      * @throws PortletLayoutException if a layout error occurs during rendering
      * @throws IOException            if an I/O error occurs during rendering
      */
     public void doRender(GridSphereEvent event) throws PortletLayoutException, IOException {
         super.doRender(event);
     	String markupName=event.getPortletRequest().getClient().getMarkupName();
     	if (markupName.equals("html")){
     		doRenderHTML(event);
     	} else {
     		doRenderWML(event);
     	}
     }
     public void doRenderHTML(GridSphereEvent event) throws PortletLayoutException, IOException {
         String[] links = createTabLinks(event);
         if (style.equals("sub-menu")) {
             doRenderSubMenuHTML(event, links);
         } else {
             doRenderMenuHTML(event, links);
         }
     }
     public void doRenderWML(GridSphereEvent event) throws PortletLayoutException, IOException {
         String[] links = createTabLinks(event);
         if (style.equals("sub-menu")) {
             doRenderSubMenuWML(event, links);
         } else {
             doRenderMenuWML(event, links);
         }
     }
 
     public void remove(PortletComponent pc, PortletRequest req) {
         tabs.remove(pc);
         if (tabs.isEmpty()) parent.remove(this, req);
     }
 
     public void save() throws IOException {
         try {
             String layoutMappingFile = GridSphereConfig.getServletContext().getRealPath("/WEB-INF/mapping/layout-mapping.xml");
             PortletLayoutDescriptor.savePortletTabbedPane(this, layoutDescriptor, layoutMappingFile);
         } catch (PersistenceManagerException e) {
             throw new IOException("Unable to save user's tabbed pane: " + e.getMessage());
         }
     }
 
     public Object clone() throws CloneNotSupportedException {
         PortletTabbedPane t = (PortletTabbedPane) super.clone();
         t.style = this.style;
         t.startIndex = this.startIndex;
         //t.selectedIndex = this.selectedIndex;
         List stabs = Collections.synchronizedList(tabs);
         synchronized (stabs) {
             t.tabs = new ArrayList(stabs.size());
             for (int i = 0; i < stabs.size(); i++) {
                 PortletTab tab = (PortletTab) stabs.get(i);
                 t.tabs.add(tab.clone());
             }
         }
         return t;
     }
 }
