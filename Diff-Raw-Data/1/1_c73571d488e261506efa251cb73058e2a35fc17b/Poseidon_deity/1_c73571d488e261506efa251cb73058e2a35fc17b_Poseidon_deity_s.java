 package com.legit2.Demigods.Deities.Gods;
 
 import java.util.ArrayList;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.util.Vector;
 
 import com.google.common.base.Joiner;
 import com.legit2.Demigods.DUtil;
 import com.legit2.Demigods.Libraries.ReflectCommand;
 
 public class Poseidon_deity implements Listener
 {	
 	// Create required universal deity variables
 	private static final String DEITYNAME = "Poseidon";
 	private static final String DEITYALLIANCE = "God";
 
 	/*
 	 *  Set deity-specific ability variable(s).
 	 */
 	// "/reel" Command:
 	private static String REEL_NAME = "Reel"; // Sets the name of this command
 	private static long REEL_TIME; // Creates the variable for later use
 	private static final int REEL_COST = 120; // Cost to run command in "favor"
 	private static final int REEL_DELAY = 1100; // In milliseconds
 
 	// "/drown" Command:
 	private static String DROWN_NAME = "Drown"; // Sets the name of this command
 	private static long DROWN_TIME; // Creates the variable for later use
 	private static final int DROWN_COST = 240; // Cost to run command in "favor"
 	private static final int DROWN_DELAY = 10000; // In milliseconds
 
 	public String loadDeity()
 	{
 		DUtil.plugin.getServer().getPluginManager().registerEvents(this, DUtil.plugin);
 		REEL_TIME = System.currentTimeMillis();
 		DROWN_TIME = System.currentTimeMillis();
 		return DEITYNAME + " loaded.";
 	}
 	
 	public ArrayList<Material> getClaimItems()
 	{
 		ArrayList<Material> claimItems = new ArrayList<Material>();
 		
 		// Add new items in this format: claimItems.add(Material.NAME_OF_MATERIAL);
 		claimItems.add(Material.WATER_BUCKET);
 		claimItems.add(Material.WATER_LILY);
 		
 		return claimItems;
 	}
 
 	public ArrayList<String> getInfo(String username)
 	{		
 		ArrayList<String> toReturn = new ArrayList<String>();
 		
 		if(DUtil.canUseDeitySilent(username, DEITYNAME))
 		{
 			toReturn.add(ChatColor.YELLOW + "[Demigods] " + ChatColor.AQUA + DEITYNAME); //TODO
 			toReturn.add(ChatColor.GREEN + "You are a follower of " + DEITYNAME + "!");
 			
 			return toReturn;
 		}
 		else
 		{
 			// Get Claim Item Names from ArrayList
 			ArrayList<String> claimItemNames = new ArrayList<String>();
 			for(Material item : getClaimItems())
 			{
 				claimItemNames.add(item.name());
 			}
 			
 			// Make Claim Items readable.
 			String claimItems = Joiner.on(", ").join(claimItemNames);
 			
 			toReturn.add(ChatColor.YELLOW + "[Demigods] " + ChatColor.AQUA + DEITYNAME); //TODO
 			toReturn.add("Claim Items: " + claimItems);
 			
 			return toReturn;
 		}
 	}
 
 	// This sets the particular passive ability for the Zeus_deity deity.
 	@EventHandler(priority = EventPriority.MONITOR)
 	public static void onEntityDamange(EntityDamageEvent damageEvent)
 	{
 		if(damageEvent.getEntity() instanceof Player)
 		{
 			Player player = (Player)damageEvent.getEntity();
 			if(!DUtil.canUseDeitySilent(player.getName(), DEITYNAME)) return;
 
 			// If the player receives falling damage, cancel it
 			if(damageEvent.getCause() == DamageCause.DROWNING)
 			{
 				damageEvent.setCancelled(true);
 				return;
 			}
 		}
 	}
 
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public static void onPlayerInteract(PlayerInteractEvent interactEvent)
 	{
 		// Set variables
 		Player player = interactEvent.getPlayer();
 		String username = player.getName();
 
 		if(!DUtil.canUseDeitySilent(username, DEITYNAME)) return;
 
 		if(DUtil.isEnabledAbility(username, DEITYNAME, REEL_NAME) && (player.getItemInHand().getType() == Material.FISHING_ROD))
 		{
 			if(!DUtil.isCooledDown(player, REEL_NAME, REEL_TIME, false)) return;
 			
 			// Set the ability's delay
 			REEL_TIME = System.currentTimeMillis() + REEL_DELAY;
 
 			// Check to see if player has enough favor to perform ability
 			if(DUtil.getFavor(username) >= REEL_COST)
 			{
 				reel(player);
 				DUtil.subtractFavor(username, REEL_COST);
 			}
 			else
 			{
 				player.sendMessage(ChatColor.YELLOW + "You do not have enough " + ChatColor.GREEN + "favor" + ChatColor.RESET + ".");
 				DUtil.disableAbility(username, DEITYNAME, REEL_NAME);
 			}
 		}
 		
 		if(DUtil.isEnabledAbility(username, DEITYNAME, DROWN_NAME) || ((player.getItemInHand() != null) && (player.getItemInHand().getType() == DUtil.getBind(username, DEITYNAME, DROWN_NAME))))
 		{
 			if(!DUtil.isCooledDown(player, DROWN_NAME, DROWN_TIME, false)) return;
 
 			// Set the ability's delay
 			DROWN_TIME = System.currentTimeMillis() + DROWN_DELAY;
 
 			// Check to see if player has enough favor to perform ability
 			if(DUtil.getFavor(username) >= DROWN_COST)
 			{
 				drown(player);
 				DUtil.subtractFavor(username, DROWN_COST);
 			}
 			else
 			{
 				player.sendMessage(ChatColor.YELLOW + "You do not have enough " + ChatColor.GREEN + "favor" + ChatColor.RESET + ".");
 				DUtil.disableAbility(username, DEITYNAME, DROWN_NAME);
 			}
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void onPlayerMove(PlayerMoveEvent event)
 	{
 		Player player = event.getPlayer();
 		
 		// PHELPS SWIMMING
 		if(player.getLocation().getBlock().getType().equals(Material.STATIONARY_WATER) || player.getLocation().getBlock().getType().equals(Material.WATER))
 		{
 			Vector direction = player.getLocation().getDirection().normalize().multiply(1.3D);
 			Vector victor = new Vector(direction.getX(), direction.getY(), direction.getZ());
 			if(player.isSneaking()) player.setVelocity(victor);
 		}
 	}
 
 	/* ------------------
 	 *  Command Handlers
 	 * ------------------
 	 *
 	 *  Command: "/reel"
 	 */
 	@ReflectCommand.Command(name = "reel", sender = ReflectCommand.Sender.PLAYER, permission = "demigods." + DEITYALLIANCE + "." + DEITYNAME)
 	public static void reelCommand(Player player, String arg1)
 	{
 		// Set variables
 		String username = player.getName();
 		
 		if(!DUtil.canUseDeity(player, DEITYNAME)) return;
 
 		if(DUtil.isEnabledAbility(username, DEITYNAME, REEL_NAME))
 		{
 			DUtil.disableAbility(username, DEITYNAME, REEL_NAME);
 			player.sendMessage(ChatColor.YELLOW + REEL_NAME + " is no longer active.");
 		}
 		else
 		{
 			DUtil.enableAbility(username, DEITYNAME, REEL_NAME);
 			player.sendMessage(ChatColor.YELLOW + REEL_NAME + " is now active.");
 		}
 	}
 
 	// The actual ability command
 	public static void reel(Player player)
 	{
 		String username = player.getName();
 		int damage = (int) Math.ceil(0.37286 * Math.pow(DUtil.getDevotion(username, DEITYNAME), 0.371238));
 		LivingEntity target = DUtil.autoTarget(player);
 		
 		if(!DUtil.canPVP(player.getLocation())) player.sendMessage(ChatColor.YELLOW + "You can't do that from a no-PVP zone.");
 		
 		if(target == null)
 		{
 			player.sendMessage(ChatColor.YELLOW + "No target found.");
 			return;
 		}
 		
 		if(target instanceof Player)
 		{
 			if(DUtil.areAllied(username, ((Player) target).getName())) return;
 		}
 			
 		if(target.equals(target)) if (DUtil.canPVP(target.getLocation()))
 		{
 			if (target.getLocation().getBlock().getType() == Material.AIR)
 			{
 				target.getLocation().getBlock().setType(Material.WATER);
 				target.getLocation().getBlock().setData((byte) 0x8);
 			}
 		}
 
 		DUtil.customDamage(player, target, damage, DamageCause.CUSTOM);
 		
 		REEL_TIME = System.currentTimeMillis();
 	}
 	
 	/*
 	 *  Command: "/lightning"
 	 */
 	
 	@ReflectCommand.Command(name = "drown", sender = ReflectCommand.Sender.PLAYER, permission = "demigods." + DEITYALLIANCE + "." + DEITYNAME)
 	public static void drownCommand(Player player, String arg1)
 	{
 		// Set variables
 		String username = player.getName();
 		
 		if(!DUtil.canUseDeity(player, DEITYNAME)) return;
 
 		if(arg1.equalsIgnoreCase("bind"))
 		{		
 			// Bind item
 			DUtil.setBound(username, DEITYNAME, DROWN_NAME, player.getItemInHand().getType());
 		}
 		else
 		{
 			if(DUtil.isEnabledAbility(username, DEITYNAME, DROWN_NAME)) 
 			{
 				DUtil.disableAbility(username, DEITYNAME, DROWN_NAME);
 				player.sendMessage(ChatColor.YELLOW + DROWN_NAME + " is no longer active.");
 			}
 			else
 			{
 				DUtil.enableAbility(username, DEITYNAME, DROWN_NAME);
 				player.sendMessage(ChatColor.YELLOW + DROWN_NAME + " is now active.");
 			}
 		}
 	}
 
 	// The actual ability command
 	public static void drown(Player player)
 	{
 		// Define variables
 		String username = player.getName();
 		int devotion = DUtil.getDevotion(username, DEITYNAME);
 		int radius = (int) Math.ceil(1.6955424 * Math.pow(devotion, 0.129349));
 		int duration = (int) Math.ceil(2.80488 * Math.pow(devotion, 0.2689)); //seconds
 		LivingEntity target = DUtil.autoTarget(player);
 		
 		if(!DUtil.canPVP(player.getLocation())) player.sendMessage(ChatColor.YELLOW + "You can't do that from a no-PVP zone.");
 		
 		if(target == null)
 		{
 			player.sendMessage(ChatColor.YELLOW + "No target found.");
 			return;
 		}
 		
 		if(target instanceof Player)
 		{
 			if(DUtil.areAllied(player.getName(), ((Player) target).getName())) return;
 		}
 		
 		if(DUtil.canPVP(target.getLocation()))
 		{
 			final ArrayList<Block> toReset = new ArrayList<Block>();
 			for(int x =- radius; x <= radius; x++)
 			{
 				for(int y =- radius; y <= radius; y++)
 				{
 					for(int z =- radius; z <= radius; z++)
 					{
 						Block block = target.getWorld().getBlockAt(target.getLocation().getBlockX() + x, target.getLocation().getBlockY() + y, target.getLocation().getBlockZ() + z);
 						if(block.getLocation().distance(target.getLocation()) <= radius)
 						{
 							if(block.getType() == Material.AIR)
 							{
 								block.setType(Material.WATER);
 								block.setData((byte) (0x8));
 								toReset.add(block);
 							}
 						}
 					}
 				}
 			}
 			
 			DUtil.getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(DUtil.getPlugin(), new Runnable()
 			{
 				@Override
 				public void run()
 				{
 					for(Block block : toReset)
 					{
 						if((block.getType() == Material.WATER) || (block.getType() == Material.STATIONARY_WATER)) block.setType(Material.AIR);
 					}
 				}
 			}, duration);
 		}
 	}
 	
 	// Don't touch these, they're required to work.
 	public static String getName() { return DEITYNAME; }
 	public static String getAlliance() { return DEITYALLIANCE; }
 }
