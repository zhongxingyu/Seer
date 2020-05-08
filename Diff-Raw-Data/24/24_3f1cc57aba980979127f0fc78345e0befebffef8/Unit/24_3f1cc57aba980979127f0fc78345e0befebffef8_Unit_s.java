 package com.battleships.base;
 
 
 import java.util.LinkedList;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.Body;
 import com.badlogic.gdx.physics.box2d.CircleShape;
 import com.badlogic.gdx.physics.box2d.FixtureDef;
 import com.badlogic.gdx.scenes.scene2d.Actor;
 
 public class Unit extends Actor {
 
 	String team;
 	public static String lastSpawnTeam;
     int Health = 1000;
     Sprite baseSprite;
 	Sprite colorSprite;
 	int MaxHealth = 1000;
 	int moveSpeed = 50;
 	int goldworth = 50;
 	Body CollisionBody;
 	
 	Vector2 DesiredVelocity = new Vector2(0,0);
 	
 	Weapon gun;
 	Visor visor;
 	protected int VisibleEnemiesCount = 0;
 	protected LinkedList<Unit> VisibleEnemies = new LinkedList<Unit>(); //used by allies
 	Actor icon;
 	static BodyPool bodyPool = new BodyPool();
 	
 	Unit(String Team, float InitialX, float InitialY)
 	{
 		team = Team;
 				
 		baseSprite = new Sprite();
 		baseSprite.setRegion(Resources.BaseTextureRegion[0]);
 		
 		baseSprite.setSize(64, 64);
 		baseSprite.setOrigin(baseSprite.getWidth()/2, baseSprite.getHeight()/2);
 		baseSprite.setPosition(-baseSprite.getWidth()/2, -baseSprite.getHeight()/2);
 		
 		colorSprite = new Sprite();
 		colorSprite.setRegion(Resources.ColorTextureRegion[0]);
 		
 		colorSprite.setSize(64, 64);
 		colorSprite.setOrigin(colorSprite.getWidth()/2, colorSprite.getHeight()/2);
 		colorSprite.setPosition(-colorSprite.getWidth()/2, -colorSprite.getHeight()/2);
 		
 		if(team == "red")
 			colorSprite.setColor(Color.RED);
 		else
 			colorSprite.setColor(Color.BLUE);
 
 		createBody(InitialX,InitialY);
 		BaseGame.gameStage.addActor(this);
 		
 		icon = new Actor(){
 			float scale = BaseGame.miniMap.getWidth()/2048;
 	        public void draw (SpriteBatch batch, float parentAlpha) {
 	    		if(team != BaseGame.LocalPlayerTeam && VisibleEnemiesCount<=0 && !(Unit.this instanceof Tower))
 	    			return;
 	    		
         		batch.setColor(colorSprite.getColor());
 	            batch.draw(Resources.iconTexture,
 	            		BaseGame.miniMap.getX() + BaseGame.miniMap.getWidth()/2 + Unit.this.getX()*scale -2,
 	            		BaseGame.miniMap.getY() + BaseGame.miniMap.getHeight()/2 + Unit.this.getY()*scale -2,4,4);
 	            batch.setColor(Color.WHITE);
 	        }
 		};
 		BaseGame.hudStage.addActor(icon);
 	}
 	
 	void createBody(float initialX, float initialY)
 	{ 
 		CollisionBody = bodyPool.obtain();
 		CollisionBody.setTransform(initialX*BaseGame.WORLD_TO_BOX,initialY*BaseGame.WORLD_TO_BOX, 0);
 		
 		CircleShape dynamicCircle = new CircleShape();  
         dynamicCircle.setRadius(16f*BaseGame.WORLD_TO_BOX);  
         FixtureDef fixtureDef = new FixtureDef();  
         fixtureDef.shape = dynamicCircle;  
         fixtureDef.density = 1.0f;  
         fixtureDef.friction = 0.0f;  
         fixtureDef.restitution = 0.0f;
         CollisionBody.createFixture(fixtureDef);  
         CollisionBody.getFixtureList().get(0).setUserData(this);
 	}   
 	
 	public void draw (SpriteBatch batch, float parentAlpha) {
 		onUpdate(Gdx.graphics.getDeltaTime());
 		updateVelocity();
 		
 		this.setPosition(CollisionBody.getPosition().x*BaseGame.BOX_WORLD_TO,CollisionBody.getPosition().y*BaseGame.BOX_WORLD_TO);
 		
 		if(team != BaseGame.LocalPlayerTeam && VisibleEnemiesCount<=0)
 			return;
 		
 		float widthScaled = baseSprite.getWidth()*baseSprite.getScaleX();
 		
         batch.draw(baseSprite, getX()-widthScaled/2,getY()-widthScaled/2,widthScaled,widthScaled);
         batch.setColor(colorSprite.getColor());
         batch.draw(colorSprite, getX()-widthScaled/2,getY()-widthScaled/2,widthScaled,widthScaled);
         batch.setColor(Color.WHITE);
         batch.draw(Resources.HealthbarTextureRegion[(int)Math.min(((float)Health/(float)MaxHealth)*10,9)], 
         		getX()-widthScaled/4,getY()+widthScaled/4,widthScaled/2,widthScaled/20);
 	}
 	
 	void onUpdate(float delta)
 	{
     	if(Health<=0)
     	{
     		BaseGame.hudStage.getRoot().removeActor(icon);
         	this.getParent().removeActor(this);
         	int fixtures = CollisionBody.getFixtureList().size();
     		for(int i=0; i<fixtures; i++)
     		{
     			CollisionBody.destroyFixture(CollisionBody.getFixtureList().get(0));
     		}
     		bodyPool.free(CollisionBody);
         	if(team.equals(BaseGame.LocalPlayerTeam))
     		{
     			for(Unit current : VisibleEnemies)
     			{
     				current.VisibleEnemiesCount--;
     			}
     		}        	
         	return;
     	}
 		gun.onUpdate(delta);
 	}
 	
     void TakeDamage(int Damage)
     {
     	Health -= Damage;
     	if(Health <0)
     		Health = 0;
     }
     
     void setDesiredVelocity(float X, float Y)
     {
     	DesiredVelocity.x = X;
     	DesiredVelocity.y = Y;
     }
     
     void updateVelocity()
     {
     	float velocity = DesiredVelocity.len();
     	if(velocity > 0)
     	{
     		CollisionBody.setLinearVelocity(DesiredVelocity.x*moveSpeed/velocity*BaseGame.WORLD_TO_BOX, DesiredVelocity.y*moveSpeed/velocity*BaseGame.WORLD_TO_BOX);
         	setVisualRotation(DesiredVelocity.x, DesiredVelocity.y);
     	}
     	else
     		CollisionBody.setLinearVelocity(0, 0);
     }
     
     void setVisualRotation(float X, float Y)
     {
     	int Angle = (int) Math.toDegrees(-Math.atan2(X, -Y));
     	if(Angle < 0) 
     		Angle += 360;
 
     		Angle = Angle/10;
     		if(Angle >17)
     		{
     			Angle = 35 - Angle;
     			Angle *= 0.833333f;
     			baseSprite.setRegion(Resources.BaseTextureRegion[Angle]);
     			colorSprite.setRegion(Resources.ColorTextureRegion[Angle]);
     			baseSprite.flip(true, false);
     			colorSprite.flip(true, false);
     		}
     		else
     		{
     			Angle *= 0.833333f;
     			baseSprite.setRegion(Resources.BaseTextureRegion[Angle]);
     			colorSprite.setRegion(Resources.ColorTextureRegion[Angle]);
     			baseSprite.flip(false, false);
     			colorSprite.flip(false, false);
     		}
     } 
 }
