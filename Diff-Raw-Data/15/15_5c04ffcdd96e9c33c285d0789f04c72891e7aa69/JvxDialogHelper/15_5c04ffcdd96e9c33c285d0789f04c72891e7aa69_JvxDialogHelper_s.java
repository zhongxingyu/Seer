 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package jui;
 
 import gengram.SentenceX;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import javax.swing.*;
 import java.awt.*;
 import java.awt.datatransfer.DataFlavor;
 import java.awt.datatransfer.Transferable;
 import java.awt.event.*;
 import java.io.FileOutputStream;
import java.io.FilterOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import javax.swing.JPopupMenu;
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.DefaultTreeModel;
 import javax.swing.tree.TreeNode;
 import javax.swing.JTree;
 import javax.swing.tree.TreeModel;
 import javax.swing.tree.TreePath;
 
 
 
 /**
  *
  * @author lin
  */
 public class JvxDialogHelper {
     static String datadir = JvxConfiguration.datadir;
     JvxMainFrame theFrame = null;
     
     public JvxDialogHelper(JvxMainFrame frame) {
         super();
         theFrame = frame;
     }
     public JPopupMenu createPopup( DefaultMutableTreeNode rightClickedNode) {
         final DialogMenuAction menuAction = new DialogMenuAction(theFrame.getDialogTree(), rightClickedNode);
         
         JPopupMenu popup = new JPopupMenu();
         JMenuItem addMenuItem = new JMenuItem("Add");
         JMenuItem delMenuItem = new JMenuItem("Delete");
         JMenuItem editMenuItem = new JMenuItem("Edit");
         //JMenuItem synMenuItem = new JMenuItem("Synonyms");
         
         addMenuItem.addActionListener(menuAction);
         delMenuItem.addActionListener(menuAction);
         editMenuItem.addActionListener(menuAction);
         //synMenuItem.addActionListener(menuAction);
         
        
         
         popup.add(addMenuItem);
         popup.add(delMenuItem);
         popup.add(editMenuItem);
         //popup.add(createSynonymsMenu(rightClickedNode.getUserObject()));
         popup.add(theFrame.getSynsHelper().createOkaysSynonymsMenu(rightClickedNode.getUserObject()));
         
         return popup;
     }
     
     public void dialogTreeRClicked(java.awt.event.MouseEvent evt) {
         JTree tree = (JTree)evt.getSource();
             
         if( evt.isPopupTrigger() ) {
             int x = evt.getX();
             int y = evt.getY();
             
             TreePath path = tree.getPathForLocation(x, y);
             if (path == null) return;
             
             DefaultMutableTreeNode rightClickedNode =
                             (DefaultMutableTreeNode)path.getLastPathComponent();
 
             TreePath[] selectionPaths = tree.getSelectionPaths();
             //check if node was selected
             boolean isSelected = false;
             if (selectionPaths != null) {
                 for (TreePath selectionPath : selectionPaths) {
                     if (selectionPath.equals(path)) {
                         isSelected = true;
                     }
                 }
             }
             //if clicked node was not selected, select it
             if(!isSelected){
                 tree.setSelectionPath(path);
             }
             //if(rightClickedNode.isLeaf()){
                 JPopupMenu popup = createPopup(rightClickedNode);
                 popup.show(tree, x, y);
            // }
         }
         else {
         }
     }
     public void dumpTreeToFile(String fname) throws IOException {
         java.io.FileOutputStream out = new FileOutputStream(fname);
         //out.write(dumpTree(theFrame.getDialogTree()).getBytes());
         out.write(dumpTree(theFrame.getDialogTree().getModel(), 
                 (DefaultMutableTreeNode)theFrame.getDialogTree().getModel().getRoot())
                 .getBytes());
     }
     
     private String dumpTree(TreeModel model, DefaultMutableTreeNode node) {
         StringBuffer tdump = new StringBuffer();
         Object sx = node.getUserObject();
         if(sx instanceof SentenceX) {
             String s = ((SentenceX)sx).dump(node.getLevel() - 2); // skip the dialog.road
             tdump.append(s);
         }
         for (int i = 0; i < model.getChildCount(node); i++) {
             String s = dumpTree(model, (DefaultMutableTreeNode)model.getChild(node, i));
             tdump.append( s );
         }
         return tdump.toString();
     }
     public String dumpTree(JTree tree) {
         StringBuffer tdump = new StringBuffer();
         DefaultMutableTreeNode root = (DefaultMutableTreeNode)tree.getModel().getRoot();
         int level = 0;
         for(Enumeration e = root.depthFirstEnumeration(); e.hasMoreElements();) {
             DefaultMutableTreeNode nd = (DefaultMutableTreeNode)e.nextElement();
             
             Object sx = nd.getUserObject();
             if(sx instanceof SentenceX) {
                 String s = ((SentenceX)sx).dump(nd.getLevel() - 2); // skip the dialog.road
                 tdump.append(s);
                 System.out.println(nd.getLevel() +": "+ s);
             }
         }
         return tdump.toString();
     }
     void debugTree(JTree tree) {
         DefaultMutableTreeNode root = (DefaultMutableTreeNode)tree.getModel().getRoot();
         for(Enumeration e = root.breadthFirstEnumeration(); e.hasMoreElements();) {
             DefaultMutableTreeNode nd = (DefaultMutableTreeNode)e.nextElement();
             System.out.println("---" + nd.getLevel() +"---");
             Object sx = nd.getUserObject();
             if(sx instanceof SentenceX) {
                 ((SentenceX)sx).debug();
             }
             System.out.println("---" + nd.getLevel() +"---");
             
         }
     }
     void dialogTreeMouseClicked(MouseEvent evt) {
         JTree tree = (JTree)evt.getSource();
         //debugTree(tree);
         if(evt.getClickCount() == 1 || evt.getClickCount() == 2) {
             TreePath tpath = tree.getSelectionPath();
             if(tpath != null) {
                 ArrayList<String> oks = new ArrayList<String>();
                 DefaultMutableTreeNode node = (DefaultMutableTreeNode)tpath.getLastPathComponent();
                 System.out.println(node);
                 for(Enumeration e = node.breadthFirstEnumeration(); e.hasMoreElements();) {
                     DefaultMutableTreeNode nd = (DefaultMutableTreeNode)e.nextElement();
                     ArrayList<String> al = null;
                     Object sx = nd.getUserObject();
                     if(sx instanceof SentenceX) {
                         al = new ArrayList<String> ();
                         ((SentenceX)sx).generateokays(al);
                     }
                     if(al != null) {
                         oks.addAll(al);
                     }
                     break; ///?
                 }
                 theFrame.getGrammarList().setListData(oks.toArray());
                 theFrame.getSynsHelper().populateSynonymsTab(node.getUserObject());
             }
                 
         }
     }
 }
 class DialogMenuAction implements ActionListener {
 
     private JTree dialogTree = null;
     DefaultMutableTreeNode rightClickedNode = null;
     
     DialogMenuAction(JTree tree, DefaultMutableTreeNode rNode) {
         dialogTree = tree;
         rightClickedNode = rNode;
     }
     
     public void actionPerformed(ActionEvent ae) {
         DefaultTreeModel model = (DefaultTreeModel)dialogTree.getModel();
         JMenuItem mi = (JMenuItem)ae.getSource();
         String action = mi.getText();
         System.out.println("Menu: " + action);
         // TODO - may be a confirm action here
         if(action.equals("Add")) {
             DefaultMutableTreeNode anotherNode = new DefaultMutableTreeNode(" ");
             rightClickedNode.add(anotherNode);
             model.reload(rightClickedNode);
             TreeNode[] nodes = ((DefaultTreeModel) dialogTree.getModel()).getPathToRoot(anotherNode);
             TreePath tpath = new TreePath(nodes);
             //dialogTree.scrollPathToVisible(tpath);
             dialogTree.expandPath(tpath);
             dialogTree.setSelectionPath(tpath);
             //dialogTree.startEditingAtPath(tpath);
         }
         else if(action.equals("Delete")) {
             rightClickedNode.removeAllChildren();
             model.removeNodeFromParent(rightClickedNode);
             rightClickedNode = null;
         }
         else if(action.equals("Edit")) {
             dialogTree.startEditingAtPath(dialogTree.getSelectionPath());
         }
         if(action.equals("Synonyms")) {
             TreePath tpath = dialogTree.getSelectionPath();
             DefaultMutableTreeNode node = (DefaultMutableTreeNode)tpath.getLastPathComponent();
             
             Object sx = node.getUserObject();
             if(sx instanceof SentenceX) {
    
                 
             }
         }
         model.reload(rightClickedNode);
     }
 }
 class DragHandler extends TransferHandler {
     JvxMainFrame theFrame = null;
     public DragHandler(JvxMainFrame frame)
     {
         super();
         theFrame = frame;
     }
     public boolean canImport(TransferSupport support) {
          if (!support.isDrop()) {
              return false;
          }
 
          return support.isDataFlavorSupported(DataFlavor.stringFlavor);
      }
 
      public boolean importData(TransferSupport support) {
          if (!canImport(support)) {
            return false;
          }
          Component targ = support.getComponent();
          Transferable transferable = support.getTransferable();
          String line;
          try {
            line = (String) transferable.getTransferData(DataFlavor.stringFlavor);
          } catch (Exception e) {
            return false;
          }
          if(targ instanceof JTable) {
              JTable.DropLocation dl = (JTable.DropLocation) support.getDropLocation();
              theFrame.synsHelper.dropSynonym(line, dl.getRow(), dl.getColumn());
          }
          JTree.DropLocation dl = (JTree.DropLocation) support.getDropLocation();
          
          String[] data = line.split("\n");
          for (String item: data) {
              if (!item.isEmpty()) {
                 //DefaultMutableTreeNode node = (DefaultMutableTreeNode)dl.getPath().getLastPathComponent();
                 Point p = dl.getDropPoint();
                 DefaultMutableTreeNode node = theFrame.getMouseOnNode((int)p.getX(), (int)p.getY());
                 node.add(new DefaultMutableTreeNode(item));
                 ((DefaultTreeModel)theFrame.getDialogTree().getModel()).reload(node);
              }
          }
          return true;
      }
 }
