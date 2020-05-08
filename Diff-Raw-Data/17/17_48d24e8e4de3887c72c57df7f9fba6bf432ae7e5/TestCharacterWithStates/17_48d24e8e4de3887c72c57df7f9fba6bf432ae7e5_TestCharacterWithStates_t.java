 package sprites;
 
import com.golden.gamedev.object.Sprite;

 import StateMachines.CharacterStateMachine;
 import StateMachines.StateMachine;
 
public class TestCharacterWithStates extends Sprite{
 	private StateMachine stateManager;
 	
 	
 	public TestCharacterWithStates()
 	{
 		super();
		stateManager = new CharacterStateMachine(((Sprite) this));
 		
 	}
 	
 	public void update(long elapsedTime)
 	{
 		
 	}
 	public void detectKeyBoard(long elapsedTime)
 	{
 		
 	}
 	
 
 }
