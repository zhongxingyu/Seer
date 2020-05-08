 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.tiled.TiledMap;
 
 public class Player {
 


 	private int xLocation;
 	private int yLocation;
 	private float verticalSpeed;
 	private float horizontalSpeed;
 	private boolean isJumping;
 
 	private PlayerMapCollisionChecker mapCollisionChecker;
 	private PlayerAnimationComponent animationComponent;
 
 	public Player(TiledMap tiledMap, StartingPoint startingPoint)
 			throws SlickException {
 
 		mapCollisionChecker = new PlayerMapCollisionChecker(tiledMap,
 				startingPoint);
 		animationComponent=new PlayerAnimationComponent();
 		
 
 		xLocation = startingPoint.getX();
 		yLocation = startingPoint.getY();
 
 		verticalSpeed = 0;
 		horizontalSpeed = 0;
 
 		isJumping = true;
 
 	}
 
 	public Point broadcastLocation() {
 		return new Point(xLocation, yLocation);
 	}
 
 	public void render() {
 		animationComponent.render();
 	}
 
 
 	public void update(Input input, int delta) {
 
 		animationComponent.updateAnimtaionLocation(xLocation, yLocation);
 		animationComponent.updateHorizontalSpeedForJumping(horizontalSpeed);
 		animationComponent.updateIsJumpingForAnimations(isJumping);
 		checkIfPlayerIsJumping();
 		checkInputFromKeyboard(input);
 		ifPlayerIsJumpingAddGravity(delta);
 		updateLocationWithCurrentVelocity();
 		
 
 	}
 
 	
 	private void checkInputFromKeyboard(Input input) {
 		if (ifUpKeyDown(input)) {
 			if (ifPlayerCanMoveUp())
 				verticalSpeed -= Constant.PLAYER_JUMP_THRUST;
 		}
 
 		if (ifDownKeyDown(input)) {
 			if (ifPlayerCanMoveDown())
 				verticalSpeed += Constant.PLAYER_FALL_THRUST;
 		}
 
 		if (ifLeftKeyDown(input)) {
 			if (ifPlayerCanMoveLeft())
 				if (horizontalSpeedIsNotTooFastToTheLeft())
 					horizontalSpeed -= Constant.PLAYER_SIDE_MOMENTUM;
 		}
 
 		if (ifRightKeyDown(input)) {
 			if (ifPlayerCanMoveRight())
 				if (horizontalSpeedIsNotTooFastToTheRight())
 					horizontalSpeed += Constant.PLAYER_SIDE_MOMENTUM;
 		}
 		
 	}
 
 	private void ifPlayerIsJumpingAddGravity(int delta) {
 		if (isJumping == true) {
 			verticalSpeed += (Constant.GRAVITY * delta);
 		}
 		
 	}
 
 	private void updateLocationWithCurrentVelocity() {
 		xLocation += horizontalSpeed;
 		yLocation += verticalSpeed;
 		
 	}
 
 	private void checkIfPlayerIsJumping() {
 		if (mapCollisionChecker.isNotBlockedInAnyDirection(new Point(xLocation,yLocation), horizontalSpeed,verticalSpeed))
 			isJumping = true;
 		else {
 			isJumping = false;
 			verticalSpeed = 0;
 			calculateHorizontalFriction();
 		}
 		
 	}
 
 	private boolean horizontalSpeedIsNotTooFastToTheRight() {
 		if (horizontalSpeed<Constant.PLAYER_HORIZONTAL_TERMINAL_VELOCITY)
 			return true;
 		else
 			return false;
 	}
 
 	private boolean horizontalSpeedIsNotTooFastToTheLeft() {
 		if (horizontalSpeed>-Constant.PLAYER_HORIZONTAL_TERMINAL_VELOCITY)
 			return true;
 		else
 			return false;
 	}
 
 	private boolean ifPlayerCanMoveRight() {
 		if (!mapCollisionChecker.isBlockedRight(new Point(xLocation, yLocation),horizontalSpeed))
 			return true;
 		else
 			return false;
 
 	}
 
 	private boolean ifPlayerCanMoveLeft() {
 		if (!mapCollisionChecker.isBlockedLeft(new Point(xLocation, yLocation),horizontalSpeed))
 			return true;
 		else
 			return false;
 	}
 
 	private boolean ifPlayerCanMoveDown() {
 		if (!mapCollisionChecker.isBlockedDown(new Point(xLocation, yLocation),verticalSpeed))
 			return true;
 		else
 			return false;
 	}
 
 	private boolean ifPlayerCanMoveUp() {
 		if (!mapCollisionChecker.isBlockedUp(new Point(xLocation, yLocation),verticalSpeed)
 				&& (isJumping == false))
 			return true;
 		else
 			return false;
 	}
 
 	private boolean ifUpKeyDown(Input input) {
 		if (input.isKeyDown(Input.KEY_UP))
 			return true;
 		else
 			return false;
 	}
 
 	private boolean ifDownKeyDown(Input input) {
 		if (input.isKeyDown(Input.KEY_DOWN))
 			return true;
 		else
 			return false;
 	}
 
 	private boolean ifLeftKeyDown(Input input) {
 		if (input.isKeyDown(Input.KEY_LEFT))
 			return true;
 		else
 			return false;
 	}
 
 	private boolean ifRightKeyDown(Input input) {
 		if (input.isKeyDown(Input.KEY_RIGHT))
 			return true;
 		else
 			return false;
 	}
 
 	private void calculateHorizontalFriction() {
 		if (horizontalSpeed > 0) {
 			horizontalSpeed -= Constant.PLAYER_SIDE_MOMENTUM * 1.2;
 		}
 		if (horizontalSpeed < 0) {
 			horizontalSpeed += Constant.PLAYER_SIDE_MOMENTUM * 1.2;
 		}
 		if (horizontalSpeed == 0) {
 
 		}
 
 	}
 
 	
 
 }
