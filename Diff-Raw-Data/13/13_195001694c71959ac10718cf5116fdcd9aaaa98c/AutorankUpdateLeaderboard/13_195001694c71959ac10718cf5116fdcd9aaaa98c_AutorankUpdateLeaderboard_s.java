 package me.armar.plugins.autorank;
 
 import java.util.ConcurrentModificationException;
 import java.util.Iterator;
 import java.util.Set;
 
 public class AutorankUpdateLeaderboard implements Runnable {
 
 	private Config data;
 	private Leaderboard leaderboard;
 
 	public AutorankUpdateLeaderboard(Autorank plugin) {
 		data = plugin.getData();
 		leaderboard = plugin.getLeaderboard();
 	}
 
 	@Override
 	public void run() {
 	    try{
 		Set<String> keys = data.getKeys();
 		Iterator<String> it = keys.iterator();
 		while (it.hasNext()) {
			// Get element
 			String key = (String) it.next();
 			leaderboard.addScore((Integer) data.get(key), key);
 		}
 		leaderboard.updateText();
 	    }catch (ConcurrentModificationException e){
 		run();
 	    }
 
 	}
 
 }
