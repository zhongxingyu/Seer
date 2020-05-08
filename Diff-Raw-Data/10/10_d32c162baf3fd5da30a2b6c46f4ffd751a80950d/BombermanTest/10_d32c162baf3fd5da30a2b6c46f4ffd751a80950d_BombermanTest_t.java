 import org.newdawn.slick.Animation;
 import org.newdawn.slick.AppGameContainer;
 import org.newdawn.slick.BasicGame;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.SpriteSheet;
 import org.newdawn.slick.tiled.TiledMap;
 import org.newdawn.slick.geom.Polygon;
 
 public class BombermanTest extends BasicGame {
 
 	private float playerX = 31;
 	private float playerY = 31;
 	private BlockMap map;
 	private Animation player;
 	private Polygon playerPoly;
 	private boolean Xtendency, Ytendency;
 	private boolean pos = true;
 	private boolean neg = false;
 	Boolean debug = true; // True um zusätzliche Infos anzuzeigen.
 
 	public BombermanTest() {
 		super("BOMBASTISCHER MANN");
 	}
 
 	public void init(GameContainer container) throws SlickException {
 		container.setVSync(true);
 		SpriteSheet sheet = new SpriteSheet("res/bomberman1.png", 32, 32);
 		map = new BlockMap("res/testmap2.tmx");
 		player = new Animation();
 		player.setAutoUpdate(true);
 		for (int frame = 0; frame < 3; frame++) {
 			player.addFrame(sheet.getSprite(frame, 0), 150);
 		}
 
		playerPoly = new Polygon(new float[] { playerX, playerY, playerX + 31,
				playerY, playerX + 31, playerY + 31, playerX, playerY + 31 });
 	}
 
 	
 	/* Bewegung von Bombi! Inklusive Kollisionsabfrage (sehr buggy :( )*/
 	
 	public void moveplayer(float x, float y) throws SlickException {
 
 		float Xtemp = playerX;
 		float Ytemp = playerY;
 
 		playerX = x;
 		playerY = y;
 		
 		playerPoly.setX(playerX);
 		playerPoly.setY(playerY);
 		
 		if (entityCollisionWith()){
 			
 			playerX = Xtemp;
 			playerY = Ytemp;
 			
 			playerPoly.setX(playerX);
 			playerPoly.setY(playerY);
 			
 		}
 		
 		
 
 	}
 
 	// Steuerung (Very Basic!)
 
 	public void update(GameContainer container, int delta)
 			throws SlickException {
 
 		/*
 		 * MAGNETISMUS:
 		 * 
 		 * Wenn der Player sich an einer nicht durch 32 teilbaren XYPosition
 		 * befindet wird er solange bewegt bis er eine erreicht. Die Richtung in
 		 * die er sich bewegt wird dabei von Xtendency bzw. Ytendency bestimmt.
 		 */
 
 		if (playerX % 32 != 0) {// Für X
 
			if (Xtendency == pos) {
 
 				moveplayer(playerX + 1, playerY);
 			} else {
 				moveplayer(playerX - 1, playerY);
 			}
 
 		}
 
 		if (playerY % 32 != 0) {// Für Y
 
			if (Ytendency == pos) {
 
 				moveplayer(playerX, playerY + 1);
 			} else {
 				moveplayer(playerX, playerY - 1);
 			}
 
 		}
 
 		if (container.getInput().isKeyDown(Input.KEY_LEFT)) {
 			Xtendency = neg;
 			if (playerX % 32 == 0) {
 				moveplayer(playerX - 1, playerY);
 			}
 		}
 
 		if (container.getInput().isKeyDown(Input.KEY_RIGHT)) {
 			Xtendency = pos;
 			if (playerX % 32 == 0) {
 				moveplayer(playerX + 1, playerY);
 			}
 		}
 
 		if (container.getInput().isKeyDown(Input.KEY_UP)) {
 			Ytendency = neg;
 			if (playerX % 32 == 0) {
 				moveplayer(playerX, playerY - 1);
 			}
 		}
 
 		if (container.getInput().isKeyDown(Input.KEY_DOWN)) {
 			Ytendency = pos;
 			if (playerX % 32 == 0) {
 				moveplayer(playerX, playerY + 1);
 			}
 		}
 		
 		
 
 
 		if (container.getInput().isKeyDown(Input.KEY_ESCAPE)) { // Esc beendet
 																// Spiel
 			System.exit(0);
 		}
 	}
 
 	public boolean entityCollisionWith() throws SlickException {
 		
 		for (int i = 0; i < BlockMap.entities.size(); i++) {
 			
 			Block entity1 = (Block) BlockMap.entities.get(i);
 			
 			if (playerPoly.intersects(entity1.poly)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public void render(GameContainer container, Graphics g) {
 		BlockMap.tmap.render(0, 0);
 		g.drawAnimation(player, playerX, playerY);
 		g.draw(playerPoly);
 		if (debug) {
 			g.drawString("X:" + playerX + " Y:" + playerY, 32, 460);
 			try {
 				if (entityCollisionWith()){
 					g.drawString("Coll: False", 256,460);
 				} else{
 					g.drawString("Coll: True", 256,460);
 				}
 			} catch (SlickException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 				
 			
 		}
 	}
 
 	public static void main(String[] argv) throws SlickException {
 		AppGameContainer container = new AppGameContainer(new BombermanTest(),
 				640, 480, false);
 		container.start();
 	}
 }
