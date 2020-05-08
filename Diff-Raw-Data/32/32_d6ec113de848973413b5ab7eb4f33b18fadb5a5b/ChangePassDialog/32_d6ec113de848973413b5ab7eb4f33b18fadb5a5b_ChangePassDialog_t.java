 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package client;
 
 import java.awt.Color;
 import java.awt.FlowLayout;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.Arrays;
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JPasswordField;
 
 /**
  *
  * @author radim
  */
 public class ChangePassDialog extends JDialog {
 
     JPasswordField current, newpass, againpass;
     JButton change, cancel;
     JLabel warn;
    int userId;
 
    public ChangePassDialog(int id) {
        userId = id;
         setTitle("Change password");
         setModalityType(ModalityType.APPLICATION_MODAL);
         current = new JPasswordField(20);
         newpass = new JPasswordField(20);
         againpass = new JPasswordField(20);
         change = new JButton("Change password");
         cancel = new JButton("Cancel");
         warn = new JLabel();
         warn.setForeground(Color.red);
         setLayout(new GridLayout(5, 1));
         add(new JPanel(new FlowLayout()) {
             // this is Instance Initializer, which gets invoked upon object creation - think of it as an unnamed constructor
 
             {
                 add(warn);
             }
         });
         add(new JPanel(new FlowLayout()) {
 
             { //current password row
                 add(new JLabel("Current password"));
                 add(current);
             }
         });
         add(new JPanel(new FlowLayout()) {
 
             { //new password row
                 add(new JLabel("New password"));
                 add(newpass);
             }
         });
         add(new JPanel(new FlowLayout()) {
 
             { //new password confirmation row
                 add(new JLabel("New password again"));
                 add(againpass);
             }
         });
         add(new JPanel(new FlowLayout()) {
 
             {
                 add(change);
                 add(cancel);
             }
         });
 
 
         change.addActionListener(new ActionListener() {
 
             @Override
             public void actionPerformed(ActionEvent e) {
                 warn.setText("");
                 if (!Arrays.equals(newpass.getPassword(), againpass.getPassword())) {
                     warn.setText("Please type new password again for confirmation."); // TODO words!
                     return;
                 }
                 ServerConnection sc = ServerConnection.getInstance();
                String ret = sc.sendMSG("CHANGE_PASS " + Integer.toString(userId) + " " + new String(current.getPassword()) + " " + new String(newpass.getPassword()));
                 if (ret == null) {
                     warn.setText("Error changing password.");
                 } else if (ret.startsWith("KO")) {
                     warn.setText(ret);
                } else {
                    setVisible(false);
                    dispose();
                 }
             }
         });
         cancel.addActionListener(new ActionListener() {
 
             @Override
             public void actionPerformed(ActionEvent e) {
                 setVisible(false);
                 dispose();
             }
         });
         pack();
         setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
     }
 
     public static void main(String[] args) {
        ServerConnection.getInstance().connect("666", "admin");
        JDialog j = new ChangePassDialog(666);
         j.setVisible(true);
     }
 }
