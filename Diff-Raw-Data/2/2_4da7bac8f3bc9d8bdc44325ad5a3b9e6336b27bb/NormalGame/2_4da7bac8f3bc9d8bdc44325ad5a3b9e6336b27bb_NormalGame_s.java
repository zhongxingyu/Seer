 package pruebas.Renders;
 
 import pruebas.Accessors.ActorAccessor;
 import pruebas.Controllers.GameController;
 import pruebas.Controllers.WorldController;
 import pruebas.CrystalClash.CrystalClash;
 import pruebas.Entities.Cell;
 import pruebas.Entities.GridPos;
 import pruebas.Entities.Path;
 import pruebas.Entities.Unit;
 import pruebas.Entities.helpers.AttackUnitAction;
 import pruebas.Entities.helpers.MoveUnitAction;
 import pruebas.Entities.helpers.NoneUnitAction;
 import pruebas.Entities.helpers.UnitAction;
 import pruebas.Entities.helpers.UnitAction.UnitActionType;
 import pruebas.Renders.helpers.CellHelper;
 import pruebas.Renders.helpers.PathManager;
 import pruebas.Renders.helpers.ResourceHelper;
 import aurelienribon.tweenengine.BaseTween;
 import aurelienribon.tweenengine.Timeline;
 import aurelienribon.tweenengine.Tween;
 import aurelienribon.tweenengine.TweenCallback;
 import aurelienribon.tweenengine.TweenManager;
 
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.TextureAtlas;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.scenes.scene2d.Group;
 import com.badlogic.gdx.scenes.scene2d.InputEvent;
 import com.badlogic.gdx.scenes.scene2d.ui.Image;
 import com.badlogic.gdx.scenes.scene2d.ui.Skin;
 import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
 import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
 import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
 import com.badlogic.gdx.utils.Array;
 
 public class NormalGame extends GameRender {
 	private TweenManager tweenManager;
 
 	private Unit selectedUnit;
 	private Cell selectedCell;
 
 	private Image selectorArrow;
 	private float arrowX;
 	private float arrowY;
 
 	private Image actionsBar;
 	private TextButton btnAttack;
 	private TextButton btnMove;
 	private TextButton btnDefense;
 	private TextButton btnUndo;
 	private Group grpActionBar;
 	//private Label lblMoves;
 	//private Label lblAttack;
 	private boolean actionsBarVisible;
 
 	private UnitActionType actionType;
 	private int maxMoves;
 
 	private Array<MoveUnitAction> moveActions;
 	private Array<AttackUnitAction> attackActions;
 	private UnitAction unitAction;
 	private Array<Cell> ghostlyCells;
 	private Array<Unit> defensiveUnits;
 
 	private PathManager paths;
 
 	public NormalGame(WorldController world) {
 		super(world);
 
 		tweenManager = new TweenManager();
 
 		selectedUnit = null;
 		selectedCell = null;
 
 		arrowX = 0;
 		arrowY = CrystalClash.HEIGHT + 20;
 		actionsBarVisible = false;
 
 		actionType = UnitActionType.NONE;
 		maxMoves = 0;
 
 		moveActions = new Array<MoveUnitAction>();
 		ghostlyCells = new Array<Cell>();
 
 		defensiveUnits = new Array<Unit>();
 
 		attackActions = new Array<AttackUnitAction>();
 
 		paths = new PathManager();
 
 		load();
 		clearAllChanges();
 		GameEngine.hideLoading();
 	}
 
 	public void load() {
 		GameController.getInstance().loadUnitsStats();
 
 		PathManager.load();
 
 		Texture arrow = ResourceHelper.getTexture("data/Images/InGame/selector_arrow.png");
 		selectorArrow = new Image(arrow);
 		selectorArrow.setPosition(arrowX, arrowY);
 
 		TextureAtlas atlas = new TextureAtlas("data/Images/InGame/options_bar.pack");
 		Skin skin = new Skin(atlas);
 
 		TextureRegion aux = skin.getRegion("actions_hud");
 		actionsBar = new Image(aux);
 
 		TextButtonStyle attackStyle = new TextButtonStyle(
 				skin.getDrawable("action_attack_button"),
 				skin.getDrawable("action_attack_button_pressed"), null, ResourceHelper.getFont());
 		btnAttack = new TextButton("", attackStyle);
 		btnAttack.setPosition(actionsBar.getX(), actionsBar.getY() + 155);
 		btnAttack.addListener(new ClickListener() {
 			@Override
 			public void clicked(InputEvent event, float x, float y) {
 				setUnitAction(new AttackUnitAction(selectedUnit.isMelee()));
 				unitAction.origin = selectedCell;
 				showAbleToAttackCells();
 			}
 		});
 
 		TextButtonStyle defenseStyle = new TextButtonStyle(
 				skin.getDrawable("action_defensive_button"),
 				skin.getDrawable("action_defensive_button_pressed"), null, ResourceHelper.getFont());
 		btnDefense = new TextButton("", defenseStyle);
 		btnDefense.setPosition(actionsBar.getX() + 5, actionsBar.getY() + 13);
 		btnDefense.addListener(new ClickListener() {
 			@Override
 			public void clicked(InputEvent event, float x, float y) {
 				setUnitAction(new DefendUnitAction());
 				unitAction.origin = selectedCell;
 				defensiveUnits.add(selectedUnit);
 				selectedUnit.setDefendingPosition(true);
 			}
 		});
 
 		TextButtonStyle moveStyle = new TextButtonStyle(
 				skin.getDrawable("action_run_button"),
 				skin.getDrawable("action_run_button_pressed"), null, ResourceHelper.getFont());
 		btnMove = new TextButton("", moveStyle);
 		btnMove.setPosition(actionsBar.getX() + 233, actionsBar.getY() + 155);
 		btnMove.addListener(new ClickListener() {
 			@Override
 			public void clicked(InputEvent event, float x, float y) {
 				setUnitAction(new MoveUnitAction());
 				unitAction.origin = selectedCell;
 				((MoveUnitAction) unitAction).moves.add(selectedCell);
 				selectedCell.addState(Cell.MOVE_TARGET);
 
 				showAbleToMoveCells();
 			}
 		});
 
 		TextButtonStyle undoStyle = new TextButtonStyle(
 				skin.getDrawable("action_cancel_button"),
 				skin.getDrawable("action_cancel_button_pressed"), null, ResourceHelper.getFont());
 		btnUndo = new TextButton("", undoStyle);
 		btnUndo.setPosition(actionsBar.getX() + 231, actionsBar.getY() + 9);
 		btnUndo.addListener(new ClickListener() {
 			@Override
 			public void clicked(InputEvent event, float x, float y) {
 				undoAction();
 			}
 		});
 
 		//lblAttack = new Label("150", new LabelStyle(ResourceHelper.getFont(), Color.WHITE));
 		//lblAttack.setPosition(btnAttack.getX() + (btnAttack.getWidth() / 2 - lblAttack.getWidth() / 2), btnAttack.getY() + 3);
 
 		//lblMoves = new Label("5", new LabelStyle(ResourceHelper.getFont(), Color.WHITE));
 		//lblMoves.setPosition(btnMove.getX() + (btnMove.getWidth() / 2 - lblMoves.getWidth() / 2), btnMove.getY() + 3);
 
 		grpActionBar = new Group();
 		grpActionBar.addActor(actionsBar);
 		grpActionBar.addActor(btnAttack);
 		grpActionBar.addActor(btnMove);
 		grpActionBar.addActor(btnDefense);
 		grpActionBar.addActor(btnUndo);
 
 		grpActionBar.setSize(actionsBar.getWidth(), actionsBar.getHeight());
 		grpActionBar.setPosition(CrystalClash.WIDTH / 2 - grpActionBar.getWidth() / 2, CrystalClash.HEIGHT + 50);
 
 		addActor(grpActionBar);
 	}
 
 	private void moveArrow(Unit u) {
 		if (u != null) {
 			if (selectorArrow.getY() >= CrystalClash.HEIGHT) {
 				selectorArrow.setPosition(u.getX(), CrystalClash.HEIGHT + 20);
 			}
 			arrowX = u.getX();
 			arrowY = u.getY() + 120;
 		} else {
 			arrowY = CrystalClash.HEIGHT + 20;
 		}
 
 		tweenManager.killTarget(selectorArrow);
 		Timeline.createParallel()
 				.push(Tween.to(selectorArrow, ActorAccessor.X, CrystalClash.ANIMATION_SPEED)
 						.target(arrowX))
 				.push(Tween.to(selectorArrow, ActorAccessor.Y, CrystalClash.ANIMATION_SPEED)
 						.target(arrowY))
 				.setCallback(new TweenCallback() {
 					@Override
 					public void onEvent(int type, BaseTween<?> source) {
 						selectorArrow.setPosition(arrowX, arrowY);
 						arrowAnimation();
 					}
 				}).start(tweenManager);
 	}
 
 	private void arrowAnimation() {
 		Timeline.createSequence()
 				.push(Tween.set(selectorArrow, ActorAccessor.Y).target(arrowY))
 				.push(Tween.to(selectorArrow, ActorAccessor.Y, CrystalClash.ANIMATION_SPEED).target(
 						arrowY - 10))
 				.push(Tween.to(selectorArrow, ActorAccessor.Y, CrystalClash.ANIMATION_SPEED).target(
 						arrowY)).repeat(Tween.INFINITY, 0).start(tweenManager);
 	}
 
 	private void moveActionsBar(Unit u) {
 		if (u != null) {
 			UnitActionType type = selectedCell.getAction().getActionType();
 			if(type.equals(UnitActionType.PLACE) || type.equals(UnitActionType.NONE)) {
 				//btnAttack.setDisabled(false);
 				//btnDefense.setDisabled(false);
 				//btnMove.setDisabled(false);
 				//btnUndo.setDisabled(true);
 				grpActionBar.addActor(btnAttack);
 				grpActionBar.addActor(btnDefense);
 				grpActionBar.addActor(btnMove);
 				grpActionBar.removeActor(btnUndo);
 			} else {
 				//btnAttack.setDisabled(true);
 				//btnDefense.setDisabled(true);
 				//btnMove.setDisabled(true);
 				//btnUndo.setDisabled(false);
 				grpActionBar.removeActor(btnAttack);
 				grpActionBar.removeActor(btnDefense);
 				grpActionBar.removeActor(btnMove);
 				grpActionBar.addActor(btnUndo);
 			}
 			
 			if (!actionsBarVisible)
 				grpActionBar.setPosition(CellHelper.getUnitCenterX(selectedCell) - actionsBar.getWidth() / 2, CrystalClash.HEIGHT + grpActionBar.getHeight());
 
 			tweenManager.killTarget(grpActionBar);
 			Timeline.createParallel()
 					.push(Tween.to(grpActionBar, ActorAccessor.X, CrystalClash.ANIMATION_SPEED).target(CellHelper.getUnitCenterX(selectedCell) - actionsBar.getWidth() / 2))
 					.push(Tween.to(grpActionBar, ActorAccessor.Y, CrystalClash.ANIMATION_SPEED).target(selectedCell.getY() - 80))
 					.start(tweenManager);
 
 			actionsBarVisible = true;
 		}
 	}
 
 	private void hideActionsBar() {
 		actionsBarVisible = false;
 		Timeline.createParallel()
 				.push(Tween.to(grpActionBar, ActorAccessor.Y, CrystalClash.ANIMATION_SPEED).target(CrystalClash.HEIGHT + grpActionBar.getHeight()))
 				.start(tweenManager);
 	}
 
 	private Timeline pushHideActionBar(Timeline t) {
		return t.push(Tween.to(grpActionBar, ActorAccessor.Y, CrystalClash.ANIMATION_SPEED).target(CrystalClash.HEIGHT + grpActionBar.getHeight()))
 	}
 
 	private void showAbleToActionCells() {
 		switch (actionType) {
 		case PLACE:
 			break;
 		case ATTACK:
 			showAbleToAttackCells();
 			break;
 		case DEFENSE:
 			break;
 		case MOVE:
 			showAbleToMoveCells();
 			break;
 		case NONE:
 			break;
 		default:
 			break;
 		}
 	}
 
 	private void showAbleToMoveCells() {
 		clearAvailableCells();
 		actionType = UnitActionType.MOVE;
 
 		if (((MoveUnitAction) unitAction).moves.size <= maxMoves) {
 			Cell top = ((MoveUnitAction) unitAction).moves.peek();
 			boolean continueMoving = true;
 
 			if (top.Equals(unitAction.origin))
 				continueMoving = true;
 
 			if (continueMoving) {
 				int[][] cells = top.neigbours;
 				Cell aux = null;
 				for (int i = 0; i < top.neigbours.length; i++) {
 					aux = world.cellAtByGrid(cells[i][0], cells[i][1]);
 					if (!aux.hasState(Cell.MOVE_TARGET) && aux.getUnit() == null)
 						aux.addState(Cell.ABLE_TO_MOVE);
 				}
 			}
 		}
 	}
 
 	private void showAbleToAttackCells() {
 		clearAvailableCells();
 
 		showAbleToAttackCellRecursive(selectedCell, selectedUnit.isMelee(), selectedUnit.getRange(), false);
 		selectedCell.setState(Cell.State.NONE);
 	}
 
 	// Method that actually "shows" (change state) the cell where units can attack
 	private void showAbleToAttackCellRecursive(Cell cell, boolean onlyCellsWithUnit, int range, boolean hide) {
 		int[][] cells = cell.neigbours;
 
 		Unit unit;
 		Cell neigbourCell = null;
 		for (int i = 0; i < cells.length; i++) {
 			neigbourCell = world.cellAtByGrid(cells[i][0], cells[i][1]);
 			unit = neigbourCell.getUnit();
 			if (!neigbourCell.hasState(Cell.MOVE_TARGET | Cell.ATTACK_TARGET_CENTER) &&
 					(hide || (onlyCellsWithUnit && (unit == null || !unit.isEnemy())))) {
 				neigbourCell.removeState(Cell.ABLE_TO_ATTACK);
 			} else if (neigbourCell != selectedCell && (unit == null || unit.isEnemy()) &&
 					!neigbourCell.hasState(Cell.MOVE_TARGET | Cell.ATTACK_TARGET_CENTER))
 				neigbourCell.addState(Cell.ABLE_TO_ATTACK);
 
 			if (range > 1)
 				showAbleToAttackCellRecursive(neigbourCell, onlyCellsWithUnit, range - 1, hide);
 		}
 	}
 
 	private void clearAvailableCells() {
 		switch (actionType) {
 		case PLACE:
 			break;
 		case ATTACK:
 			showAbleToAttackCellRecursive(selectedCell, false, selectedUnit.getRange(), true);
 			break;
 		case DEFENSE:
 			break;
 		case MOVE:
 			Cell cell = null;
 			for (int i = 0; i < ((MoveUnitAction) unitAction).moves.size; i++) {
 				cell = ((MoveUnitAction) unitAction).moves.get(i);
 				int[][] cells = cell.neigbours;
 				for (int j = 0; j < cell.neigbours.length; j++) {
 					world.cellAtByGrid(cells[j][0], cells[j][1]).removeState(Cell.ABLE_TO_MOVE);
 				}
 			}
 			break;
 		case NONE:
 			break;
 		default:
 			break;
 		}
 	}
 
 	private void clearPathCells(Array<Cell> cells) {
 		for (int i = 0; i < cells.size; i++) {
 			cells.get(i).removeState(Cell.MOVE_TARGET);
 		}
 	}
 
 	private void clearSelection() {
 		selectedUnit = null;
 		selectedCell = null;
 		unitAction = null;
 		actionType = UnitAction.UnitActionType.NONE;
 		moveArrow(selectedUnit);
 		hideActionsBar();
 	}
 
 	private void undoAction() {
 		grpActionBar.addActor(btnAttack);
 		grpActionBar.addActor(btnDefense);
 		grpActionBar.addActor(btnMove);
 		grpActionBar.removeActor(btnUndo);
 		
 		switch (actionType) {
 		case ATTACK:
 			clearAvailableCells();
 			paths.removePath(selectedUnit);
 			((AttackUnitAction) unitAction).target.removeState(Cell.ATTACK_TARGET_CENTER);
 			attackActions.removeValue((AttackUnitAction) unitAction, false);
 
 			setUnitAction(new NoneUnitAction());
 			selectedCell.setAction(unitAction);
 			break;
 		case DEFENSE:
 			selectedUnit.setDefendingPosition(false);
 
 			setUnitAction(new NoneUnitAction());
 			selectedCell.setAction(unitAction);
 			break;
 		case MOVE:
 			clearAvailableCells();
 			paths.removePath(selectedUnit);
 			Array<Cell> moves = ((MoveUnitAction) unitAction).moves;
 			clearPathCells(moves);
 			if (moves.size > 1)
 				popUnitFromPath(moves);
 
 			//TODO: popup
 			//lblMoves.setText(maxMoves + "");
 
 			setUnitAction(new NoneUnitAction());
 			selectedCell.setAction(unitAction);
 			paths.removePath(selectedUnit);
 			break;
 		case NONE:
 			break;
 		case PLACE:
 			break;
 		default:
 			break;
 		}
 	}
 
 	private void updateActionsBar() {
 		if (undoVisible) {
 			grpActionBar.removeActor(btnAttack);
 			grpActionBar.removeActor(lblAttack);
 			grpActionBar.removeActor(btnMove);
 			grpActionBar.removeActor(lblMoves);
 			grpActionBar.removeActor(btnDefense);
 			grpActionBar.addActor(btnUndo);
 		} else {
 			grpActionBar.addActor(btnAttack);
 			grpActionBar.addActor(lblAttack);
 			grpActionBar.addActor(btnMove);
 			grpActionBar.addActor(lblMoves);
 			grpActionBar.addActor(btnDefense);
 			grpActionBar.removeActor(btnUndo);
 		}
 	}
 
 	@Override
 	public void clearAllChanges() {
 		clearSelection();
 
 		moveActions.clear();
 		ghostlyCells.clear();
 		attackActions.clear();
 
 		for (int i = 0; i < defensiveUnits.size; i++) {
 			defensiveUnits.get(i).setDefendingPosition(false);
 		}
 		defensiveUnits.clear();
 
 		setUnitAction(new NoneUnitAction());
 		for (int i = 0; i < world.cellGrid.length; i++) {
 			for (int j = 0; j < world.cellGrid[0].length; j++) {
 				world.cellGrid[i][j].setAction(unitAction);
 				world.cellGrid[i][j].state = Cell.NONE;
 			}
 		}
 	}
 
 	@Override
 	public void renderInTheBack(float dt, SpriteBatch batch) {
 		paths.render(batch, dt, Path.TYPE.MOVE);
 	}
 
 	@Override
 	public void renderInTheFront(float dt, SpriteBatch batch) {
 		paths.render(batch, dt, Path.TYPE.ATTACK);
 		selectorArrow.draw(batch, 1);
 
 		tweenManager.update(dt);
 	}
 
 	@Override
 	public boolean touchDown(float x, float y, int pointer, int button) {
 		Cell cell = world.cellAt(x, y);
 		if (cell != null) {
 			switch (actionType) {
 			case PLACE:
 				break;
 			case ATTACK:
 				if (cell.hasState(Cell.ABLE_TO_ATTACK)) {
 					if (unitAction != null && ((AttackUnitAction) unitAction).target != null)
 						((AttackUnitAction) unitAction).target.removeState(Cell.ATTACK_TARGET_CENTER);
 					cell.addState(Cell.ATTACK_TARGET_CENTER);
 					((AttackUnitAction) unitAction).target = cell;
 
 					Path p = paths.createOrResetPath(selectedUnit, Path.TYPE.ATTACK);
 					if (selectedUnit.isMelee()) {
 						PathManager.addLine(p,
 								selectedCell.getCenterX(),
 								selectedCell.getCenterY(),
 								cell.getCenterX(),
 								cell.getCenterY());
 					} else {
 						PathManager.addArc(p,
 								selectedCell.getCenterX(),
 								selectedCell.getCenterY(),
 								cell.getCenterX(),
 								cell.getCenterY());
 					}
 				} else {
 					saveAttack();
 				}
 				break;
 			case DEFENSE:
 				saveDefense();
 				break;
 			case MOVE:
 				if (cell.hasState(Cell.ABLE_TO_MOVE)) {
 					clearAvailableCells();
 					Array<Cell> moves = ((MoveUnitAction) unitAction).moves;
 					Path p = paths.getOrCreatePath(selectedUnit, Path.TYPE.MOVE);
 
 					if (moves.size == 0) {
 						PathManager.addLine(p,
 								selectedCell.getCenterX(),
 								selectedCell.getCenterY(),
 								cell.getCenterX(),
 								cell.getCenterY());
 					} else {
 						PathManager.addLine(p,
 								moves.get(moves.size - 1).getCenterX(),
 								moves.get(moves.size - 1).getCenterY(),
 								cell.getCenterX(),
 								cell.getCenterY());
 					}
 					Unit ghost;
 					if (moves.size > 1) {
 						ghost = popUnitFromPath(moves);
 					} else {
 						ghost = new Unit(selectedUnit.getName(), world.player);
 						ghost.getRender().setState(STATE.ghost);
 						ghostlyCells.add(cell);
 					}
 					cell.setUnit(ghost);
 					cell.addState(Cell.MOVE_TARGET);
 					cell.removeState(Cell.ABLE_TO_MOVE);
 
 					//TODO: popup
 					//lblMoves.setText(maxMoves - moves.size + "");
 					moves.add(cell);
 
 					showAbleToMoveCells();
 				} else if (cell.hasState(Cell.MOVE_TARGET)) {
 					clearAvailableCells();
 					Array<Cell> moves = ((MoveUnitAction) unitAction).moves;
 					if (moves.size > 0) {
 						Path p = paths.createOrResetPath(selectedUnit, Path.TYPE.MOVE);
 						p.clear();
 
 						int index = ((MoveUnitAction) unitAction).moves.indexOf(cell, true);
 						if (index > 0) {
 							ghostlyCells.removeValue(moves.get(moves.size - 1), true);
 							ghostlyCells.add(cell);
 
 							cell.setUnit(popUnitFromPath(moves));
 
 							for (int i = moves.size - 1; i > index; i--) {
 								moves.get(i).removeState(Cell.MOVE_TARGET);
 							}
 							moves.truncate(index + 1);
 
 							for (int i = 1; i < moves.size; i++) {
 								PathManager.addLine(p,
 										moves.get(i - 1).getCenterX(),
 										moves.get(i - 1).getCenterY(),
 										moves.get(i).getCenterX(),
 										moves.get(i).getCenterY());
 							}
 						} else {
 							if (moves.size > 1) {
 								popUnitFromPath(moves);
 								for (int i = 1; i < moves.size; i++) {
 									moves.get(i).removeState(Cell.MOVE_TARGET);
 								}
 							}
 							moves.truncate(1);
 						}
 
 						lblMoves.setText(maxMoves + 1 - ((MoveUnitAction) unitAction).moves.size + "");
 						showAbleToMoveCells();
 					}
 				} else {
 					saveMove();
 				}
 				break;
 			case NONE:
 				clearSelection();
 				Unit u = cell.getUnit();
 				if (u != null && u.getRender().getState() != STATE.ghost) {
 					if (selectedUnit != u) {
 						selectedUnit = u;
 						selectedCell = cell;
 
 						// TODO: popup
 //						lblAttack.setText(GameController.getInstance().getUnitAttack(selectedUnit.getName()) + "");
 //						maxMoves = GameController.getInstance().getUnitSpeed(selectedUnit.getName());
 //						lblMoves.setText(maxMoves + "");
 
 						moveArrow(selectedUnit);
 
 						if (u.isEnemy()) {
 							hideActionsBar();
 						} else {
 							if (cell.getAction() != null && cell.getAction().getActionType() != UnitActionType.NONE) {
 								setUnitAction(cell.getAction());
 								showAbleToActionCells();
 								undoVisible = true;
 							} else {
 								undoVisible = false;
 							}
 							moveActionsBar(selectedUnit);
 						}
 					}
 				} else {
 					showAssignedActions();
 					clearSelection();
 				}
 				break;
 			default:
 				break;
 			}
 		}
 		return true;
 	}
 
 	private Unit popUnitFromPath(Array<Cell> moves) {
 		Unit ghost = moves.get(moves.size - 1).getUnit();
 		moves.get(moves.size - 1).removeUnit();
 		return ghost;
 	}
 
 	private void setUnitAction(UnitAction act) {
 		unitAction = act;
 		actionType = unitAction.getActionType();
 	}
 
 	@Override
 	public boolean touchUp(float screenX, float screenY, int pointer, int button) {
 		return false;
 	}
 
 	@Override
 	public boolean touchDragged(float screenX, float screenY, int pointer) {
 		return false;
 	}
 
 	@Override
 	public boolean pan(float x, float y, float deltaX, float deltaY) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public Timeline pushEnterAnimation(Timeline t) {
 		return t;
 	}
 
 	@Override
 	public Timeline pushExitAnimation(Timeline t) {
 		arrowY = CrystalClash.HEIGHT + 20;
 		tweenManager.killAll();
 		return pushHideActionBar(t)
 				.push(Tween.to(grpActionBar, ActorAccessor.X, CrystalClash.ANIMATION_SPEED)
 						.target(-grpActionBar.getHeight()))
 				.push(Tween.to(selectorArrow, ActorAccessor.Y, CrystalClash.ANIMATION_SPEED)
 						.target(arrowY));
 	}
 
 	@Override
 	public boolean canSend() {
 		return selectedUnit == null;
 	}
 
 	private void saveAttack() {
 		clearAvailableCells();
 		if (((AttackUnitAction) unitAction).target != null) {
 			attackActions.add((AttackUnitAction) unitAction);
 		} else {
 			setUnitAction(new NoneUnitAction());
 		}
 		selectedCell.setAction(unitAction);
 		clearSelection();
 	}
 
 	private void saveDefense() {
 		selectedCell.setAction(unitAction);
 		clearSelection();
 		actionType = UnitActionType.NONE;
 	}
 
 	private void saveMove() {
 		clearAvailableCells();
 
 		MoveUnitAction action = (MoveUnitAction) unitAction;
 		if (action.moves.size > 1) {
 			moveActions.add(action);
 		} else {
 			undoAction();
 		}
 		selectedCell.setAction(unitAction);
 
 		clearSelection();
 	}
 
 	@Override
 	public void onSend() {
 		switch (actionType) {
 		case ATTACK:
 			saveAttack();
 			break;
 		case DEFENSE:
 			saveDefense();
 			break;
 		case MOVE:
 			saveMove();
 			break;
 		}
 	}
 
 	public void pause() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void resume() {
 		// TODO Auto-generated method stub
 
 	}
 }
