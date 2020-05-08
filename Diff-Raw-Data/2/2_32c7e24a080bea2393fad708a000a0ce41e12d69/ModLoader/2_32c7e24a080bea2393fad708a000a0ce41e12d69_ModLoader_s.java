 package btwmods;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.lang.reflect.Method;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Enumeration;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.AbstractMap.SimpleEntry;
 import java.util.concurrent.ConcurrentLinkedQueue;
 import java.util.logging.Level;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipException;
 import java.util.zip.ZipFile;
 
 import net.minecraft.server.MinecraftServer;
 import net.minecraft.src.CommandBase;
 import net.minecraft.src.World;
 
 import btwmods.events.IAPIListener;
 import btwmods.io.Settings;
 
 public class ModLoader {
 	
 	private ModLoader() {}
 
 	/**
 	 * Settings used by ModLoader.
 	 */
 	private static Settings settings = null;
 	
 	/**
 	 * The thread that ModLoader was initialized in.
 	 */
 	private static Thread thread = null;
 	
 	/**
 	 * Whether or not init() has been called.
 	 */
 	private static boolean hasInit = false;
 	
 	/**
 	 * The class loader used by ModLoader. May be a {@link URLClassLoader}.
 	 */
 	private static ClassLoader classLoader = null;
 	
 	/**
 	 * The URLs that were originally in the {@link URLClassLoader}, and ones added to load mods.
 	 * This is empty if {@link #classLoader} is not a URLClassLoader.
 	 */
 	private static Set<URL> classLoaderUrls = new HashSet<URL>();
 	
 	/**
 	 * The protected method {@link URLClassLoader#addUrl(URL)}, set to be public.
 	 * This is <code>null</code> if {@link #classLoader} is not a URLClassLoader.
 	 */
 	private static Method classLoaderAddURLMethod = null;
 	
 	/**
 	 * Version label for BTWMods
 	 */
 	public static final String VERSION = "1.7 (vMC 1.3.2 BTW 4.22)";
 	
 	/**
 	 * Pattern that IMod class files must match.
 	 */
 	public static final String BTWMOD_REGEX = "(?i)^(BTWMod|mod_|BTWMod_).*\\.class$";
 	
 	/**
 	 * Pattern that IMod class files must match in zip files.
 	 */
	public static final String BTWMODZIP_REGEX = "(?i)^btwmods/(BTWMod|mod_|BTWMod_).*\\.class$";
 	
 	/**
 	 * Prefixed to the output of {@link #outputInfo} and {@link #outputError} calls.
 	 */
 	private static final String LOGPREFIX = "BTWMods: ";
 	
 	/**
 	 * Holds failed listeners from other threads.
 	 */
 	private static ConcurrentLinkedQueue<SimpleEntry<Throwable, IAPIListener>> failedListenerQueue = new ConcurrentLinkedQueue<SimpleEntry<Throwable, IAPIListener>>();
 	
 	/**
 	 * File or folder names that will be ignored from the btwmods directory.
 	 */
 	private static Set<String> ignoredMods = new HashSet<String>();
 	
 	/**
 	 * Mod class names that will be ignored.
 	 * 
 	 * May be one of the following formats:
 	 *   - btwmod.{modpackage}.{classname}
 	 *   - {modpackage}.{classname}
 	 *   - btwmod.{modpackage}
 	 *   - {modpackage}
 	 * 
 	 * The last two formats will ignore all mods in that mod package.
 	 */
 	private static Set<String> ignoredModClasses = new HashSet<String>();
 	
 	/**
 	 * Initialize the ModLoader and mods. Should only be called from the {@link World} constructor.
 	 */
 	public static void init() {
 		if (!hasInit) {
 			
 			// Mark the current thread as the main one.
 			thread = Thread.currentThread();
 			
 			outputInfo("Version " + VERSION + " initializing...");
 			
 			// Attempt to get the URLClassLoader and its private addURL() method.
 			if (classLoader == null) {
 				classLoader = ModLoader.class.getClassLoader();
 				
 				if (classLoader instanceof URLClassLoader) {
 					try {
 						classLoaderUrls.addAll(Arrays.asList(((URLClassLoader)classLoader).getURLs()));
 						classLoaderAddURLMethod = URLClassLoader.class.getDeclaredMethod("addURL", new Class[] { URL.class });
 						classLoaderAddURLMethod.setAccessible(true);
 					} catch (Throwable e) {
 						outputError(e, "Could not load mods from the class path (i.e. any mods directly in your minecraft_server.jar).");
 					}
 				}
 				
 				// TODO: Can we use our own URLClassLoader instead?
 			}
 			
 			// Load settings.
 			settings = loadSettings("BTWMods");
 			
 			try {
 				StatsAPI.init(settings);
 			}
 			catch (Exception e) {
 				outputError(e, "StatsAPI failed (" + e.getClass().getSimpleName() + ") to load: " + e.getMessage(), Level.SEVERE);
 				outputError("Initialization aborted.", Level.SEVERE);
 				hasInit = true;
 				return;
 			}
 			
 			try {
 				TranslationsAPI.init();
 			}
 			catch (Exception e) {
 				outputError(e, "TranslationsAPI failed (" + e.getClass().getSimpleName() + ") to load: " + e.getMessage(), Level.SEVERE);
 				outputError("Initialization aborted.", Level.SEVERE);
 				hasInit = true;
 				return;
 			}
 			
 			try {
 				CommandsAPI.init();
 			}
 			catch (Exception e) {
 				outputError(e, "CommandsAPI failed (" + e.getClass().getSimpleName() + ") to load: " + e.getMessage(), Level.SEVERE);
 				outputError("Initialization aborted.", Level.SEVERE);
 				hasInit = true;
 				return;
 			}
 			
 			try {
 				ServerAPI.init(settings);
 			}
 			catch (Exception e) {
 				outputError(e, "ServerAPI failed (" + e.getClass().getSimpleName() + ") to load: " + e.getMessage(), Level.SEVERE);
 				outputError("Initialization aborted.", Level.SEVERE);
 				hasInit = true;
 				return;
 			}
 			
 			if (settings.hasKey("ignoredmodclasses")) {
 				ignoredModClasses.addAll(Arrays.asList(settings.get("ignoredmodclasses").split("[^A-Za-z0-9_\\.\\$]+")));
 			}
 			
 			if (settings.hasKey("ignoredmods")) {
 				ignoredMods.addAll(Arrays.asList(settings.get("ignoredmods").split("[\\s;,]+")));
 			}
 			
 			findModsInClassPath();
 			findModsInFolder(new File(".", "btwmods"));
 
 			outputInfo("Initialization complete.");
 		}
 		
 		hasInit = true;
 	}
 	
 	/**
 	 * Return if the current thread is the same one that ModLoader was initialized in.
 	 * 
 	 * @return true if the current thread is the same ModLoader was initialized in; false otherwise.
 	 */
 	public static boolean inInitThread() {
 		return Thread.currentThread() == thread;
 	}
 	
 	/**
 	 * Return the thread that ModLoader was initialized in.
 	 * 
 	 * @return the {@link Thread} instance
 	 */
 	public static Thread getInitThread() {
 		return thread;
 	}
 	
 	/**
 	 * Returns if {@link URLClassLoader} was successfully set.
 	 * 
 	 * @return <code>true</code> if it was set; <code>false</code> otherwise.
 	 */
 	public static boolean hasURLClassLoader() {
 		return classLoader != null && classLoader instanceof URLClassLoader;
 	}
 	
 	/**
 	 * Returns if {@link URLClassLoader} was successfully set and we can add URLs to it.
 	 * 
 	 * @return <code>true</code> if we can add URLs; <code>false</code> otherwise.
 	 */
 	public static boolean supportsAddClassLoaderURL() {
 		return hasURLClassLoader() && classLoaderAddURLMethod != null;
 	}
 	
 	/**
 	 * Add a URL to {@link ModLoader}'s class loader.
 	 * 
 	 * @see <a href="http://www.javafaq.nu/java-example-code-895.html">http://www.javafaq.nu/java-example-code-895.html</a>
 	 * @param url
 	 * @return <code>true</code> if the URL was added (or is already set); <code>false</code> if {@link #supportsAddClassLoaderURL()} returns <code>false</code> or the URL could not be added.
 	 * @throws IllegalArgumentException
 	 */
 	public static boolean addClassLoaderURL(URL url) throws IllegalArgumentException {
 		if (url == null)
 			throw new IllegalArgumentException("url argument cannot be null");
 		
 		if (classLoaderUrls.contains(url))
 			return true;
 		
 		// Add the URL only if the private URLClassLoader#addURL() method has been set.
 		if (supportsAddClassLoaderURL()) {
 			try {
 				classLoaderAddURLMethod.invoke((URLClassLoader)classLoader, new Object[] { url });
 				return true;
 			} catch (Throwable e) {
 				outputError(e, "Failed (" + e.getClass().getSimpleName() + ") to add the following mod path: " + url.toString());
 			}
 		}
 		
 		return false;
 	}
 	
 	private static void findModsInFolder(File modsDir) {
 		if (!modsDir.exists()) {
 			modsDir.mkdir();
 		}
 		
 		if (modsDir.isDirectory()) {
 			
 			// Get a list of mod folders/files. The contents of these folders/files should be in package format.
 			File[] file = modsDir.listFiles();
 			if (file != null) {
 				for (int i = 0; i < file.length; i++) {
 					try {
 						String name = file[i].getName();
 						
 						if (!ignoredMods.contains(name)) {
 							// Load mod from a regular directory.
 							if (file[i].isDirectory()) {
 								String[] binaryNames = getModBinaryNamesFromDirectory(file[i]);
 								if (binaryNames.length > 0 && addClassLoaderURL(file[i].toURI().toURL())) {
 									loadMods(binaryNames);
 								}
 							}
 							
 							// Load mod from a zip or jar.
 							else if (file[i].isFile() && (name.endsWith(".jar") || name.endsWith(".zip"))) {
 								String[] binaryNames = getModBinaryNamesFromZip(file[i]);
 								if (binaryNames.length > 0 && addClassLoaderURL(file[i].toURI().toURL())) {
 									loadMods(binaryNames);
 								}
 							}
 						}
 					} catch (Throwable e) {
 						outputError(e, "Failed (" + e.getClass().getSimpleName() + ") to load mods from: " + file[i].getPath());
 					}
 				}
 			}
 		}
 	}
 	
 	private static void findModsInClassPath() {
 		for (URL url : classLoaderUrls) {
 			try {
 				File path = new File(url.toURI());
 				String name = path.getName();
 				
 				// Load mod from a regular directory.
 				if (path.isDirectory()) {
 					loadMods(getModBinaryNamesFromDirectory(path));
 				}
 				
 				// Load mod from a zip or jar.
 				else if (path.isFile() && (name.endsWith(".jar") || name.endsWith(".zip"))) {
 					loadMods(getModBinaryNamesFromZip(path));
 				}
 			} catch (Throwable e) {
 				outputError(e, "Failed (" + e.getClass().getSimpleName() + ") to search the following classpath for mods: " + url.toString());
 			}
 		}
 	}
 	
 	/**
 	 * Searches a directory for a 'btwmod' package dir, sub package dirs under it (i.e. 'mymod'), and then for BTWMod*.class files.
 	 * 
 	 * @param directory The directory to search.
 	 * @return An array of binary names (see {@link ClassLoader}.
 	 */
 	private static String[] getModBinaryNamesFromDirectory(File directory) {
 		ArrayList<String> names = new ArrayList<String>();
 		
 		// Make sure the 'btwmod' package folder exists.
 		File btwmodPackage = new File(directory, "btwmod");
 		if (btwmodPackage.isDirectory()) {
 			
 			// Loop through the second level of package names, which should be for mods.
 			File[] modPackages = btwmodPackage.listFiles();
 			if (modPackages != null) {
 				for (int p = 0; p < modPackages.length; p++) {
 					if (modPackages[p].isDirectory()) {
 						
 						// Check for mod_*.class files.
 						File[] classNames = modPackages[p].listFiles();
 						if (classNames != null) {
 							for (int c = 0; c < classNames.length; c++) {
 								if (classNames[c].isFile() && classNames[c].getName().matches(BTWMOD_REGEX)) {
 									names.add("btwmod." + modPackages[p].getName() + "." + classNames[c].getName().substring(0, classNames[c].getName().length() - ".class".length()));
 								}
 							}
 						}
 					}
 				}
 			}
 		}
 		
 		return names.toArray(new String[names.size()]);
 	}
 	
 	/**
 	 * Searches a zip file (or jar) for files that match the package pattern btwmod.{somename}.BTWMod{somename}.class.
 	 * 
 	 * @see <a href="http://www.javamex.com/tutorials/compression/zip_individual_entries.shtml">http://www.javamex.com/tutorials/compression/zip_individual_entries.shtml</a>
 	 * @param zip The zip (or jar) file to search.
 	 * @return An array of binary names (see {@link ClassLoader}.
 	 * @throws IllegalStateException If an action is taken on a closed zip file.
 	 * @throws ZipException
 	 * @throws IOException If the zip file could not be loaded.
 	 */
 	private static String[] getModBinaryNamesFromZip(File zip) throws IllegalStateException, ZipException, IOException {
 		ArrayList<String> names = new ArrayList<String>();
 		
 		ZipFile zipFile = null;
 		try {
 			zipFile = new ZipFile(zip);
 			
 			for (Enumeration<? extends ZipEntry> list = zipFile.entries(); list.hasMoreElements(); ) {
 				ZipEntry entry = list.nextElement();
 				if (!entry.isDirectory() && entry.getName().matches(BTWMODZIP_REGEX)) {
 					names.add(entry.getName().substring(0, entry.getName().length() - ".class".length()).replace('/',  '.'));
 				}
 			}
 			
 			return names.toArray(new String[names.size()]);
 			
 		} finally {
 			if (zipFile != null)
 				try { zipFile.close(); } catch (Throwable e) { }
 		}
 	}
 	
 	private static void loadMod(String binaryName) {
 		if (ignoredModClasses.contains(binaryName) || ignoredModClasses.contains(binaryName.substring("btwmod.".length()))
 				|| ignoredModClasses.contains("btwmod." + getModPackageName(binaryName)) || ignoredModClasses.contains(getModPackageName(binaryName)))
 			return;
 		
 		try {
 			Class mod = classLoader.loadClass(binaryName);
 			if (IMod.class.isAssignableFrom(mod)) {
 				IMod modInstance = (IMod)mod.newInstance();
 				String modName = modInstance.getName();
 				
 				try {
 					modInstance.init(loadModSettings(binaryName));
 				}
 				catch (Throwable e) {
 					outputError(e, "Failed (" + e.getClass().getSimpleName() + ") while running init for: " + binaryName);
 				}
 				
 				outputInfo("Loaded: " + (modName == null ? binaryName : modName + " (" + binaryName + ")"));
 			}
 		} catch (Throwable e) {
 			outputError(e, "Failed (" + e.getClass().getSimpleName() + ") to create an instance of: " + binaryName);
 		}
 	}
 	
 	private static void loadMods(String[] binaryNames) {
 		for (int n = 0; n < binaryNames.length; n++) {
 			loadMod(binaryNames[n]);
 		}
 	}
 	
 	private static Settings loadSettings(String name) {
 		File settingsFile = new File(new File("."), "btwmods/" + name + ".txt");
 		
 		if (settingsFile.isFile()) {
 			try {
 				return Settings.readSettings(settingsFile);
 			}
 			catch (IOException e) {
 				outputError(e, "Failed (" + e.getClass().getSimpleName() + ") to read the settings file from " + settingsFile.getPath());
 			}
 		}
 		
 		return new Settings();
 	}
 	
 	private static Settings loadModSettings(String binaryName) {
 		return loadSettings(getModPackageName(binaryName));
 	}
 	
 	public static String getModPackageName(IMod mod) {
 		return getModPackageName(mod.getClass().getName());
 	}
 	
 	private static String getModPackageName(String binaryName) {
 		return binaryName.replaceAll("^btwmod\\.([^.]+)\\..+$", "$1");
 	}
 	
 	public static void outputError(String message) {
 		outputError(message, Level.WARNING);
 	}
 	
 	public static void outputError(String message, Level level) {
 		MinecraftServer.logger.log(level, LOGPREFIX + message);
 	}
 	
 	public static void outputError(Throwable throwable, String message) {
 		outputError(throwable, message, Level.WARNING);
 	}
 	
 	public static void outputError(Throwable throwable, String message, Level level) {
 		outputError(message, level);
 		throwable.printStackTrace(new PrintStream(System.out));
 	}
 	
 	public static void outputInfo(String message) {
 		MinecraftServer.logger.info(LOGPREFIX + message);
 	}
 	
 	/**
 	 * Process any failed listeners that were queued due to being from another thread.
 	 */
 	public static void processFailureQueue() {
 		SimpleEntry<Throwable, IAPIListener> entry;
 		if (inInitThread()) {
 			while ((entry = failedListenerQueue.poll()) != null) {
 				reportListenerFailure(entry.getKey(), entry.getValue());
 			}
 		}
 	}
 	
 	public static void reportListenerFailure(Throwable t, IAPIListener listener) {
 		// Queue the failure if it is coming from another thread.
 		if (!inInitThread()) {
 			failedListenerQueue.add(new SimpleEntry<Throwable, IAPIListener>(t, listener));
 			return;
 		}
 		
 		processFailureQueue();
 		
 		// Remove the listener from all APIs.
 		StatsAPI.removeListener(listener);
 		WorldAPI.removeListener(listener);
 		NetworkAPI.unregisterCustomChannels(listener);
 		PlayerAPI.removeListener(listener);
 		
 		IMod mod = null;
 		String name = listener.getClass().getName();
 		boolean unloadSuccess = false;
 		
 		try {
 			mod = listener.getMod();
 			if (mod != null) {
 				try {
 					name = mod.getName() + " (" + name + ")";
 				}
 				catch (Throwable e) { }
 				
 				mod.unload();
 				unloadSuccess = true;
 			}
 		}
 		catch (Throwable e) { }
 		
 		// TODO: alert server admins to failed mods.
 		outputError(t, name + " threw a " + t.getClass().getSimpleName() + (t.getMessage() == null ? "." : ": " + t.getMessage()), Level.SEVERE);
 		
 		if (unloadSuccess)
 			outputError(name + " has been unloaded successfully.", Level.INFO);
 		else
 			outputError(name + " has been unloaded disabled as much as possible.", Level.SEVERE);
 	}
 
 	public static void reportCommandFailure(RuntimeException e, CommandBase command, IMod mod) {
 
 		String name = command.getClass().getName();
 		try {
 			name = mod.getName() + " (" + name + ")";
 		}
 		catch (Throwable ex) { }
 		
 		// Unregister the command so it does not run again.
 		CommandsAPI.unregisterCommand(command);
 		
 		outputError(e, name + " threw a " + e.getClass().getSimpleName() + (e.getMessage() == null ? "." : ": " + e.getMessage()), Level.SEVERE);
 	}
 }
