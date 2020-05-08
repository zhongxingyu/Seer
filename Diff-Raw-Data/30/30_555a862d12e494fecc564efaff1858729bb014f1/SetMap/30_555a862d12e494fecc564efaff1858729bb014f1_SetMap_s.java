 package DD.GMToolsBox;
 
 import org.newdawn.slick.SlickException;
 
 import DD.Character.Abilities.Ability;
 import DD.Character.CharacterSheet.CharacterSheet;
 import DD.CombatSystem.CombatSystem;
 import DD.CombatSystem.CombatSystem.Action;
 import DD.CombatSystem.CombatSystem.ActionType;
 import DD.CombatSystem.Interpreter.Standard.I_StandardAttack;
 import DD.CombatSystem.Interpreter.System.I_PlaceCharacter;
 import DD.CombatSystem.Interpreter.System.I_SetMap;
 import DD.MapTool.Map;
 import DD.Message.CombatMessage;
 
 /*****************************************************************************************************
  * SetMap will be used by the GM to load up a new map and place pre-placed characters (mobs) onto the
  * map;
  * 
  * @author Carlos Vasquez
  ******************************************************************************************************/
 
 public class SetMap extends Ability
 {
 	/************************************ Class Attributes *************************************/
 	private Map map;	/* characters data */
 	
 	/************************************ Class Methods *************************************/
 	public SetMap(int id) 
 	{
 		this(id, null);
 	} /* end constructor */
 	
 	public SetMap(int id, Map map) 
 	{
 		super(id, ActionType.SYSTEM, Action.SET_MAP, "Set Map", "Gives map to CombatSystem");
 		this.map = map;
 	} /* end constructor */
 
 	@Override
 	protected void action() throws SlickException 
 	{
 		if(map != null)
 		{
 			Object[] body = new Object[I_SetMap.BODY_SIZE];
 			body[I_SetMap.MAP] = (Object)map;
 			CombatMessage cm = new CombatMessage
 					(
 						null,
 						null,
 						CombatSystem.ActionType.SYSTEM,
						CombatSystem.Action.PLACE_CHARACTER,
 						body
 					);
 			sendToInterpreter(cm);
 		} /* end if */
 		
 		done();
 		
 	} /* end action method */
 	
 	/****************************************************************************************
 	 ************************************ Setter Methods ************************************
 	 ****************************************************************************************/
 	public void setMap(Map map)
 	{
 		this.map = map;
 	} /* end setMap method */
 	
 } /* end setMap class */
