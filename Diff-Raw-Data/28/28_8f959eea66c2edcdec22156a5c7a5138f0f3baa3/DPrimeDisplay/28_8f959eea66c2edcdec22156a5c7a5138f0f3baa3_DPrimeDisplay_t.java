 package edu.mit.wi.haploview;
 
 import java.awt.*;
 import java.awt.image.*;
 import java.awt.font.*;
 //import java.io.*;
 //import java.text.*;
 import javax.swing.*;
 import java.util.*;
 
 class DPrimeDisplay extends JComponent {
     BufferedImage  buffIm;
     boolean alreadyPainted = false;
 
     static final int H_BORDER = 15;
     static final int V_BORDER = 15;
 
     int BOX_SIZE = 50;
     int BOX_RADIUS = 24;
     int TICK_HEIGHT = 8;
     int TICK_BOTTOM = 50;
 
     static final int TEXT_NUMBER_GAP = 3;
 
     int widestMarkerName = 80; //default size
 
     Font boxFont = new Font("SansSerif", Font.PLAIN, 12);
     Font markerNumFont = new Font("SansSerif", Font.BOLD, 12);
     Font markerNameFont = new Font("Default", Font.PLAIN, 12);
 
     boolean markersLoaded;
     PairwiseLinkage dPrimeTable[][];
     Vector markers;
 
     DPrimeDisplay(PairwiseLinkage[][] t, boolean b, Vector v){
         markersLoaded = b;
         dPrimeTable = t;
         markers = v;
     }
 
    public void loadMarkers(Vector v){
        markersLoaded = true;
        alreadyPainted = false;
        markers = v;
        repaint();

    }

     public void paintComponent(Graphics gComponent){
         Dimension size = getSize();
         Dimension pref = getPreferredSize();
 
         if (!(alreadyPainted)){
             buffIm = new BufferedImage(pref.width, pref.height, BufferedImage.TYPE_3BYTE_BGR);
             Graphics g = buffIm.getGraphics();
 
             FontMetrics boxFontMetrics = g.getFontMetrics(boxFont);
 
             int diamondX[] = new int[4];
             int diamondY[] = new int[4];
             Polygon diamond;
 
             int left = H_BORDER;
             int top = V_BORDER;
 
             FontMetrics metrics;
             int ascent;
 
             Graphics2D g2 = (Graphics2D) g;
             g2.setColor(this.getBackground());
             g2.fillRect(0,0,pref.width,pref.height);
             g2.setColor(Color.BLACK);
 
 
             if (markersLoaded) {
                 g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                         RenderingHints.VALUE_ANTIALIAS_ON);
 
                 //// draw the marker locations
 
                 BasicStroke thickerStroke = new BasicStroke(1);
                 BasicStroke thinnerStroke = new BasicStroke(0.25f);
 
                 int wide = dPrimeTable.length * BOX_SIZE;
                 int lineLeft = wide / 4;
                 int lineSpan = wide / 2;
                 long minpos = ((SNP)markers.elementAt(0)).getPosition();
                 long maxpos = ((SNP)markers.elementAt(markers.size()-1)).getPosition();
                 double spanpos = maxpos - minpos;
 
                 g2.setStroke(thinnerStroke);
                 g2.setColor(Color.white);
                 g2.fillRect(left + lineLeft, 5, lineSpan, TICK_HEIGHT);
                 g2.setColor(Color.black);
                 g2.drawRect(left + lineLeft, 5, lineSpan, TICK_HEIGHT);
 
                 for (int i = 0; i < markers.size(); i++) {
                     double pos = (((SNP)markers.elementAt(i)).getPosition() - minpos) / spanpos;
                     int xx = (int) (left + lineLeft + lineSpan*pos);
                     g2.setStroke(thickerStroke);
                     g.drawLine(xx, 5, xx, 5 + TICK_HEIGHT);
                     g2.setStroke(thinnerStroke);
                     g.drawLine(xx, 5 + TICK_HEIGHT,
                             left + i*BOX_SIZE, TICK_BOTTOM);
                 }
                 top += TICK_BOTTOM;
 
                 //// draw the marker names
 
                 g.setFont(markerNameFont);
                 metrics = g.getFontMetrics();
                 ascent = metrics.getAscent();
 
                 widestMarkerName = metrics.stringWidth(((SNP)markers.elementAt(0)).getName());
                 for (int x = 1; x < dPrimeTable.length; x++) {
                     int thiswide = metrics.stringWidth(((SNP)markers.elementAt(x)).getName());
                     if (thiswide > widestMarkerName) widestMarkerName = thiswide;
                 }
                 //System.out.println(widest);
 
                 g2.translate(left, top + widestMarkerName);
                 g2.rotate(-Math.PI / 2.0);
                 TextLayout markerNameTL;
                 for (int x = 0; x < dPrimeTable.length; x++) {
                     g2.drawString(((SNP)markers.elementAt(x)).getName(),TEXT_NUMBER_GAP, x*BOX_SIZE + ascent/3);
                 }
 
                 g2.rotate(Math.PI / 2.0);
                 g2.translate(-left, -(top + widestMarkerName));
 
                 // move everybody down
                 top += widestMarkerName + TEXT_NUMBER_GAP;
 
                 g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                         RenderingHints.VALUE_ANTIALIAS_OFF);
             }
 
             //// draw the marker numbers
 
             g.setFont(markerNumFont);
             metrics = g.getFontMetrics();
             ascent = metrics.getAscent();
             for (int x = 0; x < dPrimeTable.length; x++) {
                 String mark = String.valueOf(x + 1);
                 g.drawString(mark,
                         left + x*BOX_SIZE - metrics.stringWidth(mark)/2,
                         top + ascent);
             }
             top += BOX_RADIUS/2; // give a little space between numbers and boxes
 
             // draw table column by column
             for (int x = 0; x < dPrimeTable.length-1; x++) {
                 for (int y = x + 1; y < dPrimeTable.length; y++) {
                     double d = dPrimeTable[x][y].getDPrime();
                     double l = dPrimeTable[x][y].getLOD();
                     Color boxColor = dPrimeTable[x][y].getColor();
 
                     // draw markers above
 
                     int rt2 = (int) (Math.sqrt(2) * (double)BOX_SIZE);
                     //int rt2half = (int) (0.5 * Math.sqrt(2) * (double)BOX_SIZE);
                     int rt2half = rt2 / 2;
                     //System.out.println(rt2 + " " + rt2half);
 
                     //int xx = left + x*BOX_SIZE + (int) (y*Math.sqrt(4)*BOX_SIZE*0.5);
                     int xx = left + (x + y) * BOX_SIZE / 2;
                     //int yy = top + (x - y) * BOX_SIZE;
                     //int xx = left + x*BOX_SIZE;
                     //int yy = top + y*BOX_SIZE - (int) (x*Math.sqrt(4)*BOX_SIZE*0.5);
                     int yy = top + (y - x) * BOX_SIZE / 2;
 
                     diamondX[0] = xx; diamondY[0] = yy - BOX_RADIUS;
                     diamondX[1] = xx + BOX_RADIUS; diamondY[1] = yy;
                     diamondX[2] = xx; diamondY[2] = yy + BOX_RADIUS;
                     diamondX[3] = xx - BOX_RADIUS; diamondY[3] = yy;
 
                     diamond = new Polygon(diamondX, diamondY, 4);
                     g.setColor(boxColor);
                     g.fillPolygon(diamond);
                     if (boxColor == Color.white) {
                         g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                 RenderingHints.VALUE_ANTIALIAS_ON);
                         g.setColor(Color.lightGray);
                         g.drawPolygon(diamond);
                         g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                 RenderingHints.VALUE_ANTIALIAS_OFF);
                     }
 
                     g.setFont(boxFont);
                     ascent = boxFontMetrics.getAscent();
                     int val = (int) (d * 100);
                     g.setColor((val < 50) ? Color.gray : Color.black);
                     if (val != 100) {
                         String valu = String.valueOf(val);
                         int widf = boxFontMetrics.stringWidth(valu);
                         g.drawString(valu, xx - widf/2, yy + ascent/2);
                     }
                 }
             }
             alreadyPainted=true;
         }
         Graphics2D gComponent2 = (Graphics2D)gComponent;
        //okay so this dumb if block is to prevent the ugly
        //repainting bug when loading markers after the data are already
        //being displayed, results in a little off-centering for small
        //datasets, but not too bad.
        if (!(markersLoaded)){
            gComponent2.translate((size.width - pref.width) / 2,
                    (size.height - pref.height) / 2);
        } else {
            gComponent2.translate((size.width - pref.width) / 2,
                    0);
        }
         gComponent2.drawImage(buffIm,0,0,this);
     }
 
 
     public Dimension getPreferredSize() {
         int count = dPrimeTable.length;
        int high = 2*V_BORDER + count*BOX_SIZE/2;
         if (markersLoaded){
             high += TICK_BOTTOM + widestMarkerName + TEXT_NUMBER_GAP;
         }
        return new Dimension(2*H_BORDER + BOX_SIZE*(count-1), high);
     }
 
     int[] centerString(String s, FontMetrics fm) {
         int[] returnArray = new int[2];
         returnArray[0] = (30-fm.stringWidth(s))/2;
         returnArray[1] = 10+(30-fm.getAscent())/2;
         return returnArray;
     }
 
 }
