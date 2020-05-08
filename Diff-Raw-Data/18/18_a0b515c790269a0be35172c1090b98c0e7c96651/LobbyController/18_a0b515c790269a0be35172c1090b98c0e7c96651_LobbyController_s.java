 package it.chalmers.tendu.controllers;
 
 import it.chalmers.tendu.gamemodel.GameId;
 import it.chalmers.tendu.gamemodel.GameSession;
 import it.chalmers.tendu.gamemodel.LobbyModel;
 import it.chalmers.tendu.gamemodel.Player;
 import it.chalmers.tendu.tbd.C;
 import it.chalmers.tendu.tbd.EventBus;
 import it.chalmers.tendu.tbd.EventMessage;
 import it.chalmers.tendu.tbd.Listener;
 import it.chalmers.tendu.tbd.C.Msg;
 import it.chalmers.tendu.tbd.C.Tag;
 
 import java.util.List;
 
 import com.badlogic.gdx.Gdx;
 
 public class LobbyController implements Listener {
 	public static final String TAG = "LobbyController";
 
 	private LobbyModel model;
 
 	public LobbyController(LobbyModel model) {
 		EventBus.INSTANCE.addListener(this);
 		this.model = model;
 	}
 
 	@Override
 	public void onBroadcast(EventMessage message) {
 		if (model.isHost()) {
 			Gdx.app.log(TAG, "Are we host yet?");
 			handleAsHost(message);
 		} else {
 			Gdx.app.log(TAG, "Message: " + (message == null));
 			handleAsClient(message);
 		}
 	}
 
 	private void handleAsHost(EventMessage message) {
 		if (message.tag == C.Tag.CLIENT_REQUESTED
 				|| message.tag == C.Tag.ACCESS_MODEL) {
 			switch (message.msg) {
 			case PLAYER_CONNECTED:
 				Gdx.app.log(TAG, "Player connected should be seen on screen");
 				model.addPlayer((String) message.content);
 				EventBus.INSTANCE
 						.broadcast(new EventMessage(C.Tag.COMMAND_AS_HOST,
 								C.Msg.UPDATE_LOBBY_MODEL, model));
 				if (model.isMaxPlayersConnected()) {
 					GameSession gameSession = new GameSession();
 					new ModelController(gameSession);
 					gameSession.setCurrentMiniGame(gameSession
 							.getNextMiniGame());
 					EventBus.INSTANCE.broadcast(new EventMessage(
 							C.Tag.COMMAND_AS_HOST, C.Msg.GAME_SESSION_MODEL,
 							gameSession));
 				}
 				break;
 			case PLAYER_READY:
 
 				// Message content is the players macID
 				model.playerReady((String) message.content, true);
 				EventBus.INSTANCE
 						.broadcast(new EventMessage(C.Tag.COMMAND_AS_HOST,
 								C.Msg.UPDATE_LOBBY_MODEL, model));
 
 				// Start the game for all players if they are ready.
 				if (model.allPlayersReady()) {
 					EventBus.INSTANCE.broadcast(new EventMessage(
 							C.Tag.COMMAND_AS_HOST, C.Msg.START_MINI_GAME));
 				}
 				break;
 			default:
 				Gdx.app.error(TAG, "Incorrect C.msg broadcasted");
 				break;
 			}
 		}
 	}
 
 	private void handleAsClient(EventMessage message) {
 		if (message.tag == C.Tag.ACCESS_MODEL) {
 			if (message.msg == C.Msg.PLAYER_READY) {
 				model.playerReady(Player.getInstance().getMac(), true);
 				EventBus.INSTANCE.broadcast(new EventMessage(
 						C.Tag.REQUEST_AS_CLIENT, C.Msg.PLAYER_READY, Player
 								.getInstance().getMac()));
 			}

			if (message.tag == Tag.HOST_COMMANDED) {
				if (message.msg == Msg.GAME_SESSION_MODEL) {
					new ModelController((GameSession) message.content);
				}
				if (message.msg == Msg.UPDATE_LOBBY_MODEL) {
					LobbyModel lModel = (LobbyModel) message.content;
					Gdx.app.debug("DEBUG LOBBY", "new Model:" + lModel.players.keySet().toString());
					this.model = lModel;
				}
 			}
 		}
 	}
 
 	private void createGameSession() {
 		GameSession gameSession = new GameSession();
 		new ModelController(gameSession);
 	}
 	
 	public LobbyModel getModel(){
 		return model;
 	}
 }
