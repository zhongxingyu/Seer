 package gui;
 
 import java.awt.*;
 import java.awt.image.*;
 import javax.swing.*;
 
 /**
  * Calendar item.
  */
 public class Calendar extends JPanel implements Runnable {
 	private boolean started = false, running = false;
 	
 	// buffering
 	private Graphics2D g, bg; // graphics and background graphics
 	private BufferStrategy strategy;
 	private BufferedImage background;
 	
 	// drawing
 	private GraphicsConfiguration gc;
 	private Canvas canvas;
 	private Dimension content;
 	
 	public Calendar() {
 		super(true); // set isDoubleBuffered to true
 		gc = GraphicsEnvironment.getLocalGraphicsEnvironment()
 				.getDefaultScreenDevice().getDefaultConfiguration();
 		
		setSize(content = new Dimension(800, 600));
 		canvas = new Canvas(gc) {
 			public void paint(Graphics g) {
 				System.out.println("Paint.");
 			}
 		};
 		canvas.setSize(content);
 		add(canvas, 0);
 	}
 	/**
 	 * Creates a new <code>BufferedImage</code> from graphics. Useful for
 	 * double-buffering.
 	 * @param gc A configuration for graphics.
 	 * @param bounds Dimension of our window to draw graphics in.
 	 * @param alpha Whether or not our image has an alpha channel.
 	 * @return A new buffered image.
 	 */
 	public BufferedImage createBufferedImage(Dimension bounds, boolean alpha) {
 		return gc.createCompatibleImage(bounds.width, bounds.height,
 				alpha ? Transparency.TRANSLUCENT : Transparency.OPAQUE);
 	}
 	/**
 	 * Refreshes contents in the buffer.
 	 * @return If successful or not.
 	 */
 	public boolean screenUpdate() {
 		g.dispose(); // free resources
 		g = null;
 		
 		try {
 			strategy.show();
 			Toolkit.getDefaultToolkit().sync();
 			return !strategy.contentsLost();
 		} catch (NullPointerException e) {
 			System.out.println("NullPointerException encountered: ");
 			e.printStackTrace();
 		} catch (IllegalStateException e) {
 			System.out.println("IllegalStateException encountered: ");
 			e.printStackTrace();
 		}
 		
 		return true;
 	}
 	/**
 	 * Gets the main screen buffer.
 	 * @return The front buffer for the graphics.
 	 */
 	public Graphics2D getBuffer() {
 		if (g == null)
 			try {
 				g = (Graphics2D) strategy.getDrawGraphics();
 			} catch (IllegalStateException e) {
 				System.out.println("Illegal state for buffer strategy.");
 				e.printStackTrace();
 			}
 		return g;
 	}
 	/* Renders contents to the <code>canvas</code> from graphics. */
 	public void render(Graphics2D g2d) {
 		g2d.setColor(Color.black);
 		g2d.fillRect(0, 0, content.width, content.height);
 		g2d.setColor(Color.red);
 		g2d.fillArc(50, 50, 200, 200, 0, 360);
 	}
 	/* Starts the running of the main canvas. */
 	public void start() {
 		background = createBufferedImage(content, true);
 		canvas.createBufferStrategy(2); // two buffers
 		do {
 			strategy = canvas.getBufferStrategy();
 		} while (strategy == null);
 		started = true;
 		run();
 	}
 	/**
 	 * Iterates through the maincanvas.
 	 */
 	public void run() {
 		if (!started || running) return;
 		running = true;
 		main: while (running) {
 			bg = (Graphics2D) background.getGraphics();
 			do {
 				if (!running) break main;
 				Graphics2D gb = getBuffer(); // front-screen buffer
 				render(bg); // render with background graphics (offscreen)
 				gb.drawImage(background, 0, 0, null); // draw to screen 
 			} while (!screenUpdate());
 		}
 	}
 }
