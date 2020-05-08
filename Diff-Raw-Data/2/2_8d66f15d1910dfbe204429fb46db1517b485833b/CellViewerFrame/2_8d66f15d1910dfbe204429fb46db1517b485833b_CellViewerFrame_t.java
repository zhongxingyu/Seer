 /**
  * Project Wonderland
  *
  * Copyright (c) 2004-2008, Sun Microsystems, Inc., All Rights Reserved
  *
  * Redistributions in source code form must reproduce the above
  * copyright and this condition.
  *
  * The contents of this file are subject to the GNU General Public
  * License, Version 2 (the "License"); you may not use this file
  * except in compliance with the License. A copy of the License is
  * available at http://www.opensource.org/licenses/gpl-license.php.
  *
  * $Revision$
  * $Date$
  * $State$
  */
 package org.jdesktop.wonderland.modules.artimport.client.jme;
 
 import com.jme.scene.Node;
 import com.jme.scene.Spatial;
 import java.awt.Component;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.logging.Logger;
 import javax.swing.DefaultListModel;
 import javax.swing.JPopupMenu;
 import javax.swing.JTree;
 import javax.swing.ToolTipManager;
 import javax.swing.event.TreeSelectionEvent;
 import javax.swing.event.TreeSelectionListener;
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.DefaultTreeCellRenderer;
 import javax.swing.tree.DefaultTreeModel;
 import javax.swing.tree.TreePath;
 import org.jdesktop.mtgame.Entity;
 import org.jdesktop.mtgame.EntityComponent;
 import org.jdesktop.mtgame.PickInfo;
 import org.jdesktop.mtgame.RenderComponent;
 import org.jdesktop.wonderland.client.ClientContext;
 import org.jdesktop.wonderland.client.cell.Cell;
 import org.jdesktop.wonderland.client.cell.CellCache;
 import org.jdesktop.wonderland.client.cell.CellComponent;
 import org.jdesktop.wonderland.client.cell.CellManager;
 import org.jdesktop.wonderland.client.cell.CellRenderer;
 import org.jdesktop.wonderland.client.cell.CellStatusChangeListener;
 import org.jdesktop.wonderland.client.comms.WonderlandSession;
 import org.jdesktop.wonderland.client.input.Event;
 import org.jdesktop.wonderland.client.input.EventClassFocusListener;
 import org.jdesktop.wonderland.client.jme.CellRefComponent;
 import org.jdesktop.wonderland.client.jme.cellrenderer.CellRendererJME;
 import org.jdesktop.wonderland.client.jme.input.InputEvent3D;
 import org.jdesktop.wonderland.client.jme.input.KeyEvent3D;
 import org.jdesktop.wonderland.client.jme.input.MouseEvent3D;
 import org.jdesktop.wonderland.client.login.ServerSessionManager;
 import org.jdesktop.wonderland.client.login.LoginManager;
 import org.jdesktop.wonderland.common.cell.CellStatus;
 
 /**
  *
  * @author  paulby
  */
 public class CellViewerFrame extends javax.swing.JFrame {
 
     private ArrayList<Cell> rootCells = new ArrayList();
     private DefaultMutableTreeNode treeRoot = new DefaultMutableTreeNode("Root");
     private HashMap<Cell, DefaultMutableTreeNode> cellNodes = new HashMap();
     private HashMap<Entity, DefaultMutableTreeNode> entityNodes = new HashMap();
     
     private boolean active = false;
 
     private CellViewerEventListener cellViewerListener;
     
     private static final Logger logger = Logger.getLogger(CellViewerFrame.class.getName());
 
     private Entity jmeGraphEntity=null; // The entity currently showing in the jme graph panel
     
     /** Creates new form CellViewerFrame */
     public CellViewerFrame() {
         ServerSessionManager lm = LoginManager.getPrimary();
         WonderlandSession session = lm.getPrimarySession();
 
 //        if (sessions.length>1) {
 //            // TODO Implement multi session support in CellViewFrame
 //            logger.warning("CellViewFrame only supports a single session at the moment");
 //        }
         
         JPopupMenu.setDefaultLightWeightPopupEnabled(false);
         ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);
 
         initComponents();
         cellViewerListener = new CellViewerEventListener();
 
         CellManager.getCellManager().addCellStatusChangeListener(new CellStatusChangeListener() {
 
             public void cellStatusChanged(Cell cell, CellStatus status) {
                 DefaultMutableTreeNode node = cellNodes.get(cell);
                 
                 switch(status) {
                     case DISK :
                         if (node!=null)
                             ((DefaultTreeModel)cellTree.getModel()).removeNodeFromParent(node);
                         break;
                     case BOUNDS :
                         if (node==null) {
                             node = createJTreeNode(cell);
                         }
                         break;
                 }
             }
             
         });
         
         refreshCells(session);
         ((DefaultTreeModel)cellTree.getModel()).setRoot(treeRoot);
         
         jmeTree.setCellRenderer(new JmeTreeCellRenderer());
         jmeTree.addTreeSelectionListener(new TreeSelectionListener() {
 
             public void valueChanged(TreeSelectionEvent e) {
                 Object selectedNode = jmeTree.getLastSelectedPathComponent();
                 System.out.print("Selected "+selectedNode);
                 if (selectedNode!=null)
                     System.out.print("  "+((Spatial)selectedNode).getLocalTranslation()+"  world "+((Spatial)selectedNode).getWorldTranslation());
                 System.out.println();
             }
             
         });
         
         cellTree.setCellRenderer(new WonderlandCellRenderer());
         
     }
     
     
     /**
      * Show the JME scene graph for this node, find the 
      * @param node
      */
     private void showJMEGraph(Entity entity) {
         // Only update the graph if the panel is visible
         if (jTabbedPane1.getSelectedComponent()==jmeGraphPanel) {
             if (entity==null || entity.getComponent(RenderComponent.class)==null) {
                 // Clear the graph
                 jmeTree.setModel(null);
             } else {
                 Node root = ((RenderComponent)entity.getComponent(RenderComponent.class)).getSceneRoot();
                 while(root.getParent()!=null) {
         //            System.out.println("Finding root "+root);
                     root = root.getParent();
                 }
 
                 jmeTree.setModel(new JmeTreeModel(root));
              }
         }
         jmeGraphEntity = entity;
     }
     
     /**
      * Get the  cells from the cache and update the UI
      */
     private void refreshCells(WonderlandSession session) {
         CellCache cache = ClientContext.getCellCache(session);
         if (cache==null)
             return;
         
         for(Cell rootCell : cache.getRootCells()) {
             rootCells.add(rootCell);
         }
         
         populateJTree();
     }
     
     private void populateJTree() {
         for(Cell rootCell : rootCells) {
             DefaultMutableTreeNode root = createJTreeNode(rootCell);
             if (root!=null)
                 treeRoot.add(root);
         }
     }
     
     private DefaultMutableTreeNode createJTreeNode(Cell cell) {
         DefaultMutableTreeNode parentNode = cellNodes.get(cell.getParent());
 //        if (parentNode==null && !(cell instanceof RootCell)) {
 //            logger.severe("******* Null parent "+cell.getParent());
 //            return null;
 //        }
         
         
         DefaultMutableTreeNode ret = new DefaultMutableTreeNode(cell);
         
         CellRenderer cr = cell.getCellRenderer(Cell.RendererType.RENDERER_JME);
         if (cr!=null && cr instanceof CellRendererJME) {
             CellRendererJME crj = (CellRendererJME)cr;
             Entity e = crj.getEntity();
             DefaultMutableTreeNode entityNode = new DefaultMutableTreeNode(e);
             entityNodes.put(e, entityNode);
             ret.add(entityNode);
 
             addChildEntities(e, entityNode);
         }
                 
         cellNodes.put(cell, ret);
         if (parentNode==null)
             parentNode = treeRoot;
         ((DefaultTreeModel)cellTree.getModel()).insertNodeInto(ret, parentNode, parentNode.getChildCount());
 
         
         List<Cell> children = cell.getChildren();
         for(Cell child : children)
             ret.add(createJTreeNode(child));
         
         
         return ret;
     }
 
     /**
      * Recursively create and add treeNodes for all the children of entity e.
      * The recursion stops when an entity is found which is part of another cell
      * @param e
      * @param treeNode
      */
     private void addChildEntities(Entity e, DefaultMutableTreeNode treeNode) {
         if (e==null || e.numEntities()==0)
             return;
 
         for(int i=0; i<e.numEntities(); i++) {
             Entity child = e.getEntity(i);
 
             // If the child entity belongs to another cell, then break
             if (child.getComponent(CellRefComponent.class)!=null)
                 break;
 
             DefaultMutableTreeNode childTreeNode = new DefaultMutableTreeNode(child);
             entityNodes.put(child, childTreeNode);
             treeNode.add(childTreeNode);
             addChildEntities(child, childTreeNode);
         }
     }
     
     private void populateCellPanelInfo(Cell cell) {
         if (cell==null) {
             cellClassNameTF.setText(null);
             cellNameTF.setText(null);
             DefaultListModel listModel = (DefaultListModel) cellComponentList.getModel();
             listModel.clear();
         } else {
             cellClassNameTF.setText(cell.getClass().getName());
             cellNameTF.setText(cell.getName());
             DefaultListModel listModel = (DefaultListModel) cellComponentList.getModel();
             listModel.clear();
             for(CellComponent c : cell.getComponents()) {
                 listModel.addElement(c.getClass().getName());
             }
         }
     }
     
     private void populateEntityPanelInfo(Entity entity) {
         if (entity==null) {
             entityNameTF.setText(null);
             DefaultListModel listModel = (DefaultListModel)entityComponentList.getModel();
             listModel.clear();
         } else {
             entityNameTF.setText(entity.getName());
            DefaultListModel listModel = (DefaultListModel) entityComponentList.getModel();
             listModel.clear();
             for(EntityComponent c : entity.getComponents()) {
                 listModel.addElement(c.getClass().getName());
             }
         }
     }
     
     private void setViewerActive(boolean active) {
         if (this.active == active)
             return;
         
         if (active) {
             ClientContext.getInputManager().addGlobalEventListener(cellViewerListener);            
         } else {
             ClientContext.getInputManager().removeGlobalEventListener(cellViewerListener);
         }
         
         this.active = active;
     }
 
     /**
      * Return the classname of the object, trimming off the package name
      * @param o
      * @return
      */
     private String getTrimmedClassname(Object o) {
         String str = o.getClass().getName();
 
         return str.substring(str.lastIndexOf('.')+1);
     }
 
     /**
      * JTree Renderer for Cell and Entity nodes
      */
     class WonderlandCellRenderer extends DefaultTreeCellRenderer {
 
         @Override
         public Component getTreeCellRendererComponent(JTree tree,
                                                Object value,
                                                boolean selected,
                                                boolean expanded,
                                                boolean leaf,
                                                int row,
                                                boolean hasFocus) {
             super.getTreeCellRendererComponent(
                         tree, value, selected,
                         expanded, leaf, row,
                         hasFocus);
             
             DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) value;
 
             if (treeNode.getUserObject() instanceof Cell) {
                 Cell cell = (Cell) treeNode.getUserObject();
                 String name = cell.getName();
                 if (name==null)
                     name="";
 
                 setText("C "+getTrimmedClassname(cell)+":"+name);
             } else if (treeNode.getUserObject() instanceof Entity) {
                 Entity entity = (Entity)treeNode.getUserObject();
                 String name = entity.getName();
                 if (name==null)
                     name="";
                 setText("E "+getTrimmedClassname(entity)+":"+name);
             }
             return this;
         }       
 
     }
     
     class CellViewerEventListener extends EventClassFocusListener {
         @Override
         public Class[] eventClassesToConsume () {
             return new Class[] { KeyEvent3D.class, MouseEvent3D.class };
         }
 
         @Override
         public void commitEvent (Event event) {
             Entity ent = null;
             PickInfo pickInfo = ((InputEvent3D)event).getPickInfo();
             if (pickInfo!=null)
                 ent = pickInfo.get(0).getEntity();
             
             if (ent!=null) {
                 DefaultMutableTreeNode treeNode = entityNodes.get(ent);
                 TreePath path = new TreePath(treeNode.getPath());
                 cellTree.setSelectionPath(path);
                 cellTree.scrollPathToVisible(path);
             }
         }
 
     }
     
     /**
      * Find the cell that contains entity.
      * TODO move to a general utility class
      * 
      * @param entity
      * @return
      */
      Cell findCell(Entity entity) {
         Cell ret = null;
         while(ret==null && entity!=null) {
             CellRefComponent ref = (CellRefComponent) entity.getComponent(CellRefComponent.class);
             if (ref!=null)
                 ret = ((CellRefComponent)ref).getCell();
             else
                 entity = entity.getParent();
         }
         return ret;
     }
         
     /** This method is called from within the constructor to
      * initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is
      * always regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         jPanel1 = new javax.swing.JPanel();
         jSplitPane1 = new javax.swing.JSplitPane();
         jPanel2 = new javax.swing.JPanel();
         jLabel4 = new javax.swing.JLabel();
         jPanel3 = new javax.swing.JPanel();
         jScrollPane1 = new javax.swing.JScrollPane();
         cellTree = new javax.swing.JTree();
         jTabbedPane1 = new javax.swing.JTabbedPane();
         cellInfoPanel = new javax.swing.JPanel();
         jLabel1 = new javax.swing.JLabel();
         jLabel2 = new javax.swing.JLabel();
         jLabel3 = new javax.swing.JLabel();
         jScrollPane2 = new javax.swing.JScrollPane();
         cellComponentList = new javax.swing.JList();
         cellClassNameTF = new javax.swing.JTextField();
         cellNameTF = new javax.swing.JTextField();
         jLabel5 = new javax.swing.JLabel();
         jLabel6 = new javax.swing.JLabel();
         jScrollPane4 = new javax.swing.JScrollPane();
         entityComponentList = new javax.swing.JList();
         entityNameTF = new javax.swing.JTextField();
         jmeGraphPanel = new javax.swing.JPanel();
         jPanel4 = new javax.swing.JPanel();
         jScrollPane3 = new javax.swing.JScrollPane();
         jmeTree = new javax.swing.JTree();
         jMenuBar1 = new javax.swing.JMenuBar();
         jMenu1 = new javax.swing.JMenu();
         jMenu2 = new javax.swing.JMenu();
 
         setTitle("Cell Viewer");
         addWindowListener(new java.awt.event.WindowAdapter() {
             public void windowOpened(java.awt.event.WindowEvent evt) {
                 formWindowOpened(evt);
             }
             public void windowClosed(java.awt.event.WindowEvent evt) {
                 formWindowClosed(evt);
             }
             public void windowIconified(java.awt.event.WindowEvent evt) {
                 formWindowIconified(evt);
             }
             public void windowDeiconified(java.awt.event.WindowEvent evt) {
                 formWindowDeiconified(evt);
             }
         });
 
         jPanel1.setPreferredSize(new java.awt.Dimension(600, 404));
         jPanel1.setLayout(new java.awt.BorderLayout());
 
         jSplitPane1.setDividerLocation(251);
         jSplitPane1.setLastDividerLocation(250);
         jSplitPane1.setPreferredSize(new java.awt.Dimension(251, 404));
 
         jPanel2.setLayout(new java.awt.BorderLayout());
 
         jLabel4.setText("Cell and Entity graph");
         jPanel2.add(jLabel4, java.awt.BorderLayout.NORTH);
 
         jPanel3.setLayout(new java.awt.BorderLayout());
 
         jScrollPane1.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
 
         cellTree.setAutoscrolls(true);
         cellTree.setDragEnabled(true);
         cellTree.setMaximumSize(new java.awt.Dimension(400, 57));
         cellTree.setPreferredSize(new java.awt.Dimension(250, 57));
         cellTree.setRequestFocusEnabled(false);
         cellTree.setRootVisible(false);
         cellTree.setScrollsOnExpand(true);
         cellTree.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
             public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
                 cellTreeValueChanged(evt);
             }
         });
         jScrollPane1.setViewportView(cellTree);
 
         jPanel3.add(jScrollPane1, java.awt.BorderLayout.CENTER);
 
         jPanel2.add(jPanel3, java.awt.BorderLayout.CENTER);
 
         jSplitPane1.setLeftComponent(jPanel2);
 
         jTabbedPane1.setPreferredSize(new java.awt.Dimension(400, 67));
         jTabbedPane1.addChangeListener(new javax.swing.event.ChangeListener() {
             public void stateChanged(javax.swing.event.ChangeEvent evt) {
                 jTabbedPane1StateChanged(evt);
             }
         });
 
         cellInfoPanel.setMinimumSize(new java.awt.Dimension(50, 50));
         cellInfoPanel.setPreferredSize(new java.awt.Dimension(400, 400));
 
         jLabel1.setText("Cell Class :");
 
         jLabel2.setText("Cell Name :");
 
         jLabel3.setText("Cell Components :");
 
         cellComponentList.setModel(new DefaultListModel());
         jScrollPane2.setViewportView(cellComponentList);
 
         jLabel5.setText("Entity Name :");
 
         jLabel6.setText("Entity Components :");
 
         entityComponentList.setModel(new DefaultListModel());
         jScrollPane4.setViewportView(entityComponentList);
 
         org.jdesktop.layout.GroupLayout cellInfoPanelLayout = new org.jdesktop.layout.GroupLayout(cellInfoPanel);
         cellInfoPanel.setLayout(cellInfoPanelLayout);
         cellInfoPanelLayout.setHorizontalGroup(
             cellInfoPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(cellInfoPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .add(cellInfoPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                     .add(cellInfoPanelLayout.createSequentialGroup()
                         .add(jLabel6)
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                         .add(jScrollPane4))
                     .add(cellInfoPanelLayout.createSequentialGroup()
                         .add(jLabel5)
                         .add(54, 54, 54)
                         .add(entityNameTF))
                     .add(cellInfoPanelLayout.createSequentialGroup()
                         .add(cellInfoPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                             .add(jLabel3)
                             .add(jLabel1)
                             .add(jLabel2))
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                         .add(cellInfoPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                             .add(jScrollPane2)
                             .add(cellNameTF)
                             .add(cellClassNameTF, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 286, Short.MAX_VALUE))))
                 .addContainerGap(42, Short.MAX_VALUE))
         );
         cellInfoPanelLayout.setVerticalGroup(
             cellInfoPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(cellInfoPanelLayout.createSequentialGroup()
                 .add(23, 23, 23)
                 .add(cellInfoPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                     .add(jLabel1)
                     .add(cellClassNameTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(cellInfoPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                     .add(jLabel2)
                     .add(cellNameTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(cellInfoPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                     .add(jLabel3)
                     .add(jScrollPane2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 98, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                 .add(44, 44, 44)
                 .add(cellInfoPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                     .add(jLabel5)
                     .add(entityNameTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(cellInfoPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                     .add(jLabel6)
                     .add(jScrollPane4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 98, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                 .addContainerGap(43, Short.MAX_VALUE))
         );
 
         jTabbedPane1.addTab("Cell Info", cellInfoPanel);
 
         jmeGraphPanel.setPreferredSize(new java.awt.Dimension(400, 400));
         jmeGraphPanel.setLayout(new java.awt.BorderLayout());
 
         jScrollPane3.setViewportView(jmeTree);
 
         org.jdesktop.layout.GroupLayout jPanel4Layout = new org.jdesktop.layout.GroupLayout(jPanel4);
         jPanel4.setLayout(jPanel4Layout);
         jPanel4Layout.setHorizontalGroup(
             jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(jScrollPane3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 474, Short.MAX_VALUE)
         );
         jPanel4Layout.setVerticalGroup(
             jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(jScrollPane3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 414, Short.MAX_VALUE)
         );
 
         jmeGraphPanel.add(jPanel4, java.awt.BorderLayout.CENTER);
 
         jTabbedPane1.addTab("JME Graph", jmeGraphPanel);
 
         jSplitPane1.setRightComponent(jTabbedPane1);
 
         jPanel1.add(jSplitPane1, java.awt.BorderLayout.CENTER);
 
         getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);
 
         jMenu1.setText("File");
         jMenuBar1.add(jMenu1);
 
         jMenu2.setText("Edit");
         jMenuBar1.add(jMenu2);
 
         setJMenuBar(jMenuBar1);
 
         pack();
     }// </editor-fold>//GEN-END:initComponents
 
 private void cellTreeValueChanged(javax.swing.event.TreeSelectionEvent evt) {//GEN-FIRST:event_cellTreeValueChanged
     // Tree selection
     DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                        cellTree.getLastSelectedPathComponent();
 
     if (node == null) { //Nothing is selected.	
         return;
     }
 
     Object o = node.getUserObject();
     if (o instanceof Cell) {
         populateCellPanelInfo((Cell)o);
         populateEntityPanelInfo(null);
     } else if (o instanceof Entity) {
         populateEntityPanelInfo((Entity)o);
         populateCellPanelInfo(findCell((Entity)o));
 
         showJMEGraph(((Entity)o));
     }
 
 }//GEN-LAST:event_cellTreeValueChanged
 
 private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
     setViewerActive(false);
 }//GEN-LAST:event_formWindowClosed
 
 private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
     setViewerActive(true);
 }//GEN-LAST:event_formWindowOpened
 
 private void formWindowIconified(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowIconified
     setViewerActive(false);
 }//GEN-LAST:event_formWindowIconified
 
 private void formWindowDeiconified(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowDeiconified
     setViewerActive(true);
 }//GEN-LAST:event_formWindowDeiconified
 
 private void jTabbedPane1StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jTabbedPane1StateChanged
     if (jTabbedPane1.getSelectedComponent()==jmeGraphPanel) {
         showJMEGraph(jmeGraphEntity);
     }
 }//GEN-LAST:event_jTabbedPane1StateChanged
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JTextField cellClassNameTF;
     private javax.swing.JList cellComponentList;
     private javax.swing.JPanel cellInfoPanel;
     private javax.swing.JTextField cellNameTF;
     private javax.swing.JTree cellTree;
     private javax.swing.JList entityComponentList;
     private javax.swing.JTextField entityNameTF;
     private javax.swing.JLabel jLabel1;
     private javax.swing.JLabel jLabel2;
     private javax.swing.JLabel jLabel3;
     private javax.swing.JLabel jLabel4;
     private javax.swing.JLabel jLabel5;
     private javax.swing.JLabel jLabel6;
     private javax.swing.JMenu jMenu1;
     private javax.swing.JMenu jMenu2;
     private javax.swing.JMenuBar jMenuBar1;
     private javax.swing.JPanel jPanel1;
     private javax.swing.JPanel jPanel2;
     private javax.swing.JPanel jPanel3;
     private javax.swing.JPanel jPanel4;
     private javax.swing.JScrollPane jScrollPane1;
     private javax.swing.JScrollPane jScrollPane2;
     private javax.swing.JScrollPane jScrollPane3;
     private javax.swing.JScrollPane jScrollPane4;
     private javax.swing.JSplitPane jSplitPane1;
     private javax.swing.JTabbedPane jTabbedPane1;
     private javax.swing.JPanel jmeGraphPanel;
     private javax.swing.JTree jmeTree;
     // End of variables declaration//GEN-END:variables
 
 }
