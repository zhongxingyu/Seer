 package npc;
 import gameCharacter.CharacterDecorator;
 import gameCharacter.GameCharacter;
 import utils.Location;
 import ai.ScriptedMovementAI;
 import app.RPGame;
 import dialogue.AbstractDialogue;
 import dialogue.AbstractDialogue.DialogueObject;
 import dialogue.SimpleDialogue;
 
 public class NPC extends CharacterDecorator{
 
 	/**
 	 * Computer-generated serial ID number
 	 */
 	private static final long serialVersionUID = -5360689062786017503L;
 	protected AbstractDialogue dialogue;
 	private boolean alive;
 	private boolean canDie;
 
 	/**
 	 * constructs a non-player character based on the information at the configuration URL given
 	 * @param game
 	 * @param loc
 	 * @param configURL
 	 */
	public NPC(RPGame game, GameCharacter character) {
 		super(character);
 		character.setDecorator(this);
 	}
 	
 	private void constructActions(String json) {
 //		Gson gson = new Gson();
 //		JsonUtil.JSONNpcActions actions = gson.fromJson(json,
 //				JsonUtil.JSONNpcActions.class);
 //		if (actions.talking == null)
 //			this.dialogue = new NullDialogue();
 //		this.getActions().add("talking",
 //				new Talking(new Talk(this.getActions(), actions.talking)));
 	}
 	
 	public void setDialogue (AbstractDialogue dialogue){
 		this.dialogue = dialogue;
 	}
 	
 	public String getTalk(){
 		return dialogue.getCurrentLine();
 	}
 	
 	public void setAlive(boolean alive){
 		this.alive = alive;
 	}
 	
 	public boolean isAlive(){
 		return alive;
 	}
 	
 	public void update(long elapsed){
 		super.update(elapsed);
 	}
 	
 	public NPC setCharacter(GameCharacter character){
 		this.character = character;
 		return this;
 	}
 
 }
