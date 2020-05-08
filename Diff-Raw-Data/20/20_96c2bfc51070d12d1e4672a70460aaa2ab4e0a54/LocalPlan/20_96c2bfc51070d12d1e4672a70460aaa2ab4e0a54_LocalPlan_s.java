 package org.micoli.minecraft.localPlan;
 
 import static com.sk89q.worldguard.bukkit.BukkitUtil.toVector;
 
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Scanner;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.dynmap.DynmapCommonAPI;
 import org.dynmap.markers.Marker;
 import org.dynmap.markers.MarkerSet;
 import org.micoli.minecraft.bukkit.QDBukkitPlugin;
 import org.micoli.minecraft.bukkit.QDCommand;
 import org.micoli.minecraft.bukkit.QDCommandException;
 import org.micoli.minecraft.bukkit.QDCommandUsageException;
 import org.micoli.minecraft.localPlan.entities.InterestPoint;
 import org.micoli.minecraft.localPlan.entities.Parcel;
 import org.micoli.minecraft.localPlan.entities.Parcel.buyStatusTypes;
 import org.micoli.minecraft.localPlan.entities.Parcel.ownerTypes;
 import org.micoli.minecraft.localPlan.managers.LocalPlanCommandManager;
 import org.micoli.minecraft.utils.BlockUtils;
 import org.micoli.minecraft.utils.ChatFormater;
 import org.micoli.minecraft.utils.PluginEnvironment;
 import org.micoli.minecraft.utils.ServerLogger;
 
 import com.avaje.ebean.Expression;
 import com.avaje.ebean.Query;
 import com.sk89q.worldedit.BlockVector;
 import com.sk89q.worldedit.BlockVector2D;
 import com.sk89q.worldedit.Vector;
 import com.sk89q.worldedit.bukkit.WorldEditPlugin;
 import com.sk89q.worldedit.bukkit.selections.CuboidSelection;
 import com.sk89q.worldedit.bukkit.selections.Polygonal2DSelection;
 import com.sk89q.worldedit.bukkit.selections.Selection;
 import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
 import com.sk89q.worldguard.domains.DefaultDomain;
 import com.sk89q.worldguard.protection.ApplicableRegionSet;
 import com.sk89q.worldguard.protection.UnsupportedIntersectionException;
 import com.sk89q.worldguard.protection.databases.ProtectionDatabaseException;
 import com.sk89q.worldguard.protection.flags.DefaultFlag;
 import com.sk89q.worldguard.protection.flags.Flag;
 import com.sk89q.worldguard.protection.flags.InvalidFlagFormat;
 import com.sk89q.worldguard.protection.managers.RegionManager;
 import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
 import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
 import com.sk89q.worldguard.protection.regions.ProtectedRegion;
 
 // TODO: Auto-generated Javadoc
 /**
  * The Class LocalPlan.
  */
 public class LocalPlan extends QDBukkitPlugin implements ActionListener {
 
 	/** The my executor. */
 	protected LocalPlanCommandManager executor;
 
 	/** The instance. */
 	private static LocalPlan instance;
 
 	/** The internal array of parcels. */
 	public static Map<String, Parcel> aParcel;
 
 	/** The worldguard plugin. */
 	private WorldGuardPlugin worldGuardPlugin;
 
 	/** The worldedit plugin. */
 	private WorldEditPlugin worldEditPlugin;
 
 	/** The dynmap plugin. */
 	DynmapCommonAPI dynmapPlugin;
 	
 	/** The marker default price. */
 	public double markerDefaultPrice = 50;
 	
 	/** The marker maximum distance. */
 	public double markerMaximumDistance = 1000;
 
 	/** The preview blocks. */
 	private HashMap<String, List<Block>> previewBlocks = new HashMap<String, List<Block>>();
 	
 	/** The interest points. */
 	private HashMap<String, ArrayList<InterestPoint>> interestPoints;
 
 	/**
 	 * Gets the single instance of LocalPlan.
 	 * 
 	 * @return the instance
 	 */
 	public static LocalPlan getInstance() {
 		return instance;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.micoli.minecraft.bukkit.QDBukkitPlugin#onEnable()
 	 */
 	@Override
 	public void onEnable() {
 		instance = this;
 		commandString = "lp";
 		super.onEnable();
 		log(ChatFormater.format("%s version enabled", this.pdfFile.getName(), this.pdfFile.getVersion()));
 
 		aParcel = new HashMap<String, Parcel>();
 		worldGuardPlugin = PluginEnvironment.getWorldGuard(getServer());
 		worldEditPlugin = PluginEnvironment.getWorldEdit(getServer());
 		dynmapPlugin = (DynmapCommonAPI) getServer().getPluginManager().getPlugin("dynmap");
 
 		configFile.set("marker.defaultPrice", configFile.getDouble("marker.defaultPrice", 50));
 		markerDefaultPrice = configFile.getDouble("marker.defaultPrice", 50);
 		
 		configFile.set("marker.markerMaximumDistance", configFile.getDouble("marker.markerMaximumDistance", 300));
 		markerMaximumDistance = configFile.getDouble("marker.defaultPrice", 300);
 		saveConfig();
 
		initializeInterestsPoint();
		initalizeRegions();
 
 		executor = new LocalPlanCommandManager(this, new Class[] { getClass() });
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.micoli.minecraft.bukkit.QDBukkitPlugin#getDatabaseORMClasses()
 	 */
 	protected java.util.List<Class<?>> getDatabaseORMClasses() {
 		List<Class<?>> list = new ArrayList<Class<?>>();
 		list.add(Parcel.class);
 		return list;
 	};
 
 	/**
 	 * List parcels.
 	 *
 	 * @param player the player
 	 * @param owner the owner
 	 * @param buyStatus the buy status
 	 * @param ownerType the owner type
 	 */
 	public void listParcels(Player player, String owner, buyStatusTypes buyStatus, ownerTypes ownerType) {
 		String ownerArg = owner;
 		String buyStatusArg = buyStatus.toString();
 		String ownerTypeArg = ownerType.toString();
 
 		if (owner.equalsIgnoreCase("__all__")) {
 			owner = "ALL";
 			ownerArg = "%";
 		}
 
 		if (buyStatus.equals(Parcel.buyStatusTypes.ANY)) {
 			buyStatusArg = "%";
 		}
 
 		if (ownerType.equals(Parcel.ownerTypes.ANY)) {
 			ownerTypeArg = "%";
 		}
 
 		//todo revoir l'ordre des resultats pour les alignements
 		Iterator<Parcel> parcelIterator = getStaticDatabase().find(Parcel.class).where().like("owner", ownerArg).like("buyStatus", buyStatusArg).like("ownerType", ownerTypeArg).orderBy("id desc").findList().iterator();
 
 		if (parcelIterator.hasNext()) {
 			sendComments(player, ChatFormater.format("List of owned parcels"));
 			while (parcelIterator.hasNext()) {
 				Parcel re = parcelIterator.next();
 				sendComments(player, ChatFormater.format("%5s:%15s:%8s:%8s:%8s", re.getWorld(), re.getRegionId(), re.getBuyStatus(), re.getOwner(), re.getOwnerType()));
 			}
 			sendComments(player, ChatFormater.format("-----------"));
 		} else {
 			sendComments(player, ChatFormater.format("No parcels", owner));
 		}
 	}
 	
 	/**
 	 * Initialize interests point.
 	 */
	public void initializeInterestsPoint(){
 		interestPoints = new HashMap<String, ArrayList<InterestPoint>>();
 
 		MarkerSet localPlanMarkerSet = dynmapPlugin.getMarkerAPI().getMarkerSet("LocalPlanPOI");
 		Iterator<Marker> localPlanMarkerSetIterator = localPlanMarkerSet.getMarkers().iterator();
 		Pattern pattern = Pattern.compile("-(\\d+(\\.\\d+)?)$");
 		while (localPlanMarkerSetIterator.hasNext()) {
 			Marker marker = localPlanMarkerSetIterator.next();
 			Matcher matcher = pattern.matcher(marker.getLabel());
 			if (matcher.find()) {
 				if (!interestPoints.containsKey(marker.getWorld())) {
 					interestPoints.put(marker.getWorld(), new ArrayList<InterestPoint>());
 				}
 				interestPoints.get(marker.getWorld()).add(new InterestPoint(marker.getWorld(), marker.getMarkerSet(), marker.getLabel(), matcher.group().substring(1),new BlockVector2D( marker.getX(),marker.getZ())));
 				ServerLogger.log("Markers : %s ", marker.getLabel());
 			}
 		}
 	}
 
 	/**
 	 * Initalize regions.
 	 */
 	public void initalizeRegions() {
 		final int maxdepth = 10;
 		ServerLogger.log("InitalizeRegions");
 		for (World w : getServer().getWorlds()) {
 			String worldName = w.getName();
 			List<String> listRegions = new ArrayList<String>();
 			Map<?, Parcel> listParcels = getStaticDatabase().find(Parcel.class).where().like("world", worldName).findMap();
 			ServerLogger.log("Map %s (%d)", worldName, listParcels.size());
 
 			RegionManager rm = worldGuardPlugin.getRegionManager(w);
 			if (rm == null)
 				continue;
 
 			Map<String, ProtectedRegion> regions = rm.getRegions();
 			for (ProtectedRegion pr : regions.values()) {
 				int depth = 1;
 				ProtectedRegion p = pr;
 				while (p.getParent() != null) {
 					depth++;
 					p = p.getParent();
 				}
 				if (depth > maxdepth) {
 					continue;
 				}
 				if (!pr.getId().equalsIgnoreCase("__global__")) {
 					ServerLogger.log("W:%s,P:%s,O:%s,S:%d", worldName, pr.getId(), pr.getOwners().toPlayersString(), LocalPlanUtils.getRegionSurface(pr));
 					listRegions.add(pr.getId());
 					if (!listParcels.containsKey(worldName + "::" + pr.getId())) {
 						Parcel parcel = new Parcel(worldName, pr);
 						parcel.save();
 						ServerLogger.log("Automatically adding %s::%s(%s)", worldName, pr.getId(), pr.getOwners().toPlayersString());
 					} else {
 						Parcel parcel = listParcels.get(worldName + "::" + pr.getId());
 						if (parcel.getSurface() != LocalPlanUtils.getRegionSurface(pr)) {
 							parcel.setPriceAndSurface(worldName, pr);
 							parcel.save();
 							ServerLogger.log("Updating surface of  %s::%s(%d)", worldName, pr.getId(), parcel.getSurface());
 						}
 					}
 				}
 			}
 			ServerLogger.log("List of deleted parcels");
 			Query<?> query = getStaticDatabase().find(Parcel.class);
 			Expression exp = query.getExpressionFactory().in("regionId", listRegions);
 			@SuppressWarnings("unchecked")
 			Iterator<Parcel> listDeletedParcels = (Iterator<Parcel>) query.where().like("world", worldName).not(exp).findList().iterator();
 			while (listDeletedParcels.hasNext()) {
 				Parcel parcel = listDeletedParcels.next();
 				ServerLogger.log("Automatically removing W:%s,P:%s", worldName, parcel.getRegionId());
 				parcel.delete();
 			}
 		}
 		ServerLogger.log("EndInitalizeRegions");
 	}
 
 	/**
 	 * Gets the parcel around.
 	 * 
 	 * @param player
 	 *            the player
 	 * @return the parcel around
 	 */
 	public void getParcelAround(Player player) {
 		World w = player.getWorld();
 		Vector pt = toVector(player.getLocation());
 
 		ApplicableRegionSet set = worldGuardPlugin.getRegionManager(w).getApplicableRegions(pt);
 
 		ServerLogger.log("list %s %s", player.getName(), set.toString());
 		for (ProtectedRegion reg : set) {
 			ServerLogger.log("%s %s", player.getName(), reg.getId());
 		}
 		ServerLogger.log("--list %s", player.getName());
 	}
 
 	/**
 	 * Creates the parcel.
 	 *
 	 * @param player the player
 	 * @param parcelName the parcel name
 	 * @throws Exception the exception
 	 */
 	public void createParcel(Player player, String parcelName) throws Exception {
 		if (!ProtectedRegion.isValidId(parcelName)) {
 			throw new QDCommandException("Invalid region ID specified!");
 		}
 
 		if (parcelName.equalsIgnoreCase("__global__")) {
 			throw new QDCommandException("A region cannot be named __global__");
 		}
 
 		// Attempt to get the player's selection from WorldEdit
 		Selection sel = worldEditPlugin.getSelection(player);
 
 		if (sel == null) {
 			throw new QDCommandException("Select a region with WorldEdit first.");
 		}
 
 		World w = sel.getWorld();
 		RegionManager mgr = worldGuardPlugin.getGlobalRegionManager().get(w);
 		if (mgr.hasRegion(parcelName)) {
 			throw new QDCommandException("That region is already defined. Use redefine instead.");
 		}
 
 		ProtectedRegion region;
 
 		// Detect the type of region from WorldEdit
 		if (sel instanceof Polygonal2DSelection) {
 			Polygonal2DSelection polySel = (Polygonal2DSelection) sel;
 			region = new ProtectedPolygonalRegion(parcelName, polySel.getNativePoints(), 0, w.getMaxHeight());
 		} else if (sel instanceof CuboidSelection) {
 			BlockVector min = sel.getNativeMinimumPoint().setY(0).toBlockVector();
 			BlockVector max = sel.getNativeMaximumPoint().setY(w.getMaxHeight()).toBlockVector();
 			region = new ProtectedCuboidRegion(parcelName, min, max);
 			ServerLogger.log("region %s %s %d",min.toString(),max.toString(), w.getMaxHeight());
 		} else {
 			throw new QDCommandException("The type of region selected in WorldEdit is unsupported in WorldGuard!");
 		}
 		List<ProtectedRegion> allregionslist = new ArrayList<ProtectedRegion>(mgr.getRegions().values());
 		List<ProtectedRegion> overlaps;
 
 		try {
 			overlaps = region.getIntersectingRegions(allregionslist);
 			if (!(overlaps == null || overlaps.isEmpty())) {
 				throw new QDCommandException("That region is overlapping an existing one.");
 			}
 		} catch (UnsupportedIntersectionException e) {
 			e.printStackTrace();
 		}
 		// Get the list of region owners
 		DefaultDomain own = new DefaultDomain();
 		own.addPlayer(player.getName());
 		region.setOwners(own);
 		setRegionFlag(player, region, "BUILD", "allow");
 
 		mgr.addRegion(region);
 		Parcel parcel = Parcel.getParcel(w.getName(), parcelName);
 		if(parcel==null){
 			parcel = new Parcel(w.getName(), region);
 		}else{
 			parcel.setPriceAndSurface(w.getName(), region);
 		}
 		parcel.save();
 
 		try {
 			mgr.save();
 			sendComments(player, ChatColor.YELLOW + "Region saved as " + parcelName + ".", false);
 		} catch (ProtectionDatabaseException e) {
 			throw new QDCommandException("Failed to write regions: " + e.getMessage());
 		}
 	}
 
 	/**
 	 * Sets the flag.
 	 * 
 	 * @param <V>
 	 *            the value type
 	 * @param region
 	 *            the region
 	 * @param flag
 	 *            the flag
 	 * @param sender
 	 *            the sender
 	 * @param value
 	 *            the value
 	 * @throws InvalidFlagFormat
 	 *             the invalid flag format
 	 * @author sk89q
 	 */
 	public <V> void setFlag(ProtectedRegion region, Flag<V> flag, CommandSender sender, String value) throws InvalidFlagFormat {
 		region.setFlag(flag, flag.parseInput(worldGuardPlugin, sender, value));
 	}
 
 	/**
 	 * Sets the region flag.
 	 * 
 	 * @param player
 	 *            the player
 	 * @param region
 	 *            the region
 	 * @param flagName
 	 *            the flag name
 	 * @param flagValue
 	 *            the flag value
 	 * @author sk89q
 	 */
 	private void setRegionFlag(Player player, ProtectedRegion region, String flagName, String flagValue) {
 		Flag<?> foundFlag = null;
 		// Now time to find the flag!
 		for (Flag<?> flag : DefaultFlag.getFlags()) {
 			// Try to detect the flag
 			if (flag.getName().replace("-", "").equalsIgnoreCase(flagName)) {
 				foundFlag = flag;
 				break;
 			}
 		}
 
 		if (foundFlag != null) {
 			try {
 				setFlag(region, foundFlag, player, flagValue);
 			} catch (InvalidFlagFormat e) {
 				e.printStackTrace();
 			}
 		}
 
 	}
 
 	/**
 	 * Teleport the player to parcel.
 	 *
 	 * @param player the player
 	 * @param parcelName the parcel name
 	 */
 	public void teleportToParcel(Player player, String parcelName) {
 		World w = player.getWorld();
 		RegionManager rm = worldGuardPlugin.getRegionManager(w);
 		ProtectedRegion region = rm.getRegion(parcelName);
 		if (region != null) {
 			final BlockVector min = region.getMinimumPoint();
 			final BlockVector max = region.getMaximumPoint();
 			Location dstLocation = BlockUtils.getTopPositionAtPos(new Location(w, (double) (min.getBlockX() + max.getBlockX()) / 2, (double) 0, (double) (min.getBlockZ() + max.getBlockZ()) / 2));
 			player.teleport(dstLocation);
 		}
 	}
 
 	/**
 	 * Allocate parcel.
 	 *
 	 * @param player the player
 	 * @param WorldId the world id
 	 * @param parcelName the parcel name
 	 * @param newOwner the new owner
 	 * @throws QDCommandException the qD command exception
 	 */
 	public void allocateParcel(Player player, String WorldId, String parcelName, String newOwner) throws QDCommandException {
 		Parcel parcel = Parcel.getParcel(WorldId, parcelName);
 		if (parcel == null) {
 			throw new QDCommandException("Parcel not found");
 		}
 
 		RegionManager mgr = worldGuardPlugin.getGlobalRegionManager().get(getServer().getWorld(parcel.getWorld()));
 		ProtectedRegion region = mgr.getRegion(parcel.getRegionId());
 
 		DefaultDomain own = new DefaultDomain();
 		own.addPlayer(newOwner);
 		region.setOwners(own);
 
 		parcel.setOwner(newOwner);
 		parcel.setBuyStatus(Parcel.buyStatusTypes.UNBUYABLE);
 		parcel.save();
 
 		sendComments(player, ChatFormater.format("Allocation of %s to %s done", parcelName, newOwner));
 	}
 
 	/**
 	 * Sets the buyable.
 	 *
 	 * @param player the player
 	 * @param parcelName the parcel name
 	 * @param priceString the price string
 	 * @throws Exception the exception
 	 */
 	public void setBuyable(Player player, String parcelName, String priceString) throws Exception {
 		Parcel parcel = Parcel.getParcel(player.getWorld().toString(), parcelName, player);
 		if (parcel == null) {
 			throw new QDCommandException("Parcel not found or doesn't belong to you");
 		}
 		Scanner scanner = new Scanner(priceString);
 		if (!scanner.hasNextDouble()) {
 			throw new QDCommandException("Price not found or not the good format 99.9");
 		}
 		double price = scanner.nextDouble();
 		parcel.setPrice(price);
 		parcel.setBuyStatus(Parcel.buyStatusTypes.BUYABLE);
 		parcel.save();
 		sendComments(player, ChatFormater.format("Parcel %s is now buyable at the following price %f", parcelName, price));
 	}
 
 	/**
 	 * Sets the unbuyable.
 	 *
 	 * @param player the player
 	 * @param parcelName the parcel name
 	 * @throws Exception the exception
 	 */
 	public void setUnbuyable(Player player, String parcelName) throws Exception {
 		Parcel parcel = Parcel.getParcel(player.getWorld().toString(), parcelName, player);
 		if (parcel == null) {
 			throw new QDCommandException("Parcel not found or doesn't belong to you");
 		}
 		parcel.setBuyStatus(Parcel.buyStatusTypes.UNBUYABLE);
 		parcel.save();
 		sendComments(player, ChatFormater.format("Parcel %s is now unbuyable ", parcelName));
 	}
 
 	/**
 	 * Buy parcel.
 	 *
 	 * @param player the player
 	 * @param parcelName the parcel name
 	 * @throws Exception the exception
 	 */
 	public void buyParcel(Player player, String parcelName) throws Exception {
 		Parcel parcel = Parcel.getParcel(player.getWorld().toString(), parcelName);
 		if (parcel == null) {
 			throw new QDCommandException("Parcel not found");
 		}
 
 		if (parcel.getBuyStatus().equals(Parcel.buyStatusTypes.UNBUYABLE)) {
 			throw new QDCommandException("Parcel is not buyable");
 		}
 
 		if (parcel.getPrice() > vaultEconomy.getBalance(player.getName())) {
 			throw new QDCommandException(ChatFormater.format("Not enough money to buy that parcel %f<%f", vaultEconomy.getBalance(player.getName()), parcel.getPrice()));
 		}
 		vaultEconomy.depositPlayer(parcel.getOwner(), parcel.getPrice());
 		vaultEconomy.withdrawPlayer(player.getName(), parcel.getPrice());
 		allocateParcel(player, player.getWorld().toString(), parcelName, player.getDisplayName());
 		sendComments(player, ChatFormater.format("Parcel %s bought ", parcelName));
 
 	}
 
 	/**
 	 * Manage parcel member.
 	 *
 	 * @param player the player
 	 * @param args the args
 	 * @throws Exception the exception
 	 */
 	public void manageParcelMember(Player player, String[] args) throws Exception {
 		String parcelName = args[1];
 		Parcel parcel = Parcel.getParcel(player.getWorld().toString(), parcelName);
 		if (parcel == null) {
 			throw new QDCommandException("Parcel not found");
 		}
 
 		if (!(parcel.getOwner().equalsIgnoreCase(player.getName()) || vaultPermission.playerHas(player, "localPlan.members.allow"))) {
 			throw new QDCommandException("You don't have right on that Parcel");
 		}
 		sendComments(player, "You have right on that Parcel, but nothing is coded ^^");
 	}
 
 	/**
 	 * Show parcel.
 	 *
 	 * @param player the player
 	 * @param parcelName the parcel name
 	 * @throws Exception the exception
 	 */
 	public void showParcel(Player player, String parcelName) throws Exception {
 		Parcel parcel = Parcel.getParcel(player.getWorld().toString(), parcelName);
 		if (parcel == null) {
 			throw new QDCommandException("Parcel not found " + player.getWorld().toString() + "::" + parcelName);
 		}
 		if (!(parcel.getOwner().equalsIgnoreCase(player.getName()) || vaultPermission.playerHas(player, "localPlan.members.allow"))) {
 			throw new QDCommandException("You don't have right on that Parcel");
 		}
 		if (previewBlocks.containsKey(player.getWorld().getName() + "::" + parcel.getRegionId())) {
 			throw new QDCommandException("Parcel preview already shown");
 		}
 		List<Block> listBlock = new ArrayList<Block>();
 		previewBlocks.put(player.getWorld().getName() + "::" + parcel.getRegionId(), listBlock);
 		RegionManager mgr = worldGuardPlugin.getGlobalRegionManager().get(getServer().getWorld(parcel.getWorld()));
 		ProtectedRegion region = mgr.getRegion(parcel.getRegionId());
 
 		int nb = 0;
 		World world = getServer().getWorld(parcel.getWorld());
 		List<BlockVector2D> points = region.getPoints();
 		if (points != null && points.size() > 0) {
 			BlockVector2D firstPoint = points.get(0);
 			BlockVector2D lastPoint = points.get(points.size() - 1);
 			if (region.getTypeName().equalsIgnoreCase("cuboid")) {
 				lastPoint = points.get(2);
 				points.set(2, points.get(3));
 				points.set(3, lastPoint);
 			}
 			for (int i = 0; i < points.size(); i++) {
 				if (nb == 0) {
 					firstPoint = points.get(i);
 					lastPoint = firstPoint;
 				} else {
 					BlockVector2D point = points.get(i);// pointIterator.next();
 					BlockUtils.drawLineOnTop(new Location(world, lastPoint.getX(), 0, lastPoint.getZ()), new Location(world, point.getX(), 0, point.getZ()), Material.FENCE, listBlock);
 					// sendComments(player, ChatFormater.format("Point %f,%f",
 					// point.getX(), point.getZ()));
 					lastPoint = point;
 				}
 				nb++;
 			}
 			// sendComments(player, ChatFormater.format("Point %f,%f",
 			// firstPoint.getX(), firstPoint.getZ()));
 			BlockUtils.drawLineOnTop(new Location(world, lastPoint.getX(), 0, lastPoint.getZ()), new Location(world, firstPoint.getX(), 0, firstPoint.getZ()), Material.FENCE, listBlock);
 			sendComments(player, "Parcel shown");
 		}
 	}
 
 	/**
 	 * Hide parcel.
 	 *
 	 * @param player the player
 	 * @param parcelName the parcel name
 	 * @throws Exception the exception
 	 */
 	public void hideParcel(Player player, String parcelName) throws Exception {
 		Parcel parcel = Parcel.getParcel(player.getWorld().getName(), parcelName);
 		if (parcel == null) {
 			throw new QDCommandException("Parcel not found");
 		}
 		if (!(parcel.getOwner().equalsIgnoreCase(player.getName()) || vaultPermission.playerHas(player, "localPlan.members.allow"))) {
 			throw new QDCommandException("You don't have right on that Parcel");
 		}
 		if (!previewBlocks.containsKey(player.getWorld().getName() + "::" + parcel.getRegionId())) {
 			throw new QDCommandException("Parcel preview already shown");
 		}
 		World world = getServer().getWorld(parcel.getWorld());
 		List<Block> listBlock = previewBlocks.get(player.getWorld().getName() + "::" + parcel.getRegionId());
 		for (Iterator<Block> pointIterator = listBlock.iterator(); pointIterator.hasNext();) {
 			Block block = pointIterator.next();
 			world.getBlockAt(block.getLocation()).setTypeId(0);
 		}
 
 		previewBlocks.remove(player.getWorld().getName() + "::" + parcel.getRegionId());
 		sendComments(player, "Parcel hidden");
 	}
 
 	/**
 	 * Gets the interest points.
 	 *
 	 * @return the interestPoints
 	 */
 	public HashMap<String, ArrayList<InterestPoint>> getInterestPoints() {
 		return interestPoints;
 	}
 
 	/**
 	 * Gets the parcel.
 	 *
 	 * @param worldId the world id
 	 * @param parcelName the parcel name
 	 * @return the parcel
 	 */
 	public Parcel getParcel(String worldId, String parcelName) {
 		return Parcel.getParcel(worldId, parcelName);
 	}
 
 	/**
 	 * Cmd_comments on.
 	 *
 	 * @param sender the sender
 	 * @param command the command
 	 * @param label the label
 	 * @param args the args
 	 */
 	@QDCommand(aliases = "commentsOn", permissions = {},usage="",description="enable plugin comments")
 	public void cmd_commentsOn(CommandSender sender, Command command, String label, String[] args) {
 		setComments((Player) sender, true);
 	}
 
 	/**
 	 * Cmd_comments off.
 	 *
 	 * @param sender the sender
 	 * @param command the command
 	 * @param label the label
 	 * @param args the args
 	 */
 	@QDCommand(aliases = "commentsOff", permissions = {},usage="",description="disabled plugin comments")
 	public void cmd_commentsOff(CommandSender sender, Command command, String label, String[] args) {
 		setComments((Player) sender, false);
 	}
 
 	/**
 	 * Cmd_list.
 	 *
 	 * @param sender the sender
 	 * @param command the command
 	 * @param label the label
 	 * @param args the args
 	 * @throws Exception the exception
 	 */
 	@QDCommand(aliases = "list", permissions = { "localplan.list" },usage="[<player>]",description="list all parcel belonging to a given player, if no player given then use the current player")
 	public void cmd_list(CommandSender sender, Command command, String label, String[] args) throws Exception {
 		if (args.length == 1) {
 			this.listParcels((Player) sender, ((Player) sender).getName(), buyStatusTypes.ANY, ownerTypes.ANY);
 		} else {
 			if (args.length == 2) {
 				this.listParcels((Player) sender, args[1], buyStatusTypes.ANY, ownerTypes.ANY);
 			} else {
 				throw new QDCommandException("Too many arguments");
 			}
 		}
 	}
 
 	/**
 	 * Cmd_listall.
 	 *
 	 * @param sender the sender
 	 * @param command the command
 	 * @param label the label
 	 * @param args the args
 	 */
 	@QDCommand(aliases = "listall", permissions = {"localplan.listall"},usage="",description="list all parcels")
 	public void cmd_listall(CommandSender sender, Command command, String label, String[] args) {
 		this.listParcels((Player) sender, "__all__", buyStatusTypes.ANY, ownerTypes.ANY);
 	}
 
 	/**
 	 * Cmd_listavailable.
 	 *
 	 * @param sender the sender
 	 * @param command the command
 	 * @param label the label
 	 * @param args the args
 	 */
 	@QDCommand(aliases = "listavailable", permissions = {"localplan.listavailable"},usage="",description="list all parcels with no owner")
 	public void cmd_listavailable(CommandSender sender, Command command, String label, String[] args) {
 		this.listParcels((Player) sender, "", buyStatusTypes.ANY, ownerTypes.ANY);
 	}
 
 	/**
 	 * Cmd_listbuyable.
 	 *
 	 * @param sender the sender
 	 * @param command the command
 	 * @param label the label
 	 * @param args the args
 	 */
 	@QDCommand(aliases = "listbuyable", permissions = {"localplan.listbuyable"},usage="",description="list all buyable parcels")
 	public void cmd_listbuyable(CommandSender sender, Command command, String label, String[] args) {
 		this.listParcels((Player) sender, "__all__", buyStatusTypes.BUYABLE, ownerTypes.ANY);
 	}
 
 	/**
 	 * Cmd_buyable.
 	 *
 	 * @param sender the sender
 	 * @param command the command
 	 * @param label the label
 	 * @param args the args
 	 * @throws Exception the exception
 	 */
 	@QDCommand(aliases = "buyable", permissions = {"localplan.setbuyable"},usage="<parcelName> <price>",description="put a parcel on the market, set it as buyable to the given price")
 	public void cmd_buyable(CommandSender sender, Command command, String label, String[] args) throws Exception {
 		this.setBuyable((Player) sender, args[1], args[2]);
 	}
 
 	/**
 	 * Cmd_unbuyable.
 	 *
 	 * @param sender the sender
 	 * @param command the command
 	 * @param label the label
 	 * @param args the args
 	 * @throws Exception the exception
 	 */
 	@QDCommand(aliases = "unbuyable", permissions = {"localplan.setunbuyable"},usage="<parcelName>",description="set a parcel unbuyable, disallow to buy it")
 	public void cmd_unbuyable(CommandSender sender, Command command, String label, String[] args) throws Exception {
 		this.setUnbuyable((Player) sender, args[1]);
 	}
 
 	/**
 	 * Cmd_buy.
 	 *
 	 * @param sender the sender
 	 * @param command the command
 	 * @param label the label
 	 * @param args the args
 	 * @throws Exception the exception
 	 */
 	@QDCommand(aliases = "buy", permissions = {"localplan.buy"},usage="<parcelName>",description="buy a parcel if it is buyable, use economy")
 	public void cmd_buy(CommandSender sender, Command command, String label, String[] args) throws Exception {
 		this.buyParcel((Player) sender, args[1]);
 	}
 
 	/**
 	 * Cmd_tp.
 	 *
 	 * @param sender the sender
 	 * @param command the command
 	 * @param label the label
 	 * @param args the args
 	 */
 	@QDCommand(aliases = "tp", permissions = {"localplan.teleport"},usage="<parcelName>",description="Teleport the player to the center of the parcel")
 	public void cmd_tp(CommandSender sender, Command command, String label, String[] args) {
 		this.teleportToParcel((Player) sender, args[1]);
 	}
 
 	/**
 	 * Cmd_create.
 	 *
 	 * @param sender the sender
 	 * @param command the command
 	 * @param label the label
 	 * @param args the args
 	 * @throws Exception the exception
 	 */
 	@QDCommand(aliases = "create", permissions = {"localplan.create"},usage="<parcelName>",description="define a region and the parcel affected to it, no owner attributed to STATE")
 	public void cmd_create(CommandSender sender, Command command, String label, String[] args) throws Exception {
 		this.createParcel((Player) sender, args[1]);
 	}
 
 	/**
 	 * Cmd_allocate.
 	 *
 	 * @param sender the sender
 	 * @param command the command
 	 * @param label the label
 	 * @param args the args
 	 * @throws QDCommandException the qD command exception
 	 */
 	@QDCommand(aliases = "allocate", permissions = {"localplan.allocate"},usage="<parcelName> <newOwner>",description="allocate a parcel to an owner without economy")
 	public void cmd_allocate(CommandSender sender, Command command, String label, String[] args) throws QDCommandException {
 		if (args.length != 3) {
 			throw new QDCommandUsageException("need 3 arguments");
 		}
 		this.allocateParcel((Player) sender, ((Player) sender).getWorld().getName(), args[1], args[2]);
 	}
 
 	/**
 	 * Cmd_member.
 	 *
 	 * @param sender the sender
 	 * @param command the command
 	 * @param label the label
 	 * @param args the args
 	 * @throws Exception the exception
 	 */
 	@QDCommand(aliases = "member", permissions = {"localplan.member.set"},usage="<memberName>",description="change the member of a parcel")
 	public void cmd_member(CommandSender sender, Command command, String label, String[] args) throws Exception {
 		this.manageParcelMember((Player) sender, args);
 	}
 
 	/**
 	 * Cmd_show.
 	 *
 	 * @param sender the sender
 	 * @param command the command
 	 * @param label the label
 	 * @param args the args
 	 * @throws Exception the exception
 	 */
 	@QDCommand(aliases = "show", permissions = {"localplan.show"},usage="<parcelName>",description="show the border of a parcel in fence")
 	public void cmd_show(CommandSender sender, Command command, String label, String[] args) throws Exception {
 		this.showParcel((Player) sender, args[1]);
 	}
 
 	/**
 	 * Cmd_hide.
 	 *
 	 * @param sender the sender
 	 * @param command the command
 	 * @param label the label
 	 * @param args the args
 	 * @throws Exception the exception
 	 */
 	@QDCommand(aliases = "hide", permissions = {"localplan.hide"},usage="<parcelName>",description="hide the border of a parcel")
 	public void cmd_hide(CommandSender sender, Command command, String label, String[] args) throws Exception {
 		this.hideParcel((Player) sender, args[1]);
 	}
 
 	/**
 	 * Cmd_scan.
 	 *
 	 * @param sender the sender
 	 * @param command the command
 	 * @param label the label
 	 * @param args the args
 	 */
 	@QDCommand(aliases = "scan", permissions = {"localplan.scan"},usage="",description="rescan region and add parcels")
 	public void cmd_scan(CommandSender sender, Command command, String label, String[] args) {
		this.initalizeRegions();
 	}
 }
