 package htmlFileFolderNavigator;
 
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTree;
 import javax.swing.tree.DefaultTreeModel;
import java.awt.Dimension;
 
 public class HTMLFileFolderNavigatorUI {
   private JTree tree;
 
   public HTMLFileFolderNavigatorUI(DefaultTreeModel treeNode) {
     tree = new JTree(treeNode);
    tree.setSize(new Dimension(500, 1000));
    tree.setMaximumSize(new Dimension(500, 1000));
   }
 
   public JComponent getScrollableTree() {
     return new JScrollPane(tree);
   }
 
   public JTree getTree() {
     return tree;
   }
 }
