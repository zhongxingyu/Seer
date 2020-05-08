 /**
  * Data base class.
  * Functionality:
  * 1) contains list of tables
  * 2) provide elementary manipulations: add, delete, get.
  * 3) provide operations: union, intersection
  * 
  * @author:			Denis Panchenko, Tanya Pushchalo
  * Written:			20/10/2013
  * Last Updated: 	20/10/2013
  */
 
 package core;
 
 import java.util.*;
 
 public class DataBase {
 	private String _dbName;
 	
 	private ArrayList<Table> _tableList; // list of tables	
 	
 	/**
 	 * Add a new table to the database
 	 * @return void
 	 */
 	public void addTable(Table table) {
 
 	}
 	
 	/**
 	 * Delete table from database.
 	 * Table is specified by id in table list. 
 	 * Throws wrong argument exception if gets an id
 	 * of non-existing table.
 	 * Returns false in case of successfully completed
 	 * @throws WrongArgumentException if id is not exists
 	 * @return boolean
 	 */
 	public boolean deleteTable(Integer id) {
 		boolean result = false;
 		
 		return result;
 	}
 	
 	/**
 	 * Returns number of tables in database
 	 * @return Integer - quantity of tables
 	 */
 	public Integer getNumOfTables() {
 		return _tableList.size();
 	};
 	
 	/**
 	 * Returns the list of tables 
 	 * @return ArrayList<Table> - list of tables in database
 	 */
 	public ArrayList<Table> getTableList() {
 		return _tableList;
 	};
 	
 	public DataBase(){
		_dbName = "New database ";
 	}
 	
 	public DataBase(String name){
 		if(name != null)
 			_dbName = name;
 		else
 			_dbName = "New database";
 	}
 }
