 package com.imjake9.snes.tile.gui;
 
 import com.imjake9.snes.tile.SNESTile;
 import com.imjake9.snes.tile.data.DataConverter;
 import com.imjake9.snes.tile.data.SNESImage;
 import com.imjake9.snes.tile.utils.GuiUtils;
 import com.imjake9.snes.tile.utils.Pair;
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.awt.event.MouseWheelEvent;
 import java.awt.event.MouseWheelListener;
 import java.awt.image.BufferedImage;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.Map;
 import java.util.Map.Entry;
 import javax.swing.JPanel;
 import javax.swing.JViewport;
 import javax.swing.Scrollable;
 import javax.swing.SwingConstants;
 import javax.swing.event.UndoableEditEvent;
 import javax.swing.undo.AbstractUndoableEdit;
 import javax.swing.undo.CannotRedoException;
 import javax.swing.undo.CannotUndoException;
 
 
 public class DrawingPanel extends JPanel implements MouseListener, MouseMotionListener, MouseWheelListener, Scrollable {
     
     private SNESImage image;
     private Rectangle marqueeRect;
     private BufferedImage marqueeLayer;
     private PalettePanel palette;
     private int scalingFactor = 2;
     private Tool currentTool = Tool.PENCIL;
     private boolean gridEnabled;
     
     public DrawingPanel() {
         this.setBackground(Color.BLACK);
         this.addMouseListener(this);
         this.addMouseMotionListener(this);
         this.addMouseWheelListener(this);
     }
     
     public void setPalette(PalettePanel palette) {
         this.palette = palette;
     }
     
     public void setData(byte[] data) {
         image = new SNESImage(DataConverter.fromSNES4BPP(data));
         repaintAll();
     }
     
     public byte[] getData() {
         return DataConverter.toSNES4BPP(image.getData());
     }
     
     public int getScalingFactor() {
         return scalingFactor;
     }
     
     public void setCurrentTool(Tool tool) {
         currentTool = tool;
     }
     
     public void setCurrentTool(String tool) {
         currentTool = Tool.valueOf(tool);
     }
     
     public Tool getCurrentTool() {
         return currentTool;
     }
     
     public void setGridEnabled(boolean gridEnabled) {
         this.gridEnabled = gridEnabled;
         repaint();
     }
     
     public boolean getGridEnabled() {
         return gridEnabled;
     }
     
     public void setPixelColor(Point location, byte index) {
         Color color = palette.getPaletteSet().getSelectedPalette().getColor(index);
         image.setRGB(location.x, location.y, color.getRGB());
         repaint();
     }
     
     private byte getPixelColor(Point location) {
         try {
             return image.getIndexForColor(new Color(image.getImage().getRGB(location.x, location.y)));
         } catch (ArrayIndexOutOfBoundsException ex) {
             return 0;
         }
     }
     
     public Rectangle getClippingMask() {
         return marqueeRect != null ? marqueeRect : new Rectangle(new Point(), image.getSize());
     }
     
     @Override
     public void paintComponent(Graphics g) {
         g.setColor(Color.BLACK);
         g.fillRect(0, 0, this.getWidth(), this.getHeight());
         
         BufferedImage result;
         if (palette == null || image == null || (result = image.getImage()) == null) {
             return;
         }
         
         g.drawImage(result, 0, 0, result.getWidth() * scalingFactor, result.getHeight() * scalingFactor, null);
         
         if (gridEnabled) {
             int interval = scalingFactor > 8 ? 4
                          : scalingFactor > 4 ? 8
                                              : 16;
             for (int i = 0; i < result.getWidth() * scalingFactor; i += interval * scalingFactor) {
                 if (i % (16 * scalingFactor) == 0) {
                     g.setColor(new Color(0xFFFFFFFF, true));
                 } else {
                     g.setColor(new Color(0x7FFFFFFF, true));
                 }
                 g.drawLine(i, 0, i, result.getHeight() * scalingFactor);
             }
             for (int i = 0; i < result.getHeight() * scalingFactor; i += interval * scalingFactor) {
                 if (i % (16 * scalingFactor) == 0) {
                     g.setColor(new Color(0xFFFFFFFF, true));
                 } else {
                     g.setColor(new Color(0x7FFFFFFF, true));
                 }
                 g.drawLine(0, i, result.getWidth() * scalingFactor, i);
             }
         }
         
         if (marqueeRect != null && marqueeRect.height > 0 && marqueeRect.width > 0) {
             marqueeLayer = new BufferedImage(marqueeRect.width * scalingFactor, marqueeRect.height * scalingFactor, BufferedImage.TYPE_INT_ARGB);
             Graphics2D gm = marqueeLayer.createGraphics();
             gm.setColor(Color.BLACK);
             gm.setStroke(new BasicStroke(1));
             gm.drawRect(0, 0, marqueeLayer.getWidth() - 1, marqueeLayer.getHeight() - 1);
             gm.setStroke(new BasicStroke(1, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10, new float[] {2, 4}, 0));
             gm.setColor(Color.WHITE);
             gm.drawRect(0, 0, marqueeLayer.getWidth() - 1, marqueeLayer.getHeight() - 1);
             gm.dispose();
             g.drawImage(marqueeLayer, marqueeRect.x * scalingFactor, marqueeRect.y * scalingFactor, null);
         }
     }
     
     public void repaintAll() {
         Dimension size = recalculatePreferredSize();
         image.setPalette(palette.getPaletteSet().getSelectedPalette());
         repaint();
     }
     
     private Dimension recalculatePreferredSize() {
         Dimension size = new Dimension(8 * 16 * scalingFactor, (image.getData().length/(8 * 16) + 7) / 8 * 8 * scalingFactor);
         setPreferredSize(size);
         JViewport viewport = (JViewport) getParent();
         viewport.doLayout();
         return size;
     }
     
     public void incrementScalingFactor() {
         scalingFactor *= 2;
         boolean rescale = true;
         Point initialPoint = ((JViewport) getParent()).getViewPosition();
         if (scalingFactor > 32) {
             rescale = false;
             scalingFactor = 32;
         }
         repaintAll();
         if (rescale) {
             ((JViewport) getParent()).setViewPosition(new Point(initialPoint.x, initialPoint.y * 2));
         }
     }
     
     public void decrementScalingFactor() {
         scalingFactor /= 2;
         boolean rescale = true;
         Point initialPoint = ((JViewport) getParent()).getViewPosition();
         if (scalingFactor < 1) {
             rescale = false;
             scalingFactor = 1;
         }
         repaintAll();
         if (rescale) {
             ((JViewport) getParent()).setViewPosition(new Point(initialPoint.x, initialPoint.y / 2));
         }
     }
     
     private Point getSelectedPixel(MouseEvent me) {
         return new Point(me.getX()/scalingFactor, me.getY()/scalingFactor);
     }
     
     private Point previousLocation;
     
     @Override
     public void mouseClicked(MouseEvent me) {
         if (GuiUtils.isLeftClick(me)) {
             currentTool.mouseClicked(getSelectedPixel(me));
         } else if (GuiUtils.isRightClick(me)) {
             palette.getPaletteSet().setSelectedColor(getPixelColor(getSelectedPixel(me)));
             palette.repaint();
         }
     }
 
     @Override
     public void mousePressed(MouseEvent me) {
         if (GuiUtils.isLeftClick(me)) {
             currentTool.mouseDown(getSelectedPixel(me));
             currentTool.mouseDragged(getSelectedPixel(me));
         } else if (GuiUtils.isMiddleClick(me)) {
             previousLocation = me.getLocationOnScreen();
         }
     }
 
     @Override
     public void mouseReleased(MouseEvent me) {
         if (GuiUtils.isLeftClick(me)) {
             currentTool.mouseUp(getSelectedPixel(me));
         }
     }
 
     @Override
     public void mouseEntered(MouseEvent me) {}
 
     @Override
     public void mouseExited(MouseEvent me) {}
     
     @Override
     public void mouseDragged(MouseEvent me) {
         if (GuiUtils.isLeftClick(me)) {
             currentTool.mouseDragged(getSelectedPixel(me));
         } else if (GuiUtils.isMiddleClick(me) && previousLocation != null) {
             JViewport viewport = (JViewport) getParent();
             Point newLocation = me.getLocationOnScreen();
             Rectangle viewRect = viewport.getViewRect();
             viewRect.x += previousLocation.x - newLocation.x;
             viewRect.y += previousLocation.y - newLocation.y;
             scrollRectToVisible(viewRect);
             previousLocation = newLocation;
         }
     }
     
     @Override
     public void mouseMoved(MouseEvent me) {}
     
     @Override
     public void mouseWheelMoved(MouseWheelEvent mwe) {
         if (GuiUtils.isMenuShortcutKeyDown(mwe)) {
             if (mwe.getWheelRotation() < 0) {
                 incrementScalingFactor();
             } else {
                 decrementScalingFactor();
             }
         } else {
             getParent().getParent().dispatchEvent(mwe);
         }
     }
     
     @Override
     public Dimension getPreferredScrollableViewportSize() {
         return getPreferredSize();
     }
 
     @Override
     public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
         return 16;
     }
 
     @Override
     public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
         return orientation == SwingConstants.VERTICAL ? visibleRect.height : visibleRect.width;
     }
 
     @Override
     public boolean getScrollableTracksViewportWidth() {
         return false;
     }
 
     @Override
     public boolean getScrollableTracksViewportHeight() {
         return false;
     }
     
     public static enum Tool {
         MARQUEE("Marquee") {
             private Rectangle oldMarqueeRect;
             @Override
             public void mouseDown(Point location) {
                 DrawingPanel panel = SNESTile.getInstance().getDrawingPanel();
                 oldMarqueeRect = panel.marqueeRect;
                 panel.marqueeRect = new Rectangle(location);
                 panel.repaint();
             }
             @Override
             public void mouseDragged(Point location) {
                 DrawingPanel panel = SNESTile.getInstance().getDrawingPanel();
                 panel.marqueeRect = getDrawableRect(panel.marqueeRect.getLocation(), location);
                 panel.repaint();
             }
             @Override
             public void mouseUp(Point location) {
                 DrawingPanel panel = SNESTile.getInstance().getDrawingPanel();
                 if (panel.marqueeRect.width > 0 && panel.marqueeRect.height > 0) {
                     SNESTile.getInstance().addUndoableEdit(new UndoableEditEvent(this, new SelectAction(oldMarqueeRect, panel.marqueeRect)));
                 }
             }
             @Override
             public void mouseClicked(Point location) {
                 DrawingPanel panel = SNESTile.getInstance().getDrawingPanel();
                SNESTile.getInstance().addUndoableEdit(new UndoableEditEvent(this, new SelectAction(panel.marqueeRect, null, "Select None")));
                 panel.marqueeRect = null;
                 panel.repaint();
             }
         },
         PENCIL("Pencil") {
             private Map<Point, Pair<Byte, Byte>> actionMap;
             @Override
             public void mouseDown(Point location) {
                 actionMap = new HashMap<Point, Pair<Byte, Byte>>();
             }
             @Override
             public void mouseDragged(Point location) {
                 SNESTile window = SNESTile.getInstance();
                 if (window.getDrawingPanel().getClippingMask().contains(location)) {
                     if (!actionMap.containsKey(location))
                         actionMap.put(location, new Pair<Byte, Byte>(window.getDrawingPanel().getPixelColor(location), window.getPalettePanel().getPaletteSet().getSelectedColorIndex()));
                     window.getDrawingPanel().setPixelColor(location, window.getPalettePanel().getPaletteSet().getSelectedColorIndex());
                 }
             }
             @Override
             public void mouseUp(Point location) {
                 SNESTile.getInstance().addUndoableEdit(new UndoableEditEvent(this, new DrawAction(actionMap, this)));
                 actionMap = null;
             }
         },
         LINE("Line") {
             private Point lineStart;
             @Override
             public void mouseDown(Point location) {
                 lineStart = location;
             }
             @Override
             public void mouseDragged(Point location) {
                 DrawingPanel panel = SNESTile.getInstance().getDrawingPanel();
                 PalettePanel palette = SNESTile.getInstance().getPalettePanel();
                 Graphics2D g = panel.image.createGraphics(true);
                 g.setColor(palette.getPaletteSet().getSelectedColor());
                 g.drawLine(lineStart.x, lineStart.y, location.x, location.y);
                 g.dispose();
                 panel.repaint();
             }
             @Override
             public void mouseUp(Point location) {
                 DrawingPanel panel = SNESTile.getInstance().getDrawingPanel();
                 PalettePanel palette = SNESTile.getInstance().getPalettePanel();
                 Graphics2D g = panel.image.createGraphics(true);
                 g.setClip(panel.getClippingMask());
                 g.setColor(palette.getPaletteSet().getSelectedColor());
                 g.drawLine(lineStart.x, lineStart.y, location.x, location.y);
                 Map<Point, Pair<Byte, Byte>> actionMap = panel.image.commitChanges();
                 panel.repaint();
                 SNESTile.getInstance().addUndoableEdit(new UndoableEditEvent(this, new DrawAction(actionMap, this)));
             }
         },
         FILL_RECT("Fill Rectangle") {
             private Point rectStart;
             @Override
             public void mouseDown(Point location) {
                 rectStart = location;
             }
             @Override
             public void mouseDragged(Point location) {
                 DrawingPanel panel = SNESTile.getInstance().getDrawingPanel();
                 PalettePanel palette = SNESTile.getInstance().getPalettePanel();
                 Graphics2D g = panel.image.createGraphics(true);
                 Rectangle rect = getDrawableRect(rectStart, location);
                 g.setColor(palette.getPaletteSet().getSelectedColor());
                 g.fillRect(rect.x, rect.y, rect.width + 1, rect.height + 1);
                 g.dispose();
                 panel.repaint();
             }
             @Override
             public void mouseUp(Point location) {
                 DrawingPanel panel = SNESTile.getInstance().getDrawingPanel();
                 PalettePanel palette = SNESTile.getInstance().getPalettePanel();
                 Rectangle rect = getDrawableRect(rectStart, location);
                 Graphics2D g = panel.image.createGraphics(true);
                 g.setClip(panel.getClippingMask());
                 g.setColor(palette.getPaletteSet().getSelectedColor());
                 g.fillRect(rect.x, rect.y, rect.width + 1, rect.height + 1);
                 Map<Point, Pair<Byte, Byte>> actionMap = panel.image.commitChanges();
                 panel.repaint();
                 SNESTile.getInstance().addUndoableEdit(new UndoableEditEvent(this, new DrawAction(actionMap, this)));
             }
         },
         STROKE_RECT("Stroke Rectangle") {
             private Point rectStart;
             @Override
             public void mouseDown(Point location) {
                 rectStart = location;
             }
             @Override
             public void mouseDragged(Point location) {
                 DrawingPanel panel = SNESTile.getInstance().getDrawingPanel();
                 PalettePanel palette = SNESTile.getInstance().getPalettePanel();
                 Graphics2D g = panel.image.createGraphics(true);
                 Rectangle rect = getDrawableRect(rectStart, location);
                 g.setColor(palette.getPaletteSet().getSelectedColor());
                 g.drawRect(rect.x, rect.y, rect.width, rect.height);
                 g.dispose();
                 panel.repaint();
             }
             @Override
             public void mouseUp(Point location) {
                 DrawingPanel panel = SNESTile.getInstance().getDrawingPanel();
                 PalettePanel palette = SNESTile.getInstance().getPalettePanel();
                 Rectangle rect = getDrawableRect(rectStart, location);
                 Graphics2D g = panel.image.createGraphics(true);
                 g.setClip(panel.getClippingMask());
                 g.setColor(palette.getPaletteSet().getSelectedColor());
                 g.drawRect(rect.x, rect.y, rect.width, rect.height);
                 Map<Point, Pair<Byte, Byte>> actionMap = panel.image.commitChanges();
                 panel.repaint();
                 SNESTile.getInstance().addUndoableEdit(new UndoableEditEvent(this, new DrawAction(actionMap, this)));
             }
         },
         FILL_ELLIPSE("Fill Ellipse") {
             private Point ellipseStart;
             @Override
             public void mouseDown(Point location) {
                 ellipseStart = new Point(location.x - 1, location.y - 1);
             }
             @Override
             public void mouseDragged(Point location) {
                 DrawingPanel panel = SNESTile.getInstance().getDrawingPanel();
                 PalettePanel palette = SNESTile.getInstance().getPalettePanel();
                 Graphics2D g = panel.image.createGraphics(true);
                 Rectangle rect = getDrawableRect(ellipseStart, location);
                 g.setColor(palette.getPaletteSet().getSelectedColor());
                 g.fillOval(rect.x, rect.y, rect.width + 1, rect.height + 1);
                 g.dispose();
                 panel.repaint();
             }
             @Override
             public void mouseUp(Point location) {
                 DrawingPanel panel = SNESTile.getInstance().getDrawingPanel();
                 PalettePanel palette = SNESTile.getInstance().getPalettePanel();
                 Rectangle rect = getDrawableRect(ellipseStart, location);
                 Graphics2D g = panel.image.createGraphics(true);
                 g.setClip(panel.getClippingMask());
                 g.setColor(palette.getPaletteSet().getSelectedColor());
                 g.fillOval(rect.x, rect.y, rect.width + 1, rect.height + 1);
                 Map<Point, Pair<Byte, Byte>> actionMap = panel.image.commitChanges();
                 panel.repaint();
                 SNESTile.getInstance().addUndoableEdit(new UndoableEditEvent(this, new DrawAction(actionMap, this)));
             }
         },
         STROKE_ELLIPSE("Stroke Ellipse") {
             private Point ellipseStart;
             @Override
             public void mouseDown(Point location) {
                 ellipseStart = new Point(location.x, location.y);
             }
             @Override
             public void mouseDragged(Point location) {
                 DrawingPanel panel = SNESTile.getInstance().getDrawingPanel();
                 PalettePanel palette = SNESTile.getInstance().getPalettePanel();
                 Graphics2D g = panel.image.createGraphics(true);
                 Rectangle rect = getDrawableRect(ellipseStart, location);
                 g.setColor(palette.getPaletteSet().getSelectedColor());
                 g.drawOval(rect.x, rect.y, rect.width, rect.height);
                 g.dispose();
                 panel.repaint();
             }
             @Override
             public void mouseUp(Point location) {
                 DrawingPanel panel = SNESTile.getInstance().getDrawingPanel();
                 PalettePanel palette = SNESTile.getInstance().getPalettePanel();
                 Rectangle rect = getDrawableRect(ellipseStart, location);
                 Graphics2D g = panel.image.createGraphics(true);
                 g.setClip(panel.getClippingMask());
                 g.setColor(palette.getPaletteSet().getSelectedColor());
                 g.drawOval(rect.x, rect.y, rect.width, rect.height);
                 Map<Point, Pair<Byte, Byte>> actionMap = panel.image.commitChanges();
                 panel.repaint();
                 SNESTile.getInstance().addUndoableEdit(new UndoableEditEvent(this, new DrawAction(actionMap, this)));
             }
         },
         FILL("Fill") {
             @Override
             public void mouseClicked(Point location) {
                 DrawingPanel panel = SNESTile.getInstance().getDrawingPanel();
                 PalettePanel palette = SNESTile.getInstance().getPalettePanel();
                 byte target = panel.getPixelColor(location);
                 byte replacement = palette.getPaletteSet().getSelectedColorIndex();
                 LinkedList<Point> openPixels = new LinkedList<Point>();
                 Map<Point, Pair<Byte, Byte>> actionMap = new HashMap<Point, Pair<Byte, Byte>>();
                 openPixels.addFirst(location);
                 Point node;
                 while ((node = openPixels.poll()) != null) {
                     if (panel.getPixelColor(node) == target && panel.getClippingMask().contains(node)) {
                         panel.setPixelColor(node, replacement);
                         if (!actionMap.containsKey(node))
                             actionMap.put(node, new Pair<Byte, Byte>(target, replacement));
                         openPixels.addFirst(new Point(node.x - 1, node.y));
                         openPixels.addFirst(new Point(node.x + 1, node.y));
                         openPixels.addFirst(new Point(node.x, node.y - 1));
                         openPixels.addFirst(new Point(node.x, node.y + 1));
                     }
                 }
                 SNESTile.getInstance().addUndoableEdit(new UndoableEditEvent(this, new DrawAction(actionMap, this)));
             }
         };
         
         private static Rectangle getDrawableRect(Point a, Point b) {
             return new Rectangle(Math.min(a.x, b.x), Math.min(a.y, b.y), Math.abs(a.x - b.x), Math.abs(a.y - b.y));
         }
         
         private final String displayName;
         
         Tool(String displayName) {
             this.displayName = displayName;
         }
         
         public String getDisplayName() {
             return displayName;
         }
         
         public void mouseClicked(Point location) {}
         public void mouseDown(Point location) {}
         public void mouseUp(Point location) {}
         public void mouseDragged(Point location) {}
     }
     
     public static class DrawAction extends AbstractUndoableEdit {
         
         protected final Map<Point, Pair<Byte, Byte>> modifiedPixels;
         private final Tool tool;
         
         public DrawAction(Map<Point, Pair<Byte, Byte>> modifiedPixels) {
             this(modifiedPixels, null);
         }
         
         public DrawAction(Map<Point, Pair<Byte, Byte>> modifiedPixels, Tool tool) {
             this.modifiedPixels = modifiedPixels;
             this.tool = tool;
         }
         
         @Override
         public void undo() {
             super.undo();
             DrawingPanel panel = SNESTile.getInstance().getDrawingPanel();
             for (Entry<Point, Pair<Byte, Byte>> entry : modifiedPixels.entrySet()) {
                 panel.setPixelColor(entry.getKey(), entry.getValue().getLeft());
             }
             panel.repaint();
         }
         
         @Override
         public void redo() {
             super.redo();
             DrawingPanel panel = SNESTile.getInstance().getDrawingPanel();
             for (Entry<Point, Pair<Byte, Byte>> entry : modifiedPixels.entrySet()) {
                 panel.setPixelColor(entry.getKey(), entry.getValue().getRight());
             }
             panel.repaint();
         }
         
         @Override
         public String getPresentationName() {
             return tool == null ? "Draw" : tool.getDisplayName();
         }
         
     }
     
     public static class SelectAction extends AbstractUndoableEdit {
         
         protected final Rectangle oldSelection;
         protected final Rectangle newSelection;
         private final String name;
         
         public SelectAction(Rectangle oldSelection, Rectangle newSelection) {
             this(oldSelection, newSelection, "Select");
         }
         
         public SelectAction(Rectangle oldSelection, Rectangle newSelection, String name) {
             this.oldSelection = oldSelection;
             this.newSelection = newSelection;
             this.name = name;
         }
         
         @Override
         public void undo() throws CannotUndoException {
             super.undo();
             DrawingPanel panel = SNESTile.getInstance().getDrawingPanel();
             panel.marqueeRect = oldSelection;
             panel.repaint();
         }
         
         @Override
         public void redo() throws CannotRedoException {
             super.redo();
             DrawingPanel panel = SNESTile.getInstance().getDrawingPanel();
             panel.marqueeRect = newSelection;
             panel.repaint();
         }
         
         @Override
         public String getPresentationName() {
             return name;
         }
         
     }
     
 }
