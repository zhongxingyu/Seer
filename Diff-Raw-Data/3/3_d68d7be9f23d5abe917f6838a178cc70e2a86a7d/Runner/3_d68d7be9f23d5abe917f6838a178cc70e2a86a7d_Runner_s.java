 package com.ajgames.endless_runner.model;
 
 import org.jbox2d.common.Vec2;
 import org.jbox2d.dynamics.BodyType;
 import org.jbox2d.dynamics.World;
 
 
 public class Runner extends PhysicsSprite
 {
 	private static final Vec2 JUMP_VECTOR = new Vec2( 0.0f, -100.0f );
 	private static final Vec2 MOVE_RIGHT_VECTOR = new Vec2( 50.0f, 0.0f );
 	private static final Vec2 MOVE_LEFT_VECTOR = new Vec2( -50.0f, 0.0f );
 
 	public boolean movingLeft = false;
 	public boolean movingRight = false;
 
 	public Runner( World world )
 	{
 		super( 200.0f, 0.0f, 20, 20, world );
 		this.createBox( BodyType.DYNAMIC, Physics.DEFAULT_DENSITY,
				Physics.DEFAULT_FRICTION, 0 );
 	}
 
 	public void update()
 	{
 		if( this.getX() <= 10 && this.getLinearVelocity().x < 0 )
 			this.body.applyForce( MOVE_RIGHT_VECTOR, this.body.getPosition() );
 		else if( this.getX() >= 310 && this.getLinearVelocity().x > 0 )
 			this.body.applyForce( MOVE_LEFT_VECTOR, this.body.getPosition() );
 	}
 
 	public void jump()
 	{
 		this.body.applyForce( JUMP_VECTOR, this.body.getPosition() );
 	}
 
 }
