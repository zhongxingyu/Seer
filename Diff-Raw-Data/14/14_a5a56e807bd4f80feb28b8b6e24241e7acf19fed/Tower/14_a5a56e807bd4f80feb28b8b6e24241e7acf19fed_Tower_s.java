 package com.battleships.base;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
 
 public class Tower extends Unit {
 
 	Tower(String Team, float InitialX, float InitialY)
 	{
 		super(Team, InitialX, InitialY);
 		if(team == "red")
 			baseSprite.setRegion(Resources.TowerTextureRegion[0]);
 		else
 			baseSprite.setRegion(Resources.TowerTextureRegion[1]);
     	MaxHealth = 5000;
     	Health = MaxHealth;
     	visor = new Visor(this);
     	gun = new PlayerWeapon(this,9);
     	CollisionBody.setType(BodyType.StaticBody);
 	}
 	
 	public void draw (SpriteBatch batch, float parentAlpha) {
 		this.setPosition(CollisionBody.getPosition().x*GameScreen.BOX_WORLD_TO,CollisionBody.getPosition().y*GameScreen.BOX_WORLD_TO);
 		this.setZIndex(2);
         batch.draw(baseSprite, getX()-16,getY()-64,64,128);
         batch.draw(Resources.HealthbarTextureRegion[0], 
         		getX()-16,getY()+32,32,4);
        

         batch.draw(Resources.HealthbarTextureRegion[1], 
         		getX()-16+1,getY()+32,32*((float)Health/(float)MaxHealth),4);
         
 		onUpdate(Gdx.graphics.getDeltaTime());
 	}
 }
