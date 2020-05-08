package graphs;
 
 import java.sql.*;
 import javax.sql.*;
 import java.io.*;
 import java.util.*;
 import java.util.regex.*;
 
 import org.netezza.*;
 import org.netezza.util.*;
 
 //import org.jdom.*;
 //import org.jdom.output.*;
 //import org.jdom.xpath.* ;
 //import org.jdom.input.SAXBuilder;
 
 import org.apache.commons.dbcp.*;
 import org.apache.commons.pool.*;
 import org.apache.commons.pool.impl.*;
 
 public class TopKRank {
 	private Object monitor;
 	Connection c = null;
 
 	public static void main (String args[]) {
 		String f = "";
 
 		try {
 			new TopKRank(args, f);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	public TopKRank ()
 	{
 		c = null;
 	}
 
 	public TopKRank (String args[], String file)
 	{
 		try
 		{
 			String jdbcUrl = new String ("jdbc:netezza://192.168.1.56/graphs?user=admin&password=password");
 			String jdbcClass = new String ("org.netezza.Driver");
 
 			// Load the driver for this database
 			Class.forName(jdbcClass).newInstance();
 
 			int connectionPoolSize = (5 * 2) + 1;	
 			ObjectPool connectionPool;
 			connectionPool = new GenericObjectPool(null,              // factory
 					connectionPoolSize,                       // maxActive
 					GenericObjectPool.WHEN_EXHAUSTED_BLOCK,   // whenExhaustedAction
 					-1,                                       // maxWait
 					connectionPoolSize,                       // maxidle
 					1,                                        // minIdle
 					true,                                     // testOnBorrow
 					false,                                    // testOnReturn
 					3600000,                                  // timeBetweenEvictionRunMillis
 					3,                                        // numTestsPerEvictionRun
 					1800000,                                  // minEvictableIdleTimeMillis
 					true);                                    // testWhileIdle
 
 			ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(jdbcUrl, null);
 			PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory(connectionFactory, // connFactory
 					connectionPool,    // pool
 					null,              // stmtPoolFactory
 					"select 1",        // validationQuery
 					false,             // defaultReadOnly
 					true);             // defaultAutoCommit
 
 			DataSource dataSource = new PoolingDataSource(connectionPool);
 			c = dataSource.getConnection();	
 
 			String name = "test_collection";
 			int range = 0;
 			int lifetime = 0;
 
 			// read in queries from query file
 			// while (queryIn)
 			// {
 			// 	results << runQuery(query)
 			// 	query = newQuery()
 			// }
 
 			//c.close();
 		}
 		catch (SQLException sqle)
 		{
 			sqle.printStackTrace();
 		}
 		catch (ClassNotFoundException cnfe)
 		{
 			cnfe.printStackTrace();
 		}
 		catch (InstantiationException ie) {
 			ie.printStackTrace();
 		}
 		catch (IllegalAccessException iae)
 		{
 			iae.printStackTrace();
 		}
 	}
 
 	private void runQuery(ArrayList query)
 	{
 		int NUM_SUBGRAPHS = 15;
 	}
 
 	public class Subgraph {
 
 		private class SubgraphComparator implements Comparator {
 			public int compare(Object obj1, Object obj2)
 			{
 				Subgraph s1 = (Subgraph) obj1;
 				Subgraph s2 = (Subgraph) obj2;
 
 				int ret = -1;
 
 				// isomorph score is used after isomorph sub-graph is found.
 				// it is the official rank
 				if (s1.getIsomorphScore() > s2.getIsomorphScore())
 					ret = 1;
 				else if (s1.getIsomorphScore() > s2.getIsomorphScore())
 					ret = -1;
 				else // if the isomorphs are the same, they probably haven't been set
 					// so we want to order by upScore
 				{
 					if (s1.getUpScore() > s2.getUpScore())
 						ret = 1;
 					else if (s1.getUpScore() > s2.getUpScore())
 						ret = -1;
 					else
 						ret = 0;
 				}
 
 				return ret;
 			}
 		}
 
 		private LinkedList<String> query;
 		private final SubgraphComparator comp = new SubgraphComparator();
 
 		private int subgraph;
 
 		private double upScore;
 		private double isomorphScore;
 
 		public Subgraph(LinkedList<String> query, int subgraph)
 		{
 			this.query = query;
 			this.subgraph = subgraph;
 			this.isomorphScore = -1.0;
 
 			this.upScore = calcUpScore();
 		}
 
 		/**
 		 * Runs each query in query against the subgraph. If the particular
 		 * vertex_1 ---edge_type---> vertex_2 exists, we increment our upScore
 		 */
 		private double calcUpScore()
 		{
 			double ret = 0.0;
 			int numResults = 0;
 			try
 			{
 				PreparedStatement ps = c.prepareStatement(UPSCORE_QUERY);
 
 				String attrValue;
 				String vertex1;
 				String vertex2;
 
 				while (!query.isEmpty())
 				{
 					attrValue = query.removeFirst();
 					vertex1 = query.removeFirst();
 					vertex2 = query.removeFirst();
 
 					ps.setInt(1, subgraph);
 					ps.setString(2, vertex1);
 					ps.setString(3, vertex2);
 					ps.setString(4, attrValue);
 
 					System.err.println("Subgraph: " + subgraph);
 					System.err.println("Query: " + ps.toString() + "\n");
 
 					if (ps.execute())
 					{
 						System.out.println("Query success!");
 						// we're just incrementing now, eventually
 						// we need to normalize it based on something
 						// or other
 						ret++;
 					}
 					else
 					{
 						System.out.println("Query failure!");
 					}
 				}
 			}
 			catch (SQLException sqle)
 			{
 				sqle.printStackTrace();
 			}
 
 			return ret;
 		}
 
 		public double getUpScore()
 		{
 			return upScore;
 		}
 
 		public double getIsomorphScore()
 		{
 			return isomorphScore;
 		}
 
 		public Comparator getComparator()
 		{
 			return comp;
 		}
 	}
 
 	// The table selected depends on the naming scheme you use for the goddamn subgraphs. Remember that shit.
 	private static String UPSCORE_QUERY = "SELECT * FROM SUBGRAPHS_2 WHERE SUBGRAPH = ? AND VERTEX_1='?' AND VERTEX_2='?' AND ATTR_VALUE='?'";
 }
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
