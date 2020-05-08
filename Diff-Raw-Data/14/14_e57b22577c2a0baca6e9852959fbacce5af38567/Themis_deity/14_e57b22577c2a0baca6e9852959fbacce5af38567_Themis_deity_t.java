 package com.legit2.Demigods.Deities.Titans;
 
 import java.util.ArrayList;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerInteractEvent;
 
 import com.legit2.Demigods.Libraries.ReflectCommand;
 import com.legit2.Demigods.Utilities.DAbilityUtil;
 import com.legit2.Demigods.Utilities.DCharUtil;
 import com.legit2.Demigods.Utilities.DDataUtil;
 import com.legit2.Demigods.Utilities.DPlayerUtil;
 import com.legit2.Demigods.Utilities.DMiscUtil;
 
 public class Themis_deity implements Listener
 {	
 	// Create required universal deity variables
 	private static final String DEITYNAME = "Themis";
 	private static final String DEITYALLIANCE = "Titan";
 	private static final ChatColor DEITYCOLOR = ChatColor.GRAY;
 
 	/*
 	 *  Set deity-specific ability variable(s).
 	 */
 	// "/swap" Command:
 	private static String SWAP_NAME = "Swap"; // Sets the name of this command
 	private static long SWAP_TIME; // Creates the variable for later use
 	private static final int SWAP_COST = 310; // Cost to run command in "favor"
 	private static final int SWAP_DELAY = 2400; // In milliseconds
 
 	// "/congregate" Command:
 	@SuppressWarnings("unused")
 	private static String ULTIMATE_NAME = "Congregate";
 	private static long ULTIMATE_TIME; // Creates the variable for later use
 	private static final int ULTIMATE_COST = 6000; // Cost to run command in "favor"
 	private static final int ULTIMATE_COOLDOWN_MAX = 1200; // In seconds
 	private static final int ULTIMATE_COOLDOWN_MIN = 500; // In seconds
 
 	public ArrayList<Material> getClaimItems()
 	{
 		ArrayList<Material> claimItems = new ArrayList<Material>();
 		
 		// Add new items in this format: claimItems.add(Material.NAME_OF_MATERIAL);
 		claimItems.add(Material.COMPASS);
 		claimItems.add(Material.PAPER);
 		
 		return claimItems;
 	}
 
 	public ArrayList<String> getInfo(Player player)
 	{		
 		ArrayList<String> toReturn = new ArrayList<String>();
 		
 		if(DMiscUtil.canUseDeitySilent(player, DEITYNAME))
 		{
 			toReturn.add(" "); //TODO
 			toReturn.add(ChatColor.AQUA + " Demigods > " + ChatColor.RESET + DEITYCOLOR + DEITYNAME);
 			toReturn.add(ChatColor.RESET + "-----------------------------------------------------");
 			toReturn.add(ChatColor.YELLOW + " Active:");
 			toReturn.add(ChatColor.GRAY + " -> " + ChatColor.GREEN + "/swap" + ChatColor.WHITE + " - Trade places with your enemy.");
 			toReturn.add(" ");
 			toReturn.add(ChatColor.YELLOW + " Passive:");
 			toReturn.add(ChatColor.GRAY + " -> " + ChatColor.WHITE + "None.");
 			toReturn.add(" ");
 			toReturn.add(ChatColor.YELLOW + " Ultimate:");
 			toReturn.add(ChatColor.GRAY + " -> " + ChatColor.GREEN + "/congregate" + ChatColor.WHITE + " - Bring everyone together for party time!");
 			toReturn.add(" ");
 			toReturn.add(ChatColor.YELLOW + " You are a follower of " + DEITYNAME + "!");
 			toReturn.add(" ");
 
 			return toReturn;
 		}
 		else
 		{						
 			toReturn.add(" "); //TODO
 			toReturn.add(ChatColor.AQUA + " Demigods > " + ChatColor.RESET + DEITYCOLOR + DEITYNAME);
 			toReturn.add(ChatColor.RESET + "-----------------------------------------------------");
 			toReturn.add(ChatColor.YELLOW + " Active:");
 			toReturn.add(ChatColor.GRAY + " -> " + ChatColor.GREEN + "/swap" + ChatColor.WHITE + " - Trade places with your enemy.");
 			toReturn.add(" ");
 			toReturn.add(ChatColor.YELLOW + " Passive:");
 			toReturn.add(ChatColor.GRAY + " -> " + ChatColor.WHITE + "None.");
 			toReturn.add(" ");
 			toReturn.add(ChatColor.YELLOW + " Ultimate:");
 			toReturn.add(ChatColor.GRAY + " -> " + ChatColor.GREEN + "/congregate" + ChatColor.WHITE + " - Bring everyone together for party time!");
 			toReturn.add(" ");
 			toReturn.add(ChatColor.YELLOW + " Claim Items:");
 			for(Material item : getClaimItems())
 			{
 				toReturn.add(ChatColor.GRAY + " -> " + ChatColor.WHITE + item.name());
 			}
 			toReturn.add(" ");
 
 			return toReturn;
 		}
 	}
 
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public static void onPlayerInteract(PlayerInteractEvent interactEvent)
 	{
 		// Set variables
 		Player player = interactEvent.getPlayer();
 
 		if(!DMiscUtil.canUseDeitySilent(player, DEITYNAME)) return;
 
 		if(DCharUtil.isEnabledAbility(player, SWAP_NAME) || ((player.getItemInHand() != null) && (player.getItemInHand().getType() == DCharUtil.getBind(player, SWAP_NAME))))
 		{
 			if(!DCharUtil.isCooledDown(player, SWAP_NAME, SWAP_TIME, false)) return;
 
 			swap(player);
 		}
 	}
 
 	/* ------------------
 	 *  Command Handlers
 	 * ------------------
 	 *
 	 *  Command: "/swap"
 	 */
 	@ReflectCommand.Command(name = "swap", sender = ReflectCommand.Sender.PLAYER, permission = "demigods." + DEITYALLIANCE + "." + DEITYNAME)
 	public static void swapCommand(Player player, String arg1)
 	{
 		if(!DMiscUtil.canUseDeity(player, DEITYNAME)) return;
 
 		if(arg1.equalsIgnoreCase("bind"))
 		{		
 			// Bind item
 			DCharUtil.setBound(player, SWAP_NAME, player.getItemInHand().getType());
 		}
 		else
 		{
 			if(DCharUtil.isEnabledAbility(player, SWAP_NAME))
 			{
 				DCharUtil.disableAbility(player, SWAP_NAME);
 				player.sendMessage(ChatColor.YELLOW + SWAP_NAME + " is no longer active.");
 			}
 			else
 			{
 				DCharUtil.enableAbility(player, SWAP_NAME);
 				player.sendMessage(ChatColor.YELLOW + SWAP_NAME + " is now active.");
 			}
 		}
 	}
 
 	// The actual ability command
 	public static void swap(Player player)
 	{
 		// Define variables
 		int charID = DPlayerUtil.getCurrentChar(player);
 		Location between = player.getLocation();
 		LivingEntity target = DMiscUtil.autoTarget(player);
 		
 		if(!DAbilityUtil.doAbilityPreProcess(player, target, SWAP_COST)) return;
 		SWAP_TIME = System.currentTimeMillis() + SWAP_DELAY;
 		DCharUtil.subtractFavor(charID, SWAP_COST);
 
 		player.teleport(target.getLocation());
 		target.teleport(between);
 	}
 	
 	/*
 	 *  Command: "/assemble"
 	 */
 	@ReflectCommand.Command(name = "assemble", sender = ReflectCommand.Sender.PLAYER, permission = "demigods." + DEITYALLIANCE + "." + DEITYNAME)
 	public static void assembleCommand(Player player, String arg1)
 	{
 		int charID = DPlayerUtil.getCurrentChar(player);
 		
 		if(!DCharUtil.isImmortal(player)) return;
 		if(!DDataUtil.hasCharData(charID, "temp_themis_congregate")) return;
 		for(Player pl : player.getWorld().getPlayers())
 		{
 			if(DCharUtil.isImmortal(pl) && DDataUtil.hasCharData(DPlayerUtil.getCurrentChar(pl), "temp_themis_congregate_call"))
 			{
 				DDataUtil.removeCharData(charID, "temp_themis_congregate");
 				player.teleport(pl.getLocation());
 				return;
 			}
 		}
 		player.sendMessage(ChatColor.YELLOW + "Unable to reach the congregation's location.");
 	}
 
 	/*
 	 *  Command: "/congregate"
 	 */
 	@ReflectCommand.Command(name = "congregate", sender = ReflectCommand.Sender.PLAYER, permission = "demigods." + DEITYALLIANCE + "." + DEITYNAME + ".ultimate")
 	public static void ultimateCommand(Player player)
 	{
 		// Set variables
 		int charID = DPlayerUtil.getCurrentChar(player);
 		
 		// Check the player for DEITYNAME
 		if(!DCharUtil.hasDeity(charID, DEITYNAME)) return;
 
 		// Check if the ultimate has cooled down or not
 		if(System.currentTimeMillis() < ULTIMATE_TIME)
 		{
 			player.sendMessage(ChatColor.YELLOW + "You cannot use the " + DEITYNAME + " ultimate again for " + ChatColor.WHITE + ((((ULTIMATE_TIME)/1000)-(System.currentTimeMillis()/1000)))/60 + " minutes");
 			player.sendMessage(ChatColor.YELLOW + "and " + ChatColor.WHITE + ((((ULTIMATE_TIME)/1000)-(System.currentTimeMillis()/1000))%60)+" seconds.");
 			return;
 		}
 
 		// Perform ultimate if there is enough favor
 		if(!DAbilityUtil.doAbilityPreProcess(player, ULTIMATE_COST)) return;
 		
 		int n = congregate(player);
 		if(n > 0) player.sendMessage(ChatColor.YELLOW + "Themis has called upon " + n + " players!");
 		else player.sendMessage(ChatColor.YELLOW + "There are no players to congregate.");
 		
 
 		// Set favor and cooldown
 		DCharUtil.subtractFavor(charID, ULTIMATE_COST);
 		player.setNoDamageTicks(1000);
 		int cooldownMultiplier = (int)(ULTIMATE_COOLDOWN_MAX - ((ULTIMATE_COOLDOWN_MAX - ULTIMATE_COOLDOWN_MIN)*((double)DCharUtil.getAscensions(charID) / 100)));
 		ULTIMATE_TIME = System.currentTimeMillis() + cooldownMultiplier * 1000;
 	}
 	
 	// The actual ability command
 	public static int congregate(Player player)
 	{
 		// Define variables
 		int charID = DPlayerUtil.getCurrentChar(player);
 		
 		DDataUtil.saveTimedCharData(charID, "temp_themis_congregate_call", true, 60);
 		
 		int count = 0;	
 		for(Player pl : player.getWorld().getPlayers())
 		{
			if(pl == player) continue;
 			if(DCharUtil.isImmortal(pl))
 			{
 				count++;
 				if(!player.equals(pl) && !DDataUtil.hasCharData(charID, "temp_themis_congregate"))
 				{
 					pl.sendMessage(ChatColor.GOLD + "Themis has called for an assembly of deities at " + player.getName() + "'s location.");
					pl.sendMessage(ChatColor.GOLD + "Type " + ChatColor.WHITE + "/assemble" + ChatColor.GOLD + " to be teleported.");
 					pl.sendMessage(ChatColor.GRAY + "You have one minute to answer the invitation.");
 					DDataUtil.saveTimedCharData(charID, "temp_themis_congregate", true, 60);
 				}
 			}
 		}
 		
 		return count;
 	}
 	
 	// Don't touch these, they're required to work.
 	public String loadDeity()
 	{
 		DMiscUtil.plugin.getServer().getPluginManager().registerEvents(this, DMiscUtil.plugin);
 		ULTIMATE_TIME = System.currentTimeMillis();
 		SWAP_TIME = System.currentTimeMillis();
 		return DEITYNAME + " loaded.";
 	}
 	public static String getName() { return DEITYNAME; }
 	public static String getAlliance() { return DEITYALLIANCE; }
 	public static ChatColor getColor() { return DEITYCOLOR; }
 }
