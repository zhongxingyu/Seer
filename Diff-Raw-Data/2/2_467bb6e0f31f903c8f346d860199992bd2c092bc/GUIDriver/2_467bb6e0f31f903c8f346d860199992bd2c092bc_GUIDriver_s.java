 package driver;
 
 import java.awt.Canvas;
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.GraphicsConfiguration;
 import java.awt.GraphicsDevice;
 import java.awt.GraphicsEnvironment;
 import java.awt.Point;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.awt.image.BufferStrategy;
 import java.awt.image.BufferedImage;
 import java.util.ArrayList;
 import java.util.ConcurrentModificationException;
 import java.util.HashMap;
 import java.util.Random;
 
 import javax.swing.JFrame;
 
 import world.Character;
 import world.Enemy;
 import world.Forge;
 import world.Grid;
 import world.GridSpace;
 import world.LivingThing;
 import world.Magic;
 import world.RangedWeapon;
 import world.Terrain;
 import world.Thing;
 import world.World;
 
 public class GUIDriver {
 
 	private static Grid g;
 	private static long gravityRate;
 	private static int lastKey;
 	private static long hangTime;
 	private static long value;
 	private static int stage = 1;
 	private static boolean keepGoing;
 
 	public static void main(String[] args) {
 
 		// Create game window...
 
 		JFrame app = new JFrame();
 		app.setIgnoreRepaint(true);
 		app.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
 		// Create canvas for painting...
 
 		Canvas canvas = new Canvas();
 		canvas.setIgnoreRepaint(true);
 		canvas.setSize(1200, 480);
 
 		// Add canvas to game window...
 
 		app.add(canvas);
 		app.pack();
 		app.setVisible(true);
 
 		// Create BackBuffer...
 
 		canvas.createBufferStrategy(2);
 		BufferStrategy buffer = canvas.getBufferStrategy();
 
 		// Get graphics configuration...
 
 		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
 		GraphicsDevice gd = ge.getDefaultScreenDevice();
 		GraphicsConfiguration gc = gd.getDefaultConfiguration();
 
 		// Create off-screen drawing surface
 
 		BufferedImage bi = gc.createCompatibleImage(1200, 480);
 
 		// Objects needed for rendering...
 
 		Graphics graphics = null;
 		Graphics2D g2d = null;
 		Color background = Color.BLACK;
 
 		// Variables for counting frames per seconds
 
 		int fps = 0;
 		int frames = 0;
 		long totalTime = 0;
 		long gravityTime = 0;
 		long enemyDamageTime = 0;
 		hangTime = 500;
 		gravityRate = 300;
 		value = gravityRate + hangTime;
 		long curTime = System.currentTimeMillis();
 		long lastTime = curTime;
 
 		keepGoing = true;
 
 		g = new Grid(0);
 
 		g.makeDefaultGrid();
 		Character blue = g.getGrid().get(g.getCharacterLocation()).returnCharacter();
 		System.out.println(blue.getRangedStore());
 		System.out.println(blue.getCloseStore());
 		stage = 1;
 
 		app.addKeyListener(new KeyAdapter() {
 			@Override
 			public void keyPressed(KeyEvent e) {
 				SaveGameWriter w = new SaveGameWriter();
 				Character c = g.getGrid().get(g.getCharacterLocation()).returnCharacter();
 				Forge f = new Forge();
 				int keyCode = e.getKeyCode();
 				if (keyCode == KeyEvent.VK_UP) {
 					g.retractWeapon(KeyEvent.VK_A);
 					g.retractWeapon(KeyEvent.VK_D);
 					if (g.getGrid()
 							.get(new Point((int) g.getCharacterLocation().getX(),
 									(int) g.getCharacterLocation().getY() + 1)).hasSolid()) {
 						g.moveCharacter(0, -1, lastKey);
 						g.moveCharacter(0, -1, lastKey);
 						g.moveCharacter(0, -1, lastKey);
 						g.moveCharacter(0, -1, lastKey);
 						value = gravityRate + hangTime;
 
 					}
 					
 				} else if(keyCode == KeyEvent.VK_ESCAPE){
 					w.writeSaveGame(c);
 				} else if (keyCode == KeyEvent.VK_LEFT) {
 					g.moveCharacter(-1, 0, lastKey);
 					lastKey = KeyEvent.VK_LEFT;
 				} else if (keyCode == KeyEvent.VK_DOWN) {
 					g.moveCharacter(0, 1, lastKey);
 				} else if (keyCode == KeyEvent.VK_RIGHT) {
 					g.moveCharacter(1, 0, lastKey);
 					lastKey = KeyEvent.VK_RIGHT;
 				} else if (keyCode == KeyEvent.VK_A) {
 					lastKey = KeyEvent.VK_A;
 					g.useWeapon(lastKey);
 				} else if (keyCode == KeyEvent.VK_D) {
 					lastKey = KeyEvent.VK_D;
 					g.useWeapon(lastKey);
 				} else if (keyCode == KeyEvent.VK_P) {
 					String name = "Yo Mama";
 					Color col = Color.GRAY;
 					Point p = g.findValidEnemyLocation();
 					if (p != null && c.getMoney() >= 5) {
 						g.spawnNewEnemy(p, new Enemy(true, col, name, 10, 10, 10));
 						c.addMoney(-5);
 					} else {
 						System.out.println("Could not spawn a new enemy.");
 					}
 				} else if (keyCode == KeyEvent.VK_OPEN_BRACKET) {
 					String name = "Yo Mama 2";
 					Color col = Color.MAGENTA;
 					Point p = g.findValidEnemyLocation();
 					if (p != null && c.getMoney() >= 10) {
 						g.spawnNewEnemy(p, new Enemy(true, col, name, 20, 20, 20));
 						c.addMoney(-10);
 					} else {
 						System.out.println("Could not spawn a new enemy.");
 					}
 				} else if (keyCode == KeyEvent.VK_CLOSE_BRACKET) {
 					String name = "Yo Mama 3";
 					Color col = Color.pink;
 					Point p = g.findValidEnemyLocation();
 					if (p != null && c.getMoney() >= 20) {
 						g.spawnNewEnemy(p, new Enemy(true, col, name, 40, 40, 40));
 						c.addMoney(-20);
 					} else {
 						System.out.println("Could not spawn a new enemy.");
 					}
 				} else if (keyCode == KeyEvent.VK_BACK_SLASH) {
 					String name = "BOSS";
 					Color col = Color.RED;
 					Point p = g.findValidEnemyLocation();
 					if (p != null && c.getMoney() >= 50) {
 						g.spawnNewEnemy(p, new Enemy(true, col, name, 100, 100, 100));
 						c.addMoney(-50);
 					} else {
 						System.out.println("Could not spawn a new enemy.");
 					}
 				} else if (keyCode == KeyEvent.VK_Q || keyCode == KeyEvent.VK_E) {
 					g.retractWeapon(KeyEvent.VK_A);
 					g.retractWeapon(KeyEvent.VK_D);
 					if (c.getWeapon() instanceof RangedWeapon && !(c.getWeapon() instanceof Magic)) {
 						c.setWeapon((Magic) c.getMagicStore().get(c.getMagicStore().size()-1));
 					} else if (c.getWeapon() instanceof Magic) {
 						c.setWeapon(c.getCloseStore().get(c.getCloseStore().size()-1));
 					} else {
 						c.setWeapon((RangedWeapon) c.getRangedStore().get(c.getRangedStore().size()-1));
 					}
 				} else if (keyCode == KeyEvent.VK_SLASH) {
 					g.killAllEnemies();
 				} else if (keyCode == KeyEvent.VK_SEMICOLON) {
 					g.placeTerrain(lastKey);
 				} else if (keyCode == KeyEvent.VK_PERIOD) {
 					g.placeTerrain(keyCode);
 				}else if (!e.isShiftDown() && keyCode == KeyEvent.VK_1){
 					if (c.getBoughtWeapons().get(0) == true){
 						c.addWeapon(f.constructRangedWeapons(0, c));
 						c.setWeapon(c.getRangedStore().get(c.getRangedStore().size()-1));
 					}else{
 						if (c.getMoney() >= 100){
 							c.addWeapon(f.constructRangedWeapons(0, c));
 							c.setWeapon(c.getRangedStore().get(c.getRangedStore().size()-1));
 							c.addMoney(-100);
 							c.updateBoolean(0);
 						}else{
 							System.out.println("You do not have enough money.");
 						}
 					}
 				}else if (!e.isShiftDown() && keyCode == KeyEvent.VK_2){
 					if (c.getBoughtWeapons().get(1) == true){
 						c.addWeapon(f.constructRangedWeapons(1, c));
 						c.setWeapon(c.getRangedStore().get(c.getRangedStore().size()-1));
 					}else{
 						if (c.getMoney() >= 300){
 							c.addWeapon(f.constructRangedWeapons(1, c));
 							c.setWeapon(c.getRangedStore().get(c.getRangedStore().size()-1));
 							c.addMoney(-300);
 							c.updateBoolean(1);
 						}else{
 							System.out.println("You do not have enough money.");
 						}	
 					}
 				}else if (!e.isShiftDown() && keyCode == KeyEvent.VK_3){
 					if (c.getBoughtWeapons().get(2) == true){
 						c.addWeapon(f.constructRangedWeapons(2, c));
 						c.setWeapon(c.getRangedStore().get(c.getRangedStore().size()-1));
 					}else{
 						if (c.getMoney() >= 400){
 							c.addWeapon(f.constructRangedWeapons(2, c));
 							c.setWeapon(c.getRangedStore().get(c.getRangedStore().size()-1));
 							c.addMoney(-400);
 							c.updateBoolean(2);
 						}else{
 							System.out.println("You do not have enough money.");
 						}
 					}
 				}else if (!e.isShiftDown() && keyCode == KeyEvent.VK_4){
 					if (c.getBoughtWeapons().get(3) == true){
 						c.addWeapon(f.constructRangedWeapons(3, c));
 						c.setWeapon(c.getRangedStore().get(c.getRangedStore().size()-1));
 					}else{
 						if(c.getMoney() >= 1000){
 							c.addWeapon(f.constructRangedWeapons(3, c));
 							c.setWeapon(c.getRangedStore().get(c.getRangedStore().size()-1));
 							c.addMoney(-1000);
 							c.updateBoolean(3);
 						}else{
 							System.out.println("You do not have enough money.");
 						}
 						
 					}
 				}else if (!e.isShiftDown() && keyCode == KeyEvent.VK_5){
 					if (c.getBoughtWeapons().get(4) == true){
 						c.addWeapon(f.constructRangedWeapons(4, c));
 						c.setWeapon(c.getRangedStore().get(c.getRangedStore().size()-1));
 					}else{
 						if(c.getMoney() >= 500){
 							c.addWeapon(f.constructRangedWeapons(4, c));
 							c.setWeapon(c.getRangedStore().get(c.getRangedStore().size()-1));
 							c.addMoney(-500);
 							c.updateBoolean(4);
 						}else{
 							System.out.println("You do not have enough money.");
 						}
 						
 					}
 				}else if (!e.isShiftDown() && keyCode == KeyEvent.VK_6){
 					if (c.getBoughtWeapons().get(5) == true){
 						c.addWeapon(f.constructRangedWeapons(5, c));
 						c.setWeapon(c.getRangedStore().get(c.getRangedStore().size()-1));
 					}else{
 						if(c.getMoney() >= 200){
 							c.addWeapon(f.constructRangedWeapons(5, c));
 							c.setWeapon(c.getRangedStore().get(c.getRangedStore().size()-1));
 							c.addMoney(-200);
 							c.updateBoolean(5);
 						}else{
 							System.out.println("You do not have enough money.");
 						}
 						
 					}
 				}else if (e.isShiftDown() && keyCode == KeyEvent.VK_1){
 					if (c.getBoughtWeapons().get(7) == true){
 						c.addWeapon(f.constructRangedWeapons(7, c));
 						c.setWeapon(c.getRangedStore().get(c.getRangedStore().size()-1));
 					}else{
 						if(c.getMoney() >= 300){
 							c.addWeapon(f.constructRangedWeapons(7, c));
 							c.setWeapon(c.getRangedStore().get(c.getRangedStore().size()-1));
 							c.addMoney(-300);
 							c.updateBoolean(7);
 						}else{
 							System.out.println("You do not have enough money.");
 						}
 						
 					}
 				}else if (e.isShiftDown() && keyCode == KeyEvent.VK_2){
 					if (c.getBoughtWeapons().get(8) == true){
 						c.addWeapon(f.constructRangedWeapons(8, c));
 						c.setWeapon(c.getRangedStore().get(c.getRangedStore().size()-1));
 					}else{
 						if(c.getMoney() >= 500){
 							c.addWeapon(f.constructRangedWeapons(8, c));
 							c.setWeapon(c.getRangedStore().get(c.getRangedStore().size()-1));
 							c.addMoney(-500);
 							c.updateBoolean(8);
 						}else{
 							System.out.println("You do not have enough money.");
 						}
 						
 					}
 				}else if (e.isShiftDown() && keyCode == KeyEvent.VK_3){
 					if (c.getBoughtWeapons().get(9) == true){
 						c.addWeapon(f.constructRangedWeapons(9, c));
 						c.setWeapon(c.getRangedStore().get(c.getRangedStore().size()-1));
 					}else{
 						if(c.getMoney() >= 1000){
 							c.addWeapon(f.constructRangedWeapons(9, c));
 							c.setWeapon(c.getRangedStore().get(c.getRangedStore().size()-1));
 							c.addMoney(-1000);
 							c.updateBoolean(9);
 						}else{
 							System.out.println("You do not have enough money.");
 						}
 						
 					}
 				}else if (e.isShiftDown() && keyCode == KeyEvent.VK_4){
 					if (c.getBoughtWeapons().get(10) == true){
 						c.addWeapon(f.constructRangedWeapons(10, c));
 						c.setWeapon(c.getRangedStore().get(c.getRangedStore().size()-1));
 					}else{
 						if(c.getMoney() >= 300){
 							c.addWeapon(f.constructRangedWeapons(10, c));
 							c.setWeapon(c.getRangedStore().get(c.getRangedStore().size()-1));
 							c.addMoney(-300);
 							c.updateBoolean(10);
 						}else{
 							System.out.println("You do not have enough money.");
 						}
 						
 					}
 				}else if (e.isShiftDown() && keyCode == KeyEvent.VK_5){
 					if (c.getBoughtWeapons().get(11) == true){
 						c.addWeapon(f.constructRangedWeapons(11, c));
 						c.setWeapon(c.getRangedStore().get(c.getRangedStore().size()-1));
 					}else{
 						if(c.getMoney() >= 500){
 							c.addWeapon(f.constructRangedWeapons(11, c));
 							c.setWeapon(c.getRangedStore().get(c.getRangedStore().size()-1));
 							c.addMoney(-500);
 							c.updateBoolean(11);
 						}else{
 							System.out.println("You do not have enough money.");
 						}
 						
 					}
 				}else if (e.isShiftDown() && keyCode == KeyEvent.VK_6){
 					if (c.getBoughtWeapons().get(12) == true){
 						c.addWeapon(f.constructRangedWeapons(12, c));
 						c.setWeapon(c.getRangedStore().get(c.getRangedStore().size()-1));
 					}else{
 						if(c.getMoney() >= 1000){
 							c.addWeapon(f.constructRangedWeapons(12, c));
 							c.setWeapon(c.getRangedStore().get(c.getRangedStore().size()-1));
 							c.addMoney(-1000);
 							c.updateBoolean(12);
 						}else{
 							System.out.println("You do not have enough money.");
 						}
 						
 					}
 				
 				}
 			}
 
 			public void keyReleased(KeyEvent e) {
 				int keyCode = e.getKeyCode();
 				if (keyCode == KeyEvent.VK_A || keyCode == KeyEvent.VK_D) {
 					g.retractWeapon(KeyEvent.VK_A);
 					g.retractWeapon(KeyEvent.VK_D);
 				}
 			}
 		});
 
 		while (keepGoing) {
 			try {
 				lastTime = curTime;
 				curTime = System.currentTimeMillis();
 				totalTime += curTime - lastTime;
 				gravityTime += curTime - lastTime;
 				enemyDamageTime += curTime - lastTime;
 				if (keepGoing) {
 					if (gravityTime > value) {
 						value += gravityRate;
 						g.moveCharacter(0, 1, lastKey);
 						g.applyDot();
 						for (int a = 0; a < 2; a++) {
 							g.moveRangedWeapon();
 						}
 
 						Point charLoc = g.getCharacterLocation();
 						ArrayList<Point> enemyLocs = g.getEnemyLocation();
 						for (int j = 0; j < enemyLocs.size(); j++) {
 							if (charLoc.distance(enemyLocs.get(j)) <= 1) {
 								keepGoing = g.characterDamage(g.getGrid().get(enemyLocs.get(j)).returnEnemy());
 								if (!keepGoing) {
 									g.gameOver();
 								}
 							}
 						}
 
 						if (keepGoing) {
 							// check every instance of p
 							for (int i = 0; i < g.getEnemyLocation().size(); i++) {
 								Point p = g.getEnemyLocation().get(i);
 								Point q = new Point((int) p.getX(), (int) p.getY() + 1);
 								GridSpace gs = g.getGrid().get(q);
 
 								if (p.getX() - g.getCharacterLocation().getX() > 0) {
 									g.moveEnemy(-1, 0, p);
 								} else {
 									g.moveEnemy(1, 0, p);
 								}
 
 								if (g.getCharacterLocation().getX() > g.getEnemyLocation().get(i).getX()) {
 									Point check = new Point((int) (p.getX() + 1), (int) (p.getY()));
 									GridSpace more = g.getGrid().get(check);
 									if (more.hasSolid()) {
 										for (Terrain t : more.returnTerrain()) {
 											if (t.getSolid()) {
 												g.moveEnemy(0, -1, p);
 												g.moveEnemy(0, -1, p);
 												g.moveEnemy(0, -1, p);
 												g.moveEnemy(0, -1, p);
 											}
 										}
 										for (LivingThing e : more.returnLivingThings()) {
 											if (e.getSolid() && !(e instanceof Character)) {
 												g.moveEnemy(0, -1, p);
 												g.moveEnemy(0, -1, p);
 												g.moveEnemy(0, -1, p);
 												g.moveEnemy(0, -1, p);
 											}
 										}
 									}
 								} else if (g.getCharacterLocation().getX() < g.getEnemyLocation().get(i).getX()) {
 									Point check = new Point((int) (p.getX() - 1), (int) (p.getY()));
 									GridSpace more = g.getGrid().get(check);
 									if (more.hasSolid()) {
 										for (Terrain t : more.returnTerrain()) {
 											if (t.getSolid()) {
 												g.moveEnemy(0, -1, p);
 												g.moveEnemy(0, -1, p);
 												g.moveEnemy(0, -1, p);
 												g.moveEnemy(0, -1, p);
 											}
 										}
 										for (LivingThing e : more.returnLivingThings()) {
 											if (e.getSolid() && !(e instanceof Character)) {
 												g.moveEnemy(0, -1, p);
 												g.moveEnemy(0, -1, p);
 												g.moveEnemy(0, -1, p);
 												g.moveEnemy(0, -1, p);
 											}
 										}
 									}
 								}
 
 								g.moveEnemy(0, 1, p);
 							}
 							if (gravityTime > 4 * gravityRate + hangTime) {
 								gravityTime = 0;
 								value = gravityRate + hangTime;
 							}
 
 							if (enemyDamageTime > 500) {
 
 							}
 
 							// clear back buffer...
 							if (g.getCharacterLocation().getX() >= 100) {
 								HashMap<Point, GridSpace> grid = g.getGrid();
 								Point oldLocation = g.getCharacterLocation();
 								Character c = grid.get(oldLocation).returnCharacter();
 								c.setHp(c.getMaxHp());
 								World w = new World();
 								int killed = g.getNumKilled();
 								g = w.drawWorld(1, killed);
 								stage++;
 								grid = g.getGrid();
 								g.setNumKilled(killed);
 								ArrayList<Thing> t = new ArrayList<Thing>();
 								t.add(c);
 								GridSpace gs = new GridSpace(t);
 								gs.sortArrayOfThings();
 								grid.put(new Point(1, (int) oldLocation.getY() - 1), gs);
 								g.setCharacterLocation(new Point(1, (int) oldLocation.getY() - 1));
 
 								Random r = new Random();
 								int numEnemies = r.nextInt(stage) + 1;
 
 								for (int i = 0; i < numEnemies; i++) {
 									String name = "Yo Mama";
 									Color d = Color.GRAY;
 									Point p = g.findValidEnemyLocation();
 									if (p != null) {
 										g.spawnNewEnemy(p, new Enemy(true, d, name, 10, 10, 10));
 									}
 								}
 							}
 						}
 					}
 				}
 				if (totalTime > 1000) {
 
 					totalTime -= 1000;
 					fps = frames;
 					frames = 0;
 				}
 				++frames;
 				g2d = bi.createGraphics();
 				g2d.setColor(background);
 				g2d.fillRect(0, 0, 639, 479);
 				HashMap<Point, GridSpace> grid = g.getGrid();
 				for (int i = 0; i < 100; i++) {
 
 					for (int j = 0; j < 25; j++) {
 						g2d.setColor(grid.get(new Point(i, j)).getColor());
 						g2d.fillRect(i * 10, j * 10, 10, 10);
 
 					}
 
 				}
 
 				// display frames per second...
 
 				g2d.setFont(new Font("Courier New", Font.PLAIN, 12));
 				g2d.setColor(Color.GREEN);
 				g2d.drawString(String.format("FPS: %s", fps), 20, 20);
 				g2d.drawString(String.format("Stage: %s", stage), 100, 20);
 				g2d.drawString(String.format("Enemies killed: %s", g.getNumKilled()), 180, 20);
 				if (keepGoing) {
 					try {
 						g2d.drawString("Current weapon: "
 								+ g.getGrid().get(g.getCharacterLocation()).returnCharacter().getWeapon().getName(),
 								620, 20);
 					} catch (NullPointerException e) {
 						System.out.println("Caught null pointer error on HUD for weapon.");
 					} catch (ConcurrentModificationException c) {
 						System.out.println("Caught concurrent modification exception.");
 					}
 					try {
 						Character c = g.getGrid().get(g.getCharacterLocation()).returnCharacter();
 						g2d.drawString("Current level: " + c.getLevel(), 840, 20);
 						String healthString = "";
 						for (int i = 0; i < c.getMaxHp(); i += 5) {
 							if (c.getHp() > i) {
 								healthString += "* ";
 							} else {
 								healthString += "_ ";
 							}
 						}
 
 						g2d.drawString("Health: " + healthString, 320, 20);
 
 					} catch (NullPointerException e) {
 						System.out.println("Caught that error");
 						g2d.drawString("Health: Dead", 320, 20);
 					}
 					try{
 						//0=basic bow + arrows
 						//1=longbow + arrows
 						//2=basic crossbow + bolts
 						//3=finely crafted crossbow + bolts
 						//4=javelins
 						//5=throwing knive
 						g2d.drawString("WELCOME TO THE MARKET", 5, 270);
 						g2d.drawString("PRESS A NUMBER OR SYMBOL TO BUY A WEAPON FROM THE MERCHANT. YOUR CURRENT MONEY IS: $" + g.getGrid().get(g.getCharacterLocation()).returnCharacter().getMoney(), 5, 285);
 						g2d.drawString("1: Basic Bow ($100)", 5, 305);
 						g2d.drawString("2: Long Bow ($300)", 5, 320);
 						g2d.drawString("3: Cross Bow ($400)", 5, 335);
 						g2d.drawString("4: Cross Bow Plus ($1000)", 5, 350);
 						g2d.drawString("5: Javelin ($500)", 5, 365);
 						g2d.drawString("6: Throwing Knives ($200)", 5, 380);	
 						g2d.drawString("!: Fire Magic 1 ($300)", 200, 305);
 						g2d.drawString("@: Fire Magic 2 ($500)", 200, 320);
 						g2d.drawString("#: Fire Magic 3 ($1000)", 200, 335);
 						g2d.drawString("$: Ice Magic 1 ($300)", 200, 350);
 						g2d.drawString("%: Ice Magic 2 ($500)", 200, 365);
 						g2d.drawString("^: Ice Magic 3 ($1000)", 200, 380);
 						g2d.drawString("P: Enemy 1 ($5)", 375, 305);
 						g2d.drawString("[: Enemy 2 ($10)", 375, 320);
 						g2d.drawString("]: Enemy 3 ($20)", 375, 335);
						g2d.drawString("\": Boss ($50)", 375, 350);
 						
 						
 					}catch (Exception e){
 						System.out.println("Caught some error.");
 					}
 				}
 				// Blit image and flip...
 
 				graphics = buffer.getDrawGraphics();
 				graphics.drawImage(bi, 0, 0, null);
 				if (!buffer.contentsLost()) {
 
 					buffer.show();
 				}
 				// Let the OS have a little time...
 				Thread.yield();
 
 			} finally {
 
 				// release resources
 				if (graphics != null)
 					graphics.dispose();
 				if (g2d != null)
 					g2d.dispose();
 
 			}
 
 		}
 
 	}
 }
