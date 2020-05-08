 package plugin;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.nio.file.FileSystems;
 import java.nio.file.Path;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.jar.Attributes;
 import java.util.jar.JarFile;
 import java.util.jar.Manifest;
 
 import dependencies.DependencyResolver;
 import dependencies.exception.CycleDependencyException;
 import extension.ExtensionsManager;
 import extension.IPluginListenerExtension;
 import extension.IStatusExtension;
 import fileSystemListener.DirectoryAction;
 import fileSystemListener.WatchDir;
 
 
 public class LoadableApplicationManager implements Runnable {
 	private WatchDir loadableApplicationsWatchDir;
 	
 	private HashMap<Path, ILoadableApplication> pathToPlugin;
 	private HashMap<Path, URLClassLoader> pathToJarClassloader;
 	
 	public LoadableApplicationManager() throws IOException {
 		this.pathToPlugin = new HashMap<Path, ILoadableApplication>();
		this.pathToPlugin = new HashMap<>();
 		
 		final String loadableAppsPath = BrahmaGlobalProperties.INSTANCE.getProperty(BrahmaGlobalProperties.LOADABLE_APPS_DIR_PROPERTY);
 		loadableApplicationsWatchDir = new WatchDir(FileSystems.getDefault().getPath(loadableAppsPath), false);
 	}
 
 	@Override
 	public void run() {
 		// First load existing plugins if any
 		try {
 			Path pluginDir = FileSystems.getDefault().getPath("plugins");
 			File pluginFolder = pluginDir.toFile();
 			File[] files = pluginFolder.listFiles();
 			if(files != null) {
 				for(File f : files) {
 					this.loadBundle(f.toPath());
 				}
 			}
 		}
 		catch(Exception e) {
 			e.printStackTrace();
 		}
 
 		// Listen for newly added plugins
 		processBundles(this.loadableApplicationsWatchDir);
 	}
 	
 	private void processBundles(WatchDir watchDir) {
 		Map<DirectoryAction, Path> loadMap = watchDir.processEvent();
 		Path child;
 		
 		while (!loadMap.containsKey(DirectoryAction.END)) {
 			try {
 				child = loadMap.get(DirectoryAction.LOAD);
 				if (child != null) {
 					loadBundle(child);
 				}
 
 				child = loadMap.get(DirectoryAction.UNLOAD);
 				if (child != null) {
 					unloadBundle(child);
 				}
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 			
 			loadMap = watchDir.processEvent();
 		}
 	}
 
 	private void loadBundle(Path bundlePath) {
 		// Get hold of the jar file
 		System.out.println(bundlePath.toString());
 		File jarBundle = bundlePath.toFile();
 		JarFile jarFile = null;
 		try {
 			jarFile = new JarFile(jarBundle);
 		} catch (IOException e) {
 			notifyStatus("File could not be loaded: " + bundlePath.toString());
 			return;
 		}
 
 		DependencyResolver dependencyResolver = new DependencyResolver(jarFile);
 		List<JarFile> dependencies = new ArrayList<JarFile>();
 		try {
 			dependencies = dependencyResolver.getOrderedDependencies();
 		} catch (CycleDependencyException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		for (JarFile jar : dependencies) {
 
 			boolean pluginRegistered = registerLoadableApplication(bundlePath, jar);
 			if(pluginRegistered) {
 				notifyStatus("Plugin loaded: " + bundlePath.toString());
 			} else {
 				// there was something wrong with this jar...
 				notifyStatus("The entry point of the jar file does not conform to the ILoadableApplication Interface and has not been loaded.: " + jar.getName());
 			}
 		}
 
 	}
 
 	private boolean registerLoadableApplication(Path bundlePath, JarFile jarFile) {
 
 		boolean success = false;
 
 		try {
 			// Get the manifest file in the jar file
 			Manifest mf = jarFile.getManifest();
 			Attributes mainAttribs = mf.getMainAttributes();
 
 			// Get hold of the Plugin-Class attribute and load the class
 			String className = mainAttribs.getValue("Plugin-Class");
 			
 			if (className == null) {
 				// this is not a Brahma plugin
 				return false;
 			}
 			
 			URL[] urls = new URL[]{bundlePath.toUri().toURL()};
 			URLClassLoader classLoader = new URLClassLoader(urls);
 			Class<?> pluginClass = classLoader.loadClass(className);
 
 			// Create a new instance of the plugin
 			if (ILoadableApplication.class.isAssignableFrom(pluginClass)) {
 				ILoadableApplication plugin = (ILoadableApplication)pluginClass.newInstance();
 				success = true;
 				this.pathToPlugin.put(bundlePath, plugin);
 				this.pathToJarClassloader.put(bundlePath, classLoader);
 				notifyLoaded(plugin);
 			}
 
 			// If we close the classloader, the plugin is not able to load its other classes when it needs them :(
 			// classLoader.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		return success;
 	}
 
 	private void unloadBundle(Path bundlePath) {
 		ILoadableApplication plugin = this.pathToPlugin.remove(bundlePath);
 		notifyUnloaded(plugin);
 		notifyStatus("Plugin unloaded: " + bundlePath.toString());
 		URLClassLoader bundleClassLoader = this.pathToJarClassloader.remove(bundlePath);
 		try {
 		// release the resources
 			bundleClassLoader.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private void notifyLoaded(ILoadableApplication plugin) {
 		for (IPluginListenerExtension extension : ExtensionsManager.INSTANCE.getPluginListenerExtensions()) {
 			extension.pluginLoaded(plugin);
 		}
 		// System.out.println("Plugin loaded: " + plugin.getId());
 	}
 
 	private void notifyUnloaded(ILoadableApplication plugin) {
 		for (IPluginListenerExtension extension : ExtensionsManager.INSTANCE.getPluginListenerExtensions()) {
 			extension.pluginUnloaded(plugin);
 		}
 		// System.out.println("Plugin loaded: " + plugin.getId());
 	}
 
 	// TODO: call this whenever a plugin is loaded/unloaded and whenever there is a dependency problem of some sort
 	private void notifyStatus(String message) {
 		for (IStatusExtension extension : ExtensionsManager.INSTANCE.getStatusExtensions()) {
 			extension.updateStatus(message);
 		}
 		// System.out.println("New status: " + message);
 	}
 }
