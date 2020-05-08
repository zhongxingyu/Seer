 package com.almuramc.digilock;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.UUID;
 
 import com.almuramc.digilock.gui.LockButton;
 import com.almuramc.digilock.util.BlockTools;
 import com.almuramc.digilock.util.Messages;
 import com.almuramc.digilock.util.Permissions;
 import com.bekvon.bukkit.residence.Residence;
 import com.bekvon.bukkit.residence.protection.ClaimedResidence;
 
 import org.getspout.spoutapi.block.SpoutBlock;
 import org.getspout.spoutapi.gui.GenericLabel;
 import org.getspout.spoutapi.gui.GenericPopup;
 import org.getspout.spoutapi.gui.GenericTextField;
 import org.getspout.spoutapi.gui.GenericTexture;
 import org.getspout.spoutapi.gui.PopupScreen;
 import org.getspout.spoutapi.gui.RenderPriority;
 import org.getspout.spoutapi.gui.WidgetAnchor;
 import org.getspout.spoutapi.player.SpoutPlayer;
 
 public class LockCore {
 	public SpoutBlock sBlock;
 	public String pincode;
 	public String owner;
 	public int closetimer;
 	public String coOwners;
 	public String users;
 	public int typeId;
 	public String connectedTo;
 	public int useCost;
 	private Digilock plugin;
 
 	/**
 	 * Constructs a new LockCore
 	 * @param block
 	 * @param pincode
 	 * @param owner
 	 * @param closetimer
 	 * @param coowners
 	 * @param users
 	 * @param typeId
 	 * @param connectedTo
 	 * @param useCost
 	 */
 	public LockCore(Digilock plugin, SpoutBlock block, String pincode, String owner, int closetimer,
 			String coowners, String users, int typeId, String connectedTo,
 			int useCost) {
 		this.plugin = plugin;
 		this.sBlock = block;
 		this.pincode = pincode;
 		this.owner = owner;
 		this.closetimer = closetimer;
 		this.coOwners = coowners;
 		this.users = users;
 		this.typeId = typeId;
 		this.connectedTo = connectedTo;
 		this.useCost = useCost;
 	}
 
 	/**
 	 * Saves the lock to the database.
 	 * @param sPlayer     the player who is interactions with the lock.
 	 * @param block       The block of the lock.
 	 * @param pincode     of the lock. "fingerprint" or "" or a 10 digits code.
 	 * @param owner       is the owner of the lock
 	 * @param closetimer  is number of seconds before the door closes.
 	 * @param coowners    is the list of co - owners of the lock.
 	 * @param typeId      is the type of the block.
 	 * @param connectedTo - not used yet.
 	 * @param useCost     is the cost to use the block.
 	 */
 	public static void SaveDigiLock(SpoutPlayer sPlayer, SpoutBlock block,
 			String pincode, String owner, Integer closetimer, String coowners,
 			String users, int typeId, String connectedTo, int useCost) {
 		String query;
 		boolean createlock = true;
 		boolean newLock = true;
 		double cost = Digilock.getConf().getLockCost();
 		block = BlockTools.getDigiLockBlock(block);
 
 		if (Digilock.getHooks().isResidencyAvailable()) {
 			ClaimedResidence res = Residence.getResidenceManager().getByLoc(block.getLocation());
 			boolean canLock = true;
 
 			if (res != null) {
 				canLock = res.getPermissions().playerHas(sPlayer.getName(), "lockable", true);
 			}
 
 			if (!canLock) {
 				sPlayer.sendMessage("Residence is currently restricting your Digilock Abilities.");
 				return;
 			}
 		}
 
 		if (BlockTools.isLocked(block)) {
 			newLock = false;
 			query = "UPDATE " + Digilock.getHandler().getTableName() + " SET pincode='" + pincode
 					+ "', owner='" + owner + "', closetimer=" + closetimer
 					+ " , coowners='" + coowners + "' , users='" + users
 					+ "', typeid=" + typeId + ", connectedto='" + connectedTo
 					+ "', usecost=" + useCost + " WHERE x = " + block.getX() + " AND y = " + block.getY()
 					+ " AND z = " + block.getZ() + " AND world='"
 					+ block.getWorld().getName() + "';";
 		} else {
 			// NEW DIGILOCK
 			query = "INSERT INTO " + Digilock.getHandler().getTableName()
 					+ " (pincode, owner, closetimer, "
 					+ "x, y, z, world, coowners, users, "
 					+ "typeid, connectedto, usecost) VALUES ('" + pincode
 					+ "', '" + owner + "', " + closetimer + ", " + block.getX()
 					+ ", " + block.getY() + ", " + block.getZ() + ", '"
 					+ block.getWorld().getName() + "', '" + coowners + "', '"
 					+ users + "', " + block.getTypeId() + ", '" + connectedTo
 					+ "', " + useCost + " );";
 			if (Digilock.getConf().useEconomy()) {
 				if (Digilock.getHooks().getEconHook().hasAccount(sPlayer.getName()) && cost > 0) {
 					if (Digilock.getHooks().getEconHook().has(sPlayer.getName(), cost)) {
 						Digilock.getHooks().getEconHook().withdrawPlayer(sPlayer.getName(), cost);
 						sPlayer.sendMessage("Your account ("
 								+ Digilock.getHooks().getEconHook().getBalance(sPlayer.getName())
 								+ ") has been deducted "
 								+ Digilock.getHooks().getEconHook().format(cost) + ".");
 					} else {
 						sPlayer.sendMessage("You dont have enough money ("
 								+ Digilock.getHooks().getEconHook().getBalance(sPlayer.getName())
 								+ "). Cost is:"
 								+ Digilock.getHooks().getEconHook().format(cost));
 						createlock = false;
 					}
 				}
 			}
 		}
 		if (createlock) {
 			if (Digilock.getConf().getSQLType().equals("MYSQL")) {
 				Digilock.getHandler().getMySQLHandler().query(query);
 			} else {
 				Digilock.getHandler().getSqliteHandler().query(query);
 
 				Messages.sendNotification(sPlayer, "SQLite Command Ran.");
 			}
 			if (newLock) {
 				Messages.sendNotification(sPlayer, "DigiLock created.");
 			} else {
 				Messages.sendNotification(sPlayer, "DigiLock updated.");
 			}
 		} else {
 			sPlayer.sendMessage("You dont have enough money. Cost is:" + cost);
 		}
 	}
 
 	/**
 	 * Checks if sPlayer is co Owner of the lock.
 	 * @param sPlayer
 	 * @return true or false
 	 */
 	public boolean isCoowner(SpoutPlayer sPlayer) {
 		if (coOwners.toLowerCase().contains(sPlayer.getName().toLowerCase())
 				|| coOwners.toLowerCase().contains("everyone")) {
 			return true;
 		}
 		return false;
 	}
 
 	public boolean isUser(SpoutPlayer sPlayer) {
 		if (users.toLowerCase().contains(sPlayer.getName().toLowerCase())
 				|| users.toLowerCase().contains("everyone")) {
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Checks if sPlayer is Owner of the lock.
 	 * @param sPlayer
 	 * @return true or false
 	 */
 	public boolean isOwner(SpoutPlayer sPlayer) {
 		if (owner.toLowerCase().equals(sPlayer.getName().toLowerCase())) {
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Checks if sPlayer is Owner of the lock placed on sBlock.
 	 * @param sPlayer
 	 * @return true or false
 	 */
 	public static boolean isOwner(SpoutPlayer sPlayer, SpoutBlock sBlock) {
 		if (sBlock != null) {
 			if (BlockTools.loadDigiLock(sBlock).getOwner().toLowerCase()
 					.equals(sPlayer.getName().toLowerCase())) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public void addCoowner(String name) {
 		this.coOwners = coOwners.concat("," + name);
 	}
 
 	public boolean removeCoowner(String name) {
 		if (coOwners.toLowerCase().contains(name.toLowerCase())) {
 			this.coOwners = coOwners.replace(name, "");
 			this.coOwners = coOwners.replace(",,", ",");
 			return true;
 		}
 		return false;
 	}
 
 	public void addUser(String name) {
 		this.users = users.concat("," + name);
 	}
 
 	public boolean removeUser(String name) {
 		if (users.toLowerCase().contains(name.toLowerCase())) {
 			this.users = users.replace(name, "");
 			this.users = users.replace(",,", ",");
 			return true;
 		}
 		return false;
 	}
 
 	public String getPincode() {
 		return pincode;
 	}
 
 	public String getOwner() {
 		return owner;
 	}
 
 	public int getClosetimer() {
 		return closetimer;
 	}
 
 	public String getCoOwners() {
 		return coOwners;
 	}
 
 	public String getUsers() {
 		return users;
 	}
 
 	public SpoutBlock getBlock() {
 		return sBlock;
 	}
 
 	public int getUseCost() {
 		return useCost;
 	}
 
 	public String getConnectedTo() {
 		return connectedTo;
 	}
 
 	public void setPincode(String pincode) {
 		this.pincode = pincode;
 	}
 
 	public void setBlock(SpoutBlock block) {
 		this.sBlock = block;
 	}
 
 	public void setOwner(String owner) {
 		this.owner = owner;
 	}
 
 	public void setClosetimer(int closetimer) {
 		this.closetimer = closetimer;
 	}
 
 	public void setCoowners(String coowners) {
 		this.coOwners = coowners;
 	}
 
 	public void setUsers(String users) {
 		this.users = users;
 	}
 
 	public void setUseCost(int useCost) {
 		this.useCost = useCost;
 	}
 
 	public void setConnectedTo(String connectedTo) {
 		this.connectedTo = connectedTo;
 	}
 
 	public void setDigiLock(SpoutBlock block, String pincode, String owner,
 			int closetimer, String coowners, String users, String connectedTo,
 			int useCost) {
 		this.sBlock = block;
 		this.pincode = pincode;
 		this.owner = owner;
 		this.closetimer = closetimer;
 		this.coOwners = coowners;
 		this.users = users;
 		this.typeId = block.getTypeId();
 		this.connectedTo = connectedTo;
 		this.useCost = useCost;
 	}
 
 	public void RemoveDigiLock(SpoutPlayer sPlayer) {
 		boolean deletelock = true;
 		if (Digilock.getConf().useEconomy()) {
 			if (Digilock.getHooks().getEconHook().hasAccount(sPlayer.getName())) {
 				if (Digilock.getHooks().getEconHook().has(sPlayer.getName(),
 						Digilock.getConf().getDestroyCost())
 						|| Digilock.getConf().getDestroyCost() < 0) {
 					Digilock.getHooks().getEconHook().withdrawPlayer(sPlayer.getName(),
 							Digilock.getConf().getDestroyCost());
 					sPlayer.sendMessage("Your account ("
 							+ Digilock.getHooks().getEconHook().getBalance(sPlayer.getName())
 							+ ") has been deducted "
 							+ Digilock.getHooks().getEconHook().format(Digilock.getConf().getDestroyCost())
 							+ ".");
 				} else {
 					sPlayer.sendMessage("You dont have enough money ("
 							+ Digilock.getHooks().getEconHook().getBalance(sPlayer.getName())
 							+ "). Cost is:"
 							+ Digilock.getHooks().getEconHook().format(Digilock.getConf().getDestroyCost()));
 					deletelock = false;
 				}
 			}
 		}
 		String query = "DELETE FROM " + Digilock.getHandler().getTableName() + " WHERE (x = "
 				+ sBlock.getX() + " AND y = " + sBlock.getY() + " AND z = "
 				+ sBlock.getZ() + " AND world='" + sBlock.getWorld().getName()
 				+ "');";
 		if (deletelock) {
 			if (Digilock.getConf().getSQLType().equals("MYSQL")) {
 				Digilock.getHandler().getMySQLHandler().query(query);
 			} else { // SQLLITE
 				Digilock.getHandler().getSqliteHandler().query(query);
 			}
 			Messages.sendNotification(sPlayer, "DigiLock removed.");
 		} else {
 			Messages.sendNotification(sPlayer, "You need more money ("
 					+ Digilock.getConf().getDestroyCost() + ")");
 		}
 	}
 
 	public SpoutBlock getNextLockedBlock(SpoutPlayer sPlayer, SpoutBlock sBlock) {
 		for (int i = -1; i < 1 + 1; i++) {
 			for (int j = -1; j < +1; j++) {
 				for (int k = -1; k < +1; k++) {
 					if (!(i == 0 && j == 0 && k == 0)) {
 						SpoutBlock sb = sBlock.getRelative(i, j, k);
 						if (
 								// BlockTools.isLockable(sb)
 								BlockTools.isLocked(sb)
 								&& (BlockTools.isDoubleDoor(sb)
 										|| BlockTools.isDoor(sb) || BlockTools.isPiston(sb)
 										|| BlockTools.isTrapdoor(sb)
 										|| BlockTools.isDispenser(sb))) {
 							return sb;
 						}
 					}
 				}
 			}
 		}
 		return null;
 	}
 
 	public SpoutBlock getNextLockableBlock(SpoutPlayer sPlayer,
 			SpoutBlock sBlock) {
 		for (int i = -1; i < 1 + 1; i++) {
 			for (int j = -1; j < +1; j++) {
 				for (int k = -1; k < +1; k++) {
 					if (!(i == 0 && j == 0 && k == 0)) {
 						SpoutBlock sb = sBlock.getRelative(i, j, k);
 						if (BlockTools.isLockable(sb)
 								// BlockTools.isLocked(sb)
 								&& (BlockTools.isDoubleDoor(sb)
 										|| BlockTools.isDoor(sb) || BlockTools.isPiston(sb)
 										|| BlockTools.isTrapdoor(sb) || BlockTools.isDispenser(sb))) {
 							return sb;
 						}
 					}
 				}
 			}
 		}
 		return null;
 	}
 
 	// *******************************************************
 	//
 	// GUI for the lock
 	//
 	// *******************************************************
 	// USERDATA lock
 	public static Map<Integer, PopupScreen> popupScreen = new HashMap<Integer, PopupScreen>();
 	public static Map<Integer, Integer> userno = new HashMap<Integer, Integer>();
 	public static Map<Integer, GenericTextField> pincodeGUI = new HashMap<Integer, GenericTextField>();
 	public static Map<Integer, GenericTextField> ownerGUI = new HashMap<Integer, GenericTextField>();
 	public static Map<Integer, GenericTextField> closetimerGUI = new HashMap<Integer, GenericTextField>();
 	public static Map<Integer, GenericTextField> coOwnersGUI = new HashMap<Integer, GenericTextField>();
 	public static Map<Integer, GenericTextField> usersGUI = new HashMap<Integer, GenericTextField>();
 	public static Map<Integer, GenericTextField> useCostGUI = new HashMap<Integer, GenericTextField>();
 	public static Map<Integer, GenericTextField> connectedToGUI = new HashMap<Integer, GenericTextField>();
 	public static Map<Integer, SpoutBlock> clickedBlock = new HashMap<Integer, SpoutBlock>();
 	// Buttons for lock
 	public static HashMap<UUID, String> BITDigiLockButtons = new HashMap<UUID, String>();
 
 	/**
 	 * @param sPlayer
 	 */
 	public static void cleanupPopupScreen(SpoutPlayer sPlayer) {		
 		int playerId = sPlayer.getEntityId();		
 		if (popupScreen.containsKey(playerId)) {			
 			popupScreen.get(playerId).removeWidgets(Digilock.getInstance());
 			popupScreen.get(playerId).setDirty(true);
 			sPlayer.getMainScreen().removeWidgets(Digilock.getInstance());
 			popupScreen.get(playerId).close();
 			clickedBlock.remove(sPlayer.getEntityId());			
 		}
 	}
 
 	//TODO put this in config
 	private static String getTextureUrl(SpoutBlock sBlock) {
 		switch (sBlock.getTypeId()) {
 		case 23:
 			return "http://www.almuramc.com/images/dispenser.png";
 			// Dispenser - looks nice.
 
 		case 54:
 			return "http://www.almuramc.com/images/singlechest.png";
 			// Wooden Chest
 
 		case 61:
 			return "http://www.almuramc.com/images/furnace.png";
 			// Furnace - looks nice.
 
 		case 62:
 			return "http://www.almuramc.com/images/furnace.png";
 			// Burning Furnace
 
 		case 64:
 			// return 324
 			// Wooden door
 			return "http://www.almuramc.com/images/woodendoor.png";
 
 		case 69:
 			// return 69; // Lever
 			return "http://www.almuramc.com/images/lever.png";
 
 		case 71:
 			// return 330; // Iron door
 			return "http://www.almuramc.com/images/steeldoor.png";
 
 		case 77:
 			// return 77; // Stone button
 			return "http://www.almuramc.com/images/button.png";
 
 		case 96:
 			return "http://www.almuramc.com/images/Trapdoor.png";
 			// Trap_door
 		default:
 			return "http://www.almuramc.com/images/noid.png";
 		}
 	}
 
 	/**
 	 * @param sBlock
 	 * @return
 	 */
 	public static int getPincodeBlock(SpoutBlock sBlock) {
 		switch (sBlock.getTypeId()) {
 		case 23:
 			return 23; // Dispenser - looks nice.
 		case 47:
 			return 47; // Bookshelf - looks nice.
 		case 54:
 			return 54; // Chest - looks nice.
 		case 61:
 			return 61; // Furnace - looks nice.
 		case 62:
 			return 62; // Burning Furnace
 		case 63:
 			return 95; // SIGN_POST
 		case 64:
 			return 324; // Wooden door
 			// return 95;
 		case 68:
 			return 68;
 		case 69:
 			return 69; // Lever
 			// return 95;
 		case 71:
 			return 330; // Iron door
 			// return 95;
 		case 77:
 			return 77; // Stone button
 			// return 95;
 		case 96:
 			return 95; // Trap_door
 		case 107:
 			return 95; // FENCEGATE
 		}
 		return 95;
 	}
 
 	/**
 	 *
 	 * @param sBlock
 	 * @return
 	 */
 
 	// ***************************************************************
 	//
 	// getPincode: Open GenericPopup and ask for pincode before to
 	// unlock the inventory.
 	//
 	// ***************************************************************
 
 	/**
 	 * Open Generic PopupScreen and ask for the pincode.
 	 * @param sPlayer
 	 * @param sBlock
 	 * @author Gabriel333 / Rocologo
 	 */
 	public static void getPincode(SpoutPlayer sPlayer, SpoutBlock sBlock) {   // y = up/down  x = left/right
 		int y = 88; int height = 15; int width = 50;
 		int x = 204;
 		int id = sPlayer.getEntityId();
 
 		cleanupPopupScreen(sPlayer);
 		addUserData(id);
 		clickedBlock.put(id, sBlock);
 
 		GenericTexture border = new GenericTexture();
 		border.setUrl(getTextureUrl(sBlock));
 		border.setAnchor(WidgetAnchor.CENTER_CENTER);
 		border.shiftXPos(-105).shiftYPos(-85);
 		border.setPriority(RenderPriority.High);
 		border.setWidth(263).setHeight(150);
 		//popupScreen.get(Integer.valueOf(id)).attachWidget(Digilock.getInstance(), border);
 		popupScreen.get(id).attachWidget(Digilock.getInstance(), border);		
 		y += 3 * height;
 
		GenericTextField pincodeGUI = new GenericTextField();
		pincodeGUI.setText("");
		pincodeGUI.setTooltip("Enter the pincode and press unlock.");
		pincodeGUI.setCursorPosition(1).setMaximumCharacters(20);
		pincodeGUI.setAnchor(WidgetAnchor.CENTER_CENTER);
		pincodeGUI.shiftXPos(-50).shiftYPos(43);
		pincodeGUI.setHeight(height).setWidth(80);
		pincodeGUI.setMargin(0);
		pincodeGUI.setPasswordField(true);
		pincodeGUI.setFocus(true);
		popupScreen.get(id).attachWidget(Digilock.getInstance(), pincodeGUI);
 		y += height;
 
 		LockButton unlockButton = new LockButton("Unlock");
 		unlockButton.setAnchor(WidgetAnchor.CENTER_CENTER);
 		unlockButton.shiftXPos(55).shiftYPos(42);
 		unlockButton.setHeight(18).setWidth(40);
 		unlockButton.setAuto(true);
 		BITDigiLockButtons.put(unlockButton.getId(), "getPincodeUnlock");
 		popupScreen.get(id).attachWidget(Digilock.getInstance(), unlockButton);
 
 		LockButton cancelButton = new LockButton("Cancel");
 		cancelButton.setAnchor(WidgetAnchor.CENTER_CENTER);
 		cancelButton.shiftXPos(100).shiftYPos(42);
 		cancelButton.setHeight(18).setWidth(40);
 		cancelButton.setAuto(true);
 		popupScreen.get(id).attachWidget(Digilock.getInstance(), cancelButton);
 		BITDigiLockButtons.put(cancelButton.getId(), "getPincodeCancel");
 
 		popupScreen.get(id).setTransparent(true);
 		sPlayer.getMainScreen().attachPopupScreen(popupScreen.get(id));
 	}
 
 	/**
 	 * setPincode - Open GenericPopup and enter a pincode to lock the inventory.
 	 * @param sPlayer
 	 * @param sBlock
 	 */
 	public static void setPincode(SpoutPlayer sPlayer, SpoutBlock sBlock) {
 		int id = sPlayer.getEntityId();
 		int height = 9;
 		int x, y, w1, w2, w3, w4;
 		cleanupPopupScreen(sPlayer);
 		addUserData(id);
 		clickedBlock.put(id, sBlock);
 		
 		if (BlockTools.isLocked(sBlock)) {
 			LockCore lock = BlockTools.loadDigiLock(sBlock);
 			pincodeGUI.get(id).setText(lock.getPincode());
 			ownerGUI.get(id).setText(lock.getOwner());
 			coOwnersGUI.get(id).setText(lock.getCoOwners());
 			usersGUI.get(id).setText(lock.getUsers());
 			closetimerGUI.get(id).setText(Integer.toString(lock.getClosetimer()));
 			useCostGUI.get(id).setText(Integer.toString(lock.getUseCost()));
 		} else {
 			pincodeGUI.get(id).setText("");
 			ownerGUI.get(id).setText(sPlayer.getName());
 			coOwnersGUI.get(id).setText("");
 			usersGUI.get(id).setText("");
 			useCostGUI.get(id).setText("0");
 			closetimerGUI.get(id).setText(String.valueOf(Digilock.getConf().getDefaultCloseTimer()));
 		}
 
 		// Digilock Request Lock Interface
 		GenericTexture border = new GenericTexture();
 		border.setUrl("http://www.almuramc.com/images/digilock1.png");		
 		border.setAnchor(WidgetAnchor.CENTER_CENTER);
 		border.setPriority(RenderPriority.High);
 		border.setWidth(350).setHeight(240);		
 		border.shiftXPos(-180).shiftYPos(-128);
 		popupScreen.get(id).attachWidget(Digilock.getInstance(), border);
 		
 		// Password Field
 		pincodeGUI.get(id).setTooltip("Enter/change the pincode...");
 		pincodeGUI.get(id).setCursorPosition(1).setMaximumCharacters(20);
 		pincodeGUI.get(id).setMaximumLines(1);
 		pincodeGUI.get(id).setHeight(14).setWidth(130);
 		pincodeGUI.get(id).setPasswordField(false);
 		pincodeGUI.get(id).setAnchor(WidgetAnchor.CENTER_CENTER);
 		pincodeGUI.get(id).shiftXPos(-65).shiftYPos(-80);
 		pincodeGUI.get(id).setFocus(true);
 		popupScreen.get(id).attachWidget(Digilock.getInstance(), pincodeGUI.get(id));
 				
 		// owner text field
 		ownerGUI.get(id).setTooltip("Owner of the DigiLock");
 		ownerGUI.get(id).setMaximumCharacters(30);
 		ownerGUI.get(id).setMaximumLines(1);
 		ownerGUI.get(id).setHeight(14).setWidth(200);
 		ownerGUI.get(id).setAnchor(WidgetAnchor.CENTER_CENTER);
 		ownerGUI.get(id).shiftXPos(-105).shiftYPos(-50);			
 		popupScreen.get(id).attachWidget(Digilock.getInstance(), ownerGUI.get(id));
 		
 		// list Of Co-Owners		
 		coOwnersGUI.get(id).setMaximumCharacters(1000).setMaximumLines(2);
 		coOwnersGUI.get(id).setTooltip("Co-Owners");
 		coOwnersGUI.get(id).setText(coOwnersGUI.get(id).getText());
 		coOwnersGUI.get(id).setHeight(28).setWidth(200);
 		coOwnersGUI.get(id).setAnchor(WidgetAnchor.CENTER_CENTER);
 		coOwnersGUI.get(id).shiftXPos(-105).shiftYPos(-27);		
 		popupScreen.get(id).attachWidget(Digilock.getInstance(), coOwnersGUI.get(id));
 		
 		// listOfUsers
 		usersGUI.get(id).setMaximumCharacters(1000).setMaximumLines(2);
 		usersGUI.get(id).setTooltip("Users");
 		usersGUI.get(id).setText(usersGUI.get(id).getText());
 		usersGUI.get(id).setHeight(28).setWidth(200);
 		usersGUI.get(id).setAnchor(WidgetAnchor.CENTER_CENTER);
 		usersGUI.get(id).shiftXPos(-105).shiftYPos(10);		
 		popupScreen.get(id).attachWidget(Digilock.getInstance(), usersGUI.get(id));
 		
 		// closetimer
 		closetimerGUI.get(id).setTooltip("Autoclosing time in sec.");
 		closetimerGUI.get(id).setMaximumCharacters(3);		
 		closetimerGUI.get(id).setHeight(14).setWidth(35);
 		closetimerGUI.get(id).setAnchor(WidgetAnchor.CENTER_CENTER);
 		closetimerGUI.get(id).shiftXPos(-100).shiftYPos(58);			
 		popupScreen.get(id).attachWidget(Digilock.getInstance(), closetimerGUI.get(id));
 
 		// useCost Text Field
 		useCostGUI.get(id).setTooltip("This is the cost to use the DigiLock");
 		useCostGUI.get(id).setMaximumCharacters(6);
 		useCostGUI.get(id).setHeight(14).setWidth(40);
 		useCostGUI.get(id).setMaximumCharacters(3);	
 		useCostGUI.get(id).setAnchor(WidgetAnchor.CENTER_CENTER);
 		useCostGUI.get(id).shiftXPos(45).shiftYPos(58);			
 		popupScreen.get(id).attachWidget(Digilock.getInstance(), useCostGUI.get(id));
 		
 		GenericLabel costToCreate = new GenericLabel(" " + String.valueOf(Digilock.getConf().getLockCost()));
 		costToCreate.setHeight(14).setWidth(80);			
 		costToCreate.setAnchor(WidgetAnchor.CENTER_CENTER);
 		costToCreate.shiftXPos(-110).shiftYPos(93);		
 		costToCreate.setTooltip("The cost to create a new DigiLock");
 		popupScreen.get(id).attachWidget(Digilock.getInstance(), costToCreate);
 		
 		// SaveButton
 		LockButton lockButton = new LockButton("Save");		
 		lockButton.setAuto(true);
 		lockButton.setAnchor(WidgetAnchor.CENTER_CENTER);
 		lockButton.setHeight(18).setWidth(40);
 		lockButton.shiftXPos(60).shiftYPos(87);
 		popupScreen.get(id).attachWidget(Digilock.getInstance(), lockButton);
 		BITDigiLockButtons.put(lockButton.getId(), "setPincodeLock");
 
 		// cancelButton
 		LockButton cancelButton2 = new LockButton("Cancel");
 		cancelButton2.setAuto(true);
 		cancelButton2.setAnchor(WidgetAnchor.CENTER_CENTER);
 		cancelButton2.setHeight(18).setWidth(40);
 		cancelButton2.shiftXPos(110).shiftYPos(87);
 		popupScreen.get(id).attachWidget(Digilock.getInstance(), cancelButton2);
 		BITDigiLockButtons.put(cancelButton2.getId(), "setPincodeCancel");
 
 		// removeButton
 		if (BlockTools.isLocked(sBlock)) {
 			LockButton removeButton = new LockButton("Remove");
 			removeButton.setAuto(true);
 			removeButton.setAnchor(WidgetAnchor.CENTER_CENTER);
 			removeButton.setHeight(18).setWidth(45);
 			removeButton.shiftXPos(0).shiftYPos(87);
 			removeButton.setTooltip("Press Remove to delete the lock.");
 			removeButton.setEnabled(true);
 			BITDigiLockButtons.put(removeButton.getId(), "setPincodeRemove");
 			popupScreen.get(id).attachWidget(Digilock.getInstance(), removeButton);
 		}
 
 		// AdminButton
 		if (BlockTools.isLocked(sBlock)
 				&& BlockTools.isChest(sBlock) // Displays only if sBlock=Chest.
 				&& Permissions.hasPerm(sPlayer, "admin",
 						Permissions.NOT_QUIET)) {
 			LockButton adminButton = new LockButton("Open");
 			adminButton.setAuto(true);
 			adminButton.setAnchor(WidgetAnchor.CENTER_CENTER);
 			adminButton.setHeight(18).setWidth(40);
 			adminButton.shiftXPos(-50).shiftYPos(87);
 			adminButton.setTooltip("AOpen Override.");
 			adminButton.setEnabled(true);
 			BITDigiLockButtons.put(adminButton.getId(), "AdminOpen");
 			popupScreen.get(id).attachWidget(Digilock.getInstance(), adminButton);
 		}
 
 		// Open Window		
 		popupScreen.get(id).setTransparent(true).setDirty(true);
 		sPlayer.getMainScreen().attachPopupScreen(popupScreen.get(id));
 	}
 
 	public static void removeUserData(int id) {
 		if (userno.containsKey(id)) {
 			// lock
 			popupScreen.remove(id);
 			pincodeGUI.remove(id);
 			ownerGUI.remove(id);
 			coOwnersGUI.remove(id);
 			usersGUI.remove(id);
 			closetimerGUI.remove(id);
 			useCostGUI.remove(id);
 			connectedToGUI.remove(id);
 			userno.remove(id);
 			clickedBlock.remove(id);
 		}
 	}
 
 	public static void addUserData(int id) {
 		if (userno.containsKey(id)) {
 			// Cleanup is required since its not properly removing key data and causing a issue with the screen.
 			removeUserData(id);
 		}
 		if (!userno.containsKey(id)) {
 			// lock			
 			userno.put(id, new Integer(id));
 			popupScreen.put(id, new GenericPopup());
 			pincodeGUI.put(id, new GenericTextField());
 			ownerGUI.put(id, new GenericTextField());
 			coOwnersGUI.put(id, new GenericTextField());
 			usersGUI.put(id, new GenericTextField());
 			closetimerGUI.put(id, new GenericTextField());
 			useCostGUI.put(id, new GenericTextField());
 			connectedToGUI.put(id, new GenericTextField());
 			clickedBlock.put(id, null);
 		}
 	}
 }
