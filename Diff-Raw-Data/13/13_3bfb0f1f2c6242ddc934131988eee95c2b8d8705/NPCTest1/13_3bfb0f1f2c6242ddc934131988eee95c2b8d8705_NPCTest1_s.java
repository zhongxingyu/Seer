 package npc;
 
 import gameCharacter.GameCharacter;
 import dialogue.AbstractDialogue.DialogueObject;
 import dialogue.SimpleDialogue;
 
 import state.MovingAttackingState;
 
 import ai.SquareMovementAI;
 
 public class NPCTest1 extends NPC {
 
 	private boolean hasTalked;
 
 	/**
 	 * Computer-generated serial ID number
 	 */
 	@SuppressWarnings("unused")
 	private static final long serialVersionUID = 4483591744499315422L;
 
 	public NPCTest1(GameCharacter character) {
 		super(character);
 
 		int[][] testArray = new int[][] { { 1, 2100 }, { 2, 2000 } };
 		this.setCurrentState(new MovingAttackingState(new SquareMovementAI(
 				this.character.getGame(), this.getCharacter(), 300), null));
 		dialogue = new SimpleDialogue("rsc/savedmaps/npc1.txt");
 	}
 
 	/**
 	 * 
 	 * @return
 	 */
 	public String getTalk(DialogueObject choice) {
		if (!hasTalked) {
			hasTalked = true;
		} else {
			dialogue.goToNextLine(new SimpleDialogue.SimpleDialogueObject());
		}
 		return dialogue.getCurrentLine();
 	}
 
 	public static class NPCTest1Factory extends NPCFactory {
 
 		public NPCTest1Factory() {
 		};
 
 		@Override
 		public boolean isThisType(String npcName) {
 			return npcName.equals("NPCTest1");
 		}
 
 		@Override
 		public NPC constructNPC(GameCharacter gameChar) {
 			return new NPCTest1(gameChar);
 		}
 
 	}
 
 }
