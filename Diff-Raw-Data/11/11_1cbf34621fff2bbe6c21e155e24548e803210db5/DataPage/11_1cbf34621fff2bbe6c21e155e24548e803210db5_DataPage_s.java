 package org.geoserver.web.data.tree;
 
 import java.io.Serializable;
import java.util.ArrayList;
 import java.util.List;
 
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
import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
 import org.apache.wicket.markup.html.panel.EmptyPanel;
 import org.apache.wicket.model.Model;
import org.geoserver.catalog.Catalog;
 import org.geoserver.catalog.FeatureTypeInfo;
import org.geoserver.catalog.ResourceInfo;
 import org.geoserver.catalog.WorkspaceInfo;
 import org.geoserver.web.GeoServerBasePage;
 import org.geoserver.web.data.NewDataPage;
 import org.geoserver.web.data.ResourceConfigurationPage;
 
 public class DataPage extends GeoServerBasePage {
 
     DataTreeTable tree;
     CatalogRootNode root;
     private Form buttonForm;
     private AjaxButton addButton;
     private AjaxButton removeButton;
     private AjaxButton configureButton;
 
     public DataPage() {
         WebMarkupContainer treeContainer = new WebMarkupContainer("treeParent");
         treeContainer.setOutputMarkupId(true);
         root = new CatalogRootNode();
         tree = new DataTreeTable("dataTree", new DefaultTreeModel(
                 root), new IColumn[] { new SelectionColumn(),
                 new CatalogNameColumn(), new ActionColumn() });
 
         tree.setRootLess(true);
         tree.getTreeState().setAllowSelectMultiple(false);
         tree.getTreeState().addTreeStateListener(new DataTreeListener());
 
         treeContainer.add(tree);
         add(treeContainer);
 
         buttonForm = new Form("controlForm");
         addButton = new AjaxButton("addChecked") {
             @Override
             protected void onSubmit(AjaxRequestTarget target, Form form) {
                 addButtonClicked(target, form);
             }
         };
         buttonForm.add(addButton);
         removeButton = new AjaxButton("removeChecked") {
             @Override
             protected void onSubmit(AjaxRequestTarget target, Form form) {
                 removeButtonClicked(target, form);
             }
         };
         buttonForm.add(removeButton);
         configureButton = new AjaxButton("configureChecked") {
             @Override
             protected void onSubmit(AjaxRequestTarget target, Form form) {
                 configureButtonClicked(target, form);
             }
         };
         configureButton.add(new SimpleAttributeModifier("onclick",
         "alert('This will allow you mass configure multiple feature types'); return false;"));
 
         buttonForm.add(configureButton);
         add(buttonForm);
         
         updateButtonState();
 
 //        final Catalog catalog = getCatalog();
 //        final List<ResourceInfo> resources = catalog.getResources(ResourceInfo.class);
 //        // feed the ListView with a list whose elements are serializable. The
 //        // index in the resources list is ok.
 //        final int size = resources.size();
 //        List<Integer> resourceIndexes = new ArrayList<Integer>(size);
 //        for (int index = 0; index < size; index++) {
 //            resourceIndexes.add(Integer.valueOf(index));
 //        }
 //
 //        ListView view = new ListView("resources", resourceIndexes) {
 //
 //            private static final long serialVersionUID = 1L;
 //
 //            @Override
 //            protected void populateItem(ListItem item) {
 //                final Integer index = (Integer) item.getModelObject();
 //                final ResourceInfo info = resources.get(index.intValue());
 //
 //                Link link = new Link("resourcelink") {
 //                    private static final long serialVersionUID = 1L;
 //
 //                    @Override
 //                    public void onClick() {
 //                        setResponsePage(new ResourceConfigurationPage(info, false));
 //                    }
 //                };
 //                link.add(new Label("resourcelabel", info.getId()));
 //                item.add(link);
 //            }
 //        };
 //        add(view);
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
 
     class CatalogNameColumn extends AbstractTreeColumn implements IColumn {
 
         public CatalogNameColumn() {
             super(new ColumnLocation(Alignment.MIDDLE, 88, Unit.PROPORTIONAL), "Catalog");
         }
 
         @Override
         public String renderNode(TreeNode node) {
             return ((AbstractCatalogNode) node).getNodeLabel();
         }
 
         @Override
         public Component newCell(MarkupContainer parent, String id, TreeNode node, int level) {
             if (node instanceof UnconfiguredFeatureTypesNode) {
                 return new UnconfiguredFeatureTypesPanel(id, tree, parent,
                         (UnconfiguredFeatureTypesNode) node, level);
             }
             if (node instanceof UnconfiguredFeatureTypeNode) {
                 return new UnconfiguredFeatureTypePanel(id, tree, parent,
                         (AbstractCatalogNode) node, level);
             }
             if (node instanceof ResourceNode) {
                 return new LabelPanel(id, tree, parent, (AbstractCatalogNode) node, level);
             }
             if (node instanceof NewDatastoreNode) {
                 return new NewDataStorePanel(id, tree, parent, (AbstractCatalogNode) node, level);
             } else {
                 return super.newCell(parent, id, node, level);
             }
 
         }
     }
 
     class SelectionColumn extends AbstractColumn {
 
         public SelectionColumn() {
             super(new ColumnLocation(Alignment.LEFT, 24, Unit.PX), "");
         }
 
         public Component newCell(MarkupContainer parent, String id, TreeNode node, int level) {
             AbstractCatalogNode cn = (AbstractCatalogNode) node;
             if (!cn.isSelectable())
                 return new EmptyPanel(id);
             else
                 return new DataPageSelectionPanel(id, node, tree);
         }
 
         public IRenderable newCell(TreeNode node, int level) {
             return null;
         }
 
     }
 
     class ActionColumn extends AbstractColumn {
 
         public ActionColumn() {
             super(new ColumnLocation(Alignment.MIDDLE, 12, Unit.PROPORTIONAL), "");
         }
 
         public Component newCell(MarkupContainer parent, String id, TreeNode node, int level) {
             if (node instanceof UnconfiguredFeatureTypeNode)
                 return new AddConfigPanel(id, (AbstractCatalogNode) node);
             else if (node instanceof AbstractPlaceholderNode)
                 return new EmptyPanel(id);
             else
                 return new EditRemovePanel(id, (AbstractCatalogNode) node);
         }
 
         public IRenderable newCell(TreeNode node, int level) {
             return null;
         }
 
     }
 
     final class DataTreeListener extends TreeAdapter implements Serializable {
 
         @Override
         public void nodeUnselected(TreeNode node) {
             if (!tree.getTreeState().isNodeExpanded(node))
                 tree.getTreeState().expandNode(node);
             else
                 tree.getTreeState().collapseNode(node);
         }
 
         public void nodeSelected(TreeNode selected) {
 
             if (!tree.getTreeState().isNodeExpanded(selected))
                 tree.getTreeState().expandNode(selected);
             else
                 tree.getTreeState().collapseNode(selected);
         }
 
     }
 
     protected TreeNode getWorkspaceNode(TreeNode selected) {
         TreeNode node = selected;
         while (node != null && !(node instanceof WorkspaceNode)) {
             node = node.getParent();
         }
         return node;
     }
 
     class UnconfiguredFeatureTypesPanel extends LinkPanel {
 
         public UnconfiguredFeatureTypesPanel(String id, DataTreeTable tree, MarkupContainer parent,
                 UnconfiguredFeatureTypesNode node, int level) {
             super(id, tree, parent, node, level);
             label.add(new AttributeModifier("class", true, new Model("command")));
         }
 
         @Override
         protected void onClick(AjaxRequestTarget target) {
             ((DataStoreNode) node.getParent()).setUnconfiguredChildrenVisible(true);
             tree.getTreeState().expandNode(
                     (((DataStoreNode) node.getParent()).checkPartialSelection()));
             target.addComponent(tree.getParent());
         }
 
         @Override
         protected ResourceReference getNodeIcon(DataTreeTable tree, TreeNode node) {
             return null;
         }
 
     }
 
     class NewDataStorePanel extends LinkPanel {
 
         public NewDataStorePanel(String id, DataTreeTable tree, MarkupContainer parent,
                 AbstractCatalogNode node, int level) {
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
 
     class UnconfiguredFeatureTypePanel extends LinkPanel {
 
         public UnconfiguredFeatureTypePanel(String id, DataTreeTable tree, MarkupContainer parent,
                 AbstractCatalogNode node, int level) {
             super(id, tree, parent, node, level);
             label.add(new AttributeModifier("class", true, new Model("unconfiguredLayer")));
         }
 
         /**
          * Creates a new, detached from the catalog, {@link FeatureTypeInfo} and
          * pass it through to {@link ResourceConfigurationPage}
          */
         @Override
         protected void onClick(AjaxRequestTarget target) {
             EditRemovePanel.edit(this, (AbstractCatalogNode) node);
         }
     }
     
     class DataPageSelectionPanel extends SelectionPanel {
 
         public DataPageSelectionPanel(String id, TreeNode node,
                 DataTreeTable tree) {
             super(id, node, tree);
         }
 
         @Override
         protected void onCheckboxClick(AjaxRequestTarget target) {
             // change the state of the current node
             catalogNode.nextSelectionState();
             icon.setImageResourceReference(getImageResource(catalogNode));
             
             AbstractCatalogNode lastChangedParent = catalogNode.getParent().checkPartialSelection();
 
             // force the tree refresh
             tree.refresh(lastChangedParent);
             target.addComponent(tree.getParent());
             
             updateButtonState();
             target.addComponent(removeButton);
             target.addComponent(configureButton);
             target.addComponent(addButton);
         }
 
        
         
     }
     
     void updateButtonState() {
         // button state update
         List<AbstractCatalogNode> selection =  root.getSelectedNodes();
         boolean configured = false;
         boolean unconfigured = false;
         for (AbstractCatalogNode node : selection) {
             if(node instanceof UnconfiguredFeatureTypeNode) {
                 unconfigured = true;
             } else {
                 configured = true;
             }
             
         }
         removeButton.setEnabled(configured);
         configureButton.setEnabled(unconfigured);
         addButton.setEnabled(unconfigured);
     }
     
    
 
 }
