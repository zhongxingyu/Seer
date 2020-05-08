 package org.mule.galaxy.web.client;
 
 import com.google.gwt.user.client.History;
 import com.google.gwt.user.client.ui.ClickListener;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.Hyperlink;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.TreeItem;
 import com.google.gwt.user.client.ui.TreeListener;
 import com.google.gwt.user.client.ui.Widget;
 
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Set;
 
 import org.mule.galaxy.web.client.util.ColumnView;
 import org.mule.galaxy.web.client.util.Toolbox;
 import org.mule.galaxy.web.client.workspace.EditWorkspacePanel;
 import org.mule.galaxy.web.client.workspace.ManageWorkspacePanel;
 import org.mule.galaxy.web.rpc.AbstractCallback;
 import org.mule.galaxy.web.rpc.RegistryServiceAsync;
 import org.mule.galaxy.web.rpc.WArtifactType;
 import org.mule.galaxy.web.rpc.WWorkspace;
 
 public class RegistryPanel extends AbstractMenuPanel {
 
     private Set artifactTypes;
     private Toolbox artifactTypesBox;
     private String workspaceId;
     private Collection workspaces;
     private RegistryServiceAsync service;
     private WorkspacePanel workspacePanel;
     private Toolbox workspaceBox;
     protected ColumnView cv;
     private FlowPanel currentTopPanel;
     private FlowPanel browsePanel;
     private FlowPanel searchingPanel;
     private SearchPanel searchPanel;
     
     public RegistryPanel(Galaxy galaxy) {
         super(galaxy);
         this.service = galaxy.getRegistryService();
         
         workspaceBox = new Toolbox(false);
         workspaceBox.setTitle("Workspaces");
         
         Image addImg = new Image("images/add_obj.gif");
         MenuPanelPageInfo page = createPageInfo("add-artifact", new ArtifactForm(this));
         addImg.addClickListener(createClickListener(page));
         
         final RegistryPanel registryPanel = this;
         
         Image addWkspcImg = new Image("images/fldr_obj.gif");
         ClickListener addWkspcListener = new ClickListener() {
             public void onClick(Widget w) {
                 final TreeItem item = cv.getSelectedItem();
                 final String parentId = item != null ? (String) item.getUserObject() : null;
 
                 String id = "add-workspace";
                 if (parentId != null) {
                     id += "-" + workspaceId;
                 }
                 registryPanel.setMain(new EditWorkspacePanel(registryPanel, workspaces, parentId));
                ((Hyperlink) w).setTargetHistoryToken(id);
             }            
         };
         addWkspcImg.addClickListener(addWkspcListener);
 
         Image editWkspcImg = new Image("images/editor_area.gif");
         ClickListener editWkspcListener = new ClickListener() {
             public void onClick(Widget w) {
                 String id = "manage-workspace-" + workspaceId;
                 createPageInfo(id, new ManageWorkspacePanel(registryPanel, workspaces, workspaceId, getWorkspace(workspaceId)));
                ((Hyperlink) w).setTargetHistoryToken(id);
             }
         };
         editWkspcImg.addClickListener(editWkspcListener);
 
         Toolbox topMenuLinks = new Toolbox(false);
         topMenuLinks.add(asHorizontal(addImg, new Label(" "), new Hyperlink("Add Artifact", "add-artifact")));
         
         Hyperlink hl = new Hyperlink("Add Workspace", "add-workspace");
         hl.addClickListener(addWkspcListener);
         topMenuLinks.add(asHorizontal(addWkspcImg, new Label(" "), hl));
         
         hl = new Hyperlink("Manage Workspace", "manage-workspace");
         hl.addClickListener(editWkspcListener);
         topMenuLinks.add(asHorizontal(editWkspcImg, new Label(" "), hl));
         
         addMenuItem(topMenuLinks);
         
         FlowPanel browseToolbar = new FlowPanel();
         browseToolbar.setStyleName("toolbar");
         
         Hyperlink searchLink = new Hyperlink("Search", "search");
         searchLink.addClickListener(new ClickListener() {
             public void onClick(Widget w) {
                 currentTopPanel = searchingPanel;
                 setTop(searchingPanel);
             }
         });
         browseToolbar.add(searchLink);
         
         //addMenuItem(workspaceBox);
         workspacePanel = new WorkspacePanel(registryPanel);
         
         searchingPanel = new FlowPanel();
         FlowPanel searchToolbar = new FlowPanel();
         searchToolbar.setStyleName("toolbar");
         Hyperlink browseLink = new Hyperlink("Browse Workspaces", "browse");
         browseLink.addClickListener(new ClickListener() {
             public void onClick(Widget w){
                 currentTopPanel = browsePanel;
                 setTop(browsePanel);
                 searchPanel.clear();
                 refreshArtifacts();
             }
         });
         searchToolbar.add(browseLink);
         searchingPanel.add(searchToolbar);
         searchPanel = new SearchPanel(this);
         searchingPanel.add(searchPanel);
         
         browsePanel = new FlowPanel(); 
         cv = new ColumnView();
         browsePanel.add(browseToolbar);
         browsePanel.add(cv);
         currentTopPanel = browsePanel;
         setTop(browsePanel);
 
         refreshWorkspaces();
         
         cv.addTreeListener(new TreeListener() {
             public void onTreeItemSelected(TreeItem ti) {
                 setActiveWorkspace((String) ti.getUserObject());
             }
 
             public void onTreeItemStateChanged(TreeItem ti) {
             }
         });
         
         artifactTypesBox = new Toolbox(false);
         artifactTypesBox.setTitle("By Artifact Type");
     }
 
     protected WWorkspace getWorkspace(String workspaceId) {
         return getWorkspace(workspaceId, workspaces);
     }
 
     private WWorkspace getWorkspace(String workspaceId2, Collection workspaces2) {
        if (workspaces2 == null) return null;
        
         for (Iterator itr = workspaces2.iterator(); itr.hasNext();) {
             WWorkspace w = (WWorkspace)itr.next();
             
             if (w.getId().equals(workspaceId2)) {
                 return w;
             }
             
             WWorkspace child = getWorkspace(workspaceId2, w.getWorkspaces());
             if (child != null) return child;
         }
         return null;
     }
 
     /**
      * We've gone back, or browsed to this tab again.
      * {@inheritDoc}
      */
     public void onShow() {
         super.onShow();
 
         refreshArtifacts();
         refreshWorkspaces();
         showArtifactTypes();
         refreshArtifactTypes();
         showSearchOrBrowse();
     }
 
     private ClickListener createClickListener(final MenuPanelPageInfo page) {
         return new ClickListener() {
             public void onClick(Widget w) {
                 History.newItem(page.getName());
             }
         };
     }
 
     public void refreshWorkspaces() {
         final TreeItem treeItem = new TreeItem();
         
         // Load the workspaces into a tree on the left
         service.getWorkspaces(new AbstractCallback(this) {
 
             public void onSuccess(Object o) {
                 workspaces = (Collection) o;
                 
                 initWorkspaces(treeItem, workspaces);
 
                 TreeItem child = treeItem.getChild(0);
                 workspaceId = (String) child.getUserObject();
                 cv.setRootItem(treeItem);
             }
         });
     }
 
     private void refreshArtifactTypes() {
         artifactTypes = new HashSet();
         artifactTypesBox.clear();
         artifactTypesBox.add(new Label("Loading..."));
         
         service.getArtifactTypes(new AbstractCallback(this) {
 
             public void onSuccess(Object o) {
                 artifactTypesBox.clear();
                 Collection workspaces = (Collection) o;
                 
                 for (Iterator itr = workspaces.iterator(); itr.hasNext();) {
                     final WArtifactType at = (WArtifactType) itr.next();
                     
                     Hyperlink hl = new Hyperlink(at.getDescription(), at.getId());
                     hl.addClickListener(new ClickListener() {
                         public void onClick(Widget w) {
                             String style = w.getStyleName();
                             if ("unselected-link".equals(style)) {
                                 w.setStyleName("selected-link");
                                 addArtifactTypeFilter(at.getId());
                             } else {
                                 w.setStyleName("unselected-link");
                                 removeArtifactTypeFilter(at.getId());
                             }
                             
                         }
                     });
                     hl.setStyleName("unselected-link");
                     artifactTypesBox.add(hl, false);
                 }
             }
         });
     }
     
     private void initWorkspaces(TreeItem ti, Collection workspaces) {
         for (Iterator itr = workspaces.iterator(); itr.hasNext();) {
             WWorkspace wi = (WWorkspace) itr.next();
             
             TreeItem treeItem = ti.addItem(wi.getName());
             treeItem.setUserObject(wi.getId());
             
             Collection children = wi.getWorkspaces();
             if (children != null) {
                 initWorkspaces(treeItem, children);
             }
         }
     }
     
     public void addArtifactTypeFilter(String id) {
         artifactTypes.add(id);
         refreshArtifacts();
     }
 
     public void removeArtifactTypeFilter(String id) {
         artifactTypes.remove(id);
         refreshArtifacts();
     }
 
     public void setActiveWorkspace(String workspaceId) {
         this.workspaceId = workspaceId;
         refreshArtifacts();
     }
     
     public void refreshArtifacts() {
         setMain(workspacePanel);
         workspacePanel.reloadArtifacts();
     }
 
     public Set getArtifactTypes() {
         return artifactTypes;
     }
 
     public String getWorkspaceId() {
         return workspaceId;
     }
 
     public Collection getWorkspaces() {
         return workspaces;
     }
     
     public SearchPanel getSearchPanel() {
         return searchPanel;
     }
     
     public void showSearchOrBrowse() {
         setTop(currentTopPanel);
     }
     
     public void showArtifactTypes() {
         addMenuItem(artifactTypesBox);
     }
     
     public void hideArtifactTypes() {
         removeMenuItem(artifactTypesBox);
     }
 
     // TODO
     protected int getErrorPanelPosition() {
         return 0;
     }
 }
