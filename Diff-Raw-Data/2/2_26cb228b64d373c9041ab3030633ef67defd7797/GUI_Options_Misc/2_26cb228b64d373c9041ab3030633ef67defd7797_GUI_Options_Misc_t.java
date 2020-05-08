 package gui;
 
 import aniAdd.Communication.ComEvent;
 
 public class GUI_Options_Misc extends javax.swing.JPanel {
     private IGUI gui;
 
     public GUI_Options_Misc() {
         initComponents();        
     }
     public GUI_Options_Misc(IGUI gui) {
         this();
         this.gui = gui;
 
         chck_ShowFileInfoPane.setSelected((Boolean)gui.FromMem("ShowFileInfoPane", false));
         chck_ShowEditboxes.setSelected((Boolean)gui.FromMem("ShowSrcStrOtEditBoxes", false));
         chck_ShowFileInfoPane.setVisible(false);
     }
 
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         chck_ShowFileInfoPane = new javax.swing.JCheckBox();
         chck_ShowEditboxes = new javax.swing.JCheckBox();
 
         chck_ShowFileInfoPane.setLabel("Show Fileinfo Pane");
         chck_ShowFileInfoPane.setOpaque(false);
         chck_ShowFileInfoPane.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 chck_ShowFileInfoPaneActionPerformed(evt);
             }
         });
 
        chck_ShowEditboxes.setLabel("Show Storage/Source/Other Editboxes");
         chck_ShowEditboxes.setOpaque(false);
         chck_ShowEditboxes.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 chck_ShowEditboxesActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
         this.setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(chck_ShowEditboxes)
             .addComponent(chck_ShowFileInfoPane)
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addComponent(chck_ShowFileInfoPane)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(chck_ShowEditboxes))
         );
     }// </editor-fold>//GEN-END:initComponents
 
     private void chck_ShowFileInfoPaneActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chck_ShowFileInfoPaneActionPerformed
     gui.ToMem("ShowFileInfoPane", chck_ShowFileInfoPane.isSelected());
     gui.GUIEvent(new ComEvent(this, ComEvent.eType.Information, "OptionChange", "ShowFileInfoPane", chck_ShowFileInfoPane.isSelected()));
 }//GEN-LAST:event_chck_ShowFileInfoPaneActionPerformed
 
     private void chck_ShowEditboxesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chck_ShowEditboxesActionPerformed
     gui.ToMem("ShowSrcStrOtEditBoxes", chck_ShowEditboxes.isSelected());
     gui.GUIEvent(new ComEvent(this, ComEvent.eType.Information, "OptionChange", "ShowSrcStrOtEditBoxes", chck_ShowEditboxes.isSelected()));
 }//GEN-LAST:event_chck_ShowEditboxesActionPerformed
 
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     protected javax.swing.JCheckBox chck_ShowEditboxes;
     protected javax.swing.JCheckBox chck_ShowFileInfoPane;
     // End of variables declaration//GEN-END:variables
 
 }
