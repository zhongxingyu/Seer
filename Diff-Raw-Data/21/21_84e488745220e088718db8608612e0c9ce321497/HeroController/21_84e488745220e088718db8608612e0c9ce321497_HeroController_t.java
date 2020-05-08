 package se.chalmers.tda367.group15.game.controllers;
 
 import java.awt.geom.Rectangle2D.Float;
 import java.util.List;
 import java.util.Map;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 
 import se.chalmers.tda367.group15.game.models.AbstractCharacterModel;
 import se.chalmers.tda367.group15.game.models.AbstractMeleeWeapon;
 import se.chalmers.tda367.group15.game.models.AbstractMovingModel;
 import se.chalmers.tda367.group15.game.models.AbstractProjectileModel;
 import se.chalmers.tda367.group15.game.models.AbstractRangedWeaponModel;
 import se.chalmers.tda367.group15.game.models.AbstractWeaponModel;
 import se.chalmers.tda367.group15.game.models.BulletModel;
 import se.chalmers.tda367.group15.game.models.HeroModel;
 import se.chalmers.tda367.group15.game.views.HeroView;
 
 public class HeroController extends AbstractMovingModelController {
 
 	private boolean goingUp;
 	private boolean goingDown;
 	private boolean goingLeft;
 	private boolean goingRight;
 	private long timer = 0;
 
 	/**
 	 * Create a new controller for the hero.
 	 * 
 	 * @param gameController
 	 * 
 	 */
 	public HeroController(GameController gameController) {
 		super(gameController);
 		setModel(new HeroModel());
 		setView(new HeroView(getModel()));
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void update(GameContainer container, int delta,
 			List<Float> staticBounds,
 			Map<AbstractMovingModel, Float> dynamicBounds) {
 
 		AbstractCharacterModel model = (AbstractCharacterModel) getModel();
 		Input input = container.getInput();
 		float mouseX = input.getMouseX();
 		float mouseY = input.getMouseY();
 		
 		// Change weapons
 		
 		if(input.isKeyPressed(Input.KEY_1)){
 			model.setCurrentWeapon(model.getWeapons().get(0));
 		} else if(input.isKeyPressed(Input.KEY_2)){
 			model.setCurrentWeapon(model.getWeapons().get(1));
 		} else if(input.isKeyPressed(Input.KEY_3)){
 			model.setCurrentWeapon(model.getWeapons().get(2));
 		}
		
		AbstractWeaponModel weapon = model.getCurrentWeapon();

 
 		// Fire bullets if mouse clicked and a ranged weapon is equipped
 		if (input.isMousePressed(Input.MOUSE_LEFT_BUTTON)) {
 			if (weapon instanceof AbstractRangedWeaponModel) {
 				createBullet();
 			} else if (weapon instanceof AbstractMeleeWeapon) {
 				swingWeapon();
 			}
 			timer = System.currentTimeMillis();
 		} else if (input.isMouseButtonDown(Input.MOUSE_LEFT_BUTTON)
 				&& System.currentTimeMillis() - timer > model
 						.getCurrentWeapon().getFiringSpeed()) {
 
 			timer = System.currentTimeMillis();
 
 			if (weapon instanceof AbstractRangedWeaponModel) {
 				createBullet();
 			} else if (weapon instanceof AbstractMeleeWeapon) {
 				swingWeapon();
 			}
 		}
 
 		// Calculate facing depending on where the mouse is relative
 		// to the center of the hero
 		model.setRotation(Math.toDegrees(Math.atan2((model.getHeight() / 2
 				+ model.getY() - mouseY),
 				(model.getWidth() / 2 + model.getX() - mouseX))));
 
 		goingUp = input.isKeyDown(Input.KEY_W) || input.isKeyDown(Input.KEY_UP);
 		goingDown = input.isKeyDown(Input.KEY_S)
 				|| input.isKeyDown(Input.KEY_DOWN);
 		goingRight = input.isKeyDown(Input.KEY_D)
 				|| input.isKeyDown(Input.KEY_RIGHT);
 		goingLeft = input.isKeyDown(Input.KEY_A)
 				|| input.isKeyDown(Input.KEY_LEFT);
 
 		// Calculate move direction and move
 		float speedY = (goingUp ? 1 : 0) - (goingDown ? 1 : 0);
 		float speedX = (goingLeft ? 1 : 0) - (goingRight ? 1 : 0);
 
 		if (speedY != 0 || speedX != 0) {
 			double direction = Math.atan2(speedY, speedX);
 			speedY = (float) (model.getVelocity() * Math.sin(direction));
 			speedX = (float) (model.getVelocity() * Math.cos(direction));
 		}
 
 		float newX = model.getX() - (delta * speedX);
 		float newY = model.getY() - (delta * speedY);
 
 		if (!isCollision(newX, model.getY(), model.getHeight(),
 				model.getWidth(), staticBounds, dynamicBounds)) {
 			model.setX(newX);
 		}
 
 		if (!isCollision(model.getX(), newY, model.getHeight(),
 				model.getWidth(), staticBounds, dynamicBounds)) {
 			model.setY(newY);
 		}
 
 		model.setMoving(speedY != 0 || speedX != 0);
 		RoomsController roomsController = getGameController().getRoomController();
 		float tmpX = model.getX() + model.getWidth() / 2;
 		float tmpY = model.getY() + model.getHeight() / 2;
 
 		if(tmpX <= 0) {
 			roomsController.moveLeft();
 			model.setX(container.getWidth() - model.getWidth());
 		}else if(tmpX >= container.getWidth()) {
 			roomsController.moveRight();
 			model.setX(0);
 		}else if(tmpY <= 0) {
 			roomsController.moveUp();
 			model.setY(container.getHeight());
 		}else if(tmpY >= container.getHeight()) {
 			roomsController.moveDown();
 			model.setY(0);
 		}
 
 	}
 
 	private void createBullet() {
 		AbstractCharacterModel model = (AbstractCharacterModel) getModel();
 		AbstractProjectileModel newBullet = new BulletModel();
 
 		float heroAngle = (float) Math.toRadians(model.getRotation());
 		float heroMiddleX = model.getX() + model.getWidth() / 2;
 		float heroMiddleY = model.getY() + model.getHeight() / 2;
 
		// +15 pixels in end of expression to make bullet appear outside
 		// hero's collision box
 		float heroFaceX = heroMiddleX - (float) Math.cos(heroAngle)
				* ((model.getWidth() / 2) + 15);
 		float heroFaceY = heroMiddleY - (float) Math.sin(heroAngle)
				* ((model.getHeight() / 2) + 15);
 
 		// *3 pixels compensating for the width and height of the bullet
 		newBullet.setX(heroFaceX + (float) Math.sin(heroAngle) * 3);
 		newBullet.setY(heroFaceY + (float) Math.cos(heroAngle) * 3);
 		newBullet.setRotation(model.getRotation());
 		newBullet.setDamage(model.getCurrentWeapon().getDamage());
 		newBullet.setAlive(true);
 		AbstractRoomController currentRoom = getGameController()
 				.getRoomController().getCurrentRoom();
 		currentRoom.addProjectile(newBullet);
 	}
 
 	private void swingWeapon() {
 		AbstractCharacterModel model = (AbstractCharacterModel) getModel();
 		
 		// Run the swinging animation for the weapon
 		AbstractMeleeWeapon weapon = (AbstractMeleeWeapon)model.getCurrentWeapon();
 		HeroView view = (HeroView)getView();
 		view.runAnimation(weapon.getSwingAnimation());
 		
 //		AbstractProjectileModel newSwing = new MeleeSwingModel();
 		
 		float heroAngle = (float) Math.toRadians(model.getRotation());
 		float heroMiddleX = model.getX() + model.getWidth() / 2;
 		float heroMiddleY = model.getY() + model.getHeight() / 2;
 
 
 		float heroFaceX = heroMiddleX - (float) Math.cos(heroAngle)
 				* ((model.getWidth() / 2) + 12);
 		float heroFaceY = heroMiddleY - (float) Math.sin(heroAngle)
 				* ((model.getHeight() / 2) + 12);
 
 //		newSwing.setX(heroFaceX + (float) Math.sin(heroAngle));
 //		newSwing.setY(heroFaceY + (float) Math.cos(heroAngle));
 //		newSwing.setRotation(model.getRotation());
 //		newSwing.setDamage(model.getCurrentWeapon().getDamage());
 //		newSwing.setAlive(true);
 	
 		AbstractRoomController currentRoom = getGameController()
 				.getRoomController().getCurrentRoom();
 //		currentRoom.addSwing(newSwing);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void render(GameContainer container, Graphics g)
 			throws SlickException {
 		getView().render(container, g);
 	}
 }
