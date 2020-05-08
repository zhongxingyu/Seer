 package interactiveSprites;
 import game.Platformer;
 
 import interactiveSprites.interactiveExample.RPGGame;
 
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import javax.imageio.ImageIO;
 
 import sprites.GeneralSprite;
 import sprites.LevelEditable;
 import stateManagers.StateManager;
 import stateTransitions.ChangeStateTransition;
 import stateTransitions.StateTransition;
 
 import States.StationaryState;
 import States.InteractiveSpriteStates.CarryingState;
 import States.InteractiveSpriteStates.TouchingState;
 
 import com.golden.gamedev.GameObject;
 import com.golden.gamedev.object.Sprite;
 import com.golden.gamedev.object.collision.CollisionGroup;
 import com.sun.corba.se.spi.orbutil.fsm.State;
 
 @SuppressWarnings("serial")
 public class Spring_IS extends GeneralSprite implements LevelEditable, InteractiveSprite {
 	
 	Platformer myGame;
 	String path;
 	String myType;
 	StateManager myStateManager;
 	
 //	
 //	public Spring_IS(BufferedImage bufferedImage, int i, int j, Platformer game) {
 //		super(bufferedImage, i, j);
 //		myType = "spring";
 //		myGame = game;
 //		setStateManager(new StateManager(((Sprite)this), new StationaryState(this)));
 //		myGame.INTERACTIVE_SPRITES.add(this);
 //		
 //		StateTransition collide = new ChangeStateTransition(getStateManager(), "ISCollision", new TouchingState((this)));
 //		c.activate();
 //		
 //		StateTransition carrying = new ChangeStateTransition(getStateManager(),"toCarrying", new CarryingState(this));
 //		collide.activate();
 //	}
 	
 	public Spring_IS() {
 		
 		myType = "spring";
		setStateManager(new StateManager(((Sprite)this), new StationaryState(this)));
 		//myGame.INTERACTIVE_SPRITES.add(this);
 		
 		StateTransition collide = new ChangeStateTransition(getStateManager(), "ISCollision", new TouchingState((this)));
 		collide.activate();
 		
 		StateTransition carrying = new ChangeStateTransition(getStateManager(),"toCarrying", new CarryingState(this));
 		carrying.activate();
 		
 		StateTransition stop = new ChangeStateTransition(getStateManager(),"X", new StationaryState(this));
 		stop.activate();
 
 	}
 
 	public void primaryAction(CollisionGroup c, GeneralSprite s) {
 		
 		if(c.getCollisionSide()== c.BOTTOM_TOP_COLLISION) {
 			s.setVerticalSpeed(-.25);			
 		}
 		if(c.getCollisionSide()== c.TOP_BOTTOM_COLLISION) {
 			
 		}
 		if(c.getCollisionSide()== c.LEFT_RIGHT_COLLISION) {
 			
 		}
 		if(c.getCollisionSide()== c.RIGHT_LEFT_COLLISION) {
 		}
 		
 	}
 	
 	
 	public String getType() {
 		return myType;
 	}
 	
 	public ArrayList<String> writableObject() {
 		ArrayList<String> list= new ArrayList<String>();
 		list.add(this.getClass().toString());
 		list.add(path);
 		list.add(getX() +"");
 		list.add(getY() +"");
 		return list;
 	}
 	
 	public Sprite parse(ArrayList<String> o, Platformer game) {
 			Spring_IS s= new Spring_IS();
 			s.path=o.get(1);
 			s.setX( Double.parseDouble(o.get(2)));
 			s.setY( Double.parseDouble(o.get(3)));
 			File file= new File(path);
 			BufferedImage image;
 			try {
 				image = ImageIO.read(file);
 				s.setImage(image);		} 
 			catch (IOException e) {
 				e.printStackTrace();
 			}
 			return s;
 		
 	}
 
 
 		@Override
 	public Boolean isInstanceOf(ArrayList<String> o) {
 			if (this.getClass().toString().equals(o.get(0))) {
 				return true;
 			}
 			return false;
 	}
 	
 
 }
