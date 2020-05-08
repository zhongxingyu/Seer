 import java.util.*;
 import com.sleepycat.db.*;
 
 public class recordDB {
   public static Database title_DB;
   public static Database artists_DB;
   public static Database userrat_DB;
   
   public static void configDB() {
     try {
       DatabaseConfig dbConfig = new DatabaseConfig();
       dbConfig.setType(DatabaseType.BTREE);
       //dbConfig.setSortedDuplicates(true);
       dbConfig.setAllowCreate(true);
       title_DB = new Database("title.db", null, dbConfig);
       dbConfig.setSortedDuplicates(true);
       artists_DB = new Database("artists.db", null, dbConfig);
      //dbConfig.setSortedDuplicates(false);
       userrat_DB = new Database("userrat.db", null, dbConfig);
       OperationStatus opStat;
 
       populateDB();
     } catch (Exception e) {
       e.getMessage();
     }
   }
   private static void populateDB() {
     try {
       DatabaseEntry key, data;
       key = new DatabaseEntry();
       data = new DatabaseEntry();
       for (int i=0; i<Record.recordSize(); i++) {
         //System.out.println(Record.recordSize());
         String id = Record.getID(i).toString();
         key = new DatabaseEntry();
         key.setData(id.getBytes());
         key.setSize(id.length());
         data.setData(Record.getTitle(i).getBytes());
         data.setSize(Record.getTitle(i).length());
         //System.out.println(id);
         title_DB.put(null, key, data);
         data = new DatabaseEntry();
         String[] art = Record.getArtists(i);
         for (int j=0; j<art.length; j++) {
           data.setData(art[j].getBytes());
           data.setSize(art[j].length());
           
           artists_DB.put(null, key, data);
           data = new DatabaseEntry();
         }
         
         String[] user = Record.getUsers(i);
         for (int j=0; j<user.length; j++) {
           String pair = user[j].concat(",").concat(Record.getRating(i, user[j]).toString());
           data.setData(pair.getBytes());
           data.setSize(pair.length());
           
           userrat_DB.put(null, key, data);
           
           data = new DatabaseEntry();
         }
         //title_DB.get(null, key, data, LockMode.DEFAULT);
         //System.out.println(new String(key.getData()) + " " + new String(data.getData()));
         key = new DatabaseEntry();
         
       }
     } catch (Exception e) {
       e.getMessage();
     }
   }
 }
