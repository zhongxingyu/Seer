 package gui;
 
 import javax.swing.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.*;
 import java.util.Scanner;
 import net.infonode.docking.View;
 
 /**
  * Listen for menu actions.
  */
 class MenuListener implements ActionListener {
     static MenuListener me;
     JFileChooser FileDialog;
 
     /**
      * Initialize the menu listener.
      */
     private MenuListener() {
         FileDialog = new JFileChooser();
     }
 
     /**
      * Access the menu listener.
      *
      * @return The menu listener.
      */
     public static MenuListener me() {
         if (me == null)
             me = new MenuListener();
 
         return me;
     }
 
     /**
      * When the menu item is clicked. Dispatch to doCommand based on the menu item name.
      *
      * @param e The event.
      */
     public void actionPerformed(ActionEvent e) {
         doCommand(e.getActionCommand());
     }
 
     /**
      * Run a command.
      *
      * @param cmd The command to run.
      */
     private boolean doCommand(String cmd) {
         DocumentManager dm = MainFrame.me.Documents;
         
         // Create a new file in a new tab.
         if ("New".equals(cmd)) {
             dm.New();
         } 
         
         // Load a file into a new tab.
         else if ("Open".equals(cmd)) {
             View v = MainFrame.me.Root.getFocusedView();
             
             if (JFileChooser.APPROVE_OPTION == FileDialog.showOpenDialog(MainFrame.me())) 
                 return dm.Load(FileDialog.getSelectedFile());
             
             return false;
         } 
         
         // Save the currently active file.
         else if ("Save".equals(cmd)) {
             View v = MainFrame.me.Root.getFocusedView();
             
             if (v == null)
                 return false;
             
             if (!dm.HasFile(v))
                 return doCommand("Save as");
             else
                 return dm.Save(v);
         } 
         
         // Save the currently selected file with a new name.
         else if ("Save as".equals(cmd)) {
             View v = MainFrame.me.Root.getFocusedView();
             
             if (v == null)
                 return false;
             
             if (JFileChooser.APPROVE_OPTION == FileDialog.showSaveDialog(MainFrame.me()))
             {
                 dm.SetFile(v, FileDialog.getSelectedFile());
                 return doCommand("Save");
             }
             else
                 return false;
         } 
         
         // Close the active document (save if it had content).
         else if ("Close".equals(cmd)) {
             View v = MainFrame.me.Root.getFocusedView();
             
             if (v == null)
                 return false;
             
             if (!dm.IsEmpty(v))
                 if (!doCommand("Save"))
                     return false;
 
            v.close();
            
            if (dm.Views.getViewCount() == 2)
                 doCommand("New");
             
             return true;
         } 
         
         // Exit the program.
         else if ("Exit".equals(cmd)) {
             MainFrame.me().dispose();
             return true;
         } 
         
         // Run the currently selected file.
         else if ("Run".equals(cmd)) {
             View v = MainFrame.me.Root.getFocusedView();
             
             if (v == null)
                 return false;
             
             if (!dm.HasFile(v))
             {
                 if (doCommand("Save as"))
                     doCommand("Run");
             }
             else
             {
                 MainFrame.me.doCommand("(load \"" + dm.GetFile(v).getAbsolutePath().replace("\\", "/")  + "\")");
                 MainFrame.me().REPL.code.requestFocusInWindow();
                 return true;
             }
             
             return false;
         } 
         
         // Format the current code.
         else if ("Format".equals(cmd)) {
             View v = MainFrame.me.Root.getFocusedView();
             
             if (v == null)
                 return false;
             
             SchemeTextArea ss = (SchemeTextArea) v.getComponent();
             ss.format();
             return true;
         } 
         
         // Load the configuration file to edit.
         else if ("Edit configuration".equals(cmd)) {
 //            File f = new File(Options.FILENAME);
 //            if (!f.exists())
 //                exportFromJar(Options.FILENAME);
 //            return loadFile(f);
         } 
         
         // Load the syntax file to edit.
         else if ("Edit syntax highlighting".equals(cmd)) {
 //            File f = new File(SchemeDocument.FILENAME);
 //            if (!f.exists())
 //                exportFromJar(SchemeDocument.FILENAME);
 //            return loadFile(f);
         } 
         
         // Show the about dialog.
         else if ("About".equals(cmd)) {
             // TODO: Fix this
         } 
         
         // Explode. Horribly.
         else {
             // TODO: Fix this
         }
 
         return false;
     }
     
     /**
      * Grab a file from the JAR and make a real file out of it.
      * @param res 
      */
     private void exportFromJar(String res)
     {
         try
         {
             // Get the input stream from the JAR.
             InputStream fromJar;
             if ((fromJar = getClass().getResourceAsStream(res)) == null)
                 if ((fromJar = getClass().getResourceAsStream("/" + res)) == null)
                     throw new FileNotFoundException(res);
             
             // Get the output stream to the file.
             OutputStream toFile = new FileOutputStream(new File(res));
             
             // Copy.
             byte[] buf = new byte[8192];
             while (true) {
               int length = fromJar.read(buf);
               if (length < 0)
                 break;
               toFile.write(buf, 0, length);
             }     
             
             // Close buffers.
             fromJar.close();
             toFile.close();
         }
         catch (FileNotFoundException ex)
         {
             ex.printStackTrace();
         }
         catch (IOException ex)
         {
             ex.printStackTrace();
         }
             
     }
 }
