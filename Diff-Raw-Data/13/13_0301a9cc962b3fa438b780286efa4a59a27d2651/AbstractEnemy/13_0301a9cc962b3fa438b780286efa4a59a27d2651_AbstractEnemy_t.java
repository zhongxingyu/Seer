 package enemy;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import counters.EnemyHealth;
 
 import state.State;
 import state.TalkingState;
 
 import state.MovingAttackingState;
 import ai.GreedyPathFindingAI;
 import ai.SimpleAttackAI;
 
 import app.RPGame;
 import attacks.AbstractAttack;
 import gameCharacter.Attackable;
 import gameCharacter.CharacterDecorator;
 import gameCharacter.GameCharacter;
 
 public abstract class AbstractEnemy extends CharacterDecorator implements Attackable{
 
 	protected HashMap<String, AbstractAttack> attacks = new HashMap<String, AbstractAttack>();
 	public static ArrayList<EnemyFactory> enemyFactories = new ArrayList<EnemyFactory>();
 	protected String configURL;
 	private boolean alive = true;
 	protected RPGame game;
 
 	private State currentState;
 	protected int moneyValue;
 
 	public AbstractEnemy(GameCharacter character) {
 		super(character);
 		character.setDecorator(this);
 	}
 	
 	static{
 		enemyFactories.add(new TestEnemy.TestEnemyFactory());
 	}
 
 	public void initResources() {
 		//String json = JsonUtil.getJSON(configURL);
 		//initActions(json);
 		initAttacks();
 		getCharacter().getCounters().add("health",
 				new EnemyHealth(getCharacter().getCounters(), 2));
 		initAI();
 	}
 
 	
 	public void initAI()
 	{
 		setCurrentState(new MovingAttackingState(new GreedyPathFindingAI(game, this.getCharacter()), new SimpleAttackAI(game,this)));
 	}
 
 	protected abstract void initAttacks();
 	protected abstract void initActions(String json);
 
 	public void update(long elapsedTime) {
 		currentState.update(elapsedTime, this);
 	}
 
 	public HashMap<String, AbstractAttack> getAttacks() {
 		return attacks;
 	}
 
 	public void setCurrentState(State s) {
 		currentState = s;
 	}
 
 	@Override
 	public int getHealth() {
 		return character.getCounters().get("health").getCount();
 	}
 
 	@Override
 	public void addToHealth(int delta) {
 		character.getCounters().get("health").increase(delta);
 		if(character.getCounters().get("health").isEmpty())
 			alive = false;
 	}
 	
 	public static AbstractEnemy createEnemy(String enemyName, RPGame game, GameCharacter gameChar, String configURL){
 
 		for (EnemyFactory fac : enemyFactories){
 			if (fac.isThisType(enemyName))
 				return fac.constructEnemy(game,gameChar,configURL);
 		}
 		throw new RuntimeException("Given name of NPC not recognized");
 	}
 
 	@Override
 	public void setAlive(boolean alive) {
 		this.alive = alive;
 	}
 
 	@Override
 	public boolean isAlive() {
 		return alive;
 	}
 
	public void uponDeath() {
		//TODO: enable when money item is ready
		//getCharacter().getGame().getPlayer().getCharacter().getInventory().get("money").add(moneyValue);
	}
 
 }
