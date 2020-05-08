 /*
  * JMBS: Java Micro Blogging System
  *
  * Copyright (C) 2012  
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY.
  * See the GNU General Public License for more details.
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  * @author Younes CHEIKH http://cyounes.com
  * @author Benjamin Babic http://bbabic.com
  * 
  */
 package jmbs.client.Graphics;
 
 import javax.swing.BorderFactory;
 import javax.swing.JPanel;
 import javax.swing.JLabel;
 import javax.swing.JSeparator;
 import javax.swing.SwingConstants;
 import java.awt.Font;
 import java.awt.Color;
 import javax.swing.JTextField;
 import javax.swing.JPasswordField;
 import javax.swing.JButton;
 
 import jmbs.client.CurrentUser;
 import jmbs.client.HashPassword;
 import jmbs.client.RemoteRequests;
 import jmbs.common.User;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import java.util.HashMap;
 import java.util.regex.Pattern;
 
 public class ProfilePanel extends JPanel {
 
     /**
      *
      */
     private static final long serialVersionUID = -398362580268810333L;
     private JTextField nameTextField;
     private JTextField fnameTextField;
     private JTextField emailTextField;
     private JPasswordField passwordField;
     private JPasswordField newpasswordField;
     private JPasswordField confirmpasswordField;
     private JTextField textField;
 
     /**
      * Create
      * the
      * panel.
      */
     public ProfilePanel(User currentUser) {
 
         JLabel lblName = new JLabel("Name:");
         lblName.setBounds(9, 46, 40, 16);
 
         JLabel lblLastName = new JLabel("Last Name:");
         lblLastName.setBounds(9, 86, 70, 16);
 
         JLabel lblEmailAdress = new JLabel("Email Adress:");
         lblEmailAdress.setBounds(9, 126, 85, 16);
 
         JSeparator separator = new JSeparator();
         separator.setBounds(9, 154, 267, 12);
 
         JLabel lblPublicInformations = new JLabel("Public Informations");
         lblPublicInformations.setBounds(84, 8, 162, 20);
         lblPublicInformations.setHorizontalAlignment(SwingConstants.CENTER);
         lblPublicInformations.setFont(new Font("Lucida Grande", Font.BOLD, 16));
 
         JLabel lblChangePassword = new JLabel("Change Password");
         lblChangePassword.setBounds(82, 172, 163, 20);
         lblChangePassword.setForeground(Color.RED);
         lblChangePassword.setHorizontalAlignment(SwingConstants.CENTER);
         lblChangePassword.setFont(new Font("Lucida Grande", Font.BOLD, 16));
 
         JLabel lblOldPassword = new JLabel("Old Password:");
         lblOldPassword.setBounds(6, 204, 89, 16);
 
         JLabel lblNewPassword = new JLabel("New Password:");
         lblNewPassword.setBounds(6, 238, 94, 16);
 
         JLabel lblConfirmPassword = new JLabel("Confirm Password:");
         lblConfirmPassword.setBounds(9, 272, 118, 16);
 
         nameTextField = new JTextField(currentUser.getName());
         nameTextField.setBorder(BorderFactory.createLineBorder(null));
         nameTextField.setBounds(142, 40, 180, 28);
         nameTextField.setColumns(10);
 
         fnameTextField = new JTextField(currentUser.getFname());
         fnameTextField.setBounds(142, 80, 180, 28);
         fnameTextField.setColumns(10);
         fnameTextField.setBorder(BorderFactory.createLineBorder(null));
 
         emailTextField = new JTextField(currentUser.getMail());
         emailTextField.setBounds(142, 120, 180, 28);
         emailTextField.setColumns(10);
         emailTextField.setBorder(BorderFactory.createLineBorder(null));
 
         passwordField = new JPasswordField();
         passwordField.setBounds(142, 198, 180, 28);
         passwordField.setBorder(BorderFactory.createLineBorder(null));
 
         newpasswordField = new JPasswordField();
         newpasswordField.setBounds(142, 232, 180, 28);
         newpasswordField.setBorder(BorderFactory.createLineBorder(null));
 
         confirmpasswordField = new JPasswordField();
         confirmpasswordField.setBounds(142, 266, 180, 28);
         confirmpasswordField.setBorder(BorderFactory.createLineBorder(null));
 
         JPanel panel = new JPanel();
         panel.setBounds(6, 324, 70, 70);
         panel.setBackground(Color.GRAY);
 
         JSeparator separator_1 = new JSeparator();
         separator_1.setBounds(9, 306, 264, 12);
 
         JLabel lblProfilePicture = new JLabel("Profile Picture");
         lblProfilePicture.setBounds(88, 326, 115, 20);
         lblProfilePicture.setFont(new Font("Lucida Grande", Font.BOLD, 16));
 
         JButton btnUploadNewPhoto = new JButton("Upload");
         btnUploadNewPhoto.setBounds(263, 365, 89, 29);
 
         textField = new JTextField();
         textField.setBounds(84, 364, 159, 28);
         textField.setColumns(10);
         setLayout(null);
         add(lblPublicInformations);
         add(lblChangePassword);
         add(lblOldPassword);
         add(newpasswordField);
         add(passwordField);
         add(lblNewPassword);
         add(lblProfilePicture);
         add(separator);
         add(lblName);
         add(lblLastName);
         add(lblEmailAdress);
         add(emailTextField);
         add(fnameTextField);
         add(nameTextField);
         add(panel);
         add(textField);
         add(btnUploadNewPhoto);
         add(separator_1);
         add(lblConfirmPassword);
         add(confirmpasswordField);
 
         JSeparator separator_2 = new JSeparator();
         separator_2.setBounds(9, 406, 343, 12);
         add(separator_2);
 
         JButton btnUpdate = new JButton("Update");
         btnUpdate.addActionListener(new ActionListener() {
 
             public void actionPerformed(ActionEvent e) {
                 // This hashMap contains the values which we want to update
                 HashMap<String, Boolean> valuesToEdit = new HashMap<String, Boolean>();
                 // Put the values we want to edit in the HashMap with the false
                 // as default
                 if (passwordChanged()) {
                     valuesToEdit.put("pass", false);
                 }
                 if (nameEdited()) {
                     valuesToEdit.put("name", false);
                 }
                 if (fNameEdited()) {
                     valuesToEdit.put("fname", false);
                 }
                 if (mailEdited()) {
                     valuesToEdit.put("mail", false);
                 }
 
                 // Coloring the textFileds
                 if (valuesToEdit.containsKey("pass")) {
                     if (newPassConfirmed()) {
                         valuesToEdit.put("pass", true);
                         newpasswordField.setBorder(BorderFactory.createLineBorder(Color.green));
                         confirmpasswordField.setBorder(BorderFactory.createLineBorder(Color.green));
                     } else {
                         newpasswordField.setBorder(BorderFactory.createLineBorder(Color.red));
                         confirmpasswordField.setBorder(BorderFactory.createLineBorder(Color.red));
                     }
                 }
 
                 if (valuesToEdit.containsKey("name")) {
                     if (!nameTextField.getText().equals(CurrentUser.getName())
                             && nameTextField.getText().length() > 0) {
                         valuesToEdit.put("name", true);
                         nameTextField.setBorder(BorderFactory.createLineBorder(Color.green));
                     } else {
                         nameTextField.setBorder(BorderFactory.createLineBorder(Color.red));
                     }
                 }
 
                 if (valuesToEdit.containsKey("fname")) {
                     if (!fnameTextField.getText().equals(CurrentUser.getFname())
                             && fnameTextField.getText().length() > 0) {
                         valuesToEdit.put("fname", true);
                         fnameTextField.setBorder(BorderFactory.createLineBorder(Color.green));
                     } else {
                         fnameTextField.setBorder(BorderFactory.createLineBorder(Color.red));
                     }
                 }
 
                 if (valuesToEdit.containsKey("mail")) {
                     if (!emailTextField.getText().equals(CurrentUser.getMail())
                             && Pattern.matches(
                             "^[_a-z0-9-]+(\\.[_a-z0-9-]+)*@[a-z0-9-]+(\\.[a-z0-9-]+)+$",
                             emailTextField.getText()) && passwordField.getPassword().length >= 4) {
                         valuesToEdit.put("mail", true);
                         emailTextField.setBorder(BorderFactory.createLineBorder(Color.green));
                     } else {
                         emailTextField.setBorder(BorderFactory.createLineBorder(Color.red));
                         passwordField.setBorder(BorderFactory.createLineBorder(Color.red));
                     }
                 }
 
                 HashMap<String, Boolean> editingResults = new HashMap<String, Boolean>();
 
                 if (!valuesToEdit.containsValue(false)) {
                     if (valuesToEdit.containsKey("name")) {
                         editingResults.put("name", RemoteRequests.changName(
                                 CurrentUser.getId(), nameTextField.getText()));
                     }
 
                     if (valuesToEdit.containsKey("fname")) {
                         editingResults.put("fname", RemoteRequests.changeFname(
                                 CurrentUser.getId(), fnameTextField.getText()));
                     }
 
                     if (valuesToEdit.containsKey("mail")) {
                         editingResults.put("mail", RemoteRequests.changeMail(
                                 CurrentUser.getId(), new HashPassword(
                                 passwordField.getPassword()).getHashed(), emailTextField.getText()));
                     }
 
                     if (valuesToEdit.containsKey("pass")) {
                         editingResults.put("pass", RemoteRequests.changePassword(
                                 CurrentUser.getId(),
                                 new HashPassword(passwordField.getPassword()).getHashed(),
                                new HashPassword(passwordField.getPassword()).getHashed()));
                     }
 
                     String updateSucess = "<b>Updates : <b><br />";
                     String updateFailure = "<b>Failures : </b><br />";
 
                     if (editingResults.containsKey("name")) {
                         if (editingResults.get("name")) {
                             updateSucess += "name<br />";
                             CurrentUser.get().setName(nameTextField.getText());
                         } else {
                             updateFailure += "name<br />";
                             nameTextField.setBorder(BorderFactory.createLineBorder(Color.red));
                         }
                     }
                     if (editingResults.containsKey("fname")) {
                         if (editingResults.get("fname")) {
                             updateSucess += "forname<br />";
                             CurrentUser.get().setFname(fnameTextField.getText());
                         } else {
                             updateFailure += "forname<br />";
                             fnameTextField.setBorder(BorderFactory.createLineBorder(Color.red));
                         }
                     }
                     if (editingResults.containsKey("mail")) {
                         if (editingResults.get("mail")) {
                             updateSucess += "Email Adress<br />";
                             CurrentUser.get().setMail(emailTextField.getText());
                         } else {
                             updateFailure += "Email Adress<br />";
                             emailTextField.setBorder(BorderFactory.createLineBorder(Color.red));
                             passwordField.setBorder(BorderFactory.createLineBorder(Color.red));
                         }
                     }
                     if (editingResults.containsKey("pass")) {
                         if (editingResults.get("pass")) {
                             updateSucess += "Password<br />";
                         } else {
                             updateFailure += "Password<br />";
                             passwordField.setBorder(BorderFactory.createLineBorder(Color.red));
                             newpasswordField.setBorder(BorderFactory.createLineBorder(Color.red));
                             confirmpasswordField.setBorder(BorderFactory.createLineBorder(Color.red));
                         }
                     }
 
                     if (!updateSucess.equals("<b>Updates : <b><br />")) {
                         if (updateFailure.equals("<b>Failures : </b><br />")) {
                             SayToUser.success("Update Successed", updateSucess);
                         } else {
                             SayToUser.warning("Update Warning", updateSucess
                                     + updateFailure);
                         }
                     } else if (!updateFailure.equals("<b>Failures : </b><br />")) {
                         SayToUser.error("Update Failure", updateFailure);
                     } else {
                         SayToUser.warning("", "No thing to update :)");
                     }
 
                     if (!mailEdited() && !passwordChanged()) {
                         passwordField.setBorder(BorderFactory.createLineBorder(Color.black));
                     }
                 }
 
             }
         });
         btnUpdate.setBounds(116, 425, 117, 29);
         add(btnUpdate);
     }
 
     public void resetAll(User currentUser) {
         nameTextField.setText(currentUser.getName());
         fnameTextField.setText(currentUser.getFname());
         emailTextField.setText(currentUser.getMail());
     }
 
     private boolean passwordChanged() {
         boolean retVal = false;
         if (newpasswordField.getPassword().length > 0
                 || confirmpasswordField.getPassword().length > 0) {
             retVal = true;
         } else {
             newpasswordField.setBorder(BorderFactory.createLineBorder(Color.black));
             confirmpasswordField.setBorder(BorderFactory.createLineBorder(Color.black));
         }
         return retVal;
     }
 
     private boolean nameEdited() {
         boolean retVal = false;
         if (!nameTextField.getText().equals(CurrentUser.getName())) {
             retVal = true;
         } else {
             nameTextField.setBorder(BorderFactory.createLineBorder(Color.black));
         }
         return retVal;
     }
 
     private boolean fNameEdited() {
         boolean retVal = false;
         if (!fnameTextField.getText().equals(CurrentUser.getFname())) {
             retVal = true;
         } else {
             fnameTextField.setBorder(BorderFactory.createLineBorder(Color.black));
         }
         return retVal;
     }
 
     private boolean mailEdited() {
         boolean retVal = false;
         if (!emailTextField.getText().equals(CurrentUser.getMail())) {
             retVal = true;
         } else {
             emailTextField.setBorder(BorderFactory.createLineBorder(Color.black));
         }
         return retVal;
     }
 
     private boolean newPassConfirmed() {
 
         boolean passConfirmed = newpasswordField.getPassword().length == confirmpasswordField.getPassword().length;
         if (!passConfirmed) {
             return false;
         }
         passConfirmed = newpasswordField.getPassword().length >= 4;
         if (passConfirmed) {
             for (int i = 0; i < newpasswordField.getPassword().length; i++) {
                 if (newpasswordField.getPassword()[i] != confirmpasswordField.getPassword()[i]) {
                     passConfirmed = false;
                     break;
                 }
             }
         }
         return passConfirmed;
     }
 }
