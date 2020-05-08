 package org.darkquest.gs.plugins.misc;
 
 import java.util.Arrays;
 
 import org.darkquest.config.Constants;
 import org.darkquest.config.Formulae;
 import org.darkquest.gs.event.SingleEvent;
 import org.darkquest.gs.model.Bubble;
 import org.darkquest.gs.model.GameObject;
 import org.darkquest.gs.model.InvItem;
 import org.darkquest.gs.model.Player;
 import org.darkquest.gs.plugins.listeners.action.InvUseOnObjectListener;
 import org.darkquest.gs.tools.DataConversions;
 import org.darkquest.gs.world.World;
 
public class InvUseOnObject implements InvUseOnObjectListener {
 
 	static int[] objectIDs;
 	static {
 		objectIDs = new int[] { 2, 466, 814, 48, 26, 86, 1130 };
 		Arrays.sort(objectIDs);
 	}
 	@Override
 	public void onInvUseOnObject(GameObject obj, InvItem item, Player owner) {
		if(Arrays.binarySearch(objectIDs, obj.getID()) >= 0) {
 			handleRefill(owner, item);
 			return;
		}
 	}
 	private void handleRefill(Player owner, final InvItem item) {
 		if (!itemId(new int[] { 21, 140, 465 }, item) && !itemId(Formulae.potionsUnfinished, item) && !itemId(Formulae.potions1Dose, item) && !itemId(Formulae.potions2Dose, item) && !itemId(Formulae.potions3Dose, item)) {
 			owner.getActionSender().sendMessage("Nothing interesting happens.");
 			return;
 		}
 		if (owner.getInventory().remove(item) > -1) {
 			owner.setBusy(true);
 			showBubble(owner, item);
 			owner.getActionSender().sendSound("filljug");
 			switch (item.getID()) {
 			case 21:
 				owner.getInventory().add(new InvItem(50));
 				break;
 			case 140:
 				owner.getInventory().add(new InvItem(141));
 				break;
 			default:
 				owner.getInventory().add(new InvItem(464));
 				break;
 			}
 			owner.getActionSender().sendMessage("You fill the " + item.getDef().getName());
 			owner.getActionSender().sendInventory();
 
 			if(owner.getInventory().hasItemId(item.getID()) && Constants.GameServer.BATCH_EVENTS) {
 				
 					World.getWorld().getDelayedEventHandler().add(new SingleEvent(owner, 200) {
 						public void action() {
 							owner.setBusy(false);
 							handleRefill(owner, item);
 						}
 					});
 				
 			} else {
 				owner.setBusy(false);
 			}
 		}
 
 	}
 	private boolean itemId(int[] ids, InvItem item) {
 		return DataConversions.inArray(ids, item.getID());
 	}
 	private void showBubble(Player owner, InvItem item) {
 		owner.informGroupOfBubble(new Bubble(owner, item.getID()));
 	}
 }
