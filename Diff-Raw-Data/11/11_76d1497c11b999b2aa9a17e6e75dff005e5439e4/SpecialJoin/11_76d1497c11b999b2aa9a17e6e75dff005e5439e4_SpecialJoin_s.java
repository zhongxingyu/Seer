 package net.slipcor.pvparena.modules.specialjoin;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.block.BlockState;
 import org.bukkit.block.Sign;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerInteractEvent;
 
 import net.slipcor.pvparena.PVPArena;
 import net.slipcor.pvparena.arena.Arena;
 import net.slipcor.pvparena.arena.ArenaTeam;
 import net.slipcor.pvparena.classes.PABlockLocation;
 import net.slipcor.pvparena.commands.PAA_Edit;
 import net.slipcor.pvparena.commands.PAA_Setup;
 import net.slipcor.pvparena.commands.PAG_Join;
 import net.slipcor.pvparena.commands.PAG_Spectate;
 import net.slipcor.pvparena.core.Config;
 import net.slipcor.pvparena.core.Config.CFG;
 import net.slipcor.pvparena.core.Language;
 import net.slipcor.pvparena.core.Language.MSG;
 import net.slipcor.pvparena.loadables.ArenaModule;
 
 public class SpecialJoin extends ArenaModule implements Listener {
 	public SpecialJoin() {
 		super("SpecialJoin");
 	}
 	static HashMap<PABlockLocation, Arena> places = new HashMap<PABlockLocation, Arena>();
 	static HashMap<String, Arena> selections = new HashMap<String, Arena>();
 	boolean setup = false;
 	
 	@Override
 	public String version() {
		return "v1.1.0.335";
 	}
 	
 	@Override
 	public boolean checkCommand(String s) {
 		return s.toLowerCase().equals("setjoin");
 	}
 	
 	@Override
 	public void configParse(YamlConfiguration config) {
 		List<String> res;
 		try {
 			res = config.getStringList("modules.specialjoin.places");
 			for (String s : res) {
 				places.put(Config.parseBlockLocation(s), arena);
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	@Override
 	public void commitCommand(CommandSender sender, String[] args) {
 		if (!PVPArena.hasAdminPerms(sender)
 				&& !(PVPArena.hasCreatePerms(sender, arena))) {
 			arena.msg(sender,
 					Language.parse(MSG.ERROR_NOPERM, Language.parse(MSG.ERROR_NOPERM_X_ADMIN)));
 			return;
 		}
 		
 		// /pa [arenaname] setjoin
 		
 		if (selections.containsKey(sender.getName())) {
 			// remove & announce
 			selections.remove(sender.getName());
 			arena.msg(sender,
 					Language.parse(MSG.MODULE_SPECIALJOIN_STOP));
 		} else {
 			// add & announce
 			selections.put(sender.getName(), arena);
 			arena.msg(sender,
 					Language.parse(MSG.MODULE_SPECIALJOIN_START));
 		}
 	}
 	
 	@Override
 	public void onThisLoad() {
 		if (!setup) {
 			Bukkit.getPluginManager().registerEvents(this, PVPArena.instance);
 			setup = true;
 		}
 	}
 	
 	@EventHandler
 	public void onSpecialJoin(PlayerInteractEvent event) {
 		if (event.isCancelled()) {
 			debug.i("PIA cancelled!", event.getPlayer());
 			return;
 		}
 		debug.i("PIA!", event.getPlayer());
 
 		
 		if (PAA_Edit.activeEdits.containsKey(event.getPlayer().getName()) || 
 				PAA_Setup.activeSetups.containsKey(event.getPlayer().getName())) {
 			debug.i("edit mode. OUT!", event.getPlayer());
 			return;
 		}
 		if (event.getAction().equals(Action.PHYSICAL)) {
 			
 			debug.i("Join via pressure plate!", event.getPlayer());
 			
 			if (event.getPlayer() == null) {
 				debug.i("wth?", event.getPlayer());
 				return;
 			}
 			PABlockLocation loc = new PABlockLocation(event.getPlayer().getLocation());
 			
 			PABlockLocation find = null;
 			
 			for (PABlockLocation l : places.keySet()) {
 				if (l.getWorldName().equals(loc.getWorldName())
 						&& l.getDistance(loc) < 0.1f) {
 					find = l;
 				}
 			}
 			
 			if (find == null) {
 				debug.i("not contained!", event.getPlayer());
 				return;
 			}
 			PAG_Join j = new PAG_Join();
 			j.commit(places.get(find), event.getPlayer(), new String[0]);
 			return;
 		}
 		
 		if (!event.hasBlock()) {
 			debug.i("not has block, out!", event.getPlayer());
 			return;
 		}
 		
 		if (selections.containsKey(event.getPlayer().getName())) {
 			debug.i("selection contains!", event.getPlayer());
 			
 			Material mat = event.getClickedBlock().getType();
 			String place = null;
 			
 			if (mat == Material.STONE_PLATE || mat == Material.WOOD_PLATE) {
 				place = mat.name();
 			} else if (mat == Material.STONE_BUTTON || mat == Material.WOOD_BUTTON || mat == Material.LEVER) {
 				place = mat.name();
 			} else if (mat == Material.SIGN || mat == Material.SIGN_POST || mat == Material.WALL_SIGN) {
 				place = mat.name();
 			} else {
 				return;
 			}
 			Arena a = selections.get(event.getPlayer().getName());
 			places.put(new PABlockLocation(event.getClickedBlock().getLocation()), a);
 			selections.remove(event.getPlayer().getName());
 			a.msg(event.getPlayer(),
 					Language.parse(MSG.MODULE_SPECIALJOIN_DONE, place));
 			event.setCancelled(true);
 			update(a);
 			return;
 		}
 		
 		PABlockLocation loc = new PABlockLocation(event.getClickedBlock().getLocation());
 		
 		PABlockLocation find = null;
 		
 		for (PABlockLocation l : places.keySet()) {
 			if (l.getWorldName().equals(loc.getWorldName())
 					&& l.getDistance(loc) < 0.1f) {
 				find = l;
 			}
 		}
 		
 		
 		if (find == null) {
 			debug.i("places does not contain!", event.getPlayer());
 			return;
 		}
 		
 		event.setCancelled(true);
 
 		PAG_Join j = new PAG_Join();
 		
 		Material mat = event.getClickedBlock().getType();
 		
 		if (mat == Material.STONE_BUTTON || mat == Material.WOOD_BUTTON || mat == Material.LEVER) {
 			j.commit(places.get(find), event.getPlayer(), new String[0]);
 		} else if (mat == Material.SIGN || mat == Material.SIGN_POST || mat == Material.WALL_SIGN) {
 			Sign s = (Sign) event.getClickedBlock().getState();
 			String[] arr = new String[1];
 			arr[0] = s.getLine(1); // second line
 			
 			
 			if (s.getLine(2) != null && s.getLine(2).length() > 0) {
 				PAG_Spectate jj = new PAG_Spectate();
 				jj.commit(places.get(find), event.getPlayer(), new String[0]);
 			} else {
 				j.commit(places.get(find), event.getPlayer(), arr);
 			}
 			
 		}
 	}
 	
 	@Override
 	public void parseJoin(CommandSender sender, ArenaTeam team) {
 		updateSignDisplay();
 	}
 	
 	@Override
 	public void parsePlayerLeave(Player player, ArenaTeam team) {
 		updateSignDisplay();
 	}
 	
 	@Override
 	public void parseStart() {
 		updateSignDisplay();
 	}
 	
 	@Override
 	public void reset(boolean force) {
 		updateSignDisplay();
 	}
 
 	private static void update(Arena a) {
 		ArrayList<String> locs = new ArrayList<String>();
 		for (PABlockLocation l : places.keySet()) {
 			if (a.getName().equals(places.get(l).getName())) {
 				locs.add(Config.parseToString(l));
 			}
 		}
 		a.getArenaConfig().setManually("modules.specialjoin.places", locs);
 		a.getArenaConfig().save();
 	}
 	
 	private static void updateSignDisplay() {
 		class RunLater implements Runnable {
 
 			@Override
 			public void run() {
 
 				for (PABlockLocation loc : places.keySet()) {
 					Arena arena = places.get(loc);
 					if (!arena.getArenaConfig().getBoolean(CFG.MODULES_SPECIALJOIN_SHOWPLAYERS)) {
 						continue;
 					}
 					
 					BlockState state = loc.toLocation().getBlock().getState();
 					
 					if (!(state instanceof Sign)) {
 						continue;
 					}
 					
 					Sign sign = (Sign) state;
 					
 					String line = (arena.isFightInProgress()?ChatColor.GREEN.toString():(arena.isLocked()?ChatColor.RED.toString():ChatColor.GOLD.toString())) + arena.getFighters().size();
 					
 					final int maxPlayers = arena.getArenaConfig().getInt(CFG.READY_MAXPLAYERS);
 					
 					if (maxPlayers > 0) {
 						line += " / " + maxPlayers;
 					}
 					
 					sign.setLine(3, line);
 					sign.update();
 				}
 			}
 			
 		}
		Bukkit.getScheduler().runTaskLater(PVPArena.instance, new RunLater(), 3L);
 	}
 }
