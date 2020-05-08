 package dev.mCraft.Coinz.GUI;
 
 import org.bukkit.Bukkit;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 import org.getspout.spoutapi.event.screen.ButtonClickEvent;
 import org.getspout.spoutapi.event.screen.TextFieldChangeEvent;
 import org.getspout.spoutapi.gui.Button;
 import org.getspout.spoutapi.gui.GenericLabel;
 import org.getspout.spoutapi.gui.GenericTextField;
 import org.getspout.spoutapi.inventory.SpoutItemStack;
 import org.getspout.spoutapi.player.SpoutPlayer;
 
 import dev.mCraft.Coinz.Coinz;
 import dev.mCraft.Coinz.GUI.TellerMenu.Popup;
 
 public class TellerScreenListener implements Listener {
 	private Popup popup;
	private Coinz plugin;
 	
 	private Button button;
 	private SpoutPlayer player;
 	private PlayerInventory inv;
 	private SpoutItemStack stack;
 	private short dur;
 	
 	private GenericTextField enter;
 	private GenericLabel balance;
 	
 	private double add;
 	private double amount;
 	private double oldAmount;
 	private double coin;
 	private double remove;
 	
 	private SpoutItemStack copp;
 	private SpoutItemStack bron;
 	private SpoutItemStack silv;
 	private SpoutItemStack gold;
 	private SpoutItemStack plat;
 	
 	public TellerScreenListener() {
 		Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
 	}
 	
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onButtonClick(ButtonClickEvent event) {
 		button = event.getButton();
 		player = event.getPlayer();
 		inv = player.getInventory();
 		popup = Popup.hook;
 		plugin = Coinz.instance;
 		
 		enter = popup.enter;
 		balance = popup.amount;
 		
 		if (button.getText() != null && button.getPlugin() == plugin) {
 			
 			if (button.getId() == popup.escape.getId()) {
 				player.closeActiveWindow();
 			}
 			
 			if (button.getId() == popup.deposit.getId()) {
 				depositCoins();
 			}
 			
 			if (button.getId() == popup.withdraw.getId()) {
 				withdrawCoins();
 			}
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onTextFieldChange(TextFieldChangeEvent event) {
 		event.getTextField().setFocus(true);
 		popup = Popup.hook;
 		player = event.getPlayer();
 		if (popup.containsWidget(popup.notEnoughA)) {
 			popup.removeWidget(popup.notEnoughA);
 		}
 		
 		if (popup.containsWidget(popup.notEnoughC)) {
 			popup.removeWidget(popup.notEnoughC);
 		}
 		
 		if (popup.containsWidget(popup.wrongChange)) {
 			popup.removeWidget(popup.wrongChange);
 		}
 	}
 	
 	public void depositCoins() {
 		
 		if (enter.getPlugin() == plugin && !enter.getText().isEmpty()) {
 			add = Double.parseDouble(enter.getText());
 			amount = add;
 			coin = 0;
 			
 			for (ItemStack item : inv.getContents()) {
 				if (item != null) {
 					stack = new SpoutItemStack(item);
 					dur = stack.getDurability();
 					
 					if (stack.isCustomItem()) {
 						if (dur == plugin.CopperCoin.getDurability()) {
 							coin = coin + (stack.getAmount() * 0.1);
 							copp = stack;
 						}
 						
 						if (dur == plugin.BronzeCoin.getDurability()) {
 							coin = coin + (stack.getAmount() * 1);
 							bron = stack;
 						}
 						
 						if (dur == plugin.SilverCoin.getDurability()) {
 							coin = coin + (stack.getAmount() * 10);
 							silv = stack;
 						}
 						
 						if (dur == plugin.GoldCoin.getDurability()) {
 							coin = coin + (stack.getAmount() * 100);
 							gold = stack;
 						}
 						
 						if (dur == plugin.PlatinumCoin.getDurability()) {
 							coin = coin + (stack.getAmount() * 1000);
 							plat = stack;
 						}
 					}
 				}
 			}
 
 			if (coin >= amount) {
 				oldAmount = amount;
 				if (plat != null && plat.getDurability() == plugin.PlatinumCoin.getDurability()) {
 					while (plat.getAmount() >=1 && amount >= 1000) {
 						inv.removeItem(plugin.PlatinumCoin);
 						amount = amount - 1000;
 						plat.setAmount(plat.getAmount() - 1);
 					}
 				}
 				
 				if (gold != null && gold.getDurability() == plugin.GoldCoin.getDurability()) {
 					while (gold.getAmount() >= 1 && amount >= 100) {
 						inv.removeItem(plugin.GoldCoin);
 						amount = amount - 100;
 						gold.setAmount(gold.getAmount() - 1);
 					}
 				}
 				
 				if (silv != null && silv.getDurability() == plugin.SilverCoin.getDurability()) {
 					while (silv.getAmount() >= 1 && amount >= 10) {
 						inv.removeItem(plugin.SilverCoin);
 						amount = amount - 10;
 						silv.setAmount(silv.getAmount() - 1);
 					}
 				}
 				
 				if (bron != null && bron.getDurability() == plugin.BronzeCoin.getDurability()) {
 					while (bron.getAmount() >= 1 && amount >= 1) {
 						inv.removeItem(plugin.BronzeCoin);
 						amount = amount - 1;
 						bron.setAmount(bron.getAmount() - 1);
 					}
 				}
 				
 				if (copp != null && copp.getDurability() == plugin.CopperCoin.getDurability()) {
 					while (copp.getAmount() >= 1 && amount >= 0.1) {
 						inv.removeItem(plugin.CopperCoin);
 						amount = amount - 0.1;
 						copp.setAmount(copp.getAmount() - 1);
 					}
 				}
 				
 				if (amount > 0) {
 					oldAmount = oldAmount - amount;
 					popup.attachWidget(plugin, popup.wrongChange);
 
 					while (oldAmount >= 1000) {
 						inv.addItem(plugin.PlatinumCoin);
 						oldAmount = oldAmount - 1000;
 					}
 					
 					while (oldAmount >= 100) {
 						inv.addItem(plugin.GoldCoin);
 						oldAmount = oldAmount - 100;
 					}
 					
 					while (oldAmount >= 10) {
 						inv.addItem(plugin.SilverCoin);
 						oldAmount = oldAmount - 10;
 					}
 					
 					while (oldAmount >= 1) {
 						inv.addItem(plugin.BronzeCoin);
 						oldAmount = oldAmount - 1;
 					}
 					
 					while (oldAmount >= 0.1) {
 						inv.addItem(plugin.CopperCoin);
 						oldAmount = oldAmount - 0.1;
 					}
 				}
 				else {
 					plugin.econ.depositPlayer(player.getName(), add);
 					player.sendMessage(add + " " + "has been added to your account");
 					enter.setText("");
 					balance.setText(plugin.econ.format(plugin.econ.getBalance(player.getName())));
 				}
 			}
 			
 			else {
 				popup.attachWidget(plugin, popup.notEnoughC);
 			}
 		}
 	}
 	
 	public void withdrawCoins() {
 		
 		if (!enter.getText().isEmpty()) {
 			remove = Double.parseDouble(enter.getText());
 			amount = remove;
 			
 			if (plugin.econ.has(player.getName(), remove)) {
 				
 				while (amount >= 1000) {
 					inv.addItem(plugin.PlatinumCoin);
 					amount = amount - 1000;
 				}
 				
 				while (amount >= 100) {
 					inv.addItem(plugin.GoldCoin);
 					amount = amount - 100;
 				}
 				
 				while (amount >= 10) {
 					inv.addItem(plugin.SilverCoin);
 					amount = amount - 10;
 				}
 				
 				while (amount >= 1) {
 					inv.addItem(plugin.BronzeCoin);
 					amount = amount - 1;
 				}
 				
 				while (amount >= 0.1) {
 					inv.addItem(plugin.CopperCoin);
 					amount = amount - 0.1;
 				}
 				
 				plugin.econ.withdrawPlayer(player.getName(), remove);
 				player.sendMessage(enter.getText() + " " + "has been taken from your account");
 				enter.setText("");
 				balance.setText(plugin.econ.format(plugin.econ.getBalance(player.getName())));
 			}
 			
 			else {
 				popup.attachWidget(plugin, popup.notEnoughA);
 			}
 		}
 	}
 }
