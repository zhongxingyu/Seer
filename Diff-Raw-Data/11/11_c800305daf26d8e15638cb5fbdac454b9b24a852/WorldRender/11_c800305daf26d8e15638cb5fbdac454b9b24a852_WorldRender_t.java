 package pruebas.Renders;
 
 import pruebas.Accessors.ActorAccessor;
 import pruebas.Controllers.WorldController;
 import pruebas.CrystalClash.CrystalClash;
 import pruebas.Renders.helpers.CellHelper;
 import pruebas.Renders.helpers.ResourceHelper;
 import pruebas.Renders.helpers.UnitHelper;
 import pruebas.Renders.helpers.ui.MessageBox;
 import pruebas.Renders.helpers.ui.MessageBoxCallback;
 import aurelienribon.tweenengine.Timeline;
 import aurelienribon.tweenengine.Tween;
 import aurelienribon.tweenengine.TweenEquations;
 
 import com.badlogic.gdx.Input.Keys;
 import com.badlogic.gdx.InputProcessor;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.TextureAtlas;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.scenes.scene2d.Group;
 import com.badlogic.gdx.scenes.scene2d.InputEvent;
 import com.badlogic.gdx.scenes.scene2d.ui.Image;
 import com.badlogic.gdx.scenes.scene2d.ui.Skin;
 import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
 import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
 import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
 
 public class WorldRender extends Group implements InputProcessor {
 	public static CellHelper cellHelper;
 
 	private Texture txrTerrain;
 	private Image imgTerrain;
 
 	private Group grpBtnSend;
 	private Image imgBtnSendBackground;
 	private TextButton btnSend;
 
 	private Group grpBtnOptions;
 	private Image imgBtnOptionsBackground;
 	private TextButton btnOptions;
 
 	private Group grpOptions;
 	private Image imgOptionsBackground;
 	private TextButton btnSurrender;
 	private TextButton btnBack;
 	private TextButton btnClear;
 	private boolean hideMoreOptions;
 
 	private WorldController world;
 	GameRender gameRender;
 
 	private boolean readInput = true;
 
 	private MessageBoxCallback backCallback;
 
 	public WorldRender(WorldController world) {
 		this.world = world;
 
 		cellHelper = new CellHelper();
 		cellHelper.load();
 
 		hideMoreOptions = false;
 
 		UnitHelper.init();
 		load();
 	}
 
 	public void initFirstTurn() {
 		gameRender = new SelectUnitsRender(world);
 		addActor(gameRender);
 		showHuds();
 	}
 
 	public void initNormalTurn() {
 		gameRender = new NormalGame(world);
 		addActor(gameRender);
 		showHuds();
 	}
 
 	public void initTurnAnimations() {
 		gameRender = new TurnAnimations(world);
 		addActor(gameRender);
 	}
 
 	public void render(float dt, SpriteBatch batch) {
 		imgTerrain.draw(batch, 1);
 		for (int i = 0; i < world.cellGrid.length; i++) {
 			for (int j = world.cellGrid[i].length - 1; j >= 0; j--) {
 				world.cellGrid[i][j].getRender().draw(dt, batch);
 			}
 		}
 
 		for (int j = 5; j >= 0; j--) {
 			for (int i = 0; i < 9; i += 2) {
 				world.cellGrid[i][j].getRender().drawUnits(dt, batch);
 			}
 			for (int i = 1; i < 9; i += 2) {
 				world.cellGrid[i][j].getRender().drawUnits(dt, batch);
 			}
 		}
 
 		gameRender.render(dt, batch);
 	}
 
 	private void load() {
 		TextureAtlas atlas = new TextureAtlas(
 				"data/Images/InGame/options_bar.pack");
 		Skin skin = new Skin(atlas);
 
 		// Terrain
 		txrTerrain = ResourceHelper.getTexture("data/Images/InGame/terrain.jpg");
 		imgTerrain = new Image(txrTerrain);
 		imgTerrain.setSize(CrystalClash.WIDTH, CrystalClash.HEIGHT);
 
 		// Options bar
 		TextButtonStyle optionsStyle = new TextButtonStyle(
 				skin.getDrawable("option_button"),
 				skin.getDrawable("option_button_pressed"), null, ResourceHelper.getFont());
 
 		grpOptions = new Group();
 
 		imgOptionsBackground = new Image(skin.getRegion("options_bar"));
 		imgOptionsBackground.setPosition(0, 0);
 		grpOptions.addActor(imgOptionsBackground);
 
 		btnSurrender = new TextButton("Surrender", optionsStyle);
 		btnSurrender.setPosition(75, 5);
 		final MessageBoxCallback leaveCallback = new MessageBoxCallback() {
 			@Override
 			public void onEvent(int type, Object data) {
 				if (type == MessageBoxCallback.YES) {
 					GameEngine.showLoading();
 					world.surrenderCurrentGame();
 				} else {
 					MessageBox.build().hide();
 					setReadInput(true);
 					resume();
 				}
 			}
 		};
 		btnSurrender.addListener(new ClickListener() {
 			@Override
 			public void clicked(InputEvent event, float x, float y) {
 				pause();
 				setReadInput(false);
 				MessageBox.build()
 						.setMessage("\"He who knows when he can fight and when he cannot, will be victorious.\"\n- Sun Tzu")
 						.twoButtonsLayout("Surrender", "Not yet!")
 						.setHideOnAction(false)
 						.setCallback(leaveCallback)
 						.show();
 			}
 		});
 		grpOptions.addActor(btnSurrender);
 		backCallback = new MessageBoxCallback() {
 			@Override
 			public void onEvent(int type, Object data) {
 				if (type == MessageBoxCallback.YES) {
 					GameEngine.showLoading();
 					world.leaveGame();
 				} else {
 					MessageBox.build().hide();
 					setReadInput(true);
 					resume();
 				}
 			}
 		};
 		btnBack = new TextButton("Back to Menu", optionsStyle);
 		btnBack.setPosition(btnSurrender.getX() + btnSurrender.getWidth() + 2, 5);
 
 		btnBack.addListener(new ClickListener() {
 			@Override
 			public void clicked(InputEvent event, float x, float y) {
 				setReadInput(false);
 				back();
 			}
 		});
 		grpOptions.addActor(btnBack);
 
 		btnClear = new TextButton("Clear Moves", optionsStyle);
 		btnClear.setPosition(btnBack.getX() + btnBack.getWidth() + 2, 5);
 		btnClear.addListener(new ClickListener() {
 			@Override
 			public void clicked(InputEvent event, float x, float y) {
 				gameRender.clearAllChanges();
 			}
 		});
 		grpOptions.addActor(btnClear);
 
 		grpOptions.setSize(imgOptionsBackground.getWidth(), imgOptionsBackground.getHeight());
 		grpOptions.setPosition(-grpOptions.getWidth(), 0);
 
 		// Btn Options
 		grpBtnOptions = new Group();
 		imgBtnOptionsBackground = new Image(skin.getRegion("option_more_bar"));
 		imgBtnOptionsBackground.setPosition(0, 0);
 		grpBtnOptions.addActor(imgBtnOptionsBackground);
 
 		TextButtonStyle moreStyle = new TextButtonStyle(
 				skin.getDrawable("option_more_button"),
 				skin.getDrawable("option_more_button_pressed"), null, ResourceHelper.getFont());
 		btnOptions = new TextButton("", moreStyle);
 		btnOptions.setPosition(imgBtnOptionsBackground.getWidth() - btnOptions.getWidth(), 0);
 		btnOptions.addListener(new ClickListener() {
 			@Override
 			public void clicked(InputEvent event, float x, float y) {
 				hideMoreOptions = true;
 				showOptions();
 			}
 		});
 		grpBtnOptions.addActor(btnOptions);
 
 		grpBtnOptions.setSize(imgBtnOptionsBackground.getWidth(), imgBtnOptionsBackground.getHeight());
 		grpBtnOptions.setPosition(-grpBtnOptions.getWidth(), 0);
 
 		// Btn Send
 		grpBtnSend = new Group();
 
 		imgBtnSendBackground = new Image(skin.getRegion("option_send_bar"));
 		imgBtnSendBackground.setPosition(0, 0);
 		grpBtnSend.addActor(imgBtnSendBackground);
 
 		TextButtonStyle sendStyle = new TextButtonStyle(
 				skin.getDrawable("option_send_button"),
 				skin.getDrawable("option_send_button_pressed"), null, ResourceHelper.getFont());
 		btnSend = new TextButton("", sendStyle);
 		btnSend.setPosition(0, 0);
 		final MessageBoxCallback sendTurnCallback = new MessageBoxCallback() {
 			@Override
 			public void onEvent(int type, Object data) {
 				if (type == MessageBoxCallback.YES) {
 					GameEngine.showLoading();
 					world.sendTurn();
 				}
 				else {
 					MessageBox.build().hide();
 					setReadInput(true);
 				}
 			}
 		};
 		ClickListener sendListener = new ClickListener() {
 			@Override
 			public void clicked(InputEvent event, float x, float y) {
 				if (gameRender.canSend()) {
 					setReadInput(false);
 					MessageBox.build()
 							.setMessage("Comander!\nTroops are ready and waiting for battle!\nJust say the word")
 							.twoButtonsLayout("Charge!!", "Hold your horses!")
 							.setCallback(sendTurnCallback)
 							.setHideOnAction(false)
 							.show();
 				} else {
 					gameRender.onSend();
 				}
 			}
 		};
 		btnSend.addListener(sendListener);
 		grpBtnSend.addActor(btnSend);
 		grpBtnSend.setSize(imgBtnSendBackground.getWidth(), imgBtnSendBackground.getHeight());
 		grpBtnSend.setPosition(-grpBtnSend.getWidth(), 0);
 
 		addActor(grpBtnOptions);
 		addActor(grpOptions);
 		addActor(grpBtnSend);
 	}
 
 	private void back() {
 		pause();
 		MessageBox.build()
 				.setMessage("If we leave now Commander,\ntroops will lose the given formation!")
 				.twoButtonsLayout("Do it anyway", "Let me think...")
 				.setCallback(backCallback)
 				.setHideOnAction(false)
 				.show();
 	}
 
 	private void showOptions() {
 		GameEngine.start(Timeline.createSequence()
 				.push(Tween.to(grpBtnOptions, ActorAccessor.X, CrystalClash.FAST_ANIMATION_SPEED)
 						.target(-grpBtnOptions.getWidth()))
 				.push(Tween.to(grpOptions, ActorAccessor.X, CrystalClash.FAST_ANIMATION_SPEED)
 						.target(75)
 						.ease(TweenEquations.easeOutCirc)));
 	}
 
 	private void hideOptions() {
 		GameEngine.start(Timeline.createSequence()
 				.push(Tween.to(grpOptions, ActorAccessor.X, CrystalClash.FAST_ANIMATION_SPEED)
 						.target(-grpOptions.getWidth()))
 				.push(Tween.to(grpBtnOptions, ActorAccessor.X, CrystalClash.FAST_ANIMATION_SPEED)
 						.target(grpBtnSend.getWidth() - 35)
 						.ease(TweenEquations.easeOutCirc)));
 	}
 
 	public void showHuds() {
 		GameEngine.start(Timeline.createSequence()
 				.push(Tween.to(grpBtnSend, ActorAccessor.X, CrystalClash.FAST_ANIMATION_SPEED)
 						.target(0)
 						.ease(TweenEquations.easeOutCirc))
 				.push(Tween.to(grpBtnOptions, ActorAccessor.X, CrystalClash.FAST_ANIMATION_SPEED)
 						.target(grpBtnSend.getWidth() - 35)
 						.ease(TweenEquations.easeOutCirc)));
 	}
 
 	public void dispose() {
 		txrTerrain.dispose();
 	}
 
 	public void init() {
 	}
 
 	@Override
 	public boolean keyDown(int keycode) {
 		if (keycode == Keys.BACK)
 			back();
 		return true;
 	}
 
 	@Override
 	public boolean keyUp(int keycode) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean keyTyped(char character) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
 		if (readInput) {
 			Vector2 vec = GameEngine.getRealPosition(screenX, screenY);
 
 			if (hideMoreOptions
 					&& (vec.x > imgOptionsBackground.getX() + imgOptionsBackground.getWidth() || vec.y > btnSurrender
 							.getTop() + 25)) {
 				hideOptions();
 			}
 			gameRender.touchDown(vec.x, vec.y, pointer, button);
 		}
 		return false;
 	}
 
 	@Override
 	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
 		if (readInput) {
 			Vector2 vec = GameEngine.getRealPosition(screenX, screenY);
 			gameRender.touchUp(vec.x, vec.y, pointer, button);
 		}
 		return false;
 	}
 
 	@Override
 	public boolean touchDragged(int screenX, int screenY, int pointer) {
 		if (readInput) {
 			Vector2 vec = GameEngine.getRealPosition(screenX, screenY);
 			gameRender.touchDragged(vec.x, vec.y, pointer);
 		}
 		return false;
 	}
 
 	@Override
 	public boolean mouseMoved(int screenX, int screenY) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean scrolled(int amount) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	public Timeline pushEnterAnimation(Timeline t) {
 		return t;
 	}
 
 	public Timeline pushExitAnimation(Timeline t) {
 		t.beginSequence();
 		t.push(Tween.to(grpOptions, ActorAccessor.X, CrystalClash.FAST_ANIMATION_SPEED)
 				.target(-grpOptions.getWidth()))
 				.push(Tween.to(grpBtnOptions, ActorAccessor.X, CrystalClash.FAST_ANIMATION_SPEED)
 						.target(-grpBtnOptions.getWidth()))
 				.push(Tween.to(grpBtnSend, ActorAccessor.X, CrystalClash.FAST_ANIMATION_SPEED)
 						.target(-grpBtnSend.getWidth()))
 				.end();
 
 		gameRender.pushExitAnimation(t);
 		return t;
 	}
 
 	public void setReadInput(boolean read) {
 		readInput = read;
 	}
 
 	public void pause() {
 		gameRender.pause();
 	}
 
 	public void resume() {
 		gameRender.resume();
 	}
 }
