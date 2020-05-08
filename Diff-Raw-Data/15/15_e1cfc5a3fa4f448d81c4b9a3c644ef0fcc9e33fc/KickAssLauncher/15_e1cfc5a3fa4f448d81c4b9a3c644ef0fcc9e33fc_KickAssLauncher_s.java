 /*
  Kick Assembler plugin - An Eclipse plugin for convenient Kick Assembling
  Copyright (c) 2012 - P-a Backstrom <pa.backstrom@gmail.com>
  
  Based on ASMPlugin - http://sourceforge.net/projects/asmplugin/
  Copyright (c) 2006 - Andy Reek, D. Mitte
  
  This program is free software; you can redistribute it and/or
  modify it under the terms of the GNU General Public License
  as published by the Free Software Foundation; either version 2
  of the License, or (at your option) any later version.
 
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */ 
 package org.lyllo.kickassplugin;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.jar.Attributes;
 import java.util.jar.JarFile;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.debug.core.ILaunch;
 import org.eclipse.debug.core.ILaunchManager;
 import org.eclipse.debug.core.IStreamListener;
 import org.eclipse.debug.core.Launch;
 import org.eclipse.debug.core.model.IProcess;
 import org.eclipse.debug.core.model.IStreamMonitor;
 import org.eclipse.jdt.launching.IVMInstall;
 import org.eclipse.jdt.launching.IVMRunner;
 import org.eclipse.jdt.launching.JavaRuntime;
 import org.eclipse.jdt.launching.VMRunnerConfiguration;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.swt.widgets.Display;
 import org.lyllo.kickassplugin.prefs.ProjectPrefenceHelper;
 
 public class KickAssLauncher implements IStreamListener {
 
 	public void launch(String[] args, String filedir, IProject project) throws CoreException {
 		IVMInstall vmInstall = JavaRuntime.getDefaultVMInstall();
 		if (vmInstall != null) {
 			IVMRunner vmRunner = vmInstall.getVMRunner(ILaunchManager.RUN_MODE);
 			if (vmRunner != null) {
 				JarFile jar = null;
 				IProcess iProcess = null;
 				try {
 					String[] classPathTemp = null;
 					IPreferenceStore store = Activator.getDefault().getPreferenceStore();
 					classPathTemp = new String[]{store.getString(Constants.PREFERENCES_COMPILER_NAME)};
 					jar = new JarFile(classPathTemp[0]);
 					String mainClass = jar.getManifest().getMainAttributes().getValue(Attributes.Name.MAIN_CLASS);
 					jar.close();
 					
 					List<String> classPathList = new ArrayList<String>();
 					classPathList.add(classPathTemp[0]);
 					classPathList.addAll(ProjectPrefenceHelper.getAbsoluteLibDirs(project));
 					classPathList.addAll(Activator.getGlobalLibdirs());
 					
 					VMRunnerConfiguration vmConfig = 
 							new VMRunnerConfiguration(mainClass, classPathList.toArray(new String[]{}));
 					ILaunch launch = new Launch(null, ILaunchManager.RUN_MODE, null);
 					vmConfig.setProgramArguments(args);
 					vmConfig.setWorkingDirectory(filedir);
 					vmRunner.run(vmConfig, launch, null);
 					iProcess = launch.getProcesses()[0];
 					IStreamMonitor outputStreamMonitor = iProcess.getStreamsProxy().getOutputStreamMonitor();
 					outputStreamMonitor.addListener(this);
 
 					IStreamMonitor errorStreamMonitor = iProcess.getStreamsProxy().getErrorStreamMonitor();
 					errorStreamMonitor.addListener(this);
 
 					while(!iProcess.isTerminated()){
 						try {
 							Thread.sleep(500);
 						} catch (InterruptedException e) {
 						}
 					}
 					
 				} catch (IOException e) {
 					throw new CoreException(new Status(Status.ERROR, Constants.PLUGIN_ID,
 							"Could not compile. Please make sure that you have set the path to Kickass.jar in the Preferences",e));
 				} finally {
 					if (jar != null){
 						try {
 							jar.close();
 						} catch (IOException e) {
 						}
 					}
 					if (iProcess != null && !iProcess.isTerminated()){
 						iProcess.terminate();
 					}
 				}
 			}
 		} else {
 			throw new CoreException(new Status(Status.ERROR, Constants.PLUGIN_ID,
 					"Could not compile. Please make sure that you have set a default JRE"));
 
 		}
 
 	}
 
 	public void streamAppended(final String arg0, IStreamMonitor arg1) {
 
 		Display.getDefault().asyncExec(new Runnable() {
 			public void run() {
				Activator.getConsole().println(arg0);
 			}
 		});
 	}
 }
