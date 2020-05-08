 package chalmers.TDA367.B17.states;
 
 import java.awt.Dimension;
 import java.io.IOException;
 import java.net.*;
 import java.util.*;
 import java.util.Map.*;
 import java.util.concurrent.ConcurrentLinkedQueue;
 import org.newdawn.slick.*;
 import org.newdawn.slick.geom.Vector2f;
 import org.newdawn.slick.state.*;
 import com.esotericsoftware.kryonet.*;
 import com.esotericsoftware.minlog.Log;
 import chalmers.TDA367.B17.*;
 import chalmers.TDA367.B17.console.*;
 import chalmers.TDA367.B17.console.Console.*;
 import chalmers.TDA367.B17.controller.*;
 import chalmers.TDA367.B17.model.*;
 import chalmers.TDA367.B17.model.Entity.RenderLayer;
 import chalmers.TDA367.B17.network.Network.*;
 import chalmers.TDA367.B17.network.*;
 import chalmers.TDA367.B17.tanks.*;
 
 public class ServerState extends BasicGameState {
 	private int state;
 	private static ServerState instance;
 	private GameController controller;
 	private ImageHandler imgHandler;
 	private ConcurrentLinkedQueue<Packet> packetQueue;
 	
 	private Server server;
 	private GameConditions gameConditions;
 	private ArrayList<Player> playerList;
 	private String ipAddress;
 	
 	private boolean gameStarted = false;
 	private int latestID = 0;
 	
 	private ServerState(int state) {
 	    this.state = state;
 		controller = GameController.getInstance();
 		controller.setConsole(new Console(10, 565, 450, 192, OutputLevel.ALL));
     }
 	
 	public static ServerState getInstance(){
 		if(instance == null)
 			instance = new ServerState(Tansk.SERVER);
 	
 		return instance;
 	}
 	
 	@Override
     public void init(GameContainer gc, StateBasedGame game) throws SlickException {
 		gc.setAlwaysRender(true);
 		gc.setMouseCursor(new Image("data/crosshair.png"), 16, 16);
 		imgHandler = new ImageHandler();
 		
 		gameConditions = new GameConditions();
 		playerList = new ArrayList<Player>();
 		ipAddress = "N/A";
     }
 	
 	@Override
 	public void enter(GameContainer container, StateBasedGame game) throws SlickException {
 		super.enter(container, game);
 		controller.setWorld(new World(new Dimension(Tansk.SCREEN_WIDTH, Tansk.SCREEN_HEIGHT), true));
 		controller.getWorld().init();
 		imgHandler.loadAllImages(Tansk.DATA_FOLDER);
 		MapLoader.createEntities("whatever");
 		
 		Log.set(Log.LEVEL_DEBUG);
 		packetQueue = new ConcurrentLinkedQueue<Packet>();
 		server = new Server();
 		Network.register(server);
 		server.addListener(new Listener(){
 			public void received(Connection con, Object msg){
 			   super.received(con, msg);
 			   if (msg instanceof Packet) {
 				   Packet packet = (Packet)msg;
 				   packet.setConnection(con);
 				   packetQueue.add(packet);
 			   }
 			}
 			
 			@Override
 			public void connected(Connection connection) {
 //				GameController.getInstance().getConsole().addMsg("Connected.", MsgLevel.INFO);
 			}
 			
 			@Override
 			public void disconnected(Connection connection) {
 				if(getPlayer(connection) != null){
 			    	String msg = getPlayer(connection).getName() + " disconnected.";
 					controller.getConsole().addMsg(msg, MsgLevel.INFO);
 			    	Pck3_ServerMessage disconnectedMsg = new Pck3_ServerMessage();
 			    	disconnectedMsg.message = msg;
 			    	server.sendToAllExceptTCP(connection.getID(), disconnectedMsg);
 			    	
 			    	Player lostPlayer = getPlayer(connection);
 			    	controller.getWorld().removeEntity(lostPlayer.getTank().getTurret());
 			    	controller.getWorld().removeEntity(lostPlayer.getTank());
 			    	playerList.remove(lostPlayer);
 					
 			    	Log.info("[SERVER] " + msg);
 				} else {
 					GameController.getInstance().getConsole().addMsg("Player disconnected.", MsgLevel.INFO);
 					Log.info("[SERVER] Unkown player has  disconnected.");
 				}
 	
 				playerList.remove(getPlayer(connection));
 			}
 		});
 		
 		try {
 	        server.bind(Network.PORT, Network.PORT);
 			server.start();	
 			controller.getConsole().addMsg("Server loaded and ready!", MsgLevel.INFO);
         } catch (IOException e) {
     		controller.getConsole().addMsg("Address in use!", MsgLevel.ERROR);
 	        e.printStackTrace();
         }
 		
 		try {
 			ipAddress = InetAddress.getLocalHost().getHostAddress();
         } catch (UnknownHostException e1) {
 	        e1.printStackTrace();
         }
 	}
 	
 	@Override
     public void update(GameContainer gc, StateBasedGame game, int delta) throws SlickException {
 		processPackets();	
 		
 		if(gc.getInput().isKeyPressed(Input.KEY_SPACE)){
 			controller.getConsole().clearMessages();
 			sendMessageToClients("Server says hello!");
 		}
 		
 		if(!gameStarted){
 			Input input = gc.getInput();
 			int x = input.getMouseX();
 			int y = input.getMouseY();
 			if(x > 410 && x < 460 && y > 525 && y < 555){
 				if(input.isMousePressed(0)){
 					startGame();
 					GameController.getInstance().getConsole().addMsg("Game started!", MsgLevel.INFO);
 				}
 			}
 		}
 		
 		if(gameStarted){
 			// Update for tankspawner
 //			controller.getWorld().getTankSpawner().update(delta, playerList);
 			
 			// Update for gameconditions
 //			gameConditions.update(delta);
 
 			for(Player player : playerList){		
 				ArrayList<Boolean> pressedKeys = player.getInput();
 				if(player.getTank() != null){
 					if(pressedKeys.get(Player.INPT_W)){
 						player.getTank().accelerate(delta);
 					} else if (pressedKeys.get(Player.INPT_S)){
 						player.getTank().reverse(delta);
 					} else {
 						player.getTank().friction(delta);
 					}
 					
 					if(pressedKeys.get(Player.INPT_A) && !pressedKeys.get(Player.INPT_D)){
 						if(pressedKeys.get(Player.INPT_S)){
 							player.getTank().turnRight(delta);
 						} else {
 							player.getTank().turnLeft(delta);
 						}
 					}
 					
 					if(pressedKeys.get(Player.INPT_D) && !pressedKeys.get(Player.INPT_A)){
 						if(pressedKeys.get(Player.INPT_S)){
 							player.getTank().turnLeft(delta);
 						} else {
 							player.getTank().turnRight(delta);
 						}
 					}
 				}
 			}
 			
 
 			Pck100_WorldState worldState = new Pck100_WorldState();
 			worldState.updatePackets = new ArrayList<EntityPacket>();
 			
 			Iterator<Entry<Integer, Entity>> updateIterator = controller.getWorld().getEntities().entrySet().iterator();
 			while(updateIterator.hasNext()){
 				Map.Entry<Integer, Entity> entry = (Entry<Integer, Entity>) updateIterator.next();
 				Entity entity = entry.getValue();
 				
 				entity.update(delta);
 				
 				if(entity instanceof MovableEntity)
 					controller.getWorld().checkCollisionsFor((MovableEntity)entity);
 				if(entity instanceof AbstractSpawnPoint)
 					controller.getWorld().checkCollisionsFor(entity);
 				
 				if(entity instanceof AbstractTank){
 					AbstractTank tank = (AbstractTank) entity;
 					Pck101_TankUpdate pck = new Pck101_TankUpdate();
 					pck.entityID = tank.getId();
 					pck.tankPosition = tank.getPosition();
 					pck.tankDirection = tank.getDirection();
 					pck.turretPosition = tank.getTurret().getPosition();
 					pck.turretAngle = (float) tank.getTurret().getRotation();
 					worldState.updatePackets.add(pck);
 				}
 			}			
 			sendToAll(worldState);
 		}
     }
 	
 	// only used for debug
 	private void startGame() {
 		gameStarted = true;
 		int x = 100;
 		for(Player player : playerList){
 			AbstractTank tank = new DefaultTank(generateID(), new Vector2f(0,-1));
 			tank.setPosition(new Vector2f(x, 100));
 			player.setTank(tank);
 	    	
 			Pck7_TankID tankPck = new Pck7_TankID();
 	    	tankPck.tankID = player.getTank().getId();
 	    	player.getConnection().sendTCP(tankPck);
 	    	
 			x+=100;
 		}
 		// Start a new round
 //		gameConditions.init(10, 2, 1, 5000, 500000, 1500000);
 //		gameConditions.newRoundDelayTimer(3000);
     }
 
 	public void processPackets() {
 		Packet packet;
 		while ((packet = packetQueue.poll()) != null) {
 			if(packet instanceof Pck0_JoinRequest){
 		    	Pck0_JoinRequest pck = (Pck0_JoinRequest) packet;
 		    	
 		    	controller.getConsole().addMsg(pck.playerName + " attempting to connect..", MsgLevel.INFO);
 		    	Pck1_LoginAnswer responsePacket = new Pck1_LoginAnswer();
 		    	
//		    	if(!gameStarted){
 			    	Player newPlayer = new Player(packet.getConnection(), pck.playerName);
 			    	newPlayer.setLives(gameConditions.getPlayerLives());
 			    	newPlayer.setRespawnTime(gameConditions.getSpawnTime());
 			    	
 			    	playerList.add(newPlayer);
 			    	responsePacket.accepted = true;
//		    	} else {
//		    		responsePacket.accepted = false;
//		    		responsePacket.reason = "Game started.";
//		    		GameController.getInstance().getConsole().addMsg(pck.playerName + " kicked, game has started.", MsgLevel.INFO);
//		    	}
 			   	packet.getConnection().sendTCP(responsePacket);
 		    }
 		    
 		    if(packet instanceof Pck2_ClientConfirmJoin){
 		    	String msg = getPlayer(packet.getConnection()).getName() + " joined.";
 				controller.getConsole().addMsg(msg, MsgLevel.INFO);
 		    	Pck3_ServerMessage welcomeMsg = new Pck3_ServerMessage();
 		    	welcomeMsg.message = msg;
 		    	server.sendToAllExceptTCP(packet.getConnection().getID(), welcomeMsg);
 				Log.info(msg);
 		    }
 
 		    if(packet instanceof Pck3_ServerMessage){
 				String message = ((Pck3_ServerMessage) packet).message;
 				controller.getConsole().addMsg(message, MsgLevel.INFO);
 				Log.info(message);
 		    }
 		    
 		    if(packet instanceof Pck4_ClientInput){
 		    	if(gameStarted)
 		    		receiveClientInput((Pck4_ClientInput) packet);
  		    }
 		    
 		    if(packet instanceof Pck5_ClientTurretAngle){
 		    	getPlayer(packet.getConnection()).getTank().getTurret().setRotation(((Pck5_ClientTurretAngle) packet).turretNewAngle);
  		    }
 	   	}
     }
 
 	private void receiveClientInput(Pck4_ClientInput pck) {
 		getPlayer(pck.getConnection()).setInputStatus(Player.INPT_W, pck.W_pressed);
 		getPlayer(pck.getConnection()).setInputStatus(Player.INPT_A, pck.A_pressed);
 		getPlayer(pck.getConnection()).setInputStatus(Player.INPT_S, pck.S_pressed);
 		getPlayer(pck.getConnection()).setInputStatus(Player.INPT_D, pck.D_pressed);
     }
 
 	@Override
     public void render(GameContainer container, StateBasedGame game, Graphics g) throws SlickException {
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
 			renderEntities(firstLayerEnts, g);
 			renderEntities(secondLayerEnts, g);
 			renderEntities(thirdLayerEnts, g);
 			renderEntities(fourthLayerEnts, g);
 		}
 		controller.getConsole().renderMessages(g);
 		g.drawString("Players: " + playerList.size(), 18, 500);
 		g.drawString("Entities: " + controller.getWorld().getEntities().size(), 18, 520);
 		g.drawString("LAN IP: " + ipAddress, 18, 545);
 		
 		int y = 390;
 		for(Player player : playerList){
 			g.drawString(player.getMouseCoordinates().x + " " + player.getMouseCoordinates().y, 18, y);
 			y+=20;
 		}
 		
 		if(gameStarted)
 			g.setColor(Color.green);
 		else
 			g.setColor(Color.red);
 		g.drawString("Run!", 415, 530);
 		g.drawRect(410, 525, 50, 30);
 		g.setColor(Color.white);
	
		
     }
 	
 	private void renderEntities(ArrayList<Entity> entities, Graphics g){
 		g.setColor(Color.yellow);
 		for(Entity entity : entities){
 			if(entity instanceof AbstractTurret){
 				g.drawOval(entity.getPosition().x-10, entity.getPosition().y-10, 20, 20);
 				g.drawLine(entity.getPosition().x, entity.getPosition().y, ((AbstractTurret)entity).getTurretNozzle().x, ((AbstractTurret)entity).getTurretNozzle().y);
 			} else {
 				g.draw(entity.getShape());
 			}
 		}
 		g.setColor(Color.white);
 	}
 	
 	public void addPlayer(Player player){
 		playerList.add(player);
 	}	
 	
 	public void sendMessageToClients(String message){
 		Pck3_ServerMessage msg = new Pck3_ServerMessage();
 		msg.message = message;
 		server.sendToAllTCP(msg);
 	}
 	
 	@Override
     public int getID() {
 	    return this.state;
     }
 	
 	public ArrayList<Player> getPlayerList(){
 		return playerList;
 	}
 	
 	public Player getPlayer(Connection con){
 		for(Player player : playerList){
 			if(player.getConnection().getID() == con.getID()){
 				return player;
 			}
 		}
 		return null;
 	}
 
 	public void sendToAll(Packet packet) {
 		if(server != null){
 			for(Player player : playerList){
 				server.sendToTCP(player.getConnection().getID(), packet);
 			}
 		}
     }	
 	
 	public int generateID(){
 		latestID += 1;
 		return latestID;
 	}
 }
