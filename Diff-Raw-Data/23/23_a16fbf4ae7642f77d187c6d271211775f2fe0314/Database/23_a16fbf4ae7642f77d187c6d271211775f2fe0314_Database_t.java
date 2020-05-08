 package Indexer;

 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.sql.ResultSet;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Set;
 
 public class Database {
 	private final String dbName = "Traceability";
 	private final String password = "vExKFHJpxy2XPUvt";
 	private final String userName = "trace";
 	private final String ip = "96.63.247.168:3306";
 	private final String driver = "com.mysql.jdbc.Driver";
 	private final String url = "jdbc:mysql://" +ip ;
 
 	private Connection conn = null;
 
 	private final String addTokenQuery = "INSERT INTO token (column2) VALUES ('%s') "; // Data
 	private final String addLinkQuery = "INSERT INTO link (TokenID,DocID, Quantity, SourceType) VALUES ((SELECT ID FROM token WHERE Data='%s'),(SELECT ID FROM document WHERE Path='%s') ,'%s', '%s')";
 	private final String addDocQuery = "INSERT INTO document(path) VALUES ('%s')";
 	
	//singleton code
		private static Database db = null;
		//getInstance() is how you access the Database
		public static Database getInstance(){
			if(db==null){
				db = new Database();
				return db;
			}
			else
			{
				return db;
			}
			
		}
		
		// constructor
		protected Database(){
			openConnect();
		}

	
 	public void buildQuery(){
 
 	} 
 
 	private void submitQuery(){
 
 	}
 
 	public void storeTokens(TokenTracker tt, String docName){
 		Set<String> codeTokens = tt.getCodeKeys();
 		Set<String> commentTokens = tt.getCommentKeys();
 
 		HashMap<String, Boolean> tokenStored = new HashMap<String, Boolean>();
 
 		Iterator<String> codeIterator = codeTokens.iterator();
 		Iterator<String> commentIterator = commentTokens.iterator(); 
 
 		StringBuffer queryBuffer = new StringBuffer();
 		
 		String eachTokenInsertQuery;
 		String eachToken;
 		String eachTokenCount;
 		String eachLinkInsertQuery;
 		
 		while(codeIterator.hasNext()){
 			eachToken = codeIterator.next();
 			eachTokenCount = Integer.toString(tt.getCodeTokCount(eachToken));
 			
 			eachTokenInsertQuery = String.format(addTokenQuery, eachToken);
 			queryBuffer.append(eachTokenInsertQuery);
 			
 			eachLinkInsertQuery = String.format(addLinkQuery, eachToken,docName,eachTokenCount,"CODE");
 			queryBuffer.append(eachLinkInsertQuery);
 			
 			tokenStored.put(eachToken,true);
 			
 		}
 
 		while(commentIterator.hasNext()){
 			eachToken = codeIterator.next();
 			eachTokenCount = Integer.toString(tt.getCommentTokCount(eachToken));
 			
 			if(!tokenStored.get(eachToken)){
 				eachTokenInsertQuery = String.format(addTokenQuery, commentIterator.next());
 				queryBuffer.append(eachTokenInsertQuery);
 			}
 			
 			eachLinkInsertQuery = String.format(addLinkQuery, eachToken,docName,eachTokenCount,"COMMENT");
 			queryBuffer.append(eachLinkInsertQuery);
 		}
 	}
 	public void storeToken(String token, int quantity, String SourceType){
 
 	}
 
 
 	//-------------------------------------------------------------
 
 	public void openConnect(){
 		try {
 			Class.forName(driver).newInstance();
 			conn = DriverManager.getConnection(url+dbName,userName,password);
 
 			// Do something with the Connection
 
 		} 
 		catch (SQLException | InstantiationException | IllegalAccessException | ClassNotFoundException ex) {
 			// handle any errors
 			System.out.println("SQLException: " + ex.getMessage());
 			System.out.println("SQLState: " + ((SQLException) ex).getSQLState());
 			System.out.println("VendorError: " + ((SQLException) ex).getErrorCode());   
 		}
 
 	}
 
 	public void closeConnect(){
 		try{
 			conn.close();
 		}
 		catch(Exception e){
 
 		}
 	}
 }
