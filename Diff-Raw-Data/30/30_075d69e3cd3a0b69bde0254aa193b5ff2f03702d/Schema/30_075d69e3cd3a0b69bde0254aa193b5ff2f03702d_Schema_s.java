 package fatworm.record;
 
 import static java.sql.Types.*;
 import java.util.*;
 import fatworm.util.ByteLib;
 
 public class Schema {
     private ArrayList<FieldInfo> info = new ArrayList<FieldInfo>();
 
     public Schema() {}
 
     public boolean addField(String fldname, int type, int length) {
         if (indexOf(fldname) != -1)
             return false;
         info.add(new FieldInfo(fldname, type, length));
         return true;
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
 
     public int columnCount() {
         return info.size();
     }
 
     public byte[] getBytes() {
         int len = 4;
         for (int i = 0; i < info.size(); ++i) {
             len += 4;
             len += ByteLib.stringToBytes(info.get(i).name).length;
             len += 8;
         }
 
         byte[] data = new byte[len];
         int s = 0;
         ByteLib.intToBytes(info.size(), data, s);
         s += 4;
         for (int i = 0; i < info.size(); ++i) {
             FieldInfo field = info.get(i);
             byte[] tmp = ByteLib.stringToBytes(field.name);
             ByteLib.intToBytes(tmp.length, data, s);
             s += 4;
             System.arraycopy(tmp, 0, data, s, tmp.length);
             s += tmp.length;
 
             ByteLib.intToBytes(field.type, data, s);
             s += 4;
             ByteLib.intToBytes(field.length, data, s);
             s += 4;
         }
 
         return data;
     }
 
    public Schema(byte[] data, int offset, int len) {
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
 
             addField(name, type, length);
         }
     }
 
     class FieldInfo {
         String name;
         int type, length;
         public FieldInfo(String name, int type, int length) {
             this.name = name;
             this.type = type;
             this.length = length;
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
             else
                 tot += field.length;
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
