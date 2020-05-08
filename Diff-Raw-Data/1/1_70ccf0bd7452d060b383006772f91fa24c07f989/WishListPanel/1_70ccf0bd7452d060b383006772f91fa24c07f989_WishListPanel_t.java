 package interiores.presentation.swing.views.editor;
 
 import interiores.business.controllers.DesignController;
 import interiores.business.controllers.FixedElementController;
 import interiores.business.controllers.FurnitureTypeController;
 import interiores.business.events.backtracking.SolveDesignFinishedEvent;
 import interiores.business.events.backtracking.SolveDesignStartedEvent;
 import interiores.business.events.catalogs.FTCatalogCheckoutEvent;
 import interiores.business.events.furniture.ElementSelectedEvent;
 import interiores.business.events.furniture.ElementUnselectedEvent;
 import interiores.business.exceptions.WantedElementNotFoundException;
 import interiores.core.presentation.SwingController;
 import interiores.core.presentation.annotation.Listen;
 import interiores.presentation.swing.views.ConstraintEditorFrame;
 import interiores.presentation.swing.views.GlobalConstraintsFrame;
 import java.awt.event.KeyEvent;
 import java.util.Collection;
 import java.util.TimerTask;
 import javax.swing.DefaultListModel;
 import javax.swing.ImageIcon;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.DefaultTreeModel;
 
 /**
  *
  * @author alvaro
  */
 public class WishListPanel extends JPanel {
 
     private DesignController designController;
     private FurnitureTypeController furnitureTypeController;
     private FixedElementController fixedElementController;
     private DefaultListModel listModel;
     private SwingController swing;
     
     java.util.Timer timeout;
     
     /** Creates new form WishListPanel */
     public WishListPanel(SwingController presentation) {
         initComponents();
         
         swing = presentation;
         designController = presentation.getBusinessController(DesignController.class);
         furnitureTypeController = presentation.getBusinessController(FurnitureTypeController.class);
         fixedElementController = presentation.getBusinessController(FixedElementController.class);
         listModel = new DefaultListModel();
         
         ImageIcon im = new ImageIcon("src/resources/padlock.png");
         im.setImage( im.getImage().getScaledInstance(15,15,java.awt.Image.SCALE_SMOOTH) );
         globalConstraintsButton.setIcon(im);
         
         initLists();
     }
     
     private void initLists() {
         selected.setModel(listModel);
         updateSelectable();
         updateSelected();
     }
     
     private void solveDesign() {
         designController.solve(debugCheckBox.isSelected(), timeCheckBox.isSelected());
     }
     
     @Listen({FTCatalogCheckoutEvent.class})
     public void updateSelectable() {
         Collection<String> selectableFurniture = furnitureTypeController.getSelectableFurniture();
         DefaultTreeModel model = (DefaultTreeModel) selectable.getModel();
         DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
         root.removeAllChildren();
         for (String sf: selectableFurniture) {
             root.add(new DefaultMutableTreeNode(sf));
         }
         
         model.reload();
     }
     
     public void updateSelected() {
         listModel.clear();
         
         for(String element : furnitureTypeController.getRoomFurniture())
              listModel.addElement(element);
         
         for(String element : fixedElementController.getFixedNames())
             listModel.addElement(element);
     }
     
     @Listen(ElementSelectedEvent.class)
     public void addSelected(ElementSelectedEvent evt) {
         listModel.addElement(evt.getName());
     }
     
     @Listen(ElementUnselectedEvent.class)
     public void removeSelected(ElementUnselectedEvent evt) {
         listModel.removeElement(evt.getName());
     }
     
     private void launchConstraintEditor(String selectedId) {
         ConstraintEditorFrame cef = swing.getNew(ConstraintEditorFrame.class);
         cef.setSelectedId(selectedId);
         cef.setVisible(true);
     }
     
     private void launchRoomConstraintsEditor() {
         GlobalConstraintsFrame roomConst = swing.getNew(GlobalConstraintsFrame.class);
         roomConst.setVisible(true);
     }
     
     @Listen(SolveDesignStartedEvent.class)
     public void setTimeoutTimer() {
        if (timeout != null) timeout.cancel();
         timeout = new java.util.Timer();
         if( ! debugCheckBox.isSelected() ) {
             timeout.schedule(new TimerTask() {
 
                 @Override
                 public void run() {
                     String title = "Solving is taking too long";
                     String msg = "It seems it is taking too long to find out if there is a solution.\n"
                                + "Maybe you should try with a different design";
                     int choice = JOptionPane.showConfirmDialog(WishListPanel.this, msg, title,
                                  JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                     if (choice == JOptionPane.YES_OPTION) designController.stopSolver();
                 }
             }, 30000);
         }
         
         solveButton.setText("Abort");
     }
     
     @Listen(SolveDesignFinishedEvent.class)
     public void setTimeoutTimer(SolveDesignFinishedEvent evt) {
         timeout.cancel();
         solveButton.setText("Solve design");
     }
     
     
     /** This method is called from within the constructor to
      * initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is
      * always regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         scrollSelectable = new javax.swing.JScrollPane();
         selectable = new javax.swing.JTree();
         scrollSelected = new javax.swing.JScrollPane();
         selected = new javax.swing.JList();
         solveButton = new javax.swing.JButton();
         debugCheckBox = new javax.swing.JCheckBox();
         timeCheckBox = new javax.swing.JCheckBox();
         globalConstraintsButton = new javax.swing.JButton();
 
         javax.swing.tree.DefaultMutableTreeNode treeNode1 = new javax.swing.tree.DefaultMutableTreeNode("Furniture");
         selectable.setModel(new javax.swing.tree.DefaultTreeModel(treeNode1));
         selectable.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
         selectable.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 selectableMouseClicked(evt);
             }
         });
         scrollSelectable.setViewportView(selectable);
 
         selected.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 selectedMouseClicked(evt);
             }
         });
         selected.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyPressed(java.awt.event.KeyEvent evt) {
                 selectedKeyPressed(evt);
             }
         });
         scrollSelected.setViewportView(selected);
 
         solveButton.setText("Solve design");
         solveButton.setActionCommand("solve");
         solveButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 solveButtonActionPerformed(evt);
             }
         });
 
         debugCheckBox.setText("Debug");
 
         timeCheckBox.setText("Time");
 
         globalConstraintsButton.setBorderPainted(false);
         globalConstraintsButton.setContentAreaFilled(false);
         globalConstraintsButton.setLabel("Global constraints");
         globalConstraintsButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 globalConstraintsButtonActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
         this.setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(layout.createSequentialGroup()
                         .addComponent(scrollSelectable, javax.swing.GroupLayout.DEFAULT_SIZE, 277, Short.MAX_VALUE)
                         .addContainerGap())
                     .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                         .addComponent(globalConstraintsButton)
                         .addContainerGap())
                     .addGroup(layout.createSequentialGroup()
                         .addComponent(scrollSelected, javax.swing.GroupLayout.DEFAULT_SIZE, 275, Short.MAX_VALUE)
                         .addGap(14, 14, 14))
                     .addGroup(layout.createSequentialGroup()
                         .addComponent(solveButton)
                         .addGap(18, 18, 18)
                         .addComponent(debugCheckBox)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addComponent(timeCheckBox)
                         .addContainerGap(14, Short.MAX_VALUE))))
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(globalConstraintsButton)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addComponent(scrollSelectable, javax.swing.GroupLayout.DEFAULT_SIZE, 269, Short.MAX_VALUE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(scrollSelected, javax.swing.GroupLayout.PREFERRED_SIZE, 274, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(solveButton)
                     .addComponent(debugCheckBox)
                     .addComponent(timeCheckBox))
                 .addGap(18, 18, 18))
         );
     }// </editor-fold>//GEN-END:initComponents
 
 private void solveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_solveButtonActionPerformed
     if(designController.isSolving()) {
         designController.stopSolver();
         solveButton.setText("Solve design");
     }
     else
         solveDesign();
 }//GEN-LAST:event_solveButtonActionPerformed
 
 private void selectedMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_selectedMouseClicked
     if (evt.getClickCount() >= 2) { // Double click or faster
         int index = selected.getSelectedIndex();
         if (index >= 0) {
             String type = (String) selected.getModel().getElementAt(index);
             if (! isFixed(type)) {
                 launchConstraintEditor(type);
             }
         }
     }
 }//GEN-LAST:event_selectedMouseClicked
 
 private void selectableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_selectableMouseClicked
     if (evt.getClickCount() >= 2) { // Double click or faster
         DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                        selectable.getLastSelectedPathComponent();
 
 
         if (node != null) {
             String nodeInfo = (String) node.getUserObject();
             if (node.isLeaf()) {
                 furnitureTypeController.select(nodeInfo);
             }
         }
     }
 }//GEN-LAST:event_selectableMouseClicked
 
 private void selectedKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_selectedKeyPressed
  
     int index = selected.getSelectedIndex();
     if (index >= 0) {
         String type = (String) selected.getModel().getElementAt(index);
         switch (evt.getKeyCode()) {
             case KeyEvent.VK_DELETE:  // press delete
                 try {
                     furnitureTypeController.unselect(type);
                 }
                 catch (WantedElementNotFoundException nfe) {
                     // It wasn't a furniture element it is a fixed
                     fixedElementController.remove(type);
                 }
                 break;
                 
             case KeyEvent.VK_ENTER:
                 if (! isFixed(type))
                     launchConstraintEditor(type);
                 break;
         }
    }
 }//GEN-LAST:event_selectedKeyPressed
 
 private void globalConstraintsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_globalConstraintsButtonActionPerformed
     launchRoomConstraintsEditor();
 }//GEN-LAST:event_globalConstraintsButtonActionPerformed
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JCheckBox debugCheckBox;
     private javax.swing.JButton globalConstraintsButton;
     private javax.swing.JScrollPane scrollSelectable;
     private javax.swing.JScrollPane scrollSelected;
     private javax.swing.JTree selectable;
     private javax.swing.JList selected;
     private javax.swing.JButton solveButton;
     private javax.swing.JCheckBox timeCheckBox;
     // End of variables declaration//GEN-END:variables
 
     private boolean isFixed(String name) {
         return fixedElementController.getFixedNames().contains(name);
     }
 
 }
