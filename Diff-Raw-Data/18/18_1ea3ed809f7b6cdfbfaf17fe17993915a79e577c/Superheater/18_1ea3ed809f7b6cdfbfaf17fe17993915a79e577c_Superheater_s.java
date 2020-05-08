 package autobot.behavior;
 
 import org.powerbot.game.api.methods.input.Keyboard;
 import org.powerbot.game.api.methods.tab.Equipment;
 import org.powerbot.game.api.methods.tab.Equipment.Slot;
 import org.powerbot.game.api.methods.tab.Inventory;
 import org.powerbot.game.api.methods.widget.Bank;
 import org.powerbot.game.api.util.Time;
 import org.powerbot.game.api.wrappers.node.Item;
 
 /** Sample Behavior that heats mith@author Eric Kuxhausen **/
 public class Superheater extends Behavior {
 
 	private boolean canAccomplish;
 	public int errorCount;
 
 	public static final int mithrilOre = 447, mithBar = 2359, coalOre = 453,
			natureRunes = 561, fireBattleStaff = 1393, staffOfFire = 1387;
 
	/** this assumes near bank, fire staff equipped, inventory tab open, 
	 * and superheat bound to 1 on the open action bar **/
 	public Superheater(){
 		// TODO reduce assumptions
 		try {
 			Bank.open();
 			sleep(Times.SHORT);
 			Bank.depositInventory();
 			sleep(Times.SHORT);
 			Bank.depositEquipment();
 			sleep(Times.SHORT);
 			if(!Bank.withdraw(staffOfFire, 1)){
 				System.out.println("withdraw failed once"); 
 				
 				sleep(Times.SHORT);
 				Bank.close();
 				sleep(Times.BANK);
 				
 				Bank.open();
 				sleep(Times.SHORT);
 				if(!Bank.withdraw(staffOfFire, 1)){
 					System.out.println("withdraw failed both times");
 					canAccomplish = false; 
 					
 					sleep(Times.SHORT);
 					Bank.close();
 					sleep(Times.BANK);
 					
 					return;
 				}
 			}
 			sleep(Times.SHORT);
 			Bank.close();
 			sleep(Times.BANK);
 			
 			Inventory.getItem(staffOfFire).getWidgetChild().click(true);
 			canAccomplish = true;
 			System.out.println("Prepare Completed");
 
 		} catch (Exception e) {
 			canAccomplish = false;
 		}
 	}
 
 	@Override
 	public boolean canAct() {
 		if (errorCount > 5) // arbitrary number of acceptable errors
 		{
 			System.out.println("Shutting down");
 			canAccomplish = false;
 		}
 		// TODO add more checking
 		return canAccomplish;
 	}
 
 	@Override
 	public void act() {
 		try {
 			bank();
 			for (int i = 0; i < 5; i++)
 			{
 				System.out.println("making bar num: "+i);
 				sleep(Times.VERYSHORT);
 				smelt();
 			}
 		} catch (Exception e) {
 			errorCount++;
 			System.out.println("Caught an error: "+e);
 		}
 	}
 
 	private void smelt() throws Exception {
 		Keyboard.sendKey((char) '1', getTime(Times.KEYPRESS));
 		sleep(Times.KEYPRESS);
 		sleep(Times.VERYSHORT);
 		Inventory.getItemAt(25).getWidgetChild().click(true);
 		sleep(Times.SUPERHEAT);
 	}
 
 	private void bank() throws Exception {
 		if(Bank.open())
 			System.out.println("opened bank");
 		else {
 			sleep(Times.SHORT);
 			Bank.open();
 		}
 		sleep(Times.SHORT);
 		Bank.depositInventory();
 		sleep(Times.SHORT);
		Bank.withdraw(natureRunes, 5);
 		sleep(Times.SHORT);
		Bank.withdraw(coalOre, 20);
 		sleep(Times.SHORT);
		Bank.withdraw(mithrilOre, 5);
 		sleep(Times.SHORT);
 		Bank.close();
 		sleep(Times.BANK);
 
 	}
 }
