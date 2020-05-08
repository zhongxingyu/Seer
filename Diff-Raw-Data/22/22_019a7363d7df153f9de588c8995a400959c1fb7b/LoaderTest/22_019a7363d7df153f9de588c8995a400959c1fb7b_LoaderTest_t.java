 package dbmigrate.parser;
 
 import junit.framework.TestCase;
 import dbmigrate.executor.CreateTableExecutor;
 import dbmigrate.model.operation.CreateTableOperationDescriptor;
 import dbmigrate.model.operation.MigrationConfiguration;
 import dbmigrate.parser.model.Migration;
 import dbmigrate.parser.model.RemoveColumn;
 
 public class LoaderTest extends TestCase {
 
     public void testMapping() throws Exception {
 	Migration m = new Migration();
 	RemoveColumn rm = new RemoveColumn();
 	rm.setTable("TEST");
 	rm.setName("TEST2|");
 
 	m.getDoList().add(rm);
 	Loader.map(m);
     }
 
     public void testCreateMigrationConfiguration() {
 	try {
	    String c = "<migration version=\"2011111001\">" + "<do>"
		    + "<create-table id=\"first1\">" + "<name>users</name>"
		    + "<columns>" + "<column name=\"id\">" + "<type>int</type>"
		    + "<length>4</length>" + "<notnull>true</notnull>"
		    + "</column>" + "<column name=\"username\">"
		    + "<type>varchar</type>" + "<notnull>true</notnull>"
		    + "<length>40</length>" + "</column>"
		    + "<column name=\"password\">" + "<type>varchar</type>"
		    + "<length>40</length>" + "<notnull>true</notnull>"
		    + "</column>" + "</columns>" + "</create-table>" + "</do>"
		    + "<undo>" + "<remove-table for=\"first1\">"
		    + "<name>users</name>" + "</remove-table>" + "</undo>"
		    + "</migration>";
	    Migration m = MigrationParser.loadMigration(c);
	    MigrationConfiguration mc = Loader.map(m);
 	    CreateTableOperationDescriptor desc = (CreateTableOperationDescriptor) mc
 		    .getOperations().get(0);
 	    CreateTableExecutor executor = new CreateTableExecutor(null);
 	    assertEquals(
 		    "CREATE TABLE \"users\" ( id INT NOT NULL,username VARCHAR (40) NOT NULL,password VARCHAR (40) NOT NULL);",
 		    executor.createSql(desc).trim());
 	} catch (Exception e) {
 	    e.printStackTrace();
 	}
 
     }
 }
