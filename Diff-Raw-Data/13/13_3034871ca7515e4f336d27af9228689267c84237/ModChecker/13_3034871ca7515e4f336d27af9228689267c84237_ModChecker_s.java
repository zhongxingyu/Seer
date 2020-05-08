 package de.davboecki.multimodworld.server;
 
 import java.lang.reflect.Field;
 import java.util.ArrayList;
 import java.util.Collections;
 
 import net.minecraft.server.BaseMod;
 import net.minecraft.server.ModLoader;
 import net.minecraft.server.World;
 
 import org.bukkit.Bukkit;
 import org.bukkit.craftbukkit.CraftServer;
 import org.bukkit.plugin.Plugin;
 
 import de.davboecki.multimodworld.server.plugin.IModWorldHandlePlugin;
 
 public class ModChecker {
 	
 	private static IModWorldHandlePlugin staticplugin = null;
 	
 	private static IModWorldHandlePlugin checkField(Field field,Plugin instance) throws Exception{
 		field.setAccessible(true);
 		Object pluginobject;
 		pluginobject = field.get(instance);
 		return (pluginobject instanceof IModWorldHandlePlugin?((IModWorldHandlePlugin)pluginobject):null);
 	}
 	
 	private static IModWorldHandlePlugin getModWorldHandlePlugin() {
 		if(staticplugin != null) return staticplugin;
 		CraftServer server = (CraftServer)Bukkit.getServer();
 		Plugin[] plugins = server.getPluginManager().getPlugins();
 		boolean pluginfound = false;
 		boolean morepluginsfound = false;
 		IModWorldHandlePlugin plugin = null;
 		for(Plugin instance:plugins) {
 			try {
 				if(instance instanceof IModWorldHandlePlugin){
 					if(pluginfound && plugin != instance){
 						morepluginsfound = true;
 						break;
 					} else {
 						pluginfound = true;
 						plugin = (IModWorldHandlePlugin)instance;
 					}
 				} else {
 					try {
 						for(Field field:instance.getClass().getDeclaredFields()) {
							if((plugin = checkField(field,instance)) != null){
								if(pluginfound && !plugin.equals(field.get(instance))){
 									morepluginsfound = true;
 									break;
 								} else {
 									pluginfound = true;
									plugin = (IModWorldHandlePlugin)field.get(instance);
 								}
 							}
 						}
 					} catch(NoClassDefFoundError e){}
 				}
			} catch(Exception e){}
 		}
 		if(morepluginsfound){
 			server.getLogger().severe("[MultiModWorld] More then one ModWorldHandlePlugin found.");
 			return null;
 			//server.shutdown();
 			//throw new IllegalStateException("[MultiModWorld] More then one ModWorldHandlePlugin found.");
 		}
 		if(!pluginfound){
 			server.getLogger().severe("[MultiModWorld] No ModWorldHandlePlugin found.");
 			return null;
 			//server.shutdown();
 			//throw new IllegalStateException("[MultiModWorld] No ModWorldHandlePlugin found.");
 		}
 		return staticplugin = plugin;
 	}
 
 	private static ArrayList<BaseMod> ModList = new ArrayList<BaseMod>();
 	private static ArrayList<ModBlockAddList> AddedBlockList = new ArrayList<ModBlockAddList>();
 
 	public static boolean isIdAllowed(String WorldName, int id){
 		if(getModWorldHandlePlugin() == null) return true;
 		return getModWorldHandlePlugin().isIdAllowed(WorldName, id);
 	}
 	
 	public static boolean isCraftingAllowed(String WorldName, int id){
 		if(getModWorldHandlePlugin() == null) return true;
 		return getModWorldHandlePlugin().isCraftingAllowed(WorldName, id);
 	}
 
 	public static boolean isEntityAllowed(String WorldName, net.minecraft.server.Entity entity){
 		if(getModWorldHandlePlugin() == null) return true;
 		return getModWorldHandlePlugin().isEntityAllowed(WorldName, entity);
 	}
 	
 	public static boolean hasWorldSetting(String WorldName, String Setting){
 		if(getModWorldHandlePlugin() == null) return true;
 		return getModWorldHandlePlugin().hasWorldSetting(WorldName, Setting);
 	}
 	
 	public static World getModWorldbyTag(String Tag){
 		if(getModWorldHandlePlugin() == null) return (World)ModLoader.getMinecraftServerInstance().worlds.get(0);
 		for(int i = 0; i < ModLoader.getMinecraftServerInstance().worlds.size();i++){
 			  World world = (World)ModLoader.getMinecraftServerInstance().worlds.get(i);
 			  if(hasWorldSetting(world.getWorld().getName(), Tag)) {
 				  return world;
 			  }
 		  }
 		Bukkit.getServer().getLogger().severe("[MultiModWorld] No world with tag "+Tag+" found. Returning default world.");
 		return (World)ModLoader.getMinecraftServerInstance().worlds.get(0);
 	}
 	
 	public static void ModLoaded(BaseMod Mod){
 		ModList.add(Mod);
 	}
 	
 	public static void ModAddedBlock(BaseMod Mod, int[] Ids){
 		AddedBlockList.add(new ModBlockAddList(Mod,Ids));
 	}
 	
 	public static ArrayList<BaseMod> getModList(){
 		return (ArrayList<BaseMod>)Collections.unmodifiableList(ModList);
 	}
 	
 	public static ArrayList<ModBlockAddList> getAddedBlockList(){
 		return (ArrayList<ModBlockAddList>)Collections.unmodifiableList(AddedBlockList);
 	}
 }
