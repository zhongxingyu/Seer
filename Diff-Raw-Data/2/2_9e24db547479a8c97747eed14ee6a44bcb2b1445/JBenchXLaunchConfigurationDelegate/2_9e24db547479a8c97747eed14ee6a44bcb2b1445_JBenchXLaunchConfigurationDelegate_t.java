 package org.jbenchx.ui.eclipse.launch;
 
 import java.io.*;
 import java.util.*;
 
 import org.eclipse.core.resources.*;
 import org.eclipse.core.runtime.*;
 import org.eclipse.debug.core.*;
 import org.eclipse.jdt.launching.*;
 import org.jbenchx.util.*;
 
 public class JBenchXLaunchConfigurationDelegate extends AbstractJavaLaunchConfigurationDelegate {
 
   @Override
   public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitorOrNull) throws CoreException {
     IProgressMonitor monitor = (monitorOrNull != null) ? monitorOrNull : new NullProgressMonitor();
     if (monitor.isCanceled()) {
       return;
     }
 
     monitor.beginTask("Launching JBenchX", 5); // TODO how many steps are it?
     try {
 
       try {
         preLaunchCheck(configuration, mode, new SubProgressMonitor(monitor, 2));
       } catch (CoreException e) {
         if (e.getStatus().getSeverity() == IStatus.CANCEL) {
           monitor.setCanceled(true);
           return;
         }
         throw e;
       }
 
       if (monitor.isCanceled()) {
         return;
       }
 
       // do set port
 
       String benchmarks = getBenchmarks(configuration);
 
       String mainTypeName = verifyMainTypeName(configuration);
       IVMRunner runner = getVMRunner(configuration, mode);
 
       File workingDir = verifyWorkingDirectory(configuration);
       String workingDirName = null;
       if (workingDir != null) {
         workingDirName = workingDir.getAbsolutePath();
       }
 
       // Environment variables
       String[] envp = getEnvironment(configuration);
 
       String programArgumentString = getProgramArguments(configuration);
       String vmArgumentString = getVMArguments(configuration);
       ExecutionArguments execArgs = new ExecutionArguments(vmArgumentString, programArgumentString);
       List<String> vmArguments = new ArrayList<String>(Arrays.asList(execArgs.getVMArgumentsArray()));
       List<String> programArguments = new ArrayList<String>(Arrays.asList(execArgs.getVMArgumentsArray()));
 
       programArguments.add(createBenchmarkArgument(benchmarks));
 
       // VM-specific attributes
      Map<String, Object> vmAttributesMap = getVMSpecificAttributesMap(configuration);
 
       // Classpath
       String[] classpath = getClasspath(configuration);
 
       // Create VM config
       VMRunnerConfiguration runConfig = new VMRunnerConfiguration(mainTypeName, classpath);
       runConfig.setVMArguments(vmArguments.toArray(new String[vmArguments.size()]));
       runConfig.setProgramArguments(programArguments.toArray(new String[programArguments.size()]));
       runConfig.setEnvironment(envp);
       runConfig.setWorkingDirectory(workingDirName);
       runConfig.setVMSpecificAttributesMap(vmAttributesMap);
 
       // Bootpath
       runConfig.setBootClassPath(getBootpath(configuration));
 
       // check for cancellation
       if (monitor.isCanceled()) {
         return;
       }
 
       // done the verification phase
       monitor.worked(1);
 
 //      monitor.subTask(JUnitMessages.JUnitLaunchConfigurationDelegate_create_source_locator_description);
       // set the default source locator if required
       setDefaultSourceLocator(launch, configuration);
       monitor.worked(1);
 
       // Launch the configuration - 1 unit of work
       runner.run(runConfig, launch, monitor);
 
       // check for cancellation
       if (monitor.isCanceled()) {
         return;
       }
 
       // FIXME this does not work as we would need to wait until the benchmark process has completed
       String projectName = configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, (String)null);
       if (projectName != null) {
         IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
         if (project.exists()) {
           project.refreshLocal(IResource.DEPTH_INFINITE, null);
         }
       }
 
     } finally {
       monitor.done();
     }
 
   }
 
   private String createBenchmarkArgument(String benchmarks) {
     return "-benchmarks " + StringUtil.join(",", benchmarks);
   }
 
   private String getBenchmarks(ILaunchConfiguration configuration) throws CoreException {
     return configuration.getAttribute(JBenchXLaunchConfig.ATTR_JBENCHX_BENCHMARKS, "");
   }
 
 }
