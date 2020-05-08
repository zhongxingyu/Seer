 package com.meros.playn.core;
 
 import java.util.Random;
 
 import playn.core.Surface;
 
 import com.meros.playn.core.Constants.Direction;
 import com.meros.playn.core.entities.Coin;
 import com.meros.playn.core.entities.ParticleSystem;
 import com.meros.playn.core.entities.ReactorCore;
 
 public class Boss extends Entity {
 	
 	private Random random = new Random();
 	
 	private int BLOW_TIME = (60 * 4 + 30);
 	private int DAMAGE_MAX = 16;
 	private int FRAME_PER_DAMAGE = 4;
 	private int INITIAL_BLOW_TIME = (60 * 4 + 30);
 	private int MAX_HEALTH = 40;
 	private int MIN_HEALTH = 10;
 	private int myHealth = UtilMethods.lerp(MAX_HEALTH, MIN_HEALTH, PlayerSkill.get());
 
 	enum State
 	{
 		INIT,
 		SLEEPING,
 		INITIAL_BLOW,
 		AWEKENING,
 		MOVE_UPWARDS,
 		FLOAT,
 		STOP,
 		ATTACK,
 		VULNERABLE,
 		DEAD
 	}
 
 	private State myState = State.INIT;
 	private Direction myDirection = Direction.LEFT;
 	private int myInitalHealth = 16;
 	private int myAnimFrameCounter = 0;
 
 	private Animation myReactor = Resource.getAnimation("data/images/reactor_shell.bmp", 4);
 	private Animation myBoss = Resource.getAnimation("data/images/boss.bmp", 4);
 	private int myFrameCounter = 0;
 	private float2 myOriginalPos;
 
 	public Boss()
 	{
 		setSize(new float2(30, 40));
 
 
 	}
 
 	public void draw( Surface buffer, int offsetX, int offsetY, int layer )
 	{
 		int x = (int) (getDrawPositionX() + offsetX - getHalfSize().x);
 		int y = (int) (getDrawPositionY() + offsetY - getHalfSize().y);
 
 		if (myState == State.SLEEPING)
 			myReactor.drawFrame(buffer, DAMAGE_MAX / FRAME_PER_DAMAGE - (myInitalHealth + 3) / FRAME_PER_DAMAGE, x , y );
 		else if (myState == State.INITIAL_BLOW)
 			myReactor.drawFrame(buffer, 3, x , y );
 		else if  (myState == State.VULNERABLE)
 			myBoss.drawFrame(buffer, 0, x, y);
 		else
 			myBoss.drawFrame(buffer, myAnimFrameCounter / 5, x, y);
 	}
 
 	public void update()
 	{
 		myAnimFrameCounter++;
 		myFrameCounter ++;
 
 		if (myState == State.INIT)
 		{
 			myOriginalPos = getPosition();
 			myState = State.SLEEPING;
 		}
 
 		if (myState != State.MOVE_UPWARDS 
 				&& myState != State.SLEEPING 
 				&& myState != State.INITIAL_BLOW 
 				&& myState != State.DEAD 
 				&& myState != State.VULNERABLE)
 		{
			if (mRoom.getHero().getCollisionRect().Collides(getCollisionRect()))
 			{
 				mRoom.getHero().kill();
 			}
 		}
 
 		if (myState == State.INITIAL_BLOW)
 		{
 			if (myFrameCounter % 60 == 0)
 				Sound.playSample("data/sounds/alarm");
 
 			if (myFrameCounter >= INITIAL_BLOW_TIME)
 			{
 				Sound.playSample("data/sounds/reactor_explosion");
 				Sound.playSample("data/sounds/start");
 				ParticleSystem ps = new ParticleSystem(Resource.getAnimation("data/images/debris.bmp", 4), 20, 200, 20, 1, 50, 50, new float2(0.0f, -150.0f), 5.0f);
 				ps.setPosition(getPosition(), 10.0f, false);
 				mRoom.addEntity(ps);
 				myFrameCounter = 0;
 				myState = State.AWEKENING;
 			}		
 		}
 
 		if (myState == State.AWEKENING)
 		{
 			if (myFrameCounter > 60 * 3)
 			{
 				myState = State.MOVE_UPWARDS;
 				myFrameCounter = 0;
 				setTilesCollidable(false);
 				Music.pushSong();
 				Music.playSong("data/music/olof3.xm");
 			}
 		}
 
 		if (myState == State.MOVE_UPWARDS)
 		{
 			setPosition(getPosition().add(new float2(0, -1)));
 			if (myOriginalPos.y - getPosition().y > 130)
 			{
 				myState = State.FLOAT;
 				mRoom.broadcastBoosWallActivate();
 				myFrameCounter = 0;
 			}
 		}
 
 		if (myState == State.FLOAT)
 		{
 			float y =  (float) (Math.sin(myFrameCounter / 8) * 2.0f);
 			if (myDirection == Direction.RIGHT)
 			{
 				setPosition(getPosition().add(new float2(1.5f, y)));
 				if (getPosition().x  - myOriginalPos.x > 110)
 					myDirection = Direction.LEFT;
 
 			}
 
 			if (myDirection == Direction.LEFT)
 			{
 				setPosition(getPosition().add(new float2(-1.5f, y)));
 				if (myOriginalPos.x - getPosition().x > 110)
 					myDirection = Direction.RIGHT;
 			}
 
 			if (myFrameCounter > 60 * 7)
 			{
 				myState = State.ATTACK;
 				myFrameCounter = 0;
 			}
 		}
 
 		if (myState == State.ATTACK)
 		{
 			if (myFrameCounter < 40)
 			{
 				if (myFrameCounter % 2 == 0)
 					setPosition(getPosition().add(new float2(-2, 0)));
 				else 
 					setPosition(getPosition().add(new float2(2, 0)));
 			}
 			else
 			{
 				setPosition(getPosition().add(new float2(0, 6.0f)));
 				if (myOriginalPos.y < getPosition().y)
 				{
 					mRoom.broadcastBoosWallDectivate();
 					setPosition(new float2(getPosition().x, myOriginalPos.y));
 					myState = State.VULNERABLE;
 					mRoom.getCamera().addShake(5.0f, 80);
 					
 					int numPs = random.nextInt() % 10 + 5;
 					ParticleSystem ps = new ParticleSystem(Resource.getAnimation("data/images/debris.bmp", 4), 10, 40, 10, 1, 50, numPs, new float2(0.0f, -20.0f), 2.0f);
 					ps.setPosition(getPosition(), 10.0f, false);
 					mRoom.addEntity(ps);
 					mRoom.broadcastBoosFloorActivate();
 					Sound.playSample("data/sounds/reactor_explosion");
 					Coin.SpawnDeathCoins(random.nextInt() % 5 + 3, getPosition(), 60 * 6, mRoom);
 				}
 			}
 
 		}
 
 		if (myState == State.VULNERABLE)
 		{
 			if (myFrameCounter > 60 * 5)
 			{
 				myState = State.MOVE_UPWARDS;
 				myFrameCounter = 0;
 			}
 		}
 
 		if (myState == State.DEAD)
 		{
 			if (myFrameCounter % 60 == 0)
 				Sound.playSample("data/sounds/alarm");
 
 			if (myFrameCounter >= BLOW_TIME)
 			{
 				Sound.playSample("data/sounds/reactor_explosion");
 				Sound.playSample("data/sounds/start");
 				ParticleSystem ps = new ParticleSystem(Resource.getAnimation("data/images/debris.bmp", 4), 20, 200, 20, 1, 50, 50, new float2(0.0f, -150.0f), 5.0f);
 				ps.setPosition(getPosition(), 10.0f, false);
 				mRoom.addEntity(ps);
 				ReactorCore core = new ReactorCore();
 				core.setPosition(getPosition());
 				core.setVelocity(new float2(0, -50));
 				mRoom.addEntity(core);
 				remove();
 			}
 		}
 	}
 
 	public int getLayer()
 	{
 		return 3;
 	}
 
 	public void 
 	setTilesCollidable( 
 			boolean  aCollidable )
 			{
 		int sx = (int) ((getPosition().x-getHalfSize().x)/10);
 		int sy = (int) ((getPosition().y-getHalfSize().y)/10);
 
 		for (int x = sx; x < sx + 3; x++)
 			for (int y = sy; y < sy + 4; y++)
 				mRoom.setCollidable(x, y, aCollidable);
 			}
 
 	public void setRoom( Room room )
 	{
 		super.setRoom(room);
 		setTilesCollidable(true);
 	}
 
 	public void onDamage()
 	{
 		if (myState == State.SLEEPING)
 		{
 			int numPs = random.nextInt() % 5 + 1;
 			ParticleSystem ps = new ParticleSystem(Resource.getAnimation("data/images/debris.bmp", 4), 10, 40, 10, 1, 50, numPs, new float2(0.0f, -20.0f), 2.0f);
 			ps.setPosition(getPosition(), 10.0f, false);
 			mRoom.addEntity(ps);
 
 			myInitalHealth--;
 			if (myInitalHealth == 0)
 			{
 				myFrameCounter = 0;
 				myState = State.INITIAL_BLOW;
 				Sound.playSample("data/sounds/damage");
 				mRoom.getCamera().addShake(4.0f, INITIAL_BLOW_TIME);
 			}
 			else
 			{
 				Sound.playSample("data/sounds/damage");
 				mRoom.getCamera().addShake(1.0f, 20);
 			}
 		}
 
 		if (myState == State.VULNERABLE)
 		{
 			int numPs = random.nextInt() % 5 + 1;
 			ParticleSystem ps = new ParticleSystem(Resource.getAnimation("data/images/debris.bmp", 4), 10, 40, 10, 1, 50, numPs, new float2(0.0f, -20.0f), 2.0f);
 			ps.setPosition(getPosition(), 10.0f, false);
 			mRoom.addEntity(ps);
 
 			myHealth--;
 
 			if (myHealth < 0)
 			{
 				myState = State.DEAD;
 				myFrameCounter = 0;
 				Sound.playSample("data/sounds/damage");
 				mRoom.getCamera().addShake(4.0f, BLOW_TIME);
 			}
 			else
 			{
 				Sound.playSample("data/sounds/damage");
 				mRoom.getCamera().addShake(1.0f, 20);
 			}
 		}
 	}
 
 	public CollisionRect getCollisionRect()
 	{
 		CollisionRect rect = new CollisionRect();
 		rect.myTopLeft = getPosition().subtract(getHalfSize()).subtract(new float2(2,2));
 		rect.myBottomRight = getPosition().add(getHalfSize()).add(new float2(2,2));
 		return rect;
 	}
 
 	public boolean isDamagable()
 	{
 		return true;
 	}
 
 	public void onRespawn()
 	{
 		setPosition(myOriginalPos);
 		myState = State.SLEEPING;
 		myDirection = Direction.LEFT;
 		myHealth = UtilMethods.lerp(MAX_HEALTH, MIN_HEALTH, PlayerSkill.get());
 		myInitalHealth = 16;
 		myAnimFrameCounter = 0;
 		Music.popSong();
 		setTilesCollidable(true);
 	}
 }
