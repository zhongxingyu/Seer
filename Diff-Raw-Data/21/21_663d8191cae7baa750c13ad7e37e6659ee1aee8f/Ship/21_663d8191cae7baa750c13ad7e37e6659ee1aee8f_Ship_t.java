 package com.jjaz.aetherflames;
 
 import java.util.ArrayList;
 
 import org.andengine.entity.primitive.Rectangle;
 import org.andengine.entity.sprite.Sprite;
 import org.andengine.entity.sprite.TiledSprite;
 import org.andengine.extension.physics.box2d.PhysicsConnector;
 import org.andengine.extension.physics.box2d.PhysicsFactory;
 import org.andengine.extension.physics.box2d.util.Vector2Pool;
 import org.andengine.util.math.MathUtils;
 
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.Body;
 import com.badlogic.gdx.physics.box2d.FixtureDef;
 import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
 
 public class Ship implements AetherFlamesConstants
 {
 	protected static final int WHITE_SHIP = 0;
 	protected static final int RED_SHIP = 1;
 	protected static final int ORANGE_SHIP = 2;
 	protected static final int YELLOW_SHIP = 3;
 	protected static final int GREEN_SHIP = 4;
 	protected static final int BLUE_SHIP = 5;
 	protected static final int PURPLE_SHIP = 6;
 	protected static final int BLACK_SHIP = 7;
 
 	protected static final float FORWARD = 1.0f;
 	protected static final float BACKWARD = -1.0f;
 	
 	protected static final float LEFT = -1.0f;
 	protected static final float RIGHT = 1.0f;
 
 	private static final int SHIP_SIZE = 64;
 	private static final int STATUS_BAR_PADDING = 3;
 	private static final int REGEN_GRANULARITY = 5;
 
 	private static final int MAX_HP = 1000;
 	private static final int MAX_EP = 1000;
 	
 	public int id;
 	private int hp;
 	private int ep;
 	private float thrust = 25;//7.5f;
 	private boolean shieldsOn;
 	private float epRegenRate = 200.0f; //amount of EP regenerated in a second
 	
 	private long weaponCooldownOver;
 	private long regenCooldownOver;
 	
 	private Body body;
 	private TiledSprite sprite;
 	private Sprite shield;
 	private Rectangle healthBar;
 	private Rectangle healthBarBackground;
 	private Rectangle energyBar;
 	private Rectangle energyBarBackground;
 	
 	private PhysicsConnector mPhysicsConnector;
 
 	private ProjectileWeapon currentWeapon;
 	private int currentWeaponIndex;
 	private ArrayList<ProjectileWeapon> availableWeapons;
 	
 	public boolean isWithinRange(Vector2 position, float radius)
 	{
 		position.x -= body.getWorldCenter().x;
 		position.y -= body.getWorldCenter().y;
 		float distance = (float) Math.sqrt(position.x*position.x + position.y*position.y);//Math.abs((position.cpy().sub(body.getWorldCenter())).len());
 		if(distance - (SHIP_SIZE*AetherFlamesActivity.CAMERA_TO_WORLD) < radius)
 		{
 			return true;
 		}
 		return false;
 	}
 	
 	public void updateStatusBars()
 	{
 		int yMod = ((body.getWorldCenter().y < 1.5)? (SHIP_SIZE + 12 + 2*STATUS_BAR_PADDING) : 0); //so that you can still see the bars when the ships are near the top of the screen
 		
 		Vector2 healthBarPosition = body.getWorldCenter();
 		healthBarPosition.x = (healthBarPosition.x*AetherFlamesActivity.WORLD_TO_CAMERA) - SHIP_SIZE/2;
 		healthBarPosition.y = (healthBarPosition.y*AetherFlamesActivity.WORLD_TO_CAMERA) - SHIP_SIZE/2 - 12 - STATUS_BAR_PADDING + yMod;
 		healthBar.setWidth(hp*SHIP_SIZE/MAX_HP);
 		healthBar.setPosition(healthBarPosition.x, healthBarPosition.y);
 		healthBarBackground.setWidth((MAX_HP - hp)*SHIP_SIZE/MAX_HP);
 		healthBarBackground.setPosition(healthBarPosition.x + healthBar.getWidth(), healthBarPosition.y);
 		
 		Vector2 energyBarPosition = body.getWorldCenter();
 		energyBarPosition.x = (energyBarPosition.x*AetherFlamesActivity.WORLD_TO_CAMERA) - SHIP_SIZE/2;
 		energyBarPosition.y = (energyBarPosition.y*AetherFlamesActivity.WORLD_TO_CAMERA) - SHIP_SIZE/2 - 6 - STATUS_BAR_PADDING + yMod;
 		energyBar.setWidth(ep*SHIP_SIZE/MAX_EP);
 		energyBar.setPosition(energyBarPosition.x, energyBarPosition.y);
 		energyBarBackground.setWidth((MAX_EP - ep)*SHIP_SIZE/MAX_EP);
 		energyBarBackground.setPosition(energyBarPosition.x + energyBar.getWidth(), energyBarPosition.y);
 	}
 	
 	public void regen()
 	{
 		long currentTime = System.currentTimeMillis();
 		if(shieldsOn)
 		{
 			regenCooldownOver = currentTime;
 		}
 		
 		long timeSinceLastRegen = currentTime - regenCooldownOver;
 		
 		if(timeSinceLastRegen > 0)
 		{
 			ep += (int)((float)epRegenRate * ((float)timeSinceLastRegen/1000.0f));
 			if(ep > MAX_EP)
 			{
 				ep = MAX_EP;
 			}
 			regenCooldownOver = System.currentTimeMillis() + REGEN_GRANULARITY;
 		}
 	}
 	
 	void destroyShip(boolean explode)
 	{
 		AetherFlamesActivity.mPhysicsWorld.reportState();
 		body.setUserData("delete");
 		AetherFlamesActivity.mScene.detachChild(healthBar);
 		AetherFlamesActivity.mScene.detachChild(healthBarBackground);
 		AetherFlamesActivity.mScene.detachChild(energyBar);
 		AetherFlamesActivity.mScene.detachChild(energyBarBackground);
 		AetherFlamesActivity.ships.remove(id);
 		
 		if(explode)
 		{
 			CollisionHandler.drawExplosion(body.getWorldCenter().cpy().mul(AetherFlamesActivity.WORLD_TO_CAMERA), SHIP_SIZE*2*AetherFlamesActivity.CAMERA_TO_WORLD);
 		}
 	}
 	
 	public void damage(int amount)
 	{
 		if(shieldsOn)
 		{
 			if(ep > amount)
 			{
 				ep -= amount;
 			}
 			else
 			{
 				deactivateShields();
 				hp -= (amount - ep);
 				ep = 0;
 				if(hp <= 0)
 				{
 					destroyShip(true);
 				}
 			}
 		}
 		else
 		{
 		hp -= amount;
 		if(hp <= 0)
 		{
 			destroyShip(true);
 		}
 	}
 	
 	}
 	
 	public void heal(int amount)
 	{
 		hp += amount;
 		if(hp > MAX_HP)
 		{
 			hp = MAX_HP;
 		}
 	}
 
 	public int getHealth() 
 	{
 		return hp;
 	}
 	
 	public int getEnergy() 
 	{
 		return ep;
 	}
 	
 	public boolean shieldActivated()
 	{
 		return shieldsOn;
 	}
 	
 	public float getAngle()
 	{
 		return body.getAngle();
 	}
 	
 	public float getAngularVelocity()
 	{
 		return body.getAngularVelocity();
 	}
 	
 	public Vector2 getPosition() {
 		return body.getWorldCenter();
 	}
 	
 	public Vector2 getVelocity()
 	{	
 		return body.getLinearVelocity();
 	}
 	
 	public void setHealth(int health)
 	{
 		hp = health;
 	}
 	
 	public void setEnergy (int energy) 
 	{
 		ep = energy;
 	}
 	
 	public void setAngle(float angle)
 	{
 		body.setTransform(body.getPosition(), angle);
 		sprite.setRotation(MathUtils.radToDeg(body.getAngle()));
 	}
 	
 	public void setPosition(Vector2 position)
 	{
 		body.setTransform(position, body.getAngle());
 	}
 	
 	public void setVelocity(Vector2 velocity)
 	{
 		body.setLinearVelocity(velocity);
 	}
 	
 	public void applyForce(Vector2 direction)
 	{
 		Vector2 force = direction;
 		force.x *= thrust;
 		force.y *= thrust;
 		Vector2 point = body.getWorldCenter();
 		body.applyForce(force, point);
 	}
 	
 	public void fireThrusters(float magnitude)
 	{
 		float shipAngle = body.getAngle();
 		float thrustMagnitude = Math.signum(magnitude) * thrust; //used Math.signum previously
 		float thrustX = (float)(thrustMagnitude*Math.sin(shipAngle));
 		float thrustY = -(float)(thrustMagnitude*Math.cos(shipAngle));
 		Vector2 force = Vector2Pool.obtain(thrustX, thrustY);
 		Vector2 point = body.getWorldCenter();
 		body.applyForce(force, point);
 		Vector2Pool.recycle(force);
 	}
 	
 	public void turnInstantAndThrust(Vector2 direction)
 	{
 		body.setTransform(body.getWorldCenter(), (float) Math.atan2(-direction.x, direction.y));
 		sprite.setRotation(MathUtils.radToDeg(body.getAngle()));
 		
 		Vector2 force = direction;
 		force.x *= thrust;
 		force.y *= thrust;
 		Vector2 point = body.getWorldCenter();
 		body.applyForce(force, point);
 	}
 	
 	public void turn(float direction)
 	{	
 		body.setAngularVelocity(Math.signum(direction));
 		sprite.setRotation(MathUtils.radToDeg(body.getAngle()));
 	}
 	
 	Vector2 barrelPosition()
 	{
 		Vector2 shipCenter = body.getLocalCenter().cpy();
 		shipCenter.y += currentWeapon.BULLET_SIZE*AetherFlamesActivity.CAMERA_TO_WORLD + 1.25;
 		Vector2 gunPoint = body.getWorldPoint(shipCenter);
 		return gunPoint;
 	}
 	
 	boolean weaponIsCool()
 	{
 		return weaponCooldownOver < System.currentTimeMillis();
 	}
 	
 	public void fireWeapon()
 	{
 		if(!shieldsOn) //can't fire while shields up
 		{
 			if(ep > currentWeapon.COST && weaponIsCool())
 			{
 				int bulletID = AetherFlamesActivity.mPhysicsWorld.nextBulletID();
				Vector2 position = barrelPosition();
				Vector2 velocity = body.getLinearVelocity();
				float angle = body.getAngle();
 				ep -= currentWeapon.COST;
				Body bulletBody = currentWeapon.fire(bulletID, position, velocity, angle);
 				AetherFlamesActivity.mPhysicsWorld.registerBullet(bulletID, currentWeapon.getType(), bulletBody,
						position, velocity, angle);
 				weaponCooldownOver = System.currentTimeMillis() + currentWeapon.COOLDOWN;
 			}
 		}
 	}
 
 	public void nextWeapon()
 	{
 		//TODO: make and update current weapon picture
 		currentWeapon.weaponSelectionSprite.setVisible(false);
 		currentWeaponIndex++;
 		if(currentWeaponIndex >= availableWeapons.size())
 		{
 			currentWeaponIndex = 0;
 		}
 		currentWeapon = availableWeapons.get(currentWeaponIndex);
 		currentWeapon.weaponSelectionSprite.setVisible(true);
 	}
 	
 	public void previousWeapon()
 	{
 		//TODO: make and update current weapon picture
 		currentWeapon.weaponSelectionSprite.setVisible(false);
 		currentWeaponIndex--;
 		if(currentWeaponIndex < 0)
 		{
 			currentWeaponIndex = availableWeapons.size() - 1;
 		}
 		currentWeapon = availableWeapons.get(currentWeaponIndex);
 		currentWeapon.weaponSelectionSprite.setVisible(true);
 	}
 	
 	public ArrayList<ProjectileWeapon> getAvailableWeapons() {
 		return availableWeapons;
 	}
 
 	public void activateShields()
 	{
 		if(ep > 0)
 		{
 			shield.setVisible(true);
 			shieldsOn = true;
 		}
 		else
 		{
 			shield.setVisible(false);
 			shieldsOn = false;
 		}
 	}
 	
 	public boolean isShielded()
 	{
 		return shieldsOn;
 	}
 	
 	public void deactivateShields()
 	{
 		shield.setVisible(false);
 		shieldsOn = false;
 	}
 	
 	void setUpBars()
 	{
 		healthBar = new Rectangle(0, 0, SHIP_SIZE, 6, AetherFlamesActivity.mVertexBufferObjectManager);
 		healthBar.setColor(1.0f,0.0f,0.0f,0.5f);
 		AetherFlamesActivity.mScene.attachChild(this.healthBar);
 		
 		healthBarBackground = new Rectangle(0, 0, SHIP_SIZE, 6, AetherFlamesActivity.mVertexBufferObjectManager);
 		healthBarBackground.setColor(1.0f,1.0f,1.0f,0.5f);
 		AetherFlamesActivity.mScene.attachChild(this.healthBarBackground);
 		
 		energyBar = new Rectangle(0, 0, SHIP_SIZE, 6, AetherFlamesActivity.mVertexBufferObjectManager);
 		energyBar.setColor(0.0f,0.0f,1.0f,0.5f);
 		AetherFlamesActivity.mScene.attachChild(this.energyBar);
 		
 		energyBarBackground = new Rectangle(0, 0, SHIP_SIZE, 6, AetherFlamesActivity.mVertexBufferObjectManager);
 		energyBarBackground.setColor(1.0f,1.0f,1.0f,0.5f);
 		AetherFlamesActivity.mScene.attachChild(this.energyBarBackground);
 	}
 	
 	void setUpWeapons()
 	{
 		availableWeapons.add(new PlasmaBlaster());
 		availableWeapons.add(new Nyannon());
 		currentWeapon = availableWeapons.get(0);
 	}
 	
 	public Ship(float x, float y, float angle, int color) //angle in degrees 0 is down, -90 is right, 90 is left
 	{
 		//set up ship stats
 		id = color;
 		hp = MAX_HP;
 		ep = MAX_EP;
 		shieldsOn = false;
 		regenCooldownOver = System.currentTimeMillis();
 		weaponCooldownOver = System.currentTimeMillis();
 		setUpBars();
 		//currentWeapon = new PlasmaBlaster();
 		availableWeapons = new ArrayList<ProjectileWeapon>();
 		setUpWeapons();
 		if(color == AetherFlamesActivity.myShipColor)
 		{
 			currentWeapon.weaponSelectionSprite.setVisible(true);
 		}
 		
 		//set up physical ship
 		this.sprite = new TiledSprite(-20, -20, SHIP_SIZE, SHIP_SIZE, AetherFlamesActivity.mShipTextureRegion, AetherFlamesActivity.mVertexBufferObjectManager);
 		this.sprite.setCurrentTileIndex(color);
 		
 		this.shield = new Sprite(0, 0, SHIP_SIZE, SHIP_SIZE, AetherFlamesActivity.mShieldTextureRegion, AetherFlamesActivity.mVertexBufferObjectManager);
 		sprite.attachChild(shield);
 		shield.setVisible(false);
 
 		final FixtureDef shipFixtureDef = PhysicsFactory.createFixtureDef(1, 0.5f, 0.0f);
 		this.body = PhysicsFactory.createCircleBody(AetherFlamesActivity.mPhysicsWorld, this.sprite, BodyType.DynamicBody, shipFixtureDef);
 		body.setUserData("ship "+color);
 		
 		this.body.setTransform(x, y, (float) Math.toRadians(angle));
 		this.sprite.setRotation(MathUtils.radToDeg(this.body.getAngle()));
 		
 		this.mPhysicsConnector = new PhysicsConnector(this.sprite, this.body, true, false);
 		AetherFlamesActivity.mPhysicsWorld.registerPhysicsConnector(this.mPhysicsConnector);
 		AetherFlamesActivity.mScene.attachChild(this.sprite);
 	}
 }
