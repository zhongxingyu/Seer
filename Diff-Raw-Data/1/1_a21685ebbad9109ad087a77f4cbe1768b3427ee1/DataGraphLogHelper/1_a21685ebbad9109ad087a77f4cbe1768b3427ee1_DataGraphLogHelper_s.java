 package org.concord.datagraph.log;
 
 import java.util.Date;
 
 import org.concord.framework.otrunk.OTObjectList;
 
 public class DataGraphLogHelper {
     
     public static void add(OTEventLog log, String name) {
         add(log, name, null);
     }
     
     public static void add(OTEventLog log, String name, String value) {
         try {
             OTEventLogItem item = log.getOTObjectService().createObject(OTEventLogItem.class);
             item.setName(name);
             item.setValue(value);
             item.setTime(new Date().getTime());
             log.getItems().add(item);
         }
         catch (Exception e) {
             e.printStackTrace();
         }
     }
     
     public static int getNumStarts(OTEventLog log) {
         OTObjectList items = log.getItems();
         int cnt = 0;
         for (int i = 0; i < items.size(); ++i) {
             OTEventLogItem item = (OTEventLogItem) items.get(i);
             if (item.getName().equals(OTEventLog.START)) {
                 ++cnt;
             }
         }
         return cnt;
     }
     
     public static long getTotalCollectionTime(OTEventLog log) {
         OTObjectList items = log.getItems();
         long sum = 0;
         long start = 0;
         for (int i = 0; i < items.size(); ++i) {
             OTEventLogItem item = (OTEventLogItem) items.get(i);
             String name = item.getName();
             if (name.equals(OTEventLog.START)) {
                 start = item.getTime();
             }
             else if (name.equals(OTEventLog.STOP)) {
                 sum += item.getTime() - start;
             }
         }
         return sum;
     }
 }
