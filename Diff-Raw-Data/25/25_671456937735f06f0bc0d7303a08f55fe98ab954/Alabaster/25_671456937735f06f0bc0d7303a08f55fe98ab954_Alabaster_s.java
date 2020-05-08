 package com.me.ampdom;
 
 import java.util.ArrayList;
 
 import com.badlogic.gdx.Files.FileType;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Input;
 import com.badlogic.gdx.audio.Sound;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.Texture.TextureFilter;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.Body;
 import com.badlogic.gdx.physics.box2d.BodyDef;
 import com.badlogic.gdx.physics.box2d.EdgeShape;
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
 	boolean upKey;
 	boolean shell = false;
 	boolean shout = false;
 	boolean tongueAct=false;
 	boolean powerLegs=true;
 	boolean spit = false;
 	boolean tongueOut = false;
 	boolean doubleJump;
 	
 	//shell stuff
 	Sprite shellSprite;
 	Texture shellText;
 	
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
 	Sound jump;	
 	
 	//doublejump sensor
 	FixtureDef doubleJumpFixtureDef;
 	Body doubleJumpBody;
 	BodyDef doubleJumpBodyDef;
 	
 	final float spitVel = 5.0f;
 	
 	long now, last;
 	
 	public Alabaster(World world, float x, float y) {
 		super(world, x, y);	
 		/*setup physics n sounds*/
 		//--alabaster
 		texture = new Texture(Gdx.files.internal("data/Alabaster/alabasterS.png"));
 		texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
 		sprite = new Sprite(texture, 0, 0, 64, 51);//21, 37);
 		
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
 			//--double jump sensor			
 			EdgeShape doubleJumpSensorShape = new EdgeShape();
 			float xOffset = sprite.getWidth() / (2 * PIXELS_PER_METER);
 			float yOffset = sprite.getHeight() / (2 * PIXELS_PER_METER);
 			
 			/*doubleJumpBodyDef = new BodyDef();
 			doubleJumpBodyDef.position.set(x, y-sprite.getHeight()/(2 * PIXELS_PER_METER));
 			doubleJumpBodyDef.type = BodyDef.BodyType.KinematicBody;
 			
 			doubleJumpBody = world.createBody(doubleJumpBodyDef);
 			doubleJumpBody.setUserData("SENSOR");			*/
 			
 			//doubleJumpSensorShape.set((x-60)/PIXELS_PER_METER, (y-sprite.getHeight())/(PIXELS_PER_METER), (x+60)/PIXELS_PER_METER, (y-sprite.getHeight())/(PIXELS_PER_METER));
 			
 			float yVal = (y-sprite.getHeight())/PIXELS_PER_METER;
 			
 			
 		    doubleJumpFixtureDef = new FixtureDef();
 		    doubleJumpFixtureDef.isSensor = true;
 		    doubleJumpFixtureDef.shape = doubleJumpSensorShape;
 		    doubleJumpFixtureDef.density = 1.0f;
 		    doubleJumpFixtureDef.friction = 1.0f;
 			
 		    doubleJumpSensorShape.set((x-60)/PIXELS_PER_METER, yVal, (x+60)/PIXELS_PER_METER, yVal);
 		    
 		   // doubleJumpSensorShape.set((x)/PIXELS_PER_METER, y/PIXELS_PER_METER, x/PIXELS_PER_METER, (y-sprite.getHeight())/PIXELS_PER_METER);
 		    
 		    //attach to frog body
 			entity.createFixture(doubleJumpFixtureDef);		
 			
 			doubleJumpSensorShape.dispose();
 			/******************************************************/
 		//--shout
 		 shoutText = new Texture(Gdx.files.internal("data/shockwave.png"));
 		 shoutText.setFilter(TextureFilter.Linear, TextureFilter.Linear);
 	     shoutSprite = new Sprite(shoutText, 0, 0, 64, 128);
 	     
 	     shoutBodyDef = new BodyDef();
 		 shoutBodyDef.type = BodyDef.BodyType.StaticBody;
 		 shoutBodyDef.position.set(x, y);
 		 
 	   	 shoutBody = world.createBody(shoutBodyDef);
 	   	 
 	   	 shoutSound = Gdx.audio.newSound(Gdx.files.getFileHandle("data/sounds/jump.wav", FileType.Internal));
 	   	 
 	   	 PolygonShape shoutShape = new PolygonShape();
 		    shoutShape.setAsBox(shoutSprite.getWidth() / ( 1.5f*PIXELS_PER_METER)-.4f,
 					sprite.getHeight() / (PIXELS_PER_METER/2));			
 			
 		shoutFixtureDef = new FixtureDef();
 		shoutFixtureDef.shape = shoutShape;
 		shoutFixtureDef.density = 1.0f;
 		shoutFixtureDef.friction = 1.0f;
 
 		
 		
 		shoutBody.createFixture(shoutFixtureDef);
 
 		shoutShape.dispose();
 		
 		shoutBody.setUserData("SHOUT");
 		
 		//--tongue
 	   	tongueText = new Texture(Gdx.files.internal("data/shockwave.png"));
 	    tongueSprite = new Sprite(shoutText, 0, 0, 64, 128);
 	   	 
 	   	tongueBodyDef = new BodyDef();
 	   	tongueBodyDef.type = BodyDef.BodyType.StaticBody;
 	   	tongueBodyDef.position.set(entity.getPosition().x,entity.getPosition().y);
 	   	
 		tongueBody = world.createBody(tongueBodyDef);
 		
 		tongueSound = Gdx.audio.newSound(Gdx.files.getFileHandle("data/sounds/jump.wav", FileType.Internal));
 		
 		PolygonShape tongueShape = new PolygonShape();
 		tongueShape.setAsBox(sprite.getWidth() / (2 * PIXELS_PER_METER),
 					sprite.getHeight() / (2 * PIXELS_PER_METER));
 		
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
 		    footShape.setAsBox(sprite.getWidth() / (3 * PIXELS_PER_METER),
 					sprite.getHeight() / (10 * PIXELS_PER_METER));
 		    foot.setFixedRotation(true);
 			 
 		footFix = new FixtureDef();
 	    footFix.shape= footShape;
 	    footFix.isSensor=true;
 	    footFix.density = 1.0f;
 		footFix.friction = 1.0f;
 		foot.createFixture(footFix);
 		footShape.dispose();
 		
 		foot.setUserData("FOOT");
 		    
 		//--spit			
 		spitTexture = new Texture(Gdx.files.internal("data/spit1.png"));
 	    spitSprite = new Sprite(spitTexture, 0, 0, 32, 32);
 	   	 
 	   	spitBodyDef = new BodyDef();
 	   	spitBodyDef.type = BodyDef.BodyType.KinematicBody;
 	   	//spitBodyDef.position.set(entity.getPosition().x+bodyOffset,entity.getPosition().y + bodyOffset);
 	   	
 		spitBody = world.createBody(spitBodyDef);
 		
 		spitSound = Gdx.audio.newSound(Gdx.files.getFileHandle("data/sounds/jump.wav", FileType.Internal));
 		
 		PolygonShape spitShape = new PolygonShape();
 		spitShape.setAsBox(spitSprite.getWidth() / (2 * PIXELS_PER_METER),
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
 	    
 	    /*sounds*/
 	    //--jump	    
 		jump = Gdx.audio.newSound(Gdx.files.getFileHandle("data/sounds/jump.wav", FileType.Internal));		
 	}
 
 	
 	
 public void move(MyInputProcessor input) 
 {	  				
 		tongueBody.setActive(false);
 		boolean doJump = false;
 		boolean moveLeft = false;
 		boolean moveRight = false;
 		shout = false;
 		shoutBody.setActive(false);
 		
 		 if(EnemyContact.grounded)
 		 {
 			  upKey = false;
 		    	doubleJump = true;
 		 }
 		 
 		 shell= false;
 		
 		//jumping
 
 		if(!input.buttons[input.JUMP] && input.oldButtons[input.JUMP])
 	    {
 			if(powerLegs)
 			{
				/*if(count > 0)
				{
 					entity.applyLinearImpulse(new Vector2(0.0f,4.5f),entity.getWorldCenter());
					count--;
				}*/
 			}
 			else
 			{
 				//if on ground apply linear impulse
 			}
		}

 		
 	    //move left
   		if (Gdx.input.isKeyPressed(Input.Keys.DPAD_LEFT)) 
   			moveLeft = true;		
   	
   		//move right
   		if (Gdx.input.isKeyPressed(Input.Keys.DPAD_RIGHT)) 
   			moveRight = true;	
   		
   		if (Gdx.input.isKeyPressed(Input.Keys.S)) {
 			moveRight = false;
 		   moveLeft=false;
 		   doJump=false;
 		   shell = true;
 		}
   		
   		if (moveRight) 
   		{
   			entity.applyLinearImpulse(new Vector2(.15f, 0.0f),
   			entity.getWorldCenter());
   			
   			if (facingRight == false)
   			{
   				sprite.flip(true, false);
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
   				shoutSprite.flip(true,false);
   				spitSprite.flip(true, false);
   			}
   			facingRight = false;
   		}
 
   		/****************************************************************
   		 * SPIT INPUT  		 											* 
   		 ***************************************************************/
 	    if(!input.buttons[input.SPIT] && input.oldButtons[input.SPIT])
 	    {
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
 	    /****************************************************************
   		 * TONGUE INPUT  		 											* 
   		 ***************************************************************/
 	    //--hold out tongue .25 secs
 	    if(!input.buttons[input.TONGUE] && input.oldButtons[input.TONGUE] && !tongueOut)
 	    {
 	    	tongueOut = true;
 	    	now = System.nanoTime();
 	    	System.out.println("tongue");
 	    	tongueBody.setActive(true);
 	    	if(facingRight)
   				tongueBody.setTransform(entity.getPosition().x+.64f,entity.getPosition().y,0);	
   			else
   				tongueBody.setTransform(entity.getPosition().x-.64f,entity.getPosition().y,0);    	
 	    }
 	    
 	    /****************************************************************
   		 * SHOUT INPUT  		 											* 
   		 ***************************************************************/
 	    if(input.buttons[input.SHOUT] && !input.oldButtons[input.SHOUT])
 	    	
 	    { 
 	    	shout = true;
 	    	shoutBody.setActive(true);
 	        shoutSound.play();
 			if(facingRight)
 			shoutBody.setTransform(entity.getPosition().x+.64f,entity.getPosition().y,0);	
 			else
 		    shoutBody.setTransform(entity.getPosition().x-.64f,entity.getPosition().y,0);
 		
 	     }
 	    		    	
 	    foot.setTransform(entity.getPosition().x,entity.getPosition().y-.36f,0);
 	    
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
 	        	if(b.getPosition().x < entity.getPosition().x - 2*800/PIXELS_PER_METER || b.getPosition().x > entity.getPosition().x + 2*800/PIXELS_PER_METER)
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
 				sprite.setPosition(
 						PIXELS_PER_METER * entity.getPosition().x+30
 								- sprite.getWidth() / 2,
 						PIXELS_PER_METER * entity.getPosition().y
 								- sprite.getHeight() / 2);			
 				sprite.draw(batch);
 			}
 			else{
 				
 		    	sprite.setPosition(
 						PIXELS_PER_METER * entity.getPosition().x-30
 								- sprite.getWidth() /2,
 						PIXELS_PER_METER * entity.getPosition().y
 								- sprite.getHeight() / 2);
 		    	 sprite.draw(batch);
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
 	public void reset(float x, float y) {
 		entityDef.position.set(x, y);
 		System.out.println(x+"+"+y);
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
