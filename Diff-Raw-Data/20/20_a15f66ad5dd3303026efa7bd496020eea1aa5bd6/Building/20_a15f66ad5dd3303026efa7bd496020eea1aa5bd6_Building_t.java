 package com.subfty.krkjam2013.game.actor.buildings;
 
 import com.badlogic.gdx.graphics.g2d.BitmapFont.HAlignment;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.scenes.scene2d.Group;
 import com.badlogic.gdx.utils.Array;
 import com.subfty.krkjam2013.Krakjam;
 import com.subfty.krkjam2013.game.Background;
 import com.subfty.krkjam2013.game.actor.Bullet;
 import com.subfty.krkjam2013.game.actor.Player;
 import com.subfty.krkjam2013.game.actor.aliens.Alien;
 import com.subfty.krkjam2013.game.actor.buildings.BuildingsOverlord.B_TYPE;
 import com.subfty.krkjam2013.util.Art;
 
 public class Building extends Group{
 	
 	public int tileX,
 			   tileY,
 			   tileWidth,
 			   tileHeight;
 	
 	
 	private Sprite image;
 	private Sprite imageDamaged;
 	
 	private String desc;
 	private Background bg;
 	private float health;
 	
 	public int possesedCristals;
 	
 	public B_TYPE type;
 	
 	public final float TURET_COOLDOWN = .5f;
 	private float turretNextShoot = TURET_COOLDOWN;
 	
 	public Building(Background bg){
 		health = 1.0f;
 		this.bg = bg;
 		
 		image = new Sprite();//Krakjam.art.atlases[Art.A_AGENTS].createSprite("base1");
 		imageDamaged = new Sprite();
 		this.visible = false;
 	}
 	
 	public void init(int tileX, int tileY, B_TYPE type){
 		this.type = type;
 		
 		this.tileX = tileX;
 		this.tileY = tileY;
 		this.tileWidth = type.width;
 		this.tileHeight = type.height;
 		
 		possesedCristals  = 0;
 		
 		this.visible = true;
 		bg.registerBuilding(this);
 		this.desc = type.desc;
 		if(type == B_TYPE.TURRET)
 			desc = type.desc+"\nBULLETS: "+possesedCristals;
 		if(type == B_TYPE.GENERATOR)
 			desc = type.desc+"\n"+possesedCristals+"/"+60;
 		
 		this.width = tileWidth * Background.TILE_SIZE;
 		this.height = tileHeight * Background.TILE_SIZE;
 		
 		image.setRegion(Krakjam.art.atlases[Art.A_AGENTS].createSprite(type.img, 1));
 		image.setSize(this.width, this.height);
 		
 		if(type.destroyable){
			if(type == B_TYPE.MEDCENTRE)
				imageDamaged.setRegion(Krakjam.art.atlases[Art.A_AGENTS].createSprite(type.img, 3));
			else
				imageDamaged.setRegion(Krakjam.art.atlases[Art.A_AGENTS].createSprite(type.img, 2));
 			imageDamaged.setSize(image.getWidth(), 
 								 image.getHeight());
 		}
 		
 		if(type == B_TYPE.BASE) {
 			Krakjam.gameScreen.base = this;
 		}
 		
 		this.health = type.MAX_HEALTH;
 		
 		act(0);
 	}
 	public void kill(){
 		this.visible = false;
 		bg.unregisterBuilding(this);
 	}
 	
 	@Override
 	public void act(float delta) {
 		this.x = -bg.x + tileX * Background.TILE_SIZE;
 		this.y = -bg.y + tileY * Background.TILE_SIZE;
 		
 		image.setPosition(x,y);
 		
 		if(type == B_TYPE.TURRET) {
 			turretNextShoot -= delta;
 			if(turretNextShoot <= 0 && possesedCristals > 0) {
 				
 				turretNextShoot = TURET_COOLDOWN;
 				
 				Array<Alien> aliens = Krakjam.gameScreen.aOverlord.aliens;
 				
 				float minDist = 10000;
 				
 				float	cx = x + width/2.0f, 
 						cy = y + height/2.0f;
 				
 				float ax = 0, ay = 0;
 				
 				for(int i=0; i<aliens.size; i++) {
 					Alien a = aliens.get(i);
 					
 					if(a.visible == false)
 						continue;
 					
 					float len = Vector2.tmp.set(a.x, a.y).sub(cx, cy).len();
 					
 					if(minDist > len) {
 						minDist = len;
 						Vector2.tmp.nor();
 						ax = Vector2.tmp.x;
 						ay = Vector2.tmp.y;
 					}
 					
 				}
 				if(minDist < 300.0f) {
 					Player player = Krakjam.gameScreen.player;
 					Bullet bullet = player.obtainBullet();
 					final float bulletSpeed = 400;
 					bullet.antyPlayer = false;
 					bullet.init(ax*bulletSpeed, ay*bulletSpeed, 
 							cx, cy, Krakjam.rand.nextFloat()*2*(float)Math.PI, 0, this);
 					
 					Krakjam.art.turret.play();
 					
 					possesedCristals--;
 					desc = type.desc+"\nBULLETS: "+possesedCristals;
 				} else {
 					turretNextShoot = 0.0f;
 				}
 			}
 		}
 	}
 	
 	@Override
 	public void draw(SpriteBatch batch, float parentAlpha) {	
 		if(type == B_TYPE.GENERATOR)
 			if(possesedCristals == 60 && isHealth())
 				image.setRegion(Krakjam.art.atlases[Art.A_AGENTS].createSprite(type.img, 2));
 		
 		if(type == B_TYPE.MEDCENTRE){
 			
			if(Krakjam.gameScreen.generatorsRunning() && this.isHealth())
 				image.setRegion(Krakjam.art.atlases[Art.A_AGENTS].createSprite(type.img, 2));
 			else
 				image.setRegion(Krakjam.art.atlases[Art.A_AGENTS].createSprite(type.img, 1));
 		}
 		
 		if(type.destroyable) {
 			imageDamaged.setPosition(x, y);
 			imageDamaged.draw(batch, 1);
 		}
 		image.draw(batch, getHealth()/type.MAX_HEALTH);
 		
 		Krakjam.art.fonts[Art.F_DIGITAL].setColor(1, 1, 1, 0.5f);
 		Krakjam.art.fonts[Art.F_DIGITAL].setScale(0.25f);
 		if(type == B_TYPE.TURRET)
 			Krakjam.art.fonts[Art.F_DIGITAL].drawWrapped(batch, desc, this.x-this.width * 0.15f, this.y-5, this.width*1.3f, HAlignment.CENTER);
 		else
 			Krakjam.art.fonts[Art.F_DIGITAL].drawWrapped(batch, desc, this.x, this.y-5, this.width, HAlignment.CENTER);
 		
 	}
 
 	public void feedCristal(){
 		possesedCristals++;
 		if(type == B_TYPE.TURRET)
 			desc = type.desc+"\nBULLETS: "+possesedCristals;
 		if(type == B_TYPE.GENERATOR){
 			possesedCristals = Math.min(60, possesedCristals);
 			desc = type.desc+"\n"+possesedCristals+"/"+60;
 			
 		}
 	}
 	
     //HEALTH
 	public void repair(float ammount){
 		this.health += ammount;
 		health = Math.min(type.MAX_HEALTH, health);
 	}
 	public float getHealth(){
 		return Math.max(0, Math.min(type.MAX_HEALTH, health));
 	}
 	public boolean isHealth(){
 		return getHealth() == type.MAX_HEALTH;
 	}
 	
 	public void damage(float ammount) {
 		if(!type.destroyable) return;
 		this.health = Math.max(0, this.health-ammount);
 	}
 }
