 package vooga.rts.util;
 
 import java.awt.AlphaComposite;
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.image.BufferedImage;
 
 /**
  * This class is used to color the images of interactive entities in order to
  * distinguish between player units
  * 
  * @author Junho Oh
  * 
  */
 public class FilterImageColor {
 	public static int COLOR_POSITION_OFFSET = 4;
	private static Color[] myPlayerColors = { new Color(128, 255, 128, 150),
 			new Color(255, 0, 0, 150), new Color(255, 255, 0, 200),
			new Color(0, 0, 255, 200), new Color(0, 255, 0, 200),
 			new Color(255, 128, 0, 200), new Color(255, 0, 255, 150),
 			new Color(0, 128, 255, 150) };
 
 	/**
 	 * If the passed in image is an Image, converts to a BufferedImage first and
 	 * returns the colorImage
 	 * 
 	 * @param image
 	 *            the image to change
 	 * @param playerID
 	 *            the index of the color to use
 	 * @return the colorImage
 	 */
 	public static BufferedImage colorImage(Image image, int playerID) {
 		BufferedImage img = new BufferedImage(image.getWidth(null),
 				image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
 		Graphics graphics = img.getGraphics();
 		graphics.drawImage(image, 0, 0, null);
 
 		return colorImage(img, playerID);
 	}
 
 	/**
 	 * Adds a rectangle band to the bottom of the passed in BufferedImage that
 	 * is blended using AlphaComposite and color based on playerID. Band is used
 	 * to distinguish the interactive entities of different entities.
 	 * 
 	 * @param image
 	 *            the passed in BufferedImage to add the band to
 	 * @param playerID
 	 *            the index of the color to use
 	 * @return the changed image with the colored band
 	 */
 	public static BufferedImage colorImage(BufferedImage image, int playerID) {
 		BufferedImage toReturn = new BufferedImage(image.getWidth(),
 				image.getHeight(), image.getType());
 		Graphics2D g = (Graphics2D) toReturn.getGraphics();
 		g.setComposite(AlphaComposite
 				.getInstance(AlphaComposite.DST_ATOP, 1.0f));
 		g.setColor(myPlayerColors[playerID]);
 		g.fillRect(0, image.getHeight() - image.getHeight()
 				/ COLOR_POSITION_OFFSET, image.getWidth(), image.getHeight()
 				/ COLOR_POSITION_OFFSET);
 		g.drawImage(image, 0, 0, null);
 		g.dispose();
 		return toReturn;
 	}
 }
