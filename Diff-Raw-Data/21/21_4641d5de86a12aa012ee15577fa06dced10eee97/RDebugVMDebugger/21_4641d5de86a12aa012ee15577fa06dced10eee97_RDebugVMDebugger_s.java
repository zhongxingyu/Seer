 package org.rubypeople.rdt.internal.launching;
 
import java.io.File;
import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.debug.core.ILaunch;
 import org.rubypeople.rdt.internal.debug.core.RubyDebuggerProxy;
 import org.rubypeople.rdt.internal.debug.core.model.RubyDebugTarget;
import org.rubypeople.rdt.internal.debug.core.model.RubyProcessingException;
 import org.rubypeople.rdt.launching.IRubyLaunchConfigurationConstants;
 import org.rubypeople.rdt.launching.IVMInstall;
 import org.rubypeople.rdt.launching.VMRunnerConfiguration;
 
 public class RDebugVMDebugger extends StandardVMDebugger {
 
 	public RDebugVMDebugger(IVMInstall vmInstance) {
 		super(vmInstance);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.rubypeople.rdt.launching.IVMRunner#run(org.rubypeople.rdt.launching.VMRunnerConfiguration,
 	 *      org.eclipse.debug.core.ILaunch,
 	 *      org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	public void run(VMRunnerConfiguration config, ILaunch launch, IProgressMonitor monitor) throws CoreException {
 
 		// Set it up to use rdebug executable
 		Map map = config.getVMSpecificAttributesMap();
 		if (map == null) map = new HashMap();
 		map.put(IRubyLaunchConfigurationConstants.ATTR_RUBY_COMMAND, "rdebug");
 		config.setVMSpecificAttributesMap(map);
 		
 		super.run(config, launch, monitor);
 	}
 
 	protected List<String> debugSpecificVMArgs(RubyDebugTarget debugTarget) {
 		List<String> arguments = new ArrayList<String>();
 		arguments.add("--server");
 		arguments.add("-w"); // wait for client to connect on command port
 		arguments.add("-n"); // do not halt when client connects
 		arguments.add("--port");
 		arguments.add(Integer.toString(debugTarget.getPort()));
 		arguments.add("--cport");
 		arguments.add(Integer.toString(debugTarget.getPort() + 1));
 		if (isDebuggerVerbose()) {
 			arguments.add("-d");
 		}
 		arguments.add("-f");
 		arguments.add("xml");
 		return arguments;
 	}
 	
 }
