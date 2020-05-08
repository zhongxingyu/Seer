 package kkckkc.jsourcepad.util.ui;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.*;
 
 public class PopupUtils {
     public static void bind(final JPopupMenu jp, final Component component, final boolean allButtons, final PopupListener listener) {
 		component.addMouseListener(new MouseAdapter() {
 			public void mousePressed(MouseEvent e) {
 				maybeShowPopup(e);
 			}
 
 			public void mouseReleased(MouseEvent e) {
 				maybeShowPopup(e);
 			}
 
 			private void maybeShowPopup(MouseEvent e) {
 				if (allButtons || e.isPopupTrigger()) {
                     if (listener != null) {
                         listener.show(e);
                     }
                     jp.show(component, (int) e.getPoint().getX(), (int) e.getPoint().getY());
 				}
 			}
 		});
     }
 
     public static void bind(final JPopupMenu jp, final Component component, final boolean allButtons) {
         bind(jp, component, allButtons, null);
         component.addKeyListener(new KeyListener() {
             @Override
             public void keyTyped(KeyEvent e) {
             }
 
             @Override
             public void keyPressed(KeyEvent e) {
                 processKeyEvent(e, jp);
             }
 
             private boolean processKeyEvent(KeyEvent ke, MenuElement element) {
                 for (MenuElement me : element.getSubElements()) {
                     if (processKeyEvent(ke, me)) return true;
                 }
 
                 if (element instanceof JMenuItem) {
                     JMenuItem menuItem = (JMenuItem) element;
                     if (menuItem.isEnabled() && menuItem.getAccelerator() != null && KeyStrokeUtils.matches(menuItem.getAccelerator(), ke)) {
                         menuItem.getAction().actionPerformed(new ActionEvent(component, 0, null));
                         return true;
                     }
                 }
 
                 return false;
             }
 
             @Override
             public void keyReleased(KeyEvent e) {
             }
         });
     }
 
     public interface PopupListener {
         public void show(MouseEvent e);
     }
 }
