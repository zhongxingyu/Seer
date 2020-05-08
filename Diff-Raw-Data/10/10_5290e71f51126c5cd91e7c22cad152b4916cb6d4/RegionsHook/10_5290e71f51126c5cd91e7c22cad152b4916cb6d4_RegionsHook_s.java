 package me.asofold.bpl.contextmanager.hooks.regions;
 
 import me.asofold.bpl.contextmanager.hooks.AbstractServiceHook;
 import me.asofold.bpl.contextmanager.plshared.Utils;
 import me.asofold.bpl.contextmanager.plshared.permissions.pex.PexUtil;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.block.BlockFace;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import com.sk89q.worldedit.BlockVector;
 import com.sk89q.worldguard.protection.regions.ProtectedRegion;
 
 public class RegionsHook extends AbstractServiceHook {
 	
 	final static BlockFace[] horizontalFaces = new BlockFace[]{
 		BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST
 	};
 	
 	private static BlockFace xPosFace;
 	private static BlockFace xNegFace;
 	private static BlockFace zPosFace;
 	private static BlockFace zNegFace;
 	
 	static{
 		for (final BlockFace face : horizontalFaces){
			if (face.getModX() > 0) xPosFace = face;
			else if (face.getModX() < 0) xNegFace = face;
			if (face.getModZ() > 0) zPosFace = face;
			else if (face.getModZ() < 0) zNegFace = face;
 		}
 	}
 	
 	public RegionsHook(){
 		if (Bukkit.getPluginManager().getPlugin("WorldGuard") == null) throw new RuntimeException("WorldGuard not present.");
 	}
 
 	@Override
 	public String getHookName() {
 		return "Regions";
 	}
 
 	@Override
 	public String[] getCommandLabels() {
 		return new String[]{"region"};
 	}
 
 	@Override
 	public String[] getCommandLabelAliases(String label) {
 		if (label.equals("region")) return new String[]{"regions", "plot", "plots"};
 		else return null;
 	}
 
 	@Override
 	public void onCommand(CommandSender sender, String label, String[] args) {
 		if (args.length == 2){
 			if (!trySendDistance(sender, args[1], true)) sendUsage(sender);
 		}
 		else sendUsage(sender);
 	}
 	
 	/**
 	 * Send distance to region, if player is owner or member or has permission.
 	 * @param sender
 	 * @param rid
 	 * @param message if to messsage on failure.
 	 * @return
 	 */
 	public boolean trySendDistance(CommandSender sender, String rid, boolean message) {
 		if (!(sender instanceof Player)) return false;
 		Player player = (Player) sender;
 		World world = player.getWorld();
 		ProtectedRegion region = Utils.getWorldGuard().getRegionManager(world).getRegion(rid);
 		if (region != null){
 			String playerName = player.getName();
 			if (!region.isMember(playerName) && !region.isOwner(playerName)){
 				if (!PexUtil.hasPermission(playerName, "regions.find.w."+(world.getName().toLowerCase())+".r."+(region.getId().toLowerCase())) ) region = null;
 			}
 		}
 		if (region == null){
 			if (message) player.sendMessage(ChatColor.RED+"[Regions] Not available: "+rid);
 			return false;
 		}
 		sendDistance(player, region);
 		return true;
 	}
 
 	boolean sendUsage(CommandSender sender){
 		sender.sendMessage(ChatColor.RED+"Only players: /cx region find <region> | /cx find <region>");
 		return false;
 	}
 
 	public static void sendDistance(Player player, ProtectedRegion region) {
 		Location loc = player.getLocation();
 		BlockVector min = region.getMinimumPoint();
 		BlockVector max = region.getMaximumPoint();
 		int cx = (max.getBlockX() + min.getBlockX()) / 2;
 		int cy = (max.getBlockY() + min.getBlockY()) / 2;
 		int cz = (max.getBlockZ() + min.getBlockZ()) / 2;
 		
 		int rx = (max.getBlockX() - min.getBlockX()) / 2;
 		int ry = (max.getBlockY() - min.getBlockY()) / 2;
 		int rz = (max.getBlockZ() - min.getBlockZ()) / 2;
 		
 		int dx = cx - loc.getBlockX();
 		int dy = cy - loc.getBlockY();
 		int dz = cz - loc.getBlockZ();
 		StringBuilder b = new StringBuilder();
 		b.append(ChatColor.DARK_GRAY+"[Regions] "+ChatColor.GREEN+region.getId()+ChatColor.GRAY+": "+ChatColor.AQUA);
 		if (dx > rx) b.append((dx-rx) + " " + capitalize(xNegFace) + " ");
 		else if (dx < -rx) b.append((-dx-rx) +"  " + capitalize(xPosFace)  + " ");
 		if (dz > rz) b.append((dz-rz) + " " + capitalize(zNegFace) + " ");
 		else if (dz < -rz) b.append((-dz-rz) + " " + capitalize(zPosFace) + " ");
 		if (dy > ry) b.append((dy-ry) + " Up");
 		else if (dy < -ry) b.append((-dy-ry) + " Down");
 		player.sendMessage(b.toString());
 	}
 	
 	public static String capitalize(Object o){
 		String s = o.toString();
 		return "" + Character.toUpperCase(s.charAt(0)) + s.substring(1).toLowerCase();
 	}
 	
 	@Override
 	public boolean delegateFind(CommandSender sender, String[] args) {
 		if (args.length == 2) return trySendDistance(sender, args[1], false);
 		else return false;
 	}
 	
 	
 
 }
