 /*
  *  This file is part of Pac Defence.
  *
  *  Pac Defence is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  Pac Defence is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with Pac Defence.  If not, see <http://www.gnu.org/licenses/>.
  * 
  *  (C) Liam Byrne, 2008 - 09.
  */
 
 package gui;
 
 import gui.maps.MapParser.GameMap;
 import images.ImageHelper;
 
 import java.awt.AlphaComposite;
 import java.awt.Color;
 import java.awt.Composite;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.GraphicsConfiguration;
 import java.awt.GraphicsEnvironment;
 import java.awt.Point;
 import java.awt.Polygon;
 import java.awt.RenderingHints;
 import java.awt.Shape;
 import java.awt.Transparency;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.image.BufferedImage;
 import java.util.List;
 
 import javax.swing.JPanel;
 
 
 @SuppressWarnings("serial")
 public class GameMapPanel extends JPanel {
    
    private final boolean debugTimes;
    private final boolean debugPath;
    
    private final BufferedImage backgroundImage;
    //Have two precreated buffers as recreating them at each step is much slower
    private BufferedImage front;
    private BufferedImage back;
    private final List<Polygon> path;
    private final List<Point> pathPoints;
    private final List<Shape> pathBounds;
 
    private GameOver gameOver = null;
    private final TextDisplay textDisplay;
    
    private long lastPaintTime = 0;
 
    public GameMapPanel(int width, int height, GameMap map, boolean debugTimes, boolean debugPath) {
       this.debugTimes = debugTimes;
       this.debugPath = debugPath;
       GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment().
             getDefaultScreenDevice().getDefaultConfiguration();
       backgroundImage = gc.createCompatibleImage(width, height, Transparency.OPAQUE);
       front = gc.createCompatibleImage(width, height, Transparency.OPAQUE);
       back = gc.createCompatibleImage(width, height, Transparency.OPAQUE);
       Graphics g = backgroundImage.getGraphics();
       // Draws the actual map (maze)
       g.drawImage(map.getImage(), 0, 0, width, height, null);
       setDoubleBuffered(false);
       setPreferredSize(new Dimension(width, height));
       pathPoints = map.getPathPoints();
       path = map.getPath();
       pathBounds = map.getPathBounds();
       textDisplay = new TextDisplay(width, height);
       if(debugPath) {
          printClickedCoords();
       }
       setIgnoreRepaint(true);
    }
    
    @Override
    public synchronized void paintComponent(Graphics g) {
       long beginTime = System.nanoTime();
       g.drawImage(front, 0, 0, null);
       lastPaintTime = System.nanoTime() - beginTime;
    }
    
    public void displayText(String... lines) {
       textDisplay.displayText(lines);
    }
    
    public void removeText() {
       textDisplay.clear();
    }
    
    public void restart() {
       gameOver = null;
       removeText();
       repaint();
    }
    
    public void signalGameOver() {
       if(gameOver == null) {
          gameOver = new GameOver();
       }
    }
    
    public long draw(Iterable<Drawable> drawables, long processTime, long processSpritesTime,
          long processBulletsTime, long processTowersTime, long drawTime, int numBullets) {
       if(gameOver == null) {
          Graphics2D g = back.createGraphics();
          // The default value for alpha interpolation causes significant lag
          g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,
                RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
          //System.out.println(g.getRenderingHint(RenderingHints.KEY_ANTIALIASING) ==
          //      RenderingHints.VALUE_ANTIALIAS_ON);
          //g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
          drawUpdate(g, drawables, processTime, processSpritesTime, processBulletsTime, processTowersTime,
                drawTime, numBullets);
          textDisplay.draw(g);
          flip();
          g.dispose();
       } else {
          // Game over needs to always draw on the same buffer for its sliding effect
          gameOver.draw(front.createGraphics());
       }
       return lastPaintTime;
    }
    
    // draw implementation that uses a VolatileImage instead of a BufferedImage
    // for the buffer, which should be faster, but it was about as fast in most
    // cases, except much slower at drawing transparencies (under Xubuntu)
    /*public long draw(List<Tower> towers, Tower buildingTower, List<Sprite> sprites,
          List<Bullet> bullets, long processTime, long processSpritesTime,
          long processBulletsTime, long processTowersTime, long drawTime, int numBullets) {
       if(gameOver == null) {
          int nextIndex = (bufferIndex + 1) % buffers.length;
          VolatileImage vi = buffers[nextIndex];
          do {
             vi.validate(gc);
             Graphics2D g = vi.createGraphics();
             drawUpdate(g, towers, buildingTower, sprites, bullets, processTime,
                   processSpritesTime, processBulletsTime, processTowersTime, drawTime, numBullets);
             textDisplay.draw(g);
             g.dispose();
          } while(vi.contentsLost());
          bufferIndex = nextIndex;
       } else {
          // Game over needs to always draw on the same buffer for its sliding effect
           
          // Needs changing to check the VolatileImage, maybe copy to a BufferedImage
          gameOver.draw(buffers[bufferIndex].createGraphics());
       }
       return lastPaintTime;
    }*/
    
    /**
     * Flips the front and back buffers
     */
    private void flip() {
       BufferedImage temp = back;
       back = front;
       front = temp;
    }
    
    private void drawUpdate(Graphics g, Iterable<Drawable> drawables, long processTime,
          long processSpritesTime, long processBulletsTime, long processTowersTime, long drawTime,
          int numBullets) {
       // This should completely cover the old image in the buffer
       g.drawImage(backgroundImage, 0, 0, null);
       for(Drawable d : drawables) {
         // These should never be null, but occasionally, I think due to threading, they are
         if(d != null) {
            d.draw(g);
         }
       }
       drawDebug(g, processTime, processSpritesTime, processBulletsTime, processTowersTime,
             drawTime, numBullets);
    }
    
    private void drawDebug(Graphics g, long processTime, long processSpritesTime,
          long processBulletsTime, long processTowersTime, long drawTime, int numBullets) {
       if(debugTimes) {
          g.setColor(Color.WHITE);
          g.drawString("Process time: " + processTime, 10, 15);
          g.drawString("Draw time: " + drawTime, 10, 30);
          g.drawString("Sprites time: " + processSpritesTime, 150, 15);
          g.drawString("Bullets time: " + processBulletsTime, 150, 30);
          g.drawString("Towers time: " + processTowersTime, 150, 45);
          g.drawString("Num bullets: " + numBullets, 400, 15);
       }
       if(debugPath) {
          drawPath(g);
          drawPathOutline(g);
          drawPathBounds(g);
       }
    }
    
    /**
     * Test method that draws in each point on the path and links them up.
     */
    private void drawPath(Graphics g) {
       g.setColor(Color.BLACK);
       Point lastPoint = null;
       for(Point p : pathPoints) {
          int x = p.x;
          int y = p.y;
          // These two lines are the cross at each point
          g.drawLine(x - 10, y - 10, x + 10, y + 10);
          g.drawLine(x - 10, y + 10, x + 10, y - 10);
          if (lastPoint != null) {
             g.drawLine(lastPoint.x, lastPoint.y, x, y);
          }
          lastPoint = p;
       }
    }
    
    /**
     * Test method that draws an outline of the path.
     */
    private void drawPathOutline(Graphics g) {
       for(Polygon p : path) {
          g.setColor(Color.RED);
          g.drawPolygon(p);
          g.setColor(new Color(0, 0, 0, 90));
          g.fillPolygon(p);
       }
    }
    
    private void drawPathBounds(Graphics g) {
       Graphics2D g2D = (Graphics2D) g;
       g2D.setColor(new Color(0, 0, 255, 30));
       for(Shape s : pathBounds) {
          g2D.fill(s);
       }
       g2D.setColor(new Color(0, 0, 255, 100));
       for(Shape s : pathBounds) {
          g2D.draw(s);
       }
    }
    
    /**
     * Test method that prints the coords of a mouse click.
     */
    private void printClickedCoords() {
       addMouseListener(new MouseAdapter(){
          @Override
          public void mouseClicked(MouseEvent e) {
             System.out.println("<point x=\"" + e.getX() + "\" y=\"" + e.getY() + "\" />");
          }
       });
    }
    
    /**
     * Draws game over screen for a loss
     * 
     * @author Liam Byrne
     *
     */
    private class GameOver {
       
       private final int framesBetweenRedraw = 1;
       private int framesUntilRedraw = 0;
       private int deltaY, deltaX;
       private BufferedImage img;
       private int currentX, currentY, iterations, numTimes;
       
       private GameOver() {
          deltaY = 1;
          img = ImageHelper.makeImage((int)(getWidth()*0.75), (int)(getHeight()*0.2), "other",
                "GameOver.png");
          double mult = 1.2;
          currentY = (int)(img.getHeight() * mult);
          numTimes = (int)(((getHeight() * 0.75) - img.getHeight() * mult - currentY)/deltaY);
          currentX = getWidth()/2 - img.getWidth()/2;
       }
       
       private void draw(Graphics2D g) {
          if(iterations > numTimes) {
             return;
          }
          if(framesUntilRedraw > 0) {
             framesUntilRedraw--;
          } else {
             framesUntilRedraw = framesBetweenRedraw;
             currentY += deltaY;
             currentX += deltaX;
             iterations++;
          }
          // Save the current composite to reset back to later
          Composite c = g.getComposite();
          // Makes it so what is drawn is partly transparent
          g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
                0.7f/(framesBetweenRedraw + 1)));
          g.drawImage(img, currentX, currentY, null);
          g.setComposite(c);
       }
    }
 
    private static class TextDisplay {
       
       private static final Color backgroundColour = Color.WHITE;
       private static final Color textColour = Color.BLACK;
       private static final Composite composite =
             AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .5F);
       private final int startPosition;
       private int currentPosition;
       private int heightToDisplay;
       // The amount of rounding on the corners
       private static final int rounding = 40;
       private static final int aboveTextMargin = 7;
       private static final int belowTextMargin = 10;
       // The margin on the side from the left of the TextDisplay (not the screen)
       private static final int sideMargin = 7;
       // The offset from the side of the screen for the TextDisplay
       private static final int offset = 3;
       // The height of the screen
       private final int screenHeight;
       private final BufferedImage image;
       private boolean isOnDisplay = false;
       private boolean goingUp;
       
       /**
        * Creates a new TextDisplay for a screen with the specified width and height
        * 
        * @param width
        * @param height
        */
       public TextDisplay(int width, int height) {
          // It starts at the bottom of the screen
          startPosition = height;
          currentPosition = startPosition;
          this.screenHeight = height;
          image = new BufferedImage(width - offset * 2, height, BufferedImage.TYPE_INT_ARGB_PRE);
       }
       
       public void draw(Graphics g) {
          if(!isOnDisplay) {
             return;
          }
          adjustImagePosition();
          // Only draw if it's still on display
          if(isOnDisplay) {
             Graphics2D g2D = (Graphics2D) g;
             // Use a composite to make it translucent, save the previous composite to restore later
             Composite c = g2D.getComposite();
             g2D.setComposite(composite);
             g2D.drawImage(image, offset, currentPosition, null);
             g2D.setComposite(c);
          }
       }
       
       public void displayText(String... lines) {
          if(isOnDisplay) {
             throw new RuntimeException("There is already text on display.");
          }
          drawImage(lines);
          goingUp = true;
          isOnDisplay = true;
       }
       
       public void clear() {
          heightToDisplay = 0;
          goingUp = false;
       }
       
       private void adjustImagePosition() {
          // Adjust the position of the display
          if(goingUp) {
             // Decrement the current position until it reaches the full height on display
             if(currentPosition > startPosition - heightToDisplay) {
                currentPosition--;
             }
          } else {
             currentPosition++;
             // When it has gone off screen, reset currentPosition and set it so it's not on display
             if(currentPosition >= startPosition) {
                currentPosition = startPosition;
                isOnDisplay = false;
             }
          }
       }
       
       private void drawImage(String[] lines) {
          ImageHelper.clearImage(image);
          Graphics2D g = image.createGraphics();
          
          // Draw the background
          g.setColor(backgroundColour);
          g.fillRoundRect(0, 0, image.getWidth(), screenHeight, rounding, rounding);
          
          // Draw the text on top
          int lineHeight = g.getFontMetrics().getHeight() + aboveTextMargin;
          g.setColor(textColour);
          g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
          for(String s : lines) {
             heightToDisplay += lineHeight;
             g.drawString(s, sideMargin, heightToDisplay);
          }
          
          heightToDisplay += belowTextMargin;
       }
       
    }
 
 }
