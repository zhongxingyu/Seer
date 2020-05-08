 import java.awt.Graphics;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 
 import javax.imageio.ImageIO;
 
 
 public class Sprite {
 	private String file;
 	private BufferedImage img;
 	
 	public Sprite(String filename){
 		file = filename;
 		
 		try {
 			img = ImageIO.read(new File(file));
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public void draw(Graphics g, int x, int y, int width, int height, int spriteStage, int direction){
		System.out.println("spriteStage*width, "+ spriteStage*width+" direction*height, "+direction*height+" width, "+width+" height "+height);
 		BufferedImage temp = img.getSubimage(spriteStage*width, direction*height, width, height);
 		g.drawImage(temp, x, y, width, height, null);
 	}
	
 }
