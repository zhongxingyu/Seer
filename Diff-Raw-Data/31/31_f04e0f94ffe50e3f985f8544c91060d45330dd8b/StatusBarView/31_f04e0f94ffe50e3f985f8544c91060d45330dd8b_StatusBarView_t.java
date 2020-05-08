 package view;
 
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.geom.Shape;
 
 import model.StatusBar;
 
 public class StatusBarView {
 
 	private StatusBar statusBar;
 	private Image[] heart;
	private Shape staticBar;
 	
 	public StatusBarView(StatusBar statusBar) throws SlickException {
 		this.statusBar = statusBar;
 		heart = new Image[3];
 		heart[0] = new Image("pics/heart.png");
 		heart[1] = heart[0].copy();
		heart[2] = heart[0].copy();
		staticBar = new Rectangle(StatusBar.FIXED_BAR_POSX, StatusBar.HEART_POSY, StatusBar.FIXED_BAR_WIDTH, StatusBar.FIXED_BAR_HEIGHT);
 	}
 
 	public Image[] getHeart() {
 		return heart;
 	}

	public Shape getStaticBar() {
		return staticBar;
	}
 }
