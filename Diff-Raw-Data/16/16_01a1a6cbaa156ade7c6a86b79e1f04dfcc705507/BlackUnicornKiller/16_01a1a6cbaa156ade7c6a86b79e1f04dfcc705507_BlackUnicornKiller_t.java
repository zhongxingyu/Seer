 package BlackUnicornKiller;
 
 import BlackUnicornKiller.Nodes.BankingHandlers.BankingHandler;
 import BlackUnicornKiller.Nodes.CombatHandlers.ActionBarHandler;
 import BlackUnicornKiller.Nodes.CombatHandlers.AttackHandler;
 import BlackUnicornKiller.Nodes.CombatHandlers.LootHandler;
 import BlackUnicornKiller.Nodes.Globals;
 import BlackUnicornKiller.Nodes.WalkingHandlers.TeleportToBankHandler;
 import BlackUnicornKiller.Nodes.WalkingHandlers.TeleportToUnicornsHandler;
 import BlackUnicornKiller.Nodes.WalkingHandlers.WalkToBankHandler;
 import BlackUnicornKiller.Nodes.WalkingHandlers.WalkToUnicornsHandler;
 import org.powerbot.core.Bot;
 import org.powerbot.core.event.listeners.PaintListener;
 import org.powerbot.core.script.ActiveScript;
 import org.powerbot.core.script.job.Task;
 import org.powerbot.core.script.job.state.Node;
 import org.powerbot.core.script.job.state.Tree;
 import org.powerbot.game.api.Manifest;
 import org.powerbot.game.api.methods.Game;
 import org.powerbot.game.api.methods.input.Mouse;
 import org.powerbot.game.api.methods.interactive.NPCs;
 import org.powerbot.game.api.methods.widget.Camera;
 import org.powerbot.game.api.methods.widget.WidgetCache;
 import org.powerbot.game.api.util.Random;
 import org.powerbot.game.api.util.Timer;
 import org.powerbot.game.api.wrappers.Entity;
 import org.powerbot.game.client.Client;
 
 import java.awt.*;
 
 
 @Manifest(name = "Black Unicorn Killer", authors = "tails111", description = "Kills Black Unicorns, and collects horns.", version = 1.0, hidden = true)
 public class BlackUnicornKiller extends ActiveScript implements PaintListener {
 
     private Client client = Bot.client();
     public static long startTime = System.currentTimeMillis();
     private int getPerHour(final int value){
         return (int) (value * 3600000D / (System.currentTimeMillis() - startTime));
     }
 
     private Tree script = new Tree(new Node[]{
             new WalkToBankHandler(),
             new BankingHandler(),
             new TeleportToUnicornsHandler(),
             new WalkToUnicornsHandler(),
             new AttackHandler(),
             new LootHandler(),
             new TeleportToBankHandler()
     });
 
     public static int actualProfit = 0;
     public static int postedProfitPerMath = 0;
     public static int postedHorns = 0;
     public static int postedHornsPerMath = 0;
     public static int postedTimeRun = 0;
 
     public static String status = "Hello Stephen.";
     public static Entity interacting = null;
 
     @Override
     public void onRepaint(Graphics g1){
 
         postedProfitPerMath = getPerHour(actualProfit);
         postedHornsPerMath = getPerHour(postedHorns);
         postedTimeRun = getPerHour(1000);
 
         String second =""+ ((System.currentTimeMillis() - startTime) / 1000) % 60;
         String minute =""+ ((System.currentTimeMillis() - startTime) / (1000 * 60)) % 60;
         String hour = ""+((System.currentTimeMillis() - startTime) / (1000 * 60 * 60)) % 24;
 
         String time=hour+":"+minute+":"+second;
         Graphics2D g = (Graphics2D)g1;
 
         g.setColor(Color.ORANGE);
         int mouseY = (int) Mouse.getLocation().getY();
         int mouseX = (int) Mouse.getLocation().getX();
         g.drawLine(mouseX - 999, mouseY + 999, mouseX + 999, mouseY - 999);
         g.drawLine(mouseX + 999, mouseY + 999, mouseX - 999, mouseY - 999);
         g.drawOval(mouseX-4,mouseY-4, 8, 8);
         g.setColor(Color.GREEN);
         g.fillOval(mouseX-2,mouseY-2,5,5);
 
         Font fontNormal = new Font("Comic Sans MS", Font.PLAIN, 12);
         Font fontTitle = new Font("Comic Sans MS", Font.BOLD, 12);
         g.setFont(fontTitle);
         g.setColor(Color.BLACK);
         g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
         g.fillRect(10, 75, 200, 84);
         g.setColor(Color.CYAN);
         g.drawLine(10, 75, 10, 159);//LEFT
         g.drawLine(10,75,210,75);//TOP
         g.drawLine(210,75,210,159);//RIGHT
         g.drawLine(10,159,210,159);//BOTTOM
         //x1,y1,x2,y2
         g.drawString("    Black Unicorn Killer",11,90);
         g.setFont(fontNormal);
         g.drawString(("Status: " + status), 15, 105);
         g.drawString(("Time Run: " + time), 15, 122);
         g.drawString(("Profit: " + actualProfit + "(" + postedProfitPerMath + ")"), 15, 139);
         g.drawString(("Horns/H: "+ postedHorns + "(" + postedHornsPerMath + ")"),15, 156);
         g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.2f));
 
        Globals.interacting = Globals.me.getInteracting();
 
         if(Globals.interacting != null){
             if(Globals.interacting.getHealthPercent() >= 75){
                 g.setColor(Color.GREEN);
             } else if(Globals.interacting.getHealthPercent() >= 50){
                 g.setColor(Color.YELLOW);
             } else if(Globals.interacting.getHealthPercent() >= 25){
                 g.setColor(Color.ORANGE);
             } else {
                 g.setColor(Color.RED);
             }
 
             for(final Polygon p : Globals.interacting.getBounds()){
                 g.fillPolygon(p);
             }
         }
     }
 
     @Override
     public void onStart(){
         Mouse.setSpeed(Mouse.Speed.VERY_FAST);
         Camera.setPitch(Random.nextInt(37,45));
 
         do{
             sleep(50,75);
         }while(Globals.me == null);
 
         //Timer timeCheck = new Timer(15000);
 
         //do{
         //    if(ActionBarHandler.abilityReady(1)){
         //        ActionBarHandler.executeAbility(1);
         //    }
         //    if(Globals.me.getMessage().matches("Momentum is now active.")){
         //        break;
         //    }
          //   Task.sleep(50,75);
         //}while(timeCheck.isRunning() && !Globals.me.getMessage().matches("Momentum is now active."));
     }
 
     @Override
     public int loop(){
         final Node stateNode = script.state();
         if(stateNode != null && Game.isLoggedIn()){
             script.set(stateNode);
             final Node setNode = script.get();
             if(setNode != null){
                 getContainer().submit(setNode);
                 setNode.join();
             }
         }
 
         if (client != Bot.client()) {
             WidgetCache.purge();
             Bot.context().getEventManager().addListener(this);
             client = Bot.client();
         }
 
         return Random.nextInt(650, 800);
     }
 }
