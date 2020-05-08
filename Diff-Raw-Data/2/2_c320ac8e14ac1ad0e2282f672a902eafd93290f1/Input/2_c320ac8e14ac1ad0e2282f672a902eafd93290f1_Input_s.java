 /*
 // Good enough for now
 */
 import java.awt.event.KeyListener;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.util.Arrays;
 
 public class Input implements KeyListener{
 	public boolean[] buttons1 = new boolean[5];
 	/*
 	0 = up
 	1 = down
 	2 = left
 	3 = right
 	4 = space
 	*/
 
 	//constructor
 
 	public Input(){
 
 	}
 	//public void startUpdate(){}
 	//public void finishUpdate(){
 	//	Arrays.fill(buttons1, false);
 	//}
 
 	//key class, do not rename/remove
 	public void keyPressed(KeyEvent e) {
 		if (e.getKeyCode() == KeyEvent.VK_UP) {
 			buttons1[0] = true;
 		}
 		if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                 	buttons1[1] = true;
 		}
 		if (e.getKeyCode() == KeyEvent.VK_LEFT) {
 			buttons1[2] = true;
 		}
 		if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
 			buttons1[3] = true;
 		}
 		if (e.getKeyChar() == KeyEvent.VK_SPACE) {
 			buttons1[4] = true;
 		}
 	}
 
 	//key class, do not rename/remove
 	public void keyReleased(KeyEvent e) {
 		if (e.getKeyCode() == KeyEvent.VK_UP) {
 			buttons1[0] = false;
 		}
 		if (e.getKeyCode() == KeyEvent.VK_DOWN) {
 			buttons1[1] = false;
 		}
 		if (e.getKeyCode() == KeyEvent.VK_LEFT) {
 			buttons1[2] = false;
 		}
 		if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
			buttons1[4] = false;
 		}
 		if (e.getKeyChar() == KeyEvent.VK_SPACE) {
 			buttons1[4] = false;
 		}
 	}
 	//key class, do not rename/remove
 	public void keyTyped(KeyEvent e) {
 		if (e.getKeyChar() == 27) {
 			System.exit(0);
 		}
 	}
 };
