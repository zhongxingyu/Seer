 package jDistsim.core.simulation.modules.lib.sender;
 
 import jDistsim.ui.module.ColorScheme;
 import jDistsim.ui.module.ModuleView;
 import jDistsim.utils.math.PointUtilities;
 
 import java.awt.*;
 
 /**
  * Author: Jirka Pénzeš
  * Date: 1.3.13
  * Time: 20:28
  */
 public class SenderView extends ModuleView {
 
     public SenderView() {
     }
 
     public SenderView(ColorScheme colorScheme) {
         super(colorScheme);
     }
 
     @Override
     protected void initializeConnectedPoints(int width, int height) {
        inputPoints.add(new Point(0, height / 2));
     }
 
     @Override
     public Polygon getBounds(int width, int height) {
         Polygon polygon = new Polygon();
        polygon.addPoint(0, height / 2 - 1);
        polygon.addPoint(PointUtilities.byPercentageOnLine(40, width), 1);
        polygon.addPoint(width - 1, 1);
        polygon.addPoint(width - 1, height - 1);
        polygon.addPoint(PointUtilities.byPercentageOnLine(40, width), height - 1);
         return polygon;
     }
 
     @Override
     protected void postDraw(Graphics2D graphics, int width, int height) {
         super.postDraw(graphics, width, height);
         /*
         Color color = getColorScheme().getBorderColor();
         int msgWidth = PointUtilities.byPercentageOnLine(60, width);
 
         graphics.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 50));
         graphics.drawLine(1, 1, msgWidth / 2, PointUtilities.byPercentageOnLine(65, height));
         graphics.drawLine(msgWidth, 1, msgWidth / 2, PointUtilities.byPercentageOnLine(65, height));
         graphics.drawLine(1, height - 1, PointUtilities.byPercentageOnLine(30, msgWidth), PointUtilities.byPercentageOnLine(50, height));
         graphics.drawLine(msgWidth - 1, height - 1, PointUtilities.byPercentageOnLine(70, msgWidth), PointUtilities.byPercentageOnLine(50, height));
         */
     }
 }
