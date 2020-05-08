 package org.eclipse.dltk.javascript.internal.debug;
 
 
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Plugin;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.debug.core.DebugException;
 import org.osgi.framework.BundleContext;
 
 /**
  * The plugin class for the Tcl Debug Model plug-in.
  */
 
 public class JavaScriptDebugPlugin extends Plugin {
 	
 	public static final String PLUGIN_ID =  "org.eclipse.dltk.javascript.debug";
 	
	public static final String MODEL_ID =  "org.eclipse.dltk.debug.javascriptModel";
	
 	private static JavaScriptDebugPlugin fgPlugin;
 			
 	public static final int INTERNAL_ERROR = 120;
 	
 	public static void log(IStatus status) {
 		getDefault().getLog().log(status);
 	}
 	
 		
 	private boolean fTrace = false;
 		
 	public boolean isTraceMode() {
 		return fTrace;
 	}
 
 	public static void logTraceMessage(String message) {
 		if (getDefault().isTraceMode()) {
 			IStatus s = new Status(IStatus.WARNING, JavaScriptDebugPlugin.PLUGIN_ID, INTERNAL_ERROR, message, null);
 			getDefault().getLog().log(s);
 		}
 	}	
 
 	public static JavaScriptDebugPlugin getDefault() {		
 		return fgPlugin;
 	}
 
 	public JavaScriptDebugPlugin() {
 		super();	
 		fgPlugin = this;
 	}
 	
 	public void start(BundleContext context) throws Exception {
 		super.start(context);		
 		
 		//getDefault().getLog().log(new Status(Status.INFO, PLUGIN_ID, Status.OK, "TCLDebugPlugin starting...", null));
 	}
 	
 	public void stop(BundleContext context) throws Exception {
 		try {			
 			savePluginPreferences();
 		} finally {
 			fgPlugin = null;
 			super.stop(context);
 		}
 	}
 		
 	public static void log(Throwable t) {
 		Throwable top= t;
 		if (t instanceof DebugException) {
 			DebugException de = (DebugException)t;
 			IStatus status = de.getStatus();
 			if (status.getException() != null) {
 				top = status.getException();
 			}
 		} 
 		// this message is intentionally not internationalized, as an exception may
 		// be due to the resource bundle itself
 		log(new Status(IStatus.ERROR, PLUGIN_ID, INTERNAL_ERROR, "Internal error logged from Tcl Debug: ", top));		
 	}	
 }
