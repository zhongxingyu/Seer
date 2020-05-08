 package renderer;
 
import java.awt.Container;
 import java.awt.MediaTracker;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.io.IOException;
 
 import javax.swing.ImageIcon;
 import javax.swing.JLabel;
 
 public class Menu implements MouseListener {
 
 	private JLabel menuBackground;
 	
	private Container container;
	
 	private int width;
 	private int height;
 	private boolean gameNotBegun;
 
 
	public Menu(Container c, int width, int height){
 		super();
 		
 		this.menuBackground = new JLabel();
		
		this.container = c;
		
 		this.height=height;
 		this.width=width;
 		this.gameNotBegun=true;
 	}
 	
 	public void init() throws IOException{
 		//Load the menu background image
 		ImageIcon bgMenuImage = new ImageIcon("./tex/youWin.png");
 		if(bgMenuImage.getImageLoadStatus() != MediaTracker.COMPLETE){
 			throw new IOException();
 		}
 		this.menuBackground.setBounds(0, 0, this.width, this.height);
 		this.menuBackground.setIcon(bgMenuImage);				
 	}
 
 	// GETTERS
 	
 	public JLabel getMenuBackground() {
 		return menuBackground;
 	}
 
 	public boolean isGameNotBegun() {
 		return gameNotBegun;
 	}
 
 	@Override
 	public void mouseClicked(MouseEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void mouseEntered(MouseEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void mouseExited(MouseEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void mousePressed(MouseEvent e) {
 		this.gameNotBegun = false;
 		System.out.println("Ca affiche quelque chose");
 		
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 	
 
 	
 	
 }
