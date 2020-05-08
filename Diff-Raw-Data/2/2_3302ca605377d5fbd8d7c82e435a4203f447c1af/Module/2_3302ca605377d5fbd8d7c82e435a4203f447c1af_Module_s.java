 package pl.shockah.shocky2;
 
 import java.io.File;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import org.pircbotx.PircBotX;
 
 public abstract class Module extends ShockyListenerAdapter implements Comparable<Module> {
 	private static final List<Module> modules = Util.syncedList(Module.class), modulesOn = Util.syncedList(Module.class);
 	private static final Map<String,List<Module>> disabledModules = Collections.synchronizedMap(new HashMap<String,List<Module>>());
 	
 	public static Module load(ModuleSource<?> source) {
 		return load(source,true);
 	}
 	private static Module load(ModuleSource<?> source, boolean breakIfAlreadyLoaded) {
 		Module module = tryToLoad(source);
 		
 		if (module != null) {
 			if (breakIfAlreadyLoaded) for (int i = 0; i < modules.size(); i++) if (modules.get(i).name().equals(module.name())) return null;
 			
 			modules.add(module);
 			Data.cfg.setNotExists("module-"+module.name(),true);
 			if (Data.cfg.getBoolean("module-"+module.name())) enable(module,null);
 			for (String key : Data.cfg.getKeysSubconfigs()) if (key.startsWith("#") && !Data.getBoolean(key,"module-"+module.name())) disable(module,key);
 		}
 		return module;
 	}
 	private static Module tryToLoad(ModuleSource<?> source) {
 		Module module = null;
 		try {
 			Class<?> c = null;
 			if (source.source instanceof File) {
 				File file = (File)source.source;
 				String moduleName = file.getName(); 
 				if (moduleName.equals("Module.class")) moduleName = new StringBuilder(moduleName).reverse().delete(0,6).reverse().toString(); else return null;
 				
 				c = new URLClassLoader(new URL[]{file.getParentFile().toURI().toURL()}).loadClass(moduleName);
 			} else if (source.source instanceof URL) {
 				URL url = (URL)source.source;
 				String moduleName = url.toString();
 				StringBuilder sb = new StringBuilder(moduleName).reverse();
 				moduleName = new StringBuilder(sb.substring(0,sb.indexOf("/"))).reverse().toString();
 				String modulePath = new StringBuilder(url.toString()).delete(0,url.toString().length()-moduleName.length()).toString();
 				if (moduleName.equals("Module.class")) moduleName = new StringBuilder(moduleName).reverse().delete(0,6).reverse().toString(); else return null;
 				
 				c = new URLClassLoader(new URL[]{new URL(modulePath)}).loadClass(moduleName);
 			}
 			
 			if (c != null && Module.class.isAssignableFrom(c)) module = (Module)c.newInstance();
 		} catch (Exception e) {e.printStackTrace();}
 		return module;
 	}
 	public static boolean unload(Module module) {
 		if (module == null) return false;
 		if (!modules.contains(module)) return false;
 		if (modulesOn.contains(module)) disable(module,null);
 		modules.remove(module);
 		return true;
 	}
 	public static boolean reload(Module module) {
 		if (module == null) return false;
 		ModuleSource<?> src = module.source;
		Module m = load(src);
 		if (m != null) {
 			unload(module);
 			return true;
 		} else return false;
 	}
 	
 	public static boolean enable(Module module, String channel) {
 		if (module == null) return false;
 		if (channel != null) {
 			List<Module> disabled;
 			if (!disabledModules.containsKey(channel)) {
 				disabled = new ArrayList<Module>();
 				disabledModules.put(channel, disabled);
 			} else disabled = disabledModules.get(channel);
 			return disabled.remove(module);
 		} else {
 			if (modulesOn.contains(module)) return false;
 			module.onEnable();
 			if (module.isListener()) Shocky.botManager.listenerManager.addListener(module);
 			modulesOn.add(module);
 			return true;
 		}
 	}
 	public static boolean disable(Module module, String channel) {
 		if (module == null) return false;
 		if (channel != null) {
 			List<Module> disabled;
 			if (!disabledModules.containsKey(channel)) {
 				disabled = new ArrayList<Module>();
 				disabledModules.put(channel, disabled);
 			} else disabled = disabledModules.get(channel);
 			if (disabled.contains(module)) return false;
 			return disabled.add(module);
 		} else {
 			if (!modulesOn.contains(module)) return false;
 			if (module.isListener()) Shocky.botManager.listenerManager.removeListener(module);
 			module.onDisable();
 			modulesOn.remove(module);
 			return true;
 		}
 	}
 	
 	public static ArrayList<Module> loadNewModules() {
 		ArrayList<Module> ret = new ArrayList<Module>();
 		File dir = new File("modules"); dir.mkdir();
 		
 		ArrayList<File> dirs = new ArrayList<File>();
 		dirs.add(dir);
 		
 		while (dirs.isEmpty()) {
 			dir = dirs.remove(0);
 			for (File f : dir.listFiles()) {
 				if (f.getName().matches("\\.{1,2}")) continue;
 				if (f.isDirectory()) dirs.add(f);
 				else {
 					Module m = load(new ModuleSource<File>(f));
 					if (m != null) ret.add(m);
 				}
 			}
 		}
 		Collections.sort(ret);
 		return ret;
 	}
 	
 	private ModuleSource<?> source;
 	
 	public abstract String name();
 	public abstract String info();
 	protected boolean isListener() {return false;}
 	protected boolean canDisable() {return true;}
 	protected String[] staticClasses() {return new String[]{};}
 	
 	public void onEnable() {}
 	public void onDisable() {}
 	public void onDie(PircBotX bot) {}
 	
 	public final int compareTo(Module module) {
 		return name().compareTo(module.name());
 	}
 	
 	public final boolean isEnabled(String channel) {
 		if (disabledModules.containsKey(channel) && disabledModules.get(channel).contains(this)) return false;
 		return modulesOn.contains(this);
 	}
 }
