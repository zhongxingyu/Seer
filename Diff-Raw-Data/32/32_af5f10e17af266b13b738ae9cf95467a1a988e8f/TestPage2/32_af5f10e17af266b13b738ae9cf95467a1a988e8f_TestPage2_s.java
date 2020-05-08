 package test;
 
 import java.awt.event.ActionEvent;
 import java.beans.PropertyChangeListener;
 import javax.swing.Action;
 
 /*
  * TestPage2.java
  *
  * Created on October 8, 2010, 11:04 AM
  * @author jaycverg
  */
 
 
 public class TestPage2 extends javax.swing.JPanel {
     
     public TestPage2() {
         initComponents();
     }
     
     // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
     private void initComponents() {
         jPanel1 = new javax.swing.JPanel();
         formPanel1 = new com.rameses.rcp.util.FormPanel();
         xTextField1 = new com.rameses.rcp.control.XTextField();
         xDateField1 = new com.rameses.rcp.control.XDateField();
 
         formPanel1.setLayout(null);
 
        com.rameses.rcp.control.border.XTitledBorder xTitledBorder1 = new com.rameses.rcp.control.border.XTitledBorder();
        xTitledBorder1.setOutline(new java.awt.Color(153, 153, 153));
        xTitledBorder1.setTitleBackground(new java.awt.Color(204, 204, 204));
        formPanel1.setBorder(xTitledBorder1);
         com.rameses.rcp.control.border.XUnderlineBorder xUnderlineBorder1 = new com.rameses.rcp.control.border.XUnderlineBorder();
         xUnderlineBorder1.setLineColor(new java.awt.Color(217, 216, 216));
         formPanel1.setCaptionBorder(xUnderlineBorder1);
         xTextField1.setText("xTextField1");
         formPanel1.add(xTextField1);
        xTextField1.setBounds(80, 0, 72, 19);
 
         xDateField1.setText("xDateField1");
         formPanel1.add(xDateField1);
        xDateField1.setBounds(80, 0, 78, 19);
 
         org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
         jPanel1.setLayout(jPanel1Layout);
         jPanel1Layout.setHorizontalGroup(
             jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(jPanel1Layout.createSequentialGroup()
                 .addContainerGap()
                 .add(formPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 325, Short.MAX_VALUE)
                 .addContainerGap())
         );
         jPanel1Layout.setVerticalGroup(
             jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(jPanel1Layout.createSequentialGroup()
                 .addContainerGap()
                 .add(formPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 147, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(25, Short.MAX_VALUE))
         );
 
         org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
         this.setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(layout.createSequentialGroup()
                 .addContainerGap()
                 .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(43, Short.MAX_VALUE))
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(layout.createSequentialGroup()
                 .addContainerGap()
                 .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(72, Short.MAX_VALUE))
         );
     }// </editor-fold>//GEN-END:initComponents
     
     
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private com.rameses.rcp.util.FormPanel formPanel1;
     private javax.swing.JPanel jPanel1;
     private com.rameses.rcp.control.XDateField xDateField1;
     private com.rameses.rcp.control.XTextField xTextField1;
     // End of variables declaration//GEN-END:variables
     
 }
