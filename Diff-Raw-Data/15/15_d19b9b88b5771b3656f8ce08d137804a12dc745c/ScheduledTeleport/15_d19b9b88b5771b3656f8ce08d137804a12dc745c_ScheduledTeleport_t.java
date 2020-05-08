 /*******************************************************************************
  * Copyright (c) 2012 James Richardson.
  * 
  * ScheduledTeleport.java is part of Hearthstone.
  * 
  * Hearthstone is free software: you can redistribute it and/or modify it under
  * the terms of the GNU General Public License as published by the Free Software
  * Foundation, either version 3 of the License, or (at your option) any later
  * version.
  * 
  * Hearthstone is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  * 
  * You should have received a copy of the GNU General Public License along with
  * Hearthstone. If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 package name.richardson.james.bukkit.hearthstone.teleport;
 
 import java.text.MessageFormat;
 import java.util.Hashtable;
 import java.util.Locale;
 import java.util.Map;
 import java.util.ResourceBundle;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Effect;
 import org.bukkit.Location;
 import org.bukkit.Sound;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.scheduler.BukkitScheduler;
 
 import name.richardson.james.bukkit.hearthstone.Home;
 import name.richardson.james.bukkit.utilities.formatters.ColourFormatter;
 import name.richardson.james.bukkit.utilities.formatters.TimeFormatter;
 import name.richardson.james.bukkit.utilities.localisation.Localised;
 import name.richardson.james.bukkit.utilities.localisation.ResourceBundles;
 
 public class ScheduledTeleport implements Runnable, Localised {
 
 	public static final int MOVEMENT_THRESHOLD = 2;
 	public static final Sound TELEPORT_FAILED_SOUND = Sound.ITEM_BREAK;
 	public static final Effect TELEPORT_SUCCESS_EFFECT = Effect.ENDER_SIGNAL;
 	public static final Sound TELEPORT_SUCCESS_SOUND = Sound.ENDERMAN_TELEPORT;
 
 	private static final BukkitScheduler BUKKIT_SCHEDULER = Bukkit.getScheduler();
 	private static final Map<String, Long> COOLDOWN_TRACKER = new Hashtable<String, Long>();
 	private static final ResourceBundle LOCALISATION = ResourceBundle.getBundle(ResourceBundles.MESSAGES.getBundleName());
 
 	private static long cooldown = 0;
 	private static Plugin plugin = null;
 	private static long warmup = 0;
 	private static String warmupTime;
 
 	public static Plugin getPlugin() {
 		if (ScheduledTeleport.plugin == null) {
 			ScheduledTeleport.plugin = Bukkit.getPluginManager().getPlugin("Hearthstone");
 		}
 		return ScheduledTeleport.plugin;
 	}
 
 	public static void setCooldownTime(final long milliseconds) {
 		ScheduledTeleport.cooldown = milliseconds;
 	}
 
 	public static void setWarmupTime(final long milliseconds) {
 		ScheduledTeleport.warmup = milliseconds;
 		ScheduledTeleport.warmupTime = TimeFormatter.millisToLongDHMS(warmup);
 	}
 
 	private final int health;
 
 	private final Home home;
 
 	private final Location lastLocation;
 
 	private final Player player;
 
 	public ScheduledTeleport(final Player player, final Home location) {
 		this.player = player;
 		this.health = player.getHealth();
 		this.lastLocation = player.getLocation().clone();
 		this.home = location;
 		if (!this.hasCooldown()) {
 			this.schedule();
 		}
 	}
 
 	public String getMessage(final String key) {
 		String message = LOCALISATION.getString(key);
 		message = ColourFormatter.replace(message);
 		return message;
 	}
 
 	public String getMessage(final String key, final Object... elements) {
 		final MessageFormat formatter = new MessageFormat(LOCALISATION.getString(key));
 		formatter.setLocale(Locale.getDefault());
 		String message = formatter.format(elements);
 		message = ColourFormatter.replace(message);
 		return message;
 	}
 
 	public void run() {
 		if (!(this.hasPlayerMoved()) && !(this.hasPlayerTakenDamage()) && !(this.isLocationObstructed())) {
 			this.player.getLocation().getWorld().playEffect(this.player.getLocation(), TELEPORT_SUCCESS_EFFECT, 0);
 			this.player.getLocation().getWorld().playSound(this.player.getLocation(), TELEPORT_SUCCESS_SOUND, 1, 0);
 			this.player.teleport(this.home.getLocation());
 			this.setCooldown();
 			this.player.getLocation().getWorld().playEffect(this.player.getLocation(), TELEPORT_SUCCESS_EFFECT, 0);
 			this.player.getLocation().getWorld().playSound(this.player.getLocation(), TELEPORT_SUCCESS_SOUND, 1, 0);
 		} else {
 			this.player.playSound(this.player.getLocation(), TELEPORT_FAILED_SOUND, 1, 1);
 		}
 	}
 
 	private boolean hasCooldown() {
 		if (!COOLDOWN_TRACKER.containsKey(this.player.getName())) { return false; }
		final long timeRemaining = COOLDOWN_TRACKER.get(this.player.getName()) - System.currentTimeMillis();
		if (timeRemaining > 0) {
			this.player.sendMessage(this.getMessage("error.teleport-cooldown", TimeFormatter.millisToLongDHMS(timeRemaining)));
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	private boolean hasPlayerMoved() {
 		if (this.player.getLocation().distance(this.lastLocation) >= MOVEMENT_THRESHOLD) {
 			this.player.sendMessage(this.getMessage("error.player-moved-too-far"));
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	private boolean hasPlayerTakenDamage() {
		System.out.append(String.valueOf(this.player.getHealth()));
		System.out.append(String.valueOf(this.health));
		if (this.player.getHealth() < this.health) {
 			this.player.sendMessage(this.getMessage("error.player-taken-damage"));
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	private boolean isLocationObstructed() {
 		if (this.home.isObstructed()) {
 			this.player.sendMessage(this.getMessage("error.location-obstructed"));
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	private void schedule() {
 		if (this.player.hasPermission("hearthstone.teleport.warmup")) {
 			this.player.sendMessage(this.getMessage("notice.teleport-warmup", ScheduledTeleport.warmupTime));
 			BUKKIT_SCHEDULER.scheduleSyncDelayedTask(ScheduledTeleport.getPlugin(), this, ((ScheduledTeleport.warmup / 1000) * 20));
 		} else {
 			BUKKIT_SCHEDULER.scheduleSyncDelayedTask(plugin, this);
 		}
 	}
 
 	private void setCooldown() {
 		if (this.player.hasPermission("hearthstone.teleport.cooldown")) {
 			COOLDOWN_TRACKER.put(this.player.getName(), System.currentTimeMillis() + ScheduledTeleport.cooldown);
 		}
 	}
 
 }
