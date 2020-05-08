 package jig.ironLegends.screens;
 
 import java.util.List;
 
 import jig.engine.Mouse;
 import jig.engine.RenderingContext;
 import jig.engine.ViewableLayer;
 import jig.engine.physics.Body;
 import jig.engine.util.Vector2D;
 import jig.ironLegends.Bullet;
 import jig.ironLegends.CommandState;
 import jig.ironLegends.Destructible;
 import jig.ironLegends.IronLegends;
 import jig.ironLegends.Obstacle;
 import jig.ironLegends.PowerUp;
 import jig.ironLegends.Tank;
 import jig.ironLegends.collision.Handler_CPBLayer_BodyLayer;
 import jig.ironLegends.collision.Handler_CPBLayer_CPBLayer;
 import jig.ironLegends.collision.ISink_CPB_Body;
 import jig.ironLegends.collision.ISink_CPB_CPB;
 import jig.ironLegends.collision.Sink_CPB_CPB_Default;
 import jig.ironLegends.core.ConvexPolyBody;
 import jig.ironLegends.core.GameScreen;
 import jig.ironLegends.core.KeyCommands;
 
 public class GamePlay_GS extends GameScreen {
 	IronLegends game;
 	
 	public GamePlay_GS(int name, IronLegends gm) {
 		super(name);
 		this.game = gm;
 
 		addViewableLayer(game.m_bgLayer);
 		addViewableLayer(game.m_tankObstacleLayer);
 		addViewableLayer(game.m_tankBulletObstacleLayer);
 		addViewableLayer(game.m_powerUpLayer);
 		addViewableLayer(game.m_tankLayer);
 		addViewableLayer(game.m_bulletLayer);
 		addViewableLayer(game.m_hudLayer);
 	}
 
 	@Override
 	public void activate(int prevScreen) {
 		game.newGame();
 	}
 
 	@Override
 	public void deactivate()
 	{
 		game.m_server = null;
 		game.m_client = null;
 	}
 	
 	@Override
 	public void populateLayers(List<ViewableLayer> gameObjectLayers) {
 		super.populateLayers(gameObjectLayers);
 
 		// part of single player "start game" - based on map and user selections
 		// for multiplayer, tank creation all happens on the server as well, each player
 		// will have to be assigned a unique entityNumber
 		{
 			game.m_tank = null;
 			// Main player
 			int entityNumber = 0;
 			game.m_tank = new Tank(game, 0, Tank.Team.WHITE, new Vector2D(100, 250), entityNumber);
 			entityNumber++;
 			
 			// set position of tank to one of "bluespawn" locations/orientations
 			game.setSpawn(game.m_tank, "bluespawn");
 
 			game.m_gameProgress.setSelf(game.m_tank);
 			game.m_tankLayer.clear();
 			game.m_tankLayer.add(game.m_tank);
 			game.m_entityLayer.add(game.m_tank);
 			
 			// Temporary: add random 10 AI tanks
 			while (game.m_tankLayer.size() < 5) {
 				game.addAITank(entityNumber);
 				entityNumber++;
 			}
 		}
 		
 		// objects for both single and multiplayer game
 		{
 			game.m_hudLayer.clear();
 			game.m_hudLayer.add(game.m_radarHUD);
 			game.m_hudLayer.add(game.m_powerUpHUD);
 			game.m_hudLayer.add(game.m_lifeHUD);
 			
 			
 		}
 		game.m_gameProgress.m_levelProgress.setTanksToDestroy(game.m_numTanks);
 		game.m_gameProgress.m_levelProgress.setTanksDestroyed(0);
 		game.m_gameProgress.setBasesRemaining(2);
 		
 		game.m_physicsEngine
 				.manageViewableSet(game.m_tankLayer);
 		game.m_physicsEngine
 				.manageViewableSet(game.m_bulletLayer);
 
 		// Register Collision Handlers
 		registerCollisionHandles();
 		
 
 	}
 
 
 	private void registerCollisionHandles() {
 		// Tank to Tank
 		ISink_CPB_CPB htanktank = new ISink_CPB_CPB() {
 			@Override
 			public boolean onCollision(ConvexPolyBody main,
 					ConvexPolyBody other, Vector2D vCorrection) {
 				if (!main.equals(other)) {
 					Tank t = (Tank) main;
 					t.setPosition(t.getPosition().translate(vCorrection));
 					t.stopMoving();
 					t.stopTurning();
 				}
 				return false;
 			}
 		};
 		game.m_physicsEngine
 				.registerCollisionHandler(new Handler_CPBLayer_CPBLayer(
 						game.m_tankLayer,
 						game.m_tankLayer, htanktank));
 
 		// Bullets and Tanks
 		ISink_CPB_Body htankbull = new ISink_CPB_Body() {
 			@Override
 			public boolean onCollision(ConvexPolyBody main, Body other,
 					Vector2D vCorrection) {
 				Tank t = (Tank) main;
 				Bullet b = (Bullet) other;
 				Tank bo = (Tank) b.getOwner();
 				if (!t.equals(bo)) { // not killing self
 					if (t.getTeam() != bo.getTeam()) { // don't damage team mate
 						
 						if (!(game.m_godmode && t.isPlayerControlled())) {
 							game.m_soundFx.play("bulletHitTank");
 							t.causeDamage(b.getDamage());
 						}
 						
 						bo.addPoints(b.getDamage());
 						if (t.getHealth() <= 0)
 						{
 							game.m_gameProgress.tankDestroyed(t);
 							if (t.isPlayerControlled()) {
 								// TODO? if death match vs capture do different behavior
 								// in death match if only single tank remaining game over, winner is last tank
 								// in capture base mode, tank death->respawn
 								
 							} else {								
 								if (game.m_levelProgress.getTanksRemaining() > 0) {
									game.addPowerUp(t);
									
 									// add new AI Tank
									//game.addAITank(game.m_tankLayer.size());
 								}								
 							}
 						}		
 					}
 					
 					b.setActivation(false);
 					return true;
 				}
 
 				return false;
 			}
 		};
 
 		game.m_physicsEngine
 				.registerCollisionHandler(new Handler_CPBLayer_BodyLayer(
 						game.m_polygonFactory,
 						game.m_tankLayer,
 						game.m_bulletLayer, 4, 27, htankbull));
 
 		// Tanks & Destroyable
 		game.m_physicsEngine
 				.registerCollisionHandler(new Handler_CPBLayer_CPBLayer(
 						game.m_tankLayer,
 						game.m_tankBulletObstacleLayer,
 						new Sink_CPB_CPB_Default()));
 
 		// Bullets & Destroyable
 		ISink_CPB_Body bulldestroyable = new ISink_CPB_Body() {
 			@Override
 			public boolean onCollision(ConvexPolyBody main, Body other,
 					Vector2D vCorrection) {
 				Obstacle o = (Obstacle) main;
 				Bullet b = (Bullet) other;
 
 				Destructible d = o.getDestructible();
 				if (d != null)
 				{
 					if (d.causeDamage(b.getDamage())) {						
 						o.setActivation(false);
 						if (o.name().equals("redbase") || 
 								o.name().equals("bluebase")  )
 						{
 							game.m_gameProgress.baseDestroyed(o);
 						} else if(o.name().equals("crate")) {
 							game.addPowerUp(o);
 						}
 					}
 					
 					game.m_soundFx.play("bulletWall");
 				}
 				
 				Tank bo = (Tank) b.getOwner();
 				bo.addPoints(b.getDamage());
 				b.setActivation(false);
 				return true;
 			}
 		};
 
 		game.m_physicsEngine
 				.registerCollisionHandler(new Handler_CPBLayer_BodyLayer(
 						game.m_polygonFactory,
 						game.m_tankBulletObstacleLayer,
 						game.m_bulletLayer, 4, 27, bulldestroyable));
 
 		// Tanks & Obstacles
 		game.m_physicsEngine
 		.registerCollisionHandler(new Handler_CPBLayer_CPBLayer(
 				game.m_tankLayer,
 				game.m_tankObstacleLayer,
 				new Sink_CPB_CPB_Default()));
 
 		// Bullet & Obstacles
 		ISink_CPB_Body bullobstacles = new ISink_CPB_Body() {
 			@Override
 			public boolean onCollision(ConvexPolyBody main, Body other,
 					Vector2D vCorrection) {
 				Obstacle o = (Obstacle) main;
 				if (!o.isTree()) { // if tree -> let bullet pass thru
 					Bullet b = (Bullet) other;
 					b.setActivation(false);
 				}
 				return true;
 			}
 		};
 
 		game.m_physicsEngine
 				.registerCollisionHandler(new Handler_CPBLayer_BodyLayer(
 						game.m_polygonFactory,
 						game.m_tankObstacleLayer,
 						game.m_bulletLayer, 4, 27, bullobstacles));
 		
 		// Tanks & PowerUps
 		ISink_CPB_Body htankpowerup = new ISink_CPB_Body() {
 			@Override
 			public boolean onCollision(ConvexPolyBody main, Body other,
 					Vector2D vCorrection) {
 				Tank t = (Tank) main;
 				PowerUp p = (PowerUp) other;
 				t.addPoints(p.getPoint());
 				p.executePower(t);
 				p.setActivation(false);
 				return true;
 			}
 		};
 
 		game.m_physicsEngine
 				.registerCollisionHandler(new Handler_CPBLayer_BodyLayer(
 						game.m_polygonFactory,
 						game.m_tankLayer,
 						game.m_powerUpLayer, 36, 36, htankpowerup));		
 	}
 
 	protected void collectPlayerCommands(KeyCommands keyCmds, Mouse mouse, CommandState cs)
 	{
 		if (keyCmds.isPressed("up") || keyCmds.isPressed("w"))
 			cs.setState(CommandState.CMD_UP, true);
 		else
 			cs.setState(CommandState.CMD_UP, false);
 
 
 		if (keyCmds.isPressed("down") || keyCmds.isPressed("s"))
 			cs.setState(CommandState.CMD_DOWN, true);
 		else
 			cs.setState(CommandState.CMD_DOWN, false);
 
 		if (keyCmds.isPressed("left") || keyCmds.isPressed("a"))
 			cs.setState(CommandState.CMD_LEFT, true);
 		else
 			cs.setState(CommandState.CMD_LEFT, false);
 
 		if (keyCmds.isPressed("right") || keyCmds.isPressed("d"))
 			cs.setState(CommandState.CMD_RIGHT, true);
 		else
 			cs.setState(CommandState.CMD_RIGHT, false);
 		
 		if (mouse.isLeftButtonPressed() || keyCmds.isPressed("fire"))
 			cs.setState(CommandState.CMD_FIRE, true);
 		else
 			cs.setState(CommandState.CMD_FIRE, false);
 	}
 	
 	@Override
 	public int processCommands(KeyCommands keyCmds, Mouse mouse,
 			final long deltaMs) {
 		
 		// command state may get modified per tank by client update
 		// for now just suck in the commands here
 		CommandState cs = new CommandState();
 		// client processing
 		{
 			cs.setEntityNumber(game.m_tank.getEntityNumber());
 			cs.setTurretRotationRad(game.m_tank.getTurretRotation());
 
 			collectPlayerCommands(keyCmds, mouse, cs);
 			
 			if (keyCmds.wasPressed("die"))
 			{
 				cs.setState(CommandState.CMD_DIE, true);				
 			}
 			// send to server here?
 		}
 
 		// when server receives the entity command state, it can find the entity and then
 		// update it
 		{
 			Tank t = game.findEntity(cs.getEntityNumber());
 			t.serverControlMovement(keyCmds, mouse, cs);
 			if (cs.isActive(CommandState.CMD_DIE))
 			{
 				// the below should happen when server issues a die cmd
 				game.m_tank.causeDamage(game.m_tank.getHealth());
 				game.m_gameProgress.playerDied();
 			}
 		}
 
 		// this adjusts the tanks turret and handles the "fix" turret request (not currently "sent" to server)
 		game.m_tank.clientControlMovement(keyCmds, mouse);
 		
 		return name();
 	}
 	
 	@Override
 	public void update(long deltaMs) {
 		
 		Vector2D center = game.m_tank.getShapeCenter();
 		game.updateMapCenter(center.clamp(IronLegends.VISIBLE_BOUNDS));
 		
 		if (game.m_levelProgress.isIntro()) {
 			game.m_levelProgress.update(deltaMs);
 		} else if (game.m_levelProgress.isExitActivated()) {
 			game.m_levelProgress.update(deltaMs);
 		} else {
 			game.m_physicsEngine.applyLawsOfPhysics(deltaMs);
 		}
 		game.m_sfx.update(deltaMs);
 		
 		// TODO: check tanks for death? then respawn (team) or adjust life (deathmatch)
 		// if deathmatch (ie single player for now?)
 		//if (game.m_tank.getHealth() < 0)
 		
 		// TODO: Temporary hack to show score
 		game.m_levelProgress.setScore(game.m_tank.getScore());
 		
 		boolean bGameOver = false;
 		
 		if (game.m_gameProgress.getLivesRemaining() < 0) {
 			bGameOver = true;
 		}
 		else if (game.m_levelProgress.getTanksRemaining() <= 0) {
 			bGameOver = true;
 		}
 		else if (game.m_gameProgress.getBasesRemaining() <= 1)
 		{
 			bGameOver = true;
 		}
 		
 		if (bGameOver)
 		{
 			game.m_gameProgress.getLevelProgress().setExit(true);
 
 			int totalScore = game.m_gameProgress.gameOver();
 			if (totalScore > game.m_highScore.getHighScore()) {
 				game.m_highScore.setHighScore(totalScore);
 				game.m_highScore.setPlayer(game.m_playerInfo.getName());
 				game.m_highScorePersist.save(game.m_highScore);
 			}
 			
 			game.screenTransition(name(), IronLegends.GAMEOVER_SCREEN);
 		}
 		game.m_radarHUD.update(deltaMs);
 	}
 	
 	@Override
 	public void render(RenderingContext rc)
 	{
 		super.render(rc);
 
 		rc.setTransform(game.m_mapCalc.getWorldToScreenTransform());
 		game.m_sfx.render(rc);
 	}
 }
