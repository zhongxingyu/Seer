 package benchmarks.sp2b;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.NavigableMap;
 
 import lupos.datastructures.queryresult.QueryResult;
 import lupos.engine.evaluators.BasicIndexQueryEvaluator;
 import luposdate.LuposServer;
 import luposdate.evaluators.P2PIndexQueryEvaluator;
 import net.tomp2p.peers.Number480;
 import net.tomp2p.storage.Data;
 
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 
 import p2p.P2PAdapter;
 import p2p.P2PConnection;
 import console.commands.LoadN3;
 
 public class Sp2bTest {
 
 
 	protected static final String			file_100_triples	= "benchmarks/sp2b/sp2b_100.n3";
 	protected static final String			file_1000_triples	= "benchmarks/sp2b/sp2b_1000.n3";
 	protected static final String			file_10000_triples	= "benchmarks/sp2b/sp2b_10000.n3";
 	protected static final String			file_500_triples	= "benchmarks/sp2b/sp2b_500.n3";
 	protected static final String			file_5000_triples	= "benchmarks/sp2b/sp2b_5000.n3";
 	protected static final String			file_50000_triples	= "benchmarks/sp2b/sp2b_50000.n3";
 	protected static final String			file_q1					= "benchmarks/sp2b/sp2b_q1.n3";
 	protected static final String			file_q2				= "benchmarks/sp2b/sp2b_q2.n3";
 	protected static final String			file_q2_with_optional	= "benchmarks/sp2b/sp2b_q2_with_optional.n3";
 	protected static final String			file_q3a				= "benchmarks/sp2b/sp2b_q3a.n3";
 	protected static final String			file_q3b				= "benchmarks/sp2b/sp2b_q3b.n3";
 	protected static final String			file_q3c				= "benchmarks/sp2b/sp2b_q3c.n3";
 	protected static final String			file_q4					= "benchmarks/sp2b/sp2b_q4.n3";
 	protected static final String			file_q5a				= "benchmarks/sp2b/sp2b_q5a.n3";
 	protected static final String			file_q5b				= "benchmarks/sp2b/sp2b_q5b.n3";
 	protected static final String			file_q6					= "benchmarks/sp2b/sp2b_q6.n3";
 	protected static final String			file_q7					= "benchmarks/sp2b/sp2b_q7.n3";
 	protected static final String			file_q8					= "benchmarks/sp2b/sp2b_q8.n3";
 	protected static final String			file_q9					= "benchmarks/sp2b/sp2b_q9.n3";
 	protected static final String			file_q10				= "benchmarks/sp2b/sp2b_q10.n3";
 	protected static final String			file_q11				= "benchmarks/sp2b/sp2b_q11.n3";
 	protected static final String			file_q12a				= "benchmarks/sp2b/sp2b_q12a.n3";
 	protected static final String			file_q12b				= "benchmarks/sp2b/sp2b_q12b.n3";
 	protected static final String			file_q12c				= "benchmarks/sp2b/sp2b_q12c.n3";
 
 	protected static final String			default_file			= file_5000_triples;
 
 	protected static final String			q1_query_filename	= "benchmarks/sp2b/queries/q1.sparql";
 	protected static final String			q2_query_filename	= "benchmarks/sp2b/queries/q2.sparql";
 	protected static final String			q3a_query_filename	= "benchmarks/sp2b/queries/q3a.sparql";
 	protected static final String			q3b_query_filename	= "benchmarks/sp2b/queries/q3b.sparql";
 	protected static final String			q3c_query_filename	= "benchmarks/sp2b/queries/q3c.sparql";
 	protected static final String			q4_query_filename	= "benchmarks/sp2b/queries/q4.sparql";
 	protected static final String			q5a_query_filename	= "benchmarks/sp2b/queries/q5a.sparql";
 	protected static final String			q5b_query_filename	= "benchmarks/sp2b/queries/q5b.sparql";
 	protected static final String			q6_query_filename	= "benchmarks/sp2b/queries/q6.sparql";
 	protected static final String			q7_query_filename	= "benchmarks/sp2b/queries/q7.sparql";
 	protected static final String			q8_query_filename	= "benchmarks/sp2b/queries/q8.sparql";
 	protected static final String			q9_query_filename	= "benchmarks/sp2b/queries/q9.sparql";
 	protected static final String			q10_query_filename	= "benchmarks/sp2b/queries/q10.sparql";
 	protected static final String			q11_query_filename	= "benchmarks/sp2b/queries/q11.sparql";
 	protected static final String			q12a_query_filename	= "benchmarks/sp2b/queries/q12a.sparql";
 	protected static final String			q12b_query_filename	= "benchmarks/sp2b/queries/q12b.sparql";
 	protected static final String			q12c_query_filename	= "benchmarks/sp2b/queries/q12c.sparql";
 
 	protected static P2PIndexQueryEvaluator	p2pEvaluator;
 	private static P2PAdapter				p2pAdapter;
 
 	public Sp2bTest() {
 		super();
 	}
 
 	@BeforeClass
 	public static void initAndLoadP2PNetwork() {
 		p2pEvaluator = initP2PQueryEvaluator();
 		// loadInP2PNetwork(default_file);
 	}
 
 	
 	protected static void loadInP2PNetwork(String file) {
 		LoadN3 loader = new LoadN3();
 		int loadedTriples = 7 * loader.load(p2pEvaluator, file);
 		NavigableMap<Number480, Data> p2pMap = ((P2PAdapter) p2pEvaluator
 				.getP2PAdapter()).peer.getPeerBean().getStorage().map();
 
		while (loadedTriples > p2pMap.size()) {
 			System.out.println(p2pMap.size());
 			try {
 				Thread.sleep(500);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 
 		System.out.println("fertig:" + p2pMap.size());
 	}
 
 	@AfterClass
 	public static void tearDown() {
 		p2pAdapter.peer.shutdown();
 	}
 
 	private static P2PIndexQueryEvaluator initP2PQueryEvaluator() {
 
 		try {
 			P2PConnection connection = new P2PConnection();
 			LuposServer server = new LuposServer();
 			connection.connect();
 			p2pAdapter = new P2PAdapter(connection.getPeer());
 			server.start(p2pAdapter);
 			p2pAdapter.setEvaluator(server.getEvaluator());
 
 			return server.getEvaluator();
 		} catch (ClassNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		return null;
 	}
 
 	protected QueryResult executeQuery(BasicIndexQueryEvaluator evaluator,
 			String query) throws Exception {
 		evaluator.compileQuery(query);
 		evaluator.logicalOptimization();
 		evaluator.physicalOptimization();
 		return evaluator.getResult();
 	}
 
 	protected String readFile(String file) {
 		InputStream is = getClass().getClassLoader().getResourceAsStream(file);
 		String selectQuery = convertStreamToString(is);
 		return selectQuery;
 	}
 
 	private String convertStreamToString(java.io.InputStream is) {
 	    java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
 	    return s.hasNext() ? s.next() : "";
 	}
 
 }
