 package se.chalmers.tda367.group15.game.models;
 
 /**
  * Simple class for representing a dummy enemy. This enemy is only intended for
  * testing.
  * 
  * @author simon
  * 
  */
 public class DummyEnemyModel extends AbstractCharacterModel {
 
 	/**
 	 * Creates a new dummy enemy.
 	 */
 	public DummyEnemyModel() {
 		this(64f, 128f);
 	}
 	
 	public DummyEnemyModel(float x, float y){
 		this(x, y, 360*Math.random());
 	}
 	
 	/**
 	 * Creates a new dummy enemy.
 	 * @param x x position of DummyEnemyModel
 	 * @param y y position of DummyEnemyModel
 	 * @param rot angle to face in beginning
 	 */
 	public DummyEnemyModel(float x, float y, double rot) {
 		setX(x);
 		setY(y);
 		setVelocity(0.1f);
 		setWidth(42);
 		setHeight(42);
 		setRotation(rot);
 		setOffset(11);
 		setAlive(true);
 		setHealth(100);
		addWeapon(new EnemyUnarmed());
 		setCurrentWeapon(getWeapons().get(0));
 	}
 }
