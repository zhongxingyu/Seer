 package forma.tap;
 
 import backtype.hadoop.pail.PailStructure;
 import cascading.kryo.KryoFactory;
 import cascalog.hadoop.ClojureKryoSerialization;
 import com.esotericsoftware.kryo.ObjectBuffer;
 
 import java.util.Collections;
 import java.util.List;
 
 /** User: sritchie Date: 12/29/11 Time: 7:39 AM */
 public abstract class KryoPailStructure implements PailStructure<Object> {
 
     private transient ObjectBuffer kryoBuf;
 
     private ObjectBuffer getKryoBuffer() {
         if(kryoBuf == null) {
             ClojureKryoSerialization serialization = new ClojureKryoSerialization();
            kryoBuf = KryoFactory.newBuffer(serialization.makeKryo());
         }
         return kryoBuf;
     }
 
     public byte[] serialize(Object obj) {
         return getKryoBuffer().writeClassAndObject(obj);
     }
 
     public Object deserialize(byte[] record) {
         return getKryoBuffer().readClassAndObject(record);
     }
 
     public boolean isValidTarget(String... dirs) {
         return true;
     }
 
     public List<String> getTarget(Object o) {
         return Collections.EMPTY_LIST;
     }
 }
