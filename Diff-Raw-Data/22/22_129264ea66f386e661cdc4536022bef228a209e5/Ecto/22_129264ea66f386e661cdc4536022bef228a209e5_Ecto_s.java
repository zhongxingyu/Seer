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
 
 public class Ecto extends Node {
 	private boolean ran = false;
 	@Override
 	public boolean activate() {
 		
		return !ran;
 	}
 
 	@Override
 	public void execute() {
 		VARS.Status = "Teleportinggg";
 		Inventory.getItem(VARS.ECTOPHIAL).getWidgetChild().interact("Empty");
 		sleep(10000);
 		VARS.Status = "Walking to the patch";
 		Move.moveto("Ecto");
 		
 		SceneObject Patch = SceneEntities.getNearest(VARS.ECTO_PATCH);
 		sleep(3000);
 		int x = 10;
 		
 		if(!Patch.isOnScreen()){
 			Walking.walk(new Tile(3606, 3531, 0));
 			
 		}
 		Mouse.click(Patch.getCentralPoint(),false);
 		VARS.Status = "Thinking...";
 		if(Menu.contains("Pick", "Herbs")){
 			VARS.Status = "Picking Herbs";
 			Patch.interact("Pick");
 			sleep(5000);
 			while(Players.getLocal().getAnimation() == 2282){
 				sleep(1000);			
 			}
 			VARS.Status = "Getting them bank notes";
 			Inventory.getItem(VARS.HERB).getWidgetChild().interact("Use");
 			NPC TOOL = NPCs.getNearest(VARS.TOOL_LEPRE_ECTO);
 			x = 10;
 			while(!TOOL.isOnScreen()){
 				VARS.Status = "Seaching for the wee man";
 				Camera.setAngle(x);
 				x++;
 				
 			}
 			TOOL.click(true);
 		}else if(Menu.contains("Clear", "Dead herbs") || Menu.contains("Clear", "Diseased herbs")){
 			VARS.Status = "Clearing herbs";
 			sleep(2000);
 			Patch.interact("Clear");
 			sleep(2000);
 			while(Players.getLocal().getAnimation() == 830){
 				sleep(2000);
 			}
 		}else if(Menu.contains("Rake", "Herb patch" )){
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
 			Walking.walk(new Tile(3606, 3531, 0));
 			
 		}
 		VARS.Status = "REPLANT";
 		Camera.setPitch(80);
 		Inventory.getItem(VARS.SUPER_COMPOST).getWidgetChild().interact("Use");
 		Patch.click(true);
 		sleep(3000);
 		Inventory.getItem(VARS.AVANTOE_SEED).getWidgetChild().interact("Use");
 		Patch.click(true);
 		sleep(5000);
 
 		ran = true;
 	}
 	
 	
 }
