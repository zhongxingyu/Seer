 package btwmods;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Properties;
 
 import net.minecraft.server.MinecraftServer;
 
 public class ModLoader<P, T extends IMod> {
 
 	private Map<Class, HashSet<Class>> ModClassCache = new HashMap<Class, HashSet<Class>>();
 	
 	private Class parentClass;
	private Class<T> modClass;
 	
	public ModLoader(Class parentClass, Class<T> modClass) {
 		this.parentClass = parentClass;
		this.modClass = modClass;
 	}
 	
 	public HashSet<Class> loadModClasses(String modList) {
 		HashSet<Class> classes;
 		
 		if ((classes = ModClassCache.get(this.parentClass)) == null) {
 			classes = new HashSet<Class>();
 			
 			if (modList != null && !modList.isEmpty()) {
 				String[] modNames = modList.split("[:,;]+");
 				for (int i = 0; i < modNames.length; i++) {
 					if (!modNames[i].isEmpty()) {
 						try {
 							Class modClass = Class.forName(modNames[i]);
							if (modClass.isAssignableFrom(modClass)) {
 								classes.add(modClass);
 							}
 							else {
								MinecraftServer.logger.warning("BTWMod's " + this.parentClass.getSimpleName() + " failed to load the '" + modNames[i] + "' mod class, as it does not extend " + modClass.getName() + ".");
 							}
 						} catch (ClassNotFoundException e) {
 							MinecraftServer.logger.warning("BTWMod's " + this.parentClass.getSimpleName() + " failed to find the class for the '" + modNames[i] + "' mod.");
 						}
 					}
 				}
 			}
 			
 			ModClassCache.put(this.parentClass, classes);
 		}
 		
 		return classes;
 	}
 	
 	public Mods createMods(String modList) {
 		HashSet<Class> classes = this.loadModClasses(modList);
 		HashSet<T> mods = new HashSet<T>();
 		
 		for (Class modClass : classes) {
 			try {
 				mods.add((T)modClass.getDeclaredConstructor().newInstance());
 			} catch (Exception e) {
 				MinecraftServer.logger.warning("BTWMod's " + this.parentClass.getSimpleName() + " failed to create an instance of the '" + modClass.getName() + "' mod (" + e.getClass().getName() + "): " + e.getMessage());
 			}
 		}
 		
 		return new Mods(mods);
 	}
 	
 	public class Mods {
 		
 		private HashSet<T> mods;
 		
 		private Mods(HashSet<T> mods) {
 			this.mods = mods;
 		}
 		
 		public void initMods(P parentMod) {
 			for (T mod : mods) {
 				mod.init(parentMod);
 			}
 		}
 
 		public void unloadMods(P parentMod) {
 			for (T mod : mods) {
 				mod.unload(parentMod);
 			}
 			mods.clear();
 		}
 	}
 }
