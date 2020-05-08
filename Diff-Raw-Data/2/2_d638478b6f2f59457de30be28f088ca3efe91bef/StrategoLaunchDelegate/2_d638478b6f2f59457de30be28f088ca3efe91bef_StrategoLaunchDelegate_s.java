 package org.strjdbg.eclipse.core.str.launching;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.ServerSocket;
 import java.net.URL;
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.FileLocator;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.debug.core.DebugPlugin;
 import org.eclipse.debug.core.ILaunch;
 import org.eclipse.debug.core.ILaunchConfiguration;
 import org.eclipse.debug.core.ILaunchManager;
 import org.eclipse.debug.core.IStatusHandler;
 import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
 import org.eclipse.jdt.launching.AbstractJavaLaunchConfigurationDelegate;
 import org.eclipse.jdt.launching.IVMInstall;
 import org.eclipse.jdt.launching.IVMRunner;
 import org.eclipse.jdt.launching.JavaRuntime;
 import org.eclipse.jdt.launching.VMRunnerConfiguration;
 import org.osgi.framework.Bundle;
 import org.strategoxt.debug.core.util.DebugCompileException;
 import org.strategoxt.debug.core.util.DebugCompiler;
 import org.strategoxt.debug.core.util.DebugSessionSettings;
 import org.strategoxt.debug.core.util.DebugSessionSettingsFactory;
 import org.strjdbg.eclipse.core.Activator;
 import org.strjdbg.eclipse.core.str.model.StrategoDebugTarget;
 
 /**
  * Launches a Stratego program.
  * 
  * The program can be launched in debug or in run mode.
  * 
  * Debug Launch:
  * - add debug metadata to the stratego program
  * - compile the stratego program to java (strj)
  * - compile the java-stratego to class files
  * - run the class
  * - [handle breakpoints]
  * 
  * Run launch
  * - compile the stratego program to java (strj)
  * - compile the java-stratego to class files
  * - run the class
  * 
  * The delegate extends the AbstractJavaLaunchConfigurationDelegate as this simplifies how to use a JVM
  * @author rlindeman
  *
  */
 @SuppressWarnings("unchecked")
 public class StrategoLaunchDelegate extends AbstractJavaLaunchConfigurationDelegate implements ILaunchConfigurationDelegate {
 
 	
 	public void launch(ILaunchConfiguration configuration, String mode,
 			ILaunch launch, IProgressMonitor monitor) throws CoreException {
         if (monitor == null){
             monitor = new NullProgressMonitor();
         }
         monitor.beginTask("Launching Stratego program", IProgressMonitor.UNKNOWN);
         
         // project
         String project = configuration.getAttribute(IStrategoConstants.ATTR_STRATEGO_PROJECT, (String) null);
         if (project == null)
         {
         	abort("Eclipse project unspecified.", null);
         	return;
         }
         
 		// program name
 		String program = configuration.getAttribute(IStrategoConstants.ATTR_STRATEGO_PROGRAM, (String)null);
 		if (program == null) {
 			abort("Stratego program unspecified.", null);
 			return;
 		}
 		
 		IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(program));
 		if (!file.exists()) {
 			abort(MessageFormat.format("Stratego program {0} does not exist.", new Object[] {file.getFullPath().toString()}), null);
 			return;
 		}
 		
 		// program arguments
 		List programArguments = configuration.getAttribute(IStrategoConstants.ATTR_STRATEGO_PROGRAM_ARGUMENTS, (List)null);
 		if (programArguments == null) {
 			//abort("Stratego program unspecified.", null);
 			programArguments = new ArrayList<String>();
 		}
 				
 		// the started wm will wait for a debugger to connect to this port
 		String port = ""+findFreePort();
 		
 		//String strategoFilePath = file.getLocation().toOSString(); // full path to the stratego program
 		String strategoFilePath = program;
 		String strategoSourceBasedir = ResourcesPlugin.getWorkspace().getRoot().getProject(project).getLocation().toOSString();
 		//String strategoSourceBasedir = ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString();
 		IFile f = ResourcesPlugin.getWorkspace().getRoot().getProject(project).getFile(strategoFilePath); // path to the stratego file
 		strategoFilePath =  f.getProjectRelativePath().toOSString();
 		IPath projectPath = new Path(project);
 		strategoFilePath = f.getProjectRelativePath().makeRelativeTo(projectPath).toOSString(); // make the stratego file path relative to the project path
 		System.out.println("PROJECT...:" + project);
 		System.out.println("BASEDIR...: " + strategoSourceBasedir);
 		System.out.println("COMPILING...: " + strategoFilePath);
 
 		// now find a suitable temp directory to generate the files in...
 		String w = file.getProject().getFolder("working").getFullPath().toOSString();
 		IFolder wDir = ResourcesPlugin.getWorkspace().getRoot().getFolder(new Path(w));
 		String workingDirFolder = wDir.getLocation().toOSString();
 		System.out.println("WORKING DIR: " + workingDirFolder);
 		
 		DebugCompiler debugCompiler = new DebugCompiler("/tmp");
 		String projectName = DebugCompiler.createProjectName(new File(program));
 		DebugSessionSettings debugSessionSettings = DebugSessionSettingsFactory.create("/tmp", projectName);
 		Bundle b = Activator.getDefault().getBundle();
 		//URL e = b.getEntry("/lib");
 		
 		IPath path = new Path("/lib");
 		Map override = null;
 		URL url = FileLocator.find(b, path, override);
 		URL fileURL = null;
 		try {
 			fileURL = FileLocator.toFileURL(url);
 			System.out.println("FILE URL:" + fileURL);
 		} catch (IOException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 		System.out.println("URL: " + fileURL);
 		String directory = fileURL.getPath();
 		debugSessionSettings.setJarLibraryDirectory(directory);
 		debugSessionSettings.setStrategoSourceBasedir(strategoSourceBasedir);
 		debugSessionSettings.setStrategoFilePath(strategoFilePath);
 		// compile the stratego program
 		//String binBase = debugSessionSettings.getClassDirectory(); // default
 		String binBase = null;
 		try {
 			binBase = prepareProgram(configuration, monitor, mode, debugCompiler, debugSessionSettings);
 		} catch (DebugCompileException e) {
 			// TODO Auto-generated catch block
 			//e.printStackTrace();
 			// TODO: could not compile program: Show error message
 			String message = MessageFormat.format("Could not launch Stratego program {0}. Failed to compile the program.", new Object[] { program });
 			this.abort(message, e);
 			return;
 		}
 		
 		monitor.subTask("Starting Stratego VM");
 		// Initialize the VMRunner
 		IVMInstall defaultInstall = JavaRuntime.getDefaultVMInstall();
 		System.out.println("default: " + defaultInstall.getName());
 		//IVMRunner vmRunner = defaultInstall.getVMRunner(mode);
 		IVMRunner vmRunner = defaultInstall.getVMRunner(ILaunchManager.RUN_MODE); // always use RUN, so we can control the debug parameters of the VM
 
 		
 		// set up vm arguments
 		String classToLaunch = projectName + "." + projectName;
 		
 		String strategoxtjar = debugSessionSettings.getStrategoxtJar();
 		String debugRuntime = debugSessionSettings.getStrategoDebugRuntimeJar();
 		String debugRuntimeJava = debugSessionSettings.getStrategoDebugRuntimeJavaJar();
 		
 		String[] classPath = new String[] { binBase, strategoxtjar, debugRuntime, debugRuntimeJava};
 		VMRunnerConfiguration vmRunnerConfiguration = new VMRunnerConfiguration(classToLaunch, classPath);
 		
 		// setup program arguments
 		System.out.println("Args: " + programArguments);
 		String[] programArgsArray = new String[programArguments.size()];
 		programArgsArray = (String[]) programArguments.toArray(programArgsArray);
 		vmRunnerConfiguration.setProgramArguments(programArgsArray);
 
 
 		
 		// if we arein DEBUG_MODE also set the debugging parameters for the VM as we previously created an IVMRunner in RUN_MODE
 		if (mode.equals(ILaunchManager.DEBUG_MODE)) {
 			// socket attach
 			//String[] realVMargs = new String[] { "-Xdebug", "-Xrunjdwp:transport=dt_socket,address="+port+",server=y,suspend=y" };
 			// socket listen
 			String[] realVMargs = new String[] { "-Xdebug", "-Xrunjdwp:transport=dt_socket,address="+port+",suspend=y" };
 		//String[] realVMargs = new String[] { "-Xrunjdwp:transport=dt_socket,address=9000,server=y,suspend=y" };
 		//String[] realVMargs = new String[] { "-Xdebug" };
 			vmRunnerConfiguration.setVMArguments(realVMargs);
 		}
 		
 
 		
 		if (mode.equals(ILaunchManager.DEBUG_MODE)) {
 			monitor.subTask("Attaching to the Stratego VM");
 			StrategoDebugTarget target = new StrategoDebugTarget(debugSessionSettings, launch, port);
 			//(launch,p,requestPort,eventPort );
 			launch.addDebugTarget(target);
 			monitor.worked(1);
 		}
 		
 		// start the VM with the stratego program
 		// using attach, run before the StrategoDebugTarget is initialized
 		// using listen, run after the StrategoDebugTarget is initialized
 		System.out.println("RUN");
 		vmRunner.run(vmRunnerConfiguration, launch, monitor);
 		monitor.worked(1);
 		
 		monitor.done();
 	}
 	
 	private String prepareProgram(ILaunchConfiguration configuration, IProgressMonitor monitor, String mode, DebugCompiler debugCompiler, DebugSessionSettings debugSessionSettings) throws DebugCompileException, CoreException {
 		// program recompile
 		boolean rebuildBinary = false;
 		rebuildBinary = configuration.getAttribute(IStrategoConstants.ATTR_STRATEGO_PROGRAM_RECOMPILE, true);
 		
 		String binBase = debugSessionSettings.getClassDirectory(); // default
 
 		if (rebuildBinary)
 		{
 			binBase = compile(monitor, mode, debugCompiler, debugSessionSettings);
 		}
 		else
 		{
 			// TODO: check if all the necessary files exist in the working dir...
 			// check if binBase contains javafiles
 			IPath binBasePath = new Path(binBase);
 			File binBaseFile = binBasePath.toFile();
 			if (!binBaseFile.exists())
 			{
 				// did not compile to class files
 				// try to compile it
 				System.out.println("Class files not found, compile...");
 				binBase = compile(monitor, mode, debugCompiler, debugSessionSettings);
 			}
 			else
 			{
 				// check if dir is empty
 				String[] files = binBaseFile.list();
 				if (files == null || files.length == 0)
 				{
 					System.out.println("Class files not found, compile...");
 					binBase = compile(monitor, mode, debugCompiler, debugSessionSettings);
 				}
 			}
 			
 			// TODO: check if table file exists
 			IPath strBasePath = new Path(debugSessionSettings.getStrategoDirectory());
 			if (!strBasePath.toFile().exists())
 			{
 				System.out.println("Stratego program does not have debug info, compile...");
 				binBase = compile(monitor, mode, debugCompiler, debugSessionSettings);
 			} else {
 				String[] files = strBasePath.toFile().list();
 				if (files == null || files.length == 0)
 				{
 					System.out.println("Stratego program does not have debug info, compile...");
 					binBase = compile(monitor, mode, debugCompiler, debugSessionSettings);
 				}
 			}
 			
 		}
 		return binBase;
 	}
 
 	private String compile(IProgressMonitor monitor, String mode, DebugCompiler debugCompiler, DebugSessionSettings debugSessionSettings) throws DebugCompileException
 	{
 		monitor.subTask("Compiling stratego program");
 		String binBase = null;
 		if (mode.equals(ILaunchManager.DEBUG_MODE)) 
 		{
 			binBase = debugCompile(debugCompiler, debugSessionSettings);
 
 		}
 		else if (mode.equals(ILaunchManager.RUN_MODE))
 		{
 			binBase = runCompile(debugCompiler, debugSessionSettings);
 		}
 		monitor.worked(3);
 		return binBase;
 	}
 	
 	private String debugCompile(DebugCompiler debugCompiler, DebugSessionSettings debugSessionSettings) throws DebugCompileException
 	{
 		// compile for a debug
 		String binBase = null;
 		try {
 			binBase = debugCompiler.debugCompile(debugSessionSettings);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} 
 		/*
 		catch (DebugCompileException e) {
 			// TODO Auto-generated catch block
 			//e.printStackTrace();
 			abort("Could not compile stratego program.", e);
 		}*/
 		return binBase;
 	}
 	
 	private String runCompile(DebugCompiler debugCompiler, DebugSessionSettings debugSessionSettings) throws DebugCompileException
 	{
 		// compile for a run
 		String binBase = null;
 		try {
 			binBase = debugCompiler.runCompile(debugSessionSettings);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} 
 		/*
 		catch (DebugCompileException e) {
 			// TODO Auto-generated catch block
 			//e.printStackTrace();
 			abort("Could not compile stratego program.", e);
 		}*/
 		return binBase;
 	}
 	
 	public static void showDebugInfo(IVMInstall defaultInstall, VMRunnerConfiguration vmRunnerConfiguration)
 	{
 		// show debug info
 		String[] installArgs = defaultInstall.getVMArguments();
 		String installArgsString = Arrays.toString(installArgs);
 		System.out.println("installArgsString: " + installArgsString);
 		
 		String[] env = vmRunnerConfiguration.getEnvironment();
 		String envString = Arrays.toString(env);
 		System.out.println("env: " + envString);
 		
 		String[] vmArgs = vmRunnerConfiguration.getVMArguments();
 		String vmArgsString = Arrays.toString(vmArgs);
 		System.out.println("vmArgs: " + vmArgsString);
 		
 		Map map = vmRunnerConfiguration.getVMSpecificAttributesMap();
 		if (map != null)
 		{
 			for(Object key : map.keySet())
 			{
 				Object value = map.get(key);
 				System.out.println("key: " + key + "    val:"+value);
 			}
 		}
 	}
 
 
 	
 	/*
 	private void startRunVM()
 	{
 		
 	}
 	
 	private void startDebugVM(com.sun.jdi.VirtualMachine vm, VMMonitor vmMonitor)
 	{
 		// create a new VM
 		// TODO: let the user select the VM in the launchconfiguration
 		
 		DebugSessionManager manager = new DebugSessionManager(vmMonitor);
 		VMLauncherHelper vmHelper = null;
 		//manager.initVM(mainArgs, classpath);
 		
 		manager.setVirtualMachine(vm);
 		manager.setupEventListeners();
 		manager.redirectOutput();
 
 	}
 	*/
 	
 	/**
 	 * Throws an exception with a new status containing the given
 	 * message and optional exception.
 	 * 
 	 * @param message error message
 	 * @param e underlying exception
 	 * @throws CoreException
 	 */
 	private void abort(String message, Throwable e) throws CoreException {
 		// TODO: the plug-in code should be the example plug-in, not Stratego debug model id
 		IStatus status = new Status(IStatus.ERROR, IStrategoConstants.ID_STRATEGO_DEBUG_MODEL, 0, message, e);
 		//throw new CoreException(status);
 		
 		IStatusHandler handler = DebugPlugin.getDefault().getStatusHandler(status);
 		
 		if (handler != null) {
 			Object result = handler.handleStatus(status, null);
 			System.out.println(result);
 		}
 	}
 	
 	/**
 	 * Returns a free port number on localhost, or -1 if unable to find a free port.
 	 * 
 	 * @return a free port number on localhost, or -1 if unable to find a free port
 	 */
 	public static int findFreePort() {
 		ServerSocket socket= null;
 		try {
 			socket= new ServerSocket(0);
 			return socket.getLocalPort();
 		} catch (IOException e) { 
 		} finally {
 			if (socket != null) {
 				try {
 					socket.close();
 				} catch (IOException e) {
 				}
 			}
 		}
 		return -1;		
 	}
 	
 }
