 package ch.epfl.ad.milestone2.app;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 import ch.epfl.ad.AbstractQuery;
 import ch.epfl.ad.bloomjoin.BloomJoin;
 import ch.epfl.ad.db.DatabaseManager;
 
 public class Query7 extends AbstractQuery {
 	@Override
 	public void run(String[] args) throws SQLException, InterruptedException {
 	
 		if (args.length < 1) {
 		    System.out.println("Arguments: config-file [NATION1 [NATION2]]");
 		    System.exit(1);
 		}
 		
 		String nation1 = "FRANCE";
 		String nation2 = "GERMANY";
 		
 		if (args.length >= 2) {
 			nation1 = args[1];
 		}
 		
 		if (args.length >= 3) {
 			nation2 = args[2];
 		}
 		
 		System.out.println(String.format("Executing Q7 with nations %s and %s",
 				nation1, nation2));
 		
 		DatabaseManager dbManager = createDatabaseManager(args[0]);
 		
 		dbManager.setResultShipmentBatchSize(5000);
 		
 		// ensure that temporary tables do not exist
 		dbManager.execute("DROP TABLE IF EXISTS temp_nation", allNodes);
 		dbManager.execute("DROP TABLE IF EXISTS temp_suppnation", "node0");
 		dbManager.execute("DROP TABLE IF EXISTS temp_col", allNodes);
 		dbManager.execute("DROP TABLE IF EXISTS temp_suppnation_col", "node0");
 		
 		// distribute reduced nation result to all nodes
 		dbManager.execute("CREATE TABLE temp_nation (n_name CHAR(25), n_nationkey INTEGER, PRIMARY KEY (n_nationkey))", allNodes);
 		dbManager.execute(
 			String.format(
 				"SELECT n_name, n_nationkey " +
 				"FROM nation " +
 				"WHERE n_name = '%s' OR n_name = '%s'", 
 			nation1, nation2),
 			"node0",
 			"temp_nation",
 			allNodes
 		);
 		
 		// join supplier with nation
		dbManager.execute("CREATE TABLE temp_suppnation(supp_nation CHAR(25), s_suppkey INTEGER, PRIMARY KEY (s_suppkey))", "node0");
 		dbManager.execute(
				"SELECT n_name as supp_nation, s_suppkey " +
 				"FROM supplier, temp_nation " +
 				"WHERE s_nationkey = n_nationkey",
 				"node0",
 				"temp_suppnation"
 			);
 		
 		// join customer, orders, lineitem and temp_nation
 		dbManager.execute("CREATE TABLE temp_col(cust_nation CHAR(25), l_year CHAR(4), volume DOUBLE PRECISION, l_suppkey INTEGER)", allNodes);
 		dbManager.execute("CREATE INDEX ON temp_col(l_suppkey)", allNodes);
 		dbManager.execute(
 				"SELECT n_name AS cust_nation, EXTRACT(year FROM l_shipdate) as l_year, l_extendedprice * (1 - l_discount) AS volume, l_suppkey " +
 				"FROM customer, orders, lineitem, temp_nation " +
 				"WHERE o_orderkey = l_orderkey AND c_custkey = o_custkey AND l_shipdate between date '1995-01-01' AND date '1996-12-31' AND c_nationkey = n_nationkey",
 				allNodes,
 				"temp_col"
 			);
 		
 		// bloom join supplier-nation with customer-orders-lineitem-temp_nation
 		BloomJoin bloomJoin = new BloomJoin(dbManager);
 		
 		dbManager.execute("CREATE TABLE temp_suppnation_col(supp_nation CHAR(25), cust_nation CHAR(25), l_year CHAR(4), revenue DOUBLE PRECISION)", "node0");
 		bloomJoin.join(
 				"temp_suppnation", "s_suppkey", "node0",
 				"temp_col", "l_suppkey", allNodes,
 				
 				"SELECT supp_nation, cust_nation, l_year, sum(volume) as revenue " +
 				"FROM temp_suppnation, temp_col " +
				"WHERE s_suppkey = l_suppkey and cust_nation <> supp_nation " +
 				"GROUP BY supp_nation, cust_nation, l_year ",
 				
 				"temp_suppnation_col",
 				"node0"
 		);
 
 		// get the final result and print the results
 		ResultSet result = dbManager.fetch(
 			"SELECT supp_nation, cust_nation, l_year, sum(revenue) as revenue " +
 			"FROM temp_suppnation_col " +			
 			"GROUP BY supp_nation, cust_nation, l_year ",
 			"node0"
 		);
 		
 		while(result.next()) {
 			System.out.print(result.getString(1));
 			System.out.print("|");
 			System.out.print(result.getString(2));
 			System.out.print("|");
 			System.out.print(result.getString(3));
 			System.out.print("|");
 			System.out.print(getFormattedFloat(result.getString(4)));
 			System.out.print("\n");
 		}
 
 		// drop temporary tables
 		dbManager.execute("DROP TABLE temp_nation", allNodes);
 		dbManager.execute("DROP TABLE temp_suppnation", "node0");
 		dbManager.execute("DROP TABLE temp_col", allNodes);
 		dbManager.execute("DROP TABLE temp_suppnation_col", "node0");
 	
 		bloomJoin.shutDown();
 		dbManager.shutDown();
 	}
 	
 	public static void main(String[] args) throws SQLException, InterruptedException {
 		new Query7().run(args);
 	}
 }
