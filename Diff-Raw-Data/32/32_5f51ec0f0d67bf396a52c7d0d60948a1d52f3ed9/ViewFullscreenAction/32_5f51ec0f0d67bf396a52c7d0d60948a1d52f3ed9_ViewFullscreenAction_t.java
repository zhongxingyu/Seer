 package kkckkc.jsourcepad.action;
 
 import java.awt.AWTEvent;
import java.awt.Container;
 import java.awt.Cursor;
 import java.awt.EventQueue;
 import java.awt.Image;
 import java.awt.Insets;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.Toolkit;
 import java.awt.event.AWTEventListener;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.image.MemoryImageSource;
 
 import javax.swing.AbstractAction;
 import javax.swing.JFrame;
 import javax.swing.Timer;
 
import kkckkc.jsourcepad.model.Application;
 import kkckkc.jsourcepad.model.Window;
 
 // see http://code.google.com/p/bookjar-utils/source/browse/BookJar-utils/src/util/swing/components/FullScreenFrame.java?r=152&spec=svn194
 public class ViewFullscreenAction extends AbstractAction {
 
 	private Window w;
 
 	Rectangle applicationPosition = new Rectangle();
 
 	public ViewFullscreenAction(Window w) {
 		this.w = w;
 	}
 
 	boolean MAXIMIZED = false;
 	boolean PROCESS_WINDOW_EVENTS = false;
 	private static Cursor emptyCursor = createEmptyCursor();
 
 	private static Cursor createEmptyCursor() {
 		int[] pixels = new int[0/* 16 * 16 */];
 		Image image = Toolkit.getDefaultToolkit().createImage(
 				new MemoryImageSource(0, 0, pixels, 0, 0));
 		return Toolkit.getDefaultToolkit().createCustomCursor(image,
 				new Point(0, 0), "InvisibleCursor");
 	}
 
 	private transient Timer timeToHideMouse = new Timer(1500, new HideMouse());
 
 	private class HideMouse implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
			Container c = Application.get().getWindowManager().getContainer(w);
			c.setCursor(emptyCursor);
 		}
 	}
 
 	private transient AWTEventListener scheduleHideMouse = new HideMouseStart();
 
 	private class HideMouseStart implements AWTEventListener {
 		public void eventDispatched(AWTEvent e) {
 			if (MAXIMIZED) {
				Container c = Application.get().getWindowManager().getContainer(w);
				c.setCursor(Cursor.getDefaultCursor());
 				timeToHideMouse.restart();
 				timeToHideMouse.setRepeats(false);
 			}
 		}
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		boolean setFull = !MAXIMIZED;
 
 		if (setFull != MAXIMIZED) {
			final JFrame jf = (JFrame) Application.get().getWindowManager().getContainer(w);
 			// stop processing window events now
 			PROCESS_WINDOW_EVENTS = false;
 			final boolean wasVisible = jf.isVisible();
 
 			jf.setVisible(false);
 			jf.dispose();
 
 			if (setFull) {
 				Toolkit.getDefaultToolkit().addAWTEventListener(
 						scheduleHideMouse, AWTEvent.MOUSE_MOTION_EVENT_MASK);
 				timeToHideMouse.start();
 				jf.getBounds(applicationPosition);
 				
 				Insets i = java.awt.Toolkit
 				.getDefaultToolkit().getScreenInsets(jf.getGraphicsConfiguration());
 		        Rectangle max = new Rectangle(java.awt.Toolkit
 						.getDefaultToolkit().getScreenSize());
 		        max.x += i.left;
 		        max.y += i.top;
 		        max.width -= (i.left + i.right);
 		        max.height -= (i.top + i.bottom);
 				
 				jf.setBounds(max);
 			} else {
 				Toolkit.getDefaultToolkit().removeAWTEventListener(
 						scheduleHideMouse);
 				timeToHideMouse.stop();
 				jf.setCursor(Cursor.getDefaultCursor());
 				jf.setBounds(applicationPosition);
 			}
 			//jf.setAlwaysOnTop(setFull);
 			jf.setUndecorated(setFull);
 			jf.setResizable(!setFull);
 			jf.validate();
 			jf.setVisible(wasVisible);
 			MAXIMIZED = setFull;
 			// the events posted here are in front of
 			// this control event
 			EventQueue.invokeLater(new Runnable() {
 				public void run() {
 					PROCESS_WINDOW_EVENTS = true;
 					jf.repaint();
 				}
 			});
 		}
 	}
 
 }
