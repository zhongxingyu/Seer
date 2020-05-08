 package chalmers.TDA367.B17.states;
 
 import java.awt.Dimension;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.newdawn.slick.*;
 import org.newdawn.slick.geom.*;
 import org.newdawn.slick.gui.*;
 import org.newdawn.slick.state.*;
 
 import chalmers.TDA367.B17.*;
 import chalmers.TDA367.B17.controller.*;
 import chalmers.TDA367.B17.controller.GameController.GameSettings;
 import chalmers.TDA367.B17.event.*;
import chalmers.TDA367.B17.gamemodes.KingOfTheHillMode;
 import chalmers.TDA367.B17.model.*;
 import chalmers.TDA367.B17.model.Entity.RenderLayer;
 import chalmers.TDA367.B17.network.Network.*;
 import chalmers.TDA367.B17.powerups.*;
 import chalmers.TDA367.B17.powerups.pickups.*;
 import chalmers.TDA367.B17.resource.*;
 import chalmers.TDA367.B17.resource.SoundHandler.*;
 import chalmers.TDA367.B17.tanks.*;
 import chalmers.TDA367.B17.view.*;
 import chalmers.TDA367.B17.view.Console.*;
 import chalmers.TDA367.B17.weapons.*;
 import chalmers.TDA367.B17.weapons.pickups.*;
 
 import com.esotericsoftware.kryonet.*;
 import com.esotericsoftware.minlog.Log;
 
 public class ClientState extends TanskState {
 	
 	private static ClientState instance;
 	private Client client;
 	private boolean isConnected;
 	private Input input;
 	private Image map = null;
 	private SpriteSheet entSprite;
 	private String playerName;
 	private boolean mapLoadAttempted;
 	private Lifebar lifebar;
 	private Scoreboard scoreboard;
 	private SoundSwitch soundSwitch;
 	protected TextField chatField;
 	private ArrayList<Player> playerList;
 	private Player localPlayer;
 	private int uniqueIdentifier;
 	private GameSettings gameSettings;
 	private boolean gameDelaying;	
 	private boolean gameOver;
 	private int delayTimer;
 	private boolean showInfo;
 		
 	private ClientState(int state) {
 		super(state);
 	}
 	
 	public static ClientState getInstance(){
 		if(instance == null)
 			instance = new ClientState(Tansk.CLIENT);
 	
 		return instance;
 	}
 
 	public void setClient(final Client client){
 		this.client = client;
 
 		this.client.addListener(new Listener(){
 			@Override
 			public void connected(Connection connection) {
 				Pck0_JoinRequest pck = new Pck0_JoinRequest();
 				uniqueIdentifier = (int) (Math.random()*1000*Math.random());
 				pck.unique = uniqueIdentifier;
 				pck.playerName = GameController.getInstance().getPlayerName();;
 				client.sendTCP(pck);
 			}
 
 			public void received(Connection con, Object msg) {
 				super.received(con, msg);
 				if (msg instanceof Packet) {
 					Packet packet = (Packet)msg;
 					packet.setConnection(con);
 					packetQueue.add(packet);
 					packetsReceived++;
 				}
 			}
 
 			@Override
 			public void disconnected(Connection connection) {
 				GameController.getInstance().getConsole().addMsg("Disconnected from server.", MsgLevel.ERROR);
 				GameController.getInstance().getConsole().setTimerHide(false);
 			}
 		});
 	}
 	
 	@Override
     public void init(GameContainer gc, StateBasedGame game) throws SlickException {
 		super.init(gc, game);
 		
 		map = new Image(Tansk.IMAGES_FOLDER + "/map.png");
     }
 	
 	@Override
 	public void enter(GameContainer gc, StateBasedGame game) throws SlickException {
 		super.enter(gc, game);
 		
 		gameOver = false;
 		showInfo = true;
 		gameSettings = new GameSettings();
 		playerList = new ArrayList<Player>();
 		mapLoadAttempted = false;
 		playerName = GameController.getInstance().getPlayerName();
 		
 		controller.getSoundHandler().stopAllMusic();
 		chatField = new TextField(gc, gc.getDefaultFont(), 10, 733, 450, 23);
 		Console console = new Console(10, 533, 450, 192, Color.black, OutputLevel.ALL);
 		console.setBorder(false);
 		console.setTimerHide(true);
 		controller.setConsole(console);
 
 		scoreboard = new Scoreboard(false, playerList);
 		lifebar = new Lifebar((Tansk.SCREEN_WIDTH/2)-100, 10);
 		controller.setWorld(new World(new Dimension(Tansk.SCREEN_WIDTH, Tansk.SCREEN_HEIGHT), false));
 		soundSwitch = new SoundSwitch(Tansk.SCREEN_WIDTH-40, 10);
 
 		client.start();
 		
 		input = gc.getInput();
 		input.addMouseListener(this);
 	}
 	
 	@Override
 	public void leave(GameContainer gc, StateBasedGame sbg) throws SlickException {
 		super.leave(gc, sbg);
 		isConnected = false;
 		client.close();
 	}	
 	
 	@Override
     public void update(GameContainer gc, StateBasedGame game, int delta) throws SlickException {
 		super.update(gc, game, delta);
 		delayTimer -= delta;
 		if((isConnected) && (!mapLoadAttempted)){
 			mapLoadAttempted = true;
 			if(!MapLoader.createEntities("map_standard")){
 				client.close();
 			}
 			controller.getSoundHandler().playMusic(MusicType.BATTLE_MUSIC);
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
 		if(input.isKeyPressed(Input.KEY_S) && input.isKeyDown(Input.KEY_LCONTROL)){
 			if(controller.getSoundHandler().isSoundOn()){
 				soundSwitch.turnSoundOff(controller.getSoundHandler().getVolume());
 			}else{
 				soundSwitch.turnSoundOn();
 			}
 		}
 		
 		GameController.getInstance().getConsole().update(delta);
 		processPackets();	
 		if(!gameOver){
 			sendClientInput(gc.getInput());
 		} else {
 			scoreboard.update(gc);
 			if(scoreboard.getCurrentPressedButton() == Scoreboard.MENU_BUTTON){
 				game.enterState(Tansk.MENU);
 			} 
 		}
 	}
 
 	@Override
     public void render(GameContainer container, StateBasedGame game, Graphics g) throws SlickException {
 		if(isConnected){
 			g.drawImage(map, 0, 0);
 			if(controller.getWorld().getEntities() != null){
 				ArrayList<Entity> firstLayerEnts = new ArrayList<Entity>();
 				ArrayList<Entity> secondLayerEnts = new ArrayList<Entity>();
 				ArrayList<Entity> thirdLayerEnts = new ArrayList<Entity>();
 				ArrayList<Entity> fourthLayerEnts = new ArrayList<Entity>();
 	
 				Iterator<Entry<Integer, Entity>> iterator = controller.getWorld().getEntities().entrySet().iterator();
 				while(iterator.hasNext()){
 					Map.Entry<Integer, Entity> entry = (Entry<Integer, Entity>) iterator.next();
 					Entity entity = entry.getValue();
 					
 					if(!entity.getSpriteID().equals("")){
 						if(entity.getRenderLayer() == RenderLayer.FIRST)
 							firstLayerEnts.add(entity);
 						else if(entity.getRenderLayer() == RenderLayer.SECOND)
 							secondLayerEnts.add(entity);
 						else if(entity.getRenderLayer() == RenderLayer.THIRD)
 							thirdLayerEnts.add(entity);
 						else if(entity.getRenderLayer() == RenderLayer.FOURTH)
 							fourthLayerEnts.add(entity);
 					}
 				}
 				renderEntities(firstLayerEnts);
 				renderEntities(secondLayerEnts);
 				renderEntities(thirdLayerEnts);
 				renderEntities(fourthLayerEnts);
 			}
 		}
 		controller.getAnimationHandler().renderAnimations();
 		renderGUI(container, g);
     }
 		
 	@Override
 	public void renderGUI(GameContainer gc, Graphics g){
 		super.renderGUI(gc, g);
 		
 		g.drawRect(chatField.getX(), chatField.getY(), chatField.getWidth(), chatField.getHeight());
 		chatField.render(gc, g);
 		
 		//Cool timer
 		if(gameDelaying){
 			if(delayTimer > 0){
 				g.setColor(Color.black);
 				g.drawString("Round starts in: ", 440, 75);
 				g.drawString((delayTimer/1000 + 1) + " seconds!", 465, 95);
 				g.setColor(Color.white);
 				if(delayTimer < 3000)
 					showInfo = false;
 			} else {
 				gameDelaying = false;
 			}
 		}
 		
 		if(localPlayer != null){
 			if(showInfo && client.isConnected()){
 				g.setColor(Color.black);
 				g.drawString("Waiting for host to start the game...", 350, 175);
 				g.drawString("You are:", 450, 195);
 				g.setColor(localPlayer.getColor());
 				g.drawString(localPlayer.getColorAsString().toUpperCase(), 528, 195);
 				
 				g.setColor(Color.black);
 				if(gameSettings.gameMode.equals("koth")){
 					g.drawString("KING OF THE HILL", 435, 235);
 					g.drawString("Score points by being in the zone in the center!", 295, 255);
 				} else if(gameSettings.gameMode.equals("standard")){
 					g.drawString("STANDARD", 465, 235);
 					g.drawString("The last one alive gets the most points!", 340, 255);
 				}
 				
 				g.drawString("Scorelimit: " + gameSettings.scorelimit, 445, 315);
 				g.drawString("Rounds: " + gameSettings.rounds, 445, 335);
 				g.drawString("Player lives: " + gameSettings.playerLives, 445, 355);
 				g.drawString("Spawn time: " + gameSettings.spawnTime/1000, 445, 375);
 				g.drawString("Round time: " + gameSettings.roundTime/1000, 445, 395);
 				g.drawString("Game time: " + gameSettings.gameTime/1000, 445, 415);
 
 
 				g.drawString("Players:", 445, 455);
 				int tempY = 475;
 				for(Player player : playerList){
 					g.setColor(player.getColor());
 					g.drawString(player.getName(), 445, tempY);
 					tempY += 20;
 				}
 
 				g.setColor(Color.black);
 				g.drawString("Press 'Enter' to chat...", 400, 575);
 				g.setColor(Color.white);
 			}
 			
 			if(gameSettings.gameMode.equals("koth")){
 				Vector2f tmpPosition = new Vector2f(512, 384);
 //	TODO						((KingOfTheHillMode)controller.getGameMode()).getZone().getPosition();
 				g.setColor(Color.green);
 				g.fillRoundRect(tmpPosition.x-42, tmpPosition.y-60, 75*
 						((float)localPlayer.getScore()/(float)gameSettings.scorelimit), 10, 10);
 				g.setColor(Color.black);
 				g.drawRoundRect(tmpPosition.x-42, tmpPosition.y-60, 75, 10, 10);
 			}
 			
 			if(localPlayer.getTank() != null){
 				if(localPlayer.getTank().getShield() != null && localPlayer.getTank().getShield().getHealth() <= 100){
 					lifebar.render(localPlayer.getTank().getHealth()/AbstractTank.MAX_HEALTH, localPlayer.getTank().getShield().getHealth()/AbstractTank.MAX_SHIELD_HEALTH, g);
 				}else{
 					lifebar.render(localPlayer.getTank().getHealth()/AbstractTank.MAX_HEALTH, 0, g);
 				}
 			}
 		}
 		
 		if(gameOver){
 			scoreboard.render(g);
 		}
 		
 		soundSwitch.render(g);
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
 	
 	private void sendClientInput(Input input) {
 		if(isConnected){
 			if(localPlayer.getTank() != null){
 				Pck4_ClientInput clientPck = new Pck4_ClientInput();
 				clientPck.W_pressed = input.isKeyDown(Input.KEY_W);
 				clientPck.A_pressed = input.isKeyDown(Input.KEY_A);
 				clientPck.S_pressed = input.isKeyDown(Input.KEY_S);
 				clientPck.D_pressed = input.isKeyDown(Input.KEY_D);
 				clientPck.LMB_pressed = input.isMouseButtonDown(0);
 
 				AbstractTurret playerTurret = localPlayer.getTank().getTurret();
 				clientPck.turretNewAngle = (float) Math.toDegrees(Math.atan2(playerTurret.getPosition().x - input.getMouseX() + 0, playerTurret.getPosition().y - input.getMouseY() + 0)* -1)+180;		
 			
 				client.sendTCP(clientPck);
 				packetsSent++;
 			}
 		}
     }
 
 	public void processPackets() {
 		Packet packet;
 		while ((packet = packetQueue.poll()) != null) {
 			if(packet instanceof Pck1_JoinAnswer) {
 				if(((Pck1_JoinAnswer) packet).accepted){
 					Pck2_ClientConfirmJoin pck = new Pck2_ClientConfirmJoin();
 					pck.message = playerName + " connected!";
 					packet.getConnection().sendTCP(pck);
 					isConnected = true;
 					controller.getConsole().addMsg("Joined game!", MsgLevel.INFO);
 					
 					for(Pck6_CreatePlayer oldPlayerPck : ((Pck1_JoinAnswer) packet).oldPlayers){
 						createPlayer(oldPlayerPck);
 					}
 					
 					GameSettings newGameSettings = ((Pck1_JoinAnswer) packet).gameSettings;
 					this.gameSettings.gameMode = newGameSettings.gameMode;
 					this.gameSettings.gameTime = newGameSettings.gameTime;
 					this.gameSettings.playerLives = newGameSettings.playerLives;
 					this.gameSettings.rounds = newGameSettings.rounds;
 					this.gameSettings.roundTime = newGameSettings.roundTime;
 					this.gameSettings.scorelimit = newGameSettings.scorelimit;
 					this.gameSettings.spawnTime = newGameSettings.spawnTime;
 					
 				} else {
 					GameController.getInstance().getConsole().addMsg("Connection refused by server.", MsgLevel.ERROR);
 					GameController.getInstance().getConsole().addMsg("Reason: " + ((Pck1_JoinAnswer) packet).reason, MsgLevel.ERROR);
 					GameController.getInstance().getConsole().setTimerHide(false);
 					packet.getConnection().close();
 				}
 			}
 						
 			if(packet instanceof Pck3_Message){
 				if(packet instanceof Pck3_1_ChatMessage){
 					GameController.getInstance().getConsole().addMsg(((Pck3_1_ChatMessage)packet).message);
 				} else {
 					String message = ((Pck3_Message) packet).message;
 					GameController.getInstance().getConsole().addMsg(message, MsgLevel.INFO);
 					Log.info(message);
 				}
 			}
 			
 			if(packet instanceof Pck5_PlayerKicked){
 				GameController.getInstance().getConsole().addMsg("You have been kicked from the server.", MsgLevel.INFO);
 				client.close();
 			}
 
 			if(packet instanceof Pck6_CreatePlayer){
 				createPlayer((Pck6_CreatePlayer) packet);
 			}
 			
 			if(packet instanceof Pck8_EntityDestroyed){
 				Pck8_EntityDestroyed pck = (Pck8_EntityDestroyed) packet;
 				controller.getWorld().removeEntity(pck.entityID);
 				if(localPlayer.getTank() != null){
 					if(pck.entityID == localPlayer.getTank().getId()){
 						localPlayer.setTank(null);			
 					}	
 				}
 			}
 			
 			if(packet instanceof Pck9_EntityCreated){
 				createClientEntity((Pck9_EntityCreated) packet);
 			}
 			
 			if(packet instanceof Pck10_TankCreated){
 				Pck10_TankCreated pck = (Pck10_TankCreated) packet;
 				createClientTank(pck.entityID, pck.identifier, pck.owner, pck.direction, pck.color);
 			}
 			
 			if(packet instanceof Pck11_StaticObjectCreated){
 				Pck11_StaticObjectCreated pck = (Pck11_StaticObjectCreated) packet;
 				createStaticObject(pck.entityID, pck.identifier, pck.position);
 			}
 
 			if(packet instanceof Pck12_RemovePlayer){
 				Pck12_RemovePlayer pck = (Pck12_RemovePlayer) packet;
 				Player lostPlayer = null;
 				for(Player player : playerList){
 					if(player.getId() == pck.playerID)
 						lostPlayer = player;
 				}
 				if(lostPlayer != null)
 					playerList.remove(lostPlayer);
 			}
 			
 			if(packet instanceof Pck14_GameDelayStarted){
 				gameDelaying = true;
 				delayTimer = ((Pck14_GameDelayStarted)packet).delayTimer;
 			}
 			
 			if(packet instanceof Pck15_GameOver){
 				gameOver = true;
 			}
 			
 			if(packet instanceof Pck100_WorldState){
 				if(isConnected)
 					updateClientWorld((Pck100_WorldState) packet);
 			}			
 			
 			if(packet instanceof Pck1000_GameEvent){
 				Pck1000_GameEvent evtPck = (Pck1000_GameEvent)packet;
 				GameController.getInstance().getWorld().handleEvent(new GameEvent(evtPck.eventType, evtPck.sourceID, evtPck.eventDesc));
 			}
 		}
     }			
 
 	private void createPlayer(Pck6_CreatePlayer playerPck) {
 		Player newPlayer = new Player(playerPck.name);
 		newPlayer.setId(playerPck.id);
 		newPlayer.setScore(playerPck.score);
 		newPlayer.setLives(playerPck.lives);
 		newPlayer.setActive(playerPck.active);
 		newPlayer.setEliminated(playerPck.eliminated);
 		newPlayer.setColor(playerPck.color);
 		
 		if(playerPck.unique  == uniqueIdentifier){
 			localPlayer = newPlayer;
 		}
 		
 		playerList.add(newPlayer);
     }
 
 	private void updateClientWorld(Pck100_WorldState worldState) {
 		for(Packet pck : worldState.updatePackets){
 			if(pck instanceof Pck102_TankUpdate){
 				Pck102_TankUpdate packet = (Pck102_TankUpdate) pck;
 				AbstractTank tank = (AbstractTank) controller.getWorld().getEntity(packet.entityID);
 				tank.setPosition(packet.tankPosition);
 				tank.setDirection(packet.tankDirection);
 				tank.setHealth(packet.tankHealth);
 				if(tank.getShield() != null){
 					tank.getShield().setHealth(packet.tankShieldHealth);
 					if(packet.shieldPosition != null)
 						tank.getShield().setPosition(packet.shieldPosition);
 				}
 				AbstractTurret turret = tank.getTurret();
 				turret.setPosition(packet.turretPosition);
 				turret.setRotation(packet.turretAngle);
 			}
 			if(pck instanceof Pck103_ProjectileUpdate){
 				Pck103_ProjectileUpdate packet = (Pck103_ProjectileUpdate) pck;
 				AbstractProjectile proj = (AbstractProjectile) controller.getWorld().getEntity(packet.entityID);
 				if(proj != null){
 					proj.setPosition(packet.projPosition);
 					proj.setDirection(packet.projDirection);
 				}
 			}
 			
 			if(pck instanceof Pck13_UpdatePlayer){
 				Pck13_UpdatePlayer packet = (Pck13_UpdatePlayer) pck;
 
 				Player matchedPlayer = null;
 				for(Player player : playerList){
 					if(player.getId() == packet.id)
 						matchedPlayer = player;
 				}
 				
 				if(matchedPlayer != null){
 					matchedPlayer.setId(packet.id);
 					matchedPlayer.setScore(packet.score);
 					matchedPlayer.setLives(packet.lives);
 					matchedPlayer.setActive(packet.active);
 					matchedPlayer.setEliminated(packet.eliminated);
 				}
 			}
 		}
     }
 			
 	@Override
 	public void keyReleased(int key, char c){
 		super.keyReleased(key, c);
 		if(key == Input.KEY_ENTER){
 			if(chatField.hasFocus()){
 				if(!chatField.getText().equals("")){
 					if(client != null){
 						String msg = localPlayer.getName() + ": " + chatField.getText();
 						msg = msg.replace('\n', ' ');
 						if(msg.length() > 39)
 							msg = msg.substring(0, 38);
 						Pck3_1_ChatMessage pck = new Pck3_1_ChatMessage();
 						pck.message = msg;
 						chatField.setText("");
 						client.sendTCP(pck);
 						GameController.getInstance().getConsole().setActive(true);
 					}
 				} else {
 					GameController.getInstance().getConsole().setVisible(false);
 				}
 				chatField.setFocus(false);
 			} else {
 				GameController.getInstance().getConsole().setVisible(true);
 				chatField.setFocus(true);
 			}
 		}
 	}
 		
 	private void createClientTank(int entityID, String identifier, int ownerID, Vector2f direction, String playerColor) {
 	    if(identifier.equals("DefaultTank")){
 	    	DefaultTank newTank = new DefaultTank(entityID, direction, null, playerColor);
 	    	if(ownerID == localPlayer.getId())
 	    		localPlayer.setTank(newTank);
 	    }
     }
 	
 	private void createStaticObject(int entityID, String identifier, Vector2f position){
 		if(identifier.equals("BouncePickup")){
 			new BouncePickup(entityID, position);
 		} else if(identifier.equals("FlamethrowerPickup")){
 			new FlamethrowerPickup(entityID, position);
 		} else if(identifier.equals("ShockwavePickup")){
 			new ShockwavePickup(entityID, position);
 		} else if(identifier.equals("SlowspeedyPickup")){
 			new SlowspeedyPickup(entityID, position);
 		}  else if(identifier.equals("ShotgunPickup")){
 			new ShotgunPickup(entityID, position);
 		}  else if(identifier.equals("DamagePowerUpPickup")){
 			new DamagePowerUpPickup(entityID, position);
 		}  else if(identifier.equals("FireRatePowerUpPickup")){
 			new FireRatePowerUpPickup(entityID, position);
 		}  else if(identifier.equals("HealthPowerUpPickup")){
 			new HealthPowerUpPickup(entityID, position);
 		}  else if(identifier.equals("ShieldPowerUpPickup")){
 			new ShieldPowerUpPickup(entityID, position);
 		}  else if(identifier.equals("SpeedPowerUpPickup")){
 			new SpeedPowerUpPickup(entityID, position);
 		} else if(identifier.equals("KingOfTheHillZone")){
 			new KingOfTheHillZone(entityID, position);
 		}
 	}
 	
 	private void createClientEntity(Pck9_EntityCreated pck) {
 		if(pck.identifier.equals("DefaultProjectile")){
 			new DefaultProjectile(pck.entityID, null, null);
 	    } else if(pck.identifier.equals("BounceProjectile")){
 			new BounceProjectile(pck.entityID, null, null);
 	    } else if(pck.identifier.equals("FlamethrowerProjectile")){
 			new FlamethrowerProjectile(pck.entityID, null, null);
 	    } else if(pck.identifier.equals("ShockwaveProjectile")){
 			new ShockwaveProjectile(pck.entityID, null, null);
 	    } else if(pck.identifier.equals("ShockwaveSecondaryProjectile")){
 			new ShockwaveSecondaryProjectile(pck.entityID, null, null, null);
 	    } else if(pck.identifier.equals("ShotgunProjectile")){
 			new ShotgunProjectile(pck.entityID, null, null);
 	    } else if(pck.identifier.equals("SlowspeedyProjectile")){
 			new SlowspeedyProjectile(pck.entityID, null, null);
 	    }  else if(pck.identifier.equals("Shield")){
 	    	AbstractTank tank = (AbstractTank)GameController.getInstance().getWorld().getEntity(pck.possibleOwnerID);
 	    	tank.setShield(new Shield(pck.entityID, tank, 0));
 	    } else if(pck.identifier.equals("DefaultTurret")){
 	    	AbstractTank tank = (AbstractTank)GameController.getInstance().getWorld().getEntity(pck.possibleOwnerID);
 	    	if(tank != null){
 	    		double rotation = tank.getTurret().getRotation();
 	    		Vector2f position = tank.getTurret().getPosition();
 	    		AbstractTurret turret = new DefaultTurret(pck.entityID, position, rotation, tank, pck.color);
 	    		tank.setTurret(turret);
 	    	}
 	    } else if(pck.identifier.equals("BounceTurret")){
 	    	AbstractTank tank = (AbstractTank)GameController.getInstance().getWorld().getEntity(pck.possibleOwnerID);
 	    	if(tank != null){
 	    		double rotation = tank.getTurret().getRotation();
 	    		Vector2f position = tank.getTurret().getPosition();
 	    		AbstractTurret turret = new BounceTurret(pck.entityID, position, rotation, tank, tank.getColor());
 	    		tank.setTurret(turret);
 	    	}
 	    } else if(pck.identifier.equals("FlamethrowerTurret")){
 	    	AbstractTank tank = (AbstractTank)GameController.getInstance().getWorld().getEntity(pck.possibleOwnerID);
 	    	if(tank != null){
 	    		double rotation = tank.getTurret().getRotation();
 	    		Vector2f position = tank.getTurret().getPosition();
 	    		AbstractTurret turret = new FlamethrowerTurret(pck.entityID, position, rotation, tank, tank.getColor());
 	    		tank.setTurret(turret);
 	    	}
 	    } else if(pck.identifier.equals("ShockwaveTurret")){
 	    	AbstractTank tank = (AbstractTank)GameController.getInstance().getWorld().getEntity(pck.possibleOwnerID);
 	    	if(tank != null){
 	    		double rotation = tank.getTurret().getRotation();
 	    		Vector2f position = tank.getTurret().getPosition();
 	    		AbstractTurret turret = new ShockwaveTurret(pck.entityID, position, rotation, tank, tank.getColor());
 	    		tank.setTurret(turret);
 	    	}
 	    } else if(pck.identifier.equals("ShotgunTurret")){
 	    	AbstractTank tank = (AbstractTank)GameController.getInstance().getWorld().getEntity(pck.possibleOwnerID);
 	    	if(tank != null){
 	    		double rotation = tank.getTurret().getRotation();
 	    		Vector2f position = tank.getTurret().getPosition();
 	    		AbstractTurret turret = new ShotgunTurret(pck.entityID, position, rotation, tank, tank.getColor());
 	    		tank.setTurret(turret);
 	    	}
 	    } else if(pck.identifier.equals("SlowspeedyTurret")){
 	    	AbstractTank tank = (AbstractTank)GameController.getInstance().getWorld().getEntity(pck.possibleOwnerID);
 	    	if(tank != null){
 	    		double rotation = tank.getTurret().getRotation();
 	    		Vector2f position = tank.getTurret().getPosition();
 	    		AbstractTurret turret = new SlowspeedyTurret(pck.entityID, position, rotation, tank, tank.getColor());
 	    		tank.setTurret(turret);
 	    	}
 	    } 
     }
 }
