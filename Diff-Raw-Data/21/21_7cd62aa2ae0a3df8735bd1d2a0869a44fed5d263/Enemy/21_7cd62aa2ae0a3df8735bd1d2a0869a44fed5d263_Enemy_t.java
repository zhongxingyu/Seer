 package model.sprites;
 
 import java.util.List;
 
 import model.geometrical.CollisionBox;
 import model.geometrical.Position;
 import model.geometrical.Rectangle;
 import model.items.Item;
 import model.items.weapons.Weapon;
 
 public class Enemy implements Sprite{
 
 	private State state;
 	private float direction;
 	private float speed;
 	private Weapon weapon;
 	private int health;
 	private CollisionBox collisionBox;
 	private CollisionBox hitBox;
 	private List<Position> pathfindingList;
 	private int pathfindingListIndex;
 
 	/**
 	 * Creates a new enemy.
 	 * @param position the position of the enemy.
 	 * @param speed the speed of the enemy.
 	 * @param weapon the weapon of the enemy.
 	 * @param health the health of the ememy.
 	 */
 	protected Enemy(Position position, float speed, Weapon weapon, int health){
 		this.setState(Sprite.State.STANDING);
 		this.speed = speed;
 		this.weapon = weapon;
 		this.health = health;
 		collisionBox = new Rectangle(0, 0, 0.7f, 0.7f);
 		hitBox = new Rectangle(0, 0, 0.5f, 0.5f);
 		this.setPosition(position);
 	}
 	
 	/**
 	 * Gives the position of the enemy.
 	 * @return the position of the enemy.
 	 */
 	public Position getPosition() {
 		return hitBox.getPosition();
 	}
 	
 	/**
 	 * Gives the speed of the enemy.
 	 * @return the speed of the enemy.
 	 */
 	public float getSpeed() {
 		return speed;
 	}
 	
 	@Override
 	public Position getProjectileSpawn() {
 		return new Position(getX() + getHitBox().getWidth()/2 + (float)(Math.cos(direction)*0.5f), 
 				getY() + getHitBox().getHeight()/2 - (float)(Math.sin(direction)*0.5f));
 	}
 
 	/**
 	 * Sets the center position.
 	 */
 	@Override
 	public void setPosition(Position p) {
		this.hitBox.setPosition(new Position(p.getX() - hitBox.getWidth()/2, p.getY() - hitBox.getHeight()/2));
 	}
 	
 	/**
 	 * Return the PathfindlingListIndex.
 	 * @return the pathfindlingList
 	 */
 	public List<Position> getPathfindingList(){
 		return this.pathfindingList;
 	}
 	
 	/**
 	 * Set the pathfindingList.
 	 * @param pathfindingList
 	 */
 	public void setPathfindingList(List<Position> pathfindingList){
 		this.pathfindingList = pathfindingList;
 	}
 	
 	/**
 	 * Return the PathfindlingListIndex.
 	 * @return PathfindlingListIndex
 	 */
 	public int getPathfindingListIndex(){
 		return this.pathfindingListIndex;
 	}
 	
 	/**
 	 * Set the PathfindlingListIndex.
 	 * @param PathfindlingListIndex
 	 */
 	public void setPathfindingListIndex(int pathfindingListIndex){
 		this.pathfindingListIndex = pathfindingListIndex;
 	}
 
 
 	@Override
 	public void moveXAxis(){
 		if(this.state == Sprite.State.RUNNING) {
 			hitBox.setPosition(new Position(hitBox.getPosition().getX() + (float)(Math.cos(direction)*speed), 
 					hitBox.getPosition().getY()));
 		}else if(this.state == Sprite.State.WALKING){
 			hitBox.setPosition(new Position(hitBox.getPosition().getX() + (float)(Math.cos(direction)*speed/3), 
 					hitBox.getPosition().getY()));
 		}
 	}
 
 	@Override
 	public void moveYAxis(){
 		if(this.state == Sprite.State.RUNNING) {
 			hitBox.setPosition(new Position(hitBox.getPosition().getX(), 
 					hitBox.getPosition().getY() - (float)(Math.sin(direction)*speed)));
 		}else if(this.state == Sprite.State.WALKING){
 			hitBox.setPosition(new Position(hitBox.getPosition().getX(), 
 					hitBox.getPosition().getY() - (float)(Math.sin(direction)*speed/3)));
 		}
 	}
 	
 
 	@Override
 	public float getDirection() {
 		return direction;
 	}
 	
 	@Override
 	public int getHealth() {
 		return health;
 	}
 
 	@Override
 	public void setDirection(float direction) {
 		this.direction = direction;
 	}
 
 	/**
 	 * Gives the x-coordinate.
 	 * @return the x-coordinate.
 	 */
 	public float getX() {
 		return this.hitBox.getPosition().getX();
 	}
 
 	/**
 	 * Gives the y-coordinate.
 	 * @return the y-coordinate.
 	 */
 	public float getY() {
 		return this.hitBox.getPosition().getY();
 	}
 
 	/**
 	 * Sets the x-coordinate.
 	 * @param x the x-coordinate.
 	 */
 	public void setX(float x) {
 		this.setPosition(new Position(x, this.getY()));
 	}
 
 	/**
 	 * Sets the y-coordinate.
 	 * @param y the y-coordinate.
 	 */
 	public void setY(float y) {
 		this.setPosition(new Position(this.getX(),y));
 	}
 
 	@Override
 	public Weapon getActiveWeapon() {
 		return weapon;
 	}
 	
 	@Override
 	public void reduceHealth(int damage) {
 		health = health - damage;
 	}
 	
 	/**
 	 * Set a list which the enemy will move along.
 	 * @param list the list of Positions the enemy will follow.
 	 */
 	public void setWay(List<Position> list){
 		this.state = State.RUNNING;
 		pathfindingListIndex = 0;
 		this.pathfindingList = list;
 	}
 
 	@Override
 	public Position getCenter() {
 		return new Position(getX() + getHitBox().getWidth()/2, getY() + getHitBox().getHeight()/2);
 	}
 
 	@Override
 	public void setState(State state) {
 		this.state = state;
 	}
 
 	@Override
 	public State getState() {
 		return this.state;
 	}
 
 	@Override
 	public boolean pickUpItem(Item i) {
 		return false;
 	}
 
 	@Override
 	public CollisionBox getMoveBox() {
 		this.collisionBox.setPosition(new Position(this.getX() + (hitBox.getWidth() - collisionBox.getWidth()) / 2
 				, this.getY() + (hitBox.getHeight() - collisionBox.getHeight()) / 2));
 		return this.collisionBox;
 	}
 	
 	@Override
 	public CollisionBox getHitBox() {
 		return this.hitBox;
 	}
 
 	@Override
 	public void moveBack() {
 		this.hitBox.moveBack();
 		if(this.state == Sprite.State.WALKING){
 			direction = (float) ((direction+Math.PI)%(2*Math.PI));
 		}
 	}
 
 }
