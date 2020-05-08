 package beerBuyer;
 
import org.powerbot.concurrent.Task;
 import org.powerbot.concurrent.strategy.Strategy;
 import org.powerbot.game.api.methods.Game;
 import org.powerbot.game.api.methods.Walking;
 import org.powerbot.game.api.methods.interactive.NPCs;
 import org.powerbot.game.api.methods.interactive.Players;
 import org.powerbot.game.api.methods.tab.Inventory;
 import org.powerbot.game.api.methods.widget.Bank;
 import org.powerbot.game.api.methods.widget.Camera;
 import org.powerbot.game.api.wrappers.interactive.NPC;
 
@SuppressWarnings("deprecation")
public class bankBeer extends Strategy implements Task {
 
 	@Override
 	public void run() {
 
 		if (Inventory.isFull() == true) {
 
 			if (ID.bankArea.contains(Players.getLocal().getLocation())) {
 
 				NPC Banker = NPCs.getNearest(ID.bankerID);
 				if (Banker.isOnScreen()) {
 
					Banker.interact("Bank");
 					Bank.depositInventory();
 					Bank.close();
 
 				}
 
 				else {
 					Camera.turnTo(Banker);
 				}
				Banker.interact("Bank");
 
 			} else {
 				Walking.newTilePath(ID.bankToBeerShop);
 			}
 
 		}
 
 		else {
 			ID.Bank = false;
 		}
 
 	}
 
 	public Boolean Validate() {
		return Inventory.isFull() == true && Game.getClientState() != 12
				&& ID.Bank == true;

 	}
 
 }
