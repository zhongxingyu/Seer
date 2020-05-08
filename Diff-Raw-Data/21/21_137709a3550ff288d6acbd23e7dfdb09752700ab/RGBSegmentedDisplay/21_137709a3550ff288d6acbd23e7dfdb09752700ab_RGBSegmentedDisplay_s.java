 package camibrate.gui.blobcreator;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.image.BufferedImage;
 import java.util.Vector;
 
 import javax.swing.JPanel;
 
 import camibrate.CamibrateBlob;
 import camibrate.StaticFunctions;
 
 public class RGBSegmentedDisplay extends JPanel {
 	
 	Vector<CamibrateBlob> blobs;
 	BufferedImage image;
 	
 	RGBSegmentedDisplay(Vector<CamibrateBlob> blobs, BufferedImage image){
 		this.blobs = blobs;
 		this.image = image;
 		
 		this.image = StaticFunctions.scaleImageToFit(image, 500, 500);
 		this.setPreferredSize(new Dimension(this.image.getWidth(),this.image.getHeight()));
 		
 		this.setBackground(Color.magenta);
 	}
 	
 	public void update(Vector<CamibrateBlob> blobs){
 		this.blobs = blobs;
 		repaint();
 	}
 
 	@Override
 	protected void paintComponent(Graphics g) {
 		super.paintComponent(g);
 		for(int x = 0; x < image.getWidth(); x++){
 			for(int y = 0; y < image.getHeight(); y++){
 				Color c = new Color(image.getRGB(x, y));
 				g.setColor(Color.black);
 				g.drawRect(x, y, 1, 1);
 				for(int i = 0; i < blobs.size(); i++){
 					if(blobs.get(i).testColor(c)){
						System.out.println("Pixel("+x+","+y+") color("+c+") passed!---------------------");
 						//image.setRGB(x, y, c.getRGB());
 						g.setColor(blobs.get(i).getDisplayColor());
 						g.drawLine(x, y, x, y);
 					}else{
 						//System.out.println("Pixel("+x+","+y+") color("+c+") FAILED!!!!");
 					}
 				}
 			}
 		}
 		
 	}
 	
 	
 
 }
