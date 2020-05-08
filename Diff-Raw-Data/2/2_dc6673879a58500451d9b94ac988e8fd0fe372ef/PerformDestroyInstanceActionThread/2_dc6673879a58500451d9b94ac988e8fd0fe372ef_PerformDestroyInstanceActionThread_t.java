 package org.jboss.tools.deltacloud.ui.views;
 
 import java.text.MessageFormat;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.swt.widgets.Display;
 import org.jboss.tools.deltacloud.core.DeltaCloud;
 import org.jboss.tools.deltacloud.core.DeltaCloudException;
 import org.jboss.tools.deltacloud.core.DeltaCloudInstance;
 import org.jboss.tools.deltacloud.ui.ErrorUtils;
 
 public class PerformDestroyInstanceActionThread extends Job {
 	private DeltaCloud cloud;
 	private DeltaCloudInstance instance;
 	private String taskName;
 
 	public PerformDestroyInstanceActionThread(DeltaCloud cloud, DeltaCloudInstance instance,
 			String title, String taskName) {
 		super(title);
 		this.cloud = cloud;
 		this.instance = instance;
 		this.taskName = taskName;
 	}
 
 	@Override
 	public IStatus run(IProgressMonitor pm) {
 		String id = instance.getId();
 		try {
 			pm.beginTask(taskName, IProgressMonitor.UNKNOWN);
 			pm.worked(1);
 			// To handle the user starting a new action when we haven't
 			// confirmed the last one yet,
 			// cancel the previous job and then go on performing this action
 			Job job = cloud.getInstanceJob(id);
 			if (job != null) {
 				job.cancel();
 				try {
 					job.join();
 				} catch (InterruptedException e) {
 					// do nothing, this is ok
 				}
 			}
 			cloud.registerInstanceJob(id, this);
 			Display.getDefault().asyncExec(new Runnable() {
 				@Override
 				public void run() {
 					try {
						cloud.performInstanceAction(instance.getId(), DeltaCloudInstance.DESTROY);
 					} catch (DeltaCloudException e) {
 						// TODO internationalize strings
 						ErrorUtils.handleError("Error",
 								MessageFormat.format("Could not destroy instance {0}", instance.getName()), e,
 								Display.getDefault().getActiveShell());
 					}
 				}
 			});
 		} finally {
 			cloud.removeInstanceJob(id, this);
 			pm.done();
 		}
 		return Status.OK_STATUS;
 	}
 }
