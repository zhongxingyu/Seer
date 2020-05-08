 package test.xtab;
 /*
  * Address.java
  *
  * Created on October 6, 2010, 2:01 PM
  * @author jaycverg
  */
 
 
 public class Address extends javax.swing.JPanel {
     
     public Address() {
         initComponents();
     }
     
     // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
     private void initComponents() {
         jScrollPane1 = new javax.swing.JScrollPane();
         formPanel1 = new com.rameses.rcp.util.FormPanel();
         xTextField1 = new com.rameses.rcp.control.XTextField();
         xTextField2 = new com.rameses.rcp.control.XTextField();
         xTextField3 = new com.rameses.rcp.control.XTextField();
         xTextField4 = new com.rameses.rcp.control.XTextField();
 
         formPanel1.setCaptionWidth(120);
         xTextField1.setCaption("Street");
         xTextField1.setName("entity.street");
         xTextField1.setPreferredSize(new java.awt.Dimension(150, 19));
         formPanel1.add(xTextField1);
 
         xTextField2.setCaption("Barangay");
         xTextField2.setName("entity.barangay");
         xTextField2.setPreferredSize(new java.awt.Dimension(150, 19));
         formPanel1.add(xTextField2);
 
         xTextField3.setCaption("City/Municipality");
         xTextField3.setName("entity.lgu");
         xTextField3.setPreferredSize(new java.awt.Dimension(150, 19));
         formPanel1.add(xTextField3);
 
         xTextField4.setCaption("Province");
         xTextField4.setName("entity.province");
         xTextField4.setPreferredSize(new java.awt.Dimension(150, 19));
         formPanel1.add(xTextField4);
 
         jScrollPane1.setViewportView(formPanel1);
 
         org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
         this.setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jScrollPane1)
         );
     }// </editor-fold>//GEN-END:initComponents
     
     
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private com.rameses.rcp.util.FormPanel formPanel1;
     private javax.swing.JScrollPane jScrollPane1;
     private com.rameses.rcp.control.XTextField xTextField1;
     private com.rameses.rcp.control.XTextField xTextField2;
     private com.rameses.rcp.control.XTextField xTextField3;
     private com.rameses.rcp.control.XTextField xTextField4;
     // End of variables declaration//GEN-END:variables
     
 }
