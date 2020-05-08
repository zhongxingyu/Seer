 package qs.swornshop;
 
 import java.util.HashMap;
 import java.util.logging.Logger;
 
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.block.Sign;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class Main extends JavaPlugin implements Listener {
 	private static final int SIGN = 63;
 	
 	public static final String[] shopHelp = {
 		"E------------A Shop Help E------------",
 		"B/shop &3[&Bhelp3(Bh3)] 7-F show help with shops",
 		"B/shop create3(Bc3) 3<owner> 7-F create a new shop",
 		"B/shop pending3(Bp3) 7-F view pending shop requests",
 		"B/shop buy3(Bb3) 3<item> <quantity> 7-F buy an item from the selected shop",
 		"B/shop sell3(Bs3) 3<item> <quantity> 7-F request to sell an item to the selected shop"
 	};
 	
 	protected HashMap<Location, Shop> shops = new HashMap<Location, Shop>();
 	protected HashMap<Player, Shop> selectedShops = new HashMap<Player, Shop>();
 	protected Logger log;
 	
 	public Main() {}
 
 	@Override
 	public void onEnable() {
         getServer().getPluginManager().registerEvents(this, this);
 		log = this.getLogger();
 	}
 	@Override
 	public void onDisable() {}
 	
 	@Override
 	public boolean onCommand(CommandSender sender, Command command,
 			String label, String[] args) {
 		if (command.getName().equalsIgnoreCase("shop")) {
 			if (args.length == 0) {
 				sender.sendMessage(shopHelp);
 				return true;
 			}
			if (args[0].equalsIgnoreCase("create") || 
 					args[0].equalsIgnoreCase("c") &&
					args.length > 1 &&
 					sender instanceof Player) {
 				
 				Player pl = (Player) sender;
 				Location loc = pl.getLocation();
 				World world = pl.getWorld();
 				Block b = world.getBlockAt(loc);
				
 				byte angle = (byte) ((((int) loc.getYaw() + 225) / 90) << 2);
 				b.setTypeIdAndData(SIGN, angle, false);
 				
 				Sign sign = (Sign) b.getState();
 				String owner = args[1];
 				sign.setLine(1, (owner.length() < 13 ? owner : owner.substring(0, 12) + '') + "'s");
 				sign.setLine(2, "shop");
 				sign.update();
 				
 				Shop shop = new Shop();
 				shop.owner = owner;
 				shops.put(b.getLocation(), shop);
 				
 			} else {
 				sender.sendMessage(shopHelp);
 			}
 			return true;
 		}
 		return false;
 	}
 	
	@EventHandler
 	public PlayerInteractEvent.Result onPlayerInteract(PlayerInteractEvent event) {
 		Block b = event.getClickedBlock();
 		if (b.getTypeId() == SIGN) {
 			Shop shop = shops.get(b.getLocation());
 			if (shop != null) {
 				Player pl = event.getPlayer();
 				boolean isOwner = shop.owner.equals(pl.getName());
 				selectedShops.put(pl, shop);
 				pl.sendMessage(new String[] {
 					isOwner ? "FWelcome to your shop." :
 							String.format("FWelcome to B%sF's shop.", shop.owner),
 					"7For help with shops, type 3/shop help7."
 				});
 				return PlayerInteractEvent.Result.DENY;
 			}
 		}
 		return PlayerInteractEvent.Result.DEFAULT;
 	}
 }
