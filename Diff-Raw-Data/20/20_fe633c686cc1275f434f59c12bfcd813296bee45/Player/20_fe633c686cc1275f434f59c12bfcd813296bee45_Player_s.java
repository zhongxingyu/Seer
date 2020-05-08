 package com.subfty.krkjam2013.game.actor;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Input.Keys;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.math.Matrix4;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.scenes.scene2d.Actor;
 import com.badlogic.gdx.utils.Array;
 import com.badlogic.gdx.utils.Logger;
 import com.subfty.krkjam2013.Krakjam;
 import com.subfty.krkjam2013.game.Background;
 import com.subfty.krkjam2013.game.actor.aliens.Alien;
 import com.subfty.krkjam2013.game.actor.player.Stats;
 import com.subfty.krkjam2013.util.Art;
 
 public class Player extends Collider {
 	//SETTINGS
 	private final float MARGIN_X = 250,
 						MARGIN_Y = 200,
 						MAX_LIFE = 60*3;
 	private final float SPAWN_X, SPAWN_Y;
 	
 	private float step;
 	private float cursorDistance;
 
 	private float angle,
 				  angle2,
 				  move;
 	public float life;
 	
 	public Stats stats;
 	public int exp;
 	
     //VISUALS
 	private TextureRegion playerSprites[];
 	private Sprite image;
 	private Cursor cursor;
 	
 	private Sprite smuga;
 	
 	private Background bg;
 	
 	public Player(Background bg, float spawn_x, float spawn_y){
 		this.bg = bg;
 		
 		
 		this.SPAWN_X = spawn_x;
 		this.SPAWN_Y = spawn_y;
 		
 		step=500f;
 		cursorDistance=100;
 	
 		stats = new Stats();
 		cursor=new Cursor();		
 		playerSprites = new TextureRegion[]{Krakjam.art.atlases[Art.A_AGENTS].findRegion("clone", 1),
 											Krakjam.art.atlases[Art.A_AGENTS].findRegion("clone", 2),
 											Krakjam.art.atlases[Art.A_AGENTS].findRegion("clone", 3),
 											Krakjam.art.atlases[Art.A_AGENTS].findRegion("clone", 4)};
 		image = new Sprite();
 		image.setRegion(playerSprites[0]);
 		
 		image.setSize(Background.TILE_SIZE, 200/100*Background.TILE_SIZE);
 		smuga = Krakjam.art.atlases[Art.A_ENTITIES].createSprite("smugi");
 		smuga.setOrigin(smuga.getWidth()/2.0f, 0);
 		
 		this.addActor(cursor);
 	}
 	
     //STAGES OF LIFE
 	public void init(){
 		stats.clearAll();
 		
 		this.x = SPAWN_X;
 		this.y = SPAWN_Y;
 		life = MAX_LIFE;
 		exp = 0;
 	}
 	public void kill(){
 		init();
 	}
 	
 	@Override
 	public void act(float delta) {
 		if(!this.visible || Krakjam.gameScreen.pause)
 			return;
 		final float gunY = 40.0f;
 		final float gunX = -25.0f;
 		move=step*delta;
 		angle=0f;
 
 		updateInput();
 		
 		dx+=move*Math.cos(angle);
 		dy+=move*Math.sin(angle);
 		
 		if(dx < -0.1f)
 			image.setRegion(playerSprites[2]);
 		else if(dx > 0.1f)
 			image.setRegion(playerSprites[3]);
 		else if(dy > 0.1f)
 			image.setRegion(playerSprites[1]);
 		else
 			image.setRegion(playerSprites[0]);
 		
 		int wspx = Gdx.input.getX();
 		int wspy = Gdx.input.getY();
 		Vector2 v=new Vector2();
 		Krakjam.stage.toStageCoordinates(wspx, wspy, v);
 		
		angle2=(float)Math.atan2(v.y-y-gunY, v.x-x);
 				
		cursor.setX((float)(cursorDistance*Math.cos(angle2))+x+gunX);
		cursor.setY((float)(cursorDistance*Math.sin(angle2))+y+gunY);
 		
 		resolveCollisions();
 		scrollBackground(delta);
 		
 		if(Gdx.input.justTouched()) {
 			smuga.setPosition(x-smuga.getWidth()/2.0f+gunX, y+gunY);
 			FadeOutSprite fadeOut = new FadeOutSprite(0.2f, smuga);
 			fadeOut.angle = (float)(angle2*180/Math.PI) - 90;
 			stage.addActor(fadeOut);
 			
 			Bullet bullet = new Bullet();
 			Vector2 tmp = Vector2.tmp;
 			tmp.set(v.y-y-gunY, v.x-x);
 			tmp.nor().mul(10);
 			bullet.init(tmp.x, tmp.y);
 			bullet.init(x+gunX-bg.x, y+gunY-bg.y);
 			
 			stage.addActor(bullet);
 			
 			Array<Alien> aliens = Krakjam.gameScreen.aOverlord.aliens;
 			
 			final float SHOOT_SIZE = 200.0f;
 			
 			for(int i=0; i<aliens.size; i++) {
 				Alien a = aliens.get(i);
 				if(a.visible) {
 					int hits = 0;
 					final int MAX_J = 20;
 					for(int k=-1; k<2; k++) {
 					for(int j=0; j<=MAX_J; j++) {
 							float dist = SHOOT_SIZE*j/((float)MAX_J);
 							final float katdiff = (float)Math.PI/6.0f;
 							float px = (float)(Math.cos(angle2+k*katdiff)*dist+x+gunX);
 							float py = (float)(Math.sin(angle2+k*katdiff)*dist+y+gunY);
 						
 							if(px >= a.x- width/2.0f && py >= a.y && px <= a.x+a.width- width/2.0f && py <= a.y+a.height) {
 								a.shoot();
 								hits++;
 								break;
 							}
 						}
 					}
 					
 					if(hits != 0) {
 						Background bg = Krakjam.gameScreen.background;
 						FlyingPoints points = new FlyingPoints(a.x-a.width/2.0f+bg.x, a.y+a.height+bg.y, hits);
 						stage.addActor(points);
 					}
 				}
 			}
 		}
 		
 		//UPDATING LIFE
 		life -= delta;
 		if(life < 0){
 			kill();
 		}
 	}
 	private void updateInput(){
 		if(Gdx.input.isKeyPressed(Keys.W) && Gdx.input.isKeyPressed(Keys.D))
 			angle=(float)Math.PI/4.0f;
 		else if(Gdx.input.isKeyPressed(Keys.D) && Gdx.input.isKeyPressed(Keys.S))
 			angle=7*(float)Math.PI/4.0f;
 		else if(Gdx.input.isKeyPressed(Keys.A) && Gdx.input.isKeyPressed(Keys.S))
 			angle=5*(float)Math.PI/4.0f;
 		else if(Gdx.input.isKeyPressed(Keys.A) && Gdx.input.isKeyPressed(Keys.W))
 			angle=3*(float)Math.PI/4.0f;
 		else if(Gdx.input.isKeyPressed(Keys.W))
 			angle=(float)Math.PI/2.0f;
 		else if(Gdx.input.isKeyPressed(Keys.S))
 			angle=3*(float)Math.PI/2.0f;
 		else if(Gdx.input.isKeyPressed(Keys.A))
 			angle=(float)Math.PI;
 		else if(Gdx.input.isKeyPressed(Keys.D))
 			angle=0f;
 		else
 			move=0f;
 	}
 	private void scrollBackground(float delta){
 		float speed = 3f;
 
 		float screenx = ((this.x+parent.x) / Krakjam.SCALE),
 			  screeny = ((this.y+parent.y) / Krakjam.SCALE);
 
 		if(screenx < MARGIN_X/Krakjam.SCALE)
 			this.parent.x += (MARGIN_X/Krakjam.SCALE-screenx)*speed*delta;
 		if(screenx > (Krakjam.STAGE_W-MARGIN_X)/Krakjam.SCALE)
 			this.parent.x -=(screenx-((Krakjam.STAGE_W-MARGIN_X)/Krakjam.SCALE))*speed*delta;
 		if(screeny < MARGIN_Y/Krakjam.SCALE)
 			this.parent.y +=  (MARGIN_Y/Krakjam.SCALE-screeny)*speed*delta;
 		if(screeny > (Krakjam.STAGE_H-MARGIN_Y)/Krakjam.SCALE)
 			this.parent.y -=(screeny-((Krakjam.STAGE_H-MARGIN_Y)/Krakjam.SCALE))*speed*delta;
 	}
 	
 	@Override
 	public void draw(SpriteBatch batch, float parentAlpha) {
 		///drawDebug(batch);
 		
 		image.setPosition(x - image.getWidth()/2.0f, y-15);
 		image.draw(batch);
 		cursor.draw(batch, parentAlpha);
 	}
 	
 	@Override
 	public Actor hit(float x, float y) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 }
