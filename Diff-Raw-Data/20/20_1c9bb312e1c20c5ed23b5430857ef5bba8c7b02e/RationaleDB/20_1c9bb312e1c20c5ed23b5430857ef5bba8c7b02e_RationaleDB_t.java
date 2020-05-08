 package edu.wpi.cs.jburge.SEURAT.rationaleData;
 
 import java.util.*;
 import java.io.*; //needed to be serializable
 import javax.xml.transform.*;
 import org.w3c.dom.*;
 import javax.xml.transform.stream.StreamResult;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.parsers.*;
 
 import org.xml.sax.SAXException;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.sql.ResultSet;
 import java.sql.PreparedStatement;
 
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.MessageBox;
 import org.eclipse.swt.widgets.Shell;
 import SEURAT.preferences.PreferenceConstants;
 
 import edu.wpi.cs.jburge.SEURAT.SEURATPlugin;
 import edu.wpi.cs.jburge.SEURAT.views.CandidateTreeParent;
 import edu.wpi.cs.jburge.SEURAT.views.TreeParent;
 import SEURAT.events.*;
 
 /**
  * This is the MUCH too large class that handles most of the database
  * operations. If you need to get things from the DB in bulk, this is the one to call.
  * The class is set up as a singleton design pattern.
  * @author burgeje
  *
  */
 public final class RationaleDB implements Serializable {
 	/**
 	 * Added by Eclipse
 	 */
 	private static final long serialVersionUID = 1L;
 
 	/**
 	 * This is the handle to our ONE singleton instance
 	 */
 	private static RationaleDB s;
 
 	/**
 	 * If you're loading up the DB from an XML file, this is where the
 	 * name is stored.
 	 */
 	private static String ratFile;
 
 	/**
 	 * Used when reading in from XML
 	 */
 	private static Hashtable idRefs;
 	/**
 	 * Contains the last rationale ID. I'm not sure what this is used
 	 * for - I'd guess maybe the XML?
 	 */
 	private int saveLast;
 
 	// for rationale XML input. I'm not clear why these are actually necessary!
 	/**
 	 * Holds arguments when reading in from XML
 	 */
 	private Vector<Argument> arguments;
 	/**
 	 * Holds assumptions when reading in from XML
 	 */
 	private Vector<Assumption> assumptions;
 	/**
 	 * Holds claims when reading in from XML
 	 */
 	private Vector<Claim> claims;
 	/**
 	 * Holds requirements when reading in from XML
 	 */
 	private Vector<Requirement> requirements;
 	/**
 	 * Holds decisions when reading in from XML
 	 */
 	private Vector<Decision> decisions;
 	/**
 	 * Holds tradeoffs when reading in from XML
 	 */
 	private Vector<Tradeoff> tradeoffs;
 	/**
 	 * Holds co_occurrences when reading in from XML
 	 */
 	private Vector<Tradeoff> co_occurences;
 	/**
 	 * Holds questions when reading in from XML
 	 */
 	private Vector<Question> questions;
 	/**
 	 * Holds alternatives when reading in from XML
 	 */
 	private Vector<Alternative> alternatives;
 	/**
 	 * Holds the root of the argument ontology when reading in
 	 * from XML
 	 * (This is used in the old readXMLData method, not the new
 	 * argument ontology import functionality.)
 	 */
 	private OntEntry argumentOntology;
 	
 	/**
 	 * The location of the XML argument ontology
 	 */
 	private static String ontFile = SEURATPlugin.getDefault().getStateLocation()
 		.addTrailingSeparator().toOSString() + "argument-ontology.xml";
 		
 	/**
 	 * The default database name
 	 */
 	private static String dbName = "testrat";
 
 	/**
 	 * Points towards our plugin preferences.
 	 */
 	IPreferenceStore store = SEURATPlugin.getDefault().getPreferenceStore();
 
 	/**
 	 * The connection to our database
 	 */
 	private static Connection conn;
 	
 	private PublishSubscribeManager<RationaleElement,Object,RationaleUpdateEvent>
 		updateNotifier = new 
 			PublishSubscribeManager<RationaleElement,Object,RationaleUpdateEvent>(
 				new RationaleUpdateEvent(), false
 			);
 
 	public PublishSubscribeManager<RationaleElement,Object,RationaleUpdateEvent>
 		Notifier()
 		{
 			return updateNotifier;
 		}
 	/**
 	 * Rationale DB Constructor. This MUST remain PRIVATE in order for this
 	 * class to stay a d. This class sets up the JDBC database
 	 * connection using the information from the preferences.
 	 * @param x - unused parameter.
 	 */
 	private RationaleDB(int x) {
 		idRefs = new Hashtable();
 		requirements = new Vector<Requirement>();
 		arguments = new Vector<Argument>();
 		assumptions = new Vector<Assumption>();
 		alternatives = new Vector<Alternative>();
 		claims = new Vector<Claim>();
 		decisions = new Vector<Decision>();
 		questions = new Vector<Question>();
 		tradeoffs = new Vector<Tradeoff>();
 		co_occurences = new Vector<Tradeoff>();
 		argumentOntology = new OntEntry("Argument-Ontology", null);
 		saveLast = RationaleElement.getLastID();
 
 		if( store.getString(PreferenceConstants.P_DATABASETYPE).equals(
 							PreferenceConstants.DatabaseType.DERBY
 			))
 		{
 			derbyConnect();
 		}
 		else if( store.getString(PreferenceConstants.P_DATABASETYPE).equals(
 				PreferenceConstants.DatabaseType.MYSQL
 		))
 		{
 			sqlConnect();
 		}
 		
 		migrate();
 		
 		prepareStatements();
 	};
 
 	/**
 	 * Rationale DB connect method for a MySQL database.  This method is called
 	 * from the RationaleDB constructor if the SEURAT preferences specify MySQL.
 	 */
 	private void sqlConnect()
 	{	//Set up our database connection
 		System.out.println("Setting up our database connection");
 		try {
 			//get the file name from the preferences
 			dbName = store.getString(PreferenceConstants.P_DATABASE);
 		}
 		catch (Exception ex) {
 			Shell shell = new Shell();
 			MessageBox mbox = new MessageBox(shell, SWT.ICON_ERROR);
 			mbox.setMessage("Error getting preferences");
 			mbox.open();
 		}
 
 
 		//lets set up our database
 		try {
 			// The newInstance() call is a work around for some
 			// broken Java implementations
 			com.mysql.jdbc.Driver.class.newInstance();
 			//Class.forName("com.mysql.jdbc.Driver").newInstance();
 		} catch (Exception ex) {
 			// handle the error
 			int i = 0;
 			i++;
 		}
 		
 		String passwordSpec;
 		String password = store.getString(PreferenceConstants.P_DATABASEPASSWORD);
 		if (password.length() == 0)
 		{
 			passwordSpec = "";
 		}
 		else
 		{
 			passwordSpec = "&password=" + password;
 			// passwordSpec = " ?password=" + password;
 		}
 		
 		String connStr = "jdbc:mysql://";
 		
 		// Set up connection string based on server location, address, and port preferences
 		if (store.getString(PreferenceConstants.P_MYSQLLOCATION).equals(PreferenceConstants.
 				MySQLLocationType.LOCAL)) {
 			connStr += "localhost/";
 		}
 		else {
 			String address = store.getString(PreferenceConstants.P_MYSQLADDRESS);
 			String port = store.getString(PreferenceConstants.P_MYSQLPORT);
 			
 			if (port.length() == 0) {
 				connStr += address+"/";
 			}
 			else {
 				connStr += address+":"+port+"/";
 			}
 		}
 		connStr += dbName
 			+ "?user="
 			+ store
 			.getString(PreferenceConstants.P_DATABASEUSER)
 			+ passwordSpec;
 		
 		try {
 			
 			System.out.println(connStr);
 			conn = DriverManager.getConnection(connStr);
 		} catch (SQLException ex) {
 			try {
 				conn = DriverManager.getConnection(connStr + "&createDatabaseIfNotExist=true");
 				createTables();
 			}
 			catch( SQLException eInner ) {
 				reportError(ex, "constructor", "couldn't connect to database "
 						+ dbName);
 				Shell shell = new Shell();
 				MessageBox mbox = new MessageBox(shell, SWT.ICON_ERROR);
 				mbox.setMessage("Couldn't connect to database " + dbName);
 				mbox.open();
 			}
 		}
 	}
 
 	/**
 	 * Rationale DB connect method for an Apache Derby local database.  This method
 	 * is called from the RationaleDB constructor if the SEURAT preferences specify Derby.
 	 */
 	private void derbyConnect()
 	{
 		String l_DerbyPath = "";
 		String l_DerbyPathType = "";
 
 		try {
 			//get the file name from the preferences
 			dbName = store.getString(PreferenceConstants.P_DERBYNAME);
 			l_DerbyPath = store.getString(PreferenceConstants.P_DERBYPATH);
 			l_DerbyPathType = store.getString(PreferenceConstants.P_DERBYPATHTYPE);
 		}
 		catch (Exception ex) {
 			Shell shell = new Shell();
 			MessageBox mbox = new MessageBox(shell, SWT.ICON_ERROR);
 			mbox.setMessage("Error getting preferences");
 			mbox.open();
 		}
 		
 		final String l_ConnectString = "jdbc:derby:";
 		Properties props = new Properties();
 		props.put("user", "seurat_dummy");
 		props.put("password", "seurat_dummy");
 
 		String l_dbPath = "";
 		if( l_DerbyPathType.equals(PreferenceConstants.DerbyPathType.ABSOLUTE_PATH)) {
 			l_dbPath += l_DerbyPath;
 		}
 		else {
 			l_dbPath += ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString();
 		}
 		l_dbPath += "/" + dbName;
 		props.put("databaseName", l_dbPath);
 
 		try {
 			//final String DRIVER = "org.apache.derby.jdbc.EmbeddedDriver";
 			org.apache.derby.jdbc.EmbeddedDriver.class.newInstance();
 			//Class.forName(DRIVER).newInstance();
 		} catch (Exception ex) {
 			// handle the error
 			int i = 0;
 			i++;
 		}
 
 
 		// Attempt To Connect To Pre Existing Database
 		try {
 			conn = DriverManager.getConnection(l_ConnectString, props);
 		}
 		catch( Exception eError ) {
 			try
 			{
 				props.put("create", "true");
 	    		// If the database can't be opened, it needs to be created.
 	    		conn = DriverManager.getConnection(l_ConnectString, props);
 
 	    		createTables();
 			}
 			catch( SQLException eInner )
 			{
 				reportError(eInner, "constructor", "couldn't connect to database " + dbName);
 				Shell shell = new Shell();
 				MessageBox mbox = new MessageBox(shell, SWT.ICON_ERROR);
 				mbox.setMessage("Couldn't connect to database " + dbName);
 				mbox.open();
 			}
 		}
 		System.out.println(l_dbPath);
 
 	}
 
 	/**
 	 * Rationale DB method to create the initial tables for a new rationale database.
 	 * This method is called by the connect methods if a new database needs to be created,
 	 * and functions by retrieving and executing queries using the RationaleDBCreate class. 
 	 * @throws SQLException
 	 */
 	private void createTables() throws SQLException
 	{		
 		String []l_queries = RationaleDBCreate.getQueries();
 
 		Statement l_stmt = conn.createStatement();
 		
 		for( String l_query : l_queries )
 		{
 			try
 			{
 				l_stmt.execute(l_query);
 			}
 			catch( SQLException eError )
 			{
 				int i = 0;
 				i++;
 				throw eError;
 			}
 		}
 		
 		// Now import the argument ontology, if the file exists
 		boolean importSuccess = false;
 		if (new File(ontFile).exists()){
 			importSuccess = RationaleDBUtil.importArgumentOntology(ontFile);
 		}
 		if (!importSuccess) {
 			// File doesn't exist or import failed for some reason, use the hardcoded ontology
 			String [] l_ontQueries = RationaleDBCreate.getOntologyQueries();
 			for (String l_ontQuery : l_ontQueries) {
 				try
 				{
 					l_stmt.execute(l_ontQuery);
 				}
 				catch( SQLException eError )
 				{
 					int i = 0;
 					i++;
 					throw eError;
 				}
 			}
 		}
 		
 		l_stmt.close();	
 
 	}
 
 	/**
 	 * Prepared Statement Which Is Used Primarily By The Select
 	 * Ontology Dialog To Generate A Tree Of Ontology
 	 * Elements To Select.
 	 * 
 	 * @see SelectOntEntry
 	 */
 	private static PreparedStatement m_ontEntriesQuery = null;
 	
 	/**
 	 * Prepared Statement For The Ontology Entry's
 	 * FromDatabase pseudo constructor.
 	 * 
 	 * @see OntEntry#fromDatabase(String)
 	 */
 	private static PreparedStatement m_ontEntryFromDB = null;
 	
 	/**
 	 * Accessor Method For The Ontology Entries FromDatabase Pseudo
 	 * Constructor. This is how the Ontology Entry Class Retrieves
 	 * The Prepared Statement
 	 * 
 	 * @see OntEntry#fromDatabase(String)
 	 * 
 	 * @return A prepared statement for retrieving an ontology
 	 * 		entry from the database by name.	 
 	 */
 	public PreparedStatement getStatement_OntologyEntryFromDB() {
 		return m_ontEntryFromDB;
 	}
 	
 	/**
 	 * Generate prepared statements for use in database queries. The
 	 * Prepared Statements Make accessing The Database Much Faster. 
 	 */
 	private void prepareStatements() {
 		try {
 			if( m_ontEntriesQuery != null )
 				m_ontEntriesQuery.close();
 			
 			m_ontEntriesQuery = conn.prepareStatement("SELECT childName from " 
 				+ RationaleDBUtil.escapeTableName("ONT_HIERARCHY") + " "
 				+ " where parentName = ?");
 			m_ontEntriesQuery.setEscapeProcessing(true);
 		} catch( SQLException eError ) {
 			int doNothing;
 			doNothing = 0;
 		}
 		try {
 			if( m_ontEntryFromDB != null )
 				m_ontEntryFromDB.close();
 			
 			m_ontEntryFromDB = conn.prepareStatement("SELECT id, importance, description "
 					+ " from " + RationaleDBUtil.escapeTableName("OntEntries")
 					+ " where name = ?");
 			m_ontEntryFromDB.setEscapeProcessing(true);
 		} catch( SQLException eError ) {
 			int doNothing;
 			doNothing = 0;
 		}
 	}
 	
 	/**
 	 * This Code Seamlessly Updates A Database With New Data. It will
 	 * check the existing database schema and determine whether the
 	 * desired database properties exist. If the properties do not
 	 * exist then the database schema will be modified to contain
 	 * the new attributes with default values.
 	 * 
 	 * TODO: Move This To RationaleDBCreate
 	 * @see RationaleDBCreate
 	 */
 	private void migrate() {
 		Statement l_stmt = null;
 		
 		try {
 			l_stmt = conn.createStatement();
 			
 			l_stmt.executeQuery("SELECT * FROM " + RationaleDBUtil.escapeTableName("ONT_HIERARCHY"));
 		}
 		catch( Exception eError ) {
 			Statement l_create = null;
 			try {
 				l_create = conn.createStatement();
 				
 				l_create.execute(RationaleDBCreate.CREATEVIEW_ONTRELATIONSHIP_HIERARCHY());
 			}
 			catch( Exception eInner ) 
 			{
 				int a = 0;
 				a++;
 			}
 			finally {
 				try {
 				l_create.close();
 				}
 				catch( Exception eInner ) {
 				}
 			}
 		}
 		finally {
 			try {
 				l_stmt.close();
 			}
 			catch( Exception eInner ) {
 			}
 		}
 		
 	}
 
 	/**
 	 * This does not appear to be used!
 	 *
 	 */
 	public void resetDatabase()
 	{
 		RationaleElement.setLastID(saveLast);
 	}
 	
 	/**
 	 * Get the connection to the database so we can query it.
 	 * @return the database connection
 	 */
 	public Connection getConnection() {
 		return conn;
 	}
 
 	/**
 	 * This is how we get a handle to our database. This is from the Singleton
 	 * Design Pattern
 	 * @return the database class
 	 */
 	public static RationaleDB getHandle() {
 		if (s == null) {
 			s = new RationaleDB(0);
 		}
 		return s;
 	}
 
 	/**
 	 * Set our file name. The user may decide to use a different DB.
 	 * @param fileN
 	 */
 	public static void setFile(String fileN) {
 		ratFile = fileN;
 	}
 
 	/**
 	 * Reset our connection to the database. This is done when the user
 	 * decides to connect to a different database
 	 *
 	 */
 	public static void resetConnection() {
 		s = null;
 		s = new RationaleDB(0);
 	}
 
 	/**
 	 * Create a new database using the SEURAT_Base database as a start. This
 	 * is needed so we get our empty rationale element tables and a fully populated
 	 * Argument Ontology
 	 * @param newDBName - the name for our new database.
 	 */
 	public static void createNewDB(String newDBName)
 	{
 		IPreferenceStore store = SEURATPlugin.getDefault().getPreferenceStore();
 		
 		//set our preferences to give the new database name
 		if( store.getString(PreferenceConstants.P_DATABASETYPE).equals
 			(PreferenceConstants.DatabaseType.DERBY) )
 		{
 			store.setValue(PreferenceConstants.P_DERBYNAME, newDBName);
 		}
 		else
 		if( store.getString(PreferenceConstants.P_DATABASETYPE).equals
 				(PreferenceConstants.DatabaseType.MYSQL) )
 		{
 			store.setValue(PreferenceConstants.P_DATABASE, newDBName);
 		}
 	}
 
 	/**
 	 * Given the name and type of a rationale element, get it from the database.
 	 * @param name - the name of the element
 	 * @param type - the type of the element
 	 * @return our rationale element from the database
 	 */
 	public static RationaleElement getRationaleElement(String name,
 			RationaleElementType type) {
 		RationaleElement ourEle = RationaleElementFactory
 		.getRationaleElement(type);
 		if (ourEle != null) {
 			ourEle.fromDatabase(name);
 		}
 		return ourEle;
 	}
 
 	/**
 	 * Given the ID and type of a rationale element, get it from the database
 	 * @param id - the ID (key) of the element
 	 * @param type - the type of the element
 	 * @return our rationale element from the database
 	 */
 	public static RationaleElement getRationaleElement(int id,
 			RationaleElementType type) {
 		RationaleElement ourElement;
 		ourElement = RationaleElementFactory.getRationaleElement(type);
 
 		if (ourElement != null)
 			ourElement.fromDatabase(id);
 		return ourElement;
 	}
 
 	/**
 	 * Given the name and type of a rationale element (such as an argument), return
 	 * find all requirements that are children of that element in the database.
 	 * @param parentName - name of the parent element in the tree
 	 * @param parentType - type of the parent element
 	 * @return a vector of the children elements
 	 */
	public Vector<TreeParent> getRequirements(String parentName,
 			RationaleElementType parentType) {
 		return getTreeElements(parentName, "requirements", parentType);
 	}
 
 	/**
 	 * Given the name and type of a rationale element , return
 	 * find all contingencies that are children of that element in the database.
 	 * @param parentName - name of the parent element in the tree
 	 * @param parentType - type of the parent element
 	 * @return a vector of the children elements
 	 */
	public Vector<TreeParent> getContingencies(String parentName,
 			RationaleElementType parentType) {
 		return getTreeElements(parentName, "contingencies", parentType);
 	}
 	/**
 	 * Given the name and type of a rationale element , return
 	 * find all designers that are children of that element in the database.
 	 * @param parentName - name of the parent element in the tree
 	 * @param parentType - type of the parent element
 	 * @return a vector of the children elements
 	 */
	public Vector<TreeParent> getDesigners(String parentName,
 			RationaleElementType parentType) {
 		return getTreeElements(parentName, "designerprofiles", parentType);
 	}
 
 	/**
 	 * Get all the associations from the database.
 	 * @return a vector of associations
 	 */
 	public Vector<Association> getAssociations() {
 		Vector<Association> assocs = new Vector<Association>();
 		Statement stmt = null;
 		ResultSet rs = null;
 		String findQuery = "";
 		try {
 			stmt = conn.createStatement();
 
 			findQuery = "SELECT * from " 
 				+ RationaleDBUtil.escapeTableName("associations");
 			rs = stmt.executeQuery(findQuery);
 			while (rs.next()) {
 				Association as = new Association(rs.getInt("alternative"), rs.getString("artifact"),
 						rs.getString("artresource"), rs.getString("artname"), rs.getString("assocmessage"));
 				assocs.add(as);
 			}
 		} catch (SQLException ex) {
 			// handle any errors
 			reportError(ex, "Error in getAssociations", findQuery);
 		} finally {
 			releaseResources(stmt, rs);
 
 		}
 		return assocs;
 	}
 	
 	/**
 	 * Get all the associations from the database that are at a given resource.
 	 * @return a vector of associations
 	 */
 	public Vector<Association> getAssociations(String resource) {
 		Vector<Association> assocs = new Vector<Association>();
 		Statement stmt = null;
 		ResultSet rs = null;
 		String findQuery = "";
 		try {
 			stmt = conn.createStatement();
 
 			findQuery = "SELECT * from " 
 				+ RationaleDBUtil.escapeTableName("associations")
 				+ " where artresource = '" + resource + "'";
 			rs = stmt.executeQuery(findQuery);
 			while (rs.next()) {
 				Association as = new Association(rs.getInt("alternative"), rs.getString("artifact"),
 						rs.getString("artresource"), rs.getString("artname"), rs.getString("assocmessage"));
 				assocs.add(as);
 			}
 		} catch (SQLException ex) {
 			// handle any errors
 			reportError(ex, "Error in getAssociations", findQuery);
 		} finally {
 			releaseResources(stmt, rs);
 
 		}
 		return assocs;
 	}
 
 	/**
 	 * Get all the requirements from the database that have a particular
 	 * status value (Addressed, Satisfied, or Violated)
 	 * @param stat the status value we are looking for
 	 * @return a vector of requirements
 	 */
 	public Vector<Requirement> getRequirements(ReqStatus stat) {
 		Vector<Requirement> reqst = new Vector<Requirement>();
 		Statement stmt = null;
 		ResultSet rs = null;
 		String findQuery = "";
 		try {
 			stmt = conn.createStatement();
 			findQuery = "SELECT name from "
 				+ RationaleDBUtil.escapeTableName("requirements") + " "
 				+ "where status = '" + stat.toString() + "'";
 			//***			System.out.println(findQuery);
 			rs = stmt.executeQuery(findQuery);
 			while (rs.next()) {
 				String name = RationaleDBUtil.decode(rs.getString("name"));
 				Requirement req = new Requirement();
 				req.fromDatabase(name);
 				reqst.add(req);
 			}
 		} catch (SQLException ex) {
 			reportError(ex, "Error in getRequirements", findQuery);
 		} finally {
 			releaseResources(stmt, rs);
 		}
 		return reqst;
 	}
 	
 	/**
 	 * Get all the requirements from the database that are either enabled
 	 * or disabled, depending on what we are looking for
 	 * @param enabled the enabled value we are looking for (true/false)
 	 * @return a vector of requirements
 	 */
 	public Vector<Requirement> getEnabledRequirements(boolean enabled) {
 		Vector<Requirement> reqst = new Vector<Requirement>();
 		Statement stmt = null;
 		ResultSet rs = null;
 		String findQuery = "";
 		String enabledStr;
 		if (enabled)
 			enabledStr = "True";
 		else
 			enabledStr = "False";
 		try {
 			stmt = conn.createStatement();
 			findQuery = "SELECT name from "
 				+ RationaleDBUtil.escapeTableName("requirements") + " "
 				+ "where enabled = '" + enabledStr + "'";
 			//***			System.out.println(findQuery);
 			rs = stmt.executeQuery(findQuery);
 			while (rs.next()) {
 				String name = RationaleDBUtil.decode(rs.getString("name"));
 				Requirement req = new Requirement();
 				req.fromDatabase(name);
 				reqst.add(req);
 			}
 		} catch (SQLException ex) {
 			reportError(ex, "Error in getEnabledRequirements", findQuery);
 		} finally {
 			releaseResources(stmt, rs);
 		}
 		return reqst;
 	}
 
 	/**
 	 * Get all the decisions that correspond to a particular parent and
 	 * parent type.
 	 * @param parentName - the name of our parent element
 	 * @param parentType - the parent type
 	 * @return the list of decisions
 	 */
	public Vector<TreeParent> getDecisions(String parentName,
 			RationaleElementType parentType) {
 		//		return getElements(parentName, "decisions", parentType);
 		return getTreeElements(parentName, "decisions", parentType);
 	}
 
 	/**
 	 * Return a list of questions corresponding to a particular parent
 	 * @param parentName - the name of our parent element
 	 * @param parentType - the parent type
 	 * @return the list of questions
 	 */
	public Vector<TreeParent> getQuestions(String parentName,
 			RationaleElementType parentType) {
 		//   	return getElements(parentName, "questions", parentType);
 		return getTreeElements(parentName, "questions", parentType);
 	}
 
 	/**
 	 * Return a list of alternatives that are depent on the passed in alternative
 	 * @param alt - our alternative
 	 * @return the alternatives dependent on our alternative
 	 */
 	public Vector<Alternative> getDependentAlternatives(Alternative alt) {
 		Vector<Alternative> dependent = new Vector<Alternative>();
 		Statement stmt = null;
 		ResultSet rs = null;
 		String findQuery = "";
 		try {
 			Vector<Integer> altV = new Vector<Integer>();
 			stmt = conn.createStatement();
 			findQuery = "SELECT parent, ptype from " 
 				+ RationaleDBUtil.escapeTableName("arguments") + " " 
 				+ "where alternative = "
 				+ new Integer(alt.getID()).toString()
 				+ " and argType = 'Alternative'"
 				+ " and (type = 'Pre-supposes' or type = 'Opposed')";
 			//***			System.out.println(findQuery);
 			rs = stmt.executeQuery(findQuery);
 			while (rs.next()) {
 				if ((RationaleElementType) RationaleElementType.fromString(rs
 						.getString("ptype")) == RationaleElementType.ALTERNATIVE) {
 					int altID = rs.getInt("parent");
 					altV.add(new Integer(altID));
 
 				}
 			}
 
 			if (altV.size() > 0) {
				Iterator<Integer> altI = altV.iterator();
 				while (altI.hasNext()) {
 					Alternative relAlt = new Alternative();
					relAlt.fromDatabase((altI.next()).intValue());
 					dependent.add(relAlt);
 				}
 			}
 
 		} catch (SQLException ex) {
 			reportError(ex, "Error in getDependentAlternatives", findQuery);
 		} finally {
 			releaseResources(stmt, rs);
 
 		}
 		return dependent;
 	}
 	
 	/**
 	 * Get all  arguments that argue about a specific requirement
 	 * @param reqID - the requirement ID
 	 * @return the dependent alternatives
 	 */
 	public static Vector<Argument> getDependentArguments(int reqID) {
 		Vector<Argument> dependent = new Vector<Argument>();
 		Statement stmt = null;
 		ResultSet rs = null;
 		String findQuery = "";
 		try {
 			Vector<Integer> argV = new Vector<Integer>();
 			stmt = conn.createStatement();
 			findQuery = "SELECT id from " 
 				+ RationaleDBUtil.escapeTableName("arguments") 
 				+ " where argtype = 'Requirement'"
 				+ " and requirement = "
 				+ new Integer(reqID).toString();
 			//***			System.out.println(findQuery);
 			rs = stmt.executeQuery(findQuery);
 			while (rs.next()) {
 					int altID = rs.getInt("id");
 					argV.add(new Integer(altID));
 			}
 
 			if (argV.size() > 0) {
 				Iterator argI = argV.iterator();
 				while (argI.hasNext()) {
 					Argument relArg = new Argument();
 					relArg.fromDatabase(((Integer) argI.next()).intValue());
 					dependent.add(relArg);
 				}
 			}
 
 		} catch (SQLException ex) {
 			reportError(ex, "Error in getting arguments that argue the requirement", findQuery);
 		} finally {
 			releaseResources(stmt, rs);
 
 		}
 		return dependent;
 	}
 
 	/**
 	 * Get all  arguments that argue about a specific requirement
 	 * @param reqID - the requirement ID
 	 * @return the dependent alternatives
 	 */
 	public static Vector<Argument> getDependentAssumptionArguments(int reqID) {
 		Vector<Argument> dependent = new Vector<Argument>();
 		Statement stmt = null;
 		ResultSet rs = null;
 		String findQuery = "";
 		try {
 			Vector<Integer> argV = new Vector<Integer>();
 			stmt = conn.createStatement();
 			findQuery = "SELECT id from " 
 				+ RationaleDBUtil.escapeTableName("arguments") 
 				+ " where argtype = 'Assumption"
 				+ " and assumption = "
 				+ new Integer(reqID).toString();
 			//***			System.out.println(findQuery);
 			rs = stmt.executeQuery(findQuery);
 			while (rs.next()) {
 					int altID = rs.getInt("id");
 					argV.add(new Integer(altID));
 			}
 
 			if (argV.size() > 0) {
 				Iterator argI = argV.iterator();
 				while (argI.hasNext()) {
 					Argument relArg = new Argument();
 					relArg.fromDatabase(((Integer) argI.next()).intValue());
 					dependent.add(relArg);
 				}
 			}
 
 		} catch (SQLException ex) {
 			reportError(ex, "Error in getting arguments that argue the requirement", findQuery);
 		} finally {
 			releaseResources(stmt, rs);
 
 		}
 		return dependent;
 	}
 	/**
 	 * Get all dependent alternatives that are dependent on a parent where the
 	 * dependency is of a particular argument type
 	 * @param alt - the alternative
 	 * @param atype - the argument type
 	 * @return the dependent alternatives
 	 */
 	public Vector<Alternative> getDependentAlternatives(Alternative alt, ArgType atype) {
 		Vector<Alternative> dependent = new Vector<Alternative>();
 		Statement stmt = null;
 		ResultSet rs = null;
 		String findQuery = "";
 		try {
 			Vector<Integer> altV = new Vector<Integer>();
 			stmt = conn.createStatement();
 			findQuery = "SELECT parent, ptype from " 
 				+ RationaleDBUtil.escapeTableName("arguments") 
 				+ " where alternative = "
 				+ new Integer(alt.getID()).toString()
 				+ " and (type = '"
 				+ atype.toString() + "')";
 			//***			System.out.println(findQuery);
 			rs = stmt.executeQuery(findQuery);
 			while (rs.next()) {
 				if ((RationaleElementType) RationaleElementType.fromString(rs
 						.getString("ptype")) == RationaleElementType.ALTERNATIVE) {
 					int altID = rs.getInt("parent");
 					altV.add(new Integer(altID));
 
 				}
 			}
 
 			if (altV.size() > 0) {
 				Iterator altI = altV.iterator();
 				while (altI.hasNext()) {
 					Alternative relAlt = new Alternative();
 					relAlt.fromDatabase(((Integer) altI.next()).intValue());
 					dependent.add(relAlt);
 				}
 			}
 
 		} catch (SQLException ex) {
 			reportError(ex, "Error in getDependentAlternatives2", findQuery);
 		} finally {
 			releaseResources(stmt, rs);
 
 		}
 		return dependent;
 	}
 
 	/**
 	 * Check to see if a requirement has any dependent alternatives
 	 * @param req - the requirement
 	 * @return true if there are dependent alternatives
 	 */
 	public boolean dependentAlternatives(Requirement req) {
 		Statement stmt = null;
 		ResultSet rs = null;
 		String findQuery = "";
 		try {
 			stmt = conn.createStatement();
 			findQuery = "SELECT parent, ptype from "
 				+ RationaleDBUtil.escapeTableName("arguments") + " "
 				+ " where requirement = "
 				+ new Integer(req.getID()).toString();
 			//***			System.out.println(findQuery);
 			rs = stmt.executeQuery(findQuery);
 			if (rs.next()) {
 				return true;
 			} else {
 				return false;
 			}
 
 		} catch (SQLException ex) {
 			reportError(ex, "ERror in get dependent alternatives3", findQuery);
 		} finally {
 			releaseResources(stmt, rs);
 		}
 		return false;
 	}
 
 	/**
 	 * Find all the alternatives that have a specific parent
 	 * @param parentName - the name of the parent
 	 * @param parentType - the type of the parent
 	 * @return a vector of alternatives
 	 */
 	public Vector getAlternatives(String parentName,
 			RationaleElementType parentType) {
 		String findQuery = "";
 		//    	return getElements(parentName, "alternatives", parentType);
 		Vector altTree = getTreeElements(parentName, "alternatives", parentType);
 		if (altTree != null) {
 			Statement stmt = null;
 			ResultSet rs = null;
 			try {
 				stmt = conn.createStatement();
 				//we need to find out if we are selected
 				Enumeration alts = altTree.elements();
 				while (alts.hasMoreElements()) {
 					TreeParent alt = (TreeParent) alts.nextElement();
 					alt.setActive(false);
 
 					//now, get our status
 					findQuery = "SELECT status from " 
 						+ RationaleDBUtil.escapeTableName("alternatives") 
 						+ " where "
 						+ "name = '" + RationaleDBUtil.escape(alt.getName()) + "'";
 					rs = stmt.executeQuery(findQuery);
 					if (rs.next()) {
 						String stat = rs.getString("status");
 						AlternativeStatus altst = AlternativeStatus
 						.fromString(stat);
 						if (altst == AlternativeStatus.ADOPTED) {
 							alt.setActive(true);
 						}
 						rs.close();
 					}
 
 				}
 			} catch (SQLException ex) {
 				reportError(ex, "Error in getAlternatives", findQuery);
 			} finally {
 				releaseResources(stmt, rs);
 			}
 		}
 		return altTree;
 
 	}
 
 	/**
 	 * Find all the arguments that have a particular parent
 	 * @param parentName - the name of the parent
 	 * @param parentType - the type of the parent
 	 * @return the arguments
 	 */
 	public Vector getArguments(String parentName,
 			RationaleElementType parentType) {
 		//    	return getElements(parentName, "arguments", parentType);
 		return getTreeElements(parentName, "arguments", parentType);
 	}
 
 	/**
 	 * Get all the decisions from the database (hmm... this could have
 	 * scalability issues in the future!)
 	 * @return a vector of all the decisions.
 	 */
 	public Vector<Decision> getAllDecisions() {
 		Vector<Decision> all = new Vector<Decision>();
 		Vector<String> decNames = new Vector<String>();
 		Statement stmt = null;
 		ResultSet rs = null;
 		String findQuery = "";
 		try {
 			stmt = conn.createStatement();
 			findQuery = "SELECT name from " + RationaleDBUtil.escapeTableName("decisions");
 			rs = stmt.executeQuery(findQuery);
 			while (rs.next()) {
 				String name = rs.getString("name");
 				//				System.out.println("dec name = " + name);
 				decNames.add(name);
 				//				rs.close();
 			}
 
 			Iterator decI = decNames.iterator();
 			while (decI.hasNext()) {
 				Decision dec = new Decision();
 				dec.fromDatabase((String) decI.next());
 				all.add(dec);
 			}
 		} catch (SQLException ex) {
 			reportError(ex, "Error in getAllDecisions", findQuery);
 		} finally {
 			releaseResources(stmt, rs);
 		}
 
 		return all;
 	}
 
 	/**
 	 * Given the type of RationaleElement (RationaleElementType), return
 	 * the name of the database table that contains elements of that type.
 	 * @param element - the element type
 	 * @return the table name
 	 */
 	public static String getTableName(RationaleElementType element) {
 		if (element == RationaleElementType.REQUIREMENT) {
 			return ("requirements");
 		} else if (element == RationaleElementType.DECISION) {
 			return ("decisions");
 		} else if (element == RationaleElementType.QUESTION) {
 			return ("questions");
 		} else if (element == RationaleElementType.ARGUMENT) {
 			return ("arguments");
 		} else if (element == RationaleElementType.ALTERNATIVE) {
 			return ("alternatives");
 		} else if (element == RationaleElementType.ASSUMPTION) {
 			return ("assumptions");
 		} else if (element == RationaleElementType.CLAIM) {
 			return ("claims");
 		} else if (element == RationaleElementType.EXPERTISE) {
 			return ("expertise");
 		} else if (element == RationaleElementType.ONTENTRY) {
 			return ("ontentries");
 		} else if (element == RationaleElementType.COOCCURRENCE) {
 			return ("tradeoffs");
 		} else if (element == RationaleElementType.TRADEOFF) {
 			return ("tradeoffs");
 		} else if (element == RationaleElementType.CONTINGENCY) {
 			return ("contingencies");
 		} else if (element == RationaleElementType.DESIGNER) {
 			return ("designerprofiles");
 		} else {
 			//   		System.out.println("Need to add new type " + parent.toString() );
 
 			return null;
 		}
 
 	}
 
 	/**
 	 * Given the name of the database table, return the RationaleElementType
 	 * @param elementType - the name of the database table
 	 * @return the element type
 	 */
 	public RationaleElementType getElementType(String elementType) {
 		if (elementType.compareTo("requirements") == 0) {
 			return (RationaleElementType.REQUIREMENT);
 		} else if (elementType.compareTo("decisions") == 0) {
 			return (RationaleElementType.DECISION);
 		} else if (elementType.compareTo("questions") == 0) {
 			return (RationaleElementType.QUESTION);
 		} else if (elementType.compareTo("arguments") == 0) {
 			return (RationaleElementType.ARGUMENT);
 		} else if (elementType.compareTo("alternatives") == 0) {
 			return (RationaleElementType.ALTERNATIVE);
 		} else if (elementType.compareTo("assumptions") == 0) {
 			return (RationaleElementType.ASSUMPTION);
 		} else if (elementType.compareTo("claims") == 0) {
 			return (RationaleElementType.CLAIM);
 		} else if (elementType.compareTo("expertise") == 0) {
 			return (RationaleElementType.EXPERTISE);
 		} else if (elementType.compareTo("ontentries") == 0) {
 			return (RationaleElementType.ONTENTRY);
 		} else if (elementType.compareTo("tradeoffs") == 0) {
 			return RationaleElementType.COOCCURRENCE; //or tradeoff?
 		} else if (elementType.compareTo("contingencies") == 0) {
 			return RationaleElementType.CONTINGENCY; //or contingency?
 		} else if (elementType.compareTo("designerprofiles") == 0) {
 			return RationaleElementType.DESIGNER;
 		}
 		/*		else if (elementType.compareTo("requirements" == 0))
 		 {
 		 return ("tradeoffs");
 		 } */
 		else {
 			System.out.println("Need to add new type " + elementType);
 
 			return null;
 		}
 
 	}
 
 	/**
 	 * Return the name of the claim mapping to the parent argument
 	 * @param parentName - the parents name
 	 * @return our elements name
 	 */
 	public String getClaim(String parentName) {
 		String claims = getElement(parentName, "arguments", "claim",
 				RationaleElementType.CLAIM);
 		return claims;
 	}
 	/**
 	 * Return the name of the assumption mapping to the parent argument
 	 * @param parentName - the parents name
 	 * @return our elements name
 	 */
 	public String getAssumption(String parentName) {
 		String claims = getElement(parentName, "arguments", "assumption",
 				RationaleElementType.ASSUMPTION);
 		return claims;
 	}
 	/**
 	 * Return the name of the requirement mapping to the parent argument
 	 * @param parentName - the parents name
 	 * @return our elements name
 	 */
 	public String getRequirement(String parentName) {
 		String claims = getElement(parentName, "arguments", "requirement",
 				RationaleElementType.REQUIREMENT);
 		return claims;
 	}
 	/**
 	 * Return the name of the alternative mapping to the parent argument
 	 * @param parentName - the parents name
 	 * @return our elements name
 	 */
 	public String getAlternative(String parentName) {
 		String claims = getElement(parentName, "arguments", "alternative",
 				RationaleElementType.ALTERNATIVE);
 		return claims;
 	}
 	/**
 	 * Return the name of the ontology entry mapping to the parent tradeoff
 	 * @param tradeName - the parents name
 	 * @param whichOnt the element name
 	 * @return our elements name
 	 */
 	public String getTradeOntology(String tradeName, String whichOnt) {
 		String onts = getElement(tradeName, "tradeoffs", whichOnt,
 				RationaleElementType.ONTENTRY);
 		return onts;
 	}
 	/**
 	 * Return the name of the ontology entry mapping to the parent claim
 	 * @param parentName - the parents name
 	 * @return our elements name
 	 */
 	public String getOntEntry(String parentName) {
 		String onts = getElement(parentName, "claims", "ontology",
 				RationaleElementType.ONTENTRY);
 		return onts;
 	}
 
 	/**
 	 * Given the name of the parent, the table type of the parent, the name
 	 * of the element field in the parent table, and the element type, get the
 	 * name of the element. Ok, this is confusing but the parent table has the
 	 * ID of the item associated with it, not its name. As an example, you
 	 * want the ontology entry associated with a claim called "Cheap to code" so
 	 * you would give the parent name as "Cheap to code", the parent table as "claims", the
 	 * name of the field that will identy the ontology (rather misleadingly
 	 * called "elementName" here) as "ontology", and the element type as ONTENTRY.
 	 *
 	 * @param parentName - the name of the parent element
 	 * @param parentTable - the type of table for the parent
 	 * @param elementName - the name of the field in the parent's table that contains our element
 	 * @param elementType - the type of element we are trying to get
 	 * @return the name of our child element
 	 */
 	private String getElement(String parentName, String parentTable,
 			String elementName, RationaleElementType elementType) {
 
 		String ourElement = "not set!";
 		String findQuery = "";
 		Statement stmt = null;
 		ResultSet rs = null;
 		try {
 			stmt = conn.createStatement();
 			int pid = 0;
 			//first, get the ID of our element
 			findQuery = "SELECT " + elementName + " FROM " 
 			+ RationaleDBUtil.escapeTableName(parentTable)
 			+ " WHERE name = '" + RationaleDBUtil.escape(parentName) + "'";
 
 			rs = stmt.executeQuery(findQuery);
 			if (rs.next()) {
 				pid = rs.getInt(elementName);
 				rs.close();
 			} else {
 				//				System.out.println(findQuery);
 				//***			System.out.println("No matching " + elementType + " for name " + parentName);
 				return ("");
 			}
 
 			findQuery = "SELECT name FROM " 
 				+ RationaleDBUtil.escapeTableName(this.getTableName(elementType)) 
 				+ " WHERE id = " + new Integer(pid).toString();
 
 			rs = stmt.executeQuery(findQuery);
 
 			if (rs.next()) {
 				ourElement = RationaleDBUtil.decode(rs.getString("name"));
 			}
 		} catch (SQLException ex) {
 			// handle any errors
 			reportError(ex, "error in getElement", findQuery);
 		} finally {
 			releaseResources(stmt, rs);
 		}
 		return ourElement;
 	}
 
 	/**
 	 * Return the name of the ont entry mapping to the parent claim
 	 * @param parentName - the parents name
 	 * @return our elements name
 	 */
 	public String getClaimOntEntry(String parentName) {
 
 		String ourElement = "not set!";
 		String findQuery = "";
 		Statement stmt = null;
 		ResultSet rs = null;
 		try {
 			stmt = conn.createStatement();
 			int pid = 0;
 			String direction = "";
 			//first, get the ID of our element
 			findQuery = "SELECT ontology, direction " + " FROM "
 			+ RationaleDBUtil.escapeTableName("claims") + " " 
 			+ " WHERE name = '" + RationaleDBUtil.escape(parentName) + "'";
 			//				System.out.println(findQuery);
 			rs = stmt.executeQuery(findQuery);
 			if (rs.next()) {
 				pid = rs.getInt("ontology");
 				direction = rs.getString("direction") + " ";
 				//				rs.close();
 			} else {
 				//				System.out.println(findQuery);
 				System.out.println("No matching ontolgy entry " + " for name "
 						+ parentName);
 				return ("");
 			}
 
 			findQuery = "SELECT name FROM " 
 				+ RationaleDBUtil.escapeTableName("OntEntries") + " " 
 				+ " WHERE id = "
 			+ new Integer(pid).toString();
 			//			System.out.println(findQuery);
 			rs = stmt.executeQuery(findQuery);
 
 			if (rs.next()) {
 				ourElement = direction
 				+ RationaleDBUtil.decode(rs.getString("name"));
 			}
 		} catch (SQLException ex) {
 			reportError(ex, "Error in getClaimOntEntry", findQuery);
 		} finally {
 			releaseResources(stmt, rs);
 		}
 		return ourElement;
 	}
 
 	/**
 	 * Find the names of the ontology entries that are children of the parent
 	 * @param parentName - the name of the parent element
 	 * @return the name of the ontology entries
 	 */
 	public Vector<String> getOntology(String parentName) {
 
 		Vector<String> onts = new Vector<String>();
 		Vector<String> children = new Vector<String>();
 		Statement stmt = null;
 		ResultSet rs = null;
 		String findQuery = "";
 		try {
 			stmt = conn.createStatement();
 			int pid = 0;
 			//first, get the ID of our element
 			findQuery = "SELECT id " + " FROM " + 
 			RationaleDBUtil.escapeTableName("ontentries") + " "
 			+ " WHERE name = '" + RationaleDBUtil.escape(parentName)
 			+ "'";
 
 			rs = stmt.executeQuery(findQuery);
 			if (rs.next()) {
 				pid = rs.getInt("id");
 				rs.close();
 			} else {
 				//				System.out.println(findQuery);
 				System.out.println("No matching ontolgy entry " + " for name "
 						+ parentName);
 			}
 
 			findQuery = "SELECT child FROM " 
 				+ RationaleDBUtil.escapeTableName("OntRelationships") + " "
 				+ " WHERE parent = " + new Integer(pid).toString();
 
 			rs = stmt.executeQuery(findQuery);
 
 			while (rs.next()) {
 				children.addElement(new Integer(rs.getInt("child")).toString());
 			}
 			rs.close();
 			//Now, finally, get our children
 			Enumeration ids = children.elements();
 			while (ids.hasMoreElements()) {
 				findQuery = "SELECT name FROM "
 					+ RationaleDBUtil.escapeTableName("ontentries") + " "
 					+ "WHERE id = "
 					+ (String) ids.nextElement();
 				rs = stmt.executeQuery(findQuery);
 				if (rs.next()) {
 					onts.addElement(RationaleDBUtil.decode(rs.getString("name")));
 					rs.close();
 				}
 			}
 
 		} catch (SQLException ex) {
 			reportError(ex, "error in getOntology", findQuery);
 		} finally {
 			releaseResources(stmt, rs);
 
 		}
 		return onts;
 	}
 
 	/**
 	 * Gets a list of all the constraints belonging to a particular parent entry
 	 * @param parentName - the name of the parent
 	 * @param ptype - the parents type
 	 * @return a vector of constraints
 	 */
 	public Vector<String> getConstraints(String parentName, RationaleElementType ptype) {
 
 		Vector<String> consts = new Vector<String>();
 		Vector<String> children = new Vector<String>();
 		Statement stmt = null;
 		ResultSet rs = null;
 		String findQuery = "";
 		try {
 			stmt = conn.createStatement();
 
 			if (ptype == RationaleElementType.CONSTRAINT) {
 
 				int pid = 0;
 				//first, get the ID of our element
 				findQuery = "SELECT id " + " FROM "
 				+ RationaleDBUtil.escapeTableName("CONSTRAINTS") + " "
 				+ " WHERE name = '" + RationaleDBUtil.escape(parentName)
 				+ "'";
 
 				rs = stmt.executeQuery(findQuery);
 				if (rs.next()) {
 					pid = rs.getInt("id");
 					rs.close();
 				} else {
 					//						System.out.println(findQuery);
 					System.out.println("No matching constraint entry "
 							+ " for name " + parentName);
 				}
 
 				findQuery = "SELECT child FROM " 
 					+ RationaleDBUtil.escapeTableName("ConstraintRelationships") + " "
 					+ " WHERE parent = " + new Integer(pid).toString();
 
 				rs = stmt.executeQuery(findQuery);
 
 				while (rs.next()) {
 					children.addElement(new Integer(rs.getInt("child"))
 							.toString());
 				}
 				rs.close();
 			} else if (ptype == RationaleElementType.ALTCONSTREL) {
 				int pid = 0;
 				//first, get the ID of our element
 				findQuery = "SELECT id " + " FROM " 
 				+ RationaleDBUtil.escapeTableName("AltConstRel") + " "
 				+ " WHERE name = '" + RationaleDBUtil.escape(parentName)
 				+ "'";
 
 				rs = stmt.executeQuery(findQuery);
 				if (rs.next()) {
 					pid = rs.getInt("id");
 					rs.close();
 				} else {
 					System.out.println(findQuery);
 					System.out.println("No matching parent entry "
 							+ " for name " + parentName);
 				}
 
 				findQuery = "SELECT constr FROM " 
 					+ RationaleDBUtil.escapeTableName("AltConstRel") + " "
 					+ " WHERE id = " + new Integer(pid).toString();
 
 				rs = stmt.executeQuery(findQuery);
 
 				while (rs.next()) {
 					children.addElement(new Integer(rs.getInt("constr"))
 							.toString());
 				}
 				rs.close();
 			} else if (ptype == RationaleElementType.DECISION) {
 				int pid = 0;
 				//first, get the ID of our element
 				findQuery = "SELECT id " + " FROM " +
 				RationaleDBUtil.escapeTableName("Decisions") + " "
 				+ " WHERE name = '" + RationaleDBUtil.escape(parentName)
 				+ "'";
 
 				rs = stmt.executeQuery(findQuery);
 				if (rs.next()) {
 					pid = rs.getInt("id");
 					rs.close();
 				} else {
 					System.out.println(findQuery);
 					System.out.println("No matching parent entry "
 							+ " for name " + parentName);
 				}
 
 				findQuery = "SELECT constr FROM " 
 					+ RationaleDBUtil.escapeTableName("ConDecRelationships") + " "
 					+ " WHERE decision = " + new Integer(pid).toString();
 
 				rs = stmt.executeQuery(findQuery);
 
 				while (rs.next()) {
 					children.addElement(new Integer(rs.getInt("constr"))
 							.toString());
 				}
 				rs.close();
 			}
 			//Now, finally, get our children
 			Enumeration ids = children.elements();
 			while (ids.hasMoreElements()) {
 				findQuery = "SELECT name FROM " 
 					+ RationaleDBUtil.escapeTableName("CONSTRAINTS") + " " 
 					+ "WHERE id = "
 					+ (String) ids.nextElement();
 				rs = stmt.executeQuery(findQuery);
 				if (rs.next()) {
 					consts.addElement(RationaleDBUtil.decode(rs.getString("name")));
 					rs.close();
 				}
 			}
 
 		} catch (SQLException ex) {
 			reportError(ex, "error in getConstraints", findQuery);
 		} finally {
 			releaseResources(stmt, rs);
 
 		}
 		return consts;
 	}
 
 	/**
 	 * Return a list of relationships between alternatives and constraints
 	 * @param parentName - the name of the parent alternative
 	 * @return the list of relationships (constraint names???)
 	 */
 	public Vector<String> getAltConstRels(String parentName) {
 
 		Vector<String> relationships = new Vector<String>();
 		Statement stmt = null;
 		ResultSet rs = null;
 		String findQuery = "";
 		try {
 			stmt = conn.createStatement();
 
 			int pid = 0;
 			//first, get the ID of our element
 			findQuery = "SELECT id " + " FROM " 
 				+ RationaleDBUtil.escapeTableName("Alternatives") + " "
 				+ " WHERE name = '"
 				+ RationaleDBUtil.escape(parentName) + "'";
 
 			rs = stmt.executeQuery(findQuery);
 			if (rs.next()) {
 				pid = rs.getInt("id");
 				rs.close();
 			} else {
 				//						System.out.println(findQuery);
 				System.out.println("No matching constraint entry "
 						+ " for name " + parentName);
 			}
 
 			findQuery = "SELECT name FROM " 
 				+ RationaleDBUtil.escapeTableName("AltConstRel") + " "
 				+ " WHERE alternative = " + new Integer(pid).toString();
 
 			rs = stmt.executeQuery(findQuery);
 
 			while (rs.next()) {
 				relationships.addElement(RationaleDBUtil.decode(rs.getString("name")));
 			}
 			rs.close();
 
 		} catch (SQLException ex) {
 			reportError(ex, "error in getConstraints", findQuery);
 		} finally {
 			releaseResources(stmt, rs);
 
 		}
 		return relationships;
 	}
 
 	/**
 	 * Gets a list of child design components given the name of a parent component
 	 * @param parentName - the parent component name
 	 * @return a list of child names
 	 */
 	public Vector<String> getProducts(String parentName) {
 
 		Vector<String> products = new Vector<String>();
 		Vector<String> children = new Vector<String>();
 		Statement stmt = null;
 		ResultSet rs = null;
 		String findQuery = "";
 		try {
 			stmt = conn.createStatement();
 			int pid = 0;
 			//first, get the ID of our element
 			findQuery = "SELECT id " + " FROM " 
 			+ RationaleDBUtil.escapeTableName("DesignComponents") + " "
 			+ " WHERE name = '" + RationaleDBUtil.escape(parentName) + "'";
 
 			rs = stmt.executeQuery(findQuery);
 			if (rs.next()) {
 				pid = rs.getInt("id");
 				rs.close();
 			} else {
 				//				System.out.println(findQuery);
 				System.out.println("No matching component entry "
 						+ " for name " + parentName);
 			}
 
 			findQuery = "SELECT child FROM " 
 				+ RationaleDBUtil.escapeTableName("DesignComponentRelationships") + " "
 				+ " WHERE parent = " + new Integer(pid).toString();
 
 			rs = stmt.executeQuery(findQuery);
 
 			while (rs.next()) {
 				children.addElement(new Integer(rs.getInt("child")).toString());
 			}
 			rs.close();
 			//Now, finally, get our children
 			Enumeration ids = children.elements();
 			while (ids.hasMoreElements()) {
 				findQuery = "SELECT name FROM " 
 					+ RationaleDBUtil.escapeTableName("DesignComponents") + " " 
 					+ " WHERE id = " + (String) ids.nextElement();
 				rs = stmt.executeQuery(findQuery);
 				if (rs.next()) {
 					products.addElement(RationaleDBUtil.decode(rs.getString("name")));
 					rs.close();
 				}
 			}
 
 		} catch (SQLException ex) {
 			reportError(ex, "error in getOntology", findQuery);
 		} finally {
 			releaseResources(stmt, rs);
 		}
 		return products;
 	}
 
 	/**
 	 * Return a list of child ontology entries given the parent
 	 * @param parentName - the name of the parent
 	 * @return a list of child elements - the actual elements, not just names
 	 */
 	public Vector<OntEntry> getOntologyElements(String parentName) {
 
 		Vector<OntEntry> onts = new Vector<OntEntry>();
 		ResultSet rs = null;
 		try {
 			m_ontEntriesQuery.setString(1, parentName);
 			rs = m_ontEntriesQuery.executeQuery();
 
 			while (rs.next()) {			
 					OntEntry entry = new OntEntry();
 					entry.fromDatabase(rs.getString("childName"));
 					onts.addElement(entry);
 			}
 			rs.close();
 		} catch (SQLException ex) {
 			reportError(ex, "Error in getOntologyElements", "getOntologyElements");
 		} finally {
 			releaseResources(null, rs);
 		}
 		return onts;
 	}
 	
 	/**
 	 * Return a list of child element entries given the parent for treeview display
 	 * @param parentName - the name of the parent
 	 * @param type - specify the type of element
 	 * @return a list of child elements - the actual elements, not just names
 	 */
 	public Vector<String> getElements_TreeView(String parentName, RationaleElementType type,boolean firstTime) {
 		
 		Vector<String> elements = new Vector<String>();
 		ResultSet rs = null;
 		try {
 			RationaleDB db = RationaleDB.getHandle();
 			Connection conn = db.getConnection();
 			Statement stmt = null;
 			String selectStr="";
 			if(firstTime)
 			{
 				if (type==RationaleElementType.REQUIREMENT)
 					selectStr="Select * from (SELECT a.id as childID,a.name as childName,a.parent as parentID,b.name as parentName FROM (requirements as a) left join (requirements as b) on (a.parent=b.id)) as t where t.parentName is null";
 				else if (type==RationaleElementType.DECISION)
 					selectStr="Select * from (SELECT a.id as childID,a.name as childName,a.parent as parentID,b.name as parentName FROM (decisions as a) left join (decisions as b) on (a.parent=b.id)) as t where t.parentName is null";
 				else if (type==RationaleElementType.ALTERNATIVE)
 					selectStr="Select * from (SELECT a.id as childID,a.ptype,a.name as childName,a.parent as parentID,b.name as parentName FROM (alternatives as a) left join (alternatives as b) on (a.parent=b.id)) as t where t.ptype="+"'Decision'";
 				else if (type==RationaleElementType.ARGUMENT)
 					selectStr="Select * from (SELECT a.id as childID,a.name as childName,a.parent as parentID,b.name as parentName FROM (arguments as a) left join (arguments as b) on (a.parent=b.id)) as t where t.parentName is null";
 			}
 			else
 			{
 				if (type==RationaleElementType.REQUIREMENT)
 					selectStr="Select * from (SELECT a.id as childID,a.name as childName,a.parent as parentID,b.name as parentName FROM (requirements as a) left join (requirements as b) on (a.parent=b.id)) as t where t.parentName="+"'"+parentName+"'";
 				else if (type==RationaleElementType.DECISION)
 					selectStr="Select * from (SELECT a.id as childID,a.name as childName,a.parent as parentID,b.name as parentName FROM (decisions as a) left join (decisions as b) on (a.parent=b.id)) as t where t.parentName="+"'"+parentName+"'";
 				else if (type==RationaleElementType.ALTERNATIVE)
 					selectStr="Select * from (SELECT a.id as childID,a.name as childName,a.parent as parentID,b.name as parentName FROM (alternatives as a) left join (alternatives as b) on (a.parent=b.id)) as t where t.parentName="+"'"+parentName+"'";
 				else if (type==RationaleElementType.ARGUMENT)
 					selectStr="Select * from (SELECT a.id as childID,a.name as childName,a.parent as parentID,b.name as parentName FROM (arguments as a) left join (arguments as b) on (a.parent=b.id)) as t where t.parentName="+"'"+parentName+"'";
 			}
 //			System.out.println(selectStr);
 			stmt = conn.createStatement();
 			rs = stmt.executeQuery(selectStr);
 			while (rs.next()) {			
 					String name = "";
 					name = rs.getString("childName");
 					elements.addElement(name);
 			}
 			rs.close();
 		} catch (SQLException ex) {
 			reportError(ex, "Error in getElements_TreeView method", "getOntologyElements");
 		} finally {
 			releaseResources(null, rs);
 		}
 		return elements;
 	}
 	
 
 	/**
 	 * Given a parent constraint, return a list of sub-constraints
 	 * @param parentName - the name of the parent
 	 * @return the child constraints
 	 */
 	public Vector<Constraint> getConstraintElements(String parentName) {
 
 		Vector<Constraint> constrs = new Vector<Constraint>();
 		Vector<String> children = new Vector<String>();
 		Statement stmt = null;
 		ResultSet rs = null;
 		String findQuery = "";
 		try {
 			stmt = conn.createStatement();
 			int pid = 0;
 			//first, get the ID of our element
 			findQuery = "SELECT id " + " FROM " 
 			+ RationaleDBUtil.escapeTableName("CONSTRAINTS") + " "
 			+ " WHERE name = '" + RationaleDBUtil.escape(parentName)
 			+ "'";
 
 			rs = stmt.executeQuery(findQuery);
 			if (rs.next()) {
 				pid = rs.getInt("id");
 				rs.close();
 			} else {
 				System.out.println(findQuery);
 				System.out.println("No matching constraint entry "
 						+ " for name " + parentName);
 			}
 
 			findQuery = "SELECT child FROM " 
 				+ RationaleDBUtil.escapeTableName("ConstraintRelationships") + " "
 				+ " WHERE parent = " + new Integer(pid).toString();
 
 			rs = stmt.executeQuery(findQuery);
 
 			while (rs.next()) {
 				children.addElement(new Integer(rs.getInt("child")).toString());
 			}
 			rs.close();
 			//Now, finally, get our children
 			Enumeration ids = children.elements();
 			while (ids.hasMoreElements()) {
 				findQuery = "SELECT name FROM "
 					+ RationaleDBUtil.escape("CONSTRAINTS") + " "
 					+ " WHERE id = "
 					+ (String) ids.nextElement();
 				rs = stmt.executeQuery(findQuery);
 				if (rs.next()) {
 					Constraint entry = new Constraint();
 					entry
 					.fromDatabase(RationaleDBUtil.decode(rs
 							.getString("name")));
 					constrs.addElement(entry);
 
 					rs.close();
 				}
 			}
 
 		} catch (SQLException ex) {
 			reportError(ex, "Error in getConstraintElements", findQuery);
 		} finally {
 			releaseResources(stmt, rs);
 
 		}
 		return constrs;
 	}
 
 	/**
 	 * Get a list of sub-design product elements given the parent component
 	 * @param parentName - the name of the parent element
 	 * @return the list of sub-elements
 	 */
 	public Vector<DesignProductEntry> getDesignProductElements(String parentName) {
 		String findQuery = "";
 		Vector<DesignProductEntry> components = new Vector<DesignProductEntry>();
 		Vector<String> children = new Vector<String>();
 		Statement stmt = null;
 		ResultSet rs = null;
 		try {
 			stmt = conn.createStatement();
 			int pid = 0;
 			//first, get the ID of our element
 			findQuery = "SELECT id " + " FROM " +
 			RationaleDBUtil.escapeTableName("DesignComponents") + " "
 			+ " WHERE name = '" + RationaleDBUtil.escape(parentName)
 			+ "'";
 
 			rs = stmt.executeQuery(findQuery);
 			if (rs.next()) {
 				pid = rs.getInt("id");
 				rs.close();
 			} else {
 				System.out.println(findQuery);
 				System.out.println("No matching design component entry "
 						+ " for name " + parentName);
 			}
 
 			findQuery = "SELECT child FROM " 
 			+ RationaleDBUtil.escapeTableName("DesignComponentRelationships") + " "
 			+ " WHERE parent = " + new Integer(pid).toString();
 
 			rs = stmt.executeQuery(findQuery);
 
 			while (rs.next()) {
 				children.addElement(new Integer(rs.getInt("child")).toString());
 			}
 			rs.close();
 			//Now, finally, get our children
 			Enumeration ids = children.elements();
 			while (ids.hasMoreElements()) {
 				findQuery = "SELECT name FROM "
 					+ RationaleDBUtil.escapeTableName("DesignComponents") + " "
 					+ " WHERE id = " + (String) ids.nextElement();
 				rs = stmt.executeQuery(findQuery);
 				if (rs.next()) {
 					DesignProductEntry entry = new DesignProductEntry();
 					entry
 					.fromDatabase(RationaleDBUtil.decode(rs
 							.getString("name")));
 					components.addElement(entry);
 
 					rs.close();
 				}
 			}
 
 		} catch (SQLException ex) {
 			reportError(ex, "Error in getDesignProductElements", findQuery);
 		} finally {
 			releaseResources(stmt, rs);
 
 		}
 		return components;
 	}
 	/**
 	 * Given the name of an argument, get its type from the database
 	 * @param argName - argument name
 	 * @return argument type
 	 */
 	public String getArgumentType(String argName) {
 		String argType = "";
 		Statement stmt = null;
 		ResultSet rs = null;
 		String findQuery = "";
 		//just in case
 		argName = RationaleDBUtil.escape(argName);
 		try {
 			stmt = conn.createStatement();
 			//first, get the ID of our element
 			findQuery = "SELECT argtype from " +
 			RationaleDBUtil.escapeTableName("arguments") + " "
 			+ " where name ='" + argName + "'";
 
 			rs = stmt.executeQuery(findQuery);
 			if (rs.next()) {
 				argType = rs.getString("argtype");
 				rs.close();
 			} else {
 				//				System.out.println(findQuery);
 				argType = "";
 			}
 
 		} catch (SQLException ex) {
 			reportError(ex, "error in getArgumentType", findQuery);
 		} finally {
 			releaseResources(stmt, rs);
 		}
 		return argType;
 	}
 
 
 	/**
 	 * Get a list of names of all elements in the database that have a
 	 * particular rationale element type
 	 * @param type - the type of element we are looking for
 	 * @return - the list of names
 	 */
 	public Vector<String> getNameList(RationaleElementType type) {
 		Vector<String> ourElements = new Vector<String>();
 		String tableName = this.getTableName(type);
 		String findQuery = "";
 		Statement stmt = null;
 		ResultSet rs = null;
 		try {
 			stmt = conn.createStatement();
 			String additional = "";
 			if (type == RationaleElementType.TRADEOFF) {
 				additional = "WHERE type = 'Tradeoff'";
 			}
 			else if (type == RationaleElementType.COOCCURRENCE) {
 				additional = "WHERE type = 'Co-Occurrence'";
 			}
 			findQuery = "SELECT name FROM " 
 				+ RationaleDBUtil.escapeTableName(tableName) + " " 
 				+ additional + " ORDER BY name ASC";
 			System.out.println(findQuery);
 			rs = stmt.executeQuery(findQuery);
 
 			while (rs.next()) {
 				ourElements
 				.addElement(RationaleDBUtil.decode(rs.getString("name")));
 			}
 
 		} catch (SQLException ex) {
 			reportError(ex, "Error in getNameList", findQuery);
 		} finally {
 			releaseResources(stmt, rs);
 		}
 		return ourElements;
 
 	}
 	
 	/**
 	 * Get a list of names of all elements in the database that have a
 	 * particular rationale element type and where the name contains a search string
 	 * @param type - the type of element we are looking for
 	 * @param sstring - the substring we want to find in the name
 	 * @return - the list of names
 	 */
 	public Vector<String> getNameList(RationaleElementType type, String sstring) {
 		Vector<String> ourElements = new Vector<String>();
 		String tableName = this.getTableName(type);
 		String findQuery = "";
 		Statement stmt = null;
 		ResultSet rs = null;
 		try {
 			stmt = conn.createStatement();
 			String additional = "";
 			if (type == RationaleElementType.TRADEOFF) {
 				additional = "WHERE type = 'Tradeoff'";
 			}
 			else if (type == RationaleElementType.COOCCURRENCE) {
 				additional = "WHERE type = 'Co-Occurrence'";
 			}
 			findQuery = "SELECT name FROM " 
 				+ RationaleDBUtil.escapeTableName(tableName) + " " 
 				+ additional 
 				+ "WHERE upper(name) LIKE" + " upper('%" + sstring + "%')"
 				+ " ORDER BY name ASC";
 			System.out.println(findQuery);
 			rs = stmt.executeQuery(findQuery);
 
 			while (rs.next()) {
 				ourElements
 				.addElement(RationaleDBUtil.decode(rs.getString("name")));
 			}
 
 		} catch (SQLException ex) {
 			reportError(ex, "Error in getNameList with Search String", findQuery);
 		} finally {
 			releaseResources(stmt, rs);
 		}
 		return ourElements;
 
 	}
 	/**
 	 * Find out if a  particular element exists and has the passed in name
 	 * @param name - the name of the element
 	 * @param type - the type of element we are looking for
 	 * @return - the list of names
 	 */
 	public static boolean elementExists(String name, RationaleElementType type) {
 		
 		boolean exists;
 		exists = false;
 		int id = -1;
 		
 		Vector<String> ourElements = new Vector<String>();
 		String tableName = RationaleDB.getTableName(type);
 		String findQuery = "";
 		Statement stmt = null;
 		ResultSet rs = null;
 		try {
 			stmt = conn.createStatement();
 			findQuery = "SELECT id FROM " 
 				+ RationaleDBUtil.escapeTableName(tableName) + " " 
 				+ "WHERE name = '" +
 				name + "' ";
 			System.out.println(findQuery);
 			rs = stmt.executeQuery(findQuery);
 
 			while (rs.next()) {
 				int nextID = rs.getInt("id");
 				exists = true;
 			}
 			rs.close();
 
 		} catch (SQLException ex) {
 			reportError(ex, "Error in elementExists", findQuery);
 		} finally {
 			releaseResources(stmt, rs);
 		}
 		return exists;
 
 	}
 	/**
 	 * Get a list of all "potential new parents" for a particular type of candidate rationale element
 	 * @param type - the type of element whose new parent we are looking for
 	 * @return - the list of names
 	 */
 	public Vector<String> getCandidateList(RationaleElementType type) {
 		Vector<String> ourElements = new Vector<String>();
 		String findQuery = "";
 		Statement stmt = null;
 		ResultSet rs = null;
 		try {
 			stmt = conn.createStatement();
 			if (type == RationaleElementType.ALTERNATIVE)
 			{
 				findQuery = "SELECT name FROM candidates where type='Decision'";
 			}
 			else if (type == RationaleElementType.ARGUMENT)
 			{
 				findQuery = "SELECT name FROM candidates where type='Requirement' or type='Alternative'";	
 			}
 			else if (type == RationaleElementType.ASSUMPTION)
 			{
 				findQuery = "SELECT name FROM candidates where type='Argument'";
 			}
 			else if (type == RationaleElementType.QUESTION)
 			{
 				findQuery = "SELECT name FROM candidates where type='Decision' or type='Alternative'";	
 			}
 			else
 			{
 				return null;
 			}
 			System.out.println(findQuery);
 			rs = stmt.executeQuery(findQuery);
 			
 			while (rs.next()) {
 				ourElements
 				.addElement(RationaleDBUtil.decode(rs.getString("name")));
 			}
 			
 		} catch (SQLException ex) {
 			reportError(ex, "Error in getNameList", findQuery);
 		} finally {
 			releaseResources(stmt, rs);
 		}
 		return ourElements;
 	}
 	
 	/**
 	 * Get a list of all "potential new parents" for a particular type of rationale element
 	 * @param type - the type of element whose new parent we are looking for
 	 * @return - the list of names
 	 */
 	public Vector<String> getRationaleElementList(RationaleElementType type) {
 		Vector<String> ourElements = new Vector<String>();
 		String findQuery = "";
 		Statement stmt = null;
 		ResultSet rs = null;
 		try {
 			stmt = conn.createStatement();
 			if (type == RationaleElementType.ALTERNATIVE)
 			{
 				findQuery = "SELECT name FROM decisions";
 			}
 /*			else if (type == RationaleElementType.ARGUMENT)
 			{
 				findQuery = "(SELECT name FROM requirements) union (select name FROM alternatives)";	
 			} */
 			else if (type == RationaleElementType.REQUIREMENT)
 			{
 				findQuery = "SELECT name FROM requirements";	
 			}
 			else if (type == RationaleElementType.DECISION)
 			{
 				findQuery = "select name FROM alternatives";
 			}
 			else if (type == RationaleElementType.QUESTION)
 			{
 				findQuery = "SELECT decisions.name,alternatives.name FROM decisions ,alternatives";	
 			}
 			else
 			{
 				return null;
 			}
 			System.out.println(findQuery);
 			rs = stmt.executeQuery(findQuery);
 			
 			while (rs.next()) {
 				ourElements
 				.addElement(RationaleDBUtil.decode(rs.getString("name")));
 			}
 			
 		} catch (SQLException ex) {
 			reportError(ex, "Error in getNameList", findQuery);
 		} finally {
 			releaseResources(stmt, rs);
 		}
 		return ourElements;	
 	}
 	
 	/**
 	 * Get a list of all "potential new parents" for Argument type of rationale element
 	 * @param type - the type of element whose new parent we are looking for
 	 * @param ptype - parent's the type of element
 	 * @return - the list of names
 	 */
 	public Vector<String> getRationaleElementList(RationaleElementType type, RationaleElementType ptype ) {
 		Vector<String> ourElements = new Vector<String>();
 		String findQuery = "";
 		Statement stmt = null;
 		ResultSet rs = null;
 		try {
 			stmt = conn.createStatement();
 			
 			if (type == RationaleElementType.ARGUMENT && ptype== RationaleElementType.REQUIREMENT)
 			{
 				findQuery = "SELECT name FROM requirements";	
 			}
 			if (type == RationaleElementType.ARGUMENT && ptype== RationaleElementType.ALTERNATIVE)
 			{
 				findQuery = "select name FROM alternatives";	
 			}
 			if (type == RationaleElementType.DECISION && ptype== RationaleElementType.DECISION)
 			{
 				findQuery = "SELECT name FROM decisions";	
 			}
 			if (type == RationaleElementType.DECISION && ptype== RationaleElementType.ALTERNATIVE)
 			{
 				findQuery = "select name FROM alternatives";	
 			}
 			System.out.println(findQuery);
 			rs = stmt.executeQuery(findQuery);
 			
 			while (rs.next()) {
 				ourElements
 				.addElement(RationaleDBUtil.decode(rs.getString("name")));
 			}
 			
 		} catch (SQLException ex) {
 			reportError(ex, "Error in getNameList", findQuery);
 		} finally {
 			releaseResources(stmt, rs);
 		}
 		return ourElements;
 	}
 	
 	
 	/**
 	 * Returns a list of TreeParent items (to put in the tree from the RationaleExplorer)
 	 * from the database given the parent name, the element type, and the parent type.
 	 * @param parentName - parent name
 	 * @param elementType - type of element we are looking for (table name)
 	 * @param parentType - parents element type
 	 * @return a list of tree elements
 	 */
 	private Vector<TreeParent> getTreeElements(String parentName, String elementType,
 			RationaleElementType parentType) {
 
 		Vector<TreeParent> treeElementList = new Vector<TreeParent>();
 		String findQuery = "";
 		Statement stmt = null;
 		ResultSet rs = null;
 		boolean error = false;
 		try {
 			stmt = conn.createStatement();
 			if (parentType == null) {
 				findQuery = "SELECT name FROM " 
 					+ RationaleDBUtil.escapeTableName(elementType)
 					+ " ORDER BY name ASC";
 			} else {
 				//first, lets get the ID for the parent
 				int pid = 0;
 				findQuery = "SELECT id FROM " 
 					+ RationaleDBUtil.escapeTableName(getTableName(parentType))
 					+ "  WHERE name= '" + RationaleDBUtil.escape(parentName)
 					+ "'";
 				rs = stmt.executeQuery(findQuery);
 				if (rs.next()) {
 					pid = rs.getInt("id");
 					rs.close();
 				} else {
 					error = true;
 					System.out.println(findQuery);
 					System.out.println("No matching " + elementType
 							+ " for name " + parentName);
 					return (new Vector<TreeParent>());
 				}
 
 				if (parentType == null) {
 					findQuery = "SELECT name FROM " 
 						+ RationaleDBUtil.escapeTableName(elementType) + " "
 						+ " WHERE parent = " + new Integer(pid).toString()
 						+ " ORDER BY name ASC";
 				} else {
 					findQuery = "SELECT name FROM " 
 						+ RationaleDBUtil.escapeTableName(elementType) + " "
 						+ " WHERE parent = " + new Integer(pid).toString()
 						+ " AND ptype = '" + parentType.toString()
 						+ "' ORDER BY name ASC";
 				}
 
 			}
 			if (error) {
 				//				 System.out.println(findQuery);
 			}
 			rs = stmt.executeQuery(findQuery);
 
 			while (rs.next()) {
 				TreeParent element = new TreeParent(RationaleDBUtil.decode(rs
 						.getString("name")), getElementType(elementType));
 				treeElementList.addElement(element);
 			}
 			//now, need to enumerate over the list of items and add
 			//their status information *if* this is a type that has status.
 
 			RationaleElementType type = getElementType(elementType);
 
 			if ((type == RationaleElementType.REQUIREMENT)
 					|| (type == RationaleElementType.DECISION)
 					|| (type == RationaleElementType.ALTERNATIVE)) {
 
 				Enumeration stats = treeElementList.elements();
 				while (stats.hasMoreElements()) {
 					TreeParent element = (TreeParent) stats.nextElement();
 					RationaleErrorLevel ourStatus = getActiveStatus(element
 							.getName(), type);
 					element.setStatus(ourStatus);
 				}
 			}
 
 		} catch (SQLException ex) {
 			if (ex.getErrorCode() == 1146) {
 				reportError(ex, "getTreeElements - could not find table",
 						findQuery);
 			} else {
 				reportError(ex, "ERror in getTreeELements", findQuery);
 			}
 		} finally {
 			releaseResources(stmt, rs);
 		}
 		return treeElementList;
 	}
 
 	/**
 	 * Returns a list of CandidateTreeParent items (to put in the tree from the RationaleExplorer) 
 	 * from the database given the element type
 	 * @param elementType - type of element we are looking for
 	 * @return a list of tree elements
 	 */
 	public Vector<CandidateTreeParent> getCandidateTreeElements(RationaleElementType etype) {
 		
 		Vector<CandidateTreeParent> treeElementList = new Vector<CandidateTreeParent>();
 		String findQuery = "";
 		Statement stmt = null;
 		ResultSet rs = null;
 		boolean error = false;
 		try {
 			stmt = conn.createStatement();
 			findQuery = "Select name FROM candidates where type = '" + 
 			etype.toString() + "'";
 			rs = stmt.executeQuery(findQuery);
 			while (rs.next()) {
 				String name = RationaleDBUtil.decode(rs.getString("name"));
 				CandidateTreeParent treeE = new CandidateTreeParent(name, etype);
 				treeElementList.add(treeE);
 			}
 
 			
 		} catch (SQLException ex) {
 			if (ex.getErrorCode() == 1146) {
 				reportError(ex, "getTreeElements - could not find table",
 						findQuery);
 			} else {
 				reportError(ex, "ERror in getTreeELements", findQuery);
 			}
 		} finally {
 			releaseResources(stmt, rs);
 		}
 		return treeElementList;
 	}
 	
 
 	/**
 	 * Given an alternative, find all the related constraints.
 	 * @param parentName - the name of the parent alternative
 	 * @param parentType - it is not clear why this is needed!
 	 * Probably a cut-and-paste artifact...
 	 * @return a list of constraints in tree element form
 	 */
 	public Vector<TreeParent> getAltConstRels(String parentName,
 			RationaleElementType parentType) {
 
 		Vector<TreeParent> relationshipList = new Vector<TreeParent>();
 		String findQuery = "";
 		Statement stmt = null;
 		ResultSet rs = null;
 		boolean error = false;
 		try {
 			stmt = conn.createStatement();
 			if (parentType == null) {
 				findQuery = "SELECT name FROM "
 					+ RationaleDBUtil.escapeTableName("AltConstRel") + " " 
 					+ " ORDER BY name ASC";
 			} else {
 				//first, lets get the ID for the parent
 				int pid = 0;
 				findQuery = "SELECT id FROM " 
 					+ RationaleDBUtil.escapeTableName(getTableName(parentType)) + " "
 					+ "  WHERE name= '" + RationaleDBUtil.escape(parentName)
 					+ "'";
 				//
 				rs = stmt.executeQuery(findQuery);
 				if (rs.next()) {
 					pid = rs.getInt("id");
 					rs.close();
 				} else {
 					error = true;
 					System.out.println(findQuery);
 					System.out.println("No matching " + "constraint rel "
 							+ " for name " + parentName);
 					return (new Vector<TreeParent>());
 				}
 
 				findQuery = "SELECT name FROM " 
 					+ RationaleDBUtil.escapeTableName("altconstrel")
 					+ " WHERE alternative = " + new Integer(pid).toString()
 					+ " ORDER BY name ASC";
 
 			}
 			if (error) {
 				//				 System.out.println(findQuery);
 			}
 			rs = stmt.executeQuery(findQuery);
 
 			while (rs.next()) {
 				TreeParent element = new TreeParent(RationaleDBUtil.decode(rs
 						.getString("name")), RationaleElementType.ALTCONSTREL);
 				relationshipList.addElement(element);
 			}
 
 		} catch (SQLException ex) {
 			reportError(ex, "ERror in getTreeELements", findQuery);
 		} finally {
 			releaseResources(stmt, rs);
 		}
 		return relationshipList;
 
 	}
 
 	/**
 	 * Find out what error level an element has (error, warning, information). If
 	 * there is more than one error, return the most severe level
 	 * @param name - the name of the rationale element
 	 * @param elementType - the type of the rationale element
 	 * @return the error level
 	 */
 	public RationaleErrorLevel getActiveStatus(String name,
 			RationaleElementType elementType) {
 		String findQuery = "";
 		RationaleErrorLevel worst;
 		Statement stmt = null;
 		ResultSet rs = null;
 		//Assume least severe error level to start
 		worst = RationaleErrorLevel.INFORMATION;
 		//just in case
 		name = RationaleDBUtil.escape(name);
 		try {
 			stmt = conn.createStatement();
 			;
 			findQuery = "SELECT S.type from "
 				+ RationaleDBUtil.escapeTableName("status") + " S, "
 				+ RationaleDBUtil.escapeTableName(getTableName(elementType))
 				+ " P" + " where P.name = '"
 				+ name + "' and " + "P.id = S.parent " + "and S.ptype = '"
 				+ elementType.toString() + "' and override = 'No'";
 
 			//***	 System.out.println(findQuery);
 			rs = stmt.executeQuery(findQuery);
 
 			while (rs.next()) {
 				//				System.out.println("found element");
 				//				System.out.println("type = " + rs.getString("type"));
 				RationaleErrorLevel newStat = RationaleErrorLevel.fromString(rs
 						.getString("type"));
 				if (worst == RationaleErrorLevel.INFORMATION) {
 					worst = newStat;
 				} else if (worst == RationaleErrorLevel.WARNING) {
 					if (newStat == RationaleErrorLevel.ERROR) {
 						worst = newStat;
 					}
 				}
 			}
 
 		} catch (SQLException ex) {
 			reportError(ex, "SQLException, get status: " + ex.getMessage(),
 					findQuery);
 		} finally {
 			releaseResources(stmt, rs);
 		}
 		return worst;
 
 	}
 	/*
 	 public RationaleErrorLevel getStatus(String name,
 	 RationaleElementType elementType) {
 	 String findQuery = "";
 	 RationaleErrorLevel worst;
 	 Statement stmt = null;
 	 ResultSet rs = null;
 	 worst = RationaleErrorLevel.INFORMATION;
 	 //just in case
 	  name = RationaleDB.escape(name);
 	  try {
 	  stmt = conn.createStatement();
 	  ;
 	  findQuery = "SELECT S.type from status S, "
 	  + getTableName(elementType) + " P" + " where P.name = '"
 	  + name + "' and " + "P.id = S.parent " + "and S.ptype = '"
 	  + elementType.toString() + "'";
 	  //***	 System.out.println(findQuery);
 	   rs = stmt.executeQuery(findQuery);
 
 	   while (rs.next()) {
 	   //				System.out.println("found element");
 	    //				System.out.println("type = " + rs.getString("type"));
 	     RationaleErrorLevel newStat = RationaleErrorLevel.fromString(rs
 	     .getString("type"));
 	     if (worst == RationaleErrorLevel.INFORMATION) {
 	     worst = newStat;
 	     } else if (worst == RationaleErrorLevel.WARNING) {
 	     if (newStat == RationaleErrorLevel.ERROR) {
 	     worst = newStat;
 	     }
 	     }
 	     }
 
 	     } catch (SQLException ex) {
 	     reportError(ex, "SQLException, get status: " + ex.getMessage(),
 	     findQuery);
 	     } finally {
 	     releaseResources(stmt, rs);
 	     }
 	     return worst;
 
 	     }
 	     */
 
 
 	/**
 	 * Given an element ID and type, find out if it is enabled (active) or not
 	 * @param pid - the element ID
 	 * @param elementType - the type
 	 * @return true if the element is enabled
 	 */
 	public boolean getActive(int pid, RationaleElementType elementType) {
 		RationaleElement ourElement = getRationaleElement(pid, elementType);
 		return ourElement.getEnabled();
 	}
 
 	/**
 	 * Find the rationale error level given an element ID and type. Seems a bit redundant
 	 * with ActiveStatus which uses the name and type...
 	 * @param pid - the element ID
 	 * @param elementType - the element type
 	 * @return - the level of error
 	 */
 	public RationaleErrorLevel getStatusLevel(int pid,
 			RationaleElementType elementType) {
 		RationaleErrorLevel worst;
 		Statement stmt = null;
 		ResultSet rs = null;
 		String findQuery = "";
 		worst = RationaleErrorLevel.NONE;
 		try {
 			stmt = conn.createStatement();
 			;
 			findQuery = "SELECT S.type from " 
 				+ RationaleDBUtil.escapeTableName("status") + " S, "
 				+ RationaleDBUtil.escapeTableName(getTableName(elementType))
 				+ " P where " + "S.parent = "
 				+ new Integer(pid).toString() + " and S.ptype = '"
 				+ elementType.toString() + "' and S.override = 'No'";
 			//***		 System.out.println(findQuery);
 			rs = stmt.executeQuery(findQuery);
 
 			while (rs.next()) {
 				//***				System.out.println("found element");
 				//***				System.out.println("type = " + rs.getString("type"));
 				RationaleErrorLevel newStat = RationaleErrorLevel.fromString(rs
 						.getString("type"));
 				if ((worst == RationaleErrorLevel.NONE)
 						|| (worst == RationaleErrorLevel.INFORMATION)) {
 					worst = newStat;
 				} else if (worst == RationaleErrorLevel.WARNING) {
 					if (newStat == RationaleErrorLevel.ERROR) {
 						worst = newStat;
 					}
 				}
 			}
 
 		} catch (SQLException ex) {
 			reportError(ex, "Error in getStatusLevel", findQuery);
 		} finally {
 			releaseResources(stmt, rs);
 		}
 		return worst;
 
 	}
 
 
 	/**
 	 * Get a list of all elements that are tradeoffs or cooccurrences
 	 * @return a vector of tradeoff elements
 	 */
 	public Vector<Tradeoff> getTradeoffData() {
 		Vector<String> tradeoffNames = new Vector<String>();
 		Vector<Tradeoff> tradeoffList = new Vector<Tradeoff>();
 
 		tradeoffNames = getTradeoffs(RationaleElementType.TRADEOFF);
 		tradeoffNames.addAll(getTradeoffs(RationaleElementType.COOCCURRENCE));
 
 		Iterator tradeI = tradeoffNames.iterator();
 		while (tradeI.hasNext()) {
 			Tradeoff ourTrade = new Tradeoff();
 			ourTrade.fromDatabase((String) tradeI.next());
 			tradeoffList.add(ourTrade);
 		}
 		return tradeoffList;
 	}
 
 	/**
 	 * Get the names of all the tradeoffs of a specific type (tradeoff or
 	 * cooccurrence relationship)
 	 * @param type - the type
 	 * @return a list of names
 	 */
 	public Vector<String> getTradeoffs(RationaleElementType type) {
 
 		Vector<String> nameList = new Vector<String>();
 		String findQuery = "";
 		Statement stmt = null;
 		ResultSet rs = null;
 		try {
 			stmt = conn.createStatement();
 			findQuery = "SELECT name FROM " 
 				+ RationaleDBUtil.escapeTableName("tradeoffs") + " "
 				+ " WHERE type = '" + type.toString() + "' ORDER BY name ASC";
 
 			rs = stmt.executeQuery(findQuery);
 
 			while (rs.next()) {
 				nameList.addElement(RationaleDBUtil.decode(rs.getString("name")));
 			}
 		} catch (SQLException ex) {
 			reportError(ex, "Error in getTradeoffs", findQuery);
 		} finally {
 			releaseResources(stmt, rs);
 		}
 		return nameList;
 	}
 
 	/**
 	 * Get all the status items from the database so they can be displayed on
 	 * the Task List
 	 * @return the current status elements
 	 */
 	public Vector<RationaleStatus> getStatus() {
 
 		Vector<RationaleStatus> statusList = new Vector<RationaleStatus>();
 		String findQuery = "";
 		Statement stmt = null;
 		ResultSet rs = null;
 		try {
 			stmt = conn.createStatement();
 			findQuery = "SELECT *  FROM " 
 				+ RationaleDBUtil.escapeTableName("status")
 				+ " ORDER BY description ASC";
 			//***		System.out.println(findQuery);
 			rs = stmt.executeQuery(findQuery);
 
 			while (rs.next()) {
 				RationaleStatus rstat = new RationaleStatus(RationaleErrorLevel
 						.fromString(rs.getString("type")), RationaleDBUtil
 						.decode(rs.getString("description")),
 						RationaleElementType.fromString(rs.getString("ptype")),
 						rs.getDate("date"), rs.getInt("parent"),
 						RationaleStatusType.fromString(rs.getString("status")));
 				if (rs.getString("override").compareTo("Yes") == 0) {
 					rstat.setOverride(true);
 				}
 				statusList.add(rstat);
 			}
 
 		} catch (SQLException ex) {
 			reportError(ex, "error in getStatus", findQuery);
 		} finally {
 			releaseResources(stmt, rs);
 		}
 		return statusList;
 	}
 
 	/**
 	 * Override the current status of an element in the database. This is done
 	 * if the user feels that an error or warning is not actually a problem and
 	 * they do not want to see it appear on the list of errors and warnings. Of course,
 	 * this can easily be reversed later.
 	 * @param descrip - the error/warning description
 	 * @param parent - the element the error/warning is about
 	 * @param ptype - the type of element the error/warning is about
 	 */
 	public void overrideStatus(String descrip, int parent, String ptype) {
 		Statement stmt = null;
 		ResultSet rs = null;
 		String findQuery = "";
 		try {
 			stmt = conn.createStatement();
 
 			//who is our parent?
 			findQuery = "SELECT status  FROM " 
 				+ RationaleDBUtil.escapeTableName("status")
 				+ " WHERE parent = "
 			+ new Integer(parent).toString() + " and ptype = '" + ptype
 			+ "'" + " and description = '"
 			+ RationaleDBUtil.escape(descrip) + "'";
 			System.out.println(findQuery);
 			rs = stmt.executeQuery(findQuery);
 
 			if (rs.next()) {
 				String stat = rs.getString("status");
 				String updateQuery;
 				updateQuery = "UPDATE " 
 					+ RationaleDBUtil.escapeTableName("Status") + " " 
 					+ "SET override = 'Yes' "
 					+ "WHERE status = '" + stat + "' AND parent = "
 					+ new Integer(parent).toString() + " AND ptype = '"
 					+ ptype + "'";
 				System.out.println(updateQuery);
 				stmt.execute(updateQuery);
 			}
 		} catch (SQLException ex) {
 			reportError(ex, "Error in OverrideStatus", findQuery);
 		} finally {
 			releaseResources(stmt, rs);
 
 		}
 	}
 
 	/**
 	 * Given an ID and type, get the rationale element's status
 	 * @param ID - the id
 	 * @param type - the type of element
 	 * @return the status
 	 */
 	public Vector<RationaleStatus> getStatus(int ID, RationaleElementType type) {
 
 		Vector<RationaleStatus> statusList = new Vector<RationaleStatus>();
 		String findQuery = "";
 		Statement stmt = null;
 		ResultSet rs = null;
 		try {
 			stmt = conn.createStatement();
 			findQuery = "SELECT *  FROM " 
 				+ RationaleDBUtil.escapeTableName("status") + " " 
 				+ " WHERE parent = "
 				+ new Integer(ID).toString() + " and ptype = '"
 				+ type.toString() + "'";
 			//***		System.out.println(findQuery);
 			rs = stmt.executeQuery(findQuery);
 
 			while (rs.next()) {
 				RationaleStatus rstat = new RationaleStatus(RationaleErrorLevel
 						.fromString(rs.getString("type")), RationaleDBUtil
 						.decode(rs.getString("description")),
 						RationaleElementType.fromString(rs.getString("ptype")),
 						rs.getDate("date"), rs.getInt("parent"),
 						RationaleStatusType.fromString(RationaleDBUtil.decode(rs
 								.getString("status"))));
 				if (rs.getString("override").compareTo("Yes") == 0) {
 					rstat.setOverride(true);
 				}
 				statusList.add(rstat);
 			}
 
 		} catch (SQLException ex) {
 			reportError(ex, "Error in getStatus2", findQuery);
 		} finally {
 			releaseResources(stmt, rs);
 		}
 		return statusList;
 	}
 
 	/**
 	 * Get all the rationale status elements from the database that have been
 	 * overriden by the user.
 	 * @return the status overrides
 	 */
 	public Vector<RationaleStatus> getOverrides() {
 
 		Vector<RationaleStatus> statusList = new Vector<RationaleStatus>();
 		String findQuery = "";
 		Statement stmt = null;
 		ResultSet rs = null;
 		try {
 			stmt = conn.createStatement();
 			findQuery = "SELECT *  FROM " 
 				+ RationaleDBUtil.escapeTableName("status") + " "
 				+ " WHERE override = 'Yes'";
 			//***		System.out.println(findQuery);
 			rs = stmt.executeQuery(findQuery);
 
 			while (rs.next()) {
 
 				RationaleStatus rstat = new RationaleStatus(RationaleErrorLevel
 						.fromString(rs.getString("type")), RationaleDBUtil
 						.decode(rs.getString("description")),
 						RationaleElementType.fromString(rs.getString("ptype")),
 						rs.getDate("date"), rs.getInt("parent"),
 						RationaleStatusType.fromString(RationaleDBUtil.decode(rs
 								.getString("status"))));
 				rstat.setOverride(true);
 
 				statusList.add(rstat);
 			}
 
 		} catch (SQLException ex) {
 			reportError(ex, "Error in getOverrides", findQuery);
 		} finally {
 			releaseResources(stmt, rs);
 		}
 		return statusList;
 	}
 
 	/**
 	 * Add a new status elements to the database. This is done because
 	 * SEURAT detected problems with the rationale.
 	 * @param newStatus - the status element
 	 */
 	public void addStatus(Vector<RationaleStatus> newStatus) {
 		Iterator statI = newStatus.iterator();
 		while (statI.hasNext()) {
 			RationaleStatus stat = (RationaleStatus) statI.next();
 			if (stat.getParent() <= 0) {
 				System.out.println("error in status value");
 			} else {
 				stat.toDatabase(stat.getParent());
 			}
 
 		}
 	}
 
 	/**
 	 * Remove status elements from the database (presumably because the
 	 * problem with the rationale has been fixed).
 	 * @param oldStatus - the status element to remove
 	 */
 	public void removeStatus(Vector<RationaleStatus> oldStatus) {
 		String updateStr = "";
 		Iterator statI = oldStatus.iterator();
 		while (statI.hasNext()) {
 			RationaleStatus stat = (RationaleStatus) statI.next();
 			Statement stmt = null;
 			try {
 				stmt = conn.createStatement();
 				updateStr = "DELETE from " 
 					+ RationaleDBUtil.escapeTableName("STATUS") + " "
 					+ " WHERE parent = "
 					+ new Integer(stat.getParent()).toString() + " and "
 					+ "ptype = '" + stat.getRationaleType().toString()
 					+ "' and status = '" + stat.getStatusType().toString()
 					+ "' and description = '"
 					+ RationaleDBUtil.escape(stat.getDescription()) + "'";
 				//			System.out.println(updateStr);
 				stmt.execute(updateStr);
 
 			} catch (SQLException ex) {
 				reportError(ex, "Error in removeStatus", updateStr);
 			} finally {
 				releaseResources(stmt, null);
 			}
 		} //end while
 	}
 
 	/**
 	 * Associate an alternative with a resource. In Eclipse, a resource can
 	 * be a file, attribute, or method. This information is actually maintained
 	 * using markers (bookmarks and special markers to put the rat icon on the file) but
 	 * we also store it in the database so we can share it between machines.  The database
 	 * schema also includes the line number, but this is useless in helping us restore
 	 * the markers between sessions, so the method no longer uses it.
 	 * @param alternative - the ID of the alternative being associated
 	 * @param artifact - the Eclipse format artifact identifier
 	 * @param resource - the file name for the resource - this is what gets displayed in the bookmark
 	 * @param name - the name of the method or attribute we are associating with
 	 * @param message - the name of the alternative that is displayed
 	 */
 	public void associateAlternative(String alternative, String artifact,
 			String resource, String name, String message) {
 		String getAltID = "";
 		String insertStr = "";
 		Statement stmt = null;
 		ResultSet rs = null;
 		//just in case
 		alternative = RationaleDBUtil.escape(alternative);
 		name = RationaleDBUtil.escape(name);
 		try {
 			stmt = conn.createStatement();
 			getAltID = "SELECT id from " 
 				+ RationaleDBUtil.escapeTableName("Alternatives") + " "
 				+ " where name = '"
 				+ alternative + "'";
 			rs = stmt.executeQuery(getAltID);
 			if (rs.next()) {
 				int altID = rs.getInt("id");
 				// TODO get alternative object here so that it can be used in an update event
 				insertStr = "Insert into "
 					+ RationaleDBUtil.escapeTableName("Associations") + " "
 					+ "(alternative, artifact, artresource, artname, assocmessage) "
 					+ "Values (" + Integer.toString(altID) + ", '"
 					+ artifact + "', '" + resource + "', '" + name + "', '"
 					+ RationaleDBUtil.escape(message) + "')";
 				//				System.out.println(insertStr);
 				stmt.execute(insertStr);
 			}
 
 		} catch (SQLException ex) {
 			reportError(ex, "Error in associate alternative", getAltID + " or "
 					+ insertStr);
 		} finally {
 			releaseResources(stmt, rs);
 		}
 	}
 
 	/**
 	 * Given the Eclipse format resource, delete all associations to that resource
 	 * from the associations table. This will remove all the associations to a file.
 	 * @param resource - the artifact to remove
 	 */
 	public void removeAssociation(String resource) {
 
 		Statement stmt = null;
 		String removeAssoc = "";
 		try {
 			stmt = conn.createStatement();
 			// TODO get alternative object here so that it can be used in an update event
 			removeAssoc = "Delete from Associations where artresource = '"
 				+ resource + "'";
 			stmt.execute(removeAssoc);
 
 		} catch (SQLException ex) {
 			reportError(ex, "Error in removeAssociation", removeAssoc);
 		} finally {
 			releaseResources(stmt, null);
 		}
 	}
 
 	/**
 	 * Removes associations of a specific alternative to a specific file at
 	 * a specific line number. This is very brittle - if the line number comes from a bookmark
 	 * and the file has been edited it will not match the database!
 	 * @param resource - the file name where the association is
 	 * @param message - the name of the alternative it was associated with
 	 * @param lineNumber - the specific line number
 	 */
 	public void removeAssociation(String resource, String message,
 			int lineNumber) {
 		//TODO May not be necessary anymore? candidate for cleanup -molerjc
 		String removeAssoc = "";
 		Statement stmt = null;
 		try {
 			stmt = conn.createStatement();
 			removeAssoc = "Delete from " 
 				+ RationaleDBUtil.escapeTableName("Associations") + " " 
 				+ " where artresource = '"
 				+ resource + "' and assocmessage = '"
 				+ RationaleDBUtil.escape(message) + "'";
 			System.out.println(removeAssoc);
 			stmt.execute(removeAssoc);
 
 		} catch (SQLException ex) {
 			reportError(ex, "Error in removeAssociation", removeAssoc);
 		} finally {
 			releaseResources(stmt, null);
 		}
 	}
 
 	/**
 	 * Remove all status records for a specific rationale element.
 	 * @param id - the ID of the element
 	 * @param ptype - the type of the element
 	 */
 	public void clearStatus(int id, RationaleElementType ptype) {
 		String deleteCmd = "";
 		//make sure we are of a type that has status!
 		if (!((ptype == RationaleElementType.REQUIREMENT)
 				|| (ptype == RationaleElementType.DECISION) || (ptype == RationaleElementType.ALTERNATIVE))) {
 			return;
 		}
 		Connection conn = getConnection();
 
 		Statement stmt = null;
 
 		//		System.out.println("Clearing out status");
 
 		try {
 			stmt = conn.createStatement();
 			deleteCmd = "DELETE FROM "
 				+ RationaleDBUtil.escapeTableName("status") + " "
 				+ " where parent = "
 				+ new Integer(id).toString() + " and ptype = '"
 				+ ptype.toString() + "'";
 			//			 System.out.println(deleteCmd);
 			stmt.execute(deleteCmd);
 
 		} catch (SQLException ex) {
 			reportError(ex, "Error in clearStatus", deleteCmd);
 		}
 
 		finally {
 			releaseResources(stmt, null);
 		}
 
 	}
 
 	/**
 	 * Clear out the rationale history record for a specific element
 	 * @param id - the element ID
 	 * @param ptype - the element type
 	 */
 	public void clearHistory(int id, RationaleElementType ptype) {
 
 		//make sure we are of a type that has status!
 		if (!((ptype == RationaleElementType.REQUIREMENT)
 				|| (ptype == RationaleElementType.DECISION)
 				|| (ptype == RationaleElementType.ALTERNATIVE) || (ptype == RationaleElementType.QUESTION))) {
 			return;
 		}
 		Connection conn = getConnection();
 		String deleteCmd = "";
 		Statement stmt = null;
 
 		//		System.out.println("Clearing out history");
 
 		try {
 			stmt = conn.createStatement();
 			deleteCmd = "DELETE FROM " 
 				+ RationaleDBUtil.escapeTableName("history") + " "
 				+ " where parent = "
 				+ new Integer(id).toString() + " and ptype = '"
 				+ ptype.toString() + "'";
 			///			 System.out.println(deleteCmd);
 			stmt.execute(deleteCmd);
 
 		} catch (SQLException ex) {
 			reportError(ex, "Error in clear history", deleteCmd);
 		}
 
 		finally {
 			releaseResources(stmt, null);
 
 		}
 
 	}
 
 	/**
 	 * Remove an element from the database. This includes clearing out
 	 * the history but doesn't clear out the status.
 	 * @param ele - the rationale element to remove.
 	 */
 	public void deleteRationaleElement(RationaleElement ele) {
 		//first, clear out our status
 		int id = ele.getID();
 
 		/* don't clear out status -- we need this to be handled by explorer
 		 clearStatus(id, ele.getElementType());
 		 */
 		//clear our history
 		clearHistory(id, ele.getElementType());
 
 		//now, delete ourself from the database
 		Connection conn = getConnection();
 		String deleteCmd = "";
 		Statement stmt = null;
 
 		//		System.out.println("Deleting rationale from the database");
 
 		try {
 			stmt = conn.createStatement();
 			deleteCmd = "DELETE FROM "
 				+ RationaleDBUtil.escapeTableName(this.getTableName(ele.getElementType()))
 				+ " where id = "
 				+ new Integer(id).toString();
 			//		 System.out.println(deleteCmd);
 			stmt.execute(deleteCmd);
 
 		} catch (SQLException ex) {
 			reportError(ex, "Error in deleteRationaleElement", deleteCmd);
 		}
 
 		finally {
 			releaseResources(stmt, null);
 		}
 
 	}
 	
 	/** 
 	* Remove a rationale candidate from the database. This can't use the generic
 	* deleteRationaleElement because the type doesn't signify the table name. 
 	 * @param ele - the rationale element to remove.
 	 */
 	public void deleteCandidateRationaleElement(CandidateRationale ele) {
 
 		int id = ele.getID();
 		
 		//now, delete ourself from the database
 		Connection conn = getConnection();
 		String deleteCmd = "";
 		Statement stmt = null;
 		
 		//		System.out.println("Deleting rationale from the database");
 		
 		try {
 			stmt = conn.createStatement();
 			deleteCmd = "DELETE FROM Candidates where id = "
 				+ new Integer(id).toString();
 			//		 System.out.println(deleteCmd);
 			stmt.execute(deleteCmd);
 			
 		} catch (SQLException ex) {
 			reportError(ex, "Error in deleteRationaleElement", deleteCmd);
 		}
 		
 		finally {
 			releaseResources(stmt, null);
 		}
 		
 	}
 
 	/**
 	 * Find out how many arguments use a particular element (claim, assumption,
 	 * or requirement)
 	 * @param ele - the element of interest
 	 * @return the number of arguments that refer to it
 	 */
 	public int countArgReferences(RationaleElement ele) {
 		int numReferences = 0;
 
 		//get our ID
 		int id = ele.getID();
 		RationaleElementType type = ele.getElementType();
 
 		//get the field name
 		String argField = new String();
 		if (type == RationaleElementType.CLAIM) {
 			argField = "claim";
 		} else if (type == RationaleElementType.ASSUMPTION) {
 			argField = "assumption";
 		} else if (type == RationaleElementType.REQUIREMENT) {
 			argField = "requirement";
 		}
 		//now, find out how often we occur
 		Connection conn = getConnection();
 		ResultSet rs = null;
 
 		String findCmd = "";
 		Statement stmt = null;
 
 		//		System.out.println("Counting arguments");
 
 		try {
 			stmt = conn.createStatement();
 			findCmd = "Select id FROM "
 				+ RationaleDBUtil.escapeTableName("arguments") + " "
 				+ " where " + argField + " = "
 			+ new Integer(id).toString();
 			//***			 System.out.println(findCmd);
 			rs = stmt.executeQuery(findCmd);
 
 			while (rs.next()) {
 				numReferences++;
 			}
 
 		} catch (SQLException ex) {
 			reportError(ex, "Error in countArgReferences", findCmd);
 		}
 
 		finally {
 			releaseResources(stmt, rs);
 		}
 
 		return numReferences;
 	}
 
 	/**
 	 * Get all the claims that have an importance that overrides the default
 	 * inherited from the Argument Ontology
 	 * @return the overridden claims
 	 */
 	public Vector<Claim> getOverridenClaims() {
 
 		Vector<Claim> claimList = new Vector<Claim>();
 		String findQuery = "";
 		Statement stmt = null;
 		ResultSet rs = null;
 		try {
 			stmt = conn.createStatement();
 			findQuery = "SELECT *  FROM " 
 				+ RationaleDBUtil.escapeTableName("claims") + " "
 				+ " WHERE importance <> '"
 			+ Importance.DEFAULT.toString() + "'";
 			//***		System.out.println(findQuery);
 			rs = stmt.executeQuery(findQuery);
 
 			while (rs.next()) {
 				String cname = RationaleDBUtil.decode(rs.getString("name"));
 				//experiment
 				Claim newClaim = new Claim();
 				newClaim.fromDatabase(cname);
 				claimList.add(newClaim);
 			}
 
 		} catch (SQLException ex) {
 			reportError(ex, "Error in getOverridenClaims", findQuery);
 		} finally {
 			releaseResources(stmt, rs);
 
 		}
 		return claimList;
 	}
 
 	/**
 	 * Get all the arguments that have an importance that overrides the default
 	 * inherited from a claim
 	 * @return the overriden arguments
 	 */
 	public Vector<Argument> getOverridenArguments() {
 
 		Vector<Argument> argList = new Vector<Argument>();
 		String findQuery = "";
 		Statement stmt = null;
 		ResultSet rs = null;
 		try {
 			stmt = conn.createStatement();
 			findQuery = "SELECT *  FROM "
 				+ RationaleDBUtil.escapeTableName("arguments") + " "
 				+ " WHERE importance <> '"
 				+ Importance.DEFAULT.toString() + "'";
 			//***		System.out.println(findQuery);
 			rs = stmt.executeQuery(findQuery);
 
 			while (rs.next()) {
 				String cname = RationaleDBUtil.decode(rs.getString("name"));
 				//experiment
 				Argument newArg = new Argument();
 				newArg.fromDatabase(cname);
 				if ((newArg.getCategory() == ArgCategory.CLAIM)
 						|| ((newArg.getCategory() == ArgCategory.REQUIREMENT) && (newArg
 								.getImportance() != Importance.ESSENTIAL))) {
 					argList.add(newArg);
 				}
 
 			}
 
 		} catch (SQLException ex) {
 			// handle any errors
 			reportError(ex, "Error in getOverridenArguments", findQuery);
 		} finally {
 			releaseResources(stmt, rs);
 		}
 		return argList;
 	}
 
 	/**
 	 * Release our database resources. This is done over and over and over again so
 	 * we have this nifty utility method to cut down on duplicated code.
 	 * @param stmt - the statement to close
 	 * @param rs - the resource to close
 	 */
 	public static void releaseResources(Statement stmt, ResultSet rs) {
 		// it is a good idea to release
 		// resources in a finally{} block
 		// in reverse-order of their creation
 		// if they are no-longer needed
 
 		if (rs != null) {
 			try {
 				rs.close();
 			} catch (SQLException sqlEx) { // ignore
 			}
 
 			rs = null;
 		}
 
 		if (stmt != null) {
 			try {
 				stmt.close();
 			} catch (SQLException sqlEx) { // ignore
 			}
 
 			stmt = null;
 		}
 	}
 
 	/**
 	 * Release our database resources. This is done over and over and over again so
 	 * we have this nifty utility method to cut down on duplicated code. We
 	 * need the two resource version because sometimes we have to nest queries
 	 * @param stmt - the statement to close
 	 * @param rs - the resource to close
 	 * @param rs2 - the other resource to close
 	 */
 	public static void releaseResources(Statement stmt, ResultSet rs, ResultSet rs2) {
 		// it is a good idea to release
 		// resources in a finally{} block
 		// in reverse-order of their creation
 		// if they are no-longer needed
 
 		if (rs != null) {
 			try {
 				rs.close();
 			} catch (SQLException sqlEx) { // ignore
 			}
 
 			rs = null;
 		}
 
 		if (rs2 != null) {
 			try {
 				rs2.close();
 			} catch (SQLException sqlEx) { // ignore
 			}
 
 			rs2 = null;
 		}
 		if (stmt != null) {
 			try {
 				stmt.close();
 			} catch (SQLException sqlEx) { // ignore
 			}
 
 			stmt = null;
 		}
 	}
 
 	/**
 	 * Report an error that occured when trying to do something with the database.
 	 * @param ex - the SQL exception that has occured
 	 * @param location - the part of SEURAT that ran into trouble (typically a method name)
 	 * @param message - the message describing what went wrong
 	 */
 	public static void reportError(SQLException ex, String location,
 			String message) {
 		System.out.println(location);
 		System.out.println(message);
 		System.out.println("SQLException: " + ex.getMessage());
 		System.out.println("SQLState: " + ex.getSQLState());
 		System.out.println("VendorError: " + ex.getErrorCode());
 	}
 
 	/**
 	 * Gets the name of the database
 	 * @return the database name
 	 */
 	public static String getDbName() {
 		return dbName;
 	}
 	
 	/**
 	 * Gets the name of the default ontology file
 	 * @return the filename
 	 */
 	public static String getOntName() {
 		return ontFile;
 	}
 
 	/**
 	 * Load up a new database from an XML file.
 	 *
 	 */
 	public static void loadRationaleDB() {
 		FileInputStream istream = null;
 
 		try {
 			istream = new FileInputStream(ratFile);
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
 		Document ratDoc;
 		try {
 			DocumentBuilder builder = factory.newDocumentBuilder();
 			ratDoc = builder.parse(new File(ratFile));
 			readXMLData(ratDoc);
 			RationaleDB db = RationaleDB.getHandle();
 			//Element rationale = ratDoc.createElement("DR:rationale");
 			//ratDoc.appendChild(rationale);
 
 
 			db.addXMLRequirements();
 			db.addXMLDecisions();
 			//db.addXMLOntology(ratDoc);
 
 
 
 		} catch (SAXException sce) {
 			System.err.println( sce.toString());
 		} catch (IOException ioe) {
 			System.err.println (ioe.toString());
 		} catch (ParserConfigurationException pce) {
 			System.err.println (pce.toString());
 		}
 
 		try {
 			istream.close();
 		} catch (IOException ioe) {
 			System.err.println (ioe.toString());
 			return;
 		}
 		return;
 
 	} //end of reading in new data
 
 	/**
 	 * Read in XML data and put it in the database. How does this relate to the
 	 * previous method???
 	 * @param ratDoc - an XML document
 	 */
 	private static void readXMLData(Document ratDoc)
 	{
 		s = RationaleDB.getHandle();
 		idRefs.clear(); //shouldn't actually need to do
 
 		Element ratTop = ratDoc.getDocumentElement();
 		//this should be our parent Rationale element
 
 		Element ratNext = (Element) ratTop.getFirstChild();
 		while (ratNext != null)
 		{
 			String nextName;
 			nextName = ratNext.getNodeName();
 //			System.out.println(nextName);
 			//here we check the type, then process
 			if (nextName.compareTo("DR:argOntology") == 0)
 			{
 //				System.out.println("found the ontology");
 				//need to get the root ontology entry
 				Element topOnt = (Element) ratNext.getFirstChild();
 				//process argument ontology
 				OntEntry topE = new OntEntry();
 				topE.fromXML(topOnt, null); //null parent
 				s.argumentOntology = topE;
 			}
 			else if (nextName.compareTo("DR:requirement") == 0)
 			{
 				Requirement nextReq = new Requirement();
 //				s.addRequirement(nextReq);
 				nextReq.fromXML(ratNext);
 				s.addRequirement(nextReq);
 			}
 			else if (nextName.compareTo("reqref") == 0)
 			{
 				//this is unusual but can happen!
 				//nothing to do here. it is already in the database and it's a top
 				//level item
 			}
 			else if (nextName.compareTo("DR:decisionproblem") == 0)
 			{
 				//process decision
 				Decision nextDec = new Decision();
 				nextDec.fromXML(ratNext);
 				s.addDecision(nextDec);
 			}
 			else if (nextName.compareTo("decref") == 0)
 			{
 				//again, nothing to do here, it is in the DB already
 			}
 			else if (nextName.compareTo("DR:backgroundKn") == 0)
 			{
 				//process background knowledge
 			}
 			ratNext = (Element) ratNext.getNextSibling();
 		}
 
 	}
 	
 	/**
 	 * Exports the argument ontology to XML by calling the appropriate
 	 * utility function and passing it the name of the file we are exporting to.
 	 * 
 	 * @return a boolean indicating success or failure which can be used to provide
 	 * appropriate feedback to the user
 	 */
 	public boolean exportOntology(String path) {
 		return RationaleDBUtil.exportArgumentOntology(path);
 	}
 
 	/**
 	 * Add a requirement to our requirement vector
 	 * @param newReq - the requirement
 	 */
 	public void addRequirement(Requirement newReq)
 	{
 		requirements.addElement(newReq);
 	}
 
 	/**
 	 * Add an argument to our argument vector
 	 * @param newArg - the argument
 	 */
 	public void addArgument(Argument newArg)
 	{
 		arguments.addElement(newArg);
 	}
 
 	/**
 	 * Add an assumption to our assumption vector
 	 * @param newAssump - the new assumption
 	 */
 	public void addAssumption(Assumption newAssump)
 	{
 		assumptions.addElement(newAssump);
 	}
 
 	/**
 	 * Add a claim to our claim vector
 	 * @param newClaim - the new claim
 	 */
 	public void addClaim(Claim newClaim)
 	{
 		claims.addElement(newClaim);
 	}
 
 	/**
 	 * Add a new question to our question vector
 	 * @param newQuestion - the new question
 	 */
 	public void addQuestion(Question newQuestion)
 	{
 		questions.addElement(newQuestion);
 	}
 
 	/**
 	 * Add a new decision to our decision vector
 	 * @param newDecision - the new decision
 	 */
 	public void addDecision(Decision newDecision)
 	{
 		decisions.addElement(newDecision);
 	}
 
 	/**
 	 * Add a new alternative to our alternative vector
 	 * @param alt - the new alternative
 	 */
 	public void addAlternative(Alternative alt)
 	{
 		alternatives.addElement(alt);
 	}
 
 	/**
 	 * Add a new tradeoff to our tradeoff vector
 	 * @param newTradeoff - the new tradeoff
 	 */
 	public void addTradeoff(Tradeoff newTradeoff)
 	{
 		tradeoffs.addElement(newTradeoff);
 	}
 
 	/**
 	 * Add a new cooccurence to our co-occurence vector
 	 * @param newCooccurences - our new element
 	 */
 	public void addCo_Occurence(Tradeoff newCooccurences)
 	{
 		co_occurences.addElement(newCooccurences);
 	}
 
 
 	/**
 	 * Given an XML reference ID, get the element name
 	 * @param i - the id
 	 * @return the name
 	 */
 	public String getRef(int i)
 	{
 		Integer inti = new Integer(i);
 		return (String) idRefs.get(inti);
 	}
 
 	/**
 	 * this is the version of getRef that returns a rationale element
 	 * this is used when reading in rationale from the XML, in this
 	 * case, storing ids is not sufficient
 	 */
 	public RationaleElement getRef(String sr)
 	{
 		RationaleElement t;
 		t = (RationaleElement) idRefs.get(sr);
 		if (t == null)
 		{
 			System.out.println("bad value from getRef");
 		}
 		return (RationaleElement) idRefs.get(sr);
 	}
 
 	/**
 	 * Add a reference
 	 * @param i - the ref ID
 	 * @return the string version of the reference
 	 */
 	public String addRef(int i)
 	{
 		Integer tempi;
 		tempi = new Integer(i);
 		String newRef = "r" + tempi.toString();
 		idRefs.put(tempi, newRef);
 		return newRef;
 	}
 
 	/**
 	 * This is the version of addRef that puts a RationaleElement into the hash
 	 * table. This is used when reading in the XML
 	 */
 	public void addRef(String sr, RationaleElement re)
 	{
 		//ref is already in the correct form, no change needed
 		idRefs.put(sr, re);
 	}
 
 	/* The following method does not appear to be used anywhere - why wouldn't
 	 * we need to do this???
 	 private void addXMLBackgroundKN(Document ratDoc, Element rationale)
 	 {
 	 System.out.println("adding background knowledge (if any)");
 	 Element knParent = ratDoc.createElement("DR:backgroundKn");
 	 rationale.appendChild(knParent);
 	 Enumeration trades = tradeoffs.elements();
 	 while (trades.hasMoreElements())
 	 {
 	 Tradeoff trade = (Tradeoff) trades.nextElement();
 	 }
 	 Enumeration occurences = co_occurences.elements();
 	 while (occurences.hasMoreElements())
 	 {
 	 Tradeoff trade = (Tradeoff) occurences.nextElement();
 	 }
 	 System.out.println("done adding background knowledge");
 	 }
 	 */
 
 	/**
 	 * What appears to be happening with the XML is that the rationale elements
 	 * are read from the file and stored into vectors. Then, this method is called
 	 * to put the elements actually INTO the database.
 	 */
 //	private void addXMLRequirements(Document ratDoc)
 	private void addXMLRequirements()
 	{
 		Enumeration reqs = requirements.elements();
 		while (reqs.hasMoreElements())
 		{
 			Requirement req = (Requirement) reqs.nextElement();
 			req.toDatabase(0, RationaleElementType.NONE);
 		}
 	}
 
 
 
 	private void addXMLDecisions()
 	{
 
 		Enumeration decs = decisions.elements();
 		while (decs.hasMoreElements())
 		{
 			Decision dec = (Decision) decs.nextElement();
 			dec.toDatabase(0, RationaleElementType.NONE);
 			//dec.toDatabase(0, RationaleElementType.NONE);
 		}
 		System.out.println("done adding decisions");
 	}
 
 	/**
 	 * Get all top-level XFeature mapping nodes
 	 * @return the top level elements
 	 */
 	public static Vector<XFeatureMapping> getToplevelMappings() {
 		Vector<XFeatureMapping> xfeaturenodes = new Vector<XFeatureMapping>();
 		Statement stmt = null;
 		ResultSet rs = null;
 		String findQuery = "";
 		try {
 			stmt = conn.createStatement();
 			findQuery = "SELECT * from " 
 				+ RationaleDBUtil.escapeTableName("xfeaturemapping") 
 				+ " where parent < 1";
 			//***			System.out.println(findQuery);
 			rs = stmt.executeQuery(findQuery);
 			while (rs.next()) {
 					int mapID = rs.getInt("id");
 					XFeatureMapping xNode = new XFeatureMapping();
 					xNode.fromDatabase(mapID);
 					xfeaturenodes.add(xNode);
 			}
 
 		} catch (SQLException ex) {
 			reportError(ex, "Error in getting top level xfeature elements", findQuery);
 		} finally {
 			releaseResources(stmt, rs);
 
 		}
 		return xfeaturenodes;
 	}
 	/**
 	 * Get all top-level XFeature mapping nodes
 	 * @return the top level elements
 	 */
 	public static Vector<XFeatureMapping> getDependentMappings(int parent) {
 		Vector<XFeatureMapping> dependent = new Vector<XFeatureMapping>();
 		Statement stmt = null;
 		ResultSet rs = null;
 		String findQuery = "";
 		try {
 			stmt = conn.createStatement();
 			findQuery = "SELECT * from " 
 				+ RationaleDBUtil.escapeTableName("xfeaturemapping") 
 				+ " where parent = " + parent;
 			//***			System.out.println(findQuery);
 			rs = stmt.executeQuery(findQuery);
 			while (rs.next()) {
 					int mapID = rs.getInt("id");
 					XFeatureMapping relArg = new XFeatureMapping();
 					relArg.fromDatabase(mapID);
 					dependent.add(relArg);
 			}
 
 		} catch (SQLException ex) {
 			reportError(ex, "Error in getting dependent xfeature elements", findQuery);
 		} finally {
 			releaseResources(stmt, rs);
 
 		}
 		return dependent;
 	}
 	/**
 	 * Get all top-level XFeature mapping nodes
 	 * @return the top level elements
 	 */
 	public static Vector<XFeatureMapping> getDependentMappings(String xnodeName) {
 		Vector<XFeatureMapping> dependent = new Vector<XFeatureMapping>();
 		Statement stmt = null;
 		ResultSet rs = null;
 		String findQuery = "";
 		try {
 			stmt = conn.createStatement();
 			findQuery = "SELECT * from " 
 				+ RationaleDBUtil.escapeTableName("xfeaturemapping") 
 				+ " where nodename = '" + xnodeName
 				+ "' and rattype = 'Alternative'";
 			//***			System.out.println(findQuery);
 			rs = stmt.executeQuery(findQuery);
 			while (rs.next()) {
 					int mapID = rs.getInt("id");
 					XFeatureMapping relArg = new XFeatureMapping();
 					relArg.fromDatabase(mapID);
 					dependent.add(relArg);
 			}
 
 		} catch (SQLException ex) {
 			reportError(ex, "Error in getting dependent xfeature elements", findQuery);
 		} finally {
 			releaseResources(stmt, rs);
 
 		}
 		return dependent;
 	}
 	/*
 	 public static void  main (String [] argv)
 	 {
 	 RationaleDB d = RationaleDB.getHandle();
 	 Vector reqs = d.getRequirements(null, null);
 
 	 } */
 
 }
