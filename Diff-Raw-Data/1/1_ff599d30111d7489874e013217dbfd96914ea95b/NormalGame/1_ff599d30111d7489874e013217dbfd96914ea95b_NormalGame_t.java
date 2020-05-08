 package pruebas.Renders;
 
 import pruebas.Accessors.ActorAccessor;
 import pruebas.Controllers.GameController;
 import pruebas.Controllers.WorldController;
 import pruebas.CrystalClash.CrystalClash;
 import pruebas.Entities.Cell;
 import pruebas.Entities.Unit;
 import pruebas.Renders.UnitRender.FACING;
 import aurelienribon.tweenengine.BaseTween;
 import aurelienribon.tweenengine.Timeline;
 import aurelienribon.tweenengine.Tween;
 import aurelienribon.tweenengine.TweenCallback;
 import aurelienribon.tweenengine.TweenManager;
 
 import com.badlogic.gdx.Game;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.BitmapFont;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.TextureAtlas;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.scenes.scene2d.Group;
 import com.badlogic.gdx.scenes.scene2d.InputEvent;
 import com.badlogic.gdx.scenes.scene2d.Stage;
 import com.badlogic.gdx.scenes.scene2d.ui.Image;
 import com.badlogic.gdx.scenes.scene2d.ui.Skin;
 import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
 import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
 import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
 
 public class NormalGame extends GameRender {
 
 	private TweenManager tweenManager;
 	
 	private Unit testUnit1;
 	private Unit testUnit2;
 	private Unit testUnit3;
 	private Unit testUnit4;
 	private Unit selectedUnit;
 	private Image selectorArrow;
 	private Image actionsBar;
 
 	private TextButton btnAttack;
 	private TextButton btnMove;
 	private TextButton btnDefense;
 	private Group grpActionBar;
 	private boolean actionsBarVisible;
 	
 	private float arrowX;
 	private float arrowY;
 	
 	public NormalGame(WorldController world) {
 		super(world);
 		
 		tweenManager = new TweenManager();
 		actionsBarVisible = false;
 		arrowX = 0;
 		arrowY = 0;
 		
 		init();
 	}
 
 	public void init() {
 		GameController.getInstancia().loadUnitsStats();
 		
 		testUnit1 = new Unit("fire_archer");
 		if (world.player == 2)
 			testUnit1.getRender().setFacing(FACING.left);
 		
 		testUnit2 = new Unit("earth_tank");
 		if (world.player == 2)
 			testUnit2.getRender().setFacing(FACING.left);
 		
 		testUnit3 = new Unit("darkness_mage");
 		if (world.player == 2)
 			testUnit3.getRender().setFacing(FACING.left);
 		
 		testUnit4 = new Unit("wind_assassin");
 		if (world.player == 2)
 			testUnit4.getRender().setFacing(FACING.left);
 		
 		world.setCellEnable(165, 675);
 		world.placeUnit(165, 690, testUnit1);
 		world.setCellEnable(635, 675);
 		world.placeUnit(635, 675, testUnit2);
 		world.setCellEnable(1140, 675);
 		world.placeUnit(1140, 675, testUnit3);
 		world.setCellEnable(550, 450);
 		world.placeUnit(550, 450, testUnit4);
 		
 		Texture arrow = new Texture(Gdx.files.internal("data/Images/InGame/selector_arrow.png"));
 		selectorArrow = new Image(arrow);
 		selectorArrow.setPosition(arrowX, arrowY);
 		
 		TextureAtlas atlas = new TextureAtlas(
 				"data/Images/InGame/options_bar.pack");
 		Skin skin = new Skin(atlas);
 		
 		TextureRegion aux = skin.getRegion("actions_bar");
 		actionsBar = new Image(aux);
 		
 		BitmapFont font = new BitmapFont(
 				Gdx.files.internal("data/Fonts/font.fnt"), false);
 		TextButtonStyle attackStyle = new TextButtonStyle(
 				skin.getDrawable("action_attack_button"),
 				skin.getDrawable("action_attack_button_pressed"), null, font);
 		btnAttack = new TextButton("", attackStyle);
 		btnAttack.setPosition(actionsBar.getX() + 15, actionsBar.getY() - 20);
 		btnAttack.addListener(new ClickListener() {
 			@Override
 			public void clicked(InputEvent event, float x, float y) {
 				// TODO: Pintar AbleToAttack
 			}
 		});
 		
 		TextButtonStyle defenseStyle = new TextButtonStyle(
 				skin.getDrawable("action_defensive_button"),
 				skin.getDrawable("action_defensive_button_pressed"), null, font);
 		btnDefense = new TextButton("", defenseStyle);
 		btnDefense.setPosition(btnAttack.getX() + btnAttack.getWidth() + 15, actionsBar.getY());
 		btnDefense.addListener(new ClickListener() {
 			@Override
 			public void clicked(InputEvent event, float x, float y) {
 				// TODO: Pintar Escudito
 			}
 		});
 
 		TextButtonStyle moveStyle = new TextButtonStyle(
 				skin.getDrawable("action_run_button"),
 				skin.getDrawable("action_run_button_pressed"), null, font);
 		btnMove = new TextButton("", moveStyle);
 		btnMove.setPosition(btnDefense.getX() + btnDefense.getWidth() + 15, actionsBar.getY() - 20);
 		btnMove.addListener(new ClickListener() {
 			@Override
 			public void clicked(InputEvent event, float x, float y) {
 				// TODO: Pintar AbleToMove
 			}
 		});
 		
 		grpActionBar = new Group();
 		grpActionBar.addActor(actionsBar);
 		grpActionBar.addActor(btnAttack);
 		grpActionBar.addActor(btnMove);
 		grpActionBar.addActor(btnDefense);
 
 		grpActionBar.setSize(actionsBar.getWidth(), actionsBar.getHeight());
 		grpActionBar.setPosition(CrystalClash.WIDTH / 2 - grpActionBar.getWidth() / 2, CrystalClash.HEIGHT + 50);
 	}
 	
 	private void moveArrow(Unit u){
 		if(u != null){
 			arrowX = u.getX();
 			arrowY = u.getY() + 120;
 		} else {
 			arrowX = 0;
 			arrowY = 0;
 		}
 		
		tweenManager.killAll();
 		float speed = 1f; // CrystalClash.ANIMATION_SPEED;
 		Timeline.createParallel()
 				.push(Tween.to(selectorArrow, ActorAccessor.X, speed).target(arrowX))
 				.push(Tween.to(selectorArrow, ActorAccessor.Y, speed).target(arrowY))
 				.setCallback(new TweenCallback() {
 					@Override
 					public void onEvent(int type, BaseTween<?> source) {
 						selectorArrow.setPosition(arrowX, arrowY);
 						arrowAnimation();
 					}
 				}).start(tweenManager);
 	}
 	
 	private void arrowAnimation(){
 		float speed = 1f; // CrystalClash.ANIMATION_SPEED;
 		Timeline.createSequence()
 				.push(Tween.set(selectorArrow, ActorAccessor.Y).target(arrowY))
 				.push(Tween.to(selectorArrow, ActorAccessor.Y, speed).target(arrowY - 10))
 				.push(Tween.to(selectorArrow, ActorAccessor.Y, speed).target(arrowY))
 				.repeat(Tween.INFINITY, 0)
 				.start(tweenManager);
 	}
 	
 	private void showActionsBar(){
 		//GridPos g = selectedUnit.getGridPosition();
 		//if(3 <= g.getX() && g.getX() < 6)
 		//	grpActionBar.setX(CrystalClash.WIDTH / 4 - grpActionBar.getWidth() / 2);
 		//else
 		//	grpActionBar.setX(CrystalClash.WIDTH / 2 - grpActionBar.getWidth() / 2);
 		float speed = 0.5f; // CrystalClash.ANIMATION_SPEED;
 		Timeline t = Timeline.createSequence();
 		if (450 < selectedUnit.getX() && selectedUnit.getX() < 825){
 			if(actionsBarVisible) {
 				t.push(Tween.to(grpActionBar, ActorAccessor.X, speed).target(CrystalClash.WIDTH / 4 - grpActionBar.getWidth() / 2));
 			}else{
 				grpActionBar.setX(CrystalClash.WIDTH / 4 - grpActionBar.getWidth() / 2);
 				t.push(Tween.to(grpActionBar, ActorAccessor.Y, speed).target(CrystalClash.HEIGHT - grpActionBar.getHeight()));
 			}
 		}else{
 			if(actionsBarVisible) {
 				t.push(Tween.to(grpActionBar, ActorAccessor.X, speed).target(CrystalClash.WIDTH / 2 - grpActionBar.getWidth() / 2));
 			}else{
 				grpActionBar.setX(CrystalClash.WIDTH / 2 - grpActionBar.getWidth() / 2);
 				t.push(Tween.to(grpActionBar, ActorAccessor.Y, speed).target(CrystalClash.HEIGHT - grpActionBar.getHeight()));
 			}
 		}
 		t.start(tweenManager);
 		actionsBarVisible = true;
 	}
 	
 	private void hideActionsBar(){
 		actionsBarVisible = false;
 		float speed = 0.5f; // CrystalClash.ANIMATION_SPEED;
 		Timeline.createSequence()
 				.push(Tween.to(grpActionBar, ActorAccessor.Y, speed).target(CrystalClash.HEIGHT + 50))
 				.start(tweenManager);
 	}
 
 	@Override
 	public void render(float dt, SpriteBatch batch, Stage stage) {
 		selectorArrow.draw(batch, 1);
 
 		stage.addActor(grpActionBar);
 		grpActionBar.act(dt);
 		tweenManager.update(dt);
 	}
 
 	@Override
 	public boolean touchDown(float x, float y, int pointer, int button) {
 		Cell cell = world.cellAt(x, y);
 		if (cell != null) {
 			Unit u = cell.getUnit(world.player);
 			if(u != null){
 				if (selectedUnit != u) {
 					selectedUnit = u;
 					moveArrow(selectedUnit);
 					showActionsBar();
 					System.out.println(selectedUnit.getName());
 				}else{
 					selectedUnit = null;
 					moveArrow(selectedUnit);
 					hideActionsBar();
 				}
 			}else{
 				selectedUnit = null;
 				moveArrow(selectedUnit);
 				hideActionsBar();
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
