 package model;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 
 import javax.swing.JEditorPane;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 import javax.swing.text.BadLocationException;
 
 public class HTMLBuffer extends JEditorPane implements DocumentListener
 {
     private static final long serialVersionUID = 1L;
 
     private File file; // File that buffer is using
     private Boolean changed = false; // boolean to determine if the file has
                                      // changed or not
     private int width; // width bondary (for word wrapping)
     private Mailbox mailbox;
     private Mediator mediator;
     
 
     /**
      * HTMLBuffer constructor - takes in the initial file and width for editor
      * 
      * @param file
      * @param width
      */
     public HTMLBuffer(File file, int width, Mediator mediator, Mailbox mailbox)
     {
         this.file = file;
         this.width = width;
        this.setContentType("text/html");
         this.mediator = mediator;
         this.mailbox = mailbox;
         try
         {
             BufferedReader reader = new BufferedReader(new FileReader(file));
             String next=reader.readLine();
             while(next!=null){
                this.setText(this.getText()+next);
                 next=reader.readLine();
             }
         }
         catch (FileNotFoundException e)
         {
         }
         catch (IOException e)
         {
             // TODO Auto-generated catch block
             e.printStackTrace();
         } 
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
      * getChanged - returns changed
      * 
      * @return changed
      */
     public Boolean getChanged()
     {
         return changed;
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
         int start;
         int end;
         if (fullDoc)
         {
             start = 0;
             end = this.countNewLines(this.getText());
         }
         else
         {
             int dot = this.getCaret().getDot(); // start of selection
             int mark = this.getCaret().getMark(); // end of selection
             try
             {
                 start = this.countNewLines(this.getText(0, dot));
                 end = this.countNewLines(this.getText(dot, mark - dot));
             }
             catch (BadLocationException e)
             {
                 e.printStackTrace();
                 start = 0;
                 end = 0;
             }
         }
 
         for (int i = start; i < end; i++)
         {
             this.indent(i);
         }
 
     }
 
     /**
      * indents - one line
      * 
      * @param line number to indent
      */
     private void indent(int line)
     {
         // TODO indent
         // save cursor position
         // move to the beginning of the line
         // indent from the mediant session array list
         // move cursor back to position + indent length
     }
 
     /**
      * countNewLine - counts the new lines in the string
      * 
      * @param str
      *            string checked
      * @return the number of new lines
      */
     public int countNewLines(String str)
     {
         return str.split("\\n").length - 1;
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
      * @return 
      */
     protected boolean save()
     {
         return save(file);
     }
 
     /**
      * insert - inserts text into document at the Caret
      * 
      * @param text
      */
     protected void insert(String text)
     {
         Integer cursorPos = this.getCaret().getDot();
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
