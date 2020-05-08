 package chalmers.TDA367.B17;
 
 import org.newdawn.slick.*;
 
 import chalmers.TDA367.B17.controller.*;
 import chalmers.TDA367.B17.model.*;
 import chalmers.TDA367.B17.powerups.DamagePowerUp;
 import chalmers.TDA367.B17.powerups.FireRatePowerUp;
 import chalmers.TDA367.B17.powerups.Shield;
 import chalmers.TDA367.B17.powerups.ShieldPowerUp;
 import chalmers.TDA367.B17.powerups.SpeedPowerUp;
 import chalmers.TDA367.B17.spawnpoints.PowerUpSpawnPoint;
 import chalmers.TDA367.B17.spawnpoints.TankSpawnPoint;
 import chalmers.TDA367.B17.terrain.BrownWall;
 import chalmers.TDA367.B17.view.Lifebar;
 import chalmers.TDA367.B17.weaponPickups.*;
 import chalmers.TDA367.B17.weapons.*;
 
 import org.newdawn.slick.geom.*;
 import org.newdawn.slick.state.*;
 import java.awt.Point;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Map.Entry;
 
 public class Play extends BasicGameState{
 	
 	public ArrayList<AbstractTurret> turrets;
 	
 	private GameController controller;
 	private ArrayList<Player> players;
 	private Player playerOne;
 	private Entity obstacle;
 	private Image map = null;
 	private Point mouseCoords;
 	private Input input;
 	private SpriteSheet entSprite = null;
 	Lifebar lifebar;
 	
 	private Player playerTwo;
 	private Player playerThree;
 	private Player playerFour;
 	
 	public Play(int state) {
 		
 	}
 	
 	@Override
 	public void init(GameContainer gc, StateBasedGame sbg) throws SlickException {
 
 		controller = GameController.getInstance();
 		
 		gc.setMouseCursor(new Image("data/crosshair.png"), 16, 16);
 
		controller.newGame(GameController.SCREEN_WIDTH, GameController.SCREEN_HEIGHT, 10, 4, 2, 5000, 500000, 1500000);
 
 		//Players
 		playerOne = new Player("Player One");
 		players = new ArrayList<Player>();
 		players.add(playerOne);
 		
 		playerTwo = new Player("Player Two");
 		players.add(playerTwo);
 		
 		playerThree = new Player("Player Three");
 		players.add(playerThree);
 		
 		playerFour = new Player("Player Four");
 		players.add(playerFour);
 		
 		map = new Image("data/map.png");
 		
 		input = gc.getInput();
 		input.addMouseListener(this);
 		mouseCoords = new Point();
 
 		//WeaponPickups
 		/*
 		new FlamethrowerPickup(new Vector2f(400, 300));
 		new ShotgunPickup(new Vector2f(500, 300));
 		new ShockwavePickup(new Vector2f(600, 300));
 		new BouncePickup(new Vector2f(700, 300));
 		new SlowspeedyPickup(new Vector2f(800, 300));
 		*/
 		
 		//ObstacleTest
 		new BrownWall(new Vector2f(150, 50), new Vector2f(700, 600));
 		
 		//PowerUpSpawnPoints
 		/*
 		new PowerUpSpawnPoint(new Vector2f(250, 100), 10000, "shield");
 		new PowerUpSpawnPoint(new Vector2f(250, 500), 10000, "speed");
 		new PowerUpSpawnPoint(new Vector2f(500, 100), 10000, "damage");
 		new PowerUpSpawnPoint(new Vector2f(500, 250), 10000, "firerate");
 		new PowerUpSpawnPoint(new Vector2f(750, 500), 10000, "health");
 		*/
 		
 		lifebar = new Lifebar((GameController.SCREEN_WIDTH/2)-100, 10, 200, 25);
 		
 		//TankSpawnPoints
 		TankSpawnPoint tsp = new TankSpawnPoint(new Vector2f(100, 100));
 		tsp.setRotation(315);
 		tsp = new TankSpawnPoint(new Vector2f(900, 100));
 		tsp.setRotation(45);
 		tsp = new TankSpawnPoint(new Vector2f(100, 650));
 		tsp.setRotation(225);
 		tsp = new TankSpawnPoint(new Vector2f(900, 650));
 		tsp.setRotation(135);
 
 	//	turretSprite.setCenterOfRotation(playerOne.getTank().getTurret().getTurretCenter().x, playerOne.getTank().getTurret().getTurretCenter().y);
 
 	//	obstacle = new Entity() {};
 	//	obstacle.setShape(new Rectangle(75, 250, 40, 40));
 		
 		//Start a new round
 		controller.getGameConditions().newRoundDelayTimer(3000);
 	}
 	
 	@Override
 	public void update(GameContainer gc, StateBasedGame sbg, int delta)
 			throws SlickException {
 		if(input.isKeyDown(Input.KEY_W)){
 			if(playerOne.getTank() != null)
 				playerOne.getTank().accelerate(delta);
 		} else if (input.isKeyDown(Input.KEY_S)){
 			if(playerOne.getTank() != null)
 			playerOne.getTank().reverse(delta);
 		} else {
 			if(playerOne.getTank() != null)
 			playerOne.getTank().friction(delta);
 		}
 
 		if(input.isKeyDown(Input.KEY_A) && !input.isKeyDown(Input.KEY_D)){
 			if(input.isKeyDown(Input.KEY_S)){
 				if(playerOne.getTank() != null)
 				playerOne.getTank().turnRight(delta);
 			} else {
 				if(playerOne.getTank() != null)
 				playerOne.getTank().turnLeft(delta);
 			}
 		}
 
 		if(input.isKeyDown(Input.KEY_D) && !input.isKeyDown(Input.KEY_A)){
 			if(input.isKeyDown(Input.KEY_S)){
 				if(playerOne.getTank() != null)
 				playerOne.getTank().turnLeft(delta);
 			} else {
 				if(playerOne.getTank() != null)
 				playerOne.getTank().turnRight(delta);
 			}
 		}
 
 		if(input.isMouseButtonDown(0)){
 			if(playerOne.getTank() != null)
 				playerOne.getTank().fireWeapon(delta);
 			if(playerTwo.getTank() != null)
 				playerTwo.getTank().fireWeapon(delta);
 		}
 		
 		if(input.isKeyDown(Input.KEY_Q)){
 			new SlowspeedyPickup(new Vector2f(800, 300));
 		}
 		
 		if(input.isKeyDown(Input.KEY_UP)){
 			float tmp = controller.getSoundHandler().getVolume();
 			if(tmp < 1){
 				tmp+=0.1;
 				controller.getSoundHandler().setVolume(tmp);
 			}
 		}
 		if(input.isKeyDown(Input.KEY_DOWN)){
 			float tmp = controller.getSoundHandler().getVolume();
 			if(tmp >= 0.1){
 				tmp-=0.1f;
 			}else if(tmp == 0.1f){
 				tmp = 0;
 			}
 			controller.getSoundHandler().setVolume(tmp);
 		}
 		
 		//Go back to the menu
 		if(input.isKeyDown(Input.KEY_M) && input.isKeyDown(Input.KEY_LSHIFT)){
 			sbg.enterState(0);
 		}
 		
 		//Weapons
 		/*
 		if(input.isKeyDown(Input.KEY_1)){
 			if(playerOne.getTank() != null)
 				playerOne.getTank().setTurret(new DefaultTurret(playerOne.getTank()));
 		}
 		
 		if(input.isKeyDown(Input.KEY_2)){
 			if(playerOne.getTank() != null)
 				playerOne.getTank().setTurret(new FlamethrowerTurret(playerOne.getTank()));
 		}
 		if(input.isKeyDown(Input.KEY_3)){
 			if(playerOne.getTank() != null)
 				playerOne.getTank().setTurret(new ShotgunTurret(playerOne.getTank()));
 		}
 		if(input.isKeyDown(Input.KEY_4)){
 			if(playerOne.getTank() != null)
 				playerOne.getTank().setTurret(new SlowspeedyTurret(playerOne.getTank()));
 		}
 		if(input.isKeyDown(Input.KEY_5)){
 			if(playerOne.getTank() != null)
 				playerOne.getTank().setTurret(new ShockwaveTurret(playerOne.getTank()));
 		}
 		if(input.isKeyDown(Input.KEY_6)){
 			if(playerOne.getTank() != null)
 				playerOne.getTank().setTurret(new BounceTurret(playerOne.getTank()));
 		}
 		*/
 		if(input.isKeyDown(Input.KEY_ESCAPE)){
 			gc.exit();
 		}
 		
 		//Update for tankspawner
 		controller.getWorld().getTankSpawner().update(delta);
 		
 		controller.getWorld().getSpawner().update(delta);
 		
 		//Update for getGameConditions()
 		controller.getGameConditions().update(delta);
 		
 		Iterator<Entry<Integer, Entity>> iterator = controller.getWorld().getEntities().entrySet().iterator();
 		while(iterator.hasNext()){
 			Map.Entry<Integer, Entity> entry = (Entry<Integer, Entity>) iterator.next();
 			Entity entity = entry.getValue();
 			
 			entity.update(delta);
 			
 			if(entity instanceof MovableEntity || entity instanceof AbstractSpawnPoint)
 				controller.getWorld().checkCollisionsFor(entity);
 		}
 	}
 	
 	@Override
 	public void render(GameContainer container, StateBasedGame sbg, Graphics g) throws SlickException {	
 		map.draw();
 		
 		// Render the entities in three layers, bottom, middle and top
 		ArrayList<Entity> firstLayerEnts = new ArrayList<Entity>();
 		ArrayList<Entity> secondLayerEnts = new ArrayList<Entity>();
 		ArrayList<Entity> thirdLayerEnts = new ArrayList<Entity>();
 		ArrayList<Entity> fourthLayerEnts = new ArrayList<Entity>();
 
 		Iterator<Entry<Integer, Entity>> iterator = controller.getWorld().getEntities().entrySet().iterator();
 		while(iterator.hasNext()){
 			Map.Entry<Integer, Entity> entry = (Entry<Integer, Entity>) iterator.next();
 			Entity entity = entry.getValue();
 			
 			if(!entity.getSpriteID().equals("")){
 				if(entity.getRenderLayer() == GameController.RenderLayer.FIRST)
 					firstLayerEnts.add(entity);
 				else if(entity.getRenderLayer() == GameController.RenderLayer.SECOND)
 					secondLayerEnts.add(entity);
 				else if(entity.getRenderLayer() == GameController.RenderLayer.THIRD)
 					thirdLayerEnts.add(entity);
 				else if(entity.getRenderLayer() == GameController.RenderLayer.FOURTH)
 					fourthLayerEnts.add(entity);
 			}
 		}
 		
 		renderEntities(firstLayerEnts);
 		renderEntities(secondLayerEnts);
 		renderEntities(thirdLayerEnts);
 		renderEntities(fourthLayerEnts);
 		
 		//Cool timer
 		if(controller.getGameConditions().isDelaying()){
 			if(controller.getGameConditions().getDelayTimer() > 0)
 				g.drawString("Round starts in: " + 
 			(controller.getGameConditions().getDelayTimer()/1000 + 1) + " seconds!", 500, 400);
 		}
 		
 		if(controller.getGameConditions().isGameOver()){
 			g.drawString("Game Over!", 500, 300);
 			g.drawString("Winner: " + controller.getGameConditions().getWinningPlayer().getName(), 500, 400);
 			int i = 0;
 			for(Player p : controller.getGameConditions().getPlayers()){
 				i++;
 				g.drawString(p.getName() + "'s score: " + p.getScore(), 500, (450+(i*25)));
 			}
 		}
 		
 		controller.getAnimationHandler().renderAnimations();
 		debugRender(g);
 		if(playerOne.getTank() != null){
 			if(playerOne.getTank().getShield() != null && playerOne.getTank().getShield().getHealth() <= 100){
 				lifebar.render(playerOne.getTank().getHealth()/playerOne.getTank().getMaxHealth(), playerOne.getTank().getShield().getHealth()/playerOne.getTank().getMaxShieldHealth(), g);
 			}else{
 				lifebar.render(playerOne.getTank().getHealth()/playerOne.getTank().getMaxHealth(), 0, g);
 			}
 		}
 	}
 
 	private void renderEntities(ArrayList<Entity> entities){
 		
 		for(Entity entity : entities){
 			entSprite = GameController.getInstance().getImageHandler().getSprite(entity.getSpriteID());
 			
 			if(entSprite != null){
 				if(entity instanceof AbstractTank){
 					entSprite = GameController.getInstance().getImageHandler().getSprite(entity.getSpriteID());
 					if(entity.getRotation()!=0){
 							entSprite.setRotation((float) entity.getRotation());
 							// draw sprite at the coordinates of the top left corner of tank when it is not rotated
 							Shape nonRotatedShape = entity.getShape().transform(Transform.createRotateTransform((float)Math.toRadians(-entity.getRotation()), entity.getPosition().x, entity.getPosition().y));
 							entSprite.draw(nonRotatedShape.getMinX(), nonRotatedShape.getMinY());
 					} else {
 						entSprite.draw(entity.getShape().getMinX(), entity.getShape().getMinY());
 					}
 				} else {
 					if(entity instanceof AbstractTurret){
 						entSprite.setCenterOfRotation(((AbstractTurret) entity).getTurretCenter().x, ((AbstractTurret) entity).getTurretCenter().y);
 					}
 					entSprite.setRotation((float) entity.getRotation());
 					entSprite.draw(entity.getSpritePosition().x, entity.getSpritePosition().y);						
 				}
 			}
 		}
 	}
 	
 	@Override
 	public int getID() {
 		return 1;
 	}
 	
 	public void debugRender(Graphics g){
 		g.setColor(Color.black);
 		g.drawString("Volume: " + ((int)(controller.getSoundHandler().getVolume() * 100)) + " %",  10, 50);
 		/*g.setColor(Color.black);
 		g.drawString("tankPosX:   " + playerOne.getTank().getPosition().x,  10, 30);
 		g.drawString("tankPosY:   " + playerOne.getTank().getPosition().y,  10, 50);
 		g.drawString("tankAng:    " + playerOne.getTank().getRotation(),	10, 70);
 //		g.drawString("tankImgAng: " + (tankSprite.getRotation()),			10, 90);
 
 		g.drawString("turPosX:   " + playerOne.getTank().getTurret().getPosition().x, 300, 30);
 		g.drawString("turPosY:   " + playerOne.getTank().getTurret().getPosition().y, 300, 50);
 		g.drawString("turAng:    " + playerOne.getTank().getTurret().getRotation(),	  300, 70);
 //		g.drawString("turImgAng: " + turretSprite.getRotation(),		 			  300, 90);
 
 		g.drawString("mouseX: " + mouseCoords.x, 530, 30);
 		g.drawString("mouseY: " + mouseCoords.y, 530, 50);
 
 		g.drawString("speed: " + Double.toString(playerOne.getTank().getSpeed()), 530, 90);
 		g.drawString("projs: " + playerOne.getTank().getProjectiles().size(), 530, 130);
 	
 	//	g.setColor(Color.blue);
 	//	g.draw(playerOne.getTank().getShape());
 
 		if(!playerOne.getTank().getProjectiles().isEmpty()){
 			g.drawString("projPos: "+playerOne.getTank()
 				.getProjectiles().get(0).getPosition().x+" , "+playerOne.getTank()
 				.getProjectiles().get(0).getPosition().y, 530, 110);
 		}*/
 	}
 
 	public void mouseMoved(int oldx, int oldy, int newx, int newy){
 		controller.setMouseCoordinates(newx, newy);
 	}
 	
 	public void mouseDragged(int oldx, int oldy, int newx, int newy){
 		controller.setMouseCoordinates(newx, newy);
 	}
 	
 }
