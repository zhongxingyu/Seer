 
 /**
 $Revision: 1.42 $"
 $Id: DBSSql.java,v 1.42 2007/01/04 15:36:38 afaq Exp $"
  *
  */
 package dbs.sql;
 import java.util.Hashtable;
 import java.util.Enumeration;
 import java.io.StringReader;
 import java.sql.Connection;
 import java.sql.Timestamp;
 //import java.sql.Clob;
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 import dbs.util.DBSUtil;
 import db.DBManagement;
 
 
 
 /**
  * This class is the SQL generator. All the methods in this class are static method. The main function of this class is to construct a <code>java.sql.PreparedStatement</code> with the proper query. All the methods returns the constructed <code>java.sql.PreparedStatement</code> after packing it with proper variables' values that are passed in as parameters to various method calls. All the methods can throw and <code>java.sql.SQLException</code> if the prepared statement fails to construct.
  */
 public class DBSSql {
 	/**
 	 * 
 	 */
 	public static String getDual() throws SQLException {
 		return "SELECT 1 FROM dual";
 	}
 
 	public static PreparedStatement getSchemaVersion(Connection conn) throws SQLException {
 
 		String sql = "SELECT SchemaVersion FROM SchemaVersion";
 		PreparedStatement ps = DBManagement.getStatement(conn, sql);
                 DBSUtil.writeLog("\n\n" + ps + "\n\n");
 		return ps;
         }
 
        	public static PreparedStatement insertName(Connection conn, String tableName, String key, String value, String cbUserID, String lmbUserID, String cDate) throws SQLException {	
 		Hashtable table = new Hashtable();
 		table.put(key, value);
 		table.put("CreatedBy", cbUserID);
 		table.put("LastModifiedBy", lmbUserID);
 		table.put("CreationDate", cDate);
 		return getInsertSQL(conn, tableName, table);
 	}
 	
 	public static PreparedStatement insertMap(Connection conn, String tableName, String key1, String key2, String value1, String value2, String cbUserID, String lmbUserID, String cDate) throws SQLException {
 		Hashtable table = new Hashtable();
 		table.put(key1, value1);
 		table.put(key2, value2);
 		table.put("CreatedBy", cbUserID);
 		table.put("LastModifiedBy", lmbUserID);
 		table.put("CreationDate", cDate);
 		return getInsertSQL(conn, tableName, table);
 	}
 	
 
 	public static PreparedStatement insertPrimaryDataset(Connection conn, String ann, String name, String descID, String startDate, String endDate, String typeID , String cbUserID, String lmbUserID, String cDate) throws SQLException {
 		Hashtable table = new Hashtable();
 		table.put("Annotation", ann);
 		table.put("Name", name);
 		table.put("Description", descID);
 		table.put("StartDate", startDate);
 		table.put("EndDate", endDate);
 		table.put("Type", typeID);
 		table.put("CreatedBy", cbUserID);
 		table.put("LastModifiedBy", lmbUserID);
 		table.put("CreationDate", cDate);
 		return getInsertSQL(conn, "PrimaryDataset", table);
 	}
 
 	public static PreparedStatement insertRun(Connection conn, String runNumber, String nOfEvents, String nOfLumiSections, String totalLumi, String storeNumber, String startOfRun, String endOfRun, String cbUserID, String lmbUserID, String cDate) throws SQLException {
 		Hashtable table = new Hashtable();
 		table.put("RunNumber", runNumber);
 		table.put("NumberOfEvents", nOfEvents);
 		table.put("NumberOfLumiSections", nOfLumiSections);
 		table.put("TotalLuminosity", totalLumi);
 		table.put("StoreNumber", storeNumber);
 		table.put("StartOfRun", startOfRun);
 		table.put("EndOfRun", endOfRun);
 		table.put("CreatedBy", cbUserID);
 		table.put("LastModifiedBy", lmbUserID);
 		table.put("CreationDate", cDate);
 		return getInsertSQL(conn, "Runs", table);
 	}
 
 	public static PreparedStatement insertBlock(Connection conn, String size, String name, String procDSID, String nOfFiles, String nOfEvts, String openForWriting, String cbUserID, String lmbUserID, String cDate) throws SQLException {
 		Hashtable table = new Hashtable();
 		table.put("BlockSize", size);
 		table.put("Name", name);
 		table.put("Dataset", procDSID);
 		table.put("NumberOfFiles", nOfFiles);
 		table.put("NumberOfEvents", nOfEvts);
 		table.put("OpenForWriting", openForWriting);
 		table.put("CreatedBy", cbUserID);
 		table.put("LastModifiedBy", lmbUserID);
 		table.put("CreationDate", cDate);
 		return getInsertSQL(conn, "Block", table);
 	}
 
 	public static PreparedStatement insertLumiSection(Connection conn, String lsNumber, String runID, String startEvNumber, String endEvNumber, String lStartTime, String lEndTime, String cbUserID, String lmbUserID, String cDate) throws SQLException {
 		Hashtable table = new Hashtable();
 		table.put("LumiSectionNumber", lsNumber);
 		table.put("RunNumber", runID);
 		table.put("StartEventNumber", startEvNumber);
 		table.put("EndEventNumber", endEvNumber);
 		table.put("LumiStartTime", lStartTime);
 		table.put("LumiEndTime", lEndTime);
 		table.put("CreatedBy", cbUserID);
 		table.put("LastModifiedBy", lmbUserID);
 		table.put("CreationDate", cDate);
 		return getInsertSQL(conn, "LumiSection", table);
 	}
 		       
         public static PreparedStatement insertPerson(Connection conn, String userName, String userDN, String contactInfo, String cbUserID, String lmbUserID, String cDate) throws SQLException {
 		Hashtable table = new Hashtable();
 		table.put("Name", userName);
 		table.put("DistinguishedName", userDN);
 		table.put("ContactInfo", contactInfo);
 		table.put("CreatedBy", cbUserID);
 		table.put("LastModifiedBy", lmbUserID);
 		table.put("CreationDate", cDate);
 		return getInsertSQL(conn, "Person", table);
 	}
 
 
 
 	public static PreparedStatement insertParameterSet(Connection conn, String hash, String name, String version, String type, String annotation, String content, String cbUserID, String lmbUserID, String cDate) throws SQLException {
 		Hashtable table = new Hashtable();
 		table.put("Hash", hash);
 		table.put("Name", name);
 		table.put("Version", version);
 		table.put("Type", type);
 		table.put("Annotation", annotation);
 		table.put("Content", content);
 		table.put("CreatedBy", cbUserID);
 		table.put("LastModifiedBy", lmbUserID);
 		table.put("CreationDate", cDate);
 		return getInsertSQL(conn, "QueryableParameterSet", table);
 	}
 				       
         public static PreparedStatement insertApplication(Connection conn, String exeID, String versionID, String familyID, String psID, String cbUserID, String lmbUserID, String cDate) throws SQLException {
 		Hashtable table = new Hashtable();
 		table.put("ExecutableName", exeID);
 		table.put("ApplicationVersion", versionID);
 		table.put("ApplicationFamily", familyID);
 		table.put("ParameterSetID", psID);
 		table.put("CreatedBy", cbUserID);
 		table.put("LastModifiedBy", lmbUserID);
 		table.put("CreationDate", cDate);
 		return getInsertSQL(conn, "AlgorithmConfig", table);
 	}
 
 	// SQL for inserting ProcessedDatatset and its related tables.
 	// ____________________________________________________
 
 	public static PreparedStatement insertProcessedDatatset(Connection conn, String name, String primDSID, String openForWriting, String phyGroupID, String statusID, String cbUserID, String lmbUserID, String cDate) throws SQLException {
 		Hashtable table = new Hashtable();
 		table.put("Name", name);
 		table.put("PrimaryDataset", primDSID);
 		//table.put("OpenForWriting", );OpenForWriting flag can be managed through Status why we duplicate
 		table.put("PhysicsGroup", phyGroupID);
 		table.put("Status", statusID);
 		table.put("CreatedBy", cbUserID);
 		table.put("LastModifiedBy", lmbUserID);
 		table.put("CreationDate", cDate);
 		return getInsertSQL(conn, "ProcessedDataset", table);
 	}
 	
 	public static PreparedStatement insertPhysicsGroup(Connection conn, String name, String conID, String cbUserID, String lmbUserID, String cDate) throws SQLException {
 		Hashtable table = new Hashtable();
 		table.put("PhysicsGroupName", name);
 		table.put("PhysicsGroupConvener", conID);
 		table.put("CreatedBy", cbUserID);
 		table.put("LastModifiedBy", lmbUserID);
 		table.put("CreationDate", cDate);
 		return getInsertSQL(conn, "PhysicsGroup", table);
 	}
 
 
 	// SQL for inserting File and its related tables.
 	// ____________________________________________________
 	
 	//public static String insertFile(Connection conn, String procDSID, String blockID, String lfn, String checksum, String nOfEvents, String size, String fileStatusID, String typeID, String valStatusID, String qMetaData, String cbUserID, String lmbUserID) throws SQLException {
 	public static PreparedStatement insertFile(Connection conn, String procDSID, String blockID, String lfn, String checksum, String nOfEvents, String size, String fileStatusID, String typeID, String valStatusID, String qMetaData, String cbUserID, String lmbUserID, String cDate) throws SQLException {
 		Hashtable table = new Hashtable();
 		table.put("LogicalFileName", lfn);
 		table.put("Dataset", procDSID);
 		table.put("Block", blockID);
 		table.put("Checksum", checksum);
 		table.put("NumberOfEvents", nOfEvents);
 		table.put("FileSize", size);
 		table.put("FileStatus", fileStatusID);
 		table.put("FileType", typeID);
 		table.put("ValidationStatus", valStatusID);
 		table.put("QueryableMetadata", qMetaData);
 		table.put("CreatedBy", cbUserID);
 		table.put("LastModifiedBy", lmbUserID);
 		table.put("CreationDate", cDate);
 		return getInsertSQL(conn, "Files", table);
 	}
 	
 
         public static PreparedStatement insertAnalysisDataset(Connection conn, String ann,
                              String name,
                              String query,
                              String processedDSID,
                              String typeID,
                              String statusID, 
                              String phyGroupID, 
                              String cbUserID, String lmbUserID, String cDate) throws SQLException
         {
 		Hashtable table = new Hashtable();
 		table.put("Annotation", ann);
 		table.put("Name", name);
 		table.put("Query", query);
 		table.put("ProcessedDS", processedDSID);
 		table.put("Type", typeID);
 		table.put("Status", statusID);
 		table.put("PhysicsGroup", phyGroupID);
 		table.put("CreatedBy", cbUserID);
 		table.put("LastModifiedBy", lmbUserID);
 		table.put("CreationDate", cDate);
 		return getInsertSQL(conn, "AnalysisDataset", table);
         }
 
 	public static PreparedStatement updateBlock(Connection conn, String blockID) throws SQLException {
 		String sql = "UPDATE Block \n" +
 			"SET BlockSize = (SELECT SUM(FileSize) FROM Files f WHERE f.Block = ?) , \n" +
 			"NumberOfFiles = (SELECT COUNT(*) FROM Files f WHERE f.Block = ?) , \n" +
 			"NumberOfEvents= (SELECT SUM(NumberOfEvents) FROM Files f WHERE f.Block = ?) \n" +
 			"WHERE ID = ?" ;
 		PreparedStatement ps = DBManagement.getStatement(conn, sql);
                 int columnIndx = 1;
 		ps.setString(columnIndx++, blockID);
 		ps.setString(columnIndx++, blockID);
 		ps.setString(columnIndx++, blockID);
 		ps.setString(columnIndx++, blockID);
                 DBSUtil.writeLog("\n\n" + ps + "\n\n");
 		return ps;
 	}
 
 
 
         public static PreparedStatement closeBlock(Connection conn, String blockID) throws SQLException {
                 String sql = "UPDATE Block \n" +
                         "SET OpenForWriting=0 \n" +
                         "WHERE ID = ?" ;
                 PreparedStatement ps = DBManagement.getStatement(conn, sql);
                 int columnIndx = 1;
                 ps.setString(columnIndx++, blockID);
                 DBSUtil.writeLog("\n\n" + ps + "\n\n");
                 return ps;
         }
 
 	//FIXME Just use this and delete all other getBlockIds
 	public static PreparedStatement getBlock(Connection conn, String procDSID,  String blockName) throws SQLException {
 		String sql = "SELECT b.ID as ID, \n " +
 			"b.Name as NAME, \n" +
 			"b.BlockSize as BLOCKSIZE, \n" +
 			"b.NumberOfFiles as NUMBER_OF_FILES, \n" +
 			"b.OpenForWriting as OPEN_FOR_WRITING \n" +
 			"FROM Block b \n";
 		if(procDSID != null && blockName != null) {
 			sql += "WHERE b.Dataset = ? AND  b.Name = ? \n";
 		} else if(procDSID != null) {
 			sql += "WHERE b.Dataset = ? \n";
 		} else if(blockName != null) {
 			sql += "WHERE b.Name = ? \n";
 		}
 
 		PreparedStatement ps = DBManagement.getStatement(conn, sql);
                 int columnIndx = 1;
 		if(procDSID != null && blockName != null) {
                 	ps.setString(columnIndx++, procDSID);
                 	ps.setString(columnIndx++, blockName);
 		} else if(procDSID != null) {
                 	ps.setString(columnIndx++, procDSID);
 		} else if(blockName != null) {
                 	ps.setString(columnIndx++, blockName);
 		}
 
                 DBSUtil.writeLog("\n\n" + ps + "\n\n");
 		return ps;
 	}
 
 
         public static PreparedStatement getOpenBlockID(Connection conn, String processedDSID) throws SQLException {
                 String sql = "SELECT blk.ID as ID, \n" +
                              "blk.BlockSize as BLOCKSIZE, \n" +
                              "blk.NumberOfFiles as NUMBER_OF_FILES \n" +
                              "From Block blk \n" +
                              " WHERE blk.Dataset = ?";
                 PreparedStatement ps = DBManagement.getStatement(conn, sql);
                 int columnIndx = 1;
                 ps.setString(columnIndx++, processedDSID);
                 DBSUtil.writeLog("\n\n" + ps + "\n\n");
                 return ps;
         }  
 
 
         public static PreparedStatement getBlockID(Connection conn, String blockName) throws SQLException {
                 String sql = "SELECT b.ID as ID, \n" +
                              "b.BlockSize as BLOCKSIZE, \n" +
                              "b.NumberOfFiles as NUMBER_OF_FILES, \n" +
                              "b.OpenForWriting as OPEN_FOR_WRITING \n" +
                              "FROM Block b \n" +
                              "WHERE b.Name = ?";
                 PreparedStatement ps = DBManagement.getStatement(conn, sql);
                 int columnIndx = 1;
                 ps.setString(columnIndx++, blockName);
                 DBSUtil.writeLog("\n\n" + ps + "\n\n");
                 return ps;
         }
 
 
 	// ____________________________________________________
 	
 	
 	
         public static PreparedStatement listLumiIDsForRun(Connection conn, String runNumber)
         throws SQLException
         {
               //We can expand this query if we need, for now we don't !
               String sql = "SELECT ls.id as ID \n "+
                            "FROM LumiSection ls \n"+
                            "WHERE ls.RunNumber = ? \n";
               PreparedStatement ps = DBManagement.getStatement(conn, sql);
               ps.setString(1, runNumber);
               DBSUtil.writeLog("\n\n" + ps + "\n\n");
               return ps;
         }
 
         public static PreparedStatement listLumiSections(Connection conn, String procDSID) throws SQLException {
 		//FIXME We can expand this query if we need, for now we don't !
 		String sql = "SELECT ls.id as ID, \n " +
 			"ls.LumiSectionNumber as LUMISECTIONNUMBER, \n " +
 			"ls.RunNumber as RUNNUMBER \n" +
 			"FROM LumiSection ls \n" +
 			"JOIN Runs rs\n" +	
 				"ON rs.ID = ls.ID\n" +
 			"JOIN ProcDSRuns pdsr\n" +
 				"ON pdsr.Run = rs.ID\n" +
 			"WHERE pdsr.Dataset = ?";
 		PreparedStatement ps = DBManagement.getStatement(conn, sql);
 		ps.setString(1, procDSID);
 		DBSUtil.writeLog("\n\n" + ps + "\n\n");
 		return ps;
 	}
 
 	public static PreparedStatement listPrimaryDatasets(Connection conn, String pattern) throws SQLException {
 		String sql = "SELECT pd.ID as ID, \n" +
 			"pd.Annotation as ANNOTATION, \n" +
 			"pd.Name as PRIMARY_NAME, \n" +
 			"pd.StartDate as START_DATE, \n"  +
 			"pd.EndDate as END_DATE, \n" + 
 			"pd.CreationDate as CREATION_DATE, \n" +
 			"pd.LastModificationDate as LAST_MODIFICATION_DATE, \n" +
 			"tpd.TriggerPathDescription as TRIGGER_PATH_DESCRIPTION, \n" +
 			"md.MCChannelDescription as MC_CHANNEL_DESCRIPTION, \n" +
 			"md.MCProduction as MC_PRODUCTION, \n" +
 			"md.MCDecayChain as MC_DECAY_CHAIN, \n" +
 			"od.Description as OTHER_DESCRIPTION, \n" +
 			"ty.Type as TYPE, \n" +
 			"percb.DistinguishedName as CREATED_BY, \n" +
 			"perlm.DistinguishedName as LAST_MODIFIED_BY \n" +
 			"FROM PrimaryDataset pd \n" +
 			"LEFT OUTER JOIN PrimaryDSType ty \n" +
 				"ON ty.id = pd.Type \n" +
 			"LEFT OUTER JOIN PrimaryDatasetDescription pdd \n" +
 				"ON pdd.id = pd.Description \n" +
 			"LEFT OUTER JOIN TriggerPathDescription tpd \n" +
 				"ON tpd.id = pdd.TriggerDescriptionID \n" + 
 			"LEFT OUTER JOIN MCDescription md \n" +
 				"ON md.id = pdd.MCChannelDescriptionID \n" +
 			"LEFT OUTER JOIN OtherDescription od \n" +
 				"ON od.id = pdd.OtherDescriptionID \n" +
 			"LEFT OUTER JOIN Person percb \n" +
 				"ON percb.id = pd.CreatedBy \n" +
 			"LEFT OUTER JOIN Person perlm \n" +
				"ON perlm.id = pd.LastModifiedBy \n" +
			"WHERE pd.Name like ?\n" +
 				"ORDER BY PRIMARY_NAME DESC";
 		PreparedStatement ps = DBManagement.getStatement(conn, sql);
 		ps.setString(1, pattern);
                 DBSUtil.writeLog("\n\n" + ps + "\n\n");
 		return ps;
 	}
 
 	public static PreparedStatement listProcessedDatasets(Connection conn, String patternPrim, String patternDT, String patternProc, String patternVer, String patternFam, String patternExe, String patternPS) throws SQLException {
 		String sql = "SELECT procds.id as id, \n" +
 			/*"concat( \n" +
 				"concat( \n" +
 					"concat( \n" +
 						"concat( \n" +
 							"concat('/', primds.Name \n" +
 							"),'/' \n" +
 						"),dt.Name \n" +
 					"),'/' \n" +
 				"), procds.name \n" +
 			") as path, \n" +*/
 			"primds.Name as PRIMARY_DATATSET_NAME, \n" +
 			"dt.Name as DATA_TIER, \n" +
 			"procds.name as PROCESSED_DATATSET_NAME, \n" +
                         //OpenForWriting could be managed by Status we don't need that
 			//"procds.OpenForWriting as OPEN_FOR_WRITING, \n" +
 			"procds.CreationDate as CREATION_DATE, \n" +
 			"procds.LastModificationDate as LAST_MODIFICATION_DATE, \n" +
 			"pg.PhysicsGroupName as PHYSICS_GROUP_NAME, \n" +
 			"perpg.DistinguishedName as PHYSICS_GROUP_CONVENER, \n" +
 			"av.Version as APP_VERSION, \n" +
 			"af.FamilyName as APP_FAMILY_NAME, \n" +
 			"ae.ExecutableName as APP_EXECUTABLE_NAME, \n" +
 			"ps.Name as PS_NAME, \n" +
 			"ps.Hash as PS_HASH, \n" +
 			"ps.Version as PS_VERSION, \n" +
 			"ps.Type as PS_TYPE, \n" +
 			"ps.Annotation as PS_ANNOTATION, \n" +
 			"ps.Content as PS_CONTENT, \n" +
 			"percb.DistinguishedName as CREATED_BY, \n" +
 			"perlm.DistinguishedName as LAST_MODIFIED_BY \n" +
 			"FROM ProcessedDataset procds \n" +
 			"JOIN PrimaryDataset primds \n" +
 				"ON primds.id = procds.PrimaryDataset \n" +
 			"LEFT OUTER JOIN ProcDSTier pdst \n" +
 				"ON pdst.Dataset = procds.id \n" +
 			"LEFT OUTER JOIN DataTier dt \n" +
 				"ON dt.id = pdst.DataTier \n" +
 			"LEFT OUTER JOIN PhysicsGroup pg \n" +
 				"ON pg.id = procds.PhysicsGroup \n" +
 			"LEFT OUTER JOIN Person perpg \n" +
 				"ON perpg.id = pg.PhysicsGroupConvener \n" +
 			"LEFT OUTER JOIN ProcAlgo pa \n" +
 				"ON pa.Dataset = procds.id \n" +
 			"LEFT OUTER JOIN AlgorithmConfig algo \n" +
 				"ON algo.id = pa.Algorithm \n" +
 			"LEFT OUTER JOIN AppVersion av \n" +
 				"ON av.id = algo.ApplicationVersion \n" +
 			"LEFT OUTER JOIN AppFamily af \n" +
 				"ON af.id = algo.ApplicationFamily \n" +
 			"LEFT OUTER JOIN AppExecutable ae \n" +
 				"ON ae.id = algo.ExecutableName \n" +
 			"LEFT OUTER JOIN QueryableParameterSet ps \n" +
 				"ON ps.id = algo.ParameterSetID \n" +
 			"LEFT OUTER JOIN Person percb \n" +
 				"ON percb.id = procds.CreatedBy \n" +
 			"LEFT OUTER JOIN Person perlm \n" +
 				"ON perlm.id = procds.LastModifiedBy \n";
 
 
 		/*if(patternPrim == null) patternPrim = "%";
 		if(patternDT == null) patternDT = "%";
 		if(patternProc == null) patternProc = "%";
 		if(patternVer == null) patternVer = "%";
 		if(patternFam == null) patternFam = "%";
 		if(patternExe == null) patternExe = "%";
 		if(patternPS == null) patternPS = "%";*/
 		boolean useAnd = false;
 		if(!patternPrim.equals("%") || !patternDT.equals("%") || !patternProc.equals("%") || !patternVer.equals("%") || !patternFam.equals("%") || !patternExe.equals("%") || !patternPS.equals("%")) {
 			sql += "WHERE \n";
 		}
 		if(!patternPrim.equals("%")) {
 			sql += " primds.Name like ? \n";
 			useAnd = true;
 		}
 		if(!patternDT.equals("%")) {
 			if(useAnd) sql += " AND ";
 			sql += "dt.Name like ?  \n";
 			useAnd = true;
 		}
 		if(!patternProc.equals("%")) {
 			if(useAnd) sql += " AND ";
 			sql += "procds.name like ? \n";
 			useAnd = true;
 		}
 		if(!patternVer.equals("%")) {
 			if(useAnd) sql += " AND ";
 			sql += "av.Version like ? \n";
 			useAnd = true;
 		}
 		if(!patternFam.equals("%")) {
 			if(useAnd) sql += " AND ";
 			sql += "af.FamilyName like ? \n";
 			useAnd = true;
 		}
 		if(!patternExe.equals("%")) {
 			if(useAnd) sql += " AND ";
 			sql += "ae.ExecutableName like ? \n";
 			useAnd = true;
 		}
 		if(!patternPS.equals("%")) {
 			if(useAnd) sql += " AND ";
 			sql += "ps.Name like ? \n";
 		}
 
 
 		sql += "ORDER BY id, APP_VERSION, APP_FAMILY_NAME, APP_EXECUTABLE_NAME, PS_NAME, DATA_TIER DESC";
 		PreparedStatement ps = DBManagement.getStatement(conn, sql);
                 int columnIndx = 1; 
 		if(!patternPrim.equals("%")) ps.setString(columnIndx++, patternPrim);
 		if(!patternDT.equals("%")) ps.setString(columnIndx++, patternDT);
 		if(!patternProc.equals("%")) ps.setString(columnIndx++, patternProc);
 		if(!patternVer.equals("%")) ps.setString(columnIndx++, patternVer);
 		if(!patternFam.equals("%")) ps.setString(columnIndx++, patternFam);
 		if(!patternExe.equals("%")) ps.setString(columnIndx++, patternExe);
 		if(!patternPS.equals("%")) ps.setString(columnIndx++, patternPS);
                 DBSUtil.writeLog("\n\n" + ps + "\n\n");
 		return ps;
 	}
 
 	public static PreparedStatement listDatasetParents(Connection conn, String procDSID) throws SQLException {
 		String sql = "SELECT procds.id as id, \n" +
 			"concat( \n" +
 				"concat( \n" +
 					"concat( \n" +
 						"concat( \n" +
 							"concat('/', primds.Name \n" +
 							"),'/' \n" +
 						"),dt.Name \n" +
 					"),'/' \n" +
 				"), procds.name \n" +
 			") as PATH, \n" +
 			"procds.CreationDate as CREATION_DATE, \n" +
 			"procds.LastModificationDate as LAST_MODIFICATION_DATE, \n" +
 			"pg.PhysicsGroupName as PHYSICS_GROUP_NAME, \n" +
 			"perpg.DistinguishedName as PHYSICS_GROUP_CONVENER, \n" +
 			"percb.DistinguishedName as CREATED_BY, \n" +
 			"perlm.DistinguishedName as LAST_MODIFIED_BY \n" +
 			"FROM ProcessedDataset procds \n" +
 			"JOIN PrimaryDataset primds \n" +
 				"ON primds.id = procds.PrimaryDataset \n" +
 			"LEFT OUTER JOIN ProcDSTier pdst \n" +
 				"ON pdst.Dataset = procds.id \n" +
 			"LEFT OUTER JOIN DataTier dt \n" +
 				"ON dt.id = pdst.DataTier \n" +
 			"LEFT OUTER JOIN PhysicsGroup pg \n" +
 				"ON pg.id = procds.PhysicsGroup \n" +
 			"JOIN DatasetParentage dp \n" +
 				"ON dp.ItsParent = procds.id \n" +
 			"LEFT OUTER JOIN Person perpg \n" +
 				"ON perpg.id = pg.PhysicsGroupConvener \n" +
 			"LEFT OUTER JOIN Person percb \n" +
 				"ON percb.id = procds.CreatedBy \n" +
 			"LEFT OUTER JOIN Person perlm \n" +
 				"ON perlm.id = procds.LastModifiedBy \n";
 		
 		if(procDSID != null) {
 			sql += "WHERE dp.ThisDataset = ? \n";
 		}
 		sql +=	"ORDER BY PATH DESC";
 		
 		PreparedStatement ps = DBManagement.getStatement(conn, sql);
 		if(procDSID != null) {
 			ps.setString(1, procDSID);
 		}
                 DBSUtil.writeLog("\n\n" + ps + "\n\n");
 		return ps;
 	}
 
 	public static PreparedStatement listAlgorithms(Connection conn, String patternVer, String patternFam, String patternExe, String patternPS) throws SQLException {
 		String sql = "SELECT algo.id as ID, \n" +
 			"av.Version as APP_VERSION, \n" +
 			"af.FamilyName as APP_FAMILY_NAME, \n" +
 			"ae.ExecutableName as APP_EXECUTABLE_NAME, \n" +
 			"ps.Name as PS_NAME, \n" +
 			"ps.Hash as PS_HASH, \n" +
 			"ps.Version as PS_VERSION, \n" +
 			"ps.Type as PS_TYPE, \n" +
 			"ps.Annotation as PS_ANNOTATION, \n" +
 			"ps.Content as PS_CONTENT, \n" +
 			"algo.CreationDate as CREATION_DATE, \n" +
 			"algo.LastModificationDate as LAST_MODIFICATION_DATE, \n" +
 			"percb.DistinguishedName as CREATED_BY, \n" +
 			"perlm.DistinguishedName as LAST_MODIFIED_BY \n" +
 			"FROM AlgorithmConfig algo \n" +
 			"JOIN AppVersion av \n" +
 				"ON av.id = algo.ApplicationVersion \n" +
 			"JOIN AppFamily af \n" +
 				"ON af.id = algo.ApplicationFamily \n" +
 			"JOIN AppExecutable ae \n" +
 				"ON ae.id = algo.ExecutableName \n" +
 			"JOIN QueryableParameterSet ps \n" +
 				"ON ps.id = algo.ParameterSetID \n" +
 			"LEFT OUTER JOIN Person percb \n" +
 				"ON percb.id = algo.CreatedBy \n" +
 			"LEFT OUTER JOIN Person perlm \n" +
 				"ON perlm.id = algo.LastModifiedBy \n";
 
 		/*if(patternVer == null) patternVer = "%";
 		if(patternFam == null) patternFam = "%";
 		if(patternExe == null) patternExe = "%";
 		if(patternPS == null) patternPS = "%";*/
 		
 		sql += "WHERE av.Version like ? \n" +
 			"and af.FamilyName like ? \n" +
 			"and ae.ExecutableName like ? \n" +
 			"and ps.Name like ? \n" +
 			"ORDER BY APP_FAMILY_NAME, APP_EXECUTABLE_NAME, APP_VERSION, PS_NAME DESC";
 		PreparedStatement ps = DBManagement.getStatement(conn, sql);
                 int columnIndx = 1;
 		ps.setString(columnIndx++, patternVer);
 		ps.setString(columnIndx++, patternFam);
 		ps.setString(columnIndx++, patternExe);
 		ps.setString(columnIndx++, patternPS);
                 DBSUtil.writeLog("\n\n" + ps + "\n\n");
 		return ps;
 	}
 
 	public static PreparedStatement listRuns(Connection conn, String procDSID) throws SQLException {
 		String sql = "SELECT run.ID as ID, \n " +
 			"run.RunNumber as RUN_NUMBER, \n" +
 			"run.NumberOfEvents as NUMBER_OF_EVENTS, \n" +
 			"run.NumberOfLumiSections as NUMBER_OF_LUMI_SECTIONS, \n" +
 			"run.TotalLuminosity as TOTAL_LUMINOSITY, \n" +
 			"run.StoreNumber as STRORE_NUMBER, \n" +
 			"run.StartOfRun as START_OF_RUN, \n" +
 			"run.EndOfRun as END_OF_RUN, \n" +
 			"run.CreationDate as CREATION_DATE, \n" +
 			"run.LastModificationDate as LAST_MODIFICATION_DATE, \n" +
 			"percb.DistinguishedName as CREATED_BY, \n" +
 			"perlm.DistinguishedName as LAST_MODIFIED_BY \n" +
 			"FROM Runs run \n" +
 			"JOIN ProcDSRuns pdsr \n" +
 				"ON pdsr.Run = run.id \n" +
 			"LEFT OUTER JOIN Person percb \n" +
 				"ON percb.id = run.CreatedBy \n" +
 			"LEFT OUTER JOIN Person perlm \n" +
 				"ON perlm.id = run.LastModifiedBy \n";
 
 		if(procDSID != null) {
 			sql += "WHERE pdsr.Dataset = ? \n";
 		}
 		sql +=	"ORDER BY RUN_NUMBER DESC";
 		PreparedStatement ps = DBManagement.getStatement(conn, sql);
 		ps.setString(1, procDSID);
                 DBSUtil.writeLog("\n\n" + ps + "\n\n");
 		return ps;
 	}
 
 	public static PreparedStatement listTiers(Connection conn, String procDSID) throws SQLException {
 		String sql = "SELECT dt.ID as ID, \n " +
 			"dt.Name as NAME, \n" +
 			"dt.CreationDate as CREATION_DATE, \n" +
 			"dt.LastModificationDate as LAST_MODIFICATION_DATE, \n" +
 			"percb.DistinguishedName as CREATED_BY, \n" +
 			"perlm.DistinguishedName as LAST_MODIFIED_BY \n" +
 			"FROM DataTier dt \n" +
 			"JOIN ProcDSTier pdst \n" +
 				"ON pdst.DataTier = dt.id \n" +
 			"LEFT OUTER JOIN Person percb \n" +
 				"ON percb.id = dt.CreatedBy \n" +
 			"LEFT OUTER JOIN Person perlm \n" +
 				"ON perlm.id = dt.LastModifiedBy \n";
 
 		if(procDSID != null) {
 			sql += "WHERE pdst.Dataset = ? \n";
 		}
 		sql +=	"ORDER BY NAME DESC";
 		PreparedStatement ps = DBManagement.getStatement(conn, sql);
 		ps.setString(1, procDSID);
                 DBSUtil.writeLog("\n\n" + ps + "\n\n");
 		return ps;
 	}
 
 	public static PreparedStatement listBlocks(Connection conn, String procDSID, String blockName, String seName) throws SQLException {
 		String sql = "SELECT b.ID as ID, \n " +
 			"b.Name as NAME, \n" +
 			"b.NumberOfEvents as NUMBER_OF_EVENTS, \n" +
 			"b.BlockSize as BLOCKSIZE, \n" +
 			"b.NumberOfFiles as NUMBER_OF_FILES, \n" +
 			"b.OpenForWriting as OPEN_FOR_WRITING, \n" +
 			"b.CreationDate as CREATION_DATE, \n" +
 			"b.LastModificationDate as LAST_MODIFICATION_DATE, \n" +
 			"se.SEName as STORAGE_ELEMENT_NAME, \n" +
 			"percb.DistinguishedName as CREATED_BY, \n" +
 			"perlm.DistinguishedName as LAST_MODIFIED_BY \n" +
 			"FROM Block b \n" +
 			"LEFT OUTER JOIN SEBlock seb \n" +
 				"ON seb.BlockID = b.ID \n" +
 			"LEFT OUTER JOIN StorageElement se \n" +
 				"ON se.ID = seb.SEID \n" +
 			"LEFT OUTER JOIN Person percb \n" +
 				"ON percb.id = b.CreatedBy \n" +
 			"LEFT OUTER JOIN Person perlm \n" +
 				"ON perlm.id = b.LastModifiedBy \n";
 
 		boolean useAnd = false;
 		if(procDSID != null) {
 			sql += "WHERE \n";
 		} else if(!seName.equals("%") || !blockName.equals("%") ){//Assumming seName will never be null
 			 sql += "WHERE \n";
 		}
 		if(procDSID != null) {
 			sql += "b.Dataset = ? \n";
 			useAnd = true;
 		}
 		if(!blockName.equals("%")) {
 			if(useAnd) sql += " AND ";
 			sql += "b.Name like ? \n";
 			useAnd = true;
 		}
 		if(!seName.equals("%")) {
 			if(useAnd) sql += " AND ";
 			sql += "se.SEName like ? \n";
 		}
 		
 		sql +=	"ORDER BY NAME DESC";
                 int columnIndx = 1;
 		PreparedStatement ps = DBManagement.getStatement(conn, sql);
 		if(procDSID != null) ps.setString(columnIndx++, procDSID);
 		if(!blockName.equals("%")) ps.setString(columnIndx++, blockName);
 		if(!seName.equals("%")) ps.setString(columnIndx++, seName);
 		
 		DBSUtil.writeLog("\n\n" + ps + "\n\n");
 		return ps;
 	}
 
 	public static PreparedStatement listFiles(Connection conn, String procDSID, String blockID, String patternLFN) throws SQLException {
 		String sql = "SELECT f.ID as ID, \n " +
 			"f.LogicalFileName as LFN, \n" +
 			"f.Checksum as CHECKSUM, \n" +
 			"f.FileSize as FILESIZE, \n" +
 			"f.QueryableMetaData as QUERYABLE_META_DATA, \n" +
 			"f.CreationDate as CREATION_DATE, \n" +
 			"f.LastModificationDate as LAST_MODIFICATION_DATE, \n" +
 			"f.NumberOfEvents as NUMBER_OF_EVENTS, \n" +
 			"vst.Status as VALIDATION_STATUS, \n" +
 			"st.Status as STATUS, \n" +
 			"ty.Type as TYPE, \n" +
                         "b.Name as BLOCK_NAME, \n"+ 
 			"dt.Name as DATA_TIER, \n"+ 
 			"percb.DistinguishedName as CREATED_BY, \n" +
 			"perlm.DistinguishedName as LAST_MODIFIED_BY \n" +
 			"FROM Files f \n" +
 			"LEFT OUTER JOIN Block b \n" +
 				"ON b.id = f.Block \n "+  
 			"LEFT OUTER JOIN FileTier fdt \n" +
 				"ON fdt.Fileid = f.id \n" +
 			"LEFT OUTER JOIN DataTier dt \n" +
 				"ON dt.id = fdt.DataTier " +
 			"LEFT OUTER JOIN FileType ty \n" +
 				"ON ty.id = f.FileType \n" +
 			"LEFT OUTER JOIN FileStatus st \n" +
 				"ON st.id = f.FileStatus \n" +
 			"LEFT OUTER JOIN FileStatus vst \n" +
 				"ON vst.id = f.ValidationStatus \n" +
 			"LEFT OUTER JOIN Person percb \n" +
 				"ON percb.id = f.CreatedBy \n" +
 			"LEFT OUTER JOIN Person perlm \n" +
 				"ON perlm.id = f.LastModifiedBy \n";
 
 		//if(patternLFN == null) patternLFN = "%";
 		sql += "WHERE f.LogicalFileName like ? \n" ;
 		if(procDSID != null) {
 			sql += "and f.Dataset = ? \n";
 		}
 		if(blockID != null) {
 			sql += "and f.Block = ? \n";
 		}
 		sql +=	"ORDER BY LFN DESC";
 		PreparedStatement ps = DBManagement.getStatement(conn, sql);
                 
                 int columnIndx=1;
   
 		ps.setString(columnIndx++, patternLFN);
 		if(procDSID != null) {
 			ps.setString(columnIndx++, procDSID);
 		}
 		if(blockID != null) {
 			ps.setString(columnIndx++, blockID);
 
 		}
                 DBSUtil.writeLog("\n\n" + ps + "\n\n");
 		return ps;
 	}
 
 	public static PreparedStatement listFileParents(Connection conn, String fileID) throws SQLException {
 		String sql = "SELECT f.ID as ID, \n " +
 			"f.LogicalFileName as LFN, \n" +
 			"f.Checksum as CHECKSUM, \n" +
 			"f.FileSize as FILESIZE, \n" +
 			"f.QueryableMetaData as QUERYABLE_META_DATA, \n" +
 			"f.CreationDate as CREATION_DATE, \n" +
 			"f.LastModificationDate as LAST_MODIFICATION_DATE, \n" +
 			"f.NumberOfEvents as NUMBER_OF_EVENTS, \n" +
 			"f.ValidationStatus as VALIDATION_STATUS, \n" +
 			"st.Status as STATUS, \n" +
 			"ty.Type as TYPE, \n" +
                         "b.Name as BLOCK_NAME, \n"+ 
 			"percb.DistinguishedName as CREATED_BY, \n" +
 			"perlm.DistinguishedName as LAST_MODIFIED_BY \n" +
 			"FROM Files f \n" +
 			"JOIN FileParentage fp \n" +
 				"ON fp.ItsParent = f.ID \n" +
 			"LEFT OUTER JOIN Block b \n" +
 				"ON b.id = f.Block \n "+  
 			"LEFT OUTER JOIN FileType ty \n" +
 				"ON ty.id = f.FileType \n" +
 			"LEFT OUTER JOIN FileStatus st \n" +
 				"ON st.id = f.FileStatus \n" +
 			"LEFT OUTER JOIN Person percb \n" +
 				"ON percb.id = f.CreatedBy \n" +
 			"LEFT OUTER JOIN Person perlm \n" +
 				"ON perlm.id = f.LastModifiedBy \n";
 	
 		if(fileID != null) {
 			sql += "WHERE fp.ThisFile = ? \n";
 		}
 		sql +=	"ORDER BY LFN DESC";
 		
 		PreparedStatement ps = DBManagement.getStatement(conn, sql);
 		if(fileID != null) {
 			ps.setString(1, fileID);
 		}
                 DBSUtil.writeLog("\n\n" + ps + "\n\n");
 		return ps;
 	}
 
 	public static PreparedStatement listFileTiers(Connection conn, String fileID) throws SQLException {
 		String sql = "SELECT dt.ID as ID, \n " +
 			"dt.Name as NAME, \n" +
 			"dt.CreationDate as CREATION_DATE, \n" +
 			"dt.LastModificationDate as LAST_MODIFICATION_DATE, \n" +
 			"percb.DistinguishedName as CREATED_BY, \n" +
 			"perlm.DistinguishedName as LAST_MODIFIED_BY \n" +
 			"FROM DataTier dt \n" +
 			"JOIN FileTier ft \n" +
 				"ON ft.DataTier = dt.id \n" +
 			"LEFT OUTER JOIN Person percb \n" +
 				"ON percb.id = dt.CreatedBy \n" +
 			"LEFT OUTER JOIN Person perlm \n" +
 				"ON perlm.id = dt.LastModifiedBy \n";
 		if(fileID != null) {
 			sql += "WHERE ft.Fileid = ? \n";
 		}
 
 		PreparedStatement ps = DBManagement.getStatement(conn, sql);
 		if(fileID != null) {
 			ps.setString(1, fileID);
 		}
                 DBSUtil.writeLog("\n\n" + ps + "\n\n");
 		return ps;
 	}
 
 	public static PreparedStatement listFileAlgorithms(Connection conn, String fileID) throws SQLException {
 		String sql = "SELECT algo.id as ID, \n" +
 			"av.Version as APP_VERSION, \n" +
 			"af.FamilyName as APP_FAMILY_NAME, \n" +
 			"ae.ExecutableName as APP_EXECUTABLE_NAME, \n" +
 			"ps.Name as PS_NAME, \n" +
 			"ps.Hash as PS_HASH, \n" +
 			"algo.CreationDate as CREATION_DATE, \n" +
 			"algo.LastModificationDate as LAST_MODIFICATION_DATE, \n" +
 			"percb.DistinguishedName as CREATED_BY, \n" +
 			"perlm.DistinguishedName as LAST_MODIFIED_BY \n" +
 			"FROM AlgorithmConfig algo \n" +
 			"JOIN FileAlgo fa \n" +
 				"ON fa.Algorithm = algo.id \n" +
 			"JOIN AppVersion av \n" +
 				"ON av.id = algo.ApplicationVersion \n" +
 			"JOIN AppFamily af \n" +
 				"ON af.id = algo.ApplicationFamily \n" +
 			"JOIN AppExecutable ae \n" +
 				"ON ae.id = algo.ExecutableName \n" +
 			"JOIN QueryableParameterSet ps \n" +
 				"ON ps.id = algo.ParameterSetID \n" +
 			"LEFT OUTER JOIN Person percb \n" +
 				"ON percb.id = algo.CreatedBy \n" +
 			"LEFT OUTER JOIN Person perlm \n" +
 				"ON perlm.id = algo.LastModifiedBy \n";
 		if(fileID != null) {
 			sql += "WHERE fa.Fileid = ? \n";
 		}
 		PreparedStatement ps = DBManagement.getStatement(conn, sql);
 		if(fileID != null) {
 			ps.setString(1, fileID);
 		}
                 DBSUtil.writeLog("\n\n" + ps + "\n\n");
 		return ps;
 	}
 
 	public static PreparedStatement listFileLumis(Connection conn, String fileID) throws SQLException {
 		String sql = "SELECT lumi.id as ID, \n" +
 			"lumi.LumiSectionNumber as LUMI_SECTION_NUMBER, \n" +
 			"lumi.RunNumber as RUN_NUMBER, \n" +
 			"lumi.StartEventNumber as START_EVENT_NUMBER, \n" +
 			"lumi.EndEventNumber as END_EVENT_NUMBER, \n" +
 			"lumi.LumiStartTime as LUMI_START_TIME, \n" +
 			"lumi.LumiEndTime as LUMI_END_TIME, \n" +
 			"lumi.CreationDate as CREATION_DATE, \n" +
 			"lumi.LastModificationDate as LAST_MODIFICATION_DATE, \n" +
 			"percb.DistinguishedName as CREATED_BY, \n" +
 			"perlm.DistinguishedName as LAST_MODIFIED_BY \n" +
 			"FROM LumiSection lumi \n" +
 			"JOIN FileLumi fl \n" +
 				"ON fl.Lumi = lumi.id \n" +
 			"LEFT OUTER JOIN Person percb \n" +
 				"ON percb.id = lumi.CreatedBy \n" +
 			"LEFT OUTER JOIN Person perlm \n" +
 				"ON perlm.id = lumi.LastModifiedBy \n";
 		if(fileID != null) {
 			sql += "WHERE fl.Fileid = ? \n";
 		}
 		PreparedStatement ps = DBManagement.getStatement(conn, sql);
 		if(fileID != null) {
 			ps.setString(1, fileID);
 		}
                 DBSUtil.writeLog("\n\n" + ps + "\n\n");
 		return ps;
 	}
 
 	public static PreparedStatement getID(Connection conn, String table, String key, String value) throws SQLException {
 		String sql = "SELECT ID \n " +
 			"FROM " + table + "\n " +
 			"WHERE " + key + " = ? \n";
 		PreparedStatement ps = DBManagement.getStatement(conn, sql);
 		ps.setString(1, value);
                 DBSUtil.writeLog("\n\n" + ps + "\n\n");
 		//return ((String)("SELECT ID AS id FROM " + table + " WHERE " + key + " = '" + value + "'")); 
 		return ps;
 	}
 
 	public static PreparedStatement getMapID(Connection conn, String table, String key1, String key2, String value1, String value2) throws SQLException {
 		String sql = "SELECT ID \n " +
 			"FROM " + table + "\n " +
 			"WHERE " + key1 + " = ? \n" +
 			"AND " + key2 + " = ? \n" ;
 		PreparedStatement ps = DBManagement.getStatement(conn, sql);
                 int columnIndx = 1;
 		ps.setString(columnIndx++, value1);
 		ps.setString(columnIndx++, value2);
                 DBSUtil.writeLog("\n\n" + ps + "\n\n");
 		return ps;
 	}
 	public static PreparedStatement getProcessedDSID(Connection conn, String prim, String dt ,String proc) throws SQLException {
 		String sql = "SELECT procds.ID as ID \n" +
 				"FROM ProcessedDataset procds \n" +
 				"JOIN PrimaryDataset primds \n" +
 					"ON primds.id = procds.PrimaryDataset \n" +
 				"JOIN ProcDSTier pdst \n" +
 					"ON pdst.Dataset = procds.id \n" +
 				"JOIN DataTier dt \n" +
 					"ON dt.id = pdst.DataTier \n";
 		if(prim == null || dt == null || proc == null) {
 			return DBManagement.getStatement(conn, sql);
 		}
 		sql += "WHERE primds.Name = ? \n" +
 			"and dt.Name = ? \n" +
 			"and procds.Name = ? \n";
 		PreparedStatement ps = DBManagement.getStatement(conn, sql);
                 int columnIndx = 1; 
 		ps.setString(columnIndx++, prim);
 		ps.setString(columnIndx++, dt);
 		ps.setString(columnIndx++, proc);
                 DBSUtil.writeLog("\n\n" + ps + "\n\n");
 		return ps;
 	}
 
 
 	public static PreparedStatement getAlgorithmID(Connection conn, String ver, String fam, String exe, String psName) throws SQLException {
 		String sql = "SELECT algo.id \n" +
 			"FROM AlgorithmConfig algo \n" +
 			"JOIN AppVersion av \n" +
 				"ON av.id = algo.ApplicationVersion \n" +
 			"JOIN AppFamily af \n" +
 				"ON af.id = algo.ApplicationFamily \n" +
 			"JOIN AppExecutable ae \n" +
 				"ON ae.id = algo.ExecutableName \n" +
 			"JOIN QueryableParameterSet ps \n" +
 				"ON ps.id = algo.ParameterSetID \n" +
 			"WHERE av.Version = ? \n" +
 			"and af.FamilyName = ? \n" +
 			"and ae.ExecutableName = ? \n" +
 			"and ps.Name = ? \n" ;
 		PreparedStatement ps = DBManagement.getStatement(conn, sql);
                 int columnIndx = 1;
 		ps.setString(columnIndx++, ver);
 		ps.setString(columnIndx++, fam);
 		ps.setString(columnIndx++, exe);
 		ps.setString(columnIndx++, psName);
                 DBSUtil.writeLog("\n\n" + ps + "\n\n");
 		return ps;
 	}
 
 	private static PreparedStatement getInsertSQL (Connection conn, String tableName, Hashtable table) throws SQLException	{
 		String sql = "INSERT INTO " + tableName + " ( \n";
 		String sqlKeys = "  ";
 		String sqlValues = "  ";
 		Enumeration e = table.keys();
 		while(e.hasMoreElements()) {
 			String key = (String)e.nextElement();
 			if(!DBSUtil.isNull( DBSUtil.get(table, key) )) {
 				sqlKeys += "\t" + key + ",\n";
 				sqlValues += "\t?,\n";
 			}
 		}
 		sql += sqlKeys.substring(0, sqlKeys.length() - 2) + 
 			"\n ) VALUES ( \n" + 
 			sqlValues.substring(0, sqlValues.length() - 2) + 
 			"\n)\n";
 		
 		//System.out.println("THE QUERY IS " +sql);
 		
 		PreparedStatement ps = DBManagement.getStatement(conn, sql);
 		e = table.keys();
                 int columnIndx = 1;
 		while(e.hasMoreElements()) {
 			String key = (String)e.nextElement();
 			String value = DBSUtil.get(table, key);
 			if(!DBSUtil.isNull(value)) {
 				if(key.equals("CreationDate")) {
 					ps.setTimestamp(columnIndx++, new Timestamp(Long.valueOf(value)) );
 				//} else if(key.equals("Content")) {
 				//	ps.setCharacterStream(columnIndx++, (new StringReader(value)), value.length());
 					//ps.setClob(columnIndx++, (Clob)(value)) ;
 				} else {
 					ps.setString(columnIndx++, value);
 				}
 			}
 		}
                 DBSUtil.writeLog("\n\n" + ps + "\n\n");
 		return ps;
 
 	}
 	
 	private static String getSQL(String key, String value) {
 		if(!DBSUtil.isNull(value)) {
 			return key;
 		}
 		return "";
 
 	}
 	
 
 }
