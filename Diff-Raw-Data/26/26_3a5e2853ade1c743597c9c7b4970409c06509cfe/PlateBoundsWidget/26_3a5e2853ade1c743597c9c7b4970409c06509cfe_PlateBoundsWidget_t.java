 package edu.ualberta.med.scannerconfig;
 
 import java.io.File;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.core.runtime.ListenerList;
 import org.eclipse.jface.util.SafeRunnable;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.events.MouseListener;
 import org.eclipse.swt.events.PaintEvent;
 import org.eclipse.swt.events.PaintListener;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.GC;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.widgets.Canvas;
 import org.eclipse.ui.PlatformUI;
 
 public class PlateBoundsWidget {
 
     private Canvas canvas;
 
     public static final String PALLET_IMAGE_FILE = "plates.bmp";
 
     public static final double PALLET_IMAGE_DPI = 100.0;
 
     private boolean pointTopLeft;
 
     private Rectangle plateRect;
 
     private ScannerRegion scannerRegion;
 
     private Image img;
 
     private long platesFileLastModified;
 
     protected ListenerList changeListeners = new ListenerList();
 
     // pointTopLeft: Used to determine which point the user is currently
     // adjusting.The point is either top-left or bottom-right.
 
     public PlateBoundsWidget(Canvas c, ScannerRegion r, final Color mycolor) {
         canvas = c;
         scannerRegion = r;
         plateRect = new Rectangle(-1, -1, -1, -1);
 
         File platesFile = new File(PlateBoundsWidget.PALLET_IMAGE_FILE);
         if (platesFile.exists()) {
             platesFileLastModified = platesFile.lastModified();
             img = new Image(PlatformUI.getWorkbench()
                 .getActiveWorkbenchWindow().getShell().getDisplay(),
                 PALLET_IMAGE_FILE);
         }
 
         pointTopLeft = true;
 
         canvas.addMouseListener(new MouseListener() {
             @Override
             public void mouseDoubleClick(MouseEvent e) {
             }
 
             @Override
             public void mouseDown(MouseEvent e) {
                 if (img == null)
                     return;
 
                 getPlateRect();
 
                 if (pointTopLeft) {
                     plateRect.x = e.x;
                     plateRect.y = e.y;
                     plateRect.width = 0;
                     plateRect.height = 0;
                 } else {
                     if (e.x > plateRect.x) {
                         plateRect.width = e.x - plateRect.x;
                     } else {
                         plateRect.width = plateRect.x - e.x;
                         plateRect.x = e.x;
                     }
 
                     if (e.y > plateRect.y) {
                         plateRect.height = e.y - plateRect.y;
                     } else {
                         plateRect.height = plateRect.y - e.y;
                         plateRect.y = e.y;
                     }
                 }
 
                 pointTopLeft = !pointTopLeft;
                 canvas.redraw();
                 canvas.update();
                 notifyChangeListener();
             }
 
             @Override
             public void mouseUp(MouseEvent e) {
             }
         });
 
         canvas.addPaintListener(new PaintListener() {
             public void paintControl(PaintEvent e) {
                 File platesFile = new File(PlateBoundsWidget.PALLET_IMAGE_FILE);
                 if (platesFile.exists()
                     && (platesFileLastModified != platesFile.lastModified())) {
                     platesFileLastModified = platesFile.lastModified();
                     img = new Image(PlatformUI.getWorkbench()
                         .getActiveWorkbenchWindow().getShell().getDisplay(),
                         PALLET_IMAGE_FILE);
                 }
 
                 if (img == null)
                     return;
 
                 getPlateRect();
 
                 Rectangle imgBounds = img.getBounds();
                 Rectangle canvasBounds = canvas.getBounds();
 
                 GC gc = new GC(canvas);
                 gc.drawImage(img, 0, 0, imgBounds.width, imgBounds.height, 0,
                     0, canvasBounds.width, canvasBounds.height);
                 gc.setForeground(mycolor);
                 gc.drawRectangle(plateRect);
                 gc.dispose();
             }
         });
 
         canvas.update();
     }
 
     public void addChangeListener(IPlateBoundsListener listener) {
         changeListeners.add(listener);
     }
 
     private void notifyChangeListener() {
         Object[] listeners = changeListeners.getListeners();
         for (int i = 0; i < listeners.length; ++i) {
             final IPlateBoundsListener l = (IPlateBoundsListener) listeners[i];
             SafeRunnable.run(new SafeRunnable() {
                 public void run() {
                     l.change();
                 }
             });
         }
 
     }
 
     private void getPlateRect() {
         Assert.isNotNull(canvas, "canvas is null");
 
         if (plateRect.width != -1)
             return;
 
         Rectangle imgBounds = img.getBounds();
         Rectangle canvasBounds = canvas.getBounds();
 
         plateRect.x = (int) (scannerRegion.left * canvasBounds.width
             * PALLET_IMAGE_DPI / imgBounds.width);
         plateRect.y = (int) (scannerRegion.top * canvasBounds.height
             * PALLET_IMAGE_DPI / imgBounds.height);
         plateRect.width = (int) (scannerRegion.right * canvasBounds.width
             * PALLET_IMAGE_DPI / imgBounds.width)
             - plateRect.x;
         plateRect.height = (int) (scannerRegion.bottom * canvasBounds.height
             * PALLET_IMAGE_DPI / imgBounds.height)
             - plateRect.y;
     }
 
     public void assignRegionLeft(double left) {
         Assert.isNotNull(canvas, "canvas is null");
 
         Rectangle imgBounds = img.getBounds();
         Rectangle canvasBounds = canvas.getBounds();
         plateRect.x = (int) (left * canvasBounds.width * PALLET_IMAGE_DPI / imgBounds.width);
         canvas.redraw();
         canvas.update();
     }
 
     public void assignRegionTop(double top) {
         Assert.isNotNull(canvas, "canvas is null");
         Rectangle imgBounds = img.getBounds();
         Rectangle canvasBounds = canvas.getBounds();
         plateRect.y = (int) (top * canvasBounds.height * PALLET_IMAGE_DPI / imgBounds.height);
         canvas.redraw();
         canvas.update();
     }
 
     public void assignRegionRight(double right) {
         Assert.isNotNull(canvas, "canvas is null");
         Rectangle imgBounds = img.getBounds();
         Rectangle canvasBounds = canvas.getBounds();
         plateRect.width = (int) (right * canvasBounds.width * PALLET_IMAGE_DPI / imgBounds.width)
             - plateRect.x;
         canvas.redraw();
         canvas.update();
     }
 
     public void assignRegionBottom(double bottom) {
         Assert.isNotNull(canvas, "canvas is null");
         Rectangle imgBounds = img.getBounds();
         Rectangle canvasBounds = canvas.getBounds();
         plateRect.height = (int) (bottom * canvasBounds.height
             * PALLET_IMAGE_DPI / imgBounds.height)
             - plateRect.y;
         canvas.redraw();
         canvas.update();
     }
 
     public ScannerRegion getPlateRetion() {
         ScannerRegion region = new ScannerRegion();
 
         Rectangle imgBounds = img.getBounds();
         Rectangle canvasBounds = canvas.getBounds();
         double widthRatio = imgBounds.width / PALLET_IMAGE_DPI
             / canvasBounds.width;
         double heightRatio = imgBounds.height / PALLET_IMAGE_DPI
             / canvasBounds.height;
 
         region.set(plateRect.x * widthRatio, plateRect.y * heightRatio,
             (plateRect.width + plateRect.x) * widthRatio,
             (plateRect.height + plateRect.y) * heightRatio);
 
         return region;
     }
 }
