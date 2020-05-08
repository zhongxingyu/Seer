 import java.applet.Applet;
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.util.LinkedList;
 import java.util.List;
 import netscape.javascript.JSObject;
 
 /**
  * Java Applet which access the keystrokes times directly from the OS data.
  * @author Victor Hatinguais, www.victorhatinguais.fr
  */
 @SuppressWarnings("serial")
 public class KeyboardApplet extends Applet implements KeyListener {
 
 	long first = 0;
 	List<Long> pressed = new LinkedList<Long>();
 	List<Long> released = new LinkedList<Long>();
 	List<Character> characters = new LinkedList<Character>();
 	String string = new String();
 	//List<Long> typed = new LinkedList<Long>();
 	boolean newData = false;
 
 	/**
 	 * Initialize the Applet.
 	 */
 	public void init() {
 		setBackground(Color.lightGray);
 		addKeyListener(this);
 	}
 
 	/**
 	 * Action which is done on key press event.
 	 */
 	public void keyPressed(KeyEvent e) {
 		int code = e.getKeyCode();
 		if ((code >= KeyEvent.VK_A && code <= KeyEvent.VK_Z)
 				|| code == KeyEvent.VK_SPACE) {
 			char c = e.getKeyChar();
 			long tsp = e.getWhen();
 			if (first == 0)
 				first = tsp;
 			pressed.add(tsp - first);
 			characters.add(c);
 			string = string + c;
 			this.callJSCharacter(Character.toString(c));
 		}
 	}
 
 	/**
 	 * Action which is done on key release event.
 	 */
 	public void keyReleased(KeyEvent e) {
 		if (pressed.size() != 0) {
 			int code = e.getKeyCode();
 			if ((code >= KeyEvent.VK_A && code <= KeyEvent.VK_Z)
 					|| code == KeyEvent.VK_SPACE) {
 				long tsp = e.getWhen();
 				released.add(tsp - first);
 			}
 			else {
 				System.out.println("chars    " + characters);
 				//System.out.println("typed    " + typed);
 				System.out.println("pressed  " + pressed);
 				System.out.println("released " + released);
 				newData = true;
 				repaint();
 			}
 		}
 	}
 
 	/**
 	 * Action which is done on key typed event.
 	 */
 	public void keyTyped(KeyEvent e) {	}
 
 	/**
 	 * Call the JavaScript function <appletCallback> which has to be defined
 	 * on the web page which display the Applet.
 	 * Send the entire word or passphrase or typing sequence to the JS.
 	 */
 	public void callJSString() {
 		try {
 			JSObject window = JSObject.getWindow(this);
 			window.call("appletCallback",
 					new String[] {string + ";" + pressed.toString() + ";"
 							+ released.toString()});
 		} catch (Exception e) {
 			//e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Call the JavaScript function <appletCallbackChar> which has to be defined
 	 * on the web page which display the Applet.
 	 * Send the last typed character to the JS.
 	 */
 	public void callJSCharacter(String c) {
 		try {
 			JSObject window = JSObject.getWindow(this);
 			window.call("appletCallbackChar",
 					new String[] {c});
 		} catch (Exception e) {
 			//e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Define what is displayed on the Applet.
 	 */
 	public void paint(Graphics g) {
 		if (newData) {
 			g.setColor(Color.black);
 			g.drawString("chars    " + characters.toString(), 5, 15);
 			//g.drawString("typed    " + typed.toString(), 5, 30);
 			g.drawString("pressed  " + pressed.toString(), 5, 30);
 			g.drawString("released " + released.toString(), 5, 45);
 			this.callJSString();
 			first = 0;
 			characters.clear();
 			string = "";
 			//typed.clear();
 			pressed.clear();
 			released.clear();
 			newData = false;
 		}
 	}
 }
