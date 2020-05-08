
import javax.swing.LookAndFeel;
import javax.swing.UIManager;

 
 public class Main extends javax.swing.JFrame {
 

     public Main() {
         initComponents();
         ArrowButton.setUI(new VerticalButtonUI(270));
         OpenButton.setUI(new VerticalButtonUI(270));
         SaveButton.setUI(new VerticalButtonUI(270));
         CloseButton.setUI(new VerticalButtonUI(270));
     }
 

     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         margin = new javax.swing.JPanel();
         ArrowButton = new javax.swing.JButton();
         jLabel1 = new javax.swing.JLabel();
         OpenButton = new javax.swing.JButton();
         SaveButton = new javax.swing.JButton();
         CloseButton = new javax.swing.JButton();
         page = new javax.swing.JPanel();
         miniMap = new javax.swing.JPanel();
         toolBar = new javax.swing.JPanel();
         PenButton = new javax.swing.JButton();
         SelectButton = new javax.swing.JButton();
         InsertButton = new javax.swing.JButton();
         DeleteButton = new javax.swing.JButton();
         writingArea = new javax.swing.JPanel();
         jScrollPane1 = new javax.swing.JScrollPane();
 
         setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
         setBackground(new java.awt.Color(153, 153, 153));
         setMinimumSize(new java.awt.Dimension(768, 1024));
         setResizable(false);
         getContentPane().setLayout(null);
 
         margin.setBackground(new java.awt.Color(255, 204, 255));
         margin.setMaximumSize(new java.awt.Dimension(58, 717));
         margin.setMinimumSize(new java.awt.Dimension(58, 717));
         margin.setPreferredSize(new java.awt.Dimension(58, 717));
         margin.setLayout(new javax.swing.BoxLayout(margin, javax.swing.BoxLayout.Y_AXIS));
 
         ArrowButton.setText("^");
         ArrowButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 ArrowButtonActionPerformed(evt);
             }
         });
         margin.add(ArrowButton);
 
         jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
         jLabel1.setText("Menu");
         margin.add(jLabel1);
 
         OpenButton.setText("Open");
         OpenButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 OpenButtonActionPerformed(evt);
             }
         });
         margin.add(OpenButton);
 
         SaveButton.setText("Save");
         SaveButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 SaveButtonActionPerformed(evt);
             }
         });
         margin.add(SaveButton);
 
         CloseButton.setText("Close");
         CloseButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 CloseButtonActionPerformed(evt);
             }
         });
         margin.add(CloseButton);
 
         getContentPane().add(margin);
         margin.setBounds(0, 0, 58, 717);
 
         page.setBackground(new java.awt.Color(255, 255, 204));
         page.setMinimumSize(new java.awt.Dimension(556, 717));
         page.setPreferredSize(new java.awt.Dimension(556, 717));
         page.setLayout(null);
         getContentPane().add(page);
         page.setBounds(58, 0, 556, 717);
 
         miniMap.setBackground(new java.awt.Color(255, 204, 204));
         miniMap.setMaximumSize(new java.awt.Dimension(154, 717));
         miniMap.setMinimumSize(new java.awt.Dimension(154, 717));
         miniMap.setPreferredSize(new java.awt.Dimension(154, 717));
         miniMap.setLayout(null);
         getContentPane().add(miniMap);
         miniMap.setBounds(614, 0, 154, 717);
 
         toolBar.setBackground(new java.awt.Color(204, 255, 204));
         toolBar.setMaximumSize(new java.awt.Dimension(768, 51));
         toolBar.setMinimumSize(new java.awt.Dimension(768, 51));
         toolBar.setPreferredSize(new java.awt.Dimension(768, 51));
         toolBar.setLayout(new javax.swing.BoxLayout(toolBar, javax.swing.BoxLayout.LINE_AXIS));
 
         PenButton.setText("Pen");
         PenButton.setFocusable(false);
         PenButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         PenButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         PenButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 PenButtonActionPerformed(evt);
             }
         });
         toolBar.add(PenButton);
 
         SelectButton.setText("Select");
         SelectButton.setFocusable(false);
         SelectButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         SelectButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         SelectButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 SelectButtonActionPerformed(evt);
             }
         });
         toolBar.add(SelectButton);
 
         InsertButton.setText("Insert");
         InsertButton.setFocusable(false);
         InsertButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         InsertButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         InsertButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 InsertButtonActionPerformed(evt);
             }
         });
         toolBar.add(InsertButton);
 
         DeleteButton.setText("Delete");
         DeleteButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 DeleteButtonActionPerformed(evt);
             }
         });
         toolBar.add(DeleteButton);
 
         getContentPane().add(toolBar);
         toolBar.setBounds(0, 718, 768, 51);
 
         writingArea.setBackground(new java.awt.Color(204, 204, 255));
         writingArea.setMaximumSize(new java.awt.Dimension(768, 256));
         writingArea.setMinimumSize(new java.awt.Dimension(768, 256));
         writingArea.setPreferredSize(new java.awt.Dimension(768, 256));
         writingArea.setLayout(new java.awt.BorderLayout());
         writingArea.add(jScrollPane1, java.awt.BorderLayout.CENTER);
 
         getContentPane().add(writingArea);
         writingArea.setBounds(0, 768, 768, 256);
 
         pack();
     }// </editor-fold>//GEN-END:initComponents
 
     private void ArrowButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ArrowButtonActionPerformed
         System.out.println("Arrow Button Pressed!");
     }//GEN-LAST:event_ArrowButtonActionPerformed
 
     private void OpenButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_OpenButtonActionPerformed
         System.out.println("Open Button Pressed!");
     }//GEN-LAST:event_OpenButtonActionPerformed
 
     private void SaveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SaveButtonActionPerformed
         System.out.println("Save Button Pressed!");
     }//GEN-LAST:event_SaveButtonActionPerformed
 
     private void CloseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CloseButtonActionPerformed
         System.out.println("Close Button Pressed!");
     }//GEN-LAST:event_CloseButtonActionPerformed
 
     private void PenButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_PenButtonActionPerformed
         System.out.println("Pen Selected!");
     }//GEN-LAST:event_PenButtonActionPerformed
 
     private void SelectButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SelectButtonActionPerformed
         System.out.println("Select Button Selected!");
     }//GEN-LAST:event_SelectButtonActionPerformed
 
     private void InsertButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_InsertButtonActionPerformed
         System.out.println("Insert Button Selected!");
     }//GEN-LAST:event_InsertButtonActionPerformed
 
     private void DeleteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DeleteButtonActionPerformed
         System.out.println("Delete Button Selected!");
     }//GEN-LAST:event_DeleteButtonActionPerformed
 
     /**
    * @param args the command line arguments
    */
     public static void main(String args[]) {
        
         java.awt.EventQueue.invokeLater(new Runnable() {
             public void run() {
                 new Main().setVisible(true);
             }
         });
     }

     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JButton ArrowButton;
     private javax.swing.JButton CloseButton;
     private javax.swing.JButton DeleteButton;
     private javax.swing.JButton InsertButton;
     private javax.swing.JButton OpenButton;
     private javax.swing.JButton PenButton;
     private javax.swing.JButton SaveButton;
     private javax.swing.JButton SelectButton;
     private javax.swing.JLabel jLabel1;
     private javax.swing.JScrollPane jScrollPane1;
     private javax.swing.JPanel margin;
     private javax.swing.JPanel miniMap;
     private javax.swing.JPanel page;
     private javax.swing.JPanel toolBar;
     private javax.swing.JPanel writingArea;
     // End of variables declaration//GEN-END:variables

 }
