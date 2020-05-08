 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package nrkprosjekt;
 
 import Entities.Album;
 import Entities.Artist;
 import Handlers.Songhandler;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.sql.SQLException;
 import java.util.HashMap;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.JLabel;
 
 /**
  *
  * @author mNygaard
  */
 public class SongInformation extends javax.swing.JDialog {
 
       private Object[] option = {"No", "Yes"};
       HashMap<String, JLabel> tags;
       Songhandler songhandler;
     /**
      * Creates new form SongInformation
      */
     public SongInformation(java.awt.Frame parent, boolean modal) throws SQLException, FileNotFoundException, IOException {
         
         initComponents();
         setLocationRelativeTo(null);
         tags = new HashMap<>();
         initTags();
        
         
         
         
               
     }
     
     
     public void getstaticInfo() throws SQLException{
     
         Songhandler songhandler = new Songhandler();
         String allartist = "";
         int index = 0;
         String allalbum = "";
        
         for(Artist a : songhandler.getTrack().getArtists()){
         if(index != songhandler.getTrack().getArtists().size() -1){
             
             allartist += a.getIART() + ",";
         }
         else{
             allartist += a.getIART();
         }
         }
         
         for (Album a : songhandler.getTrack().getAlbums()){
             if(index != songhandler.getTrack().getAlbums().size() -1){
             allalbum += a.getIALB() + ",";
         }
             else{
                 allalbum += a.getIALB();
             }
         }
         
         IART.setText(allartist);
         INAM.setText(songhandler.getTrack().getValues().get("INAM"));
         IART.setText(allartist);
         IGNR.setText(songhandler.getTrack().getValues().get("IGNR"));
         IKEY.setText(songhandler.getTrack().getValues().get("IKEY"));
         ISRF.setText(songhandler.getTrack().getValues().get("ISRF"));
         IMED.setText(songhandler.getTrack().getValues().get("IMED"));
         ICOM.setText(songhandler.getTrack().getValues().get("ICOM"));
         ILYR.setText(songhandler.getTrack().getValues().get("ILYR"));
         ISRC.setText(songhandler.getTrack().getValues().get("ISRC"));
         IPEO.setText(songhandler.getTrack().getValues().get("IPEO"));
         IDIS.setText(songhandler.getTrack().getValues().get("IDIS"));
         ICRD.setText(songhandler.getTrack().getValues().get("ICRD"));
         IALB.setText(allalbum);
         ILAN.setText(songhandler.getTrack().getValues().get("ILAN"));
         IREG.setText(songhandler.getTrack().getValues().get("IREG"));
         IVIL.setText(songhandler.getTrack().getValues().get("IVIL"));
         ICRD.setText(songhandler.getTrack().getValues().get("ICRD"));
         ICON.setText(songhandler.getTrack().getValues().get("ICON"));
         ILYR.setText(songhandler.getTrack().getValues().get("ILYR"));
         IDIG.setText(songhandler.getTrack().getValues().get("IDIG"));
 }
     
     public HashMap<String, JLabel> getTags() {
         return tags;
     }
 
     public void fill(JLabel l, String text) {
         tags.put(text, l);
     }
 
     private void initTags() {
         fill(INAM, "INAM");
         fill(IART, "IART");
         fill(IGNR, "IGNR");
         fill(IKEY, "IKEY");
         fill(ISRF, "ISRF");
         fill(IMED, "IMED");
         fill(ICOM, "ICOM");
         fill(ILYR, "ILYR");
         fill(ISRC, "ISRC");
         fill(IPEO, "IPEO");
         fill(IDIS, "IDIS");
         fill(ICRD, "ICRD");
         fill(IALB, "IALB");
         fill(ILAN, "ILAN");
         fill(IREG, "IREG");
         fill(IVIL, "IVIL");
         fill(ICRD, "ICRD");
         fill(ICON, "ICON");
         fill(IDIG, "IDIG");
     }
 
   
     
    
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         jPanel1 = new javax.swing.JPanel();
         showing1 = new javax.swing.JLabel();
         jLabel21 = new javax.swing.JLabel();
         jSeparator1 = new javax.swing.JSeparator();
         jPanel2 = new javax.swing.JPanel();
         jLabel15 = new javax.swing.JLabel();
         jLabel14 = new javax.swing.JLabel();
         jLabel13 = new javax.swing.JLabel();
         jLabel12 = new javax.swing.JLabel();
         jLabel8 = new javax.swing.JLabel();
         jLabel9 = new javax.swing.JLabel();
         jLabel10 = new javax.swing.JLabel();
         IPEO = new javax.swing.JLabel();
         ICON = new javax.swing.JLabel();
         IDIS = new javax.swing.JLabel();
         IPLA = new javax.swing.JLabel();
         IVIL = new javax.swing.JLabel();
         IREG = new javax.swing.JLabel();
         ILAN = new javax.swing.JLabel();
         jPanel3 = new javax.swing.JPanel();
         jLabel6 = new javax.swing.JLabel();
         jLabel2 = new javax.swing.JLabel();
         jLabel1 = new javax.swing.JLabel();
         jLabel4 = new javax.swing.JLabel();
         jLabel3 = new javax.swing.JLabel();
         jLabel7 = new javax.swing.JLabel();
         INAM = new javax.swing.JLabel();
         IART = new javax.swing.JLabel();
         ICOM = new javax.swing.JLabel();
         IALB = new javax.swing.JLabel();
         ILYR = new javax.swing.JLabel();
         ISRC = new javax.swing.JLabel();
         jButton2 = new javax.swing.JButton();
         jPanel4 = new javax.swing.JPanel();
         jLabel17 = new javax.swing.JLabel();
         jLabel20 = new javax.swing.JLabel();
         jLabel18 = new javax.swing.JLabel();
         jLabel19 = new javax.swing.JLabel();
         jLabel16 = new javax.swing.JLabel();
         jLabel11 = new javax.swing.JLabel();
         ISRF = new javax.swing.JLabel();
         ICRD = new javax.swing.JLabel();
         IDIG = new javax.swing.JLabel();
         IMED = new javax.swing.JLabel();
         IKEY = new javax.swing.JLabel();
         IGNR = new javax.swing.JLabel();
 
         setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
         getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
 
         jPanel1.setBackground(new java.awt.Color(255, 255, 255));
         jPanel1.setMinimumSize(new java.awt.Dimension(100, 100));
         jPanel1.setLayout(null);
 
         showing1.setFont(new java.awt.Font("Calibri Light", 0, 24)); // NOI18N
         showing1.setForeground(new java.awt.Color(102, 102, 102));
         showing1.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
         showing1.setText("Track Information");
         jPanel1.add(showing1);
         showing1.setBounds(70, 20, 260, 20);
 
         jLabel21.setMaximumSize(new java.awt.Dimension(40, 40));
         jLabel21.setMinimumSize(new java.awt.Dimension(40, 40));
         jPanel1.add(jLabel21);
         jLabel21.setBounds(10, 0, 50, 50);
 
         getContentPane().add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 473, 50));
         getContentPane().add(jSeparator1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 56, 473, 10));
 
         jPanel2.setBorder(javax.swing.BorderFactory.createEtchedBorder());
         jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
 
         jLabel15.setText("People group:");
         jPanel2.add(jLabel15, new org.netbeans.lib.awtextra.AbsoluteConstraints(12, 13, -1, -1));
 
         jLabel14.setText("Village:");
         jPanel2.add(jLabel14, new org.netbeans.lib.awtextra.AbsoluteConstraints(249, 58, -1, -1));
 
         jLabel13.setText("District:");
         jPanel2.add(jLabel13, new org.netbeans.lib.awtextra.AbsoluteConstraints(12, 33, -1, -1));
 
         jLabel12.setText("Region:");
         jPanel2.add(jLabel12, new org.netbeans.lib.awtextra.AbsoluteConstraints(12, 58, -1, -1));
 
         jLabel8.setText("Country:");
         jPanel2.add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(245, 13, -1, -1));
 
         jLabel9.setText("Place:");
         jPanel2.add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(249, 33, -1, -1));
 
         jLabel10.setText("Language:");
         jPanel2.add(jLabel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(12, 83, -1, -1));
 
         IPEO.setText("People group");
         jPanel2.add(IPEO, new org.netbeans.lib.awtextra.AbsoluteConstraints(85, 13, -1, -1));
 
         ICON.setText("ICON");
         jPanel2.add(ICON, new org.netbeans.lib.awtextra.AbsoluteConstraints(298, 13, -1, -1));
         ICON.getAccessibleContext().setAccessibleDescription("");
 
         IDIS.setText("District");
         jPanel2.add(IDIS, new org.netbeans.lib.awtextra.AbsoluteConstraints(55, 33, -1, -1));
 
         IPLA.setText("Place");
         jPanel2.add(IPLA, new org.netbeans.lib.awtextra.AbsoluteConstraints(289, 33, -1, -1));
 
         IVIL.setText("Village");
         jPanel2.add(IVIL, new org.netbeans.lib.awtextra.AbsoluteConstraints(289, 58, -1, -1));
 
         IREG.setText("Region");
         jPanel2.add(IREG, new org.netbeans.lib.awtextra.AbsoluteConstraints(55, 58, -1, -1));
 
         ILAN.setText("Language");
         jPanel2.add(ILAN, new org.netbeans.lib.awtextra.AbsoluteConstraints(69, 83, -1, -1));
 
         getContentPane().add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 195, 453, 110));
 
         jPanel3.setBorder(javax.swing.BorderFactory.createEtchedBorder());
 
         jLabel6.setText("Album:");
 
         jLabel2.setText("Artist:");
 
         jLabel1.setText("Title:");
 
         jLabel4.setText("Lyric writer:");
 
         jLabel3.setText("Composer:");
 
         jLabel7.setText("Supplier:");
 
         INAM.setText("Track");
 
         IART.setText("Artist");
 
         ICOM.setText("Composer");
 
         IALB.setText("IALB");
 
         ILYR.setText("Writer");
 
         ISRC.setText("ISRC");
 
         javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
         jPanel3.setLayout(jPanel3Layout);
         jPanel3Layout.setHorizontalGroup(
             jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel3Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(jPanel3Layout.createSequentialGroup()
                         .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(jLabel3)
                             .addComponent(jLabel4))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(ILYR)
                             .addComponent(ICOM))
                         .addGap(0, 130, Short.MAX_VALUE))
                     .addGroup(jPanel3Layout.createSequentialGroup()
                         .addComponent(jLabel1)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(INAM)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                 .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                         .addComponent(jLabel2)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(IART)
                         .addGap(127, 127, 127))
                     .addGroup(jPanel3Layout.createSequentialGroup()
                         .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addGroup(jPanel3Layout.createSequentialGroup()
                                 .addComponent(jLabel7)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                 .addComponent(ISRC))
                             .addGroup(jPanel3Layout.createSequentialGroup()
                                 .addComponent(jLabel6)
                                 .addGap(3, 3, 3)
                                 .addComponent(IALB)))
                         .addContainerGap())))
         );
         jPanel3Layout.setVerticalGroup(
             jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel3Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel1)
                     .addComponent(jLabel2)
                     .addComponent(INAM)
                     .addComponent(IART))
                 .addGap(14, 14, 14)
                 .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                         .addComponent(jLabel6)
                         .addComponent(IALB))
                     .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                         .addComponent(jLabel3)
                         .addComponent(ICOM)))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(ISRC, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                         .addComponent(jLabel4)
                         .addComponent(jLabel7)
                         .addComponent(ILYR)))
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
         getContentPane().add(jPanel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 84, 453, -1));
 
         jButton2.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
         jButton2.setForeground(new java.awt.Color(240, 240, 240));
         jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/nrkprosjekt/graphics/button.png"))); // NOI18N
         jButton2.setText("CLOSE");
         jButton2.setContentAreaFilled(false);
         jButton2.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
         jButton2.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         jButton2.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButton2ActionPerformed(evt);
             }
         });
         getContentPane().add(jButton2, new org.netbeans.lib.awtextra.AbsoluteConstraints(170, 450, -1, -1));
 
         jPanel4.setBorder(javax.swing.BorderFactory.createEtchedBorder());
 
         jLabel17.setText("Digitization source:");
 
         jLabel20.setText("Digitization date:");
 
         jLabel18.setText("Original medium:");
 
         jLabel19.setText("Original date:");
 
         jLabel16.setText("Keywords:");
 
         jLabel11.setText("Genre:");
 
         ISRF.setText("ISRF");
 
         ICRD.setText("jLabel35");
 
         IDIG.setText("Date");
 
         IMED.setText("IMED");
 
         IKEY.setText("IKEY");
 
         IGNR.setText("IGNR");
 
         javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
         jPanel4.setLayout(jPanel4Layout);
         jPanel4Layout.setHorizontalGroup(
             jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel4Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(jPanel4Layout.createSequentialGroup()
                         .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addGroup(jPanel4Layout.createSequentialGroup()
                                 .addComponent(jLabel17)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                 .addComponent(ISRF))
                             .addGroup(jPanel4Layout.createSequentialGroup()
                                 .addComponent(jLabel18)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                 .addComponent(IMED))
                             .addGroup(jPanel4Layout.createSequentialGroup()
                                 .addComponent(jLabel11)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                 .addComponent(IGNR, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 109, Short.MAX_VALUE)
                         .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                                 .addComponent(jLabel20)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                 .addComponent(ICRD)
                                 .addGap(83, 83, 83))
                             .addGroup(jPanel4Layout.createSequentialGroup()
                                 .addComponent(jLabel19)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                 .addComponent(IDIG)
                                 .addContainerGap())))
                     .addGroup(jPanel4Layout.createSequentialGroup()
                         .addComponent(jLabel16)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(IKEY)
                         .addGap(0, 0, Short.MAX_VALUE))))
         );
         jPanel4Layout.setVerticalGroup(
             jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel4Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel17)
                     .addComponent(jLabel20)
                     .addComponent(ISRF)
                     .addComponent(ICRD))
                 .addGap(18, 18, 18)
                 .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(IDIG, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                         .addComponent(jLabel18)
                         .addComponent(jLabel19)
                         .addComponent(IMED)))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 9, Short.MAX_VALUE)
                 .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel11)
                     .addComponent(IGNR))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel16)
                     .addComponent(IKEY))
                 .addContainerGap())
         );
 
         getContentPane().add(jPanel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 316, 453, 120));
 
         pack();
     }// </editor-fold>//GEN-END:initComponents
 
     private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        closeWindow();
     }//GEN-LAST:event_jButton2ActionPerformed
 
         private void closeWindow() {
         int val = javax.swing.JOptionPane.showOptionDialog(null, "Are you sure you want to exit?",
                 "Exit", javax.swing.JOptionPane.YES_NO_OPTION,
                 javax.swing.JOptionPane.QUESTION_MESSAGE, null, option, option[0]);
         
         if(val == 1) {
             System.exit(0);
         }
     }
   
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
             java.util.logging.Logger.getLogger(SongInformation.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (InstantiationException ex) {
             java.util.logging.Logger.getLogger(SongInformation.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (IllegalAccessException ex) {
             java.util.logging.Logger.getLogger(SongInformation.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (javax.swing.UnsupportedLookAndFeelException ex) {
             java.util.logging.Logger.getLogger(SongInformation.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         }
         //</editor-fold>
 
         /* Create and display the dialog */
         java.awt.EventQueue.invokeLater(new Runnable() {
             public void run() {
                 SongInformation dialog = null;
                 try {
                     dialog = new SongInformation(new javax.swing.JFrame(), true);
                 } catch (SQLException ex) {
                     Logger.getLogger(SongInformation.class.getName()).log(Level.SEVERE, null, ex);
                 } catch (FileNotFoundException ex) {
                     Logger.getLogger(SongInformation.class.getName()).log(Level.SEVERE, null, ex);
                 } catch (IOException ex) {
                     Logger.getLogger(SongInformation.class.getName()).log(Level.SEVERE, null, ex);
                 }
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
     private javax.swing.JLabel IALB;
     private javax.swing.JLabel IART;
     private javax.swing.JLabel ICOM;
     private javax.swing.JLabel ICON;
     private javax.swing.JLabel ICRD;
     private javax.swing.JLabel IDIG;
     private javax.swing.JLabel IDIS;
     private javax.swing.JLabel IGNR;
     private javax.swing.JLabel IKEY;
     private javax.swing.JLabel ILAN;
     private javax.swing.JLabel ILYR;
     private javax.swing.JLabel IMED;
     private javax.swing.JLabel INAM;
     private javax.swing.JLabel IPEO;
     private javax.swing.JLabel IPLA;
     private javax.swing.JLabel IREG;
     private javax.swing.JLabel ISRC;
     private javax.swing.JLabel ISRF;
     private javax.swing.JLabel IVIL;
     private javax.swing.JButton jButton2;
     private javax.swing.JLabel jLabel1;
     private javax.swing.JLabel jLabel10;
     private javax.swing.JLabel jLabel11;
     private javax.swing.JLabel jLabel12;
     private javax.swing.JLabel jLabel13;
     private javax.swing.JLabel jLabel14;
     private javax.swing.JLabel jLabel15;
     private javax.swing.JLabel jLabel16;
     private javax.swing.JLabel jLabel17;
     private javax.swing.JLabel jLabel18;
     private javax.swing.JLabel jLabel19;
     private javax.swing.JLabel jLabel2;
     private javax.swing.JLabel jLabel20;
     private javax.swing.JLabel jLabel21;
     private javax.swing.JLabel jLabel3;
     private javax.swing.JLabel jLabel4;
     private javax.swing.JLabel jLabel6;
     private javax.swing.JLabel jLabel7;
     private javax.swing.JLabel jLabel8;
     private javax.swing.JLabel jLabel9;
     private javax.swing.JPanel jPanel1;
     private javax.swing.JPanel jPanel2;
     private javax.swing.JPanel jPanel3;
     private javax.swing.JPanel jPanel4;
     private javax.swing.JSeparator jSeparator1;
     private javax.swing.JLabel showing1;
     // End of variables declaration//GEN-END:variables
 }
