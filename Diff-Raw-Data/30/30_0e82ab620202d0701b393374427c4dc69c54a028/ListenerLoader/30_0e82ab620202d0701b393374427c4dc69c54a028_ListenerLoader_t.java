 package net.worldoftomorrow.eventcontrol.listeners;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.List;
 
 import org.bukkit.plugin.PluginManager;
 
 import net.worldoftomorrow.eventcontrol.EventControl;
 import net.worldoftomorrow.eventcontrol.Validate;
 
 public class ListenerLoader {
 	
 	private final EventControl instance = EventControl.instance();
 	private final File listenerFolder = new File(instance.getDataFolder() + "/listeners");
 	
 	private List<ECListener> listeners = new ArrayList<ECListener>();
 	private List<File> jars = new ArrayList<File>();
 	
 	private URLClassLoader classLoader;
 	
 	public ListenerLoader() {
 		if(!listenerFolder.exists()) listenerFolder.mkdir();
 		this.loadJars(); // Get the jars to be loaded
 		this.classLoader = this.createClassLoader(); // Create the class loader
 		this.instanceListeners();
 		this.registerListeners();
 	}
 	
 	private void registerListeners() {
 		PluginManager pm = EventControl.instance().getServer().getPluginManager();
 		EventControl instance = EventControl.instance();
 		for(ECListener l : listeners) {
 			pm.registerEvents(l, instance);
 		}
 	}
 
 	private void loadJars() {
 		for(File f : listenerFolder.listFiles()) {
 			if(Validate.isValidJar(f)) {
 				jars.add(f);
 			}
 		}
 	}
 	
 	private URLClassLoader createClassLoader() {
 		List<URL> urls = new ArrayList<URL>();
 		for(File jar : jars) {
 			try {
 				urls.add(jar.toURI().toURL());
 			} catch (MalformedURLException e) {
 				e.printStackTrace();
 			}
 		}
 		return new URLClassLoader(urls.toArray(new URL[urls.size()]), EventControl.class.getClassLoader());
 	}
 	
 	private List<Class<ECListener>> getListeners() {
 		List<Class<ECListener>> listeners = new ArrayList<Class<ECListener>>();
 		for(String listenerName : getListenersToLoad()) {
 			Class<ECListener> listener = getListener(listenerName);
 			if(listener != null) listeners.add(listener);
 		}
 		return listeners;
 	}
 	
 	@SuppressWarnings("unchecked")
 	private Class<ECListener> getListener(String name) {
 		try {
 			Class<?> clazz = classLoader.loadClass(name);
 			if(clazz.isAssignableFrom(clazz)) {
 				return (Class<ECListener>) clazz;
 			}
 			return null;
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 	
 	private String[] getListenersToLoad() {
 		List<String> toLoad = new ArrayList<String>();
 		try {
 			Enumeration<URL> listenerFiles = classLoader.findResources("listeners.txt");
 			while(listenerFiles.hasMoreElements()) {
 				BufferedReader in = new BufferedReader(
 						new InputStreamReader(listenerFiles.nextElement().openStream()));
 				String line;
 				while((line = in.readLine()) != null) {
 					toLoad.add(line);
 				}
 				in.close();
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return toLoad.toArray(new String[toLoad.size()]);
 	}
 	
 	private void instanceListeners() {
 		List<Class<ECListener>> listeners = getListeners();
 		for(Class<ECListener> listener : listeners) {
 			if(listener == null) {
 				continue;
 			}
 			try {
 				// All ECListeners should not need constructor arguments, this should be
 				// take care of by using super(Event, String);
 				Constructor<? extends ECListener> con = listener.getConstructor();
 				this.listeners.add(con.newInstance());
 			} catch (NoSuchMethodException | SecurityException | InstantiationException | 
 					IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
 				// Catch ALL the exception!
 				e.printStackTrace();
 			}
 		}
 	}
 }
