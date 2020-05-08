 package de.htwg.se.dog.view.modules;
 
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.RenderingHints;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.geom.Arc2D;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map.Entry;
 import java.util.SortedMap;
 import java.util.TreeMap;
 
 import javax.imageio.ImageIO;
 import javax.swing.JPanel;
 
 import de.htwg.se.dog.controller.GameTableInterface;
 import de.htwg.se.dog.models.FieldInterface;
 import de.htwg.se.dog.models.GameFieldInterface;
 
 /**
  * Draws the gamefield
  * 
  * @author Michael
  * 
  */
 public class GuiDrawGameField extends JPanel implements MouseListener {
 
     private static final int THREE = 3;
     private static final long serialVersionUID = 1L;
     private static int radius;
     private static final int HUNDRED = 100;
     private static final int CIRCLE = 360;
     private static final int MAXRADIUS = 90;
     private static final int NORM = 35;
     private static final int HEIGHTNORM = 25;
     private static final double TWOTHREE = 2.3;
     private static final double ONETWO = 1.2;
     private static final double STRINGX = 0.78;
     private static final double STRINGY = 0.35;
 
     private GameTableInterface controller;
 
     private static boolean second;
     // Map with all Gamefields
     private static SortedMap<Integer, Arc2D.Double> gMap;
     // Map with highlighted GameFields
     private static ColorMap col = new ColorMap();
     private static GameFieldInterface game;
     private static FieldInterface[] array;
     private static List<Integer> fromto = new ArrayList<Integer>();
 
     /**
      * initializes the panel and sets the controller this panel is working with
      * 
      * @param controller
      *            the gamecontroller
      */
     public GuiDrawGameField(GameTableInterface controller) {
         this.controller = controller;
         gMap = new TreeMap<Integer, Arc2D.Double>();
         this.addMouseListener(this);
         game = controller.getGameField();
         array = game.getGameArray();
     }
 
     /**
      * house fields are treated different cause they need to be inside the
      * circle in one line
      * 
      * @t: defines at which radius the point will be drawn next point will only
      *     be drawn when counter is inkremented so we make sure that counter
      *     gets only inkremented after all houses have been drawn or after each
      *     normal field
      * 
      * @dif: defines where the next point is inside the circle
      * 
      * @r2: the radius of a 1 gamefield
      */
     @Override
     protected void paintComponent(Graphics g) {
         gMap.clear();
         Graphics2D g2d = (Graphics2D) g;
         super.paintComponent(g);
 
         setRendering(g2d);
         radius = (int) ((this.getHeight() / TWOTHREE) - NORM);
         int house = game.getHouseCount() * game.getPlayerCount();
         int size = game.getFieldSize();
         int start = game.getFieldsTillHouse() + game.getHouseCount();
         int a = getWidth() / 2 + HEIGHTNORM;
         int b = getHeight() / 2 + HEIGHTNORM;
         drawImageMiddle(g2d);
         // PREVENT too big fields e.g when playing with 1:1 fields
         double r2 = 2 * Math.PI * radius / size;
         if (r2 > MAXRADIUS) {
             r2 = MAXRADIUS;
         }
         // next point on outer circle
         double t = 0;
         // counter for normalfields
         int counter = 1;
         // counter for housefields
         int counterhouse = 1;
         // -radius where the house field will be drawn
         double dif = -(r2 * ONETWO);
         // x,y coordinates for points
         double x, y;
         Arc2D.Double gArc;
         g2d.setStroke(new BasicStroke(radius / HUNDRED));
         g2d.setFont(new Font("Tahoma", Font.BOLD, (int) Math.round(r2 * 1 / 2)));
 
         for (int i = 0; i < size; i++) {
 
             if (array[i].isHouse()) {
 
                 x = a + (radius + dif) * -Math.cos(t);
                 y = b + (radius + dif) * -Math.sin(t);
                 gArc = new Arc2D.Double(x - r2, y - r2, r2, r2, 0, CIRCLE, Arc2D.OPEN);
                 dif -= (r2 * ONETWO);
                 g2d.setColor(col.getColor(array[i].getOwner()));
                 setArcType(g2d, gArc, i);
                 drawString(g2d, String.valueOf(counterhouse + 1), r2, x, y);
 
                 if (counterhouse == game.getHouseCount()) {
                     counter++;
                 }
                 counterhouse++;
             } else {
 
                 dif = -(r2 * ONETWO);
                 counterhouse = 0;
                 drawColoredField(g2d, i);
                 t = 2 * Math.PI * (counter) / (size - house);
                 x = a + radius * -Math.cos(t);
                 y = b + radius * -Math.sin(t);
                 gArc = new Arc2D.Double(x - r2, y - r2, r2, r2, 0, CIRCLE, Arc2D.OPEN);
                 startFieldArc(g2d, start, r2, x, y, gArc, i);
                 counter++;
             }
             gMap.put(i, gArc);
 
         }
         /**
          * override the fields with the number and playercolor when they have
          * been clicked
          */
         highlightFields(g2d, r2);
     }
 
     /**
      * Highlight the fields in gHIgh map
      * 
      * @param g2d
      * @param r2
      */
     private void highlightFields(Graphics2D g2d, double r2) {
         for (Integer f : fromto) {
             g2d.setColor(col.getColor(controller.getCurrentPlayerID()));
             Arc2D.Double arc = gMap.get(f);
             g2d.fill(arc);
             drawString(g2d, String.valueOf(f), r2, arc.x + r2, arc.y + r2);
         }
     }
 
     /**
      * draw image in the middle of Panel
      * 
      * @param g2d
      */
     private void drawImageMiddle(Graphics2D g2d) {
         // draw comic dog in the middle of this panel
         try {
             BufferedImage img = ImageIO.read(new File(this.getClass().getResource("/dog.jpg").getPath()));
             g2d.drawImage(img, (getWidth() + NORM - img.getWidth()) / 2, (getHeight() + NORM - img.getHeight()) / 2, null);
         } catch (IOException e) {
             System.exit(1);
         }
     }
 
     /**
      * set the rendering quality
      * 
      * @param g2d
      */
     private void setRendering(Graphics2D g2d) {
         // Maximum quality
         RenderingHints renderHints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
         renderHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
         g2d.setRenderingHints(renderHints);
     }
 
     /**
      * sets and draws the arc, filled or unfilles
      * 
      * @param g2d
      * @param gArc
      * @param i
      */
     private void setArcType(Graphics2D g2d, Arc2D.Double gArc, int i) {
         if (array[i].getFigureOwnerNr() != -1) {
             g2d.fill(gArc);
         } else {
             g2d.draw(gArc);
         }
     }
 
     /**
      * Decides what colore the field will be drawn with
      * 
      * @param g2d
      * @param i
      */
     private void drawColoredField(Graphics2D g2d, int i) {
         if (array[i].getFigureOwnerNr() != -1) {
             g2d.setColor(col.getColor(array[i].getFigureOwnerNr()));
         } else {
             g2d.setColor(Color.GRAY);
         }
     }
 
     /**
      * Draw the startfield arc
      * 
      * @param g2d
      * @param start
      * @param r2
      * @param x
      * @param y
      * @param gArc
      * @param i
      */
     private void startFieldArc(Graphics2D g2d, int start, double r2, double x, double y, Arc2D.Double gArc, int i) {
        int size = game.getFieldSize();
         if (i % start == 0 && !array[i].isBlocked()) {
             if (array[i].getFigure() == null) {
                 // set startfield color
                 g2d.setColor(col.getColor(array[((size - 1) + i) % size].getOwner()));
                 g2d.draw(gArc);
             } else {
                 g2d.fill(gArc);
             }
             drawString(g2d, "S", r2, x, y);
         } else {
             g2d.fill(gArc);
         }
         if (array[i].isBlocked()) {
             drawString(g2d, "B", r2, x, y);
         }
     }
 
     /**
      * Draw a String on this Panel
      * 
      * @param g2d
      *            the graphics interface
      * @param s
      *            the string that shoule be drawn
      * @param r2
      *            the radius of the circle in which the String gets places
      * @param x
      *            coordinate
      * @param y
      *            coordinate
      */
     private void drawString(Graphics2D g2d, String s, double r2, double x, double y) {
 
         g2d.setColor(Color.BLACK);
         g2d.drawString(String.format("%2s", s), Float.parseFloat(String.valueOf(x - r2 * STRINGX)), Float.parseFloat(String.valueOf(y - r2 * STRINGY)));
     }
 
     @Override
     public void mouseClicked(MouseEvent arg0) {}
 
     @Override
     public void mouseEntered(MouseEvent arg0) {}
 
     @Override
     public void mouseExited(MouseEvent arg0) {}
 
     /**
      * Highlight a gamefield when we klick on it
      */
     @Override
     public void mousePressed(MouseEvent arg0) {
         int current = controller.getCurrentPlayerID();
         for (Entry<Integer, Arc2D.Double> a : gMap.entrySet()) {
             if (a.getValue().contains(arg0.getX(), arg0.getY())) {
                 Integer feldId = a.getKey();
                 if (fromto.contains(feldId)) {
                     if (fromto.indexOf(feldId) == 0) {
                         fromto.clear();
                     } else {
                         fromto.remove(feldId);
                     }
                     repaint();
                     break;
                 }
                 buildListWithActualHighlighters(current, feldId);
                 repaint();
                 break;
             }
         }
     }
 
     /**
      * Builds a list with the actual highlighters
      * 
      * @param current
      *            the currentplayer
      * @param feldId
      *            the fieldid to be highlighted
      */
     private void buildListWithActualHighlighters(int current, Integer feldId) {
         if (fromto.size() == 1 && !second) {
             fromto.remove(0);
         }
         if (array[feldId].getFigureOwnerNr() == current && fromto.size() < 1) {
             fromto.add(0, feldId);
         }
         // second highlighter for switch move
         else if (secondHighlighterAllowed(current, feldId)) {
             fromto.add(1, feldId);
             if (fromto.size() == THREE) {
                 fromto.remove(2);
             }
         }
     }
 
     private boolean secondHighlighterAllowed(int current, Integer feldId) {
         boolean ok = second && array[feldId].getFigure() != null;
         boolean ok2 = array[feldId].getFigureOwnerNr() == current || !array[feldId].isBlocked();
         return (fromto.size() > 0 && ok && ok2);
     }
 
     @Override
     public void mouseReleased(MouseEvent arg0) {
         // TODO Auto-generated method stub
     }
 
     /**
      * clears the highlighter
      */
     public void clearField() {
         fromto.clear();
         second = false;
     }
 
     /**
      * returns the fieldID for the starting field
      * 
      * @return the fieldID
      */
     public Integer getFromFieldID() {
         Integer i;
         if (fromto.size() == 0) {
             i = null;
         } else {
             i = fromto.get(0);
         }
         return i;
     }
 
     /**
      * returns the fieldID for the field u are going for
      * 
      * @return the fieldID
      */
     public Integer getToFieldID() {
         Integer i;
         if (fromto.size() != 2) {
             i = null;
         } else {
             i = fromto.get(1);
         }
         return i;
     }
 
     /**
      * allows a second highlighter
      * 
      * @param ok
      */
     public void allowSecond(boolean ok) {
         second = ok;
         if (!ok && fromto.size() == 2) {
             fromto.remove(1);
             repaint();
         }
 
     }
 }
