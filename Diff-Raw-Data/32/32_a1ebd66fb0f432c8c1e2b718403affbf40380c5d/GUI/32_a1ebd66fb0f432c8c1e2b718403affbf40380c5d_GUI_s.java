 package Core;
 import java.awt.Graphics;
 import java.awt.image.BufferedImage;
 import java.io.IOException;
 
 import javax.imageio.ImageIO;
 import javax.swing.JFrame;
 
 import environment.Tiles;
 
 
 public class GUI extends JFrame {
 	Canvas canvas;
 	MainGame main;
 	public GUI(){
 		this.setExtendedState(this.getExtendedState() | this.MAXIMIZED_BOTH);
 		this.setVisible(true);
 		Tiles tiles = new Tiles();
 		this.canvas = new Canvas(this,tiles);
 		this.add(canvas);
 		this.canvas.setVisible(true);
 		addKeyListener(canvas);
 		
 		//create main game
 		this.main = new MainGame(canvas.getPlayer(),this,canvas,tiles);
 		
 	}
 	
 	@Override
 	public void paint(Graphics g){
 		if(canvas != null){
 			canvas.repaint();
 		}
 		
 	}
 	
 	public static void main(String[] args){
 		GUI gui = new GUI();
 	}
 
 }
