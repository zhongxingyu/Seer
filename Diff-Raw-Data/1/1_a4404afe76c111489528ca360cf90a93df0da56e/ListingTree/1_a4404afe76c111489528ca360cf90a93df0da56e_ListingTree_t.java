 /**
  * 
  */
 package cz.cuni.mff.peckam.java.origamist.gui.listing;
 
 import java.awt.Component;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.event.MouseEvent;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Stack;
 
 import javax.swing.JTree;
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.DefaultTreeModel;
 import javax.swing.tree.DefaultTreeSelectionModel;
 import javax.swing.tree.TreePath;
 import javax.swing.tree.TreeSelectionModel;
 
 import cz.cuni.mff.peckam.java.origamist.files.Category;
 import cz.cuni.mff.peckam.java.origamist.files.File;
 import cz.cuni.mff.peckam.java.origamist.files.Listing;
 import cz.cuni.mff.peckam.java.origamist.services.ServiceLocator;
 import cz.cuni.mff.peckam.java.origamist.services.interfaces.ConfigurationManager;
 
 /**
  * A JTree displaying the list of loaded files and categories.
  * 
  * @author Martin Pecka
  */
 public class ListingTree extends JTree
 {
 
     /** */
     private static final long serialVersionUID           = 7977020048548617471L;
 
     /**
      * The expanded nodes. A hashtable would serve better, but we workaround the unability to use hashcode() of the
      * changing nodes.
      */
     protected List<TreePath>  expanded                   = new LinkedList<TreePath>();
 
     /** If false, do not fire TreeExpansionListeners' events. */
     protected boolean         fireTreeExpansionListeners = true;
 
     public ListingTree(Listing listing)
     {
         setModel(new DefaultTreeModel(createStructure(listing)));
 
         setSelectionModel(new DefaultTreeSelectionModel());
         getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
 
         setRootVisible(false);
         setShowsRootHandles(true);
        setToolTipText(""); // XXX - don't delete this line, or the tooltips for items won't display :(
 
         setCellRenderer(new ListingTreeCellRenderer());
 
         addTreeSelectionListener(new ListingTreeSelectionListener());
 
         ServiceLocator.get(ConfigurationManager.class).get()
                 .addPropertyChangeListener("diagramLocale", new PropertyChangeListener() {
                     @Override
                     public void propertyChange(PropertyChangeEvent evt)
                     {
                         invalidate();
                     }
                 });
     }
 
     /**
      * Create a structure of DefaultMutableTreeNodes corresponding to the given listing.
      * 
      * @param listing The listing to generate the structure from.
      * @return The structure generated from the listing.
      */
     protected DefaultMutableTreeNode createStructure(Listing listing)
     {
         if (listing == null)
             return null;
 
         Stack<DefaultMutableTreeNode> categories = new Stack<DefaultMutableTreeNode>();
 
         DefaultMutableTreeNode root = new DefaultMutableTreeNode(listing);
         if (listing.getCategories() != null) {
             for (Category cat : listing.getCategories().getCategory()) {
                 DefaultMutableTreeNode node = new DefaultMutableTreeNode(cat);
                 categories.push(node);
                 root.add(node);
             }
         }
         if (listing.getFiles() != null) {
             for (File file : listing.getFiles().getFile()) {
                 root.add(new DefaultMutableTreeNode(file, false));
             }
         }
 
         while (!categories.isEmpty()) {
             DefaultMutableTreeNode catNode = categories.pop();
             Category cat = (Category) catNode.getUserObject();
             if (cat.getCategories() != null) {
                 for (Category cat1 : cat.getCategories().getCategory()) {
                     DefaultMutableTreeNode node = new DefaultMutableTreeNode(cat1);
                     categories.push(node);
                     catNode.add(node);
                 }
             }
             if (cat.getFiles() != null) {
                 for (File file : cat.getFiles().getFile()) {
                     catNode.add(new DefaultMutableTreeNode(file, false));
                 }
             }
         }
 
         return root;
     }
 
     @Override
     public String getToolTipText(MouseEvent event)
     {
         if (getRowForLocation(event.getX(), event.getY()) == -1)
             return null;
         TreePath curPath = getPathForLocation(event.getX(), event.getY());
         Object comp = curPath.getLastPathComponent();
         if (comp instanceof File) {
             FileRenderer fc = (FileRenderer) getCellRenderer().getTreeCellRendererComponent(this, comp, false, false,
                     true, 0, false);
             Rectangle entryBounds = this.getPathBounds(curPath);
             int x = (int) (event.getX() - entryBounds.getX());
             int y = (int) (event.getY() - entryBounds.getY());
             MouseEvent e = new MouseEvent((Component) event.getSource(), event.getID(), event.getWhen(),
                     event.getModifiers(), x, y, event.getXOnScreen(), event.getYOnScreen(), event.getClickCount(),
                     true, event.getButton());
             return fc.getToolTipText(e);
         } else {
             return super.getToolTipText(event);
         }
     }
 
     @Override
     public Point getToolTipLocation(MouseEvent event)
     {
         if (getRowForLocation(event.getX(), event.getY()) == -1)
             return super.getToolTipLocation(event);
         TreePath curPath = getPathForLocation(event.getX(), event.getY());
         Object comp = ((DefaultMutableTreeNode) curPath.getLastPathComponent()).getUserObject();
         if (comp instanceof File) {
             FileRenderer fc = (FileRenderer) getCellRenderer().getTreeCellRendererComponent(this,
                     curPath.getLastPathComponent(), false, false, true, 0, false);
             Rectangle entryBounds = this.getPathBounds(curPath);
             int x = (int) (event.getX() - entryBounds.getX());
             int y = (int) (event.getY() - entryBounds.getY());
             MouseEvent e = new MouseEvent((Component) event.getSource(), event.getID(), event.getWhen(),
                     event.getModifiers(), x, y, event.getXOnScreen(), event.getYOnScreen(), event.getClickCount(),
                     true, event.getButton());
             Point loc = fc.getToolTipLocation(e);
             if (loc != null) {
                 loc.x += entryBounds.x;
                 loc.y += entryBounds.y;
                 return loc;
             }
         }
         return super.getToolTipLocation(event);
     }
 
 }
