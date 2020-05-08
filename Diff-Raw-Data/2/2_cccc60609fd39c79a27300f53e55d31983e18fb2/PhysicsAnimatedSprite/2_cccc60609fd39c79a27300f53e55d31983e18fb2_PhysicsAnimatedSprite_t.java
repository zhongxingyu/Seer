 package edu.ua.cs.pbvs;
 
 import org.anddev.andengine.entity.sprite.AnimatedSprite;
 import org.anddev.andengine.extension.physics.box2d.PhysicsConnector;
 import org.anddev.andengine.extension.physics.box2d.PhysicsFactory;
 import org.anddev.andengine.extension.physics.box2d.PhysicsWorld;
 import org.anddev.andengine.extension.physics.box2d.util.Vector2Pool;
 import org.anddev.andengine.opengl.texture.region.TiledTextureRegion;
 
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.Body;
 import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
 import com.badlogic.gdx.physics.box2d.FixtureDef;
 
 public class PhysicsAnimatedSprite extends AnimatedSprite{
 
 	public Body body;
 	public boolean jumping = true;
 	pbvs act;
 	public int dir = 0;
 
 	public PhysicsAnimatedSprite(float pX, float pY, TiledTextureRegion pTiledTextureRegion, PhysicsWorld world, pbvs act, float friction ) {
 		super(pX, pY, pTiledTextureRegion);
 		this.act = act;
 	    final FixtureDef FIXTURE_DEF = PhysicsFactory.createFixtureDef(1, 0f, 0.5f);
 		body = PhysicsFactory.createBoxBody(world, this, BodyType.DynamicBody, FIXTURE_DEF);
 		PhysicsConnector connect = new PhysicsConnector(this, body, true, true);
         world.registerPhysicsConnector(connect);
         body.setUserData(new PhysicsData(this, connect));
 		this.setScaleCenterY((pTiledTextureRegion.getHeight()/3)-10);
 		act.characters.add(this);
 		act.scene.getLastChild().attachChild(this);
 		// TODO Auto-generated constructor stub
 	}
 
 	public void jump(){
 		final Vector2 velocity = Vector2Pool.obtain(0, -40);
 		if (jumping)
 		{
 			body.applyLinearImpulse (velocity, body.getLocalCenter());
 			jumping = false;
 		}
 		Vector2Pool.recycle(velocity);
 	}
 	
 	public void setJump()
 	{
 		jumping = true;
 	}
 	
 	public void hit(Bullet bullet) {
 	}
 	
 	public void shoot() {
 		Bullet bullet = new Bullet(this.getX()+(this.dir*40), this.getY()-25, act.bulletTextureRegion, act.mPhysicsWorld, act, false);
		//act.scene.getLastChild().attachChild(bullet);
 		bullet.shoot(this.dir);
 	}
 }
 
 //force commit
 
