 
 package Interfaces;
 
 import java.sql.Connection;
 import java.sql.Date;
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 import java.util.Calendar;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.JOptionPane;
 
 public class GenerarEspera extends javax.swing.JFrame {
 
     javax.swing.JFrame Menu, Busqueda;
     Connection conexion;
     private int idPa;
     
     /** Creates new form GenerarEspera */
     public GenerarEspera(javax.swing.JFrame menu, javax.swing.JFrame busqueda, Connection con, int idPac) {
         initComponents();
         this.conexion = con;
         this.idPa = idPac;
         Menu=menu;
         Busqueda=busqueda;
         jlAsterisco.setVisible(false);
         jlIngrese.setVisible(false);
         GrupodeBotones.add(jrbAlta);
         GrupodeBotones.add(jrbMedia);
         GrupodeBotones.add(jrbBaja);
         jrbAlta.setSelected(true);
     }
 
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         GrupodeBotones = new javax.swing.ButtonGroup();
         jPanel1 = new javax.swing.JPanel();
         jLabel1 = new javax.swing.JLabel();
         jScrollPane1 = new javax.swing.JScrollPane();
         jTextArea1 = new javax.swing.JTextArea();
         jLabel2 = new javax.swing.JLabel();
         jrbAlta = new javax.swing.JRadioButton();
         jrbMedia = new javax.swing.JRadioButton();
         jrbBaja = new javax.swing.JRadioButton();
         jbGenerar = new javax.swing.JButton();
         jbAnterior1 = new javax.swing.JButton();
         jlAsterisco = new javax.swing.JLabel();
         jlIngrese = new javax.swing.JLabel();
 
         setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
 
         jPanel1.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 1, true));
 
         jLabel1.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
         jLabel1.setText("Síntomas:");
 
         jTextArea1.setColumns(20);
         jTextArea1.setRows(5);
         jScrollPane1.setViewportView(jTextArea1);
 
         jLabel2.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
         jLabel2.setText("Nivel de Prioridad:");
 
         GrupodeBotones.add(jrbAlta);
         jrbAlta.setText("Alta");
 
         GrupodeBotones.add(jrbMedia);
         jrbMedia.setSelected(true);
         jrbMedia.setText("Media");
 
         GrupodeBotones.add(jrbBaja);
         jrbBaja.setText("Baja");
 
         jbGenerar.setText("Generar");
         jbGenerar.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jbGenerarActionPerformed(evt);
             }
         });
 
         jbAnterior1.setText("Atras");
         jbAnterior1.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jbAnterior1ActionPerformed(evt);
             }
         });
 
         jlAsterisco.setForeground(new java.awt.Color(204, 0, 0));
         jlAsterisco.setText("*");
 
         jlIngrese.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
         jlIngrese.setForeground(new java.awt.Color(204, 0, 0));
         jlIngrese.setText("INGRESE AL MENOS UN SINTOMA");
 
         javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
         jPanel1.setLayout(jPanel1Layout);
         jPanel1Layout.setHorizontalGroup(
             jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel1Layout.createSequentialGroup()
                 .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(jPanel1Layout.createSequentialGroup()
                         .addGap(80, 80, 80)
                         .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addGroup(jPanel1Layout.createSequentialGroup()
                                 .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                 .addGap(352, 352, 352))
                             .addGroup(jPanel1Layout.createSequentialGroup()
                                 .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                 .addGap(39, 39, 39)
                                 .addComponent(jlAsterisco, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                 .addComponent(jlIngrese)
                                 .addGap(273, 273, 273))
                             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                 .addComponent(jbAnterior1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                 .addGap(18, 18, 18)
                                 .addComponent(jbGenerar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                             .addComponent(jScrollPane1)))
                     .addGroup(jPanel1Layout.createSequentialGroup()
                         .addGap(203, 203, 203)
                         .addComponent(jrbAlta, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                         .addGap(18, 18, 18)
                         .addComponent(jrbMedia, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                         .addGap(18, 18, 18)
                         .addComponent(jrbBaja, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                         .addGap(148, 148, 148)))
                 .addGap(66, 66, 66))
         );
         jPanel1Layout.setVerticalGroup(
             jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel1Layout.createSequentialGroup()
                 .addGap(30, 30, 30)
                 .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(jlAsterisco)
                     .addComponent(jlIngrese))
                 .addGap(18, 18, 18)
                 .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 243, Short.MAX_VALUE)
                 .addGap(18, 18, 18)
                 .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addGap(18, 18, 18)
                 .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jrbAlta, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(jrbMedia, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(jrbBaja, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                 .addGap(42, 42, 42)
                 .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jbGenerar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(jbAnterior1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                 .addGap(51, 51, 51))
         );
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
         );
 
         pack();
     }// </editor-fold>//GEN-END:initComponents
 
     private void jbGenerarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbGenerarActionPerformed
         jlAsterisco.setVisible(false);
         jlIngrese.setVisible(false);
         if(jTextArea1.getText().equals("")){
             jlAsterisco.setVisible(true);
             jlIngrese.setVisible(true);
         }else{
             String prioridad;
             if(jrbAlta.isSelected()){
                 prioridad = "ALTA";
             }else{
                 if(jrbBaja.isSelected()){
                     prioridad = "BAJA";
                 }else{
                     prioridad = "MEDIA";
                 }
             }
             int i;
             i=JOptionPane.showConfirmDialog(rootPane, "¿Esta seguro que desea agregar a lista de espera?", "Confirmación", WIDTH);
             if(i==0){
                 try {
                     Calendar cal = Calendar.getInstance();
                    String fecha = Integer.toString(cal.get(Calendar.YEAR))+"-"+Integer.toString(cal.get(Calendar.MONTH+1))+"-"+Integer.toString(cal.get(Calendar.DATE));
                     PreparedStatement ps = conexion.prepareStatement("insert into esperas (id_paciente, fecha, sintomas, nivel_imp, estado) values (?,?,?,?,?)");
                     ps.setInt(1, idPa);
                     ps.setDate(2, Date.valueOf(fecha));
                     ps.setString(3, jTextArea1.getText().toUpperCase());
                     ps.setString(4, prioridad);
                     ps.setString(5, "No Atendido");
                     if(ps.execute()){
 
                     }
                 } catch (SQLException ex) {
                     Logger.getLogger(GenerarEspera.class.getName()).log(Level.SEVERE, null, ex);
                 }
                 new Busqueda(Menu, conexion).setVisible(true);
                 this.setVisible(false);
             }else{
                 if(i==1){
                     Menu.setVisible(true);
                     this.setVisible(false);
                 }
             }
         }
     }
         // TODO add your handling code here:}//GEN-LAST:event_jbGenerarActionPerformed
 
     
     private void jbAnterior1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbAnterior1ActionPerformed
         this.setVisible(false);
         Busqueda.setVisible(true);
     }//GEN-LAST:event_jbAnterior1ActionPerformed
 
     /**
      * @param args the command line arguments
      */
     /*public static void main(String args[]) {
         /* Set the Nimbus look and feel */
         //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
         /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
          * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
          */
        /* try {
             for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                 if ("Nimbus".equals(info.getName())) {
                     javax.swing.UIManager.setLookAndFeel(info.getClassName());
                     break;
                 }
             }
         } catch (ClassNotFoundException ex) {
             java.util.logging.Logger.getLogger(GenerarEspera.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (InstantiationException ex) {
             java.util.logging.Logger.getLogger(GenerarEspera.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (IllegalAccessException ex) {
             java.util.logging.Logger.getLogger(GenerarEspera.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (javax.swing.UnsupportedLookAndFeelException ex) {
             java.util.logging.Logger.getLogger(GenerarEspera.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         }
         //</editor-fold>
 
         /* Create and display the form */
        /* java.awt.EventQueue.invokeLater(new Runnable() {
 
             public void run() {
                 new GenerarEspera().setVisible(true);
             }
         });
     }*/
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.ButtonGroup GrupodeBotones;
     private javax.swing.JLabel jLabel1;
     private javax.swing.JLabel jLabel2;
     private javax.swing.JPanel jPanel1;
     private javax.swing.JScrollPane jScrollPane1;
     private javax.swing.JTextArea jTextArea1;
     private javax.swing.JButton jbAnterior1;
     private javax.swing.JButton jbGenerar;
     private javax.swing.JLabel jlAsterisco;
     private javax.swing.JLabel jlIngrese;
     private javax.swing.JRadioButton jrbAlta;
     private javax.swing.JRadioButton jrbBaja;
     private javax.swing.JRadioButton jrbMedia;
     // End of variables declaration//GEN-END:variables
 }
