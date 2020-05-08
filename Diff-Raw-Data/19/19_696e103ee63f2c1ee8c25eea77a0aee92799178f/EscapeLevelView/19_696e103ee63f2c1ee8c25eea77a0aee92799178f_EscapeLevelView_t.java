 /*
  * Created on Dec 19, 2004
  *
  * TODO To change the template for this generated file go to
  * Window - Preferences - Java - Code Style - Code Templates
  */
 package org.spacebar.escape;
 
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.geom.AffineTransform;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 import javax.swing.JComponent;
 import javax.swing.KeyStroke;
 
 import org.spacebar.escape.util.BitInputStream;
 import org.spacebar.escape.util.Characters;
 import org.spacebar.escape.util.IntTriple;
 import org.spacebar.escape.util.StyleStack;
 
 /**
  * @author adam
  * 
  * TODO To change the template for this generated type comment go to Window -
  * Preferences - Java - Code Style - Code Templates
  */
 public class EscapeLevelView extends JComponent {
 
     private class Mover extends AbstractAction {
         final int dir;
 
         public Mover(int dir) {
             this.dir = dir;
         }
 
         public void actionPerformed(ActionEvent e) {
             if (!done) {
                 if (theLevel.move(dir, effects)) {
                     // append to solution
                     addToSolution(dir);
                 }
                 setPlayerDir(dir);
 
                 if ((laser = theLevel.isDead()) != null) {
                     effects.doLaser();
                     System.out.println("dead");
                     done = true;
                 } else if (theLevel.isWon()) {
                     effects.doExit();
                     System.out.println("won in " + solutionCount + " steps");
                     System.out.print("solution: [");
                     for (int i = 0; i < solutionCount; i++) {
                         int d = solution[i];
                         String s;
                         switch (d) {
                         case Level.DIR_UP:
                             //                            s = "up";
                             s = "↑";
                             break;
                         case Level.DIR_DOWN:
                             //                            s = "down";
                             s = "↓";
                             break;
                         case Level.DIR_LEFT:
                             //                            s = "left";
                             s = "←";
                             break;
                         case Level.DIR_RIGHT:
                             //                            s = "right";
                             s = "→";
                             break;
                         default:
                             s = "?";
                         }
                         System.out.print(s);
                         if (i != solutionCount - 1) {
                             System.out.print(" ");
                         }
                     }
                     System.out.println("]");
                     done = true;
                 }
                 repairDamage();
             }
         }
     }
 
     private class Scaler extends AbstractAction {
         final boolean smaller;
 
         public Scaler(boolean smaller) {
             this.smaller = smaller;
         }
 
         public void actionPerformed(ActionEvent e) {
             if (smaller) {
                 scale++;
                 if (scale > SCALE_DOWN_FACTORS - 1) {
                     scale = SCALE_DOWN_FACTORS - 1;
                 }
             } else {
                 scale--;
                 if (scale < -SCALE_UP_FACTORS) {
                     scale = -SCALE_UP_FACTORS;
                 }
             }
 
             if (scale < 0) {
                 scaleVal = 1 * (double) (1 << -scale);
             } else {
                 scaleVal = 1 / (double) (1 << scale);
             }
 
             //            System.out.println("scale: " + scale + ", scaleVal: " +
             // scaleVal);
 
             bufferPaint();
             repaint();
         }
     }
 
     final static Effects effects;
 
     private final static BufferedImage font = ResourceUtils
             .loadImage("font.png");
 
     private final static int FONT_HEIGHT = 16;
 
     private final static int FONT_SPACE = 1;
 
     private final static int FONT_WIDTH = 8;
 
     private final static int FONT_MARGIN = 2;
 
     private final static int SCALE_DOWN_FACTORS = 5;
 
     private final static int SCALE_UP_FACTORS = 2;
 
     private final static BufferedImage[] player = ResourceUtils
             .loadScaledImages("player.png", SCALE_DOWN_FACTORS,
                     SCALE_UP_FACTORS);
 
     private final static int PLAYER_BORDER = 2;
 
     private final static int LEVEL_MARGIN = 12;
 
     private final static int TILE_SIZE = 32;
 
     private final static BufferedImage[] tiles = ResourceUtils
             .loadScaledImages("tiles.png", SCALE_DOWN_FACTORS, SCALE_UP_FACTORS);
 
     private final static int TILES_ACROSS = 16;
 
     static {
         //        Effects e1 = new NESEffects();
         Effects e2 = new TextEffects();
         CompoundEffects e = new CompoundEffects();
         //        e.add(e1);
         e.add(e2);
         effects = e;
     }
 
     private BufferedImage backBuffer;
 
     boolean done;
 
     boolean showBizarro;
 
     private int playerDir;
 
     IntTriple laser;
 
     final File levelFile;
 
     int scale = 0;
 
     double scaleVal = 1.0;
 
     Level theLevel;
 
     private int xScroll;
 
     private int yScroll;
 
     private int paintedTilesAcross;
 
     private int paintedTilesDown;
 
     byte solution[];
 
     int solutionCount;
 
     public EscapeLevelView(File f) {
         super();
 
         setOpaque(true);
 
         levelFile = f;
         initLevel();
 
         playerDir = Level.DIR_DOWN;
 
         setupKeys();
     }
 
     void bufferPaint() {
         if (backBuffer == null) {
             return;
         }
 
         int w = backBuffer.getWidth();
         int h = backBuffer.getHeight();
 
         updateScroll();
 
         Graphics2D g2 = backBuffer.createGraphics();
 
         // clear
         g2.setBackground(Color.BLACK);
         g2.clearRect(0, 0, w, h);
 
         // save clip, setup for drawing level
         Shape clip = g2.getClip();
         g2.clip(new Rectangle(LEVEL_MARGIN, LEVEL_MARGIN, w - 2 * LEVEL_MARGIN,
                 h - 2 * LEVEL_MARGIN));
 
         // save transform and translate
         AffineTransform origAT = g2.getTransform();
         g2.translate(LEVEL_MARGIN, LEVEL_MARGIN);
 
         // paint things within boundaries of level
         paintLevel(g2);
         paintPlayer(g2);
 
         // undo scale and draw laser
         g2.setTransform(origAT);
         g2.translate(LEVEL_MARGIN, LEVEL_MARGIN);
         paintLaser(g2);
 
         // restore clip and draw the rest
         g2.setClip(clip);
         g2.setTransform(origAT);
         paintArrows(g2);
 
         // restore transform and draw title
         g2.setTransform(origAT);
         g2.translate(FONT_MARGIN, FONT_MARGIN);
         paintTitle(g2);
 
         g2.dispose();
     }
 
     private void drawString(Graphics2D g2, String text) {
         StyleStack s = new StyleStack();
 
         Composite ac = g2.getComposite();
 
         int dx = 0;
         int dy = 0;
         for (int i = 0; i < text.length(); i++) {
             char ch = text.charAt(i);
             if (ch == '^') {
                 i++;
                 ch = text.charAt(i);
                 switch (ch) {
                 case '^':
                     break;
                 case '<':
                     s.pop();
                     break;
                 default:
                     s.push(ch);
                 }
             } else {
                 int tile = Characters.getIndexForChar(text.charAt(i));
 
                 int sx = tile * (FONT_WIDTH + FONT_SPACE);
                 int sy = s.getColor() * (FONT_HEIGHT);
 
                 g2.setComposite(AlphaComposite.getInstance(
                         AlphaComposite.SRC_OVER, s.getAlphaValue()));
                 g2.drawImage(font, dx, dy, dx + FONT_WIDTH, dy + FONT_HEIGHT,
                         sx, sy, sx + FONT_WIDTH, sy + FONT_HEIGHT, this);
                 dx += FONT_WIDTH;
             }
         }
         g2.setComposite(ac);
     }
 
     /**
      * @return Returns the dir.
      */
     public int getPlayerDir() {
         return playerDir;
     }
 
     private void initBackBuffer() {
         //        System.out.println("initializing back buffer");
         backBuffer = (BufferedImage) createImage(getWidth(), getHeight());
         //        System.out.println("backBuffer: " + backBuffer);
     }
 
     /**
      * @throws IOException
      * @throws FileNotFoundException
      */
     void initLevel() {
         try {
             theLevel = new Level(new BitInputStream(new FileInputStream(
                     levelFile)));
         } catch (FileNotFoundException e) {
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         }
 
         done = false;
         showBizarro = false;
         laser = theLevel.isDead();
         solution = null;
         solutionCount = 0;
 
         bufferPaint();
         repaint();
     }
 
     protected void paintComponent(Graphics g) {
         if (backBuffer == null || backBuffer.getHeight() != getHeight()
                 || backBuffer.getWidth() != getWidth()) {
             initBackBuffer();
             bufferPaint();
         }
 
         g.drawImage(backBuffer, 0, 0, this);
     }
 
     private void paintPlayer(Graphics2D g2) {
         int zoom = getZoomIndex();
         int tileSize = getTileSize();
 
         int dx = (theLevel.getPlayerX() - xScroll) * tileSize;
         int dy = (theLevel.getPlayerY() - yScroll) * tileSize;
 
         int sx, sy;
         switch (playerDir) {
         case Level.DIR_LEFT:
             sx = 0;
             sy = 0;
             break;
         case Level.DIR_UP:
             sx = tileSize;
             sy = 0;
             break;
         case Level.DIR_RIGHT:
             sx = tileSize;
             sy = tileSize;
             break;
         case Level.DIR_DOWN:
         default:
             sx = 0;
             sy = tileSize;
             break;
         }
 
         g2.drawImage(player[zoom], dx, dy, dx + tileSize, dy + tileSize, sx,
                 sy, sx + tileSize, sy + tileSize, this);
     }
 
     private int getZoomIndex() {
         int zoom = scale;
         if (zoom < 0) {
             zoom = -scale + SCALE_DOWN_FACTORS - 1;
         }
         return zoom;
     }
 
     private void paintLaser(Graphics2D g2) {
         if (laser == null) {
             return;
         }
 
         int d = laser.getD();
 
         int gx = (theLevel.getPlayerX() - xScroll) * TILE_SIZE
                 + (TILE_SIZE >> 1);
         int gy = (theLevel.getPlayerY() - yScroll) * TILE_SIZE
                 + (TILE_SIZE >> 1);
 
         int lx = (laser.getX() - xScroll) * TILE_SIZE;
         int ly = (laser.getY() - yScroll) * TILE_SIZE;
 
         Rectangle outer, inner;
 
         switch (d) {
         case Level.DIR_DOWN:
             lx += TILE_SIZE >> 1;
             ly += TILE_SIZE;
 
             lx *= scaleVal;
             ly *= scaleVal;
             gx *= scaleVal;
             gy *= scaleVal;
 
             outer = new Rectangle(lx - 1, ly, 3, gy - ly);
             inner = new Rectangle(lx, ly, 1, gy - ly);
             break;
         case Level.DIR_UP:
             lx *= scaleVal;
             ly *= scaleVal;
             gx *= scaleVal;
             gy *= scaleVal;
 
             outer = new Rectangle(gx - 1, gy + 1, 3, ly - gy);
             inner = new Rectangle(gx, gy + 1, 1, ly - gy);
             break;
         case Level.DIR_RIGHT:
             lx += TILE_SIZE;
             ly += TILE_SIZE >> 1;
 
             lx *= scaleVal;
             ly *= scaleVal;
             gx *= scaleVal;
             gy *= scaleVal;
 
             outer = new Rectangle(lx, ly - 1, gx - lx, 3);
             inner = new Rectangle(lx, ly, gx - lx, 1);
             break;
         case Level.DIR_LEFT:
             lx *= scaleVal;
             ly *= scaleVal;
             gx *= scaleVal;
             gy *= scaleVal;
 
             outer = new Rectangle(gx + 1, gy - 1, lx - gx, 3);
             inner = new Rectangle(gx + 1, gy, lx - gx, 1);
             break;
         default:
             outer = inner = null;
         }
 
         g2.setColor(Color.RED);
         g2.fill(outer);
         g2.setColor(Color.WHITE);
         g2.fill(inner);
     }
 
     private void paintLevel(Graphics2D g2) {
         int zoom = getZoomIndex();
         int tileSize = getTileSize();
         //        System.out.println("tilesize: " + tileSize);
         //        System.out.println("zoom: " + zoom);
 
         for (int j = 0; j < theLevel.getHeight() - yScroll; j++) {
             for (int i = 0; i < theLevel.getWidth() - xScroll; i++) {
                 int dx = i * tileSize;
                 int dy = j * tileSize;
 
                 int tile;
                 if (showBizarro) {
                     tile = theLevel.oTileAt(i + xScroll, j + yScroll);
                } else {
                    tile = theLevel.tileAt(i + xScroll, j + yScroll);
                 }

                paintTile(g2, zoom, tileSize, dx, dy, tile);
             }
         }
     }
 
     private void paintTile(Graphics2D g2, int zoom, int tileSize, int dx,
            int dy, int tile) {
         int sx = tile % TILES_ACROSS * tileSize;
         int sy = tile / TILES_ACROSS * tileSize;
 
         g2.drawImage(tiles[zoom], dx, dy, dx + tileSize, dy + tileSize, sx, sy,
                 sx + tileSize, sy + tileSize, this);
     }
 
     private int getTileSize() {
         if (scale < 0) {
             return TILE_SIZE * (1 << -scale);
         } else {
             return TILE_SIZE / (1 << scale);
         }
     }
 
     private void paintArrows(Graphics2D g) {
         int h = backBuffer.getHeight();
         int w = backBuffer.getWidth();
 
         AffineTransform t = g.getTransform();
 
         if (xScroll > 0) {
             // left arrow
             int x = 3;
             int y = h / 2;
             g.translate(x, y);
             drawString(g, Characters.PICS + Characters.ARROWL + Characters.POP);
             g.setTransform(t);
         }
         if (yScroll > 0) {
             // top arrow
             int x = w / 2;
             int y = -4;
             g.translate(x, y);
             drawString(g, Characters.PICS + Characters.ARROWU + Characters.POP);
             g.setTransform(t);
         }
         if (paintedTilesAcross + xScroll < theLevel.getWidth()) {
             // right arrow
             int x = w - LEVEL_MARGIN;
             int y = h / 2;
             g.translate(x, y);
             drawString(g, Characters.PICS + Characters.ARROWR + Characters.POP);
             g.setTransform(t);
         }
         if (paintedTilesDown + yScroll < theLevel.getHeight()) {
             // down arrow
             int x = w / 2;
             int y = h - LEVEL_MARGIN;
             g.translate(x, y);
             drawString(g, Characters.PICS + Characters.ARROWD + Characters.POP);
             g.setTransform(t);
         }
     }
 
     private void paintTitle(Graphics2D g2) {
         String text = theLevel.getTitle() + " " + Characters.GRAY + "by "
                 + Characters.POP + Characters.BLUE + theLevel.getAuthor()
                 + Characters.POP;
 
         drawString(g2, text);
     }
 
     void repairDamage() {
         bufferPaint();
         repaint();
     }
 
     void updateScroll() {
         // keep at least LEVEL_MARGIN everywhere
         paintedTilesAcross = (int) ((getWidth() - (2 * LEVEL_MARGIN)) / (TILE_SIZE * scaleVal));
         paintedTilesDown = (int) ((getHeight() - (2 * LEVEL_MARGIN)) / (TILE_SIZE * scaleVal));
 
         int w = theLevel.getWidth();
         int h = theLevel.getHeight();
         if (paintedTilesAcross > w) {
             paintedTilesAcross = w;
         }
         if (paintedTilesDown > h) {
             paintedTilesDown = h;
         }
 
         int playerBorderX = PLAYER_BORDER;
         int playerBorderY = PLAYER_BORDER;
 
         if (paintedTilesAcross < playerBorderX * 2 + 1) {
             playerBorderX = Math.round(paintedTilesAcross / 2f) - 1;
             if (playerBorderX < 0) {
                 playerBorderX = 0;
             }
         }
 
         if (paintedTilesDown < playerBorderY * 2 + 1) {
             playerBorderY = Math.round(paintedTilesDown / 2f) - 1;
             if (playerBorderY < 0) {
                 playerBorderY = 0;
             }
         }
 
         //        System.out.println("pbx: " + playerBorderX + ", pby: " +
         // playerBorderY);
 
         final int playerX = theLevel.getPlayerX();
         final int playerY = theLevel.getPlayerY();
         final int playerScreenX = playerX - xScroll;
         final int playerScreenY = playerY - yScroll;
 
         if (playerScreenX < playerBorderX) {
             //            System.out.println("scroll left!");
             xScroll = playerX - playerBorderX;
         } else if (playerScreenX > (paintedTilesAcross - 1) - playerBorderX) {
             //            System.out.println("scroll right!");
             xScroll = playerX - (paintedTilesAcross - 1) + playerBorderX;
         }
 
         if (playerScreenY < playerBorderY) {
             //            System.out.println("scroll up!");
             yScroll = playerY - playerBorderY;
         } else if (playerScreenY > (paintedTilesDown - 1) - playerBorderY) {
             //            System.out.println("scroll down!");
             yScroll = playerY - (paintedTilesDown - 1) + playerBorderY;
         }
 
         // normalize
         final int maxXScroll = w - paintedTilesAcross;
         final int maxYScroll = h - paintedTilesDown;
 
         if (xScroll < 0) {
             xScroll = 0;
         } else if (xScroll > maxXScroll) {
             xScroll = maxXScroll;
         }
 
         if (yScroll < 0) {
             yScroll = 0;
         } else if (yScroll > maxYScroll) {
             yScroll = maxYScroll;
         }
 
         //        System.out.println("pta: " + paintedTilesAcross + ", ptd: "
         //                + paintedTilesDown + ", xs: " + xScroll + ", ys: " + yScroll);
     }
 
     /**
      * @param dir
      *            The dir to set.
      */
     void setPlayerDir(int dir) {
         if (dir != Level.DIR_DOWN && dir != Level.DIR_LEFT
                 && dir != Level.DIR_RIGHT && dir != Level.DIR_UP) {
             throw new IllegalArgumentException("Bad direction");
         }
         this.playerDir = dir;
     }
 
     void addToSolution(int dir) {
         if (solution == null) {
             solution = new byte[16];
         }
 
         solutionCount++;
 
         if (solutionCount == solution.length) {
             // extend
             byte b[] = new byte[(int) (solution.length * 1.5)];
             System.arraycopy(solution, 0, b, 0, solution.length);
             solution = b;
         }
 
         solution[solutionCount - 1] = (byte) dir;
     }
 
     private void setupKeys() {
         // moving
         addAction("LEFT", "goLeft", new Mover(Level.DIR_LEFT));
         addAction("DOWN", "goDown", new Mover(Level.DIR_DOWN));
         addAction("RIGHT", "goRight", new Mover(Level.DIR_RIGHT));
         addAction("UP", "goUp", new Mover(Level.DIR_UP));
 
         // scaling
         addAction("CLOSE_BRACKET", "scaleUp", new Scaler(false));
         addAction("OPEN_BRACKET", "scaleDown", new Scaler(true));
 
         // bizarro
         addAction("Y", "toggleAlt", new AbstractAction() {
             public void actionPerformed(ActionEvent e) {
                 showBizarro = !showBizarro;
                 repairDamage();
             }
         });
 
         // restart
         addAction("ENTER", "restart", new AbstractAction() {
             public void actionPerformed(ActionEvent e) {
                 initLevel();
             }
         });
 
         // quit
         addAction("ESCAPE", "exit", new AbstractAction() {
             public void actionPerformed(ActionEvent e) {
                 System.exit(0);
             }
         });
     }
 
     private void addAction(String keyStroke, String name, Action a) {
         getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                 KeyStroke.getKeyStroke(keyStroke), name);
         getActionMap().put(name, a);
     }
 }
