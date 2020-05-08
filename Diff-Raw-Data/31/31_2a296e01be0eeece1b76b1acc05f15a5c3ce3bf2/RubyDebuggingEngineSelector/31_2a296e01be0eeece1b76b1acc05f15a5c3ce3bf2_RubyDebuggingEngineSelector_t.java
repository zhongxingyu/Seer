 package org.eclipse.dltk.ruby.internal.launching.debug;
 
import org.eclipse.dltk.launching.debug.IdBasedDebuggingEngineSelector;
 import org.eclipse.dltk.ruby.launching.RubyLaunchingPlugin;
 
public class RubyDebuggingEngineSelector extends IdBasedDebuggingEngineSelector {
	protected String getDebuggingEngineId() {
		return RubyLaunchingPlugin.getDefault().getPluginPreferences().getString(
				RubyDebuggingConstants.DEBUGGING_ENGINE_ID_KEY);
 	}
 }
