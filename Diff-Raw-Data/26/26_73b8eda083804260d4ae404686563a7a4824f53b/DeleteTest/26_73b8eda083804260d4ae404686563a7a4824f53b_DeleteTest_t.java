 package coffeedb.test;
 
 import static org.junit.Assert.*;
 
 import java.util.List;
 
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import coffeedb.CoffeeDB;
 import coffeedb.Tuple;
 
 public class DeleteTest {
 
 	@BeforeClass
 	public static void setUpBeforeClass() throws Exception {
 	}
 
 	@AfterClass
 	public static void tearDownAfterClass() throws Exception {
 	}
 
 	@Before
 	public void setUp() throws Exception {
 		CoffeeDB db = CoffeeDB.getInstance();
 		db.reset();
		db.runQuery("create table test (a int, b int);");
 		db.runQuery("insert into test values (10, 20);");
 		db.runQuery("insert into test values (15, 30);");
 	}
 
 	@After
 	public void tearDown() throws Exception {
 	}
 
 	@Test
 	public void testDelete() {
 		CoffeeDB db = CoffeeDB.getInstance();
 		List<Tuple> results = db.runQuery("delete from test where b = 20;");
 		assertTrue(results.size() == 1);
 		
 		results = db.runQuery("select * from test;");
 		assertTrue(results.size() == 1);
 		
 		Tuple resultTuple = results.get(0);
 		assertTrue (resultTuple.getValue("b").toInt() != 20);
 	}
 
 }
