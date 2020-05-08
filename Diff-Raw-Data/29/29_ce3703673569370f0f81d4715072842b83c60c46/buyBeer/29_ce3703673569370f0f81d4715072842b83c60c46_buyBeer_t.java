 package beerBuyer;
 
 import org.powerbot.concurrent.strategy.Strategy;
 import org.powerbot.game.api.methods.Game;
 import org.powerbot.game.api.methods.Walking;
 import org.powerbot.game.api.methods.Widgets;
 import org.powerbot.game.api.methods.interactive.NPCs;
 import org.powerbot.game.api.methods.interactive.Players;
 import org.powerbot.game.api.methods.tab.Inventory;
 import org.powerbot.game.api.methods.widget.Camera;
 import org.powerbot.game.api.util.Time;
 import org.powerbot.game.api.wrappers.interactive.NPC;
 
public class buyBeer extends Strategy implements Runnable {
 
 	@Override
 	public void run() {
 
 		if (Inventory.isFull() == false) {
 			if (ID.barArea.contains(Players.getLocal().getLocation())) {
 
 				NPC barKeep = NPCs.getNearest(ID.barKeepID);
 				if (barKeep.isOnScreen()) {
 					barKeep.interact("Talk-to");
 					Widgets.get(1184, 18).click(true);
 					Time.sleep(100, 300);
 					Widgets.get(1188, 2).click(true);
 					Time.sleep(100, 200);
 					Widgets.get(1191, 19).click(true);
 					Time.sleep(100, 400);
 					Widgets.get(1184, 18).click(true);
 					Time.sleep(100, 200);
 					ID.beerID++;
 				} else {
 					Camera.turnTo(barKeep);
 				}
 			} else {
 				Walking.newTilePath(ID.bankToBeerShop).traverse();
 			}
 		}
 
 		else {
 
 			ID.Bank = true;
 
 		}
 	}
 
 	public Boolean Validate() {
 		return Inventory.isFull() == false && Game.getClientState() != 12
 				&& ID.Bank == false;
 
 	}
 
 }
