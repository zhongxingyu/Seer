 package com.wolvencraft.prison.mines.settings;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.wolvencraft.prison.mines.CommandManager;
 import com.wolvencraft.prison.mines.PrisonMine;
 
 public class Settings extends com.wolvencraft.prison.settings.Settings {
 	public final boolean PLAYERS_TP_ON_RESET;
 	public final boolean RESET_FORCE_TIMER_UPDATE;
 	public final boolean RESET_ALL_MINES_ON_STARTUP;
 	public final List<String> BANNEDNAMES;
 	public final int DEFAULTTIME;
 	
 	public Settings(PrisonMine plugin) {
 		super(PrisonMine.getPrisonSuite());
 		PLAYERS_TP_ON_RESET = plugin.getConfig().getBoolean("players.teleport-players-out-of-the-mine");
 		RESET_FORCE_TIMER_UPDATE = plugin.getConfig().getBoolean("reset.force-reset-timer-on-mine-reset");
 		RESET_ALL_MINES_ON_STARTUP = plugin.getConfig().getBoolean("reset.reset-all-mines-on-startup");
 		BANNEDNAMES = new ArrayList<String>();
 		for(CommandManager cmd : CommandManager.values()) {
 			for(String alias : cmd.getLocalAlias()) {
 				BANNEDNAMES.add(alias);
 			}
 		}
 		DEFAULTTIME = 900;
 	}
 }
