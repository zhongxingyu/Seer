 package UI;
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Point;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.io.File;
 import java.util.Random;
 
 import javax.swing.JComponent;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 
 import javax.swing.SwingUtilities;
 
 import networking.common.Network;
 
 
 import logic.FileReader;
 
 import logic.Logic;
 
 import logic.UpdateThread;
 
 import state.Tile;
 import state.World;
 
 //TODO Rotate the view by inverting the draw
//TODO Hovering over the screen will show a tempory bit on the screen
 
 
 @SuppressWarnings("serial")
 public class Window extends JPanel implements KeyListener, MouseListener {
 
 	//mouse x y points on a click
 	private int mouseX = 0;
 	private int mouseY = 0;
 
 	//used for buttons being pushed (direction keys)
 	boolean up = false;
 	boolean down = false;
 	boolean left = false;
 	boolean right = false;
 
 	Random random = new Random();
 
 	Display display;
 	UpdateThread update;
 
 	public long seed;
 	public Network network;
	public String fileMap= "resources/map";
 
 	public Window() {
 //		this.setSize(1920, 1080);
 		initialize();
 	}
 
 	/**
 	 *
 	 * @param seed
 	 * @param network
 	 * @param fileMap
 	 */
 	public Window(long seed, Network network, String fileMap) {//TODO //mapfile tpye?
 		this.seed = seed;
 		this.network = network;
 		fileMap = this.fileMap;
 
 		//TODO
 		//load map from file given
 		//store network as field
 		//use seed to generate any random events
 		initialize();
 	}
 
 
 	/**
 	 * Returns one of two random tiles.
 	 * @return String
 	 */
 	public String generateRandomTile() {
 		if (random.nextInt(2) == 1)
 			return "tile";
 		else
 			return "tile0";
 	}
 
 
 	public void initialize(){
 		//Was code to randomly generate a map.  Replaced now by reading a map from a file.
 		//Tile[][] map = FileReader.readMap("resources/map");
 
 
 //		for(int i = 0; i < 200; i++){
 //			for(int j = 0; j < 200; j++){
 //				map[i][j] = new Tile(generateRandomTile());
 //			}
 //		}
 
 
 		// set up menu
 
 
 		//Create a new world with the map read from the file.
 		World world = new World(FileReader.readMap(fileMap));//resources/map
 		display = new Display(world); // was just new World()
 		FileReader.setStructures(world); // Set up the structures that the file
 											// reader now knows about
 
 		addMouseListener(this);
 		addKeyListener(this);
 		setFocusable(true);
 
 		this.add(display);
         update = new UpdateThread(world, display);
         update.start();
 
 	}
 
 	/**
 	 * Draws a basic graphic pane needs actual graphical outlines and suchlike
 	 * -Outdated-
 	 * @param Graphics
 	 */
 	public void paint(Graphics g) {
 		super.paint(g);
 
 //		g.setColor(Color.BLACK);
 //		// Bottom pane
 //		g.fillRect(0, getHeight() - (getHeight() / 4), getWidth(),
 //				getHeight() / 4);
 //		// left hand pane
 //		g.fillRect(0, 0, 25, getHeight() - (getHeight() / 4));
 //		// right hand pane
 //		g.fillRect(getWidth() - 25, 0, 50, getHeight() - (getHeight() / 4));
 //		g.fillRect(25, 0, getWidth(), 25);
 //		g.setColor(Color.red);
 //
 //		g.fillOval(mouseX, mouseY, 20, 20);
 
 	}
 
 
 	private void panMap() {
 	// Pans the map by 1 tile but only while direction keys are currently being held down
 		if (up)
 			display.panUp(1);
 		if (down)
 			display.panDown(1);
 		if (right)
 			display.panRight(1);
 		if (left)
 			display.panLeft(1);
 	}
 
 	public static void main(String[] args) {
 		JFrame f = new JFrame("test");
 		f.getContentPane().add(new Window()) ;
 		//f.add(new Window());
 		f.setSize(1920, 1080);
 		f.pack();
 		f.setVisible(true);
 
 		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 	}
 
 	@Override
 	public void keyTyped(KeyEvent e) {
 
 	}
 
 	// gets key events for panning possibly add shortcuts
 	@Override
 	public void keyPressed(KeyEvent e) {
 		int code = e.getKeyCode();
 
 		switch (code) {
 		case KeyEvent.VK_RIGHT:
 		case KeyEvent.VK_KP_RIGHT:
 		case KeyEvent.VK_D:
 			right = true;
 			break;
 		case KeyEvent.VK_LEFT:
 		case KeyEvent.VK_KP_LEFT:
 		case KeyEvent.VK_A:
 			left = true;
 			break;
 		case KeyEvent.VK_UP:
 		case KeyEvent.VK_KP_UP:
 		case KeyEvent.VK_W:
 			up = true;
 			break;
 		case KeyEvent.VK_DOWN:
 		case KeyEvent.VK_KP_DOWN:
 		case KeyEvent.VK_S:
 			down = true;
 			break;
 		}
 		panMap();
 		repaint();
 		// display.repaint();
 	}
 
 	// disables a given pan direction
 	@Override
 	public void keyReleased(KeyEvent e) {
 
 		int code = e.getKeyCode();
 
 		switch (code) {
 		case KeyEvent.VK_RIGHT:
 		case KeyEvent.VK_KP_RIGHT:
 		case KeyEvent.VK_D:
 			right = false;
 			break;
 		case KeyEvent.VK_LEFT:
 		case KeyEvent.VK_KP_LEFT:
 		case KeyEvent.VK_A:
 			left = false;
 			break;
 		case KeyEvent.VK_UP:
 		case KeyEvent.VK_KP_UP:
 		case KeyEvent.VK_W:
 			up = false;
 			break;
 		case KeyEvent.VK_DOWN:
 		case KeyEvent.VK_KP_DOWN:
 		case KeyEvent.VK_S:
 			down = false;
 			break;
 		}
 	}
 
 	// mouse commands, awaiting some level of world to play with
 	@Override
 	public void mouseClicked(MouseEvent e) {
 		Point p = e.getPoint();
 		mouseX = p.x;
 		mouseY = p.y;
 
		mouseY += 490;
 

		double xMinusY = (mouseX - display.getWidth()/2) / (32.0); // ( x click - half width of screen )  / half the width of a tile
 		double xPlusY = (mouseY / 16.0);		  // ( y click  /  half height of tile )
 
 		int[] cameraPoint = display.getCameraCoordinates();
 
 		// finds interger value of square in array position and adjusts for
 		// where the camera is looking
 		int x = (int) ((xMinusY + xPlusY) / 2 - 0.5);
 		int y = (int) ((xPlusY - xMinusY) / 2 - 0.5);
 
 		// you are NOT off the map
//		if !(x < 0 || x > 29 || y < 0 || y > 29) {//TODO all wrong now
 			// invalid click
//		} else {
 			//Adjusts for the camera's possible location and sets the x/y acordingly
 			x = x + cameraPoint[0];
 			y = y + cameraPoint[1];
 
 			//set tile to be somthing
 			if(e.getButton()==3){
 				//Dude d = new Dude("")
 				Tile t = new Tile("DarkTree",0, x,y);
 				display.getWorld().setTile(x, y, t);
 			}else{
 				Tile t = new Tile("BarrenWall",1, x,y);
 				display.getWorld().setTile(x, y, t);
//			}
 		}
 		this.repaint();
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
 	}
 
 }
