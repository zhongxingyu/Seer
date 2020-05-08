 package com.theminequest.MQCoreRPG.SpoutPlugin;
 
 import org.bukkit.entity.Player;
 import org.bukkit.event.Listener;
 import org.getspout.spoutapi.SpoutManager;
 import org.getspout.spoutapi.gui.Color;
 import org.getspout.spoutapi.gui.GenericLabel;
 import org.getspout.spoutapi.gui.Widget;
 import org.getspout.spoutapi.gui.WidgetAnchor;
 import org.getspout.spoutapi.gui.WidgetAnim;
 import org.getspout.spoutapi.player.SpoutPlayer;
 
 import com.theminequest.MQCoreRPG.MQCoreRPG;
 import com.theminequest.MineQuest.MineQuest;
 
 public class PopupManager implements Listener {
 	
 	public PopupManager() throws ClassNotFoundException{
 		Class.forName("org.getspout.spoutapi.Spout");
 		MineQuest.log("[Popup] Starting Manager...");
 	}
 	
 	public void triggerNotificationPopup(Player player, String message){
 		final SpoutPlayer sp = SpoutManager.getPlayer(player);
		final Widget text = new GenericLabel(message).setTextColor(new Color("808080")).setHeight(10).setWidth(20).setAnchor(WidgetAnchor.BOTTOM_RIGHT).shiftXPos(-5).shiftYPos(-10).animate(WidgetAnim.POS_Y, -1F, (short)20, (short)1, false, false).animateStart();
 		sp.getMainScreen().attachWidget(MQCoreRPG.activePlugin,text);
 		
 		MQCoreRPG.activePlugin.getServer().getScheduler().scheduleSyncDelayedTask(MQCoreRPG.activePlugin, new Runnable() {
 		    public void run() {
 		    	sp.getMainScreen().removeWidget(text);
 		    }
 		}, 100L);
 	}
 	
 	/*
 	 * Borrowed from RpgEssentials :<
 	 */
 	public void triggerExpPopup(Player player, double addexp){
 		final SpoutPlayer splayer = SpoutManager.getPlayer(player);
 		final Widget exp = new GenericLabel("+" + addexp + " exp").setTextColor(new Color(1.0F, 1.0F, 0, 1.0F)).setHeight(10).setWidth(20).setAnchor(WidgetAnchor.CENTER_CENTER).shiftXPos(-5).shiftYPos(-10).animate(WidgetAnim.POS_Y, -1F, (short)20, (short)1, false, false).animateStart();
 		splayer.getMainScreen().attachWidget(MQCoreRPG.activePlugin, exp);
 
 		MQCoreRPG.activePlugin.getServer().getScheduler().scheduleSyncDelayedTask(MQCoreRPG.activePlugin, new Runnable() {
 		    public void run() {
 		    	splayer.getMainScreen().removeWidget(exp);
 		    }
 		}, 20L);
 	}
 
 }
