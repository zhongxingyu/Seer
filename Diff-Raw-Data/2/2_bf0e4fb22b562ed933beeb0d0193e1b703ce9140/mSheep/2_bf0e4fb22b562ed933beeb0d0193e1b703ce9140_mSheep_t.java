 package mprz.textline;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import jline.console.ConsoleReader;
 import jline.console.completer.AggregateCompleter;
 import jline.console.completer.StringsCompleter;
 import org.fusesource.jansi.Ansi.Color;
 
 /**
  *
  * @author michcioperz <michcioperz@gmail.com>
  */
 public class mSheep {
     
     public ConsoleReader console;
     public PrintWriter out;
     public GameObject[] inventory;
     public String[] effects;
     public StringsCompleter strCompleter = new StringsCompleter("help", "inventory", "stop", "sheep", "effects", "lookover", "use", "ninja");
     public Location currentLoc;
     private boolean ninjaVisible = true;
     
     private mSheep(int inventoryCount, int effectsCount) {
         try {
             this.init(inventoryCount, effectsCount);
         } catch (IOException ex) {
             Logger.getLogger(mSheep.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
     
     public static mSheep getInstance() {
         return mSheepHolder.INSTANCE;
     }
 
     private void init(int inventoryCount, int effectsCount) throws IOException {
         inventory = new GameObject[inventoryCount];
         effects = new String[effectsCount];
         console = new ConsoleReader();
         out = new PrintWriter(console.getOutput());
         console.setHandleUserInterrupt(true);
         console.addCompleter(strCompleter);
         say("MPRZ Tech Labs", Color.CYAN, 100);
         say("Transmission begun.", Color.RED, 100);
         sleep(1000);
         sheep();
     }
     
     public void stop() throws IOException {
         console.clearScreen();
         say("MPRZ Tech Labs", Color.CYAN, 100);
         say("Transmission done.", Color.RED, 100);
         sheep();
         console.shutdown();
         System.exit(0);
     }
     
     public void loadLocation(Location loc) {
         try {
             console.clearScreen();
             sheep();
         } catch (IOException ex) {
             Logger.getLogger(mSheep.class.getName()).log(Level.SEVERE, null, ex);
         }
         currentLoc = loc;
         currentLoc.onArrival();
     }
     
     public void sheep() {
         say("mSHEEP Text Game Engine", 500);
         say("", 50);
         say("           /\\0", 50);
         say("          /" + color("_", Color.GREEN) + " \\0", 50);
         say("         /  " + color("_", Color.GREEN) + " \\@@@@@@@@@   @", 50);
         say("        /\\    @#########@ @#@", 50);
         say("        \\ \\/ @###########@###@", 50);
         say("         \\  @#############@#@", 50);
         say("          \\@###############@", 50);
         say("          @###############@", 50);
         say("          @###############@", 50);
         say("           @#############@", 50);
         say("            @###########@", 50);
         say("             @#########@", 50);
         say("              @@@@@@@@@", 50);
         say("              /|      |\\", 50);
         say("             / |      | \\", 50);
         say("            /---      ---\\", 50);
         say("", 50);
         say("", 50);
         sleep(1000);
     }
     
     public void run() throws IOException {
         String line;
         while (true) {
             line = console.readLine(color("mprz: ", Color.GREEN));
             if (line.startsWith("stop")) {
                 stop();
                 break;
             } else if (line.startsWith("help")) {
                 say(color("help", Color.MAGENTA) + "           shows help");
                 say(color("stop", Color.MAGENTA) + "           exits game");
                 say(color("inventory", Color.MAGENTA) + "      lists your character's inventory");
                 say(color("effects", Color.MAGENTA) + "        lists physical and psychical effects affecting your character");
                 say(color("sheep", Color.MAGENTA) + "          uses " + color("the Mysterious Magic of Green Unicorns from Parallel Universe Where Edison Never Existed", Color.CYAN) + " to draw a black hole in time-space continuum fabric");
                 say(color("lookover", Color.MAGENTA) + "       asks your character's eyes for things it sees");
                 say(color("use", Color.MAGENTA) + "            lets you use an object from the inventory or the environment");
                 say(color("ninja", Color.MAGENTA) + "          use the ninja abilities");
             } else if (line.startsWith("inventory")) {
                 for (GameObject iter: inventory) {
                     if (iter != null) {
                         if (iter.isVisible()) {
                             say((color(iter.getCodename(), Color.MAGENTA) + " " + iter.getName()), 250);
                         }
                     }
                 }
             } else if (line.startsWith("effects")) {
                 if (!isNinjaVisible()) {
                     say("You are invisible...", 250);
                 }
                 for (String iter: effects) {
                     if (iter != null) {
                         say(iter, 250);
                     }
                 }
             } else if (line.startsWith("sheep")) {
                 sheep();
             } else if (line.startsWith("use")) {
                 AggregateCompleter agComp = new AggregateCompleter();
                 for (GameObject iter: inventory) {
                     if (iter != null) {
                         if (iter.isVisible())
                             agComp.getCompleters().add(new StringsCompleter(iter.getCodename()));
                     }
                 }
                 for (GameObject iter: currentLoc.getObjectsList()) {
                     if (iter != null) {
                         if (iter.isVisible())
                         agComp.getCompleters().add(new StringsCompleter(iter.getCodename()));
                     }
                 }
                 console.removeCompleter(strCompleter);
                 console.addCompleter(agComp);
                 String useLine = console.readLine(color("mprz:use: ", Color.GREEN));
                 boolean objUsed = false;
                 for (GameObject iter: inventory) {
                     if (iter != null && !objUsed) {
                         if (useLine.startsWith(iter.getCodename()) && iter.isVisible()) {
                             iter.onUse();
                             objUsed = true;
                         }
                     }
                 }
                 for (GameObject iter: currentLoc.getObjectsList()) {
                     if (iter != null && !objUsed) {
                         if (useLine.startsWith(iter.getCodename()) && iter.isVisible()) {
                             iter.onUse();
                             objUsed = true;
                         }
                     }
                 }
                 if (!objUsed) {
                     say("Can't use something that's not on the list!");
                 }
                 console.removeCompleter(agComp);
                 console.addCompleter(strCompleter);
             } else if (line.startsWith("lookover")) {
                 currentLoc.onLookover();
             } else if (line.startsWith("ninja")) {
                 if (effects[0] == null) {
                     StringsCompleter ninjAbilComp = new StringsCompleter("collide", "invisibility");
                     console.removeCompleter(strCompleter);
                     console.addCompleter(ninjAbilComp);
                     String abiLine = console.readLine(color("mprz:", Color.GREEN) + color("ninja: ", Color.MAGENTA));
                     if (abiLine.startsWith("collide")) {
                         AggregateCompleter agComp = new AggregateCompleter();
                         for (GameObject iter: currentLoc.getObjectsList()) {
                             if (iter != null) {
                                 if (iter.isVisible())
                                 agComp.getCompleters().add(new StringsCompleter(iter.getCodename()));
                             }
                         }
                         console.removeCompleter(ninjAbilComp);
                         console.addCompleter(agComp);
                         String useLine = console.readLine(color("mprz:", Color.GREEN) + color("ninja:", Color.MAGENTA) + color("collide: ", Color.BLUE));
                         boolean objUsed = false;
                         for (GameObject iter: inventory) {
                             if (iter != null && !objUsed) {
                                 if (useLine.startsWith(iter.getCodename()) && iter.isVisible()) {
                                     iter.onNinjaCollide();
                                     objUsed = true;
                                 }
                             }
                         }
                         for (GameObject iter: currentLoc.getObjectsList()) {
                             if (iter != null && !objUsed) {
                                 if (useLine.startsWith(iter.getCodename()) && iter.isVisible()) {
                                     iter.onNinjaCollide();
                                     objUsed = true;
                                 }
                             }
                         }
                         if (!objUsed) {
                             say("Can't ninja-collide with something that's not on the list!");
                         }
                         console.removeCompleter(agComp);
                         console.addCompleter(strCompleter);
                     } else if (abiLine.startsWith("invisibility")) {
                         if (isNinjaVisible()) {
                             say("You disappear in the darkness...");
                         } else {
                             say("You re-appear from the darkness...");
                         }
                         setNinjaVisible(!isNinjaVisible());
                     }
                 } else {
                     say("Hungry ninja is not ninja.");
                 }
             }
             sleep(500);
         }
     }
 
     /**
      * @return the ninjaVisible
      */
     public boolean isNinjaVisible() {
         return ninjaVisible;
     }
 
     /**
      * @param ninjaVisible the ninjaVisible to set
      */
     public void setNinjaVisible(boolean ninjaVisible) {
         this.ninjaVisible = ninjaVisible;
     }
     
     private static class mSheepHolder {
 
         private static final mSheep INSTANCE = new mSheep(4, 2);
     }
     
     public void sleep(long millis) {
         out.flush();
         try {
             Thread.sleep(millis);
         } catch (InterruptedException ex) {
             Logger.getLogger(mSheep.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
     
     public void say(String text, Color color, long time) {
        out.println(color(text, color));
         out.flush();
         sleep(time);
     }
     
     public void say(String text, long time) {
         say(text, Color.WHITE, time);
     }
     
     public void say(String text, Color color) {
         say(text, color, 0);
     }
     
     public void say(String text) {
         say(text, Color.WHITE);
     }
     
     public String color(String text, int color) {
         return "\u001B[" + color + "m" + text + "\u001B[" + Color.WHITE.fg() + "m";
     }
     
     public String color(String text, Color color) {
         return color(text, color.fg());
     }
 }
