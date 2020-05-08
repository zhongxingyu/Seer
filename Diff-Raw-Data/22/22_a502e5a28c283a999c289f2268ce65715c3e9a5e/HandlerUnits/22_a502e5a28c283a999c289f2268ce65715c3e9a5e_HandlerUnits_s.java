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
     
     public HandlerUnits(GuiGame parent) {
         this.parent = parent;
         currentUnitArray = new Unit[15][20];
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
         unitSword.setLocation(2, 4);
         unitMap.put(1, unitSword);
         
         updateUnitPositions();
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
         if(currentUnitArray[y][x] != null) {
             return true;
         }
         return false;
     }
     
     public void selectUnit(int x, int y) {
         currentUnitArray[y][x].select();
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
         for(int i = 0; i < 15; i++) {
             for(int j = 0; j < 20; j++) {
                 currentUnitArray[i][j] = unitArray[parent.handlerPlayer.xPos + i][parent.handlerPlayer.yPos + j];
             }
         }
         needsUpdate = true;
     }
     
     public void renderCurrentUnits() {
         unitRender = new BufferedImage(640, 480, BufferedImage.TYPE_INT_ARGB);
         Graphics g = unitRender.createGraphics();
         for(int rows = 0; rows < 20; rows++) {
             for(int cols = 0; cols < 15; cols++) {
                 if(currentUnitArray[cols][rows] != null && getSprite(currentUnitArray[cols][rows].getID()) != null) {
                     g.drawImage(getSprite(currentUnitArray[cols][rows].getID()), rows * 32, cols * 32, parent);
                     if(currentUnitArray[cols][rows].isSelected()) {
                         g.setColor(Color.YELLOW);
                         g.drawRect(rows * 32, cols * 32, 32, 32);
                     }
                     g.drawImage(getHealthBar(currentUnitArray[cols][rows]), rows * 32, cols * 32, parent);
                 }
             }
         }
         needsUpdate = false;
     }
     
     public boolean needsUpdate() {
         return needsUpdate;
     }
     
     public void render(Graphics g) {
         updateUnitPositions();
         updateCurrentUnits();
         renderCurrentUnits();
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
     }
     
     public BufferedImage getHealthBar(Unit unitToMeasure) {
         BufferedImage healthBar = new BufferedImage(32, 4, BufferedImage.TYPE_INT_RGB);
         Graphics g = healthBar.createGraphics();
         
         int greenWidth = 0;
        System.out.println("health:" + (32 / unitToMeasure.getMaxHealth()) * unitToMeasure.getCurrentHealth());
        greenWidth = (int) ((32 / unitToMeasure.getMaxHealth()) * unitToMeasure.getCurrentHealth());
         
         g.setColor(Color.GREEN);
        g.fillRect(0, 0, /*greenWidth*/ 20, 4);
         
         return healthBar;
     }
     
     public GuiGame getGuiGame() {
         return parent;
     }
 }
