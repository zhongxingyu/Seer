 package me.asofold.bukkit.fattnt;
 
 import java.io.File;
 import java.util.Collection;
 import java.util.List;
 
 import me.asofold.bukkit.fattnt.config.Defaults;
 import me.asofold.bukkit.fattnt.config.ExplosionSettings;
 import me.asofold.bukkit.fattnt.config.Settings;
 import me.asofold.bukkit.fattnt.config.compatlayer.CompatConfig;
 import me.asofold.bukkit.fattnt.config.compatlayer.NewConfig;
 import me.asofold.bukkit.fattnt.effects.DamageProcessor;
 import me.asofold.bukkit.fattnt.effects.ExplosionManager;
 import me.asofold.bukkit.fattnt.events.FatExplodeEvent;
 import me.asofold.bukkit.fattnt.propagation.Propagation;
 import me.asofold.bukkit.fattnt.propagation.PropagationFactory;
 import me.asofold.bukkit.fattnt.scheduler.ProcessHandler;
 import me.asofold.bukkit.fattnt.scheduler.ScheduledEntitySpawn;
 import me.asofold.bukkit.fattnt.scheduler.ScheduledExplosion;
 import me.asofold.bukkit.fattnt.scheduler.ScheduledItemSpawn;
 import me.asofold.bukkit.fattnt.scheduler.SchedulerSet;
 import me.asofold.bukkit.fattnt.stats.Stats;
 import me.asofold.bukkit.fattnt.utils.Utils;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Item;
 import org.bukkit.entity.TNTPrimed;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityCombustEvent;
 import org.bukkit.event.entity.EntityExplodeEvent;
 import org.bukkit.event.entity.ExplosionPrimeEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.scheduler.BukkitScheduler;
 
 /**
  * Experimental plugin to replace explosions completely.
  * 
  * @author mc_dev
  * @license See project folder, either LICENSE.TXT or fattnt.lists.
  *
  */
 public class FatTnt extends JavaPlugin implements Listener {
 	
 	public static final boolean DEBUG = false;
 	public static final boolean DEBUG_LOTS = false;
 	
 	private static final Stats stats = new Stats(Defaults.msgPrefix.trim()+"[STATS]");
 	
 	// Calculate one explosion
 	public static final Integer statsExplosion = stats.getNewId("explosion");
 	public static final Integer statsGetBlocks = stats.getNewId("get_blocks");
 	public static final Integer statsExplodeEvent = stats.getNewId("event_explode");
 	public static final Integer statsApplyBlocks = stats.getNewId("apply_blocks");
 	public static final Integer statsNearbyEntities = stats.getNewId("nearby_entities");
 	public static final Integer statsApplyEntities = stats.getNewId("apply_entities");
 	// Counts for an explosion:
 	public static final Integer statsStrength = stats.getNewId("strength");
 	public static final Integer statsBlocksVisited = stats.getNewId("blocks_visited");
 	public static final Integer statsBlocksCollected = stats.getNewId("blocks_collected");
 	public static final Integer statsDamage = stats.getNewId("damage");
 	// Scheduling of explosions:
 	public static final Integer statsProcessExpl = stats.getNewId("sched_proc_expl");
 	public static final Integer statsNExpl = stats.getNewId("sched_n_expl");
 	public static final Integer statsNExplStore = stats.getNewId("sched_store_expl");
 	// Scheduling of spawning tnt:
 	public static final Integer statsProcessEnt = stats.getNewId("sched_proc_ent");
 	public static final Integer statsNEnt = stats.getNewId("sched_n_ent");
 	public static final Integer statsNEntStore = stats.getNewId("sched_store_ent");
 	// Scheduling of spawning items:
 	public static final Integer statsProcessItem = stats.getNewId("sched_proc_item");
 	public static final Integer statsNItem = stats.getNewId("sched_n_item");
 	public static final Integer statsNItemStore = stats.getNewId("sched_store_item");
 	
 	
 	static {
 		stats.setLogStats(DEBUG);
 		ExplosionManager.setStats(stats);
 	}
 	
 //	ExplosionPrimeEvent waitingEP = null;
 	
 	private final Settings settings = new Settings(stats);
 	private DamageProcessor damageProcessor = new DamageProcessor();
 	
 	private Propagation propagation = null;
 	
 	private int taskIdScheduler = -1;
 	
 	private final SchedulerSet schedulers = new SchedulerSet();
 	
 	private final ProcessHandler<ScheduledExplosion> explosionHandler = new ProcessHandler<ScheduledExplosion>() {
 		@Override
 		public final void process(final ScheduledExplosion explosion) {
 			createExplosion(explosion.world, explosion.x, explosion.y, explosion.z, explosion.getRadius(), explosion.isFire(), explosion.getExplEntity(), explosion.getEntityType());
 		}
 	};
 	
 	private final ProcessHandler<ScheduledEntitySpawn> entityHandler = new ProcessHandler<ScheduledEntitySpawn>() {
 		@Override
 		public final void process(final ScheduledEntitySpawn spawnEnt) {
 			spawnEnt.spawn();
 		}
 	};
 	
 	private final ProcessHandler<ScheduledItemSpawn> itemHandler = new ProcessHandler<ScheduledItemSpawn>() {
 		@Override
 		public final void process(final ScheduledItemSpawn spawnItem) {
 			spawnItem.spawn();
 		}
 	};
 	
 	public FatTnt(){
 		super();
 	}
 	
 	public void checkSchedulers(){
 		// TODO: in case of halted: halt ...
 		if (taskIdScheduler != -1) return;
 		if (!schedulers.hasEntries()) return;
 		taskIdScheduler = getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
 			@Override
 			public void run() {
 				boolean needRun = false;
 				// TODO: consider order ! 
 				if (schedulers.explosions.onTick(explosionHandler, stats, statsProcessExpl, statsNExpl, statsNExplStore)) needRun = true;
 				if (schedulers.spawnEntities.onTick(entityHandler, stats, statsProcessEnt, statsNEnt, statsNEntStore)) needRun = true;
 				if (schedulers.spawnItems.onTick(itemHandler, stats, statsProcessItem, statsNItem, statsNItemStore)) needRun = true;
 				// TODO: other schedulers.
 				if (!needRun){
 					getServer().getScheduler().cancelTask(taskIdScheduler);
 					taskIdScheduler = -1;
 				}
 			}
 		}, 1, 1);
 		if (taskIdScheduler == -1){
 			getLogger().severe("[FatTnt] Failed to schedule the scheduler task, clear all waiting explosions!");
 			schedulers.clear();
 		}
 	}
 	
 	@Override
 	public void onEnable() {
 		reloadSettings();
 		getServer().getPluginManager().registerEvents(this, this);
 		System.out.println(Defaults.msgPrefix+getDescription().getFullName()+" is enabled.");
 	}
 	
 	@Override
 	public void onDisable() {
 		taskIdScheduler = -1;
 		// TODO: scheduler.clear ?
 		System.out.println(Defaults.msgPrefix+getDescription().getFullName()+" is disabled.");
 	}
 	
 	@Override
 	public boolean onCommand(CommandSender sender, Command command,
 			String label, String[] args) {
 		label = label.toLowerCase();
 		if ( !label.equals("fattnt") && !label.equals("ftnt")) return false;
 		int len = args.length;
 		String cmd = null;
 		if (len > 0) cmd = args[0].trim().toLowerCase();
 		if (len==1 && cmd.equals("reload")){
 			if ( !Utils.checkPerm(sender, "fattnt.cmd.reload")) return true;
 			reloadSettings();
 			Utils.send(sender, "Settings reloaded.");
 			return true;
 		} 
 		else if (len==1 && cmd.equals("enable")){
 			if ( !Utils.checkPerm(sender, "fattnt.cmd.enable")) return true;
 			settings.setHandleExplosions(true);
 			Utils.send( sender, "Explosions will be handled by FatTnt."); 
 			return true;
 		}
 		else if (len==1 && cmd.equals("disable")){
 			if ( !Utils.checkPerm(sender, "fattnt.cmd.disable")) return true;
 			settings.setHandleExplosions(false);
 			Utils.send( sender, "Explosions are back to default behavior (disregarding other plugins)."); 
 			return true;
 		}
 		else if (len==1 && (cmd.equals("stats") || args[0].equalsIgnoreCase("st"))){
 			if ( !Utils.checkPerm(sender, "fattnt.cmd.stats.see")) return true;
 			Utils.send(sender, stats.getStatsStr(true), false);
 			return true;
 		}
 		else if (len==2 && (cmd.equals("stats") || args[0].equalsIgnoreCase("st")) && args[1].equalsIgnoreCase("reset")){
 			if ( !Utils.checkPerm(sender, "fattnt.cmd.stats.reset")) return true;
 			stats.clear();
 			Utils.send(sender, "Stats reset.");
 			return true;
 		}
 		else if (len == 1 && cmd.equals("panic")){
 			if ( !Utils.checkPerm(sender, "fattnt.cmd.panic")) return true;
 			onPanic();
 			sender.sendMessage("[FatTnt] Removed all primed tnt and scheduled explosions, prevent explosions.");
 			return true;
 		}
 		else if (len == 1 && cmd.equals("unpanic")){
 			if ( !Utils.checkPerm(sender, "fattnt.cmd.unpanic")) return true;
 			settings.setPreventExplosions(false);
 			sender.sendMessage("[FatTnt] Allowing explosions.");
 			return true;
 		}
 		return false;
 	}
 
 	private void onPanic() {
 		settings.setPreventExplosions(true);
 		removeAllPrimedTnt();
 		schedulers.clear();
 	}
 
 	public static void removeAllPrimedTnt() {
 		for (World world : Bukkit.getServer().getWorlds()){
 			Collection<TNTPrimed> primed = world.getEntitiesByClass(TNTPrimed.class);
 			if (primed == null) continue; // TODO: remove this.
 			for (TNTPrimed tnt : primed){
 				tnt.remove();
 			}
 		}
 	}
 
 	/**
 	 * Reload and apply settings from the default configuration file.
 	 * (uses applySettings)
 	 */
 	public void reloadSettings() {
 		BukkitScheduler sched = getServer().getScheduler();
 		sched.cancelTasks(this);
 		taskIdScheduler = -1;
 		File file = new File (getDataFolder(), "config.yml");
 		boolean exists = file.exists();
 		CompatConfig cfg = new NewConfig(file);
 		cfg.load();
 		boolean changed = Defaults.addDefaultSettings(cfg);
 		if (!exists || changed) cfg.save();
 		applySettings(cfg);
 		sched.scheduleSyncRepeatingTask(this, new Runnable(){
 			@Override
 			public void run() {
 				onIdle();
 			}
 		}, 217, 217); // TODO ?
 		schedulers.fromConfig(cfg);
 		checkSchedulers();
 	}
 	
 	private void onIdle() {
 		propagation.onIdle();
 	}
 	
 	/**
 	 * Apply the settings.
 	 * @param cfg
 	 */
 	public void applySettings(CompatConfig cfg){
 		settings.applyConfig(cfg);
 		// TODO: propagation pbased on config (Factory)
 		propagation = PropagationFactory.getPropagation(settings);
 	}
 
 	/**
 	 * API
 	 * @param damageProcessor
 	 */
 	public void setDamageProcessor(DamageProcessor damageProcessor) {
 		this.damageProcessor = damageProcessor;
 	}
 
 	@EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
 	final void onExplosionPrime(final ExplosionPrimeEvent event){
 		final Entity explEntity = event.getEntity();
 		final Location loc = explEntity.getLocation();
 		final World world = loc.getWorld();
 		final String worldName = world.getName();
 		final EntityType entityType = (explEntity == null) ? null:explEntity.getType();
 		if (settings.preventsExplosions(worldName, entityType)){
 			event.setCancelled(true);
 			return;
 		}
		if (!settings.handlesExplosions(worldName, entityType)){
			if (settings.preventsOtherExplosions(worldName, entityType)){
				event.setCancelled(true);
				return;
			}
			return;
		}
 		// do prepare to handle this explosion:
 		event.setCancelled(true);
 		if (!explEntity.isDead()) explEntity.remove();
 		final ExplosionSettings explSettings = settings.getApplicableExplosionSettings(worldName, Utils.usedEntityType(explEntity, entityType));
 		if (explSettings.scheduleExplosions){
 			schedulers.explosions.addEntry(new ScheduledExplosion(world, loc.getX(), loc.getY(), loc.getZ(), event.getRadius(), event.getFire(), explEntity, entityType));
 			checkSchedulers();
 		}
 		else createExplosion(world, loc.getX(), loc.getY(), loc.getZ(), event.getRadius(), event.getFire(), explEntity, entityType);
 	}
 	
 	@EventHandler(priority=EventPriority.LOWEST, ignoreCancelled=true)
 	final void onEntityExplode(final EntityExplodeEvent event){
 		final Location loc = event.getLocation();
 		final World world = loc.getWorld();
 		final String worldName = world.getName();
 		final Entity entity = event.getEntity();
		if (settings.preventsExplosions(worldName, (entity==null)?null:entity.getType())){
 			event.setCancelled(true);
 			return;
 		}
 		if (event instanceof FatExplodeEvent) return; // do not handle these
		if (settings.preventsOtherExplosions(worldName, (entity==null)?null:entity.getType())){
			event.setCancelled(true);
			return;
		}
 	}
 	
 	@EventHandler(priority=EventPriority.HIGHEST)
 	final void onEntityCombust(final EntityCombustEvent event){
 		// TODO:
 		if (event.isCancelled()) return;
 		final Entity entity = event.getEntity();
 		if ( !(entity instanceof Item)) return;
 		final Item item = (Item) entity;
 		final ItemStack stack = item.getItemStack();
 		if ( stack.getType() != Material.TNT) return;
 		final Location loc = entity.getLocation();
 		ExplosionSettings es = settings.getApplicableExplosionSettings(loc.getWorld().getName(), null);
 		if (!es.itemTnt || es.preventExplosions) return;
 		event.setCancelled(true);
 		// TODO might have to be scheduled
 		if (es.scheduleEntities) schedulers.spawnEntities.addEntry(ExplosionManager.getScheduledTnt(item));
 		else ExplosionManager.replaceByTNTPrimed(item);		
 	}
 	
 	/**
 	 * API
 	 * @param loc
 	 * @param radius As in World.createExplosion
 	 * @param fire
 	 * @param explEntity
 	 * @param entityType May be null, might be unused.
 	 */
 	public void createExplosion(Location loc, float radius, boolean fire, Entity explEntity, EntityType entityType){
 		World world = loc.getWorld();
 		double x = loc.getX();
 		double y = loc.getY();
 		double z = loc.getZ();
 		createExplosion(world, x, y, z, radius, fire, explEntity, entityType);
 	}
 	
 	/**
 	 * API
 	 * (called from event handling as well)
 	 * @param world
 	 * @param x
 	 * @param y
 	 * @param z
 	 * @param radius As in World.createExplosion
 	 * @param fire
 	 * @param explEntity Not sure what happens if this is null: the events use this.
 	 * @param entityType May be null, might be unused.
 	 */
 	public void createExplosion(World world, double x, double y, double z, float radius, boolean fire, Entity explEntity, EntityType entityType){
 		// create a fake explosion
 		ExplosionManager.createExplosionEffect(world, x, y, z, radius, fire);
 		if (radius==0.0f) return;
 		// calculate effects
 		// WORKAROUND:
 		ExplosionSettings explSettings = settings.getApplicableExplosionSettings(world.getName(), Utils.usedEntityType(explEntity, entityType));
 		float realRadius = radius*explSettings.radiusMultiplier;
 		List<Entity> nearbyEntities;
 		long ms = System.nanoTime();
 		if (explEntity==null) nearbyEntities = Utils.getNearbyEntities(world, x,y,z, realRadius*explSettings.entityRadiusMultiplier);
 		else nearbyEntities = explEntity.getNearbyEntities(realRadius, realRadius, realRadius*explSettings.entityRadiusMultiplier);
 		stats.addStats(statsNearbyEntities, System.nanoTime()-ms);
 		applyExplosionEffects(world, x, y, z, realRadius, fire, explEntity, entityType, nearbyEntities);
 	}
 
 	/**
 	 * API
 	 * @param loc
 	 * @param radius As in World.createExplosion
 	 * @param fire
 	 */
 	public void createExplosion(Location loc, float radius, boolean fire){
 		createExplosion(loc, radius, fire, null, null);
 	}
 
 	/**
 	 * API
 	 * @param world
 	 * @param x
 	 * @param y
 	 * @param z
 	 * @param radius As in World.createExplosion
 	 * @param fire
 	 */
 	public void createExplosion(World world, double x, double y, double z, float radius, boolean fire) {
 		createExplosion(world, x, y, z, radius, fire, null, null);
 	}
 	
 	/**
 	 * This method only considers the given entities for damage.
 	 * API
 	 * (used internally)
 	 * (this method uses seqMax, such that it should not get manipulated anywhere but inside of getExplodingBlocks)
 	 * @param world
 	 * @param x
 	 * @param y
 	 * @param z
 	 * @param realRadius intended to be the block distance rather, so that resistance of 1.0 (AIR) would lead to the full range.
 	 * @param fire
 	 * @param entityType Causing the explosion.
 	 * @param nearbyEntities List of entities that can be affected , should be within radius (x,y,z independently), damage depends on settings.
 	 */
 	public void applyExplosionEffects(World world, double x, double y, double z, float realRadius, boolean fire, Entity explEntity, EntityType entityType,
 			List<Entity> nearbyEntities) {
 		applyExplosionEffects(world, x, y, z, realRadius, fire, explEntity, entityType, nearbyEntities, 1.0f);
 	}
 	
 	/**
 	 * This method only considers the given entities for damage, but allows for specification of an extra damage multiplier.
 	 * API
 	 * (used internally)
 	 * (this method uses seqMax, such that it should not get manipulated anywhere but inside of getExplodingBlocks)
 	 * @param world
 	 * @param x
 	 * @param y
 	 * @param z
 	 * @param realRadius
 	 * @param fire
 	 * @param explEntity
 	 * @param entityType
 	 * @param nearbyEntities
 	 * @param damageMultiplier
 	 */
 	public void applyExplosionEffects(World world, double x, double y, double z, float realRadius, boolean fire, Entity explEntity, EntityType entityType,
 			List<Entity> nearbyEntities, float damageMultiplier) {
 		long ns = System.nanoTime();
 		if (ExplosionManager.applyExplosionEffects(world, x, y, z, realRadius, fire, explEntity, entityType, nearbyEntities, damageMultiplier, settings.getApplicableExplosionSettings(world.getName(), Utils.usedEntityType(explEntity, entityType)), propagation, damageProcessor, schedulers)){
 			checkSchedulers(); // simplified.
 		}
 		stats.addStats(statsExplosion, System.nanoTime()-ns);
 	}
 
 	/**
 	 * Get exploding blocks for a world, explosion center, explosion strength.
 	 * This will use the default propagation model.
 	 * This does not change the world in any way, it just collects the block. However it changes FatTnt internals.
 	 * NEVER CALL THIS ASYNCHRONOUSLY OR DURING EVENT PROCESSING (ExplosionPrime, EntityDamageEvent, EntityExplodeEvent).
 	 * Command based access should be ok, for instance.
 	 * (API)
 	 * (Used internally)
 	 * @param world
 	 * @param cx Center ...
 	 * @param cy
 	 * @param cz
 	 * @param realRadius
 	 * @return
 	 */
 	public List<Block> getExplodingBlocks(World world, double cx, double cy,
 			double cz, float realRadius) {
 		return propagation.getExplodingBlocks(world, cx, cy, cz, realRadius, settings.getApplicableExplosionSettings(world.getName(), null));
 	}
 	
 	/**
 	 * This only should be called after an explosion has occurred, 
 	 * or after getExplodinBlocks or any of the createExplosion methods has been called.
 	 * (API)
 	 * @param loc
 	 * @return
 	 */
 	public final float getExplosionStrength(final Location loc){
 		return propagation.getStrength(loc);
 	}
 	
 	/**
 	 * This only should be called after an explosion has occurred, 
 	 * or after getExplodinBlocks or any of the createExplosion methods has been called.
 	 * (API)
 	 * @param x
 	 * @param y
 	 * @param z
 	 * @return
 	 */
 	public final float getExplosionStrength(double x, double y, double z){
 		return propagation.getStrength(x, y, z);
 	}
 	
 	/**
 	 * Convenience.
 	 * (API)
 	 * @return
 	 */
 	public static FatTnt getInstance(){
 		Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("FatTnt");
 		if ( plugin instanceof FatTnt ) return  (FatTnt) plugin;
 		else return null;
 	}
 	
 	/**
 	 * Get the default settings in use.
 	 * HANDLE WITH CARE, DO NOT MANIPULATE.
 	 * (API)
 	 * @return
 	 */
 	public Settings getSettings(){
 		return settings;
 	}
 	
 }
