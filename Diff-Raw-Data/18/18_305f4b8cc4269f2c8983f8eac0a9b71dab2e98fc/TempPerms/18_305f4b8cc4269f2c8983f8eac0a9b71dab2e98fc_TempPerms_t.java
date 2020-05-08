 package net.slipcor.pvparena.modules.tempperms;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import org.bukkit.entity.Player;
 import org.bukkit.event.block.SignChangeEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.permissions.PermissionAttachment;
 
 import net.slipcor.pvparena.PVPArena;
 import net.slipcor.pvparena.arena.Arena;
 import net.slipcor.pvparena.arena.ArenaPlayer;
 import net.slipcor.pvparena.arena.ArenaTeam;
 import net.slipcor.pvparena.loadables.ArenaModule;
 
 public class TempPerms extends ArenaModule {
 	public TempPerms() {
 		super("TempPerms");
 	}
 	
 	@Override
 	public String version() {
		return "v0.9.3.33";
 	}
 	
 	/**
 	 * get the permissions map
 	 * 
 	 * @return the temporary permissions map
 	 */
 	private HashMap<String, Boolean> getTempPerms(Arena arena) {
 		HashMap<String, Boolean> result = new HashMap<String, Boolean>();
 
 		if (arena.getArenaConfig().getYamlConfiguration().contains("perms.default")) {
 			List<String> list = arena.getArenaConfig().getStringList("perms.default",
 					new ArrayList<String>());
 			for (String key : list) {
 				result.put(key.replace("-", "").replace("^", ""),
 						!(key.startsWith("^") || key.startsWith("-")));
 			}
 		}
 
 		return result;
 	}
 	
 	@Override
 	public boolean isActive(Arena arena) {
 		return true;
 	}
 	
 	@Override
 	public void lateJoin(Arena arena, Player player) {
 		setPermissions(arena, player);
 	}
 	
 	public boolean onPlayerInteract(PlayerInteractEvent event) {
 		return false;
 	}
 	
 	public void onSignChange(SignChangeEvent event) {
 
 	}
 
 	/**
 	 * set temporary permissions for a player
 	 * 
 	 * @param p
 	 *            the player to set
 	 */
 	private void setPermissions(Arena arena, Player p) {
 		HashMap<String, Boolean> perms = getTempPerms(arena);
 		if (perms == null || perms.isEmpty())
 			return;
 
 		ArenaPlayer player = ArenaPlayer.parsePlayer(p.getName());
 		PermissionAttachment pa = p.addAttachment(PVPArena.instance);
 
 		for (String entry : perms.keySet()) {
 			pa.setPermission(entry, perms.get(entry));
 		}
 		p.recalculatePermissions();
 		player.getTempPermissions().add(pa);
 	}
 
 	/**
 	 * remove temporary permissions from a player
 	 * 
 	 * @param p
 	 *            the player to reset
 	 */
 	private void removePermissions(Player p) {
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
	public void resetPlayer(Arena arena, Player player, boolean force) {
 		removePermissions(player);
 	}
 	
 	@Override
 	public void teleportAllToSpawn(Arena arena) {
 		for (ArenaTeam team : arena.getTeams()) {
 			for (ArenaPlayer ap : team.getTeamMembers()) {
 				setPermissions(arena, ap.get());
 			}
 		}
 	}
 }
