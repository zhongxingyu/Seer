 /**
  * 
  */
 package rezend.musix;
 
 import java.awt.Graphics2D;
 import java.awt.geom.AffineTransform;
 import java.awt.image.AffineTransformOp;
 import java.awt.image.BufferedImage;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.text.NumberFormat;
 import java.text.ParseException;
 import java.util.Locale;
 
 import javax.imageio.ImageIO;
 
 import net.sourceforge.tess4j.Tesseract;
 import net.sourceforge.tess4j.TesseractException;
 
 /**
  * This class provides the necessary methods to read the text content of simple
  * images using the tesseract library.
  * 
  * @author Rafael Rezende
  * 
  */
 public final class OCRModule {
 
 	/**
 	 * Scale factor of the price image for Tesseract OCR. It strongly affects
 	 * the precision of the OCR. Recommended: 2 for AffineTransform; 3 for
 	 * Graphics.
 	 */
 	static final int SCALE = 2;
 	/**
 	 * Flag to use graphics. readImage() will use AffineTransform otherwise.
 	 * Default: false.
 	 */
 	static final boolean USE_GRAPHICS = false;
 
 	/**
 	 * Type of transformation in case AffineTransform is used. It strongly
 	 * affects the precision of the OCR. Types are
 	 * AffineTransformOp.TYPE_BICUBIC (recommended),
 	 * AffineTransformOp.TYPE_BILINEAR and
 	 * AffineTransformOp.TYPE_NEAREST_NEIGHBOR.
 	 */
 	static final int AFFINE_TRANSFORMATION_TYPE = AffineTransformOp.TYPE_NEAREST_NEIGHBOR;
 
 	/**
 	 * Singleton instance of Tesseract.
 	 */
 	static Tesseract instance = Tesseract.getInstance();
 
 	/**
 	 * Get value from the price image. This image must be scaled before running
 	 * through Tesseract. Options are available as constants above.
 	 * 
 	 * @param url
 	 *            URL of the image
 	 * @return the value read in the price image.
 	 */
	public static double readImage(URL url) {
 
 		// product price
 		Double price = null;
 
 		try {
 
 			// buffer that holds the image from url
 			BufferedImage priceImg = ImageIO.read(url);
 
 			// preventive for too small figures (invalid values) returned from
 			// the url
 			if (priceImg.getWidth() < 20 || priceImg.getHeight() < 20)
 				return 0.0;
 
 			// buffer that holds the scaled image
 			BufferedImage scaledPriceImg = new BufferedImage(
 					priceImg.getWidth() * SCALE, priceImg.getHeight() * SCALE,
 					BufferedImage.TYPE_INT_RGB);
 
 			if (USE_GRAPHICS) {
 				// Scale solution using Graphics
 				Graphics2D grph = (Graphics2D) scaledPriceImg.getGraphics();
 				grph.scale(SCALE, SCALE);
 				grph.drawImage(priceImg, 0, 0, null);
 				grph.dispose();
 			} else {
 				// Scale solution using AffineTransform
 				AffineTransform at = new AffineTransform();
 				at.scale(SCALE, SCALE);
 				AffineTransformOp scaleOp = new AffineTransformOp(at,
 						AFFINE_TRANSFORMATION_TYPE);
 				scaledPriceImg = scaleOp.filter(priceImg, scaledPriceImg);
 			}
 
 			// run the Tesseract OCR to get the text from the image
 			String priceStr = instance.doOCR(scaledPriceImg);
 
 			// get the german number format due to comma
 			NumberFormat format = NumberFormat.getInstance(Locale.GERMAN);
 			price = format.parse(priceStr).doubleValue();
 
 		} catch (IOException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		} catch (TesseractException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ParseException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		return price;
 	}
 
 	/**
 	 * Get value from the price image. This image must be scaled before running
 	 * through Tesseract. Options are available as constants above.
 	 * 
 	 * @param url
 	 *            URL String of the image
 	 * @return the value read in the price image.
 	 */
	public static double readImage(String urlStr) {
		
 		URL url = null;
 		try {
			// complete the url with "http://" in case the given one does not
			// provide the protocol (https, ftp etc)
			if (!urlStr.toLowerCase().contains("://")) {
				urlStr = "http://" + urlStr;
			}
			
 			// create URL from string
 			url = new URL(urlStr);
 		} catch (MalformedURLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
		// if the URL is invalid, return -1
 		if (url == null)
			return -1.0;
 
 		// else return the readImage(url) accordingly
 		return readImage(url);
 
 	}
 }
