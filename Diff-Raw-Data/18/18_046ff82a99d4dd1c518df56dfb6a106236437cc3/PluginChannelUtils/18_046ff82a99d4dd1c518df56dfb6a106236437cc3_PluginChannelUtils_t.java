 package com.martinbrook.tesseractuhc.util;
 
 import java.io.UnsupportedEncodingException;
 import java.util.Calendar;
 
 import org.apache.commons.lang.StringUtils;
 import org.bukkit.ChatColor;
 import org.bukkit.World.Environment;
 import org.bukkit.entity.Player;
 
 import com.martinbrook.tesseractuhc.MatchPhase;
 import com.martinbrook.tesseractuhc.TesseractUHC;
 import com.martinbrook.tesseractuhc.UhcMatch;
 import com.martinbrook.tesseractuhc.UhcParticipant;
 import com.martinbrook.tesseractuhc.UhcPlayer;
 import com.martinbrook.tesseractuhc.UhcTeam;
 
 public class PluginChannelUtils {
 	
 	public static void messageSpectators(String ...parts) {
 		for(UhcPlayer up : TesseractUHC.getInstance().getMatch().getOnlinePlayers()) {
 			if (up.isSpectator())
 				messageSpectator(up.getPlayer(), parts);
 		}
 	}
 	
 	/**
 	 * Sends a plugin channel message to a specific player, properly delimited.
 	 *
 	 * @param pl spectator to recieve the plugin channel message
 	 */
 	public static void messageSpectator(Player pl, String ...parts) {
 		try {
 			String msg = StringUtils.join(parts, TesseractUHC.PLUGIN_CHANNEL_DELIMITER);
 			pl.sendPluginMessage(TesseractUHC.getInstance(), TesseractUHC.PLUGIN_CHANNEL,
 				msg.getBytes(TesseractUHC.PLUGIN_CHANNEL_ENC));
 		}
 		catch (UnsupportedEncodingException e){}
 	}
 	
 	/**
 	 * Sends all information to a single spectator necessary to sync a match's current status.
 	 *
 	 * @param spec spectator to receive the plugin channel messages
 	 * @param match the match of which to send updates
 	 */
 	public static void updateSpectator(Player spec, UhcMatch match) {
 		messageSpectator(spec, "match", TesseractUHC.PLUGIN_CHANNEL_WORLD, "init");
 		messageSpectator(spec, "match", TesseractUHC.PLUGIN_CHANNEL_WORLD, "gametype", "UHC");
 
 		if (match.getMatchPhase() == MatchPhase.MATCH)
 			messageSpectator(spec, "match", TesseractUHC.PLUGIN_CHANNEL_WORLD, "time", MatchUtils.formatDuration(match.getMatchStartTime(), Calendar.getInstance(), true));
 
 		
 		if(match.getConfig().isFFA()){
 			messageSpectator(spec, "team", "FFA", "init");
 			messageSpectator(spec, "team", "FFA", "color", ChatColor.RED.toString());
 			messageSpectator(spec, "match", "gamerule","FFA","on");
 		}		
 		for (UhcTeam team : match.getTeams())
 		{
 			String teamName = "FFA";
 			if(!match.getConfig().isFFA()){
 				teamName = team.getName();
 				messageSpectator(spec, "team", teamName, "init");
 				messageSpectator(spec, "team", teamName, "color", team.getColor().toString());
 			}
 			for (UhcParticipant pl : team.getMembers())
 			{
 				messageSpectator(spec, "team", teamName, "player", "+" + pl.getName());
 			}
 		}
 			
 		for (UhcParticipant pl : match.getParticipants()){
 			updateSpectatorPlayerInfo(spec, pl);
 		}
 	}
 	
 	private static void updateSpectatorPlayerInfo(Player spec, UhcParticipant up) {
 		messageSpectator(spec, "player", up.getName(), "kills", Integer.toString(up.getKills()));
 		messageSpectator(spec, "player", up.getName(), "deaths", Integer.toString(up.getDeaths()));
 		messageSpectator(spec, "player", up.getName(), "streak", Integer.toString(up.getKills()));
 		messageSpectator(spec, "player", up.getName(), "accuracy", up.getAccuracy());
 
 		Player pl = up.getPlayer().getPlayer();
 		if (pl != null)
 		{
 			messageSpectator(spec, "player", up.getName(), "hp", Integer.toString(pl.getHealth()));
 			messageSpectator(spec, "player", up.getName(), "armor", Integer.toString(ArmorPoints.fromPlayer(pl)));
 		}
 
 		messageSpectator(spec, "player", up.getName(), up.getPlayer().isOnline() ? "login" : "logout");
 		
		if (pl != null)
		{
			Environment env = pl.getWorld().getEnvironment();
			String envString = "overworld";
			if (env == Environment.NETHER)
				envString = "nether";
			else if (env == Environment.THE_END)
				envString = "end";
			PluginChannelUtils.messageSpectators("player", pl.getName(), "dimension", envString);
		}
 		up.updateSpectatorOnInventory(spec);
 		
 		//TODO add cape support
 		//messageSpectator(spec, "player", up.getName(), "cape", up.getCape());
 	}
 	
 }
