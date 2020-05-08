 package inf1.oop.turnbased.screen;
 
 import java.util.Random;
 
 import inf1.oop.turnbased.TurnBasedGame;
 import inf1.oop.turnbased.combat.Battle;
 import inf1.oop.turnbased.combat.BattleEndCondition;
 import inf1.oop.turnbased.combat.BattleEndListener;
 import inf1.oop.turnbased.combat.BattleTurnListener;
 import inf1.oop.turnbased.combat.CombatEntity;
 import inf1.oop.turnbased.combat.Turn;
 import inf1.oop.turnbased.combat.TurnAction;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Screen;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.g2d.BitmapFont;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.TextureAtlas;
 import com.badlogic.gdx.scenes.scene2d.InputEvent;
 import com.badlogic.gdx.scenes.scene2d.InputListener;
 import com.badlogic.gdx.scenes.scene2d.Stage;
 import com.badlogic.gdx.scenes.scene2d.ui.Label;
 import com.badlogic.gdx.scenes.scene2d.ui.Skin;
 import com.badlogic.gdx.scenes.scene2d.ui.Table;
 import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
 import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
 import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
 import com.badlogic.gdx.scenes.scene2d.utils.Align;
 
 public class CombatScreen implements Screen {
 
 	private final TurnBasedGame game;
 	private Battle battle;
 	
 	private Stage stage;
 	private Table combatActions;
 	
 	private BitmapFont font, whiteFont;
 	private TextureAtlas atlas;
 	private Skin skin;
 	private TextButtonStyle style;
 	private SpriteBatch batch;
 	private Label combatLog;
 	private Random rng;
 	
 	private CombatEntity player, monster;
 	private final MapScreen mapScreen;
 	
 	public CombatScreen(TurnBasedGame _game, Battle battle, CombatEntity player, CombatEntity monster, MapScreen parent) {
 		this.game = _game;
 		this.battle = battle;
 		this.player = player;
 		this.monster = monster;
 		this.mapScreen = parent;
 		rng = new Random();
 		
 		battle.addBattleEndListener(new BattleEndListener() {
 			@Override
 			public void onBattleEnd(BattleEndCondition cond) {
 				if(cond == BattleEndCondition.WIN || cond == BattleEndCondition.FLEE)
 					game.setScreen(mapScreen);
 			}
 		});
 		
 		battle.addBattleTurnListener(new BattleTurnListener() {
 			@Override
 			public void onTurn(Turn t, int amount){
 				if(combatLog != null) {
 					String text = "error";
 					switch(t.getAction()) {
 					case ATTACK:
 						text = t.getSourceEntity().getName() 
 								+ " attacks " 
 								+ t.getTargetEntity().getName() 
 								+ " for "
 								+ amount;
 						break;
 						
 					case HEAL:
 						text = t.getSourceEntity().getName() 
 								+ " heals self for "
 								+ amount;
 						break;
 						
 					case FIRE:
 						text = t.getSourceEntity().getName() 
 								+ " casts fire on " 
 								+ t.getTargetEntity().getName() 
 								+ " for "
 								+ amount;
 						break;
 						
 					case ICE:
 						text = t.getSourceEntity().getName() 
 								+ " casts ice on " 
 								+ t.getTargetEntity().getName() 
 								+ " for "
 								+ amount;
 						break;
 						
 					case DEFEND:
 						text = t.getSourceEntity().getName() 
 								+ " defends itself against the next attack!";
 						break;
 						default:
 							text = "error2";
 							break;
 					}
 					
 					combatLog.setText(text);
 				}
 			}
 		});
 	}
 	
 	private void addButton(Table table, String text, InputListener listener) {
 		TextButton b = new TextButton(text, style);
 		b.addListener(listener);
 		table.add(b);
 	}
 	
 	@Override
 	public void render(float delta) {
 		Gdx.gl.glClearColor(0, 0, 0, 1);
 		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 
 		stage.act(delta);
 
 		batch.begin();
 		stage.draw();
 		batch.end();
 		
 	}
 	
 	public void setCombatText(String combatText) {
 		combatLog.setText(combatText);
 	}
 	
 	private void generateMonsterTurn() {
 		int turnType = rng.nextInt(5); // we don't want to include flee
 		Turn t = null;
 		switch(turnType) {
 		case 0:
 			t = new Turn(monster, TurnAction.ATTACK, player);
 			break;
 		case 1:
 			t = new Turn(monster, TurnAction.FIRE, player);
 			break;
 		case 2:
 			t = new Turn(monster, TurnAction.ICE, player);
 			break;
 		case 3:
 			t = new Turn(monster, TurnAction.DEFEND, monster);
 			break;
 		case 4:
 			t = new Turn(monster, TurnAction.HEAL, monster);
 			break;
 		default:
 			t = new Turn(monster, TurnAction.ATTACK, player);
 			break;
 		}
 		battle.applyTurn(t);
 	}
 	
 	@Override
 	public void resize(int width, int height) {
 		if(stage == null)
 			stage = new Stage(width, height, true);
 		Gdx.input.setInputProcessor(stage);
 		
 		combatActions = new Table();
 		
 		style = new TextButtonStyle();
 		style.up = skin.getDrawable("buttonnormal");
 		style.down = skin.getDrawable("buttonpressed");
 		style.font = font;
 		
 		addButton(combatActions, "Attack", new InputListener() {
 			@Override
 			public boolean touchDown(InputEvent event, float x, float y,
 					int pointer, int button) {
 				return true;
 			}
 
 			@Override
 			public void touchUp(InputEvent event, float x, float y,
 					int pointer, int button) {
 				battle.applyTurn(new Turn(player, TurnAction.ATTACK, monster));
 				generateMonsterTurn();
 				//combatLog.setText("Player attacks monster for 100 damage");
 			}
 		});
 		
 		addButton(combatActions, "Fire", new InputListener() {
 			@Override
 			public boolean touchDown(InputEvent event, float x, float y,
 					int pointer, int button) {
 				return true;
 			}
 
 			@Override
 			public void touchUp(InputEvent event, float x, float y,
 					int pointer, int button) {
 				battle.applyTurn(new Turn(player, TurnAction.FIRE, monster));
 				generateMonsterTurn();
 				//combatLog.setText("Player casts fire on monster for 100 damage");
 			}
 		});
 		
 		addButton(combatActions, "Ice", new InputListener() {
 			@Override
 			public boolean touchDown(InputEvent event, float x, float y,
 					int pointer, int button) {
 				return true;
 			}
 
 			@Override
 			public void touchUp(InputEvent event, float x, float y,
 					int pointer, int button) {
 				battle.applyTurn(new Turn(player, TurnAction.ICE, monster));
 				generateMonsterTurn();
 				//combatLog.setText("Player casts ice on monster for 100 damage");
 			}
 		});
 		
 		addButton(combatActions, "Heal", new InputListener() {
 			@Override
 			public boolean touchDown(InputEvent event, float x, float y,
 					int pointer, int button) {
 				return true;
 			}
 
 			@Override
 			public void touchUp(InputEvent event, float x, float y,
 					int pointer, int button) {
 				battle.applyTurn(new Turn(player, TurnAction.HEAL, player));
 				generateMonsterTurn();
 				//combatLog.setText("Player heals self for 100 health");
 			}
 		});
 		
 		addButton(combatActions, "Defend", new InputListener() {
 			@Override
 			public boolean touchDown(InputEvent event, float x, float y,
 					int pointer, int button) {
 				return true;
 			}
 
 			@Override
 			public void touchUp(InputEvent event, float x, float y,
 					int pointer, int button) {
 				battle.applyTurn(new Turn(player, TurnAction.DEFEND, player));
 				generateMonsterTurn();
 				//combatLog.setText("Player defends against attack!");
 			}
 		});
 		
 		addButton(combatActions, "Flee", new InputListener() {
 			@Override
 			public boolean touchDown(InputEvent event, float x, float y,
 					int pointer, int button) {
 				return true;
 			}
 
 			@Override
 			public void touchUp(InputEvent event, float x, float y,
 					int pointer, int button) {
 				battle.applyTurn(new Turn(player, TurnAction.FLEE, player));
 				generateMonsterTurn();
 			}
 		});
 		
 		combatActions.setWidth(width);
 		combatActions.setHeight(40);
 		combatActions.setY(10);
 		
 		stage.addActor(combatActions);
 		
 		LabelStyle ls = new LabelStyle(whiteFont, Color.WHITE);
 		
 		combatLog = new Label("", ls);
 		combatLog.setWidth(width);
 		combatLog.setHeight(40);
 		combatLog.setY(60);
 		combatLog.setAlignment(Align.center);
 		
 		stage.addActor(combatLog);
 		
 	}
 
 	@Override
 	public void show() {
 		atlas = new TextureAtlas("assets/data/button.pack");
 		skin = new Skin();
 		skin.addRegions(atlas);
 		font = new BitmapFont(Gdx.files.internal("assets/data/font.fnt"), false);
 		whiteFont = new BitmapFont(Gdx.files.internal("assets/data/whitefont.fnt"), false);
 		batch = new SpriteBatch();
 	}
 
 	@Override
 	public void hide() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void pause() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void resume() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void dispose() {
 		// TODO Auto-generated method stub
 		atlas.dispose();
 		skin.dispose();
 		font.dispose();
 		whiteFont.dispose();
 		batch.dispose();
 		stage.dispose();
 	}
 
 }
