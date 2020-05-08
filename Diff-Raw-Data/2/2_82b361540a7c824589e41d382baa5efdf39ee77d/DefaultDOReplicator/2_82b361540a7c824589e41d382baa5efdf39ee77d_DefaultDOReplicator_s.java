 package fedora.server.storage.replication;
 
 import java.util.*;
 import java.sql.*;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.util.regex.Pattern;
 
 import fedora.server.errors.*;
 import fedora.server.storage.*;
 import fedora.server.storage.types.*;
 import fedora.server.utilities.SQLUtility;
 
 import fedora.server.Module;
 import fedora.server.Server;
 import fedora.server.storage.ConnectionPoolManager;
 import fedora.server.errors.ModuleInitializationException;
 
 /**
  *
  * <p><b>Title:</b> DefaultDOReplicator.java</p>
  * <p><b>Description:</b> A Module that replicates digital object information
  * to the dissemination database.</p>
  *
  * <p>Converts data read from the object reader interfaces and creates or
  * updates the corresponding database rows in the dissemination database.</p>
  *
  * -----------------------------------------------------------------------------
  *
  * <p><b>License and Copyright: </b>The contents of this file are subject to the
  * Mozilla Public License Version 1.1 (the "License"); you may not use this file
  * except in compliance with the License. You may obtain a copy of the License
  * at <a href="http://www.mozilla.org/MPL">http://www.mozilla.org/MPL/.</a></p>
  *
  * <p>Software distributed under the License is distributed on an "AS IS" basis,
  * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
  * the specific language governing rights and limitations under the License.</p>
  *
  * <p>The entire file consists of original code.  Copyright &copy; 2002, 2003 by The
  * Rector and Visitors of the University of Virginia and Cornell University.
  * All rights reserved.</p>
  *
  * -----------------------------------------------------------------------------
  *
  * @author Paul Charlton, cwilper@cs.cornell.edu
  * @version $Id$
  */
 public class DefaultDOReplicator
         extends Module
         implements DOReplicator {
     private ConnectionPool m_pool;
     private RowInsertion m_ri;
     private DBIDLookup m_dl;
 
     // sdp - local.fedora.server conversion
     private Pattern hostPattern = null;
     private Pattern hostPortPattern = null;
     private Pattern serializedLocalURLPattern = null;
 
     /**
      * Server instance to work with in this module.
      */
     private Server server;
 
     /** Port number on which the Fedora server is running; determined from
      * fedora.fcfg config file.
      */
     private static String fedoraServerPort = null;
 
     /** Hostname of the Fedora server determined from
      * fedora.fcfg config file, or (fallback) by hostIP.getHostName()
      */
     private static String fedoraServerHost = null;
 
     /** The IP address of the local host; determined dynamically. */
     private static InetAddress hostIP = null;
 
 
     public DefaultDOReplicator(Map moduleParameters, Server server, String role)
             throws ModuleInitializationException {
         super(moduleParameters, server, role);
     }
 
     public void initModule() {
     }
 
     public void postInitModule()
             throws ModuleInitializationException {
         try {
             ConnectionPoolManager mgr=(ConnectionPoolManager)
                     getServer().getModule(
                     "fedora.server.storage.ConnectionPoolManager");
             m_pool=mgr.getPool();
 
             // sdp: insert new
             hostIP = null;
             fedoraServerPort = getServer().getParameter("fedoraServerPort");
             getServer().logFinest("fedoraServerPort: " + fedoraServerPort);
             hostIP = InetAddress.getLocalHost();
             fedoraServerHost = getServer().getParameter("fedoraServerHost");
             if (fedoraServerHost==null || fedoraServerHost.equals("")) {
                 fedoraServerHost=hostIP.getHostName();
             }
             hostPattern = Pattern.compile("http://"+fedoraServerHost+"/");
             hostPortPattern = Pattern.compile("http://"+fedoraServerHost+":"+fedoraServerPort+"/");
             serializedLocalURLPattern = Pattern.compile("http://local.fedora.server/");
             //System.out.println("Replicator: hostPattern is " + hostPattern.pattern());
             //System.out.println("Replicator: hostPortPattern is " + hostPortPattern.pattern());
             //
         } catch (ServerException se) {
             throw new ModuleInitializationException(
                     "Error getting default pool: " + se.getMessage(),
                     getRole());
         } catch (UnknownHostException se) {
             throw new ModuleInitializationException(
                     "Error determining hostIP address: " + se.getMessage(),
                     getRole());
         }
     }
 
     /**
      * If the object has already been replicated, update the components
      * and return true.  Otherwise, return false.
      *
      * @ids=select doDbID from do where doPID='demo:5'
      * if @ids.size=0, return false
      * else:
      *     foreach $id in @ids
      *         ds=reader.getDatastream(id, null)
      *         update dsBind set dsLabel='mylabel', dsLocation='' where dsID='$id'
      *
      * // currentVersionId?
      *
      * @param reader a digital object reader.
      * @return true is successful update; false oterhwise.
      * @throws ReplicationException if replication fails for any reason.
      */
     private boolean updateComponents(DOReader reader)
             throws ReplicationException {
         Connection connection=null;
         Statement st=null;
         ResultSet results=null;
         boolean triedUpdate=false;
         boolean failed=false;
         logFinest("DefaultDOReplicator.updateComponents: Entering ------");
         try {
             connection=m_pool.getConnection();
             st=connection.createStatement();
 
             // get db ID for the digital object
             results=logAndExecuteQuery(st, "SELECT doDbID,doState FROM do WHERE "
                     + "doPID='" + reader.GetObjectPID() + "'");
             if (!results.next()) {
                 logFinest("DefaultDOReplication.updateComponents: Object is "
                         + "new; components dont need updating.");
                 return false;
             }
             int doDbID=results.getInt("doDbID");
             String doState=results.getString("doState");
             results.close();
             ArrayList updates=new ArrayList();
 
             // check if state has changed for the digital object
             String objState = reader.GetObjectState();
             if (!doState.equalsIgnoreCase(objState)) {
               updates.add("UPDATE do SET doState='"+objState+"' WHERE doDbID=" + doDbID);
               updates.add("UPDATE doRegistry SET objectState='"+objState+"' WHERE doPID='" + reader.GetObjectPID() + "'");
             }
 
             // check if any mods to datastreams for this digital object
             results=logAndExecuteQuery(st, "SELECT dsID, dsLabel, dsLocation, dsCurrentVersionID, dsState "
                     + "FROM dsBind WHERE doDbID=" + doDbID);
             while (results.next()) {
                 String dsID=results.getString("dsID");
                 String dsLabel=results.getString("dsLabel");
                 String dsCurrentVersionID=results.getString("dsCurrentVersionID");
                 String dsState=results.getString("dsState");
                 // sdp - local.fedora.server conversion
                 String dsLocation=unencodeLocalURL(results.getString("dsLocation"));
                 // compare the latest version of the datastream to what's in the db...
                 // if different, add to update list
                 Datastream ds=reader.GetDatastream(dsID, null);
                 if (!ds.DSLabel.equals(dsLabel)
                         || !ds.DSLocation.equals(dsLocation)
                         || !ds.DSVersionID.equals(dsCurrentVersionID)
                         || !ds.DSState.equals(dsState)) {
                     updates.add("UPDATE dsBind SET dsLabel='"
                             + SQLUtility.aposEscape(ds.DSLabel) + "', dsLocation='"
                             // sdp - local.fedora.server conversion
                             + SQLUtility.aposEscape(encodeLocalURL(ds.DSLocation))
                             + "', dsCurrentVersionID='" + ds.DSVersionID + "', "
                             + "dsState='" + ds.DSState + "' "
                             + " WHERE doDbID=" + doDbID + " AND dsID='" + dsID + "'");
                 }
             }
             results.close();
             // do any required updates via a transaction
             if (updates.size()>0) {
                 connection.setAutoCommit(false);
                 triedUpdate=true;
                 for (int i=0; i<updates.size(); i++) {
                     String update=(String) updates.get(i);
                     logAndExecuteUpdate(st, update);
                 }
                 connection.commit();
             } else {
                 logFinest("No datastream labels or locations changed.");
             }
 
             // check if any mods to disseminators for this object...
             // first get a list of disseminator db IDs
             results=logAndExecuteQuery(st, "SELECT dissDbID FROM doDissAssoc WHERE "
                     + "doDbID="+doDbID);
             HashSet dissDbIDs = new HashSet();
             while (results.next()) {
               dissDbIDs.add(new Integer(results.getInt("dissDbID")));
             }
 
            if (dissDbIDs.size()==0) {
                 logFinest("DefaultDOReplication.updateComponents: Object "
                         + "has no disseminators; components dont need updating.");
                 return false;
             }
             results.close();
 
             Iterator dissIter = dissDbIDs.iterator();
             // Iterate over disseminators to check if any have been modified
             while(dissIter.hasNext()) {
               Integer dissDbID = (Integer) dissIter.next();
               logFinest("Iterating, dissDbID: "+dissDbID);
 
               // get disseminator info for this disseminator
               results=logAndExecuteQuery(st, "SELECT diss.bDefDbID, diss.bMechDbID, bMech.bMechPID, diss.dissID, diss.dissLabel, diss.dissState "
                   + "FROM diss,bMech WHERE bMech.bMechDbID=diss.bMechDbID AND diss.dissDbID=" + dissDbID);
               updates=new ArrayList();
               int bDefDbID = 0;
               int bMechDbID = 0;
               String dissID=null;
               String dissLabel=null;
               String dissState=null;
               String bMechPID=null;
               while(results.next()) {
                 bDefDbID = results.getInt("bDefDbID");
                 bMechDbID = results.getInt("bMechDbID");
                 dissID=results.getString("dissID");
                 dissLabel=results.getString("dissLabel");
                 dissState=results.getString("dissState");
                 bMechPID=results.getString("bMechPID");
               }
               results.close();
 
               // compare the latest version of the disseminator with what's in the db...
               // replace what's in db if they are different
               Disseminator diss=reader.GetDisseminator(dissID, null);
               if (diss == null) {
                 // xml object has no disseminators
                 // so this must be a purgeComponents or a new object
                 logFinest("DefaultDOReplicator.updateComponents: XML object has no disseminators");
                 return false;
               }
               if (!diss.dissLabel.equals(dissLabel)
                   || !diss.bMechID.equals(bMechPID)
                   || !diss.dissState.equals(dissState)) {
                 if (!diss.dissLabel.equals(dissLabel))
                     logFinest("dissLabel changed from '" + dissLabel + "' to '" + diss.dissLabel + "'");
                 if (!diss.dissState.equals(dissState))
                     logFinest("dissState changed from '" + dissState + "' to '" + diss.dissState + "'");
                 // we might need to set the bMechDbID to the id for the new one,
                 // if the mechanism changed
                 int newBMechDbID;
                 if (diss.bMechID.equals(bMechPID)) {
                   newBMechDbID=bMechDbID;
                 } else {
                   logFinest("bMechPID changed from '" + bMechPID + "' to '" + diss.bMechID + "'");
                   results=logAndExecuteQuery(st, "SELECT bMechDbID "
                                                + "FROM bMech "
                                                + "WHERE bMechPID='"
                                                        + diss.bMechID + "'");
                   if (!results.next()) {
                     // shouldn't have gotten this far, but if so...
                     throw new ReplicationException("The behavior mechanism "
                         + "changed to " + diss.bMechID + ", but there is no "
                         + "record of that object in the dissemination db.");
                   }
                   newBMechDbID=results.getInt("bMechDbID");
                   results.close();
                 }
                 // update the diss table with all new, correct values
                 logAndExecuteUpdate(st, "UPDATE diss SET dissLabel='"
                     + SQLUtility.aposEscape(diss.dissLabel)
                     + "', bMechDbID=" + newBMechDbID + ", "
                     + "dissID='" + diss.dissID + "', "
                     + "dissState='" + diss.dissState + "' "
                     + " WHERE dissDbID=" + dissDbID + " AND bDefDbID=" + bDefDbID + " AND bMechDbID=" + bMechDbID);
               }
 
               // compare the latest version of the disseminator's bindMap with what's in the db
               // and replace what's in db if they are different
               results=logAndExecuteQuery(st, "SELECT DISTINCT dsBindMap.dsBindMapID,dsBindMap.dsBindMapDbID FROM dsBind,dsBindMap WHERE "
                   + "dsBind.doDbID=" + doDbID + " AND dsBindMap.dsBindMapDbID=dsBind.dsBindMapDbID "
                   + "AND dsBindMap.bMechDbID="+bMechDbID);
               String origDSBindMapID=null;
               int origDSBindMapDbID=0;
               while (results.next()) {
                 origDSBindMapID=results.getString("dsBindMapID");
                 origDSBindMapDbID=results.getInt("dsBindMapDbID");
               }
               results.close();
               String newDSBindMapID=diss.dsBindMapID;
               if (!newDSBindMapID.equals(origDSBindMapID)) {
                 // dsBindingMap was modified so remove original bindingMap and datastreams.
                 // BindingMaps can be shared by other objects so first check to see if
                 // the orignial bindingMap is bound to datastreams of any other objects.
                 Statement st2 = connection.createStatement();
                 results=logAndExecuteQuery(st2,"SELECT DISTINCT doDbId,dsBindMapDbId FROM dsBind WHERE dsBindMapDbId="+origDSBindMapDbID);
                 int numRows = 0;
                 while (results.next()) {
                   numRows++;
                 }
                 st2.close();
                 results.close();
                 if(numRows == 1) {
                   // The bindingMap is NOT shared by any other objects and can be removed.
                   int rowCount = logAndExecuteUpdate(st, "DELETE FROM dsBindMap WHERE dsBindMapDbID=" + origDSBindMapDbID);
                   logFinest("deleted "+rowCount+" rows from dsBindMapDbID");
                 } else {
                   // The bindingMap IS shared by other objects so leave bindingMap untouched.
                   logFinest("dsBindMapID: "+origDSBindMapID+" is shared by other objects; it will NOT be deleted");
                 }
                 int rowCount = logAndExecuteUpdate(st,"DELETE FROM dsBind WHERE doDbID=" + doDbID);
                 logFinest("deleted "+rowCount+" rows from dsBind");
 
                 // now add back new datastreams and dsBindMap associated with this disseminator
                 DSBindingMapAugmented[] allBindingMaps;
                 Disseminator disseminators[];
                 String bDefDBID;
                 String bindingMapDBID;
                 String bMechDBID;
                 String dissDBID;
                 String doDBID;
                 String doPID;
                 String doLabel;
                 String dsBindingKeyDBID;
                 allBindingMaps = reader.GetDSBindingMaps(null);
                 for (int i=0; i<allBindingMaps.length; ++i) {
                   // only update bindingMap that was modified
                   if (allBindingMaps[i].dsBindMapID.equals(newDSBindMapID)) {
                     bMechDBID = lookupBehaviorMechanismDBID(connection,
                         allBindingMaps[i].dsBindMechanismPID);
                     if (bMechDBID == null) {
                       throw new ReplicationException("BehaviorMechanism row "
                           + "doesn't exist for PID: "
                           + allBindingMaps[i].dsBindMechanismPID);
                     }
 
                     // Insert dsBindMap row if it doesn't exist.
                     bindingMapDBID = lookupDataStreamBindingMapDBID(connection,
                         bMechDBID, allBindingMaps[i].dsBindMapID);
                     if (bindingMapDBID == null) {
                       // DataStreamBinding row doesn't exist, add it.
                       insertDataStreamBindingMapRow(connection, bMechDBID,
                           allBindingMaps[i].dsBindMapID,
                           allBindingMaps[i].dsBindMapLabel);
                           bindingMapDBID = lookupDataStreamBindingMapDBID(
                               connection,bMechDBID,allBindingMaps[i].dsBindMapID);
                           if (bindingMapDBID == null) {
                             throw new ReplicationException(
                                 "lookupdsBindMapDBID row "
                                 + "doesn't exist for bMechDBID: " + bMechDBID
                                 + ", dsBindingMapID: "
                                 + allBindingMaps[i].dsBindMapID);
                           }
                     }
                     logFinest("augmentedlength: "+allBindingMaps[i].dsBindingsAugmented.length);
                     for (int j=0; j<allBindingMaps[i].dsBindingsAugmented.length;
                          ++j) {
                       logFinest("j: "+j);
                       dsBindingKeyDBID = lookupDataStreamBindingSpecDBID(
                           connection, bMechDBID,
                           allBindingMaps[i].dsBindingsAugmented[j].
                           bindKeyName);
                       if (dsBindingKeyDBID == null) {
                         throw new ReplicationException(
                             "lookupDataStreamBindingDBID row doesn't "
                             + "exist for bMechDBID: " + bMechDBID
                             + ", bindKeyName: " + allBindingMaps[i].
                             dsBindingsAugmented[j].bindKeyName + "i=" + i
                             + " j=" + j);
                       }
 
                       // Insert DataStreamBinding row
                       insertDataStreamBindingRow(connection, new Integer(doDbID).toString(),
                           dsBindingKeyDBID,
                           bindingMapDBID,
                           allBindingMaps[i].dsBindingsAugmented[j].seqNo,
                           allBindingMaps[i].dsBindingsAugmented[j].
                           datastreamID,
                           allBindingMaps[i].dsBindingsAugmented[j].DSLabel,
                           allBindingMaps[i].dsBindingsAugmented[j].DSMIME,
                           // sdp - local.fedora.server conversion
                           encodeLocalURL(allBindingMaps[i].dsBindingsAugmented[j].DSLocation),
                           allBindingMaps[i].dsBindingsAugmented[j].DSControlGrp,
                           allBindingMaps[i].dsBindingsAugmented[j].DSVersionID,
                           "1",
                           "A");
 
                     }
                   }
                 }
               }
             }
 
         } catch (SQLException sqle) {
             failed=true;
             throw new ReplicationException("An error has occurred during "
                 + "Replication. The error was \" " + sqle.getClass().getName()
                 + " \". The cause was \" " + sqle.getMessage());
         } catch (ServerException se) {
             failed=true;
             throw new ReplicationException("An error has occurred during "
                 + "Replication. The error was \" " + se.getClass().getName()
                 + " \". The cause was \" " + se.getMessage());
         } catch (Exception e) {
           e.printStackTrace();
         } finally {
             if (connection!=null) {
                 try {
                     if (triedUpdate && failed) connection.rollback();
                 } catch (Throwable th) {
                     logWarning("While rolling back: " +  th.getClass().getName()
                             + ": " + th.getMessage());
                 } finally {
                     try {
                         if (results != null) results.close();
                         if (st!=null) st.close();
                         connection.setAutoCommit(true);
                     } catch (SQLException sqle) {
                         logWarning("While cleaning up: " +  sqle.getClass().getName()
                             + ": " + sqle.getMessage());
                     } finally {
                         m_pool.free(connection);
                     }
                 }
             }
         }
         logFinest("DefaultDOReplicator.updateComponents: Exiting ------");
         return true;
     }
 
 	private boolean addNewComponents(DOReader reader)
 			throws ReplicationException {
 
 		Connection connection=null;
 		Statement st=null;
 		ResultSet results=null;
 		boolean failed=false;
                 logFinest("DefaultDOReplicator.addNewComponents: Entering ------");
 		try {
 
 			String doPID = reader.GetObjectPID();
 			connection=m_pool.getConnection();
 			connection.setAutoCommit(false);
 			st=connection.createStatement();
 
 			// get db ID for the digital object
 			results=logAndExecuteQuery(st, "SELECT doDbID FROM do WHERE "
 					+ "doPID='" + doPID + "'");
 			if (!results.next()) {
 				logFinest("DefaultDOReplication.addNewComponents: Object is "
 						+ "new; components will be added as part of new object replication.");
 				return false;
 			}
 			int doDbID=results.getInt("doDbID");
 			results.close();
 
 			Disseminator[] dissArray = reader.GetDisseminators(null, null);
 			HashSet newDisseminators = new HashSet();
 			for (int j=0; j< dissArray.length; j++)
 			{
 				// Find disseminators that are NEW within an existing object
 				// (disseminator does not already exist in the database)
 				results=logAndExecuteQuery(st, "SELECT diss.dissDbID"
 					+ " FROM doDissAssoc, diss"
 					+ " WHERE doDissAssoc.doDbID=" + doDbID + " AND diss.dissID='" + dissArray[j].dissID + "'"
 					+ " AND doDissAssoc.dissDbID=diss.dissDbID");
 				if (!results.next()) {
 					// the disseminator does NOT exist in the database; it is NEW.
 					newDisseminators.add(dissArray[j]);
 				}
 			}
 			addDisseminators(doPID, (Disseminator[])newDisseminators.toArray(new Disseminator[0]), reader, connection);
 			connection.commit();
 		} catch (SQLException sqle) {
 			failed=true;
 			throw new ReplicationException("An error has occurred during "
 				+ "Replication. The error was \" " + sqle.getClass().getName()
 				+ " \". The cause was \" " + sqle.getMessage());
 		} catch (ServerException se) {
 			failed=true;
 			throw new ReplicationException("An error has occurred during "
 				+ "Replication. The error was \" " + se.getClass().getName()
 				+ " \". The cause was \" " + se.getMessage());
 		} catch (Exception e) {
 		  e.printStackTrace();
 		} finally {
 			// TODO: make sure this makes sense here
 			if (connection!=null) {
 				try {
 					if (failed) connection.rollback();
 				} catch (Throwable th) {
 					logWarning("While rolling back: " +  th.getClass().getName()
 							+ ": " + th.getMessage());
 				} finally {
 					try {
 						if (results != null) results.close();
 						if (st!=null) st.close();
 						connection.setAutoCommit(true);
 					} catch (SQLException sqle) {
 						logWarning("While cleaning up: " +  sqle.getClass().getName()
 							+ ": " + sqle.getMessage());
 					} finally {
 						m_pool.free(connection);
 					}
 				}
 			}
 		}
                 logFinest("DefaultDOReplicator.addNewComponents: Exiting ------");
 		return true;
 	}
 
         /**
          * <p> Removes components of a digital object from the database.</p>
          *
          * @param reader an instance a DOReader.
          * @return True if the removal was successfult; false otherwise.
          * @throws ReplicationException If any type of error occurs during the removal.
          */
         private boolean purgeComponents(DOReader reader)
                         throws ReplicationException {
 
                 Connection connection=null;
                 Statement st=null;
                 ResultSet results=null;
                 boolean failed=false;
                 logFinest("DefaultDOReplication.purgeComponents: Entering -----");
                 try {
 
                         String doPID = reader.GetObjectPID();
                         connection=m_pool.getConnection();
                         connection.setAutoCommit(false);
                         st=connection.createStatement();
 
                         // get db ID for the digital object
                         results=logAndExecuteQuery(st, "SELECT doDbID FROM do WHERE "
                                         + "doPID='" + doPID + "'");
                         if (!results.next()) {
                                 logFinest("DefaultDOReplication.purgeComponents: Object is "
                                                 + "new; components will be added as part of new object replication.");
                                 return false;
                         }
                         int doDbID=results.getInt("doDbID");
                         results.close();
 
                         // Get all disseminators that are in db for this object
                         HashSet dissDbIds = new HashSet();
                         results=logAndExecuteQuery(st, "SELECT dissDbID"
                             + " FROM doDissAssoc"
                             + " WHERE doDbID=" + doDbID);
                         while (results.next()) {
                           Integer id = new Integer(results.getInt("dissDbID"));
                           dissDbIds.add(id);
                         }
                         results.close();
                         logFinest("DefaultDOReplicator.purgeComponents: Found "
                             + dissDbIds.size() + "dissDbId(s). ");
 
                         // Get all binding maps that are in db for this object
                         HashSet dsBindMapIds = new HashSet();
                         results=logAndExecuteQuery(st, "SELECT DISTINCT dsBindMapDbID "
                             + " FROM dsBind WHERE doDbID=" + doDbID);
                         while (results.next()) {
                           Integer id = new Integer(results.getInt("dsBindMapDbID"));
                           dsBindMapIds.add(id);
                         }
                         results.close();
                         logFinest("DefaultDOReplicator.purgeComponents: Found "
                             + dsBindMapIds.size() + "dsBindMapDbId(s). ");
 
                         // Now get all existing disseminators that are in xml object for this object
                         Disseminator[] dissArray = reader.GetDisseminators(null, null);
                         HashSet existingDisseminators = new HashSet();
                         HashSet purgedDisseminators = new HashSet();
                         for (int j=0; j< dissArray.length; j++)
                         {
                                 // Find disseminators that have been removed within an existing object
                                 // (disseminator(s) still exist in the database)
                                 results=logAndExecuteQuery(st, "SELECT diss.dissDbID"
                                     + " FROM doDissAssoc, diss"
                                     + " WHERE doDissAssoc.doDbID=" + doDbID + " AND diss.dissID='" + dissArray[j].dissID + "'"
                                         + " AND doDissAssoc.dissDbID=diss.dissDbID");
 
                                 if (!results.next()) {
                                   // No disseminator was found in db so it must be new one
                                   // indicating an instance of AddNewComponents rather than purgeComponents
                                   logFinest("DefaultDOReplicator.purgeComponents: Disseminator not found in db; Assuming this is case of AddNewComponents");
                                   return false;
                                 } else {
                                   Integer id = new Integer(results.getInt("dissDbID"));
                                   existingDisseminators.add(id);
                                   logFinest("DefaultDOReplicator.purgeComponents: Adding "
                                       + " dissDbId: " + id + " to list of Existing dissDbId(s). ");
                                 }
                                 results.close();
                         }
                         logFinest("DefaultDOReplicator.purgeComponents: Found "
                             + existingDisseminators.size() + " existing dissDbId(s). ");
 
                         // Now get all existing dsbindmapids that are in xml object for this object
                         HashSet existingDsBindMapIds = new HashSet();
                         HashSet purgedDsBindMapIds = new HashSet();
                         for (int j=0; j< dissArray.length; j++)
                         {
                                 // Find disseminators that have been removed within an existing object
                                 // (disseminator(s) still exist in the database)
                                 results=logAndExecuteQuery(st, "SELECT dsBindMapDbID, dsBindMapID"
                                     + " FROM dsBindMap,bMech,diss"
                                     + " WHERE dsBindMap.bmechDbID=bMech.bmechDbID AND bMech.bmechPID='" + dissArray[j].bMechID + "' "
                                     + " AND diss.dissID='" + dissArray[j].dissID + "' AND dsBindMapID='" + dissArray[j].dsBindMapID + "'");
 
                                 if (!results.next()) {
                                   // No disseminator was found in db so it must be new one
                                   // indicating an instance of AddNewComponents rather than purgeComponents
                                   logFinest("DefaultDOReplicator.purgeComponents: Disseminator not found in db; Assuming this is case of AddNewComponents");
                                   return false;
                                 } else {
                                   Integer dsBindMapDbId = new Integer(results.getInt("dsBindMapDbID"));
                                   String dsBindMapID = results.getString("dsBindMapID");
                                   existingDsBindMapIds.add(dsBindMapDbId);
                                   logFinest("DefaultDOReplicator.purgeComponents: Adding "
                                       + " dsBindMapDbId: " + dsBindMapDbId + " to list of Existing dsBindMapDbId(s). ");
                                 }
                                 results.close();
                         }
                         logFinest("DefaultDOReplicator.purgeComponents: Found "
                             + existingDsBindMapIds.size() + " existing dsBindMapDbId(s). ");
 
                         // Compare what's in db with what's in xml object
                         Iterator dissDbIdIter = dissDbIds.iterator();
                         Iterator existingDissIter = existingDisseminators.iterator();
                         while (dissDbIdIter.hasNext()) {
                           Integer dissDbId = (Integer) dissDbIdIter.next();
                           if (existingDisseminators.contains(dissDbId)) {
                             // database disseminator exists in xml object
                             // so ignore
                           } else {
                             // database disseminator does not exist in xml object
                             // so remove it from database
                             purgedDisseminators.add(dissDbId);
                             logFinest("DefaultDOReplicator.purgeComponents: Adding "
                                       + " dissDbId: " + dissDbId + " to list of Purged dissDbId(s). ");
                           }
                         }
                         if (purgedDisseminators.isEmpty()) {
                           // no disseminators were removed so this must be an
                           // an instance of addComponent or updateComponent
                           logFinest("DefaultDOReplicator.purgeComponents: "
                               + "No disseminators have been removed from object;"
                               + " Assuming this a case of UpdateComponents");
                           return false;
                         }
 
                         // Compare what's in db with what's in xml object
                         Iterator dsBindMapIdIter = dsBindMapIds.iterator();
                         Iterator existingDsBindMapIdIter = existingDsBindMapIds.iterator();
                         while (dsBindMapIdIter.hasNext()) {
                           Integer dsBindMapDbId = (Integer) dsBindMapIdIter.next();
                           if (existingDsBindMapIds.contains(dsBindMapDbId)) {
                             // database disseminator exists in xml object
                             // so ignore
                           } else {
                             // database disseminator does not exist in xml object
                             // so remove it from database
                             purgedDsBindMapIds.add(dsBindMapDbId);
                             logFinest("DefaultDOReplicator.purgeComponents: Adding "
                                       + " dsBindMapDbId: " + dsBindMapDbId + " to list of Purged dsBindMapDbId(s). ");
                           }
                         }
                         if (purgedDsBindMapIds.isEmpty()) {
                           // no disseminators were removed so this must be an
                           // an instance of addComponent or updateComponent
                           logFinest("DefaultDOReplicator.purgeComponents: "
                               + "No disseminators have been removed from object;"
                               + " Assuming this a case of UpdateComponents");
                           return false;
                         }
 
                         purgeDisseminators(doPID, purgedDisseminators, purgedDsBindMapIds, reader, connection);
                         connection.commit();
                 } catch (SQLException sqle) {
                         failed=true;
                         throw new ReplicationException("An error has occurred during "
                                 + "Replication. The error was \" " + sqle.getClass().getName()
                                 + " \". The cause was \" " + sqle.getMessage());
                 } catch (ServerException se) {
                         failed=true;
                         throw new ReplicationException("An error has occurred during "
                                 + "Replication. The error was \" " + se.getClass().getName()
                                 + " \". The cause was \" " + se.getMessage());
                 } finally {
                         // TODO: make sure this makes sense here
                         if (connection!=null) {
                                 try {
                                         if (failed) connection.rollback();
                                 } catch (Throwable th) {
                                         logWarning("While rolling back: " +  th.getClass().getName()
                                                         + ": " + th.getMessage());
                                 } finally {
                                         try {
                                                 if (results != null) results.close();
                                                 if (st!=null) st.close();
                                                 connection.setAutoCommit(true);
                                         } catch (SQLException sqle) {
                                                 logWarning("While cleaning up: " +  sqle.getClass().getName()
                                                         + ": " + sqle.getMessage());
                                         } finally {
                                                 m_pool.free(connection);
                                         }
                                 }
                         }
                 }
                 logFinest("DefaultDOReplicator.purgeComponents: Exiting ------");
                 return true;
 	}
 
     /**
      * If the object has already been replicated, update the components
      * and return true.  Otherwise, return false.
      *
      * Currently bdef components cannot be updated, so this will
      * simply return true if the bDef has already been replicated.
      *
      * @param reader a behavior definitionobject reader.
      * @return true if bdef update successful; false otherwise.
      * @throws ReplicationException if replication fails for any reason.
      */
     private boolean updateComponents(BDefReader reader)
             throws ReplicationException {
         Connection connection=null;
         Statement st=null;
         ResultSet results=null;
         boolean triedUpdate=false;
         boolean failed=false;
         try {
             connection=m_pool.getConnection();
             st=connection.createStatement();
             results=logAndExecuteQuery(st, "SELECT bDefDbID,bDefState FROM bDef WHERE "
                     + "bDefPID='" + reader.GetObjectPID() + "'");
             if (!results.next()) {
                 logFinest("DefaultDOReplication.updateComponents: Object is "
                         + "new; components dont need updating.");
                 return false;
             }
 
             int bDefDbID=results.getInt("bDefDbID");
             String bDefState=results.getString("bDefState");
             results.close();
             ArrayList updates=new ArrayList();
             // check if state has changed for the bdef object
             String objState = reader.GetObjectState();
             if (!bDefState.equalsIgnoreCase(objState)) {
               updates.add("UPDATE bDef SET bDefState='"+objState+"' WHERE bDefDbID=" + bDefDbID);
               updates.add("UPDATE doRegistry SET objectState='"+objState+"' WHERE doPID='" + reader.GetObjectPID() + "'");
             }
 
             // do any required updates via a transaction
             if (updates.size()>0) {
                 connection.setAutoCommit(false);
                 triedUpdate=true;
                 for (int i=0; i<updates.size(); i++) {
                     String update=(String) updates.get(i);
                     logAndExecuteUpdate(st, update);
                 }
                 connection.commit();
             } else {
                 logFinest("No datastream labels or locations changed.");
             }
 
         } catch (SQLException sqle) {
             failed=true;
             throw new ReplicationException("An error has occurred during "
                 + "Replication. The error was \" " + sqle.getClass().getName()
                 + " \". The cause was \" " + sqle.getMessage());
         } catch (ServerException se) {
             failed=true;
             throw new ReplicationException("An error has occurred during "
                 + "Replication. The error was \" " + se.getClass().getName()
                 + " \". The cause was \" " + se.getMessage());
         } finally {
             if (connection!=null) {
                 try {
                     if (triedUpdate && failed) connection.rollback();
                 } catch (Throwable th) {
                     logWarning("While rolling back: " +  th.getClass().getName()
                             + ": " + th.getMessage());
                 } finally {
                     try {
                         if (results != null) results.close();
                         if (st!=null) st.close();
                         connection.setAutoCommit(true);
                     } catch (SQLException sqle) {
                         logWarning("While cleaning up: " +  sqle.getClass().getName()
                             + ": " + sqle.getMessage());
                     } finally {
                         m_pool.free(connection);
                     }
                 }
             }
         }
         return true;
     }
 
     /**
      * If the object has already been replicated, update the components
      * and return true.  Otherwise, return false.
      *
      * Currently bmech components cannot be updated, so this will
      * simply return true if the bMech has already been replicated.
      *
      * @param reader a behavior mechanism object reader.
      * @return true if bmech update successful; false otherwise.
      * @throws ReplicationException if replication fails for any reason.
      */
     private boolean updateComponents(BMechReader reader)
             throws ReplicationException {
         Connection connection=null;
         Statement st=null;
         ResultSet results=null;
         boolean triedUpdate=false;
         boolean failed=false;
         try {
             connection=m_pool.getConnection();
             st=connection.createStatement();
             results=logAndExecuteQuery(st, "SELECT bMechDbID,bMechState FROM bMech WHERE "
                     + "bMechPID='" + reader.GetObjectPID() + "'");
             if (!results.next()) {
                 logFinest("DefaultDOReplication.updateComponents: Object is "
                         + "new; components dont need updating.");
                 return false;
             }
             int bMechDbID=results.getInt("bMechDbID");
             String bMechState=results.getString("bMechState");
             results.close();
             ArrayList updates=new ArrayList();
             // check if state has changed for the bdef object
             String objState = reader.GetObjectState();
             if (!bMechState.equalsIgnoreCase(objState)) {
               updates.add("UPDATE bMech SET bMechState='"+objState+"' WHERE bMechDbID=" + bMechDbID);
               updates.add("UPDATE doRegistry SET objectState='"+objState+"' WHERE doPID='" + reader.GetObjectPID() + "'");
             }
 
             // do any required updates via a transaction
             if (updates.size()>0) {
                 connection.setAutoCommit(false);
                 triedUpdate=true;
                 for (int i=0; i<updates.size(); i++) {
                     String update=(String) updates.get(i);
                     logAndExecuteUpdate(st, update);
                 }
                 connection.commit();
             } else {
                 logFinest("No datastream labels or locations changed.");
             }
 
         } catch (SQLException sqle) {
             failed=true;
             throw new ReplicationException("An error has occurred during "
                 + "Replication. The error was \" " + sqle.getClass().getName()
                 + " \". The cause was \" " + sqle.getMessage());
         } catch (ServerException se) {
             failed=true;
             throw new ReplicationException("An error has occurred during "
                 + "Replication. The error was \" " + se.getClass().getName()
                 + " \". The cause was \" " + se.getMessage());
         } finally {
             if (connection!=null) {
                 try {
                     if (triedUpdate && failed) connection.rollback();
                 } catch (Throwable th) {
                     logWarning("While rolling back: " +  th.getClass().getName()
                             + ": " + th.getMessage());
                 } finally {
                     try {
                         if (results != null) results.close();
                         if (st!=null) st.close();
                         connection.setAutoCommit(true);
                     } catch (SQLException sqle) {
                         logWarning("While cleaning up: " +  sqle.getClass().getName()
                             + ": " + sqle.getMessage());
                     } finally {
                         m_pool.free(connection);
                     }
                 }
             }
         }
         return true;
     }
 
     /**
      * Replicates a Fedora behavior definition object.
      *
      * @param bDefReader behavior definition reader
      * @exception ReplicationException replication processing error
      * @exception SQLException JDBC, SQL error
      */
     public void replicate(BDefReader bDefReader)
             throws ReplicationException, SQLException {
         if (!updateComponents(bDefReader)) {
             Connection connection=null;
             try {
                 MethodDef behaviorDefs[];
                 String bDefDBID;
                 String bDefPID;
                 String bDefLabel;
                 String methDBID;
                 String methodName;
                 String parmRequired;
                 String[] parmDomainValues;
                 connection = m_pool.getConnection();
                 connection.setAutoCommit(false);
 
                 // Insert Behavior Definition row
                 bDefPID = bDefReader.GetObjectPID();
                 bDefLabel = bDefReader.GetObjectLabel();
                 insertBehaviorDefinitionRow(connection, bDefPID, bDefLabel, bDefReader.GetObjectState());
 
                 // Insert method rows
                 bDefDBID = lookupBehaviorDefinitionDBID(connection, bDefPID);
                 if (bDefDBID == null) {
                     throw new ReplicationException(
                         "BehaviorDefinition row doesn't exist for PID: "
                         + bDefPID);
                 }
                 behaviorDefs = bDefReader.getAbstractMethods(null);
                 for (int i=0; i<behaviorDefs.length; ++i) {
                     insertMethodRow(connection, bDefDBID,
                             behaviorDefs[i].methodName,
                             behaviorDefs[i].methodLabel);
 
                     // Insert method parm rows
                     methDBID =  lookupMethodDBID(connection, bDefDBID,
                         behaviorDefs[i].methodName);
                     for (int j=0; j<behaviorDefs[i].methodParms.length; j++)
                     {
                       MethodParmDef[] methodParmDefs =
                       new MethodParmDef[behaviorDefs[i].methodParms.length];
                       methodParmDefs = behaviorDefs[i].methodParms;
                       parmRequired =
                                methodParmDefs[j].parmRequired ? "true" : "false";
                       parmDomainValues = methodParmDefs[j].parmDomainValues;
                       StringBuffer sb = new StringBuffer();
                       if (parmDomainValues != null && parmDomainValues.length > 0)
                       {
                         for (int k=0; k<parmDomainValues.length; k++)
                         {
                           if (k < parmDomainValues.length-1)
                           {
                             sb.append(parmDomainValues[k]+",");
                           } else
                           {
                             sb.append(parmDomainValues[k]);
                           }
                       }
                       } else
                       {
                         sb.append("null");
                       }
                       insertMethodParmRow(connection, methDBID, bDefDBID,
                               methodParmDefs[j].parmName,
                               methodParmDefs[j].parmDefaultValue,
                               sb.toString(),
                               parmRequired,
                               methodParmDefs[j].parmLabel,
                               methodParmDefs[j].parmType);
                     }
                 }
                 connection.commit();
             } catch (ReplicationException re) {
                 throw re;
             } catch (ServerException se) {
                 throw new ReplicationException("Replication exception caused by "
                         + "ServerException - " + se.getMessage());
             } finally {
                 if (connection!=null) {
                     try {
                         connection.rollback();
                     } catch (Throwable th) {
                         logWarning("While rolling back: " +  th.getClass().getName() + ": " + th.getMessage());
                     } finally {
                         connection.setAutoCommit(true);
                         m_pool.free(connection);
                     }
                 }
             }
         }
     }
 
     /**
      * Replicates a Fedora behavior mechanism object.
      *
      * @param bMechReader behavior mechanism reader
      * @exception ReplicationException replication processing error
      * @exception SQLException JDBC, SQL error
      */
     public void replicate(BMechReader bMechReader)
             throws ReplicationException, SQLException {
         if (!updateComponents(bMechReader)) {
             Connection connection=null;
             try {
                 BMechDSBindSpec dsBindSpec;
                 MethodDefOperationBind behaviorBindings[];
                 MethodDefOperationBind behaviorBindingsEntry;
                 String bDefDBID;
                 String bDefPID;
                 String bMechDBID;
                 String bMechPID;
                 String bMechLabel;
                 String dsBindingKeyDBID;
                 String methodDBID;
                 String ordinality_flag;
                 String cardinality;
                 String[] parmDomainValues;
                 String parmRequired;
 
                 connection = m_pool.getConnection();
                 connection.setAutoCommit(false);
 
                 // Insert Behavior Mechanism row
                 dsBindSpec = bMechReader.getServiceDSInputSpec(null);
                 bDefPID = dsBindSpec.bDefPID;
 
                 bDefDBID = lookupBehaviorDefinitionDBID(connection, bDefPID);
                 if (bDefDBID == null) {
                     throw new ReplicationException("BehaviorDefinition row doesn't "
                             + "exist for PID: " + bDefPID);
                 }
 
                 bMechPID = bMechReader.GetObjectPID();
                 bMechLabel = bMechReader.GetObjectLabel();
 
                 insertBehaviorMechanismRow(connection, bDefDBID, bMechPID,
                         bMechLabel, bMechReader.GetObjectState());
 
                 // Insert dsBindSpec rows
                 bMechDBID = lookupBehaviorMechanismDBID(connection, bMechPID);
                 if (bMechDBID == null) {
                     throw new ReplicationException("BehaviorMechanism row doesn't "
                             + "exist for PID: " + bDefPID);
                 }
 
                 for (int i=0; i<dsBindSpec.dsBindRules.length; ++i) {
 
                   // Convert from type boolean to type String
                   ordinality_flag =
                            dsBindSpec.dsBindRules[i].ordinality ? "true" : "false";
                   // Convert from type int to type String
                   cardinality = Integer.toString(
                       dsBindSpec.dsBindRules[i].maxNumBindings);
 
                   insertDataStreamBindingSpecRow(connection,
                       bMechDBID, dsBindSpec.dsBindRules[i].bindingKeyName,
                       ordinality_flag, cardinality,
                       dsBindSpec.dsBindRules[i].bindingLabel);
 
                   // Insert dsMIME rows
                   dsBindingKeyDBID =
                       lookupDataStreamBindingSpecDBID(connection,
                       bMechDBID, dsBindSpec.dsBindRules[i].bindingKeyName);
                   if (dsBindingKeyDBID == null) {
                     throw new ReplicationException(
                         "dsBindSpec row doesn't exist for "
                         + "bMechDBID: " + bMechDBID
                         + ", binding key name: "
                         + dsBindSpec.dsBindRules[i].bindingKeyName);
                   }
 
                   for (int j=0;
                        j<dsBindSpec.dsBindRules[i].bindingMIMETypes.length;
                        ++j) {
                     insertDataStreamMIMERow(connection,
                         dsBindingKeyDBID,
                         dsBindSpec.dsBindRules[i].bindingMIMETypes[j]);
                   }
                 }
 
                 // Insert mechImpl rows
 
                 behaviorBindings = bMechReader.getServiceMethodBindings(null);
 
                 for (int i=0; i<behaviorBindings.length; ++i) {
                     behaviorBindingsEntry =
                             (MethodDefOperationBind)behaviorBindings[i];
 
                     if (!behaviorBindingsEntry.protocolType.equals("HTTP")) {
 
                       // For the time being, ignore bindings other than HTTP.
                       continue;
                     }
 
                     // Insert mechDefParm rows
                     methodDBID = lookupMethodDBID(connection, bDefDBID,
                             behaviorBindingsEntry.methodName);
                     if (methodDBID == null) {
                         throw new ReplicationException("Method row doesn't "
                                + "exist for method name: "
                                + behaviorBindingsEntry.methodName);
                     }
                     for (int j=0; j<behaviorBindings[i].methodParms.length; j++)
                     {
                       MethodParmDef[] methodParmDefs =
                           new MethodParmDef[behaviorBindings[i].methodParms.length];
                       methodParmDefs = behaviorBindings[i].methodParms;
                       //if (methodParmDefs[j].parmType.equalsIgnoreCase("fedora:defaultInputType"))
                       if (methodParmDefs[j].parmType.equalsIgnoreCase(MethodParmDef.DEFAULT_INPUT))
                       {
                       parmRequired =
                                methodParmDefs[j].parmRequired ? "true" : "false";
                       parmDomainValues = methodParmDefs[j].parmDomainValues;
                       StringBuffer sb = new StringBuffer();
                       if (parmDomainValues != null && parmDomainValues.length > 0)
                       {
                         for (int k=0; k<parmDomainValues.length; k++)
                         {
                           if (k < parmDomainValues.length-1)
                           {
                             sb.append(parmDomainValues[k]+",");
                           } else
                           {
                             sb.append(parmDomainValues[k]);
                           }
                       }
                       } else
                       {
                         if (sb.length() == 0) sb.append("null");
                       }
 
                       insertMechDefaultMethodParmRow(connection, methodDBID, bMechDBID,
                               methodParmDefs[j].parmName,
                               methodParmDefs[j].parmDefaultValue,
                               sb.toString(),
                               parmRequired,
                               methodParmDefs[j].parmLabel,
                               methodParmDefs[j].parmType);
                       }
                     }
                     for (int j=0; j<dsBindSpec.dsBindRules.length; ++j) {
                         dsBindingKeyDBID =
                                 lookupDataStreamBindingSpecDBID(connection,
                                 bMechDBID,
                                 dsBindSpec.dsBindRules[j].bindingKeyName);
                         if (dsBindingKeyDBID == null) {
                                 throw new ReplicationException(
                                         "dsBindSpec "
                                         + "row doesn't exist for bMechDBID: "
                                         + bMechDBID + ", binding key name: "
                                         + dsBindSpec.dsBindRules[j].bindingKeyName);
                         }
                         for (int k=0; k<behaviorBindingsEntry.dsBindingKeys.length;
                              k++)
                         {
                           // A row is added to the mechImpl table for each
                           // method with a different BindingKeyName. In cases where
                           // a single method may have multiple binding keys,
                           // multiple rows are added for each different
                           // BindingKeyName for that method.
                           if (behaviorBindingsEntry.dsBindingKeys[k].
                               equalsIgnoreCase(
                               dsBindSpec.dsBindRules[j].bindingKeyName))
                           {
                             insertMechanismImplRow(connection, bMechDBID,
                                 bDefDBID, methodDBID, dsBindingKeyDBID,
                                 "http", "text/html",
                                 // sdp - local.fedora.server conversion
                                 encodeLocalURL(behaviorBindingsEntry.serviceBindingAddress),
                                 encodeLocalURL(behaviorBindingsEntry.operationLocation), "1");
                           }
                         }
                     }
                 }
                 connection.commit();
             } catch (ReplicationException re) {
                 re.printStackTrace();
                 throw re;
             } catch (ServerException se) {
                 se.printStackTrace();
                 throw new ReplicationException(
                         "Replication exception caused by ServerException - "
                         + se.getMessage());
             } finally {
                 if (connection!=null) {
                     try {
                         connection.rollback();
                     } catch (Throwable th) {
                         logWarning("While rolling back: " +  th.getClass().getName() + ": " + th.getMessage());
                     } finally {
                         connection.setAutoCommit(true);
                         m_pool.free(connection);
                     }
                 }
             }
         }
     }
 
     /**
      * Replicates a Fedora data object.
      *
      * @param doReader data object reader
      * @exception ReplicationException replication processing error
      * @exception SQLException JDBC, SQL error
      */
     public void replicate(DOReader doReader)
             throws ReplicationException, SQLException {
         // do updates if the object already existed
         boolean componentsUpdated=false;
         boolean componentsAdded=false;
         boolean componentsPurged=purgeComponents(doReader);
         logFinest("DefaultDOReplicator.replicate: componentsPurged: "+componentsPurged);
 
         // Update operations are mutually exclusive
         if (!componentsPurged) {
           componentsUpdated=updateComponents(doReader);
                   logFinest("DefaultDOReplicator.replicate: componentsUpdated: "+componentsUpdated);
           if (!componentsUpdated) {
             // and do adds if the object already existed
             componentsAdded=addNewComponents(doReader);
             logFinest("DefaultDOReplicator.replicate: componentsAdded: "+componentsAdded);
           }
         }
         if ( !componentsUpdated && !componentsAdded && !componentsPurged ) {
             Connection connection=null;
             try
             {
                 DSBindingMapAugmented[] allBindingMaps;
                 Disseminator disseminators[];
                 String bDefDBID;
                 String bindingMapDBID;
                 String bMechDBID;
                 String dissDBID;
                 String doDBID;
                 String doPID;
                 String doLabel;
                 String dsBindingKeyDBID;
                 int rc;
 
                 doPID = doReader.GetObjectPID();
 
                 connection = m_pool.getConnection();
                 connection.setAutoCommit(false);
                 // Insert Digital Object row
                 doPID = doReader.GetObjectPID();
                 doLabel = doReader.GetObjectLabel();
 
                 insertDigitalObjectRow(connection, doPID, doLabel, doReader.GetObjectState());
 
                 doDBID = lookupDigitalObjectDBID(connection, doPID);
                 if (doDBID == null) {
                     throw new ReplicationException("do row doesn't "
                             + "exist for PID: " + doPID);
                 }
 				// add disseminator components (which include associated datastream components)
                 disseminators = doReader.GetDisseminators(null, null);
                 addDisseminators(doPID, disseminators, doReader, connection);
                	connection.commit();
             } catch (ReplicationException re) {
                     re.printStackTrace();
                     throw new ReplicationException("An error has occurred during "
                         + "Replication. The error was \" " + re.getClass().getName()
                         + " \". The cause was \" " + re.getMessage() + " \"");
             } catch (ServerException se) {
                     se.printStackTrace();
                     throw new ReplicationException("An error has occurred during "
                         + "Replication. The error was \" " + se.getClass().getName()
                         + " \". The cause was \" " + se.getMessage());
             } finally {
                     if (connection!=null) {
                         try {
                             connection.rollback();
                         } catch (Throwable th) {
                             logWarning("While rolling back: " +  th.getClass().getName() + ": " + th.getMessage());
                         } finally {
                             connection.setAutoCommit(true);
                             m_pool.free(connection);
                         }
                     }
                 }
            }
     }
 
     private ResultSet logAndExecuteQuery(Statement statement, String sql)
             throws SQLException {
         logFinest("Executing query: " + sql);
         return statement.executeQuery(sql);
     }
 
     private int logAndExecuteUpdate(Statement statement, String sql)
             throws SQLException {
         logFinest("Executing update: " + sql);
         return statement.executeUpdate(sql);
     }
 
     /**
      * Gets a string suitable for a SQL WHERE clause, of the form
      * <b>x=y1 or x=y2 or x=y3</b>...etc, where x is the value from the
      * column, and y1 is composed of the integer values from the given set.
      * <p></p>
      * If the set doesn't contain any items, returns a condition that
      * always evaluates to false, <b>1=2</b>.
      *
      * @param column value of the column.
      * @param integers set of integers.
      * @return string suitable for SQL WHERE clause.
      */
     private String inIntegerSetWhereConditionString(String column,
             Set integers) {
         StringBuffer out=new StringBuffer();
         Iterator iter=integers.iterator();
         int n=0;
         while (iter.hasNext()) {
             if (n>0) {
                 out.append(" OR ");
             }
             out.append(column);
             out.append('=');
             int i=((Integer) iter.next()).intValue();
             out.append(i);
             n++;
         }
         if (n>0) {
             return out.toString();
         } else {
             return "1=2";
         }
     }
 
     /**
      * Deletes all rows pertinent to the given behavior definition object,
      * if they exist.
      * <p></p>
      * Pseudocode:
      * <ul><pre>
      * $bDefDbID=SELECT bDefDbID FROM bDef WHERE bDefPID=$PID
      * DELETE FROM bDef,method,parm
      * WHERE bDefDbID=$bDefDbID
      * </pre></ul>
      *
      * @param connection a database connection.
      * @param pid the idenitifer of a digital object.
      * @throws SQLException If something totally unexpected happened.
      */
     private void deleteBehaviorDefinition(Connection connection, String pid)
             throws SQLException {
         logFinest("Entered DefaultDOReplicator.deleteBehaviorDefinition");
         Statement st=null;
         ResultSet results=null;
         try {
 		     st=connection.createStatement();
             //
             // READ
             //
             logFinest("Checking BehaviorDefinition table for " + pid + "...");
             results=logAndExecuteQuery(st, "SELECT bDefDbID FROM "
                     + "bDef WHERE bDefPID='" + pid + "'");
             if (!results.next()) {
                  // must not be a bdef...exit early
                  logFinest(pid + " wasn't found in BehaviorDefinition table..."
                          + "skipping deletion as such.");
                  return;
             }
             int dbid=results.getInt("bDefDbID");
             logFinest(pid + " was found in BehaviorDefinition table (DBID="
                     + dbid + ")");
             //
             // WRITE
             //
             int rowCount;
             logFinest("Attempting row deletion from BehaviorDefinition "
                     + "table...");
             rowCount=logAndExecuteUpdate(st, "DELETE FROM bDef "
                     + "WHERE bDefDbID=" + dbid);
             logFinest("Deleted " + rowCount + " row(s).");
             logFinest("Attempting row deletion from method table...");
             rowCount=logAndExecuteUpdate(st, "DELETE FROM method WHERE "
                     + "bDefDbID=" + dbid);
             logFinest("Deleted " + rowCount + " row(s).");
             logFinest("Attempting row deletion from parm table...");
             rowCount=logAndExecuteUpdate(st, "DELETE FROM parm WHERE "
                     + "bDefDbID=" + dbid);
             logFinest("Deleted " + rowCount + " row(s).");
         } finally {
             if (results != null) results.close();
             if (st!=null) st.close();
             logFinest("Exiting DefaultDOReplicator.deleteBehaviorDefinition");
         }
     }
 
     /**
      * Deletes all rows pertinent to the given behavior mechanism object,
      * if they exist.
      * <p></p>
      * Pseudocode:
      * <ul><pre>
      * $bMechDbID=SELECT bMechDbID
      * FROM bMech WHERE bMechPID=$PID
      * bMech
      * @BKEYIDS=SELECT dsBindKeyDbID
      * FROM dsBindSpec
      * WHERE bMechDbID=$bMechDbID
      * dsMIME WHERE dsBindKeyDbID in @BKEYIDS
      * mechImpl
      * </pre></ul>
      *
      * @param connection a database connection.
      * @param pid the identifier of a digital object.
      * @throws SQLException If something totally unexpected happened.
      */
     private void deleteBehaviorMechanism(Connection connection, String pid)
             throws SQLException {
         logFinest("Entered DefaultDOReplicator.deleteBehaviorMechanism");
         Statement st=null;
         ResultSet results=null;
         try {
 		     st=connection.createStatement();
             //
             // READ
             //
             logFinest("Checking bMech table for " + pid + "...");
             //results=logAndExecuteQuery(st, "SELECT bMechDbID, SMType_DBID "
             results=logAndExecuteQuery(st, "SELECT bMechDbID "
                     + "FROM bMech WHERE bMechPID='" + pid + "'");
             if (!results.next()) {
                  // must not be a bmech...exit early
                  logFinest(pid + " wasn't found in bMech table..."
                          + "skipping deletion as such.");
                  return;
             }
             int dbid=results.getInt("bMechDbID");
             //int smtype_dbid=results.getInt("bMechDbID");
             results.close();
             logFinest(pid + " was found in bMech table (DBID="
             //        + dbid + ", SMTYPE_DBID=" + smtype_dbid + ")");
                     + dbid);
             logFinest("Getting dsBindKeyDbID(s) from dsBindSpec "
                     + "table...");
             HashSet dsBindingKeyIds=new HashSet();
             results=logAndExecuteQuery(st, "SELECT dsBindKeyDbID from "
                     + "dsBindSpec WHERE bMechDbID=" + dbid);
             while (results.next()) {
                 dsBindingKeyIds.add(new Integer(
                         results.getInt("dsBindKeyDbID")));
             }
             results.close();
             logFinest("Found " + dsBindingKeyIds.size()
                     + " dsBindKeyDbID(s).");
             //
             // WRITE
             //
             int rowCount;
             logFinest("Attempting row deletion from bMech table..");
             rowCount=logAndExecuteUpdate(st, "DELETE FROM bMech "
                     + "WHERE bMechDbID=" + dbid);
             logFinest("Deleted " + rowCount + " row(s).");
             logFinest("Attempting row deletion from dsBindSpec "
                     + "table...");
             rowCount=logAndExecuteUpdate(st, "DELETE FROM "
                     + "dsBindSpec WHERE bMechDbID=" + dbid);
             logFinest("Deleted " + rowCount + " row(s).");
             logFinest("Attempting row deletion from dsMIME table...");
             rowCount=logAndExecuteUpdate(st, "DELETE FROM dsMIME WHERE "
                     + inIntegerSetWhereConditionString("dsBindKeyDbID",
                     dsBindingKeyIds));
             logFinest("Deleted " + rowCount + " row(s).");
             //logFinest("Attempting row deletion from StructMapType table...");
             //rowCount=logAndExecuteUpdate(st, "DELETE FROM StructMapType WHERE "
             //        + "SMType_DBID=" + smtype_dbid);
             //logFinest("Deleted " + rowCount + " row(s).");
             logFinest("Attempting row deletion from dsBindMap table...");
             rowCount=logAndExecuteUpdate(st, "DELETE FROM dsBindMap WHERE "
                     + "bMechDbID=" + dbid);
             logFinest("Deleted " + rowCount + " row(s).");
             logFinest("Attempting row deletion from mechImpl table...");
             rowCount=logAndExecuteUpdate(st, "DELETE FROM mechImpl WHERE "
                     + "bMechDbID=" + dbid);
             logFinest("Deleted " + rowCount + " row(s).");
             logFinest("Attempting row deletion from mechDefParm table...");
             rowCount=logAndExecuteUpdate(st, "DELETE FROM mechDefParm "
                 + "WHERE bMechDbID=" + dbid);
             logFinest("Deleted " + rowCount + " row(s).");
 
         } finally {
             if (results != null) results.close();
             if (st!=null)st.close();
             logFinest("Exiting DefaultDOReplicator.deleteBehaviorMechanism");
         }
     }
 
     /**
      * Deletes all rows pertinent to the given digital object (treated as a
      * data object) if they exist.
      * <p></p>
      * Pseudocode:
      * <ul><pre>
      * $doDbID=SELECT doDbID FROM do where doPID=$PID
      * @DISSIDS=SELECT dissDbID
      * FROM doDissAssoc WHERE doDbID=$doDbID
      * @BMAPIDS=SELECT dsBindMapDbID
      * FROM dsBind WHERE doDbID=$doDbID
      * do
      * doDissAssoc where $doDbID=doDbID
      * dsBind WHERE $doDbID=doDbID
      * diss WHERE dissDbID in @DISSIDS
      * dsBindMap WHERE dsBindMapDbID in @BMAPIDS
      * </pre></ul>
      *
      * @param connection a databae connection.
      * @param pid the identifier for a digital object.
      * @throws SQLException If something totally unexpected happened.
      */
     private void deleteDigitalObject(Connection connection, String pid)
             throws SQLException {
         logFinest("DefaultDOReplicator.deleteDigitalObject: Entering -----");
         Statement st=null;
         Statement st2=null;
         Statement st3=null;
         ResultSet results=null;
         try {
 		     st=connection.createStatement();
             //
             // READ
             //
             logFinest("Checking do table for " + pid + "...");
             results=logAndExecuteQuery(st, "SELECT doDbID FROM "
                     + "do WHERE doPID='" + pid + "'");
             if (!results.next()) {
                  // must not be a digitalobject...exit early
                  logFinest(pid + " wasn't found in do table..."
                          + "skipping deletion as such.");
                  return;
             }
             int dbid=results.getInt("doDbID");
             results.close();
             logFinest(pid + " was found in do table (DBID="
                     + dbid + ")");
 
             logFinest("Getting dissDbID(s) from doDissAssoc "
                     + "table...");
             HashSet dissIds=new HashSet();
             HashSet dissIdsNotShared = new HashSet();
 
             // Get all dissIds in db for this object
             results=logAndExecuteQuery(st, "SELECT dissDbID from "
                     + "doDissAssoc WHERE doDbID=" + dbid);
             while (results.next()) {
                 dissIds.add(new Integer(results.getInt("dissDbID")));
             }
             results.close();
 
             logFinest("Found " + dissIds.size() + " dissDbID(s).");
             logFinest("Getting dissDbID(s) from doDissAssoc "
                     + "table unique to this object...");
             Iterator iterator = dissIds.iterator();
 
             // Iterate over dissIds and separate those that are unique
             // (i.e., not shared by other objects)
             while (iterator.hasNext())
             {
               Integer id = (Integer)iterator.next();
               logFinest("Getting occurrences of dissDbID(s) in "
                     + "doDissAssoc table...");
               results=logAndExecuteQuery(st, "SELECT COUNT(*) from "
                     + "doDissAssoc WHERE dissDbID=" + id);
               while (results.next())
               {
                 Integer i1 = new Integer(results.getInt("COUNT(*)"));
                 if ( i1.intValue() == 1 )
                 {
                   // A dissDbID that occurs only once indicates that the
                   // disseminator is not used by other objects. In this case, we
                   // want to keep track of this dissDbID.
                   dissIdsNotShared.add(id);
                   logFinest("DefaultDOReplicator.deleteDigitalObject: added "
                     + "dissDbId that was not shared: " + id);
                 }
               }
               results.close();
 
             }
 
             // Get all binding map Ids in db for this object.
             HashSet bmapIds=new HashSet();
             results=logAndExecuteQuery(st, "SELECT dsBindMapDbID FROM "
                     + "dsBind WHERE doDbID=" + dbid);
             while (results.next()) {
                 bmapIds.add(new Integer(results.getInt("dsBindMapDbID")));
             }
             results.close();
             logFinest("Found " + bmapIds.size() + " dsBindMapDbID(s).");
 
             // Iterate over bmapIds and separate those that are unique
             // (i.e., not shared by other objects)
             iterator = bmapIds.iterator();
             HashSet bmapIdsNotShared = new HashSet();
             while (iterator.hasNext() )
             {
               Integer id = (Integer)iterator.next();
               ResultSet rs = null;
               logFinest("Getting associated bmapId(s) that are unique "
                   + "for this object in diss table...");
               st2 = connection.createStatement();
               rs=logAndExecuteQuery(st2, "SELECT DISTINCT doDbID FROM "
                   + "dsBind WHERE dsBindMapDbID=" + id);
               int rowCount = 0;
               while (rs.next())
               {
                 rowCount++;
               }
               if ( rowCount == 1 )
               {
                 // A bmapId that occurs only once indicates that
                 // a bmapId is not used by other objects. In this case, we
                 // want to keep track of this bpamId.
                 bmapIdsNotShared.add(id);
                 logFinest("DefaultDOReplicator.deleteDigitalObject: added "
                     + "dsBindMapDbId that was not shared: " + id);
               }
 
               rs.close();
               st2.close();
             }
 
             //
             // WRITE
             //
             int rowCount;
             logFinest("Attempting row deletion from do table...");
             rowCount=logAndExecuteUpdate(st, "DELETE FROM do "
                     + "WHERE doDbID=" + dbid);
             logFinest("Deleted " + rowCount + " row(s).");
             logFinest("Attempting row deletion from doDissAssoc "
                     + "table...");
             rowCount=logAndExecuteUpdate(st, "DELETE FROM "
                     + "doDissAssoc WHERE doDbID=" + dbid);
             logFinest("Deleted " + rowCount + " row(s).");
             logFinest("Attempting row deletion from dsBind table..");
             rowCount=logAndExecuteUpdate(st, "DELETE FROM dsBind "
                     + "WHERE doDbID=" + dbid);
             logFinest("Deleted " + rowCount + " row(s).");
 
             // Since dissIds can be shared by other objects in db, only remove
             // those Ids that are not shared.
             logFinest("Attempting row deletion from diss table...");
             rowCount=logAndExecuteUpdate(st, "DELETE FROM diss WHERE "
                     + inIntegerSetWhereConditionString("dissDbID", dissIdsNotShared));
             logFinest("Deleted " + rowCount + " row(s).");
             logFinest("Attempting row deletion from dsBindMap "
                     + "table...");
 
             // Since bmapIds can be shared by other objects in db, only remove
             // thos Ids that are not shared.
             rowCount=logAndExecuteUpdate(st, "DELETE FROM dsBindMap "
                     + "WHERE " + inIntegerSetWhereConditionString(
                     "dsBindMapDbID", bmapIdsNotShared));
             logFinest("Deleted " + rowCount + " row(s).");
         } finally {
             if (results != null) results.close();
             if (st!=null) st.close();
             if (st2!=null) st2.close();
             logFinest("DefaultDOReplicator.deleteDigitalObject: Exiting -----");
         }
     }
 
     /**
      * Removes a digital object from the dissemination database.
      * <p></p>
      * If the object is a behavior definition or mechanism, it's deleted
      * as such, and then an attempt is made to delete it as a data object
      * as well.
      * <p></p>
      * Note that this does not do cascading check object dependencies at
      * all.  It is expected at this point that when this is called, any
      * referencial integrity issues have been ironed out or checked as
      * appropriate.
      * <p></p>
      * All deletions happen in a transaction.  If any database errors occur,
      * the change is rolled back.
      *
      * @param pid The pid of the object to delete.
      * @throws ReplicationException If the request couldn't be fulfilled for
      *         any reason.
      */
     public void delete(String pid)
             throws ReplicationException {
         logFinest("Entered DefaultDOReplicator.delete");
         Connection connection=null;
         try {
             connection = m_pool.getConnection();
             connection.setAutoCommit(false);
             deleteBehaviorDefinition(connection, pid);
             deleteBehaviorMechanism(connection, pid);
             deleteDigitalObject(connection, pid);
             connection.commit();
         } catch (SQLException sqle) {
             throw new ReplicationException("Error while replicator was trying "
                     + "to delete " + pid + ". " + sqle.getMessage());
         } finally {
             if (connection!=null) {
                 try {
                     connection.rollback();
                     connection.setAutoCommit(true);
                     m_pool.free(connection);
                 } catch (SQLException sqle) {}
             }
             logFinest("Exiting DefaultDOReplicator.delete");
         }
     }
 
         /**
         *
         * Inserts a Behavior Definition row.
         *
         * @param connection JDBC DBMS connection
         * @param bDefPID Behavior definition PID
         * @param bDefLabel Behavior definition label
         * @param bDefState State of behavior definition object.
         *
         * @exception SQLException JDBC, SQL error
         */
 	public void insertBehaviorDefinitionRow(Connection connection, String bDefPID, String bDefLabel, String bDefState) throws SQLException {
 
 		String insertionStatement = "INSERT INTO bDef (bDefPID, bDefLabel, bDefState) VALUES ('" + bDefPID + "', '" + SQLUtility.aposEscape(bDefLabel) + "', '" + bDefState + "')";
 
 		insertGen(connection, insertionStatement);
 	}
 
         /**
         *
         * Inserts a Behavior Mechanism row.
         *
         * @param connection JDBC DBMS connection
         * @param bDefDbID Behavior definition DBID
         * @param bMechPID Behavior mechanism DBID
         * @param bMechLabel Behavior mechanism label
         * @param bMechState Statye of behavior mechanism object.
         *
         * @throws SQLException JDBC, SQL error
         */
 	public void insertBehaviorMechanismRow(Connection connection, String bDefDbID, String bMechPID, String bMechLabel, String bMechState) throws SQLException {
 
 		String insertionStatement = "INSERT INTO bMech (bDefDbID, bMechPID, bMechLabel, bMechState) VALUES ('" + bDefDbID + "', '" + bMechPID + "', '" + SQLUtility.aposEscape(bMechLabel) + "', '" + bMechState + "')";
 
 		insertGen(connection, insertionStatement);
 	}
 
         /**
         *
         * Inserts a DataStreamBindingRow row.
         *
         * @param connection JDBC DBMS connection
         * @param doDbID Digital object DBID
         * @param dsBindKeyDbID Datastream binding key DBID
         * @param dsBindMapDbID Binding map DBID
         * @param dsBindKeySeq Datastream binding key sequence number
         * @param dsID Datastream ID
         * @param dsLabel Datastream label
         * @param dsMIME Datastream mime type
         * @param dsLocation Datastream location
         * @param dsControlGroupType Datastream type
         * @param dsCurrentVersionID Datastream current version ID
         * @param policyDbID Policy DBID
         * @param dsState State of datastream.
         *
         * @exception SQLException JDBC, SQL error
         */
 	public void insertDataStreamBindingRow(Connection connection, String doDbID, String dsBindKeyDbID, String dsBindMapDbID, String dsBindKeySeq, String dsID, String dsLabel, String dsMIME, String dsLocation, String dsControlGroupType, String dsCurrentVersionID, String policyDbID, String dsState) throws SQLException {
 
 		String insertionStatement = "INSERT INTO dsBind (doDbID, dsBindKeyDbID, dsBindMapDbID, dsBindKeySeq, dsID, dsLabel, dsMIME, dsLocation, dsControlGroupType, dsCurrentVersionID, policyDbID, dsState) VALUES ('" + doDbID + "', '" + dsBindKeyDbID + "', '" + dsBindMapDbID + "', '" + dsBindKeySeq + "', '" + dsID + "', '" + SQLUtility.aposEscape(dsLabel) + "', '" + dsMIME + "', '" + dsLocation + "', '" + dsControlGroupType + "', '" + dsCurrentVersionID + "', '" + policyDbID + "', '" + dsState + "')";
 
 		insertGen(connection, insertionStatement);
 	}
 
         /**
         *
         * Inserts a dsBindMap row.
         *
         * @param connection JDBC DBMS connection
         * @param bMechDbID Behavior mechanism DBID
         * @param dsBindMapID Datastream binding map ID
         * @param dsBindMapLabel Datastream binding map label
         *
         * @exception SQLException JDBC, SQL error
         */
 	public void insertDataStreamBindingMapRow(Connection connection, String bMechDbID, String dsBindMapID, String dsBindMapLabel) throws SQLException {
 
 		String insertionStatement = "INSERT INTO dsBindMap (bMechDbID, dsBindMapID, dsBindMapLabel) VALUES ('" + bMechDbID + "', '" + dsBindMapID + "', '" + SQLUtility.aposEscape(dsBindMapLabel) + "')";
 		insertGen(connection, insertionStatement);
 	}
 
         /**
         *
         * Inserts a dsBindSpec row.
         *
         * @param connection JDBC DBMS connection
         * @param bMechDbID Behavior mechanism DBID
         * @param dsBindSpecName Datastream binding spec name
         * @param dsBindSpecOrdinality Datastream binding spec ordinality flag
         * @param dsBindSpecCardinality Datastream binding cardinality
         * @param dsBindSpecLabel Datastream binding spec lable
         *
         * @exception SQLException JDBC, SQL error
         */
 	public void insertDataStreamBindingSpecRow(Connection connection, String bMechDbID, String dsBindSpecName, String dsBindSpecOrdinality, String dsBindSpecCardinality, String dsBindSpecLabel) throws SQLException {
 
 		String insertionStatement = "INSERT INTO dsBindSpec (bMechDbID, dsBindSpecName, dsBindSpecOrdinality, dsBindSpecCardinality, dsBindSpecLabel) VALUES ('" + bMechDbID + "', '" + SQLUtility.aposEscape(dsBindSpecName) + "', '" + dsBindSpecOrdinality + "', '" + dsBindSpecCardinality + "', '" + SQLUtility.aposEscape(dsBindSpecLabel) + "')";
 
 		insertGen(connection, insertionStatement);
 	}
 
         /**
         *
         * Inserts a dsMIME row.
         *
         * @param connection JDBC DBMS connection
         * @param dsBindKeyDbID Datastream binding key DBID
         * @param dsMIMEName Datastream MIME type name
         *
         * @exception SQLException JDBC, SQL error
         */
 	public void insertDataStreamMIMERow(Connection connection, String dsBindKeyDbID, String dsMIMEName) throws SQLException {
 
 		String insertionStatement = "INSERT INTO dsMIME (dsBindKeyDbID, dsMIMEName) VALUES ('" + dsBindKeyDbID + "', '" + dsMIMEName + "')";
 
 		insertGen(connection, insertionStatement);
 	}
 
         /**
         *
         * Inserts a do row.
         *
         * @param connection JDBC DBMS connection
         * @param doPID DigitalObject PID
         * @param doLabel DigitalObject label
         * @param doState State of digital object.
         *
         * @exception SQLException JDBC, SQL error
         */
 	public void insertDigitalObjectRow(Connection connection, String doPID, String doLabel, String doState) throws SQLException {
 
 		String insertionStatement = "INSERT INTO do (doPID, doLabel, doState) VALUES ('" + doPID + "', '" +  SQLUtility.aposEscape(doLabel) + "', '" + doState + "')";
 
 		insertGen(connection, insertionStatement);
 	}
 
         /**
         *
         * Inserts a doDissAssoc row.
         *
         * @param connection JDBC DBMS connection
         * @param doDbID DigitalObject DBID
         * @param dissDbID Disseminator DBID
         *
         * @exception SQLException JDBC, SQL error
         */
 	public void insertDigitalObjectDissAssocRow(Connection connection, String doDbID, String dissDbID) throws SQLException {
 
 		String insertionStatement = "INSERT INTO doDissAssoc (doDbID, dissDbID) VALUES ('" + doDbID + "', '" + dissDbID + "')";
 		insertGen(connection, insertionStatement);
 	}
 
         /**
         *
         * Inserts a Disseminator row.
         *
         * @param connection JDBC DBMS connection
         * @param bDefDbID Behavior definition DBID
         * @param bMechDbID Behavior mechanism DBID
         * @param dissID Disseminator ID
         * @param dissLabel Disseminator label
         * @param dissState State of disseminator.
         *
         * @exception SQLException JDBC, SQL error
         */
 	public void insertDisseminatorRow(Connection connection, String bDefDbID, String bMechDbID, String dissID, String dissLabel, String dissState) throws SQLException {
 
 		String insertionStatement = "INSERT INTO diss (bDefDbID, bMechDbID, dissID, dissLabel, dissState) VALUES ('" + bDefDbID + "', '" + bMechDbID + "', '" + dissID + "', '" + SQLUtility.aposEscape(dissLabel) + "', '" + dissState + "')";
 		insertGen(connection, insertionStatement);
 	}
 
         /**
         *
         * Inserts a mechImpl row.
         *
         * @param connection JDBC DBMS connection
         * @param bMechDbID Behavior mechanism DBID
         * @param bDefDbID Behavior definition DBID
         * @param methodDbID Method DBID
         * @param dsBindKeyDbID Datastream binding key DBID
         * @param protocolType Mechanism implementation protocol type
         * @param returnType Mechanism implementation return type
         * @param addressLocation Mechanism implementation address location
         * @param operationLocation Mechanism implementation operation location
         * @param policyDbID Policy DBID
         *
         * @exception SQLException JDBC, SQL error
         */
 	public void insertMechanismImplRow(Connection connection, String bMechDbID, String bDefDbID, String methodDbID, String dsBindKeyDbID, String protocolType, String returnType, String addressLocation, String operationLocation, String policyDbID) throws SQLException {
 
 		String insertionStatement = "INSERT INTO mechImpl (bMechDbID, bDefDbID, methodDbID, dsBindKeyDbID, protocolType, returnType, addressLocation, operationLocation, policyDbID) VALUES ('" + bMechDbID + "', '" + bDefDbID + "', '" + methodDbID + "', '" + dsBindKeyDbID + "', '" + protocolType + "', '" + returnType + "', '" + addressLocation + "', '" + operationLocation + "', '" + policyDbID + "')";
 
 		insertGen(connection, insertionStatement);
 	}
 
         /**
         *
         * Inserts a method row.
         *
         * @param connection JDBC DBMS connection
         * @param bDefDbID Behavior definition DBID
         * @param methodName Behavior definition label
         * @param methodLabel Behavior definition label
         *
         * @exception SQLException JDBC, SQL error
         */
 	public void insertMethodRow(Connection connection, String bDefDbID, String methodName, String methodLabel) throws SQLException {
 
 		String insertionStatement = "INSERT INTO method (bDefDbID, methodName, methodLabel) VALUES ('" + bDefDbID + "', '" + SQLUtility.aposEscape(methodName) + "', '" + SQLUtility.aposEscape(methodLabel) + "')";
 
 		insertGen(connection, insertionStatement);
 	}
 
        /**
         *
         * @param connection An SQL Connection.
         * @param methDBID The method database ID.
         * @param bdefDBID The behavior Definition object database ID.
         * @param parmName the parameter name.
         * @param parmDefaultValue A default value for the parameter.
         * @param parmDomainValues A list of possible values for the parameter.
         * @param parmRequiredFlag A boolean flag indicating whether the
         *        parameter is required or not.
         * @param parmLabel The parameter label.
         * @param parmType The parameter type.
         * @throws SQLException JDBC, SQL error
         */
         public void insertMethodParmRow(Connection connection, String methDBID,
             String bdefDBID, String parmName, String parmDefaultValue,
             String parmDomainValues, String parmRequiredFlag,
             String parmLabel, String parmType)
             throws SQLException {
                 String insertionStatement = "INSERT INTO parm "
                 + "(methodDbID, bDefDbID, parmName, parmDefaultValue, "
                 + "parmDomainValues, parmRequiredFlag, parmLabel, "
                 + "parmType) VALUES ('"
                 + methDBID + "', '" + bdefDBID + "', '"
                 + SQLUtility.aposEscape(parmName) + "', '" + SQLUtility.aposEscape(parmDefaultValue) + "', '"
                 + SQLUtility.aposEscape(parmDomainValues) + "', '"
                 + parmRequiredFlag + "', '" + SQLUtility.aposEscape(parmLabel) + "', '"
                 + parmType + "')";
                 insertGen(connection, insertionStatement);
 	}
 
         /**
          *
          * @param connection An SQL Connection.
          * @param methDBID The method database ID.
          * @param bmechDBID The behavior Mechanism object database ID.
          * @param parmName the parameter name.
          * @param parmDefaultValue A default value for the parameter.
          * @param parmRequiredFlag A boolean flag indicating whether the
          *        parameter is required or not.
          * @param parmDomainValues A list of possible values for the parameter.
          * @param parmLabel The parameter label.
          * @param parmType The parameter type.
          * @throws SQLException JDBC, SQL error
          */
          public void insertMechDefaultMethodParmRow(Connection connection, String methDBID,
              String bmechDBID, String parmName, String parmDefaultValue,
              String parmDomainValues, String parmRequiredFlag,
              String parmLabel, String parmType)
              throws SQLException {
                  String insertionStatement = "INSERT INTO mechDefParm "
                  + "(methodDbID, bMechDbID, defParmName, defParmDefaultValue, "
                  + "defParmDomainValues, defParmRequiredFlag, defParmLabel, "
                  + "defParmType) VALUES ('"
                  + methDBID + "', '" + bmechDBID + "', '"
                  + SQLUtility.aposEscape(parmName) + "', '" + SQLUtility.aposEscape(parmDefaultValue) + "', '"
                  + SQLUtility.aposEscape(parmDomainValues) + "', '"
                  + parmRequiredFlag + "', '" + SQLUtility.aposEscape(parmLabel) + "', '"
                  + parmType + "')";
                  insertGen(connection, insertionStatement);
 	}
 
         /**
         *
         * General JDBC row insertion method.
         *
         * @param connection JDBC DBMS connection
         * @param insertionStatement SQL row insertion statement
         *
         * @exception SQLException JDBC, SQL error
         */
 	public void insertGen(Connection connection, String insertionStatement) throws SQLException {
 		int rowCount = 0;
 		Statement statement = null;
 
 		statement = connection.createStatement();
 
 		logFinest("Doing DB Insert: " + insertionStatement);
 		rowCount = statement.executeUpdate(insertionStatement);
 		statement.close();
 	}
 
         /**
         *
         * Looks up a BehaviorDefinition DBID.
         *
         * @param connection JDBC DBMS connection
         * @param bDefPID Behavior definition PID
         *
         * @return The DBID of the specified Behavior Definition row.
         *
         * @throws StorageDeviceException if db lookup fails for any reason.
         */
 	public String lookupBehaviorDefinitionDBID(Connection connection, String bDefPID) throws StorageDeviceException {
 		return lookupDBID1(connection, "bDefDbID", "bDef", "bDefPID", bDefPID);
 	}
 
         /**
         *
         * Looks up a BehaviorMechanism DBID.
         *
         * @param connection JDBC DBMS connection
         * @param bMechPID Behavior mechanism PID
         *
         * @return The DBID of the specified Behavior Mechanism row.
         *
         * @throws StorageDeviceException if db lookup fails for any reason.
         */
 	public String lookupBehaviorMechanismDBID(Connection connection, String bMechPID) throws StorageDeviceException {
 		return lookupDBID1(connection, "bMechDbID", "bMech", "bMechPID", bMechPID);
 	}
 
         /**
         *
         * Looks up a dsBindMap DBID.
         *
         * @param connection JDBC DBMS connection
         * @param bMechDBID Behavior mechanism DBID
         * @param dsBindingMapID Data stream binding map ID
         *
         * @return The DBID of the specified dsBindMap row.
         *
         * @throws StorageDeviceException if db lookup fails for any reason.
         */
 	public String lookupDataStreamBindingMapDBID(Connection connection, String bMechDBID, String dsBindingMapID) throws StorageDeviceException {
 		return lookupDBID2FirstNum(connection, "dsBindMapDbID", "dsBindMap", "bMechDbID", bMechDBID, "dsBindMapID", dsBindingMapID);
 	}
 
         public String lookupDsBindingMapDBID(Connection connection, String bMechDBID, String dsBindingMapID) throws StorageDeviceException {
                 return lookupDBID2FirstNum(connection, "dissDbID", "diss", "bMechDbID", bMechDBID, "dsBindMapID", dsBindingMapID);
         }
 
 
         /**
         *
         * Looks up a dsBindSpec DBID.
         *
         * @param connection JDBC DBMS connection
         * @param bMechDBID Behavior mechanism DBID
         * @param dsBindingSpecName Data stream binding spec name
         *
         * @return The DBID of the specified dsBindSpec row.
         *
         * @throws StorageDeviceException if db lookup fails for any reason.
         */
 	public String lookupDataStreamBindingSpecDBID(Connection connection, String bMechDBID, String dsBindingSpecName) throws StorageDeviceException {
 		return lookupDBID2FirstNum(connection, "dsBindKeyDbID", "dsBindSpec", "bMechDbID", bMechDBID, "dsBindSpecName", dsBindingSpecName);
 	}
 
         /**
         *
         * Looks up a do DBID.
         *
         * @param connection JDBC DBMS connection
         * @param doPID Data object PID
         *
         * @return The DBID of the specified DigitalObject row.
         *
         * @throws StorageDeviceException if db lookup fails for any reason.
         */
 	public String lookupDigitalObjectDBID(Connection connection, String doPID) throws StorageDeviceException {
 		return lookupDBID1(connection, "doDbID", "do", "doPID", doPID);
 	}
 
         /**
         *
         * Looks up a Disseminator DBID.
         *
         * @param connection JDBC DBMS connection
         * @param bDefDBID Behavior definition DBID
         * @param bMechDBID Behavior mechanism DBID
         * @param dissID Disseminator ID
         *
         * @return The DBID of the specified Disseminator row.
         *
         * @throws StorageDeviceException if db lookup fails for any reason.
         */
 	public String lookupDisseminatorDBID(Connection connection, String bDefDBID, String bMechDBID, String dissID) throws StorageDeviceException {
             Statement statement = null;
             ResultSet rs = null;
             String query = null;
             String ID = null;
             try
             {
 		query = "SELECT dissDbID FROM diss WHERE ";
 		query += "bDefDbID = " + bDefDBID + " AND ";
 		query += "bMechDbID = " + bMechDBID + " AND ";
 		query += "dissID = '" + dissID + "'";
 
 		logFinest("Doing Query: " + query);
 
 		statement = connection.createStatement();
 		rs = statement.executeQuery(query);
 
 		while (rs.next())
 			ID = rs.getString(1);
 
             } catch (Throwable th)
             {
               throw new StorageDeviceException("[DBIDLookup] An error has "
                   + "occurred. The error was \" " + th.getClass().getName()
                   + " \". The cause was \" " + th.getMessage() + " \"");
             } finally
             {
                 try
                 {
                     if (rs != null) rs.close();
                     if (statement != null) statement.close();
 
                 } catch (SQLException sqle)
                 {
                     throw new StorageDeviceException("[DBIDLookup] An error has "
                         + "occurred. The error was \" " + sqle.getClass().getName()
                         + " \". The cause was \" " + sqle.getMessage() + " \"");
                 }
             }
             return ID;
 	}
 
         /**
         *
         * Looks up a method DBID.
         *
         * @param connection JDBC DBMS connection
         * @param bDefDBID Behavior definition DBID
         * @param methName Method name
         *
         * @return The DBID of the specified method row.
         *
         * @throws StorageDeviceException if db lookup fails for any reason.
         */
 	public String lookupMethodDBID(Connection connection, String bDefDBID, String methName) throws StorageDeviceException {
 		return lookupDBID2FirstNum(connection, "methodDbID", "method", "bDefDbID", bDefDBID, "methodName", methName);
 	}
 
         /**
         *
         * General JDBC lookup method with 1 lookup column value.
         *
         * @param connection JDBC DBMS connection
         * @param DBIDName DBID column name
         * @param tableName Table name
         * @param lookupColumnName Lookup column name
         * @param lookupColumnValue Lookup column value
         *
         * @return The DBID of the specified row.
         *
         * @throws StorageDeviceException if db lookup fails for any reason.
         */
 	public String lookupDBID1(Connection connection, String DBIDName, String tableName, String lookupColumnName, String lookupColumnValue) throws StorageDeviceException {
 		String query = null;
 		String ID = null;
 		Statement statement = null;
 		ResultSet rs = null;
 
                 try
                 {
 
     		    query = "SELECT " + DBIDName + " FROM " + tableName + " WHERE ";
 		    query += lookupColumnName + " = '" + lookupColumnValue + "'";
 
                     logFinest("Doing Query: " + query);
 
                     statement = connection.createStatement();
                     rs = statement.executeQuery(query);
 
                     while (rs.next())
 			ID = rs.getString(1);
 
                 } catch (Throwable th)
                 {
                     throw new StorageDeviceException("[DBIDLookup] An error has "
                         + "occurred. The error was \" " + th.getClass().getName()
                         + " \". The cause was \" " + th.getMessage() + " \"");
                 } finally
                 {
                     try
                     {
                         if (rs != null) rs.close();
                         if (statement != null) statement.close();
 
                     } catch (SQLException sqle)
                     {
                         throw new StorageDeviceException("[DBIDLookup] An error has "
                             + "occurred. The error was \" " + sqle.getClass().getName()
                             + " \". The cause was \" " + sqle.getMessage() + " \"");
                     }
                 }
                 return ID;
 	}
 
         /**
         *
         * General JDBC lookup method with 2 lookup column values.
         *
         * @param connection JDBC DBMS connection
         * @param DBIDName DBID Column name
         * @param tableName Table name
         * @param lookupColumnName1 First lookup column name
         * @param lookupColumnValue1 First lookup column value
         * @param lookupColumnName2 Second lookup column name
         * @param lookupColumnValue2 Second lookup column value
         *
         * @return The DBID of the specified row.
         *
         * @throws StorageDeviceException if db lookup fails for any reason.
         */
 	public String lookupDBID2(Connection connection, String DBIDName, String tableName, String lookupColumnName1, String lookupColumnValue1, String lookupColumnName2, String lookupColumnValue2) throws StorageDeviceException {
 		String query = null;
 		String ID = null;
 		Statement statement = null;
 		ResultSet rs = null;
 
                 try
                 {
 
 		    query = "SELECT " + DBIDName + " FROM " + tableName + " WHERE ";
                     query += lookupColumnName1 + " = '" + lookupColumnValue1 + "' AND ";
                     query += lookupColumnName2 + " = '" + lookupColumnValue2 + "'";
 
                     logFinest("Doing Query: " + query);
 
                     statement = connection.createStatement();
                     rs = statement.executeQuery(query);
 
                     while (rs.next())
 			ID = rs.getString(1);
 
                 } catch (Throwable th)
                 {
                     throw new StorageDeviceException("[DBIDLookup] An error has "
                         + "occurred. The error was \" " + th.getClass().getName()
                         + " \". The cause was \" " + th.getMessage() + " \"");
                 } finally
                 {
                     try
                     {
                         if (rs != null) rs.close();
                         if (statement != null) statement.close();
 
                     } catch (SQLException sqle)
                     {
                         throw new StorageDeviceException("[DBIDLookup] An error has "
                             + "occurred. The error was \" " + sqle.getClass().getName()
                             + " \". The cause was \" " + sqle.getMessage() + " \"");
                     }
                 }
                 return ID;
 	}
 
 	public String lookupDBID2FirstNum(Connection connection, String DBIDName, String tableName, String lookupColumnName1, String lookupColumnValue1, String lookupColumnName2, String lookupColumnValue2) throws StorageDeviceException {
 		String query = null;
 		String ID = null;
 		Statement statement = null;
 		ResultSet rs = null;
 
                 try
                 {
 		    query = "SELECT " + DBIDName + " FROM " + tableName + " WHERE ";
                     query += lookupColumnName1 + " =" + lookupColumnValue1 + " AND ";
                     query += lookupColumnName2 + " = '" + lookupColumnValue2 + "'";
 
                     logFinest("Doing Query: " + query);
 
                     statement = connection.createStatement();
                     rs = statement.executeQuery(query);
 
                     while (rs.next())
 			ID = rs.getString(1);
 
                 } catch (Throwable th)
                 {
                     throw new StorageDeviceException("[DBIDLookup] An error has "
                         + "occurred. The error was \" " + th.getClass().getName()
                         + " \". The cause was \" " + th.getMessage() + " \"");
                 } finally
                 {
                     try
                     {
                         if (rs != null) rs.close();
                         if (statement != null) statement.close();
 
                     } catch (SQLException sqle)
                     {
                         throw new StorageDeviceException("[DBIDLookup] An error has "
                             + "occurred. The error was \" " + sqle.getClass().getName()
                             + " \". The cause was \" " + sqle.getMessage() + " \"");
                     }
                 }
                 return ID;
 	}
 
         private String encodeLocalURL(String locationString)
         {
           // Replace any occurences of the host:port of the local Fedora
           // server with the internal serialization string "local.fedora.server."
           // This will make sure that local URLs (self-referential to the
           // local server) will be recognizable if the server host and
           // port configuration changes after an object is stored in the
           // repository.
           if (fedoraServerPort.equalsIgnoreCase("80") &&
               hostPattern.matcher(locationString).find())
           {
               //System.out.println("port is 80 and host-only pattern found - convert to l.f.s");
               return hostPattern.matcher(
                 locationString).replaceAll("http://local.fedora.server/");
           }
           else
           {
               //System.out.println("looking for hostPort pattern to convert to l.f.s");
               return hostPortPattern.matcher(
                 locationString).replaceAll("http://local.fedora.server/");
           }
         }
 
         private String unencodeLocalURL(String storedLocationString)
         {
           // Replace any occurrences of the internal serialization string
           // "local.fedora.server" with the current host and port of the
           // local Fedora server.  This translates local URLs (self-referential
           // to the local server) back into resolvable URLs that reflect
           // the currently configured host and port for the server.
 
           if (fedoraServerPort.equalsIgnoreCase("80"))
           {
             return serializedLocalURLPattern.matcher(
               storedLocationString).replaceAll(fedoraServerHost);
           }
           else
           {
             return serializedLocalURLPattern.matcher(
               storedLocationString).replaceAll(
               fedoraServerHost+":"+fedoraServerPort);
           }
         }
 
 		private void addDisseminators(String doPID, Disseminator[] disseminators, DOReader doReader, Connection connection)
 			throws ReplicationException, SQLException, ServerException
 		{
 			DSBindingMapAugmented[] allBindingMaps;
 			String bDefDBID;
 			String bindingMapDBID;
 			String bMechDBID;
 			String dissDBID;
 			String doDBID;
 			String doLabel;
 			String dsBindingKeyDBID;
 			int rc;
 
 			doDBID = lookupDigitalObjectDBID(connection, doPID);
 			for (int i=0; i<disseminators.length; ++i) {
 				bDefDBID = lookupBehaviorDefinitionDBID(connection,
 						disseminators[i].bDefID);
 				if (bDefDBID == null) {
 					throw new ReplicationException("BehaviorDefinition row "
 							+ "doesn't exist for PID: "
 							+ disseminators[i].bDefID);
 				}
 				bMechDBID = lookupBehaviorMechanismDBID(connection,
 						disseminators[i].bMechID);
 				if (bMechDBID == null) {
 					throw new ReplicationException("BehaviorMechanism row "
 							+ "doesn't exist for PID: "
 							+ disseminators[i].bMechID);
 				}
 				// Insert Disseminator row if it doesn't exist.
 				dissDBID = lookupDisseminatorDBID(connection, bDefDBID,
 						bMechDBID, disseminators[i].dissID);
 				if (dissDBID == null) {
 					// Disseminator row doesn't exist, add it.
 					insertDisseminatorRow(connection, bDefDBID, bMechDBID,
 					disseminators[i].dissID, disseminators[i].dissLabel, disseminators[i].dissState);
 
                                         //insertDisseminatorRow(connection, bDefDBID, bMechDBID,
 					//disseminators[i].dissID, disseminators[i].dissLabel, disseminators[i].dissState, disseminators[i].dsBindMapID);
 					dissDBID = lookupDisseminatorDBID(connection, bDefDBID,
 							bMechDBID, disseminators[i].dissID);
 					if (dissDBID == null) {
 						throw new ReplicationException("diss row "
 								+ "doesn't exist for PID: "
 								+ disseminators[i].dissID);
 					}
 				}
 				// Insert doDissAssoc row
 				insertDigitalObjectDissAssocRow(connection, doDBID,
 						dissDBID);
 			}
 			allBindingMaps = doReader.GetDSBindingMaps(null);
 			for (int i=0; i<allBindingMaps.length; ++i) {
 				bMechDBID = lookupBehaviorMechanismDBID(connection,
 						allBindingMaps[i].dsBindMechanismPID);
 				if (bMechDBID == null) {
 					throw new ReplicationException("BehaviorMechanism row "
 							+ "doesn't exist for PID: "
 							+ allBindingMaps[i].dsBindMechanismPID);
 				}
 
 				// Insert dsBindMap row if it doesn't exist.
 				bindingMapDBID = lookupDataStreamBindingMapDBID(connection,
 						bMechDBID, allBindingMaps[i].dsBindMapID);
 				if (bindingMapDBID == null) {
 					// DataStreamBinding row doesn't exist, add it.
 					insertDataStreamBindingMapRow(connection, bMechDBID,
 					allBindingMaps[i].dsBindMapID,
 					allBindingMaps[i].dsBindMapLabel);
 					bindingMapDBID = lookupDataStreamBindingMapDBID(
 							connection,bMechDBID,allBindingMaps[i].dsBindMapID);
 					if (bindingMapDBID == null) {
 						throw new ReplicationException(
 								"lookupdsBindMapDBID row "
 								+ "doesn't exist for bMechDBID: " + bMechDBID
 								+ ", dsBindingMapID: "
 								+ allBindingMaps[i].dsBindMapID);
 					}
 				}
 
 				for (int j=0; j<allBindingMaps[i].dsBindingsAugmented.length;
 						++j) {
 					dsBindingKeyDBID = lookupDataStreamBindingSpecDBID(
 							connection, bMechDBID,
 							allBindingMaps[i].dsBindingsAugmented[j].
 							bindKeyName);
 					if (dsBindingKeyDBID == null) {
 						throw new ReplicationException(
 								"lookupDataStreamBindingDBID row doesn't "
 								+ "exist for bMechDBID: " + bMechDBID
 								+ ", bindKeyName: " + allBindingMaps[i].
 								dsBindingsAugmented[j].bindKeyName + "i=" + i
 								+ " j=" + j);
 					}
 
 					// Insert DataStreamBinding row
                                         //dissDBID = lookupDsBindingMapDBID(connection, bMechDBID, allBindingMaps[i].dsBindMapID);
                                         //if (dissDBID==null) {
                                         //  throw new ReplicationException(
                                         //                  "lookupDsBindingDBID row doesn't "
                                         //                  + "exist for bMechDBID: " + bMechDBID
                                         //                  + ", bindMapID: " + allBindingMaps[i].dsBindMapID + "i=" + i
                                         //                  + " j=" + j);
                                         //}
                                         //System.out.println("dissDBID: "+dissDBID+" bmechDBID: "+bMechDBID+" bindmapID: "+allBindingMaps[i].dsBindMapID);
 					insertDataStreamBindingRow(connection, doDBID,
 							dsBindingKeyDBID,
 							bindingMapDBID,
                                                         //dissDBID,
 							allBindingMaps[i].dsBindingsAugmented[j].seqNo,
 							allBindingMaps[i].dsBindingsAugmented[j].
 							datastreamID,
 							allBindingMaps[i].dsBindingsAugmented[j].DSLabel,
 							allBindingMaps[i].dsBindingsAugmented[j].DSMIME,
 							// sdp - local.fedora.server conversion
 							encodeLocalURL(allBindingMaps[i].dsBindingsAugmented[j].DSLocation),
 							allBindingMaps[i].dsBindingsAugmented[j].DSControlGrp,
 							allBindingMaps[i].dsBindingsAugmented[j].DSVersionID,
 							"1",
 							"A");
 
 				}
 			}
 			return;
 		}
 
         /**
          * <p> Permanently removes a disseminator from the database incuding
          * all associated datastream bindings and datastream binding maps.
          * Associated entries are removed from the following db tables:</p>
          * <ul>
          * <li>doDissAssoc - all entries matching pid of data object.</li>
          * <li>diss - all entries matching disseminator ID provided that no
          * other objects depend on this disseminator.</li>
          * <li>dsBind - all entries matching pid of data object.</li>
          * <li>dsBindMap - all entries matching associated bMech object pid
          * provided that no other objects depend on this binding map.</li>
          * </ul>
          * @param pid Persistent identifier for the data object.
          * @param dissIds Set of disseminator IDs to be removed.
          * @param doReader An instance of DOReader.
          * @param connection A database connection.
          * @throws SQLException If something totally unexpected happened.
          */
         private void purgeDisseminators(String pid, HashSet dissIds, HashSet bmapIds, DOReader doReader, Connection connection)
             throws SQLException
         {
 
           logFinest("DefaultDOReplicator.purgeDisseminators: Entering ------");
           Statement st=null;
           Statement st2=null;
           ResultSet results=null;
           try {
               st=connection.createStatement();
               //
               // READ
               //
               logFinest("Checking do table for " + pid + "...");
               results=logAndExecuteQuery(st, "SELECT doDbID FROM "
                       + "do WHERE doPID='" + pid + "'");
               if (!results.next()) {
                    // must not be a digitalobject...exit early
                    logFinest(pid + " wasn't found in do table..."
                            + "skipping deletion as such.");
                    return;
               }
               int dbid=results.getInt("doDbID");
               results.close();
               logFinest(pid + " was found in do table (DBID="
                       + dbid + ")");
 
               logFinest("Getting dissDbID(s) from doDissAssoc "
                       + "table...");
 
               HashSet dissIdsNotShared = new HashSet();
               logFinest("Getting dissDbID(s) from doDissAssoc "
                       + "table unique to this object...");
               logFinest("DefaultDOReplicator.purgeDisseminators: Found "
                   + dissIds.size() + " dissDbId(s). ");
 
               // Iterate over dissIds and separate those that are unique
               // (i.e., not shared by other objects)
               Iterator iterator = dissIds.iterator();
               while (iterator.hasNext())
               {
                 Integer id = (Integer)iterator.next();
                 logFinest("Getting occurrences of dissDbID(s) in "
                       + "doDissAssoc table...");
                 results=logAndExecuteQuery(st, "SELECT COUNT(*) from "
                       + "doDissAssoc WHERE dissDbID=" + id);
                 while (results.next())
                 {
                   Integer i1 = new Integer(results.getInt("COUNT(*)"));
                   if ( i1.intValue() == 1 )
                   {
                     // A dissDbID that occurs only once indicates that the
                     // disseminator is not used by other objects. In this case,
                     // we want to keep track of this dissDbID.
                     dissIdsNotShared.add(id);
                   }
                 }
                 results.close();
               }
 
               // Iterate over bmapIds and separate those that are unique
               // (i.e., not shared by other objects)
               logFinest("DefaultDOReplicator.purgeDisseminators: Found "
                   + bmapIds.size() + " dsBindMapDbId(s). ");
               iterator = bmapIds.iterator();
               HashSet bmapIdsInUse = new HashSet();
               HashSet bmapIdsShared = new HashSet();
               HashSet bmapIdsNotShared = new HashSet();
               while (iterator.hasNext() )
               {
                 Integer id = (Integer)iterator.next();
                 ResultSet rs = null;
                 logFinest("Getting associated bmapId(s) that are unique "
                     + "for this object in diss table...");
                 st2 = connection.createStatement();
                 rs=logAndExecuteQuery(st2, "SELECT DISTINCT doDbID FROM "
                     + "dsBind WHERE dsBindMapDbID=" + id);
                 int rowCount = 0;
                 while (rs.next())
                 {
                   rowCount++;
                 }
                 if ( rowCount == 1 )
                 {
                   // A dsBindMapDbId that occurs only once indicates that
                   // the dsBindMapId is not used by other objects. In this case,
                   // we want to keep track of this dsBindMapDbId.
                   bmapIdsNotShared.add(id);
                 }
 
                 rs.close();
                 st2.close();
               }
 
               //
               // WRITE
               //
               int rowCount;
 
               // In doDissAssoc table, we are removing rows specific to the
               // doDbId, so remove all dissDbIds (both shared and nonShared).
               logFinest("Attempting row deletion from doDissAssoc "
                       + "table...");
               rowCount=logAndExecuteUpdate(st, "DELETE FROM "
                       + "doDissAssoc WHERE doDbID=" + dbid
                       + " AND ( " + inIntegerSetWhereConditionString("dissDbID", dissIds) + " )");
               logFinest("Deleted " + rowCount + " row(s).");
 
               // In dsBind table, we are removing rows specific to the doDbID,
               // so remove all dsBindMapIds (both shared and nonShared).
               logFinest("Attempting row deletion from dsBind table..");
               rowCount=logAndExecuteUpdate(st, "DELETE FROM dsBind "
                       + "WHERE doDbID=" + dbid
                       + " AND ( " + inIntegerSetWhereConditionString("dsBindMapDbID", bmapIds) + " )");
 
               // In diss table, dissDbIds can be shared by other objects so only
               // remove dissDbIds that are not shared.
               logFinest("Attempting row deletion from diss table...");
               rowCount=logAndExecuteUpdate(st, "DELETE FROM diss WHERE "
                       + inIntegerSetWhereConditionString("dissDbID", dissIdsNotShared));
               logFinest("Deleted " + rowCount + " row(s).");
 
               // In dsBindMap table, dsBindMapIds can be shared by other objects
               // so only remove dsBindMapIds that are not shared.
               logFinest("Attempting row deletion from dsBindMap "
                       + "table...");
               rowCount=logAndExecuteUpdate(st, "DELETE FROM dsBindMap "
                       + "WHERE " + inIntegerSetWhereConditionString(
                       "dsBindMapDbID", bmapIdsNotShared));
               logFinest("Deleted " + rowCount + " row(s).");
 
           } finally {
               if (results != null) results.close();
               if (st!=null) st.close();
               if (st2!=null) st2.close();
               logFinest("DefaultDOReplicator.purgeDisseminators: Exiting ------");
         }
       }
 
 }
