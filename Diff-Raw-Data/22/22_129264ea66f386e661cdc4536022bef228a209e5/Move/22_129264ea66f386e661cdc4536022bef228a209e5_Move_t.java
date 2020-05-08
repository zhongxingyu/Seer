 package util;
 
 import org.powerbot.core.script.job.LoopTask;
 import org.powerbot.core.script.job.Task;
 import org.powerbot.game.api.methods.Walking;
 
 
 public abstract class Move extends LoopTask{
 	public static void moveto(String loc){
		if(loc.equals("Ardy")){
 			for(int x = 0; x < VARS.TO_ARDY_TILES.length; x++){
 				Walking.walk(VARS.TO_ARDY_TILES[x]);
 				Task.sleep(5000);	
 			}
		}else if(loc.equals("Cammy")){
 			for(int x = 0; x < VARS.TO_CATHERBY_TILES.length; x++){
 				Walking.walk(VARS.TO_CATHERBY_TILES[x]);
 				Task.sleep(2500);	
 			}
 			
		}else if(loc.equals("Draynor")){
 			for(int x = 0; x < VARS.TO_DRAYNOR_TILES.length; x++){
 				Walking.walk(VARS.TO_DRAYNOR_TILES[x]);
 				Task.sleep(2000);	
 			}
 			
 		}else{
 			for(int x = 0; x < VARS.TO_ECTO_TILES.length; x++){
 				Walking.walk(VARS.TO_ECTO_TILES[x]);
 				Task.sleep(5000);	
 			}
 			
 		}
 		
 	}
 	
 }
