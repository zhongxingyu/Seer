 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package evopaint.gui.ruleseteditor;
 
 import evopaint.pixel.rulebased.ExampleRuleSetCollectionFactory;
 import evopaint.pixel.rulebased.RuleSet;
 import evopaint.pixel.rulebased.RuleSetCollection;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import javax.swing.JFileChooser;
 import javax.swing.JTree;
 import javax.swing.event.TreeSelectionListener;
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.DefaultTreeCellRenderer;
 import javax.swing.tree.DefaultTreeModel;
 
 /**
  *
  * @author tam
  */
 public class JRuleSetBrowser extends JTree {
     DefaultMutableTreeNode root;
 
     public void updateCollections() {
         File userDir = new JFileChooser().getFileSystemView().getDefaultDirectory();
         File home = new File(userDir, ".evopaint");
         if (home.exists() == false) {
             home.mkdir();
         }
         File collectionsDir = new File(home, "collections");
         if (collectionsDir.exists() == false) {
             collectionsDir.mkdir();
             createExampleCollections(collectionsDir);
         }
         File [] collectionDir = collectionsDir.listFiles();
 
         root = new DefaultMutableTreeNode();
         try {
             for (File collectionFile : collectionDir) {
                 ObjectInputStream in = new ObjectInputStream(new FileInputStream(collectionFile));
                 RuleSetCollection ruleSetCollection = (RuleSetCollection)in.readObject();
                 in.close();
                 DefaultMutableTreeNode collectionNode = new DefaultMutableTreeNode(ruleSetCollection);
                 for (RuleSet ruleSet : ruleSetCollection.getRulesets()) {
                     collectionNode.add(new DefaultMutableTreeNode(ruleSet));
                 }
                 root.add(collectionNode);
             }
         } catch (FileNotFoundException ex) {
             ex.printStackTrace();
             System.exit(1);
         } catch (IOException ex) {
             ex.printStackTrace();
             System.exit(1);
         } catch (ClassNotFoundException ex) {
             ex.printStackTrace();
             System.exit(1);
         }
         DefaultTreeModel model = new DefaultTreeModel(root);
         setModel(model);
     }
 
     public void createExampleCollections(File dir) {
         RuleSetCollection ruleSetCollection = ExampleRuleSetCollectionFactory.createSimple();
         try {
             File collectionFile = new File(dir, ruleSetCollection.getName().replace(" ", "_").toLowerCase() + ".epc");
             collectionFile.createNewFile();
             ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(collectionFile));
             out.writeObject(ruleSetCollection);
             out.close();
         } catch (FileNotFoundException ex) {
             ex.printStackTrace();
             System.exit(1);
         } catch (IOException ex) {
             ex.printStackTrace();
             System.exit(1);
         } 
     }
 
     public JRuleSetBrowser(TreeSelectionListener treeSelectionListener) {
         updateCollections();
         setRootVisible(false);
         setCellRenderer(new RuleSetTreeCellRenderer());
        setPreferredSize(new Dimension(250, 250));
         addTreeSelectionListener(treeSelectionListener);
     }
 
     class RuleSetTreeCellRenderer extends DefaultTreeCellRenderer {
 
         @Override
         public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
             Object userObject = ((DefaultMutableTreeNode)value).getUserObject();
             if (userObject instanceof RuleSet) {
                 return super.getTreeCellRendererComponent(tree, new DefaultMutableTreeNode(((RuleSet)userObject).getName()), sel, expanded, leaf, row, hasFocus);
             }
             if (userObject instanceof RuleSetCollection) {
                  return super.getTreeCellRendererComponent(tree, new DefaultMutableTreeNode(((RuleSetCollection)userObject).getName()), sel, expanded, leaf, row, hasFocus);
             }
             return super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
         }
 
     }
 }
