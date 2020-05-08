 /*
 * $Id$
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
 package org.mule.galaxy.web.client;
 
 import org.mule.galaxy.web.client.activity.ActivityPanel;
 import org.mule.galaxy.web.client.admin.AdministrationPanel;
 import org.mule.galaxy.web.client.artifact.ArtifactPanel;
 import org.mule.galaxy.web.client.registry.ArtifactForm;
 import org.mule.galaxy.web.client.registry.BrowsePanel;
 import org.mule.galaxy.web.client.registry.SearchPanel;
 import org.mule.galaxy.web.client.util.ExternalHyperlink;
 import org.mule.galaxy.web.client.workspace.ManageWorkspacePanel;
 import org.mule.galaxy.web.client.workspace.WorkspaceForm;
 import org.mule.galaxy.web.rpc.AbstractCallback;
 import org.mule.galaxy.web.rpc.HeartbeatService;
 import org.mule.galaxy.web.rpc.HeartbeatServiceAsync;
 import org.mule.galaxy.web.rpc.RegistryService;
 import org.mule.galaxy.web.rpc.RegistryServiceAsync;
 import org.mule.galaxy.web.rpc.SecurityService;
 import org.mule.galaxy.web.rpc.SecurityServiceAsync;
 import org.mule.galaxy.web.rpc.WUser;
 
 import com.google.gwt.core.client.EntryPoint;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.user.client.History;
 import com.google.gwt.user.client.HistoryListener;
 import com.google.gwt.user.client.rpc.ServiceDefTarget;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.RootPanel;
 import com.google.gwt.user.client.ui.SimplePanel;
 import com.google.gwt.user.client.ui.SourcesTabEvents;
 import com.google.gwt.user.client.ui.TabListener;
 import com.google.gwt.user.client.ui.TabPanel;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 
 /**
  * Entry point classes define <code>onModuleLoad()</code>.
  */
 public class Galaxy implements EntryPoint, HistoryListener {
 
     private static final String DEFAULT_PAGE = "browse";
     private SimplePanel registryPanel;
     private SimplePanel activityPanel;
     private SimplePanel adminPanel;
     private RegistryServiceAsync registryService;
     private SecurityServiceAsync securityService;
     private HeartbeatServiceAsync heartbeatService;
     private FlowPanel rightPanel;
     private PageInfo curInfo;
     private Map history = new HashMap();
     private TabPanel tabPanel;
     private WUser user;
     protected int oldTab;
     private boolean suppressTabHistory;
     
     private Map historyListeners = new HashMap();
     private int adminTabIndex;
     private BrowsePanel browsePanel;
     
     /**
      * This is the entry point method.
      */
     public void onModuleLoad() {
         History.addHistoryListener(this);
         
         this.registryService = (RegistryServiceAsync) GWT.create(RegistryService.class);
         
         ServiceDefTarget target = (ServiceDefTarget) registryService;
         target.setServiceEntryPoint(GWT.getModuleBaseURL() + "../handler/registry.rpc");
         
         this.securityService = (SecurityServiceAsync) GWT.create(SecurityService.class);
         
         target = (ServiceDefTarget) securityService;
         target.setServiceEntryPoint(GWT.getModuleBaseURL() + "../handler/securityService.rpc");
 
         this.heartbeatService = (HeartbeatServiceAsync) GWT.create(HeartbeatService.class);
         target = (ServiceDefTarget) heartbeatService;
         target.setServiceEntryPoint(GWT.getModuleBaseURL() + "../handler/heartbeat.rpc");
 
         FlowPanel base = new FlowPanel();
         base.setStyleName("base");
         base.setWidth("100%");
 
         registryPanel = new SimplePanel();
         activityPanel = new SimplePanel();
         adminPanel = new SimplePanel();
 
         tabPanel = new TabPanel();
         
         rightPanel = new FlowPanel();
         rightPanel.setStyleName("header-right");
 
         FlowPanel header = new FlowPanel();
         header.setStyleName("header");
         header.add(rightPanel);
         header.add(new Image("images/galaxy_small_logo.png"));
 
         base.add(header);
 
         tabPanel.setStyleName("headerTabPanel");
         tabPanel.getDeckPanel().setStyleName("headerTabDeckPanel");
         tabPanel.addTabListener(new TabListener() {
 
             public boolean onBeforeTabSelected(SourcesTabEvents event, int newTab) {
                 return true;
             }
 
             public void onTabSelected(SourcesTabEvents tabPanel, int tab) {
                 if (oldTab != tab && !suppressTabHistory) {
                     History.newItem("tab-" + tab);
                 }
                 oldTab = tab;
             }
         });
         base.add(tabPanel);
         
         browsePanel = new BrowsePanel(this);
         createPageInfo(DEFAULT_PAGE, browsePanel, 0);
         registryPanel.add(browsePanel);
         
         final Galaxy galaxy = this;
         registryService.getUserInfo(new AbstractCallback(browsePanel) {
             public void onSuccess(Object o) {
                 user = (WUser) o;
                 rightPanel.add(new Label(user.getName()));
 
                 ExternalHyperlink logout = new ExternalHyperlink("Logout", GWT.getHostPageBaseURL() + "j_logout");
                 rightPanel.add(logout);
 
                 loadTabs(galaxy);
             }
 
         });
         
         
         Label footer = new Label("Mule Galaxy, Copyright 2008 MuleSource, Inc.");
         footer.setStyleName("footer");
         base.add(footer);
         RootPanel.get().add(base);
         
         createPageInfo("artifact/*", new ArtifactPanel(this), 0);
         createPageInfo("add-artifact", new ArtifactForm(this), 0);
         createPageInfo("new-artifact-version/*", new ArtifactForm(this), 0);
         createPageInfo("add-workspace", new WorkspaceForm(this), 0);
         createPageInfo("manage-workspace", new ManageWorkspacePanel(this), 0);
         createPageInfo("search", new SearchPanel(this), 0);
 
         new HeartbeatTimer(Galaxy.this);
     }
     
     public PageInfo createPageInfo(String token, 
                                    final AbstractComposite composite,
                                    int tab) {
         PageInfo page = new PageInfo(token, tab) {
             public AbstractComposite createInstance() {
                 return composite;
             }
         };
         addPage(page);
         return page;
     }
 
     protected void loadTabs(final Galaxy galaxy) {
         tabPanel.insert(registryPanel, "Registry", 0);
         
         if (hasPermission("VIEW_ACTIVITY")) {
             createPageInfo("tab-1", new ActivityPanel(this), 1);
             tabPanel.insert(activityPanel, "Activity", tabPanel.getWidgetCount());
         }
         
         if (showAdminTab(user)) {
             adminTabIndex = tabPanel.getWidgetCount() ;
             createPageInfo("tab-" + adminTabIndex, new AdministrationPanel(galaxy), adminTabIndex);
             tabPanel.add(adminPanel, "Administration");
         }
         showFirstPage();
     }
     
     protected boolean showAdminTab(WUser user) {
         for (Iterator itr = user.getPermissions().iterator(); itr.hasNext();) {
             String s = (String)itr.next();
             
             if (s.startsWith("MANAGE_")) return true;
         }
         return false;
     }
     
     protected void showFirstPage() {
         // Show the initial screen.
         String initToken = History.getToken();
         if (initToken.length() > 0) {
             onHistoryChanged(initToken);
         } else {
             tabPanel.selectTab(0);
             browsePanel.onShow(Collections.EMPTY_LIST);
         }
     }
 
     public void addPage(PageInfo info) {
         history.put(info.getName(), info);
     }
 
     public void show(PageInfo info, boolean affectHistory) {
         // Don't bother re-displaying the existing sink. This can be an issue
         // in practice, because when the history context is set, our
         // onHistoryChanged() handler will attempt to show the currently-visible
         // sink.
         if (info == curInfo) {
             return;
         }
         suppressTabHistory = true;
         curInfo = info;
 
         // If affectHistory is set, create a new item on the history stack. This
         // will ultimately result in onHistoryChanged() being called. It will
         // call
         // show() again, but nothing will happen because it will request the
         // exact
         // same sink we're already showing.
         if (affectHistory) {
             History.newItem(info.getName());
         }
 
         // Display the page
         history.put(info.getName(), info);
         show(info);
         suppressTabHistory = false;
     }
     
     private void show(PageInfo page) {
         SimplePanel p = (SimplePanel) tabPanel.getWidget(page.getTabIndex());
         
         p.clear();
         
         p.add(page.getInstance());
     }
     public void onHistoryChanged(String token) {
         suppressTabHistory = true;
         if ("".equals(token)) {
             token = DEFAULT_PAGE;
         }
         
         if ("nohistory".equals(token) && curInfo != null)
         {
             suppressTabHistory = false;
             return;
         }
         
         PageInfo page = getPageInfo(token);
         List params = new ArrayList();
         if (page == null) {
             String[] split = token.split("/");
             
             // hack to match "foo/*" style tokens
             int slashIdx = token.lastIndexOf('/');
             if (slashIdx != -1) {
                 page = getPageInfo(token.substring(0, slashIdx) + "/*");
             }
             
             if (page == null) {
                 page = getPageInfo(split[0]);
             }
             
             if (split.length > 1) {
                 for (int i = 1; i < split.length; i++) {
                     params.add(split[i]);
                 }
             }
         }
         
         // hide the previous page
         if (curInfo != null) {
             curInfo.getInstance().onHide();
         }
         
         if (page == null) {
             // went to a page which isn't in our history anymore. go to the first page
             if (curInfo == null) {
                 onHistoryChanged(DEFAULT_PAGE);
             }
         } else {
             curInfo = page;
             
             int idx = page.getTabIndex();
             if (idx >= 0 && idx < tabPanel.getWidgetCount()) {
                 tabPanel.selectTab(page.getTabIndex());
             }
             show(page);
             page.getInstance().onShow(params);
         }
         
         suppressTabHistory = false;
     }
 
     public PageInfo getPageInfo(String token) {
         PageInfo page = (PageInfo) history.get(token);
         return page;
     }
 
     public void setMessageAndGoto(String token, String message) {
         PageInfo pi = getPageInfo(token);
         
         ErrorPanel ep = (ErrorPanel) pi.getInstance();
         
         ep.setMessage(message);
         
         History.newItem(token);
     }
     
     public RegistryServiceAsync getRegistryService() {
         return registryService;
     }
 
     public SecurityServiceAsync getSecurityService() {
         return securityService;
     }
 
     public HeartbeatServiceAsync getHeartbeatService()
     {
         return this.heartbeatService;
     }
 
     public TabPanel getTabPanel() {
         return tabPanel;
     }
 
     public boolean hasPermission(String perm) {
         for (Iterator itr = user.getPermissions().iterator(); itr.hasNext();) {
             String s = (String)itr.next();
             
             if (s.startsWith(perm)) return true;
         }
         return false;
     }
 
     public int getAdminTab() {
         return adminTabIndex;
     }
 
     public void addHistoryListener(String token, AbstractComposite composite) {
         historyListeners.put(token, composite);
     }
 
 }
