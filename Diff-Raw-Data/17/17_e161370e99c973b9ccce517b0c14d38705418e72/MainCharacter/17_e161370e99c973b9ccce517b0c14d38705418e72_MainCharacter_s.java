 package sprites;
 
 import hudDisplay.NumberStat;
 import hudDisplay.Stat;
 
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 
 import javax.imageio.ImageIO;
 
 import org.jdom2.Element;
 
 import com.golden.gamedev.object.SpriteGroup;
 
 import States.InAirState;
 import States.JetPackPowerup;
 import States.OnLandState;
 import States.State;
import States.TeleJumpPowerup;
 import sprites.StateSprite;
 import stateTransitions.AddStateTransition;
 import stateTransitions.ReplaceStateTransition;
 import stateTransitions.StateTransition;
 
 public class MainCharacter extends StateSprite {
 	private boolean canFire;
 	public MainCharacter()
 	{
 		super();
 		State s1 = new InAirState(this);
 		setGravity(0.002);
 		getStateManager().addState(s1);
 		StateTransition land = new ReplaceStateTransition(getStateManager(), "landed",  new OnLandState(this), s1);
 		StateTransition jump = new ReplaceStateTransition(getStateManager(), "jumped", s1, new OnLandState(this));
 		StateTransition powerup = new AddStateTransition(getStateManager(), "pwrup", new JetPackPowerup(this));
 		setMyStats(new HashMap<String, Stat>());
 		land.activate();
 		jump.activate();
 		powerup.activate();
 		canFire=true;
 	}
 	
 	public void Shoot(SpriteGroup Projectile, int x, int y) {
         Projectile shot;
         if(canFire == true){
             try {
 				shot = new Projectile(ImageIO.read(new File("images/Blk-Rd-Bullet.gif")));
 				shot.setLocation( this.getX()+15, this.getY()-5 );
 	            shot.fireAtTarget(x,y);
 	            Projectile.add(shot);
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
             
         }
         
 
     }
 	
 
 	public void update(long elapsedTime)
 	{
 		super.update(elapsedTime);
 		this.addVerticalSpeed(elapsedTime, getGravity(), 0.5);
 	}
 	
 	public MainCharacter parse(Element e) {
 		String path=e.getChild("image").getText();
 		File file=new File(path);
 		BufferedImage image=null;
 		try {
 			image = ImageIO.read(file);
 		} catch (IOException e1) {
 			System.out.print("IOException");
 		}
 		setPath(path);
 		setX(Double.parseDouble(e.getChildText("x")));
 		setY(Double.parseDouble(e.getChildText("y")));
 		setImage(image);
 		e.getChildText("lives");
 		e.getChildText("score");
 		e.getChildText("coinMult");
 		createStat("lives", new NumberStat(5));
 		createStat("score", new NumberStat(0));
 		createStat("coinMult", new NumberStat(0));
 		return this;
 		
 	}
 }
