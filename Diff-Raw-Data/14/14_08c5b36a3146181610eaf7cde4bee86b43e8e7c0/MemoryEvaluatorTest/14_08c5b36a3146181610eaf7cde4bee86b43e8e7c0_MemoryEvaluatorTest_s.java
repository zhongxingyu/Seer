 package benchmarks.sp2b;
 
 import static org.junit.Assert.assertEquals;
 
 import java.util.Collection;
 import java.util.LinkedList;
 
 import lupos.datastructures.items.literal.LiteralFactory;
 import lupos.datastructures.items.literal.LiteralFactory.MapType;
 import lupos.datastructures.items.literal.URILiteral;
 import lupos.datastructures.queryresult.QueryResult;
 import lupos.engine.evaluators.MemoryIndexQueryEvaluator;
 
import org.junit.BeforeClass;
 import org.junit.Test;
 
 public class MemoryEvaluatorTest extends Sp2bTest {
 
 	static MemoryIndexQueryEvaluator	memoryEvaluator;
 
	@BeforeClass
	public static void initAndLoadMemoryEvaluator() {
 
 		memoryEvaluator = initMemoryEvaluator();
 
 		// load dataset to with any sample query (because the lupos api requires
 		// it)
 
 	}
 
 	private void loadInMemory(String file) {
 		try {
 			memoryEvaluator.getResult(full_filepath(file),
 					"SELECT * WHERE {<s> <p> <o>}");
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Test
 	public void testQ1() throws Exception {
 
 		loadInP2PNetwork(file_q1);
 		loadInMemory(file_q1);
 
 		String selectQuery = readFile(q1_query_filename);
 
 		QueryResult expected = executeQuery(memoryEvaluator, selectQuery);
 		QueryResult actual = executeQuery(p2pEvaluator, selectQuery);
 
 		assertEquals(expected, actual);
 	}
 
 	@Test
 	public void testQ2() throws Exception {
 		loadInP2PNetwork(file_q2);
 		loadInMemory(file_q2);
 
 		String selectQuery = readFile(q2_query_filename);
 
 		QueryResult expected = executeQuery(memoryEvaluator, selectQuery);
 		QueryResult actual = executeQuery(p2pEvaluator, selectQuery);
 
 		System.out.println(expected);
 
 		System.out.println(expected.toString().length());
 		System.out.println(actual);
 		System.out.println(actual.toString().length());
 		assertEquals(expected, actual);
 	}
 
 	@Test
 	public void testQ3a() throws Exception {
 		loadInP2PNetwork(file_q3a);
 		loadInMemory(file_q3a);
 
 		String selectQuery = readFile(q3a_query_filename);
 
 		QueryResult expected = executeQuery(memoryEvaluator, selectQuery);
 		QueryResult actual = executeQuery(p2pEvaluator, selectQuery);
 
 		assertEquals(expected, actual);
 	}
 
 	@Test
 	public void testQ3b() throws Exception {
 		loadInP2PNetwork(file_q3b);
 		loadInMemory(file_q3b);
 
 		String selectQuery = readFile(q3b_query_filename);
 
 		QueryResult expected = executeQuery(memoryEvaluator, selectQuery);
 		QueryResult actual = executeQuery(p2pEvaluator, selectQuery);
 
 		assertEquals(expected, actual);
 	}
 
 	@Test
 	public void testQ3c() throws Exception {
 		loadInP2PNetwork(file_q3c);
 		loadInMemory(file_q3c);
 
 		String selectQuery = readFile(q3c_query_filename);
 
 		QueryResult expected = executeQuery(memoryEvaluator, selectQuery);
 		QueryResult actual = executeQuery(p2pEvaluator, selectQuery);
 
 		assertEquals(expected, actual);
 	}
 
 	@Test
 	public void testQ4() throws Exception {
 		loadInP2PNetwork(file_q4);
 		loadInMemory(file_q4);
 
 		String selectQuery = readFile(q4_query_filename);
 
 		QueryResult expected = executeQuery(memoryEvaluator, selectQuery);
 		QueryResult actual = executeQuery(p2pEvaluator, selectQuery);
 
 		assertEquals(expected, actual);
 	}
 
 	@Test
 	public void testQ5a() throws Exception {
 
 		loadInP2PNetwork(file_q5a);
 		loadInMemory(file_q5a);
 
 		String selectQuery = readFile(q5a_query_filename);
 
 		QueryResult expected = executeQuery(memoryEvaluator, selectQuery);
 		QueryResult actual = executeQuery(p2pEvaluator, selectQuery);
 
 		assertEquals(expected, actual);
 	}
 
 	@Test
 	public void testQ5b() throws Exception {
 
 		loadInP2PNetwork(file_q5b);
 		loadInMemory(file_q5b);
 
 		String selectQuery = readFile(q5b_query_filename);
 
 		QueryResult expected = executeQuery(memoryEvaluator, selectQuery);
 		QueryResult actual = executeQuery(p2pEvaluator, selectQuery);
 
 		assertEquals(expected, actual);
 	}
 
 	@Test
 	public void testQ6() throws Exception {
 
 		loadInP2PNetwork(file_q6);
 		loadInMemory(file_q6);
 
 		String selectQuery = readFile(q6_query_filename);
 
 		QueryResult expected = executeQuery(memoryEvaluator, selectQuery);
 		QueryResult actual = executeQuery(p2pEvaluator, selectQuery);
 
 		assertEquals(expected, actual);
 	}
 
 	@Test
 	public void testQ7() throws Exception {
 
 		loadInP2PNetwork(file_q7);
 		loadInMemory(file_q7);
 
 		String selectQuery = readFile(q7_query_filename);
 
 		QueryResult expected = executeQuery(memoryEvaluator, selectQuery);
 		QueryResult actual = executeQuery(p2pEvaluator, selectQuery);
 
 		assertEquals(expected, actual);
 	}
 
 	@Test
 	public void testQ8() throws Exception {
 
 		loadInP2PNetwork(file_q8);
 		loadInMemory(file_q8);
 
 		String selectQuery = readFile(q8_query_filename);
 
 		QueryResult expected = executeQuery(memoryEvaluator, selectQuery);
 		QueryResult actual = executeQuery(p2pEvaluator, selectQuery);
 
 		assertEquals(expected, actual);
 	}
 
 	@Test
 	public void testQ9() throws Exception {
 
 		loadInP2PNetwork(file_q9);
 		loadInMemory(file_q9);
 
 		String selectQuery = readFile(q9_query_filename);
 
 		QueryResult expected = executeQuery(memoryEvaluator, selectQuery);
 		QueryResult actual = executeQuery(p2pEvaluator, selectQuery);
 
 		assertEquals(expected, actual);
 	}
 
 	@Test
 	public void testQ10() throws Exception {
 
 		loadInP2PNetwork(file_q10);
 		loadInMemory(file_q10);
 
 		String selectQuery = readFile(q10_query_filename);
 
 		QueryResult expected = executeQuery(memoryEvaluator, selectQuery);
 		QueryResult actual = executeQuery(p2pEvaluator, selectQuery);
 
 		assertEquals(expected, actual);
 	}
 
 	@Test
 	public void testQ11() throws Exception {
 
 		loadInP2PNetwork(file_q11);
 		loadInMemory(file_q11);
 		String selectQuery = readFile(q11_query_filename);
 
 		QueryResult expected = executeQuery(memoryEvaluator, selectQuery);
 		QueryResult actual = executeQuery(p2pEvaluator, selectQuery);
 
 		assertEquals(expected, actual);
 	}
 
 	@Test
 	public void testQ12a() throws Exception {
 
 		loadInP2PNetwork(file_q12a);
 		loadInMemory(file_q12a);
 		String selectQuery = readFile(q12a_query_filename);
 
 		QueryResult expected = executeQuery(memoryEvaluator, selectQuery);
 		QueryResult actual = executeQuery(p2pEvaluator, selectQuery);
 
 		assertEquals(expected, actual);
 	}
 	
 
 	@Test
 	public void testQ12b() throws Exception {
 
 		loadInP2PNetwork(file_q12b);
 		loadInMemory(file_q12b);
 		String selectQuery = readFile(q12b_query_filename);
 
 		QueryResult expected = executeQuery(memoryEvaluator, selectQuery);
 		QueryResult actual = executeQuery(p2pEvaluator, selectQuery);
 
 		assertEquals(expected, actual);
 	}
 	
 
 	@Test
 	public void testQ12c() throws Exception {
 
 		loadInP2PNetwork(file_q12c);
 		loadInMemory(file_q12c);
 		String selectQuery = readFile(q12c_query_filename);
 
 		QueryResult expected = executeQuery(memoryEvaluator, selectQuery);
 		QueryResult actual = executeQuery(p2pEvaluator, selectQuery);
 
 		assertEquals(expected, actual);
 	}
 
 
 	private static MemoryIndexQueryEvaluator initMemoryEvaluator() {
 		try {
 			memoryEvaluator = new MemoryIndexQueryEvaluator();
 			Collection<URILiteral> defaultGraphs = new LinkedList<URILiteral>();
 			defaultGraphs.add(LiteralFactory
 					.createURILiteralWithoutLazyLiteral("<inlinedata:>"));
 
 			LiteralFactory.setType(MapType.NOCODEMAP);
 			Collection<URILiteral> namedGraphs = new LinkedList<URILiteral>();
 			memoryEvaluator.prepareInputData(defaultGraphs, namedGraphs);
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		return memoryEvaluator;
 	}
 
 	private static String full_filepath(String filename) {
 		return MemoryEvaluatorTest.class.getClassLoader()
 				.getResource(filename).getPath();
 	}
 
 }
