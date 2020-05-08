 package de.postcrafter.tutorial;
 
 import java.sql.Connection;
 import java.util.HashMap;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.inventory.InventoryClickEvent;
 import org.bukkit.event.inventory.InventoryCloseEvent;
 import org.bukkit.event.player.PlayerKickEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.meta.ItemMeta;
 
 public class Teleport implements CommandExecutor, Listener {
 
 	private HashMap<String, String> inv = new HashMap<String, String>();
 	
 	@Override
 	public boolean onCommand(CommandSender cs, Command cmd, String label, String[] arg3) {
 		if (!(cs instanceof Player)) {
 			cs.sendMessage("§cComputer können sich nicht teleportieren");
 			return true;
 		}
 		Player p = (Player) cs;
 		this.openInv(p);
 		Connection conn;
 		return false;
 	}
 	
 	@EventHandler
 	public void onClick(InventoryClickEvent e) {
 		if (e.getSlot() == e.getRawSlot()) {
 			Player p = (Player) e.getWhoClicked();
 			if (this.inv.containsKey(p.getName())) {
 				if (this.inv.get(p.getName()).equals("Teleport")) {
 					e.setCancelled(true);
 					p.updateInventory();
 					ItemStack item = e.getCurrentItem();
					if (item != null && item.getType != Material.AIR) {
						String display = item.getItemMeta().getDisplayName();
						if (display != null) {
							String name = ChatColor.stripColor(display);
							Player p_ = Bukkit.getPlayerExact(name);
							if (p_ != null) {
								p.teleport(p_);
								p.sendMessage("§7[§6InvTP§7] Du wurdest zu §6" + p_.getName() + "§7 teleportiert.");
							} else {
								p.sendMessage("§7[§6InvTP§7] Der Spieler §6" + name + "§7 ist nicht online.");
							}
							this.closeInv(p);
 						}
 					}
 				}
 			}
 		}
 	}
 	
 	@EventHandler
 	public void onClose(InventoryCloseEvent e) {
 		this.closeInv((Player) e.getPlayer());
 	}
 	
 	@EventHandler
 	public void onKick(PlayerKickEvent e) {
 		this.closeInv(e.getPlayer());
 	}
 	
 	@EventHandler
 	public void onQuit(PlayerQuitEvent e) {
 		this.closeInv(e.getPlayer());
 	}
 	
 	public void openInv(Player p) {
 		int lines = 0;
 		Player[] players = Bukkit.getOnlinePlayers();
 		while (lines * 9 < players.length - 1) {
 			lines++;
 		}
 		if (lines > 6) {
 			lines = 6;
 		}
 		Inventory inv = Bukkit.createInventory(null, lines * 9, "§8§lTeleport");
 		int slot = 0;
 		for (int i = 0; i < players.length; i++) {
 			Player p_ = players[i];
 			if (p_ != p) {
 				ItemStack item = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
 				ItemMeta meta = item.getItemMeta();
 				meta.setDisplayName("§l" + p_.getName());
 				item.setItemMeta(meta);
 				inv.setItem(slot, item);
 				slot++;
 			}
 		}
 		p.openInventory(inv);
 		this.inv.put(p.getName(), "Teleport");
 	}
 	
 	public void closeInv(Player p) {
 		if (this.inv.containsKey(p.getName())) {
 			p.closeInventory();
 			this.inv.remove(p.getName());
 		}
 	}
 
 }
