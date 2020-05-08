 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.gdms.h2;
 
 import com.vividsolutions.jts.geom.Coordinate;
 import com.vividsolutions.jts.geom.GeometryFactory;
 import java.io.File;
 import java.sql.*;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.HashMap;
 import org.gdms.DBTestSource;
 import org.gdms.TestBase;
 import org.gdms.data.DataSource;
 import org.gdms.data.DataSourceCreationException;
 import org.gdms.data.NoSuchTableException;
 import org.gdms.data.NonEditableDataSourceException;
 import org.gdms.data.db.DBSource;
 import org.gdms.data.db.DBSourceCreation;
 import org.gdms.data.schema.DefaultMetadata;
 import org.gdms.data.schema.Metadata;
 import org.gdms.data.schema.MetadataUtilities;
 import org.gdms.data.types.*;
 import org.gdms.data.values.Value;
 import org.gdms.data.values.ValueFactory;
 import org.gdms.driver.DriverException;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assume.assumeTrue;
 import org.junit.Test;
 
 /**
  *
  * @author alexis
  */
 public class H2DriverTest extends TestBase {
 
         private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
         private static final SimpleDateFormat stf = new SimpleDateFormat("HH:mm:ss");
         private static final HashMap<Integer, Value> sampleValues = new HashMap<Integer, Value>();
 
         static {
                 try {
                         sampleValues.put(Type.BINARY, ValueFactory.createValue(new byte[]{
                                         (byte) 4, (byte) 5, (byte) 6}));
                         sampleValues.put(Type.BOOLEAN, ValueFactory.createValue(true));
                         sampleValues.put(Type.BYTE, ValueFactory.createValue((byte) 200));
                         sampleValues.put(Type.DATE, ValueFactory.createValue(sdf.parse("1980-09-05")));
                         sampleValues.put(Type.DOUBLE, ValueFactory.createValue(4.5d));
                         sampleValues.put(Type.FLOAT, ValueFactory.createValue(4.5f));
                         sampleValues.put(Type.GEOMETRY, ValueFactory.createValue(new GeometryFactory().createPoint(new Coordinate(193, 9285))));
                         sampleValues.put(Type.INT, ValueFactory.createValue(324));
                         sampleValues.put(Type.LONG, ValueFactory.createValue(1290833232L));
                         sampleValues.put(Type.SHORT, ValueFactory.createValue((short) 64000));
                         sampleValues.put(Type.STRING, ValueFactory.createValue("kasdjusk"));
                         sampleValues.put(Type.TIME, ValueFactory.createValue(new Time(stf.parse("15:34:40").getTime())));
                         sampleValues.put(Type.TIMESTAMP, ValueFactory.createValue(Timestamp.valueOf("1980-07-23 15:34:40.2345")));
                 } catch (ParseException e) {
                         e.printStackTrace();
                 }
         }
 
         private DBSource h2DBSource = new DBSource(null, 0, TestBase.backupDir
                 + File.separator + "h2AllTypes", "sa", null, "alltypes", "jdbc:h2");
         private DBTestSource h2Src = new DBTestSource("source", "org.h2.Driver",
                 pluginData+ File.separator + "h2AllTypes.sql", h2DBSource);
         private DBSource schemaH2DBSource = new DBSource(null, 0, TestBase.backupDir
                 + File.separator + "h2SchemaTest", "sa", null, "gis_schema", "schema_test", "jdbc:h2");
         private DBTestSource schemaH2Src = new DBTestSource("source", "org.h2.Driver",
                TestBase.internalData + "h2SchemaTest.sql", schemaH2DBSource);
 
 
         @Test
         public void testReadAllTypesH2() throws Exception {
                 testReadAllTypes(h2DBSource, h2Src);
         }
 
 
         @Test
         public void testReadSchemaH2() throws Exception {
                 testReadAllTypes(schemaH2DBSource, schemaH2Src);
         }
         
         @Test
         public void testCreateAllTypesH2() throws Exception {
                 assumeTrue(TestBase.h2Available);
                 DBTestSource src = new DBTestSource("source", "org.h2.Driver",
                         TestBase.internalData + "removeAllTypes.sql", h2DBSource);
                 TestBase.dsf.getSourceManager().removeAll();
                 src.backup();
                 testCreateAllTypes(h2DBSource, true, false);
         }
 
         @Test
         public void testH2CommitTwice() throws Exception {
                 assumeTrue(TestBase.h2Available);
                 DBSource dbSource = new DBSource(null, -1,
                         "src/test/resources/backup/testH2Commit", "sa", "", "mytable",
                         "jdbc:h2");
                 DefaultMetadata metadata = new DefaultMetadata(new Type[]{
                                 TypeFactory.createType(Type.STRING, new PrimaryKeyConstraint())},
                         new String[]{"field1"});
                 testCommitTwice(dbSource, metadata);
         }
 
         @Test
         public void testDoublePrimaryKey() throws Exception {
                 assumeTrue(TestBase.h2Available);
                 DefaultMetadata metadata = new DefaultMetadata(
                         new Type[]{
                                 TypeFactory.createType(Type.STRING,
                                 new PrimaryKeyConstraint()),
                                 TypeFactory.createType(Type.STRING,
                                 new PrimaryKeyConstraint())}, new String[]{
                                 "field1", "field2"});
                 DBSource dbSource = new DBSource(null, -1,
                         "src/test/resources/backup/testH2Commit", "sa", "", "mytable",
                         "jdbc:h2");
                 testCommitTwice(dbSource, metadata);
         }
 
         private void testCreateAllTypes(DBSource dbSource, boolean byte_,
                 boolean stringLength) throws Exception {
                 DefaultMetadata metadata = new DefaultMetadata();
                 metadata.addField("f1", Type.BINARY);
                 metadata.addField("f2", Type.BOOLEAN);
                 if (byte_) {
                         metadata.addField("f3", Type.BYTE);
                 }
                 metadata.addField("f4", Type.DATE);
                 metadata.addField("f5", Type.DOUBLE);
                 metadata.addField("f6", Type.FLOAT);
                 metadata.addField("f7", Type.INT, new NotNullConstraint(),
                         new AutoIncrementConstraint());
                 metadata.addField("f8", Type.LONG);
                 metadata.addField("f9", Type.SHORT, new NotNullConstraint());
                 metadata.addField("f10", Type.STRING, new LengthConstraint(50));
                 metadata.addField("f11", Type.TIME);
                 metadata.addField("f12", Type.TIMESTAMP);
 
                 DBSourceCreation dsc = new DBSourceCreation(dbSource, metadata);
                 dsf.createDataSource(dsc);
                 readAllTypes();
 
                 DataSource ds = dsf.getDataSource("source");
                 ds.open();
                 if (stringLength) {
                         assertTrue(check("f10", Constraint.LENGTH, "50", ds));
                 }
                 assertTrue(check("f9", Constraint.NOT_NULL, ds));
                 assertTrue(check("f7", Constraint.NOT_NULL, ds));
                 assertTrue(check("f7", Constraint.AUTO_INCREMENT, ds));
                 assertTrue(check("f7", Constraint.PK, ds));
                 assertTrue(check("f7", Constraint.READONLY, ds));
         }
 
         private void readAllTypes() throws NoSuchTableException,
                 DataSourceCreationException, DriverException,
                 NonEditableDataSourceException {
                 DataSource ds = TestBase.dsf.getDataSource("source");
 
                 ds.open();
                 Metadata m = ds.getMetadata();
                 Value[] newRow = new Value[m.getFieldCount()];
                 for (int i = 0; i < m.getFieldCount(); i++) {
                         Type fieldType = m.getFieldType(i);
                         if (MetadataUtilities.isWritable(fieldType)) {
                                 newRow[i] = sampleValues.get(fieldType.getTypeCode());
                         }
                 }
                 ds.insertFilledRow(newRow);
                 Value[] firstRow = ds.getRow(0);
                 ds.commit();
                 ds.close();
                 ds.open();
                 Value[] commitedRow = ds.getRow(0);
                 ds.commit();
                 ds.close();
                 ds.open();
                 Value[] reCommitedRow = ds.getRow(0);
                 assertTrue(equals(reCommitedRow, commitedRow, ds.getMetadata()));
                 assertTrue(equals(firstRow, commitedRow, ds.getMetadata()));
                 ds.commit();
                 ds.close();
         }
 
         private void testReadAllTypes(DBSource dbSource, DBTestSource src)
                 throws Exception {
                 src.backup();
                 readAllTypes();
         }
 
         private boolean check(String fieldName, int constraint, String value,
                 DataSource ds) throws DriverException {
                 int fieldId = ds.getFieldIndexByName(fieldName);
                 Type type = ds.getMetadata().getFieldType(fieldId);
                 return (type.getConstraintValue(constraint).equals(value));
         }
 
         private boolean check(String fieldName, int constraint, DataSource ds)
                 throws DriverException {
                 int fieldId = ds.getFieldIndexByName(fieldName);
                 Type type = ds.getMetadata().getFieldType(fieldId);
                 return (type.getConstraint(constraint) != null);
         }
 
         private void testCommitTwice(DBSource dbSource, Metadata metadata)
                 throws Exception, DataSourceCreationException,
                 NonEditableDataSourceException {
                 try {
                         execute(dbSource, "drop table \"mytable\";");
                 } catch (SQLException e) {
                         // ignore, something else will fail
                 }
                 dsf.createDataSource(new DBSourceCreation(dbSource, metadata));
                 dsf.getSourceManager().register("tototable", dbSource);
                 DataSource ds = dsf.getDataSource(dbSource);
                 ds.open();
                 Value[] row = new Value[metadata.getFieldCount()];
                 for (int i = 0; i < row.length; i++) {
                         row[i] = ValueFactory.createValue("value");
                 }
                 ds.insertFilledRow(row);
                 ds.commit();
                 ds.deleteRow(0);
                 ds.commit();
                 ds.close();
         }
 
         private void execute(DBSource dbSource, String statement) throws Exception {
                 Class.forName("org.postgresql.Driver").newInstance();
                 String connectionString = dbSource.getPrefix() + ":";
                 if (dbSource.getHost() != null) {
                         connectionString += "//" + dbSource.getHost();
 
                         if (dbSource.getPort() != -1) {
                                 connectionString += (":" + dbSource.getPort());
                         }
                         connectionString += "/";
                 }
 
                 connectionString += (dbSource.getDbName());
 
                 Connection c = DriverManager.getConnection(connectionString, dbSource.getUser(), dbSource.getPassword());
 
                 Statement st = c.createStatement();
                 st.execute(statement);
                 st.close();
                 c.close();
         }
 }
