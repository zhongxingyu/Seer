 package com.dre.managerxl;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.bukkit.Bukkit;
 import org.bukkit.GameMode;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 
 import com.dre.managerxl.broadcaster.BroadcasterPlayerMsg;
 
 public class MPlayer {
 	private static Set<MPlayer> mPlayers = new HashSet<MPlayer>();
 
 	private String name;
 	private boolean isOnline;
 	private boolean isBanned;
 	private boolean isMuted;
 	private boolean isVisible = true;
 	private long bannedTime;
 	private String bannedReason;
 	private Location home;
 	private GameMode gameMode = GameMode.SURVIVAL;
 	private long lastTeleport;
 
 	/* BroadcasterPlayer */
 	public HashMap<Integer, BroadcasterPlayerMsg> playerMsgs = new HashMap<Integer, BroadcasterPlayerMsg>();
 
 	public BroadcasterPlayerMsg getBMsg(int id) {
 		BroadcasterPlayerMsg msg = playerMsgs.get(id);
 
 		if (msg == null) {
 			msg = new BroadcasterPlayerMsg(id, this);
 		}
 
 		return msg;
 	}
 
 	public MPlayer(String name) {
 		mPlayers.add(this);
 
 		this.name = name;
 
 		if (Bukkit.getPlayer(name) != null) {
 			this.setOnline(true);
 		}
 	}
 
 	/* Statics */
 	public static Set<MPlayer> get() {
 		return mPlayers;
 	}
 
 	public static MPlayer get(String name) {
 		for (MPlayer mPlayer : mPlayers) {
 			if (mPlayer.getName().equalsIgnoreCase(name)) {
 				return mPlayer;
 			}
 		}
 
 		return null;
 	}
 
 	public static MPlayer getOrCreate(String name) {
 		for (MPlayer mPlayer : mPlayers) {
 			if (mPlayer.getName().equalsIgnoreCase(name)) {
 				return mPlayer;
 			}
 		}
 
 		return new MPlayer(name);
 	}
 
 	/* Save and Load Functions */
 	public static boolean SaveAsYml(File file) {
 		FileConfiguration ymlFile = new YamlConfiguration();
 
 		for (MPlayer player : MPlayer.get()) {
 			/* Ban */
 			ymlFile.set(player.getName() + ".isBanned", player.isBanned());
 			ymlFile.set(player.getName() + ".bannedTime", player.getBannedTime());
 			if (player.getBannedReason() != null)
 				ymlFile.set(player.getName() + ".bannedReason", player.getBannedReason());
 
 			/* Mute */
 			ymlFile.set(player.getName() + ".isMuted", player.isMuted());
 
 			/* GameMode */
 			ymlFile.set(player.getName() + ".GameMode", player.getGameMode().name());
 
 			/* Home */
 			if (player.getHome() != null) {
 				ymlFile.set(player.getName() + ".home.x", player.getHome().getX());
 				ymlFile.set(player.getName() + ".home.y", player.getHome().getY());
 				ymlFile.set(player.getName() + ".home.z", player.getHome().getZ());
 				ymlFile.set(player.getName() + ".home.pitch", (int) player.getHome().getPitch());
 				ymlFile.set(player.getName() + ".home.yaw", (int) player.getHome().getYaw());
 				ymlFile.set(player.getName() + ".home.world", player.getHome().getWorld().getName());
 			}
 
 			/* Visible */
 			ymlFile.set(player.getName() + ".isVisible", player.isVisible());
 		}
 
 		try {
 			ymlFile.save(file);
 		} catch (IOException e) {
 			return false;
 		}
 
 		return true;
 	}
 
 	public static boolean LoadAsYml(File file) {
 		FileConfiguration ymlFile = YamlConfiguration.loadConfiguration(file);
 
 		Set<String> keys = ymlFile.getKeys(false);
 
 		for (String name : keys) {
 			MPlayer mPlayer = new MPlayer(name);
 
 			/* Ban */
 			mPlayer.setBanned(ymlFile.getBoolean(name + ".isBanned"));
			mPlayer.setBannedTime(ymlFile.getLong(name + ".bannedTime"));
 			mPlayer.setBannedReason(ymlFile.getString(name + ".bannedReason"));
 
 			/* Mute */
 			mPlayer.setMuted(ymlFile.getBoolean(name + ".isMuted"));
 
 			/* GameMode */
 			mPlayer.setGameMode(GameMode.valueOf(ymlFile.getString(name + ".GameMode")));
 
 			/* Location */
 			if (ymlFile.contains(name + ".home")) {
 				World world = Bukkit.getWorld(ymlFile.getString(name + ".home.world"));
 				if (world != null) {
 					Location loc = new Location(world, ymlFile.getDouble(name + ".home.x"), ymlFile.getDouble(name + ".home.y"), ymlFile.getDouble(name + ".home.z"), ymlFile.getInt(name
 							+ ".home.pitch"), ymlFile.getInt(name + ".home.yaw"));
 					mPlayer.setHome(loc);
 				}
 			}
 
 			/* Visible */
 			mPlayer.setVisible(ymlFile.getBoolean(name + ".isVisible"));
 		}
 
 		return true;
 	}
 
 	/* Getters and Setters */
 	public String getName() {
 		return name;
 	}
 
 	public Player getPlayer() {
 		if (this.isOnline) {
 			return P.p.getServer().getPlayer(this.name);
 		}
 
 		return null;
 	}
 
 	public boolean isOnline() {
 		return isOnline;
 	}
 
 	public void setOnline(boolean online) {
 		isOnline = online;
 	}
 
 	public boolean isBanned() {
 		if (getBannedTime() > 0) {
 			if (getUntilUnBannedTime() <= 0) {
 				isBanned = false;
 				setBannedTime(0);
 			}
 		}
 
 		return isBanned;
 	}
 
 	public void setBanned(boolean banned) {
 		isBanned = banned;
 
 		if (isBanned) {
 			if (isOnline()) {
 				if (getBannedTime() > 0) {
 					getPlayer().kickPlayer(P.p.replaceColors(P.p.getLanguageReader().get("Player_Kick_TimeBan", this.getBannedReason(), "" + this.getBannedTime())));
 				} else {
 					getPlayer().kickPlayer(P.p.replaceColors(P.p.getLanguageReader().get("Player_Kick_Ban", this.getBannedReason())));
 				}
 			}
 		}
 	}
 
 	public void setBannedTime(long l) {
 		this.bannedTime = l;
 	}
 
 	public long getBannedTime() {
 		return bannedTime;
 	}
 
 	public long getUntilUnBannedTime() {
 		return bannedTime - System.currentTimeMillis();
 	}
 
 	public void setBannedReason(String bannedReason) {
 		this.bannedReason = bannedReason;
 	}
 
 	public String getBannedReason() {
 		return bannedReason;
 	}
 
 	public Location getHome() {
 		return home;
 	}
 
 	public void setHome(Location home) {
 		this.home = home;
 	}
 
 	public boolean isMuted() {
 		return isMuted;
 	}
 
 	public void setMuted(boolean isMuted) {
 		this.isMuted = isMuted;
 	}
 
 	public GameMode getGameMode() {
 		return gameMode;
 	}
 
 	public boolean setGameMode(GameMode gameMode) {
 		if (gameMode != null) {
 			this.gameMode = gameMode;
 
 			if (this.getPlayer() != null) {
 				this.getPlayer().setGameMode(this.gameMode);
 				P.p.msg(this.getPlayer(), P.p.getLanguageReader().get("Player_GameModeChanged", this.gameMode.name()));
 			}
 
 			return true;
 		}
 
 		return false;
 	}
 
 	public boolean isVisible() {
 		return this.isVisible;
 	}
 
 	public void setVisible(boolean isVisible) {
 		if (this.getPlayer() != null) {
 			for (Player player : Bukkit.getOnlinePlayers()) {
 				if (isVisible) {
 					player.showPlayer(this.getPlayer());
 				} else {
 					player.hidePlayer(this.getPlayer());
 				}
 			}
 		}
 
 		// Dynmap
 		if (P.p.dynmap != null) {
 			P.p.dynmap.assertPlayerInvisibility(this.getName(), !isVisible, "ManagerXL");
 		}
 
 		this.isVisible = isVisible;
 	}
 
 	public long getLastTeleport() {
 		return this.lastTeleport;
 	}
 
 	public void setLastTeleport(long time) {
 		this.lastTeleport = time;
 	}
 }
