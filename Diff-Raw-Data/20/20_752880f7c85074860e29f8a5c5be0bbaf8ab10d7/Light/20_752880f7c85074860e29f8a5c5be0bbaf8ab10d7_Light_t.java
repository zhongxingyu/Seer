 import java.awt.*;
import model.Drawable;
 
 import javax.imageio.ImageIO;
 import java.io.*;
 
 
 public class Light implements Drawable{
 	private Image image;
 	
 	
	public Light(Location l, String image){
		try {
			this.image = ImageIO.read(new File(image));
		} catch (IOException e) {
			e.printStackTrace();
		}
 	}
 
     public void draw(Graphics g){
    	
     }
 }
