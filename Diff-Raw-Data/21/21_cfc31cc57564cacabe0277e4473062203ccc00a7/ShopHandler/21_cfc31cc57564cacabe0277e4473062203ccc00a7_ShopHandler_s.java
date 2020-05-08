 package shop;
 
 import java.util.ArrayList;
 import Input.Button;
 
public class ShopHandler {
 	private static Shop repairShop;
 	static private boolean repairShopOpen = false;
 	
 	private static Shop upgradeShop;
 	static private boolean upgradeShopOpen = false;
 	
 	public static boolean getUpgradeShopOpen() {
 		return upgradeShopOpen;
 	}
 	public static void setUpgradeShopOpen(boolean upgradeShopOpen) {
 		ShopHandler.upgradeShopOpen = upgradeShopOpen;
 	}
 	public static boolean getRepairShopOpen() {
 		return repairShopOpen;
 	}
 	public static void setRepairShopOpen(boolean repairShopOpen) {
 		ShopHandler.repairShopOpen = repairShopOpen;
 	}
 	
	
 }
