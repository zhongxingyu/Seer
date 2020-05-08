 package amber.gui.editor.map;
 
 import amber.data.map.Layer;
 import amber.data.map.LevelMap;
 import amber.data.map.Tile;
 import amber.data.map.codec.Codec;
 import amber.data.res.Tileset;
 import amber.data.sparse.SparseMatrix;
 import amber.data.sparse.SparseVector;
 import amber.gl.FrameTimer;
 import static amber.gui.editor.map.MapContext.*;
 import amber.gui.editor.map.tool._2d.Brush2D;
 import amber.gui.editor.map.tool._2d.Eraser2D;
 import amber.gui.editor.map.tool._2d.Fill2D;
 import amber.gui.editor.map.tool._2d.Tool2D;
 import amber.gui.misc.ErrorHandler;
 import amber.input.awt.AWTInputMap;
 import amber.swing.MenuBuilder;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.*;
 import java.awt.image.BufferedImage;
 import java.io.DataOutputStream;
 import java.io.FileOutputStream;
 import java.util.List;
 import javax.swing.*;
 
 import static amber.gui.editor.map.MapContext.MODE_BRUSH;
 import static amber.gui.editor.map.MapContext.MODE_ERASE;
 import static amber.gui.editor.map.MapContext.MODE_FILL;
 import amber.swing.misc.TransferableImage;
 import java.awt.datatransfer.Clipboard;
 import java.awt.datatransfer.ClipboardOwner;
 import java.awt.datatransfer.Transferable;
 import java.io.File;
 import java.io.IOException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.imageio.ImageIO;
 
 /**
  * @author xiaomao
  */
 public class J2DMapComponent2D extends JComponent implements IMapComponent {
 
     private static int u = 32;
     protected final MapContext context = new MapContext();
     protected Point cursorPos = new Point();
     protected JScrollPane display = new JScrollPane(this);
     protected Font infoFont = UIManager.getFont("MapEditor.font");
     protected Color background = UIManager.getColor("MapEditor.background");
     protected boolean moved = false;
 
     public J2DMapComponent2D(LevelMap map) {
         setFocusable(true);
         setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
 
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
             @Override
             public void mouseDragged(MouseEvent mouseEvent) {
                 onMouseDown(mouseEvent);
             }
 
             @Override
             public void mouseMoved(MouseEvent mouseEvent) {
                 onMouseMove(mouseEvent);
                 if (moved) {
                     repaint();
                 }
             }
 
             @Override
             public void mousePressed(MouseEvent mouseEvent) {
                 onMouseDown(mouseEvent);
             }
         };
 
         addMouseMotionListener(adapter);
         addMouseListener(adapter);
 
         display.addMouseWheelListener(new MouseWheelListener() {
             @Override
             public void mouseWheelMoved(MouseWheelEvent e) {
                 if (e.isControlDown()) {
                     double delta = e.getPreciseWheelRotation();
                     u = Math.max(1, (int) (u - delta));
                     repaint();
                     updateSize();
                     e.consume();
                 }
             }
         });
 
         setComponentPopupMenu(getContextMenus()[0].getPopupMenu());
 
         context.map = map;
         updateSize();
     }
 
     private void updateSize() {
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
     public void paintComponent(Graphics g_) {
         Graphics2D g = (Graphics2D) g_;
         g.setColor(Color.WHITE);
         g.fillRect(0, 0, getWidth(), getHeight());
 
         Rectangle clip = g.getClipBounds();
         int x1 = Math.max(0, clip.x / u - 1);
         int x2 = Math.min(x1 + clip.width / u + 3, context.map.getWidth());
         int y1 = Math.max(0, clip.y / u - 1);
         int y2 = Math.min(y1 + clip.height / u + 3, context.map.getLength());
 
         List<Layer> layers = context.map.getLayers();
         for (int i = 0; i != layers.size(); i++) {
             drawLayer(g, layers.get(i), x1, y1, x2, y2);
         }
         drawGrid(g, x1, y1, x2, y2);
 
         if (info) {
             g.setColor(Color.BLACK);
             g.setFont(infoFont);
             g.translate(0, -(getHeight() - context.map.getLength() * u));
             g.drawString(String.format("Cursor: (%d, %d)", cursorPos.x, cursorPos.y), 4, 4 + g.getFontMetrics().getHeight());
         }
     }
 
     protected void drawLayer(Graphics2D g, Layer layer, int x1, int y1_, int x2, int y2_) {
         SparseVector<SparseMatrix<Tile>> tileVector = layer.tileMatrix();
         int y1 = context.map.getLength() - y2_;
         int y2 = context.map.getLength() - y1_;
 
         for (int x = x1; x < x2; x++) {
             for (int y = y1; y < y2; y++) {
                 SparseVector.SparseVectorIterator iterator = tileVector.iterator();
                 while (iterator.hasNext()) {
                     SparseMatrix<Tile> matrix = (SparseMatrix<Tile>) iterator.next();
                     Tile t = matrix.get(x, y);
 
                     if (t != null) {
                         Tileset.TileSprite sprite = t.getSprite();
                         Tileset sheet = sprite.getTileset();
                         BufferedImage texture = sheet.getImage();
 
                         Point start = sprite.getStart();
                         Dimension size = sprite.getSize();
 
                         int dx = x * u;
                         int dy = context.map.getLength() * u - y * u - u;
 
                         g.drawImage(texture, dx, dy, dx + u, dy + u, start.x, start.y, start.x + size.width, start.y + size.height, null);
                     }
                 }
             }
         }
     }
     static final Stroke stroke2 = new BasicStroke(2);
     static final Stroke stroke3 = new BasicStroke(3);
 
     protected void drawGrid(Graphics2D g, int x1, int y1, int x2, int y2) {
         Color oldColor = g.getColor();
         g.setColor(Color.GRAY);
 
         Stroke oldStroke = g.getStroke();
 
         if (grid) {
             for (int x = x1; x <= x2; x++) {
                 g.drawLine(x * u, 0, x * u, context.map.getLength() * u);
             }
             for (int y = y1; y <= y2; y++) {
                 g.drawLine(0, y * u, context.map.getWidth() * u, y * u);
             }
         }
 
         g.setColor(Color.BLACK);
         g.setStroke(stroke3);
         g.drawLine(0, 0, context.map.getWidth() * u, 0);
         g.drawLine(0, 0, 0, context.map.getLength() * u);
         g.drawLine(context.map.getWidth() * u, 0, context.map.getWidth() * u, context.map.getLength() * u);
        g.drawLine(0, context.map.getLength() * u, context.map.getWidth() * u, context.map.getLength() * u);
 
         g.setStroke(stroke2);
         if (cursorPos != null) {
             Dimension size = currentTool().getDrawRectangleSize();
             if (size.height > 0 && size.width > 0) {
                 g.drawRect(cursorPos.x * u, context.map.getLength() * u - cursorPos.y * u - size.height * u, size.width * u, size.height * u);
             }
         }
 
         g.setColor(oldColor);
         g.setStroke(oldStroke);
     }
     protected FrameTimer timer = new FrameTimer();
 
     protected void onKeyPress(KeyEvent e) {
         int delta = timer.getDelta() / 5;
         if (delta > 200) {
             delta = 200;
         }
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
             }
         } else if (e.isControlDown()) {
             switch (e.getKeyCode()) {
                 case KeyEvent.VK_I:
                     BufferedImage shot = new BufferedImage(getWidth(), getHeight(),BufferedImage.TYPE_INT_ARGB);
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
                     break;
                 case KeyEvent.VK_0:
                 case KeyEvent.VK_NUMPAD0:
                     u = 32;
                     repaint();
                     updateSize();
                     break;
                 case KeyEvent.VK_EQUALS:
                 case KeyEvent.VK_PLUS:
                 case KeyEvent.VK_ADD:
                     u += 4;
                     repaint();
                     updateSize();
                     break;
                 case KeyEvent.VK_MINUS:
                 case KeyEvent.VK_SUBTRACT:
                     u = Math.max(1, u - 4);
                     repaint();
                     updateSize();
                     break;
             }
         } else {
             currentTool().doKey(AWTInputMap.map(e));
         }
     }
 
     protected void onMouseDown(MouseEvent e) {
         requestFocusInWindow();
         onMouseMove(e);
         if ((SwingUtilities.isLeftMouseButton(e) && e.getID() == MouseEvent.MOUSE_PRESSED)
                 || (e.getID() == MouseEvent.MOUSE_DRAGGED && moved)) {
             LevelMap pre = context.map.clone();
             Tool2D tool = currentTool();
 
             if (tool != null && tool.apply(cursorPos.x, cursorPos.y)) {
                 context.undoStack.push(pre);
             }
             repaint();
         }
     }
 
     protected void onMouseMove(MouseEvent e) {
         int x = e.getX() / u, y = context.map.getLength() - e.getY() / u - 1;
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
