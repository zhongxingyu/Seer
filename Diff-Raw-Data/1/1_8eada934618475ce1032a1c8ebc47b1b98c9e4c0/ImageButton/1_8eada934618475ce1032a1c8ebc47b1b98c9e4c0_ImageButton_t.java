 package view;
 
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Image;
 import java.awt.Insets;
 import java.awt.Rectangle;
 
 import javax.swing.JButton;
 import javax.swing.SwingUtilities;
 
 @SuppressWarnings("serial")
 public class ImageButton extends JButton {
 
     private Image image;
     private Rectangle innerArea = new Rectangle();
     private boolean drawImage;
     private int x;
     private int y;
 	
     public ImageButton(Image img, int x, int y)
     {
     	this.image = img;
 		this.setMargin(new Insets(0, 0, 0, 0));
 		this.setFont(new Font("Impact", Font.BOLD, 16));
 		
 		this.x = x;
 		this.y = y;
     }
     
 	public int x()
 	{
 		return x;
 	}
 	
 	public int y()
 	{
 		return y;
 	}
     
     public void setDrawImage(boolean b)
     {
     	drawImage = b;
    	repaint();
     }
 
 	@Override
 	protected void paintComponent(Graphics g) {
 
 		super.paintComponent(g);
 		
 		if (image != null && drawImage) {
 			SwingUtilities.calculateInnerArea(this, innerArea);
 
 			int width;
 
 			width = innerArea.width < innerArea.height ? innerArea.width : innerArea.height;
 
 			g.drawImage(
 					image,
 					innerArea.x,
 					innerArea.y,
 					width,
 					width, 
 					this);
 		}
 	}
 }
