 package TKM;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.awt.image.*;
 import java.awt.geom.*;
 import java.io.*;
 import javax.imageio.*;
 import javax.swing.*;
 import java.util.AbstractList;
 import java.util.ListIterator;
 import TNM.Train;
 
 
 /* TrackMapPanel will provide a sophisticated track map */
 /* A comment */
 
 
 public class TrackMapPanel extends JPanel implements MouseListener{
 
     BufferedImage xingIcon;
     TrackLayout lyt;
     //AbstractList<Train> trainList;
 
     private int x;
     private int y;
 
     public TrackMapPanel(TrackLayout lyt) {
         try {
             xingIcon = ImageIO.read(new File("xing_icon.png"));
         } catch (IOException e) {}
 
         this.lyt = lyt;
         x = 0;
         y = 0;
 
         addMouseListener(this);
     }
 
     private void drawTrain(Graphics g, Train tn) {
         /* Draw the train front */
         Block blk = tn.positionBlock;
         double dist_fact = tn.positionMeters/blk.length;
         int xFr = (int)((blk.mapX2 - blk.mapX1)*dist_fact + blk.mapX1);
         int yFr = (int)((blk.mapY2 - blk.mapY1)*dist_fact + blk.mapY1);
         double thetaFr = Math.atan2((blk.mapY2 - blk.mapY1),(blk.mapX2 - blk.mapX1));
         if (tn.positionDirection == Block.DIRECTION_REV) {
             thetaFr += Math.PI;
         }
         Graphics2D g2 = (Graphics2D) g;
 
         AffineTransform saveAt = g2.getTransform();
         AffineTransform dirAt = new AffineTransform();
         dirAt.rotate(thetaFr, xFr, yFr);
         g2.transform(dirAt);
 
         g2.setPaint(Color.BLUE);
         //g2.fill(new Ellipse2D.Double(xFr-8,yFr-4,16,8));
         g2.draw(new Line2D.Double(xFr,yFr,xFr-12,yFr-8));
         g2.draw(new Line2D.Double(xFr,yFr,xFr-12,yFr+8));
         g2.draw(new Line2D.Double(xFr-12,yFr-8,xFr-12,yFr+8));
 
         /* Restore saved transform */
         g2.setTransform(saveAt);
     }
 
     private void drawTrackBlock(Graphics g, Block blk, Color tkColor) {
         Graphics2D g2 = (Graphics2D) g;
 
         float[] dashUground = {1f, 0f};
         float[] dashAground = {1f, 0f};
 
         float[] dash = (blk.isUground) ? dashUground : dashAground;
 
         Stroke s = new BasicStroke(7f, BasicStroke.CAP_ROUND,
                 BasicStroke.JOIN_MITER, 1f, dash, 0f);
 
         g2.setStroke(s);
         
         if (blk == lyt.getSelectedElement()) {
             g2.setPaint(Color.CYAN);
         } else if (blk.isOccupied()) {
             g2.setPaint(Color.YELLOW);
         } else {
             g2.setPaint(tkColor);
         }
 
         
         //g.drawLine(blk.mapX1, blk.mapY1, blk.mapX2, blk.mapY2);
         g2.draw(new Line2D.Double(blk.mapX1, blk.mapY1, blk.mapX2, blk.mapY2));
 
 
     }
 
     private void drawTrackOverlay(Graphics g, Block blk, Color tkColor) {
         Graphics2D g2 = (Graphics2D) g;
 
         Stroke s = new BasicStroke(2f, BasicStroke.CAP_ROUND,
                 BasicStroke.JOIN_MITER);
         g2.setStroke(s);
 
         if (blk == lyt.getSelectedElement()) {
             g2.setPaint(Color.CYAN);
         } else if (blk.isOccupied()) {
             g2.setPaint(Color.YELLOW);
         } else {
             g2.setPaint(Color.WHITE);
         }
 
         int xAvg = (blk.mapX1 + blk.mapX2)/2;
         int yAvg = (blk.mapY1 + blk.mapY2)/2;
 
         if (blk.isStation) {
             g2.fill(new Ellipse2D.Double(xAvg-5,yAvg-5,10,10));
             g.setColor(Color.WHITE);
             g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 9));
             g.drawString(blk.stationName, xAvg + 12, yAvg + 12);
         }
 
         if (blk.isCrossing) {
            //g.setColor(Color.MAGENTA);
            //g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
             //g.drawString("X", xAvg + 6, yAvg + 6);
             g.drawImage(xingIcon, xAvg - 10, yAvg - 10, null);
         }
             
     }
 
     private void drawSwitch(Graphics g, Switch sw) {
         Graphics2D g2 = (Graphics2D) g;
 
         g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
         g2.setPaint(new Color(255,255,255,128));
         g2.fill(new Rectangle2D.Double(sw.mapX1-8, sw.mapY1-8, 16, 16));
 
         if (sw == lyt.getSelectedElement()) {
             g2.setPaint(Color.CYAN);
         } else {
             g2.setPaint(Color.WHITE);
         }
         
         String str = (sw.state == Switch.STATE_DIVERGENT) ? "D" : "S";
         g.drawString(str, sw.mapX1-4, sw.mapY1+4);
     }
 
     public void paintComponent(Graphics g) {
         super.paintComponent(g);
 
         Graphics2D g2 = (Graphics2D)g;
         g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                         RenderingHints.VALUE_ANTIALIAS_ON);
         
         //g.drawImage(img, 0, 0, null);
         g.setColor(new Color(20, 20, 30));
         g.fillRect(0, 0, 520, 670);
 
         for (TrackLayout.TrackLine line : lyt.getLines()) {
 
             Color tkColor;
 
             if (line == lyt.redLine) {
                 tkColor = new Color(192,0,0);
             } else if (line == lyt.greenLine){
                 tkColor = new Color(0,128,0);
             } else {
                 tkColor = null;
                 System.out.println("Unknown line");
                 System.exit(1);
             }
 
             
             /* Draw track */
             for (Block blk : line.getBlocks()) {
                 drawTrackBlock(g, blk, tkColor);
             }
 
             for (Block blk : line.getBlocks()) {
                 drawTrackOverlay(g, blk, tkColor);
             }
 
             /* Draw switches */
             for (Switch sw : line.getSwitches()) {
                 drawSwitch(g, sw);
             }
         }
 
         /* Draw trains */
         if (lyt.trains != null)
         {
             for (Train t : lyt.trains) {
                 drawTrain(g, t);
             }
         }
     }
 
     public void mouseClicked(MouseEvent e) {
         //System.out.printf("%d %d\n", e.getX(), e.getY());
        /* Check if we clicked a track block */
         }
     public void mouseEntered(MouseEvent e) {
         }
     public void mouseExited(MouseEvent e) {
         }
     public void mousePressed(MouseEvent e) {
         }
     public void mouseReleased(MouseEvent e) {
         }
 }
