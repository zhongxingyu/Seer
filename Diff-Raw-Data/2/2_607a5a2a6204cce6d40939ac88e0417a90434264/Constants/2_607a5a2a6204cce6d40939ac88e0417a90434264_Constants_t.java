 package musician101.controlcreativemode.lib;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import org.bukkit.ChatColor;
 import org.bukkit.GameMode;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 /**
  * Constants for commands, permissions, and chat messages.
  * 
  * @author Musician101
  */
 public class Constants
 {
 	/** Format constants */
 	public static final ChatColor RED = ChatColor.RED;
 	public static final ChatColor GREEN = ChatColor.GREEN;
 	public static final ChatColor PURPLE = ChatColor.LIGHT_PURPLE;
 	
 	/** Various prefixes */
 	public static final String PREFIX_GAMEMODE = PURPLE + "[CCM] ";
 	public static final String PREFIX_PERMISSION = RED + "[CCM] ";
 	public static final String PREFIX_INFO_WARNING = GREEN + "[CCM] ";
 	
 	/** Error strings */
 	public static final String IS_CONSOLE = PREFIX_PERMISSION + "Error: This is a player command.";
 	public static final String NON_EMPTY_INV = PREFIX_INFO_WARNING + "Error: You have items in your inventory.";
 	
 	public static String getMaterialError(String material)
 	{
 		return "Error: " + material.toUpperCase() + " is not a valid material.";
 	}
 	
 	public static String getMobError(String mob)
 	{
 		return "Error: " + mob + " is not a valid mob.";
 	}
 	
 	/** "No Permission" strings */
 	public static final String NO_PERMISSION_ATTACK = PREFIX_PERMISSION + "You do not have permission to attack this mob/player.";
 	public static final String NO_PERMISSION_COMMAND = PREFIX_PERMISSION + "You do not have permission for that command.";
 	public static final String NO_PERMISSION_DROP = PREFIX_PERMISSION + "You do not have permission to drop items while in creative.";
 	public static final String NO_PERMISSION_INVENTORY = PREFIX_PERMISSION + "You do not have permission to access this inventory.";
 	public static final String NO_PERMISSION_PLACE = PREFIX_PERMISSION + "You do not have permission to place this block.";
 	public static final String NO_PERMISSION_THROW = PREFIX_PERMISSION + "You do not have permission to throw this item.";
 	public static final String NO_PERMISSION_SPAWN = PREFIX_PERMISSION + "You do not have permission to spawn this mob.";
 	
 	/** Warning message when a player in Creative mode attacks a mob or player. */
 	public static String getAttackWarning(Player player, Entity entity)
 	{
 		String attackedEntity = "";
 		if (entity instanceof Player)
 			attackedEntity = ((Player) entity).getName();
 		else
 			attackedEntity = "a " + entity.getType().toString();
 		
 		return PREFIX_INFO_WARNING + player.getName() + " attacked " + attackedEntity + " at X: " + player.getLocation().getBlockX() + ", Y: "
 				+ player.getLocation().getBlockY() + ", Z: " + player.getLocation().getBlockZ() + ".";
 	}
 	
 	/** Warning message when a player places a block. */
 	public static String getBlockWarning(Player player, Block block)
 	{
 		return PREFIX_INFO_WARNING + player.getName() + " placed " + block.toString() + " at X: " + block.getLocation().getBlockX() + ", Y: "
 				+ block.getLocation().getBlockY() + ", Z: " + block.getLocation().getBlockZ() + ".";
 	}
 
 	/** Warning message when a player right clicks a block. */
 	public static String getBlockInteractWarning(Player player, String material, Location location)
 	{
 		return PREFIX_INFO_WARNING + player.getName() + " opened a " + material + " at X: " + location.getBlockX() + ", Y: " + location.getBlockY() + ", Z: " + location.getBlockZ() + ".";
 	}
 	
 	/** Warning message when a player uses a water/lava bucket. */
 	public static String getBucketWarning(Player player, Material material, Location location)
 	{
 		return PREFIX_INFO_WARNING + player.getName() + " opened a " + material + " at X: " + location.getBlockX() + ", Y: " + location.getBlockY()
 				+ ", Z: " + location.getBlockZ();
 	}
 	
 	/** Warning message when a TNT Minecart is placed. */
 	public static String getCartWarning(Player player, ItemStack item, Location location)
 	{
 		return PREFIX_INFO_WARNING + player.getName() + " has placed an " + item.getType().toString() + " at X: " + location.getBlockX() + ", Y: " + location.getBlockY() + ", Z: " + location.getBlockZ() + ".";
 	}
 	
 	/** Error message when a player uses a command to change to a gamemode they're already in. */
 	public static String getCommandError(GameMode gm)
 	{
 		return PREFIX_INFO_WARNING + "Error: You are already in " + gm.toString() + ".";
 	}
 	
 	/** Warning message when a player right clicks a mob. */
 	public static String getEntityInteractWarning(Player player, EntityType entity, Location location)
 	{
 		return PREFIX_INFO_WARNING + player.getName() + " has interacted with a " + entity.toString() + " at X: " + location.getBlockX() + ", Y: " + location.getBlockY() + ", Z: " + location.getBlockZ() + ".";
 	}
 	
 	/** Warning message when a player drops an item. */
 	public static String getItemDropWarning(Player player, String material, Location location)
 	{
 		return PREFIX_INFO_WARNING + player.getName() + " has dropped a " + material + " at X: " + Math.round(location.getX()) + ", Y: " + Math.round(location.getY()) + ", Z: " + Math.round(location.getZ()) + ".";
 	}
 	
 	/** Warning message when a player changes gamemodes with items in their inventory. */
 	public static String getItemKeptWarning(Player player)
 	{
 		return PREFIX_INFO_WARNING + player.getName() + " has kept items in their inventory when switching modes.";
 	}
 	
 	/** Notification message when a player uses /creative or /survival. */
 	public static String getModeMsg(GameMode gm)
 	{
 		return PREFIX_GAMEMODE + "You are now in " + gm.toString() + ".";
 	}
 	
 	/** Warning message when a player changes gamemode using the plugin's commands. */
 	public static String getModeWarning(Player player, GameMode gm)
 	{
 		return PREFIX_INFO_WARNING + player.getName() + " is now in " + gm.toString() + ".";
 	}
 	
 	/** Warning message when a player right clicks a throwable item. */
 	public static String getThrownItemWarning(Player player, ItemStack item, Location location)
 	{
 		return PREFIX_INFO_WARNING + player.getName() + " threw a " + item.getType().toString() + " at X: " + Math.round(location.getX()) + ", Y: " + Math.round(location.getY()) + ", Z: " + Math.round(location.getZ()) + ".";
 	}
 	
 	/**  Warning message when a player uses a spawn egg. */
 	public static String getSpawnWarning(Player player, short data, Location location)
 	{
 		String mob = "";
 		if (data == 50)
 			mob = "a CREEPER";
 		else if (data == 51)
 			mob = "a SKELETON";
 		else if (data  == 52)
 			mob = "a SPIDER";
 		else if (data == 54)
 			mob = "a ZOMBIE";
 		else if (data == 55)
 			mob = "a SLIME";
 		else if (data == 56)
 			mob = "a GHAST";
 		else if (data == 57)
 			mob = "a ZOMBIE PIGMAN";
 		else if (data == 58)
 			mob = "an ENDERMAN";
 		else if (data == 59)
 			mob = "a CAVE SPIDER";
 		else if (data == 60)
 			mob = "a SILVERFISH";
 		else if (data == 61)
 			mob = "a BLAZE";
 		else if (data == 62)
 			mob = "a MAGMA CUBE";
 		else if (data == 65)
 			mob = "a BAT";
 		else if (data == 66)
 			mob = "a WITCH";
 		else if (data == 90)
 			mob = "a PIG";
 		else if (data == 91)
 			mob = "a SHEEP";
 		else if (data == 92)
 			mob = "a COW";
 		else if (data == 93)
 			mob = "a CHICKEN";
 		else if (data == 94)
 			mob = "a SQUID";
 		else if (data == 95)
 			mob = "a WOLF";
 		else if (data == 96)
 			mob = "a MOOSHROOM";
 		else if (data == 97)
 			mob = "a SNOW GOLEM";
 		else if (data == 98)
 			mob = "an OCELOT";
 		else if (data == 99)
 			mob = "a IRON GOLEM";
 		else if (data == 120)
 			mob = "a VILLAGER";
 		else
 			mob = "an UNIDENTIFIED MOB";
 		
 		return PREFIX_INFO_WARNING + player.getName() + " spawned " + mob + " at X: " + location.getBlockX() + ", Y: " + location.getBlockY() + ", Z: " + location.getBlockZ() + ".";
 	}
 	
 	/** Command constants */
 	public static final String CCM = "ccm";
 	public static final String CREATIVE = "creative";
 	public static final String SURVIVAL = "survival";
 	
 	/** Permission constants */
 	public static final String BASE_PERMISSION = "ccm.";
 	public static final String PERMISSION_ALLOW = "allow.";
 	public static final String PERMISSION_ALLOW_ATTACK = BASE_PERMISSION + PERMISSION_ALLOW + "attack";
 	public static final String PERMISSION_ALLOW_BLOCK = BASE_PERMISSION + PERMISSION_ALLOW + "block";
 	public static final String PERMISSION_ALLOW_DROP = BASE_PERMISSION + PERMISSION_ALLOW + "drop";
 	public static final String PERMISSION_ALLOW_OPEN_CHESTS = BASE_PERMISSION + PERMISSION_ALLOW + "openchests";
 	public static final String PERMISSION_ALLOW_SPAWN = BASE_PERMISSION + PERMISSION_ALLOW + "spawn";
 	public static final String PERMISSION_ALLOW_THROW = BASE_PERMISSION + PERMISSION_ALLOW + "throw";
 	public static final String PERMISSION_KEEP_ITEMS = BASE_PERMISSION + PERMISSION_ALLOW + "keepitems";
 	public static final String PERMISSION_SPY = BASE_PERMISSION + "spy";
 	public static final String PERMISSION_USE = BASE_PERMISSION + "use";
 	
 	/** Other */
 	public static final List<String> MOB_LIST = new ArrayList<String>(Arrays.asList("bat", "blaze", "cavespider", "chicken", "cow", 
			"creeper", "enderman", "ghast", "irongolem", "magmacube", "mooshroom", "ocelot", "pig", "skeleton", "sheep", "silverfish", "slime",
 			"snowgolem", "spider", "squid", "villager", "witch", "wolf", "zombie", "zombiepigman"));
 }
