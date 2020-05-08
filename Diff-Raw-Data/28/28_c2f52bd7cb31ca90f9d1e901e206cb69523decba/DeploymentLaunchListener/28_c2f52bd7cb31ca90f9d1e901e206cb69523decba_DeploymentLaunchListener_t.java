 package org.zend.php.zendserver.deployment.debug.ui.listeners;
 
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.debug.core.ILaunch;
 import org.eclipse.debug.core.ILaunchConfiguration;
 import org.eclipse.php.debug.core.debugger.launching.ILaunchDelegateListener;
import org.zend.php.zendserver.deployment.core.DeploymentNature;
import org.zend.php.zendserver.deployment.debug.core.config.LaunchUtils;
import org.zend.php.zendserver.deployment.debug.ui.Activator;
 import org.zend.php.zendserver.deployment.debug.ui.config.DeploymentHandler;
 
 public class DeploymentLaunchListener implements ILaunchDelegateListener {
 
	public int preLaunch(ILaunchConfiguration configuration, String mode,
			ILaunch launch, IProgressMonitor monitor) {
		try {
			final IProject project = LaunchUtils.getProjectFromFilename(configuration);
			if (project == null || !project.hasNature(DeploymentNature.ID)) {
				 return 0;
			}
			
			DeploymentHandler handler = new DeploymentHandler(configuration);
			return handler.executeDeployment(mode);
		} catch (CoreException e) {
			Activator.log(e);
		}
		return 0;
 	}

 }
