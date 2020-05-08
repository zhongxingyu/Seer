 package states;
 
 
 
 import java.util.ArrayList;
 
 import lib.LevelLoader;
 
 import ships.AlienMotherShip;
 import ships.AlienShip;
 import stuff.Game;
 
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.geom.Rectangle;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 
 import entities.Bullet;
 import entities.Entity;
 import entities.Ship;
 import entities.Weapon;
 
 import stuff.CollisionDetection;
 import stuff.ShipContainer;
 
 public class GamePlayState extends BasicGameState {
 
 	private int id;
 
 	private Ship playerEnt;
 	private AlienMotherShip alienMotherShip;
 	public static ArrayList<Bullet> bullets;
 	public static ArrayList<Rectangle> removeList;
 	public static ArrayList<Ship> entityList;
 	public static ArrayList<Rectangle> starsList;
 	private String scoreLabel = "Score";
 	public static int score = 0;
 	public static boolean gameOver = false;
 	private boolean win = false;
 	private boolean gameStarted;
 	public static boolean lostLive = false;
 	public int lostLiveDrawCounter = 0;
 	private boolean drawWeapons = false;
 	private int screenWidth;
 	private int screenHeight;
 	private boolean drawAlertOverlay = false;
 	
 	Image entityImage = null;
 	
 	public GamePlayState(int gameplaystate) {
 		this.id = gameplaystate;
 	}
 
 	@Override
 	public void init(GameContainer gc, StateBasedGame state)
 			throws SlickException {
 		gc.setVSync(true);
 		gc.setTargetFrameRate(60);
 		gc.setShowFPS(false);
 		
 		
 		screenWidth = gc.getWidth();
 		screenHeight = gc.getHeight();
 		
 		gameStarted = false;
 
 		//INITIALIZE LISTS
 		bullets = new ArrayList<Bullet>();
 		entityList = new ArrayList<Ship>();
 		
 		//ADD PLAYER SHIP
 		playerEnt = new Ship((gc.getWidth() / 2) - 15, 550, 32, 32);
 		playerEnt.setLives(3);
 		entityList.add(playerEnt);
 		
 		LevelLoader.initLevel(1);
 		entityList.addAll(LevelLoader.shipContainer.alienShips);
 		
 		
 		//=============================================================================
 		// START: CREATING SOME INITIAL STARS 
 		//=============================================================================
 		starsList = new ArrayList<Rectangle>();
 		//=============================================================================
 		// END: CREATING SOME INITIAL STARS 
 		//=============================================================================
 		
 		removeList = new ArrayList();
 	}
 
 	
 	
 	
 	@Override
 	public void render(GameContainer gc, StateBasedGame sbg, final Graphics g) throws SlickException {
 		
 		g.setLineWidth(1);
 		//BACKGROUND ==================================================================
 		g.setColor(Color.black);
 		g.fillRect(0,  0, 800, 600);
 		
 		// STARS =======================================================================
 		g.setColor(Color.white);
 		for(Rectangle r: starsList) {
 			g.fillRect(r.getX(), r.getY(), r.getWidth(), r.getHeight());
 		}
 		
 		if(gameStarted)
 		{
 			//============================================================================================
 			// START: ALIEN SHIP CONTAINER 
 			//============================================================================================
 			float tmp_x = 0;
 			float tmp_y = 0;
 			float tmp_max_x = 0;
 			int tmp_row = 0;
 			for(Ship e: entityList) {
 				if(e instanceof AlienShip) {
 					g.setColor(((AlienShip) e).getColor());
 					
 					if(tmp_x == 0)
 						tmp_x = e.getX();
 					else {
 						if(tmp_x > e.getX())
 							tmp_x = e.getX();
 					}
 					
 					if(tmp_y == 0)
 						tmp_y = e.getY();
 					else {
 						if(tmp_y > e.getY())
 							tmp_y = e.getY();
 					}
 					if(((AlienShip) e).getRow() > tmp_row)
 						tmp_row = ((AlienShip) e).getRow();
 					
 					if(e.getX() > tmp_max_x)
 						tmp_max_x = e.getX();
 					
 				} //END: if (e instanceof AlienShip)
 				else
 					g.setColor(Color.white);
 				
 				e.draw(g, (int) e.getX(), (int) e.getY());
 					
 					
 			}//END: (Entity e: entityList)
 			
 			LevelLoader.shipContainer.borders.setX(tmp_x);
 			LevelLoader.shipContainer.borders.setY(tmp_y);
 			LevelLoader.shipContainer.borders.setHeight((int) (tmp_row*40)-10);
 			LevelLoader.shipContainer.borders.setWidth((int)(tmp_max_x-tmp_x)+30);
 			
 			if(((LevelLoader.shipContainer.borders.getY()+ LevelLoader.shipContainer.borders.getHeight()) > 350) && LevelLoader.shipContainer.getDx() != 15) {
 				LevelLoader.shipContainer.setDx(15);
 				LevelLoader.shipContainer.setDy(15);
 			}
 			//============================================================================================
 			// END: ALIEN SHIP CONTAINER 
 			//============================================================================================
 		
 			
 			// BULLETS =================================================
 			g.setColor(Color.yellow);
 			for(Bullet b: bullets) {
 				g.drawRect(b.getX(), b.getY(), b.getHeight(), b.getWidth());
 			}
 			
 			
 			
 			//Weapons
 			if(drawWeapons) {
 				int w_x = 10;
 				int w_y = gc.getHeight() - 60;
 				int w_cntr = 0;
 				g.setLineWidth(3);
 				
 				for (Weapon w : playerEnt.getWeapons()) {
 					if(playerEnt.getSelectedWeaponIndex() == w_cntr)
 						g.setColor(Color.white);
 					else
 						g.setColor(Color.gray);
 					
 					w.getImg().draw(w_x, w_y);
 					g.drawRoundRect(w_x, w_y, 45, 45, 4, 4);
 					
 					
 					if(playerEnt.getSelectedWeaponIndex() != w_cntr) {
 						Color trans = new Color(0f,0f,0f,0.7f);
 				        g.setColor(trans);
 				        g.fillRect(w_x+3, w_y+3, 40, 40);
 					}
 					
 					w_cntr++;
 					
 					w_y -= 60;
 				}
 			}
 			
 			
 			drawHUD(gc, sbg, g);
 		}
 		else
 		{
 			g.drawString("Are you ready?" , (gc.getWidth() / 2) - 60, (gc.getHeight() / 2) - 10);
 			g.drawString("Press return to start the game" , (gc.getWidth() / 2) - 120, (gc.getHeight() / 2) + 30);
 		}
 		
 		if(drawAlertOverlay)
 			drawAlertOverlay(gc, sbg, g);
 		
 		// SCORE ====================================================
 		g.setColor(Color.white);
 		g.drawString(scoreLabel, 650, 10);
 		g.drawString(Integer.toString(score), 700, 10);
 		
 		// LIVES ====================================================
 		g.setColor(Color.white);
 		g.drawString("LIVES", 30, 20);
 		int lives_x = 70;
 		for (int i = 1; i <= playerEnt.getLives(); i++) {
 			lives_x += 20; 
 			g.fillRect(lives_x, 25, 10, 10);
 		}
 		
 		if(lostLive) {
 			
 			Color trans = new Color(1f,0f,0f,0.5f);
 	        g.setColor(trans);
 	        g.fillRect(0,0, screenWidth, screenHeight);
 	        if(lostLiveDrawCounter == 6) {
 	        	lostLive = false;
 	        	lostLiveDrawCounter = 0;
 	        }
 	        
 	        lostLiveDrawCounter++;
 	        
 		}
 		
 		
 		
 		// GAME OVER ================================================
 		if(gameOver) {
 			g.setColor(new Color(221, 72, 20));
 			String gameOverString = "GAME OVER";
 			if(win)
 				gameOverString = "YOU WON!";
 			else
 				gameOverString = "YOU LOST!";
 				
 			g.drawString(gameOverString , (screenWidth / 2) - 20, (screenHeight / 2) - 10);
 		}
 	}
 
 	
 	
 	
 	@Override
 	public void update(GameContainer gc, StateBasedGame sbg, int delta)
 			throws SlickException {
 		
 		//==================================================================
 		// START: RANDOM STARS
 		//==================================================================
 		int rand = (int) (Math.random() * (500 - 1) + 1);
 		if(rand <= 40)
 		{
 			int stars_x = (int) (Math.random() * (800 - 1) + 1);
 			int width =	3;
 			if(rand < 20)
 				width = 1;
 			starsList.add(new Rectangle(stars_x, -12,	width, 7));
 		}
 		
 		//CHECK WHETHER STARS ARE OUT OF SCREEN => REMOVE OBJECT
 		for(Rectangle r: starsList) {
 			r.setY((float) (r.getY()+((delta *9.5) / 100)));
 			if(r.getY() > 650)
 				removeList.add(r);
 		}
 		//==================================================================
 		// END: RANDOM STARS
 		//==================================================================
 
 		
 		handleInput(gc, playerEnt, delta);
 		
 		if(!gameOver && gameStarted) {
 			
 			boolean enemiesAlive = false;
 			
 			//temporary not active  due to implementation of Levels
 //			if(LevelLoader.shipContainer.borders.getY() >= 100 && alienMotherShip == null) {
 //				alienMotherShip = new AlienMotherShip(null, -80, 20, 30, 90, 0);
 //				entityList.add(alienMotherShip);
 //			}
 
 			for(Ship s: entityList) {
 				s.updateLogic(delta);
 				
 				if (s instanceof AlienShip && !(s instanceof AlienMotherShip))
 					enemiesAlive = true;
 				
 				if(s.getBulletLoader().size() > 0)
 				{
 					bullets.addAll(s.getBulletLoader());
 					s.getBullets().addAll(s.getBulletLoader());
 					s.getBulletLoader().clear();
 				}
 			}
 			
 			
 			if(playerEnt.isEmergency())
 				drawAlertOverlay = true;
 			else
 				drawAlertOverlay = false;
 			
 			//Bullets
 			for(Bullet b: bullets) {
 				b.updateLogic(delta, gc);
 				
 				for (Entity ship: entityList) {
 					
 					CollisionDetection.checkCollision(b, ship);
 					boolean removed = false;
 					if(b.getLives() <= 0 || b.isDestroyed())
 					{
 						removeList.add(b);
 						removed = true;
 					}
 					if(ship.getLives() <= 0)
 						removeList.add(ship);
 				}
 			}
 			
 			
 			if(playerEnt.getLives() <= 0) {
 					gameOver = true;
 					win = false;
 			}
 			else if (!enemiesAlive) {
 				if(LevelLoader.nextLevelExists()) {
 					boolean levelChanged = LevelLoader.changeToNextLevel();
 					if(levelChanged)
 						entityList.addAll(LevelLoader.shipContainer.alienShips);
 					else
 					{
 						gameOver = true;
 						win = true;
 					}
 						
 				}
 				else
 				{
 					gameOver = true;
 					win = true;
 				}
 			}
 			
 			
 			if(alienMotherShip != null)
 				alienMotherShip.updateLogic(delta);
 			
 			
 			// REMOVE NOT NEEDED OBJECTS ==========================================
 			if(removeList.size() > 0) {
 				entityList.removeAll(removeList);
 				bullets.removeAll(removeList);
 				starsList.removeAll(removeList);
 				removeList.clear();
 			}
 		}
 		
 		
 		
 		if (gc.getInput().isKeyPressed(Input.KEY_ESCAPE)) {
 			sbg.enterState(Game.GAMEMENUSTATE);
 		}
 		
 	}
 
 	
 	@Override
 	public int getID() {
 		return this.id;
 	}
 	
 	
 	
 	public void handleInput(GameContainer gc, Ship entity, int delta) throws SlickException {
 		
 		
 		if(gameStarted && !gameOver) {
 			if (gc.getInput().isKeyDown(gc.getInput().KEY_RIGHT)) {
 				entity.setMoveRight(true);
 			}
 			else if (gc.getInput().isKeyDown(gc.getInput().KEY_LEFT)) {
 				entity.setMoveLeft(true);
 			}
 			
 			if (!gc.getInput().isKeyDown(gc.getInput().KEY_RIGHT)) {
 				entity.setMoveRight(false);
 			}
 			if (!gc.getInput().isKeyDown(gc.getInput().KEY_LEFT)) {
 				entity.setMoveLeft(false);
 			}
 			
 			
 			if (gc.getInput().isKeyPressed(gc.getInput().KEY_C)) {
 				if(drawWeapons)
 					drawWeapons = false;
 				else
 					drawWeapons = true;
 			}
 			
 			
 			if (drawWeapons) {
 				if (gc.getInput().isKeyPressed(gc.getInput().KEY_TAB)) {
 					System.out.println("change Weapon");
 					int w_count = playerEnt.getWeaponsCount();
 					int w_cur_index = playerEnt.getSelectedWeaponIndex();
 					System.out.println("selected Weapon Index:"+w_cur_index);
 					System.out.println("selected Weapon w_count:"+w_count);
 					System.out.println("selected Weapon index increase:"+w_cur_index);
 					
 					
 					if(w_cur_index >= (w_count - 1))
 						playerEnt.setSelectedWeaponIndex(0);
 					else
 						playerEnt.setSelectedWeaponIndex(w_cur_index+1);
 				}
 			}
 			
 			if(gc.getInput().isKeyPressed(gc.getInput().KEY_SPACE))
 				entity.getSelectedWeapon().shoot(entity);
 			
 			entity.move(delta);
 		}
 		else
 		{
 			if (gc.getInput().isKeyPressed(gc.getInput().KEY_RETURN)) {
 				System.out.println("PRESSED");
 				gameStarted =  true;
 			}
 		}
 	}
 	
 	
 	public void drawHUD(GameContainer gc, StateBasedGame sbg, final Graphics g) {
		
 		//START: DRAW CURRENT WEAPON ========================================
		//not necessary if choosing a weapon
		if(!drawWeapons)
		{
			int w_x = 10;
			int w_y = gc.getHeight() - 60;
			g.setLineWidth(3);
			g.setColor(Color.white);
			playerEnt.getSelectedWeapon().getImg().draw(w_x, w_y);
			g.drawRoundRect(w_x, w_y, 45, 45, 4, 4);
		}
 		//END: DRAW CURRENT WEAPON ========================================
 		
 		
 		
 		//START: DRAW WEAPON HEAT DISPLAY =====================================
 		g.setColor(Color.white);
 		g.setLineWidth(3);
 		g.drawRect(screenWidth - 30, screenHeight - 120, 20, 100);
 		
 		float full = 100;
 		float one = playerEnt.getSelectedWeapon().getHeatLoad() / 100;
 		
 		g.setColor(Color.red);
 		int tmp_y = ((screenHeight - 120)+100) - playerEnt.getSelectedWeapon().getHeat();
 		g.fillRect(screenWidth - 28, tmp_y, 17, playerEnt.getSelectedWeapon().getHeat());
 		//START: DRAW WEAPON HEAT DISPLAY =====================================
 	}
 	
 	
 	
 	public void drawAlertOverlay(GameContainer gc, StateBasedGame sbg, final Graphics g) {
 		Color trans = new Color(1f,0f,0f,0.5f);
         g.setColor(trans);
         g.fillRect(0,0, screenWidth, screenHeight);
 	}
 	
 	
 }
