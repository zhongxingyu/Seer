 package com.detourgames.raw;
 
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.BodyDef;
 import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
 import com.badlogic.gdx.physics.box2d.Fixture;
 import com.badlogic.gdx.physics.box2d.FixtureDef;
 import com.badlogic.gdx.physics.box2d.PolygonShape;
 import com.badlogic.gdx.physics.box2d.World;
 
 public class PhysicsProjectile extends PhysicsComponent{
 
		float mVelocity = 1f;//changed by this.SetType()
 		Vector2 mDestinationPoint;
 		
 		public PhysicsProjectile(){
 			super();
 		}
 		
 		
 		@Override
 		public void update() {
 			
 		}
 		
 		public void setProjectileProperties(int type, Sprite parent, Vector2 destinationPoint){
 			//TODO finish. also swap parent for just vec2 spawnpoint if no other info is needed from parent.
 			mBody.setTransform(parent.getX(), parent.getY(), 0);
 			//mType = type;
 			mDestinationPoint = destinationPoint;
 			setType(type);
 			// TODO .mul(mBody.getMass()).mul(mVelocity); <- impulse vector should be multiplied like this, but, for
 			//some reason, it is always normalized even after being multiplied. I suspect a problem with Vector2.nor()
			mBody.applyLinearImpulse( getDistanceVectorToPoint(destinationPoint).nor().mul(mVelocity), mBody.getWorldCenter());
 			
 		}
 		
 		private void setType(int type){
 			
 			if(type==Projectile.TYPE_RAW){
 				mVelocity = Projectile.VELOCITY_RAW;
 				//mVertices = Projectile.VERTS_RAW;
 				for(Fixture f : mBody.getFixtureList())
 				{
 					f.getFilterData().groupIndex = PhysicsComponent.GROUP_ALLY;//TODO ideal place to do this, but gets reset by something, so this has no effect.
 					//TODO maybe it is being reset by 16th line of create()?
 				}
 			}
 			if(type==Projectile.TYPE_ENEMY){
 				mVelocity = Projectile.VELOCITY_RAW;
 				//mVertices = Projectile.VERTS_RAW;
 				for(Fixture f : mBody.getFixtureList())
 				{
 					f.getFilterData().groupIndex = PhysicsComponent.GROUP_ENEMY;
 				}
 			}
 			
 		}
 		
 		@Override
 		public void create(World world, float x, float y, Vector2 vertices[], boolean dynamic, int mainFixtureType) {
 
 			BodyDef bodyDef = new BodyDef();
 			if (dynamic) {
 				bodyDef.type = BodyType.DynamicBody;
 			} else {
 				bodyDef.type = BodyType.StaticBody;
 			}
 			bodyDef.position.set(x, y);
 			mBody = world.createBody(bodyDef);
 
 			PolygonShape dynamicBox = new PolygonShape();
 			dynamicBox.set(vertices);
 			FixtureDef fixtureDef = new FixtureDef();
 			fixtureDef.shape = dynamicBox;
 			fixtureDef.density = 1.0f;
 			fixtureDef.friction = 0.0f;
 			fixtureDef.filter.groupIndex = PhysicsComponent.GROUP_ALLY;
 			mBody.createFixture(fixtureDef).setUserData(mainFixtureType);
 			mBody.setFixedRotation(true);
 			mBody.setUserData(this);
 
 		}
 
 	
 }
