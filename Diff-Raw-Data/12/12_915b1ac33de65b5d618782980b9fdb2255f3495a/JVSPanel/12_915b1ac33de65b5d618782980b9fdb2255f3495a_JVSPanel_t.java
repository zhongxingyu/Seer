 package org.javascool.gui;
 
 import java.awt.BorderLayout;
 import java.io.Console;
 import java.io.File;
 import java.util.HashMap;
 import javax.swing.JFileChooser;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import org.javascool.core.ProgletEngine;
 import org.javascool.core.ProgletEngine.Proglet;
 import org.javascool.tools.UserConfig;
 
 /** The main panel for Java's cool
  * This class wich is very static contain all that we need to run Java's cool like save and open file command.
  * This class can only be called by JVSPanel on instance otherwise it can throw very big errors
  *
  * @author Philippe Vienne
  */
 class JVSPanel extends JPanel {
 
     private static final long serialVersionUID = 1L;
     /** This HashMap say if a file has to be saved */
     private HashMap<String, Boolean> haveToSave = new HashMap<String, Boolean>();
     private Boolean noFileEdited = true;
 
     /** Access to the unique instance of the JVSPanel object. */
     public static JVSPanel getInstance() {
         if (desktop == null) {
             desktop = new JVSPanel();
         }
         return desktop;
     }
     private static JVSPanel desktop = null;
 
     private JVSPanel() {
         setVisible(true);
         setLayout(new BorderLayout());
         add(JVSStartPanel.getInstance());
         this.revalidate();
     }
 
     public void closeProglet() {
         if (closeAllFiles()) {
             this.removeAll();
             this.setOpaque(true);
             this.repaint();
             this.validate();
             this.repaint();
             add(JVSStartPanel.getInstance());
             this.repaint();
             this.revalidate();
             this.repaint();
         } else {
         }
     }
 
     /** Open a new file in the editor
      * @see JVSFileTabs
      */
     public void newFile() {
         String fileId = JVSFileTabs.getInstance().openNewFile();
         haveToSave.put(fileId, false);
     }
 
     /** Compile file in the editor
      * @see JVSFileTabs
      */
     public void compileFile() {
         JVSFileTabs.getInstance().getEditor(JVSFileTabs.getCurrentCompiledFile()).removeLineSignals();
         if (JVSFileTabs.getInstance().saveCurrentFile()) {
             JVSWidgetPanel.getInstance().focusOnConsolePanel();
             if (JVSFileTabs.getInstance().compileFile(JVSFileTabs.getInstance().getCurrentFileId())) {
                 JVSToolBar.getInstance().enableStartStopButton();
             } else {
                 JVSToolBar.getInstance().disableStartStopButton();
             }
         }
     }
 
     /** Open a file
      * Start a file chooser and open selected file
      * @see JFileChooser
      * @see JVSFileTabs
      */
     public void openFile() {
         final JFileChooser fc = new JFileChooser();
         if (UserConfig.getInstance("javascool").getProperty("dir") != null) {
             fc.setCurrentDirectory(new File(UserConfig.getInstance("javascool").getProperty("dir")));
         } else if (System.getProperty("os.name").toLowerCase().contains("nix") || System.getProperty("os.name").toLowerCase().contains("nux")) {
             fc.setCurrentDirectory(new File(System.getProperty("user.dir")));
         } else if (System.getProperty("home.dir") != null) {
             fc.setCurrentDirectory(new File(System.getProperty("home.dir")));
         }
         int returnVal = fc.showOpenDialog(Desktop.getInstance().getFrame());
         if (returnVal == JFileChooser.APPROVE_OPTION) {
             String path = fc.getSelectedFile().getAbsolutePath();
             if(!fc.getSelectedFile().exists()){
                 Dialog.error("Erreur", "Le fichier indiqué n'existe pas !!!");
                 return;
             }
             UserConfig.getInstance("javascool").setProperty("dir", fc.getSelectedFile().getParentFile().getAbsolutePath());
             if (noFileEdited) {
                 noFileEdited = false;
             }
             String fileId = JVSFileTabs.getInstance().open(path);
             haveToSave.put(fileId, false);
         } else {
         }
     }
 
     /** Save the current file
      * @see JVSFileTabs
      * @see JVSFile
      */
     public boolean saveFile() {
         if (JVSFileTabs.getInstance().saveCurrentFile()) {
             haveToSave.put(JVSFileTabs.getInstance().getCurrentFileId(), false);
             return true;
         }
         return false;
     }
 
     /** Close the current file
      * @see JVSFileTabs
      */
     public void closeFile() {
         if (haveToSave.get(JVSFileTabs.getInstance().getCurrentFileId())) {
             if (saveFileIdBeforeClose(JVSFileTabs.getInstance().getCurrentFileId()) == 1) {
                 JVSFileTabs.getInstance().closeFile(JVSFileTabs.getInstance().getCurrentFileId());
             }
         } else {
             JVSFileTabs.getInstance().closeFile(JVSFileTabs.getInstance().getCurrentFileId());
         }
         if (JVSFileTabs.getInstance().getOppenedFileCount() == 0) {
             newFile();
         }
     }
 
     /** Update haveToSave for a file
      * Set it to true
      * @param fileId The file id
      */
     public void mustSave(String fileId) {
         noFileEdited = false;
         haveToSave.put(fileId, true);
     }
 
     /** Update haveToSave for a file
      * Set it to true
      * @param fileId The file id
      */
     public void haveNotToSave(String fileId) {
         haveToSave.put(fileId, false);
     }
 
     public Boolean getHasToSave(String fileId) {
         return haveToSave.get(fileId);
     }
 
     /** Show a compile error for an human
      * Open a dialog with compile error explains and hightlight the error line
      * @param line The line error
      * @param explication Human explain for that error
      * @see Console
      */
     public void reportCompileError(int line, String explication) {
         org.javascool.widgets.Console.getInstance().clear();
         JVSWidgetPanel.getInstance().focusOnConsolePanel();
         if (JVSFileTabs.getInstance().getEditor(JVSFileTabs.getCurrentCompiledFile()) != null) {
             JVSFileTabs.getInstance().getEditor(JVSFileTabs.getCurrentCompiledFile()).signalLine(line);
         }
     }
 
     /** Handle the close application task
      * Check if all files are saved and if the user want to close the application
      * @return True mean that app can be close and false that app can NOT be closed
      */
     public Boolean close() {
         String id = "";
         Boolean[] can_close = new Boolean[haveToSave.keySet().toArray().length];
         int i = 0;
         int j = 0;
         for (Object fileId : haveToSave.keySet().toArray()) {
             if (haveToSave.get((String) fileId)) {
                 j++;
             }
         }
         // If user no have dialog to stop close, we create one
         if (j == 0) {
             final int n = JOptionPane.showConfirmDialog(
                     Desktop.getInstance().getFrame(),
                     "Voulez vous vraiment quitter Java's cool ?",
                     "Confirmation",
                     JOptionPane.YES_NO_OPTION);
             if (n == JOptionPane.YES_OPTION) {
                 return true;
             } else {
                 return false;
             }
         }
         j = 0;
         // Check save for each file
         for (Object fileId : haveToSave.keySet().toArray()) {
             id = (String) fileId;
             if (haveToSave.get(id)) {
                 // File has to be saved
                 // For number see saveFileIdBeforeClose() documentation about return
                 switch (saveFileIdBeforeClose(id)) {
                     case 1:
                         can_close[i] = true;
                         break;
                     case 0:
                         can_close[i] = false;
                         break;
                     case -1:
                         return false;
                 }
                 j++;
             } else // If file has not to be saved it's good
             {
                 can_close[i] = true;
             }
             if (can_close[i]) // If we can close this file, we close the tab
             {
                 JVSFileTabs.getInstance().closeFile(id);
             }
             i++;
         }
         // Check if a file is not save, if yes we can not close the application
         for (Boolean can_close_r : can_close) {
             if (can_close_r == false) {
                 return false;
             }
         }
         // We return true if all is good
         return true;
     }
 
     /** Handle the close file task
      * Check if all files are saved and if the user want to continue
      * @return True meen that app can be close and false that app can NOT be closed
      */
     public Boolean closeAllFiles() {
         String id = "";
         Boolean[] can_close = new Boolean[haveToSave.keySet().toArray().length];
         int i = 0;
         int j = 0;
         for (Object fileId : haveToSave.keySet().toArray()) {
             if (haveToSave.get((String) fileId)) {
                 j++;
             } else {
                 JVSFileTabs.getInstance().closeFile((String) fileId);
             }
         }
         // If user no have dialog to stop close, we create one
         if (j == 0) {
             final int n = JOptionPane.showConfirmDialog(
                     Desktop.getInstance().getFrame(),
                     "Voulez vous vraiment continuer ?",
                     "Confirmation",
                     JOptionPane.YES_NO_OPTION);
             if (n == JOptionPane.YES_OPTION) {
                 return true;
             } else {
                 return false;
             }
         }
         j = 0;
         // Check save for each file
         for (Object fileId : haveToSave.keySet().toArray()) {
             id = (String) fileId;
             if (haveToSave.get(id)) {
                 // File has to be saved
                 // For number see saveFileIdBeforeClose() documentation about return
                 switch (saveFileIdBeforeClose(id)) {
                     case 1:
                         can_close[i] = true;
                         break;
                     case 0:
                         can_close[i] = false;
                         break;
                     case -1:
                         return false;
                 }
                 j++;
             } else // If file has not to be saved it's good
             {
                 can_close[i] = true;
             }
             if (can_close[i]) // If we can close this file, we close the tab
             {
                 JVSFileTabs.getInstance().closeFile(id);
             }
             i++;
         }
         // Check if a file is not save, if yes we can not close the application
         for (Boolean can_close_r : can_close) {
             if (can_close_r == false) {
                 return false;
             }
         }
         // We return true if all is good
         return true;
     }
 
     /** Ask to user to save a file before it close
      * @param fileId The file id
      * @return 1 meen that file is saved or that user not want to save the file. 0 meen that there was an error during the save of file. -1 meen that user want to stop all that happend (Cancel option).
      */
     public int saveFileIdBeforeClose(String fileId) {
         JVSFile file = JVSFileTabs.getInstance().getFile(fileId);
         int result = JOptionPane.showConfirmDialog(
                 Desktop.getInstance().getFrame(),
                 "Voulez vous enregistrer " + file.getName() + " avant de continuer ?");
         if (result == JOptionPane.YES_OPTION) {
             if (JVSFileTabs.getInstance().saveFile(fileId)) {
                 haveToSave.put(fileId, false);
                 return 1;
             } else {
                 return 0;
             }
         } else if (result == JOptionPane.NO_OPTION) {
             return 1;
         } else {
             haveToSave.put(fileId, true);
             return -1;
         }
     }
 
    /** Charge une nouvelle proglet dans l'interface utilisateur.
     * @param name Le nom de code de la proglet (ex:abcdAlgos)
     * @see org.javascool.core.ProgletEngine
     */
     public void loadProglet(String name) {
         Proglet proglet = ProgletEngine.getInstance().setProglet(name);
         System.gc();
         this.removeAll();
         JVSToolBar.getInstance().disableDemoButton();
         this.revalidate();
         this.add(JVSToolBar.getInstance(), BorderLayout.NORTH);
         this.add(JVSCenterPanel.getInstance(), BorderLayout.CENTER);
         this.revalidate();
         JVSCenterPanel.getInstance().revalidate();
         JVSCenterPanel.getInstance().setDividerLocation(getWidth() / 2);
         JVSCenterPanel.getInstance().revalidate();
         JVSWidgetPanel.getInstance().setProglet(name);
         if (proglet.hasDemo()) {
             JVSToolBar.getInstance().enableDemoButton();
         } else {
             JVSToolBar.getInstance().disableDemoButton();
         }
         this.newFile();
     }
 
     public void reportRuntimeBug(String ex) {
         StackTraceElement[] stack = Thread.currentThread().getStackTrace();
         int line = 0;
         for (StackTraceElement elem : stack) {
             if (elem.getFileName().startsWith("JvsToJavaTranslated")) {
                 line = elem.getLineNumber();
             } else {
                 System.err.println(elem.getClassName());
             }
         }
         if (JVSFileTabs.getInstance().getEditor(JVSFileTabs.getCurrentCompiledFile()) != null) {
             JVSFileTabs.getInstance().getEditor(JVSFileTabs.getCurrentCompiledFile()).signalLine(line);
         }
         ProgletEngine.getInstance().doStop();
         Dialog.error("Erreur du logiciel à la ligne " + line, ex);
     }
 
     public void reportApplicationBug(String ex) {
         Dialog.error("Erreur dans Java's Cool", ex);
     }
 
     public static class Dialog {
 
         /** Show a success dialog */
         public static void success(String title, String message) {
             JOptionPane.showMessageDialog(Desktop.getInstance().getFrame(), message, title, JOptionPane.INFORMATION_MESSAGE);
         }
 
         /** Show an error dialog */
         public static void error(String title, String message) {
             JOptionPane.showMessageDialog(Desktop.getInstance().getFrame(), message, title, JOptionPane.ERROR_MESSAGE);
         }
     }
 }
