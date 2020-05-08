 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package SubwayMaker.Elements;
 
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.geom.Point2D;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 /**
  *
  * @author Yichen
  */
 public class Station extends Element {
 
     public static final float OUTER_DIA_SCALE = 1f;
     public static final float OUTER_LINK_SCALE = 0.6f;
     public static final float INNER_TO_OUTER = 0.7f;
     private boolean complete;
     private List<Point2D> terminalList;
 
     public Station() {
         complete = false;
         terminalList = new ArrayList<>();
     }
 
     @Override
     public void add(Point2D pt, boolean mod) {
         pt = nearPoint(pt);
         terminalList.add(pt);
     }
 
     @Override
     public void paint(Graphics2D g, int blocksize) {
         int outerDia = Math.round(OUTER_DIA_SCALE * blocksize);
         float outerLink = OUTER_DIA_SCALE * OUTER_LINK_SCALE * blocksize;
 
         g.setColor(Color.black);
         g.setStroke(new BasicStroke(outerLink, BasicStroke.CAP_ROUND, BasicStroke.CAP_ROUND));
         paintLayer(g, blocksize, outerDia);
 
         g.setColor(Color.white);
         g.setStroke(new BasicStroke(outerLink - (outerDia * (1 - INNER_TO_OUTER)), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
         paintLayer(g, blocksize, (int) (outerDia * INNER_TO_OUTER));
     }
 
     private void paintLayer(Graphics2D g, int blocksize, int diameter) {
         Point2D curTer, prevTer = null;
         for (Iterator<Point2D> it = terminalList.iterator(); it.hasNext();) {
             curTer = pointScale(it.next(), blocksize);
 
            g.fillOval((int) Math.round(curTer.getX() - diameter / 2.0), (int) Math.round(curTer.getY() - diameter / 2.0), diameter, diameter);
 
             if (prevTer != null) {
                 g.drawLine((int) prevTer.getX(), (int) prevTer.getY(), (int) curTer.getX(), (int) curTer.getY());
             }
 
             prevTer = curTer;
         }
     }
 
     @Override
     public void complete() {
         complete = true;
     }
 }
