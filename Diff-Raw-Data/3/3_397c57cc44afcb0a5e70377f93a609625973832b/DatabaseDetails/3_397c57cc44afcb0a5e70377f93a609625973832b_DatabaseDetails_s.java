 /*
  * This file is part of the aidGer project.
  *
  * Copyright (C) 2010-2011 The aidGer Team
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package de.aidger.view.wizard;
 
 import de.aidger.controller.ActionNotFoundException;
 import de.aidger.controller.ActionRegistry;
 import de.aidger.controller.actions.DatabaseDetailsFinishAction;
 import static de.aidger.utils.Translation._;
 import de.aidger.view.WizardPanel;
 import de.aidger.model.Runtime;
 import de.aidger.view.UI;
 import javax.swing.AbstractAction;
 
 /**
  * Allow the user to enter the details regarding his database connection.
  *
  * @author rmbl
  */
 public class DatabaseDetails extends WizardPanel {
 
     /** Creates new form DatabaseDetails */
     public DatabaseDetails() {
         initComponents();
         try {
             setNextAction((AbstractAction) ActionRegistry.getInstance().get(DatabaseDetailsFinishAction.class.getName()));
         } catch (ActionNotFoundException ex) {
             UI.displayError(ex.getMessage());
         }
     }
 
     /**
      * Prepare the panel depending on the selected database type.
      */
     @Override
     public void preparePanel() {
         String dbtype = Runtime.getInstance().getOption("database-type");
         String uri = Runtime.getInstance().getOption("database-uri");
 
         if (dbtype == null || dbtype.equals("0")) {
             portLbl.setVisible(false);
             portSpinner.setVisible(false);
             databaseLbl.setVisible(false);
             databaseText.setVisible(false);
             usernameLbl.setVisible(false);
             usernameText.setVisible(false);
             passwordLbl.setVisible(false);
             passwordText.setVisible(false);
             driverLbl.setVisible(false);
             driverText.setVisible(false);
 
             hostLbl.setText(_("Path:"));
             driverText.setText("org.apache.derby.jdbc.EmbeddedDriver");
 
             String host;
             if (uri != null && uri.startsWith("jdbc:derby")) {
                 host = uri.substring(11);
                 if (host.endsWith(";create=true")) {
                     host = host.substring(0, host.length() - 12);
                 }
                 
             } else {            
                 host = Runtime.getInstance().getConfigPath() + "/database";
             }
             hostText.setText(host);
         } else if (dbtype.equals("1")) {
             portLbl.setVisible(true);
             portSpinner.setVisible(true);
             databaseLbl.setVisible(true);
             databaseText.setVisible(true);
             usernameLbl.setVisible(true);
             usernameText.setVisible(true);
             passwordLbl.setVisible(true);
             passwordText.setVisible(true);
             driverLbl.setVisible(false);
             driverText.setVisible(false);
             driverText.setText("com.mysql.jdbc.Driver");
 
             if (uri != null && uri.startsWith("jdbc:mysql")) {
                 uri = uri.substring(13);
                 String[] parts = uri.split("/");
                 String[] subparts = parts[0].split(":");
                 hostText.setText(subparts[0]);
                 portSpinner.setValue(Integer.parseInt(subparts[1]));
                 subparts = parts[1].split("\\?");
                 databaseText.setText(subparts[0]);
                 if (subparts.length > 1) {
                     usernameText.setText(subparts[1].substring(5, subparts[1].indexOf("&")));
                     if (subparts[1].charAt(subparts[1].indexOf("&") + 9) == '=') {
                         passwordText.setText(subparts[1].substring(subparts[1].indexOf("&") + 10, subparts[1].lastIndexOf("&")));
                     } else {
                         passwordText.setText("");
                     }
 
                 }
             } else {
                 hostText.setText("localhost");
                 portSpinner.setValue(3306);
                 databaseText.setText("aidger");
                 usernameText.setText("root");
                 passwordText.setText("");
             }            
         } else {
             portLbl.setVisible(false);
             portSpinner.setVisible(false);
             databaseLbl.setVisible(false);
             databaseText.setVisible(false);
             usernameLbl.setVisible(false);
             usernameText.setVisible(false);
             passwordLbl.setVisible(false);
             passwordText.setVisible(false);
             driverLbl.setVisible(true);
             driverText.setVisible(true);
 
             hostLbl.setText(_("JDBC Uri:"));
 
             if (uri != null && !uri.isEmpty()) {
                 hostText.setText(uri);
             } else {
                 hostText.setText("jdbc:derby:" + Runtime.getInstance().getConfigPath() + "/database;create=true");
             }
 
             String driver = Runtime.getInstance().getOption("database-driver");
             if (driver != null && !driver.isEmpty()) {
                 driverText.setText(driver);
             } else {
                 driverText.setText("org.apache.derby.jdbc.EmbeddedDriver");
             }
         }
     }
 
     /**
      * Get the database driver.
      *
      * @return The driver
      */
     public String getDriver() {
         return driverText.getText();
     }
 
     /**
      * Get the host/path of the database.
      *
      * @return The host/path
      */
     public String getHost() {
         return hostText.getText();
     }
 
     /**
      * Get the port of the database.
      *
      * @return The port
      */
     public Integer getPort() {
         return (Integer) portSpinner.getValue();
     }
 
     /**
      * Get the database name.
      *
      * @return The database name
      */
     public String getDatabase() {
         return databaseText.getText();
     }
 
     /**
      * Get the username needed to connect to the database.
      *
      * @return The username
      */
     public String getUsername() {
         return usernameText.getText();
     }
 
     /**
      * Get the password associated to the username.
      *
      * @return The password
      */
     public String getPassword() {
        return passwordText.getText();
     }
 
     /** This method is called from within the constructor to
      * initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is
      * always regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         jLabel2 = new javax.swing.JLabel();
         jLabel1 = new javax.swing.JLabel();
         hostLbl = new javax.swing.JLabel();
         hostText = new javax.swing.JTextField();
         portLbl = new javax.swing.JLabel();
         usernameLbl = new javax.swing.JLabel();
         usernameText = new javax.swing.JTextField();
         passwordLbl = new javax.swing.JLabel();
         passwordText = new javax.swing.JPasswordField();
         driverLbl = new javax.swing.JLabel();
         driverText = new javax.swing.JTextField();
         portSpinner = new javax.swing.JSpinner();
         databaseLbl = new javax.swing.JLabel();
         databaseText = new javax.swing.JTextField();
 
         setPreferredSize(new java.awt.Dimension(500, 300));
 
         jLabel2.setFont(new java.awt.Font("DejaVu Sans", 0, 36));
         jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
         jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/aidger/res/icons/aidger-icon.png"))); // NOI18N
         jLabel2.setText("aidGer");
 
         jLabel1.setText(_("Please enter details for your database connection here."));
 
         hostLbl.setText(_("Host:"));
 
         portLbl.setText(_("Port:"));
 
         usernameLbl.setText(_("Username:"));
 
         passwordLbl.setText(_("Password:"));
 
         driverLbl.setText(_("Driver:"));
 
         portSpinner.setModel(new javax.swing.SpinnerNumberModel(1, 0, 65536, 1));
         portSpinner.setAlignmentX(portSpinner.LEFT_ALIGNMENT);
 
         databaseLbl.setText(_("Database:"));
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
         this.setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(layout.createSequentialGroup()
                         .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(hostLbl)
                             .addComponent(passwordLbl)
                             .addComponent(usernameLbl)
                             .addComponent(driverLbl)
                             .addComponent(databaseLbl)
                             .addComponent(portLbl))
                         .addGap(99, 99, 99)
                         .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                             .addComponent(portSpinner, javax.swing.GroupLayout.DEFAULT_SIZE, 288, Short.MAX_VALUE)
                             .addComponent(databaseText, javax.swing.GroupLayout.DEFAULT_SIZE, 288, Short.MAX_VALUE)
                             .addComponent(driverText, javax.swing.GroupLayout.DEFAULT_SIZE, 288, Short.MAX_VALUE)
                             .addComponent(usernameText, javax.swing.GroupLayout.DEFAULT_SIZE, 288, Short.MAX_VALUE)
                             .addComponent(passwordText, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 288, Short.MAX_VALUE)
                             .addComponent(hostText, javax.swing.GroupLayout.DEFAULT_SIZE, 288, Short.MAX_VALUE)))
                     .addComponent(jLabel1))
                 .addContainerGap())
             .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 500, Short.MAX_VALUE)
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jLabel2)
                 .addGap(18, 18, 18)
                 .addComponent(jLabel1)
                 .addGap(18, 18, 18)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(hostLbl)
                     .addComponent(hostText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(portSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(portLbl))
                 .addGap(7, 7, 7)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(databaseText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(databaseLbl))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(usernameText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(usernameLbl))
                 .addGap(7, 7, 7)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(passwordLbl)
                     .addComponent(passwordText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addGap(7, 7, 7)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(driverText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(driverLbl))
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
     }// </editor-fold>//GEN-END:initComponents
 
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JLabel databaseLbl;
     private javax.swing.JTextField databaseText;
     private javax.swing.JLabel driverLbl;
     private javax.swing.JTextField driverText;
     private javax.swing.JLabel hostLbl;
     private javax.swing.JTextField hostText;
     private javax.swing.JLabel jLabel1;
     private javax.swing.JLabel jLabel2;
     private javax.swing.JLabel passwordLbl;
     private javax.swing.JPasswordField passwordText;
     private javax.swing.JLabel portLbl;
     private javax.swing.JSpinner portSpinner;
     private javax.swing.JLabel usernameLbl;
     private javax.swing.JTextField usernameText;
     // End of variables declaration//GEN-END:variables
 
 }
