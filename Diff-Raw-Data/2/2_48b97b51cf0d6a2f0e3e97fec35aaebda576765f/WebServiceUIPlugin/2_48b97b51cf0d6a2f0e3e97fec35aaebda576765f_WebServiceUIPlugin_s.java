 /*
  * Created on Nov 15, 2004
  *
  * TODO To change the template for this generated file go to
  * Window - Preferences - Java - Code Style - Code Templates
  */
 package org.eclipse.jst.j2ee.internal.webservice.plugin;
 
 import org.eclipse.core.resources.IWorkspace;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.ui.plugin.AbstractUIPlugin;
 
 /**
  * @author cbridgha
  *
  * TODO To change the template for this generated type comment go to
  * Window - Preferences - Java - Code Style - Code Templates
  */
 public class WebServiceUIPlugin extends AbstractUIPlugin {
 	
	public static final String PLUGIN_ID = "org.eclipse.jst.j2ee.webservices.ui"; //$NON-NLS-1$
 	
 	//	The shared instance.
 	private static WebServiceUIPlugin plugin;
 
 	/**
 	 * The constructor.
 	 */
 	public WebServiceUIPlugin() {
 		super();
 		plugin = this;
 	}
 	
 
 	/**
 	 * Returns the shared instance.
 	 */
 	public static WebServiceUIPlugin getDefault() {
 		return plugin;
 	}
 
 	/**
 	 * Returns the workspace instance.
 	 */
 	public static IWorkspace getWorkspace() {
 		return ResourcesPlugin.getWorkspace();
 	}
 }
