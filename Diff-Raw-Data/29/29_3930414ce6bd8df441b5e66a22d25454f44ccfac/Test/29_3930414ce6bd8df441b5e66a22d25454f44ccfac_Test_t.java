 package nl.utwente.db.neogeo.preaggregate;
 
 import java.sql.Connection;
import java.sql.DriverManager;
 import java.sql.SQLException;
 import java.sql.Timestamp;
 
 public class Test {
 	
 	public static void main(String[] argv) {
 		System.out.println("Test pre-aggregate package");
		Connection connection = null;
		try {
			Class.forName("org.postgresql.Driver");
			connection = DriverManager.getConnection(
					   "jdbc:postgresql://silo2.ewi.utwente.nl:5432/xxxx","flokstra", "xxxx");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    runTest( connection );
 	}
 	
 	public static void runTest(Connection c) {
 		try {
 			// new TweetConverter(c,"public","london_hav_raw",c,"public","london_hav");
 			// new TweetConverter("/Users/flokstra/twitter_sm.db",c,"public","london_hav");
 			// new TweetConverter("/Users/flokstra/uk_raw.sql",c,"public","uk");
 			//
 			GeotaggedTweetAggregate pa = new GeotaggedTweetAggregate(c, "public", "london_hav_neogeo", "myAggregate", "coordinates",0,200000,null);
 			Object[][] obj_range = pa.getRangeValues(c);
 			pa.SQLquery(PreAggregate.AGGR_COUNT, obj_range);
 			
 			// GeotaggedTweetAggregate pa = new GeotaggedTweetAggregate(c, "public", "london_hav_neogeo", "myAggregate");
 			//
 			// pa.boxQuery("count",0.18471,51.60626,0.23073,51.55534); // in the middle of havering map *correction anomaly
 //			pa.boxQuery("count",-0.058,51.59,0.095,51.483); // left of havering, few tweets
 			// pa.boxQuery("count",-0.058,51.58961,0.095,51.48287); // left of havering, few tweets
 			// pa.boxQuery("count",-0.38326,51.62780,0.14554,51.39572); // a big london query
 			// pa.boxQuery("count",-8.4,60,1.9,49); // the entire UK query
 			
 			// pa.boxQuery3d("count",-0.058,51.58961,0.095,51.48287,new Timestamp(1319000000000L), new Timestamp(1319900000000L)); // left of havering, few tweets
 			// pa.boxQuery3d("count",0.18471,51.60626,0.23073,51.55534,new Timestamp(1319000000000L), new Timestamp(1319900000000L)); // in the middle of havering map *correction anomaly
 
 			 double vertcells = 70;
 //		     pa.createAggrGrid("uk_grid","count",(double)(60-49)/vertcells,-8.4,60,1.9,49); // the entire UK query
 			int[] iv_count = {10, 10};
 			Double[][] iv_first_obj = new Double[2][2];
 			// lowX,highY,highX,low = -8.4,60,1.9,49
			// pa.SQLquery_grid(PreAggregate.AGGR_COUNT, iv_first_obj, iv_count);
 			if ( false ) {
 			PegelAndelfingen2Aggregate pegel = new PegelAndelfingen2Aggregate(c, "public" , "andelfingen2", "pegel_andelfingen2", "timed");
 			// PegelAndelfingen2Aggregate pegel = new PegelAndelfingen2Aggregate(c, "public" , "andelfingen2", "pegel_andelfingen2");
 			pegel.timeQuery("count", 1167606600, 1312737480);
 			}
 
 			c.close();
 		} catch (SQLException e) {
 			System.out.println("Caught: " + e);
 			e.printStackTrace(System.out);
 		}
 		System.out.println("#!finished");
 	}
 
 }
