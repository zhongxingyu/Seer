 package com.lenis0012.bukkit.pvp;
 
 import java.util.Calendar;
 import java.util.List;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.command.ConsoleCommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 import com.lenis0012.bukkit.pvp.data.DataManager;
 import com.lenis0012.bukkit.pvp.hooks.VaultHook;
 import com.lenis0012.bukkit.pvp.utils.MathUtil;
 
 public class PvpPlayer {
 	private String name;
 	private DataManager sql;
 	
 	//Lets store all values local
 	private int killstreak;
 	private int kills;
 	private int deaths;
 	private int level;
 	
 	protected PvpPlayer(String playerName) {
 		this.name = playerName;
 		this.sql = PvpLevels.instance.getSqlControler();
		if(this.isCreated()) {
			this.kills = get("kills");
			this.deaths = get("deaths");
			this.level = get("level");
			this.killstreak = 0;
		}
 	}
 	
 	public boolean isCreated() {
 		return sql.contains("username", name);
 	}
 	
 	public void create() {
 		int lvl = 0;
 		int kills = 0;
 		int deaths = 0;
 		int lastLogin = days(Calendar.getInstance());
 		
 		sql.set(name, lvl, kills, deaths, lastLogin);
 	}
 	
 	private void set(String index, int value) {
 		sql.update("username", index, name, value);
 	}
 	
 	private int get(String index) {
 		return (Integer) sql.get("username", index, name);
 	}
 	
 	public int getKills() {
 		return kills;
 	}
 
 	public void setKills(int kills) {
 		this.kills = kills;
 	}
 
 	public int getDeaths() {
 		return deaths;
 	}
 
 	public void setDeaths(int deaths) {
 		this.deaths = deaths;
 	}
 
 	public int getLevel() {
 		return level;
 	}
 
 	public void setLevel(int level) {
 		this.level = level;
 	}
 
 	public void setKillstreak(int killstreak) {
 		this.killstreak = killstreak;
 	}
 	
 	public int getKillstreak() {
 		return this.killstreak;
 	}
 	
 	public void save() {
 		set("kills", this.kills);
 		set("deaths", this.deaths);
 		set("level", this.level);
 		set("lastlogin", days(Calendar.getInstance()));
 	}
 	
 	@SuppressWarnings("unchecked")
 	public void reward(Player player) {
 		PvpLevels plugin = PvpLevels.instance;
 		String lvl = String.valueOf(this.get("level"));
 		if((Boolean) plugin.getReward(lvl, "item.use")) {
 			String itemName = (String) plugin.getReward(lvl, "item.name");
 			int amount = (Integer) plugin.getReward(lvl, "item.amount");
 			Material type = Material.getMaterial(itemName);
 			ItemStack it = new ItemStack(type, amount);
 			player.getInventory().addItem(it);
 			player.sendMessage(ChatColor.GREEN+"You gained "+amount+" of "+
 					type.toString().toLowerCase());
 		}
 			
 		if((Boolean) plugin.getReward(lvl, "money.use")) {
 			double amount = (Double) plugin.getReward(lvl, "money.amount");
 			plugin.getHook("Vault").invoke(player.getName(), amount, VaultHook.DEPOSIT);
 			player.sendMessage(ChatColor.GREEN+"You gained "+amount+"$");
 		}
 			
 		if((Boolean) plugin.getReward(lvl, "commands.use")) {
 			List<String> cmds = (List<String>) plugin.getReward(lvl, "commands.commands");
 			for(String cmd : cmds) {
 				cmd = cmd.replace("{User}", player.getName());
 				ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
 				Bukkit.getServer().dispatchCommand(console, cmd);
 			}
 		}
 	}
 	
 	private int days(Calendar cal) {
 		int years = (cal.get(Calendar.YEAR) - 2000) * 365;
 		int days = cal.get(Calendar.DAY_OF_YEAR);
 		int longYear = MathUtil.floor(cal.get(Calendar.YEAR) / 4);
 		
 		return years + days + longYear;
 	}
 }
