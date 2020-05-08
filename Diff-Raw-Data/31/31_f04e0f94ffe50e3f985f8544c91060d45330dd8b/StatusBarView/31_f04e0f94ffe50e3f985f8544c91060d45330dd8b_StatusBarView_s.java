 package view;
 
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 
 import model.StatusBar;
 
 public class StatusBarView {
 
 	private StatusBar statusBar;
 	private Image[] heart;
	
 	
 	public StatusBarView(StatusBar statusBar) throws SlickException {
 		this.statusBar = statusBar;
 		heart = new Image[3];
 		heart[0] = new Image("pics/heart.png");
 		heart[1] = heart[0].copy();
		heart[2] = heart[0].copy();		
 	}
 
 	public Image[] getHeart() {
 		return heart;
 	}
 }
