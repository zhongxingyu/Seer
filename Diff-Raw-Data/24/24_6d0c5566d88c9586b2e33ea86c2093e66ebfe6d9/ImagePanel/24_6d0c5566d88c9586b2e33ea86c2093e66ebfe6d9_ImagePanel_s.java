 package ui.isometric.builder;
 
 import java.awt.Canvas;
 import java.awt.Graphics;
 
 import ui.isometric.IsoImage;
 
 public class ImagePanel extends Canvas {
 	private static final long serialVersionUID = 1L;
 	
 	private IsoImage image;
 	
 	public ImagePanel(IsoImage im) {
 		this.image = im;
 	}
 
 	@Override
 	public void paint(Graphics g) {
		g.drawImage(image.image(), 0, 0, this.getWidth(), this.getHeight(), this);
 	}
 }
