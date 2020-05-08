 package net.avh4.framework.uilayer.scene;
 
 import net.avh4.framework.uilayer.Font;
 import net.avh4.framework.uilayer.swing.SwingUILayerService;
 
 import java.awt.*;
 
 public class SwingGraphicsOperations implements GraphicsOperations {
    public static final String PROPERTY_KEY_USE_ANTIALIASING = "uilayer.swing.antialiasing";
    public static final boolean USE_ANTIALIASING = System.getProperty(PROPERTY_KEY_USE_ANTIALIASING, "true").equals("true");

     private Graphics g;
 
     @Override
     public void drawRect(int leftX, int topY, int width, int height, int argbColor) {
         g.setColor(loadColor(argbColor));
         g.fillRect(leftX, topY, width, height);
     }
 
     @Override
     public void drawText(String text, float leftX, float baselineY, Font font, int argbColor) {
         g.setColor(loadColor(argbColor));
         g.setFont(SwingUILayerService.loadFont(font));
         g.drawString(text, (int) leftX, (int) baselineY);
     }
 
     @Override
     public void translate(int deltaX, int deltaY) {
         g.translate(deltaX, deltaY);
     }
 
     @Override
     public void drawLine(int startX, int startY, int stopX, int stopY, int argbColor) {
         g.setColor(loadColor(argbColor));
         g.drawLine(startX, startY, stopX, stopY);
     }
 
     @Override
     public void drawOval(int leftX, int topY, int width, int height, int argbColor) {
         g.setColor(loadColor(argbColor));
         g.fillOval(leftX, topY, width, height);
     }
 
     @Override
     public void drawImage(String image, int destLeftX, int destTopY, int destRightX, int destBottomY,
                           int sourceLeftX, int sourceTopY, int sourceRightX, int sourceBottomY) {
         g.drawImage(SwingUILayerService.loadImage(image), destLeftX, destTopY, destRightX, destBottomY,
                 sourceLeftX, sourceTopY, sourceRightX, sourceBottomY, null);
     }
 
     public void setGraphicsContext(Graphics g) {
         if (g instanceof Graphics2D) {
             final Graphics2D g2 = (Graphics2D) g;
            if (USE_ANTIALIASING) {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            }
         }
         this.g = g;
     }
 
     private static Color loadColor(final int color) {
         return new Color(color);
     }
 }
