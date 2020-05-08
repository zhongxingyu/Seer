 package org.jboss.tools.jst.web.kb;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.ISaveContext;
 import org.eclipse.core.resources.ISaveParticipant;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.jboss.tools.common.log.BaseUIPlugin;
 import org.jboss.tools.jst.web.kb.internal.KbProject;
 import org.osgi.framework.BundleContext;
 
 /**
  * The activator class controls the plug-in life cycle
  */
 public class WebKbPlugin extends BaseUIPlugin {
 
 	// The plug-in ID
 	public static final String PLUGIN_ID = "org.jboss.tools.jst.web.kb"; //$NON-NLS-1$
 
 	// The shared instance
 	private static WebKbPlugin plugin;
 
 	/**
 	 * The constructor
 	 */
 	public WebKbPlugin() {
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
 	 */
 	public void start(BundleContext context) throws Exception {
 		super.start(context);
 		plugin = this;
 
 		ResourcesPlugin.getWorkspace().addSaveParticipant(PLUGIN_ID, new ISaveParticipant() {
 			
 			public void saving(ISaveContext context)
 					throws CoreException {
 				switch (context.getKind()) {
 					case ISaveContext.SNAPSHOT:
 					case ISaveContext.FULL_SAVE:
 						IProject[] ps = ResourcesPlugin.getWorkspace().getRoot().getProjects();
 						for (IProject p: ps) {
 							KbProject sp = (KbProject)KbProjectFactory.getKbProject(p, false, true);
 							if(sp != null && sp.getModificationsSinceLastStore() > 0) {
 //								sp.printModifications();
 								try {
 									sp.store();
 								} catch (IOException e) {
 									WebKbPlugin.getDefault().logError(e);
 								}
 							}
 						}
 						break;
 					case ISaveContext.PROJECT_SAVE:
						KbProject sp = (KbProject)KbProjectFactory.getKbProject(context.getProject(), false);
 						try {
 							if(sp != null && sp.getModificationsSinceLastStore() > 0) {
 //								sp.printModifications();
 								//Not any project is a seam project
 								sp.store();
 							}
 						} catch (IOException e) {
 							WebKbPlugin.getDefault().logError(e);
 						}
 						break;
 				}
 				
 				cleanObsoleteFiles();
 			}
 			
 			public void rollback(ISaveContext context) {
 
 			}
 			
 			public void prepareToSave(ISaveContext context) throws CoreException {
 			}
 			
 			public void doneSaving(ISaveContext context) {
 			}
 		});
 	}
 
 	private void cleanObsoleteFiles() {
 		IProject[] ps = ResourcesPlugin.getWorkspace().getRoot().getProjects();
 		Set<String> projectNames = new HashSet<String>();
 		for (IProject p: ps) projectNames.add(p.getName());
 		WebKbPlugin plugin = WebKbPlugin.getDefault();
 		if(plugin!=null) {
 			IPath path = plugin.getStateLocation();
 			File file = new File(path.toFile(), "projects"); //$NON-NLS-1$
 			if(!file.isDirectory()) return;
 			File[] fs = file.listFiles();
 			if(fs != null) for (File f: fs) {
 				String name = f.getName();
 				if(name.endsWith(".xml")) { //$NON-NLS-1$
 					name = name.substring(0, name.length() - 4);
 					if(!projectNames.contains(name)) {
 						f.delete();
 					}
 				}
 			}
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
 	 */
 	public void stop(BundleContext context) throws Exception {
 		super.stop(context);
 		
 		// Fix for JBIDE-7621: The following line is moved to the very end of the method 
 		// due to prevent NullPointerException to be thrown in cleanObsoleteFiles() method --->>>
 		plugin = null;
 		// <<<---
 	}
 
 	/**
 	 * Returns the shared instance
 	 *
 	 * @return the shared instance
 	 */
 	public static WebKbPlugin getDefault() {
 		return plugin;
 	}
 }
