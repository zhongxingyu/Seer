 package beerBuyer;
 
 import org.powerbot.concurrent.strategy.Strategy;
 import org.powerbot.game.api.methods.Game;
 import org.powerbot.game.api.methods.Walking;
 import org.powerbot.game.api.methods.interactive.NPCs;
 import org.powerbot.game.api.methods.interactive.Players;
 import org.powerbot.game.api.methods.tab.Inventory;
 import org.powerbot.game.api.methods.widget.Bank;
 import org.powerbot.game.api.methods.widget.Camera;
import org.powerbot.game.api.util.Time;
 import org.powerbot.game.api.wrappers.interactive.NPC;
 
public class bankBeer extends Strategy implements Runnable {
 
 	@Override
 	public void run() {
 
 		if (Inventory.isFull() == true) {
 
 			if (ID.bankArea.contains(Players.getLocal().getLocation())) {
 
 				NPC Banker = NPCs.getNearest(ID.bankerID);
 				if (Banker.isOnScreen()) {
 
					Bank.open();
 					Bank.depositInventory();
					Time.sleep(300, 1000);
 					Bank.close();
 
 				}
 
 				else {
 					Camera.turnTo(Banker);
 				}
 
 			} else {
 				Walking.newTilePath(ID.bankToBeerShop);
 			}
 
 		}
 
 		else {
 			ID.Bank = false;
 		}
 
 	}
 
 	public Boolean Validate() {
		return Inventory.isFull() && Game.getClientState() != 12;
 	}
 
 }
