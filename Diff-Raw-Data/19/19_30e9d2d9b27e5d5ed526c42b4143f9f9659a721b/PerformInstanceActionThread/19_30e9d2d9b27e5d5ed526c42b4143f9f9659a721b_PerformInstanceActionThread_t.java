 /*******************************************************************************
  * Copyright (c) 2010 Red Hat, Inc.
  * Distributed under license by Red Hat, Inc. All rights reserved.
  * This program is made available under the terms of the
  * Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Red Hat, Inc. - initial API and implementation
  ******************************************************************************/
 package org.jboss.tools.deltacloud.ui.views;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.jboss.tools.common.log.StatusFactory;
 import org.jboss.tools.deltacloud.core.DeltaCloud;
 import org.jboss.tools.deltacloud.core.DeltaCloudException;
 import org.jboss.tools.deltacloud.core.DeltaCloudInstance;
import org.jboss.tools.deltacloud.ui.Activator;
import org.jboss.tools.internal.deltacloud.ui.utils.UIUtils;
 
 public class PerformInstanceActionThread extends Job {

	private static final String INSTANCEACTION_ERROR_TITLE = "InstanceActionError.title"; //$NON-NLS-1$
	private static final String INSTANCEACTION_ERROR_MESSAGE = "InstanceActionError.msg"; //$NON-NLS-1$

 	private DeltaCloud cloud;
 	private DeltaCloudInstance instance;
 	private String action;
 	private String taskName;
 	private String expectedState;
 	
  	public PerformInstanceActionThread(DeltaCloud cloud, DeltaCloudInstance instance, 
  			String action, String title, String taskName, String expectedState) {
  		super(title);
  		this.cloud = cloud;
  		this.instance = instance;
  		this.action = action;
  		this.taskName = taskName;
  		this.expectedState = expectedState;
  	}
  	
 	@Override
 	public IStatus run(IProgressMonitor pm) {
 		String id = instance.getId();
 		try {
 			pm.beginTask(taskName, IProgressMonitor.UNKNOWN);
 			pm.worked(1);
 			// To handle the user starting a new action when we haven't confirmed the last one yet,
 			// cancel the previous job and then go on performing this action
 			Job job = cloud.getActionJob(id);
 			if (job != null) {
 				job.cancel();
 				try {
 					job.join();
 				} catch (InterruptedException e) {
 					// do nothing, this is ok
 				}
 			}
 			cloud.performInstanceAction(id, action);
 			while (instance != null && !(instance.getState().equals(expectedState))
 					&& !(instance.getState().equals(DeltaCloudInstance.TERMINATED))) {
 				instance = cloud.refreshInstance(id);
 				try {
 					Thread.sleep(300);
 				} catch (InterruptedException e) {
 					break;
 				}
 			}
 		} catch (DeltaCloudException e) {
			IStatus status = StatusFactory.getInstance(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage(), e);
			Activator.log(status);
			ErrorDialog.openError(
					UIUtils.getActiveShell(),
					CVMessages.getString(INSTANCEACTION_ERROR_TITLE),
					CVMessages.getFormattedString(INSTANCEACTION_ERROR_MESSAGE, action, instance.getName()),
					status);
 		} finally {
 			cloud.removeActionJob(id, this);
 			pm.done();
 		}
 		return Status.OK_STATUS;
 	}
 }
