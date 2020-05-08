 package net.lordsofcode.zephyrus.listeners;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import net.lordsofcode.zephyrus.Zephyrus;
 import net.lordsofcode.zephyrus.api.ICustomItem;
 import net.lordsofcode.zephyrus.api.ISpell;
 import net.lordsofcode.zephyrus.events.PlayerGainXPEvent;
 import net.lordsofcode.zephyrus.events.PlayerLevelUpEvent;
 import net.lordsofcode.zephyrus.events.PlayerPostCastSpellEvent;
 import net.lordsofcode.zephyrus.items.ItemUtil;
 import net.lordsofcode.zephyrus.items.Merchant;
 import net.lordsofcode.zephyrus.utils.Lang;
 import net.lordsofcode.zephyrus.utils.PlayerConfigHandler;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.craftbukkit.v1_6_R3.entity.CraftLivingEntity;
 import org.bukkit.entity.HumanEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.inventory.InventoryClickEvent;
 import org.bukkit.event.inventory.InventoryCloseEvent;
 import org.bukkit.event.inventory.InventoryType;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.scheduler.BukkitRunnable;
 
 /**
  * Zephyrus
  * 
  * @author minnymin3
  * @license Lesser GNU Public License v3 (http://www.gnu.org/licenses/lgpl.html)
  * 
  */
 
 public class LevelingListener implements Listener {
 
 	public LevelingListener() {
 		Lang.add("levelling.nonew", "You have not learned any new spells");
 		Lang.add("levelling.newspells", "You have learned");
 		Lang.add("itemlevel.max", "That item is already at max level!");
 		Lang.add("itemlevel.noitemerror", "Something went wrong. Item not found...");
 	}
 
 	@EventHandler
 	public void onCast(PlayerPostCastSpellEvent e) {
 		Zephyrus.getUser(e.getPlayer()).levelProgress(e.getSpell().getExp());
 	}
 
 	@EventHandler
 	public void onXPGain(PlayerGainXPEvent e) {
		if (e.getPlayer().hasPermission("zephyrus.xp")) {
 			e.setCancelled(true);
 		}
 	}
 	
 	@EventHandler
 	public void onLevelUp(PlayerLevelUpEvent e) {
 		if (Zephyrus.getConfig().getBoolean("Levelup-Spells")) {
 			Player player = e.getPlayer();
 			List<String> l = new ArrayList<String>();
 			List<String> learned = PlayerConfigHandler.getConfig(player).getStringList("learned");
 			for (ISpell spell : Zephyrus.getSpellMap().values()) {
 				if (spell.getReqLevel() == e.getLevel()) {
 					learned.add(spell.getDisplayName().toLowerCase());
 					l.add(spell.getDisplayName().toLowerCase());
 				}
 			}
 			FileConfiguration cfg = PlayerConfigHandler.getConfig(player);
 			cfg.set("learned", learned);
 			PlayerConfigHandler.saveConfig(player, cfg);
 			StringBuilder sb = new StringBuilder();
 			Iterator<String> it = l.iterator();
 			while (it.hasNext()) {
 				String str = it.next();
 				if (it.hasNext()) {
 					sb.append(", " + str);
 				} else if (sb.length() != 0) {
 					sb.append(" and " + str);
 				} else {
 					sb.append(str);
 				}
 			}
 			String str = sb.toString();
 			if (str.equals("") || sb.length() == 0) {
 				player.sendMessage(ChatColor.AQUA + Lang.get("levelling.nonew"));
 			} else {
 				player.sendMessage(ChatColor.AQUA + Lang.get("levelling.newspells") + ChatColor.DARK_AQUA
 						+ str.replaceFirst(",", ""));
 			}
 		}
 	}
 
 	@SuppressWarnings("deprecation")
 	@EventHandler
 	public void onClickWithItem(PlayerInteractEvent e) {
 		if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
 			Block block = e.getClickedBlock();
 			byte b = 12;
 			if (block.getType() == Material.ENCHANTMENT_TABLE && block.getData() == b) {
 				ItemStack i = e.getItem();
 				if (i != null && i.hasItemMeta() && i.getItemMeta().hasDisplayName()
 						&& Zephyrus.getItemMap().containsKey(i.getItemMeta().getDisplayName())) {
 					e.setCancelled(true);
 					try {
 						new CraftLivingEntity(null, null);
 					} catch (NoClassDefFoundError err) {
 						Lang.errMsg("outofdatebukkit", e.getPlayer());
 						return;
 					}
 					ICustomItem customItem = Zephyrus.getItemMap().get(i.getItemMeta().getDisplayName());
 					if (!(new ItemUtil().getItemLevel(i) < customItem.getMaxLevel())) {
 						Lang.errMsg("itemlevel.max", e.getPlayer());
 						return;
 					}
 					if (Zephyrus.getTradeMap().containsKey(e.getItem())) {
 						Merchant mer = Zephyrus.getTradeMap().get(e.getItem());
 						Merchant m = mer.clone();
 						m.openTrade(e.getPlayer());
 						Zephyrus.getMerchantMap().put(e.getPlayer().getName(), m);
 					} else {
 						Lang.errMsg("itemlevel.noitemerror", e.getPlayer());
 					}
 				}
 			}
 		}
 	}
 
 	@EventHandler
 	public void onInventoryClose(InventoryCloseEvent e) {
 		if (Zephyrus.getMerchantMap().containsKey(e.getPlayer().getName())) {
 			Zephyrus.getMerchantMap().remove(e.getPlayer().getName());
 		}
 	}
 
 	@EventHandler
 	public void onClickInv(InventoryClickEvent e) {
 		if (Zephyrus.getMerchantMap().containsKey(e.getWhoClicked().getName())) {
 			if (e.getInventory().getType() == InventoryType.MERCHANT) {
 				Merchant m = Zephyrus.getMerchantMap().get(e.getWhoClicked().getName());
 				ItemStack i = e.getCurrentItem();
 				ItemStack i2 = e.getCursor();
 				ItemStack mi = m.getInput1();
 				ItemStack m2 = m.getOutput();
 				if (e.getRawSlot() != 0 && e.getRawSlot() != 1 && i != null && i2 != null && e.getRawSlot() != 2
 						&& !i.equals(mi) && !i.equals(m2) && !i2.equals(mi) && !i2.equals(m2)
 						&& i.getType() != Material.EMERALD && i2.getType() != Material.EMERALD) {
 					e.setCancelled(true);
 				}
 				if (i != null && i.getType() == Material.EMERALD || i != null && i2.getType() == Material.EMERALD) {
 					if ((i.hasItemMeta() || i2.hasItemMeta()) && (!i.equals(mi) && !i2.equals(mi))
 							&& (!i.equals(m2) && !i2.equals(m2))) {
 						e.setCancelled(true);
 					}
 				}
 				if (i2 != null && i2.equals(m2)) {
 					new CloseInv(e.getViewers().get(0)).runTaskLater(Zephyrus.getPlugin(), 2);
 				}
 			}
 		}
 	}
 
 	private class CloseInv extends BukkitRunnable {
 		HumanEntity e;
 
 		CloseInv(HumanEntity e) {
 			this.e = e;
 		}
 
 		@Override
 		public void run() {
 			e.closeInventory();
 		}
 	}
 }
