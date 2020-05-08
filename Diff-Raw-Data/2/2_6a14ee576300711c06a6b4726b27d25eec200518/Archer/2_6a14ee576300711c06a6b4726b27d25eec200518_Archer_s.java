 package me.asofold.bukkit.archer;
 
 import java.io.File;
 import java.text.DecimalFormat;
 import java.text.DecimalFormatSymbols;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import me.asofold.bukkit.archer.config.compatlayer.CompatConfig;
 import me.asofold.bukkit.archer.config.compatlayer.CompatConfigFactory;
 import me.asofold.bukkit.archer.config.compatlayer.ConfigUtil;
 import me.asofold.bukkit.archer.core.PlayerData;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.block.BlockState;
 import org.bukkit.block.Sign;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Arrow;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Projectile;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.ProjectileHitEvent;
 import org.bukkit.event.entity.ProjectileLaunchEvent;
 import org.bukkit.material.Attachable;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.util.Vector;
 
 public class Archer extends JavaPlugin implements Listener{
 	
 	private final DecimalFormat format = new DecimalFormat("##.##");
 	
 	private final Map<String, PlayerData> players = new HashMap<String, PlayerData>(20);
 
 	private static final String msgStart = ChatColor.DARK_GRAY + "[Archer] " + ChatColor.GRAY;
 	
 	/**
 	 * Lines of target (trim applied).
 	 */
 	private final String[] defaultLines = new String[]{
 			"ooooo",
 			"ooxxxoo",
 			"ooxxxoo",
 			"ooooo"
 	};
 	
 	private final String[] lines = new String[4];
 	
 	private static final double defaultSignHitDist = 0.31;
 	private double signHitDist = defaultSignHitDist;
 	
 //	public static final double defaultArrowLength = 0.7;
 //	private double arrowLength = defaultArrowLength;
 	private double defaultStep = 0.3;
 	private double step = defaultStep;
 
 	private double defaultOffsetX = 0.0;
 	private double offsetX = defaultOffsetX;
 	private double defaultOffsetY = 0.08;
 	private double offsetY = defaultOffsetY ;
 	private double defaultOffsetZ = 0.0;
 	private double offsetZ = defaultOffsetZ;
 
 	private double defaultDivisor = 100;
 	private double offDivisor = defaultDivisor;
 
 	private double defaultNotifyDistance = 120.0;
 	private double notifyDistance = defaultNotifyDistance ;
 
 	private boolean defaultNotifyCrossWorld = false;
 	private boolean notifyCrossWorld = defaultNotifyCrossWorld ;
 
 	private boolean defaultVerbose = false;
 	private boolean verbose = defaultVerbose ;
 
 	private double defaultShootDistMin = 0.0;
 	private double shootDistMin = defaultShootDistMin ;
 	
 	private double defaultShootDistMax = 0.0;
 	private double shootDistMax = defaultShootDistMax;
 
 	private boolean defaultTrim = false;
 	private boolean trim = defaultTrim;
 
 	private boolean defaultStripColor = false;
 	private boolean stripColor = defaultStripColor;
 
 	private boolean defaultIgnoreCase = false;
 	private boolean ignoreCase = defaultIgnoreCase;
 
 	private boolean defaultUsePermissions = true;
 	private boolean usePermissions = defaultUsePermissions;
 
 	private final long defaultDurExpireData = 20 * 60 * 1000;
 	private long durExpireData = defaultDurExpireData;
 	
 	public CompatConfig getDefaultSettings(){
 		CompatConfig cfg = CompatConfigFactory.getConfig(null);
 		LinkedList<String> lines = new LinkedList<String>();
 		for (String line : this.defaultLines){
 			lines.add(line);
 		}
 		cfg.set("target.lines", lines);
 		cfg.set("target.trim", defaultTrim);
 		cfg.set("target.stripColor", defaultStripColor);
 		cfg.set("target.ignore-case", defaultIgnoreCase);
 		cfg.set("notify.distance", defaultNotifyDistance);
 		cfg.set("notify.cross-world", defaultNotifyCrossWorld);
 		cfg.set("shooter.distance.min", defaultShootDistMin);
 		cfg.set("shooter.distance.max", defaultShootDistMax);
 		cfg.set("off-target.distance", defaultSignHitDist);
 //		cfg.set("arrow-length", defaultArrowLength);
 		cfg.set("step", defaultStep);
 		cfg.set("offset.x", defaultOffsetX);
 		cfg.set("offset.y", defaultOffsetY);
 		cfg.set("offset.z", defaultOffsetZ);
 		cfg.set("off-target.divisor", defaultDivisor);
 		cfg.set("verbose", defaultVerbose);
 		cfg.set("permissions.use", defaultUsePermissions);
 		cfg.set("players.expire-offline", defaultDurExpireData / 60 / 1000); // Set as minutes.
 		return cfg;
 	}
 
 	public void reloadSettings() {
 		File file = new File(getDataFolder(), "config.yml");
 		CompatConfig cfg = CompatConfigFactory.getConfig(file);
 		boolean exists = file.exists();
 		if (exists) cfg.load();
 		if (ConfigUtil.forceDefaults(getDefaultSettings(), cfg) || !exists) cfg.save();
 		signHitDist = cfg.getDouble("off-target.distance", defaultSignHitDist);
 //		arrowLength = cfg.getDouble("arrow-length", defaultArrowLength);
 		step = cfg.getDouble("step", defaultStep);
 		offsetX = cfg.getDouble("offset.x", defaultOffsetX);
 		offsetY = cfg.getDouble("offset.y", defaultOffsetY);
 		offsetZ = cfg.getDouble("offset.z", defaultOffsetZ);
 		offDivisor = cfg.getDouble("off-target.divisor", defaultDivisor);
 		verbose = cfg.getBoolean("verbose", defaultVerbose);
 		notifyDistance = cfg.getDouble("notify.distance", defaultNotifyDistance);
 		notifyCrossWorld = cfg.getBoolean("notify.cross-world", defaultNotifyCrossWorld);
 		shootDistMin = cfg.getDouble("shooter.distance.min", defaultShootDistMin);
 		shootDistMax = cfg.getDouble("shooter.distance.max", defaultShootDistMax);
 		String[] lines = readLines(cfg, "target.lines");
 		if (lines == null) lines = defaultLines;
 		for (int i = 0; i < 4; i++){
 			this.lines[i] = lines[i];
 		}
 		trim = cfg.getBoolean("target.trim", defaultTrim);
 		stripColor = cfg.getBoolean("target.stripColor", defaultStripColor);
 		ignoreCase = cfg.getBoolean("target.ignore-case", defaultIgnoreCase);
 		usePermissions = cfg.getBoolean("permissions.use", defaultUsePermissions);
 		durExpireData = cfg.getLong("players.expire-offline", defaultDurExpireData) * 60 * 1000; // Set as minutes.
 	}
 	
 	private String[] readLines(CompatConfig cfg, String path) {
 		String[] out = new String[4];
 		List<String> lines = cfg.getStringList(path, null);
 		if (lines == null) return null;
 		if (lines.size() != 4) return null;
 		for (int i = 0; i < 4; i++){
 			String line = lines.get(i);
 			if (trim) line = line.trim();
 			if (stripColor) line = ChatColor.stripColor(line);
 			if (ignoreCase) line = line.toLowerCase();
 			if (line.length() > 15) return null;
 			out[i] = line;
 		}
 		return out;
 	}
 
 	public Archer(){
 		DecimalFormatSymbols sym = format.getDecimalFormatSymbols();
 		sym.setDecimalSeparator('.');
 		format.setDecimalFormatSymbols(sym);
 	}
 	
 	@Override
 	public boolean onCommand(CommandSender sender, Command command,
 			String label, String[] args) {
 		if (command != null) label = command.getLabel();
 		label = label.toLowerCase();
 		
 		if (label.equals("archer")){
 			return archerCommand(sender, args);
 		}
 		return false;
 	}
 
 	private boolean archerCommand(CommandSender sender, String[] args) {
 		int len = args.length;
 		String cmd = null;
 		if (len > 0){
 			cmd = args[0].trim().toLowerCase();
 		}
 		if (len == 1 && cmd.equals("notify")){
 			// toggle notify
 			if (usePermissions && !checkPerm(sender, "archer.notify")) return true;
 			if (!checkPlayer(sender) ) return true;
 			Player player = (Player) sender;
 			String playerName = player.getName();
 			String lcName = playerName.toLowerCase();
 			if (removeData(lcName)){
 				player.sendMessage(msgStart + "You " + ChatColor.RED + "unsubscribed" + ChatColor.GRAY + " from archer events.");
 				return true;
 			}
 			players.put(lcName, new PlayerData(player));
 			player.sendMessage(msgStart + "You " + ChatColor.GREEN + "subscribed" + ChatColor.GRAY + " to archer events.");
 			return true;
 		}
 		else if (len == 1 && cmd.equals("reload")){
 			if (!checkPerm(sender, "archer.reload")) return true;
 			reloadSettings();
 			sender.sendMessage("[Archer] Settings reloaded.");
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * 
 	 * @param lcName
 	 * @return If data was present.
 	 */
 	private boolean removeData(String lcName) {
 		PlayerData data = players.remove(lcName);
 		if (data == null) return false;
 		data.clear();
 		return true;
 	}
 
 	private boolean checkPlayer(CommandSender sender) {
 		if (sender instanceof Player) return true;
 		else{
 			sender.sendMessage("[Archer] Only available for players !");
 			return false;
 		}
 	}
 
 	private boolean checkPerm(CommandSender sender, String perm) {
 		if (!hasPermission(sender, perm)){
 			sender.sendMessage(ChatColor.DARK_RED + "You don't have permission.");
 			return false;
 		}
 		else return true;
 	}
 
 	private boolean hasPermission(CommandSender sender, String perm) {
 		return sender.isOp() || sender.hasPermission(perm);
 	}
 
 	@Override
 	public void onEnable() {
 		reloadSettings();
 		getServer().getPluginManager().registerEvents(this, this);
 		super.onEnable();
 	}
 	
 	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled = true)
 	final void onHit(final ProjectileHitEvent event){
 		if (players.isEmpty()) return;
 		final Projectile projectile = event.getEntity();
 		final PlayerData data = getPlayerData(projectile);
 		if (data == null) return;
 		final int entityId = projectile.getEntityId();
 		final Location launchLoc = data.removeLaunch(entityId);
 		if (launchLoc == null) return;
 		
 		// TODO: later: add miss / hit events
 		final Vector velocity = projectile.getVelocity();
 		final Location projLoc = projectile.getLocation();
 		
 		if (verbose) System.out.println("projectile at: " + stringPos(projLoc)); // TODO: REMOVE
 		
 		final Location hitLoc = getHitLocation(projLoc, velocity);
 		if (hitLoc == null) return;
 		
 		if (verbose) System.out.println("hit loc at: " + stringPos(hitLoc)); // TODO: REMOVE
 		
 		final Block hitBlock = hitLoc.getBlock();
 		
 		final BlockState state = hitBlock.getState();
 		if (!(state instanceof Sign)) return;
 		final Sign sign = (Sign) state;
 		final String[] lines = sign.getLines();
 		for (int i = 0; i < 4; i ++){
 			String line = lines[i];
 			if (trim) line = line.trim();
 			if (stripColor) line = ChatColor.stripColor(line);
 			if (ignoreCase) line = line.toLowerCase();
 			if (!line.equals(this.lines[i])) return;
 		}
 		// Target sign hit !
 		
 		// Get middle of sign (!)
 		final BlockFace attached = ((Attachable) sign.getData()).getAttachedFace();
 		final Block attachedTo = hitBlock.getRelative(attached); 
 		
 		// Hit block (sign) coordinates.
 		final double x = hitBlock.getX();
 		final double y = hitBlock.getY();
 		final double z = hitBlock.getZ();
 		
 		// Attached to block coordinates.
 		final double dx = attachedTo.getX() - x;
 		final double dy = attachedTo.getY() - y;
 		final double dz = attachedTo.getZ() - z;
 		
 		// Middle of of sign.
 		final double mx = 0.5 + x + .5 * dx + offsetX;
 		final double my = 0.5 + y + .5 * dy + offsetY;
 		final double mz = 0.5 + z + .5 * dz + offsetZ;
 		
 		final double distOff;
 		
 		// Hit location on sign block (not exact !).
 		final double hX = hitLoc.getX();
 		final double hY = hitLoc.getY();
 		final double hZ = hitLoc.getZ();
 		
 		// Velocity
 		final double vX = velocity.getX();
 		final double vY = velocity.getY();
 		final double vZ = velocity.getZ();
 		
 		// add time correction ? [Rather not, the arrow has hit, so calculate where it would actually hit.]
 		
 		// Corrected coordinates + distance off target.
 		final double cX;
 		final double cY;
 		final double cZ;
 		if (dx != 0.0){
 			final double t = (mx - hX)/vX;
 			cX = mx;
 			cY = hY + t * vY;
 			cZ = hZ + t * vZ;
 			distOff = getLength(my - cY, mz - cZ );
 		}
 		// Not for dy !
 		else if (dz != 0.0){
 			final double t = (mz - hZ)/vZ;
 
 			cX = hX + t * vX;
 			cY = hY + t * vY;
 			cZ = mz;
 			distOff = getLength(mx - cX, my - cY );
 		}
 		else throw new RuntimeException("HUH?");
 		
 		if (verbose) System.out.println("dx,dy,dz: " + stringPos(dx, dy, dz)); // TODO: REMOVE
 		if (verbose) System.out.println("middle at: " + stringPos(mx, my, mz)); // TODO: REMOVE
 		if (verbose) System.out.println("corrected hit pos: " +stringPos(cX, cY, cZ) + " -> off by " + format.format(distOff)); // TODO: REMOVE
 		
 		if (distOff > signHitDist) return;
 		// Hit !
 		final Location targetLocation = new Location(hitLoc.getWorld(), mx,my,mz);
 		final double shootDist = launchLoc.toVector().distance(new Vector(mx,my,mz));
 		if (shootDistMin > 0.0 && shootDist < shootDistMin) return;
 		if (shootDistMax > 0.0 && shootDist > shootDistMax) return;
 		final int off = (int) Math.round((1000.0 - 1000.0 * (signHitDist - distOff) / signHitDist) / offDivisor);
 		final String specPart = ChatColor.YELLOW.toString() + off + ChatColor.GRAY + " off target at " + ChatColor.WHITE + format.format(shootDist) + ChatColor.GRAY + " blocks distance.";
 		final String msg = ChatColor.WHITE + data.playerName + ChatColor.GRAY + " hits " + specPart;
 		data.player.sendMessage(ChatColor.BLACK + "[Archer] " + ChatColor.GRAY + "hits " + specPart);
 		sendAll(msg, targetLocation, data);
 	}
 	
 	private String stringPos(double x, double y, double z) {
 		return "" + format.format(x) + ", " + format.format(y) + ", " + format.format(z);
 	}
 
 	private final String stringPos(final Location loc){
 		return "" + format.format(loc.getX()) + ", " + format.format(loc.getY()) + ", " + format.format(loc.getZ());
 	}
 	
 	/**
 	 * Sign hit location;
 	 * @param loc
 	 * @return
 	 */
 	public final Location getHitLocation(Location loc, final Vector velocity) {
 	//		loc = loc.add(direction.normalize().multiply(arrowLength));
 		final Block hitBlock = loc.getBlock();
 		int type = hitBlock.getTypeId();
 		final double l = velocity.length();
 		double done = 0.0;
 		final double step = this.step;
 		if (type == 0){
 			// TODO: also for other block types !
 			// TODO: optimize: find block transitions directly (one by one).
 			final Vector add = velocity.clone().multiply(step/l);
 			while (type == 0){
 				loc = loc.add(add);
 				if (verbose) System.out.println("EXTEND: " + stringPos(loc)); // TODO: REMOVE
 				type = loc.getBlock().getTypeId();
 				done += step;
 				if (done >= l) break;
 			}
 			
 		}
 		
 		if (verbose) System.out.println("Hit type ("+format.format(l)+"): "+ type); // TODO: REMOVE
 		
 		if (type != Material.WALL_SIGN.getId()) return null;
 		return loc;
 	}
 
 	private double getLength(double x1, double x2) {
 		return Math.sqrt(x1*x1 + x2*x2);
 	}
 
 	public static final double getHitDist(final Location loc){
 		return loc.distance(new Location(loc.getWorld(), 0.5 + (double) loc.getBlockX(), 0.5 + (double) loc.getBlockY(), 0.5 + (double) loc.getBlockZ()));
 	}
 	
 	/**
 	 * Always positive distance to 0.5 .
 	 * @param coord
 	 * @return
 	 */
 	public static final double getHitDist(final double coord){
 		return Math.abs(0.5 - Math.abs(Math.floor(coord)));
 	}
 	
 	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled = true)
 	final void onLaunch(final ProjectileLaunchEvent event){
 		if (players.isEmpty()) return;
 		final Projectile projectile = event.getEntity();
 		final PlayerData data = getPlayerData(projectile);
 		if (data == null) return;
 		// Register projectile for aiming.
 		data.addLaunch(projectile.getEntityId(), data.player.getLocation().add(new Vector(0.0, data.player.getEyeHeight(), 0.0))); // projectile.getLocation());
 	}
 	
 	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled = false)
 	final void onDamage(final EntityDamageByEntityEvent event){
 		// also check cancelled events.
 		if (players.isEmpty()) return;
 		final Entity entity = event.getDamager();
 		if (!(entity instanceof Projectile)) return;
 		final Projectile projectile = (Projectile) entity;
 		final PlayerData data = getPlayerData(projectile);
 		if (data == null) return;
 		final int id = projectile.getEntityId();
 		final Location launchLoc = data.removeLaunch(id);
 		if (launchLoc == null) return;
 		// TODO: later: check if contest + add miss / hit events
 	}
 	
 	public void sendAll(String msg, boolean label, Location ref, PlayerData exclude){
 		if (!label) sendAll(msg, ref, exclude);
 		else sendAll(msgStart + msg, ref, exclude);
 	}
 	
 	public void sendAll(String msg, Location ref, PlayerData exclude){
 		boolean distance = notifyDistance > 0.0;
 		boolean restrict = ref != null && (!notifyCrossWorld || distance);
 		String worldName = null;
 		if (restrict) worldName = ref.getWorld().getName();
 		List<String> rem = new LinkedList<String>();
 		final long tsNow = System.currentTimeMillis();
 		for (PlayerData data : players.values()){
 			if (data == exclude) continue;
 			if (data.player == null || !data.player.isOnline()){
				if (data.mayForget(tsNow, durExpireData)) rem.add(data.playerName.toLowerCase());
 				continue;
 			}
 			if (restrict){
 				if (!worldName.equals(data.player.getWorld().getName())) continue;
 				else if (distance && ref.distance(data.player.getLocation()) > notifyDistance) continue; 
 			}
 			data.player.sendMessage(msg);
 		}
 		for (String name : rem){
 			players.remove(name);
 		}
 	}
 	
 	/**
 	 * Get PlayerData and set bPlayer if present and projectile is Arrow (!).
 	 * @param projectile
 	 * @return
 	 */
 	public final PlayerData getPlayerData(final Projectile projectile){
 		final Player player = getPlayer(projectile);
 		if (player == null) return null;
 		final PlayerData data = players.get(player.getName().toLowerCase());
 		if ( data == null) return null;
 		data.setPlayer(player);
 		return data;
 	}
 	
 	public static final Player getPlayer(final Projectile projectile){
 		if (!(projectile instanceof Arrow)) return null;
 		final Entity entity = projectile.getShooter();
 		if (entity == null) return null;
 		else if (entity instanceof Player) return (Player) entity;
 		else return null;
 	}
 
 }
