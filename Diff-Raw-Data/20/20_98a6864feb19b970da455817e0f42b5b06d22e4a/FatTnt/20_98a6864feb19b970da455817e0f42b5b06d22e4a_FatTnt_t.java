 package me.asofold.bukkit.fattnt;
 
 import java.io.File;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Random;
 import java.util.Set;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.Server;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.Configuration;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Item;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.TNTPrimed;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 import org.bukkit.event.entity.EntityExplodeEvent;
 import org.bukkit.event.entity.ExplosionPrimeEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.util.Vector;
 
 /**
  * Experimental plugin to replace explosions completely.
  * 
  * Features: 
  * - select what entities are handled (tnt, creeper?)
  * - configure: block resistance (strength)
  * - modifiers for explosion radius and damage
  * - Let explosions go through blocks without destruction (configurable, defaults to: lava, water, bedrock, other).
  * - Fires EntityExplodeEvent and EntityDamageEvent and allows canceling by other plugins.
  * - API might be used by other events to trigger explosions.
  * 
  * 
  * Issues:
 * ! maybe do remove the entity before the explosion [check other code / CB ?]
  * ! Sand/TNT spazzing: First set blocks without physics, then later apply physics [physics might be scheduled to the next tick ...]?
  * ! Must add: make affected TNT-blocks primed !
  * ! Must add: fire, if fire is set.
  * ! The Shape of the explosion is not optimal:
  *      - Attempt made: Penalty for propagation in the same direction as before.
  *      - Distance map
  *      - Direction map (precalculated, less realistic, more memory, faster)
  *      - Diagonal propagation (diag only, with penalty.)
  * ! adjust default values to something realistic...
  * ! Re-think event priorities and canceling to allow other plugins canceling the ExplosionPrimeEvent as well [Probably ok with highest].
  * ! Re-think which events to intercept: [Currently for performance reason TNTPrimed is canceled always, to prevent calculations for the explosion being done by CraftBukkit or MC] 
  * ? Allow arbitrary strength (but limit radius) ?
  * 
  * Planned:
 * ! add configuration flag for enabled (need not be changed by commands).
  * ! Damage entities according to strength values of the array.
  * ? Damage or change  entities according to their type (TNT->Explode, ItemStacks - damage)
  * ! More fine grained vector manipulation 
  * ! Use velocity events where possible !
  * 
  * Maybe:
  * ? More custom settings to allow for adding fire or whatever to certain or all explosions.
  * ! Fire custom event allowing for adjusting manipulations in a more fine grained way (!)
  * ? configure maximum radius to be handled and if to be aborted otherwise.
  * ? propagate explosion slowly ?
  * ? Schedule TNT option (limit to n per tick)
  * 
  * @author mc_dev
  *
  */
 public class FatTnt extends JavaPlugin implements Listener {
 	
 	// config
 	public static final String cfgMultRadius = "multiplier.radius";
 	public static final String cfgMultDamage = "multiplier.damage";
 	public static final String cfgIgnore = "ignore-blocks";
 	public static final String cfgInvertIgnored= "invert-ignored";
 	public static final String cfgResistence = "resistence";
 	public static final String cfgDefaultResistence = "resistence.default";
 	public static final String cfgMaxRadius = "radius.max";
 	public static final String cfgRandRadius = "radius.random";
 	public static final String cfgEntities= "entities";
 	public static final String cfgYield = "yield";
 	public static final String cfgVelUse = "velocity.use";
 	public static final String cfgVelMin = "velocity.min";
 	public static final String cfgVelCen= "velocity.center";
 	public static final String cfgVelRan = "velocity.random";
 	public static final String cfgFStraight = "multiplier.straight";
 	
 	int[] defaultIgnoreBlocks = new int[]{
 //			7, // bedrock
 			8,9, // water
 			10,11, // lava
 //			49,90, // obsidian/nether portal
 //			119,120 // end portal / frame
 			};
 	
 	int[] defaultLowResistance = new int[]{
 			0, // air
 			8, 18, 30, 31, 32, 37,38, 39, 40, 50, 51, 55,
 			59,	63, 75,76, 78, 83, 102, 104, 105, 106, 111,
 	};
 	
 	int[] defaultHigherResistance = new int[]{
 			1, 4, 22, 23, 41,42,45, 44, 48, 54, 57, 
 			98, 108, 109, 112, 95
 	};
 	
 	int[] defaultStrongResistance = new int[]{
 			49, 116, 
 	};
 	
 	int[] defaultMaxResistance = new int[]{
 			7, // bedrock
 	};
 	
 //	/**
 //	 * opposite direction:
 //	 * 0:  no direction
 //	 * 1:  reserved: diagonal
 //	 * 2:  x+
 //	 * 3:  reserved: diagonal
 //	 * 4:  x-
 //	 * 5:  reserved: diagonal
 //	 * 6:  y+
 //	 * 7:  reserved: diagonal
 //	 * 8:  y-
 //	 * 9:  reserved: diagonal
 //	 * 10: z+
 //	 * 11: reserved: diagonal
 //	 * 12: z-
 //	 */
 //	private final static int[] oDir = new int[]{
 //		0,  // 0: no direction maps to no direction
 //		0,  // UNUSED
 //		4,  // x+ -> x-
 //		0,  // UNUSED
 //		2,  // x- -> x+
 //		0,  // UNUSED
 //		8,  // y+ -> y-
 //		0,  // UNUSED
 //		6,  // y- -> y+
 //		0,  // UNUSED
 //		12, // z+ -> z-
 //		0,  // UNUSED
 //		10, // z- -> z+
 //	} ;
 	
 	// other
 	/**
 	 * To put in front of messages.
 	 */
 	public static final String msgPrefix = "[FatTnt] ";
 	
 	/**
 	 * Handle and alter explosions
 	 */
 	boolean handleExplosions = true;
 	
 	final Set<EntityType> handledEntities = new HashSet<EntityType>();
 	
 	float maxRadius = 20.0f;
 	public static final float radiusLock = 100.0f; 
 	
 	float radiusMultiplier = 4.0f;
 	
 	float damageMultiplier = 5.0f;
 	
 	float defaultResistance = 2.0f;
 	
 	float fStraight = 0.85f;
 	
 	boolean invertIgnored = false;
 	
 	float randDec = 0.2f;
 	/**
 	 * If to not apply damage to primed tnt.
 	 */
 	boolean sparePrimed = true;
 	
 	ExplosionPrimeEvent waitingEP = null;
 	
 	int[] sequence = null;
 	float[] strength = null;
 	int seqMax = 0;
 	
 	int center = -1;
 	int fY = 0;
 	int fZ = 0;
 	int izMax = 0;
 	
 	boolean velUse = true;
 	float velMin = 0.2f;
 	float velCen = 1.0f;
 	float velRan = 0.5f;
 	
 	boolean[] ignore = new boolean[4096];
 	float[] resistance = new float[4096];
 
 	private float defaultYield = 0.2f;
 	
 	private final Random random = new Random(System.currentTimeMillis()-1256875);
 	
 	final Vector vCenter = new Vector(0.5,0.5,0.5); 
 	
 	public FatTnt(){
 		handledEntities.add(EntityType.PRIMED_TNT);
 	}
 	
 	@Override
 	public void onEnable() {
 		reloadSettings();
 		getServer().getPluginManager().registerEvents(this, this);
 		System.out.println(msgPrefix+getDescription().getFullName()+" is enabled.");
 	}
 	
 	@Override
 	public void onDisable() {
 		System.out.println(msgPrefix+getDescription().getFullName()+" is disabled.");
 	}
 	
 	@Override
 	public boolean onCommand(CommandSender sender, Command command,
 			String label, String[] args) {
 		label = label.toLowerCase();
 		if ( !label.equals("fattnt")) return false;
 		int len = args.length;
 		if (len==1 && args[0].equalsIgnoreCase("reload")){
 			if ( !checkPerm(sender, "fattnt.cmd.reload")) return true;
 			reloadSettings();
 			send(sender, "Settings reloaded.");
 			return true;
 		} 
 		else if (len==1 && args[0].equalsIgnoreCase("enable")){
 			if ( !checkPerm(sender, "fattnt.cmd.enable")) return true;
 			setHandleExplosions(true);
 			send( sender, "Explosions will be handled by FatTnt."); 
 			return true;
 		}
 		else if (len==1 && args[0].equalsIgnoreCase("disable")){
 			if ( !checkPerm(sender, "fattnt.cmd.disable")) return true;
 			setHandleExplosions(false);
 			send( sender, "Explosions are back to default behavior (disregarding other plugins)."); 
 			return true;
 		}
 		return false;
 	}
 
 	public static final boolean hasPermission(CommandSender sender, String perm){
 		return sender.isOp() || sender.hasPermission(perm);
 	}
 	
 	public final static boolean checkPerm(CommandSender sender, String perm){
 		if (hasPermission(sender, perm)) return true;
 		send(sender, "You are missing the permission: "+perm);
 		return false;
 	}
 	
 	public static void send ( CommandSender sender, String message){
 		sender.sendMessage(msgPrefix+message);
 	}
 	
 	public void setHandleExplosions(boolean handle){
 		handleExplosions = handle;
 		// TODO: maybe save to some configuration file ?
 	}
 	
 	public void reloadSettings() {
 		File file = new File (getDataFolder(), "config.yml");
 		boolean exists = file.exists();
 		if ( exists) reloadConfig();
 		FileConfiguration cfg = getConfig();
 		if (addDefaultSettings(cfg)) saveConfig();
 		else if (!exists) saveConfig();
 		applySettings(cfg);
 	}
 	
 	/**
 	 * 
 	 * @param cfg
 	 * @return If changes were done.
 	 */
 	boolean addDefaultSettings(FileConfiguration cfg) {
 		boolean changed = false;
 		if ( !cfg.contains(cfgEntities)){
 			List<String> l = new LinkedList<String>();
 			l.add("PRIMED_TNT");
 			cfg.set(cfgEntities, l);
 			changed = true;
 		}
 		if ( !cfg.contains(cfgIgnore)){
 			List<Integer> l = new LinkedList<Integer>();
 			for (int i : defaultIgnoreBlocks){
 				l.add(i);
 			}
 			cfg.set(cfgIgnore, l);
 			changed = true;
 		}
 		if ( !cfg.contains(cfgInvertIgnored)){
 			cfg.set(cfgInvertIgnored, false);
 			changed = true;
 		}
 		if ( !cfg.contains(cfgResistence)){
 			float[] v = new float[]{1.0f, 4.0f, 20.0f, Float.MAX_VALUE};
 			int[][] ids = new int[][]{defaultLowResistance, defaultHigherResistance, defaultStrongResistance, defaultMaxResistance};
 			String[] keys = new String[]{"low", "higher", "strongest", "indestructible"};
 			for ( int i = 0; i<v.length; i++){
 				String base = cfgResistence+"."+keys[i];
 				List<Integer> l = new LinkedList<Integer>();
 				for ( int id: ids[i]) {
 					l.add(id);
 				}
 				cfg.set(base+".value", v[i]);
 				cfg.set(base+".ids", l);
 			}
 			changed = true;
 		}
 		if ( !cfg.contains(cfgDefaultResistence)){
 			cfg.set(cfgDefaultResistence, 2.0);
 			changed = true;
 		}
 		if ( !cfg.contains(cfgMaxRadius)){
 			cfg.set(cfgMaxRadius, 20.0);
 			changed = true;
 		}
 		if ( !cfg.contains(cfgMultDamage)){
 			cfg.set(cfgMultDamage, 7.0);
 			changed = true;
 		}
 		if ( !cfg.contains(cfgMultRadius)){
 			cfg.set(cfgMultRadius, 2.0);
 			changed = true;
 		}
 		if ( !cfg.contains(cfgRandRadius)){
 			cfg.set(cfgRandRadius, 0.2);
 			changed = true;
 		}
 		if ( !cfg.contains(cfgYield)){
 			cfg.set(cfgYield, 0.2);
 			changed = true;
 		}
 		if ( !cfg.contains(cfgVelUse)){
 			cfg.set(cfgVelUse, true);
 			changed = true;
 		}
 		if ( !cfg.contains(cfgVelMin)){
 			cfg.set(cfgVelMin, 0.2);
 			changed = true;
 		}
 		if ( !cfg.contains(cfgVelCen)){
 			cfg.set(cfgVelCen, 3.0);
 			changed = true;
 		}
 		if ( !cfg.contains(cfgVelRan)){
 			cfg.set(cfgVelRan, 1.5);
 			changed = true;
 		}
 		if ( !cfg.contains(cfgFStraight)){
 			cfg.set(cfgFStraight, 0.85);
 			changed = true;
 		}
 		return changed;
 	}
 
 	void applySettings(Configuration cfg){
 		handledEntities.clear();
 		for ( String n : cfg.getStringList(cfgEntities)){
 			try{
 				EntityType etp = EntityType.valueOf(n.toUpperCase());
 				if ( etp == null) throw new IllegalArgumentException();
 				handledEntities.add(etp);
 			} catch (Throwable t){
 				getServer().getLogger().warning(msgPrefix+"Bad entity: "+n);
 			}
 		}
 		radiusMultiplier = (float) cfg.getDouble(cfgMultRadius);
 		damageMultiplier = (float) cfg.getDouble(cfgMultDamage);
 		invertIgnored = cfg.getBoolean(cfgInvertIgnored);
 		defaultResistance = (float) cfg.getDouble(cfgDefaultResistence);
 		maxRadius = (float) cfg.getDouble(cfgMaxRadius);
 		randDec = (float) cfg.getDouble(cfgRandRadius);
 		defaultYield = (float) cfg.getDouble(cfgYield);
 		velUse = cfg.getBoolean(cfgVelUse);
 		velMin = (float) cfg.getDouble(cfgVelMin);
 		velCen = (float) cfg.getDouble(cfgVelCen);
 		velRan = (float) cfg.getDouble(cfgVelRan);
 		fStraight = (float) cfg.getDouble(cfgFStraight);
 		
 		if ( maxRadius > radiusLock) maxRadius = radiusLock;
 		initBlockIds();
 		createArrays();
 		for (Integer i : getIdList(cfg, cfgIgnore)){
 			ignore[i] = !invertIgnored;
 		}
 		ConfigurationSection sec = cfg.getConfigurationSection(cfgResistence);
 		for (String key : sec.getKeys(false)){
 			if ( "default".equalsIgnoreCase(key)) continue;
 			float val = (float) cfg.getDouble(cfgResistence+"."+key+".value", 1.0);
 			for ( Integer i : getIdList(cfg, cfgResistence+"."+key+".ids")){
 				resistance[i] = val;
 			}
 		}
 	}
 	
 	public static List<Integer> getIdList(Configuration cfg, String path){
 		List<Integer> out = new LinkedList<Integer>();
 		List<String> ref = cfg.getStringList(path);
 		for ( Object x : ref){
 			Integer id = null;
 			if ( x instanceof Number){
 				// just in case
 				id = ((Number) x).intValue();
 			} else if ( x instanceof String){
 				try{
 					id = Integer.parseInt((String) x);
 				} catch(NumberFormatException exc) {
 					Material mat = Material.matchMaterial((String) x);
 					if ( mat != null){
 						id = mat.getId();
 					}
 				}
 			}
 			if (id!=null){
 				if ( id>=0 && id<4096) out.add(id);
 				continue;
 			}
 			Bukkit.getServer().getLogger().warning(msgPrefix+"Bad item ("+path+"): "+x);
 		}
 		return out;
 	}
 	
 	private void initBlockIds() {
 		for (int i = 0;i<ignore.length;i++){
 			ignore[i] = invertIgnored;
 			resistance[i] = defaultResistance;
 		}
 	}
 
 	void createArrays() {
 		int d = 1 + (int) (maxRadius*2.0);
 		center = 1 + (int) maxRadius;
 		fY = d;
 		fZ = d*d;
 		int sz = d*d*d;
 		izMax = sz - fZ;
 		sequence = new int[sz];
 		strength = new float[sz];
 		for ( int i = 0; i<sz; i++){
 			sequence[i] = 0;
 		}
 	}
 
 	@EventHandler(priority=EventPriority.HIGHEST)
 	void onExplosionPrimeLowest(ExplosionPrimeEvent event){
 		waitingEP = null;
 		if (!handleExplosions) return;
 		else if (event.isCancelled()) return;
 		else if (!handledEntities.contains(event.getEntityType())) return;
 		// do prepare to handle this explosion:
 		event.setCancelled(true);
 //		waitingEP = event;
 //	}
 //	
 //	@EventHandler(priority=EventPriority.MONITOR)
 //	void onExplosionPrime(ExplosionPrimeEvent event){
 //		// check event 
 //		if ( waitingEP != event) return;
 //		waitingEP = null;
 //		// event is to be handled:
 //		if ( !event.isCancelled()) event.setCancelled(true); // just in case other plugins mess with this one.
 		EntityType type = event.getEntityType();
 		Entity entity = event.getEntity();
 		Location loc = entity.getLocation().clone();
 		// Might have to schedule this for next tick ?
 		createExplosion(loc, event.getRadius(), event.getFire(), entity, type);
 		if (!entity.isDead()) entity.remove();
 	}
 	
 	/**
 	 * API
 	 * (called from event handling as well)
 	 * @param loc
 	 * @param radius As in World.createExplosion
 	 * @param fire
 	 * @param entityType May be null, might be unused.
 	 */
 	public void createExplosion(Location loc, float radius, boolean fire, Entity explEntity, EntityType entityType){
 		// create a fake explosion
 		World world = loc.getWorld();
 		world.createExplosion(loc, 0.0F);
 		// calculate effects
 		// WORKAROUND:
 		float realRadius = radius*radiusMultiplier;
 		double x = loc.getX();
 		double y = loc.getY();
 		double z = loc.getZ();
 		List<Entity> nearbyEntities;
 		if (explEntity==null) nearbyEntities = getNearbyEntities(world, x,y,z, realRadius);
 		else nearbyEntities = explEntity.getNearbyEntities(realRadius, realRadius, realRadius);
 		applyExplosionEffects(world, x, y, z, realRadius, fire, explEntity, entityType, nearbyEntities);
 	}
 
 	public static List<Entity> getNearbyEntities(World world, double x, double y,
 			double z, double realRadius) {
 		List<Entity> nearbyEntities;
 		Entity dummyEntity = world.spawnCreature(new Location(world,x,y,z), EntityType.CHICKEN);
 		if ( dummyEntity==null){
 			// TODO: maybe warn ?
 			nearbyEntities = new LinkedList<Entity>();
 			for ( Entity entity : world.getEntities() ){
 				Location ref = entity.getLocation();
 				if (Math.abs(x-ref.getX())<realRadius && Math.abs(z-ref.getZ())<realRadius && Math.abs(y-ref.getY())<realRadius) nearbyEntities.add(entity);
 			}
 		} else{
 			nearbyEntities = dummyEntity.getNearbyEntities(realRadius, realRadius, realRadius);
 			dummyEntity.remove();
 		}
 		return nearbyEntities;
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
 		createExplosion(new Location(world, x,y,z), radius, fire, null, null);
 	}
 	
 	/**
 	 * 
 	 * @param world
 	 * @param x
 	 * @param y
 	 * @param z
 	 * @param realRadius intended to be the block distance rather, so that resistance of 1.0 (AIR) would lead to the full range.
 	 * @param fire
 	 * @param entityType Causing the explosion.
 	 * @param nearbyEntities List of entities that can be affected , should be within radius (x,y,z independently), damage depends on settings.
 	 */
 	public void applyExplosionEffects(World world, double x, double y,
 			double z, float realRadius, boolean fire, Entity explEntity, EntityType entityType,
 			List<Entity> nearbyEntities) {
 		if ( realRadius > maxRadius){
 			// TODO: setttings ?
 			realRadius = maxRadius;
 		}
 		Server server = getServer();
 		PluginManager pm = server.getPluginManager();
 		
 		// blocks:
 		List<Block> affected = getExplodingBlocks(world , x, y, z, realRadius);
 		EntityExplodeEvent exE = new EntityExplodeEvent(explEntity, new Location(world,x,y,z), affected, defaultYield );
 		pm.callEvent(exE);
 		if (exE.isCancelled()) return;
 		float yield = exE.getYield();
 		for ( Block block : exE.blockList()){
 //			if (block.getType() == Material.TNT){
 //				// TODO: spawn primed tnt !
 //				block.setTypeId(0);
 //				continue;
 //			}
 			Collection<ItemStack> drops = block.getDrops();
 			for (ItemStack drop : drops){
 				if ( random.nextFloat()<=yield){
 					Location loc = block.getLocation().add(vCenter);
 					Item item = world.dropItemNaturally(loc, drop.clone());
 					if (item==null) continue;
 					addRandomVelocity(item, loc, x,y,z, realRadius);
 				}
 			}
 			block.setTypeId(0, true);
 		}
 		
 		// entities:
 		final double damBase = realRadius * damageMultiplier;
 		for ( Entity entity : nearbyEntities){
 			// TODO: damage entities according to type, + vector ?
 			// test damage:
 			double d = entity.getLocation().distance(new Location(world,x,y,z)); // TODO efficiency
 			// TODO: test the strength map depending on settings !
 			if ( d>realRadius) continue;
 			addRandomVelocity(entity, entity.getLocation(), x,y,z, realRadius);
 			if (sparePrimed && (entity instanceof TNTPrimed)) continue;
 			int damage = 1 + (int) (damBase/(d+1.0)) ;
 			EntityDamageEvent event = new EntityDamageEvent(entity, DamageCause.ENTITY_EXPLOSION, damage);
 			pm.callEvent(event);
 			if (!event.isCancelled()){
 				if ( entity instanceof LivingEntity){
 					((LivingEntity) entity).setLastDamageCause(event);
 				}
 				else entity.setLastDamageCause(event); // TODO
 			}
 		}
 		
 		
 	}
 
 	private void addRandomVelocity(Entity entity, Location loc, double x, double y,
 			double z, float realRadius) {
 		// TODO: make some things configurable !
 		if (!velUse) return;
 		Vector v = entity.getVelocity();
 		Vector fromCenter = new Vector(loc.getX()-x,loc.getY()-y,loc.getZ()-z).normalize();
 		Vector rv = v.add(fromCenter.multiply(velMin+random.nextFloat()*velCen)).add(Vector.getRandom().multiply(velRan));
 		if (entity instanceof LivingEntity) ((LivingEntity) entity).setVelocity(rv); 
 		else if (entity instanceof TNTPrimed) ((TNTPrimed) entity).setVelocity(rv);
 		else entity.setVelocity(rv);
 	}
 
 	public List<Block> getExplodingBlocks(World world, double cx, double cy,
 			double cz, float realRadius) {
 		if ( realRadius > maxRadius){
 			// TODO: setttings ?
 			realRadius = maxRadius;
 		}
 		List<Block> blocks = new LinkedList<Block>();
 		seqMax ++; // new round !
 		// starting at center block decrease weight and check neighbor blocks recursively, while weight > durability continue, only check
 		propagate(world, (int)cx, (int)cy, (int)cz, center*(1+fY+fZ), 0, realRadius, blocks);
 		return blocks;
 	}
 	
 	/**
 	 * TEST VERSION / LOW OPTIMIZATION !
 	 * Recursively collect blocks that get destroyed.
 	 * @param w
 	 * @param cx Current real world pos.
 	 * @param cy
 	 * @param cz
 	 * @param i index of  array
 	 * @param realRadius
 	 * @param seq
 	 * @param blocks
 	 */
 	final void propagate(final World w, final int cx, final int cy, final int cz, 
 			final int i, int dir, float realRadius, final List<Block> blocks){
 		if ( cy<0 || cy > w.getMaxHeight()) return; // TODO: maybe +-1 ?
 		// World block position:
 		final Block block = w.getBlockAt(cx,cy,cz);
 		final int id = block.getTypeId();
 		// Resistance check:
 		float dur ; // AIR
 		final boolean ign;
 		if (id>=0 && id<4096){
 			dur = resistance[id];
 			ign = ignore[id];
 		}
 		else{
 			dur = defaultResistance;
 			ign = true;
 		}
		final boolean noAdd;
		if ( sequence[i] == seqMax){
			if ( strength[i] >= dur) noAdd = true;
			else noAdd = false;
		}
		else noAdd = false;
		// Matrix position:
		sequence[i] = seqMax;
		strength[i] = realRadius;
 //		if ( randDec > 0.0) dur += random.nextFloat()*randDec;
 		if ( dur > realRadius) return; // no propagation
 		realRadius -= dur;
 		// Add block or not:
		if (id!=0 && !noAdd && !ign) blocks.add(block);
 		// propagate:
 		if (i<fZ || i>izMax) return;
 		// x-
 		if (dir != 2){
 			final float useR; // radius to be used.
 			if (dir==4) useR = realRadius * fStraight;
 			else useR = realRadius;
 			final int j1 = i - 1;
 			if (sequence[j1]!=seqMax || useR>strength[j1]) propagate(w, cx-1, cy, cz, j1, 4, useR, blocks);
 		}
 		// x+
 		if ( dir != 4){
 			final float useR; // radius to be used.
 			if (dir==2) useR = realRadius * fStraight;
 			else useR = realRadius;
 			final int j2 = i + 1;
 			if (sequence[j2]!=seqMax || useR>strength[j2]) propagate(w, cx+1, cy, cz, j2, 2, useR, blocks);
 		}
 		// y-
 		if (dir != 6){
 			final float useR; // radius to be used.
 			if (dir==8) useR = realRadius * fStraight;
 			else useR = realRadius;
 			final int j3 = i - fY;
 			if (sequence[j3]!=seqMax || useR>strength[j3]) propagate(w, cx, cy-1, cz, j3, 8, useR, blocks);
 		}
 		// y+
 		if (dir != 8){
 			final float useR; // radius to be used.
 			if (dir==6) useR = realRadius * fStraight;
 			else useR = realRadius;
 			final int j4 = i + fY;
 			if (sequence[j4]!=seqMax || useR>strength[j4]) propagate(w, cx, cy+1, cz, j4, 6, useR, blocks);
 		}
 		// z-
 		if (dir != 10){
 			final float useR; // radius to be used.
 			if (dir==12) useR = realRadius * fStraight;
 			else useR = realRadius;
 			final int j5 = i - fZ;
 			if (sequence[j5]!=seqMax || useR>strength[j5]) propagate(w, cx, cy, cz-1, j5, 12, useR, blocks);
 		}
 		// z+
 		if (dir!=12){
 			final float useR; // radius to be used.
 			if (dir==10) useR = realRadius * fStraight;
 			else useR = realRadius;
 			final int j6 = i + fZ;
 			if (sequence[j6]!=seqMax || useR>strength[j6]) propagate(w, cx, cy, cz+1, j6, 10, useR, blocks); 
 		}
 	}
 	
 }
