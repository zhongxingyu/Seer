 
 package eionet.meta;
 
 import java.sql.*;
 import java.util.*;
 import javax.servlet.ServletContext;
 
 import com.tee.util.*;
 import com.tee.xmlserver.AppUserIF;
 
 import eionet.meta.savers.*;
 
 import com.tee.uit.security.*;
 import eionet.util.SecurityUtil;
 
 public class VersionManager{
     
     private Connection conn = null;
     private DDSearchEngine searchEngine = null;
     private AppUserIF user = null;
     private boolean upwardsVersioning = false;
 	private boolean versionUpdate = false;
     
     /** possible registration statuses*/
     private Vector regStatuses = new Vector();
     
 	/** servlet context object if instatiated from servlet environment*/
 	private ServletContext ctx = null;
     
     /**
     *
     */
     public VersionManager(Connection conn, AppUserIF user){
         this(conn, new DDSearchEngine(conn), user);
     }
     
     /**
     *
     */
     public VersionManager(Connection conn, DDSearchEngine searchEngine,
                                 AppUserIF user){
         this();
         this.conn = conn;
         this.user = user;
         this.searchEngine = searchEngine;
     }
     
     /**
     *
     */
     public VersionManager(){
         
 		// init registration statuses vector
 		regStatuses.add("Incomplete");
 		regStatuses.add("Candidate");
 		regStatuses.add("Recorded");
 		regStatuses.add("Qualified");
 		regStatuses.add("Released");
     }
     
     public void setUpwardsVersioning(boolean f){
     	this.upwardsVersioning = f;
     }
     
     /**
      * See if the specified object type has the specified short name
      * in the specified namespace (or dataset if object type is "tbl")
      * checked out.
      * If yes then return the name of the user who checked it out.
      * Otherwise return null.
      * 
      * @param   type    object type ("elm", "tbl" or "dst")
      * @param   ctxID    namespace id if type=="elm",
      *                   dataset id if type=="tbl",
      *                   ignored if type=="dst"
      * @param   shortName
      *
      * @return  the name of the working user or null if it's missing
      * @exception   SQLException
      */
     public String getWorkingUser(String ctxID, String idfier, String type)
         throws SQLException {
         
         String tblName = null;
         if (type.equals("elm"))
             tblName = "DATAELEM";
         else if (type.equals("tbl"))
             tblName = "DS_TABLE";
         else if (type.equals("dst"))
             tblName = "DATASET";
         else
             throw new SQLException("Unknown type");
         
         String ctxField = null;
         if (type.equals("elm") || type.equals("tbl"))
             ctxField = "PARENT_NS";
         
         String qry = "select distinct WORKING_USER, VERSION from " + tblName +
         " where IDENTIFIER='" + idfier + "'";
         if (ctxField!=null && ctxID!=null)
             qry = qry + " and " + ctxField + "=" + ctxID;
         
         qry = qry + " and " + tblName + ".WORKING_COPY='Y'";
         qry = qry + " order by VERSION desc";
 
         ResultSet rs = conn.createStatement().executeQuery(qry);
         if (rs.next())
             return rs.getString("WORKING_USER");
         else
             return null;
     }
     
     /**
     *
     */
     public String getWorkingUser(String nsID) throws SQLException {
         
         if (nsID==null)
             return null;
         
         String s =
         "select WORKING_USER from NAMESPACE where NAMESPACE_ID=" + nsID;
         
         ResultSet rs = conn.createStatement().executeQuery(s);
         if (rs.next())
             return rs.getString(1);
         else
             return null;
     }
     
     /**
     *
     */
     public String getTblWorkingUser(String idfier, String parentNs)
         throws SQLException {
             
         String q =
         "select distinct DS_TABLE.WORKING_USER from DS_TABLE " +
         "where IDENTIFIER=" + com.tee.util.Util.strLiteral(idfier) +
         " and WORKING_COPY='Y'";
         
         if (parentNs!=null)
             q = q + " and PARENT_NS=" + parentNs;
         else
             q = q + " and PARENT_NS is null";
         
         ResultSet rs = conn.createStatement().executeQuery(q);
         if (rs.next())
             return rs.getString("WORKING_USER");
         else
             return null;
     }
     
     /**
     *
     */
     public String getDstWorkingUser(String idfier)
         throws SQLException {
         
         String q = "select distinct DATASET.WORKING_USER from DATASET " +
         "where DATASET.IDENTIFIER='" + idfier + "' and " +
         "DATASET.WORKING_COPY='Y'";
         
         ResultSet rs = conn.createStatement().executeQuery(q);
         if (rs.next())
             return rs.getString("WORKING_USER");
         else
             return null;
     }
     
     /**
     *
     */
     public String getWorkingCopyID(DataElement elm) throws SQLException {
     	
     	String parentNsID = elm.getNamespace()==null ? null : elm.getNamespace().getID();
     	
     	StringBuffer buf = new StringBuffer().
     	append("select DATAELEM_ID from DATAELEM where WORKING_COPY='Y' and IDENTIFIER=").
     	append(Util.strLiteral(elm.getIdentifier()));
     	if (parentNsID!=null) buf.append(" and PARENT_NS=").append(parentNsID);
     	
         ResultSet rs = conn.createStatement().executeQuery(buf.toString());
         if (rs.next())
         	return rs.getString(1);
         else
         	return null;
     }
     
     /**
     *
     */
     public String getWorkingCopyID(DsTable tbl) throws SQLException {
         
         String q =
         "select distinct TABLE_ID from DS_TABLE " +
         "where WORKING_COPY='Y' and " +
         "IDENTIFIER='" + tbl.getIdentifier() + "' and " +
         "PARENT_NS=" + tbl.getParentNs();
         
         ResultSet rs = conn.createStatement().executeQuery(q);
         if (rs.next()){
             return rs.getString(1);
         }
         
         return null;
     }
     
     /**
     *
     */
     public String getWorkingCopyID(Dataset dst) throws SQLException {
         String q =
         "select distinct DATASET_ID from DATASET " +
         "where WORKING_COPY='Y' and " +
         "IDENTIFIER='" + dst.getIdentifier() + "'";
         
         ResultSet rs = conn.createStatement().executeQuery(q);
         if (rs.next()){
             return rs.getString(1);
         }
         
         return null;
     }
     
     /**
      * Check out the specified object. Meaning a working copy of the
      * object will be created.
      * 
      * @param   id    object id.
      * @param   type  object type (one of "elm", "tbl" or "dst")
      * @return  id of the working copy
      * @exception   Exception
      */
     public String checkOut(String id, String type) throws Exception{
     	
         if (id==null || type==null)
             throw
             new Exception("Unable to locate the object to check out!");
             
         if (type.equals("elm"))
             return checkOutElm(id);
         else if (type.equals("tbl"))
             return checkOutTbl(id);
         else if (type.equals("dst"))
             return checkOutDst(id);
         else
             throw new Exception("Unknown object type!");
     }
     
 	private String checkOutElm(String elmID) throws Exception{
 		return checkOutElm(elmID, false);
 	}
     
     /**
      */
     public String checkOutElm(String elmID, boolean elmCommon) throws Exception{
         
         if (user==null || !user.isAuthentic())
             throw new Exception("Check-out attempt by an unauthorized user!");
         
         String userName = user.getUserName();
         
         // set the original's WORKING_USER
         SQLGenerator gen = new SQLGenerator();
         gen.setTable("DATAELEM");
         gen.setField("WORKING_USER", userName);
         String q = gen.updateStatement() + " where DATAELEM_ID=" + elmID;
         Statement stmt = conn.createStatement();
         stmt.executeUpdate(q);
         
         // set the WORKING_USER of top namespace
         q = "select TOP_NS from DATAELEM where DATAELEM_ID=" + elmID;
         ResultSet rs = stmt.executeQuery(q);
         String topNS = rs.next() ? rs.getString(1) : null;
         if (topNS!=null){
             gen.clear();
             gen.setTable("NAMESPACE");
             gen.setField("WORKING_USER", userName);
             q = gen.updateStatement() + " where NAMESPACE_ID=" + topNS;
             stmt.executeUpdate(q);
         }
         
 		// copy element
 		CopyHandler copyHandler = new CopyHandler(conn);
 		copyHandler.setUser(user);
 		String newID = copyHandler.copyElem(elmID, true, !elmCommon);
         
         return newID;
     }
     
     /**
      * Check out the specified table.
      * 
      * @param   tblID    table id.
      * @return  id of the working copy
      * @exception   Exception
      */
     private String checkOutTbl(String tblID) throws Exception{
         
         if (user==null || !user.isAuthentic())
             throw new Exception("Check-out attempt by an unauthorized user!");
         
         // set the original's WORKING_USER
         SQLGenerator gen = new SQLGenerator();
         gen.setTable("DS_TABLE");
         gen.setField("WORKING_USER", user.getUserName());
         String q = gen.updateStatement() + " where TABLE_ID=" + tblID;
         Statement stmt = conn.createStatement();
         stmt.executeUpdate(q);
         
         // set the WORKING_USER of top namespace
         q = "select PARENT_NS from DS_TABLE where TABLE_ID=" + tblID;
         ResultSet rs = stmt.executeQuery(q);
         String topNS = rs.next() ? rs.getString(1) : null;
         if (topNS!=null){
             gen.clear();
             gen.setTable("NAMESPACE");
             gen.setField("WORKING_USER", user.getUserName());
             q = gen.updateStatement() + " where NAMESPACE_ID=" + topNS;
             stmt.executeUpdate(q);
         }
         
         // copy table
         CopyHandler copyHandler = new CopyHandler(conn);
         copyHandler.setUser(user);
         String newID = copyHandler.copyTbl(tblID, true, true);
         
         return newID;
     }
     
     /**
      * Check out the specified dataset.
      * 
      * @param   dstID    dataset id.
      * @return  id of the working copy
      * @exception   Exception
      */
     private String checkOutDst(String dstID) throws Exception{
     	
     	if (dstID==null) throw new Exception("Dataset ID missing!"); 
     	
 		// set the working user of the original
 		SQLGenerator gen = new SQLGenerator();
 		gen.setTable("DATASET");
 		gen.setField("WORKING_USER", user.getUserName());
 		conn.createStatement().executeUpdate(gen.updateStatement() +
 					" where DATASET_ID=" + dstID);
 		
 		// set the WORKING_USER of top namespace
         String q = "select CORRESP_NS from DATASET where DATASET_ID=" + dstID;
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(q);
         String topNS = rs.next() ? rs.getString(1) : null;
         if (topNS!=null){
             gen.clear();
             gen.setTable("NAMESPACE");
             gen.setField("WORKING_USER", user.getUserName());
             q = gen.updateStatement() + " where NAMESPACE_ID=" + topNS;
             stmt.executeUpdate(q);
         }
         
 		// copy the dataset
 		CopyHandler copyHandler = new CopyHandler(conn);
 		copyHandler.setUser(user);
 		String newID = copyHandler.copyDst(dstID, true, true, false);
 		
 		return newID;
     }
     
     /**
      * Check in the specified object.
      *
      * @param   objID    object id
      * @param   objType    object type ("elm", "tbl" or "dst")
      * @param   status  registration status where the user wants
      *                  the checked-in object to be. The method
      *                  checks if the user has filled all the
      *                  requirements of that status. If not, an
      *                  exception with a proper message is thrown.
      * @exception   Exception
      */
     public boolean checkIn(String objID, String objType, String status)
         throws Exception {
             
         if (objID==null || objType==null)
             throw
             new Exception("Unable to locate the object to check out!");
 
 		// JH 031203
 		// check if this is really a working copy.
 		// it might not be if the user has tried to cancel a previous
 		// check-in with the browser's STOP button
 		
 		if (!searchEngine.isWorkingCopy(objID, objType))
 			throw new Exception("Trying to check in a non-working copy!");
 		
         if (objType.equals("elm"))
             return checkInElm(objID, status);
         else if (objType.equals("tbl"))
             return checkInTbl(objID, status);
         else if (objType.equals("dst"))
             return checkInDst(objID, status);
         else
             throw new Exception("Unknown object type!");
     }
 
 	/**
 	 * Check in the specified element.
 	 */
 	private boolean checkInElm(String elmID, String status) throws Exception{
 		return checkInElm(elmID, status, false);
 	}
     
     /**
      * Check in the specified element.
      */
     public boolean checkInElm(String elmID, String status, boolean elmCommon) throws Exception{
         
         // load the element we need to check in
         DataElement elm = loadElm(elmID);
         
         // check the requirements for checking in a data element
         checkRequirements(elm, status);
         
         String newVersion = null;
         DataElement latestElm = null;
         
 		String latestID = getLatestElmID(elm);
 		if (latestID==null || latestID.equals(elm.getID())) // a brand new element
 			newVersion = composeNewVersion(null);
 		else
 			latestElm = loadElm(latestID);
         
 		if (newVersion==null)
 		newVersion = versionUpdate ? composeNewVersion(elm.getVersion()) : elm.getVersion();
         
         // update from working copy to acting copy
         SQLGenerator gen = new SQLGenerator();
         gen.setTable("DATAELEM");
         gen.setField("WORKING_COPY", "N");
         gen.setFieldExpr("WORKING_USER", "NULL");
         gen.setField("VERSION", newVersion);
         
         // set the fields for keeping history info
         gen.setField("USER", user.getUserName());
         gen.setFieldExpr("DATE", String.valueOf(System.currentTimeMillis()));
         
         Statement stmt = conn.createStatement();
         stmt.executeUpdate(gen.updateStatement() +
                                 " where DATAELEM_ID=" + elmID);
         
         // If the version wasn't updated (meaning non-effective attributes were
         // changed), the original has to be deleted, otherwise set
         // to WORKING_USER=NULL.
         // This is no problem in a situation when the user updates status from
         // no-version-requiring to version-requiring, because status is not
         // part of unique ID, but the version however is. It is the new version
         // that creates a new element. In other cases the previous latest has
         // to be deleted. Status only effects if version is updated at all.
         
         if (latestElm!=null){
         	
         	// JH240804 - added !versionUpdate to make sure the previous copy is deleted
             if (!versionUpdate || newVersion.equals(elm.getVersion())){
                 // delete the previous copy                
                 Parameters params = new Parameters();
                 params.addParameterValue("mode", "delete");
                 params.addParameterValue("delem_id", latestElm.getID());
                 DataElementHandler delemHandler =
                     new DataElementHandler(conn, params, ctx);
                 delemHandler.setUser(user);
                 delemHandler.setVersioning(false);
                 delemHandler.execute();
             }
             else{
                 gen.clear();
                 gen.setTable("DATAELEM");
                 gen.setFieldExpr("WORKING_USER", "NULL");
                 stmt.executeUpdate(gen.updateStatement() +
                         " where DATAELEM_ID=" + latestElm.getID());
                 
                 // if this is a common element and we update its version,
                 // we must delete the new version's TBL2ELEM relations
                 // so that the previous copy remains used in tables wherever it was used
                 if (elmCommon)
 					stmt.executeUpdate("delete from TBL2ELEM where DATAELEM_ID=" + elmID);
 
                 // update the parent table
                 versionUpwards(elm, latestElm.getID());
             }
         }
         else
             versionUpwards(elm, null);
         
         // release the top namespace
         String topNS = elm.getTopNs();
         if (topNS!=null){
             gen.clear();
             gen.setTable("NAMESPACE");
             gen.setFieldExpr("WORKING_USER", "NULL");
             stmt.executeUpdate(gen.updateStatement() + " where NAMESPACE_ID=" + topNS);
         }
         
         return true;
     }
     
     /**
      * Check in the specified table.
      */
     private boolean checkInTbl(String tblID, String status) throws Exception{
         
         // load the table we need to check in
         DsTable tbl = loadTbl(tblID);
         
 		// check the requirements for checking in a table
         checkRequirements(tbl, status);
         
         String newVersion = null;
         DsTable latestTbl = null;
         
 		String latestID = getLatestTblID(tbl);
 		if (latestID==null || latestID.equals(tbl.getID()))
 			newVersion = composeNewVersion(null);
 		else
 			latestTbl = loadTbl(latestID);
         
 		if (newVersion==null)
 			newVersion = versionUpdate ?
 						composeNewVersion(tbl.getVersion()) : tbl.getVersion();
         
         // update from working copy to acting copy
         SQLGenerator gen = new SQLGenerator();
         gen.setTable("DS_TABLE");
         gen.setField("WORKING_COPY", "N");
         gen.setFieldExpr("WORKING_USER", "NULL");
         gen.setField("VERSION", newVersion);
         
         // set the fields for keeping history info
         gen.setField("USER", user.getUserName());
         gen.setFieldExpr("DATE", String.valueOf(System.currentTimeMillis()));
         
         Statement stmt = conn.createStatement();
         stmt.executeUpdate(gen.updateStatement() +
                                 " where TABLE_ID=" + tblID);
         
         // If the version wasn't updated (meaning non-effective attributes were
         // changed), the original has to be deleted, otherwise set
         // to WORKING_USER=NULL.
         // This is no problem in a situation when the user updates status from
         // no-version-requiring to version-requiring, because status is not
         // part of unique ID, but the version however is. It is the new version
         // that creates a new element. In other cases the previous latest has
         // to be deleted. Status only effects if version is updated at all.
 
         if (latestTbl!=null){
 			// JH240804 - added !versionUpdate to make sure the previous copy is deleted
             if (!versionUpdate || newVersion.equals(tbl.getVersion())){
                 // delete the previous copy                
                 Parameters params = new Parameters();
                 params.addParameterValue("mode", "delete");
                 params.addParameterValue("del_id", latestTbl.getID());
                 DsTableHandler tblHandler =
                     new DsTableHandler(conn, params, ctx);
                 tblHandler.setUser(user);
                 tblHandler.setVersioning(false);
                 tblHandler.execute();
             }
             else{
                 gen.clear();
                 gen.setTable("DS_TABLE");
                 gen.setFieldExpr("WORKING_USER", "NULL");
                 stmt.executeUpdate(gen.updateStatement() +
                         " where TABLE_ID=" + latestTbl.getID());
 
                 // update the parent dataset
                 versionUpwards(tbl, latestTbl.getID());
             }
         }
         else
             versionUpwards(tbl, null);
         
         // release the top namespace
         String topNS = tbl.getParentNs();
         if (topNS!=null){
             gen.clear();
             gen.setTable("NAMESPACE");
             gen.setFieldExpr("WORKING_USER", "NULL");
             stmt.executeUpdate(gen.updateStatement() +
                     " where NAMESPACE_ID=" + topNS);
         }
         
         return true;
     }
     
     /**
      * Check in the specified dataset.
      */
     private boolean checkInDst(String dstID, String status) throws Exception{
     	
 		// load the table we need to check in
 		Dataset dst = loadDst(dstID);
         
 		// check the requirements for checking in a dataset
 		checkRequirements(dst, status);
         
 		String newVersion = null;
 		Dataset latestDst = null;
         
 		String latestID = getLatestDstID(dst);
 		if (latestID==null || latestID.equals(dst.getID()))
 			newVersion = composeNewVersion(null);
 		else
 			latestDst = loadDst(latestID);
         
 		if (newVersion==null)
 			newVersion = versionUpdate ?
 						composeNewVersion(dst.getVersion()) : dst.getVersion();
         
 		// update from working copy to acting copy
 		SQLGenerator gen = new SQLGenerator();
 		gen.setTable("DATASET");
 		gen.setField("WORKING_COPY", "N");
 		gen.setFieldExpr("WORKING_USER", "NULL");
 		gen.setField("VERSION", newVersion);
         
 		// set the fields for keeping history info
 		gen.setField("USER", user.getUserName());
 		gen.setFieldExpr("DATE", String.valueOf(System.currentTimeMillis()));
         
 		Statement stmt = conn.createStatement();
 		stmt.executeUpdate(gen.updateStatement() +
 								" where DATASET_ID=" + dstID);
         
 		// If the version wasn't updated (meaning non-effective attributes were
 		// changed), the original has to be deleted, otherwise set
 		// to WORKING_USER=NULL.
 		// This is no problem in a situation when the user updates status from
 		// no-version-requiring to version-requiring, because status is not
 		// part of unique ID, but the version however is. It is the new version
 		// that creates a new dataset. In other cases the previous latest has
 		// to be deleted. Status only effects if version is updated at all.
         
 		if (latestDst!=null){
 			
 			// JH240804 - added !versionUpdate to make sure the previous copy is deleted 
 			if (!versionUpdate || newVersion.equals(dst.getVersion())){
 				// delete the previous copy                
 				Parameters params = new Parameters();
 				params.addParameterValue("mode", "delete");
 				params.addParameterValue("complete", "true");
 				params.addParameterValue("ds_id", latestDst.getID());
 				DatasetHandler dstHandler =
 					new DatasetHandler(conn, params, ctx);
 				dstHandler.setUser(user);
 				dstHandler.setVersioning(false);
 				dstHandler.execute();
 			}
 			else{
 				gen.clear();
 				gen.setTable("DATASET");
 				gen.setFieldExpr("WORKING_USER", "NULL");
 				stmt.executeUpdate(gen.updateStatement() +
 						" where DATASET_ID=" + latestDst.getID());
 			}
 		}
 		
 		// release the top namespace
         String topNS = dst.getNamespaceID();
         if (topNS!=null){
             gen.clear();
             gen.setTable("NAMESPACE");
             gen.setFieldExpr("WORKING_USER", "NULL");
             stmt.executeUpdate(gen.updateStatement() +
                     " where NAMESPACE_ID=" + topNS);
         }
 		
 		return true;
     }
     
     private void checkRequirements(DataElement elm, String status)
                                                     throws Exception{        
         // check Submitting Org
         DElemAttribute submOrg = elm.getAttributeByShortName("SubmitOrganisation");
         if (submOrg==null) throw new Exception("SubmitOrganisation complex attribute required!");
     }
     
     /**
      * Check status requirements of the specified table
      */
     private void checkRequirements(DsTable tbl, String status)
                                                     throws Exception{        
         // check Submitting Org
         DElemAttribute submOrg = tbl.getAttributeByShortName("SubmitOrganisation");
 		if (submOrg==null) throw new Exception("SubmitOrganisation complex attribute required!");
     }
     
 	/**
 	 * Check status requirements of the specified table
 	 */
 	private void checkRequirements(Dataset dst, String status)
 													throws Exception{        
 		// check Submitting Org
 		DElemAttribute submOrg = dst.getAttributeByShortName("SubmitOrganisation");
 		if (submOrg==null) throw new Exception("SubmitOrganisation complex attribute required!");
 	}
     
     /**
     *
     */
     private DataElement loadElm(String elmID) throws Exception{
 
         if (Util.nullString(elmID)) throw new Exception("Data element ID not specified!");
         
         // get the element (this will return simple attributes + tableID
         DataElement elem = searchEngine.getDataElement(elmID);
         if (elem == null) throw new Exception("Element not found!");
         
         // get and set the element's complex attributes
         elem.setComplexAttributes(
         searchEngine.getComplexAttributes(elmID,"E",null,elem.getTableID(),elem.getDatasetID()));
 
         // set fixed values
         elem.setFixedValues(searchEngine.getFixedValues(elmID, "elem"));
         
         return elem;
     }
     
     /**
     *
     */
     private DsTable loadTbl(String tblID) throws Exception{
         
         if (Util.nullString(tblID)) throw new Exception("Table ID not specified!");
         
         // get the table
         DsTable dsTable = searchEngine.getDatasetTable(tblID);
         if (dsTable == null) throw new Exception("Table not found!");
             
         // get simple attributes
         Vector v = searchEngine.getSimpleAttributes(tblID, "T");
         dsTable.setSimpleAttributes(v);
         
         // get & set complex attributes
         dsTable.setComplexAttributes(
         searchEngine.getComplexAttributes(tblID, "T", null, null,dsTable.getDatasetID()));
 
         // get data elements (this will also return simple attributes, but no fixed values!)
         dsTable.setElements(searchEngine.getDataElements(null, null, null, null, tblID));
         
         return dsTable;
     }
     
     /**
     *
     */
     private Dataset loadDst(String dstID) throws Exception{
         
         if (Util.nullString(dstID)) throw new Exception("Dataset ID not specified!");
         
         Dataset ds = searchEngine.getDataset(dstID);
         if (ds == null) throw new Exception("Dataset not found!");
             
         // get & set simple attributes, compelx attributes and tables
         ds.setSimpleAttributes(searchEngine.getSimpleAttributes(dstID, "DS"));
         ds.setComplexAttributes(searchEngine.getComplexAttributes(dstID, "DS"));
         ds.setTables(searchEngine.getDatasetTables(dstID));
         
         return ds;
     }
 
     
     /**
     *
     */
     private void versionUpwards(DataElement elm, String latestID)
                 throws Exception{
         
         if (!upwardsVersioning)
         	return;
         
         // create new version of parent table by copying
         // everything from the old version and substituting
         // latestElm with the new elm
         
         String oldTblID = elm.getTableID();
         if (Util.nullString(oldTblID))
             return;
         
         // copy table
         CopyHandler copyHandler = new CopyHandler(conn);
         copyHandler.setUser(user);
         String newTblID = copyHandler.copyTbl(oldTblID, false, true);
         if (Util.nullString(newTblID))
             return;
 
         // set the new tables credentials for history
         SQLGenerator gen = new SQLGenerator();
         gen.setTable("DS_TABLE");
         gen.setFieldExpr("VERSION", "VERSION+1");
         gen.setFieldExpr("DATE", String.valueOf(System.currentTimeMillis()));
         if (user==null)
             gen.setFieldExpr("USER", "NULL");
         else
             gen.setField("USER", user.getUserName());
         
         Statement stmt = conn.createStatement();
         stmt.executeUpdate(gen.updateStatement() +
                 " where TABLE_ID=" + newTblID);
 
 		// check if this is an element restore and if so,
 		// add element with latestID to the new table
         if (elm.getID()==null){
         	if (latestID!=null){
 				gen = new SQLGenerator();
 				gen.setTable("TBL2ELEM");
 				gen.setFieldExpr("DATAELEM_ID", latestID);
 				gen.setFieldExpr("TABLE_ID", newTblID);
 				stmt.executeUpdate(gen.insertStatement());
         	}
         	
         	DsTable tbl = loadTbl(newTblID);
         	if (tbl!=null){
         		String dstID = tbl.getDatasetID();
         		tbl = new DsTable(null, dstID, null);
         		//versionUpwards(tbl,"");
         	}
         	
         	return;
         }
         
         // remove new element from table old version
         stmt.executeUpdate("delete from TBL2ELEM where TABLE_ID=" + oldTblID +
                                 " and DATAELEM_ID=" + elm.getID());
         
         // remove old element from table new version
         // (that is if there ever was an old element, i.e.
         // it's a completely new element)
         if (latestID!=null)
             stmt.executeUpdate("delete from TBL2ELEM where TABLE_ID=" +
                                 newTblID + " and DATAELEM_ID=" + latestID);
                                 
         // update the parent dataset
         versionUpwards(loadTbl(newTblID), oldTblID);
     }
     
     /**
     *
     */
     private void versionUpwards(DsTable tbl, String latestID)
                                                 throws Exception {
 		if (!upwardsVersioning)
 			return;
         
         // create new version of parent dataset by copying
         // everything from the old version and substituting
         // old table in the new dataset with new table
         
         String oldDstID = tbl.getDatasetID();
         if (Util.nullString(oldDstID))
             return;
             
         // copy dataset
         CopyHandler copyHandler = new CopyHandler(conn);
         copyHandler.setUser(user);
         String newDstID = copyHandler.copyDst(oldDstID, false, true, false);
         if (Util.nullString(newDstID))
             return;
 
         // set the new dataset's credentials for history
         SQLGenerator gen = new SQLGenerator();
         gen.clear();
         gen.setTable("DATASET");
         gen.setFieldExpr("DATE", String.valueOf(System.currentTimeMillis()));
 		if (versionUpdate)
 			gen.setFieldExpr("VERSION", "VERSION+1");
         if (user==null)
             gen.setFieldExpr("USER", "NULL");
         else
             gen.setField("USER", user.getUserName());
         
         Statement stmt = conn.createStatement();
         String sql = gen.updateStatement() + " where DATASET_ID=" + newDstID;
         stmt.executeUpdate(sql);
 
 		// remove old table from the new dataset
 		// (that is if there ever was an old table, i.e. it's a completely new table)
 		if (latestID!=null)
 			stmt.executeUpdate("delete from DST2TBL where DATASET_ID=" + newDstID +
 								" and TABLE_ID=" + latestID);
         
         if (versionUpdate){
 	        // remove new table from the dataset's old version
 	        stmt.executeUpdate("delete from DST2TBL where DATASET_ID=" + oldDstID +
 	                                " and TABLE_ID=" + tbl.getID());
         }
         else{
         	// delete old dataset completely
 			Parameters params = new Parameters();
 			params.addParameterValue("mode", "delete");
 			params.addParameterValue("complete", "true");
 			params.addParameterValue("ds_id", oldDstID);
 			DatasetHandler dstHandler = new DatasetHandler(conn, params, ctx);
 			dstHandler.setUser(user);
 			dstHandler.setVersioning(false);
 			dstHandler.execute();
         }
     }
 
 	/**
 	*
 	*/
 	public String deleteElmLinks(String dstID, String tblID, String[] elmlinks) throws Exception{
 		
 		if (dstID==null || tblID==null || elmlinks==null || elmlinks.length==0)
 			return null;
         
 		// make sure that we're doing this in the latest dataset
 		Dataset dst = searchEngine.getDataset(dstID);
 		if (dst==null)
 			return null;
 		else if (!isLatestDst(dst.getID(), dst.getIdentifier()))
 			throw new Exception("Cannot delete in history!");
         
 		// removing a links to common elements means creating a new dataset where these links
 		// are missing in this table. So first create a new table where these links
 		// are missing and then put that table into the new dataset
         
 		// copy the table
 		CopyHandler copyHandler = new CopyHandler(conn);
 		copyHandler.setUser(user);
 		String newTblID = copyHandler.copyTbl(tblID, false, true);
 		if (Util.nullString(newTblID))
 			return null;
 
 		// set the new table's credentials for history
 		SQLGenerator gen = new SQLGenerator();
 		gen.clear();
 		gen.setTable("DS_TABLE");
 		gen.setFieldExpr("DATE", String.valueOf(System.currentTimeMillis()));
 		if (versionUpdate)
 			gen.setFieldExpr("VERSION", "VERSION+1");
 		if (user==null)
 			gen.setFieldExpr("USER", "NULL");
 		else
 			gen.setField("USER", user.getUserName());
         
 		Statement stmt = conn.createStatement();
 		stmt.executeUpdate(gen.updateStatement() + " where TABLE_ID=" + newTblID);
         
 		// remove the element links from the new table
 		StringBuffer buf = new StringBuffer().
 		append("delete from TBL2ELEM where TABLE_ID=").append(newTblID).append(" and (");
 		for (int i=0; i<elmlinks.length; i++){
 			if (i>0) buf.append(" or ");
 			buf.append("DATAELEM_ID=").append(elmlinks[i]);
 		}
 		buf.append(")");
 		stmt.executeUpdate(buf.toString());
         
 		// update the parent dataset
 		versionUpwards(loadTbl(newTblID), tblID);
         
 		return newTblID;
 	}
 
 	/*
 	 * 
 	 */
 	public String deleteElm(String dstID,String dstIdf,String tblID,Vector elms)throws Exception{
 		
 		if (elms==null || elms.size()==0 || dstID==null || tblID==null) return null;
         
 		// make sure that we're doing this in the latest dataset
 		if (!isLatestDst(dstID, dstIdf)) throw new Exception("Cannot delete in history!");
         
 		// deleting elements belonging to a table means creating a new dataset where
 		// these elements are missing in this table. So first create a new table where
 		// these elements are missing and then put that table into the new dataset
         
 		// copy old table
 		CopyHandler copyHandler = new CopyHandler(conn);
 		copyHandler.setUser(user);
 		String newTblID = copyHandler.copyTbl(tblID, false, true);
 		if (Util.nullString(newTblID))
 			return null;
 
 		// set the new table's credentials for history
 		SQLGenerator gen = new SQLGenerator();
 		gen.clear();
 		gen.setTable("DS_TABLE");
 		gen.setFieldExpr("DATE", String.valueOf(System.currentTimeMillis()));
 		if (versionUpdate)
 			gen.setFieldExpr("VERSION", "VERSION+1");
 		if (user==null)
 			gen.setFieldExpr("USER", "NULL");
 		else
 			gen.setField("USER", user.getUserName());
         
 		Statement stmt = conn.createStatement();
 		stmt.executeUpdate(gen.updateStatement() + " where TABLE_ID=" + newTblID);
         
 		// remove the deleted elements from the new table
 		StringBuffer buf = new StringBuffer().
 		append("delete from TBL2ELEM where TABLE_ID=").append(newTblID).append(" and (");
 		for (int i=0; i<elms.size(); i++){
 			if (i>0) buf.append(" or ");
 			buf.append("DATAELEM_ID=").append(elms.get(i));
 		}
 		buf.append(")");
 		stmt.executeUpdate(buf.toString());
         
 		// update the parent dataset
 		versionUpwards(loadTbl(newTblID), tblID);
         
 		return newTblID;
 	}
     
     /**
     *
     */
     public String deleteElm(String elmID) throws Exception{
         
         DataElement elm = loadElm(elmID);
         if (elm==null)
             return null;
         
         // make sure that we're doing this in the latest dataset
         Dataset dst = searchEngine.getDataset(elm.getDatasetID());
         if (dst==null)
         	return null;
         else if (!isLatestDst(dst.getID(), dst.getIdentifier()))
             throw new Exception("Cannot delete in history!");
         
         // Deleting an element means creating a new dataset where this element
         // is missing in this table. So first create a new table where this element
         // is missing and then put that table into the new dataset
         
         String oldTblID = elm.getTableID();
         if (Util.nullString(oldTblID))
             return null;
         
         // copy old table
         CopyHandler copyHandler = new CopyHandler(conn);
         copyHandler.setUser(user);
         String newTblID = copyHandler.copyTbl(oldTblID, false, true);
         if (Util.nullString(newTblID))
             return null;
 
         // set the new table's credentials for history
         SQLGenerator gen = new SQLGenerator();
         gen.clear();
         gen.setTable("DS_TABLE");
         gen.setFieldExpr("DATE", String.valueOf(System.currentTimeMillis()));
 		if (versionUpdate)
 			gen.setFieldExpr("VERSION", "VERSION+1");
         if (user==null)
             gen.setFieldExpr("USER", "NULL");
         else
             gen.setField("USER", user.getUserName());
         
         Statement stmt = conn.createStatement();
         stmt.executeUpdate(gen.updateStatement() + " where TABLE_ID=" + newTblID);
         
         // remove this element from the new table
         stmt.executeUpdate(
 		"delete from TBL2ELEM where TABLE_ID=" + newTblID + " and DATAELEM_ID=" + elmID);
         
         // update the parent dataset
         versionUpwards(loadTbl(newTblID), oldTblID);
         
         return newTblID;
     }
     
     /**
     *
     */
     public String deleteTbl(String tblID) throws Exception{
         
         DsTable tbl = loadTbl(tblID);
         if (tbl==null)
             return null;
         
 		// make sure that we're doing this in the latest dataset
 		Dataset dst = searchEngine.getDataset(tbl.getDatasetID());
 		if (dst==null)
 			return null;
 		else if (!isLatestDst(dst.getID(), dst.getIdentifier()))
 			throw new Exception("Cannot delete in history!");
         
         // deleting a table means creating a new dataset where this table is missing
         // so first we need to copy the old dataset and its relations.
         String oldDstID = tbl.getDatasetID();
         if (Util.nullString(oldDstID)) return null;
         
         CopyHandler copyHandler = new CopyHandler(conn);
         copyHandler.setUser(user);
         String newDstID = copyHandler.copyDst(oldDstID, false,true,false);
         if (Util.nullString(newDstID))
             return null;
 
         // we need to update the new dataset's version
         SQLGenerator gen = new SQLGenerator();
         gen.clear();
         gen.setTable("DATASET");
         gen.setFieldExpr("DATE", String.valueOf(System.currentTimeMillis()));
 		if (versionUpdate)
 			gen.setFieldExpr("VERSION", "VERSION+1");
         if (user==null)
             gen.setFieldExpr("USER", "NULL");
         else
             gen.setField("USER", user.getUserName());
         Statement stmt = conn.createStatement();
         stmt.executeUpdate(gen.updateStatement() +
                 " where DATASET_ID=" + newDstID);
         
         // remove this table from the new dataset
         stmt.executeUpdate("delete from DST2TBL where DATASET_ID=" +
                 newDstID + " and TABLE_ID=" + tblID);
         
         // if this table deletion should NOT create a new version of the dataset,
         // then delete the old dataset and delete it completely
         if (!versionUpdate){
 			Parameters params = new Parameters();
 			params.addParameterValue("mode", "delete");
 			params.addParameterValue("complete", "true");
 			params.addParameterValue("ds_id", oldDstID);
 			DatasetHandler dstHandler = new DatasetHandler(conn, params, ctx);
 			dstHandler.setUser(user);
 			dstHandler.setVersioning(false);
 			dstHandler.execute();
         }
 		
 		return newDstID;
     }
     
     /**
     *
     */
     public String getLatestElmID(DataElement elm) throws SQLException{
     	
     	// see if this is a common element and behave relevantly
 		boolean elmCommon = elm.getNamespace()==null || elm.getNamespace().getID()==null;
     	
         StringBuffer buf = new StringBuffer("select DATAELEM.DATAELEM_ID from DATAELEM");
         if (elm.getNamespace()!=null && elm.getNamespace().getID()!=null){ // non-common element
 			buf.append(", TBL2ELEM, DST2TBL, DATASET ").
 			append("where ").
 			append("DATAELEM.DATAELEM_ID=TBL2ELEM.DATAELEM_ID and ").
 			append("TBL2ELEM.TABLE_ID=DST2TBL.TABLE_ID and ").
 			append("DST2TBL.DATASET_ID=DATASET.DATASET_ID and ").
 			append("DATAELEM.WORKING_COPY='N' and DATAELEM.PARENT_NS=").
 			append(elm.getNamespace().getID()).append(" and DATAELEM.IDENTIFIER=").
 			append(Util.strLiteral(elm.getIdentifier())).
 			append(" and DATASET.DELETED is null order by DATASET.VERSION desc");
         }
         else{
 			buf.append(" where ").
 			append("DATAELEM.WORKING_COPY='N' and DATAELEM.PARENT_NS is null and ").
 			append("DATAELEM.IDENTIFIER=").append(Util.strLiteral(elm.getIdentifier())).
 			append(" order by DATAELEM.VERSION desc");
         }
         
         Statement stmt = null;
 		ResultSet rs = null;
 		try{
 			stmt = conn.createStatement();
 			rs = stmt.executeQuery(buf.toString());
 			if (rs.next()) return rs.getString(1);
 		}
 		finally{
 			try{
 				if (stmt!=null) stmt.close();
 				if (rs!=null) rs.close();
 			}
 			catch (SQLException e){}
 		}
 		
 		return null;
     }
 
 	/**
 	*
 	*/
 	public String getLatestTblID(DsTable tbl) throws SQLException{
         
 		StringBuffer buf = new StringBuffer().
 		append("select DS_TABLE.TABLE_ID from DS_TABLE ").
 		append("left outer join DST2TBL on DS_TABLE.TABLE_ID=DST2TBL.TABLE_ID ").
 		append("left outer join DATASET on DST2TBL.DATASET_ID=DATASET.DATASET_ID ").
 		append("where DS_TABLE.WORKING_COPY='N' and DS_TABLE.PARENT_NS=").
 		append(tbl.getParentNs()).append(" and DS_TABLE.IDENTIFIER=").
 		append(Util.strLiteral(tbl.getIdentifier())).
 		append(" and DATASET.DELETED is null order by DATASET.VERSION desc");
         
 		Statement stmt = null;
 		ResultSet rs = null;
 		try{
 			stmt = conn.createStatement();
 			rs = stmt.executeQuery(buf.toString());
 			if (rs.next()) return rs.getString(1);
 		}
 		finally{
 			try{
 				if (stmt!=null) stmt.close();
 				if (rs!=null) rs.close();
 			}
 			catch (SQLException e){}
 		}
 		
 		return null;
 	}
     
 	/**
 	*
 	*/
 	public String getLatestDstID(Dataset dst) throws SQLException{
     
 		StringBuffer buf = new StringBuffer().
 		append("select DATASET_ID from DATASET where WORKING_COPY='N' and DELETED is null and ").
 		append("IDENTIFIER=").append(Util.strLiteral(dst.getIdentifier())).
 		append(" order by VERSION desc");
 
 		Statement stmt = null;
 		ResultSet rs = null;
 		try{
 			stmt = conn.createStatement();
 			rs = stmt.executeQuery(buf.toString());
 			if (rs.next()) return rs.getString(1);
 		}
 		finally{
 			try{
 				if (stmt!=null) stmt.close();
 				if (rs!=null) rs.close();
 			}
 			catch (SQLException e){}
 		}
 		
 		return null;
 	}
     
     /**
     *
     */
     private String composeNewVersion(String oldVersion){
         if (oldVersion==null) oldVersion = "0";
         int oldVer = Integer.parseInt(oldVersion);
         return String.valueOf(oldVer+1);
     }
     
 	/**
 	*
 	*/
 	public Vector getRegStatuses(){
 		return regStatuses;
 	}
     
     /**
     * Needed for checking if the namespace should be deleted as well
     */
     public boolean isLastTbl(String id, String idfier, String parentNS)
                                                         throws SQLException{
         
         String s =
 		"select count(*) from DS_TABLE " +
 		"left outer join DST2TBL on DS_TABLE.TABLE_ID=DST2TBL.TABLE_ID " +
 		"left outer join DATASET on DST2TBL.DATASET_ID=DATASET.DATASET_ID " +
         "where DS_TABLE.IDENTIFIER='" + idfier +
 		"' and DATASET.DELETED is null and " +
         "DS_TABLE.TABLE_ID<>" + id + " and ";
         
         if (parentNS==null)
             s = s + "DS_TABLE.PARENT_NS is null";
         else
             s = s + "DS_TABLE.PARENT_NS=" + parentNS;
             
         boolean f = false;
         
         ResultSet rs = conn.createStatement().executeQuery(s);
         if (rs.next()){
             if (rs.getInt(1) == 0)
                 f = true;
         }
         
         return f;
     }
     
     /**
     * Needed for checking if the namespace should be deleted as well
     */
     public boolean isLastDst(String id, String idfier)
                                                         throws SQLException{
         
         String s = "select count(*) from DATASET " +
         "where IDENTIFIER='" + idfier + "' and DELETED is null and " +
         "DATASET_ID<>" + id;
             
         boolean f = false;
         
         ResultSet rs = conn.createStatement().executeQuery(s);
         if (rs.next()){
             if (rs.getInt(1) == 0)
                 f = true;
         }
         
         return f;
     }
 
 	/**
 	*
 	*/
 	public boolean isLatestDst(String id, String idf) throws SQLException{
 		
 		Dataset dst = new Dataset(null, null, null);
 		dst.setIdentifier(idf);
 		return id.equals(getLatestDstID(dst));
 	}
 	
 	public boolean isLatestCommonElm(String id, String idf) throws SQLException{
 		DataElement elm = new DataElement(id, null, null);
 		elm.setIdentifier(idf);
 		return id.equals(getLatestElmID(elm));
 	}
 
 	public boolean isFirstCommonElm(String idf) throws SQLException{
 		
 		StringBuffer buf = new StringBuffer().
		append("select count(*) from DATAELEM where IDENTIFIER=").append(Util.strLiteral(idf)).
		append(" and PARENT_NS is null");
 		
 		Statement stmt = null;
 		ResultSet rs = null;
 		try{
 			stmt = conn.createStatement();
 			rs = stmt.executeQuery(buf.toString());
 			if (rs.next() && rs.getInt(1)==1)
 				return true;
 		}
 		finally{
 			try{
 				if (stmt!=null) stmt.close();
 				if (rs!=null) rs.close();
 			}
 			catch (SQLException e){}
 		}
 		
 		return false;
 	}
 
 	/**
 	*
 	*/
 	public void setContext(ServletContext ctx){
 		this.ctx = ctx;
 	}
 
 	/**
 	*
 	*/
 	public void updateVersion(){
 		this.versionUpdate = true;
 	}
     
     /**
     * main for testing
     */
     public static void main(String[] args){
         
         try{
             Class.forName("com.mysql.jdbc.Driver");
             Connection conn =
             DriverManager.getConnection("jdbc:mysql://195.250.186.33:3306/dd", "dduser", "xxx");
                 
             AppUserIF user = new TestUser(false);
             user.authenticate("heinlja", "sss");
             
 			VersionManager verMan = new VersionManager(conn, user);
             verMan.checkInElm("14917", "Incomplete");
       }
         catch (Exception e){
             System.out.println(e.toString());
         }
     }
 }
