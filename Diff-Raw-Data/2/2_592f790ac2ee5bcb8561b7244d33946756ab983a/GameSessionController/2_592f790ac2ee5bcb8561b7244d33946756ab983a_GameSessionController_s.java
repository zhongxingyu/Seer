 package it.chalmers.tendu.controllers;
 
 import it.chalmers.tendu.gamemodel.GameResult;
 import it.chalmers.tendu.gamemodel.GameSession;
 import it.chalmers.tendu.gamemodel.GameState;
 import it.chalmers.tendu.gamemodel.MiniGame;
 import it.chalmers.tendu.gamemodel.Player;
 import it.chalmers.tendu.tbd.C;
 import it.chalmers.tendu.tbd.C.Tag;
 import it.chalmers.tendu.tbd.EventBus;
 import it.chalmers.tendu.tbd.EventMessage;
 import it.chalmers.tendu.tbd.Listener;
 
 import com.badlogic.gdx.Gdx;
 
 public class GameSessionController implements Listener {
 
 	private String TAG = getClass().getSimpleName();
 
 	private GameSession gameSession;
 
 	public GameSessionController(GameSession gameSession) {
 		this.gameSession = gameSession;
 		EventBus.INSTANCE.addListener(this);
 		gameSession.nextScreen();
 	}
 
 	public void setModel(GameSession session) {
 		this.gameSession = session;
 	}
 
 	@Override
 	public void onBroadcast(EventMessage message) {
 		if (Player.getInstance().isHost()) {
 			handleAsHost(message);
 		} else {
 			Gdx.app.log(TAG, "Message: " + (message == null));
 			handleAsClient(message);
 		}
 	}
 
 	private void handleAsHost(EventMessage message) {
 		if (message.tag == C.Tag.CLIENT_REQUESTED
 				|| message.tag == C.Tag.TO_SELF) {
 
 			if (message.msg == C.Msg.WAITING_TO_START_GAME) {
 				String macAddress = (String) message.content;
 				gameSession.playerWaitingToStart(macAddress);
 
 				if (gameSession.allWaiting()) {
 					EventMessage msg = new EventMessage(C.Tag.COMMAND_AS_HOST,
 							C.Msg.START_MINI_GAME);
 					EventBus.INSTANCE.broadcast(msg);
 					msg.tag = C.Tag.TO_SELF;
 					EventBus.INSTANCE.broadcast(msg);
 				}
 
 			} else if (message.msg == C.Msg.GAME_RESULT) {
 
 				GameResult result = (GameResult) message.content;
 				gameSession.enterResult(result);
 
 				if (result.getGameState() == GameState.WON) {
 					MiniGame miniGame = gameSession.getNextMiniGame();
 					gameSession.setCurrentMiniGame(miniGame);
 					EventMessage eventMessage = new EventMessage(
 							C.Tag.COMMAND_AS_HOST, C.Msg.SHOW_INTERIM_SCREEN,
 							gameSession);
 					EventBus.INSTANCE.broadcast(eventMessage);
 					gameSession.interimScreen();
 
 				} else if (result.getGameState() == GameState.LOST) {
 					EventMessage eventMessage = new EventMessage(
 							C.Tag.COMMAND_AS_HOST, C.Msg.SHOW_GAME_OVER_SCREEN,
 							gameSession);
 					EventBus.INSTANCE.broadcast(eventMessage);
 					gameSession.gameOverScreen();
 
 				}
 
 			} else if (message.msg == C.Msg.INTERIM_FINISHED) {
 				EventMessage msg = new EventMessage(C.Tag.COMMAND_AS_HOST,
 						C.Msg.LOAD_GAME);
 				EventBus.INSTANCE.broadcast(msg);
 				gameSession.nextScreen();
 
 			} else if (message.msg == C.Msg.PLAYER_REPLAY_READY) {
 				String playerMac = (String) message.content;
 				gameSession.playerReplayReady(playerMac);
 
 				if (gameSession.arePlayersReady()) {
 					gameSession.getNextMiniGame();
 					EventMessage msg = new EventMessage(C.Tag.COMMAND_AS_HOST,
 							C.Msg.GAME_SESSION_MODEL, gameSession);
 					EventBus.INSTANCE.broadcast(msg);
					msg = new EventMessage(C.Tag.HOST_COMMANDED,
 							C.Msg.LOAD_GAME);
 					EventBus.INSTANCE.broadcast(msg);
 					gameSession.nextScreen();
 				}
 
 			} else if (message.msg == C.Msg.RETURN_MAIN_MENU) {
 				returnToMainMenu();
 			}
 		}
 	}
 
 	private void handleAsClient(EventMessage message) {
 		if (message.tag == C.Tag.TO_SELF) {
 
 			if (message.msg == C.Msg.WAITING_TO_START_GAME) {
 				message.tag = C.Tag.REQUEST_AS_CLIENT;
 				EventBus.INSTANCE.broadcast(message);
 
 			} else if (message.msg == C.Msg.GAME_RESULT) {
 				GameResult result = (GameResult) message.content;
 				gameSession.enterResult(result);
 
 			} else if (message.msg == C.Msg.PLAYER_READY) {
 				message.tag = C.Tag.REQUEST_AS_CLIENT;
 				EventBus.INSTANCE.broadcast(message);
 
 			} else if (message.msg == C.Msg.PLAYER_REPLAY_READY) {
 				message.tag = C.Tag.REQUEST_AS_CLIENT;
 				EventBus.INSTANCE.broadcast(message);
 
 			} else if (message.msg == C.Msg.RETURN_MAIN_MENU) {
 				returnToMainMenu();
 			}
 
 		} else if (message.tag == Tag.HOST_COMMANDED) {
 
 			if (message.msg == C.Msg.LOAD_GAME) {
 				gameSession.nextScreen();
 
 			} else if (message.msg == C.Msg.START_MINI_GAME) {
 				message.tag = C.Tag.TO_SELF;
 				EventBus.INSTANCE.broadcast(message);
 
 			} else if (message.msg == C.Msg.GAME_SESSION_MODEL) {
 				this.gameSession = (GameSession) message.content;
 
 			} else if (message.msg == C.Msg.SHOW_INTERIM_SCREEN) {
 				this.gameSession = (GameSession) message.content;
 				gameSession.interimScreen();
 
 			} else if (message.msg == C.Msg.SHOW_GAME_OVER_SCREEN) {
 				this.gameSession = (GameSession) message.content;
 				gameSession.gameOverScreen();
 			}
 		}
 	}
 
 	private void returnToMainMenu() {
 		EventMessage message = new EventMessage(C.Tag.TO_SELF, C.Msg.RESTART);
 		EventBus.INSTANCE.broadcast(message);
 		unregister();
 	}
 
 	@Override
 	public void unregister() {
 		EventBus.INSTANCE.removeListener(this);
 	}
 }
