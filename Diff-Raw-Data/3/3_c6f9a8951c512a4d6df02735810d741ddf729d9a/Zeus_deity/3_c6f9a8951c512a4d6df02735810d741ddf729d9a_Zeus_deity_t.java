 package com.legit2.Demigods.Deities.Gods;
 
 import java.util.ArrayList;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.util.Vector;
 
 import com.google.common.base.Joiner;
 import com.legit2.Demigods.DUtil;
 import com.legit2.Demigods.Libraries.ReflectCommand;
 
 public class Zeus_deity implements Listener
 {	
 	// Create required universal deity variables
 	private static final String DEITYNAME = "Zeus";
 	private static final String DEITYALLIANCE = "God";
 
 	/*
 	 *  Set deity-specific ability variable(s).
 	 */
 	// "/shove" Command:
 	private static String SHOVE_NAME = "Shove"; // Sets the name of this command
 	private static long SHOVE_TIME; // Creates the variable for later use
 	private static final int SHOVE_COST = 170; // Cost to run command in "favor"
 	private static final int SHOVE_DELAY = 10000; // In milliseconds
 
 	// "/lightning" Command:
 	private static String LIGHTNING_NAME = "Lightning"; // Sets the name of this command
 	private static long LIGHTNING_TIME; // Creates the variable for later use
 	private static final int LIGHTNING_COST = 140; // Cost to run command in "favor"
 	private static final int LIGHTNING_DELAY = 1000; // In milliseconds
 
 	// "/storm" Command:
 	private static String ULTIMATE_NAME = "Storm";
 	private static long ULTIMATE_TIME; // Creates the variable for later use
 	private static final int ULTIMATE_COST = 3700; // Cost to run command in "favor"
 	private static final int ULTIMATE_COOLDOWN_MAX = 600; // In seconds
 	private static final int ULTIMATE_COOLDOWN_MIN = 60; // In seconds
 
 	public String loadDeity()
 	{
 		DUtil.plugin.getServer().getPluginManager().registerEvents(this, DUtil.plugin);
 		ULTIMATE_TIME = System.currentTimeMillis();
 		SHOVE_TIME = System.currentTimeMillis();
 		LIGHTNING_TIME = System.currentTimeMillis();
 		return DEITYNAME + " loaded.";
 	}
 	
 	public ArrayList<Material> getClaimItems()
 	{
 		ArrayList<Material> claimItems = new ArrayList<Material>();
 		
 		// Add new items in this format: claimItems.add(Material.NAME_OF_MATERIAL);
 		claimItems.add(Material.IRON_INGOT);
 		claimItems.add(Material.FEATHER);
 		
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
 			if(damageEvent.getCause() == DamageCause.FALL)
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
 
 		if(DUtil.isEnabledAbility(username, DEITYNAME, SHOVE_NAME) || ((player.getItemInHand() != null) && (player.getItemInHand().getType() == DUtil.getBind(username, DEITYNAME, SHOVE_NAME))))
 		{
 			if(!DUtil.isCooledDown(player, SHOVE_NAME, SHOVE_TIME, false)) return;
 
 			// Set the ability's delay
 			SHOVE_TIME = System.currentTimeMillis() + SHOVE_DELAY;
 
 			// Check to see if player has enough favor to perform ability
 			if(DUtil.getFavor(username) >= SHOVE_COST)
 			{
 				shove(player);
 				DUtil.subtractFavor(username, SHOVE_COST);
 			}
 			else
 			{
 				player.sendMessage(ChatColor.YELLOW + "You do not have enough " + ChatColor.GREEN + "favor" + ChatColor.RESET + ".");
 				DUtil.disableAbility(username, DEITYNAME, SHOVE_NAME);
 			}
 		}
 		
 		if(DUtil.isEnabledAbility(username, DEITYNAME, LIGHTNING_NAME) || ((player.getItemInHand() != null) && (player.getItemInHand().getType() == DUtil.getBind(username, DEITYNAME, LIGHTNING_NAME))))
 		{
 			if(!DUtil.isCooledDown(player, LIGHTNING_NAME, LIGHTNING_TIME, false)) return;
 
 			// Set the ability's delay
 			LIGHTNING_TIME = System.currentTimeMillis() + LIGHTNING_DELAY;
 
 			// Check to see if player has enough favor to perform ability
 			if(DUtil.getFavor(username) >= LIGHTNING_COST)
 			{
 				lightning(player);
 				DUtil.subtractFavor(username, LIGHTNING_COST);
 			}
 			else
 			{
 				player.sendMessage(ChatColor.YELLOW + "You do not have enough " + ChatColor.GREEN + "favor" + ChatColor.RESET + ".");
 				DUtil.disableAbility(username, DEITYNAME, LIGHTNING_NAME);
 			}
 		}
 	}
 
 	/* ------------------
 	 *  Command Handlers
 	 * ------------------
 	 *
 	 *  Command: "/shove"
 	 */
 	@ReflectCommand.Command(name = "shove", sender = ReflectCommand.Sender.PLAYER, permission = "demigods." + DEITYALLIANCE + "." + DEITYNAME)
 	public static void shoveCommand(Player player, String arg1)
 	{
 		// Set variables
 		String username = player.getName();
 		
 		if(!DUtil.canUseDeity(player, DEITYNAME)) return;
 
 		if(arg1.equalsIgnoreCase("bind"))
 		{		
 			// Bind item
 			DUtil.setBound(username, DEITYNAME, SHOVE_NAME, player.getItemInHand().getType());
 		}
 		else
 		{
 			if(DUtil.isEnabledAbility(username, DEITYNAME, SHOVE_NAME))
 			{
 				DUtil.disableAbility(username, DEITYNAME, SHOVE_NAME);
 				player.sendMessage(ChatColor.YELLOW + SHOVE_NAME + " is no longer active.");
 			}
 			else
 			{
 				DUtil.enableAbility(username, DEITYNAME, SHOVE_NAME);
 				player.sendMessage(ChatColor.YELLOW + SHOVE_NAME + " is now active.");
 			}
 		}
 	}
 
 	// The actual ability command
 	public static void shove(Player player)
 	{
 		// Define variables
 		String username = player.getName().toLowerCase();
 		int devotion = DUtil.getDevotion(username, DEITYNAME);
 		int targets = (int) Math.ceil(1.561 * Math.pow(devotion, 0.128424));
 		double multiply = 0.1753 * Math.pow(devotion, 0.322917);
 		
 		if(!DUtil.canPVP(player.getLocation())) player.sendMessage(ChatColor.YELLOW + "You can't do that from a no-PVP zone.");
 		
 		// Get Targets as an ArrayList
 		ArrayList<LivingEntity> hit = new ArrayList<LivingEntity>();
 		
 		for (LivingEntity livingEntity : player.getWorld().getLivingEntities())
 		{
 				if(targets == hit.size()) break;
 				
 				if(livingEntity instanceof Player)
 				{
 					if(DUtil.areAllied(username, ((Player)livingEntity).getName())) continue;
 				}
 				
 				if((livingEntity.equals(DUtil.autoTarget(player))) && !hit.contains(livingEntity)) if (DUtil.canPVP(livingEntity.getLocation())) hit.add(livingEntity);
 		}
 		
 		if (hit.size() > 0)
 		{
 			for (LivingEntity livingEntity : hit)
 			{
 				Vector vector = player.getLocation().toVector();
 				Vector victor = livingEntity.getLocation().toVector().subtract(vector);
 				victor.multiply(multiply);
 				livingEntity.setVelocity(victor);
 			}
 		}
 	}
 	
 	/*
 	 *  Command: "/lightning"
 	 */
 	
 	@ReflectCommand.Command(name = "lightning", sender = ReflectCommand.Sender.PLAYER, permission = "demigods." + DEITYALLIANCE + "." + DEITYNAME)
 	public static void lightningCommand(Player player, String arg1)
 	{
 		// Set variables
 		String username = player.getName();
 		
 		if(!DUtil.canUseDeity(player, DEITYNAME)) return;
 
 		if(arg1.equalsIgnoreCase("bind"))
 		{		
 			// Bind item
 			DUtil.setBound(username, DEITYNAME, LIGHTNING_NAME, player.getItemInHand().getType());
 		}
 		else
 		{
 			if(DUtil.isEnabledAbility(username, DEITYNAME, LIGHTNING_NAME)) 
 			{
 				DUtil.disableAbility(username, DEITYNAME, LIGHTNING_NAME);
 				player.sendMessage(ChatColor.YELLOW + LIGHTNING_NAME + " is no longer active.");
 			}
 			else
 			{
 				DUtil.enableAbility(username, DEITYNAME, LIGHTNING_NAME);
 				player.sendMessage(ChatColor.YELLOW + LIGHTNING_NAME + " is now active.");
 			}
 		}
 	}
 
 	// The actual ability command
 	public static void lightning(Player player)
 	{
 		// Define variables
 		Block block = player.getTargetBlock(null, 200);
 		Location target = block.getLocation();
 		
 		if(!DUtil.canPVP(player.getLocation())) player.sendMessage(ChatColor.YELLOW + "You can't do that from a no-PVP zone.");
 		
 		if (player.getLocation().distance(target) > 2)
 		{
 			try
 			{
 				strikeLightning(player, target);
 			} 
 			catch (Exception nullpointer) {} //ignore it if something went wrong
 		}
 		else player.sendMessage(ChatColor.YELLOW + "Your target is too far away, or too close to you.");		
 	}
 
 	/*
 	 *  Command: "/storm"
 	 */
 	@ReflectCommand.Command(name = "storm", sender = ReflectCommand.Sender.PLAYER, permission = "demigods." + DEITYALLIANCE + "." + DEITYNAME + ".ultimate")
 	public static void ultimateCommand(Player player)
 	{
 		// Set variables
 		String username = player.getName();
 		
 		// Check the player for DEITYNAME
 		if(!DUtil.hasDeity(username, DEITYNAME)) return;
 
 		// Check if the ultimate has cooled down or not
 		if(System.currentTimeMillis() < ULTIMATE_TIME)
 		{
 			player.sendMessage(ChatColor.YELLOW + "You cannot use the " + DEITYNAME + " ultimate again for " + ChatColor.WHITE + ((((ULTIMATE_TIME)/1000)-(System.currentTimeMillis()/1000)))/60 + " minutes");
 			player.sendMessage(ChatColor.YELLOW + "and " + ChatColor.WHITE + ((((ULTIMATE_TIME)/1000)-(System.currentTimeMillis()/1000))%60)+" seconds.");
 			return;
 		}
 
 		// Perform ultimate if there is enough favor
 		if(DUtil.getFavor(username) >= ULTIMATE_COST)
 		{
 			if(!DUtil.canPVP(player.getLocation()))
 			{
 				player.sendMessage(ChatColor.YELLOW + "You can't do that from a no-PVP zone.");
 				return; 
 			}
 			
 			player.sendMessage(ChatColor.YELLOW + "Zeus has struck " + storm(player) + " targets!");
 
 			// Set favor and cooldown
 			DUtil.subtractFavor(username, ULTIMATE_COST);
 			player.setNoDamageTicks(1000);
 			int cooldownMultiplier = (int)(ULTIMATE_COOLDOWN_MAX - ((ULTIMATE_COOLDOWN_MAX - ULTIMATE_COOLDOWN_MIN)*((double)DUtil.getAscensions(username) / 100)));
 			ULTIMATE_TIME = System.currentTimeMillis() + cooldownMultiplier * 1000;
 		}
 		// Give a message if there is not enough favor
 		else player.sendMessage(ChatColor.YELLOW + ULTIMATE_NAME + " requires " + ULTIMATE_COST + ChatColor.GREEN + " favor" + ChatColor.YELLOW + ".");
 	}
 	
 	// The actual ability command
 	public static int storm(Player player)
 	{
 		// Define variables
 		ArrayList<Entity> entityList = new ArrayList<Entity>();
 		Vector playerLocation = player.getLocation().toVector();
 		
 		if(!DUtil.canPVP(player.getLocation())) player.sendMessage(ChatColor.YELLOW + "You can't do that from a no-PVP zone.");
 		
 		for(Entity anEntity : player.getWorld().getEntities()) if(anEntity.getLocation().toVector().isInSphere(playerLocation, 50.0)) entityList.add(anEntity);
 
 		int count = 0;
 		for(Entity entity : entityList)
 		{
 			try
 			{
 				if(entity instanceof Player)
 				{
 					Player otherPlayer = (Player) entity;
 					if (!DUtil.areAllied(player.getName(), otherPlayer.getName()) && !otherPlayer.equals(player))
 					{
 						strikeLightning(player, otherPlayer.getLocation());
 						strikeLightning(player, otherPlayer.getLocation());
 						strikeLightning(player, otherPlayer.getLocation());
 						count++;
 					}
 				}
 				else if(entity instanceof LivingEntity)
 				{
 					LivingEntity livingEntity = (LivingEntity) entity;
 					strikeLightning(player, livingEntity.getLocation());
 					strikeLightning(player, livingEntity.getLocation());
 					strikeLightning(player, livingEntity.getLocation());
 					count++;
 				}
 			}
 			catch (Exception notAlive) {} //ignore stuff like minecarts
 		}
 		
 		return count;
 	}
 
 	private static void strikeLightning(Player player, Location target)
 	{
 		if(!player.getWorld().equals(target.getWorld())) return;
 		if(!DUtil.canPVP(target)) return;
 		
 		player.getWorld().strikeLightningEffect(target);
 		
 		for(Entity entity : target.getBlock().getChunk().getEntities())
 		{
 			if(entity instanceof LivingEntity)
 			{
 				LivingEntity livingEntity = (LivingEntity) entity;
 				if(livingEntity.getLocation().distance(target) < 1.5) DUtil.customDamage(player, livingEntity, DUtil.getAscensions(player.getName())*2, DamageCause.LIGHTNING);
 			}
 		}
 	}
 	
 	// Don't touch these, they're required to work.
 	public static String getName() { return DEITYNAME; }
 	public static String getAlliance() { return DEITYALLIANCE; }
 }
