 /**
  * 
  */
 package pl.psnc.dl.wf4ever.portal.model;
 
 import java.util.Enumeration;
 
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.DefaultTreeModel;
 
 import org.purl.wf4ever.rosrs.client.Resource;
 import org.purl.wf4ever.rosrs.client.Thing;
 
 /**
  * Tree model used for RO resources visualization.
  * 
  * @author piotrhol
  * 
  */
 public class RoTreeModel extends DefaultTreeModel {
 
     /** id. */
     private static final long serialVersionUID = 4708607931110844599L;
 
 
     /**
      * Constructor.
      * 
      * @param root
      *            the Research Object
      */
     public RoTreeModel(Thing root) {
         super(new DefaultMutableTreeNode(root));
     }
 
 
     /**
      * Adds the resource grouping it to all matching folders. If no folders is matched, it is added directly to root
      * node.
      * 
      * @param resource
      *            resource to add
      * @param addToGroups
      *            should it be added to groups that it belongs to
      */
     public void addAggregatedResource(Resource resource) {
         DefaultMutableTreeNode root = (DefaultMutableTreeNode) this.getRoot();
         root.add(new DefaultMutableTreeNode(resource));
 
         //add to groups, for later use with folders
         //        DefaultMutableTreeNode root = (DefaultMutableTreeNode) this.getRoot();
         //        Set<ResourceGroup> notFound = new HashSet<>(resource.getMatchingGroups());
         //        @SuppressWarnings("unchecked")
         //        Enumeration<DefaultMutableTreeNode> e = root.breadthFirstEnumeration();
         //        while (e.hasMoreElements()) {
         //            DefaultMutableTreeNode node = e.nextElement();
         //            if (node.getUserObject() instanceof ResourceGroup
         //                    && resource.getMatchingGroups().contains(node.getUserObject())) {
         //                node.add(new DefaultMutableTreeNode(resource));
         //                notFound.remove(node.getUserObject());
         //            }
         //        }
         //
         //        for (ResourceGroup resourceGroup : notFound) {
         //            DefaultMutableTreeNode node = new DefaultMutableTreeNode(resourceGroup);
         //            node.add(new DefaultMutableTreeNode(resource));
         //            root.insert(node, 0);
         //        }
     }
 
 
     /**
      * Removes all nodes that have this resource as object.
      * 
      * @param resource
      *            resource to remove
      */
     public void removeAggregatedResource(Resource resource) {
         DefaultMutableTreeNode root = (DefaultMutableTreeNode) this.getRoot();
         @SuppressWarnings("unchecked")
         Enumeration<DefaultMutableTreeNode> e = root.breadthFirstEnumeration();
         while (e.hasMoreElements()) {
             DefaultMutableTreeNode node = e.nextElement();
             if (resource.equals(node.getUserObject())) {
                 DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
                 if (parent != null && parent.getChildCount() == 1) {
                     parent.removeFromParent();
                 } else {
                     node.removeFromParent();
                 }
             }
         }
 
     }
 }
