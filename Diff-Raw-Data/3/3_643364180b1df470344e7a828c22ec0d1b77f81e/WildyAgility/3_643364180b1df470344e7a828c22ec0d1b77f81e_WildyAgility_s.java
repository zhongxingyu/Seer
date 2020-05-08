 import org.powerbot.core.event.events.MessageEvent;
 import org.powerbot.core.event.listeners.MessageListener;
 import org.powerbot.core.event.listeners.PaintListener;
 import org.powerbot.core.script.ActiveScript;
 import org.powerbot.core.script.util.Random;
 import org.powerbot.game.api.Manifest;
 import org.powerbot.game.api.methods.Calculations;
 import org.powerbot.game.api.methods.Walking;
 import org.powerbot.game.api.methods.input.Mouse;
 import org.powerbot.game.api.methods.interactive.Players;
 import org.powerbot.game.api.methods.node.Menu;
 import org.powerbot.game.api.methods.node.SceneEntities;
 import org.powerbot.game.api.methods.tab.Inventory;
 import org.powerbot.game.api.methods.tab.Skills;
 import org.powerbot.game.api.methods.widget.Camera;
 import org.powerbot.game.api.util.Timer;
 import org.powerbot.game.api.wrappers.Area;
 import org.powerbot.game.api.wrappers.Tile;
 import org.powerbot.game.api.wrappers.map.Path;
 import org.powerbot.game.api.wrappers.node.Item;
 import org.powerbot.game.api.wrappers.node.SceneObject;
 
 import javax.imageio.ImageIO;
 import java.awt.*;
 import java.io.IOException;
 import java.net.URL;
 import java.text.NumberFormat;
 import java.util.concurrent.TimeUnit;
 
 /**
  * @author: Mentor_Altair
  */
 
 @Manifest(name = "Mentor's Wilderness Agility", authors = {"Mentor_Altair"}, description = "Completes the Wilderness Agility Course", version = 1.0)
 public class WildyAgility extends ActiveScript implements PaintListener, MessageListener {
 
     public void onStart() {
         stage = 1;
         img1 = getImage("http://i1336.photobucket.com/albums/o654/Mentor_Altair/wildyagility_zpsab00e945.png");
         mouseImg = getImage("http://i427.photobucket.com/albums/pp360/restlesso5/cursors/wendys-pink-skull-cursor.png");
         startExp = Skills.getExperience(Skills.AGILITY);
         startTime = System.currentTimeMillis();
     }
 
 
 
     @Override
     public int loop() {
 
         if (item(lobster) != null && Players.getLocal().getHealthPercent() <= 20) {
             interact(item(lobster), "Eat");
         }
         checkStatus();
         antiban();
         return random(150, 250);
     }
 
     int startExp, exp;
     long startTime, timeRan, timeTNL;
     String status = "Loading...";
     int eatAt = 1500;
     int squueezePipe = 65362;
     int animation1 = 10580;
     Tile tile1 = new Tile (3004,3937,0);
 
     int ropeSwing = 64696;
     String message2 = "You skilfully";
     Tile tile2 = new Tile (3005,3953,0);
 
     int steppingStone = 64699;
     String message3 = "and reach the other side";
     Tile tile3 = new Tile (3002,3960,0);
 
     int logBalance = 64698;
     String message4 = "across the gap.";
     Tile tile4 = new Tile (3002 ,3945,0);
 
     int cliffSide = 65734;
     int animation5 = 3378;
     Tile tile5 = new Tile (2994,3939,0);
 
     int ladder = 32015;
     Tile ladderTile = new Tile (3005, 10362,0);
     int stage = 0;
     int ready = 0;
     int lobster = 379;
 
     Timer walkTimer = new Timer (1500);
     Timer animationTimer = new Timer (1000);
 
     SceneObject nearest(final int id) {
         return SceneEntities.getNearest(id);
     }
 
     Item item(final int id) {
         return Inventory.getItem(id);
     }
 
     int random(int num1, int num2) {
         return Random.nextInt(num1, num2);
     }
 
     boolean inArea(Area a) {
         return a.contains(Players.getLocal().getLocation().getX(), Players.getLocal().getLocation().getY());
     }
 
     void interact(Item i, String action) {
         if (i != null) {
             i.getWidgetChild().interact(action);
             sleep(random(600, 1000));
         }
     }
 
     void interact(SceneObject o, String action, Tile t) {
         if (!Players.getLocal().isIdle() && !Players.getLocal().isInCombat()){
             animationTimer.reset();
             walkTimer.reset();
         }
 
         if (o != null) {
             status = "Interacting with: " + o.getDefinition().getName();
             if (o.isOnScreen()) {
                 if (animationTimer.getRemaining() <= 0) {
                     if (o.click(false)) {
                         if (Menu.contains(action)) {
                             Menu.select(action);
                             sleep(random(400, 1200));
                         } else {
                             Mouse.move(random(0, 800), random(0, 600));
                         }
                     }
                 }
             } else {
                 if (walkTimer.getRemaining() > 0) {
                     return;
                 }
 
                 if (Calculations.distanceTo(t) >= 6){
                     Path p = Walking.findPath(t);
                     p.traverse();
                 }
 
                 if (Walking.walk(t)){
                         walkTimer.reset();
                 }
             }
         }
     }
 
     void doCourse() {
         switch (stage) {
             case 1:
                 interact(nearest(squueezePipe), "Squeeze-through", tile1);
                 if (Players.getLocal().getAnimation() == animation1){
                 stage = 2;
                 }
                 break;
             case 2:
                 interact(nearest(ropeSwing), "Swing-on", tile2);
                 break;
             case 3:
                 interact(nearest(steppingStone), "Cross", tile3);
                 break;
             case 4:
                 interact(nearest(logBalance), "Walk-across", tile4);
                 break;
             case 5:
                 interact(nearest(cliffSide), "Climb", tile5);
                 if (Players.getLocal().getAnimation() == animation5){
                     stage = 1;
                 }
                 break;
         }
     }
 
     void checkStatus () {
         if (Calculations.distanceTo(ladderTile) <= 30){
             interact(nearest(ladder), "Climb", ladderTile);
         } else {
             doCourse();
         }
     }
     //START: Code generated using Enfilade's Easel
     private Image getImage(String url) {
         try {
             return ImageIO.read(new URL(url));
         } catch(IOException e) {
             return null;
         }
     }
 
     private final Color color1 = new Color(255, 255, 255);
 
     private final Font font1 = new Font("Lithos Pro Regular", 0, 18);
 
     private Image img1, mouseImg;
 
     public void onRepaint(Graphics g1) {
         timeRan = System.currentTimeMillis() - startTime;
         exp = Skills.getExperience(Skills.AGILITY);
         timeTNL = (long) ((double) Skills.getExperienceToLevel(Skills.AGILITY, Skills.getRealLevel(Skills.AGILITY) + 1) / (double) ph(exp - startExp) * 3600000);
 
         Graphics2D g = (Graphics2D)g1;
         g.drawImage(img1, -5, 330, null);
         g.setFont(font1);
         g.setColor(color1);
         g.drawString(fn(exp - startExp) + " ( " +fn(ph(exp - startExp)) + " )", 133, 433);
         g.drawString(tf(timeTNL), 211, 452);
         g.drawString(status, 97, 472);
         g.drawString(tf(timeRan), 121, 492);
 
         g.drawImage(mouseImg, Mouse.getX(), Mouse.getY(), null);
     }
     //END: Code generated using Enfilade's Easel
 
     private int ph(int arg0) {
         int num = (int) (3600000.0 / timeRan * arg0);
         return num;
     }
 
     private String fn(int n) {
         return NumberFormat.getInstance().format(n);
     }
 
     private String tf(long duration) {
         String res = "";
         long days = TimeUnit.MILLISECONDS.toDays(duration);
         long hours = TimeUnit.MILLISECONDS.toHours(duration)
                 - TimeUnit.DAYS.toHours(TimeUnit.MILLISECONDS.toDays(duration));
         long minutes = TimeUnit.MILLISECONDS.toMinutes(duration)
                 - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS
                 .toHours(duration));
         long seconds = TimeUnit.MILLISECONDS.toSeconds(duration)
                 - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS
                 .toMinutes(duration));
         if (days == 0) {
             res = (hours + ":" + minutes + ":" + seconds);
         } else {
             res = (days + ":" + hours + ":" + minutes + ":" + seconds);
         }
         return res;
     }
     private void antiban() {
         int rand = Random.nextInt(0, 500);
         switch (rand) {
             case 1:
                 Mouse.move(Random.nextInt(0, 700), Random.nextInt(0, 500));
                 break;
            case 2:
                Camera.setPitch(Random.nextBoolean());
                break;
             case 3:
                 Camera.setPitch(Random.nextInt(1, 100));
                 break;
             case 4:
                 Camera.setAngle(Random.nextInt(1, 360));
                 break;
             default:
                 break;
         }
     }
 
 
     @Override
     public void messageReceived(MessageEvent messageEvent) {
         String msg = messageEvent.getMessage();
         if (msg.contains(message2)){
              stage = 3;
         }
         if (msg.contains(message3)){
              stage = 4;
         }
         if (msg.contains(message4)){
              stage = 5;
         }
     }
 }
