 package org.bitweaver.lucene;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 
 import org.apache.lucene.analysis.Analyzer;
 import org.apache.lucene.analysis.standard.StandardAnalyzer;
 import org.apache.lucene.document.Document;
 import org.apache.lucene.document.Field;
 import org.apache.lucene.index.IndexWriter;
 
 /**
  * @author Christian Fowler <spider@bitweaver.org>
  * Based on sample code from phparch.com artilce by Dave Palmer <dave@engineworks.org>
  * in June 2003 issue.
  *
  * In order to run this, you need to first compile it. To do so, you need
  * a version of the JDK. I would recommend getting the Sun JDK 1.4:
  * http://java.sun.com
  */
 public class IndexEngine {
 
 	public static void main(String[] args) throws Exception {
 		try {
 			System.out.println("Preparing to index links database...");
 			index(getConnection( args[0], args[1], args[2] ), args[3] );
 			System.out.println("Index complete");
 		} catch( ArrayIndexOutOfBoundsException e ) {
 			System.out.println("Usage: javac org.bitweaver.IndexEngine <dbstring, ex: jdbc:mysql://localhost/bitweaver or jdbc:postgresql://localhost/bitweaver> <dbuser> <dbpassword> <db_table_prefix>");
 		}
 	}
 
 	private static void index(Connection conn, String pDbPrefix) throws Exception {
 
 		long indexTime = (System.currentTimeMillis() / 1000);
 		String qSql = "SELECT luci.lucene_id, lucene_query, index_path, index_fields FROM " + pDbPrefix + "lucene_indexes luci INNER JOIN " + pDbPrefix + "lucene_queries lucq ON (lucq.lucene_id=luci.lucene_id) WHERE next_index < "+indexTime;
 		PreparedStatement qStmt = conn.prepareStatement(qSql);
 		ResultSet qrs = qStmt.executeQuery();
 
 		while (qrs.next()) {
 
 			qrs.getString("lucene_query");
 
 			// this is the query we are going to use to populate our index
 			String sql = qrs.getString("lucene_query");
 			String indexPath = qrs.getString("index_path");
 
			Analyzer analyzer = new StandardAnalyzer();
			IndexWriter writer = new IndexWriter(indexPath,analyzer,true);
			PreparedStatement pStmt = conn.prepareStatement(sql);
 
 			System.out.println( "Executing query: "+sql );
 			ResultSet rs = pStmt.executeQuery();
 
 			int count = 0;
 			int interval = 250;
 			long timeout = 50;
 
 			System.out.println("Preparing to build index...");
 
 			ResultSetMetaData rsmd = rs.getMetaData();
 
 			String[] fields = new String[rsmd.getColumnCount()] ;
 			for( int i = 1; i <= rsmd.getColumnCount(); i++ ) {
 				fields[i-1] = rsmd.getColumnName(i);
 			}
 			while (rs.next()) {
 				try {
 					if (count == interval) {
 						java.lang.Thread.sleep(timeout);
 						count = 0;
 					} else {
 						count++;
 					}
 
 					System.out.println("Indexing: " + rs.getString(1) + ": " + rs.getString(2));
 
 					Document d = new Document();
 
 					for( int i = 0; i < fields.length; i++ ) {
 						// adding our columns to our Lucene Document object
 						if( rs.getString(fields[i]) != null ) {
 								d.add(Field.Text(fields[i], rs.getString(fields[i])));
 						}
 					}
 
 					// adding our document object instance to our writer
 					writer.addDocument(d);
 				} catch( Exception e ) {
 					System.out.println( "ERROR: " + rs.getString(1) + " - " + e );
 				}
 			}
 
			writer.close();
 
 			String uSql = "UPDATE " + pDbPrefix + "lucene_indexes SET next_index=index_interval+" + indexTime + ", last_indexed=" + indexTime + " WHERE lucene_id = " + qrs.getString("lucene_id");
 			PreparedStatement uStmt = conn.prepareStatement(uSql);
 			uStmt.execute();
 
 		}
 	}
 
 	private static Connection getConnection( String pDbConnection, String pDbUser, String pDbPass  ) throws Exception {
 		// the user name and password you need to use to log in to your MySQL DB
 		if( pDbConnection.indexOf( "mysql" ) > -1 ) {
 			Class.forName("com.mysql.jdbc.Driver").newInstance();
 		} else if( pDbConnection.indexOf( "postgresql" ) > -1 ) {
 			Class.forName("org.postgresql.Driver").newInstance();
 		}
 
 		System.out.println("Preparing connection with URL: " + pDbConnection);
 		System.out.println("database user: " + pDbUser);
 		return DriverManager.getConnection(pDbConnection, pDbUser, pDbPass);
 	}
 }
