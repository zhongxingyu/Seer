 package me.olloth.lordned.seasons.listener;
 
 import java.util.UUID;
 
 import me.olloth.lordned.seasons.Seasons;
 import me.olloth.lordned.seasons.util.Config;
 import me.olloth.lordned.seasons.util.Times;
 
 import org.bukkit.entity.Player;
 import org.getspout.spoutapi.event.spout.ServerTickEvent;
 import org.getspout.spoutapi.event.spout.SpoutCraftEnableEvent;
 import org.getspout.spoutapi.event.spout.SpoutListener;
 import org.getspout.spoutapi.gui.GenericLabel;
 import org.getspout.spoutapi.player.SpoutPlayer;
 
 /**
  * 
  * @author Olloth
  */
 public class SListener extends SpoutListener {
 
 	private Seasons plugin;
 	long count;
 
 	public static int DAY_OF_WEEK, DAY_OF_SEASON, SEASON, YEAR;
 	public static long TIME_OF_DAY, FULL_TIME;
 
	private int dayOfSeason, season, year;
 
 	
 	/**
 	 * Constructor, sets the ticks counter to 0;
 	 * @param plugin
 	 */
 	public SListener(Seasons plugin) {
 		count = 0;
 		this.plugin = plugin;
 	}
 
 	/**
 	 * Runs when SpoutCraftEnables after a player joins.
 	 */
 	@Override
 	public void onSpoutCraftEnable(SpoutCraftEnableEvent event) {
 		
 		if(Config.ENABLED_WORLDS.contains(event.getPlayer().getWorld().getName())) {
 			event.getPlayer().setTexturePack(Times.getSeasonUrl());
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
 
 		FULL_TIME = plugin.getServer().getWorld(Config.CALENDAR_WORLD)
 				.getFullTime();
 		TIME_OF_DAY = plugin.getServer().getWorld(Config.CALENDAR_WORLD)
 				.getTime();
 
 		DAY_OF_WEEK = Times.getDayOfWeek(FULL_TIME);
 		DAY_OF_SEASON = Times.getDayOfSeason(FULL_TIME);
 		SEASON = Times.getSeason(FULL_TIME);
 		YEAR = Times.getYear(FULL_TIME);
 
		if (DAY_OF_SEASON != dayOfSeason || SEASON != season || YEAR != year) {
 			updateHud();
			dayOfSeason = DAY_OF_SEASON;
			year = YEAR;
 		}
 
 		if (SEASON != season) {
 			updateTextures();
 			season = SEASON;
 		}
 
 	}
 
 	/**
 	 * Updates the HUD for every player currently online.
 	 */
 	public void updateHud() {
 		for (Player player : plugin.getServer().getOnlinePlayers()) {
 			SpoutPlayer sPlayer = (SpoutPlayer) player;
 			UUID labelId = (UUID) Seasons.labels.get(player.getName());
 			GenericLabel label = (GenericLabel) sPlayer.getMainScreen()
 					.getWidget(labelId);
 			label.setText(Times.getDate());
 			label.setDirty(true);
 		}
 	}
 
 	/**
 	 * Updates the texture pack for every player currently online.
 	 */
 	public void updateTextures() {
 		for (Player player : plugin.getServer().getOnlinePlayers()) {
 			SpoutPlayer sPlayer = (SpoutPlayer) player;
 			if(Config.ENABLED_WORLDS.contains(player.getWorld().getName())) {
 				sPlayer.setTexturePack(Times.getSeasonUrl());
 			}
 		}
 	}
 
 }
