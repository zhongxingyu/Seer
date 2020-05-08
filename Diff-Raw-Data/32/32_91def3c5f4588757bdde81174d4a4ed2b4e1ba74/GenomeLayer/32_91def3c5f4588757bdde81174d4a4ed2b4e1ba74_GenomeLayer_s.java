 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package structurevis.ui.layerpanel;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.RenderingHints;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.awt.geom.Line2D;
 import java.awt.geom.Rectangle2D;
 import java.util.ArrayList;
 import javax.swing.JMenuItem;
 import javax.swing.JPopupMenu;
 import javax.swing.SwingUtilities;
 import structurevis.structures.Structure;
 import structurevis.ui.StructureDrawPanel;
 
 /**
  *
  * @author Michael Golden
  */
 public class GenomeLayer extends GeneralLayer implements ActionListener, MouseListener, MouseMotionListener {
 
     //Graphics2D g = null;
     public int rulerHeight = 20;
     public int blockHeight = 25;
     GenomeOrganization genomeOrganization;
     int maxLevel = 0;
     int mouseoverStart = -1;
     int mouseoverEnd = -1;
     int selectedStart = -1;
     int selectedEnd = -1;
     JPopupMenu popupMenu = new JPopupMenu();
     JMenuItem autofitItem = new JMenuItem("Autofit width");
     JMenuItem zoomInItem = new JMenuItem("Zoom in");
     JMenuItem zoomOutItem = new JMenuItem("Zoom out");
     int minorTickMark = 500;
     int majorTickMark = 1000;
     int[] tickMarkPossibilities = {1, 5, 10, 15, 20, 25, 50, 75, 100, 200, 250, 500, 750, 1500, 2000};
     Structure selected = null;
     ArrayList<Structure> structures = null;
     ArrayList<StructureAndMouseoverRegion> structurePositions = null;
 
     public GenomeLayer(LayerPanel layerPanel, GenomeOrganization genomeStructure, String name) {
         super(layerPanel, name);
         setGenomeOrganization(genomeStructure);
         addMouseListener(this);
         addMouseMotionListener(this);
 
         autofitItem.addActionListener(this);
         zoomInItem.addActionListener(this);
         popupMenu.add(zoomOutItem).addActionListener(this);
         popupMenu.add(autofitItem);
         popupMenu.add(zoomInItem);
         popupMenu.add(zoomOutItem);
 
     }
     boolean forceRepaint = true;
 
     public int chooseBestTickMarkSize(int genomeLength) {
         for (int i = tickMarkPossibilities.length - 1; i >= 0; i--) {
             double distanceBetweenTicks = (double) getWidth() / (double) genomeLength * (double) tickMarkPossibilities[i];
             if (distanceBetweenTicks < 40) {
                 if (i < tickMarkPossibilities.length - 1) {
                     return tickMarkPossibilities[i + 1];
                 } else {
                     return tickMarkPossibilities[i];
                 }
             }
         }
         return 1;
     }
 
     @Override
     public void redraw() {
         forceRepaint = true;
         repaint();
     }
 
     public void setGenomeOrganization(GenomeOrganization g) {
         this.genomeOrganization = g;
         if (g != null) {
             for (int i = 0; i < g.genome.size(); i++) {
                 maxLevel = Math.max(maxLevel, g.genome.get(i).level);
             }
             setPreferredSize(new Dimension(10000, rulerHeight + (maxLevel + 1) * blockHeight));
             redraw();
         }
     }
 
     /*
      * @Override public void paintComponent(Graphics graphics) {
      * super.paintComponent(graphics); Graphics2D g2 = (Graphics2D) graphics;
      *
      * int panelWidth = this.getWidth(); int panelHeight = this.getHeight();
      *
      *
      * if (genomeOrganization != null) { if (bufferedImage == null ||
      * bufferedImage.getWidth() != panelWidth || bufferedImage.getHeight() !=
      * panelHeight) { bufferedImage = (BufferedImage)
      * (this.createImage(panelWidth, panelHeight)); g = (Graphics2D)
      * bufferedImage.getGraphics();
      * g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
      * RenderingHints.VALUE_ANTIALIAS_ON); forceRepaint = true; }
      *
      * if (forceRepaint) { forceRepaint = false;
      *
      * g.setColor(Color.white); g.fillRect(0, 0, panelWidth, panelHeight);
      *
      * majorTickMark = chooseBestTickMarkSize(layerPanel.genomeLength);
      * minorTickMark = Math.max(majorTickMark/2,1);
      * System.out.println("M="+majorTickMark);
      *
      * // draw tick marks g.setFont(layerPanel.f2); for (int i = 0; i <
      * layerPanel.genomeLength; i++) { if (i % minorTickMark == 0) { double x =
      * ((double) i / layerPanel.genomeLength) * getWidth();
      * g.setColor(Color.black); Line2D.Double tick = new Line2D.Double(x +
      * xoffset, rulerHeight - 1, x + xoffset, rulerHeight + 1); g.draw(tick); if
      * (i % majorTickMark == 0) { } } if (i % majorTickMark == 0) { double x =
      * ((double) i / layerPanel.genomeLength) * getWidth();
      * g.setColor(Color.black); Line2D.Double tick = new Line2D.Double(x +
      * xoffset, rulerHeight - 2, x + xoffset, rulerHeight + 2); g.draw(tick);
      * StructureDrawPanel.drawStringCentred(g, x + xoffset, rulerHeight / 2, i +
      * ""); } }
      *
      *
      * for (int i = 0; i < genomeOrganization.genome.size(); i++) { // draw
      * blocks Region region = genomeOrganization.genome.get(i); double
      * regionLength = region.end - region.start; double regionWidth =
      * (regionLength / (double) layerPanel.genomeLength) * getWidth(); double x
      * = (region.start / (double) layerPanel.genomeLength) * getWidth();
      * g.setColor(region.color); Rectangle2D rect = new Rectangle2D.Double(x +
      * xoffset, rulerHeight + region.level * blockHeight, regionWidth,
      * blockHeight); g.fill(rect);
      *
      * // draw gene names g.setColor(Color.black);
      * StructureDrawPanel.drawStringCentred(g, x + xoffset + regionWidth / 2,
      * rulerHeight + region.level * blockHeight + blockHeight / 2, region.name);
      * } }
      *
      * g2.drawImage(bufferedImage, 0, 0, this);
      *
      *
      * if (mouseoverStart != -1 || mouseoverEnd != -1) { double mouseoverLength
      * = mouseoverEnd - mouseoverStart; double regionWidth = (mouseoverLength /
      * (double) layerPanel.genomeLength) * getWidth(); double x =
      * (mouseoverStart / (double) layerPanel.genomeLength) * getWidth();
      * g2.setColor(Color.BLACK); Rectangle2D rect = new Rectangle2D.Double(x +
      * xoffset, rulerHeight + 0, regionWidth, getHeight() - rulerHeight - 1);
      * g2.draw(rect); }
      *
      * if (selectedStart != -1 || selectedEnd != -1) { double mouseoverLength =
      * selectedEnd - selectedStart; double regionWidth = (mouseoverLength /
      * (double) layerPanel.genomeLength) * getWidth(); double x = (selectedStart
      * / (double) layerPanel.genomeLength) * getWidth(); g2.setColor(Color.RED);
      * Rectangle2D rect = new Rectangle2D.Double(x + xoffset, rulerHeight + 0,
      * regionWidth, getHeight() - rulerHeight - 1); g2.draw(rect); }
      *
      * }
      * }
      */
     @Override
     public void paintComponent(Graphics graphics) {
         super.paintComponent(graphics);
         Graphics2D g2 = (Graphics2D) graphics;
         g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
 
         int panelWidth = this.getWidth();
         int panelHeight = this.getHeight();
 
 
         if (genomeOrganization != null) {
             /*
              * if (bufferedImage == null || bufferedImage.getWidth() !=
              * panelWidth || bufferedImage.getHeight() != panelHeight) {
              * bufferedImage = (BufferedImage) (this.createImage(panelWidth,
              * panelHeight)); g = (Graphics2D) bufferedImage.getGraphics();
              * g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
              * RenderingHints.VALUE_ANTIALIAS_ON); forceRepaint = true; }
              */
 
             //if (forceRepaint) {
             // forceRepaint = false;
 
             g2.setColor(Color.white);
             g2.fillRect(0, 0, panelWidth, panelHeight);
 
             minorTickMark = chooseBestTickMarkSize(layerPanel.genomeLength);
             majorTickMark = minorTickMark * 2;
 
             // draw tick marks
             g2.setFont(layerPanel.f2);
             for (int i = 0; i < layerPanel.genomeLength; i++) {
                 if (i % majorTickMark == 0) {
                     double x = ((double) i / (double) layerPanel.genomeLength) * getWidth();
                     g2.setColor(Color.black);
                     Line2D.Double tick = new Line2D.Double(x + xoffset, rulerHeight - 1, x + xoffset, rulerHeight + 1);
                     g2.draw(tick);
                     StructureDrawPanel.drawStringCentred(g2, x + xoffset, rulerHeight / 2, i + "");
                 } else if (i % minorTickMark == 0) {
                     double x = ((double) i / (double) layerPanel.genomeLength) * getWidth();
                     g2.setColor(Color.black);
                     Line2D.Double tick = new Line2D.Double(x + xoffset, rulerHeight - 1, x + xoffset, rulerHeight + 1);
                     g2.draw(tick);
                 }
             }
 
 
             for (int i = 0; i < genomeOrganization.genome.size(); i++) {
                 // draw blocks
                 Region region = genomeOrganization.genome.get(i);
                 double regionLength = region.end - region.start;
                 double regionWidth = (regionLength / (double) layerPanel.genomeLength) * getWidth();
                 double x = ((double) region.start / (double) layerPanel.genomeLength) * getWidth();
                 g2.setColor(region.color);
                 Rectangle2D rect = new Rectangle2D.Double(x + xoffset, rulerHeight + region.level * blockHeight, regionWidth, blockHeight);
                 g2.fill(rect);
 
                 // draw gene names
                 g2.setColor(Color.black);
                 StructureDrawPanel.drawStringCentred(g2, x + xoffset + regionWidth / 2, rulerHeight + region.level * blockHeight + blockHeight / 2, region.name);
             }
         }
 
         g2.drawImage(bufferedImage, 0, 0, this);
 
         if (structures != null && structurePositions != null) {
 
 
            /* StructureAndMouseoverRegion selectedRect = null;
             if (structurePositions.size() > 0) {
                 selectedRect = structurePositions.get(0);
             }*/
             for (int i = 0; i < structurePositions.size(); i++) {
 
                 Color blockColor = new Color(150, 150, 150, 100);
                 if (structurePositions.get(i).structure.equals(selected)) {
                     blockColor = new Color(255, 10, 10, 100);
                     //selectedRect = structurePositions.get(i);
                 }
                 g2.setColor(blockColor);
                 g2.fill(structurePositions.get(i).rectangle);
                 
                 g2.setColor(Color.GRAY);
                 
             }
             
             /*if(selectedRect != null)
             {
                 double fontHeight = g2.getFontMetrics().getHeight();
                 g2.drawString(selected.index + "", (float) (structurePositions.get(0).rectangle.getX() - 14), (float) (selectedRect.rectangle.getY() + ((selectedRect.rectangle.getHeight() + fontHeight) / 2)));
             }*/
             /*
              * for (int i = 0; i < structures.size(); i++) { Color blockColor =
              * new Color(150, 150, 150, 100); if
              * (structures.get(i).equals(selected)) { blockColor = new
              * Color(255, 10, 10, 100); } double h = (getHeight() - rulerHeight
              * - 1) / (double) (structures.size()); double y = rulerHeight + i *
              * h; double mouseoverLength = (double)
              * structures.get(i).getEndPosition() - (double)
              * structures.get(i).startPosition; double regionWidth =
              * (mouseoverLength / (double) layerPanel.genomeLength) *
              * getWidth(); double x = ((double) structures.get(i).startPosition
              * / (double) layerPanel.genomeLength) * getWidth();
              * g2.setColor(blockColor); Rectangle2D rect = new
              * Rectangle2D.Double(x + xoffset, y, regionWidth, h);
              * g2.fill(rect);
              *
              * // wrap around if (layerPanel.genomeLength < (double)
              * structures.get(i).getEndPosition()) { mouseoverLength = (double)
              * structures.get(i).getEndPosition() - layerPanel.genomeLength;
              * regionWidth = (mouseoverLength / (double)
              * layerPanel.genomeLength) * getWidth(); x = 0;
              * g2.setColor(blockColor); rect = new Rectangle2D.Double(x +
              * xoffset, y, regionWidth, h); g2.fill(rect); } }
              */
         }
 
 
         double rulerHeight = 0;
         if (mouseoverStart != -1 || mouseoverEnd != -1) {
             double mouseoverLength = mouseoverEnd - mouseoverStart;
             double regionWidth = (mouseoverLength / (double) layerPanel.genomeLength) * getWidth();
             double x = (mouseoverStart / (double) layerPanel.genomeLength) * getWidth();
             g2.setColor(new Color(0, 0, 0, 125));
             Rectangle2D rect = new Rectangle2D.Double(x + xoffset, rulerHeight + 0, regionWidth, getHeight() - rulerHeight - 1);
             g2.draw(rect);
 
             // wrap around
             if (layerPanel.genomeLength < mouseoverEnd) {
                 mouseoverLength = mouseoverEnd - layerPanel.genomeLength;
                 regionWidth = (mouseoverLength / (double) layerPanel.genomeLength) * getWidth();
                 x = 0;
                 g2.setColor(new Color(125, 125, 125, 125));
                 rect = new Rectangle2D.Double(x + xoffset, rulerHeight + 0, regionWidth, getHeight() - rulerHeight - 1);
                 g2.draw(rect);
             }
         }
         //rulerHeight = this.rulerHeight;
 
         if (selectedStart != -1 || selectedEnd != -1) {
             double mouseoverLength = selectedEnd - selectedStart;
             double regionWidth = (mouseoverLength / (double) layerPanel.genomeLength) * getWidth();
             double x = (selectedStart / (double) layerPanel.genomeLength) * getWidth();
             g2.setColor(Color.RED);
             Rectangle2D rect = new Rectangle2D.Double(x + xoffset, rulerHeight + 0, regionWidth, getHeight() - rulerHeight - 1);
             g2.draw(rect);
 
             // wrap around
             if (layerPanel.genomeLength < selectedEnd) {
                 mouseoverLength = selectedEnd - layerPanel.genomeLength;
                 regionWidth = (mouseoverLength / (double) layerPanel.genomeLength) * getWidth();
                 x = 0;
                 g2.setColor(Color.RED);
                 rect = new Rectangle2D.Double(x + xoffset, rulerHeight + 0, regionWidth, getHeight() - rulerHeight - 1);
                 g2.draw(rect);
             }
         }
 
         // }
     }
 
     public void drawMouseOverSelection(int start, int end) {
         mouseoverStart = start;
         mouseoverEnd = end;
         repaint();
     }
 
     public void drawSelected(int start, int end) {
         selectedStart = start;
         selectedStart = end;
         repaint();
     }
 
     public void mouseClicked(MouseEvent e) {
         if (this.isEnabled()) {
             if (selected != null) {
                 layerPanel.mainapp.openStructure(selected);
             } else {
                 int x = e.getX();
                 if (x >= 0 && x < getWidth()) {
                     int position = (int) (((double) x / (double) getWidth()) * layerPanel.genomeLength);
                     int structureIndex = layerPanel.mainapp.getStructureIndexAtPosition(position);
                     if (structureIndex != -1) {
                         //System.o
                         layerPanel.mainapp.openStructure(structureIndex);
                     }
                 } else {
                 }
             }
         }
     }
 
     public void selectStructureAtPosition(int position) {
         if (layerPanel.mainapp != null) {
             Structure s = layerPanel.mainapp.getStructureAtPosition(position);
             selectStructure(s);
         }
     }
 
     public void selectStructure(Structure s) {
         if (s == null) {
             selectedStart = -1;
             selectedEnd = -1;
         } else {
             selectedStart = s.getStartPosition();
             selectedEnd = s.getEndPosition();
         }
         repaint();
     }
 
     public void mousePressed(MouseEvent e) {
         // throw new UnsupportedOperationException("Not supported yet.");
     }
 
     public void mouseReleased(MouseEvent e) {
         if (SwingUtilities.isRightMouseButton(e)) {
             this.popupMenu.show(this, e.getX(), e.getY());
         }
     }
 
     public void mouseEntered(MouseEvent e) {
         //throw new UnsupportedOperationException("Not supported yet.");
     }
 
     public void mouseExited(MouseEvent e) {
         structures = null;
         mouseoverStart = -1;
         mouseoverEnd = -1;
         repaint();
     }
 
     public void mouseDragged(MouseEvent e) {
         //throw new UnsupportedOperationException("Not supported yet.");
     }
 
     public void mouseMoved(MouseEvent e) {
         if (this.isEnabled()) {
             int x = e.getX();
             int y = e.getY();
             if (x >= 0 && x < getWidth()) {
                 int position = (int) (((double) x / (double) getWidth()) * layerPanel.genomeLength);
                 if (layerPanel.mainapp != null) {
                     Structure s = layerPanel.mainapp.getStructureAtPosition(position);
                     Structure largest = layerPanel.mainapp.getLargestStructureAtPosition(position, 500);
                     if (largest != null) {
                         selected = null;
                         structures = layerPanel.mainapp.getStructuresInRegion(largest.startPosition, largest.getEndPosition());
                         structurePositions = getStructurePositions(structures);
                         for (int i = 0; i < structurePositions.size(); i++) {
                             if (structurePositions.get(i).rectangle.contains(x, y)) {
                                 selected = structurePositions.get(i).structure;
                             }
                         }
                         if (selected == null) {
                             selected = s;
                         }
                     } else {
                         structures = null;
                     }
                     if (selected == null) {
                         mouseoverStart = -1;
                         mouseoverEnd = -1;
                     } else {
                         mouseoverStart = selected.getStartPosition();
                         mouseoverEnd = selected.getEndPosition();
                     }
                     repaint();
                 }
             } else {
             }
         }
     }
 
     public void actionPerformed(ActionEvent e) {
         if (e.getSource().equals(this.autofitItem)) {
             layerPanel.autofitWidth();
         } else if (e.getSource().equals(this.zoomInItem)) {
             layerPanel.zoomIn();
         } else if (e.getSource().equals(this.zoomOutItem)) {
             layerPanel.zoomOut();
         }
     }
 
     public ArrayList<StructureAndMouseoverRegion> getStructurePositions(ArrayList<Structure> structures) {
         double rulerHeight = 0;
         ArrayList<StructureAndMouseoverRegion> rectangles = new ArrayList<StructureAndMouseoverRegion>();
         double minDistance = 3;
         int level = 0;
         for (int i = 0; i < structures.size(); i++) {
             double h = (getHeight() - rulerHeight - 1) / (double) (structures.size());
             double y = rulerHeight + i * h;
             double mouseoverLength = (double) structures.get(i).getEndPosition() - (double) structures.get(i).startPosition;
             double regionWidth = (mouseoverLength / (double) layerPanel.genomeLength) * getWidth();
             double x = ((double) structures.get(i).startPosition / (double) layerPanel.genomeLength) * getWidth();
             Rectangle2D rect = new Rectangle2D.Double(x + xoffset, y, regionWidth, h);
 
             int rectLevel = 0;
             for (rectLevel = 0; rectLevel <= level + 1; rectLevel++) {
                if (minHorizontalDistance(rectangles, rect, rectLevel) < minDistance) {
                 } else {
                     break;
                 }
             }
             level = Math.max(level, rectLevel);
             rect.setRect(x + xoffset, rulerHeight + rectLevel * h, regionWidth, h);
             rectangles.add(new StructureAndMouseoverRegion(structures.get(i), rect, rectLevel));
 
             // wrap around
             if (layerPanel.genomeLength < (double) structures.get(i).getEndPosition()) {
                 Rectangle2D rect2 = new Rectangle2D.Double(x + xoffset, y, regionWidth, h);
                 rectLevel = 0;
                 for (rectLevel = 0; rectLevel <= level + 1; rectLevel++) {
                     if (minHorizontalDistance(rectangles, rect2, rectLevel) < minDistance) {
                     } else {
                         break;
                     }
                 }
                 level = Math.max(level, rectLevel);
                 rect2.setRect(x + xoffset, rulerHeight + rectLevel * h, regionWidth, h);
                 rectangles.add(new StructureAndMouseoverRegion(structures.get(i), rect2, rectLevel));
            }
         }
 
         for (int i = 0; i < rectangles.size(); i++) {
             Rectangle2D r = rectangles.get(i).rectangle;
             double h = (getHeight() - rulerHeight - 1) / (double) (level + 1);
             double y = rulerHeight + rectangles.get(i).level * h;
             rectangles.get(i).rectangle.setRect(r.getX(), y, r.getWidth(), h);
         }
 
         return rectangles;
     }
 
     public double minHorizontalDistance(ArrayList<StructureAndMouseoverRegion> rectangles, Rectangle2D rect, int level) {
         double x = rect.getX();
         double width = rect.getWidth();
         double distance = Double.MAX_VALUE;
         for (int i = 0; i < rectangles.size(); i++) {
            if (rectangles.get(i).level == level) {
                double dist1 = rectangles.get(i).rectangle.getX() - (x + width);
                 if (dist1 >= 0) {
                     distance = Math.min(distance, dist1);
                 }
                double dist2 = x - (rectangles.get(i).rectangle.getX() + rectangles.get(i).rectangle.getWidth());
                 if (dist2 >= 0) {
                     distance = Math.min(distance, dist2);
                 }
                if (x >= rectangles.get(i).rectangle.getX() && x + width <= rectangles.get(i).rectangle.getX() + rectangles.get(i).rectangle.getWidth()) {
                     distance = 0;
                 }
             }
         }
         return distance;
     }
 
     public class StructureAndMouseoverRegion {
 
         Structure structure;
         Rectangle2D rectangle;
         int level;
 
         public StructureAndMouseoverRegion(Structure structure, Rectangle2D rectangle, int level) {
             this.structure = structure;
             this.rectangle = rectangle;
             this.level = level;
         }
     }
 }
