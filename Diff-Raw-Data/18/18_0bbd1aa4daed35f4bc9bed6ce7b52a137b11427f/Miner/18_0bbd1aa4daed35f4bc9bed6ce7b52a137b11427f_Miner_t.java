 package com.miners.ironminer;
 
 import com.miners.ironminer.nodes.deposit.DepositOres;
 import com.miners.ironminer.nodes.deposit.WalkDeposit;
 import com.miners.ironminer.nodes.mine.MineOres;
 import com.miners.ironminer.nodes.mine.WalkMine;
 import com.miners.ironminer.utils.Vars;
 
import org.powerbot.core.Bot;
 import org.powerbot.core.event.events.MessageEvent;
 import org.powerbot.core.event.listeners.MessageListener;
 import org.powerbot.core.event.listeners.PaintListener;
 import org.powerbot.core.script.ActiveScript;
 import org.powerbot.core.script.job.state.Branch;
 import org.powerbot.core.script.job.state.Node;
 import org.powerbot.core.script.job.state.Tree;
 import org.powerbot.core.script.methods.Game;
 import org.powerbot.game.api.Manifest;
 import org.powerbot.game.api.methods.input.Mouse;
 import org.powerbot.game.api.methods.tab.Inventory;
 import org.powerbot.game.api.methods.tab.Skills;
 import org.powerbot.game.api.methods.widget.Camera;
 import org.powerbot.game.api.methods.widget.WidgetCache;
 import org.powerbot.game.api.util.Random;
 import org.powerbot.game.api.util.net.GeItem;
 import org.powerbot.game.api.wrappers.Tile;
 import org.powerbot.game.bot.Context;
 import org.powerbot.game.client.Client;
 
 import java.awt.*;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.geom.RoundRectangle2D;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.LinkedList;
 
 /**
  * Created with IntelliJ IDEA.
  * User: coldasice
  * Date: 23/04/13
  * Time: 13:07
  */
 // Shortcut keys Warp ctrl+W, for experssions ctrl+j, for surrounds ctrl+alt+j, shift+f6 for rename alle kjapt
 
 @SuppressWarnings("unused")
 @Manifest(authors = {"CoLdAsIcE"}, name = "Iron Miner pac", description = "Mines iron and use deposit box in port sarim", version = 1.7)
 public  class Miner extends ActiveScript implements PaintListener, MessageListener, MouseListener {
     //todo  Package, add gui, add dropper(regular and action bar).
 
     //manifest
     private final ActiveScript instance = this;
     private final Manifest manifest = (Manifest) instance.getClass().getAnnotation(Manifest.class);
     @SuppressWarnings("deprecation")
    private Client client = Bot.client();
 
     Node[] nodeList;
     private final java.util.List<Node> jobsCollection = Collections.synchronizedList(new ArrayList<Node>());
     private Tree jobContainer = null;
 
     public synchronized final void provide(final Node... jobs) {
         for (final Node job : jobs) {
             if (!jobsCollection.contains(job)) {
                 jobsCollection.add(job);
             }
         }
         jobContainer = new Tree(jobsCollection.toArray(new Node[jobsCollection.size()]));
     }
 
     public void onStart() {
         startTime = System.currentTimeMillis();
         startExp = Skills.getExperience(Skills.MINING);
         startLvl = Skills.getRealLevel(Skills.MINING);
         int ironOre = 440;
         orePrice = GeItem.lookup(ironOre).getPrice();
         provide(new Mining(), new Banking());
         //
     }
 
     @Override
     public int loop() {
         if (Camera.getPitch() < 100 & Game.isLoggedIn()) {
             Camera.setPitch(100);
         }
        // taken from X303 :P
 
         if (Game.getClientState() != Game.INDEX_MAP_LOADED) {
             return 1000;
         }
 
        if (client != Bot.client()) {
             WidgetCache.purge();
            Bot.context().getEventManager().addListener(this);
            client = Bot.client();
         }
         //check if Tree is constructed
         if (jobContainer != null) {
         /*Gets first Node / Branch (state of Tree) that activates (remember priority system explained above)
         or null if none of Nodes/Branches activates*/
             final Node job = jobContainer.state();
             if (job != null) {
                 //Sets the current Node to the running state
                 jobContainer.set(job);
                 //Sets job (Node) to work
                 getContainer().submit(job);
                 //Attempt to pause calling thread till execution of job is done
                 job.join();
             }
         }
 
         return Random.nextInt(10, 50);
     }
 
     public class Mining extends Branch {
 
         public Mining() {
             super(new Node[]{
 
                     new MineOres(),
                     new WalkMine(),
 
             });
         }
 
         @Override
         public boolean branch() {
             return !Inventory.isFull();
         }
     }
 
     public class Banking extends Branch {
 
         public Banking() {
             super(new Node[]{
                     new WalkDeposit(),
                     new DepositOres()
             });
         }
 
         @Override
         public boolean branch() {
             return Inventory.isFull();
         }
     }
 
 
     @Override
     public void messageReceived(MessageEvent e) {
 
         String txt = e.getMessage();
         if (txt.contains("You manage to mine")) {
             oresMined++;
         }
     }
     private class MousePathPoint extends Point { // All credits to Enfilade
 
         private long finishTime;
         private double lastingTime;
 
         public MousePathPoint(int x, int y, int lastingTime) {
             super(x, y);
             this.lastingTime = lastingTime;
             finishTime = System.currentTimeMillis() + lastingTime;
         }
 
         public boolean isUp() {
             return System.currentTimeMillis() > finishTime;
         }
     }
 
     //START: Code generated using Enfilade's Easel
     private final Color transBlack = new Color(0, 0, 0, 150);
     private final Color transBlack2 = new Color(0, 0, 0, 230);
     private final Color C_BLACK = new Color(0, 0, 0);
     private final Color C_WHITE = new Color(255, 255, 255);
     private final Color C_RED = new Color(255, 0, 51);
     private final Color C_GREEN = new Color(51, 255, 51);
     private final BasicStroke stroke1 = new BasicStroke(1);
     private final Font font1 = new Font("Arial", Font.PLAIN, 32);
     private final Font font2 = new Font("Arial", Font.PLAIN, 10);
 
     private final LinkedList<MousePathPoint> mousePath = new LinkedList<MousePathPoint>();
     private final RoundRectangle2D CLOSE = new RoundRectangle2D.Float(497, 393, 23, 23, 16, 16);
     private final RoundRectangle2D OPEN = new RoundRectangle2D.Float(480, 395, 23, 23, 16, 16);
     private final Rectangle CHAT = new Rectangle(1, 506, 511, 23);
     boolean HIDE_PAINT = false;
     boolean HIDE_CHAT = false;
     Point p;
 
     //calc vars;
     private static long startTime;
     private static int startExp;
     private static int startLvl;
     private static int oresMined = 0;
     private static int currentLvL = 0;
     private static int orePrice;
 
     @Override
     public void onRepaint(Graphics g1) {
         Graphics2D g = (Graphics2D) g1;
 
         // mouse trail taken from somewhere on forum :P
         while (!mousePath.isEmpty() && mousePath.peek().isUp())
             mousePath.remove();
         Point clientCursor = Mouse.getLocation();
         MousePathPoint mpp = new MousePathPoint(clientCursor.x, clientCursor.y,
                 300);
         if (mousePath.isEmpty() || !mousePath.getLast().equals(mpp))
             mousePath.add(mpp);
         MousePathPoint lastPoint = null;
         for (MousePathPoint a : mousePath) {
             if (lastPoint != null) {
                 g.setColor(Color.GREEN);
                 g.drawLine(a.x, a.y, lastPoint.x, lastPoint.y);
             }
             lastPoint = a;
         }
         // for testing purpose :P
         for (final Tile t : Vars.MINE_AREA.getTileArray()) {
             if (t != null) {
                 if (t.isOnScreen()) {
                     g.setColor(Color.BLACK);
                     t.draw(g);
                 }
             }
         }
         if (Vars.PAINT_ROCK != null) {
             g.setColor(new Color(67, 24, 7));
             Vars.PAINT_ROCK.draw(g);
         }
         if (Vars.PAINT_NEXT_ROCK != null) {
             g.setColor(Color.blue);
             Vars.PAINT_NEXT_ROCK.draw(g);
         }
 
         // mouse crosshair taken from somewhere on forum :P
         g.setColor(Mouse.isPressed() ? Color.RED : Color.GREEN);
         final Point m = Mouse.getLocation();
         g.drawLine(m.x - 5, m.y + 5, m.x + 5, m.y - 5);
         g.drawLine(m.x - 5, m.y - 5, m.x + 5, m.y + 5);
 
 
         int totXpforNewLvL = Skills.getExperienceRequired(currentLvL + 1) - Skills.getExperienceRequired(currentLvL);
         int levelsGained = currentLvL - startLvl;
         currentLvL = Skills.getRealLevel(Skills.MINING);
         int expGained = Skills.getExperience(Skills.MINING) - startExp;
         int expToLevel = Skills.getExperienceToLevel(Skills.MINING, currentLvL + 1);
 
         int percent = (int) (((double) totXpforNewLvL - (double) expToLevel) / (double) totXpforNewLvL * 100D);
 
         int orePriceTot;
         if (oresMined == 0) {
             orePriceTot = orePrice;
         } else {
 
             orePriceTot = oresMined * orePrice;
         }
         long millis = System.currentTimeMillis() - startTime;
         long hours = millis / (1000 * 60 * 60);
         millis -= hours * (1000 * 60 * 60);
         long minutes = millis / (1000 * 60);
         millis -= minutes * (1000 * 60);
         long seconds = millis / 1000;
 
         float xpsec = 0;
         if ((minutes > 0 || hours > 0 || seconds > 0) && expGained > 0) {
             xpsec = ((float) expGained) / (float) (seconds + (minutes * 60) + (hours * 60 * 60));
         }
         float xpmin = xpsec * 60;
         float xphour = xpmin * 60;
 
         float oresec = 0;
         if ((minutes > 0 || hours > 0 || seconds > 0) && oresMined > 0) {
             oresec = ((float) oresMined) / (float) (seconds + (minutes * 60) + (hours * 60 * 60));
 
         }
 
         float oremin = oresec * 60;
         float orehour = oremin * 60;
 
         g.setColor(C_BLACK);
         g.setStroke(stroke1);
         g.fillRect(-7, 2, 776, 47);
         g.drawRect(-7, 2, 776, 47);
         g.setFont(font1);
         g.setColor(C_WHITE);
         g.drawString(manifest.name() + " v " + manifest.version(), 284, 44);
 
         Vars.Status STATUS = Vars.Status.STATUS;
 
         if (Game.getClientState() == 11) {
             if (!HIDE_PAINT) {
                 g.setColor(C_BLACK);
                 g.fillRect(5, 392, 507, 117);
                 g.setStroke(stroke1);
                 g.drawRect(5, 392, 507, 117);
                 g.setColor((HIDE_CHAT) ? transBlack : transBlack2);
                 g.fillRect(1, 506, 511, 23);
                 g.drawRect(1, 506, 511, 23);
                 g.setColor(C_WHITE);
                 g.setFont(font2);
                 g.drawString("Runtime: " + hours + " : " + minutes + " : " + seconds, 10, 412);
                 g.drawString("Xp gain: " + expGained + " exp/h:" + (int) xphour, 10, 422);
                 g.drawString("Ores mined: " + oresMined + " Ores/h " + (int) orehour, 10, 432);
                 g.drawString("Status: " + STATUS.getStatus(), 10, 442);
                 g.drawString("Iron Price: " + orePrice, 10, 452);
                 g.drawString("Bank Runs: " + Vars.bankrun, 10, 462);
                 g.drawString("Level Gained: " + levelsGained, 252, 412);
                 g.drawString("Current Level: " + currentLvL, 252, 422);
                 g.drawString("LvL when started: " + startLvl, 252, 432);
                 g.drawString("Exp To next lvl: " + expToLevel, 252, 442);
                 g.drawString("% to LvL: ", 252, 452);
                 g.drawString("Money Earned: " + orePriceTot, 252, 462);
                 g.setColor(Color.white);
                 g.fillRoundRect(299, 444, 100, 10, 15, 15); //these must be on same cordinates
                 g.setColor(Color.BLUE);
                 g.fillRoundRect(299, 444, percent, 10, 15, 15); //these must be on same cordinates
                 g.setColor(Color.red);
                 g.drawString("" + percent + "%", 341, 453); //this must be in the center of the bar
                 g.setColor(Color.BLACK);
                 g.drawRoundRect(299, 444, 100, 10, 15, 15); //these must be on same cordinates
                 g.setColor(Color.black);
 
                 //CLOSE buttn
                 g.setColor(C_GREEN);
                 g.fillOval(497, 393, 15, 14);
                 g.setColor(C_BLACK);
                 g.drawOval(497, 393, 15, 14);
 
             }
             else
             {
                 g.setColor(C_RED);
                 g.fillOval(480, 395, 15, 14);
                 g.setColor(C_BLACK);
                 g.drawOval(480, 395, 15, 14);
 
             }
         }
     }
 
     @Override
     public void mouseClicked(MouseEvent e) {
         p = e.getPoint();
         if (CLOSE.contains(p) && !HIDE_PAINT) {
             HIDE_PAINT = true;
         } else if (OPEN.contains(p) && HIDE_PAINT) {
             HIDE_PAINT = false;
         } else if (CHAT.contains(p) && !HIDE_CHAT) {
             HIDE_CHAT = true;
         } else if (CHAT.contains(p) && HIDE_CHAT) {
             HIDE_CHAT = false;
         }
     }
 
     @Override
     public void mousePressed(MouseEvent e) {
 
     }
 
     @Override
     public void mouseReleased(MouseEvent e) {
 
     }
 
     @Override
     public void mouseEntered(MouseEvent e) {
 
     }
 
     @Override
     public void mouseExited(MouseEvent e) {
 
     }
 }
