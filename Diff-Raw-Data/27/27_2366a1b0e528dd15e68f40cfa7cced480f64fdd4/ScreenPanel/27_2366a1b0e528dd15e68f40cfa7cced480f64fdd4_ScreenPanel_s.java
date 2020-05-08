 package Screen;
 
 import java.util.*;
 import javax.swing.*;
 import javax.swing.Timer;
 import java.awt.*;
 import java.awt.event.*;
 import cells.Map;
 import pxlJam.*;
 import cells.*;
 
 @SuppressWarnings("serial")
 public class ScreenPanel extends JPanel implements ActionListener, KeyListener {
 	
 	Graphics2D bufferGraphics;
 	Map theMap = new Map("levels/level3.txt");
 	Camera cam;
 	int xorigin, yorigin;
 	long lastTime = System.currentTimeMillis();
 	static long WAIT = 1000/30;
 	
 	
 	HeadsUp headUp = new HeadsUp();
 
 	public ScreenPanel() {
 		setFocusable(true);
 		requestFocusInWindow();
 		MListener mlist = new MListener();
 		addMouseListener(mlist);
 		addMouseMotionListener(mlist);
 		addKeyListener(this);
 		
 		cam = new Camera(theMap.getCharacter().getX(), theMap.getCharacter().getY());
 	}
 
 	public void paintComponent(Graphics g) {
 		requestFocusInWindow();
 		Graphics2D g2d = (Graphics2D) g;
 		Image offscreen = createImage(getWidth(), getHeight());
 		bufferGraphics = (Graphics2D) offscreen.getGraphics();
 		bufferGraphics.setColor(Color.white);
 		bufferGraphics.fillRect(0, 0, getWidth(), getHeight());
 		
 		gameStuff();
 		g2d.drawImage(offscreen, 0, 0, this);
 	}
 	
 	private void gameStuff(){
 		cam.setCamera(theMap.getCharacter());
 		bufferGraphics.translate(-cam.getX(), -cam.getY());
 		theMap.draw(bufferGraphics);
 		theMap.getCharacter().move(theMap.getTiles());
 		bufferGraphics.translate(cam.getX(), cam.getY());
 		headUp.draw(bufferGraphics);
 	}
 
 	public void drawIntroScreen(Graphics2D g2d) {
 
 	}
 
 	public void drawBalance(Graphics2D bf) {
 
 	}
 
 	/** Respond to the buttons */
 
 	public void drawButtons(Graphics2D bf) {
 
 	}
 
 	/** Create a new order and put it on the queue to be processed */
 	public void generateOrder() {
 
 	}
 
 	public void addItem(String item) {
 
 	}
 
 	public void deliverOrder(Graphics2D bf) {
 
 	}
 
 	public void newLevel(Graphics2D bf) {
 
 	}
 
 	public void drawOrders(Graphics bf) {
 
 	}
 
 	public void rotateOrders(Graphics bf) {
 
 	}
 
 	public void startTimer(int time) {
 
 	}
 
 	public void bonusTimer(int time) {
 
 	}
 
 	public void run() {
 		while (true){
 			long current = System.currentTimeMillis();
 			if (current - lastTime > WAIT) {
 				repaint();
 				lastTime = current;
 			}
 			
 		}
 	}
 
 	public void sleep(double millis) {
 		try {
 			Thread.sleep((long) millis);
 		} catch (InterruptedException e) {
 		}
 	}
 
 	private class MListener implements MouseListener, MouseMotionListener {
 		int xChange, yChange;
 
 		public void mousePressed(MouseEvent event) {
			double x = event.getX();
			double y = event.getY();
 			int xPos = (int) (x / Cell.CELL_WIDTH);
 			int yPos = (int) (y / Cell.CELL_HEIGHT);
 			if (xPos > 0 && xPos < theMap.getHeight() && yPos > 0 && yPos < theMap.getWidth()){
 				xorigin = xPos;
 				yorigin = yPos;
 				theMap.setCell(yPos, xPos, new DrawnWall(xPos * Cell.CELL_WIDTH, yPos * Cell.CELL_HEIGHT,2000,theMap));
 			}
 		}
 
 		public void mouseDragged(MouseEvent event) {
			double x = event.getX();
			double y = event.getY();
 			int xPos = (int) (x / Cell.CELL_WIDTH);
 			int yPos = (int) (y / Cell.CELL_HEIGHT);
 			if (xPos > 0 && xPos < theMap.getHeight() && yPos > 0 && yPos < theMap.getWidth()){
 				double xDistance = xorigin - xPos;
 				double yDistance = yorigin - yPos;
 				double xInc = xDistance / 50; 
 				double yInc = yDistance / 50;
 				double xincrement = 0, yincrement=0;
 				while(Math.abs((xPos+xincrement) - xorigin) > 0.25 || Math.abs((yPos+yincrement) - yorigin) > 0.25){
 					if ((int)(xPos+(int)xincrement) > 0 && (int)(xPos+(int)xincrement) < theMap.getHeight() && (int)(yPos+(int)yincrement) > 0 && (int)(yPos+(int)yincrement) < theMap.getWidth()){
 					theMap.setCell((int)(yPos+(int)yincrement),(int)(xPos+(int)xincrement), new DrawnWall((int)((xPos+(int)xincrement)*Cell.CELL_WIDTH), (int)((yPos+(int)yincrement)*Cell.CELL_HEIGHT),2000,theMap));
 					}
 					xincrement+=xInc;
 					yincrement+=yInc;
 				}
 				xorigin = xPos;
 				yorigin = yPos;
 			}
 		}
 
 		public void mouseReleased(MouseEvent event) {
 
 		}
 
 		public void mouseClicked(MouseEvent event) {
 
 		}
 
 		public void mouseEntered(MouseEvent event) {
 
 		}
 
 		public void mouseExited(MouseEvent event) {
 
 		}
 
 		public void mouseMoved(MouseEvent event) {
 
 		}
 
 	}
 
 	/** Handle the key typed event from the text field. */
 	public void keyTyped(KeyEvent e) {
 
 	}
 
 	/** Handle the key-pressed event from the text field. */
 	public void keyPressed(KeyEvent e) {
 		if (e.getKeyChar() == 'a' || e.getKeyChar() == 'A') {
 			theMap.getCharacter().setDirection(-1);
 		} else if (e.getKeyChar() == 'd' || e.getKeyChar() == 'D') {
 			theMap.getCharacter().setDirection(1);
 		} else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
 			System.exit(0);
 		} else if (e.getKeyCode() == KeyEvent.VK_W){
 			theMap.getCharacter().jump();
 		}
 
 	}
 
 	/** Handle the key-released event from the text field. */
 	public void keyReleased(KeyEvent e) {
 			if (e.getKeyChar() == 'a' || e.getKeyChar() == 'A') {
 				theMap.getCharacter().setDirection(0);
 			} else if (e.getKeyChar() == 'd' || e.getKeyChar() == 'D') {
 				theMap.getCharacter().setDirection(0);
 			}
 			theMap.getCharacter().setDirection(0);
 	}
 	
 	public boolean isPressed(int kc) {
 		if (KeyEvent.VK_A == (char)kc) {
 			theMap.getCharacter().setDirection(-1);
 			return true;
 		} else if (KeyEvent.VK_D == (char)kc) {
 			theMap.getCharacter().setDirection(1);
 			return true;
 		} else if (KeyEvent.VK_ESCAPE == (char)kc) {
 			System.exit(0);
 			return true;
 		} else if (KeyEvent.VK_W == (char)kc){
 			theMap.getCharacter().jump();
 	
 			return true;
 		}
 		return false;
 	}
 
 	public void actionPerformed(ActionEvent e) {
 		run();
 	}
 }
