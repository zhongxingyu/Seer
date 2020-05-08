 package org.eclipse.dltk.launching;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.debug.core.ILaunch;
 import org.eclipse.debug.core.ILaunchManager;
 import org.eclipse.debug.core.Launch;
 import org.eclipse.dltk.core.IDLTKProject;
 
 public class DLTKLaunchUtil {
	public static IInterpreterInstall getDefaultInterpreterInstall(String nature)
			throws CoreException {
 		return ScriptRuntime.getDefaultInterpreterInstall(nature);
 	}
 
 	public static IInterpreterInstall getProjectInterpreterInstall(
 			IDLTKProject project, String nature) throws CoreException {
 		IInterpreterInstall interp = ScriptRuntime
 				.getInterpreterInstall(project);
 		if (interp == null) {
 			interp = ScriptRuntime.getDefaultInterpreterInstall(nature);
 		}
 		return interp;
 	}

	public static IInterpreterInstall getDefaultInterpreter(String nature) {
		return ScriptRuntime.getDefaultInterpreterInstall(nature);
	}

 	public static ILaunch launchScript(IInterpreterInstall install,
 			String scriptPath, String[] args, String mode) throws CoreException {
 		ILaunch launch = new Launch(null, ILaunchManager.RUN_MODE, null);
 
 		InterpreterRunnerConfiguration config = new InterpreterRunnerConfiguration(
 				scriptPath);
 		config.setProgramArguments(args);
 
 		IInterpreterRunner runner = install.getInterpreterRunner(mode);
 		runner.run(config, launch, null);
 
 		return launch;
 	}
 }
