 package sce.finalprojects.sceprojectbackend.database;
 import sce.finalprojects.sceprojectbackend.datatypes.ClusterRepresentationDO;
 import sce.finalprojects.sceprojectbackend.datatypes.Comment;
 import sce.finalprojects.sceprojectbackend.datatypes.CommentEntityDS;
 import sce.finalprojects.sceprojectbackend.datatypes.MapCell;
 import sce.finalprojects.sceprojectbackend.managers.ClustersManager;
 import  sce.finalprojects.sceprojectbackend.managers.DatabaseManager;
 import sce.finalprojects.sceprojectbackend.utils.MarkupUtility;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 
 public class DatabaseOperations {
 
 	/**
 	 * return all the comments vectors that belongs to the article
 	 * @param articleID 
 	 * @return an arrayList of comments 
 	 * @throws SQLException 
 	 */
 	public static ArrayList<Comment> getAllComentsWithoutHTML(String articleID) throws SQLException {
 		
 		//TODO: check what is the impact if the order is changed when returning the entries from the DB (when building the Mapping or XML )
 		//SELECT vector FROM comments WHERE article_id = 'articleID'
 		
 		Connection conn = DatabaseManager.getInstance().getConnection();
 		PreparedStatement sqlQuerry = conn.prepareStatement("SELECT comment_id,vector FROM comments WHERE article_id = ? ORDER BY comment_id ASC;");
 		sqlQuerry.setString(1, articleID);
 		ResultSet rs = sqlQuerry.executeQuery();
 		
 		ArrayList<Comment> ArrayOfComments = new ArrayList<Comment>();
 		
 		while(rs.next())
 		{
 			ArrayOfComments.add(new Comment(rs.getString("comment_id"),Comment.replaceStringWithVector(rs.getString("vector"))));
 		}
 		
 		return ArrayOfComments;
 	}
 	
 	/**
 	 * save the article mapping
 	 * @param articleId
 	 * @param mapping
 	 * @throws SQLException
 	 */
 	public static void setArticleMapping(String articleId, ArrayList<MapCell> mapping) throws SQLException {
 		//INSERT INTO `mydb`.`hacnodesmapping` (`articleid`, `commentid`, `node_mapping`) VALUES ('1', '12', 'dsa'), ('2', '12', 'dsa');
 		
 		Connection conn = DatabaseManager.getInstance().getConnection();
 		conn.setAutoCommit(false);
 		
 		PreparedStatement sqlQuerry = conn.prepareStatement("DELETE FROM HACNodesMapping WHERE  article_id = ? ;");
 		sqlQuerry.setString(1, articleId);
 		sqlQuerry.addBatch();
 		
 		String insertQuerry = "";
 		for (MapCell mapCell : mapping) {
 			insertQuerry += "('"+mapCell.getArticle_id()+"','"+mapCell.getComment_id()+"','"+mapCell.getMapping()+"'),";
 		}
 		insertQuerry = insertQuerry.substring(0, insertQuerry.length() - 1) + ";";
 		
 		sqlQuerry = conn.prepareStatement("INSERT INTO HACNodesMapping (articleid, commentid, node_mapping) VALUES "+insertQuerry);
 		sqlQuerry.addBatch();
 		
 		sqlQuerry.executeBatch();
 		conn.commit();
 		conn.setAutoCommit(true);
 		
 	}
 	
 	/**
 	 * return from DB the XML representation of the HAC for a given article
 	 * @param articleID
 	 * @return
 	 * @throws SQLException 
 	 */
 	public static String getXMLRepresentation(String articleID) throws SQLException {
 		
 		Connection conn = DatabaseManager.getInstance().getConnection();
 		PreparedStatement sqlQuerry = conn.prepareStatement("SELECT xmlRepresentation FROM articles WHERE article_id = ?;");
 		sqlQuerry.setString(1, articleID);
 		ResultSet rs = sqlQuerry.executeQuery();
 		
 		rs.next();
 		String xmlrep= rs.getString("xmlRepresentation");
 		
 		if(xmlrep.length() == 0)
 			return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>";
 		
 		//return xmlrep;
 		return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><Cluster id=\"1\" level=\"0\" mergeSim=\"1.0\"><Cluster id=\"6\" level=\"1\" mergeSim=\"0.9\"><Cluster id=\"10\" level=\"2\" mergeSim=\"1.0\"><Cluster id=\"10\" level=\"3\" mergeSim=\"1\"><Cluster id=\"10\" level=\"4\" mergeSim=\"1\"/></Cluster></Cluster><Cluster id=\"6\" level=\"2\" mergeSim=\"0.8\"><Cluster id=\"9\" level=\"3\" mergeSim=\"0.6\"><Cluster id=\"8\" level=\"4\" mergeSim=\"1.0\"/><Cluster id=\"9\" level=\"4\" mergeSim=\"0.6\"/></Cluster><Cluster id=\"6\" level=\"3\" mergeSim=\"0.4\"><Cluster id=\"7\" level=\"4\" mergeSim=\"1.0\"/><Cluster id=\"6\" level=\"4\" mergeSim=\"0.4\"/></Cluster></Cluster></Cluster><Cluster id=\"1\" level=\"1\" mergeSim=\"0.7\"><Cluster id=\"4\" level=\"2\" mergeSim=\"0.3\"><Cluster id=\"5\" level=\"3\" mergeSim=\"1.0\"><Cluster id=\"5\" level=\"4\" mergeSim=\"1\"/></Cluster><Cluster id=\"4\" level=\"3\" mergeSim=\"0.3\"><Cluster id=\"4\" level=\"4\" mergeSim=\"1\"/></Cluster></Cluster><Cluster id=\"1\" level=\"2\" mergeSim=\"0.5\"><Cluster id=\"3\" level=\"3\" mergeSim=\"1.0\"><Cluster id=\"3\" level=\"4\" mergeSim=\"1\"/></Cluster><Cluster id=\"1\" level=\"3\" mergeSim=\"0.2\"><Cluster id=\"2\" level=\"4\" mergeSim=\"1.0\"/><Cluster id=\"1\" level=\"4\" mergeSim=\"0.2\"/></Cluster></Cluster></Cluster></Cluster>";
 		
 	}
 	
 	/**
 	 * save the xml representation
 	 * @param articleId
 	 * @param xmlHacRepresentation
 	 * @throws SQLException
 	 */
 	public static void setXmlRepresentation(String articleId, String xmlHacRepresentation) throws SQLException {
 		
 		Connection conn = DatabaseManager.getInstance().getConnection();
 		PreparedStatement sqlQuerry = conn.prepareStatement("UPDATE  articles SET xmlRepresentation = ?  WHERE  article_id = ? ;");
 		sqlQuerry.setString(1, xmlHacRepresentation);
 		sqlQuerry.setString(2, articleId);
 
 		sqlQuerry.execute();
 	}
 	
 	/**
 	 * return the article mapping
 	 * @param articleId
 	 * @return
 	 * @throws SQLException
 	 */
 	public static ArrayList<MapCell> getArticleMapping(String articleId) throws SQLException {
 		//TODO check that method
 		Connection conn = DatabaseManager.getInstance().getConnection();
 		
 		PreparedStatement sqlQuerry = conn.prepareStatement("SELECT * FROM HACNodesMapping WHERE article_id = ? ;");
 		sqlQuerry.setString(1, articleId);
 		ResultSet rs = sqlQuerry.executeQuery();
 		ArrayList<MapCell> returnarray = new ArrayList<MapCell>();
 		
 		while(rs.next()) {
 			returnarray.add(new MapCell(rs.getString("article_id"),rs.getString("comment_id"),rs.getString("node_mapping")));
 		}
 		
 		return returnarray;
 		
 		
 //		//TODO: replace by real DB statement
 //		
 //		String temp = "1 10 10_3, 1 8 9_3, 1 9 9_3, 1 7 6_3, 1 6 6_3, 1 5 5_3, 1 4 4_3, 1 3 3_3, 1 2 1_3, 1 1 1_3, 1 10 10_2, 1 8 6_2, 1 9 6_2, 1 7 6_2, 1 6 6_2, 1 5 4_2, 1 4 4_2, 1 3 1_2, 1 2 1_2, 1 1 1_2, 1 10 6_1, 1 8 6_1, 1 7 6_1, 1 6 6_1, 1 9 6_1, 1 5 1_1, 1 4 1_1, 1 3 1_1, 1 2 1_1, 1 1 1_1, 1 8 1_0, 1 7 1_0, 1 6 1_0, 1 9 1_0, 1 10 1_0, 1 3 1_0, 1 5 1_0, 1 2 1_0, 1 4 1_0, 1 1 1_0";
 //
 //		ArrayList<MapCell> returnarray = new ArrayList<MapCell>();
 //		String subs[] = temp.split("\\, ");
 //		
 //		for (String string : subs) {
 //			returnarray.add(new MapCell(string.split("\\ ")[0].trim(),string.split("\\ ")[1].trim(),string.split("\\ ")[2].trim()));
 //		}
 //		
 //		System.out.println(returnarray.toString());
 //		return returnarray;
 		
 	}
 	
 	/**
 	 * set the article words
 	 * @param articleId
 	 * @param words
 	 * @throws SQLException
 	 */
 	public static void setArticleWords(String articleId , String[] words) throws SQLException {
 		//TODO check the method
 		Connection conn = DatabaseManager.getInstance().getConnection();
 		String insertQuerry ="";
 		int i=0;
 		for (String word : words) {
 			insertQuerry += "("+articleId+","+word+","+(i++)+") , ";
 		}
 		insertQuerry = insertQuerry.substring(0, insertQuerry.length() - 1) + ";";
 		
 		PreparedStatement sqlQuerry = conn.prepareStatement("INSERT IGNORE INTO article_words (article_id,word,order) VALUES " + insertQuerry + ";");
 		
 		sqlQuerry.execute();
 	}
 	
 	public static String[] getArticleWords(String articleId) throws SQLException {
 		Connection conn = DatabaseManager.getInstance().getConnection();
 		
 		PreparedStatement sqlQuerry = conn.prepareStatement("SELECT * FROM articel_words WHERE article_id = ? ORDER BY order ASC;");
 		sqlQuerry.setString(1, articleId);
 		ResultSet rs = sqlQuerry.executeQuery();
 		
 		ArrayList<String> arrayOfWords = new ArrayList<String>();
 		
 		while(rs.next()) {
 			arrayOfWords.add(rs.getString("word"));
 		}
 		
 		return (String[]) arrayOfWords.toArray();
 	}
 	
 	/**
 	 * creating a new Article in DB
 	 * @param articleId
 	 * @param articleUrl
 	 * @param numOfComments
 	 * @throws SQLException
 	 */
 	public static void addNewArticle(String articleId, String articleUrl, int numOfComments) throws SQLException {
 		//TODO check that method
     	Connection conn = DatabaseManager.getInstance().getConnection();
 		PreparedStatement sqlQuerry = conn.prepareStatement("INSET INTO articles (article_id,url,number_of_comments,last_update) VALUES (?,?,?,?) ;");
 		sqlQuerry.setString(1, articleId);
 		sqlQuerry.setString(2, articleUrl);
 		sqlQuerry.setInt(3, numOfComments);
 		
 		java.text.SimpleDateFormat sdf = 
 		     new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 
 		sqlQuerry.setString(4, sdf.format(new java.util.Date()));
 		
 		sqlQuerry.execute();
 		
             //set in the table: article id, article url and the number of the comment we get the first time
     }
 	
 	/**
 	 * return the number of comments that stored in the DB for a given article
 	 * @param articleId
 	 * @return
 	 * @throws SQLException
 	 */
     public static int getArticleNumOfComments(String articleId) throws SQLException
     {
     	//TODO check that method
     	Connection conn = DatabaseManager.getInstance().getConnection();
 		PreparedStatement sqlQuerry = conn.prepareStatement("SELECT number_of_comments FROM articles WHERE article_id = ? ;");
 		sqlQuerry.setString(1, articleId);
 		ResultSet rs = sqlQuerry.executeQuery();
 		
 		while(rs.next()) {
 			return rs.getInt("number_of_comments");
 		}
 		return -1;   
     }
     
     /**
      * return the URL of given Article_id
      * @param articleId
      * @return
      * @throws SQLException
      */
     public static String getUrl(String articleId) throws SQLException
     {
     	//TODO check that method
     	Connection conn = DatabaseManager.getInstance().getConnection();
 		PreparedStatement sqlQuerry = conn.prepareStatement("SELECT url FROM articles WHERE article_id = ? ;");
 		sqlQuerry.setString(1, articleId);
 		ResultSet rs = sqlQuerry.executeQuery();
 		
 		while(rs.next()) {
 			return rs.getString("url");
 		}
 		return null;
     }
     
     /**
      * set the new number of comments for a given comment
      * @param articleId
      * @param numberOfComments
      * @throws SQLException
      */
     public static void setArticleNumOfComments(String articleId, int numberOfComments) throws SQLException
     {
     	//TODO check that method
     	Connection conn = DatabaseManager.getInstance().getConnection();
 		PreparedStatement sqlQuerry = conn.prepareStatement("UPDATE articles SET number_of_comments = ? WHERE article_id = ? ;");
 		sqlQuerry.setInt(1, numberOfComments);
 		sqlQuerry.setString(2, articleId);
 		sqlQuerry.execute();
     }
 
     /**
      * add comments for an article
      * @param articleId
      * @param commments
      * @throws SQLException
      */
     public static void addComments(String articleId,ArrayList<CommentEntityDS> commments) throws SQLException
     {
     	//TODO check that method
     	Connection conn = DatabaseManager.getInstance().getConnection();
     	String insertQuerry = "";
     	for (CommentEntityDS comm : commments) { //TODO check the toString => look at the getter of comments
 			insertQuerry += "("+comm.getId()+","+articleId+","+comm.getCommentHTML()+","+comm.getVector().toString()+") , ";
 		}
     	insertQuerry = insertQuerry.substring(0, insertQuerry.length() - 1) + ";";
 		
 		PreparedStatement sqlQuerry = conn.prepareStatement("INSERT INTO comments (comment_id,article_id,html,vector) VALUES " +insertQuerry+";");
 		
 		sqlQuerry.execute();
 	
     }
     
     
     private static Map<String, String> getClustersChildrenIDs(String clusterID, int level, String articleID){
     	try {
 			Connection conn = DatabaseManager.getInstance().getConnection();
 			Map<String, String> childrenIDs = new HashMap<String, String>();
 			String qryString = "SELECT comments_id,html FROM comments comms, HACNodesMapping mapping WHERE comms.comment_id=mapping.comment_id AND mapping.article_id=? AND mapping.node_mapping = \""+clusterID+"_"+level+"\"";
 	    	PreparedStatement qry = conn.prepareStatement(qryString);
 			qry.setString(1, articleID);
 			ResultSet rs = qry.executeQuery();
 			
 			while(rs.next()){
 				childrenIDs.put(rs.getString(0), rs.getString(1));
 			}
 			
 			return childrenIDs;
 				
     	} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
     	
     	return null;
     }
 
     public static Set<ClusterRepresentationDO> getClustersRepresentationByIDs(List<String> clusterIDs, int level, String articleID){
     	
     	Connection conn;
     	Map<String, String> children;
     	Set<String> childrenIDSet;
     	Set<ClusterRepresentationDO> repDOSet = new HashSet<ClusterRepresentationDO>();
    	String clusterID;
     	String parsedLabel;
     	
     	try{
     		
     		conn = DatabaseManager.getInstance().getConnection();

 	    	String qryString = "SELECT comments.* FROM comments comms, HACNodesMapping mapping WHERE comms.comment_id=mapping.comment_id AND mapping.article_id=? AND mapping.node_mapping IN (";
 	    	
 	    	for(String cid:clusterIDs){
 	    		qryString += "\""+cid+"_"+level+"\",";
 	    	}
 	    	
 	    	qryString = qryString.substring(0, qryString.lastIndexOf(","));
 	    	
 	    	qryString+=")";
 	    	
 	    
 	    	PreparedStatement qry = conn.prepareStatement(qryString);
 			qry.setString(1, articleID);
 			ResultSet rs = qry.executeQuery();
 			
 			level++;
			
			while(rs.next()) {
				clusterID = rs.getString("comment_id");
 				children = getClustersChildrenIDs(clusterID,level,articleID);
 				childrenIDSet = children.keySet();
 				parsedLabel = MarkupUtility.getCommentBodyFromMarkup(children.get(childrenIDSet.iterator().next()));
 				repDOSet.add(new ClusterRepresentationDO(clusterID, parsedLabel, childrenIDSet));
 			}
 
 		
     	}catch(SQLException e){
     		
     	}
 		
 		return repDOSet;
     }
     
     public static List<String> getCommentsForGivenCluster(String articleID, String clusterID, int level, int from, int to){
     	
     	Connection conn;
     	List<String> markupList = new ArrayList<String>();
     	
     	try{
     		
     		conn = DatabaseManager.getInstance().getConnection();
 
 	    	String qryString = "SELECT html FROM comments comms, HACNodesMapping mapping WHERE comms.comment_id=mapping.comment_id AND mapping.article_id=? AND mapping.node_mapping = ? LIMIT ?,?";
 	    	
 	    
 	    	PreparedStatement qry = conn.prepareStatement(qryString);
 			qry.setString(1, articleID);
 			qry.setString(2, clusterID+"_"+level);
 			qry.setInt(3, from);
 			qry.setInt(4, to);
 			ResultSet rs = qry.executeQuery();
 
 			while(rs.next()) {
 				markupList.add(rs.getString("html"));
 			}
 
 		
     	}catch(SQLException e){
     		
     	}
     	
     	return markupList;
 		
     }
     
     
 	
 	
 }
