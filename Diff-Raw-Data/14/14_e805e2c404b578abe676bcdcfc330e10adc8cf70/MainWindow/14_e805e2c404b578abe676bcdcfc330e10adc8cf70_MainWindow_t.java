 package od.games.bitwar.login;
 
 import javax.swing.*;
 
 public class MainWindow extends javax.swing.JFrame {
 
     private javax.swing.JButton jButton1;
     private javax.swing.JButton jButton2;
     private javax.swing.JLabel jLabel1;
     private javax.swing.JLabel jLabel2;
     private javax.swing.JLabel jLabel3;
     private javax.swing.JLayeredPane jLayeredPane1;
     private javax.swing.JPanel jPanel1;
     private javax.swing.JPasswordField jPasswordField1;
     private javax.swing.JTextField jTextField1;
     private OptionsDialog opt;
     private String table, host, port, database, username, password;
     private Database db;
 
     public MainWindow() {
         initComponents();
         this.setTitle("BitWar Launcher");
         opt = new OptionsDialog(this, true);
         this.setLocationRelativeTo(null);
     }
 
     private void initComponents() {
         db = new Database(host, port, database, username, password);
         db.openDatabaseConnection();
         jLayeredPane1 = new javax.swing.JLayeredPane();
         jPanel1 = new javax.swing.JPanel();
         jTextField1 = new javax.swing.JTextField();
         jPasswordField1 = new javax.swing.JPasswordField();
         jLabel1 = new javax.swing.JLabel();
         jLabel2 = new javax.swing.JLabel();
         jButton1 = new javax.swing.JButton();
         jButton2 = new javax.swing.JButton();
         jLabel3 = new javax.swing.JLabel();
 
         setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
         setBounds(new java.awt.Rectangle(0, 0, 1277, 608));
         setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
         setMinimumSize(new java.awt.Dimension(1277, 608));
         setResizable(false);
 
         jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());
 
         jLabel1.setText("Username:");
 
         jLabel2.setText("Password:");
 
         jButton1.setText("Login");
         jButton1.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButton1ActionPerformed(evt);
             }
         });
 
         jButton2.setText("Options");
         jButton2.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButton2ActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
         jPanel1.setLayout(jPanel1Layout);
         jPanel1Layout.setHorizontalGroup(
                 jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                         .addGroup(jPanel1Layout.createSequentialGroup()
                                 .addContainerGap()
                                 .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                         .addComponent(jLabel1)
                                         .addComponent(jLabel2))
                                 .addGap(18, 18, 18)
                                 .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                         .addComponent(jPasswordField1)
                                         .addComponent(jTextField1, javax.swing.GroupLayout.DEFAULT_SIZE, 113, Short.MAX_VALUE))
                                 .addGap(18, 18, 18)
                                 .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                         .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                         .addComponent(jButton2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
         jPanel1Layout.setVerticalGroup(
                 jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                         .addGroup(jPanel1Layout.createSequentialGroup()
                                 .addContainerGap()
                                 .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                         .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                         .addComponent(jLabel1)
                                         .addComponent(jButton1))
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                 .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                         .addComponent(jPasswordField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                         .addComponent(jLabel2)
                                         .addComponent(jButton2))
                                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
         jPanel1.setBounds(960, 500, 294, 83);
         jLayeredPane1.add(jPanel1, javax.swing.JLayeredPane.DEFAULT_LAYER);
 
         jLabel3.setIcon(new javax.swing.ImageIcon(getClass().getClassLoader().getResource("background.png")));
         jLabel3.setBounds(0, 0, 1280, 610);
         jLayeredPane1.add(jLabel3, javax.swing.JLayeredPane.DEFAULT_LAYER);
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
                 layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                         .addComponent(jLayeredPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 1277, Short.MAX_VALUE)
         );
         layout.setVerticalGroup(
                 layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                         .addComponent(jLayeredPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 608, Short.MAX_VALUE)
         );
 
         pack();
     }// </editor-fold>
 
     private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {
         opt.setVisible(true);
     }
 
     private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {
         if (db.validateGameLogin(table, jTextField1.getText(), jPasswordField1.getPassword())) {
             JOptionPane.showMessageDialog(null, "login sucessfull!");
         } else {
             JOptionPane.showMessageDialog(null, "login failed!");
         }
     }
 
     public static void main(String args[]) {
         try {
             for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                 if ("Nimbus".equals(info.getName())) {
                     javax.swing.UIManager.setLookAndFeel(info.getClassName());
                     break;
                 }
             }
         } catch (ClassNotFoundException ex) {
             java.util.logging.Logger.getLogger(MainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (InstantiationException ex) {
             java.util.logging.Logger.getLogger(MainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (IllegalAccessException ex) {
             java.util.logging.Logger.getLogger(MainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (javax.swing.UnsupportedLookAndFeelException ex) {
             java.util.logging.Logger.getLogger(MainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         }
 
         java.awt.EventQueue.invokeLater(new Runnable() {
 
             public void run() {
                 new MainWindow().setVisible(true);
             }
         });
     }
 
 }
