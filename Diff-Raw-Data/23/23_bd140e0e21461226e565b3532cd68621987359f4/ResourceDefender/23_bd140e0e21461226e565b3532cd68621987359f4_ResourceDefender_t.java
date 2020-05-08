 package net.techythomas.game.ResourceDefender;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.Scanner;
 
 import net.techythomas.game.ResourceDefender.control.Keyboard;
 import net.techythomas.game.ResourceDefender.entities.Player;
 import net.techythomas.game.ResourceDefender.projectiles.Bullet;
 
 import org.newdawn.slick.BasicGame;
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.AppGameContainer;
 import org.newdawn.slick.geom.Rectangle;
 
 public class ResourceDefender extends BasicGame {
 	
 	private static int width = 1280;
 	private static int height = 720;
 	private static boolean fullscreen = false;
 	
 	private Rectangle rect;
 	private Bullet bullet;
 	
 	private Player player;
 	private World world;
 	private LevelEditor editor;
 
     public ResourceDefender() {
         super("Resource Defender");
     }
     
     public void checkCollisions() {
 		Rectangle p = player.getBounds();
 		Rectangle w = world.getWallBounds();
 		
 		if (p.intersects(w)) {
 			if (player.FACING == 0) {
 				player.allowMoveUp = false;
 				player.allowMoveDown = true;
 				player.allowMoveLeft = true;
 				player.allowMoveRight = true;
				
 			}
 			else if (player.FACING == 1) {
 				player.allowMoveUp = true;
 				player.allowMoveDown = false;
 				player.allowMoveLeft = true;
 				player.allowMoveRight = true;
 			}
 			else if (player.FACING == 2) {
 				player.allowMoveUp = true;
 				player.allowMoveDown = true;
 				player.allowMoveLeft = false;
 				player.allowMoveRight = true;
				player.cannotMoveDiagonalLeft = true;
 			}
 			else if (player.FACING == 3) {
 				player.allowMoveUp = true;
 				player.allowMoveDown = true;
 				player.allowMoveLeft = true;
 				player.allowMoveRight = false;
					player.cannotMoveDiagonalRight = true;
 			}
			
 		}
 		
 		else {
 			player.allowMoveUp = true;
 			player.allowMoveDown = true;
 			player.allowMoveLeft = true;
 			player.allowMoveRight = true;
			player.cannotMoveDiagonalRight = false;
			player.cannotMoveDiagonalLeft = false;
 		}
 	}
     
     public void addWalls (World world, InputStream stream) {
     	BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
     	String line;
     	
     	try {
     		while ((line = reader.readLine()) != null) {
     			if (line.startsWith("#") || line.trim().length() < 1) continue;
     			String[] coords = line.split(" ", 4);
     			Rectangle rect = new Rectangle(Integer.parseInt(coords[0]), Integer.parseInt(coords[1]), Integer.parseInt(coords[2]), Integer.parseInt(coords[3]));
     			world.addWalls(rect);
     		}
     	}
     	catch (IOException e) {
     		System.out.println("Couldn't load walls!");
     		e.printStackTrace();
     	}
     }
     
     @Override
     public void init(GameContainer container) throws SlickException {
     	player = new Player(new Image("res/player_right.png"), 70, 92);
     	world = new World();
     	rect = new Rectangle(player.getWidth(), player.getHeight(), player.getX(), player.getY());
     	editor = new LevelEditor();
     	bullet = new Bullet(player.getX(), player.getWidth());
     	container.getInput().addMouseListener(editor);
     	
     	addWalls(world, getClass().getResourceAsStream("walls.txt"));
     	
     	//BufferedReader reader = new BufferedReader("res/collisions.txt");
     	//reader.readLine();
     }
 
     @Override
     public void update(GameContainer container, int delta) throws SlickException {
     	ArrayList bullets = player.getBullets();
     	for (int w = 0; w < bullets.size(); w++) {
     		Bullet bullet = (Bullet) bullets.get(w);
     		if (bullet.getVisible() == true) {
     			bullet.update();
     		}
     		else
     			bullets.remove(w);
     	}
     	player.update(container);
     	checkCollisions();
     	
     	//world.update(container);
     	
     	
     }
 
     
     @Override
     public void render(GameContainer container, Graphics g) throws SlickException {
         world.render(g);
         player.render();
         
         ArrayList bullets = player.getBullets();
         for (int w = 0; w < bullets.size(); w++) {
         	Bullet bullet = (Bullet) bullets.get(w);
         	bullet.draw(50, 30);
         }
     }
 
     public static void main(String[] args) throws SlickException {
         
             AppGameContainer app = new AppGameContainer(new ResourceDefender());
             app.setDisplayMode(width, height, fullscreen);
             app.setTargetFrameRate(60);
             app.setMaximumLogicUpdateInterval(3);
             app.setMinimumLogicUpdateInterval(3);
             app.start();
         
     }
 }
