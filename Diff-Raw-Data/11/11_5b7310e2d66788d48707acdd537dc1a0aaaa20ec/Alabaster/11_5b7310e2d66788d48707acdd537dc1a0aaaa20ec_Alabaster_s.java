 package com.me.ampdom;
 
 import java.util.ArrayList;
 
 import com.badlogic.gdx.Files.FileType;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Input;
 import com.badlogic.gdx.audio.Sound;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.Texture.TextureFilter;
 import com.badlogic.gdx.graphics.g2d.BitmapFont;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
 import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.Body;
 import com.badlogic.gdx.physics.box2d.BodyDef;
 import com.badlogic.gdx.physics.box2d.FixtureDef;
 import com.badlogic.gdx.physics.box2d.PolygonShape;
 import com.badlogic.gdx.physics.box2d.World;
 
 
 public class Alabaster extends Character {
 	//position
 	float x,y;
 	
 	//body offset
 	final float bodyOffset = 0.5f;
 	
 	//double jump
 	static int count=2;
 	
 	//bools
 	
 	boolean doubleJump;
 	boolean shell = false;
 	boolean shout = false;
 	boolean tongueAct=false;
 	boolean powerLegs=false;
 	boolean spit = false;
 	boolean tongueOut = false;
 
 	//shell stuff
 	Sprite shellSprite;
 	Texture shellText;
 	Sound shellSound;
 	
 	//tongue stuff
 	Sprite tongueSprite;
 	Texture tongueText;
 	FixtureDef tongueFixtureDef;
 	Body tongueBody;
 	BodyDef tongueBodyDef;
 	Sound tongueSound;
 	
 	//shout stuff
 	Sprite shoutSprite;
 	Texture shoutText;
 	FixtureDef shoutFixtureDef;
 	Body shoutBody;
 	BodyDef shoutBodyDef;
 	Sound shoutSound;
 	//foot
 	FixtureDef footFix;
 	Body foot;
 	BodyDef footDef;
 	
 	//spit stuff
 	Sprite spitSprite;
 	Texture spitTexture;
 	FixtureDef spitFixtureDef;
 	Body spitBody;
 	BodyDef spitBodyDef;
 	Sound spitSound;
 	ArrayList<Body> spits = new ArrayList<Body>();
 	
 	//sounds
 	Sound jumpSound;	
 	
 	
 	// HUD
 	private float k;
 	protected SpriteBatch icon;
 	private SpriteBatch healthText;
 	private BitmapFont font;
 	private ShapeRenderer ShapeRenderer;
 	protected int shellCharge = 100;
 	protected int spitCharge = 100;
 	protected int shoutCharge = 100;

 	
 	final float spitVel = 10.0f;
 	
 	long now, last;
 	
 	public Alabaster(World world, float x, float y) {
 		super(world, x, y);	
 		/*setup physics n sounds*/
 		//--alabaster
 		texture = new Texture(Gdx.files.internal("data/Alabaster/alabasterS.png"));
 		texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
 		sprite = new Sprite(texture, 0, 0, 64, 51);
 		
 		entityDef.position.set(x, y);
 		entity = world.createBody(entityDef);
 		
 		PolygonShape shape = new PolygonShape();
 	    shape.setAsBox(sprite.getWidth() / (2 * PIXELS_PER_METER),
 				sprite.getHeight() / (2 * PIXELS_PER_METER));
 	    
 	    entity.setFixedRotation(true);		
 		
 		fixtureDef = new FixtureDef();
 		fixtureDef.shape = shape;
 		fixtureDef.density = 1.0f;
 		fixtureDef.friction = 1.0f;
 		
 		entity.createFixture(fixtureDef);		
 		//shape.dispose();
 		
 		entity.setUserData("PLAYER");
 		
 			/******************************************************/
 
 			/******************************************************/
 		//--shout
 		 shoutText = new Texture(Gdx.files.internal("data/shockwave.png"));
 		 shoutText.setFilter(TextureFilter.Linear, TextureFilter.Linear);
 	     shoutSprite = new Sprite(shoutText, 0, 0, 64, 128);
 	     
 	     shoutBodyDef = new BodyDef();
 		 shoutBodyDef.type = BodyDef.BodyType.StaticBody;
 		 shoutBodyDef.position.set(x, y);
 		 
 	   	 shoutBody = world.createBody(shoutBodyDef);
 	   	 
 	   	 shoutSound = Gdx.audio.newSound(Gdx.files.getFileHandle("data/sounds/RandomSound.wav", FileType.Internal));
 	   	 
 	   	 PolygonShape shoutShape = new PolygonShape();
 		    shoutShape.setAsBox(shoutSprite.getWidth() / ( 2f*PIXELS_PER_METER),
 					sprite.getHeight() / PIXELS_PER_METER);			
 			
 		shoutFixtureDef = new FixtureDef();
 		shoutFixtureDef.shape = shoutShape;
 		shoutFixtureDef.density = 1.0f;
 		shoutFixtureDef.friction = 1.0f;
 
 		
 		
 		shoutBody.createFixture(shoutFixtureDef);
 
 		shoutShape.dispose();
 		
 		shoutBody.setUserData("SHOUT");
 		
 		//--tongue
 	   	tongueText = new Texture(Gdx.files.internal("data/alabaster/tongue.png"));
 	    tongueSprite = new Sprite(tongueText, 0, 0, 64, 64);
 	   	 
 	   	tongueBodyDef = new BodyDef();
 	   	tongueBodyDef.type = BodyDef.BodyType.StaticBody;
 	   	tongueBodyDef.position.set(entity.getPosition().x,entity.getPosition().y);
 	   	
 		tongueBody = world.createBody(tongueBodyDef);
 		
 		tongueSound = Gdx.audio.newSound(Gdx.files.getFileHandle("data/sounds/jump.wav", FileType.Internal));
 		
 		PolygonShape tongueShape = new PolygonShape();
 		tongueShape.setAsBox(tongueSprite.getWidth() / (2 * PIXELS_PER_METER),
 					tongueSprite.getHeight() / (5 * PIXELS_PER_METER));
 		
 		tongueBody.setFixedRotation(true);
 		    
 		tongueFixtureDef = new FixtureDef();
 		tongueFixtureDef.shape = tongueShape;
 		tongueFixtureDef.density = 1.0f;
 		tongueFixtureDef.friction = 1.0f;
 		
 		tongueBody.createFixture(tongueFixtureDef);
 		
 		tongueBody.setUserData("TONGUE");
 		
 		//--foot
 		
 		 footDef = new BodyDef();
 		 footDef.type = BodyDef.BodyType.DynamicBody;
 		 footDef.position.set(entity.getPosition().x,entity.getPosition().y);
 	   	 foot = world.createBody(footDef);
 	   	 PolygonShape footShape = new PolygonShape();
 		    footShape.setAsBox(sprite.getWidth() / (2.3F * PIXELS_PER_METER),
 					sprite.getHeight() / (8f * PIXELS_PER_METER));
 		    foot.setFixedRotation(true);
 			 
 		footFix = new FixtureDef();
 	    footFix.shape= footShape;
 	    footFix.isSensor=true;
 		foot.createFixture(footFix);
 		footShape.dispose();
 		
 		foot.setUserData("FOOT");
 		    
 		//--spit			
 		spitTexture = new Texture(Gdx.files.internal("data/spit.png"));
 	    spitSprite = new Sprite(spitTexture, 0, 0, 64, 64);
 	   	 
 	   	spitBodyDef = new BodyDef();
 	   	spitBodyDef.type = BodyDef.BodyType.KinematicBody;
 	   	//spitBodyDef.position.set(entity.getPosition().x+bodyOffset,entity.getPosition().y + bodyOffset);
 	   	
 		spitBody = world.createBody(spitBodyDef);
 		
 		spitSound = Gdx.audio.newSound(Gdx.files.getFileHandle("data/sounds/bearLaser.wav", FileType.Internal));
 		
 		PolygonShape spitShape = new PolygonShape();
 		spitShape.setAsBox(spitSprite.getWidth() / (5 * PIXELS_PER_METER),
 					spitSprite.getHeight() / (2 * PIXELS_PER_METER));
 		
 		spitBody.setFixedRotation(true);
 		    
 		spitFixtureDef = new FixtureDef();
 		
 		/*set bullet as sensor*/
 		spitFixtureDef.isSensor = true;		
 		spitFixtureDef.shape = tongueShape;
 		spitFixtureDef.density = 1.0f;
 		spitFixtureDef.friction = 1.0f;
 		
 		spitBody.createFixture(spitFixtureDef);
 		
 		spitShape.dispose();
 		
 		spitBody.setUserData("SPIT");
 		
 		//--shell			     
 		shellText = new Texture(Gdx.files.internal("data/flyJar.png"));
 	    shellSprite = new Sprite(shellText, 0, 0, 32, 32);
 	    shellSound = Gdx.audio.newSound(Gdx.files.getFileHandle("data/sounds/shellSound.wav", FileType.Internal));
 	    
 	    /*sounds*/
 	    //--jump
 	    jumpSound = Gdx.audio.newSound(Gdx.files.getFileHandle("data/sounds/jump.wav", FileType.Internal));
 		
 		// HUD
 		healthText = new SpriteBatch();
 		font = new BitmapFont();
 	    font.setColor(1.0f, 1.0f, 1.0f, 1.0f);
 		ShapeRenderer = new ShapeRenderer();
 		
 		// alabaster head
 		icon = new SpriteBatch();//texture, 0, 0, 0, 32);
 		//icon.flip(true, false);
 	}
 
 public void displayHUD() {   
 	// display health
 	k = (100f - health) / 100f;
 	
 	ShapeRenderer.begin(ShapeType.FilledRectangle);
 	
 	// health
 	ShapeRenderer.setColor(0f, 0f, 0f, 1.0f);
 	ShapeRenderer.identity();
 	ShapeRenderer.translate(40.0f, (680-35), 0.f);
 	ShapeRenderer.filledRect(0f, 0f, 200, 20f);
 	ShapeRenderer.setColor((255*k)/100, 255*(1.0f - k)/100, 0f, 1.0f);
 	ShapeRenderer.identity();
 	ShapeRenderer.translate(40.0f, (680-35), 0.f);
 	ShapeRenderer.filledRect(0f, 0f, 2*health, 20f);
 	
 	// shell
 	ShapeRenderer.setColor(0f, 0f, 0f, 1.0f);
 	ShapeRenderer.identity();
 	if(health > 100)
 		ShapeRenderer.translate(2*health+10+40, (680-30), 0.f);
 	else
 		ShapeRenderer.translate(2*100+10+40, (680-30), 0.f);
 	ShapeRenderer.filledRect(0f, 0f, 100, 15f);
 	ShapeRenderer.setColor(1.0f, .77f, .05f, 1.0f);
 	ShapeRenderer.identity();
 	if(health > 100)
 		ShapeRenderer.translate(2*health+10+40, (680-30), 0.f);
 	else
 		ShapeRenderer.translate(2*100+10+40, (680-30), 0.f);
 	ShapeRenderer.filledRect(0f, 0f, shellCharge, 15f);
 	
 	// spit
 	ShapeRenderer.setColor(0f, 0f, 0f, 1.0f);
 	ShapeRenderer.identity();
 	if(health > 100)
 		ShapeRenderer.translate(2*health+40+120, (680-30), 0.f);
 	else
 		ShapeRenderer.translate(2*100+40+120, (680-30), 0.f);
 	ShapeRenderer.filledRect(0f, 0f, 100, 15f);
 	ShapeRenderer.setColor(0.52f, 0.38f, .53f, 1.0f);
 	ShapeRenderer.identity();
 	if(health > 100)
 		ShapeRenderer.translate(2*health+40+120, (680-30), 0.f);
 	else
 		ShapeRenderer.translate(2*100+40+120, (680-30), 0.f);
 	ShapeRenderer.filledRect(0f, 0f, spitCharge, 15f);
 	
 	// shock-wave
 	ShapeRenderer.setColor(0f, 0f, 0f, 1.0f);
 	ShapeRenderer.identity();
 	if(health > 100)
 		ShapeRenderer.translate(2*health+40+230, (680-30), 0.f);
 	else
 		ShapeRenderer.translate(2*100+40+230, (680-30), 0.f);
 	ShapeRenderer.filledRect(0f, 0f, 100, 15f);
 	ShapeRenderer.setColor(0.18f, 0.46f, 1.0f, 1.0f);
 	ShapeRenderer.identity();
 	if(health > 100)
 		ShapeRenderer.translate(2*health+40+230, (680-30), 0.f);
 	else
 		ShapeRenderer.translate(2*100+40+230, (680-30), 0.f);
 	ShapeRenderer.filledRect(0f, 0f, shoutCharge, 15f);
 	
 	// Icon Background
 	ShapeRenderer.setColor(0f, 0f, 0f, 1.0f);
 	ShapeRenderer.identity();
 	ShapeRenderer.translate(0f, (680-40), 0.f);
 	ShapeRenderer.filledRect(0f, 0f, 40f, 40f);
 	ShapeRenderer.end();
 	
 	// BORDER
 	ShapeRenderer.begin(ShapeType.Rectangle);
 	
 	// health
 	ShapeRenderer.setColor((255*k)/100, 255*(1.0f - k)/100, 0f, 1.0f);
 	ShapeRenderer.identity();
 	ShapeRenderer.translate(40.0f, (680-35), 0.f);
 	if(health > 100)
 		ShapeRenderer.rect(0f, 0f, 2*health, 20f);
 	else
 		ShapeRenderer.rect(0f, 0f, 2*100, 20f);
 	
 	// shell 
 	ShapeRenderer.setColor(1.0f, .77f, .05f, 1.0f);
 	ShapeRenderer.identity();
 	if(health > 100)
 		ShapeRenderer.translate(2*health+40+120, (680-30), 0.f);
 	else
 		ShapeRenderer.translate(2*100+40+120, (680-30), 0.f);
 	ShapeRenderer.rect(0f, 0f, 100, 15f);
 	
 	// spit
 	ShapeRenderer.setColor(0.52f, 0.38f, .53f, 1.0f);
 	ShapeRenderer.identity();
 	if(health > 100)
 		ShapeRenderer.translate(2*health+40+120, (680-30), 0.f);
 	else
 		ShapeRenderer.translate(2*100+40+120, (680-30), 0.f);
 	ShapeRenderer.rect(0f, 0f, 100, 15f);
 	
 	// shock-wave
 	ShapeRenderer.setColor(0.18f, 0.46f, 1.0f, 1.0f);
 	ShapeRenderer.identity();
 	if(health > 100)
 		ShapeRenderer.translate(2*health+40+230, (680-30), 0.f);
 	else
 		ShapeRenderer.translate(2*100+40+230, (680-30), 0.f);
 	ShapeRenderer.rect(0f, 0f, 100, 15f);
 	ShapeRenderer.end();
 	
 	// Print TEXT
 	healthText.begin();
 	font.draw(healthText, "HEALTH" , 40.0f, 680);
 	if(health > 100)
 		font.draw(healthText, "SHELL" , 2*health+10+40, 680);
 	else
 		font.draw(healthText, "SHELL" , 2*100+10+40, 680);
 	if(health > 100)
 		font.draw(healthText, "SPIT" , 2*health+40+120, 680);
 	else
 		font.draw(healthText, "SPIT" , 2*100+40+120, 680);
 	if(health > 100)
 		font.draw(healthText, "SHOCKWAVE" , 2*health+40+230, 680);
 	else
 		font.draw(healthText, "SHOCKWAVE" , 2*100+40+230, 680);
 	healthText.end();
 	
 	// draw Alabaster Icon
 	icon.begin();
 	//icon.setPosition(0,650f);
	icon.draw(texture,0,680-60,55,55);
 	icon.end();
 	
 }
 
 	
 public void move(MyInputProcessor input) 
 {	  				
 	System.out.println(count);
 	foot.setTransform(entity.getPosition().x,entity.getPosition().y-.36f,0);
 	foot.applyForceToCenter(0.0f,10f);
 	tongueBody.setActive(false);
 	boolean moveLeft = false;
 	boolean moveRight = false;
 	shout = false;
 	shoutBody.setActive(false);
 	shell = false;
 	spit = false;
 		
 		//jumping
 
 		if(!input.buttons[MyInputProcessor.JUMP] && input.oldButtons[MyInputProcessor.JUMP] && !shell)
 	    {
 			
 			EnemyContact.grounded=false;
 			if(powerLegs)
 			{
 				
 				if(count > 0&&doubleJump)
 				{
 			
 					jumpSound.setLooping(1, false);
 					jumpSound.play();
 					entity.applyLinearImpulse(new Vector2(0.0f,4.8f),entity.getWorldCenter());
 					if(count==1)doubleJump = false;
 				    count--;
 					
 				}
 			}
 			else
 			{
 	
               if(count==2)
               {
 					jumpSound.setLooping(1, false);
 					jumpSound.play();
 					entity.applyLinearImpulse(new Vector2(0.0f,4.8f),entity.getWorldCenter());
 				count--;
               }	
 			}
 		}
 
 		if(EnemyContact.grounded)
 		{
 			count=2; 
 			doubleJump=true;
 		}
 		if(count==0)		
 			count=2;
 	
 	    //move left
   		if (Gdx.input.isKeyPressed(Input.Keys.DPAD_LEFT)) 
   			moveLeft = true;		
   	
   		//move right
   		if (Gdx.input.isKeyPressed(Input.Keys.DPAD_RIGHT)) 
   			moveRight = true;	
   		
   		if (Gdx.input.isKeyPressed(Input.Keys.S)) {
 
 	  		if(shellCharge > 0) {
 				moveRight = false;
 				moveLeft=false;
 		// prevent tongue or shout usage
 				tongueAct = false;
 				tongueOut = false;
 				tongueBody.setActive(false);
 				shout = false;
 				shoutBody.setActive(false);
 				shell = true;
 				entity.applyLinearImpulse(new Vector2(0, -3.4f), entity.getPosition());
 				shellSound.setLooping(1,false);
 				shellSound.play();
 			}
 			else {
 				
 			}
   		}
 		
 //		if (Gdx.input.isKeyPressed(Input.Keys.A)) {
 //			if(shoutCharge > 0) {
 //	// prevent tongue or shout usage
 //			tongueAct = false;
 //			tongueOut = false;
 //			tongueBody.setActive(false);
 //			shout = true;
 //			shoutBody.setActive(true);
 //			shoutSound.play();
 //		}
 //		else {
 //			
 //		}
 //
 //	}
   		
   		if (moveRight) 
   		{
   			entity.applyLinearImpulse(new Vector2(.15f, 0.0f),
   			entity.getWorldCenter());
   			
   			if (facingRight == false)
   			{
   				sprite.flip(true, false);
   				tongueSprite.flip(true, false);
   				shoutSprite.flip(true,false);
   				spitSprite.flip(true, false);
   			}
   		
   			facingRight = true;
   		} 
   		else if (moveLeft)
   		{
   			entity.applyLinearImpulse(new Vector2(-.15f, 0.0f),
   			entity.getWorldCenter());
   			if (facingRight == true)
   			{
   				sprite.flip(true, false);
   				tongueSprite.flip(true, false);
   				shoutSprite.flip(true,false);
   				spitSprite.flip(true, false);
   			}
   			facingRight = false;
   		}
 
   		/****************************************************************
   		 * SPIT INPUT  		 											* 
   		 ***************************************************************/
 	    if(!input.buttons[MyInputProcessor.SPIT] && input.oldButtons[MyInputProcessor.SPIT] && !shell)
 	    {
 	    	if(spitCharge > 0) {
 	    		spitSound.play();
 		    	spit = true;
 		    	spitBody.setActive(true);
 		    	spitSound.play();
 	    	
 	    	//System.out.print("spit");
 	    	//spitBody.setLinearVelocity(new Vector2(2.0f, 0.0f));
 			Body b = world.createBody(spitBodyDef);
 			b.createFixture(spitFixtureDef);
 	    	
 			b.setUserData("SPIT");
 	    	
 			if(facingRight)
 			{
 				b.setTransform(entity.getPosition().x+bodyOffset,entity.getPosition().y,0);
 				b.setLinearVelocity(spitVel, 0.0f);
 				
 			}
 			else
 			{
 				b.setTransform(entity.getPosition().x-bodyOffset,entity.getPosition().y,0);
 				b.setLinearVelocity(-spitVel, 0.0f);
 			}			
 			spits.add(b);
 	    	}
 		}	
 	    /****************************************************************
   		 * TONGUE INPUT  		 											* 
   		 ***************************************************************/
 	    //--hold out tongue .25 secs
 	    if(!input.buttons[MyInputProcessor.TONGUE] && input.oldButtons[MyInputProcessor.TONGUE] && !tongueOut &&!shell)
 	    {
 	    	tongueOut = true;
 	    	now = System.nanoTime();
	    	System.out.println("tongue");
 	    	tongueBody.setActive(true);
 	    	if(facingRight)
   				tongueBody.setTransform(entity.getPosition().x+1f,entity.getPosition().y,0);	
   			else
   				tongueBody.setTransform(entity.getPosition().x-1f,entity.getPosition().y,0);    	
 	    }
 	    
 	    /****************************************************************
   		 * SHOUT INPUT  		 											* 
   		 ***************************************************************/
 	    if(input.buttons[MyInputProcessor.SHOUT] && !input.oldButtons[MyInputProcessor.SHOUT] && !shell)
 	    	
 	    {
 	    	if(shoutCharge > 0) {
 		    	shout = true;
 		    	shoutBody.setActive(true);
 		        shoutSound.play();
 	    	}
 			if(facingRight)
 				shoutBody.setTransform(entity.getPosition().x+1.1f,entity.getPosition().y,0);	
 			else
 				shoutBody.setTransform(entity.getPosition().x-1.1f,entity.getPosition().y,0);
 		
 	     }
 	    		    	
 	    
 	    
 	    if(tongueOut)
 	    {
 	    	if((System.nanoTime() - now)/1000000 > 250)
     		{
 	    		tongueOut = false;
 	    		tongueBody.setActive(false);
     		}
 	    }
 
 	    /****************************************************************
   		 * UPDATE SPITS		 											* 
   		 ***************************************************************/
 	    //destroy spit if too far or collided w/something
         for(Body b: spits)
         {
         	if(b == null) break;        	
 	        	//System.out.println(spits.size());
 	        	if(b.getPosition().x < entity.getPosition().x - 2*580/PIXELS_PER_METER || b.getPosition().x > entity.getPosition().x + 2*580/PIXELS_PER_METER)
 	        	{
 	        		//System.out.println("removing");
 	        		
 	        		//world.destroyBody(b);
 	        		//System.out.println(spits.remove(b));
 	        		//spits.clear();
 	        		
 	        		int i = spits.indexOf(b);
 	        		//spits.set(i, null);
 	        		
 	        		//world.destroyBody(b);
 	        		spits.set(i,null);
 	        		world.destroyBody(b);
 	        		//b.setActive(false);
 	        	}
 	        	else
 	        	{
 	        		/*if(b.getLinearVelocity().x > 0)
 	        			b.setLinearVelocity(spitVel, 0.0f);
 	        		else if (b.getLinearVelocity().x < 0)
 	        			b.setLinearVelocity(-spitVel, 0.0f);
 	        		
 	        		if(b.getLinearVelocity().x ==0)
 	        		{
 	        			b.setLinearVelocity(spitVel, 0.0f);
 	        			//b.applyLinearImpulse(new Vector2(1.0f,0.0f),entity.getWorldCenter());
 	        		}*/
 	        	}
         }
         
         if(spitBody.getLinearVelocity().x < 2.0f)
         {
         	spitBody.setLinearVelocity(2.0f, 0.0f);
         }
         
 	}
 	
 	@Override
 	public void attack() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void speak() {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	public void die() {
 		setHealth(0);
 		world.destroyBody(shoutBody);
 		world.destroyBody(tongueBody);
 		spitSound.dispose();
 		shoutSound.dispose();
 		shellSound.dispose();
 		jumpSound.dispose();
 		System.out.println("youre dead! NEW LIFE");
 	}
 
 public void batchRender(TiledMapHelper tiledMapHelper) {	
 	batch.setProjectionMatrix(tiledMapHelper.getCamera().combined);
 	batch.begin();
 	
 		if(!shell){
 				sprite.setPosition(
 						PIXELS_PER_METER * entity.getPosition().x
 								- sprite.getWidth() / 2,
 						PIXELS_PER_METER * entity.getPosition().y
 								- sprite.getHeight() / 2);
 				sprite.draw(batch);
 		}else{
 			shellSprite.setPosition(
 					PIXELS_PER_METER * entity.getPosition().x
 							- shellSprite.getWidth() / 2,
 					PIXELS_PER_METER * entity.getPosition().y
 							- shellSprite.getHeight() / 2);
 				shellSprite.draw(batch);
 				
 		}
 		if(tongueOut){			
 			if(facingRight){		
 				tongueSprite.setPosition(
 						PIXELS_PER_METER * entity.getPosition().x+60
 								- sprite.getWidth() / 2,
 						PIXELS_PER_METER * entity.getPosition().y-22
 								- sprite.getHeight() / 2);			
 				tongueSprite.draw(batch);
 			}
 			else{
 				
 		    	tongueSprite.setPosition(
 						PIXELS_PER_METER * entity.getPosition().x-58
 								- sprite.getWidth() /2,
 						PIXELS_PER_METER * entity.getPosition().y-22
 								- sprite.getHeight() / 2);
 		    	 tongueSprite.draw(batch);
 				}			   
 		}
 			if(shout){
 				if(facingRight){
 					shoutSprite.setPosition(
 							PIXELS_PER_METER * entity.getPosition().x+64f
 									- shoutSprite.getWidth() / 2,
 							PIXELS_PER_METER * entity.getPosition().y+10f
 									- shoutSprite.getHeight() / 2);				
 					shoutSprite.draw(batch);
 				}
 				else{	  		
 			    	shoutSprite.setPosition(
 							PIXELS_PER_METER * entity.getPosition().x-64f
 									- shoutSprite.getWidth() /2,
 							PIXELS_PER_METER * entity.getPosition().y+10f
 									- shoutSprite.getHeight() / 2);
 			    	 shoutSprite.draw(batch);
 					}			 
 			}
 			if(spit)
 			{
 				if(facingRight){
 					spitSprite.setPosition(
 							PIXELS_PER_METER * spitBody.getPosition().x+64f
 									- spitSprite.getWidth() / 2,
 							PIXELS_PER_METER * spitBody.getPosition().y+10f
 									- spitSprite.getHeight() / 2);				
 					spitSprite.draw(batch);
 				}
 				else{	  		
 					spitSprite.setPosition(
 							PIXELS_PER_METER * spitBody.getPosition().x-64f
 									- spitSprite.getWidth() /2,
 							PIXELS_PER_METER * spitBody.getPosition().y+10f
 									- spitSprite.getHeight() / 2);
 					spitSprite.draw(batch);
 					}			 
 			}
 			for(Body b: spits)
 			{
 				if(b == null) break;
 				
 					spitSprite.setPosition(
 							PIXELS_PER_METER * b.getPosition().x
 									- spitSprite.getWidth() / 2,
 							PIXELS_PER_METER * b.getPosition().y
 									- spitSprite.getHeight() / 2);				
 					spitSprite.draw(batch);
 			}
 		batch.end();
 }
 
 	@Override
 	public void reset() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void move() {
 		// TODO Auto-generated method stub
 		
 	}	
 }
