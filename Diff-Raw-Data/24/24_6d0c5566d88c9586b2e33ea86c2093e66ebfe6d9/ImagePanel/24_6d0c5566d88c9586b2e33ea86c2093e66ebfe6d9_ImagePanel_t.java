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
		double vwidth = this.getWidth();
		double vheight = this.getHeight();
		double vaspect = vwidth/vheight;
		
		double iwidth = image.width();
		double iheight = image.height();
		double iaspect = iwidth/iheight;
		
		if(iaspect < vaspect) {
			vwidth = iaspect * vheight;
		}
		else {
			vheight = vwidth / iaspect;
		}
				
		g.drawImage(image.image(), (this.getWidth() - (int)vwidth)/2, (this.getHeight() - (int)vheight)/2, (int)vwidth, (int)vheight, this);
 	}
 }
