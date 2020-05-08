 package org.eclipse.dltk.ruby.internal.launching.debug;
 
import org.eclipse.dltk.launching.debug.IDebuggingEngine;
import org.eclipse.dltk.launching.debug.IDebuggingEngineSelector;
 import org.eclipse.dltk.ruby.launching.RubyLaunchingPlugin;
 
public class RubyDebuggingEngineSelector implements IDebuggingEngineSelector {

	public IDebuggingEngine select(IDebuggingEngine[] engines) {
		String id = RubyLaunchingPlugin.getDefault().getPluginPreferences()
				.getString(RubyDebuggingConstants.DEBUGGING_ENGINE_ID);

		if (id != null) {
			for (int i = 0; i < engines.length; ++i) {
				IDebuggingEngine engine = engines[i];
				if (engine.getId().equals(id)) {
					return engine;
				}
			}
		}

		return null;
 	}
 }
