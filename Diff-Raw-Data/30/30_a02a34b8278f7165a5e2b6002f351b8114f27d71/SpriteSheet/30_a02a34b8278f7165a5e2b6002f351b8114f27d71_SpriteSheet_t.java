 import java.awt.Image;
 import java.awt.image.BufferedImage;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 
 import javax.imageio.ImageIO;
 
 
 public class SpriteSheet {
 	File file;
 	BufferedImage image;
 	
 	public SpriteSheet(String Filename) {
 		// TODO Auto-generated constructor stub
 	 file = new File(Filename);
 	 try {
 		image = ImageIO.read(file);
 	
 	} catch (IOException e) {
 		// TODO Auto-generated catch block
 		e.printStackTrace();
 	}
 	}
 	
	public BufferedImage getSprite(int spriteID, int width, int height){
 		int row = spriteID/16;
 		int col = spriteID%16;
 		
		BufferedImage img = new BufferedImage(width,height, BufferedImage.TYPE_INT_RGB);
		for(int i=row*32; i<row*32+height; i++)
			for(int j=col*32; j<col*32+width; j++)
 				img.setRGB(j-(col*32), i-(row*32), this.image.getRGB(j,i));
 		return img;
 		
 		
 	}
 
 }
