 package com.oboturov.ht;
 
 import com.fasterxml.jackson.annotation.JsonCreator;
 import com.fasterxml.jackson.annotation.JsonProperty;
 import com.fasterxml.jackson.core.JsonProcessingException;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import org.apache.hadoop.io.WritableComparable;
 
 import java.io.DataInput;
 import java.io.DataOutput;
 import java.io.IOException;
 
 /**
  * @author aoboturov
  */
 public class Item implements WritableComparable<Item> {
 
     public static final Item NULL = new Item(ItemType.NULL, "");
 
     private String type;
     private String value;
 
     @JsonCreator
     public Item(final @JsonProperty("type") String type, final @JsonProperty("value") String value) {
         this.type = type;
         this.value = value;
     }
 
     private Item() {}
 
     public String getType() {
         return type;
     }
 
     public String getValue() {
         return value;
     }
 
     @Override
     public void write(final DataOutput dataOutput) throws IOException {
         dataOutput.writeUTF(this.type);
         dataOutput.writeUTF(this.value);
     }
 
     @Override
     public void readFields(final DataInput dataInput) throws IOException {
         this.type = dataInput.readUTF();
         this.value = dataInput.readUTF();
     }
 
     public static void writeWritable(final Item anItem, final DataOutput dataOutput) throws IOException {
        anItem.write(dataOutput);
     }
 
     public static Item readWritable(final DataInput dataInput) throws IOException {
         final Item anItem = new Item();
         anItem.type = dataInput.readUTF();
         anItem.value = dataInput.readUTF();
         return anItem;
     }
 
     @Override
     public int compareTo(final Item rhs) {
         int res = this.type.compareTo(rhs.getType());
         if ( res != 0) {
             return res;
         }
         return this.value.compareTo(rhs.getValue());
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (o == null || getClass() != o.getClass()) return false;
 
         Item item = (Item) o;
 
         if (type != null ? !type.equals(item.type) : item.type != null) return false;
         if (value != null ? !value.equals(item.value) : item.value != null) return false;
 
         return true;
     }
 
     @Override
     public int hashCode() {
         int result = type != null ? type.hashCode() : 0;
         result = 31 * result + (value != null ? value.hashCode() : 0);
         return result;
     }
 
     @Override
     public String toString() {
         final ObjectMapper objectMapper = ObjectMapperInstance.get();
         try {
             return objectMapper.writeValueAsString(this);
         } catch (JsonProcessingException e) {
             return null;
         }
     }
 }
