 /**
 $Revision: 1.137 $"
 $Id: DBSApiFileLogic.java,v 1.137 2009/06/18 19:45:22 afaq Exp $"
  *
  */
 
 package dbs.api;
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.PreparedStatement;
 import java.io.Writer;
 import java.util.Hashtable;
 import java.util.Vector;
 import java.util.ArrayList;
 import dbs.sql.DBSSql;
 import dbs.util.DBSUtil;
 import codec.Base64;
 import dbs.DBSException;
 import java.sql.SQLException;
 import db.DBManagement;
 
 /**
 * A class that has the core business logic of all the File APIs.  The signature for the API is internal to DBS and is not exposed to the clients. There is another class <code>dbs.api.DBSApi</code> that has an interface for the clients. All these low level APIs are invoked from <code>dbs.api.DBSApi</code>. This class inherits from DBSApiLogic class.
 * @author sekhri
 */
 public class DBSApiFileLogic extends DBSApiLogic {
 		
 	/**
 	* Constructs a DBSApiLogic object that can be used to invoke several APIs.
 	*/
 
 	DBSApiPersonLogic personApi = null;
 	DBSApiData data = null;
 	public DBSApiFileLogic(DBSApiData data) {
 		super(data);
 		this.data = data;
 		personApi = new DBSApiPersonLogic(data);
 	}
 
 	private boolean contains(ArrayList attributes, String param) {
 		return DBSUtil.contains(attributes, param);
 	}
 	/*public void listFiles(Connection conn, 
 			Writer out, 
 			String path, 
 			String primary, 
 			String proc, 
 			String dataTierList, 
 			String aDSName, 
 			String blockName, 
 			String patternLFN, 
 			String runNumber,
 			ArrayList attributes,
 			String clientVersion
 			) throws Exception {
 		listFiles(conn, out, path,primary, proc, dataTierList, aDSName, blockName, patternLFN, runNumber, attributes, clientVersion, "True", "True");
 	}
 
 	public void listFiles(Connection conn, 
 			Writer out, 
 			String path, 
 			String primary, 
 			String proc, 
 			String dataTierList, 
 			String aDSName, 
 			String blockName, 
 			String patternLFN, 
 			String runNumber,
 			ArrayList attributes,
 			String clientVersion,
 			String detail,
 			String otherDetail
 			) throws Exception {*/
 
 	public void listFiles(Connection conn, 
 			Writer out, 
 			String path, 
 			String aDSName, 
 			String blockName, 
 			String patternLFN, 
 			String runNumber,
 			ArrayList attributes,
 			String clientVersion,
 			String detail,
 			String otherDetail,
 			Boolean isMigrate
 			) throws Exception {
 
 		boolean allOtherDetails = false;
 		boolean listInvalidFiles = false;
 		boolean useJustPath = false;
 		String procDSID = null;
 		String aDSID = null;
 		String blockID = null;
 		String runID = null;
 		String patternlfn = "";
 		Vector tierIDList = new Vector();
 		if(otherDetail.equals("True")) allOtherDetails = true;
 			
 		if(contains(attributes, "retrive_invalid_files")) listInvalidFiles = true;
 		//Search can be based on LFN pattern
 		if(!isNull(patternLFN)) patternlfn = getPattern(patternLFN, "pattern_lfn");
 
 		if (!isNull(path)) procDSID = (new DBSApiProcDSLogic(this.data)).getProcessedDSID(conn, path, true);
 		if (!isNull(runNumber)) runID = getID(conn, "Runs", "RunNumber", runNumber , true);
 		//Search can be based on Analysis Dataset
 		if (!isNull(aDSName)) aDSID = (new DBSApiAnaDSLogic(this.data)).getADSID(conn, aDSName, true);
 	
 		//Search can be based on Block Name
 		if(!isNull(blockName)) 	blockID = (new DBSApiBlockLogic(this.data)).getBlockID(conn, blockName, false, true);
 
 		if(isNull(blockID) && isNull(procDSID) && isNull(aDSID) && (patternlfn.equals("%") || isNull(patternlfn)))
                       throw new DBSException("Missing data", "1005",
                           "Null Fields. Expected either a (Primary, Processed) Dataset, Analysis Dataset, Block or LFN pattern");
 
 		//Block is always required
 		//At least until everyone switches to DBS_1_0_9 and CRAB stop using BLOCK infor from listFiles()
 		attributes.add("retrive_block");
 
 		//if old client then send empty xml
 		boolean oldClients = false;
 		if(clientVersion.compareTo("DBS_1_0_9") < 0) oldClients = true;
 		PreparedStatement ps = null;
 		ResultSet rs =  null;
 		try {
 			//ps = DBSSql.listFiles(conn, procDSID, aDSID, blockID, tierIDList, patternlfn, listInvalidFiles);
 			ps = DBSSql.listFiles(conn, procDSID, path, runID, aDSID, blockID, tierIDList, patternlfn, attributes);
 			pushQuery(ps);
 			rs =  ps.executeQuery();
 			while(rs.next()) {
 
 				String fileID = get(rs, "ID");
 				String lfn = get(rs, "LFN");
 				String toSend = "<file id='" + fileID +
 					"' lfn='" + lfn +
 					"' checksum='" + get(rs, "CHECKSUM") +
 					"' adler32='" + get(rs, "ADLER32") +
 					"' md5='" + get(rs, "MD5") +
 					"' size='" + get(rs, "FILESIZE") +
 					"' queryable_meta_data='" + get(rs, "QUERYABLE_META_DATA") +
 					"' number_of_events='" + get(rs, "NUMBER_OF_EVENTS") + "'";
 					if (!DBSUtil.isNull(get(rs, "AUTO_CROSS_SECTION"))) toSend += " auto_cross_section='" + get(rs, "AUTO_CROSS_SECTION") + "'";
 				
 					if(oldClients) {
 						if(DBSUtil.contains(attributes, "retrive_status")) toSend += " validation_status='" + get(rs, "VALIDATION_STATUS") + 	"' status='" + get(rs, "STATUS") + "'";
 						else toSend += " validation_status='' status=''";
 
 
 						if(DBSUtil.contains(attributes, "retrive_type")) toSend += " type='" + get(rs, "TYPE") + "'";
 						else toSend += " type=''";
 
 						if(DBSUtil.contains(attributes, "retrive_block")) toSend += " block_name='" + get(rs, "BLOCK_NAME") + "'";
 						else toSend += " block_name=''";
 
 						if(DBSUtil.contains(attributes, "retrive_date")) toSend += " creation_date='" + getTime(rs, "CREATION_DATE") +	"' last_modification_date='" + get(rs, "LAST_MODIFICATION_DATE") + "'";
 						else toSend += " creation_date='0' last_modification_date='0'" ;
 
 						if(DBSUtil.contains(attributes, "retrive_person")) toSend += " created_by='" + get(rs, "CREATED_BY") +	 "' last_modified_by='" + get(rs, "LAST_MODIFIED_BY") + "'";
 						else toSend += " created_by='' last_modified_by=''";
 
 
 					} else {
 						if(DBSUtil.contains(attributes, "retrive_status")) toSend += " validation_status='" + get(rs, "VALIDATION_STATUS") + "' status='" + get(rs, "STATUS") + "'";
 						if(DBSUtil.contains(attributes, "retrive_type")) toSend += " type='" + get(rs, "TYPE") + "'";
 						if(DBSUtil.contains(attributes, "retrive_block")) toSend += " block_name='" + get(rs, "BLOCK_NAME") + "'";
 						if(DBSUtil.contains(attributes, "retrive_date")) toSend += " creation_date='" + getTime(rs, "CREATION_DATE") +	"' last_modification_date='" + get(rs, "LAST_MODIFICATION_DATE") + "'";
 						if(DBSUtil.contains(attributes, "retrive_person")) toSend += " created_by='" + get(rs, "CREATED_BY") +
 							 "' last_modified_by='" + get(rs, "LAST_MODIFIED_BY")  + "'";
 					}
 					toSend += ">\n";
 				out.write(toSend);
 				this.data.localFile = new Hashtable();
 				this.data.localFile.put(lfn, fileID);
 				boolean otherDetailsOfParent = allOtherDetails;
 				if(DBSUtil.contains(attributes, "retrive_parent_block")) otherDetailsOfParent = true;
 				if(oldClients && detail.equals("True")) {
 					listFileProvenence(conn, out, lfn, true, listInvalidFiles, otherDetailsOfParent, isMigrate);//Parents
 					listFileProvenence(conn, out, lfn, false, listInvalidFiles, otherDetailsOfParent, isMigrate);//Children
 					listFileAlgorithms(conn, out, lfn, allOtherDetails);
 					listFileLumis(conn, out, lfn, allOtherDetails);
 					//listFileRuns(conn, out, lfn);
 					listBranch(conn, out, get(rs, "FILE_BRANCH"), allOtherDetails);
 
 				}else if( detail.equals("True")) {	
 					listFileProvenence(conn, out, lfn, true, listInvalidFiles,otherDetailsOfParent, isMigrate);//Parents
 					listFileProvenence(conn, out, lfn, false, listInvalidFiles, otherDetailsOfParent, isMigrate);//Children
 					listFileAlgorithms(conn, out, lfn, allOtherDetails);
 					listFileLumis(conn, out, lfn, allOtherDetails);
 					//listFileRuns(conn, out, lfn);
 					listBranch(conn, out, get(rs, "FILE_BRANCH"), allOtherDetails);
 
 					
 				} else {	
 					if(DBSUtil.contains(attributes, "retrive_parent")) listFileProvenence(conn, out, lfn, true, listInvalidFiles, otherDetailsOfParent, isMigrate);//Parents
 					if(DBSUtil.contains(attributes, "retrive_child")) listFileProvenence(conn, out, lfn, false, listInvalidFiles, otherDetailsOfParent, isMigrate);//Children
 					if(DBSUtil.contains(attributes, "retrive_algo")) listFileAlgorithms(conn, out, lfn, allOtherDetails);
 					if(DBSUtil.contains(attributes, "retrive_lumi")) {
 									if (!isNull(aDSID) ) listADSFileLumis(conn, out, aDSID, lfn);
 									else listFileLumis(conn, out, lfn, allOtherDetails);
 					}
 					if(DBSUtil.contains(attributes, "retrive_lumi_excluded")) {
 									if (!isNull(aDSID) ) listADSFileLumisExcluded(conn, out, aDSID, lfn);
 					}
 
 					if(DBSUtil.contains(attributes, "retrive_run")) {
 									if (!isNull(aDSID) ) listADSFileRuns(conn, out, aDSID, lfn);
 									else listFileRuns(conn, out, lfn);
 					}
 					if(DBSUtil.contains(attributes, "retrive_branch")) listBranch(conn, out, get(rs, "FILE_BRANCH"), allOtherDetails);
 				}
                 		out.write(((String) "</file>\n"));
       
 			}
 		} finally { 
 			if (rs != null) rs.close();
 			if (ps != null) ps.close();
 		}
 
 	}
 
 
 	private void writeFileParentXML(Writer out, ResultSet rs, String tag) throws Exception {
 
 
                                                 out.write(((String) "<" + tag + " id='" +  get(rs, "ID") +
                                                 "' lfn='" + get(rs, "LFN") +
                                                 "' checksum='" + getNoExcep(rs, "CHECKSUM") +
                                                 "' adler32='" + getNoExcep(rs, "ADLER32") +
                                                 "' md5='" + getNoExcep(rs, "MD5") +
                                                 "' size='" + getNoExcep(rs, "FILESIZE") +
                                                 "' queryable_meta_data='" + getNoExcep(rs, "QUERYABLE_META_DATA") +
                                                 "' number_of_events='" + getNoExcep(rs, "NUMBER_OF_EVENTS") +
                                                 "' validation_status='" + getNoExcep(rs, "VALIDATION_STATUS") +
                                                 "' type='" + getNoExcep(rs, "TYPE") +
                                                 "' status='" + getNoExcep(rs, "STATUS") +
                                                 "' block_name='" + getNoExcep(rs, "BLOCK_NAME") +
                                                 "' creation_date='" + getNoExcep(rs, "CREATION_DATE") +
                                                 "' last_modification_date='" + getNoExcep(rs, "LAST_MODIFICATION_DATE") +
                                                 "' created_by='" + getNoExcep(rs, "CREATED_BY") +
                                                 "' last_modified_by='" + getNoExcep(rs, "LAST_MODIFIED_BY") +
                                                 "' auto_cross_section='" + getNoExcep(rs, "AUTO_CROSS_SECTION") +
                                                 "'/>\n"));
 	}
 
 	private ArrayList listFileParentDatasets(Connection conn, String lfn)  throws Exception  {
                 PreparedStatement ps = null;
                 ResultSet rs =  null;
 
 		ArrayList fileParentDS = new ArrayList();
                 try {
                         ps = DBSSql.listFileParentDatasets(conn, lfn);
                         pushQuery(ps);
                         rs =  ps.executeQuery();
                         while(rs.next()) {
 				fileParentDS.add(get(rs, "ID"));
                         }
                 } finally {
                         if (rs != null) rs.close();
                         if (ps != null) ps.close();
                 }
 		return fileParentDS;
 	}
 
 
         private String listFileDataset(Connection conn, String lfn)  throws Exception  {
                 PreparedStatement ps = null;
                 ResultSet rs =  null;
 		String result = "";
                 ArrayList fileParentDS = new ArrayList();
                 try {
                         ps = DBSSql.listFileDataset(conn, lfn);
                         pushQuery(ps);
                         rs =  ps.executeQuery();
                         while(rs.next()) {
                                 result = get(rs, "PATH");
                         }
                 } finally {
                         if (rs != null) rs.close();
                         if (ps != null) ps.close();
                 }
                 return result;
         }
 
 	/**
 	 * Lists all the parents of the given file.  This method makes one sql query, execute it, fetch the results and packs and write it in xml format to the output stream. The query that it executes get generated by <code>dbs.DBSSql.listFileParents</code> method. It writes a list of logical file names on the output stream.  A sample XML that is written to the output stream is like <br>
 	 * <code> <""/></code>
 	 * @param conn a database connection <code>java.sql.Connection</code> object created externally.
 	 * @param out an output stream <code>java.io.Writer</code> object where this method writes the results into.
 	 * @param lfn the logical file name of the file whose parents needs to be listed.
 	 * @param parentOrChild a logical flag to determine weather parent or child is asked for. True - is for parent and False is for child
 	 * @throws Exception Various types of exceptions can be thrown. Commonly they are thrown if the supplied lfn is invalid, the database connection is unavailable or the file is not found.
 	 */
 
 
 	 //public void listFileParents(Connection conn, Writer out, String lfn) throws Exception {
 	 public void listFileProvenence(Connection conn, Writer out, String lfn, boolean parentOrChild) throws Exception {
 		 listFileProvenence(conn, out, lfn, parentOrChild, true, true, false);
 		 //Note that by default the invalid files will be listed . This is a required behaviour.
 	 }
 	 public void listFileProvenence(Connection conn, Writer out, String lfn, boolean parentOrChild, boolean listInvalidFiles) throws Exception {
 		 listFileProvenence(conn, out, lfn, parentOrChild, listInvalidFiles, true, false);
 		 //Note that by default the invalid files will be listed . This is a required behaviour.
 	 }
 
 	 public void listFileProvenence(Connection conn, Writer out, String lfn, boolean parentOrChild, boolean listInvalidFiles, boolean detail, Boolean isMigrate) throws Exception {
 		PreparedStatement ps = null;
 		ResultSet rs =  null;
 
 		ArrayList parents=new ArrayList();
 		ArrayList parentdatasets=new ArrayList();
 	
 		//Make sure same file cannot be added here
 		parents.add(lfn);
 
 		// We list parents of a file
 		// If its a normal file (i.e. the parent file) than add it to out going list of parents
 		// If its a unmerged file
 		//		:Make sure that its parent is a Merged file, add as it is, if its an unmerged file, find its child that should be an merged file
 		// THIS SUCKS BIG TIME -AA,VS : 03/17/2009
 	
 		try {
 			ps = DBSSql.listFileProvenence(conn, getFileID(conn, lfn, true), parentOrChild, listInvalidFiles, detail);
 			pushQuery(ps);
 			rs =  ps.executeQuery();
 		
 			//Shunting the code that tests for migration codition, we seems not to need that !
 			isMigrate=false;
 
 			String tag = "";
 			if(parentOrChild) tag = "file_parent";
 			else tag = "file_child";
 
 			boolean flag=true;
 			while(rs.next()) {
 				String plfn = get(rs, "LFN");
 				if ( isMigrate ) {
 
 					if (flag) {
 						String filedataset=listFileDataset(conn, lfn); 
 						parentdatasets=(new DBSApiBlockLogic(this.data)).listPathParentIDs(conn, out, filedataset); 
 						flag=false;
 					}
 					//TREAT parent files only
 					if (plfn.indexOf("unmerged") != -1 && parentOrChild) {
 							ResultSet tmpRSpp= null;
 							try {
 								ps = DBSSql.listFileProvenence(conn, getFileID(conn, plfn, true), true, false, true);
 								tmpRSpp=ps.executeQuery();
 								while(tmpRSpp.next()) {
 									String ppfile=get(tmpRSpp, "LFN");
 									if (ppfile.indexOf("unmerged") != -1)	{
 										ResultSet tmpRSppc=null;
 										try {		
 											ps = DBSSql.listFileProvenence(conn, getFileID(conn, ppfile, true), false, false, true);
 											tmpRSppc=ps.executeQuery();
 											ArrayList ppcVec=new ArrayList();
 											while(tmpRSppc.next()) {
 												String	ppclfn=get(tmpRSppc, "LFN");
 												if (ppclfn.indexOf("unmerged") != -1) continue;
 												else {
 													ppcVec.add(ppclfn);
 													if (!parents.contains(ppclfn)) {
 														String pds = get(tmpRSppc, "DATASET");
 														if (parentdatasets.contains(pds)){
 															writeFileParentXML(out, tmpRSppc, tag);
 															parents.add(ppclfn);
 														}
 													}
 												}
 											}//while(tmpRSppc.next())
 											if ( ppcVec.size() <= 0 )
                                                                     					new DBSException("Missing data", "1005",
                                                                               					"Expected to find a corresponding merged file for "+ppfile);
 										} finally {if (tmpRSppc != null) tmpRSppc.close();}
 									}
 									else {
 										if (!parents.contains(ppfile)) { 
 											String pds = get(tmpRSpp, "DATASET");
 											if (parentdatasets.contains(pds)){
 												parents.add(ppfile); 
 												writeFileParentXML(out, tmpRSpp, tag);
 											}
 										} 
 									}
 								}//while(tmpRSpp.next())
 							} finally {if (tmpRSpp != null) tmpRSpp.close();}
 					} // if (parentOrChild==true) && (plfn.indexOf("unmerged") != -1)
 					else if (!parents.contains(plfn) && plfn.indexOf("unmerged") == -1 ) {
 						//String pds = get(rs, "DATASET");
 						//if (parentdatasets.contains(pds)){
                                         		parents.add(plfn);
                                         		writeFileParentXML(out, rs, tag);
 						//}
                                         }
 				}//if ( isMigrate == true )
 				else if (!parents.contains(plfn)) {
 						parents.add(plfn);
 						writeFileParentXML(out, rs, tag);
 				}// if (!parents.contains(plfn))
 			}//While
 		} finally { 
 			if (rs != null) rs.close();
 			if (ps != null) ps.close();
 		}
 	 }
 
 
          public void listFileTrigs(Connection conn, Writer out, String lfn) throws Exception {
                 PreparedStatement ps = null;
                 ResultSet rs =  null;
                 try {
                         ps = DBSSql.listFileTrigs(conn, getFileID(conn, lfn, true));
 			pushQuery(ps);
                         rs =  ps.executeQuery();
                         while(rs.next()) {
                                 out.write(((String) "<file_trigger_tag id='" + get(rs, "ID") +
 							"' trigger_tag='" + get(rs, "TRIGGER_TAG") +
 							"' number_of_events='" + get(rs, "NUMBER_OF_EVENTS") +
                                         "' creation_date='" + getTime(rs, "CREATION_DATE") +
                                         "' last_modification_date='" + get(rs, "LAST_MODIFICATION_DATE") +
                                         "' created_by='" + get(rs, "CREATED_BY") +
                                         "' last_modified_by='" + get(rs, "LAST_MODIFIED_BY") +
                                         "'/>\n"));
                         }
                 } finally {
                         if (rs != null) rs.close();
                         if (ps != null) ps.close();
                 }
          }
 
 
  	 /**
 	 * Lists all the algorithm configs of the given file.  This method makes one sql query, execute it, fetch the results and packs and write it in xml format to the output stream. The query that it executes get generated by <code>dbs.DBSSql.listFileAlgorithms</code> method. It writes a list of algorithms on the output stream.  A sample XML that is written to the output stream is like <br>
 	 * <code> <""/></code>
 	 * @param conn a database connection <code>java.sql.Connection</code> object created externally.
 	 * @param out an output stream <code>java.io.Writer</code> object where this method writes the results into.
 	 * @param lfn the logical file name of the file whose algorithms needs to be listed.
 	 * @throws Exception Various types of exceptions can be thrown. Commonly they are thrown if the supplied lfn is invalid, the database connection is unavailable or the file is not found.
 	 */
 	 public void listFileAlgorithms(Connection conn, Writer out, String lfn) throws Exception {
 		 listFileAlgorithms(conn, out, lfn, true);
 	 }
 	 public void listFileAlgorithms(Connection conn, Writer out, String lfn, boolean detail) throws Exception {
 		PreparedStatement ps = null;
 		ResultSet rs =  null;
 		try {
 			ps = DBSSql.listFileAlgorithms(conn, getFileID(conn, lfn, true), detail);
 			pushQuery(ps);
 			rs =  ps.executeQuery();
 			while(rs.next()) {
 				out.write(((String) "<file_algorithm id='" + get(rs, "ID") + 
 					"' app_version='" + get(rs, "APP_VERSION") +
 					"' app_family_name='" + get(rs, "APP_FAMILY_NAME") +
 					"' app_executable_name='" + get(rs, "APP_EXECUTABLE_NAME") +
 					"' ps_name='" + getNoExcep(rs, "PS_NAME") +
 					"' ps_hash='" + get(rs, "PS_HASH") +
 					"' creation_date='" + getNoExcep(rs, "CREATION_DATE") +
 					"' last_modification_date='" + getNoExcep(rs, "LAST_MODIFICATION_DATE") +
 					"' created_by='" + getNoExcep(rs, "CREATED_BY") +
 					"' last_modified_by='" + getNoExcep(rs, "LAST_MODIFIED_BY") +
 					"'/>\n"));
 			}
 		} finally { 
 			if (rs != null) rs.close();
 			if (ps != null) ps.close();
 		}
 	 }
 
         public void listBranch(Connection conn, Writer out, String branchId) throws Exception {
 		listBranch(conn, out, branchId, true);
 	}
         public void listBranch(Connection conn, Writer out, String branchId, boolean detail) throws Exception {
 
 		if (isNull(branchId)) return;
 
                 PreparedStatement ps = null;
                 ResultSet rs =  null;
                 try {
                         ps = DBSSql.listBranch(conn, branchId, detail);
 			pushQuery(ps);
                         rs =  ps.executeQuery();
                         while(rs.next()) {
                                 out.write(((String) "<file_branch id='" + get(rs, "ID") +
                                                         "' hash='" + get(rs, "NAME") +
 							"' content='" + Base64.encodeBytes(get(rs, "CONTENT").getBytes()) +
                                                         "' description='" + get(rs, "DESCRIPTION") +
                                                         "' creation_date='" + getNoExcep(rs, "CREATION_DATE") +
                                                         "' last_modification_date='" + getNoExcep(rs, "LAST_MODIFICATION_DATE") +
                                                         "' created_by='" + getNoExcep(rs, "CREATED_BY") +
                                                         "' last_modified_by='" + getNoExcep(rs, "LAST_MODIFIED_BY") +
                                                         "'/>\n"));
                         }
                 } finally {
                         if (rs != null) rs.close();
                         if (ps != null) ps.close();
                 }
         }
 
 	public void listFileBranches(Connection conn, Writer out, String lfn) throws Exception {
 		String branchID = null;
 
 		PreparedStatement ps = null;
 		ResultSet rs =  null;
 		try {
 			ps = DBSSql.listFileBranchID(conn, lfn);
 			pushQuery(ps);
 			rs =  ps.executeQuery();
 			
 			if (rs.next()) branchID = get(rs, "FILE_BRANCH");
 			else throw new DBSException("Missing data", "1091", "File does not exist : " + lfn);
 
 		} finally {
 			if (rs != null) rs.close();
 			if (ps != null) ps.close();
 		}
 
 		if (!isNull(branchID)) {
 			listBranch(conn, out, branchID);
 		}
 		else {
 			throw new DBSException("Missing data", "1091", "File Branches do not exist for this file: " + lfn);
 		}
 
 	}
 
 
  	 /**
 	 * Lists all the lumi sections of the given file.  This method makes one sql query, execute it, fetch the results and packs and write it in xml format to the output stream. The query that it executes get generated by <code>dbs.DBSSql.listFileLumis</code> method. It writes a list of lumi sections on the output stream.  A sample XML that is written to the output stream is like <br>
 	 * <code> <""/></code>
 	 * @param conn a database connection <code>java.sql.Connection</code> object created externally.
 	 * @param out an output stream <code>java.io.Writer</code> object where this method writes the results into.
 	 * @param lfn the logical file name of the file whose lumi sections needs to be listed.
 	 * @throws Exception Various types of exceptions can be thrown. Commonly they are thrown if the supplied lfn is invalid, the database connection is unavailable or the file is not found.
 	 */
 	 public void listFileLumis(Connection conn, Writer out, String lfn) throws Exception {
 		 listFileLumis(conn, out, lfn, true);
 	 }
 	 public void listFileLumis(Connection conn, Writer out, String lfn, boolean detail) throws Exception {
 		PreparedStatement ps = null;
 		ResultSet rs =  null;
 		try {
 			ps = DBSSql.listFileLumis(conn, getFileID(conn, lfn, true), detail);
 			pushQuery(ps);
 			rs =  ps.executeQuery();
 			while(rs.next()) {
 				out.write(((String) "<file_lumi_section id='" +  get(rs, "ID") +
 					"' lumi_section_number='" + get(rs, "LUMI_SECTION_NUMBER") +
 					"' run_number='" + get(rs, "RUN_NUMBER") +
 					"' start_event_number='" + get(rs, "START_EVENT_NUMBER") +
 					"' end_event_number='" + get(rs, "END_EVENT_NUMBER") +
 					"' lumi_start_time='" + get(rs, "LUMI_START_TIME") +
 					"' lumi_end_time='" + get(rs, "LUMI_END_TIME") +
 					"' creation_date='" + getNoExcep(rs, "CREATION_DATE") +
 					"' last_modification_date='" + getNoExcep(rs, "LAST_MODIFICATION_DATE") +
 					"' created_by='" + getNoExcep(rs, "CREATED_BY") +
 					"' last_modified_by='" + getNoExcep(rs, "LAST_MODIFIED_BY") +
 					"'/>\n"));
 
 				}
 		} finally { 
 			if (rs != null) rs.close();
 			if (ps != null) ps.close();
 		}
 	 }
 
 	public void listADSFileLumisExcluded(Connection conn, Writer out, String aDSID, String lfn) throws Exception {
 		PreparedStatement ps = null;
 		ResultSet rs =  null;
 		try {
 			ps = DBSSql.listADSFileLumisExcluded(conn, aDSID, getFileID(conn, lfn, true));
 			pushQuery(ps);
 			rs =  ps.executeQuery();
 			while(rs.next()) {
 				out.write(((String) "<file_lumi_section_excluded id='" +  get(rs, "ID") +
 							"' lumi_section_number='" + get(rs, "LUMI_SECTION_NUMBER") +
 							"' run_number='" + get(rs, "RUN_NUMBER") +
 							"' start_event_number='" + get(rs, "START_EVENT_NUMBER") +
 							"' end_event_number='" + get(rs, "END_EVENT_NUMBER") +
 							"' lumi_start_time='" + get(rs, "LUMI_START_TIME") +
 							"' lumi_end_time='" + get(rs, "LUMI_END_TIME") +
 							"' creation_date='" + getTime(rs, "CREATION_DATE") +
 							"' last_modification_date='" + get(rs, "LAST_MODIFICATION_DATE") +
 							"' created_by='" + get(rs, "CREATED_BY") +
 							"' last_modified_by='" + get(rs, "LAST_MODIFIED_BY") +
 							"'/>\n"));
 			}
 		} finally {
 			if (rs != null) rs.close();
 			if (ps != null) ps.close();
 		}
 	}
 
 
          public void listADSFileLumis(Connection conn, Writer out, String aDSID, String lfn) throws Exception {
                 PreparedStatement ps = null;
                 ResultSet rs =  null;
                 try {
                         ps = DBSSql.listADSFileLumis(conn, aDSID, getFileID(conn, lfn, true));
                         pushQuery(ps);
                         rs =  ps.executeQuery();
                         while(rs.next()) {
                                 out.write(((String) "<file_lumi_section id='" +  get(rs, "ID") +
                                         "' lumi_section_number='" + get(rs, "LUMI_SECTION_NUMBER") +
                                         "' run_number='" + get(rs, "RUN_NUMBER") +
                                         "' start_event_number='" + get(rs, "START_EVENT_NUMBER") +
                                         "' end_event_number='" + get(rs, "END_EVENT_NUMBER") +
                                         "' lumi_start_time='" + get(rs, "LUMI_START_TIME") +
                                         "' lumi_end_time='" + get(rs, "LUMI_END_TIME") +
                                         "' creation_date='" + getTime(rs, "CREATION_DATE") +
                                         "' last_modification_date='" + get(rs, "LAST_MODIFICATION_DATE") +
                                         "' created_by='" + get(rs, "CREATED_BY") +
                                         "' last_modified_by='" + get(rs, "LAST_MODIFIED_BY") +
                                         "'/>\n"));
 
                                 }
                 } finally {
                         if (rs != null) rs.close();
                         if (ps != null) ps.close();
                 }
          }
 
 
          public void listFileRuns(Connection conn, Writer out, String lfn) throws Exception {
                 PreparedStatement ps = null;
                 ResultSet rs =  null;
                 try {
                         ps = DBSSql.listFileRuns(conn, getFileID(conn, lfn, true));
 			pushQuery(ps);
                         rs =  ps.executeQuery();
                         while(rs.next()) {
                                 out.write(((String) "<file_run id='" +  get(rs, "ID") +
                                         "' run_number='" + get(rs, "RUN_NUMBER") +
 					"' number_of_events='" + get(rs, "NUMBER_OF_EVENTS") +
                                         "' number_of_lumi_sections='" + get(rs, "NUMBER_OF_LUMI_SECTIONS") +
                                         "' total_luminosity='" + get(rs, "TOTAL_LUMINOSITY") +
                                         "' store_number='" + get(rs, "STRORE_NUMBER") +
                                         "' start_of_run='" + get(rs, "START_OF_RUN") +
                                         "' end_of_run='" + get(rs, "END_OF_RUN") +
                                         "' creation_date='" + getTime(rs, "CREATION_DATE") +
                                         "' last_modification_date='" + get(rs, "LAST_MODIFICATION_DATE") +
                                         "' created_by='" + get(rs, "CREATED_BY") +
                                         "' last_modified_by='" + get(rs, "LAST_MODIFIED_BY") +
                                         "'/>\n"));
 
                                 }
                 } finally {
                         if (rs != null) rs.close();
                         if (ps != null) ps.close();
                 }
          }
 
 
          public void listADSFileRuns(Connection conn, Writer out, String aDSID, String lfn) throws Exception {
                 PreparedStatement ps = null;
                 ResultSet rs =  null;
                 try {
                         ps = DBSSql.listADSFileRuns(conn, aDSID, getFileID(conn, lfn, true));
                         pushQuery(ps);
                         rs =  ps.executeQuery();
                         while(rs.next()) {
                                 out.write(((String) "<file_run id='" +  get(rs, "ID") +
                                         "' run_number='" + get(rs, "RUN_NUMBER") +
                                         "' number_of_events='" + get(rs, "NUMBER_OF_EVENTS") +
                                         "' number_of_lumi_sections='" + get(rs, "NUMBER_OF_LUMI_SECTIONS") +
                                         "' total_luminosity='" + get(rs, "TOTAL_LUMINOSITY") +
                                         "' store_number='" + get(rs, "STRORE_NUMBER") +
                                         "' start_of_run='" + get(rs, "START_OF_RUN") +
                                         "' end_of_run='" + get(rs, "END_OF_RUN") +
                                         "' creation_date='" + getTime(rs, "CREATION_DATE") +
                                         "' last_modification_date='" + get(rs, "LAST_MODIFICATION_DATE") +
                                         "' created_by='" + get(rs, "CREATED_BY") +
                                         "' last_modified_by='" + get(rs, "LAST_MODIFIED_BY") +
                                         "'/>\n"));
 
                                 }
                 } finally {
                         if (rs != null) rs.close();
                         if (ps != null) ps.close();
                 }
          }
 
 
 
 	 public void listLFNs(Connection conn, Writer out, String path, String patternMetaData) throws Exception {
  		 PreparedStatement ps = null;
  		 ResultSet rs =  null;
  		 boolean first = true; 
  		 String prevLFN = "";
  		 try {
  			 ps = DBSSql.listLFNs(conn, (new DBSApiProcDSLogic(this.data)).getProcessedDSID(conn, path, true) ,  getPattern(patternMetaData, "pattern_meta_data"));
 			pushQuery(ps);
  			 rs =  ps.executeQuery();
  			 while(rs.next()) {
  				 String lfn = get(rs, "LFN");
  				 if( !prevLFN.equals(lfn) && ! first) out.write(((String) "</file_lfn>\n"));
 	 			 if( !prevLFN.equals(lfn) || first) {
 					 out.write(((String) "<file_lfn lfn='" + get(rs, "LFN") +
 								 "' creation_date='" + getTime(rs, "CREATION_DATE") +
 								 "' last_modification_date='" + get(rs, "LAST_MODIFICATION_DATE") +
 								 "' created_by='" + get(rs, "CREATED_BY") +
 								 "' last_modified_by='" + get(rs, "LAST_MODIFIED_BY") +
 			 					 "'>\n"));
 		 			 first = false;
  					 prevLFN = lfn;
 				 }
 				 out.write(((String) "<file_trigger_tag trigger_tag='" + get(rs, "TRIGGER_TAG") +
  							 "' number_of_events='" + get(rs, "NUMBER_OF_EVENTS") +
  							 "'/>\n"));
 
  			 }
 	 		 if (!first) out.write(((String) "</file_lfn>\n"));
  		 } finally {
  			 if (rs != null) rs.close();
  			 if (ps != null) ps.close();
  		 }
 	 }
 
         //Checks if files have same tiers as provided in tierVecToCheckWithFile
 	//private boolean matchWithFileTiers(Vector files, Vector tierVecToCheckWithFile, String path)  throws Exception {
 	private boolean matchWithFileTiers(ArrayList files, Vector tierVecToCheckWithFile, String path)  throws Exception {
 
                 //File Tiers must be within ProcDS Tiers
                 for (int i = 0; i != files.size() ; ++i) {
                         ArrayList tierVector = DBSUtil.getArrayList((Hashtable)files.get(i) ,"file_data_tier");
                         //Vector tierVector = DBSUtil.getVector((Hashtable)files.get(i) ,"file_data_tier");
                         if(tierVector.size() != tierVecToCheckWithFile.size())
                                 throw new DBSException("Tier Mismatch", "1039", "Number of Tiers in file " + tierVector.size() +
                                                 " is not equal to the number of tiers " + tierVecToCheckWithFile.size() +
                                                 " in the dataset " + path);
                         for (int j = 0; j != tierVector.size() ; ++j) {
                                 String tierName = ((String) get((Hashtable)tierVector.get(j), "name")).toUpperCase();
                                 if(!tierVecToCheckWithFile.contains(tierName))
                                         throw new DBSException("Tier Mismatch", "1042",
                                                         "Provided tier " + tierName +
                                                         " is not present in " + path + " it contains " +
                                                         tierVecToCheckWithFile.toString());
                         }
                 }
 
 		return true;
 	}
 
 
 
 	//Method that adds BranchInfo (BranchHash, contents and corresponding Branches in DBS)
 
         public void insertBranchInfo(Connection conn, Writer out, Hashtable branchInfo, Hashtable dbsUser) throws Exception {
 		
 		//Lets insert BrtanchHash if not already there
 		String branchHash = DBSUtil.get(branchInfo, "branch_hash");
 		//String branchHashID = getID(conn, "BranchHash", "Hash", branchHash, false);
 
                 String lmbUserID = personApi.getUserID(conn, dbsUser);
                 String cbUserID = personApi.getUserID(conn, get(branchInfo, "created_by"), dbsUser );
                 String creationDate = getTime(branchInfo, "creation_date", false);
 
                 //if( branchHashID == null ) {
                 PreparedStatement ps = null;
                 try {
                         String contentBase64 = get(branchInfo, "content");
 
                         if(!isNull(contentBase64)) contentBase64 = new String(Base64.decode(contentBase64));
 
                         ps = DBSSql.insertBranchHash(conn,
                                                 branchHash,
 						contentBase64,
                                                 get(branchInfo, "description"),
                                                 cbUserID, lmbUserID, creationDate);
 			pushQuery(ps);
                         ps.execute();
 		} catch (SQLException ex) {
 			String exmsg = ex.getMessage();
 			if(!exmsg.startsWith("Duplicate entry") && !exmsg.startsWith("ORA-00001: unique constraint") ) throw ex;
 			else writeWarning(out, "Already Exists", "1020", "Branch Hash " + branchHash +  " Already Exists");
 
                 } finally {
                         if (ps != null) ps.close();
                 }
 		String branchHashID = getID(conn, "BranchHash", "Hash", branchHash, true);
 
 		//Lest insert the BranchHashMap entries
 		//get the list of branches
 		Vector branchVector = DBSUtil.getVector(branchInfo,"branch_names");
 		String branchID =null;
 		//Insert Branch and then its entry into BranchHashMap
 		for (int j = 0; j < branchVector.size(); ++j) {
 			//insert Branch, if not already there
 			String branchName = get((Hashtable)branchVector.get(j), "name");
 
 
 			//Assuming in most cases the Branches will already be in DBS
 			branchID = getID(conn, "Branch", "Name", branchName, false);
 			if (isNull(branchID)) {
 				insertNameNoExistCheck(conn, out, "Branch", "Name", branchName, cbUserID, lmbUserID, creationDate);
 				branchID = getID(conn, "Branch", "Name", branchName, false);
 			}
 
 
 			//insert File-Branch Map now.
 			insertMap(conn, out,  "BranchHashMap", "BranchID", "BranchHashID",
 					branchID,
 					branchHashID,
 					cbUserID, lmbUserID, creationDate);
 		}
 	}
 
 
 	//Simple API that takes a BlockName as input and checks to see if:
 	// 1-Block exists
 	// 2-Block's Path tier-list matches File-Tier list
 	// 3-Block is open and has sufficient "space" to hold this file
 	//Then it insert files with all information
 	//Makes is pretty speedy operation
 	//ANZAR: 12/03/2007
         public void insertFilesInBlock(Connection conn, Writer out, Hashtable block, ArrayList files, Hashtable dbsUser, boolean ignoreParent, boolean calledFromTranfer) throws Exception {
 
 
 		//Verify Block Name
                 String blockName = getBlockPattern(DBSUtil.get(block, "block_name"));
                 if (isNull(blockName) )
                         throw new DBSException("Wrong Parameters", "1038",
                                                         "User must provide a valid blockName");
 
 		//Tier List Check, matchWithFileTiers throws exception in case of error
 		String blockPath = blockName.split("#")[0];
                 String[] blockPathTokens = blockPath.split("/");
 		if (blockPathTokens.length < 4)
 			throw new DBSException("Wrong Parameters", "1038",
                                                         "User must provide a valid blockName");
                 //matchWithFileTiers(files, parseTierVec(blockPathTokens[3]), blockName);
 
                 String blockID = null;
                 String procDSID = null;
 		long number_of_files=0;
 		long size = 0;
 		long number_of_events;
 		int open_for_writing;
 		String path;
 		
 		//Lets get inf. about Block
 		PreparedStatement ps = null;
 		ResultSet rs =  null;
 		try {
                         ps =  DBSSql.listBlockInfo(conn, getBlockPattern(blockName));
 			pushQuery(ps);
                         rs =  ps.executeQuery();
                         if (rs.next()) {
                                 blockID = get(rs, "ID");
                                 path= get(rs, "PATH");
 				procDSID = get(rs, "DATASET");
                                 size= Long.parseLong((String)get(rs, "BLOCKSIZE"));
                                 number_of_files= Long.parseLong((String)get(rs, "NUMBER_OF_FILES"));
                                 number_of_events= Long.parseLong((String)get(rs, "NUMBER_OF_EVENTS"));
                                 open_for_writing= Integer.parseInt((String)get(rs, "OPEN_FOR_WRITING"));
 			}
 			else {
 				throw new DBSException("Do Not Exist", "1068",
                                                         "Provided BlockName do not exist in DBS, please verify");
                         }
 
                 } catch(Exception e) {
 			throw new DBSException("Database Exception", "1069", e.getMessage());
                 } finally {
 			if (rs != null) rs.close();
                         if (ps != null) ps.close();
 		}
 		//AA conn.commit();
 
 		//We have ALL infor about the Block, lets decide if its OK to write to this Block
 
 		//Is it OK to write to this Block ?
 		if ( open_for_writing == 0 ) {
 			throw new DBSException("Block Closed", "1024", "Block "+ blockName +" not open for further files");
 		}
 
 
 		//Block can hold more files  ??
 		//Leaving the code commenetd as this may not be handled by DBS !!
 		//DBSConfig config = DBSConfig.getInstance();
 		//long configuredBlkSize = config.getMaxBlockSize();
 		//long configuredNumFiles = config.getMaxBlockFiles();
 		//if (size >= configuredBlkSize || number_of_files >= configuredNumFiles ) 
 		//	throw new DBSException("Block is FULL");
 
                 DBSApiProcDSLogic procDSApiObj = new DBSApiProcDSLogic(this.data);
 
 		//Is it OK to write to this Dataset ??
 		procDSApiObj.checkProcDSStatus(conn, out, path, procDSID);
 		//conn.commit();
 
                 //File insert will happen now     **************************
 
                 //Get the User ID from USERDN
                 String lmbUserID = personApi.getUserID(conn, dbsUser);
 		conn.commit();
 
                 //These tables are used to store the types and staus fileds along with thier id
                 //If the id can be fetched from here then we do not have to fietch it from database again and again
                 boolean newFileInserted = false;
                 Hashtable statusTable = new Hashtable();
                 Hashtable typeTable = new Hashtable();
                 Hashtable valStatusTable = new Hashtable();
                 String statusID = "";
                 String typeID = "";
                 String valStatusID = "";
                                 
                 String thisBranchHash = null;
                 String lastBranchHash = null;
                 String branchID = null;
 		//Vector that just hold some cache values for insertMap function.
 		ArrayList valueVec = new ArrayList();
 		//Vector valueVec = new Vector();
 		//Simple vector that keeps track of what runs have already been added to this dataset
 		//Vector runsMapped = new Vector();
 		ArrayList runsMapped = new ArrayList();
 		String cbUserID = null;
 		String creationDate = null;
 		java.util.ArrayList  blockIDList = new java.util.ArrayList();
 		
 		Vector runstoUpdate = new Vector();
 
 
                 for (int i = 0; i < files.size() ; ++i) {
                         Hashtable file = (Hashtable)files.get(i);
 
                         String fileID = "";
                         String lfn = get(file, "lfn", true);
                         String fileStatus = get(file, "file_status", false).toUpperCase();
 
                         String type = get(file, "type", true).toUpperCase();
 
                         String valStatus = get(file, "validation_status", false).toUpperCase();
                         cbUserID = personApi.getUserID(conn, get(file, "created_by"), dbsUser );
 			//AA conn.commit();
                         creationDate = getTime(file, "creation_date", false);
                         ArrayList lumiVector = DBSUtil.getArrayList(file,"file_lumi_section");
                         ArrayList tierVector = DBSUtil.getArrayList(file,"file_data_tier");
                         ArrayList parentVector = DBSUtil.getArrayList(file,"file_parent");
                         ArrayList childVector = DBSUtil.getArrayList(file,"file_child");
                         ArrayList algoVector = DBSUtil.getArrayList(file,"file_algorithm");
                         ArrayList trigTagVector = DBSUtil.getArrayList(file,"file_trigger_tag");
 
                         //Set defaults Values
                         if (isNull(fileStatus)) fileStatus = "VALID";
                         //if (isNull(type)) type = "EVD";
                         if (isNull(valStatus)) valStatus = "VALID";
 
                                 //TODO Exception of null status or type should be catched and parsed and 
                                 //a proper message should be returned back to the user. Different Database can have different error message YUK
                                 //Status should be defaulted to something in the database itself. A wrong status may insert a dafult value.
                                 //User will never know about this YUK
                                 if( isNull(statusID = get(statusTable, fileStatus)) ) {
                                         statusID = getFileStatusID(conn,  fileStatus, true);
                                         statusTable.put(fileStatus, statusID);
                                 }
 				//AAconn.commit();
                                 if( isNull(typeID = get(typeTable, type)) ) {
                                         typeID = getFileTypeID(conn, type, true);
                                         typeTable.put(type, typeID);
                                 }
 				//AAconn.commit();
                                 if( isNull(valStatusID = get(valStatusTable, valStatus)) ) {
                                         valStatusID = getFileValStatusID(conn, valStatus, true);
                                         valStatusTable.put(valStatus, valStatusID);
                                 }
 				//AAconn.commit();
 
                                 thisBranchHash = get(file, "branch_hash", false);
                                 if (! thisBranchHash.equals(lastBranchHash) ) {
                                         branchID = getID(conn, "BranchHash", "Hash", thisBranchHash, false);
                                         lastBranchHash = thisBranchHash;
                                 }
 				//AAconn.commit();
 				//Try to insert a Fille if its already there (Break out!), For all other reasons throw exception.
 				try {
                                 	ps = DBSSql.insertFile(conn,
                                             procDSID,
                                             blockID,
                                             lfn,
                                             get(file, "checksum", false),
                                             get(file, "adler32", false),
                                             get(file, "md5", false),
                                             get(file, "number_of_events", false),
                                             get(file, "size", false),
                                             statusID,
                                             typeID,
                                             valStatusID,
                                             get(file, "queryable_meta_data", false),
                                             branchID,
 					    get(file, "auto_cross_section", false),
                                             cbUserID, lmbUserID, creationDate);
 
 					pushQuery(ps);
                                 	ps.execute();
 					newFileInserted = true;
 				} catch (SQLException ex) {
 					String exmsg = ex.getMessage();
 					if(!exmsg.startsWith("Duplicate entry") && !exmsg.startsWith("ORA-00001: unique constraint") ) throw ex;
 					else writeWarning(out, "Already Exists", "1020", "Logical File Name  " + lfn + " Already Exists");
 
 				}finally {
                                 	if (ps != null) ps.close();
 				}
 				//AAconn.commit();
                                 //if(isNull(fileID)) fileID = getFileID(conn, lfn);
                                 //Fetch the File ID that was just inseted to be used for subsequent insert of other tables only if it is needed.
                                 //FileID is needed if any of the other table information is provided i.e the vector size is > 0
                                 //if(algoVector.size() > 0 || tierVector.size() > 0 || parentVector.size() > 0 || lumiVector.size() > 0 || childVector.size() > 0 )
 				if(isNull(fileID)) fileID = getFileID(conn, lfn, true);
 				//conn.commit();
                                         //fileID = getFileID(conn, lfn, true);
 
                                 //Insert FileAlgo table by fetching application ID. 
                                 //Use get with 2 params so that it does not do type checking, since it will be done in getID call.
 				valueVec.clear();
 				for (int j = 0; j < algoVector.size(); ++j) {
                                         Hashtable hashTable = (Hashtable)algoVector.get(j);
                                         String psHash = get(hashTable, "ps_hash", false);
                                         if (isNull (psHash) ) psHash =  "NO_PSET_HASH";
 					valueVec.add((new DBSApiAlgoLogic(this.data)).getAlgorithmID(conn, get(hashTable, "app_version"),
                                                                         get(hashTable, "app_family_name"),
                                                                         get(hashTable, "app_executable_name"),
                                                                         psHash,
                                                                         true));
 				}
 				insertMapBatch(conn, out, "FileAlgo", "Fileid", "Algorithm", fileID, valueVec, cbUserID, lmbUserID, creationDate);
 				//AAconn.commit();
                                 //Insert FileTier table by fetching data tier ID
 				valueVec.clear();
 				
                                 //Insert FileParentage table by fetching parent File ID
                                 if(!ignoreParent) {
 					valueVec.clear();
 					for (int j = 0; j < parentVector.size(); ++j) 
 						valueVec.add(getFileID(conn, get((Hashtable)parentVector.get(j), "lfn"), true));
 					insertMapBatch(conn, out, "FileParentage", "ThisFile", "ItsParent", fileID, valueVec, cbUserID, lmbUserID, creationDate);
 				}
 				//AAconn.commit();
 				
                                 //Insert FileParentage for all the child of give by the client. Used during Merge operation
 				valueVec.clear();
 				for (int j = 0; j < childVector.size(); ++j)
 					valueVec.add(getFileID(conn, get((Hashtable)childVector.get(j), "lfn"), true));
 				insertMapBatch(conn, out, "FileParentage", "ItsParent", "ThisFile", fileID, valueVec, cbUserID, lmbUserID, creationDate);
 				//AAconn.commit();
 
 //----------------------------Lumi/Run mapping 				
                                 //Insert FileLumi table by first inserting and then fetching Lumi Section ID
 				valueVec.clear();
 				String runID = null;
 
 				ArrayList lumiOnlyValueVec = new ArrayList();
 				
                 		java.util.ArrayList keys = new java.util.ArrayList();
 				//AA- NEVER change this ORDER
 		                keys.add("Fileid");
                 		keys.add("Lumi");
 		                keys.add("Run");
                 		keys.add("CreationDate");
 		                keys.add("CreatedBy");
 		                keys.add("LastModifiedBy");
 				Vector tmpRunVector = new Vector();
                                 for (int j = 0; j < lumiVector.size(); ++j) {
                                         Hashtable hashTable = (Hashtable)lumiVector.get(j);
                                         String thisRunN = get(hashTable, "run_number", true);
                                         runID = getID(conn, "Runs", "RunNumber",  thisRunN, true);
 					//conn.commit();
                                         String lsNumber = get(hashTable, "lumi_section_number", false);
 
                                         if ( !isNull(lsNumber) ) {
 						insertLumiSection(conn, out, hashTable, cbUserID, lmbUserID, creationDate);
 						conn.commit();//Release lock on Run Table
 						if (!runstoUpdate.contains(runID)) runstoUpdate.add(runID);
 						String lumiID = getMapID(conn, "LumiSection", "LumiSectionNumber", "RunNumber", lsNumber, runID, true);
 						valueVec.add(fileID);
 						valueVec.add(lumiID);
 						valueVec.add(runID);
 						valueVec.add(creationDate);
 		                                valueVec.add(cbUserID);
                 		                valueVec.add(lmbUserID);
 
						if (!tmpRunVector.contains(runID)) tmpRunVector.add(runID);
 
 					} else {
 						//No Lumi
 						lumiOnlyValueVec.add(fileID);
 						lumiOnlyValueVec.add(runID);
 						lumiOnlyValueVec.add(creationDate);
 		                                lumiOnlyValueVec.add(cbUserID);
                 		                lumiOnlyValueVec.add(lmbUserID);
 
						if (!tmpRunVector.contains(runID)) tmpRunVector.add(runID);
 					}
 					//AA conn.commit();
 					// Insert ProcDS-Run Map, if its already not there
 					insertMap(conn, out, "ProcDSRuns", "Dataset", "Run",
 							procDSID, runID,
 							cbUserID, lmbUserID, creationDate, true);
 					//AA conn.commit();//Release lock on Run Table
 
 
 				}
 				conn.commit();
 				if(tmpRunVector.size() > 0) lockRunRows(conn, out, tmpRunVector);
 				insertGeneralBatch(conn, out, "FileRunLumi", keys, valueVec);
 				//conn.commit();
 				insertGeneralBatch(conn, out, "FileRunLumi", keys, lumiOnlyValueVec);
 				conn.commit();
 
 
 
 //----------------------------Lumi/Run mapping done				
 
                                 //Insert Trigger tags (if present)
                                 for (int j = 0; j < trigTagVector.size(); ++j) {
                                         Hashtable hashTable = (Hashtable)trigTagVector.get(j);
                                         //insert File-Trigger-Tag Map now.
                                         insertMap(conn, out,  "FileTriggerTag", "Fileid", "TriggerTag", "NumberOfEvents",
                                                         fileID,
                                                         get(hashTable, "trigger_tag"),
                                                         get(hashTable, "number_of_events"),
                                                         cbUserID, lmbUserID, creationDate, true);
                                 }
 				//AA conn.commit();
 
                                 //Insert the file association   
                                 String fileAssoc = get(file, "file_assoc", false);
                                 if (! isNull(fileAssoc)) {
                                         insertMap(conn, out, "FileAssoc", "ThisFile", "ItsAssoc",
                                                                 fileID,
                                                                 getID(conn, "Files", "LogicalFileName", fileAssoc, true),
                                                                 cbUserID, lmbUserID, creationDate, true);
                                 }
 				//AA conn.commit();
 
 				//Insert Block Parentage
 				java.util.ArrayList  blockIDListTMP = getFileBlockParentage(conn, out, fileID);
 				for (Object s: blockIDListTMP) 
 						if (!blockIDList.contains(s)) blockIDList.add(s);
                         //if ( i%10 == 0) 
 				conn.commit(); //For Every 10 files commit the changes
                 }//For loop
 
 
                 //Update Block numberOfFiles and Size
                 if (newFileInserted) {
                 	DBSApiBlockLogic blockApiObj = new DBSApiBlockLogic(this.data);
                         blockApiObj.updateBlock(conn, out, blockID, lmbUserID);
 			//AA conn.commit();
 			insertBlockParentage(conn, out, blockID, blockIDList, cbUserID, lmbUserID, creationDate);
 			//AA conn.commit();
 			//FIXME/////////////////////updateRunsSpecial(out, runstoUpdate);
 			if (!calledFromTranfer) updateRunsSpecial(out, runstoUpdate);
 		}
 		conn.commit();
 		
 
 	}
 
 
 	public void updateRunsSpecial(Writer out, Vector runstoUpdate) throws Exception  {
 		Connection conn1 = null;
 
 		boolean comeagain=true;
 		for (int tt=0; tt !=10;++tt) {
 			if (comeagain) {
 				try {
 					conn1 = DBManagement.getDBConnManInstance().getConnection();
 					conn1.setAutoCommit(false);
 					//Lock the associated run tables rows for deadlock avoidance
 					if (runstoUpdate.size() > 0) {
 						//conn1.setAutoCommit(false);
 						//lockRunRows(conn1, out, runstoUpdate);
 						//Fix the the number of lumi sections in the RUN
 						for(Object aRun: runstoUpdate) updateRunLumiCount(conn1, out, (String)aRun);
 						
 						comeagain = false;
 						
 					}
 				} catch (SQLException ex) {
 					String exmsg = ex.getMessage();
 					if ( exmsg.startsWith("Deadlock found")) {
 						//may introduce some delay here ??
 						System.out.println("RETRYING FAILED Deadlocked transaction...");
 						comeagain = true;
 					}
 					//already tried 09 times, what are my chances for the 10th time 
 					else if (tt==9) throw ex;
 				} finally {
 					if (conn1 != null) { conn1.commit(); conn1.close(); }	 
 					
 				}
 			}
 		}
 	}
 
 
 /*private Connection getConnection() throws Exception {
 
 DBSConfig config = DBSConfig.getInstance();
 return DBManagement.getConnection( config.getDbDriver(),
                                         config.getDbURL(),
                                         config.getDbUserName(),
                                         config.getDbUserPasswd());
 }*/
 
         java.util.ArrayList getFileBlockParentage(Connection conn, Writer out, String fileID) throws Exception {
 
                 PreparedStatement ps = null;
                 ResultSet rs =  null;
 		java.util.ArrayList  blockIDList = new java.util.ArrayList();
                 try {
                         ps = DBSSql.listFileParentBlocks(conn, fileID);
                         pushQuery(ps);
                         rs =  ps.executeQuery();
                         while(rs.next()) {
                                 String blkID = get(rs, "ID");
 				blockIDList.add(blkID);
 			}
 		} finally {
 			if (ps != null) ps.close();
 			if (rs != null) rs.close();
 		}
 
 		return blockIDList;
 	}
 
 
         void insertBlockParentage(Connection conn, Writer out, String blockID, ArrayList  blockIDList,
                                         String cbUserID, String lmbUserID, String creationDate) throws Exception {
 		/*try {
 
 
 			insertMapBatch(conn, out, "BlockParent", "ThisBlock", "ItsParent",
                                         blockID, blockIDList, cbUserID, lmbUserID, creationDate);
 
                         } catch (SQLException ex) {
                                 String exmsg = ex.getMessage();
                                 if ( exmsg.startsWith("Duplicate entry") ||
                                         exmsg.startsWith("ORA-00001: unique constraint") ) {
                                         //do nothing, just continue
                                 }
                                 else
                                         throw new SQLException("'"+ex.getMessage()+"' insertBlockParentage failed for unknown reasons");
                         }*/
 
                 for (Object s: blockIDList) {
                 	try {
                        	       insertMap(conn, out, "BlockParent", "ThisBlock", "ItsParent", 
                                         blockID, (String) s, cbUserID, lmbUserID, creationDate, true);
                 	} catch (SQLException ex) {
                         	String exmsg = ex.getMessage();
                         	if ( exmsg.startsWith("Duplicate entry") ||
                                 	exmsg.startsWith("ORA-00001: unique constraint") ) {
 					//do nothing, just continue
                          	}
                          	else
                                 	throw new SQLException("'"+ex.getMessage()+"' insertBlockParentage failed for unknown reasons");
                 	}
 		}
 
         }
 
        /**
 	 * Insert a list of Files whose parameters are provided in the passed files <code>java.util.Vector</code>. This vector contains a list of hashtable and is generated externally and filled in with the file parameters by parsing the xml input provided by the client. This method inserts entries into more than one table associated with File table. The the main query that it executes to insert in File table, get generated by <code>dbs.DBSSql.insertFile</code> method.<br> 
 	 * First it fetches the userID by using the parameters specified in the dbsUser <code>java.util.Hashtable</code> and if the user does not exists then it insert the new user in the Person table. All this user operation is done by a private method getUserID. <br>
 	 * Then it fetches the processed dataset id and the block id by calling a private getProcessedDSID method and private getBlockID method with parameters path and blockName. The log of insert file bahaves in a way such that all the files to be inserted per this method call, should belong to the same processed dataset and same block <br>
 	 * Then it iterates through all the files Hashtable provided in the files vector and inserts one file at a time. <br>
 	 * Then in the same loop it fetches all the algorithm list of the file that just got inserted and inserts a new row in FileAlgoMap table by calling a generic private insertMap method. <br>
 	 * Then in the same loop it fetches all the tier list of the file that just got inserted and inserts a new row in FileTier table by calling a generic private insertMap method. <br>
 	 * Then in the same loop it fetches all the parent list of the file that just got inserted and inserts a new row in FileParentage table by calling a generic private insertMap method. <br>
 	 * Then in the same loop it fetches all the lumi section list of the file that just got inserted and inserts a new row in FileLumi table by calling a generic private insertMap method. Before it insert in to this FileLumi table , it first insert the LumiSection by calling insertLumiSection method<br>
 	 * Finally it updates the block information with correct number of files and size in bytes by calling the updateBlock method.
 	 * @param conn a database connection <code>java.sql.Connection</code> object created externally.
 	 * @param out an output stream <code>java.io.Writer</code> object where this method writes the results into.
 	 * @param files a <code>java.util.Vector</code> that contains a list of <code>java.util.Hastable</code>  that contain all the necessary key value pairs required for inserting a new file. The keys along with its values that it may or may not contain are <br>
 	 * <code>lfn, checksum, number_of_events, size, queryable_meta_data, file_status, type, validation_status, lumi_section, data_tier, parent, algorithm, created_by, creation_date </code> <br>
 	 * Further the keys <code>lumi_section, data_tier, parent, algorithm </code> are itself vector of Hashtable. <br>
 	 * The key that <code>parent </code> hashtable may or may not contain is <code>lfn</code> <br>
 	 * The key that <code>data_tier </code> hashtable may or may not contain is <code>name</code> <br>
 	 * The keys that <code>lumi_section </code> hashtable may or may not contain are <br>
 	 * <code>lumi_section_number, run_number, start_event_number, end_event_number, lumi_start_time, lumi_end_time</code> <br>
 	 * The keys that <code>algorithm </code> hashtable may or may not contain are <br> 
 	 * <code>app_version, app_family_name, app_executable_name, ps_name</code> <br>
 	 * @param path a dataset path in the format of /primary/processed/tier. This path is used to find the existing processed dataset id.
 	 * @param block a block hashtable in the format of /primary/processed/tier#GUID. This block name is used to find the existing block id.
 	 * @param dbsUser a <code>java.util.Hashtable</code> that contains all the necessary key value pairs for a single user. The most import key in this table is the user_dn. This hashtable is used to insert the bookkeeping information with each row in the database. This is to know which user did the insert at the first place.
 	 * @throws Exception Various types of exceptions can be thrown. Commonly they are thrown if the supplied parameters in the hashtable are invalid, the database connection is unavailable or a duplicate entry is being added.
 	 */
 	public void insertFiles(Connection conn, Writer out, String path, String primary, 
 							String proc, Hashtable block, ArrayList files, Hashtable dbsUser) throws Exception { 
 		insertFiles(conn, out, path, primary, proc, block, files, dbsUser, false, false);
 	}
 
         public void insertFiles(Connection conn, Writer out, String path, String primary, 
 					String proc, Hashtable block, ArrayList files, 
 						Hashtable dbsUser, boolean ignoreParent, boolean calledFromTranfer) throws Exception { 
 
 		//The presedence order is Block, then Path, then (Primary, Process Dataset)
 		//InsertFile in Block iks the only used method, so removing all other ways to insert a file - AA 01/15/2009
                 String blockName = DBSUtil.get(block, "block_name");
 
 
 		//If Block is provided, cut the crap and jump to simpler implementation.
 		if (!isNull(blockName)) {
 			 insertFilesInBlock(conn, out, block, files, dbsUser, ignoreParent, calledFromTranfer);
 		}
 
 		return;
 	}
 
 
 
 	private String getFileBlockID(Connection conn, Writer out, String lfn) throws Exception {
 		String blockID = null;
                 PreparedStatement ps = null;
                 ResultSet rs =  null;
                 try {
                         ps = DBSSql.listFiles(conn, lfn);
                         pushQuery(ps);
                         rs =  ps.executeQuery();
                         if(rs.next()) blockID = get(rs, "BLOCK_ID");
                         else throw new DBSException("Unavailable data", "1011", "No such Files : LogicalFileName : " + lfn );
                 } finally {
                         if (rs != null) rs.close();
                         if (ps != null) ps.close();
                 }
 		return blockID;
 	}
 
 	
 	/**
 	 * Insert a parent in a file. 
 	 * First it fetches the userID by using the parameters specified in the dbsUser <code>java.util.Hashtable</code> and if the user does not exists then it insert the new user in the Person table. All this user operation is done by a private method getUserID. <br>
 	 * Then it inserts entry into just one table FileParentage by calling a generic private <code>insertMap</code> method. It first fetches the file id by calling a generic private getID method.
 	 * @param conn a database connection <code>java.sql.Connection</code> object created externally.
 	 * @param out an output stream <code>java.io.Writer</code> object where this method writes the results into.
 	 * @param table a <code>java.util.Hashtable</code> that contains all the necessary key value pairs for a single file. The keys along with its values that it may or may not contain are <br>
 	 * <code>lfn, created_by, creation_date </code> <br>
 	 * If this lfn is not provided or the file id could not be found then an exception is thrown.
 	 * @param parentLFN a logical file name of the parent file.
 	 * @param dbsUser a <code>java.util.Hashtable</code> that contains all the necessary key value pairs for a single user. The most import key in this table is the user_dn. This hashtable is used to insert the bookkeeping information with each row in the database. This is to know which user did the insert at the first place.
 	 * @throws Exception Various types of exceptions can be thrown. Commonly they are thrown if the supplied parameters in the hashtable are invalid, the database connection is unavailable or the file is not found.
 	 */
 	public void insertParentInFile(Connection conn, Writer out, Hashtable table, String parentLFN, Hashtable dbsUser) throws Exception {
 
 		String lfn = get(table, "lfn", true);
 		String fileID = getFileID(conn, lfn, true);
 		String parentID = getFileID(conn, parentLFN, true);
 		String cbUserID = personApi.getUserID(conn, get(table, "created_by"), dbsUser );
 		String lmbUserID = personApi.getUserID(conn, dbsUser);
 		String creationDate = getTime(table, "creation_date", false);
 
 		insertMap(conn, out, "FileParentage", "ThisFile", "itsParent", 
 				fileID,
 				parentID,
 				cbUserID, lmbUserID, creationDate);
 
 		//Get the block ID of the input File
 		String blockID = getFileBlockID(conn, out, lfn);
 		//Insert Block Parentage
 		java.util.ArrayList  blockIDList = getFileBlockParentage(conn, out, fileID);
 		insertBlockParentage(conn, out, blockID, blockIDList, cbUserID, lmbUserID, creationDate);
 
 
 		//System.out.println("\n JUST added :: " + blockID + "List : " + blockIDList );
 		conn.commit();
 
 
 	}
 	public void deleteFileParent(Connection conn, Writer out, String lfn, String parentLFN, Hashtable dbsUser) throws Exception {
 		deleteMap(conn, out, "FileParentage", "ThisFile", "itsParent",
 				getFileID(conn, lfn, true),
 				getFileID(conn, parentLFN, true));
 	}
 	/**
 	 * Insert a algorithm in a file. 
 	 * First it fetches the userID by using the parameters specified in the dbsUser <code>java.util.Hashtable</code> and if the user does not exists then it insert the new user in the Person table. All this user operation is done by a private method getUserID. <br>
 	 * Then it inserts entry into just one table FileAlgoMap by calling a generic private <code>insertMap</code> method. It first fetches the file id by calling a generic private getID method.
 	 * @param conn a database connection <code>java.sql.Connection</code> object created externally.
 	 * @param out an output stream <code>java.io.Writer</code> object where this method writes the results into.
 	 * @param table a <code>java.util.Hashtable</code> that contains all the necessary key value pairs for a single file. The keys along with its values that it may or may not contain are <br>
 	 * <code>lfn, created_by, creation_date </code> <br>
 	 * If this lfn is not provided or the file id could not be found then an exception is thrown.
 	 * @param algo a <code>java.util.Hashtable</code> that conatin the parameter that defines an algorithm. The keys that <code>algo </code> hashtable may or may not contain are <br> 
 	 * <code>app_version, app_family_name, app_executable_name, ps_name</code> <br>
 	 * @param dbsUser a <code>java.util.Hashtable</code> that contains all the necessary key value pairs for a single user. The most import key in this table is the user_dn. This hashtable is used to insert the bookkeeping information with each row in the database. This is to know which user did the insert at the first place.
 	 * @throws Exception Various types of exceptions can be thrown. Commonly they are thrown if the supplied parameters in the hashtable are invalid, the database connection is unavailable or the file is not found.
 	 */
 	public void insertAlgoInFile(Connection conn, Writer out, Hashtable table, Hashtable algo, Hashtable dbsUser) throws Exception {
 		//String psHash = get(algo, "ps_hash");
 		//if (isNull (psHash) ) {
 		//	psHash =  "NO_PSET_HASH";
 		//}
 		insertMap(conn, out, "FileAlgo", "Fileid", "Algorithm", 
 				getFileID(conn, get(table, "lfn", true), true), 
 				(new DBSApiAlgoLogic(this.data)).getAlgorithmID(conn, get(algo, "app_version"), 
 						get(algo, "app_family_name"), 
 						get(algo, "app_executable_name"),
 						get(algo, "ps_hash"), 
 						//psHash,
 						true), 
 				personApi.getUserID(conn, get(table, "created_by"), dbsUser ),
 				personApi.getUserID(conn, dbsUser),
 				getTime(table, "creation_date", false));
 	}
 
 	/**
 	 * Insert a lumi section in a file. 
 	 * First it fetches the userID by using the parameters specified in the dbsUser <code>java.util.Hashtable</code> and if the user does not exists then it insert the new user in the Person table. All this user operation is done by a private method getUserID. <br>
 	 * Then it inserts entry into just one table FileLumi by calling a generic private <code>insertMap</code> method. It first fetches the file id by calling a generic private getID method.
 	 * @param conn a database connection <code>java.sql.Connection</code> object created externally.
 	 * @param out an output stream <code>java.io.Writer</code> object where this method writes the results into.
 	 * @param table a <code>java.util.Hashtable</code> that contains all the necessary key value pairs for a single file. The keys along with its values that it may or may not contain are <br>
 	 * <code>lfn, created_by, creation_date </code> <br>
 	 * If this lfn is not provided or the file id could not be found then an exception is thrown.
 	 * @param lsNumber a lumi section number that uniquely identifies a lumi section.
 	 * @param dbsUser a <code>java.util.Hashtable</code> that contains all the necessary key value pairs for a single user. The most import key in this table is the user_dn. This hashtable is used to insert the bookkeeping information with each row in the database. This is to know which user did the insert at the first place.
 	 * @throws Exception Various types of exceptions can be thrown. Commonly they are thrown if the supplied parameters in the hashtable are invalid, the database connection is unavailable or the file is not found.
 	 */
 	public void insertLumiInFile(Connection conn, Writer out, Hashtable table, String lsNumber, Hashtable dbsUser) throws Exception {
 
 
 		//This METHOD is INCORRECT, We ALSO NEED RUn NUMBER
 
 		insertMap(conn, out, "FileLumi", "Fileid", "Lumi", 
 				getFileID(conn, get(table, "lfn", true), true), 
 				getID(conn, "LumiSection", "LumiSectionNumber", lsNumber, true), 
 				personApi.getUserID(conn, get(table, "created_by"), dbsUser ),
 				personApi.getUserID(conn, dbsUser),
 				getTime(table, "creation_date", false));
 	}
 
  	/**
 	 * Remaps a list parents and childern of input Files to one single output File. The list of input file is provided as  <code>java.util.Vector</code> that contains the list of LFN. The output file is provided by just one LFN passed in as a String
 	 * Finally it updates the block information with correct number of files and size in bytes by calling the updateBlock method.
 	 * @param conn a database connection <code>java.sql.Connection</code> object created externally.
 	 * @param out an output stream <code>java.io.Writer</code> object where this method writes the results into.
 	 * @param inFiles a <code>java.util.Vector</code> that contains a list of <code>java.lang.String</code> that represents the logical name of the files.  
 	 * @param outFileTable a <code>java.util.Hashtable</code> that contains a the logical name of the the output file and/or created by , creation date.  
 	 * @param dbsUser a <code>java.util.Hashtable</code> that contains all the necessary key value pairs for a single user. The most import key in this table is the user_dn. This hashtable is used to insert the bookkeeping information with each row in the database. This is to know which user did the insert at the first place.
 	 * @throws Exception Various types of exceptions can be thrown. Commonly they are thrown if the supplied parameters in the hashtable are invalid, the database connection is unavailable or a duplicate entry is being added.
 	 */
         public void remapFiles(Connection conn, Writer out, Vector inFiles, Hashtable outFileTable, Hashtable dbsUser) throws Exception { 
 		String outFileID = getFileID(conn, get(outFileTable, "lfn", true), true);
 		String lmbUserID = personApi.getUserID(conn, dbsUser);
 		String cbUserID = personApi.getUserID(conn, get(outFileTable, "created_by"), dbsUser );
 		String creationDate = getTime(outFileTable, "creation_date", false);
 
 		for (int j = 0; j < inFiles.size(); ++j) {
 			String inFileID = getFileID(conn, (String)inFiles.get(j), true);
 			PreparedStatement ps = null;
 			ResultSet rs =  null;
 			try {
 				//Get all the parents
 				ps = DBSSql.listFileProvenence(conn, inFileID, true, true);
 				pushQuery(ps);
 				rs =  ps.executeQuery();
 				while(rs.next()) {
 					insertMap(conn, out, "FileParentage", "ThisFile", "itsParent", 
 						outFileID, 
 						get(rs, "ID"),
 						cbUserID, lmbUserID, creationDate);
 				}
 			} finally { 
 				if (rs != null) rs.close();
 				if (ps != null) ps.close();
 			}
 			
 			ps = null;
 			rs = null;
 			try {
 				//Get all the childern
 				ps = DBSSql.listFileProvenence(conn, inFileID, false, true);
 				pushQuery(ps);
 				rs =  ps.executeQuery();
 				while(rs.next()) {
 					insertMap(conn, out, "FileParentage", "ThisFile", "itsParent", 
 						get(rs, "ID"),
 						outFileID, 
 						cbUserID, lmbUserID, creationDate);
 				}
 			} finally { 
 				if (rs != null) rs.close();
 				if (ps != null) ps.close();
 			}
 
 			//Set the status of the inFile to merged
 		
 		}
 		
 	}
 
  	 /**
 	 * Updates the type of a file. 
 	 * First it fetches the userID by using the parameters specified in the dbsUser <code>java.util.Hashtable</code> and if the user does not exists then it insert the new user in the Person table. All this user operation is done by a private method getUserID. <br>
 	 * Then it fetches the file ID and call a generic methods updateName that fetches the type id and updates it in Files table.
 	 * @param conn a database connection <code>java.sql.Connection</code> object created externally.
 	 * @param out an output stream <code>java.io.Writer</code> object where this method writes the results into.
 	 * @param lfn the logocal file name.
 	 * @param value a value of the type field to be set in this file.
 	 * @param dbsUser a <code>java.util.Hashtable</code> that contains all the necessary key value pairs for a single user. The most import key in this table is the user_dn. This hashtable is used to insert the bookkeeping information with each row in the database. This is to know which user did the insert at the first place.
 	 * @throws Exception Various types of exceptions can be thrown. Commonly they are thrown if the supplied parameters are invalid, the database connection is unavailable or a procsssed dataset is not found.
 	 */
 	public void updateFileType(Connection conn, Writer out, String lfn, String value, Hashtable dbsUser) throws Exception {
 		updateName(conn, out, "Files",  getFileID(conn, lfn, true),
 				                        "FileType", "FileType", "Type", value, personApi.getUserID(conn, dbsUser));
 	}
 
 	/**
 	 * Updates the status of a file. 
 	 * First it fetches the userID by using the parameters specified in the dbsUser <code>java.util.Hashtable</code> and if the user does not exists then it insert the new user in the Person table. All this user operation is done by a private method getUserID. <br>
 	 * Then it fetches the file ID and call a generic methods updateName that fetches the status id and updates it in Files table.
 	 * @param conn a database connection <code>java.sql.Connection</code> object created externally.
 	 * @param out an output stream <code>java.io.Writer</code> object where this method writes the results into.
 	 * @param lfn the logocal file name.
 	 * @param value a value of the status field to be set in this file.
 	 * @param dbsUser a <code>java.util.Hashtable</code> that contains all the necessary key value pairs for a single user. The most import key in this table is the user_dn. This hashtable is used to insert the bookkeeping information with each row in the database. This is to know which user did the insert at the first place.
 	 * @throws Exception Various types of exceptions can be thrown. Commonly they are thrown if the supplied parameters are invalid, the database connection is unavailable or a procsssed dataset is not found.
 	 */
 	public void updateFileStatus(Connection conn, Writer out, String lfn, String value, String descrp, Hashtable dbsUser) throws Exception {
 		String desc="updateFileStatus";
 		if (!isNull(descrp)) {
 			desc=descrp;
 		}
 		String lmbUserID = personApi.getUserID(conn, dbsUser);
 		updateName(conn, out, "Files",  getFileID(conn, lfn, true),
 				                        "FileStatus", "FileStatus", "Status", value, personApi.getUserID(conn, dbsUser));
 
 		PreparedStatement ps = null;
 		ResultSet rs =  null;
 		try {
 			ps = DBSSql.listFiles(conn, lfn);
 			pushQuery(ps);
 			rs =  ps.executeQuery();
 			if(rs.next()) (new DBSApiBlockLogic(this.data)).updateBlock(conn, out, get(rs, "BLOCK_ID"), lmbUserID);
 			else throw new DBSException("Unavailable data", "1011", "No such Files : LogicalFileName : " + lfn );
 		} finally { 
 			if (rs != null) rs.close();
 			if (ps != null) ps.close();
 		}
 
 		if (this.data.instanceName.equals("GLOBAL")) { 
 			insertTimeLog(conn, "UpdateFileStatus", "UpdateFileStatus Called By User",
                                                   "File: "+lfn+" Status Changed To: "+value,
 						   desc,
                                                    dbsUser);
 		}
 	}
 
 	 /**
 	 * Updates the meta data of a file a file. 
 	 * First it fetches the userID by using the parameters specified in the dbsUser <code>java.util.Hashtable</code> and if the user does not exists then it insert the new user in the Person table. All this user operation is done by a private method getUserID. <br>
 	 * Then it fetches the file ID and call a generic methods updateName that fetches the type id and updates it in Files table.
 	 * @param conn a database connection <code>java.sql.Connection</code> object created externally.
 	 * @param out an output stream <code>java.io.Writer</code> object where this method writes the results into.
 	 * @param lfn the logocal file name.
 	 * @param value a value of the meta data field to be set in this file.
 	 * @param dbsUser a <code>java.util.Hashtable</code> that contains all the necessary key value pairs for a single user. The most import key in this table is the user_dn. This hashtable is used to insert the bookkeeping information with each row in the database. This is to know which user did the insert at the first place.
 	 * @throws Exception Various types of exceptions can be thrown. Commonly they are thrown if the supplied parameters are invalid, the database connection is unavailable or a procsssed dataset is not found.
 	 */
 	public void updateFileMetaData(Connection conn, Writer out, String lfn, String value, Hashtable dbsUser) throws Exception {
 		checkString(value, "queryable_meta_data");
 		updateValue(conn, out, "Files",  getFileID(conn, lfn, true), "QueryableMetadata", value, personApi.getUserID(conn, dbsUser));
 	}
 
 
 
         public void updateFileAutoCrossSection(Connection conn, Writer out, String lfn, String xSection, Hashtable dbsUser) throws Exception {
                 PreparedStatement ps = null;
                 try {
 
                 String fileID = getFileID(conn, lfn, true);
 
                         ps =  DBSSql.updateFileAutoCrossSection(conn, fileID, xSection, personApi.getUserID(conn, dbsUser));
                         pushQuery(ps);
                         ps.execute();
                 } finally {
                         if (ps != null) ps.close();
                 }
         }
 
 
 
 
 	private String getFileID(Connection conn, String lfn, boolean excep) throws Exception {
 		String id = "";
 		if(!isNull( id = get(this.data.localFile, lfn) )) {
 			return id;
 		}
 		if( !isNull(id = getID(conn, "Files", "LogicalFileName", lfn, excep)) ) {
 			this.data.localFile = new Hashtable();//Just store one file id only
 			this.data.localFile.put(lfn, id);
 		}
 		return id;
 	}
 	
 	private String getFileStatusID(Connection conn, String status, boolean excep) throws Exception {
 		String id = "";
 		if(!isNull( id = get(this.data.localFileStatus, status) )) {
 			return id;
 		}
 		//Looking in Global Cache 
 		if(!isNull( id = this.data.getGlobalCache().getFileStatusID(conn, status)))  {
 			this.data.localFileStatus.put(status, id);
 			return id;
 		}
 		return getID(conn, "FileStatus", "Status", status , excep);
 	}
 
 	private String getFileTypeID(Connection conn, String type, boolean excep) throws Exception {
 		String id = "";
 		if(!isNull( id = get(this.data.localFileType, type) )) {
 			return id;
 		}
 		//Looking in Global Cache 
 		if(!isNull( id = this.data.getGlobalCache().getFileTypeID(conn, type)))  {
 			this.data.localFileType.put(type, id);
 			return id;
 		}
 		return getID(conn, "FileType", "Type", type , excep);
 	}
 
 	private String getFileValStatusID(Connection conn, String status, boolean excep) throws Exception {
 		String id = "";
 		if(!isNull( id = get(this.data.localFileValStatus, status) )) {
 			return id;
 		}
 		//Looking in Global Cache 
 		if(!isNull( id = this.data.getGlobalCache().getFileValStatusID(conn, status)))  {
 			this.data.localFileValStatus.put(status, id);
 			return id;
 		}
 		return getID(conn, "FileValidStatus", "Status", status , excep);
 	}
 }
