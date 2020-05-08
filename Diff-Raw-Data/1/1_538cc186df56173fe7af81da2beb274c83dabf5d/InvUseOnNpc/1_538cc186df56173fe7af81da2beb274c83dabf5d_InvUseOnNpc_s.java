 package org.darkquest.gs.plugins.misc;
 
 import org.darkquest.gs.model.Bubble;
 import org.darkquest.gs.model.InvItem;
 import org.darkquest.gs.model.Npc;
 import org.darkquest.gs.model.Player;
 import org.darkquest.gs.plugins.listeners.action.InvUseOnNpcListener;
 import org.darkquest.gs.plugins.listeners.executive.InvUseOnNpcExecutiveListener;
 
 public class InvUseOnNpc implements InvUseOnNpcListener, InvUseOnNpcExecutiveListener {
 
 	@Override
 	public boolean blockInvUseOnNpc(Player player, Npc npc, InvItem item) {
 		if(npc.getID() == 6 && item.getID() == 21)
 			return true;
 		return false;
 	}
 
 	@Override
 	public void onInvUseOnNpc(Player player, Npc npc, InvItem item) {
 		if(item.getID() == 21 && npc.getID() == 6) {
 			player.getInventory().remove(item);
 			player.setBusy(true);
 			showBubble(player, item);
 			player.getActionSender().sendSound("filljug");
 			player.getActionSender().sendMessage("You fill up the bucket with milk");
 			player.getInventory().add(new InvItem(22));
 			player.setBusy(false);
 		}
 		return;	
 	}
 	
 	private void showBubble(Player owner, InvItem item) {
 		owner.informGroupOfBubble(new Bubble(owner, item.getID()));
 	}
 
 }
