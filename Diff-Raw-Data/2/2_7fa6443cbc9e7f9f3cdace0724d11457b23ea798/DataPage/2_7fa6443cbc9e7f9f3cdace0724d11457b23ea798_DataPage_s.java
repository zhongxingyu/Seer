 /* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
  * This code is licensed under the GPL 2.0 license, available at the root
  * application directory.
  */
 package org.geoserver.web.data.tree;
 
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.swing.tree.DefaultTreeModel;
 import javax.swing.tree.TreeNode;
 
 import org.apache.wicket.AttributeModifier;
 import org.apache.wicket.Component;
 import org.apache.wicket.MarkupContainer;
 import org.apache.wicket.ResourceReference;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.ajax.markup.html.form.AjaxButton;
 import org.apache.wicket.behavior.SimpleAttributeModifier;
 import org.apache.wicket.extensions.markup.html.tree.table.AbstractColumn;
 import org.apache.wicket.extensions.markup.html.tree.table.AbstractTreeColumn;
 import org.apache.wicket.extensions.markup.html.tree.table.ColumnLocation;
 import org.apache.wicket.extensions.markup.html.tree.table.IColumn;
 import org.apache.wicket.extensions.markup.html.tree.table.IRenderable;
 import org.apache.wicket.extensions.markup.html.tree.table.ColumnLocation.Alignment;
 import org.apache.wicket.extensions.markup.html.tree.table.ColumnLocation.Unit;
 import org.apache.wicket.markup.html.WebMarkupContainer;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.panel.EmptyPanel;
 import org.apache.wicket.markup.html.tree.ITreeState;
 import org.apache.wicket.model.Model;
 import org.geoserver.catalog.DataStoreInfo;
 import org.geoserver.catalog.FeatureTypeInfo;
 import org.geoserver.catalog.StoreInfo;
 import org.geoserver.catalog.WorkspaceInfo;
 import org.geoserver.web.GeoServerApplication;
 import org.geoserver.web.GeoServerBasePage;
 import org.geoserver.web.data.DataStorePanelInfo;
 import org.geoserver.web.data.NewDataPage;
 import org.geoserver.web.data.ResourceConfigurationPage;
 import org.geotools.data.DataAccessFactory;
 import org.geotools.util.logging.Logging;
 
 /**
  * The one stop shop UI for data configuration, handles all data oriented
  * resources in a single tree and allows for modification of it thru the various
  * sub-panels nested into this one
  * 
  * @author Andrea Aime - TOPP
  * 
  */
 public class DataPage extends GeoServerBasePage {
 
     static final Logger LOGGER = Logging.getLogger(DataPage.class);
 
     DataTreeTable tree;
 
     CatalogRootNode root;
 
     private Form buttonForm;
 
     private AjaxButton addButton;
 
     private AjaxButton removeButton;
 
     private AjaxButton configureButton;
 
     public DataPage() {
         // build a parent so that ajax requests can update the tree
         // (repeater like objects cannot act as refresh targets
         WebMarkupContainer treeContainer = new WebMarkupContainer("treeParent");
         treeContainer.setOutputMarkupId(true);
         add(treeContainer);
 
         // build the tree
         root = new CatalogRootNode();
         tree = new DataTreeTable("dataTree", new DefaultTreeModel(root), new IColumn[] {
                 new SelectionColumn(), new DataPageTreeColumn(), new ActionColumn() }) {
             @Override
             protected void onNodeLinkClicked(AjaxRequestTarget target, TreeNode node) {
                 DataPage.this.onNodeLinkClicked(target, node);
             }
         };
         tree.setRootLess(true);
         tree.getTreeState().setAllowSelectMultiple(false);
         treeContainer.add(tree);
 
         // build the button bar
         buttonForm = new Form("controlForm");
         addButton = new AjaxButton("addChecked") {
             @Override
             protected void onSubmit(AjaxRequestTarget target, Form form) {
                 addButtonClicked(target, form);
             }
         };
        configureButton.add(new SimpleAttributeModifier("onclick",
                 "alert('This will allow to auto configure multiple layers'); return false;"));
         buttonForm.add(addButton);
         removeButton = new AjaxButton("removeChecked") {
             @Override
             protected void onSubmit(AjaxRequestTarget target, Form form) {
                 removeButtonClicked(target, form);
             }
         };
         removeButton
         .add(new SimpleAttributeModifier("onclick",
                 "alert('This will allow you mass configure delete multiple resources'); return false;"));
         buttonForm.add(removeButton);
         configureButton = new AjaxButton("configureChecked") {
             @Override
             protected void onSubmit(AjaxRequestTarget target, Form form) {
                 configureButtonClicked(target, form);
             }
         };
         configureButton
                 .add(new SimpleAttributeModifier("onclick",
                         "alert('This will allow you mass configure multiple feature types'); return false;"));
         buttonForm.add(configureButton);
         add(buttonForm);
 
         // refresh the state of the button based on the current selection
         updateButtonState();
     }
     
     protected void onNodeLinkClicked(AjaxRequestTarget target, TreeNode node) {
         // toggle expansion
         ITreeState ts = tree.getTreeState();
         if (ts.isNodeExpanded(node))
             ts.collapseNode(node);
         else
             ts.expandNode(node);
 
         // select the current workspace to provide some context
         TreeNode ws = getWorkspaceNode(node);
         for (CatalogNode child : root.childNodes()) {
             if (!(child.equals(ws)))
                 ts.collapseNode(child);
         }
         ts.selectNode(ws, true);
         
         // make the proper ajax repaint
         tree.updateTree(target);
     }
 
     /**
      * Returns the proper icon for the specified store
      * @param info
      * @return
      */
     protected ResourceReference getStoreIcon(StoreInfo info) {
         if (info instanceof DataStoreInfo) {
             DataStoreInfo ds = (DataStoreInfo) info;
 
             // look for the associated panel info if there is one
             List<DataStorePanelInfo> infos = getGeoServerApplication().getBeansOfType(
                     DataStorePanelInfo.class);
             for (DataStorePanelInfo panelInfo : infos) {
                 try {
                     // we know if a factory is the right one if it can process
                     // the params
                     DataAccessFactory factory = (DataAccessFactory) panelInfo.getFactoryClass()
                             .newInstance();
                     if (factory.canProcess(ds.getConnectionParameters())) {
                         return new ResourceReference(panelInfo.getIconBase(), panelInfo.getIcon());
                     }
                 } catch (Exception e) {
                     LOGGER.log(Level.WARNING,
                             "Could not create an instance of the data store factory "
                                     + panelInfo.getFactoryClass());
                 }
             }
 
             // fall back on generic vector icon otherwise
             return new ResourceReference(GeoServerApplication.class, "img/icons/geosilk/vector.png");
 
         } else {
             // use a generic raster icon for the moment
             return new ResourceReference(GeoServerApplication.class, "img/icons/geosilk/raster.png");
         }
     }
 
     protected void configureButtonClicked(AjaxRequestTarget target, Form form) {
         // TODO Auto-generated method stub
 
     }
 
     protected void removeButtonClicked(AjaxRequestTarget target, Form form) {
         // TODO Auto-generated method stub
 
     }
 
     protected void addButtonClicked(AjaxRequestTarget target, Form form) {
         // TODO Auto-generated method stub
 
     }
 
     /**
      * Updates the state of the buttons in the bar below the
      */
     void updateButtonState() {
         // button state update
         List<CatalogNode> selection = root.getSelectedNodes();
         boolean configured = false;
         boolean unconfigured = false;
         for (CatalogNode node : selection) {
             if (node instanceof UnconfiguredResourceNode) {
                 unconfigured = true;
             } else {
                 configured = true;
             }
 
         }
         removeButton.setEnabled(configured);
         configureButton.setEnabled(unconfigured);
         addButton.setEnabled(unconfigured);
     }
 
     /**
      * The column with the actual tree. It will insert all our custom components
      * into it.
      * 
      * @author Andrea Aime - TOPP
      * 
      */
     class DataPageTreeColumn extends AbstractTreeColumn implements IColumn {
 
         public DataPageTreeColumn() {
             super(new ColumnLocation(Alignment.MIDDLE, 88, Unit.PROPORTIONAL), "Catalog");
         }
 
         @Override
         public String renderNode(TreeNode node) {
             return ((CatalogNode) node).getNodeLabel();
         }
 
         @Override
         public Component newCell(MarkupContainer parent, String id, TreeNode node, int level) {
             if (node instanceof UnconfiguredResourcesNode) 
                 return new UnconfiguredResourcesPanel(id, tree, parent,
                         (UnconfiguredResourcesNode) node, level);
             if (node instanceof UnconfiguredResourceNode)
                 return new UnconfiguredResourcePanel(id, tree, parent, (CatalogNode) node, level);
             if (node instanceof ResourceNode)
                 return new ResourcePanel(id, tree, parent, (CatalogNode) node, level);
             if (node instanceof NewDatastoreNode)
                 return new NewDataStorePanel(id, tree, parent, (CatalogNode) node, level);
             if (node instanceof StoreNode) 
                 return new StorePanel(id, tree, parent, (CatalogNode) node, level);
             // else
             return super.newCell(parent, id, node, level);
         }
     }
 
     /**
      * The column with the check marks used for selection. Uses a selection
      * panel for selectable nodes, an empty one of the others.
      * 
      * @author Andrea Aime - TOPP
      * 
      */
     class SelectionColumn extends AbstractColumn {
 
         public SelectionColumn() {
             super(new ColumnLocation(Alignment.LEFT, 24, Unit.PX), "");
         }
 
         public Component newCell(MarkupContainer parent, String id, TreeNode node, int level) {
             CatalogNode cn = (CatalogNode) node;
             if (!cn.isSelectable())
                 return new EmptyPanel(id);
             else
                 return new DataPageSelectionPanel(id, node, tree);
         }
 
         public IRenderable newCell(TreeNode node, int level) {
             return null;
         }
 
     }
 
     /**
      * The column that contains per row action buttons
      * 
      * @author Andrea Aime - TOPP
      * 
      */
     class ActionColumn extends AbstractColumn {
 
         public ActionColumn() {
             super(new ColumnLocation(Alignment.RIGHT, 50, Unit.PX), "");
         }
 
         public Component newCell(MarkupContainer parent, String id, TreeNode node, int level) {
             if (node instanceof UnconfiguredResourceNode)
                 return new AddConfigPanel(id, (CatalogNode) node);
             else if (node instanceof PlaceholderNode)
                 return new EmptyPanel(id);
             else
                 return new EditRemovePanel(id, (CatalogNode) node);
         }
 
         public IRenderable newCell(TreeNode node, int level) {
             return null;
         }
 
     }
 
     protected TreeNode getWorkspaceNode(TreeNode selected) {
         TreeNode node = selected;
         while (node != null && !(node instanceof WorkspaceNode)) {
             node = node.getParent();
         }
         return node;
     }
 
     /**
      * The tree item that handles the "add data" link and forces the expansion
      * of the unconfigured feature types for data stores that do have one or
      * more unconfigured elements
      * 
      * @author Andrea Aime - TOPP
      * 
      */
     class UnconfiguredResourcesPanel extends LinkPanel {
 
         public UnconfiguredResourcesPanel(String id, DataTreeTable tree, MarkupContainer parent,
                 UnconfiguredResourcesNode node, int level) {
             super(id, tree, parent, node, level);
             label.add(new AttributeModifier("class", true, new Model("command")));
         }
 
         @Override
         protected void onClick(AjaxRequestTarget target) {
             ((StoreNode) node.getParent()).setUnconfiguredChildrenVisible(true);
             tree.refresh(node.getParent());
             tree.refresh((node.getParent().checkPartialSelection()));
             tree.updateTree(target);
         }
 
         @Override
         protected ResourceReference getNodeIcon(DataTreeTable tree, TreeNode node) {
             return null;
         }
 
     }
 
     /**
      * The custom component handling the unconfigured feature types
      * 
      * @author Andrea Aime - TOPP
      * @TODO change this back to a {@link LabelPanel}, we have the buttons to
      *       handle this
      */
     class UnconfiguredResourcePanel extends LabelPanel {
 
         public UnconfiguredResourcePanel(String id, DataTreeTable tree, MarkupContainer parent,
                 CatalogNode node, int level) {
             super(id, tree, parent, node, level);
             label.add(new AttributeModifier("class", true, new Model("unconfiguredLayer")));
         }
 
         @Override
         protected ResourceReference getNodeIcon(DataTreeTable tree, TreeNode node) {
             return null;
         }
     }
     
     /**
      * The custom component handles the datastores (with proper icon)
      * 
      * @author Andrea Aime - TOPP
      * @TODO change this back to a {@link LabelPanel}, we have the buttons to
      *       handle this
      */
     class StorePanel extends LinkPanel {
 
         public StorePanel(String id, DataTreeTable tree, MarkupContainer parent,
                 CatalogNode node, int level) {
             super(id, tree, parent, node, level);
         }
         
         @Override
         protected ResourceReference getNodeIcon(DataTreeTable tree, TreeNode node) {
             final CatalogNode cn = (CatalogNode) node;
             return getStoreIcon((StoreInfo) cn.getModel());
         }
 
         /**
          * Creates a new, detached from the catalog, {@link FeatureTypeInfo} and
          * pass it through to {@link ResourceConfigurationPage}
          */
         @Override
         protected void onClick(AjaxRequestTarget target) {
             onNodeLinkClicked(target, node);
         }
     }
 
     /**
      * The custom tree components handling the addition of a new datastore in
      * the current workspace
      * 
      * @author Andrea Aime - TOPP
      * 
      */
     class NewDataStorePanel extends LinkPanel {
 
         public NewDataStorePanel(String id, DataTreeTable tree, MarkupContainer parent,
                 CatalogNode node, int level) {
             super(id, tree, parent, node, level);
             label.add(new AttributeModifier("class", true, new Model("command")));
         }
 
         @Override
         protected void onClick(AjaxRequestTarget target) {
             final WorkspaceInfo workspace = ((NewDatastoreNode) node).getModel();
             final String workspaceId = workspace.getId();
             setResponsePage(new NewDataPage(workspaceId));
         }
 
         @Override
         protected ResourceReference getNodeIcon(DataTreeTable tree, TreeNode node) {
             return null;
         }
     }
 
     /**
      * The {@link SelectionPanel} actually interacting with this page. Computes
      * the new state of the clicked item, propagates the selection state
      * updates, changes the button selection state configuration and notifies
      * ajax of what components need repainting.
      * 
      * @author Andrea Aime - TOPP
      * 
      */
     class DataPageSelectionPanel extends SelectionPanel {
 
         public DataPageSelectionPanel(String id, TreeNode node, DataTreeTable tree) {
             super(id, node, tree);
         }
 
         @Override
         protected void onCheckboxClick(AjaxRequestTarget target) {
             // change the state of the current node
             catalogNode.nextSelectionState();
             icon.setImageResourceReference(getImageResource(catalogNode));
 
             CatalogNode lastChangedParent = catalogNode.getParent().checkPartialSelection();
 
             // force the tree refresh
             tree.refresh(lastChangedParent);
             target.addComponent(tree.getParent());
 
             updateButtonState();
             target.addComponent(removeButton);
             target.addComponent(configureButton);
             target.addComponent(addButton);
         }
     }
 
     /**
      * A plain resource, just handles the icon integration
      * 
      * @author Andrea Aime - TOPP
      * 
      */
     class ResourcePanel extends LabelPanel {
 
         public ResourcePanel(String id, DataTreeTable tree, MarkupContainer parent,
                 CatalogNode node, int level) {
             super(id, tree, parent, node, level);
         }
 
         @Override
         protected ResourceReference getNodeIcon(DataTreeTable tree, TreeNode node) {
             // a regular resource does not need an icon
             if (node.getParent() instanceof DataStoreNode
                     || node.getParent() instanceof CoverageStoreNode) {
                 return null;
             } else {
                 // but if the resource it's a collapsed datastore or coverage
                 // store then yes,
                 // we need to get the proper icon
                 ResourceNode rn = (ResourceNode) node;
                 return getStoreIcon(getCatalog().getDataStoreByName(rn.getStoreName()));
             }
         }
     }
 
 }
