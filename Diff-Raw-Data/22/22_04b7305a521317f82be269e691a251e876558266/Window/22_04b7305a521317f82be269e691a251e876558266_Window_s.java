 package UI;
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Point;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseWheelEvent;
 import java.awt.event.MouseWheelListener;
 import java.awt.image.RescaleOp;
 import java.awt.event.MouseMotionListener;
 import java.io.File;
 import java.util.Random;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 
 
 import javax.swing.SwingUtilities;
 
 import menu.AudioPlayer;
 import networking.common.Network;
 
 import logic.FileReader;
 import logic.UpdateThread;
 import state.Structure;
 import state.Tile;
 import state.World;
 import util.UIImageStorage;
 
 //TODO Rotate the view by inverting the draw
 //TODO Hovering over the screen will show a tempory bit on the screen
 
 
 @SuppressWarnings("serial")
 public class Window extends JPanel implements KeyListener, MouseListener, MouseMotionListener, MouseWheelListener {
 
 	//mouse x y points on a click
 	private int mouseX = 0;
 	private int mouseY = 0;
 
 	//used for buttons being pushed (direction keys)
 	boolean up = false;
 	boolean down = false;
 	boolean left = false;
 	boolean right = false;
 
 	private boolean drawTransparent = false;
 
 	Random random = new Random();
 
 	Display display;
 	UpdateThread update;
 
 	public long seed;
 	public Network network;
 	public String fileMap= "resources/map";
 
 	public Window(Thread thread) {
 		startAudio(thread);
 		initialize();
 	}
 
 	/**
 	 *
 	 * @param seed
 	 * @param network
 	 * @param fileMap
 	 */
 	public Window(long seed, Network network, String fileMap, Thread thread) {//TODO //mapfile tpye?
 
 
 		startAudio(thread);
 
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
 		addMouseMotionListener(this);
 		addMouseWheelListener(this);
 		addKeyListener(this);
 		setFocusable(true);
 
 		this.setLayout(new BorderLayout());
 		this.add(display, BorderLayout.CENTER);
         update = new UpdateThread(world, display);
         update.start();
 
 
         UIImageStorage.add("HealthBarsToggle");
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
 			switch(display.getRotation()) {
 			case 0: display.panUp(1); break;
 			case 1: display.panLeft(1); break;
 			case 2: display.panDown(1); break;
 			case 3: display.panRight(1); break;
 			}
 		if (down)
 			switch(display.getRotation()) {
 			case 2: display.panUp(1); break;
 			case 3: display.panLeft(1); break;
 			case 0: display.panDown(1); break;
 			case 1: display.panRight(1); break;
 			}
 		if (right)
 			switch(display.getRotation()) {
 			case 1: display.panUp(1); break;
 			case 2: display.panLeft(1); break;
 			case 3: display.panDown(1); break;
 			case 0: display.panRight(1); break;
 			}
 		if (left)
 			switch(display.getRotation()) {
 			case 3: display.panUp(1); break;
 			case 0: display.panLeft(1); break;
 			case 1: display.panDown(1); break;
 			case 2: display.panRight(1); break;
 			}
 	}
 
 	public static void main(String[] args) {
 		JFrame f = new JFrame("test");
 		f.getContentPane().add(new Window(null)) ;
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
 		case KeyEvent.VK_R:
 			display.rotate();
 			break;
 		}
 	}
 
 	// mouse commands, awaiting some level of world to play with
 	@Override
 
 	public void mousePressed(MouseEvent e) {
 		Point point = display.displayToTileCoordinates(e.getX(), e.getY());
 		if(0 == (e.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK)) {
 
 			//set tile to be somthing
 			if(e.getButton()==3){
 				//Dude d = new Dude("")
 				Tile t = new Tile("Grass",0, (int)point.getX(), (int)point.getY());
 				display.getWorld().setTile((int)point.getX(), (int)point.getY(), t);
 			}else if (drawTransparent == true){
 				Structure s = new Structure((int)point.getX(), (int)point.getY(), 1, 1, "Assets/EnvironmentTiles/BarrenWall.png");
 				/* Copied from Java tutorial.
 				 * Create a rescale filter op that makes the image
 				 * 50% opaque.
 				 */
 				float[] scales = { 1f, 1f, 1f, 0.5f };
 				float[] offsets = new float[4];
 				RescaleOp rop = new RescaleOp(scales, offsets, null);
 				s.setFilter(rop);
 
 				display.getWorld().addStructure(s);
 			}
 			else{
 				Tile w = new Tile("BarrenWall", 0, (int)point.getX(), (int)point.getY());
 				display.getWorld().setTile((int)point.getX(), (int)point.getY(), w);
 			}
 
 		} else {
 			Tile t = display.getWorld().getTile(point.x, point.y);
 			if(0 != (e.getModifiersEx() & MouseEvent.SHIFT_DOWN_MASK)) {
 				switch(e.getButton()) {
 				case 1: t.setImage("BarrenGrass"); break;
 				case 2: t.setImage("DarkSand"); break;
 				case 3: t.setImage("BarrenWall"); break;
 				}
 			} else {
 				switch(e.getButton()) {
 				case 3: t.setHeight(t.getHeight() - 1); break;
 				case 1: t.setHeight(t.getHeight() + 1); break;
 				}
 			}
 		}
 
 		this.repaint();
 		//
 
 	}
 
 	@Override
 	public void mouseWheelMoved(MouseWheelEvent e) {
 		Point point = display.displayToTileCoordinates(e.getX(), e.getY());
 		Tile t = display.getWorld().getTile(point.x, point.y);
 		t.setHeight(t.getHeight() + e.getWheelRotation());
 	}
 
 
 
 	@Override
 	public void mouseClicked(MouseEvent e) {
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
 
 	@Override
 	public void mouseDragged(MouseEvent e) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void mouseMoved(MouseEvent e) {
 
 
 		Point tilePt = display.displayToTileCoordinates(e.getX(), e.getY());
 
 		display.setHighlightedTile(tilePt.x, tilePt.y);
 	}
 
 	public void startAudio(Thread thread){
//		thread.stop();//TODO THIS CAUSES NULL POINTER EXCEPTIONS
		thread.interrupt();
		new Thread(
	            new Runnable() {
	                public void run() {
	                    try {
	                    	new AudioPlayer("TempInGameSong.wav", false);
	                        // PLAY AUDIO CODE
	                    } catch (Exception e) {
	                        e.printStackTrace();
	                    }
	                }
	            }).start();
 
 	}
 
 }
