 package main;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.image.BufferedImage;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Set;
 import javax.imageio.ImageIO;
 import javax.swing.JOptionPane;
 import resources.Resources;
 
 public class HandlerUnits {
 
     protected GuiGame parent;
     protected Unit[][] unitArray;
     protected Unit[][] currentUnitArray;
     protected HashMap<Integer, Unit> unitTypeMap;
     protected HashMap<Integer, Unit> unitMap;
     protected BufferedImage unitRender;
     protected boolean needsUpdate = true;
     protected BufferedImage spriteSword;
     protected BufferedImage spriteArcher;
     protected BufferedImage spriteMage;
     
     protected HandlerMovement handlerMovement = new HandlerMovement(this);
     protected Thread movementThread = new Thread(handlerMovement);
 
     public HandlerUnits(GuiGame parent) {
         this.parent = parent;
         currentUnitArray = new Unit[20][15];
         unitArray = new Unit[50][50];
         unitTypeMap = new HashMap<>();
         unitMap = new HashMap<>();
         
         try {
             spriteSword = ImageIO.read(Resources.class.getResourceAsStream("UnitSword.png"));
             spriteArcher = ImageIO.read(Resources.class.getResourceAsStream("UnitArcher.png"));
             spriteMage = ImageIO.read(Resources.class.getResourceAsStream("UnitMage.png"));
         } catch(Exception ex) {
             JOptionPane.showMessageDialog(null, "Failed to load resources for units", "Resource failure", JOptionPane.ERROR_MESSAGE);
         }
 
         registerUnitType(new UnitSword(this));
         registerUnitType(new UnitArcher(this));
         registerUnitType(new UnitMage(this));
 
         UnitSword unitSword = new UnitSword(this);
         unitSword.setLocation(4, 2);
         unitMap.put(1, unitSword);
 
         UnitArcher unitArcher = new UnitArcher(this);
         unitArcher.setLocation(4, 5);
         unitArcher.playerOwned = false;
         unitMap.put(2, unitArcher);
 
         updateCurrentUnits();
         
         movementThread.start();
     }
 
     public boolean isUnitsSelected() {
         boolean unitsSelected = false;
         Set keys = unitMap.keySet();
         Iterator i = keys.iterator();
 
         int entryKey = 0;
         Unit entryUnit = null;
         while(i.hasNext()) {
             entryKey = (int) i.next();
             entryUnit = unitMap.get(entryKey);
             if(unitArray[entryUnit.getLocation().x][entryUnit.getLocation().y].isSelected()) {
                 unitsSelected = true;
             }
         }
         return unitsSelected;
     }
 
     public boolean isOccupied(int x, int y) {
         if(unitArray[x][y] != null) {
             return true;
         }
         return false;
     }
 
     public BufferedImage getSprite(int id) {
         switch(id) {
             case 1:
                 return spriteSword;
             case 2:
                 return spriteArcher;
             case 3:
                 return spriteMage;
         }
         return null;
     }
 
     public boolean tileHasUnit(int x, int y) {
        if(currentUnitArray[x][y] != null) {
             return true;
         }
         return false;
     }
 
     public void selectUnit(int x, int y) {
         currentUnitArray[x][y].select();
     }
 
     public Unit getUnit(int x, int y) {
         return unitArray[x][y];
     }
 
     public Unit getUnitType(int id) {
         return unitTypeMap.get(id);
     }
 
     public void registerUnitType(Unit unitToRegister) {
         if(unitTypeMap.get(unitToRegister.getID()) == null) {
             unitTypeMap.put(unitToRegister.getID(), unitToRegister);
         }
     }
 
     public void updateCurrentUnits() {
         updateUnitPositions();
         for(int cols = 0; cols < 15; cols++) {
             for(int rows = 0; rows < 20; rows++) {
                 currentUnitArray[rows][cols] = unitArray[parent.handlerPlayer.xPos + rows][parent.handlerPlayer.yPos + cols];
             }
         }
         renderCurrentUnits();
     }
 
     public void renderCurrentUnits() {
         unitRender = new BufferedImage(640, 480, BufferedImage.TYPE_INT_ARGB);
         Graphics g = unitRender.createGraphics();
         for(int cols = 0; cols < 15; cols++) {
             for(int rows = 0; rows < 20; rows++) {
                 if(currentUnitArray[rows][cols] != null && getSprite(currentUnitArray[rows][cols].getID()) != null) {
                     g.drawImage(getSprite(currentUnitArray[rows][cols].getID()), rows * 32, cols * 32, parent);
                     if(currentUnitArray[rows][cols].isSelected()) {
                         g.setColor(Color.YELLOW);
                         g.drawRect(rows * 32, cols * 32, 32, 32);
                     }
                     g.drawImage(getHealthBar(currentUnitArray[rows][cols]), rows * 32, cols * 32, parent);
                 }
             }
         }
         needsUpdate = false;
     }
 
     public boolean needsUpdate() {
         return needsUpdate;
     }
 
     public void render(Graphics g) {
         updateCurrentUnits();
         g.drawImage(unitRender, 0, 0, parent.getMainWindow());
     }
 
     public void clearUnitArray() {
         unitArray = null;
         unitArray = new Unit[50][50];
     }
 
     public void updateUnitPositions() {
         Set keys = unitMap.keySet();
         Iterator i = keys.iterator();
 
         clearUnitArray();
 
         int entryKey = 0;
         Unit entryUnit = null;
         while(i.hasNext()) {
             entryKey = (int) i.next();
             entryUnit = unitMap.get(entryKey);
             unitArray[entryUnit.getLocation().x][entryUnit.getLocation().y] = entryUnit;
         }
     }
 
     public void deselectAll() {
         Set keys = unitMap.keySet();
         Iterator i = keys.iterator();
 
         int entryKey = 0;
         Unit entryUnit = null;
         while(i.hasNext()) {
             entryKey = (int) i.next();
             entryUnit = unitMap.get(entryKey);
             unitArray[entryUnit.getLocation().x][entryUnit.getLocation().y].deselect();
         }
     }
 
     public void moveSelected(int x, int y) {
         if(isUnitsSelected()) {
             Set keys = unitMap.keySet();
             Iterator i = keys.iterator();
 
             int entryKey = 0;
             Unit entryUnit = null;
             while(i.hasNext()) {
                 entryKey = (int) i.next();
                 entryUnit = unitMap.get(entryKey);
                 if(unitArray[entryUnit.getLocation().x][entryUnit.getLocation().y].isSelected()) {
                     unitArray[entryUnit.getLocation().x][entryUnit.getLocation().y].move(x, y);
                 }
             }
         }
        // parent.parent.repaint();
     }
 
     public void attackSelected(int x, int y) {
         if(isUnitsSelected()) {
             Set keys = unitMap.keySet();
             Iterator i = keys.iterator();
 
             int entryKey = 0;
             Unit entryUnit = null;
             while(i.hasNext()) {
                 entryKey = (int) i.next();
                 entryUnit = unitMap.get(entryKey);
                 if(unitArray[entryUnit.getLocation().x][entryUnit.getLocation().y].isSelected()) {
                     unitArray[entryUnit.getLocation().x][entryUnit.getLocation().y].attack(x, y);
                 }
             }
         }
     }
 
     public BufferedImage getHealthBar(Unit unitToMeasure) {
         BufferedImage healthBar = new BufferedImage(32, 4, BufferedImage.TYPE_INT_RGB);
         Graphics g = healthBar.createGraphics();
 
         /* 
          * Changed the calculations for the healthbars.
          * It just does (32*curHealth) / maxHealth, so it gives the width of the healthbar to use.
          * You can test if it works by yourself by modifying the "currentHealth" var in UnitSword.java
          *      Or just do in a line below : unitToMeasure.currentHealth = 40 (for example) (and don't forget to remove it :p)
          */
         //greenWidth = (int) (((32 * unitToMeasure.getCurrentHealth()) / unitToMeasure.getMaxHealth()));
         int greenWidth = (int) (((32 * unitToMeasure.getCurrentHealth()) / unitToMeasure.getMaxHealth()));
 
         g.setColor(Color.GREEN);
         g.fillRect(0, 0, greenWidth, 4);
 
         return healthBar;
     }
 
     public GuiGame getGuiGame() {
         return parent;
     }
 
     public boolean isFriendly(int x, int y) {
         if(!isOccupied(x, y)) {
             return true;
         }
         return getUnit(x, y).isPlayerOwned();
     }
 
     public void killUnit(Unit unitToKill) {
         Set keys = unitMap.keySet();
         Iterator i = keys.iterator();
 
         int entryKey = 0;
         Unit entryUnit = null;
         while(i.hasNext()) {
             entryKey = (int) i.next();
             entryUnit = unitMap.get(entryKey);
             if(unitArray[entryUnit.getLocation().x][entryUnit.getLocation().y] == unitToKill) {
                 unitMap.remove(entryKey);
                 break;
             }
         }
         updateUnitPositions();
     }
 
     public int getUnitCount() {
         Set keys = unitMap.keySet();
         Iterator i = keys.iterator();
 
         int unitCount = 0;
         
         int entryKey = 0;
         Unit entryUnit = null;
         while(i.hasNext()) {
             entryKey = (int) i.next();
             entryUnit = unitMap.get(entryKey);
             if(unitArray[entryUnit.getLocation().x][entryUnit.getLocation().y].isPlayerOwned()) {
                 unitCount++;
             }
         }
         return unitCount;
     }
 }
