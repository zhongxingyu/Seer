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
 	
 	public void removeTrainer(String trainerNumber) {
 		try {
 			stmt.execute("delete from trainer where t_id = " + trainerNumber);
 		} catch (SQLException e) {
 			displayError(e.getMessage(), SQLERROR);
 		}
 	}
 	
 	public void addTrainer(String user) {
 		try {
 			stmt.execute("insert into trainer (t_name) values ('" +
 					user + "');");
 		} catch (SQLException e) {
 			displayError(e.getMessage(), SQLERROR);
 		}
 	}
 	
 	public void addTrainer(String user, int hash) {
 		try {
 			stmt.execute("insert into trainer (t_name, hash) values ('" +
 					user + "', " + hash + ");");
 		} catch (SQLException e) {
 			displayError(e.getMessage(), SQLERROR);
 		}
 	}
 	
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
 	
 	public void updateTrainerNameWithID(Integer id, String newName) {
 		try {
 			stmt.execute("update trainer set t_name = '" + 
 					newName + "' where t_id = " + id + ";");
 		} catch (SQLException e) {
 			displayError(e.getMessage(), SQLERROR);
 		}
 	}
 
 	public void removePartyEntry(Integer party_id) {
 		try {
 			stmt.execute("delete from party where party_id = " + party_id + ";");
 		} catch (SQLException e) {
 			displayError(e.getMessage(), SQLERROR);
 		}
 	}
 	
 	public void addPokemonToTrainer(Integer national_id, Integer user) {
 		try {
 			stmt.execute("insert into party (t_id, national_id)" +
 					" values (" + user + ", " + national_id + ");");
 		} catch (SQLException e) {
 			displayError(e.getMessage(), SQLERROR);
 		}
 	}
 	
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
 	
 	public void connectToRIT(String user, String pass) {
 		try {
 			this.establishConnection(url, user, pass);
 			this.createStatement();
 		} catch (SQLException e) {
 			displayError(e.getMessage(), SQLERROR);
 		}
 	}
 	
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
 	
 	
 	public ArrayList<ArrayList<Object>> iterate(ResultSet rs) {
 		ArrayList<Object> columnNames = new ArrayList<Object>();
 		ArrayList<ArrayList<Object>> data = new ArrayList<ArrayList<Object>>();
 		try {
 			ResultSetMetaData metaData = rs.getMetaData();
 
 			// names of columns
 
 			int columnCount = metaData.getColumnCount();
 			for (int column = 1; column <= columnCount; column++) {
 				columnNames.add(metaData.getColumnName(column));
 			}
 
 			// data of the table
 
 			while (rs.next()) {
 				ArrayList<Object> vector = new ArrayList<Object>();
 				for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
 					vector.add(rs.getObject(columnIndex));
 				}
 				data.add(vector);
 			}
 		} catch (SQLException e) {
 //			System.err.println(e.getMessage());
 			displayError(e.getMessage(), SQLERROR);
 		} catch (NullPointerException e) {
 			displayError(e.getMessage(), SQLERROR);
 		}
 		return data;
 	}
 	
 	/**
 	 * 
 	 * @param rs
 	 * @return
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
 //			System.err.println(e.getMessage());
 			displayError(e.getMessage(), SQLERROR);
 		} catch (NullPointerException e) {
 			error = true;
 		}
 		return error ? new DefaultTableModel() : new DefaultTableModel(data, columnNames);
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
 	
 	public ArrayList<String> getPokevolveQuery(String search){
 		ArrayList<String> queryData = new ArrayList<String>();
		if(search.matches("[a-zA-Z]+") == false){
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
 	
 	public ArrayList<String> getExpQuery(String search){
 		ArrayList<String> queryData = new ArrayList<String>();
 		try{
 			ResultSet rs = stmt.executeQuery("select * from v_exp_group where english ilike '%" + search + "%';");
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
 	
 	public ArrayList<String> getMetricsQuery(String search){
 		ArrayList<String> queryData = new ArrayList<String>();
 		try{
 			ResultSet rs = stmt.executeQuery("select * from v_base_stats where name ilike '%" + search + "%';");
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
 	public ArrayList<String> getPokedexQuery(String search){
 		ArrayList<String> queryData = new ArrayList<String>();
 		try{
 			ResultSet rs = stmt.executeQuery("select * from v_pokedex where name ilike '%" + search + "%';");
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
 	 * tests queries
 	 * @param args	command line arguments
 	 * 		args[0]	username
 	 * 		args[1] password
 	 */
 	public static void main(String[] args) {
 		String user = "";
 		String pass = "";
 
 		try {
 			if(args.length < 2) {	
 				InputStreamReader inReader = new InputStreamReader(System.in);
 				BufferedReader br = new BufferedReader(inReader);
 				System.out.print("Enter user id: ");
 				user = br.readLine();
 				System.out.print("This is bad, but may I have your password? - ");
 				pass = br.readLine();
 
 			} else {
 				user = args[0];
 				pass = args[1];
 			}
 		} catch(IOException e) {
 			System.err.println(e.getMessage());
 			return;
 		}
 
 		DataFetch df = DataFetch.getInstance();
 		try {
 			df.establishConnection(url, user, pass);
 			df.createStatement();
 			df.executeAndPrintTestQuery();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 
 	}
 
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
 	
 	public TableModel getMovesPokemonModel(String search){
 		boolean error = false;
 		ResultSet rs = null;
 		try{
 			rs = stmt.executeQuery("select level, move from v_level_moves where name ilike '%" + search + "%';");
 		}catch (SQLException e){
 			error = true;
 //			System.err.println(e.getMessage());
 			displayError(e.getMessage(), SQLERROR);
 		}
 		return error ? new DefaultTableModel() : buildTableModel(rs);
 	}
 	public TableModel getMetricsPokemonModel(String search){
 		boolean error = false;
 		ResultSet rs = null;
 		try{
 			rs = stmt.executeQuery("select hp, atk, def, spatk, spdef, spd from v_base_stats where name ilike '%" + search + "%';");
 		}catch (SQLException e){
 			error = true;
 //			System.err.println(e.getMessage());
 			displayError(e.getMessage(), SQLERROR);
 		}
 		return error ? new DefaultTableModel() : buildTableModel(rs);
 	}
 	
 	public TableModel getSimplifiedDefaultPokemonModel(){
 		boolean error = false;
 		ResultSet rs = null;
 		try {
 			rs = stmt.executeQuery("select national_id, english from name_both_types order by national_id;");
 		} catch (SQLException e) {
 			error = true;
 //			System.err.println(e.getMessage());
 			displayError(e.getMessage(), SQLERROR);
 		}
 		return error ? new DefaultTableModel() : buildTableModel(rs);
 	}
 	
 	public TableModel getDefaultPokemonModel() {
 		boolean error = false;
 		ResultSet rs = null;
 		try {
 			rs = stmt.executeQuery("select * from name_both_types order by national_id;");
 		} catch (SQLException e) {
 			error = true;
 //			System.err.println(e.getMessage());
 			displayError(e.getMessage(), SQLERROR);
 		}
 		return error ? new DefaultTableModel() : buildTableModel(rs);
 	}
 
 
 	
 }
