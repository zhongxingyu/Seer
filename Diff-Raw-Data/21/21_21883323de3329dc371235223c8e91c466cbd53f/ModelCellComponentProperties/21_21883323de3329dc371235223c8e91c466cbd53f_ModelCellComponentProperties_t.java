 /**
  * Project Wonderland
  *
  * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
  *
  * Redistributions in source code form must reproduce the above
  * copyright and this condition.
  *
  * The contents of this file are subject to the GNU General Public
  * License, Version 2 (the "License"); you may not use this file
  * except in compliance with the License. A copy of the License is
  * available at http://www.opensource.org/licenses/gpl-license.php.
  *
  * Sun designates this particular file as subject to the "Classpath"
  * exception as provided by Sun in the License file that accompanied
  * this code.
  */
 package org.jdesktop.wonderland.modules.artimport.client.jme;
 
 import java.util.ResourceBundle;
 import javax.swing.JPanel;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 import org.jdesktop.wonderland.client.cell.properties.CellPropertiesEditor;
 import org.jdesktop.wonderland.client.cell.properties.annotation.PropertiesFactory;
 import org.jdesktop.wonderland.client.cell.properties.spi.PropertiesFactorySPI;
 import org.jdesktop.wonderland.common.cell.state.CellComponentServerState;
 import org.jdesktop.wonderland.common.cell.state.CellServerState;
 import org.jdesktop.wonderland.common.cell.state.ModelCellComponentServerState;
 
 /**
  *
  * @author paulby
  * @author Ronny Standtke <ronny.standtke@fhnw.ch>
  */
 @PropertiesFactory(ModelCellComponentServerState.class)
 public class ModelCellComponentProperties
         extends JPanel implements PropertiesFactorySPI {
 
     private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(
             "org/jdesktop/wonderland/modules/artimport/client/jme/resources/Bundle");
     private CellPropertiesEditor editor = null;
     private ModelCellComponentServerState origState = null;
 
     /** Creates new form SampleComponentProperties */
     public ModelCellComponentProperties() {
         // Initialize the GUI
         initComponents();
 
         // Listen for changes to the info text field
         deployedModelURLTF.getDocument().addDocumentListener(
                 new InfoTextFieldListener());
     }
 
     /**
      * @inheritDoc()
      */
     public String getDisplayName() {
        return BUNDLE.getString("Model_Component");
     }
 
     /**
      * @inheritDoc()
      */
     public JPanel getPropertiesJPanel() {
         return this;
     }
 
     /**
      * @inheritDoc()
      */
     public void setCellPropertiesEditor(CellPropertiesEditor editor) {
         this.editor = editor;
     }
 
     /**
      * @inheritDoc()
      */
     public void open() {
         CellServerState state = editor.getCellServerState();
         CellComponentServerState compState = state.getComponentServerState(
                 ModelCellComponentServerState.class);
         if (state != null) {
             ModelCellComponentServerState mState =
                     (ModelCellComponentServerState) compState;
             origState = (ModelCellComponentServerState) mState.clone(null);
             deployedModelURLTF.setText(mState.getDeployedModelURL());
             collisionEnabledCB.setSelected(mState.isCollisionEnabled());
             pickingEnabledCB.setSelected(mState.isPickingEnabled());
             lightingEnabledCB.setSelected(mState.isLightingEnabled());
 
             checkDirty();
         }
     }
 
     /**
      * @inheritDoc()
      */
     public void close() {
         // Do nothing for now.
     }
 
     /**
      * @inheritDoc()
      */
     public void apply() {
         // Fetch the latest from the info text field and set it.
         CellServerState state = editor.getCellServerState();
        ModelCellComponentServerState compState =
                (ModelCellComponentServerState) state.getComponentServerState(
                 ModelCellComponentServerState.class);
         compState.setCollisionEnabled(collisionEnabledCB.isSelected());
         compState.setPickingEnable(pickingEnabledCB.isSelected());
         compState.setLightingEnabled(lightingEnabledCB.isSelected());
         editor.addToUpdateList(compState);
     }
 
     /**
      * @inheritDoc()
      */
     public void restore() {
         // Restore from the original state stored.
         deployedModelURLTF.setText(origState.getDeployedModelURL());
         collisionEnabledCB.setSelected(origState.isCollisionEnabled());
         pickingEnabledCB.setSelected(origState.isPickingEnabled());
         lightingEnabledCB.setSelected(origState.isLightingEnabled());
 
         checkDirty();
     }
 
     /**
      * Inner class to listen for changes to the text field and fire off dirty
      * or clean indications to the cell properties editor.
      */
     class InfoTextFieldListener implements DocumentListener {
 
         public void insertUpdate(DocumentEvent e) {
             checkDirty();
         }
 
         public void removeUpdate(DocumentEvent e) {
             checkDirty();
         }
 
         public void changedUpdate(DocumentEvent e) {
             checkDirty();
         }
     }
 
     private void checkDirty() {
         if (origState == null) {
             editor.setPanelDirty(ModelCellComponentProperties.class, true);
             return;
         }
 
         boolean dirty;
 
         dirty = !deployedModelURLTF.getText().equals(origState.getDeployedModelURL());
         dirty |= (collisionEnabledCB.isSelected() != origState.isCollisionEnabled());
         dirty |= (pickingEnabledCB.isSelected() != origState.isPickingEnabled());
         dirty |= (lightingEnabledCB.isSelected() != origState.isLightingEnabled());
 
         editor.setPanelDirty(ModelCellComponentProperties.class, dirty);
     }
 
     /** This method is called from within the constructor to
      * initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is
      * always regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         jLabel1 = new javax.swing.JLabel();
         deployedModelURLTF = new javax.swing.JTextField();
         collisionEnabledCB = new javax.swing.JCheckBox();
         pickingEnabledCB = new javax.swing.JCheckBox();
         lightingEnabledCB = new javax.swing.JCheckBox();
 
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/jdesktop/wonderland/modules/artimport/client/jme/resources/Bundle"); // NOI18N
        jLabel1.setText(bundle.getString("ModelCellComponentProperties.jLabel1.text")); // NOI18N
 
         deployedModelURLTF.setEditable(false);
 
        collisionEnabledCB.setText(bundle.getString("ModelCellComponentProperties.collisionEnabledCB.text")); // NOI18N
         collisionEnabledCB.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 collisionEnabledCBActionPerformed(evt);
             }
         });
 
        pickingEnabledCB.setText(bundle.getString("ModelCellComponentProperties.pickingEnabledCB.text")); // NOI18N
         pickingEnabledCB.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 pickingEnabledCBActionPerformed(evt);
             }
         });
 
        lightingEnabledCB.setText(bundle.getString("ModelCellComponentProperties.lightingEnabledCB.text")); // NOI18N
         lightingEnabledCB.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 lightingEnabledCBActionPerformed(evt);
             }
         });
 
         org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
         this.setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(layout.createSequentialGroup()
                 .addContainerGap()
                 .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                     .add(layout.createSequentialGroup()
                         .add(jLabel1)
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                         .add(deployedModelURLTF, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 221, Short.MAX_VALUE))
                     .add(collisionEnabledCB)
                     .add(pickingEnabledCB)
                     .add(lightingEnabledCB))
                 .addContainerGap())
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(layout.createSequentialGroup()
                 .addContainerGap()
                 .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                     .add(jLabel1)
                     .add(deployedModelURLTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                 .add(collisionEnabledCB)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(pickingEnabledCB)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(lightingEnabledCB)
                 .addContainerGap(159, Short.MAX_VALUE))
         );
     }// </editor-fold>//GEN-END:initComponents
 
     private void collisionEnabledCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_collisionEnabledCBActionPerformed
         checkDirty();
     }//GEN-LAST:event_collisionEnabledCBActionPerformed
 
     private void pickingEnabledCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pickingEnabledCBActionPerformed
         checkDirty();
     }//GEN-LAST:event_pickingEnabledCBActionPerformed
 
     private void lightingEnabledCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_lightingEnabledCBActionPerformed
         checkDirty();
     }//GEN-LAST:event_lightingEnabledCBActionPerformed
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JCheckBox collisionEnabledCB;
     private javax.swing.JTextField deployedModelURLTF;
     private javax.swing.JLabel jLabel1;
     private javax.swing.JCheckBox lightingEnabledCB;
     private javax.swing.JCheckBox pickingEnabledCB;
     // End of variables declaration//GEN-END:variables
 }
