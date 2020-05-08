 package it.chalmers.tendu.controllers;
 
 import it.chalmers.tendu.gamemodel.GameId;
 import it.chalmers.tendu.gamemodel.Player;
 import it.chalmers.tendu.gamemodel.shapesgame.Shape;
 import it.chalmers.tendu.gamemodel.shapesgame.ShapesGame;
 import it.chalmers.tendu.tbd.C;
 import it.chalmers.tendu.tbd.C.Msg;
 import it.chalmers.tendu.tbd.C.Tag;
 import it.chalmers.tendu.tbd.EventBus;
 import it.chalmers.tendu.tbd.EventMessage;
 import it.chalmers.tendu.tbd.Listener;
 
 import java.util.List;
 
 import com.badlogic.gdx.Gdx;
 
 public class ShapeGameModelController implements Listener {
 
 	private final String TAG = "ShapeGameModelController";
 	private ShapesGame model;
 
 	public ShapeGameModelController(ShapesGame model) {
 		this.model = model;
 		EventBus.INSTANCE.addListener(this);
 	}
 
 	public ShapesGame getModel() {
 		return model;
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
 			if (message.gameId == GameId.SHAPES_GAME) {
 				if (message.msg == C.Msg.LOCK_ATTEMPT) {
					insertIntoSlot(message.content);
					message.tag = C.Tag.COMMAND_AS_HOST;
					Gdx.app.log(TAG, "Sent from server");
					EventBus.INSTANCE.broadcast(message);

 				}
 			}
 		}
 
 	}
 
 	private void handleAsClient(EventMessage message) {
 		if (message.tag == C.Tag.TO_SELF) {
 			if (message.gameId == GameId.SHAPES_GAME) {
 				if (message.msg == C.Msg.LOCK_ATTEMPT
 						|| message.msg == C.Msg.SHAPE_SENT) {
 					message.tag = Tag.REQUEST_AS_CLIENT;
 					EventBus.INSTANCE.broadcast(message);
 				}
 			}
 		}
 
 		if (message.tag == Tag.HOST_COMMANDED) {
 			if (message.gameId == GameId.SHAPES_GAME) {
				Gdx.app.log(TAG, "Recived from host");
 				insertIntoSlot(message.content);
 			}
 		}
 	}
 
 	private boolean fitsIntoSlot(Object content) {
 		List<Object> messageContent = (List) content;
 		int player = (Integer) messageContent.get(0);
 		Shape lockShape = (Shape) messageContent.get(1);
 		Shape shape = (Shape) messageContent.get(2);
 
 		return model.shapeFitIntoLock(player, shape, lockShape);
 	}
 
 	private boolean insertIntoSlot(Object content) {
 		List<Object> messageContent = (List) content;
 		int player = (Integer) messageContent.get(0);
 		Shape lockShape = (Shape) messageContent.get(1);
 		Shape shape = (Shape) messageContent.get(2);
 
 		return model.insertShapeIntoSlot(player, shape, lockShape);
 	}
 
 }
