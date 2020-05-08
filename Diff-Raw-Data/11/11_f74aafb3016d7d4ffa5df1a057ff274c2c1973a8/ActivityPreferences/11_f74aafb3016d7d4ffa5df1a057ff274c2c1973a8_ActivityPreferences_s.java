 package com.gratex.perconik.activity;
 
 import java.net.URL;
 import javax.xml.namespace.QName;
 import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
 import org.eclipse.jface.preference.IPreferenceStore;
 import sk.stuba.fiit.perconik.utilities.net.UniformResources;
 import com.gratex.perconik.activity.plugin.Activator;
 
 public final class ActivityPreferences
 {
 	static final String key = Activator.PLUGIN_ID + ".preferences";
 	
 	static final String watcherUrl = key + ".watcher.url";
 	
 	static final String watcherNamespace = key + ".watcher.namespace";
 	
 	static final String watcherLocalPart = key + ".watcher.local";
 	
 	private ActivityPreferences()
 	{
 		throw new AssertionError();
 	}
 	
 	public static final class Initializer extends AbstractPreferenceInitializer
 	{
 		public Initializer()
 		{
 		}
 
 		@Override
 		public final void initializeDefaultPreferences()
 		{
 			IPreferenceStore store = store();
 			
 			store.setDefault(watcherUrl, ActivityDefaults.watcherUrl.toString());
 			
 			store.setDefault(watcherNamespace, ActivityDefaults.watcherName.getNamespaceURI());
 			store.setDefault(watcherLocalPart, ActivityDefaults.watcherName.getLocalPart());
 		}
 	}
 	
 	static final IPreferenceStore store()
 	{
 		return Activator.getDefault().getPreferenceStore();
 	}
 	
 	static final URL getWatcherServiceUrl()
 	{
		return UniformResources.newUrl(store().getString(watcherUrl));
 	}
 	
 	static final QName getWatcherServiceName()
 	{
 		IPreferenceStore store = store();
 		
 		return new QName(store.getString(watcherNamespace), store.getString(watcherLocalPart));
 	}
 }
