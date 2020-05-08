 /*******************************************************************************
  * Copyright (c) 2009, 2010 SpringSource, a divison of VMware, Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     SpringSource, a division of VMware, Inc. - initial API and implementation
  *******************************************************************************/
 package org.eclipse.virgo.ide.runtime.core;
 
 import java.net.URI;
 import java.net.URL;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import org.eclipse.core.runtime.FileLocator;
 import org.eclipse.ui.plugin.AbstractUIPlugin;
 import org.eclipse.virgo.ide.runtime.core.provisioning.ArtefactRepositoryManager;
 import org.osgi.framework.BundleContext;
 
 
 /**
  * Bundle Activator for the server.core plugin.
  * @author Christian Dupuis
  * @since 1.0.0
  */
 public class ServerCorePlugin extends AbstractUIPlugin {
 	
 	public static final String CONNECTOR_BUNDLE_NAME = 
		"org.eclipse.virgo.ide.management.remote_1.0.0.RELEASE.jar";
 
 	/** The bundle symbolic name */
 	public static final String PLUGIN_ID = "org.eclipse.virgo.ide.runtime.core";
 
 	public static final String PREF_LOAD_CLASSES_KEY = PLUGIN_ID + ".load.classes.from.index";
 	
 	private URI connectorBundleUri;
 	
 	/** The shared bundle instance */
 	private static ServerCorePlugin plugin;
 	
 	/** Internal artefact repository manager */
 	private ArtefactRepositoryManager artefactRepositoryManager;
 	
 	public static ExecutorService EXECUTOR = Executors.newCachedThreadPool();
 
 	@Override
 	public void start(BundleContext context) throws Exception {
 		super.start(context);
 		plugin = this; 
 		ServerUtils.clearCacheDirectory();
 		
 		URL url = FileLocator.toFileURL(context.getBundle().getEntry(CONNECTOR_BUNDLE_NAME));
 		if (url != null) {
 			connectorBundleUri = new URI("file", url.toString().substring(5), null);
 		}
 		
 		plugin.getPreferenceStore().setDefault(PREF_LOAD_CLASSES_KEY, false);
 		
 		artefactRepositoryManager = new ArtefactRepositoryManager();
 	}
 
 	@Override
 	public void stop(BundleContext context) throws Exception {
 		ServerUtils.clearCacheDirectory();
 		artefactRepositoryManager.stop();
 		artefactRepositoryManager = null;
 		plugin = null;
 		super.stop(context);
 	}
 
 	public static ServerCorePlugin getDefault() {
 		return plugin;
 	}
 
 	public static String getPreference(String id) {
 		return getDefault().getPluginPreferences().getString(id);
 	}
 
 	public static void setPreference(String id, String value) {
 		getDefault().getPluginPreferences().setValue(id, value);
 		getDefault().savePluginPreferences();
 	}
 
 	public static ArtefactRepositoryManager getArtefactRepositoryManager() {
 		return getDefault().artefactRepositoryManager;
 	}
 	
 	public URI getConnectorBundleUri() {
 		return connectorBundleUri;
 	}
 }
