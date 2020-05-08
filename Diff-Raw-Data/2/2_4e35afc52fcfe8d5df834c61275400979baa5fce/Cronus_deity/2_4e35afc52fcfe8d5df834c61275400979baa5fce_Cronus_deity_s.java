 package com.legit2.Demigods.Deities.Titans;
 
 import java.util.ArrayList;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Effect;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.potion.PotionEffect;
 import org.bukkit.potion.PotionEffectType;
 import org.bukkit.util.Vector;
 
 import com.google.common.base.Joiner;
 import com.legit2.Demigods.DUtil;
 import com.legit2.Demigods.Libraries.ReflectCommand;
 
 public class Cronus_deity implements Listener
 {	
 	// Create required universal deity variables
 	private static final String DEITYNAME = "Cronus";
 	private static final String DEITYALLIANCE = "Titan";
 
 	/*
 	 *  Set deity-specific ability variable(s).
 	 */
 	// "/cleave" Command:
 	private static String CLEAVE_NAME = "Cleave"; // Sets the name of this command
 	private static long CLEAVE_TIME; // Creates the variable for later use
 	private static final int CLEAVE_COST = 100; // Cost to run command in "favor"
 	private static final int CLEAVE_DELAY = 1000; // In milliseconds
 
 	// "/slow" Command:
 	private static String SLOW_NAME = "Slow"; // Sets the name of this command
 	private static long SLOW_TIME; // Creates the variable for later use
 	private static final int SLOW_COST = 180; // Cost to run command in "favor"
 	private static final int SLOW_DELAY = 1000; // In milliseconds
 
 	// "/timestop" Command:
 	private static String ULTIMATE_NAME = "Timestop";
 	private static long ULTIMATE_TIME; // Creates the variable for later use
 	private static final int ULTIMATE_COST = 3700; // Cost to run command in "favor"
 	private static final int ULTIMATE_COOLDOWN_MAX = 600; // In seconds
 	private static final int ULTIMATE_COOLDOWN_MIN = 60; // In seconds
 
 	public String loadDeity()
 	{
 		DUtil.plugin.getServer().getPluginManager().registerEvents(this, DUtil.plugin);
 		ULTIMATE_TIME = System.currentTimeMillis();
 		CLEAVE_TIME = System.currentTimeMillis();
 		SLOW_TIME = System.currentTimeMillis();
 		return DEITYNAME + " loaded.";
 	}
 	
 	public ArrayList<Material> getClaimItems()
 	{
 		ArrayList<Material> claimItems = new ArrayList<Material>();
 		
 		// Add new items in this format: claimItems.add(Material.NAME_OF_MATERIAL);
 		claimItems.add(Material.SOUL_SAND);
 		claimItems.add(Material.WATCH);
 		
 		return claimItems;
 	}
 
 	public void printInfo(Player player)
 	{		
 		if(!DUtil.canUseDeity(player, DEITYNAME, false))
 		{
 			// Print Deity Info to Chat
 			DUtil.taggedMessage(player, ChatColor.AQUA + DEITYNAME);
 			// TODO Deity Info
 			return;
 		}
 
 		DUtil.taggedMessage(player, ChatColor.AQUA + DEITYNAME);
 		// TODO Deity Info
 		
 		// Get Claim Item Names from ArrayList
 		ArrayList<String> claimItemNames = new ArrayList<String>();
 		for(Material item : getClaimItems())
 		{
 			claimItemNames.add(item.name());
 		}
 		
 		// Make Claim Items readable.
 		String claimItems = Joiner.on(",").join(claimItemNames);
 		
 		player.sendMessage("Claim Items: " + claimItems);
 	}
 
 	// This sets the particular passive ability for the Cronus deity.
 	@EventHandler(priority = EventPriority.MONITOR)
 	public static void onEntityDamange(EntityDamageByEntityEvent damageEvent)
 	{
		if(damageEvent.getEntity() instanceof Player)
 		{
 			Player player = (Player)damageEvent.getDamager();
 			String username = player.getName();
 			
 			if(!DUtil.canUseDeity(player, DEITYNAME, false)) return;
 			
 			if(!DUtil.canPVP(damageEvent.getEntity().getLocation())) return;
 
 			if(!player.getItemInHand().getType().name().contains("_HOE")) return;
 			
 			if(damageEvent.getEntity() instanceof Player)
 			{
 				Player attacked = (Player)damageEvent.getEntity();
 				
 				// Cronus Passive: Stop movement
 				if(!DUtil.isImmortal(attacked.getName()) || (DUtil.isImmortal(attacked.getName()) && !DUtil.areAllied(username, attacked.getName()))) attacked.setVelocity(new Vector(0,0,0));
 			}
 			
 			if(DUtil.isEnabledAbility(username, DEITYNAME, CLEAVE_NAME))
 			{
 				if(!DUtil.isCooledDown(player, CLEAVE_NAME, CLEAVE_TIME, false)) return;
 
 				// Set the ability's delay
 				CLEAVE_TIME = System.currentTimeMillis() + CLEAVE_DELAY;
 				
 				// Check to see if player has enough favor to perform ability
 				if(DUtil.getFavor(username) >= CLEAVE_COST)
 				{
 					cleave(damageEvent);
 					DUtil.subtractFavor(username, CLEAVE_COST);
 					return;
 				}
 				else
 				{
 					player.sendMessage(ChatColor.YELLOW + "You do not have enough " + ChatColor.GREEN + "favor" + ChatColor.RESET + ".");
 					DUtil.setDeityData(username, DEITYNAME, CLEAVE_NAME, false);
 				}
 			}
 		}
 	}
 
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public static void onPlayerInteract(PlayerInteractEvent interactEvent)
 	{
 		// Set variables
 		Player player = interactEvent.getPlayer();
 		String username = player.getName();
 
 		if(!DUtil.canUseDeity(player, DEITYNAME, false)) return;
 
 		if(DUtil.isEnabledAbility(username, DEITYNAME, SLOW_NAME) || ((player.getItemInHand() != null) && (player.getItemInHand().getType() == DUtil.getDeityData(username, DEITYNAME, SLOW_NAME + "_bind"))))
 		{
 			if(!DUtil.isCooledDown(player, SLOW_NAME, SLOW_TIME, false)) return;
 
 			// Set the ability's delay
 			SLOW_TIME = System.currentTimeMillis() + SLOW_DELAY;
 
 			// Check to see if player has enough favor to perform ability
 			if(DUtil.getFavor(username) >= SLOW_COST)
 			{
 				slow(player);
 				DUtil.subtractFavor(username, SLOW_COST);
 				return;
 			}
 			else
 			{
 				player.sendMessage(ChatColor.YELLOW + "You do not have enough " + ChatColor.GREEN + "favor" + ChatColor.RESET + ".");
 				DUtil.setDeityData(username, DEITYNAME, SLOW_NAME, false);
 			}
 		}
 	}
 
 	/* ------------------
 	 *  Command Handlers
 	 * ------------------
 	 *
 	 *  Command: "/cleave"
 	 */
 	@ReflectCommand.Command(name = "cleave", sender = ReflectCommand.Sender.PLAYER, permission = "demigods." + DEITYALLIANCE + "." + DEITYNAME)
 	public static void cleaveCommand(Player player)
 	{
 		// Set variables
 		String username = player.getName();
 		
 		if(!DUtil.canUseDeity(player, DEITYNAME, true)) return;
 
 		if(DUtil.getDeityData(username, DEITYNAME, CLEAVE_NAME) != null && (Boolean) DUtil.getDeityData(username, DEITYNAME, CLEAVE_NAME)) 
 		{
 			DUtil.setDeityData(username, DEITYNAME, CLEAVE_NAME, false);
 			player.sendMessage(ChatColor.YELLOW + CLEAVE_NAME + " is no longer active.");
 		}
 		else
 		{
 			DUtil.setDeityData(username, DEITYNAME, CLEAVE_NAME, true);
 			player.sendMessage(ChatColor.YELLOW + CLEAVE_NAME + " is now active.");
 		}
 	}
 
 	// The actual ability command
 	public static void cleave(EntityDamageByEntityEvent damageEvent)
 	{
 		// Define variables
 		Player player = (Player)damageEvent.getDamager();
 		Entity attacked = damageEvent.getEntity();
 		String username = player.getName();
 		
 		if (DUtil.getFavor(username) >= CLEAVE_COST)
 		{
 			if (!(attacked instanceof LivingEntity)) return;
 			
 			for (int i = 1; i <= 31; i += 4) attacked.getWorld().playEffect(attacked.getLocation(), Effect.SMOKE, i);
 			
 			DUtil.customDamage(player, (LivingEntity)attacked, (int)Math.ceil(Math.pow(DUtil.getDevotion(username, DEITYNAME), 0.35)), DamageCause.ENTITY_ATTACK);
 			
 			if ((LivingEntity)attacked instanceof Player)
 			{
 				Player attackedPlayer = (Player)((LivingEntity)attacked);
 				
 				attackedPlayer.setFoodLevel(attackedPlayer.getFoodLevel() - (damageEvent.getDamage()/2));
 				
 				if (attackedPlayer.getFoodLevel() < 0) attackedPlayer.setFoodLevel(0);
 			}
 		}
 	}
 	
 	/*
 	 *  Command: "/slow"
 	 */
 	
 	@ReflectCommand.Command(name = "slow", sender = ReflectCommand.Sender.PLAYER, permission = "demigods." + DEITYALLIANCE + "." + DEITYNAME)
 	public static void slowCommand(Player player, String arg1)
 	{
 		// Set variables
 		String username = player.getName();
 		
 		if(!DUtil.canUseDeity(player, DEITYNAME, true)) return;
 
 		if(arg1.equalsIgnoreCase("bind"))
 		{		
 			// Bind item
 			DUtil.setBound(username, DEITYNAME, SLOW_NAME, player.getItemInHand().getType());
 		}
 		else
 		{
 			if(DUtil.getDeityData(username, DEITYNAME, SLOW_NAME) != null && (Boolean) DUtil.getDeityData(username, DEITYNAME, SLOW_NAME)) 
 			{
 				DUtil.setDeityData(username, DEITYNAME, SLOW_NAME, false);
 				player.sendMessage(ChatColor.YELLOW + SLOW_NAME + " is no longer active.");
 			}
 			else
 			{
 				DUtil.setDeityData(username, DEITYNAME, SLOW_NAME, true);
 				player.sendMessage(ChatColor.YELLOW + SLOW_NAME + " is now active.");
 			}
 		}
 	}
 
 	// The actual ability command
 	public static void slow(Player player)
 	{
 		// Define variables
 		String username = player.getName();
 		int devotion = DUtil.getDevotion(username, DEITYNAME);
 		int duration = (int) Math.ceil(3.635 * Math.pow(devotion, 0.2576)); //seconds
 		int strength = (int) Math.ceil(2.757 * Math.pow(devotion, 0.097));
 		Player target = null;
 		Block block = player.getTargetBlock(null, 200);
 		
 		for (Player onlinePlayer : block.getWorld().getPlayers())
 		{
 			if (onlinePlayer.getLocation().distance(block.getLocation()) < 4)
 			{
 				if (!DUtil.areAllied(onlinePlayer.getName(), username) && DUtil.canPVP(onlinePlayer.getLocation()))
 				{
 					target = onlinePlayer;
 					break;
 				}
 			}
 		}
 		
 		if ((target != null) && (target.getEntityId() != player.getEntityId()))
 		{
 			target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, duration * 20, strength));
 			player.sendMessage(ChatColor.YELLOW + target.getName() + " has been slowed.");
 			target.sendMessage(ChatColor.RED + "You have been slowed for " + duration + " seconds.");
 			
 			// DUtil.setPlayerData(target.getName(), "slow", duration);
 		}
 		else
 		{
 			player.sendMessage(ChatColor.YELLOW + "No target found.");
 		}
 	}
 
 	/*
 	 *  Command: "/timestop"
 	 */
 	@ReflectCommand.Command(name = "timestop", sender = ReflectCommand.Sender.PLAYER, permission = "demigods." + DEITYALLIANCE + "." + DEITYNAME + ".ultimate")
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
 			
 			int duration = (int) Math.round(9.9155621 * Math.pow(DUtil.getAscensions(username), 0.459019));
 			player.sendMessage(ChatColor.YELLOW + "Cronus has stopped time for " + duration + " seconds, for " + timestop(player, duration) + " enemies!");
 
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
 	public static int timestop(Player player, int duration)
 	{
 		// Define variables
 		String username = player.getName();
 		int slowamount = (int)Math.round(4.77179 * Math.pow(DUtil.getAscensions(username), 0.17654391));
 		int count = 0;
 		
 		for(Player onlinePlayer : player.getWorld().getPlayers())
 		{
 			if(!(onlinePlayer.getLocation().toVector().isInSphere(player.getLocation().toVector(), 70))) continue;
 			
 			if(!DUtil.canPVP(onlinePlayer.getLocation())) continue;
 			
 			if (DUtil.isImmortal(onlinePlayer.getName()) && DUtil.areAllied(username, onlinePlayer.getName())) continue;
 
 			onlinePlayer.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, duration * 20, slowamount));
 			
 			//DUtil.setPlayerDamage(username, "timestop", duration);
 			
 			count++;
 		}
 		
 		return count;
 	}
 	
 	// Don't touch these, they're required to work.
 	public String getName() { return DEITYNAME; }
 	public String getAlliance() { return DEITYALLIANCE; }
 }
