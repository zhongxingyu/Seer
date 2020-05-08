 import org.powerbot.core.script.job.Task;
 import org.powerbot.game.api.methods.Calculations;
 import org.powerbot.game.api.methods.Walking;
 import org.powerbot.game.api.methods.Widgets;
 import org.powerbot.game.api.methods.interactive.NPCs;
 import org.powerbot.game.api.methods.node.SceneEntities;
 import org.powerbot.game.api.methods.tab.Inventory;
 import org.powerbot.game.api.methods.widget.Bank;
 import org.powerbot.game.api.methods.widget.Camera;
 import org.powerbot.game.api.wrappers.Tile;
 
 
 public class Methods {
 
 	public static void openBank() {
 		if(SceneEntities.getNearest(782) != null) {
 			Walking.walk(SceneEntities.getNearest(782));
 			Task.sleep(1000, 1500);
 			SceneEntities.getNearest(782).interact("Bank", "Bank booth");
 			Task.sleep(20, 40);
 			DynamicSleep.bankIsOpen();
 			Task.sleep(200, 400);
 		}
 	}
 
 	public static void leaveBank() {
 		Walking.walk(new Tile(3170, 3429, 0));
 		DynamicSleep.leavingBank();
 		Task.sleep(200, 400);
 	}
 
 	public static void deposit() {
 		if(Variables.CRAFTEDRUNE_ID != 556) {
 			Task.sleep(20, 40);
 			if(Inventory.getItem(Variables.CRAFTEDRUNE_ID) != null) {
 				Task.sleep(20, 40);
 				Bank.deposit(Variables.CRAFTEDRUNE_ID, 26);
 				Task.sleep(20, 40);
 			}
 		} else {
 			if(Inventory.getItem(Variables.CRAFTEDRUNE_ID) != null) {
 				Task.sleep(20, 40);
 				Bank.deposit(Variables.CRAFTEDRUNE_ID, 999);
 				Task.sleep(20, 40);
 			}
 		}
 	}
 
 	public static void neckyCheck() {
 		if(!checkForNecky()) {
 			Bank.withdraw(5521, 1);
 			Task.sleep(20, 40);
 			Inventory.getItem(5521).getWidgetChild().interact("Wear");
 			Task.sleep(200, 400);
 		}
 	}
 
 	public static boolean checkForNecky() {
 		if(Bank.isOpen()) {
 			Widgets.get(762).getChild(120).interact("Show Equipment Stats");
 			Task.sleep(50, 80);
 			if(Widgets.get(667).getChild(190).getChild(2).getActions() != null) {
 				Widgets.get(667).getChild(13).interact("Show Bank");
 				Task.sleep(50, 80);
 				return true;
 			} else {
 				Widgets.get(667).getChild(13).interact("Show Bank");
 				Task.sleep(50, 80);
 			}
 		}
 		return false;
 	}
 
 	public static void withdraw() {
 		if(Variables.ELEMENTALRUNE_ID != 556) {
 			if(Inventory.getItem(Variables.ELEMENTALRUNE_ID) == null) {
 				Bank.withdraw(Variables.ELEMENTALRUNE_ID, 999999);
 				Task.sleep(20, 40);
 				Bank.withdraw(Variables.TALISMAN_ID, 1);
 				Task.sleep(20, 40);
 			} else {
				if(Inventory.getItem(Variables.TALISMAN_ID) == null) {
					Bank.withdraw(Variables.TALISMAN_ID, 1);
					Task.sleep(20, 40);
				}
			}
			if(Inventory.getItem(Variables.ESSENCE_ID) == null) {
				Bank.withdraw(Variables.ESSENCE_ID, 26);
				Task.sleep(20, 40);
 			}
 		} else {
 			Bank.withdraw(Variables.ESSENCE_ID, 28);
 			Task.sleep(20, 40);
 		}
 		Bank.close();
 	}
 
 	public static void Rest() {
 		Variables.status = "Resting...";
 		if(Walking.getEnergy() <= 30 && NPCs.getNearest(8699) != null && Calculations.distanceTo(NPCs.getNearest(8699)) < 15) {
 			Walking.walk(NPCs.getNearest(8699));
 			Task.sleep(2000);
 			NPCs.getNearest(8699).interact("Listen-to", "Musician");
 			Task.sleep(20, 40);
 			DynamicSleep.whileResting();
 			Task.sleep(200, 400);
 		}
 	}
 
 	public static void leaveAlterEntrance() {
 		Walking.walk(new Tile(3142, 3413, 0));
 		DynamicSleep.leavingAlterEntrance();
 	}
 
 	public static void enterAlter() {
 		Walking.walk(SceneEntities.getNearest(2452));
 		Task.sleep(500, 1000);
 		SceneEntities.getNearest(2452).interact("Enter", "Mysterious ruins");
 		DynamicSleep.enteringAlter();
 		Task.sleep(200, 400);
 	}
 
 	public static void leaveAlter() {
 		Walking.walk(SceneEntities.getNearest(2465));
 		Task.sleep(2000, 3000);
 		Camera.turnTo(SceneEntities.getNearest(2465));
 		Task.sleep(500, 1000);
 		SceneEntities.getNearest(2465).interact("enter", "Portal");
 		DynamicSleep.leavingAlter();
 		Task.sleep(200, 400);
 	}
 
 	public static void craftRunes() {
 		if(Variables.ELEMENTALRUNE_ID != 556) {
 			Task.sleep(500, 1000);
 			Walking.walk(SceneEntities.getNearest(2478));
 			Task.sleep(2000, 3000);
 			Camera.turnTo(SceneEntities.getNearest(2478));
 			Task.sleep(500, 1000);
 			Inventory.selectItem(Variables.TALISMAN_ID);
 			Task.sleep(20, 40);
 			SceneEntities.getNearest(2478).interact("Use");
 			Task.sleep(20, 40);
 			DynamicSleep.whileCrafting();
 			Task.sleep(200, 400);
 		}
 		
 	}
 }
