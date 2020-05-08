 import java.awt.Image;
 import java.awt.image.BufferedImage;
 
 import org.scilab.forge.jlatexmath.TeXConstants;
 import org.scilab.forge.jlatexmath.TeXFormula;
 import org.scilab.forge.jlatexmath.TeXIcon;
 
 import acm.graphics.GImage;
 
 public class LatexHandler {
 		
 		/** 
 		 * Uses the latex string given to it to create an image of the corresponding 
 		 * "pretty mathematics" and returns this image.
 		 * @param latex string containing raw latex to be turned into an image
 		 * @return the image of the "pretty mathematics" described by the latex string
 		 */
 		public static Image latexToImage(String latex) {
			TeXFormula fomule = new TeXFormula(latex);
			TeXIcon ti = fomule.createTeXIcon(
			TeXConstants.STYLE_DISPLAY, 40);
			BufferedImage b = new BufferedImage(ti.getIconWidth(), ti
			   .getIconHeight(), BufferedImage.TYPE_4BYTE_ABGR);
 			return b;
 		}
 		
 		/** 
 		 * Uses the latex string given to it to create an image of the corresponding 
 		 * "pretty mathematics" and returns this image as a acm.graphics.GImage.
 		 * @param latex string containing raw latex to be turned into an image
 		 * @return the GImage of the "pretty mathematics" described by the latex string
 		 */
 		public static GImage latexToGImage(String latex) {
 			Image image = latexToImage(latex);
 			GImage gImage = new GImage(image);
 			return gImage;
 		}
 
 }
