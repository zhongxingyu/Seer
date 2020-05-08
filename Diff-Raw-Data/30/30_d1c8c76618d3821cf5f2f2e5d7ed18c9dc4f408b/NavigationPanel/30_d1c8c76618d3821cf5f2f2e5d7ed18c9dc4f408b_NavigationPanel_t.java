 /*
  * Copyright 2000-2003 Oracle, Inc. This software was developed in conjunction with the National Cancer Institute, and so to the extent government employees are co-authors, any rights in such works shall be subject to Title 17 of the United States Code, section 105.
  *
  Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
  *
  * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the disclaimer of Article 3, below. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
  *
  * 2. The end-user documentation included with the redistribution, if any, must include the following acknowledgment:
  *
  * "This product includes software developed by Oracle, Inc. and the National Cancer Institute."
  *
  * If no such end-user documentation is to be included, this acknowledgment shall appear in the software itself, wherever such third-party acknowledgments normally appear.
  *
  * 3. The names "The National Cancer Institute", "NCI" and "Oracle" must not be used to endorse or promote products derived from this software.
  *
  * 4. This license does not authorize the incorporation of this software into any proprietary programs. This license does not authorize the recipient to use any trademarks owned by either NCI or Oracle, Inc.
  *
  * 5. THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESSED OR IMPLIED WARRANTIES, (INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE) ARE DISCLAIMED. IN NO EVENT SHALL THE NATIONAL CANCER INSTITUTE, ORACLE, OR THEIR AFFILIATES BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE 
  */
 package gov.nih.nci.ncicb.cadsr.loader.ui;
 
 import gov.nih.nci.ncicb.cadsr.domain.Concept;
 import gov.nih.nci.ncicb.cadsr.domain.DomainObjectFactory;
 import gov.nih.nci.ncicb.cadsr.domain.ObjectClass;
 import gov.nih.nci.ncicb.cadsr.domain.ObjectClassRelationship;
 import gov.nih.nci.ncicb.cadsr.loader.ElementsLists;
 import gov.nih.nci.ncicb.cadsr.loader.ReviewTracker;
 import gov.nih.nci.ncicb.cadsr.loader.ReviewTrackerType;
 import gov.nih.nci.ncicb.cadsr.loader.UserSelections;
 import gov.nih.nci.ncicb.cadsr.loader.event.ReviewEvent;
 import gov.nih.nci.ncicb.cadsr.loader.event.ReviewListener;
 import gov.nih.nci.ncicb.cadsr.loader.ui.event.NavigationEvent;
 import gov.nih.nci.ncicb.cadsr.loader.ui.event.NavigationListener;
 import gov.nih.nci.ncicb.cadsr.loader.ui.event.SearchEvent;
 import gov.nih.nci.ncicb.cadsr.loader.ui.event.SearchListener;
 import gov.nih.nci.ncicb.cadsr.loader.ui.event.TreeEvent;
 import gov.nih.nci.ncicb.cadsr.loader.ui.event.TreeListener;
 import gov.nih.nci.ncicb.cadsr.loader.ui.event.ViewChangeEvent;
 import gov.nih.nci.ncicb.cadsr.loader.ui.event.ViewChangeListener;
 import gov.nih.nci.ncicb.cadsr.loader.ui.tree.AbstractUMLNode;
 import gov.nih.nci.ncicb.cadsr.loader.ui.tree.AssociationEndNode;
 import gov.nih.nci.ncicb.cadsr.loader.ui.tree.AssociationNode;
 import gov.nih.nci.ncicb.cadsr.loader.ui.tree.AttributeNode;
 import gov.nih.nci.ncicb.cadsr.loader.ui.tree.ClassNode;
 import gov.nih.nci.ncicb.cadsr.loader.ui.tree.InheritedAttributeNode;
 import gov.nih.nci.ncicb.cadsr.loader.ui.tree.NodeUtil;
 import gov.nih.nci.ncicb.cadsr.loader.ui.tree.ReviewableUMLNode;
 import gov.nih.nci.ncicb.cadsr.loader.ui.tree.RootNode;
 import gov.nih.nci.ncicb.cadsr.loader.ui.tree.TreeBuilder;
 import gov.nih.nci.ncicb.cadsr.loader.ui.tree.UMLNode;
 import gov.nih.nci.ncicb.cadsr.loader.ui.tree.PackageNode;
 import gov.nih.nci.ncicb.cadsr.loader.ui.tree.ValidationNode;
 import gov.nih.nci.ncicb.cadsr.loader.ui.tree.ValueDomainNode;
 import gov.nih.nci.ncicb.cadsr.loader.ui.tree.ValueMeaningNode;
 import gov.nih.nci.ncicb.cadsr.loader.ui.util.TreeUtil;
 import gov.nih.nci.ncicb.cadsr.loader.util.PropertyAccessor;
 import gov.nih.nci.ncicb.cadsr.loader.util.RunMode;
 import gov.nih.nci.ncicb.cadsr.loader.util.UserPreferences;
 import gov.nih.nci.ncicb.cadsr.loader.util.UserPreferencesEvent;
 import gov.nih.nci.ncicb.cadsr.loader.util.UserPreferencesListener;
 
 import java.awt.BorderLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Enumeration;
 import java.util.List;
 import java.util.Set;
 
 import javax.swing.JCheckBoxMenuItem;
 import javax.swing.JMenu;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JPopupMenu;
 import javax.swing.JScrollPane;
 import javax.swing.JTree;
 import javax.swing.ToolTipManager;
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.DefaultTreeModel;
 import javax.swing.tree.TreeNode;
 import javax.swing.tree.TreePath;
 
 /**
  * Panel from where you navigate the UML Elements.
  *
  * @author <a href="mailto:chris.ludet@oracle.com">Christophe Ludet</a>
  * @author <a href="mailto:rokickik@mail.nih.gov">Konrad Rokicki</a>
  */
 public class NavigationPanel extends JPanel 
   implements ActionListener, MouseListener, ReviewListener, NavigationListener,
              KeyListener, SearchListener, TreeListener, UserPreferencesListener
 {
   private JTree tree;
   private TreeNode rootTreeNode;
   
   // only one of these tree nodes is used at a time, 
   // the one being used is pointed to by rootTreeNode
   private TreeNode rootUnsortedTreeNode;
   private TreeNode rootSortedTreeNode;
   
   private JPopupMenu nodePopup, blankPopup;
   private JScrollPane scrollPane;
   private UMLNode rootNode = TreeBuilder.getInstance().getRootNode(); 
 
   private List<ViewChangeListener> viewListeners = new ArrayList();
   private List<NavigationListener> navigationListeners = new ArrayList();
 
   private ElementsLists elements = ElementsLists.getInstance();
 
   private JCheckBoxMenuItem inheritedAttItem, assocItem, sortItem;
 
   private TreePath selectedPath;
 
   private ReviewTracker reviewTracker;
   
   public NavigationPanel()
   {
     try
       {
         initUI();
         TreeBuilder.getInstance().addTreeListener(this);
         UserPreferences prefs = UserPreferences.getInstance();
         prefs.addUserPreferencesListener(this);
 
 	RunMode runMode = (RunMode)(UserSelections.getInstance().getProperty("MODE"));
 	if(runMode.equals(RunMode.Curator)) {
 	  reviewTracker = ReviewTracker.getInstance(ReviewTrackerType.Curator);
 	} else {
 	  reviewTracker = ReviewTracker.getInstance(ReviewTrackerType.Owner);
 	}
       
       }
     catch(Exception e)
       {
         e.printStackTrace();
       }
   }
 
   public void addViewChangeListener(ViewChangeListener l) {
     viewListeners.add(l);
   }
 
   public void addNavigationListener(NavigationListener l) {
     navigationListeners.add(l);
   }
 
   public void reviewChanged(ReviewEvent event) {
     ReviewableUMLNode node = (ReviewableUMLNode)event.getUserObject();
     node.setReviewed(event.isReviewed());
 
     tree.repaint();
   }
   
   private void initUI() throws Exception
   {
     rootTreeNode = buildTree();
     rootUnsortedTreeNode = rootTreeNode;
 
     tree = new JTree(new DefaultTreeModel(rootTreeNode));
     tree.setRootVisible(false);
     tree.setShowsRootHandles(true);
     tree.addKeyListener(this);
     
     UserPreferences prefs = UserPreferences.getInstance();
     if (prefs.getSortElements()) {
         showSortedTree(true);
     }
 
     //Traverse Tree expanding all nodes    
     TreeUtil.expandAll(tree, rootTreeNode);
     
     tree.setCellRenderer(new UMLTreeCellRenderer());
 
     this.setLayout(new BorderLayout());
 
     scrollPane = new JScrollPane(tree);
     this.add(scrollPane, BorderLayout.CENTER);
 
     ToolTipManager.sharedInstance().registerComponent(tree);
     
     buildPopupMenu();
   }
   
   /**
    * Replace the current tree model with a sorted or unsorted one, depending
    * on the boolean parameter. 
    * @param sorted true to show a sorted tree, false to show the original tree
    */
   private void showSortedTree(boolean sorted) {
 
       if (sorted) {
           if (rootSortedTreeNode == null) {
               // this extra tree is only built if the user requests it
               rootSortedTreeNode = buildSortedTree(
                       (DefaultMutableTreeNode)rootUnsortedTreeNode);
           }
           rootTreeNode = rootSortedTreeNode;
       }
       else {
           rootTreeNode = rootUnsortedTreeNode;
       }
 
       tree.setModel(new DefaultTreeModel(rootTreeNode));
   }
 
   /**
    * Find the specified path in the given tree. To compare objects between 
    * the path and the tree, toString() is called on the respective objects.
    * Thus, the path could be composed of Strings or TreeNodes from another 
    * tree or whatever.
    * @param path list of objects on the path, beginning with the root
    * @param node root of the subtree to search
    * @return nodes in the tree forming the path, or null if the path is not found
    */
   private List<TreeNode> translatePath(List path, TreeNode node) {
 
       // TODO: Find the correct node given two identical paths. 
       // For example suppose we have two paths Classes/String and Classes/String.
       // This method will always translate to the first path, regardless of the
       // actual path given.
       
       List tail = new ArrayList();
       tail.addAll(path);
       Object target = tail.remove(0);
       
       if (!node.toString().equals(target.toString())) {
           return null;
       }
       
       List<TreeNode> newPath = new ArrayList<TreeNode>();
       newPath.add(node);
 
       // no more nodes to find, just return this one
       if (tail.isEmpty()) return newPath;
       
       Enumeration e = node.children();
       while(e.hasMoreElements()) {
           TreeNode child = (TreeNode)e.nextElement();
           List nodes = translatePath(tail, child);
           if (nodes != null) {
               newPath.addAll(nodes);
               return newPath;
           }
       }
       
       // the path was not found under this subtree
       return null;
   }
 
   
   /**
    * Builds a sorted version of the tree.
    * @param oldNode the root of the unsorted tree
    * @return the root of the sorted tree
    */
   private DefaultMutableTreeNode buildSortedTree(DefaultMutableTreeNode oldNode) {
       return buildSortedTree(oldNode, 
               new DefaultMutableTreeNode(oldNode.getUserObject()));
   }
 
   /**
    * Builds a sorted version of the specified subtree.
    * @param oldNode the node on the unsorted tree
    * @param newNode the node on the sorted tree
    * @return the node on the sorted tree
    */
   private DefaultMutableTreeNode buildSortedTree(DefaultMutableTreeNode oldNode, 
           DefaultMutableTreeNode newNode) {
       
       List<DefaultMutableTreeNode> children = new ArrayList();
       Enumeration e = oldNode.children();
       while(e.hasMoreElements()) {
           DefaultMutableTreeNode child = (DefaultMutableTreeNode)e.nextElement();
           children.add(child);
       }
       
       // sort everything except the first level
       if (!(oldNode.getUserObject() instanceof RootNode)) {
           Collections.sort(children, new Comparator<DefaultMutableTreeNode>() {
               public int compare(DefaultMutableTreeNode node1, DefaultMutableTreeNode node2) {
                   return node1.getUserObject().toString().compareTo(
                           node2.getUserObject().toString());
               }
             });
       }
       
       for(DefaultMutableTreeNode oldChild : children) {
           DefaultMutableTreeNode newChild = new DefaultMutableTreeNode(oldChild.getUserObject());
           newNode.add(newChild);
           buildSortedTree(oldChild, newChild);
       }
     
       return newNode;
   }
   
   public void actionPerformed(ActionEvent event) {
     DefaultMutableTreeNode dmtn, node;
 
     TreePath path = tree.getSelectionPath();
 
     if(path != null) {
       dmtn = (DefaultMutableTreeNode) path.getLastPathComponent();
       if(event.getSource() instanceof JMenuItem) {
         JMenuItem menuItem = (JMenuItem)event.getSource();
         
         ViewChangeEvent evt = new ViewChangeEvent(ViewChangeEvent.VIEW_CONCEPTS);
         evt.setViewObject(dmtn.getUserObject());
         
         if(menuItem.getActionCommand().equals("OPEN_NEW_TAB"))
           evt.setInNewTab(true);
         else 
           evt.setInNewTab(false);
 
         fireViewChangeEvent(evt);
         
       }
     } 
     if(event.getSource() instanceof JMenuItem) {
       JMenuItem menuItem = (JMenuItem)event.getSource();
       if(menuItem.getActionCommand().equals("COLLAPSE_ALL")) {
         TreeUtil.collapseAll(tree);
       } else if(menuItem.getActionCommand().equals("EXPAND_ALL")) {
         TreeUtil.expandAll(tree, rootTreeNode);
       } else if(menuItem.getActionCommand().equals("PREF_INHERITED_ATT")) {
         UserPreferences prefs = UserPreferences.getInstance();
         prefs.setShowInheritedAttributes(inheritedAttItem.isSelected());
       } else if(menuItem.getActionCommand().equals("PREF_INCLASS_ASSOC")) {
         UserPreferences prefs = UserPreferences.getInstance();
         prefs.setViewAssociationType(assocItem.isSelected()?"true":"false");
       } else if(menuItem.getActionCommand().equals("PREF_SORT_ELEM")) {
         UserPreferences prefs = UserPreferences.getInstance();
         prefs.setSortElements(sortItem.isSelected());
       }
     }
   }
   
   public void keyPressed(KeyEvent e) {
     DefaultMutableTreeNode selected = 
       (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
        
     //if the down arrow is pressed then display the next element
     //in the tree in the ViewPanel    
     if(e.getKeyCode() == KeyEvent.VK_DOWN  && selected != null) 
       {
         if (selected.getNextNode() != null) 
           {
             TreePath path =  new TreePath(selected.getNextNode().getPath());
             tree.makeVisible(path);
             newViewEvent(path);
           }
       }
     //if the up arrow is pressed then display the previous element
     //in the tree in the ViewPanel
     if(e.getKeyCode() == KeyEvent.VK_UP && selected != null)
       {
         if (selected.getPreviousNode() != null) 
           {
             TreePath path =  new TreePath(selected.getPreviousNode().getPath());
             tree.makeVisible(path);
             newViewEvent(path);
           }  
       }
 
   }
   
   public void keyTyped(KeyEvent e) {
   }
   
   public void keyReleased(KeyEvent e) {
   }
 
   public void mousePressed(MouseEvent e) {
     showPopup(e);
   }
   
   public void mouseExited(MouseEvent e) {
   }
   
   public void mouseClicked(MouseEvent e) {
   }
 
   public void mouseEntered(MouseEvent e) {
   }
   
   public void mouseReleased(MouseEvent e) {
     showPopup(e);
     doMouseEvent(e);
   }
   
   private void showPopup(MouseEvent e) {
     if (e.isPopupTrigger()) {
       TreePath path = tree.getPathForLocation(e.getX(), e.getY());
       tree.setSelectionPath(path);
       if(path != null) {
         DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();
         Object o = node.getUserObject();
         if((o instanceof ClassNode)
            || (o instanceof AttributeNode)
            )
           nodePopup.show(e.getComponent(),
                          e.getX(), e.getY());
       } else {
         blankPopup.show(e.getComponent(),
                         e.getX(), e.getY());
       }
     }
   }
 
   private void doMouseEvent(MouseEvent e) {
     if(e.getButton() == MouseEvent.BUTTON1) {
       selectedPath = tree.getPathForLocation(e.getX(), e.getY());
       newViewEvent(selectedPath);
     } else if(e.getButton() == MouseEvent.BUTTON2) {
       selectedPath = tree.getPathForLocation(e.getX(), e.getY());
       if(selectedPath != null) {
         DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode) selectedPath.getLastPathComponent();
         ViewChangeEvent evt = new ViewChangeEvent(ViewChangeEvent.VIEW_CONCEPTS);
         evt.setViewObject(dmtn.getUserObject());
         evt.setInNewTab(true);
 
         fireViewChangeEvent(evt);
       }
     }
   }
 
   private void newViewEvent(TreePath path)
   {
     if(path != null) {
       DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode) path.getLastPathComponent();
   	  
       Object o = dmtn.getUserObject();
       if((o instanceof ClassNode) || (o instanceof AttributeNode)) 
       {
     	if(o instanceof InheritedAttributeNode) {
           // inherited attribute
           InheritedAttributeNode attNode = (InheritedAttributeNode)o;
           
           String fpath = attNode.getFullPath();
           int sd = fpath.lastIndexOf(".");
           String sourceClassName = fpath.substring(0, sd);
           String attributeName = fpath.substring(sd+1);
           
           AttributeNode anode = findSuper(sourceClassName,attributeName);
           
           if (anode != null) {
               ViewChangeEvent evt = new ViewChangeEvent(ViewChangeEvent.VIEW_INHERITED);
               evt.setViewObject(anode);
               evt.setInNewTab(false);
               fireViewChangeEvent(evt);
           }
     	}
         else {
             // normal attribute
             ViewChangeEvent evt = new ViewChangeEvent(ViewChangeEvent.VIEW_CONCEPTS);
             evt.setViewObject(dmtn.getUserObject());
             evt.setInNewTab(false);
 
             fireViewChangeEvent(evt);
         }
       } else if(o instanceof AssociationNode) {
         ViewChangeEvent evt = new ViewChangeEvent(ViewChangeEvent.VIEW_ASSOCIATION);
         evt.setViewObject(dmtn.getUserObject());
         evt.setInNewTab(false);
 
         fireViewChangeEvent(evt);
       }
       else if(o instanceof ValueDomainNode) {
         ViewChangeEvent evt = new ViewChangeEvent(ViewChangeEvent.VIEW_VALUE_DOMAIN);
         evt.setViewObject(dmtn.getUserObject());
         evt.setInNewTab(false);
         
         fireViewChangeEvent(evt);
       }
       else if(o instanceof ValueMeaningNode) {
         ViewChangeEvent evt = new ViewChangeEvent(ViewChangeEvent.VIEW_VALUE_MEANING);
         evt.setViewObject(dmtn.getUserObject());
         evt.setInNewTab(false);
         
         fireViewChangeEvent(evt);
       }
     }
   }
 
   /**
    * Find the given attribute in the given class's inheritance hierarchy. 
    * Returns the first non-inherited AttributeNode.
    * @param className fully qualified class name
    * @param attributeName attribute name
    * @return
    */
   private AttributeNode findSuper(String className, String attributeName) {
 
       // find the superclass by searching the OCR's for a generalization 
       
       List<ObjectClassRelationship> ocrs = 
           elements.getElements(DomainObjectFactory.newObjectClassRelationship());
       
       String superClassName = null;
       for(ObjectClassRelationship ocr : ocrs) {
           if (ocr.getType() == ObjectClassRelationship.TYPE_IS && 
                   ocr.getSource().getLongName().equals(className)) {
               superClassName = ocr.getTarget().getLongName();
               break;
           }
       }
       
       if (superClassName == null) {
           System.err.println("Superclass not found for "+className);
           return null;
       }
           
       // find the super class in the tree
       
       int div = superClassName.lastIndexOf(".");
       String sPackage = superClassName.substring(0, div);
       String sName = superClassName.substring(div+1);
       
       for(Object pchild : rootNode.getChildren()) {
           PackageNode pnode = (PackageNode)pchild;
           if (pnode.getFullPath().equals(sPackage)) {
               for(Object cchild : pnode.getChildren()) {
                   ClassNode cnode = (ClassNode)cchild;
                   if (cnode.getDisplay().equals(sName)) {
                       PackageNode inherited = null;
                       for(Object achild : cnode.getChildren()) {
                          if ("Inherited Attributes".equals(
                                  ((UMLNode)achild).getDisplay())) {
                              // remember the inheritance subtree for later
                               inherited = (PackageNode)achild;
                           }
                          else if (achild instanceof AttributeNode) {
                               AttributeNode anode = (AttributeNode)achild;
                               if (anode.getDisplay().equals(attributeName)) {
                                   return anode;
                               }
                           }
                       }
                      // attribute wasn't found, check inheritance subtree
                       if (inherited != null) {
                           for(Object achild : inherited.getChildren()) {
                               AttributeNode anode = (AttributeNode)achild;
                               if (anode.getDisplay().equals(attributeName)) {
                                   return findSuper(cnode.getFullPath(), attributeName);
                               }
                           }
                           
                       }
                   }
               } 
           }
       }
       
       return null;
   }
 
   private void buildPopupMenu() {
     JMenuItem newTabItem = new JMenuItem("Open in New Tab");
     JMenuItem openItem = new JMenuItem("Open");
 
     newTabItem.setActionCommand("OPEN_NEW_TAB");
     openItem.setActionCommand("OPEN");
 
     nodePopup = new JPopupMenu();
 
     newTabItem.addActionListener(this);
     openItem.addActionListener(this);
 
     nodePopup.add(openItem);
     nodePopup.add(newTabItem);
     nodePopup.addSeparator();
 
     
     JMenu preferenceMenu = new JMenu("Preferences");
     inheritedAttItem = new JCheckBoxMenuItem(PropertyAccessor.getProperty("preference.inherited.attributes"));
     inheritedAttItem.setActionCommand("PREF_INHERITED_ATT");
     assocItem = new JCheckBoxMenuItem(PropertyAccessor.getProperty("preference.view.association"));
     assocItem.setActionCommand("PREF_INCLASS_ASSOC");
     sortItem = new JCheckBoxMenuItem(PropertyAccessor.getProperty("preference.sort.elements"));
     sortItem.setActionCommand("PREF_SORT_ELEM");
     
     UserPreferences prefs = UserPreferences.getInstance();
     inheritedAttItem.setSelected(prefs.getShowInheritedAttributes());
     inheritedAttItem.addActionListener(this);
 
     assocItem.setSelected(prefs.getViewAssociationType().equalsIgnoreCase("true"));
     assocItem.addActionListener(this);
 
     sortItem.setSelected(prefs.getSortElements());
     sortItem.addActionListener(this);
     
     preferenceMenu.add(inheritedAttItem);
     preferenceMenu.add(assocItem);
     preferenceMenu.add(sortItem);
 
     JMenuItem collapseAllItem = new JMenuItem("Collapse All");
     collapseAllItem.setActionCommand("COLLAPSE_ALL");
     collapseAllItem.addActionListener(this);
 
     JMenuItem expandAllItem = new JMenuItem("Expand All");
     expandAllItem.setActionCommand("EXPAND_ALL");
     expandAllItem.addActionListener(this);
     
     blankPopup = new JPopupMenu();
     blankPopup.add(collapseAllItem);
     blankPopup.add(expandAllItem);
     blankPopup.add(preferenceMenu);
 
     //     nodePopup.add(collapseAllItem);
     //     nodePopup.add(expandAllItem);
     //     nodePopup.add(preferenceMenu);
     
     tree.addMouseListener(this);
    
   } 
 
 
   private DefaultMutableTreeNode buildTree() {
       
     DefaultMutableTreeNode node = new DefaultMutableTreeNode(rootNode);
     return doNode(node, rootNode);
     
   }
 
   private DefaultMutableTreeNode doNode(DefaultMutableTreeNode node, UMLNode parentNode) {
 
     Set<UMLNode> children = parentNode.getChildren();
     for(UMLNode child : children) {
       DefaultMutableTreeNode newNode = 
         new DefaultMutableTreeNode(child);
 
       if(!(child instanceof ValidationNode) && !(child instanceof AssociationEndNode))
         node.add(newNode);  
       doNode(newNode, child);
     }
 
     return node;
     
   }
   
   public void treeChange(TreeEvent event) 
   {
     this.remove(scrollPane);
     rootNode = TreeBuilder.getInstance().getRootNode();
           
     try
       {
         initUI();
       }
     catch (Exception e)
       {
       
       }
    
     this.updateUI();
   }
   
   public void navigate(NavigationEvent event) 
   {    
     DefaultMutableTreeNode selected = 
       (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
     
     if(event.getType() == NavigationEvent.NAVIGATE_NEXT
        && selected.getNextNode() != null) 
       {      
         TreePath path =  new TreePath(selected.getNextNode().getPath());
         tree.setSelectionPath(path);
         tree.scrollPathToVisible(path);
         newViewEvent(path);
       }
     else 
       if(event.getType() == NavigationEvent.NAVIGATE_PREVIOUS
          && selected.getPreviousNode() != null) 
         {
           TreePath path = new TreePath(selected.getPreviousNode().getPath());
           tree.setSelectionPath(path);
           tree.scrollPathToVisible(path);
           newViewEvent(path);            
         }
     
   }
   
   public void search(SearchEvent event) 
   {
     DefaultMutableTreeNode selected =
       (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
 
     TreePath path = null;
     if(event.getSearchFromBeginning())
       selected = null;
     //if(event.getSearchByLongName()) {
     //search the tree in a forward direction from top to bottom
     if(!event.getSearchFromBottom()) {
       
       //if no node is selected then select the first node 
       if(selected == null)
         selected = (DefaultMutableTreeNode)tree.getPathForRow(0).getLastPathComponent();
 
       selected = selected.getNextNode();
       while(selected != null) {
         AbstractUMLNode n = (AbstractUMLNode) selected.getUserObject();
         if(event.getSearchByLongName()) 
           {
             Concept [] concepts = NodeUtil.getConceptsFromNode(n);
             for(int i=0; i < concepts.length; i++) {
               //System.out.println("Concepts" + i +" "+ concepts[i]);
               if(!event.getSearchString().toLowerCase().contains(concepts[i].getLongName().toLowerCase())) 
                 {
                   path = new TreePath(selected.getPath());
                   tree.setSelectionPath(path);
                   tree.scrollPathToVisible(path);
                   newViewEvent(path);
                   selected = null;
                   break;
                 }
             }    
           }
         else {
           //if there is a match then select that node in the tree
           if(event.getExactMatch()) {
             if((n.getDisplay().toLowerCase()).equals(event.getSearchString().toLowerCase()))
               {
                 path = new TreePath(selected.getPath());
                 tree.setSelectionPath(path);
                 tree.scrollPathToVisible(path);
                 newViewEvent(path);
                 break;        
               }
           }
           else 
             if((n.getDisplay().toLowerCase()).contains(event.getSearchString().toLowerCase())) 
               {
                 path = new TreePath(selected.getPath());
                 tree.setSelectionPath(path);
                 tree.scrollPathToVisible(path);
                 newViewEvent(path);
                 break;        
               }
           }
         if(selected != null)
           selected = selected.getNextNode();
       }
     }
     
     //search the tree in backward direction from bottom to top
     else 
       {
         //if no node is selected then select the last node in the tree
         if(selected == null)
           selected = (DefaultMutableTreeNode)tree.getPathForRow(tree.getRowCount()-1).getLastPathComponent();
 
         selected = selected.getPreviousNode();
         while(selected != null) {
           AbstractUMLNode n = (AbstractUMLNode) selected.getUserObject();
           if(event.getSearchByLongName()) 
             {
               Concept [] concepts = NodeUtil.getConceptsFromNode(n);
               for(int i=0; i < concepts.length; i++) {
                 //System.out.println("Concepts" + i +" "+ concepts[i]);
                 if(event.getSearchString().toLowerCase().contains(concepts[i].getLongName().toLowerCase())) 
                   {
                     path = new TreePath(selected.getPath());
                     tree.setSelectionPath(path);
                     tree.scrollPathToVisible(path);
                     newViewEvent(path);
                     selected = null;
                     break;
                   }
               }    
             }
         
           else {  
             //if there is a match then select that node in the tree
             if((n.getDisplay().toLowerCase()).contains(event.getSearchString().toLowerCase())) 
               {
                 path = new TreePath(selected.getPath());
                 tree.setSelectionPath(path);
                 tree.scrollPathToVisible(path);
                 newViewEvent(path);
                 break;        
               }
           }
           if(selected != null)
             selected = selected.getPreviousNode();
         }
       }
     
 
     //if no match was found display error message
     if(path == null)
       {
         //int result = JOptionPane.showConfirmDialog(null, "Text Not Found", "No Match", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
         //if(result == JOptionPane.YES_OPTION)
         //  search(event);
         
         JOptionPane.showMessageDialog(null,"Text Not Found", "No Match",JOptionPane.ERROR_MESSAGE);
       }
 
   }
 
   private void fireViewChangeEvent(ViewChangeEvent evt) {
     fireNavigationEvent(new NavigationEvent(NavigationEvent.NAVIGATE_NEXT));
 
     for(ViewChangeListener vcl : viewListeners) 
       vcl.viewChanged(evt);
   }
   
   private void fireNavigationEvent(NavigationEvent evt) {
     for(NavigationListener nl : navigationListeners) {
       nl.navigate(evt);
     }
   }
   
 
   public void preferenceChange(UserPreferencesEvent event) 
   {
     if(event.getTypeOfEvent() == UserPreferencesEvent.SORT_ELEMENTS) 
     {
       Boolean sortClasses = new Boolean (event.getValue());
       showSortedTree(sortClasses);
       TreeUtil.expandAll(tree, rootTreeNode);
 
       if (selectedPath != null) {
           // reselect the node which was selected
           List pathList = Arrays.asList(selectedPath.getPath());
           List newPath = translatePath(pathList, rootTreeNode);
          if (newPath != null)
            tree.setSelectionPath(new TreePath(newPath.toArray()));
       }
     }
   }
 
 
 }
