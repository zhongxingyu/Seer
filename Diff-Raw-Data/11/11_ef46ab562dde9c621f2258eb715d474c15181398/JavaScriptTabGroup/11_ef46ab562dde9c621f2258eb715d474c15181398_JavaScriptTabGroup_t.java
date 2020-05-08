 package org.eclipse.dltk.javascript.internal.debug.ui.launchConfigurations;
 
 import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
 import org.eclipse.debug.ui.EnvironmentTab;
 import org.eclipse.debug.ui.ILaunchConfigurationDialog;
 import org.eclipse.debug.ui.ILaunchConfigurationTab;
 import org.eclipse.dltk.javascript.internal.debug.ui.interpreters.JavaScriptInterpreterTab;
 
 
 public class JavaScriptTabGroup extends AbstractLaunchConfigurationTabGroup {
 	public void createTabs(ILaunchConfigurationDialog dialog, String mode) {
 		ILaunchConfigurationTab[] tabs = new ILaunchConfigurationTab[] {
				new JavaScriptMainLaunchConfigurationTab(mode),
 				new JavaScriptArgumentsTab(),
 				new JavaScriptInterpreterTab(),
 				new EnvironmentTab(),
 				//new SourceContainerLookupTab(),
 				//new CommonTab()
 				new JavaScriptCommonTab()
 		};
 		setTabs(tabs);
 	}
 }
