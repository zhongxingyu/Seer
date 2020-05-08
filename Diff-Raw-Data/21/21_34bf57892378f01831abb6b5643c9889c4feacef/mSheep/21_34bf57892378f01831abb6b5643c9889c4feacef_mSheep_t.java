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
         slowSay("MPRZ Tech Labs", Color.CYAN, 100);
         slowSay("Transmission begun.", Color.RED, 100);
         loadingScreen();
     }
     
     public void stop() throws IOException {
         console.clearScreen();
         sheep();
         slowSay("MPRZ Tech Labs", Color.CYAN, 100);
         slowSay("Transmission done.", Color.RED, 100);
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
         slowSay("mSHEEP Text Game Engine", 50);
         say("", 50);
         slowSay("           /\\0", 25);
         slowSay("          /_ \\0", 25);
         slowSay("         /  _ \\@@@@@@@@@   @", 25);
         slowSay("        /\\    @#########@ @#@", 25);
         slowSay("        \\ \\/ @###########@###@", 25);
         slowSay("         \\  @#############@#@", 25);
         slowSay("          \\@###############@", 25);
         slowSay("          @###############@", 25);
         slowSay("          @###############@", 25);
         slowSay("           @#############@", 25);
         slowSay("            @###########@", 25);
         slowSay("             @#########@", 25);
         slowSay("              @@@@@@@@@", 25);
         slowSay("              /|      |\\", 25);
         slowSay("             / |      | \\", 25);
         slowSay("            /---      ---\\", 25);
         say("", 50);
         say("", 50);
         sleep(1000);
     }
     
     public void run() throws IOException {
         String line;
         while (true) {
             line = console.readLine(color("mprz: ", Color.GREEN) + color("", Color.WHITE));
            console.setPrompt("");
             if (line.startsWith("stop")) {
                 stop();
                 break;
             } else if (line.startsWith("help")) {
                 slowSay(color("help", Color.MAGENTA) + color("           shows help", Color.WHITE), 25);
                 slowSay(color("stop", Color.MAGENTA) + color("           exits game", Color.WHITE), 25);
                 slowSay(color("inventory", Color.MAGENTA) + color("      lists your character's inventory", Color.WHITE), 25);
                 slowSay(color("effects", Color.MAGENTA) + color("        lists physical and psychical effects affecting your character", Color.WHITE), 25);
                 slowSay(color("sheep", Color.MAGENTA) + color("          uses " + color("the Mysterious Magic of Green Unicorns from Parallel Universe Where Edison Never Existed", Color.CYAN) + " to draw a black hole in time-space continuum fabric", Color.WHITE), 50);
                 slowSay(color("lookover", Color.MAGENTA) + color("       asks your character's eyes for things it sees", Color.WHITE), 25);
                 slowSay(color("use", Color.MAGENTA) + color("            lets you use an object from the inventory or the environment", Color.WHITE), 25);
                 slowSay(color("ninja", Color.MAGENTA) + color("          use the ninja abilities", Color.WHITE), 25);
             } else if (line.startsWith("inventory")) {
                 for (GameObject iter: inventory) {
                     if (iter != null) {
                         if (iter.isVisible()) {
                             slowSay((color(iter.getCodename(), Color.MAGENTA) + " " + iter.getName()), 50);
                         }
                     }
                 }
             } else if (line.startsWith("effects")) {
                 if (!isNinjaVisible()) {
                     slowSay("You are invisible...", 50);
                 }
                 for (String iter: effects) {
                     if (iter != null) {
                         slowSay(iter, 50);
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
                 String useLine = console.readLine(color("mprz:use: ", Color.GREEN) + color("", Color.WHITE));
                console.setPrompt("");
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
                     slowSay("Can't use something that's not on the list!", 50);
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
                     String abiLine = console.readLine(color("mprz:", Color.GREEN) + color("ninja: ", Color.MAGENTA) + color("", Color.WHITE));
                    console.setPrompt("");
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
                         String useLine = console.readLine(color("mprz:", Color.GREEN) + color("ninja:", Color.MAGENTA) + color("collide: ", Color.BLUE) + color("", Color.WHITE));
                        console.setPrompt("");
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
                             slowSay("Can't ninja-collide with something that's not on the list!", 50);
                         }
                         console.removeCompleter(agComp);
                         console.addCompleter(strCompleter);
                     } else if (abiLine.startsWith("invisibility")) {
                         if (isNinjaVisible()) {
                             slowSay("You disappear in the darkness...", 100);
                         } else {
                             slowSay("You re-appear from the darkness...", 100);
                         }
                         setNinjaVisible(!isNinjaVisible());
                     }
                 } else {
                     slowSay("Hungry ninja is not ninja.", Color.YELLOW, 50);
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
     
     public void slowSay(String text, long interval) {
         slowSay(text, Color.WHITE, interval);
     }
     
     public void slowSay(String text, Color color, long interval) {
         slowSay(text, color, interval, true);
     }
     
     public void slowSay(String text, Color color, long interval, boolean ln) {
         out.print(color("", color));
         for (int i = 0; i < text.length(); i++) {
             out.print(text.charAt(i));
             out.flush();
             try {
                 Thread.sleep(interval);
             } catch (InterruptedException ex) {
                 Logger.getLogger(mSheep.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
         if (ln) {
             out.println();
         }
     }
     
    public void visionSay(String text) {
        String visText = "";
        for (int i = 0; i < text.length(); i++) {
            visText += color(text.substring(i, i + 1), randomColor());
        }
        slowSay(visText, 100);
    }
    
    public Color randomColor() {
        java.util.Random rand = new java.util.Random();
        return Color.values()[rand.nextInt(Color.values().length)];
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
         return "\u001B[" + color + "m" + text;
     }
     
     public String color(String text, Color color) {
         return color(text, color.fg());
     }
     
     public void loadingScreen() {
         slowSay("Loading [", Color.WHITE, 50, false);
         slowSay("|||||||||||||||||||||||||]", Color.WHITE, 25);
         sleep(2000);
     }
 }
