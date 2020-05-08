 package com.nearinfinity.honeycomb.mysql;
 
 import com.nearinfinity.honeycomb.mysql.gen.RowContainer;
 import com.nearinfinity.honeycomb.mysql.gen.UUIDContainer;
 import org.apache.avro.io.DatumReader;
 import org.apache.avro.io.DatumWriter;
 import org.apache.avro.specific.SpecificDatumReader;
 import org.apache.avro.specific.SpecificDatumWriter;
 import org.apache.hadoop.hbase.util.Bytes;
 
 import java.nio.ByteBuffer;
 import java.util.Map;
 import java.util.Set;
 import java.util.UUID;
 
 import static com.google.common.base.Preconditions.checkNotNull;
 
 public class Row {
     private static final DatumWriter<RowContainer> writer =
             new SpecificDatumWriter<RowContainer>(RowContainer.class);
     private static final DatumReader<RowContainer> reader =
             new SpecificDatumReader<RowContainer>(RowContainer.class);
     private final RowContainer row;
 
     /**
      * Construct a new Row with specified records and UUID.
      *
      * @param records Map of column name to record value
      * @param uuid    UUID representing the unique position of the Row
      */
     public Row(Map<String, ByteBuffer> records, UUID uuid) {
         checkNotNull(records, "records must not be null.");
         // uuid nullity will be checked by UUIDToBytes
         row = new RowContainer(new UUIDContainer(Util.UUIDToBytes(uuid)), records);
     }
 
     /**
      * Constructor called during deserialization.
      *
      * @param row {@link RowContainer} the underlying content for this row
      */
     private Row(RowContainer row) {
         this.row = row;
     }
 
     /**
      * Deserialize the provided serialized row buffer to a new {@link Row} instance
      *
      * @param serializedRow byte buffer containing serialized Row
      * @return new Row instance from serializedRow
      */
     public static Row deserialize(byte[] serializedRow) {
         return new Row(Util.deserializeAvroObject(serializedRow, reader));
     }
 
     /**
      * Returns the {@link UUID} of this Row.
      *
      * @return UUID of this Row.
      */
     public UUID getUUID() {
         return Util.bytesToUUID(row.getUuid().bytes());
     }
 
     /**
      * Set UUID to a new random UUID
      */
     public void setRandomUUID() {
         row.setUuid(new UUIDContainer(Util.UUIDToBytes(UUID.randomUUID())));
     }
 
     /**
      * Returns the a map of column names to records of this Row.
      *
      * @return Map of column names to records
      */
     public Map<String, ByteBuffer> getRecords() {
         return row.getRecords();
     }
 
     /**
      * Serialize this {@link Row} instance to a byte array.
      *
      * @return Serialized row
      */
     public byte[] serialize() {
         return Util.serializeAvroObject(row, writer);
     }
 
     @Override
     public int hashCode() {
         return row.hashCode();
     }
 
     @Override
     public boolean equals(Object obj) {
         if (this == obj) {
             return true;
         }
 
         if (obj == null) {
             return false;
         }
 
         if (getClass() != obj.getClass()) {
             return false;
         }
 
         Row other = (Row) obj;
         if (row == null) {
             if (other.row != null) {
                 return false;
             }
         } else if (!row.equals(other.row)) {
             return false;
         }
 
         return true;
     }
 
     @Override
     public String toString() {
         StringBuilder sb = new StringBuilder();
         sb.append(String.format("UUID: %s\n", getUUID().toString()));
         for (Map.Entry<String, ByteBuffer> entry : getRecords().entrySet()) {
             sb.append(entry.getKey());
             sb.append(": ");
             sb.append(Bytes.toStringBinary(entry.getValue()));
         }
         return sb.toString();
     }
 
    public boolean isRecordsEqual(Row other, Set<String> columns) {
         Map<String, ByteBuffer> records = other.getRecords();
        for (Map.Entry<String, ByteBuffer> entry : this.getRecords().entrySet()) {
            ByteBuffer otherValue = records.get(entry.getKey());
            if (columns.contains(entry.getKey()) && !entry.getValue().equals(otherValue)) {
                 return false;
             }
         }
 
         return true;
     }
 }
