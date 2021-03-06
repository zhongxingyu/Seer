 /*
  * Copyright (C) 2008 The Android Open Source Project
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.android.sdklib.avd;
 
 import com.android.prefs.AndroidLocation;
 import com.android.prefs.AndroidLocation.AndroidLocationException;
 import com.android.sdklib.IAndroidTarget;
 import com.android.sdklib.ISdkLog;
 import com.android.sdklib.SdkConstants;
 import com.android.sdklib.SdkManager;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeSet;
 import java.util.Map.Entry;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * Android Virtual Device Manager to manage AVDs.
  */
 public final class AvdManager {
     
     public static final String AVD_FOLDER_EXTENSION = ".avd";
 
     private final static String AVD_INFO_PATH = "path";
     private final static String AVD_INFO_TARGET = "target";
     
     /**
      * AVD/config.ini key name representing the SDK-relative path of the skin folder, if any,
      * or a 320x480 like constant for a numeric skin size.
      * 
      * @see #NUMERIC_SKIN_SIZE
      */
     public final static String AVD_INI_SKIN_PATH = "skin.path";
     /**
      * AVD/config.ini key name representing an UI name for the skin.
      * This config key is ignored by the emulator. It is only used by the SDK manager or
      * tools to give a friendlier name to the skin.
      * If missing, use the {@link #AVD_INI_SKIN_PATH} key instead.
      */
     public final static String AVD_INI_SKIN_NAME = "skin.name";
     /**
      * AVD/config.ini key name representing the path to the sdcard file.
      * If missing, the default name "sdcard.img" will be used for the sdcard, if there's such
      * a file.
      * 
      * @see #SDCARD_IMG
      */
     public final static String AVD_INI_SDCARD_PATH = "sdcard.path";
     /**
      * AVD/config.ini key name representing the size of the SD card.
      * This property is for UI purposes only. It is not used by the emulator.
      * 
      * @see #SDCARD_SIZE_PATTERN
      */
     public final static String AVD_INI_SDCARD_SIZE = "sdcard.size";
     /**
      * AVD/config.ini key name representing the first path where the emulator looks
      * for system images. Typically this is the path to the add-on system image or
      * the path to the platform system image if there's no add-on.
      * <p/>
      * The emulator looks at {@link #AVD_INI_IMAGES_1} before {@link #AVD_INI_IMAGES_2}.
      */
     public final static String AVD_INI_IMAGES_1 = "image.sysdir.1";
     /**
      * AVD/config.ini key name representing the second path where the emulator looks
      * for system images. Typically this is the path to the platform system image.
      * 
      * @see #AVD_INI_IMAGES_1
      */
     public final static String AVD_INI_IMAGES_2 = "image.sysdir.2";
 
     /**
      * Pattern to match pixel-sized skin "names", e.g. "320x480".
      */
     public final static Pattern NUMERIC_SKIN_SIZE = Pattern.compile("[0-9]{2,}x[0-9]{2,}");
 
     
     private final static String USERDATA_IMG = "userdata.img";
     private final static String CONFIG_INI = "config.ini";
     private final static String SDCARD_IMG = "sdcard.img";
 
     private final static String INI_EXTENSION = ".ini";
     private final static Pattern INI_NAME_PATTERN = Pattern.compile("(.+)\\" + INI_EXTENSION + "$",
             Pattern.CASE_INSENSITIVE);
 
     /**
      * Pattern for matching SD Card sizes, e.g. "4K" or "16M".
      */
     private final static Pattern SDCARD_SIZE_PATTERN = Pattern.compile("\\d+[MK]?");
 
     /** An immutable structure describing an Android Virtual Device. */
     public static final class AvdInfo {
         private final String mName;
         private final String mPath;
         private final IAndroidTarget mTarget;
         private final Map<String, String> mProperties;
         private final String mError;
 
         /**
          * Creates a new valid AVD info. Values are immutable. 
          * <p/>
          * Such an AVD is available and can be used.
          * The error string is set to null.
          * 
          * @param name The name of the AVD (for display or reference)
          * @param path The path to the config.ini file
          * @param target The target. Cannot be null.
          * @param properties The property map. Cannot be null.
          */
         public AvdInfo(String name, String path, IAndroidTarget target,
                 Map<String, String> properties) {
             this(name, path, target, properties, null /*error*/);
         }
 
         /**
          * Creates a new <em>invalid</em> AVD info. Values are immutable. 
          * <p/>
          * Such an AVD is not complete and cannot be used.
          * The error string must be non-null.
          * 
          * @param name The name of the AVD (for display or reference)
          * @param path The path to the config.ini file
          * @param target The target. Can be null.
          * @param properties The property map. Can be null.
          * @param error The error describing why this AVD is invalid. Cannot be null.
          */
         public AvdInfo(String name, String path, IAndroidTarget target,
                 Map<String, String> properties, String error) {
             mName = name;
             mPath = path;
             mTarget = target;
             mProperties = properties;
             mError = error;
         }
 
         /** Returns the name of the AVD. */
         public String getName() {
             return mName;
         }
 
         /** Returns the path of the AVD data directory. */
         public String getPath() {
             return mPath;
         }
 
         /** Returns the target of the AVD. */
         public IAndroidTarget getTarget() {
             return mTarget;
         }
 
         /** Returns the error describing why an AVD failed to load. Always null for valid AVDs. */
         public String getError() {
             return mError;
         }
 
         /** 
          * Helper method that returns the .ini {@link File} for a given AVD name. 
          * @throws AndroidLocationException if there's a problem getting android root directory.
          */
         public static File getIniFile(String name) throws AndroidLocationException {
             String avdRoot;
             avdRoot = AndroidLocation.getFolder() + AndroidLocation.FOLDER_AVD;
             return new File(avdRoot, name + INI_EXTENSION);
         }
         
         /** 
          * Returns the .ini {@link File} for this AVD. 
          * @throws AndroidLocationException if there's a problem getting android root directory.
          */
         public File getIniFile() throws AndroidLocationException {
             return getIniFile(mName);
         }
         
         /**
          * Returns a map of properties for the AVD.
          */
         public Map<String, String> getProperties() {
             return mProperties;
         }
     }
 
     private final ArrayList<AvdInfo> mAvdList = new ArrayList<AvdInfo>();
     private ISdkLog mSdkLog;
     private final SdkManager mSdk;
 
     public AvdManager(SdkManager sdk, ISdkLog sdkLog) throws AndroidLocationException {
         mSdk = sdk;
         mSdkLog = sdkLog;
         buildAvdList(mAvdList);
     }
 
     /**
      * Returns the existing AVDs.
      * @return a newly allocated array containing all the AVDs.
      */
     public AvdInfo[] getAvds() {
         return mAvdList.toArray(new AvdInfo[mAvdList.size()]);
     }
 
     /**
      * Returns the {@link AvdInfo} matching the given <var>name</var>.
      * @return the matching AvdInfo or <code>null</code> if none were found.
      */
     public AvdInfo getAvd(String name) {
         for (AvdInfo info : mAvdList) {
             if (info.getName().equals(name)) {
                 return info;
             }
         }
         
         return null;
     }
     
     /**
      * Reloads the AVD list.
      * @throws AndroidLocationException if there was an error finding the location of the
      * AVD folder.
      */
     public void reloadAvds() throws AndroidLocationException {
         // build the list in a temp list first, in case the method throws an exception.
         // It's better than deleting the whole list before reading the new one.
         ArrayList<AvdInfo> list = new ArrayList<AvdInfo>();
         buildAvdList(list);
         mAvdList.clear();
         mAvdList.addAll(list);
     }
 
     /**
      * Creates a new AVD. It is expected that there is no existing AVD with this name already.
      * @param avdFolder the data folder for the AVD. It will be created as needed.
      * @param name the name of the AVD
      * @param target the target of the AVD
      * @param skinName the name of the skin. Can be null. Must have been verified by caller.
      * @param sdcard the parameter value for the sdCard. Can be null. This is either a path to
      * an existing sdcard image or a sdcard size (\d+, \d+K, \dM).
      * @param hardwareConfig the hardware setup for the AVD
      * @param removePrevious If true remove any previous files.
      */
     public AvdInfo createAvd(File avdFolder, String name, IAndroidTarget target,
             String skinName, String sdcard, Map<String,String> hardwareConfig,
             boolean removePrevious, ISdkLog log) {
         
         File iniFile = null;
         boolean needCleanup = false;
         try {
             if (avdFolder.exists()) {
                 if (removePrevious) {
                     // AVD already exists and removePrevious is set, try to remove the
                     // directory's content first (but not the directory itself).
                     recursiveDelete(avdFolder);
                 } else {
                     // AVD shouldn't already exist if removePrevious is false.
                     if (log != null) {
                         log.error(null,
                                 "Folder %1$s is in the way. Use --force if you want to overwrite.",
                                 avdFolder.getAbsolutePath());
                     }
                     return null;
                 }
             } else {
                 // create the AVD folder.
                 avdFolder.mkdir();
             }
 
             // actually write the ini file
             iniFile = createAvdIniFile(name, avdFolder, target);
 
             // writes the userdata.img in it.
             String imagePath = target.getPath(IAndroidTarget.IMAGES);
             File userdataSrc = new File(imagePath, USERDATA_IMG);
             
             if (userdataSrc.exists() == false && target.isPlatform() == false) {
                 imagePath = target.getParent().getPath(IAndroidTarget.IMAGES);
                 userdataSrc = new File(imagePath, USERDATA_IMG);
             }
             
             if (userdataSrc.exists() == false) {
                 log.error(null, "Unable to find a '%1$s' file to copy into the AVD folder.",
                         USERDATA_IMG);
                 needCleanup = true;
                 return null;
             }
             
             FileInputStream fis = new FileInputStream(userdataSrc);
             
             File userdataDest = new File(avdFolder, USERDATA_IMG);
             FileOutputStream fos = new FileOutputStream(userdataDest);
             
             byte[] buffer = new byte[4096];
             int count;
             while ((count = fis.read(buffer)) != -1) {
                 fos.write(buffer, 0, count);
             }
             
             fos.close();
             fis.close();
             
             // Config file.
             HashMap<String, String> values = new HashMap<String, String>();
 
             // First the image folders of the target itself
             imagePath = getImageRelativePath(target, log);
             if (imagePath == null) {
                 needCleanup = true;
                 return null;
             }
             
             values.put(AVD_INI_IMAGES_1, imagePath);
             
             // If the target is an add-on we need to add the Platform image as a backup.
             IAndroidTarget parent = target.getParent();
             if (parent != null) {
                 imagePath = getImageRelativePath(parent, log);
                 if (imagePath == null) {
                     needCleanup = true;
                     return null;
                 }
 
                 values.put(AVD_INI_IMAGES_2, imagePath);
             }
             
             
             // Now the skin.
             if (skinName == null) {
                 skinName = target.getDefaultSkin();
             }
 
             if (NUMERIC_SKIN_SIZE.matcher(skinName).matches()) {
                 // Skin name is an actual screen resolution.
                 // Set skin.name for display purposes in the AVD manager and
                 // set skin.path for use by the emulator.
                 values.put(AVD_INI_SKIN_NAME, skinName);
                 values.put(AVD_INI_SKIN_PATH, skinName);
             } else {
                 // get the path of the skin (relative to the SDK)
                 // assume skin name is valid
                 String skinPath = getSkinRelativePath(skinName, target, log);
                 if (skinPath == null) {
                     needCleanup = true;
                     return null;
                 }
 
                 values.put(AVD_INI_SKIN_PATH, skinPath);
                 values.put(AVD_INI_SKIN_NAME, skinName);
             }
 
             if (sdcard != null) {
                 File sdcardFile = new File(sdcard);
                 if (sdcardFile.isFile()) {
                     // sdcard value is an external sdcard, so we put its path into the config.ini
                     values.put(AVD_INI_SDCARD_PATH, sdcard);
                 } else {
                     // Sdcard is possibly a size. In that case we create a file called 'sdcard.img'
                     // in the AVD folder, and do not put any value in config.ini.
                     
                     // First, check that it matches the pattern for sdcard size
                     Matcher m = SDCARD_SIZE_PATTERN.matcher(sdcard);
                     if (m.matches()) {
                         // create the sdcard.
                         sdcardFile = new File(avdFolder, SDCARD_IMG);
                         String path = sdcardFile.getAbsolutePath();
                         
                         // execute mksdcard with the proper parameters.
                         File toolsFolder = new File(mSdk.getLocation(), SdkConstants.FD_TOOLS);
                         File mkSdCard = new File(toolsFolder, SdkConstants.mkSdCardCmdName());
                         
                         if (mkSdCard.isFile() == false) {
                             log.error(null, "'%1$s' is missing from the SDK tools folder.",
                                     mkSdCard.getName());
                             needCleanup = true;
                             return null;
                         }
                         
                         if (createSdCard(mkSdCard.getAbsolutePath(), sdcard, path, log) == false) {
                             needCleanup = true;
                             return null; // mksdcard output has already been displayed, no need to
                                          // output anything else.
                         }
                         
                         // add a property containing the size of the sdcard for display purpose
                         // only when the dev does 'android list avd'
                         values.put(AVD_INI_SDCARD_SIZE, sdcard);
                     } else {
                         log.error(null,
                                 "'%1$s' is not recognized as a valid sdcard value.\n" +
                                 "Value should be:\n" +
                                 "1. path to an sdcard.\n" +
                                 "2. size of the sdcard to create: <size>[K|M]",
                                 sdcard);
                         needCleanup = true;
                         return null;
                     }
                 }
             }
 
             if (hardwareConfig != null) {
                 values.putAll(hardwareConfig);
             }
 
             File configIniFile = new File(avdFolder, CONFIG_INI);
             createConfigIni(configIniFile, values);
             
             if (log != null) {
                 if (target.isPlatform()) {
                     log.printf("Created AVD '%1$s' based on %2$s\n", name, target.getName());
                 } else {
                     log.printf("Created AVD '%1$s' based on %2$s (%3$s)\n", name, target.getName(),
                                target.getVendor());
                 }
             }
             
             // create the AvdInfo object, and add it to the list
             AvdInfo avdInfo = new AvdInfo(name, avdFolder.getAbsolutePath(), target, values);
             
             mAvdList.add(avdInfo);
             
             return avdInfo;
         } catch (AndroidLocationException e) {
             if (log != null) {
                 log.error(e, null);
             }
         } catch (IOException e) {
             if (log != null) {
                 log.error(e, null);
             }
         } finally {
             if (needCleanup) {
                 if (iniFile != null && iniFile.exists()) {
                     iniFile.delete();
                 }
                 
                 recursiveDelete(avdFolder);
                 avdFolder.delete();
             }
         }
         
         return null;
     }
 
     /**
      * Returns the path to the target images folder as a relative path to the SDK.
      */
     private String getImageRelativePath(IAndroidTarget target, ISdkLog log) {
         String imageFullPath = target.getPath(IAndroidTarget.IMAGES);
 
         // make this path relative to the SDK location
         String sdkLocation = mSdk.getLocation();
         if (imageFullPath.startsWith(sdkLocation) == false) {
             // this really really should not happen.
             log.error(null, "Target location is not inside the SDK.");
             assert false;
             return null;
         }
 
         imageFullPath = imageFullPath.substring(sdkLocation.length());
         if (imageFullPath.charAt(0) == File.separatorChar) {
             imageFullPath = imageFullPath.substring(1);
         }
         return imageFullPath;
     }
     
     /**
      * Returns the path to the skin, as a relative path to the SDK.
      */
     private String getSkinRelativePath(String skinName, IAndroidTarget target, ISdkLog log) {
         // first look to see if the skin is in the target
         
         String path = target.getPath(IAndroidTarget.SKINS);
         File skin = new File(path, skinName);
         
         if (skin.exists() == false && target.isPlatform() == false) {
             target = target.getParent();
 
             path = target.getPath(IAndroidTarget.SKINS);
             skin = new File(path, skinName);
         }
         
         // skin really does not exist!
         if (skin.exists() == false) {
             log.error(null, "Skin '%1$s' does not exist.", skinName);
             return null;
         }
         
         // get the skin path
         path = skin.getAbsolutePath();
 
         // make this path relative to the SDK location
         String sdkLocation = mSdk.getLocation();
         if (path.startsWith(sdkLocation) == false) {
             // this really really should not happen.
             log.error(null, "Target location is not inside the SDK.");
             assert false;
             return null;
         }
 
         path = path.substring(sdkLocation.length());
         if (path.charAt(0) == File.separatorChar) {
             path = path.substring(1);
         }
         return path;
     }
 
     /**
      * Creates the ini file for an AVD.
      * 
      * @param name of the AVD.
      * @param avdFolder path for the data folder of the AVD.
      * @param target of the AVD.
      * @throws AndroidLocationException if there's a problem getting android root directory.
      * @throws IOException if {@link File#getAbsolutePath()} fails.
      */
     private File createAvdIniFile(String name, File avdFolder, IAndroidTarget target)
             throws AndroidLocationException, IOException {
         HashMap<String, String> values = new HashMap<String, String>();
         File iniFile = AvdInfo.getIniFile(name);
         values.put(AVD_INFO_PATH, avdFolder.getAbsolutePath());
         values.put(AVD_INFO_TARGET, target.hashString());
         createConfigIni(iniFile, values);
         
         return iniFile;
     }
     
     /**
      * Creates the ini file for an AVD.
      * 
      * @param info of the AVD.
      * @throws AndroidLocationException if there's a problem getting android root directory.
      * @throws IOException if {@link File#getAbsolutePath()} fails.
      */
     private File createAvdIniFile(AvdInfo info) throws AndroidLocationException, IOException {
         return createAvdIniFile(info.getName(), new File(info.getPath()), info.getTarget());
     }
 
     /**
      * Actually deletes the files of an existing AVD.
      * <p/>
      * This also remove it from the manager's list, The caller does not need to
      * call {@link #removeAvd(AvdInfo)} afterwards.
      * <p/>
      * This method is designed to somehow work with an unavailable AVD, that is an AVD that
      * could not be loaded due to some error. That means this method still tries to remove
      * the AVD ini file or its folder if it can be found. An error will be output if any of
      * these operations fail.
      * 
      * @param avdInfo the information on the AVD to delete
      */
     public void deleteAvd(AvdInfo avdInfo, ISdkLog log) {
         try {
             boolean error = false;
             
             File f = avdInfo.getIniFile();
             if (f != null && f.exists()) {
                 log.warning("Deleting file %1$s", f.getCanonicalPath());
                 if (!f.delete()) {
                     log.error(null, "Failed to delete %1$s", f.getCanonicalPath());
                     error = true;
                 }
             }
 
             String path = avdInfo.getPath();
             if (path != null) {
                 f = new File(path);
                 if (f.exists()) {
                     log.warning("Deleting folder %1$s", f.getCanonicalPath());
                     recursiveDelete(f);
                     if (!f.delete()) {
                         log.error(null, "Failed to delete %1$s", f.getCanonicalPath());
                         error = true;
                     }
                 }
             }
 
             removeAvd(avdInfo);
 
             if (error) {
                 log.printf("AVD '%1$s' deleted with errors. See warnings above.",
                         avdInfo.getName());
             } else {
                 log.printf("AVD '%1$s' deleted.", avdInfo.getName());
             }
 
         } catch (AndroidLocationException e) {
             log.error(e, null);
         } catch (IOException e) {
             log.error(e, null);
         }
     }
     
     /**
      * Moves and/or rename an existing AVD and its files.
      * This also change it in the manager's list.
      * <p/>
      * The caller should make sure the name or path given are valid, do not exist and are
      * actually different than current values.
      * 
      * @param avdInfo the information on the AVD to move.
      * @param newName the new name of the AVD if non null.
      * @param paramFolderPath the new data folder if non null.
      * @return True if the move succeeded or there was nothing to do.
      *         If false, this method will have had already output error in the log. 
      */
     public boolean moveAvd(AvdInfo avdInfo, String newName, String paramFolderPath, ISdkLog log) {
         
         try {
             if (paramFolderPath != null) {
                 File f = new File(avdInfo.getPath());
                 log.warning("Moving '%1$s' to '%2$s'.", avdInfo.getPath(), paramFolderPath);
                 if (!f.renameTo(new File(paramFolderPath))) {
                     log.error(null, "Failed to move '%1$s' to '%2$s'.",
                             avdInfo.getPath(), paramFolderPath);
                     return false;
                 }
     
                 // update AVD info
                 AvdInfo info = new AvdInfo(avdInfo.getName(), paramFolderPath, avdInfo.getTarget(),
                         avdInfo.getProperties());
                 mAvdList.remove(avdInfo);
                 mAvdList.add(info);
                 avdInfo = info;
 
                 // update the ini file
                 createAvdIniFile(avdInfo);
             }
 
             if (newName != null) {
                 File oldIniFile = avdInfo.getIniFile();
                 File newIniFile = AvdInfo.getIniFile(newName);
                 
                 log.warning("Moving '%1$s' to '%2$s'.", oldIniFile.getPath(), newIniFile.getPath());
                 if (!oldIniFile.renameTo(newIniFile)) {
                     log.error(null, "Failed to move '%1$s' to '%2$s'.", 
                             oldIniFile.getPath(), newIniFile.getPath());
                     return false;
                 }
 
                 // update AVD info
                 AvdInfo info = new AvdInfo(newName, avdInfo.getPath(), avdInfo.getTarget(),
                         avdInfo.getProperties());
                 mAvdList.remove(avdInfo);
                 mAvdList.add(info);
             }
 
             log.printf("AVD '%1$s' moved.", avdInfo.getName());
 
         } catch (AndroidLocationException e) {
             log.error(e, null);
         } catch (IOException e) {
             log.error(e, null);
         }
 
         // nothing to do or succeeded
         return true;
     }
 
     /**
      * Helper method to recursively delete a folder's content (but not the folder itself).
      * 
      * @throws SecurityException like {@link File#delete()} does if file/folder is not writable.
      */
     public void recursiveDelete(File folder) {
         for (File f : folder.listFiles()) {
             if (f.isDirectory()) {
                 recursiveDelete(folder);
             }
             f.delete();
         }
     }
 
     /**
      * Returns a list of files that are potential AVD ini files.
      * <p/>
      * This lists the $HOME/.android/avd/<name>.ini files.
      * Such files are properties file than then indicate where the AVD folder is located.
      * 
      * @return A new {@link File} array or null. The array might be empty.
      * @throws AndroidLocationException if there's a problem getting android root directory.
      */
     private File[] buildAvdFilesList() throws AndroidLocationException {
         // get the Android prefs location.
         String avdRoot = AndroidLocation.getFolder() + AndroidLocation.FOLDER_AVD;
 
         // ensure folder validity.
         File folder = new File(avdRoot);
         if (folder.isFile()) {
             throw new AndroidLocationException(
                     String.format("%1$s is not a valid folder.", avdRoot));
         } else if (folder.exists() == false) {
             // folder is not there, we create it and return
             folder.mkdirs();
             return null;
         }
         
         File[] avds = folder.listFiles(new FilenameFilter() {
             public boolean accept(File parent, String name) {
                 if (INI_NAME_PATTERN.matcher(name).matches()) {
                     // check it's a file and not a folder
                     boolean isFile = new File(parent, name).isFile();
                     return isFile;
                 }
 
                 return false;
             }
         });
         
         return avds;
     }
 
     /**
      * Computes the internal list of available AVDs.
      * This only contains AVDs that reference the target currently available.
      * 
      * @param list An array list that will contain the list of AVDs.
      * @throws AndroidLocationException if there's a problem getting android root directory.
      */
     private void buildAvdList(ArrayList<AvdInfo> list) throws AndroidLocationException {
         
         File[] avds = buildAvdFilesList();
        if (avds != null) {
            for (File avd : avds) {
                AvdInfo info = parseAvdInfo(avd, false /*acceptError*/);
                if (info != null) {
                    list.add(info);
                }
             }
         }
     }
 
     /**
      * Computes the internal list of <em>not</em> available AVDs.
      * <p/>
      * These are the AVDs that failed to load for some reason or another.
      * You can retrieve the load error using {@link AvdInfo#getError()}.
      * <p/>
      * These {@link AvdInfo} must not be used for usual operations (e.g. instanciating
      * an emulator) or trying to use them for anything else but {@link #deleteAvd(AvdInfo, ISdkLog)}
      * will have unpredictable results -- that is most likely the operation will fail. 
      * 
      * @return A list of unavailable AVDs, all with errors. The list can be null or empty if there
      *         are no AVDs to return.
      * @throws AndroidLocationException if there's a problem getting android root directory.
      */
     public List<AvdInfo> getUnavailableAvds() throws AndroidLocationException {
         AvdInfo[] avds = getAvds();
         File[] allAvds = buildAvdFilesList();
         if (allAvds == null || allAvds.length == 0) {
             return null;
         }
 
         TreeSet<File> list = new TreeSet<File>(Arrays.asList(allAvds));
 
         for (AvdInfo info : avds) {
             if (list.remove(info.getIniFile())) {
                 if (list.size() == 0) {
                     return null;
                 }
             }
         }
         
         ArrayList<AvdInfo> errorAvds = new ArrayList<AvdInfo>(list.size());
         for (File file : list) {
             errorAvds.add(parseAvdInfo(file, true /*acceptError*/));
         }
         
         return errorAvds;
     }
 
     /**
      * Parses an AVD config.ini to create an {@link AvdInfo}.
      * 
      * @param path The path to the AVD config.ini
      * @param acceptError When false, an AVD that fails to load will be discarded and null will be
      *        returned. When true, such an AVD will be returned with an error description.
      * @return A new {@link AvdInfo} or null if the file is not valid or null if the AVD is not
      *         valid and acceptError is false.
      */
     private AvdInfo parseAvdInfo(File path, boolean acceptError) {
         String error = null;
         Map<String, String> map = SdkManager.parsePropertyFile(path, mSdkLog);        
 
         String avdPath = map.get(AVD_INFO_PATH);
         String targetHash = map.get(AVD_INFO_TARGET);
 
         IAndroidTarget target = null;
         File configIniFile = null;
         Map<String, String> properties = null;
         
         if (targetHash != null) {
             target = mSdk.getTargetFromHashString(targetHash);
         }
 
         // load the AVD properties.
         if (avdPath != null) {
             configIniFile = new File(avdPath, CONFIG_INI);
         }
         
         if (configIniFile != null) {
             properties = SdkManager.parsePropertyFile(configIniFile, mSdkLog);
         }
 
         // get name
         String name = path.getName();
         Matcher matcher = INI_NAME_PATTERN.matcher(path.getName());
         if (matcher.matches()) {
             name = matcher.group(1);
         }
         
         if (!acceptError) {
             if (avdPath == null ||
                     targetHash == null ||
                     target == null ||
                     configIniFile == null ||
                     properties == null) {
                 return null;
             }
         } else {
             if (avdPath == null || configIniFile == null) {
                 error = String.format("Missing AVD 'path' property in %1$s", name);
             } else if (targetHash == null) {
                 error = String.format("Missing 'target' property in %1$s", name);
             } else if (target == null) {
                 error = String.format("Unknown target '%2$s' in %1$s", name, targetHash);
             } else if (properties == null) {
                 error = String.format("Failed to parse properties from %1$s", avdPath);
             }
         }
         
         AvdInfo info = new AvdInfo(
                 name,
                 avdPath,
                 target,
                 properties,
                 error);
         
         return info;
     }
 
     /**
      * Writes a new AVD config.ini file from a set of properties.
      * 
      * @param iniFile The file to generate.
      * @param values THe properties to place in the ini file.
      * @throws IOException if {@link FileWriter} fails to open, write or close the file.
      */
     private static void createConfigIni(File iniFile, Map<String, String> values)
             throws IOException {
         FileWriter writer = new FileWriter(iniFile);
         
         for (Entry<String, String> entry : values.entrySet()) {
             writer.write(String.format("%1$s=%2$s\n", entry.getKey(), entry.getValue()));
         }
         writer.close();
 
     }
     
     /**
      * Invokes the tool to create a new SD card image file.
      * 
      * @param toolLocation The path to the mksdcard tool.
      * @param size The size of the new SD Card, compatible with {@link #SDCARD_SIZE_PATTERN}.
      * @param location The path of the new sdcard image file to generate.
      * @param log The logger object, to report errors.
      * @return True if the sdcard could be created.
      */
     private boolean createSdCard(String toolLocation, String size, String location, ISdkLog log) {
         try {
             String[] command = new String[3];
             command[0] = toolLocation;
             command[1] = size;
             command[2] = location;
             Process process = Runtime.getRuntime().exec(command);
     
             ArrayList<String> errorOutput = new ArrayList<String>();
             ArrayList<String> stdOutput = new ArrayList<String>();
             int status = grabProcessOutput(process, errorOutput, stdOutput,
                     true /* waitForReaders */);
 
             if (status == 0) {
                 return true;
             } else {
                 for (String error : errorOutput) {
                     log.error(null, error);
                 }
             }
 
         } catch (InterruptedException e) {
             // pass, print error below
         } catch (IOException e) {
             // pass, print error below
         }
         
         log.error(null, "Failed to create the SD card.");
         return false;
     }
 
     /**
      * Gets the stderr/stdout outputs of a process and returns when the process is done.
      * Both <b>must</b> be read or the process will block on windows.
      * @param process The process to get the ouput from
      * @param errorOutput The array to store the stderr output. cannot be null.
      * @param stdOutput The array to store the stdout output. cannot be null.
      * @param waitforReaders if true, this will wait for the reader threads. 
      * @return the process return code.
      * @throws InterruptedException
      */
     private int grabProcessOutput(final Process process, final ArrayList<String> errorOutput,
             final ArrayList<String> stdOutput, boolean waitforReaders)
             throws InterruptedException {
         assert errorOutput != null;
         assert stdOutput != null;
         // read the lines as they come. if null is returned, it's
         // because the process finished
         Thread t1 = new Thread("") { //$NON-NLS-1$
             @Override
             public void run() {
                 // create a buffer to read the stderr output
                 InputStreamReader is = new InputStreamReader(process.getErrorStream());
                 BufferedReader errReader = new BufferedReader(is);
 
                 try {
                     while (true) {
                         String line = errReader.readLine();
                         if (line != null) {
                             errorOutput.add(line);
                         } else {
                             break;
                         }
                     }
                 } catch (IOException e) {
                     // do nothing.
                 }
             }
         };
 
         Thread t2 = new Thread("") { //$NON-NLS-1$
             @Override
             public void run() {
                 InputStreamReader is = new InputStreamReader(process.getInputStream());
                 BufferedReader outReader = new BufferedReader(is);
 
                 try {
                     while (true) {
                         String line = outReader.readLine();
                         if (line != null) {
                             stdOutput.add(line);
                         } else {
                             break;
                         }
                     }
                 } catch (IOException e) {
                     // do nothing.
                 }
             }
         };
 
         t1.start();
         t2.start();
 
         // it looks like on windows process#waitFor() can return
         // before the thread have filled the arrays, so we wait for both threads and the
         // process itself.
         if (waitforReaders) {
             try {
                 t1.join();
             } catch (InterruptedException e) {
             }
             try {
                 t2.join();
             } catch (InterruptedException e) {
             }
         }
 
         // get the return code from the process
         return process.waitFor();
     }
 
     /**
      * Removes an {@link AvdInfo} from the internal list.
      * 
      * @param avdInfo The {@link AvdInfo} to remove.
      * @return true if this {@link AvdInfo} was present and has been removed.
      */
     public boolean removeAvd(AvdInfo avdInfo) {
         return mAvdList.remove(avdInfo);
     }
 
 }
