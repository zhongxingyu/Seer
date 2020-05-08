 
 /**
 $Revision: 1.73 $"
 $Id: DBSSql.java,v 1.73 2007/02/26 20:41:56 sekhri Exp $"
  *
  */
 package dbs.sql;
 import java.util.Hashtable;
 import java.util.Enumeration;
 import java.util.Vector;
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
 	
 	public static PreparedStatement insertMap(Connection conn, String tableName, String key1, String key2, String key3, String value1, String value2, String value3, String cbUserID, String lmbUserID, String cDate) throws SQLException {
 		Hashtable table = new Hashtable();
 		table.put(key1, value1);
 		table.put(key2, value2);
 		table.put(key3, value3);
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
 
         public static PreparedStatement updateRun(Connection conn, String runNumber, String nOfEvents, String nOfLumiSections, String totalLumi, String endOfRun, String lmbUserID) throws SQLException {
 
                String sql = "UPDATE Runs SET \n";
                         if ( !DBSUtil.isNull(nOfEvents) ) {
                             sql += "NumberOfEvents = ? ,";
                         }
                         if ( !DBSUtil.isNull(nOfLumiSections) ) {
                              sql += "NumberOfLumiSections = ? ,";
                         }
                         if ( !DBSUtil.isNull(totalLumi) ) {
                              sql += "TotalLuminosity = ? ,";
                         }
                         if ( !DBSUtil.isNull(endOfRun) ) {
                              sql += "EndOfRun = ? ,";
                         }
                         sql += "LastModifiedBy = ? \n";
                         sql += "WHERE RunNumber = ?";
 
                 PreparedStatement ps = DBManagement.getStatement(conn, sql);
                 int columnIndx = 1;
                 if ( !DBSUtil.isNull(nOfEvents) ) {
                         ps.setString(columnIndx++, nOfEvents);
                 }
                 if ( !DBSUtil.isNull(nOfLumiSections) ) {
                         ps.setString(columnIndx++, nOfLumiSections);
                 }
                 if ( !DBSUtil.isNull(totalLumi) ) {
                         ps.setString(columnIndx++, totalLumi);
                 }
                 if ( !DBSUtil.isNull(endOfRun) ) {
                         ps.setString(columnIndx++, endOfRun);
                 }
                 ps.setString(columnIndx++, lmbUserID);
                 ps.setString(columnIndx++, runNumber);
                 DBSUtil.writeLog("\n\n" + ps + "\n\n");
 
                 return ps;
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
 
 	public static PreparedStatement insertAnalysisDatasetDefinition(Connection conn, String adsDefName, 
 			String path, String lumiNumberList, String lumiRangeList, String runNumberList, String runRangeList, 
 			String tierList, String fileList, String adsList, String algoList, String userCut, String desc,
 			String cbUserID, String lmbUserID, String cDate) throws SQLException {
 		Hashtable table = new Hashtable();
 		table.put("Name", adsDefName);
 		table.put("LumiSections", lumiNumberList);
 		table.put("LumiSectionRanges", lumiRangeList);
 		table.put("Runs", runNumberList);
 		table.put("RunsRanges", runRangeList);
 		table.put("Algorithms", algoList);
 		table.put("LFNs", fileList);
 		table.put("Path", path);
 		table.put("Tiers", tierList);
 		table.put("AnalysisDatasets", adsList);
 		table.put("UserCut", userCut);
 		table.put("Description", desc);
 		table.put("CreatedBy", cbUserID);
 		table.put("LastModifiedBy", lmbUserID);
 		table.put("CreationDate", cDate);
 		return getInsertSQL(conn, "AnalysisDSDef", table);
 	}
 
 	public static PreparedStatement insertAnalysisDataset(Connection conn, String analysisDatasetName, 
 			String annotation, String procDSID, String anaDSDefID,
 			String type, String status, String parentID, String phyGroupID,
 			String cbUserID, String lmbUserID, String cDate) throws SQLException {
 		Hashtable table = new Hashtable();
 		table.put("Name", analysisDatasetName);
 		table.put("Annotation", annotation);
 		table.put("ProcessedDS", procDSID);
 		table.put("Definition", anaDSDefID);
 		table.put("Type", type);
 		table.put("Status", status);
 		table.put("Parent", parentID);
 		table.put("PhysicsGroup", phyGroupID);
 		table.put("CreatedBy", cbUserID);
 		table.put("LastModifiedBy", lmbUserID);
 		table.put("CreationDate", cDate);
 		return getInsertSQL(conn, "AnalysisDataset", table);
 	}
 
 	//public static PreparedStatement listAnalysisDSFileLumi(Connection conn, String adsID, String procDSID, 
 	public static PreparedStatement listAnalysisDSFileLumi(Connection conn,  String procDSID, 
 			Vector tierIDList, Vector algoIDList, Vector fileList, 
 			Vector lumiRangeList, Vector runRangeList, 
 			Vector adsList, String userCut,	String op,
 			String cbUserID, String lmbUserID, String cDate) throws SQLException {
 		//String sql = "SELECT DISTINCT " + adsID + " as ADSID, \n " +
 		String sql = "SELECT DISTINCT \n" + 
 			"f.ID as FILEID, \n" +
 			"ls.ID as LUMIID \n" +
			"FROM LumiSection ls \n" +
			"JOIN FileRunLumi fl \n\t" +
 				"ON fl.Lumi = ls.ID \n" +
 			"JOIN Files f \n\t" +
 				"ON f.ID = fl.Fileid \n" +
 			"LEFT OUTER JOIN FileTier fdt \n\t" +
 				"ON fdt.Fileid = f.ID \n" +
 			"LEFT OUTER JOIN FileAlgo fa \n\t" +
 				"ON fa.Fileid = f.ID \n" +
 			"LEFT OUTER JOIN Runs r \n\t" +
 				"ON r.ID = ls.RunNumber \n" +
 			"WHERE f.Dataset = ? \n\t" ;
 
 		//if(!DBSUtil.isNull(tierIDList)) sql += op + " fdt.DataTier IN (?) \n\t";
 		if(tierIDList.size() > 0) {
 			String tmpSql = "";
 			for(int i = 0 ; i != tierIDList.size(); ++i) {
 				if(!DBSUtil.isNull(tmpSql)) tmpSql += ",";
 				tmpSql += "?";
 			}
 			sql += op + " fdt.DataTier IN (" + tmpSql + ") \n\t";
 		}
 		
 		if(algoIDList.size() > 0) {
 			String tmpSql = "";
 			for(int i = 0 ; i != algoIDList.size(); ++i) {
 				if(!DBSUtil.isNull(tmpSql)) tmpSql += ",";
 				tmpSql += "?";
 			}
 			sql += op + " fa.Algorithm IN (" + tmpSql + ") \n\t";
 		}
 		
 		if(fileList.size() > 0) {
 			String tmpSql = "";
 			for(int i = 0 ; i != fileList.size(); ++i) {
 				if(!DBSUtil.isNull(tmpSql)) tmpSql += ",";
 				tmpSql += "?";
 			}
 			sql += op + " f.LogicalFileName IN (" + tmpSql + ") \n\t";
 		}
 
 		//if(!DBSUtil.isNull(algoIDList)) sql += op + " fa.Algorithm IN (?) \n\t";
 		//if(!DBSUtil.isNull(fileList)) sql += op + " f.LogicalFileName IN (?) \n\t";
 		if(lumiRangeList.size() > 0) { 
 			String tmpSql = "";
 			for(int i = 0 ; i != lumiRangeList.size(); ++i) {
 				if(!DBSUtil.isNull(tmpSql)) tmpSql += " OR ";
 				tmpSql += "(ls.LumiSectionNumber BETWEEN ? AND ?)\n\t";
 			}
 			sql += op + "( " + tmpSql + " )\n\t";
 		}
 		if(runRangeList.size() > 0) { 
 			String tmpSql = "";
 			for(int i = 0 ; i != runRangeList.size(); ++i) {
 				if(!DBSUtil.isNull(tmpSql)) tmpSql += " OR ";
 				tmpSql += "(r.RunNumber BETWEEN ? AND ?)\n\t";
 			}
 			sql += op + "( " + tmpSql + " )\n\t";
 		}
 		
 		if(adsList.size() > 0) {
 			String tmpSql = "";
 			for(int i = 0 ; i != adsList.size(); ++i) {
 				if(!DBSUtil.isNull(tmpSql)) tmpSql += ",";
 				tmpSql += "?";
 			}	
 			sql += op + " ls.ID IN ( \n\t" +
 					"SELECT adsfl.Lumi FROM AnalysisDSFileLumi adsfl \n\t" +
 					"JOIN AnalysisDataset ad \n\t\t" +
 						"ON ad.ID = adsfl.AnalysisDataset \n\t" +
 					"WHERE ad.Name IN (" + tmpSql + ") \n" +
 				")\n";
 
 		}
 
 		/*if(!DBSUtil.isNull(adsList)) {
 			sql += op + " ls.LumiSectionNumber IN ( \n\t" +
 					"SELECT adsfl.Lumi FROM AnalysisDSFileLumi adsfl \n\t" +
 					"JOIN AnalysisDataset ad \n\t\t" +
 						"ON ad.ID = adsfl.AnalysisDataset \n\t" +
 					"WHERE ad.Name IN (?) \n" +
 				")\n";
 		}*/
 		sql += "ORDER BY FILEID,LUMIID";
 		//System.out.println("The SQL query is " + sql);
 
 		PreparedStatement ps = DBManagement.getStatement(conn, sql);
                 int columnIndx = 1;
 		ps.setString(columnIndx++, procDSID);
 		for(int i = 0 ; i != tierIDList.size(); ++i) ps.setString(columnIndx++, (String)tierIDList.get(i));
 		for(int i = 0 ; i != algoIDList.size(); ++i) ps.setString(columnIndx++, (String)algoIDList.get(i));
 		for(int i = 0 ; i != fileList.size(); ++i) ps.setString(columnIndx++, (String)fileList.get(i));
 		
 		//if(!DBSUtil.isNull(tierIDList)) ps.setString(columnIndx++, tierIDList);
 		//if(!DBSUtil.isNull(algoIDList)) ps.setString(columnIndx++, algoIDList);
 		//if(!DBSUtil.isNull(fileList)) ps.setString(columnIndx++, fileList);
 		for(int i = 0 ; i != lumiRangeList.size(); ++i) {
 			String tmp[] = (String[])lumiRangeList.get(i);
 			//ps.setString(columnIndx++, (String)lumiRangeList.get(i));
 			ps.setString(columnIndx++, tmp[0]);
 			ps.setString(columnIndx++, tmp[1]);
 		}
 		for(int i = 0 ; i != runRangeList.size(); ++i) {
 			String tmp[] = (String[])runRangeList.get(i);
 			ps.setString(columnIndx++, tmp[0]);
 			ps.setString(columnIndx++, tmp[1]);
 			//ps.setString(columnIndx++, (String)runRangeList.get(i));
 		}
 		//if(!DBSUtil.isNull(adsList)) ps.setString(columnIndx++, adsList);
 		for(int i = 0 ; i != adsList.size(); ++i) ps.setString(columnIndx++, (String)adsList.get(i));
 		System.out.println("The SQL query is " + ps);
                 DBSUtil.writeLog("\n\n" + ps + "\n\n");
 		return ps;
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
 
 	public static PreparedStatement deleteMap(Connection conn, String tableName, String key1, String key2, String value1, String value2) throws SQLException {	
 		String sql = "DELETE FROM \n" +
 			tableName + "\n" +
 			"WHERE \n" +
 			key1 + " like ?\n" +
 			"AND " + key2 + " like ?\n";
 		PreparedStatement ps = DBManagement.getStatement(conn, sql);
 		int columnIndx = 1;
 		ps.setString(columnIndx++, value1);
 		ps.setString(columnIndx++, value2);
 		DBSUtil.writeLog("\n\n" + ps + "\n\n");
 		return ps;
 	}
 
 	//FIXME Just use this and delete all other getBlockIds
 	public static PreparedStatement getBlock(Connection conn, String procDSID,  String blockName) throws SQLException {
 		String sql = "SELECT DISTINCT b.ID as ID, \n " +
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
                 String sql = "SELECT DISTINCT blk.ID as ID, \n" +
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
                 String sql = "SELECT DISTINCT b.ID as ID, \n" +
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
 	
 	
         public static PreparedStatement listRowsInTable(Connection conn, String tableName, String from, String rows) 
         throws SQLException
         {
                 String sql = "SELECT * \n"+
                                 "FROM "+tableName ;
                 if (!DBSUtil.isNull(from) || !rows.equals("*") ) {
                         sql +=  " WHERE\n" ;
                         sql +=  " ID between ? and ? \n";
                 }
                 PreparedStatement ps = DBManagement.getStatement(conn, sql);
                 if (!DBSUtil.isNull(from) || !rows.equals("*") ) {
 
                         Integer iFrom = Integer.parseInt(from);
                         Integer iRows = Integer.parseInt(rows);
 
                         String to = String.valueOf(iFrom.intValue() + iRows.intValue() - 1);
 
                         ps.setString(1, from);
                         ps.setString(2, to);
 
                 }
                 DBSUtil.writeLog("\n\n" + ps + "\n\n");
               return ps;
         }
 
 	
         public static PreparedStatement listLumiIDsForRun(Connection conn, String runNumber)
         throws SQLException
         {
               //We can expand this query if we need, for now we don't !
               String sql = "SELECT DISTINCT ls.id as ID \n "+
                            "FROM LumiSection ls \n"+
                            "WHERE ls.RunNumber = ? \n";
               PreparedStatement ps = DBManagement.getStatement(conn, sql);
               ps.setString(1, runNumber);
               DBSUtil.writeLog("\n\n" + ps + "\n\n");
               return ps;
         }
 
         public static PreparedStatement listLumiSections(Connection conn, String procDSID) throws SQLException {
 		//FIXME We can expand this query if we need, for now we don't !
 		String sql = "SELECT DISTINCT ls.id as ID, \n " +
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
 		String sql = "SELECT DISTINCT pd.ID as ID, \n" +
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
                         //Could be managed by Status field
 			//"procds.OpenForWriting as OPEN_FOR_WRITING, \n" +
 			"pds.Status as STATUS, \n" +
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
 			"LEFT OUTER JOIN ProcDSStatus pds \n" +
 				"ON pds.id = procds.Status \n" +
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
 		//if(!patternPrim.equals("%") || !patternDT.equals("%") || !patternProc.equals("%") || !patternVer.equals("%") || !patternFam.equals("%") || !patternExe.equals("%") || !patternPS.equals("%")) {
 			sql += "WHERE \n";
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
 			useAnd = true;
 		}
 
 
 		if(useAnd) sql += " AND ";
 		sql +=	"pds.Status <> 'INVALID' \n" +
 			"ORDER BY id, APP_VERSION, APP_FAMILY_NAME, APP_EXECUTABLE_NAME, PS_NAME, DATA_TIER DESC";
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
 		String sql = "SELECT DISTINCT procds.id as id, \n" +
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
 			"JOIN ProcDSParent dp \n" +
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
 			"and ps.Hash like ? \n" +
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
 		String sql = "SELECT DISTINCT run.ID as ID, \n " +
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
 		String sql = "SELECT DISTINCT dt.ID as ID, \n " +
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
 		String sql = "SELECT DISTINCT b.ID as ID, \n " +
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
 		if(!DBSUtil.isNull(procDSID)) {
 			sql += "WHERE \n";
 		} else if(!seName.equals("%") || !blockName.equals("%") ){//Assumming seName will never be null
 			 sql += "WHERE \n";
 		}
 		if(!DBSUtil.isNull(procDSID)) {
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
 		if(!DBSUtil.isNull(procDSID)) ps.setString(columnIndx++, procDSID);
 		if(!blockName.equals("%")) ps.setString(columnIndx++, blockName);
 		if(!seName.equals("%")) ps.setString(columnIndx++, seName);
 		
 		DBSUtil.writeLog("\n\n" + ps + "\n\n");
 		return ps;
 	}
 
 	public static PreparedStatement listStorageElements(Connection conn, String seName) throws SQLException {
 		String sql = "SELECT DISTINCT se.ID as ID, \n " +
 			"se.SEName as STORAGE_ELEMENT_NAME, \n" +
 			"se.CreationDate as CREATION_DATE, \n" +
 			"se.LastModificationDate as LAST_MODIFICATION_DATE, \n" +
 			"percb.DistinguishedName as CREATED_BY, \n" +
 			"perlm.DistinguishedName as LAST_MODIFIED_BY \n" +
 			"FROM StorageElement se \n" +
 			"LEFT OUTER JOIN Person percb \n" +
 				"ON percb.id = se.CreatedBy \n" +
 			"LEFT OUTER JOIN Person perlm \n" +
 				"ON perlm.id = se.LastModifiedBy \n";
 
 		if(!seName.equals("%")){
 			 sql += "WHERE \n" +
 			 	"se.SEName like ? \n";
 		}
 		sql +=	"ORDER BY STORAGE_ELEMENT_NAME DESC";
                 int columnIndx = 1;
 		PreparedStatement ps = DBManagement.getStatement(conn, sql);
 		if(!seName.equals("%")) ps.setString(columnIndx++, seName);
 		
 		DBSUtil.writeLog("\n\n" + ps + "\n\n");
 		return ps;
 	}
 
 	//public static PreparedStatement listFiles(Connection conn, String procDSID, String blockID, String tierID, String patternLFN) throws SQLException {
 	public static PreparedStatement listFiles(Connection conn, String procDSID, String aDSID, String blockID, String tierID, String patternLFN) throws SQLException {
 		String joinStr = "";
 		if(!DBSUtil.isNull(aDSID)) {
 			joinStr = "JOIN AnalysisDSFileLumi adfl \n" +
 				"ON adfl.fileid = f.ID \n";
 		}
 			
 		//System.out.println("Block ID is "+blockID);
 		String sql = "SELECT DISTINCT f.ID as ID, \n " +
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
 			//"dt.Name as DATA_TIER, \n"+ 
 			"percb.DistinguishedName as CREATED_BY, \n" +
 			"perlm.DistinguishedName as LAST_MODIFIED_BY \n" +
 			"FROM Files f \n" +
 			"LEFT OUTER JOIN Block b \n" +
 				"ON b.id = f.Block \n "+  
 			"LEFT OUTER JOIN FileTier fdt \n" +
 				"ON fdt.Fileid = f.id \n" +
 			joinStr +
 			//"LEFT OUTER JOIN DataTier dt \n" +
 			//	"ON dt.id = fdt.DataTier " +
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
 		if(!DBSUtil.isNull(procDSID)){
 			sql += "and f.Dataset = ? \n";
 		}
 		if(!DBSUtil.isNull(blockID)){
 			sql += "and f.Block = ? \n";
 		}
 		if(!DBSUtil.isNull(tierID)){
 			sql += "and fdt.DataTier = ? \n";
 		}
 		if(!DBSUtil.isNull(aDSID)) {
 			sql += "and adfl.AnalysisDataset = ? \n";
 		}
 
 		sql +=	"and st.Status <> 'INVALID' \n" +
 			"ORDER BY LFN DESC";
 		PreparedStatement ps = DBManagement.getStatement(conn, sql);
                 
                 int columnIndx=1;
   
 		ps.setString(columnIndx++, patternLFN);
 		if(!DBSUtil.isNull(procDSID)){
 			ps.setString(columnIndx++, procDSID);
 		}
 		if(!DBSUtil.isNull(blockID)){
 			ps.setString(columnIndx++, blockID);
 		}
 		if(!DBSUtil.isNull(tierID)){
 			ps.setString(columnIndx++, tierID);
 		}
 		if(!DBSUtil.isNull(aDSID)) {
 			ps.setString(columnIndx++, aDSID);
 		}
 		
                 DBSUtil.writeLog("\n\n" + ps + "\n\n");
 		return ps;
 	}
 
 	//public static PreparedStatement listFileParents(Connection conn, String fileID) throws SQLException {
 	public static PreparedStatement listFileProvenence(Connection conn, String fileID, boolean parentOrChild) throws SQLException {
 		//parentOrChild if true means we need to get the parents of the file 
 		//parentOrChild if false means we need to get the childern of the file
 		String joinStr = "";
 		String whereStr = "";
 		if (parentOrChild) {
 			joinStr = "ON fp.ItsParent = f.ID \n";
 			whereStr = "WHERE fp.ThisFile = ? \n";
 		} else {
 			joinStr = "ON fp.ThisFile = f.ID \n";
 			whereStr = "WHERE fp.ItsParent = ? \n";
 		}
 			
 		String sql = "SELECT DISTINCT f.ID as ID, \n " +
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
 				joinStr +
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
 	
 		if(!DBSUtil.isNull(fileID)) {
 			sql += whereStr;
 		}
 		sql +=	"ORDER BY LFN DESC";
 		
 		PreparedStatement ps = DBManagement.getStatement(conn, sql);
 		if(!DBSUtil.isNull(fileID)) {
 			ps.setString(1, fileID);
 		}
                 DBSUtil.writeLog("\n\n" + ps + "\n\n");
 		return ps;
 	}
 
 	public static PreparedStatement listFileTiers(Connection conn, String fileID) throws SQLException {
 		String sql = "SELECT DISTINCT dt.ID as ID, \n " +
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
 
         public static PreparedStatement listFileBranches(Connection conn, String fileID) throws SQLException {
                 String sql = "SELECT DISTINCT br.ID as ID, \n " +
                         "br.Name as NAME, \n" +
                         "br.CreationDate as CREATION_DATE, \n" +
                         "br.LastModificationDate as LAST_MODIFICATION_DATE, \n" +
                         "percb.DistinguishedName as CREATED_BY, \n" +
                         "perlm.DistinguishedName as LAST_MODIFIED_BY \n" +
                         "FROM Branch br \n" +
                         "JOIN FileBranch fb \n" +
                                 "ON fb.Branch = br.id \n" +
                         "LEFT OUTER JOIN Person percb \n" +
                                 "ON percb.id = br.CreatedBy \n" +
                         "LEFT OUTER JOIN Person perlm \n" +
                                 "ON perlm.id = br.LastModifiedBy \n";
                 if(fileID != null) {
                         sql += "WHERE fb.Fileid = ? \n";
                 }
 
                 PreparedStatement ps = DBManagement.getStatement(conn, sql);
                 if(fileID != null) {
                         ps.setString(1, fileID);
                 }
                 DBSUtil.writeLog("\n\n" + ps + "\n\n");
                 return ps;
         }
 
 	public static PreparedStatement listFileAlgorithms(Connection conn, String fileID) throws SQLException {
 		String sql = "SELECT DISTINCT algo.id as ID, \n" +
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
 		String sql = "SELECT DISTINCT lumi.id as ID, \n" +
 			"lumi.LumiSectionNumber as LUMI_SECTION_NUMBER, \n" +
 			"r.RunNumber as RUN_NUMBER, \n" +
 			"lumi.StartEventNumber as START_EVENT_NUMBER, \n" +
 			"lumi.EndEventNumber as END_EVENT_NUMBER, \n" +
 			"lumi.LumiStartTime as LUMI_START_TIME, \n" +
 			"lumi.LumiEndTime as LUMI_END_TIME, \n" +
 			"lumi.CreationDate as CREATION_DATE, \n" +
 			"lumi.LastModificationDate as LAST_MODIFICATION_DATE, \n" +
 			"percb.DistinguishedName as CREATED_BY, \n" +
 			"perlm.DistinguishedName as LAST_MODIFIED_BY \n" +
 			"FROM LumiSection lumi \n" +
 			"JOIN FileRunLumi fl \n" +
 				"ON fl.Lumi = lumi.id \n" +
 			"JOIN Runs r \n" +
 				"ON r.ID = fl.Run \n" +
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
 
         public static PreparedStatement listFileRuns(Connection conn, String fileID) throws SQLException {
                 String sql = "SELECT DISTINCT run.id as ID, \n" +
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
                         "JOIN FileRunLumi fl \n" +
                                 "ON run.ID = fl.Run \n" +
                         "LEFT OUTER JOIN Person percb \n" +
                                 "ON percb.id = run.CreatedBy \n" +
                         "LEFT OUTER JOIN Person perlm \n" +
                                 "ON perlm.id = run.LastModifiedBy \n";
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
 
 	public static PreparedStatement listAnalysisDatasetDefinition(Connection conn, String patternName) throws SQLException {
 		//String sql = "SELECT DISTINCT adsdef.ID as ID, \n " +
 		String sql = "SELECT adsdef.ID as ID, \n " +
 			"adsdef.Name as ANALYSIS_DATASET_DEF_NAME, \n" +
 			"adsdef.LumiSections as LUMI_SECTIONS, \n" +
 			"adsdef.LumiSectionRanges as LUMI_SECTION_RANGES, \n" +
 			"adsdef.Runs as RUNS, \n" +
 			"adsdef.RunsRanges as RUNS_RANGES, \n" +
 			"adsdef.Algorithms as ALGORITHMS, \n" +
 			"adsdef.LFNs as LFNS, \n" +
 			"adsdef.Path as PATH, \n" +
 			"adsdef.Tiers as TIERS, \n" +
 			"adsdef.AnalysisDatasets as ANALYSIS_DATASET_NAMES, \n" +
 			"adsdef.UserCut as USER_CUT, \n" +
 			"adsdef.Description as DESCRIPTION, \n" +
 			"adsdef.CreationDate as CREATION_DATE, \n" +
 			"adsdef.LastModificationDate as LAST_MODIFICATION_DATE, \n" +
 			"percb.DistinguishedName as CREATED_BY, \n" +
 			"perlm.DistinguishedName as LAST_MODIFIED_BY \n" +
 			"FROM AnalysisDSDef adsdef \n" +
 			"LEFT OUTER JOIN Person percb \n" +
 				"ON percb.id = adsdef.CreatedBy \n" +
 			"LEFT OUTER JOIN Person perlm \n" +
 				"ON perlm.id = adsdef.LastModifiedBy \n" +
 			"WHERE adsdef.Name like  ? \n" +
 				"ORDER BY ANALYSIS_DATASET_DEF_NAME DESC";
 		
 		PreparedStatement ps = DBManagement.getStatement(conn, sql);
 		ps.setString(1, patternName);
                 DBSUtil.writeLog("\n\n" + ps + "\n\n");
 		return ps;
 	}
 
 
 
       public static PreparedStatement listAnalysisDataset(Connection conn, String patternName, String procDSID) throws SQLException {
                 //String sql = "SELECT DISTINCT ads.ID as ID, \n " +
                 String sql = "SELECT ads.ID as ID, \n " +
 			"ads.Name as ANALYSIS_DATASET_NAME, \n " +
 			"ads.Annotation as ANNOTATION, \n" +    
 			"ads.ProcessedDS as PROCDSID, \n" +  
 			"ads.Definition as DEFID, \n" +  
 			"ads.Type as TYPE, \n" +  
 			"ads.Status as STATUS, \n" +  
 			//"Parent as PARENT, \n" +  
                         "ads.CreationDate as CREATION_DATE, \n" +
                         "ads.LastModificationDate as LAST_MODIFICATION_DATE, \n" +
                         "percb.DistinguishedName as CREATED_BY, \n" +
                         "perlm.DistinguishedName as LAST_MODIFIED_BY, \n" +
 
 			"adsdef.ID as ADDID, \n" +
                         "adsdef.Name as ANALYSIS_DATASET_DEF_NAME, \n" +
                         "adsdef.LumiSections as LUMI_SECTIONS, \n" +
                         "adsdef.LumiSectionRanges as LUMI_SECTION_RANGES, \n" +
                         "adsdef.Runs as RUNS, \n" +
                         "adsdef.RunsRanges as RUNS_RANGES, \n" +
                         "adsdef.Algorithms as ALGORITHMS, \n" +
                         "adsdef.LFNs as LFNS, \n" +
                         "adsdef.Path as PATH, \n" +
                         "adsdef.Tiers as TIERS, \n" +
                         "adsdef.AnalysisDatasets as ANALYSIS_DATASET_NAMES, \n" +
                         "adsdef.UserCut as USER_CUT, \n" +
                         "adsdef.Description as ADD_DESCRIPTION, \n" +
 
                         "adsdef.CreationDate as ADD_CREATION_DATE, \n" +
                         "adsdef.LastModificationDate as ADD_LAST_MODIFICATION_DATE, \n" +
                         "adsdefpercb.DistinguishedName as ADD_CREATED_BY, \n" +
                         "adsdefperlm.DistinguishedName as ADD_LAST_MODIFIED_BY \n" +
 
                         "FROM AnalysisDataset ads \n" +
 
                         "JOIN AnalysisDSDef adsdef \n"+
                                 "ON adsdef.ID = ads.Definition \n"+
 
                         "LEFT OUTER JOIN Person adsdefpercb \n" +
                                 "ON adsdefpercb.id = adsdef.CreatedBy \n" +
                         "LEFT OUTER JOIN Person adsdefperlm \n" +
                                 "ON adsdefperlm.id = adsdef.LastModifiedBy \n" +
 
                         "LEFT OUTER JOIN Person percb \n" +
                                 "ON percb.id = ads.CreatedBy \n" +
                         "LEFT OUTER JOIN Person perlm \n" +
                                 "ON perlm.id = ads.LastModifiedBy \n" +
                         "WHERE ads.Name like ? \n";
 			if (! DBSUtil.isNull(procDSID)) {
 				sql += "AND ProcessedDS = ? \n";
 			}
                 sql += "ORDER BY ads.NAME DESC";
                 PreparedStatement ps = DBManagement.getStatement(conn, sql);
                 int columnIndx = 1;
                 ps.setString(columnIndx++, patternName);
                 if (! DBSUtil.isNull(procDSID)) {
 			ps.setString(columnIndx++, procDSID);
 		}
                 DBSUtil.writeLog("\n\n" + ps + "\n\n");
                 return ps;
 	}
 
 
 	
 	public static PreparedStatement getID(Connection conn, String table, String key, String value) throws SQLException {
 		String sql = "SELECT DISTINCT ID \n " +
 			"FROM " + table + "\n " +
 			"WHERE " + key + " = ? \n";
 		PreparedStatement ps = DBManagement.getStatement(conn, sql);
 		ps.setString(1, value);
                 DBSUtil.writeLog("\n\n" + ps + "\n\n");
 		//return ((String)("SELECT ID AS id FROM " + table + " WHERE " + key + " = '" + value + "'")); 
 		return ps;
 	}
 
 	public static PreparedStatement getMapID(Connection conn, String table, String key1, String key2, String value1, String value2) throws SQLException {
 		String sql = "SELECT DISTINCT ID \n " +
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
 	
 	public static PreparedStatement getMapID(Connection conn, String table, String key1, String key2, String key3, String value1, String value2, String value3) throws SQLException {
 		String sql = "SELECT DISTINCT ID \n " +
 			"FROM " + table + "\n " +
 			"WHERE " + key1 + " = ? \n" +
 			"AND " + key2 + " = ? \n" +
 			"AND " + key3 + " = ? \n" ;
 		PreparedStatement ps = DBManagement.getStatement(conn, sql);
                 int columnIndx = 1;
 		ps.setString(columnIndx++, value1);
 		ps.setString(columnIndx++, value2);
 		ps.setString(columnIndx++, value3);
                 DBSUtil.writeLog("\n\n" + ps + "\n\n");
 		return ps;
 	}
 
 	public static PreparedStatement getProcessedDSID(Connection conn, String prim, String proc) throws SQLException {
 		String sql = "SELECT DISTINCT procds.ID as ID \n" +
 				"FROM ProcessedDataset procds \n" +
 				"JOIN PrimaryDataset primds \n" +
 					"ON primds.id = procds.PrimaryDataset \n" ;
 		if(DBSUtil.isNull(prim) || DBSUtil.isNull(proc)) {
 			return DBManagement.getStatement(conn, sql);
 		}
 		sql += "WHERE primds.Name = ? \n" +
 			"and procds.Name = ? \n";
 		PreparedStatement ps = DBManagement.getStatement(conn, sql);
                 int columnIndx = 1; 
 		ps.setString(columnIndx++, prim);
 		ps.setString(columnIndx++, proc);
                 DBSUtil.writeLog("\n\n" + ps + "\n\n");
 		return ps;
 	}
 
 	/*public static PreparedStatement getProcessedDSID(Connection conn, String prim, String dt ,String proc) throws SQLException {
 		String sql = "SELECT DISTINCT procds.ID as ID \n" +
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
 	}*/
 
 
 	//public static PreparedStatement getAlgorithmID(Connection conn, String ver, String fam, String exe, String psName) throws SQLException {
 	public static PreparedStatement getAlgorithmID(Connection conn, String ver, String fam, String exe, String psHash) throws SQLException {
 		String sql = "SELECT DISTINCT algo.id \n" +
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
 			"and ps.Hash = ? \n" ;
 		PreparedStatement ps = DBManagement.getStatement(conn, sql);
                 int columnIndx = 1;
 		ps.setString(columnIndx++, ver);
 		ps.setString(columnIndx++, fam);
 		ps.setString(columnIndx++, exe);
 		ps.setString(columnIndx++, psHash);
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
