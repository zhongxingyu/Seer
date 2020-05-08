 /*
  * This code is distributed under terms of GNU GPLv2.
  * *See LICENSE file.
  * ©UKRINFORM 2011-2012
  */
 
 package ribbonserver;
 
 /**
  * Directories handle class
  * @author Stanislav Nepochatov
  */
 public class Directories {
     
     /**
      * ID of this component or object for loging
      */
     private String LOG_ID = "Обробник напрямків";
     
     /**
      * Root directory
      */
     private dirEntry rootDir;
     
     /**
      * Default constructor
      */
     Directories() {
         rootDir = new dirEntry();
         java.util.ArrayList<dirSchema> readedDirs = csvHandler.readDirectories();
         java.util.ListIterator<dirSchema> readIter = readedDirs.listIterator();
         while (readIter.hasNext()) {
             dirSchema currDir = readIter.next();
             createDirs(currDir);
         }
         dumpTree();
     }
     
     /**
      * Directory scheme 
      * This class using to deliver schemas from csvHandler;
      */
     public static class dirSchema {
         
         /**
          * Default constructor from csv form
          * @param givenArray array of parsed arguments
          * @deprecated old specifications
          */
         dirSchema(String[] givenArray) {
             SH_DIR_PATH = givenArray[0];
             SH_COMM = givenArray[1];
             if (givenArray[2].equals("1")) {
                 SH_ANON_MODE = true;
             } else {
                 SH_ANON_MODE = false;
             }
         }
         
         /**
          * Default constructor from csv form
          * @param givenStruct given structure after parsing
          * @since RibbonServer a2
          */
         dirSchema(java.util.ArrayList<String[]> givenStruct) {
             SH_DIR_PATH = givenStruct.get(0)[0];
             SH_COMM = givenStruct.get(0)[1];
             SH_LANGS = givenStruct.get(1);
             SH_ACCESS = givenStruct.get(2);
             SH_EXPORTS = givenStruct.get(3);
         }
         
         /**
          * Parametrick costructor
          * @param givenPath full path of directory
          * @param givenComm comment for directory
          * @param givenFlag anonymous flag for this directory
          */
         dirSchema(String givenPath, String givenComm) {
             SH_DIR_PATH = givenPath;
             SH_COMM = givenComm;
             SH_LANGS = new String[] {"ALL"};
             SH_EXPORTS = null;
             SH_ACCESS = new String[] {"ALL:" + RibbonServer.CURR_ALL_MASK};
         }
         
         /** a1 endian **/
         
         /**
          * Full directory path
          */
         public String SH_DIR_PATH;
         
         /**
          * Commentary for directory
          */
         public String SH_COMM;
         
         /**
          * Anonymous mode flag
          * @deprecated a2 has no support of completely anonymous users
          */
         public Boolean SH_ANON_MODE;
         
         /** a2 endian **/
         
         /**
          * Directory's supported languages
          * @since RibbonServer a2
          */
         public String[] SH_LANGS;
         
         /**
          * Access list for directory
          * @since RibbonServer a2
          */
         public String[] SH_ACCESS;
         
         /**
          * Directory's exports list
          * @since RibbonServer a2
          */
         public String[] SH_EXPORTS;
     }
     
     /**
      * Directory entry
      */
     private class dirEntry {
 
         /**
          * Default constructor (empty)
          * Usually this constructor used for creating root dir
          */
         dirEntry() {
             DIR_NAME = "";
             FULL_DIR_NAME = "";
             DIR_ACCESS = null;
             //RibbonServer.logAppend(LOG_ID, 2, "додано кореневий напрямок");
         }
         
         /**
          * Schema-based end constructor
          * @param givenSchema schema to build directory
          */
         dirEntry(Directories.dirSchema givenSchema) {
             this();
             applySchema(givenSchema);
         }
         
         /**
          * Chain constuctor (adapted to a2)
          * @param upperLevel all parent directories
          * @param rest rest of the creation query
          * @param givenComm commentary for directory
          * @param givenPath path for images
          * @param givenAnon anonymous mode flag
          */
         dirEntry(String upperLevel, String rest, Directories.dirSchema givenSchema) {
             this();
             Integer joint;
             if ((joint = rest.indexOf(".")) != -1) {
                 DIR_NAME = rest.substring(0, joint);
                 if (upperLevel.isEmpty()) {
                     FULL_DIR_NAME = DIR_NAME;
                 } else {
                     FULL_DIR_NAME = upperLevel + "." + DIR_NAME;
                 }
                 DIR_PATH = RibbonServer.BASE_PATH + "/" + FULL_DIR_NAME.toLowerCase().replaceAll("\\.", "/") + "/";
                 new java.io.File(DIR_PATH).mkdirs();
                 RibbonServer.logAppend(LOG_ID, 2, "додано порожній напрямок (" + FULL_DIR_NAME + ")");
                 COMM = "Порожній напрямок";
                 FOLDED_DIR.add(new dirEntry(FULL_DIR_NAME, rest.substring(joint + 1), givenSchema));
             } else {
                 applySchema(givenSchema);
                 RibbonServer.logAppend(LOG_ID, 2, "додано напрямок (" + FULL_DIR_NAME + ":" + COMM + ")");
                 new java.io.File(DIR_PATH).mkdirs();
             }
         }
         
         /** a1 endian **/
         
         /**
          * Short directrory name
          */
         public String DIR_NAME;
         
         /**
          * Full directory name (with parents)
          */
         public String FULL_DIR_NAME;
 
         /**
          * Arraylist of inner directiries
          */
         public java.util.ArrayList<dirEntry> FOLDED_DIR = new java.util.ArrayList<>();
         
         /**
          * Arraylist of messages indexes
          */
         public java.util.ArrayList<String> DIR_INDEXCES = new java.util.ArrayList<>();
         
         /**
          * Directory commentary
          */
         public String COMM;
         
         /**
          * Anonymous access to directory
          * @deprecated a1 has no full anonymous mode
          */
         public Boolean ANON_MODE;
         
         /**
          * Path to dir messages
          */
         public String DIR_PATH;
         
         /** a2 endian **/
         
         /**
          * Array of exports shemas names
          * @since RibbonServer a2
          */
         public String[] DIR_EXPORTS;
         
         /**
          * Array of directory's acceptable languages
          * @since RibbonServer a2
          */
         public String[] DIR_LANGS;
         
         /**
          * Access array of this directory
          * @since RibbonServer a2
          */
         public dirEntry.dirPermissionEntry[] DIR_ACCESS;
         
         /**
          * Last searched directory
          * @since RibbonServer a2
          */
         private dirEntry lastEntry;
         
         /**
          * Permission object class
          * @since RibbonServer a2
          */
         public class dirPermissionEntry {
             
             /**
              * Default constructor
              * @param rawDescriptor string descriptor of permission to directory
              */
             dirPermissionEntry(String rawDescriptor) {
                 String[] parsedArr = csvHandler.parseDoubleStruct(rawDescriptor);
                 KEY = parsedArr[0];
                 MAY_READ = parsedArr[1].charAt(0) == '1' ? true : false;
                 MAY_RELEASE = parsedArr[1].charAt(1) == '1' ? true : false;
                 MAY_ADMIN = parsedArr[1].charAt(2) == '1' ? true : false;
             }
             
             /**
              * Access key (user or group)
              */
             public String KEY;
             
             /**
              * Set if owner of key may read this directory
              */
             public Boolean MAY_READ;
             
             /**
              * Set if owner of key may release messages within this directory
              */
             public Boolean MAY_RELEASE;
             
             /**
              * Set if owner of key may administrate this directory
              */
             public Boolean MAY_ADMIN;
             
            /**
             * Return csv representation of permission object
             * @return formated string representation of this permission object
             */
            public String toCsv() {
                String returned = KEY + ":";
                returned += this.MAY_READ == true ? "1" : "0";
                returned += this.MAY_RELEASE == true ? "1" : "0";
                returned += this.MAY_ADMIN == true ? "1" : "0";
                return returned;
            }
         }
         
         /**
          * Insert chain of directories in current directory
          * @param upperLevel all parent directories
          * @param rest rest of the creation query
          * @param givenComm commentary for directory
          * @param givenPath path for images
          * @param givenAnon anonymous mode flag
          */
         public void insertDir(String upperLevel, String rest, Directories.dirSchema givenSchema) {
             Integer joint;
             if ((joint = rest.indexOf(".")) != -1) {
                 String inserted_DIR_NAME = rest.substring(0, joint);
                 String inserted_FULL_DIR_NAME;
                 if (upperLevel.isEmpty()) {
                     inserted_FULL_DIR_NAME = upperLevel + inserted_DIR_NAME;
                 } else {
                     inserted_FULL_DIR_NAME = upperLevel + "." + inserted_DIR_NAME;
                 }
                 if (this.hasFoldDir(inserted_DIR_NAME)) {
                     lastEntry.insertDir(inserted_FULL_DIR_NAME, rest.substring(joint + 1), givenSchema);
                 } else {
                     if (this.DIR_NAME.isEmpty()) {
                         this.FOLDED_DIR.add(new dirEntry("", rest, givenSchema));
                     } else {
                         this.FOLDED_DIR.add(new dirEntry(inserted_FULL_DIR_NAME, rest.substring(joint + 1), givenSchema));
                     }
                 }
             } else {
                 String inserted_DIR_NAME = rest;
                 if (hasFoldDir(inserted_DIR_NAME)) {
                     lastEntry.applySchema(givenSchema);
                 } else {
                     FOLDED_DIR.add(new dirEntry(upperLevel, rest, givenSchema));
                 }
             }
         }
         
         /**
          * Find out if there is a specified dir in FOLDED_DIR
          * @param foldedDirName name of directory
          * @return true if this entry contain such directory in it's children
          */
         private Boolean hasFoldDir(String foldedDirName) {
             java.util.ListIterator<dirEntry> dirIter = FOLDED_DIR.listIterator();
             while (dirIter.hasNext()) {
                 dirEntry foldedDir = dirIter.next();
                 if (foldedDir.DIR_NAME.equals(foldedDirName)) {
                     lastEntry = foldedDir;
                     return true;
                 }
             }
             return false;
         }
         
         /**
          * Update empty directory
          * @param givenComm commentary
          * @param givenAnon anonymous mode flag 
          */
         public void updateDir(String givenComm, Boolean givenAnon) {
             RibbonServer.logAppend(LOG_ID, 2, "оновлення напрямку (" + this.FULL_DIR_NAME + ")");
             this.COMM = givenComm;
             this.ANON_MODE = givenAnon;
         }
         
         /**
          * Apply schema to given directory;
          * @param givenSchema 
          */
         public void applySchema(Directories.dirSchema givenSchema) {
             this.FULL_DIR_NAME = givenSchema.SH_DIR_PATH;
             this.COMM = givenSchema.SH_COMM;
             this.DIR_LANGS = givenSchema.SH_LANGS;
             this.DIR_EXPORTS = givenSchema.SH_EXPORTS;
             if (givenSchema.SH_ACCESS.length == 1 && givenSchema.SH_ACCESS[0].isEmpty()) {
                 this.DIR_ACCESS = null;
             } else {
                 this.DIR_ACCESS = new dirEntry.dirPermissionEntry[givenSchema.SH_ACCESS.length];
                 for (Integer accessIndex = 0; accessIndex < givenSchema.SH_ACCESS.length; accessIndex++) {
                     this.DIR_ACCESS[accessIndex] = new dirEntry.dirPermissionEntry(givenSchema.SH_ACCESS[accessIndex]);
                 }
             }
             String[] chunks = this.FULL_DIR_NAME.split("\\.");
             this.DIR_NAME = chunks[chunks.length - 1];
             this.DIR_PATH = RibbonServer.BASE_PATH + "/" + FULL_DIR_NAME.toLowerCase().replaceAll("\\.", "/") + "/";
         }
         
         /**
          * Build tree from specifed inner level
          * @param inLevel inner folding level
          * @return tree formated string
          */
         public String treeReport(Integer inLevel) {
             String cap = "==================================================================================================================";
             String spaceStr = "";
             for (Integer space = 0; space < inLevel; space++) {
                 spaceStr += "   ";
             }
             String returned = "";
             if (inLevel > 0) {
                 returned = spaceStr + this.DIR_NAME + " " + csvHandler.renderGroup(this.DIR_LANGS) + " : " + this.COMM + "\n";
             } else {
                 returned = cap + "\nНапрямки системи \"Стрічка\"\n\nКореневий напрямок:\n" + cap + "\n";
             }
             if (!this.FOLDED_DIR.isEmpty()) {
                 java.util.ListIterator<dirEntry> foldedIter = this.FOLDED_DIR.listIterator();
                 while (foldedIter.hasNext()) {
                     dirEntry foldDir = foldedIter.next();
                     returned += foldDir.treeReport(inLevel + 1);
                 }
             }
             if (inLevel == 0) {
                 return returned + cap + "\n" + RibbonServer.getCurrentDate() + "\n" + cap;
             } else {
                 return returned;
             }
         }
         
         /**
          * Build full text report
          * @return csv report
         * @deprecated old code
          */
         public String report() {
             return this.FULL_DIR_NAME + "," + this.COMM + this.ANON_MODE.booleanValue() + "\n";
         }
         
         /**
          * Add index to folded directory
          * @param upperLevel upper level
          * @param rest rest of add query
          * @param givenIndex index of message
          */
         public void addIndex(String upperLevel, String rest, String givenIndex) {
             Integer joint;
             if ((joint = rest.indexOf(".")) != -1) {
                 String indxed_DIR_NAME = rest.substring(0, joint);
                 if (this.hasFoldDir(indxed_DIR_NAME) == false) {
                     RibbonServer.logAppend(LOG_ID, 1, "неможливо додати індекс " + givenIndex + " до напрямку " + upperLevel + ">" + rest);
                     return;
                 } else {
                     lastEntry.addIndex(this.FULL_DIR_NAME, rest.substring(joint + 1), givenIndex);
                 }
             } else {
                 String indxed_DIR_NAME = rest;
                 if (this.hasFoldDir(indxed_DIR_NAME) == false) {
                     RibbonServer.logAppend(LOG_ID, 1, "неможливо додати індекс " + givenIndex + " до напрямку " + upperLevel + ">" + rest);
                     return;
                 } else {
                     lastEntry.DIR_INDEXCES.add(givenIndex);
                 }
             }
         }
         
         /**
          * Remove index from folded directory
          * @param upperLevel upper level
          * @param rest rest of remove query
          * @param givenIndex index of message
          */
         public void removeIndex(String upperLevel, String rest, String givenIndex) {
             Integer joint;
             if ((joint = rest.indexOf(".")) != -1) {
                 String indxed_DIR_NAME = rest.substring(0, joint);
                 if (this.hasFoldDir(indxed_DIR_NAME) == false) {
                     RibbonServer.logAppend(LOG_ID, 1, "неможливо вилучити індекс " + givenIndex + " з напрямку " + upperLevel + ">" + rest);
                     return;
                 } else {
                     lastEntry.removeIndex(this.FULL_DIR_NAME, rest.substring(joint + 1), givenIndex);
                 }
             } else {
                 String indxed_DIR_NAME = rest;
                 if (this.hasFoldDir(indxed_DIR_NAME) == false) {
                     RibbonServer.logAppend(LOG_ID, 1, "неможливо вилучити індекс " + givenIndex + " з напрямку " + upperLevel + ">" + rest);
                     return;
                 } else {
                     lastEntry.DIR_INDEXCES.remove(givenIndex);
                 }
             }
         }
         
         /**
          * Get cascade of foded diretories.
          * @return formated string for net protocol
          */
         public String PROC_GET_DIR() {
             String returned = "RIBBON_UCTL_LOAD_DIR:" + this.toCsv() + "\n";
             java.util.ListIterator<dirEntry> foldIter = this.FOLDED_DIR.listIterator();
             while (foldIter.hasNext()) {
                 returned += foldIter.next().PROC_GET_DIR();
             }
             return returned;
         }
         
         /**
          * Translate directory object into csv index line
          * @return csv formated string
          */
         public String toCsv() {
            String[] stringedAccess = new String[this.DIR_ACCESS.length];
            for (Integer strIndex = 0; strIndex < this.DIR_ACCESS.length; strIndex++) {
                stringedAccess[strIndex] = this.DIR_ACCESS[strIndex].toCsv();
             }
            return this.FULL_DIR_NAME + ",{" + this.COMM + "}," + csvHandler.renderGroup(DIR_LANGS) + "," + 
                    csvHandler.renderGroup(stringedAccess) + "," + csvHandler.renderGroup(this.DIR_EXPORTS);
         }
         
         /**
          * Return dir with specified path
          * @param upperLevel upper level of path
          * @param rest rest of path
          * @return folded directory object
          */
         public dirEntry returnEndDir(String upperLevel, String rest) {
             Integer joint;
             if ((joint = rest.indexOf(".")) != -1) {
                 String indxed_DIR_NAME = rest.substring(0, joint);
                 if (this.hasFoldDir(indxed_DIR_NAME) == false) {
                     RibbonServer.logAppend(LOG_ID, 1, "неможливо знайти шлях напрямку " + upperLevel + ">" + rest);
                     return null;
                 } else {
                     return lastEntry.returnEndDir(this.FULL_DIR_NAME, rest.substring(joint + 1));
                 }
             } else {
                 String indxed_DIR_NAME = rest;
                 if (this.hasFoldDir(indxed_DIR_NAME) == false) {
                     RibbonServer.logAppend(LOG_ID, 1, "неможливо знайти шлях напрямку " + upperLevel + ">" + rest);
                     return null;
                 } else {
                     return lastEntry;
                 }
             }
         }
     }
     
     /**
      * Create full chain of directories with given schema
      * @param givenSchema directory schema
      */
     public void createDirs(dirSchema givenSchema) {
         this.rootDir.insertDir("", givenSchema.SH_DIR_PATH, givenSchema);
     }
     
     /**
      * Dump current tree as text report
      */
     private void dumpTree() {
         try {
             java.io.FileWriter treeWriter = new java.io.FileWriter(RibbonServer.BASE_PATH + "/tree");
             treeWriter.write(this.rootDir.treeReport(0));
             treeWriter.close();
         } catch (java.io.IOException ex) {
             RibbonServer.logAppend(LOG_ID, 1, "неможливо створити файл дерева напрямків!");
         } finally {
             RibbonServer.logAppend(LOG_ID, 3, "створено файл дерева напрямків.");
         }
     }
     
     /**
      * Add given index to specified directory
      * @param givenDir directory in which index will be added
      * @param givenIndex index identifier
      */
     public void addIndexToDir(String givenDir, String givenIndex) {
         this.rootDir.addIndex("", givenDir, givenIndex);
     }
     
     /**
      * Remove given index from specified directory
      * @param givenDir directory from which index will be removed
      * @param givenIndex index indentifier
      */
     public void removeIndexFromDir(String givenDir, String givenIndex) {
         this.rootDir.removeIndex("", givenDir, givenIndex);
     }
     
     /**
      * Return anonymoys mode flag for specifed dir
      * @param givenDir given path to directory
      * @return anonymous flag
      */
     public Boolean getAnonMode(String givenDir) {
         Boolean returned = rootDir.returnEndDir("", givenDir).ANON_MODE;
         if (returned == null) {
             RibbonServer.logAppend(LOG_ID, 1, "не удалось получить доступ к данным директории: " + givenDir);
             return false;
         }
         return returned;
     }
     
     /**
      * Return anonymoys mode flag for specifed dir
      * @param givenDir given path to directory
      * @return path to file directory
      */
     public String getDirPath(String givenDir) {
         String returned = rootDir.returnEndDir("", givenDir).DIR_PATH;
         if (returned == null) {
             RibbonServer.logAppend(LOG_ID, 1, "не удалось получить доступ к данным директории: " + givenDir);
             return null;
         }
         return returned;
     }
     
     /**
      * <b>[RIBBON a1]</b><br>
      * Return all dirs to protocol commandlet
      * @return dirs in csv form;
      */
     public String PROC_GET_DIRS() {
         String returned = "";
         java.util.ListIterator<dirEntry> rootDirs = this.rootDir.FOLDED_DIR.listIterator();
         while (rootDirs.hasNext()) {
             returned += rootDirs.next().PROC_GET_DIR();
         }
         return returned + "END:";
     }
 }
