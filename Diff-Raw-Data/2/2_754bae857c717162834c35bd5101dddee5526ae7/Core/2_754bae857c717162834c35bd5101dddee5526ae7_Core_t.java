 /*
  * Copyright 2011 Jonathan Anderson
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package me.footlights.core;
 
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.lang.reflect.Method;
 import java.net.URI;
 import java.nio.ByteBuffer;
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.logging.Logger;
 
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 
 import me.footlights.core.crypto.Keychain;
 import me.footlights.core.data.File;
 import me.footlights.core.data.store.DiskStore;
 import me.footlights.core.data.store.Store;
 import me.footlights.core.plugin.PluginLoadException;
 import me.footlights.core.plugin.PluginWrapper;
 import me.footlights.plugin.KernelInterface;
 import me.footlights.plugin.Plugin;
 
 
 public class Core implements Footlights
 {
 	public Core(ClassLoader pluginLoader)
 	{
 		this.pluginLoader = pluginLoader;
 
 		try { prefs = Preferences.loadFromDefaultLocation(); }
 		catch (IOException e)
 		{
 			log.severe("Unable to load Footlights preferences");
 			throw new RuntimeException(e);
 		}
 
 		keychain = new Keychain();
 		try
 		{
 			keychain.importKeystoreFile(
 				new FileInputStream(
 					new java.io.File(prefs.getString(FileBackedPreferences.KEYCHAIN_KEY))));
 		}
 		catch (Exception e)
 		{
 			Logger.getLogger(Core.class.getName())
 				.warning("Unable to open keychain: " + e.getLocalizedMessage());
 		}
 
 		plugins          = Maps.newHashMap();
 		uis              = Lists.newArrayList();
 		store            =
 			DiskStore.newBuilder()
 				.setPreferences(prefs)
 				.setDefaultDirectory()
 				.build();
 	}
 
 
 	public void registerUI(UI ui) { uis.add(ui); }
 	public void deregisterUI(UI ui) { uis.remove(ui); }
 
 	public Collection<PluginWrapper> plugins() { return plugins.values(); }
 
 
 	/** Load a plugin and wrap it up in a convenient wrapper */
 	public PluginWrapper loadPlugin(String name, URI uri) throws PluginLoadException
 	{
 		if (plugins.containsKey(uri)) return plugins.get(uri);
 
 		PluginWrapper plugin;
 		try
 		{
 			Class<?> c = pluginLoader.loadClass(uri.toString());
 			Method init = c.getMethod("init", KernelInterface.class, Logger.class);
 			Plugin p = (Plugin) init.invoke(null, this, Logger.getLogger(uri.toString()));
 			plugin = new PluginWrapper(name, uri, p);
 		}
 		catch (Exception e) { throw new PluginLoadException(uri, e); }
 
 
 		plugins.put(uri, plugin);
 		for (UI ui : uis) ui.pluginLoaded(plugin);
 
 		return plugin;
 	}
 
 
 	/** Unload a plugin; after calling this, set ALL references to null */
 	public void unloadPlugin(PluginWrapper plugin)
 	{
 		URI key = null;
 		for (Entry<URI,PluginWrapper> e : plugins.entrySet())
 			if (e.getValue().equals(plugin))
 			{
 				key = e.getKey();
 				break;
 			}
 
 		if (key == null) return;
 
 		for (UI ui : uis) ui.pluginUnloading(plugins.get(key));
 		plugins.remove(key);
 	}
 
 
 	// KernelInterface implementation
 	public java.util.UUID generateUUID() { return java.util.UUID.randomUUID(); }
 
 	@Override public File save(ByteBuffer data) throws IOException
 	{
 		List<ByteBuffer> chunked = Lists.newLinkedList();
 		chunked.add(data);  // TODO: actually chunk properly
 		try
 		{
 			File f = File.newBuilder()
 				.setContent(chunked)
 				.freeze();
 
 			store.store(f.toSave());
 			return f;
 		}
 		catch (Exception e)
 		{
 			String message = "Unable to save user data: " + e.getMessage();
 			log.warning(message);
			throw new IOException(message, e);
 		}
 	}
 
 	/** Name of the Core {@link Logger}. */
 	public static final String CORE_LOG_NAME = "me.footlights.core";
 	private static Logger log = Logger.getLogger(Core.class.getCanonicalName());
 
 	/** User preferences. */
 	private Preferences prefs;
 
 	/** Our keychain */
 	private Keychain keychain;
 
 	/** {@link ClassLoader} used to load {@link Plugin}s. */
 	private final ClassLoader pluginLoader;
 
 	/** Loaded plugins */
 	private Map<URI,PluginWrapper> plugins;
 
 	/** UIs which might like to be informed of events */
 	private List<UI> uis;
 
 	/** Where we are storing user data. */
 	private Store store;
 }
