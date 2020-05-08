 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package gui;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.io.Writer;
 import java.util.Scanner;
 
 import net.infonode.docking.*;
 import net.infonode.docking.util.*;
 
 /**
  *
  * @author John-Paul
  */
 public class DocumentManager {
     int lastIndex;
     TabWindow Documents;
     StringViewMap Views = new StringViewMap();
     
     /**
      * Manage documents.
      * @param views View map.
      * @param documents Document tab manager.
      */
     public DocumentManager(StringViewMap views, TabWindow documents) {
         lastIndex = 0;
         Views = views;
         Documents = documents;
     }
     
     /**
      * Create a new document.
      */
     public void New() {
         lastIndex++;
         
         String id = "document-" + lastIndex;
 
         Views.addView(id, new View("<new document>", null, new SchemeTextArea()));
         Documents.addTab(Views.getView(id));
        Views.getView(id).getComponent().requestFocusInWindow();
     }
     
     /**
      * Load a file.
      * @param file The file to load.
      * @return If the load worked.
      */
     public boolean Load(File file) {
         lastIndex++;
         
         String id = "document-" + lastIndex;
         String filename = file.getName();
         
         try {
             if (!file.exists())
                 file.createNewFile();
 
             // Get the new content.
             Scanner scanner = new Scanner(file);
             StringBuilder content = new StringBuilder();
             String NL = System.getProperty("line.separator");
 
             while (scanner.hasNextLine()) {
                 content.append(scanner.nextLine());
                 content.append(NL);
             }
 
             SchemeTextArea ss = new SchemeTextArea(content.toString());
             ss.myFile = file;
             Views.addView(id, new View(filename, null, ss));
             Documents.addTab(Views.getView(id));
            Views.getView(id).getComponent().requestFocusInWindow();
             
             return true;
         }
         catch(IOException ex) {
             ex.printStackTrace();
             return false;
         }
     }
     
     /**
      * Set the file used by a view. (For Save As)
      * @param view The view to set.
      * @param file The file to associate with that view.
      */
     public void SetFile(View view, File file) {
         if (Views.contains(view))
         {
             SchemeTextArea ss = (SchemeTextArea) view.getComponent();
             ss.myFile = file;
             view.setName(file.getName());
         }
     }
     
     /**
      * Get the file from a view.
      * @param view The view to get.
      * @return The file.
      */
     public File GetFile(View view) {
         if (Views.contains(view))
         {
             SchemeTextArea ss = (SchemeTextArea) view.getComponent();
             return ss.myFile;
         }
         return null;
     }
     
     /**
      * Check if the active view has a file.
      * 
      * @param view The view to check.
      * @return True if it has a file already.
      */
     public boolean HasFile(View view) {
         if (Views.contains(view))
         {
             SchemeTextArea ss = (SchemeTextArea) view.getComponent();
             return ss.myFile != null;
         }
         return false;
     }
     
     /**
      * Save the file contained in the given view.
      * 
      * @param view The view to save.
      * @return If the save worked.
      */
     public boolean Save(View view) {
         if (Views.contains(view))
         {
             SchemeTextArea ss = (SchemeTextArea) view.getComponent();
             if (ss.myFile == null)
                 return false;
             
             try {
                 Writer out = new OutputStreamWriter(new FileOutputStream(ss.myFile));
                 out.write(ss.getText());
                 out.flush();
                 out.close();
                 return true;
             } catch(FileNotFoundException ex) {
                 return false;
             } catch(IOException ex) {
                 return false;
             }
         }
         return false;
     }
     
     /**
      * Check if a view is empty.
      * @param view The view to check.
      * @return True/false.
      */
     public boolean IsEmpty(View view)
     {
         return !Views.contains(view) || ((SchemeTextArea) view.getComponent()).getText().length() == 0;
     }
 }
