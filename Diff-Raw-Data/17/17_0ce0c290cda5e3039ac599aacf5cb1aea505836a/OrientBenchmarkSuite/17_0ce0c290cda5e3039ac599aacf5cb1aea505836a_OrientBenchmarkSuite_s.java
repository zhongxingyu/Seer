 package de.uni.leipzig.IR15.Benchmark.suites;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 
 import com.orientechnologies.orient.core.db.graph.OGraphDatabase;
 import com.orientechnologies.orient.core.record.impl.ODocument;
 import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
 
 import de.uni.leipzig.IR15.Benchmark.Benchmark;
 import de.uni.leipzig.IR15.Importer.OrientDBImporter;
 
 public class OrientBenchmarkSuite extends AbstractBenchmarkSuite {
 
 	public static Logger log = Logger.getLogger(OrientBenchmarkSuite.class);
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		List<Benchmark> benchmarks = new ArrayList<Benchmark>();
 
 		/**
 		 * Place your benchmarks here
 		 */
 		testorient();
 
 		runBenchmarks(benchmarks);
 	}
 
 	public static void testorient() {		
 		List<ODocument> result;
 		OrientDBImporter OrientImport = new OrientDBImporter();
 
		// OrientImport.setUp();
		// OrientImport.importData();
 
 		OGraphDatabase orientdb = OrientImport.onlyLoadDB();
 
 		String q1 = "SELECT FROM OGraphVertex WHERE w_id = 4560";
 		result = orientdb.query(new OSQLSynchQuery<ODocument>(q1));
 		for (ODocument v : result)
 			System.out.println(v.toString());
 		/*
 		 * String q2 = "SELECT FROM 5:1000 WHERE all()"; result =
 		 * orientdb.query(new OSQLSynchQuery<ODocument>(q2)); for (ODocument v :
 		 * result) System.out.println(v.toString());
 		 */
 		OrientImport.tearDown();
 	}
 }
