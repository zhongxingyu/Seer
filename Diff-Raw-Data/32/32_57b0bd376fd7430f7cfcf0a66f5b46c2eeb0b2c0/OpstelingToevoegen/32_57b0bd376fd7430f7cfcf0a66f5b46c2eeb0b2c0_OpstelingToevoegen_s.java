 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package GUI;
 
 
 import Main.Opstelling_Generator;
 import Models.OpstellingDeelnemer;
 import configuration.SimpleDataSourceV2;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.util.ArrayList;
 import java.util.List;
 import javax.swing.JOptionPane;
 import javax.swing.table.DefaultTableModel;
 
 /**
  *
  * @author arjandoets
  */
 public class OpstelingToevoegen extends javax.swing.JFrame {
 
     /**
      * Creates new form OpstelingMaken
      */
     
     
     
     private int T_Code;
     private int R_Code;
     List<OpstellingDeelnemer> deelnemers = new ArrayList();
     DefaultTableModel model = new DefaultTableModel();
     
     public OpstelingToevoegen() {
         initComponents();
     }
 
     public OpstelingToevoegen(int T_Code, int R_Code) {
         initComponents();
         this.T_Code = T_Code;
         this.R_Code = R_Code;
         inittable();
         CreateOpstelling();
     }
     
     private void inittable(){
         //setup the jtable
         String[] kolommen = {"Voornaam", "Achternaam", "Tafel_Nummer"};
         model = new DefaultTableModel(kolommen, 0);
         Table_Opstelling.setModel(model);
 
     }
     
     private void CreateOpstelling(){
         
        if (R_Code == 0){
             int aantal = 0;
             String sql = "select count(D_Code) from heeft_betaald where inleggeld_betaald = 'j' and T_Code = ?";
            
             try{
                 Connection conn;
                 conn = SimpleDataSourceV2.getConnection();
                 PreparedStatement stat = conn.prepareStatement(sql); 
                 
                 stat.setInt(1, T_Code);
                 
                 ResultSet res = stat.executeQuery();
                 
                 while(res.next()){
                     aantal = res.getInt("count(D_Code)");
                 }
                 
             } catch (Exception ex) {
             System.out.println(ex);
             JOptionPane.showMessageDialog(this, "Database Error" + ex.getMessage());
             }
 
             int[] tavels = Opstelling_Generator.calculatetavels(aantal);
             System.out.println(aantal);
             int step = 0;
             int tavels_nr = 1;
             
             sql = "select voornaam, achternaam, D_Code from deelnemer where d_code in (select d_code from heeft_betaald where inleggeld_betaald = 'j' and t_code = ?)";
             
             try{
                 Connection conn;
                 conn = SimpleDataSourceV2.getConnection();
                 PreparedStatement stat = conn.prepareStatement(sql); 
 
                 stat.setInt(1, T_Code);
                 
                 ResultSet res = stat.executeQuery();
                 
                 while(res.next()){
                     String voornaam = res.getString("voornaam");
                     String achternaam = res.getString("achternaam");
                     int d_code = res.getInt("D_Code");
                     
                     int max_table = 8;
                    
                     if (tavels[7] != 0){
                         max_table = 8;
                     } else if (tavels[6] != 0) {
                         max_table = 7;
                     } else if (tavels[5] != 0) {
                         max_table = 6;
                     } else if (tavels[4] != 0) {
                         max_table = 5;
                     } else if (tavels[3] != 0) {
                         max_table = 4;
                     } else if (tavels[2] != 0) {
                         max_table = 3;
                     } else if (tavels[1] != 0) {
                         max_table = 2;
                     } else if (tavels[0] != 0) {
                         max_table = 1;
                     }
                     
                     if(step == 0){
                         step = max_table;
                     }
                     
                     step --;
                     
                    OpstellingDeelnemer op = new OpstellingDeelnemer(R_Code, tavels_nr, d_code, voornaam, achternaam, 0);
                     deelnemers.add(op);
                     
                     if (step == 0){
                         tavels[max_table] --;
                         tavels_nr ++;
                     }
                 }
                 
             } catch (Exception ex) {
             System.out.println(ex);
             JOptionPane.showMessageDialog(this, "Database Error" + ex.getMessage());
             }
             
             
             for (int a = 0; a < deelnemers.size(); a ++){
                 OpstellingDeelnemer deelnemer = deelnemers.get(a);
                 model.addRow(deelnemer.torow());
             }
             
             
             
        }
         
         
     }
     
     
     /**
      * This method is called from within the constructor to initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is always
      * regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         jScrollPane1 = new javax.swing.JScrollPane();
         Table_Opstelling = new javax.swing.JTable();
         Button_Save = new javax.swing.JButton();
         Button_Back = new javax.swing.JButton();
 
         setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
 
         Table_Opstelling.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
                 {null, null, null, null},
                 {null, null, null, null},
                 {null, null, null, null},
                 {null, null, null, null}
             },
             new String [] {
                 "Title 1", "Title 2", "Title 3", "Title 4"
             }
         ));
         jScrollPane1.setViewportView(Table_Opstelling);
 
         Button_Save.setText("Save");
         Button_Save.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 Button_SaveActionPerformed(evt);
             }
         });
 
         Button_Back.setText("Back");
         Button_Back.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 Button_BackActionPerformed(evt);
             }
         });
 
         org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(layout.createSequentialGroup()
                 .addContainerGap()
                 .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                     .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 388, Short.MAX_VALUE)
                     .add(layout.createSequentialGroup()
                         .add(Button_Back, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 84, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                         .add(Button_Save, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 102, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                 .addContainerGap())
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(layout.createSequentialGroup()
                 .addContainerGap()
                 .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 253, Short.MAX_VALUE)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                     .add(Button_Save)
                     .add(Button_Back))
                 .addContainerGap())
         );
 
         pack();
     }// </editor-fold>//GEN-END:initComponents
 
     private void Button_BackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Button_BackActionPerformed
         new ToernooiStart(T_Code).setVisible(true);
         this.dispose();
     }//GEN-LAST:event_Button_BackActionPerformed
 
     private void Button_SaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Button_SaveActionPerformed
             int tafel = 0;
             
             for (int a = 0; a < deelnemers.size(); a ++){        
                 OpstellingDeelnemer deelnemer = deelnemers.get(a);
                 
                 if (deelnemer.getTF_Code() != tafel){
                     String sql = "insert into opstelling (Ronde, Tafel, Toernooi) values (?,?,?)";
                     try{
                         Connection conn;
                         conn = SimpleDataSourceV2.getConnection();
                         PreparedStatement stat = conn.prepareStatement(sql); 
                         stat.setInt(1, deelnemer.getR_Code());
                         stat.setInt(2, deelnemer.getTF_Code());
                         stat.setInt(3, T_Code);
                         stat.execute();
 
                     } catch (Exception ex) {
                         System.out.println(ex);
                         JOptionPane.showMessageDialog(this, "Database Error" + ex.getMessage());
                     }   
                     tafel ++;
                 }
                 
                 
                 String sql = "insert into plaats (Ronde, Tafel, Toernooi, D_Code) values (?,?,?,?)";
                 try{
                     Connection conn;
                     conn = SimpleDataSourceV2.getConnection();
                     PreparedStatement stat = conn.prepareStatement(sql); 
                     stat.setInt(1, deelnemer.getR_Code());
                     stat.setInt(2, deelnemer.getTF_Code());
                     stat.setInt(3, T_Code);
                     stat.setInt(4, deelnemer.getD_Code());
                     stat.execute();
                     
                 } catch (Exception ex) {
                     System.out.println(ex);
                     JOptionPane.showMessageDialog(this, "Database Error" + ex.getMessage());
                 }  
             
             }
             new ToernooiStart(T_Code).setVisible(true);
             this.dispose();
     
     }//GEN-LAST:event_Button_SaveActionPerformed
 
     /**
      * @param args the command line arguments
      */
     public static void main(String args[]) {
         /*
          * Set the Nimbus look and feel
          */
         //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
         /*
          * If Nimbus (introduced in Java SE 6) is not available, stay with the
          * default look and feel. For details see
          * http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
          */
         try {
             for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                 if ("Nimbus".equals(info.getName())) {
                     javax.swing.UIManager.setLookAndFeel(info.getClassName());
                     break;
                 }
             }
         } catch (ClassNotFoundException ex) {
             java.util.logging.Logger.getLogger(OpstelingToevoegen.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (InstantiationException ex) {
             java.util.logging.Logger.getLogger(OpstelingToevoegen.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (IllegalAccessException ex) {
             java.util.logging.Logger.getLogger(OpstelingToevoegen.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (javax.swing.UnsupportedLookAndFeelException ex) {
             java.util.logging.Logger.getLogger(OpstelingToevoegen.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         }
         //</editor-fold>
 
         /*
          * Create and display the form
          */
         java.awt.EventQueue.invokeLater(new Runnable() {
 
             public void run() {
                 new OpstelingToevoegen().setVisible(true);
             }
         });
     }
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JButton Button_Back;
     private javax.swing.JButton Button_Save;
     private javax.swing.JTable Table_Opstelling;
     private javax.swing.JScrollPane jScrollPane1;
     // End of variables declaration//GEN-END:variables
 }
