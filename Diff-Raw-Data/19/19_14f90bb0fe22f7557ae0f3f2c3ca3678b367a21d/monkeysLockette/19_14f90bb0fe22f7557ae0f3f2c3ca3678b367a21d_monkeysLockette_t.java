 package monkeysLockette;
 
 import java.util.ArrayList;
 import java.util.logging.Logger;
 
 import net.milkbowl.vault.economy.Economy;
 import net.milkbowl.vault.permission.Permission;
 
 import org.bukkit.Bukkit;
 import org.bukkit.GameMode;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.block.BlockState;
 import org.bukkit.block.Sign;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Minecart;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockCanBuildEvent;
 import org.bukkit.event.block.BlockPhysicsEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.block.BlockRedstoneEvent;
 import org.bukkit.event.block.SignChangeEvent;
 import org.bukkit.event.entity.EntityInteractEvent;
 import org.bukkit.event.inventory.InventoryMoveItemEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.inventory.InventoryHolder;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
 
 public class monkeysLockette extends JavaPlugin implements Listener {
 	public static Plugin plug;
 	public static boolean Debug = false;
 	public static Logger Log;
 	public static Economy VaultEcon;
 	public static Permission VaultPerm;
 	public static boolean VaultEnabled = false;
 	public static Material[] ProtectedBlocks = new Material[] {Material.LEVER, Material.WOOD_BUTTON,
 		                     Material.STONE_BUTTON, Material.WOOD_PLATE, Material.STONE_PLATE, Material.FENCE_GATE,
 		                     Material.TRAP_DOOR, Material.FURNACE, Material.BURNING_FURNACE, Material.DISPENSER, Material.DROPPER,
		                     Material.JUKEBOX, Material.NOTE_BLOCK, Material.HOPPER };
 	public static Material[] ProtectedChests = new Material[] {Material.CHEST, Material.TRAPPED_CHEST};
 	public static Material[] ProtectedSmallDoors = new Material[] {Material.TRAP_DOOR, Material.FENCE_GATE};
 	public static Material[] ProtectedDoors = new Material[] {Material.WOODEN_DOOR, Material.IRON_DOOR_BLOCK};
 	public static Material[] ProtectedNoOutput = new Material[] {Material.WOOD_PLATE, Material.STONE_PLATE};
 	public static BlockFace[][] ProtectedBlockLocations = new BlockFace[][]
 			{
 		new BlockFace[] {BlockFace.NORTH}, new BlockFace[] {BlockFace.WEST},
 		new BlockFace[] {BlockFace.SOUTH}, new BlockFace[] {BlockFace.EAST}, new BlockFace[] {BlockFace.DOWN},
 		new BlockFace[] {BlockFace.UP, BlockFace.SOUTH},
 		new BlockFace[] {BlockFace.UP, BlockFace.EAST},
 		new BlockFace[] {BlockFace.UP, BlockFace.NORTH},
 		new BlockFace[] {BlockFace.UP, BlockFace.WEST},
 		new BlockFace[] {BlockFace.UP, BlockFace.UP},
 		new BlockFace[] {BlockFace.UP}, new BlockFace[] {BlockFace.DOWN, BlockFace.SOUTH},
 		new BlockFace[] {BlockFace.UP, BlockFace.UP, BlockFace.SOUTH},
 		new BlockFace[] {BlockFace.UP, BlockFace.UP, BlockFace.NORTH},
 		new BlockFace[] {BlockFace.DOWN, BlockFace.NORTH},
 		new BlockFace[] {BlockFace.DOWN, BlockFace.SOUTH, BlockFace.EAST},
 		new BlockFace[] {BlockFace.DOWN, BlockFace.SOUTH, BlockFace.WEST},
 		new BlockFace[] {BlockFace.SOUTH, BlockFace.WEST},
 		new BlockFace[] {BlockFace.SOUTH, BlockFace.EAST},
 		new BlockFace[] {BlockFace.UP, BlockFace.SOUTH, BlockFace.WEST},
 		new BlockFace[] {BlockFace.UP, BlockFace.SOUTH, BlockFace.EAST},
 		new BlockFace[] {BlockFace.UP, BlockFace.UP, BlockFace.SOUTH, BlockFace.WEST},
 		new BlockFace[] {BlockFace.UP, BlockFace.UP, BlockFace.SOUTH, BlockFace.EAST},
 		new BlockFace[] {BlockFace.DOWN, BlockFace.NORTH, BlockFace.EAST},
 		new BlockFace[] {BlockFace.DOWN, BlockFace.NORTH, BlockFace.WEST},
 		new BlockFace[] {BlockFace.NORTH, BlockFace.WEST},
 		new BlockFace[] {BlockFace.NORTH, BlockFace.EAST},
 		new BlockFace[] {BlockFace.UP, BlockFace.NORTH, BlockFace.EAST},
 		new BlockFace[] {BlockFace.UP, BlockFace.NORTH, BlockFace.WEST},
 		new BlockFace[] {BlockFace.UP, BlockFace.UP, BlockFace.NORTH, BlockFace.EAST},
 		new BlockFace[] {BlockFace.UP, BlockFace.UP, BlockFace.NORTH, BlockFace.WEST},
 		new BlockFace[] {BlockFace.WEST, BlockFace.WEST},
 		new BlockFace[] {BlockFace.DOWN, BlockFace.WEST, BlockFace.WEST},
 		new BlockFace[] {BlockFace.UP, BlockFace.WEST, BlockFace.WEST},
 		new BlockFace[] {BlockFace.UP, BlockFace.UP, BlockFace.WEST, BlockFace.WEST},
 		new BlockFace[] {BlockFace.DOWN, BlockFace.EAST, BlockFace.EAST},
 		new BlockFace[] {BlockFace.EAST, BlockFace.EAST},
 		new BlockFace[] {BlockFace.UP, BlockFace.EAST, BlockFace.EAST},
 		new BlockFace[] {BlockFace.UP, BlockFace.UP, BlockFace.EAST, BlockFace.EAST},
 		new BlockFace[] {BlockFace.DOWN, BlockFace.WEST},
 		new BlockFace[] {BlockFace.DOWN, BlockFace.EAST},
 		new BlockFace[] {BlockFace.UP, BlockFace.UP, BlockFace.WEST},
 		new BlockFace[] {BlockFace.UP, BlockFace.UP, BlockFace.EAST},
 		new BlockFace[] {BlockFace.DOWN, BlockFace.NORTH, BlockFace.NORTH},
 		new BlockFace[] {BlockFace.NORTH, BlockFace.NORTH},
 		new BlockFace[] {BlockFace.UP, BlockFace.NORTH, BlockFace.NORTH},
 		new BlockFace[] {BlockFace.UP, BlockFace.UP, BlockFace.NORTH, BlockFace.NORTH},
 		new BlockFace[] {BlockFace.DOWN, BlockFace.SOUTH, BlockFace.SOUTH},
 		new BlockFace[] {BlockFace.UP, BlockFace.SOUTH, BlockFace.SOUTH},
 		new BlockFace[] {BlockFace.UP, BlockFace.UP, BlockFace.SOUTH, BlockFace.SOUTH},
 		new BlockFace[] {BlockFace.SOUTH, BlockFace.SOUTH, BlockFace.DOWN},
 		new BlockFace[] {BlockFace.SOUTH, BlockFace.SOUTH, BlockFace.UP},
 		new BlockFace[] {BlockFace.SOUTH, BlockFace.SOUTH, BlockFace.UP, BlockFace.UP},
 		new BlockFace[] {BlockFace.SOUTH, BlockFace.SOUTH},
 		new BlockFace[] {BlockFace.NORTH, BlockFace.NORTH, BlockFace.DOWN},
 		new BlockFace[] {BlockFace.NORTH, BlockFace.NORTH, BlockFace.UP},
 		new BlockFace[] {BlockFace.NORTH, BlockFace.NORTH, BlockFace.UP, BlockFace.UP},
 		new BlockFace[] {BlockFace.NORTH, BlockFace.NORTH}
 			};
 	public static BlockFace[][] ProtectedChestLocations = new BlockFace[][]
 			{
 				new BlockFace[] { BlockFace.NORTH}, new BlockFace[] {BlockFace.WEST}, new BlockFace[] {BlockFace.SOUTH},
 				new BlockFace[] {BlockFace.UP}, new BlockFace[] {BlockFace.DOWN},
 				new BlockFace[] {BlockFace.EAST, BlockFace.NORTH},
 				new BlockFace[] {BlockFace.EAST, BlockFace.EAST},
 				new BlockFace[] {BlockFace.EAST, BlockFace.SOUTH},
 				new BlockFace[] {BlockFace.EAST, BlockFace.UP},
 				new BlockFace[] {BlockFace.EAST, BlockFace.DOWN}
 			};
 	public static BlockFace[][] ProtectedDoorLocations = new BlockFace[][]
 			{
 				new BlockFace[] {BlockFace.NORTH}, new BlockFace[] {BlockFace.WEST},
 				new BlockFace[] {BlockFace.SOUTH}, new BlockFace[] {BlockFace.EAST}, new BlockFace[] {BlockFace.DOWN},
 				new BlockFace[] {BlockFace.UP, BlockFace.SOUTH},
 				new BlockFace[] {BlockFace.UP, BlockFace.EAST},
 				new BlockFace[] {BlockFace.UP, BlockFace.NORTH},
 				new BlockFace[] {BlockFace.UP, BlockFace.WEST},
 				new BlockFace[] {BlockFace.UP, BlockFace.UP},
 				new BlockFace[] {BlockFace.UP}, new BlockFace[] {BlockFace.DOWN, BlockFace.SOUTH},
 				new BlockFace[] {BlockFace.UP, BlockFace.UP, BlockFace.SOUTH},
 				new BlockFace[] {BlockFace.UP, BlockFace.UP, BlockFace.NORTH},
 				new BlockFace[] {BlockFace.DOWN, BlockFace.NORTH},
 				new BlockFace[] {BlockFace.DOWN, BlockFace.SOUTH, BlockFace.EAST},
 				new BlockFace[] {BlockFace.DOWN, BlockFace.SOUTH, BlockFace.WEST},
 				new BlockFace[] {BlockFace.SOUTH, BlockFace.WEST},
 				new BlockFace[] {BlockFace.SOUTH, BlockFace.EAST},
 				new BlockFace[] {BlockFace.UP, BlockFace.SOUTH, BlockFace.WEST},
 				new BlockFace[] {BlockFace.UP, BlockFace.SOUTH, BlockFace.EAST},
 				new BlockFace[] {BlockFace.UP, BlockFace.UP, BlockFace.SOUTH, BlockFace.WEST},
 				new BlockFace[] {BlockFace.UP, BlockFace.UP, BlockFace.SOUTH, BlockFace.EAST},
 				new BlockFace[] {BlockFace.DOWN, BlockFace.NORTH, BlockFace.EAST},
 				new BlockFace[] {BlockFace.DOWN, BlockFace.NORTH, BlockFace.WEST},
 				new BlockFace[] {BlockFace.NORTH, BlockFace.WEST},
 				new BlockFace[] {BlockFace.NORTH, BlockFace.EAST},
 				new BlockFace[] {BlockFace.UP, BlockFace.NORTH, BlockFace.EAST},
 				new BlockFace[] {BlockFace.UP, BlockFace.NORTH, BlockFace.WEST},
 				new BlockFace[] {BlockFace.UP, BlockFace.UP, BlockFace.NORTH, BlockFace.EAST},
 				new BlockFace[] {BlockFace.UP, BlockFace.UP, BlockFace.NORTH, BlockFace.WEST},
 				new BlockFace[] {BlockFace.WEST, BlockFace.WEST},
 				new BlockFace[] {BlockFace.DOWN, BlockFace.WEST, BlockFace.WEST},
 				new BlockFace[] {BlockFace.UP, BlockFace.WEST, BlockFace.WEST},
 				new BlockFace[] {BlockFace.UP, BlockFace.UP, BlockFace.WEST, BlockFace.WEST},
 				new BlockFace[] {BlockFace.DOWN, BlockFace.EAST, BlockFace.EAST},
 				new BlockFace[] {BlockFace.EAST, BlockFace.EAST},
 				new BlockFace[] {BlockFace.UP, BlockFace.EAST, BlockFace.EAST},
 				new BlockFace[] {BlockFace.UP, BlockFace.UP, BlockFace.EAST, BlockFace.EAST},
 				new BlockFace[] {BlockFace.DOWN, BlockFace.WEST},
 				new BlockFace[] {BlockFace.DOWN, BlockFace.EAST},
 				new BlockFace[] {BlockFace.UP, BlockFace.UP, BlockFace.WEST},
 				new BlockFace[] {BlockFace.UP, BlockFace.UP, BlockFace.EAST},
 				new BlockFace[] {BlockFace.DOWN, BlockFace.NORTH, BlockFace.NORTH},
 				new BlockFace[] {BlockFace.NORTH, BlockFace.NORTH},
 				new BlockFace[] {BlockFace.UP, BlockFace.NORTH, BlockFace.NORTH},
 				new BlockFace[] {BlockFace.UP, BlockFace.UP, BlockFace.NORTH, BlockFace.NORTH},
 				new BlockFace[] {BlockFace.DOWN, BlockFace.SOUTH, BlockFace.SOUTH},
 				new BlockFace[] {BlockFace.UP, BlockFace.SOUTH, BlockFace.SOUTH},
 				new BlockFace[] {BlockFace.UP, BlockFace.UP, BlockFace.SOUTH, BlockFace.SOUTH},
 				new BlockFace[] {BlockFace.SOUTH, BlockFace.SOUTH, BlockFace.DOWN},
 				new BlockFace[] {BlockFace.SOUTH, BlockFace.SOUTH, BlockFace.UP},
 				new BlockFace[] {BlockFace.SOUTH, BlockFace.SOUTH, BlockFace.UP, BlockFace.UP},
 				new BlockFace[] {BlockFace.SOUTH, BlockFace.SOUTH},
 				new BlockFace[] {BlockFace.NORTH, BlockFace.NORTH, BlockFace.DOWN},
 				new BlockFace[] {BlockFace.NORTH, BlockFace.NORTH, BlockFace.UP},
 				new BlockFace[] {BlockFace.NORTH, BlockFace.NORTH, BlockFace.UP, BlockFace.UP},
 				new BlockFace[] {BlockFace.NORTH, BlockFace.NORTH}
 			};
 	public static BlockFace[][] ProtectedDoubleDoorLocations = new BlockFace[][]
 			{
 				new BlockFace[] {BlockFace.SOUTH}, new BlockFace[] {BlockFace.WEST}, new BlockFace[] {BlockFace.NORTH},
 				new BlockFace[] {BlockFace.EAST, BlockFace.SOUTH}, new BlockFace[] {BlockFace.EAST, BlockFace.EAST},
 				new BlockFace[] {BlockFace.EAST, BlockFace.NORTH}, new BlockFace[] {BlockFace.UP, BlockFace.SOUTH},
 				new BlockFace[] {BlockFace.UP, BlockFace.EAST, BlockFace.SOUTH}, new BlockFace[] {BlockFace.UP, BlockFace.WEST},
 				new BlockFace[] {BlockFace.UP, BlockFace.NORTH}, new BlockFace[] {BlockFace.UP, BlockFace.EAST, BlockFace.NORTH},
 				new BlockFace[] {BlockFace.UP, BlockFace.EAST, BlockFace.EAST}, new BlockFace[] {BlockFace.UP, BlockFace.UP},
 				new BlockFace[] {BlockFace.UP, BlockFace.UP, BlockFace.EAST},
 				new BlockFace[] {BlockFace.SOUTH, BlockFace.UP, BlockFace.UP, BlockFace.WEST},
 				new BlockFace[] {BlockFace.SOUTH, BlockFace.UP, BlockFace.UP},
 				new BlockFace[] {BlockFace.SOUTH, BlockFace.UP, BlockFace.WEST}, new BlockFace[] {BlockFace.SOUTH, BlockFace.WEST},
 				new BlockFace[] {BlockFace.SOUTH, BlockFace.DOWN, BlockFace.WEST}, new BlockFace[] {BlockFace.SOUTH, BlockFace.DOWN},
 				new BlockFace[] {BlockFace.SOUTH, BlockFace.EAST, BlockFace.DOWN},
 				new BlockFace[] {BlockFace.SOUTH, BlockFace.EAST, BlockFace.EAST, BlockFace.DOWN},
 				new BlockFace[] {BlockFace.SOUTH, BlockFace.EAST, BlockFace.EAST},
 				new BlockFace[] {BlockFace.SOUTH, BlockFace.UP, BlockFace.EAST, BlockFace.EAST},
 				new BlockFace[] {BlockFace.SOUTH, BlockFace.UP, BlockFace.UP, BlockFace.EAST},
 				new BlockFace[] {BlockFace.SOUTH, BlockFace.UP, BlockFace.UP, BlockFace.EAST, BlockFace.EAST},
 				new BlockFace[] {BlockFace.DOWN, BlockFace.WEST, BlockFace.WEST}, new BlockFace[] {BlockFace.WEST, BlockFace.WEST},
 				new BlockFace[] {BlockFace.UP, BlockFace.WEST, BlockFace.WEST},
 				new BlockFace[] {BlockFace.UP, BlockFace.UP, BlockFace.WEST, BlockFace.WEST},
 				new BlockFace[] {BlockFace.DOWN, BlockFace.EAST, BlockFace.EAST, BlockFace.EAST},
 				new BlockFace[] {BlockFace.EAST, BlockFace.EAST, BlockFace.EAST},
 				new BlockFace[] {BlockFace.UP, BlockFace.EAST, BlockFace.EAST, BlockFace.EAST},
 				new BlockFace[] {BlockFace.UP, BlockFace.UP, BlockFace.EAST, BlockFace.EAST, BlockFace.EAST},
 				new BlockFace[] {BlockFace.UP, BlockFace.UP, BlockFace.WEST},
 				new BlockFace[] {BlockFace.UP, BlockFace.UP, BlockFace.EAST, BlockFace.EAST},
 				new BlockFace[] {BlockFace.DOWN, BlockFace.WEST},
 				new BlockFace[] {BlockFace.DOWN, BlockFace.EAST},
 				new BlockFace[] {BlockFace.DOWN, BlockFace.EAST, BlockFace.EAST},
 				new BlockFace[] {BlockFace.DOWN, BlockFace.NORTH},
 				new BlockFace[] {BlockFace.WEST, BlockFace.NORTH},
 				new BlockFace[] {BlockFace.EAST, BlockFace.NORTH},
 				new BlockFace[] {BlockFace.DOWN, BlockFace.EAST, BlockFace.NORTH},
 				new BlockFace[] {BlockFace.NORTH, BlockFace.DOWN, BlockFace.EAST, BlockFace.EAST},
 				new BlockFace[] {BlockFace.NORTH, BlockFace.DOWN, BlockFace.WEST},
 				new BlockFace[] {BlockFace.NORTH, BlockFace.EAST, BlockFace.EAST},
 				new BlockFace[] {BlockFace.NORTH, BlockFace.UP, BlockFace.WEST},
 				new BlockFace[] {BlockFace.NORTH, BlockFace.UP, BlockFace.EAST, BlockFace.EAST},
 				new BlockFace[] {BlockFace.NORTH, BlockFace.UP, BlockFace.UP, BlockFace.EAST, BlockFace.EAST},
 				new BlockFace[] {BlockFace.NORTH, BlockFace.UP, BlockFace.UP, BlockFace.WEST},
 				new BlockFace[] {BlockFace.NORTH, BlockFace.UP, BlockFace.UP},
 				new BlockFace[] {BlockFace.NORTH, BlockFace.EAST, BlockFace.UP, BlockFace.UP}
 			};
 	public static int CloseDoorsAfter = 7;
 	public static ArrayList<Block> ToClose;
 	public static String SignColor = "b";
 	public static String WarnColor = "c";
 	public static String CommonColor = "6";
 	public static String RefuseMessage = "This block is locked.";
 	public static String NopermMessage = "You aren't allowed to do that.";
 	public static String EmptyMessage = "There's nothing to protect here.";
 	public static String NosignMessage = "That's not a sign.";
 	public static String NodoorMessage = "That's not a door.";
 	public static String BadlineMessage = "Only lines 2, 3, and 4 are permitted.";
 	public static String NotthatlineMessage = "You can't edit that line.";
 	public static String NotlockedMessage = "This isn't locked.";
 	public static String NobuildMessage = "You are not allowed to build here.";
 	public static String FirstlockableMessage = "Place a sign headed [Lock] next to that to lock it.";
 	public static String PermissionBase = "monkeyslockette.";
 	public static String PermissionAdmin = "admin";
 	public static String PermissionCreate = "create";
 	public static void DebugInfo(String message)
 	{
 		if (Debug)
 		{
 			Log.info(message);
 		}
 	}
 	public static DoorThread dt;
 	WorldGuardPlugin wg;
 	public void onEnable()
 	{
 		Log = getLogger();
 		Log.info("Initializing...");
 		plug = this;
 		getServer().getPluginManager().registerEvents(this, this);
 		VaultEnabled = linkVault();
 		if (VaultEnabled)
 		{
 			Log.info("Vault loaded! Found " + String.valueOf(VaultPerm.getGroups().length) + " groups!");
 		}
 		else
 		{
 			Log.info("Error loading Vault!");
 		}
 		wg = (WorldGuardPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldGuard");
 		dt = new DoorThread();
 		dt.runTaskTimer(this, 20, 20);
 		ToClose = new ArrayList<Block>();
 		Log.info("Ready!");
 	}
     private boolean linkVault() {
         if (getServer().getPluginManager().getPlugin("Vault") == null) {
             return false;
         }
         RegisteredServiceProvider<Economy> rspe = getServer().getServicesManager().getRegistration(Economy.class);
         if (rspe == null) {
             return false;
         }
         VaultEcon = rspe.getProvider();
         RegisteredServiceProvider<Permission> rspp = getServer().getServicesManager().getRegistration(Permission.class);
         if (rspp == null) {
             return false;
         }
         VaultPerm = rspp.getProvider();
         return true;
     }
 	public void onDisable()
 	{
 		Log.info("Shutting down...");
 		dt.cancel();
 	}
 	public static int AT_INVALID = 0;
 	public static int AT_NOTALLOWED = 1;
 	public static int AT_USER = 2;
 	public static int AT_OWNER = 3;
 	public static int AT_OP = 4;
 	public static boolean isalockmoresign(String title)
 	{
 		String ttitle = title.replace(SignColor, "");
 		if (ttitle.equalsIgnoreCase("[more users]"))
 		{
 			return true;
 		}
 		else
 		{
 			return false;
 		}
 	}
 	public static boolean isalocksign(String title)
 	{
 		String ttitle = title.replace(SignColor, "");
 		if (ttitle.equalsIgnoreCase("[lock]") || ttitle.equalsIgnoreCase("[rslock]") || ttitle.equalsIgnoreCase("[private]"))
 		{
 			return true;
 		}
 		else
 		{
 			return false;
 		}
 	}
 	public static boolean isallowed(Player player, String line, String name)
 	{
 		if (line.equalsIgnoreCase(name))
 		{
 			return true;
 		}
 		else if (line.equalsIgnoreCase("[everyone]"))
 		{
 			return true;
 		}
 		else if (line.toLowerCase().startsWith("g:"))
 		{
 			if (line.length() < 3)
 			{
 				return false;
 			}
 			if (!VaultEnabled)
 			{
 				return false;
 			}
 			String groupname = line.substring(2);
 			return VaultPerm.playerInGroup(player, groupname);
 		}
 		else
 		{
 			return false;
 		}
 	}
 	public static int checkallowed(Sign trysign, Player player)
 	{
 		return checkallowed(trysign, player, false);
 	}
 	public static int checkallowed(Sign trysign, Player player, boolean hopper)
 	{
 		if (hopper)
 		{
 			for (int i = 1; i < 4; i++)
 			{
 				if (trysign.getLine(i).toLowerCase().startsWith("[hopper"))
 				{
 					return AT_USER;
 				}
 			}
 			return AT_NOTALLOWED;
 		}
 		if (player.hasPermission(PermissionBase + PermissionAdmin))
 		{
 			return AT_OP;
 		}
 		String title = trysign.getLine(0);
 		String name = player.getName();
 		if (name.length() > 15)
 		{
 			name = name.substring(0, 15);
 			DebugInfo(player.getName() + " shortened  to " + name);
 		}
 		if (isalocksign(title))
 		{
 			DebugInfo(name + " comparing against " + trysign.getLine(1) + "/" + trysign.getLine(2) + "/" + trysign.getLine(3));
 			if (trysign.getLine(1).equalsIgnoreCase(name))
 			{
 				return AT_OWNER;
 			}
 			else if (isallowed(player, trysign.getLine(2), name))
 			{
 				return AT_USER;
 			}
 			else if (isallowed(player, trysign.getLine(3), name))
 			{
 				return AT_USER;
 			}
 			else
 			{
 				return AT_NOTALLOWED;
 			}
 		}
 		else if (isalockmoresign(title))
 		{
 			DebugInfo(name + " comparing against " + trysign.getLine(1) + "/" + trysign.getLine(2) + "/" + trysign.getLine(3));
 			if (isallowed(player, trysign.getLine(1), name))
 			{
 				return AT_USER;
 			}
 			else if (isallowed(player, trysign.getLine(2), name))
 			{
 				return AT_USER;
 			}
 			else if (isallowed(player, trysign.getLine(3), name))
 			{
 				return AT_USER;
 			}
 			else
 			{
 				return AT_NOTALLOWED;
 			}
 		}
 		else
 		{
 			return AT_INVALID;
 		}
 	}
 	public static boolean BlockProtectable(Block b)
 	{
 		Material mat = b.getType();
 		if (Util.Contains(ProtectedBlocks, mat) ||
 			Util.Contains(ProtectedChests, mat) ||
 			Util.Contains(ProtectedDoors, mat) ||
 			Util.Contains(ProtectedDoors, mat) ||
 			Util.Contains(ProtectedSmallDoors, mat))
 		{
 			return true;
 		}
 		else
 		{
 			return false;
 		}
 	}
	public static Block whatwouldthisprotect(Location loc)
 	{
 		if (loc.getY() > 250 || loc.getY() < 5)
 		{
 			return null;
 		}
 		Block b = loc.getBlock();
 		Block tb = b.getRelative(BlockFace.NORTH);
 		if (BlockProtectable(tb))
 		{
 			return tb;
 		}
 		tb = b.getRelative(BlockFace.WEST);
 		if (BlockProtectable(tb))
 		{
 			return tb;
 		}
 		tb = b.getRelative(BlockFace.SOUTH);
 		if (BlockProtectable(tb))
 		{
 			return tb;
 		}
 		tb = b.getRelative(BlockFace.EAST);
 		if (BlockProtectable(tb))
 		{
 			return tb;
 		}
 		Block ftb = b.getRelative(BlockFace.DOWN);
 		if (BlockProtectable(ftb))
 		{
 			return ftb;
 		}
 		tb = ftb.getRelative(BlockFace.NORTH);
 		if (BlockProtectable(tb))
 		{
 			return tb;
 		}
 		tb = ftb.getRelative(BlockFace.WEST);
 		if (BlockProtectable(tb))
 		{
 			return tb;
 		}
 		tb = ftb.getRelative(BlockFace.SOUTH);
 		if (BlockProtectable(tb))
 		{
 			return tb;
 		}
 		tb = ftb.getRelative(BlockFace.EAST);
 		if (BlockProtectable(tb))
 		{
 			return tb;
 		}
 		for (int x = -2;x < 3;x++)
 		{
 			for (int y = -1;y < 2;y++)
 			{
 				for (int z = -2;z < 3;z++)
 				{
 					tb = loc.clone().add(x, y, z).getBlock();
 					if (BlockProtectable(tb))
 					{
 						return tb;
 					}
 				}
 			}
 		}
 		return null;
 	}
 	public static int CheckLocked(Block ab, Player player)
 	{
 		return CheckLocked(ab, player, false);
 	}
 	public static int CheckLocked(Block ab, Player player, boolean hopper)
 	{
 		Block b = ab;
 		Material mat = b.getType();
 		Location bloc = b.getLocation();
 		if (bloc.getY() > 250 || bloc.getY() < 5)
 		{
 			return AT_INVALID;
 		}
 		BlockFace[][] blockstocheck;
 		Block connected = null;
 		boolean needsflip = false;
 		if (Util.Contains(ProtectedBlocks, mat))
 		{
 			blockstocheck = ProtectedBlockLocations;
 		}
 		else if (Util.Contains(ProtectedChests, mat))
 		{
 			blockstocheck = ProtectedChestLocations;
 			connected = b.getRelative(BlockFace.NORTH);
 			if (connected.getType() == mat)
 			{
 				needsflip = true;
 			}
 			else
 			{
 				connected = ab.getRelative(BlockFace.SOUTH);
 				if (connected.getType() == mat)
 				{
 					b = connected;
 					connected = ab;
 					needsflip = true;
 				}
 				else
 				{
 					connected = ab.getRelative(BlockFace.WEST);
 					if (connected.getType() == mat)
 					{
 						b = connected;
 						connected = ab;
 					}
 					else
 					{
 						connected = ab.getRelative(BlockFace.EAST);
 						if (connected.getType() != mat)
 						{
 							blockstocheck = ProtectedBlockLocations;
 							connected = null;
 						}
 					}
 				}
 			}
 		}
 		else if (Util.Contains(ProtectedSmallDoors, mat))
 		{
 			blockstocheck = ProtectedDoorLocations;
 		}
 		else if (Util.Contains(ProtectedDoors, mat))
 		{
 			blockstocheck = ProtectedDoubleDoorLocations;
 			connected = ab.getRelative(BlockFace.NORTH);
 			if (connected.getType() == mat)
 			{
 				needsflip = true;
 			}
 			else
 			{
 				connected = ab.getRelative(BlockFace.SOUTH);
 				if (connected.getType() == mat)
 				{
 					b = connected;
 					connected = ab;
 					needsflip = true;
 				}
 				else
 				{
 					connected = ab.getRelative(BlockFace.WEST);
 					if (connected.getType() == mat)
 					{
 						b = connected;
 						connected = ab;
 					}
 					else
 					{
 						connected = ab.getRelative(BlockFace.EAST);
 						if (connected.getType() != mat)
 						{
 							blockstocheck = ProtectedDoorLocations;
 							connected = null;
 						}
 					}
 				}
 			}
 			if (b.getRelative(BlockFace.DOWN).getType() == mat)
 			{
 				b = b.getRelative(BlockFace.DOWN);
 				if (connected != null)
 				{
 					connected = connected.getRelative(BlockFace.DOWN);
 				}
 			}
 		}
 		else
 		{
 			return AT_INVALID;
 		}
 		Sign thesign = null;
 		ArrayList<Sign> moresigns = new ArrayList<Sign>();
 		for (BlockFace[] list: blockstocheck)
 		{
 			Block tb = b;
 			for (BlockFace face: list)
 			{
 				if (needsflip)
 				{
 					if (face == BlockFace.NORTH)
 					{
 						tb = tb.getRelative(BlockFace.EAST);
 					}
 					else if (face == BlockFace.EAST)
 					{
 						tb = tb.getRelative(BlockFace.NORTH);
 					}
 					else if (face == BlockFace.SOUTH)
 					{
 						tb = tb.getRelative(BlockFace.WEST);
 					}
 					else if (face == BlockFace.WEST)
 					{
 						tb = tb.getRelative(BlockFace.SOUTH);
 					}
 					else
 					{
 						tb = tb.getRelative(face);
 					}
 				}
 				else
 				{
 					tb = tb.getRelative(face);
 				}
 			}
 			if (tb.getType() == Material.WALL_SIGN || tb.getType() == Material.SIGN_POST)
 			{
 				Sign trysign = (Sign)tb.getState();
 				String title = trysign.getLine(0);
 				Block potent = tb.getRelative(Util.DataToFace(tb.getData()));
 				if (BlockProtectable(potent))
 				{
 					if (Util.Contains(ProtectedDoors, potent.getType()))
 					{
 						if (potent.getRelative(BlockFace.DOWN).getType() == potent.getType())
 						{
 							potent = potent.getRelative(BlockFace.DOWN);
 						}
 						if (potent.getRelative(BlockFace.WEST).getType() == potent.getType())
 						{
 							potent = potent.getRelative(BlockFace.WEST);
 						}
 						if (potent.getRelative(BlockFace.SOUTH).getType() == potent.getType())
 						{
 							potent = potent.getRelative(BlockFace.SOUTH);
 						}
 					}
 					else if (Util.Contains(ProtectedChests, potent.getType()))
 					{
 						if (potent.getRelative(BlockFace.WEST).getType() == potent.getType())
 						{
 							potent = potent.getRelative(BlockFace.WEST);
 						}
 						if (potent.getRelative(BlockFace.SOUTH).getType() == potent.getType())
 						{
 							potent = potent.getRelative(BlockFace.SOUTH);
 						}
 					}
 					if (!potent.equals(b))
 					{
 						continue;
 					}
 				}
 				if (isalocksign(title))
 				{
 					thesign = trysign;
 				}
 				else if (isalockmoresign(title))
 				{
 					moresigns.add(trysign);
 				}
 			}
 		}
 		if (thesign == null)
 		{
 			DebugInfo("No Sign!");
 			return AT_INVALID;
 		}
 		if (player == null)
 		{
 			int allowed = checkallowed(thesign, null, true);
 			DebugInfo("Allowed? " + String.valueOf(allowed));
 			if (allowed == AT_OP || allowed == AT_OWNER || allowed == AT_USER)
 			{
 				return allowed;
 			}
 			for (Sign trysign: moresigns)
 			{
 				allowed = checkallowed(trysign, null, true);
 				DebugInfo("More Allowed? " + String.valueOf(allowed));
 				if (allowed == AT_USER)
 				{
 					return AT_USER;
 				}
 			}
 			return AT_NOTALLOWED;
 		}
 		if (player.hasPermission(PermissionBase + PermissionAdmin))
 		{
 			return AT_OP;
 		}
 		int allowed = checkallowed(thesign, player);
 		DebugInfo("Allowed? " + String.valueOf(allowed));
 		if (allowed == AT_OP || allowed == AT_OWNER || allowed == AT_USER)
 		{
 			return allowed;
 		}
 		for (Sign trysign: moresigns)
 		{
 			allowed = checkallowed(trysign, player);
 			DebugInfo("More Allowed? " + String.valueOf(allowed));
 			if (allowed == AT_USER)
 			{
 				return AT_USER;
 			}
 		}
 		return AT_NOTALLOWED;
 	}
 	@EventHandler
 	public void onInventoryMove(InventoryMoveItemEvent event)
 	{
 		InventoryHolder IH = event.getSource().getHolder();
 		if (IH instanceof BlockState && (event.getDestination().getHolder() instanceof BlockState || event.getDestination().getHolder() instanceof Minecart))
 		{
 			Block b = ((BlockState) IH).getBlock();
 			DebugInfo("Potential hopper at " + b.getLocation().toString());
 			int allowed = CheckLocked(b, null, true);
 			DebugInfo("allowed = " + String.valueOf(allowed));
 			if (allowed == AT_NOTALLOWED)
 			{
 				DebugInfo("DENIED!");
 				event.setCancelled(true);
 			}
 		}
 	}
 	@EventHandler
 	public void onBlockRedstone(BlockRedstoneEvent event)
 	{
 		Block block = event.getBlock();
 		Material mat = block.getType();
 		DebugInfo("Redstone on " + block.getType().name() + " at " + block.getLocation().toString());
 		if (Util.Contains(ProtectedDoors, mat) || Util.Contains(ProtectedSmallDoors, mat))
 		{
 			if (CheckLocked(block, null) != AT_INVALID)
 			{
 				DebugInfo("RedNOPE");
 				event.setNewCurrent(0);
 			}
 		}
 	}
 	@EventHandler (priority = EventPriority.HIGHEST)
 	public void onPlayerInteract(PlayerInteractEvent event)
 	{
 		if (event.isCancelled())
 		{
 			return;
 		}
 		if (!event.hasBlock())
 		{
 			DebugInfo("Bad interact?");
 			return;
 		}
 		Player player = event.getPlayer();
 		Block block = event.getClickedBlock();
 		DebugInfo("Interact with a " + block.getType().name());
 		int allowed = CheckLocked(block, player);
 		DebugInfo("Allowed returned " + String.valueOf(allowed) + ".");
 		Material mat = block.getType();
 		if (allowed == AT_NOTALLOWED)
 		{
 			event.setCancelled(true);
 			if (!Util.Contains(ProtectedNoOutput, block.getType()))
 			{
 				player.sendMessage(WarnColor + RefuseMessage);
 				Log.info(player.getName() + " was denied a block interaction at " + block.getLocation().toString() + ".");
 			}
 			return;
 		}
 		else
 		{
 			ItemStack held = player.getItemInHand();
 			if (event.getAction() == Action.RIGHT_CLICK_BLOCK)
 			{
 			if (held != null && held.getType() == Material.SIGN && player.hasPermission(PermissionBase + PermissionCreate))
 			{
 				if (Util.Contains(ProtectedBlocks, mat) ||
 						Util.Contains(ProtectedDoors, mat) ||
 						Util.Contains(ProtectedSmallDoors, mat) ||
 						Util.Contains(ProtectedChests, mat))
 				{
 					if (allowed == AT_INVALID || allowed == AT_OWNER || allowed == AT_OP)
 					{
 						BlockFace bf = event.getBlockFace();
 						if (bf == BlockFace.NORTH || bf == BlockFace.WEST || bf == BlockFace.SOUTH || bf == BlockFace.EAST)
 						{
 							Block signspot = block.getRelative(bf);
 							if (signspot.getType() == Material.AIR)
 							{
 								BlockCanBuildEvent evt = new BlockCanBuildEvent(signspot, Material.WALL_SIGN.getId(), true);
 								Bukkit.getServer().getPluginManager().callEvent(evt);
 								if (!evt.isBuildable() || (wg != null && !wg.getGlobalRegionManager().canBuild(player, signspot)))
 								{
 									DebugInfo(player.getName() + " tried to add sign at " + player.getLocation().toString() + "- build was denied by a build protection plugin.");
 									player.sendMessage(WarnColor + NobuildMessage);
 									return;
 								}
 								signspot.setType(Material.WALL_SIGN);
 								signspot.setData(Util.FaceToData(bf.getOppositeFace()));
 								Sign newsign = (Sign)signspot.getState();
 								if (allowed == AT_INVALID)
 								{
 									newsign.setLine(0, SignColor + "[Lock]");
 									newsign.setLine(1, player.getName());
 									Log.info(player.getName() + " made a lock sign at " + newsign.getLocation().toString() + ".");
 								}
 								else if (allowed == AT_OWNER || allowed == AT_OP)
 								{
 									newsign.setLine(0, SignColor + "[More Users]");
 									newsign.setLine(1, "[Everyone]");
 									Log.info(player.getName() + " made a moreusers sign at " + newsign.getLocation().toString() + ".");
 								}
 								if (player.getGameMode() != GameMode.CREATIVE)
 								{
 									if (held.getAmount() > 1)
 									{
 										held.setAmount(held.getAmount() - 1);
 									}
 									else
 									{
 										held.setType(Material.AIR);
 									}
 									player.setItemInHand(held);
 								}
 								newsign.update();
 								event.setCancelled(true);
 								return;
 							}
 						}
 					}
 				}
 			}
 			}
 		}
 		if (event.getAction() == Action.RIGHT_CLICK_BLOCK && (allowed == AT_USER || allowed == AT_OWNER || allowed == AT_OP))
 		{
 			if (Util.Contains(ProtectedDoors, mat) || Util.Contains(ProtectedSmallDoors, mat))
 			{
 				if (block.getType() == Material.IRON_DOOR_BLOCK)
 				{
 					Util.FlipDoor(block, DoorThread.toggledoor(block));
 				}
 				else
 				{
 					DoorThread.toggledoor(block);
 				}
 				Block con = Util.findconnected(block);
 				if (con != null)
 				{
 					Util.FlipDoor(con, DoorThread.toggledoor(con));
 				}
 			}
 		}
 	}
 	@EventHandler
 	public void onSignChange(SignChangeEvent event)
 	{
 		if (event.isCancelled())
 		{
 			return;
 		}
 		Player player = event.getPlayer();
 		Block block = event.getBlock();
 		if (player == null)
 		{
 			return;
 		}
 		String title = event.getLine(0);
 		if (title.length() > 13)
 		{
 			title = title.substring(0, 13);
 		}
 		boolean isalockmore = isalockmoresign(title);
 		if (isalocksign(title) || isalockmore)
 		{
 			if (!player.hasPermission(PermissionBase + PermissionCreate))
 			{
 				Log.info(player.getName() + " was denied a sign creation at " + event.getBlock().getLocation().toString() + ".");
 				event.setLine(0, "[?]");
 				player.sendMessage(WarnColor + NopermMessage);
 				return;
 			}
 			if (!isalockmore && ((!player.hasPermission(PermissionBase + PermissionAdmin) || event.getLine(1).length() == 0)))
 			{
 				event.setLine(1, player.getName());
 			}
 			event.setLine(0, SignColor + (isalockmore ? "[More Users]": "[Lock]"));
 			Block targ = whatwouldthisprotect(block.getLocation());
 			if (targ == null)
 			{
 				Log.info(player.getName() + " tried  to protect nothing at " + event.getBlock().getLocation().toString() + ".");
 				event.setLine(0, "[?]");
 				player.sendMessage(WarnColor + EmptyMessage);
 				return;
 			}
 			int allowed = CheckLocked(targ, player);
 			if (allowed == AT_USER || allowed == AT_OWNER)
 			{
 				if (!isalockmore)
 				{
 					Log.info(player.getName() + " tried  to protect something twice at " + event.getBlock().getLocation().toString() + ".");
 					event.setLine(0, "[?]");
 					player.sendMessage(WarnColor + RefuseMessage);
 					return;
 				}
 			}
 			if (isalockmore)
 			{
 				Log.info(player.getName() + " made a lock sign at " + event.getBlock().getLocation().toString() + ".");
 			}
 			else
 			{
 				Log.info(player.getName() + " made a moreusers sign at " + event.getBlock().getLocation().toString() + ".");
 			}
 			/*if (block.getType() == Material.SIGN_POST)
 			{
 				block.setType(Material.WALL_SIGN);
 				if (BlockProtectable(block.getRelative(BlockFace.NORTH)))
 				{
 					block.setData(Util.FaceToData(BlockFace.NORTH));
 				}
 				else if (BlockProtectable(block.getRelative(BlockFace.WEST)))
 				{
 					block.setData(Util.FaceToData(BlockFace.WEST));
 				}
 				else if (BlockProtectable(block.getRelative(BlockFace.SOUTH)))
 				{
 					block.setData(Util.FaceToData(BlockFace.SOUTH));
 				}
 				else
 				{
 					block.setData(Util.FaceToData(BlockFace.EAST));
 				}
 				Sign tomake = (Sign)block.getState();
 				tomake.setLine(0, event.getLine(0));
 				tomake.setLine(1, event.getLine(1));
 				tomake.setLine(2, event.getLine(2));
 				tomake.setLine(3, event.getLine(3));
 				tomake.update();
 			}*/
 		}
 	}
 	@EventHandler
 	public void onBlockPhysics(BlockPhysicsEvent event)
 	{
 		if (event.isCancelled())
 		{
 			return;
 		}
 		Block block = event.getBlock();
 		if (block.getType() == Material.TRAP_DOOR)
 		{
 			if (CheckLocked(block, null) != AT_INVALID)
 			{
 				DebugInfo("Protected trapdoor from those annoying laws of physics!");
 				event.setCancelled(true);
 				return;
 			}
 		}
 		if (block.getType() == Material.WALL_SIGN || block.getType() == Material.SIGN_POST)
 		{
 			Sign trysign = (Sign)block.getState();
 			String title = trysign.getLine(0);
 			if (isalocksign(title) || isalockmoresign(title))
 			{
 				if (!(BlockProtectable(block.getRelative(BlockFace.NORTH)) ||
 					BlockProtectable(block.getRelative(BlockFace.WEST)) ||
 					BlockProtectable(block.getRelative(BlockFace.SOUTH)) ||
 					BlockProtectable(block.getRelative(BlockFace.EAST))))
 				{
 					return;
 				}
 				DebugInfo("Protected sign from those annoying laws of physics!");
 				event.setCancelled(true);
 				return;
 			}
 		}
 	}
 	@EventHandler
 	public void onBlockBreak(BlockBreakEvent event)
 	{
 		if (event.isCancelled())
 		{
 			return;
 		}
 		Player player = event.getPlayer();
 		Block block = event.getBlock();
 		Material mat = block.getType();
 		if (mat == Material.WALL_SIGN || mat == Material.SIGN_POST)
 		{
 			Sign trysign = (Sign)block.getState();
 			String title = trysign.getLine(0);
 			boolean islock = isalocksign(title);
 			boolean islockmore = isalockmoresign(title);
 			if (player == null && (islock || islockmore))
 			{
 				event.setCancelled(true);
 				DebugInfo("Protected sign that was broken by dark magic!");
 				return;
 			}
 			if (islock)
 			{
 				int allowed = checkallowed(trysign, player);
 				if (allowed == AT_OWNER || allowed == AT_OP)
 				{
 					Log.info(player.getName() + " broke a sign at " + block.getLocation().toString() + ".");
 				}
 				else
 				{
 					player.sendMessage(WarnColor + RefuseMessage);
 					event.setCancelled(true);
 					Log.info(player.getName() + " was denied a sign break at " + block.getLocation().toString() + ".");
 					return;
 				}
 			}
 			else if (islockmore)
 			{
 				Block huh = whatwouldthisprotect(trysign.getLocation());
 				if (huh == null)
 				{
 					Log.info("Smashed error at " + block.getLocation().toString());
 					block.breakNaturally();
 					return;
 				}
 				int allowed = CheckLocked(huh, player);
 				if (allowed == AT_USER || allowed == AT_NOTALLOWED)
 				{
 					player.sendMessage(WarnColor + RefuseMessage);
 					event.setCancelled(true);
 					Log.info(player.getName() + " was denied a sign break at " + block.getLocation().toString() + ".");
 					return;
 				}
 			}
 		}
 		else if (Util.Contains(ProtectedDoors, mat) || Util.Contains(ProtectedSmallDoors, mat))
 		{
 			DoorThread.removedoor(block);
 		}
 	}
 	@EventHandler
 	public void onBlockPlace(BlockPlaceEvent event)
 	{
 		Player player = event.getPlayer();
 		Block block = event.getBlock();
 		Material mat = block.getType();
 		int allowed = CheckLocked(block, player);
 		if (allowed == AT_NOTALLOWED || allowed == AT_USER)
 		{
 			player.sendMessage(WarnColor + RefuseMessage);
 			Log.info(player.getName() + " was denied a block build at " + block.getLocation().toString() + ".");
 			event.setCancelled(true);
 		}
 		if (Util.Contains(ProtectedBlocks, mat) || Util.Contains(ProtectedChests, mat) ||
 				Util.Contains(ProtectedDoors, mat) || Util.Contains(ProtectedSmallDoors, mat))
 		{
 			String hasshown = Util.getMetadata(player, "monkeylock_shown");
 			if (hasshown == null || !hasshown.equalsIgnoreCase("true"))
 			{
 				if (player.hasPermission(PermissionBase + PermissionCreate))
 				{
 					DebugInfo("Showing help to " + player.getName());
 					player.sendMessage(CommonColor + FirstlockableMessage);
 					Util.setMetadata(player, "monkeylock_shown", "true");
 				}
 			}
 		}
 	}
 	@EventHandler
 	public void onEntityInteract(EntityInteractEvent event)
 	{
 		if (event.isCancelled())
 		{
 			return;
 		}
 		Block block = event.getBlock();
 		int allowed = CheckLocked(block, null);
 		if (allowed != AT_INVALID)
 		{
 			event.setCancelled(true);
 			DebugInfo("Preventing entity interact at " + block.getLocation().toString());
 			return;
 		}
 	}
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
 	{
 		Player player = null;
 		if (sender instanceof Player)
 		{
 			player = (Player) sender;
 		}
 		String command = cmd.getName().toLowerCase();
 		if (player == null)
 		{
 			if (command.equalsIgnoreCase("mlock") || command.equalsIgnoreCase("lock") || command.equalsIgnoreCase("lockette"))
 			{
 				if (args.length < 1)
 				{
 					sender.sendMessage("/" + command + " debug");
 					return true;
 				}
 				if (args[0].equalsIgnoreCase("debug"))
 				{
 					if (Debug)
 					{
 						sender.sendMessage("Stopped debugging.");
 						Debug = false;
 					}
 					else
 					{
 						sender.sendMessage("Debugging.");
 						Debug = true;
 					}
 				}
 				return true;
 			}
 			return false;
 		}
 		if (command.equalsIgnoreCase("mlock") || command.equalsIgnoreCase("lock") || command.equalsIgnoreCase("lockette"))
 		{
 			if (args.length < 1)
 			{
 				sender.sendMessage(CommonColor + "/" + command + " line <#> <message> - Change a line on the sign you're targetting");
 				sender.sendMessage(CommonColor + "/" + command + " fix - flip a locked door you're targetting");
 				return true;
 			}
 			else if (args[0].equalsIgnoreCase("line"))
 			{
 				if (args.length < 2)
 				{
 					sender.sendMessage(CommonColor + "/" + command + " line <#> [text] - Change a line on the sign you're targetting");
 				}
 				Block target = player.getTargetBlock(null, 10);
 				if (target == null || (target.getType() != Material.WALL_SIGN && target.getType() != Material.SIGN_POST))
 				{
 					sender.sendMessage(WarnColor + NosignMessage);
 				}
 				else
 				{
 					int line = 0;
 					try
 					{
 						line = Integer.parseInt(args[1]);
 					}
 					catch (Exception ex)
 					{
 						DebugInfo(ex.toString());
 					}
 					if (line < 2 || line > 4)
 					{
 						sender.sendMessage(WarnColor + BadlineMessage);
 						return true;
 					}
 					Sign trysign = (Sign)target.getState();
					Block b = whatwouldthisprotect(trysign.getLocation());
					if (b == null)
					{
						sender.sendMessage(WarnColor + NotlockedMessage);
						return true;
					}
					int allowed = CheckLocked(b, player);
 					if (allowed == AT_INVALID)
 					{
 						sender.sendMessage(WarnColor + NotlockedMessage);
 						return true;
 					}
 					else if (allowed == AT_USER || allowed == AT_NOTALLOWED)
 					{
 						sender.sendMessage(WarnColor + RefuseMessage);
 						return true;
 					}
 					String title = trysign.getLine(0);
 					boolean isalock = isalocksign(title);
 					if (isalock)
 					{
 						if (line == 2)
 						{
 							if (!player.hasPermission(PermissionBase + PermissionAdmin))
 							{
 								sender.sendMessage(WarnColor + NotthatlineMessage);
 								return true;
 							}
 						}
 					}
 					if (isalock || isalockmoresign(title))
 					{
 						String message = (args.length > 2?args[2] + (args.length > 3?" " + args[3]:""):"");
 						if (message.length() > 15)
 						{
 							message = message.substring(0, 15);
 						}
 						trysign.setLine(line - 1, message);
 						trysign.update();
 					}
 					else
 					{
 						sender.sendMessage(WarnColor + NosignMessage);
 					}
 				}
 			}
 			else if (args[0].equalsIgnoreCase("fix"))
 			{
 				Block target = player.getTargetBlock(null, 10);
 				if (target == null || !(Util.Contains(ProtectedDoors, target.getType()) || Util.Contains(ProtectedSmallDoors, target.getType())))
 				{
 					sender.sendMessage(WarnColor + NodoorMessage);
 				}
 				else
 				{
 					int allowed = CheckLocked(target, player);
 					if (allowed == AT_INVALID)
 					{
 						sender.sendMessage(WarnColor + NotlockedMessage);
 						return true;
 					}
 					else if (allowed == AT_USER || allowed == AT_NOTALLOWED)
 					{
 						sender.sendMessage(WarnColor + RefuseMessage);
 						return true;
 					}
 					Util.FlipDoor(target, true);
 				}
 			}
 			else
 			{
 				sender.sendMessage(CommonColor + "/" + command + " line <#> <message> - Change a line on the sign you're targetting");
 				sender.sendMessage(CommonColor + "/" + command + " fix - flip a locked door you're targetting");
 			}
 			return true;
 		}
 		else
 		{
 			return false;
 		}
 	}
 }
