 import api.methods.*;
 import bot.script.Script;
 import bot.script.ScriptManifest;
 
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.io.IOException;
 import java.net.URL;
 import java.util.*;
 import javax.imageio.ImageIO;
 import javax.swing.*;
 
 @ScriptManifest(authors = { "Dwarfeh" }, name = "Dwarfeh's Firemaker", version = 0.1, description = "I MAKEITY THE FIRE",  category = "Firemaking")
 public class DwarfehFiremaker extends Script {
     private String STATE;
 
     private long startTime = 0;
 
     private Point midScreen = new Point(259, 162);
     private Point middleMiniMap = new Point(627, 86);
 
     private Color banker = new Color(241, 230, 133);
     private Color tinderbox = new Color(92, 90, 87);
     private Color[] log = { new Color(131, 105, 58), new Color(154, 124, 87), new Color(89, 83, 59),
             new Color(76, 49, 10), new Color(116, 93, 52), new Color(48, 155, 143) }; //0= normal, 1= oak, 2= willow, 3= maple, 4= yew, 5= magic
     private Color bankIcon = new Color(102, 78, 13);
 
     //OLD MAPLE 120, 85, 38
 
     private double[] xpPerLog = {40, 60, 90, 135, 202.5, 303.8};
 
     private int logChosen;
     private int antibanAmount;
     private int mouseSpeed = 0;
     private int lagAdjust;
     private int logsLit;
     private int failToOpenBank = 0;
 
     private static final Rectangle WALKRIGHT = new Rectangle(680, 67, 15, 30);
     private static final Rectangle LOGSINBANKSPOT = new Rectangle(40, 95, 25, 25);
     private static final Rectangle SIXTHROW = new Rectangle(560, 395, 165, 25);
     private static final Rectangle SEVENTHROW = new Rectangle(560, 430, 165, 25);
     private static final Rectangle ONOFFSWITCH = new Rectangle(10, 314, 66, 22);
     private static final Rectangle TILENORTH = new Rectangle(245, 135, 30, 25);
     private static final Rectangle TILESOUTH = new Rectangle(240, 200, 35, 35);
 
 
     private boolean ignoreYouCan;
     private boolean tinderboxLight;
     private boolean guiOpened = true;
 
     private UserInterface ui;
 
     @Override
     public boolean onStart() {
         log("Starting up Dwarfeh's FireMaker");
         log("Please post feedback on the thread !");
         log("Remember it only works at Fist of Guthix !");
         STATE = "Starting up...";
         startTime = System.currentTimeMillis();
         //UI
         SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                 ui = new UserInterface();
                 ui.setVisible(true);
             }
         }
         );
         //end UI
         startTime = System.currentTimeMillis();
         return true;
     }
     
 
     @Override
     public void onFinish() {
         log("Thanks for using Dwarfeh's FireMaker");
     }
 
     boolean didDo = false;
     @Override
     public int loop() {
         try {
         if (mouseSpeed == 0){
             return 300;
         }
 
         Mouse.setSpeed(random(mouseSpeed - 1, mouseSpeed + 1));
        if (InventoryContainsTinderbox()) {
             if (InventoryContainsLog()) {
                 if (!StandsOnFire()) {
                     STATE = "Burning logs";
                     if (tinderboxLight) {
                         BurnOneLogWithTinderbox();
                     } else {
                         BurnOneLogWithRightClick();
                     }
                 } else {
                     STATE = "Can't light fire, moving";
                     WalkToOtherTile();
                 }
             } else {
                 if (NearBanker() || BankIsOpen()) {
                     if (BankIsOpen()) {
                         STATE = "Withdrawing logs";
                         WithdrawLogs();
                     } else {
                         STATE = "Opening bank";
                         OpenBank();
                     }
                 } else {
                     STATE = "Walking to banker";
                     WalkToBank();
                 }
             }
         } else {
             log("Can not spot a tinderbox in the inventory");
             STATE = "We don't have a tinderbox";
         }
         AntiBan();
         }    catch (Exception ignored) {}
         return 1;
     }
 
     private void WalkToOtherTile() {
         switch(random(1, 3)) {
             case 2:
                 Mouse.click(RandomPointInRect(TILENORTH));
                 sleep(1250 + lagAdjust, 1500 + lagAdjust);
                 break;
             
             default:
                 Mouse.click(RandomPointInRect(TILESOUTH));
                 sleep(1250 + lagAdjust, (1500 + lagAdjust));
                 break;
         }
     }
 
     private void AntiBan() {
         switch(random(0, antibanAmount)){
             case 1:
                 STATE = "Performing Anti-Ban";
                 Mouse.moveOffScreen();
             case 2:
                 STATE = "Performing Anti-Ban";
                 Mouse.moveSlightly();
         }
     }
 
     private void WalkToRight() {
         sleep(random(500 + lagAdjust, 750 + lagAdjust));
         Mouse.click(RandomPointInRect(WALKRIGHT));
         sleep(5000 + lagAdjust, 6500 + lagAdjust);
     }
 
     private void WalkToBank() {
         Point bankIconPos = BankIconOnMap();
         if (bankIconPos != null) {
             sleep(random(500 + lagAdjust, 750 + lagAdjust));
             Mouse.click(bankIconPos);
             for (int i = 0; i < 10; i++) {
                 boolean spottedBanker = NearBanker();
                 if (!spottedBanker) {
                     sleep(random(500 + lagAdjust, 600 + lagAdjust));
                 }
             }
         } else {
             WalkToRight();
         }
     }
 
     private Point BankIconOnMap() {
         return GetRandomPoint(bankIcon, 0.005D, 0, 75, middleMiniMap);
     }
 
     private boolean BankIsOpen() {
         return ColorIsInBounds(log[logChosen], 0.03D, 20, new Point(52, 107));
     }
 
     private void OpenBank() {
         Point bankerPos = PointByColorInBounds(banker, 0.07D, 250, midScreen);
         if (bankerPos != null) {
             RightClick(bankerPos);
             sleep(random(600 + lagAdjust, 900 + lagAdjust));
             Mouse.click(bankerPos.x + random(-10, 10), bankerPos.y + random(40, 50));
             failToOpenBank++;
             sleep(random(2000 + lagAdjust, 2350 + lagAdjust));
             if (failToOpenBank >= 3) {
                 failToOpenBank = 0;
                 WalkToBank();
             }
         }
     }
 
     private void WithdrawLogs() {
         failToOpenBank = 0;
         Point rClick = RandomPointInRect(LOGSINBANKSPOT);
         RightClick(rClick);
         sleep(600 + lagAdjust, 900 + lagAdjust);
         Mouse.click(rClick.x + random(-10, 10), rClick.y + random(105, 115));
         sleep(600 + lagAdjust, 900 + lagAdjust);
         WalkToRight();
     }
 
     private boolean StandsOnFire() {
         Rectangle yourBounds = new Rectangle(8, 442, 50, 12);
         String OCR = findNumberString(yourBounds);
         if ("Yocuca".equals(OCR) && !ignoreYouCan) {
             ignoreYouCan = true;
             return true;
         }
         ignoreYouCan = false;
         return false;
     }
 
     private boolean NearBanker() {
         return ColorIsInBounds(banker, 0.07D, 250);
     }
 
     private boolean ColorIsInBounds(Color COLOR, double TOLERANCE, double MAXDIST) {
         return ColorIsInBounds(COLOR, TOLERANCE, MAXDIST, midScreen);
     }
     private boolean ColorIsInBounds(Color COLOR, double TOLERANCE, double MAXDIST, Point MID) {
         Point POINT = PointByColorInBounds(COLOR, TOLERANCE, MAXDIST, MID);
         return POINT != null;
     }
 
     private Point PointByColorInBounds(Color COLOR, double TOLERANCE, double MAXDIST, Point MID) {
         Point NEAREST = null;
         double DIST = 0;
         java.util.List<Point> colorLocs = ImageUtil.getPointsWithColor(Game.getImage(), COLOR, TOLERANCE);
         for (Point POINT : colorLocs) {
             double distTmp = getDistanceBetween(POINT, MID);
             if (distTmp < MAXDIST) {
                 if (NEAREST == null) {
                     DIST = distTmp;
                     NEAREST = POINT;
                 } else if (distTmp < DIST) {
                     NEAREST = POINT;
                     DIST = distTmp;
                 }
             }
         }
         return NEAREST;
     }
 
     private Point GetRandomPoint(Color COLOR, double TOLERANCE, int MINDIST,int MAXDIST, Point MID) {
         java.util.List<Point> Locs = ImageUtil.getPointsWithColor(Game.getImage(), COLOR, TOLERANCE);
         Point RandomPoint = null;
         while (RandomPoint == null && Locs != null) {
             try {
                 Point RandomPointGuess = Locs.get(random(0, Locs.size()));
                 double DIST = getDistanceBetween(RandomPointGuess, MID);
                 if (DIST > MINDIST && DIST < MAXDIST) {
                     RandomPoint = RandomPointGuess;
                 }
             } catch (Throwable ignored) { }
         }
         return RandomPoint;
     }
 
     private void BurnOneLogWithTinderbox() {
         Point tinderboxPos = getSlotWithCenterColor(tinderbox, 5).getCenter();
         Point nextLog = getSlotWithCenterColor(log[logChosen], 5).getCenter();
         if (tinderboxPos != null && nextLog != null) {
             Mouse.click(tinderboxPos);
             sleep(500 + lagAdjust, 750 + lagAdjust);
             Mouse.click(nextLog);
             logsLit++;
             sleep(1150 + lagAdjust, 1400 + lagAdjust);
         }
     }
 
     private void BurnOneLogWithRightClick() {
         Point nextLog = getSlotWithCenterColor(log[logChosen], 5).getCenter();
         if (nextLog != null) {
             int yChange;
             if (PointInRect(nextLog, SIXTHROW)) {
                 yChange = 30;
             } else if (PointInRect(nextLog, SEVENTHROW)) {
                 yChange = -15;
             } else {
                 yChange = 45;
             }
             RightClick(nextLog);
             sleep(500 + lagAdjust, 750 + lagAdjust);
             Mouse.click(nextLog.x + random(-10, 10), nextLog.y + random(yChange - 3, yChange + 3));
             logsLit++;
             sleep(1250 + lagAdjust, 1500 + lagAdjust);
         }
     }
 
     public Inventory.Slot getSlotWithCenterColor(Color color, int tolerance) {
         for (Inventory.Slot a : Inventory.Slot.values()) {
             if (areColorsClose(a.getCenterColor(), color, 5)) {
                 return a;
             }
         }
         return null;
     }
 
     public boolean areColorsClose(Color color1, Color color2, int toleranceAmount) {
         return (color1.getRed() - color2.getRed() < toleranceAmount && color1.getRed() - color2.getRed() > -toleranceAmount) && (color1.getBlue() - color2.getBlue() < toleranceAmount && color1.getBlue() - color2.getBlue() > -toleranceAmount) && (color1.getGreen() - color2.getGreen() < toleranceAmount && color1.getGreen() - color2.getGreen() > -toleranceAmount);
     }
 
     private boolean InventoryContainsLog() {
         return ItemCountInInventory(log[logChosen], 0.01D) != 0;
     }
 
     private boolean InventoryContainsTinderbox() {
         return ItemCountInInventory(tinderbox, 0.015D) != 0;
     }
 
     private int ItemCountInInventory(Color COLOR, double TOLERANCE) {
         boolean[] clickedInvSpot = { false, false, false, false, false, false, false, false, false, false, false, false, false, false, false
                 , false, false, false, false, false, false, false, false, false, false, false, false, false };
         int count = 0;
         java.util.List<Point> plankLoc = ImageUtil.getPointsWithColor(Game.getImage(), COLOR, TOLERANCE);
         for (Point POINT : plankLoc) {
             for (int current = 0; current < 28; current++) {
                 if (PointInRect(POINT, Inventory.getSlotAt(current).getBounds()) && !clickedInvSpot[current]) {
                     count ++;
                     clickedInvSpot[current] = true;
                 }
             }
         }
         return count;
     }
 
     private boolean PointInRect(Point POINT, Rectangle RECT) {
         return POINT.x >= RECT.x && POINT.x <= (RECT.x + RECT.width) && POINT.y >= RECT.y && POINT.y <= (RECT.y + RECT.height);
     }
 
     private Point RandomPointInRect(Rectangle RECT) {
         return new Point(RECT.x + random(0, RECT.width), RECT.y + random(0, RECT.height));
     }
 
     public static void RightClick(Point POINT) {
         if (!Game.isPointValid(POINT.x, POINT.y)) {
             return;
         }
         if (!Mouse.getLocation().equals(new Point(POINT.x, POINT.y))) {
             Mouse.move(POINT.x, POINT.y);
         }
         Mouse.click(POINT.x, POINT.y, false);
     }
 
     private Image getImage(String url) {
         try {
             return ImageIO.read(new URL(url));
         } catch(IOException e) {
             return null;
         }
     }
 
 
     @Override
     public Graphics doPaint(Graphics g1) {
         Graphics2D g = (Graphics2D) g1;
         g.setStroke(new BasicStroke(6));
         g.setColor(Color.white);
         g.draw3DRect(1, 38, 140, 150, true);
         g.setStroke(new BasicStroke(3));
         g.setColor(new Color(0, 0, 0, 70));
         g.fill3DRect(1, 38, 140, 150, true);
         g.setColor(Color.white);
 
         g.setFont(new Font("Arial", 0, 9));
 
         g.drawRect(Mouse.getLocation().x, Mouse.getLocation().y, 10, 10);
 
         g.drawString("Dwarfeh's Firemaker", 10, 50);
 
         g.drawString("Running for " + SortTime(System.currentTimeMillis() - startTime), 10, 70);
 
         g.drawString("Status: " + STATE, 10, 90);
 
         g.drawString("Logs Lit: " + logsLit, 10, 110);
 
         g.drawString("XP Gained: " + RoundToK(logsLit * xpPerLog[logChosen]) + "K", 10, 130);
 
         g.drawString("Firemaking XP per hour: " + RoundToK(XpPerHour(logsLit * xpPerLog[logChosen], startTime)) + "K", 10, 150);
         return null;
     }
 
     private double RoundToK(Double ROUND) {
         return (Math.round(ROUND * 1000) / 1000) / 1000;
     }
 
     private double XpPerHour(double XP, long START) {
         return XP == 0 ? 0 : (int)(XP / ((System.currentTimeMillis() - START) / 1000L) * 3600.0D);
     }
 
     public static String SortTime(long millis){
         //Returns the current time
         long time = millis / 1000;
         String seconds = Integer.toString((int) (time % 60));
         String minutes = Integer.toString((int) ((time % 3600) / 60));
         String hours = Integer.toString((int) (time / 3600));
         for (int i = 0; i < 2; i++){
             if (seconds.length() < 2){
                 seconds = "0" + seconds;
             }
             if (minutes.length() < 2){
                 minutes = "0" + minutes;
             }
             if (hours.length() < 2){
                 hours = "0" + hours;
             }
         }
         return hours + "h " + minutes + "m " + seconds + "s";
     }
     //OCR
     public class NUM {
         private Point[] points;
         private  String num;
 
 
         public NUM(Point[] points, String number) {
             this.points = points;
             this.num = number;
         }
     }
 
     public class Numbers {
         private int x;
         private String num;
 
         public Numbers(String num, int x) {
             this.x = x;
             this.num = num;
         }
     }
 
     ArrayList<Numbers> nums = new ArrayList<Numbers>();
     private Point[] Y = {new Point(0, 0), new Point(4, 0), new Point(0, 1), new Point(4, 1), new Point(0, 2), new Point(4, 2), new Point(1, 3), new Point(3, 3), new Point(2, 4),
             new Point(2, 4), new Point(2, 5), new Point(2, 6), new Point(2, 7), new Point(2, 8), new Point(2, 9)};
 
     private Point[] o = {new Point(1, 0), new Point(2, 0), new Point(3, 0), new Point(0, 1), new Point(4, 1), new Point(0, 2), new Point(4, 2), new Point(0, 3), new Point(4, 3),
             new Point(0, 4), new Point(4, 4), new Point(1, 5), new Point(2, 5), new Point(3, 5)};
 
     private Point[] u = {new Point(0, 0), new Point(4, 0), new Point(0, 1), new Point(4, 1), new Point(0, 2), new Point(4, 2), new Point(0, 3), new Point(4, 3), new Point(0, 4),
             new Point(4, 4), new Point(1, 5), new Point(2, 5), new Point(3, 5)};
 
     private Point[] c = {new Point(1, 0), new Point(2, 0), new Point(3, 0), new Point(0, 1), new Point(0, 2), new Point(0, 3), new Point(0, 3), new Point(0, 4), new Point(1, 5),
             new Point(2, 5), new Point(3, 5)};
 
     private Point[] a = {new Point(1, 0), new Point(2, 0), new Point(3, 0), new Point(0, 1), new Point(4, 1), new Point(1, 2), new Point(2, 2), new Point(3, 2), new Point(4, 2),
             new Point(0, 3), new Point(4, 3), new Point(0, 4), new Point(4, 4), new Point(1, 5), new Point(2, 5), new Point(3, 5), new Point(4, 5)};
 
     private Point[] n = {new Point(0, 0), new Point(2, 0), new Point(3, 0), new Point(0, 1), new Point(1, 1), new Point(4, 1), new Point(0, 2), new Point(4, 2), new Point(0, 3),
             new Point(4, 3), new Point(0, 4), new Point(4, 4), new Point(4, 4), new Point(0, 5), new Point(5, 5)};
     private NUM[] allNumbers = {new NUM(Y, "Y"), new NUM(o, "o"), new NUM(u, "u"), new NUM(c, "c"), new NUM(a, "a"), new NUM(n, "n")};
 
     private final Color BLACK = new Color(0, 0, 0);
 
     void findNumber(NUM number, ArrayList<Numbers> numbF, Rectangle rec) {
 
         // TRAVERSE GAME SCREEN
         for(int y = rec.y; y < rec.y + rec.height; y++) {
             for(int x = rec.x; x < rec.x + rec.width; x++) {
                 // FIND POINT WITH COLOR THAT MATCHES TEXT COLOR
                 if(colorsMatch(Game.getColorAt(x, y), BLACK)) {
                     // MOVE POINT TO STARTING LOCATION OF NUMBER TO CHECK
                     final Point loc = new Point(x - number.points[0].x, y - number.points[0].y);
                     boolean found = true;
                     for(int i = 0; i < number.points.length; i++) {
                         if(!colorsMatch(Game.getColorAt(loc.x + number.points[i].x, loc.y + number.points[i].y), BLACK)) {
                             found = false;
                             break;
                         }
                     }
                     if(found) {
                         numbF.add(new Numbers(number.num, loc.x));
                     }
                 }
             }
         }
     }
 
     String findNumberString(Rectangle rec) {
         nums.clear();
         for (NUM allNumber : allNumbers) {
             findNumber(allNumber, nums, rec);
         }
         return sortNumbers();
     }
 
     private String sortNumbers() {
         String num = "";
         while(!nums.isEmpty()) {
             Numbers curNum = new Numbers("X", 800);
             for (Numbers num1 : nums) {
                 if (num1.x < curNum.x) {
                     curNum = num1;
                 }
             }
             num += curNum.num;
             nums.remove(curNum);
         }
         return num;
     }
 
     boolean colorsMatch(final Color origC, final Color... comparC) {
         for (Color color : comparC) {
             if(origC.equals(color)) {
                 return true;
             }
         }
         return false;
     }
     //END-OCR
 
 
     private class UserInterface extends JFrame {
         public UserInterface(){
             super("Dwarfeh's FireMaker By Dwarfeh");
             Tools();
         }
         private JLabel lblGENSET;
         private JLabel lblmouseSpeed;
         private JComboBox<String> cmbmouseSpeed;
         private JLabel lbllagAdjust;
         private JComboBox<String> cmblagAdjust;
         private JLabel lblLOGS;
         private JComboBox<String> cmbLOGS;
         private JLabel lblANTIBAN;
         private JComboBox<String> cmbANTIBAN;
         private JLabel lblLIGHTMETHOD;
         private JComboBox<String> cmbLIGHTMETHOD;
         private JButton btnStart;
 
         public final void Tools(){
             setLocationRelativeTo(null);
             setLayout(null);
             setSize(320, 212);
             setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
 
             lblGENSET = new JLabel("General settings", JLabel.CENTER);
             lblGENSET.setForeground(Color.BLUE);
             lblGENSET.setBounds(5, 5, 300, 30);
             lblGENSET.setFont(new Font("Arial", Font.BOLD, 18));
             add(lblGENSET);
 
             lblmouseSpeed = new JLabel("Mouse speed");
             lblmouseSpeed.setForeground(Color.BLACK);
             lblmouseSpeed.setBounds(5, 40, 300, 14);
             lblmouseSpeed.setFont(new Font("Arial", Font.BOLD, 14));
             add(lblmouseSpeed);
 
             String[] mouseSpeedS = { "Slow", "Medium", "Fast", "Ultra fast" };
             cmbmouseSpeed = new JComboBox<String>(mouseSpeedS);
             cmbmouseSpeed.setForeground(Color.BLACK);
             cmbmouseSpeed.setSelectedIndex(2);
             cmbmouseSpeed.setBounds(125, 38, 180, 20);
             cmbmouseSpeed.setFont(new Font("Arial", Font.PLAIN, 14));
             add(cmbmouseSpeed);
 
             lbllagAdjust = new JLabel("Lag adjust");
             lbllagAdjust.setForeground(Color.BLACK);
             lbllagAdjust.setBounds(5, 62, 300, 14);
             lbllagAdjust.setFont(new Font("Arial", Font.BOLD, 14));
             add(lbllagAdjust);
 
             String[] lagAdjustS = { "Nothing", "Minimal", "Little", "Medium", "Much" };
             cmblagAdjust = new JComboBox<String>(lagAdjustS);
             cmblagAdjust.setForeground(Color.BLACK);
             cmblagAdjust.setSelectedIndex(1);
             cmblagAdjust.setBounds(125, 60, 180, 20);
             cmblagAdjust.setFont(new Font("Arial", Font.PLAIN, 14));
             add(cmblagAdjust);
 
             lblLOGS = new JLabel("Logs to light");
             lblLOGS.setForeground(Color.BLACK);
             lblLOGS.setBounds(5, 84, 300, 14);
             lblLOGS.setFont(new Font("Arial", Font.BOLD, 14));
             add(lblLOGS);
 
             String[] LOGS = { "Normal", "Oak", "Willow", "Maple", "Yew", "Magic" };
             cmbLOGS = new JComboBox<String>(LOGS);
             cmbLOGS.setForeground(Color.BLACK);
             cmbLOGS.setSelectedIndex(3);
             cmbLOGS.setBounds(125, 82, 180, 20);
             cmbLOGS.setFont(new Font("Arial", Font.PLAIN, 14));
             add(cmbLOGS);
 
             lblANTIBAN = new JLabel("Anti-Ban usage");
             lblANTIBAN.setForeground(Color.BLACK);
             lblANTIBAN.setBounds(5, 106, 300, 14);
             lblANTIBAN.setFont(new Font("Arial", Font.BOLD, 14));
             add(lblANTIBAN);
 
             String[] antibanAmountS = { "Rarely", "Sometimes", "Medium", "Alot", "Almost always" };
             cmbANTIBAN = new JComboBox<String>(antibanAmountS);
             cmbANTIBAN.setForeground(Color.BLACK);
             cmbANTIBAN.setSelectedIndex(1);
             cmbANTIBAN.setBounds(125, 104, 180, 20);
             cmbANTIBAN.setFont(new Font("Arial", Font.PLAIN, 14));
             add(cmbANTIBAN);
 
             lblLIGHTMETHOD = new JLabel("Lighting method");
             lblLIGHTMETHOD.setForeground(Color.BLACK);
             lblLIGHTMETHOD.setBounds(5, 128, 300, 14);
             lblLIGHTMETHOD.setFont(new Font("Arial", Font.BOLD, 14));
             add(lblLIGHTMETHOD);
 
             String[] LIGHTMETHODS = { "Tinderbox > Log", "Right click Log > Light" };
             cmbLIGHTMETHOD = new JComboBox<String>(LIGHTMETHODS);
             cmbLIGHTMETHOD.setForeground(Color.BLACK);
             cmbLIGHTMETHOD.setSelectedIndex(0);
             cmbLIGHTMETHOD.setBounds(125, 126, 180, 20);
             cmbLIGHTMETHOD.setFont(new Font("Arial", Font.PLAIN, 14));
             add(cmbLIGHTMETHOD);
 
             btnStart = new JButton("Start");
             btnStart.addActionListener(new java.awt.event.ActionListener() {
                 public void actionPerformed(java.awt.event.ActionEvent evt) {
                     btnStartActionPerformed(evt);
                 }
             });
             btnStart.setBounds(5, 152, 300, 20);
             add(btnStart);
 
             setVisible(true);
         }
 
         public void btnStartActionPerformed(ActionEvent e) {
             switch(cmbmouseSpeed.getSelectedIndex()) {
                 case 0:
                     mouseSpeed =7;
                     break;
 
                 case 1:
                     mouseSpeed = 5;
                     break;
 
                 case 2:
                     mouseSpeed = 4;
                     break;
 
                 case 3:
                     mouseSpeed = 2;
                     break;
             }
 
             switch(cmblagAdjust.getSelectedIndex()) {
                 case 0:
                     lagAdjust = 0;
                     break;
 
                 case 1:
                     lagAdjust = 75;
                     break;
 
                 case 2:
                     lagAdjust = 150;
                     break;
 
                 case 3:
                     lagAdjust = 275;
                     break;
 
                 case 4:
                     lagAdjust = 450;
                     break;
             }
 
             switch(cmbANTIBAN.getSelectedIndex()) {
                 case 0:
                     antibanAmount = 1000;
                     break;
 
                 case 1:
                     antibanAmount = 750;
                     break;
 
                 case 2:
                     antibanAmount = 500;
                     break;
 
                 case 3:
                     antibanAmount = 250;
                     break;
 
                 case 4:
                     antibanAmount = 150;
                     break;
             }
 
             tinderboxLight = cmbLIGHTMETHOD.getSelectedIndex() == 0;
 
             logChosen = cmbLOGS.getSelectedIndex();
             guiOpened = false;
             ui.dispose();
         }
     }
 
     public int random(int min, int max) {
         Random rand = new Random();
         return rand.nextInt((max+1) - min) + min;
     }
 
     public void sleep(int a, int b) {
         sleep(random(a, b));
     }
 
     public static int getDistanceBetween(final Point p1, final Point p2) {
         if (p1 == null || p2 == null) {
             return -1;
         }
         final int xDiff = p2.x - p1.x;
         final int yDiff = p2.y - p1.y;
         return (int) Math.sqrt(xDiff * xDiff + yDiff * yDiff);
     }
 }
