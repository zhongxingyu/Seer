 /*-
  * Copyright © 2009 Diamond Light Source Ltd.
  *
  * This file is part of GDA.
  *
  * GDA is free software: you can redistribute it and/or modify it under the
  * terms of the GNU General Public License version 3 as published by the Free
  * Software Foundation.
  *
  * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License along
  * with GDA. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package gda.rcp;
 
 import gda.rcp.util.UIScanDataPointEventService;
 
 import java.lang.reflect.InvocationTargetException;
 
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.jface.resource.ImageRegistry;
 import org.eclipse.ui.plugin.AbstractUIPlugin;
 import org.osgi.framework.BundleContext;
 
 import uk.ac.gda.common.rcp.NamedServiceProvider;
 
 /**
  * The activator class controls the plug-in life cycle.
  * 
  * This is the main GDA Client activator and is called when the client
  * plugin is loaded. It connects the UIScanDataPointEventService to the server
  * which then listens to scan data points coming in. These can then be plotted later.
  * 
  * This step is required because Eclipse uses lazy loading so unless a view that is 
  * dealing with points has been started, they would be lost. Instead UIScanDataPointEventService
  * listens to everything. It can be configured in size using an extension point.
  */
 public class GDAClientActivator extends AbstractUIPlugin {
 	
  	
 	// The plug-in ID - corresponds to name in plugin.xml file
 	/**
 	 *
 	 */
 	public static final String PLUGIN_ID = "uk.ac.gda.client"; //this must match value in plugin.xml
 
 	private BundleContext context;
 	// The shared instance
 	private static GDAClientActivator plugin;
 	public static BundleContext getBundleContext(){
 		return plugin.context;
 	}
 	/**
 	 * The constructor
 	 */
 	public GDAClientActivator() {
 	}
 
 	@Override
 	public void start(final BundleContext context) throws Exception {
 		
 		super.start(context);
 		plugin = this;
 		plugin.context=context;
 		
 	}
 
 	@Override
 	public void stop(BundleContext context) throws Exception {
		try{
			super.stop(context);
			if(namedServiceProvider != null){
				namedServiceProvider.close();
				namedServiceProvider = null;
			}
			UIScanDataPointEventService.getInstance().dispose();
		} finally{
			plugin = null;
 		}
 	}
 
 	/**
 	 * Returns the shared instance
 	 *
 	 * @return the shared instance
 	 */
 	public static GDAClientActivator getDefault() {
 		return plugin;
 	}
 
 	/**
 	 * Returns an image descriptor for the image file at the given
 	 * plug-in relative path
 	 *
 	 * @param path the path
 	 * @return the image descriptor
 	 */
 	public static ImageDescriptor getImageDescriptor(String path) {
 		return imageDescriptorFromPlugin(PLUGIN_ID, path);
 	}
 
 	/**
 	 * Record throwable in Eclipse error log
 	 * @param e Throwable
 	 */
 	public static void log(Throwable e) {
 		if (e instanceof InvocationTargetException){
 			e = ((InvocationTargetException) e).getTargetException();
 		}
 		IStatus status = null;
 		if (e instanceof CoreException){
 			status = ((CoreException) e).getStatus();
 		}else {
 			status = new Status(IStatus.ERROR, PLUGIN_ID, IStatus.OK, e.getMessage(), e);
 		}
 		log(status);
 	}
 
 	/**
 	 * Record IStatus object in Eclipse error log
 	 * @param status
 	 */
 	private static void log(IStatus status) {
 		ResourcesPlugin.getPlugin().getLog().log(status);
 	}
 
 	@Override
 	protected void initializeImageRegistry(ImageRegistry reg) {
 		for (String imgPath : ImageConstants.IMAGES) {
 			reg.put(imgPath, imageDescriptorFromPlugin(PLUGIN_ID, imgPath));
 		}
 	}
 	
 	private static NamedServiceProvider namedServiceProvider;
 	
 	public static Object getNamedService(@SuppressWarnings("rawtypes") Class clzz, final String name) {
 		if(namedServiceProvider == null){
 			namedServiceProvider = new NamedServiceProvider(plugin.context);
 		}
 		return namedServiceProvider.getNamedService(clzz, "SERVICE_NAME", name);
 		
 	}	
 	
 }
