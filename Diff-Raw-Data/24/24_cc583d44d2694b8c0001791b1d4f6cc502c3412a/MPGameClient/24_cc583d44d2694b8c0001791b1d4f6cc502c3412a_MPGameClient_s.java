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
 package com.ahsgaming.valleyofbones.network;
 
 import java.io.IOException;
 import java.util.HashMap;
 
 import com.ahsgaming.valleyofbones.GameController;
 import com.ahsgaming.valleyofbones.GameResult;
 import com.ahsgaming.valleyofbones.GameStates;
 import com.ahsgaming.valleyofbones.Player;
 import com.ahsgaming.valleyofbones.VOBGame;
 import com.ahsgaming.valleyofbones.network.KryoCommon.AddAIPlayer;
 import com.ahsgaming.valleyofbones.network.KryoCommon.RegisterPlayer;
 import com.ahsgaming.valleyofbones.network.KryoCommon.RegisteredPlayer;
 import com.ahsgaming.valleyofbones.network.KryoCommon.RemovePlayer;
 import com.ahsgaming.valleyofbones.network.KryoCommon.StartGame;
 import com.ahsgaming.valleyofbones.screens.GameSetupConfig;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.utils.Array;
 import com.badlogic.gdx.utils.Json;
 import com.esotericsoftware.kryonet.Client;
 import com.esotericsoftware.kryonet.Connection;
 import com.esotericsoftware.kryonet.Listener;
 
 /**
  * @author jami
  *
  */
 public class MPGameClient implements NetController {
 	public String LOG = "MPGameClient";
 	
 	Client client;
 	String host;
 
 	int playerId = -1;
 	Player player = null;
 	
 	GameController controller;
 	
 	GameSetupConfig gameConfig;
 	
 	Array<Player> players = new Array<Player>();
     HashMap<Integer, String> spectators = new HashMap<Integer, String>();
 
     int firstTurnPid = -1;
 	
 	VOBGame game;
 	
 	boolean stopClient = false;
 	
 	boolean isConnecting = false;
     boolean recdRegisterPlayer = false;
 	
 	GameResult gameResult = null;
 
     boolean sentEndTurn = false;
     boolean recdEndTurn = false;
 
     boolean isReconnect = false;
 
     KryoCommon.Error error;
 
     Array<String> chatLog = new Array<String>();
 
     KryoCommon.GameUpdate gameUpdate;
     Array<Command> commandQueue = new Array<Command>();
 	
 	/**
 	 * 
 	 */
 	public MPGameClient(VOBGame g, final GameSetupConfig cfg) {
 		this.game = g;
 		gameConfig = cfg;
 		
 		client = new Client();
 		client.start();
 		
 		KryoCommon.register(client);
 		
 		client.addListener(new Listener() {
 			
 			public void connected (Connection c) {
                 RegisterPlayer rp = new RegisterPlayer();
                 rp.name = cfg.playerName;
                 rp.spectator = cfg.isSpectator;
                 rp.race = cfg.playerRace;
                 rp.key = cfg.playerKey;
                 client.sendTCP(rp);
 			}
 			
 			public void received (Connection c, Object obj) {
 
                 if (isConnecting) {
                     if (obj instanceof KryoCommon.VersionError) {
                         Gdx.app.log(LOG, "VersionError");
                         error = (KryoCommon.VersionError)obj;
                         c.close();
                     }
 
                     if (obj instanceof KryoCommon.GameFullError) {
                         Gdx.app.log(LOG, "GameFullError");
                         error = (KryoCommon.GameFullError)obj;
                         c.close();
                     }
 
                     if (obj instanceof RegisteredPlayer) {
                         RegisteredPlayer reg = (RegisteredPlayer)obj;
                         playerId = reg.id;
                         gameConfig.isHost = reg.host;
                         Gdx.app.log(LOG, String.format("RegisteredPlayer rec'd (id: %d)", playerId));
                         recdRegisterPlayer = true;
                         gameConfig.isSpectator = reg.spectator;
                     }
                     isConnecting = false;
                     return;
                 }
 
                 if (obj instanceof KryoCommon.ChatMessage) {
                     Gdx.app.log(LOG, "ChatMessage rec'd");
                     KryoCommon.ChatMessage chatMessage = (KryoCommon.ChatMessage)obj;
                     chatLog.add(chatMessage.name + ": " + chatMessage.message);
                 }
 
                 if (obj instanceof Command[]) {
                     Gdx.app.log(LOG, "Game history rec'd: " + ((Command[])obj).length);
                     commandQueue.addAll((Command[])obj);
                 }
 
                 if (obj instanceof KryoCommon.GameUpdate) {
                     Gdx.app.log(LOG, "Game update rec'd");
                     gameUpdate = (KryoCommon.GameUpdate)obj;
                 }
 
                 if (controller == null) {
                     if (obj instanceof RegisteredPlayer[]) {
                         Gdx.app.log(LOG, "Playerlist rec'd");
                         RegisteredPlayer[] plist = (RegisteredPlayer[])obj;
                         players.clear();
                         spectators.clear();
                         for (int p=0;p<plist.length;p++) {
                             if (plist[p].spectator) {
                                 spectators.put(plist[p].id, plist[p].name);
                             } else {
                                 Player pl = new Player(plist[p].id, plist[p].name, Player.AUTOCOLORS[plist[p].color], plist[p].race);
                                 if (plist[p].isAI) {
                                     pl.setReady(true);
                                     pl.setAI(true);
                                 } else {
                                     pl.setReady(plist[p].ready);
                                 }
                                 players.add(pl);
                                 if (pl.getPlayerId() == playerId) {
                                     player = pl;
                                     gameConfig.isHost = plist[p].host;
                                 }
 
                                 Gdx.app.log(LOG, "Player ready: " + plist[p].ready);
                             }
                         }
                     }
 
                     if (obj instanceof KryoCommon.GameDetails) {
                         Gdx.app.log(LOG, "GameDetails rec'd");
                         KryoCommon.GameDetails details = (KryoCommon.GameDetails) obj;
                         gameConfig.setDetails(details);
 
                         Json json = new Json();
                         System.out.println(json.toJson(details, KryoCommon.GameDetails.class));
 
                     }
 
                     if (obj instanceof StartGame) {
                         // we want to start the game, but we need to load our objects on the other thread, where we have an OpenGL context
                         Gdx.app.log(LOG, "StartGame rec'd");
                         game.setLoadGame();
                         firstTurnPid = ((StartGame)obj).currentPlayer;
                         gameConfig.spawnType = ((StartGame)obj).spawnType;
                     }
                     return;
                 }
 
                 if (obj instanceof Command) {
                     Command cmd = (Command)obj;
                     if (cmd instanceof Unpause) System.out.println("Unpause " + Integer.toString(cmd.turn));
                     if (cmd instanceof Pause) System.out.println("Pause " + Integer.toString(cmd.turn));
 //                    if (cmd instanceof Move) System.out.println("Move recd");
                     if (!(obj instanceof StartTurn || obj instanceof EndTurn)) {
                         if (commandQueue.size > 0) {
 //                            System.out.println("cmd to commandQueue");
                             commandQueue.add(cmd);
                         } else {
                             controller.queueCommand(cmd);
 //                            System.out.println("cmd to controller");
                         }
                     }
                 }
 
 				if (obj instanceof GameResult) {
 					Gdx.app.log(LOG, "GameResult rec'd");
 					gameResult = (GameResult)obj;
 				}
 				
 				if (obj instanceof StartTurn) {
 					Gdx.app.log(LOG, "StartTurn");
 				}
 				
 				if (obj instanceof EndTurn) {
 					Gdx.app.log(LOG, "EndTurn");
 					if (controller != null) {
                         controller.setCommandQueue(((EndTurn)obj).commands);
                         Gdx.app.log(LOG, String.format("EndTurn.commands.length = %d", ((EndTurn)obj).commands.length));
 
                         sentEndTurn = false;
                         recdEndTurn = true;
                     }
 				}
 
 			}
 			
 			public void disconnected (Connection c) {
                if (gameResult != null) {
                     isReconnect = true;
                     Pause p = new Pause();
                     p.isAuto = true;
                     p.owner = -1;
                     p.turn = controller.getGameTurn();
                     controller.queueCommand(p);
                 }
 			}
 		});
         connect();
 	}
 
     public void connect() {
         isConnecting = true;
         host = gameConfig.hostName;
         new Thread() {
             public void run() {
                 try {
                     Gdx.app.log(LOG, "Connecting to " + host + ":" + gameConfig.hostPort);
                     client.connect(5000, host, gameConfig.hostPort);
 
                 } catch (IOException e) {
                     Gdx.app.log(LOG, "Client connection failed: " + e.getMessage());
                     e.printStackTrace();
                     isConnecting = false;
                 }
 
             }
         }.start();
     }
 
     public void reconnect() {
         isConnecting = true;
         new Thread() {
             public void run() {
                 try {
                     Gdx.app.log(LOG, "Attempting to reconnect");
                     client.reconnect(5000);
                 } catch (IOException e) {
                     Gdx.app.log(LOG, "Reconnect failed: " + e.getMessage());
                     e.printStackTrace();
                     isConnecting = false;
                     gameResult = new GameResult();
                     gameResult.losers = new int[]{playerId};
                     int i = players.indexOf(player, true);
                     gameResult.winner = (i == 0 ? players.get(1).getPlayerId() : players.get(0).getPlayerId());
                 }
                 isReconnect = false;
                 isConnecting = false;
             }
         }.start();
     }
 	
 	public void startGame() {
 		// OK, this should be called within an opengl context, so we can create everything
 		controller = new GameController(gameConfig, players);
 		controller.LOG = controller.LOG + "#MPClient";
 
         controller.setCurrentPlayer(firstTurnPid);
 
 		sendStartGame();
 	}
 	
 	public void sendStartGame() {
 		// report that we're ready
 		client.sendTCP(new StartGame());
 	}
 	
 	public void endGame() {
 		client.stop();
 		controller.setState(GameStates.GAMEOVER);
 		game.setGameResult(gameResult);
 	}
 
     public void sendEndTurn() {
         sentEndTurn = true;
         client.sendTCP(new EndTurn());
     }
 	
 	public void stop() {
 		stopClient = true;
 	}
 	
 	public boolean update(float delta) {
 		if (stopClient) {
 			client.stop();
 			return false;
 		}
 
 		if (controller == null) return true;
 
         if (gameResult != null) {
             endGame();
             return false;
         }
 
         controller.update(delta);
 
         if (isReconnect && !isConnecting) {
            Gdx.app.log(LOG, "" + isReconnect);
            reconnect();
         }
 
         while (commandQueue.size > 0) {
             Gdx.app.log(LOG, "turn: " + controller.getGameTurn());
             if (commandQueue.first().turn > controller.getGameTurn()) {
                 controller.setNextTurn(true);
                 controller.doTurn();
             } else {
                 Command cmd = commandQueue.removeIndex(0);
                 if (cmd instanceof EndTurn) {
                     controller.setNextTurn(true);
                     controller.doTurn();
                 } else {
                     controller.queueCommand(cmd);
                 }
             }
             controller.update(0);
         }
 
         if (gameUpdate != null) {
             while (gameUpdate.turn > controller.getGameTurn()) {
                 controller.setNextTurn(true);
                 controller.doTurn();
                 controller.update(0);
             }
             controller.setCurrentPlayer(gameUpdate.currentPlayer);
             controller.setTurnTimer(gameUpdate.timer);
             gameUpdate = null;
         }
 
         if (recdEndTurn) {
             controller.setNextTurn(true);
             controller.doTurn();
             recdEndTurn = false;
         }
 
         if (!gameConfig.isSpectator) {
             if (controller.isNextTurn() || controller.getTurnTimer() <= 0) {
                 sendEndTurn();
             }
         }
 
 		return true;
 	}
 	
 	public void addAIPlayer() {
 		AddAIPlayer add = new AddAIPlayer();
 		client.sendTCP(add);
 	}
 	
 	public void removePlayer(int playerId) {
 		RemovePlayer rem = new RemovePlayer();
 		rem.id = playerId;
 		client.sendTCP(rem);
 	}
 	
 	public void sendCommand(Command cmd) {
         cmd.turn = controller.getGameTurn();
 		client.sendTCP(cmd);
 	}
 
     public void sendAICommand(Command cmd) {
         sendCommand(cmd);
     }
 	
 	public Array<Player> getPlayers() {
 		return players;
 	}
 	
 	public Player getPlayer() {
 		return player;
 	}
 
     public HashMap<Integer, String> getSpectators() {
         return spectators;
     }
 	
 	public boolean isConnected() {
 		if (client == null) return false;
 		
 		return recdRegisterPlayer && client.isConnected();
 	}
 	
 	public boolean isConnecting() {
 		return isConnecting;
 	}
 
     public KryoCommon.Error getError() {
         return error;
     }
 
 	@Override
 	public void setGameController(GameController controller) {
 		this.controller = controller;
 	}
 
 	@Override
 	public GameController getGameController() {
 		return controller;
 	}
 
     public void sendGameDetails(KryoCommon.GameDetails gameDetails) {
         if (gameConfig.isHost) {
             client.sendTCP(gameDetails);
             gameConfig.firstMove = gameDetails.firstMove;
             gameConfig.mapName = gameDetails.map;
             gameConfig.spawnType = gameDetails.spawn;
             gameConfig.ruleSet = gameDetails.rules;
         }
     }
 
     public void sendPlayerUpdate(KryoCommon.UpdatePlayer update) {
         client.sendTCP(update);
     }
 
     public void sendChat(String message) {
         KryoCommon.ChatMessage chat = new KryoCommon.ChatMessage();
         chat.name = gameConfig.playerName;//player.getPlayerName();
         chat.message = message;
         client.sendTCP(chat);
     }
 
     public Array<String> getChatLog() {
         return chatLog;
     }
 }
