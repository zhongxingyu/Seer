 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package streamfish;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.table.DefaultTableModel;
 import static javax.swing.JOptionPane.*;
 
 /**
  *
  * @author Kristian
  */
 public class MainMenu extends javax.swing.JPanel {
 
     private int kundenr = -1;
     private final GUI gui;
     private Customer[] customers;
     private int viewRow;
     private Orderinfo[] orderinfo;
 
     /**
      * Creates new form MainMenu
      */
     public MainMenu(final GUI gui) {
         this.gui = gui;
         gui.setTitle("Main Menu");
         initComponents();
         customers = gui.getCustomers(jTextField1.getText(), jCheckBox1.isSelected());
 //		jTable1.setModel();1
         if (customers != null && customers.length > 0) {
             for (int i = 0; i < customers.length; i++) {
                 DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
                 model.addRow(new Object[]{customers[i].getCustomerID(), customers[i].getCustomerName(), customers[i].getPhoneNumber(), customers[i].isBusiness()});
             }
         }
         // from here tab2 NorC
         orderinfo = gui.getTodaysTasks();
 
         if (orderinfo != null && orderinfo.length > 0) {
             for (int i = 0; i < orderinfo.length; i++) {
                 DefaultTableModel model = (DefaultTableModel) jTable2.getModel();
                 model.addRow(new Object[]{orderinfo[i].getAddress(), orderinfo[i].getCustomerName(), orderinfo[i].getPhone()});
 
             }
         }
         
         jTable1.getSelectionModel().addListSelectionListener(
                 new ListSelectionListener() {
                     public void valueChanged(ListSelectionEvent event) {
 
                         if (!event.getValueIsAdjusting()) {
                             viewRow = jTable1.getSelectedRow();
                         }
                     }
                 });
         //to here tab2 Norc
         jTextField1.getDocument().addDocumentListener(new DocumentListener() {
             @Override
             public void insertUpdate(DocumentEvent e) {
                 customers = gui.getCustomers(jTextField1.getText(), jCheckBox1.isSelected());
                 DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
                 model.setRowCount(0);
                 if (customers != null && customers.length > 0) {
                     for (int i = 0; i < customers.length; i++) {
                         model.addRow(new Object[]{customers[i].getCustomerID(), customers[i].getCustomerName(), customers[i].getPhoneNumber(), customers[i].isBusiness()});
                     }
                 }
             }
 
             @Override
             public void removeUpdate(DocumentEvent e) {
                 customers = gui.getCustomers(jTextField1.getText(), jCheckBox1.isSelected());
                 DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
                 model.setRowCount(0);
                 if (customers != null && customers.length > 0) {
                     for (int i = 0; i < customers.length; i++) {
                         model.addRow(new Object[]{customers[i].getCustomerID(), customers[i].getCustomerName(), customers[i].getPhoneNumber(), customers[i].isBusiness()});
                     }
                 }
             }
 
             @Override
             public void changedUpdate(DocumentEvent e) {
                 customers = gui.getCustomers(jTextField1.getText(), jCheckBox1.isSelected());
                 DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
                 model.setRowCount(0);
                 if (customers != null && customers.length > 0) {
                     for (int i = 0; i < customers.length; i++) {
                         model.addRow(new Object[]{customers[i].getCustomerID(), customers[i].getCustomerName(), customers[i].getPhoneNumber(), customers[i].isBusiness()});
                     }
                 }
             }
         });
         
         
         jCheckBox1.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 customers = gui.getCustomers(jTextField1.getText(), jCheckBox1.isSelected());
                 DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
                 model.setRowCount(0);
                 if (customers != null && customers.length > 0) {
                     for (int i = 0; i < customers.length; i++) {
                         model.addRow(new Object[]{customers[i].getCustomerID(), customers[i].getCustomerName(), customers[i].getPhoneNumber(), customers[i].isBusiness()});
                     }
                 }
             }
         });
         jTable1.getSelectionModel().addListSelectionListener(
                 new ListSelectionListener() {
                     public void valueChanged(ListSelectionEvent event) {
                         int viewRow = jTable1.getSelectedRow();
                         if (!event.getValueIsAdjusting()) {
                             try {
                                 kundenr = Integer.parseInt(jTable1.getValueAt(viewRow, 0).toString());
                             } catch (Exception e) {
                             }
                         }
                     }
                 });
     }
 
     public void updt() {
 
         customers = gui.getCustomers(jTextField1.getText(), jCheckBox1.isSelected());
         DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
         model.setRowCount(0);
         if (customers != null && customers.length > 0) {
             for (int i = 0; i < customers.length; i++) {
                 model.addRow(new Object[]{customers[i].getCustomerID(), customers[i].getCustomerName(), customers[i].getPhoneNumber(), customers[i].isBusiness()});
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
 
         jButton1 = new javax.swing.JButton();
         jButton2 = new javax.swing.JButton();
         jTextField1 = new javax.swing.JTextField();
         jLabel1 = new javax.swing.JLabel();
         jButton3 = new javax.swing.JButton();
         jButton4 = new javax.swing.JButton();
         jCheckBox1 = new javax.swing.JCheckBox();
         jButton5 = new javax.swing.JButton();
         jTabbedPane1 = new javax.swing.JTabbedPane();
         jPanel1 = new javax.swing.JPanel();
         jScrollPane2 = new javax.swing.JScrollPane();
         jTable1 = new javax.swing.JTable();
         jPanel2 = new javax.swing.JPanel();
         jScrollPane1 = new javax.swing.JScrollPane();
         jTable2 = new javax.swing.JTable();
         jPanel3 = new javax.swing.JPanel();
         jScrollPane3 = new javax.swing.JScrollPane();
         jTable3 = new javax.swing.JTable();
 
         jButton1.setText("Register customer");
         jButton1.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButton1ActionPerformed(evt);
             }
         });
 
         jButton2.setText("Register order");
         jButton2.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButton2ActionPerformed(evt);
             }
         });
 
         jLabel1.setText("Search:");
 
         jButton3.setText("Edit customer");
         jButton3.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButton3ActionPerformed(evt);
             }
         });
 
         jButton4.setText("Get info");
         jButton4.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButton4ActionPerformed(evt);
             }
         });
 
         jCheckBox1.setText("Show only inactive");
 
         jButton5.setText("Storage");
         jButton5.setToolTipText("");
         jButton5.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButton5ActionPerformed(evt);
             }
         });
 
         jTable1.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
 
             },
             new String [] {
                 "Customer_id", "Customer name", "Phone", "Business"
             }
         ) {
             boolean[] canEdit = new boolean [] {
                 false, false, false, false
             };
 
             public boolean isCellEditable(int rowIndex, int columnIndex) {
                 return canEdit [columnIndex];
             }
         });
         jTable1.setMaximumSize(new java.awt.Dimension(300, 64));
         jTable1.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
         jScrollPane2.setViewportView(jTable1);
         jTable1.getColumnModel().getColumn(0).setResizable(false);
         jTable1.getColumnModel().getColumn(1).setResizable(false);
         jTable1.getColumnModel().getColumn(2).setResizable(false);
         jTable1.getColumnModel().getColumn(3).setResizable(false);
 
         javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
         jPanel1.setLayout(jPanel1Layout);
         jPanel1Layout.setHorizontalGroup(
             jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 445, Short.MAX_VALUE)
         );
         jPanel1Layout.setVerticalGroup(
             jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 176, Short.MAX_VALUE)
         );
 
         jTabbedPane1.addTab("Customers", jPanel1);
 
         jTable2.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
 
             },
             new String [] {
                 "Address", "Name", "Phone number"
             }
         ));
         jScrollPane1.setViewportView(jTable2);
 
         javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
         jPanel2.setLayout(jPanel2Layout);
         jPanel2Layout.setHorizontalGroup(
             jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 445, Short.MAX_VALUE)
         );
         jPanel2Layout.setVerticalGroup(
             jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                 .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 177, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(0, 0, Short.MAX_VALUE))
         );
 
         jTabbedPane1.addTab("Todays tasks", jPanel2);
 
         jTable3.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
 
             },
             new String [] {
                 "Title 1", "Title 2", "Title 3", "Title 4"
             }
         ));
         jScrollPane3.setViewportView(jTable3);
 
         javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
         jPanel3.setLayout(jPanel3Layout);
         jPanel3Layout.setHorizontalGroup(
             jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                 .addGap(0, 0, Short.MAX_VALUE)
                 .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 445, javax.swing.GroupLayout.PREFERRED_SIZE))
         );
         jPanel3Layout.setVerticalGroup(
             jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 176, Short.MAX_VALUE)
         );
 
         jTabbedPane1.addTab("Subscriptions", jPanel3);
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
         this.setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(layout.createSequentialGroup()
                         .addComponent(jButton1)
                         .addGap(18, 18, 18)
                         .addComponent(jButton3)
                         .addGap(18, 18, 18)
                         .addComponent(jButton4)
                         .addGap(18, 18, 18)
                         .addComponent(jButton2))
                     .addGroup(layout.createSequentialGroup()
                         .addComponent(jCheckBox1)
                         .addGap(18, 18, 18)
                         .addComponent(jButton5)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                         .addComponent(jLabel1)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
                     .addComponent(jTabbedPane1))
                 .addContainerGap())
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jLabel1)
                     .addComponent(jCheckBox1)
                     .addComponent(jButton5))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 201, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 30, Short.MAX_VALUE)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jButton2)
                     .addComponent(jButton1)
                     .addComponent(jButton3)
                     .addComponent(jButton4))
                 .addContainerGap())
         );
     }// </editor-fold>//GEN-END:initComponents
 
     private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
         if (kundenr == -1) {
             showMessageDialog(null, "Ingen kunde er valgt.");
         } else {
             gui.byttVindu(this, new Reg_ordre(kundenr, gui));
         }
     }//GEN-LAST:event_jButton2ActionPerformed
 
     private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
         // TODO add your handling code here:
         gui.byttVindu(this, new Reg_kunde(gui));
     }//GEN-LAST:event_jButton1ActionPerformed
 
     private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
         // TODO add your handling code here:
         if (kundenr == -1) {
             showMessageDialog(null, "Ingen kunde er valgt.");
         } else {
             gui.byttVindu(this, new Edit_customer(kundenr, gui));
         }
 
     }//GEN-LAST:event_jButton3ActionPerformed
 
     private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        if(jTabbedPane1.getSelectedIndex() == 0){
             System.out.println("pane1 works");
        }else if(jTabbedPane1.getSelectedIndex() == 1){
             new TodaysTasksFrame(orderinfo[viewRow], gui);
        }else{
            System.out.println("panel3 works!");
         }
     }//GEN-LAST:event_jButton4ActionPerformed
 
     private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
         // TODO add your handling code here:
         gui.byttVindu(this, new Storage(gui));
     }//GEN-LAST:event_jButton5ActionPerformed
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JButton jButton1;
     private javax.swing.JButton jButton2;
     private javax.swing.JButton jButton3;
     private javax.swing.JButton jButton4;
     private javax.swing.JButton jButton5;
     private javax.swing.JCheckBox jCheckBox1;
     private javax.swing.JLabel jLabel1;
     private javax.swing.JPanel jPanel1;
     private javax.swing.JPanel jPanel2;
     private javax.swing.JPanel jPanel3;
     private javax.swing.JScrollPane jScrollPane1;
     private javax.swing.JScrollPane jScrollPane2;
     private javax.swing.JScrollPane jScrollPane3;
     private javax.swing.JTabbedPane jTabbedPane1;
     private javax.swing.JTable jTable1;
     private javax.swing.JTable jTable2;
     private javax.swing.JTable jTable3;
     private javax.swing.JTextField jTextField1;
     // End of variables declaration//GEN-END:variables
 }
