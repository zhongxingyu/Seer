 package dbmigrate.parser;
 
 import java.io.File;
 
 import junit.framework.TestCase;
 import dbmigrate.executor.CreateTableExecutor;
 import dbmigrate.model.operation.CreateTableOperationDescriptor;
 import dbmigrate.model.operation.MigrationConfiguration;
 import dbmigrate.parser.model.Migration;
 import dbmigrate.parser.model.RemoveColumn;
 
 public class LoaderTest  extends TestCase{
 	
 	public void testMapping() throws Exception {
 		Migration m=new Migration();
 		RemoveColumn rm=new RemoveColumn();
 		rm.setTable("TEST");
 		rm.setName("TEST2|");
 		
 		m.getDoList().add(rm);
 		Loader.map(m);
 	}
 	
 	public void testCreateMigrationConfiguration(){
 		try {
			MigrationConfiguration mc = Loader.load(new File("migrations/2011111001_first_migration.xml", false));
 			CreateTableOperationDescriptor desc = (CreateTableOperationDescriptor) mc.getOperations().get(0);
 			CreateTableExecutor executor = new CreateTableExecutor(null);
 			assertEquals("CREATE TABLE \"users\" ( id INT NOT NULL,username TEXT (40) NOT NULL,password TEXT (40) NOT NULL);", executor.createSql(desc).trim());
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 	}
 	
 
 }
