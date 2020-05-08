 /*
  * $Id: LicenseHeader-GPLv2.txt 288 2008-01-29 00:59:35Z andrew $
  * --------------------------------------------------------------------------------------
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  */
 
 package org.mule.galaxy.web.client.admin;
 
 import org.mule.galaxy.web.client.Galaxy;
 import org.mule.galaxy.web.client.PageInfo;
 import org.mule.galaxy.web.client.PageManager;
 import org.mule.galaxy.web.client.ui.NavMenuItem;
 import org.mule.galaxy.web.client.ui.panel.MenuPanel;
 import org.mule.galaxy.web.client.ui.panel.WidgetHelper;
 import org.mule.galaxy.web.rpc.SecurityServiceAsync;
 
 import com.extjs.gxt.ui.client.widget.ContentPanel;
 import com.google.gwt.user.client.History;
 import com.google.gwt.user.client.ui.Widget;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class AdministrationPanel extends MenuPanel {
 
     private final Galaxy galaxy;
     protected List<NavMenuItem> manageItems;
     protected List<NavMenuItem> utilityItems;
     private ContentPanel accordionPanel;
 
     protected AdministrationPanel(Galaxy galaxy, boolean init) {
         this.galaxy = galaxy;
 
         if (init) {
             init();
         }
     }
 
     public AdministrationPanel(Galaxy galaxy) {
         this(galaxy, true);
     }
 
     protected void init() {
         setId("administrationTabBody");
 
         accordionPanel = WidgetHelper.createAccodionWrapperPanel();
 
         manageItems = new ArrayList<NavMenuItem>();
         utilityItems = new ArrayList<NavMenuItem>();
 
         if (galaxy.hasPermission("MANAGE_USERS") && galaxy.isUserManagementSupported()) {
             addManageMenuItem(new NavMenuItem("Users",
                     "users",
                     new UserListPanel(this),
                     new UserForm(this)));
         }
 
         if (galaxy.hasPermission("MANAGE_GROUPS")) {
             addManageMenuItem(new NavMenuItem("User Groups",
                     "groups",
                     new GroupListPanel(this),
                     new GroupForm(this)));
         }
 
        addUtilityMenuItem(new NavMenuItem("Admin Shell",
                "adminShell",
                new AdminShellPanel(this)));

        addUtilityMenuItem(new NavMenuItem("Scheduler",
                "schedules",
                new ScheduleListPanel(this),
                new ScheduleForm(this)));
     }
 
     public void registerPage(final NavMenuItem item) {
         // handle page creation for list forms
         createPageInfo(item.getTokenBase(), item.getListPanel());
 
         if (item.getFormPanel() != null) {
             // handle page info creation for add forms
             createPageInfo(item.getTokenBase() + "/" + PageManager.WILDCARD, item.getFormPanel());
         }
     }
 
     @Override
     protected void onFirstShow() {
         super.onFirstShow();
 
         // list of all items for this panel
         accordionPanel.add(WidgetHelper.createPanelWithListView("Manage", manageItems));
         accordionPanel.add(WidgetHelper.createPanelWithListView("Utility", utilityItems));
         addMenuItem(accordionPanel);
 
         // default to users panel.
         if ("admin".equals(History.getToken())) {
             History.newItem("users");
         }
     }
 
 
     protected boolean showTypeSystem() {
         return true;
     }
 
     protected void createPageInfo(String token, final WidgetHelper composite) {
         final AdministrationPanel aPanel = this;
         PageInfo page = new PageInfo(token) {
 
             @Override
             public int getTabIndex() {
                 return galaxy.getAdminTab();
             }
 
             public Widget createInstance() {
                 return null;
             }
 
             public Widget getInstance() {
                 aPanel.setMain(composite);
                 return aPanel;
             }
 
         };
         getGalaxy().getPageManager().addPage(page);
     }
 
 
     public Galaxy getGalaxy() {
         return galaxy;
     }
 
     public SecurityServiceAsync getSecurityService() {
         return getGalaxy().getSecurityService();
     }
 
     public List<NavMenuItem> getManageItems() {
         return manageItems;
     }
 
     public void setManageItems(List<NavMenuItem> manageItems) {
         this.manageItems = manageItems;
     }
 
 
     public List<NavMenuItem> getUtilityItems() {
         return utilityItems;
     }
 
     public void setUtilityItems(List<NavMenuItem> utilityItems) {
         this.utilityItems = utilityItems;
     }
 
     public void addUtilityMenuItem(NavMenuItem item) {
         addUtilityMenuItem(item, -1);
     }
 
     public void addUtilityMenuItem(NavMenuItem item, int pos) {
         if (pos == -1) {
             pos = utilityItems.size();
         }
         utilityItems.add(pos, item);
         registerPage(item);
     }
 
     public void addManageMenuItem(NavMenuItem item) {
         addManageMenuItem(item, -1);
     }
 
     public void addManageMenuItem(NavMenuItem item, int pos) {
         if (pos == -1) {
             pos = manageItems.size();
         }
         manageItems.add(pos, item);
         registerPage(item);
     }
 
 
     public ContentPanel getAccordionPanel() {
         return accordionPanel;
     }
 
     public void setAccordionPanel(ContentPanel accordionPanel) {
         this.accordionPanel = accordionPanel;
     }
 }
