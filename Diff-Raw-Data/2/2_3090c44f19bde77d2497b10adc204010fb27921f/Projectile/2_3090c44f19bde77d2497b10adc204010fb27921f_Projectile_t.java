 package com.battleships.base;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.math.MathUtils;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.scenes.scene2d.Actor;
 
 public class Projectile extends Actor{
 	Unit Instigator;
 	Unit Target;
 	float DistToTarget;
 	int Damage;
 	float HitX;
 	float HitY;
 	float LaunchX;
 	float LaunchY;
 	float TravelTime;
 	private Sprite sprite;
 	private BalisticMoveModifier moveModifier;
 
 	public static ProjectilePool projectilepool = new ProjectilePool();
 	
 	static void Launch(Unit inst, Unit targ, int dmg, int type)
 	{
 		projectilepool.obtain().Init(inst, targ, dmg, type);
 	}
 	
 	Projectile()
 	{
 		sprite = new Sprite();
 		moveModifier = new BalisticMoveModifier();
 		BaseGame.gameStage.addActor(this);
 	}
 	
 	private class BalisticMoveModifier{
 
 		private float Duration;
 		private float mX1;
 		private float mY1;
 		private float mX2;
 		private float mY2;
 		private float mX3;
 		private float mY3;
 		private float mX4;
 		private float mY4;
 		private float yOffset;
 		private float timer;
 		
 		public void Init(final float pDuration, final float pX1, final float pY1, final float pX2, final float pY2, final float pX3, final float pY3, final float pX4, final float pY4, final float yOff) {
 			this.yOffset = yOff;
 			this.Duration = pDuration;
 			this.timer = 0;
 			this.mX1 = pX1;
 			this.mY1 = pY1;
 			this.mX2 = pX2;
 			this.mY2 = pY2;
 			this.mX3 = pX3;
 			this.mY3 = pY3;
 			this.mX4 = pX4;
 			this.mY4 = pY4;
 			setPosition(2*this.mX1-this.mX4, 2*this.mY1-this.mY4);
 		}
 		
 		protected void onManagedUpdate(final float pSecondsElapsed) {
 			this.timer += pSecondsElapsed;
 			final float percentageDone = this.timer/this.Duration;
 			
			if(percentageDone >=0.95)
 			{
   			  explode();
 			  return;
 			}
 
 			this.mX3 = (LaunchX+3*Target.getX())/4;
 			this.mY3 = (LaunchY+3*Target.getY())/4 + yOffset*TravelTime;
 			this.mX4 = Target.getX();
 			this.mY4 = Target.getY();
 			
 			final float u = 1 - percentageDone;
 			final float tt = percentageDone * percentageDone;
 			final float uu = u * u;
 			final float uuu = uu * u;
 			final float ttt = tt * percentageDone;
 
 			final float ut3 = 3 * uu * percentageDone;
 			final float utt3 = 3 * u * tt;
 
 			/*
 			 * Formula: ((1-t)^3 * P1) + (3*(t)*(1-t)^2 * P2) + (3*(tt)*(1-t) * P3) + (ttt * P4)
 			 */
 			final float x = (uuu * this.mX1) + (ut3 * this.mX2) + (utt3 * this.mX3) + (ttt * this.mX4);
 			final float y = (uuu * this.mY1) + (ut3 * this.mY2) + (utt3 * this.mY3) + (ttt * this.mY4);
 			
 			setRotation((float) Math.toDegrees(-Math.atan2(x-getX(), y-getY())));
 			setPosition(x, y);
 		}
 		
 	}
 	
 	public void draw (SpriteBatch batch, float parentAlpha) {
 		moveModifier.onManagedUpdate(Gdx.graphics.getDeltaTime());
         batch.draw(sprite, getX()-8,getY()-8,8, 8, 16, 16, 1, 1, this.getRotation());
 	}
 	
 	void Init(Unit inst, Unit targ, int dmg, int type)
 	{   
 		sprite.setRegion(Resources.ProjectileTextureRegion[type]);
 		this.setVisible(true); 	
 		this.toFront();
 		Instigator = inst;
 		Target = targ;
 		HitX = Target.getX();
 		HitY = Target.getY();
 		LaunchX = Instigator.getX() + MathUtils.random(-8, 8);
 		LaunchY = Instigator.getY();
 		sprite.setPosition(Instigator.getX(), Instigator.getY());
 		DistToTarget = new Vector2(HitX - sprite.getX(),HitY - sprite.getY()).len();
 		TravelTime = DistToTarget / PlayerWeapon.Speed[type];
 		
 		Damage = dmg;
 		
 		if(type<4 || type>=6 && type<12)
 		{
 			moveModifier.Init(TravelTime, 
 					LaunchX - sprite.getWidth()/2, LaunchY, 
 					(3*LaunchX+HitX)/4 - sprite.getWidth()/2, (3*LaunchY+HitY)/4 + 64*TravelTime, 
 					(LaunchX+3*HitX)/4 - sprite.getWidth()/2, (LaunchY+3*HitY)/4 + 64*TravelTime, 
 					HitX - sprite.getWidth()/2, HitY,64);
 		}
 		else if(type>=6 && type<12){
 			moveModifier.Init(TravelTime, 
 					LaunchX - sprite.getWidth()/2, LaunchY, 
 					(3*LaunchX+HitX)/4 - sprite.getWidth()/2, (3*LaunchY+HitY)/4 + 64*TravelTime, 
 					(LaunchX+3*HitX)/4 - sprite.getWidth()/2, (LaunchY+3*HitY)/4 + 64*TravelTime, 
 					HitX - sprite.getWidth()/2, HitY,16);
 		}
 		else
 		{
 			moveModifier.Init(TravelTime, 
 					LaunchX - sprite.getWidth()/2, LaunchY, 
 					(3*LaunchX+HitX)/4 - sprite.getWidth()/2, (3*LaunchY+HitY)/4, 
 					(LaunchX+3*HitX)/4 - sprite.getWidth()/2, (LaunchY+3*HitY)/4, 
 					HitX - sprite.getWidth()/2, HitY,0);
 		}
 	}
 	
     void explode()
     {
 		Target.TakeDamage(Damage);
 		this.setVisible(false);
 		sprite.setPosition(HitX,HitY);
     	projectilepool.free(this);
     }
 }
