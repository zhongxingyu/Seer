 package me.asofold.bukkit.contextmanager.hooks.chestshop;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import me.asofold.bukkit.contextmanager.core.CMCore;
 import me.asofold.bukkit.contextmanager.hooks.AbstractServiceHook;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.block.Chest;
 import org.bukkit.block.Sign;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.util.Vector;
 
 import asofold.pluginlib.shared.Blocks;
 import asofold.pluginlib.shared.Logging;
 import asofold.pluginlib.shared.Utils;
 import asofold.pluginlib.shared.blocks.FBlockPos;
 import asofold.pluginlib.shared.items.ItemSpec;
 import asofold.pluginlib.shared.mixin.configuration.compatlayer.CompatConfig;
 import asofold.pluginlib.shared.mixin.configuration.compatlayer.CompatConfigFactory;
 import asofold.pluginlib.shared.mixin.configuration.compatlayer.ConfigUtil;
 
 import com.Acrobot.ChestShop.Utils.uBlock;
 import com.Acrobot.ChestShop.Utils.uSign;
 import com.sk89q.worldguard.protection.ApplicableRegionSet;
 import com.sk89q.worldguard.protection.regions.ProtectedRegion;
 
 /**
  * This hook gets added in onEnable.
  * @author mc_dev
  *
  */
 public class ChestShopHook extends AbstractServiceHook implements Listener{
 	
 	private final static String[] labels = new String[]{"shops", "shop"};
 	
 	// TODO: filter by another region, if desired.
 	
 	Map<FBlockPos, ShopSpec> blockMap = new HashMap<FBlockPos, ShopSpec>();
 	
 	/**
 	 * For checking ...
 	 */
 	Map<String, Map<String, RegionSpec>> regionMap = new HashMap<String, Map<String,RegionSpec>>();
 	
 	/**
 	 * Mapping raw id to items.
 	 */
 	Map<Integer, Set<RegionSpec>> idMap = new HashMap<Integer, Set<RegionSpec>>();
 	
 	/**
 	 * Only if one of these regions matches, regions will be added.
 	 */
 	Map<String, Set<String>> filter = new HashMap<String, Set<String>>();
 	
 	/**
 	 * Lower case names to lower case names.
 	 */
 	Map<String, String> shopOwners = new HashMap<String, String>();
 
 	
 	// TODO: sort in to filter ! -> available item types.
 	
 	/**
 	 * if false, only regions owned by the shop owner will get considered, but one region must match filter.
 	 */
 	boolean addUnowned = false;
 	
 	boolean useFilter = true;
 	
 	
 	static final long msDay = 1000*60*60*24;
 	
 	long durExpire = 14 * msDay;
 
 	public void addFilter(String world, String region){
 		String lcWorld = world.toLowerCase();
 		String lcRid = region.toLowerCase();
 		Set<String> rs = filter.get(lcWorld);
 		if (rs == null){
 			rs = new HashSet<String>();
 			filter.put(lcWorld, rs);
 		}
 		rs.add(lcRid);
 	}
 	
 	@Override
 	public String getHookName() {
 		return "ChestShop3";
 	}
 	
 	@EventHandler(priority = EventPriority.MONITOR)
 	final void onPlayerInteract(final PlayerInteractEvent event){
 //		if (event.isCancelled()) return;
 		Action action = event.getAction();
 		if ( action!= Action.RIGHT_CLICK_BLOCK && action != Action.LEFT_CLICK_BLOCK) return; // [mind leftclick sell]
 		final Block block = event.getClickedBlock();
 		if (block == null || block.getType() != Material.WALL_SIGN) return;
 		// Check wall signs for ChestShop syntax:
 		final Sign sign = (Sign) block.getState();
 		final String[] lines = sign.getLines();
 		
 		// TODO: split here if desied to add signChange event ? [would change concept though]
 		
 		if (!uSign.isValid(lines)) return;
 		final Player player = event.getPlayer();
 		final String playerName = player.getName();
 		// TODO: maybe better heck or leave it out:
 		final String shopOwner = getShopOwner(lines[0]);
 		if (playerName.equalsIgnoreCase(shopOwner)) return; // ignore the shop owner.
 		final String priceSpec = lines[2];
 		Chest chest = uBlock.findChest(sign);
 		if (chest == null){
 			update(block, null, null, 0, -1.0, -1.0);
 			return;
 		}
 		double priceBuy = uSign.buyPrice(priceSpec);
 		double priceSell= uSign.sellPrice(priceSpec);
 		int amount = uSign.itemAmount(lines[1]);
 		ItemStack stack = com.Acrobot.ChestShop.Items.Items.getItemStack(lines[3]);
 		if(stack == null){
 			update(block, null, null, 0, -1.0, -1.0);
 			return;
 		}
 		if (priceBuy >= 0){
 			// TODO: check if out of stock.
 		}
 		
 		if (priceSell >= 0){
 			// TODO: Check if chest has space.
 		}
 		
 		update(block, shopOwner, stack, amount, priceBuy, priceSell);
 	}
 	
 	@EventHandler(priority=EventPriority.MONITOR)
 	final void onBlockBreak(BlockBreakEvent event){
 		if (event.isCancelled()) return;
 		Block block = event.getBlock();
 		if (block.getType() != Material.SIGN) return;
 		// TODO: maybe this is fastest, maybe not.
 		update(block, null, null, 0, -1.0, -1.0);
 	}
 	
 	public final void update(final Block block, String shopOwner, final ItemStack stack, final int amount, final double priceBuy, final double priceSell) {
 		final FBlockPos pos = Blocks.FBlockPos(block);
 		final ShopSpec spec = blockMap.get(pos);
 		if (spec == null){
 			if (priceBuy > 0 || priceSell > 0){
 				checkAddShopSpec(pos, block, shopOwner, stack, amount, priceBuy, priceSell);
 			}
 		}
 		else{
 			// TODO: update might be more specialized later on.
 			spec.update(shopOwner, stack, amount, priceBuy, priceSell);
 			if (spec.priceBuy < 0 && spec.priceSell < 0){
 				// Do remove this shop.
 				removeShopSpec(pos, spec);
 			}
 		}	
 	}
 	
 	/**
 	 * convenience method for simple removal.
 	 * @param pos
 	 * @return
 	 */
 	private boolean removeShop(FBlockPos pos){
 		ShopSpec spec = blockMap.get(pos);
 		if (spec == null) return false;
 		removeShopSpec(pos, spec);
 		return true;
 	}
 
 	/**
 	 * Call this to remove a spec.
 	 * @param pos
 	 * @param spec
 	 */
 	private void removeShopSpec(FBlockPos pos, ShopSpec spec) {
 		blockMap.remove(pos);
 		for (RegionSpec rSpec : spec.regions){
 			rSpec.shops.remove(spec);
 			if (rSpec.shops.isEmpty()){
 				removeRegionSpec(rSpec);
 				final int id = spec.stack.getTypeId();
 				Set<RegionSpec> rs = idMap.get(id);
 				if (rs!=null){
 					rs.remove(rSpec);
 					if (rs.isEmpty()) idMap.remove(id);
 				}
 			}
 			else{
 				// TODO: might still have to remove the id mapping !
 			}
 		}
 		// "cleanup":
 		spec.regions.clear();
 		spec.stack = null;
 	}
 	
 	/**
 	 * This does NOT remove all the shops or id mappings!
 	 * @param rSpec
 	 */
 	private void removeRegionSpec(RegionSpec rSpec) {
 		Map<String, RegionSpec> rMap = regionMap.get(rSpec.worldName);
 		if (rMap != null){
 			// should always be the case...
 			rMap.remove(rSpec.regionName);
 			if (rMap.isEmpty()) regionMap.remove(rSpec.worldName);
 		}
 		
 	}
 
 	/**
 	 * This is to create a new ShjopSpec for the position, or deny creation.
 	 * @param pos
 	 * @param block
 	 * @param shopOwner
 	 * @param stack
 	 * @param amount
 	 * @param priceBuy
 	 * @param priceSell
 	 * @return
 	 */
 	public final boolean checkAddShopSpec(final FBlockPos pos, final Block block, final String shopOwner, final ItemStack stack, final int amount,
 			final double priceBuy, final double priceSell) {
 		final Location loc = block.getLocation();
 		
 		// Check if passes filter / regions:
 		final List<String> valid = getValidRids(loc, shopOwner);
 		if (valid == null || valid.isEmpty()) return false;
 		
 		// Do add !
 		final ShopSpec spec = new ShopSpec(shopOwner, stack, amount, priceBuy, priceSell);
 		// block map
 		blockMap.put(pos, spec);
 		// id mapping set:
 		final int id = stack.getTypeId();
 		Set<RegionSpec> rs = idMap.get(id);
 		if (rs == null){
 			rs = new HashSet<RegionSpec>();
 			idMap.put(id, rs);
 		}
 		// Add region spec entries:
 		final String lcWorld = loc.getWorld().getName().toLowerCase();
 		for (final String lcId : valid){
 			final RegionSpec rSpec = getRegionSpec(lcWorld, lcId, true);
 			rs.add(rSpec);
 			rSpec.shops.add(pos);
 			spec.regions.add(rSpec);
 		}
 		return true;
 	}
 
 	public final List<String> getValidRids(final Location loc, final String shopOwner) {
 		final World world = loc.getWorld();
 		final String lcWorld = world.getName().toLowerCase();
 		final Set<String> rids = filter.get(lcWorld);
 		if (useFilter && rids == null) return null;
 		final ApplicableRegionSet set = Utils.getWorldGuard().getRegionManager(world).getApplicableRegions(loc);
 		if (useFilter){
 			boolean matchedFilter = false;
 			for (final ProtectedRegion r : set){
 				final String lcId = r.getId().toLowerCase();
 				if (rids.contains(lcId)){
 					matchedFilter = true;
 					break;
 				}
 			}
 			if (!matchedFilter) return null;
 		}
 		final List<String> valid = new LinkedList<String>();
 		for (final ProtectedRegion r : set){
 			final String lcId = r.getId().toLowerCase();
 			if (addUnowned || shopOwner == null || r.isOwner(shopOwner) || r.isMember(shopOwner)) valid.add(lcId);
 		}
 		return valid;
 	}
 
 	/**
 	 * 
 	 * @param lcWorld
 	 * @param b
 	 * @return
 	 */
 	private final RegionSpec getRegionSpec(final String lcWorld, final String lcId, final boolean create) {
 		Map<String, RegionSpec> rMap = regionMap.get(lcWorld);
 		RegionSpec rSpec;
 		if (rMap == null){
 			if (!create) return null;
 			rMap = new HashMap<String, RegionSpec>();
 			regionMap.put(lcWorld, rMap);
 			rSpec = new RegionSpec(lcWorld, lcId);
 			rMap.put(lcId, rSpec);
 			return rSpec;
 		}
 		rSpec = rMap.get(lcId);
 		if (rSpec == null){
 			if (!create) return null;
 			rSpec = new RegionSpec(lcWorld, lcId);
 			rMap.put(lcId, rSpec);
 			return rSpec;
 		}
 		return rSpec;
 	}
 
 	@Override
 	public String[] getCommandLabels() {
 		return labels;
 	}
 
 	@Override
 	public Listener getListener() {
 		return this;
 	}
 
 	@Override
 	public void onCommand(CommandSender sender, String label, String[] args) {
 		int len = args.length;
 		// ignore label, currently
 		
 		// shop / shops:
 		if (len == 0) sender.sendMessage("[ShopService] Options  (/cx shop ...): info | find <item> | find <region> | list <region> | list <world> <region> |");
 		else if (len == 1 && args[0].equalsIgnoreCase("info")) sendInfo(sender);
 		else if (len == 2 && args[0].equalsIgnoreCase("find")) onFind(sender, args[1]);
 		else if (len == 2 && args[0].equalsIgnoreCase("list")) onList(sender, null, args[1]);
 		else if (len == 3 && args[0].equalsIgnoreCase("list")) onList(sender, args[1], args[2]);
 		// TODO: list
 	}
 
 	private void onList(CommandSender sender, String world, String rid) {
 		if ((sender instanceof Player) && world == null) world = ((Player)sender).getWorld().getName();
 		sender.sendMessage("[ShopService] Items for "+rid+" (world: "+((world==null)?"<all>":world)+"):");
 		if (world == null){
 			for (String worldName : regionMap.keySet()){
 				sender.sendMessage("("+worldName+"): "+getItemsStr(worldName, rid));
 			}
 		}
 		else sender.sendMessage(getItemsStr(world, rid));
 	}
 
 	private final String getItemsStr(final String world, final String rid) {
 		// TODO: use digested versions and save them somewhere with timestamps !
 		final RegionSpec rSpec = getRegionSpec(world.toLowerCase(), rid.toLowerCase(), false);
 		if (rSpec == null) return "<not found>";
 		StringBuilder b = new StringBuilder();
 		for (final FBlockPos pos : rSpec.shops){
 			final ShopSpec spec = blockMap.get(pos);
 			b.append(spec.toString());
 			b.append(" | ");
 		}
 		return b.toString();
 	}
 
 	/**
 	 * Find shops for an item.
 	 * @param sender
 	 * @param input
 	 */
 	private void onFind(CommandSender sender, String input) {
 		ItemSpec spec = ItemSpec.match(input);
 		if (spec != null) sendFindItem(sender , spec);
 		else{
 			boolean found = false;
 			if (sender instanceof Player){
 				// TODO find region + show distance !
 				Player player = (Player) sender;
 				World world = player.getWorld();
 				String lcWorld = world.getName().toLowerCase();
 				Map<String, RegionSpec> rMap = regionMap.get(lcWorld);
 				if (rMap != null && rMap.containsKey(input.toLowerCase())){
 					ProtectedRegion r = Utils.getWorldGuard().getRegionManager(world).getRegion(input);
 					if (r != null){
 						double d = getDistanceToCenter(player.getLocation(), r);
 						player.sendMessage("[ShopService] Distance to center of "+r.getId()+": "+((int) Math.round(d)));
 						found = true;
 					}
 				}
 			}
 			if (!found) sender.sendMessage("[ShopService] No shops found.");
 		}
 	}
 
 	private double getDistanceToCenter(Location location, ProtectedRegion r) {
 		com.sk89q.worldedit.Vector middle = r.getMinimumPoint().add(r.getMaximumPoint()).multiply(0.5);
 		Vector center = new Vector(middle.getX(), middle.getY(), middle.getZ());
 		return location.toVector().distance(center);
 	}
 
 	private void sendFindItem(CommandSender sender, ItemSpec spec) {
 		Set<RegionSpec> specs = idMap.get(spec.id);
 		if ( specs == null){
 			sender.sendMessage("[ShopService] No shops found.");
 			return;
 		}
 		else{
 			String world = null;
 			if (sender instanceof Player){
 				world = ((Player) sender).getWorld().getName().toLowerCase();
 				// TODO: restrict to when players are on a filter region ?
 			}
 			sender.sendMessage("[ShopService] Shops with item type "+Material.getMaterial(spec.id).toString()+":");
 			// TODO: more sophisticated.
 			StringBuilder b = new StringBuilder();
 			b.append("Regions: ");
 			for (RegionSpec rSpec : specs ){
 				if (world != null && !rSpec.worldName.equals(world)) continue;
 				b.append(" ");
 				b.append(rSpec.regionName);
 				if (world == null) b.append("("+rSpec.worldName+")");
 			}	 
 			sender.sendMessage(b.toString());
 		}
 	}
 
 	private void sendInfo(CommandSender sender) {
 		if (sender instanceof Player) sendPlayerInfo((Player) sender);
 		sendGeneralInfo(sender);
 	}
 
 	private void sendPlayerInfo(Player player) {
 		World world = player.getWorld();
 		ApplicableRegionSet set = Utils.getWorldGuard().getRegionManager(world).getApplicableRegions(player.getLocation());
 		String lcWorld = world.getName().toLowerCase();
 		for (ProtectedRegion r : set){
 			String rid = r.getId();
 			RegionSpec rSpec = getRegionSpec(lcWorld, rid.toLowerCase(), false);
 			if (rSpec == null) continue;
 			player.sendMessage(rid+": "+rSpec.shops.size()+" chest shops.");
 		}
 		if (set.size() > 0) player.sendMessage("To list items for a region, use: /cx shop list <region>");
 	}
 
 	private void sendGeneralInfo(CommandSender sender) {
 		sender.sendMessage("[ShopService] General info: | "+idMap.size()+" Total item types | "+blockMap.size()+" total shops |");
 	}
 	
 	@Override
 	public void onEnable(Plugin plugin) {
 		loadSettings();
 		loadData();
 	}
 	
 	@Override
 	public void onDisable() {
 		saveData();
 		clear();
 	}
 
 	@Override
 	public void onRemove() {
 		clear();
 	}
 	
 	private void clearData() {
 		idMap.clear();
 		shopOwners.clear();
 		// not sure this is really needed, might make de referencing fatser, for later.
 		for (Map<String, RegionSpec> map : regionMap.values()){
 			for (RegionSpec rSpec : map.values()){
 				rSpec.shops.clear();
 			}
 		}
 		regionMap.clear();
 		for (ShopSpec spec : blockMap.values()){
 			spec.regions.clear();
 			spec.stack = null;
 		}
 		blockMap.clear();
 	}
 
 	private void clear() {
 		clearData();
 		filter.clear();
 	}
 
 	private File getDataFolder(){
 		File out = new File(new File(CMCore.getPlugin().getDataFolder(), "hooks"),"chestshop");
 		if (!out.exists()) out.mkdirs();
 		return out;
 	}
 
 	private File getSettingsFile() {
 		return new File (getDataFolder(), "settings.yml");
 	}
 	
 	private File getFilterFile() {
 		return new File (getDataFolder(), "filter.yml");
 	}
 	
 	private File getDataFile() {
 		return new File (getDataFolder(), "shops.yml");
 	}
 	
 	private static CompatConfig getDefaultConfig(){
 		CompatConfig cfg = CompatConfigFactory.getConfig(null);
 		cfg.set("use-filter", true);
 		cfg.set("add-unowned", false);
 		cfg.set("expiration-duration", 14);
 		return cfg;
 	}
 	
 	private void loadSettings() {
 		File file = getSettingsFile();
 		CompatConfig cfg = CompatConfigFactory.getConfig(file);
 		cfg.load();
 		if (ConfigUtil.forceDefaults(getDefaultConfig(), cfg)) cfg.save();
 		useFilter = cfg.getBoolean("use-filter", true);
 		addUnowned = cfg.getBoolean("add-unowned", false);
 		durExpire = cfg.getLong("expiration-duration")*msDay;
 		loadFilter();
 	}
 	
 	private void loadFilter() {
 		filter.clear();
 		File file = getFilterFile();
 		if (!file.exists()){
 			try {
 				file.createNewFile();
 			} catch (IOException e) {
 				Logging.warn("[ServiceHook/ChestShop] Could not create filter file", e);
 			}
 			return;
 		}
 		CompatConfig cfg = CompatConfigFactory.getConfig(file);
 		cfg.load();
 		for (String world : cfg.getStringKeys("allow-regions")){
 			for (String rid : cfg.getStringList("allow-regions."+world, new LinkedList<String>())){
 				addFilter(world, rid);
 			}
 		}
 	}
 	
 	private final void saveData() {
 		final File file = getDataFile();
 		final CompatConfig cfg = CompatConfigFactory.getConfig(file);
 		int i = 0;
 		final long ts = System.currentTimeMillis();
 		final List<FBlockPos> rem = new LinkedList<FBlockPos>();
 		for (final FBlockPos pos : blockMap.keySet()){
 			ShopSpec spec = blockMap.get(pos);
 			if (spec == null) continue; // overly ...
 			if (spec.priceBuy <0 && spec.priceSell < 0) continue; // overly ...
 			if (ts - spec.tsAccess > durExpire){
 				rem.add(pos);
 				continue;
 			}
 			i++;
 			final String keyBase = "s"+i+".";
 			cfg.set(keyBase+"w", pos.w);
 			cfg.set(keyBase+"x", pos.x);
 			cfg.set(keyBase+"y", pos.y);
 			cfg.set(keyBase+"z", pos.z);
 			cfg.set(keyBase+"id", spec.stack.getTypeId());
 			if (spec.owner != null) cfg.set(keyBase+"o", spec.owner);
 			if (spec.amount != 1) cfg.set(keyBase + "n", spec.amount);
 			final int d;
 			if (spec.stack.getType().isBlock()) d = spec.stack.getData().getData();
 			else d = spec.stack.getDurability();
 			if (d != 0) cfg.set(keyBase+"d", d);
 			if (spec.priceBuy>=0) cfg.set(keyBase+"pb", spec.priceBuy);
 			if (spec.priceSell>=0) cfg.set(keyBase+"ps", spec.priceSell);
 			cfg.set(keyBase+"ts", spec.tsAccess);
 		}
 		cfg.save();
 		for (final FBlockPos pos : rem){
 			removeShop(pos);
 		}
 	}
 	
 	private final void loadData() {
 		clearData();
 		final File file = getDataFile();
 		final CompatConfig cfg = CompatConfigFactory.getConfig(file);
 		cfg.load();
 		final long ts = System.currentTimeMillis();
 		final List<String> keys = cfg.getStringKeys();
 		for (final String key : keys){
 			// TODO: try catch !
 			try{
 				readShopSpec(cfg, key, ts);
 			}
 			catch(Throwable t){
 				Logging.warn("[ServiceHook/ChestShop3] Bad shop spec at: "+key, t);
 			}
 		}
 	}
 
 	/**
 	 * Read from config + add to internals
 	 * @param cfg
 	 * @param key
 	 * @param ts
 	 */
 	private final void readShopSpec(final CompatConfig cfg, final String key, final long ts) {
 		final String keyBase = key + ".";
 		final long tsA = cfg.getLong(keyBase+"ts", 0L);
 		if (ts - tsA > durExpire) return; // ignore expired entries.
 		final String w = cfg.getString(keyBase+"w");
 		final World world = Bukkit.getWorld(w);
 		if (world == null) return;
 		final int x = cfg.getInt(keyBase+"x");
 		final int y = cfg.getInt(keyBase+"y");
 		final int z = cfg.getInt(keyBase+"z");
		final String shopOwner = getShopOwner(cfg.getString(keyBase + "o", null));
 		FBlockPos pos = new FBlockPos(w, x, y, z);
 		Block block = world.getBlockAt(x, y, z);
 		final double pb = cfg.getDouble(keyBase+"pb", -1.0);
 		final double ps = cfg.getDouble(keyBase+"ps", -1.0);
 		final int id = cfg.getInt(keyBase+"id", 0);
 		final int amount = cfg.getInt(keyBase+"n", 1);
 		if (id == 0) return;
 		int d = cfg.getInt(keyBase+"d", 0);
 		final Material mat = Material.getMaterial(id);
 		final ItemStack stack;
 		if (mat.isBlock()) stack = new ItemStack(mat, 0, (short) 0, (byte) d);
 		else stack = new ItemStack(mat, 0, (short) d, (byte) 0);
 		checkAddShopSpec(pos, block, shopOwner, stack, amount, pb, ps);
 	}
 
 	/**
 	 * Get standard lower case name, ensure that references are used internally.
 	 * @param name
 	 * @return
 	 */
 	private final String getShopOwner(final String name) {
 		// TODO: add entries for long name mapping from chestshop ?
 		if (name == null) return null; // admin shop
 		final String lcn = name.trim().toLowerCase();
 		final String ref = shopOwners.get(lcn);
 		if (ref == null){
 			shopOwners.put(lcn, lcn);
 			return lcn;
 		}
 		return ref;
 	}
 	
 }
