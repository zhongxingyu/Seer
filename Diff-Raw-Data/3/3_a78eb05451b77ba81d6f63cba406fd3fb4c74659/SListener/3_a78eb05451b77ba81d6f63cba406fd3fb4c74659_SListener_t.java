 package me.ambientseasons.listener;
 
 import me.ambientseasons.AmbientSeasons;
 import me.ambientseasons.HUDLabel;
 import me.ambientseasons.util.Config;
 import me.ambientseasons.util.Times;
 
 import org.bukkit.entity.Player;
 import org.getspout.spoutapi.SpoutManager;
 import org.getspout.spoutapi.event.spout.ServerTickEvent;
 import org.getspout.spoutapi.event.spout.SpoutCraftEnableEvent;
 import org.getspout.spoutapi.event.spout.SpoutListener;
 import org.getspout.spoutapi.player.SpoutPlayer;
 
 /**
  * 
  * @author Olloth
  */
 public class SListener extends SpoutListener {
 
 	private AmbientSeasons plugin;
 	long count;
 	int seconds;
 
 	public static int DAY_OF_WEEK, DAY_OF_SEASON, SEASON, YEAR;
 
 	private int dayOfSeason, season, year;
 
 	/**
 	 * Constructor, sets the ticks counter to 0;
 	 * 
 	 * @param plugin
 	 */
 	public SListener(AmbientSeasons plugin) {
 		seconds = Config.getSeconds();
 		count = 0;
 		this.plugin = plugin;
 	}
 
 	/**
 	 * Runs when SpoutCraft enables after a player joins.
 	 */
 	@Override
 	public void onSpoutCraftEnable(SpoutCraftEnableEvent event) {
 
 		SpoutPlayer sPlayer = event.getPlayer();
 		
 		if(!AmbientSeasons.HUDEnable.containsKey(sPlayer.getName())) {
 			AmbientSeasons.HUDEnable.put(sPlayer.getName(),true);
 		}
 		
 		HUDLabel label = new HUDLabel(sPlayer);
 		label.setX(10).setY(Config.getHUDPosition());
 		
 		sPlayer.getMainScreen().attachWidget(label);
 		
		if (Config.isWorldEnabled(sPlayer.getWorld())) {
			sPlayer.setTexturePack(Times.getSeasonUrl());
		}
 	}
 
 
 	/**
 	 * Runs every tick, BE CAREFUL HERE.
 	 */
 	@Override
 	public void onServerTick(ServerTickEvent event) {
 
 		if ((count % 20) == 0) {
 			onSecond();
 		}
 
 		count++;
 	}
 
 	/**
 	 * Runs every second, BE CAREFUL HERE.
 	 */
 	private void onSecond() {
 		seconds++;
 
 		if (Config.getCalcType().toLowerCase().equals("world")) {
 			Config.setTimeCalc(plugin.getServer().getWorld(Config.getCalendarWorld()).getFullTime());
 		} else {
 			Config.setTimeCalc(seconds);
 		}
 
 		DAY_OF_WEEK = Times.getDayOfWeek(Config.getTimeCalc());
 		DAY_OF_SEASON = Times.getDayOfSeason(Config.getTimeCalc());
 		SEASON = Times.getSeason(Config.getTimeCalc());
 		YEAR = Times.getYear(Config.getTimeCalc());
 
 		if (DAY_OF_SEASON != dayOfSeason || SEASON != season || YEAR != year) {
 			dayOfSeason = DAY_OF_SEASON;
 			year = YEAR;
 		}
 
 		if (SEASON != season) {
 			updateTextures();
 
 			if (AmbientSeasons.WHEAT_MOD) {
 				plugin.wheatMod.updateSettings();
 			}
 
 			season = SEASON;
 		}
 
 	}
 	
 	/**
 	 * Updates the texture pack for every player currently online.
 	 */
 	public void updateTextures() {
 		for(Player player : plugin.getServer().getOnlinePlayers()) {
 			if (Config.isWorldEnabled(player.getWorld())) {
 				SpoutPlayer sPlayer = SpoutManager.getPlayer(player);
 				sPlayer.setTexturePack(Times.getSeasonUrl());
 			}
 		}
 	}
 
 	/**
 	 * Gets the seconds that have gone by since the plugin started.
 	 * 
 	 * @return seconds that have gone by since the plugin started
 	 */
 	public int getSeconds() {
 		return seconds;
 	}
 
 }
