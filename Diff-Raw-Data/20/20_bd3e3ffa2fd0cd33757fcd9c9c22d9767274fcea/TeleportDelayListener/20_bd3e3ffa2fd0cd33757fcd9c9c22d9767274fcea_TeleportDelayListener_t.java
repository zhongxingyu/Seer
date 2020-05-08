 import java.util.HashMap;
 
 
 public class TeleportDelayListener extends PluginListener{
 	HashMap<String,String> players = new HashMap<String,String>();
 	public boolean onCommand(Player player, String[] split) {
		if (split[0].equals("/spawn") && !player.canUseCommand("/skipdelay") && player.canUseCommand("/spawn")){
 			if (players.containsKey(player.getName())){
 				player.notify("You already have a pending teleport request!");
 				return true;
 			}
			player.sendMessage("�7Teleportation will commence in 3 seconds. Don't move.");
 			runTimer(player, split);
 		return true;
 		}
		if (split[0].equals("/home") && !player.canUseCommand("/skipdelay") && player.canUseCommand("/home")){
 			if (players.containsKey(player.getName())){
 				player.notify("You already have a pending teleport request!");
 				return true;
 			}
			player.sendMessage("�7Teleportation will commence in 3 seconds. Don't move.");
 			runTimer(player, split);
 			return true;
 		}
		if (split[0].equals("/warp") && !player.canUseCommand("/skipdelay") && player.canUseCommand("/warp")){
 			if (players.containsKey(player.getName())){
 				player.notify("You already have a pending teleport request!");
 				return true;
 			}
 			if (split.length <2 || split.length >2){
 				return false;
 			}
 			
 			if (etc.getDataSource().getWarp(split[1]) == null){
 				return false;
 			}
			player.sendMessage("�7Teleportation will commence in 3 seconds. Don't move.");
 			runTimer(player, split);
 			return true;
 		}
 		return false;
 	}
 	
 	public void onPlayerMove(Player player,Location from,Location to){
 		if (players.containsKey(player.getName())){
		player.sendMessage("�cTeleportation request cancelled.");
 		players.remove(player.getName());
 		}
 	}
 	
 	public void runTimer(final Player player, final String[] split){
     new Thread() {
   	     public void run() {
   	          try{
   	        	  players.put(player.getName(), Math.floor(player.getX())+","+Math.floor(player.getY())+","+Math.floor(player.getZ()));
   	        	  Thread.sleep(3000);
   	        	  if (players.containsKey(player.getName())){
   	        	  if (players.get(player.getName()).equals(Math.floor(player.getX())+","+Math.floor(player.getY())+","+Math.floor(player.getZ()))){
   	        	  if (split[0].equals("/spawn")){
   	        		  player.teleportTo(etc.getServer().getDefaultWorld().getSpawnLocation());
   	        		players.remove(player.getName());
   	        		  return;
   	        	  }
   	        	  if (split[0].equals("/home")){
   	        		  if (etc.getDataSource().getHome(player.getName()) == null){
   	        			  player.teleportTo(player.getWorld().getSpawnLocation());
   	        			players.remove(player.getName());
     	        		  return;
   	        		  }
   	        		  player.teleportTo(etc.getDataSource().getHome(player.getName()).Location);
   	        		players.remove(player.getName());
   	        		  return;
   	        	  }
   	        	  if (split[0].equals("/warp")){
   	        		 player.teleportTo(etc.getDataSource().getWarp(split[1]).Location);
   	        		 players.remove(player.getName());
   	        		 return;
   	        	  }
   	        	  }
   	        	players.remove(player.getName());
   	        	  }
  	        	}catch(InterruptedException e) {}
  	        }
  	     }.start();
 }
 }
