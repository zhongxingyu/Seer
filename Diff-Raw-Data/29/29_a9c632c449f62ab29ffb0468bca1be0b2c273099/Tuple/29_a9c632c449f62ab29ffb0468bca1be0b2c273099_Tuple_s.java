 package fatworm.storage;
 
 import java.util.Map;
 import static java.sql.Types.*;
 
 import fatworm.util.ByteLib;
 import fatworm.dataentity.*;
 import fatworm.record.Schema;
 
 public class Tuple {
     private DataEntity[] values;
     private Schema schema;
 
     private Tuple(Schema schema) {
         this.schema = schema;
     }
 
     public static Tuple create(Schema schema, Map<String, DataEntity> map) {
         Tuple ret = new Tuple(schema);
 
         int len = schema.columnCount();
         ret.values = new DataEntity[len];
 
         int count = 0;
         for (int i = 0; i < len; ++i) {
             DataEntity de = map.get(schema.name(i));
             if (de == null)
                de = new NullDataEntity();
             ++count;
             ret.values[i] = de;
         }
         if (count != schema.columnCount())
             return null;
 
         return ret;
     }
 
     public Tuple(Schema schema, byte[] data, int offset) {
         this(schema);
 
         int s = offset;
         int count = ByteLib.bytesToInt(data, s);
         s += 4;
 
         values = new DataEntity[count];
 
         for (int i = 0; i < count; ++i) {
             if (data[s] == 0) {
                 ++s;
 
                 int len = ByteLib.bytesToInt(data, s);
                 s += 4;
                 values[i] = DataEntity.fromBytes(schema.type(i), data, s);
                 s += len;
             } else {
                 ++s;
                 values[i] = new NullDataEntity();
             }
         }
     }
 
     public byte[] getBytes() {
         int len = 4;
         byte[][] buffer = new byte[values.length][];
         for (int i = 0; i < values.length; ++i) {
             if (values[i].isNull()) {
                 buffer[i] = null;
                 len += 1;
             } else {
                 buffer[i] = values[i].getBytes();
                 len += 5 + buffer[i].length;
             }
         }
 
         byte[] data = new byte[len];
         int s = 0;
         ByteLib.intToBytes(values.length, data, s);
         s += 4;
         for (int i = 0; i < values.length; ++i) {
             if (buffer[i] == null) {
                 data[s] = 1;
                 ++s;
             } else {
                 data[s] = 0;
                 ++s;
 
                 ByteLib.intToBytes(buffer[i].length, data, s);
                 s += 4;
 
                 System.arraycopy(buffer[i], 0, data, s, buffer[i].length);
                 s += buffer[i].length;
             }
         }
 
         return data;
     }
 
     public DataEntity get(int index) {
         return values[index];
     }
 }
