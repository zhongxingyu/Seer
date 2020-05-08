 package org.eclipse.dltk.ruby.internal.launching;
 
 import java.io.File;
 import java.io.IOException;
 
 import org.eclipse.core.runtime.ILog;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.dltk.internal.launching.AbstractInterpreterInstallType;
 import org.eclipse.dltk.internal.launching.InterpreterMessages;
 import org.eclipse.dltk.launching.IInterpreterInstall;
 import org.eclipse.dltk.ruby.core.RubyNature;
 import org.eclipse.dltk.ruby.launching.RubyLaunchingPlugin;
 import org.osgi.framework.Bundle;
 
 public class JRubyInstallType extends AbstractInterpreterInstallType {
 	private static final String INSTALL_TYPE_NAME = "JRuby";
 
 	private static final String[] INTERPRETER_NAMES = { "jruby" };
 
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
 
 	protected File createPathFile() throws IOException {
 		Bundle bundle = RubyLaunchingPlugin.getDefault().getBundle();
 		return storeToMetadata(bundle, "path.rb", "scripts/path.rb");
 	}
 
 	protected String getBuildPathDelimeter() {
 		return ";:";
 	}
 
 	protected ILog getLog() {
 		return RubyLaunchingPlugin.getDefault().getLog();
 	}
 
 	public IStatus validateInstallLocation(File installLocation) {
 		if (!Platform.getOS().equals(Platform.OS_WIN32)) {
			if (installLocation.getName().indexOf(".bat") != 0) {
 				return createStatus(
 						IStatus.ERROR,
 						InterpreterMessages.errNonExistentOrInvalidInstallLocation,
 						null);
 			}
 		}
 
 		return super.validateInstallLocation(installLocation);
 	}
 }
