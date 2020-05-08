 /**
 $Revision: 1.82 $"
 $Id: DBSApi.java,v 1.82 2007/03/15 14:24:45 afaq Exp $"
  *
 */
 
 
 package dbs.api;
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.io.Writer;
 import java.util.Vector;
 import java.util.Hashtable;
 import java.util.Enumeration;
 import xml.DBSXMLParser;
 import xml.Element;
 import xml.XMLException;
 import db.DBManagement;
 import dbs.DBSConstants;
 import dbs.sql.DBSSql;
 import dbs.DBSException;
 import dbs.util.DBSUtil;
 import db.DBManagement;
 import java.sql.ResultSet;
 import dbs.util.DBSConfig;
 import dbs.api.parser.DBSApiParser;
 
 /**
  * This class encapsulate <code>dbs.api.DBSApiLogic</code> , handles database connection management,handle XML parsing for the input provided by the clients, checks the match between schema and api version and handles exceptions. This class works as the higher level dispatcher for DBS API. All the DBS API calls are invoked vias a public method call. The interface of this class is this call method which can take a hashtable of key value pairs. It invokes the API call depending upon the value of the api key in the hashtable. The reason for having this higher level class is to separate the lovel level business logic from database connection management and xml parsing.<br>
 * Most of the insert API calls requires an xml input that needs to be parsed and converted into a hastable that can be further passed down to <code>dbs.api.DBSApiLogic</code> class. Here are the sample XML input required by various insert APIs <br> <br><code>
  * <b>insertPrimaryDataset</b> <br>
 	<"?xml version='1.0' standalone='yes'?"> <br>
 			<"dbs"> <br>
 				<"primary_dataset annotation='aaaa' primary_name='abcd' start_date='NOV' end_date='DEC' trigger_path_description='anyTD' mc_channel_description='MCDesc' mc_production='MCProd' mc_decay_chain='DC' other_description='OD' type='VALID'">
 				<"/primary_dataset"> <br>
  			<"/dbs"> <br>
  * <br>		
  * <br>		
  * <b>insertAlgorithm</b> <br>
 		<"?xml version='1.0' standalone='yes'?"> <br>
 		<"dbs"> <br>
 			<"algorithm app_version='MyVersion1' app_family_name='MyFamily1' app_executable_name='MyExe1' ps_name='DUMMY_ps_name2' ps_hash='DUMMY_HASH' ps_version='DUMMY1' ps_type='DUMMYTYPE1' ps_annotation='ANN1' ps_content='DUMMYCON'/"> <br>
 		<"/dbs"> <br>
 		
 
  * <br>		
  * <br>		
  * <b>insertRun</b> <br>
 		<"?xml version='1.0' standalone='yes'?"> <br>
 		<"dbs"> <br>
 			<"run run_number='111' number_of_events='54' number_of_lumi_sections='12' total_luminosity='2' store_number='32' start_of_run='nov' end_of_run='dec'/"> <br>
 		<"/dbs"> <br>
 	
  * <br>		
  * <br>		
  * <b>insertLumiSection</b> <br>
 		<"?xml version='1.0' standalone='yes'?"> <br>
 		<"dbs"> <br>
 			<"lumi_section lumi_section_number='1111' run_number='11' start_event_number='20' end_event_number='200' lumi_start_time='nov' lumi_end_time='dec'/"> <br>
 		<"/dbs"> <br>
 
  * <br>		
  * <br>		
  * <b>insertProcessedDataset</b>  <br>
 		<"?xml version='1.0' standalone='yes'?"> <br>
 		<"dbs"> <br>
 			<"processed_dataset primary_datatset_name='$primary_name' processed_datatset_name='$processed_name' open_for_writing='y' physics_group_name='AnyName' physics_group_convener='ANZARDN' status='VALID'"> <br>
 				<"data_tier name='$tier_name1'/"> <br>
 				<"algorithm app_version='MyVersion1' app_family_name='MyFamily1' app_executable_name='MyExe1' ps_name='DUMMY_ps_name2' ps_hash='DUMMY_HASH' ps_version='DUMMY1' ps_type='DUMMYTYPE1' ps_annotation='ANN1' ps_content='DUMMYCON'/"> <br>
 				<"algorithm app_version='MyVersion2' app_family_name='MyFamily2' app_executable_name='MyExe2' ps_name='DUMMY_ps_name2' ps_hash='DUMMY_HASH' ps_version='DUMMY2' ps_type='DUMMYTYPE2' ps_annotation='ANN2' ps_content='DUMMYCON'/"> <br>
 				<"run run_number='222'/"> <br>
 				<"run run_number='111'/"> <br>
 			<"/processed_dataset"> <br>
 		<"/dbs"> <br>
 
  * <br>		
  * <br>		
  * <b>insertBlock</b> <br>
 		<"?xml version='1.0' standalone='yes'?"> <br>
 		"<"dbs"> <br>
 			<"block path='$path' name='$block_name' open_for_writing='1'/"> <br>
 		<"/dbs"> <br>
 
  * <br>		
  * <br>		
  * <b>insertFiles</b> <br>
 		<"?xml version='1.0' standalone='yes'?"> <br>
 		<"dbs"> <br>
 		<"processed_datatset path='$path' block_name='$block_name'"> <br>
 			<"file lfn='TEST_LFN_1' checksum='CHKSUM' number_of_events='200' size='200' file_status='VALID' type= 'EVD' validation_status='VALID' queryable_meta_data='any'"> <br>
 				<"lumi_section lumi_section_number='9997' run_number='$run_number1' start_event_number='4' end_event_number='7' lumi_start_time='nov' lumi_end_time='dec'/"> <br>
 				<"lumi_section lumi_section_number='9996' run_number='$run_number1' start_event_number='4' end_event_number='7' lumi_start_time='nov' lumi_end_time='dec'/"> <br>
 				<"lumi_section lumi_section_number='9995' run_number='$run_number2' start_event_number='4' end_event_number='7' lumi_start_time='nov' lumi_end_time='dec'/"> <br>
 				<"data_tier name='$tier_name1'/"> <br>
 				<"data_tier name='$tier_name2'/"> <br>
 				<"algorithm app_version='MyVersion1' app_family_name='MyFamily1' app_executable_name='MyExe1' ps_name='DUMMY_ps_name1' ps_hash='DUMMY_HASH' ps_version='DUMMY1' ps_type='DUMMYTYPE2' ps_annotation='ANN1' ps_content='DUMMYCON'/"> <br>
 				<"algorithm app_version='MyVersion2' app_family_name='MyFamily2' app_executable_name='MyExe2' ps_name='DUMMY_ps_name2' ps_hash='DUMMY_HASH' ps_version='DUMMY2' ps_type='DUMMYTYPE2' ps_annotation='ANN2' ps_content='DUMMYCON'/"> <br>
 			<"/file"> <br>
 			<"file lfn='TEST_LFN_2' checksum='CHKSUM2' number_of_events='300' size='2002' file_status='MERGED' type= 'EVD' validation_status='VALID' queryable_meta_data='any'"> <br>
 				<"lumi_section lumi_section_number='1006' run_number='$run_number1' start_event_number='4' end_event_number='7' lumi_start_time='nov' lumi_end_time='dec'/"> <br>
 				<"lumi_section lumi_section_number='1017' run_number='$run_number2' start_event_number='4' end_event_number='7' lumi_start_time='nov' lumi_end_time='dec'/"> <br>
 				<"lumi_section lumi_section_number='1028' run_number='$run_number1' start_event_number='4' end_event_number='7' lumi_start_time='nov' lumi_end_time='dec'/"> <br>
 				<"data_tier name='$tier_name1'/"> <br>
 				<"data_tier name='$tier_name2'/"> <br>
 				<"parent lfn='TEST_LFN_1'/"> <br>
 				<"algorithm app_version='MyVersion1' app_family_name='MyFamily1' app_executable_name='MyExe1' ps_name='DUMMY_ps_name1' ps_hash='DUMMY_HASH' ps_version='DUMMY1' ps_type='DUMMYTYPE2' ps_annotation='ANN1' ps_content='DUMMYCON'/"> <br>
 			<"/file"> <br>
 		<"/processed_datatset"> <br>
 		<"/dbs"> <br>
  * </code>
  * @author sekhri
  * 
  */
 public class DBSApi {
 	
 	private DBSApiLogic api;
 	private DBSApiData data = null;
 
 	/**
 	* Constructs a DBSApi object that can be used to invoke call method that invokes several APIs from <code>dbs.api.DBSApiLogic</code> class. The constructor instantiates a private <code>dbs.api.DBSApiLogic</code> object.
 	*/
 	public DBSApi() {
 		data = new DBSApiData();
 		api = new DBSApiLogic(data);
 	}
 
         public Vector supportedClientApiVersions() throws Exception {
 		//FIXME ASK Anzar  to use some conventions in declaing vaiables.
                 DBSConfig config = DBSConfig.getInstance();
                 String clientVers = config.getSupportedClientVersions(); 
                 String[] result = clientVers.split(","); 
                 //String[] result = clientVers.split("\\s"); 
                 //FIXME Turning array into a verstor is just something not required, fix this later 
 		Vector supported_version_list  = new Vector();
                 for (int x=0; x<result.length; x++) {
                     supported_version_list.add(result[x].trim());
                 }
 		return supported_version_list;
 
         }
 
 	public String supportedSchemaVersions() throws Exception {
                //Get Schema Version from DBSConfig
                DBSConfig config = DBSConfig.getInstance();
                String suppSchemaVer = config.getSupportedSchemaVersion();
 	       return suppSchemaVer;
 	}
 
         //This func must move to Utility
         private boolean isIn(String param, Enumeration e) {
                 while (e.hasMoreElements()) {
                         if( param.equals((String)e.nextElement()) ) {
                                 return true;
                         }
                 }
                 return false;
         }
 
 	private void checkSchemaVersion() throws Exception {
 		Connection conn =  getConnection();
 		try {
 			String sql = "select SchemaVersion from SchemaVersion";
 			DBSUtil.writeLog(sql);
 			ResultSet rs =  DBSSql.getSchemaVersion(conn).executeQuery();
 			String dbsSchemaVersion="";
 			if(rs.next()) {
 				dbsSchemaVersion = rs.getString("SchemaVersion");
 			} else {
 				throw new DBSException("Schema Version Failure", "1001", "Unable to get Schema Version from Database, cannot continue");
 			} 
 			String suppSchemaVer = supportedSchemaVersions();
 			if (! dbsSchemaVersion.equals(suppSchemaVer) ) {
 				throw new DBSException("Unsupported Schema version", "1002", "Database Schema Mismatch, $DBS_SERVER_CONFIG Version is " + suppSchemaVer + " Current schema version in DB is :" + dbsSchemaVersion); 
 			}
 		} finally {
 			if(conn != null) conn.close();
                 }
         }  
 
         private void checkVersion(String apiversion) throws Exception {
 		//FIXME I dont think this api check is does anything. Anyways it has to move in the call method insated of the constructor
                 Enumeration verEnum = supportedClientApiVersions().elements();
                 String msg  = "Incorrect API version specified '"+apiversion+"'";
                        msg += " Supported versions are: ";
 
                 for (Enumeration e = supportedClientApiVersions().elements() ; 
                                  e.hasMoreElements() ;) {
                     msg += " "+(String)e.nextElement();
                 }
 
                 if ( ! isIn(apiversion, verEnum ) ) {
                         throw new DBSException("BAD Api Version", "1003",  msg);
                         //return;
                 }
 
 
                 //Now lets check the Schema version too !!!!!!!!!!!!
         }
 
 	/**
 	 * This method is the main interface for invoking the various API calls. It makes a database connection, extract the key value pairs from the input hashtable and calls the low level DBSApiLogic with the extracted parameters. If the input parameters are in xml format then it parse the xml and packs those parameters in another hashtable and pass that to the low level DBSApiLogic.
 	 * If any exception is occured then it takes that exception and converts it into a proper string message and writes it ont to the output stream  that gets transferred back to the client. Any hash table with any key value pair can be passed to this call method and it determines which low level DBSApiLogic api to call depending upon the value of api parameter.
 	 * @param out an output stream <code>java.io.Writer</code> object where this method writes the results into and passed it further down to the DBSApiLogic apis.
 	 * @param table a <code>java.util.Hashtable</code> that contains all the necessary key value pairs required for any particular api. It determines which api to call by the value of the api field in the table. For the various API the following key value parsie may or may not be required. <br>
 	 * listPrimaryDatasets - pattern <br>
 	 * listProcessedDatasets - primary_datatset_name_pattern, data_tier_name_pattern, processed_datatset_name_pattern, app_version,	app_family_name, app_executable_name, parameterset_name <br>
 	 * listAlgorithms - app_version, app_family_name, app_executable_name, 	parameterset_name <br>
 	 * listRuns - path <br>
 	 * listTiers - path <br>
 	 * listBlocks - path <br>
 	 * listFiles - path, block_name, pattern_lfn <br>
 	 * listDatasetContents - path<br>
 	 * insertPrimaryDataset - xmlinput <br>
 	 * insertAlgorithm - xmlinput <br>
 	 * insertRun - xmlinput <br>
 	 * insertTier - tier_name <br>
 	 * insertLumiSection - xmlinput <br>
 	 * insertProcessedDataset - xmlinput <br>
 	 * createAnalysisDatasetFromPD - xmlinput <br>
 	 * insertBlock - xmlinput <br>
 	 * insertFiles - xmlinput <br>
 	 * insertTierInPD - path, tier_name <br>
 	 * insertParentInPD - path, parent_path <br>
 	 * insertAlgoInPD - path, xmlinput <br>
 	 * insertRunInPD - path, run_number <br>
 	 * insertTierInFile - lfn, tier_name <br>
 	 * insertParentInFile - lfn, parent_lfn <br>
 	 * insertAlgoInFile - lfn, xmlinput <br>
 	 * insertLumiInFile - lfn, ls_number <br>
 	 * 
 	 * @param dbsUser a <code>java.util.Hashtable</code> that contains all the necessary key value pairs for a single user. The most import key in this table is the user_dn. This hashtable is used to insert the bookkeeping information with each row in the database. This is to know which user did the insert at the first place.
 	 * @throws Exception All the exceptions that are thrown by the underlying layesr are caught and written down in the output stream that goes back to the client in proper XML format. Only an un expected exception may be raised in this method when trying to wirte to the output stream, which may not propogate back to the client.
 	 */
 	public void call(Writer out, Hashtable table, Hashtable dbsUser) throws Exception {
                 
 		Connection conn = null;
 
 		try {
 
 			out.write(DBSConstants.XML_HEADER); 
 			String apiStr = get(table, "api", true);
         	        String apiVersion = get(table, "apiversion", true);
                 	DBSUtil.writeLog("apiStr: "+apiStr);
 
 	                checkVersion(get(table, "apiversion", true));
  
         	        checkSchemaVersion();
 
 
 			conn = getConnection();
 			conn.setAutoCommit(false);
 			if (apiStr.equals("listPrimaryDatasets")) {
 				//System.out.println("Pattern is "+ get(table, "pattern", false));
 				(new DBSApiPrimDSLogic(this.data)).listPrimaryDatasets(conn, out, get(table, "pattern", false));
 				
 			} else if (apiStr.equals("listProcessedDatasets")) {
 				(new DBSApiProcDSLogic(this.data)).listProcessedDatasets(conn, out, 
 						get(table, "primary_datatset_name_pattern", false),
 						get(table, "data_tier_name_pattern", false),
 						get(table, "processed_datatset_name_pattern", false),
 						get(table, "app_version", false),
 						get(table, "app_family_name", false),
 						get(table, "app_executable_name", false),
 						get(table, "ps_name", false)
 						);
 			} else if (apiStr.equals("listDatasetContents")) {
 				(new DBSApiTransferLogic(this.data)).listDatasetContents(conn, out, 
 						get(table, "path", false),
 						get(table, "block_name", false)
 						);
 			} else if (apiStr.equals("listDatasetParents")) {
 				(new DBSApiProcDSLogic(this.data)).listDatasetParents(conn, out, 
 						get(table, "path", true)
 						);
 
 			} else if (apiStr.equals("listAlgorithms")) {
 				(new DBSApiAlgoLogic(this.data)).listAlgorithms(conn, out,
 						get(table, "app_version", false),
 						get(table, "app_family_name", false),
 						get(table, "app_executable_name", false),
 						get(table, "ps_hash", false)
 						);
 				
 			} else if (apiStr.equals("listRuns")) {
 				(new DBSApiProcDSLogic(this.data)).listRuns(conn, out, get(table, "path", true));
 				
 			} else if (apiStr.equals("listTiers")) {
 				(new DBSApiProcDSLogic(this.data)).listTiers(conn, out, get(table, "path", true));
 				
 			} else if (apiStr.equals("listBlocks")) {
 				(new DBSApiBlockLogic(this.data)).listBlocks(conn, out, 
 						get(table, "path", false),
 						get(table, "block_name", false),
 						get(table, "storage_element_name", false)
 						);
 
 			} else if (apiStr.equals("listStorageElements")) {
 				(new DBSApiBlockLogic(this.data)).listStorageElements(conn, out, 
 						get(table, "storage_element_name", false)
 						);
 
 			} else if (apiStr.equals("listFiles")) {
 				(new DBSApiFileLogic(this.data)).listFiles(conn, out, 
 						get(table, "path", false),
 						get(table, "primary_dataset", false),
 						get(table, "processed_dataset", false),
 						get(table, "data_tier_list", false),
 						get(table, "analysis_dataset_name", false),
 						get(table, "block_name", false),
 						get(table, "pattern_lfn", false),
 						get(table, "detail", false)
 						);
 			} else if (apiStr.equals("listFileParents")) {
 				(new DBSApiFileLogic(this.data)).listFileProvenence(conn, out, 
 						get(table, "lfn", true),
 						true
 						);
 
 			} else if (apiStr.equals("listFileChilds")) {
 				(new DBSApiFileLogic(this.data)).listFileProvenence(conn, out, 
 						get(table, "lfn", true),
 						false
 						);
 
 			} else if (apiStr.equals("listFileAlgorithms")) {
 				(new DBSApiFileLogic(this.data)).listFileAlgorithms(conn, out, 
 						get(table, "lfn", true)
 						);
 			} else if (apiStr.equals("listFileTiers")) {
 				(new DBSApiFileLogic(this.data)).listFileTiers(conn, out, 
 						get(table, "lfn", true)
 						);
 			} else if (apiStr.equals("listFileLumis")) {
 				(new DBSApiFileLogic(this.data)).listFileLumis(conn, out, 
 						get(table, "lfn", true)
 						);
 			} else if (apiStr.equals("listFileBranches")) {
 				(new DBSApiFileLogic(this.data)).listFileBranches(conn, out, 
 						get(table, "lfn", true)
 						);
 			} else if (apiStr.equals("listAnalysisDataset")) {
                                 (new DBSApiAnaDSLogic(this.data)).listAnalysisDataset(conn, out, 
 									get(table, "analysis_dataset_name_pattern", false), 
 									get(table, "path", false)
 						);
                         } else if (apiStr.equals("listAnalysisDatasetDefinition")) {
                                 (new DBSApiAnaDSLogic(this.data)).listAnalysisDatasetDefinition(conn, out,
                                                                         get(table, "pattern_analysis_dataset_definition_name", false)
                                                 );
 
                         } else if (apiStr.equals("listRowsInTable")) {
 				(new DBSApiLogic(this.data)).listRowsInTable(conn, out,
      									get(table, "table_name", true),
      									get(table, "from", false),
      									get(table, "rows", false)
 			    						);
 
                         } else if (apiStr.equals("insertPrimaryDataset")) {
 				(new DBSApiPrimDSLogic(this.data)).insertPrimaryDataset(conn, out,
 						DBSApiParser.parse( getXml(table), "primary_dataset") , 
 						dbsUser);
 
 			} else if (apiStr.equals("updatePrimDSType")) {
 				(new DBSApiPrimDSLogic(this.data)).updatePrimDSType(conn, out,
 						get(table, "prmary_dataset_name", true),
 						get(table, "type", true),
 						dbsUser);
 
 			} else if (apiStr.equals("insertAlgorithm")) {
 				(new DBSApiAlgoLogic(this.data)).insertAlgorithm(conn, out,
 						DBSApiParser.parse(getXml(table), "algorithm") , 
 						dbsUser);
 			} else if (apiStr.equals("insertRun")) {
 				api.insertRun(conn, out,
 						DBSApiParser.parse(getXml(table), "run") , 
 						dbsUser);
 
                         } else if (apiStr.equals("updateRun")) {
                                 api.updateRun(conn, out,
                                                 DBSApiParser.parse(getXml(table), "run") ,
                                                 dbsUser);
 
 			} else if (apiStr.equals("insertTier")) {
 				api.insertTier(conn, out, DBSApiParser.parse(getXml(table), "tier"), dbsUser);
 				
 
 			} else if (apiStr.equals("insertStorageElement")) {
 				(new DBSApiBlockLogic(this.data)).insertStorageElement(conn, out, DBSApiParser.parse(getXml(table), "storage_element"), dbsUser);
 			
 			} else if (apiStr.equals("deleteSEFromBlock")) {
 				(new DBSApiBlockLogic(this.data)).deleteSEFromBlock(conn, out, DBSApiParser.parse(getXml(table), "storage_element"), dbsUser);
 			
 
 
 			} else if (apiStr.equals("insertLumiSection")) {
 				api.insertLumiSection(conn, out,
 						DBSApiParser.parse(getXml(table), "lumi_section") , 
 						dbsUser);
 				
 			} else if (apiStr.equals("insertProcessedDataset")) {
 				(new DBSApiProcDSLogic(this.data)).insertProcessedDataset(conn, out,  DBSApiParser.parsePD(getXml(table)), dbsUser);
 				
			/*} else if (apiStr.equals("createAnalysisDatasetFromPD")) {
 				(new DBSApiAnaDSLogic(this.data)).createAnalysisDatasetFromPD(conn, out,
 					DBSApiParser.parse(getXml(table), "analysis_dataset"),
					dbsUser);*/
 				
 			} else if (apiStr.equals("createAnalysisDatasetDefinition")) {
 				(new DBSApiAnaDSLogic(this.data)).createAnalysisDatasetDefinition(conn, out,  DBSApiParser.parseADD(getXml(table)), dbsUser);
 
 			} else if (apiStr.equals("createAnalysisDataset")) {
 				(new DBSApiAnaDSLogic(this.data)).createAnalysisDataset(conn, out,
 					DBSApiParser.parse(getXml(table), "analysis_dataset"),
 					dbsUser);
 	
 			} else if (apiStr.equals("updateAnalDSType")) {
 				(new DBSApiAnaDSLogic(this.data)).updateAnalDSType(conn, out,
 						get(table, "analysis_dataset_name", true),
 						get(table, "type", true),
 						dbsUser);
 
 			} else if (apiStr.equals("updateAnalDSStatus")) {
 				(new DBSApiAnaDSLogic(this.data)).updateAnalDSStatus(conn, out,
 						get(table, "analysis_dataset_name", true),
 						get(table, "status", true),
 						dbsUser);
 
                         } else if (apiStr.equals("insertBlock")) {
 				(new DBSApiBlockLogic(this.data)).insertBlock(conn, out,
 						DBSApiParser.parseBlock(getXml(table)) , 
 						dbsUser);
 				
 			} else if (apiStr.equals("insertFiles")) {
 
 				DBSApiParser.insertFiles(conn, out, get(table, "primary_dataset", false), 
 									get(table, "processed_dataset", false), 
 									getXml(table), dbsUser);
 	
 			} else if (apiStr.equals("updateFileStatus")) {
 				(new DBSApiFileLogic(this.data)).updateFileStatus(conn, out,
 						get(table, "lfn", true),
 						get(table, "status", true),
 						dbsUser);
 	
 			} else if (apiStr.equals("updateFileType")) {
 				(new DBSApiFileLogic(this.data)).updateFileType(conn, out,
 						get(table, "lfn", true),
 						get(table, "type", true),
 						dbsUser);
 
 
 
 			} else if (apiStr.equals("remapFiles")) {
 				DBSApiParser.remapFiles(conn, out, getXml(table), dbsUser);
 			
 			} else if (apiStr.equals("insertTierInPD")) {
 				(new DBSApiProcDSLogic(this.data)).insertTierInPD(conn, out,
 						DBSApiParser.parse(getXml(table), "processed_dataset"),
 						get(table, "tier_name", true), 
 						dbsUser);
 				
 			} else if (apiStr.equals("insertParentInPD")) {
 				(new DBSApiProcDSLogic(this.data)).insertParentInPD(conn, out,
 						DBSApiParser.parse(getXml(table), "processed_dataset"),
 						get(table, "parent_path", true), 
 						dbsUser);
 					
 			} else if (apiStr.equals("insertAlgoInPD")) {
 				(new DBSApiProcDSLogic(this.data)).insertAlgoInPD(conn, out,
 						DBSApiParser.parse(getXml(table), "processed_dataset"),
 						DBSApiParser.parse(getXml(table), "algorithm"), 
 						dbsUser);
 			
 			} else if (apiStr.equals("insertRunInPD")) {
 				(new DBSApiProcDSLogic(this.data)).insertRunInPD(conn, out,
 						DBSApiParser.parse(getXml(table), "processed_dataset"),
 						get(table, "run_number", true), 
 						dbsUser);
 	
 			} else if (apiStr.equals("updateProcDSStatus")) {
 				(new DBSApiProcDSLogic(this.data)).updateProcDSStatus(conn, out,
 						get(table, "path", true),
 						get(table, "status", true),
 						dbsUser);
 				
 			} else if (apiStr.equals("insertTierInFile")) {
 				(new DBSApiFileLogic(this.data)).insertTierInFile(conn, out,
 						DBSApiParser.parse(getXml(table), "file"),
 						get(table, "tier_name", true), 
 						dbsUser);
 				
 			} else if (apiStr.equals("insertParentInFile")) {
 				(new DBSApiFileLogic(this.data)).insertParentInFile(conn, out,
 						DBSApiParser.parse(getXml(table), "file"),
 						get(table, "parent_lfn", true), 
 						dbsUser);
 				
 			} else if (apiStr.equals("insertAlgoInFile")) {
 				(new DBSApiFileLogic(this.data)).insertAlgoInFile(conn, out,
 						DBSApiParser.parse(getXml(table), "file"),
 						DBSApiParser.parse(getXml(table), "algorithm"), 
 						dbsUser);
 				
 			} else if (apiStr.equals("insertLumiInFile")) {
 				(new DBSApiFileLogic(this.data)).insertLumiInFile(conn, out,
 						DBSApiParser.parse(getXml(table), "file"),
 						get(table, "ls_number", true), 
 						dbsUser);
 				
 			} else if (apiStr.equals("insertDatasetContents")) {
 				(new DBSApiTransferLogic(this.data)).insertDatasetContents(conn, out,
 						DBSApiParser.parseDatasetContents(getXml(table)), 
 						dbsUser);
 	
 			} else if (apiStr.equals("closeBlock")) {
 				(new DBSApiBlockLogic(this.data)).closeBlock(conn, out,
 						get(table, "block_name", true),
 						dbsUser);
 		
 			} else {
 				writeException(out, "Invalid API", "1018", "The api " + apiStr + " provided by the client is not valid");
 				return;
 			}
 			conn.commit();
 		} catch (DBSException dbsEx) {
 			if(conn != null) conn.rollback();
 			if (dbsEx.getMessage() == null ) {
 				writeException(out, "Unexpected execution exception", "1080", "NULL POINTER DBSException");
 				return;
 			}
 			//dbsEx.printStackTrace();
 			writeException(out, dbsEx.getMessage(), dbsEx.getCode(), dbsEx.getDetail());
 			return;
 		} catch (XMLException xmlEx) {
 			if(conn != null) conn.rollback();
 			if (xmlEx.getMessage() == null ) {
 				writeException(out, "Unexpected execution exception", "3003", "NULL POINTER XMLException");
 				return;
 			}
 			//xmlEx.printStackTrace();
 			writeException(out, xmlEx.getMessage(), xmlEx.getCode(), xmlEx.getDetail());
 			return;
 		} catch (SQLException sqlEx) {
 			if(conn != null) conn.rollback();
 			if (sqlEx.getMessage() == null ) {
 				writeException(out, "Unexpected execution exception", "2001", "NULL POINTER SQLException");
 				return;
 			}
 			//sqlEx.printStackTrace();
 			writeException(out, "Database exception", "2000", sqlEx.getMessage());
 			return;
 		} catch (Exception ex) {
 			if(conn != null) conn.rollback();
 			ex.printStackTrace();
 			if (ex.getMessage() == null ) {
 				writeException(out, "Unexpected execution exception", "4001", "NULL POINTER Exception");
 				return;
 			}
 			//writeException(out, "Unexpected execution exception", "4000", ex.getMessage());
 			return;
 		} finally {
 			if(conn != null) {
 				conn.clearWarnings();
 				conn.close();
 			}
 		}
 
 		out.write(DBSConstants.XML_SUCCESS);
 		out.write(DBSConstants.XML_FOOTER);
 		out.flush();
 	}
 	
 	private String get(Hashtable table, String key, boolean excep) throws Exception {
 		String value = "";
 		if ( isNull(value = DBSUtil.get(table, key)) ) {
 			if (excep) throw new DBSException("Mandatory data missing", "1004", "Null Fields. Expected a valid " + key);
 		}
 		return value;
 	}
 	
 	
 	private String getXml(Hashtable table) throws Exception {
 		return 	get(table, "xmlinput", true);
 	}
 	
 	public void writeException(Writer out, String message, String code, String detail) throws Exception {
 		//out.write(DBSConstants.XML_EXCEPTION_HEADER); 
 		 message = message.replace('\'',' ');
 		message = message.replace('<',' ');
 		message = message.replace('>',' ');
                 detail= detail.replace('\'',' ');
 		detail = detail.replace('<',' ');
 		detail = detail.replace('>',' ');
 		code = code.replace('\'',' ');
 		code = code.replace('<',' ');
 		code = code.replace('>',' ');
 		out.write("<exception message='" + message + "' "); 
 		out.write(" code ='" + code + "' "); 
 		out.write(" detail ='" + detail + "' />\n"); 
 		out.write(DBSConstants.XML_FOOTER);
 		out.flush();
 		//out.write(DBSConstants.XML_EXCEPTION_FOOTER); 
 	}
 
 
 	/*
 	
 	private void writeWarnning(Writer out, String message) throws Exception {
 		out.write(DBSConstants.XML_WARNNING_HEADER); 
 		out.write(message); 
 		out.write(DBSConstants.XML_WARNNING_FOOTER); 
 	}*/
 
 	private Connection getConnection() throws Exception {
 		Connection conn = DBManagement.getDBConnManInstance().getConnection();
 		if (conn != null) {
        			DBSUtil.writeLog("Pooling at work");
 			return conn;
 		} else {
 			DBSUtil.writeLog("Pooling not required for standalone client");
 			DBSConfig config = DBSConfig.getInstance();
 			return DBManagement.getConnection( config.getDbDriver(),
 					config.getDbURL(), 
 					config.getDbUserName(),
 					config.getDbUserPasswd());
 					
 		}
 		
 	}
 	private boolean isNull(String pattern) {
 		return DBSUtil.isNull(pattern);
 	}
 	
 }
