 package net.roguedraco.infobutton;
 
 import java.io.BufferedReader;
 import java.io.DataInputStream;
 import java.io.FileInputStream;
 import java.io.InputStreamReader;
 import java.util.HashSet;
 import java.util.Set;
 
 
 import net.roguedraco.infobutton.player.RDPlayer;
 import net.roguedraco.infobutton.player.RDPlayers;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 
 public class InfoButton {
 
 	private Location location;
 
 	private boolean enabled = false;
 	private double price = 0.00;
 	private String permission = "infobutton.use";
 
 	private Set<ButtonAction> actions = new HashSet<ButtonAction>();
 
 	public InfoButton(Block block) {
 		this.location = block.getLocation();
 		this.enabled = InfoButtonPlugin.getPlugin().getConfig()
 				.getBoolean("buttonDefaults.enabled");
 		this.price = InfoButtonPlugin.getPlugin().getConfig()
 				.getDouble("buttonDefaults.price");
 		this.permission = InfoButtonPlugin.getPlugin().getConfig()
 				.getString("buttonDefaults.permission");
 	}
 
 	public Location getLocation() {
 		return location;
 	}
 
 	public boolean isEnabled() {
 		return enabled;
 	}
 
 	public double getPrice() {
 		return price;
 	}
 
 	public String getPermission() {
 		return permission;
 	}
 
 	public Set<ButtonAction> getActions() {
 		return actions;
 	}
 
 	public void setLocation(Location location) {
 		this.location = location;
 	}
 
 	public void setEnabled(boolean enabled) {
 		this.enabled = enabled;
 	}
 
 	public void setPrice(double price) {
 		this.price = price;
 	}
 
 	public void setPermission(String permission) {
 		this.permission = permission;
 	}
 
 	public void execute(Player player) {
 		if(enabled == false) {
 			player.sendMessage(Lang.get("exceptions.notEnabled"));
 			return;
 		}
 		if(permission != null && permission != "") {
 			if(!InfoButtonPlugin.permission.playerHas(player, permission)) {
 				player.sendMessage(Lang.get("exceptions.noPermission"));
 				return;
 			}
 		}
 		
 		RDPlayer rdp = RDPlayers.getPlayer(player.getName());
 		int x,y,z = 0;
 		x = this.location.getBlockX();
 		y = this.location.getBlockY();
 		z = this.location.getBlockZ();
 		boolean confirmed = false;
 		
		if(rdp.getInt("tmp.confirm."+x+"-"+y+"-"+z) == 1l) {
 			confirmed = true;
 			rdp.set("tmp.confirm."+x+"-"+y+"-"+z,null);
 		}
 		
 		if (price > 0) {			
 			if(confirmed == true) {
 				if(InfoButtonPlugin.economy.getBalance(player.getName()) >= price) {
 					InfoButtonPlugin.economy.withdrawPlayer(player.getName(), price);
 				}
 				else {
 					player.sendMessage(Lang.get("exceptions.notEnoughFunds"));
 					return;
 				}
 			}
 			else {
 				rdp.set("tmp.confirm."+x+"-"+y+"-"+z,"1");
 				player.sendMessage(Lang.get("warning.confirmPaidButton"));
 			}
 		}
 		for (ButtonAction action : actions) {
 			ActionType type = action.getType();
 			if (type == ActionType.CONSOLE_COMMAND) {
 				Bukkit.getServer().dispatchCommand(
 						Bukkit.getServer().getConsoleSender(),
 						action.getValue().replaceAll("%player%", player.getName()));
 			}
 			if (type == ActionType.PLAYER_COMMAND) {
 				player.chat("/" + action.getValue().replaceAll("%player%", player.getName()));
 			}
 			if (type == ActionType.FILE_READ) {
 				try {
 					FileInputStream fstream = new FileInputStream(
 							InfoButtonPlugin.getPlugin().getDataFolder()
 									+ "/files/" + action.getValue() + ".txt");
 					DataInputStream in = new DataInputStream(fstream);
 					BufferedReader br = new BufferedReader(
 							new InputStreamReader(in));
 					String strLine;
 					while ((strLine = br.readLine()) != null) {
 						player.sendMessage(Lang.parseColours(strLine).replaceAll("%player%", player.getName()));
 					}
 					in.close();
 				} catch (Exception e) {
 					player.sendMessage(Lang.get("exceptions.invalidFile"));
 				}
 			}
 		}
 	}
 
 	public void addAction(ActionType type, String value) {
 		ButtonAction action = new ButtonAction(type, value);
 		this.addAction(action);
 	}
 
 	public void addAction(ButtonAction action) {
 		actions.add(action);
 	}
 
 }
