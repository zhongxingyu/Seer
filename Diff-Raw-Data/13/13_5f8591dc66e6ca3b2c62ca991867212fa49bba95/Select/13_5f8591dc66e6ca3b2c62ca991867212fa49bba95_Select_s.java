 
 import java.sql.ResultSetMetaData;
 import java.sql.SQLException;
 
 /**
  * Used to make various selections and get information about the database or a specific table.
  * <br />Not suitable for counting as much as for obtaining data.
  * 
  * 
  * @author Daniel
  *
  */
 public class Select extends DataAccess {
 	
 	private static final long serialVersionUID = 3188564773946400482L;
 	
 	/**
 	 * Used to make various selections and get information about the database or a specific table.
 	 * <br />Not suitable for counting as much as for obtaining data.
 	 * 
 	 * @param dbName the url with the database name
 	 * @param user the username
 	 * @param pwd the password
 	 */
 	public Select(String dbName, String user, String pwd) {
 		super(dbName, user, pwd);
 	}
 	
 	/**
 	 * Returns the value of a field from a table. If more than one result, the first one is returned.
 	 * 
 	 * @param table
 	 * @param where
 	 * @param column
 	 * @return
 	 */
 	public String selectField(String table, String where, String column){
         String field=null;
         openConnection();
         try{
        	String myStatement = "SELECT "+column+" WHERE "+where+";";
 			statement = con.prepareStatement(myStatement);
 			result = statement.executeQuery();
			field = result.getString(0);
         }catch(SQLException s){
 			s.printStackTrace();
         }finally{
         	closeConnection();
         }
 		return field;
 	}
 	
 	//under development
 	public String[] selectValuesFromWhere(String table, String where) {
         String[] valuesList = null; //index 0 shows column number
         openConnection();
 		try{
 			String myStatement = "SELECT * FROM " + table;
 			
 			if (where=="") {
 				statement = con.prepareStatement(myStatement);
 			}else{
 				statement = con.prepareStatement(myStatement + " WHERE " + where); 
 			}
 			result = statement.executeQuery();
 			
 			int totalColumns = getTotalColumns(table);
 			valuesList = new String[totalColumns+1];
 			while(result.next()){
 				for (int i=1; i<totalColumns;i++) {
 					valuesList[i] = result.getString(i+1);
 				}
 			}
 			
 		}catch(SQLException e) {
 			e.printStackTrace();
 		}finally{
 			closeConnection();
 		}
 		return valuesList;
 	}
 	
 	/**
 	 * Returns the number of columns in a table.
 	 * 
 	 * @param table the table whose columns to count
 	 * @return total number of columns
 	 */
 	public int getTotalColumns(String table) {
         int numberOfColumns = 0;
         openConnection();
         try{
         	statement = con.prepareStatement("SELECT * FROM " + table);
         	result = statement.executeQuery();
         	ResultSetMetaData rsMetaData = result.getMetaData();
         	numberOfColumns = rsMetaData.getColumnCount();
         }catch(SQLException s){
         	s.printStackTrace();
         }finally{
         	closeConnection();
         }
         return numberOfColumns;
 	}
 	
 	/**
 	 * Returns an array with names of the columns in a given table.
 	 * 
 	 * @param table the table whose columns names are needed
 	 * @return a list of the columns in an array of strings
 	 */
 	public String[] getColumnsAsArray(String table) {
         String[] columnNames = null; //list of column names as an array of strings
         openConnection();
         try{
         	statement = con.prepareStatement("SELECT * FROM " + table);
         	result = statement.executeQuery();
         	ResultSetMetaData rsMetaData = result.getMetaData();
         	int numberOfColumns = rsMetaData.getColumnCount();
         	columnNames = new String[numberOfColumns+1];
         	
         	// get the column names; column indexes start from 1
         	for (int i = 1; i <= numberOfColumns; i++) {
         		String columnName = rsMetaData.getColumnName(i);
         	    // Get the name of the column's table name
         	    columnNames[i] = columnName;
         	}
         }catch(SQLException s){
         	s.printStackTrace();
         }finally{
         	closeConnection();
         }
         return columnNames;
 	}
 	
 }
