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
 
 import java.io.ByteArrayOutputStream;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.lang.reflect.Method;
 import java.net.URI;
 import java.nio.ByteBuffer;
 import java.security.AccessController;
 import java.security.AllPermission;
 import java.security.GeneralSecurityException;
 import java.security.PrivilegedExceptionAction;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.NoSuchElementException;
 import java.util.Map.Entry;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 
 import me.footlights.core.crypto.Fingerprint;
 import me.footlights.core.crypto.Keychain;
 import me.footlights.core.data.File;
 import me.footlights.core.data.Link;
 import me.footlights.core.data.store.DiskStore;
 import me.footlights.core.data.store.Store;
 import me.footlights.core.plugin.PluginLoadException;
 import me.footlights.core.plugin.PluginWrapper;
 import me.footlights.plugin.KernelInterface;
 import me.footlights.plugin.ModifiablePreferences;
 import me.footlights.plugin.Plugin;
 
 
 public class Core implements Footlights
 {
 	public static Core init(ClassLoader pluginLoader) throws Exception
 	{
 		// This is the Footlights core, the security kernel; ensure that we can do anything.
 		AccessController.checkPermission(new AllPermission());
 
 		FileBackedPreferences prefs = FileBackedPreferences.loadFromDefaultLocation();
 		Flusher.apply(prefs).start();
 
 		final Keychain keychain = Keychain.create();
 		final java.io.File keychainFile =
 			new java.io.File(prefs.getString(FileBackedPreferences.KEYCHAIN_KEY));
 
 		if (keychainFile.exists())
 			try { keychain.importKeystoreFile(new FileInputStream(keychainFile)); }
 			catch (IOException e) { log.log(Level.SEVERE, "Error loading keychain", e); }
 
 		Flusher.apply(keychain, keychainFile).start();
 
 		Map<URI,PluginWrapper> plugins = Maps.newHashMap();
 		List<UI> uis = Lists.newArrayList();
 		Store store = DiskStore.newBuilder()
 				.setPreferences(Preferences.create(prefs))
 				.setDefaultDirectory()
 				.build();
		Flusher.apply(store).start();
 
 		return new Core(pluginLoader, prefs, keychain, plugins, uis, store);
 	}
 
 
 	public void registerUI(UI ui) { uis.add(ui); }
 	public void deregisterUI(UI ui) { uis.remove(ui); }
 
 	public Collection<PluginWrapper> plugins() { return plugins.values(); }
 
 
 	/** Load a plugin and wrap it up in a convenient wrapper */
 	public PluginWrapper loadPlugin(final String name, final URI uri) throws PluginLoadException
 	{
 		if (plugins.containsKey(uri)) return plugins.get(uri);
 
 		ModifiablePreferences pluginPreferences = openPreferences(name);
 
 		final Plugin plugin;
 		try
 		{
 			Class<?> c = AccessController.doPrivileged(
 				new PrivilegedExceptionAction<Class<?> >()
 					{
 						@Override public Class<?> run() throws Exception
 						{
 							return pluginLoader.loadClass(uri.toString());
 						}
 					});
 
 			Method init = c.getMethod("init",
 				KernelInterface.class,
 				me.footlights.plugin.ModifiablePreferences.class,
 				Logger.class);
 
 			plugin = (Plugin) init.invoke(null,
 				this, pluginPreferences, Logger.getLogger(uri.toString()));
 		}
 		catch (Exception e) { throw new PluginLoadException(uri, e); }
 
 		PluginWrapper wrapper = new PluginWrapper(name, uri, plugin);
 		plugins.put(uri, wrapper);
 		for (UI ui : uis) ui.pluginLoaded(wrapper);
 
 		return wrapper;
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
 
 	@Override public File open(String name) throws IOException
 	{
 		// Construct a complete Link, including secret key.
 		final Link link;
 		try { link = keychain.getLink(Fingerprint.decode(name)); }
 		catch (Exception e)
 		{
 			throw new IOException("Unable to find secret key for file '" + name + "'");
 		}
 
 		try { return store.fetch(link); }
 		catch (GeneralSecurityException e) { throw new IOException(e); }
 	}
 
 	@Override public File save(ByteBuffer data) throws IOException
 	{
 		List<ByteBuffer> chunked = Lists.newLinkedList();
 		chunked.add(data);  // TODO: actually chunk properly
 		try
 		{
 			final File f = File.newBuilder()
 				.setContent(chunked)
 				.freeze();
 
 			AccessController.doPrivileged(new PrivilegedExceptionAction<Void>()
 				{
 					@Override
 					public Void run() throws IOException
 					{
 						store.store(f.toSave());
 						return null;
 					}
 				});
 
 			return f;
 		}
 		catch (Exception e)
 		{
 			String message = "Unable to save user data: " + e.getMessage();
 			log.warning(message);
 			throw new IOException(message, e);
 		}
 	}
 
 
 	private Core(ClassLoader pluginLoader, FileBackedPreferences prefs, Keychain keychain,
 			Map<URI,PluginWrapper> plugins, List<UI> uis, Store store)
 	{
 		this.pluginLoader = pluginLoader;
 		this.prefs = prefs;
 		this.keychain = keychain;
 		this.plugins = plugins;
 		this.uis = uis;
 		this.store = store;
 	}
 
 	private ModifiablePreferences openPreferences(final String plugin)
 	{
 		final HashMap<String,String> toSave = Maps.newHashMap();
 		final String pluginKey = "plugin.prefs." + plugin;
 
 		try
 		{
 			final String filename = prefs.getString(pluginKey);
 			File file = open(filename);
 			Object o = new ObjectInputStream(file.getInputStream()).readObject();
 			HashMap<?,?> loaded = ((HashMap<?,?>) o);
 
 			// Explicitly convert objects of whatever form into Strings.
 			for (Map.Entry<?,?> entry : loaded.entrySet())
 				toSave.put(entry.getKey().toString(), entry.getValue().toString());
 		}
 		catch (NoSuchElementException e)
 		{
 		}
 		catch (Exception e)
 		{
 			log.log(Level.WARNING, "Error opening preferences for " + plugin, e);
 		}
 
 		final PreferenceStorageEngine reader = PreferenceStorageEngine.wrap(toSave);
 		return new ModifiableStorageEngine()
 		{
 			private File savePrefs() throws IOException
 			{
 				ByteArrayOutputStream out = new ByteArrayOutputStream();
 				new ObjectOutputStream(out).writeObject(toSave);
 				return save(ByteBuffer.wrap(out.toByteArray()));
 			}
 
 			@Override public synchronized ModifiableStorageEngine set(String key, String value)
 			{
 				toSave.put(key, value);
 
 				try
 				{
 					File saved = savePrefs();
 					saved.link().saveTo(keychain);
 					prefs.set(pluginKey, saved.link().fingerprint().encode());
 				}
 				catch (IOException e) { log.log(Level.WARNING, "Unable to save prefs", e); }
 
 				notifyAll();
 				return this;
 			}
 
 			@Override protected Map<String, ?> getAll() { return reader.getAll(); }
 			@Override protected String getRaw(String key) throws NoSuchElementException
 			{
 				return reader.getRaw(key);
 			}
 		};
 	}
 
 	/** Name of the Core {@link Logger}. */
 	public static final String CORE_LOG_NAME = "me.footlights.core";
 	private static Logger log = Logger.getLogger(Core.class.getCanonicalName());
 
 	/** User preferences. */
 	private final FileBackedPreferences prefs;
 
 	/** Our keychain */
 	private final Keychain keychain;
 
 	/** {@link ClassLoader} used to load {@link Plugin}s. */
 	private final ClassLoader pluginLoader;
 
 	/** Loaded plugins */
 	private final Map<URI,PluginWrapper> plugins;
 
 	/** UIs which might like to be informed of events */
 	private final List<UI> uis;
 
 	/** Where we are storing user data. */
 	private final Store store;
 }
