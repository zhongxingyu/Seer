 package us.yuxin.hump;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Types;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Properties;
 
 import com.google.common.collect.Iterables;
 import org.apache.avro.Schema;
 import org.apache.avro.Schema.Type;
 import org.apache.avro.Schema.Field;
 import org.apache.avro.file.CodecFactory;
 import org.apache.avro.file.DataFileWriter;
 import org.apache.avro.generic.GenericData;
 import org.apache.avro.generic.GenericDatumWriter;
 import org.apache.avro.generic.GenericRecord;
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.fs.FileSystem;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.compress.CompressionCodec;
 
 import static org.apache.avro.Schema.Type.*;
 
 public class AvroStore extends StoreBase {
   public AvroStore(FileSystem fs, Configuration conf, CompressionCodec codec) {
     super(fs, conf, codec);
   }
 
   @Override
   public void store(Path file, JdbcSource source, Properties prop, StoreCounter counter) throws IOException {
     JdbcSourceMetadata jdbcMetadata = prepareMetadata(source);
     int columns = jdbcMetadata.getColumnCount();
     ResultSet rs = source.getResultSet();
 
     int[] sqlTypes = jdbcMetadata.types;
 
     Schema schema = generateSchema(source, jdbcMetadata, prop);
     if (counter == null) {
       counter = new StoreCounter();
     }
 
     setLastRealPath(file);
     if (useTemporary) {
       file = genTempPath();
     }
 
     GenericDatumWriter writer = new GenericDatumWriter(schema);
     OutputStream outs = fs.create(file);
     DataFileWriter fileWriter = new DataFileWriter(writer);
     if (codec != null) {
       if (codec.getClass().getName().endsWith("SnappyCodec")) {
         fileWriter.setCodec(CodecFactory.snappyCodec());
       }
     }
 
     fileWriter.create(schema, outs);
 
     List<VirtualColumn> virtualColumnList = source.getVirtualColumns();
     int virtualColumnCount = 0;
     VirtualColumn[] virtualColumnArray = null;
     Object[] virtualColumnValues = null;
 
     if (virtualColumnList != null) {
       virtualColumnCount = virtualColumnList.size();
       virtualColumnArray = Iterables.toArray(virtualColumnList, VirtualColumn.class);
       virtualColumnValues = prepareVirtualColumnValues(virtualColumnArray);
     }
 
     int[] columnTypes = jdbcMetadata.types;
 
     try {
       while (rs.next()) {
         GenericRecord datum = new GenericData.Record(schema);
         ++counter.rows;
         for (int c = 0; c < columns; ++c) {
           if (rs.getObject(c + 1) == null) {
             datum.put(c, null);
             continue;
           }
 
           int sqlType = columnTypes[c];
           switch(sqlType) {
             case Types.INTEGER:
               datum.put(c, rs.getInt(c + 1));
               break;
             case Types.BIGINT:
              datum.put(c, rs.getLong(c + 1));
              break;
             case Types.TIMESTAMP:
               datum.put(c, rs.getTimestamp(c + 1).getTime());
               break;
             case Types.TIME:
             case Types.DATE:
               datum.put(c, rs.getString(c + 1));
               break;
 
             default:
 //              Object o = rs.getObject(c + 1);
 //              System.out.println(o.getClass().getName() + "/" + sqlType + ":" + o.toString());
               datum.put(c, rs.getObject(c + 1));
           }
         }
 
         if (virtualColumnCount > 0) {
           for (int c = 0; c < virtualColumnCount; ++c) {
             datum.put(c + columns, virtualColumnValues[c]);
           }
         }
         fileWriter.append(datum);
       }
     } catch (SQLException e) {
       throw new IOException("Failed to fetch data from JDBC source", e);
     }
     fileWriter.close();
 
     counter.outBytes = fs.getFileStatus(file).getLen();
     if (useTemporary) {
       fs.mkdirs(getLastRealPath().getParent());
       fs.rename(getLastTempPath(), getLastRealPath());
     }
   }
 
   private Object[] prepareVirtualColumnValues(VirtualColumn[] virtualColumnArray) {
     Object[] values = new Object[virtualColumnArray.length];
 
     for (int i = 0; i < values.length; ++i) {
       String v = (String)virtualColumnArray[i].defaultValue;
       if (v == null) {
         values[i] = null;
         continue;
       }
 
       Type type = toAvroType(virtualColumnArray[i].columnType);
       switch(type) {
         case INT:
           values[i] = Integer.getInteger(v);
           break;
 
         case LONG:
           values[i] = Long.getLong(v);
           break;
 
         default:
           values[i] = v;
       }
     }
     return values;
   }
 
   private Schema toNullableAvroSchema(int sqlType) {
     List<Schema> childSchemas = new ArrayList<Schema>();
     childSchemas.add(Schema.create(toAvroType(sqlType)));
     childSchemas.add(Schema.create(NULL));
     return Schema.createUnion(childSchemas);
   }
 
 
   private Schema toNullableAvroSchema(Type type) {
     List<Schema> childSchemas = new ArrayList<Schema>();
     childSchemas.add(Schema.create(type));
     childSchemas.add(Schema.create(NULL));
     return Schema.createUnion(childSchemas);
   }
 
 
   private Schema generateSchema(JdbcSource source, JdbcSourceMetadata jdbcMetadata, Properties prop) {
     List<Field> fields= new ArrayList<Field>();
 
     int columnNumber = jdbcMetadata.getColumnCount();
     String[] columnNames = jdbcMetadata.names;
     int[] columnTypes = jdbcMetadata.types;
 
     for(int c = 0; c < columnNumber; ++c) {
       Schema fieldSchema = toNullableAvroSchema(columnTypes[c]);
       Field field = new Field(columnNames[c], fieldSchema, null, null);
       field.addProp("sqlType", Integer.toString(columnTypes[c]));
       fields.add(field);
     }
 
     List<VirtualColumn> virtualColumns = source.getVirtualColumns();
     if (virtualColumns != null && virtualColumns.size() >0 ) {
       for (VirtualColumn vc: virtualColumns) {
         Schema fieldSchema = toNullableAvroSchema(toAvroType(vc.columnType));
         Field field = new Field(vc.columnName, fieldSchema, null, null);
         field.addProp("virtualType", vc.columnType);
         fields.add(field);
       }
     }
 
     // TODO More table detail.
     String tableName = "block";
     Schema schema = Schema.createRecord(tableName, null, null, true);
     schema.setFields(fields);
     return schema;
   }
 
 
   @Override
   public String getFormatId() {
     return "avro";
   }
 
 
   private Type toAvroType(int sqlType) {
     switch (sqlType) {
       case Types.TINYINT:
       case Types.SMALLINT:
       case Types.INTEGER:
         return INT;
       case Types.BIGINT:
         return LONG;
       case Types.BIT:
       case Types.BOOLEAN:
         return BOOLEAN;
       case Types.REAL:
         return FLOAT;
       case Types.FLOAT:
       case Types.DOUBLE:
         return DOUBLE;
       case Types.NUMERIC:
       case Types.DECIMAL:
         return STRING;
       case Types.CHAR:
       case Types.VARCHAR:
       case Types.LONGVARCHAR:
       case Types.LONGNVARCHAR:
       case Types.NVARCHAR:
       case Types.NCHAR:
       case Types.DATE:
       case Types.TIME:
         return STRING;
       case Types.TIMESTAMP:
         return LONG;
       case Types.BINARY:
       case Types.VARBINARY:
         return BYTES;
       default:
         throw new IllegalArgumentException("Cannot convert SQL type "
           + sqlType);
     }
   }
 
 
   private Type toAvroType(String type) {
     if (type.equalsIgnoreCase("INT")) { return INT; }
     if (type.equalsIgnoreCase("INTEGER")) { return INT; }
     if (type.equalsIgnoreCase("LONG")) { return LONG; }
     if (type.equalsIgnoreCase("BOOLEAN")) { return BOOLEAN; }
     if (type.equalsIgnoreCase("FLOAT")) { return FLOAT; }
     if (type.equalsIgnoreCase("DOUBLE")) { return DOUBLE; }
     if (type.equalsIgnoreCase("STRING")) { return STRING; }
     if (type.equalsIgnoreCase("BYTES")) { return BYTES; }
 
     // Mapping was not found
     throw new IllegalArgumentException("Cannot convert to AVRO type " + type);
   }
 }
