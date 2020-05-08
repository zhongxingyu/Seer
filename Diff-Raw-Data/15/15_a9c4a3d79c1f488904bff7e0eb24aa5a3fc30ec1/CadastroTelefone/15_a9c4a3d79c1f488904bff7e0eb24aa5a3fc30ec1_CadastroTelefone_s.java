 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 /*
  * CadastroTelefone1.java
  *
  * Created on 18/10/2011, 20:16:05
  */
 package controleatleta;
 
 public class CadastroTelefone extends javax.swing.JDialog {
     private String telefone;
 
     public String getTelefone() {
         return this.telefone;
     }
     
     public CadastroTelefone(java.awt.Frame parent, boolean modal) {
         super(parent, modal);
         initComponents();
     }
 
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         jLabelTelefone = new javax.swing.JLabel();
         jTextFieldTelefone = new javax.swing.JTextField();
         jButtonCancelar = new javax.swing.JButton();
         jButtonAdicionar = new javax.swing.JButton();
 
         setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
 
         jLabelTelefone.setText("Telefone:");
 
         jButtonCancelar.setText("Cancelar");
         jButtonCancelar.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButtonCancelarActionPerformed(evt);
             }
         });
 
         jButtonAdicionar.setText("Adicionar");
         jButtonAdicionar.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButtonAdicionarActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
             .addGroup(layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(layout.createSequentialGroup()
                         .addComponent(jLabelTelefone)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(jTextFieldTelefone, javax.swing.GroupLayout.DEFAULT_SIZE, 330, Short.MAX_VALUE))
                     .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                         .addComponent(jButtonAdicionar)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addComponent(jButtonCancelar)))
                 .addContainerGap())
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 68, Short.MAX_VALUE)
             .addGroup(layout.createSequentialGroup()
                 .addGap(8, 8, 8)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabelTelefone)
                     .addComponent(jTextFieldTelefone, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jButtonCancelar)
                     .addComponent(jButtonAdicionar))
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
         pack();
     }// </editor-fold>//GEN-END:initComponents
 
 private void jButtonAdicionarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAdicionarActionPerformed
     this.telefone = jTextFieldTelefone.getText();
     this.setVisible(false);
 }//GEN-LAST:event_jButtonAdicionarActionPerformed
 
 private void jButtonCancelarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCancelarActionPerformed
     this.setVisible(false);
 }//GEN-LAST:event_jButtonCancelarActionPerformed
 
     /**
      * @param args the command line arguments
      */
     public static void main(String args[]) {
         /* Set the Nimbus look and feel */
         //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
         /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
          * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
          */
         try {
             for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                 if ("Nimbus".equals(info.getName())) {
                     javax.swing.UIManager.setLookAndFeel(info.getClassName());
                     break;
                 }
             }
         } catch (ClassNotFoundException ex) {
             java.util.logging.Logger.getLogger(CadastroTelefone.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (InstantiationException ex) {
             java.util.logging.Logger.getLogger(CadastroTelefone.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (IllegalAccessException ex) {
             java.util.logging.Logger.getLogger(CadastroTelefone.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (javax.swing.UnsupportedLookAndFeelException ex) {
             java.util.logging.Logger.getLogger(CadastroTelefone.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         }
         //</editor-fold>
 
         /* Create and display the dialog */
         java.awt.EventQueue.invokeLater(new Runnable() {
 
             public void run() {
                 CadastroTelefone dialog = new CadastroTelefone(new javax.swing.JFrame(), true);
                 dialog.addWindowListener(new java.awt.event.WindowAdapter() {
 
                     @Override
                     public void windowClosing(java.awt.event.WindowEvent e) {
                         System.exit(0);
                     }
                 });
                 dialog.setVisible(true);
             }
         });
     }
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JButton jButtonAdicionar;
     private javax.swing.JButton jButtonCancelar;
     private javax.swing.JLabel jLabelTelefone;
     private javax.swing.JTextField jTextFieldTelefone;
     // End of variables declaration//GEN-END:variables
 }
