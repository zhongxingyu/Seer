 package com.scurab.java.ftpleechergui.controller;
 
 import com.scurab.java.ftpleecher.FTPConnection;
 import com.scurab.java.ftpleecher.FTPContext;
 import com.scurab.java.ftpleecher.FTPFactory;
 import com.scurab.java.ftpleecher.FTPLeechMaster;
 import com.scurab.java.ftpleechergui.TextUtils;
 import com.scurab.java.ftpleechergui.window.MainWindow;
 import com.scurab.java.ftpleechergui.window.OpenConnectionDialog;
 import org.apache.commons.net.ftp.FTPClient;
 import org.apache.commons.net.ftp.FTPFile;
 import org.apache.commons.net.time.TimeTCPClient;
 
 import javax.imageio.ImageIO;
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.io.IOException;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Joe Scurab
  * Date: 12.2.13
  * Time: 20:09
  * To change this template use File | Settings | File Templates.
  */
 public class MainWindowController extends BaseController {
 
    public static final int DELAY = 2000;

     private MainWindow mWindow;
 
     private LocalStorageController mStorageController;
 
     private FTPController mFtpController;
 
     private DownloadController mDownloadController;
 
     private FTPClient mFtpClient;
 
     private FTPLeechMaster mMaster;
 
     public MainWindowController(MainWindow window) {
         mWindow = window;
         try {
             mWindow.setIconImage(ImageIO.read(getClass().getResourceAsStream("/assets/appicon.png")));
             mWindow.setTitle("FTP Leecher");
         } catch (IOException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
         bind();
         mWindow.setVisible(true);
     }
 
     private void bind(){
         AbstractAction action = new AbstractAction() {
             @Override
             public void actionPerformed(ActionEvent e) {
                onAction(e.getSource(), e.getActionCommand());
             }
         };
         mWindow.getOpenConnection().addActionListener(action);
         mWindow.getDisconnect().addActionListener(action);
         mWindow.getDownload().addActionListener(action);
     }
 
     public void onAction(Object source, String action){
         if("OpenConnection".equals(action)){
             //show open ftp connection dialog
             OpenConnectionDialog ocd = new OpenConnectionDialog(new AbstractAction() {
                 @Override
                 public void actionPerformed(ActionEvent e) {
                     OpenConnectionDialog o = (OpenConnectionDialog) e.getSource();
                     onOpenConnection(o.getValues());
                 }
             });
             FTPConnection[] saved = getSavedConnections();
             if(saved != null && saved.length > 0){
                 ocd.initValues(saved[0]);
             }
             ocd.setVisible(true);
         }else if("Disconnect".equals(action)){
             closeConnection();
         }else if("Download".equals(action)){
             onDownload();
         }
     }
 
     private void onDownload() {
         try {
             FTPFile[] selection = mFtpController.getSelectedItems();
            if (selection.length == 0 || selection[0] == null) {
                 throw new Exception(getResourceLabel("NoSelectedItem"));
             } else {
                 File to = mStorageController.getCurrentFolder();
                 if(to == null){
                     throw new Exception(getResourceLabel("SelectDestination"));
                 }
 
                 String pwd = getFtpPwd();
                 mDownloadController.onDownloadItem(pwd, selection[0], to.getAbsolutePath());
             }
         } catch (Exception e) {
             showError(e.getMessage());
         }
     }
 
     /**
      * return ftp current folder
      * @return
      * @throws IOException
      */
     private String getFtpPwd() throws IOException {
         String ftpPwd = null;
 
         StringBuilder pwd = new StringBuilder(mFtpClient.printWorkingDirectory());
         if(pwd.charAt(pwd.length()-1) == '\"'){
             pwd.setLength(pwd.length()-1);
         }
         if(pwd.charAt(0) == '\"'){
             ftpPwd = pwd.substring(1);
         }
 
         if(ftpPwd == null){
             ftpPwd = pwd.toString();
         }
         return ftpPwd;
     }
 
     /**
      * Open new connection
      * @param config
      */
     public void onOpenConnection(FTPConnection config) {
         closeConnection();
         try {
           mFtpClient = FTPFactory.openFtpClient(config);
           mFtpController.setFTPClient(mFtpClient);
           saveConnections(config);
           mDownloadController.setConfig(config);
         } catch (Exception e) {
             showError(e.getMessage());
         }
     }
 
     private void closeConnection(){
         if (mFtpClient != null) {
             try {
                 mFtpClient.disconnect();
                 mFtpClient = null;
             } catch (IOException e) {
                 e.printStackTrace();
                 showStatusBarMessage(e.getMessage());
                 //ignore it
             }
         }
         mFtpController.setFTPClient(null);
     }
 
     public void onCreate() {
         mStorageController = new LocalStorageController(mWindow.getLocalStorage());
         mFtpController = new FTPController(mWindow.getFtpStorage());
         mMaster = application().getMaster();
         mDownloadController = new DownloadController(mWindow.getQueue(), mMaster);
        Timer t = new Timer(DELAY, new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 mWindow.getGlobalSpeed().setText(TextUtils.getSpeedReadable(mMaster.getCurrentDownloadSpeed()));
             }
         });
         t.start();
     }
 
     @Override
     public void showStatusBarMessage(String s) {
         super.showStatusBarMessage(s);
     }
 
     @Override
     public void showStatusBarMessage(String s, int type) {
         mWindow.getStatusLabel().setText(s);
     }
 
     @Override
     public void showProgress(boolean value) {
         mWindow.getProgressBar().setVisible(value);
     }
 
     @Override
     public Component getView() {
         return mWindow;
     }
 }
