 /*
  * Copyright 2012 University of Helsinki.
  *
  * This file is part of BMVis².
  *
  * BMVis² is free software: you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License, or (at your option) any later version.
  *
  * BMVis² is distributed in the hope that it will be useful, but
  * WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with BMVis².  If not, see
  * <http://www.gnu.org/licenses/>.
  */
 
 package biomine.bmvis2;
 
 import biomine.bmvis2.color.ColorPalette;
 import biomine.bmvis2.utils.ResourceUtils;
 import com.kitfox.svg.SVGDiagram;
 
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Graphics2D;
 import java.awt.geom.RectangularShape;
 import java.awt.geom.RoundRectangle2D;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.imageio.ImageIO;
 
 /**
  * Base class for edges and nodes.
  *
  * @author alhartik
  */
 public class LabeledItem extends LayoutItem {
     public static final Set<String> nonVisibleAttributes = new HashSet<String>();
 
     static {
         nonVisibleAttributes.add("pos");
         nonVisibleAttributes.add("pinned");
     }
 
     ;
 
     public LabeledItem(VisualGraph graph) {
         super(graph);
         shape = new RoundRectangle2D.Double();
         ((RoundRectangle2D.Double) shape).archeight = 20;
         ((RoundRectangle2D.Double) shape).arcwidth = 20;
     }
 
     private static BufferedImage pinImage;
 
     static {
         pinImage = new BufferedImage(1, 1, BufferedImage.TYPE_4BYTE_ABGR);
         try {
            InputStream pinStream = LabeledItem.class.getResourceAsStream("/pin.png");
             if (pinStream == null) {
                pinStream = new FileInputStream(new File("/pin.png"));
             }
             pinImage = ImageIO.read(pinStream);
         } catch (Exception e) {
             Logging.error("graph_drawing", "Could not load pin image.");
         }
     }
 
     private Collection<String> labels;
     private RectangularShape shape;
 
     public RectangularShape getShape() {
         return shape;
     }
 
     public void setShape(RectangularShape r) {
         shape = r;
     }
 
     private Color baseColor;
     private Color hlColor;
     private Color neighborHLColor;
     private Color strokeColor = Color.BLACK;
     private double strokeWidth = 0.0;
 
     /**
      * Height of font, updated in paint
      */
     private int textHeight = 0;
     /**
      * Horizontal padding
      */
     private final int hpad = 5;
 
     /**
      * @param mw string width
      */
     private void setShapeFrame(double mw) {
         Vec2 p = getPos();
         int l = labels == null ? 0 : labels.size();
         int vpad = 4;
         shape.setFrame(p.x - mw / 2 - hpad, p.y - textHeight * 0.5 * l - vpad, mw + hpad * 2, vpad * 2 - 8 + textHeight * l);
     }
 
     public void paint(Graphics2D g) {
         Vec2 p = getPos();
 
         Font f = g.getFont();
         int FONT_SIZE = 10;
         Font f2 = new Font(f.getName(), 0, FONT_SIZE);
         g.setFont(f2);
 
         int mw = 0;
         if (labels != null) {
             for (String lbl : labels) {
                 if (lbl == null) lbl = "";
                 int w = g.getFontMetrics(f2).stringWidth(lbl);
                 if (w > mw)
                     mw = w;
             }
         }
         Collection<String> labels = getLabels();
         textHeight = g.getFontMetrics(f2).getHeight();
         int l = labels == null ? 0 : labels.size();
 
         setShapeFrame(mw);
         // if (shape == null || mw > shape.getWidth() - 10) {
         //shape.setFrame(p.x - mw / 2 - 5, p.y - textHeight * 0.5 * l - 8,
         //		mw + 10, 14 + textHeight * l);
         // }
 
         Color colorToUse = getColor();
 
         if (colorToUse != null) {
             g.setColor(colorToUse);
             g.fill(shape);
 
             g.setColor(strokeColor);
             BasicStroke stroke = new BasicStroke((float) (strokeWidth));
 
             g.setStroke(stroke);
 
             if (strokeWidth != 0)
                 g.draw(shape);
         }
 
         if (isPositionFixed() && !this.getGraph().getPrintMode()) {
             double sz = 16;
 
             try {
                SVGDiagram diagram = ResourceUtils.getSVGDiagramByPath("/thumbtack.svg");
                 int x = (int) (shape.getX() + shape.getWidth() - 12);
                 int y = (int) (shape.getY() - 11);
                 Graphics2D pinGraphics = (Graphics2D) g.create(x, y, 24, 19);
                 diagram.render(pinGraphics);
             } catch (Exception e) {
                 e.printStackTrace();
                 g.drawImage(pinImage, (int) (shape.getX() + shape.getWidth() - 10),
                         (int) (shape.getY() - 11), null);
             }
 
             /*
                 * g.setColor(new Color(233,222,200)); double sz = 10; Arc2D a = new
                 * Arc2D.Double(
                 * shape.getX()+shape.getWidth()-sz*1.5,shape.getY()+shape
                 * .getHeight()-sz*1.5, sz,sz,0,360,Arc2D.CHORD); g.fill(a);
                 * g.setColor(Color.BLACK); g.draw(a);
                 */
         }
 
         g.setColor(ColorPalette.getTextColorForBackground(colorToUse));
 
         if (g.getTransform().getScaleX() > 0.5) {
             int i = 0;
 
             if (labels != null)
                 for (String lbl : labels) {
                     if (lbl == null) continue;
                     int w = g.getFontMetrics(f2).stringWidth(lbl);
 
                     g.drawString(lbl, (float) p.x - w / 2, (float) (p.y
                             + textHeight * 0.5 - textHeight * 0.5 * l + textHeight
                             * i));
                     i++;
                 }
         }
     }
 
     private void updateRect() {
         if (shape == null)
             return;
         Vec2 p = getPos();
 
         double mw = 40;
         int l = labels == null ? 0 : labels.size();
         if (shape != null) {
             mw = shape.getWidth();
         }
         int extraPad = 0;
         setShapeFrame(mw - hpad * 2);
 //		shape.setFrame(p.x - mw / 2, p.y - textHeight * 0.5 * l - 8, mw, 0
 //				+ textHeight * l);
     }
 
     public void setPos(Vec2 pos) {
         super.setPos(pos);
 
         updateRect();
     }
 
     @Override
     public boolean containsPoint(Vec2 v) {
         if (shape == null)
             return false;
 
         return shape.contains(v.x, v.y);
     }
 
     @Override
     public boolean containsPoint(double x, double y) {
         if (shape == null)
             return false;
 
         return shape.contains(x, y);
     }
 
     protected void setLabels(Collection<String> labels) {
         this.labels = new ArrayList<String>(labels);
         updateRect();
         // shape = null;
     }
 
     public Collection<String> getLabels() {
         return labels;
     }
 
     public void setBaseColor(Color color) {
         this.baseColor = color;
     }
 
     public Color getBaseColor() {
         return baseColor;
     }
 
     public void setHLColor(Color hlColor) {
         this.hlColor = hlColor;
     }
 
     public Color getHLColor() {
         return hlColor;
     }
 
     public void setNeighborHLColor(Color neighborHLColor) {
 
         this.neighborHLColor = neighborHLColor;
     }
 
     public Color getNeighborHLColor() {
         return neighborHLColor;
     }
 
     public void setStrokeColor(Color strokeColor) {
         this.strokeColor = strokeColor;
     }
 
     public Color getStrokeColor() {
         return strokeColor;
     }
 
     public void setStrokeWidth(double pStrokeWidth) {
         this.strokeWidth = (float) pStrokeWidth;
     }
 
     public double getStrokeWidth() {
         return strokeWidth;
     }
 
     public Color getColor() {
         Color colorToUse = null;
         if (isHighlighted()) {
             colorToUse = getHLColor();
         } else if (isNeighborHighlighted()) {
 
             colorToUse = getNeighborHLColor();
         } else {
             colorToUse = getBaseColor();
         }
         return colorToUse;
     }
 }
