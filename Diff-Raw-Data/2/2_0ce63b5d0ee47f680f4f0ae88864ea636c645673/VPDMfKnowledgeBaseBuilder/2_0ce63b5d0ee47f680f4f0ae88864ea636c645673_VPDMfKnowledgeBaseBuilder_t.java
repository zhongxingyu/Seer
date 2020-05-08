 package edu.isi.bmkeg.vpdmf.controller;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.FileFilter;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.io.StringReader;
 import java.lang.reflect.Constructor;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.Collection;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 import java.util.UUID;
 import java.util.jar.JarEntry;
 import java.util.jar.JarFile;
 import java.util.prefs.Preferences;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.swing.ProgressMonitor;
 import javax.swing.Timer;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.log4j.Logger;
 
 import com.darwinsys.util.FileIO;
 import com.google.common.io.Files;
 
 import edu.isi.bmkeg.uml.model.UMLattribute;
 import edu.isi.bmkeg.uml.model.UMLclass;
 import edu.isi.bmkeg.uml.model.UMLmodel;
 import edu.isi.bmkeg.utils.Converters;
 import edu.isi.bmkeg.utils.TextUtils;
 import edu.isi.bmkeg.utils.jna.LibC;
 import edu.isi.bmkeg.utils.xml.XmlBindingTools;
 import edu.isi.bmkeg.vpdmf.exceptions.VPDMfException;
 import edu.isi.bmkeg.vpdmf.model.definitions.VPDMf;
 import edu.isi.bmkeg.vpdmf.model.definitions.specs.VpdmfSpec;
 import edu.isi.bmkeg.vpdmf.utils.VPDMfConverters;
 
 public class VPDMfKnowledgeBaseBuilder {
 
 	Logger logger = Logger.getLogger("VPDMfKnowledgeBaseBuilder");
 
 	protected boolean done = false;
 	protected boolean canceled = false;
 
 	protected ProgressMonitor progress;
 	protected String progressMessage;
 	protected int progressValue;
 	protected int lengthOfTask;
 	protected int onePercent;
 	protected Timer timer;
 	protected TimerListener secondHand;
 
 	public static final String TOP_FILE = "vpdmf.bin";
 	public static final String EXCEL_FILE = "data.xls";
 	public static final String SCRIPT_DIR = "sqlFiles";
 	public static final String BUILD_FILE = "sqlFiles/build.sql";
 	public static final String LOAD_FILE = "sqlFiles/batch_upload.sql";
 
 	private boolean lc = false;
 	private HashSet builds;
 	private Map<String, Boolean> permissionsTable = new HashMap<String, Boolean>();
 
 	private String login;
 	private String password;
 	private String kbName;
 	private String pathName;
 
 	private VPDMf top;
 
 	private String groupId;
 	private String artifactId;
 	private String version;
 
 	private File vpdmfArchiveFile;
 	private File vpdmfArchiveUnzipDir;
 	private File vpdmfModelJar;
 
 	private String buildSQL;
 	private String loadSQL;
 
 	private boolean showProgressMonitor = true;
 
 	private File temp;
 
 	public VPDMfKnowledgeBaseBuilder(File vpdmfArchiveFile, String login,
 			String password, String kbName) throws Exception {
 		super();
 		this.setDefaultPermissions();
 
 		this.login = login;
 		this.password = password;
 		this.vpdmfArchiveFile = vpdmfArchiveFile;
 		this.kbName = kbName;
 
 	}
 
 	public void setKbName(String kbName) {
 		this.kbName = kbName;
 	}
 
 	public void setLogin(String login) {
 		this.login = login;
 	}
 
 	public void setPassword(String password) {
 		this.password = password;
 	}
 
 	public void setTop(VPDMf top) {
 		this.top = top;
 	}
 
 	public VPDMf getTop() {
 		return this.top;
 	}
 
 	public void setPathName(String pathName) {
 		this.pathName = pathName;
 	}
 
 	protected Map<String, Boolean> getPermissionsTable() {
 		return this.permissionsTable;
 	}
 
 	private void setDefaultPermissions() {
 		this.permissionsTable.put("user.index", new Boolean(true));
 		this.permissionsTable.put("user.view", new Boolean(true));
 		this.permissionsTable.put("user.edit", new Boolean(true));
 		this.permissionsTable.put("everyone.index", new Boolean(true));
 		this.permissionsTable.put("everyone.view", new Boolean(true));
 		this.permissionsTable.put("everyone.edit", new Boolean(false));
 	}
 
 	public boolean checkIfKbExists(String name) throws SQLException {
 		Statement quickStat = null;
 
 		Connection dbConnection = DriverManager.getConnection(
 				"jdbc:mysql://localhost:3306/", login, new String(password));
 
 		quickStat = dbConnection.createStatement(
				ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
 
 		return this.checkIfKbExists(name, quickStat);
 	}
 
 	protected boolean checkIfKbExists(String name, Statement quickStat)
 			throws SQLException {
 		String sql = "SHOW DATABASES;";
 
 		if (this.lc)
 			sql = sql.toLowerCase();
 
 		ResultSet rs = quickStat.executeQuery(sql);
 		boolean nav = rs.last();
 		int pvCount = rs.getRow();
 		for (int i = 1; i <= pvCount; i++) {
 			rs.absolute(i);
 			String dbName = rs.getString("Database");
 			if (dbName.equals(name)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public void setVPDMfArchiveFile(File buildFile) {
 		this.vpdmfArchiveFile = buildFile;
 	}
 
 	public File getVPDMfArchiveFile() {
 		return this.vpdmfArchiveFile;
 	}
 
 	public void destroyDatabase(String kbName) throws Exception {
 
 		Statement quickStat = null;
 
 		try {
 
 			quickStat = this.ConnectToDB();
 
 			this.destroyDatabase(kbName, quickStat);
 
 		} catch (SQLException sqlE) {
 
 			if (!sqlE.getMessage().contains("database doesn't exist")) {
 				sqlE.printStackTrace();
 				throw sqlE;
 			}
 
 		} finally {
 
 			quickStat.close();
 
 		}
 
 	}
 
 	public VPDMf readTop() throws Exception {
 
 		//
 		// Builds the knowledge base from the selected input scripts
 		//
 		File temp = unzipBuildFile();
 
 		UMLmodel m = this.top.getUmlModel();
 		this.setPathName(m.getName());
 
 		if (kbName == null)
 			kbName = top.getUmlModel().getName();
 
 		Converters.recursivelyDeleteFiles(temp);
 
 		return this.top;
 
 	}
 
 	public ClassLoader readClassLoader(File jarLocation) throws Exception {
 
 		//
 		// Builds the knowledge base from the selected input scripts
 		//
 		File temp = unzipBuildFile();
 		Converters.copyFile(this.vpdmfModelJar, jarLocation);
 		Converters.recursivelyDeleteFiles(temp);
 
 		URL url = jarLocation.toURI().toURL();
 		URL[] urls = new URL[] { url };
 		ClassLoader cl = new URLClassLoader(urls);
 
 		return cl;
 
 	}
 
 	// TODO
 	public void buildDatabaseFromArchive() throws Exception {
 
 		//
 		// Builds the knowledge base from the selected input scripts
 		//
 		File temp = unzipBuildFile();
 
 		UMLmodel m = this.top.getUmlModel();
 		this.setPathName(m.getName());
 
 		if (kbName == null)
 			kbName = top.getUmlModel().getName();
 
 		this.setTop(top);
 
 		// _____________________________________________________________________
 		// Global database operations
 		//
 		Statement quickStat = this.ConnectToDB();
 
 		//
 		// Set global logins & passwords
 		//
 		// setGlobalLogins(quickStat);
 
 		//
 		// Make sure the specified database doesn't already exist.
 		//
 		if (checkIfKbExists(kbName, quickStat)) {
 			logger.debug("Database " + kbName + " already exists");
 
 			throw new Exception("Database " + kbName + " already exists");
 		}
 
 		//
 		// Create the database.
 		//
 		createDatabase(kbName.toLowerCase(), quickStat);
 
 		int n = buildSQL.length() - buildSQL.replaceAll("\\n", "").length();
 
 		//
 		// Run all the commands to build the database
 		//
 		runBatchSqlCommands(this.buildSQL, quickStat);
 
 		//
 		// Instantiate the database representation of the VPDMf model.
 		//
 		insertKBData(quickStat.getConnection(), this.buildSQL);
 
 		//
 		// Close the database connection
 		//
 		quickStat.getConnection().close();
 
 		// _____________________________________________________________________
 		// Open another connection to the database that has just been built
 		//
 		quickStat = this.ConnectToDB(this.kbName);
 		Connection dbConnection = quickStat.getConnection();
 
 		//
 		// Run the load commands to populate the database
 		//
 		if (loadSQL != null)
 			runBatchSqlCommands(this.loadSQL, quickStat);
 
 		//
 		// Set all passwords in grant statements and in local preferences
 		//
 		setPasswords(quickStat);
 
 		dbConnection.close();
 
 		Converters.recursivelyDeleteFiles(temp);
 
 	}
 
 	public void setModelFromPath(String selectedModel) throws VPDMfException {
 
 		try {
 			Constructor[] cc = Class.forName(selectedModel).getConstructors();
 			UMLmodel m = (UMLmodel) cc[0].newInstance(null);
 			// this.top = m.get_top();
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw new VPDMfException(
 					"Can't invoke buildModel Method in vpdmf model class "
 							+ selectedModel);
 		}
 
 	}
 
 	private void setGlobalLogins(Statement quickStat) throws SQLException {
 
 		//
 		// Sets global logins / password permissions so that any remote
 		// NeuroScholar may enquire about the knowledge bases in this
 		// installation
 		//
 		this.runGrantStatement(quickStat, "*", "", // VPDMf.vpdmfLogin,
 				"", // VPDMf.vpdmfPassword,
 				"SHOW DATABASES", true);
 
 		this.runGrantStatement(quickStat, "*", login, password, "ALL", false);
 
 		quickStat.execute("Flush Privileges;");
 	}
 
 	public boolean isValidUser(String l) {
 		Set<String> hs = null;
 
 		try {
 			hs = this.getUsers(this.ConnectToDB());
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 
 		Iterator it = hs.iterator();
 		while (it.hasNext()) {
 			String u = (String) it.next();
 
 			if (u.equals(l)) {
 				return true;
 			}
 		}
 
 		return false;
 	}
 
 	/**
 	 * Retrieves user data for this database (no password data)
 	 * 
 	 * @param quickStat
 	 *            Statement, needs the input statement
 	 * @return HashSet, a set of all the users
 	 * @throws SQLException
 	 */
 	private Set<String> getUsers(Statement quickStat) throws SQLException {
 		Set<String> lookup = new HashSet<String>();
 		String sql = "select distinct login from " + this.kbName
 				+ ".vpdmfUser;";
 
 		if (this.lc)
 			sql = sql.toLowerCase();
 
 		ResultSet rs = quickStat.executeQuery(sql);
 		rs.last();
 		int pvCount = rs.getRow();
 		for (int i = 1; i <= pvCount; i++) {
 			rs.absolute(i);
 			String login = rs.getString("login");
 			lookup.add(login);
 		}
 		return lookup;
 	}
 
 	protected void destroyDatabase(String dbName, Statement quickStat)
 			throws SQLException {
 		String sql = "DROP DATABASE " + dbName + ";";
 
 		logger.debug(sql);
 
 		if (this.lc)
 			sql = sql.toLowerCase();
 
 		quickStat.execute(sql);
 
 	}
 
 	private void createDatabase(String dbName, Statement quickStat)
 			throws SQLException {
 		String sql = "CREATE DATABASE " + dbName + ";";
 		logger.debug(sql);
 
 		if (this.lc)
 			sql = sql.toLowerCase();
 
 		quickStat.execute(sql);
 		sql = "USE " + dbName + ";";
 		logger.debug(sql);
 
 		if (this.lc)
 			sql = sql.toLowerCase();
 
 		quickStat.execute(sql);
 	}
 
 	private File unzipBuildFile() throws Exception {
 
 		Preferences prefs = Preferences.userNodeForPackage(this.getClass());
 
 		File temp = Files.createTempDir();
 
 		Converters.unzipIt(this.vpdmfArchiveFile, temp);
 
 		String p = temp.getAbsolutePath();
 		p = p.replace("\\", "/");
 
 		File buildSQLFile = new File(p + "/" + BUILD_FILE);
 		File loadSQLFile = new File(p + "/" + LOAD_FILE);
 		File topFile = new File(p + "/" + TOP_FILE);
 
 		this.buildSQL = FileUtils.readFileToString(buildSQLFile);
 		if (loadSQLFile.exists()) {
 			this.loadSQL = FileUtils.readFileToString(loadSQLFile);
 			this.loadSQL = this.loadSQL.replaceAll("SUB_FILEPATH_HERE", p + "/"
 					+ SCRIPT_DIR);
 		}
 
 		byte[] bArray = Converters.fileContentsToBytesArray(topFile);
 		this.top = (VPDMf) VPDMfConverters.byteArrayToVPDMfObject(bArray);
 
 		this.vpdmfModelJar = new File(p + "/" + top.getUmlModel().getName()
 				+ "-jpa.jar");
 
 		File sf = new File(p + "/vpdmf.xml");
 		String spec = TextUtils.readFileToString(sf);
 		spec = spec.replaceAll("[\\t\\n]", "");
 		StringReader reader = new StringReader(spec);
 		VpdmfSpec vpdmfSpec = XmlBindingTools.parseXML(reader, VpdmfSpec.class);
 
 		this.artifactId = vpdmfSpec.getArtifactId();
 		this.groupId = vpdmfSpec.getGroupId();
 		this.version = vpdmfSpec.getVersion();
 
 		return temp;
 
 	}
 
 	private void destroy() throws Exception {
 
 		logger.debug("cleaning up" + this.temp);
 		Converters.recursivelyDeleteFiles(temp);
 
 		this.login = null;
 		this.password = null;
 		this.vpdmfArchiveFile = null;
 		this.kbName = null;
 
 	}
 
 	private void runBatchSqlCommands(String commandStr, Statement quickStat)
 			throws SQLException {
 
 		String d = VPDMfKnowledgeBaseBuilder.SCRIPT_DIR;
 		File sqlDir = new File(d);
 
 		quickStat.execute("SET FOREIGN_KEY_CHECKS = 0");
 
 		String[] commands = commandStr.split(";");
 		for (int i = 0; i < commands.length; i++) {
 			String sql = commands[i];
 			logger.debug(sql);
 			if (sql.indexOf("./" + d + "/") != -1) {
 				String absPath = sqlDir.getAbsolutePath();
 				if (absPath.indexOf("\\") != -1) {
 					absPath = absPath.replaceAll("\\\\", "\\/");
 				}
 				if (!absPath.endsWith("/"))
 					absPath += "/";
 				sql = sql.replaceAll("\\.\\/" + d + "\\/", absPath);
 			}
 
 			this.progressValue++;
 			this.progressMessage = "Creating knowledge base";
 
 			if (sql.equals("null")) {
 				continue;
 			}
 
 			logger.debug("Running sql: " + sql);
 
 			try {
 				if (this.lc)
 					sql = sql.toLowerCase();
 				quickStat.execute(sql);
 			}
 			//
 			// If there is an empty line in the sql, the server will return
 			// an error. In this case, we disregard that error and continue.
 			//
 			catch (SQLException sqle) {
 				if (sqle.getMessage().indexOf("Query was empty") == -1) {
 					quickStat.execute("SET FOREIGN_KEY_CHECKS = 1");
 					throw sqle;
 				} else {
 					int checkHere = 0;
 					checkHere++;
 				}
 			}
 
 		}
 
 		quickStat.execute("SET FOREIGN_KEY_CHECKS = 1");
 
 	}
 
 	private boolean getPermission(String key) {
 
 		Boolean bool = (Boolean) this.permissionsTable.get(key);
 		return bool.booleanValue();
 
 	}
 
 	private void updatePermissionsFromDB(Statement quickStat) throws Exception {
 
 		String sql = "select permissions from KnowledgeBase;";
 
 		if (this.lc)
 			sql = sql.toLowerCase();
 
 		ResultSet rs = quickStat.executeQuery(sql);
 		boolean nav = rs.last();
 		String permissions = rs.getString("permissions");
 
 		String[] pp = permissions.split("&");
 		for (int i = 0; i < pp.length; i++) {
 			String[] p = pp[i].split("=");
 			this.permissionsTable.put(p[0], new Boolean(p[1]));
 		}
 
 	}
 
 	protected Statement ConnectToDB() throws Exception {
 		return this.ConnectToDB("");
 	}
 
 	private Statement ConnectToDB(String targetDb) throws Exception {
 
 		//
 		// Log on to local system.
 		//
 		Class.forName("com.mysql.jdbc.Driver").newInstance();
 
 		// _____________________________________________________________________
 		// Global database operations
 		//
 
 		//
 		// Bugfix:
 		//
 		// Only ever perform knowledge base builder operations on local database
 		// (this is not actually true, if you're remotely logging in for the
 		// first time, you will need to get permissions set locally, note: this
 		// a good security measure, since you should ask the local administrator
 		// to set up a local password for you before you login).
 		//
 		Connection dbConnection = DriverManager.getConnection(
 				"jdbc:mysql://localhost:3306/" + targetDb, login, new String(
 						password));
 
 		if (dbConnection == null) {
 			throw new Exception("Can't connect!");
 		}
 
 		return dbConnection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,
 		// ResultSet.CONCUR_UPDATABLE
 				ResultSet.CONCUR_READ_ONLY);
 
 	}
 
 	public void updateGlobalPasswords() {
 
 		try {
 
 			Statement quickStat = this.ConnectToDB();
 
 			this.setGlobalLogins(quickStat);
 
 			quickStat.getConnection().close();
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Creates the user for a given login / password. Note that this is the only
 	 * place in the system where we set a new user.
 	 * 
 	 * @param stat
 	 *            - Statement, the statement that is managing this connection
 	 * @param l
 	 *            - the login name
 	 * @param p
 	 *            - the password
 	 */
 	public void createUser(Statement stat, String l, String p) {
 
 		try {
 
 			runGrantStatement(stat, "*", l, p, "SHOW DATABASES", true);
 
 		} catch (Exception e) {
 
 			e.printStackTrace();
 
 		}
 
 	}
 
 	/**
 	 * Updates passwords for users in the system
 	 * 
 	 * @param stat
 	 */
 	public void updatePasswords(Statement stat, String l, String p) {
 
 		try {
 
 			this.updatePermissionsFromDB(stat);
 			this.setPasswords(stat, l, p);
 
 		} catch (Exception e) {
 
 			e.printStackTrace();
 
 		}
 
 	}
 
 	private void setPasswords(Statement quickStat, String l, String p)
 			throws Exception {
 
 		Set<String> indexTables = new HashSet<String>();
 		indexTables.add("ViewLinkTable");
 		indexTables.add("ViewTable");
 
 		//
 		// deal with permissions for the users of the system
 		//
 		Set<String> users = this.getUsers(quickStat);
 		Iterator<String> userIt = users.iterator();
 		while (userIt.hasNext()) {
 			String login = userIt.next();
 
 			if (!login.equals(l)) {
 				continue;
 			}
 
 			if (getPermission("user.edit")) {
 				runGrantStatement(quickStat, "*", login, p,
 						"SELECT,UPDATE,INSERT,DELETE");
 			} else if (getPermission("user.view")) {
 				runGrantStatement(quickStat, "*", login, p, "SELECT");
 			} else if (getPermission("user.index")) {
 				Iterator<String> cIt = indexTables.iterator();
 				while (cIt.hasNext()) {
 					String cname = (String) cIt.next();
 					runGrantStatement(quickStat, cname, login, p, "SELECT");
 				}
 			} else {
 				throw new Exception("No permissions are set for the user : "
 						+ l);
 			}
 		}
 	}
 
 	/**
 	 * Sets all the passwords using GRANT statements Note that this now only
 	 * uses DB level access to the database.
 	 * 
 	 * @param quickStat
 	 *            Statement
 	 * @throws Exception
 	 */
 	private void setPasswords(Statement quickStat) throws Exception {
 		Set<String> indexTables = new HashSet<String>();
 		indexTables.add("ViewLinkTable");
 		indexTables.add("ViewTable");
 
 		//
 		// deal with permissions for the generic vpdmfUser
 		//
 		if (getPermission("everyone.edit")) {
 			runGrantStatement(quickStat, "*", "", // VPDMf.vpdmfLogin,
 					"", // VPDMf.vpdmfPassword,
 					"SELECT,UPDATE,INSERT,DELETE");
 		} else if (getPermission("everyone.view")) {
 			runGrantStatement(quickStat, "*", "", // VPDMf.vpdmfLogin,
 					"", // VPDMf.vpdmfPassword,
 					"SELECT");
 		} else if (getPermission("everyone.index")) {
 			Iterator<String> cIt = indexTables.iterator();
 			while (cIt.hasNext()) {
 				String cname = (String) cIt.next();
 				runGrantStatement(quickStat, cname, "", // VPDMf.vpdmfLogin,
 						"", // VPDMf.vpdmfPassword,
 						"SELECT");
 			}
 		} else {
 			throw new Exception("No permissions are set for everyone");
 		}
 
 		//
 		// deal with permissions for the users of the system
 		//
 		Set<String> users = this.getUsers(quickStat);
 		Iterator<String> userIt = users.iterator();
 		while (userIt.hasNext()) {
 			String login = (String) userIt.next();
 
 			if (getPermission("user.edit")) {
 				runGrantStatement(quickStat, "*", login, "",
 						"SELECT,UPDATE,INSERT,DELETE");
 			} else if (getPermission("user.view")) {
 				runGrantStatement(quickStat, "*", login, "", "SELECT");
 			} else if (getPermission("user.index")) {
 				Iterator<String> cIt = indexTables.iterator();
 				while (cIt.hasNext()) {
 					String cname = (String) cIt.next();
 					runGrantStatement(quickStat, cname, login, "", "SELECT");
 				}
 			} else {
 				throw new Exception("No permissions are set for users");
 			}
 
 		}
 
 		quickStat.execute("Flush Privileges;");
 	}
 
 	private void runGrantStatement(Statement quickStat, String c, String l,
 			String p, String s) throws SQLException {
 		this.runGrantStatement(quickStat, c, l, p, s, false);
 	}
 
 	private void runGrantStatement(Statement quickStat, String c, String l,
 			String p, String s, boolean globalFlag) throws SQLException {
 
 		String extra = "";
 
 		String pp = password;
 		String ll = login;
 		if (l.equals(ll) && p.equals(pp))
 			extra = " WITH GRANT OPTION";
 
 		String kb = kbName + "." + c;
 		if (globalFlag)
 			kb = "*.*";
 
 		String sql = "GRANT " + s + " on " + kb + " to '" + l + "'@'localhost'"
 				+ extra + ";";
 
 		if (p != null && p.length() > 0)
 			sql = "GRANT " + s + " on " + kb + " to '" + l
 					+ "'@'localhost' IDENTIFIED BY  '" + p + "'" + extra + ";";
 
 		if (this.lc)
 			sql = sql.toLowerCase();
 
 		logger.debug(sql);
 
 		// TODO Remove this for now.
 		// quickStat.execute(sql);
 
 	}
 
 	/**
 	 * This function takes the data from the buildfile and pushes it into the
 	 * database.
 	 * 
 	 * @param buildFile
 	 * @param quickStat
 	 * @throws Exception
 	 */
 	private void restoreDBFromBuildFile(File buildFile, Statement quickStat)
 			throws Exception {
 
 		if (this.vpdmfArchiveFile == null) {
 			throw new VPDMfException("No build file specified");
 		}
 
 		quickStat.execute("SET FOREIGN_KEY_CHECKS = 0");
 
 		Preferences prefs = Preferences.userNodeForPackage(this.getClass());
 		String tempPath = prefs.get("tempLocation", "");
 
 		/**
 		 * @todo How to fix the user's current directory on Mac machines.
 		 */
 		if (System.getProperty("mrj.version") != null) {
 			tempPath = System.getProperty("user.dir");
 		}
 
 		File tempDir = new File(tempPath);
 
 		File commands = new File(tempPath + "/commands.txt");
 		Map<String, File> toClean = Converters.unzipIt(this.vpdmfArchiveFile,
 				tempDir);
 
 		BufferedReader in = new BufferedReader(new FileReader(commands));
 		String str;
 		tempPath = tempDir.getAbsolutePath().replaceAll("\\\\", "/");
 		while ((str = in.readLine()) != null) {
 			String cmd = str.replaceAll("SUB_FILEPATH_HERE", tempPath);
 			logger.debug(cmd);
 
 			progressValue++;
 			progressMessage = "Preloading data";
 
 			if (this.lc)
 				cmd = cmd.toLowerCase();
 
 			try {
 				quickStat.execute(cmd);
 			} catch (Exception e) {
 				logger.debug("Warning: update command '" + cmd + " failed");
 			}
 		}
 
 		quickStat.execute("SET FOREIGN_KEY_CHECKS = 1");
 
 		in.close();
 
 		Converters.cleanItUp(toClean);
 
 	}
 
 	private void insertKBData(Connection dbConnection, String buildScript)
 			throws Exception {
 		
 		// delete anything from this table first.
 		String sql = "INSERT INTO KnowledgeBase ( "
 				+ "namespace, build, isRoot, buildScript, permissions, jar, groupId, artifactId, version"
 				+ ") VALUES (?, ?, 1, ?, ?, ?, ?, ?, ?);";
 
 		logger.debug(sql);
 
 		if (this.lc)
 			sql = sql.toLowerCase();
 
 		PreparedStatement psmt = dbConnection.prepareStatement(sql);
 
 		UMLmodel m = this.top.getUmlModel();
 		String catAddr = m.getTopPackage().getPkgAddress();
 
 		psmt.setString(1, catAddr);
 
 		InputStream is = new ByteArrayInputStream(
 				VPDMfConverters.vpdmfObjectToByteArray(this.top));
 		psmt.setBinaryStream(2, is, is.available());
 
 		psmt.setString(3, buildScript);
 
 		String p = "";
 		Iterator<String> en = this.permissionsTable.keySet().iterator();
 		while (en.hasNext()) {
 			String key = en.next();
 			if (p.length() > 0)
 				p += "&";
 			p += key + "=" + this.getPermission(key);
 		}
 		psmt.setString(4, p);
 
 		InputStream jarIs = new ByteArrayInputStream(
 				Converters.fileContentsToBytesArray(this.vpdmfModelJar));
 		psmt.setBinaryStream(5, jarIs, jarIs.available());
 
 		psmt.setString(6, this.groupId);
 		psmt.setString(7, this.artifactId);
 		psmt.setString(8, this.version);
 
 		psmt.executeUpdate();
 		psmt.close();
 
 	}
 
 	private void updateModelName(String modelName, Statement quickStat)
 			throws Exception {
 
 		String sql = "UPDATE KnowledgeBase SET namespace='" + modelName
 				+ "' WHERE isRoot=1;";
 		logger.debug(sql);
 
 		if (this.lc)
 			sql = sql.toLowerCase();
 
 		quickStat.execute(sql);
 
 	}
 
 	private String getFileContents(String p) throws IOException {
 
 		String c = "";
 		try {
 
 			Reader r = new FileReader(p);
 			c = FileIO.readerToString(r);
 
 		} catch (Exception e1) {
 
 			String thisLine;
 			InputStream is = this.getClass().getClassLoader()
 					.getResourceAsStream(p);
 
 			if (is == null)
 				return "";
 
 			BufferedReader br = new BufferedReader(new InputStreamReader(is));
 			while ((thisLine = br.readLine()) != null) {
 				if (c.length() > 0)
 					c += "\n";
 				c += thisLine;
 			}
 
 			//
 			// Read extra model information from the system model directory.
 			//
 			String[] nsFromModelJarFiles = this.getNamespaceFromModelJarFiles();
 			for (int i = 0; i < nsFromModelJarFiles.length; i++) {
 				if (c.indexOf(nsFromModelJarFiles[i]) == -1) {
 					c += "\n" + nsFromModelJarFiles[i];
 				}
 			}
 
 		}
 
 		c = c.replaceAll("\\r", "");
 		return c;
 
 	}
 
 	/**
 	 * Dumps all files listed in sqlFiles/toDump.txt to the ./sqlFiles directory
 	 * on the target system if the file does not already exist.
 	 * <p>
 	 * This method is something of a hack to overcome problems with building an
 	 * appropriate directory structure when the system is being run with Java
 	 * Web Start.
 	 */
 	private void dumpSqlFiles() throws IOException {
 
 		File sqlDir = new File(SCRIPT_DIR);
 		if (!sqlDir.exists()) {
 			sqlDir.mkdir();
 		}
 
 		//
 		// get the contents of the SCRIPT_DIR/models.txt file
 		Hashtable files = new Hashtable();
 		String thisLine;
 		InputStream is = this.getClass().getClassLoader()
 				.getResourceAsStream(SCRIPT_DIR + "/toDump.txt");
 
 		if (is == null)
 			return;
 
 		BufferedReader br = new BufferedReader(new InputStreamReader(is));
 		while ((thisLine = br.readLine()) != null) {
 			if (thisLine.length() > 0) {
 				String[] filesArray = thisLine.split("\\s+");
 				if (filesArray.length == 2) {
 					files.put(SCRIPT_DIR + "/" + filesArray[0], SCRIPT_DIR
 							+ "/" + filesArray[1]);
 				} else if (filesArray.length == 1) {
 					files.put(SCRIPT_DIR + "/" + filesArray[0], SCRIPT_DIR
 							+ "/" + filesArray[0]);
 				}
 			}
 		}
 
 		Enumeration en = files.keys();
 		while (en.hasMoreElements()) {
 			int i;
 			String p = (String) en.nextElement();
 			String t = (String) files.get(p);
 			File f = new File(t);
 			if (!f.exists()) {
 				FileWriter fw = new FileWriter(f);
 				is = this.getClass().getClassLoader().getResourceAsStream(p);
 				if (is == null)
 					continue;
 				br = new BufferedReader(new InputStreamReader(is));
 				String ln = null;
 				while ((ln = br.readLine()) != null) {
 					fw.write(ln);
 					fw.write("\n");
 				}
 				fw.close();
 				br.close();
 			}
 		}
 
 		//
 		// Read extra model information from the system model directory.
 		//
 		// Get the contents of the model directory:
 		// - Look into each jar file and dump relevant files into
 		// target SQL script folder.
 		// - Put the namespace of each model into the 'models.txt'
 		// file in the SQL script folder.
 		File[] jarFiles = this.getModelJarFiles();
 		for (int i = 0; i < jarFiles.length; i++) {
 			JarFile f = new JarFile(jarFiles[i]);
 
 			INNER: for (Enumeration e = f.entries(); e.hasMoreElements();) {
 				JarEntry entry = (JarEntry) e.nextElement();
 				String targetName = entry.getName();
 
 				// Skip those meta files.
 				if (targetName.indexOf("META-INF") != -1) {
 					continue;
 				} else if (targetName.equals("models.txt")) {
 
 					// Read the namespace of the current model and prepare to
 					// append it into the 'models.txt' file in the SQL script
 					// folder.
 					InputStream inStream = f.getInputStream(entry);
 					br = new BufferedReader(new InputStreamReader(inStream));
 					String modelNS = br.readLine();
 					br.close();
 
 					File modelFile = new File(SCRIPT_DIR + "/" + targetName);
 					if (modelFile.exists()) {
 
 						// Check if the namespace of the current model exists
 						// in the target 'models.txt' file or not. Do nothing if
 						// it exists.
 						br = new BufferedReader(new FileReader(modelFile));
 						String ln = null;
 						while ((ln = br.readLine()) != null) {
 							if (ln.equals(modelNS)) {
 								continue INNER;
 							}
 						}
 						br.close();
 
 						// Append the namespace of the current model into the
 						// target
 						// 'models.txt' file.
 						try {
 							BufferedWriter out = new BufferedWriter(
 									new FileWriter(modelFile, true));
 							out.write(System.getProperty("line.separator"));
 							out.write(modelNS);
 							out.close();
 						} catch (IOException ioe) {
 							ioe.printStackTrace();
 						}
 
 						continue;
 					}
 
 				}
 
 				FileWriter fw = new FileWriter(SCRIPT_DIR + "/" + targetName);
 
 				InputStream inStream = f.getInputStream(entry);
 				br = new BufferedReader(new InputStreamReader(inStream));
 
 				String ln = null;
 				while ((ln = br.readLine()) != null) {
 					fw.write(ln);
 					fw.write("\n");
 				}
 
 				fw.close();
 				br.close();
 			}
 		}
 	}
 
 	private File[] getModelJarFiles() {
 		Preferences prefs = Preferences.userNodeForPackage(this.getClass());
 		File modelDir = new File(prefs.get("ModelDirectory", ""));
 		File[] jarFiles = modelDir.listFiles(new FileFilter() {
 			public boolean accept(File f) {
 				if (f.isHidden() || f.isDirectory()
 						|| !f.getName().toLowerCase().endsWith(".jar")) {
 					return false;
 				}
 
 				return true;
 			}
 		});
 
 		return jarFiles;
 	}
 
 	private String[] getNamespaceFromModelJarFiles() throws IOException {
 		File[] jarFiles = this.getModelJarFiles();
 		String[] ns = new String[jarFiles.length];
 
 		OUT: for (int i = 0; i < jarFiles.length; i++) {
 			JarFile f = new JarFile(jarFiles[i]);
 
 			for (Enumeration e = f.entries(); e.hasMoreElements();) {
 				JarEntry entry = (JarEntry) e.nextElement();
 				String targetName = entry.getName();
 
 				if (targetName.equals("models.txt")) {
 					InputStream inStream = f.getInputStream(entry);
 					BufferedReader br = new BufferedReader(
 							new InputStreamReader(inStream));
 					ns[i] = br.readLine();
 					br.close();
 
 					continue OUT;
 				}
 			}
 		}
 
 		return ns;
 	}
 
 	public class TimerListener implements ActionListener {
 
 		public void actionPerformed(ActionEvent evt) {
 
 			progress.setProgress(progressValue);
 			if (progressMessage != null) {
 				String s = progressMessage;
 				if (s.length() > 30) {
 					s = s.substring(0, 30);
 				}
 				progress.setNote(s);
 			}
 
 			if (progress.isCanceled())
 				canceled = true;
 
 			if (canceled || done) {
 				progress.close();
 				timer.stop();
 			}
 
 		}
 	}
 
 	/**
 	 * Dumps the data from the repository to a VPDMf archive
 	 * 
 	 * @param targetZip
 	 * @throws Exception
 	 */
 	public void refreshDataToNewArchive(File targetZip) throws Exception {
 
 		File temp = unzipBuildFile();
 
 		UMLmodel m = this.top.getUmlModel();
 		this.setPathName(m.getName());
 
 		if (kbName == null)
 			kbName = top.getUmlModel().getName();
 
 		this.setTop(top);
 
 		String osName = System.getProperty("os.name");
 
 		File dumpDir = null;
 		if (osName.equals("Mac OS X")) {
 			dumpDir = new File("/tmp/vpdmf_" + UUID.randomUUID().toString());
 			dumpDir.mkdir();
 			System.err
 					.println("Dumping files to "
 							+ dumpDir.getPath()
 							+ ". Please remove these manually after program completion");
 			LibC.chmod(dumpDir.getPath(), 0777);
 		} else {
 			dumpDir = Files.createTempDir();
 		}
 		dumpDir.deleteOnExit();
 		String dAddr = dumpDir.getPath();
 
 		Statement stat = this.ConnectToDB(kbName);
 
 		// List classes in model
 		Collection<UMLclass> cVec = m.listClasses(".model.").values();
 
 		File dir = new File(dumpDir.getPath() + "/" + SCRIPT_DIR);
 		dir.mkdir();
 		LibC.chmod(dir.getPath(), 0777);
 
 		File commands = new File(dumpDir.getPath() + "/" + LOAD_FILE);
 		FileWriter out = new FileWriter(commands);
 
 		File sqlCmdFile = new File(dumpDir.getPath() + "/" + BUILD_FILE);
 		FileWriter sqlOut = new FileWriter(sqlCmdFile);
 
 		//
 		// Make a list of all tables in the database
 		//
 		HashSet tableLookup = new HashSet();
 
 		long t = System.currentTimeMillis();
 		ResultSet rs = stat.executeQuery("show tables");
 		long deltaT = System.currentTimeMillis() - t;
 
 		logger.debug("Show tables: " + deltaT + " ms");
 
 		boolean nav = rs.last();
 		int rowCount = rs.getRow();
 		for (int i = 1; i <= rowCount; i++) {
 			rs.absolute(i);
 			String tName = rs.getString(1);
 			tableLookup.add(tName);
 		}
 
 		Map<String, File> filesToZip = new HashMap<String, File>();
 		Iterator cIt = cVec.iterator();
 		while (cIt.hasNext()) {
 			UMLclass c = (UMLclass) cIt.next();
 			
 			String atts = "";
 			String ugh = "";
 			Iterator aIt = c.getAttributes().iterator();
 			while (aIt.hasNext()) {
 				UMLattribute a = (UMLattribute) aIt.next();
 				if (a.getToImplement()) {
 					if (atts.length() > 0)
 						atts += ",";
 					atts += a.getBaseName();
 				}
 			}
 
 			//
 			// UGH: to make sure that MySQL uses the order of columns we provide
 			// - this works by adding an extra column to the backup text file so
 			// there is a forced mismatch between our load statement and the
 			// file.
 			// In this case MySQL will use the column names in the order that we
 			// specify, otherwise it will use it's own order and misassign
 			// columns.
 			//
 			String rAtts = atts;
 			// UMLattribute a = (UMLattribute) c.getPkArray().get(0);
 			// atts += "," + a.getBaseName();
 
 			String filepath = dir.getPath() + "/" + c.getBaseName() + ".dat";
 			filepath = filepath.replaceAll("\\\\", "/");
 			File datFile = new File(filepath);
 			if (datFile.exists())
 				datFile.delete();
 
 			//
 			// We count the data to see if there's any rows in this table if
 			//
 			String sqlCheck = "SELECT count(*) from " + c.getBaseName() + ";";
 
 			if (this.lc)
 				sqlCheck = sqlCheck.toLowerCase();
 
 			try {
 
 				rs = stat.executeQuery(sqlCheck);
 				rs.absolute(1);
 				int count = rs.getInt(1);
 				if (count == 0)
 					continue;
 
 			} catch (SQLException e) {
 
 				Pattern patt = Pattern.compile("Table '.*' doesn't exist");
 				Matcher match = patt.matcher(e.getMessage());
 				if (match.find())
 					continue;
 				else
 					e.printStackTrace();
 			}
 
 			String sqlDump = "SELECT " + atts + " INTO OUTFILE '" + filepath
 					+ "' FIELDS TERMINATED BY '\\t\\t\\t' LINES "
 					+ "TERMINATED BY '\\n\\n\\n' FROM " + c.getBaseName() + ";";
 
 			if (this.lc)
 				sqlDump = sqlDump.toLowerCase();
 
 			boolean fileOK = true;
 
 			try {
 
 				stat.execute(sqlDump);
 
 			} catch (SQLException e) {
 
 				if (e.getMessage().indexOf("doesn't exist") != -1)
 					continue;
 				System.out.println("WARNING, error dumping data in "
 						+ c.getBaseName() + ", skipping this file");
 				fileOK = false;
 
 				throw e;
 
 			}
 
 			if (fileOK) {
 
 				filesToZip.put(SCRIPT_DIR + "/" + datFile.getName(), datFile);
 
 				String restoreSQL = "LOAD DATA LOCAL INFILE 'SUB_FILEPATH_HERE/"
 						+ c.getBaseName()
 						+ ".dat' REPLACE INTO TABLE "
 						+ c.getBaseName()
 						+ " FIELDS TERMINATED BY '\\t\\t\\t' LINES TERMINATED BY "
 						+ "'\\n\\n\\n' (" + rAtts + ");";
 
 				out.write(restoreSQL + "\n");
 
 			}
 
 		}
 		out.close();
 		filesToZip.put(SCRIPT_DIR + "/" + commands.getName(), commands);
 
 		//
 		// Save the mysql script that
 		// generated the database into a file
 		// for subsequent retrieval
 		//
 
 		String sql = "SELECT buildScript FROM KnowledgeBase WHERE isRoot=1;";
 
 		if (this.lc)
 			sql = sql.toLowerCase();
 
 		rs = stat.executeQuery(sql);
 		nav = rs.last();
 		rowCount = rs.getRow();
 
 		sql = "";
 		for (int i = 1; i <= rowCount; i++) {
 			rs.absolute(i);
 			sql += rs.getString(1) + "\n";
 		}
 		sqlOut.write(sql);
 		sqlOut.close();
 		filesToZip.put(SCRIPT_DIR + "/" + sqlCmdFile.getName(), sqlCmdFile);
 
 		File oldArchive = new File(dumpDir.getPath() + "/oldArchive");
 		oldArchive.mkdir();
 		Map<String, File> toClean = Converters.unzipIt(
 				this.getVPDMfArchiveFile(), oldArchive);
 		toClean.put(oldArchive.getPath(), oldArchive);
 
 		filesToZip = this.addAllFilesToMap(filesToZip, oldArchive,
 				oldArchive.getPath() + "/", SCRIPT_DIR);
 
 		Converters.zipIt(filesToZip, targetZip);
 		Converters.recursivelyDeleteFiles(dumpDir);
 		
 		logger.info("Completed, generated new archive at " + targetZip);
 
 	}
 
 	private Map<String, File> addAllFilesToMap(Map<String, File> filesToZip,
 			File dirToAdd, String topStem, String exPatt) {
 
 		File[] fArray = dirToAdd.listFiles();
 		for (int i = 0; i < fArray.length; i++) {
 			File f = fArray[i];
 
 			if (f.isDirectory()) {
 				this.addAllFilesToMap(filesToZip, f, topStem, exPatt);
 			} else if (!f.getPath().contains(exPatt)) {
 				String stem = f.getPath().replaceAll(topStem, "");
 				filesToZip.put(stem, f);
 			}
 
 		}
 
 		return filesToZip;
 
 	}
 
 	public void buildVpdmfModelInDatabaseFromArchive() throws Exception {
 
 		//
 		// Builds the knowledge base from the selected input scripts
 		//
 		File temp = unzipBuildFile();
 
 		UMLmodel m = this.top.getUmlModel();
 		this.setPathName(m.getName());
 
 		if (kbName == null)
 			kbName = top.getUmlModel().getName();
 
 		this.setTop(top);
 
 		// _____________________________________________________________________
 		// Global database operations
 		//
 		Statement quickStat = this.ConnectToDB();
 
 		//
 		// Make sure the specified database doesn't already exist.
 		//
 		if (!checkIfKbExists(kbName, quickStat)) {
 			logger.debug("Database " + kbName + " does not exist, can't rebuild.");
 
 			throw new Exception("Database " + kbName + " already exists");
 		}
 
 		String sql = "USE " + kbName + ";";
 		if (this.lc)
 			sql = sql.toLowerCase();
 		quickStat.execute(sql);
 		
 		sql = "DELETE FROM KnowledgeBase;";
 		if (this.lc)
 			sql = sql.toLowerCase();
 		quickStat.execute(sql);
 				
 		//
 		// Instantiate the database representation of the VPDMf model.
 		//
 		insertKBData(quickStat.getConnection(), this.buildSQL);
 
 		//
 		// Close the database connection
 		//
 		quickStat.getConnection().close();
 
 		Converters.recursivelyDeleteFiles(temp);
 
 	}
 
 }
