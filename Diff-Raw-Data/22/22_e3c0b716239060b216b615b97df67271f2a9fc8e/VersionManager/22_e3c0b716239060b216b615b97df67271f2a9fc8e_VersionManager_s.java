 package eionet.meta;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.sql.Types;
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.Vector;
 
 import javax.servlet.ServletContext;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 
 import eionet.meta.notif.Subscriber;
 import eionet.meta.notif.UNSEventSender;
 import eionet.meta.savers.CopyHandler;
 import eionet.meta.savers.DataElementHandler;
 import eionet.meta.savers.DatasetHandler;
 import eionet.meta.savers.DsTableHandler;
 import eionet.meta.savers.Parameters;
 import eionet.meta.service.IDataService;
 import eionet.util.Util;
 import eionet.util.sql.INParameters;
 import eionet.util.sql.SQL;
 import eionet.util.sql.SQLGenerator;
 import eionet.util.sql.SQLTransaction;
 
 /**
  *
  * @author Jaanus Heinlaid
  *
  */
 public class VersionManager {
 
     /** */
     private static final Logger LOGGER = Logger.getLogger(VersionManager.class);
 
     private Connection conn = null;
     private DDSearchEngine searchEngine = null;
     private DDUser user = null;
     private boolean upwardsVersioning = false;
     private boolean versionUpdate = false;
 
     /** possible registration statuses */
     private Vector regStatuses = new Vector();
 
     /** servlet context object if instatiated from servlet environment */
     private ServletContext ctx = null;
     protected Parameters servlRequestParams = null;
 
     /** local instance of data service. */
     private IDataService dataService;
 
     /**
      *
      */
     public VersionManager(Connection conn, DDUser user) {
         this(conn, new DDSearchEngine(conn), user);
     }
 
     /**
      *
      */
     public VersionManager(Connection conn, DDSearchEngine searchEngine, DDUser user) {
         this();
         this.conn = conn;
         this.user = user;
         this.searchEngine = searchEngine;
     }
 
     /**
      *
      */
     public VersionManager() {
 
         // init registration statuses vector
         regStatuses.add("Incomplete");
         regStatuses.add("Candidate");
         regStatuses.add("Recorded");
         regStatuses.add("Qualified");
         regStatuses.add("Released");
     }
 
     public void setUpwardsVersioning(boolean f) {
         this.upwardsVersioning = f;
     }
 
     /**
      * See if the specified object type has the specified short name in the specified namespace (or dataset if object type is "tbl")
      * checked out. If yes then return the name of the user who checked it out. Otherwise return null.
      *
      * @param type
      *            object type ("elm", "tbl" or "dst")
      * @param namespaceID
      *            namespace id if type=="elm", dataset id if type=="tbl", ignored if type=="dst"
      * @param shortName
      *
      * @return the name of the working user or null if it's missing
      * @exception SQLException
      */
     public String getWorkingUser(String namespaceID, String idfier, String type) throws SQLException {
 
         String tblName = null;
         String idField = null;
         if (type.equals("elm")) {
             tblName = "DATAELEM";
             idField = "DATAELEM_ID";
         } else if (type.equals("tbl")) {
             tblName = "DS_TABLE";
             idField = "TABLE_ID";
         } else if (type.equals("dst")) {
             tblName = "DATASET";
             idField = "DATASET_ID";
         } else {
             throw new SQLException("Unknown type");
         }
 
         String namespaceField = null;
         if (type.equals("elm") || type.equals("tbl")) {
             namespaceField = "PARENT_NS";
         }
 
         INParameters inParams = new INParameters();
 
         String qry =
                 "select distinct WORKING_USER, " + idField + " from " + tblName + " where IDENTIFIER="
                         + inParams.add(idfier, Types.VARCHAR);
         if (namespaceField != null && namespaceID != null) {
             qry += " and " + namespaceField + "=" + inParams.add(namespaceID, Types.INTEGER);
         }
 
         qry += " and " + tblName + ".WORKING_COPY='Y'";
         qry += " order by " + idField + " desc";
 
         PreparedStatement stmt = SQL.preparedStatement(qry, inParams, conn);
 
         ResultSet rs = stmt.executeQuery();
         if (rs.next()) {
             return rs.getString("WORKING_USER");
         } else {
             return null;
         }
     }
 
     /**
      *
      */
     public String getWorkingUser(String nsID) throws SQLException {
 
         if (nsID == null) {
             return null;
         }
 
         INParameters inParams = new INParameters();
 
         String s = "select WORKING_USER from NAMESPACE where NAMESPACE_ID=" + inParams.add(nsID, Types.INTEGER);
 
         PreparedStatement stmt = SQL.preparedStatement(s, inParams, conn);
         ResultSet rs = stmt.executeQuery();
         if (rs.next()) {
             return rs.getString(1);
         } else {
             return null;
         }
     }
 
     /**
      *
      */
     public String getTblWorkingUser(String idfier, String parentNs) throws SQLException {
 
         INParameters inParams = new INParameters();
         String q =
                 "select distinct DS_TABLE.WORKING_USER from DS_TABLE " + "where IDENTIFIER=" + inParams.add(idfier, Types.VARCHAR)
                         + " and WORKING_COPY='Y'";
 
         if (parentNs != null) {
             q = q + " and PARENT_NS=" + inParams.add(parentNs, Types.INTEGER);
         } else {
             q = q + " and PARENT_NS is null";
         }
 
         PreparedStatement stmt = SQL.preparedStatement(q, inParams, conn);
         ResultSet rs = stmt.executeQuery();
         if (rs.next()) {
             return rs.getString("WORKING_USER");
         } else {
             return null;
         }
     }
 
     /**
      *
      */
     public String getDstWorkingUser(String idfier) throws SQLException {
 
         INParameters inParams = new INParameters();
         String q =
                 "select distinct DATASET.WORKING_USER from DATASET " + "where DATASET.IDENTIFIER="
                         + inParams.add(idfier, Types.VARCHAR) + " and " + "DATASET.WORKING_COPY='Y'";
 
         PreparedStatement stmt = SQL.preparedStatement(q, inParams, conn);
         ResultSet rs = stmt.executeQuery();
         if (rs.next()) {
             return rs.getString("WORKING_USER");
         } else {
             return null;
         }
     }
 
     /**
      *
      */
     public String getDstCopyWorkingUser(String copyID) throws SQLException {
 
         INParameters inParams = new INParameters();
         StringBuffer buf = new StringBuffer("select WORKING_USER from DATASET ");
         buf.append("where DATASET_ID=").append(inParams.add(copyID, Types.INTEGER));
 
         PreparedStatement stmt = SQL.preparedStatement(buf.toString(), inParams, conn);
         ResultSet rs = stmt.executeQuery();
 
         if (rs.next()) {
             return rs.getString("WORKING_USER");
         } else {
             return null;
         }
     }
 
     /**
      *
      */
     public String getWorkingCopyID(DataElement elm) throws SQLException {
 
         if (elm == null || user == null) {
             return null;
         }
 
         INParameters inParams = new INParameters();
         StringBuffer buf = new StringBuffer();
         buf.append("select DATAELEM_ID from DATAELEM where WORKING_COPY='Y'").append(" and WORKING_USER=")
                 .append(inParams.add(user.getUserName(), Types.VARCHAR)).append(" and CHECKEDOUT_COPY_ID=")
                 .append(inParams.add(elm.getID(), Types.INTEGER));
 
         PreparedStatement stmt = SQL.preparedStatement(buf.toString(), inParams, conn);
         ResultSet rs = stmt.executeQuery();
 
         return rs.next() ? rs.getString(1) : null;
     }
 
     /**
      *
      */
     public String getWorkingCopyID(DsTable tbl) throws SQLException {
 
         INParameters inParams = new INParameters();
 
         String q =
                 "select distinct TABLE_ID from DS_TABLE " + "where WORKING_COPY='Y' and " + "IDENTIFIER="
                         + inParams.add(tbl.getIdentifier(), Types.VARCHAR) + " and " + "PARENT_NS="
                         + inParams.add(tbl.getParentNs(), Types.INTEGER);
 
         PreparedStatement stmt = SQL.preparedStatement(q, inParams, conn);
         ResultSet rs = stmt.executeQuery();
 
         if (rs.next()) {
             return rs.getString(1);
         }
 
         return null;
     }
 
     /**
      *
      */
     public String getWorkingCopyID(Dataset dst) throws SQLException {
 
         if (dst == null || user == null) {
             return null;
         }
 
         INParameters inParams = new INParameters();
 
         StringBuffer buf = new StringBuffer();
         buf.append("select DATASET_ID from DATASET where WORKING_COPY='Y'").append(" and WORKING_USER=")
                 .append(inParams.add(user.getUserName(), Types.VARCHAR)).append(" and CHECKEDOUT_COPY_ID=")
                 .append(inParams.add(dst.getID(), Types.INTEGER));
 
         PreparedStatement stmt = SQL.preparedStatement(buf.toString(), inParams, conn);
         ResultSet rs = stmt.executeQuery();
 
         return rs.next() ? rs.getString(1) : null;
     }
 
     /**
      * Check out the specified object. Meaning a working copy of the object will be created.
      *
      * @param id
      *            object id.
      * @param type
      *            object type (one of "elm", "tbl" or "dst")
      * @return id of the working copy
      * @exception Exception
      */
     public String checkOut(String id, String type) throws Exception {
 
         if (type.equals("elm")) {
             return checkOutElm(id);
         } else if (type.equals("dst")) {
             return checkOutDst(id);
         } else {
             throw new Exception("Unknown object type: " + type);
         }
     }
 
     /**
      *
      * @param elmID
      * @return
      * @throws Exception
      */
     public String checkOutElm(String elmID) throws Exception {
 
         SQLTransaction tx = null;
         try {
             tx = SQLTransaction.begin(conn);
             String result = checkOutElm_(elmID);
             tx.commit();
             return result;
         } catch (Exception e) {
             SQLTransaction.rollback(tx);
             throw e;
         } finally {
             SQLTransaction.end(tx);
         }
     }
 
     /**
      */
     private String checkOutElm_(String elmID) throws Exception {
 
         if (user == null || !user.isAuthentic()) {
             throw new Exception("Check-out attempt by an unauthorized user!");
         }
 
         String newID = null;
         String topNS = null;
         try {
             INParameters inParams = new INParameters();
             // set the working user of the original
             SQLGenerator gen = new SQLGenerator();
             gen.setTable("DATAELEM");
             gen.setFieldExpr("WORKING_USER", inParams.add(user.getUserName()));
 
             String q = gen.updateStatement() + " where DATAELEM_ID=" + inParams.add(elmID, Types.INTEGER);
 
             PreparedStatement stmt = SQL.preparedStatement(q, inParams, conn);
             stmt.executeUpdate();
 
             // copy the element
             String strResetVersionAndStatus =
                     servlRequestParams == null ? null : servlRequestParams.getParameter("resetVersionAndStatus");
             CopyHandler copyHandler = new CopyHandler(conn, ctx, searchEngine);
             copyHandler.setUser(user);
 
             // copy TBL2ELEM relations too, because common elements have links to tables
             newID = copyHandler.copyElm(elmID, true, strResetVersionAndStatus == null, strResetVersionAndStatus != null);
 //TODO VOC_ELEM
             if (newID != null) {
                 gen.clear();
                 gen.setTable("DATAELEM");
                 gen.setFieldExpr("CHECKEDOUT_COPY_ID", elmID);
                 conn.createStatement().executeUpdate(gen.updateStatement() + " where DATAELEM_ID=" + newID);
             }
         } catch (Exception e) {
             try {
                 cleanupCheckout(elmID, "DATAELEM", topNS, newID);
             } catch (Exception ee) {
             }
             throw e;
         }
 
         return newID;
     }
 
     /**
      *
      * @param dstID
      * @return
      * @throws Exception
      */
     private String checkOutDst(String dstID) throws Exception {
 
         SQLTransaction tx = null;
         try {
             tx = SQLTransaction.begin(conn);
             String result = checkOutDst_(dstID);
             tx.commit();
             return result;
         } catch (Exception e) {
             SQLTransaction.rollback(tx);
             throw e;
         } finally {
             SQLTransaction.end(tx);
         }
     }
 
     /**
      * Check out the specified dataset.
      *
      * @param dstID
      *            dataset id.
      * @return id of the working copy
      * @exception Exception
      */
     private String checkOutDst_(String dstID) throws Exception {
 
         if (dstID == null) {
             throw new Exception("Dataset ID missing!");
         }
 
         LOGGER.debug("Checking out dataset with id " + dstID);
 
         String newID = null;
         String topNS = null;
         try {
             // set the working user of the original
 
             LOGGER.debug("Setting the working user of the original copy");
 
             INParameters inParams = new INParameters();
             SQLGenerator gen = new SQLGenerator();
             gen.setTable("DATASET");
             gen.setFieldExpr("WORKING_USER", inParams.add(user.getUserName()));
 
             String q = gen.updateStatement() + " where DATASET_ID=" + inParams.add(dstID, Types.INTEGER);
 
             PreparedStatement stmt = SQL.preparedStatement(q, inParams, conn);
             stmt.executeUpdate();
 
             // set the WORKING_USER of top namespace
 
             LOGGER.debug("Setting the working user of the top namespace");
 
             inParams = new INParameters();
             q = "select CORRESP_NS from DATASET where DATASET_ID=" + inParams.add(dstID, Types.INTEGER);
             stmt = SQL.preparedStatement(q, inParams, conn);
             ResultSet rs = stmt.executeQuery();
             topNS = rs.next() ? rs.getString(1) : null;
             if (topNS != null) {
                 inParams = new INParameters();
                 gen.clear();
                 gen.setTable("NAMESPACE");
                 gen.setFieldExpr("WORKING_USER", inParams.add(user.getUserName()));
                 q = gen.updateStatement() + " where NAMESPACE_ID=" + inParams.add(topNS, Types.INTEGER);
 
                 stmt = SQL.preparedStatement(q, inParams, conn);
                 stmt.executeUpdate();
             }
 
             // copy the dataset
 
             String strResetVersionAndStatus =
                     servlRequestParams == null ? null : servlRequestParams.getParameter("resetVersionAndStatus");
             CopyHandler copyHandler = new CopyHandler(conn, ctx, searchEngine);
             copyHandler.setUser(user);
 
             long startTime = System.currentTimeMillis();
             newID = copyHandler.copyDst(dstID, true, strResetVersionAndStatus != null);
             LOGGER.info("Copying dataset " + dstID + " took " + (System.currentTimeMillis() - startTime) + " ms");
             if (newID != null) {
                 inParams = new INParameters();
                 gen.clear();
                 gen.setTable("DATASET");
                 gen.setFieldExpr("CHECKEDOUT_COPY_ID", inParams.add(dstID, Types.INTEGER));
                 q = gen.updateStatement() + " where DATASET_ID=" + inParams.add(newID, Types.INTEGER);
                 stmt = SQL.preparedStatement(q, inParams, conn);
                 stmt.executeUpdate();
             }
 
         } catch (Exception e) {
 
             try {
                 cleanupCheckout(dstID, "DATASET", topNS, newID);
             } catch (Exception ee) {
             }
             throw e;
         }
 
         return newID;
     }
 
     /**
      *
      *
      */
     private void cleanupCheckout(String objID, String objTable, String topNS, String newID) throws Exception {
         // reset the original's WORKING_USER
         SQLGenerator gen = new SQLGenerator();
         gen.setTable(objTable);
         gen.setFieldExpr("WORKING_USER", "NULL");
         StringBuffer buf = new StringBuffer(gen.updateStatement());
         buf.append(" where ");
         if (objTable.equals("DS_TABLE")) {
             buf.append("TABLE_ID");
         } else {
             buf.append(objTable);
             buf.append("_ID");
         }
         buf.append("=");
         buf.append(objID);
 
         Statement stmt = conn.createStatement();
         stmt.executeUpdate(buf.toString());
 
         // reset the WORKING_USER of top namespace
         if (topNS != null) {
             gen.clear();
             gen.setTable("NAMESPACE");
             gen.setFieldExpr("WORKING_USER", "NULL");
             buf = new StringBuffer(gen.updateStatement());
             buf.append(" where NAMESPACE_ID=");
             buf.append(topNS);
             stmt.executeUpdate(buf.toString());
         }
 
         // destroy the copy
         if (newID == null) {
             return;
         }
 
         MrProper mrProper = new MrProper(conn);
         mrProper.setContext(ctx);
         mrProper.setUser(user);
 
         Parameters pars = new Parameters();
         String objType = null;
         if (objTable.equals("DATASET")) {
             objType = "dst";
         } else if (objTable.equals("DS_TABLE")) {
             objType = "tbl";
         }
         if (objTable.equals("DATAELEM")) {
             objType = "elm";
         }
         pars.addParameterValue("rm_obj_type", objType);
         pars.addParameterValue("rm_crit", "id");
         pars.addParameterValue("rm_id", newID);
 
         mrProper.removeObj(pars);
     }
 
     /**
      * Check in the specified object.
      *
      * @param objID
      *            object id
      * @param objType
      *            object type ("elm", "tbl" or "dst")
      * @param status
      *            registration status where the user wants the checked-in object to be. The method checks if the user has filled all
      *            the requirements of that status. If not, an exception with a proper message is thrown.
      * @exception Exception
      */
     public boolean checkIn(String objID, String objType, String status) throws Exception {
 
         if (objType.equals("elm")) {
             return checkInElm(objID, status);
         } else if (objType.equals("dst")) {
             return checkInDst(objID, status);
         } else {
             throw new Exception("Unknown object type: " + objType);
         }
     }
 
     /**
      *
      * @param elmID
      * @param status
      * @return
      * @throws Exception
      */
     public boolean checkInElm(String elmID, String status) throws Exception {
 
         SQLTransaction tx = null;
         try {
             tx = SQLTransaction.begin(conn);
             boolean result = checkInElm_(elmID, status);
             tx.commit();
             return result;
         } catch (Exception e) {
             SQLTransaction.rollback(tx);
             throw e;
         } finally {
             SQLTransaction.end(tx);
         }
     }
 
     /**
      * Check in the specified element.
      */
     private boolean checkInElm_(String elmID, String status) throws Exception {
 
         // load the element we need to check in
         DataElement elm = loadElm(elmID);
 
         // check the requirements for checking in a dataset
         checkRequirements(elm, status);
 
         // init the SQL statement object and SQL generator
         SQLGenerator gen = new SQLGenerator();
         Statement stmt = conn.createStatement();
 
         String checkedoutCopyID = servlRequestParams == null ? null : servlRequestParams.getParameter("checkedout_copy_id");
         if (checkedoutCopyID != null) {
             if (!versionUpdate) {
 
                 //backup vocabulary bindings before deleting the element to the new element
                 gen.clear();
                 gen.setTable("VOCABULARY2ELEM");
                 gen.setFieldExpr("DATAELEM_ID", elmID);
                 stmt.executeUpdate(gen.updateStatement() + " where DATAELEM_ID=" + checkedoutCopyID);
 
                 gen.clear();
                 gen.setTable("VOCABULARY_CONCEPT_ELEMENT");
                 gen.setFieldExpr("DATAELEM_ID", elmID);
                 stmt.executeUpdate(gen.updateStatement() + " where DATAELEM_ID=" + checkedoutCopyID);
 
                 // delete the previous copy
                 Parameters params = new Parameters();
                 params.addParameterValue("mode", "delete");
                 params.addParameterValue("complete", "true");
                 params.addParameterValue("delem_id", checkedoutCopyID);
                 DataElementHandler elmHandler = new DataElementHandler(conn, params, ctx);
                 elmHandler.setUser(user);
                 elmHandler.setVersioning(false);
                 elmHandler.setUseForce(true);
                 elmHandler.execute();
 
                 // the new copy must get the id of the previous one
                 gen.clear();
                 gen.setTable("DATAELEM");
                 gen.setFieldExpr("DATAELEM_ID", checkedoutCopyID);
                 stmt.executeUpdate(gen.updateStatement() + " where DATAELEM_ID=" + elmID);
 
                 // the id of the new copy must be changed in all relations as well
                 DataElementHandler.replaceID(elmID, checkedoutCopyID, conn);
                 elmID = checkedoutCopyID;
             } else {
                 // unlock the checked-out copy
                 gen.clear();
                 gen.setTable("DATAELEM");
                 gen.setFieldExpr("WORKING_USER", "NULL");
                 stmt.executeUpdate(gen.updateStatement() + " where DATAELEM_ID=" + checkedoutCopyID);
             }
         }
 
         // update the checked-in copy's vital fields
         gen.clear();
         gen.setTable("DATAELEM");
         gen.setField("WORKING_COPY", "N");
         gen.setFieldExpr("WORKING_USER", "NULL");
         gen.setField("USER", user.getUserName());
         gen.setFieldExpr("DATE", String.valueOf(System.currentTimeMillis()));
         gen.setFieldExpr("CHECKEDOUT_COPY_ID", "NULL");
         stmt.executeUpdate(gen.updateStatement() + " where DATAELEM_ID=" + elmID);
 
         // make sure certain users are subscribed and send UNS notification
         try {
             String checkedoutCopyUser = null;
             if (checkedoutCopyID != null) {
                 ResultSet rs = stmt.executeQuery("select USER from DATAELEM where DATAELEM_ID=" + checkedoutCopyID);
                 if (rs != null && rs.next()) {
                     checkedoutCopyUser = rs.getString(1);
                 }
             }
             List<String> usersToSubscribe = getMustBeSubscribedUsers(elm, checkedoutCopyID, stmt);
             if (usersToSubscribe != null && usersToSubscribe.size() > 0) {
                 Subscriber.subscribeToElement(usersToSubscribe, elm.getIdentifier());
             }
         } catch (Throwable t) {
             t.printStackTrace(System.out);
         }
         String eventType =
                 checkedoutCopyID != null && checkedoutCopyID.length() > 0 ? Subscriber.COMMON_ELEMENT_CHANGED_EVENT
                         : Subscriber.NEW_COMMON_ELEMENT_EVENT;
 
         new UNSEventSender().definitionChanged(elm, eventType, user == null ? null : user.getUserName());
 
         stmt.close();
         return true;
     }
 
     /**
      *
      * @param dstID
      * @param status
      * @return
      * @throws Exception
      */
     private boolean checkInDst(String dstID, String status) throws Exception {
 
         SQLTransaction tx = null;
         try {
             tx = SQLTransaction.begin(conn);
             boolean result = checkInDst_(dstID, status);
             tx.commit();
             return result;
         } catch (Exception e) {
             SQLTransaction.rollback(tx);
             throw e;
         } finally {
             SQLTransaction.end(tx);
         }
     }
 
     /**
      * Check in the specified dataset.
      */
     private boolean checkInDst_(String dstID, String status) throws Exception {
 
         // load the dataset we need to check in
         Dataset dst = loadDst(dstID);
         String correspNamespaceID = dst.getNamespaceID();
 
         // check the requirements for checking in a dataset
         checkRequirements(dst, status);
 
         // init the SQL statement object and SQL generator
         SQLGenerator gen = new SQLGenerator();
         Statement stmt = conn.createStatement();
 
         String checkedoutCopyID = servlRequestParams == null ? null : servlRequestParams.getParameter("checkedout_copy_id");
         if (checkedoutCopyID != null) {
             if (!versionUpdate) {
 
                 // remember the id-identifier mappings of tables in the previous copy, before deleting it
                 Hashtable tableIdsAndIdentifiers = new Hashtable();
                 Vector v = searchEngine.getDatasetTables(checkedoutCopyID, false);
                 for (int i = 0; v != null && i < v.size(); i++) {
                     DsTable tbl = (DsTable) v.get(i);
                     tableIdsAndIdentifiers.put(tbl.getIdentifier(), tbl.getID());
                 }
 
                 // delete the previous copy
                 Parameters params = new Parameters();
                 params.addParameterValue("mode", "delete");
                 params.addParameterValue("complete", "true");
                 params.addParameterValue("ds_id", checkedoutCopyID);
                 DatasetHandler dstHandler = new DatasetHandler(conn, params, ctx);
                 dstHandler.setUser(user);
                 dstHandler.setVersioning(false);
                 dstHandler.setUseForce(true);
                 dstHandler.execute();
 
                 // the new copy must get the id of the previous one
                 gen.clear();
                 gen.setTable("DATASET");
                 gen.setFieldExpr("DATASET_ID", checkedoutCopyID);
                 stmt.executeUpdate(gen.updateStatement() + " where DATASET_ID=" + dstID);
 
                 // the id of the new copy must be changed in all relations as well
                 DatasetHandler.replaceID(dstID, checkedoutCopyID, conn);
                 dstID = checkedoutCopyID;
 
                 // the tables must get the ids of previous ones too
                 v = searchEngine.getDatasetTables(checkedoutCopyID, false);
                 for (int i = 0; v != null && i < v.size(); i++) {
                     DsTable tbl = (DsTable) v.get(i);
                     String checkedOutTableID = (String) tableIdsAndIdentifiers.get(tbl.getIdentifier());
                     if (checkedOutTableID != null) {
                         DsTableHandler.replaceTableId(checkedOutTableID, tbl.getID(), this.conn);
                     }
                 }
             } else {
                 // unlock the checked-out copy
                 gen.clear();
                 gen.setTable("DATASET");
                 gen.setFieldExpr("WORKING_USER", "NULL");
                 stmt.executeUpdate(gen.updateStatement() + " where DATASET_ID=" + checkedoutCopyID);
             }
         }
 
         // update the checked-in copy's vital fields
         gen.clear();
         gen.setTable("DATASET");
         gen.setField("WORKING_COPY", "N");
         gen.setFieldExpr("WORKING_USER", "NULL");
         gen.setField("USER", user.getUserName());
         gen.setFieldExpr("DATE", String.valueOf(System.currentTimeMillis()));
         gen.setFieldExpr("CHECKEDOUT_COPY_ID", "NULL");
         stmt.executeUpdate(gen.updateStatement() + " where DATASET_ID=" + dstID);
 
         // unlock the corresponding namespace
         if (correspNamespaceID != null) {
             gen.clear();
             gen.setTable("NAMESPACE");
             gen.setFieldExpr("WORKING_USER", "NULL");
             stmt.executeUpdate(gen.updateStatement() + " where NAMESPACE_ID=" + correspNamespaceID);
         }
 
         // send UNS notification
         String eventType =
                 checkedoutCopyID != null && checkedoutCopyID.length() > 0 ? Subscriber.DATASET_CHANGED_EVENT
                         : Subscriber.NEW_DATASET_EVENT;
 
         new UNSEventSender().definitionChanged(dst, eventType, user == null ? null : user.getUserName());
 
         return true;
     }
 
     private void checkRequirements(DataElement elm, String status) throws Exception {
         // check Submitting Org
         DElemAttribute submOrg = elm.getAttributeByShortName("SubmitOrganisation");
         if (submOrg == null) {
             throw new Exception("SubmitOrganisation complex attribute required!");
         }
     }
 
     /**
      * Check status requirements of the specified table
      */
     private void checkRequirements(DsTable tbl, String status) throws Exception {
         // check Submitting Org
         DElemAttribute submOrg = tbl.getAttributeByShortName("SubmitOrganisation");
         if (submOrg == null) {
             throw new Exception("SubmitOrganisation complex attribute required!");
         }
     }
 
     /**
      * Check status requirements of the specified table
      */
     private void checkRequirements(Dataset dst, String status) throws Exception {
         // check Submitting Org
         DElemAttribute submOrg = dst.getAttributeByShortName("SubmitOrganisation");
         if (submOrg == null) {
             throw new Exception("SubmitOrganisation complex attribute required!");
         }
 
         //if dataset has non-released elements refuse check in
         if ((status != null && status.equalsIgnoreCase("Released")) || dst.getStatus().equalsIgnoreCase("Released")) {
             initDataService();
            List<eionet.meta.dao.domain.DataElement> unreleasedElems = dataService.getUnreleasedCommonElements(Integer.valueOf(dst.getID()));
             if (unreleasedElems.size() > 0) {
                throw new Exception("Released dataset must not have unreleased common elements linked to the tables. Element(s) not released: " + StringUtils.join(unreleasedElems, ","));
             }
         }
 
     }
 
     /**
      *
      */
     private DataElement loadElm(String elmID) throws Exception {
 
         if (Util.isEmpty(elmID)) {
             throw new Exception("Data element ID not specified!");
         }
 
         // get the element (this will return simple attributes + tableID
         DataElement elem = searchEngine.getDataElement(elmID);
         if (elem == null) {
             throw new Exception("Element not found!");
         }
 
         // get and set the element's complex attributes
         elem.setComplexAttributes(searchEngine.getComplexAttributes(elmID, "E", null, elem.getTableID(), elem.getDatasetID()));
 
         // set fixed values
         elem.setFixedValues(searchEngine.getFixedValues(elmID, "elem"));
 
         return elem;
     }
 
     /**
      *
      */
     private DsTable loadTbl(String tblID) throws Exception {
 
         if (Util.isEmpty(tblID)) {
             throw new Exception("Table ID not specified!");
         }
 
         // get the table
         DsTable dsTable = searchEngine.getDatasetTable(tblID);
         if (dsTable == null) {
             throw new Exception("Table not found!");
         }
 
         // get simple attributes
         Vector v = searchEngine.getSimpleAttributes(tblID, "T");
         dsTable.setSimpleAttributes(v);
 
         // get & set complex attributes
         dsTable.setComplexAttributes(searchEngine.getComplexAttributes(tblID, "T", null, null, dsTable.getDatasetID()));
 
         // get data elements (this will also return simple attributes, but no fixed values!)
         dsTable.setElements(searchEngine.getDataElements(null, null, null, null, tblID));
 
         return dsTable;
     }
 
     /**
      *
      */
     private Dataset loadDst(String dstID) throws Exception {
 
         if (Util.isEmpty(dstID)) {
             throw new Exception("Dataset ID not specified!");
         }
 
         Dataset ds = searchEngine.getDataset(dstID);
         if (ds == null) {
             throw new Exception("Dataset not found!");
         }
 
         // get & set simple attributes, compelx attributes and tables
         ds.setSimpleAttributes(searchEngine.getSimpleAttributes(dstID, "DS"));
         ds.setComplexAttributes(searchEngine.getComplexAttributes(dstID, "DS"));
         ds.setTables(searchEngine.getDatasetTables(dstID, false));
 
         return ds;
     }
 
     /**
      *
      */
     public String getLatestElmID(DataElement elm) throws SQLException {
 
         // see if this is a common element and behave relevantly
         boolean elmCommon = elm.getNamespace() == null || elm.getNamespace().getID() == null;
 
         StringBuffer buf = new StringBuffer("select DATAELEM.DATAELEM_ID from DATAELEM");
         if (elm.getNamespace() != null && elm.getNamespace().getID() != null) { // non-common element
             buf.append(", TBL2ELEM, DST2TBL, DATASET ").append("where ").append("DATAELEM.DATAELEM_ID=TBL2ELEM.DATAELEM_ID and ")
                     .append("TBL2ELEM.TABLE_ID=DST2TBL.TABLE_ID and ").append("DST2TBL.DATASET_ID=DATASET.DATASET_ID and ")
                     .append("DATAELEM.WORKING_COPY='N' and DATAELEM.PARENT_NS=").append(elm.getNamespace().getID())
                     .append(" and DATAELEM.IDENTIFIER=").append(SQL.toLiteral(elm.getIdentifier()))
                     .append(" and DATASET.DELETED is null order by DATASET.DATASET_ID desc");
         } else {
             buf.append(" where ").append("DATAELEM.WORKING_COPY='N' and DATAELEM.PARENT_NS is null and ")
                     .append("DATAELEM.IDENTIFIER=").append(SQL.toLiteral(elm.getIdentifier()))
                     .append(" order by DATAELEM.DATAELEM_ID desc");
         }
 
         Statement stmt = null;
         ResultSet rs = null;
         try {
             stmt = conn.createStatement();
             rs = stmt.executeQuery(buf.toString());
             if (rs.next()) {
                 return rs.getString(1);
             }
         } finally {
             try {
                 if (stmt != null) {
                     stmt.close();
                 }
                 if (rs != null) {
                     rs.close();
                 }
             } catch (SQLException e) {
             }
         }
 
         return null;
     }
 
     /**
      *
      * @param tbl
      * @return
      * @throws SQLException
      */
     public String getLatestReleasedTblID(DsTable tbl) throws SQLException {
 
         String tblIdf = tbl.getIdentifier();
         String parentNs = tbl.getParentNs();
         if (Util.isEmpty(tblIdf) || Util.isEmpty(parentNs)) {
             return null;
         }
 
         StringBuffer buf = new StringBuffer();
         buf.append("select DST2TBL.TABLE_ID from DS_TABLE ")
                 .append("left outer join DST2TBL on DS_TABLE.TABLE_ID=DST2TBL.TABLE_ID ")
                 .append("left outer join DATASET on DST2TBL.DATASET_ID=DATASET.DATASET_ID ")
                 .append("where DS_TABLE.IDENTIFIER=? and DATASET.CORRESP_NS=? ")
                 .append("and DATASET.REG_STATUS='Released' and DATASET.WORKING_COPY='N' ")
                 .append("and DATASET.CHECKEDOUT_COPY_ID is null and DATASET.WORKING_USER is null and DATASET.DELETED is null ")
                 .append("order by DST2TBL.DATASET_ID desc");
 
         PreparedStatement pstmt = null;
         ResultSet rs = null;
         try {
             pstmt = conn.prepareStatement(buf.toString());
             pstmt.setString(1, tblIdf);
             pstmt.setInt(2, Integer.parseInt(parentNs));
 
             rs = pstmt.executeQuery();
             return rs.next() ? rs.getString(1) : null;
         } finally {
             SQL.close(rs);
             SQL.close(pstmt);
         }
     }
 
     /**
      *
      */
     public String getCheckedOutCopyID(Dataset dst) throws SQLException {
 
         StringBuffer buf = new StringBuffer();
         buf.append("select DATASET_ID from DATASET where WORKING_COPY='N' and DELETED is null and ").append("IDENTIFIER=")
                 .append(SQL.toLiteral(dst.getIdentifier())).append(" order by DATASET_ID desc");
 
         Statement stmt = null;
         ResultSet rs = null;
         try {
             stmt = conn.createStatement();
             rs = stmt.executeQuery(buf.toString());
             if (rs.next()) {
                 return rs.getString(1);
             }
         } finally {
             try {
                 if (stmt != null) {
                     stmt.close();
                 }
                 if (rs != null) {
                     rs.close();
                 }
             } catch (SQLException e) {
             }
         }
 
         return null;
     }
 
     /**
      *
      */
     private String composeNewVersion(String oldVersion) {
         if (oldVersion == null) {
             oldVersion = "0";
         }
         int oldVer = Integer.parseInt(oldVersion);
         return String.valueOf(oldVer + 1);
     }
 
     /**
      *
      */
     public Vector getRegStatuses() {
         return regStatuses;
     }
 
     /**
      * Needed for checking if the namespace should be deleted as well
      */
     public boolean isLastTbl(String id, String idfier, String parentNS) throws SQLException {
 
         String s =
                 "select count(*) from DS_TABLE " + "left outer join DST2TBL on DS_TABLE.TABLE_ID=DST2TBL.TABLE_ID "
                         + "left outer join DATASET on DST2TBL.DATASET_ID=DATASET.DATASET_ID " + "where DS_TABLE.IDENTIFIER='"
                         + idfier + "' and DATASET.DELETED is null and " + "DS_TABLE.TABLE_ID<>" + id + " and ";
 
         if (parentNS == null) {
             s = s + "DS_TABLE.PARENT_NS is null";
         } else {
             s = s + "DS_TABLE.PARENT_NS=" + parentNS;
         }
 
         boolean f = false;
 
         ResultSet rs = conn.createStatement().executeQuery(s);
         if (rs.next()) {
             if (rs.getInt(1) == 0) {
                 f = true;
             }
         }
 
         return f;
     }
 
     /**
      * Needed for checking if the namespace should be deleted as well
      */
     public boolean isLastDst(String id, String idfier) throws SQLException {
 
         String s =
                 "select count(*) from DATASET " + "where IDENTIFIER='" + idfier + "' and DELETED is null and " + "DATASET_ID<>"
                         + id;
 
         boolean f = false;
 
         ResultSet rs = conn.createStatement().executeQuery(s);
         if (rs.next()) {
             if (rs.getInt(1) == 0) {
                 f = true;
             }
         }
 
         return f;
     }
 
     /**
      *
      * @param id
      * @param idf
      * @return
      * @throws SQLException
      */
     public boolean isLatestCommonElm(String id, String idf) throws SQLException {
         DataElement elm = new DataElement(id, null, null);
         elm.setIdentifier(idf);
         return id.equals(getLatestElmID(elm));
     }
 
     /**
      *
      * @param idf
      * @return
      * @throws SQLException
      */
     public boolean isFirstCommonElm(String idf) throws SQLException {
 
         StringBuffer buf =
                 new StringBuffer().append("select count(*) from DATAELEM where IDENTIFIER=").append(SQL.toLiteral(idf))
                         .append(" and PARENT_NS is null");
 
         Statement stmt = null;
         ResultSet rs = null;
         try {
             stmt = conn.createStatement();
             rs = stmt.executeQuery(buf.toString());
             if (rs.next() && rs.getInt(1) == 1) {
                 return true;
             }
         } finally {
             try {
                 if (stmt != null) {
                     stmt.close();
                 }
                 if (rs != null) {
                     rs.close();
                 }
             } catch (SQLException e) {
             }
         }
 
         return false;
     }
 
     /**
      *
      */
     public void setContext(ServletContext ctx) {
         this.ctx = ctx;
     }
 
     /**
      *
      */
     public void setVersionUpdate(boolean b) {
         this.versionUpdate = b;
     }
 
     /**
      *
      * @param elm
      * @param latestElm
      * @return
      * @throws SQLException
      */
     private List<String> getMustBeSubscribedUsers(DataElement elm, String checkedoutCopyID, Statement stmt) throws SQLException {
 
         ArrayList<String> result = new ArrayList<String>();
 
         // add owner of this element
         String owner = searchEngine.getElmOwner(elm.getIdentifier());
         if (owner != null) {
             result.add(owner);
         }
 
         // add owners of datasets of referring tables
         Vector refTables = searchEngine.getReferringTables(elm.getID());
         for (int i = 0; refTables != null && i < refTables.size(); i++) {
             DsTable tbl = (DsTable) refTables.get(i);
             String tblOwner = tbl.getOwner();
             if (tblOwner != null && !result.contains(tblOwner)) {
                 result.add(tblOwner);
             }
         }
 
         // add creator of checked-out copy
         if (checkedoutCopyID != null) {
             ResultSet rs = stmt.executeQuery("select USER from DATAELEM where DATAELEM_ID=" + checkedoutCopyID);
             if (rs != null && rs.next()) {
                 String checkedoutCopyUser = rs.getString(1);
                 if (checkedoutCopyUser != null) {
                     result.add(checkedoutCopyUser);
                 }
             }
         }
 
         // finally, remove this.user he doesn't need a notification anyway
         if (user != null && user.getUserName() != null) {
             result.remove(user.getUserName());
         }
 
         return result.size() > 0 ? result : null;
     }
 
     /**
      *
      * @param servlRequestParams
      */
     public void setServlRequestParams(Parameters servlRequestParams) {
         this.servlRequestParams = servlRequestParams;
     }
 
     /**
      *
      *
      */
     public Parameters getServlRequestParams() {
         return this.servlRequestParams;
     }
 
     private void initDataService() {
         if (dataService == null) {
            ApplicationContext ctx = new ClassPathXmlApplicationContext("spring-context.xml");
            dataService = ctx.getBean(IDataService.class);
         }
 
     }
 }
