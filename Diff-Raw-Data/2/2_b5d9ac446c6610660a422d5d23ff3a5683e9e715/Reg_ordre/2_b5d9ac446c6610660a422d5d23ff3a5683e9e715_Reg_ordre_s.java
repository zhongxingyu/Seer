 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package streamfish;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.DefaultListModel;
 
 /**
  *
  * @author Kristian
  */
 public class Reg_ordre extends javax.swing.JPanel {
 
 	private final int CUSTID;
 	private GUI gui;
 	private Menu[] menu;
 	private CustomerAddress[] address;
 	private Object[] addressPlus1;
 	private Reg_ordre order = this;
 	private DefaultComboBoxModel comboBox;
 
 	/**
 	 * Creates new form Reg_kunde
 	 */
 	public Reg_ordre(int custid, final GUI gui) {
 
 		this.gui = gui;
 		this.CUSTID = custid;
 		menu = gui.getMenus();
 		address = gui.getAddress(CUSTID);
 		addressPlus1 = new Object[address.length + 1];
 		for (int i = 0; i < address.length; i++) {
 			addressPlus1[i] = address[i];
 		}
 		addressPlus1[address.length] = new String("Add new address");
 		initComponents();
 		jLabel1.setText("Kundenr: " + CUSTID);
 		DefaultListModel model = (DefaultListModel) jList1.getModel();
 		for (Menu men : menu) {
 			model.addElement(men);
 		}
 
 		comboBox = (DefaultComboBoxModel) jComboBox1.getModel();
 		for (Object addr : addressPlus1) {
 			comboBox.addElement(addr);
 		}
 		jComboBox1.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				javax.swing.JComboBox box = (javax.swing.JComboBox) e.getSource();
				if (box.getSelectedItem().getClass().equals(String.class)) {
 					AddAddress address = new AddAddress(gui, CUSTID);
 					address.addWindowListener(new WindowListener(){
 						@Override
 						public void windowOpened(WindowEvent e) {
 						}
 						@Override
 						public void windowClosing(WindowEvent e) {
 						}
 						@Override
 						public void windowClosed(WindowEvent e) {
 							order.update();
 						}
 						@Override
 						public void windowIconified(WindowEvent e) {
 						}
 						@Override
 						public void windowDeiconified(WindowEvent e) {
 						}
 						@Override
 						public void windowActivated(WindowEvent e) {
 						}
 						@Override
 						public void windowDeactivated(WindowEvent e) {
 						}
 					});
 				}
 			}
 		});
 
 	}
 
 	private void update() {
 		address = gui.getAddress(CUSTID);
 		addressPlus1 = new Object[address.length + 1];
 		for (int i = 0; i < address.length; i++) {
 			addressPlus1[i] = address[i];
 		}
 		addressPlus1[address.length] = new String("Add new address");
 		comboBox.removeAllElements();
 		comboBox = (DefaultComboBoxModel) jComboBox1.getModel();
 		
 		for (Object addr : addressPlus1) {
 			comboBox.addElement(addr);
 		}
 	}
 
 	@SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         jScrollPane1 = new javax.swing.JScrollPane();
         jList1 = new javax.swing.JList();
         jButton1 = new javax.swing.JButton();
         jButton2 = new javax.swing.JButton();
         jLabel1 = new javax.swing.JLabel();
         jSpinner1 = new javax.swing.JSpinner();
         jLabel2 = new javax.swing.JLabel();
         jLabel3 = new javax.swing.JLabel();
         jLabel4 = new javax.swing.JLabel();
         jLabel5 = new javax.swing.JLabel();
         jComboBox1 = new javax.swing.JComboBox();
         jComboBox2 = new javax.swing.JComboBox();
         jComboBox3 = new javax.swing.JComboBox();
         jComboBox4 = new javax.swing.JComboBox();
         jComboBox5 = new javax.swing.JComboBox();
         jComboBox6 = new javax.swing.JComboBox();
         jLabel6 = new javax.swing.JLabel();
 
         jList1.setModel(new DefaultListModel());
         jScrollPane1.setViewportView(jList1);
 
         jButton1.setText("Cancel");
         jButton1.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButton1ActionPerformed(evt);
             }
         });
 
         jButton2.setText("Register");
         jButton2.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButton2ActionPerformed(evt);
             }
         });
 
         jLabel1.setText("Kundenr: " + CUSTID);
 
         jSpinner1.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(1), Integer.valueOf(1), null, Integer.valueOf(1)));
         jSpinner1.setValue(1);
 
         jLabel2.setText("Delivery date: (yyyy-mm-dd)");
         jLabel2.setToolTipText("");
 
         jLabel3.setText("# Persons");
 
         jLabel4.setText("Select menu:");
 
         jLabel5.setText("Address");
 
         jComboBox1.setModel(new DefaultComboBoxModel());
 
         jComboBox2.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "2013", "2014", "2015", "2016", "2017", "2018", "2019", "2020" }));
 
         jComboBox3.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12" }));
 
         jComboBox4.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31" }));
 
         jComboBox5.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23" }));
 
         jComboBox6.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "00", "15", "30", "45" }));
 
         jLabel6.setText("Time: (hh-mm)");
         jLabel6.setToolTipText("");
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
         this.setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jComboBox1, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(jScrollPane1)
                     .addGroup(layout.createSequentialGroup()
                         .addComponent(jButton1)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                         .addComponent(jButton2))
                     .addGroup(layout.createSequentialGroup()
                         .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 138, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addComponent(jLabel4)
                             .addComponent(jLabel5))
                         .addGap(0, 0, Short.MAX_VALUE))
                     .addGroup(layout.createSequentialGroup()
                         .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(jLabel2)
                             .addGroup(layout.createSequentialGroup()
                                 .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                 .addGap(0, 0, 0)
                                 .addComponent(jComboBox3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                 .addGap(0, 0, 0)
                                 .addComponent(jComboBox4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                         .addGap(18, 18, 18)
                         .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addGroup(layout.createSequentialGroup()
                                 .addComponent(jComboBox5, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                 .addComponent(jComboBox6, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE))
                             .addComponent(jLabel6))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 57, Short.MAX_VALUE)
                         .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(jSpinner1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.TRAILING))))
                 .addContainerGap())
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jLabel1)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jLabel4)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel2)
                     .addComponent(jLabel3)
                     .addComponent(jLabel6))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jSpinner1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jComboBox3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jComboBox4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jComboBox5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jComboBox6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jLabel5)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 18, Short.MAX_VALUE)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jButton1)
                     .addComponent(jButton2))
                 .addContainerGap())
         );
     }// </editor-fold>//GEN-END:initComponents
 
     private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
 		// TODO add your handling code here:
 		gui.byttVindu(this, "streamfish.MainMenu");
     }//GEN-LAST:event_jButton1ActionPerformed
 
     private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
 
 		Menu selMenu = (Menu) jList1.getSelectedValue();
 		int antPers = Integer.parseInt(jSpinner1.getValue().toString());
 
 		String date = (String) jComboBox2.getSelectedItem() + "-" + (String) jComboBox3.getSelectedItem() + "-" + (String) jComboBox4.getSelectedItem();
 		String time = (String) jComboBox5.getSelectedItem() + ":" + (String) jComboBox6.getSelectedItem();
 		CustomerAddress orderAddress = (CustomerAddress) jComboBox1.getSelectedItem();
 		Order order = new Order(selMenu.getMenuId(), CUSTID, 1/*TODO?*/, antPers, date, time, orderAddress);
 		gui.registerOrder(order);
 		gui.byttVindu(this, "streamfish.MainMenu");
 
 
     }//GEN-LAST:event_jButton2ActionPerformed
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JButton jButton1;
     private javax.swing.JButton jButton2;
     private javax.swing.JComboBox jComboBox1;
     private javax.swing.JComboBox jComboBox2;
     private javax.swing.JComboBox jComboBox3;
     private javax.swing.JComboBox jComboBox4;
     private javax.swing.JComboBox jComboBox5;
     private javax.swing.JComboBox jComboBox6;
     private javax.swing.JLabel jLabel1;
     private javax.swing.JLabel jLabel2;
     private javax.swing.JLabel jLabel3;
     private javax.swing.JLabel jLabel4;
     private javax.swing.JLabel jLabel5;
     private javax.swing.JLabel jLabel6;
     private javax.swing.JList jList1;
     private javax.swing.JScrollPane jScrollPane1;
     private javax.swing.JSpinner jSpinner1;
     // End of variables declaration//GEN-END:variables
 }
