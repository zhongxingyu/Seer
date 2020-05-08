 package me.Joeyy.Armor;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.PrintStream;
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.event.player.PlayerListener;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.nijiko.permissions.PermissionHandler;
 import com.nijikokun.bukkit.Permissions.Permissions;
 
 
 public class Armor extends JavaPlugin {
 	ArmorListener ArmorListener = new ArmorListener(this);
 	PlayerListener ArmorMove = new PlayerListener();
 
 	public static PermissionHandler Permissions;
 	public static final Logger log = Logger.getLogger("Minecraft");
 	public final static String premessage = ChatColor.RED + "[Armor] "
 			+ ChatColor.YELLOW;
 
 	public void onEnable() {
 		PluginManager pm = getServer().getPluginManager();
 		pm.registerEvent(Event.Type.ENTITY_DAMAGE, ArmorListener,
 				Event.Priority.Normal, this);
 		this.loadConfigFile();
 		setupPermissions();
 
 		log.info("Armor version " + getDescription().getVersion()
 				+ " is enabled");
 		
 		getCommand("equiparmor").setExecutor(new EquipArmorCommand(this));
 	}
 
 	public void onDisable() {
 		log.info("Armor version " + getDescription().getVersion()
 				+ " is disabled");
 
 	}
 
 	public void loadConfigFile() {
 		// load config file, creating it first if it doesn't exist
 		File configFile = new File(this.getDataFolder(), "config.properties");
 
 		if (!configFile.canRead())
 			try {
 				FileOutputStream myFile;
 				PrintStream myStream;
 				configFile.getParentFile().mkdirs();
 				myFile = new FileOutputStream(configFile);
 				myStream = new PrintStream(myFile);
 				myStream.println("##Control falldamage for each shoe##");
 				myStream.println("\n");
 				myStream.println("Leather-Boots-Fall-Damage-Prevention: 1\n");
 				myStream.println("Iron-Boots-Fall-Damage-Prevention: 2\n");
 				myStream.println("Gold-Boots-Fall-Damage-Prevention: 3\n");
 				myStream.println("Diamond-Boots-Fall-Damage-Prevention: 4\n");
 				myStream.println("\n");
 				myStream.println("##Control lavadamage for each armor set##");
 				myStream.println("\n");
 				myStream.println("Set-Lava-Damage-Using-Ironarmor: 3\n");
 				myStream.println("Set-Lava-Damage-Using-Goldarmor: 2\n");
 				myStream.println("Set-Lava-Damage-Using-Diamondarmor: 1\n");
 				myStream.println("\n");
 				myStream.println("##Hurt on hitting cactus with armor (true/false)##");
 				myStream.println("\n");
 				myStream.println("Hurt-On-Catcus-Contact-Prevention: true\n");
 				myStream.println("\n");
 				myStream.println("##Gold boots giving light (true/false)##");
 				myStream.println("\n");
 				myStream.println("Gold-Boots-Give-Light: true\n");
 				myStream.println("\n");
 				myStream.println("##Control monster damage##");
 				myStream.println("\n");
 				myStream.close();
 			} catch (Exception e) {
				log.warning("Armor: could not create configuration file");
 			}
 	}
 
 		private void setupPermissions() {
 		      Plugin test = this.getServer().getPluginManager().getPlugin("Permissions");
 
		      if (Armor.Permissions == null) {
 		          if (test != null) {
		              Armor.Permissions = ((Permissions)test).getHandler();
 		          } else {
 		              log.info("Permission system not detected, defaulting to OP");
 		          }
 		      }
 		  }
 
 	public boolean isPlayer(CommandSender sender) {
 		if (sender instanceof Player)
 			return true;
 		return false;
 	}
 	
 	public static void givePlayerArmor(Player player, ArmorType set) {
 		if (player == null || !player.isOnline())
 			return;
 		PlayerInventory inv = player.getInventory();
 		
 		inv.setHelmet(new ItemStack(set.helmet, 1, set.helmet.getMaxDurability(), new Byte((byte) 0)));
 		inv.setChestplate(new ItemStack(set.chestplate, 1, set.chestplate.getMaxDurability(), new Byte((byte) 0)));
 		inv.setLeggings(new ItemStack(set.leggings, 1, set.leggings.getMaxDurability(), new Byte((byte) 0)));
 		inv.setBoots(new ItemStack(set.boots, 1, set.boots.getMaxDurability(), new Byte((byte) 0)));
 	}
 }
