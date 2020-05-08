 /**
  * This file is part of RibbonServer application (check README).
  * Copyright (C) 2012-2013 Stanislav Nepochatov
  * 
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 **/
 
 package ribbonserver;
 
 import Utils.IOControl;
 
 /**
  * Directories handle class
  * @author Stanislav Nepochatov
  * @since RibbonServer a1
  */
 public final class Directories {
     
     /**
      * ID of this component or object for loging.
      */
     private static String LOG_ID = "НАПРЯМКИ";
     
     /**
      * Root directory.
      * @since RibbonServer a1
      */
     private static DirClasses.DirEntry rootDir;
     
     /**
      * List with pseudo directories.
      * @since RibbonServer a2
      */
     private static java.util.ArrayList<PseudoDirEntry> pseudoDirs;
     
     /**
      * Directories global lock.
      * @since RibbonServer a2
      */
     private static final Object dirLock = new Object();
     
     /**
      * Pseudo directory class.
      * @author Stanislav Nepochatov
      * @since RibbonServer a2
      */
     public static class PseudoDirEntry extends Generic.CsvElder {
 
         /**
          * Name of the pseudo directory.
          */
         public String PSEUDO_DIR_NAME;
         
         /**
          * Commentary for this pseudo directory.
          */
         private String COMM;
         
         /**
          * Array of inernal directories;
          */
        private java.util.ArrayList<DirClasses.DirEntry> INTERNAL_DIRS = new java.util.ArrayList<DirClasses.DirEntry>();
         
         /**
          * Default constructor.
          */
         public PseudoDirEntry() {
             this.baseCount = 2;
             this.groupCount = 1;
             this.currentFormat = Generic.CsvElder.csvFormatType.ComplexCsv;
         }
         
         /**
          * Parametric constructor.
          * @param givenCsv csv representation of pseudo durectory;
          */
         public PseudoDirEntry(String givenCsv) {
             this();
             java.util.ArrayList<String[]> parsed = Generic.CsvFormat.fromCsv(this, givenCsv);
             PSEUDO_DIR_NAME = parsed.get(0)[0];
             COMM = parsed.get(0)[1];
             String[] parsedDirs = parsed.get(1);
             for (String currParsedDir : parsedDirs) {
                 DirClasses.DirEntry addDir = Directories.rootDir.returnEndDir("", currParsedDir);
                 if (addDir != null) {
                     INTERNAL_DIRS.add(addDir);
                 } else {
                     RibbonServer.logAppend(LOG_ID, 1, "помилка у індексі псевдонапрямків (напрямок " + currParsedDir + " не існує)");
                 }
             }
         }
         
         /**
          * Get internal directories as string array;
          * @return 
          */
         public String[] getinternalDirectories() {
             String[] returned = new String[this.INTERNAL_DIRS.size()];
             for (int index = 0; index < returned.length; index++) {
                 returned[index] = this.INTERNAL_DIRS.get(index).FULL_DIR_NAME;
             }
             return returned;
         }
         
         /**
          * Check if user can use this pseudo dir.
          * @param userName name of user to check access;
          * @return true if user can use this pseudo dir;
          */
         public Boolean checkPseudoDir(String userName) {
             if (AccessHandler.checkAccessForAll(userName, this.getinternalDirectories(), 1) == null) {
                 return true;
             } else {
                 return false;
             }
         }
         
         
         @Override
         public String toCsv() {
             String returned = "{" + this.PSEUDO_DIR_NAME + "},{" + this.COMM + "},[";
             for (DirClasses.DirEntry currDir : INTERNAL_DIRS) {
                 returned += currDir.FULL_DIR_NAME;
             }
             return returned + "]";
         }
     }
     
     /**
      * Init directory's component.
      * @since RibbonServer a2
      */
     public static void init() {
         rootDir = new DirClasses.DirEntry();
         java.util.ArrayList<DirClasses.DirSchema> readedDirs = IndexReader.readDirectories();
         java.util.ListIterator<DirClasses.DirSchema> readIter = readedDirs.listIterator();
         while (readIter.hasNext()) {
             DirClasses.DirSchema currDir = readIter.next();
             RibbonServer.logAppend(LOG_ID, 3, "додано напрямок (" + currDir.FULL_DIR_NAME + ": " + currDir.COMM + ")");
             createDirs(currDir);
             if (RibbonServer.IO_ENABLED) {
                 IOControl.dispathcer.subscribeDir(currDir.DIR_EXPORTS, currDir.FULL_DIR_NAME);
             }
         }
         rootDir.deployDir(RibbonServer.BASE_PATH);
         if (RibbonServer.ACCESS_ALLOW_REMOTE) {
             pseudoDirs = IndexReader.readPseudoDirectories();
             for (PseudoDirEntry curr: pseudoDirs) {
                 RibbonServer.logAppend(LOG_ID, 3, "додано псевдонапрямок " + curr.PSEUDO_DIR_NAME + " " + Generic.CsvFormat.renderGroup(curr.getinternalDirectories()));
             }
         }
     }
     
     /**
      * Create full chain of directories with given schema
      * @param givenSchema directory schema
      * @since RibbonServer a1
      */
     public static void createDirs(DirClasses.DirSchema givenSchema) {
         Directories.rootDir.insertDir("", givenSchema.FULL_DIR_NAME, givenSchema);
     }
     
     /**
      * Dump current tree as text report
      * @since RibbonServer a1
      */
     public static void dumpTree() {
         try {
             java.io.FileWriter treeWriter = new java.io.FileWriter(RibbonServer.BASE_PATH + "/tree");
             treeWriter.write(Directories.rootDir.treeReport(0));
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
      * @since RibbonServer a1
      */
     public static void addIndexToDir(String givenDir, String givenIndex) {
         synchronized (dirLock) {
             Directories.rootDir.addIndex("", givenDir, givenIndex);
         }
     }
     
     /**
      * Remove given index from specified directory
      * @param givenDir directory from which index will be removed
      * @param givenIndex index indentifier
      * @since RibbonServer a1
      */
     public static void removeIndexFromDir(String givenDir, String givenIndex) {
         synchronized (dirLock) {
             Directories.rootDir.removeIndex("", givenDir, givenIndex);
         }
     }
     
     /**
      * Return access description array from 
      * @param givenDir dir to search;
      * @return array with permission entries;
      * @since RibbonServer a2
      */
     public static DirClasses.DirPermissionEntry[] getDirAccess(String givenDir) {
         try {
             return Directories.rootDir.getAccess("", givenDir);
         } catch (Exception ex) {
             return null;
         }
     }
     
     /**
      * Return anonymoys mode flag for specifed dir
      * @param givenDir given path to directory
      * @return path to file directory
      * @since RibbonServer a1
      */
     public static String getDirPath(String givenDir) {
         DirClasses.DirEntry returned = rootDir.returnEndDir("", givenDir);
         if (returned == null) {
             return null;
         }
         return returned.DIR_PATH;
     }
     
     /**
      * Return all dirs to protocol commandlet.
      * @return dirs in csv form;
      * @since RibbonServer a1
      */
     public static String PROC_GET_DIRS() {
         String returned = "";
         java.util.ListIterator<DirClasses.DirEntry> rootDirs = Directories.rootDir.FOLDED_DIR.listIterator();
         while (rootDirs.hasNext()) {
             returned += rootDirs.next().PROC_GET_DIR();
         }
         return returned + "END:";
     }
     
     /**
      * Return pseudo directory object.
      * @param pseudoName name of the pseudo dir;
      * @return pseudo dir reference or null;
      * @since RibbonServer a2
      */
     public static PseudoDirEntry getPseudoDir(String pseudoName) {
         for (PseudoDirEntry curr: pseudoDirs) {
             if (curr.PSEUDO_DIR_NAME.equals(pseudoName)) {
                 return curr;
             }
         }
         return null;
     }
     
     /**
      * Return pseudo directories which specified user may use.
      * @param userName name to check;
      * @return formatted csv strings end END: command at the end;
      * @since RibbonServer a2
      */
     public static String PROC_GET_PSEUDO(String userName) {
         StringBuffer buf = new StringBuffer();
         for (PseudoDirEntry curr: pseudoDirs) {
             if (curr.checkPseudoDir(userName)) {
                 buf.append(curr.toCsv());
                 buf.append("\n");
             }
         }
         buf.append("END:");
         return buf.toString();
     }
 }
