 package pylos.view.appstate;
 
 import pylos.Pylos;
 import pylos.controller.GameController;
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
 import com.jme3.input.controls.ActionListener;
 import com.jme3.input.controls.MouseButtonTrigger;
 import com.jme3.scene.Node;
 
 public class ActionManager extends AbstractAppState implements ActionListener {
 	static final int MaxClickTime = 250; // ms
 	static final String LeftClick = "LeftClick";
 	static final String RightClick = "RightClick";
 
 	View view;
 	Collisions collisions;
 	HighlightBallGraphics highlightBall = new HighlightBallGraphics();
 	Node highlightBallNode = new Node("Highlight Ball");
 	private long lastLeftClick, lastRightClick;
 
 	@Override
 	public void initialize(AppStateManager stateManager, Application app) {
 		super.initialize(stateManager, app);
 		view = Pylos.view;
 		view.getRootNode().attachChild(highlightBallNode);
 
 		initListener();
 	}
 
 	// Action Listener part: listen to clicks
 	private void initListener() {
 		InputManager input = view.getInputManager();
 		input.addMapping(LeftClick, new MouseButtonTrigger(0));
 		input.addMapping(RightClick, new MouseButtonTrigger(1));
 		input.addListener(this, LeftClick, RightClick);
 	}
 
 	public boolean getCollisions(Node target) {
 		collisions = new Collisions(view, target);
 		return collisions.any();
 	}
 
 	public boolean getCollisions() {
 		Node target;
 		switch (Model.currentPlayer.action) {
 		case PLACE:
 			target = view.positionBalls;
 			break;
 		case MOUNT:
 			target = view.positionsToMountBall;
 			break;
 		case REMOVE:
 			target = view.balls;
 			break;
 		default:
 			return false;
 		}
 		return getCollisions(target);
 	}
 
 	@Override
 	public void update(float tpf) {
 		if (Model.currentPlayer.isPlacing() && getCollisions()) {
 			highlightBall.setMaterial(Model.currentPlayer.graphics.ballMaterial);
 			view.board.place(highlightBall, collisions.getPosition());
 			highlightBallNode.attachChild(highlightBall);
 		} else {
 			highlightBallNode.detachChild(highlightBall);
 		}
 	}
 
 	public void onAction(String action, boolean pressed, float tpf) {
		if (!Model.currentPlayer.canMakeMove())
 			return;
 
 		long time = System.currentTimeMillis();
 		if (action == LeftClick) {
 			if (pressed) {
 				lastLeftClick = time;
 			} else if (time - lastLeftClick < MaxClickTime && getCollisions()) {
 				if (Model.currentPlayer.isPlacing()) { // Place a ball
 					Position pos = collisions.getPosition();
 					if (Model.canPlaceBallAt(pos))
 						GameController.placePlayerBall(collisions.getPosition());
 				} else if (Model.currentPlayer.isRemoving()) { // Remove the first ball
 					Ball ball = collisions.getBall();
 					if (ball.isRemovableByCurrentPlayer())
 						GameController.removePlayerBall(ball, false);
 				}
 			}
 		} else if (action == RightClick) {
 			if (pressed) {
 				lastRightClick = time;
 			} else if (time - lastRightClick < MaxClickTime && getCollisions(view.balls)) {
 				Ball ball = collisions.getBall();
 				if (Model.currentPlayer.isMounting()) { // Mount a ball
 					if (ball.isMountableByCurrentPlayer())
 						GameController.mountPlayerBall(ball);
 				} else if (Model.currentPlayer.isRemoving()) { // Remove the last ball to remove
 					if (ball.isRemovableByCurrentPlayer())
 						GameController.removePlayerBall(ball, true);
 				}
 			}
 		}
 	}
 }
