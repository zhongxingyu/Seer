 package breakout;
 
import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.image.BufferedImage;
 
 import javax.swing.JPanel;
 
 public class GamePanel extends JPanel{
 	private BufferedImage bufferedImage;
 	
 	public BufferedImage getBufferedImage(){
 		this.bufferedImage = new BufferedImage(this.getWidth(), this.getHeight(), BufferedImage.TYPE_INT_ARGB);
 		return this.bufferedImage;
 	}
 	public void paint(Graphics g){
 		super.paint(g);
 		g.drawImage(this.bufferedImage, 0, 0, this.getBackground(), this);
 	}
 
 }
