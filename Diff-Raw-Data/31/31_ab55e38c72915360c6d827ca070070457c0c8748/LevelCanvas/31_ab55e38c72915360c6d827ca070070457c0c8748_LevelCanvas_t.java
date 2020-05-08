 /*
  * Created on Dec 19, 2004
  */
 package org.spacebar.escape.j2se;
 
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.Rectangle;
 import java.awt.Shape;
 import java.awt.geom.AffineTransform;
 import java.awt.image.BufferedImage;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 
 import org.spacebar.escape.common.*;
 import org.spacebar.escape.solver.Level;
 
 /**
  * @author adam
  */
 public class LevelCanvas extends DoubleBufferCanvas {
 
     /**
      * 
      */
     private static final long serialVersionUID = 6295616802465884961L;
 
     final static Effects effects;
 
     private final static int FONT_MARGIN = 2;
 
     private final static int PLAYER_BORDER = 2;
 
     private final static int LEVEL_MARGIN_X = 12;
 
     private final static int LEVEL_MARGIN_Y = 15;
 
     static {
         //        Effects e1 = new NESEffects();
         Effects e2 = new TextEffects();
         CompoundEffects e = new CompoundEffects();
         //        e.add(e1);
         e.add(e2);
         effects = e;
     }
 
     //    boolean done;
 
     private boolean showBizarro;
 
     //    IntTriple laser;
 
     int scale = 0;
 
     //    double scaleVal = 1.0;
 
     ListenableLevel theLevel;
 
     String status;
 
     private int xScroll;
 
     private int yScroll;
 
     private int paintedTilesAcross;
 
     private int paintedTilesDown;
 
     private BufferedImage levelSurface;
 
     public LevelCanvas(Level l) {
         super();
         setLevel(l);
     }
 
     public LevelCanvas(Level l, Continuation c) {
         super(c);
         setLevel(l);
     }
 
     @Override
 	synchronized protected void bufferPaint(Graphics2D g) {
         int w = getWidth();
         int h = getHeight();
 
         updateScroll();
 
         // clear
         g.setBackground(Color.BLACK);
         g.clearRect(0, 0, w, h);
 
         // save clip, setup for drawing level
         Shape clip = g.getClip();
         g.clip(new Rectangle(LEVEL_MARGIN_X, LEVEL_MARGIN_Y, w - 2
                 * LEVEL_MARGIN_X, h - 2 * LEVEL_MARGIN_Y));
 
         // save transform and translate
         AffineTransform origAT = g.getTransform();
         g.translate(LEVEL_MARGIN_X, LEVEL_MARGIN_Y);
 
         // paint things within boundaries of level
         paintLevel(g);
         paintSprites(g);
 
         // restore clip and draw the rest
         g.setTransform(origAT);
         g.setClip(clip);
         paintArrows(g);
 
         // restore transform and draw title
         g.setTransform(origAT);
         g.translate(FONT_MARGIN, FONT_MARGIN);
         paintTitle(g);
 
         // restore transform and draw status
         g.setTransform(origAT);
         g.translate(FONT_MARGIN, h - Characters.FONT_HEIGHT - 1);
         paintStatus(g);
 
         g.setTransform(origAT);
     }
 
     /**
      * @param g
      */
     private void paintLevel(Graphics2D g) {
         if (levelSurface == null) {
             initLevelSurface();
            theLevel.dirty.setAllDirty();
         }
 //        System.out.println(theLevel.dirty);
        if (theLevel.dirty.isAnyDirty()) {
             paintLevelToSurface();
         }
         int sx1 = xScroll * Drawing.getTileSize(scale);
         int sy1 = yScroll * Drawing.getTileSize(scale);
         int w = theLevel.getWidth() * Drawing.getTileSize(scale);
         int h = theLevel.getHeight() * Drawing.getTileSize(scale);
         int sx2 = sx1 + w;
         int sy2 = sy1 + h;
         
 //        System.out.print("drawImage... ");
 //        System.out.flush();
 //        long t = System.currentTimeMillis();
         g.drawImage(levelSurface, 0, 0, w, h, sx1, sy1, sx2, sy2, null);
 //        System.out.println("done in " + (System.currentTimeMillis() - t));
     }
 
     private void initLevelSurface() {
         levelSurface = getGraphicsConfiguration().createCompatibleImage(
                 theLevel.getWidth() * Drawing.getTileSize(scale),
                 theLevel.getHeight() * Drawing.getTileSize(scale));
     }
 
     /**
      * @throws IOException
      * @throws FileNotFoundException
      */
     private void initLevel() {
         showBizarro = false;
         status = null;
 
         bufferRepaint();
     }
 
     private void paintLevelToSurface() {
         Graphics2D bg = levelSurface.createGraphics();
         try {
             Drawing.paintLevel(bg, theLevel, 0, 0, showBizarro, scale);
         } finally {
             bg.dispose();
         }
     }
 
     private void paintSprites(Graphics2D g) {
         Drawing.paintSprites(g, theLevel, xScroll, yScroll, scale);
 
     }
 
     private void paintArrows(Graphics2D g) {
         int h = getHeight();
         int w = getWidth();
 
         AffineTransform t = g.getTransform();
 
         if (xScroll > 0) {
             // left arrow
             int x = 3;
             int y = h / 2;
             g.translate(x, y);
             Drawing.drawString(g, Characters.PICS + Characters.ARROWL
                     + Characters.POP);
             g.setTransform(t);
         }
         if (yScroll > 0) {
             // top arrow
             int x = w / 2;
             int y = -4;
             g.translate(x, y);
             Drawing.drawString(g, Characters.PICS + Characters.ARROWU
                     + Characters.POP);
             g.setTransform(t);
         }
         if (paintedTilesAcross + xScroll < theLevel.getWidth()) {
             // right arrow
             int x = w - LEVEL_MARGIN_X;
             int y = h / 2;
             g.translate(x, y);
             Drawing.drawString(g, Characters.PICS + Characters.ARROWR
                     + Characters.POP);
             g.setTransform(t);
         }
         if (paintedTilesDown + yScroll < theLevel.getHeight()) {
             // down arrow
             int x = w / 2;
             int y = h - LEVEL_MARGIN_Y;
             g.translate(x, y);
             Drawing.drawString(g, Characters.PICS + Characters.ARROWD
                     + Characters.POP);
             g.setTransform(t);
         }
     }
 
     private void paintTitle(Graphics2D g2) {
         String text = theLevel.getTitle() + " " + Characters.GRAY + "by "
                 + Characters.POP + Characters.BLUE + theLevel.getAuthor()
                 + Characters.POP;
 
         Drawing.drawString(g2, text);
     }
 
     private void paintStatus(Graphics2D g2) {
         if (status != null) {
             Drawing.drawString(g2, status);
         }
     }
 
     void updateScroll() {
         int tileSize = Drawing.getTileSize(scale);
 
         // keep at least LEVEL_MARGIN everywhere
         paintedTilesAcross = ((getWidth() - (2 * LEVEL_MARGIN_X)) / tileSize);
         paintedTilesDown = ((getHeight() - (2 * LEVEL_MARGIN_Y)) / tileSize);
 
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
 
     public void setLevel(Level l) {
         this.theLevel = new ListenableLevel(l);
        theLevel.trackDirty(true);
         theLevel.addMoveListener(new MoveListener() {
             public void moveOccurred(boolean success) {
                 bufferRepaint();
             }
         });
 
         initLevel();
     }
 
     public void swapWithBizarro() {
         showBizarro = !showBizarro;
        theLevel.dirty.setAllDirty();
     }
 
     public void setRelativeScale(int s) {
         int s2 = -s + scale;
         setScale(s2);
     }
 
     private void setScale(int s) {
         if (s > Drawing.SCALE_DOWN_FACTORS - 1) {
             s = Drawing.SCALE_DOWN_FACTORS - 1;
         } else if (s < -Drawing.SCALE_UP_FACTORS) {
             s = -Drawing.SCALE_UP_FACTORS;
         }
         scale = s;
        theLevel.dirty.setAllDirty();
 
         bufferRepaint();
     }
 }
