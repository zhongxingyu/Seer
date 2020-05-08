 import java.awt.*;
 import javax.swing.*;
 import java.util.*;
 import java.awt.event.*;
 
 public class myFrame extends javax.swing.JFrame implements Observer
 {
     public static String getHTML()
     {
         return frame.HTMLTextArea.getText();
     }
     
     public static String getBBCode()
     {
         return frame.BBCodeTextArea.getText();
     }
     
     public static void setHTML(String newText)
     {
         frame.HTMLTextArea.setText(newText);
     }
     
     public static void setBBCode(String newText)
     {
         frame.BBCodeTextArea.setText(newText);
     }
 
     private static myFrame frame = new myFrame();
     
     Toolkit myKit=Toolkit.getDefaultToolkit();
     Dimension myScreenSize = myKit.getScreenSize();
     
     // Menu Bar variables
     private JMenuBar myMenuBar = new JMenuBar();
     private JMenu File = new JMenu();
     private JMenu Tools = new JMenu();
     private JMenuItem ImportHTML = new JMenuItem();
     private JMenuItem ImportBBCode = new JMenuItem();
     private JMenuItem SaveHTML = new JMenuItem();
     private JMenuItem SaveBBCode = new JMenuItem();
     private JMenuItem Exit = new JMenuItem();
     private JMenuItem Palette = new JMenuItem();
     
     public void update(Observable s, Object c)
     {   
         if (s instanceof MenuListener)
         {
             ActionEvent ae = (ActionEvent)c;
             
             if (ae.getActionCommand().compareTo(ImportHTML.getLabel()) == 0)
             {
                 FileFrame myFileChooser = new FileFrame(1);
             }
             else if (ae.getActionCommand().compareTo(ImportBBCode.getLabel()) == 0)
             {
                 FileFrame myFileChooser = new FileFrame(2);
             }
             else if (ae.getActionCommand().compareTo(SaveHTML.getLabel()) == 0)
             {
                 FileFrame myFileChooser = new FileFrame(3);
             }
             else if (ae.getActionCommand().compareTo(SaveBBCode.getLabel()) == 0)
             {
                 FileFrame myFileChooser = new FileFrame(4);
             }
             else if (ae.getActionCommand().compareTo(Exit.getLabel()) == 0)
             {
                 System.exit(0);
             }
             else if (ae.getActionCommand().compareTo(Palette.getLabel()) == 0)
             {
                 myPalette newPalette = new myPalette();
             }
         }
         /*else if (s instanceof KeyboardListener)
         {
             TO DO: KeyboardListener
         }*/
             
     }
     
     public static myFrame getFrame()
     {
         return frame;
     }
     
     private void initMenu()
     {	
         myMenuBar.add(File);
         myMenuBar.add(Tools);
         
         File.add(ImportHTML);
         File.add(ImportBBCode);
         File.add(SaveHTML);
         File.add(SaveBBCode);
         File.add(Exit);
 
         Tools.add(Palette);
         
         File.setText("File");
         Tools.setText("Tools");
   
         ImportHTML.setText("Import HTML..");
         ImportBBCode.setText("Import BBCode..");
         SaveHTML.setText("Save HTML");
         SaveBBCode.setText("Save BBCode");
         Exit.setText("Exit");
 
         Palette.setText("Palette");
         
         MenuListener myMenuListener = new MenuListener(this);
         
         ImportHTML.addActionListener(myMenuListener);
         ImportBBCode.addActionListener(myMenuListener);
         SaveHTML.addActionListener(myMenuListener);
         SaveBBCode.addActionListener(myMenuListener);
         Exit.addActionListener(myMenuListener);
 
         Palette.addActionListener(myMenuListener);
     }
     
     private myFrame()
     {
         initComponents();
         initMenu();
         this.setDefaultCloseOperation(EXIT_ON_CLOSE);
         this.setResizable(false);
         this.setTitle("HTML-BBCode converter");
         this.setSize(myScreenSize.width/2, myScreenSize.height/2);
         this.setLocation(myScreenSize.width/4, myScreenSize.height/4);
         this.setJMenuBar(myMenuBar);
         this.setVisible(true);
     }
 
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         HTMLPanel = new javax.swing.JPanel();
         convert1 = new javax.swing.JButton();
         check1 = new javax.swing.JButton();
         HTMLLabel = new javax.swing.JLabel();
         jScrollPane1 = new javax.swing.JScrollPane();
         HTMLTextArea = new javax.swing.JTextArea();
         clear1 = new javax.swing.JButton();
         BBCodePanel = new javax.swing.JPanel();
         convert2 = new javax.swing.JButton();
         check2 = new javax.swing.JButton();
         BBCodeLabel = new javax.swing.JLabel();
         jScrollPane2 = new javax.swing.JScrollPane();
         BBCodeTextArea = new javax.swing.JTextArea();
         Clear1 = new javax.swing.JButton();
         clear2 = new javax.swing.JButton();
 
         setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
 
         convert1.setText("Convert");
         convert1.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 convert1ActionPerformed(evt);
             }
         });
 
         check1.setText("Check");
         check1.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 check1ActionPerformed(evt);
             }
         });
 
         HTMLLabel.setText("HTML");
 
         HTMLTextArea.setColumns(20);
         HTMLTextArea.setFont(new java.awt.Font("Comic Sans MS", 0, 13)); // NOI18N
         HTMLTextArea.setRows(5);
         jScrollPane1.setViewportView(HTMLTextArea);
 
         clear1.setText("Clear");
         clear1.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 clear1ActionPerformed(evt);
             }
         });
 
         org.jdesktop.layout.GroupLayout HTMLPanelLayout = new org.jdesktop.layout.GroupLayout(HTMLPanel);
         HTMLPanel.setLayout(HTMLPanelLayout);
         HTMLPanelLayout.setHorizontalGroup(
             HTMLPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(HTMLPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .add(HTMLPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                     .add(HTMLPanelLayout.createSequentialGroup()
                         .add(6, 6, 6)
                         .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 253, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                         .addContainerGap())
                     .add(HTMLPanelLayout.createSequentialGroup()
                         .add(check1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                         .add(convert1)
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                         .add(clear1)
                         .add(157, 157, 157))))
             .add(HTMLPanelLayout.createSequentialGroup()
                 .add(126, 126, 126)
                 .add(HTMLLabel)
                 .add(0, 0, Short.MAX_VALUE))
         );
         HTMLPanelLayout.setVerticalGroup(
             HTMLPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(HTMLPanelLayout.createSequentialGroup()
                 .add(HTMLLabel)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(HTMLPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                     .add(check1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .add(clear1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .add(convert1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 247, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .add(34, 34, 34))
         );
 
         convert2.setText("Convert");
         convert2.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 convert2ActionPerformed(evt);
             }
         });
 
         check2.setText("Check");
         check2.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 check2ActionPerformed(evt);
             }
         });
 
         BBCodeLabel.setText("BBCode");
 
         BBCodeTextArea.setColumns(20);
         BBCodeTextArea.setFont(new java.awt.Font("Comic Sans MS", 0, 13)); // NOI18N
         BBCodeTextArea.setRows(5);
         jScrollPane2.setViewportView(BBCodeTextArea);
 
         Clear1.setText("Clear");
 
         clear2.setText("Clear");
         clear2.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 clear2ActionPerformed(evt);
             }
         });
 
         org.jdesktop.layout.GroupLayout BBCodePanelLayout = new org.jdesktop.layout.GroupLayout(BBCodePanel);
         BBCodePanel.setLayout(BBCodePanelLayout);
         BBCodePanelLayout.setHorizontalGroup(
             BBCodePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(BBCodePanelLayout.createSequentialGroup()
                 .add(BBCodePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                     .add(BBCodePanelLayout.createSequentialGroup()
                         .add(112, 112, 112)
                         .add(BBCodeLabel)
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                     .add(org.jdesktop.layout.GroupLayout.TRAILING, BBCodePanelLayout.createSequentialGroup()
                         .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                         .add(check2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 77, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                         .add(convert2)
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                         .add(clear2)
                         .add(175, 175, 175)))
                 .add(Clear1))
             .add(BBCodePanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .add(jScrollPane2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 252, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap())
         );
         BBCodePanelLayout.setVerticalGroup(
             BBCodePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(BBCodePanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .add(Clear1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .add(341, 341, 341))
             .add(org.jdesktop.layout.GroupLayout.TRAILING, BBCodePanelLayout.createSequentialGroup()
                 .add(BBCodeLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 17, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(BBCodePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                     .add(check2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 29, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                     .add(convert2)
                     .add(clear2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(jScrollPane2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 246, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .add(72, 72, 72))
         );
 
         org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(layout.createSequentialGroup()
                 .add(12, 12, 12)
                 .add(HTMLPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 287, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(BBCodePanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 280, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(332, Short.MAX_VALUE))
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(layout.createSequentialGroup()
                 .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                     .add(layout.createSequentialGroup()
                         .addContainerGap()
                         .add(BBCodePanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 326, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                     .add(layout.createSequentialGroup()
                         .add(27, 27, 27)
                         .add(HTMLPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                 .addContainerGap(56, Short.MAX_VALUE))
         );
 
         pack();
     }// </editor-fold>//GEN-END:initComponents
 
     public Context ContextHandler = Context.getContextHandler();
     
     private void check1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_check1ActionPerformed
         ContextHandler.getHTML().check(HTMLTextArea.getText());
     }//GEN-LAST:event_check1ActionPerformed
 
     private void convert2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_convert2ActionPerformed
         HTMLTextArea.setText(ContextHandler.getBBCode().convert(BBCodeTextArea.getText()));
     }//GEN-LAST:event_convert2ActionPerformed
 
     private void check2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_check2ActionPerformed
         ContextHandler.getBBCode().check(BBCodeTextArea.getText());
     }//GEN-LAST:event_check2ActionPerformed
 
     private void convert1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_convert1ActionPerformed
         BBCodeTextArea.setText(ContextHandler.getHTML().convert(HTMLTextArea.getText()));
     }//GEN-LAST:event_convert1ActionPerformed
 
     private void clear1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clear1ActionPerformed
         HTMLTextArea.setText("");
     }//GEN-LAST:event_clear1ActionPerformed
 
     private void clear2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clear2ActionPerformed
         BBCodeTextArea.setText("");
     }//GEN-LAST:event_clear2ActionPerformed
 
     public static void main(String args[]) {
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
             java.util.logging.Logger.getLogger(myFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (InstantiationException ex) {
             java.util.logging.Logger.getLogger(myFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (IllegalAccessException ex) {
             java.util.logging.Logger.getLogger(myFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (javax.swing.UnsupportedLookAndFeelException ex) {
             java.util.logging.Logger.getLogger(myFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         }
         //</editor-fold>
         java.awt.EventQueue.invokeLater(new Runnable() 
         {
 
             public void run()
             {
                 getFrame();
             }
         });
     }
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JLabel BBCodeLabel;
     private javax.swing.JPanel BBCodePanel;
     private javax.swing.JTextArea BBCodeTextArea;
     private javax.swing.JButton Clear1;
     private javax.swing.JLabel HTMLLabel;
     private javax.swing.JPanel HTMLPanel;
     private javax.swing.JTextArea HTMLTextArea;
     private javax.swing.JButton check1;
     private javax.swing.JButton check2;
     private javax.swing.JButton clear1;
     private javax.swing.JButton clear2;
     private javax.swing.JButton convert1;
     private javax.swing.JButton convert2;
     private javax.swing.JScrollPane jScrollPane1;
     private javax.swing.JScrollPane jScrollPane2;
     // End of variables declaration//GEN-END:variables
 }
