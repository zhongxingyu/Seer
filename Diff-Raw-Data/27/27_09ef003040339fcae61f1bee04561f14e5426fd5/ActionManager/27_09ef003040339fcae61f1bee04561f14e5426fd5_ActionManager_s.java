 package pylos.view.appstate;
 
 import pylos.Pylos;
 import pylos.controller.Controller;
 import pylos.model.Ball;
 import pylos.model.Model;
 import pylos.model.Position;
 import pylos.view.Collisions;
 import pylos.view.View;
 import pylos.view.ball.HighlightBallGraphics;
 
 import com.jme3.app.Application;
 import com.jme3.app.state.AbstractAppState;
 import com.jme3.app.state.AppStateManager;
 import com.jme3.input.InputManager;
 import com.jme3.input.KeyInput;
 import com.jme3.input.controls.ActionListener;
 import com.jme3.input.controls.KeyTrigger;
 import com.jme3.input.controls.MouseButtonTrigger;
 import com.jme3.scene.Node;
 
 public class ActionManager extends AbstractAppState implements ActionListener {
 	static final int MaxRightClickTime = 250; // ms
 	static final String Quit = "Quit";
 	static final String PickBall = "PickBall";
 	static final String RiseBall = "RiseBall";
 
 	View view;
 
 	HighlightBallGraphics highlightBall = new HighlightBallGraphics();
 	Node highlightBallNode = new Node("Highlight Ball");
 
 	private long lastRightClick;
 
 	@Override
 	public void initialize(AppStateManager stateManager, Application app) {
 		super.initialize(stateManager, app);
 		view = Pylos.view;
 		view.getRootNode().attachChild(highlightBallNode);
 
 		initListener();
 	}
 
 	@Override
 	public void update(float tpf) {
 		Collisions collisions = getPickCollisions();
 		if (collisions != null && collisions.any()) {
 			Position position = collisions.getPosition();
 			highlightBall.setMaterial(Model.currentPlayer.graphics.ballMaterial);
 			view.board.place(highlightBall, position);
 			highlightBallNode.attachChild(highlightBall);
 		} else {
 			highlightBallNode.detachChild(highlightBall);
 		}
 	}
 
 	// Action Listener part: listen to clicks
 	private void initListener() {
 		InputManager input = view.getInputManager();
 		input.addMapping(Quit, new KeyTrigger(KeyInput.KEY_Q));
 		input.addMapping(PickBall, new MouseButtonTrigger(0)); // left-button click
 		input.addMapping(RiseBall, new MouseButtonTrigger(1)); // right-button click
 		input.addListener(this, Quit, PickBall, RiseBall);
 	}
 
 	public Collisions getPickCollisions() {
 		Node target;
 		switch (Model.currentPlayer.action) {
 		case PLACE:
 			target = view.positionBalls;
 			break;
 		case RISE:
 			target = view.positionsToRiseBall;
 			break;
 		default:
 			return null;
 		}
 		return new Collisions(view, target);
 	}
 
 	public void onAction(String action, boolean pressed, float tpf) {
 		if (action == Quit) {
 			view.stop();
 		} else if (action == PickBall) {
 			if (!pressed && !Model.isWinner()) {
 				Collisions collisions = getPickCollisions();
 				if (collisions != null && collisions.any())
 					Controller.placePlayerBall(collisions.getPosition());
 			}
 		} else if (action == RiseBall) {
 			long time = System.currentTimeMillis();
 			if (pressed) {
 				lastRightClick = time;
 			} else {
				if (time - lastRightClick < MaxRightClickTime) {
 					Collisions collisions = new Collisions(view, view.balls);
 					if (collisions.any()) {
 						Ball ball = Model.board.ballAt(collisions.getPosition());
 						if (ball.onBoard && ball.isMountableByCurrentPlayer()) {
 							Controller.risePlayerBall(ball);
 						}
 					}
 				}
 			}
 		}
 	}
 }
