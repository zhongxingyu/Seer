 package dev.mCraft.Coinz.GUI;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.block.Block;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.inventory.Inventory;
 import org.getspout.spoutapi.SpoutManager;
 import org.getspout.spoutapi.event.screen.ButtonClickEvent;
 import org.getspout.spoutapi.gui.Button;
 import org.getspout.spoutapi.gui.GenericTextField;
 import org.getspout.spoutapi.inventory.InventoryBuilder;
 import org.getspout.spoutapi.player.SpoutPlayer;
 
 import dev.mCraft.Coinz.Coinz;
 import dev.mCraft.Coinz.GUI.VaultInv.KeypadPopup;
 import dev.mCraft.Coinz.Serializer.PersistPasswords;
 import dev.mCraft.Coinz.Serializer.PersistVault;
 
 public class KeyPadListener implements Listener {
 	private Coinz plugin = Coinz.instance;
 	private KeypadPopup hook;
 	private PersistPasswords persistPass;
 	private PersistVault persist;
 	
 	private Button button;
 	private GenericTextField pass;
 	private InventoryBuilder builder;
 	
 	private Location loc;
 	
 	public KeyPadListener() {
 		Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
 	}
 
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onButtonClick(ButtonClickEvent event) {
 		hook = KeypadPopup.hook;
 		persistPass = PersistPasswords.hook;
 		
 		if (hook == null) {
 			return;
 		}
 		
 		button = event.getButton();
 		SpoutPlayer player = event.getPlayer();
 
 		Block block = player.getTargetBlock(null, 6);
 		loc = block.getLocation();
 		
 		if (button.getText() != null && button.getPlugin() == plugin) {
 			pass = hook.pass;
 		
 			if (button.getId() == hook.b0.getId()) {
 				pass.setText(pass.getText() + 0);
 			}
 			if (button.getId() == hook.b1.getId()) {
 				pass.setText(pass.getText() + 1);
 			}
 			if (button.getId() == hook.b2.getId()) {
 				pass.setText(pass.getText() + 2);
 			}
 			if (button.getId() == hook.b3.getId()) {
 				pass.setText(pass.getText() + 3);
 			}
 			if (button.getId() == hook.b4.getId()) {
 				pass.setText(pass.getText() + 4);
 			}
 			if (button.getId() == hook.b5.getId()) {
 				pass.setText(pass.getText() + 5);
 			}
 			if (button.getId() == hook.b6.getId()) {
 				pass.setText(pass.getText() + 6);
 			}
 			if (button.getId() == hook.b7.getId()) {
 				pass.setText(pass.getText() + 7);
 			}
 			if (button.getId() == hook.b8.getId()) {
 				pass.setText(pass.getText() + 8);
 			}
 			if (button.getId() == hook.b9.getId()) {
 				pass.setText(pass.getText() + 9);
 			}
 			if (button.getId() == hook.pound.getId()) {
 				pass.setText(pass.getText() + "#");
 			}
 			if (button.getId() == hook.star.getId()) {
 				pass.setText(pass.getText() + "*");
 			}
 			if (button.getId() == hook.corr.getId()) {
 				if (pass.getText() != null) {
 					String text = pass.getText();
 					int length = text.length();
 					if (length >= 2) {
 						String newtext = text.substring(0, length-1);
 						pass.setText(newtext);
 					}
 					if (length == 1) {
 						pass.setText("");
 					}
 				}
 			}
 			if (button.getId() == hook.enter.getId()) {
 				String pass = hook.pass.getText();
 				String loadPass = null;
 				
 				if (persistPass != null && loc != null) {
 					if (!persistPass.doesPasswordFileExist(loc)) {
 						try {
 							persistPass.save(loc, pass);
 						}
 						catch (Exception e) {
 							e.printStackTrace();
 						}
 						
 						player.closeActiveWindow();
 					}
 					else {
 						try {
 							loadPass = persistPass.load(loc);
 						}
 						catch (Exception e) {
 							e.printStackTrace();
 						}
 						
 						if (pass.equalsIgnoreCase(loadPass)) {
							player.getMainScreen().removeWidget(hook);
 							
 							persist = PersistVault.hook;
 							builder = SpoutManager.getInventoryBuilder();
 							
 							Inventory Vault = builder.construct(9, "Vault");
 							Inventory temp = Vault;
 							
 							try {
 								temp = persist.load(loc, temp);
 							}
 							catch (Exception e) {
 								e.printStackTrace();
 							}
 							
 							Vault = temp;
 							
 							player.openInventoryWindow(Vault, loc);
 						}
 					}
 				}
 			}
 			if (button.getId() == hook.cancel.getId()) {
 				player.closeActiveWindow();
 			}
 		}
 	}
 
 }
