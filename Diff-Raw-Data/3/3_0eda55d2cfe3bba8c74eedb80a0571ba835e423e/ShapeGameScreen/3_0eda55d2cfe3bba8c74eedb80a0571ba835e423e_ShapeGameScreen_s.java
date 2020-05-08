 package it.chalmers.tendu.screens;
 
 import it.chalmers.tendu.Tendu;
 import it.chalmers.tendu.controllers.InputController;
 import it.chalmers.tendu.controllers.ShapeGameModelController;
 import it.chalmers.tendu.defaults.Constants;
 import it.chalmers.tendu.defaults.TextLabels;
 import it.chalmers.tendu.gamemodel.GameState;
 import it.chalmers.tendu.gamemodel.MiniGame;
 import it.chalmers.tendu.gamemodel.SimpleTimer;
 import it.chalmers.tendu.gamemodel.shapesgame.NetworkShape;
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
 import com.badlogic.gdx.graphics.g2d.BitmapFont;
 import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
 import com.badlogic.gdx.math.Vector2;
 
 public class ShapeGameScreen extends GameScreen {
 
 	public final String TAG = this.getClass().getName();
 	private final int SEND_MARGIN = 5;
 	private int timeForInstructions;
 
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
 
 	private TextWidget instructions;
 	private TextWidget teamInstructions;
 	private TextWidget teamInstructionsContinued;
 	private BitmapFont font;
 
 	private SimpleTimer gameCompletedTimer;
 	private SimpleTimer instructionsTimer; // used to time how long the
 											// instructions should be displayed
 	private List<Integer> otherPlayers;
 
 	private static int timesPlayed = 0;
 
 	//
 	// // For debug
 	// int count = 0;
 
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
 		instructionsTimer = new SimpleTimer();
 
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
 
 		otherPlayers = controller.getModel().getOtherPlayerNumbers();
 
 		// setup the instructions
 		instructions = new TextWidget(TextLabels.PLACE_THE_SHAPE, new Vector2(
 				90, 595), Constants.MENU_FONT_COLOR);
 
 		teamInstructions = new TextWidget(TextLabels.SEND_SHAPE_BY,
 				new Vector2(65, 400), Constants.MENU_FONT_COLOR);
 		teamInstructionsContinued = new TextWidget(
 				TextLabels.SEND_SHAPE_TEAMMATES, new Vector2(65, 300),
 				Constants.MENU_FONT_COLOR);
 		// load the font
 		font = new BitmapFont(Gdx.files.internal("fonts/menuFont.fnt"),
 				Gdx.files.internal("fonts/menuFont.png"), false);
 
 		// hack, we only need to show the instructions once
 		if (timesPlayed > 0) {
 			timeForInstructions = 0;
 		} else {
 			timeForInstructions = 3500;
 			timesPlayed++;
 		}
 	}
 
 	/** All graphics are drawn here */
 	@Override
 	public void render() {
 
 		if (model.hasStarted()) {
 			super.render();
 			instructionsTimer.start(timeForInstructions);
 			if (!instructionsTimer.isDone()) {
 				instructions.draw(tendu.spriteBatch, font);
 				if (otherPlayers.size() > 0) {
 					teamInstructions.draw(tendu.spriteBatch, font);
 					teamInstructionsContinued.draw(tendu.spriteBatch, font);
 				}
 
 			} else {
 				controller.getModel().startGameTimer();
 				if (shapeGameModel.checkGameState() == GameState.RUNNING
 						|| gameCompletedTimer.isRunning()) {
 					shapeRenderer
 							.setProjectionMatrix(tendu.getCamera().combined);
 					// Renders locks
 					for (GraphicalShape sgs : locks) {
 						sgs.render(shapeRenderer);
 					}
 					// Renders shapes
 					for (GraphicalShape sgs : shapes) {
 						sgs.render(shapeRenderer);
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * @param s
 	 */
 	private void sendToTeamMate(GraphicalShape s) {
 		Gdx.app.log(TAG, "SHAPE SENDING!!!!!!!!");
 		if (s.getBounds().y + s.HEIGHT >= Constants.SCREEN_HEIGHT - SEND_MARGIN
 				&& otherPlayers.size() >= 1) {
 			EventBus.INSTANCE.broadcast(new EventMessage(/*
 														 * Player.getInstance()
 														 * .getMac(),
 														 */C.Tag.TO_SELF,
 					C.Msg.SHAPE_SENT, controller.getModel().getGameId(),
 					messageContentFactory(controller.getModel()
 							.getOtherPlayerNumbers().get(0), s.getShape())));
 		} else if (s.getBounds().x <= SEND_MARGIN && otherPlayers.size() >= 2) {
 			EventBus.INSTANCE.broadcast(new EventMessage(/*
 														 * Player.getInstance()
 														 * .getMac(),
 														 */C.Tag.TO_SELF,
 					C.Msg.SHAPE_SENT, controller.getModel().getGameId(),
 					messageContentFactory(otherPlayers.get(1), s.getShape())));
 		} else if (s.getBounds().x + s.WIDTH >= Constants.SCREEN_WIDTH
 				- SEND_MARGIN
 				&& otherPlayers.size() >= 3) {
 			EventBus.INSTANCE.broadcast(new EventMessage(/*
 														 * Player.getInstance()
 														 * .getMac(),
 														 */C.Tag.TO_SELF,
 					C.Msg.SHAPE_SENT, controller.getModel().getGameId(),
 					messageContentFactory(otherPlayers.get(2), s.getShape())));
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
 	private NetworkShape messageContentFactory(int player, Shape shape) {
 
 		// List<Object> l = new ArrayList<Object>();
 		// l.add(player);
 		// l.add(shape);
 		// return l;
 
 		// return Player.getInstance();
 		return new NetworkShape(player, shape);
 		// return 1;
 	}
 
 	/** All game logic goes here (within the model...) */
 	@Override
 	public void tick(InputController input) {
 		updateShapesFromModel();
 		if (model.hasStarted()) {
 			if (instructionsTimer.isDone()) {
 				if (gameCompletedTimer.isDone()) {
 					Gdx.app.log(TAG, "Brodcasting gameresult! timer done");
 					// Received by GameSessionController
 					sendEndMessage();
 				} else if (!gameCompletedTimer.isRunning()) {
 					if (controller.getModel().checkGameState() == GameState.WON) {
 						gameCompletedTimer.start(750);
 						controller.getModel().stopTimer();
 						Gdx.app.log(TAG, "Timer started! game won");
 					} else if (controller.getModel().checkGameState() == GameState.LOST) {
 						gameCompletedTimer.start(1500);
 					}
 
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
 	}
 
 	public boolean snapIntoPlace(GraphicalShape shape, GraphicalShape lock) {
 		boolean result = false;
 		if (shape.getBounds().overlaps(lock.getBounds())) {
 			if (shapeGameModel.shapeFitIntoLock(player_num, shape.getShape(),
 					lock.getShape())) {
 				shape.moveShape(lock.getBounds().x, lock.getBounds().y);
 				result = true;
 				Gdx.app.log(TAG, "Animated" + "x=" + lock.getBounds().x + "y="
 						+ lock.getBounds().getY());
 
 			}
 			List<Object> content = new ArrayList<Object>();
 			content.add(player_num);
 			content.add(lock.getShape());
 			content.add(shape.getShape());
 
 			// Received by ShapeGameController.
 			EventBus.INSTANCE.broadcast(new EventMessage(/*
 														 * Player.getInstance()
 														 * .getMac(),
 														 */C.Tag.TO_SELF,
 					C.Msg.LOCK_ATTEMPT, controller.getModel().getGameId(),
 					content));
 		}
 
 		return result;
 
 	}
 
 	@Override
 	public void removed() {
 		super.removed();
 		shapeRenderer.dispose();
 		sound.unregister();
 		controller.unregister();
 		font.dispose();
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
 		if (!otherPlayers.contains(sender))
 			return false;
 
 		shapes.add(receivedShape);
 
 		if (otherPlayers.get(0) == sender) {
 			receivedShape.moveShape(Constants.SCREEN_WIDTH / 2,
 					Constants.SCREEN_HEIGHT - 110);
 		} else if (otherPlayers.get(1) == sender) {
 			receivedShape.moveShape(110, Constants.SCREEN_HEIGHT / 2);
 		} else if (otherPlayers.get(2) == sender) {
			receivedShape.moveShape(Constants.SCREEN_HEIGHT / 2,
					Constants.SCREEN_WIDTH - 110);
 		}
 
 		return true;
 	}
 
 	// TODO not the best solution but it works.
 	// this message must be sent only once
 	private boolean ended = false;
 
 	private void sendEndMessage() {
 		if (!ended) {
 			// Received by GameSessionController.
 			EventMessage message = new EventMessage(C.Tag.TO_SELF,
 					C.Msg.GAME_RESULT, model.getGameResult());
 			EventBus.INSTANCE.broadcast(message);
 		}
 
 		ended = true;
 	}
 }
