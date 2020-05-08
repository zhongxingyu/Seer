 package hudDisplay;
 
 import sprites.TestCharacterWithStates;
 
 import com.golden.gamedev.object.Sprite;
 
 import core.EventManager;
 
 
 public class DecrementBarAction extends HUDAction {
 
 	public DecrementBarAction(TestCharacterWithStates s) {
 		super(s);
 		EventManager em = EventManager.getEventManager();
 		em.registerEventListener("got hit", this);
 	}
 
 	@Override
 	public void actionPerformed(String eventName) {	
		mySprite.changeScore("health", -5);
 		mySprite.changeScore("score", 20);
 	}
 
 	@Override
 	public void actionPerformed(Object object) {
		//what is this for????
		
 	}
 
 
 
 }
