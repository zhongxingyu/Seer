 package modules.led;
 
 import java.awt.AWTException;
 import java.awt.Color;
 import java.awt.HeadlessException;
 import java.awt.MouseInfo;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.Robot;
 import java.awt.Toolkit;
 import java.awt.image.BufferedImage;
 
 import output.LEDOutput;
 import modules.LEDModule;
 import modules.Module;
 
 public class ScreenColor extends LEDModule implements Runnable {
 
 	private int refreshInterval;
 
 	/**
 	 * Change color depending on the average color of the pixels on the screen.
 	 * Default refresh interval is 100 milliseconds.
 	 * 
 	 * @param output
 	 *            The object to send the output to.
 	 */
 	public ScreenColor(LEDOutput output) {
 		this(output, 100);
 	}
 
 	/**
 	 * Change color depending on the average color of the pixels on the screen.
 	 * 
 	 * @param output
 	 *            The object to send the output to.
 	 * @param refreshInterval
 	 *            How many milliseconds the thread should wait before the next
 	 *            refresh. default is 100.
 	 */
 	public ScreenColor(LEDOutput output, int refreshInterval) {
 		super(output);
 		this.refreshInterval = refreshInterval;
 	}
 
 	@Override
 	public void run() {
 		try {
 			while (!stop) {
 				if (pause)
 					Thread.sleep(refreshInterval);
 				else {
 					BufferedImage bi = new Robot()
 							.createScreenCapture(new Rectangle(Toolkit
 									.getDefaultToolkit().getScreenSize()));
 					int r = 0, g = 0, b = 0;
 					int numberOfPixels = 0;
 					for (int x = 0; x < bi.getWidth(); x += 4) {
 						for (int y = 0; y < bi.getHeight(); y += 4) {
 							int pixel = bi.getRGB(x, y);
 							r = r + (int) (255 & (pixel >> 16)); // add up red
 							g = g + (int) (255 & (pixel >> 8)); // add up green
 							b = b + (int) (255 & (pixel)); // add up blue
 							numberOfPixels++;
 						}
 					}
 
 					r = r / numberOfPixels;
 					g = g / numberOfPixels;
 					b = b / numberOfPixels;
 					output.sendRGB(r, g, b);
 
 					Thread.sleep(refreshInterval);
 				}
 			}
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (HeadlessException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (AWTException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 }
