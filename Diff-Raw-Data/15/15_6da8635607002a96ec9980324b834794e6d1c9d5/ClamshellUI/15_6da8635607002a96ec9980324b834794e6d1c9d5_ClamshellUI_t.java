 package Clamshell;
 
 import java.io.*;
 import java.nio.charset.*;
 import javax.swing.*;
 import java.util.ArrayList;
 
 /**
  * Main UI for secure password management application COSC 625 Fall 2013
  *
  * @author Andrew Ramsey
  * @version 2.0
  */
 public class ClamshellUI extends javax.swing.JFrame {
 
     private ArrayList<Entry> passList; // must specify Entry type object
     private String writeHelper = "";
     // default designations for delimeters and padding
     private final String delimeter = "   "; // entry data delimeter
     private final String newline = "\n";
     private final String pad = "20";
     private final String placeholder = "placeholder"; // placeholder represents blank fields inside entry object when encrypted to file
     private final Charset charset = Charset.forName("UTF-8");
     private final int keylength = 32;
     // values for length of key and plaintext in hex (256 vs 128 bit AES) ... 256: k = 64 pt = 32 iv = 32 128: k = 32 pt = 32
     // the values passed in from initial UI window (authentication procedure)
     private File authFile;
     private String userName;
     private String masterPass;
     private boolean isAuthenticated = false;
 
     /**
      * Beginning insertion point of program
      *
      * @param args the command line arguments
      */
     public static void main(String args[]) {
 
         ClamshellUI clam = new ClamshellUI();
         clam.setLocationRelativeTo(null); // center window in middle of screen
 
         if (clam.isAuthenticated == true) {
             clam.setVisible(true);
         } else {
             System.exit(0);
         }
     }
 
     /**
      * UI constructor Provides authentication and loads entry objects Then
      * create ClamshellUI and perform crypto IO
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
                 String bigSky = "";
                 String encryptedFileName = RC4Cipher(userName, masterPass);
                 bigSky = readFromFile(encryptedFileName);
                 // decrypt raw data from password file by running RC4 (symmetric cipher)
                 String deciphered = byteToString(RC4(hexStringToByteArray(bigSky), hexStringToByteArray(stringToHex(masterPass))));
                 //String deciphered = bigSky;
                 String[] entryLines = deciphered.split(newline);
                 for (int i = 0; i < entryLines.length; ++i) {
 
                     String[] rawEntries = entryLines[i].split(delimeter); // further break down 2D array
                     //first decrypt the AES encrypted password
                     //ciphertext, key, IV
                     passList.add(new Entry(rawEntries[0], rawEntries[1], rawEntries[2]));
                     deleteButton.setEnabled(true);
                     updateTextArea();
                     updatePasswordFile();
 
                 }
             }// otherwise, deny access - shutdown program
 
         } else {
             System.exit(0);
         }
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
         editEntryButton = new javax.swing.JButton();
 
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
        setTitle("Password Manager");
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
 
         editEntryButton.setText("Edit Entry");
         editEntryButton.setToolTipText("");
         editEntryButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
         editEntryButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 editEntryButtonActionPerformed(evt);
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
                         .addComponent(sname, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(uname, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(pass, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(addButton)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(editEntryButton)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 59, Short.MAX_VALUE)
                         .addComponent(deleteButton))
                     .addComponent(jScrollPane2)))
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
                     .addComponent(deleteButton)
                     .addComponent(editEntryButton))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 227, Short.MAX_VALUE))
         );
 
         pack();
     }// </editor-fold>//GEN-END:initComponents
 
     public boolean setSomething() {
         return true;
     }
 
     /**
      * Update the text area field when passList is updated
      */
     private void updateTextArea() {
         String bigSky = "";
         String bigSky2 = ""; // used to generate raw data for password file output including init vector value to decrypt password values with AES
         for (int i = 0; i < passList.size(); ++i) {
             bigSky += i + delimeter + passList.get(i).getServiceName() + delimeter + passList.get(i).getUserName() + delimeter + decryptAES(passList.get(i).getPassword(), masterPass) + newline;
             bigSky2 += passList.get(i).getServiceName() + delimeter + passList.get(i).getUserName() + delimeter + passList.get(i).getPassword() + newline;
         }
         jTextPane2.setText(bigSky);
         jTextPane2.updateUI(); // update jTextField2 (text area)
         writeHelper = RC4Cipher(bigSky2, masterPass); // encrypt using RC4 for writing to File
     }
 
     /**
      * Remove entry from text area based on the user input (number provided)
      *
      * @param evt
      */
     private void deleteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteButtonActionPerformed
 
         if (!passList.isEmpty()) {
             try {
                 int entryNumber = Integer.parseInt(JOptionPane.showInputDialog(null, "Enter Entry Number:", "Delete Entry?", JOptionPane.OK_OPTION));
 
                 if (entryNumber >= 0 && entryNumber < passList.size()) {
                     passList.remove((int) entryNumber); // delete entry from flexible object type (arraylist)
                 } else {
                     JOptionPane.showMessageDialog(null, "Entry Number Out of Bounds", "Error", JOptionPane.ERROR_MESSAGE);
                 }
             } catch (Exception e) {
                 JOptionPane.showMessageDialog(null, "Entry Number Out of Bounds", "Error", JOptionPane.ERROR_MESSAGE);
             }
             if (passList.isEmpty()) // ensure turn off button for case user deleted last entry and there are no entries left
             {
                 deleteButton.setEnabled(false);
             } else {
                 deleteButton.setEnabled(true);
             }
             updateTextArea();
             updatePasswordFile();
         } else {
             deleteButton.setEnabled(false);
         }
     }//GEN-LAST:event_deleteButtonActionPerformed
 
     /**
      * Write RC4-encrypted entry data to file
      */
     private void updatePasswordFile() {
 
         if (authFile == null) {
             //String encryptedFileName = RC4Cipher(userName, masterPass);
             authFile = new File(RC4Cipher(userName, masterPass));
         }
         try {
             writeToFile();
         } catch (Exception e) {
             System.exit(66);
         }
     }
 
     public void writeToFile() throws IOException {
         OutputStream out = new FileOutputStream(authFile);
         Closeable stream = out;
         try {
             Writer writer = new OutputStreamWriter(out, charset);
             stream = writer;
             writer.write(writeHelper);
         } finally {
             stream.close();
         }
     }
 
     public String readFromFile(String file) {
 
         try {
             InputStream in = new FileInputStream(file);
             Closeable stream = in;
             Reader reader = new InputStreamReader(in, charset);//specify UTF8
             stream = reader;
             StringBuilder inputBuilder = new StringBuilder();
             char[] buffer = new char[1024];
             while (true) {
                 int readCount = reader.read(buffer);
                 if (readCount < 0) {
                     break;
                 }
                 inputBuilder.append(buffer, 0, readCount);
             }
             stream.close();
             return inputBuilder.toString();
 
         } catch (Exception e) {
         }
         return null;
     }
 
     /**
      * Add Entry From three inputs on Clamshell UI Then update passList, UI
      *
      * @param evt
      */
     private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
 
         String sn = sname.getText().trim();
         String un = uname.getText().trim();
         String pss = pass.getText().trim(); //String m = encryptAES(pss, masterPass); // cipher, key
         // create placeholder variables so that when decrypted, so that delimeters are not skipped
         if (sn.isEmpty()) {
             sn = placeholder;
         }
         if (un.isEmpty()) {
             un = placeholder;
         }
         if (pss.isEmpty()) {
             pss = placeholder;
         }
         // add new entry to passList
         Entry newentry = new Entry(sn, un, encryptAES(pss, masterPass)); //store password encrypted in system
        try{
         int i = 0;
         if (passList.isEmpty()) {
             passList.add(newentry);
         } else {
             while (newentry.getServiceName().compareToIgnoreCase(passList.get(i).getServiceName()) > 0 && i < passList.size()) {
                 i++;
             }
             passList.add(i, newentry);
         }
        }catch (IndexOutOfBoundsException e){
            passList.add(newentry);
        }
         updateTextArea();
         updatePasswordFile();
 
         sname.setText(""); // reset UI fields
         uname.setText("");
         pass.setText("");
 
         if (!passList.isEmpty()) {
             deleteButton.setEnabled(true); // now we can delete entries
         } else {
             deleteButton.setEnabled(false);
         }
 
     }//GEN-LAST:event_addButtonActionPerformed
 
     private void editEntryButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editEntryButtonActionPerformed
 
         int entryNum;
 
         try {
             entryNum = Integer.parseInt(JOptionPane.showInputDialog(null, "Input Entry Number:", "Edit Entry", JOptionPane.QUESTION_MESSAGE));
 
             if (entryNum >= 0 && entryNum < passList.size()) {
                 String password = JOptionPane.showInputDialog(null, "Enter New Password:", "Change Password", JOptionPane.QUESTION_MESSAGE);
 
                 if (!password.isEmpty()) {
 
                     Entry temper = passList.get(entryNum);
                     String sName = temper.getServiceName();
                     String uName = temper.getUserName();
                     temper = new Entry(sName, uName, encryptAES(password, masterPass));
 
                     passList.set(entryNum, temper);
 
                     if (!passList.isEmpty()) {
                         deleteButton.setEnabled(true); // now we can delete entries
                     } else {
                         deleteButton.setEnabled(false);
                     }
                 } else {
                     JOptionPane.showMessageDialog(null, "Password Entry Is Empty", "Invalid Input ", JOptionPane.ERROR_MESSAGE);
                 }
 
             } else {
                 JOptionPane.showMessageDialog(null, "Entry Number Out of Bounds", "Error", JOptionPane.ERROR_MESSAGE);
                 //JOptionPane.showMessageDialog(null, "Password Entry Is Empty", "Please Input Password ", JOptionPane.ERROR_MESSAGE);
             }
         } catch (Exception e) {
             JOptionPane.showMessageDialog(null, "Entry Number Out of Bounds", "Error", JOptionPane.ERROR_MESSAGE);
         }
 
 
         updateTextArea();
         updatePasswordFile();
     }//GEN-LAST:event_editEntryButtonActionPerformed
 
     /**
      * Convert user name input to RC4-encrypted string
      *
      * @return
      */
     private String RC4Cipher(String plain, String key) {
         return byteArrayToHexString(RC4(hexStringToByteArray(stringToHex(plain)), hexStringToByteArray(stringToHex(key))));
     }
 
     /**
      * RC4 Cipher in Bytes
      *
      * @param unlocked
      * @param key
      * @return
      */
     public byte[] RC4(byte[] plain, byte[] key) {
         byte[] S = new byte[256];
         for (int temp = 0; temp < 256; temp++) {
             S[temp] = (byte) temp;
         }
         int j = 0;
         byte swap;
         int i;
         for (i = 0; i < 256; i++)//KSA
         {
             j = (j + key[i % key.length] + S[i]) & 255;
             swap = S[i];
             S[i] = S[j];
             S[j] = swap;
         }
         i = j = 0;
         byte[] keystream = new byte[plain.length];
         for (int place = 0; place < plain.length; place++)//PRGA //Middle argument is how much stream to gen
         {
             i = (i + 1) & 255;
             j = (j + S[i]) & 255;
             swap = S[i];
             S[i] = S[j];
             S[j] = swap;
             keystream[place] = S[(S[i] + S[j]) & 255];
         }
         byte[] ciphertext = new byte[keystream.length];
         for (int place = 0; place < ciphertext.length; place++) {
             ciphertext[place] = (byte) (keystream[place] ^ plain[place]);
         }
         return ciphertext;
     }
 
     /**
      * Decrypt ciphertext password
      *
      * @param cipher - plaintext in hexadecimal
      * @param k - key as plain text
      * @return String
      */
     private String decryptAES(String cipher, String k) {
         // no need to convert to hexadecimal - input text is already hexadecimal
         // init vector already in hexadecimal - no need to convert
         String key = stringToHex(k);
         String decryptedPass = ""; // what we return from method
         // ensure key length to 64 chars hex
         if (key.length() < keylength) {
             while (key.length() < keylength) {
                 key += pad; // pad with zeros  
             }
         } else if (key.length() > keylength) {
             key = key.substring(0, keylength); // truncate
         }
         // now we decipher the padded text        
         int r; // remainder of characters need to be padded to end of hex block on plaintext
         int d; // number of rounds to run AES (number of 32 character blocks)
         int length = cipher.length();
 
         if (length < 32) {
             r = 32 - length;
             d = 1; // only one round required
         } else {
             r = length % 32; // r = how many we must pad to end 
             d = (int) length / 32; // d = number of AES rounds required to cycle thru to encipher all plaintext 
         }
 
         for (int i = length; i < length + r; ++i) {
             cipher += pad;
 
         }
 
         AES aes = new AES(key);
         for (int i = 0; i < d; ++i) {
             decryptedPass += aes.decrypt(cipher.substring(i * 32, i * 32 + 32));
         }// we are handed back 32 char hex string from AES 128 decryption
         // convert it back to plaintext
         aes = null;
         return byteToString(hexStringToByteArray(decryptedPass));
     }
 
     /**
      * Encrypt using AES 256
      *
      * @param plain - plaintext as plain text
      * @param k - key as plain text
      * @return String - encrypted text as hexadecimal
      */
     private String encryptAES(String p, String k) {
         // convert to hexadecimal
         String plain = stringToHex(p);
         String key = stringToHex(k);
         // init vector already in hexadecimal - no need to convert
         String encryptedPass = "";
         // ensure key length to 32 chars hex (32 * 4 = 128)
         // IV length is already set to 32 chars hex by Entry object
         if (key.length() < keylength) {
             while (key.length() < keylength) {
                 key += pad; // pad with zeros
             }
         } else if (key.length() > keylength) {
             key = key.substring(0, keylength); // truncate
         }
         // parse password data into & out of AES 32 hex at a time
         // first ensure that hex chars are multiple of 32
         int r; // remainder of characters need to be padded to end of hex block on plaintext
         int d; // number of rounds to run AES (number of 32 character blocks)
         int length = plain.length();
 
         if (length < 32) {
             r = 32 - length;
             d = 1; // only one round required
         } else {
             r = length % 32; // r = how many we must pad to end 
             d = (int) length / 32; // d = number of AES rounds required to cycle thru to encipher all plaintext 
         }
         for (int i = length; i < length + r; ++i) {
             plain += pad;
         }
         // now we encipher the padded text
         AES aes = new AES(key);
 
         for (int i = 0; i < d; ++i) // starts at round one
         {
             encryptedPass += aes.encrypt(plain.substring(i * 32, i * 32 + 32));
         }
         aes = null;
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
     // convert back to plaintext
 
     public String byteToString(byte[] in) {
         String output = "";
         for (int a = 0; a < in.length; a++) {
             output += (char) in[a];
         }
         return output;
     }
     // convert plaintext string to hexadecimal (length is always x2 plaintext)
 
     private String stringToHex(String s) {
 
         StringBuilder buf = new StringBuilder(200);
         for (char ch : s.toCharArray()) {
             buf.append(String.format("%02x", (int) ch));
         }
         return buf.toString();
         //return byteArrayToHexString(s.getBytes());
     }
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JButton addButton;
     private javax.swing.JButton deleteButton;
     private javax.swing.JButton editEntryButton;
     private javax.swing.JDialog jDialog1;
     private javax.swing.JScrollPane jScrollPane2;
     private javax.swing.JTextPane jTextPane2;
     private javax.swing.JTextField pass;
     private javax.swing.JTextField sname;
     private javax.swing.JTextField uname;
     // End of variables declaration//GEN-END:variables
 }
