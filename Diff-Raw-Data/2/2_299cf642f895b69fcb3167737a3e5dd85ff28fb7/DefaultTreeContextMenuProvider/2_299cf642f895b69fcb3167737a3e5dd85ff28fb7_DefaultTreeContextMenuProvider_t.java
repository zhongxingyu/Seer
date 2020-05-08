 package shellderp.bcexplorer.ui;
 
 import shellderp.bcexplorer.Node;
 
 import javax.swing.*;
 import javax.swing.tree.TreePath;
 import java.awt.event.ActionEvent;
 
 /**
  * Created by: Mike
  * Date: 1/25/12
  * Time: 11:23 PM
  */
 public class DefaultTreeContextMenuProvider implements TreeContextMenuProvider {
 
     public JPopupMenu createContextMenu(final JTree tree, final TreePath path, final Node node) {
         JPopupMenu menu = new JPopupMenu();
 
         menu.add(new AbstractAction("Copy") {
             public void actionPerformed(ActionEvent e) {
                 SwingUtils.copyStringToClipboard(node.toString());
             }
         });
 
         menu.add(new AbstractAction("Copy tree") {
             public void actionPerformed(ActionEvent e) {
                 SwingUtils.copyStringToClipboard(node.treeString());
             }
         });
 
         menu.addSeparator();
 
         menu.add(new AbstractAction("Expand all children") {
             public void actionPerformed(ActionEvent e) {
                 SwingUtils.expandAllChildren(tree, path, true);
             }
         });
 
         menu.add(new AbstractAction("Collapse all children") {
             public void actionPerformed(ActionEvent e) {
                 SwingUtils.expandAllChildren(tree, path, false);
             }
         });
 
         return menu;
     }
 
     public JPopupMenu createContextMenu(final JTree tree, final TreePath[] paths, final Node[] nodes) {
         JPopupMenu menu = new JPopupMenu();
 
         menu.add(new AbstractAction("Copy") {
             public void actionPerformed(ActionEvent e) {
                 StringBuilder string = new StringBuilder();
                 for (Node node : nodes)
                     string.append(node.toString()).append(System.getProperty("line.separator"));
                 SwingUtils.copyStringToClipboard(string.toString());
             }
         });
 
         menu.addSeparator();
 
         menu.add(new AbstractAction("Expand all children") {
             public void actionPerformed(ActionEvent e) {
                 for (TreePath path : paths)
                     SwingUtils.expandAllChildren(tree, path, true);
             }
         });
 
         menu.add(new AbstractAction("Collapse all children") {
             public void actionPerformed(ActionEvent e) {
                 for (TreePath path : paths)
                    SwingUtils.expandAllChildren(tree, path, false);
             }
         });
 
         return menu;
     }
 }
