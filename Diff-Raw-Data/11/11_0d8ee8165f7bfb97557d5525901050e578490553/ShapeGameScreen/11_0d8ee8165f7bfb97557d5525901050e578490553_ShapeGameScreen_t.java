 package it.chalmers.tendu.screens;
 
 import it.chalmers.tendu.Tendu;
 import it.chalmers.tendu.controllers.InputController;
 import it.chalmers.tendu.controllers.ShapeGameModelController;
 import it.chalmers.tendu.defaults.Constants;
 import it.chalmers.tendu.gamemodel.GameState;
 import it.chalmers.tendu.gamemodel.MiniGame;
 import it.chalmers.tendu.gamemodel.Player;
 import it.chalmers.tendu.gamemodel.SimpleTimer;
 import it.chalmers.tendu.gamemodel.shapesgame.Shape;
 import it.chalmers.tendu.gamemodel.shapesgame.ShapeGame;
 import it.chalmers.tendu.gamemodel.shapesgame.ShapeGameSound;
 import it.chalmers.tendu.tbd.C;
 import it.chalmers.tendu.tbd.EventBus;
 import it.chalmers.tendu.tbd.EventMessage;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
 
 public class ShapeGameScreen extends GameScreen {
 
 	public final String TAG = this.getClass().getName();
 
 	private int player_num;
 	private ShapeRenderer shapeRenderer; // used to render vector graphics
 
 	private List<GraphicalShape> shapes;
 	private List<GraphicalShape> locks;
 
 	private GraphicalShape movingShape;
 
 	private ShapeGameModelController controller;
 
 	private ShapeGameSound sound;
 
 	private Map<Integer, Shape> latestAddedShape;
 	private Shape latestRemovedShape;
 
 	ShapeGame shapeGameModel;
 
 	private SimpleTimer gameCompletedTimer;
 	private List<Integer> otherPlayers;
 
 	// For debug
 	int count = 0;
 
 	public ShapeGameScreen(Tendu game, MiniGame model) {
 		super(game, model);
 
 		controller = new ShapeGameModelController((ShapeGame) model);
 		shapeGameModel = controller.getModel();
 		this.shapeRenderer = new ShapeRenderer();
 
 		latestAddedShape = new HashMap<Integer, Shape>();
 
 		player_num = shapeGameModel.getplayerNbr();
 		sound = new ShapeGameSound();
 
 		shapes = new ArrayList<GraphicalShape>();
 		gameCompletedTimer = new SimpleTimer();
 
 		int x = Constants.SCREEN_WIDTH
 				/ (controller.getModel().getLock(player_num).getLockSequence()
 						.size() + 1) - 100;
 		for (Shape s : controller.getModel().getAllInventory().get(player_num)) {
 			GraphicalShape sgs = new GraphicalShape(s);
 			sgs.moveShape(x, 500);
 			shapes.add(sgs);
 			x = x
 					+ Constants.SCREEN_WIDTH
 					/ (controller.getModel().getLock(player_num)
 							.getLockSequence().size() + 1);
 		}
 
 		locks = new ArrayList<GraphicalShape>();
 
 		x = Constants.SCREEN_WIDTH
 				/ (controller.getModel().getLock(player_num).getLockSequence()
 						.size() + 1) - 100;
 		for (Shape s : controller.getModel().getLock(player_num)
 				.getLockSequence()) {
 
 			GraphicalShape sgs = new GraphicalShape(s);
 			sgs.moveShape(x, 250);
 			sgs.setRenderAsLock(true);
 			locks.add(sgs);
 			x = x
 					+ Constants.SCREEN_WIDTH
 					/ (controller.getModel().getLock(player_num)
 							.getLockSequence().size() + 1);
 		}
 		
 		otherPlayers = model.getOtherPlayerNumbers();
 		

 
 	}
 
 	/** All graphics are drawn here */
 	@Override
 	public void render() {
 		super.render();
 		if (shapeGameModel.checkGameState() == GameState.RUNNING) {
 			shapeRenderer.setProjectionMatrix(tendu.getCamera().combined);
 			// Renders locks
 			for (GraphicalShape sgs : locks) {
 				sgs.render(shapeRenderer);
 			}
 			// Renders shapes
 			for (GraphicalShape sgs : shapes) {
 				sgs.render(shapeRenderer);
 			}
 
 		} else {
 			// showGameResult();
 		}
 	}
 
 	/**
 	 * @param s
 	 */
 	private void sendToTeamMate(GraphicalShape s) {
 		Gdx.app.log(TAG, "SHAPE SENDING!!!!!!!!");
 		if (s.getBounds().x <= 160 && otherPlayers.size() >= 2) {
 			Gdx.app.log(TAG, "To ");
 			EventBus.INSTANCE.broadcast(new EventMessage(Player.getInstance()
 					.getMac(), C.Tag.TO_SELF, C.Msg.SHAPE_SENT, controller
 					.getModel().getGameId(), messageContentFactory(
 							otherPlayers.get(1) - 1, s.getShape())));
 		} else if (s.getBounds().x >= Constants.SCREEN_WIDTH - 160
 				&& otherPlayers.size() >= 3) {
 			EventBus.INSTANCE.broadcast(new EventMessage(Player.getInstance()
 					.getMac(), C.Tag.TO_SELF, C.Msg.SHAPE_SENT, controller
 					.getModel().getGameId(), messageContentFactory(
 							otherPlayers.get(2) - 1, s.getShape())));
 
 		} else if (s.getBounds().y >= Constants.SCREEN_HEIGHT - 160
 				&& otherPlayers.size() >= 1) {
 			EventBus.INSTANCE.broadcast(new EventMessage(Player.getInstance()
 					.getMac(), C.Tag.TO_SELF, C.Msg.SHAPE_SENT, controller
 					.getModel().getGameId(), messageContentFactory(
 							otherPlayers.get(0) - 1, s.getShape())));
 		}
 	}
 
 	/**
 	 * 
 	 * @param player
 	 *            That will receive the shape
 	 * @param shape
 	 *            shape to be sent
 	 * @return
 	 */
 	private List<Object> messageContentFactory(int player, Shape shape) {
 
 		List<Object> l = new ArrayList<Object>();
 		l.add(player);
 		l.add(shape);
 		return l;
 
 	}
 
 	/** All game logic goes here (within the model...) */
 	@Override
 	public void tick(InputController input) {
 		updateShapesFromModel();
 
 		if (!gameCompletedTimer.isRunning()) {
 			if (controller.getModel().checkGameState() == GameState.WON) {
 				EventMessage soundMsg = new EventMessage(C.Tag.TO_SELF,
 						C.Msg.SOUND_WIN);
 				EventBus.INSTANCE.broadcast(soundMsg);
 				gameCompletedTimer.start(1500);
 				controller.getModel().stopTimer();
 				Gdx.app.log(TAG, "Timer started! game won");
 
 			} else if (controller.getModel().checkGameState() == GameState.LOST) {
 				EventMessage soundMsg = new EventMessage(C.Tag.TO_SELF,
 						C.Msg.SOUND_LOST);
 				EventBus.INSTANCE.broadcast(soundMsg);
 				gameCompletedTimer.start(1500);
 			}
 			if (gameCompletedTimer.isDone()) {
 				Gdx.app.log(TAG, "Brodcasting gameresult! timer done");
 				EventMessage message = new EventMessage(C.Tag.TO_SELF,
 						C.Msg.GAME_RESULT, controller.getModel()
 								.getGameResult());
 				EventBus.INSTANCE.broadcast(message);
 			}
 		}
 
 		// TODO nullpointer movingShape
 		if (input.isTouchedDown()) {
 			for (GraphicalShape s : shapes) {
 				if (s.getBounds().contains(input.x, input.y)
 						&& !s.getShape().isLocked()) {
 					movingShape = s;
 				}
 			}
 		}
 
 		if (input.isTouchedUp()) {
 			if (movingShape != null) {
 				for (GraphicalShape lock : locks) {
 					snapIntoPlace(movingShape, lock);
 				}
 				sendToTeamMate(movingShape);
 				movingShape = null;
 			}
 		}
 
 		if (input.isDragged()) {
 			if (movingShape != null) {
 				if (!movingShape.getShape().isLocked()) {
 					movingShape.moveShape(input.x
 							- movingShape.getBounds().width / 2, input.y
 							- movingShape.getBounds().height / 2);
 				}
 			}
 		}
 	}
 
 	// TODO : Adds a new shape if any shape has changed color.
 	private void updateShapesFromModel() {
 
 		Map<Integer, Shape> latestModelReceivedShape = shapeGameModel
 				.getLatestReceivedShape(player_num);
 
 		// if(!shapeGameModel.getLatestReceivedShape(player_num).isEmpty()){
 		// Gdx.app.log(TAG, shapeGameModel
 		// .getLatestReceivedShape(player_num).toString() + "");
 		// }
 
 		if (!latestModelReceivedShape.isEmpty()) {
 			if (!latestModelReceivedShape.equals(latestAddedShape)) {
 				for (Map.Entry<Integer, Shape> entry : latestModelReceivedShape
 						.entrySet()) {
 					showShapeFromSender(entry.getValue(), entry.getKey());
 					latestAddedShape = latestModelReceivedShape;
 				}
 			}
 
 		}
 
 		// Removes shapes that are no longer part of the model
 		if (controller.getModel().getLatestSentShapes(player_num).size() >= 1)
 			latestRemovedShape = controller
 					.getModel()
 					.getLatestSentShapes(player_num)
 					.get(controller.getModel().getLatestSentShapes(player_num)
 							.size() - 1);
 		if (latestRemovedShape != null) {
 			List<GraphicalShape> removeList = new ArrayList<GraphicalShape>();
 			for (GraphicalShape gs : shapes) {
 				if (latestRemovedShape.equals(gs.getShape())) {
 					if (!controller.getModel().getAllInventory()
 							.get(player_num).contains(latestRemovedShape)) {
 						removeList.add(gs);
 						Gdx.app.log(TAG, "Added to removeList" + gs.getShape());
 					}
 				}
 			}
 			for (GraphicalShape gs : removeList)
 				shapes.remove(gs);
 		}
 		// Adds shapes to the gui that are no longer part
 		// of the model.
 		// for (Shape s : shapeGameModel.getAllInventory().get(player_num)) {
 		// if (!shapes.contains(new GraphicalShape(s))) {
 		// shapes.add(new GraphicalShape(s));
 		// Gdx.app.log(TAG, "new Shape!");
 		// }
 		// }
 
 		// Removes shapes that are no longer parts of the
 		// model.
 		// List<GraphicalShape> removeList = new ArrayList<GraphicalShape>();
 		// for (GraphicalShape gs : shapes) {
 		// if (!shapeGameModel.getAllInventory().get(player_num)
 		// .contains(gs.getShape())) {
 		// removeList.add(gs);
 		// Gdx.app.log(TAG, "Shape removed!");
 		// }
 		// }
 		//
 		// for (GraphicalShape gs : removeList)
 		// shapes.remove(gs);
 
 	}
 
 	public boolean snapIntoPlace(GraphicalShape shape, GraphicalShape lock) {
 		boolean result = false;
 		if (shape.getBounds().overlaps(lock.getBounds())) {
 			if (shapeGameModel.shapeFitIntoLock(player_num, shape.getShape(),
 					lock.getShape())) {
 				shape.moveShape(lock.getBounds().x, lock.getBounds().y);
 				// shape.getShape().setLocked(true);
 				result = true;
 				Gdx.app.log(TAG, "Animated" + "x=" + lock.getBounds().x + "y="
 						+ lock.getBounds().getY());
 
 			}
 			List<Object> content = new ArrayList<Object>();
 			content.add(player_num);
 			content.add(lock.getShape());
 			content.add(shape.getShape());
 
 			// Received by ShapeGameController.
 			EventBus.INSTANCE.broadcast(new EventMessage(Player.getInstance()
 					.getMac(), C.Tag.TO_SELF, C.Msg.LOCK_ATTEMPT, controller
 					.getModel().getGameId(), content));
 		}
 
 		return result;
 
 	}
 
 	@Override
 	public void removed() {
 		super.removed();
 		shapeRenderer.dispose();
 		sound.unregister();
 		controller.unregister();
 	}
 
 	/**
 	 * Used to move the shape to appear as if it was sent by the proper sender
 	 * 
 	 * @param shape
 	 *            That was sent
 	 * @param sender
 	 * @return <code>true</code> everything went according to plan, sit back and
 	 *         relax. <code>false</code> something went wrong, run around and
 	 *         scream in utter terror
 	 */
 	public boolean showShapeFromSender(Shape shape, int sender) {
 		GraphicalShape receivedShape = new GraphicalShape(shape);
 		if (!otherPlayers.contains(sender + 1))
 			return false;
 
 		// for (GraphicalShape s : shapes) {
 		// if (s.getShape().equals(shape))
 		// receivedShape = s;
 		// }
 		Gdx.app.log(TAG, "Shapes being added");
 
 		shapes.add(receivedShape);
 
 		if (otherPlayers.get(0) == sender + 1) {
 			receivedShape.moveShape(Constants.SCREEN_WIDTH / 2,
 					Constants.SCREEN_HEIGHT - 110);
 		} else if (otherPlayers.get(1) == sender + 1) {
 			receivedShape.moveShape(110, Constants.SCREEN_HEIGHT / 2);
 		} else if (otherPlayers.get(2) == sender + 1) {
 			receivedShape.moveShape(Constants.SCREEN_HEIGHT / 2,
 					Constants.SCREEN_WIDTH - 110);
 		}
 
 		return true;
 	}
 }
