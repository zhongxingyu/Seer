 package jDistsim.core.simulation.modules.lib.receiver;
 
 import jDistsim.ui.module.ColorScheme;
 import jDistsim.ui.module.ModuleView;
 import jDistsim.utils.math.PointUtilities;
 
 import java.awt.*;
 
 /**
  * Author: Jirka Pénzeš
  * Date: 5.3.13
  * Time: 17:11
  */
 public class ReceiverView extends ModuleView {
 
     public ReceiverView() {
         super();
     }
 
     public ReceiverView(ColorScheme colorScheme) {
         super(colorScheme);
     }
 
     @Override
     protected void initializeConnectedPoints(int width, int height) {
        outputPoints.add(new Point(width - 1, height / 2));
     }
 
     @Override
     public Polygon getBounds(int width, int height) {
         Polygon polygon = new Polygon();
        polygon.addPoint(1, 1);
        polygon.addPoint(PointUtilities.byPercentageOnLine(60, width), 1);
        polygon.addPoint(width - 1, height / 2 - 1);
        polygon.addPoint(PointUtilities.byPercentageOnLine(60, width), height - 1);
        polygon.addPoint(1, height - 1);
         return polygon;
     }
 }
