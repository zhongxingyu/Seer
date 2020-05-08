 package pv168.project.swing;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import pv168.project.Book;
 import pv168.project.Disk;
 
 import javax.swing.*;
 

 /**
  * Created with IntelliJ IDEA.
  * User: m4r10
  * Date: 5/21/13
  * Time: 6:47 PM
  * To change this template use File | Settings | File Templates.
  */
 public class SaveToDBSwingWorker extends SwingWorker<Integer,Void> {
 
     final static Logger log = LoggerFactory.getLogger(SaveToDBSwingWorker.class);
 
     @Override
     protected Integer doInBackground() throws Exception {
         int counter = 0;
 
         BooksTableModel modelBooks = MainWindow.thisWindow.getModelBooks();
         DisksTableModel modelDisks = MainWindow.thisWindow.getModelDisks();
 
         MainWindow.thisWindow.getManagerEntities().removeAll();
 
         for(Book book : modelBooks.getAll())
         {
             MainWindow.thisWindow.getManagerEntities().addEntity(book);
             counter++;
         }
 
         log.info("Books were saved to DB. Count: " + counter);
         counter = 0;
 
 
         for(Disk disk : modelDisks.getAll())
         {
             MainWindow.thisWindow.getManagerEntities().addEntity(disk);
             counter++;
         }
 
         log.info("Disks were saved to DB. Count: " + counter);
 
         return 0;
     }
 
 }
