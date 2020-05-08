 package databases;
 
 import geoLocation.GeoLocationDiscover;
 import geoLocation.LocationDescriptor;
 
 import java.net.MalformedURLException;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import keywordExctraction.Keyphrase;
 import application.Configuration;
 import documentParsing.Document;
 import documentParsing.ParsedDocument;
 
 public class KeywordMySQLInterface extends AbstractMysqlInterface {
 	private static KeywordMySQLInterface mySqlInterface = null;
 
 	public static KeywordMySQLInterface getinstance() {
 		if (mySqlInterface == null) {
 			mySqlInterface = new KeywordMySQLInterface();
 		}
 		return mySqlInterface;
 	}
 
 	private KeywordMySQLInterface() {
 		dbUrl = "jdbc:mysql://" + Configuration.keywordDBServerIp + ":"
 				+ Configuration.keywordDBServerPort + "/" + Configuration.keywordDBName;
 		user = Configuration.keywordDBUser;
 		password = Configuration.keywordDBPassword;
 		initConnection();
 	}
 
 	public static String validate(String s) {
 		// serve perché inserire stringhe con virgolette in MySql crea problemi.
 		String validatedUrl = s;
 		if (s != null) {
 			validatedUrl = s.replaceAll("\\\\", "\\\\\\\\");
 			validatedUrl = validatedUrl.replaceAll("\"", "\\\\\"");
 		}
 		return validatedUrl;
 	}
 
 	public List<ParsedDocument> addDocuments(Collection<ParsedDocument> pds) {
 		/**
 		 * This method must ensure that pd's URL is not added to the database if
 		 * it's already present (or delete the document before re-adding it),
 		 * since there is no UNIQUE costraint on url column.
 		 * */
 
 		// FIXME O MIO DIO!
 		// TODO inventare FOXME
 
 		List<ParsedDocument> processedDocs = new ArrayList<>();
 		String query = "";
		Map<Integer, ParsedDocument> sqlDocuments = new HashMap<>(); //TODO eliminare
		Map<Integer, Keyphrase> sqlKeywords = new HashMap<>(); //TODO eliminare
 
 		try {
 			openConnection();
 
 			query = "DROP TABLE IF EXISTS tmpTable";
 			st.executeUpdate(query);
 
 			query = "CREATE TEMPORARY TABLE tmpTable(url VARCHAR(2083))";
 			st.executeUpdate(query);
 
 			StringBuilder insertTmpTableQuery = new StringBuilder();
 			String insertTmpTableQueryComma = "";
 			StringBuilder insertDocumentQuery = new StringBuilder();
 			String insertDocumentQueryComma = "";
 
 			for (ParsedDocument pd : pds) {
 				try {
 					Document doc = pd.getDoc();
 					LocationDescriptor ld = GeoLocationDiscover.getLocationInfo(doc.getUrl());
 
 					String city = validate(ld.city);
 					String latitude = String.valueOf(ld.latitude);
 					String longitude = String.valueOf(ld.longitude);
 					String date = validate(doc.getDate());
 					String time = validate(doc.getTime());
 
 					String docUrl = validate(doc.getUrl());
 
 					insertTmpTableQuery.append(insertTmpTableQueryComma + "(\"" + docUrl + "\")");
 					insertTmpTableQueryComma = ", ";
 
 					String docValues = "\"" + docUrl + "\"" + ", " + "\"" + city + "\"" + ", "
 							+ latitude + ", " + longitude + ", \"" + date + "\", \"" + time + "\"";
 					insertDocumentQuery.append(insertDocumentQueryComma + "(" + docValues + ")");
 					insertDocumentQueryComma = ", ";
 
 					processedDocs.add(pd);
 				}
 				catch (MalformedURLException e) {
 					System.err.println(e);
 				}
 
 			} // end for
 
 			if (!insertTmpTableQuery.toString().isEmpty()) {
 				query = "INSERT INTO tmpTable VALUES " + insertTmpTableQuery.toString();
 				st.executeUpdate(query);
 				query = "DELETE FROM Document WHERE url IN (select * from tmpTable)";
 				st.executeUpdate(query);
 				query = "DELETE FROM Keyword WHERE key_id NOT IN "
 						+ " (SELECT kd.key_id FROM KeywordInDocument kd)";
 				st.executeUpdate(query);
 			}
 
 			if (!insertDocumentQuery.toString().isEmpty()) {
 				query = "INSERT INTO Document(url, city, latitude, longitude, crawling_date,"
 						+ "crawling_time) VALUES " + insertDocumentQuery.toString();
 				st.executeUpdate(query);
 			}
 
 			Set<String> values = new HashSet<>();
 			for (ParsedDocument pd : processedDocs) {
 				for (Keyphrase k : pd.getKeyphrases()) {
 					String phrase = validate(k.getPhrase());
 					String tag = (k.getTag() != null && !k.getTag().equals("")) ? "\""
 							+ k.getTag().getId() + "\"" : null;
 
 					query = "SELECT key_id FROM Keyword WHERE keyword=\"" + phrase
 							+ "\" AND tag " + ((tag != null) ? "=" + tag : " IS NULL");
 					rs = st.executeQuery(query);
 					if (!rs.next()) {
 						values.add("(\"" + phrase + "\", " + tag + ", \"" + k.getPos() + "\")");
 					}
 				}
 
 			}// end for
 
 			if (!values.isEmpty()) {
 				StringBuilder queryBuilder = new StringBuilder();
 				queryBuilder.append("INSERT INTO Keyword(keyword, tag, pos) VALUES ");
 				String tempKeywordComma = "";
 				for (String value : values) {
 					queryBuilder.append(tempKeywordComma + value);
 					tempKeywordComma = ", ";
 				}
 
 				query = queryBuilder.toString();
 				st.executeUpdate(query);
 
 			}
 
 			StringBuilder tempKeywordInDocument = new StringBuilder("");
 			String tempKeywordInDocumentComma = "";
 			for (ParsedDocument pd : processedDocs) {
 				Document doc = pd.getDoc();
 				query = "SELECT doc_id FROM Document WHERE url=\"" + doc.getUrl()
 						+ "\"";
 				rs = st.executeQuery(query);
 				int docId = -1;
 				if (rs.next()) {
 					docId = rs.getInt("doc_id");
					sqlDocuments.put(docId, pd);
 				}
 				if (docId == -1) {
 					throw new IllegalStateException("Cannot retrieve document id");
 				}
 				for (Keyphrase k : pd.getKeyphrases()) {
 					String phrase = validate(k.getPhrase());
 					String tag = (k.getTag() != null) ? "\"" + k.getTag().getId() + "\"" : null;
 					double percentRelevance = 100 * ((double) pd.getFrequency(k))
 							/ pd.getKeyphrases().size(); // FIXME
 															// controllare
 															// lo
 															// split
 					int keyId = -1;
 
 					query = "SELECT key_id FROM Keyword WHERE keyword=\"" + phrase
 							+ "\" AND tag " + ((tag != null) ? "=" + tag + "" : " IS NULL");
 					rs = st.executeQuery(query);
 					if (rs.next()) {
 						keyId = Integer.parseInt(rs.getString("key_id"));
						sqlKeywords.put(keyId, k);
 						tempKeywordInDocument.append(tempKeywordInDocumentComma + "(" + keyId
 								+ ", " + docId + ", " + percentRelevance + ")");
 
 						tempKeywordInDocumentComma = ", ";
 					}
 					else {
 						throw new IllegalStateException(
 								"The database does not contain the keyword " + k);
 					}
 				}
 			}// end for
 			if (!tempKeywordInDocument.toString().equals("")) {
 				query = "INSERT INTO KeywordInDocument(key_id, doc_id, relevance) VALUES "
 						+ tempKeywordInDocument.toString();
 				st.executeUpdate(query);
 			}
 		}
 
 		catch (SQLException ex) {
 			System.err.println("Error caused by query: \n\t" + query);
			System.err.println("Document map: \n\t" + sqlDocuments);
			System.err.println("Keyword map: \n\t" + sqlKeywords);
 			processedDocs = new ArrayList<>();
 			try {
 				con.rollback();
 			}
 			catch (SQLException e) {
 				e.printStackTrace();
 			}
 			ex.printStackTrace();
 
 		}
 		finally {
 			try {
 				closeConnection();
 			}
 			catch (SQLException ex) {
 				ex.printStackTrace();
 			}
 		}
 
 		return processedDocs;
 	}
 
 	@Deprecated
 	public void addDocument(ParsedDocument pd) throws MalformedURLException {
 		/**
 		 * This method must ensure that pd's URL is not added to the database if
 		 * it's already present (or delete the document before re-adding it),
 		 * since there is no UNIQUE costraint on url column.
 		 * */
 
 		Set<Keyphrase> keyphrases = pd.getExtractedKeyphrases();
 		if (!keyphrases.isEmpty()) {
 			ResultSet rs = null;
 
 			try {
 
 				Document doc = pd.getDoc();
 				LocationDescriptor ld = GeoLocationDiscover.getLocationInfo(doc.getUrl());
 
 				openConnection();
 
 				String city = ld.city;
 				String latitude = String.valueOf(ld.latitude);
 				String longitude = String.valueOf(ld.longitude);
 				String date = doc.getDate();
 				String time = doc.getTime();
 
 				st.executeUpdate("DELETE FROM Document WHERE url=\"" + doc.getUrl() + "\" ");
 
 				String docValues = "\"" + doc.getUrl() + "\"" + ", " + "\"" + city + "\"" + ", "
 						+ latitude + ", " + longitude + ", \"" + date + "\", \"" + time + "\"";
 
 				st.executeUpdate("INSERT INTO Document(url, city, latitude, longitude, crawling_date,"
 						+ "crawling_time) VALUES(" + docValues + ")");
 
 				rs = st.executeQuery("SELECT doc_id FROM Document WHERE url=\"" + doc.getUrl()
 						+ "\"");
 				int docId = -1;
 				if (rs.next()) {
 					docId = rs.getInt("doc_id");
 				}
 				if (docId == -1) {
 					throw new RuntimeException("Cannot retrieve document id");
 				}
 
 				StringBuilder tempKeyword = new StringBuilder("");
 				String comma = "";
 				for (Keyphrase k : keyphrases) {
 					String phrase = k.getPhrase();
 					String tag = (k.getTag() != null && !k.getTag().equals("")) ? "\""
 							+ k.getTag().getId() + "\"" : null;
 
 					String query = "SELECT key_id FROM Keyword WHERE keyword=\"" + phrase
 							+ "\" AND tag " + ((tag != null) ? "=" + tag + "" : " IS NULL");
 					rs = st.executeQuery(query);
 					if (!rs.next()) {
 						tempKeyword.append(comma + "(\"" + phrase + "\", " + tag + ", \""
 								+ k.getPos() + "\")");
 						comma = ", ";
 					}
 				}
 
 				if (!tempKeyword.toString().equals("")) {
 					String query = "INSERT INTO Keyword(keyword, tag, pos) VALUES "
 							+ tempKeyword.toString();
 					st.executeUpdate(query);
 
 				}
 
 				StringBuilder tempKeywordInDocument = new StringBuilder("");
 				comma = "";
 				for (Keyphrase k : keyphrases) {
 					String phrase = k.getPhrase();
 					String tag = (k.getTag() != null) ? "\"" + k.getTag().getId() + "\"" : null;
 					double percentRelevance = 100 * ((double) pd.getFrequency(k))
 							/ pd.getKeyphrases().size(); // FIXME controllare lo
 															// split
 					int keyId = -1;
 
 					rs = st.executeQuery("SELECT key_id FROM Keyword WHERE keyword=\"" + phrase
 							+ "\" AND tag " + ((tag != null) ? "=" + tag + "" : " IS NULL"));
 					if (rs.next()) {
 						keyId = Integer.parseInt(rs.getString("key_id"));
 						tempKeywordInDocument.append(comma + "(" + keyId + ", " + docId + ", "
 								+ percentRelevance + ")");
 
 						comma = ", ";
 					}
 					else {
 						throw new IllegalStateException(
 								"The database does not contain the keyword " + k);
 					}
 				}
 
 				if (!tempKeywordInDocument.toString().equals("")) {
 					String query = "INSERT INTO KeywordInDocument(key_id, doc_id, relevance) VALUES "
 							+ tempKeywordInDocument.toString();
 					st.executeUpdate(query);
 				}
 
 			}
 
 			catch (SQLException ex) {
 				try {
 					con.rollback();
 				}
 				catch (SQLException e) {
 					e.printStackTrace();
 				}
 				ex.printStackTrace();
 
 			}
 			finally {
 				try {
 					closeConnection();
 				}
 				catch (SQLException ex) {
 					ex.printStackTrace();
 				}
 			}
 		}
 	}
 
 	public List<KeywordQueryResult> getKeyDetails(String word) {
 		/**
 		 * Restituisce la lista vuota se non ci sono keyword corrispondenti alla
 		 * parola data.
 		 */
 		List<KeywordQueryResult> results = new ArrayList<>();
 
 		try {
 			openConnection();
 
 			rs = st.executeQuery("SELECT url, keyword, tag, pos, relevance "
 					+ " FROM KeywordInDocument kd, Document d, Keyword k "
 					+ " WHERE k.key_id=kd.key_id AND d.doc_id=kd.doc_id AND keyword=\"" + word
 					+ "\"");
 
 			while (rs.next()) {
 				results.add(new KeywordQueryResult(rs.getInt("key_id"), rs.getString("url"), rs
 						.getString("keyword"), rs.getString("tag"), rs.getString("pos"), Double
 						.parseDouble(rs.getString("relevance"))));
 			}
 
 		}
 		catch (SQLException ex) {
 			try {
 				con.rollback();
 			}
 			catch (SQLException e) {
 				e.printStackTrace();
 			}
 			ex.printStackTrace();
 
 		}
 		finally {
 			try {
 				closeConnection();
 
 			}
 			catch (SQLException ex) {
 				ex.printStackTrace();
 			}
 		}
 
 		return results;
 	}
 
 	public Map<String, String> getKeywordsFromTags(List<String> tags) {
 		Map<String, String> result = new HashMap<>();
 
 		if (tags.isEmpty()) {
 			return result;
 		}
 		try {
 			openConnection();
 
 			String query = "DROP TABLE IF EXISTS tmpTable";
 			st.executeUpdate(query);
 
 			query = "CREATE TEMPORARY TABLE tmpTable "
 					+ " (tag VARCHAR(20) CHARACTER SET utf8 COLLATE utf8_bin ) "
 					+ " CHARACTER SET utf8 COLLATE utf8_bin";
 			st.executeUpdate(query);
 
 			StringBuilder tmpTableQuery = new StringBuilder("INSERT INTO tmpTable(tag) VALUES ");
 			String comma = "";
 			for (String t : tags) {
 				tmpTableQuery.append(comma + "(\"" + t + "\")");
 				comma = ", ";
 			}
 			query = tmpTableQuery.toString();
 			st.executeUpdate(query);
 
 			query = "SELECT keyword, tag FROM Keyword k WHERE tag IN (SELECT * FROM tmpTable)";
 			rs = st.executeQuery(query);
 
 			while (rs.next()) {
 				result.put(rs.getString("tag"), rs.getString("keyword"));
 			}
 		}
 		catch (SQLException ex) {
 			try {
 				con.rollback();
 			}
 			catch (SQLException e) {
 				e.printStackTrace();
 			}
 			ex.printStackTrace();
 
 		}
 		finally {
 			try {
 				closeConnection();
 
 			}
 			catch (SQLException ex) {
 				ex.printStackTrace();
 			}
 		}
 
 		return result;
 	}
 
 }
