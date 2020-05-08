 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Point;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.util.Random;
 
 import javax.swing.JComponent;
 import javax.swing.JFrame;
 import javax.swing.SwingUtilities;
 
 import state.Tile;
 
 
 
 
 @SuppressWarnings("serial")
 public class Window extends JFrame implements KeyListener, MouseListener {
 
 
 	private int mouse_X = 0;
 	private int mouse_Y = 0;
 
 	boolean up 			= false;
 	boolean down 		= false;
 	boolean left 		= false;
 	boolean right 		= false;
 
 	Random random = new Random();
 
 	JComponent drawing;
 	Display display;
 
 	public Window(){
 		this.setSize(1900, 1080 );
 		initialize();
 		this.setVisible(true);
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 	}
 
 	public String generateRandomTile(){
 		if(random.nextInt(2)==1)
 			return "tile";
 
 			else
 				return "tile0";
 
 	}
 
 	public void initialize(){
 
 
 
 		Tile[][] map = new Tile[200][200];
 
 
 		for(int i = 0; i < 200; i++){
 			for(int j = 0; j < 200; j++){
 				map[i][j] = new Tile(generateRandomTile());
 			}
 		}
 
 
 		// set up menu
 		TopMenu menu =new TopMenu();
 		setJMenuBar(menu);
 		display = new Display(map);
 
 		drawing = new JComponent() {
 			protected void paintComponent(Graphics g) {
 				redraw(g);
 			}
 		};
 
 		addMouseListener(this);
 		addKeyListener(this);
 		setFocusable(true);
 
 		add(drawing);
 		drawing.repaint();
 
 	}
 
 //Draws a basic graphic pane needs actual graphical outlines and suchlike
 	private void redraw(Graphics g) {
 
 		add(display);
 		//display.repaint();
 		g.setColor(Color.BLACK);
 		//Bottom pane
 		g.fillRect(0,getHeight()-(getHeight()/4),getWidth(),getHeight()/4);
 		//left hand pane
 		g.fillRect(0, 0, 25, getHeight()-(getHeight()/4));
 		//right hand pane
 		g.fillRect(getWidth() - 25, 0, 50, getHeight()-(getHeight()/4));
 		g.fillRect(25, 0, getWidth(), 25);
 		g.fillOval(mouse_X - 10, mouse_Y - 20, 20, 20);
 
 	}
 
 
 	private void panMap(){
 		if(up)
 			display.panUp(1);
 		if(down)
 			display.panDown(1);
 		if(right)
 			display.panRight(1);
 		if(left)
 			display.panLeft(1);
 	}
 
 
 	public static void main(String[] args) {
 		new Window();
 	}
 
 
 	@Override
 	public void keyTyped(KeyEvent e) {
 
 	}
 	//gets key events for panning possibly add shortcuts
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
 		//display.repaint();
 	}
 	//disables a given pan direction
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
 	//mouse commands, awaiting some level of world to play with
 	@Override
 	public void mouseClicked(MouseEvent e) {
 		Point p = e.getPoint();
 		SwingUtilities.convertPointFromScreen(p,display);
 		mouse_X = p.x;
 		mouse_Y = p.y;
 
 		mouse_X = e.getPoint().x;
 		mouse_Y = e.getPoint().y;
 
 		drawing.repaint();
 
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
 
 
 
 
