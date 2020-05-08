 package com.ajgames.endless_runner.model;
 
 import org.jbox2d.common.Vec2;
 import org.jbox2d.dynamics.BodyType;
 import org.jbox2d.dynamics.Filter;
 import org.jbox2d.dynamics.World;
 
 
 public class Runner extends PhysicsSprite
 {	
 	private static final Vec2 JUMP_VECTOR = new Vec2( 0.0f, -200.0f );
 	private static final Vec2 MOVE_RIGHT_VECTOR = new Vec2( 50.0f, 0.0f );
 	private static final Vec2 MOVE_LEFT_VECTOR = new Vec2( -50.0f, 0.0f );
 
 	public float speed = -5.0f;
 	
 	private boolean grounded = false;
 	private boolean movingDown = false;
 	
 	private Filter collidePlatformsFilter;
 	private Filter avoidPlatformsFilter;
 
 	public Runner( float startX, float startY, World world )
 	{
 		super( startX, startY, 20, 20, world );
 		this.createBox( BodyType.DYNAMIC, Physics.DEFAULT_DENSITY,
 				0, 0, true );
 		
 		this.initFilters();
 	}
 
 	public void update()
 	{
 		if( this.getX() <= 10 && this.getLinearVelocity().x < 0 )
 			this.body.applyForce( MOVE_RIGHT_VECTOR, this.body.getPosition() );
 		else if( this.getX() >= 310 && this.getLinearVelocity().x > 0 )
 			this.body.applyForce( MOVE_LEFT_VECTOR, this.body.getPosition() );
 		
 		if( this.getLinearVelocity().y > 0 && !this.movingDown )
 		{
 			this.movingDown = true;
 			this.body.getFixtureList().setFilterData( this.collidePlatformsFilter );
 		}
		else if( this.getLinearVelocity().y <= 0 )
 		{
 			this.movingDown = false;
 		}
 	}
 
 	@Override
 	public void onCollisionBegan( PhysicsSprite sprite )
 	{
 		this.grounded = true;
 	}
 	
 	public void jump()
 	{
 		if( this.grounded )
 		{
			this.movingDown = false;
 			this.grounded = false;
			this.body.getFixtureList().setFilterData( this.avoidPlatformsFilter );
 			this.body.applyForce( JUMP_VECTOR, this.body.getPosition() );
 		}
 	}
 	
 	public void setDirection(int clickedX)
 	{
 		//i don't know how this all works yet :)
 		//where ever it was click on screen, base the direction on that
 		//left
 		//right
 		//stop moving side to side
 	}
 
 	private void initFilters()
 	{
 		this.avoidPlatformsFilter = new Filter();
 		this.avoidPlatformsFilter.categoryBits = Collision.NO_COLLISION;
 		this.avoidPlatformsFilter.maskBits = Collision.NO_COLLISION;
 		
 		this.collidePlatformsFilter = new Filter();
 		this.collidePlatformsFilter.categoryBits = Collision.MAIN_CATEGORY;
 		this.collidePlatformsFilter.maskBits = Collision.MAIN_CATEGORY;
 	}
 	
 }
