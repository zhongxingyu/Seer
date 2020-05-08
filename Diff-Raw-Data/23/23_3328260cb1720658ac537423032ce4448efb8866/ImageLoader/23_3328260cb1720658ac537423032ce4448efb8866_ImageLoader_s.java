 package image;
 
import java.awt.Image;
 import java.io.IOException;
 
 import javax.imageio.ImageIO;
 
 public class ImageLoader {
 	
	public Image getImage(String name){
 		
		Image img;
 			try{ img = ImageIO.read(ImageLoader.class.getResource(name)); }
 			catch (IOException e){ throw new Error(e); }
 		
		
 		return img;
 	}
 
 }
