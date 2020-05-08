 package com.scurab.java.ftpleechergui;
 
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 import com.scurab.java.ftpleecher.FTPConnection;
 import com.scurab.java.ftpleecher.FTPLeechMaster;
 import com.scurab.java.ftpleechergui.controller.ApplicationController;
 import com.scurab.java.ftpleechergui.model.Settings;
 import org.apache.commons.io.IOUtils;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.Comparator;
 import java.util.ResourceBundle;
 
 /**
  * Base application class
  */
 public class Application {
 
     private static Application sSelf;
 
     private ResourceBundle mLables;
 
     private ApplicationController mAppController;
 
     private FTPLeechMaster mMaster;
 
     public static void main(String[] args) {
         sSelf = new Application();
         sSelf.start();
     }
 
     public Application() {
         if (sSelf != null) {
             throw new IllegalStateException("App object is already created");
         }
     }
 
     public static Application getInstance() {
         return sSelf;
     }
 
     /**
      * ResourceBundle for lables
      *
      * @return
      */
     public static ResourceBundle getLabels() {
         return sSelf.mLables;
     }
 
     /**
      * Start application
      */
     public void start() {
         onLoadResources();
         mSettings = onLoadSettings();
         if(mSettings == null){
             setSettings(new Settings());
         }
         mSavedConnections = onLoadSavedConnections();
         mMaster = new FTPLeechMaster();
        mMaster.setWorkingThreads(mSettings.threads);
 
         mAppController = new ApplicationController();
         mAppController.start();
     }
 
     protected void onLoadResources() {
         mLables = ResourceBundle.getBundle("Labels");
     }
 
     /**
      * Shows message in mainwindows status bar
      *
      * @param msg
      * @param type
      */
     public void showStatusBarMessage(String msg, int type) {
         mAppController.showStatusBarMessage(msg, type);
     }
 
     /**
      * Set visibility for indtereminate progress bar in status bar
      *
      * @param value
      */
     public void showProgress(boolean value) {
         mAppController.showProgress(value);
     }
 
     /**
      * returns singleton of {@link FTPLeechMaster}
      *
      * @return
      */
     public FTPLeechMaster getMaster() {
         return mMaster;
     }
 
     public FTPConnection[] getConnections(){
         return mSavedConnections;
     }
 
     public Settings getSettings(){
         return mSettings;
     }
 
     public void setSettings(Settings s){
         mSettings = s;
         try {
             save(SETTINGS_FILE, s);
         } catch (IOException e) {
             e.printStackTrace();
             showStatusBarMessage("Unable to save settings", 0);
         }
     }
 
     public void setConnections(FTPConnection... conns){
         mSavedConnections = conns;
         if(conns != null && conns.length > 0){
             Arrays.sort(mSavedConnections, new ConnSorter<FTPConnection>());
         }
         try {
             save(CONNECTIONS_FILE, conns);
         } catch (IOException e) {
             e.printStackTrace();
             showStatusBarMessage("Unable to save connection", 0);
         }
     }
 
     //region Settings and connections
 
     public static final String CONNECTIONS_FILE = "connections.json";
 
     public static final String SETTINGS_FILE = "settings.json";
 
     private FTPConnection[] mSavedConnections;
 
     private Settings mSettings;
 
     private static final Gson sGson = new GsonBuilder().setPrettyPrinting().create();
     /**
      * Load saved connections from connections.json file
      *
      * @return
      */
     protected FTPConnection[] onLoadSavedConnections() {
         FTPConnection[] result = null;
         try {
             result = loadJson(CONNECTIONS_FILE, FTPConnection[].class);
         } catch (Exception e) {
             result = new FTPConnection[0];
         }
         return result;
     }
 
     /**
      * Load settings from settings.json
      * @return
      */
     private Settings onLoadSettings() {
         Settings result = null;
         try {
             result = loadJson(SETTINGS_FILE, Settings.class);
         } catch (Exception e) {
             e.printStackTrace();
         }
         return result;
     }
 
     /**
      * Save collection to json file
      *
      * @param file
      * @param value
      * @throws java.io.IOException
      */
     public void save(String file, Object value) throws IOException {
         File f = new File(file);
         f.delete();
         FileOutputStream fos = new FileOutputStream(f);
         String json = sGson.toJson(value);
         fos.write(json.getBytes());
         fos.close();
     }
 
     public static <T> T loadJson(String file, Class<T> clazz){
         T result = null;
         try {
             File f = new File(file);
             if (f.exists() && f.isFile()) {
                 String values = IOUtils.toString(new FileInputStream(f));
                 result = sGson.fromJson(values, clazz);
             }
         } catch (Exception e) {
             e.printStackTrace();
         }
         return result;
     }
 
 
     private static class ConnSorter<T extends FTPConnection> implements Comparator<FTPConnection>{
 
         @Override
         public int compare(FTPConnection o1, FTPConnection o2) {
             return o1.server.compareTo(o2.server);
         }
     }
 }
 
