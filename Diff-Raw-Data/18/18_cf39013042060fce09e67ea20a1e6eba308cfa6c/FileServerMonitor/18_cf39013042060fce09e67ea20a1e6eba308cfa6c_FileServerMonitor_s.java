 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package dBox.Broker;
 
 import dBox.ServerUtils.DataAccess;
 import java.sql.SQLException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author harsimran.maan
  */
 public class FileServerMonitor extends Thread
 {
 
     @Override
     public void run()
     {
        int timeout = 15;
         while (true)
         {
             try
             {
                 DataAccess.updateOrInsertSingle("DELETE FROM ServerDetails WHERE now()-lastCheck > " + timeout);
             }
             catch (SQLException ex)
             {
                 Logger.getLogger(FileServerMonitor.class.getName()).log(Level.SEVERE, null, ex);
             }
             try
             {
                Thread.sleep(8000);
             }
             catch (InterruptedException ex)
             {
                 Logger.getLogger(FileServerMonitor.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
     }
 }
