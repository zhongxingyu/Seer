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
 
 
 public class TrackMapPanel extends JPanel {
 
     //BufferedImage img;
     TrackLayout lyt;
     AbstractList<Train> trainList;
 
     private int x;
     private int y;
 
     public TrackMapPanel(TrackLayout lyt) {
         //try {
         //    img = ImageIO.read(new File("map.png"));
         //} catch (IOException e) {}
 
         this.lyt = lyt;
         x = 0;
         y = 0;
     }
 
     private void drawTrackBlock(Block blk, Graphics g) {
         Graphics2D g2 = (Graphics2D) g;
 
         Stroke s = new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
 
         g2.setStroke(s);
         
         if (blk == lyt.getSelectedElement()) {
             g2.setPaint(Color.WHITE);
         } else if (blk.isOccupied()) {
            g2.setPaint(Color.YELLOW);
         } else {
             g2.setPaint(Color.RED);
         }
 
         
         //g.drawLine(blk.mapX1, blk.mapY1, blk.mapX2, blk.mapY2);
         g2.draw(new Line2D.Double(blk.mapX1, blk.mapY1, blk.mapX2, blk.mapY2));
 
         if (blk.isStation) {
             int xAvg = (blk.mapX1 + blk.mapX2)/2;
             int yAvg = (blk.mapY1 + blk.mapY2)/2;
             //g.fillOval(xAvg-4, yAvg-4, 8, 8);
             g2.fill(new Ellipse2D.Double(xAvg-6,yAvg-6,12,12));
         }
             
     }
 
     public void setTrainList(AbstractList<Train> trainList) {
         this.trainList = trainList;
     }
 
     public void paintComponent(Graphics g) {
         super.paintComponent(g);
         //g.drawImage(img, 0, 0, null);
 
         /* Draw track */
         ListIterator<Block> iter = lyt.getBlocks().listIterator();
 
         while (iter.hasNext()) {
             drawTrackBlock(iter.next(), g);
         }
 
         /* Draw trains */
         //ListIterator<Block> iter = 
     }
 }
