 import org.powerbot.concurrent.Task;
 import org.powerbot.concurrent.strategy.Condition;
 import org.powerbot.concurrent.strategy.Strategy;
 import org.powerbot.game.api.ActiveScript;
 import org.powerbot.game.api.Manifest;
 import org.powerbot.game.api.methods.Widgets;
 import org.powerbot.game.api.methods.input.Mouse;
 import org.powerbot.game.api.methods.interactive.NPCs;
 import org.powerbot.game.api.methods.interactive.Players;
 import org.powerbot.game.api.methods.node.Menu;
 import org.powerbot.game.api.methods.node.SceneEntities;
 import org.powerbot.game.api.methods.tab.Inventory;
 import org.powerbot.game.api.methods.tab.Skills;
 import org.powerbot.game.api.util.Random;
 import org.powerbot.game.api.util.Time;
 import org.powerbot.game.api.wrappers.interactive.NPC;
 import org.powerbot.game.api.wrappers.node.Item;
 import org.powerbot.game.api.wrappers.node.SceneObject;
 import org.powerbot.game.api.wrappers.widget.WidgetChild;
 import org.powerbot.game.bot.event.MessageEvent;
 import org.powerbot.game.bot.event.listener.MessageListener;
 import org.powerbot.game.bot.event.listener.PaintListener;
 
 import java.awt.*;
 import java.io.IOException;
 import java.net.URI;
 import java.net.URISyntaxException;
 
 @Manifest(authors = {"Soviet"}, name = "RedexHerblore", description = "Flawless Herblore", version = 1.2)
 public class RedexHerblore extends ActiveScript implements PaintListener,MessageListener {
 
     public final static int[] BANK_IDS = {82, 2213, 2995, 5276, 6084, 10517,
             11402, 11758, 12759, 14367, 19230, 20325, 24914, 25808, 26972,
             29085, 52589, 34752, 35647, 36786, 2012, 2015, 2019, 42377, 42217,
             42378, 693, 4483, 12308, 20607, 21301, 27663, 42192};
     public final static int[] BANK_NPCS = {44, 45, 496, 497, 494, 495, 498,
             499, 909, 958, 1036, 2271, 2354, 2355, 2718, 3293, 3416, 3218,
             3824, 5488, 5901, 4456, 4457, 4458, 4459, 5912, 5913, 6362, 6532,
             6533, 6534, 6535, 7605, 8948, 9710, 14367};
 
     private static int herb, primary, secondary;
     private final Object[][] HERBS =  {{ 199, "Guam" },
             { 201, "Marrentil" }, { 203, "Tarromin" },
             { 205, "Harralander" }, { 207, "Ranarr" }, { 209, "Irit" },
             { 211, "Avantoe" }, { 213, "Kwuarm" }, { 215, "Cadantine" },
             { 2485, "Lantadyme" }, { 217, "Dwarf Weed" },
             { 221, "Torstol" }, { 3049, "Toadflax" },
             { 12174, "Spirit Weed" }, { 14836, "Wergali" },
             { 3051, "Snapdragon" }, { 21626, "Fellstalk" } };
 
     private final Object[][] POTS =  {{91, 221, "Attack Potion"},
             {93,235,"Antipoison"},{95, 225, "Strength Potion"},
             {97,223, "Restore Potion"},{97, 1975, "Energy Potion"},
             {99,239, "Defence Potion"},{3002,2152, "Agility Potion"},
             {97, 9736, "Combat Potion"}, {99, 231,"Prayer Potion"},
             {101,221,"Super Attack Potion"},
             {101,235,"Super Antipoison"}, {103, 231,"Fishing Potion"},
             {103,2970, "Super Energy Potion"}, {103,10111,"Hunter Potion"},
             {105,225, "Super Strength Potion"},{14856,11525,"Fletching Potion"},
             {105,241,"Weapon Poison"},{3004,223,"Super Restore Potion"},
             {107,239,"Super Defence"}, {2483,241,"Antifire"}, {109,245,"Ranging Potion"},
             {2483,3138,"Magic Potion"}, {111,247,"Zamorak Brew"},{3002,6693,"Saradomin Brew"},
             {21628,21622,"Prayer Renewal"},{95,592,"Serum 207"},{227,249,"Unfinished Guam"},
             {227,251,"Unfinished Marrentill"},{227,253,"Unfinished Tarromin"},
             {227,255,"Unfinished Harralander"},{227,257,"Unfinished Ranarr"},
             {227,3099,"Unfinished Toadflax",3002},{227,12224,"Unfinished Spirit Weed"},
             {227,14886,"Unfinished Wergali"},{227,259,"Unfinished Irit"},
             {227,211,"Unfinished Avantoe"},{227,263,"Unfinished Kwuarm"},
             {227,3101,"Unfinished Snapdragon"},{227,265,"Unfinished Cadantine"},
             {227,2535,"Unfinished Lantadyme"},{227,267,"Unfinished Dwarf Weed"},
             {227,271,"Unfinished Torstol"},{227,21676,"Unfinished Fellstalk"}};
 
     private int herbsCleaned = 0, potionsMixed = 0;
 
     String status = "";
     public static String activity;
     public static String item;
     int startExp;
     long startTime;
     public static boolean start = false, invoked = false, guiSelection = false, cleaning = false;
 
     private double getVersion() {
        return 1.2;
     }
 
     @Override
     protected void setup() {
 
         ShowGUI show = new ShowGUI();
         provide(new Strategy(show, show));
         WaitGUI wait = new WaitGUI();
         provide(new Strategy(wait, wait));
         Sof sof = new Sof();
         provide(new Strategy(sof,sof));
         Clean clean = new Clean();
         provide(new Strategy(clean, clean));
         CleanBank cleanbank = new CleanBank();
         provide(new Strategy(cleanbank, cleanbank));
         MixBank mixbank = new MixBank();
         provide(new Strategy(mixbank, mixbank));
         Mix mix = new Mix();
         provide(new Strategy(mix,mix));
 
         startExp = Skills.getExperiences()[15];
         startTime = System.currentTimeMillis();
     }
 
     private class ShowGUI implements Task, Condition {
         public boolean validate() {
             return !invoked;
         }
 
 
         public void run() {
             log.info("we inside showgui nigga");
             try {
                 new GUI().setVisible(true);
 
                 while (!start) {
                     Time.sleep(500);
                 }
             } catch (Exception e) {
                 e.printStackTrace();
             }
             invoked = true;
         }
     }
 
     private class WaitGUI implements Task, Condition {
         public boolean validate() {
             return !start;
         }
 
 
         public void run() {
             log.info("Waiting for the user to complete the gui");
             Time.sleep(1000);
         }
     }
 
     private final class Sof extends Strategy implements Task {
 
         @Override
         public boolean validate() {
             return Inventory.getCount(24154) > 0;
         }
 
         @Override
         public void run() {
             for (Item i : Inventory.getItems()) {
                 if (i.getId() == 24154) {
                     i.getWidgetChild().interact("Claim");
                     Time.sleep(Random.nextInt(200, 300));
                 }
             }
         }
     }
 
 
     private class Clean implements Task, Condition {
         public boolean validate() {
             return cleaning && Inventory.getCount(herb) > 0;
         }
 
         public void run() {
             status = "Cleaning";
             for (Item i : Inventory.getItems()) {
                 if (i.getId() == herb) {
                     i.getWidgetChild().click(true);
                     herbsCleaned++;
                 }
             }
         }
     }
 
     private class CleanBank implements Task, Condition {
         public boolean validate() {
             return cleaning && Inventory.getCount(herb) == 0;
         }
 
         public void run() {
             status = "Banking(cleaning)";
 
             if (!bankIsOpen()) {
                 openBank();
                 Time.sleep(Random.nextInt(1000, 2000));
             }
 
             if(bankIsOpen()){
                 if(Inventory.getCount() > 0){
                     depositAll();
                     Time.sleep(2000);
                 }
                 if (withdraw(herb, 0)) {
                     Time.sleep(Random.nextInt(750, 1000));
                 }
                 else {
                     log.info("You are out of herbs to clean");
                     stop();
                 }
 
                 if (Inventory.getCount() == 28) {
                     closeBank();
                     Time.sleep(Random.nextInt(500, 1000));
                 }
             }
         }
     }
 
     private class MixBank implements Task, Condition {
         public boolean validate() {
             return !cleaning && ((bankIsOpen() || (Inventory.getCount(primary) == 0 ||  Inventory.getCount(secondary) == 0)));
         }
 
         public void run() {
             status = "Banking(mixing)";
 
             if (!bankIsOpen()) {
                 openBank();
                 Time.sleep(Random.nextInt(1000, 2000));
             }
             if(bankIsOpen()){
                 if(Inventory.getCount() > 0){
                     depositAll();
                     Time.sleep(2000);
                 }
                 if (withdraw(primary, 14)) {
                     Time.sleep(Random.nextInt(750, 1000));
                 } else {
                     log.info("Primary ingredient not in bank");
                     stop();
                 }
                 if (withdraw(secondary, 0)) {
                     Time.sleep(Random.nextInt(750, 1000));
                 } else {
                     log.info("Secondary ingredient not in bank");
                     stop();
                 }
 
                 if (Inventory.getCount() == 28) {
                     closeBank();
                     Time.sleep(Random.nextInt(500, 1000));
                 }
             }
         }
     }
 
     private class Mix implements Task, Condition {
         public boolean validate() {
             return !bankIsOpen() && !cleaning && Inventory.getCount(primary) > 0 && Inventory.getCount(secondary) > 0 ;
         }
 
         public void run() {
             status = "Mixing";
             Item i = Inventory.getItemAt(Random.nextInt(0,13));
             if (i.getId() == primary) {
                 i.getWidgetChild().interact("Use");
             }
             i = Inventory.getItemAt(Random.nextInt(14,27));
             if (i.getId() == secondary) {
                 i.getWidgetChild().interact("Use");
             }
             Time.sleep(Random.nextInt(1000,1500));
 
             if(Widgets.get(905).getChild(14).isVisible() && Widgets.get(905).getChild(14).validate()){
                 Widgets.get(905).getChild(14).click(true);
                 Time.sleep(Random.nextInt(1000,2000));
 
                 while(Players.getLocal().getAnimation() != -1 || (Inventory.getCount() < 28 && Inventory.getCount() > 15)){
                     Time.sleep(Random.nextInt(100,200));
                 }
             }
         }
     }
 
     private boolean bankIsOpen() {
         return Widgets.get(762).getChild(45).isVisible();
     }
 
     /**
      * AIO BANKING!
      */
 
     private void openBank() {
         boolean canBreak = false;
         for (SceneObject loc : SceneEntities.getLoaded()) {
             for (int i : BANK_IDS) {
                 if (loc.getId() == 42192) {
                     loc.click(true);
                     break;
                 }
                 if(loc.getId() == i){
                     loc.click(false);
                     if(Menu.contains("Bank")){
                         loc.interact("Bank");
                     }
                     else{
                         loc.interact("Use");
                     }
                     canBreak = true;
                     break;
                 }
             }
             if (canBreak)
                 break;
         }
         for (NPC npc : NPCs.getLoaded()) {
             for (int i : BANK_NPCS) {
                 if (npc.getId() == i) {
                     npc.interact("bank");
                     canBreak = true;
                     break;
                 }
             }
             if (canBreak)
                 break;
         }
     }
 
     public boolean withdraw(int id, int amount) {
         WidgetChild[] bank = Widgets.get(762, 95).getChildren();
         for (WidgetChild child : bank) {
             if (child.getChildId() == id) {
                 Mouse.click(child.getRelativeX() + 40,
                         child.getRelativeY() + 100, false);
                 if (amount == 0)
                     return Menu.select("Withdraw-All");
                 if (amount == 1)
                     return Menu.select("Withdraw-1");
                 if (amount == 5)
                     return Menu.select("Withdraw-5");
                 if (amount == 10)
                     return Menu.select("Withdraw-10");
                 if (amount == 14)
                     return Menu.select("Withdraw-14");
             }
         }
         return false;
     }
 
     private void closeBank() {
         Widgets.get(762).getChild(45).click(true);
     }
 
     private void depositAll() {
         Widgets.get(762).getChild(33).interact("Deposit");
     }
 
 
     public String formatTime(final long milliseconds) {
         final long t_seconds = milliseconds / 1000;
         final long t_minutes = t_seconds / 60;
         final long t_hours = t_minutes / 60;
         final long seconds = t_seconds % 60;
         final long minutes = t_minutes % 60;
         final long hours = t_hours % 500;
         return hours + ":" + minutes + ":" + seconds;
     }
 
     public String formatXP(long paramLong) {
         if (paramLong >= 1000000000L) {
             return paramLong / 1000000000L + "BIL";
         }
         if (paramLong >= 10000000L) {
             return paramLong / 1000000L + "MIL";
         }
         if (paramLong >= 1000L) {
             return paramLong / 1000L + "K";
         }
         return "" + paramLong;
     }
 
     public void onRepaint(Graphics g) {
         long xpHour = (long) (3600000.0 / (System.currentTimeMillis() - startTime) * (Skills.getExperiences()[15] - startExp));
         g.setColor(new Color(0, 0, 0));
         g.fillRect(547, 404, 191, 61);
         g.setColor(new Color(255, 255, 255));
         g.setFont(new Font("Arial", 1, 10));
         g.drawString("RedexHerblore by Soviet v" + getVersion(), 555, 415);
         g.drawString("Status : " + status, 555, 460);
         g.drawString("" + formatTime(System.currentTimeMillis() - startTime), 555, 445);
         if(cleaning){
             g.drawString("Herbs Cleaned : " + herbsCleaned, 600, 445);
         }
         else{
             g.drawString("Potions Mixed : " + potionsMixed, 600, 445);
         }
         g.drawString("XP/H : " + formatXP(xpHour), 555, 430);
     }
 
     public class GUI extends javax.swing.JFrame {
 
         public GUI() {
             initComponents();
         }
 
         private void initComponents(){
 
             jLabel1 = new javax.swing.JLabel();
             jLabel2 = new javax.swing.JLabel();
             jTabbedPane2 = new javax.swing.JTabbedPane();
             jPanel2 = new javax.swing.JPanel();
             jLabel6 = new javax.swing.JLabel();
             jComboBox1 = new javax.swing.JComboBox();
             jPanel3 = new javax.swing.JPanel();
             jLabel7 = new javax.swing.JLabel();
             jComboBox2 = new javax.swing.JComboBox();
             jButton1 = new javax.swing.JButton();
             jButton2 = new javax.swing.JButton();
 
             setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
 
             jLabel1.setText("Redex Herblore");
 
             jLabel2.setText("By Soviet");
 
             jLabel6.setText("Select the potion you would like to make");
 
             jComboBox1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "", "Attack Potion","Antipoison","Strength Potion","Restore Potion",
                     "Energy Potion", "Defence Potion","Agility Potion","Combat Potion", "Prayer Potion","Crafting Potion", "Super Attack Potion",
                     "Super Antipoison", "Fishing Potion","Super Energy Potion", "Hunter Potion", "Super Strength Potion", "Fletching Potion", "Weapon Poison",
                     "Super Restore Potion", "Super Defence", "Antifire", "Ranging Potion", "Magic Potion", "Zamorak Brew","Saradomin Brew", "Prayer Renewal",
                     "Unfinished Guam","Unfinished Marrentill","Unfinished Harralander","Unfinished Ranarr","Unfinished Toadflax","Unfinished Spirit Weed",
                     "Unfinished Wergali","Unfinished Irit","Unfinished Avantoe","Unfinished Kwuarm","Unfinished Snapdragon","Unfinished Cadantine","Unfinished Lantadyme",
                     "Unfinished Dwarf Weed","Unfinished Torstol","Unfinished Fellstalk","Serum 207", "Unfinished Tarromin"}));
             jComboBox1.addActionListener(new java.awt.event.ActionListener() {
                 public void actionPerformed(java.awt.event.ActionEvent evt) {
                     jComboBox1ActionPerformed(evt);
                 }
             });
 
             javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
             jPanel2.setLayout(jPanel2Layout);
             jPanel2Layout.setHorizontalGroup(
                     jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addGroup(jPanel2Layout.createSequentialGroup()
                                     .addGap(20, 20, 20)
                                     .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                             .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 186, javax.swing.GroupLayout.PREFERRED_SIZE)
                                             .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 202, javax.swing.GroupLayout.PREFERRED_SIZE))
                                     .addContainerGap(22, Short.MAX_VALUE))
             );
             jPanel2Layout.setVerticalGroup(
                     jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addGroup(jPanel2Layout.createSequentialGroup()
                                     .addContainerGap()
                                     .addComponent(jLabel6)
                                     .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                     .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                     .addContainerGap(40, Short.MAX_VALUE))
             );
 
             jTabbedPane2.addTab("Mix Potions", jPanel2);
 
             jLabel7.setText("Select the herb you would like to clean");
 
             jComboBox2.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "", "Ranarr", "Guam","Cadantine", "Dwarf Weed", "Avantoe", "Toadflax", "Tarromin", "Torstol", "Snapdragon", "Marrentil", "Kwuarm", "Lantadyme", "Irit", "Harralander", "Spirit Weed", "Wergali" }));
             jComboBox2.addActionListener(new java.awt.event.ActionListener() {
                 public void actionPerformed(java.awt.event.ActionEvent evt) {
                     jComboBox2ActionPerformed(evt);
                 }
             });
 
             javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
             jPanel3.setLayout(jPanel3Layout);
             jPanel3Layout.setHorizontalGroup(
                     jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addGroup(jPanel3Layout.createSequentialGroup()
                                     .addGap(38, 38, 38)
                                     .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                             .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, 185, javax.swing.GroupLayout.PREFERRED_SIZE)
                                             .addComponent(jLabel7))
                                     .addContainerGap(21, Short.MAX_VALUE))
             );
             jPanel3Layout.setVerticalGroup(
                     jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addGroup(jPanel3Layout.createSequentialGroup()
                                     .addContainerGap()
                                     .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                                     .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                     .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                     .addContainerGap(40, Short.MAX_VALUE))
             );
 
             jTabbedPane2.addTab("Clean Herbs", jPanel3);
 
             jButton1.setText("Visit Thread");
             jButton1.addActionListener(new java.awt.event.ActionListener() {
                 public void actionPerformed(java.awt.event.ActionEvent evt) {
                     jButton1ActionPerformed(evt);
                 }
             });
 
             jButton2.setText("Start Script");
             jButton2.setToolTipText("");
             jButton2.addActionListener(new java.awt.event.ActionListener() {
                 public void actionPerformed(java.awt.event.ActionEvent evt) {
                     jButton2ActionPerformed(evt);
                 }
             });
 
             javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
             getContentPane().setLayout(layout);
             layout.setHorizontalGroup(
                     layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                     .addContainerGap()
                                     .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                             .addGroup(layout.createSequentialGroup()
                                                     .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                     .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                     .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE))
                                             .addGroup(layout.createSequentialGroup()
                                                     .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                     .addGap(28, 28, 28)
                                                     .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE))
                                             .addComponent(jTabbedPane2))
                                     .addContainerGap())
             );
             layout.setVerticalGroup(
                     layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addGroup(layout.createSequentialGroup()
                                     .addContainerGap()
                                     .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                             .addComponent(jLabel1)
                                             .addComponent(jLabel2))
                                     .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                     .addComponent(jTabbedPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE)
                                     .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                     .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                             .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                             .addComponent(jButton2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
             );
 
             pack();
         }// </editor-fold>
 
         private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {
             try {
                 Desktop
                         .getDesktop()
                         .browse(
                                 new URI(
                                         "http://www.powerbot.org/community/topic/681186-redexherblore/"));
             } catch (IOException e) {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
             } catch (URISyntaxException e) {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
             }
         }
 
         private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {
             // Sets a default gui selection in the event that none is made
             if(!guiSelection){
                 log.info("You must select an option");
                 stop();
             }
             if(cleaning){
                 activity = "Herbs Cleaned: ";
                 item = "Herbs";
             }
             else{
                 activity = "Potions Mixed: ";
                 item = "Potions";
             }
             //Tells gui to dispose and start with the script
             start = true;
             this.dispose();
         }
 
         private void jComboBox1ActionPerformed(java.awt.event.ActionEvent evt) {
             String chosen = jComboBox1.getSelectedItem().toString();
 
             for(int i = 0;i < POTS.length; i++){
                 if(POTS[i][2].equals(chosen)){
                     primary = (Integer) POTS[i][0];
                     secondary = (Integer) POTS[i][1];
                 }
             }
             cleaning = false;
             guiSelection = true;
         }
 
 
         private void jComboBox2ActionPerformed(java.awt.event.ActionEvent evt) {
             String chosen = jComboBox2.getSelectedItem().toString();
 
             for(int i = 0;i < HERBS.length; i++){
                 if(HERBS[i][1].equals(chosen)){
                     herb = (Integer) HERBS[i][0];
                 }
             }
             cleaning = true;
             guiSelection = true;
         }
     }
 
     // Variables declaration - do not modify
 
     private javax.swing.JButton jButton1;
     private javax.swing.JButton jButton2;
     private javax.swing.JComboBox jComboBox2;
     private javax.swing.JComboBox jComboBox1;
     private javax.swing.JLabel jLabel1;
     private javax.swing.JLabel jLabel2;
     private javax.swing.JLabel jLabel6;
     private javax.swing.JLabel jLabel7;
     private javax.swing.JPanel jPanel2;
     private javax.swing.JPanel jPanel3;
     private javax.swing.JTabbedPane jTabbedPane2;
     // End of variables declaration
 
     public void messageReceived(MessageEvent e) {
         String x = e.getMessage().toLowerCase();
         if (x.contains("You clean the dirt from the")){
             herbsCleaned++;
         }
         if(x.contains("you put the") || x.contains("into the vial")
                 || x.contains("mix the")) {
             potionsMixed++;
         }
     }
 }
