 package gamestate;
 
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.event.KeyEvent;
 import java.awt.image.BufferedImage;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Random;
 
 import javax.imageio.ImageIO;
 
 import main.GamePanel;
 import tilemap.Background;
 import tilemap.TileMap;
 import entities.Enemy;
 import entities.FloatingText;
 import entities.HUD;
 import entities.Player;
 import entities.enemies.Slugger;
 
 @SuppressWarnings("unused")
 public class Level1State extends Level {
 
 	private BufferedImage overlay;
 	private boolean lightmap;
 
 
 	public Level1State(GameStateManager manager) {
 		super(manager);
 		init();
 	}	
 
 	public void init() {
 		tilemap = new TileMap(30);
 		tilemap.loadTiles("/Tilesets/grasstileset3.png");
 		tilemap.loadMap("/Maps/level1-4.map");
 		tilemap.setPosition(0, 0);
 
 		try {
 			overlay = ImageIO.read(getClass().getResourceAsStream("/Backgrounds/lightmap.png"));
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		bg = new Background("/Backgrounds/menubg.gif", 0.1);
		bg.setScroll(-0.2, 0);
 		player = new Player(tilemap);
 		player.setPosition(tilemap.getSpawnPointX() + (player.getWidth() / 2), tilemap.getSpawnPointY());
 		enemies = new ArrayList<Enemy>();
 		spawnSomeSlugs();
 		lightmap = false;
 		hud = new HUD(player);
 	}
 
 	public void spawnSomeSlugs() {
 		Slugger[] s = new Slugger[15];
 		for (int i = 0; i < s.length; i++) {
 			s[i] = new Slugger(tilemap);
 
 			//			s[i].setPosition(50 + (i * 20) , 100);
 			s[i].setPosition(tilemap.getSpawnPointX() + (new Random().nextInt(2000) + 150) , 100);
 			enemies.add(s[i]);
 		}
 	}
 
 
 
 	public void update() {
 		bg.update();
 		player.update();
 		tilemap.setPosition(GamePanel.WIDTH / 2  - player.getX(), GamePanel.HEIGHT / 2 - player.getY() - 22);
 
 
 
 		//		bg.setPosition(tilemap.getX(), tilemap.getY());
 
 		player.checkAttack(enemies);
 
 
 		for (int i = 0; i < enemies.size(); i++) {
 			enemies.get(i).update();
 		}
 	}
 
 	public void draw(Graphics2D g) {
 		bg.draw(g);
 
 		// draw tilemap
 
 		tilemap.draw(g);
 		player.draw(g);
 		g.setColor(Color.BLACK);
 		g.drawString("XY: " + player.getX() + " " + player.getY(), 6, GamePanel.HEIGHT - 14);
 		g.setColor(Color.WHITE);
 		g.drawString("XY: " + player.getX() + " " + player.getY(), 5, GamePanel.HEIGHT - 15);
 
 		g.setColor(Color.BLACK);
 		g.drawString("LSW XY: " + player.getLSWX() + " " + player.getLSWY(), 6, GamePanel.HEIGHT - 4);
 		g.setColor(Color.WHITE);
 		g.drawString("LSW XY: " + player.getLSWX() + " " + player.getLSWY(), 5, GamePanel.HEIGHT - 5);
 
 		g.setColor(Color.BLACK);
 		g.drawString("Enemy (Linus) count: " + enemies.size(), 185 , 12);
 		g.setColor(Color.WHITE);
 		g.drawString("Enemy (Linus) count: " + enemies.size(), 184, 11);
 
 
 		for (int i = 0; i < enemies.size(); i++) {
 			enemies.get(i).draw(g);
 			//			System.out.println(enemies.get(i).getDiedFromFalling());
 			if (enemies.get(i).isDead() || enemies.get(i).getDiedFromFalling()) {
 				if (!enemies.get(i).getDiedFromFalling()) {
 					int xp = new Random().nextInt(3) + 1;
 					player.setExperience(xp);
 					player.addText("+ " + xp +  "xp", player.getX(), player.getY() - 10, 1000,  new Color(255, 211, 109));
 				} else {
 					player.addText("Mähähä! Han ramlade ner!", player.getX() - 20, player.getY() - 20, 700,  new Color(255, 255, 255), 0.2);
 				}
 				enemies.remove(i);
 				i--;
 			}
 		}
 
 		hud.draw(g);
 		if (lightmap) g.drawImage(overlay, 0, 0, null);
 
 	}
 
 	public void respawn() {
 		player.setPosition(tilemap.getSpawnPointX(), tilemap.getSpawnPointY());
 	}
 
 	public void keyPressed(int k) {
 		if (k == KeyEvent.VK_LEFT) player.setLeft(true);
 		if (k == KeyEvent.VK_RIGHT) player.setRight(true);
 
 
 		if (player.isPeeing() && !player.isJumping() && !player.isFalling() && !player.isMoving()) {
 			if (k == KeyEvent.VK_UP) player.changePeeArcY(-0.1);
 			if (k == KeyEvent.VK_DOWN) player.changePeeArcY(0.1);
 		} else {
 			if (player.isJumping() || player.isFalling()) {
 				if (k == KeyEvent.VK_UP) player.setGliding(true);
 			} else {
 				if (k == KeyEvent.VK_UP) player.setJumping(true);
 			}
 		}
 
 		//		if (k == KeyEvent.VK_DOWN) player.setDown(true);
 		if (k == KeyEvent.VK_CONTROL) player.setJumping(true);
 		if (k == KeyEvent.VK_SPACE) player.setFiring(true);
 
 		if (k == KeyEvent.VK_F1) spawnSomeSlugs();
 		if (k == KeyEvent.VK_F2) respawn();
 		if (k == KeyEvent.VK_F3) {
 			lightmap = !lightmap;
 		}
 
 		if (k == KeyEvent.VK_ESCAPE) {
 			System.exit(0);
 		}
 
 		//		if (k == KeyEvent.VK_F4) {
 		//			addText("Test", (double) player.getX(), (double) player.getY());
 		//		}
 
 	}
 
 	public void keyReleased(int k) {
 		if (k == KeyEvent.VK_LEFT) player.setLeft(false);
 		if (k == KeyEvent.VK_RIGHT) player.setRight(false);
 
 		if (player.isJumping() || player.isFalling()) {
 			if (k == KeyEvent.VK_UP) player.setGliding(false);
 		} else {
 			if (k == KeyEvent.VK_UP) player.setJumping(false);
 		}
 
 
 		if (k == KeyEvent.VK_CONTROL) player.setJumping(false);
 		if (k == KeyEvent.VK_SPACE) player.setFiring(false);
 	}
 
 
 }
