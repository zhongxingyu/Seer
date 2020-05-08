 import java.awt.Canvas;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics2D;
 import java.awt.image.BufferStrategy;
 
 import javax.swing.JFrame;
 
 @SuppressWarnings("serial")
 // Varf�r varnar Eclipse f�r det h�r?
 public class Skateboardspel extends Canvas implements Runnable {
 
 	private static JFrame frame = new JFrame();
 	private static Dimension d;
 	private static Controller controller = new Controller();
 	private boolean running;
 	private World world;
	private Katt katten;
	private Pool poolen;
	private Ramp rampen;
 
 	public Skateboardspel() {
 		// RUN SKATEBOARDSPELET
 
 		world = new World("background.JPG");
 
 		// Setting up static size of window.
 		d = world.getCameraDimension();
 		setPreferredSize(d);
 		setMinimumSize(d);
 		setMaximumSize(d);
 
 		// Setting up listeners for mouse and keyboard
 		addKeyListener(controller);
 		// addMouseListener(controller);
 		// addMouseMotionListener(controller);
 	}
 
 	@Override
 	public void run() {
 		while (running) {
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
 		BufferStrategy strategy = getBufferStrategy();
 
 		Graphics2D g = (Graphics2D) strategy.getDrawGraphics();
 		g.setColor(Color.WHITE);
 		g.fillRect(0, 0, getWidth(), getHeight());
 
 		world.render(g);
 
 		strategy.show();
 	}
 
 	private void update() {
 		world.update(controller);
 	}
 
 	public static void main(String[] args) {
 		Skateboardspel game = new Skateboardspel();
 		frame = new JFrame("SKATEBOARDSPELET");
 		frame.add(game);
 		frame.pack();
 		frame.setVisible(true);
 		frame.setLocationRelativeTo(null);
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.setResizable(false);
 		game.start();
 
 	}
 }
