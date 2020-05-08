 package juva.database;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 import juva.database.Database;
 
 public class ModelProxy {
 	
 	public Database db;
 	protected Model model;
 	private boolean isDesc = false;
 	Map<String, String> _info = new HashMap<String, String>();
 	protected ArrayList<String[]> selectFilter = new ArrayList<String[]>();
 	
 
 	public ModelProxy(){
 	}
 	
 	public void setDatabase(Database database){
 		this.db = database;
 	}
 	
 	public void setModel(Model model)
 	        throws ClassNotFoundException, SQLException {
 		this.model = model;
 	}
 
 	public Database getDb(){
 		return db;
 	}
 	
 	public void connect() throws SQLException{
 		this.db.connect();
 	}
 	
 	public void close() throws SQLException{
 		this.db.closeConnection();
 	}
 	
 	public Model find(String id) throws Throwable{
 		Model output = this.model.getClass().newInstance();
 		output._table = this.model._table;
 		output.columns = this.model.columns;
 		this.addSelectFilter("id", id);
 		ArrayList columns = output.getColumnList();
 		ResultSet first = this.select();
 		if (first.next()){
 			for (int i=0;i<columns.size();++i){
 				Column column = (Column )columns.get(i);
 				String columnName = column.getName();
 				String columnValue = first.getString(columnName);
 				output.setValue(columnName, columnValue);
 			}
 		}
 		this.clearSelectFilter();
 		return output;
 	}
 
 	public int count() throws SQLException{
 		int total = 0;
 		ResultSet rs = this.select();
 		while (rs.next()){
 			total++;
 		}
 		return total;
 	}
 	
 	// Operation
 	
 	public void setDesc(){
 		this.isDesc = true;
 	}
 
 	public boolean isRecordExist() throws SQLException{
 		String table = this.model.getTable();
 		String primaryKey = this.model.getPrimaryKey();
 		String primaryValue = this.model.getValue(primaryKey);
 		String selectSql = ("SELECT " + primaryKey + " FROM " + table +
                            " WHERE " + primaryKey + "=" + primaryValue);
 		ResultSet validSet = this.db.statement.executeQuery(selectSql) ;
 		if (validSet.next()){
 			return true;
 		}
 		return false;
 	}
 
 	public void insert() throws SQLException{
 
 		String table = this.model.getTable();
 		String insertSql = "INSERT INTO " + table;
 		String columnsSectionSql = "(";
 		String valueSectionSql = "VALUES(";
 		ArrayList columns = this.model.getColumnList();
 		
 		for (int i=0;i<columns.size();++i){
 			Column currentColumn = (Column) columns.get(i);
 			String columnName = currentColumn.getName();
 			columnsSectionSql = (columnsSectionSql + columnName + ", ");
 			valueSectionSql = valueSectionSql + " ?, ";
 		}
 		
 		columnsSectionSql = removeComma(columnsSectionSql);
 		valueSectionSql = removeComma(valueSectionSql);
 		
 		columnsSectionSql = columnsSectionSql + ") ";
 		valueSectionSql = valueSectionSql + ") " ;
 		insertSql = insertSql + columnsSectionSql + valueSectionSql;
 		
 		this.db.preparedStatement =
             this.db.connection.prepareStatement(insertSql);
 		this.db.preparedStatement.clearParameters();
 		
 		for(int i=0;i<columns.size();++i){
 			Column currentColumn = (Column) columns.get(i);
 			String currentValue = this.model.getValue(currentColumn);
 			this.db.preparedStatement.setString(i+1, currentValue);
 		}
 
 		this.db.preparedStatement.executeUpdate();
 
 	}
 	
 	public void update() throws SQLException{
 		String table = this.model.getTable();
 		String updateSql = "UPDATE " + table + " SET ";
 		String primaryKey = this.model.getPrimaryKey();
 		String primaryValue = this.model.getValue(primaryKey);
 		ArrayList columns = this.model.getColumnList();
 		
 		for (int i=0;i<columns.size();++i){
 			Column currentColumn = (Column) columns.get(i);
 			String columnName = currentColumn.getName();
 			if (primaryKey == columnName){
 				continue;
 			}
 			updateSql = updateSql + columnName + "= ?, ";
 		}
 		
 		updateSql = removeComma(updateSql);
 		updateSql = updateSql + " WHERE " + primaryKey + " = ?";
 		
 		this.db.preparedStatement =
 			this.db.connection.prepareStatement(updateSql);
 		this.db.preparedStatement.clearParameters();
 		
 		int j = 0;
 		for (int i=0;i<columns.size();++i){
 			Column currentColumn = (Column) columns.get(i);
 			String columnName = currentColumn.getName();
 			if (primaryKey == columnName){
 				continue;
 			}
 			String currentValue = this.model.getValue(currentColumn);
 			this.db.preparedStatement.setString(j+1, currentValue);
 			j = j + 1;
 		}
 		
 		this.db.preparedStatement.setString(j+1, primaryValue);
 		this.db.preparedStatement.setString(columns.size(), primaryValue);
 		this.db.preparedStatement.executeUpdate();
 		
 	}
 	
 	public void addSelectFilter(String columnName, String columnValue){
 		this.addSelectFilter(columnName, columnValue, "=");
 	}
 	
 	public void addSelectFilter(String columnName,
                                  String columnValue,
                                  String operator){
 		Column column = this.model.getColumn(columnName);
 		if (this.model.isColumExsit(column)){
 			String[] tuple = {columnName, columnValue, operator};
 			selectFilter.add(tuple);
 		}		
 	}
 	
 	 public ResultSet select() throws SQLException{
 		 ArrayList list = this.model.getColumnList();
 		 Column[] all = new Column[list.size()];
 		 for (int i=0;i<list.size(); ++i){
 			 all[i] = (Column) list.get(i);
 		 }
 		 return this.select(all);
 	 }
 
 	public ResultSet select(Column[] columns) throws SQLException{
 
 		String table = this.model.getTable();
 		String selectSql = "SELECT ";
 		for (int i=0; i<columns.length; ++i){
 			Column column = columns[i];
 			selectSql = selectSql + column.getName() + ", ";
 		}
 		selectSql = removeComma(selectSql);
 		selectSql = selectSql + " FROM " + table + " WHERE ";
 		
 		for (int i=0;i<selectFilter.size();++i){
 			String[] columnInfo = selectFilter.get(i);
 			selectSql = selectSql + columnInfo[0] + " " +
 			            columnInfo[2] + " ? AND ";
 		}
 
 		selectSql = removeAndOrCmd(selectSql);
 		
 		if (isDesc){
 			selectSql += " ORDER BY id DESC";
 		}
 		
 		this.db.preparedStatement =
 			this.db.connection.prepareStatement(selectSql);
 		this.db.preparedStatement.clearParameters();
 
 		for (int i=0;i<selectFilter.size();++i){
 			String[] columnInfo = selectFilter.get(i);
 			String currentValue = columnInfo[1];
 			this.db.preparedStatement.setString(i+1, currentValue);
 		}
 		
 		ResultSet rs = this.db.preparedStatement.executeQuery();
 		return rs;
 	}
 	
 	public ResultSet querySelect(String[] columns, String sql, String[] param)
         throws SQLException{
 		String table = this.model.getTable();
 		String selectSql = "SELECT ";
 		for (int i=0; i<columns.length; ++i){
 			String column = columns[i];
 			selectSql = selectSql + column + ", ";
 		}
 		selectSql = removeComma(selectSql);
 		selectSql = selectSql + " FROM " + table + " WHERE ";
 		selectSql += sql;
 		this.db.preparedStatement = 
 			this.db.connection.prepareStatement(selectSql);
 		this.db.preparedStatement.clearParameters();
 		for (int i=0;i<param.length;++i){
 			this.db.preparedStatement.setString(i+1, param[i]);
 		}
 		ResultSet rs = this.db.preparedStatement.executeQuery();
 		return rs;
 	}
 	
 	private String removeComma(String input){
 		if (input.endsWith(",")){
 			input = input.substring(0, input.length() - 1);
 		}
 		if (input.endsWith(", ")){
 			input = input.substring(0, input.length() - 2);
 		}
 		return input;
 	}
 	
 	public String removeAndOrCmd(String input){
 		if (input.endsWith("OR")){
 			input = input.substring(0, input.length() - 2);
 		}
 		if (input.endsWith("AND") || input.endsWith("OR ")){
 			input = input.substring(0, input.length() - 3);
 		}
 		if (input.endsWith("AND ")){
 			input = input.substring(0, input.length() - 4);
 		}
 		return input;
 	}
 	
 	public void clearSelectFilter(){
 		this.selectFilter.clear();
 	}
 }
