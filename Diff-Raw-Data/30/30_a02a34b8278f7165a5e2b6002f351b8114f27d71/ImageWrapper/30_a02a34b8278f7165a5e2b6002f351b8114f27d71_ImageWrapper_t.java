 import java.awt.*;
 import java.awt.image.BufferedImage;
 import java.io.*;
 
 import javax.imageio.ImageIO;
 
 public class ImageWrapper {
 	private BufferedImage picture;
 	private Color transparency;
 
 	public ImageWrapper(String location, Color colorKey) {
 		File myfile = new File(location);
 
 		try {
 			picture = ImageIO.read(myfile);
 
 		} catch (IOException e) {
 			System.out.println("the error is " + e);
 		}
 
 		transparency = colorKey;
 	}
 
 	public ImageWrapper(String location) {
 		File myfile = new File(location);
 
 		try {
 			picture = ImageIO.read(myfile);
 
 		} catch (IOException e) {
 			System.out.println("the error is " + e);
 		}
 		
 		transparency = new Color(picture.getRGB(0, 0));
 	}
 	
	public ImageWrapper(int bit, int width, int height, SpriteSheet sprites)
 	{
 		//if(bit < 0){bit = 0;}
 		bit++;
		picture = sprites.getSprite(bit, width, height);
 	}
 	
 	public void draw(Graphics g, int x, int y, int width, int height)
 	{
 		g.drawImage(picture, x, y, width, height, null);
 	}
 
 	public Image getImage() {
 		return (picture);
 	}
 
 	public void setImage(BufferedImage tempImage) {
 		picture = tempImage;
 	}
 
 	public Color getTranspercy() {
 		return transparency;
 	}
 
 	public void setTranspercy(Color transparency) {
 		this.transparency = transparency;
 	}
 
 }
