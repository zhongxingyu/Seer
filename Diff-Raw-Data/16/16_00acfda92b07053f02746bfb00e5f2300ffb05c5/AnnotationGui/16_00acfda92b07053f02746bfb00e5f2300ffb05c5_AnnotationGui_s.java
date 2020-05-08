 // AnnotationGui.java:  the user interface for cytoscape annotations
 
 /** Copyright (c) 2002 Institute for Systems Biology and the Whitehead Institute
  **
  ** This library is free software; you can redistribute it and/or modify it
  ** under the terms of the GNU Lesser General Public License as published
  ** by the Free Software Foundation; either version 2.1 of the License, or
  ** any later version.
  ** 
  ** This library is distributed in the hope that it will be useful, but
  ** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
  ** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
  ** documentation provided hereunder is on an "as is" basis, and the
  ** Institute for Systems Biology and the Whitehead Institute 
  ** have no obligations to provide maintenance, support,
  ** updates, enhancements or modifications.  In no event shall the
  ** Institute for Systems Biology and the Whitehead Institute 
  ** be liable to any party for direct, indirect, special,
  ** incidental or consequential damages, including lost profits, arising
  ** out of the use of this software and its documentation, even if the
  ** Institute for Systems Biology and the Whitehead Institute 
  ** have been advised of the possibility of such damage.  See
  ** the GNU Lesser General Public License for more details.
  ** 
  ** You should have received a copy of the GNU Lesser General Public License
  ** along with this library; if not, write to the Free Software Foundation,
  ** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
  **/
 
 //----------------------------------------------------------------------------------------
 // $Revision$
 // $Date$
 // $Author$
 //----------------------------------------------------------------------------------------
 package cytoscape.data.annotation;
 //----------------------------------------------------------------------------------------
 import java.awt.event.*;
 import javax.swing.*;
 import javax.swing.event.*;
 import javax.swing.border.*;
 import java.awt.*;
 import java.awt.event.*;
 import javax.swing.tree.*;
 import javax.swing.JOptionPane;
 
 import java.io.*;
 import java.util.*;
 
 import giny.view.NodeView;
 
 import cytoscape.data.annotation.*;
 import cytoscape.data.servers.*;
 //import cytoscape.layout.*;
 import cytoscape.data.GraphObjAttributes;
 import cytoscape.data.CyNetwork;
 import cytoscape.data.Semantics;
 import cytoscape.view.CyWindow;
 //----------------------------------------------------------------------------------------
 /**
  */
 public class AnnotationGui extends AbstractAction {
   protected CyWindow cyWindow;
   protected BioDataServer dataServer;
   protected String defaultSpecies;
   AnnotationDescription [] annotationDescriptions;
   JTree availableAnnotationsTree;
   JTree currentAnnotationsTree;
   JList actionListBox;
   DefaultListModel actionListModel;
   int actionListBoxCurrentSelection;
   TreePath annotationPath;
   String currentAnnotationCategory;
   //AttributeLayout attributeLayouter;
 
   JDialog mainDialog;
   JButton annotateNodesButton;
   JButton layoutByAnnotationButton;
   JButton addSharedAnnotationEdgesButton;
 
 //----------------------------------------------------------------------------------------
 public AnnotationGui (CyWindow cyWindow)
 {
   super ();
   this.cyWindow = cyWindow;
  dataServer = cyWindow.getCytoscapeObj().getBioDataServer ();
   if (dataServer != null)
    annotationDescriptions = dataServer.getAnnotationDescriptions ();
   
   //defaultSpecies = Semantics.getDefaultSpecies(cyWindow.getNetwork(),cyWindow.getCytoscapeObj() );
 
 } // ctor
 //----------------------------------------------------------------------------------------
 public void actionPerformed (ActionEvent e) 
 {
   if (dataServer == null) {
     JOptionPane.showMessageDialog (null, "No annotations are available.",
                                    "Error!", JOptionPane.ERROR_MESSAGE);
     return;
    }
   
   //if (this.attributeLayouter == null) {
   //    this.attributeLayouter = new AttributeLayout (cyWindow);
   //}
   
   defaultSpecies = Semantics.getDefaultSpecies(cyWindow.getNetwork(),cyWindow.getCytoscapeObj() );
   //if (this.mainDialog == null) {
   mainDialog = new Gui ("Annotation");
   mainDialog.pack ();
   mainDialog.setLocationRelativeTo (cyWindow.getMainFrame ());
       //}
   mainDialog.setVisible (true);
 
 } // actionPerformed
 //----------------------------------------------------------------------------------------
 protected class Gui extends JDialog {
 
 Gui (String title) 
 {
   super (cyWindow.getMainFrame (), false);
   setTitle (title);
   setContentPane (createWidgets ());
   setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
 }
 //------------------------------------------------------------------------------
 private JPanel createWidgets ()
 {
   JPanel mainPanel = new JPanel ();
   mainPanel.setLayout (new BorderLayout ());
 
   JPanel topPanel = new JPanel ();
   topPanel.setLayout (new GridLayout(0, 2));
 
   availableAnnotationsTree = createAvailableAnnotationsTree ();
   currentAnnotationsTree = createNodeSelectionTree ();
 
   availableAnnotationsTree.addTreeSelectionListener (new AddAnnotationTreeSelectionListener ());
   currentAnnotationsTree.addTreeSelectionListener (new SelectNodesTreeSelectionListener ());
 
 
   JScrollPane chooserScrollPane = new JScrollPane (availableAnnotationsTree);
   JPanel chooserPanel = new JPanel ();
   chooserPanel.setLayout (new BorderLayout ());
   chooserPanel.add (chooserScrollPane, BorderLayout.CENTER);
   annotateNodesButton = new JButton ("Apply Annotation to All Nodes");
   annotateNodesButton.setEnabled (false);
   chooserPanel.add (annotateNodesButton, BorderLayout.SOUTH);
   chooserPanel.setBorder (BorderFactory.createCompoundBorder (
                           BorderFactory.createTitledBorder(
                          "Available"),
                           BorderFactory.createEmptyBorder(10,10,10,10)));
 
   annotateNodesButton.addActionListener (new ApplyAnnotationAction ());
   topPanel.add (chooserPanel);  
 
   JPanel currentAnnotationsButtonPanel = new JPanel ();
   currentAnnotationsButtonPanel.setLayout (new GridLayout (0,2));
   layoutByAnnotationButton = new JButton ("Layout");
   layoutByAnnotationButton.setEnabled (false);
   currentAnnotationsButtonPanel.add (layoutByAnnotationButton);
   layoutByAnnotationButton.addActionListener (new LayoutByAnnotationAction ());
   addSharedAnnotationEdgesButton = new JButton ("Add Edges");
   currentAnnotationsButtonPanel.add (addSharedAnnotationEdgesButton);
   addSharedAnnotationEdgesButton.setEnabled (false);
   addSharedAnnotationEdgesButton.addActionListener (new DrawSharedEdgesAnnotationAction ());
 
   JScrollPane currentChoicesScrollPane = new JScrollPane (currentAnnotationsTree);
   currentChoicesScrollPane.setPreferredSize (chooserScrollPane.getPreferredSize ());
   JPanel panel3 = new JPanel ();
   panel3.setLayout (new BorderLayout ());
   panel3.add (currentChoicesScrollPane, BorderLayout.CENTER);
   panel3.setBorder (BorderFactory.createCompoundBorder (
                           BorderFactory.createTitledBorder(
                          "Current Annotations"),
                           BorderFactory.createEmptyBorder(10,10,10,10)));
 
   panel3.add (currentAnnotationsButtonPanel, BorderLayout.SOUTH);
   topPanel.add (panel3);
 
   mainPanel.add (topPanel, BorderLayout.CENTER);
   JPanel dismissButtonPanel = new JPanel ();
   JButton dismissButton = new JButton ("Dismiss");
   dismissButton.addActionListener (new DismissAction ());
   dismissButtonPanel.add (dismissButton);
   mainPanel.add (dismissButtonPanel, BorderLayout.SOUTH);
 
   return mainPanel;
 
 } // createWidgets
 //------------------------------------------------------------------------------
 private void expandAll (JTree tree, TreePath parent, boolean expand) 
 {
   TreeNode node = (TreeNode) parent.getLastPathComponent ();
   if (node.getChildCount() >= 0) {
     for (Enumeration e=node.children(); e.hasMoreElements(); ) {
       TreeNode n = (TreeNode) e.nextElement();
       TreePath path = parent.pathByAddingChild (n);
       expandAll (tree, path, expand);
       } // for
     } // if
 
   if (expand)
     tree.expandPath(parent);
   else
     tree.collapsePath(parent);
 
 } // expandAll
 //------------------------------------------------------------------------------
 protected JTree createAvailableAnnotationsTree ()
 {
   DefaultMutableTreeNode root = new DefaultMutableTreeNode ("Available Annotations");
   createTreeNodes (root, annotationDescriptions);
   JTree tree = new JTree (root);
   tree.getSelectionModel().setSelectionMode (TreeSelectionModel.SINGLE_TREE_SELECTION);
   tree.setRootVisible (false);
   tree.setShowsRootHandles (true);
   // expandAll (tree, new TreePath (root), true);
   return tree;
 }
 //------------------------------------------------------------------------------
 protected JTree createNodeSelectionTree ()
 {
   DefaultMutableTreeNode root = new DefaultMutableTreeNode ("Annotations Categories");
   JTree tree = new JTree (root);
   //tree.getSelectionModel().setSelectionMode (TreeSelectionModel.SINGLE_TREE_SELECTION);
   tree.setRootVisible (false);
   tree.setShowsRootHandles (true);
   expandAll (tree, new TreePath (root), true);
   return tree;
 
 } // createNodeSelectionTree
 //------------------------------------------------------------------------------
 class AddAnnotationTreeSelectionListener implements TreeSelectionListener {
 
   public void valueChanged (TreeSelectionEvent e) {
     DefaultMutableTreeNode node = 
       (DefaultMutableTreeNode) availableAnnotationsTree.getLastSelectedPathComponent ();
     if (node == null) return;
     if (!node.isLeaf ()) return;
     annotationPath = availableAnnotationsTree.getSelectionPaths()[0];
     Object nodeInfo = node.getUserObject ();
     annotateNodesButton.setEnabled (true);
     }
 
 } // inner class AddAnnotationTreeSelectionListener
 //-----------------------------------------------------------------------------------
 class SelectNodesTreeSelectionListener implements TreeSelectionListener {
 
   public void valueChanged (TreeSelectionEvent e) {
     DefaultMutableTreeNode node = 
         (DefaultMutableTreeNode) currentAnnotationsTree.getLastSelectedPathComponent ();
     if (node == null) return;
     layoutByAnnotationButton.setEnabled (!node.isLeaf ());
     addSharedAnnotationEdgesButton.setEnabled (!node.isLeaf ());
     if (!node.isLeaf ()) return;
 
     String callerID = "AnnotationGui.SelectNodesTreeSelectionListener.valueChanged";
     CyNetwork network = cyWindow.getNetwork();
     GraphObjAttributes nodeAttributes = network.getNodeAttributes();
     network.beginActivity(callerID);
     //unselect every node in the graph
     for (Iterator nvi = cyWindow.getView().getNodeViewsIterator(); nvi.hasNext(); ) {
         ((NodeView)nvi.next()).setSelected(false);
     }
     TreePath [] selectedPaths = currentAnnotationsTree.getSelectionPaths();
     HashMap selectionHash = extractAnnotationsFromSelection (selectedPaths);
     String [] annotationNames = (String []) selectionHash.keySet().toArray (new String [0]);
     for (int i=0; i < annotationNames.length; i++) {
       String name = annotationNames [i];
       Vector categoryList = (Vector) selectionHash.get (name);
       for (Iterator nvi = cyWindow.getView().getNodeViewsIterator(); nvi.hasNext(); ) {
           NodeView nv = (NodeView)nvi.next();
           String canonicalName = nodeAttributes.getCanonicalName(nv.getNode());
           if (canonicalName == null) {continue;}
           Object attributeValue = nodeAttributes.getValue(name, canonicalName);
           if (attributeValue == null) {continue;}
           String [] parsedCategories = 
           GraphObjAttributes.unpackPossiblyCompoundStringAttributeValue (attributeValue);
           for (int pc=0; pc<parsedCategories.length; pc++) {
               if (categoryList.contains(parsedCategories[pc])) {
                   nv.setSelected(true);
               }
           }
       }
     }//end for
     cyWindow.redrawGraph(false, true);
 
   } // valueChanged
 
 //-----------------------------------------------------------------------------
 /**
  * create a hashmap, <String, String []>: 
  *
  *   "KEGG Metabolic Pathway (level 1)" -> (Genetic Information Processing)
  *   "KEGG Metabolic Pathway (level 2)" -> (Amino Acid Metabolism, Nucleotide Metabolism)
  *
  * this method is brittle, and will fail if the structure of the tree changes:
  * it expects 
  *    level 0: root
  *    level 1: a standard annotation name (see above)
  *    level 2: standard annotation category name (also see above)
  */
 protected HashMap extractAnnotationsFromSelection (TreePath [] paths)
 {
   HashMap hash = new HashMap ();
 
   for (int i=0; i < paths.length; i++) {
     String annotationName  = paths[i].getPathComponent (1).toString ();
     String annotationValue = paths[i].getPathComponent (2).toString ();
     Vector list;
     if (!hash.containsKey (annotationName)) {
       list = new Vector ();
       hash.put (annotationName, list);
       }
     list = (Vector) hash.get (annotationName);
     list.add (annotationValue);
      } // for i
 
   String [] keys = (String []) hash.keySet().toArray (new String [0]);
 
   return hash;
 
 } // extractAnnotationsFromSelection
 //----------------------------------------------------------------------------------------
 } // inner class SelectNodesTreeSelectionListener
 //-----------------------------------------------------------------------------------
 protected void createTreeNodes (DefaultMutableTreeNode root, 
                                 AnnotationDescription [] descriptions)
 // for each of the descriptions, and only if the description is of a species in the 
 // current graph:  create a 'topLevelName' which will be the branch of the JTree,
 // and a set of leaves for each logical level in that description's ontology
 {
   if (descriptions == null || descriptions.length == 0) return;
 
   Set speciesInGraph = Semantics.getSpeciesInNetwork(cyWindow.getNetwork());
 
   DefaultMutableTreeNode branch = null;
   DefaultMutableTreeNode leaf = null;
   Vector topLevelNamesList = new Vector ();
 
   for (int i=0; i < descriptions.length; i++) {
     String species = descriptions[i].getSpecies ();
     if (!speciesInGraph.contains (species)) {
       continue;
       }
     topLevelNamesList.add (descriptions[i].getCurator () + ", " +
                            descriptions[i].getType () + ", " +
                            descriptions[i].getSpecies ());
     branch = new DefaultMutableTreeNode (descriptions [i]);
     Annotation annotation = dataServer.getAnnotation (descriptions [i]);
    
     if (annotation == null) {
       continue;
       }
     int maxDepth = annotation.maxDepth ();
     for (int level=0; level < maxDepth; level++) 
       branch.add (new DefaultMutableTreeNode (new Integer (level + 1)));
     root.add (branch);
     }
 
 } // createTreeNodes
 //-----------------------------------------------------------------------------------
 public class DismissAction extends AbstractAction 
 {
   DismissAction () {super ("");}
 
   public void actionPerformed (ActionEvent e) {
     Gui.this.setVisible(false);
     }
 
 } // DismissAction
 //-----------------------------------------------------------------------------
 public class LayoutByAnnotationAction  extends AbstractAction 
 {
   LayoutByAnnotationAction () {super ("");}
 
   public void actionPerformed (ActionEvent e) {
       String title = "Operation not supported";
       String message = "This operation is not yet supported.";
       JOptionPane.showMessageDialog(cyWindow.getMainFrame(), message,
                                     title, JOptionPane.ERROR_MESSAGE);
     //attributeLayouter.doCallback (currentAnnotationCategory, AttributeLayout.DO_LAYOUT);
     }
 
 } // LayoutByAnnotationAction
 //-----------------------------------------------------------------------------
 public class DrawSharedEdgesAnnotationAction extends AbstractAction 
 {
   DrawSharedEdgesAnnotationAction  () {super ("");}
 
   public void actionPerformed (ActionEvent e) {
       String title = "Operation not supported";
       String message = "This operation is not yet supported.";
       JOptionPane.showMessageDialog(cyWindow.getMainFrame(), message,
                                     title, JOptionPane.ERROR_MESSAGE);
     //attributeLayouter.doCallback (currentAnnotationCategory, AttributeLayout.CREATE_EDGES);
     }
 
 } // DrawSharedEdgesAnnotationAction
 //-----------------------------------------------------------------------------
 } // inner class Gui
 //-----------------------------------------------------------------------------------
 public String addAnnotationToNodes (AnnotationDescription aDesc, int level)
 {
   String callerID = "AnnotationGui.addAnnotationToNodes";
   cyWindow.getNetwork().beginActivity(callerID);
   GraphObjAttributes nodeAttributes = cyWindow.getNetwork().getNodeAttributes();
      // something like "GO biological process" or "KEGG metabolic pathway"
   String baseAnnotationName = aDesc.getCurator () + " " + aDesc.getType ();
   String annotationNameAtLevel = baseAnnotationName  + " (level " + level + ")";
   String annotationNameForLeafIDs = baseAnnotationName  + " leaf IDs";
      // make a fresh start
   nodeAttributes.deleteAttribute (annotationNameAtLevel);
   nodeAttributes.deleteAttribute (annotationNameForLeafIDs);
 
 
   HashMap nodeNameMap = nodeAttributes.getNameMap ();
   String [] canonicalNodeNames = (String []) nodeNameMap.values().toArray(new String [0]);
 
   int unAnnotatedNodeCount = 0;
   for (int i=0; i < canonicalNodeNames.length; i++) {
     String [][] fullAnnotations = dataServer.getAllAnnotations (aDesc, canonicalNodeNames [i]);
     if (fullAnnotations.length == 0)
       unAnnotatedNodeCount++;
     else {
       String [] uniqueAnnotationsAtLevel = collapseToUniqueAnnotationsAtLevel (fullAnnotations, level);
       for(int j=0;j<uniqueAnnotationsAtLevel.length;j++) {
 	  nodeAttributes.append(annotationNameAtLevel,
 				canonicalNodeNames[i],
 				uniqueAnnotationsAtLevel[j]);
       }
       int [] annotationIDs = dataServer.getClassifications (aDesc, canonicalNodeNames [i]);
       Integer [] integerArray = new Integer [annotationIDs.length];
       for (int j=0; j < annotationIDs.length; j++)
         integerArray [j] = new Integer (annotationIDs [j]);
       //nodeAttributes.add (annotationNameForLeafIDs, canonicalNodeNames [i], integerArray);
       } // else: this node is annotated
     } // for i
 
   nodeAttributes.setCategory (annotationNameAtLevel, "annotation");
   cyWindow.getNetwork().endActivity(callerID);
   
   return annotationNameAtLevel;
 
 } // addAnnotationToNodes
 //----------------------------------------------------------------------------------------
 /**
  *  return only the unique categories (typically representing gene annotations) 
  *  which exist at the specified level.
  *  genes (for example) may be annotated as belonging to two or more different categories,
  *  but at a higher logical level, these different categories may merge.  for example,
  *  the halobacterium gene VNG0623G is classified by KEGG, in its 3-level ontology
  *  as belonging to two categories at the most specific (third) level
  *  <ol>
  *    <li> Valine, leucine and isoleucine degradation
  *    <li> Propanoate metabolism
  *  <ol>
  *  At level two, the annotation is:
  *  <ol>
  *    <li> Amino Acid Metabolism
  *    <li> Carbohydrate Metabolism
  *  <ol>
  *  At level one, the annotation is simply
  *  <ol>
  *    <li>Metabolism
  *  <ol>
  *  This method calculates and returns the unique annotations for a biological entity
  *  at the specified level.
  */
 private String [] collapseToUniqueAnnotationsAtLevel (String [][] fullAnnotations, int level)
 {
   Vector collector = new Vector ();
 
   for (int i=0; i < fullAnnotations.length; i++) {
     int indexOfClosestAvailableLevel = level - 1;
     if (indexOfClosestAvailableLevel > (fullAnnotations [i].length - 1))
       indexOfClosestAvailableLevel = fullAnnotations [i].length - 1;
     String annotationAtLevel = fullAnnotations [i][indexOfClosestAvailableLevel];
     if (!collector.contains (annotationAtLevel))
       collector.add (annotationAtLevel);
     } // for i
 
   return (String []) collector.toArray (new String [0]);
 
 } // collapseToUniqueAnnotationsAtLevel
 //----------------------------------------------------------------------------------------
 class ApplyAnnotationAction extends AbstractAction {
   ApplyAnnotationAction () {
     super (""); 
     }
 
   public void actionPerformed (ActionEvent e) {
     int max = annotationPath.getPathCount ();
     for (int i=0; i < max; i++) {
       DefaultMutableTreeNode node = (DefaultMutableTreeNode) annotationPath.getPathComponent (i);
       Object userObj = node.getUserObject ();
       }
 
     DefaultMutableTreeNode node1 = (DefaultMutableTreeNode) annotationPath.getPathComponent (1);
     AnnotationDescription aDesc = (AnnotationDescription) node1.getUserObject ();
     DefaultMutableTreeNode node2 = (DefaultMutableTreeNode) annotationPath.getPathComponent (2);
     int level = ((Integer) node2.getUserObject ()).intValue ();
     if (aDesc == null) return;
     String callerID = "ApplyAnnotationAction.actionPerformed";
     cyWindow.getNetwork().beginActivity(callerID);
     currentAnnotationCategory = addAnnotationToNodes (aDesc, level);
     Object [] uniqueAnnotationValues =
     cyWindow.getNetwork().getNodeAttributes().getUniqueValues (currentAnnotationCategory);
     if (uniqueAnnotationValues != null && 
         uniqueAnnotationValues.length > 0 && 
         uniqueAnnotationValues [0].getClass() == "string".getClass ()) {
       java.util.Arrays.sort (uniqueAnnotationValues, ( Comparator )String.CASE_INSENSITIVE_ORDER);
       appendToSelectionTree (currentAnnotationCategory, uniqueAnnotationValues);
       }
     cyWindow.getNetwork().endActivity(callerID);
     }
 
 //--------------------------------------------------------------------------------------
 protected void appendToSelectionTree (String currentAnnotationCategory, 
                                       Object [] uniqueAnnotationValues)
 {
   DefaultMutableTreeNode branch = new DefaultMutableTreeNode (currentAnnotationCategory);
 
   for (int i=0; i < uniqueAnnotationValues.length; i++)
     branch.add (new DefaultMutableTreeNode (uniqueAnnotationValues [i]));
 
   DefaultMutableTreeNode root = (DefaultMutableTreeNode) currentAnnotationsTree.getModel().getRoot();
   DefaultTreeModel model = (DefaultTreeModel) currentAnnotationsTree.getModel ();
   model.insertNodeInto (branch, root, root.getChildCount ());
   currentAnnotationsTree.scrollPathToVisible (new TreePath (branch.getPath ()));
   model.reload ();
 
 } // appendToSelectionTree
 //-----------------------------------------------------------------------------
 } // inner class ApplyAnnotationAction
 class ActionListBoxSelectionListener implements ListSelectionListener {
 
   public void valueChanged (ListSelectionEvent e) {
     if (e.getValueIsAdjusting ()) return;
     ListSelectionModel lsm = (ListSelectionModel) e.getSource();
     Vector selectedCategoryNames = new Vector ();
     if (!lsm.isSelectionEmpty()) {
       int minIndex = lsm.getMinSelectionIndex ();
       int maxIndex = lsm.getMaxSelectionIndex ();
       for (int i = minIndex; i <= maxIndex; i++) {
         if (lsm.isSelectedIndex (i)) {
           Object obj = actionListModel.elementAt (i);
           currentAnnotationCategory = (String) obj;
           }
         } // for i
       } // if !empty
     } // valueChanged
 
 } // inner class AttributeListSelectionListener
 //-----------------------------------------------------------------------------
 } // class AnnotationGui
 
 
