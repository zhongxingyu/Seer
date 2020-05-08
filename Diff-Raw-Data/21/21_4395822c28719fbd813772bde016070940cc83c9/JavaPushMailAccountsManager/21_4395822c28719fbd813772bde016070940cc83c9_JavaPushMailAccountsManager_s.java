 package com.mofirouz.javapushmail.app;
 
 import com.mofirouz.javapushmail.JavaPushMailAccount;
 import com.mofirouz.javapushmail.app.ui.JavaPushMail;
 import com.mofirouz.notifier.SystemNotification;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.util.ArrayList;
 import java.util.Scanner;
 import javax.crypto.Cipher;
 import javax.crypto.CipherInputStream;
 import javax.crypto.CipherOutputStream;
 import javax.crypto.spec.DESKeySpec;
 import javax.crypto.spec.SecretKeySpec;
 
 /**
  *
  * @author Mo Firouz
  * @since 16/10/11
  */
 public abstract class JavaPushMailAccountsManager {
 
     private static final String ENCRYPTION_KEY = "mofirouz6874134567$££";
     private static final String ACCOUNT_FILE = "accounts.jpm";
     private MailAccountList accounts = new MailAccountList();
     private ArrayList<JavaPushMailNotifier> notifiers = new ArrayList<JavaPushMailNotifier>();
     private SystemNotification sysnot;
     private static boolean connected = false;
 
     public JavaPushMailAccountsManager() {
         sysnot = new SystemNotification();
         sysnot.setIcon(JavaPushMail.NOTIFICATION_ICON_FILE);
     }
 
     public synchronized void addAccount(final String name, final String server, final int port, final boolean useSSL, final String username, final String password) {
         final JavaPushMailAccount mail = new JavaPushMailAccount(name, server, port, useSSL) {
 
             @Override
             public void onError(Exception e) {
                 connected = false;
                 handleError(e);
             }
 
             @Override
             public void onConnect() {
                 connected = true;
                 onStateChange();
             }
 
             @Override
             public void onDisconnect() {
                 connected = false;
                 onStateChange();
             }
         };
         mail.setCredentials(username, password);
         accounts.add(mail);
         notifiers.add(new JavaPushMailNotifier(mail, sysnot));
         onModelChange();
         startMailDaemon(mail);
     }
 
     public synchronized void removeAccount(int remove) {
         accounts.get(remove).disconnect();
         accounts.removeAccount(accounts.get(remove));
     }
 
     public synchronized void reconnectAllDisconnected() {
         for (JavaPushMailAccount mail : accounts) {
             startMailDaemon(mail);
         }
     }
 
     public synchronized void disconnectAllAccounts() {
        for (JavaPushMailAccount mail : accounts) {
             mail.disconnect();
        }
     }
     
     public synchronized boolean allDisconnected() {
         for (JavaPushMailAccount mail : accounts) {
             if (mail.isConnected()) 
                 return false;
         }
         
         return true;
     }
     
     public JavaPushMailAccount getAccount(int i) {
         return accounts.get(i);
     }
 
     public JavaPushMailNotifier getNotifier(int i) {
         return notifiers.get(i);
     }
 
     public int countAccounts() {
         return accounts.size();
     }
 
     public SystemNotification getSystemNotification() {
         return sysnot;
     }
 
     public static boolean isConnected() {
         return connected;
     }
 
     private void startMailDaemon(JavaPushMailAccount mail) {
         Thread t = new Thread(mail);
         t.setName("JPM-" + mail.getAccountName());
         t.start();
     }
 
     @Deprecated
     public void readAccounts(String filepath) {
         if ((new File(ACCOUNT_FILE)).exists() && accounts.size() > 0)
             return;
 
         try {
             BufferedReader in = new BufferedReader(new FileReader(filepath));
             String str;
             while ((str = in.readLine()) != null) {
                 if (!str.startsWith("##")) {
                     String[] line = str.split(",");
                     addAccount(line[0].replaceAll("\"", "").trim(), line[1].replaceAll("\"", "").trim(), Integer.parseInt(line[2].replaceAll("\"", "").trim()), Boolean.parseBoolean(line[3].replaceAll("\"", "").trim()), line[4].replaceAll("\"", "").trim(), line[5].replaceAll("\"", "").trim());
                 }
             }
             in.close();
         } catch (Exception e) {
             System.err.println(filepath + " was not found. Ignoring...");
         }
     }
 
     public boolean saveAccounts() {
         try {
             File saveFile = new File(ACCOUNT_FILE);
             if (saveFile.exists())
                 saveFile.delete();
             
             if (accounts.isEmpty())
                 return true;
             
             FileOutputStream outputStream = new FileOutputStream(saveFile, false);
             SecretKeySpec key = new SecretKeySpec(new DESKeySpec(ENCRYPTION_KEY.getBytes()).getKey(), "DES");
             Cipher cipher = Cipher.getInstance("DES");
             cipher.init(Cipher.ENCRYPT_MODE, key);
             CipherOutputStream encryptedOutput = new CipherOutputStream(outputStream, cipher);
             String saveData = "";
 
             for (JavaPushMailAccount mail : accounts) {
                 saveData += "\"" + mail.getAccountName() + "\",\"" + mail.getServerAddress() + "\"," + mail.getServerPort() + ",\"" + mail.isSSL() + "\",\"" + mail.getUsername() + "\",\"" + mail.getPassword() + "\"";
                 saveData += "\n";
             }
             encryptedOutput.write(saveData.getBytes());
             encryptedOutput.flush();
             encryptedOutput.close();
             return true;
 
         } catch (Exception e) {
             return false;
         }
     }
 
     public boolean loadAccounts() {
         try {
             File file = new File(ACCOUNT_FILE);
             if (!file.exists())
                 return false;
             String data = decryptSave(file);
             Scanner sc = new Scanner(data);
 
             while (sc.hasNextLine()) {
                 String str = sc.nextLine();
                 String[] line = str.replaceAll("\"", "").split(",");
                 addAccount(line[0].trim(), line[1].trim(), Integer.parseInt(line[2].trim()), Boolean.parseBoolean(line[3].trim()), line[4].trim(), line[5].trim());
             }
 
         } catch (Exception e) {
         }
         return false;
     }
 
     private String decryptSave(File file) {
         String bufferData;
         ArrayList<Integer> ali = new ArrayList<Integer>();
         byte[] by;
 
         int intRead = 0;
         try {
             FileInputStream inputStream = new FileInputStream(file);
             SecretKeySpec key = new SecretKeySpec(new DESKeySpec(ENCRYPTION_KEY.getBytes()).getKey(), "DES");
             Cipher cipher = Cipher.getInstance("DES");
             cipher.init(Cipher.DECRYPT_MODE, key);
             CipherInputStream dec = new CipherInputStream(inputStream, cipher); //Decrypted input...
 
             while (intRead != -1) {
                 intRead = dec.read();
                 if (intRead != -1) {
                     ali.add(intRead);
                 }
             }
             by = new byte[ali.size()];
             for (int i = 0; i < ali.size(); i++) {
                 by[i] = (Byte.decode("" + (Integer) ali.get(i)));
             }
             bufferData = new String(by);
             return bufferData;
         } catch (Exception ex) {
             ex.printStackTrace();
         }
         return "";
     }
 
     public abstract void handleError(Exception ex);
 
     public abstract void onModelChange();
 
     public abstract void onStateChange();
 
     class MailAccountList extends ArrayList<JavaPushMailAccount> {
 
         /**
          * Normal behaviour + Calls the onModelChange
          * @param e
          * @return 
          */
         @Override
         public boolean add(JavaPushMailAccount e) {
             boolean add = super.add(e);
             onModelChange();
             return add;
         }
 
         /**
          * Normal behaviour + Calls the onModelChange
          * @param e
          * @return 
          */
         @Override
         public JavaPushMailAccount remove(int i) {
             JavaPushMailAccount removed = super.remove(i);
             onModelChange();
             return removed;
         }
 
         public void removeAccount(JavaPushMailAccount e) {
             super.remove(e);
             onModelChange();
         }
     }
 }
