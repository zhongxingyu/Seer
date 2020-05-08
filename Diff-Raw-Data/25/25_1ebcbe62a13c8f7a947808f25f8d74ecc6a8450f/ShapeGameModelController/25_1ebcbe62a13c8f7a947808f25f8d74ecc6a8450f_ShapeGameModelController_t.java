 package it.chalmers.tendu.controllers;
 
 import it.chalmers.tendu.gamemodel.GameId;
 import it.chalmers.tendu.gamemodel.Player;
 import it.chalmers.tendu.gamemodel.shapesgame.Shape;
 import it.chalmers.tendu.gamemodel.shapesgame.ShapeGame;
 import it.chalmers.tendu.tbd.C;
 import it.chalmers.tendu.tbd.C.Tag;
 import it.chalmers.tendu.tbd.EventBus;
 import it.chalmers.tendu.tbd.EventMessage;
 
 import java.util.List;
 
 import com.badlogic.gdx.Gdx;
 
 public class ShapeGameModelController implements MiniGameController {
 
 	private final String TAG = "ShapeGameModelController";
 	private ShapeGame shapeGame;
 
 	public ShapeGameModelController(ShapeGame model) {
 		shapeGame = model;
 		EventBus.INSTANCE.addListener(this);
 	}
 
 	public ShapeGame getModel() {
 		return shapeGame;
 	}
 
 	@Override
 	public void onBroadcast(EventMessage message) {
 		if (Player.getInstance().isHost()) {
 			handleAsHost(message);
 		} else {
			//Gdx.app.log(TAG, "Message: " + (message == null));
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
					//Gdx.app.log(TAG, "Sent from server");
 					EventBus.INSTANCE.broadcast(message);
 				}
 				// Send object
 				if (message.msg == C.Msg.SHAPE_SENT) {
 					sendShape(message.content);
 					message.tag = C.Tag.COMMAND_AS_HOST;
					//Gdx.app.log(TAG, "Sent from server");
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
				//Gdx.app.log(TAG, "Recived from host");
 				// Lock attempt
 				if (message.msg == C.Msg.LOCK_ATTEMPT) {
 					if (insertIntoSlot(message.content)) {
 						EventMessage soundMsg = new EventMessage(C.Tag.TO_SELF,
 								C.Msg.SOUND_SUCCEED);
 						EventBus.INSTANCE.broadcast(soundMsg);
 						Gdx.app.log(TAG, "Client changed model");
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
 		// Since we send objects, their references no longer matches our model
 		// we have to see which of the objects in "our" model that was sent. 
 		for (Shape l : shapeGame.getLock(player).getLockSequence()) {
 			if (l.equals(lockShape))
 				lockShape = l;
 		}
 		Shape shape = (Shape) messageContent.get(2);
 		for (Shape s : shapeGame.getAllInventory().get(player)) {
 			if (s.equals(shape))
 				shape = s;
 		}
 		return shapeGame.insertShapeIntoSlot(player, shape, lockShape);
 	}
 
 	@Override
 	public void unregister() {
 		EventBus.INSTANCE.removeListener(this);
 	}
 
 	// TODO Shape should appear on the proper pos
 	private void sendShape(Object content) {
 		List<Object> messageContent = (List) content;
 		int player = (Integer) messageContent.get(0);
 		// Since we send objects, their references no longer matches our model
 				// we have to see which of the objects in "our" model that was sent.
 		Shape shape = (Shape) messageContent.get(1);
 		for (Shape s : shapeGame.getAllInventory().get(player)) {
 			if (s.equals(shape))
 				shape = s;
 		}
 		int sender = shapeGame.move(shape, player);
 		
 	}
 }
