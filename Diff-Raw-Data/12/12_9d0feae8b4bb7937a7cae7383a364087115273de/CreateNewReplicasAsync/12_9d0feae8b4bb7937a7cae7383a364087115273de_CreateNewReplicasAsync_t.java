 package org.h2o.db.manager;
 
 import java.sql.SQLException;
 import java.util.HashSet;
 import java.util.Queue;
 import java.util.Set;
 
 import org.h2o.autonomic.numonic.metric.CreateReplicaMetric;
 import org.h2o.autonomic.numonic.ranking.Requirements;
 import org.h2o.db.id.DatabaseID;
 import org.h2o.db.id.TableInfo;
 import org.h2o.db.interfaces.IDatabaseInstanceRemote;
 import org.h2o.db.manager.interfaces.ISystemTableReference;
 import org.h2o.db.replication.ReplicaManager;
 import org.h2o.db.wrappers.DatabaseInstanceWrapper;
 import org.h2o.util.exceptions.MovedException;
 
 import uk.ac.standrews.cs.nds.rpc.RPCException;
 import uk.ac.standrews.cs.nds.util.Diagnostic;
 import uk.ac.standrews.cs.nds.util.DiagnosticLevel;
 import uk.ac.standrews.cs.nds.util.ErrorHandling;
 import uk.ac.standrews.cs.nds.util.PrettyPrinter;
 
 public class CreateNewReplicasAsync implements Runnable {
 
     private final TableManager tableManager;
     private final ReplicaManager replicaManager;
     private final int desiredRelationReplicationFactor;
     private final ISystemTableReference iSystemTableReference;
     private final DatabaseID databaseID;
 
     public CreateNewReplicasAsync(final TableManager tableManager, final ReplicaManager replicaManager, final ISystemTableReference iSystemTableReference, final int desiredRelationReplicationFactor, final DatabaseID databaseID) {
 
         this.tableManager = tableManager;
         this.replicaManager = replicaManager;
         this.iSystemTableReference = iSystemTableReference;
         this.desiredRelationReplicationFactor = desiredRelationReplicationFactor;
         this.databaseID = databaseID;
 
     }
 
     @Override
     public void run() {
 
         try {
             final int currentReplicationFactor = replicaManager.getAllReplicasOnActiveMachines().size();
             final int newReplicasNeeded = desiredRelationReplicationFactor - currentReplicationFactor;
 
             Diagnostic.traceNoEvent(DiagnosticLevel.FINAL, "Deciding whether to create new replicas. Current replication factor is " + currentReplicationFactor + " and the desired replication factor is " + desiredRelationReplicationFactor + ".");
 
             if (newReplicasNeeded > 0) {
                 final Queue<DatabaseInstanceWrapper> potentialReplicaLocations = iSystemTableReference.getRankedListOfInstances(new CreateReplicaMetric(), Requirements.NO_FILTERING);
 
                 Diagnostic.traceNoEvent(DiagnosticLevel.FULL, "Table Manager for " + tableManager.getFullTableName() + " (on " + databaseID + ") will attempt to replicate to " + newReplicasNeeded + " of these machines: " + PrettyPrinter.toString(potentialReplicaLocations));
 
                 DatabaseInstanceWrapper primaryLocation = null;
 
                Diagnostic.traceNoEvent(DiagnosticLevel.FINAL, "** " + PrettyPrinter.toString(replicaManager.getAllReplicasOnActiveMachines()));
 
                 for (final DatabaseInstanceWrapper wrapper : replicaManager.getAllReplicasOnActiveMachines().keySet()) {

                    try {

                        wrapper.getDatabaseInstance().getConnectionString();
                    }
                    catch (final Exception e) {
                        continue;
                    }

                     primaryLocation = wrapper;
                     break;
                 }
 
                 final String createReplicaSQL = "CREATE  REPLICA " + tableManager.getFullTableName() + " FROM '" + primaryLocation.getURL().getURL() + "'";
 
                 final Set<DatabaseInstanceWrapper> locationOfNewReplicas = new HashSet<DatabaseInstanceWrapper>();
 
                 DatabaseInstanceWrapper wrapper = null;
                 while ((wrapper = potentialReplicaLocations.poll()) != null) {
 
                     if (!replicaManager.contains(wrapper)) {
                         Diagnostic.traceNoEvent(DiagnosticLevel.FULL, "Attempting to replicate table state of " + tableManager.getFullTableName() + " to " + wrapper.getURL());
 
                         final IDatabaseInstanceRemote instance = wrapper.getDatabaseInstance();
 
                         try {
 
                             Diagnostic.traceNoEvent(DiagnosticLevel.FULL, "Executing " + createReplicaSQL + " on " + instance.getAddress() + ".");
                             final int result = instance.executeUpdate(createReplicaSQL, true);
 
                             if (result == 0) {
                                 Diagnostic.traceNoEvent(DiagnosticLevel.FULL, "Successfully replicated " + tableManager.getFullTableName() + " onto " + wrapper.getURL());
                                 locationOfNewReplicas.add(wrapper);
                             }
                         }
                         catch (final RPCException e) {
                             ErrorHandling.errorNoEvent("Tried to create replica of " + tableManager.getFullTableName() + " onto " + wrapper.getURL() + ", but couldn't connnect: " + e.getMessage());
 
                             iSystemTableReference.suspectInstanceOfFailure(wrapper.getURL());
                         }
                         catch (final SQLException e) {
                             ErrorHandling.errorNoEvent("Tried to create replica of " + tableManager.getFullTableName() + " onto " + wrapper.getURL() + ", but couldn't connnect: " + e.getMessage());
 
                         }
                     }
                     else {
                         Diagnostic.traceNoEvent(DiagnosticLevel.FULL, "There is already a replica on " + wrapper.getURL() + ", so we won't replicate here.");
                     }
                 } //end of while loop attempting replication.
 
                 // Update meta-data to reflect new replica locations.
 
                 Diagnostic.traceNoEvent(DiagnosticLevel.FINAL, "New replicas created at: " + locationOfNewReplicas);
 
                 for (final DatabaseInstanceWrapper databaseInstanceWrapper : locationOfNewReplicas) {
                     // Update Table Manager meta-data.
 
                     final TableInfo tableDetails = new TableInfo(tableManager.getTableInfo(), databaseInstanceWrapper.getURL());
                     try {
                         tableManager.addReplicaInformation(tableDetails);
                     }
                     catch (final SQLException e) {
                         ErrorHandling.errorNoEvent("Failed to add information regarding new replicas for " + tableManager.getFullTableName() + " on " + databaseID + ".");
 
                     }
                     catch (final RPCException e) {
                         // TODO Auto-generated catch block
                         e.printStackTrace();
                     }
                     catch (final MovedException e) {
                         // TODO Auto-generated catch block
                         e.printStackTrace();
                     }
 
                 }
 
             }
         }
         catch (final RPCException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
         catch (final MovedException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
 
     }
 
 }
