 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package dBox.Server;
 
 import dBox.ServerUtils.DataAccess;
 import java.sql.SQLException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author harsimran.maan
  */
 public class AliveCheck extends Thread
 {
 
     private String server;
     private final int clusterId;
     private int serverIndex;
 
     public AliveCheck(String server, int port, int clusterId)
     {
         this.server = server;
         this.clusterId = clusterId;
         try
         {
             DataAccess.updateOrInsertSingle("INSERT INTO ServerDetails VALUES('" + server + "'," + port + ",(SELECT m FROM (SELECT IFNULL(MAX(serverIndex),0)+1 AS m FROM ServerDetails WHERE clusterId = " + clusterId + " ) AS M), now()," + clusterId + ",(SELECT m FROM(SELECT servername as m FROM ServerDetails WHERE clusterId = " + clusterId + " AND serverIndex= (SELECT MAX(serverIndex) FROM ServerDetails WHERE clusterId =  " + clusterId + ") ) AS M))");
             serverIndex = new PeerDetailsGetter().getServerDetails(server).getServerIndex();
            DataAccess.updateOrInsertSingle("DELETE FROM ServerSync WHERE servername='" + server + "'");
 
         }
         catch (SQLException ex)
         {
             Logger.getLogger(AliveCheck.class.getName()).log(Level.SEVERE, null, ex);
         }
         catch (Exception ex)
         {
             Logger.getLogger(AliveCheck.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     private void startHeartbeat()
     {
         while (true)
         {
             try
             {
                 DataAccess.updateOrInsertSingle("UPDATE ServerDetails SET lastCheck=now() WHERE servername='" + server + "'");
                 DataAccess.updateOrInsertSingle("UPDATE ServerDetails SET monitoring = (SELECT m FROM(SELECT MAX(sd.serverIndex) as m from ServerDetails sd where sd.clusterId =" + clusterId + " AND sd.serverIndex < " + serverIndex + ") AS M) WHERE servername = '" + server + "' AND monitoring IS NULL");
             }
             catch (SQLException ex)
             {
                 Logger.getLogger(AliveCheck.class.getName()).log(Level.SEVERE, null, ex);
             }
             try
             {
                 Thread.sleep(10000);
             }
             catch (InterruptedException ex)
             {
                 Logger.getLogger(AliveCheck.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
     }
 
     @Override
     public void run()
     {
         this.startHeartbeat();
     }
 }
