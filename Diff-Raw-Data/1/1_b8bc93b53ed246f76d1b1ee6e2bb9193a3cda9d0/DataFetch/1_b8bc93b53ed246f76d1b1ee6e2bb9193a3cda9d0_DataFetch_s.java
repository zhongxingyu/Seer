 /**
  * DataFetch.java
  * 
  */
 package data;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.Vector;
 import javax.swing.JOptionPane;
 import javax.swing.table.DefaultTableModel;
 import javax.swing.table.TableModel;
 
 import view.GUIEntryPoint;
 
 
 /**
  * This class will be used for the majority of Database IO
  * @author JimiHFord jhf3617
  */
 public class DataFetch {
 
 	/*
 	 * static data
 	 */
 	private static final String driver = "org.postgresql.Driver"; 
 	private static final String url = "jdbc:postgresql://reddwarf.cs.rit.edu/";
 	private static final String SQLERROR = "SQLException";
 	
 	/*
 	 * instance variables
 	 */
 	private Statement stmt;
 	private Connection con;
 	private GUIEntryPoint listener;
 
 	/**
 	 * Singleton Wrapper class 
 	 * @author JimiHFord jhf3617
 	 *
 	 */
 	private static class SingletonWrapper {
 		private static DataFetch INSTANCE = new DataFetch();
 	}
 
 	/**
 	 * Use as an alternative to a constructor. This will ensure singleton
 	 * behavior
 	 * @return	the singleton instance of DataFetch
 	 */
 	public static DataFetch getInstance() {
 		return SingletonWrapper.INSTANCE;
 	}
 
 	/**
 	 * Private constructor ensures no extraneous DataFetchers will be created
 	 */
 	private DataFetch() {
 		listener = null;
 	}
 
 	/**
 	 * Sets the "listener" for error messages to be displayed
 	 * @param listener	the component to be updated with error messages
 	 */
 	public void setListener(GUIEntryPoint listener) {
 		if(this.listener == null) {
 			this.listener = listener;
 		} else {
 			System.err.println("Listener has already been set.");
 		}
 	}
 
 	/**
 	 * returns the main trainer model for the poke party panel
 	 * @return
 	 */
 	public DefaultTableModel getMainTrainerTableModel() {
 		boolean error = false;
 		ResultSet rs = null;
 		try {
 			rs = stmt.executeQuery("select t_id, t_name from trainer order by t_id;");
 		} catch (SQLException e) {
 			error = true;
 			displayError(e.getMessage(), SQLERROR);
 		}
 		return error ? new DefaultTableModel() : buildTableModel(rs);
 	}
 	
 	/**
 	 * removes the trainer from the database
 	 * @param trainerNumber the unique trainer ID to remove from database
 	 */
 	public void removeTrainer(String trainerNumber) {
 		try {
 			stmt.execute("delete from trainer where t_id = " + trainerNumber);
 		} catch (SQLException e) {
 			displayError(e.getMessage(), SQLERROR);
 		}
 	}
 	
 	/**
 	 * adds trainer to the database
 	 * @param user username
 	 * @param hash password
 	 */
 	public void addTrainer(String user, int hash) {
 		try {
 			stmt.execute("insert into trainer (t_name, hash) values ('" +
 					user + "', " + hash + ");");
 		} catch (SQLException e) {
 			displayError(e.getMessage(), SQLERROR);
 		}
 	}
 	
 	/**
 	 * 
 	 * @param id id of trainer
 	 * @return trainer name associated with that id
 	 */
 	public String getTrainerNameFromID(Integer id) {
 		ResultSet rs = null;
 		String userName = new String();
 		try {
 			rs = stmt.executeQuery("select t_name from trainer where " +
 					"t_id = " + id + ";");
 			if(rs.next()) {
 				userName = rs.getString(1);
 			}
 		} catch (SQLException e){
 			displayError(e.getMessage(), SQLERROR);
 		}
 		return userName;
 	}
 	
 	/**
 	 * 
 	 * @param id id of trainer whose name is to be updated
 	 * @param newName new name of the trainer
 	 */
 	public void updateTrainerNameWithID(Integer id, String newName) {
 		try {
 			stmt.execute("update trainer set t_name = '" + 
 					newName + "' where t_id = " + id + ";");
 		} catch (SQLException e) {
 			displayError(e.getMessage(), SQLERROR);
 		}
 	}
 
 	/**
 	 * 
 	 * @param party_id id of party to be removed
 	 */
 	public void removePartyEntry(Integer party_id) {
 		try {
 			stmt.execute("delete from party where party_id = " + party_id + ";");
 		} catch (SQLException e) {
 			displayError(e.getMessage(), SQLERROR);
 		}
 	}
 	
 	/**
 	 * 
 	 * @param national_id national id of Pokemon to add
 	 * @param user id of trainer whose party the Pokemon is to be added to
 	 */
 	public void addPokemonToTrainer(Integer national_id, Integer user) {
 		try {
 			stmt.execute("insert into party (t_id, national_id)" +
 					" values (" + user + ", " + national_id + ");");
 		} catch (SQLException e) {
 			displayError(e.getMessage(), SQLERROR);
 		}
 	}
 	
 	/**
 	 * 
 	 * @param user id of the trainer
 	 * @return the model for the JTable
 	 */
 	public DefaultTableModel getTeamPanelModel(Integer user) {
 		boolean error = false;
 		ResultSet rs = null;
 		try {
 			rs = stmt.executeQuery(
 					"select "+
 			"party_id, national_id, english, type1, type2" +
 			" from explicit_party where t_id = "
 					+ user + " order by party_id;");
 		} catch (SQLException e) {
 			error = true;
//			System.err.println(e.getMessage());
 			displayError(e.getMessage(), SQLERROR);
 		}
 		return error ? new DefaultTableModel() : buildTableModel(rs);
 	}
 	
 	/**
 	 * 
 	 * @param user id of trainer
 	 * @return linked list of all the types of the Pokemon on the team
 	 */
 	public LinkedList<String> getTypesOnTeam(int user) {
 		boolean error = false;
 		ResultSet rs = null;
 		LinkedList<String> result = new LinkedList<String>();
 		try {
 			rs = stmt.executeQuery("" +
 					"select type1, type2" +
 					" from explicit_party where t_id = " + user +
 					" order by party_id;");
 			String temp1 = "";
 			String temp2 = "";
 			while(rs.next()) {
 				temp1 = rs.getString("type1");
 				result.add(temp1);
 				temp2 = rs.getString("type2");
 				if(!temp1.equals(temp2)) {
 					result.add(temp2);
 				}
 			}
 		} catch(SQLException e) {
 			error = true;
 			displayError(e.getMessage(), SQLERROR);
 		}
 		return error ? new LinkedList<String>() : result;
 	}
 	
 	/**
 	 * 
 	 * @param user username
 	 * @param pass password
 	 */
 	public void connectToRIT(String user, String pass) {
 		try {
 			this.establishConnection(url, user, pass);
 			this.createStatement();
 		} catch (SQLException e) {
 			displayError(e.getMessage(), SQLERROR);
 		}
 	}
 	
 	/**
 	 * 
 	 * @param t_id id of trainer who is logging in
 	 * @param hash password of said trainer
 	 * @return true if the trainer logged in successfully
 	 */
 	public boolean login(Integer t_id, Integer hash) {
 		boolean loginSuccess = false;
 		ResultSet rs = null;
 		try {
 			rs = stmt.executeQuery("select hash from trainer where t_id = " + t_id + ";");
 			if(rs.next()) {
 				Integer dbHash = rs.getInt(1);
 				loginSuccess = dbHash.equals(hash);
 			}
 		} catch (SQLException e) {
 			displayError(e.getMessage(), SQLERROR);
 		}
 		return loginSuccess;
 	}
 	
 	/**
 	 * 
 	 * @param rs result set to make table model of
 	 * @return table model of the result set
 	 */
 	public DefaultTableModel buildTableModel(ResultSet rs) {
 		boolean error = false;
 		Vector<String> columnNames = new Vector<String>();
 		Vector<Vector<Object>> data = new Vector<Vector<Object>>();
 		try {
 			ResultSetMetaData metaData = rs.getMetaData();
 
 			// names of columns
 
 			int columnCount = metaData.getColumnCount();
 			for (int column = 1; column <= columnCount; column++) {
 				columnNames.add(metaData.getColumnName(column));
 			}
 
 			// data of the table
 
 			while (rs.next()) {
 				Vector<Object> vector = new Vector<Object>();
 				for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
 					vector.add(rs.getObject(columnIndex));
 				}
 				data.add(vector);
 			}
 		} catch (SQLException e) {
 			error = true;
 			displayError(e.getMessage(), SQLERROR);
 		} catch (NullPointerException e) {
 			error = true;
 		}
 		return error ? new DefaultTableModel() : new DefaultTableModel(data, columnNames){
 			@Override
 			public boolean isCellEditable(int row, int col){
 				return false;
 			}
 		};
 	}
 
 	/**
 	 * Displays the error message that occurred
 	 * @param msg 	the message 
 	 * @param title	the title of the error message
 	 */
 	private void displayError(String msg, String title) {
 		if(listener == null) {
 			JOptionPane.showMessageDialog(null, msg, title,
 					JOptionPane.ERROR_MESSAGE, null);
 		} else {
 			listener.showError(msg, title);
 		}
 	}
 
 
 	/**
 	 * 
 	 * @param url	full url to database including database name (not needed though)
 	 * @param user	username of the psql account
 	 * @param pass	that user's password
 	 * @throws SQLException				if something is wrong with the connection
 	 * @throws ClassNotFoundException	if the driver for psql can not be found
 	 */	
 	public void establishConnection(String url, String user, String pass) 
 			throws SQLException {
 		try {
 			Class.forName(driver);
 		} catch (ClassNotFoundException e) {
 			displayError(e.getMessage(), "ClassNotFoundExcpetion");
 		}
 		this.con = DriverManager.getConnection(url + user, user, pass);
 	}
 
 	/**
 	 * Creates a statement for the DataFetch object to execute queries with.
 	 * @throws SQLException
 	 */
 	public void createStatement() throws SQLException {
 		this.stmt = con.createStatement();
 	}
 
 	/**
 	 * @return the statement that manages queries
 	 */
 	public Statement getStatement() {
 		return this.stmt;
 	}
 
 	/**
 	 * Tests the connection of our database
 	 * @throws SQLException
 	 */
 	public void executeAndPrintTestQuery() throws SQLException {
 		ResultSet rs = stmt.executeQuery("select * from pokemon_name;");
 		while(rs.next()) {
 			System.out.println("Pokemon ID: " + rs.getInt("national_id") + '\t' + rs.getString("english"));
 		}
 	}
 	
 	/**
 	 * 
 	 * @param query query to execute to get the Pokedex data
 	 * @return Pokedex data requested by the query
 	 */
 	public String getDexEntry(String query){
 		String result = "";
 		try{
 			ResultSet rs = stmt.executeQuery(query);
 			while(rs.next()){
 				result = rs.getString(1);	
 			}
 		}catch(SQLException e){
 			displayError(e.getMessage(), "SQL Exception");
 		}
 		return result;
 	}
 	
 	/**
 	 * 
 	 * @param search national id of Pokemon whose evolution data is to be returned
 	 * @return evolution data of a Pokemon
 	 */
 	public ArrayList<String> getPokevolveQuery(String search){
 		ArrayList<String> queryData = new ArrayList<String>();
 		if(search.matches("[a-zA-Z. 2-]+") == false){
 			try{
 				ResultSet rs = stmt.executeQuery("select * from v_evolution_data where national_id = " + search + ";");
 				while(rs.next()){
 					for(int i = 1; i <= rs.getMetaData().getColumnCount(); i++){
 						queryData.add(rs.getString(i));
 					}
 				}
 			}catch(SQLException e){
 				displayError(e.getMessage(), "SQL Exception");
 			}
 		}else{
 			try{
 				ResultSet rs = stmt.executeQuery("select * from v_evolution_data where name ilike '" + search + "';");
 				while(rs.next()){
 					for(int i = 1; i <= rs.getMetaData().getColumnCount(); i++){
 						queryData.add(rs.getString(i));
 					}
 				}
 			}catch(SQLException e){
 				displayError(e.getMessage(), "SQL Exception");
 			}
 		}
 		return queryData;
 	}
 	
 	/**
 	 * 
 	 * @param search name of Pokemon to get ExpGroup of
 	 * @return Exp Group data of Pokemon
 	 */
 	public ArrayList<String> getExpQuery(String search){
 		ArrayList<String> queryData = new ArrayList<String>();
 		try{
 			ResultSet rs = stmt.executeQuery("select * from v_exp_group where english ilike '" + search + "';");
 			while(rs.next()){
 				for(int i = 1; i <= rs.getMetaData().getColumnCount(); i++){
 					queryData.add(rs.getString(i));
 				}
 			}
 		}catch(SQLException e){
 			displayError(e.getMessage(), "SQL Exception");
 		}
 		return queryData;
 	}
 	
 	/**
 	 * 
 	 * @param search name of Pokemon to search
 	 * @return base stats and other metrics of the Pokemon
 	 */
 	public ArrayList<String> getMetricsQuery(String search){
 		ArrayList<String> queryData = new ArrayList<String>();
 		try{
 			ResultSet rs = stmt.executeQuery("select * from v_base_stats where name ilike '" + search + "';");
 			while(rs.next()){
 				for(int i = 1; i <= rs.getMetaData().getColumnCount(); i++){
 					queryData.add(rs.getString(i));
 				}
 			}
 		}catch(SQLException e){
 			displayError(e.getMessage(), "SQL Exception");
 		}
 		return queryData;
 	}
 	
 	/**
 	 * 
 	 * @param search name of Pokemon to search 
 	 * @return Pokedex entries of Pokemon
 	 */
 	public ArrayList<String> getPokedexQuery(String search){
 		ArrayList<String> queryData = new ArrayList<String>();
 		try{
 			ResultSet rs = stmt.executeQuery("select * from v_pokedex where name ilike '" + search + "';");
 			while(rs.next()){
 				for(int i = 1; i <= rs.getMetaData().getColumnCount(); i++){
 					queryData.add(rs.getString(i));
 				}
 			}
 		} catch(SQLException e){
 			displayError(e.getMessage(), "SQL Exception");
 		}
 		return queryData;
 	}
 	
 	/**
 	 * 
 	 * @param search name/type/id number to search
 	 * @return table of search results for the home screen
 	 */
 	public TableModel getSearchPokemonModel(String search) {
 		boolean error = false;
 		ResultSet rs = null;
 		try {
 			rs = stmt.executeQuery(
 					"select * from name_both_types where english ilike '"+
 					search + "%' union "+
 					"select * from name_both_types where type1 ilike '"+
 					search + "%' union "+
 					"select * from name_both_types where type2 ilike '"+
 					search + "%' union select * from name_both_types where " +
 					"cast(national_id as text) like '" + search + "%' " +
 							"order by national_id;"
 					);
 		} catch (SQLException e) {
 			error = true;
 			displayError(e.getMessage(), "SQLError");
 		}
 		return error ? new DefaultTableModel() : buildTableModel(rs);
 	}
 	
 	/**
 	 * 
 	 * @param search name/id of Pokemon to search
 	 * @return table of search results for all screens aside from the home screen
 	 */
 	public TableModel getSimplifiedSearchPokemonModel(String search) {
 		boolean error = false;
 		ResultSet rs = null;
 		try {
 			rs = stmt.executeQuery(
 					"select national_id, english from name_both_types where english ilike '"+
 					search + "%' union select national_id, english from name_both_Types where " +
 							"cast(national_id as text) like '" + search + "%' order by national_id;"
 					);
 		} catch (SQLException e) {
 			error = true;
 			displayError(e.getMessage(), "SQLError");
 		}
 		return error ? new DefaultTableModel() : buildTableModel(rs);
 	}
 	
 	/**
 	 * 
 	 * @param search name of Pokemon to search
 	 * @return table of Pokemon's moveset
 	 */
 	public TableModel getMovesPokemonModel(String search){
 		boolean error = false;
 		ResultSet rs = null;
 		try{
 			rs = stmt.executeQuery("select level, move from v_level_moves where name ilike '" + search + "';");
 		}catch (SQLException e){
 			error = true;
 			displayError(e.getMessage(), SQLERROR);
 		}
 		return error ? new DefaultTableModel() : buildTableModel(rs);
 	}
 	
 	/**
 	 * 
 	 * @param search name of Pokemon to search
 	 * @return table of base stats of Pokemon
 	 */
 	public TableModel getMetricsPokemonModel(String search){
 		boolean error = false;
 		ResultSet rs = null;
 		try{
 			rs = stmt.executeQuery("select hp, atk, def, spatk, spdef, spd from v_base_stats where name ilike '" + search + "';");
 		}catch (SQLException e){
 			error = true;
 			displayError(e.getMessage(), SQLERROR);
 		}
 		return error ? new DefaultTableModel() : buildTableModel(rs);
 	}
 	
 	/**
 	 * 
 	 * @return table of national id and name of all Pokemon
 	 */
 	public TableModel getSimplifiedDefaultPokemonModel(){
 		boolean error = false;
 		ResultSet rs = null;
 		try {
 			rs = stmt.executeQuery("select national_id, english from name_both_types order by national_id;");
 		} catch (SQLException e) {
 			error = true;
 			displayError(e.getMessage(), SQLERROR);
 		}
 		return error ? new DefaultTableModel() : buildTableModel(rs);
 	}
 	
 	/**
 	 * 
 	 * @return table of national id, name, and both types of all Pokemon
 	 */
 	public TableModel getDefaultPokemonModel() {
 		boolean error = false;
 		ResultSet rs = null;
 		try {
 			rs = stmt.executeQuery("select * from name_both_types order by national_id;");
 		} catch (SQLException e) {
 			error = true;
 			displayError(e.getMessage(), SQLERROR);
 		}
 		return error ? new DefaultTableModel() : buildTableModel(rs);
 	}
 
 }
