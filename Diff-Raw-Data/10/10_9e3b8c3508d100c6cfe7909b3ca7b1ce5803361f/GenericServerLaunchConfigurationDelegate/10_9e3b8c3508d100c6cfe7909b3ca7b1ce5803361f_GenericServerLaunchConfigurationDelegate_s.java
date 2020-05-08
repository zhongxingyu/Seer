 /*******************************************************************************
  * Copyright (c) 2004 Eteration Bilisim A.S.
  * All rights reserved.  This program and the accompanying materials
  * are made available under the terms of the Common Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/cpl-v10.html
  * 
  * Contributors:
  *     Gorkem Ercan - initial API and implementation
  *     Naci M. Dai
  * 
  * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
  * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
  * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED.  IN NO EVENT SHALL ETERATION A.S. OR
  * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
  * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
  * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
  * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
  * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
  * SUCH DAMAGE.
  * ====================================================================
  *
  * This software consists of voluntary contributions made by many
  * individuals on behalf of the Eteration Bilisim A.S.  For more
  * information on eteration, please see
  * <http://www.eteration.com/>.
  ***************************************************************************/
 package org.eclipse.jst.server.generic.core.internal;
 
 import java.io.File;
 import java.util.Map;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.debug.core.ILaunch;
 import org.eclipse.debug.core.ILaunchConfiguration;
 import org.eclipse.jdt.launching.AbstractJavaLaunchConfigurationDelegate;
 import org.eclipse.jdt.launching.ExecutionArguments;
 import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
 import org.eclipse.jdt.launching.IVMInstall;
 import org.eclipse.jdt.launching.IVMRunner;
 import org.eclipse.jdt.launching.VMRunnerConfiguration;
 import org.eclipse.wst.server.core.IServer;
 import org.eclipse.wst.server.core.ServerUtil;
 import org.eclipse.wst.server.core.model.ServerBehaviourDelegate;
 /**
  * ServerLaunchConfiguration for the generic server.
  *
  * @author Gorkem Ercan
  */
 public class GenericServerLaunchConfigurationDelegate extends AbstractJavaLaunchConfigurationDelegate {
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.debug.core.model.ILaunchConfigurationDelegate#launch(org.eclipse.debug.core.ILaunchConfiguration, java.lang.String, org.eclipse.debug.core.ILaunch, org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	public void launch(ILaunchConfiguration configuration, String mode,
 			ILaunch launch, IProgressMonitor monitor) throws CoreException {
 
 		IServer server = ServerUtil.getServer(configuration);
 		if (server == null) {
 			abort("Server does not exist", null,
 					IJavaLaunchConfigurationConstants.ERR_INTERNAL_ERROR);
 		}
 		GenericServerBehaviour genericServer = (GenericServerBehaviour) server.loadAdapter(ServerBehaviourDelegate.class, null);
 
 		try {
 			genericServer.setupLaunch(launch, mode, monitor);
 			String mainTypeName = genericServer.getStartClassName();
 			IVMInstall vm = verifyVMInstall(configuration);
 			IVMRunner runner = vm.getVMRunner(mode);

 			File workingDir = verifyWorkingDirectory(configuration);
 			String workingDirName = null;
 			if (workingDir != null)
 				workingDirName = workingDir.getAbsolutePath();
 
 			// Program & VM args
 			String pgmArgs = getProgramArguments(configuration);
 			String vmArgs = getVMArguments(configuration);
 
 			ExecutionArguments execArgs = new ExecutionArguments(vmArgs,
 					pgmArgs);
 
 			// VM-specific attributes
 			Map vmAttributesMap = getVMSpecificAttributesMap(configuration);
 
 			// Classpath
 			String[] classpath = getClasspath(configuration);
 
 			// Create VM config
 			VMRunnerConfiguration runConfig = new VMRunnerConfiguration(
 					mainTypeName, classpath);
 			runConfig.setProgramArguments(execArgs.getProgramArgumentsArray());
 			runConfig.setVMArguments(execArgs.getVMArgumentsArray());
 			runConfig.setWorkingDirectory(workingDirName);
 			runConfig.setVMSpecificAttributesMap(vmAttributesMap);
 
 			// Bootpath
 			String[] bootpath = getBootpath(configuration);
 			if (bootpath != null && bootpath.length > 0)
 				runConfig.setBootClassPath(bootpath);
 
 			setDefaultSourceLocator(launch, configuration);
 			// Launch the configuration
 			runner.run(runConfig, launch, monitor);
 			genericServer.setProcess(launch.getProcesses()[0]);
 		} catch (CoreException e) {
 			Trace.trace(Trace.SEVERE,"error lauching generic server",e);
 			genericServer.terminate();
 			throw e;
 		}
 	}
 
 	/**
 	 * Throws a core exception with the given message and optional
 	 * exception. The exception's status code will indicate an error.
 	 * 
 	 * @param message error message
 	 * @param exception cause of the error, or <code>null</code>
 	 * @exception CoreException with the given message and underlying
 	 *  exception
 	 */
 	protected void abort(String message, Throwable exception, int code) throws CoreException {
 		throw new CoreException(new Status(IStatus.ERROR, CorePlugin.getDefault().getBundle().getSymbolicName(), code, message, exception));
 	}
 	
 }
