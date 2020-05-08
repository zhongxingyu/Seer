 package jDistsim.ui.module;
 
 import jDistsim.core.simulation.modules.IModuleView;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.ComponentAdapter;
 import java.awt.event.ComponentEvent;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Author: Jirka Pénzeš
  * Date: 2.11.12
  * Time: 12:41
  */
 public abstract class ModuleView implements IModuleView {
 
     private JComponent view;
    private ColorScheme colorScheme;
     private ColorScheme defaultColorScheme;
 
     protected final List<Point> inputPoints;
     protected final List<Point> outputPoints;
 
     protected ModuleView() {
         this(null);
     }
 
     protected ModuleView(ColorScheme colorScheme) {
         this.defaultColorScheme = colorScheme;
         setDefaultColorScheme();
 
         view = new ModuleViewComponent();
         inputPoints = new ArrayList<>();
         outputPoints = new ArrayList<>();
 
         view.addComponentListener(new ComponentAdapter() {
             @Override
             public void componentResized(ComponentEvent componentEvent) {
                 invalidateConnectedPoints();
             }
         });
     }
 
     @Override
     public void setDefaultColorScheme(ColorScheme defaultColorScheme) {
         this.defaultColorScheme = defaultColorScheme;
         if (colorScheme == null) setDefaultColorScheme();
     }
 
     public ColorScheme getColorScheme() {
         return colorScheme;
     }
 
     public void setColorScheme(ColorScheme colorScheme) {
         this.colorScheme = colorScheme;
     }
 
     public void invalidateConnectedPoints() {
         invalidateConnectedPoints(view.getWidth(), view.getHeight());
     }
 
     public void invalidateConnectedPoints(int width, int height) {
         inputPoints.clear();
         outputPoints.clear();
         initializeConnectedPoints(width, height);
     }
 
     protected abstract void initializeConnectedPoints(int width, int height);
 
     @Override
     public JComponent getContentPane() {
         return view;
     }
 
     @Override
     public void draw(Graphics2D graphics, int width, int height) {
         Graphics2D graphics2D = graphics;
         setDefaultRenderingMode(graphics2D);
         setDefaultBasicStroke(graphics2D);
 
         Polygon polygon = getBounds(width, height);
         graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
         GradientPaint gradientPaint = new GradientPaint(0, 0, colorScheme.getBackgroundColorA(), 0, height, colorScheme.getBackgroundColorB());
         Paint currentPaint = graphics2D.getPaint();
         graphics2D.setPaint(gradientPaint);
         graphics2D.fillPolygon(polygon);
         graphics2D.setPaint(currentPaint);
         postDraw(graphics2D, width, height);
         graphics2D.setColor(colorScheme.getBorderColor());
         graphics2D.drawPolygon(polygon);
     }
 
     protected void postDraw(Graphics2D graphics, int width, int height) {
     }
 
     protected void setDefaultRenderingMode(Graphics2D graphics2D) {
         graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
     }
 
     protected void setDefaultBasicStroke(Graphics2D graphics2D) {
         graphics2D.setStroke(new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
     }
 
     public void setDefaultColorScheme() {
         colorScheme = defaultColorScheme;
     }
 
     @Override
     public List<Point> getInputPoints() {
         return inputPoints;
     }
 
     @Override
     public List<Point> getOutputPoints() {
         return outputPoints;
     }
 
     private class ModuleViewComponent extends JComponent {
 
         public ModuleViewComponent() {
             setOpaque(false);
         }
 
         @Override
         protected void paintComponent(Graphics graphics) {
             super.paintComponent(graphics);
             draw((Graphics2D) graphics, getWidth(), getHeight());
         }
     }
 }
