 package gov.nih.nci.ncicb.cadsr.loader.ui;
 
 import javax.swing.tree.TreePath;
 import javax.swing.tree.TreeNode;
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.DefaultTreeModel;
 
 import javax.swing.*;
 import java.awt.BorderLayout;
 import java.awt.FlowLayout;
 import java.awt.Color;
 import java.awt.event.*;
 
 import java.util.*;
 
 import gov.nih.nci.ncicb.cadsr.loader.ui.tree.ReviewableUMLNode;
 import gov.nih.nci.ncicb.cadsr.loader.ui.tree.FilterPackage;
 import gov.nih.nci.ncicb.cadsr.loader.ui.tree.FilterClass;
 
 import gov.nih.nci.ncicb.cadsr.loader.util.PropertyAccessor;
 
 import gov.nih.nci.ncicb.cadsr.loader.ElementsLists;
 
 public class PackageClassFilterPanel extends JPanel
 {
 
   private JTree tree = new JTree();
   private ElementsLists elements = ElementsLists.getInstance();
   private JScrollPane scrollPane;
 
   public PackageClassFilterPanel() 
   {
     initUI();
   }  
 
   public void  init() 
   {
     DefaultTreeModel treeModel = new DefaultTreeModel(buildTree());
     tree.setModel(treeModel);
   }
 
   private void initUI() 
   { 
     tree.setModel(null);
     tree.setRootVisible(false);
     this.setLayout(new BorderLayout());
     tree.setCellRenderer(new CheckRenderer());
     tree.addMouseListener(new NodeSelectionListener(tree));
     scrollPane = new JScrollPane(tree);
 
     JPanel infoPanel = new JPanel();
     infoPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
     infoPanel.setBackground(Color.WHITE);
     infoPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
 
     infoPanel.add(new JLabel(new ImageIcon(Thread.currentThread().getContextClassLoader().getResource("siw-logo3_2.gif"))));
 
     JLabel infoLabel = new JLabel(PropertyAccessor.getProperty("package.class.filter.help.label"));
     infoPanel.add(infoLabel);
     
     this.add(infoPanel, BorderLayout.NORTH);
 
     this.add(scrollPane, BorderLayout.CENTER);
   }
 
   private TreeNode buildTree() {
     CheckNode node = new CheckNode(new FilterPackage("Packages"));
     
     doPackages(node);
     
     return node;
   }
 
 
   private void doPackages(DefaultMutableTreeNode parentNode) {
     
     FilterPackage pkg = new FilterPackage("");
     List<FilterPackage> packages = elements.getElements(pkg);
     
     for(FilterPackage pack : packages) {
 //       CheckNode node = new CheckNode(pack.getName());
       CheckNode node = new CheckNode(pack);
       parentNode.add(node);
       doClasses(node);
     }
     
   }
   
   private void doClasses(DefaultMutableTreeNode parentNode) 
   {
     FilterPackage pkg = new FilterPackage("");
     List<FilterPackage> packages = elements.getElements(pkg);
 
     FilterClass c = new FilterClass("", "");
     List<FilterClass> classes = elements.getElements(c);
     
     for(FilterClass clazz : classes) {
 //       for(FilterPackage pack : packages) {
         if(clazz.getPackageName().equals(parentNode.getUserObject().toString())) {
 //           parentNode.add(new CheckNode(clazz.getName()));
           parentNode.add(new CheckNode(clazz));
         }
 //       }
     }
 
   }
 }
 
 class NodeSelectionListener extends MouseAdapter {
   JTree tree;
   
   NodeSelectionListener(JTree tree) {
     this.tree = tree;
   }
   
   public void mouseClicked(MouseEvent e) {
     int x = e.getX();
     int y = e.getY();
     int row = tree.getRowForLocation(x, y);
     TreePath  path = tree.getPathForRow(row);
     //TreePath  path = tree.getSelectionPath();
     if (path != null) {
       CheckNode node = (CheckNode)path.getLastPathComponent();
       boolean isSelected = ! (node.isSelected());
       node.setSelected(isSelected);
       if (node.getSelectionMode() == CheckNode.DIG_IN_SELECTION) {
         if ( isSelected ) {
           tree.expandPath(path);
         } else {
          tree.collapsePath(path);
         }
         ((ReviewableUMLNode)node.getUserObject()).setReviewed(isSelected);
         Enumeration enu = node.children();
         while(enu.hasMoreElements()) {
           CheckNode cnode = (CheckNode)enu.nextElement();
           ((ReviewableUMLNode)cnode.getUserObject()).setReviewed(isSelected);
         }
       }
       ((DefaultTreeModel)tree.getModel()).nodeChanged(node);
       // I need revalidate if node is root.  but why?
       if (row == 0) {
         tree.revalidate();
       }
       tree.repaint();
     }
   }
 }
