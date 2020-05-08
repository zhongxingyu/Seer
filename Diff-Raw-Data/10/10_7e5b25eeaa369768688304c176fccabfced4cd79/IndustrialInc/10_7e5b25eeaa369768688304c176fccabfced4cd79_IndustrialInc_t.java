 package me.furt.industrial;
 
 import java.io.File;
 import java.util.logging.Level;
 
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.getspout.spoutapi.inventory.SpoutItemStack;
 import org.getspout.spoutapi.material.MaterialData;
 
 import com.github.Zarklord1.FurnaceApi.FurnaceListener;
 import com.github.Zarklord1.FurnaceApi.FurnaceRecipes;
 
 public class IndustrialInc extends JavaPlugin {
 	public CustomItems ci;
 	public CustomBlocks cb;
 	public Assets assets;
 
 	@Override
 	public void onEnable() {
 		if (!getDataFolder().exists())
 			getDataFolder().mkdir();
 
 		if (!new File(this.getDataFolder() + "/imageCache").exists())
 			new File(this.getDataFolder() + "/imageCache").mkdir();
 
 		getConfig().addDefault("pixel_size", 16);
 		getConfig().options().copyDefaults(true);
 		saveConfig();
 
 		assets = new Assets(this);
 		assets.addAsset("block_0");
 		assets.addAsset("block_cable");
 		assets.addAsset("block_electric");
 		assets.addAsset("block_generator");
 		assets.addAsset("block_machine");
 		assets.addAsset("item_0");
 		assets.addAsset("GUIMiner");
 		assets.addAsset("copperingot");
 		assets.addAsset("tiningot");
 		assets.addAsset("temperedironingot");
 		assets.addAsset("mixedingot");
 		assets.addAsset("copperwirebare");
 		assets.addAsset("tincell");
		
 		ci = new CustomItems(this);
 		ci.init();
 		cb = new CustomBlocks(this);
 		cb.init();
 
 		getServer().getPluginManager().registerEvents(
 				new FurnaceListener(this), this);
 
 		// Furnace recipes
 		FurnaceRecipes.CustomFurnaceRecipe(new SpoutItemStack(ci.copperIngot),
 				318, cb.copperOre.getCustomId());
 		FurnaceRecipes.CustomFurnaceRecipe(new SpoutItemStack(ci.tinIngot),
 				318, cb.tinOre.getCustomId());
 		FurnaceRecipes.CustomFurnaceRecipe(new SpoutItemStack(
 				ci.temperedIronIngot), MaterialData.ironIngot.getRawId(),
 				MaterialData.ironIngot.getRawData());
 
 		PluginDescriptionFile pdf = this.getDescription();
 		getLogger().log(Level.INFO,
 				"v" + pdf.getVersion() + " is now enabled!");
 	}
 
 	@Override
 	public void onDisable() {
 		assets.freeImageCacheMemory();
 		getLogger().log(Level.INFO, "Disabled");
 	}
 
 }
