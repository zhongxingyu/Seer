 package npc;
 import gameCharacter.GameCharacter;
 import utils.Location;
 import app.RPGame;
 import dialogue.AbstractDialogue;
 import dialogue.AbstractDialogue.DialogueObject;
 
 public class NPC extends GameCharacter{
 
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
 	public NPC(RPGame game, Location loc, String configURL) {
 		super(game, loc, configURL);
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
 	
	public String getTalk(DialogueObject choice){
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
 
 }
