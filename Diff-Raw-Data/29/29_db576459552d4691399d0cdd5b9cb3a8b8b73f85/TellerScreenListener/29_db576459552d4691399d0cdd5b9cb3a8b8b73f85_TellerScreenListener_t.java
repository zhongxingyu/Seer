 package dev.mCraft.Coinz.GUI;
 
 import org.bukkit.Bukkit;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 
 import org.getspout.spoutapi.event.screen.ButtonClickEvent;
 import org.getspout.spoutapi.event.screen.TextFieldChangeEvent;
 import org.getspout.spoutapi.gui.Button;
 import org.getspout.spoutapi.gui.GenericTextField;
 import org.getspout.spoutapi.player.SpoutPlayer;
 
 import dev.mCraft.Coinz.Coinz;
 import dev.mCraft.Coinz.GUI.TellerMenu.TellerPopup;
 import dev.mCraft.Coinz.api.Coins.Customer;
 
 public class TellerScreenListener implements Listener {
 	private TellerPopup tellerPopup;
 	private Coinz plugin = Coinz.instance;
 	
 	private Button button;
 	private SpoutPlayer player;
 	
 	private GenericTextField enter;
 	
 	private double amount;
 	
 	private Customer customer;
 	
 	public TellerScreenListener() {
 		Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
 	}
 	
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onButtonClick(ButtonClickEvent event) {
 		tellerPopup = TellerPopup.hook;
 		
 		if (tellerPopup == null) {
 			return;
 		}
 		
 		button = event.getButton();
 		player = event.getPlayer();
 		customer = new Customer(player);
 		plugin = Coinz.instance;
 		
 		enter = tellerPopup.enter;
 		
 		if (button.getText() != null && button.getPlugin() == plugin) {
 			
 			if (button.getId() == tellerPopup.escape.getId()) {
 				player.closeActiveWindow();
 			}
 			
 			if (button.getId() == tellerPopup.deposit.getId()) {
				try {
					amount = Double.parseDouble(enter.getText());
				}
				catch (Exception e) {
					tellerPopup.attachWidget(plugin, tellerPopup.invalidChar);
					enter.setText("");
					return;
				}
				
 				customer.depositCoins(amount);
 				player.closeActiveWindow();
 			}
 			
 			if (button.getId() == tellerPopup.withdraw.getId()) {
				try {
					amount = Double.parseDouble(enter.getText());
				}
				catch (Exception e) {
					tellerPopup.attachWidget(plugin, tellerPopup.invalidChar);
					enter.setText("");
					return;
				}
				
 				customer.withdrawCoins(amount);
 				player.closeActiveWindow();
 			}
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onTextFieldChange(TextFieldChangeEvent event) {
 		event.getTextField().setFocus(true);
 		tellerPopup = TellerPopup.hook;
 		player = event.getPlayer();
 		
 		if (tellerPopup.containsWidget(tellerPopup.notEnoughA)) {
 			tellerPopup.removeWidget(tellerPopup.notEnoughA);
 		}
 		
 		if (tellerPopup.containsWidget(tellerPopup.notEnoughC)) {
 			tellerPopup.removeWidget(tellerPopup.notEnoughC);
 		}
 		
 		if (tellerPopup.containsWidget(tellerPopup.wrongChange)) {
 			tellerPopup.removeWidget(tellerPopup.wrongChange);
 		}
 		
 		if (tellerPopup.containsWidget(tellerPopup.invalidChar)) {
 			tellerPopup.removeWidget(tellerPopup.invalidChar);
 		}
 		
 		if (tellerPopup.containsWidget(tellerPopup.invalidAmount)) {
 			tellerPopup.removeWidget(tellerPopup.invalidAmount);
 		}
 	}
 }
