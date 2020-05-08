 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package cars;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import javax.swing.*;
 
 /**
  *
  * @author Greg Mathews
  */
 public class CarUI extends javax.swing.JFrame {
 
     /**
      * Creates new form CarUI
      */
     public CarUI() {
         initComponents();
     }
     public JComboBox getComboBox1(){
         return makeComboBox;
     }
     public JComboBox getComboBoxMake(){
         return makeComboBox1;
     }
     public JComboBox getComboBox2(){
         return modelComboBox;
     }
     public JComboBox getComboBox3(){
         return yearComboBox;
     }
     public JComboBox getComboBox4(){
         return jComboBox1;
     }
     public JTextArea getTextArea(){
         return jTextArea1;
     }
     public JTextArea getTextArea2(){
         return jTextArea2;
     }
     public JButton getJButton1(){
         return jButton1;
     }
     public JButton getJButton2(){
         return jButton2;
     }
     public JButton getJButton3(){
         return jButton3;
     }
     public JButton getJButton4(){
         return jButton4;
     }
     public JTextField getJText1(){
         return jTextField1;
     }
     public JTextField getJText2(){
         return jTextField2;
     }
     public JTextField getJText3(){
         return jTextField3;
     }
     public JTextField getJText4(){
         return jTextField4;
     }
     public JTextField getJText5(){
         return jTextField5;
     }
     public JTextField getJText6(){
         return jTextField6;
     }
     public JTextField getJText7(){
         return jTextField7;
     }
     public JTextField getJText8(){
         return jTextField8;
     }
     public JTextField getJText9(){
         return jTextField9;
     }
     public JTextField getJText10(){
         return jTextField10;
     }
     public JTextField getJText11(){
         return jTextField11;
     }
     public JTextField getJText12(){
         return jTextField12;
     }
     public JTextField getJText13(){
         return jTextField13;
     }
     public JTextField getJText14(){
         return jTextField14;
     }
     public JTextField getJText15(){
         return jTextField15;
     }
     public JTextField getJText16(){
         return jTextField16;
     }
     public JTextField getJText17(){
         return jTextField17;
     }
     public String selectedMake(String selectedMake){
         //Methods in below comments are named incorrectly
 //String selectedMake = getComboBoxText();
 //String threeLetterMake = null;
 String threeLetterMake = "";
 if (selectedMake.equals("AMERICAN MOTORS OR AMC"))
 	threeLetterMake = "AMC";
 else if (selectedMake.equals("AUDI"))
 	threeLetterMake="AUD";
 else if (selectedMake.equals("BMW"))
 	threeLetterMake="BMW";
 else if (selectedMake.equals("BUICK"))
 	threeLetterMake="BUK";
 else if (selectedMake.equals("CADILLAC"))
 	threeLetterMake="CAD";
 else if (selectedMake.equals("CHEVROLET"))
 	threeLetterMake="CHE";
 else if (selectedMake.equals("CHRYSLER"))
 	threeLetterMake="CRY";
 else if (selectedMake.equals("DODGE"))
 	threeLetterMake="DOD";
 else if (selectedMake.equals("DODGE/PLYMOUTH LITE TRK & VAN"))
 	threeLetterMake="DOT";
 else if (selectedMake.equals("FORD LIGHT TRUCK AND VAN"))
 	threeLetterMake="FDT";
 else if (selectedMake.equals("FORD MEDIUM AND HEAVY TRUCK"))
 	threeLetterMake="FDM";
 else if (selectedMake.equals("FIAT"))
 	threeLetterMake="FIA";
 else if (selectedMake.equals("FORD"))
 	threeLetterMake="FOR";
 else if (selectedMake.equals("CHEVROLET & GMC TRUCK & VAN"))
 	threeLetterMake="GMC";
 else if (selectedMake.equals("HONDA"))
 	threeLetterMake="HON";
 else if (selectedMake.equals("INTERNATIONAL TRUCK (I.H.C.)"))
 	threeLetterMake="INT";
 else if (selectedMake.equals("ISUZU"))
 	threeLetterMake="ISU";
 else if (selectedMake.equals("LINCOLN"))
 	threeLetterMake="LIN";
 else if (selectedMake.equals("MERCEDES"))
 	threeLetterMake="MCS";
 else if (selectedMake.equals("MITSUBISHI"))
 	threeLetterMake="MIT";
 else if (selectedMake.equals("MERCURY"))
 	threeLetterMake="MRY";
 else if (selectedMake.equals("MAZDA"))
 	threeLetterMake="MZD";
 else if (selectedMake.equals("NISSAN/DATSUN"))
 	threeLetterMake="NIS";
 else if (selectedMake.equals("OLDSMOBILE"))
 	threeLetterMake="OLD";
 else if (selectedMake.equals("PLYMOUTH"))
 	threeLetterMake="PLY";
 else if (selectedMake.equals("PORSCHE"))
 	threeLetterMake="POR";
 else if (selectedMake.equals("PONTIAC"))
 	threeLetterMake="PON";
 else if (selectedMake.equals("RENAULT"))
 	threeLetterMake="REN";
 else if (selectedMake.equals("SAAB"))
 	threeLetterMake="SAB";
 else if (selectedMake.equals("SUBARU"))
 	threeLetterMake="SUB";
 else if (selectedMake.equals("TOYOTA"))
 	threeLetterMake="TOY";
 else if (selectedMake.equals("UPS"))
 	threeLetterMake="UPS";
 else if (selectedMake.equals("VOLKSWAGEN"))
 	threeLetterMake="VOL";
 else if (selectedMake.equals("HYUNDAI"))
 	threeLetterMake="HUN";
 else if (selectedMake.equals("YUGO"))
 	threeLetterMake="YUG";
 else if (selectedMake.equals("GMC TRUCK AND VAN"))
 	threeLetterMake="GMM";
 else
 	//failure case
 	;
         return threeLetterMake;
     }
     /**
      * This method is called from within the constructor to initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is always
      * regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         makeComboBox = new javax.swing.JComboBox();
         makeLabel = new javax.swing.JLabel();
         modelLabel = new javax.swing.JLabel();
         modelComboBox = new javax.swing.JComboBox();
         yearLabel = new javax.swing.JLabel();
         yearComboBox = new javax.swing.JComboBox();
         jScrollPane1 = new javax.swing.JScrollPane();
         jTextArea1 = new javax.swing.JTextArea();
         jPanel1 = new javax.swing.JPanel();
         jLabel1 = new javax.swing.JLabel();
         jComboBox1 = new javax.swing.JComboBox();
         jLabel2 = new javax.swing.JLabel();
         jLabel3 = new javax.swing.JLabel();
         jScrollPane2 = new javax.swing.JScrollPane();
         jTextArea2 = new javax.swing.JTextArea();
         jTextField2 = new javax.swing.JTextField();
         jTextField3 = new javax.swing.JTextField();
         jTextField4 = new javax.swing.JTextField();
         jLabel4 = new javax.swing.JLabel();
         jLabel5 = new javax.swing.JLabel();
         yearLabel1 = new javax.swing.JLabel();
         modelLabel1 = new javax.swing.JLabel();
         jTextField1 = new javax.swing.JTextField();
         jLabel6 = new javax.swing.JLabel();
         jLabel7 = new javax.swing.JLabel();
         jTextField5 = new javax.swing.JTextField();
         jLabel8 = new javax.swing.JLabel();
         jTextField6 = new javax.swing.JTextField();
         jLabel9 = new javax.swing.JLabel();
         jTextField7 = new javax.swing.JTextField();
         jButton1 = new javax.swing.JButton();
         jLabel10 = new javax.swing.JLabel();
         jTextField8 = new javax.swing.JTextField();
         jLabel11 = new javax.swing.JLabel();
         jLabel12 = new javax.swing.JLabel();
         jTextField9 = new javax.swing.JTextField();
         jLabel13 = new javax.swing.JLabel();
         jTextField10 = new javax.swing.JTextField();
         jLabel14 = new javax.swing.JLabel();
         jTextField11 = new javax.swing.JTextField();
         jLabel15 = new javax.swing.JLabel();
         jTextField12 = new javax.swing.JTextField();
         jLabel16 = new javax.swing.JLabel();
         jTextField13 = new javax.swing.JTextField();
         jLabel17 = new javax.swing.JLabel();
         jTextField14 = new javax.swing.JTextField();
         jLabel18 = new javax.swing.JLabel();
         jTextField15 = new javax.swing.JTextField();
         jLabel19 = new javax.swing.JLabel();
         jTextField16 = new javax.swing.JTextField();
         jLabel20 = new javax.swing.JLabel();
         jTextField17 = new javax.swing.JTextField();
         jButton2 = new javax.swing.JButton();
         jButton3 = new javax.swing.JButton();
         jButton4 = new javax.swing.JButton();
         makeComboBox1 = new javax.swing.JComboBox();
 
         setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
 
         makeComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
         makeComboBox.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 makeComboBoxActionPerformed(evt);
             }
         });
 
         makeLabel.setText("Make");
 
         modelLabel.setText("Model");
 
         modelComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
 
         yearLabel.setText("Year");
 
         yearComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
 
         jTextArea1.setColumns(20);
         jTextArea1.setRows(5);
         jScrollPane1.setViewportView(jTextArea1);
 
         jPanel1.setLayout(new java.awt.BorderLayout());
 
         jLabel1.setText("RLINK");
 
         jComboBox1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
         jComboBox1.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jComboBox1ActionPerformed(evt);
             }
         });
 
         jLabel2.setText("Car Make/Model/Year:");
 
         jLabel3.setText("Car Parts");
 
         jTextArea2.setColumns(20);
         jTextArea2.setRows(5);
         jScrollPane2.setViewportView(jTextArea2);
 
         jTextField2.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jTextField2ActionPerformed(evt);
             }
         });
 
         jLabel4.setText("New Car");
 
         jLabel5.setText("RLINK");
 
         yearLabel1.setText("Year");
 
         modelLabel1.setText("Model");
 
         jTextField1.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jTextField1ActionPerformed(evt);
             }
         });
 
         jLabel6.setText("Description");
 
         jLabel7.setText("Liter");
 
         jLabel8.setText("Engine");
 
         jLabel9.setText("Cubic");
 
         jButton1.setText("Create New Car");
         jButton1.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButton1ActionPerformed(evt);
             }
         });
 
         jLabel10.setText("New Part");
 
         jLabel11.setText("Pnum");
 
         jLabel12.setText("Core");
 
         jLabel13.setText("InHead");
 
         jLabel14.setText("OutHead");
 
         jLabel15.setText("InCon");
 
         jLabel16.setText("Oucon");
 
         jLabel17.setText("Tmount");
 
         jLabel18.setText("oilcool");
 
         jLabel19.setText("Price");
 
         jLabel20.setText("Amount");
 
         jButton2.setText("Add Part");
         jButton2.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButton2ActionPerformed(evt);
             }
         });
 
         jButton3.setText("Delete Car");
         jButton3.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButton3ActionPerformed(evt);
             }
         });
 
         jButton4.setText("Delete Part");
 
         makeComboBox1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
         makeComboBox1.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 makeComboBox1ActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(layout.createSequentialGroup()
                         .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                         .addContainerGap())
                     .addGroup(layout.createSequentialGroup()
                         .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                             .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 350, Short.MAX_VALUE)
                             .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(yearComboBox, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                             .addComponent(modelComboBox, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                             .addComponent(makeLabel, javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(modelLabel, javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(yearLabel, javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(makeComboBox, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                             .addComponent(jComboBox1, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                         .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addGroup(layout.createSequentialGroup()
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                     .addComponent(jLabel10, javax.swing.GroupLayout.Alignment.LEADING)
                                     .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                         .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                             .addComponent(jTextField12)
                                             .addComponent(jTextField11)
                                             .addComponent(jTextField10)
                                             .addComponent(jTextField9)
                                             .addComponent(jTextField8)
                                             .addComponent(jTextField2)
                                             .addComponent(jTextField4)
                                             .addComponent(jTextField3)
                                             .addComponent(jTextField7, javax.swing.GroupLayout.Alignment.TRAILING)
                                             .addComponent(makeComboBox1, javax.swing.GroupLayout.Alignment.TRAILING, 0, 156, Short.MAX_VALUE)
                                             .addGroup(layout.createSequentialGroup()
                                                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                     .addComponent(jLabel4)
                                                     .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                     .addComponent(yearLabel1)
                                                     .addComponent(modelLabel1)
                                                     .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                     .addComponent(jLabel11)
                                                     .addComponent(jLabel12)
                                                     .addComponent(jLabel13)
                                                     .addComponent(jLabel14)
                                                     .addComponent(jLabel15))
                                                 .addGap(0, 0, Short.MAX_VALUE)))
                                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                         .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                             .addComponent(jLabel20)
                                             .addComponent(jLabel19)
                                             .addComponent(jLabel18)
                                             .addComponent(jLabel17)
                                             .addComponent(jLabel16)
                                             .addComponent(jTextField1)
                                             .addComponent(jLabel6)
                                             .addComponent(jTextField5)
                                             .addComponent(jLabel7)
                                             .addComponent(jTextField6)
                                             .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                                             .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, 156, Short.MAX_VALUE)
                                             .addComponent(jTextField13)
                                             .addComponent(jTextField14)
                                             .addComponent(jTextField15)
                                             .addComponent(jTextField16)
                                             .addComponent(jTextField17)))
                                     .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 156, javax.swing.GroupLayout.PREFERRED_SIZE)))
                             .addGroup(layout.createSequentialGroup()
                                 .addGap(125, 125, 125)
                                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                     .addComponent(jButton3)
                                     .addComponent(jButton4))))
                         .addGap(10, 10, 10))))
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(makeLabel)
                     .addComponent(jLabel4))
                 .addGap(5, 5, 5)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(makeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(makeComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addGap(18, 18, 18)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(modelLabel)
                     .addComponent(modelLabel1)
                     .addComponent(jLabel6))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(modelComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(yearLabel)
                     .addComponent(yearLabel1)
                     .addComponent(jLabel7))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(yearComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel1)
                     .addComponent(jLabel5)
                     .addComponent(jLabel8))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jTextField6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(layout.createSequentialGroup()
                         .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                         .addGap(136, 136, 136))
                     .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2)
                            .addComponent(jLabel9))
                         .addGap(10, 10, 10)
                         .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                             .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 138, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addGroup(layout.createSequentialGroup()
                                 .addComponent(jTextField7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                 .addComponent(jButton3)
                                 .addGap(35, 35, 35)
                                 .addComponent(jButton4)))
                         .addGap(18, 18, Short.MAX_VALUE)))
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel3)
                     .addComponent(jLabel10))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel11)
                     .addComponent(jLabel16))
                 .addGap(5, 5, 5)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 258, Short.MAX_VALUE)
                     .addGroup(layout.createSequentialGroup()
                         .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                             .addComponent(jTextField8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addComponent(jTextField13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                             .addComponent(jLabel12)
                             .addComponent(jLabel17))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                             .addComponent(jTextField9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addComponent(jTextField14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                             .addComponent(jLabel13)
                             .addComponent(jLabel18))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                             .addComponent(jTextField10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addComponent(jTextField15, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                             .addComponent(jLabel14)
                             .addComponent(jLabel19))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                             .addComponent(jTextField11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addComponent(jTextField16, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                             .addComponent(jLabel15)
                             .addComponent(jLabel20))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                             .addComponent(jTextField12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addComponent(jTextField17, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(jButton2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                 .addContainerGap())
         );
 
         pack();
     }// </editor-fold>//GEN-END:initComponents
 
     private void makeComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_makeComboBoxActionPerformed
         // TODO add your handling code here:
     }//GEN-LAST:event_makeComboBoxActionPerformed
 
     private void jComboBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox1ActionPerformed
         // TODO add your handling code here:
     }//GEN-LAST:event_jComboBox1ActionPerformed
 
     private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
         // TODO add your handling code here:
     }//GEN-LAST:event_jButton1ActionPerformed
 
     private void jTextField2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField2ActionPerformed
         // TODO add your handling code here:
     }//GEN-LAST:event_jTextField2ActionPerformed
 
     private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField1ActionPerformed
         // TODO add your handling code here:
     }//GEN-LAST:event_jTextField1ActionPerformed
 
     private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
         // TODO add your handling code here:
     }//GEN-LAST:event_jButton3ActionPerformed
 
     private void makeComboBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_makeComboBox1ActionPerformed
         // TODO add your handling code here:
     }//GEN-LAST:event_makeComboBox1ActionPerformed
 
     private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
         // TODO add your handling code here:
     }//GEN-LAST:event_jButton2ActionPerformed
 
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
             java.util.logging.Logger.getLogger(CarUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (InstantiationException ex) {
             java.util.logging.Logger.getLogger(CarUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (IllegalAccessException ex) {
             java.util.logging.Logger.getLogger(CarUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (javax.swing.UnsupportedLookAndFeelException ex) {
             java.util.logging.Logger.getLogger(CarUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         }
         //</editor-fold>
 
         /* Create and display the form */
         java.awt.EventQueue.invokeLater(new Runnable() {
             public void run() {
                 new CarUI().setVisible(true);
             }
         });
         
         
     }
     
     public class jComboBox1 implements ActionListener{
         public void actionPerformed(ActionEvent e){
             JComboBox cb = (JComboBox)e.getSource();
             String makeName = (String)cb.getSelectedItem();
             System.out.println("pressed");
             modelComboBox.addItem("test2".toString());
         }
     }
     
     
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JButton jButton1;
     private javax.swing.JButton jButton2;
     private javax.swing.JButton jButton3;
     private javax.swing.JButton jButton4;
     private javax.swing.JComboBox jComboBox1;
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
     private javax.swing.JLabel jLabel3;
     private javax.swing.JLabel jLabel4;
     private javax.swing.JLabel jLabel5;
     private javax.swing.JLabel jLabel6;
     private javax.swing.JLabel jLabel7;
     private javax.swing.JLabel jLabel8;
     private javax.swing.JLabel jLabel9;
     private javax.swing.JPanel jPanel1;
     private javax.swing.JScrollPane jScrollPane1;
     private javax.swing.JScrollPane jScrollPane2;
     private javax.swing.JTextArea jTextArea1;
     private javax.swing.JTextArea jTextArea2;
     private javax.swing.JTextField jTextField1;
     private javax.swing.JTextField jTextField10;
     private javax.swing.JTextField jTextField11;
     private javax.swing.JTextField jTextField12;
     private javax.swing.JTextField jTextField13;
     private javax.swing.JTextField jTextField14;
     private javax.swing.JTextField jTextField15;
     private javax.swing.JTextField jTextField16;
     private javax.swing.JTextField jTextField17;
     private javax.swing.JTextField jTextField2;
     private javax.swing.JTextField jTextField3;
     private javax.swing.JTextField jTextField4;
     private javax.swing.JTextField jTextField5;
     private javax.swing.JTextField jTextField6;
     private javax.swing.JTextField jTextField7;
     private javax.swing.JTextField jTextField8;
     private javax.swing.JTextField jTextField9;
     private javax.swing.JComboBox makeComboBox;
     private javax.swing.JComboBox makeComboBox1;
     private javax.swing.JLabel makeLabel;
     private javax.swing.JComboBox modelComboBox;
     private javax.swing.JLabel modelLabel;
     private javax.swing.JLabel modelLabel1;
     private javax.swing.JComboBox yearComboBox;
     private javax.swing.JLabel yearLabel;
     private javax.swing.JLabel yearLabel1;
     // End of variables declaration//GEN-END:variables
 }
