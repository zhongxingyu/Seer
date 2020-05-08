 /*******************************************************************************
  * Copyright (c) 2003, 2004 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.common.internal.annotations.ui;
 
 import java.util.MissingResourceException;
 import java.util.ResourceBundle;
 
import org.eclipse.core.runtime.Platform;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jdt.internal.ui.text.PreferencesAdapter;
import org.eclipse.jdt.ui.JavaUI;
 import org.eclipse.jdt.ui.text.JavaTextTools;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.ui.editors.text.EditorsUI;
 import org.eclipse.ui.plugin.AbstractUIPlugin;
 import org.eclipse.ui.texteditor.ChainedPreferenceStore;
 
 /**
  * The main plugin class to be used in the desktop.
  */
 public class UiPlugin extends AbstractUIPlugin {
 	//The shared instance.
 	private static UiPlugin plugin;
 	//Resource bundle.
 	private ResourceBundle resourceBundle;
 	
 	private IPreferenceStore fCombinedPreferenceStore;
 	private JavaTextTools fJavaTextTools;
 
 	/**
 	 * The constructor.
 	 */
 	public UiPlugin() {
 		super();
 		plugin = this;
 		try {
 			resourceBundle = ResourceBundle.getBundle("org.eclipse.wst.common.internal.annotations.ui.UiPluginResources"); //$NON-NLS-1$
 		} catch (MissingResourceException x) {
 			resourceBundle = null;
 		}
 	}
 
 	/**
 	 * Returns the shared instance.
 	 */
 	public static UiPlugin getDefault() {
 		return plugin;
 	}
 
 	/**
 	 * Returns the string from the plugin's resource bundle, or 'key' if not found.
 	 */
 	public static String getResourceString(String key) {
 		ResourceBundle bundle = UiPlugin.getDefault().getResourceBundle();
 		try {
 			return (bundle != null) ? bundle.getString(key) : key;
 		} catch (MissingResourceException e) {
 			return key;
 		}
 	}
 
 	/**
 	 * Returns the plugin's resource bundle,
 	 */
 	public ResourceBundle getResourceBundle() {
 		return resourceBundle;
 	}
 	
 	public IPreferenceStore getCombinedPreferenceStore() {
 		if (fCombinedPreferenceStore == null) {
 			IPreferenceStore generalTextStore= EditorsUI.getPreferenceStore(); 
			fCombinedPreferenceStore= new ChainedPreferenceStore(new IPreferenceStore[] { getJavaPlugin().getPreferenceStore(), new PreferencesAdapter(JavaCore.getPlugin().getPluginPreferences()), generalTextStore });
 		}
 		return fCombinedPreferenceStore;
 	}
 	
 	public synchronized JavaTextTools getJavaTextTools() {
 		if (fJavaTextTools == null)
			fJavaTextTools= new JavaTextTools(getJavaPlugin().getPreferenceStore(), JavaCore.getPlugin().getPluginPreferences());
 		return fJavaTextTools;
 	}
	
	protected AbstractUIPlugin getJavaPlugin() {
		return (AbstractUIPlugin) Platform.getPlugin(JavaUI.ID_PLUGIN);
	}
 }
