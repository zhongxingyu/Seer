 package net.bioclipse.xws4j;
 
 import net.bioclipse.core.util.LogUtils;
import net.bioclipse.xws4j.business.IXWSManager;
 
 import org.apache.log4j.Logger;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.ui.plugin.AbstractUIPlugin;
 import org.osgi.framework.BundleContext;
 import org.osgi.util.tracker.ServiceTracker;
 
 /**
  * 
  * This file is part of the Bioclipse xws4j Plug-in.
  * 
  * Copyright (C) 2008 Johannes Wagener
  * 
  * This program is free software; you can redistribute it and/or modify it under
  * the terms of the GNU General Public License as published by the Free Software
  * Foundation; either version 3 of the License, or (at your option) any later
  * version.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License along with
  * this program; if not, see <http://www.gnu.org/licenses>.
  * 
  * @author Johannes Wagener
  */
 public class Activator extends AbstractUIPlugin {
 
 	// The plug-in ID
 	public static final String PLUGIN_ID = "net.bioclipse.xws4j";
 
     private static final Logger logger = Logger.getLogger(Activator.class);
 
 	// The shared instance
 	private static Activator plugin;
 	private static DefaultClientCurator clientcurator;
 
     private ServiceTracker finderTracker;
 
 	/**
 	 * The constructor
 	 */
 	public Activator() {
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
 	 */
 	public void start(BundleContext context) throws Exception {
 		super.start(context);
 		plugin = this;
 		clientcurator = new DefaultClientCurator();
 		
         finderTracker = new ServiceTracker( context, 
                IXWSManager.class.getName(), 
                 null );
         finderTracker.open();
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
 	 */
 	public void stop(BundleContext context) throws Exception {
 		clientcurator.stop();
 		clientcurator = null;
 		plugin = null;
 		super.stop(context);
 	}
 	
 	public static ImageDescriptor getImageDescriptor(String path) {
 		return imageDescriptorFromPlugin(PLUGIN_ID, path);
 	}
 	
 	public static DefaultClientCurator getDefaultClientCurator() {
 		return clientcurator;
 	}
 	
 	/**
 	 * Returns the shared instance
 	 *
 	 * @return the shared instance
 	 */
 	public static Activator getDefault() {
 		return plugin;
 	}
 
	public IXWSManager getXWSManager() {
		IXWSManager manager = null;
         try {
            manager = (IXWSManager) finderTracker.waitForService(1000*10);
         } catch (InterruptedException e) {
            logger.warn("Exception occurred while attempting to get the XWSManager" + e);
             LogUtils.debugTrace(logger, e);
         }
         if(manager == null) {
            throw new IllegalStateException("Could not get the XWS manager");
         }
         return manager;
 	}
 }
