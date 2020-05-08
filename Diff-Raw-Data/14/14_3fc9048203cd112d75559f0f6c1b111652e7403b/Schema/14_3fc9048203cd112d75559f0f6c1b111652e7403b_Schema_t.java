 package fatworm.record;
 
 import static java.sql.Types.*;
 import java.util.*;
 
 import fatworm.util.ByteLib;
 import fatworm.dataentity.*;
 
 public class Schema {
     private ArrayList<FieldInfo> info = new ArrayList<FieldInfo>();
 
     public Schema() {}
 
     public boolean addField(String fldname, int type, int length, boolean notNull, boolean autoIncrement, boolean primaryKey, DataEntity defaultValue) {
         if (indexOf(fldname) != -1)
             return false;
         if (primaryKey && hasPrimary())
             return false;
         if (autoIncrement && !primaryKey)
             return false;
         if (autoIncrement && type != INTEGER)
             return false;
         if (defaultValue == null || defaultValue.isNull())
             defaultValue = new NullDataEntity();
         else if (type != defaultValue.type())
             return false;
         info.add(new FieldInfo(fldname, type, length, notNull, autoIncrement, primaryKey, defaultValue));
         return true;
     }
 
     private boolean hasPrimary() {
         for (int i = 0; i < info.size(); ++i)
             if (info.get(i).primaryKey)
                 return true;
         return false;
     }
 
     public boolean hasField(String fldname) {
         if (indexOf(fldname) == -1)
             return false;
         else
             return true;
     }
 
     private int indexOf(String fldname) {
         for (int i = 0; i < info.size(); ++i)
             if (info.get(i).name.equals(fldname))
                 return i;
         return -1;
     }
 
     public String name(int index) {
         return info.get(index).name;
     }
 
     public int index(String name) {
         return indexOf(name);
     }
 
     public int type(int index) {
         return info.get(index).type;
     }
 
     public int type(String fldname) {
         return type(indexOf(fldname));
     }
 
     public int length(int index) {
         return info.get(index).length;
     }
 
     public int length(String fldname) {
         return length(indexOf(fldname));
     }
 
     public boolean notNull(int index) {
         return info.get(index).notNull;
     }
 
     public boolean notNull(String fldname) {
         return notNull(indexOf(fldname));
     }
 
     public boolean autoIncrement(int index) {
         return info.get(index).autoIncrement;
     }
 
     public boolean autoIncrement(String fldname) {
         return autoIncrement(indexOf(fldname));
     }
 
     public boolean primaryKey(int index) {
         return info.get(index).primaryKey;
     }
 
     public boolean primaryKey(String fldname) {
         return primaryKey(indexOf(fldname));
     }
 
     public DataEntity defaultValue(int index) {
         return info.get(index).defaultValue;
     }
 
     public DataEntity defaultValue(String fldname) {
         return defaultValue(indexOf(fldname));
     }
 
     public int columnCount() {
         return info.size();
     }
 
     public byte[] getBytes() {
         byte[][] buffer = new byte[info.size()][];
         int len = 4;
         for (int i = 0; i < info.size(); ++i) {
             FieldInfo field = info.get(i);
             byte[] stringBuffer = ByteLib.stringToBytes(field.name);
             byte[] valueBuffer = null;
             int bufferSize;
             if (field.defaultValue.isNull())
                 bufferSize = 4 + stringBuffer.length + 8 + 4;
             else {
                 valueBuffer = field.defaultValue.getBytes();
                 bufferSize = 4 + stringBuffer.length + 8 + 4 + 4 + valueBuffer.length;
             }
 
             buffer[i] = new byte[bufferSize];
             len += bufferSize;
 
             int s = 0;
             ByteLib.intToBytes(stringBuffer.length, buffer[i], s);
             s += 4;
             System.arraycopy(stringBuffer, 0, buffer[i], s, stringBuffer.length);
             s += stringBuffer.length;
             ByteLib.intToBytes(field.type, buffer[i], s);
             s += 4;
             ByteLib.intToBytes(field.length, buffer[i], s);
             s += 4;
             if (field.notNull)
                 buffer[i][s] = 1;
             else
                 buffer[i][s] = 0;
             ++s;
             if (field.autoIncrement)
                 buffer[i][s] = 1;
             else
                 buffer[i][s] = 0;
             ++s;
             if (field.primaryKey)
                 buffer[i][s] = 1;
             else
                 buffer[i][s] = 0;
             ++s;
             if (field.defaultValue.isNull()) {
                 buffer[i][s] = 1;
                 ++s;
             } else {
                 buffer[i][s] = 0;
                 ++s;
                 ByteLib.intToBytes(valueBuffer.length, buffer[i], s);
                 s += 4;
                 System.arraycopy(valueBuffer, 0, buffer[i], s, valueBuffer.length);
                 s += valueBuffer.length;
             }
         }
 
         byte[] data = new byte[len];
         int s = 0;
         ByteLib.intToBytes(info.size(), data, s);
         s += 4;
         for (int i = 0; i < info.size(); ++i) {
             System.arraycopy(buffer[i], 0, data, s, buffer[i].length);
             s += buffer[i].length;
         }
 
         return data;
     }
 
     public Schema(byte[] data, int offset) {
         int s = offset;
         int count = ByteLib.bytesToInt(data, s);
         s += 4;
         for (int i = 0; i < count; ++i) {
             int slen = ByteLib.bytesToInt(data, s);
             s += 4;
             String name = ByteLib.bytesToString(data, s, slen);
             s += slen;
             int type = ByteLib.bytesToInt(data, s);
             s += 4;
             int length = ByteLib.bytesToInt(data, s);
             s += 4;
             boolean notNull, autoIncrement, primaryKey;
             if (data[s] == 0)
                 notNull = false;
             else
                 notNull = true;
             ++s;
             if (data[s] == 0)
                 autoIncrement = false;
             else
                 autoIncrement = true;
             ++s;
             if (data[s] == 0)
                 primaryKey = false;
             else
                 primaryKey = true;
             ++s;
             DataEntity defaultValue;
             if (data[s] == 0) {
                 ++s;
                 slen = ByteLib.bytesToInt(data, s);
                 s += 4;
                 defaultValue = DataEntity.fromBytes(type, data, s);
                 s += 4;
             } else {
                 ++s;
                 defaultValue = new NullDataEntity();
             }
 
             addField(name, type, length, notNull, autoIncrement, primaryKey, defaultValue);
         }
     }
 
     class FieldInfo {
         String name;
         int type, length;
         boolean notNull, autoIncrement, primaryKey;
         DataEntity defaultValue;
         public FieldInfo(String name, int type, int length, boolean notNull, boolean autoIncrement,
                 boolean primaryKey, DataEntity defaultValue) {
             this.name = name;
             this.type = type;
             this.length = length;
             this.notNull = notNull;
             this.autoIncrement = autoIncrement;
             this.primaryKey = primaryKey;
             this.defaultValue = defaultValue;
         }
     }
 
     public int estimatedLength() {
         int tot = 0;
         for (int i = 0; i < info.size(); ++i) {
             FieldInfo field = info.get(i);
             if (field.type == VARCHAR)
                 tot += field.length / 3;
             else if (field.type == DECIMAL)
                 tot += field.length + 2;
             else if (field.type == INTEGER)
                 tot += 4;
             else if (field.type == BOOLEAN)
                 tot += 1;
             else if (field.type == DATE)
                 tot += 8;
             else if (field.type == TIMESTAMP)
                 tot += 8;
             else if (field.type == FLOAT)
                 tot += 8;
             else
                tot += field.length + 1;
         }
         return tot;
     }
 
     public Collection<String> fields() {
         Collection<String> ret = new LinkedList<String>();
         for (int i = 0; i < info.size(); ++i)
             ret.add(info.get(i).name);
         return ret;
     }
 }
