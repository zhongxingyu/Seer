 package me.botsko.dhmcdeath;
 
 
 /**
  * dhmcDeath
  * 
  * The lively community of the Darkhelmet server (s.dhmc.us) have been using HeroicDeath since we began.
  * However our needs have grown and while our players really enjoy the death messages we've come up
  * with, it's time to improve.
  * 
  * Get Help:
  * irc.esper.net #dhmc_us
  * 
  * FEATURES
  * - Tons of great messages out-of-the-box.
  * - Easy configuration of death messages. Similar syntax as HeroicDeath for easy conversion.
  * - Use generic mob messages or add them for specific mob types
  * - Custom colors allowed in messages.
  * - Disable message for specific events, i.e. show mob death messages but ignore PVP
  * - Optionally limit messages by world
  * - Optionally limit the message to players within a range of the death
  * - Shows owners of tamed wolves
  * - Supports all 1.1 Minecraft mobs
  * 
  * Version 0.1
  * - Added configurable death messages for all death types, and mob types
  * - Added color code support
  * - Optional world limit
  * - Optional range limit
  * - Tamed wolf owners are listed too
  * - Per-death-type event disabling
  * Version 0.1.1
  * - Fixed message log left inside a loop
  * - Fixed weapon of "air" reported when using hands
  * Version 0.1.2
  * - Minor change to character in a message
  * Version 0.1.3
  * - Corrected string comparison for "air"
  * Version 0.1.4
  * - Adding config reload command, with basic permission support
  * - Moved debug option to config
  * - Added central logging/debug methods
  * Version 0.1.5
  * - Attempting to fix distance between worlds error (issue #1)
  * Version 0.1.6
  * - Fixed issue with radius being ignored in certain situations
  * - Fixed issue with removed configuration options being re-added
  * - Removed use_hear_distance config, hear distance will be ignored if set to 0
  * - Thanks to napalm1 for testing the radius fix with me.
  * Version 0.1.7
  * - Added /death command that returns a player to their last death
  * - Modified config system
  * - Added config to disallow /death when died in pvp
  * - Added config to disable logging deaths
  * - Changed log to log the death info, not the message
  * 
  * TODO
  * - Allow players to ignore all death messages
  * 
  * @author Mike Botsko (viveleroi aka botskonet) http://www.botsko.net
  * 
  */
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Random;
 import java.util.logging.Logger;
 
 import me.botsko.dhmcdeath.commands.DeathCommandExecutor;
 import me.botsko.dhmcdeath.commands.DhmcdeathCommandExecutor;
 import me.botsko.dhmcdeath.tp.Death;
 
 import org.bukkit.ChatColor;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.Arrow;
 import org.bukkit.entity.Blaze;
 import org.bukkit.entity.CaveSpider;
 import org.bukkit.entity.Creeper;
 import org.bukkit.entity.EnderDragon;
 import org.bukkit.entity.Enderman;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Ghast;
 import org.bukkit.entity.MagmaCube;
 import org.bukkit.entity.PigZombie;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Silverfish;
 import org.bukkit.entity.Skeleton;
 import org.bukkit.entity.Slime;
 import org.bukkit.entity.Spider;
 import org.bukkit.entity.Wolf;
 import org.bukkit.entity.Zombie;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDeathEvent;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.java.JavaPlugin;
 
 /**
  * dhmcDeath
  * @author botskonet
  *
  */
 public class DhmcDeath extends JavaPlugin implements Listener  {
 	
 	Logger log = Logger.getLogger("Minecraft");
 	protected FileConfiguration config;
 	protected HashMap<String,Death> deaths = new HashMap<String,Death>();
 	
 	
 	/**
      * Enables the plugin and activates our player listeners
      */
 	public void onEnable(){
 		
 		this.log("[dhmcDeath]: Initializing.");
 		
 		// Load configuration, or install if new
 		config = DeathConfig.init( this );
 		
 		getServer().getPluginManager().registerEvents(this, this);
 		
 		getCommand("dhmcdeath").setExecutor( (CommandExecutor) new DhmcdeathCommandExecutor(this) );
 		getCommand("death").setExecutor( (CommandExecutor) new DeathCommandExecutor(this) );
 
 	}
 	
 	
 	/**
 	 * Handles the entity death.
 	 * 
 	 * @param event
 	 */
 	@SuppressWarnings("unchecked")
 	@EventHandler(priority = EventPriority.NORMAL)
     public void onEntityDeath(final EntityDeathEvent event) {
 		
 		if (event.getEntity() instanceof Player){
 			
 			// Disable Bukkit death messages
 	        if (event instanceof PlayerDeathEvent) {
 	            PlayerDeathEvent e = (PlayerDeathEvent) event;
 	            e.setDeathMessage(null);
 	        }
   
 	        // Find player who died
 	        Player p = (Player)event.getEntity();
 			
 	        // Determine the cause
 	        String cause = getCauseOfDeath( event, p );
 	        String attacker = getAttacker(event, p);
             
             // Verify death messages are enabled for this type
             if( getConfig().getBoolean("messages."+cause.toLowerCase()+".enabled") ){
             
 	            // Load the death message for this type
 	            List<Object> messages = (List<Object>) getConfig().getList( "messages." + cause.toLowerCase() + ".messages" );
 	            
 	            String final_msg = "";
 	            if(messages != null && !messages.isEmpty()){
 	            	
 	            	// If mob, and mob has specific messages, we need to override the standard mob messages
 		            if(cause == "mob"){
 		            	List<Object> mob_msg = (List<Object>) getConfig().getList( "messages.mob."+attacker.toLowerCase()+".messages" );
 		            	if(mob_msg != null && !mob_msg.isEmpty()){
 		            		messages = mob_msg;
 		            	}
 		            }
 	            	
 	            	Random rand = new Random();
 		            int choice = rand.nextInt(messages.size());
 		            final_msg = (String) messages.get(choice);  
 	            } else {
 	            	messages = (List<Object>) getConfig().getList( "messages.default.messages" );
 	            	Random rand = new Random();
 		            int choice = rand.nextInt(messages.size());
 		            final_msg = (String) messages.get(choice);  
 	            }
 	            
 	            // Build the final message
 	            final_msg = final_msg.replaceAll("%d", p.getName());
 
 	            if(attacker == "pvpwolf"){
 	            	String owner = getTameWolfOwner(event);
 	            	attacker = owner+"'s wolf";
 	            }
 	            final_msg = final_msg.replaceAll("%a", attacker);
 	            final_msg = final_msg.replaceAll("%i", getWeapon(p) );
 	            
 	            // Colorize
 	            final_msg = colorize(final_msg);
 	            
 	            // Store the death data
 	            if(this.deaths.containsKey(p.getName())){
 	            	this.deaths.remove(p.getName());
 	            }
 	            
 	            boolean allow_tp = true;
 	            if(!getConfig().getBoolean("allow_dethpoint_tp_on_pvp")){
 	            	if(cause.equals("pvp")){
 	            		allow_tp = false;
 	            	}
 	            }
 	            Death death = new Death( p.getLocation(), p, p.getWorld(), cause, attacker );
 	            if(allow_tp){
 	            	deaths.put( p.getName(), death );
 	            }
 	            
 	            // Send the final message
 	            if( getConfig().getBoolean("allow_cross_world") && !getConfig().getBoolean("use_hear_distance") ){
 		            for (Player player : getServer().getOnlinePlayers()) {
 			            player.sendMessage( final_msg );
 			            debug("Messaging Player: " + player.getName());
 		    		}
 	            } else {
 	            	
 	            	// Iterate all players within the world
 		            for (Player player : p.getWorld().getPlayers()) {
 		            	double dist = player.getLocation().distance( p.getLocation() );
 		            	debug("Distance for "+ player.getName()+ " is " + dist );
 		            	// Only send message if player is within distance
 		            	if( getConfig().getDouble("messages.hear_distance") == 0 || dist <= getConfig().getDouble("messages.hear_distance") ) {
 			            	player.sendMessage( final_msg );
 			            	debug("Messaging Player: " + player.getName() + " " + dist + " <= " + getConfig().getInt("messages.hear_distance"));
 		            	}
 		    		}
 	            }
 	            if(getConfig().getBoolean("messages.log_deaths")){
 	            	this.log(death.getPlayer().getName() + " died from " + death.getCause() + " (killer: "+death.getAttacker()+") in " + death.getWorld().getName() + " at x:" + death.getLocation().getX() + " y:" + death.getLocation().getY() + " z:" + death.getLocation().getZ() );
 	            }
             } else {
             	debug("Messages are disabled for this cause: " + cause);
             }
 		}
 	}
 	
 	
 	/**
 	 * 
 	 * @return
 	 */
 	public HashMap<String,Death> getDeaths(){
 		return this.deaths;
 	}
 	
 	
 	/**
 	 * Translates the cause of death into a more consistent naming convention we
 	 * can use to relate messages.
 	 * 
 	 * All possible deaths:
 	 * custom
 	 * drowning
 	 * fall
 	 * lava
 	 * lightning
 	 * mob
 	 * poison ?
 	 * pvp
 	 * starvation
 	 * suffocation
 	 * suicide
 	 * void
 	 * 
 	 * http://jd.bukkit.org/apidocs/org/bukkit/event/entity/EntityDamageEvent.DamageCause.html
 	 * 
 	 * @param event
 	 * @param p
 	 * @return String
 	 */
 	private String getCauseOfDeath(EntityDeathEvent event, Player p){
 		
 		// Determine the root cause
         String cause = event.getEntity().getLastDamageCause().getCause().toString();
         debug("Raw Cause: " + cause);
         
         // Detect additional suicide. For example, when you potion
         // yourself with instant damage it doesn't show as suicide.
         if(p.getKiller() instanceof Player){
         	Player killer = p.getKiller();
         	if(killer.getName() == p.getName()){
         		cause = "suicide";
         	}
         }
         
         // translate bukkit events to nicer names
         if(cause == "ENTITY_ATTACK" && p.getKiller() instanceof Player){
         	cause = "pvp";
         }
         if(cause == "ENTITY_ATTACK" && !(p.getKiller() instanceof Player)){
         	cause = "mob";
         }
         if(cause == "PROJECTILE" && !(p.getKiller() instanceof Player)){
         	cause = "mob"; // skeleton
         }
         if(cause == "PROJECTILE" && (p.getKiller() instanceof Player)){
         	cause = "pvp"; // bow and arrow
         }
         if(cause == "ENTITY_EXPLOSION"){
         	cause = "mob"; // creeper
         }
         if(cause == "CONTACT"){
         	cause = "cactus";
         }
         if(cause == "BLOCK_EXPLOSION"){
         	cause = "tnt";
         }
         if(cause == "FIRE" || cause == "FIRE_TICK"){
         	cause = "fire";
         }
         if(cause == "MAGIC"){
         	cause = "potion";
         }
         
         debug("Parsed Cause: " + cause);
         
         return cause;
 		
 	}
 	
 	
 	/**
 	 * Returns the name of the attacker, whether mob or player.
 	 * 
 	 * @param event
 	 * @param p
 	 * @return
 	 */
 	private String getAttacker(EntityDeathEvent event, Player p){
 		
 		String attacker = "";
 		String cause = getCauseOfDeath(event, p);
         if(p.getKiller() instanceof Player){
         	attacker = p.getKiller().getName();
         } else {
             if(cause == "mob"){
             	
             	Entity killer = ((EntityDamageByEntityEvent)event.getEntity().getLastDamageCause()).getDamager();
             	debug("Entity Was: " + killer);
             	
             	if (killer instanceof Blaze){
             		attacker = "blaze";
             	}
             	if (killer instanceof CaveSpider){
             		attacker = "cave spider";
             	}
             	if (killer instanceof Creeper){
             		attacker = "creeper";
             	}
             	if (killer instanceof EnderDragon){
             		attacker = "ender dragon";
             	}
             	if (killer instanceof Enderman){
             		attacker = "enderman";
             	}
             	if (killer instanceof Ghast){
             		attacker = "ghast";
             	}
             	if (killer instanceof MagmaCube){
             		attacker = "magma cube";
             	}
             	if (killer instanceof PigZombie){
             		attacker = "pig zombie";
             	}
             	if (killer instanceof Silverfish){
             		attacker = "silverfish";
             	}
             	if (killer instanceof Skeleton){
             		attacker = "skeleton";
             	}
             	if (killer instanceof Arrow){
             		attacker = "skeleton";
             	}
             	if (killer instanceof Slime){
             		attacker = "slime";
             	}
             	if (killer instanceof Spider){
             		attacker = "spider";
             	}
             	if (killer instanceof Wolf){
                     Wolf wolf = (Wolf)killer;
                     if(wolf.isTamed()){
                         if(wolf.getOwner() instanceof Player || wolf.getOwner() instanceof OfflinePlayer ){
                             attacker = "pvpwolf";
                         } else {
                         	attacker = "wolf";
                         }
                     } else {
                     	attacker = "wolf";
                     }
             		
             	}
             	if (killer instanceof Zombie){
             		attacker = "zombie";
             	}
             }
         }
         debug("Attacker: " + attacker);
         
         return attacker;
         
 	}
 	
 	
 	/**
 	 * Determines the owner of a tamed wolf.
 	 * 
 	 * @param event
 	 * @return
 	 */
 	private String getTameWolfOwner(EntityDeathEvent event){
 		String owner = "";
 		Entity killer = ((EntityDamageByEntityEvent)event.getEntity().getLastDamageCause()).getDamager();
 		if (killer instanceof Wolf){
             Wolf wolf = (Wolf)killer;
             if(wolf.isTamed()){
                 if(wolf.getOwner() instanceof Player){
                     owner = ((Player)wolf.getOwner()).getName();
                 }
                 if(wolf.getOwner() instanceof OfflinePlayer){
                     owner = ((OfflinePlayer)wolf.getOwner()).getName();
                 }
             }
     	}
 		debug("Wolf Owner: " + owner);
 		return owner;
 	}
 	
 	
 	/**
 	 * Determines the weapon used.
 	 * 
 	 * @param p
 	 * @return
 	 */
 	private String getWeapon(Player p){
 
         String death_weapon = "";
         if(p.getKiller() instanceof Player){
         	ItemStack weapon = p.getKiller().getItemInHand();
         	death_weapon = weapon.getType().toString().toLowerCase();
         	death_weapon = death_weapon.replaceAll("_", " ");
         	if(death_weapon.equalsIgnoreCase("air")){
         		death_weapon = " hands";
         	}
         }
         debug("Weapon: " + death_weapon );
         
         return death_weapon;
         
 	}
 	
 	
 	/**
 	 * Converts colors place-holders.
 	 * @param text
 	 * @return
 	 */
 	private String colorize(String text){
         String colorized = text.replaceAll("(&([a-f0-9A-F]))", "\u00A7$2");
         return colorized;
     }
 	
 	
 	/**
 	 * 
 	 * @param msg
 	 * @return
 	 */
 	public String playerMsg(String msg){
 		return ChatColor.GOLD + "[dhmcDeath]: " + ChatColor.WHITE + msg;
 	}
 	
 	
 	/**
 	 * 
 	 * @param msg
 	 * @return
 	 */
 	public String playerError(String msg){
 		return ChatColor.GOLD + "[dhmcDeath]: " + ChatColor.RED + msg;
 	}
 	
 	
 	/**
 	 * 
 	 * @param message
 	 */
 	public void log(String message){
		log.info("[dhmcDeath}]: "+message);
 	}
 	
 	
 	/**
 	 * 
 	 * @param message
 	 */
 	public void debug(String message){
 		if(this.getConfig().getBoolean("debug")){
 			this.log("[dhmcDeath]: " + message);
 		}
 	}
 
 
 	/**
 	 * Disables plugin
 	 */
 	@Override
 	public void onDisable() {
 		this.log("[dhmcDeath]: Shutting down.");
 	}
 }
