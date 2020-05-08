 /**
  * 
  */
 package hu.modembed.test;
 
 import hu.e.compiler.IWorkflowLauncherListener;
 import hu.modembed.model.core.workflow.Task;
 import hu.modembed.model.core.workflow.WorkflowTask;
 
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.emf.common.util.URI;
 
 /**
  * @author balazs.grill
  *
  */
 public class SysoutWorkflowLauncherListener implements
 		IWorkflowLauncherListener {
 	
 	private long start;
 	
 	private long time(){
 		return System.currentTimeMillis() - start;
 	}
 
 	/* (non-Javadoc)
 	 * @see hu.e.compiler.IWorkflowLauncherListener#started()
 	 */
 	@Override
 	public void started() {
 		start = System.currentTimeMillis();
 	}
 
 	/* (non-Javadoc)
 	 * @see hu.e.compiler.IWorkflowLauncherListener#taskStarted(hu.modembed.model.core.workflow.WorkflowTask)
 	 */
 	@Override
 	public void taskStarted(WorkflowTask task) {
 		String name = ((Task)task).getType();
 		System.out.println("["+time()+"] - "+name);
 	}
 
 	/* (non-Javadoc)
 	 * @see hu.e.compiler.IWorkflowLauncherListener#outputSaving(org.eclipse.emf.common.util.URI)
 	 */
 	@Override
 	public void outputSaving(URI document) {
 		System.out.println("["+time()+"] - Saving "+document);
 	}
 
 	/* (non-Javadoc)
 	 * @see hu.e.compiler.IWorkflowLauncherListener#log(org.eclipse.core.runtime.IStatus)
 	 */
 	@Override
 	public void log(IStatus status) {
		System.out.println("["+time()+"] - "+status.toString());
 
 	}
 
 	/* (non-Javadoc)
 	 * @see hu.e.compiler.IWorkflowLauncherListener#ended()
 	 */
 	@Override
 	public void ended() {
 		System.out.println("["+time()+"] - Workflow finished.");
 	}
 
 }
