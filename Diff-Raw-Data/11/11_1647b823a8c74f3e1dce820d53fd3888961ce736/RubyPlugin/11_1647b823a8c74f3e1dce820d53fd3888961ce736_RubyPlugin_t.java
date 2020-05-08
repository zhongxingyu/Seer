 package org.eclipse.dltk.ruby.core;
 
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.core.runtime.Plugin;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.dltk.core.DLTKCore;
import org.eclipse.dltk.ruby.internal.parser.mixin.RubyMixinModel;
 import org.osgi.framework.BundleContext;
 
 /**
  * The activator class controls the plug-in life cycle
  */
 public class RubyPlugin extends Plugin {
 	// The plug-in ID
 	public static final String PLUGIN_ID = "org.eclipse.dltk.ruby.core";
 	
 	public static final boolean DUMP_EXCEPTIONS_TO_CONSOLE = Boolean.valueOf(
 			Platform.getDebugOption("org.eclipse.dltk.ruby.core/dumpErrorsToConsole"))
 			.booleanValue();
 
 	// The shared instance
 	private static RubyPlugin plugin;
 	
 	/**
 	 * The constructor
 	 */
 	public RubyPlugin() {
 		plugin = this;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
 	 */
 	public void start(BundleContext context) throws Exception {
 		super.start(context);		
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
 	 */
 	public void stop(BundleContext context) throws Exception {
 		plugin = null;
 		super.stop(context);
		
		RubyMixinModel.getRawInstance().stop();
 	}
 
 	/**
 	 * Returns the shared instance
 	 *
 	 * @return the shared instance
 	 */
 	public static RubyPlugin getDefault() {
 		return plugin;
 	}
 
 	public static void log(Exception ex) {
 		if (DLTKCore.DEBUG || DUMP_EXCEPTIONS_TO_CONSOLE)
 			ex.printStackTrace();
 		String message = ex.getMessage();
 		if (message == null)
 			message = "(no message)";
 		getDefault().getLog().log(new Status(Status.ERROR,
 				PLUGIN_ID, 0, message, ex));
 	}
 	
 	public static void log(String message) {
 		if (DLTKCore.DEBUG || DUMP_EXCEPTIONS_TO_CONSOLE)
 			System.out.println(message);
 		getDefault().getLog().log(new Status(Status.WARNING,
 				PLUGIN_ID, 0, message, null));
 	}
 
 }
