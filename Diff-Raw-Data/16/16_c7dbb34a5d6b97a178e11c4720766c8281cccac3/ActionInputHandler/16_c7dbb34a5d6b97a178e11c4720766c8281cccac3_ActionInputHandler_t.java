 package projectrts.controller;
 
 import projectrts.model.IGame;
 import projectrts.model.abilities.AttackAbility;
 import projectrts.model.abilities.GatherResourceAbility;
 import projectrts.model.abilities.IAbility;
 import projectrts.model.abilities.IBuildStructureAbility;
 import projectrts.model.abilities.ITargetAbility;
 import projectrts.model.abilities.MoveAbility;
 import projectrts.model.entities.AbstractPlayerControlledEntity;
 import projectrts.model.entities.EntityManager;
 import projectrts.model.entities.IEntity;
 import projectrts.model.entities.IPlayerControlledEntity;
 import projectrts.model.entities.Resource;
 import projectrts.model.world.Position;
 import projectrts.view.GameView;
 
 import com.jme3.app.SimpleApplication;
 import com.jme3.input.controls.ActionListener;
 import com.jme3.math.Vector3f;
 
 /**
  * A digital listener, use if the input is digital - i.e. it can only be either
  * "on" or "off".
  * 
  * @author Markus Ekstrom
  */
 class ActionInputHandler implements ActionListener {
 	private final SimpleApplication app;
 	private final IGame game;
 	private final GameView view;
 	private IPlayerControlledEntity selectedEntity;
 	private boolean choosingTarget;
 	private boolean choosingPosition = false;
 	private IAbility currentAbility;
 	private float buildingSize;
 
 	public ActionInputHandler(SimpleApplication app, IGame game, GameView view) {
 		this.app = app;
 		this.game = game;
 		this.view = view;
 	}
 
 	/**
 	 * Converts a Vector3f position from the world into a Position position in
 	 * model.
 	 * 
 	 * @param worldLoc
 	 *            The world position.
 	 * @return The model position in the form of a Position.
 	 */
 	public static Position convertWorldToModel(Vector3f worldLoc) {
 		float x = worldLoc.x / InGameState.MODEL_TO_WORLD;
 		float y = -worldLoc.y / InGameState.MODEL_TO_WORLD;
 		return new Position(x, y);
 	}
 	
 	/**
 	 * A digital listener, use if the input is digital - i.e. it can only be
 	 * either "on" or "off".
 	 */
 	public void onAction(String name, boolean keyPressed, float tpf) {
 		if (name.equals("exit") && keyPressed) {
 			app.stop();
 		}
 
 		if (name.equals("mouseLeftButton") && keyPressed) {
 			handleLeftClick();
 		}
 		if (name.equals("mouseRightButton") && keyPressed) {
 			handleRightClick();
 		}
 	}
 
 	private void handleLeftClick() {
 		Position pos = convertWorldToModel(app.getCamera()
 				.getWorldCoordinates(
 						app.getInputManager().getCursorPosition(), 0));
 		if (choosingPosition) {
 			if (!game.isAnyNodeOccupied(game.getWorld().getNodesAt(pos,
 					buildingSize))) {
 
 				game.getAbilityManager().doAbility(
 						currentAbility.getClass().getSimpleName(),
 						game.getWorld().getNodeAt(pos).getPosition(),
 						selectedEntity);
 				choosingPosition = false;
 				view.clearNodes();
 			}
 		} else if (choosingTarget) {
 			game.getAbilityManager().doAbility(
 					currentAbility.getClass().getSimpleName(), pos,
 					selectedEntity);
 			choosingTarget = false;
 
 		} else {
 			game.getEntityManager().select(pos, game.getHumanPlayer());
 		}
 	}
 
 	private void handleRightClick() {
 		Position click = convertWorldToModel(app.getCamera()
 				.getWorldCoordinates(
 						app.getInputManager().getCursorPosition(), 0));
 		if (choosingPosition || choosingTarget) {
 			choosingPosition = false;
 			view.clearNodes();
 			choosingTarget = false;
 		} else {
 			IEntity e = EntityManager.INSTANCE.getEntityAtPosition(click);
 			if (e == null) {
 				doMoveAbility();
 			} else {
 				if (e instanceof Resource) {
 					game.getAbilityManager().useAbilitySelected(
 							GatherResourceAbility.class.getSimpleName(), click,
 							game.getHumanPlayer());
 
 				} else if (e instanceof AbstractPlayerControlledEntity) {
 					AbstractPlayerControlledEntity pce = (AbstractPlayerControlledEntity) e;
 					if (pce.getOwner().equals(game.getHumanPlayer())) {
 						doMoveAbility();
 					} else {
 						game.getAbilityManager().useAbilitySelected(
 								AttackAbility.class.getSimpleName(),
 								pce.getPosition(), game.getHumanPlayer());
 					}
 				} else {
 					doMoveAbility();
 				}
 			}
 		}
 	}
 
 	private void doMoveAbility() {
 		game.getAbilityManager().useAbilitySelected(
 				MoveAbility.class.getSimpleName(),
 				convertWorldToModel(app.getCamera()
 						.getWorldCoordinates(
 								app.getInputManager().getCursorPosition(), 0)),
 				game.getHumanPlayer());
 	}
 
 	/**
 	 * Selects an ability
 	 * 
 	 * @param ability
 	 *            the ability to become selected
 	 */
 	public void selectAbility(IAbility ability, IPlayerControlledEntity e) {
 		if (e.getOwner().equals(game.getHumanPlayer())) {
 			IPlayerControlledEntity pce = (IPlayerControlledEntity) e;
 			currentAbility = ability;
 			selectedEntity = pce;
 			choosingPosition = false;
 			choosingTarget = false;
 			if (currentAbility instanceof IBuildStructureAbility) {
 				choosingPosition = true;
 				IBuildStructureAbility ab = (IBuildStructureAbility) ability;
 				buildingSize = ab.getSizeOfBuilding();
 			} else if (currentAbility instanceof ITargetAbility) {
 				choosingTarget = true;
 			} else {
 				game.getAbilityManager().doAbility(
 						currentAbility.getClass().getSimpleName(),
 						pce.getPosition(), pce);
 			}
 		}
 	}
 
 	public boolean isChoosingPosition() {
 		return choosingPosition;
 	}
 
 	/**
 	 * If the player is choosing position (i.e. isChoosingPosition returns true)
 	 * for a building, then this method returns that building's size.
 	 * 
 	 * @return A float representing the building's size.
 	 */
 	public float getBuildingSize() {
 		return buildingSize;
 	}
 }
