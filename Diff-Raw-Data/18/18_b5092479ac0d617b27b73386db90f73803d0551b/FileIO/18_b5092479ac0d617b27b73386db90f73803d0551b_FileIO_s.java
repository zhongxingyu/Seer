 package Core;
 
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.image.BufferedImage;
 import java.awt.image.RenderedImage;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.util.ArrayList;
 import javax.imageio.ImageIO;
 import javax.swing.ImageIcon;
 
 public class FileIO {
 	public static ArrayList <String> getLines (InputStream stream) {
 		ArrayList <String> lines = new ArrayList <> ();
 		
 		try {
 			BufferedReader reader = new BufferedReader (new InputStreamReader (stream));
 			String line;
 			
 			while ((line = reader.readLine ()) != null) {
 				lines.add (line);
 			}
 		}
 		catch (Exception ex) {
 			Logger.logException ("FileReader::getLines", ex);
 		}
 		
 		return lines;
 	}
 	
 	public static Image getImage (URL url, int width, int height) {
 		Image image = new ImageIcon (url).getImage ();
 		if (width != 0 && height != 0) {
 			image = image.getScaledInstance (width, height, 5);
 		}
 		
 		return image;
 	}
 	
	public static void writeImage (ImageIcon img, String fileName, String format) {
 		RenderedImage renderedImg = null;
 		
 		if (img.getImage() instanceof RenderedImage) {
 			renderedImg = (RenderedImage) img.getImage ();
 		}
 		else {
 			BufferedImage buffered = new BufferedImage (
 					img.getIconWidth(),
 					img.getIconHeight(),
 					BufferedImage.TYPE_INT_RGB);
 			Graphics2D g2 = buffered.createGraphics();
 			g2.drawImage(img.getImage(), 0, 0, null);
 			g2.dispose();
 			renderedImg = buffered;
 		}
 		
 		try {
			ImageIO.write(renderedImg, format, new File (fileName));
 		}
 		catch (Exception ex) {
 			Logger.logException("FileIO::writeImage", ex);
 		}
 	}
 }
 
