 package coffeedb.test;
 
 import static org.junit.Assert.*;
 
 import java.util.List;
 
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import coffeedb.CoffeeDB;
 import coffeedb.Schema;
 import coffeedb.Tuple;
 import coffeedb.types.Type;
 
 public class JoinTests {
 
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
 	}
 
 	@After
 	public void tearDown() throws Exception {
 	}
 
 	@Test
 	public void basicJoin() {
 		CoffeeDB db = CoffeeDB.getInstance();
		db.runQuery("create table test (a int);");
 		db.runQuery("create table test2 (b int);");
 		db.runQuery("insert into test values (10);");
 		db.runQuery("insert into test2 values (20);");
 		
 		List<Tuple> results = db.runQuery("select * from test, test2;");
 		assertTrue(results.size() == 1);
 		Tuple joined = results.get(0);
 		assertTrue(joined.getValue("a").toInt() == 10);
 		assertTrue(joined.getValue("b").toInt() == 20);
 	}
 }
