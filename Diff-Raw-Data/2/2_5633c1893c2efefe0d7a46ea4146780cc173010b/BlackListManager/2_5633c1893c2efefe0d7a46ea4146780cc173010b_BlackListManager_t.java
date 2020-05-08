 package mediateka.datamanagers;
 
 import mediateka.db.*;
 
 /**
  * Менеджер черного списка
  * @author DeKaN
  */
 public class BlackListManager implements RecordsManager {
 
     private Blacklist blackList = null;
 
     BlackListManager(String fileName) throws Exception {
         blackList = new Blacklist();
         if (!blackList.load(fileName)) {
            throw new Exception("Черный список не загружен!");
         }
     }
 
     public boolean add(Record record) {
         if (record == null) {
             throw new NullPointerException();
         }
         return blackList.add(record);
     }
 
     public boolean delete(int id) {
         return blackList.delete(find(id));
     }
 
     public boolean edit(int id, Record newData) {
         if (newData == null) {
             throw new NullPointerException();
         }
         return blackList.update(find(id), newData);
     }
 
     public Record find(int id) {
         return blackList.find(new BlackListRecord(id)).getRecord(0);
     }
 
     public Records find(Record record) {
         if (record == null) {
             throw new NullPointerException();
         }
         return blackList.find(record);
     }
     
     public Record[] getRecords() {
         return blackList.ToArray();
     }
 }
