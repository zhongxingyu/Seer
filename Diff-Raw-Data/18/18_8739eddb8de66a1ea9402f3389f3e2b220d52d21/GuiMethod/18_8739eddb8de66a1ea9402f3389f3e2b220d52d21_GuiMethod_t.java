 package iface;
 
 import methods.*;
 
 import javax.swing.*;
 
 public class GuiMethod extends javax.swing.JDialog {
     private javax.swing.JPanel contentPane;
     private javax.swing.JButton buttonOK;
     private JTextField a0TextField;
     private JComboBox comboBox1;
     private JTextField a0TextField1;
     private JTextField a0TextField2;
     private JTextField a0TextField3;
     private JTextField a0TextField4;
     private JTextField a0TextField5;
     private JTextArea textArea1;
     private JTextField a0TextField6;
 
     public GuiMethod() {
         setContentPane(contentPane);
         setModal(true);
         getRootPane().setDefaultButton(buttonOK);
         buttonOK.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent e) {
                 onOK();
             }
         });
 
 // call onCancel() when cross is clicked
         setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
         addWindowListener(new java.awt.event.WindowAdapter() {
             public void windowClosing(java.awt.event.WindowEvent e) {
                 onCancel();
             }
         });
 
 // call onCancel() on ESCAPE
         contentPane.registerKeyboardAction(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent e) {
                 onCancel();
             }
         }, javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0), javax.swing.JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
     }
 
     private void onOK() {
     /*  M/M/1   = 0
         M/M/S   = 1
         M/G/1   = 2
         M/D/1   = 3
         M/D/S   = 4
         M/E/K/1 = 5
         M/E/K/S = 6
         */
         String result="";
         switch(comboBox1.getSelectedIndex()){
             case 0:
                 Mms mms=new Mms();
                 mms.setValues(Float.valueOf(a0TextField.getText()),Float.valueOf(a0TextField1.getText()),Float.valueOf(a0TextField2.getText()),Float.valueOf(a0TextField3.getText()));
                 result+="Po= "+mms.getPo()+"\n";
                 result+="Pn= "+mms.getPn()+"\n";
                 result+="W= "+mms.getW()+"\n";
                 result+="Wq= "+mms.getWq()+"\n";
                 result+="L= "+mms.getL()+"\n";
                 result+="Lq= "+mms.getLq()+"\n";
                 break;
             case 1:
                 Mm1 mm1=new Mm1();
                mm1.setValues(Float.valueOf(a0TextField.getText()),Float.valueOf(a0TextField1.getText()),Float.valueOf(a0TextField2.getText()));
                 result+="Po= "+mm1.getPo()+"\n";
                 result+="Pn= "+mm1.getPn()+"\n";
                 result+="W= "+mm1.getW()+"\n";
                 result+="Wq= "+mm1.getWq()+"\n";
                 result+="L= "+mm1.getL()+"\n";
                 result+="Lq= "+mm1.getLq()+"\n";
                 break;
             case 2:
                 Mg1 mg1=new Mg1();
                 mg1.setValues(Float.valueOf(a0TextField.getText()),Float.valueOf(a0TextField1.getText()),Float.valueOf(a0TextField6.getText()));
                 result+="Po= "+mg1.getPo()+"\n";
                 result+="W= "+mg1.getW()+"\n";
                 result+="Wq= "+mg1.getWq()+"\n";
                 result+="L= "+mg1.getL()+"\n";
                 result+="Lq= "+mg1.getLq()+"\n";
                 result+="Media Servicio= "+mg1.getMeanService()+"\n";
                 break;
             case 3:
                 Md1 md1=new Md1();
                 md1.setValues(Float.valueOf(a0TextField.getText()),Float.valueOf(a0TextField1.getText()));
                 result+="Po= "+md1.getPo()+"\n";
                 result+="W= "+md1.getW()+"\n";
                 result+="Wq= "+md1.getWq()+"\n";
                 result+="L= "+md1.getL()+"\n";
                 result+="Lq= "+md1.getLq()+"\n";
                 break;
             case 4:
                 Mds mds=new Mds();
                 mds.setValues(Float.valueOf(a0TextField.getText()),Float.valueOf(a0TextField1.getText()));
                 result+="Pn= "+mds.getPn()+"\n";
                 result+="W= "+mds.getW()+"\n";
                 result+="Wq= "+mds.getWq()+"\n";
                 result+="L= "+mds.getL()+"\n";
                 result+="Lq= "+mds.getLq()+"\n";
                 break;
             case 5:
                 Mek1 mek1=new Mek1();
                 mek1.setValues(Float.valueOf(a0TextField4.getText()),Float.valueOf(a0TextField.getText()),Float.valueOf(a0TextField1.getText()));
                 result+="Po= "+mek1.getPo()+"\n";
                 result+="W= "+mek1.getW()+"\n";
                 result+="Wq= "+mek1.getWq()+"\n";
                 result+="L= "+mek1.getL()+"\n";
                 result+="Lq= "+mek1.getLq()+"\n";
                 break;
             case 6:
                 Meks meks = new Meks();
                 meks.setValues(Float.valueOf(a0TextField.getText()),Float.valueOf(a0TextField1.getText()),Float.valueOf(a0TextField4.getText()));
                 result+="W= "+meks.getW()+"\n";
                 result+="Wq= "+meks.getWq()+"\n";
                 result+="L= "+meks.getL()+"\n";
                 result+="Lq= "+meks.getLq()+"\n";
                 break;
         }
         textArea1.setText(result);
     }
 
     private void onCancel() {
 // add your code here if necessary
         dispose();
     }
 
 }
