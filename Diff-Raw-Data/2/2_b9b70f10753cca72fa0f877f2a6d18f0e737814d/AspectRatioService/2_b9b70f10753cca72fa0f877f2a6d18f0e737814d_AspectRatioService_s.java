 package ambibright.engine;
 
 import java.awt.Rectangle;
 import java.awt.image.BufferedImage;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import ambibright.engine.capture.ScreenCapture;
 import ambibright.ressources.CurrentBounds;
 
 /**
  * Checks for changes in aspect ratio and update the bounds if any
  */
 public class AspectRatioService implements Runnable {
 
 	private static final Logger logger = LoggerFactory.getLogger(AspectRatioService.class);
 
 	private static final int blackLimit = 6;
 	private final Rectangle fullScreenBounds;
 	private final CurrentBounds currentBounds;
 	private final ScreenCapture screenCapture;
 	private Rectangle lastScreenBounds;
 	private int red = 0;
 	private int green = 0;
 	private int blue = 0;
 	private int y = 0;
 	private int x = 0;
 	private int testY, testX;
 
 	public AspectRatioService(Rectangle fullScreenBounds, CurrentBounds currentBounds, ScreenCapture screenCapture) {
 		this.fullScreenBounds = fullScreenBounds;
 		this.currentBounds = currentBounds;
 		this.screenCapture = screenCapture;
 		this.lastScreenBounds = fullScreenBounds;
 	}
 
 	public void run() {
 		logger.debug("Checking if aspect ratio changed");
 
 		// Get current image
 		BufferedImage image = screenCapture.captureScreen(fullScreenBounds);
 
 		// Detect top
 		y = fullScreenBounds.height / 4;
 		for (testX = 0; testX < fullScreenBounds.width; testX += fullScreenBounds.width / 5) {
 			for (testY = 0; testY < fullScreenBounds.height / 4; testY++) {
 				if (!isBlack(image.getRGB(testX, testY))) {
 					y = Math.min(y, testY);
 					break;
 				}
 			}
 		}
 
 		// Detect left
 		x = fullScreenBounds.width / 4;
 		for (testY = 0; testY < fullScreenBounds.height; testY += fullScreenBounds.height / 5) {
 			for (testX = 0; testX < fullScreenBounds.width / 4; testX++) {
				if (!isBlack(image.getRGB(testX, testY))) {
 					x = Math.min(x, testX);
 					break;
 				}
 			}
 		}
 
 		// Flushing the image
 		image.flush();
 
 		Rectangle newBounds = new Rectangle(fullScreenBounds.x + x, fullScreenBounds.y + y, fullScreenBounds.width - (2 * x), fullScreenBounds.height - (2 * y));
 		if (!lastScreenBounds.equals(newBounds)) {
 			logger.info("Aspect ratio changed. New bounds : {}", newBounds);
 			lastScreenBounds = newBounds;
 			currentBounds.updateBounds(newBounds);
 		} else {
 			logger.debug("Aspect ratio didn't change");
 		}
 	}
 
 	private boolean isBlack(int color) {
 		red = (color & 0x00ff0000) >> 16;
 		green = (color & 0x0000ff00) >> 8;
 		blue = color & 0x000000ff;
 		return (red + blue + green) <= blackLimit;
 	}
 }
