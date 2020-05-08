 package org.eclipse.dltk.ruby.internal.launching;
 
 import java.io.IOException;
 
 import org.eclipse.core.runtime.ILog;
 import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
 import org.eclipse.dltk.core.environment.IDeployment;
import org.eclipse.dltk.core.environment.IFileHandle;
 import org.eclipse.dltk.internal.launching.AbstractInterpreterInstallType;
import org.eclipse.dltk.internal.launching.InterpreterMessages;
 import org.eclipse.dltk.launching.IInterpreterInstall;
 import org.eclipse.dltk.ruby.core.RubyNature;
 import org.eclipse.dltk.ruby.launching.RubyLaunchingPlugin;
 
 public class JRubyInstallType extends AbstractInterpreterInstallType {
 	private static final String INSTALL_TYPE_NAME = "JRuby"; //$NON-NLS-1$
 
 	private static final String[] INTERPRETER_NAMES = { "jruby" }; //$NON-NLS-1$
 
 	public String getNatureId() {
 		return RubyNature.NATURE_ID;
 	}
 
 	public String getName() {
 		return INSTALL_TYPE_NAME;
 	}
 
 	protected String getPluginId() {
 		return RubyLaunchingPlugin.PLUGIN_ID;
 	}
 
 	protected String[] getPossibleInterpreterNames() {
 		return INTERPRETER_NAMES;
 	}
 
 	protected IInterpreterInstall doCreateInterpreterInstall(String id) {
 		return new RubyGenericInstall(this, id);
 	}
 
 	protected IPath createPathFile(IDeployment deployment) throws IOException {
 		return deployment.add(RubyLaunchingPlugin.getDefault().getBundle(),
 				"scripts/path.rb"); //$NON-NLS-1$
 	}
 
 	protected String getBuildPathDelimeter() {
 		return ";:"; //$NON-NLS-1$
 	}
 
 	protected ILog getLog() {
 		return RubyLaunchingPlugin.getDefault().getLog();
 	}

	public IStatus validateInstallLocation(IFileHandle installLocation) {
		if (!Platform.getOS().equals(Platform.OS_WIN32)) {
			if (installLocation.getName().indexOf(".bat") != -1) { //$NON-NLS-1$
				return createStatus(
						IStatus.ERROR,
						InterpreterMessages.errNonExistentOrInvalidInstallLocation,
						null);
			}
		}

		return super.validateInstallLocation(installLocation);
	}
 }
