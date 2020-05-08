 package it.chalmers.tendu.controllers;
 
 import it.chalmers.tendu.Tendu;
 import it.chalmers.tendu.gamemodel.GameId;
 import it.chalmers.tendu.gamemodel.GameSession;
 import it.chalmers.tendu.gamemodel.Player;
 import it.chalmers.tendu.gamemodel.shapesgame.Shape;
 import it.chalmers.tendu.gamemodel.shapesgame.ShapesGame;
 import it.chalmers.tendu.tbd.C;
 import it.chalmers.tendu.tbd.C.Tag;
 import it.chalmers.tendu.tbd.EventBus;
 import it.chalmers.tendu.tbd.EventMessage;
 import it.chalmers.tendu.tbd.Listener;
 
 import java.util.List;
 
 import com.badlogic.gdx.Gdx;
 
 public class ShapeGameModelController implements MiniGameController {
 
 	private final String TAG = "ShapeGameModelController";
 	private ShapesGame shapeGame;
 
 	public ShapeGameModelController(ShapesGame model) {
 		this.shapeGame = model;
 		EventBus.INSTANCE.addListener(this);
 	}
 
 	public ShapesGame getModel() {
 		return shapeGame;
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
 
 	@Override
 	public void handleAsHost(EventMessage message) {
 		if (message.tag == C.Tag.CLIENT_REQUESTED
 				|| message.tag == C.Tag.TO_SELF) {
 			if (message.msg == C.Msg.START_MINI_GAME) {
 				shapeGame.startGame();
 				shapeGame.startGameTimer();
 			}
 
 			if (message.gameId == GameId.SHAPE_GAME) {
 				// Lock attempt
 				if (message.msg == C.Msg.LOCK_ATTEMPT) {
 					if (insertIntoSlot(message.content)) {
 						EventMessage soundMsg = new EventMessage(C.Tag.TO_SELF,
 								C.Msg.SOUND_SUCCEED);
 						EventBus.INSTANCE.broadcast(soundMsg);
 					} else {
 						EventMessage soundMsg = new EventMessage(C.Tag.TO_SELF,
 								C.Msg.SOUND_FAIL);
 						EventBus.INSTANCE.broadcast(soundMsg);
 					}
 					message.tag = C.Tag.COMMAND_AS_HOST;
 					Gdx.app.log(TAG, "Sent from server");
 					EventBus.INSTANCE.broadcast(message);
 				}
 				// Send object
 				if (message.msg == C.Msg.SHAPE_SENT) {
 					sendShape(message.content);
 					message.tag = C.Tag.COMMAND_AS_HOST;
 					Gdx.app.log(TAG, "Sent from server");
 					EventBus.INSTANCE.broadcast(message);
 				}
 			}
 
 		}
 	}
 
 	@Override
 	public void handleAsClient(EventMessage message) {
 		if (message.tag == C.Tag.TO_SELF) {
 			if (message.gameId == GameId.SHAPE_GAME) {
 				if (message.msg == C.Msg.LOCK_ATTEMPT
 						|| message.msg == C.Msg.SHAPE_SENT) {
 					message.tag = Tag.REQUEST_AS_CLIENT;
 					EventBus.INSTANCE.broadcast(message);
 				}
 			} else if (message.msg == C.Msg.START_MINI_GAME) {
 				shapeGame.startGame();
 				shapeGame.startGameTimer();
 			}
 		}
 
 		if (message.tag == Tag.HOST_COMMANDED) {
 			if (message.gameId == GameId.SHAPE_GAME) {
 				Gdx.app.log(TAG, "Recived from host");
 				// Lock attempt
 				if (message.msg == C.Msg.LOCK_ATTEMPT) {
 					if (insertIntoSlot(message.content)) {
 						EventMessage soundMsg = new EventMessage(C.Tag.TO_SELF,
 								C.Msg.SOUND_SUCCEED);
 						EventBus.INSTANCE.broadcast(soundMsg);
 					} else {
 						EventMessage soundMsg = new EventMessage(C.Tag.TO_SELF,
 								C.Msg.SOUND_FAIL);
 						EventBus.INSTANCE.broadcast(soundMsg);
 					}
 				}
 				if (message.msg == C.Msg.SHAPE_SENT) {
 					sendShape(message.content);
 				}
 			}
 		}
 	}
 
 	private boolean fitsIntoSlot(Object content) {
 		List<Object> messageContent = (List) content;
 		int player = (Integer) messageContent.get(0);
 		Shape lockShape = (Shape) messageContent.get(1);
 		Shape shape = (Shape) messageContent.get(2);
 
 		return shapeGame.shapeFitIntoLock(player, shape, lockShape);
 	}
 
 	private boolean insertIntoSlot(Object content) {
 		List<Object> messageContent = (List) content;
 		int player = (Integer) messageContent.get(0);
 		Shape lockShape = (Shape) messageContent.get(1);
 		Shape shape = (Shape) messageContent.get(2);
 
 		return shapeGame.insertShapeIntoSlot(player, shape, lockShape);
 	}
 
 	@Override
 	public void unregister() {
 		EventBus.INSTANCE.removeListener(this);
 	}
 
 	private void sendShape(Object content) {
 		List<Object> messageContent = (List) content;
 		int player = (Integer) messageContent.get(0);
 		Shape shape = (Shape) messageContent.get(1);
 		shapeGame.move(shape, player);
 	}
 }
