 import java.awt.AWTException;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Robot;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 
 import javax.swing.JPanel;
 
 public class CanvasPanel extends JPanel implements KeyListener,
 		MouseMotionListener,MouseListener {
 
 	private static final long serialVersionUID = 8187920910989915064L;
 	private Cursor cursor;
 	private Map map;
 	private Camera cam;
 	private Robot mouseController;
 
 	private boolean leftKeyPressed;
 	private boolean rightKeyPressed;
 	private boolean upKeyPressed;
 	private int previousMouseX;
 	private int previousMouseY;
 	// private boolean downKeyPressed; for later when droppng through blocks
 
 	private static final int screenWidth = 1024;
 	private static final int screenHeight = 768;
 
 	private static final int blockSize = 32;
 
 	public CanvasPanel(Map m) {
 		setPreferredSize(new Dimension(1024, 768));
 		
 		try {
 			mouseController = new Robot();
 			
 		} catch (AWTException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		this.map = m;
 		cursor = new Cursor(screenWidth / 2 + blockSize / 2, screenHeight / 2
 				+ blockSize / 2, blockSize, blockSize, map.getCharacter());
 		this.cam = new Camera(map.getCharacter(), cursor);
 		addKeyListener(this);
 		addMouseMotionListener(this);
 		addMouseListener(this);
 		
 	}
 
 	public void paint(Graphics g) {
 		g.clearRect(0, 0, this.getWidth(), this.getHeight());//Change to image
 		for (int i = 0; i < map.getMap().length; i++) {
 			for (int j = 0; j < map.getMap()[0].length; j++) {
 				Tile tempTile = map.getMap()[i][j];
 				
 				g.drawImage(tempTile.getImageWrapper().getImage(), tempTile.getX()-cam.getX(), tempTile.getY()-cam.getY(), null);
 				
 			}
 		}
 		
 		for(MoveTile t : map.movingTiles())
 			g.drawImage(t.getImageWrapper().getImage(), t.getX()-cam.getX(), t.getY()-cam.getY(), null);
 		map.getCharacter().getImage().draw(g, map.getCharacter().getX()-cam.getX(), map.getCharacter().getY()-cam.getY(), map.getCharacter().getWidth(), map.getCharacter().getHeight());
 		cursor.draw(g);
 	}
 
 	@Override
 	public void keyTyped(KeyEvent e) {
 	}
 
 	@Override
 	public void keyPressed(KeyEvent e) {
 		if (e.getKeyCode() == KeyEvent.VK_LEFT
 				|| e.getKeyCode() == KeyEvent.VK_A) {
 			leftKeyPressed = true;
 		} else if (e.getKeyCode() == KeyEvent.VK_RIGHT
 				|| e.getKeyCode() == KeyEvent.VK_D) {
 			rightKeyPressed = true;
 		} else if (e.getKeyCode() == KeyEvent.VK_UP
 				|| e.getKeyCode() == KeyEvent.VK_W) {
 			upKeyPressed = true;
 		}
 		/*
 		 * else if(e.getKeyCode()==KeyEvent.VK_DOWN ||
 		 * e.getKeyCode()==KeyEvent.VK_A) { downKeyPressed = true; }
 		 */
 	}
 
 	@Override
 	public void keyReleased(KeyEvent e) {
 		if    (e.getKeyCode() == KeyEvent.VK_LEFT
 			|| e.getKeyCode() == KeyEvent.VK_A) {
 			leftKeyPressed = false;
 		} else if (e.getKeyCode() == KeyEvent.VK_RIGHT
 				|| e.getKeyCode() == KeyEvent.VK_D) {
 			rightKeyPressed = false;
 
 		} else if (e.getKeyCode() == KeyEvent.VK_UP
 				|| e.getKeyCode() == KeyEvent.VK_W) {
 			upKeyPressed = false;
 		}else if(e.getKeyCode()== KeyEvent.VK_ESCAPE){
 			System.exit(0);
 			
 		}
 		/*
 		 * else if(e.getKeyCode()==KeyEvent.VK_DOWN ||
 		 * e.getKeyCode()==KeyEvent.VK_A) { downKeyPressed = false; }
 		 */
 
 	}
 
 	public void movecharacter() {
 
 		if (leftKeyPressed) {
 			map.getCharacter().walk(false);
 		}
 		if (rightKeyPressed) {
 			map.getCharacter().walk(true);
 		}
 		if (upKeyPressed) {
 			map.getCharacter().jump();
 		}
 		/*
 		 * if(downKeyPressed2) {
 		 * 
 		 * }
 		 */
 		
 		cam.setX(map.getCharacter().getX());
 		cam.setY(map.getCharacter().getY());
 	}
 
 	@Override
 	public void mouseDragged(MouseEvent e) {
 		
 	//	cam.setX(e.getX());
 	//	cam.setY(e.getY());
 	//	previousMouseX = e.getX();
 	//	previousMouseY = e.getY();
		//cursor.moveCursor(e.getX(), e.getY(), map.getCharacter().getX(),map.getCharacter().getY());
 
 		requestFocusInWindow();
 	}
 
 	@Override
 	public void mouseMoved(MouseEvent e) {
 		
 		
		cam.setX(map.getCharacter().getX());
		cam.setY(map.getCharacter().getY());
 		previousMouseX = e.getX();
 		previousMouseY = e.getY();
 		
		
		//cursor.moveCursor(e.getX(), e.getY(), map.getCharacter().getX()-cam.getX(),map.getCharacter().getY()-cam.getY());
 		
 		
 		requestFocusInWindow();
 	}
 
 	@Override
 	public void mouseClicked(MouseEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void mousePressed(MouseEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void mouseEntered(MouseEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void mouseExited(MouseEvent e) {
 		// TODO Auto-generated method stub
 		mouseController.mouseMove(previousMouseX, previousMouseY);
 		System.out.println("Executed Mouse exited");
 		
 		
 	}
 }
