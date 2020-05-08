 /*
  ********************************************************************************
  * Copyright (c) 2013 Samsung Electronics, Inc.
  * All rights reserved.
  *
  * This software is a confidential and proprietary information of Samsung
  * Electronics, Inc. ("Confidential Information"). You shall not disclose such
  * Confidential Information and shall use it only in accordance with the terms
  * of the license agreement you entered into with Samsung Electronics.
  ********************************************************************************
  */
 package com.zhideel.tapathon.logic;
 
 import android.content.Intent;
 import android.content.res.Resources;
 import com.squareup.otto.Bus;
 import com.squareup.otto.Subscribe;
 import com.zhideel.tapathon.Config;
 import com.zhideel.tapathon.chord.BusEvent;
 import com.zhideel.tapathon.chord.ChordMessage;
 import com.zhideel.tapathon.chord.ChordMessage.MessageType;
 import com.zhideel.tapathon.chord.GameChord.ClientDisconnectedEvent;
 import com.zhideel.tapathon.logic.CommunicationBus.BusManager;
 import com.zhideel.tapathon.logic.GameUtils.GameResult;
 import com.zhideel.tapathon.logic.ServerModel.GameState;
 import com.zhideel.tapathon.ui.EndGameActivity;
 import com.zhideel.tapathon.ui.GamePadActivity;
 
 import java.util.List;
 
 /**
  * Encapsulates whole logic of the game.
  */
 public class GameLogicController implements BusManager {
     private final Bus mBus;
     public static ServerModel mModel;
     private final Resources mResources;
     public GameResult gameResult;
 
     public GameLogicController(Model model, Resources resources) {
         mBus = CommunicationBus.getInstance();
         mModel = model.getServerModel();
         mModel.initTable();
         mResources = resources;
     }
 
     /**
      * Performs initial actions at the start of the game.
      *
      * @param event that triggers game start
      */
     @Subscribe
     public void startGame(StartGameEvent event) {
         mModel.setGameState(GameState.STARTED);
         mBus.post(new ClientModel.ClientModelEvent.GameStart());
         final ChordMessage gameStartMessage = ChordMessage.obtainMessage(MessageType.GAME_START);
         for (Player player : mModel.getPlayers()) {
             sendToClient(gameStartMessage, player.getNodeName());
         }
     }
 
     /**
      * Performs initial actions at the end of the game.
      *
      * @param event that triggers game end
      */
     @Subscribe
     public void endGame(EndGameEvent event) {
         final List<Player> winners;
         final List<Player> losers;
         gameResult = GameUtils.getGameResult(mModel.getPlayers());
         winners = gameResult.getWinners();
         losers = mModel.getPlayers();
 
         // Notify the winner
         for (Player winner : winners) {
             if (losers != null) {
                 losers.remove(winner);
             }
 
             final ChordMessage gameEndMessage = ChordMessage.obtainMessage(MessageType.GAME_END);
             gameEndMessage.putInt(ChordMessage.SCORE, winner.getScore());
             sendToClient(gameEndMessage, winner.getNodeName());
         }
 
         // Notify the losers
         if (losers != null) {
             for (Player loser : losers) {
                 final ChordMessage gameEndMessage = ChordMessage.obtainMessage(MessageType.GAME_END);
                 gameEndMessage.putInt(ChordMessage.SCORE, loser.getScore());
                 sendToClient(gameEndMessage, loser.getNodeName());
             }
         }
 
         mModel.clearGame();
         mModel.setGameState(GameState.GAME_FINISHED);
        /*
         Intent eg = new Intent(GamePadActivity.instance, EndGameActivity.class);
        GamePadActivity.instance.startActivity(eg);*/
     }
 
     /**
      * Handles the user name event.
      *
      * @param usernameEvent containing information about player that joined the game
      */
     @Subscribe
     public void handleUsername(GameLogicEvent.UsernameEvent usernameEvent) {
         final String nodeName = usernameEvent.getNodeName();
         final Player player = mModel.getPlayer(nodeName);
         Integer score = ServerModel.INITIAL_SCORE;
         ChordMessage stateMessage = ChordMessage.obtainMessage(MessageType.USERNAME);
         if (player == null) {
             mModel.addPlayer(Player.createPlayer(usernameEvent.getUsername(), nodeName));
         } else {
             score = player.getScore();
         }
         stateMessage.putInt(ChordMessage.SCORE, score);
         sendToClient(stateMessage, usernameEvent);
     }
 
     private <T extends GameLogicEvent> void sendToClient(ChordMessage message, T event) {
         sendToClient(message, event.getNodeName());
     }
 
     private void sendToClient(ChordMessage message, String nodeName) {
         message.setReceiverNodeName(nodeName);
         message.setFromLogic(true);
         mBus.post(message);
     }
 
     private void sendToClient(ChordMessage message, Player player) {
         sendToClient(message, player.getNodeName());
     }
 
     @Subscribe
     public void onPlayerDisconnected(ClientDisconnectedEvent event) {
         final Player player = mModel.getPlayer(event.getNodeName());
     }
 
     @Override
     public void startBus() {
         mBus.register(this);
     }
 
     @Override
     public void stopBus() {
         mBus.unregister(this);
     }
 
     /**
      * Event posted through the {@link com.squareup.otto.Bus} that contains information about changes in the game logic.
      */
     public static class GameLogicEvent extends BusEvent {
 
         private static final long serialVersionUID = 20130312L;
 
         private final GameLogicEventType mType;
         private static final String NODE_NAME = "NODE_NAME";
         private static final String SCORE = "SCORE";
 
         private GameLogicEvent(GameLogicEventType type, String nodeName) {
             super();
             putObject(NODE_NAME, nodeName);
             mType = type;
         }
 
         public String getNodeName() {
             return (String) getObject(NODE_NAME);
         }
 
         public static class UsernameEvent extends GameLogicEvent {
 
             private static final long serialVersionUID = 20130325L;
 
             public static final String USERNAME = "USERNAME";
 
             public UsernameEvent(String username, String nodeName) {
                 super(GameLogicEventType.USERNAME, nodeName);
                 putObject(USERNAME, username);
             }
 
             public String getUsername() {
                 return (String) getObject(USERNAME);
             }
 
         }
 
         /**
          * Represents type of the {@link com.zhideel.tapathon.logic.GameLogicController.GameLogicEvent}.
          */
         public enum GameLogicEventType {
             //@formatter:off
             USERNAME;
             //@formatter:on
 
             @Override
             public String toString() {
                 return name();
             }
         }
     }
 
     public enum StartGameEvent {
         INSTANCE;
     }
 
     public enum EndGameEvent {
         INSTANCE;
     }
 }
