 package com.github.mineGeek.ItemRules.Integration;
 
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 
 import com.github.mineGeek.ItemRules.ItemRules;
 import com.github.mineGeek.ItemRules.Store.Players;
 
import com.gmail.nossr50.events.experience.McMMOPlayerLevelUpEvent;
 import com.gmail.nossr50.events.party.McMMOPartyChangeEvent;
 
 /**
  * Manages Events with McMMO plugin.
  * Shame not all of the events are consistant :(
  *
  */
 public class McMMOEventListener implements Listener {
 	
 	@SuppressWarnings("unused")
 	private ItemRules itemRules;
 	
 	public McMMOEventListener( ItemRules plugin ) {
 		this.itemRules = plugin;
 	}
 	
 	
 	@EventHandler(priority = EventPriority.LOWEST)
	public void onMcMMOExperience( McMMOPlayerLevelUpEvent evt) {
 		
 		evt.getPlayer().sendMessage("levelup");
 		Players.get( evt.getPlayer() ).loadRules();
 		
 	}
 	
 	@EventHandler(priority = EventPriority.LOWEST)
 	public void onMcMMOPartyChange( McMMOPartyChangeEvent evt ) {
 		
 		Players.get(evt.getPlayer() ).loadRules();
 		
 	}
 	
 	public void close() {
 		this.itemRules = null;
 	}
 	
 
 }
