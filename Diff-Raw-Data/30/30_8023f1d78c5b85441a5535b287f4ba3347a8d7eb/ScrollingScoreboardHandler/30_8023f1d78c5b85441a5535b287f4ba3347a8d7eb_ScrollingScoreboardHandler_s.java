 package de.MiniDigger.ScrollingScoreBoardAnnouncer;
 
 import java.util.ArrayList;
 
 import org.bukkit.entity.Player;
 
 public class ScrollingScoreboardHandler {
 
	private ScrollingScoreBoardConfig	                      config;
 
	public ArrayList<ScrollingScoreBoard>	boards	= new ArrayList<>();
 
 	public void init(ScrollingScoreBoardConfig ssb_config) {
 		config = ssb_config;
 	}
 
 	@SuppressWarnings("unchecked")
 	public void loadAll() {
 		boards = (ArrayList<ScrollingScoreBoard>) config.get("boards");
 		if (boards == null) {
 			boards = new ArrayList<>();
 		}
 	}
 
 	public void saveAll() {
 		config.set("boards", "");
 		config.set("boards", boards);
 	}
 
 	public ScrollingScoreBoard get(String name) {
 		for (ScrollingScoreBoard board : boards) {
 			if (board.getName().equalsIgnoreCase(name)) {
 				return board;
 			}
 		}
 		return null;
 	}
 
 	public void create(String name) {
 		ScrollingScoreBoard ssc = new ScrollingScoreBoard(name);
 		boards.add(ssc);
 	}
 
 	public void delete(String name) {
 		ScrollingScoreBoard board = null;
 		for (ScrollingScoreBoard ssb : boards) {
 			if (ssb.getName().equalsIgnoreCase("name")) {
 				ssb.cancelAllTasks();
 				board = ssb;
 				break;
 			}
 		}
 		if (board != null) {
 			boards.remove(board);
 		}
 		saveAll();
 	}
 
 	public void announce(String name, String msg, int slot) {
 		get(name).annonce(msg, slot);
 	}
 
 	public void setBoard(Player player) {
		for(ScrollingScoreBoard ssb : boards){
			if(!ssb.useWorldWhiteList() && ssb.isGroupBlackListed(player.getName())){
 				continue;
 			}
			if(!ssb.useWorldWhiteList() && ssb.isWorldBlackListed(player.getWorld().getName())){
 				continue;
 			}
			if(!ssb.useGroupWhiteList() && ssb.isGroupBlackListed("")){
 				continue;
 			}
			if(ssb.isPlayerWhiteListed(player.getName())){
 				player.setScoreboard(ssb.board);
 				break;
 			}
			if(ssb.isWorldWhiteListed(player.getWorld().getName())){
 				player.setScoreboard(ssb.board);
 				break;
 			}
			if(ssb.isGroupWhiteListed("")){
 				player.setScoreboard(ssb.board);
 				break;
 			}
 		}
 	}
 }
