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
 
 public class WhereTests {
 
 	@BeforeClass
 	public static void setUpBeforeClass() throws Exception {
 	}
 
 	@AfterClass
 	public static void tearDownAfterClass() throws Exception {
 	}
 
 	@After
 	public void tearDown() throws Exception {
 	}
 		
 	@Before
 	public void setUp() throws Exception {
 		CoffeeDB db = CoffeeDB.getInstance();
 		db.reset();
		db.runQuery("create table test (a int, b int);");
 		db.runQuery("insert into test values (10, 20);");
 		db.runQuery("insert into test values (15, 30);");
 	}
 
 	@Test
 	public void testEquals() {
 		CoffeeDB db = CoffeeDB.getInstance();
 		List<Tuple> results = db.runQuery("select a from test where b = 20;");
 		
 		assertTrue(results.size() == 1);
 		Tuple resultTuple = results.get(0);
 		assertTrue(resultTuple.getValue("a").toInt() == 10);
 	}
 
 }
