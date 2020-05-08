 package org.zend.php.zendserver.deployment.debug.ui.listeners;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.debug.core.ILaunch;
 import org.eclipse.debug.core.ILaunchConfiguration;
 import org.eclipse.php.debug.core.debugger.launching.ILaunchDelegateListener;
 import org.zend.php.zendserver.deployment.debug.ui.config.DeploymentHandler;
 
 public class DeploymentLaunchListener implements ILaunchDelegateListener {
 
	public int preLaunch(ILaunchConfiguration configuration, String mode, ILaunch launch,
			IProgressMonitor monitor) {
		DeploymentHandler handler = new DeploymentHandler(configuration);
		return handler.executeDeployment(mode);
 	}
	
 }
