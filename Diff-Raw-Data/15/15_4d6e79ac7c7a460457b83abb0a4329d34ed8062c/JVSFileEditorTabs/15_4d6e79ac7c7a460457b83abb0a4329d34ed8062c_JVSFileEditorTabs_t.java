 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.javascool.gui;
 
 import java.util.HashMap;
 import java.util.UUID;
 import javax.swing.JFileChooser;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 import org.javascool.JVSFile;
 import org.javascool.editor.JVSEditor;
 
 /**
  *
  * @author philien
  */
 public class JVSFileEditorTabs extends JVSTabs {
 
     /** Store all JVSEditor in an HashMap by the fileId */
     private HashMap<String, JVSEditor> editors = new HashMap<String, JVSEditor>();
     /** Store all JVSFile in an HashMap by the fileId */
     private HashMap<String, JVSFile> files = new HashMap<String, JVSFile>();
     /** Store all fileIds in an HashMap by the tab name */
     private HashMap<String, String> fileIds = new HashMap<String, String>();
 
     /** Create a new JVSFileEditorTabs */
     public JVSFileEditorTabs() {
         super();
     }
 
     /** Open a new empty Java's cool file in tmp
      * @return The file's tempory id in editor tabs
      */
     public String openNewFile() {
         return this.openFile(new JVSFile("class Toto{\n"
                 + "\tvoid main(){\n"
                 + "\n"
                 + "\t}\n"
                 + "}"));
     }
 
     /** Open a new empty Java's cool file in tmp
      * @param url The url to the file (used by File())
      * @return The file's tempory id in editor tabs
      */
     public String open(String url) {
         return this.openFile(new JVSFile(url, true));
     }
 
     /** Open a file
      * Open a file from the instance and open its tab
      * @param file The opened file
      * @return The file's tempory id in editor tabs
      */
     private String openFile(JVSFile file) {
         // Check if file is not already opened
         if (!this.getFileId(file.getName()).equals("")) {
             if (this.files.get(this.getFileId(file.getName())).getPath().equals(file.getPath())) {
                 this.setSelectedIndex(this.getTabId(this.getFileId(file.getName())));
                 return this.getFileId(file.getName());
             }
         }
         // Create the fileId wich is unique
         String fileId = UUID.randomUUID().toString();
         // Create the JVSEditor for the file
         JVSEditor editor = new JVSEditor();
         // Set text in the editor
         editor.setText(file.getText());
         // Add listener for edit
         editor.getRTextArea().getDocument().addDocumentListener(new DocumentListener(){
 
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
         this.editors.put(fileId, editor);
         // Store the JVSFile
         this.files.put(fileId, file);
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
         this.add(tabTitle, "", editors.get(fileId));
         // Store the new fileId by the tab name
         this.fileIds.put(tabTitle, fileId);
         // Select the new tab
         this.setSelectedIndex(this.getTabId(fileId));
         // Return the new file id
         return fileId;
     }
 
     /** Close an opened file
      * @param fileId The file ID
      */
     public void closeFile(String fileId) {
         if (this.getTabId(fileId) != -1) { // We check if file is opened
             String tab_title = this.getTitleAt(this.getTabId(fileId)); // Save the tap title (Useful)
             try {
                 this.removeTabAt(this.getTabId(fileId)); // First remove the tab
                 this.fileIds.remove(tab_title); // Remove id in the index
                 this.files.remove(fileId); // Remove the file class
                 this.editors.remove(fileId); // Remove the editor
             } catch (Exception e) {
                 System.err.println(e.getLocalizedMessage()); // Use to debug
             }
         }
     }
 
     /** Get the Current file ID
      * @return An file id
      */
     public String getCurrentFileId() {
         String tab_name = this.getTitleAt(this.getSelectedIndex()); // Get the tab name opened to find the id
         String fileId = this.fileIds.get(tab_name); // We get the id
         return fileId;
     }
 
     /** Save the current file */
     public void saveCurrentFile() {
         if (this.currentFileIsTmp()) { // Check if current file isin tempory memory
             this.saveFilePromptWhere(this.getCurrentFileId()); // If yes prompt were we can save
         } else { // Else
             this.saveFile(this.getCurrentFileId()); // We just save the file
         }
     }
 
     /** Check if the current file is in tempory memory
      * @return True if is tempory
      */
     public Boolean currentFileIsTmp() {
         return this.files.get(this.getCurrentFileId()).isTmp(); // Check in the JVS File object if is tempory
     }
 
     /** Save a file
      * @param fileId The id of the file to save
      * @return True on success, false in case of error. Can return true if file is not openned
      */
     public Boolean saveFile(String fileId) {
         if (!this.fileIds.containsValue(fileId)) { // Check if id is opened
             return true; // Return true because file is not opened
         }
         if (this.files.get(fileId).isTmp()) {
             return this.saveFilePromptWhere(fileId);
         } else {
             this.files.get(fileId).setText(this.editors.get(fileId).getText()); // Set the editor's text into the object
             return this.files.get(fileId).save(); // Write data in the file
         }
     }
 
     /** Prompt where we can save
      * after prompt it call JVSFileEditorTabs.saveFile(fileId)
      * @param fileId The id of the file to save
      * @return See saveFile()
      */
     public Boolean saveFilePromptWhere(String fileId) {
         JFileChooser fc = new JFileChooser(); // We create a file chooser
         int returnVal = fc.showOpenDialog(this.getParent()); // Get the return value of user choice
         if (returnVal == JFileChooser.APPROVE_OPTION) { // Check if user is ok to save the file
             String path = fc.getSelectedFile().getAbsolutePath(); // Get the path which has been choosed by the user
             if (!path.endsWith(".jvs")) { // Test if user writed the extension
                 path = path + ".jvs"; // If not we just add it
             }
             this.files.get(fileId).setPath(path); // We set the new path
             this.files.get(fileId).setName(fc.getSelectedFile().getName()); // We set the new Name
             this.editTabName(fileId, fc.getSelectedFile().getName()); // Update the TabTitle to the new name
            this.files.get(fileId).setText(this.editors.get(fileId).getText()); // Set the editor's text into the object
            return this.files.get(fileId).save(); // Write data in the file
         } else {
             return false; // If the user is not ok
         }
     }
 
     /** Get the tabId for an fileId */
     public int getTabId(String fileId) {
         if (this.fileIds.containsValue(fileId)) { // We check if fileId exist
             return this.indexOfComponent(this.editors.get(fileId)); // Get index from the editor
         }
         return -1; // If file not exist, return -1. 
     }
 
     /** Change the tab name
      * @param fileId The id of the file wich we change the title
      * @param newTitle The new title
      * @return The success
      */
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
         this.fileIds.remove(oldTabTitle); // remove old index
         this.fileIds.put(tabTitle, fileId); // set the new fil
         return true; // return true all time
     }
 
     /** Get the fileId from a TabName
      * @param tabName The tab Name
      * @return The file Id
      */
     public String getFileId(String tabName) {
         if (this.fileIds.containsKey(tabName)) { // Check if tabName exist
             return this.fileIds.get(tabName); // Return the id
         } else {
             return ""; // Return empty string if tabName not exist
         }
     }
 
     /** Get a file from its ID */
     public JVSFile getFile(String id) {
         if (this.files.containsKey(id)) { // Check if id exist
             return this.files.get(id); // Return the JVSFile
         } else {
             return new JVSFile(); // Return new empty JVSFile if id not exists
         }
     }
 
     /** Get the editor for an ID */
     public JVSEditor getEditor(String fileId) {
         if (this.editors.containsKey(fileId)) { // Check if fileId exist
             return this.editors.get(fileId); // Return the editor
         } else {
             return new JVSEditor(); // Return new empty JVSEditor if fileId not exists
         }
     }
     
     protected void fileUpdateNotification(){
         ((JVSMainPanel)this.getParent()).mustSave(this.getCurrentFileId());
     }
 }
