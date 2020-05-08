 import java.io.BufferedReader;
 import java.io.DataInputStream;
 import java.io.FileInputStream;
 import java.io.InputStreamReader;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.Properties;
 
 /*
  * This class helps to read dataset in file and put it into Database
  * Beside that, it also gives more methods to interact with the Database 
  */
 
 public class Database {
 	
 	private String[] colName;
 	private int numOfRow;
 	private int numofCol;
 	private String username = "root";
 	private String pass = "123456";
 	private String databaseName = "decision_tree_demo_database";
 	private String dbms = "mysql";
 	private String serverName = "127.0.0.1";
 	private String portNumber = "3306";
 	private String tableName = "dataTable";
 	private String insertDataQuery = "";
         private Statement statement;
         private Connection connection;
 	private int count = 0;
 	
 	/*
 	 * To do: constructor
 	 */
 	public Database(String file) {
 		readFile(file);
                 connection = getConnection();
 	}
 
 	 
 	/*
 	 * To do: return a query answer
 	 */
 	public ResultSet query(String query) {
 		ResultSet set = null;
 		try {
 			//System.out.println("Execute the query: " + query);
 			statement = connection.createStatement();
 			statement.execute("use " + databaseName);
                         //System.out.println("Done!");
 			set = statement.executeQuery(query);
 		} catch (Exception e ) {
 			e.printStackTrace();
                         System.out.println(query);
 		}
 		return set;
 	}
         
         public void close_conection() {
 //          closeConnection(connection);
         }
         
 	public String[] getColName() {
 		return colName;
 	}
 
 	public void setColName(String[] colName) {
 		this.colName = colName;
 	}
 
 	public int getNumOfRow() {
 		return numOfRow;
 	}
 
 	public void setNumOfRow(int numOfRow) {
 		this.numOfRow = numOfRow;
 	}
 
 	public int getNumofCol() {
 		return numofCol;
 	}
 
 	public void setNumofCol(int numofCol) {
 		this.numofCol = numofCol;
 	}
 
 	public String getUsername() {
 		return username;
 	}
 
 	public void setUsername(String username) {
 		this.username = username;
 	}
 
 	public String getPass() {
 		return pass;
 	}
 
 	public void setPass(String pass) {
 		this.pass = pass;
 	}
 
 	public String getDatabaseName() {
 		return databaseName;
 	}
 
 	public void setDatabaseName(String databaseName) {
 		this.databaseName = databaseName;
 	}
 
 	public String getDbms() {
 		return dbms;
 	}
 
 	public void setDbms(String dbms) {
 		this.dbms = dbms;
 	}
 
 	public String getServerName() {
 		return serverName;
 	}
 
 	public void setServerName(String serverName) {
 		this.serverName = serverName;
 	}
 
 	public String getPortNumber() {
 		return portNumber;
 	}
 
 	public void setPortNumber(String portNumber) {
 		this.portNumber = portNumber;
 	}
 
 	public String getTableName() {
 		return tableName;
 	}
 
 	public void setTableName(String tableName) {
 		this.tableName = tableName;
 	}
 
 	/*
 	 * read record from file and put it into database
 	 */
 	private void readFile(String file) {
 		numOfRow = numofCol = 0;
 		int count = 0;
 		try {
 			FileInputStream fstream = new FileInputStream(file);	
 			DataInputStream in = new DataInputStream(fstream);
 			BufferedReader br = new BufferedReader(new InputStreamReader(in));
 			String strLine;
 			//Read File Line By Line
 			while ((strLine = br.readLine()) != null)   {
 				count++;
 				if(count == 2) {
 					readFileHeading(strLine);
 					createDatabase();
 					createInsertDataQuery();
 				}
 				else if(count > 2) {
 					numOfRow ++;
 					String[] record = readFileData(strLine);
 					addInsertQuery(record);				
 				}
 			}
 			writeData();
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	/*
 	 * read Heading of File, return number of Column and array of Column Names
 	 */
 	private void readFileHeading(String secondRow) {
 		numofCol = 1;
 		int begin = 0;
 		int end = 0;
 		
 		for(int i = 0; i < secondRow.length(); i++) {
 			if(secondRow.charAt(i) == ',') {
 				numofCol ++;
 			}
 		}
 		
 		colName = new String[numofCol];
 		
 		int count = 0;
 		for(int i = 0; i < secondRow.length(); i++) {
 			if(secondRow.charAt(i) == '<') {
 				begin = i;
 			} else if(secondRow.charAt(i) == '>') {
 				end = i;
 				colName[count] = secondRow.substring(begin + 1, end);
 				count ++;
 			} 
 		}
 	}
 	
 	/*
 	 * read data in File
 	 */
 	private String[] readFileData(String record) {
 		int begin = 0;
 		int end = 0;
 		String[] result = new String[numofCol];
 		
 		int count = 0;
 		for(int i = 0; i < record.length(); i++) {
 			if(record.charAt(i) == ',') {
 				end = i;
 				result[count] = record.substring(begin, end);
 				begin = end + 1;
 				count ++;
 			} else if (i == record.length() - 1) {
 				end = i;
 				result[count] = record.substring(begin, end + 1);
 			}
 		}
 		
 		return result;
 	}
 	
 	private void createInsertDataQuery() {
 		insertDataQuery += "insert into " + tableName + "(";
 		for(int i = 0; i < numofCol; i++) {
 			if(i != 0) insertDataQuery += ", ";
 			insertDataQuery += colName[i];
 		}
 		insertDataQuery += ") values";
 	}
 	
 	/*
 	 * To do: add data inserting query to Big Query.
 	 */
 	private void addInsertQuery (String[] data) {
 		count++;
 		if(count == 1) insertDataQuery += "\n(";
 		else insertDataQuery += ",\n(";
 		for(int i = 0; i < data.length; i++) {
 			if(i != 0) insertDataQuery += ", ";
 			insertDataQuery += "'" + data[i] + "'";	
 		}
 		insertDataQuery += ")";
 	}
 	
 	/*
 	 * To do: write data to database
 	 */
 	private void writeData() {
 		insertDataQuery += ";";
 		try {
 			Connection connection = getConnection();
 			Statement statement = connection.createStatement();
 			String query = "use " + databaseName;
 			statement.execute(query);
 			
 			statement.execute(insertDataQuery);
 			System.out.println("Write data successful!");
 			
 			closeConnection(connection);	
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	/*
 	 * To do: create Database
 	 */
 	private void createDatabase() {
 		try {
 		     Connection connection = getConnection();
 		     Statement statement = connection.createStatement();
 		     String query1 = "create database if not exists " + databaseName;
 		     statement.executeUpdate(query1);
 			 
                      String query2 = "use " + databaseName;
 		     statement.executeUpdate(query2);
 			 
                      String query3 = "drop table if exists " + tableName;
                      statement.executeUpdate(query3);
 
 
                      String query4 = createTableQuery();
                      statement.executeUpdate(query4);
                      System.out.println("Create Database successful!");
 			 
 			 closeConnection(connection);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 	}
 	
 	/*
 	 * To do: return SQL query to create data Table
 	 */
 	private String createTableQuery() {
 		String query = "create table if not exists " + tableName + "(";
 		for(int i = 0; i < numofCol; i++) {
 			if(i != 0) query += ",";
			query += colName[i] + "  DECIMAL(20,10)";
 		}
 		query += ")";
 		return query;
 	}
 	
 	/*
 	 * To do: Return connection
 	 */
 	private Connection getConnection() {
 		Connection conn = null;
 		try {
 			Properties connectionProps = new Properties();
 		    connectionProps.put("user", this.username);
 		    connectionProps.put("password", this.pass);
 
 		    if (this.dbms.equals("mysql")) {
 		        conn = DriverManager.getConnection(
 		                   "jdbc:" + this.dbms + "://" +
 		                   this.serverName +
 		                   ":" + this.portNumber + "/",
 		                   connectionProps);
 		    }
 		    
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		System.out.println("Connected to database!");
 		return conn;
 	}
 
 	private void closeConnection(Connection conn) {
 		try {
 			conn.close();
 			System.out.println("Close connection to database!");
 			System.out.println("");
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 	
 }
