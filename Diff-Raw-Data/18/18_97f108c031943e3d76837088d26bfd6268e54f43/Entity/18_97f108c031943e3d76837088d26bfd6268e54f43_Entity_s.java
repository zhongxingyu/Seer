 package server.model;
 
 import java.util.Collection;
 import java.util.LinkedList;
 import java.util.List;
 
 import server.listeners.MoveListener;
 import server.network.GsonExclusionStrategy.noGson;
 import server.util.IdFactory;
 import server.util.Util;
 
 public abstract class Entity {
 
 	private int id;
 	private float x;
 	private float y;
 	protected float height;
 	protected float width;
 	// the direction the entity is looking.
 	protected int dirX = 1;
 	protected int dirY;
 	protected String type = "entity";
 
 	@noGson
 	protected boolean deadlyForPlayer;
 
 	@noGson
 	protected boolean wallHit = false;
 	@noGson
 	protected float speed = 5;
 	@noGson
 	protected boolean collisionResolving = false;
 	@noGson
 	private boolean collisionState;
 	protected int killCount = 0;
 	protected int deathCount = 0;
 
 	@noGson
 	protected boolean tileCollision;
 
 	@noGson
 	private Collection<MoveListener> moveListeners;
 
 	public Entity(int id, float x, float y) {
 		this.id = id;
 
 		this.x = x;
 		this.y = y;
 		this.height = 20f;
 		this.width = 20f;
 		this.deadlyForPlayer = true;
 		this.tileCollision = true;
 
 		this.moveListeners = new LinkedList<MoveListener>();
 
 		this.setType();
 	}
 
 	public Entity(float x, float y) {
 		this(IdFactory.getNextId(), x, y);
 	}
 
 	private void setType() {
 		this.type = this.getClass().getSimpleName().toLowerCase();
 	}
 
 	/**
 	 * 
 	 * @param game
 	 * @param deltax
 	 * @param deltay
 	 * @return what collisions were resolved? for wall hit use: getWallHit
 	 */
 	public List<Entity> moveOnMap(Game game, float deltax, float deltay) {
 		this.wallHit = Util.moveOnMap(game, this, deltax, deltay);
 		List<Entity> overlapping = new LinkedList<Entity>();
 		if (this.collisionResolving)
 			overlapping = this.resolveCollisions(game);
 		else
 			overlapping = Util.getEntitiesOverlapping(game.getAllEntites(),
 					this);
 		return overlapping;
 	}
 
 	protected List<Entity> resolveCollisions(Game game) {
 		List<Entity> overlapping = Util.getEntitiesOverlapping(
 				game.getAllEntites(), this);
 		for (Entity other : overlapping) {
 			if (other.isCollisionResolvingWith(this)) {
 				Util.resolveCollision(game, this, other);
 			}
 		}
 		return overlapping;
 	}
 
 	public boolean getWallHit() {
 		return this.wallHit;
 	}
 
 	public int getId() {
 		return this.id;
 	}
 
 	public void setId(int id) {
 		this.id = id;
 	}
 
 	public float getX() {
 		return x;
 	}
 
 	public float getY() {
 		return y;
 	}
 
 	public float getWidth() {
 		return width;
 	}
 
 	public float getHeight() {
 		return height;
 	}
 
 	public final void setX(float x) {
		if (Float.isNaN(x))
			throw new RuntimeException("x is not a number");
 		this.x = x;
 		positionChanged();
 	}

 	public final void setY(float y) {
		if (Float.isNaN(y))
			throw new RuntimeException("y is not a number");
 		this.y = y;
 		positionChanged();
 	}
 
 	private void positionChanged() {
 		for (MoveListener listener : moveListeners)
 			listener.positionChanged(this);
 	}
 
 	public void setCollisionState(boolean state) {
 		this.collisionState = state;
 	}
 
 	public boolean collisionState() {
 		return this.collisionState;
 	}
 
 	/**
 	 * Defines "intelligence" of this Entity, is called once on every loop.
 	 * 
 	 * @param game
 	 */
 	public abstract void brain(Game game);
 
 	public abstract void hitByBullet(Game game, Bullet bullet);
 
 	@Override
 	public boolean equals(Object o1) {
 		return o1 instanceof Entity && ((Entity) o1).getId() == this.getId();
 	}
 
 	/**
 	 * we override the hashCode, as entities with the same id are equal.
 	 */
 	@Override
 	public int hashCode() {
 		return this.getId();
 	}
 
 	public double getRadius() {
 		return Math.min(this.getWidth() / 2, this.getHeight() / 2);
 	}
 
 	protected boolean isCollisionResolvingWith(Entity other) {
 		return this.collisionResolving;
 	}
 
 	public void setSpeed(float speed) {
 		this.speed = speed;
 	}
 
 	public String getType() {
 		return this.type;
 	}
 
 	public void incrementKillCount() {
 		this.killCount++;
 	}
 
 	protected float euclideanLength(float x, float y) {
 		return (float) Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
 	}
 
 	public float distanceTo(Entity entity) {
 		return euclideanLength(deltaX(entity), deltaY(entity));
 	}
 
 	protected float deltaX(Entity entity) {
 		return entity.getX() - this.getX();
 	}
 
 	protected float deltaY(Entity entity) {
 		return entity.getY() - this.getY();
 	}
 
 	public void addMoveListener(MoveListener listener) {
 		this.moveListeners.add(listener);
 	}
 
 	public boolean isDeadlyForPlayer() {
 		return deadlyForPlayer;
 	}
 
 	public void setDeadlyForPlayer(boolean deadlyForPlayer) {
 		this.deadlyForPlayer = deadlyForPlayer;
 	}
 
 	public boolean isTileCollision() {
 		return tileCollision;
 	}
 
 	public void setTileCollision(boolean tileCollision) {
 		this.tileCollision = tileCollision;
 	}
 
 	public void bleed(Game game) {
 		game.addEntity(new Doodle(this.x, this.y, "blood"));
 	}
 
 }
