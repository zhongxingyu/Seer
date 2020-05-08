 package spaceshooters.entities;
 
 import org.newdawn.slick.SlickException;
 
 import pl.matterross.engine.gfx.Texture;
 import pl.matterross.engine.math.AABB;
 import pl.matterross.engine.math.Vector2f;
 import spaceshooters.aurora.entity.EntityType;
 import spaceshooters.aurora.entity.IEntity;
 import spaceshooters.aurora.entity.IEntityType;
 import spaceshooters.aurora.level.ILevel;
 import spaceshooters.gfx.TextureLoader;
 import spaceshooters.sfx.Sound;
 import spaceshooters.sfx.SoundEngine;
 
 /**
  * Base class for all entities.
  * 
  * @author Mat
  * 
  */
 public abstract class Entity implements IEntity {
 	
 	protected Vector2f position;
 	protected float velocity;
 	protected int width, height, health;
 	protected boolean alive = true;
 	protected final IEntityType entityType;
 	protected Texture texture;
 	protected ILevel level;
 	
 	public Entity(ILevel level, float startX, float startY, IEntityType entityType) {
 		this.level = level;
 		this.entityType = entityType;
 		texture = TextureLoader.getTexture(this.getTextureFile());
 		position = new Vector2f(startX, startY);
 		width = texture.getWidth();
 		height = texture.getHeight();
 		health = 1;
 		velocity = 0.2F;
 	}
 	
 	/**
 	 * Called when entity collides with different entity.
 	 * 
 	 * @param entity
 	 *            The entity with which this entity collided.
 	 */
 	public abstract void onCollide(Entity entity) throws SlickException;
 	
 	/**
 	 * Gets the name of the texture of this entity.
 	 * 
 	 * @return The name of this entity's texture, ex. "example.png";
 	 */
 	public abstract String getTextureFile();
 	
 	@Override
 	public void render() {
 		texture.render((int) position.x, (int) position.y);
 	}
 	
 	@Override
 	public void onDeath() {
 		SoundEngine.getInstance().play(Sound.EXPLOSION, 1.0f, 0.3f);
 		//level.spawnParticle(new Explosion(this.getX() - 13, this.getY() - 12));
 	}
 	
 	// TODO: Rework collisions for 1.3 Alpha, along the rest of engine.
 	protected void collideCheck(EntityType type) {
 		for (IEntity e : level.getEntities()) {
 			if (e.getEntityType() == type) {
 				//if (!e.equals(this) && this.collides(e)) {
 				//this.onCollide(e);
 				//e.onCollide(this);
 				//	}
 			}
 		}
 	}
 	
 	protected void collideCheck(EntityType[] types) {
 		for (EntityType type : types) {
 			this.collideCheck(type);
 		}
 	}
 	
 	/**
 	 * Gets entity's texture.
 	 * 
 	 * @return The texture.
 	 */
 	@Override
 	public Texture getTexture() {
 		return texture;
 	}
 	
 	/**
 	 * Gets entity's current level.
 	 * 
 	 * @return The level in which the entity is currently.
 	 */
 	@Override
 	public ILevel getLevel() {
 		return level;
 	}
 	
 	/**
 	 * Gets entity's type.
 	 * 
 	 * @return The type of this entity.
 	 */
 	@Override
 	public IEntityType getEntityType() {
 		return entityType;
 	}
 	
 	/**
 	 * Gets entity's velocity.
 	 * 
 	 * @return The velocity of this entity.
 	 */
 	public float getVelocity() {
 		return velocity;
 	}
 	
 	/**
 	 * Gets entity's x position.
 	 * 
 	 * @return The x coordinate of this entity.
 	 */
 	@Override
 	public float getX() {
 		return position.x;
 	}
 	
 	/**
 	 * Gets entity's y position.
 	 * 
 	 * @return The y coordinate of this entity.
 	 */
 	@Override
 	public float getY() {
 		return position.y;
 	}
 	
 	@Override
 	public Vector2f getPosition() {
 		return position;
 	}
 	
 	/**
 	 * Gets entity's width
 	 * 
 	 * @return The width of this entity.
 	 */
 	public int getWidth() {
 		return width;
 	}
 	
 	/**
 	 * Gets entity's height.
 	 * 
 	 * @return The height of this entity.
 	 */
 	public int getHeight() {
 		return height;
 	}
 	
 	/**
 	 * Checks if entity's still alive.
 	 * 
 	 * @return True or false.
 	 */
 	@Override
 	public boolean isAlive() {
 		return alive;
 	}
 	
 	/**
 	 * Sets entity's x position.
 	 * 
 	 * @param newX
 	 *            The new x coordinate.
 	 */
 	@Override
 	public void setX(float newX) {
 		position.x = newX;
 	}
 	
 	/**
 	 * Sets entity's y position.
 	 * 
 	 * @param newY
 	 *            The new y coordinate.
 	 */
 	@Override
 	public void setY(float newY) {
 		position.y = newY;
 	}
 	
 	@Override
 	public void setPosition(Vector2f pos) {
 		position.x = pos.x;
 		position.y = pos.y;
 	}
 	
 	@Override
 	public void setAlive(boolean alive) {
 		this.alive = alive;
 	}
 	
 	@Override
 	public AABB getBoundingBox() {
 		return new AABB((int) position.x, (int) position.y, width, height);
 	}
 	
 	@Override
	public void update(int delta) {
		// TODO:
	}
	
	@Override
 	public void onSpawn() {
 	}
 }
