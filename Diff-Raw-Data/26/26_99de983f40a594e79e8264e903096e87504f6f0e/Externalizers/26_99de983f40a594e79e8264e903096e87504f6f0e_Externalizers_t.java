 package ru.spbau.recommenders.plugin.persistent;
 
 import com.intellij.util.io.DataExternalizer;
 import com.intellij.util.io.EnumeratorStringDescriptor;
 import com.intellij.util.io.KeyDescriptor;
 import org.jetbrains.annotations.NotNull;
 import ru.spbau.recommenders.plugin.data.Suggestions;
 
 import java.io.DataInput;
 import java.io.DataOutput;
 import java.io.IOException;
 import java.util.*;
 
 /**
  * @author Pavel Talanov
  */
 public final class Externalizers {
 
     public static final EnumeratorStringDescriptor STRING_EXTERNALIZER = new EnumeratorStringDescriptor();
     public static final ListExternalizer<String> STRING_LIST_EXTERNALIZER = new ListExternalizer<String>(STRING_EXTERNALIZER);
     public static final KeyDescriptor<ClassAndCallSequence> CLASS_AND_CALL_SEQUENCE_KEY_DESCRIPTOR = new KeyDescriptor<ClassAndCallSequence>() {
 
         @Override
         public void save(DataOutput out, ClassAndCallSequence value) throws IOException {
             STRING_EXTERNALIZER.save(out, value.getClassName());
             STRING_LIST_EXTERNALIZER.save(out, value.getCallSequence());
         }
 
         @Override
         public ClassAndCallSequence read(DataInput in) throws IOException {
             String className = STRING_EXTERNALIZER.read(in);
             List<String> callSequence = STRING_LIST_EXTERNALIZER.read(in);
             return new ClassAndCallSequence(className, callSequence);
         }
 
         @Override
         public int getHashCode(ClassAndCallSequence value) {
             return value.hashCode();
         }
 
         @Override
         public boolean isEqual(ClassAndCallSequence val1, ClassAndCallSequence val2) {
             return val1.equals(val2);
         }
     };
     public static final DataExternalizer<Suggestions> SUGGESTIONS_EXTERNALIZER = new DataExternalizer<Suggestions>() {
 
         @Override
         public void save(DataOutput out, Suggestions value) throws IOException {
             Collection<Map.Entry<String, Integer>> entries = value.toCollection();
            out.writeInt(entries.size());
            for (Map.Entry<String, Integer> entry : entries) {
                STRING_EXTERNALIZER.save(out, entry.getKey());
                out.writeInt(entry.getValue());
            }
         }
 
         @Override
         public Suggestions read(DataInput in) throws IOException {
             Map<String, Integer> data = new HashMap<String, Integer>();
             int size = in.readInt();
            while (--size >= 0) {
                String key = STRING_EXTERNALIZER.read(in);
                int value = in.readInt();
                data.put(key, value);
            }
             return Suggestions.fromMap(data);
         }
     };
 
     private static final class ListExternalizer<T> implements DataExternalizer<List<T>> {
 
         private final DataExternalizer<T> valueExternalizer;
 
         private ListExternalizer(@NotNull DataExternalizer<T> valueExternalizer) {
             this.valueExternalizer = valueExternalizer;
         }
 
         @Override
         public void save(DataOutput out, List<T> list) throws IOException {
            out.writeInt(list.size());
             for (T value : list) {
                 valueExternalizer.save(out, value);
             }
         }
 
         @Override
         public List<T> read(DataInput in) throws IOException {
             List<T> list = new ArrayList<T>();
             int size = in.readInt();
             while (--size >= 0) {
                 list.add(valueExternalizer.read(in));
             }
             return list;
         }
     }
 
 
     private Externalizers() {
     }
 
 
 }
