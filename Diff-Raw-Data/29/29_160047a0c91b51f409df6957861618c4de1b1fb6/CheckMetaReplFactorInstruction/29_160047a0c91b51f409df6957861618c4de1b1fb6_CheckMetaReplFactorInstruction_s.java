 package org.h2o.eval.script.coord.instructions;
 
 import java.rmi.RemoteException;
 import java.sql.SQLException;
 
 import org.h2o.eval.interfaces.IWorker;
 import org.h2o.eval.interfaces.WorkloadException;
 import org.h2o.util.exceptions.StartupException;
 
 import uk.ac.standrews.cs.nds.util.Diagnostic;
 import uk.ac.standrews.cs.nds.util.DiagnosticLevel;
 
 public class CheckMetaReplFactorInstruction implements Instruction {
 
     /**
      * 
      */
     private static final long serialVersionUID = 1L;
     private final String tableName;
     private final int expected;
 
     public CheckMetaReplFactorInstruction(final String tableName, final int expected) {
 
         this.tableName = tableName;
         this.expected = expected;
 
     }
 
     @Override
     public void execute(final CoordinatorScriptState coordState) throws RemoteException, StartupException, WorkloadException, SQLException {
 
         final String query = "GET METAREPLICATION FACTOR " + tableName;
 
         Diagnostic.traceNoEvent(DiagnosticLevel.FULL, "CSCRIPT: Executing query '" + query + "' on '0'.");
 
         final IWorker worker = coordState.getScriptedInstance(0); //always perform query on first machine.
 
         int replication_factor = 0;
         try {
             replication_factor = coordState.getCoordintor().executeQuery(worker, query);
         }
         catch (final Exception e) {
             // Happens when the system can't communicate with the System Table - some co-ordinator tests may deliberately cause this.
            e.printStackTrace();
         }
 
        if (replication_factor != expected) { throw new WorkloadException("Replication factor was not correct. Expected " + expected + ", but it was " + replication_factor); }
 
     }

 }
