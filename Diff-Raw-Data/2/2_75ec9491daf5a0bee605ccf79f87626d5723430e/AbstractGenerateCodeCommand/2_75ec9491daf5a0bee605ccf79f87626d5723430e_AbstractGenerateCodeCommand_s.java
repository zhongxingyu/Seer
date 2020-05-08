 package org.jboss.tools.ws.creation.core.commands;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.LineNumberReader;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.debug.core.DebugPlugin;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.launching.IVMInstall;
 import org.eclipse.jdt.launching.JavaRuntime;
 import org.eclipse.osgi.util.NLS;
 import org.eclipse.wst.common.frameworks.datamodel.AbstractDataModelOperation;
 import org.jboss.tools.ws.core.utils.StatusUtils;
 import org.jboss.tools.ws.creation.core.JBossWSCreationCorePlugin;
 import org.jboss.tools.ws.creation.core.data.ServiceModel;
 import org.jboss.tools.ws.creation.core.messages.JBossWSCreationCoreMessages;
 import org.jboss.tools.ws.creation.core.utils.JBossWSCreationUtils;
 
 abstract class AbstractGenerateCodeCommand extends AbstractDataModelOperation {
 
 	protected ServiceModel model;
 	private String cmdFileName_linux;
 	private String cmdFileName_win;
 	private static String JAVA_HOME = "JAVA_HOME"; //$NON-NLS-1$
 
 	public AbstractGenerateCodeCommand(ServiceModel model) {
 		this.model = model;
 		cmdFileName_linux = getCommandLineFileName_linux();
 		cmdFileName_win = getCommandLineFileName_win();
 	}
 
 	@Override
 	public IStatus execute(IProgressMonitor monitor, IAdaptable info)
 			throws ExecutionException {
 		if (monitor == null) {
 			monitor = new NullProgressMonitor();
 		}
 		try {
 			monitor.beginTask("", 100); //$NON-NLS-1$
 			monitor.subTask(JBossWSCreationCoreMessages.Progress_Message_Generating);
 			IStatus status = Status.OK_STATUS;
 
 			IProject project = model.getJavaProject().getProject();
 			JBossWSCreationCorePlugin.getDefault().setGenerateTime(System.currentTimeMillis());
 			try {
 				String runtimeLocation = JBossWSCreationUtils.getJBossWSRuntimeLocation(project);
 				String commandLocation = runtimeLocation + Path.SEPARATOR+ "bin"; //$NON-NLS-1$
 				IPath path = new Path(commandLocation);
 				List<String> command = new ArrayList<String>();
 				String[] env = getEnvironmentVariables(model.getJavaProject());
 				if (System.getProperty("os.name").toLowerCase().indexOf("win") >= 0) { //$NON-NLS-1$ //$NON-NLS-2$
 					command.add("cmd.exe"); //$NON-NLS-1$
 					command.add("/c"); //$NON-NLS-1$
 					command.add(cmdFileName_win);
 					path = path.append(cmdFileName_win);
 				} else {
 					command.add("sh"); //$NON-NLS-1$
 					command.add(cmdFileName_linux);
 					path = path.append(cmdFileName_linux);
 				}
 				if (!path.toFile().getAbsoluteFile().exists()) {
 					return StatusUtils.errorStatus(NLS.bind(JBossWSCreationCoreMessages.Error_Message_Command_File_Not_Found, new String[] { path.toOSString() }));
 				}
 				addCommandlineArgs(command);
 				addCommonArgs(command, model.getJavaProject());
 
 				Process proc = DebugPlugin.exec(command.toArray(new String[command.size()]), new File(commandLocation), env);
 				StringBuffer errorResult = new StringBuffer();
 				StringBuffer inputResult = new StringBuffer();
 				convertInputStreamToString(errorResult, proc.getErrorStream());
 				convertInputStreamToString(inputResult, proc.getInputStream());
 				int exitValue = proc.waitFor();
 				String resultInput = inputResult.toString();
 				if (exitValue != 0) {
 					JBossWSCreationCorePlugin.getDefault().logError(errorResult.toString());
 					JBossWSCreationCorePlugin.getDefault().logError(inputResult.toString());
 					// the resultInput containing "javac -d", means the java
 					// code generating is complete and there is only a javac error.
 					if (resultInput != null && resultInput.indexOf("javac -d") >= 0) {//$NON-NLS-1$
 						return StatusUtils.warningStatus(errorResult.toString());
 					}
 					return StatusUtils.errorStatus(errorResult.toString());
 				} else {
 					if (resultInput != null) {
 						// there are errors, but not complication error.
						if (resultInput.indexOf("error") >= 0 && !(resultInput.indexOf("compilation failed") >= 0)) { //$NON-NLS-1$ //$NON-NLS-2$
 							JBossWSCreationCorePlugin.getDefault().logError(resultInput);
 							IStatus errorStatus = StatusUtils.errorStatus(resultInput);
 							status = StatusUtils.errorStatus(JBossWSCreationCoreMessages.Error_Message_Failed_To_Generate_Code,new CoreException(errorStatus));
 							return status;
 						}
 						if (resultInput.indexOf("[ERROR]") >= 0) { //$NON-NLS-1$
 							JBossWSCreationCorePlugin.getDefault().logWarning(resultInput);
 							IStatus errorStatus = StatusUtils.warningStatus(resultInput);
 							status = StatusUtils.warningStatus(JBossWSCreationCoreMessages.Error_Message_Failed_To_Generate_Code, new CoreException(errorStatus));
 						}
 					}
 				}
 			} catch (InterruptedException e) {
 				JBossWSCreationCorePlugin.getDefault().logError(e);
 				return StatusUtils.errorStatus(e);
 			} catch (CoreException e) {
 				JBossWSCreationCorePlugin.getDefault().logError(e);
 				// unable to get runtime location
 				return e.getStatus();
 			} catch (Exception e) {
 				JBossWSCreationCorePlugin.getDefault().logError(e);
 				return StatusUtils.errorStatus(e);
 			}
 			return status;
 		} finally {
 			refreshProject(model.getJavaProject(), monitor);
 			monitor.done();
 		}
 
 	}
 
 	// SET JAVA_HOME environment variable to the location of java runtime of the
 	// project if the user
 	// doesn't set the env variable
 	protected String[] getEnvironmentVariables(IJavaProject javaProject) {
 		String[] env = null;
 		String javaHome = System.getenv(JAVA_HOME);
 		if (javaHome == null || !(new File(javaHome).exists())) {
 			if (javaProject == null || !javaProject.exists())
 				return null;
 			try {
 				if (!javaProject.isOpen()) {
 					javaProject.open(null);
 				}
 				IVMInstall vm = JavaRuntime.getVMInstall(javaProject);
 				String javaLocation = vm.getInstallLocation().toString();
 				env = new String[] { JAVA_HOME + "=" + javaLocation }; //$NON-NLS-1$
 			} catch (CoreException e1) {
 				e1.printStackTrace();
 			}
 		}
 		return env;
 	}
 
 	protected void addCommonArgs(List<String> command, IJavaProject javaProject) throws Exception {
 		String projectRoot = model.getJavaProject().getProject().getLocation().toOSString();
 		command.add("-k"); //$NON-NLS-1$
 		command.add("-s"); //$NON-NLS-1$
 		command.add(JBossWSCreationUtils.getCustomSrcLocation(model.getJavaSourceFolder()));
 		command.add("-o"); //$NON-NLS-1$
 		StringBuffer opDir = new StringBuffer();
 		opDir.append(projectRoot).append(Path.SEPARATOR).append(javaProject.getOutputLocation().removeFirstSegments(1).toOSString());
 		command.add(opDir.toString());
 		if (model.getAddOptions() != null && !"".equals(model.getAddOptions())) { //$NON-NLS-1$
 			String str = model.getAddOptions().trim();
 			String[] strArray = str.split(" +"); //$NON-NLS-1$
 			for (int i=0 ; i<strArray.length; i++) {
 				command.add(strArray[i]);
 			}
 		}
 		if (model.getWsdlURI() != null) {
 			command.add(model.getWsdlURI());
 		}
 	}
 
 	protected void convertInputStreamToString(final StringBuffer result,
 			final InputStream input) {
 		Thread thread = new Thread() {
 			public void run() {
 				try {
 					InputStreamReader ir = new InputStreamReader(input);
 					LineNumberReader reader = new LineNumberReader(ir);
 					String str;
 					str = reader.readLine();
 					while (str != null) {
 						result.append(str).append("\t\r"); //$NON-NLS-1$
 						str = reader.readLine();
 					}
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 
 			}
 		};
 		thread.start();
 	}
 
 	protected void refreshProject(IJavaProject project, IProgressMonitor monitor) {
 		try {
 			project.getProject().refreshLocal(2,monitor);
 		} catch (CoreException e) {
 			e.printStackTrace();
 			JBossWSCreationCorePlugin.getDefault().logError(e);
 		}
 	}
 
 	abstract protected void addCommandlineArgs(List<String> command) throws Exception;
 
 	abstract protected String getCommandLineFileName_linux();
 
 	abstract protected String getCommandLineFileName_win();
 
 }
