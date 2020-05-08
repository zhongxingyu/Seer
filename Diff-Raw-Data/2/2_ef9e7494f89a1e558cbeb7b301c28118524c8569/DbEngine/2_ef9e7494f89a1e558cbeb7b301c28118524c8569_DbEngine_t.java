 package edu.jhu.cs.damsl.engine.dbms;
 
 import java.util.HashMap;
 import java.util.List;
 
 import edu.jhu.cs.damsl.catalog.Defaults;
 import edu.jhu.cs.damsl.catalog.Schema;
 import edu.jhu.cs.damsl.catalog.identifiers.IndexId;
 import edu.jhu.cs.damsl.catalog.identifiers.TableId;
 import edu.jhu.cs.damsl.catalog.identifiers.TupleId;
 import edu.jhu.cs.damsl.engine.BaseEngine;
 import edu.jhu.cs.damsl.engine.storage.StorageEngine;
 import edu.jhu.cs.damsl.engine.storage.Tuple;
 import edu.jhu.cs.damsl.factory.file.StorageFileFactory;
 import edu.jhu.cs.damsl.engine.storage.file.StorageFile;
 import edu.jhu.cs.damsl.engine.storage.page.Page;
 import edu.jhu.cs.damsl.engine.storage.page.PageHeader;
 import edu.jhu.cs.damsl.engine.transactions.TransactionAbortException;
 
 public class DbEngine<IdType      extends TupleId,
                       HeaderType  extends PageHeader,
                       PageType    extends Page<IdType, HeaderType>,
                       FileType    extends StorageFile<IdType, HeaderType, PageType> >
                 extends BaseEngine
 {
 
   StorageEngine<IdType, HeaderType, PageType, FileType>  storage;
   
   public DbEngine(StorageFileFactory<IdType, HeaderType, PageType, FileType> f)
   {
     storage = new StorageEngine<IdType, HeaderType, PageType, FileType> (catalog, f);
     f.initialize(this);
   }
 
   public DbEngine(String catalogFile,
                   StorageFileFactory<IdType, HeaderType, PageType, FileType> f)
   {
     super(catalogFile);
     storage = new StorageEngine<IdType, HeaderType, PageType, FileType>(catalog, f);
     f.initialize(this);
   }
 
   // Accessors
   public StorageEngine<IdType, HeaderType, PageType, FileType> 
   getStorageEngine() { return storage; }
 
   // Relations
   public boolean hasRelation(String tableName) {
     return catalog.getTableNames().contains(tableName);
   }
 
   public TableId getRelation(String tableName) {
     return catalog.getTableByName(tableName).getId();
   }
 
   public TableId addRelation(String tableName, Schema schema) {
     TableId tid = null;
     try { 
      tid = catalog.addTable(tableName, schema);
       storage.addRelation(null, tid, schema);
     } catch (TransactionAbortException e) {
       logger.error("Failed to add relation {}", tableName);
     }
     return tid;
   }
 
   public String toString() {
     String[] storageLines = storage.toString().split("\\r?\\n");
     String r = "==== Storage Engine ====\n";
     for (String s : storageLines) { r += "  "+s+"\n"; }
     return r;
   }
 
   // Indexes.
   public IndexId addIndex(String tableName, Schema keySchema) {
     IndexId r = null;
     try {
       TableId tid = getRelation(tableName);
       if ( tid != null ) {
         r = catalog.addIndex(tid, keySchema);
         storage.addIndex(null, tid, r);
       }
     } catch (TransactionAbortException e) {
       logger.error("Failed to add index on {}", tableName);
     }
     return r;
   }
 
   public List<IndexId> getIndexes(TableId rel) {
     return catalog.getIndexes(rel);
   }
 
 }
