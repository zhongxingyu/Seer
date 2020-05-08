 import java.awt.*;
 
 import javax.imageio.ImageIO;
 import java.io.*;
 
 
 public class Light implements Drawable{
 	private Image image;
 	
 	
	public Light(String image){
		this.image = ImageIO.read(new File(image));
 	}
 
     public void draw(Graphics g){

     }
 }
