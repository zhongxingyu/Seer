 package edu.uwm.cs552;
 
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.Stroke;
 
 import edu.uwm.cs552.util.Pair;
 
 public enum Barrier {
   INLET, DRAWN, BLOCK, RIVER;
 
   public static final Color WATER_COLOR = new Color(0x75, 0xC5, 0xF0);
 
  public void draw(Graphics g, HexEdge e, double scale, boolean showHidden) {
     Graphics2D g2 = (Graphics2D) g;
     Stroke savedStroke = g2.getStroke();
     Pair<Point, Point> p = e.toLineSegment(scale);
 
     Color color = null;
     int thickness = 0;
 
     switch (this) {
     case BLOCK:
       color = Color.BLACK;
      if (showHidden)
         thickness = (int) (scale / 5);
       break;
     case INLET:
       color = WATER_COLOR;
      if (showHidden)
         thickness = (int) (scale / 5);
       break;
     case RIVER:
       color = WATER_COLOR;
      if (showHidden)
         thickness = (int) (scale / 5);
       else
         thickness = (int) (scale / 10);
       break;
     case DRAWN:
       color = WATER_COLOR;
       thickness = (int) (scale / 5);
     }
 
     if (thickness > 0) {
       g2.setColor(color);
       // TODO : save off the stroke thickness for efficiency
       g2.setStroke(new BasicStroke(thickness, BasicStroke.CAP_ROUND,
           BasicStroke.JOIN_ROUND));
       g2.drawLine(p.fst.x, p.fst.y, p.snd.x, p.snd.y);
       g2.setStroke(savedStroke);
     }
   }
 }
