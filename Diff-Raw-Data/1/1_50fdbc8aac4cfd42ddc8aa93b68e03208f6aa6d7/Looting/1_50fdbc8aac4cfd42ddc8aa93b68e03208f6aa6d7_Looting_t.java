 package roachkiller;
 import org.powerbot.core.script.job.Task;
 import org.powerbot.core.script.job.state.Node;
 import org.powerbot.game.api.methods.Game;
 import org.powerbot.game.api.methods.Walking;
 import org.powerbot.game.api.methods.interactive.Players;
 import org.powerbot.game.api.methods.node.GroundItems;
 import org.powerbot.game.api.methods.tab.Inventory;
 import org.powerbot.game.api.wrappers.node.GroundItem;
 import org.powerbot.game.api.wrappers.widget.WidgetChild;
 import org.powerbot.game.bot.Context;
 
 
 public class Looting extends Node{
 
 	@Override
 	public boolean activate() {
 		
 		GroundItem g = GroundItems.getNearest(Variable.lootTier);
 		boolean inroacharea = Variable.currentArea.contains(Players.getLocal().getLocation());
 		boolean incombat = Players.getLocal().isInCombat();
 		
 		return g != null && inroacharea && !incombat && Variable.currentArea.contains(g);
 	}
 
 	@SuppressWarnings("deprecation")
 	@Override
 	public void execute() {
 		GroundItem g = GroundItems.getNearest(Variable.lootTier);
 		GroundItem p = g;
 		if(Variable.currentArea.contains(g)){
 			Method.turnTo(g.getLocation(), 6);
 			if(Inventory.isFull()){
 				Variable.status="Eating for room";
 				WidgetChild e = Inventory.getItem(Variable.food).getWidgetChild();
 				e.interact("Eat");
				Task.sleep(600);
 			}else{
 				if(g != null){
 					int t = 0;
 					while(!g.isOnScreen()){
 						Variable.status="Walking to loot";
 						Walking.walk(g.getLocation());
 						Task.sleep(1000,100);
 						t++;
 						if(t==20){
 							Variable.status="Something went wrong walking to loot";
 							Game.logout(false);
 							Context.get().getScriptHandler().stop();
 						}
 					}
 					t = 0;
 					while(g!=null){
 						Variable.status="Picking up loot";
 						g.interact("Take", g.getGroundItem().getName());
 						Task.sleep(1000);
 		
 						while(Players.getLocal().getInteracting() != null || Players.getLocal().isMoving()){
 							Task.sleep(40,10);
 							t++;
 							if(t==400){
 								Variable.status="Something went wrong looting";
 								Game.logout(false);
 								Context.get().getScriptHandler().stop();
 							}
 						}
 						g = GroundItems.getNearest(Variable.lootTier);
 					}	
 					
 					System.out.println(Method.componentTime(Variable.runTime));
 					int id = p.getId();
 					int value = Variable.lootPrices.get(id);
 					int stack = p.getGroundItem().getStackSize();
 					if(value == 0) {
 						value = Variable.lootPrices.get(id - 1);
 						if(value != 0) {
 							int price = value * stack;
 							Variable.totalGained += price;	
 						}
 					} else {
 						Variable.totalGained += value * stack;
 					}
 					Task.sleep(1000);
 				}
 			}
 		}
 	}
 }
