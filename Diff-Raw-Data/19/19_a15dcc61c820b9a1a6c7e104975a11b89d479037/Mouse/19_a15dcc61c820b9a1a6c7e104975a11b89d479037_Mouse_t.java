 package jogo;
 
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 
public class Mouse implements MouseListener, MouseMotionListener {

	private static int mouseX = -1;
	private static int mouseY = -1;
 
 	public static int getX() {
 		return mouseX;
 	}
 
 	public static void setMouseX(int mouseX) {
 		Mouse.mouseX = mouseX;
 	}
 
 	public static int getY() {
 		return mouseY;
 	}
 
 	public static void setMouseY(int mouseY) {
 		Mouse.mouseY = mouseY;
 	}

 	public void mouseDragged(MouseEvent e) {
 	}
 
 	public void mouseMoved(MouseEvent e) {
 
 	}

 	public void mouseClicked(MouseEvent e) {

 	}
 
 	public void mouseEntered(MouseEvent e) {
 	}
 
 	public void mouseExited(MouseEvent e) {
 	}
 
 	public void mousePressed(MouseEvent e) {
 		mouseX = e.getX();
 		mouseY = e.getY();
 	}
 
 	public void mouseReleased(MouseEvent e) {
 	}
 
 }
