 package com.datastax.hectorjpa.meta.embed;
 
 import static com.datastax.hectorjpa.serializer.CompositeUtils.newComposite;
 
 import java.util.Collection;
 import java.util.List;
 
 import me.prettyprint.cassandra.model.HColumnImpl;
 import me.prettyprint.cassandra.serializers.DynamicCompositeSerializer;
 import me.prettyprint.cassandra.serializers.IntegerSerializer;
 import me.prettyprint.cassandra.serializers.StringSerializer;
 import me.prettyprint.hector.api.beans.ColumnSlice;
 import me.prettyprint.hector.api.beans.DynamicComposite;
 import me.prettyprint.hector.api.beans.HColumn;
 import me.prettyprint.hector.api.mutation.Mutator;
 import me.prettyprint.hector.api.query.QueryResult;
 
 import org.apache.openjpa.kernel.OpenJPAStateManager;
 import org.apache.openjpa.meta.FieldMetaData;
 
 import com.datastax.hectorjpa.meta.StringColumnField;
 import com.datastax.hectorjpa.serialize.EmbeddedSerializer;
 import com.datastax.hectorjpa.service.IndexQueue;
 
 /**
  * Class for serialising a single Embedded entity to a column value
  * 
  * @author Todd Nine
  * 
  * @param <V>
  */
 public class SerializedCollectionColumnField extends StringColumnField {
 
   protected static final DynamicCompositeSerializer serializer = new DynamicCompositeSerializer();
   
   protected static final IntegerSerializer intSerializer = IntegerSerializer.get();
 
   protected final FieldMetaData embeddedField;
 
   protected final EmbeddedSerializer embeddedSerializer;
 
   public SerializedCollectionColumnField(FieldMetaData fmd, EmbeddedSerializer serializer) {
     super(fmd.getIndex(), fmd.getName());
 
     embeddedField = fmd;
         
     embeddedSerializer = serializer;
 
   }
 
   /**
    * Adds this field to the mutation with the given clock
    * 
    * @param stateManager
    * @param mutator
    * @param clock
    * @param key
    *          The row key
    * @param cfName
    *          the column family name
    */
   public void addField(OpenJPAStateManager stateManager,
       Mutator<byte[]> mutator, long clock, byte[] key, String cfName,
       IndexQueue queue) {
 
     Object value = stateManager.fetch(fieldId);
 
     if (value == null) {
       mutator.addDeletion(key, cfName, name, StringSerializer.get(), clock);
       return;
     }
     
     DynamicComposite c = newComposite();
     
     int size = ((Collection<?>)value).size();
     
     c.addComponent(size, intSerializer);
     
     //Write all values to the composite
     for(Object element: (Collection<?>) value){
       c.add(embeddedSerializer.getBytes(element));
     }
 
     mutator.addInsertion(key, cfName, new HColumnImpl<String, DynamicComposite>(name, c, clock,
         StringSerializer.get(), serializer));
 
   }
 
   /**
    * Read the field from the query result into the opject within the state
    * manager.
    * 
    * @param stateManager
    * @param result
    * @return True if the field was loaded. False otherwise
    */
   @SuppressWarnings("unchecked")
   public boolean readField(OpenJPAStateManager stateManager,
       QueryResult<ColumnSlice<String, byte[]>> result) {
 
     HColumn<String, byte[]> column = result.get().getColumnByName(name);
 
     if (column == null) {
     	stateManager.store(fieldId, null);
       return false;
     }
 
     DynamicComposite composite = serializer.fromBytes(column.getValue());
     
     Collection<Object> collection = (Collection<Object>) stateManager.newFieldProxy(fieldId);
     
     int size = composite.get(0, intSerializer);
     
     for(int i = 1; i <= size; i ++){
       Object value = embeddedSerializer.getObject(composite.getComponent(i).getBytes());
      if (value == null) {
    	  continue;
      }
       
       collection.add(value);
       
     }
 
     stateManager.store(fieldId, collection);
 
     return true;
   }
   
   @Override
   public void addFieldNames(List<String> fields) {
    fields.add(name);    
   }
   
 
 }
