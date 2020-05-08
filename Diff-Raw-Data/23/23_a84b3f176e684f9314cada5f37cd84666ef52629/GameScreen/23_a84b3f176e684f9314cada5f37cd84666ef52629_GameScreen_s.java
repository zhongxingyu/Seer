 package com.me.Roguish.Screens;
 
 import com.badlogic.gdx.InputProcessor;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Input.Keys;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.graphics.g2d.TextureAtlas;
 import com.badlogic.gdx.scenes.scene2d.ui.Table;
 import com.badlogic.gdx.scenes.scene2d.ui.Button;
 import com.badlogic.gdx.scenes.scene2d.ui.Button.ButtonStyle;
 import com.badlogic.gdx.scenes.scene2d.ui.Image;
 import com.badlogic.gdx.scenes.scene2d.utils.Align;
 import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
 import com.badlogic.gdx.scenes.scene2d.InputEvent;
 import com.badlogic.gdx.scenes.scene2d.InputListener;
 import com.badlogic.gdx.utils.Array;
 import com.badlogic.gdx.utils.Scaling;
 import com.esotericsoftware.tablelayout.Value;
 
 import com.me.Roguish.Roguish;
 import com.me.Roguish.Model.HeroUnit;
 import com.me.Roguish.Model.Level;
 import com.me.Roguish.View.LevelRenderer;
 import com.me.Roguish.Controller.LevelController;
 import com.me.Roguish.Model.ClassCard;
 
 public class GameScreen extends AbstractScreen implements InputProcessor {
 	private Level level;
 	private LevelRenderer renderer;
 	private LevelController controller;
 	private ClassCard cCard;
 	
 	private int width, height;
 	
 	private TextureAtlas guiAtlas;
 	private TextureAtlas entAtlas;
 	
 	
 	private TextureRegion ability1;
 	private TextureRegion ability2;
 	private TextureRegion ability3;
 	private TextureRegion ability4;
 	private TextureRegion ability5;
 	
 	private ButtonStyle ability1Style;
 	private ButtonStyle ability2Style;
 	private ButtonStyle ability3Style;
 	private ButtonStyle ability4Style;
 	private ButtonStyle ability5Style;
 	
 	
 	private TextureRegion menuUp;
 	private TextureRegion menuDown;
 	private TextureRegion c_hero;
 	private TextureRegion hud;
 	private ButtonStyle menuStyle;
 	private Image ihud;
 	private Image ic_hero;
 	public static boolean level1 = false;
 	public static boolean level2 = false;
 	public static boolean level3 = false;
 
  
 	public GameScreen(Roguish game, ClassCard cCard){
         super(game);
         this.cCard = cCard;
         
 		level = new Level(cCard);
 		renderer = new LevelRenderer(level, true);
 		controller = new LevelController(level, renderer);
     }
 	
 	public GameScreen(Roguish game, ClassCard cCard, Array<Integer> abilities) {
 		super(game);
         this.cCard = cCard;
         
 		level = new Level(cCard, abilities);
 		renderer = new LevelRenderer(level, true);
 		controller = new LevelController(level, renderer);
 	}
 
 	public GameScreen(Roguish game, ClassCard cCard, HeroUnit hero) {
 		super(game);
 		this.cCard = cCard;
 		level = new Level(cCard, hero);
 		renderer = new LevelRenderer(level, true);
 		controller = new LevelController(level, renderer);
 	}
 
 	@Override
 	public void show() {
 		Gdx.input.setInputProcessor(this);	
 		
 		loadGui();
 		loadStyles();
 		
 		//Button menuButton = new Button(menuStyle);
 		Button ability1Button = new Button(ability1Style);
 		Button ability2Button = new Button(ability2Style);
 		Button ability3Button = new Button(ability3Style);
 		Button ability4Button = new Button(ability4Style);
 		Button ability5Button = new Button(ability5Style);
 		System.out.println("delta: " + (Gdx.graphics.getWidth() - stage.getWidth()));
 		System.out.println("screen: " + Gdx.graphics.getWidth());
 		System.out.println("stage: " + stage.getWidth());
 		
 		
 		Table table2 = new Table();
 		table2.setSize(480, 320);
 		
 		stage.addActor(table2);
 		
 		ability1Button.addListener(new InputListener(){
 			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
 				System.out.println("Ability 1");
 				controller.oneReleased();
 				return false;
 			}
 		});
 		table2.add(ability1Button);
 		
 		ability2Button.addListener(new InputListener(){
 			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
 				System.out.println("Ability 2");
 				controller.twoReleased();
 				return false;
 			}
 		});
 		table2.add(ability2Button);
 		
 		ability3Button.addListener(new InputListener(){
 			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
 				System.out.println("Ability 3");
 				controller.threeReleased();
 				return false;
 			}
 		});
 		table2.add(ability3Button);
 		
 		ability4Button.addListener(new InputListener(){
 			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
 				System.out.println("Ability 4");
 				controller.fourReleased();
 				return false;
 			}
 		});
 		table2.add(ability4Button);
 		
 		ability5Button.addListener(new InputListener(){
 			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
 				System.out.println("Ability 5");
 				controller.fourReleased();
 				return false;
 			}
 		});
 		table2.add(ability5Button);
 		
 		table2.setPosition(225, -125);
 	}
 
 	private void loadStyles() {
 		ability1Style = new ButtonStyle();
 		ability1Style.up = new TextureRegionDrawable(ability1);
 		ability1Style.down = new TextureRegionDrawable(ability1);
 		
 		ability2Style = new ButtonStyle();
 		ability2Style.up = new TextureRegionDrawable(ability2);
 		ability2Style.down = new TextureRegionDrawable(ability2);
 
 		ability3Style = new ButtonStyle();
 		ability3Style.up = new TextureRegionDrawable(ability3);
 		ability3Style.down = new TextureRegionDrawable(ability3);
 		
 		ability4Style = new ButtonStyle();
 		ability4Style.up = new TextureRegionDrawable(ability4);
 		ability4Style.down = new TextureRegionDrawable(ability4);
 		
 		ability5Style = new ButtonStyle();
 		ability5Style.up = new TextureRegionDrawable(ability5);
 		ability5Style.down = new TextureRegionDrawable(ability5);
 		
 	}
 
 	private void loadGui(){
 		System.out.println("ChooseClassScreen:LoadGui()");
 		guiAtlas = new TextureAtlas(Gdx.files.internal("data/gui/pack/Gui.pack"));
 		entAtlas = new TextureAtlas(Gdx.files.internal("data/entity/pack/Entities.pack"));
 		
 		menuUp = guiAtlas.findRegion("Btn_Menu");
 		menuDown = guiAtlas.findRegion("Btn_Menu_Click");
 		hud = guiAtlas.findRegion("Hud");
 		
 		menuStyle = new ButtonStyle();
 		menuStyle.up = new TextureRegionDrawable(menuUp);
 		menuStyle.down = new TextureRegionDrawable(menuDown);
 		
 		
 		ability1 = entAtlas.findRegion(findAbilityName(level.hero.getAbilities().get(0)));
 		ability2 = entAtlas.findRegion(findAbilityName(level.hero.getAbilities().get(1)));
 		ability3 = entAtlas.findRegion(findAbilityName(level.hero.getAbilities().get(2)));
 		ability4 = entAtlas.findRegion(findAbilityName(level.hero.getAbilities().get(3)));
 		ability5 = entAtlas.findRegion(findAbilityName(level.hero.getAbilities().get(4)));
 	
 		ihud = new Image(hud);
 		ihud.setScaling(Scaling.fill);
 		
 		c_hero = entAtlas.findRegion(cCard.getFaceName());
 		ic_hero = new Image(c_hero);
 		ic_hero.setScaling(Scaling.fill);
 		ic_hero.setPosition(394, 197);
 		
 	}
 	
 	
 
 	private String findAbilityName(int i) {
 		switch(i){
 		case 0: return "D_Axe";
 		case 1: return "D_Bow";
 		case 2: return "D_Crossbow";
 		case 3: return "D_Stars";
 		case 4: return "D_Staff";
 		case 5: return "D_Sword";
 		case 6: return "D_Mage";    //need icebolt
 		case 7: return "D_Boots";
 		case 8: return "D_Glove";
 		case 9: return "D_Plus_R";
 		case 10: return "D_Plus_B";
 		case 11: return "D_RandomChaos";
 		case 12: return "D_Teleport";
 		case 13: return "D_LifeSteal";
 		case 14: return "D_LifeStealTouch";
 		case 16: return "D_Wand";
 		case 18: return "D_Fireball";
 		case 19: return "D_Mage";   // need earthquake;
 		default: return "D_Mage";
 		}
 	}
 
 	@Override
 	public void hide() {
 		Gdx.input.setInputProcessor(null);
 
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
 		//Gdx.input.setInputProcessor(null);
 		guiAtlas.dispose();
 		entAtlas.dispose();
 		stage.dispose();
 	}
 	
 	
 	@Override
 	public boolean keyDown(int keycode) {
 		if (keycode == Keys.LEFT)
 			controller.leftPressed();
 		if (keycode == Keys.RIGHT)
 			controller.rightPressed();
 		if (keycode == Keys.UP)
 			controller.upPressed();
 		if (keycode == Keys.DOWN)
 			controller.downPressed();
 		if (keycode == Keys.NUM_1)
 			controller.onePressed();
 		if (keycode == Keys.NUM_2)
 			controller.twoPressed();
 		if (keycode == Keys.NUM_3)
 			controller.threePressed();
 		if (keycode == Keys.NUM_4);
 			controller.fourPressed();
 		if (keycode == Keys.NUM_5)
 			controller.fivePressed();
 		return true;
 	}
 
 	@Override
 	public boolean keyUp(int keycode) {
 		if (keycode == Keys.LEFT)
 			controller.leftReleased();
 		if (keycode == Keys.RIGHT)
 			controller.rightReleased();
 		if (keycode == Keys.UP)
 			controller.upReleased();
 		if (keycode == Keys.DOWN)
 			controller.downReleased();
 		if (keycode == Keys.NUM_1)
 			controller.oneReleased();
 		if (keycode == Keys.NUM_2)
 			controller.twoReleased();
 		if (keycode == Keys.NUM_3)
 			controller.threeReleased();
 		if (keycode == Keys.NUM_4)
 			controller.fourReleased();
 		if (keycode == Keys.NUM_5)
 			controller.fiveReleased();
 		return true;
 	}
 
 	@Override
 	public boolean keyTyped(char character) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
 		switch(getQuadrant(screenX, screenY)){
 		case 0: controller.upReleased(); break;
 		case 1: controller.downReleased(); break;
 		case 2: controller.leftReleased(); break;
 		case 3: controller.rightReleased(); break;
 		}
 		return false;
 	}
 
 	private int getQuadrant(int x, int y) {
 		if ( x > 427 ) return 3;
 		if ( x <= 427 ) return 2;
 		if (y < 240 ) return 1;
 		else return 0;
 	}
 
 	@Override
 	public boolean touchDragged(int screenX, int screenY, int pointer) {
 		// TODO Auto-generated method stub
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
 
 	@Override
 	public void render(float delta) {
 		Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
 		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 
 		controller.update(delta);
 		renderer.render();
 		if(controller.gameOver) game.setScreen(new GameOverScreen(game));
 		
 		if(controller.gameWon && level1 && level2 && level3) game.setScreen(new GameWonScreen(game));
 		else if(controller.gameWon && level1){
 			level1 = true;
 			level2 = true;
 			game.setScreen(new GameScreen(this.game, this.cCard, this.level.getHero()));
 			
 		}
 		else if(controller.gameWon){
 			level1 = true;
 			game.setScreen(new GameScreen(this.game, this.cCard, this.level.getHero()));
 		}
 
 		stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
 		stage.draw();
 		//Table.drawDebug(stage);
 	}
 
 	@Override
 	public void resize(int width, int height) {
 		stage.setViewport(480, 320, true);
 		renderer.setSize(width, height);
 		this.width = width;
 		this.height = height;
 
 	}
 }
