 package com.legit2.Demigods.Deities.Gods;
 
 import java.util.ArrayList;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
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
 
 import com.legit2.Demigods.Libraries.ReflectCommand;
 import com.legit2.Demigods.Utilities.DCharUtil;
 import com.legit2.Demigods.Utilities.DAbilityUtil;
 import com.legit2.Demigods.Utilities.DPlayerUtil;
 import com.legit2.Demigods.Utilities.DMiscUtil;
 
 public class Zeus_deity implements Listener
 {	
 	// Create required universal deity variables
 	private static final String DEITYNAME = "Zeus";
 	private static final String DEITYALLIANCE = "God";
 	private static final ChatColor DEITYCOLOR = ChatColor.YELLOW;
 
 	/*
 	 *  Set deity-specific ability variable(s).
 	 */
 	// "/shove" Command:
 	private static String SHOVE_NAME = "Shove"; // Sets the name of this command
 	private static long SHOVE_TIME; // Creates the variable for later use
 	private static final int SHOVE_COST = 170; // Cost to run command in "favor"
 	private static final int SHOVE_DELAY = 1500; // In milliseconds
 
 	// "/lightning" Command:
 	private static String LIGHTNING_NAME = "Lightning"; // Sets the name of this command
 	private static long LIGHTNING_TIME; // Creates the variable for later use
 	private static final int LIGHTNING_COST = 140; // Cost to run command in "favor"
 	private static final int LIGHTNING_DELAY = 1000; // In milliseconds
 
 	// "/storm" Command:
 	@SuppressWarnings("unused")
 	private static String ULTIMATE_NAME = "Storm";
 	private static long ULTIMATE_TIME; // Creates the variable for later use
 	private static final int ULTIMATE_COST = 3700; // Cost to run command in "favor"
 	private static final int ULTIMATE_COOLDOWN_MAX = 600; // In seconds
 	private static final int ULTIMATE_COOLDOWN_MIN = 60; // In seconds
 
 	public ArrayList<Material> getClaimItems()
 	{
 		ArrayList<Material> claimItems = new ArrayList<Material>();
 		
 		// Add new items in this format: claimItems.add(Material.NAME_OF_MATERIAL);
 		claimItems.add(Material.IRON_INGOT);
 		claimItems.add(Material.FEATHER);
 		
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
 			toReturn.add(ChatColor.GRAY + " Active:");
 			toReturn.add(ChatColor.GRAY + " -> " + ChatColor.GREEN + "/shove" + ChatColor.WHITE + " - Shove your target away from you.");
 			toReturn.add(ChatColor.GRAY + " -> " + ChatColor.GREEN + "/lightning" + ChatColor.WHITE + " - Strike lightning upon your enemies.");
 			toReturn.add(" ");
 			toReturn.add(ChatColor.GRAY + " Passive:");
 			toReturn.add(ChatColor.GRAY + " -> " + ChatColor.WHITE + "Take no damage from falling.");
 			toReturn.add(" ");
 			toReturn.add(ChatColor.GRAY + " Ultimate:");
 			toReturn.add(ChatColor.GRAY + " -> " + ChatColor.GREEN + "/storm" + ChatColor.WHITE + " - Throw all of your enemies into the sky as lightning fills the heavens.");
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
 			toReturn.add(ChatColor.GRAY + " Active:");
 			toReturn.add(ChatColor.GRAY + " -> " + ChatColor.GREEN + "/shove" + ChatColor.WHITE + " - Shove your target away from you.");
 			toReturn.add(ChatColor.GRAY + " -> " + ChatColor.GREEN + "/lightning" + ChatColor.WHITE + " - Strike lightning upon your enemies.");
 			toReturn.add(" ");
 			toReturn.add(ChatColor.GRAY + " Passive:");
 			toReturn.add(ChatColor.GRAY + " -> " + ChatColor.WHITE + "Take no damage from falling.");
 			toReturn.add(" ");
 			toReturn.add(ChatColor.GRAY + " Ultimate:");
 			toReturn.add(ChatColor.GRAY + " -> " + ChatColor.GREEN + "/storm" + ChatColor.WHITE + " - Throw all of your enemies into the sky as lightning fills the heavens.");
 			toReturn.add(" ");
 			toReturn.add(ChatColor.GRAY + " Claim Items:");
 			for(Material item : getClaimItems())
 			{
				toReturn.add(ChatColor.GRAY + " -> " + ChatColor.WHITE + item.name());
 			}
 			toReturn.add(" ");
 
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
 			if(!DMiscUtil.canUseDeitySilent(player, DEITYNAME)) return;
 
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
 
 		if(!DMiscUtil.canUseDeitySilent(player, DEITYNAME)) return;
 
 		if(DCharUtil.isEnabledAbility(player, SHOVE_NAME) || ((player.getItemInHand() != null) && (player.getItemInHand().getType() == DCharUtil.getBind(player, SHOVE_NAME))))
 		{
 			if(!DCharUtil.isCooledDown(player, SHOVE_NAME, SHOVE_TIME, false)) return;
 
 			shove(player);
 		}
 		
 		if(DCharUtil.isEnabledAbility(player, LIGHTNING_NAME) || ((player.getItemInHand() != null) && (player.getItemInHand().getType() == DCharUtil.getBind(player, LIGHTNING_NAME))))
 		{
 			if(!DCharUtil.isCooledDown(player, LIGHTNING_NAME, LIGHTNING_TIME, false)) return;
 
 			lightning(player);
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
 		if(!DMiscUtil.canUseDeity(player, DEITYNAME)) return;
 
 		if(arg1.equalsIgnoreCase("bind"))
 		{		
 			// Bind item
 			DCharUtil.setBound(player, SHOVE_NAME, player.getItemInHand().getType());
 		}
 		else
 		{
 			if(DCharUtil.isEnabledAbility(player, SHOVE_NAME))
 			{
 				DCharUtil.disableAbility(player, SHOVE_NAME);
 				player.sendMessage(ChatColor.YELLOW + SHOVE_NAME + " is no longer active.");
 			}
 			else
 			{
 				DCharUtil.enableAbility(player, SHOVE_NAME);
 				player.sendMessage(ChatColor.YELLOW + SHOVE_NAME + " is now active.");
 			}
 		}
 	}
 
 	// The actual ability command
 	public static void shove(Player player)
 	{
 		// Define variables
 		int charID = DPlayerUtil.getCurrentChar(player);
 		int devotion = DCharUtil.getDevotion(charID);
 		double multiply = 0.1753 * Math.pow(devotion, 0.322917);
 		LivingEntity target = DMiscUtil.autoTarget(player);
 		
 		if(!DAbilityUtil.doAbilityPreProcess(player, target, SHOVE_COST)) return;
 		SHOVE_TIME = System.currentTimeMillis() + SHOVE_DELAY;
 		DCharUtil.subtractFavor(charID, SHOVE_COST);
 		
 		Vector vector = player.getLocation().toVector();
 		Vector victor = target.getLocation().toVector().subtract(vector);
 		victor.multiply(multiply);
 		target.setVelocity(victor);
 	}
 	
 	/*
 	 *  Command: "/lightning"
 	 */
 	
 	@ReflectCommand.Command(name = "lightning", sender = ReflectCommand.Sender.PLAYER, permission = "demigods." + DEITYALLIANCE + "." + DEITYNAME)
 	public static void lightningCommand(Player player, String arg1)
 	{		
 		if(!DMiscUtil.canUseDeity(player, DEITYNAME)) return;
 
 		if(arg1.equalsIgnoreCase("bind"))
 		{		
 			// Bind item
 			DCharUtil.setBound(player, LIGHTNING_NAME, player.getItemInHand().getType());
 		}
 		else
 		{
 			if(DCharUtil.isEnabledAbility(player, LIGHTNING_NAME)) 
 			{
 				DCharUtil.disableAbility(player, LIGHTNING_NAME);
 				player.sendMessage(ChatColor.YELLOW + LIGHTNING_NAME + " is no longer active.");
 			}
 			else
 			{
 				DCharUtil.enableAbility(player, LIGHTNING_NAME);
 				player.sendMessage(ChatColor.YELLOW + LIGHTNING_NAME + " is now active.");
 			}
 		}
 	}
 
 	// The actual ability command
 	public static void lightning(Player player)
 	{
 		// Define variables
 		int charID = DPlayerUtil.getCurrentChar(player);
 		LivingEntity target = DMiscUtil.autoTarget(player);
 		
 		if(!DAbilityUtil.doAbilityPreProcess(player, target, LIGHTNING_COST)) return;
 		LIGHTNING_TIME = System.currentTimeMillis() + LIGHTNING_DELAY;
 		DCharUtil.subtractFavor(charID, LIGHTNING_COST);
 		
 		strikeLightning(player, target);
 	}
 
 	/*
 	 *  Command: "/storm"
 	 */
 	@ReflectCommand.Command(name = "storm", sender = ReflectCommand.Sender.PLAYER, permission = "demigods." + DEITYALLIANCE + "." + DEITYNAME + ".ultimate")
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
 
 		if(!DAbilityUtil.doAbilityPreProcess(player, ULTIMATE_COST)) return;
 
 		
 		// Perform ultimate if there is enough favor
 		int count = storm(player);
 		if(count == 0)
 		{
 			player.sendMessage(ChatColor.YELLOW + "Zeus unable to strike any targets.");
 			return;
 		}
 		
 		player.sendMessage(ChatColor.YELLOW + "Zeus has struck " + count + " targets!");
 
 		// Set favor and cooldown
 		DCharUtil.subtractFavor(charID, ULTIMATE_COST);
 		player.setNoDamageTicks(1000);
 		int cooldownMultiplier = (int)(ULTIMATE_COOLDOWN_MAX - ((ULTIMATE_COOLDOWN_MAX - ULTIMATE_COOLDOWN_MIN)*((double)DCharUtil.getAscensions(charID) / 100)));
 		ULTIMATE_TIME = System.currentTimeMillis() + cooldownMultiplier * 1000;
 	}
 	
 	// The actual ability command
 	public static int storm(Player player)
 	{
 		// Define variables
 		ArrayList<Entity> entityList = new ArrayList<Entity>();
 		Vector playerLocation = player.getLocation().toVector();
 		
 		for(Entity anEntity : player.getWorld().getEntities()) if(anEntity.getLocation().toVector().isInSphere(playerLocation, 50.0)) entityList.add(anEntity);
 
 		int count = 0;
 		for(Entity entity : entityList)
 		{
 			try
 			{
 				if(entity instanceof Player)
 				{
 					Player otherPlayer = (Player) entity;
 					if(!DMiscUtil.areAllied(player, otherPlayer) && !otherPlayer.equals(player))
 					{
 						if(strikeLightning(player, otherPlayer)) count++;
 						strikeLightning(player, otherPlayer);
 						strikeLightning(player, otherPlayer);
 					}
 				}
 				else if(entity instanceof LivingEntity)
 				{
 					LivingEntity livingEntity = (LivingEntity) entity;
 					if(strikeLightning(player, livingEntity)) count++;
 					strikeLightning(player, livingEntity);
 					strikeLightning(player, livingEntity);
 				}
 			}
 			catch (Exception notAlive) {} // Ignore stuff like Minecarts
 		}
 		
 		return count;
 	}
 
 	private static boolean strikeLightning(Player player, LivingEntity target)
 	{
 		// Set variables
 		int charID = DPlayerUtil.getCurrentChar(player);
 		
 		if(!player.getWorld().equals(target.getWorld())) return false;
 		if(!DMiscUtil.canTarget(target)) return false;
 		
 		player.getWorld().strikeLightningEffect(target.getLocation());
 		
 		for(Entity entity : target.getLocation().getBlock().getChunk().getEntities())
 		{
 			if(entity instanceof LivingEntity)
 			{
 				if(!DMiscUtil.canTarget(entity)) continue;
 				LivingEntity livingEntity = (LivingEntity) entity;
 				if(livingEntity.getLocation().distance(target.getLocation()) < 1.5) DMiscUtil.customDamage(player, livingEntity, DCharUtil.getAscensions(charID)*2, DamageCause.LIGHTNING);
 			}
 		}
 		return true;
 	}
 	
 	// Don't touch these, they're required to work.
 	public String loadDeity()
 	{
 		DMiscUtil.plugin.getServer().getPluginManager().registerEvents(this, DMiscUtil.plugin);
 		ULTIMATE_TIME = System.currentTimeMillis();
 		SHOVE_TIME = System.currentTimeMillis();
 		LIGHTNING_TIME = System.currentTimeMillis();
 		return DEITYNAME + " loaded.";
 	}
 	public static String getName() { return DEITYNAME; }
 	public static String getAlliance() { return DEITYALLIANCE; }
 	public static ChatColor getColor() { return DEITYCOLOR; }
 }
