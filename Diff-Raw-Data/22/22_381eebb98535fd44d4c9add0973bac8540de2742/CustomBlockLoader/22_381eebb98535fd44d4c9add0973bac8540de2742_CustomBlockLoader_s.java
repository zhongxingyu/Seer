 package net.worldoftomorrow.nala.ni.CustomItems;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 
 import net.worldoftomorrow.nala.ni.CustomBlocks;
 import net.worldoftomorrow.nala.ni.NoItem;
 
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.file.YamlConfiguration;
 
 public class CustomBlockLoader {
 	private final File blockFile;
 	private final YamlConfiguration blockConfig;
 	
 	public CustomBlockLoader(NoItem plugin) {
		this.blockFile = new File(plugin.getDataFolder() + File.pathSeparator + "CustomBlocks.yml");
 		if(!blockFile.exists()) {
 			try {
				blockFile.createNewFile();
				this.writeExample();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		this.blockConfig = YamlConfiguration.loadConfiguration(blockFile);
 	}
 	
     public void load() {
         Set<String> keys = blockConfig.getKeys(false);
         ArrayList<CustomFurnace> furnaces = new ArrayList<CustomFurnace>();
         ArrayList<CustomWorkbench> workbenches = new ArrayList<CustomWorkbench>();
         
         for (String key : keys) {
             ConfigurationSection block = blockConfig.getConfigurationSection(key);
             String type = block.getString("type");
             int id = block.getInt("id");
             short data = (short) block.getInt("data");
             
             if (type.equalsIgnoreCase("furnace")) {
                 List<Short> resultSlots = block.getShortList("resultSlots");
                 List<Short> itemSlots = block.getShortList("itemSlots");
                 boolean usesFuel = block.getBoolean("usesFuel");
                 List<Short> fuelSlots;
                 if(usesFuel) {
                      fuelSlots = block.getShortList("fuelSlots");
                 } else {
                 	fuelSlots = new ArrayList<Short>();
                 }
                 furnaces.add(new CustomFurnace(id, data, CustomType.FURNACE, resultSlots, fuelSlots, itemSlots, key, usesFuel));
                 
             } else if (type.equalsIgnoreCase("workbench")) {
             	List<Short> resultSlots = block.getShortList("resultSlots");
             	List<Short> recipeSlots = block.getShortList("recipeSlots");
             	boolean fakeRecipeItems = block.getBoolean("fakeRecipeItems");
                 workbenches.add(new CustomWorkbench(id, data, CustomType.WORKBENCH, resultSlots, recipeSlots, key, fakeRecipeItems));
             }
         }
         CustomBlocks.setFurnaces(furnaces);
         CustomBlocks.setWorkbenches(workbenches);
     }
     
     public void writeExample() {
     	try {
 			PrintWriter out = new PrintWriter(this.blockFile, "UTF-8");
 			out.println("# For more information on how to set up custom blocks");
 			out.println("# Go here -> http://dev.bukkit.org/server-mods/noitem/pages/configuring-custom-blocks/");
 			out.println();
 			out.println("# How to set up a furnace block #");
 			out.println("# blockname: ");
 			out.println("#     id: ");
 			out.println("#     data: ");
 			out.println("#     type: furnace");
 			out.println("#     itemSlots: ");
 			out.println("#     fuelSlots: ");
 			out.println("#     resultSlots: ");
 			out.println("#     usesFuel: ");
 			out.println();
 			out.println("# How to set up a workbench block #");
 			out.println("#     id: ");
 			out.println("#     data: ");
 			out.println("#     type: workbench");
 			out.println("#     recipeSlots: ");
 			out.println("#     resultSlots: ");
 			out.println("#     fakeRecipeItems: ");
 			out.println();
 			out.println("# Here is an example of each #");
 			out.println();
 			out.println("RMFurnace: ");
 			out.println("    id: 126");
 			out.println("    data: 4");
 			out.println("    type: furnace");
 			out.println("    itemSlots: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13]");
 			out.println("    fuelSlots: [0]");
 			out.println("    resultSlots: [14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26]");
 			out.println("    usesFuel: true");
 			out.println();
 			out.println("BlulectricFurnace: ");
 			out.println("    id: 137");
 			out.println("    data: 4");
 			out.println("    type: furnace");
 			out.println("    itemSlots: [0, 1, 2, 3, 4, 5, 6, 7 , 8]");
 			out.println("    resultSlots: [10]");
 			out.println();
 			out.println("ProjectTable: ");
 			out.println("    id: 137");
 			out.println("    data: 3");
 			out.println("    type: workbench");
 			out.println("    resultSlots: [9]");
 			out.println("    recipeSlots: [0, 1, 2, 3, 4, 5, 6, 7, 8]");
 			out.println();
 			out.println("AutomaticCraftingTableMkIIz: ");
 			out.println("    id: 194");
 			out.println("    data: 1");
 			out.println("    type: workbench");
 			out.println("    recipeSlots: [1, 2, 3, 4, 5, 6, 7, 8, 9]");
 			out.println("    resultSlots: [0]");
 			out.println("    fakeRecipeItems: true");
 			out.println();
 			out.close();
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 		}
     }
 }
