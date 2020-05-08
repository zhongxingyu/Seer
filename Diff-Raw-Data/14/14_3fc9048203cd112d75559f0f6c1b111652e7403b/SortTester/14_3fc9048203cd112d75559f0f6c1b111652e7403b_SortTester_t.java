 package fatworm.tester;
 
 import fatworm.record.Schema;
 import fatworm.record.RecordFile;
 import fatworm.storage.Storage;
 import fatworm.dataentity.*;
 
 import static java.sql.Types.*;
 import java.util.Map;
 import java.util.HashMap;
 import java.util.Random;
 
 public class SortTester {
     public static final void main(String[] args) {
         SortTester tester = new SortTester();
         tester.test();
     }
 
     String dbName = "sort";
     String tableName = "sort";
     Random rand = new Random();
 
     public void test() {
        int length = 10000;
         Schema schema = new Schema();
         schema.addField("key", INTEGER, 4, true, false, false, null);
        schema.addField("value", CHAR, length, false, false, false, null);
 
         Storage.getInstance().createDatabase(dbName);
         Storage.getInstance().useDatabase(dbName);
         RecordFile rf = Storage.getInstance().insertTable(tableName, schema);
         
         if (rf == null)
             System.out.println("Create table failed");
 
         int n = 100000;
         for (int i = 0; i < n; ++i) {
             Map<String, DataEntity> map = new HashMap<String, DataEntity>();
             map.put("key", new Int(rand.nextInt()));
            map.put("value", new FixChar("", length));
             rf.insert(map);
         }
 
         Storage.getInstance().save();
     }
 }
