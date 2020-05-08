 package utils;
 
 import java.sql.Blob;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 import db.WikipediaConnector;
 
 public class DBInterface {
 	
 	public final String SEPARATOR;
 	
 	public DBInterface() {
 		SEPARATOR = ", ";
 	}
 	
 	public DBInterface(String separator) {
 		SEPARATOR = separator;
 	}
 
 	public boolean createNormalizedPathTable(String tableName) throws SQLException, ClassNotFoundException {
 		Connection conn = WikipediaConnector.getResultsConnection();
 		conn.createStatement().executeUpdate("DROP TABLE IF EXISTS `"+tableName+"`");
 		conn.createStatement().executeUpdate(
 				"CREATE TABLE `"+tableName+"` ("
 				+ "`id` int(3) NOT NULL AUTO_INCREMENT,"
 				+ "`path` longtext NOT NULL,"
 				+ "PRIMARY KEY (`id`),"
 				+ "KEY `path` (`path`(100)) USING BTREE"
 				+ ") ENGINE=MyISAM AUTO_INCREMENT=1 DEFAULT CHARSET=utf8"
 		);
 		return true;
 	}
 	
 	public boolean addToNormalizedPathTable(String tableName, Collection<String> paths) throws ClassNotFoundException, SQLException {
 		Connection conn = WikipediaConnector.getResultsConnection();
 		try {
 			conn.setAutoCommit(false);
 			PreparedStatement insertStmt = conn.prepareStatement("INSERT INTO "+tableName+"(path) VALUES (?)");
 			for (String path : paths) {
 				insertStmt.setString(1, path);
 				insertStmt.addBatch();
 			}
 			insertStmt.executeBatch();
 			conn.commit();
 		} catch (SQLException e) {
 			conn.rollback();
 			throw new SQLException();
 		}
		conn.setAutoCommit(true);
 		return true;
 	}
 
 	public Map<Integer, String> getNormalizedPaths(String tableName, int limit, int offset) throws ClassNotFoundException, SQLException {
 		Map<Integer, String> paths = new LinkedHashMap<Integer, String>();
 		Connection conn = WikipediaConnector.getResultsConnection();
 		PreparedStatement stmt = conn.prepareStatement(this.getStrQuery(tableName, limit, offset));
 		ResultSet results = stmt.executeQuery();
 		while (results.next()) {
 			paths.put(results.getInt("id"), results.getString("path"));
 		}
 		return paths;
 	}
 	
 	public boolean createClearedEvaluationTable(String tableName) throws ClassNotFoundException, SQLException {
 		Connection conn = WikipediaConnector.getResultsConnection();
 		conn.createStatement().executeUpdate(
 				"CREATE TABLE IF NOT EXISTS `"+tableName+"` ("
 				+ "`id` int(11) NOT NULL AUTO_INCREMENT,"
 				+ "`eval_id` int(11),"
 				+ "`resource` blob,"
 				+ "`1path` mediumtext,"
 				+ "`2path` mediumtext,"
 				+ "`3path` mediumtext,"
 				+ "`4path` mediumtext,"
 				+ "`5path` mediumtext,"
 				+ "`6path` mediumtext,"
 				+ "`7path` mediumtext,"
 				+ "`8path` mediumtext,"
 				+ "`9path` mediumtext,"
 				+ "`10path` mediumtext,"
 				+ "`relevantPaths` text,"
 				+ "PRIMARY KEY (`id`)"
 				+ ") ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8"
 		);
 		return true;
 	}
 	
 	public boolean addToClearedEvaluationTable(String tableName, int evalId, FromToPair pair, Map<Integer, List<String>> paths, String relevantPaths) throws ClassNotFoundException, SQLException {
 		Connection conn = WikipediaConnector.getResultsConnection();
 		PathsResolver pathResolver = new PathsResolver(SEPARATOR);
 		PreparedStatement stmt = conn.prepareStatement("INSERT INTO "+tableName+" VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
 		stmt.setNull(1, java.sql.Types.NULL);
 		stmt.setInt(2, evalId);
 		stmt.setString(3, pair.getConcatPair());
 		for (int k : paths.keySet()) {
 			List<String> path = paths.get(k);
 			stmt.setString(k + 3, pathResolver.simpleCoupledPaths(path));
 		}
 		stmt.setString(14, relevantPaths);
 		stmt.execute();
 		return true;
 	}
 	
 	public Map<Integer, Map<String, String>> getEvaluations(String tableName, int limit, int offset) throws ClassNotFoundException, SQLException {
 		Map<Integer, Map<String, String>> evals = new LinkedHashMap<Integer, Map<String, String>>();
 		Connection conn = WikipediaConnector.getResultsConnection();
 		PreparedStatement stmt = conn.prepareStatement(this.getStrQuery(tableName, limit, offset));
 		ResultSet results = stmt.executeQuery();
 		while (results.next()) {
 			Map<String, String> eval = new HashMap<String, String>();
 			Blob blobResource = results.getBlob("resource");
 			byte[] bdata = blobResource.getBytes(1, (int) blobResource.length());
 			String resource = new String(bdata);
 			eval.put("resource", resource);
 			eval.put("relevantPaths", results.getString("relevantPaths"));
 			for (int k = 1; k <= 10; k++) {
 				eval.put(k+"path", results.getString(k + "path"));
 			}
 			evals.put(results.getInt("id"), eval);
 		}
 		return evals;
 	}
 	
 	public Map<Integer, Map<String, String>> getEvaluation(String tableName, int id) throws ClassNotFoundException, SQLException {
 		Map<Integer, Map<String, String>> retResult = new HashMap<Integer, Map<String,String>>();
 		String strQuery = "SELECT * FROM "+tableName+" WHERE id = ?";
 		Connection conn = WikipediaConnector.getResultsConnection();
 		PreparedStatement stmt = conn.prepareStatement(strQuery);
 		stmt.setInt(1, id);
 		ResultSet results = stmt.executeQuery();
 		if (results.next()) {
 			Map<String, String> tmp = new HashMap<String, String>();
 			Blob blobResource = results.getBlob("resource");
 			byte[] bdata = blobResource.getBytes(1, (int) blobResource.length());
 			String resource = new String(bdata);
 			tmp.put("resource", resource);
 			for (int k = 1; k <= 10; k++) {
 				tmp.put(k+"path", results.getString(k + "path"));
 			}
 			retResult.put(id, tmp);
 		}
 		return retResult;
 	}
 	
 	public boolean addStatistic(String evalTableName, int k, double precision, double recall,
 			double f1, double hitRate, double giniIndex, double itemSupport, double userSupport, 
 			int maxRecomm) throws ClassNotFoundException, SQLException {
 		WikipediaConnector.insertParticularStatistics(evalTableName, k, precision, recall, f1, hitRate, giniIndex, itemSupport, userSupport, maxRecomm);
 		return true;
 	}
 	
 	private String getStrQuery(String table, int limit, int offset) {
 		String strQuery = "SELECT * FROM " + table;
 		if (limit > 0) {
 			strQuery += " LIMIT " + limit;
 			if (offset > 0) {
 				strQuery += " OFFSET " + offset;
 			}
 		}
 		return strQuery;
 	}
 }
