 package util.strategies;
 
 import java.awt.event.KeyEvent;
 
 import org.powerbot.concurrent.strategy.Strategy;
 import org.powerbot.game.api.methods.Tabs;
 import org.powerbot.game.api.methods.Widgets;
 import org.powerbot.game.api.methods.input.Keyboard;
 import org.powerbot.game.api.methods.node.SceneEntities;
 import org.powerbot.game.api.methods.tab.Inventory;
 import org.powerbot.game.api.util.Time;
 import org.powerbot.game.api.util.Timer;
 
 import util.Constants;
 
 public class TeleportToBank extends Strategy implements Runnable {
 
 	@Override
 	public boolean validate() {
		return SceneEntities.getNearest(Constants.NATURE_ALTAR) != null && !PouchHandler.allFull() 
 				&& !Inventory.contains(Constants.PURE_ESSENCE);
 	}
 
 	@Override
 	public void run() {
 		if (Widgets.get(1188, 20).visible()) {
 			Keyboard.sendKey((char) KeyEvent.VK_3);
 			Time.sleep(2000);
 			return;
 		}
 		if(Tabs.EQUIPMENT.open()) {
 			if(Widgets.get(387, 31).interact("Teleport")) {
 				final Timer timer = new Timer(1500);
 				while (timer.isRunning() && !Widgets.get(1188, 20).visible()) {
 					Time.sleep(15);
 				}
 				if (Widgets.get(1188, 20).visible()) {
 					Keyboard.sendKey((char) KeyEvent.VK_3);
 					Time.sleep(2000);
 				}
 			}
 		}
 	}
 
 }
