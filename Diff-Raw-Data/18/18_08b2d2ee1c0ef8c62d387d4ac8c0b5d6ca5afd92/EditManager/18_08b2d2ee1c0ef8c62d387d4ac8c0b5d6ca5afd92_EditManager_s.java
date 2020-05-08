 package com.theminequest.MineQuest.Editable;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Level;
 
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockDamageEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 
 import com.theminequest.MineQuest.MineQuest;
 import com.theminequest.MineQuest.Quest.Quest;
 
 public class EditManager implements Listener {
 	
 	private final List<Edit> edits;
 	
 	public EditManager(){
 		MineQuest.log("[Edit] Starting Manager...");
 		edits = Collections.synchronizedList(new ArrayList<Edit>());
 	}
 	
 	public void addEditTracking(Edit e){
 		if (!edits.contains(e))
 			edits.add(e);
 	}
 	
 	public void rmEditTracking(Edit e){
 		if (edits.contains(e))
 			edits.remove(e);
 	}
 	
 	@EventHandler
 	public void onBlockPlace(BlockPlaceEvent e){
		if (!MineQuest.groupManager.getGroup(MineQuest.groupManager.indexOf(e.getPlayer())).isInQuest())
 			return;
 		e.setCancelled(true);
 		for (Edit ed : edits){
 			ed.onBlockPlace(e);
 			if (!e.isCancelled())
 				return;
 		}
 	}
 	
 	@EventHandler
 	public void onBlockDamage(BlockDamageEvent e){
		if (!MineQuest.groupManager.getGroup(MineQuest.groupManager.indexOf(e.getPlayer())).isInQuest())
 			return;
 		e.setCancelled(true);
 		for (Edit ed : edits){
 			ed.onBlockDamage(e);
 			if (!e.isCancelled())
 				return;
 		}
 	}
 }
