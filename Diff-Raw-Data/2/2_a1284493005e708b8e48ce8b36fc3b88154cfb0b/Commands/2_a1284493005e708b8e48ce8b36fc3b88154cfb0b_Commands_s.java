 package eu.icecraft.iceprotect;
 
 import java.io.IOException;
 import java.util.Set;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.World;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import com.sk89q.minecraft.util.commands.CommandException;
 import com.sk89q.worldedit.BlockVector;
 import com.sk89q.worldedit.bukkit.WorldEditPlugin;
 import com.sk89q.worldedit.bukkit.selections.CuboidSelection;
 import com.sk89q.worldedit.bukkit.selections.Polygonal2DSelection;
 import com.sk89q.worldedit.bukkit.selections.Selection;
 import com.sk89q.worldguard.LocalPlayer;
 import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
 import com.sk89q.worldguard.protection.ApplicableRegionSet;
 import com.sk89q.worldguard.protection.flags.DefaultFlag;
 import com.sk89q.worldguard.protection.flags.Flag;
 import com.sk89q.worldguard.protection.flags.InvalidFlagFormat;
 import com.sk89q.worldguard.protection.managers.RegionManager;
 import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
 import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
 import com.sk89q.worldguard.protection.regions.ProtectedRegion;
 import com.sk89q.worldguard.util.RegionUtil;
 
 public class Commands {
 
 	private WorldGuardPlugin plugin;
 	private WorldEditPlugin worldEdit;
 	private Economy econ;
 
 	public Commands(WorldGuardPlugin wg, WorldEditPlugin we, Economy econ) {
 		this.plugin = wg;
 		this.worldEdit = we;
 		this.econ = econ;
 	}
 
 	public void setRegion(Player sender, String[] args) {
 
 		if(args.length != 2) {
 			sender.sendMessage(ChatColor.RED + "Wrong usage. /pr help");
 			return;
 		}
 
 		LocalPlayer wgPlayer = plugin.wrapPlayer(sender);
 
 		String id = "icp_" + sender.getName() + "_" + args[1];
 
 		if (!ProtectedRegion.isValidId(id)) {
 			sender.sendMessage(ChatColor.RED + "Invalid region name specified!");
 			return;
 		}
 
 		Selection sel = worldEdit.getSelection(sender);
 
 		if (sel == null) {
 			sender.sendMessage(ChatColor.RED + "Select a region with a wooden axe first.");
 			return;
 		}
 
 		RegionManager mgr = plugin.getGlobalRegionManager().get(sel.getWorld());
 
 		if (mgr.hasRegion(id)) {
 			sender.sendMessage(ChatColor.RED + "That region is already defined. Delete the old one or chose a new name");
 			return;
 		}
 
 		ProtectedRegion region = null;
 
 		if (sel instanceof Polygonal2DSelection) {
 			Polygonal2DSelection polySel = (Polygonal2DSelection) sel;
 			int minY = polySel.getNativeMinimumPoint().getBlockY();
 			int maxY = polySel.getNativeMaximumPoint().getBlockY();
 			region = new ProtectedPolygonalRegion(id, polySel.getNativePoints(), minY, maxY);
 		} else if (sel instanceof CuboidSelection) {
 			BlockVector min = sel.getNativeMinimumPoint().toBlockVector();
 			BlockVector max = sel.getNativeMaximumPoint().toBlockVector();
 			region = new ProtectedCuboidRegion(id, min, max);
 		} else {
 			sender.sendMessage(ChatColor.RED + "(shouldn't happen) Something went wrong. The type of region selected is unsupported!");
 			return;
 		}
 
 		String[] names = new String[1];
 		names[0] = sender.getName();
 		region.setOwners(RegionUtil.parseDomainString(names, 0));
 
 		ApplicableRegionSet regions = mgr.getApplicableRegions(region);
 
 		if (!regions.isOwnerOfAll(wgPlayer)) {
 			sender.sendMessage(ChatColor.RED + "That region overlaps with another one not owned by you!");
 			return;
 		}
 
 		double cost = (int) Math.ceil(econ.getCost(region.volume()));
 
 		if(!econ.chargePlayer(sender, cost)) {
 			sender.sendMessage(ChatColor.RED + "You don't have enough money! " + cost + "$ needed.");
 			return;
 		}
 
 		mgr.addRegion(region);
 
 		try {
 			mgr.save();
 			sender.sendMessage(ChatColor.YELLOW + "Region saved as " + args[1] + ". Cost: " + cost + "$");
 		} catch (IOException e) {
 			sender.sendMessage(ChatColor.RED + "(shouldn't happen) Failed to write regions file: " + e.getMessage());
 			e.printStackTrace();
 			return;
 		}
 	}
 
 	public void deleteRegion(Player player, String[] args) {
 
 		if(args.length != 2) {
 			player.sendMessage(ChatColor.RED + "Wrong usage. /pr help");
 			return;
 		}
 
 		String id = "icp_" + player.getName() + "_" + args[1];
 
 		World world = player.getWorld();
 		LocalPlayer localPlayer = plugin.wrapPlayer(player);
 
 		RegionManager mgr = plugin.getGlobalRegionManager().get(world);
 		ProtectedRegion region = mgr.getRegion(id);
 
 		if (region == null) {
 			player.sendMessage(ChatColor.RED + "Could not find a region by that ID.");
 		}
 
 		if (region.isOwner(localPlayer)) {
 			mgr.removeRegion(id);
 
 			player.sendMessage(ChatColor.YELLOW + "Region " + args[1] + " removed.");
 
 			try {
 				mgr.save();
 			} catch (IOException e) {
 				player.sendMessage(ChatColor.RED + "(shouldn't happen) Failed to write regions file: " + e.getMessage());
 			}
 
 		}
 	}
 
 	public void allowUser(Player player, String[] args) {
 		if(args.length != 3) {
 			player.sendMessage(ChatColor.RED + "Wrong usage. /pr help");
 			return;
 		}
 
 		String id = "icp_" + player.getName() + "_" + args[2];
 
 		World world = player.getWorld();
 		LocalPlayer localPlayer = plugin.wrapPlayer(player);
 
 		RegionManager mgr = plugin.getGlobalRegionManager().get(world);
 		ProtectedRegion region = mgr.getRegion(id);
 
 		if (region == null) {
 			player.sendMessage(ChatColor.RED + "Could not find a region by that name.");
 			return;
 		}
 
 		if (region.isOwner(localPlayer)) {
 			region.getMembers().addPlayer(args[1]);
 			player.sendMessage(ChatColor.GREEN + "Allowed " + ChatColor.RED + args[1] + ChatColor.GREEN + " to region "+ ChatColor.RED + args[2] + ChatColor.GREEN + ".");
 
 			Player targetPlayer = Bukkit.getServer().getPlayerExact(args[1]);
 			if(targetPlayer != null) {
 				targetPlayer.sendMessage(ChatColor.GREEN + player.getName() + " has added you as a member in the " + ChatColor.RED  + args[2] + ChatColor.GREEN + " region.");
 			}
 
 		} else {
 			player.sendMessage(ChatColor.RED + "(shouldn't happen) You don't own the region " + args[2]);
 		}
 	}
 
 	public void denyUser(Player player, String[] args) {
 		if(args.length != 3) {
 			player.sendMessage(ChatColor.RED + "Wrong usage. /pr help");
 			return;
 		}
 
 		String id = "icp_" + player.getName() + "_" + args[2];
 
 		World world = player.getWorld();
 		LocalPlayer localPlayer = plugin.wrapPlayer(player);
 
 		RegionManager mgr = plugin.getGlobalRegionManager().get(world);
 		ProtectedRegion region = mgr.getRegion(id);
 
 		if (region == null) {
 			player.sendMessage(ChatColor.RED + "Could not find a region by that name.");
 			return;
 		}
 
 		if (region.isOwner(localPlayer)) {
 			region.getMembers().removePlayer(args[1]);
 			player.sendMessage(ChatColor.GREEN + "Removed player "+ ChatColor.RED + args[1] + ChatColor.GREEN + " from region "+ ChatColor.RED + args[2] + ChatColor.GREEN + ".");
 		} else {
 			player.sendMessage(ChatColor.RED + "(shouldn't happen) You don't own the region " + args[2]);
 		}
 	}
 
 	public void listRegions(Player player, String[] args) {
 		World world = player.getWorld();
 
 		RegionManager mgr = plugin.getGlobalRegionManager().get(world);
 		StringBuilder regions = new StringBuilder();
 		Set<String> keySet = mgr.getRegions().keySet();
 
 		for(String regionName : keySet) {
			if(regionName.startsWith("icp_" + player.getName() + "_")) {
 				regions.append(regionName.replaceFirst("icp_" + player.getName().toLowerCase() + "_", "") + ", ");
 			}
 		}
 
 		if(regions.length() == 0) {
 			player.sendMessage(ChatColor.RED + "You don't have regions.");
 		} else {
 			player.sendMessage(ChatColor.AQUA + "Your regions:");
 			regions.deleteCharAt(regions.length()-2); 
 			player.sendMessage(ChatColor.AQUA + regions.toString());
 		}
 	}
 
 	public void setFlags(Player sender, String[] args) { 
 
 		Player player = null;
 		try {
 			player = plugin.checkPlayer(sender);
 		} catch (CommandException e1) {
 			e1.printStackTrace();
 			return;
 		}
 
 		World world = player.getWorld();
 		LocalPlayer localPlayer = plugin.wrapPlayer(player);
 
 		String id = "icp_" + player.getName() + "_" + args[1];
 		String flagName = args[2];
 		String value = null;
 
 		if(args.length == 4) {
 			value = args[3];
 		} else if(args.length == 3) {
 			//
 		} else {
 			player.sendMessage(ChatColor.RED + "Wrong usage! /pr help");
 			return;
 		}
 
 		if(!(flagName.equals("use") || flagName.equals("chest-access") || flagName.equals("snow-fall") || flagName.equals("snow-melt") || flagName.equals("ice-form") || flagName.equals("ice-melt"))) {
 			player.sendMessage(ChatColor.RED + "Unsupported flag! /pr help flags");
 			return;
 		}
 
 		RegionManager mgr = plugin.getGlobalRegionManager().get(world);
 		ProtectedRegion region = mgr.getRegion(id);
 
 		if (region == null) {
 			player.sendMessage(ChatColor.RED + "Could not find a region by that name.");
 			return;
 		}
 
 		Flag<?> foundFlag = null;
 
 		for (Flag<?> flag : DefaultFlag.getFlags()) {
 			if (flag.getName().replace("-", "").equalsIgnoreCase(flagName.replace("-", ""))) {
 				foundFlag = flag;
 				break;
 			}
 		}
 
 		if (foundFlag == null) {
 			StringBuilder list = new StringBuilder();
 
 			for (Flag<?> flag : DefaultFlag.getFlags()) {
 				if (list.length() > 0) {
 					list.append(", ");
 				}
 
 				if (!region.isOwner(localPlayer)) {
 					continue;
 				}
 
 				list.append(flag.getName());
 			}
 
 			player.sendMessage(ChatColor.RED + "Unknown flag specified: " + flagName);
 			player.sendMessage(ChatColor.RED + "Available flags: " + list);
 			return;
 		}
 
 		if (region.isOwner(localPlayer)) {
 
 			if (value != null) {
 				try {
 					setFlag(region, foundFlag, sender, value);
 				} catch (InvalidFlagFormat e) {
 					player.sendMessage(ChatColor.RED + e.getMessage());
 					return;
 				}
 
 				sender.sendMessage(ChatColor.YELLOW + "Region flag " + ChatColor.GREEN + foundFlag.getName() + ChatColor.YELLOW + " set.");
 			} else {
 				region.setFlag(foundFlag, null);
 
 				sender.sendMessage(ChatColor.YELLOW + "Region flag " + ChatColor.GREEN + foundFlag.getName() + ChatColor.YELLOW + " cleared.");
 			}
 
 		} else {
 			sender.sendMessage(ChatColor.RED + "(shouldn't happen) You don't own this region!");
 			return;
 		}
 
 		try {
 			mgr.save();
 		} catch (IOException e) {
 			player.sendMessage(ChatColor.RED + "Failed to write regions file: " + e.getMessage());
 		}	
 	}
 
 	public void regionPrice(Player player, String[] args) {
 
 		Selection sel = worldEdit.getSelection(player);
 
 		if (sel == null) {
 			player.sendMessage(ChatColor.RED + "Select a region with a wooden axe first.");
 			return;
 		}
 
 		ProtectedRegion region = null;
 		String id = "icp__tempregion";
 
 		if (sel instanceof Polygonal2DSelection) {
 			Polygonal2DSelection polySel = (Polygonal2DSelection) sel;
 			int minY = polySel.getNativeMinimumPoint().getBlockY();
 			int maxY = polySel.getNativeMaximumPoint().getBlockY();
 			region = new ProtectedPolygonalRegion(id, polySel.getNativePoints(), minY, maxY);
 		} else if (sel instanceof CuboidSelection) {
 			BlockVector min = sel.getNativeMinimumPoint().toBlockVector();
 			BlockVector max = sel.getNativeMaximumPoint().toBlockVector();
 			region = new ProtectedCuboidRegion(id, min, max);
 		} else {
 			player.sendMessage(ChatColor.RED + "(shouldn't happen) Something went wrong. The type of region selected is unsupported!");
 			return;
 		}
 
 		double cost = (int) Math.ceil(econ.getCost(region.volume()));
 
 		player.sendMessage(ChatColor.AQUA + "That region will cost you " + cost + "$.");
 
 	}
 
 	public <V> void setFlag(ProtectedRegion region, Flag<V> flag, CommandSender sender, String value) throws InvalidFlagFormat {
 		region.setFlag(flag, flag.parseInput(plugin, sender, value));
 	}
 
 	public void help(Player sender, String[] args) {
 		sender.sendMessage(ChatColor.DARK_AQUA + "======== IceProtect ========");
 
 		if(args.length == 0 || args.length == 1) {
 			sender.sendMessage(ChatColor.YELLOW + "For help with the commands, use /pr help commands");
 			sender.sendMessage(ChatColor.YELLOW + "For all the possible flags, use /pr help flags");
 		} else if(args[1].equals("commands") || args[1].equals("cmd")) {
 			sender.sendMessage(ChatColor.YELLOW + "Regions cost 0.2$ per block.");
 			sender.sendMessage(ChatColor.YELLOW + "Use a wooden axe to select a region.");
 			sender.sendMessage(ChatColor.YELLOW + "Members can build and interact in the area.");
 			sender.sendMessage(ChatColor.YELLOW + "To check how much a region will cost, use /pr price");
 			sender.sendMessage(ChatColor.YELLOW + "To make a protected region, use /pr def <name>");
 			sender.sendMessage(ChatColor.YELLOW + "To allow members, use /pr allow <player> <name>");
 			sender.sendMessage(ChatColor.YELLOW + "To deny member, use /pr deny <player> <name>");
 			sender.sendMessage(ChatColor.YELLOW + "To add flags, use /pr flags <name> <flag> <value>");
 			sender.sendMessage(ChatColor.YELLOW + "To delete a region, use /pr delete <name>");
 			sender.sendMessage(ChatColor.YELLOW + "To see all the regions made by you, use /pr list");
 		} else if(args[1].equals("flags")) {
 			sender.sendMessage(ChatColor.YELLOW + "chest-access: Allow or block chest access.");
 			sender.sendMessage(ChatColor.YELLOW + "ice-form: Allow or block ice from forming.");
 			sender.sendMessage(ChatColor.YELLOW + "ice-melt: Allow or block ice from melting.");
 			sender.sendMessage(ChatColor.YELLOW + "snow-fall: Allow or block snow from forming.");
 			sender.sendMessage(ChatColor.YELLOW + "snow-melt: Allow or block snow from melting.");
 			sender.sendMessage(ChatColor.YELLOW + "use: Allow or block the ability to use doors, buttons,");
 			sender.sendMessage(ChatColor.YELLOW + "pressure plates, levers, note blocks, chests, etc..");
 		} else {
 			sender.sendMessage(ChatColor.YELLOW + "For help with the commands, use /pr help commands.");
 			sender.sendMessage(ChatColor.YELLOW + "For all the possible flags, use /pr help flags.");
 		}
 
 		sender.sendMessage(ChatColor.DARK_AQUA + "==========================");
 	}
 }
