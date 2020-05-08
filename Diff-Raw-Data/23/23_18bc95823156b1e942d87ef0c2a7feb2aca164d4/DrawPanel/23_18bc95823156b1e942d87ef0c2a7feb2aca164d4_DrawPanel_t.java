 package ui;
 
 import javax.swing.*;
 
 import java.awt.Graphics;
 import java.awt.image.BufferedImage;
 
 import life.LifeAlgo;
 
 @SuppressWarnings("serial")
 public class DrawPanel extends JPanel {
 	
 	private LifeAlgo currentAlgo = null;
 	private BufferedImage b = null;
 	
	
 	public DrawPanel() {
 		
 	}
 	
 	public void setLifeAlgo(LifeAlgo a){
 		currentAlgo = a;
 	}
 	
 	public void reDraw(){
 		paintComponent(getGraphics());
 	}
 	
 	@Override
 	public void paintComponent(Graphics g) {
 		if(b == null)
 			b = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
		if (currentAlgo != null) {
			currentAlgo.getDrawer().draw(0, 0, -1, currentAlgo.getState(), b);
			g.drawImage(b, 0, 0, null);
		}
 	}
 }
