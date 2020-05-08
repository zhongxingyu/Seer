 package org.eclipse.dltk.javascript.internal.core;
 
 import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
 import org.eclipse.core.runtime.preferences.DefaultScope;
 import org.eclipse.core.runtime.preferences.IEclipsePreferences;
 import org.eclipse.dltk.compiler.task.TaskTagUtils;
 import org.eclipse.dltk.javascript.core.JavaScriptPlugin;
 
 public class JavaScriptCorePreferenceInitializer extends
 		AbstractPreferenceInitializer {
 
 	@Override
 	public void initializeDefaultPreferences() {
 		final IEclipsePreferences preferences = new DefaultScope()
 				.getNode(JavaScriptPlugin.PLUGIN_ID);
 		TaskTagUtils.initializeDefaultValues(preferences);
 	}
 }
