 package ch.kanti_wohlen.asteroidminer.entities.asteroids;
 
 import ch.kanti_wohlen.asteroidminer.TaskScheduler;
 import ch.kanti_wohlen.asteroidminer.Textures;
 import ch.kanti_wohlen.asteroidminer.entities.Damageable;
 import ch.kanti_wohlen.asteroidminer.entities.Entity;
 import ch.kanti_wohlen.asteroidminer.entities.EntityType;
 import ch.kanti_wohlen.asteroidminer.entities.sub.HealthBar;
 import ch.kanti_wohlen.asteroidminer.powerups.PowerUpLauncher;
 
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.math.MathUtils;
 import com.badlogic.gdx.math.Rectangle;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.BodyDef;
 import com.badlogic.gdx.physics.box2d.CircleShape;
 import com.badlogic.gdx.physics.box2d.FixtureDef;
 import com.badlogic.gdx.physics.box2d.World;
 import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
 
 public class StoneAsteroid extends Entity implements Damageable {
 
 	public static final int HEALTH_PER_SIZE = 20;
 	public static final float STONE_ASTEROID_MIN_SIZE = 0.75f;
 	private static final float POWER_UP_SPAWN_CHANCE = 0.075f;
 
 	private final HealthBar healthBar;
 	private final float currentRadius;
 	private final float renderScale;
 	private final int maxHealth;
 
 	private int health;
 
 	public StoneAsteroid(World world, Vector2 location, float radius) {
 		this(world, location, radius, null);
 	}
 
 	public StoneAsteroid(World world, Vector2 location, float radius, Vector2 velocity) {
 		super(world, createBodyDef(location, velocity), createCircle(radius));
 		maxHealth = (int) (radius * HEALTH_PER_SIZE);
 		health = maxHealth;
 		healthBar = new HealthBar(maxHealth);
 		currentRadius = radius;
 		renderScale = (radius * BOX2D_TO_PIXEL * 2f) / Textures.ASTEROID.getRegionWidth();
 	}
 
 	@Override
 	public void render(SpriteBatch batch) {
 		Sprite s = Textures.ASTEROID;
 		positionSprite(s);
 		s.setScale(renderScale);
 		s.draw(batch);
 
 		final float healthBarX = s.getX() + s.getWidth() * 0.025f;
 		final float healthBarY = s.getY() + s.getHeight() * 0.6f + currentRadius * BOX2D_TO_PIXEL;
 		healthBar.render(batch, health, new Vector2(healthBarX, healthBarY));
 	}
 
 	@Override
 	public boolean isRemoved() {
 		return super.isRemoved() || health == 0;
 	}
 
 	@Override
 	public EntityType getType() {
 		return EntityType.ASTEROID;
 	}
 
 	@Override
 	public Rectangle getBoundingBox() {
		final float d = currentRadius * 2f;
		final Rectangle rect = new Rectangle(0f, 0f, d, d);
 		rect.setCenter(body.getPosition());
 		return rect;
 	}
 
 	public int getHealth() {
 		return health;
 	}
 
 	public void setHealth(int newHealth) {
 		if (newHealth != health) {
 			health = MathUtils.clamp(newHealth, 0, maxHealth);
 			healthBar.resetAlpha();
 
 			if (health == 0) {
 				final World w = body.getWorld();
 				final float nextRadius = currentRadius / 2f;
 
 				if (nextRadius <= STONE_ASTEROID_MIN_SIZE) {
 					if (MathUtils.random() > POWER_UP_SPAWN_CHANCE) return;
 					final World world = body.getWorld();
 					final Vector2 loc = body.getPosition();
 					PowerUpLauncher pul = new PowerUpLauncher(world, loc);
 					TaskScheduler.INSTANCE.runTask(pul);
 					return;
 				}
 
 				SplitAsteroidLauncher l = new SplitAsteroidLauncher(w, body.getPosition(), body.getLinearVelocity(),
 						nextRadius, body.getMass());
 				TaskScheduler.INSTANCE.runTask(l);
 			}
 		}
 	}
 
 	public void heal(int healingAmoung) {
 		setHealth(health + healingAmoung);
 	}
 
 	public void damage(int damageAmount) {
 		setHealth(health - damageAmount);
 	}
 
 	public void kill() {
 		setHealth(0);
 	}
 
 	private static BodyDef createBodyDef(Vector2 position, Vector2 velocity) {
 		final BodyDef bodyDef = new BodyDef();
 		bodyDef.type = BodyType.DynamicBody;
 		bodyDef.position.set(position);
 		bodyDef.angle = MathUtils.random(2 * MathUtils.PI);
 		if (velocity != null) {
 			bodyDef.linearVelocity.set(velocity);
 		}
 		bodyDef.gravityScale = 0.1f;
 
 		return bodyDef;
 	}
 
 	private static FixtureDef createCircle(float radius) {
 		final FixtureDef fixture = new FixtureDef();
 		fixture.density = 100f;
 		fixture.restitution = 0.7f;
 		final CircleShape cs = new CircleShape();
 		cs.setRadius(radius);
 		fixture.shape = cs;
 		return fixture;
 	}
 
 	private class SplitAsteroidLauncher implements Runnable {
 
 		private final World w;
 		private final Vector2 position;
 		private final Vector2 velocity;
 		private final float radius;
 		private final float mass;
 
 		public SplitAsteroidLauncher(World world, Vector2 pos, Vector2 vel, float r, float m) {
 			w = world;
 			position = pos;
 			velocity = vel;
 			radius = r;
 			mass = m;
 		}
 
 		@Override
 		public void run() {
 			final float r1 = radius * (0.75f + MathUtils.random() * 0.5f);
 			final float r2 = radius * (0.75f + MathUtils.random() * 0.5f);
 			final float rot1 = 75f + 30f * MathUtils.random();
 			final float rot2 = 255f + 30f * MathUtils.random();
 
 			Vector2 loc1 = position.cpy().add(velocity.cpy().nor().rotate(rot1));
 			Vector2 loc2 = position.cpy().add(velocity.cpy().nor().rotate(rot2));
 
 			StoneAsteroid a1 = new StoneAsteroid(w, loc1, r1, velocity);
 			StoneAsteroid a2 = new StoneAsteroid(w, loc2, r2, velocity);
 
 			final float mass1 = a1.getPhysicsBody().getMass();
 			final float mass2 = a2.getPhysicsBody().getMass();
 			final Vector2 impulse = new Vector2(velocity);
 			final Vector2 i1 = impulse.cpy().div(mass).scl(5f * mass1 * mass1).rotate(90f);
 			final Vector2 i2 = impulse.cpy().div(mass).scl(5f * mass2 * mass2).rotate(270f);
 
 			a1.getPhysicsBody().applyLinearImpulse(i1, position, true);
 			a2.getPhysicsBody().applyLinearImpulse(i2, position, true);
 		}
 	}
 }
