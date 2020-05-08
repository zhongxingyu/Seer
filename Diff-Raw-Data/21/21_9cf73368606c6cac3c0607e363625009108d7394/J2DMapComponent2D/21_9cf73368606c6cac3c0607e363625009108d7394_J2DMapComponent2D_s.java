 package amber.gui.editor.map._2d;
 
import amber.data.map.Layer;
 import amber.data.map.LevelMap;
import amber.data.map.Tile;
 import amber.data.map.codec.Codec;
import amber.data.res.Tileset;
import amber.data.sparse.SparseMatrix;
import amber.data.sparse.SparseVector;
 import amber.gl.FrameTimer;
 import amber.gui.editor.map.IMapComponent;
 import amber.gui.editor.map.MapContext;
 import amber.gui.editor.map.tool._2d.Brush2D;
 import amber.gui.editor.map.tool._2d.Eraser2D;
 import amber.gui.editor.map.tool._2d.Fill2D;
 import amber.gui.editor.map.tool._2d.Tool2D;
 import amber.gui.misc.ErrorHandler;
 import amber.input.awt.AWTInputMap;
 import amber.swing.MenuBuilder;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.awt.image.BufferedImage;
 import java.io.DataOutputStream;
 import java.io.FileOutputStream;
 import javax.swing.*;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 import static amber.gui.editor.map.MapContext.MODE_BRUSH;
 import static amber.gui.editor.map.MapContext.MODE_ERASE;
 import static amber.gui.editor.map.MapContext.MODE_FILL;
 import amber.swing.misc.TransferableImage;
 import java.awt.datatransfer.Clipboard;
 import java.awt.datatransfer.ClipboardOwner;
 import java.awt.datatransfer.Transferable;
 
 /**
  * @author xiaomao
  */
 public class J2DMapComponent2D extends JComponent implements IMapComponent {
 
     protected final MapContext context = new MapContext();
     protected FrameTimer timer = new FrameTimer();
     protected Point cursorPos = new Point();
     protected JScrollPane display = new JScrollPane(this);
     protected Font infoFont = UIManager.getFont("MapEditor.font");
     protected Color background = UIManager.getColor("MapEditor.background");
     protected boolean moved = false;
     protected boolean modified = false;
     protected J2DMapRenderer2D renderer;
 
     public J2DMapComponent2D(LevelMap map) {
         setFocusable(true);
         setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
 
         renderer = new J2DMapRenderer2D(map);
         renderer.drawGrid(true);
 
         display.getVerticalScrollBar().setUnitIncrement(16);
         display.getHorizontalScrollBar().setUnitIncrement(16);
 
         setBackground(Color.WHITE);
 
         addKeyListener(new KeyAdapter() {
             @Override
             public void keyPressed(KeyEvent e) {
                 onKeyPress(e);
                 repaint();
             }
         });
 
         MouseAdapter adapter = new MouseAdapter() {
             int lx, ly;
 
             boolean isDrag(MouseEvent e) {
                 return (e.isControlDown() && SwingUtilities.isLeftMouseButton(e)) || SwingUtilities.isMiddleMouseButton(e);
             }
 
             @Override
             public void mouseDragged(MouseEvent e) {
                 if (isDrag(e)) {
                     int dx = lx - e.getXOnScreen();
                     int dy = ly - e.getYOnScreen();
                     display.getHorizontalScrollBar().setValue(display.getHorizontalScrollBar().getValue() + dx);
                     display.getVerticalScrollBar().setValue(display.getVerticalScrollBar().getValue() + dy);
 
                     lx = e.getXOnScreen();
                     ly = e.getYOnScreen();
                 } else {
                     onMouseDown(e);
                 }
             }
 
             @Override
             public void mouseMoved(MouseEvent e) {
                 onMouseMove(e.getX(), e.getY());
                 if (moved) {
                     repaint();
                 }
             }
 
             @Override
             public void mouseReleased(MouseEvent e) {
                 if (isDrag(e)) {
                     mouseDragged(e);
                 }
             }
 
             @Override
             public void mousePressed(MouseEvent e) {
                 if (isDrag(e)) {
                     lx = e.getXOnScreen();
                     ly = e.getYOnScreen();
                 } else {
                     onMouseDown(e);
                 }
             }
         };
 
         addMouseMotionListener(adapter);
         addMouseListener(adapter);
 
         display.addMouseWheelListener(new MouseWheelListener() {
             @Override
             public void mouseWheelMoved(MouseWheelEvent e) {
                 if (e.isControlDown()) {
                     double delta = e.getPreciseWheelRotation();
                     renderer.zoomTo(Math.max(1, (int) (renderer.zoom() - delta)));
                     repaint();
                     updateSize();
                     e.consume();
                 }
             }
         });
 
         display.getViewport().addChangeListener(new ChangeListener() {
             @Override
             public void stateChanged(ChangeEvent changeEvent) {
                 Point point = MouseInfo.getPointerInfo().getLocation();
                 SwingUtilities.convertPointFromScreen(point, J2DMapComponent2D.this);
                 onMouseMove(point.x, point.y);
             }
         });
 
         setComponentPopupMenu(getContextMenus()[0].getPopupMenu());
 
         context.map = map;
         updateSize();
     }
 
     private void updateSize() {
         int u = renderer.zoom();
         Dimension size = new Dimension(context.map.getWidth() * u + 1, context.map.getLength() * u + 1);
         setPreferredSize(size);
         setSize(new Dimension(context.map.getWidth() * u + 1, context.map.getLength() * u + 1));
         display.validate();
     }
 
     @Override
     public MapContext getMapContext() {
         return context;
     }
 
     @Override
     public Component getComponent() {
         return display;
     }
     protected boolean info = true, grid = true;
 
     @Override
     public JMenu[] getContextMenus() {
         return new JMenu[]{
             new MenuBuilder("View").addCheckbox("Info", true, new AbstractAction() {
                 public void actionPerformed(ActionEvent e) {
                     info ^= true;
                     repaint();
                 }
             }).addCheckbox("Grid", true, new AbstractAction() {
                 public void actionPerformed(ActionEvent e) {
                     grid ^= true;
                     repaint();
                 }
             }).create()
         };
     }
 
     @Override
     public boolean modified() {
         return modified;
     }
 
     @Override
     public void save() {
         new Thread() {
             @Override
             public void run() {
                 try {
                     FileOutputStream fos = new FileOutputStream(context.outputFile);
                     Codec.getLatestCodec().compileMap(context.map, new DataOutputStream(fos));
                     fos.close();
                 } catch (Exception ex) {
                     ErrorHandler.alert(ex);
                 }
             }
         }.start();
         modified = false;
     }
 
     @Override
     public void paintComponent(Graphics g_) {
         int u = renderer.zoom();
         Graphics2D g = (Graphics2D) g_;
         g.setColor(Color.WHITE);
         g.fillRect(0, 0, getWidth(), getHeight());
 
         renderer.draw(g_);
 
         g.setStroke(new BasicStroke(2));
         g.setColor(Color.BLACK);
         Tool2D currentTool = currentTool();
         if (cursorPos != null && currentTool != null) {
             Dimension size = currentTool.getDrawRectangleSize();
             if (size.height > 0 && size.width > 0) {
                 int x = cursorPos.x * u, y = context.map.getLength() * u - cursorPos.y * u - size.height * u;
                 int width = size.width * u, height = size.height * u;
                 g.drawRect(x, y, width, height);
                 Composite old = g.getComposite();
                 g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .7f));
                 g.drawImage(currentTool.getPreview(), x, y, width, height, null);
                 g.setComposite(old);
             }
         }
 

         if (info) {
             g.setFont(infoFont);
             g.translate(0, -(getHeight() - context.map.getLength() * renderer.zoom()));
             g.drawString(String.format("Cursor: (%d, %d)", cursorPos.x, cursorPos.y), 4, 4 + g.getFontMetrics().getHeight());
         }
     }
 
     protected void onKeyPress(KeyEvent e) {
         int delta = Math.min(timer.getDelta() / 5, 200);
         if (e.getModifiersEx() == 0) {
             switch (e.getKeyCode()) {
                 case KeyEvent.VK_W:
                 case KeyEvent.VK_UP:
                     display.getVerticalScrollBar().setValue(display.getVerticalScrollBar().getValue() - delta);
                     break;
                 case KeyEvent.VK_S:
                 case KeyEvent.VK_DOWN:
                     display.getVerticalScrollBar().setValue(display.getVerticalScrollBar().getValue() + delta);
                     break;
                 case KeyEvent.VK_A:
                 case KeyEvent.VK_LEFT:
                     display.getHorizontalScrollBar().setValue(display.getHorizontalScrollBar().getValue() - delta);
                     break;
                 case KeyEvent.VK_D:
                 case KeyEvent.VK_RIGHT:
                     display.getHorizontalScrollBar().setValue(display.getHorizontalScrollBar().getValue() + delta);
                     break;
                 default: {
                     Tool2D tool = currentTool();
                     if (tool != null) {
                         tool.doKey(AWTInputMap.map(e));
                     }
                 }
             }
         } else if (e.isControlDown()) {
             switch (e.getKeyCode()) {
                 case KeyEvent.VK_I:
                     BufferedImage shot = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
                     Graphics2D g2 = shot.createGraphics();
                     g2.setClip(getBounds());
                     paintComponent(g2);
                     TransferableImage trans = new TransferableImage(shot);
                     Clipboard c = Toolkit.getDefaultToolkit().getSystemClipboard();
                     c.setContents(trans, new ClipboardOwner() {
                         public void lostOwnership(Clipboard clipboard, Transferable contents) {
                         }
                     });
                 case KeyEvent.VK_Z:
                     if (!context.undoStack.empty()) {
                         context.redoStack.push(context.map.clone());
                        context.map = context.undoStack.pop();
                     }
                     break;
                 case KeyEvent.VK_Y:
                     if (!context.redoStack.empty()) {
                         context.undoStack.push(context.map.clone());
                        context.map = context.redoStack.pop();
                     }
                     break;
                 case KeyEvent.VK_S:
                     save();
                     break;
                 case KeyEvent.VK_0:
                 case KeyEvent.VK_NUMPAD0:
                     renderer.zoomTo(32);
                     repaint();
                     updateSize();
                     break;
                 case KeyEvent.VK_EQUALS:
                 case KeyEvent.VK_PLUS:
                 case KeyEvent.VK_ADD:
                     renderer.zoomTo(renderer.zoom() + 4);
                     repaint();
                     updateSize();
                     break;
                 case KeyEvent.VK_MINUS:
                 case KeyEvent.VK_SUBTRACT:
                     renderer.zoomTo(Math.max(1, renderer.zoom() - 4));
                     repaint();
                     updateSize();
                     break;
             }
         }
     }
 
     protected void onMouseDown(MouseEvent e) {
         requestFocusInWindow();
         onMouseMove(e.getX(), e.getY());
         if ((SwingUtilities.isLeftMouseButton(e) && e.getID() == MouseEvent.MOUSE_PRESSED)
                 || (e.getID() == MouseEvent.MOUSE_DRAGGED && moved)) {
             LevelMap pre = context.map.clone();
             Tool2D tool = currentTool();
 
             if (tool != null && tool.apply(cursorPos.x, cursorPos.y)) {
                 context.undoStack.push(pre);
                 modified = true;
             }
             repaint();
         }
     }
 
     protected void onMouseMove(int x, int y) {
         x /= renderer.zoom();
         y = context.map.getLength() - y / renderer.zoom() - 1;
         moved = cursorPos.x != x || cursorPos.y != y;
         if (moved) {
             cursorPos.setLocation(x, y);
         }
     }
     protected Tool2D brushTool = new Brush2D(context);
     protected Tool2D eraseTool = new Eraser2D(context);
     protected Tool2D fillTool = new Fill2D(context);
 
     private Tool2D currentTool() {
         switch (context.drawMode) {
             case MODE_BRUSH:
                 return brushTool;
             case MODE_FILL:
                 return fillTool;
             case MODE_ERASE:
                 return eraseTool;
         }
         return null;
     }
 }
