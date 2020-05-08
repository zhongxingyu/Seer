 package com.adamki11s.display;
 
 import java.lang.ref.SoftReference;
 
 import org.bukkit.entity.Player;
 
 import com.adamki11s.npcs.loading.FixedLoadingTable;
 import com.adamki11s.questx.QuestX;
 
 public class FixedSpawnsDisplay {
 	
	static SoftReference<Pages> pages = new SoftReference<Pages>(new Pages(FixedLoadingTable.getFixedSpawns(), 8));
 	
 	public static void display(Player player, int page){
 		if(pages.get() == null){
 			String[] list = FixedLoadingTable.getFixedSpawns();
 			pages = new SoftReference<Pages>(new Pages(list, 8));
 		}
 		Pages p = pages.get();
 		String[] send = p.getStringsToSend(page);
 		QuestX.logChat(player, "Fixed NPC Spawns Page (" + page + "/" + p.getPages() + ") Displaying (8/" + p.getRawArrayLength() + ")");
 		for(String s : send){
 			QuestX.logChat(player, s);
 		}
 	}
 
 }
