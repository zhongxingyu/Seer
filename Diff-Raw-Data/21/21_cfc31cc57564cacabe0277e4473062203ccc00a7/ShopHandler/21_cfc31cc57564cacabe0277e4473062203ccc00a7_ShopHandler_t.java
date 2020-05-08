 package shop;
 
 import java.util.ArrayList;

import android.gameengine.icadroids.objects.GameObject;
 import Input.Button;
 
public class ShopHandler extends GameObject{
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
 	
	@ Override
	public void update() {
		if (repairShopOpen) {
			repairShop.createButtons();
		} else {
			repairShop.destroyButtons();
		}
		
		if (upgradeShopOpen) {
			upgradeShop.createButtons();
		}else {
			upgradeShop.destroyButtons();
		}
	}
 }
