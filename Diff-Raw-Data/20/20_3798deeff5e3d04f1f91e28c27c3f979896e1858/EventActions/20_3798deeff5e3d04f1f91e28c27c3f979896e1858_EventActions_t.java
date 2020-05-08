 package net.slipcor.pvparena.modules.eventactions;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 import net.slipcor.pvparena.PVPArena;
 import net.slipcor.pvparena.arena.Arena;
 import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.arena.ArenaTeam;
 import net.slipcor.pvparena.classes.PABlockLocation;
 import net.slipcor.pvparena.commands.PAA_Edit;
 import net.slipcor.pvparena.core.Config.CFG;
 import net.slipcor.pvparena.core.Language;
 import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.core.StringParser;
 import net.slipcor.pvparena.managers.SpawnManager;
 import net.slipcor.pvparena.loadables.ArenaModule;
 import net.slipcor.pvparena.loadables.ArenaRegionShape;
 
 public class EventActions extends ArenaModule {
 	private boolean setup = false;
 	
 	public EventActions() {
 		super("EventActions");
 	}
 	
 	@Override
 	public String version() {
		return "v1.0.3.164";
 	}
 	
 	@Override
 	public boolean checkCommand(String s) {
 		return s.toLowerCase().equals("setpower");
 	}
 	
 	@Override
 	public void commitCommand(CommandSender sender, String[] args) {
 		Arena a = PAA_Edit.activeEdits.get(sender.getName()+"_power");
 		
 		if (a == null) {
 			PAA_Edit.activeEdits.put(sender.getName()+"_power", arena);
 			arena.msg(sender, Language.parse(MSG.SPAWN_SET_START, "power"));
 		} else {
 			PAA_Edit.activeEdits.remove(sender.getName()+"_power");
 			arena.msg(sender, Language.parse(MSG.SPAWN_SET_DONE, "power"));
 		}
 	}
 
 	
 	@Override
 	public void configParse(YamlConfiguration config) {
 		if (setup)
 			return;
 		Bukkit.getPluginManager().registerEvents(new PAListener(this), PVPArena.instance);
 		setup = true;
 	}
 
 	protected void catchEvent(final String string, final Player p, Arena a) {
 		
 		if (a == null || !a.equals(arena)) {
 			return;
 		}
 		
 		if (a.getArenaConfig().getUnsafe("event." + string) == null) {
 			return;
 		}
 		
 		List<String> items = a.getArenaConfig().getStringList("event." + string, new ArrayList<String>());
 		
 		for (String item : items) {
 			
 			if (p != null) {
 				item = item.replace("%player%", p.getName());
 				ArenaPlayer aplayer = ArenaPlayer.parsePlayer(p.getName());
 				if (aplayer.getArenaTeam() != null) {
 					item = item.replace("%team%", aplayer.getArenaTeam().getName());
 					item = item.replace("%color%", aplayer.getArenaTeam().getColor().toString());
 				}
 			}
 			
			if (item.contains("%players%")) {
				String[] players = new String[arena.getFighters().size()];
				int pos = 0;
				
				for (ArenaTeam team : arena.getTeams()) {
					for (ArenaPlayer player : team.getTeamMembers()) {
						players[pos++] = team.colorizePlayer(player.get());
					}
				}
				item = item.replace("%players%", StringParser.joinArray(players, ChatColor.RESET + ", "));

			}
			
 			item = item.replace("%arena%", a.getName());
 			item = ChatColor.translateAlternateColorCodes('&', item);
 			
 			final String[] split = item.split("<=>");
 			if (split.length < 2) {
 				PVPArena.instance.getLogger().warning("[PE] skipping: [" + a.getName() + "]:event." + string + "=>" + item);
 				continue;
 			}
 			/*
 			items.add("cmd<=>deop %player%");
 			items.add("pcmd<=>me joins %arena%");
 			items.add("brc<=>Join %arena%!");
 			items.add("power<=>power1");
 			items.add("switch<=>switch1");
 			items.add("msg<=>Welcome to %arena%!");
 			items.add("abrc<=>Welcome, %player%");
 			items.add("clear<=>battlefield");
 			
 			
 			items.add("brc<=>we had minimum players!<=>minplayers");
 			 */
 			
 			if (split.length == 3) {
 				if ("minplayers".equals(split[2])) {
 					if (arena.getPlayedPlayers().size() < arena.getArenaConfig().getInt(CFG.ITEMS_MINPLAYERS)) {
 						return;
 					}
 				}
 			}
 			
 			if (split[0].equalsIgnoreCase("cmd")) {
 				Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), split[1]);
 			} else if (split[0].equalsIgnoreCase("pcmd")) {
 				class RunLater implements Runnable {
 
 					@Override
 					public void run() {
 						if (p == null) {
 							PVPArena.instance.getLogger().warning("Trying to commit command for null player: " + string);
 						} else {
 							p.performCommand(split[1]);
 						}
 					}
 					
 				}
 				Bukkit.getScheduler().runTaskLater(PVPArena.instance, new RunLater(), 5L);
 			} else if (split[0].equalsIgnoreCase("brc")) {
 				Bukkit.broadcastMessage(split[1]);
 			} else if (split[0].equalsIgnoreCase("abrc")) {
 				arena.broadcast(split[1]);
 			} else if (split[0].equalsIgnoreCase("clear")) {
 				ArenaRegionShape ars = arena.getRegion(split[1]);
 				if (ars == null && "all".equals(split[1])) {
 					for (ArenaRegionShape region : arena.getRegions()) {
 						region.removeEntities();
 					}
 				} else if (ars != null) {
 					ars.removeEntities();
 				}
 			} else if (split[0].equalsIgnoreCase("power")) {
 				PABlockLocation loc = new PABlockLocation(SpawnManager.getCoords(a, split[1]).toLocation());
 				
 				Bukkit.getScheduler().scheduleSyncDelayedTask(PVPArena.instance, new EADelay(loc), 1L);
 				
 			} else if (split[0].equalsIgnoreCase("msg") && p != null) {
 				p.sendMessage(split[1]);
 			}
 		}
 	}
 }
