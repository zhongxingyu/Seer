 package amber.gui.editor.map;
 
 import amber.data.res.Tileset;
 import amber.data.res.Tileset.TileSprite;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 
 /**
  * @author Tudor
  */
 public class TileSheetRenderer extends JComponent {
 
     private Tileset sheet;
     private Point clickLocation;
     private Point mouseLocation;
     private TileSelector parent;
 
     /**
      * Constructs a new TileSheetRenderer
      *
      * @param sheet the TileSheet to render
      * @param parent the TileSelector to whom to pass selection updates to
      */
     public TileSheetRenderer(final Tileset sheet, final TileSelector parent) {
         this.sheet = sheet;
         this.parent = parent;
         System.out.println("New tilesheet renderer: " + sheet);
         MouseAdapter ma = new MouseAdapter() {
             @Override
             public void mousePressed(MouseEvent e) {
                 mouseLocation = clickLocation = relative(e.getPoint());
                 if (isInBounds(clickLocation)) {
                     parent.setSelection(new TileSprite[][]{{sheet.getTile(clickLocation.x, clickLocation.y)}});
                     repaint();
                 }
             }
 
             @Override
             public void mouseDragged(MouseEvent e) {
                 mouseLocation = relative(e.getPoint());
                 if (isInBounds(mouseLocation)) {
                     TileSprite[][] selection;
                     if (mouseLocation.equals(clickLocation)) {
                         selection = new TileSprite[][]{{sheet.getTile(clickLocation.x, clickLocation.y)}};
                     } else {
                         int x1 = Math.min(clickLocation.x, mouseLocation.x);
                         int x2 = Math.max(clickLocation.x, mouseLocation.x) + 1;
                         int y1 = Math.min(clickLocation.y, mouseLocation.y);
                         int y2 = Math.max(clickLocation.y, mouseLocation.y) + 1;
 
                         selection = new TileSprite[x2 - x1][y2 - y1];
                         for (int x = 0; x != x2 - x1; x++) {
                             for (int y = 0; y != y2 - y1; y++) {
                                 selection[x][y] = sheet.getTile(x + x1, y + y1);
                             }
                         }
                     }
                     parent.setSelection(selection);
                     repaint();
                 }
             }
         };
         addMouseListener(ma);
         addMouseMotionListener(ma);
         setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
     }
 
     @Override
     public Dimension getPreferredSize() {
         return getMaximumSize();
     }
 
     @Override
     public Dimension getMinimumSize() {
         return getMaximumSize();
     }
 
     @Override
     public Dimension getMaximumSize() {
         return new Dimension(8 * 32, sheet.getHeight() * 32);
     }
 
     @Override
     public void paintComponent(Graphics g) {
         super.paintComponent(g);
        /*for (int x = 0; x < sheet.getWidth(); x++) {
            for (int y = 0; y < sheet.getHeight(); y++) {
                Tileset.TileSprite t = sheet.getTile(x, y);
                if (t != null) {
                    Dimension size = t.getSize();
                    Point start = t.getStart();
                    g.drawImage(sheet.getImage(), x * 32, y * 32, x * 32 + 32, y * 32
                            + 32, start.x, start.y, size.width + start.x, size.height + start.y, null);
                }
            }
        }*/
        Rectangle clip = g.getClipBounds();
         Dimension size = sheet.getTile(0, 0).getSize();
         double rx = 32 / size.width;
         double ry = 32 / size.height;
        g.drawImage(sheet.getImage(), (int)(clip.x * rx), (int)(clip.y * ry),
                (int)((clip.x + clip.width) * rx), (int)((clip.y + clip.height) * ry),
                clip.x, clip.y, clip.x + clip.width, clip.y + clip.height, null);
 
         if (mouseLocation != null && clickLocation != null) {
             int x1 = Math.min(clickLocation.x, mouseLocation.x);
             int x2 = Math.max(clickLocation.x, mouseLocation.x);
             int y1 = Math.min(clickLocation.y, mouseLocation.y);
             int y2 = Math.max(clickLocation.y, mouseLocation.y);
             ((Graphics2D) g).setStroke(new BasicStroke(2F));
             g.drawRect(x1 * 32, y1 * 32, parent.getSelection().length * 32, parent.getSelection()[0].length * 32);
             g.setColor(new Color(.1f, .2f, 1f, .4f));
             g.fillRect(x1 * 32, y1 * 32, parent.getSelection().length * 32, parent.getSelection()[0].length * 32);
         }
     }
 
     private Point relative(Point onScreen) {
         return new Point(onScreen.x / 32, onScreen.y / 32);
     }
 
     private boolean isInBounds(Point rel) {
         return rel.x >= 0 && rel.y >= 0 && rel.x <= sheet.getWidth() - 2 && rel.y <= sheet.getHeight() - 2;
     }
 }
