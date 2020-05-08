 package Core;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseMotionListener;
 
 import javax.swing.JPanel;
 import Items.Item;
 
 public class Canvas extends JPanel implements KeyListener {
 	private GUI gui;
 	private Player player;
 
 	public final int SCREEN_WIDTH = this.getWidth();
 	public final int SCREEN_HEIGHT = this.getHeight();
 	
 	private int pressLeft = KeyEvent.VK_LEFT;
 	private int pressRight = KeyEvent.VK_RIGHT;
 	private int pressUP = KeyEvent.VK_UP;
 	private int pressDown = KeyEvent.VK_DOWN;
 
 	private MainGame main;
 
 	public Canvas(GUI parent) {
 		gui = parent;
 		this.setSize(parent.getWidth(), parent.getHeight());
 		this.setBackground(Color.white);
 		this.addMouseMotionListener(new MouseMotionListener() {
 
 			@Override
 			public void mouseMoved(MouseEvent e) {
 				canvasMouseMoved(e);
 			}
 
 			@Override
 			public void mouseDragged(MouseEvent e) {
 			}
 		});
 		this.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent e) {
 				canvasMouseClicked(e);
 			}
 		});
 	}
 
 	// basic logic for movement:
 	// they try to move in a direction, compute what their bounding box
 	// would be and whether that box would be colliding with a wall.
 	// if it wouldn't collide, then you move player and check if they're
 	// now touching an item.
 	private void canvasKeyPressed(KeyEvent e) {

 		int STEP_SIZE = player.STEP_SIZE;
 
 			if (e.getKeyCode() == pressDown) {
 
 				if (!main.checkWallCollision(0, STEP_SIZE)) {
 					main.moveDown();
 					main.checkItemCollision();
 				}
 
 			} else if (e.getKeyCode() == pressLeft) {
 
 				if (!main.checkWallCollision(-STEP_SIZE, 0)) {
 					main.moveLeft();
 					main.checkItemCollision();
 				}
 
 			} else if (e.getKeyCode() == pressUP) {
 
 				if (!main.checkWallCollision(0, -STEP_SIZE)) {
 					main.moveUp();
 					main.checkItemCollision();
 				}
 			} else if (e.getKeyCode() == pressRight) {
 
 				if (!main.checkWallCollision(STEP_SIZE, 0)) {
 					main.moveRight();
 					main.checkItemCollision();
 				}
 			}
 
 			gui.repaint();
 	}
 
 	private void canvasMouseMoved(MouseEvent e) {
 		gui.repaint();
 	}
 
 	private void canvasMouseClicked(MouseEvent e) {
 		gui.repaint();
 	}
 
 	public void paint(Graphics g) {
 		super.paint(g);
 	}
 
 	@Override
 	public void keyPressed(KeyEvent e) {
 		canvasKeyPressed(e);
 	}
 
 	@Override
 	public void keyReleased(KeyEvent e) {
 		// TODO Auto-generated method stub
 	}
 
 	@Override
 	public void keyTyped(KeyEvent e) {
 		// TODO Auto-generated method stub
 	}
 
 	/**
 	 * @return the pressLeft
 	 */
 	public int getPressLeft() {
 		return pressLeft;
 	}
 
 	/**
 	 * @param pressLeft
 	 *            the pressLeft to set
 	 */
 	public void setPressLeft(int pressLeft) {
 		this.pressLeft = pressLeft;
 	}
 
 	/**
 	 * @return the pressRight
 	 */
 	public int getPressRight() {
 		return pressRight;
 	}
 
 	/**
 	 * @param pressRight
 	 *            the pressRight to set
 	 */
 	public void setPressRight(int pressRight) {
 		this.pressRight = pressRight;
 	}
 
 	/**
 	 * @return the pressUP
 	 */
 	public int getPressUP() {
 		return pressUP;
 	}
 
 	/**
 	 * @param pressUP
 	 *            the pressUP to set
 	 */
 	public void setPressUP(int pressUP) {
 		this.pressUP = pressUP;
 	}
 
 	/**
 	 * @return the pressDown
 	 */
 	public int getPressDown() {
 		return pressDown;
 	}
 
 	/**
 	 * @param pressDown
 	 *            the pressDown to set
 	 */
 	public void setPressDown(int pressDown) {
 		this.pressDown = pressDown;
 	}
 
 }
