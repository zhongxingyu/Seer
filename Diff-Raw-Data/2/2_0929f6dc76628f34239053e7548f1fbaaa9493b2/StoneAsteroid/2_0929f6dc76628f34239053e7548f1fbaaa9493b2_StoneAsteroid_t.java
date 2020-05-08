 package ch.kanti_wohlen.asteroidminer.entities.asteroids;
 
 import ch.kanti_wohlen.asteroidminer.Player;
 import ch.kanti_wohlen.asteroidminer.TaskScheduler;
 import ch.kanti_wohlen.asteroidminer.Textures;
 import ch.kanti_wohlen.asteroidminer.entities.Damageable;
 import ch.kanti_wohlen.asteroidminer.entities.Entity;
 import ch.kanti_wohlen.asteroidminer.entities.EntityType;
 import ch.kanti_wohlen.asteroidminer.entities.bars.HealthBar;
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
 	private static final float POWER_UP_SPAWN_CHANCE = 0.05f;
 	private static final int KILL_SCORE = 75;
 
 	private final HealthBar healthBar;
 	private final float currentRadius;
 	private final float renderScale;
 	private final int maxHealth;
 
 	private boolean invulnerable;
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
 		s.draw(batch, alpha);
 
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
 		if (invulnerable) return;
 
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
 
				SplitAsteroidLauncher l = new SplitAsteroidLauncher(w, body.getPosition().cpy(), body.getLinearVelocity(),
 						nextRadius, body.getMass());
 				TaskScheduler.INSTANCE.runTask(l);
 			}
 		}
 	}
 
 	public void heal(int healingAmoung) {
 		setHealth(health + healingAmoung);
 	}
 
 	@Override
 	public void damage(int damageAmount, Player player, float scoreMultiplier) {
 		setHealth(health - damageAmount);
 		if (health == 0 && player != null) {
 			player.addScore((int) (KILL_SCORE * scoreMultiplier));
 		}
 	}
 
 	public void kill() {
 		setHealth(0);
 	}
 
 	private static BodyDef createBodyDef(Vector2 position, Vector2 velocity) {
 		final BodyDef bodyDef = new BodyDef();
 		bodyDef.type = BodyType.DynamicBody;
 		bodyDef.position.set(position);
 		bodyDef.angle = MathUtils.random(2 * MathUtils.PI);
 		bodyDef.angularDamping = 0.15f;
 		if (velocity != null) {
 			bodyDef.linearVelocity.set(velocity);
 		}
 		bodyDef.gravityScale = 0.1f;
 
 		return bodyDef;
 	}
 
 	private static FixtureDef createCircle(float radius) {
 		final FixtureDef fixture = new FixtureDef();
 		fixture.density = 100f;
 		fixture.restitution = 1.1f;
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
 			final float rot0 = velocity.angle() * MathUtils.degreesToRadians;
 			final float rot1 = MathUtils.random(MathUtils.PI * 0.4f, MathUtils.PI * 0.6f);
 			final float rot2 = MathUtils.random(MathUtils.PI * 1.4f, MathUtils.PI * 1.6f);
 
 			Vector2 loc1 = position.cpy().add(MathUtils.cos(rot0 + rot1) * r1 * 1.1f,
 					MathUtils.sin(rot0 + rot1) * r1 * 1.1f);
 			Vector2 loc2 = position.cpy().add(MathUtils.cos(rot0 + rot2) * r2 * 1.1f,
 					MathUtils.sin(rot0 + rot2) * r2 * 1.1f);
 
 			final StoneAsteroid a1 = new StoneAsteroid(w, loc1, r1, velocity);
 			final StoneAsteroid a2 = new StoneAsteroid(w, loc2, r2, velocity);
 
 			final float mass1 = a1.getPhysicsBody().getMass();
 			final float mass2 = a2.getPhysicsBody().getMass();
 			final Vector2 impulse = new Vector2(velocity.div(mass));
 			final Vector2 i1 = impulse.cpy().scl(50f * mass1 * mass1).rotate(rot1 * MathUtils.radiansToDegrees);
 			final Vector2 i2 = impulse.cpy().scl(50f * mass2 * mass2).rotate(rot2 * MathUtils.radiansToDegrees);
 
 			a1.getPhysicsBody().applyForceToCenter(i1, true);
 			a2.getPhysicsBody().applyForceToCenter(i2, true);
 
 			a1.invulnerable = true;
 			a2.invulnerable = true;
 			TaskScheduler.INSTANCE.runTaskLater(new Runnable() {
 				
 				@Override
 				public void run() {
 					a1.invulnerable = false;
 					a2.invulnerable = false;
 				}
 			}, 0.2f);
 		}
 	}
 }
