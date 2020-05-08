 package model;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Observable;
 
 import view.EditorView;
 
 public class Session extends Observable
 {
     /**
      * Creates the htmledit session, and launches the GUI
      */
     public ArrayList<HTMLBuffer> bufferList;
     private Mediator mediator;
     private boolean autoIndent;
     private boolean autoWrap;
     
     public void setMediator(Mediator mediator)
     {
         this.mediator = mediator;
     }
 
     /**
      * @param args
      */
     public void setAutoIndent()
     {
         this.autoIndent = !autoIndent;
     }
 
     public void open(File file)
     {
         bufferList.add(new HTMLBuffer(file, 80));
         this.setChanged();
         this.notifyObservers();
     }
 
     public void setAutoWrap()
     {
         this.autoWrap = !autoWrap;
     }
 
     public void setIndentSpacing(int level, int spaces)
     {
         // TODO Auto-generated method stub
 
     }
 
     public void saveAll()
     {
         for (HTMLBuffer buffer : bufferList)
         {
            if (!buffer.close())
             {
 
             }
         }
     }
 
     public HTMLBuffer getCurrentBuffer()
     {
         return null;
     }
 
     public void close()
     {
         for (HTMLBuffer buffer : bufferList)
         {
             if (buffer.isWellFormed())
             {
                 this.close(buffer);
             }
             else
             {
 
             }
         }
     }
 
     public void close(HTMLBuffer buffer)
     {
         bufferList.remove(buffer);
         this.setChanged();
         this.notifyObservers();
         if (bufferList.isEmpty())
         {
             System.exit(0);
         }
     }
 
     public void newFile()
     {
         bufferList.add(new HTMLBuffer(new File("New Document"), 80));
         this.setChanged();
         this.notifyObservers();
     }
 }
