 package com.github.derwisch.loreLocks;
 
 import java.util.ArrayList;
 import java.util.Random;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.meta.ItemMeta;
 
 public class LockGUI {
 	
 	public static ArrayList<LockGUI> GUIs = new ArrayList<LockGUI>();
 
 	private ItemStack borderFiller; 
 	private ItemStack lockCylinder; 
 	private ItemStack lock;
 	
 	private byte difficulty = 0; 
 	
 	private int currentPos = 0;
 	private int[] lockAppeareance = new int[]{2, 2, 2, 2, 2, 2, 2};
 	private ArrayList<Integer> lockCylinders = new ArrayList<Integer>();
 	
 	public static final String BORDER_FILLER_TITLE = ChatColor.WHITE + "   " + ChatColor.RESET;
 	public static final String LOCK_CYLINDER_TITLE = ChatColor.WHITE + "Lock Cylinder" + ChatColor.RESET;
 	
 	public Inventory TargetInventory;
 	public Inventory LockInventory;
 	public Player Player;
 
 	private int hashCode;
 	
 	public static LockGUI GetGUI(int hash) {
 		for (LockGUI gui : GUIs) {
 			if (gui.hashCode == hash) {
 				return gui;
 			}
 		}
 		return null;
 	}
 	
 	public LockGUI(Player player, Inventory inventory, ItemStack lock, byte difficulty) {
 		
 		ItemMeta lockMeta = lock.getItemMeta();
 		
 		this.Player = player;
 		this.LockInventory = Bukkit.createInventory(player, 3 * 9, lockMeta.getDisplayName());
 		this.TargetInventory = inventory;
 		this.lock = lock;
 		this.difficulty = difficulty;
 
 		Random rand = new Random();
 		
 		for (int i = 0; i < difficulty + 2; i++) {
     		lockAppeareance[i] = rand.nextInt(1000) % 2;
     	}
 
 		for (int i = 0; i < difficulty + 2; i++) {
 			int i2 = rand.nextInt(difficulty + 2) + 1;
 			while (lockCylinders.contains(i2)) {
 				i2 = rand.nextInt(difficulty + 2) + 1;
 			} 
 			lockCylinders.add(i2);
     	}
 		
 		updateInventory();
 		
 		GUIs.add(this);
 	}
 
 	private void updateInventory() {
 		LockInventory.clear();
 		borderFiller = new ItemStack(Material.IRON_FENCE);
 		lockCylinder = new ItemStack(Material.IRON_BLOCK);
 
     	ItemMeta borderFillerMeta = borderFiller.getItemMeta();
     	ItemMeta lockCylinderMeta = lockCylinder.getItemMeta();
     	
     	borderFillerMeta.setDisplayName(BORDER_FILLER_TITLE);
     	lockCylinderMeta.setDisplayName(LOCK_CYLINDER_TITLE);
 
     	borderFiller.setItemMeta(borderFillerMeta);
     	lockCylinder.setItemMeta(lockCylinderMeta);	
     	
 		
     	LockInventory.setItem(0, borderFiller.clone());
     	for (int i = 0; i < 7; i++) {
         	if (lockAppeareance[i] == 1 || lockAppeareance[i] == 2 ) {
             	LockInventory.setItem(i + 1, borderFiller.clone());
         	}
     	}
     	LockInventory.setItem(8, borderFiller.clone());
 
     	updatePlayerLockCounter();
 		
     	int tmpPos = 0;
     	for (int i : lockCylinders) {
     		if (tmpPos < currentPos) {
     			if (lockAppeareance[i - 1] == 1) {
 					LockInventory.setItem(i + 18, lockCylinder.clone());
 				} else {
 					LockInventory.setItem(i + 0, lockCylinder.clone());
 				}
     		} else {
     			if (lockAppeareance[i - 1] != 2 ) {
                 	LockInventory.setItem(i + 9, lockCylinder.clone());	
             	}
 			}
     		tmpPos++;
     	}
     	
     	LockInventory.setItem(17, lock.clone());
 
     	LockInventory.setItem(18, borderFiller.clone());
     	for (int i = 0; i < 7; i++) {
         	if (lockAppeareance[i] == 0 || lockAppeareance[i] == 2 ) {
             	LockInventory.setItem(i + 19, borderFiller.clone());
         	}
     	}
     	LockInventory.setItem(26, borderFiller.clone());
 	}
 	
 	private ItemStack getLockPickCounter() {
 		int lockPickCount = getPlayerLockPicks();
     	ItemStack lockPicks = LoreLocks.instance.LockPickRecipe.getResult().clone();
     	ItemMeta lockPicksMeta = lockPicks.getItemMeta();
     	if (lockPickCount < 10) {
     		lockPicksMeta.setDisplayName(ChatColor.RED.toString() + lockPickCount + "x " + Settings.LockPickName + ChatColor.RESET);
     	} else {
     		lockPicksMeta.setDisplayName(ChatColor.WHITE.toString() + lockPickCount + "x " + Settings.LockPickName + ChatColor.RESET);
     	}
     	lockPicks.setItemMeta(lockPicksMeta);
 		return lockPicks;
 	}
 
 	private int getPlayerLockPicks() {
 		int lockPickCount = 0;
     	for (ItemStack stack : Player.getInventory().getContents()) {
     		if (stack != null && LoreLocks.instance.LockPickRecipe.getResult().getItemMeta().getDisplayName().equals(stack.getItemMeta().getDisplayName())) {
     			lockPickCount += stack.getAmount();
     		}
     	}
 		return lockPickCount;
 	}
 
 	public void updatePlayerLockCounter() {
     	LockInventory.setItem(9, getLockPickCounter());
 	}
 	
 	public void Click(int slot, boolean r, boolean s) {
 		ItemStack clickStack = LockInventory.getItem(slot);
 		if (clickStack == null) {
 			return;
 		}
 		ItemMeta clickStackMeta = clickStack.getItemMeta();
 		if (clickStackMeta.getDisplayName() != null && !clickStackMeta.getDisplayName().equals(LOCK_CYLINDER_TITLE)) {
 			return;
 		}
 
 		int clickedCylinder = slot - 9;
 		
 		if (lockCylinders.get(currentPos) == clickedCylinder) {
 			currentPos++;
 			updateInventory();
 		} else {
 			if (Math.random() <= Settings.LockPickBreakChance) {
 				breakLockPick();
 				updatePlayerLockCounter();
 			}
 			currentPos = 0;
 			updateInventory();
 		}
 		
 		if (currentPos == difficulty + 2) {
 			openPlayer = Player;
 			openedInventory = TargetInventory;
 			OpenInventory();
 		}
 	}
 	
 	private static void OpenInventory() {
 		LoreLocks.server.getScheduler().scheduleSyncDelayedTask(LoreLocks.instance, new Runnable() {
     		@Override 
     	    public void run() {
     			openInventory(openPlayer, openedInventory);
     	    }
     	}, 0L);
 	}
 	
 	private static void openInventory(Player player, Inventory inventory) {
 		player.closeInventory();
 		player.openInventory(inventory);
 	}
 	
 	private static Inventory openedInventory;	
 	private static Player openPlayer;
 
 	private void breakLockPick() {
 		if (Player == null || Player.getInventory() == null) {
 			return;
 		}
 		
 		for (int i = 0; i < Player.getInventory().getSize(); i++) {
 			ItemStack stack = Player.getInventory().getItem(i);
 			ItemMeta stackMeta = (stack != null) ? stack.getItemMeta() : null;
 			String stackName = (stackMeta != null) ? ((stackMeta.getDisplayName() != null) ? stackMeta.getDisplayName() : "") : "";
 
 			String requiredStackName = ChatColor.WHITE + Settings.LockPickName + ChatColor.RESET;
 
 			if (stackName.equals(requiredStackName)) {
 				stack.setAmount(stack.getAmount() - 1);
 				Player.getInventory().setItem(i, stack);
 				Player.sendMessage(ChatColor.DARK_RED + "Your " + Settings.LockPickName + " broke" + ChatColor.RESET);
 				
 				if (getPlayerLockPicks() == 0) {
 					Player.closeInventory();
 				}
 				return;
 			}
 		}
 	}
 	
 	@Override
 	public boolean equals(Object obj) {
 		if (!(obj instanceof LockGUI)) {
 			return false;
 		}
 		
 		if (((LockGUI)obj).hashCode() == this.hashCode()) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 	
 	public void ShowLock() {
 		if (getPlayerLockPicks() > 0) {
 			Player.openInventory(LockInventory);
 			hashCode = Player.getOpenInventory().hashCode();
 		} else {
 			Player.sendMessage(ChatColor.DARK_RED + "You need a " + Settings.LockPickName + " to open this chest!" + ChatColor.RESET);
 			GUIs.remove(this);
 		}
 	}
 	
 	public void ShowChest() {
 		Player.openInventory(TargetInventory);
 		GUIs.remove(this);
 	}
 }
