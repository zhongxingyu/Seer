 package Clamshell;
 
 import java.io.*;
 import java.nio.CharBuffer;
 import java.nio.ByteBuffer;
 import javax.swing.*;
 import java.util.ArrayList;
 /**
  * Main UI for secure password management application
  * COSC 625 Fall 2013
  * @author Andrew Ramsey
  * @version 1.6
  */
 public class ClamshellUI extends javax.swing.JFrame {
     
     private ArrayList<Entry> passList; // must specify Entry type object
     private String writeHelper = "";
     
     // default designations for delimeters and padding
     private final String delimeter = "   "; // entry data delimeter
     private final String newline = "\r\n"; // new line delimeter for DOS/Windows but also will work for UNIX
     private final String pad = "0";
     private final String placeholder = "000"; // placeholder represents blank fields inside entry object when encrypted to file
     
     // the values passed in from initial UI window (authentication procedure)
     private File authFile;
     private String userName;
     private String masterPass;
     private boolean isAuthenticated = false;
     
     /**
      * Beginning insertion point of program
      * @param args the command line arguments
      */
     public static void main(String args[]) {
         
         ClamshellUI clam = new ClamshellUI();
         clam.setLocationRelativeTo(null); // center window in middle of screen
         
         if (clam.isAuthenticated == true)
         {
             clam.setVisible(true);
         } else { System.exit(0); }
     }
     
     /**
      * UI constructor
      * Provides authentication and loads entry objects
      * Then create ClamshellUI and perform crypto IO
      */
     public ClamshellUI() {
         initComponents();
         passList = new ArrayList<Entry>(); // Must specify as <Entry> or else compiler throws Xlint - unsafe operations
         // Now authenticate the user before continuing ClamshellUI
         AuthenticateDialog dialog = new AuthenticateDialog(new javax.swing.JFrame(), true);
         dialog.setLocationRelativeTo(null);// center the auth dialog window in middle of screen
         dialog.addWindowListener(new java.awt.event.WindowAdapter() {
             @Override
             public void windowClosing(java.awt.event.WindowEvent e) {
                 System.exit(0);
             }
         });
         dialog.setVisible(true);
         // if authenticated, continue
         if (dialog.getAuthenticated() == true) { // ensure user is validated before continuing
             // make sure we pass the relevant information user provided
             //  to new UI screen before disposing of old UI screen
             authFile = dialog.getAuthFile();
             userName = dialog.getUsername();
             masterPass = dialog.getMasterPass();
             isAuthenticated = dialog.getAuthenticated();
             dialog.dispose(); // we are done authenticating
             
             if (authFile == null) // we are opening existing user's password file
             {
                 String encryptedFileName = RC4Cipher(userName, masterPass);
                 String bigSky = "";
                 try { // read the raw data, then decrypt using RC4 and master password
                     BufferedReader reader = new BufferedReader(new FileReader(encryptedFileName));
                     String line;
                     while ((line = reader.readLine()) != null) {
                         bigSky += line;
                     }
                 } catch (Exception e) {
                     System.exit(5);
                 }                
                 bigSky = RC4Cipher(bigSky, masterPass); // decrypt raw data from password file
                System.out.println(bigSky);
                //String[] rawEntries = bigSky.split('['+delimeter+']');
                 //String[] rawEntries = bigSky.split('['+delimeter+']' + '['+newline+']'); //split on delimeter and newline symbols
                 /*for (int i = 0; i < rawEntries.length; i += 4){
                     // sname, uname, IV, pass
                     passList.add(new Entry(rawEntries[i], rawEntries[i+1], rawEntries[i+2], rawEntries[i+3]));
                 }*/
                /*for (int i = 0; i < rawEntries.length; i++)
                 {
                     System.out.println(rawEntries[i]);
                }*/
                
                 deleteButton.setEnabled(true);
             } else // we are creating a new file - none yet exists
             {
                 // new file, therefore nothing to do here
                 // user enters data automatically populated to file
             }
         }// otherwise, deny access - shutdown program
         else {System.exit(0);}     
     }
     
     // initComponents() below: 
     //Auto-Generated Code from Java Swing GUI builder 
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         jDialog1 = new javax.swing.JDialog();
         jScrollPane2 = new javax.swing.JScrollPane();
         jTextPane2 = new javax.swing.JTextPane();
         sname = new javax.swing.JTextField();
         uname = new javax.swing.JTextField();
         pass = new javax.swing.JTextField();
         addButton = new javax.swing.JButton();
         deleteButton = new javax.swing.JButton();
 
         javax.swing.GroupLayout jDialog1Layout = new javax.swing.GroupLayout(jDialog1.getContentPane());
         jDialog1.getContentPane().setLayout(jDialog1Layout);
         jDialog1Layout.setHorizontalGroup(
             jDialog1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 400, Short.MAX_VALUE)
         );
         jDialog1Layout.setVerticalGroup(
             jDialog1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 300, Short.MAX_VALUE)
         );
 
         setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
         setAlwaysOnTop(true);
         setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
         setIconImages(null);
         setMinimumSize(new java.awt.Dimension(554, 255));
         setName("clamshell"); // NOI18N
 
         jTextPane2.setEditable(false);
         jTextPane2.setBorder(javax.swing.BorderFactory.createEtchedBorder());
         jTextPane2.setCursor(new java.awt.Cursor(java.awt.Cursor.TEXT_CURSOR));
         jTextPane2.setDoubleBuffered(true);
         jTextPane2.setMaximumSize(new java.awt.Dimension(1280, 1280));
         jTextPane2.setName(""); // NOI18N
         jScrollPane2.setViewportView(jTextPane2);
 
         sname.setToolTipText("Enter Service Name");
         sname.setName(""); // NOI18N
 
         uname.setToolTipText("Enter User Name");
 
         pass.setToolTipText("Enter Password");
 
         addButton.setText("Add Entry");
         addButton.setToolTipText("Add Entry to Password List");
         addButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 addButtonActionPerformed(evt);
             }
         });
 
         deleteButton.setText("Delete Entry");
         deleteButton.setToolTipText("Delete Entry from Password List");
         deleteButton.setEnabled(false);
         deleteButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 deleteButtonActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jScrollPane2)
                     .addGroup(layout.createSequentialGroup()
                         .addComponent(sname, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(uname, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(pass, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(addButton)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 40, Short.MAX_VALUE)
                         .addComponent(deleteButton, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE))))
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addGap(8, 8, 8)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(sname, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(uname, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(pass, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(addButton)
                     .addComponent(deleteButton))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 218, Short.MAX_VALUE))
         );
 
         pack();
     }// </editor-fold>//GEN-END:initComponents
     
     /**
      * Remove entry from text area based on the user input (number provided)
      * @param evt 
      */
     private void deleteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteButtonActionPerformed
        
        if (!passList.isEmpty())
        {   
             try{
                 int entryNumber = Integer.parseInt(JOptionPane.showInputDialog(null, "Enter Entry Number:", "Delete Entry?", JOptionPane.QUESTION_MESSAGE));
                 
                 if (entryNumber >= 0 && entryNumber < passList.size())
                 {
                     passList.remove((int)entryNumber);
                 }
                 else
                 {
                     JOptionPane.showMessageDialog(null, "Entry Number Out of Bounds", "Error", JOptionPane.ERROR_MESSAGE);
                 }
             }
             catch(Exception e){
                 JOptionPane.showMessageDialog(null, "Entry Number Out of Bounds", "Error", JOptionPane.ERROR_MESSAGE);
             }
             if (passList.isEmpty()) // ensure turn off button for case user deleted last entry and there are no entries left
             {
                 deleteButton.setEnabled(false);
             }
             else
             {
                 deleteButton.setEnabled(true);
             }
             updateTextArea();
             updatePasswordFile();
        }
        else
        {
            deleteButton.setEnabled(false);
        }
     }//GEN-LAST:event_deleteButtonActionPerformed
     
     /**
      * Update the text area field when passList is updated
      */
     private void updateTextArea() {
         String bigSky = "";
         String bigSky2 = ""; // used to generate raw data for password file output including init vector value to decrypt password values with AES
         for (int i = 0; i < passList.size(); ++i) {
             bigSky += i + delimeter + passList.get(i).getServiceName() + delimeter + passList.get(i).getUserName() + delimeter + passList.get(i).getPassword() + newline;
             // sname, uname, IV, pass
             bigSky2 += passList.get(i).getServiceName() + delimeter + passList.get(i).getUserName() + delimeter + passList.get(i).IV + delimeter + passList.get(i).getPassword() + delimeter;
         }
         jTextPane2.setText(bigSky); // delete entry from flexible object type (arraylist)
         jTextPane2.updateUI(); // update jTextField2 (text area)
        
        writeHelper = RC4Cipher(bigSky2, masterPass); // encrypt using RC4 for writing to File
        System.out.println(writeHelper);
     }
     
     /**
      * Write RC4-encrypted entry data to file
      */
     private void updatePasswordFile() {
        
        if (authFile == null) {
             //String encryptedFileName = RC4Cipher(userName, masterPass);
             authFile = new File(RC4Cipher(userName, masterPass));
         }
         try {
             BufferedWriter writer = new BufferedWriter(new FileWriter(authFile));
            writer.write(writeHelper);
             writer.close();
         } catch (Exception e) {
             System.exit(10);
         }
     }
     
     /**
      * Add Entry From three inputs on Clamshell UI
      * Then update passList, UI
      * @param evt 
      */
     private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
         
         String sn = sname.getText();
         String un = uname.getText();
         String pss = pass.getText();
         
         if (sn.isEmpty()) // create placeholder variables so that when decrypted, delimeters are not skipped
         {
             sn = placeholder;
         }
         if (un.isEmpty())
         {
             un = placeholder;
         }
         if (pss.isEmpty())
         {
             pss = placeholder;
         }
         
         passList.add(new Entry(sn, un, pss));
         sname.setText(""); // reset UI fields
         uname.setText("");
         pass.setText("");
         if (!passList.isEmpty())
         {
             deleteButton.setEnabled(true); // now we can delete entries
         }
         else
         {
             deleteButton.setEnabled(false); 
         }
         updateTextArea();
         updatePasswordFile();
     }//GEN-LAST:event_addButtonActionPerformed
         
     /**
      * Convert user name input to RC4-encrypted string
      * @return 
      */
     private String RC4Cipher(String u, String p)
     {
         RC4 rc = new RC4();
         return rc.cipher(byteArrayToHexString(u.getBytes()), byteArrayToHexString(p.getBytes()));
     }
     
     /**
      * Decrypt ciphertext password
     * @param cipher - plaintext in hexadecimal
      * @param k - key as 
      * @param iv
      * @return String
      */
     private String decryptAES(String cipher, String k, String iv)
     {
        // no need to convert to hexadecimal - input text is already hexadecimal
         //cipher = byteArrayToHexString(cipher.getBytes());
         String key = byteArrayToHexString(k.getBytes());
         // init vector already in hexadecimal - no need to convert
         Yaes aes = new Yaes(key);
         String decryptedPass = "";
         // ensure key length to 64 chars hex
         // IV length is already set to 32 chars hex by Entry object
         if (key.length() < 64)
         {
             while (k.length() < 64){
                 k += pad; // pad with zeros  
             }
         }
         else if (k.length() > 64)
         {
             key = key.substring(0, 64); // truncate
         }
         // parse password data into & out of AES 32 hex at a time
         // first ensure that hex chars are multiple of 32
         int r = cipher.length() % 32; // r = how many we must pad to end
         int d = (int) cipher.length() / 32; // s = number of AES rounds required to cycle thru to encipher all plaintext 
         
         for (int i = cipher.length(); i < cipher.length()+r; ++i)
         {
             cipher += pad;
         }
         // now we decipher the padded text
         for (int i = 0; i < d; ++i)
         {
             decryptedPass += aes.decryptString(cipher.substring(i*32, i*32+32), iv);
         }
         return bytesToStringUTFNIO(hexStringToByteArray(decryptedPass));
     }
     
     /**
      * Encrypt using AES 256
      * @param plain - plaintext
      * @param k - key
      * @param iv - initialization vector
      * @return String - encrypted text as hexadecimal
      */
     private String encryptAES(String plain, String k, String iv)
     {
         // convert to hexadecimal
         plain = byteArrayToHexString(plain.getBytes());
         String key = byteArrayToHexString(k.getBytes());
         // init vector already in hexadecimal - no need to convert
         Yaes aes = new Yaes(key);
         String encryptedPass = "";
         // ensure key length to 64 chars hex
         // IV length is already set to 32 chars hex by Entry object
         if (key.length() < 64)
         {
             while (k.length() < 64){
                 k += pad; // pad with zeros  
             }
         }
         else if (k.length() > 64)
         {
             key = key.substring(0, 64); // truncate
         }
         // parse password data into & out of AES 32 hex at a time
         // first ensure that hex chars are multiple of 32
         int r = plain.length() % 32; // r = how many we must pad to end
         int d = (int) plain.length() / 32; // s = number of AES rounds required to cycle thru to encipher all plaintext 
         
         for (int i = plain.length(); i < plain.length()+r; ++i)
         {
             plain += pad;
         }
         // now we encipher the padded text
         for (int i = 0; i < d; ++i)
         {
             encryptedPass += aes.encryptString(plain.substring(i*32, i*32+32), iv);
         }
         return encryptedPass;
     }
     
     private byte[] hexStringToByteArray(String s) {
         byte[] b = new byte[s.length() / 2];
         for (int i = 0; i < b.length; i++) {
             int index = i * 2;
             int v = Integer.parseInt(s.substring(index, index + 2), 16);
             b[i] = (byte) v;
         }
         return b;
     }
     
     private String byteArrayToHexString(byte[] b) {
         String result = "";
         for (int i = 0; i < b.length; i++) {
             result += Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
         }
         return result;
     }
     
     public byte[] stringToBytesUTFNIO(String str) {
         char[] buffer = str.toCharArray();
         byte[] b = new byte[buffer.length << 1];
         CharBuffer cBuffer = ByteBuffer.wrap(b).asCharBuffer();
         for (int i = 0; i < buffer.length; i++) {
             cBuffer.put(buffer[i]);
         }
         return b;
     }
     
     public String bytesToStringUTFNIO(byte[] bytes) {
         CharBuffer cBuffer = ByteBuffer.wrap(bytes).asCharBuffer();
         return cBuffer.toString();
     }
     
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JButton addButton;
     private javax.swing.JButton deleteButton;
     private javax.swing.JDialog jDialog1;
     private javax.swing.JScrollPane jScrollPane2;
     private javax.swing.JTextPane jTextPane2;
     private javax.swing.JTextField pass;
     private javax.swing.JTextField sname;
     private javax.swing.JTextField uname;
     // End of variables declaration//GEN-END:variables
 }
