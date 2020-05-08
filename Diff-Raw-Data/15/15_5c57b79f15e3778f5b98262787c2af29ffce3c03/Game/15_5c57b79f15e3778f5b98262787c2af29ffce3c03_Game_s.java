 package com.me.ProjectGreg;
 
 //import com.me.ProjectGreg.*;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.Random;
 
 import com.badlogic.gdx.ApplicationListener;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.math.Rectangle;
 import com.me.ProjectGreg.Heartbeat;
 
 public class Game implements ApplicationListener 
 {
 	public class Task extends TimerTask 
 	{
 		@Override
 		public void run()
 		{
 			Heartbeat.INSTANCE().calmDown();
 		}
 	}
 	
 	Random random = new Random();
 	private static OrthographicCamera camera;
 	SpriteBatch batch;
 	SpriteBatch playerBatch;
 	private Texture playerTexture;
 	private Texture backgroundTexture;
 	private Texture backgroundTexture2;
 	private Texture backgroundTexture3;
 	private Sprite backgroundSprite;
 //	private Texture heartMeter, heartMeter2;
 //	private Sprite heartMeterSprite, heartMeter2Sprite;
 	private Platform []level;
 	private Player player;
 //	private static Sound Wilhelm;
 
 
 	@Override
 	public void create() {		
 		float w = Gdx.graphics.getWidth();
 		float h = Gdx.graphics.getHeight();
 		//level = new Platform[1];
 		player = new Player(200, 100);
 		/*for(int i = 0; i<level.length; i++)
 		{
 			level[i] = new Platform(this);
 		}*/
 		camera = new OrthographicCamera(w, h);
 		batch = new SpriteBatch();
 		//playerBatch = new SpriteBatch();
 		playerTexture = new Texture(Gdx.files.internal("data/GregIdle.png"));
         backgroundTexture = new Texture(Gdx.files.internal("data/Background1.png"));
        backgroundTexture2 = new Texture(Gdx.files.internal("data/background2.png"));
        backgroundTexture3 = new Texture(Gdx.files.internal("data/background3.png"));
         
 		level = new Level("").getTiles();
 		
 		/*TextureRegion playerRegion = new TextureRegion(playerTexture, 20, 0, 64, 64);
 		playerSprite = new Sprite(playerRegion);
 		playerSprite.setSize(64,64);*/
 		TextureRegion backgroundRegion = new TextureRegion(backgroundTexture, 0, 0, 1224, 712);
 		backgroundSprite = new Sprite(backgroundRegion);
 		backgroundSprite.setPosition(-300, -690);
 	//  heartMeter = new Texture(Gdx.files.internal("data/Heartmeter.png"));
 	//	heartMeter2 = new Texture(Gdx.files.internal("data/Heartmeter2.png"));
 	//	heartMeterSprite = new Sprite(new TextureRegion(heartMeter,0,0,128,32));
 	//	heartMeter2Sprite = new Sprite(new TextureRegion(heartMeter2,0,0,32,32));
 		//backgroundSprite.setSize(720,480);
 		//player.setSprite(playerSprite);
 		//rect.set();
 		Task heartRateTask = new Task();
 		Timer heartTimer = new Timer();
 		
 		heartTimer.schedule(heartRateTask, 0, 500);
 	//	Wilhelm = Gdx.audio.newSound(Gdx.files.internal("sound/Wilhelm.mp3"));
 	}
 
 	@Override
 	public void dispose() {
 		batch.dispose();
 		playerTexture.dispose();
 	}
 
 	@Override
 	public void render() {		
 		Gdx.gl.glClearColor(1, 1, 1, 1);
 		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 		//camera.translate(.01f,0f);
 		batch.setProjectionMatrix(camera.combined);
 		//playerBatch.begin();
 		batch.begin();
 		float bgx = backgroundSprite.getX();
 		float bgy = backgroundSprite.getY();
 		
 		switch(level[1].darkness)
 		{
 		case 0: backgroundSprite = new Sprite(new TextureRegion(backgroundTexture3,0 ,0 , 1080,1080));
 				break;
 		case 1:	backgroundSprite = new Sprite(new TextureRegion(backgroundTexture2,0 ,0 , 1080,1080));
 				break;
 		case 2:	backgroundSprite = new Sprite(new TextureRegion(backgroundTexture,0 ,0 , 1080,1080));
 				break;
 		}
 		
 		backgroundSprite.setPosition((float)(bgx+player.getCurrentXSpeed()), (float)(bgy+player.getCurrentYSpeed()));
 		backgroundSprite.draw(batch);
 		if(player.getY()<-1500||Heartbeat.INSTANCE().getRate()>.9f)
 		{
 		//	Wilhelm.play(.5f);
 			player.setX(130);
 			player.setY(150);
 			player.setCurrentYSpeed(0);
 			backgroundSprite.setPosition(-350, -640);
 			Heartbeat.INSTANCE().setRate(0f);
 		}
 		batch.end();
 		batch.begin();
 	//	heartMeterSprite.setPosition((float)player.getX()-300, (float)player.getY()-200);
 	//	heartMeterSprite.draw(batch);
 	//	heartMeter2Sprite.setSize(Heartbeat.INSTANCE().getRate()*93f, 22f);
 	//	heartMeter2Sprite.setPosition(heartMeterSprite.getX()+38, heartMeterSprite.getY()+6);
 	//	heartMeter2Sprite.draw(batch);
 		batch.end();
 		batch.begin();
 		////System.out.println(level[0].sprite()!=null);
 		player.getSprite().draw(batch);
 		player.update();
 		collision(player, level);
 		player.sprite().setPosition((float)player.getX(), (float)player.getY());
 		batch.end();
 		if(Heartbeat.INSTANCE().getRate() >.7f)
 		{
 			for(Platform p: level)
 			{
 				p.setDarkness((short)0);
 			}
 		}
 		else if(Heartbeat.INSTANCE().getRate() >.4)
 		{
 			for(Platform p: level)
 			{
 				p.setDarkness((short)1);
 			}
 		}
 		else if(Heartbeat.INSTANCE().getRate() <.4)
 		{
 			for(Platform p: level)
 			{
 				p.setDarkness((short)2);
 			}
 		}
 		
 		for(Platform p: level)
 		{
 			batch.begin();
 			p.sprite().draw(batch);
 			batch.end();		
 		}
 		
 		if(Heartbeat.INSTANCE().getRate() < .8)
 		{
 			camera.position.set((float)(player.getX() + 32), (float)(player.getY() + 32), 0f);
 		}
 		else
 		{
 			int offset = random.nextInt(10) - 5;
 			camera.position.set((float)((player.getX() + 32) + offset), (float)((player.getY() + 32) + offset), 0f);
 		}
 		camera.zoom = 1 - (Heartbeat.INSTANCE().getRate() / 10);
 		camera.update();
 	}
 	
 	@Override
 	public void resize(int width, int height) {
 	}
 	
 	@Override
 	public void pause() {
 	}
 	
 	@Override
 	public void resume() {
 	}
 	
 	
 	static Rectangle xRect = new Rectangle();
 	static Rectangle yRect = new Rectangle();
 	public static void collision(Player p, Platform[] level)
 	{
 	     float x, y, width, height, xSpeed, ySpeed;
 	     x = (float)p.getX();
 	     y = (float)p.getY();
 	     width = (float)p.sprite.getWidth();
 	     height = (float)p.sprite.getHeight();
 	     xSpeed = (float)p.getCurrentXSpeed();
 	     ySpeed = (float)p.getCurrentYSpeed();
 	  
 		 if(xSpeed>0)
 	     {
 			 xRect.set(x+width,y+10,xSpeed-23,height);  
 	     }
 	     else if(xSpeed<0)
 	     {
 	    	 xRect.set(x-xSpeed+23,y+10,-xSpeed,height); 
 	     }
 	     if(ySpeed<0)
 	     {
 	    	 yRect.set(x+20,y+ySpeed+5,width-43,ySpeed); 
 	     }
 	     else if(ySpeed>0)
 	     {
 	    	 yRect.set(x+20,y+height,width-43,-ySpeed); 
 	     }
 	     for(int i = 0; i < level.length;i++)
 		 {
 	    	 if(xRect.overlaps(level[i].sprite.getBoundingRectangle())) 
 	    	 {
 	    		 p.setX(p.getX()-p.getCurrentXSpeed());
 	    		 p.setCurrentXSpeed(0);
 	    	 }
 	    	 if(yRect.overlaps(level[i].sprite.getBoundingRectangle())) 
 	    	 {
 	    		 p.setY(p.getY()-p.getCurrentYSpeed());
 	    		 p.setCurrentYSpeed(0);
 	    		 p.setJump(true);
 	    	 }
 		}
 	}
 }
 
