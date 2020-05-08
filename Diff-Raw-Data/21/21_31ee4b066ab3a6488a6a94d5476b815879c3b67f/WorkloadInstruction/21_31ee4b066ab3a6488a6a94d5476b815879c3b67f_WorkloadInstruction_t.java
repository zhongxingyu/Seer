 package org.h2o.eval.script.coord.instructions;
 
 import java.rmi.RemoteException;
 
 import org.h2o.eval.interfaces.WorkloadException;
 import org.h2o.util.exceptions.StartupException;
 
 import uk.ac.standrews.cs.nds.util.Diagnostic;
 import uk.ac.standrews.cs.nds.util.DiagnosticLevel;
 
 public class WorkloadInstruction implements Instruction {
 
     private static final long serialVersionUID = -5175906689517121113L;
 
     /**
      * Workload to be executed.
      */
    public final String workloadFilePath;
 
     /**
      * How long the workload should be executed for.
      */
     public final Long duration;
 
     private final String id;
 
     public WorkloadInstruction(final String id, final String workloadFile, final Long duration) {
 
         this.id = id;
        this.workloadFilePath = workloadFile;
         this.duration = duration == null ? 0 : duration;
 
     }
 
     @Override
     public void execute(final CoordinatorScriptState coordState) throws RemoteException, StartupException, WorkloadException {
 
        coordState.getCoordintor().executeWorkload(id, workloadFilePath, duration);
 
        Diagnostic.traceNoEvent(DiagnosticLevel.FULL, "CSCRIPT: Executing workload '" + workloadFilePath + "' for '" + duration + "', on '" + id + "'.");
 
         if (!coordState.hasStartedExecution()) {
             coordState.startExecution();
 
         }
     }
 
 }
