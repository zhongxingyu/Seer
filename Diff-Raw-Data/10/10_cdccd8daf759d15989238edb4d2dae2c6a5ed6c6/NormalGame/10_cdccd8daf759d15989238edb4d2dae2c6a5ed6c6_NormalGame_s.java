 package pruebas.Renders;
 
 import pruebas.Accessors.ActorAccessor;
 import pruebas.Controllers.GameController;
 import pruebas.Controllers.WorldController;
 import pruebas.CrystalClash.CrystalClash;
 import pruebas.Entities.Cell;
 import pruebas.Entities.GridPos;
 import pruebas.Entities.Unit;
 import pruebas.Entities.helpers.AttackUnitAction;
 import pruebas.Entities.helpers.DefendUnitAction;
 import pruebas.Entities.helpers.MoveUnitAction;
 import pruebas.Entities.helpers.PlaceUnitAction;
 import pruebas.Entities.helpers.UnitAction;
 import pruebas.Entities.helpers.UnitAction.UnitActionType;
 import pruebas.Util.Tuple;
 import aurelienribon.tweenengine.BaseTween;
 import aurelienribon.tweenengine.Timeline;
 import aurelienribon.tweenengine.Tween;
 import aurelienribon.tweenengine.TweenCallback;
 import aurelienribon.tweenengine.TweenManager;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.BitmapFont;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.TextureAtlas;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.scenes.scene2d.Group;
 import com.badlogic.gdx.scenes.scene2d.InputEvent;
 import com.badlogic.gdx.scenes.scene2d.Stage;
 import com.badlogic.gdx.scenes.scene2d.ui.Image;
 import com.badlogic.gdx.scenes.scene2d.ui.Label;
 import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
 import com.badlogic.gdx.scenes.scene2d.ui.Skin;
 import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
 import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
 import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
 import com.badlogic.gdx.utils.Array;
 
 public class NormalGame extends GameRender {
 	private TweenManager tweenManager;
 	
 //	private Unit testUnit1;
 //	private Unit testUnit2;
 //	private Unit testUnit3;
 //	private Unit testUnit4;
 	private Unit selectedUnit;
 	private Cell selectedCell;
 	
 	private BitmapFont font;
 
 	private Image selectorArrow;
 	private float arrowX;
 	private float arrowY;
 
 	private Image actionsBar;
 	private TextButton btnAttack;
 	private TextButton btnMove;
 	private TextButton btnDefense;
 	private TextButton btnUndo;
 	private Group grpActionBar;
 	private Label lblMoves;
 	private Label lblAttack;
 	private boolean actionsBarVisible;
 	private boolean undoVisible;
 
 	private UnitActionType actionType;
 	private int maxMoves;
 	
 	private UnitAction unitAction;
 	private Array<Cell> alreadyAssigned;
 	private Array<MoveUnitAction> mActions;
 	private Array<Tuple<Unit, MoveUnitAction>> ghostlyUnits;
 	
 	public NormalGame(WorldController world) {
 		super(world);
 
 		tweenManager = new TweenManager();
 
 		selectedUnit = null;
 		selectedCell = null;
 		
 		arrowX = 0;
 		arrowY = 0;
 		undoVisible = false;
 		actionsBarVisible = false;
 
 		actionType = UnitActionType.NONE;
 		maxMoves = 0;
 		
 		alreadyAssigned = new Array<Cell>();
 		mActions = new Array<MoveUnitAction>();
 		ghostlyUnits = new Array<Tuple<Unit, MoveUnitAction>>();
 		init();
 	}
 
 	public void init() {
 		GameController.getInstancia().loadUnitsStats();
 
 		// testUnit1 = new Unit("fire_archer");
 		// if (world.player == 2)
 		// testUnit1.getRender().setFacing(FACING.left);
 		//
 		// testUnit2 = new Unit("earth_tank");
 		// if (world.player == 2)
 		// testUnit2.getRender().setFacing(FACING.left);
 		//
 		// testUnit3 = new Unit("darkness_mage");
 		// if (world.player == 2)
 		// testUnit3.getRender().setFacing(FACING.left);
 		//
 		// testUnit4 = new Unit("wind_assassin");
 		// if (world.player == 2)
 		// testUnit4.getRender().setFacing(FACING.left);
 		//
 		// world.setCellState(165, 675, Cell.State.ABLE_TO_PLACE);
 		// world.placeUnit(165, 690, testUnit1);
 		// world.setCellState(165, 675, Cell.State.NONE);
 		// world.setCellState(635, 675, Cell.State.ABLE_TO_PLACE);
 		// world.placeUnit(635, 675, testUnit2);
 		// world.setCellState(635, 675, Cell.State.NONE);
 		// world.setCellState(1140, 675, Cell.State.ABLE_TO_PLACE);
 		// world.placeUnit(1140, 675, testUnit3);
 		// world.setCellState(1140, 675, Cell.State.NONE);
 		// world.setCellState(550, 450, Cell.State.ABLE_TO_PLACE);
 		// world.placeUnit(550, 450, testUnit4);
 		// world.setCellState(550, 450, Cell.State.NONE);
 		
 		Texture arrow = new Texture(Gdx.files.internal("data/Images/InGame/selector_arrow.png"));
 		selectorArrow = new Image(arrow);
 		selectorArrow.setPosition(arrowX, arrowY);
 
 		TextureAtlas atlas = new TextureAtlas(
 				"data/Images/InGame/options_bar.pack");
 		Skin skin = new Skin(atlas);
 
 		TextureRegion aux = skin.getRegion("actions_bar");
 		actionsBar = new Image(aux);
 
 		font = new BitmapFont(Gdx.files.internal("data/Fonts/font.fnt"), false);
 
 		TextButtonStyle attackStyle = new TextButtonStyle(
 				skin.getDrawable("action_attack_button"),
 				skin.getDrawable("action_attack_button_pressed"), null, font);
 		btnAttack = new TextButton("", attackStyle);
 		btnAttack.setPosition(actionsBar.getX() + 15, actionsBar.getY() - 20);
 		btnAttack.addListener(new ClickListener() {
 			@Override
 			public void clicked(InputEvent event, float x, float y) {
 				// TODO: Pintar AbleToAttack
 				btnUndo.setPosition(btnAttack.getX(), btnDefense.getY());
 				undoVisible = true;
 				updateActionsBar();
 				unitAction = new AttackUnitAction();
 			}
 		});
 
 		TextButtonStyle defenseStyle = new TextButtonStyle(
 				skin.getDrawable("action_defensive_button"),
 				skin.getDrawable("action_defensive_button_pressed"), null, font);
 		btnDefense = new TextButton("", defenseStyle);
 		btnDefense.setPosition(btnAttack.getX() + btnAttack.getWidth() + 15,
 				actionsBar.getY());
 		btnDefense.addListener(new ClickListener() {
 			@Override
 			public void clicked(InputEvent event, float x, float y) {
 				// TODO: Pintar Escudito
 				btnUndo.setPosition(btnDefense.getX(), btnDefense.getY());
 				undoVisible = true;
 				updateActionsBar();
 				unitAction = new DefendUnitAction();
 			}
 		});
 
 		TextButtonStyle moveStyle = new TextButtonStyle(
 				skin.getDrawable("action_run_button"),
 				skin.getDrawable("action_run_button_pressed"), null, font);
 		btnMove = new TextButton("", moveStyle);
 		btnMove.setPosition(btnDefense.getX() + btnDefense.getWidth() + 15,
 				actionsBar.getY() - 20);
 		btnMove.addListener(new ClickListener() {
 			@Override
 			public void clicked(InputEvent event, float x, float y) {
 				btnUndo.setPosition(btnMove.getX(), btnDefense.getY());
 				undoVisible = true;
 				updateActionsBar();
 				
 				unitAction = new MoveUnitAction();
 				unitAction.origin = selectedCell;
 				((MoveUnitAction) unitAction).moves.add(selectedCell);
 				
 				showAbleToMoveCells();	
 				showAction(unitAction, true);
 			}
 		});
 		
 		TextButtonStyle undoStyle = new TextButtonStyle(
 				skin.getDrawable("action_cancel_button"),
 				skin.getDrawable("action_cancel_button_pressed"), null, font);
 		btnUndo = new TextButton("", undoStyle);
 		btnUndo.setPosition(0, 300);	//Afuera de la ventana
 		btnUndo.addListener(new ClickListener() {
 			@Override
 			public void clicked(InputEvent event, float x, float y) {
 				undoAction();
 				undoVisible = false;
 				updateActionsBar();
 			}
 		});
 
 		lblAttack = new Label("150", new LabelStyle(font, Color.WHITE));
 		lblAttack.setPosition(btnAttack.getX()
 				+ (btnAttack.getWidth() / 2 - lblAttack.getWidth() / 2),
 				btnAttack.getY() + 3);
 
 		lblMoves = new Label("5", new LabelStyle(font, Color.WHITE));
 		lblMoves.setPosition(btnMove.getX()
 				+ (btnMove.getWidth() / 2 - lblMoves.getWidth() / 2),
 				btnMove.getY() + 3);
 
 		grpActionBar = new Group();
 		grpActionBar.addActor(actionsBar);
 		grpActionBar.addActor(btnAttack);
 		grpActionBar.addActor(lblAttack);
 		grpActionBar.addActor(btnMove);
 		grpActionBar.addActor(lblMoves);
 		grpActionBar.addActor(btnDefense);
 
 		grpActionBar.setSize(actionsBar.getWidth(), actionsBar.getHeight());
 		grpActionBar.setPosition(
 				CrystalClash.WIDTH / 2 - grpActionBar.getWidth() / 2,
 				CrystalClash.HEIGHT + 50);
 	}
 
 	private void moveArrow(Unit u) {
 		if (u != null) {
 			arrowX = u.getX();
 			arrowY = u.getY() + 120;
 		} else {
 			arrowX = 0;
 			arrowY = 0;
 		}
 
 		tweenManager.killAll();
 		float speed = 1f; // CrystalClash.ANIMATION_SPEED;
 		Timeline.createParallel()
 				.push(Tween.to(selectorArrow, ActorAccessor.X, speed).target(
 						arrowX))
 				.push(Tween.to(selectorArrow, ActorAccessor.Y, speed).target(
 						arrowY)).setCallback(new TweenCallback() {
 					@Override
 					public void onEvent(int type, BaseTween<?> source) {
 						selectorArrow.setPosition(arrowX, arrowY);
 						arrowAnimation();
 					}
 				}).start(tweenManager);
 	}
 
 	private void arrowAnimation() {
 		float speed = 1f; // CrystalClash.ANIMATION_SPEED;
 		Timeline.createSequence()
 				.push(Tween.set(selectorArrow, ActorAccessor.Y).target(arrowY))
 				.push(Tween.to(selectorArrow, ActorAccessor.Y, speed).target(
 						arrowY - 10))
 				.push(Tween.to(selectorArrow, ActorAccessor.Y, speed).target(
 						arrowY)).repeat(Tween.INFINITY, 0).start(tweenManager);
 	}
 	
 	private void showActionsBar(){
 		float speed = 0.5f; // CrystalClash.ANIMATION_SPEED;
 		Timeline t = Timeline.createSequence();
 		if (450 < selectedUnit.getX() && selectedUnit.getX() < 825) {
 			if (actionsBarVisible) {
 				t.push(Tween.to(grpActionBar, ActorAccessor.X, speed).target(
 						CrystalClash.WIDTH / 4 - grpActionBar.getWidth() / 2));
 			} else {
 				grpActionBar.setX(CrystalClash.WIDTH / 4
 						- grpActionBar.getWidth() / 2);
 				t.push(Tween.to(grpActionBar, ActorAccessor.Y, speed).target(
 						CrystalClash.HEIGHT - grpActionBar.getHeight()));
 			}
 		} else {
 			if (actionsBarVisible) {
 				t.push(Tween.to(grpActionBar, ActorAccessor.X, speed).target(
 						CrystalClash.WIDTH / 2 - grpActionBar.getWidth() / 2));
 			} else {
 				grpActionBar.setX(CrystalClash.WIDTH / 2
 						- grpActionBar.getWidth() / 2);
 				t.push(Tween.to(grpActionBar, ActorAccessor.Y, speed).target(
 						CrystalClash.HEIGHT - grpActionBar.getHeight()));
 			}
 		}
 		t.start(tweenManager);
 		actionsBarVisible = true;
 	}
 	
 	private void hideActionsBar(){
 		actionsBarVisible = false;
 		float speed = 0.5f; // CrystalClash.ANIMATION_SPEED;
 		Timeline.createSequence()
 				.push(Tween.to(grpActionBar, ActorAccessor.Y, speed).target(
 						CrystalClash.HEIGHT + 50)).start(tweenManager);
 	}
 	
 	private void showAbleToMoveCells(){
 		clearCells();
 		actionType = UnitActionType.MOVE;
 		
 		if(((MoveUnitAction) unitAction).moves.size <= maxMoves){
 			Cell top = ((MoveUnitAction) unitAction).moves.peek();
 			int[][] cells = top.neigbours;
 			Cell aux = null;
 			for (int i = 0; i < top.neigbours.length; i++) {
 				aux = world.cellAtByGrid(cells[i][0], cells[i][1]);
 				if(aux.getUnit(world.player) == null && !alreadyAssigned.contains(aux, true))
 					aux.setState(Cell.State.ABLE_TO_MOVE);
 			}
 		}
 	}
 	
 	private void clearCells(){
 		switch(actionType){
 		case PLACE:
 			break;
 		case ATTACK:
 			break;
 		case DEFENSE:
 			break;
 		case MOVE:
 			Cell top = null;
 			for (int i = 0; i < ((MoveUnitAction) unitAction).moves.size; i++) {
 				top = ((MoveUnitAction) unitAction).moves.get(i);
 				top.setState(Cell.State.NONE);
 				int[][] cells = top.neigbours;
 				for (int j = 0; j < top.neigbours.length; j++) {
 					world.setCellStateByGridPos(cells[j][0], cells[j][1], Cell.State.NONE);
 				}
 			}
 			break;
 		case NONE:
 			break;
 		default:
 			break;
 		}
 		actionType = UnitActionType.NONE;
 	}
 	
 	private void clearSelection(){
 		selectedUnit = null;
 		selectedCell = null;
 		moveArrow(selectedUnit);
 		hideActionsBar();
 	}
 	
 	private void showAction(UnitAction action, boolean stillAssigning){
 		unitAction = action;
 		switch(unitAction.getActionType()){
 		case ATTACK:
 			break;
 		case DEFENSE:
 			break;
 		case MOVE:
 			if(stillAssigning){
 				showAbleToMoveCells();
 			}
 			
 			for (int i = 0; i < ((MoveUnitAction) action).moves.size; i++) {
 				GridPos g = ((MoveUnitAction) action).moves.get(i).getGridPosition();
 				world.setCellStateByGridPos(g.getX(), g.getY(), Cell.State.MOVE_TARGET);
 			}
 			lblMoves.setText(maxMoves + 1 - ((MoveUnitAction) unitAction).moves.size + ""); 
 			undoVisible = true;
 			break;
 		case NONE:
 			break;
 		case PLACE:
 			undoVisible = false;
 			break;
 		default:
 			break;
 		}
 	}
 
 	private void showMovePaths(){
 		for(int i = 0; i < mActions.size; i++){
 			showAction(mActions.get(i), false);
 		}
 		actionType = UnitActionType.NONE;
 	}
 	
 	private void hideAction(UnitAction action){
 		switch(action.getActionType()){
 		case ATTACK:
 			break;
 		case DEFENSE:
 			break;
 		case MOVE:
 			for (int i = 0; i < ((MoveUnitAction) action).moves.size; i++) {
 				GridPos g = ((MoveUnitAction) action).moves.get(i).getGridPosition();
 				world.setCellStateByGridPos(g.getX(), g.getY(), Cell.State.NONE);
 			}
 			break;
 		case NONE:
 			break;
 		case PLACE:
 			break;
 		default:
 			break;
 		}
 	}
 	
 	private void hideMovePaths(){
 		for(int i = 0; i < mActions.size; i++){
 			hideAction(mActions.get(i));
 		}
 		actionType = UnitActionType.NONE;
 	}
 	
 	private void undoAction(){
 		clearCells();
 		lblMoves.setText(maxMoves + "");
 
 		clearAction();
 		
 		unitAction = new PlaceUnitAction();
 		((PlaceUnitAction) unitAction).unitName = selectedCell.getUnit(world.player).getName();
 		selectedCell.setAction(unitAction, world.player);
 		
 		actionType = UnitActionType.NONE;
 	}
 	
 	private void updateActionsBar() {
 		if (undoVisible) {
 			grpActionBar.removeActor(btnAttack);
 			grpActionBar.removeActor(lblAttack);
 			grpActionBar.removeActor(btnMove);
 			grpActionBar.removeActor(lblMoves);
 			grpActionBar.removeActor(btnDefense);
 			grpActionBar.addActor(btnUndo);
 		}else{
 			grpActionBar.addActor(btnAttack);
 			grpActionBar.addActor(lblAttack);
 			grpActionBar.addActor(btnMove);
 			grpActionBar.addActor(lblMoves);
 			grpActionBar.addActor(btnDefense);
 			grpActionBar.removeActor(btnUndo);
 		}
 	}
 	
 	//Borra la action de la lista, saca el fantasma y saca la cell de las asignadas
 	private void clearAction(){
 		MoveUnitAction action = (MoveUnitAction) unitAction;
 		MoveUnitAction aux = null;
 		for (int i = 0; i < ghostlyUnits.size; i++) {
 			aux = ghostlyUnits.get(i).getSecond();
 			if(action.equals(aux)){
 				ghostlyUnits.removeIndex(i);
 				alreadyAssigned.removeValue(aux.moves.get(aux.moves.size - 1), true);
 			}
 		}
 		
 		mActions.removeValue(action, false);
 	}
 	
 	@Override
 	public void render(float dt, SpriteBatch batch, Stage stage) {
 		selectorArrow.draw(batch, 1);
 		
 		Unit u = null;
 		for(int i = 0; i < ghostlyUnits.size; i++){
 			u = ghostlyUnits.get(i).getFirst();
 			u.getRender().setGhostly();
 			u.getRender().draw(batch, dt);
 		}
 
 		stage.addActor(grpActionBar);
 		grpActionBar.act(dt);
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
 				break;
 			case DEFENSE:
 				break;
 			case MOVE:
 				if (cell.getState() == Cell.State.ABLE_TO_MOVE) {
 					lblMoves.setText(maxMoves - ((MoveUnitAction) unitAction).moves.size + "");
 					((MoveUnitAction) unitAction).moves.add(cell);
 					showAbleToMoveCells();
 					showAction(unitAction, true);
 				} else if (cell.getState() == Cell.State.MOVE_TARGET) {
 					clearCells();
 					int index = ((MoveUnitAction) unitAction).moves.indexOf(cell, true);
 					if(index != -1){
 						((MoveUnitAction) unitAction).moves.truncate(index + 1);
 					}
 					
 					lblMoves.setText(maxMoves + 1 - ((MoveUnitAction) unitAction).moves.size + "");
 					showAbleToMoveCells();
 					showAction(unitAction, true);
 				} else {
 					clearAction();
 					
 					selectedCell.setAction(unitAction, world.player);
 					MoveUnitAction action = (MoveUnitAction) unitAction;
 					mActions.add(action);
 					
 					if(action.moves.size > 1){
 						Unit ghost = new Unit(selectedUnit.getName());
 						Cell ghostCell = action.moves.get(action.moves.size-1);
						ghost.setPosition(ghostCell.getX() + Cell.unitPlayer1X, ghostCell.getY() + Cell.unitPlayer1Y);
 						
 						ghostlyUnits.add(new Tuple<Unit, MoveUnitAction>(ghost, action));
 						alreadyAssigned.add(ghostCell);
 					}
 					
 					clearCells();
 					clearSelection();
 					showMovePaths();
 				}
 				break;
 			case NONE:
 				showMovePaths();
 				Unit u = cell.getUnit(world.player);
 				if (u != null) {
 					clearSelection();
 					if (selectedUnit != u) {
 						selectedUnit = u;
 						selectedCell = cell;
 
 						lblAttack.setText(GameController.getInstancia().getUnitAttack(selectedUnit.getName()) + "");
 						maxMoves = GameController.getInstancia().getUnitSpeed(selectedUnit.getName());
 						lblMoves.setText(maxMoves + "");
 
 						hideMovePaths();
 						if(cell.getAction(world.player) != null){
 							showAction(cell.getAction(world.player), true);
 						}
 						
 						updateActionsBar();
 						moveArrow(selectedUnit);
 						showActionsBar();
 						
 						System.out.println(selectedUnit.getName());
 					}
 				} else {
 					clearSelection();
 				}
 				break;
 			default:
 				break;
 			}
 		}
 		return true;
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
 		return false;
 	}
 
 }
