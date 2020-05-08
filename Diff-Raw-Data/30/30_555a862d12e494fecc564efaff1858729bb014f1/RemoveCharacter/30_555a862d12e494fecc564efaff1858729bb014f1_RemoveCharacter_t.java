 package DD.GMToolsBox;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.StateBasedGame;
 
 import DD.Character.Abilities.TargetAbility;
 import DD.Character.CharacterSheet.CharacterSheet;
 import DD.CombatSystem.CombatSystem;
 import DD.CombatSystem.Interpreter.Standard.I_StandardAttack;
 import DD.CombatSystem.Interpreter.System.I_RemoveCharacter;
 import DD.CombatSystem.TargetingSystem.TargetingSystem;
 import DD.Message.ChooseTargetMessage;
 import DD.Message.CombatMessage;
 import DD.Message.TargetSelectedMessage;
 import DD.Network.Network;
 import DD.SlickTools.DDImage;
 
 /*****************************************************************************************************
  * RemoveCharacter will give the GM the ability to remove a Character from the board (ie form play) and
  * then add the Character to the GMTools holder.
  * 
  * TODO: VERY IMPORTANT!! THIS METHOD STILL NEEDS A BUTTON TO MARK IT AS DONE AND THEN NOT DONE AGAIN(So it can be used again)!
  * 
  * @author Carlos Vasquez
  ******************************************************************************************************/
 
 public class RemoveCharacter extends TargetAbility
 {
 	/************************************ Class Attributes *************************************/
 	private GMToolsBox gmt;			/* Give PlaceCharacter access tot he GMToolBox */
 	private DDImage cancel;
 	private static Input mouse = new Input(650);
 	private String mousePos;
 	
 	/************************************ Class Methods *************************************/
 	public RemoveCharacter(int id, GMToolsBox gmt) 
 	{
 		super(id, CombatSystem.ActionType.SYSTEM, CombatSystem.Action.REMOVE_CHARACTER, "Remove Character", "Removes character from the map.");
 		this.gmt = gmt;
 		this.image = new DDImage("Images/GMTools/RemoveCharacter.png");
 		cancel = new DDImage("Images/GMTools/Cancel.png");
 	} /* end RemoveCharacter constructor */
 
 	@Override
 	protected void action() throws SlickException 
 	{ 
 		ChooseTargetMessage tcm = new ChooseTargetMessage
 				(
 					TargetingSystem.TargetCount.SINGLE,
 					TargetingSystem.TargetShape.ALL,
 					TargetingSystem.TargetSelection.SELECTED,
 					false,
 					null,
 					0,
 					this
 				);
 		ts.chooseTarget(tcm);
 		
 	} /* end action method */
 	
 	public void cancel()
 	{
 		ts.clearTargets();
 	} /* end cancel method */
 
 	@Override
 	public void obtainTarget(TargetSelectedMessage tsm) throws SlickException 
 	{
 		if (tsm.getTargets() != null)
 		{
 			/* We can only remove an existing character */
 			Integer[] body = new Integer[I_StandardAttack.BODY_SIZE];
 			body[I_RemoveCharacter.CHARACTER_ID] = tsm.getTargets()[0].getCharacterID(); // There should only be one target
 			body[I_RemoveCharacter.POS_X] = tsm.getPosition().x;
 			body[I_RemoveCharacter.POS_Y] = tsm.getPosition().y;
 			CombatMessage cm = new CombatMessage
 					(
 						null,
 						null,
 						CombatSystem.ActionType.SYSTEM,
 						CombatSystem.Action.REMOVE_CHARACTER,
 						body
 					);
 			
 			/* Place the character into the holder. */
 			CharacterSheet sheet = cs.getCharacter(cm.getBody()[I_RemoveCharacter.CHARACTER_ID]).getSheet();
 			/* Note that the GM will only ever have mobs and no other player will have mobs */
 			if(sheet.getNetID() == Network.GM_USER_ID) gmt.addCharacter(GMToolsBox.Holder.MOB, sheet);
 			else gmt.addCharacter(GMToolsBox.Holder.PLAYER, sheet);
 			gmt.removeCharacterID(cm.getBody()[I_RemoveCharacter.CHARACTER_ID]);
 			
 			this.sendToInterpreter(cm);
 			done(null);
			done = false;
 		} /* end if */
 		
 	} /* end obtainTarget method */
 	
 	@Override
 	public void update(GameContainer gc, StateBasedGame sbg, int delta) 
 	{
 		int posX = mouse.getMouseX();
     	int posY = mouse.getMouseY();
 		mousePos = "Mouse position: " + posX + " " + posY;
 		
 		//Remove Button
 		if((posX > 950 && posX < 950 + this.image.getWidth()) && (posY > 40 && posY < (40 + this.image.getHeight()))) {
     		//if you click on the button
     		if(gc.getInput().isMousePressed(gc.getInput().MOUSE_LEFT_BUTTON)) {
     			try {
 					action();
 				} catch (SlickException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
     			System.out.println("Remove Character");
     		}
     	}
 		
 		//Cancel Button
 		if((posX > 1145 && posX < 1145 + cancel.getWidth()) && (posY > 40 && posY < (40 + cancel.getHeight()))) {
     		//if you click on the button
     		if(gc.getInput().isMousePressed(gc.getInput().MOUSE_LEFT_BUTTON)) {
     			cancel();
     			System.out.println("Cancel");
     		}
     	}
 		
 	} /* end update method */
 	
 	@Override
 	public void render(GameContainer gc, StateBasedGame sbg, Graphics gr) 
 	{
 		gr.drawString(mousePos, 0, 0);
 		this.image.draw(950, 40);
 		cancel.draw(1145, 40);
 		
 	} /* end render method */
 
 } /* end RemoveCharacter class */
