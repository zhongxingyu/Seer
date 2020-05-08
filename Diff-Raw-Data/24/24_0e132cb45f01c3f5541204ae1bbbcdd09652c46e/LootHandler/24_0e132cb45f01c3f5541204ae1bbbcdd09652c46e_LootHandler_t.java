 package BlackUnicornKiller.Nodes.CombatHandlers;
 
 import BlackUnicornKiller.BlackUnicornKiller;
 import BlackUnicornKiller.Jobs.Job;
 import BlackUnicornKiller.Nodes.Globals;
 
 import org.powerbot.script.methods.Hud;
 import org.powerbot.script.util.Delay;
 import org.powerbot.script.util.Random;
 import org.powerbot.script.util.Timer;
 import org.powerbot.script.wrappers.Actor;
 import org.powerbot.script.wrappers.GroundItem;
 import org.powerbot.script.methods.MethodContext;
 import org.powerbot.script.wrappers.Item;
 import org.powerbot.script.wrappers.Tile;
 
 public class LootHandler extends Job {
 
     public LootHandler (MethodContext ctx){
         super(ctx);
     }
 
     GroundItem loot;
     Item extraFood;
     Actor me;
 
     public GroundItem nilGround = ctx.groundItems.getNil();
     public Item nilItem = ctx.backpack.getNil();
 
 
     private GroundItem checkGround(GroundItem tGround){
         if(tGround==null){
             return ctx.groundItems.getNil();
         }
         return tGround;
     }
 
     private Item checkItem(Item tItem){
         if(tItem==null){
             return ctx.backpack.getNil();
         }
         return tItem;
     }
 
     public void altCameraTurnTo(GroundItem e){
         Timer timeCheck = new Timer(Random.nextInt(2800, 3400));
         do{
             ctx.camera.setYaw(ctx.camera.getYaw() + Random.nextInt(35, 55));
             if(e == nilGround){
                 break;
             }
        }while(!altIsOnScreen(e) && timeCheck.isRunning());
     }
 
     public boolean invChangeSleep(){
         Timer timeCheck = new Timer(Random.nextInt(1200,1600));
         int tempInvCount = ctx.backpack.select().count();
         int newInvCount;
         do {
             BlackUnicornKiller.status="Waiting for Inv Change: " +timeCheck.getRemaining();
             Delay.sleep(20, 50);
             newInvCount = ctx.backpack.select().count();
             if(timeCheck.getRemaining()<=100){return false;}
         }while(tempInvCount==newInvCount && timeCheck.isRunning());
         return true;
     }
 
     public boolean altIsOnScreen(GroundItem e){
         return(e!=null && e.isOnScreen());
     }
 
 
     public void boneAndCharmClearer(){
         Item bones=ctx.backpack.getNil();
         Item charms=ctx.backpack.getNil();;
         for (Item tempBones : ctx.backpack.select().id(Globals.ID_ITMES_BONES).first()){bones=checkItem(tempBones);}
             if(bones.isValid()){
                 bones.interact("Drop");
             }
         for(int i=0; i<=Globals.ID_CHARMS.length-1; i++){
             for (Item tempCharms : ctx.backpack.select().id(Globals.ID_CHARMS[i]).first()){charms=checkItem(tempCharms);}
             if(charms.isValid()){
                 charms.interact("Drop");
             }
         }
     }
 
     public boolean emergencyTeleport(){
         Actor me = ctx.players.local();
         if(me!=null){
             if(me.getHealthPercent()<=70){
                 Timer timeCheck = new Timer(6000);
                 do{
                     for(Item tab : ctx.backpack.select().id(Globals.ID_ITEMS_FALLYTAB).first()){
                         if(tab.isValid()){
                             tab.interact("Break");
                         }else
                             break;
                     }
                 }while(me.getHealthPercent()<=70 && timeCheck.isRunning());
 
 
                 Timer timeCheck4 = new Timer(60000);
                 do{
                     if(!me.isInCombat()){
                         if(!ctx.widgets.get(1092,0).isVisible()){
                             ctx.widgets.get(640, 113).click(true);
                             Delay.sleep(750, 1250);
                         }
                         if(ctx.widgets.get(1092,0).isVisible()){
                             ctx.widgets.get(1092,45).click(true);
                             Timer timeCheck3 = new Timer(15000);
                             do{
                                 Delay.sleep(1000);
                             }while(me.getLocation().distanceTo(Globals.TILE_LOAD_EDGEVILLE)>=5 && timeCheck3.isRunning());
                         }
                     }
                 }while(me.getLocation().distanceTo(Globals.TILE_LOAD_EDGEVILLE)>=5 && timeCheck4.isRunning());
 
                 Timer timeCheck5 = new Timer(Random.nextInt(6000,8500));
                 do{
                     ctx.movement.newTilePath(Globals.PATH_BANK_TO).traverse();
                 }while(me.getLocation().distanceTo((Globals.TILE_BANK))>=5 && timeCheck5.isRunning());
 
                 return true;
             }
         }
         return false;
     }
 
     public boolean closeToUnicorns(){
         Tile distanceToUnicornsTile;
         for(int i=0; i<=Globals.unicornPacePath.length-1; i++){
             distanceToUnicornsTile = Globals.unicornPacePath[i];
             if(ctx.players.local().getLocation().distanceTo(distanceToUnicornsTile)<=18){
                 return(ctx.players.local().getLocation().distanceTo(distanceToUnicornsTile)<=18);
             }
         }
         return false;
     }
 
 
     public boolean activate(){
         me = ctx.players.local();
 
         if(!ctx.hud.isOpen(Hud.Window.BACKPACK)){
             ctx.hud.open(Hud.Window.BACKPACK);
         }
         boneAndCharmClearer();
         emergencyTeleport();
 
         for (GroundItem tempLoot : ctx.groundItems.select().id(Globals.ID_ITEMS_HORN).nearest().first()){loot=checkGround(tempLoot);}
 
             if(ctx.backpack.select().count() >= 27){
                 for (Item tempExtraFood : ctx.backpack.select().id(Globals.ID_ITEMS_LOBSTER)){extraFood=checkItem(tempExtraFood);}
                 if(extraFood!=nilItem && extraFood!=null){
                     extraFood.interact("Eat");
                 }
             }
         System.out.println("---Loot handler Activate----");
         System.out.println("Inv count <=28: " + (ctx.backpack.select().count()<=27));
         System.out.println("Loot!=nilGround : " + (loot!=nilGround));
         System.out.println("Close to Unis: " + closeToUnicorns());
         System.out.println("--------------------------------------");
         return(loot != nilGround && loot != null && ctx.backpack.select().count()<=27 && closeToUnicorns());
     }
 
     public void execute(){
             System.out.println("Loot handler ACTIVATED.");
             emergencyTeleport();
          //  for (GroundItem tempLoot : ctx.groundItems.select().id(Globals.ID_ITEMS_HORN).nearest().first()){loot=checkGround(tempLoot);}
             me=ctx.players.local();
                 if(loot != nilGround || loot != null){
                    if(me.getLocation().distanceTo(loot)>=10){
                         BlackUnicornKiller.status = "Walking towards Loot";
                        ctx.movement.stepTowards(loot);
                         while(me.isInMotion()){
                             me = ctx.players.local();
                             Delay.sleep(50,100);
                         }
                         BlackUnicornKiller.status = "Turning Camera to Loot";
                         altCameraTurnTo(loot);
                         if(loot.interact("Take")){
                             invChangeSleep();
                         }
                         while(me.isInMotion()){
                             me = ctx.players.local();
                             Delay.sleep(50,100);
                         }
                     } else {
                         BlackUnicornKiller.status = "Grabbing Loot.";
                         if(loot.interact("Take")){
                             invChangeSleep();
                         }
                         while(me.isInMotion()){
                             Delay.sleep(50,100);
                         }
                     }
                 }
         loot=nilGround;
 
         BlackUnicornKiller.actualProfit= BlackUnicornKiller.actualProfit + (Globals.HornPrice);
                 BlackUnicornKiller.postedHorns= BlackUnicornKiller.postedHorns + 1;
     }
 }
