 package fatworm.storage;
 
 import fatworm.record.RecordFile;
 import fatworm.storage.bucket.Bucket;
 import fatworm.record.Schema;
 import fatworm.util.ByteLib;
 import fatworm.dataentity.*;
 
 import static java.sql.Types.*;
 import java.util.Map;
 
 public class Table implements RecordFile {
     private IOHelper io;
     private String name;
     private SchemaOnDisk schema;
 
     private Bucket head;
 
     private int front, rear;
     private int capacity;
 
     private Cell currentCell;
     private int currentIndex = -1;
     private boolean removed = false;
     
     private Table(IOHelper io, String name, int schema) {
         this.io = io;
         this.name = name;
         this.schema = SchemaOnDisk.load(io, schema);
         front = 0;
         rear = 0;
         capacity = 0;
 
         currentCell = null;
         currentIndex = 0;
         removed = false;
     }
 
     public static Table create(IOHelper io, String name, int schemaBlock) throws java.io.IOException {
         Table ret = new Table(io, name, schemaBlock);
         ret.front = Cell.create(io, ret.getSchema()).save();
         ret.rear = ret.front;
         ret.capacity = (io.getBlockSize() - 16) / (4 + ret.schema.estimatedTupleSize());
         if (ret.capacity == 0)
             ret.capacity = 1;
 
         ret.head = Bucket.create(io, ret.getHeadBytes());
         return ret;
     }
 
     private byte[] getHeadBytes() {
         byte[] data = new byte[12];
         ByteLib.intToBytes(front, data, 0);
         ByteLib.intToBytes(rear, data, 4);
         ByteLib.intToBytes(capacity, data, 8);
         return data;
     }
 
     public static Table load(IOHelper io, int block, String name, int schema) throws java.io.IOException {
         Table ret = new Table(io, name, schema);
         ret.head = Bucket.load(io, block);
         byte[] data = ret.head.getData();
         ret.front = ByteLib.bytesToInt(data, 0);
         ret.rear = ByteLib.bytesToInt(data, 4);
         ret.capacity = ByteLib.bytesToInt(data, 8);
         return ret;
     }
 
     public int save() throws java.io.IOException {
         head.setData(getHeadBytes());
         return head.save();
     }
 
     public void remove() {
         schema.remove();
         int next = front;
         do {
             Cell cell = Cell.load(io, getSchema(), next);
             cell.remove();
             next = cell.next();
         } while (next != 0);
         head.remove();
     }
 
     public boolean insert(Map<String, DataEntity> map) throws java.io.IOException {
         Cell cell = Cell.load(io, getSchema(), rear);
         Tuple tuple = Tuple.create(getSchema(), map);
         if (tuple == null)
             return false;
         cell.insert(tuple);
         cell.save();
         if (cell.tupleCount() >= capacity) {
             rear = Cell.create(io, getSchema()).save();
             save();
         }
         return true;
     }
 
     public boolean update(Map<String, DataEntity> map) throws java.io.IOException {
         if (!removed && currentCell != null && currentIndex >= 0 && currentIndex < currentCell.tupleCount()) {
             Tuple tuple = Tuple.create(getSchema(), map);
             if (tuple == null)
                 return false;
             currentCell.set(currentIndex, tuple);
             currentCell.save();
             return true;
         } else
             return false;
     }
 
     public Schema getSchema() {
         return schema.schema();
     }
 
     public void beforeFirst() {
         currentCell = Cell.load(io, getSchema(), front);
         currentIndex = -1;
         removed = false;
     }
 
     public boolean next() {
         int t = currentIndex + 1;
         if (t >= currentCell.tupleCount()) {
             do {
                 int nextCell = currentCell.next();
                 if (nextCell == 0)
                     return false;
                 currentCell = Cell.load(io, getSchema(), nextCell);
             } while (currentCell.tupleCount() == 0);
 
             currentIndex = 0;
             removed = false;
             return true;
         } else {
             currentIndex = t;
             removed = false;
             return true;
         }
     }
 
     public void delete() throws java.io.IOException {
         if (!removed && currentCell != null && currentIndex >= 0 && currentIndex < currentCell.tupleCount()) {
             currentCell.remove(currentIndex);
             currentCell.save();
             removed = true;
         }
     }
 
     private Tuple getTuple() {
         if (!removed && currentCell != null && currentIndex >= 0 && currentIndex < currentCell.tupleCount())
             return currentCell.get(currentIndex);
         else
             return null;
     }
 
     public boolean hasField(String name) {
         return getSchema().hasField(name);
     }
 
     public DataEntity getFieldByIndex(int index) {
         Tuple tuple = getTuple();
         if (tuple == null)
             return null;
         else
             return tuple.get(index);
     }
 
     public DataEntity getField(String name) {
         int i = getSchema().index(name);
         if (i == -1)
             return null;
         else
             return getFieldByIndex(i);
     }
 }
