 /**
  * Copyright 2012 Jami Couch
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  * 
  * This project uses:
  * 
  * LibGDX
  * Copyright 2011 see LibGDX AUTHORS file
  * Licensed under Apache License, Version 2.0 (see above).
  * 
  */
 package com.ahsgaming.spacetactics.network;
 
 import java.io.IOException;
import java.net.UnknownHostException;
 import java.util.ArrayList;
 
 import com.ahsgaming.spacetactics.GameController;
 import com.ahsgaming.spacetactics.Player;
 import com.ahsgaming.spacetactics.SpaceTacticsGame;
 import com.ahsgaming.spacetactics.network.KryoCommon.AddAIPlayer;
 import com.ahsgaming.spacetactics.network.KryoCommon.RegisterPlayer;
 import com.ahsgaming.spacetactics.network.KryoCommon.RegisteredPlayer;
 import com.ahsgaming.spacetactics.network.KryoCommon.RemovePlayer;
 import com.ahsgaming.spacetactics.network.KryoCommon.SetupInfo;
 import com.ahsgaming.spacetactics.network.KryoCommon.StartGame;
 import com.ahsgaming.spacetactics.screens.GameSetupScreen.GameSetupConfig;
 import com.badlogic.gdx.Gdx;
 import com.esotericsoftware.kryonet.Client;
 import com.esotericsoftware.kryonet.Connection;
 import com.esotericsoftware.kryonet.Listener;
 
 /**
  * @author jami
  *
  */
 public class GameClient {
 	public String LOG = "GameClient";
 	
 	Client client;
 	String host;
 	Connection clientConn;
 
 	int playerId = -1;
 	Player player = null;
 	
 	GameController controller;
 	int sinceLastNetTick = 0;
 	int sinceLastGameTick = 0;
 	long lastTimeMillis = 0;
 	
 	GameSetupConfig gameConfig;
 	
 	ArrayList<Player> players = new ArrayList<Player>();
 	
 	SpaceTacticsGame game;
 	
 	boolean stopClient = false;
 	
 	boolean isConnecting = false;
 	
 	/**
 	 * 
 	 */
 	public GameClient(SpaceTacticsGame g, final GameSetupConfig cfg) {
 		this.game = g;
 		gameConfig = cfg;
 		
 		client = new Client();
 		client.start();
 		
 		KryoCommon.register(client);
 		
 		client.addListener(new Listener() {
 			
 			public void connected (Connection c) {
 				RegisterPlayer rp = new RegisterPlayer();
 				rp.name = cfg.playerName;
 				client.sendTCP(rp);
 			}
 			
 			public void received (Connection c, Object obj) {
 				if (controller != null) {
 					if (obj instanceof Command) {
 						Command cmd = (Command)obj;
 						if (cmd instanceof Unpause) System.out.println("Unpause " + Integer.toString(cmd.tick));
 						controller.queueCommand(cmd);
 					}
 				}
 				
 				if (obj instanceof RegisteredPlayer) {
 					RegisteredPlayer reg = (RegisteredPlayer)obj;
 					playerId = reg.id;
 					Gdx.app.log(LOG, "RegisteredPlayer rec'd");
 				}
 				
 				if (obj instanceof RegisteredPlayer[]) {
 					Gdx.app.log(LOG, "Playerlist rec'd");
 					RegisteredPlayer[] plist = (RegisteredPlayer[])obj;
 					players.clear();
 					for (int p=0;p<plist.length;p++) {
 						Player pl = new Player(plist[p].id, plist[p].name, plist[p].color, plist[p].team);
 						players.add(pl);
 						if (pl.getPlayerId() == playerId) player = pl;
 					}
 				}
 				
 				if (obj instanceof StartGame) {
 					// we want to start the game, but we need to load our objects on the other thread, where we have an OpenGL context
 					game.setLoadGame();
 				}
 				
 				if (obj instanceof SetupInfo) {
 					 
 					gameConfig.mapName = ((SetupInfo) obj).mapName;
 					
 				}
 			}
 			
 			public void disconnected (Connection c) {
 				
 			}
 		});
 		
 		isConnecting = true;
 		host = cfg.hostName;
 		new Thread() {
 			public void run() {
 				try {
 					
 					client.connect(5000, host, KryoCommon.tcpPort);
 					
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					Gdx.app.log(LOG, "Client connection failed: " + e.getMessage());
 					e.printStackTrace();
				} 
 				isConnecting = false;
 			}
 		}.start();
 	}
 	
 	public void startGame() {
 		// OK, this should be called within an opengl context, so we can create everything
 		controller = new GameController("", players);
 		controller.LOG = controller.LOG + "#Client";
 		
 		lastTimeMillis = System.currentTimeMillis();
 		
 		sendStartGame();
 	}
 	
 	public void sendStartGame() {
 		// report that we're ready
 		client.sendTCP(new StartGame());
 	}
 	
 	public void stop() {
 		stopClient = true;
 	}
 	
 	public boolean update() {
 		if (stopClient) {
 			client.stop();
 			return false;
 		}
 		
 		if (controller == null) return true;
 		
 		long time = System.currentTimeMillis();
 		int delta = (int)(time - lastTimeMillis);
 		lastTimeMillis = time;
 		
 		//Gdx.app.log("Client#Update", String.format("time: %d, lastTime: %d, delta: %d, sinceLastGameTick: %d", time, lastTimeMillis, delta, sinceLastGameTick));
 		if (delta < 0) return true;
 		
 		sinceLastNetTick += delta;
 		sinceLastGameTick += delta;
 		
 		
 		while (sinceLastNetTick >= KryoCommon.NET_TICK_LENGTH) {
 			// TODO net tick
 			sinceLastNetTick -= KryoCommon.NET_TICK_LENGTH;
 			for (Player p: players) {
 				p.update(controller, KryoCommon.NET_TICK_LENGTH * 0.001f);
 			}
 			controller.netUpdate(KryoCommon.NET_TICK_LENGTH * 0.001f);
 		}
 		
 		while (sinceLastGameTick >= KryoCommon.GAME_TICK_LENGTH) {
 			controller.update(KryoCommon.GAME_TICK_LENGTH * 0.001f);
 			sinceLastGameTick -= KryoCommon.GAME_TICK_LENGTH;
 		}
 		
 		long sleepTime = KryoCommon.GAME_TICK_LENGTH - (System.currentTimeMillis() - lastTimeMillis) - sinceLastGameTick;
 		
 		try {
 			if (sleepTime > 0) Thread.sleep(sleepTime);
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		return true;
 	}
 	
 	public void addAIPlayer(int team) {
 		AddAIPlayer add = new AddAIPlayer();
 		add.team = team;
 		client.sendTCP(add);
 	}
 	
 	public void removePlayer(int playerId) {
 		RemovePlayer rem = new RemovePlayer();
 		rem.id = playerId;
 		client.sendTCP(rem);
 	}
 	
 	public void sendCommand(Command cmd) {
 		client.sendTCP(cmd);
 	}
 	
 	public GameController getController() {
 		return controller;
 	}
 	
 	public ArrayList<Player> getPlayers() {
 		return players;
 	}
 	
 	public Player getPlayer() {
 		return player;
 	}
 	
 	public boolean isConnected() {
 		if (client == null) return false;
 		
 		return client.isConnected();
 	}
 	
 	public boolean isConnecting() {
 		return isConnecting;
 	}
 }
