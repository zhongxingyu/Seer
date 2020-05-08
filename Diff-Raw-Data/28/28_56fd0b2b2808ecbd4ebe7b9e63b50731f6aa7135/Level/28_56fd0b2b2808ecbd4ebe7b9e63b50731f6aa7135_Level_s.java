 package com.bh.game.level;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
import com.bh.game.Game;
 import com.bh.game.graphics.Image;
 import com.bh.game.graphics.Screen;
 import com.bh.game.level.entity.Bullet;
 import com.bh.game.level.entity.Enemy;
 import com.bh.game.level.entity.Entity;
 import com.bh.game.level.entity.Player;
 
 public class Level {
 	public Image background;
 	
 	public List<Entity> entities = new ArrayList<Entity>();
 	public List<Bullet> bullets = new ArrayList<Bullet>();
 	public List<Enemy> enemies = new ArrayList<Enemy>();
 	
 	public int w;
 	public int h;
 	
 	public Player player;
 	
 	public Level(int w, int h) {
 		this.w = w;
 		this.h = h;
 		
 		background = new Image("/levelBack.png");
 	}
 	
 	public void addEntity(Entity e) {
 		entities.add(e);
 		if (e instanceof Bullet) {
 			bullets.add((Bullet)e);
 		}
 		if (e instanceof Enemy) {
 			enemies.add((Enemy)e);
 		}
 		
 	}
 	
 	public void removeEntity(Entity e) {
 		entities.remove(e);
 		if (e instanceof Bullet) {
 			bullets.remove((Bullet)e);
 		}
 		if (e instanceof Enemy) {
 			enemies.remove((Enemy)e);
 		}
 	}
 	
 	public void setPlayer(Player p) {
 		this.player = p;
 	}
 	
 	public void tick() {
 		for (int i = 0; i < entities.size(); i++) {
 			entities.get(i).tick();
 			if (entities.get(i) instanceof Bullet) {
 				Bullet b = (Bullet)entities.get(i);
 				if (!b.alive) {
 					removeEntity(b);
 					i--;
 					continue;
 				}
 			}
 			if (entities.get(i) instanceof Enemy) {
 				Enemy e = (Enemy)entities.get(i);
 				if (!e.alive) {
 					removeEntity(e);
 					i--;
 					continue;
 				}
 			}
 			if (entities.get(i) instanceof Enemy) {
 				Enemy e = (Enemy)entities.get(i);
				e.rotation = Image.getAngle(e.x, e.y, player.x, player.y) + 90;
 
				e.x += 5 * Math.cos(e.rotation);
				e.y += 5 * Math.sin(e.rotation);
 			}
 		}
 		player.tick();
 	}
 	
 	public void addEnemy() {
 		Random r = new Random();
 		int x = r.nextInt(284);
 		int y = -r.nextInt(32) - 16;
 		Enemy e = new Enemy(x, y);
 		addEntity(e);
 	}
 	
 	public void preRender(Screen screen) {
 		for (Entity e : entities) {
 			e.preRender(screen);
 		}
 
 		player.preRender(screen);
 	}
 	
 	public void render(Screen screen) {
 		for (int y = 0; y < h; y++) {
 			for (int x = 0; x < w; x++) {
 				screen.render(background.pixels, x << 4, y << 4, 16, 16, (y % 2) + (x % 2), true);
 			}
 		}
 		
 		for (Entity e : entities) {
 			e.render(screen);
 		}
 
 		player.render(screen);
 	}
 }
