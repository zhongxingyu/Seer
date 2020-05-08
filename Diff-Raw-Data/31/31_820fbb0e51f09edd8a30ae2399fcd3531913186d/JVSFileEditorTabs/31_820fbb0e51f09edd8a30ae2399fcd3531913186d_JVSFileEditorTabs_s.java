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
 import org.javascool.JVSFile;
 import org.javascool.JvsMain;
 import org.javascool.editor.JVSEditor;
 import org.javascool.jvs.Jvs2Java;
 import org.javascool.tools.Console;
 
 /** The JVSFileEditorTabs
  * A powerful JVSTabs to manage a multi-file editing. It only support JVSFile.
  * @author Philippe Vienne
  */
 public class JVSFileEditorTabs extends JVSTabs implements FileEditorTabs{
 
     /** Store all JVSEditor in an HashMap by the fileId */
     private static HashMap<String, JVSEditor> editors = new HashMap<String, JVSEditor>();
     /** Store all JVSFile in an HashMap by the fileId */
     private static HashMap<String, JVSFile> files = new HashMap<String, JVSFile>();
     /** Store all fileIds in an HashMap by the tab name */
     private static HashMap<String, String> fileIds = new HashMap<String, String>();
     /** The current compiled file */
     private static String currentCompiledFile = "";
 
     /**
      * @return the currentCompiledFile
      */
     public static String getCurrentCompiledFile() {
         return currentCompiledFile;
     }
 
     /** Create a new JVSFileEditorTabs */
     public JVSFileEditorTabs() {
         super();
     }
 
     /** Open a new empty Java's cool file in tmp
      * @return The file's tempory id in editor tabs
      */
     @Override
     public String openNewFile() {
         return this.openFile(new JVSFile(JVSFile.defaultCode));
     }
 
     /** Open a new empty Java's cool file in tmp
      * @param url The url to the file (used by File())
      * @return The file's tempory id in editor tabs
      */
     @Override
     public String open(String url) {
         return this.openFile(new JVSFile(url, true));
     }
     
     private String convertStreamToString(InputStream is)
             throws IOException {
         /*
          * To convert the InputStream to String we use the
          * Reader.read(char[] buffer) method. We iterate until the
          * Reader return -1 which means there's no more data to
          * read. We use the StringWriter class to produce the string.
          */
         if (is != null) {
             Writer writer = new StringWriter();
  
             char[] buffer = new char[1024];
             try {
                 Reader reader = new BufferedReader(
                         new InputStreamReader(is, "UTF-8"));
                 int n;
                 while ((n = reader.read(buffer)) != -1) {
                     writer.write(buffer, 0, n);
                 }
             } finally {
                 is.close();
             }
             return writer.toString();
         } else {       
             return "";
         }
     }
     
     /** Open a new empty Java's cool file in tmp
      * @param stream The url to the file (used by File())
      * @return The file's tempory id in editor tabs
      */
     @Override
     public String open(InputStream stream) {
         String s;
         try {
             s = this.openFile(new JVSFile(convertStreamToString(stream)));
             return s;
         } catch (IOException ex) {
             Logger.getLogger(JVSFileEditorTabs.class.getName()).log(Level.SEVERE, null, ex);
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
         if (!this.getFileId(file.getName()).equals("")) {
             if (JVSFileEditorTabs.files.get(this.getFileId(file.getName())).getFile().equals(file.getFile())) {
                 this.setSelectedIndex(this.getTabId(this.getFileId(file.getName())));
                 return this.getFileId(file.getName());
             }
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
             public void insertUpdate(DocumentEvent e) {
                 // Not used
             }
 
             @Override
             public void removeUpdate(DocumentEvent e) {
                 // Not used
             }
 
             @Override
             public void changedUpdate(DocumentEvent e) {
                 // Make notifications for update
                 fileUpdateNotification();
             }
         });
         // Store the new editor
         JVSFileEditorTabs.editors.put(fileId, editor);
         // Store the JVSFile
         JVSFileEditorTabs.files.put(fileId, file);
         // Create the tab name
         String tabTitle = file.getName();
         if (this.indexOfTab(file.getName()) != -1) {
             int i = 1;
             while (this.indexOfTab(file.getName() + " " + i) != -1) {
                 i++;
             }
             tabTitle = file.getName() + " " + i;
             file.setName(tabTitle);
         }
         // We add the tab
         add(tabTitle, "", editors.get(fileId));
 
         // Store the new fileId by the tab name
         JVSFileEditorTabs.fileIds.put(tabTitle, fileId);
         // Select the new tab
         this.setSelectedIndex(this.getTabId(fileId));
         this.setTabComponentAt(this.getTabId(fileId),new TabPanel(this,fileId));
         // Return the new file id
         return fileId;
     }
 
     /** Close an opened file
      * @param fileId The file ID
      */
     @Override
     public void closeFile(String fileId) {
         if (this.getTabId(fileId) != -1) { // We check if file is opened
             String tab_title = this.getTitleAt(this.getTabId(fileId)); // Save the tap title (Useful)
             try {
                 this.removeTabAt(this.getTabId(fileId)); // First remove the tab
                 JVSFileEditorTabs.fileIds.remove(tab_title); // Remove id in the index
                 JVSFileEditorTabs.files.remove(fileId); // Remove the file class
                 JVSFileEditorTabs.editors.remove(fileId); // Remove the editor
                 JVSMainPanel.haveNotToSave(fileId);
             } catch (Exception e) {
                 System.err.println(e.getLocalizedMessage()); // Use to debug
             }
         }
     }
 
     /** Get the Current file ID
      * @return An file id
      */
     @Override
     public String getCurrentFileId() {
         String tab_name = this.getTitleAt(this.getSelectedIndex()); // Get the tab name opened to find the id
         String fileId = JVSFileEditorTabs.fileIds.get(tab_name); // We get the id
         return fileId;
     }
 
     /** Save the current file */
     @Override
     public Boolean saveCurrentFile() {
         return this.saveFile(this.getCurrentFileId()); // We just save the file
     }
 
     /** Check if the current file is in tempory memory
      * @return True if is tempory
      */
     @Override
     public Boolean currentFileIsTmp() {
         return JVSFileEditorTabs.files.get(this.getCurrentFileId()).isTmp(); // Check in the JVS File object if is tempory
     }
 
     /** Compile a file
      * @param fileId The id of the file to javaCompile
      * @return True on success, false in case of error. Can return true if file is not openned
      */
     @Override
     public Boolean compileFile(String fileId) {
         Console.stopProgram();
         if (!JVSFileEditorTabs.fileIds.containsValue(fileId)) { // Check if id is opened
             return true; // Return true because file is not opened
         }
         if (Jvs2Java.jvsCompile(JVSFileEditorTabs.editors.get(fileId).getText())) {
             Console.clear();
             Console.setProgram(Jvs2Java.runnable);
            JVSFileEditorTabs.currentCompiledFile=fileId;
             return true;
         } else {
             return false;
         }
     }
 
     /** Save a file
      * @param fileId The id of the file to save
      * @return True on success, false in case of error. Can return true if file is not openned
      */
     @Override
     public Boolean saveFile(String fileId) {
         if (!JVSFileEditorTabs.fileIds.containsValue(fileId)) { // Check if id is opened
             return true; // Return true because file is not opened
         }
         if (JVSFileEditorTabs.files.get(fileId).isTmp()) {
             return this.saveFilePromptWhere(fileId);
         } else {
             JVSFileEditorTabs.files.get(fileId).setCode(JVSFileEditorTabs.editors.get(fileId).getText()); // Set the editor's text into the object
             JVSFileEditorTabs.files.get(fileId).save(); // Write data in the file
             return true;
         }
     }
 
     /** Prompt where we can save
      * after prompt it call JVSFileEditorTabs.saveFile(fileId)
      * @param fileId The id of the file to save
      * @return See saveFile()
      */
     @Override
     public Boolean saveFilePromptWhere(String fileId) {
         JFileChooser fc = new JFileChooser(); // We create a file chooser
         fc.setApproveButtonText("Enregistrer");
         fc.setDialogTitle("Enregistrer");
         int returnVal = fc.showOpenDialog(this.getParent()); // Get the return value of user choice
         if (returnVal == JFileChooser.APPROVE_OPTION) { // Check if user is ok to save the file
             String path = fc.getSelectedFile().getAbsolutePath(); // Get the path which has been choosed by the user
             String name = fc.getSelectedFile().getName();
             if (!path.endsWith(".jvs")) { // Test if user writed the extension
                 path = path + ".jvs"; // If not we just add it
             }
             if (!name.endsWith(".jvs")) { // Test if user writed the extension
                 name = name + ".jvs"; // If not we just add it
             }
             JVSFile file = new JVSFile(path, true);
             if (!this.getFileId(file.getName()).equals("")) {
                 System.out.println("Le fichier n'est pas nouveau ; Test : ");
                 System.out.println(JVSFileEditorTabs.files.get(this.getFileId(file.getName())).getFile().equals(file.getFile()));
                 if (JVSFileEditorTabs.files.get(this.getFileId(file.getName())).getFile().equals(file.getFile())) {
                     JOptionPane.showMessageDialog(JVSFileEditorTabs.getJvsMainPanel(),
                             "Ce fichier est déjà ouvert dans Java's cool, choisisez un nouvelle endroit.",
                             "Erreur d'écriture",
                             JOptionPane.ERROR_MESSAGE);
                     return this.saveFilePromptWhere(fileId);
                 }
             }
             JVSFileEditorTabs.files.get(fileId).setPath(path); // We set the new path
             JVSFileEditorTabs.files.get(fileId).setName(fc.getSelectedFile().getName()); // We set the new Name
             this.editTabName(fileId, name); // Update the TabTitle to the new name
             JVSFileEditorTabs.files.get(fileId).setCode(JVSFileEditorTabs.editors.get(fileId).getText()); // Set the editor's text into the object
 
             if (JVSFileEditorTabs.files.get(fileId).save()) {
                 return true;
             } else {
                 JOptionPane.showMessageDialog(JVSFileEditorTabs.getJvsMainPanel(),
                         "Le fichier ne peut pas être écrit ici, choisisez un nouvelle endroit.",
                         "Erreur d'écriture",
                         JOptionPane.ERROR_MESSAGE);
                 return this.saveFilePromptWhere(fileId);
             }// Write data in the file
         } else {
             return false; // If the user is not ok
         }
     }
 
     /** Get the tabId for an fileId */
     @Override
     public int getTabId(String fileId) {
         if (JVSFileEditorTabs.fileIds.containsValue(fileId)) { // We check if fileId exist
             return this.indexOfComponent(JVSFileEditorTabs.editors.get(fileId)); // Get index from the editor
         }
         return -1; // If file not exist, return -1. 
     }
 
     /** Change the tab name
      * @param fileId The id of the file wich we change the title
      * @param newTitle The new title
      * @return The success
      */
     @Override
     public Boolean editTabName(String fileId, String newTitle) {
         String tabTitle = newTitle; // create the new title
         if (this.indexOfTab(newTitle) != -1) { // Check if tab with its name exist
             int i = 1;
             while (this.indexOfTab(newTitle + " " + i) != -1) { // Generate it
                 i++;
             }
             tabTitle = newTitle + " " + i;
         }
         String oldTabTitle = this.getTitleAt(this.getTabId(fileId)); // Get the old tab title
         this.setTitleAt(this.getTabId(fileId), tabTitle); // Update the title
         JVSFileEditorTabs.fileIds.remove(oldTabTitle); // remove old index
         JVSFileEditorTabs.fileIds.put(tabTitle, fileId); // set the new fil
         return true; // return true all time
     }
 
     /** Get the fileId from a TabName
      * @param tabName The tab Name
      * @return The file Id
      */
     @Override
     public String getFileId(String tabName) {
         if (JVSFileEditorTabs.fileIds.containsKey(tabName)) { // Check if tabName exist
             return JVSFileEditorTabs.fileIds.get(tabName); // Return the id
         } else {
             return ""; // Return empty string if tabName not exist
         }
     }
 
     /** Get a file from its ID */
     @Override
     public JVSFile getFile(String id) {
         if (JVSFileEditorTabs.files.containsKey(id)) { // Check if id exist
             return JVSFileEditorTabs.files.get(id); // Return the JVSFile
         } else {
             return new JVSFile(); // Return new empty JVSFile if id not exists
         }
     }
 
     /** Get the editor for an ID */
     @Override
     public JVSEditor getEditor(String fileId) {
         if (JVSFileEditorTabs.editors.containsKey(fileId)) { // Check if fileId exist
             return JVSFileEditorTabs.editors.get(fileId); // Return the editor
         } else {
             return new JVSEditor(); // Return new empty JVSEditor if fileId not exists
         }
     }
 
     /** File update is call when a file is edited
      * Call JVSMainPanel.mustSave(this.getCurrentFileId()) to check if file has to be save
      */
     protected static void fileUpdateNotification() {
         JVSMainPanel.mustSave(JVSMainPanel.getEditorTabs().getCurrentFileId());
     }
 
     /** Get the Main Panel to have main functions */
     private static JVSMainPanel getJvsMainPanel() {
         return JvsMain.getJvsMainPanel();
     }
 
     @Override
     public int getOppenedFileCount() {
         return this.tabs.entrySet().toArray().length;
     }
 }
