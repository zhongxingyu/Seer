 package dev.mCraft.Coinz;
 
import java.io.File;

 import org.getspout.spoutapi.SpoutManager;
 import org.getspout.spoutapi.player.FileManager;
 
 public class Cache {
 	
 	private Coinz plugin;
 	
 	private FileManager fm;
 	
 	public Cache() {
 		plugin = Coinz.instance;
 		
 		fm = SpoutManager.getFileManager();
 		
 		fm.addToPreLoginCache(plugin, Coinz.class.getResourceAsStream("/Images/PluginImages.png"), "PluginImages.png");
 		plugin.log.info(plugin.tag + " Block designs have been loaded");
 		
 		fm = SpoutManager.getFileManager();
 		fm.addToPreLoginCache(plugin, Coinz.class.getResourceAsStream("/Images/BronzeCoin.png"), "BronzeCoin.png");
 		fm.addToPreLoginCache(plugin, Coinz.class.getResourceAsStream("/Images/CopperCoin.png"), "CopperCoin.png");
 		fm.addToPreLoginCache(plugin, Coinz.class.getResourceAsStream("/Images/GoldCoin.png"), "GoldCoin.png");
 		fm.addToPreLoginCache(plugin, Coinz.class.getResourceAsStream("/Images/GuiScreen.png"), "GuiScreen.png");
 		fm.addToPreLoginCache(plugin, Coinz.class.getResourceAsStream("/Images/HalfBronzeCoin.png"), "HalfBronzeCoin.png");
 		fm.addToPreLoginCache(plugin, Coinz.class.getResourceAsStream("/Images/HalfGoldCoin.png"), "HalfGoldCoin.png");
 		fm.addToPreLoginCache(plugin, Coinz.class.getResourceAsStream("/Images/HalfPlatinumCoin.png"), "HalfPlatinumCoin.png");
 		fm.addToPreLoginCache(plugin, Coinz.class.getResourceAsStream("/Images/HalfSilverCoin.png"), "HalfSilverCoin.png");
 		fm.addToPreLoginCache(plugin, Coinz.class.getResourceAsStream("/Images/PlatinumCoin.png"), "PlatinumCoin.png");
		plugin.saveResource("Images/SilverCoin.png", false);
		
		File path = new File("plugins/Coinz/Images");
		File file = new File(path, "SilverCoin.png");
		
		fm.addToPreLoginCache(plugin, file);
 		plugin.log.info(plugin.tag + " Item images have been loaded");
		

		plugin.log.info(fm.getCache(plugin) + " ");
 	}
 
 }
