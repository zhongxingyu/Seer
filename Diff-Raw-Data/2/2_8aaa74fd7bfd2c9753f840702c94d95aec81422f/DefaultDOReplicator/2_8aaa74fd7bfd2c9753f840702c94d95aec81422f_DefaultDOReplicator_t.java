 package fedora.server.storage.replication;
 
 import java.util.*;
 import java.sql.*;
 import java.io.*;
 
 import fedora.server.errors.*;
 import fedora.server.errors.*;
 import fedora.server.storage.*;
 import fedora.server.storage.types.*;
 
 import fedora.server.Module;
 import fedora.server.Server;
 import fedora.server.storage.ConnectionPoolManager;
 import fedora.server.errors.ModuleInitializationException;
 
 /**
  * A Module that replicates digital object information to the dissemination
  * database.
  * <p></p>
  * Converts data read from the object reader interfaces and creates or
  * updates the corresponding database rows in the dissemination database.
  *
  * FIXME: recast sqlexceptions as replicationexceptions here and in interface
  *
  * @author Paul Charlton, cwilper@cs.cornell.edu
  * @version 1.0
  */
 public class DefaultDOReplicator
         extends Module
         implements DOReplicator {
 
     private ConnectionPool m_pool;
     private RowInsertion m_ri;
     private DBIDLookup m_dl;
 
     public DefaultDOReplicator(Map moduleParameters, Server server, String role)
             throws ModuleInitializationException {
         super(moduleParameters, server, role);
     }
 
     public void initModule() {
         m_ri=new RowInsertion();
         m_dl=new DBIDLookup();
     }
 
     public void postInitModule()
             throws ModuleInitializationException {
         try {
             ConnectionPoolManager mgr=(ConnectionPoolManager)
                     getServer().getModule(
                     "fedora.server.storage.ConnectionPoolManager");
             m_pool=mgr.getPool();
         } catch (ServerException se) {
             throw new ModuleInitializationException(
                     "Error getting default pool: " + se.getMessage(),
                     getRole());
         }
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
             System.out.println("bdefPID: "+bDefPID);
             bDefLabel = bDefReader.GetObjectLabel();
             m_ri.insertBehaviorDefinitionRow(connection, bDefPID, bDefLabel);
 
             // Insert method rows
             bDefDBID = m_dl.lookupBehaviorDefinitionDBID(connection, bDefPID);
             if (bDefDBID == null) {
                 throw new ReplicationException(
                     "BehaviorDefinition row doesn't exist for PID: "
                     + bDefPID);
             }
             behaviorDefs = bDefReader.GetBehaviorMethods(null);
             for (int i=0; i<behaviorDefs.length; ++i) {
                 m_ri.insertMethodRow(connection, bDefDBID,
                         behaviorDefs[i].methodName,
                         behaviorDefs[i].methodLabel);
 
                 // Insert method parm rows
                 methDBID =  m_dl.lookupMethodDBID(connection, bDefDBID,
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
                   m_ri.insertMethodParmRow(connection, methDBID, bDefDBID,
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
             re.printStackTrace();
             throw re;
         } catch (ServerException se) {
             se.printStackTrace();
             throw new ReplicationException("Replication exception caused by "
                     + "ServerException - " + se.getMessage());
         } finally {
             if (connection!=null) {
                 connection.rollback();
                 connection.setAutoCommit(true);
                 m_pool.free(connection);
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
         Connection connection=null;
         try {
             BMechDSBindSpec dsBindSpec;
             MethodDef behaviorBindings[];
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
             dsBindSpec = bMechReader.GetDSBindingSpec(null);
             bDefPID = dsBindSpec.bDefPID;
 
             bDefDBID = m_dl.lookupBehaviorDefinitionDBID(connection, bDefPID);
             if (bDefDBID == null) {
                 throw new ReplicationException("BehaviorDefinition row doesn't "
                         + "exist for PID: " + bDefPID);
             }
 
             bMechPID = bMechReader.GetObjectPID();
             bMechLabel = bMechReader.GetObjectLabel();
 
             m_ri.insertBehaviorMechanismRow(connection, bDefDBID, bMechPID,
                     bMechLabel);
 
             // Insert dsBindSpec rows
             bMechDBID = m_dl.lookupBehaviorMechanismDBID(connection, bMechPID);
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
 
               m_ri.insertDataStreamBindingSpecRow(connection,
                   bMechDBID, dsBindSpec.dsBindRules[i].bindingKeyName,
                   ordinality_flag, cardinality,
                   dsBindSpec.dsBindRules[i].bindingLabel);
 
               // Insert dsMIME rows
               dsBindingKeyDBID =
                   m_dl.lookupDataStreamBindingSpecDBID(connection,
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
                 m_ri.insertDataStreamMIMERow(connection,
                     dsBindingKeyDBID,
                     dsBindSpec.dsBindRules[i].bindingMIMETypes[j]);
               }
             }
 
             behaviorBindings = bMechReader.GetBehaviorMethods(null);
 
             // Insert mechImpl rows
             for (int i=0; i<behaviorBindings.length; ++i) {
                 behaviorBindingsEntry =
                         (MethodDefOperationBind)behaviorBindings[i];
                 if (!behaviorBindingsEntry.protocolType.equals("HTTP")) {
 
                   // For the time being, ignore bindings other than HTTP.
                   continue;
                 }
 
                 // Insert mechDefParm rows
                 methodDBID = m_dl.lookupMethodDBID(connection, bDefDBID,
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
                   if (methodParmDefs[j].parmType.equalsIgnoreCase("fedora:defaultInputType"))
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
                   m_ri.insertMechDefaultMethodParmRow(connection, methodDBID, bMechDBID,
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
                             m_dl.lookupDataStreamBindingSpecDBID(connection,
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
                         m_ri.insertMechanismImplRow(connection, bMechDBID,
                             bDefDBID, methodDBID, dsBindingKeyDBID,
                             "http", "text/html",
                             behaviorBindingsEntry.serviceBindingAddress,
                             behaviorBindingsEntry.operationLocation, "1");
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
                 connection.rollback();
                 connection.setAutoCommit(true);
                 m_pool.free(connection);
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
         Connection connection=null;
         try {
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
 System.out.println("insert do");
             // Insert Digital Object row
             doPID = doReader.GetObjectPID();
             doLabel = doReader.GetObjectLabel();
 
             m_ri.insertDigitalObjectRow(connection, doPID, doLabel);
 
             doDBID = m_dl.lookupDigitalObjectDBID(connection, doPID);
             if (doDBID == null) {
                 throw new ReplicationException("do row doesn't "
                         + "exist for PID: " + doPID);
             }
 
             disseminators = doReader.GetDisseminators(null);
             for (int i=0; i<disseminators.length; ++i) {
                 bDefDBID = m_dl.lookupBehaviorDefinitionDBID(connection,
                         disseminators[i].bDefID);
                 if (bDefDBID == null) {
                     throw new ReplicationException("BehaviorDefinition row "
                             + "doesn't exist for PID: "
                             + disseminators[i].bDefID);
                 }
                 bMechDBID = m_dl.lookupBehaviorMechanismDBID(connection,
                         disseminators[i].bMechID);
                 if (bMechDBID == null) {
                     throw new ReplicationException("BehaviorMechanism row "
                             + "doesn't exist for PID: "
                             + disseminators[i].bMechID);
                 }
 System.out.println("insert diss");
                 // Insert Disseminator row if it doesn't exist.
                 dissDBID = m_dl.lookupDisseminatorDBID(connection, bDefDBID,
                         bMechDBID, disseminators[i].dissID);
                 if (dissDBID == null) {
                     // Disseminator row doesn't exist, add it.
                     m_ri.insertDisseminatorRow(connection, bDefDBID, bMechDBID,
                     disseminators[i].dissID, disseminators[i].dissLabel);
                     dissDBID = m_dl.lookupDisseminatorDBID(connection, bDefDBID,
                             bMechDBID, disseminators[i].dissID);
                     if (dissDBID == null) {
                         throw new ReplicationException("diss row "
                                 + "doesn't exist for PID: "
                                 + disseminators[i].dissID);
                     }
                 }
 System.out.println("insert dodissassoc");
                 // Insert doDissAssoc row
                 m_ri.insertDigitalObjectDissAssocRow(connection, doDBID,
                         dissDBID);
             }
             try{
             allBindingMaps = doReader.GetDSBindingMaps(null);
 
 
 
             for (int i=0; i<allBindingMaps.length; ++i) {
                 bMechDBID = m_dl.lookupBehaviorMechanismDBID(connection,
                         allBindingMaps[i].dsBindMechanismPID);
                 if (bMechDBID == null) {
                     throw new ReplicationException("BehaviorMechanism row "
                             + "doesn't exist for PID: "
                             + allBindingMaps[i].dsBindMechanismPID);
                 }
 
 System.out.println("insert dsbindingmap");
                 // Insert dsBindMap row if it doesn't exist.
                 bindingMapDBID = m_dl.lookupDataStreamBindingMapDBID(connection,
                         bMechDBID, allBindingMaps[i].dsBindMapID);
                 if (bindingMapDBID == null) {
                     // DataStreamBinding row doesn't exist, add it.
                     m_ri.insertDataStreamBindingMapRow(connection, bMechDBID,
                     allBindingMaps[i].dsBindMapID,
                     allBindingMaps[i].dsBindMapLabel);
                     bindingMapDBID = m_dl.lookupDataStreamBindingMapDBID(
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
                     dsBindingKeyDBID = m_dl.lookupDataStreamBindingSpecDBID(
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
 System.out.println("insert dsbinding");
                     // Insert DataStreamBinding row
                     m_ri.insertDataStreamBindingRow(connection, doDBID,
                             dsBindingKeyDBID,
                             bindingMapDBID,
                             allBindingMaps[i].dsBindingsAugmented[j].seqNo,
                             allBindingMaps[i].dsBindingsAugmented[j].
                             datastreamID,
                             allBindingMaps[i].dsBindingsAugmented[j].bindLabel,
                             allBindingMaps[i].dsBindingsAugmented[j].DSMIME,
                             allBindingMaps[i].dsBindingsAugmented[j].DSLocation,
                             allBindingMaps[i].dsBindingsAugmented[j].DSControlGrp,
                             allBindingMaps[i].dsBindingsAugmented[j].DSVersionID,
                             "1");
 
                 }
             }
             } catch(Exception e)
             {
               e.printStackTrace();
                 }
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
                 connection.rollback();
                 connection.setAutoCommit(true);
                 m_pool.free(connection);
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
      * @throws SQLException If something totally unexpected happened.
      */
     private void deleteBehaviorDefinition(Connection connection, String pid)
             throws SQLException {
         logFinest("Entered DefaultDOReplicator.deleteBehaviorDefinition");
         Statement st=null;
         try {
 		     st=connection.createStatement();
             ResultSet results;
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
             if (st!=null) {
                 try {
                     st.close();
                 } catch (SQLException sqle) {}
             }
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
      * @throws SQLException If something totally unexpected happened.
      */
     private void deleteBehaviorMechanism(Connection connection, String pid)
             throws SQLException {
         logFinest("Entered DefaultDOReplicator.deleteBehaviorMechanism");
         Statement st=null;
         try {
 		     st=connection.createStatement();
             ResultSet results;
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
             if (st!=null) {
                 try {
                     st.close();
                 } catch (SQLException sqle) {}
             }
             logFinest("Exiting DefaultDOReplicator.deleteBehaviorMechanism");
         }
     }
 
     /**
      * Deletes all rows pertinent to the given digital object (treated as a
      * regular data object) if they exist.
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
      * @throws SQLException If something totally unexpected happened.
      */
     private void deleteDigitalObject(Connection connection, String pid)
             throws SQLException {
         logFinest("Entered DefaultDOReplicator.deleteDigitalObject");
         Statement st=null;
         try {
 		     st=connection.createStatement();
             ResultSet results;
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
             results=logAndExecuteQuery(st, "SELECT dissDbID from "
                     + "doDissAssoc WHERE doDbID=" + dbid);
             while (results.next()) {
                 dissIds.add(new Integer(results.getInt("dissDbID")));
             }
             results.close();
             HashSet bMechIds = new HashSet();
             logFinest("Getting dissDbID(s) from doDissAssoc "
                     + "table unique to this object...");
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
                 if ( i1.intValue() > 1 )
                 {
                   dissIds.remove(id);
                 } else
                 {
                   ResultSet rs;
                   logFinest("Getting associated bMechDbID(s) that are unique "
                         + "for this object in diss table...");
                   rs=logAndExecuteQuery(st, "SELECT bMechDbID from "
                     + "diss WHERE dissDbID=" + id);
                   while (rs.next())
                   {
                     bMechIds.add(new Integer(rs.getInt("bMechDbID")));
                   }
                   rs.close();
                 }
               }
               results.close();
 
             }
             logFinest("Found " + dissIds.size() + " dissDbID(s).");
             logFinest("Getting bMechDbIDs matching dsBindMapDbID(s) from dsBind "
                     + "table...");
             logFinest("Getting dsBindMapDbID(s) from dsBind "
                     + "table...");
             //HashSet bmapIds=new HashSet();
             //results=logAndExecuteQuery(st, "SELECT dsBindMapDbID FROM "
             //        + "dsBind WHERE doDbID=" + dbid);
             //while (results.next()) {
             //    bmapIds.add(new Integer(results.getInt("dsBindMapDbID")));
             //}
             //results.close();
             //logFinest("Found " + bmapIds.size() + " dsBindMapDbID(s).");
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
             logFinest("Attempting row deletion from diss table...");
             rowCount=logAndExecuteUpdate(st, "DELETE FROM diss WHERE "
                     + inIntegerSetWhereConditionString("dissDbID", dissIds));
             logFinest("Deleted " + rowCount + " row(s).");
             logFinest("Attempting row deletion from dsBindMap "
                     + "table...");
             rowCount=logAndExecuteUpdate(st, "DELETE FROM dsBindMap "
                     + "WHERE " + inIntegerSetWhereConditionString(
             //        "dsBindMapDbID", bmapIds));
                       "bMechDbID", bMechIds));
             logFinest("Deleted " + rowCount + " row(s).");
         } finally {
             if (st!=null) {
                 try {
                     st.close();
                 } catch (SQLException sqle) {}
             }
             logFinest("Exiting DefaultDOReplicator.deleteDigitalObject");
         }
     }
 
     /**
      * Removes a digital object from the dissemination database.
      * <p></p>
      * If the object is a behavior definition or mechanism, it's deleted
      * as such, and then an attempt is made to delete it as a regular
      * digital object as well.
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
 
 /*
   <table name="BehaviorDefinition" primaryKey="BDEF_DBID">
     <column name="BDEF_DBID" type="int(11)" notNull="true" autoIncrement="true"/>
     <column name="BDEF_PID" type="varchar(32)" notNull="true" default="" unique="true"/>
     <column name="BDEF_Label" type="varchar(255)" notNull="true" default="" index="BDEF_Label"/>
   </table>
   <table name="BehaviorMechanism" primaryKey="BMECH_DBID">
     <column name="BMECH_DBID" type="int(11)" notNull="true" autoIncrement="true"/>
     <column name="BDEF_DBID" type="int(11)" notNull="true" default="0" index="BDEF_ID"/>
     <column name="SMType_DBID" type="int(11)" notNull="true" default="0" index="SMType_ID"/>
     <column name="BMECH_PID" type="varchar(32)" notNull="true" default="" unique="true"/>
     <column name="BMECH_Label" type="varchar(255)" notNull="true" default="" index="BMECH_Label"/>
   </table>
   <table name="DataStreamBinding">
     <column name="DO_DBID" type="int(11)" notNull="true" default="0" index="DO_DBID"/>
     <column name="DSBindingKey_DBID" type="int(11)" notNull="true" default="0" index="DSBindingKey_DBID"/>
     <column name="BindingMap_DBID" type="int(11)" notNull="true" default="0"/>
     <column name="DSBinding_DS_BindingKey_Seq" type="int(11)" notNull="true" default="0" index="DOBindingMap_DSBindingKey_Seq"/>
     <column name="DSBinding_DS_ID" type="varchar(32)" notNull="true" default="" index="DOBindingMap_DS_ID"/>
     <column name="DSBinding_DS_Label" type="varchar(255)" notNull="true" default="" index="DOBindingMap_DS_Label"/>
     <column name="DSBinding_DS_MIME" type="varchar(32)" notNull="true" default="" index="DOBindingMap_DS_MIME"/>
     <column name="DSBinding_DS_Location" type="varchar(255)" notNull="true" default="" index="DOBindingMap_DS_URL"/>
     <column name="POLICY_DBID" type="int(11)" notNull="true" default="0" index="POLICY_ID"/>
   </table>
   <table name="DataStreamBindingMap">
     <column name="BindingMap_DBID" type="int(11)" notNull="true" autoIncrement="true" unique="true"/>
     <column name="BMECH_DBID" type="int(11)" notNull="true" default="0"/>
     <column name="DSBindingMap_ID" type="varchar(32)" notNull="true" default=""/>
     <column name="DSBindingMap_Label" type="varchar(255)" notNull="true" default=""/>
   </table>
   <table name="DataStreamBindingSpec" primaryKey="DSBindingKey_DBID">
     <column name="DSBindingKey_DBID" type="int(11)" notNull="true" autoIncrement="true"/>
     <column name="BMECH_DBID" type="int(11)" notNull="true" default="0" index="BMECH_DBID"/>
     <column name="DSBindingSpec_Name" type="varchar(32)" notNull="true" default="" index="DSBindingSpec_Name"/>
     <column name="DSBindingSpec_Ordinality_Flag" type="char(1)" notNull="true" default="N" index="DSBindingSpec_Ordinality_Flag"/>
     <column name="DSBindingSpec_Cardinality" type="smallint(6)" notNull="true" default="1" index="DSBindingSpec_Cardinality"/>
     <column name="DSBindingSpec_Label" type="varchar(255)" notNull="true" default="" index="DSBindingSpec_Label"/>
   </table>
   <table name="DataStreamMIME">
     <column name="DSBindingKey_DBID" type="int(11)" notNull="true" default="0" index="DSBindingKey_DBID"/>
     <column name="DSMIME_Name" type="varchar(32)" notNull="true" default="" index="DSBindingKeyMIME_Name"/>
   </table>
   <table name="DigitalObject" primaryKey="DO_DBID">
     <column name="DO_DBID" type="int(11)" notNull="true" autoIncrement="true"/>
     <column name="DO_PID" type="varchar(32)" notNull="true" default="" unique="true"/>
     <column name="DO_Label" type="varchar(255)" notNull="true" default="" index="DO_Label"/>
   </table>
   <table name="DigitalObjectDissAssoc" primaryKey="DO_DBID,DISS_DBID">
     <column name="DO_DBID" type="int(11)" notNull="true" default="0"/>
     <column name="DISS_DBID" type="int(11)" notNull="true" default="0"/>
   </table>
   <table name="Disseminator" primaryKey="DISS_DBID">
     <column name="DISS_DBID" type="int(11)" notNull="true" autoIncrement="true"/>
     <column name="BDEF_DBID" type="int(11)" notNull="true" default="0" index="BDEF_ID"/>
     <column name="BMECH_DBID" type="int(11)" notNull="true" default="0" index="BMECH_ID"/>
     <column name="DISS_ID" type="varchar(32)" notNull="true" default="" index="DISS_Name"/>
     <column name="DISS_Label" type="varchar(255)" notNull="true" default="" index="DISS_Label"/>
   </table>
   <table name="MechanismImpl" primaryKey="BMECH_DBID,BDEF_DBID,METH_DBID,DSBindingKey_DBID">
     <column name="BMECH_DBID" type="int(11)" notNull="true" default="0"/>
     <column name="BDEF_DBID" type="int(11)" notNull="true" default="0"/>
     <column name="METH_DBID" type="int(11)" notNull="true" default="0"/>
     <column name="DSBindingKey_DBID" type="int(11)" notNull="true" default="0"/>
     <column name="MECHImpl_Protocol_Type" type="varchar(32)" notNull="true" default="" index="MI_Protocol_Type"/>
     <column name="MECHImpl_Return_Type" type="varchar(32)" notNull="true" default="" index="MI_Return_Type"/>
     <column name="MECHImpl_Address_Location" type="varchar(255)" notNull="true" default="" index="MI_Address_Location"/>
     <column name="MECHImpl_Operation_Location" type="varchar(255)" notNull="true" default="" index="MI_Operation_Location"/>
     <column name="POLICY_DBID" type="int(11)" notNull="true" default="0" index="POLICY_ID"/>
   </table>
   <table name="Method" primaryKey="METH_DBID,BDEF_DBID">
     <column name="METH_DBID" type="int(11)" notNull="true" autoIncrement="true"/>
     <column name="BDEF_DBID" type="int(11)" notNull="true" default="0"/>
     <column name="METH_Name" type="varchar(32)" notNull="true" default="" index="METH_Name"/>
     <column name="METH_Label" type="varchar(255)" notNull="true" default="" index="METH_Label"/>
   </table>
   <table name="PIDRegistry" primaryKey="PID_DBID">
     <column name="PID_DBID" type="int(11)" notNull="true" autoIncrement="true"/>
     <column name="PID" type="varchar(32)" notNull="true" default="" unique="true"/>
     <column name="Location" type="varchar(255)" notNull="true" default=""/>
   </table>
   <table name="TempRegistry" primaryKey="PID_DBID">
     <column name="PID_DBID" type="int(11)" notNull="true" autoIncrement="true"/>
     <column name="PID" type="varchar(32)" notNull="true" default="" unique="true"/>
     <column name="Location" type="varchar(255)" notNull="true" default=""/>
   </table>
   <table name="Parameter">
     <column name="METH_DBID" type="int(11)" notNull="true" default="0" index="METH_DBID"/>
     <column name="BDEF_DBID" type="int(11)" notNull="true" default="0" index="BDEF_DBID"/>
     <column name="PARM_Name" type="varchar(32)" notNull="true" default="" index="PARM_Name"/>
     <column name="PARM_Default_Value" type="varchar(32)" index="PARM_Default_Value"/>
     <column name="PARM_Required_Flag" type="varchar(5)" notNull="true" default="false" index="PARM_Required_Flag"/>
     <column name="PARM_Label" type="varchar(255)" notNull="true" default="" index="PARM_Label"/>
   </table>
   <table name="Policy" primaryKey="POLICY_ID">
     <column name="POLICY_ID" type="int(11)" notNull="true" autoIncrement="true"/>
     <column name="POLICY_Name" type="varchar(32)" notNull="true" default="" index="POLICY_Name"/>
     <column name="POLICY_Rule" type="varchar(255)" notNull="true" default="" index="POLICY_Rule"/>
     <column name="POLICY_Label" type="varchar(255)" notNull="true" default="0"/>
   </table>
   <table name="ObjectRegistry">
     <comment>This is used internally to keep track of objects in the definitive
              store.  When an object is ingested or newly created, a PID
              has been assigned, and the object is written to the definitive
              store, an entry is created here.
              The entry will be: pid, 0, (userid from context), N
              A write lock is indicated by a non-null LockingUser value.
              As you can see above, when an object is created in the
              repository, it is automatically locked.
              Objects are only deleted from this table in the following two
              cases:  1) When ModifiedFlag is 'D' and DOWriter.commit() is
              called.  2) When DOWriter.cancel() is called and SystemVersion
              is '0' (zero).
     </comment>
     <column name="DO_PID" type="varchar(32)" notNull="true">
       <comment>The PID of the object</comment>
     </column>
     <column name="FO_TYPE" type="char(1)" notNull="true" default="O">
       <comment>The Fedora Object Type of the object.  This indicates
                whether it's a regular object (O), behavior definition object
                (D), or behavior mechanism object (M).</comment>
     </column>
     <column name="SystemVersion" type="smallint(6)" notNull="true" default="0">
       <comment>The system version of the object.  This starts at zero on
                initial creation or import, and is subsequently incremented by
                one each time a change is committed to the definitive store.
       </comment>
     </column>
     <column name="LockingUser" type="varchar(32)" notNull="false">
       <comment>The userId of the user owning the write lock on the object.
                If the object is not locked, this is NULL.
       </comment>
     </column>
     <column name="ModifiedFlag" type="char(1)" notNull="true" default="N">
       <comment>Indicates the kind of pending modification to permanent storage.
                Changes that have been save()d but not commit()ed reside
                in a temporary storage area.  They can either reflect a
                modification or a deletion of the original object.
                This is signified by "M" or "D", respectively. When
                no change is pending, the value is "N".
       </comment>
     </column>
     <column name="ObjectState" type="char(1)" notNull="true" default="A">
       <comment>The state of the object (currently unused)</comment>
     </column>
   </table>
   <table name="ObjectReplicationJob">
     <comment>Used as a queue for replication jobs.</comment>
     <column name="DO_PID" type="varchar(32)" notNull="true">
       <comment>The object pid</comment>
     </column>
     <column name="Action" type="char(1)" notNull="true">
       <comment>M or D, meaning "modified" or "deleted"</comment>
     </column>
   </table>
   <table name="StructureMapType" primaryKey="SMType_DBID">
     <column name="SMType_DBID" type="int(11)" notNull="true" autoIncrement="true"/>
     <column name="SMType_Name" type="varchar(32)" notNull="true" default="" index="SMType_Name"/>
     <column name="SMType_Label" type="varchar(255)" notNull="true" default="" index="SMType_Label"/>
   </table>*/
 
 
 }
