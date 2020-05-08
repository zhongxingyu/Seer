 package com.acme.menagerie.controller;
 
 import static org.springframework.web.bind.annotation.RequestMethod.GET;
 
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.Map;
 
 import javax.sql.DataSource;
 
 import org.apache.log4j.Logger;
 import org.junit.Assert;
 import org.junit.Test;
 import org.junit.runner.Description;
 import org.junit.runner.JUnitCore;
 import org.junit.runner.Result;
 import org.junit.runner.notification.Failure;
 import org.junit.runner.notification.RunListener;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.jdbc.core.JdbcTemplate;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 
 import schemacrawler.schema.ForeignKey;
 import schemacrawler.schema.PrimaryKey;
 import schemacrawler.schema.Schema;
 
 import com.acme.menagerie.util.SchemaUtil;
 
 
 @Controller
 @RequestMapping("/tests")
 public class TestController {
 
     protected static Logger logger = Logger.getLogger("api");
 
     @Autowired
     private DataSource dataSource;
     
     @Autowired
     @Qualifier("prodDataSource")
     private DataSource prodDataSource;
 
     @RequestMapping(method=GET)
     public String index(Model model) throws Exception {
         model.addAttribute("schemajson",SchemaUtil.toJson(dataSource));
         final LinkedHashMap<String,Map<String,Object>> results = new LinkedHashMap<String,Map<String,Object>>();
         
         JUnitCore junit = new JUnitCore();
         junit.addListener(new RunListener() {
             @Override
             public void testFinished(Description description) throws Exception {
                 Map<String,Object> result = new HashMap<String,Object>();
                 result.put("passed", true);
                 result.put("description", description);
                 if (!results.containsKey(description.getDisplayName())) {
                     results.put(description.getDisplayName(),result);
                 }
             }
             @Override
             public void testFailure(Failure failure) throws Exception {
                 Map<String,Object> result = new HashMap<String,Object>();
                 result.put("passed", false);
                 result.put("failure", failure);
                 result.put("description", failure.getDescription());
                 results.put(failure.getDescription().getDisplayName(),result);
             }
         });
         
         /* i'm so sorry */
         AbstractExcerciseTest.testDataSource = dataSource; 
         AbstractExcerciseTest.prodDataSource = prodDataSource;
         
         Result result = junit.run(
                 ExcerciseBaselineTest.class,
                 ExcerciseUpdateTest.class,
                 ExcerciseDataTest.class,
                 ExcerciseRenameColumnTest.class,
                 ExcerciseConstraintsTest.class,
                 ExcerciseLookupTableTest.class,
                 ExcerciseCreateTableTest.class,
                 ExcerciseViewTest.class,
                 ExcerciseSurrogateKeyTest.class,
                 ExcerciseRollbackTest.class,
                 ExcerciseUpdateProdTest.class
                 ); 
         
         
         model.addAttribute("progress", (int) (100 * (float) (result.getRunCount() - result.getFailureCount()) / result.getRunCount()));
         model.addAttribute("results", results.values());
         
         return "tests";
     }
     
     public static abstract class AbstractExcerciseTest {
         protected Schema schema;
         protected DataSource dataSource;
         protected static DataSource testDataSource;
         protected static DataSource prodDataSource;
         protected AbstractExcerciseTest() {
             setProdDataSource(false);
         }
         protected void setProdDataSource(boolean prod) {
             if (prod) {
                 this.dataSource = prodDataSource;
             }
             else {
                 this.dataSource = testDataSource;
             }
             try {
                 schema = SchemaUtil.getInstance().getSchemas(dataSource)[1]; /* information_schema=0, public=1 */
             }
             catch (Exception e) {
                 throw new RuntimeException(e);
             }
         }
         
         protected void assertTableExist(String tableName) {
             Assert.assertNotNull(String.format("%s table does not exist", tableName), schema.getTable(tableName));
         }
         protected void assertTableRowCount(String tableName, int expectedCount) {
             assertTableRowCount(tableName, expectedCount, "");
         }
         protected int getTableRows(String tableName) {
             JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
             int count = jdbcTemplate.queryForInt(String.format("SELECT COUNT(*) FROM %s", tableName));
             return count;
         }
         protected void assertTableRowCount(String tableName, int expectedCount, String where) {
             assertTableExist(tableName);
             JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
             int actualCount = jdbcTemplate.queryForInt(String.format("SELECT COUNT(*) FROM %s %s", tableName, where));
             Assert.assertEquals(
                     String.format("Unexpected number of rows in %s table", tableName), 
                     expectedCount,
                     actualCount);
         }
         protected void assertForeignKeyExists(String tableName, String name) {
             assertTableExist(tableName);
             ForeignKey[] foreignKeys = schema.getTable(tableName).getForeignKeys();
             for (ForeignKey foreignKey : foreignKeys) {
             	if (foreignKey.getName().equals(name)) {
             		return;
             	}
             }
             Assert.fail(String.format("%s foreign key does not exist", name));
         }
         protected void assertPrimaryKeyExist(String tableName, String name) {
             assertTableExist(tableName);
             PrimaryKey pk = schema.getTable(tableName).getPrimaryKey();
             Assert.assertNotNull(
                     String.format("%s does not have the primary key %s", tableName, name), pk);
             Assert.assertEquals(
                     String.format("%s does not have the primary key %s", tableName, name), name, pk.getName());
         }        
         protected void assertForeignKeyCount(String tableName, int expectedCount) {
             assertTableExist(tableName);
             Assert.assertEquals(
                     String.format("%s does not have the expected number of foreign keys", tableName),
                     expectedCount,
                     schema.getTable(tableName).getForeignKeys().length);
         }
         protected void assertColumnExist(String tableName, String columnName) {
             assertTableExist(tableName);
             Assert.assertNotNull(
                     String.format("%s table should have %s column", tableName, columnName),
                     schema.getTable(tableName).getColumn(columnName));            
         }
         protected void assertColumnNotExist(String tableName, String columnName) {
             assertTableExist(tableName);
             Assert.assertNull(
                     String.format("%s table should not have %s column", tableName, columnName),
                     schema.getTable(tableName).getColumn(columnName));            
         }
         protected void assertTagExist(String tag) {
             assertTableExist("DATABASECHANGELOG");
             JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
             int actualCount = jdbcTemplate.queryForInt("SELECT COUNT(*) FROM DATABASECHANGELOG WHERE TAG=?",tag);
             Assert.assertTrue(
                     String.format("No tag %s in database", tag), 
                     actualCount > 0);
         }        
     }
 
     public static class ExcerciseBaselineTest extends AbstractExcerciseTest {
         @Test
         public void hasProdDatabaseChangelogTable() {
             setProdDataSource(true);
             assertTableExist("DATABASECHANGELOG");
         }
         @Test
         public void hasProdDatabaseChangelogLockTable() {
             setProdDataSource(true);
             assertTableExist("DATABASECHANGELOGLOCK");
         }
         @Test
         public void hasProdChangeSets() {
             setProdDataSource(true);
            assertTableExist("DATABASECHANGELOG");
             Assert.assertTrue("DATABASECHANGELOG is empty", getTableRows("DATABASECHANGELOG") > 0);
         }
         @Test
         public void hasProdTag() {
             setProdDataSource(true);
             assertTagExist("excercise1");
         }        
     }
     
     public static class ExcerciseUpdateTest extends AbstractExcerciseTest {
         @Test
         public void hasDatabaseChangelogTable() {
             assertTableExist("DATABASECHANGELOG");
         }
         @Test
         public void hasDatabaseChangelogLockTable() {
             assertTableExist("DATABASECHANGELOGLOCK");
         }
         @Test
         public void hasPetTable() {
             assertTableExist("PET");
         }
         @Test
         public void hasEventTable() {
             assertTableExist("EVENT");
         }
         @Test
         public void hasTag() {
             assertTagExist("excercise2");
         }        
     }
     
     public static class ExcerciseDataTest extends AbstractExcerciseTest {
         @Test
         public void hasPetData() {
             assertTableRowCount("PET", 9);
         }
         @Test
         public void hasEventData() {
             assertTableRowCount("EVENT", 10);            
         }
         @Test
         public void hasChangeLogLoadData() {
             assertTableRowCount("DATABASECHANGELOG", 2, "WHERE description LIKE '%Load Data%'");
         }
         @Test
         public void hasTag() {
             assertTagExist("excercise3");
         }        
     }
     
     public static class ExcerciseRenameColumnTest extends AbstractExcerciseTest {
         @Test
         public void hasEventPetNameColumn() {
             assertColumnExist("EVENT","PETNAME");
             assertTableRowCount("EVENT", 0, "WHERE PETNAME IS NULL");
             assertColumnNotExist("EVENT","NAME");
         }
         @Test
         public void hasEventDateCreatedColumn() {
             assertColumnExist("EVENT","DATECREATED");
             assertTableRowCount("EVENT", 0, "WHERE DATECREATED IS NULL");
             assertColumnNotExist("EVENT","DATE");
         }
         @Test
         public void hasEventGenderColumn() {
             assertColumnExist("PET","GENDER");
             assertTableRowCount("PET", 1, "WHERE GENDER IS NULL");
             assertColumnNotExist("PET","SEX");
         }
         @Test
         public void hasTag() {
             assertTagExist("excercise4");
         }        
     }
     
     
     public static class ExcerciseConstraintsTest extends AbstractExcerciseTest {
         @Test
         public void hasEventForeignKeys() {
             assertForeignKeyExists("EVENT", "FK_EVENT_PET");
         }
         @Test
         public void hasPetPrimaryKeys() {
             assertPrimaryKeyExist("PET", "PK_PET");
         }
         @Test
         public void hasTag() {
             assertTagExist("excercise5");
         }        
     }
     
     public static class ExcerciseCreateTableTest extends AbstractExcerciseTest {
         @Test
         public void hasOwnerTable() {
             assertTableExist("OWNER");
         }
         @Test
         public void hasOwnerColumns() {
             assertColumnExist("OWNER", "FIRSTNAME");
             assertColumnExist("OWNER", "LASTNAME");
             assertColumnExist("OWNER", "GENDER");
             assertColumnExist("OWNER", "EMAIL");
             assertColumnExist("OWNER", "ADDRESS");
         }
         @Test
         public void hasOwnerPrimaryKey() {
             assertPrimaryKeyExist("OWNER", "PK_OWNER");
         }
         @Test
         public void hasPetForeignKey() {
         	assertForeignKeyExists("PET", "FK_PET_OWNER");
         }        
         @Test
         public void hasOwnerData() {
             assertTableRowCount("OWNER", 4);
         }
         @Test
         public void hasTag() {
             assertTagExist("excercise6");
         }        
     }
     
     public static class ExcerciseLookupTableTest extends AbstractExcerciseTest {
         @Test
         public void hasSpeciesTable() {
             assertTableExist("SPECIES");
         }
         @Test
         public void hasGenderTable() {
             assertTableExist("GENDER");
         }
         @Test
         public void hasSpeciesData() {
             assertTableRowCount("SPECIES", 5);
         }
         @Test
         public void hasGenderData() {
             assertTableRowCount("GENDER", 2);
         }
         @Test
         public void hasTag() {
             assertTagExist("excercise7");
         }        
     }
     
     public static class ExcerciseViewTest extends AbstractExcerciseTest {
         @Test
         public void hasPetAgeView() {
             assertTableExist("V_PETAGE");
         }
         @Test
         public void hasPetAgeCorrectCalc() {
             assertTableExist("V_PETAGE");
             JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
             int actualAge= jdbcTemplate.queryForInt("SELECT AGE FROM V_PETAGE WHERE NAME='Bowser'");
             Assert.assertEquals(
                     String.format("V_PETAGE calculates age incorrectly"), 
                     15,
                     actualAge);
         }
         @Test
         public void hasTag() {
             assertTagExist("excercise8");
         }        
         
     }
     
     public static class ExcerciseSurrogateKeyTest extends AbstractExcerciseTest {
         @Test
         public void hasPetIdColumn() {
             assertColumnExist("PET", "ID");
         }
         @Test
         public void hasEventPetIdColumn() {
             assertColumnExist("EVENT", "PETID");
         }
         @Test
         public void hasEventForeignKeys() {
             assertForeignKeyExists("EVENT", "FK_EVENT_PET");
         }
         @Test
         public void hasPetPrimaryKeys() {
             assertPrimaryKeyExist("PET", "PK_PET");
         }
         @Test
         public void hasTag() {
             assertTagExist("excercise9");
         }
     }
     
     public static class ExcerciseRollbackTest extends AbstractExcerciseTest {
         @Test
         public void hasTag() {
             assertTagExist("excercise10");
         }
     }    
     
     public static class ExcerciseUpdateProdTest extends AbstractExcerciseTest {
         @Test
         public void excercise2() {
             setProdDataSource(true);
             assertColumnExist("PET", "ID");
             assertTableRowCount("PET", 9);
             assertTableRowCount("EVENT", 10);
             assertTagExist("excercise2");
         }
         @Test
         public void excercise4() {
             setProdDataSource(true);
             assertColumnExist("EVENT","PETNAME");
             assertTableRowCount("EVENT", 0, "WHERE PETNAME IS NULL");
             assertColumnNotExist("EVENT","NAME");
             assertColumnExist("EVENT","DATECREATED");
             assertTableRowCount("EVENT", 0, "WHERE DATECREATED IS NULL");
             assertColumnNotExist("EVENT","DATE");
             assertColumnExist("PET","GENDER");
             assertTableRowCount("PET", 1, "WHERE GENDER IS NULL");
             assertColumnNotExist("PET","SEX");
             assertTagExist("excercise4");
         }
         @Test
         public void excercise6() {
             setProdDataSource(true);
             assertColumnExist("OWNER", "FIRSTNAME");
             assertColumnExist("OWNER", "LASTNAME");
             assertColumnExist("OWNER", "GENDER");
             assertColumnExist("OWNER", "EMAIL");
             assertColumnExist("OWNER", "ADDRESS");
             assertTagExist("excercise6");
         }
     }    
     
 }
 
 
