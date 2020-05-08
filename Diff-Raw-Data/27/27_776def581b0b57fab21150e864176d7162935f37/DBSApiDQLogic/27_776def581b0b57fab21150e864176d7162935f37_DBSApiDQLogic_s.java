 /**
 $Revision: 1.25 $"
 $Id: DBSApiDQLogic.java,v 1.25 2009/03/24 16:52:15 afaq Exp $"
  *
  */
 
 package dbs.api;
 import java.sql.Connection;
 import db.DBManagement;
 import java.sql.ResultSet;
 import java.sql.PreparedStatement;
 import java.io.Writer;
 import java.util.Hashtable;
 import dbs.sql.DBSSql;
 import dbs.util.DBSUtil;
 import dbs.DBSException;
 import java.util.Vector;
 import java.util.Date;
 import dbs.data.DBSDataCache;
 import java.util.ArrayList;
 
 /**
 * A class that has the core business logic of all the Primary dataset APIs.  The signature for the API is internal to DBS and is not exposed to the clients. There is another class <code>dbs.api.DBSApi</code> that has an interface for the clients. All these low level APIs are invoked from <code>dbs.api.DBSApi</code>. This class inherits from DBSApiLogic class.
 * @author anzar
 */
 public class DBSApiDQLogic extends DBSApiLogic {
 		
 	/**
 	* Constructs a DBSApiLogic object that can be used to invoke several APIs.
 	*/
 
 	DBSApiPersonLogic personApi = null;
 	DBSApiData data = null;
 	public DBSApiDQLogic(DBSApiData data) {
 		super(data);
 		this.data = data;
 		personApi = new DBSApiPersonLogic(data);
 	}
 
        private java.util.ArrayList getAllDatasetParentsFromID(Connection conn, String procDSID) throws Exception {
 
 			java.util.ArrayList allParents = new java.util.ArrayList();
                        java.util.ArrayList dsParents=(new DBSApiProcDSLogic(this.data)).listDatasetParentIDsFromID(conn, procDSID);
 			for (Object parent : dsParents) {
 				if (!parent.equals(procDSID)) {
 					allParents.add(parent);
					allParents.addAll(getAllDatasetParentsFromID(conn, (String)parent));
 				}
 			}
 			return allParents;
         }
 
 
         //Updates value of a FLAG
         public void updateDQIntFlag(Connection conn, Writer out, String procDSID, String runID, String lumiID,
                                                                 String flag, String value,
                                                                 String cbUserID, String creationDate,
                                                                 String lmbUserID
                                                                 ) throws Exception
         {
 		//First get the ID of the row to be changed (Also checks that such an entry DOES exists)
 		//Whatever is the value of this FLAG doesn't matter !!
 		String rowID = getDQIntFlagID(conn, procDSID, runID, lumiID, flag, "", true);
 
                 if (isNull(rowID)) {
                         //lets try to get parent's flag ID if one exists
                         //returns parents ID of passed dataset is added here
                         //java.util.ArrayList dsParents=(new DBSApiProcDSLogic(this.data)).listDatasetParentIDsFromID(conn, procDSID);
                        java.util.ArrayList dsParents=getAllDatasetParentsFromID(conn, procDSID);
 			dsParents.add(procDSID);
                         for (Object pprocDSID : dsParents) {
                                 //Check to see if a parent DQ flag exists
                                 String prowID =  getDQIntFlagID(conn, (String)pprocDSID, runID, lumiID, flag, "", false);
                                 if (!isNull(prowID)) {
                                         //Clone it and insert it for this dataset now
                                         insertDQIntFlag(conn, out, (String)procDSID, runID, lumiID, flag, value,
                                                                                         cbUserID, creationDate, lmbUserID);
                                         //get its ID, and further code below will take care of adding the history
                                         rowID = getDQIntFlagID(conn, procDSID, runID, lumiID, flag, "", true);
                                         break;
                                 }
                         }
                 }
 
                 else {
 
                 	//INSERT into HISTORY table FIRST
                 	PreparedStatement ps = null;
                 	try {
                         	ps = DBSSql.insertDQIntFlagHistory(conn, rowID);
 	                        pushQuery(ps);
         	                ps.execute();
                 	} finally {
 	                        if (ps != null) ps.close();
         	        }
 
                 	ps = null;
 	                try {
         	                ps = DBSSql.updateDQIntFlag(conn, rowID,
                                                         value,
                                                         lmbUserID);
                 	        pushQuery(ps);
                         	ps.executeUpdate();
 	                } finally {
         	                if (ps != null) ps.close();
                 	}
 		}
                 //If rowID is still NULL, the flag is NOT found and hence exception
                 if (isNull(rowID)) throw new DBSException("Unavailable data", "7002", "No DQ entry found" );
 
         }
 
 	//Updates value of a FLAG
         public void updateDQFlag(Connection conn, Writer out, String procDSID, String runID, String lumiID,
                                                                 String flag, String value,
                                                                 String cbUserID, String creationDate,
                                                                 String lmbUserID
                                                                 ) throws Exception
         {
 		//Find the flag
 		//NO Need to Pass the value, this is the UPDATED Value actually
                 String rowID =  getDQFlagID(conn, procDSID, runID, lumiID, flag, "", false);
 
 		if (isNull(rowID)) {
 			//lets try to get parent's flag ID if one exists
                         //returns parents and also ID of passed dataset is added here
                         //java.util.ArrayList dsParents=(new DBSApiProcDSLogic(this.data)).listDatasetParentIDsFromID(conn, procDSID);
			java.util.ArrayList dsParents=getAllDatasetParentsFromID(conn, procDSID);
 			dsParents.add(procDSID);
                         for (Object pprocDSID : dsParents) {
 				//Check to see if a parent DQ flag exists
 				String prowID =  getDQFlagID(conn, (String)pprocDSID, runID, lumiID, flag, "", false);
 				if (!isNull(prowID)) {
 					//Clone it and insert it for this dataset now
 					insertDQFlag(conn, out, (String)procDSID, runID, lumiID, flag, value, 
 											cbUserID, creationDate, lmbUserID);
 					//get its ID, and further code below will take care of adding the history
 					rowID = getDQFlagID(conn, procDSID, runID, lumiID, flag, "", true);
 					break;
 				}
 			}
 		}
 
 		else {
 
         		//INSERT into HISTORY table FIRST
                 	PreparedStatement ps = null;
                 	try {
                         	ps = DBSSql.insertDQFlagHistory(conn, rowID);
 				pushQuery(ps);
                 	        ps.execute();
 	                } finally {
         	                if (ps != null) ps.close();
                 	}
 
 	
 			//Update its value
         		ps = null;
 	                try {
         	                ps = DBSSql.updateDQFlag(conn, rowID,
                                                         getID(conn, "QualityValues", "Value", value, true),
                                                         lmbUserID);
 				pushQuery(ps);
                         	ps.executeUpdate();
 	                } finally {
         	                if (ps != null) ps.close();
                 	}
 		}
 		//If rowID is still NULL, the flag is NOT found and hence exception
 		if (isNull(rowID)) throw new DBSException("Unavailable data", "7002", "No DQ entry found" );
 
         }
 
 	//Insert a FLAG and returns its ID (Even if it already exists)	
 	//public String  AA-04/28-2008 no one need the ID back anymore
 	public void insertDQFlag(Connection conn, Writer out, String procDSID, String runID, String lumiID, 
 								String flag, String value,
 								String cbUserID, String creationDate,
 								String lmbUserID
 								) throws Exception 
 	{
 		//ONLY insert if its NOT already present, why the F*** the MySQL MUL-UQ doesn't work ?
 		String flagID =  null;
 		if ( ! isNull(flagID = getDQFlagID(conn, procDSID, runID, lumiID, flag, "", false))) {
 			DBSUtil.writeLog("FLAG:="+flag+" for This Run (LumiSection) already exixts with value:="+value);
 			writeWarning(out, "Already Exists", "1020", "FLAG:="+flag+
 							" for This Run (LumiSection) already exixts with value:="+value );	
 			return;
 		}
 
                 PreparedStatement ps = null;
                 try {
                         ps = DBSSql.insertDQFlag(conn, procDSID, runID, lumiID, 
                                                         getIDNoCheck(conn, "SubSystem", "Name", flag, true),
                                                         getID(conn, "QualityValues", "Value", value, true),
                                                         cbUserID, lmbUserID, creationDate);
 			pushQuery(ps);
                         ps.execute();
                 } finally {
                         if (ps != null) ps.close();
                 }
 
 		//ID of latest insert, going tobe used right now        
                 //flagID = getDQFlagID(conn, runID, lumiID, flag, value, true);
 		//return flagID;
         }
 
 	//Insert a DQ Flag with Integer value
         public void insertDQIntFlag(Connection conn, Writer out, String procDSID, String runID, String lumiID,
                                                                 String flag, String value,
                                                                 String cbUserID, String creationDate,
                                                                 String lmbUserID
                                                                 ) throws Exception
         {
                 //ONLY insert if its NOT already present, why the F*** the MySQL MUL-UQ doesn't work ?
                 String flagID =  null;
                 if ( ! isNull(flagID = getDQIntFlagID(conn, procDSID, runID, lumiID, flag, "", false))) {
                         DBSUtil.writeLog("FLAG:="+flag+" for This Run (LumiSection) already exixts with value:="+value);
                         writeWarning(out, "Already Exists", "1020", "FLAG:="+flag+
                                                         " for This Run (LumiSection) already exixts with value:="+value );
 			return;
                 }
 
                 PreparedStatement ps = null;
                 try {
                         ps = DBSSql.insertDQIntFlag(conn, procDSID, runID, lumiID,
                                                         getIDNoCheck(conn, "SubSystem", "Name", flag, true),
                                                         value,
                                                         cbUserID, lmbUserID, creationDate);
 			pushQuery(ps);
                         ps.execute();
                 } finally {
                         if (ps != null) ps.close();
                 }
 
                 //ID of latest insert, going tobe used right now        
                 //flagID = getDQFlagID(conn, runID, lumiID, flag, value, true);
                 //return flagID;
         }
 
 	
 
         /* In future we may like a functions like these
 		private void insertDQFlag(Connection conn, String runNumber, String lumiNumber,
                                                                 String flag, String value,
                                                                 String cbUserID, String creationDate,
                                                                 String lmbUserID
                                                                 ) throws Exception {
 
 	}
 	
 
 	public void getDQFlagID(Connection conn, String runNumber, String lumiNumber, String flag, String value)
 		throws Exception 
 	{
 	}
 	*/
 
 
 	
         private String getDQFlagID(Connection conn, String procDSID, String runID, String lumiID, String flag, String value, boolean excep)
                 throws Exception 
         {
 
 		String id = null;
 		PreparedStatement ps = null;
 		ResultSet rs = null;
 
 		//User of this method may chose to provide any of the values
 		String flagid = null;
 		String valueid = null;
 		if (!isNull(flag)) flagid = getIDNoCheck(conn, "SubSystem", "Name", flag, true);
 		if (!isNull(value)) valueid = getID(conn, "QualityValues", "Value", value, true);
 
                 try {
                         ps = DBSSql.getDQFlag(conn, procDSID, runID, lumiID, flagid,  valueid);
 			pushQuery(ps);
                         rs =  ps.executeQuery();
 
 			if(!rs.next()) {
 					if (excep)
 						throw new DBSException("Unavailable data", "7002", "No DQ entry for RunID:" + runID );
 					else return id;
                         }
                         id = get(rs, "ID");
 
                 } finally {
 
                         if (ps != null) ps.close();
                         if (rs != null) rs.close();
                 }
 		//Return the ID of recently inserted row, comes VERY HANDY
 		return id;	
 	}
 
         private String getDQIntFlagID(Connection conn, String procDSID, String runID, String lumiID, String flag, String value, boolean excep)
                 throws Exception
         {
 
                 String id = null;
                 PreparedStatement ps = null;
                 ResultSet rs = null;
 
                 //User of this method may chose to provide any of the values
                 String flagid = null;
                 String valueid = null;
                 if (!isNull(flag)) flagid = getIDNoCheck(conn, "SubSystem", "Name", flag, true);
                 //if (!isNull(value)) valueid = getID(conn, "QualityValues", "Value", value, true);
 
                 try {
                         ps = DBSSql.getDQIntFlag(conn, procDSID, runID, lumiID, flagid,  value);
 			pushQuery(ps);
                         rs =  ps.executeQuery();
 
                         if(!rs.next()) {
                                         if (excep)
                                                 throw new DBSException("Unavailable data", "7002", "No DQ entry for RunID:" + runID );
                                         else return id;
                         }
                         id = get(rs, "ID");
 
                 } finally {
 
                         if (ps != null) ps.close();
                         if (rs != null) rs.close();
                 }
                 //Return the ID of recently inserted row, comes VERY HANDY
                 return id;
         }  
 
 	public String getDQVerTimeStamp(Connection conn, String dqVersion) throws Exception {
 	
 		String timeStamp = null;
 		PreparedStatement ps = null;
                 ResultSet rs = null;
 		try {
                         ps = DBSSql.getDQVerTimeStamp(conn, dqVersion);
 			pushQuery(ps);
 			rs =  ps.executeQuery();
 
                         if(!rs.next()) {
                                         throw new DBSException("Unavailable data", "7008", "DQ Version:="+dqVersion+" do not exists in DBS" );
                         }
                         timeStamp = get(rs, "TIMESTAMP");
 
                 } finally {
 
                         if (ps != null) ps.close();
                         if (rs != null) rs.close();
                 }
                 return timeStamp;
 
 
 	}
 
 
         public void listDQVersions(Connection conn, Writer out) throws Exception {
                 PreparedStatement ps = null;
                 ResultSet rs =  null;
 
                 try {
                         ps = DBSSql.listDQVersions(conn);
 			pushQuery(ps);
                         rs =  ps.executeQuery();
                         while(rs.next()) {
                                 out.write( (String) "<dq_version version='"+get(rs, "DQ_VERSION")+"' time_stamp='"+get(rs, "TIME_STAMP")+"' />");
 			}
 
                 } finally {
                         if (rs != null) rs.close();
                         if (ps != null) ps.close();
                 }
         }
 
         public void listSubSystems(Connection conn, Writer out) throws Exception {
 		PreparedStatement ps = null;
                 ResultSet rs =  null;
 
 		try {
                         ps = DBSSql.listSubSystems(conn);
 			pushQuery(ps);
                         rs =  ps.executeQuery();
 			while(rs.next()) {
 				out.write( (String) "<sub_system name='"+get(rs, "SUBSYSTEM")+"' parent='"+get(rs, "PARENT")+"' />");
 				
 			} 
 		} finally {
                         if (rs != null) rs.close();
                         if (ps != null) ps.close();
                 }
 	}
 
 
 	public void listFilesForRunLumiDQ(Connection conn, Writer out, String query, String timeStamp, String dqVersion) throws Exception {
 		writeWarning(out, "API Deprecated", "17009",
                                 "listFilesForRunLumiDQ API is deprecated, same functionality can be achieved via QueryLanguage");
 	}
 
         public ArrayList listRunsForRunLumiDQ(Connection conn, String dsQueryForDQ, 
 								ArrayList dsQueryBindValues, String dqQuery)  throws Exception {
 
 		Boolean con_mng=false;
 		ArrayList ret = new ArrayList();
 		java.util.ArrayList dsParents = new ArrayList();
 		PreparedStatement ps = null;
 		ResultSet rs = null;
 		try {
                         if (conn==null) {
                                 conn = DBManagement.getDBConnManInstance().getConnection();
                                 if (conn == null) throw new DBSException("Unable to create database connection");
 				con_mng=true;
                         }
 
                         DBSApiData data = new DBSApiData() ;
                         DBSDataCache cache = DBSDataCache.getDBSDataCacheInstance(conn);
                         data.setGlobalCache(DBSDataCache.getDBSDataCacheInstance(conn));
 
 			ps = DBSSql.getSelectSQL(conn, dsQueryForDQ, dsQueryBindValues);
 			//pushQuery(ps);
 			rs =  ps.executeQuery();
 			boolean dsFound=false;
 			//For each dataset, lets find it parent
 			while (rs.next() ) {
 				dsFound=true;		
 				String path=get(rs, "PATH");
				String procDSID = (new DBSApiProcDSLogic(this.data)).getProcessedDSID(conn, path, true);
				dsParents.addAll(getAllDatasetParentsFromID(conn, procDSID));
 				dsParents.add(procDSID);
 				//returns parents and also ID of passed dataset
 				//dsParents.addAll((new DBSApiProcDSLogic(data)).listDatasetParentIDs(conn, path));
 			}
 			if (!dsFound) throw new DBSException("Unavailable data", "1008", "No such dataset found" );
 			removeDuplicate(dsParents);
 			ret = DBSSql.listRunsForRunLumiDQ(conn, dsParents, dqQuery);
                 } finally {
                         if(con_mng) conn.close();
                         if (rs != null) rs.close();
                         if (ps != null) ps.close();
                 }
                 return ret;
 	}
 
 	private static void removeDuplicate(ArrayList arlList) {
    		java.util.HashSet h = new java.util.HashSet(arlList);
    		arlList.clear();
    		arlList.addAll(h);
   	}
 
 	//Not used anymore
 	/*
 	private static String getPathFromQuery(String query) {
         	int dataset_ptr=query.indexOf("Dataset=");
         	String tmp1=query.substring(dataset_ptr);
         	int emp_ptr=tmp1.indexOf("&");
 		int eq_ptr=tmp1.indexOf("=");
         	String tmp2=tmp1.substring(eq_ptr+1, emp_ptr);
 		return tmp2;
 	}
 
 	public java.util.ArrayList listRunsForRunLumiDQ_OLD(Connection conn, String query) throws Exception {
 
 		String path=getPathFromQuery(query);
 		Boolean con_mng=false;
 		java.util.ArrayList tmp=new java.util.ArrayList();
 		try {
 			if (conn==null) {
 				conn = DBManagement.getDBConnManInstance().getConnection();
                 		if (conn == null) throw new DBSException("Unable to create database connection");
 			}
 
 			System.out.println(".........CONNECTION CREATED...............");
 			DBSApiData data = new DBSApiData() ;
  			DBSDataCache cache = DBSDataCache.getDBSDataCacheInstance(conn);
                         data.setGlobalCache(DBSDataCache.getDBSDataCacheInstance(conn));
 			System.out.println(".........CACHE CREATED...............");
 			//Get the list of parents
 			java.util.ArrayList dsParents=(new DBSApiProcDSLogic(data)).listDatasetParentIDs(conn, path);
 			System.out.println(".............GOT LIST of PARENTS......"+dsParents.size());
 			//Pass this list and the query to the listRunsForRunLumiDQ() to get DQ
 			tmp=DBSSql.listRunsForRunLumiDQ(conn, dsParents, query);
 		} finally {
 			if(con_mng) conn.close();
 		}
 		return tmp;
 		
 	}*/
 
         public void listRunLumiDQ(Connection conn, Writer out, String path, Vector runDQList, String timeStamp, String dqVersion) throws Exception {
 
 		//returns parents and also ID of passed dataset
 		String procDSID = (new DBSApiProcDSLogic(this.data)).getProcessedDSID(conn, path, true);	
 		//java.util.ArrayList dsParents=(new DBSApiProcDSLogic(this.data)).listDatasetParentIDs(conn, path);
		java.util.ArrayList dsParents=getAllDatasetParentsFromID(conn, procDSID);	
 		dsParents.add(procDSID);
 
                 PreparedStatement ps = null;
                 ResultSet rs =  null;
 
                 if (!isNull(timeStamp) && !isNull(dqVersion)) {
 			throw new DBSException("Duplicate information supplied", "7006",
                                                                 "You have provided both a TimeStamp:="+timeStamp+" and DQ Version:="+dqVersion);
 
 		}
 
 		if (!isNull(dqVersion)) {
 			//Get the time stamp from the version table
 		 	timeStamp = getDQVerTimeStamp(conn, dqVersion);	
 		}
 		Vector alreadyGotID = new Vector();
 		Vector alreadyGotFLAG = new Vector();
 
 		boolean first = true;
                 try {
                         ps = DBSSql.listRunLumiDQ(conn, dsParents, runDQList, timeStamp);
 			pushQuery(ps);
                         rs =  ps.executeQuery();
 
 			String prevRun="";
 			String prevLumi="";
 
 			while(rs.next()) {
 				String runNumber = get(rs, "RUN_NUMBER");
 				String lumiNumber = get(rs, "LUMI_SECTION_NUMBER");
 				if (!runNumber.equals(prevRun) || !lumiNumber.equals(prevLumi)) {
 					if (!first) out.write( (String) "</run>");
 					out.write( (String) "<run run_number='"+runNumber+"'" + 
 									" lumi_section_number='"+lumiNumber+"' >");
 					prevRun = runNumber;
 					prevLumi = lumiNumber;
 					first = false;
 				}
 				String flag = get(rs, "DQ_FLAG");
 				String parent = get(rs, "PARENT");
 				String entryID = get(rs, "ID");
 
 				//System.out.println("ID:"+entryID+" LUD:"+get(rs, "LASTMODIFICATIONDATE")+" DATASET:"
 								//+get(rs, "DATASET")+" DQ_FLAG:"+flag+" VALUE:"+get(rs, "QVALUE"));
 
 				//Assuming that CHILD entries are added AFTER Parent Dataset entries
 				//And we can just skip over Child IDs
                                 //if(!alreadyGotID.contains(entryID)) {
                                 if(!alreadyGotFLAG.contains(flag)) {
 					if ( parent.equals("CMS")) {
                                                 out.write((String) "<dq_sub_system name='"+flag+"'"+
                                                         " value='"+get(rs, "QVALUE")+"'");
                                         } else {
                                                 out.write((String) "<dq_sub_subsys name='"+flag+"'"+
                                                         " value='"+get(rs, "QVALUE")+"'");
                                         }
                                         out.write( (String) " parent='"+parent+"' />" );
 					//AND STORE IT AS WELL
                                         //alreadyGotID.add(entryID);
 					alreadyGotFLAG.add(flag);
                                 }
 			}
 
                 } finally {
                         if (rs != null) rs.close();
                         if (ps != null) ps.close();
                 }
 		if (!first) out.write( (String) "</run>");
         }
 
         public void updateRunLumiDQ(Connection conn, Writer out, String path, Vector runDQList, Hashtable dbsUser) throws Exception {
 
                 String cbUserID = null;
                 String creationDate = null;
                 String lmbUserID = personApi.getUserID(conn, dbsUser);
 
 		DBSUtil.writeLog("Entering updateRunLumiDQ...");
 
                 //This should probably be loaded from database in the cache once !
                 java.util.ArrayList valueList = new java.util.ArrayList();
                 valueList.add("GOOD");
                 valueList.add("BAD");
                 valueList.add("UNKNOWN");
 		String procDSID = (new DBSApiProcDSLogic(this.data)).getProcessedDSID(conn, path, true);
 
  		for (int i = 0; i < runDQList.size() ; ++i) {
                         //Get the Run
                         Hashtable runDQ = (Hashtable) runDQList.get(i);
 
                         creationDate = getTime(runDQ, "creation_date", false);
                         cbUserID = personApi.getUserID(conn, get(runDQ, "created_by"), dbsUser );
 
                         //Get the Run Number
                         String runNumber = get(runDQ, "run_number", true);
                         DBSUtil.writeLog("runNumber: "+runNumber);
 
                         //See if Run Exists, It must, else throw error
                         String runID = getID(conn, "Runs", "RunNumber", runNumber, true);
 
                         //Get lumi_section_number
                         String lumiSecNumber = get(runDQ, "lumi_section_number", false);
                         //See if lumiSecNumber is sent by user and if yes, it must Exists in DBS
                         String lumiID = getID(conn, "LumiSection", "LumiSectionNumber", lumiSecNumber , false);
 
                         //Get the sub-system Vector
                         Vector subSys = DBSUtil.getVector(runDQ, "dq_sub_system");
 
 
 			//Loop over each item
 			for (int j = 0; j < subSys.size() ; ++j) {
 				Hashtable dqFlag = (Hashtable) subSys.get(j);
 
                         	//Check for null yourself!      
                         	String name = DBSUtil.get(dqFlag, "name");
                         	String value = get(dqFlag, "value", true);
                         	DBSUtil.writeLog("Flag: Name..."+name);
                         	DBSUtil.writeLog("Flag: Value..."+value);
 				//Check to see if this an INT
 				//AA-06/12/2008
                                 if ( ! valueList.contains(value) ) {
                                         //Probably its a number
                                         //lets test that 
                                         try {
                                                 int m = Integer.valueOf(value).intValue();
                                         } catch (java.lang.NumberFormatException e) {
                                                 throw new DBSException("Incorrect Data", "7012", "Invalid value: " + value + " For QIM " + name );
                                         }
 					updateDQIntFlag(conn, out, procDSID, runID, lumiID, name, value, cbUserID, creationDate, lmbUserID);
                                 } else {
 
                         		//Let us UPDATE THIS FLAG
                         		updateDQFlag(conn, out, procDSID, runID, lumiID,
                                                 name, value,
                                                 cbUserID, creationDate,
                         		                        lmbUserID);
 
                         		//It may have a sub-sub-system vector to itself 
 					//ASSUMING the INT Type FLAGS do not YET have Sub-Sub FLAGS ??????????
 					//AA-06/12/2008		
                         		Vector subsubSys = DBSUtil.getVector(dqFlag, "dq_sub_subsys");
                         		for (int k = 0; k < subsubSys.size() ; ++k) {
                         			Hashtable dqsubFlag = (Hashtable) subsubSys.get(k);
 						//Check for null yourself! 
 						String subname = DBSUtil.get(dqsubFlag, "name");
                                 		String subvalue = get(dqsubFlag, "value", true);
                                 		DBSUtil.writeLog("      Flag: Sub Name..."+subname);
                                 		DBSUtil.writeLog("      Flag: Sub Value..."+subvalue);
 
                                 		//Let us update THIS FLAG
                                 		updateDQFlag(conn, out, procDSID, runID, lumiID,
                                                                 subname, subvalue,
                                                                 cbUserID, creationDate,
                                                                 lmbUserID);
 					}
 				}
 			}
 		}
 
 	}
 
 
         public void versionDQ(Connection conn, Writer out, Hashtable vertable, Hashtable dbsUser) throws Exception {
 		//Check to see if a version with same name already exists !
 
 		String versionName =  get(vertable, "version", true);
                 String descrp = get(vertable, "description", false);
 
 
 		String versionID = getID(conn, "QualityVersion", "Version", versionName, false);
 		if (!isNull(versionID)) {
 			throw new DBSException("Version Already Exists", "7007",
 							"Data Quality Version:="+versionName+" already exists in DBS, cannnot create again");
 		}
 
 		//String creationDate = Long.toString( (new Date()).getTime() / 1000 );
 		String creationDate = getTime(vertable, "creation_date", false);
 		String cbUserID = personApi.getUserID(conn, dbsUser);
 		String lmbUserID = personApi.getUserID(conn, dbsUser);
 
                 PreparedStatement ps = null;
                 try {
 			ps = DBSSql.insertDQVersion(conn, versionName, descrp, cbUserID, lmbUserID, creationDate);
 			pushQuery(ps);
                         ps.execute();
 
                 } finally {
                         if (ps != null) ps.close();
                 }
 	
 	}
 
         public void insertLumiRangeDQ(Connection conn, Writer out, String path, String runNumber, String stratLumi, String endLumi,
                                                         Vector dqFlags, Hashtable dbsUser) throws Exception {
 
 		String procDSID = (new DBSApiProcDSLogic(this.data)).getProcessedDSID(conn, path, true);
 		String runID = getID(conn, "Runs", "RunNumber", runNumber, false);
 
 		if (isNull (runID )){
 			throw new DBSException("Run Do Not Exist", "7001",
 				"RunNumber: "+ runNumber +", Do Not eixst in DBS, cannot add DQ Flags");
 		}
 
 		//The Run Information MUST already be in DBS to be able to add LumiSections to it !
 		String runExists = getID(conn, "RunLumiQuality", "Run", runID, false);
 	        if (isNull(runExists)) {
                       throw new DBSException("Run Quality Do Not Exist", "7005",
                                                                 "RunNumber: "+runNumber +
                                                                 " Do not have any DataQuality information in DBS, Run DQ goes before Lumi");
                 }
 
 		for (int i = Integer.parseInt(stratLumi); i <= Integer.parseInt(endLumi); ++i) {
 			
 			String lumiID = getID(conn, "LumiSection", "LumiSectionNumber", String.valueOf(i), false);
 			if ( isNull(lumiID) ) {
                                 throw new DBSException("Lumi Section Do Not Exist", "7004",
                                                         "Lumi Section "+String.valueOf(i)+", Do Not eixst in DBS RunNumber "+runNumber+
                                                         ", cannot add DQ Flags");
 
                         }	
 			insertDQFlags(conn, out, procDSID, runID, lumiID, dqFlags, dbsUser);
 		}
 
 
 	}
 
         public void insertSubSystem(Connection conn, Writer out, Hashtable subSys, Hashtable dbsUser) throws Exception {
 
 		//Check for nulls ?
                 String parent = DBSUtil.get(subSys, "parent");
                 String name = DBSUtil.get(subSys, "name");
 
                 String creationDate = getTime(subSys, "creation_date", false);
                 String cbUserID = personApi.getUserID(conn, dbsUser);
                 String lmbUserID = personApi.getUserID(conn, dbsUser);
 
                 String checkID = getIDNoCheck(conn, "SubSystem", "Name", name, false);
                 if ( !isNull (checkID )){
                         writeWarning(out, "Already Exist", "7009",
                                 "SubSystem "+ name +", already eixst in DBS, cannot add again");
                         return;
                 }
 
                 PreparedStatement ps = null;
 
                 try {
                         ps = DBSSql.insertSubSystem(conn, name, parent, cbUserID, lmbUserID, creationDate);
 			pushQuery(ps);
                         ps.execute();
                 } finally {
                         if (ps != null) ps.close();
                 }
 
         }
 
 
 	public void insertRunRangeDQ(Connection conn, Writer out, String path, String startRun, String endRun, 
 							Vector dqFlags, Hashtable dbsUser) throws Exception {
 
 		//We do not need lumiID
 		String lumiID = null;
 		String procDSID = (new DBSApiProcDSLogic(this.data)).getProcessedDSID(conn, path, true);
 
 		for (int i = Integer.parseInt(startRun); i <= Integer.parseInt(endRun); ++i) {
 			//See if Run Exists, It must, else throw error
 			DBSUtil.writeLog("runNumber: "+i);
 			String runID = getID(conn, "Runs", "RunNumber", String.valueOf(i), false);
 			
                         if (isNull (runID )){
                                 throw new DBSException("Run Do Not Exist", "7001",
                                                         "RunNumber: "+ i +", Do Not eixst in DBS, cannot add DQ Flags");
                         }
 
 			insertDQFlags(conn, out, procDSID, runID, lumiID, dqFlags, dbsUser);
 			
 		}	
 
 	}
 		
 	public void insertRunLumiDQ(Connection conn, Writer out, String path,
 							Vector runDQList, Hashtable dbsUser) throws Exception {
 
 	        //String cbUserID = null;
                 //String creationDate = null;
 		//String lmbUserID = personApi.getUserID(conn, dbsUser);
 
 		String procDSID = (new DBSApiProcDSLogic(this.data)).getProcessedDSID(conn, path, true);
 
 		for (int i = 0; i < runDQList.size() ; ++i) {
 			//Get the Run
                         Hashtable runDQ = (Hashtable) runDQList.get(i);
 
 			//creationDate = getTime(runDQ, "creation_date", false);
 			//cbUserID = personApi.getUserID(conn, get(runDQ, "created_by"), dbsUser );
 
 			//Get the Run Number
 			String runNumber = get(runDQ, "run_number", true);
 		        DBSUtil.writeLog("runNumber: "+runNumber);	
 
                         //See if Run Exists, It must, else throw error
                         String runID = getID(conn, "Runs", "RunNumber", runNumber, false);
 
                         if (isNull (runID )){
                                 throw new DBSException("Run Do Not Exist", "7001",
                                                         "RunNumber: "+runNumber+", Do Not eixst in DBS, cannot add DQ Flags");
                         }
 
 
 			//Get lumi_section_number
 			String lumiSecNumber = get(runDQ, "lumi_section_number", false);
 			//See if lumiSecNumber is sent by user and if yes, it must Exists in DBS
 			String lumiID = getID(conn, "LumiSection", "LumiSectionNumber", lumiSecNumber , false);
 
 			if (!isNull(lumiSecNumber) && isNull(lumiID) ) {	
 				throw new DBSException("Lumi Section Do Not Exist", "7004",
 							"Lumi Section "+lumiSecNumber+", Do Not eixst in DBS RunNumber "+runNumber+
 							", cannot add DQ Flags");
 
 			}
 
 			if (!isNull(lumiID) ){
 				//IF LumiSection is provided then The associated Run MUST already be in DBS
 				//This Biz. Rule can change, but for now it holds.
 				//Throw exception if Run doesn't exists in RunLumiQuality Table
 				String runExists = getID(conn, "RunLumiQuality", "Run", runID, false);
 				if (isNull(runExists)) {
 					throw new DBSException("Run Quality Do Not Exist", "7005",
 								"RunNumber: "+runNumber +
 								" Do not have any DataQuality information in DBS, Run DQ goes before Lumi");
 				}
 			}			
 
 			//Get the sub-system Vector
 	
 			Vector subSys = DBSUtil.getVector(runDQ, "dq_sub_system");
 			insertDQFlags(conn, out, procDSID, runID, lumiID, subSys, dbsUser);
 
 	                //returns parents and also ID of passed dataset
         	        //java.util.ArrayList dsParents=(new DBSApiProcDSLogic(this.data)).listDatasetParentIDs(conn, path);
 			//Insert DQ for each of the parent and child datasets
 			//for (Object pprocDSID : dsParents)
 			//	insertDQFlags(conn, out, (String)pprocDSID, runID, lumiID, subSys, dbsUser);
 		}
 
 	}
 
 
 	// AA - 04/28/2008 This was desperate attempt to do batch inserts, I'll pass on this at this moment
 	private void insertDQFlags(Connection conn, Writer out, String procDSID, String runID, String lumiID,
                                                         Vector subSys, Hashtable dbsUser) throws Exception {
 
                 String cbUserID = null;
                 String creationDate = null;
                 String lmbUserID = personApi.getUserID(conn, dbsUser);
 
                 //This should probably be loaded from database in the cache once !
                 java.util.ArrayList valueList = new java.util.ArrayList();
                 valueList.add("GOOD");
                 valueList.add("BAD");
                 valueList.add("UNKNOWN");
 
 		//This can move into DBSSQL later on !!!
 		java.util.ArrayList keys = new java.util.ArrayList();
 		keys.add("Dataset");
 		keys.add("Run");
 		keys.add("Lumi");
 		keys.add("SubSystem");
 		keys.add("DQValue");
 		keys.add("CreationDate");
 		keys.add("CreatedBy");
 		//keys.add("LastModificationDate");
 		keys.add("LastModifiedBy");
 	
 		//Add values in batch for each row
 		java.util.ArrayList values = new java.util.ArrayList();
 
 
 
 
 
 		//Loop over each item
 		for (int j = 0; j < subSys.size() ; ++j) {
 			Hashtable dqFlag = (Hashtable) subSys.get(j);				
 
 			if (j==0) {
 				creationDate = getTime(dqFlag, "creation_date", false);
                         	cbUserID = personApi.getUserID(conn, get(dqFlag, "created_by"), dbsUser );
 			}
 
 			//Check for null yourself!	
 			String name = DBSUtil.get(dqFlag, "name");
 			String value = get(dqFlag, "value", true);
 
                         if ( ! valueList.contains(value) ) {
                                 //Probably its a number
                                 //lets test that 
                                 try {
                                         int i = Integer.valueOf(value).intValue();
                                 } catch (java.lang.NumberFormatException e) {
                                         throw new DBSException("Incorrect Data", "7012", "Invalid value: " + value + " For QIM " + name );
                                 }
                                 //For the time being not using BATCH for INT type of Flags,
 				//Not expecting Too many, but Batch can be done easily later on.
 				// AA 05/01/2008
                          	insertDQIntFlag(conn, out, procDSID, runID, lumiID,
                                                                name, value,
                                                                cbUserID, creationDate,
                                                                lmbUserID);
 			} else {
 				values.add(procDSID);
 				values.add(runID);
 				values.add(lumiID);
 				values.add(getIDNoCheck(conn, "SubSystem", "Name", name, true));
 				values.add(getID(conn, "QualityValues", "Value", value, true));
 				values.add(creationDate);	
 				values.add(cbUserID);
 				values.add(lmbUserID);
 			}
 
 			//It may have a sub-sub-system vector to itself 
 			Vector subsubSys = DBSUtil.getVector(dqFlag, "dq_sub_subsys");
 
 			for (int k = 0; k < subsubSys.size() ; ++k) {
 				Hashtable dqsubFlag = (Hashtable) subsubSys.get(k);
 				//Check for null yourself! 
 				String subname = DBSUtil.get(dqsubFlag, "name");
                                	String subvalue = get(dqsubFlag, "value", true);
 
                         	if ( ! valueList.contains(subvalue) ) {
                                 	//Probably its a number
                                 	//lets test that 
                                 	try {
                                         	int i = Integer.valueOf(subvalue).intValue();
                                 	} catch (java.lang.NumberFormatException e) {
                                         	throw new DBSException("Incorrect Data", "7012", "Invalid value: " + subvalue + " For QIM " + name );
                                 	}
                                 	//For the time being not using BATCH for INT type of Flags,
                                 	//Not expecting Too many, but Batch can be done easily later on.
                                 	// AA 05/01/2008
                                 	insertDQIntFlag(conn, out, procDSID, runID, lumiID,
                                                                name, subvalue,
                                                                cbUserID, creationDate,
                                                                lmbUserID);
                         	} else {
 					values.add(procDSID);
                         		values.add(runID);
 		                	values.add(lumiID);
                         		values.add(getIDNoCheck(conn, "SubSystem", "Name", subname, true));
                         		values.add(getID(conn, "QualityValues", "Value", subvalue, true));
                         		values.add(creationDate); 
                         		values.add(cbUserID);
                         		values.add(lmbUserID);
 				}
 			}
 		}
 
 		if (values.size() > 0) {
 			PreparedStatement ps = null;
 			try {
 				ps = DBSSql.getInsertSQLBatch (conn, "RunLumiQuality", keys, values);
 				//System.out.println("\n\nBATCH CREATED\n\n");
 				pushQuery(ps);
 				ps.executeBatch();
 			} catch (Exception ex) {
 				String exmsg = ex.getMessage();
 				if ( exmsg.startsWith("Duplicate entry") || 
 					exmsg.startsWith("ORA-00001: unique constraint") ) {
 					throw new Exception("Duplicate entries not allowed, use update api instead\n"+exmsg);
 					
 				} else {
 					throw new Exception(ex.getMessage()+ "\nQuery failed is"+ps);
 				}
                 	} finally {
                         	if (ps != null) ps.close();
                 	}
 		}
 	}
 
 /******
 	private void insertDQFlags(Connection conn, Writer out, String runID, String lumiID,
                                                         Vector subSys, Hashtable dbsUser) throws Exception {
 
 
                 String cbUserID = null;
                 String creationDate = null;
                 String lmbUserID = personApi.getUserID(conn, dbsUser);
 
 		//This should probably be loaded from database in the cache once !
 		java.util.ArrayList valueList = new java.util.ArrayList();
 		valueList.add("GOOD");
 		valueList.add("BAD");
 		valueList.add("UNKNOWN");
 
 		//Loop over each item
 		for (int j = 0; j < subSys.size() ; ++j) {
 			Hashtable dqFlag = (Hashtable) subSys.get(j);				
 
 			//Check for null yourself!	
 			String name = DBSUtil.get(dqFlag, "name");
 			String value = get(dqFlag, "value", true);
 			
 
 			DBSUtil.writeLog("Flag: Name..."+name);
 			DBSUtil.writeLog("Flag: Value..."+value);
 
                         creationDate = getTime(dqFlag, "creation_date", false);
                         cbUserID = personApi.getUserID(conn, get(dqFlag, "created_by"), dbsUser );
 
 			//Let us insert THIS FLAG
 
 			if ( ! valueList.contains(value) ) {
 				//Probably its a number
 				//lets test that 
 				try {
 					int i = Integer.valueOf(value).intValue();
 				} catch (java.lang.NumberFormatException e) {
 					throw new DBSException("Incorrect Data", "7012", "Invalid value: " + value + " For QIM " + name );
 				}
 				
 				insertDQIntFlag(conn, out, runID, lumiID,
                                                                name, value,
                                                                cbUserID, creationDate,
                                                                lmbUserID);
 			} else 	insertDQFlag(conn, out, runID, lumiID,
                                                                name, value,
                                                                cbUserID, creationDate,
                                                                lmbUserID);
 
 			//stow away the ID of latest insert for later use
 			//String subFlagID = getDQFlagID(conn, runID, lumiID, name, value, true);
 
 			//It may have a sub-sub-system vector to itself 
 			Vector subsubSys = DBSUtil.getVector(dqFlag, "dq_sub_subsys");
 
 			for (int k = 0; k < subsubSys.size() ; ++k) {
 				Hashtable dqsubFlag = (Hashtable) subsubSys.get(k);
 				//Check for null yourself! 
 				String subname = DBSUtil.get(dqsubFlag, "name");
 
                                	String subvalue = get(dqsubFlag, "value", true);
                                	DBSUtil.writeLog("      Flag: Sub Name..."+subname);
                                	DBSUtil.writeLog("      Flag: Sub Value..."+subvalue);
 
 				//Let us insert THIS FLAG
                                	insertDQFlag(conn, out, runID, lumiID,
                                                                 subname, subvalue,
                                                                 cbUserID, creationDate,
                                                                 lmbUserID);
 
 				//ID of latest insert, going tobe used right now	
 				//String subsubID = getDQFlagID(conn, runID, lumiID, subname, subvalue, true);	
 				//AND associate it to its parent 
 				//(Thats done HARD CODED in SubSystem Table, So I do not need to do that anymore)
 				//Afaq: 06/07/2007
 				//insertMap(conn, out, "QFlagAssoc", "ThisFlag", "ItsAssoc",
                                 //        subFlagID,
                                 //        subsubID,
                                 //        cbUserID, lmbUserID, creationDate);
 			}
 		}
 	}
 
 *********/
 
 }
