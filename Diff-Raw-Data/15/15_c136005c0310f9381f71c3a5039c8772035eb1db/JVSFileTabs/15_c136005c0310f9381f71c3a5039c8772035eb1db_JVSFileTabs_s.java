 package org.javascool.gui;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.io.StringWriter;
 import java.io.Writer;
 import java.util.HashMap;
 import java.util.UUID;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.JFileChooser;
 import javax.swing.JOptionPane;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 import org.javascool.widgets.Console;
 import org.javascool.core.ProgletEngine;
 
 /** The JVSFileTabs
  * A powerful JVSTabs to manage a multi-file editing. It only support JVSFile.
  */
 class JVSFileTabs extends JVSTabs {
   private static final long serialVersionUID = 1L;
   /** Store all JVSEditor in an HashMap by the fileId */
   private static HashMap<String, JVSEditor> editors = new HashMap<String, JVSEditor>();
   /** Store all JVSFile in an HashMap by the fileId */
   private static HashMap<String, JVSFile> files = new HashMap<String, JVSFile>();
   /** Store all fileIds in an HashMap by the tab name */
   private static HashMap<String, String> fileIds = new HashMap<String, String>();
   /** The current compiled file */
   private static String currentCompiledFile = "";
   private static JVSFileTabs desktop;
 
   /** Access to the unique instance of the JVSPanel object. */
   public static JVSFileTabs getInstance() {
     if(desktop == null)
       desktop = new JVSFileTabs();
     return desktop;
   }
   /**
    * @return the currentCompiledFile
    */
   public static String getCurrentCompiledFile() {
     return currentCompiledFile;
   }
   /** Create a new JVSFileTabs */
   private JVSFileTabs() {
     super();
   }
   /** Open a new empty Java's cool file in tmp
    * @return The file's tempory id in editor tabs
    */
   public String openNewFile() {
     return this.openFile(new JVSFile(JVSFile.defaultCode));
   }
   /** Open a new empty Java's cool file in tmp
    * @param url The url to the file (used by File())
    * @return The file's tempory id in editor tabs
    */
   public String open(String url) {
     return this.openFile(new JVSFile(url, true));
   }
   // @todo Ou mettre cette fonction
   private String convertStreamToString(InputStream is)
   throws IOException {
     if(is != null) {
       Writer writer = new StringWriter();
 
       char[] buffer = new char[1024];
       try {
         Reader reader = new BufferedReader(
           new InputStreamReader(is, "UTF-8"));
         int n;
         while((n = reader.read(buffer)) != -1)
           writer.write(buffer, 0, n);
       }
       finally {
         is.close();
       }
       return writer.toString();
     } else
       return "";
   }
   /** Open a new empty Java's cool file in tmp
    * @param stream The url to the file (used by File())
    * @return The file's tempory id in editor tabs
    */
   public String open(InputStream stream) {
     String s;
     try {
       s = this.openFile(new JVSFile(convertStreamToString(stream)));
       return s;
     } catch(IOException ex) {
       Logger.getLogger(JVSFileTabs.class.getName()).log(Level.SEVERE, null, ex);
     }
     return null;
   }
   /** Open a file
    * Open a file from the instance and open its tab
    * @param file The opened file
    * @return The file's tempory id in editor tabs
    */
   private String openFile(JVSFile file) {
     // Check if file is not already opened
     if(!this.getFileId(file.getName()).equals(""))
       if(JVSFileTabs.files.get(this.getFileId(file.getName())).getFile().equals(file.getFile())) {
         this.setSelectedIndex(this.getTabId(this.getFileId(file.getName())));
         return this.getFileId(file.getName());
       }
     // Create the fileId wich is unique
     String fileId = UUID.randomUUID().toString();
     // Create the JVSEditor for the file
     JVSEditor editor = new JVSEditor();
     // Set text in the editor
     editor.setText(file.getCode());
 
     // Add listener for edit
     editor.getRTextArea().getDocument().addDocumentListener(new DocumentListener() {
                                                               @Override
                                                               public void insertUpdate(DocumentEvent e) {}
 
                                                               @Override
                                                               public void removeUpdate(DocumentEvent e) {}
 
                                                               @Override
                                                               public void changedUpdate(DocumentEvent e) {
                                                                 fileUpdateNotification();
                                                               }
                                                             }
                                                             );
     // Store the new editor
     JVSFileTabs.editors.put(fileId, editor);
     // Store the JVSFile
     JVSFileTabs.files.put(fileId, file);
     // Create the tab name
     String tabTitle = file.getName();
     if(this.indexOfTab(file.getName()) != -1) {
       int i = 1;
       while(this.indexOfTab(file.getName() + " " + i) != -1)
         i++;
       tabTitle = file.getName() + " " + i;
       file.setName(tabTitle);
     }
     // We add the tab
     add(tabTitle, "", editors.get(fileId));
 
     // Store the new fileId by the tab name
     JVSFileTabs.fileIds.put(tabTitle, fileId);
     // Select the new tab
     this.setSelectedIndex(this.getTabId(fileId));
     this.setTabComponentAt(this.getTabId(fileId), new TabPanel(this, fileId));
     // Return the new file id
     return fileId;
   }
   /** Close an opened file
    * @param fileId The file ID
    */
   public void closeFile(String fileId) {
     if(this.getTabId(fileId) != -1) {                // We check if file is opened
       String tab_title = this.getTitleAt(this.getTabId(fileId));                   // Save the tap title (Useful)
       try {
         this.removeTabAt(this.getTabId(fileId));                         // First remove the tab
         JVSFileTabs.fileIds.remove(tab_title);                         // Remove id in the index
         JVSFileTabs.files.remove(fileId);                         // Remove the file class
         JVSFileTabs.editors.remove(fileId);                         // Remove the editor
         JVSPanel.getInstance().haveNotToSave(fileId);
       } catch(Exception e) { throw new IllegalStateException(e); // Use to debug
       }
     }
   }
   /** Get the Current file ID
    * @return An file id
    */
   public String getCurrentFileId() {
     String tab_name = this.getTitleAt(this.getSelectedIndex());             // Get the tab name opened to find the id
     String fileId = JVSFileTabs.fileIds.get(tab_name);             // We get the id
     return fileId;
   }
   /** Save the current file */
   public Boolean saveCurrentFile() {
     return this.saveFile(this.getCurrentFileId());             // We just save the file
   }
   /** Check if the current file is in tempory memory
    * @return True if is tempory
    */
   public Boolean currentFileIsTmp() {
     return JVSFileTabs.files.get(this.getCurrentFileId()).isTmp();             // Check in the JVS File object if is tempory
   }
   /** Compile a file
    * @param fileId The id of the file to javaCompile
    * @return True on success, false in case of error. Can return true if file is not openned
    */
   public Boolean compileFile(String fileId) {
     if(!JVSFileTabs.fileIds.containsValue(fileId) || JVSFileTabs.files.get(fileId).isTmp())
       return false;
     JVSFileTabs.currentCompiledFile = fileId;
     if(ProgletEngine.getInstance().doCompile(JVSFileTabs.editors.get(fileId).getText())) {
       Console.getInstance().clear();
       System.out.println("Compilation réussie !");
       return true;
     } else
       return false;
   }
   /** Save a file
    * @param fileId The id of the file to save
    * @return True on success, false in case of error. Can return true if file is not openned
    */
   public Boolean saveFile(String fileId) {
     if(!JVSFileTabs.fileIds.containsValue(fileId))        // Check if id is opened
       return true;                   // Return true because file is not opened
     if(JVSFileTabs.files.get(fileId).isTmp())
       return this.saveFilePromptWhere(fileId);
     else {
       JVSFileTabs.files.get(fileId).setCode(JVSFileTabs.editors.get(fileId).getText());                   // Set the editor's text into the object
       JVSFileTabs.files.get(fileId).save();                   // Write data in the file
       return true;
     }
   }
   /** Prompt where we can save
    * after prompt it call JVSFileTabs.saveFile(fileId)
    * @param fileId The id of the file to save
    * @return See saveFile()
    */
   public Boolean saveFilePromptWhere(String fileId) {
     JFileChooser fc = new JFileChooser();             // We create a file chooser
     fc.setApproveButtonText("Enregistrer");
     fc.setDialogTitle("Enregistrer");
     int returnVal = fc.showOpenDialog(this.getParent());             // Get the return value of user choice
     if(returnVal == JFileChooser.APPROVE_OPTION) {                // Check if user is ok to save the file
       String path = fc.getSelectedFile().getAbsolutePath();                   // Get the path which has been choosed by the user
       String name = fc.getSelectedFile().getName();
       if(!path.endsWith(".jvs"))        // Test if user writed the extension
 
         path = path + ".jvs";                         // If not we just add it
       if(!name.endsWith(".jvs"))        // Test if user writed the extension
 
         name = name + ".jvs";                         // If not we just add it
      JVSFile file = new JVSFile(path, true);
      if(!this.getFileId(file.getName()).equals("")) {
         System.out.println("Le fichier n'est pas nouveau ; Test : ");
        System.out.println(JVSFileTabs.files.get(this.getFileId(file.getName())).getFile().equals(file.getFile()));
        if(JVSFileTabs.files.get(this.getFileId(file.getName())).getFile().equals(file.getFile())) {
           JOptionPane.showMessageDialog(Desktop.getInstance().getFrame(),
                                         "Ce fichier est déjà ouvert dans Java's cool, choisisez un nouvelle endroit.",
                                         "Erreur d'écriture",
                                         JOptionPane.ERROR_MESSAGE);
           return this.saveFilePromptWhere(fileId);
         }
       }
       JVSFileTabs.files.get(fileId).setPath(path);                   // We set the new path
       JVSFileTabs.files.get(fileId).setName(fc.getSelectedFile().getName());                   // We set the new Name
       this.editTabName(fileId, name);                   // Update the TabTitle to the new name
       JVSFileTabs.files.get(fileId).setCode(JVSFileTabs.editors.get(fileId).getText());                   // Set the editor's text into the object
       if(JVSFileTabs.files.get(fileId).save())
         return true;
       else {
         JOptionPane.showMessageDialog(Desktop.getInstance().getFrame(),
                                       "Le fichier ne peut pas être écrit ici, choisisez un nouvelle endroit.",
                                       "Erreur d'écriture",
                                       JOptionPane.ERROR_MESSAGE);
         return this.saveFilePromptWhere(fileId);
       }                  // Write data in the file
     } else
       return false;                   // If the user is not ok
   }
   /** Get the tabId for an fileId */
   public int getTabId(String fileId) {
     if(JVSFileTabs.fileIds.containsValue(fileId))      // We check if fileId exist
 
       return this.indexOfComponent(JVSFileTabs.editors.get(fileId));                   // Get index from the editor
     return -1;             // If file not exist, return -1.
   }
   /** Change the tab name
    * @param fileId The id of the file wich we change the title
    * @param newTitle The new title
    * @return The success
    */
   public Boolean editTabName(String fileId, String newTitle) {
     String tabTitle = newTitle;             // create the new title
     if(this.indexOfTab(newTitle) != -1) {                // Check if tab with its name exist
       int i = 1;
       while(this.indexOfTab(newTitle + " " + i) != -1)        // Generate it
 
         i++;
       tabTitle = newTitle + " " + i;
     }
     String oldTabTitle = this.getTitleAt(this.getTabId(fileId));             // Get the old tab title
     this.setTitleAt(this.getTabId(fileId), tabTitle);             // Update the title
     JVSFileTabs.fileIds.remove(oldTabTitle);             // remove old index
     JVSFileTabs.fileIds.put(tabTitle, fileId);             // set the new fil
     return true;             // return true all time
   }
   /** Get the fileId from a TabName
    * @param tabName The tab Name
    * @return The file Id
    */
   public String getFileId(String tabName) {
     if(JVSFileTabs.fileIds.containsKey(tabName))      // Check if tabName exist
 
       return JVSFileTabs.fileIds.get(tabName);                   // Return the id
     else
       return "";                   // Return empty string if tabName not exist
   }
   /** Get a file from its ID */
   public JVSFile getFile(String id) {
     if(JVSFileTabs.files.containsKey(id))      // Check if id exist
 
       return JVSFileTabs.files.get(id);                   // Return the JVSFile
     else
       return new JVSFile();                   // Return new empty JVSFile if id not exists
   }
   /** Get the editor for an ID */
   public JVSEditor getEditor(String fileId) {
     if(JVSFileTabs.editors.containsKey(fileId))      // Check if fileId exist
 
       return JVSFileTabs.editors.get(fileId);                   // Return the editor
     else
       return new JVSEditor();                   // Return new empty JVSEditor if fileId not exists
   }
   /** File update is call when a file is edited
    * Call JVSPanel.mustSave(this.getCurrentFileId()) to check if file has to be save
    */
   protected static void fileUpdateNotification() {
     JVSPanel.getInstance().mustSave(JVSFileTabs.getInstance().getCurrentFileId());
   }
   public int getOppenedFileCount() {
     return this.tabs.entrySet().toArray().length;
   }
 }
