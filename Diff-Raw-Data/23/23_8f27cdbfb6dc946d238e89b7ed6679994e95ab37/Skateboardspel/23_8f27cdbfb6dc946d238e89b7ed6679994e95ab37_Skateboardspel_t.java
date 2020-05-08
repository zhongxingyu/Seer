 import java.awt.Canvas;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.image.BufferStrategy;
 import java.io.IOException;
 
 import javax.imageio.ImageIO;
 import javax.swing.JFrame;
 
 @SuppressWarnings("serial")
 // Varf�r varnar Eclipse f�r det h�r?
 public class Skateboardspel extends Canvas implements Runnable {
 
 	private static JFrame frame = new JFrame();
 	private static Dimension d;
 	private static Controller controller = new Controller();
 	private boolean running;
 	private World world;
 	private boolean gameRunning = false;
 	private static Skateboardspel game;
 	private boolean showScreen;
 	
 	public Skateboardspel() {
 		// Setting up listeners for mouse and keyboard
 		addKeyListener(controller);
 		// addMouseListener(controller);
 		// addMouseMotionListener(controller);
 	}
 
 	@Override
 	public void run() {
 		while(running) {
 			update();
 			render();
 
 			// Make this method not running to often
 			try {
 				Thread.sleep(10);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 				running = false;
 			}
 		}
 	}
 
 	public void start() {
 		if (!running) {
 			Thread t = new Thread(this);
 			createBufferStrategy(3);
 			running = true;
 			t.start();
 		}
 	}
 
 	private void render() {
 		if(gameRunning || showScreen) {
 			BufferStrategy strategy = getBufferStrategy();
 	
 			Graphics2D g = (Graphics2D) strategy.getDrawGraphics();
 			g.setColor(Color.WHITE);
 			g.fillRect(0, 0, getWidth(), getHeight());
 	
 			world.render(g);
 	
 			strategy.show();
 		}
 		
 	}
 
 	private void update() {
 		if(gameRunning)
 			world.update(controller);
 	}
 	
 	public void newGame() {
 		world = new World("background.JPG");
 		
 		// Setting window dimensions 
 		d = world.getCameraDimension();
 		frame.setPreferredSize(d);
 		frame.setMinimumSize(d);
 		frame.setMaximumSize(d);
 		frame.setLocationRelativeTo(null);
 		frame.setVisible(true);
 		
 		this.gameRunning  = true;
 	}
 	
 	public void showScreen(String screen) {
 		// Stop the game
 		this.gameRunning = false;
 
 		running=false;
 		String spritePath = null;
 		Image sprite = null;
 		showScreen = true;
 		

		BufferStrategy strategy = getBufferStrategy();
		Graphics2D g = (Graphics2D) strategy.getDrawGraphics();
		
		
 		if(screen.equals("win")) {
 			spritePath = "winscreen2.jpg";
			g.setColor(Color.GRAY);

 		} else if(screen.equals("lose")) {
 			spritePath = "LOOSESCREEN.png";
			g.setColor(new Color(0,1,171));
 		}
 		
 		try {
 			sprite = ImageIO.read(getClass().getResource(spritePath));
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 		
 		// Clear screen
 		g.fillRect(0, 0, getWidth(), getHeight());
 		
 		// drawImage
 		g.drawImage(sprite, 100, 0, null);
 		
 		strategy.show();
 	}
 
 	public static void main(String[] args) {
 		game = new Skateboardspel();
 		frame = new JFrame("SKATEBOARDSPELET");
 		frame.add(game);
 		frame.pack();
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.setResizable(false);
 		game.start();
 		game.newGame();
 		//game.showScreen("win");
 	}
 
 	public static Skateboardspel getGame() {
 		return game;
 	}
 	
 }
