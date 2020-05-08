 package database;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.lang.reflect.Field;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 /**
  * Verbindung zur Datenbank mit allen Modifikationsbefehlen.
  * 
  */
 public class DatabaseController {
 
 	/**
 	 * Variable for storing the instance of the class.
 	 */
 	private static DatabaseController instance;
 
 	/**
 	 * Verbingung zur Datenbank
 	 */
 	private Connection con;
 	/**
 	 * Statement zum ausfuerhen von SQL Befehlen
 	 */
 	private Statement st;
 	/**
 	 * Username fuer den Datenbankzugriff
 	 */
 	private String user;
 	/**
 	 * Passwort fuer den Datenbankzugriff
 	 */
 	private String password;
 	/**
 	 * Datenbankname
 	 */
 	private String database;
 	/**
 	 * Portnummer
 	 */
 	private String port;
 
 	/**
 	 * Method for getting a valid reference of this object.
 	 * 
 	 * @return Instance of DatabaseController.
 	 */
 	public static DatabaseController getInstance() {
 		if (instance == null)
 			instance = new DatabaseController();
 		return instance;
 	}
 
 	/**
 	 * Private constructor for DatabaseController for implementing the singleton
 	 * instance. Use getInstance() to get a reference to an object of this type.
 	 */
 	private DatabaseController() {
 		try {
 			System.out.println("[Database] Connecting to Database");
 			Class.forName("com.mysql.jdbc.Driver").newInstance();
 			getLoginInfo();
 			if (password != null)
 				con = DriverManager.getConnection("jdbc:mysql://localhost:"
 						+ port + "/" + database + "?user=" + user
 						+ "&password=" + password);
 			else
 				con = DriverManager.getConnection("jdbc:mysql://localhost:"
 						+ port + "/" + database + "?user=" + user);
 			st = con.createStatement();
 
 			System.out.println("[Database] Connection successful.");
 		} catch (Exception e) {
 			System.out
 					.println("[Database] Error while connecting to database: please check if DB is running and if logindata is correct (~/.sopraconf)");
 			// Commented out by Tamino (it was making me edgy... :D )
 			// e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Methode welche ein SQL "update" Statement ausfuehrt.
 	 * 
 	 * @param table
 	 * @param columns
 	 * @param values
 	 * @param where
 	 * @return
 	 */
 	synchronized public boolean update(String table, String[] columns,
 			Object[] values, String where) {
 		String update = "UPDATE " + table + " SET "
 				+ commanator(columns, values) + " WHERE " + where;
 		ResultSet rs;
 		try {
 			rs = st.executeQuery(update);
 			return true;
 		} catch (SQLException e) {
 			System.out.println("[DatabaseController] UPDATE error! " + update);
 			return false;
 		}
 	}
 
 	/**
 	 * Methode welche ein SQL "delete" Statement ausfuehrt.
 	 * 
 	 * @param table
 	 * 
 	 *            Tabelle von der geloescht werden soll
 	 * @param where
 	 *            Where Bedingung
 	 * @return boolean ob die Aktion fehlerfrei geklappt hat
 	 */
 	synchronized public boolean delete(String table, String where) {
 
 		String del = "DELETE FROM " + table + " WHERE " + where;
 		try {
 			st.executeUpdate(del);
 			return true;
 		} catch (SQLException e) {
 			System.out.println("[DatabaseController] DELETE error! " + del);
 			return false;
 		}
 	}
 
 	/**
 	 * Methode welche ein SQL "insert" Statement ausfuehrt.
 	 * 
 	 * @param table
 	 * @param values
 	 * @return
 	 */
 	synchronized public boolean insert(String table, Object[] values) {
 		String insert = "INSERT INTO " + table + " VALUES ("
 				+ commanator(values) + ")";
 		try {
 			st.executeUpdate(insert);
 			return true;
 		} catch (SQLException e) {
 			System.out.println("[DatabaseController] INSERT error!" + insert);
 			return false;
 		}
 	}
 
 	/**
 	 * Methode welche ein SQL "select" Statement ausfuehrt.
 	 * 
 	 * @param select
 	 * @param from
 	 * @param where
 	 * @return
 	 */
 	synchronized public ResultSet select(String[] select, String[] from,
 			String where) {
 		String sel = "SELECT " + commanator(select) + " FROM "
 				+ commanator(from) + " WHERE " + where;
 		ResultSet rs;
 		try {
 			rs = st.executeQuery(sel);
 			return rs;
 		} catch (SQLException e) {
 			System.out.println("[DatabaseController] SELECT error!" + sel);
 		}
 		return null;
 	}
 
 	/**
 	 * Methode welche ein SQL "insert on not null update" Statement ausfuehrt.
 	 * 
 	 * @param table
 	 * @param columns
 	 * @param values
 	 * @param where
 	 * @return
 	 */
 	synchronized public boolean insertOnNullElseUpdate(String table,
 			String[] columns, Object[] values, String where) {
 		String update = "INSERT INTO " + table + " VALUES ("
 				+ commanator(values) + ") ON DUPLICATE KEY UPDATE " + table
 				+ " SET " + commanator(columns, values) + " WHERE " + where;
 		ResultSet rs;
 		try {
 			rs = st.executeQuery(update);
 			return true;
 		} catch (SQLException e) {
 			System.out
 					.println("[DatabaseController] INSERTONNULLUPDATE error! "
 							+ update);
 		}
 		return false;
 	}
 
 	/**
 	 * Erstellt eine Datei mit Logininformationen für die Datenbank. In der
 	 * Datei werden der Usename,der Datenbankname und das Passwort gespeichert.
 	 * Dies wird benoetigt damit eine Verbindung mit der Datenbank unabhaengig
 	 * der Hardware hergestellt werden kann.
 	 * 
 	 */
 	private void createLoginInfo() {
 		String sep = System.getProperty("file.separator");
 		String home = System.getProperty("user.home");
 		File f = new File(home + sep + ".sopraconf");
 		f.delete(); // erst vorhandene löschen
 		try {
 			f.createNewFile(); // dann neue erstellen
 		} catch (IOException e) {
 			System.out
 					.println("[Database] createLoginInfo():f.createNewFile()");
 			e.printStackTrace();
 		}
 		try {
 			BufferedWriter buf = new BufferedWriter(new FileWriter(f));
 			buf.write("database=sopra");
 			buf.newLine();
 			buf.write("port=3306");
 			buf.newLine();
 			buf.write("user=root");
 			buf.close();
 		} catch (IOException e) {
 			System.out.println("[Database] createLoginInfo():BufferedWriter");
 			e.printStackTrace();
 		}
 		System.out.println("[Database] New loginfile created!");
 
 	}
 
 	/**
 	 * Liest die Informationen zum Verbindungsaufbau zur Datenbank aus einer
 	 * Config-Datei. Die Datei behinhaltet den Username,den Datenbanknamen und
 	 * das Passwort welche benoetigt werden um eine Verbindung zur Datenbank
 	 * herzustellen.
 	 * 
 	 */
 	private void getLoginInfo() { // Liest die Config Datei aus
 		String sep = System.getProperty("file.separator");
 		String home = System.getProperty("user.home");
 		try {
 			File f = new File(home + sep + ".sopraconf"); // datei im homeordner
 															// namens .sopraconf
 			if (f.exists()) { // falls config datei existiert
 				BufferedReader buf = new BufferedReader(new FileReader(f));
 				Field field;
 				database = "sopra";
 				user = "root";
 				port = "3306";
 				password = null;
 				while (true) {
 					if (!buf.ready())
 						break;
 					String conf = buf.readLine();
					if(!conf.toLowerCase().equals("password")&&!conf.toLowerCase().equals("user")&&!conf.toLowerCase().equals("database")&&!conf.toLowerCase().equals("port")){
						String[] confarr = conf.split("=");
						field = getClass().getDeclaredField(confarr[0].trim());
						field.set(this, confarr[1].trim());
					}else System.out.println("[DatabaseController] Error in ConfigFile!");			
				}
 			} else { // falls noch nicht existent
 				createLoginInfo(); // config datei erstellen
 				database = "sopra";
 				user = "root";
 				port = "3306";
 				password = null;
 			}
 			System.out.println("[Database] Try Login: user=" + user
 					+ " password=" + password + " database=" + database
 					+ " port=" + port);
 		} catch (Exception e) {
 			System.out.print("[Database] getLoginInfo()");
 			e.printStackTrace();
 
 		}
 	}
 
 	/**
 	 * Hilfsmethode zum Konkatenieren von Strings mit Kommasetzung.
 	 * 
 	 * @param stringz
 	 *            zu konkatenierende Strings
 	 * @return Konkatenierter String
 	 */
 	private String commanator(String[] stringz) {
 		String ret = "";
 		for (int i = 0; i < stringz.length; i++) {
 			ret += stringz[i];
 			if (i != (stringz.length - 1))
 				ret += ", ";
 		}
 		return ret;
 	}
 
 	/**
 	 * Hilfsmethode zum Konkatenieren von Objekten mit Kommasetzung. Dabei
 	 * werden Strings mit einfachen Anfuehrungszeichen eingeklammert.
 	 * 
 	 * @param objects
 	 *            Zu konkatenierende objekte.
 	 * @return Konkatenierter String
 	 */
 	private String commanator(Object[] objects) {
 		String ret = "";
 		for (int i = 0; i < objects.length; i++) {
 			if (objects[i] instanceof String)
 				ret += "'" + objects[i] + "'";
 			else
 				ret += objects[i];
 			if (i != (objects.length - 1))
 				ret += ", ";
 		}
 		return ret;
 	}
 
 	/**
 	 * Hilfsmethode zum konkatenieren von zwei String arrays fuer update
 	 * statements. Ergibt in etwa einen String der Form
 	 * "name[0]=value[0], name...".
 	 * 
 	 * @param name
 	 *            Namen der Spalten.
 	 * @param value
 	 *            Werte, welche die Spalten bekommen sollen.
 	 * @return String mit zusammengesetzten Inhalt.
 	 */
 	private String commanator(String[] name, Object[] value) {
 		String ret = "";
 		if (name.length != value.length)
 			return "ERROR IN COMMANATOR!";
 		for (int i = 0; i < name.length; i++) {
 			ret += name[i] + "=";
 			if (value[i] instanceof String)
 				ret += "'" + value[i] + "'";
 			else
 				ret += value[i];
 			if (i != (name.length - 1))
 				ret += ", ";
 		}
 		return ret;
 	}
 }
