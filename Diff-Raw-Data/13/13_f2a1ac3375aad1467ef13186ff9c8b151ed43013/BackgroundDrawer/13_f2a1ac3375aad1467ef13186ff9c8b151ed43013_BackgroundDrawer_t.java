 package sliceWars.gui.drawers;
 
 import java.awt.Graphics2D;
 import java.awt.Rectangle;
 import java.awt.image.BufferedImage;
 import java.io.IOException;
 import java.io.InputStream;
 
 import javax.imageio.ImageIO;
 
 public class BackgroundDrawer implements Drawer{
 	private BufferedImage _background;
 	
 	public BackgroundDrawer() {
 		InputStream resourceAsStream = BackgroundDrawer.class.getResourceAsStream("back.png");
		if(resourceAsStream == null) return;
 		try {
 			_background = ImageIO.read(resourceAsStream);
 		} catch (IOException e1) {
 			throw new RuntimeException("Background not found");
 		}
 	}
 	
 	@Override
 	public void draw(Graphics2D g2) {
		if(_background == null) return;
 		Rectangle clipBounds = g2.getClipBounds();
 		for (int x=0; x<= clipBounds.getWidth()/_background.getWidth(); x++){
 			for (int y=0; y<= clipBounds.getHeight()/_background.getHeight(); y++){
 				g2.drawImage(_background, x * _background.getWidth(), y * _background.getHeight(), null);
 	        }
 	    }
 	}
 }
