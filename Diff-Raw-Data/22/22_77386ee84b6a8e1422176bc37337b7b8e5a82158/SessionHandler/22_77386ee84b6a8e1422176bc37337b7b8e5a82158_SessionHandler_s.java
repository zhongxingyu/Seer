 package edu.ucdenver.ccp.PhenoGen.web;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 import java.util.List;
 
 import javax.servlet.http.HttpSession;
 
 import edu.ucdenver.ccp.PhenoGen.data.Dataset;
 import edu.ucdenver.ccp.PhenoGen.data.Experiment;
 import edu.ucdenver.ccp.PhenoGen.data.GeneList;
 import edu.ucdenver.ccp.PhenoGen.data.User;
 import edu.ucdenver.ccp.PhenoGen.util.DbUtils;
 import edu.ucdenver.ccp.util.sql.Results;
 
 /* for logging messages */
 import org.apache.log4j.Logger;
 
 /**
  * Class for handling data related to managing web sessions. 
  *  @author  Cheryl Hornbaker
  */
 
 public class SessionHandler {
 
 	private Logger log = null;
 	private String applicationRoot = "";
 	private String contextRoot = "";
 	private String userFilesRoot = "";
 	private String rFunctionDir = "";
 	private String accessDir = "";
 	private String aptDir = "";
 	private String datasetsDir = "";
 	private String experimentsDir = "";
 	private String geneListsDir = "";
 	private String qtlsDir = "";
         private String exonDir = "";
         private String ucscDir = "";
         private String ucscGeneDir = "";
         private String bedDir = "";
 	private String sysBioDir = "";
 	private String imagesDir = "";
 	private String commonDir = "";
 	private String propertiesDir = "";
 	private String dbPropertiesFile = "";
         private String ensDbPropertiesFile = "";
 	private String adminDir = "";
 	private String isbraDir = "";
 	private String helpDir = "";
 	private String javascriptDir = "";
 	private String webDir = "";
 	private String perlDir = "";
 	private String mainURL = "";
 	private String downloadURL = "";
 	private String host = "";
         private String perlEnvVar="";
         private String adminEmail="";
 	
         private Dataset selectedDataset = null;
         private Dataset.DatasetVersion selectedDatasetVersion = null;
         private Experiment selectedExperiment = null;
         private GeneList selectedGeneList = null;
 
 	// 
 	// data for sessions table
 	//
 	private String session_id = null;
 	private int user_id = 0;
 
 	//
 	// data for session_activities table
 	//
 	private int session_activity_id = -99;
 	private String activity_name = null;
 	private int exp_id = -99;
 	private int dataset_id = -99;
 	private int version = -99;
 	private int gene_list_id = -99;
 	private HttpSession session; 
 
 	private	DbUtils myDbUtils = new DbUtils();
 
         public SessionHandler() {
                 log = Logger.getRootLogger();
 		//log.debug("instantiated SessionHandler with no session");
 	}
 
 	
         public SessionHandler(HttpSession session) {
                 log = Logger.getRootLogger();
 		setSession(session); 
 	//	this.session = session;
 		//log.debug("instantiated SessionHandler setting session variable");
 	}
 
 	public HttpSession getSession() {
 		log.debug("in getSession");
 		return session;
 	}
 
 	public void setSession(HttpSession inSession) {
 		log.debug("in setSession");
 		this.session = inSession;
 		setSession_id(inSession.getId());
 		this.selectedDataset = ((Dataset) session.getAttribute("selectedDataset") == null ?
                                 new Dataset(-99) :
                                 (Dataset) session.getAttribute("selectedDataset"));
         	this.selectedDatasetVersion = ((Dataset.DatasetVersion) session.getAttribute("selectedDatasetVersion") == null ?
                         	new Dataset(-99).new DatasetVersion(-99) :
                         	(Dataset.DatasetVersion) session.getAttribute("selectedDatasetVersion"));
 
         	this.selectedExperiment = ((Experiment) session.getAttribute("selectedExperiment") == null ?
                                 new Experiment(-99) :
                                 (Experiment) session.getAttribute("selectedExperiment"));
 
         	this.selectedGeneList = ((GeneList) session.getAttribute("selectedGeneList") == null ?
                         new GeneList(-99) :
                         (GeneList) session.getAttribute("selectedGeneList"));
 
 		/*
         	log.debug("here selectedDataset = " + selectedDataset.getDataset_id()); 
         	log.debug("here selectedExperiment = " + selectedExperiment.getExp_id()); 
         	log.debug("here selectedDatasetVersion = " + selectedDatasetVersion.getVersion()); 
         	log.debug("here selectedGeneList = " + selectedGeneList.getGene_list_id()); 
 		*/
 	}
 
 	public String getApplicationRoot() {
 		return applicationRoot;
 	}
 
 	public void setApplicationRoot(String inString) {
 		this.applicationRoot = inString;
 	}
 
 	public String getContextRoot() {
 		return contextRoot + "/";
 	}
 
 	public void setContextRoot(String inString) {
 		this.contextRoot = inString;
 	}
 
 	public String getUserFilesRoot() {
 		return userFilesRoot;
 	}
 
 	public void setUserFilesRoot(String inString) {
 		this.userFilesRoot = inString;
 	}
 
 	public String getR_FunctionDir() {
 		return getApplicationRoot() + getContextRoot() + rFunctionDir + "/";
 	}
 
 	public void setR_FunctionDir(String inString) {
 		this.rFunctionDir = inString;
 	}
 
 	public String getAccessDir() {
 		return getContextRoot() + accessDir + "/";
 	}
 
 	public void setAccessDir(String inString) {
 		this.accessDir = inString;
 	}
 
 	public String getAptDir() {
 		return getApplicationRoot() + getContextRoot() + aptDir + "/";
 	}
 
 	public void setAptDir(String inString) {
 		this.aptDir = inString;
 	}
 
 	public String getGeneListsDir() {
 		return getContextRoot() + geneListsDir + "/";
 	}
 
 	public void setGeneListsDir(String inString) {
 		this.geneListsDir = inString;
 	}
 
 	public String getQtlsDir() {
 		return getContextRoot() + qtlsDir + "/";
 	}
 
 	public void setQtlsDir(String inString) {
 		this.qtlsDir = inString;
 	}
         
         public String getExonDir() {
 		return getContextRoot() + exonDir + "/";
 	}
 
 	public void setExonDir(String inString) {
 		this.exonDir = inString;
 	}
         
         public String getUcscDir() {
 		return ucscDir + "/";
 	}
 
 	public void setUcscDir(String inString) {
 		this.ucscDir = inString;
 	}
         
         public void setUcscGeneDir(String inString) {
 		this.ucscGeneDir = inString;
 	}
         public String getUcscGeneDir() {
 		return ucscGeneDir + "/";
 	}
         
         public String getBedDir() {
 		return getApplicationRoot() + getContextRoot() + bedDir + "/";
 	}
 
 	public void setBedDir(String inString) {
 		this.bedDir = inString;
 	}
 
 	public String getSysBioDir() {
 		return getContextRoot() + sysBioDir + "/";
 	}
 
 	public void setSysBioDir(String inString) {
 		this.sysBioDir = inString;
 	}
 
 	public String getDatasetsDir() {
 		return getContextRoot() + datasetsDir + "/";
 	}
 
 	public void setDatasetsDir(String inString) {
 		this.datasetsDir = inString;
 	}
 
 	public String getExperimentsDir() {
 		return getContextRoot() + experimentsDir + "/";
 	}
 
 	public void setExperimentsDir(String inString) {
 		this.experimentsDir = inString;
 	}
 
 	public String getImagesDir() {
 		return getContextRoot() + imagesDir + "/";
 	}
 
 	public void setImagesDir(String inString) {
 		this.imagesDir = inString;
 	}
 
 	public String getCommonDir() {
 		return getContextRoot() + commonDir + "/";
 	}
 
 	public void setCommonDir(String inString) {
 		this.commonDir = inString;
 	}
 
 	public String getPropertiesDir() {
 		return getApplicationRoot() + getContextRoot() + propertiesDir + "/";
 	}
 
 	public void setPropertiesDir(String inString) {
 		this.propertiesDir = inString;
 	}
 
 	public String getDbPropertiesFile() {
 		return this.getPropertiesDir() + "/" + dbPropertiesFile; 
 	}
 
 	public void setDbPropertiesFile(String inString) {
 		this.dbPropertiesFile = inString;
 	}
 
         public String getENSDbPropertiesFile() {
 		return this.getPropertiesDir() + "/" + ensDbPropertiesFile; 
 	}
 
 	public void setENSDbPropertiesFile(String inString) {
 		this.ensDbPropertiesFile = inString;
 	}    
         
 	public String getAdminDir() {
 		return getContextRoot() + adminDir + "/";
 	}
 
 	public void setAdminDir(String inString) {
 		this.adminDir = inString;
 	}
 
 	public String getIsbraDir() {
 		return getContextRoot() + isbraDir + "/";
 	}
 
 	public void setIsbraDir(String inString) {
 		this.isbraDir = inString;
 	}
 
 	public String getHelpDir() {
 		return getContextRoot() + helpDir + "/";
 	}
 
 	public void setHelpDir(String inString) {
 		this.helpDir = inString;
 	}
 	
 	public String getJavascriptDir() {
 		return getContextRoot() + javascriptDir + "/";
 	}
 
 	public void setJavascriptDir(String inString) {
 		this.javascriptDir = inString;
 	}
 
 	public String getWebDir() {
 		return getContextRoot() + webDir + "/";
 	}
 
 	public void setWebDir(String inString) {
 		this.webDir = inString;
 	}
 
 	public String getPerlDir() {
 		return getApplicationRoot() + getContextRoot() + perlDir + "/";
 	}
 
 	public void setPerlDir(String inString) {
 		this.perlDir = inString;
 	}
         public String getPerlEnvVar() {
 		return perlEnvVar;
 	}
 
 	public void setPerlEnvVar(String inString) {
 		this.perlEnvVar = inString;
 	}
 
 	public String getHost() {
 		return host;
 	}
 
 	public void setHost(String inString) {
 		this.host = inString;
 	}
 
 	public String getMainURL() {
 		return "http://" + 
 			this.getHost() + 
 			this.getContextRoot() + "index.jsp";
 	}
 
 /*
 	public void setMainURL(String inString) {
 		this.mainURL = inString;
 	}
 */
 
 	public String getDownloadURL() {
 		return "http://" + 
 			this.getHost() + 
 			this.getContextRoot() + "downloads.jsp";
 	}
 
 /*
 	public void setDownloadURL(String inString) {
 		this.downloadURL = inString;
 	}
 */
 
 	public String getSession_id() {
 		return session_id;
 	}
 
 	public void setSession_id(String inString) {
 		this.session_id = inString;
 	}
 
 	public String getActivity_name() {
 		return activity_name;
 	}
 
 	public void setActivity_name(String inString) {
 		this.activity_name = inString;
 	}
 
 	public int getUser_id() {
 		return user_id;
 	}
 
 	public void setUser_id(int inInt) {
 		this.user_id = inInt;
 	}
 
 	public int getExp_id() {
 		return exp_id;
 	}
 
 	public void setExp_id(int inInt) {
 		this.exp_id = inInt;
 	}
 
 	public int getDataset_id() {
 		return dataset_id;
 	}
 
 	public void setDataset_id(int inInt) {
 		this.dataset_id = inInt;
 	}
 
 	public int getVersion() {
 		return version;
 	}
 
 	public void setVersion(int inInt) {
 		this.version = inInt;
 	}
 
         public String getAdminEmail() {
 		return adminEmail;
 	}
 
 	public void setAdminEmail(String adminEmail) {
 		this.adminEmail = adminEmail;
 	}
         
 	public int getGene_list_id() {
 		return gene_list_id;
 	}
 
 	public void setGene_list_id(int inInt) {
 		this.gene_list_id = inInt;
 	}
 
 
 	public void setSessionVariables(HttpSession session, User userLoggedIn) {
 
 		log.debug("in setSessionVariables.");  
 		session.setAttribute("userID", Integer.toString(userLoggedIn.getUser_id()));
                 session.setAttribute("full_name",
 				userLoggedIn.getTitle() + " " +
                                 userLoggedIn.getFirst_name() + " " +
                                 userLoggedIn.getLast_name());
                 session.setAttribute("lab_name", userLoggedIn.getLab_name());
                 session.setAttribute("userName", userLoggedIn.getUser_name());
 
 		//String mainURL = "http://" + (String) session.getAttribute("host") + this.getContextRoot() + "index.jsp";
 	        session.setAttribute("mainURL", getMainURL());
 	        session.setAttribute("downloadURL", getDownloadURL());
 	        //this.setMainURL(mainURL);
 		log.debug("mainURL = "+getMainURL());
 		log.debug("downloadURL = "+getDownloadURL());
 
 		//log.debug("userName = " + userLoggedIn.getUser_name());
                 //session.setAttribute("applicationRoot", this.getApplicationRoot() + "/");
 		// Don't include final slash for applicationRoot
                 session.setAttribute("applicationRoot", this.getApplicationRoot());
                 session.setAttribute("contextRoot", this.getContextRoot());
 		session.setAttribute("userFilesRoot", this.getUserFilesRoot() + "/");
 		//
 		// these 4 directories need the whole path specified
 		//
 		session.setAttribute("rFunctionDir", this.getR_FunctionDir());
 		session.setAttribute("perlDir", this.getPerlDir());
                 session.setAttribute("propertiesDir", this.getPropertiesDir());
 		session.setAttribute("aptDir", this.getAptDir());
 		//
 		// the rest of these parameters are relative to the context root 
 		//
                 session.setAttribute("accessDir", this.getAccessDir());
                 session.setAttribute("datasetsDir", this.getDatasetsDir());
                 session.setAttribute("experimentsDir", this.getExperimentsDir());
                 session.setAttribute("geneListsDir", this.getGeneListsDir());
                 session.setAttribute("qtlsDir", this.getQtlsDir());
                 session.setAttribute("exonDir", this.getExonDir());
                 session.setAttribute("ucscDir", this.getUcscDir());
                 session.setAttribute("ucscGeneDir", this.getUcscGeneDir());
                 session.setAttribute("bedDir", this.getBedDir());
                 session.setAttribute("sysBioDir", this.getSysBioDir());
                 session.setAttribute("imagesDir", this.getImagesDir());
                 session.setAttribute("commonDir", this.getCommonDir());
                 session.setAttribute("adminDir", this.getAdminDir());
                 session.setAttribute("isbraDir", this.getIsbraDir());
                 session.setAttribute("helpDir", this.getHelpDir());
                 session.setAttribute("javascriptDir", this.getJavascriptDir());
                 session.setAttribute("webDir", this.getWebDir());
                 session.setAttribute("dbPropertiesFile", this.getDbPropertiesFile()); 
                 session.setAttribute("ensDbPropertiesFile", this.getENSDbPropertiesFile());
 		//log.debug("in SessionHandler.dbPropertiesFile = "+ dbPropertiesFile);
                 session.setAttribute("perlEnvVar", this.getPerlEnvVar());
                 session.setAttribute("adminEmail", this.getAdminEmail());
                
 
 	}
 
   public void createSession(SessionHandler mySessionHandler, Connection conn) throws SQLException {
 
         String query =
                 "insert into sessions "+
                 "(session_id, user_id, login_time) "+
                 "values "+
                 "(?, ?, ?)";
 
         PreparedStatement pstmt = null;
 
 	log.debug("in createSession");  
 	//log.debug("query = "+ query);  
         try {
                 java.sql.Timestamp now = new java.sql.Timestamp(System.currentTimeMillis());
 
                 pstmt = conn.prepareStatement(query, 
 					ResultSet.TYPE_SCROLL_INSENSITIVE,
 					ResultSet.CONCUR_UPDATABLE);
                 pstmt.setString(1, mySessionHandler.getSession_id());
                 pstmt.setInt(2, mySessionHandler.getUser_id());
                 pstmt.setTimestamp(3, now);
 		
 		pstmt.executeUpdate();
 		pstmt.close();
                 
         } catch (SQLException e) {
 		log.error("In exception of createSession", e);
 		throw e;
         }
   }
 	
 	/** Update the sessions table indicating the user has logged out.
 	 * @param conn	the database connection
          * @throws            SQLException if a database error occurs
 	 */
 	public void logoutSession(Connection conn) throws SQLException {
 		log.debug("in logoutSession.  SessionID = "+this.getSession_id());
 
 		if (conn != null) {
         		String query =
                 		"update sessions "+
 				"set logout_time = ? "+
 				"where session_id = ?";
                         
                 	java.sql.Timestamp now = new java.sql.Timestamp(System.currentTimeMillis());
 
         		PreparedStatement pstmt = conn.prepareStatement(query, 
 							ResultSet.TYPE_SCROLL_INSENSITIVE,
 							ResultSet.CONCUR_UPDATABLE);
                 	pstmt.setTimestamp(1, now);
                 	pstmt.setString(2, this.getSession_id());
 		
 			pstmt.executeUpdate();
 
 		}
 	}
         
         /** Update the sessions table indicating the user has logged in from a previously anonymous session.
 	 * @param conn	the database connection
          * @throws            SQLException if a database error occurs
 	 */
 	public void updateLoginSession(SessionHandler mySessionHandler,Connection conn) throws SQLException {
 		log.debug("in updateLoginSession.  SessionID = "+this.getSession_id());
 
 		if (conn != null) {
         		String query =
                 		"update sessions "+
 				"set  user_id = ? "+
 				"where session_id = ?";
 
 
         		PreparedStatement pstmt = conn.prepareStatement(query, 
 							ResultSet.TYPE_SCROLL_INSENSITIVE,
 							ResultSet.CONCUR_UPDATABLE);
                 	pstmt.setInt(1, mySessionHandler.getUser_id());
                         pstmt.setString(2, this.getSession_id());
 		
 			pstmt.executeUpdate();
 
 		}
 	}
 
 	/**
 	 * Creates a record in the session_activities table.  Gets the variables from the session object.
 	 * @param activityName	the activity
 	 * @param conn	the database connection
          * @throws            SQLException if a database error occurs
 	 */
 	public void createActivity(String activityName, Connection conn) throws SQLException {
 	
 		// Added this here to handle cases when user session is inactive 
 		if (conn != null) {
 
 			SessionHandler mySessionHandler = new SessionHandler();
 
        			mySessionHandler.setSession_id(this.getSession_id());
 			mySessionHandler.setExp_id(this.selectedExperiment.getExp_id()); 
 			mySessionHandler.setDataset_id(this.selectedDataset.getDataset_id()); 
 			mySessionHandler.setVersion(this.selectedDatasetVersion.getVersion());
 			mySessionHandler.setGene_list_id(this.selectedGeneList.getGene_list_id());
         		mySessionHandler.setActivity_name(activityName);
 		
 			createSessionActivity(mySessionHandler, conn);
 		}
 	}
 
 	/**
 	 * Creates a record in the session_activities table.  Assumes the variables have been set explicitly.
 	 * @param mySessionHandler	the SessionHandler object containing the previously set values.
 	 * @param conn	the database connection
          * @throws            SQLException if a database error occurs
 	 */
 	public void createSessionActivity(SessionHandler mySessionHandler, Connection conn) throws SQLException {
 
 		// Added this here to handle cases when user session is inactive 
 		if (conn != null) {
 			session_activity_id = myDbUtils.getUniqueID("session_activities_seq", conn);
 			//log.debug("session_activity_id = " + session_activity_id);
 
         		String query =
                 		"insert into session_activities "+
                 		"(activity_id, session_id, "+
 				"exp_id, dataset_id, version, gene_list_id, "+
 				"activity_name, activity_time) "+
                 		"values "+
                 		"(?, ?, "+
 				"?, ?, ?, ?, "+
 				"?, ?)";
 
 			//log.debug("in createSessionActivity.  SessionID = "+mySessionHandler.getSession_id());
 
         		PreparedStatement pstmt = conn.prepareStatement(query, 
 							ResultSet.TYPE_SCROLL_INSENSITIVE,
 							ResultSet.CONCUR_UPDATABLE);
 
                 	java.sql.Timestamp now = new java.sql.Timestamp(System.currentTimeMillis());
                 	pstmt.setInt(1, session_activity_id);
                 	pstmt.setString(2, mySessionHandler.getSession_id());
 			myDbUtils.setToNullIfZero(pstmt, 3, mySessionHandler.getExp_id()); 
 			myDbUtils.setToNullIfZero(pstmt, 4, mySessionHandler.getDataset_id()); 
 			myDbUtils.setToNullIfZero(pstmt, 5, mySessionHandler.getVersion());
 			myDbUtils.setToNullIfZero(pstmt, 6, mySessionHandler.getGene_list_id());
                 	pstmt.setString(7, mySessionHandler.getActivity_name().substring(0,Math.min(mySessionHandler.getActivity_name().length(),1980)));
                 	pstmt.setTimestamp(8, now);
 		
 			pstmt.executeUpdate();
 			pstmt.close();
 		}
 
 	}
   
 	/**
 	 * Creates a record in the session_activities table. 
 	 * Used for creating an experiment-related, dataset-related or a gene-list-related activity. 
 	 * @param sessionID	the identifier of the session
 	 * @param exp_id	identifier of the experiment
 	 * @param dataset_id	identifier of the dataset
 	 * @param version	version number of the dataset
 	 * @param geneListID	identifier of the gene list
 	 * @param activityName	the activity
 	 * @param conn	the database connection
 	 */
 	 public void createSessionActivity(String sessionID, 
 				int exp_id, int dataset_id, int version, int geneListID, 
 				String activityName, Connection conn) throws SQLException {
 
 		SessionHandler mySessionHandler = new SessionHandler();
 
        		mySessionHandler.setSession_id(sessionID);
 		mySessionHandler.setExp_id(exp_id); 
 		mySessionHandler.setDataset_id(dataset_id); 
 		mySessionHandler.setVersion(version);
 		mySessionHandler.setGene_list_id(geneListID);
         	mySessionHandler.setActivity_name(activityName);
 		
 		createSessionActivity(mySessionHandler, conn);
 	 }
 
 	/**
 	 * Creates a record in the session_activities table. 
 	 * @param sessionID	the identifier of the session
 	 * @param activityName	the activity
 	 * @param conn	the database connection
 	 * @throws            SQLException if a database error occurs
 	 */
 	public void createSessionActivity(String sessionID, String activityName, Connection conn) throws SQLException {
 
 		SessionHandler mySessionHandler = new SessionHandler();
 
        		mySessionHandler.setSession_id(sessionID);
         	mySessionHandler.setActivity_name(activityName);
 		
 		createSessionActivity(mySessionHandler, conn);
 	}
   
 	/**
 	 * Creates a record in the session_activities table. Used for creating an experiment-related activity. 
 	 * @param sessionID	the identifier of the session
 	 * @param expID	identifier of the experiment
 	 * @param activityName	the activity
 	 * @param conn	the database connection
          * @throws            SQLException if a database error occurs
 	 */
 	 public void createExperimentActivity(String sessionID, int expID, String activityName, Connection conn) throws SQLException {
 
 		SessionHandler mySessionHandler = new SessionHandler();
 
        		mySessionHandler.setSession_id(sessionID);
 		mySessionHandler.setExp_id(expID);
         	mySessionHandler.setActivity_name(activityName);
 		
 		createSessionActivity(mySessionHandler, conn);
 	 }
 
 	/**
 	 * Creates a record in the session_activities table. Used for creating an experiment-related activity. 
 	 * @param activityName	the activity
 	 * @param conn	the database connection
          * @throws            SQLException if a database error occurs
 	 */
 	 public void createExperimentActivity(String activityName, Connection conn) throws SQLException {
 
 		log.debug("in createExperimentActivity. exp_id = "+this.selectedExperiment.getExp_id()); 
 		SessionHandler mySessionHandler = new SessionHandler();
 
        		mySessionHandler.setSession_id(this.getSession_id());
 		mySessionHandler.setExp_id(this.selectedExperiment.getExp_id()); 
         	mySessionHandler.setActivity_name(activityName);
 		
 		createSessionActivity(mySessionHandler, conn);
 	 }
 
 	/**
 	 * Creates a record in the session_activities table. Used for creating a dataset-related activity 
 	 * where the selectedDataset and selectedDatasetVersion are not used. 
 	 * @param sessionID	the identifier of the session
 	 * @param dataset_id	identifier of the dataset
 	 * @param version	version number of the dataset
 	 * @param activityName	the activity
 	 * @param conn	the database connection
 	 */
 	public void createDatasetActivity(String sessionID, int dataset_id, int version, String activityName, Connection conn) throws SQLException {
 
 		SessionHandler mySessionHandler = new SessionHandler();
 
        		mySessionHandler.setSession_id(sessionID);
 		mySessionHandler.setDataset_id(dataset_id); 
 		mySessionHandler.setVersion(version);
         	mySessionHandler.setActivity_name(activityName);
 		
 		createSessionActivity(mySessionHandler, conn);
 	}
 
 	/**
 	 * Creates a record in the session_activities table. Used for creating a dataset-related activity. 
 	 * @param activityName	the activity
 	 * @param conn	the database connection
 	 */
 	public void createDatasetActivity(String activityName, Connection conn) throws SQLException {
 		//log.debug("in createDatasetActivity.  Dataset_id = "+this.selectedDataset.getDataset_id());
 		//log.debug("version = "+this.selectedDatasetVersion.getVersion());
 
 		SessionHandler mySessionHandler = new SessionHandler();
 
        		mySessionHandler.setSession_id(this.getSession_id());
 		mySessionHandler.setDataset_id(this.selectedDataset.getDataset_id()); 
 		mySessionHandler.setVersion(this.selectedDatasetVersion.getVersion());
         	mySessionHandler.setActivity_name(activityName);
 		
 		createSessionActivity(mySessionHandler, conn);
 	}
   
 	/**
 	 * Creates a record in the session_activities table. Used for creating a gene-list-related activity. 
 	 * @param sessionID	the identifier of the session
 	 * @param geneListID	identifier of the gene list
 	 * @param activityName	the activity
 	 * @param conn	the database connection
          * @throws            SQLException if a database error occurs
 	 */
 	 public void createGeneListActivity(String sessionID, int geneListID, String activityName, Connection conn) throws SQLException {
 
 		SessionHandler mySessionHandler = new SessionHandler();
 
        		mySessionHandler.setSession_id(sessionID);
 		mySessionHandler.setGene_list_id(geneListID);
         	mySessionHandler.setActivity_name(activityName);
 		
 		createSessionActivity(mySessionHandler, conn);
 	 }
 
 	/**
 	 * Creates a record in the session_activities table. Used for creating a gene-list-related activity. 
 	 * @param activityName	the activity
 	 * @param conn	the database connection
          * @throws            SQLException if a database error occurs
 	 */
 	 public void createGeneListActivity(String activityName, Connection conn) throws SQLException {
 
 		SessionHandler mySessionHandler = new SessionHandler();
 
        		mySessionHandler.setSession_id(this.getSession_id());
 		mySessionHandler.setGene_list_id(this.selectedGeneList.getGene_list_id()); 
         	mySessionHandler.setActivity_name(activityName);
 		
 		createSessionActivity(mySessionHandler, conn);
 	 }
   
 	/**
 	 * Removes the session_activities records for a dataset version. Not actually deleting them so we
 	 * can see them in the future?
 	 * @param thisVersion	the DatasetVersion object
 	 * @param conn	the database connection
 	 * @throws            SQLException if a database error occurs
 	 */
 	public void deleteSessionActivitiesForDatasetVersion (Dataset.DatasetVersion thisVersion, Connection conn) throws SQLException {
 
 		log.debug("in deleteAllSessionActivitiesForDatasetVersion");
 
 		String query =
 			"update session_activities "+
         		"set dataset_id = '', "+
 			"version = '' "+
         		"where dataset_id = ? "+
 			"and version = ?";
 			/* 
 			"delete from session_activities "+
         		"where dataset_id = ? "+
 			"and version = ?";
 			*/
   	
   		PreparedStatement pstmt = conn.prepareStatement(query, 
 					ResultSet.TYPE_SCROLL_INSENSITIVE,
 					ResultSet.CONCUR_UPDATABLE);
 		pstmt.setInt(1, thisVersion.getDataset().getDataset_id());
 		pstmt.setInt(2, thisVersion.getVersion());
 
 		pstmt.executeUpdate();
 		pstmt.close();
 	}
   
 	/**
 	 * Removes the session_activities records for a gene list. Not actually deleting them so we
 	 * can see them in the future?
 	 * @param gene_list_id	the identifier of the genelist
 	 * @param conn	the database connection
 	 * @throws            SQLException if a database error occurs
 	 */
 	public void deleteSessionActivitiesForGeneList(int gene_list_id, Connection conn) throws SQLException {
 
         	log.debug("In deleteSessionActivitiesForGeneList");
 		String query =
 			"update session_activities "+
 			"set gene_list_id = '' "+
 			"where gene_list_id = ?";
 			/* 
 			"delete from session_activities "+
 			"where gene_list_id = ?";
 			*/
   	
 		PreparedStatement pstmt = conn.prepareStatement(query, 
 					ResultSet.TYPE_SCROLL_INSENSITIVE,
 					ResultSet.CONCUR_UPDATABLE);
 		pstmt.setInt(1, gene_list_id);
 
 		pstmt.executeUpdate();
 		pstmt.close();
 	}
 
 	/**
 	 * Removes the session_activities records for an experiment. Not actually deleting them so we
 	 * can see them in the future?
 	 * @param exp_id	the identifier of the experiment
 	 * @param conn	the database connection
 	 * @throws            SQLException if a database error occurs
 	 */
 	public void deleteSessionActivitiesForExperiment(int exp_id, Connection conn) throws SQLException {
 
         	log.debug("In deleteSessionActivitiesForExperiment");
 		String query =
 			"update session_activities "+
 			"set exp_id = '' "+
 			"where exp_id = ?";
 			/* 
 			"delete from session_activities "+
 			"where exp_id = ?";
 			*/
   	
 		PreparedStatement pstmt = conn.prepareStatement(query, 
 					ResultSet.TYPE_SCROLL_INSENSITIVE,
 					ResultSet.CONCUR_UPDATABLE);
 		pstmt.setInt(1, exp_id);
 
 		pstmt.executeUpdate();
 		pstmt.close();
 	}
 
 	/**
 	 * Deletes records from the session_activities table that are over 90 days old.  
 	 * Also deletes records from the sessions table that have no activities.  
 	 * @param conn	the database connection
          * @throws            SQLException if a database error occurs
 	 */
 	public void deleteOldSessions(Connection conn) throws SQLException {
 
         	String[] query = new String[3];
 
 		query[0] = 
                 	"delete from sessions s "+
 			"where not exists "+
 			"	(select 'x' "+
 			"	from session_activities sa "+
 			"	where s.session_id = sa.session_id)";
 
 		query[1] = 
                 	"delete from session_activities "+
 			"where session_id in "+
 			"	(select session_id "+
 			"	from sessions "+
 			"	where login_time < sysdate - 90)";
 
 		query[2] = 
                 	"delete from sessions "+
 			"where login_time < sysdate - 90";
 	
 		log.debug("in deleteOldSessions");
 
 		for (int i=0; i<query.length; i++) {
 	        	PreparedStatement pstmt = conn.prepareStatement(query[i], 
 						ResultSet.TYPE_SCROLL_INSENSITIVE,
 						ResultSet.CONCUR_UPDATABLE);
 			log.debug("query = "+query[i]);
 			pstmt.executeUpdate();
 			pstmt.close();
 		}
 	}
 
 
   
   public String[] getSessionsForUserStatements(String typeOfQuery) {
   	String[] query = new String[2];
 
 	String selectClause = myDbUtils.getSelectClause(typeOfQuery);
 	String rownumClause = myDbUtils.getRownumClause(typeOfQuery);
 
 	query[0] =
 		selectClause +
   		"from session_activities sa "+
 		"where exists "+
 			"(select session_id from sessions s "+
 			"where user_id = ? " +
 			"and s.session_id = sa.session_id)"+
 		rownumClause;
   	
  	query[1] = 
 		selectClause +
 		"from sessions " +
 		"where user_id = ?"+
 		rownumClause;
 
 	//for (int i=0; i<query.length; i++) {
 	//	log.debug("i = " + i + ", query = " + query[i]);
 	//}
 	return query;
   }
 
   public List<List<String[]>> getSessionsForUser (int userID, Connection conn) throws SQLException {
   	
 	log.debug("in getSessionsForUser. userID = "+userID);
 
   	String[] query = getSessionsForUserStatements("SELECT10");
 
 	List<List<String[]>> allResults = null;
 
   	try {
 		allResults = new Results().getAllResults(query, userID, conn);
 
 	} catch (SQLException e) {
 		log.error("In exception of getSessionsForUser", e);
 		throw e;
 	}
 	log.debug("returning allResults.length = "+allResults.size());
 	return allResults;
   }
 
 	/**
 	 * Removes the session_activities records for a user.
 	 * @param userID	the identifier of the user
 	 * @param conn	the database connection
 	 * @throws            SQLException if a database error occurs
 	 */
 	public void deleteSessionsForUser (int userID, Connection conn) throws SQLException {
   	
 		log.debug("in deleteSessionsForUser");
 
   		String[] query = getSessionsForUserStatements("DELETE");
 
                 for (int i=0; i<query.length; i++) {
   			PreparedStatement pstmt = conn.prepareStatement(query[i],
                             		ResultSet.TYPE_SCROLL_INSENSITIVE,
                             		ResultSet.CONCUR_UPDATABLE);
                         pstmt.setInt(1, userID);
 			pstmt.executeUpdate();
 			pstmt.close();
                 }
 	}
 
 }
 
 
 
