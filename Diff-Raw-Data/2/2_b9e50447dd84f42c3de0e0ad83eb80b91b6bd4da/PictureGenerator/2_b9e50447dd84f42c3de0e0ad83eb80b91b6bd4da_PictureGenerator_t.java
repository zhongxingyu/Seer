 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 
 import javax.imageio.ImageIO;
 
 public class PictureGenerator {
	public static void generate(String text, String name) {
 		BufferedImage image = new BufferedImage(100, 100,
 				BufferedImage.TYPE_INT_RGB);
 		// Draw text
 		Graphics g = image.getGraphics();
 		g.setColor(Color.RED);
 		g.drawString(text, 10, 10);
 
 		try {
 			ImageIO.write(image, "jpg", new File(name));
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 }
