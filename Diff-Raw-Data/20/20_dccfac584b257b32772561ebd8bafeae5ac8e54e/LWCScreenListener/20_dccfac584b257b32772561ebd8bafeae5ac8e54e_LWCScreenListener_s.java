 package tux2.spoutlwc;
 
 import com.griefcraft.model.LWCPlayer;
 import com.griefcraft.model.Permission;
 import com.griefcraft.model.Protection;
 import com.griefcraft.util.StringUtil;
 import org.bukkit.Material;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.getspout.spoutapi.event.screen.ButtonClickEvent;
 import org.getspout.spoutapi.gui.Button;
 import org.getspout.spoutapi.player.SpoutPlayer;
 
 public class LWCScreenListener implements Listener {
 
 	SpoutLWC plugin;
 
 	public LWCScreenListener(SpoutLWC plugin) {
 		this.plugin = plugin;
 	}
 
 	@EventHandler
 	public void onButtonClick(ButtonClickEvent event) {
 		// See if we are the owners of this button...
 		if (plugin == event.getButton().getPlugin()) {
 			Button eventbutton = event.getButton();
 			String completebutton = eventbutton.getText();
 			SpoutPlayer player = event.getPlayer();
 			if (completebutton.equalsIgnoreCase("cancel")) {
 				player.closeActiveWindow();
 				if(plugin.guiscreens.containsKey(player)) {
 					plugin.guiscreens.remove(player);
 				}
 				if(plugin.unlockscreens.containsKey(player)) {
 					plugin.unlockscreens.remove(player);
 				}
 			} else {
 				if(plugin.lwc != null) {
 					if (plugin.lwc.hasPermission(player, "lwc.protect")) {
 						if (completebutton.equalsIgnoreCase("save")) {
 							if(plugin.guiscreens.containsKey(player)) {
 								PlayerLwcGUI tgui = plugin.guiscreens.get(player);
 								Protection protection = plugin.lwc.findProtection(tgui.target);
 								if(protection != null) {
 									if(plugin.lwc.canAdminProtection(player, protection)) {
 										if(tgui.lwpassword.isSelected()) {
 											if(tgui.password.getText().equals("********") || tgui.password.getText().equals("")) {
 												player.sendNotification("Invalid Password", "Please input a password!", Material.FIRE);
 												return;
 											}
 										}
 										protection.remove();
 										if(createProtection(tgui, player)) {
 											player.sendNotification("Protection Updated!", "It is now secure.", getDisplayItem(tgui.target.getType()));
 										}else {
 											player.sendNotification("Error!", "Unable to lock " + tgui.target.getType().toString() + ".", Material.FIRE);
 										}
 									}else {
 										player.sendNotification("Unauthorized!", "Unable to lock " + tgui.target.getType().toString() + ".", Material.FIRE);
 									}
 								}else {
 									if(createProtection(tgui, player)) {
 										player.sendNotification("Protection Created!", "It is now secure.", getDisplayItem(tgui.target.getType()));
 									}
 								}
 							}else {
 								player.sendNotification("Error!", "Please try again.", Material.FIRE);
 							}
 							player.closeActiveWindow();
 							if(plugin.guiscreens.containsKey(player)) {
 								plugin.guiscreens.remove(player);
 							}
 						}else if (completebutton.equalsIgnoreCase("delete")) {
 							if(plugin.guiscreens.containsKey(player)) {
 								PlayerLwcGUI tgui = plugin.guiscreens.get(player);
 								Protection protection = plugin.lwc.findProtection(tgui.target);
 								if(protection != null) {
 									if(plugin.lwc.canAdminProtection(player, protection) && plugin.lwc.hasPermission(player, "lwc.remove")) {
 										protection.remove();
 										player.sendNotification("Protection Removed!", "Removed successfully.", getDisplayItem(tgui.target.getType()));
 									}
 								}
 							}else {
 								player.sendNotification("Error!", "Please try again.", Material.FIRE);
 							}
 							player.closeActiveWindow();
 							if(plugin.guiscreens.containsKey(player)) {
 								plugin.guiscreens.remove(player);
 							}
 						}else if (completebutton.equalsIgnoreCase("unlock")) {
 							if(plugin.unlockscreens.containsKey(player)) {
 								UnlockGUI tgui = plugin.unlockscreens.get(player);
 								Protection protection = plugin.lwc.findProtection(tgui.target);
 								if(protection != null) {
 									if(unlockProtection(tgui, protection, player)) {
 										player.sendNotification("Access Granted.", tgui.target.getType().toString().replace('_', ' ') + " Unlocked!", getDisplayItem(tgui.target.getType()));
 										player.closeActiveWindow();
 										if(plugin.unlockscreens.containsKey(player)) {
 											plugin.unlockscreens.remove(player);
 										}
 									}else {
 										player.sendNotification("Access Denied", "Please try again.", Material.FIRE);
 									}
 								}
 							}else {
 								player.sendNotification("Error!", "Please try again.", Material.FIRE);
 								player.closeActiveWindow();
 								if(plugin.unlockscreens.containsKey(player)) {
 									plugin.unlockscreens.remove(player);
 								}
 							}
 						}
 					}else if (plugin.lwc.hasPermission(player, "lwc.unlock")) {
 						if (completebutton.equalsIgnoreCase("unlock")) {
 							if(plugin.unlockscreens.containsKey(player)) {
 								UnlockGUI tgui = plugin.unlockscreens.get(player);
 								Protection protection = plugin.lwc.findProtection(tgui.target);
 								if(protection != null) {
 									if(unlockProtection(tgui, protection, player)) {
 										player.sendNotification("Access Granted.", tgui.target.getType().toString().replace('_', ' ') + " Unlocked!", getDisplayItem(tgui.target.getType()));player.closeActiveWindow();
 										if(plugin.unlockscreens.containsKey(player)) {
 											plugin.unlockscreens.remove(player);
 										}
 									}else {
 										player.sendNotification("Access Denied", "Please try again.", Material.FIRE);
 									}
 								}
 							}else {
 								player.sendNotification("Error!", "Please try again.", Material.FIRE);
 								player.closeActiveWindow();
 								if(plugin.unlockscreens.containsKey(player)) {
 									plugin.unlockscreens.remove(player);
 								}
 							}
 						}
 					} else {
 						player.sendNotification("Not Authorized!", "Sorry, no protections.", Material.FIRE);
 						player.closeActiveWindow();
 						if(plugin.unlockscreens.containsKey(player)) {
 							plugin.unlockscreens.remove(player);
 						}
 					}
 				}else {
 					player.closeActiveWindow();
 					if(plugin.guiscreens.containsKey(player)) {
 						plugin.guiscreens.remove(player);
 					}
 					if(plugin.unlockscreens.containsKey(player)) {
 						plugin.unlockscreens.remove(player);
 					}
 					player.sendNotification("LWC Not Found", "Please try again.", Material.FIRE);
 				}
 			}
 		}
 	}
 	
 	private boolean unlockProtection(UnlockGUI tgui, Protection protection, SpoutPlayer player) {
 //		System.out.println("Password is "+ tgui.password.getText());
 		if(!tgui.password.getText().equals("")) {
 			String password = StringUtil.encrypt(tgui.password.getText());
 //			System.out.println("Passwordhash is " + password);
 			if(protection.getType() == Protection.Type.PASSWORD) {
 //				System.out.println("Protection type is password");
 				if(protection.getPassword().equals(password)) {
 //					System.out.println("Passwords match");
 //					System.out.println("LWCPlayer is "+ LWCPlayer.getPlayer(player));
 					LWCPlayer.getPlayer(player).addAccessibleProtection(protection);
 	                return true;
 				}
 			}
 		}
 		
 		return false;
 	}
 
 	private boolean createProtection(PlayerLwcGUI tgui, SpoutPlayer player) {
 		// vars passed to the function that creates the protection
 		int blockId = tgui.target.getTypeId(); // if this remains 0, it will automatically set itself when first interacted with!
 		Protection.Type type = null; // see: http://griefcraft.com/javadoc/lwc/com/griefcraft/model/ProtectionTypes.html
 		String world = tgui.target.getWorld().getName(); // this isn't essentially REQUIRED like blockId, but if it's empty, it will automatically set itself when first opened
 		String owner = tgui.owner.getText(); // this player will have the ability to remove it !!
 		String password = tgui.password.getText(); // only applies if type = ProtectionTypes.PASSWORD
 		int x = tgui.target.getX();
 		int y = tgui.target.getY();
 		int z = tgui.target.getZ();
 		if(tgui.lwpassword.isSelected()) {
 			type = Protection.Type.PASSWORD;
 			if(tgui.password.getText().equals("********") || tgui.password.getText().equals("")) {
 				player.sendNotification("Invalid Password", "Please input a password!", Material.FIRE);
 				return false;
 			}
 		}else if(tgui.lwprivate.isSelected()) {
 			type = Protection.Type.PRIVATE;
 		}else if(tgui.lwpublic.isSelected()) {
 			type = Protection.Type.PUBLIC;
 		}
 
 		// now create the protection
 		Protection tprotection = plugin.lwc.getPhysicalDatabase().registerProtection(blockId, type, world, owner, StringUtil.encrypt(password), x, y, z);
 		
 		//Add all the admins in the list
 		String[] padmins = tgui.admins.getText().split(",");
 		for(String tadmin : padmins) {
 			String[] stadmin = tadmin.trim().split(":");
 			if(stadmin.length > 1) {
 				Permission ar = new Permission();
 				if(stadmin[0].trim().equalsIgnoreCase("g")) {
 					ar.setType(Permission.Type.GROUP);
 					ar.setAccess(Permission.Access.ADMIN);
 					ar.setName(stadmin[1].trim());
 					tprotection.addPermission(ar);
 				}else if(stadmin[0].trim().equalsIgnoreCase("l")) {
 					ar.setType(Permission.Type.LIST);
 					ar.setAccess(Permission.Access.ADMIN);
 					ar.setName(stadmin[1].trim());
 					tprotection.addPermission(ar);
 				}
 			}else {
 				Permission ar = new Permission();
 				ar.setType(Permission.Type.PLAYER);
 				ar.setAccess(Permission.Access.ADMIN);
 				ar.setName(tadmin.trim());
 				tprotection.addPermission(ar);
 			}
 		}
 		
 		//Add all the users to the list
 		String[] pusers = tgui.users.getText().split(",");
 		for(String tadmin : pusers) {
 			String[] stadmin = tadmin.trim().split(":");
 			if(stadmin.length > 1) {
 				Permission ar = new Permission();
 				if(stadmin[0].trim().equalsIgnoreCase("g")) {
 					ar.setType(Permission.Type.GROUP);
 					ar.setAccess(Permission.Access.PLAYER);
 					ar.setName(stadmin[1].trim());
 					tprotection.addPermission(ar);
 				}else if(stadmin[0].trim().equalsIgnoreCase("l")) {
 					ar.setType(Permission.Type.LIST);
 					ar.setAccess(Permission.Access.PLAYER);
 					ar.setName(stadmin[1].trim());
 					tprotection.addPermission(ar);
 				}
 			}else {
 				Permission ar = new Permission();
 				ar.setType(Permission.Type.PLAYER);
 				ar.setAccess(Permission.Access.PLAYER);
 				ar.setName(tadmin.trim());
 				tprotection.addPermission(ar);
 			}
 		}
 		tprotection.save();
 		return true;
 	}
 	
 	static Material getDisplayItem(Material mat) {
 		if(mat == Material.SIGN_POST) {
 			return Material.SIGN;
 		}else if(mat == Material.WALL_SIGN) {
 			return Material.SIGN;
 		}else if(mat == Material.WOODEN_DOOR) {
 			return Material.WOOD_DOOR;
 		}else if(mat == Material.CHEST) {
 			return Material.LOCKED_CHEST;
 		}else {
 			return mat;
 		}
 	}
 
 }
