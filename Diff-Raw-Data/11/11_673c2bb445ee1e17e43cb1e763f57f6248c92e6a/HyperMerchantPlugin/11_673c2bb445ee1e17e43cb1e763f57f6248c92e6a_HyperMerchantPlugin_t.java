 package grokswell.hypermerchant;
 
 //import static java.lang.System.out;
 
 import java.util.ArrayList;
 
 import net.citizensnpcs.api.CitizensAPI;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.GameMode;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.scheduler.BukkitRunnable;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerInteractEntityEvent;
 
 
 import regalowl.hyperconomy.EconomyManager;
 import regalowl.hyperconomy.HyperConomy;
 import regalowl.hyperconomy.HyperEconAPI;
 import regalowl.hyperconomy.HyperObjectAPI;
 import regalowl.hyperconomy.HyperAPI;
 import regalowl.hyperconomy.HyperPlayer;
 import regalowl.hyperconomy.PlayerShop;
 
 import grokswell.hypermerchant.HyperMerchantTrait;
 import grokswell.hypermerchant.ShopMenu;
 import grokswell.hypermerchant.Settings;
 
 public class HyperMerchantPlugin extends JavaPlugin implements Listener {
 	HyperEconAPI economyAPI = new HyperEconAPI();
 	HyperObjectAPI hoAPI = new HyperObjectAPI();
 	HyperAPI hyperAPI = new HyperAPI();
 	Uniquifier uniquifier = new Uniquifier();
 	Settings settings;
 	Players playerData;
 	ArrayList<String> customer_cooldowns = new ArrayList<String>();
	Boolean citizens_is_loaded = false;
 	
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String label,String[] args) {
 		
 		//REMOTEMENU 
 		if (cmd.getName().equalsIgnoreCase("remotemenu")) {
 			if (!(sender instanceof Player)) {
 				sender.sendMessage("Only players can use the command "+ChatColor.RED+"/remotemenu");
 				return true;
 			}
 			
 			Player player = (Player) sender;
 			
 			if ((player.getGameMode().compareTo(GameMode.CREATIVE) == 0) && 
 				(!player.hasPermission("creative.hypermerchant"))) {
 					player.sendMessage(ChatColor.YELLOW+"You may not interact with shops while in creative mode.");
 					return true;
 			} 
 			
 			HyperConomy hc = HyperConomy.hc;
 			EconomyManager ecoMan = hc.getEconomyManager();
 			ArrayList<String> shoplist = ecoMan.listShops();
 			
 			if (args.length != 1) {
 				sender.sendMessage(ChatColor.YELLOW+"You must specify one shop name. Example: "+
 									ChatColor.RED+"/remotemenu DonutShop");
 				sender.sendMessage(ChatColor.YELLOW+"Use "+ChatColor.RED+"/remoteshoplist or "+
 									ChatColor.RED+"/rslist "+ChatColor.YELLOW+"for valid shop names.");
 				return true;
 				
 			} else {
 				if (shoplist.contains(args[0])) {
 					new ShopMenu(args[0], 54, this, sender, player, null);
 					return true;
 					
 				} else {
 					sender.sendMessage(ChatColor.YELLOW+"Shop name not recognized. Use "+
 										ChatColor.RED+"/remoteshoplist "+ChatColor.YELLOW+ 
 										"or "+ChatColor.RED+"/rslist "+ChatColor.YELLOW+
 										"for valid shop names. Use exact spelling.");
 					return true;
 				}
 			}
 		}
 		
 		
 		// SHOPMENU 
 		else if (cmd.getName().equalsIgnoreCase("shopmenu")) {
 			if (!(sender instanceof Player)) {
 				sender.sendMessage("Only players can use the command "+ChatColor.RED+"/shopmenu");
 				return true;
 			}
 			
 			Player player = (Player) sender;
 			
 			if ((player.getGameMode().compareTo(GameMode.CREATIVE) == 0) && 
 					(!player.hasPermission("creative.hypermerchant"))) {
 						player.sendMessage(ChatColor.YELLOW+"You may not interact with shops while in creative mode.");
 						return true;
 				} 
 			
 			String name=hyperAPI.getPlayerShop(player);
 			if (name.isEmpty()) {
 				sender.sendMessage(ChatColor.YELLOW+"You must be standing inside " +
 									"of a shop to use the command "+ChatColor.RED+"/shopmenu.");
 				return true;
 				
 			} else {
 				new ShopMenu(name, 54, this, sender, player, null);
 				return true;
 			}
 		}
 		
 		
 		//REMOTESHOPLIST
 		else if (cmd.getName().equalsIgnoreCase("remoteshoplist")) {
 			sender.sendMessage(ChatColor.YELLOW+"Valid shop names to use with command /remotemenu:");
 			sender.sendMessage(hyperAPI.listShops());
 			return true;
 		}
 		
 		
 		//ONDUTY
 		else if (cmd.getName().equalsIgnoreCase("onduty")) {
 			if (!(sender instanceof Player)) {
 				sender.sendMessage("Only players can use the command "+ChatColor.RED+"/remotemenu");
 				return true;
 			}
 			
 			String playerName = ((Player) sender).getName();
 			Boolean onduty = false;
 			onduty = playerData.getPlayerData().getBoolean(playerName+".onduty");
 			
 			if (onduty){
 				playerData.savePlayerData(playerName+".onduty", false);
 				sender.sendMessage(ChatColor.YELLOW+"You are now off duty. Other players cannot click on you to trade with your shop.");
 			} else {
 				playerData.savePlayerData(playerName+".onduty", true);
 				sender.sendMessage(ChatColor.YELLOW+"You are now on duty. Other players may click on you to trade with your shop.");
 			}
 			
 			return true;
 		}
 
 		
 		//HYPERMERCHANT
 		else if (cmd.getName().equalsIgnoreCase("hmerchant")) {
			if (!this.citizens_is_loaded) {
				sender.sendMessage(ChatColor.RED+"Citizens is not loaded. NPCs are unavailable at this time.");
				return true;
			} else if (args.length < 1) {
 				return false;	
 			} else {
 				new HyperMerchantCommand(sender, args, this);
 				return true;
 			}
 			
 		} else {
 			return true;
 		}
 	}
 	
 	
 
 	@Override
 	public void onDisable() {
 		// TODO Auto-generated method stub
 	}
 
 	@Override
 	public void onEnable() {
 		getServer().getPluginManager().registerEvents(this, this);
 		
 		if (Bukkit.getPluginManager().getPlugin("Citizens") != null) {
 			CitizensAPI.getTraitFactory().registerTrait(net.citizensnpcs.api.trait.TraitInfo.create(HyperMerchantTrait.class).withName("hypermerchant"));
			this.citizens_is_loaded = true;
 		} else {
 			this.getLogger().info("Citizens not found. NPC hypermerchants will be disabled.");
 		}
 		
 		//dataFolder = getDataFolder();
 		//if (!dataFolder.isDirectory()) dataFolder.mkdir();
 		settings = new Settings(this);
 		playerData = new Players(this);
 		//settings.load();
 		//YamlStorage config = settings.getConfig();
 
 
 	}
 	
 	class RemoveCustomerCooldown extends BukkitRunnable {
     	String playername;
         public RemoveCustomerCooldown(String plynam) {
         	playername = plynam;
         }
         public void run() {
             // What you want to schedule goes here
             customer_cooldowns.remove(playername);
         }
     }
 	
 	@SuppressWarnings("static-access")
 	@EventHandler
 	public void onRightClick(PlayerInteractEntityEvent event) {
 		//out.println("onRightClick playerinteractentityevent");
 		
 		if(!event.getRightClicked().getType().equals(EntityType.PLAYER)) return;
 		//out.println("entity type = player");
 		Player owner = (Player) event.getRightClicked();
 		//out.println("owner = "+owner.getName());
 		Player player = event.getPlayer();
 		String shopname = "";
 		for (String sn : hyperAPI.getPlayerShopList()){
 			//out.println("try shopname = "+sn+" owner = "+hyperAPI.getPlayerShop(sn).getOwner().getName());
 			
 			if ( hyperAPI.getPlayerShop(sn).getOwner().getName().equals(owner.getName()) ) {
 				shopname = sn;
 				break;
 			}
 		}
 		//out.println("shopname ="+shopname);
 		
 		//return if the player who was clicked does not own a shop.
 		if ("" == shopname) return;
 		//out.println("shopname is good");
 
 		//return if the customer has already clicked to open the menu in the last few seconds
 		if (this.customer_cooldowns.contains(player.getName())) return;
 		
 		//add this player's name to the cooldown list to prevent them from click-spamming and glitching the menu
 		this.customer_cooldowns.add(player.getName());
 		new RemoveCustomerCooldown(player.getName()).runTaskLater(this, 60);
 		
 		//make sure player is not in creative mode without permission for shopping in creative.
 		if ((player.getGameMode().compareTo(GameMode.CREATIVE) == 0) && 
 		   (!player.hasPermission("creative.hypermerchant"))) {
 			
 			player.sendMessage(ChatColor.YELLOW+"You may not interact with merchants while in creative mode.");
 			return;
     	} 
 
 		//if shop owners are required to be in their shops to trade..
 		if (settings.ONDUTY_IN_SHOP_ONLY) {
 			//.. return, if the shop owner is not in shop
 			if (hyperAPI.getPlayerShop(owner) != shopname) return;
 		}
 		
 		PlayerShop shop = hyperAPI.getPlayerShop(shopname);
 		HyperPlayer hp = hoAPI.getHyperPlayer(player.getName());
 		
 		//return if the player who clicked does not have permission to trade with this shop
 		if (!hp.hasBuyPermission(shop)) return;
 		
 		//return if the shop owner has set themselves to off-duty
 		if ( !playerData.getPlayerData().getBoolean(owner.getName()+".onduty") ) return;
         
 		//if nothing has returned to this point, open a shopmenu for the player who clicked.
 		new ShopMenu(shopname, 54, this, player, player, null);
 
 		return;
 
 	}
 }
