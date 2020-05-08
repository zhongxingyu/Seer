 package sprites;
 
 import java.awt.image.BufferedImage;
 import java.util.List;
 import java.util.Map;
 
 import StateMachines.StateMachine;
 import collisions.Hitbox;
 
 import com.golden.gamedev.object.sprite.AdvanceSprite;
 
 public class GeneralSprite extends AdvanceSprite implements Boxable {
 	protected Map<String, Integer> myScores;
 	protected StateMachine myStateMachine;
 	protected List<Hitbox> myHitboxes;
 	
 	
 	public GeneralSprite() {
 		super();
 	}
 	public GeneralSprite(BufferedImage i) {
 		super();
 		BufferedImage[] image = new BufferedImage[1];
 		image[0] = i;
		setImage(i);
 	}
 	
 	public GeneralSprite(BufferedImage[] i) {
 		super(i);
 	}
 	
 	public GeneralSprite(BufferedImage i, double x, double y) {
 		super(x,y);
 		setImage(i);
 	}
 	
 	public GeneralSprite(BufferedImage[] i, double x, double y) {
 		super(i,x,y);
 	}
 	
 	public GeneralSprite(double x, double y) {
 		super(x,y);
 	}
 
 	public List<Hitbox> getHitboxes() {
 		return myHitboxes;
 	}
 
 	public String getDefaultEvent() {
 		return "";
 	}
 	
 
 }
