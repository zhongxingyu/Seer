 package com.rustbyte;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import com.rustbyte.Art;
 import com.rustbyte.Bitmap;
 import com.rustbyte.Entity;
 import com.rustbyte.InputHandler;
 import com.rustbyte.Level;
 import com.rustbyte.Player;
 import com.rustbyte.Zombie;
 
 public class Game {
 	public int WIDTH;
 	public int HEIGHT;
 	public Bitmap screen;
 	private double gravity = 1;
 	
 	public InputHandler input;
 	public Level level;
 	public Player player;
 	private List<Entity> entities = new ArrayList<Entity>();
 	public int tickcount = 0;	
 	public int FPS = 0;
 	
 	public Game(int wid, int hgt) {
 		this.WIDTH = wid;
 		this.HEIGHT = hgt;
 		this.screen = new Bitmap(WIDTH, HEIGHT);
 		
 		input = new InputHandler();
 		level = new Level(Art.level1, 20,20, this);
 		player = new Player(30, 30, 20, 20, null, this);
 		
 		level.viewX = 0;
 		level.viewY = 0;
 		level.viewWidth = WIDTH;
 		level.viewHeight = HEIGHT;
 		//addEntity(player);
 		Random rand = new Random();
 		int xx = 40;
		int minspacing = WIDTH / 50;
		for(int i=0; i < 50; i++) {
 			xx += minspacing + rand.nextInt(40);
 			if(xx > (level.width * level.tileWidth) - 40)
 				xx = 40;
 			if( xx >= ((level.width * level.tileWidth) - 40))
 				xx = (level.width * level.tileWidth) - 40;			
 			addEntity(new Zombie(xx,50, 20, 20, null ,this));
 		}	
 	}
 	
 	public void addEntity(Entity ent) {
 		ent.alive = true;
 		entities.add(ent);
 	}
 	
 	public void tick() {
 		tickcount++;
 		
 		gravity = 0.25;		
 		for(int i=0; i < entities.size(); i++) { 
 			Entity ent = entities.get(i);		
 			if(ent.alive) {
 				ent.applyGravity(gravity);
 				ent.tick();
 				ent.postTick();
 			} else {
 				entities.remove(ent);
 			}
 		}
 		player.applyGravity(gravity);
 		player.tick();
 		player.postTick();
 		
 		System.out.println("Active entities: " + entities.size());
 		
 		level.tick();
 		level.setViewPos((int)player.xx, (int)player.yy);		
 	}
 	
 	public void render() {
 		for(int i=0; i < WIDTH * HEIGHT;i++)
 			screen.pixels[i] = 0x498FFF;
 		
 		level.draw(screen);
 				
 		screen.drawText(Art.font, "fps: " + FPS, 0,0,0xFFFF00, true);
 		
 		screen.drawText(Art.font,"numbers: 012345 999 !?()",0,50, 0xFFFF00, true);
 				
 		for(int i=0; i < entities.size();i++) {
 			Entity ent = entities.get(i);
 			if( ent.alive ) {
 				if( (ent.xx + ent.wid/2) < level.viewX || ( (ent.xx - ent.wid/2) > (level.viewX + level.viewWidth))) 
 					continue;
 				if( (ent.yy + ent.hgt/2) < level.viewY || ( (ent.yy - ent.hgt/2) > (level.viewY + level.viewHeight))) 
 					continue;
 				
 				ent.render();
 			}
 		}
 		
 		player.render();
 	}
 }
