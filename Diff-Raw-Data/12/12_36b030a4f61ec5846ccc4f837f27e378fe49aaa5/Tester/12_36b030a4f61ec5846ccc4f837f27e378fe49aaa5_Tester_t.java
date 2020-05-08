 package fatworm.storage;
 
 import fatworm.storage.bplustree.*;
 import fatworm.record.Schema;
 import fatworm.record.RecordFile;
 import fatworm.dataentity.*;
 
 import java.util.Random;
 import java.util.Map;
 import java.util.HashMap;
 import java.math.BigDecimal;
 import static java.sql.Types.*;
 
 public class Tester {
     public static final void main(String[] args) throws java.io.FileNotFoundException, java.io.IOException {
         Tester tester = new Tester();
         tester.test();
     }
 
     public void test() throws java.io.FileNotFoundException, java.io.IOException {
         Random rand = new Random();
 
         Schema schema = new Schema();
         schema.addField("id", INTEGER, 4, true, true, true, new NullDataEntity());
         schema.addField("name", VARCHAR, 20, true, false, false, new NullDataEntity());
         schema.addField("age", INTEGER, 4, true, false, false, new NullDataEntity());
         schema.addField("school", CHAR, 40, false, false, false, new NullDataEntity());
         schema.addField("young_pioneer", BOOLEAN, 1, false, false, false, new NullDataEntity());
         schema.addField("birth_date", DATE, 8, false, false, false, new NullDataEntity());
         schema.addField("last_showup", TIMESTAMP, 8, false, false, false, new NullDataEntity());
         schema.addField("weight", DECIMAL, 5, false, false, false, new NullDataEntity());
         schema.addField("height", FLOAT, 8, false, false, false, new NullDataEntity());
 
        Storage storage = Storage.getInstance();
         storage.createDatabase("lichking");
         storage.useDatabase("lichking");
         RecordFile table = storage.insertTable("loli", schema);
         for (int i = 0; i < 100000; ++i) {
             Map<String, DataEntity> row = new HashMap<String, DataEntity>();
             row.put("name", new VarChar("Alice_" + i));
             row.put("id", new Int(i));
             row.put("age", new Int(rand.nextInt(7) + 7));
             row.put("school", new FixChar("Dalaran Higher School of Magic", 40));
             row.put("young_pioneer", new Bool(rand.nextBoolean()));
             row.put("birth_date", new DateTime(new java.sql.Timestamp(rand.nextLong())));
             row.put("last_showup", new DateTime(new java.sql.Timestamp(rand.nextLong())));
             row.put("height", new fatworm.dataentity.Float(rand.nextDouble() * 100));
             table.insert(row);
         }
         storage.save();
 
         printTable("lichking", "loli");
 
         /*
         table.beforeFirst();
         while (table.next()) {
             if (table.getField("id").equals(new Int(1)))
                 table.delete();
             else if (table.getField("id").equals(new Int(5))) {
                 Map<String, DataEntity> map = new HashMap<String, DataEntity>();
                 map.put("age", new Int(22));
                 table.update(map);
             }
         }
         Map<String, DataEntity> map = new HashMap<String, DataEntity>();
         map.put("name", new VarChar("Elly"));
         table.insert(map);
 
         storage.save();
 
         printTable("lichking", "loli");*/
     }
 
     public void printTable(String db, String tablename) {
        Storage storage = Storage.getInstance();
         if (!storage.useDatabase(db))
             System.out.println("Database " + db + " not found");
         RecordFile table = storage.getTable(tablename);
         if (table == null)
             System.out.println("Table " + table + " not found");
         if (table.getSchema() == null)
             System.out.println("Schema not found");
         for (String field: table.getSchema().fields())
             System.out.print(field + "\t");
         System.out.println();
         table.beforeFirst();
         int count = 0;
         while (table.next()) {
             ++count;
             for (int i = 0; i < table.getSchema().columnCount(); ++i)
                 System.out.print(table.getFieldByIndex(i) + "\t");
             System.out.println();
         }
         System.out.println(count + " tuples in total.");
         storage.save();
     }
 }
