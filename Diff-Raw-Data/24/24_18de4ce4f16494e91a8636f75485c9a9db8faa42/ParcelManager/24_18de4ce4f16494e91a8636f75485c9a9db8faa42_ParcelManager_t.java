 package org.micoli.minecraft.localPlan.managers;
 
 import static com.sk89q.worldguard.bukkit.BukkitUtil.toVector;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Scanner;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.micoli.minecraft.bukkit.QDCommandException;
 import org.micoli.minecraft.localPlan.LocalPlan;
 import org.micoli.minecraft.localPlan.LocalPlanUtils;
 import org.micoli.minecraft.localPlan.entities.Parcel;
 import org.micoli.minecraft.localPlan.entities.Parcel.buyStatusTypes;
 import org.micoli.minecraft.localPlan.entities.Parcel.ownerTypes;
 import org.micoli.minecraft.localPlan.entities.ParcelHistory;
 import org.micoli.minecraft.utils.BlockUtils;
 import org.micoli.minecraft.utils.ChatFormater;
 import org.micoli.minecraft.utils.PluginEnvironment;
 import org.micoli.minecraft.utils.StringUtils;
 
 import com.avaje.ebean.Expression;
 import com.avaje.ebean.Query;
 import com.sk89q.worldedit.BlockVector;
 import com.sk89q.worldedit.Vector;
 import com.sk89q.worldedit.bukkit.selections.CuboidSelection;
 import com.sk89q.worldedit.bukkit.selections.Polygonal2DSelection;
 import com.sk89q.worldedit.bukkit.selections.Selection;
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
 
 /**
  * The Class ParcelManager.
  */
 public class ParcelManager {
 
 	/** The plugin. */
 	private LocalPlan plugin;
 	/** The internal array of parcels. */
 	private Map<String, Parcel> aParcel;
 
 	/**
 	 * Instantiates a new parcel manager.
 	 *
 	 * @param instance the instance
 	 */
 	public ParcelManager(LocalPlan instance) {
 		this.plugin = instance;
 		aParcel = new HashMap<String, Parcel>();
 	}
 	
 	/**
 	 * @return the aParcel
 	 */
 	public Map<String, Parcel> getaParcel() {
 		return aParcel;
 	}
 
 	/**
 	 * @param aParcel the aParcel to set
 	 */
 	public void setaParcel(Map<String, Parcel> aParcel) {
 		this.aParcel = aParcel;
 	}
 
 
 	/**
 	 * Initalize regions.
 	 */
 	public void initalizeRegions() {
 		final int maxdepth = 10;
 		plugin.logger.log("InitalizeRegions");
 		for (World w : plugin.getServer().getWorlds()) {
 			String worldName = w.getName();
 			List<String> listRegions = new ArrayList<String>();
 			Map<?, Parcel> listParcels = plugin.getStaticDatabase().find(Parcel.class).where().like("world", worldName).findMap();
 			plugin.logger.log("Map %s (%d)", worldName, listParcels.size());
 
 			RegionManager rm = getRegionManager(worldName);
 			if (rm == null){
 				continue;
 			}
 
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
 					plugin.logger.log("W:%s,P:%s,O:%s,S:%d", worldName, pr.getId(), pr.getOwners().toPlayersString(), LocalPlanUtils.getRegionSurface(pr));
 					listRegions.add(pr.getId());
 					if (!listParcels.containsKey(worldName + "::" + pr.getId())) {
 						Parcel parcel = new Parcel(worldName, pr);
 						parcel.save();
 						new ParcelHistory(parcel,ParcelHistory.historyTypes.CREATION,"",true);
 						plugin.logger.log("Automatically adding %s::%s(%s)", worldName, pr.getId(), pr.getOwners().toPlayersString());
 					} else {
 						Parcel parcel = listParcels.get(worldName + "::" + pr.getId());
 						if (parcel.getSurface() != LocalPlanUtils.getRegionSurface(pr)) {
 							parcel.setPriceAndSurface(worldName, pr);
 							parcel.save();
 							new ParcelHistory(parcel,ParcelHistory.historyTypes.MODIFICATION,"",true);
 							plugin.logger.log("Updating surface of  %s::%s(%d)", worldName, pr.getId(), parcel.getSurface());
 						}
 					}
 				}
 			}
 			plugin.logger.log("List of deleted parcels");
 			Query<?> query = plugin.getStaticDatabase().find(Parcel.class);
 			Expression exp = query.getExpressionFactory().in("regionId", listRegions);
 			@SuppressWarnings("unchecked")
 			Iterator<Parcel> listDeletedParcels = (Iterator<Parcel>) query.where().like("world", worldName).not(exp).findList().iterator();
 			while (listDeletedParcels.hasNext()) {
 				Parcel parcel = listDeletedParcels.next();
 				plugin.logger.log("Automatically removing W:%s,P:%s", worldName, parcel.getRegionId());
 				parcel.delete();
 			}
 		}
 		plugin.logger.log("EndInitalizeRegions");
 	}
 
 	/**
 	 * List parcels.
 	 * 
 	 * @param player
 	 *            the player
 	 * @param owner
 	 *            the owner
 	 * @param buyStatus
 	 *            the buy status
 	 * @param ownerType
 	 *            the owner type
 	 */
 	public void listParcels(Player player, String owner, buyStatusTypes buyStatus, ownerTypes ownerType) {
 		String ownerArg = owner;
 		String buyStatusArg = buyStatus.toString();
 		String ownerTypeArg = ownerType.toString();
 
 		if (owner.equalsIgnoreCase("__all__")) {
 			ownerArg = "%";
 		}
 
 		if (buyStatus.equals(Parcel.buyStatusTypes.ANY)) {
 			buyStatusArg = "%";
 		}
 
 		if (ownerType.equals(Parcel.ownerTypes.ANY)) {
 			ownerTypeArg = "%";
 		}
 
 		Iterator<Parcel> parcelIterator = plugin.getStaticDatabase().find(Parcel.class).where().like("owner", ownerArg).ne("ownerType", Parcel.ownerTypes.SYSTEM.toString()).like("buyStatus", buyStatusArg).like("ownerType", ownerTypeArg).orderBy("buyStatus asc,id desc").findList().iterator();
 		ArrayList<String> bigStr = new ArrayList<String>();
 		String oldWorld = "";
 		String oldBuyStateColorString = "";
 		if (parcelIterator.hasNext()) {
 			while (parcelIterator.hasNext()) {
 				Parcel parcel = parcelIterator.next();
 				String buyStateColorString = "";
 				String ownerTypeStr = "";
 				String ownerStr = "";
 				String priceStr = "";
 				switch (parcel.getOwnerType()) {
 				case STATE:
 					ownerTypeStr = "{ChatColor.BLUE}";
 					ownerStr = "State";
 					break;
 				case FACTION:
 					ownerTypeStr = "{ChatColor.AQUA}";
 					ownerStr = "Faction";
 					break;
 				case SYSTEM:
 					ownerTypeStr = "{ChatColor.GOLD}System";
 					ownerStr = "System";
 					break;
 				case PLAYER:
 					ownerTypeStr = "{ChatColor.LIGHT_PURPLE}";
 					ownerStr = parcel.getOwner();
 					break;
 				}
 
 				switch (parcel.getBuyStatus()) {
 				case BUYABLE:
 					buyStateColorString = "{ChatColor.GREEN}";
 					priceStr = String.format("({ChatColor.GREEN}%.2f{ChatColor.WHITE})", parcel.getPrice());
 					break;
 				case UNBUYABLE:
 					buyStateColorString = "{ChatColor.RED}";
 					break;
 				}
 
 				if (!oldWorld.equalsIgnoreCase(parcel.getWorld())) {
 					bigStr.add(ChatFormater.format("[{ChatColor.GOLD}" + parcel.getWorld() + "{ChatColor.WHITE}]"));
 					oldWorld = parcel.getWorld();
 				}
 
 				if (!oldBuyStateColorString.equalsIgnoreCase(buyStateColorString)) {
 					bigStr.add(ChatFormater.format("%s%s", buyStateColorString, StringUtils.fixedLength(parcel.getBuyStatus().toString(), 12)));
 					oldBuyStateColorString = buyStateColorString;
 				}
 				bigStr.add(ChatFormater.format("- %s%s{ChatColor.WHITE}| {ChatColor.WHITE}%s %s", ownerTypeStr, StringUtils.fixedLength(ownerStr, 12), StringUtils.fixedLength(parcel.getRegionId(), 15), priceStr));
 			}
 			bigStr.add("-----------");
 		} else {
 			bigStr.add("No parcels");
 		}
 		plugin.sendComments(player, bigStr.toArray(new String[bigStr.size()]), false);
 
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
 
 		ApplicableRegionSet set = PluginEnvironment.getWorldGuardPlugin(plugin).getRegionManager(w).getApplicableRegions(pt);
 
 		plugin.logger.log("list %s %s", player.getName(), set.toString());
 		for (ProtectedRegion reg : set) {
 			plugin.logger.log("%s %s", player.getName(), reg.getId());
 		}
 		plugin.logger.log("--list %s", player.getName());
 	}
 
 	/**
 	 * Creates the parcel.
 	 * 
 	 * @param player
 	 *            the player
 	 * @param parcelName
 	 *            the parcel name
 	 * @throws Exception
 	 *             the exception
 	 *             
 	 *  code is from wordlguard plugin, need to be interfaced/reimplemented
 	 */
 	public void createParcel(Player player, String parcelName) throws QDCommandException {
 		
 		
 		if (!ProtectedRegion.isValidId(parcelName)) {
 			throw new QDCommandException("Invalid region ID specified!");
 		}
 
 		if (parcelName.equalsIgnoreCase("__global__")) {
 			throw new QDCommandException("A region cannot be named __global__");
 		}
 
 		// Attempt to get the player's selection from WorldEdit
 		Selection sel = PluginEnvironment.getWorldEditPlugin(plugin).getSelection(player);
 
 		if (sel == null) {
 			throw new QDCommandException("Select a region with WorldEdit first.");
 		}
 
 		World w = sel.getWorld();
 		RegionManager mgr = PluginEnvironment.getWorldGuardPlugin(plugin).getGlobalRegionManager().get(w);
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
 			plugin.logger.log("region %s %s %d", min.toString(), max.toString(), w.getMaxHeight());
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
 			plugin.logger.dumpStackTrace(e);
 		}
 		// Get the list of region owners
 		DefaultDomain own = new DefaultDomain();
 		own.addPlayer(player.getName());
 		region.setOwners(own);
 		
		setRegionFlag(player, region, "CONSTRUCT", "MEMBERS");
 
 		mgr.addRegion(region);
 		Parcel parcel = Parcel.getParcel(w.getName(), parcelName);
 		if (parcel == null) {
 			parcel = new Parcel(w.getName(), region);
 		} else {
 			parcel.setPriceAndSurface(w.getName(), region);
 		}
 		parcel.save();
 		new ParcelHistory(parcel,ParcelHistory.historyTypes.CREATION,"",true);
 
 		try {
 			mgr.save();
 			plugin.sendComments(player, ChatColor.YELLOW + "Region saved as " + parcelName + ".", false);
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
 		region.setFlag(flag, flag.parseInput(PluginEnvironment.getWorldGuardPlugin(plugin), sender, value));
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
 				plugin.logger.dumpStackTrace(e);
 			}
 		}
 
 	}
 
 	/**
 	 * Teleport the player to parcel.
 	 * 
 	 * @param player
 	 *            the player
 	 * @param parcelName
 	 *            the parcel name
 	 */
 	public void teleportToParcel(Player player, String parcelName) {
 		World w = player.getWorld();
 		RegionManager rm = PluginEnvironment.getWorldGuardPlugin(plugin).getRegionManager(w);
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
 	 * @param player
 	 *            the player
 	 * @param WorldId
 	 *            the world id
 	 * @param parcelName
 	 *            the parcel name
 	 * @param newOwner
 	 *            the new owner
 	 * @throws QDCommandException
 	 *             the qD command exception
 	 */
 	public Parcel allocateParcel(Player player, String WorldId, String parcelName, String newOwner,boolean standAlone) throws QDCommandException {
 		Parcel parcel = Parcel.getParcel(WorldId, parcelName);
 		if (parcel == null) {
 			throw new QDCommandException("Parcel not found");
 		}
 
 		RegionManager mgr = getRegionManager(parcel.getWorld());
 		ProtectedRegion region = mgr.getRegion(parcel.getRegionId());
 
 		DefaultDomain own = new DefaultDomain();
 		own.addPlayer(newOwner);
 		region.setOwners(own);
 
 		parcel.setOwner(newOwner);
 		parcel.setBuyStatus(Parcel.buyStatusTypes.UNBUYABLE);
 		parcel.save();
 		
 		if(standAlone){
 			new ParcelHistory(parcel,ParcelHistory.historyTypes.ALLOCATION,"",true);
 		}
 		plugin.sendComments(player, ChatFormater.format("Allocation of %s to %s done", parcelName, newOwner));
 		return parcel;
 	}
 
 	private RegionManager getRegionManager(String world){
 		return PluginEnvironment.getWorldGuardPlugin(plugin).getGlobalRegionManager().get(plugin.getServer().getWorld(world));
 	}
 	/**
 	 * Sets the buyable.
 	 * 
 	 * @param player
 	 *            the player
 	 * @param parcelName
 	 *            the parcel name
 	 * @param priceString
 	 *            the price string
 	 * @throws Exception
 	 *             the exception
 	 */
 	public void setBuyable(Player player, String parcelName, String priceString) throws QDCommandException {
 		Parcel parcel = Parcel.getParcel(player.getWorld().getName(), parcelName, player);
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
 		new ParcelHistory(parcel,ParcelHistory.historyTypes.SET_BUYABLE,"",true);
 		
 		plugin.sendComments(player, ChatFormater.format("Parcel %s is now buyable at the following price %f", parcelName, price));
 	}
 
 	/**
 	 * Sets the unbuyable.
 	 * 
 	 * @param player
 	 *            the player
 	 * @param parcelName
 	 *            the parcel name
 	 * @throws Exception
 	 *             the exception
 	 */
 	public void setUnbuyable(Player player, String parcelName) throws QDCommandException {
 		Parcel parcel = Parcel.getParcel(player.getWorld().getName(), parcelName, player);
 		if (parcel == null) {
 			throw new QDCommandException("Parcel not found or doesn't belong to you");
 		}
 		parcel.setBuyStatus(Parcel.buyStatusTypes.UNBUYABLE);
 		parcel.save();
 		new ParcelHistory(parcel,ParcelHistory.historyTypes.SET_UNBUYABLE,"",true);
 		
 		plugin.sendComments(player, ChatFormater.format("Parcel %s is now unbuyable ", parcelName));
 	}
 
 	/**
 	 * Buy parcel.
 	 * 
 	 * @param player
 	 *            the player
 	 * @param parcelName
 	 *            the parcel name
 	 * @throws Exception
 	 *             the exception
 	 */
 	public void buyParcel(Player player, String parcelName) throws QDCommandException {
 		Parcel parcel = Parcel.getParcel(player.getWorld().getName(), parcelName);
 		if (parcel == null) {
 			throw new QDCommandException("Parcel not found");
 		}
 
 		if (parcel.getBuyStatus().equals(Parcel.buyStatusTypes.UNBUYABLE)) {
 			throw new QDCommandException("Parcel is not buyable");
 		}
 
 		if (parcel.getPrice() > PluginEnvironment.getVaultEconomy(plugin).getBalance(player.getName())) {
 			throw new QDCommandException(ChatFormater.format("Not enough money to buy that parcel %f<%f", PluginEnvironment.getVaultEconomy(plugin).getBalance(player.getName()), parcel.getPrice()));
 		}
 		PluginEnvironment.getVaultEconomy(plugin).depositPlayer(parcel.getOwner(), parcel.getPrice());
 		PluginEnvironment.getVaultEconomy(plugin).withdrawPlayer(player.getName(), parcel.getPrice());
 		parcel = allocateParcel(player, player.getWorld().getName(), parcelName, player.getDisplayName(),false);
 		new ParcelHistory(parcel,ParcelHistory.historyTypes.SALE,"",true);
 		plugin.sendComments(player, ChatFormater.format("Parcel %s bought ", parcelName));
 
 	}
 
 	/**
 	 * Manage parcel member.
 	 * 
 	 * @param player
 	 *            the player
 	 * @param args
 	 *            the args
 	 * @throws Exception
 	 *             the exception
 	 */
 	public void manageParcelMember(Player player, String[] args) throws QDCommandException {
 		String parcelName = args[1];
 		Parcel parcel = Parcel.getParcel(player.getWorld().getName(), parcelName);
 		if (parcel == null) {
 			throw new QDCommandException("Parcel not found");
 		}
 
 		if (!(parcel.getOwner().equalsIgnoreCase(player.getName()) || PluginEnvironment.getVaultPermission(plugin).playerHas(player, "localPlan.members.allow"))) {
 			throw new QDCommandException("You don't have right on that Parcel");
 		}
 		RegionManager regionManager = getRegionManager(parcel.getWorld());
 		ProtectedRegion parcelRegion = regionManager.getRegion(parcel.getId());
 		
 		if(parcelRegion == null){
 			throw new QDCommandException("Parcel exists but there is no region associated with it :((");
 		}
 		if (args.length!=4){
 			throw new QDCommandException("Not enough or too many arguments");
 		}
 		if(args[2].equalsIgnoreCase("add")){
 			if(parcelRegion.isMember(args[3])){
 				throw new QDCommandException("Player already member of that parcel :"+parcelRegion.getMembers().toPlayersString() );
 			}else{
 				parcelRegion.getMembers().addPlayer(args[3]);
 				plugin.sendComments(player, "Addition done");
 			}
 		}else if(args[2].equalsIgnoreCase("remove")){
 			if(!parcelRegion.isMember(args[3])){
 				throw new QDCommandException("Player is not a member of that parcel :"+parcelRegion.getMembers().toPlayersString() );
 			}else{
 				parcelRegion.getMembers().removePlayer(args[3]);
 				plugin.sendComments(player, "Removal done");
 			}
 		}else{
 			throw new QDCommandException("SubCommand is not valid");
 		}
 			
 	}
 }
