 package org.kevoree.library.sky.lxc;
 
 
 
 import org.kevoree.library.sky.lxc.utils.FileManager;
 import org.kevoree.log.Log;
 
 import java.io.File;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: jed
  * Date: 26/07/13
  * Time: 12:49
  * To change this template use File | Settings | File Templates.
  */
 public class WatchBackupsContainers implements Runnable {
 
     private LxcHostNode lxcHostNode;
     private LxcManager lxcManager;
     private final static String backuppath = "/usr/share/kevoree/lxc/backup";
     private int daysretentions = 30;
 
     public WatchBackupsContainers(LxcHostNode lxcHostNode, LxcManager lxcManager) {
         this.lxcHostNode = lxcHostNode;
         this.lxcManager = lxcManager;
     }
 
     @Override
     public void run() {
         List<String> lxNodes = lxcManager.getBackupContainers();
         Date today = new Date();
         Log.debug("WatchBackupsContainers is starting");
         for(String node : lxNodes){
 
             File lxcnode = new File(backuppath+File.separatorChar+node);
            lxcnode.mkdirs();
 
             // days old
             long diff = (today.getTime() -  lxcnode.lastModified()) / (1000*60*60*24);
 
             if(diff > daysretentions ){
                 Log.info("WatchBackupsContainers is destroying "+node);
                 // backup has more than 30 days
                 FileManager.deleteOldFile(lxcnode);
             } else {
                 Log.debug("WatchBackupsContainers " + node + " is " + diff + " days old");
             }
         }
     }
 
     public int getDaysretentions() {
         return daysretentions;
     }
 
     public void setDaysretentions(int daysretentions) {
         this.daysretentions = daysretentions;
     }
 }
