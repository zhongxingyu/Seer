 package com.powersurgepub.psfiles;
 
   import com.powersurgepub.psutils.*;
   import com.powersurgepub.xos2.*;
   import java.io.*;
   import java.util.*;
   import javax.swing.*;
 
 /**
  A list of files recently accessed by an application, accessible
  to the user through the Open Recent sub-menu of the File menu. <p>
 
  The singleton design pattern is not used, since a single application may
  legitimately use different files for different purposes, and so may need
  different instances of RecentFiles for the different types of files. An
  optional file qualifier is provided to support these cases. <p>
 
  An internal list of file specs is maintained, and kept in sync with a JMenu
  list of menu items. Both lists are maintained in chronological sequence,
  with the most recently accessed files at the top. <p>
 
  Usage scenario: <p>
 
  <pre>
  {@code
 
      implements FileSpecOpener
  
      RecentFiles recentFiles;
 
      // Program initialization
      recentFiles = new RecentFiles();
      recentFiles.loadFromPrefs();
      recentFiles.registerMenu(fileOpenRecentMenu, this);
 
    public RecentFiles getRecentFiles () {
      return recentFiles;
    }
 
      // When opening a file
      recentFiles.addRecentFile ("file", file.toString(), "html");
 
 
    public void handleOpenFile (FileSpec fileSpec) {
      // open recent file when selected by user
    }
 
    public void savePrefs() {
      recentFiles.savePrefs();
    }
  </pre>
 
  @author Herb Bowie
  */
 public class RecentFiles {
 
   public static final String          RECENT_FILES_MAX = "recent-files-max";
 
   private         String              prefsQualifier          = "";
   
   private         int                 recentFilesMax          = 5;
 
   private         ArrayList<FileSpec> files                   = new ArrayList();
 
   private         JMenu               recentFilesMenu         = null;
   
   private         FileSpecOpener      fileOpener              = null;
   
   private         FilePrefs           filePrefs               = null;
   
   private         int                 fileSelectionMode       = 
       JFileChooser.DIRECTORIES_ONLY;
   
   private         String              fileContentsName        = "Items";
 
   /**
    Construct a RecentFiles instance without any prefs qualifier.
    */
   public RecentFiles () {
 
   }
 
   /**
    Construct a RecentFiles instance with a prefs qualifier.
 
    @param prefsQualifier If non-blank, will be used to distinguish between
                          different sets of recent files used for different
                          purposes by the same program.
    */
   public RecentFiles (String prefsQualifier) {
     this.prefsQualifier = prefsQualifier;
     if (prefsQualifier.length() > 0
         && prefsQualifier.charAt(prefsQualifier.length() - 1) != '-') {
       this.prefsQualifier = prefsQualifier + "-";
     } else {
       this.prefsQualifier = prefsQualifier;
     }
     fileContentsName = Home.getShared().getProgramName() + " Items";
   }
   
   public String getPrefsQualifier () {
     return prefsQualifier;
   }
   
   public void setFileContentsName (String fileContentsName) {
     this.fileContentsName = fileContentsName;
   }
   
   public void setFilePrefs (FilePrefs filePrefs) {
     this.filePrefs = filePrefs;
   }
 
   /**
    Load the recent files from the user's preferences.
    */
   public void loadFromPrefs () {
     
     loadRecentFilesMax();
     for (int i = 0; i < recentFilesMax; i++) {
       FileSpec recentFile = new FileSpec();
       recentFile.loadFromRecentPrefs(prefsQualifier, i);
       if (recentFile.hasPath()) {
         files.add(recentFile);
 
         if (recentFilesMenu != null) {
           recentFilesMenu.insert (createMenuItem(recentFile), i);
         }
         
         if (filePrefs != null) {
           filePrefs.addRecentFileAtEnd(recentFile);
         }
       }
     }
   }
   
   public void displayRecentFiles() {
     System.out.println ("RecentFiles.displayRecentFiles");
     System.out.println("  files");
     for (int i = 0; i < files.size(); i++) {
       System.out.println("    " + String.valueOf(i) + ": " + get(i).getPath());
     }
     
     if (recentFilesMenu != null) {
       System.out.println ("  Recent Files menu");
       for (int i = 0; i < recentFilesMenu.getMenuComponentCount(); i++) {
         System.out.println("  " + String.valueOf(i) + ": " + 
             recentFilesMenu.getMenuComponent(i).toString());
       }
     }
   }
   
   public void purgeInaccessibleFiles () {
     
     int i = 0;
     while (i < files.size()) {
       FileSpec recentFile = files.get(i);
       if (! recentFile.exists()) {
         removeFile(i);
       } else {
         i++;
       }
     }
   }
 
   /**
    Save the recent files to the user's preferences. 
    */
   public void savePrefs () {
     
     saveRecentFilesMax();
     
     int count = 0;
     for (int i = 0; i < files.size(); i++) {
       FileSpec recentFile = get(i);
       if (recentFile.hasPath()) {
         recentFile.saveToRecentPrefs(prefsQualifier, count);
         count++;
       }
     }
   }
   
   public void setFileSelectionMode (int fileSelectionMode) {
     this.fileSelectionMode = fileSelectionMode;
   }
   
   /**
    Prompt the user to choose a file to be opened. 
   
    @return The file specification for the file to be opened, or null
            if the user did not choose a file or folder. 
   */
   public void chooseFileToOpen (JFrame frame) {
     XFileChooser chooser = new XFileChooser ();
     chooser.setFileSelectionMode(fileSelectionMode);
     chooser.setDialogTitle ("Open " + fileContentsName);
     File result = chooser.showOpenDialog (frame);
     if (result != null
         && result.exists()
         && result.canRead()
         && ((fileSelectionMode == JFileChooser.FILES_AND_DIRECTORIES)
           || (fileSelectionMode == JFileChooser.FILES_ONLY
             && result.isFile())
           || (fileSelectionMode == JFileChooser.DIRECTORIES_ONLY
             && result.isDirectory()))) {
       FileSpec fileSpec = addRecentFile (result);
       fileOpener.handleOpenFile(fileSpec);
     } else {
       Trouble.getShared().report ("Trouble opening file " + result.toString(),
           "File Open Error");
     }
   }
 
   public FileSpec addRecentFile (File file) {
     return addRecentFile ("file", file.getAbsolutePath(), "");
   }
 
   /**
    Add a file that's been recently used.
 
    @param type A string indicating the type of file or data store.
    @param path The path of the file or data store.
    @param format The format of the data within the data store. 
    */
   public FileSpec addRecentFile (String type, String path, String format) {
     
     FileSpec recentFile = new FileSpec();
     recentFile.setType(type);
     recentFile.setPath(path);
     recentFile.setFormat(format);
     return addRecentFile (recentFile);
 
   } // end method addRecentFile
   
   /**
    Add a file that's been recently used.
 
   @param type A string indicating the type of file or data store.
   @param path The path of the file or data store.
    @param format The format of the data within the data store. 
    */
   public FileSpec addRecentFile (FileSpec recentFile) {
 
     files.add (0, recentFile);
     if (recentFilesMenu != null) {
       recentFilesMenu.insert (createMenuItem(recentFile), 0);
     }
     if (filePrefs != null) {
       filePrefs.addRecentFileAtTop(recentFile);
     }
     for (int i = 1; i < files.size(); i++) {
      if (get(i).getPath().equals(recentFile.getPath())
          || i >= recentFilesMax) {
         removeFile (i);
         i--;
       } // end if we found a file to remove
     } // end for each file in list
     savePrefs();
     return get(0);
   } // end method addRecentFile
   
   private void removeFile (int i) {
    
     files.remove(i);
     if (filePrefs != null) {
       filePrefs.removeRecentFile (i);
     }
     if (recentFilesMenu != null && i < recentFilesMenu.getItemCount()) {
       recentFilesMenu.remove(i);
     }
   }
 
   /**
    Get the user's latest preference for the maximum number of recent files
    to retain.
    */
   private void loadRecentFilesMax () {
     setRecentFilesMax (UserPrefs.getShared().getPrefAsInt (RECENT_FILES_MAX, 5));
   }
   
   private void saveRecentFilesMax () {
     UserPrefs.getShared().setPref(RECENT_FILES_MAX, getRecentFilesMax());
   }
 
   /**
    Set the maximum number of recent files to be retained.
 
    @param recentFilesMax The number of recent files to be retained.
    */
   public void setRecentFilesMax (int recentFilesMax) {
     this.recentFilesMax = recentFilesMax;
     while (files.size() > recentFilesMax) {
       removeFile (files.size() - 1);
     }
   }
   
   /**
    Get the maximum number of recent files to be retained. 
    
    @return The number of recent files to be retained.
   */
   public int getRecentFilesMax () {
     return recentFilesMax;
   }
 
   /**
    Register the menu to contain the recent file menu items.
 
    @param recentFilesMenu The menu to contain the recent files.
    @param fileOpener The object to be used to open a recent file when
                      it is selected. 
    */
   public void registerMenu (JMenu recentFilesMenu, FileSpecOpener fileOpener) {
     this.recentFilesMenu = recentFilesMenu;
     this.fileOpener = fileOpener;
     buildMenu();
   }
 
   /**
    Build the menu based on the entries in the files list.
    */
   private void buildMenu () {
     for (int i = 0; i < files.size(); i++) {
       recentFilesMenu.add(createMenuItem(get(i)));
     }
     JSeparator sep = new JSeparator();
     recentFilesMenu.add(sep);
 
     JMenuItem clear = new JMenuItem("Clear History");
     clear.addActionListener(new java.awt.event.ActionListener() {
       public void actionPerformed(java.awt.event.ActionEvent evt) {
         clearHistory(evt);
       }
     });
     recentFilesMenu.add(clear);
   }
 
   /**
    Clear the recent files history, when requested by the user.
    
    @param evt
    */
   private void clearHistory (java.awt.event.ActionEvent evt) {
     while (files.size() > 1) {
       removeFile(files.size() - 1);
     }
     FileSpec nullFileSpec = new FileSpec();
     for (int i = 1; i < recentFilesMax; i++) {
       nullFileSpec.saveToRecentPrefs("", i);
     }
     if (filePrefs != null) {
       filePrefs.clearHistory();
     }
   }
 
   /**
    Create one menu item for the recent files menu.
 
    @param fileSpec The FileSpec identifying the file or url.
    @return The new menu item created. 
    */
   private JMenuItem createMenuItem (FileSpec fileSpec) {
 
     // Create the new menu item
     JMenuItem menuItem = new JMenuItem(fileSpec.getBriefDisplayName());
     menuItem.setActionCommand (fileSpec.getPath());
     menuItem.setToolTipText (fileSpec.getDisplayName());
     menuItem.addActionListener(new java.awt.event.ActionListener() {
       public void actionPerformed(java.awt.event.ActionEvent evt) {
         fileRecentMenuItemActionPerformed(evt);
       }
     });
     return menuItem;
   } // end method
 
   /**
     Action listener for recent file menu items.
 
     @param evt = Action event.
    */
   private void fileRecentMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
     String name = evt.getActionCommand();
     int i = 0;
     boolean found = false;
     FileSpec fileSpec = null;
     while (i < files.size() && (! found)) {
       fileSpec = get(i);
       if (name.equals(fileSpec.getPath())) {
         found = true;
       } else {
         i++;
       }
     }
     if (found) {
       File file = new File(fileSpec.getPath());
       if (file.exists()) {
         fileOpener.handleOpenFile(fileSpec);
       }
     }
   } // end method
 
   /**
    Get a particular FileSpec entry, given its position in the list.
 
    @param i The index position of the desired entry in the list.
 
    @return The specified FileSpec entry, if one exists at the index given,
            otherwise null.
    */
   public FileSpec get (int i) {
     if (i < 0 || i >= files.size()) {
       return null;
     } else {
       return files.get(i);
     }
   }
   
   /**
    Return the number of recent files in the list. 
   
    @return The number of recent files in the list.  
   */
   public int size() {
     return files.size();
   }
 
 }
