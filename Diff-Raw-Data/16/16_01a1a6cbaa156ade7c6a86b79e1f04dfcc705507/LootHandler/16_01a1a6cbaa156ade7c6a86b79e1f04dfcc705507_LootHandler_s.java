 package BlackUnicornKiller.Nodes.CombatHandlers;
 
 import BlackUnicornKiller.BlackUnicornKiller;
 import BlackUnicornKiller.Nodes.Globals;
 
 import org.powerbot.core.script.job.Task;
 import org.powerbot.core.script.job.state.Node;
 import org.powerbot.game.api.methods.Calculations;
 import org.powerbot.game.api.methods.Tabs;
 import org.powerbot.game.api.methods.Walking;
 import org.powerbot.game.api.methods.input.Mouse;
 import org.powerbot.game.api.methods.node.GroundItems;
 import org.powerbot.game.api.methods.tab.Inventory;
 import org.powerbot.game.api.methods.widget.Camera;
 import org.powerbot.game.api.util.Random;
 import org.powerbot.game.api.util.Timer;
 import org.powerbot.game.api.wrappers.Entity;
 
 import java.awt.*;
 public class LootHandler extends Node {
 
     Rectangle screen = new Rectangle(1,55,518,258);
     Point clickPoint = new Point();
 
     public void altCameraTurnTo(Entity e){
         do{
             Camera.setAngle(Camera.getYaw() + Random.nextInt(35, 55));
         }while(!altIsOnScreen(e));
     }
 
     public static boolean invChangeSleep(){
 
         Timer timeCheck = new Timer(Random.nextInt(1200,1600));
         int tempInvCount = Inventory.getCount();
         int newInvCount;
         do {
             BlackUnicornKiller.status="Waiting for Inv Change: " +timeCheck.getRemaining();
             Task.sleep(20,50);
             newInvCount = Inventory.getCount();
             if(timeCheck.getRemaining()<=100){return false;}
         }while(tempInvCount==newInvCount && timeCheck.isRunning());
         return true;
     }
 
     public boolean altInteract(Entity e, String cmd, String sub){
         if(e != null){
             if(altIsOnScreen(e)){
                 for(Polygon p : e.getBounds()){
                     if(screen.contains(p.getBounds())){
                         clickPoint.setLocation(p.getBounds().x,p.getBounds().y);
                     }
                 }
                 if(Globals.upText != null && Globals.upText != null){
                     e.hover();
                     if(Globals.upText.getText().contains(cmd) && Globals.upText.getText().contains(sub)){
                         clickPoint.setLocation(e.getCentralPoint());
                         Task.sleep(50,150);
                         Mouse.click(clickPoint, true);
                         Task.sleep(250,500);
                     }else{
                         if(e.interact(cmd)){return true;}
                     }
                 }
 
             }
             Timer timeCheck = new Timer(3000);
             do{
                 Task.sleep(250,350);
                 BlackUnicornKiller.status = ("Sleeping after click.");
                 if(Globals.interacting != null && Globals.me != null){
                     if(Globals.interacting.getHealthPercent()<=0 || Globals.me.isIdle()){
                         return true;
                     }
                 } else if (Globals.interacting == null){
                     return false;
                 }
 
             }while(timeCheck.isRunning() && Globals.interacting.equals(e) ||
                    Globals.me.isMoving() || Globals.me.isInCombat());
         }
         return false;
     }
 
     public boolean altIsOnScreen(Entity e){
         for(final Polygon p : e.getBounds()){
             if(screen.contains(p.getBounds())){
                 return true;
             }
         }
         return false;
     }
 
     public void boneClearer(){
         if(Inventory.contains(Globals.ID_ITMES_BONES)){
             Inventory.getItem(Globals.ID_ITMES_BONES).getWidgetChild().interact("Drop");
         }
     }
 
     @Override
     public boolean activate(){
         if(!Tabs.getCurrent().equals(Tabs.INVENTORY)){
             Tabs.INVENTORY.open();
         }
         boneClearer();
         Globals.emergencyTeleport();
 
         Globals.Loot = GroundItems.getNearest(Globals.ID_ITEMS_HORN);
 
         if(Inventory.isFull() && Inventory.contains(Globals.ID_ITEMS_LOBSTER)){
             Inventory.getItem(Globals.ID_ITEMS_LOBSTER).getWidgetChild().interact("Eat");
         }
         return(Globals.Loot != null && !Inventory.isFull());
     }
 
     @Override
     public void execute(){
         while(activate()){
             Globals.emergencyTeleport();
 
             if(Globals.Loot != null){
                 if(!altIsOnScreen(Globals.Loot)){
                     BlackUnicornKiller.status = "Walking towards Loot";
                     Walking.walk(Globals.Loot);
                     BlackUnicornKiller.status = "Grabbing Loot.";
                     Camera.turnTo(Globals.Loot);
                     if(altInteract(Globals.Loot, "Take", Globals.Loot.getGroundItem().getName())){
                         invChangeSleep();
                     }
 
                     while(Globals.me.isMoving()){
                         sleep(50,100);
                     }
                 } else {
                     if(Calculations.distanceTo(Globals.Loot)>=2){
                         BlackUnicornKiller.status = "Turning Camera to Loot.";
                         altCameraTurnTo(Globals.Loot);
                     }
                     BlackUnicornKiller.status = "Grabbing Loot.";
                     if(altInteract(Globals.Loot, "Take", Globals.Loot.getGroundItem().getName())){
                         invChangeSleep();
                     }
                     while(Globals.me.isMoving()){
                         sleep(50,100);
                     }
                 }
             }
             BlackUnicornKiller.actualProfit= BlackUnicornKiller.actualProfit + (Globals.HornPrice);
         }
     }
 
 
 }
