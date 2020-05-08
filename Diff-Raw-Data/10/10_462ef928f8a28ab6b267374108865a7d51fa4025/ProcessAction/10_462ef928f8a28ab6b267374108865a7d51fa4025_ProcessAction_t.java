 package pt.ist.expenditureTrackingSystem.presentationTier.actions;
 
 import javax.servlet.http.HttpServletRequest;
 
import pt.ist.expenditureTrackingSystem.domain.processes.AbstractActivity;
 import pt.ist.expenditureTrackingSystem.domain.processes.GenericProcess;
 
 public class ProcessAction extends BaseAction {
 
     protected GenericProcess getProcess(final HttpServletRequest request, final String attributeName) {
 	final GenericProcess genericProcess = getDomainObject(request, attributeName);
 	return genericProcess;
     }
 
     protected GenericProcess getProcess(final HttpServletRequest request) {
 	return getProcess(request, "acquisitionProcessOid");
     }
 
     protected void genericActivityExecution(final GenericProcess genericProcess, final String activityName) {
 	AbstractActivity<GenericProcess> acquitivity = genericProcess.getActivityByName(activityName);
 	acquitivity.execute(genericProcess);
     }
 
     protected void genericActivityExecution(final GenericProcess genericProcess, final String activityName, Object... args) {
 	AbstractActivity<GenericProcess> acquitivity = genericProcess.getActivityByName(activityName);
 	acquitivity.execute(genericProcess,args);
     }
     
     protected void genericActivityExecution(final HttpServletRequest request, final String activityName) {
 	final GenericProcess genericProcess = getProcess(request);
 	genericActivityExecution(genericProcess, activityName);
     }
 
     protected void genericActivityExecution(final HttpServletRequest request, final String activityName, final Object... args) {
 	final GenericProcess genericProcess = getProcess(request);
 	genericActivityExecution(genericProcess, activityName, args);
     }
 }
