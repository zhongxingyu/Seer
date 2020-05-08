 package fr.frozentux.craftguard2.config;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Set;
 
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 
 import fr.frozentux.craftguard2.CraftGuardPlugin;
 import fr.frozentux.craftguard2.list.Id;
 import fr.frozentux.craftguard2.list.List;
 import fr.frozentux.craftguard2.list.ListManager;
 
 /**
  * Loads, writes and stores groups lists for CraftGuard
  * @author FrozenTux
  *
  */
 public class ListLoader {
 	
 	private CraftGuardPlugin plugin;
 	
 	private File configurationFile;
 	private FileConfiguration configuration;
 	
 	/**
 	 * Loads, writes and stores groups lists for CraftGuard
 	 * @author FrozenTux
 	 *
 	 */
 	public ListLoader(CraftGuardPlugin plugin, FileConfiguration fileConfiguration, File file){
 		this.plugin = plugin;
 		this.configurationFile = file;
 		this.configuration = fileConfiguration;
 	}
 	
 	/**
 	 * Loads the list from the {@link FileConfiguration} specified in the constructor
 	 * @return A HashMap of the loaded groups
 	 */
 	public HashMap<String, List> load(){
 		
 		//Initializing the groups list or clearing it
 		HashMap<String, List> groupsLists = new HashMap<String, List>();
 		
 		//If the file doesn't exist, write defaults
 		if(!configurationFile.exists()){
 			plugin.getCraftGuardLogger().debug("ListFile not existing");
 			HashMap<Integer, Id> exampleMap = new HashMap<Integer, Id>();
 			exampleMap.put(5, new Id(5));//PLANKS
 			exampleMap.put(35, new Id(35));
 			exampleMap.get(35).addMetadata(2);//Only purple WOOL
 			List exampleList = new List("example", "samplepermission", exampleMap, null, plugin.getListManager());
 			
 			configuration.addDefault(exampleList.getName() + ".list", exampleList.toStringList(false));
 			configuration.addDefault(exampleList.getName() + ".permission", exampleList.getPermission());
 			configuration.options().header("CraftGuard 2.X by FrozenTux").copyHeader(true).copyDefaults(true);
 			try {
 				configuration.save(configurationFile);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		
 		//Load the file
 		try {
 			configuration.load(configurationFile);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 		Set<String> keys = configuration.getKeys(false);
 		Iterator<String> it = keys.iterator();
 		
 		while(it.hasNext()){	//This loop will be run for each list
 			String name = it.next();
 			String permission = configuration.getString(name + ".permission");
 			String parentName = configuration.getString(name + ".parent");
 			groupsLists.put(name, new List(name, permission, configuration.getStringList(name + ".list"), parentName, plugin.getListManager()));
 		}
 		
 		plugin.getCraftGuardLogger().info("Succesfully loaded " + groupsLists.size() + " lists");
 		
 		return groupsLists;
 	}
 	
 	public void writeAllLists(ListManager manager){
 		plugin.getCraftGuardLogger().info("Saving " + manager.getListsNames().size() + " lists...");
 		
 		configurationFile.delete();
 		try{
 			configurationFile.createNewFile();
 			configuration = new YamlConfiguration();
 			System.out.println(configuration.contains("empty"));
 		}catch (Exception e){
 			e.printStackTrace();
 		}
 		
 		configuration.options().header("CraftGuard 2.X by FrozenTux").copyHeader(true);
 		
 		Iterator<String> it = manager.getListsNames().iterator();
 		
 		while(it.hasNext()){
 			writeList(manager.getList(it.next()), false);
 		}
 		
 		try {
 			configuration.save(configurationFile);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 	}
 	
 	public void writeList(List list, boolean save){
 		configuration.set(list.getName() + ".list", list.toStringList(false));
 		if(!list.getPermission().equals(list.getName()))configuration.set(list.getName() + ".permission", list.getPermission());
 		if(!(list.getParent() == null))configuration.set(list.getName() + ".parent", list.getParent().getName());
 		
 		if(save){
 			try {
 				configuration.save(configurationFile);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	
 }
