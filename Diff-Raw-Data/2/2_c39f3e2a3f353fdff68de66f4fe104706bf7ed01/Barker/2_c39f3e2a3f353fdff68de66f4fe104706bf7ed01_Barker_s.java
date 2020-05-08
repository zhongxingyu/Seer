 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package barker;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import javax.swing.ImageIcon;
 import javax.swing.UIManager;
 
 /**
  *
  * @author Colin Mackey
  * @author David Justis
  */
 public class Barker extends javax.swing.JFrame {
 
     public Barker() {
         initComponents();
     }
     static connect connection = new connect();
 
     private void initComponents() {
 
         jScrollPane1 = new javax.swing.JScrollPane();
         jTextField2 = new javax.swing.JTextField();
         jScrollBar1 = new javax.swing.JScrollBar();
         jTextField3 = new javax.swing.JTextField();
         jMenuBar1 = new javax.swing.JMenuBar();
         jMenu1 = new javax.swing.JMenu();
         jMenu2 = new javax.swing.JMenu();
 
         try {
             setIconImage(new ImageIcon(
                     new URL(
                     "http://www.mricons.com/store/png/116862_34349_24_paw_icon.png"))
                     .getImage());
         } catch (MalformedURLException e) {
             e.printStackTrace();
         }
 
         setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
 
         jTextField2.setText("jTextField2");
 
         jTextField3.setText("jTextField3");
 
         jMenuBar1.setBackground(new java.awt.Color(153, 204, 0));
         jMenuBar1.setAutoscrolls(true);
         jMenuBar1.setCursor(new java.awt.Cursor(java.awt.Cursor.SW_RESIZE_CURSOR));
 
         jMenu1.setText("File");
         jMenuBar1.add(jMenu1);
 
         jMenu2.setText("Edit");
         jMenuBar1.add(jMenu2);
 
         setJMenuBar(jMenuBar1);
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
                 layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                 .addGroup(layout.createSequentialGroup()
                 .addComponent(jScrollBar1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(99, 99, 99)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                 .addComponent(jTextField2, javax.swing.GroupLayout.DEFAULT_SIZE, 284, Short.MAX_VALUE)
                 .addComponent(jTextField3))));
         layout.setVerticalGroup(
                 layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                 .addGroup(layout.createSequentialGroup()
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                 .addGroup(layout.createSequentialGroup()
                 .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, 129, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addComponent(jTextField3))
                 .addComponent(jScrollBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 312, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addGap(0, 0, Short.MAX_VALUE)));
 
         pack();
     }// </editor-fold>
 
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) {
         connection.startConnection(
                 "root", "forDemacia!");
         // TODO code application logic here
 
 
         // for the login, need only username
         // 
         String password = connection.getPassword("duffbuster");
         System.out.println(password);
         
         
         //connection.addFriend("duffbuster", "bob");
         /**String[] friends = connection.getFriends("duffbuster");
         for (int i = 0; i < friends.length; i++) {
             if (friends[i] != null) {
                 System.out.println(friends[i]);
             }
 
         }*/
         //connection.sendBark("djustis", "This is a #second test #bark @duffbuster");
         //connection.changePassword("duffbuster", "hello", "goodbye");
         // connection.newUser("bob", "crazy");
        connection.getFriendsLastBarks("duffbuster");
         //System.out.println(barks[1][0]);
         
         
 
 
         /* Set the System look and feel */
         //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
          /* For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
          */
         try {
             UIManager.setLookAndFeel(
                     UIManager.getSystemLookAndFeelClassName());
         } catch (ClassNotFoundException ex) {
             java.util.logging.Logger.getLogger(NewJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (InstantiationException ex) {
             java.util.logging.Logger.getLogger(NewJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (IllegalAccessException ex) {
             java.util.logging.Logger.getLogger(NewJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (javax.swing.UnsupportedLookAndFeelException ex) {
             java.util.logging.Logger.getLogger(NewJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         }
         //</editor-fold>
 
         /* Create and display the form */
         java.awt.EventQueue.invokeLater(
                 new Runnable() {
                     public void run() {
                         new NewJFrame().setVisible(true);
                     }
                 });
     }
     // Variables declaration - do not modify
     private javax.swing.JMenu jMenu1;
     private javax.swing.JMenu jMenu2;
     private javax.swing.JMenuBar jMenuBar1;
     private javax.swing.JScrollBar jScrollBar1;
     private javax.swing.JScrollPane jScrollPane1;
     private javax.swing.JTextField jTextField2;
     private javax.swing.JTextField jTextField3;
     // End of variables declaration
 }
