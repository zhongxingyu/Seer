 package org.rubypeople.rdt.internal.launching;
 
 import java.io.File;
 import java.io.IOException;
 
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.debug.core.DebugPlugin;
 import org.eclipse.debug.core.ILaunchManager;
 import org.eclipse.debug.core.Launch;
 import org.eclipse.debug.core.model.IProcess;
 import org.rubypeople.rdt.launching.AbstractVMInstall;
 import org.rubypeople.rdt.launching.IVMInstallChangedListener;
 import org.rubypeople.rdt.launching.IVMInstallType;
 import org.rubypeople.rdt.launching.IVMRunner;
 import org.rubypeople.rdt.launching.PropertyChangeEvent;
 import org.rubypeople.rdt.launching.RubyRuntime;
 
 public class StandardVM extends AbstractVMInstall {
 
 	/**
 	 * Convenience handle to the system-specific file separator character
 	 */															
 	private static final char fgSeparator = File.separatorChar;
 
 	public StandardVM(IVMInstallType type, String id) {
 		super(type, id);
 	}
 	
 	@Override
 	public IVMRunner getVMRunner(String mode) {
 		if (ILaunchManager.RUN_MODE.equals(mode)) {
 			return new StandardVMRunner(this);
 		} else if (ILaunchManager.DEBUG_MODE.equals(mode)) {
 			if (useRDebug()) {
 				return new RDebugVMDebugger(this);
 			}
 			return new StandardVMDebugger(this);
 		}
 		return null;
 	}
 
 	protected boolean useRDebug() {
 		return LaunchingPlugin.getDefault().getPluginPreferences().getBoolean(PreferenceConstants.USE_RUBY_DEBUG);
 	}
 
 	public String getRubyVersion() {
 		 StandardVMType installType = (StandardVMType) getVMInstallType();
 	        File installLocation = getInstallLocation();
 	        if (installLocation != null) {
 	            File executable = StandardVMType.findRubyExecutable(installLocation);
 	            if (executable != null) {
 	                String vmVersion = installType.getVMVersion(installLocation, executable);
 	                // strip off extra info
 	                StringBuffer version = new StringBuffer();
 	                for (int i = 0; i < vmVersion.length(); i++) {
 	                    char ch = vmVersion.charAt(i);
 	                    if (Character.isDigit(ch) || ch == '.') {
 	                        version.append(ch);
 	                    } else {
 	                        break;
 	                    }
 	                }
 	                if (version.length() > 0) {
 	                    return version.toString();
 	                }
 	            }
 	        }
 	        return null;
 	}
 	
 	@Override
 	public IPath[] getLibraryLocations() {
 		IPath[] paths =  super.getLibraryLocations();
 		if (paths != null) return paths;
 		return getDefaultLibraryLocations();
 	}
 
 	@Override
 	public void setLibraryLocations(IPath[] locations) {
 		if (locations == fSystemLibraryDescriptions) {
 			return;
 		}
 		IPath[] newLocations = locations;
 		if (newLocations == null) {
 			newLocations = getDefaultLibraryLocations(); 
 		}
 		IPath[] prevLocations = fSystemLibraryDescriptions;
 		if (prevLocations == null) {
 			prevLocations = getDefaultLibraryLocations(); 
 		}
 		
 		if (newLocations.length == prevLocations.length) {
 			int i = 0;
 			boolean equal = true;
 			while (i < newLocations.length && equal) {
 				equal = newLocations[i].equals(prevLocations[i]);
 				i++;
 			}
 			if (equal) {
 				// no change
 				return;
 			}
 		}
 
 		PropertyChangeEvent event = new PropertyChangeEvent(this, IVMInstallChangedListener.PROPERTY_LIBRARY_LOCATIONS, prevLocations, newLocations);
 		fSystemLibraryDescriptions = locations;
 		if (fNotify) {
 			RubyRuntime.fireVMChanged(event);		
 		}
 	}
 
 	private IPath[] getDefaultLibraryLocations() {
 		 IPath[] dflts = getVMInstallType().getDefaultLibraryLocations(getInstallLocation());
 		 IPath coreStubsPath = generateCoreStubs(StandardVMType.findRubyExecutable(getInstallLocation()));
 		 if (coreStubsPath == null) {
 			 return dflts;
 		 }
 		 IPath[] paths = new IPath[dflts.length + 1];
 		 for (int i = 0; i < dflts.length; i++) {
 			 paths[i] = dflts[i];
 		 }
 		 paths[dflts.length] = coreStubsPath;		 
 		 return paths;
 	}
 	
 	/**
 	 * Launch a ruby script to generate core class stubs for use in RDT internally (since Ruby core 
 	 * stuff is not in any scripts, they're built into the VM in C code).
 	 * @param rubyExecutable
 	 * @return an IPath pointing to the directory containing the core library stubs
 	 */
 	private IPath generateCoreStubs(File rubyExecutable) {
 		if (rubyExecutable == null) return null;
 		//locate the script to generate our core stubs
 		File coreStubber = LaunchingPlugin.getFileInPlugin(new Path("ruby" + fgSeparator + "core_stubber.rb")); //$NON-NLS-1$ //$NON-NLS-2$
 		if (coreStubber.exists()) {	
			IPath stubFolder = new Path(coreStubber.getParentFile().getAbsolutePath() + fgSeparator + getId() + fgSeparator + "lib"); //$NON-NLS-1$
 			if (stubFolder.toFile().exists()) {
 				return stubFolder; // we've already created the stubs for this VM
 			}
 			stubFolder.toFile().mkdirs(); // Make the directory structure to throw the files into
 			String rubyExecutablePath = rubyExecutable.getAbsolutePath();
 			String[] cmdLine = new String[] {rubyExecutablePath, coreStubber.getAbsolutePath(), stubFolder.toOSString()};
 			Process p = null;
 			try {
 				p = Runtime.getRuntime().exec(cmdLine);
 				IProcess process = DebugPlugin.newProcess(new Launch(null, ILaunchManager.RUN_MODE, null), p, "Core Classes Stub Generation"); //$NON-NLS-1$
 				for (int i= 0; i < 200; i++) {
 					// Wait no more than 10 seconds (200 * 50 mils)
 					if (process.isTerminated()) {
 						break;
 					}
 					try {
 						Thread.sleep(50);
 					} catch (InterruptedException e) {
 					}
 				}
 				return stubFolder;
 			} catch (IOException ioe) {
 				LaunchingPlugin.log(ioe);
 			} finally {
 				if (p != null) {
 					p.destroy();
 				}
 			}
 		}
 		return null;
 	}
 }
