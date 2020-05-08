 package ch.kanti_wohlen.asteroidminer.entities;
 
 import ch.kanti_wohlen.asteroidminer.Textures;
 
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.math.MathUtils;
 import com.badlogic.gdx.physics.box2d.BodyDef;
 import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
 import com.badlogic.gdx.physics.box2d.FixtureDef;
 import com.badlogic.gdx.physics.box2d.PolygonShape;
 import com.badlogic.gdx.physics.box2d.World;
 
 public class SpaceShip extends Entity {
 
 	public static final int MAX_HEALTH = 100;
 	public static final float HEALTH_BAR_ALPHA_MAX = 5f;
 
 	private int health;
 	private float healthAlpha;
 	private boolean shieldEnabled = false;
 
 	public SpaceShip(World world) {
 		super(world, createBodyDef(), createFixture());
 		health = MAX_HEALTH;
 		healthAlpha = HEALTH_BAR_ALPHA_MAX;
 	}
 
 	@Override
 	public void render(SpriteBatch batch) {
 		Sprite s = Textures.SPACESHIP;
 		positionSprite(s);
 		s.draw(batch);
 		getPhysicsBody().setGravityScale(5f);
 
 		healthAlpha = Math.max(0f, healthAlpha - 0.01f);
 		// If taken some health lately, render the health bar
 		if (healthAlpha > 0f) {
 			renderHealthBar(batch, s);
 		}
 	}
 
 	private void renderHealthBar(SpriteBatch batch, Sprite s) {
 		// Render health overlay
		final float x = s.getX() - s.getWidth() * 0.05f; // Space ship texture
															// is off....
 		final float y = s.getY() + s.getHeight() * 1.15f;
 
 		final int xm = Math.round((float) health / SpaceShip.MAX_HEALTH * Textures.HEALTH_HIGH.getRegionWidth());
 		final int xn = Textures.HEALTH_LOW.getRegionWidth() - xm;
 		final int wHigh = Textures.HEALTH_HIGH.getRegionWidth();
 		final int xLow = Textures.HEALTH_LOW.getRegionX();
 		final int wLow = Textures.HEALTH_LOW.getRegionWidth();
 
 		Textures.HEALTH_HIGH.setBounds(x, y, xm, Textures.HEALTH_HIGH.getHeight());
 		Textures.HEALTH_HIGH.setRegionWidth(xm);
 		Textures.HEALTH_HIGH.draw(batch, Math.min(healthAlpha, 1f));
 		Textures.HEALTH_HIGH.setRegionWidth(wHigh);
 
 		Textures.HEALTH_LOW.setBounds(x + xm, y, xn, Textures.HEALTH_LOW.getHeight());
 		Textures.HEALTH_LOW.setRegionX(xLow + xm);
 		Textures.HEALTH_LOW.setRegionWidth(xn);
 		Textures.HEALTH_LOW.draw(batch, Math.min(healthAlpha, 1f));
 		Textures.HEALTH_LOW.setRegionX(xLow);
 		Textures.HEALTH_LOW.setRegionWidth(wLow);
 	}
 
 	@Override
 	public boolean isRemoved() {
 		return false;
 	}
 
 	public int getHealth() {
 		return health;
 	}
 
 	public void setHealth(int newHealth) {
 		if (newHealth != health) {
 			health = MathUtils.clamp(newHealth, 0, MAX_HEALTH);
 			healthAlpha = HEALTH_BAR_ALPHA_MAX;
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
 
 	public boolean getShieldEnabled() {
 		return shieldEnabled;
 	}
 
 	public void setShieldEnabled(boolean shieldEnabled) {
 		this.shieldEnabled = shieldEnabled;
 	}
 
 	public Laser fireLaser() {
 		return new Laser(getPhysicsBody().getWorld(), this);
 	}
 
 	private static BodyDef createBodyDef() {
 		BodyDef bd = new BodyDef();
 		bd.allowSleep = false;
 		bd.type = BodyType.DynamicBody;
 		bd.angularDamping = 10f;
 		bd.linearDamping = 2.5f;
 		bd.position.set(2f, 2f);
 
 		return bd;
 	}
 
 	private static FixtureDef createFixture() {
 		final FixtureDef fixture = new FixtureDef();
 		fixture.density = 1f;
 		final PolygonShape ps = new PolygonShape();
 		ps.setAsBox(Textures.SPACESHIP.getWidth() / 2f * PIXEL_TO_BOX2D, Textures.SPACESHIP.getHeight() / 2f * PIXEL_TO_BOX2D);
 		fixture.shape = ps;
 		return fixture;
 	}
 }
