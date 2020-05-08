 package fatworm.storage;
 
 import fatworm.storagemanager.*;
 import fatworm.record.RecordFile;
 import fatworm.record.Schema;
 
 import java.io.File;
 import java.util.Map;
 import java.util.HashMap;
 
 public class Storage implements StorageManagerInterface {
     private Database current = null;
     private String currentName = "";
     private Map<String, Database> map = new HashMap<String, Database>();
     private String path = "test" + java.io.File.separator;
 
     private static Storage instance = null;
 
     private Storage() {
     }
 
    public static Storage getInstance() {
         if (instance == null)
             instance = new Storage();
         return instance;
     }
 
     private String fileName(String dbName) {
         return path + dbName + ".db";
     }
 
 	public boolean createDatabase(String name) {
         File f = new File(fileName(name));
         if (f.exists())
             return false;
         else {
             try {
                 Database db = new Database(fileName(name));
                 map.put(name, db);
                 return true;
             } catch (java.io.FileNotFoundException e) {
                 return false;
             } catch (java.io.IOException e) {
                 return false;
             }
         }
     }
 
 	public boolean useDatabase(String name) {
         File f = new File(fileName(name));
         if (!f.exists())
             return false;
 
         Database db = map.get(name);
         if (db == null) {
             try {
                 db = new Database(fileName(name));
                 map.put(name, db);
             } catch (java.io.IOException e) {
                 return false;
             }
         }
 
         current = db;
         currentName = name;
 
         return true;
     }
 
     public boolean dropDatabase(String name) {
         File file = new File(fileName(name));
         if (!file.exists())
             return false;
         if (!file.delete())
             return false;
 
         map.remove(name);
         if (current != null && currentName.equals(name))
             current = null;
 
         return true;
     }
 
     public RecordFile getTable(String tablename) {
         try {
             if (current == null)
                 return null;
             else
                 return current.getTable(tablename);
         } catch (java.io.IOException e) {
             return null;
         }
     }
 
 	public RecordFile insertTable(String tablename, Schema schema) {
         if (current != null) {
             try {
                 return current.insertTable(tablename, schema);
             } catch (java.io.IOException e) {
                 return null;
             }
         } else
             return null;
     }
 
 	public void dropTable(String name) {
         if (current != null) {
             try {
                 current.dropTable(name);
             } catch (java.io.IOException e) {
             }
         }
     }
 
     public void save() {
         if (current != null) {
             try {
                 current.save();
             } catch (java.io.IOException e) {
             }
         }
     }
 
     public void setPath(String path) {
         if (!path.endsWith(java.io.File.separator))
             path += java.io.File.separator;
         this.path = path;
     }
 }
