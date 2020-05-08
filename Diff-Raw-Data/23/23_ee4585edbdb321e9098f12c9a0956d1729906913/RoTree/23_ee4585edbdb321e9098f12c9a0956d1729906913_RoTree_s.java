 package pl.psnc.dl.wf4ever.portal.pages.ro.folderviewer.components;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.TreeModel;
 import javax.swing.tree.TreeNode;
 
 import org.apache.wicket.AttributeModifier;
 import org.apache.wicket.Component;
 import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.extensions.markup.html.tree.Tree;
 import org.apache.wicket.markup.html.IHeaderResponse;
 import org.apache.wicket.markup.html.WebMarkupContainer;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.request.resource.JavaScriptResourceReference;
 import org.apache.wicket.request.resource.PackageResourceReference;
 import org.apache.wicket.request.resource.ResourceReference;
 import org.purl.wf4ever.rosrs.client.Thing;
 
 import pl.psnc.dl.wf4ever.portal.model.RoTreeModel;
 import pl.psnc.dl.wf4ever.portal.pages.ro.folderviewer.components.behaviours.ITreeListener;
 
 /**
  * An extended {@link Tree}.
  * 
  * @author piotrhol
  * 
  */
 public class RoTree extends Tree {
 
     /** id. */
     private static final long serialVersionUID = 7615686756338665378L;
 
     /** Reference to the css file. */
     private static final ResourceReference CSS = new PackageResourceReference(RoTree.class, "tree.css");
     /** Reference to the image file. */
     private static final ResourceReference TREE_IMAGE = new PackageResourceReference(RoTree.class, "tree-images.png");
     /** Reference to the js file. */
     private static final JavaScriptResourceReference treeClassRefernce = new JavaScriptResourceReference(RoTree.class,
             "roTree.js");
     /** Tree listeners. */
     private List<ITreeListener> treeListenersList;
 
 
     /**
      * Constructor.
      * 
      * @param id
      *            wicket id
      * @param model
      *            tree model
      */
     public RoTree(String id, IModel<? extends TreeModel> model) {
         super(id, model);
         treeListenersList = new ArrayList<ITreeListener>();
 
         add(new AbstractDefaultAjaxBehavior() {
 
             private static final long serialVersionUID = 1L;
 
 
             @Override
             protected void respond(AjaxRequestTarget target) {
             }
 
 
             @Override
             public void renderHead(Component component, IHeaderResponse response) {
                 super.renderHead(component, response);
                 response.renderJavaScriptReference(treeClassRefernce);
                 response.renderOnDomReadyJavaScript("test_tree_reload_sortable();");
                 response.renderOnDomReadyJavaScript("test_tree_reload();");
             }
         });
 
     }
 
 
     /**
      * Register a new tree listener.
      * 
      * @param listener
      *            listener
      */
     public void addTreeListeners(ITreeListener listener) {
         treeListenersList.add(listener);
     }
 
 
     @Override
     public RoTreeModel getModelObject() {
         return (RoTreeModel) super.getModelObject();
     }
 
 
     @Override
     protected ResourceReference getCSS() {
 
         return CSS;
     }
 
 
     @Override
     protected ResourceReference getNodeIcon(TreeNode node) {
         return super.getNodeIcon(node);
     }
 
 
     @Override
     protected void onConfigure() {
         super.onConfigure();
         getTreeState().collapseAll();
         if (getModelObject() != null) {
             getTreeState().expandNode(getModelObject().getRoot());
         }
     }
 
 
     @Override
     protected String renderNode(TreeNode node) {
         Thing thing = (Thing) ((DefaultMutableTreeNode) node).getUserObject();
         return thing.getName();
     }
 
 
     @Override
     protected void populateTreeItem(WebMarkupContainer item, int level) {
         super.populateTreeItem(item, level);
         item.add(AttributeModifier.replace("uri",
             ((Thing) ((DefaultMutableTreeNode) (item.getDefaultModelObject())).getUserObject()).getUri()));
         item.add(AttributeModifier.append("class", "wicket-tree-node"));
     }
 
 
     @Override
     protected void onNodeLinkClicked(AjaxRequestTarget target, TreeNode node) {
         super.onNodeLinkClicked(target, node);
         for (ITreeListener listener : treeListenersList) {
            listener.onNodeLinkClicked(target);
         }
         target.appendJavaScript("test_tree_reload_sortable();");
         target.appendJavaScript("test_tree_reload();");
     }
 }
