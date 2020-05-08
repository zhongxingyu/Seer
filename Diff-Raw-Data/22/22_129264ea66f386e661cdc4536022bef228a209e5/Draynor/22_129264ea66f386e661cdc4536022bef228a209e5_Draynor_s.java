 package Nodes;
 
 
 import org.powerbot.core.script.job.state.Node;
 import org.powerbot.game.api.methods.Walking;
 import org.powerbot.game.api.methods.input.Mouse;
 import org.powerbot.game.api.methods.interactive.NPCs;
 import org.powerbot.game.api.methods.interactive.Players;
 import org.powerbot.game.api.methods.node.Menu;
 import org.powerbot.game.api.methods.node.SceneEntities;
 import org.powerbot.game.api.methods.tab.Inventory;
 import org.powerbot.game.api.methods.widget.Camera;
 import org.powerbot.game.api.wrappers.Tile;
 import org.powerbot.game.api.wrappers.interactive.NPC;
 import org.powerbot.game.api.wrappers.node.SceneObject;
 
 import util.Move;
 import util.VARS;
 
 public class Draynor extends Node {
 	private boolean ran = false;
 	@Override
 	public boolean activate() {
 
		return !ran;
 	}
 
 	@Override
 	public void execute() {
 		Inventory.getItem(VARS.EXPLORER_RING).getWidgetChild().interact("Cabbage-port");
 		VARS.Status = "Teleporting to Draynor";
 		VARS.Status = "";
 		sleep(4000);
 		while(Players.getLocal().getAnimation() != -1){
 			sleep(1000);
 		}
 		VARS.Status = "Walking to Draynor Patch";
 		sleep(2000);
 		Move.moveto("Draynor");
 
 		sleep(3000);
 		int x = 10;
 		
 		SceneObject Patch = SceneEntities.getNearest(VARS.DRAYNOR_PATCH);
 		if(!Patch.isOnScreen()){
 			Walking.walk(new Tile(3057,3311,0));
 			
 		}
 		
 		Mouse.click(Patch.getCentralPoint(),false);
 		VARS.Status = "Thinking...";
 		sleep(1500);
 		if(Menu.contains("Pick", "Herbs")){
 			Patch.interact("Pick");
 			VARS.Status = "Picking";
 			sleep(5000);
 			while(Players.getLocal().getAnimation() == 2282){
 				sleep(1000);			
 			}
 			VARS.Status = "Trading for notes with the Leprechaun";
 			Inventory.getItem(VARS.HERB).getWidgetChild().interact("Use");
 			NPC TOOL = NPCs.getNearest(VARS.TOOL_LEPRE_DRAYNOR);
 			x = 10;
 			while(!TOOL.isOnScreen()){
 				VARS.Status = "Looking for the little bastard...";
 				Camera.setAngle(x);
 				x++;
 				
 			}
 			TOOL.click(true);
 		}else if(Menu.contains("Clear", "Dead herbs") || Menu.contains("Clear", "Diseased herbs")){
 			Patch.interact("Clear");
 			VARS.Status = "Clearing Herbs";
 			sleep(2000);
 			while(Players.getLocal().getAnimation() == 830){
 				sleep(2000);
 			}
 		}else if(Menu.contains("Rake", "Herb patch")){
 			Patch.interact("Rake");
 			sleep(2000);
 			while(Players.getLocal().getAnimation() == 2273){
 				sleep(1000);
 			}
 		}else{
 			ran = true;
 			return;
 		}
 		if(!Patch.isOnScreen()){
 			Walking.walk(new Tile(3057,3311,0));
 			
 		}
 		sleep(2000);
 		
 		VARS.Status = "Replanting";
 		Camera.setPitch(80);
 		Inventory.getItem(VARS.SUPER_COMPOST).getWidgetChild().interact("Use");
 		Patch.click(true);
 		sleep(3000);
 		Inventory.getItem(VARS.AVANTOE_SEED).getWidgetChild().interact("Use");
 		Patch.click(true);
 		sleep(2000);
 		ran = true;
 	}
 
 
 }
