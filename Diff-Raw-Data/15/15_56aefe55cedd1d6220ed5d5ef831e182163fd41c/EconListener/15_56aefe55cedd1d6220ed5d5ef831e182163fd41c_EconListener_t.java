 package net.lordsofcode.zephyrus.listeners;
 
 import net.lordsofcode.zephyrus.Zephyrus;
 import net.lordsofcode.zephyrus.hooks.PluginHook;
 import net.lordsofcode.zephyrus.items.SpellTome;
 import net.lordsofcode.zephyrus.items.Wand;
 import net.lordsofcode.zephyrus.player.LevelManager;
 import net.lordsofcode.zephyrus.spells.Spell;
 import net.lordsofcode.zephyrus.utils.Lang;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.block.Sign;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.block.SignChangeEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.inventory.ItemStack;
 
 /**
  * Zephyrus
  * 
  * @author minnymin3
  * @license Lesser GNU Public License v3 (http://www.gnu.org/licenses/lgpl.html)
  * 
  */
 
 public class EconListener implements Listener {
 
 	Zephyrus plugin;
 	PluginHook hook;
 
 	public EconListener(Zephyrus plugin) {
 		this.plugin = plugin;
 		hook = new PluginHook();
 		Lang.add("econ.spell.success", "You have successfully purchased [SPELL]");
 		Lang.add("econ.spell.nolevel", "You are not high enough level to purchase [SPELL]");
 		Lang.add("econ.notenough", "Insufficient funds!");
 		Lang.add("econ.spell.create.success", "Successfully created a BuySpell sign!");
 		Lang.add("econ.badcost", "Invalad cost! $<cost>");
 		Lang.add("econ.spell.create.badspell", "Invalad spell!");
 		Lang.add("econ.novault", "Vault was not detected! Economy has been disabled!");
 		Lang.add("econ.wand.create.success", "Successfully created a BuyWand sign!");
 	}
 
 	@SuppressWarnings("deprecation")
 	@EventHandler
 	public void onClickSign(PlayerInteractEvent e) {
		if (PluginHook.isEconomy()) {
 			if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
 				Material type = e.getClickedBlock().getType();
 				if (type == Material.SIGN || type == Material.SIGN_POST
 						|| type == Material.WALL_SIGN) {
 					Sign s = (Sign) e.getClickedBlock().getState();
 					if (s.getLine(0).equals(ChatColor.DARK_AQUA + "[BuySpell]")) {
 						if (e.getPlayer().hasPermission("zephyrus.buy")) {
 							double cost = Double.parseDouble(s.getLine(1)
 									.replace("$", "").replace(ChatColor.GOLD + "", ""));
 							if (PluginHook.econ.getBalance(e.getPlayer()
 									.getName()) >= cost) {
 								Spell spell = Zephyrus.spellMap.get(s
 										.getLine(2).toLowerCase()
 										.replace(ChatColor.getByChar("4") + "", ""));
 								if (e.getPlayer().hasPermission(
 										"zephyrus.spell." + spell.name()) || 
 										e.getPlayer().hasPermission("zephyrus.spell.*")) {
 									if (!(LevelManager.getLevel(e.getPlayer()) < spell
 											.getLevel())) {
 										SpellTome tome = new SpellTome(plugin,
 												spell.getDisplayName().toLowerCase(), spell.getDesc());
 										PluginHook.econ.withdrawPlayer(e
 												.getPlayer().getName(), cost);
 										e.getPlayer().getInventory()
 												.addItem(tome.item());
 										e.getPlayer().updateInventory();
 										e.getPlayer().sendMessage(Lang.get("econ.spell.success").replace("[SPELL]", spell.getDisplayName()));
 									} else {
 										e.getPlayer()
 												.sendMessage(Lang.get("econ.spell.nolevel").replace("[SPELL]", spell.getDisplayName()));
 									}
 								} else {
 									Lang.errMsg("noperm", e.getPlayer());
 								}
 							} else {
 								Lang.errMsg("econ.notenough", e.getPlayer());
 							}
 						}
 					} else if (s.getLine(0).equals(
 							ChatColor.DARK_AQUA + "[BuyWand]")) {
 						if (e.getPlayer().hasPermission("zephyrus.buy")) {
 							double cost = Double.parseDouble(s.getLine(1)
 									.replace("$", "").replace(ChatColor.GOLD + "", ""));
 							if (PluginHook.econ.getBalance(e.getPlayer()
 									.getName()) >= cost) {
 								PluginHook.econ.withdrawPlayer(e.getPlayer()
 										.getName(), cost);
 								ItemStack wand = Wand.getItem();
 								e.getPlayer().getInventory().addItem(wand);
 								e.getPlayer().updateInventory();
 							} else {
 								Lang.errMsg("econ.notenough", e.getPlayer());
 							}
 						}
 					}
 				}
 			}
 		}
 	}
 
 	@EventHandler
 	public void onCreateSign(SignChangeEvent e) {
 		if (e.getLine(0).equals("[BuySpell]")) {
 			if (e.getPlayer().hasPermission("zephyrus.buy.create")) {
				if (PluginHook.isEconomy()) {
 					Player player = e.getPlayer();
 					try {
 						if (e.getLine(1).startsWith("$")) {
 							Double.parseDouble(e.getLine(1).replace("$", ""));
 						} else {
 							throw new NumberFormatException();
 						}
 					} catch (NumberFormatException nFE) {
 						Lang.errMsg("econ.badcost", player);
 						return;
 					}
 					if (!Zephyrus.spellMap.containsKey(e.getLine(2)
 							.toLowerCase())) {
 						Lang.errMsg("econ.spell.create.badspell", player);
 						return;
 					}
 					Spell spell = Zephyrus.spellMap.get(e.getLine(2)
 							.toLowerCase());
 					if (!spell.isEnabled()) {
 						Lang.errMsg("disabled", player);
 						return;
 					}
 					e.setLine(0, ChatColor.DARK_AQUA + "[BuySpell]");
 					e.setLine(1, "$" + ChatColor.GOLD + e.getLine(1).replace("$", ""));
 					e.setLine(2, ChatColor.getByChar("4") + e.getLine(2));
 					Lang.msg("econ.spell.create.success", player);
 
 				} else {
 					Lang.errMsg("econ.novault", e.getPlayer());
 				}
 			}
 		} else if (e.getLine(0).equals("[BuyWand]")) {
 			if (e.getPlayer().hasPermission("zephyrus.buy.create")) {
				if (PluginHook.isEconomy()) {
 					Player player = e.getPlayer();
 					try {
 						if (e.getLine(1).startsWith("$")) {
 							Double.parseDouble(e.getLine(1).replace("$", ""));
 						} else {
 							throw new NumberFormatException();
 						}
 					} catch (NumberFormatException nFE) {
 						Lang.errMsg("econ.badcost", player);
 						return;
 					}
 					e.setLine(0, ChatColor.DARK_AQUA + "[BuyWand]");
 					e.setLine(1, "$" + ChatColor.GOLD + e.getLine(1).replace("$", ""));
 					Lang.msg("econ.wand.create.success", player);
 				} else {
 					Lang.errMsg("econ.novault", e.getPlayer());
 				}
 			}
 		}
 	}
 
 }
