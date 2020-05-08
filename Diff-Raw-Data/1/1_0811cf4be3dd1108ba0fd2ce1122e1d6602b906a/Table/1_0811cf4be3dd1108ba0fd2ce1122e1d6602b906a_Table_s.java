 package fatworm.storage;
 
 import fatworm.record.RecordFile;
 import fatworm.record.Schema;
 import fatworm.record.RecordIterator;
 import fatworm.storage.bucket.Bucket;
 import fatworm.util.ByteBuffer;
 import fatworm.util.Predicate;
 import fatworm.dataentity.*;
 import fatworm.storage.bplustree.*;
 import fatworm.storage.bplustree.BPlusTree.NodeIterator;
 
 import static java.sql.Types.*;
 import java.util.Map;
 import java.util.Iterator;
 import java.util.List;
 import java.util.HashSet;
 
 public class Table implements RecordFile {
     private IOHelper io;
     private SchemaOnDisk schema;
 
     private Bucket head;
 
     private int front, rear;
     private Cell rearCell;
     private int capacity;
 
     private RecordIterator scanIter;
 
     private boolean dirty;
 
     private Table(IOHelper io, int schema) {
         this.io = io;
         if (schema == 0)
             this.schema = null;
         else
             this.schema = SchemaOnDisk.load(io, schema);
 
         front = 0;
         rear = 0;
         rearCell = null;
         capacity = 0;
         dirty = false;
 
         scanIter = scan();
     }
 
     public static Table create(IOHelper io, int schemaBlock) {
         return createRaw(io, schemaBlock, 400);
     }
 
     private static Table createRaw(IOHelper io, int schemaBlock, int tupleSize) {
         try {
             Table ret = new Table(io, schemaBlock);
             ret.rearCell = Cell.create(io);
             ret.front = ret.rearCell.save();
             ret.rear = ret.front;
             if (ret.schema != null)
                 ret.capacity = (io.getBlockSize() - 16) / (ret.schema.estimatedTupleSize());
             else
                 ret.capacity = (io.getBlockSize() - 16) / (tupleSize);
             if (ret.capacity == 0)
                 ret.capacity = 1;
 
             ret.head = Bucket.create(io);
             return ret;
         } catch (java.io.IOException e) {
             return null;
         }
     }
 
     static Table createTemp(IOHelper io, int tupleSize) {
         return createRaw(io, 0, tupleSize);
     }
 
     private void getHeadBytes(ByteBuffer buffer) {
         buffer.putInt(front);
         buffer.putInt(rear);
         buffer.putInt(capacity);
     }
 
     public static Table load(IOHelper io, int block, int schema) {
         Table ret = new Table(io, schema);
         ret.head = Bucket.load(io, block);
         byte[] data = ret.head.getData();
         ByteBuffer buffer = new ByteBuffer(data);
         ret.front = buffer.getInt();
         ret.rear = buffer.getInt();
         ret.capacity = buffer.getInt();
         ret.rearCell = Cell.load(io, ret.rear);
         return ret;
     }
 
     public int save() throws java.io.IOException {
         if (dirty) {
             ByteBuffer buffer = new ByteBuffer();
             getHeadBytes(buffer);
             head.setData(buffer.array());
             int headBlock = head.save();
             if (rearCell != null)
                 rearCell.save();
             dirty = false;
             return headBlock;
         } else
             return head.block();
     }
 
     public void remove() {
         try {
             schema.remove();
             int next = front;
             do {
                 Cell cell = loadCell(next);
                 cell.remove();
                 next = cell.getNext();
             } while (next != 0);
             head.remove();
             dirty = false;
         } catch (java.io.IOException e) {
         }
     }
 
     private DataEntity[] fillTuple(Map<String, DataEntity> map) {
         Schema schema = getSchema();
         int len = schema.columnCount();
         DataEntity[] ret = new DataEntity[len];
 
         int count = 0;
         for (int i = 0; i < len; ++i) {
             DataEntity de = map.get(schema.name(i));
             if (de == null) {
                 if (schema.type(i) == TIMESTAMP)
                     de = new TimeStamp();
                 else if (schema.autoIncrement(i)) {
                     DataEntity m = max(schema.name(i));
                     int v = 1;
                     if (m != null)
                         v = ((Integer) m.toJavaType()).intValue() + 1;
                     de = new Int(v);
                 } else
                     de = schema.defaultValue(i);
             }
             ++count;
             ret[i] = de;
         }
         if (count != schema.columnCount())
             return null;
 
         return ret;
     }
 
     private DataEntity[] fillTuple(Map<String, DataEntity> map, DataEntity[] base) {
         Schema schema = getSchema();
         int len = schema.columnCount();
         DataEntity[] ret = new DataEntity[len];
 
         int count = 0;
         for (int i = 0; i < len; ++i) {
             DataEntity de = map.get(schema.name(i));
             if (de == null)
                 de = base[i];
             ++count;
             ret[i] = de;
         }
         if (count != schema.columnCount())
             return null;
 
         return ret;
     }
 
  
     public boolean insert(Map<String, DataEntity> map) {
         if (schema == null)
             return false;
         try {
             Tuple tuple = Tuple.create(fillTuple(map));
             if (tuple == null)
                 return false;
             insert(tuple);
             return true;
         } catch (java.io.IOException e) {
             return false;
         }
     }
 
     public boolean insert(DataEntity[] tuple) {
         try {
             Tuple t = Tuple.create(tuple);
             if (t == null)
                 return false;
             else {
                 insert(t);
                 return true;
             }
         } catch (java.io.IOException e) {
             return false;
         }
     }
 
     private void insert(Tuple tuple) throws java.io.IOException {
         Cell cell = loadCell(rear);
         cell.insert(tuple);
         int cBlock = cell.getBlock();
         insertIndexValues(tuple.tuple(), cBlock);
 
         if (cell.tupleCount() >= capacity) {
             rearCell = Cell.create(io);
             rear = rearCell.save();
             cell.setNext(rear);
             cell.save();
         }
 
         dirty = true;
     }
 
     public boolean update(Map<String, DataEntity> map) {
         return scanIter.update(map);
     }
 
     public Schema getSchema() {
         if (schema != null)
             return schema.schema();
         else
             return null;
     }
 
     private void insertIndexValues(DataEntity[] tuple, int block) throws java.io.IOException {
         if (schema == null)
             return;
 
         for (int i = 0; i < getSchema().columnCount(); ++i) {
             if (tuple[i].isNull())
                 continue;
 
             String colname = getSchema().name(i);
             BPlusTree tree = schema.getBPlusTree(colname);
             if (tree == null)
                 continue;
 
             DataAdapter da = schema.adapter(colname);
             tree.insert(da.putData(tuple[i]), block);
         }
     }
 
     private void removeIndexValues(DataEntity[] tuple, int block) throws java.io.IOException {
         if (schema == null)
             return;
 
         for (int i = 0; i < getSchema().columnCount(); ++i) {
             if (tuple[i].isNull())
                 continue;
 
             String colname = getSchema().name(i);
             BPlusTree tree = schema.getBPlusTree(colname);
             if (tree == null)
                 continue;
 
             DataAdapter da = schema.adapter(colname);
             tree.remove(da.putData(tuple[i]), block);
         }
     }
 
     private void updateIndexValues(DataEntity[] oldTuple, DataEntity[] newTuple, int oldBlock, int newBlock) throws java.io.IOException {
         if (schema == null)
             return;
 
         for (int i = 0; i < getSchema().columnCount(); ++i) {
             if (!oldTuple[i].isNull() && !newTuple[i].isNull() && oldTuple[i].compareTo(newTuple[i]) == 0)
                 continue;
 
             String colname = getSchema().name(i);
             BPlusTree tree = schema.getBPlusTree(colname);
             if (tree == null)
                 continue;
 
             DataAdapter da = schema.adapter(colname);
             if (!oldTuple[i].isNull())
                 tree.remove(da.putData(oldTuple[i]), oldBlock);
             if (!newTuple[i].isNull())
                 tree.insert(da.putData(newTuple[i]), newBlock);
         }
     }
 
     public void beforeFirst() {
         scanIter.beforeFirst();
     }
 
     public boolean next() {
         return scanIter.next();
     }
 
     public void delete() {
         scanIter.remove();
     }
 
     public boolean hasField(String name) {
         return getSchema().hasField(name);
     }
 
     public DataEntity getFieldByIndex(int index) {
         return scanIter.getField(index);
     }
 
     public DataEntity getField(String name) {
         return scanIter.getField(name);
     }
 
     public DataEntity[] tuple() {
         return scanIter.getTuple();
     }
 
     Cell loadCell(int block) {
         if (block == rear)
             return rearCell;
         else
             return Cell.load(io, block);
     }
 
     private class ScanIterator implements RecordIterator {
         private Cell currentCell = null;
         private int currentIndex = 0;
         private boolean removed = false;
 
         public void beforeFirst() {
             if (currentCell == null || currentCell.getBlock() != front)
                 currentCell = loadCell(front);
             currentIndex = -1;
             removed = false;
         }
         
         public boolean next() {
             int t = currentIndex + 1;
             if (t >= currentCell.tupleCount()) {
                 do {
                     int nextCell = currentCell.getNext();
                     if (nextCell == 0)
                         return false;
                     currentCell = loadCell(nextCell);
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
 
         public void remove() {
             try {
                 if (!removed && currentCell != null && currentIndex >= 0 && currentIndex < currentCell.tupleCount()) {
                     removeIndexValues(currentCell.get(currentIndex).tuple(), currentCell.getBlock());
                     currentCell.remove(currentIndex);
                     currentCell.save();
                     removed = true;
                     --currentIndex;
                 }
             } catch (java.io.IOException e) {
             }
         }
 
         public boolean update(Map<String, DataEntity> map) {
             try {
                 if (!removed && currentCell != null && currentIndex >= 0 && currentIndex < currentCell.tupleCount()) {
                     DataEntity[] oldTuple = getTuple();
                     Tuple tuple = Tuple.create(fillTuple(map, oldTuple));
                     if (tuple == null)
                         return false;
                     int oldBlock = currentCell.getBlock();
                     currentCell.set(currentIndex, tuple);
                     int newBlock = currentCell.save();
                     updateIndexValues(oldTuple, tuple.tuple(), oldBlock, newBlock);
                     return true;
                 } else
                     return false;
             } catch (java.io.IOException e) {
                 return false;
             }
         }
 
         int getBlock() {
             if (!removed && currentCell != null && currentIndex >= 0 && currentIndex < currentCell.tupleCount())
                 return currentCell.getBlock();
             else
                 return 0;
         }
 
         public DataEntity[] getTuple() {
             if (!removed && currentCell != null && currentIndex >= 0 && currentIndex < currentCell.tupleCount())
                 return currentCell.get(currentIndex).tuple();
             else
                 return null;
         }
 
         public DataEntity getField(int index) {
             DataEntity[] tuple = getTuple();
             if (tuple == null)
                 return null;
             else
                 return tuple[index];
         }
 
         public DataEntity getField(String fldname) {
             Schema schema = getSchema();
             if (schema == null)
                 return null;
             else {
                 int i = schema.index(fldname);
                 if (i == -1)
                     return null;
                 else
                     return getField(i);
             }
         }
     }
 
     private class DummyIndexIterator extends ScanIterator {
         String fldname;
         DataEntity value;
         DataComparator compare;
 
         public DummyIndexIterator(String fldname, DataEntity value, DataComparator compare) {
             super();
             this.fldname = fldname;
             this.value = value;
             this.compare = compare;
         }
 
         public boolean next() {
             boolean ret = super.next();
             while (ret) {
                 if (compare.compare(getField(fldname), value))
                     break;
                 else
                     ret = super.next();
             }
             return ret;
         }
     }
 
     public RecordIterator scan() {
         return new ScanIterator();
     }
 
     public void createIndex(String col) {
         if (schema == null)
             return;
 
         try {
             BPlusTree tree = schema.getBPlusTree(col);
             if (tree == null) {
                 DataAdapter da = schema.adapter(col);
                 BPlusTree bptree = BPlusTree.create(io, da.comparator(), da.averageKeySize(), da.isVariant());
 
                 ScanIterator iter = new ScanIterator();
                 iter.beforeFirst();
                 int colindex = getSchema().index(col);
                 while (iter.next()) {
                     DataEntity de = iter.getTuple()[colindex];
                     if (!de.isNull())
                         bptree.insert(da.putData(de), iter.getBlock());
                 }
 
                 int block = bptree.save();
 
                 schema.putBPlusTree(col, bptree);
                 schema.save();
             }
         } catch (java.io.IOException e) {
         }
     }
 
     public void dropIndex(String col) {
         if (schema == null)
             return;
 
         try {
             schema.removeBPlusTree(col);
             schema.save();
         } catch (java.io.IOException e) {
         }
     }
 
     public boolean hasIndex(String col) {
         return schema.getBPlusTree(col) != null;
     }
 
     private BPlusTree tree(String col) {
         if (schema == null)
             return null;
         else
             return schema.getBPlusTree(col);
     }
 
     public RecordIterator indexEqual(String col, DataEntity value) {
         BPlusTree tree = tree(col);
         if (tree == null)
             return new DummyIndexIterator(col, value, new EqualToComparator());
         else {
             try {
                 DataAdapter da = schema.adapter(col);
                 List<Integer> list = tree.find(da.putData(value));
                 Schema schema = getSchema();
                 int colindex = schema.index(col);
                 return new CellListIterator(schema, this, colindex, value, list);
             } catch (java.io.IOException e) {
                 return null;
             }
         }
     }
 
     public RecordIterator indexLessThan(String col, final DataEntity value) {
         BPlusTree tree = tree(col);
         if (tree == null)
             return new DummyIndexIterator(col, value, new LessThanComparator());
         else {
             try {
                 DataAdapter da = schema.adapter(col);
                 Schema schema = getSchema();
                 int colindex = schema.index(col);
                 return new IndexIterator(schema, this, colindex, tree.min(), new Predicate<DataEntity>() {
                         public boolean apply(DataEntity x) {
                             if (x.compareTo(value) < 0)
                                 return true;
                             else
                                 return false;
                         }
                     }, da);
             } catch (java.io.IOException e) {
                 return null;
             }
         }
     }
 
     public RecordIterator indexLessThanEqual(String col, final DataEntity value) {
         BPlusTree tree = tree(col);
         if (tree == null)
             return new DummyIndexIterator(col, value, new LessThanEqualToComparator());
         else {
             try {
                 DataAdapter da = schema.adapter(col);
                 Schema schema = getSchema();
                 int colindex = schema.index(col);
                 return new IndexIterator(schema, this, colindex, tree.min(), new Predicate<DataEntity>() {
                         public boolean apply(DataEntity x) {
                             if (x.compareTo(value) <= 0)
                                 return true;
                             else
                                 return false;
                         }
                     }, da);
             } catch (java.io.IOException e) {
                 return null;
             }
         }
     }
 
     public RecordIterator indexGreaterThan(String col, DataEntity value) {
         BPlusTree tree = tree(col);
         if (tree == null)
             return new DummyIndexIterator(col, value, new GreaterThanComparator());
         else {
             try {
                 DataAdapter da = schema.adapter(col);
                 Schema schema = getSchema();
                 int colindex = schema.index(col);
                 NodeIterator nodeIter = tree.findGreaterThanEqual(da.putData(value));
                 nodeIter.beforeFirst();
                 if (nodeIter.hasNext()) {
                     DataEntity de = da.getData(nodeIter.next().key());
                     if (de.compareTo(value) == 0)
                         nodeIter.mark();
                     nodeIter.beforeFirst();
                 }
                 return new IndexIterator(schema, this, colindex, nodeIter, new Predicate<DataEntity>() {
                         public boolean apply(DataEntity x) {
                             return true;
                         }
                     }, da);
             } catch (java.io.IOException e) {
                 return null;
             }
         }
     }
 
     public RecordIterator indexGreaterThanEqual(String col, DataEntity value) {
         BPlusTree tree = tree(col);
         if (tree == null)
             return new DummyIndexIterator(col, value, new GreaterThanEqualToComparator());
         else {
             try {
                 DataAdapter da = schema.adapter(col);
                 Schema schema = getSchema();
                 int colindex = schema.index(col);
                 NodeIterator nodeIter = tree.findGreaterThanEqual(da.putData(value));
                 return new IndexIterator(schema, this, colindex, nodeIter, new Predicate<DataEntity>() {
                         public boolean apply(DataEntity x) {
                             return true;
                         }
                     }, da);
             } catch (java.io.IOException e) {
                 return null;
             }
         }
     }
 
     public DataEntity max(String col) {
         BPlusTree tree = tree(col);
         if (tree == null) {
             RecordIterator iter = scan();
             iter.beforeFirst();
             DataComparator compare = new GreaterThanComparator();
             DataEntity ret = null;
             while (iter.next()) {
                 DataEntity value = iter.getField(col);
                 if (ret == null && !value.isNull())
                     ret = value;
                 else if (compare.compare(value, ret))
                     ret = value;
             }
             return ret;
         } else {
             try {
                 DataAdapter da = schema.adapter(col);
                 NodeIterator nodeIter = tree.max();
                 nodeIter.beforeFirst();
                 if (nodeIter.hasNext())
                     return da.getData(nodeIter.next().key());
                 else
                     return null;
             } catch (java.io.IOException e) {
                 return null;
             }
         }
     }
 
     public DataEntity min(String col) {
         BPlusTree tree = tree(col);
         if (tree == null) {
             RecordIterator iter = scan();
             iter.beforeFirst();
             DataComparator compare = new LessThanComparator();
             DataEntity ret = null;
             while (iter.next()) {
                 DataEntity value = iter.getField(col);
                 if (ret == null && !value.isNull())
                     ret = value;
                 else if (compare.compare(value, ret))
                     ret = value;
             }
             return ret;
         } else {
             try {
                 DataAdapter da = schema.adapter(col);
                 NodeIterator nodeIter = tree.min();
                 nodeIter.beforeFirst();
                 if (nodeIter.hasNext())
                     return da.getData(nodeIter.next().key());
                 else
                     return null;
             } catch (java.io.IOException e) {
                 return null;
             }
         }
     }
 }
