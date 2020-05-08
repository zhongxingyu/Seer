 package control;
 
 import java.awt.Frame;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 
 import javax.media.j3d.BranchGroup;
 import javax.media.j3d.Canvas3D;
 
 import view.TriangleArrayChild;
 import view.TriangleViewer;
 
 import com.sun.j3d.utils.picking.PickCanvas;
import com.sun.j3d.utils.picking.PickIntersection;
 import com.sun.j3d.utils.picking.PickTool;
import com.sun.j3d.utils.picking.PickResult;
 
 public class PickingEnvironment implements MouseListener, MouseMotionListener {
 
     PickCanvas pickCanvas;
     NewMouseRotate mouseRotate = null;
     int xPressed;
     int yPressed;
 
     public PickingEnvironment(Canvas3D c, BranchGroup group) {
 
         Frame frame = new Frame("oooo");
         frame.addWindowListener(new WindowAdapter() {
 
             @Override
             public void windowClosing(WindowEvent winEvent) {
                 System.exit(0);
             }
         });
 
         this.pickCanvas = new PickCanvas(c, group);
         this.pickCanvas.setMode(PickTool.GEOMETRY_INTERSECT_INFO);
         c.addMouseListener(this);
         c.addMouseMotionListener(this);
 
     }
 
     public NewMouseRotate getMouseRotate() {
         return this.mouseRotate;
     }
 
     public void setMouseRotate(NewMouseRotate mouseRotateIn) {
         this.mouseRotate = mouseRotateIn;
     }
 
     @Override
     public void mouseClicked(MouseEvent e) {
         int buttonDown = e.getButton();
 
         if (buttonDown == MouseEvent.BUTTON1) {
            // Bouton gauche enfonc
             this.pickCanvas.setShapeLocation(e);
             PickResult result = this.pickCanvas.pickClosest();
             if (result == null) {
                 System.out.println("Nothing picked");
             } else {
                PickIntersection PI = result.getIntersection(0);
                TriangleArrayChild ta = (TriangleArrayChild) PI
                         .getGeometryArray();
                 System.out.println(ta.triangle);
                // mouseRotate.setCenter(ta);
             }
         }
     }
 
     @Override
     public void mousePressed(MouseEvent e) {
         System.out.println("Mouse Pressed");
         this.xPressed = e.getX();
         this.yPressed = e.getY();
     }
 
     @Override
     public void mouseReleased(MouseEvent e) {
         int buttonDown = e.getButton();
         if (buttonDown == MouseEvent.BUTTON1) {
             for (int x = this.xPressed; x < e.getX(); x = x + 2) {
                 for (int y = this.yPressed; y < e.getY(); y = y + 2) {
                     this.pickCanvas.setShapeLocation(x, y);
                     PickResult result = this.pickCanvas.pickClosest();
                     if (result == null) {
                         System.out.println("Nothing picked");
                     } else {
                         TriangleViewer s = (TriangleViewer) result
                                 .getNode(PickResult.SHAPE3D);
                         if (s != null) {
                             System.out.println(s.getClass().getName());
                             s.select();
                             s.changeColor();
                             this.mouseRotate.setCenter(s);
                         } else {
                             System.out.println("null");
                         }
                     }
                 }
             }
         }
     }
 
     @Override
     public void mouseDragged(MouseEvent arg0) {
         // TODO Auto-generated method stub
     }
 
     @Override
     public void mouseEntered(MouseEvent arg0) {
         // TODO Auto-generated method stub
     }
 
     @Override
     public void mouseExited(MouseEvent arg0) {
         // TODO Auto-generated method stub
     }
 
     @Override
     public void mouseMoved(MouseEvent arg0) {
         // TODO Auto-generated method stub
     }
 }
