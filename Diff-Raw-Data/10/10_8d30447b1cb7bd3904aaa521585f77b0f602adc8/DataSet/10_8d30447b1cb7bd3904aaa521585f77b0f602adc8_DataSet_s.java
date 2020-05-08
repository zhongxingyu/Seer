 package danger_zone;
 import java.util.HashMap;
 import java.io.*;
 import java.sql.*;
 import java.sql.ResultSet;
 import java.util.Vector;
 import java.util.Iterator;
 
 /**
 *@author Ethan Eldridge <ejayeldridge @ gmail.com>
 *@version 0.0
 *@since 2012-10-22
 *
 * Class providing an interface to the SQL database for training purposes. 
 */
 public class DataSet{
 	/**
 	*Connect to the database
 	*/
 	Connection con = null;
 
 	/**
 	*Starting size of the dataset variable. 
 	*/
 	static private final int dSize = 1000;
 
 	/**
 	*Vector to hold the dataset from the database
 	*/
 	public Vector<Tweet> dataset = new Vector<Tweet>(dSize);
 
 	/**
 	*Iterator for vector
 	*/
 	private Iterator<Tweet> dataIter = dataset.iterator();
 
 	/**
 	*
 	*/
 	private transient String password = "";
 
 	/**
 	*Opens a connection to the database
 	*/
 	public Connection openConnection(String password) throws Exception{
 		java.util.Properties properties = new java.util.Properties();
 		properties.put("user","cs276");
 		properties.put("password",password);
 	  	//properties.put("characterEncoding", "ISO-8859-1");
 	  	//properties.put("useUnicode", "true");
 	  	String url = "jdbc:mysql://dangerzone.cems.uvm.edu/test";
 
 	  	Class.forName("com.mysql.jdbc.Driver").newInstance();
 	  	Connection c = DriverManager.getConnection(url, properties);
 	  	System.out.println(c);
 	  	this.password = password;
 	  	return c;
 	}
 
 	/**
 	*Returns the result set of the tweet table from the database
 	*@return The result set of the tweet table
 	*/
 	public ResultSet getData() throws Exception{
 		Statement query = con.createStatement();
 		query.executeQuery("SELECT * FROM tbl_tweet");
 		return query.getResultSet();
 	}
 
 	/**
 	*This function commits data the naive bayes has been training on to the database so we can rebuild the bayes at anytime.
 	*@param cat category used by the bayes that the text belongs to
 	*@param text the string that is used in training the bayes.
 	*/
 	public void sendTrainingData(int cat, String text){
 		//This function sends to test.online_training table;
 		boolean opened = false;
 		try {
 			if(con==null){
 				openConnection(password);
 				opened = true;
 			}
 			Statement query = con.createStatement();
 			String statement = "insert into online_training  (cat, traintext) values ( " + cat + "," + text + ");";
 			query.executeUpdate(statement);
 			query.close();
 			if(opened){
 				con.close();
 			}
 		}catch(SQLException s){
 			System.out.println("Error adding training data to online_training"  );
 			System.out.println("SQLException: " + s.getMessage());
 		}
 		System.out.println("Trainined on " + text);
 		
 	}	
 
 
 	/**
 	*Constructs the training set
 	*@param results The ResultSet from querying the database
 	*/
 	public void constructTrainingDataSet(ResultSet results) throws Exception{
 		try{
 			//Read in the data
 			while(results.next()){
 				long id = Long.parseLong(results.getString(1));
 				String text = results.getString(5);
 
 				dataset.add(new Training_Tweet((int)id, text, NaiveBayes.convertBoolToInt(results.getBoolean(8))));
 
 			}
 		}catch(java.sql.SQLException jSQL){
 			//Do nothing
 			System.out.println(jSQL.getMessage());
 			System.out.println(jSQL.getStackTrace());
 		}finally{
 			//Cut off the results connection
 			results.close();
 		}
 		dataIter = dataset.iterator();
 	}
 
 	public void addTweet(String id, String nuId, String lat, String lon, String text, String created, String somethingelseishouldcontinueherelateron ){
 
 	}
 
 
 	/**
 	*Initalizes the data set to use the database and create an interface for the database.
 	*@return Returns true if initialization succeeded.
 	*/
 	public boolean initialize(String password) throws Exception{
 		try{
 			con = openConnection(password);
 			System.out.println("connect");
 			ResultSet data = getData();
 			System.out.println("got data");
 			constructTrainingDataSet(data);	
 			System.out.println("constructed");
 			close();
 			System.out.println("Disconnecting DataSet");
 		}catch(Exception e){
 			System.out.println("Exception: ");
 			System.out.println(e.getStackTrace() + " " + e.getMessage());
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	*Gets the next tweet in the dataset. If run out, we return null and recreate the iterator.
 	*@return the next Tweet in this dataset.
 	*/
 	public Tweet getNext(){
 		if(dataIter.hasNext()){
 			return dataIter.next();
 		}else{
 			dataIter = dataset.iterator();
 			return null;
 		}
 	}
 
 	/**
 	*Get the size of the data set
 	*/
 	public int size(){
 		return dataset.size();
 	}
 
 	public void close(){
 		try{ 
 			con.close();
 			con = null;
 		}catch(SQLException sql){
 			System.out.println("Problem closing the connection to the database");
 		}
 	}
 
 	public static void main(String[] args) {
 		//Command line parameter of password
 		if(args.length < 1){
 			System.out.println("Required parameter: Password");
 			System.exit(1);
 		}
 		DataSet d = new DataSet();
 		try{
 			d.initialize(args[0]);
 			Training_Tweet t = (Training_Tweet)d.getNext();
 			System.out.println(t.getTweetText());
 		}catch(Exception e ){
 			System.out.println("Exception");
 			System.out.println(e.getStackTrace());
 		}
 
 		
 		System.out.println(d.dataset.size());
 	}
 
 
 }
