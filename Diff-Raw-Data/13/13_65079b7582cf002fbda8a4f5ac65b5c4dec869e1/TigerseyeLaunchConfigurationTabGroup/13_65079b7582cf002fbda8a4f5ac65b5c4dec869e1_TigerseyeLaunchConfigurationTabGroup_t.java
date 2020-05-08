 package de.tud.stg.tigerseye.ui.launch;
 
 import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
 import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.EnvironmentTab;
 import org.eclipse.debug.ui.ILaunchConfigurationDialog;
 import org.eclipse.debug.ui.ILaunchConfigurationTab;
 import org.eclipse.jdt.debug.ui.launchConfigurations.JavaArgumentsTab;
 import org.eclipse.jdt.debug.ui.launchConfigurations.JavaClasspathTab;
 import org.eclipse.jdt.debug.ui.launchConfigurations.JavaJRETab;
 
 public class TigerseyeLaunchConfigurationTabGroup extends
 	AbstractLaunchConfigurationTabGroup {
 
     public TigerseyeLaunchConfigurationTabGroup() {
     }
 
     @Override
     public void createTabs(ILaunchConfigurationDialog dialog, String mode) {
 	ILaunchConfigurationTab[] tabs = new ILaunchConfigurationTab[] {
 		new TigerseyeMainLaunchConfigurationTab(),//
 		new JavaArgumentsTab(),//
 		new JavaJRETab(),//
 		new JavaClasspathTab(),//
		new EnvironmentTab(),//
 		new CommonTab(),//
 	};
 	setTabs(tabs);
     }
 
     
 
 
 }
