 package org.tmcdb.engine.schema.test;
 
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.tmcdb.engine.data.NumericType;
 import org.tmcdb.engine.schema.Column;
 import org.tmcdb.engine.schema.SchemaManager;
 import org.tmcdb.engine.schema.TableSchema;
 import org.tmcdb.utils.TestUtils;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Collections;
 
 import static junit.framework.Assert.assertEquals;
 
 /**
  * @author Pavel Talanov
  */
 @SuppressWarnings("ResultOfMethodCallIgnored")
 public final class SchemaManagerTest {
 
     private static final String TEST_DATA_DIR = "testData/engine/schemaManager";
 
     @Test
     public void initializingAndAddingSchemas() {
         File testDir = new File(TEST_DATA_DIR + "/1");
         SchemaManager schemaManager = new SchemaManager(testDir);
         schemaManager.initialize();
         assertEquals(0, schemaManager.getAllTables().size());
         schemaManager.addNewSchema(new TableSchema("empty", Collections.<Column>emptyList()));
         assertEquals(1, schemaManager.getAllTables().size());
     }
 
     @Test
     public void serializingAndReadingSchemas() throws Exception {
        File testDir = new File(TEST_DATA_DIR + "/2");
         SchemaManager schemaManager = new SchemaManager(testDir);
         schemaManager.initialize();
         assertEquals(0, schemaManager.getAllTables().size());
         schemaManager.addNewSchema(new TableSchema("test", Collections.singletonList(new Column("age", NumericType.INT))));
         assertEquals(1, schemaManager.getAllTables().size());
         schemaManager.deinitialize();
         schemaManager.initialize();
         assertEquals(1, schemaManager.getAllTables().size());
         TableSchema tableSchema = schemaManager.getAllTables().iterator().next();
         assertEquals("test", tableSchema.getTableName());
         assertEquals(1, tableSchema.getColumns().size());
         Column column = tableSchema.getColumns().iterator().next();
         assertEquals(NumericType.INT, column.getType());
         assertEquals("age", column.getName());
     }
 
     @BeforeClass
     public static void prepare() throws IOException {
         new File(TEST_DATA_DIR + "/1").mkdirs();
         new File(TEST_DATA_DIR + "/2").mkdirs();
     }
 
     @SuppressWarnings("ConstantConditions")
     @AfterClass
     public static void cleanup() throws IOException {
         TestUtils.cleanDirectory(TEST_DATA_DIR);
     }
 }
