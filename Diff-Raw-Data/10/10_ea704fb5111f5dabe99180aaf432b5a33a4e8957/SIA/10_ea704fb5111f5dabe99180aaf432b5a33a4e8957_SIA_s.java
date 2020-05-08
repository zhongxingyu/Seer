 package sia.ui;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Shell;
 import org.sormula.Database;
 import org.sormula.SormulaException;
 
 import sia.models.Configuration;
 import sia.models.Contact;
 import sia.models.ContactAccount;
 import sia.models.Conversation;
 import sia.models.Message;
 import sia.models.Protocol;
 import sia.models.UserAccount;
 import sia.utils.Dictionaries;
 import sia.utils.ORM;
 
 public class SIA {
 	public static SIA instance;
 
 	private Connection connection;
 	private ORM orm;
 	private Start window;
 	
 	/**
 	 * Initialize database and GUI
 	 */
 	public void init() {
 		String error = null;
 		try {
 			dbInit("sia.db");
 			tmpInit();
 			ormInit();
 			Dictionaries.getInstance().init();
 			guiInit();
 		} catch (SQLException e) {
 			error = e.getLocalizedMessage();
 			e.printStackTrace();
 		} catch (ClassNotFoundException e) {
 			error = e.getLocalizedMessage();
 			e.printStackTrace();
 		} catch (SormulaException e) {
 			error = e.getLocalizedMessage();
 			e.printStackTrace();
 		} catch (Exception e) {
 			error = e.getLocalizedMessage();
 			e.printStackTrace();
 		} finally {
 			if (connection != null)
 				try {
 					dbClose();
 				} catch (SQLException e) {
 					error += "\n" + e.getLocalizedMessage();
 					e.printStackTrace();
 				}
 		}
 		if (error != null) {
 			MessageDialog.openError(new Shell(Display.getCurrent()), "Error", error);
 		}
 	}
 	
 	/**
 	 * Database init
 	 * @throws ClassNotFoundException 
 	 * @throws SQLException 
 	 * @throws SormulaException 
 	 */
 	public void dbInit(String dbPath) throws ClassNotFoundException, SQLException, SormulaException {
 		Class.forName("org.sqlite.JDBC");
 		connection = DriverManager.getConnection("jdbc:sqlite:"+dbPath);
 		connection.setAutoCommit(true);
 		Statement stmt = connection.createStatement();
		stmt.execute("PRAGMA main.foreign_keys = ON");
 		stmt = connection.createStatement();
		stmt.execute("ATTACH DATABASE '' AS aux1");
 		stmt.execute("PRAGMA aux1.foreign_keys = OFF");
 	}
 	
 	/**
 	 * ORM init
 	 * @throws SormulaException
 	 */
 	public void ormInit() throws SormulaException {
 		Database database = new Database(connection, "main");
 		Database databaseTemp = new Database(connection, "aux1");
 		orm = new ORM(database, databaseTemp);
 		orm.createTable(Configuration.class);
 		orm.createTable(Contact.class);
 		orm.createTable(ContactAccount.class);
 		orm.createTable(Conversation.class);
 		orm.createTable(Message.class);
 		orm.createTable(Protocol.class);
 		orm.createTable(UserAccount.class);
 		orm.createTempTable(Contact.class);
 		orm.createTempTable(ContactAccount.class);
 		orm.createTempTable(Conversation.class);
 		orm.createTempTable(Message.class);
 		orm.createTempTable(UserAccount.class);
 	}
 	
 	/**
 	 * Temporary database init
 	 * @throws SQLException
 	 */
 	public void tmpInit() throws SQLException {
 		Statement stmt = connection.createStatement();
 	    Statement select = connection.createStatement();
 	    ResultSet result = select.executeQuery("SELECT sql, name FROM main.sqlite_master WHERE type = 'table'");
 	    String name;
 		//stmt.executeUpdate("DELETE FROM sqlite_sequence");
 		while (result.next()) {
 			name = result.getString(2);
 			if (name.indexOf("sqlite_") != 0 && name.indexOf("configuration") != 0) {
 				stmt.executeUpdate(result.getString(1).replace("CREATE TABLE", "CREATE TABLE IF NOT EXISTS aux1."));
 				stmt.executeUpdate("DELETE FROM aux1."+name);
 				//stmt.executeUpdate("INSERT INTO aux1."+name+" SELECT * FROM main."+name+" WHERE id = (SELECT MAX(id) FROM main."+name+")");
 				stmt.executeUpdate("INSERT OR REPLACE INTO aux1.sqlite_sequence SELECT name, seq FROM main.sqlite_sequence WHERE name = '"+name+"'");
 			}
 		}
 	}
 
 	/**
 	 * Save changes from temporary database to file database
 	 * @throws SQLException 
 	 */
 	public void tmpSave() throws SQLException {
 	    Statement stmt = connection.createStatement();
 	    Statement select = connection.createStatement();
 	    String val;
 	    ResultSet result = select.executeQuery("SELECT name FROM main.sqlite_master WHERE type = 'table'");
 		while (result.next()) { 
 			val = result.getString(1);
 			if (val.indexOf("sqlite_") != 0 && val.indexOf("configuration") != 0) {
 				System.out.print("tmpSave"+val);
 				System.out.println(""+stmt.executeUpdate("INSERT INTO main."+val+" SELECT * FROM aux1."+val+" WHERE aux1."+val+".id > IFNULL((SELECT MAX(id) FROM main."+val+"), 0)"));
 			}
 		}
 	}
 
 	/**
 	 * Close database connection
 	 * @throws SQLException
 	 */
 	public void dbClose() throws SQLException {
 		connection.close();
 	}
 	
 	/**
 	 * GUI init
 	 */
 	public void guiInit() {
 		window = new Start();
 		window.run();
 	}
 	
 	/**
 	 * Returns SIA instance
 	 * @return SIA
 	 */
 	public static SIA getInstance() {
 		if (instance == null)
 			instance = new SIA();
 		return instance;
 	}
 
 	/**
 	 * Returns database connection
 	 * @return database connection
 	 */
 	public Connection getConnection() {
 		return connection;
 	}
 
 	/**
 	 * Returns ORM
 	 * @return ORM
 	 */
 	public ORM getORM() {
 		return orm;
 	}
 	
 	/**
 	 * Main
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		SIA.getInstance().init();
 	}
 }
