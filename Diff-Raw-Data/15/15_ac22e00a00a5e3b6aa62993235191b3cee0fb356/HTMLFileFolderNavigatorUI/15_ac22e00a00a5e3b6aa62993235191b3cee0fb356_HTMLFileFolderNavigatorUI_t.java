 package htmlFileFolderNavigator;
 
import javax.swing.*;
 import javax.swing.tree.DefaultTreeModel;
 
 public class HTMLFileFolderNavigatorUI {
   private JTree tree;
 
   public HTMLFileFolderNavigatorUI(DefaultTreeModel treeNode) {
     tree = new JTree(treeNode);
   }
 
   public JComponent getScrollableTree() {
     return new JScrollPane(tree);
   }
 
   public JTree getTree() {
     return tree;
   }
 }
