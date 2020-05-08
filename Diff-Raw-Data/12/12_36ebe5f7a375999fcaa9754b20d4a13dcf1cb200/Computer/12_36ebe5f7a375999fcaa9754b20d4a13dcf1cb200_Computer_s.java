 package smanilov.mandelbrot.compute;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Image;
 import java.awt.Point;
 import java.awt.geom.Point2D;
 import java.awt.image.RenderedImage;
 import java.io.File;
 import java.io.IOException;
 import java.util.Queue;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.concurrent.locks.ReentrantLock;
 
 import javax.imageio.ImageIO;
 
 import org.apache.commons.math3.complex.Complex;
 
 /**
  * Computes and draws the points from the Mandelbrot set.
  * 
  * The drawing is done in steps and when a new step is invoked the previous 
  * one is stopped.
  * 
  * TODO: 
  *  1. Anti-aliasing
  *  2. Simple color schemes (set color, list of color gradient with list of positions on the gradient [e.g. [0.0, 0.5, 1.0]])
  * @author szm
  */
 public class Computer {
 
 	/**
 	 * Amount of iterations of the sequence used to decide if a point belongs 
 	 * to the Mandelbrot set. Ideally this would be infinity. The sequence is
 	 * [z_0 = 0, z_n = z_(n-1) ^ 2 + c], where c is the point to test. Each
 	 * z_k where k < iterations should be <= 2. 
 	 */
 	private static int iterations = 100;
 
 	private static final int NUM_SHADERS = 1;
 	
 	/**
 	 * The identifier of the current producer thread. Used to stop old ones.
 	 */
 	private static int currentDrawingId = -1;
 	
 	/**
 	 * A list containing the points that should be drawn next.
 	 */
 	private static Queue<Point> toDoList;
 	
 	/**
 	 * Used for more complex locking of the toDoList.
 	 */
 	private static final ReentrantLock queueLock = new ReentrantLock(); 
 	
 	/**
 	 * How many points were drawn so far by the current drawing step.
 	 */
 	private static int nDrawn = 0;
 	
 	/**
 	 * Such a waste of memory...
 	 */
 	private static final ReentrantLock nDrawnLock = new ReentrantLock(); 
 	
 	/**
 	 * The starting time of the current drawing step.
 	 */
 	private static long startTime;
 	
 	
 	/**
 	 * Returns a crop view of the Mandelbrot set.
 	 * @param drawing the image to draw on.
 	 * @param foregroundColor the color to use when drawing Mandelbrot pixels.
 	 * @param backgroundColor the color to use when drawing non-Mandelbrot pixels.
 	 * @param drawingLock the lock to use for locking the image.
 	 * @param center Center of the crop, in units of the complex plane, w.r.t. the origin.
 	 * @param scale log2(pixels) per unit in the complex plane. 
 	 */
 	public static void drawMandelbrotCrop(
 			final Image drawing,
 			final Color foregroundColor,
 			final Color backgroundColor,
 			final ReentrantLock drawingLock,
 			final int scale, 
 			final Point2D center
 	) {
 		initDrawStep();
 		
 		Thread pt = createProducerThread(drawing.getWidth(null), drawing.getHeight(null));
 		pt.start();
 		
 		for (int i = 0; i < NUM_SHADERS; ++i) {
 			Thread sh = createShaderThread(drawing, foregroundColor, backgroundColor, drawingLock, scale, center);
 			sh.start();			
 		}
 	}
 
 	/**
 	 * Set the number of maximum iterations per pixel.
 	 */
 	public static void setIterations(int iterations) {
 		Computer.iterations = iterations;
 	}
 	
 	/**
 	 * Resets the state of the class for a new draw step.
 	 */
 	private static void initDrawStep() {
 		++currentDrawingId;
 		if (toDoList != null) {
 			queueLock.lock();
 			toDoList.clear();
 			queueLock.unlock();
 		}
 		toDoList = new LinkedBlockingQueue<Point>();
 		
 		nDrawnLock.lock();
 		nDrawn = 0;
 		nDrawnLock.unlock();
 		
 		startTime = System.currentTimeMillis();
 	}
 
 	/**
 	 * Creates a thread that fills the toDoList with points.
 	 * @param width The maximum x coordinate.
 	 * @param height The maximum y coordinate.
 	 * @return
 	 */
 	private static Thread createProducerThread(final int width, final int height) {
 		Thread producerThread = new Thread() {
 
 			@Override
 			public void run() {
 				System.out.println("Producer: [START]");
 				super.run();
 				int id = currentDrawingId;
 				
 				boolean [][]added = new boolean[height][width];
 				int nAdded = 0; // How many points were added in the toDoList
 				int max = width * height;
 				
 				int x, y;
 				while (nAdded < max) {
 					if (id != currentDrawingId)
 						return;
 					x = (int) (width * Math.random());
 					y = (int) (height * Math.random());
 					if (!added[y][x]) {
 						toDoList.add(new Point(x, y));
 						++nAdded;
 						added[y][x] = true;
 					}
 				}
 				long interval = System.currentTimeMillis() - startTime;
 				System.out.println("Producer: [END after " + interval + " ms]");
 			}
 		};
 		return producerThread;
 	}
 
 	/**
 	 * Creates a thread that draws points from the toDoList.
 	 * @param width The maximum x coordinate.
 	 * @param height The maximum y coordinate.
 	 * @return
 	 */
 	private static Thread createShaderThread(final Image drawing,
 			final Color foregroundColor,
 			final Color backgroundColor,
 			final ReentrantLock drawingLock,
 			final int scale, 
 			final Point2D center) {
 		Thread shaderThread = new Thread() {
 
 			@Override
 			public void run() {
 				System.out.println("Shader: [START]");
 				int id = currentDrawingId;
 				int currentIterations = iterations;
 				super.run();
 				
 				int width = drawing.getWidth(null);
 				int height = drawing.getHeight(null);
 				
 				Point pixelCenter = new Point(width / 2, height / 2);
 				
 				while (nDrawn < width * height) {
 					// TODO: remove busy-wait
 					while (true) {
 						queueLock.lock();
 						if (toDoList.size() == 0) {
 							queueLock.unlock();
 							break;
 						}
 						Point p = toDoList.poll();
 						int i = p.x;
 						int j = p.y;
 						queueLock.unlock();
 						
 						Complex c = toComplex(i, j, pixelCenter, scale, center);
						Complex z = new Complex(0.0, 0.0);
 						int k;
						for (k = 0; k < currentIterations; ++k) {
 							if (id != currentDrawingId)
 								return;
 							z = z.multiply(z).add(c);
 							if (z.abs() > 2)
 								break;
 						}
 						if (k == currentIterations) {
 							drawingLock.lock();
 							Graphics g = drawing.getGraphics();
 							g.setColor(foregroundColor);
 							g.fillRect(i, j, 1, 1);
 							drawingLock.unlock();
 						} else {
 							drawingLock.lock();
 							Graphics g = drawing.getGraphics();
 							Color color = mixColors(backgroundColor, foregroundColor, (double) k / currentIterations);
 							g.setColor(color);
 							g.fillRect(i, j, 1, 1);
 							drawingLock.unlock();
 						}
 						nDrawnLock.lock();
 						++nDrawn;
 						nDrawnLock.unlock();
 						
 					}
 				}
 				long interval = System.currentTimeMillis() - startTime;
 				System.out.println("Shader: [END after " + interval + " ms]");
 				saveImage(drawing, drawingLock);
 			}
 		};
 		return shaderThread;
 	}
 	
 	private static void saveImage(Image drawing, ReentrantLock drawingLock) {
 		drawingLock.lock();
 		try {
 			ImageIO.write((RenderedImage) drawing, "png", new File("mandelbrot.png"));
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		drawingLock.unlock();
 	}
 
 	/**
 	 * Mixes the given colors in the given proportion.
 	 */
 	private static Color mixColors(Color c1, Color c2, double proportion) {
 		Color result = new Color(
 				(int)(c1.getRed() * proportion + c2.getRed() * (1 - proportion)),
 				(int)(c1.getGreen() * proportion + c2.getGreen() * (1 - proportion)),
 				(int)(c1.getBlue() * proportion + c2.getBlue() * (1 - proportion))
 		);
 		return result;
 	}
 	
 	/**
 	 * Converts a pixel coordinate into a complex number.
 	 * @param pixelCenter The center of the pixel coordinate system.
 	 * @param scale log2(pixels) per unit in the complex plane. 
 	 * @param center The center of the view on the complex plane.
 	 */
 	private static Complex toComplex(int x, int y, Point pixelCenter, int scale, Point2D center) {
 		int dx = (int) (x - pixelCenter.getX());
 		int dy = (int) (- y + pixelCenter.getY());
 		double real = (double) dx / (1 << scale) + center.getX();
 		double imaginary = (double) dy / (1 << scale) + center.getY();
 		return new Complex(real, imaginary);
 	}
 }
