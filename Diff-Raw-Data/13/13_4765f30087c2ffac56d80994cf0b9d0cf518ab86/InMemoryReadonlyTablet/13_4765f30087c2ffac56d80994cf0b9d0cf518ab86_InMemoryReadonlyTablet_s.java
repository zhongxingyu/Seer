 package de.tuberlin.dima.presslufthammer.data.columnar.inmemory;
 
 import java.io.BufferedInputStream;
 import java.io.ByteArrayInputStream;
 import java.io.DataInputStream;
 import java.io.IOException;
 import java.util.Map;
 
 import com.google.common.collect.Maps;
 
 import de.tuberlin.dima.presslufthammer.data.ProtobufSchemaHelper;
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
  * in-memory. As the name implies this tablet can only be used to read column
  * data, writing is not supported. This class provides constructors for
  * constructing the tablet from a given {@link InMemoryWriteonlyTablet}, for
  * constructing the tablet from a map of {@link SchemaNode} to byte arrays (the
  * byte arrays contain the column data) and for constructing the tablet from a
  * byte array. This last method is used while sending tablets over network since
  * {@link InMemoryWriteonlyTablet} provides a method to serialize the tablet to
  * a byte array.
  * 
  * @author Aljoscha Krettek
  * 
  */
 public class InMemoryReadonlyTablet implements Tablet {
     private SchemaNode schema;
     private Map<SchemaNode, byte[]> columns;
     private Map<SchemaNode, ColumnReader> columnReaders;
 
     /**
      * Constructs the tablet from the given column data.
      */
     public InMemoryReadonlyTablet(SchemaNode schema,
             Map<SchemaNode, byte[]> columns) {
         this.schema = schema;
         this.columns = columns;
         columnReaders = Maps.newHashMap();
         createColumnReaders(schema);
     }
 
     /**
      * Constructs the tablet from the column data of the given
      * {@link InMemoryWriteonlyTablet}.
      */
     public InMemoryReadonlyTablet(InMemoryWriteonlyTablet sourceTablet) {
         this(sourceTablet.getSchema(), sourceTablet.getColumnData());
     }
 
     /**
      * Deserializes the tablet from the given byte array. The byte array must be
      * in a compatible format, {@link InMemoryWriteonlyTablet} provides the
      * {@code serialize} method for writing such data.
      */
     public InMemoryReadonlyTablet(byte[] data) {
         ByteArrayInputStream inArray = new ByteArrayInputStream(data);
         DataInputStream in = new DataInputStream(inArray);
         Map<String, byte[]> rawColumns = Maps.newHashMap();
 
         try {
             int magicNumber = in.readInt();
             if (magicNumber != InMemoryWriteonlyTablet.TABLET_MAGIC_NUMBER) {
                 // this should not happen, programming bug
                 throw new RuntimeException(
                         "Pressluft handed to InMemoryReadonlyTablet with wrong magic number.");
             }
 
             String schemaString = in.readUTF();
             this.schema = ProtobufSchemaHelper
                     .readSchemaFromString(schemaString);
 
             int numColumns = in.readInt();
 
             for (int i = 0; i < numColumns; ++i) {
                 String qualifiedColumnName = in.readUTF();
                 int numBytes = in.readInt();
                 byte[] buffer = new byte[numBytes];
                 if (numBytes > 0) {
                     int bytesRead = in.read(buffer, 0, numBytes);
                     if (bytesRead != numBytes) {
                         throw new RuntimeException(
                                 "Mismatch in number of bytes expected and actually read.\nField: "
                                         + qualifiedColumnName + " " + bytesRead
                                         + "/" + numBytes);
                     }
                 }
                 rawColumns.put(qualifiedColumnName, buffer);
             }
 
         } catch (IOException e) {
             // cannot happen
             e.printStackTrace();
         }
         columns = Maps.newHashMap();
         columnReaders = Maps.newHashMap();
         fillColumns(schema, rawColumns);
         createColumnReaders(schema);
     }
 
     /**
      * Fills the colum data from the provided {@link Map} by recursing on the
      * given schema.
      */
     private void fillColumns(SchemaNode schema, Map<String, byte[]> rawColumns) {
         columns.put(schema, rawColumns.get(schema.getQualifiedName()));
 
         if (schema.isRecord()) {
             for (SchemaNode childSchema : schema.getFieldList()) {
                 fillColumns(childSchema, rawColumns);
             }
         }
     }
 
     /**
      * Creates column readers that read from the byte arrays that contain the
      * columnar data by creating {@link ColumnReaderImpl} instances that read
      * from a {@link ByteArrayInputStream}.
      */
     private void createColumnReaders(SchemaNode schema) {
         ByteArrayInputStream arrayStream = new ByteArrayInputStream(
                 columns.get(schema));
         DataInputStream in = new DataInputStream(new BufferedInputStream(
                 arrayStream));
         try {
             ColumnReader reader = null;
             if (schema.isPrimitive()) {
                 switch (schema.getPrimitiveType()) {
                 case INT32:
                     reader = new ColumnReaderInt32(schema, in);
                     break;
                 case INT64:
                     reader = new ColumnReaderInt64(schema, in);
                     break;
                 case BOOLEAN:
                     reader = new ColumnReaderBool(schema, in);
                     break;
                 case FLOAT:
                     reader = new ColumnReaderFloat(schema, in);
                     break;
                 case DOUBLE:
                     reader = new ColumnReaderDouble(schema, in);
                     break;
                 case STRING:
                     reader = new ColumnReaderString(schema, in);
                     break;
                 default:
                     throw new RuntimeException("Unknown primitive type: "
                             + schema.getPrimitiveType());
                 }
             }
             columnReaders.put(schema, reader);
         } catch (IOException e) {
             // This cannot happen, we do nothing with I/O here, comes from the
             // output stream shenanigans inside InMemoryColumnReader
             e.printStackTrace();
         }
 
         if (schema.isRecord()) {
             for (SchemaNode childSchema : schema.getFieldList()) {
                 createColumnReaders(childSchema);
             }
         }
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
         throw new RuntimeException(
                 "This is a read-only tablet, call to this should not happen. (getColumnWriter)");
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public ColumnReader getColumnReader(SchemaNode schema) {
         if (!columns.containsKey(schema)) {
             throw new RuntimeException(
                     "This should not happen, bug in program.");
         }
         return columnReaders.get(schema);
     }
 
     /**
      * Prints the data contained in the columns to stdout.
      */
     public void printColumns() {
         for (SchemaNode schema : columns.keySet()) {
             System.out.println("COLUMN: " + schema.getQualifiedName());
             System.out.println("SIZE: " + columns.get(schema).length);
             ByteArrayInputStream arrayStream = new ByteArrayInputStream(
                     columns.get(schema));
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
                     break;
                 case FLOAT:
                     reader = new ColumnReaderFloat(schema, in);
                     break;
                 case DOUBLE:
                     reader = new ColumnReaderDouble(schema, in);
                     break;
                 case STRING:
                     reader = new ColumnReaderString(schema, in);
                     break;
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
