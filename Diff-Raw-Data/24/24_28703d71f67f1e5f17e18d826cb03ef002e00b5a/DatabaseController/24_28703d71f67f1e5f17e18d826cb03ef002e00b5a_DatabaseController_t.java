 /**
  * @author Manuel Guentzel
  * @author Tamino Hartmann
  * @author Patryk Boczon
  */
 
 // TODO: Is sql.date format handled correctly throughout?
 
 package database;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.Date;
 import java.util.Vector;
 
 import logger.Log;
 import config.Configurator;
 import database.garbage.GarbageCollector;
 
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
 	 * Variable welche auf den Logger zeigt.
 	 */
 	private Log log;
 
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
 		// Get & and save logger:
 		this.log = Log.getInstance();
 		log.write("DatabaseController", "Instance created.");
 		// Connect to database:
 		generateStandardConfig();
 		Configurator conf = Configurator.getInstance();
 		conf.refresh();
 		try {
 			log.write("DatabaseController", "Connecting to Database");
 			Class.forName("com.mysql.jdbc.Driver").newInstance();
 			// getLoginInfo();
 			user = conf.getString("user");
 			password = conf.getString("password");
 			database = conf.getString("database");
 			port = Integer.toString(conf.getInt("port"));
 
 			if (password != null) {
 				log.write("DatabaseController", "Try login: "
 						+ "jdbc:mysql://localhost:" + port + "/" + database
 						+ "?user=" + user + "&password=" + password);
 				con = DriverManager.getConnection("jdbc:mysql://localhost:"
 						+ port + "/" + database + "?user=" + user
 						+ "&password=" + password);
 			} else {
 				log.write("DatabaseController", "Try login: "
 						+ "jdbc:mysql://localhost:" + port + "/" + database
 						+ "?user=" + user);
 				con = DriverManager.getConnection("jdbc:mysql://localhost:"
 						+ port + "/" + database + "?user=" + user);
 			}
 			st = con.createStatement();
 
 			log.write("DatabaseController", "Connection successful.");
 			// falls kein default institut existiert wird dies nun erstellt.
 			generateDefaultInstitute();
 			// falls kein admin account existiert wird nun einer erstellt.
 			generateAdminAccount();
			generateDefaultDefaults();
 		} catch (Exception e) {
 			log.write(
 					"DatabaseController",
 					"Error while connecting to database: please check if DB is running and if logindata is correct (~/.sopra/sopraconf)");
 			// Commented out by Tamino (it was making me edgy... :D )
 			// e.printStackTrace();
 		}
 		GarbageCollector.getInstance().start();
 	}
 
 	/**
 	 * Methode welche ein SQL "update" Statement ausfuehrt.
 	 * 
 	 * @param table
 	 *            Name der Tabelle.
 	 * @param columns
 	 *            Name der Spalten welche aktualisiert werden sollen.
 	 * @param values
 	 *            Daten, welche in die entsprechenden Spalten gefuehlt werden
 	 *            sollen.
 	 * @param where
 	 *            Bedingung fuer die Aktualisierung.
 	 * @return <code>True</code> wenn erfolgreich, sonst <code>false</code>.
 	 */
 	synchronized public boolean update(String table, String[] columns,
 			Object[] values, String where) {
 		// Sicherheitsüberprüfung:
 		if (con == null) {
 			log.write("DatabaseController", "No instance of CON detected!");
 			return false;
 		}
 		String update = "UPDATE " + table + " SET "
 				+ commanator(columns, values) + " WHERE " + where;
 		try {
 			st.executeUpdate(update);
 			return true;
 		} catch (SQLException e) {
 			log.write("DatabaseController", "UPDATE error! <" + update + ">");
 			e.printStackTrace();
 			return false;
 		}
 	}
 
 	/**
 	 * Methode welche ein SQL "delete" Statement ausfuehrt.
 	 * 
 	 * @param table
 	 *            Tabelle aus der ein Eintrag bzw. mehrere Eintraege geloescht werden soll.
 	 * @param where
 	 *            'WHERE' Bedingung.
 	 * @return boolean Bei TRUE erfolgreich ausgefuehrt. Sonst FALSE.
 	 */
 	synchronized public boolean delete(String table, String where) {
 		// Sicherheitsüberprüfung:
 		if (con == null) {
 			log.write("DatabaseController", "No instance of CON detected!");
 			return false;
 		}
 		String del = "DELETE FROM " + table + " WHERE " + where;
 		try {
 			st.executeUpdate(del);
 			return true;
 		} catch (SQLException e) {
 			log.write("DatabaseController", "DELETE error! <" + del + ">");
 			// e.printStackTrace();
 			return false;
 		}
 	}
 
 	/**
 	 * Methode welche ein SQL "insert" Statement ausfuehrt.
 	 * 
 	 * @param table
 	 *            Name der Tabelle.
 	 * @param values
 	 *            Einzufuegende Werte.
 	 * @return boolean Bei TRUE erfolgreich ausgefuehrt. Sonst FALSE.
 	 */
 	synchronized public boolean insert(String table, Object[] values) {
 		// Sicherheitsüberprüfung:
 		if (con == null) {
 			log.write("DatabaseController", "No instance of CON detected!");
 			return false;
 		}
 		String insert = "INSERT INTO " + table + " VALUES ("
 				+ commanator(values) + ")";
 		try {
 			st.executeUpdate(insert);
 			return true;
 		} catch (SQLException e) {
 			log.write("DatabaseController", "INSERT error! <" + insert + ">");
 			// Commented out because this error happens often and should be
 			// handled further up.
 			// e.printStackTrace();
 			return false;
 		}
 	}
 
 	/**
 	 * Gibt die Anzahl der Zeilen einer Tabelle aus, die die "Where"- Bedingung
 	 * erfuellen
 	 * 
 	 * @param from
 	 *            Tabellen, die in die Suche miteinbezogen werden sollen
 	 * @param where
 	 *            Zu erfuellende Bedingung
 	 * @return Anzahl der Zeilen
 	 */
 	synchronized public int count(String[] from, String where) {
 		// Sicherheitsüberprüfung:
 		if (con == null) {
 			log.write("DatabaseController", "No instance of CON detected!");
 			return 0;
 		}
 		String sel = "SELECT COUNT(*) FROM " + commanator(from);
 		if (where != null)
 			sel += " WHERE " + where;
 		ResultSet rs;
 		try {
 			rs = st.executeQuery(sel);
 			rs.next();
 			return rs.getInt("COUNT(*)");
 		} catch (SQLException e) {
 			log.write("DatabaseController", "COUNT error! <" + sel + ">");
 		}
 		return 0;
 	}
 
 	/**
 	 * Methode welche ein SQL "select" Statement ausfuehrt.
 	 * 
 	 * @param select
 	 *            Welche Werte ausgewaehlt werden sollen.
 	 * @param from
 	 *            Namen der Tabellen.
 	 * @param where
 	 *            Zusaetzliche Bedingung. Wird keine benoetigt, kann
 	 *            <code>null</code> gesetzt werden.
 	 * @return Gibt ein <code>ResultSet</code> mit den Antworddaten zurueck.
 	 */
 	synchronized public ResultSet select(String[] select, String[] from,
 			String where) {
 		// Sicherheitsüberprüfung:
 		if (con == null) {
 			log.write("DatabaseController", "No instance of CON detected!");
 			return null;
 		}
 		String sel = "SELECT " + commanator(select) + " FROM "
 				+ commanator(from);
 		if (where != null)
 			sel += " WHERE " + where;
 		ResultSet rs;
 		// System.out.println(sel);
 		try {
 			rs = st.executeQuery(sel);
 			return rs;
 		} catch (SQLException e) {
 			log.write("DatabaseController", "SELECT error! <" + sel + ">");
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	/**
 	 * Methode welche ein SQL "insert on not null update" Statement ausfuehrt.
 	 * 
 	 * @param table
 	 *            Name der Tabelle.
 	 * @param columns
 	 *            Namen der Spalten.
 	 * @param values
 	 *            Ensprechende Werte welche eingefuegt oder aktualiesiert werden
 	 *            sollen.
 	 * @return boolean Bei TRUE erfolgreich ausgefuehrt. Sonst FALSE.
 	 */
 	synchronized public boolean insertOnNullElseUpdate(String table,
 			String[] columns, Object[] values) {
 		// Sicherheitsüberprüfung:
 		if (con == null) {
 			log.write("DatabaseController", "No instance of CON detected!");
 			return false;
 		}
 		String update = "INSERT INTO " + table + " VALUES ("
 				+ commanator(values) + ") ON DUPLICATE KEY UPDATE "
 				+ commanator(columns, values);
 		try {
 			st.executeUpdate(update);
 			return true;
 		} catch (SQLException e) {
 			log.write("DatabaseController", "INSERTONNULLUPDATE error! <"
 					+ update + ">");
 		}
 		return false;
 	}
 
 	/**
 	 * Hilfsmethode zum Konkatenieren von Strings mit Kommasetzung.
 	 * 
 	 * @param stringz
 	 *            Zu konkatenierende Strings.
 	 * @return Konkatenierter String.
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
 	 * @return Konkatenierter String.
 	 */
 	private String commanator(Object[] objects) {
 		String ret = "";
 		for (int i = 0; i < objects.length; i++) {
 			if (objects[i] instanceof String)
 				// Correctly display strings:
 				ret += "'" + objects[i] + "'";
 			else if (objects[i] instanceof Date) {
 				// Correctly parse & enter dates:
 				ret += "'" + new java.sql.Date(((Date) objects[i]).getTime())
 						+ "'";
 			} else
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
 			else if (value[i] instanceof Date) {
 				// Correctly parse & enter dates:
 				ret += "'" + new java.sql.Date(((Date) value[i]).getTime())
 						+ "'";
 			} else
 				ret += value[i];
 			if (i != (name.length - 1))
 				ret += ", ";
 		}
 		return ret;
 	}
 
 	
 	/**
 	 *
 	 *Funktion liefert Daten von Bewerbungen, die vom Anbieter ausgewaehlt,
 	 *aber noch nicht abgeschlossen sind.
 	 *
 	 *@param institute
 	 *			Institut des Clerks
 	 *@return
 	 *			ein Vector vom Typ HilfsDatenClerk, der die fuer den Clerk noetigen Informationen
 	 *			einer Bewerbung enthaelt
 	 */
 	public Vector<HilfsDatenClerk> getChosenApplicationDataByInstitute(
 			int institute) {
 		// Sicherheitsüberprüfung:
 		if (con == null) {
 			log.write("DatabaseController", "No instance of CON detected!");
 			return null;
 		}
 		String sel = "SELECT Accounts.name, Angebote.Name, Accounts.benutzername, Angebote.AID "
 				+ "FROM Bewerbungen, Angebote, Accounts WHERE Bewerbungen.AID = Angebote.AID AND Bewerbungen.ausgewaehlt = 1 AND Angebote.Institut = "
 				+ institute
 				+ " AND Accounts.benutzername = Bewerbungen.benutzername";
 
 		ResultSet rs;
 		// Auskommentiert da nervig (Tamino)
 //		 System.out.println(sel);
 		try {
 			rs = st.executeQuery(sel);
 			Vector<HilfsDatenClerk> hdc = new Vector<HilfsDatenClerk>();
 			while (rs.next()) {
 				// System.out.println(rs.getString(1));
 				hdc.add(new HilfsDatenClerk(rs.getString(1), rs.getString(2),
 						rs.getString(3), rs.getInt(4)));
 //				System.out.println(rs.getString(1)+ rs.getString(2)+rs.getString(3)+ rs.getInt(4));
 			}
 			return hdc;
 		} catch (SQLException e) {
 			log.write("DatabaseController", "SELECT error! <" + sel + ">");
 			e.printStackTrace();
 		}
 		return null;
 	}
 	/**
 	 * Funktion die falls noetig eine Standardkonfiguration erstellt. Hierbei wird falls noetig der 
 	 * .sopra Ordner fuer die Konfigurationsdateien und die confconf und config dateien erstellt. 
 	 */
 	public void generateStandardConfig() {
 		File configFolder = new File(System.getProperty("user.home")
 				+ System.getProperty("file.separator") + ".sopra");
 		if (!configFolder.exists()) {
 
 			configFolder.mkdir();
 			log.write("DatabaseController", ".sopra folder created.");
 		}
 		File confconf = new File(System.getProperty("user.home")
 				+ System.getProperty("file.separator") + ".sopra"
 				+ System.getProperty("file.separator") + "confconf");
 		if (!confconf.exists()) {
 			try {
 				confconf.createNewFile();
 				BufferedWriter writer = new BufferedWriter(new FileWriter(
 						confconf));
 				writer.write("# Die erste Zeile gibt an, an welchem Ort die zu interpretierende");
 				writer.newLine();
 				writer.write("# Konfigurationsdatei Liegt.");
 				writer.newLine();
 				writer.write("#");
 				writer.newLine();
 				writer.write("# in Dateipfaden steht $HOME für das Homeverzeichnis.");
 				writer.newLine();
 				writer.write("#");
 				writer.newLine();
 				writer.write("# Mögliche Optionstypen: int, String, boolean und path");
 				writer.newLine();
 				writer.write("#");
 				writer.newLine();
 				writer.write("# Neue Optionen werden nach folgendem Schema hinzugefügt:");
 				writer.newLine();
 				writer.write("#");
 				writer.newLine();
 				writer.write("# Typ | name | Standardwert");
 				writer.newLine();
 				writer.write("#");
 				writer.newLine();
 				writer.write("# Bleibt der Standardwert frei, so werden folgende Standards gesetzt:");
 				writer.newLine();
 				writer.write("#");
 				writer.newLine();
 				writer.write("# int: 0");
 				writer.newLine();
 				writer.write("# String: \"\"");
 				writer.newLine();
 				writer.write("# boolean: false");
 				writer.newLine();
 				writer.write("# path: \"\"");
 				writer.newLine();
 				writer.newLine();
 				writer.write("$HOME | .sopra | sopraconf");
 				writer.newLine();
 				writer.write("# Port der Mysql Datenbank");
 				writer.newLine();
 				writer.write("int | port | 3306");
 				writer.newLine();
 				writer.write("# Passwort für die Mysql Datenbank");
 				writer.newLine();
 				writer.write("String | password |");
 				writer.newLine();
 				writer.write("# Benutzername für die Mysql Datenbank");
 				writer.newLine();
 				writer.write("String | user | root");
 				writer.newLine();
 				writer.write("# Name der Mysql Datenbank");
 				writer.newLine();
 				writer.write("String | database | sopra");
 				writer.newLine();
 				writer.write("# Pfad der Log Datei");
 				writer.newLine();
 				writer.write("path | log | $HOME | .sopra | log");
 				writer.newLine();
 				writer.write("# Wo der log geschriebene wird.");
 				writer.newLine();
 				writer.write("# 0 = null, 1 = System.out, 2 = log file, 3 = System.out UND log file");
 				writer.newLine();
 				writer.write("int | logWriteTo | 3");
 				writer.newLine();
 				writer.write("# Pfad für den EscelExport Ordner");
 				writer.newLine();
 				writer.write("path | excel | $HOME | .sopra | excel");
 				writer.newLine();
 				writer.write("# Username des GMail Accounts zum Mailversand");
 				writer.newLine();
 				writer.write("String | GMailUsername | donotreply.hiwiboerse@gmail.com");
 				writer.newLine();
 				writer.write("# Passwort des GMail Accounts zum Mailversand");
 				writer.newLine();
 				writer.write("String | GMailPassword | Team11_11rockt");
 				writer.close();
 				log.write("DatabaseController", "confconf created.");
 			} catch (IOException e) {
 				log.write("DatebaseController", "failed to create confconf!");
 				e.printStackTrace();
 			}
 		}
 		File sopraconf = new File(System.getProperty("user.home")
 				+ System.getProperty("file.separator") + ".sopra"
 				+ System.getProperty("file.separator") + "sopraconf");
 		if (!sopraconf.exists()) {
 			try {
 				sopraconf.createNewFile();
 				log.write("DatabaseController", "sopraconf created.");
 			} catch (IOException e) {
 				log.write("DatabaseController", "failed to create sopraconf!");
 				e.printStackTrace();
 			}
 		}
 	}
 
 	/**
 	 * Funktion, die falls kein Admin Account vorhanden, ist einen erstellt.
 	 */
 	public void generateAdminAccount() {
 		ResultSet rs = select(new String[] { "count(accounttyp)" },
 				new String[] { "Accounts" }, "accounttyp=0");
 		if (rs == null) {
 			log.write("DatabaseController",
 					"No connection: couldn't create admin account.");
 			return;
 		}
 		try {
 			rs.next();
 			if (rs.getInt(1) == 0) {
 				insert("Accounts", new Object[] { "admin",
 						"ISMvKXpXpadDiUoOSoAfww", 0,
 						"donotreply.hiwiboerse@googlemail.com",
 						"Admin Account", 0, null });
 				log.write("DatabaseController",
 						"Admin account \"admin\" with password \"admin\" created.");
 			}
 		} catch (SQLException e) {
 			log.write("DatabaseController",
 					"failed to create Admin Account. Please check config.");
 		}
 	}
 
 	/**
 	 * Funktion die, falls nicht vorhanden, das default Institut erstellt.
 	 */
 	public void generateDefaultInstitute() {
 		ResultSet rs = select(new String[] { "iid" },
 				new String[] { "Institute" }, "iid=0");
 		if (rs == null) {
 			log.write("DatabaseController",
 					"No connection: couldn't create default Institute.");
 			return;
 		}
 		try {
 			if (!rs.next()) {
 				insert("Institute", new Object[] { 0, "default" });
 				log.write("DatabaseController", "default institute created.");
 			}
 		} catch (SQLException e) {
 			log.write("DatabaseController",
 					"failed to create default institute. Please check config.");
 		}
 	}
 
	/**
	 * Funktion die, falls nicht vorhanden, die Standardwerte fuer Angebote erstellt.
	 */
	public void generateDefaultDefaults() {
		ResultSet rs = select(new String[] { "*" },
				new String[] { "Standardangebot" },null);
		if (rs == null) {
			log.write("DatabaseController",
					"No connection: couldn't create default defaults.");
			return;
		}
		try {
			if (!rs.next()) {
				insert("Standardangebot", new Object[] { 30	,new Date() ,new Date() ,7.6});
				log.write("DatabaseController", "default defaults created.");
			}

		} catch (SQLException e) {
			log.write("DatabaseController",
					"failed to create default defaults. Please check config.");
		}
	}
 }
