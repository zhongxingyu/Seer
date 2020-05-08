 package org.uat.microchip.debug.launcher;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.debug.core.DebugPlugin;
 import org.eclipse.debug.core.ILaunch;
 import org.eclipse.debug.core.ILaunchConfiguration;
 import org.eclipse.debug.core.model.LaunchConfigurationDelegate;
 import org.eclipse.debug.core.model.RuntimeProcess;
import org.uat.microchip.debug.model.DeviceDebugger;
 import org.uat.microchip.toolchain.Activator;
 
import com.microchip.mplab.mdbcore.debugger.Debugger.CONNECTION_TYPE;

 /**
  * Launches the program
  * @author Jacob
  *
  */
 public class MicrochipLaunchDelegate extends LaunchConfigurationDelegate{
 
 	@Override
 	public void launch(ILaunchConfiguration configuration, String mode,
 			ILaunch launch, IProgressMonitor monitor) throws CoreException {
 
 		StringBuilder savedProperties = new StringBuilder();
 		
 		for (org.uat.microchip.debug.model.ToolProperty property : Activator.getToolProperties()) {
 			
 			if(property.getCurrentValue() != null)
 				savedProperties.append("\n " + property.toString());
 		}
 		
		DeviceDebugger debugger = new DeviceDebugger();
		debugger.Connect(CONNECTION_TYPE.DEBUGGER);
 		
 		Process process = DebugPlugin.exec(new String[]{"cmd", "/C", "\"echo Imm Working!\""}, null);
 		
 		new RuntimeProcess(launch, process, "Hello", null);		
 	}
 
 }
