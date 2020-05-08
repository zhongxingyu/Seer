 package net.slipcor.pvparena.modules.tempperms;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import org.bukkit.Bukkit;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.permissions.PermissionAttachment;
 
 import net.slipcor.pvparena.PVPArena;
 import net.slipcor.pvparena.arena.Arena;
 import net.slipcor.pvparena.arena.ArenaPlayer;
 import net.slipcor.pvparena.arena.ArenaTeam;
 import net.slipcor.pvparena.commands.AbstractArenaCommand;
 import net.slipcor.pvparena.core.Language;
 import net.slipcor.pvparena.core.Language.MSG;
 import net.slipcor.pvparena.core.StringParser;
 import net.slipcor.pvparena.loadables.ArenaModule;
 
 public class TempPerms extends ArenaModule {
 	public TempPerms() {
 		super("TempPerms");
 	}
 	
 	@Override
 	public String version() {
		return "v1.0.9.279";
 	}
 	
 	@Override
 	public boolean checkCommand(String s) {
 		return s.equals("!tps") || s.equals("tempperms");
 	}
 	
 	@Override
 	public void commitCommand(CommandSender sender, String[] args) {
 		// !tps | list perms
 		// !tps add [perm] | add perm
 		// !tps rem [perm] | remove perm
 		
 		// !tps [classname] | list class perms
 		// !tps [classname] add [perm] | add class perm
 		// !tps [classname] rem [perm] | remove class perm
 		
 		// !tps [teamname] | list team perms
 		// !tps [teamname] add [perm] | add team perm
 		// !tps [teamname] rem [perm] | remove team perm
 		
 		if (!PVPArena.hasAdminPerms(sender)
 				&& !(PVPArena.hasCreatePerms(sender, arena))) {
 			arena.msg(
 					sender,
 					Language.parse(MSG.ERROR_NOPERM,
 							Language.parse(MSG.ERROR_NOPERM_X_ADMIN)));
 			return;
 		}
 
 		if (!AbstractArenaCommand.argCountValid(sender, arena, args, new Integer[] { 1,2,3,4 })) {
 			return;
 		}
 		
 		HashMap<String, Boolean> map = getTempPerms(arena, "default");
 		
 		if (args.length == 1) {
 			arena.msg(sender, Language.parse(MSG.MODULE_TEMPPERMS_HEAD, "default"));
 			for (String s : map.keySet()) {
 				arena.msg(sender, s + " - " + StringParser.colorVar(map.get(s)));
 			}
 			return;
 		}
 		
 		if (args.length == 3 && (args[1].equals("add") || args[1].equals("rem"))) {
 			if (args[1].equals("add")) {
 				map.put(args[2], true);
 				arena.msg(sender, Language.parse(MSG.MODULE_TEMPPERMS_ADDED, args[2], "default"));
 			} else {
 				map.remove(args[2]);
 				arena.msg(sender, Language.parse(MSG.MODULE_TEMPPERMS_REMOVED, args[2], "default"));
 			}
 			setTempPerms(arena, map, "default");
 			return;
 		}
 		
 		if (args.length == 3) {
 			arena.msg(sender, Language.parse(MSG.ERROR_ARGUMENT, args[1], "add | remove"));
 			return;
 		}
 		
 		map = getTempPerms(arena, args[1]);
 		if (args.length == 2) {
 			arena.msg(sender, Language.parse(MSG.MODULE_TEMPPERMS_HEAD, args[1]));
 			for (String s : map.keySet()) {
 				arena.msg(sender, s + " - " + StringParser.colorVar(map.get(s)));
 			}
 			return;
 		}
 		
 		if (args.length == 4 && (args[2].equals("add") || args[2].equals("rem"))) {
 			if (args[2].equals("add")) {
 				map.put(args[3], true);
 				arena.msg(sender, Language.parse(MSG.MODULE_TEMPPERMS_ADDED, args[3], args[1]));
 			} else {
 				map.remove(args[3]);
 				arena.msg(sender, Language.parse(MSG.MODULE_TEMPPERMS_REMOVED, args[3], args[1]));
 			}
 			setTempPerms(arena, map, args[1]);
 			return;
 		}
 		
 		arena.msg(sender, Language.parse(MSG.ERROR_ARGUMENT, args[2], "add | remove"));
 		return;
 	}
 	
 	/**
 	 * get the permissions map
 	 * 
 	 * @return the temporary permissions map
 	 */
 	private HashMap<String, Boolean> getTempPerms(Arena arena, String node) {
 		HashMap<String, Boolean> result = new HashMap<String, Boolean>();
 
 		if (arena.getArenaConfig().getYamlConfiguration().contains("perms."+node)) {
 			List<String> list = arena.getArenaConfig().getStringList("perms."+node,
 					new ArrayList<String>());
 			for (String key : list) {
 				result.put(key.replace("-", "").replace("^", ""),
 						!(key.startsWith("^") || key.startsWith("-")));
 			}
 		}
 
 		return result;
 	}
 	
 	private void setTempPerms(Arena arena, HashMap<String, Boolean> map, String node) {
 		List<String> result = new ArrayList<String>();
 		for (String s : map.keySet()) {
 			result.add((map.get(s)?"":"^")+s);
 		}
 		arena.getArenaConfig().setManually("perms." + node, result);
 		arena.getArenaConfig().save();
 	}
 	
 	@Override
 	public void parseJoin(CommandSender player, ArenaTeam team) {
 		ArenaPlayer ap = ArenaPlayer.parsePlayer(player.getName());
 		setPermissions(arena, ap, getTempPerms(arena, "default"), getTempPerms(arena, ap.getArenaTeam().getName()));
 	}
 	
 	public boolean onPlayerInteract(PlayerInteractEvent event) {
 		return false;
 	}
 	
 	/**
 	 * set temporary permissions for a player
 	 * 
 	 * @param p
 	 *            the player to set
 	 */
 	private void setPermissions(Arena arena, ArenaPlayer ap, HashMap<String, Boolean> mGlobal, HashMap<String, Boolean> mTeam) {
 		
		HashMap<String, Boolean> mClass = getTempPerms(arena, ap.getArenaClass().getName());
 		
 		HashMap<String, Boolean> total = new HashMap<String, Boolean>();
 		for (String s : mGlobal.keySet()) {
 			total.put(s, mGlobal.get(s));
 		}
 		for (String s : mTeam.keySet()) {
 			total.put(s, mTeam.get(s));
 		}
 		for (String s : mClass.keySet()) {
 			total.put(s, mClass.get(s));
 		}
 		
 		if (total.isEmpty())
 			return;
 
 		PermissionAttachment pa = ap.get().addAttachment(PVPArena.instance);
 
 		for (String entry : total.keySet()) {
 			pa.setPermission(entry, total.get(entry));
 		}
 		ap.get().recalculatePermissions();
 		ap.getTempPermissions().add(pa);
 	}
 
 	/**
 	 * remove temporary permissions from a player
 	 * 
 	 * @param p
 	 *            the player to reset
 	 */
 	void removePermissions(Player p) {
 		ArenaPlayer player = ArenaPlayer.parsePlayer(p.getName());
 		if (player == null || player.getTempPermissions() == null) {
 			return;
 		}
 		for (PermissionAttachment pa : player.getTempPermissions()) {
 			if (pa != null) {
 				pa.remove();
 			}
 		}
 		p.recalculatePermissions();
 	}
 	
 	@Override
 	public void resetPlayer(Player player, boolean force) {
 		Bukkit.getScheduler().scheduleSyncDelayedTask(PVPArena.instance, new ResetRunnable(this, player), 5L);
 	}
 	/*
 	@Override
 	public void parseStart() {
 		HashMap<String, Boolean> mGlobal = getTempPerms(arena, "default");
 		
 		for (ArenaTeam team : arena.getTeams()) {
 			HashMap<String, Boolean> mTeam = getTempPerms(arena, team.getName());
 			for (ArenaPlayer ap : team.getTeamMembers()) {
 				setPermissions(arena, ap, mGlobal, mTeam);
 			}
 		}
 	}*/
 }
