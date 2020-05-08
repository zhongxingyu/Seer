 package com.avapir.roguelike.core.gui;
 
 import com.avapir.roguelike.core.GameStateManager;
 import com.avapir.roguelike.core.GameStateManager.GameState;
 import com.avapir.roguelike.core.Log;
 import com.avapir.roguelike.core.Viewport;
 import com.avapir.roguelike.core.controls.KeyboardHandler;
 import com.avapir.roguelike.core.resources.ImageResources;
 import com.avapir.roguelike.game.battle.Armor;
 import com.avapir.roguelike.game.battle.Attack;
import com.avapir.roguelike.game.world.character.Hero;
import com.avapir.roguelike.game.world.character.Hero.PrimaryStats;
import com.avapir.roguelike.game.world.character.Mob;
 import com.avapir.roguelike.game.world.items.Item;
 import com.avapir.roguelike.game.world.items.ItemDatabase;
 import com.avapir.roguelike.game.world.map.Map;
 import com.avapir.roguelike.game.world.map.MapHolder;
 import com.avapir.roguelike.game.world.map.Tile;
 
 import java.awt.*;
 import java.util.ArrayList;
 import java.util.List;
 
 public class GamePanel extends AbstractGamePanel {
 
     public static final  int  STAT_BAR_HEIGHT_PX  = 3;
     private static final long serialVersionUID    = 1L;
     private static final Font inventoryAmountFont = new Font(Font.SERIF, Font.PLAIN, 12);
     private final GuiPainter guiPainter;
     private final Color inventoryAmountColor = new Color(250, 250, 0);
 
     public GamePanel() {
         super();
         guiPainter = new GuiPainter();
         addKeyListener(new KeyboardHandler());
         setFocusable(true);
     }
 
     private class GuiPainter {
         private final Point o            = new Point(getMapWidth() + 15, getMapHeight() / 2);
         private final Point attackOffset = new Point(o.x, o.y + 90);
         private final Point armorOffset  = new Point(o.x + 150, o.y + 90);
         private final Point statsOffset  = new Point(o.x, o.y + 200);
         private final Point invenOffset  = new Point(o.x + 100, 100);
         private final Font  guiFont      = new Font(Font.MONOSPACED, Font.PLAIN, 15);
         private       int   validMax     = Integer.MIN_VALUE;
         private Graphics2D g2;
 
         public void draw(final Graphics2D g2) {
             invalidateMax();
             this.g2 = g2;
             heroEquipment();
             heroMainStats();
             heroAttack();
             heroArmor();
             heroStats();
 
 //            if (GameStateManager.getInstance().getState() == GameState.INVENTORY) {
                 /*draw inventory
                 * actually, I draw it at GamePanel#drawMap(Map, Graphics2D)
                 * Theres 2 goals got by this decision:
                 * 1) Do not draw inventory-pixels twice: first for map tiles and second for inventory
                 * 2) I hope it will look nice
                 */
 //            }
         }
 
         private void heroEquipment() {
             final Image itemBg = ImageResources.getImage("inventory_bg");
             int itemBgWidth = itemBg.getWidth(null);
             int itemBgHeight = itemBg.getHeight(null);
 
             for (int i = 0; i < 3; i++) {
                 for (int j = 0; j < 4; j++) {
                     int xx = invenOffset.x + i * (itemBgWidth + 1);
                     int yy = invenOffset.y + j * (itemBgHeight + 1);
                     g2.drawImage(itemBg, xx, yy, null);
                     g2.setColor(Color.yellow);
                     Item item = Hero.getInstance().getEquipment().get(j * 3 + i);
                     if (item != null) {
                         g2.drawImage(ImageResources.getImage(item.getData().getImageName()), xx, yy, null);
                     }
                 }
             }
 
             GameStateManager gsm = GameStateManager.getInstance();
             if (gsm.getState() == GameState.INVENTORY && gsm.getInventoryHandler().isOnEquipment()) {
                 g2.setColor(Color.yellow);
                 final Point cursor = gsm.getInventoryHandler().getCursor();
                 g2.drawRect(invenOffset.x + cursor.x * (itemBgWidth + 1), invenOffset.y + cursor.y * (itemBgHeight + 1),
                             itemBgWidth, itemBgHeight);
             }
         }
 
         private void heroMainStats() {
             Hero hero = Hero.getInstance();
             g2.setFont(guiFont);
             g2.setColor(new Color(255, 255, 0));
             g2.drawString("X: " + hero.getLoc().x, o.x, o.y);
             g2.drawString("Y: " + hero.getLoc().y, o.x, o.y + 15);
             g2.drawString(hero.getName(), o.x, o.y + 30);
             g2.drawString(String.format("Level [%d] (%d/%d)", hero.getLevel(), hero.getXP(), hero.getAdvanceXP()),
                           o.x + 80, o.y);
             float v = roundThreeDigits(
                     (hero.getXP() - hero.getPrevLevelXp()) / Float.valueOf(Integer.toString(hero.getAdvanceXP())) *
                             100);
             g2.drawString(String.format("[%s%%]", v), o.x + 80, o.y + 15);
 
             g2.setColor(new Color(80, 255, 0));
            g2.drawString(String.format("%s/%s", roundOneDigit(hero.getHP()), Hero.StatsFormulas.getMaxHP(hero)), o.x,
                           o.y + 50);
 
             g2.setColor(new Color(0, 80, 255));
            g2.drawString(String.format("%s/%s", roundOneDigit(hero.getMP()), Hero.StatsFormulas.getMaxMP(hero)), o.x,
                           o.y + 65);
         }
 
         private void heroAttack() {
             g2.setColor(new Color(255, 50, 0));
             Attack atk = Hero.getInstance().getAttack();
             for (int i = 0; i < Attack.TOTAL_DMG_TYPES; i++) {
                 final float heroDmg = roundOneDigit(atk.getDamageOfType(i));
                 g2.drawString(Float.toString(heroDmg), attackOffset.x, attackOffset.y + i * 15);
             }
         }
 
         private void heroArmor() {
             g2.setColor(new Color(255, 200, 0));
             Armor armr = Hero.getInstance().getArmor();
             for (int i = 0; i < Armor.TOTAL_DEF_TYPES; i++) {
                 final float heroDef = roundOneDigit(armr.getArmorOfType(i));
                 g2.drawString(Float.toString(heroDef), armorOffset.x, armorOffset.y + i * 15);
             }
         }
 
         /**
          * SP: 0-74 === case 0 === 255\0\0 -> 255\252\0
          * <p>
          * SP: 75-149 === case 1 === 255\0\0 -> 0\255\0
          * <p>
          * SP: 150-224 === case 2 === 0\255\0 -> 0\252\255
          * <p>
          * SP: 225-299 === case 3 === 0\255\255 -> 0\3\255
          * <p>
          * SP: 300 === case 4 === 0\0\255
          *
          * @param stat specified hero`s stat
          *
          * @return color from red to blue, through all spectre, which represents that stat-value from 0 to {@value
         * com.avapir.roguelike.game.world.character.Hero.PrimaryStats#MAX_STAT_VALUE}
          */
         private Color getStatToColor(final int stat) {
             if (stat < 0) {
                 return Color.black;
             }
 
             int r = 0, g = 0, b = 0;
            final int factor = Hero.PrimaryStats.MAX_STAT_VALUE / 4;
             switch (stat / factor) {
                 case 0:
                     r = 255;
                     g = 255 * stat / factor;
                     break;
                 case 1:
                     r = 255 - 255 * (stat - factor) / factor;
                     g = 255;
                     break;
                 case 2:
                     g = 255;
                     b = 255 * (stat - factor * 2) / factor;
                     break;
                 case 3:
                     g = 255 - 255 * (stat - factor * 3) / factor;
                     b = 255;
                     break;
                 case 4:
                     b = 255;
                     break;
                 default: // stat > 300
                     r = 255;
                     b = 255;
             }
             return new Color(r, g, b, 64);
         }
 
         private int maxStat() {
             if (validMax == Integer.MIN_VALUE) {
                 int max = 0;
                 final List<Integer> arr = new ArrayList<>();
                 GameStateManager gsm = GameStateManager.getInstance();
                for (int i = 0; i < Hero.PrimaryStats.PRIMARY_STATS_AMOUNT; i++) {
                     arr.add(Math.abs(Hero.getInstance().getStats().values(i)));
                     if (gsm.getState() == GameState.CHANGE_STATS) {
                         arr.add(Math.abs(Hero.getInstance().getStats().values(i) + gsm.getStatsHandler().getDiff()[i]));
                     }
                 }
 
                 for (final int i : arr) {
                     max = max > i ? max : i;
                 }
                 validMax = max;
             }
             return validMax;
         }
 
         private void invalidateMax() {
             validMax = Integer.MIN_VALUE;
         }
 
         private void heroStats() {
             Hero hero = Hero.getInstance();
             GameStateManager gsm = GameStateManager.getInstance();
             g2.setColor(Color.yellow);
             final int lineHeight = guiFont.getSize();
             final int oY = statsOffset.y - lineHeight + 3;// экспериментально полученное визуально лучшее значение
             final boolean isChangingStats = gsm.getState() == GameState.CHANGE_STATS;
 
             for (int i = 0; i < PrimaryStats.STATS_STRINGS.length; i++) {
                 final int curStat = Hero.getInstance().getStats().values(i);
                 final int diff = isChangingStats ? gsm.getStatsHandler().getDiff()[i] : 0;
 
                 // rectangles
                 if (curStat + diff <= 0) {
                     g2.setColor(Color.black);
                     g2.fillRect(statsOffset.x, oY + i * lineHeight, 75 - 75 * (curStat + diff) / maxStat(), lineHeight);
                 } else {
                     g2.setColor(getStatToColor(curStat + diff));
                     g2.fillRect(statsOffset.x, oY + i * lineHeight, 75, lineHeight);
                     if (curStat > 0) {
                         g2.fillRect(statsOffset.x, oY + i * lineHeight,
                                     (int) (75 + Math.signum(curStat) * 75 * curStat / maxStat()), lineHeight);
                     }
                     g2.fillRect(statsOffset.x, oY + i * lineHeight, 75 + 75 * (curStat + diff) / maxStat(), lineHeight);
                 }
 
                 if (isChangingStats) {
                     // difference
                     g2.setColor(new Color(255, 255, 255, 128));
                     g2.drawString((diff >= 0 ? "+" : "") + diff, statsOffset.x + 75, statsOffset.y + i * 15);
                 }
 
                 g2.setColor(Color.yellow);
                 g2.drawString(PrimaryStats.STATS_STRINGS[i] + getStatDelimiter(hero.getStats().values(i)) +
                                       hero.getStats().values(i), statsOffset.x, statsOffset.y + i * 15);
 
             }
 
             if (hero.getStats().isLearnable()) {
                 int free = hero.getStats().getFreeStats();
                 if (isChangingStats) {
                     free += gsm.getStatsHandler().getFreeDiff();
                 }
                 g2.drawString("Не распределено: " + free, statsOffset.x, statsOffset.y + 6 * 15);
             }
 
             if (gsm.getState() == GameState.CHANGE_STATS) {
                 // cursor
                 g2.setColor(Color.yellow);
                 final int cursor = gsm.getStatsHandler().getCursor().y;
                 Hero.getInstance().getStats().values(cursor);
 
                 g2.drawRect(statsOffset.x, oY + cursor * 15, 75 * 2, lineHeight);
 
             }
         }
 
         private String getStatDelimiter(final int stat) {
             if (stat < 0) {
                 return getStatDelimiter(-stat).substring(1);// one space less cos we have '-'
             } else if (stat < 10) {
                 return "    ";// 4
             } else if (stat < 100) {
                 return "   ";// 3
             } else if (stat < 1000) {
                 return "  ";// 2
             } else {
                 return " ";// 1
             }
         }
     }
 
     @Override
     public void paintComponent(final Graphics g) {
         super.paintComponent(g);
         final Graphics2D g2 = (Graphics2D) g;
 
         drawMap(MapHolder.getInstance(), g2);
         drawLog(g2);
         drawDialogs(g2);
         debugShowMiniMap(MapHolder.getInstance(), g2);
     }
 
     private void drawLog(Graphics2D g2) {
         int x = 15, y = 15;
         final Point offset = new Point(x, y);
         final Font logFont = new Font("Times New Roman", Font.PLAIN, 15);
         g2.setFont(logFont);
         g2.setColor(new Color(255, 255, 255));
 
         Log log = Log.getInstance();
         for (int i = 0; i < log.getSize(); i++) {
             final String s = log.get(i);
             if (!s.startsWith("E")) {
                 g2.drawString(s, offset.x, offset.y + i * logFont.getSize() + 3);
             } else {
                 g2.setColor(new Color(255, 0, 0));
                 g2.drawString(s.substring(1), offset.x, offset.y + i * logFont.getSize() + 3);
                 g2.setColor(new Color(255, 255, 255));
             }
         }
     }
 
     private void drawDialogs(Graphics2D g2) {
         //TODO GameDialog class
         switch (GameStateManager.getInstance().getState()) {
             case GAME_OVER:
                 gameOverDialog(g2);
                 break;
         }
         if (GameStateManager.getInstance().isNeedHelp()) {
             helpDialog(g2);
         }
     }
 
     private void helpDialog(Graphics2D g2) {
         String[] move = {"<Arrows>  -- go in specified direction", "       =  -- zoom in", "       -  -- zoom out",
                 "   <END>  -- end turn without step", "", "i  -- open Inventory", "d  -- make Distance Attack",
                 "v  -- switch to Viewer Mode", "c  -- change stats"};
         String[] gameover = {"Press any key to exit"};
         String[] stats = {"<Arrows>  -- choose stat and change it as you wish", "c  -- return to Moving"};
         String[] view = {"v  -- return to Moving"};
         String[] distance = {"d  -- return to Moving"};
         String[] inventory = {"<ENTER>  -- equip or take off selected item", "s  -- switch cursor to " +
                 "inventory/equipment", "e  -- equip or take off selected item and switch cursor",
                 "i  -- return to Moving"};
 
         String[] toPrint;
         g2.setColor(new Color(50, 50, 50, 128));
         switch (GameStateManager.getInstance().getState()) {
             case MOVE:
                 g2.fillRect(200, 150, 350, 125);
                 toPrint = move;
                 break;
             case GAME_OVER:
                 g2.fillRect(200, 150, 210, 20);
                 toPrint = gameover;
                 break;
             case CHANGE_STATS:
                 g2.fillRect(200, 150, 460, 35);
                 toPrint = stats;
                 break;
             case VIEW:
                 g2.fillRect(200, 150, 210, 20);
                 toPrint = view;
                 break;
             case DISTANCE_ATTACK:
                 g2.fillRect(200, 150, 210, 20);
                 toPrint = distance;
                 break;
             case INVENTORY:
                 g2.fillRect(200, 150, 510, 60);
                 toPrint = inventory;
                 break;
             default:
                 throw new IllegalStateException("Wrong game state: " + GameStateManager.getInstance().getState());
 
         }
         g2.setColor(new Color(200, 200, 0));
         g2.setFont(new Font("Monospaced", Font.PLAIN, 15));
         for (int i = 0; i < toPrint.length; i++) {
             g2.drawString(toPrint[i], 205, 165 + i * 13);
         }
     }
 
     private void gameOverDialog(Graphics2D g2) {
         final Image img = ImageResources.getImage("gameover");
         g2.drawImage(img, (getWidthInTiles() * Tile.SIZE_px - img.getWidth(null)) / 2,
                      (getHeightInTiles() * Tile.SIZE_px - img.getHeight(null)) / 4, null);
     }
 
     @Override
     protected void drawGUI(final Graphics2D g2) {
         guiPainter.draw(g2);
     }
 
     private void debugShowMiniMap(final Map map, final Graphics2D g2) {
         final int z = 2;
         final int ox = 2;
         final int oy = SCREEN_HEIGHT - map.getHeight() * z - 30;
 
         for (int i = 0; i < map.getHeight(); i++) {
             for (int j = 0; j < map.getWidth(); j++) {
                 Color c = Color.black;
                 Tile t = map.getTile(i, j);
                 if (t.isPassable()) {
                     c = Color.yellow;
                     if (t.getMob() != null) {
                         final String name = t.getMob().getName();
                         if (name.equals("Slime")) {
                             c = Color.green;
                         } else if (name.equals("Hero")) {
                             c = Color.red;
                         }
                     }
                 }
                 if (!t.isVisible()) {
                     c = c.darker();
                 }
                 if (!t.isSeen()) {
                     c = c.darker();
                 }
                 g2.setColor(c);
                 g2.fillRect(ox + z * i, oy + z * j, z, z);
             }
         }
     }
 
     //  0123456789
     // 0**********
     // 1**      **
     // 2** IIII **
     // 3** IIII **   <<<=  =>>>>
     // 4**      **   01234567890
     // 5**********   *** II ****
     // INVENTORY_LINE = 4; INVENTORY_SIZE = 2; HIT = 5; WIT = 10;
     private void drawMap(final Map map, final Graphics2D g2) {
         final int offsetX = Viewport.INSTANCE.getX() - Viewport.horizontalViewDistance();
         final int offsetY = Viewport.INSTANCE.getY() - Viewport.verticalViewDistance();
         if (GameStateManager.getInstance().getState() == GameState.INVENTORY) {
             final int WIT = getWidthInTiles();
             final int HIT = getHeightInTiles();
            int l = (WIT - Hero.InventoryHandler.LINE) / 2;
            int r = (WIT + Hero.InventoryHandler.LINE) / 2;
             int t = 3;
             int d = t + Hero.getInstance().getInventory().getSize() + 1; // +1 == border
             //todo MAKE THIS SHIT AS SHADERS
             drawMap_inventory(g2, l, r, t, d);
             drawMap_inventoryBorder(g2, l - 1, r + 1, t - 1, d + 1);
             drawMap_tiles(map, g2, offsetX, offsetY, WIT, HIT, l, r, t, d);
         } else {
             for (int i = 0; i < getHeightInTiles(); i++) {
                 for (int j = 0; j < getWidthInTiles(); j++) {
                     drawMap_tiles_tile(map, g2, offsetX, offsetY, i, j);
                 }
             }
         }
 
     }
 
     private void drawMap_tiles_tile(Map map,
                                     Graphics2D g2,
                                     int offsetX,
                                     int offsetY,
                                     int i,
                                     int j) {// indexes on the Map
         final Tile tile = map.getTile(offsetX + j, offsetY + i);
         if (tile != null) {
             drawToCell(g2, ImageResources.getImage("empty"), j, i);
 
             if (tile.isSeen()) {
                 String imageName;
                 switch (tile.getType()) {
                     case GRASS:
                         imageName = "grass";
                         break;
                     case TREE:
                         imageName = "tree";
                         break;
                     default:
                         throw new RuntimeException("Unresolved tile type");
                 }
                 drawToCell(g2, ImageResources.getImage(imageName), j, i);
 
                 if (tile.isVisible()) {
                     if (tile.getMob() != null) {
                         drawMobOnTile(g2, tile.getMob(), j, i);
                     }
                     if (tile.getItemsAmount() > 0) {
                         drawToCell(g2, ImageResources.getImage(
                                 tile.getItemsAmount() > 1 ? "many_items" : tile.getItemList()
                                                                                .get(0)
                                                                                .getItem()
                                                                                .getData()
                                                                                .getImageName()), j, i);
                     }
                 } else { // seen & !visible == "fog of war" over empty terrain
                     drawToCell(g2, ImageResources.getImage("wFog"), j, i);
                 }
             }
         }
 //        else {// some outworld. Stars ort smth}
     }
 
     private void drawMobOnTile(Graphics2D g2, Mob m, int j, int i) {
         if (m.equals(Hero.getInstance())) {
             if (m.isAlive()) {
                 drawToCell(g2, ImageResources.getImage("hero"), j, i);
                 drawMobLifeBars(g2, m, j, i, true);
             } else { //if dead
                 drawToCell(g2, ImageResources.getImage("rip"), j, i);
             }
         } else {
             if (m.isAlive()) {
                 String name = m.getName();
                 String s1 = name.toLowerCase();
                 String s = s1.replace(" ", "_");
                 Image image = ImageResources.getImage(s);
                 drawToCell(g2, image, j, i);
                 drawMobLifeBars(g2, m, j, i, false);
             }
         }
     }
 
     private void drawMobLifeBars(Graphics2D g2, Mob m, int j, int i, boolean isFriendly) {
         Color friend = new Color(0, 255, 0, 128);
         Color monster = new Color(255, 0, 0, 128);
         paintColorBar(m.getHP() / m.getMaxHp(), isFriendly ? friend : monster, 0, j, i, g2);
         if (m.getMaxMp() > 0) {
             paintColorBar(m.getMP() / m.getMaxMp(), new Color(0, 128, 255, 128), 1, j, i, g2);
         }
     }
 
     /**
      * Paints colored bar for some stat of character above him. Usually used for HP\MP. That method receives only
      * percentage value of stat (float value of {@code currentValue/maxValue}).
      *
      * @param value            percents of tile which wil be filled twice
      * @param transparentColor color which will be used
      * @param line             number of line painting already. Usually it's 0 for HP and 1 for MP
      * @param j                horizontal coordinate of tile
      * @param i                vertical coordinate of tile
      * @param g2               {@link Graphics2D} instance
      */
     private void paintColorBar(final float value,
                                final Color transparentColor,
                                final int line,
                                final int j,
                                final int i,
                                final Graphics2D g2) {
         if (value < 0 || value > 1) {
             throw new IllegalArgumentException("Value = " + value);
         }
         g2.setColor(transparentColor);
 
         int x = j * Tile.SIZE_px;
         int y = i * Tile.SIZE_px + line * GamePanel.STAT_BAR_HEIGHT_PX;
 
         g2.fillRect(x, y, Tile.SIZE_px, GamePanel.STAT_BAR_HEIGHT_PX);
         g2.fillRect(x, y, (int) (value * Tile.SIZE_px), GamePanel.STAT_BAR_HEIGHT_PX);
     }
 
     private void drawMap_inventoryBorder(Graphics2D g2, int l, int r, int t, int d) {
         Image border = ImageResources.getImage("empty");
         for (int w = l; w <= r; w++) {
             drawToCell(g2, border, w, t);
             drawToCell(g2, border, w, d);
         }
         for (int h = t + 1; h < d; h++) {
             drawToCell(g2, border, l, h);
             drawToCell(g2, border, r, h);
         }
     }
 
     /**
      * @param g2 {@link Graphics2D} instance
      * @param l  left boundary cell
      * @param r  right boundary cell
      * @param t  top boundary cell
      * @param d  bottom boundary cell
      */
     private void drawMap_inventory(Graphics2D g2, int l, int r, int t, int d) {
         Image invBgImg = ImageResources.getImage("inventory_bg");
         for (int h = t; h <= d; h++) {
             for (int w = l; w <= r; w++) {
                 drawToCell(g2, invBgImg, w, h);
             }
         }
         int[][] items = Hero.getInstance().getInventory().toPaintableArrays();
 
         for (int[] item : items) {
             drawToCell(g2, ImageResources.getImage(ItemDatabase.get(item[2]).getImageName()), l + item[1], t + item[0]);
             if (item[3] != 1) {
                 g2.setFont(inventoryAmountFont);
                 g2.setColor(inventoryAmountColor);
                 printToCell(g2, String.format("x%s", Integer.toString(item[3])), l + item[1], t + item[0]);
             }
         }
 
         GameStateManager gsm = GameStateManager.getInstance();
         if (gsm.getState() == GameState.INVENTORY) {
             final Point memorizedPress = gsm.getInventoryHandler().getPress();
             if (!gsm.getInventoryHandler().isOnEquipment()) {
                 g2.setColor(Color.yellow);
                 final Point cursor = gsm.getInventoryHandler().getCursor();
                 g2.drawRect(Tile.SIZE_px * (l + cursor.x), Tile.SIZE_px * (t + cursor.y), Tile.SIZE_px, Tile.SIZE_px);
             }
             if (memorizedPress != null) {
                 g2.setColor(Color.green);
                 g2.drawRect(Tile.SIZE_px * (l + memorizedPress.x), Tile.SIZE_px * (t + memorizedPress.y), Tile.SIZE_px,
                             Tile.SIZE_px);
             }
         }
     }
 
     private void drawMap_tiles(Map map, Graphics2D g2, int ox, int oy, int wit, int hit, int l, int r, int t, int d) {
         for (int h = 0; h < t - 1; h++) {
             for (int w = 0; w < wit; w++) {
                 drawMap_tiles_tile(map, g2, ox, oy, h, w);
             }
         }
         for (int h = t - 1; h < d + 2; h++) {
             for (int w = 0; w < l - 1; w++) {
                 drawMap_tiles_tile(map, g2, ox, oy, h, w);
             }
             for (int w = r + 2; w < wit; w++) {
                 drawMap_tiles_tile(map, g2, ox, oy, h, w);
             }
         }
         for (int h = d + 2; h < hit; h++) {
             for (int w = 0; w < wit; w++) {
                 drawMap_tiles_tile(map, g2, ox, oy, h, w);
             }
         }
 
     }
 }
