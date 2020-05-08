 package pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified;
 
 import java.util.TreeSet;
 
 import module.workflow.domain.ActivityLog;
 import module.workflow.domain.WorkflowLog;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.refund.RefundProcess;
 
 public class Util {
 
     public static boolean isAppiableForYear(final int year, final SimplifiedProcedureProcess simplifiedProcedureProcess) {
 	final TreeSet<ActivityLog> logs = new TreeSet<ActivityLog>(WorkflowLog.COMPARATOR_BY_WHEN_REVERSED);
	for (WorkflowLog log : simplifiedProcedureProcess.getExecutionLogsSet()) {
 	    logs.add((ActivityLog) log);
 	}
 	for (final ActivityLog genericLog : logs) {
 	    if (genericLog.getOperation().equals("RevertSkipPurchaseOrderDocument")) {
 		return false;
 	    }
 	    if (genericLog.getWhenOperationWasRan().getYear() == year && matchesAppiableForYearActivity(year, genericLog)) {
 		return true;
 	    }
 	}
 	return false;
     }
 
     public static boolean isAppiableForYear(final int year, final RefundProcess refundProcess) {
 	// TODO : implement this properly... until then always count
 	// everything... which will work because there is still only one year...
 	// :)
 	// Currently I'm not sure whether this should be based on the invoice
 	// date, or some authorization date.
 	return year == 2009;
     }
 
     private static boolean matchesAppiableForYearActivity(final int year, final ActivityLog genericLog) {
 	return genericLog.getOperation().equals("SendAcquisitionRequestToSupplier")
 		|| genericLog.getOperation().equals("SendPurchaseOrderToSupplier")
 		|| genericLog.getOperation().equals("SkipPurchaseOrderDocument");
     }
 
 }
