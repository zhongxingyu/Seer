 package qp.main.gui;
 
 import java.awt.Image;
import java.io.IOException;
import java.io.InputStream;
 
import javax.imageio.ImageIO;
 
 public class ImageLoader{
	static ImageLoader IL = new ImageLoader();
 	public static Image getImageFrom(String s){
		InputStream input = IL.getClass().getClassLoader().getResourceAsStream(s);
		try {
			return ImageIO.read(input);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
 	}
 }
