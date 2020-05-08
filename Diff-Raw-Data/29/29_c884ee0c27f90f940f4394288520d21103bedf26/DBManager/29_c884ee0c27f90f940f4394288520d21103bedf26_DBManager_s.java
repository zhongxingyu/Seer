 package ro.iasi.db;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import sun.font.EAttribute;
 
 public class DBManager {
 
 	private Connection connection;
 
 	private void createTables() throws SQLException {
 		Statement statement = connection.createStatement();
 		statement.executeUpdate("CREATE TABLE IF NOT EXISTS Documents (ID INTEGER PRIMARY KEY, DomainName TEXT, URL TEXT)");
 		statement.executeUpdate("CREATE TABLE IF NOT EXISTS Words (ID INTEGER PRIMARY KEY, Value TEXT)");
 		statement.executeUpdate("CREATE TABLE IF NOT EXISTS VerticalIndexes (ID INTEGER PRIMARY KEY, WordID INTEGER, DocID INTEGER, "
 				+ "FOREIGN KEY (WordID) REFERENCES Words(ID), FOREIGN KEY (DocID) REFERENCES Documents(ID))");
 	}
 
 	public Map<String, Integer> getDictionary() {
 		Map<String, Integer> result = new LinkedHashMap<>();
 
 		try {
 			Statement statement = connection.createStatement();
 			ResultSet rs = statement.executeQuery("SELECT id, value FROM words");
 			while (rs.next()) {
 				result.put(rs.getString("value"), rs.getInt("id"));
 			}
 			rs.close();
 		} catch (SQLException e) {
 			raiseSQLExecutionError();
 		}
 
 		return result;
 	}
 
 	private int storeDocument(String domain, String url) throws SQLException {
 		PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO documents (domain, url) VALUES (?, ?)",
 				PreparedStatement.RETURN_GENERATED_KEYS);
 		preparedStatement.setString(1, domain);
 		preparedStatement.setString(2, url);
 		preparedStatement.executeUpdate();
 		
 		int result = -1;
 		
 		ResultSet resultSet = preparedStatement.getGeneratedKeys();
 		while (resultSet.next()) {
 			result = resultSet.getInt(1);
 		}
 		resultSet.close();
 		
 		return result;
 	}
 
 	private void storeRelation(int docId, int wordId) throws SQLException {
 		PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO relations (doc_id, word_id) VALUES (?, ?)");
 		preparedStatement.setInt(1, docId);
 		preparedStatement.setInt(2, wordId);
		preparedStatement.execute();
 	}
 
	public void storeIndexes(Map<String, Map<String, List<Integer>>> indexes) {
 		connection.setAutoCommit(false);
 
 		for (Entry<String, Map<String, List<Integer>>> domainEntry : indexes.entrySet()) {
 			String domain = domainEntry.getKey();
 
 			for (Entry<String, List<Integer>> urlEntry : domainEntry.getValue().entrySet()) {
 				String url = urlEntry.getKey();
 
 				int docId = storeDocument(domain, url);
 
 				for (Integer wordId : urlEntry.getValue()) {
					store
 				}
 			}
 		}

		connection.commit();
		connection.setAutoCommit(true);

		PreparedStatement prep = conn.prepareStatement("insert into people values (?, ?);");

		prep.setString(1, "Gandhi");
		prep.setString(2, "politics");
		prep.addBatch();
		prep.setString(1, "Turing");
		prep.setString(2, "computers");
		prep.addBatch();
		prep.setString(1, "Wittgenstein");
		prep.setString(2, "smartypants");
		prep.addBatch();

		conn.setAutoCommit(false);
		prep.executeBatch();
		conn.setAutoCommit(true);

 	}
 
 	public void connect() {
 		try {
 			Class.forName("org.sqlite.JDBC");
 			connection = DriverManager.getConnection("jdbc:sqlite:test.db");
 
 			// TODO created by script
 			// createTables();
 		} catch (ClassNotFoundException ex) {
 			System.err.println("Could not find database driver");
 			System.exit(-1);
 		} catch (SQLException e) {
 			raiseSQLExecutionError();
 		}
 	}
 
 	private void raiseSQLExecutionError() {
 		System.err.println("SQL execution problem");
 		System.exit(-1);
 	}
 
 }
