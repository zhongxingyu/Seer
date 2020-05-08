 package tux2.spoutlwc;
 
 import com.griefcraft.model.Permission;
 import com.griefcraft.model.Protection;
 import java.util.List;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.inventory.ItemStack;
 import org.getspout.spoutapi.gui.*;
 import org.getspout.spoutapi.player.SpoutPlayer;
 
 public class PlayerLwcGUI {
 	
 	//All the Text boxes.
 	GenericTextField owner = new GenericTextField();
 	GenericTextField admins = new GenericTextField();
 	GenericTextField users = new GenericTextField();
 	GenericTextField password = new GenericTextField();
 	GenericRadioButton lwpassword = new GenericRadioButton("Password Lock");
 	GenericRadioButton lwprivate = new GenericRadioButton("Private Lock");
 	GenericRadioButton lwpublic = new GenericRadioButton("Public Lock");
 	Protection protection;
 	Block target;
 	
 	public PlayerLwcGUI(SpoutLWC plugin, Protection protection, SpoutPlayer splayer) {
 		this(plugin, protection, splayer, protection.getBlock());
 	}
 	
 	public PlayerLwcGUI(SpoutLWC plugin, Protection protection, SpoutPlayer splayer, Block target) {
 		this.protection = protection;
 		this.target = target;
 		//Let's create a new popup
 		GenericPopup ppane = new GenericPopup();
 		//Add the label at the top of the window
 		GenericLabel label = new GenericLabel("LWC Locking System");
 		label.setTextColor(new Color(0, 200, 0)); //This makes the label green.
 		label.setAlign(WidgetAnchor.TOP_CENTER).setAnchor(WidgetAnchor.TOP_CENTER); //This puts the label at top center and align the text correctly.
 		label.shiftYPos(5);
 		splayer.getClipboardText();
 		ppane.attachWidget(plugin, label);
 		
 		int y = 30, height = 15;
 		int x = 170;
 		if(target.getType() == Material.CHEST || target.getType() == Material.FURNACE || target.getType() == Material.BURNING_FURNACE) {
 			y = 50;
 		}
 		GenericItemWidget chesticon = new GenericItemWidget(new ItemStack(LWCScreenListener.getDisplayItem(target.getType())));
 		chesticon.setX(x + 2 * height).setY(y);
 		chesticon.setHeight(height * 2).setWidth(height * 2).setDepth(30);
 		chesticon.setTooltip("Lock that chest!");
 		ppane.attachWidget(plugin, chesticon);
 		
 		//Create the owner label
 		GenericLabel olabel = new GenericLabel("Owner:");
 		//Set it's position on the screen (in pixels)
 		olabel.setX(50).setY(100);
 		//set it's position
 		olabel.setHeight(20);
 		//add the label to the popup
 		ppane.attachWidget(plugin, olabel);
 		//set properties on the Owner text box.
 		owner.setX(95).setY(95);
 		owner.setWidth(80).setHeight(15);
 		//Add the Owner Text box to the popup.
 		ppane.attachWidget(plugin, owner);
 		GenericLabel plabel = new GenericLabel("Password:");
 		plabel.setX(220).setY(100);
 		plabel.setHeight(20);
 		ppane.attachWidget(plugin, plabel);
 		password.setX(275).setY(95);
 		password.setWidth(80).setHeight(15);
 		ppane.attachWidget(plugin, password);
 		lwpassword.setX(50).setY(115);
 		lwpassword.setWidth(80).setHeight(20);
 		lwpassword.setGroup(1);
 		lwpassword.setColor(new Color(0, 200, 0));
 		ppane.attachWidget(plugin, lwpassword);
 		lwprivate.setX(180).setY(115);
 		lwprivate.setWidth(80).setHeight(20);
 		lwprivate.setGroup(1);
 		lwprivate.setColor(new Color(0, 200, 0));
 		ppane.attachWidget(plugin, lwprivate);
 		lwpublic.setX(300).setY(115);
 		lwpublic.setWidth(80).setHeight(20);
 		lwpublic.setGroup(1);
 		lwpublic.setColor(new Color(0, 200, 0));
 		ppane.attachWidget(plugin, lwpublic);
 		GenericLabel alabel = new GenericLabel("Admins:");
 		alabel.setX(20).setY(150);
 		alabel.setHeight(20);
 		ppane.attachWidget(plugin, alabel);
 		admins.setX(65).setY(145);
 		admins.setWidth(340).setHeight(15);
 		admins.setMaximumCharacters(500);
 		ppane.attachWidget(plugin, admins);
 		GenericLabel ulabel = new GenericLabel("Users:");
 		ulabel.setX(20).setY(180);
 		ulabel.setHeight(20);
 		ppane.attachWidget(plugin, ulabel);
 		users.setX(65).setY(175);
 		users.setWidth(340).setHeight(15);
 		users.setMaximumCharacters(500);
 		ppane.attachWidget(plugin, users);
		GenericButton savebutton = new GenericButton("Save");
 		GenericButton deletebutton = new GenericButton("Delete");
 		GenericButton cancelbutton = new GenericButton("Cancel");
 		//closebutton.setX(160).setY(210);
 		savebutton.setWidth(80).setHeight(20);
 		savebutton.setX(84).setY(200);
 		savebutton.setColor(new Color(0, 150, 0));
 		deletebutton.setWidth(80).setHeight(20);
 		deletebutton.setX(174).setY(200);
 		deletebutton.setColor(new Color(150, 0, 0));
 		cancelbutton.setWidth(80).setHeight(20);
 		cancelbutton.setX(264).setY(200);
 		cancelbutton.setColor(new Color(150, 160, 0));
 		ppane.attachWidget(plugin, savebutton);
 		ppane.attachWidget(plugin, deletebutton);
 		ppane.attachWidget(plugin, cancelbutton);
 		if(protection != null) {
 			owner.setText(protection.getOwner());
 			if(!protection.getOwner().equalsIgnoreCase(splayer.getName()) && !plugin.lwc.hasPermission(splayer, "lwc.admin")) {
 				owner.setEnabled(false);
 			}
 			List<Permission> rights = protection.getPermissions();
 			String sadmins = "";
 			String susers = "";
 			for(Permission right : rights) {
 				if(right.getType() == Permission.Type.PLAYER) {
 					if(right.getAccess() == Permission.Access.ADMIN) {
 						//right.
 						if(!sadmins.equals("")) {
 							sadmins = sadmins + ", ";
 						}
 						sadmins = sadmins + right.getName();
 					}else if(right.getAccess() == Permission.Access.PLAYER) {
 						if(!susers.equals("")) {
 							susers = susers + ", ";
 						}
 						susers = susers + right.getName();
 					}
 				}else if(right.getType() == Permission.Type.GROUP) {
 					if(right.getAccess() == Permission.Access.ADMIN) {
 						if(!sadmins.equals("")) {
 							sadmins = sadmins + ", ";
 						}
 						sadmins = sadmins + "g:" + right.getName();
 					}else if(right.getAccess() == Permission.Access.PLAYER) {
 						if(!susers.equals("")) {
 							susers = susers + ", ";
 						}
 						susers = susers + "g:" + right.getName();
 					}
 				}else if(right.getType() == Permission.Type.LIST) {
 					if(right.getAccess() == Permission.Access.ADMIN) {
 						//right.
 						if(!sadmins.equals("")) {
 							sadmins = sadmins + ", ";
 						}
 						sadmins = sadmins + "l:" + right.getName();
 					}else if(right.getAccess() == Permission.Access.PLAYER) {
 						if(!susers.equals("")) {
 							susers = susers + ", ";
 						}
 						susers = susers + "l:" + right.getName();
 					}
 				}
 			}
 			if(protection.getType() == Protection.Type.PASSWORD) {
 				password.setText("********");
 				lwpassword.setSelected(true);
 			}else if(protection.getType() == Protection.Type.PRIVATE) {
 				lwprivate.setSelected(true);
 			}else if(protection.getType() == Protection.Type.PUBLIC) {
 				lwpublic.setSelected(true);
 			}
 			admins.setText(sadmins);
 			users.setText(susers);
 			if(!plugin.lwc.hasPermission(splayer, "lwc.remove")) {
 				deletebutton.setEnabled(false);
 			}
 		}else {
 			deletebutton.setEnabled(false);
 			owner.setText(splayer.getName());
//			lwprivate.setSelected(true);
 		}
 		splayer.getMainScreen().attachPopupScreen(ppane);
 	}
 
 }
