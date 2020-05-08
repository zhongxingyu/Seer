 package knn.clean;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.TreeMap;
 
 import knn.Instance;
 import knn.distance.SemanticPair;
 import strategies.LastCategoryGeneralization;
 import db.WikipediaConnector;
 import dbpedia.similarityStrategies.ValueComparator;
 
 /**
  * This class compute the evaluation for one PathIndex. The evaluation is the
  * one that is included in the journal article.
  * 
  * @author dtorres
  * 
  */
 
 public class BlueFinderEvaluation {
 
 	private KNN knn;
 
 	public BlueFinderEvaluation(KNN knn) {
 		this.knn = knn;
 	}
 
 	public void processTest(int proportionOfConnectedPairs, int kValue, String resultTableName)
 			throws ClassNotFoundException, SQLException {
 
 		ResultSet resultSet = WikipediaConnector
 				.getRandomProportionOfConnectedPairs(proportionOfConnectedPairs);
 
 		String relatedUFrom = "u_from=0 ";
 		String relatedString = "";
 
 		while (resultSet.next()) {
 			long time_start, time_end;
 			time_start = System.currentTimeMillis();
 
 			SemanticPair disconnectedPair = this.knn.generateSemanticPair(
 					resultSet.getString("page"), resultSet.getLong("id"));
 
 			List<Instance> kNearestNeighbors = this.knn.getKNearestNeighbors(
 					kValue, disconnectedPair);
 
 			SemanticPairInstance disconnectedInstance = new SemanticPairInstance(
 					0, disconnectedPair);
 
 			kNearestNeighbors.remove(disconnectedInstance);
 
 			List<String> knnResults = new ArrayList<String>();
 			for (Instance neighbor : kNearestNeighbors) {
 
 				relatedUFrom = relatedUFrom + "or u_from = " + neighbor.getId()
 						+ " ";
 				relatedString = relatedString + "(" + neighbor.getDistance()
 						+ ") " + neighbor.getResource() + " ";
 
 				Statement st = WikipediaConnector.getResultsConnection()
 						.createStatement();
 
 				String queryFixed = "SELECT v_to, count(v_to) suma,V.path from UxV, V_Normalized V where v_to=V.id and ("
 						+ relatedUFrom + ") group by v_to order by suma desc";
 
 				ResultSet paths = st.executeQuery(queryFixed);
 				TreeMap<String, Integer> map = this.genericPath(paths,
 						knnResults.size() + 1);
 				// for (String pathGen : map.keySet()) {
 				// System.out.println(map);
 
 				knnResults.add(map.toString());
 				// System.out.println("end ---- k="+kvalue);
 
 			}
 			time_end = System.currentTimeMillis();
 			// Insert statem
 			
 			this.createResultTable(resultTableName);
 			
 			String insertSentence = "INSERT INTO "
 					+ resultTableName
 					+ "` (`v_to`, `related_resources`,`1path`, `2path`, `3path`, `4path`, `5path`, `6path`, `7path`, `8path`, `9path`, `10path`,`time`)"
 					+ "VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )";
 			PreparedStatement st = WikipediaConnector.getResultsConnection()
 					.prepareStatement(insertSentence);
 			st.setString(
 					1,
 					resultSet.getString("resource") + " "
 							+ resultSet.getInt("path_query"));
 			st.setString(2, relatedString);
 			int i = 3;
 			for (String string : knnResults) {
 				st.setString(i, string);
 				i++;
 
 			}
 			st.setLong(13, time_end - time_start);
 			st.executeUpdate();
 
 			relatedUFrom = "u_from=0 ";
 			relatedString = "";
 		}
 	}
 
 	void createResultTable(String resultTableName) throws SQLException, ClassNotFoundException {
 		String queryDrop = "DROP TABLE IF EXISTS `"+resultTableName+"`";
 		String query = "CREATE TABLE `"+resultTableName+"` ( `id` int(11) NOT NULL AUTO_INCREMENT, `resource` BLOB, `1path` int(11) DEFAULT NULL, `1pC` int(11) DEFAULT NULL,  `2path` int(11) DEFAULT NULL, `2pC` int(11) DEFAULT NULL,   `3path` int(11) DEFAULT NULL,  `3pC` int(11) DEFAULT NULL,  `4path` int(11) DEFAULT NULL,  `4pC` int(11) DEFAULT NULL,  `5path` int(11) DEFAULT NULL,  `5pC` int(11) DEFAULT NULL,  `6path` int(11) DEFAULT NULL,  `6pC` int(11) DEFAULT NULL,  `7path` int(11) DEFAULT NULL,  `7pC` int(11) DEFAULT NULL,  `8path` int(11) DEFAULT NULL,  `8pC` int(11) DEFAULT NULL,  `9path` int(11) DEFAULT NULL,  `9pC` int(11) DEFAULT NULL,  `10path` int(11) DEFAULT NULL,  `10pC` int(11) DEFAULT NULL,  `resourcePaths` int(11) DEFAULT NULL,  PRIMARY KEY (`id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8";
 		Statement statement = WikipediaConnector.getResultsConnection().createStatement();
 		statement.executeUpdate(queryDrop);
 		statement.close();
 		
 		statement = WikipediaConnector.getResultsConnection().createStatement();
 		statement.executeUpdate(query);
 		statement.close();		
 	}
 
 	protected TreeMap<String, Integer> genericPath(ResultSet paths, int kValue)
 			throws SQLException {
 		HashMap<String, Integer> pathDictionary = new HashMap<String, Integer>();
 		ValueComparator bvc = new ValueComparator(pathDictionary);
 		TreeMap<String, Integer> sorted_map = new TreeMap<String, Integer>(bvc);
 		LastCategoryGeneralization cg = new LastCategoryGeneralization();
 
 		while (paths.next()) {
 			// SELECT v_to, count(v_to) suma,V.path from UxV, V_Normalized V
 			String path = paths.getString("path");
 			path = cg.generalizePathQuery(path);
 			int suma = paths.getInt("suma");
 			//
 			if ((!path.contains("Articles_") || path.contains("Articles_lis"))
 					&& !path.contains("All_Wikipedia_")
 					&& !path.contains("Wikipedia_")
 					&& !path.contains("Non-free")
 					&& !path.contains("All_pages_")
 					&& !path.contains("All_non")) {
 				if (pathDictionary.get(path) == null) {
 					if (suma == kValue) { // all the cases belongs to this path
 											// query
 						suma = suma + 1000;
 					}
 					pathDictionary.put(path, suma);
 				} else {
 					suma += pathDictionary.get(path);
 					pathDictionary.put(path, suma);
 				}
 			}
 
 		}
 		// sorted_map.putAll(pathDictionary);
 		for (String path : pathDictionary.keySet()) {
 			Integer suma = pathDictionary.get(path);
 			sorted_map.put(path, suma);
 		}
 		return sorted_map;
 	}
 
 	
 	
 	public void enhanceUPage() throws ClassNotFoundException, SQLException {
 		Connection resultsConnection = WikipediaConnector.getResultsConnection();
 		resultsConnection.createStatement().executeUpdate("DROP TABLE IF EXISTS `U_pageEnhanced`");
 		resultsConnection.createStatement().executeUpdate("CREATE  TABLE `U_pageEnhanced` (`id` INT NOT NULL , `page` BLOB NOT NULL , `subjectTypes` BLOB NOT NULL , `objectTypes` BLOB NOT NULL , PRIMARY KEY (`id`))");
 		
 		String queryInsert = "INSERT INTO `U_pageEnhanced`(`id`,`page`,`subjectTypes`,`objectTypes`) VALUES "+
 						"(?,?,?,?)";
 
 		
		ResultSet rs = WikipediaConnector.getResultsConnection().createStatement().executeQuery("select * from `U_page`");
 		while(rs.next()){
 			String string=rs.getString("page");
 			String[] values = string.split(" ");
 			String subject = values[0];
 			String object = values[2];
 
 			List<String> objectTypes = WikipediaConnector
 					.getResourceDBTypes(object);
 			List<String> subjectTypes = WikipediaConnector
 					.getResourceDBTypes(subject);
 
 			SemanticPair result = new SemanticPair(object, subject, "",
 					objectTypes, subjectTypes, rs.getLong("id"));
 			
 			PreparedStatement statement = WikipediaConnector.getResultsConnection().prepareStatement(queryInsert);
 			statement.setLong(1, result.getId());
			statement.setString(2, result.getSubject()+" , "+result.getObject());
 			String subjectT="";
 			for (String type : result.getSubjectElementsBySemProperty("type")) {
 				subjectT=subjectT+" "+type;
 			}
			subjectT=subjectT.trim();
 			
 			statement.setString(3, subjectT);
 			String objectT="";
 		   for (String string2 : result.getObjectElementsBySemProperty("type")) {
 			objectT=objectT+" "+string2;
 			}
 		   
		   
		   statement.setString(4, objectT.trim());
 		   
 		   statement.executeUpdate();
 		   statement.close();
 		}
 		
 		
 	}
 	
 	public static void main(String[] args) throws ClassNotFoundException, SQLException {
 		KNN knn = new KNN();
 		BlueFinderEvaluation bfe = new BlueFinderEvaluation(knn);
		bfe.processTest(1, 10, "sc3BlueFinder");
 		
 	}
 
 	
 }
