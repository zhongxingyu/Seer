 package org.zend.php.zendserver.deployment.ui.targets;
 
 import java.io.IOException;
 
import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.zend.php.zendserver.deployment.ui.Activator;
 import org.zend.php.zendserver.deployment.ui.Messages;
 import org.zend.sdklib.SdkException;
 import org.zend.sdklib.internal.target.ZendTarget;
 import org.zend.sdklib.target.IZendTarget;
 import org.zend.webapi.core.WebApiException;
 
 /**
  * Tries to connect to target.
  */
 public class ValidateTargetJob extends Job {
 
 	private AbstractTargetDetailsComposite composite;
 	private String[] data;
 	private IZendTarget validatedTarget;
 
 	public ValidateTargetJob(
 			AbstractTargetDetailsComposite abstractTargetDetailsComposite) {
 		super(Messages.ValidateTargetJob_ValidatingTarget);
 		this.composite = abstractTargetDetailsComposite;
 
 		// get data in GUI thread
 		data = composite.getData();
 	}
 
 	@Override
 	protected IStatus run(IProgressMonitor monitor) {
 		ZendTarget target;
 		try {
 			target = (ZendTarget) composite.createTarget(data);
 		} catch (SdkException e) {
 			return new Status(IStatus.ERROR, Activator.PLUGIN_ID,
 					e.getMessage(), e);
 		} catch (IOException e) {
 			return new Status(IStatus.ERROR, Activator.PLUGIN_ID,
 					e.getMessage(), e);
		} catch (CoreException e) {
			return e.getStatus();
 		}
 
 		if (target == null) {
 			return new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Target was not created.");
 		}
 		
 		String message = target.validateTarget();
 		if (message != null) {
 			return new Status(IStatus.ERROR, Activator.PLUGIN_ID, message);
 		}
 
 		if (target.isTemporary()) {
 			validatedTarget = target;
 		} else {
 			try {
 				target.connect();
 			} catch (WebApiException ex) {
 				return new Status(IStatus.ERROR, Activator.PLUGIN_ID,
 						ex.getMessage(), ex);
 			}
 		}
 
 		validatedTarget = target;
 		return Status.OK_STATUS;
 	}
 
 	public IZendTarget getResultTarget() {
 		return validatedTarget;
 	}
 }
