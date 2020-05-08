 package characters;
 
 import java.util.Random;
 import utility.interfaces.*;
 import utility.Direction;
 import characters.actions.*;
 import java.util.ArrayDeque;
 import java.util.Queue;
 import utility.Position;
 
 public abstract class GameCharacter implements Movable, Damageable  {
 	
 	protected final class Attributes {
 		public short strength;
 		public short dexterity;
 		public short intellegence;
 		public short armor;
 	}
 	
 	protected Queue<GameAction> gameActions = null;
 	
 	public Position position= null;
 	public char charOnMap = '?';
 	
 	public short currentHP;
 	public short maxHP;
 	public double speed = 1;
 	public String name;
 	
 	protected double attackRate;
 	protected Attributes attributes;
 	protected boolean canTakeDamage;
 	protected int actionPointsOnCon;
 	protected int currentActionPoints;
 	protected int maximumActionPoints;
 
 	
 	GameCharacter(Position pos) {
 		position = pos;
 		attributes = new Attributes();
 		gameActions = new ArrayDeque<GameAction>();
 	}
 	
 	public void move(Direction there) {
 			addAction(new MovementGameAction(this, there));
 	}
 	
 	public boolean hasAction() {
 		return !gameActions.isEmpty();
 	}
 	
 	public void addAction(GameAction action) {
 		gameActions.add(action);
 	}
 	
 	public void performAction() {
 //		GameAction current = gameActions.peek();
 //		if (current.actionPointsLeft() < 0)
 //			gameActions.poll().execute();
 //		else {
 //			current.setActionPointsLeft(current.actionPointsLeft() - currentActionPoints);
 //		}
 		if (gameActions.isEmpty()) return;
 		gameActions.poll().execute();
 	}
 	
 	public void breakActionQueue() {
 		gameActions = new ArrayDeque<GameAction>();
	} 
 	
 	public void hit(GameCharacter mob) {
 	/*  TODO
 	 *  Hit should generate integer which is calculated,
 	 *  depending on attackers attributes and random number.
 	 *  This integer is passed to takeAHit method later on,
 	 *  separately from this method.
 	 */
 		Random rand = new Random();
 		int damage = rand.nextInt((int) (attributes.strength * attackRate));
 		mob.takeDamage(damage);
 	}
 	
 	public boolean canTakeDamage() {
 		return canTakeDamage;
 	}
 	
 	public void takeDamage(int damage) {
 	/* TODO
 	 * if takes 0 as arguments - attacker missed,
 	 * otherwise it should apply armor coefficient to damage and then subtract it from currenthp.	
 	 */
 		Random rand = new Random();
 		int chancetoevade = rand.nextInt(50) + attributes.dexterity;
 		if (chancetoevade < 50)
 			currentHP -= (damage - attributes.armor);
 	}
 }
