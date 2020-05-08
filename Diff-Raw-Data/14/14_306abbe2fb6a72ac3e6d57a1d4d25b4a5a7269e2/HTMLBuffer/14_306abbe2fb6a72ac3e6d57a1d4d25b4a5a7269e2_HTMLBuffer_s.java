 package model;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 
 import javax.swing.JEditorPane;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 
 public class HTMLBuffer extends JEditorPane implements DocumentListener
 {
     private static final long serialVersionUID = 1L;
 
     private File file; // File that buffer is using
     private Boolean changed = false; // boolean to determine if the file has
                                      // changed or not
     private int width; // width bondary (for word wrapping)
 
     /**
      * HTMLBuffer constructor - takes in the initial file and width for editor
      * 
      * @param file
      * @param width
      */
     public HTMLBuffer(File file, int width)
     {
         this.file = file;
         this.width = width;
         this.setContentType("text/html");
     }
 
     /**
      * getFile - returns the file being used in the buffer
      * 
      * @return file
      */
     public File getFile()
     {
         return file;
     }
 
     /**
      * changed - sets changed in the document to true
      */
     public void changed()
     {
         changed = true;
     }
 
     /**
      * isWellFormed - checks if the HTML follows "WellFormed" standards
      * 
      * @return true if well formed, false if not
      */
     protected Boolean isWellFormed()
     {
         ArrayList<String> stack = new ArrayList<String>();
         String text = this.getText();
         return (text.split("<[^>]*>").length == text.split("<[^/>]*>").length);
     }
 
     /**
      * indent - indents the entire document or multiple lines
      * 
      * @param fullDoc
      *            if entire document needs to be indented, set this to true
      */
     protected void indent(Boolean fullDoc)
     {
 
     }
 
     /**
      * save - saves to the saveFile
      * 
      * @param saveFile
      * @return
      */
     protected boolean save(File saveFile)
     {
         if (!this.isWellFormed())
         {
             return false;
         }
         file = saveFile;
         PrintWriter out;
         try
         {
             out = new PrintWriter(file);
             file.createNewFile();
             out.println(this.getText());
             out.close();
             this.changed = false;
             return true;
         }
         catch (IOException e)
         {
             e.printStackTrace();
             return false;
         }
     }
 
     /**
      * save - to the current file
      */
    protected void save()
     {
        save(file);
     }
 
     /**
      * insert - inserts text into document at the Caret
      * 
      * @param text
      */
     protected void insert(String text)
     {
         Integer cursorPos = this.getCaretPosition();
         String textBefore = this.getText().substring(0, cursorPos);
         String textAfter = this.getText().substring(cursorPos + 1,
                 this.getText().length());
         String textNew = textBefore.concat(text).concat(textAfter);
         this.setText(textNew);
         this.changed();
     }
 
     @Override
     public void changedUpdate(DocumentEvent e)
     {
         this.changed();
     }
 
     @Override
     public void insertUpdate(DocumentEvent e)
     {
         this.changed();
     }
 
     @Override
     public void removeUpdate(DocumentEvent e)
     {
         this.changed();
     }
 }
