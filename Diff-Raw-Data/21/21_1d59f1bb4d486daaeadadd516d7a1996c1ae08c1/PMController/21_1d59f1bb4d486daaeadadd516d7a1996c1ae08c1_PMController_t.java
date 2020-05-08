 package dashteacup.pman;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.io.IOException;
 
 import javax.swing.JFileChooser;
 import javax.swing.JOptionPane;
 import javax.swing.SwingUtilities;
 
 /**
  * Controller class for the Password Management (PM) Application.
  */
 public class PMController {
 
     private static final String defaultDirectory = "testfiles";
 
     /**
     * The application's view.
      */
     private PMView view;
 
     /**
      * The application's model.
      */
     private PasswordManager model;
 
     /**
      * File chooser for selecting password files to manipulate.
      */
     private JFileChooser fileChooser;
 
     /**
      * The currently loaded file.
      */
     private File currentFile;
 
     /**
      * Runner method
      * @param args not currently used
      */
     public static void main(String[] args) {
         SwingUtilities.invokeLater(new Runnable() {
             @Override
             public void run() {
                 new PMController();
             }
         });
     }
 
     /**
      * Create the Application's Controller.
      */
     public PMController() {
         view = new PMView();
         model = new PasswordManager();
         fileChooser = new JFileChooser(defaultDirectory);
         fileChooser.setFileFilter(new PmanFileFilter());
         currentFile = null;
         addListeners();
     }
 
     /**
      * Restore the application to its initial state with no file open.
      */
     public void resetController() {
         model = new PasswordManager();
         view.hideTextArea();
         currentFile = null;
     }
 
     /**
      * Add all the action listeners to their components in the view.
      */
     private void addListeners() {
         view.addNewFileListener(new NewFileAction());
         view.addOpenFileListener(new OpenFileAction());
         view.addCloseFileListener(new CloseFileAction());
         view.addSaveFileListener(new SaveFileAction());
         view.addSaveAsFileListener(new SaveAsFileAction());
     }
 
     /**
      * Action performed when the user creates a new password file.
      */
     private class NewFileAction implements ActionListener {
         @Override
         public void actionPerformed(ActionEvent e) {
             int choice = JOptionPane.NO_OPTION;
             if (bufferHasChanged()) {
                 choice = handleUnsavedChanges();
             }
             if (choice == JOptionPane.NO_OPTION || choice == JOptionPane.YES_OPTION) {
                 model = new PasswordManager();
                 view.setText("");
                 view.showTextArea();
                 // We don't actually have a file object for this thing yet.
                 currentFile = null;
             }
         }
     }
 
     /**
      * Action performed when the user opens a password file.
      */
     private class OpenFileAction implements ActionListener {
         @Override
         public void actionPerformed(ActionEvent event) {
             //TODO:unsaved changes
             int returnStatus = fileChooser.showOpenDialog(view.getMainWindow());
             if (returnStatus == JFileChooser.APPROVE_OPTION) {
                 File file = fileChooser.getSelectedFile();
                 char[] password = view.showPasswordDialog();
                 if (password == null) {
                     // User cancelled the password dialog. Do nothing.
                     return;
                 }
                 try {
                     model.openPasswordFile(file, password);
                     view.showTextArea();
                     view.setText(model.getText());
                     currentFile = file;
                 }
                 catch (BadPasswordException e) {
                     view.showMessageDialog("Invalid password for file: " + file.getName());
                     resetController();
                 }
                 catch (InvalidPasswordFileException e) {
                     System.err.println(e.getMessage());
                     view.showMessageDialog("Password file: " + file.getName() + " is not a properly formatted .pman file.");
                     resetController();
                 }
                 catch (IOException e) {
                     view.showMessageDialog("Error writing file: " + file.getName());
                     e.printStackTrace();
                     resetController();
                 }
             }
             // Do nothing if they cancel the open dialog.
         }
     }
 
     /**
      * Action performed when the user closes a password file.
      */
     private class CloseFileAction implements ActionListener {
         @Override
         public void actionPerformed(ActionEvent event) {
             int choice = JOptionPane.NO_OPTION;
             if (bufferHasChanged()) {
                 choice = handleUnsavedChanges();
             }
             if (choice == JOptionPane.NO_OPTION || choice == JOptionPane.YES_OPTION) {
                 resetController();
             }
         }
     }
 
     /**
      * Action performed when the user saves a password file.
      */
     private class SaveFileAction implements ActionListener {
         @Override
         public void actionPerformed(ActionEvent event) {
             if (bufferHasChanged()) {
                 if (fileIsLoaded()) {
                     // ignore return value
                     saveBufferToCurrentFile();
                 }
                 else {
                     // ignore return value
                     saveBufferAsFile();
                 }
             }
             // If the buffer hasn't changed then do nothing.
         }
     }
 
     /**
      * Action performed when the user saves a password file with a new name.
      */
     private class SaveAsFileAction implements ActionListener {
         @Override
         public void actionPerformed(ActionEvent event) {
             // ignore return value
             saveBufferAsFile();
         }
     }
 
     /**
      * Determine if the currently loaded text buffer has changed from the file's
      * originally loaded contents.
      * @return true if the buffer has changed, false otherwise.
      */
     private boolean bufferHasChanged() {
         // view.getText will not be null
         return ! (view.getText().equals(model.getText()));
     }
 
     /**
      * Determine if we have an open file.
      * @return true if we are editing a file, false otherwise.
      */
     private boolean fileIsLoaded() {
         return (currentFile != null);
     }
 
     /**
      * Save the text buffer to the currently loaded file.
      * @return YES_OPTION if the user saves the changes,
      * CANCEL_OPTION if the user cancels the action.
      */
     private int saveBufferToCurrentFile() {
         // Sanity check. I shouldn't call this without having a file loaded.
         if (!fileIsLoaded()) {
             return JOptionPane.CANCEL_OPTION;
         }
 
         model.setText(view.getText());
         char[] password = view.showPasswordDialog();
         // Make sure the user didn't cancel the password dialog.
         if (password != null) {
             String filename = currentFile.getAbsolutePath();
             try {
                 // TODO: At present I'm making it get a new password
                 // every time I save the file. I should probably
                 // figure out some way to hold on to the key used to
                 // encrypt the file so I can reuse that in a current
                 // instance. I will NOT save the password itself,
                 // that's bad.
                 model.savePasswordFile(filename, password);
                 return JOptionPane.YES_OPTION;
             }
             catch (IOException e) {
                 view.showMessageDialog("Error writing file: " + filename);
                 e.printStackTrace();
             }
         }
         return JOptionPane.CANCEL_OPTION;
     }
 
     /**
      * Save the text buffer to a file selected by the user.
      * @return YES_OPTION if the user saves the changes,
      * CANCEL_OPTION if the user cancels the action.
      */
     private int saveBufferAsFile() {
         int returnStatus = fileChooser.showSaveDialog(view.getMainWindow());
         if (returnStatus == JFileChooser.APPROVE_OPTION) {
             currentFile = fileChooser.getSelectedFile();
             return saveBufferToCurrentFile();
         }
         return JOptionPane.CANCEL_OPTION;
     }
 
     /**
      * Display a dialog asking the user if they want to save their changes
      * and if so, lets them.
      * @return YES_OPTION if the user chooses to save the changes,
      * NO_OPTION if the user decides not to save the changes,
      * CANCEL_OPTION if the user cancels the action.
      */
     private int handleUnsavedChanges() {
         int choice = view.unsavedChangesDialog();
         if (choice == JOptionPane.YES_OPTION) {
             if (fileIsLoaded()) {
                 return saveBufferToCurrentFile();
             }
             else {
                 return saveBufferAsFile();
             }
         }
         return choice;
     }
 
 }
 
