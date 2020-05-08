 package net.sourcewalker.xpclock;
 
 import java.awt.Color;
 import java.awt.FontMetrics;
 import java.awt.Graphics;
 import java.awt.geom.Rectangle2D;
 import java.util.Calendar;
 
 import javax.swing.JPanel;
 
 public class XpClock extends JPanel {
 
     private static final long serialVersionUID = -6314735624389927332L;
    private static final double CIRCLE = Math.PI * 2;
     private static final String[] HOURS;
     private static final String[] MINUTES;
     private static final String[] SECONDS;
     private static final int CENTER_SIZE = 10;
     private static final double SECOND_RING = 0.2;
     private static final double MINUTE_RING = 0.5;
     private static final double HOUR_RING = 0.8;
     private static final double LABEL_PAD = 15;
 
     static {
         HOURS = new String[24];
         for (int i = 0; i < 24; i++) {
             HOURS[i] = Integer.toString(i);
         }
         MINUTES = new String[12];
         for (int i = 0; i < 12; i++) {
             MINUTES[i] = Integer.toString(i * 5);
         }
         SECONDS = new String[4];
         for (int i = 0; i < 4; i++) {
             SECONDS[i] = Integer.toString(i * 15);
         }
     }
 
     private double hourFraction;
     private double minuteFraction;
     private double secondFraction;
 
     public XpClock() {
         this(Calendar.getInstance());
     }
 
     public XpClock(Calendar calendar) {
         super(true);
         setBackground(Color.BLACK);
         setForeground(Color.WHITE);
 
         setTime(calendar);
     }
 
     public void setTime(Calendar time) {
         double millis = (double) time.get(Calendar.MILLISECOND) / 1000;
         secondFraction = ((double) time.get(Calendar.SECOND) + millis) / 60;
         minuteFraction = ((double) time.get(Calendar.MINUTE) + secondFraction) / 60;
         hourFraction = ((double) time.get(Calendar.HOUR_OF_DAY) + minuteFraction) / 24;
 
         repaint();
     }
 
     public double getHourFraction() {
         return hourFraction;
     }
 
     public double getMinuteFraction() {
         return minuteFraction;
     }
 
     public double getSecondFraction() {
         return secondFraction;
     }
 
     @Override
     public void paint(Graphics g) {
         super.paint(g);
 
         double size = Math.min(getWidth(), getHeight());
         double centerX = size / 2;
         double centerY = size / 2;
        double hourAngle = getHourFraction() * CIRCLE;
        double minuteAngle = hourAngle + getMinuteFraction() * CIRCLE;
        double secondAngle = minuteAngle + getSecondFraction() * CIRCLE;
 
         g.setColor(getForeground());
         drawCenterDot(g, centerX, centerY);
         drawRings(g, centerX, centerY);
         drawHands(g, centerX, centerY, hourAngle, minuteAngle, secondAngle);
         drawText(g, centerX, centerY, hourAngle, minuteAngle);
     }
 
     private void drawText(Graphics g, double centerX, double centerY,
             double hourAngle, double minuteAngle) {
         drawText(g, centerX, centerY, 0, HOUR_RING, HOURS);
         drawText(g, centerX, centerY, hourAngle, MINUTE_RING, MINUTES);
         drawText(g, centerX, centerY, minuteAngle, SECOND_RING, SECONDS);
     }
 
     private void drawText(Graphics g, double centerX, double centerY,
             double startAngle, double size, String[] labels) {
         for (int i = 0; i < labels.length; i++) {
             double frac = (double) i / labels.length;
            double angle = startAngle + frac * CIRCLE;
             double startX = centerX + Math.sin(angle) * centerX * size;
             double startY = centerY - Math.cos(angle) * centerY * size;
             double x = centerX + Math.sin(angle) * centerX * size
                     + Math.sin(angle) * LABEL_PAD;
             double y = centerY - Math.cos(angle) * centerY * size
                     - Math.cos(angle) * LABEL_PAD;
             drawCentered(g, labels[i], (int) startX, (int) startY, (int) x,
                     (int) y);
         }
     }
 
     private void drawCentered(Graphics g, String text, int startX, int startY,
             int x, int y) {
         FontMetrics metrics = g.getFontMetrics();
         Rectangle2D bounds = metrics.getStringBounds(text, g);
         g.drawLine(startX, startY, x, y);
         g.setColor(getBackground());
         g.fillRect((int) (x - bounds.getWidth() / 2),
                 (int) (y - bounds.getHeight() / 2), (int) bounds.getWidth(),
                 (int) bounds.getHeight());
         g.setColor(getForeground());
         g.drawString(text, (int) (x - bounds.getWidth() / 2),
                 (int) (y + metrics.getDescent()));
     }
 
     private void drawCenterDot(Graphics g, double centerX, double centerY) {
         double x = centerX - CENTER_SIZE / 2;
         double y = centerY - CENTER_SIZE / 2;
         g.fillOval((int) x, (int) y, CENTER_SIZE, CENTER_SIZE);
     }
 
     private void drawRings(Graphics g, double centerX, double centerY) {
         drawRing(g, centerX, centerY, SECOND_RING);
         drawRing(g, centerX, centerY, MINUTE_RING);
         drawRing(g, centerX, centerY, HOUR_RING);
     }
 
     private void drawRing(Graphics g, double centerX, double centerY,
             double relSize) {
         double x = centerX - centerX * relSize;
         double y = centerY - centerY * relSize;
         double width = 2 * relSize * centerX;
         double height = 2 * relSize * centerY;
         g.drawOval((int) x, (int) y, (int) width, (int) height);
     }
 
     private void drawHands(Graphics g, double centerX, double centerY,
             double hourAngle, double minuteAngle, double secondAngle) {
         drawHand(g, centerX, centerY, HOUR_RING, MINUTE_RING, hourAngle);
         drawHand(g, centerX, centerY, SECOND_RING, MINUTE_RING, minuteAngle);
         drawHand(g, centerX, centerY, 0, SECOND_RING, secondAngle);
     }
 
     private void drawHand(Graphics g, double centerX, double centerY,
             double start, double end, double position) {
         double startX = centerX + Math.sin(position) * start * centerX;
         double startY = centerY - Math.cos(position) * start * centerY;
         double endX = centerX + Math.sin(position) * end * centerX;
         double endY = centerY - Math.cos(position) * end * centerY;
         g.drawLine((int) startX, (int) startY, (int) endX, (int) endY);
     }
 
 }
