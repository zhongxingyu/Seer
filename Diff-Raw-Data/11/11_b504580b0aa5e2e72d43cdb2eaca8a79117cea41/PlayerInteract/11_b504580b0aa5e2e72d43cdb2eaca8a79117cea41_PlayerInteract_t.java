 package tk.nekotech.war.events;
 
 import java.util.Random;
 
 import org.bukkit.ChatColor;
 import org.bukkit.GameMode;
 import org.bukkit.Material;
 import org.bukkit.block.Sign;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.potion.PotionEffect;
 import org.bukkit.potion.PotionEffectType;
 
 import tk.nekotech.war.War;
 
 public class PlayerInteract implements Listener {
 	private War war;
 	
 	public PlayerInteract(War war) {
 		this.war = war;
 	}
 	
 	@EventHandler
 	public void onPlayerInteract(PlayerInteractEvent event) {
 		if (event.getClickedBlock() != null) {
 			if (event.getClickedBlock().getState() instanceof Sign) {
 				Sign sign = (Sign) event.getClickedBlock().getState();
 				Player player = event.getPlayer();
 				if (player.getGameMode() == GameMode.CREATIVE) {
 					if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
 						event.setCancelled(true);
 					}
 				} else {
 					if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
 						return;
 					}
 				}
 				if (ChatColor.stripColor(sign.getLine(0)).equals("[BLU]")) {
 					sign.setLine(0, String.valueOf(ChatColor.BLUE) + String.valueOf(ChatColor.BOLD) + "[BLU]");
 					sign.setLine(2, String.valueOf(ChatColor.WHITE) + "Punch me");
 					sign.setLine(3, String.valueOf(ChatColor.WHITE) + "if on blu!");
 					if (war.teamhelpers.teamName(player) == 0) {
 						war.getServer().broadcastMessage(war.getMessage() + ChatColor.AQUA + event.getPlayer().getName() + " got a buff from the Magical Temple!");
 						event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 600, 1));
 						event.getPlayer().setExp(event.getPlayer().getExp() + 300F);
 						war.teamhelpers.toSpawn(player, war.teamhelpers.teamName(player));
 						war.sendMessage(player, ChatColor.AQUA + "Whoosh!");
 					} else {
 						war.sendMessage(player, ChatColor.RED + "You can't hit another teams sign! Find your own...");
 					}
 				}
 				if (ChatColor.stripColor(sign.getLine(0)).equals("[RED]")) {
 					sign.setLine(0, String.valueOf(ChatColor.RED) + String.valueOf(ChatColor.BOLD) + "[RED]");
 					sign.setLine(2, String.valueOf(ChatColor.WHITE) + "Punch me");
 					sign.setLine(3, String.valueOf(ChatColor.WHITE) + "if on red!");
 					if (war.teamhelpers.teamName(player) == 1) {
 						war.getServer().broadcastMessage(war.getMessage() + ChatColor.AQUA + event.getPlayer().getName() + " got a buff from the Magical Temple!");
 						event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 600, 1));
 						event.getPlayer().setExp(event.getPlayer().getExp() + 300F);
 						war.teamhelpers.toSpawn(player, war.teamhelpers.teamName(player));
 						war.sendMessage(player, ChatColor.AQUA + "Whoosh!");
 					} else {
 						war.sendMessage(player, ChatColor.RED + "You can't hit another teams sign! Find your own...");
 					}
 				}
 				if (ChatColor.stripColor(sign.getLine(0)).equals("[JKS]")) {
 					sign.setLine(0, String.valueOf(ChatColor.GRAY) + String.valueOf(ChatColor.BOLD) + "[JKS]");
 					sign.setLine(2, String.valueOf(ChatColor.WHITE) + "Punch me");
 					sign.setLine(3, String.valueOf(ChatColor.WHITE) + "if " + String.valueOf(ChatColor.STRIKETHROUGH) + "stupid" + ChatColor.RESET + " smart!");
 					event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.HARM, 600, 2));
 					event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 600, 2));
 					war.teamhelpers.toSpawn(player, war.teamhelpers.teamName(player));
 					war.sendMessage(player, ChatColor.AQUA + "Whoosh!");
 				}
 				if (ChatColor.stripColor(sign.getLine(1)).equals("It's good")) {
 					sign.setLine(1, ChatColor.WHITE + "It's good");
 					sign.setLine(2, ChatColor.WHITE + "to be " + ChatColor.AQUA + "curious" + ChatColor.RESET + "!");
 	                final Inventory inventory = war.getServer().createInventory(event.getPlayer(), 27);
 	                ItemStack[] items = new ItemStack[27];
 	                Random random = new Random();
 	                for (int i = 0; i < 27; i++) {
 	                	int r = random.nextInt(27);
 	                	if (r == 0) {
 	                		items[i] = new ItemStack(Material.DIAMOND_SWORD, 1);
 	                		items[i].setDurability((short) 5);
 	                	}
 	                	if (r == 1) {
 	                		items[i] = new ItemStack(Material.GHAST_TEAR, 2);
 	                	}
 	                	if (r == 2) {
 	                		items[i] = new ItemStack(Material.SLIME_BALL, 15);
 	                	}
 	                	if (r == 3) {
 	                		items[i] = new ItemStack(Material.WOOD_SWORD, 1);
 	                		items[i].setDurability((short) 10);
 	                	}
 	                	if (r == 4) {
 	                		items[i] = new ItemStack(Material.DIRT, 31);
 	                	}
 	                	if (r == 5) {
 	                		items[i] = new ItemStack(Material.TNT, 1);
 	                	}
 	                	if (r == 6) {
 	                		items[i] = new ItemStack(Material.BRICK, 25);
 	                	}
	                } 
 	                inventory.setContents(items);
 	                event.getPlayer().openInventory(inventory);
 				}
 				if (ChatColor.stripColor(sign.getLine(1)).equals("You might just")) {
 					sign.setLine(1, ChatColor.WHITE + "You might just");
 					sign.setLine(2, ChatColor.WHITE + "find a " + ChatColor.AQUA + "prize" + ChatColor.RESET + "...");
 				}
 			}
 		}
 	}
 
 }
