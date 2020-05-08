 package sprites;
 
 import StateMachines.CharacterStateMachine;
 import StateMachines.StateMachine;
 
public class TestCharacterWithStates extends PlatformSprite{
 	private StateMachine stateManager;
 	
 	
 	public TestCharacterWithStates()
 	{
 		super();
		stateManager = new CharacterStateMachine(this);
 		
 	}
 	
 	public void update(long elapsedTime)
 	{
 		
 	}
 	public void detectKeyBoard(long elapsedTime)
 	{
 		
 	}
 	
 
 }
