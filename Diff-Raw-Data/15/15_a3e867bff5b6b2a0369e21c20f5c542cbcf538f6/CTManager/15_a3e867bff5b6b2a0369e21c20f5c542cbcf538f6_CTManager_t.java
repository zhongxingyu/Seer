 package net.slipcor.pvparena.modules.colorteams;
 
 import org.bukkit.Bukkit;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
import org.bukkit.plugin.IllegalPluginAccessException;
 import org.kitteh.tag.TagAPI;
 
 import net.slipcor.pvparena.PVPArena;
 import net.slipcor.pvparena.arena.Arena;
 import net.slipcor.pvparena.arena.ArenaPlayer;
 import net.slipcor.pvparena.arena.ArenaTeam;
 import net.slipcor.pvparena.commands.AbstractArenaCommand;
 import net.slipcor.pvparena.core.Language;
 import net.slipcor.pvparena.core.Config.CFG;
 import net.slipcor.pvparena.core.Language.MSG;
 import net.slipcor.pvparena.core.StringParser;
 import net.slipcor.pvparena.loadables.ArenaModule;
 
 public class CTManager extends ArenaModule {
 	protected static boolean enabled = false;
 
 	public CTManager() {
 		super("ColorTeams");
 	}
 	
 	@Override
 	public String version() {
		return "v1.1.0.327";
 	}
 	
 	@Override
 	public boolean checkCommand(String s) {
 		return s.equals("!ct") || s.equals("colorteams");
 	}
 	
 	@Override
 	public void commitCommand(CommandSender sender, String[] args) {
 		// !ct [value]
 		
 		if (!PVPArena.hasAdminPerms(sender)
 				&& !(PVPArena.hasCreatePerms(sender, arena))) {
 			arena.msg(
 					sender,
 					Language.parse(MSG.ERROR_NOPERM,
 							Language.parse(MSG.ERROR_NOPERM_X_ADMIN)));
 			return;
 		}
 
 		if (!AbstractArenaCommand.argCountValid(sender, arena, args, new Integer[] { 2 })) {
 			return;
 		}
 		
 		CFG c = null;
 		
 		if (args[1].equals("hidename")) {
 			c = CFG.CHAT_COLORNICK;
 		}
 		
 		if (c == null) {
 			arena.msg(sender, Language.parse(MSG.ERROR_ARGUMENT, args[1], "hidename"));
 			return;
 		}
 		
 		boolean b = arena.getArenaConfig().getBoolean(c);
 		arena.getArenaConfig().set(c, !b);
 		arena.getArenaConfig().save();
 		arena.msg(sender, Language.parse(MSG.SET_DONE, c.getNode(), String.valueOf(!b)));
 		
 	}
 
 	@Override
 	public void displayInfo(CommandSender player) {
 		player.sendMessage(StringParser.colorVar("enabled",arena.getArenaConfig().getBoolean(CFG.CHAT_COLORNICK))
 				+ " | "
 				+ StringParser.colorVar("hidename", arena.getArenaConfig().getBoolean(CFG.MODULES_COLORTEAMS_HIDENAME)));
 	}
 	
 	@Override
 	public void configParse(YamlConfiguration config) {
 		if (enabled) {
 			return;
 		}
 		
 		if (Bukkit.getServer().getPluginManager().getPlugin("TagAPI") != null) {
 			Bukkit.getPluginManager().registerEvents(new CTListener(), PVPArena.instance);
 			Arena.pmsg(Bukkit.getConsoleSender(), Language.parse(MSG.MODULE_COLORTEAMS_TAGAPI));
 		}
 		enabled = true;
 	}
 
 	@Override
 	public void tpPlayerToCoordName(Player player, String place) {
 		if (arena.getArenaConfig().getBoolean(CFG.CHAT_COLORNICK)) {
 			ArenaTeam team = ArenaPlayer.parsePlayer(player.getName()).getArenaTeam();
 			String n;
 			if (team == null) {
 				n = player.getName();
 			} else {
 				n = team.getColorCodeString() + player.getName();
 			}
			n = n.replaceAll("(&([a-f0-9]))", "�$2");
 			
 			player.setDisplayName(n);
 			
 			updateName(player);
 		}
 	}
 	
 	@Override
 	public void unload(final Player player) {
 		player.setDisplayName(player.getName());
 		if (enabled) {
 			class TempRunner implements Runnable {
 
 				@Override
 				public void run() {
 					try {
 						TagAPI.refreshPlayer(player);
 					} catch (Exception e) {
 						
 					}
 				}
 			}
			try {
				Bukkit.getScheduler().runTaskLater(PVPArena.instance, new TempRunner()
 				, 20*3L);
			} catch (IllegalPluginAccessException e) {
				
			}
 		}
 	}
 	
 	public void updateName(Player player) {
 		if (enabled)
 			TagAPI.refreshPlayer(player);
 	}
 }
