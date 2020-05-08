 package de.tuberlin.dima.presslufthammer.data.columnar.inmemory;
 
 import java.io.BufferedInputStream;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.util.Map;
 
 import com.google.common.collect.Maps;
 
 import de.tuberlin.dima.presslufthammer.data.SchemaNode;
 import de.tuberlin.dima.presslufthammer.data.columnar.ColumnReader;
 import de.tuberlin.dima.presslufthammer.data.columnar.ColumnReaderBool;
 import de.tuberlin.dima.presslufthammer.data.columnar.ColumnReaderDouble;
 import de.tuberlin.dima.presslufthammer.data.columnar.ColumnReaderFloat;
 import de.tuberlin.dima.presslufthammer.data.columnar.ColumnReaderInt32;
 import de.tuberlin.dima.presslufthammer.data.columnar.ColumnReaderInt64;
 import de.tuberlin.dima.presslufthammer.data.columnar.ColumnReaderString;
 import de.tuberlin.dima.presslufthammer.data.columnar.ColumnWriter;
 import de.tuberlin.dima.presslufthammer.data.columnar.Tablet;
 
 /**
  * Implementation of the {@link Tablet} interface that stores the column data
  * in-memory. As the name implies this tablet can only be used to write column
  * data, reading is not supported. This class has a {@code serialize} method for
  * serializing the tablet to a byte array for sending over a network.
  * 
  * @author Aljoscha Krettek
  * 
  */
 public class InMemoryWriteonlyTablet implements Tablet {
     public static int TABLET_MAGIC_NUMBER = 0xCAFEBABE;
     private SchemaNode schema;
     private Map<SchemaNode, ByteArrayOutputStream> columns;
     private Map<SchemaNode, ColumnWriter> columnWriters;
 
     /**
      * Constructs the tablet by creating empty byte arrays for all the columns
      * (fields) in the schema.
      * 
      * @param schema
      */
     public InMemoryWriteonlyTablet(SchemaNode schema) {
         this.schema = schema;
         columns = Maps.newHashMap();
         columnWriters = Maps.newHashMap();
         createColumns(schema);
     }
 
     /**
      * Calls flush on the column writers to flush eventual buffers to the
      * underlying byte arrays.
      */
     public void flush() throws IOException {
         for (SchemaNode schema : columns.keySet()) {
             columnWriters.get(schema).flush();
         }
     }
 
     /**
      * Serializes this tablet to a byte array and returns it.
      */
     public byte[] serialize() {
         try {
             flush();
         } catch (IOException e) {
             // Should not happen
             e.printStackTrace();
         }
         ByteArrayOutputStream arrayOut = new ByteArrayOutputStream();
         DataOutputStream out = new DataOutputStream(arrayOut);
 
         try {
             out.writeInt(TABLET_MAGIC_NUMBER);
             out.writeUTF(schema.toString());
             out.writeInt(columns.size());
             for (SchemaNode schema : columns.keySet()) {
                 out.writeUTF(schema.getQualifiedName());
                 byte[] columnData = columns.get(schema).toByteArray();
                 out.writeInt(columnData.length);
                 out.write(columnData, 0, columnData.length);
             }
             out.flush();
         } catch (IOException e1) {
             // cannot happen
         }
 
         return arrayOut.toByteArray();
     }
 
     /**
      * Recurses on the schema and creates column writers for all fields of the
      * schema. The column writers write to a {@link ByteArrayOutputStream}.
      */
     private void createColumns(SchemaNode schema) {
         ByteArrayOutputStream column = new ByteArrayOutputStream();
         try {
             ColumnWriter writer = new ColumnWriter(schema, column);
             columnWriters.put(schema, writer);
         } catch (IOException e) {
             // This cannot happen, we do nothing with I/O here, comes from the
             // input stream shenanigans inside InMemoryColumnWriter
             e.printStackTrace();
         }
 
         columns.put(schema, column);
         if (schema.isRecord()) {
             for (SchemaNode childSchema : schema.getFieldList()) {
                 createColumns(childSchema);
             }
         }
     }
 
     /**
      * Returns the underlying byte array for all columns.
      */
     public Map<SchemaNode, byte[]> getColumnData() {
         Map<SchemaNode, byte[]> result = Maps.newHashMap();
 
         for (SchemaNode key : columns.keySet()) {
             result.put(key, columns.get(key).toByteArray());
         }
 
         return result;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public SchemaNode getSchema() {
         return schema;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public boolean hasColumn(SchemaNode schema) {
         return columns.containsKey(schema);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public ColumnWriter getColumnWriter(SchemaNode schema) {
         if (!columns.containsKey(schema)) {
             throw new RuntimeException("Column " + schema.getQualifiedName()
                     + " not contained in tablet, available columns: "
                     + columns.keySet());
         }
         return columnWriters.get(schema);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public ColumnReader getColumnReader(SchemaNode schema) {
         throw new RuntimeException(
                 "This is a write-only tablet, call to this should not happen. (getColumnReader)");
     }
 
     /**
      * Prints the data contained in the columns to stdout.
      */
     public void printColumns() {
         try {
             flush();
         } catch (IOException e1) {
             System.out.println("Error printing the columns:");
             e1.printStackTrace();
         }
         for (SchemaNode schema : columns.keySet()) {
            if (!schema.isPrimitive()) {
                continue;
            }
             System.out.println("COLUMN: " + schema.getQualifiedName());
             System.out.println("SIZE: "
                     + columns.get(schema).toByteArray().length);
             ByteArrayInputStream arrayStream = new ByteArrayInputStream(columns
                     .get(schema).toByteArray());
             DataInputStream in = new DataInputStream(new BufferedInputStream(
                     arrayStream));
             try {
                 ColumnReader reader = null;
                 switch (schema.getPrimitiveType()) {
                 case INT32:
                     reader = new ColumnReaderInt32(schema, in);
                     break;
                 case INT64:
                     reader = new ColumnReaderInt64(schema, in);
                     break;
                 case BOOLEAN:
                     reader = new ColumnReaderBool(schema, in);
                 case FLOAT:
                     reader = new ColumnReaderFloat(schema, in);
                 case DOUBLE:
                     reader = new ColumnReaderDouble(schema, in);
                 case STRING:
                     reader = new ColumnReaderString(schema, in);
                 default:
                     throw new RuntimeException("Unknown primitive type.");
                 }
                 while (reader.hasNext()) {
                     reader.advance();
                     System.out.println(reader.getCurrentRepetition());
                     System.out.println(reader.getCurrentDefinition());
                     System.out.println(reader.getValue());
                 }
             } catch (IOException e) {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
             }
         }
     }
 }
