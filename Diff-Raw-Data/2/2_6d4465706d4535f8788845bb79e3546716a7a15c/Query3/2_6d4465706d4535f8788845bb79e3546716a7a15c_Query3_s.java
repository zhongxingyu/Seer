 package ch.epfl.ad.milestone2.app;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 import ch.epfl.ad.AbstractQuery;
 import ch.epfl.ad.db.DatabaseManager;
 
 public class Query3 extends AbstractQuery {
 	
 	@Override
 	public void run(String[] args) throws SQLException, InterruptedException {
 		
 		if (args.length < 1) {
 		    System.out.println("Arguments: config-file [SEGMENT [DATE]]");
 		    System.exit(1);
 		}
 		
 		// Segment enum: AUTOMOBILE, BUILDING, FURNITURE, MACHINERY, HOUSEHOLD
 		String querySegment = "BUILDING";
 		String queryDate = "1995-03-15";
 		
 		if (args.length >= 2) {
 			querySegment = args[1];
 		}
 		
 		if (args.length >= 3) {
 			queryDate = args[2];
 		}
 
 		System.out.println(String.format("Executing Q3 with segment: %s and date: %s",
 				querySegment, queryDate));
 		
 		DatabaseManager dbManager = createDatabaseManager(args[0]);
 		
 		dbManager.setResultShipmentBatchSize(5000);
 		
 		// ensure that temporary tables do not exist
 		dbManager.execute("DROP TABLE IF EXISTS temp_col", allNodes);
 		
		dbManager.execute("CREATE TABLE temp_col(l_orderkey INTEGER, revenue FLOAT, o_orderdate DATE, o_shippriority INTEGER, KEY (l_orderkey, o_orderdate, o_shippriority))", "node0");
 		dbManager.execute(
 				"SELECT l_orderkey, " +
 				"sum(l_extendedprice * (1 - l_discount)) as revenue, " +
 				"o_orderdate, " +
 				"o_shippriority " +
 				"FROM customer, orders, lineitem " +
 				String.format("WHERE o_orderkey = l_orderkey AND c_custkey = o_custkey " +
 							  "AND c_mktsegment = '%s' AND o_orderdate < '%s' AND " +
 							  "l_shipdate > '%s'", querySegment, queryDate, queryDate) +
 				"GROUP BY l_orderkey, o_orderdate, o_shippriority " +
 				"ORDER BY revenue desc, o_orderdate " +
 				"LIMIT 10",
 				allNodes,
 				"temp_col",
 				"node0"
 			);
 		
 		ResultSet result = dbManager.fetch(
 				"SELECT l_orderkey, sum(revenue) AS revenue, o_orderdate, o_shippriority " +
 				"FROM temp_col " +
 				"GROUP BY l_orderkey, o_orderdate, o_shippriority " +
 				"ORDER BY revenue desc, o_orderdate " +
 				"LIMIT 10",
 				"node0");
 		
 		while(result.next()) {
 			System.out.print(result.getString(1));
 			System.out.print("|");
 			System.out.print(result.getString(2));
 			System.out.print("|");
 			System.out.print(result.getString(3));
 			System.out.print("|");
 			System.out.print(result.getString(4));
 			System.out.print("\n");
 		}
 		
 		dbManager.execute("DROP TABLE IF EXISTS temp_col", allNodes);
 		dbManager.shutDown();
 	}
 	
 	public static void main(String[] args) throws SQLException, InterruptedException {
 		new Query3().run(args);
 	}
 }
