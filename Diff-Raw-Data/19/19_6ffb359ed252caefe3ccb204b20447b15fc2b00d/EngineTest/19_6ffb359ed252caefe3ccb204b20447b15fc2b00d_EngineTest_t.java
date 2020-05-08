 package com.eroi.migrate;
 
 import java.util.List;
 
 import junit.framework.TestCase;
 
 public class EngineTest extends TestCase {
  
  @Override
  protected void tearDown() throws Exception {
    // Connection nulled out so it isn't accidentally re-used in other tests
    Configure.getDefaultConfiguration().setConnection(null);
  }
 	
 	public void testGetMigrationClasses_UsingDefaults() {
 		Configure.configure("jdbc:derby:testDerby;create=true", 
 										 "org.apache.derby.jdbc.EmbeddedDriver", 
 										 "user", 
 										 "password", 
 										 null,
 										 "com.eroi.migrate.testpackage",
 										 null, 
 										 null,
 										 null,
 										 null); 
 		
 		List<Class<? extends Migration>> list = Engine.classesToMigrate();
 		assertNotNull(list);
 		assertEquals(5, list.size());
 		assertEquals("com.eroi.migrate.testpackage.Migration_1", list.get(0).getName());
 		assertEquals("com.eroi.migrate.testpackage.Migration_2", list.get(1).getName());
 		assertEquals("com.eroi.migrate.testpackage.Migration_3", list.get(2).getName());
 		assertEquals("com.eroi.migrate.testpackage.Migration_4", list.get(3).getName());
 		assertEquals("com.eroi.migrate.testpackage.Migration_5", list.get(4).getName());
 		
 	}
 	
 	public void testGetMigrations_UsingClassPrefixThatDoesNotExist() {
 		Configure.configure("jdbc:derby:testDerby;create=true", 
 								"org.apache.derby.jdbc.EmbeddedDriver", 
 								"user", 
 								"password", 
 								null,
 								"com.eroi.migrate.testpackage",
 								"CanNotBeFound", 
 								null,
 								null,
 								null);  
 		
 		List<Class<? extends Migration>> list = Engine.classesToMigrate();
 		assertNotNull(list);
 		assertEquals(0, list.size());
 	}
 	
 	public void testClassesToMigrate_UsingCustomSeparator() {
 		Configure.configure("jdbc:derby:testDerby;create=true", 
 								"org.apache.derby.jdbc.EmbeddedDriver", 
 								"user", 
 								"password", 
 								null,
 								"com.eroi.migrate.testpackage",
 								"DataChange", 
 								"Version",
 								null,
 								null);  
 		
 		List<Class<? extends Migration>> list = Engine.classesToMigrate();
 		assertNotNull(list);
 		assertEquals(2, list.size());
 		assertEquals("com.eroi.migrate.testpackage.DataChangeVersion1", list.get(0).getName());
 		assertEquals("com.eroi.migrate.testpackage.DataChangeVersion2", list.get(1).getName());
 		
 	}
 	
 	public void testOrderMigrations_GoUpFrom3() {
 
 		List<Class<? extends Migration>> migrations = getClassesToMigrate();
 		
 		//Full ugrade with current db at 3
 		List<Class<? extends Migration>> list = Engine.orderMigrations(migrations, 3, Integer.MAX_VALUE);
 		assertNotNull(list);
 		assertEquals(2, list.size());
 		assertEquals("com.eroi.migrate.testpackage.Migration_4", list.get(0).getName());
 		assertEquals("com.eroi.migrate.testpackage.Migration_5", list.get(1).getName());
 		
 	}
 	
 	public void testOrderMigrations_GoUpFrom1To4() {
 		
 		List<Class<? extends Migration>> migrations = getClassesToMigrate();
 		
 		List<Class<? extends Migration>> list = Engine.orderMigrations(migrations, 1, 4);
 		assertNotNull(list);
 		assertEquals(3, list.size());
 		assertEquals("com.eroi.migrate.testpackage.Migration_2", list.get(0).getName());
 		assertEquals("com.eroi.migrate.testpackage.Migration_3", list.get(1).getName());
 		assertEquals("com.eroi.migrate.testpackage.Migration_4", list.get(2).getName());
 		
 	}
 	
 	public void testOrderMigrations_GoUpFrom2To3() {
 		
 		List<Class<? extends Migration>> migrations = getClassesToMigrate();
 		
 		List<Class<? extends Migration>> list = Engine.orderMigrations(migrations, 2, 3);
 		assertEquals(1, list.size());
 		assertEquals("com.eroi.migrate.testpackage.Migration_3", list.get(0).getName());
 		
 	}
 	
 	public void testOrderMigrations_GoDownFrom3() {
 
 		List<Class<? extends Migration>> migrations = getClassesToMigrate();
 
 		List<Class<? extends Migration>> list = Engine.orderMigrations(migrations, 3, 0);
 		assertEquals(3, list.size());
 		assertEquals("com.eroi.migrate.testpackage.Migration_3", list.get(0).getName());
 		assertEquals("com.eroi.migrate.testpackage.Migration_2", list.get(1).getName());
 		assertEquals("com.eroi.migrate.testpackage.Migration_1", list.get(2).getName());
 		
 	}
 	
 	public void testOrderMigrations_GoDownFrom4To1() {
 
 		List<Class<? extends Migration>> migrations = getClassesToMigrate();
 		
 		List<Class<? extends Migration>> list = Engine.orderMigrations(migrations, 4, 1);
 		assertEquals(3, list.size());
 		assertEquals("com.eroi.migrate.testpackage.Migration_4", list.get(0).getName());
 		assertEquals("com.eroi.migrate.testpackage.Migration_3", list.get(1).getName());
 		assertEquals("com.eroi.migrate.testpackage.Migration_2", list.get(2).getName());
 		
 	}
 	
 	public void testOrderMigrations_GoDownFrom3To2() {
 
 		List<Class<? extends Migration>> migrations = getClassesToMigrate();
 		
 		List<Class<? extends Migration>> list = Engine.orderMigrations(migrations, 3, 2);
 		assertEquals(1, list.size());
 		assertEquals("com.eroi.migrate.testpackage.Migration_3", list.get(0).getName());
 		
 	}
 	
 	public void testGetVersionNumber() {
 		Configure.configure("url", "driver", "", "", "com.packagename");
 		
 		assertEquals(1, Engine.getVersionNumber("com.packagename.Migration_1"));
 		assertEquals(72, Engine.getVersionNumber("com.packagename.Migration_72"));
 	}
 	
 	public void testGetVersionNumber_UsingDifferentPrefixAndSep() {
 		Configure.configure("url", 
 							"driver", 
 							"", 
 							"", 
 							null, 
 							"com.packagename",
 							"Classes",
 							"Ver",
 							new Integer(0),
 							null);
 		
 		assertEquals(1, Engine.getVersionNumber("com.packagename.ClassesVer1"));
 		assertEquals(72, Engine.getVersionNumber("com.packagename.ClassesVer72"));
 	}
 	
 /* ----------------- Helper Methods ---------------*/
 	
 	private List<Class<? extends Migration>> getClassesToMigrate() {
 		Configure.configure("jdbc:derby:testDerby;create=true", 
 								   "org.apache.derby.jdbc.EmbeddedDriver",
 								   "user",
 								   "password",
 									"com.eroi.migrate.testpackage"); 
 		
 		return Engine.classesToMigrate();
 	}
 }
