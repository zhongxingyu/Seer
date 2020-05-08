 package org.mule.galaxy.web.client.artifact;
 
 import java.util.Iterator;
 
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.ChangeListener;
 import com.google.gwt.user.client.ui.ClickListener;
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.Hyperlink;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.ListBox;
 import com.google.gwt.user.client.ui.SourcesTabEvents;
 import com.google.gwt.user.client.ui.TabListener;
 import com.google.gwt.user.client.ui.TabPanel;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.Widget;
 import org.mule.galaxy.web.client.AbstractComposite;
 import org.mule.galaxy.web.client.RegistryPanel;
 import org.mule.galaxy.web.client.admin.AdministrationPanel;
 import org.mule.galaxy.web.client.util.InlineFlowPanel;
 import org.mule.galaxy.web.rpc.AbstractCallback;
 import org.mule.galaxy.web.rpc.ArtifactGroup;
 import org.mule.galaxy.web.rpc.ArtifactVersionInfo;
 import org.mule.galaxy.web.rpc.ExtendedArtifactInfo;
 import org.mule.galaxy.web.rpc.SecurityService;
 
 /**
  * Contains:
  * - BasicArtifactInfo
  * - Service dependencies
  * - Depends on...
  * - Comments
  * - Governance tab
  *   (with history)
  * - View Artiact
  */
 public class ArtifactPanel extends AbstractComposite {
 
     private RegistryPanel registryPanel;
     private TabPanel artifactTabs;
     private ExtendedArtifactInfo info;
     private ArtifactGroup group;
     private VerticalPanel panel;
     private int selectedTab;
     private ListBox versionLB;
 
     public ArtifactPanel(RegistryPanel registryPanel, String artifactId) {
         this(registryPanel, artifactId, -1);
     }
     
     public ArtifactPanel(RegistryPanel registryPanel, String artifactId, int selectedTab) {
         this(registryPanel, selectedTab);
         
         registryPanel.getRegistryService().getArtifact(artifactId, new AbstractCallback(registryPanel) { 
             public void onSuccess(Object o) {
                 group = (ArtifactGroup) o;
                 info = (ExtendedArtifactInfo) group.getRows().get(0);
                 
                 init();
             }
         });
     }
     
     protected ArtifactPanel(RegistryPanel registryPanel, int selectedTab) {
         this.registryPanel = registryPanel;
         this.selectedTab = selectedTab;
         
         panel = new VerticalPanel();
         panel.setWidth("100%");
         
         artifactTabs = new TabPanel();
         artifactTabs.setStyleName("artifactTabPanel");
         artifactTabs.getDeckPanel().setStyleName("artifactTabDeckPanel");
         
         panel.add(artifactTabs);
         
         initWidget(panel);
     }
     
     private void init() {
         FlowPanel artifactTitle = new FlowPanel();
         artifactTitle.setStyleName("artifact-title-base");
         artifactTitle.add(newLabel(info.getPath(), "artifact-path"));
         
         FlexTable titleTable = new FlexTable();
         titleTable.setStyleName("artifact-title");
         titleTable.setWidget(0, 0, newLabel(info.getName(), "artifact-name"));
         
         ArtifactVersionInfo defaultVersion = null;
         versionLB = new ListBox();
         for (Iterator itr = info.getVersions().iterator(); itr.hasNext();) {
             ArtifactVersionInfo v = (ArtifactVersionInfo)itr.next();
             
             versionLB.addItem(v.getVersionLabel(), v.getId());
             if (v.isDefault()) {
                 defaultVersion = v;
                 versionLB.setSelectedIndex(versionLB.getItemCount()-1);
             }
         }
         versionLB.addChangeListener(new ChangeListener() {
 
             public void onChange(Widget arg0) {
                 viewNewVersion();
             }
             
         });
         titleTable.setWidget(0, 1, versionLB);
         
         Image img = new Image("images/feed-icon-14x14.png");
         img.addClickListener(new ClickListener() {
 
             public void onClick(Widget sender) {
                 Window.open(info.getArtifactFeedLink(), null, null);
             }
             
         });
         titleTable.setWidget(0, 2, img);
         
         artifactTitle.add(titleTable);
         
         panel.insert(artifactTitle, 0);
         
         initTabs(defaultVersion);
     }
 
     protected void viewNewVersion() {
         int idx = versionLB.getSelectedIndex();
         String id = versionLB.getValue(idx);
         
         for (Iterator itr = info.getVersions().iterator(); itr.hasNext();) {
             ArtifactVersionInfo avi = (ArtifactVersionInfo)itr.next();               
             
             if (avi.getId().equals(id)) {
                 artifactTabs.clear();
                 initTabs(avi);
                 return;
             }
         }
     }
 
     private void initTabs(ArtifactVersionInfo version) {
         artifactTabs.add(new ArtifactInfoPanel(registryPanel, group, info, version), "Info");
         artifactTabs.add(new GovernancePanel(registryPanel, version), "Governance");
         artifactTabs.add(new HistoryPanel(registryPanel, info), "History");
         if (registryPanel.getGalaxy().hasPermission("MANAGE_GROUPS")) {
             artifactTabs.add(new ItemGroupPermissionPanel(registryPanel, info.getId(), SecurityService.ARTIFACT_PERMISSIONS), "Security");
         }
         
         if (selectedTab > -1) {
             artifactTabs.selectTab(selectedTab);
         } else {
             artifactTabs.selectTab(0);
         }
 
         artifactTabs.addTabListener(new TabListener() {
 
             public boolean onBeforeTabSelected(SourcesTabEvents arg0, int arg1) {
                 return true;
             }
 
             public void onTabSelected(SourcesTabEvents events, int tab) {
                 AbstractComposite composite = (AbstractComposite) artifactTabs.getWidget(tab);
                 
                 composite.onShow();
             }
             
         });
     }
 
     public void onShow() {
         Hyperlink wkspcLink = new Hyperlink();
         wkspcLink.setHTML("&laquo; Back to Workspace");
         wkspcLink.addClickListener(new ClickListener() {
             public void onClick(Widget arg0) {
                 registryPanel.onShow();
             }
         });
         
         registryPanel.setTop(wkspcLink);
         registryPanel.hideArtifactTypes();
     }
 }
