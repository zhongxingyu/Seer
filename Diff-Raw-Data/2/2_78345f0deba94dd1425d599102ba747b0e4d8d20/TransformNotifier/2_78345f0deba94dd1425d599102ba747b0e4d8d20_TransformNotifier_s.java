 package wiitracker.fingertracking;
 
 
 import javax.media.jai.PerspectiveTransform;
 import javax.swing.event.EventListenerList;
 import motej.IrPoint;
 import motej.event.IrCameraEvent;
 import motej.event.IrCameraListener;
 import java.awt.geom.Point2D;
 
 public class TransformNotifier implements IrCameraListener, IrCameraNotifier {
 	protected PerspectiveTransform transform = new PerspectiveTransform();
 	EventListenerList listenerList = new EventListenerList();
 	
 	public void irImageChanged(IrCameraEvent evt) {
 		IrPoint[] in = new IrPoint[4];
 		for (int i = 0; i < 4; i++) {
 			in[i] = evt.getIrPoint(i);
 		}
 		
 		
 	}
 	public void setTransform(PerspectiveTransform p) {
 		transform.setTransform(p);
 	}
 	
 	public Point2D[] transform (Point2D[] points) {
		transform.transform(points, 0, points, 0, points.size);
 		return points;
 	}
 	public void addIrCameraListener(IrCameraListener listener) {
 		listenerList.add(IrCameraListener.class, listener);
 	}
 }
