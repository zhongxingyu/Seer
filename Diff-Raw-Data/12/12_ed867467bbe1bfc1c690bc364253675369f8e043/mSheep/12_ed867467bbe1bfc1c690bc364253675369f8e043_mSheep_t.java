 package mprz.textline;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.ResourceBundle;
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
     private static final ResourceBundle bundle = ResourceBundle.getBundle("mprz/textline/Bundle");
     
     public Noteblock sound;
     public ConsoleReader console;
     public PrintWriter out;
     public GameObject[] inventory;
     public String[] effects;
     public StringsCompleter strCompleter = new StringsCompleter(bundle.getString("COMMAND_HELP"), bundle.getString("COMMAND_INVENTORY"), bundle.getString("COMMAND_STOP"), bundle.getString("COMMAND_SHEEP"), bundle.getString("COMMAND_EFFECTS"), bundle.getString("COMMAND_LOOKOVER"), bundle.getString("COMMAND_USE"), bundle.getString("COMMAND_NINJA"));
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
         sound = new Noteblock();
         sound.setListenerPosition(50, 50);
         inventory = new GameObject[inventoryCount];
         effects = new String[effectsCount];
         console = new ConsoleReader();
         out = new PrintWriter(console.getOutput());
         console.setHandleUserInterrupt(true);
         console.addCompleter(strCompleter);
         sound.playSound("/instr1.wav", 25, 50);
         slowSay("MPRZ Tech Labs", Color.CYAN, 100);
         sound.playSound("/instr1.wav", 75, 50);
         slowSay(bundle.getString("ENGINE_INIT"), Color.RED, 100);
         loadingScreen();
     }
     
     public void stop() throws IOException {
         console.clearScreen();
         sheep();
         sound.playSound("/instr1.wav", 25, 50);
         slowSay("MPRZ Tech Labs", Color.CYAN, 100);
         sound.playSound("/instr1.wav", 75, 50);
         slowSay(bundle.getString("ENGINE_STOP"), Color.RED, 100);
        sleep(1000);
        say("");
        console.clearScreen();
         console.shutdown();
         sound.shutdown();
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
         slowSay(bundle.getString("ENGINE_NAME"), 50);
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
             line = console.readLine(color(bundle.getString("PROMPT"), Color.GREEN) + color("", Color.WHITE));
             console.setPrompt("");
             if (line.startsWith(bundle.getString("COMMAND_STOP"))) {
                 stop();
                 break;
             } else if (line.startsWith(bundle.getString("COMMAND_HELP"))) {
                 slowSay(color(bundle.getString("COMMAND_HELP"), Color.MAGENTA) + color(bundle.getString("COMMAND_HELP_DESCRIPTION"), Color.WHITE), 25);
                 slowSay(color(bundle.getString("COMMAND_STOP"), Color.MAGENTA) + color(bundle.getString("COMMAND_STOP_DESCRIPTION"), Color.WHITE), 25);
                 slowSay(color(bundle.getString("COMMAND_INVENTORY"), Color.MAGENTA) + color(bundle.getString("COMMAND_INVENTORY_DESCRIPTION"), Color.WHITE), 25);
                 slowSay(color(bundle.getString("COMMAND_EFFECTS"), Color.MAGENTA) + color(bundle.getString("COMMAND_EFFECTS DESCRIPTION"), Color.WHITE), 25);
                 slowSay(color(bundle.getString("COMMAND_SHEEP"), Color.MAGENTA) + color(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("mprz/textline/Bundle").getString("COMMAND_SHEEP_DESCRIPTION"), new Object[] {color("the Mysterious Magic of Green Unicorns from Parallel Universe Where Edison Never Existed", Color.CYAN)}), Color.WHITE), 50);
                 slowSay(color(bundle.getString("COMMAND_LOOKOVER"), Color.MAGENTA) + color(bundle.getString("COMMAND_LOOKOVER_DESCRIPTION"), Color.WHITE), 25);
                 slowSay(color(bundle.getString("COMMAND_USE"), Color.MAGENTA) + color(bundle.getString("COMMAND_USE_DESCRIPTION"), Color.WHITE), 25);
                 slowSay(color(bundle.getString("COMMAND_NINJA"), Color.MAGENTA) + color(bundle.getString("COMMAND_NINJA_DESCRIPTION"), Color.WHITE), 25);
             } else if (line.startsWith(bundle.getString("COMMAND_INVENTORY"))) {
                 for (GameObject iter: inventory) {
                     if (iter != null) {
                         if (iter.isVisible()) {
                             slowSay((color(iter.getCodename(), Color.MAGENTA) + " " + iter.getName()), 50);
                         }
                     }
                 }
             } else if (line.startsWith(bundle.getString("COMMAND_EFFECTS"))) {
                 if (!isNinjaVisible()) {
                     slowSay(bundle.getString("EFFECT_INVISIBILITY"), 50);
                 }
                 for (String iter: effects) {
                     if (iter != null) {
                         slowSay(iter, 50);
                     }
                 }
             } else if (line.startsWith(bundle.getString("COMMAND_SHEEP"))) {
                 sheep();
             } else if (line.startsWith(bundle.getString("COMMAND_USE"))) {
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
                 String useLine = console.readLine(color(bundle.getString("PROMPT_USE"), Color.GREEN) + color("", Color.WHITE));
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
                     slowSay(bundle.getString("COMMAND_USE_FAIL"), 50);
                 }
                 console.removeCompleter(agComp);
                 console.addCompleter(strCompleter);
             } else if (line.startsWith(bundle.getString("COMMAND_LOOKOVER"))) {
                 currentLoc.onLookover();
             } else if (line.startsWith(bundle.getString("COMMAND_NINJA"))) {
                 if (effects[0] == null) {
                     StringsCompleter ninjAbilComp = new StringsCompleter(bundle.getString("COMMAND_NINJA_COLLIDE"), bundle.getString("COMMAND_NINJA_INVISIBILITY"));
                     console.removeCompleter(strCompleter);
                     console.addCompleter(ninjAbilComp);
                     String abiLine = console.readLine(color(bundle.getString("PROMPT"), Color.GREEN) + color(bundle.getString("PROMPT_NINJA"), Color.MAGENTA) + color("", Color.WHITE));
                     console.setPrompt("");
                     if (abiLine.startsWith(bundle.getString("COMMAND_NINJA_COLLIDE"))) {
                         AggregateCompleter agComp = new AggregateCompleter();
                         for (GameObject iter: currentLoc.getObjectsList()) {
                             if (iter != null) {
                                 if (iter.isVisible())
                                 agComp.getCompleters().add(new StringsCompleter(iter.getCodename()));
                             }
                         }
                         console.removeCompleter(ninjAbilComp);
                         console.addCompleter(agComp);
                         String useLine = console.readLine(color(bundle.getString("PROMPT"), Color.GREEN) + color(bundle.getString("PROMPT_NINJA"), Color.MAGENTA) + color(bundle.getString("PROMPT_NINJA_COLLIDE"), Color.BLUE) + color("", Color.WHITE));
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
                             slowSay(bundle.getString("COMMAND_NINJA_COLLIDE_FAIL"), 50);
                         }
                         console.removeCompleter(agComp);
                         console.addCompleter(strCompleter);
                     } else if (abiLine.startsWith(bundle.getString("COMMAND_NINJA_INVISIBILITY"))) {
                         sound.playSound("/chem1.wav", 50, 50);
                         if (isNinjaVisible()) {
                             slowSay(bundle.getString("COMMAND_NINJA_INVISIBILITY_TURNON"), 100);
                         } else {
                             slowSay(bundle.getString("COMMAND_NINJA_INVISIBILITY_TURNOFF"), 100);
                         }
                         setNinjaVisible(!isNinjaVisible());
                     }
                 } else {
                     slowSay(bundle.getString("COMMAND_NINJA_FAIL"), Color.YELLOW, 50);
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
         slowSay(bundle.getString("LOADING"), Color.WHITE, 50, false);
         slowSay("|||||||||||||||||||||||||]", Color.WHITE, 25);
         sleep(2000);
     }
 }
