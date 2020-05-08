 package com.me.Roguish.Screens;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.graphics.g2d.TextureAtlas;
 import com.badlogic.gdx.scenes.scene2d.actions.Actions;
 import com.badlogic.gdx.scenes.scene2d.ui.Table;
 import com.badlogic.gdx.scenes.scene2d.ui.Button;
 import com.badlogic.gdx.scenes.scene2d.ui.Button.ButtonStyle;
 import com.badlogic.gdx.scenes.scene2d.ui.Image;
 import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
 import com.badlogic.gdx.scenes.scene2d.InputEvent;
 import com.badlogic.gdx.scenes.scene2d.InputListener;
 import com.badlogic.gdx.utils.Scaling;
 import com.me.Roguish.Roguish;
 
 public class ChooseClassScreen extends AbstractScreen{
 	private static final int MAX_CARDS = 4;
 	private int cardNo = 0;
 	
 	private TextureAtlas guiAtlas;
 	private TextureAtlas entAtlas;
 	
 	// Gui TextureRegions
 	private TextureRegion leftUp;
 	private TextureRegion leftDown;
 	private TextureRegion rightUp;
 	private TextureRegion rightDown;
 	private TextureRegion nextUp;
 	private TextureRegion nextDown;
 	private TextureRegion backUp;
 	private TextureRegion backDown;
 	private TextureRegion bg;
 	private TextureRegion cardRing;
 	
 	private ButtonStyle leftStyle;
 	private ButtonStyle rightStyle;
 	private ButtonStyle nextStyle;
 	private ButtonStyle backStyle;
 	
 	// Entity TextureRegions
 	private TextureRegion c_archer;
 	private TextureRegion c_mage;
 	private TextureRegion c_ninja;
 	private TextureRegion c_warrior;
 	private TextureRegion d_archer;
 	private TextureRegion d_mage;
 	private TextureRegion d_ninja;
 	private TextureRegion d_warrior;
 	
 	private Image c_ent0;
 	private Image d_ent0;
 	private Image c_ent1;
 	private Image d_ent1;
 	private Image c_ent2;
 	private Image d_ent2;
 	private Image c_ent3;
 	private Image d_ent3;
 	
 	private Image cRing;
 	
 	public ChooseClassScreen(Roguish game){
 		super(game);
 		System.out.println("Entered ChooseClassScreen");
 	}
 	
 	@Override
     public void show(){
 		System.out.println("ChooseClassScreen:Show()");
 		Gdx.input.setInputProcessor(stage);
 		
 		loadGui();    // load guiAtlas and button textures
 		loadEnt();    // load entAtlas and ent textures
 		loadStyles(); // load button styles
 		
 		// get images based on the correct CardNo
 		getEntImages();
 		cRing = new Image(cardRing);
 		cRing.setScaling(Scaling.fill);
		cRing.addAction(Actions.moveTo(90 + cardNo*23, 96));
 		updateAlphasOff(-1);
 		updateAlphasOn(cardNo);
 		
 		cRing.setPosition(88 + cardNo*24, 96);
 		System.out.println("FFF");
 		Button leftButton = new Button(leftStyle);
 		Button rightButton = new Button(rightStyle);
 		Button nextButton = new Button(nextStyle);
 		Button backButton = new Button(backStyle);
 		
 		
 		Table table = new Table();
 		table.setSize(480, 320);
 		table.setBackground(new TextureRegionDrawable(bg));
 		
 		stage.addActor(table);
 		stage.addActor(c_ent0);
 		stage.addActor(d_ent0);
 		stage.addActor(c_ent1);
 		stage.addActor(d_ent1);
 		stage.addActor(c_ent2);
 		stage.addActor(d_ent2);
 		stage.addActor(c_ent3);
 		stage.addActor(d_ent3);
 		stage.addActor(cRing);
 		
 		
 		table.columnDefaults(1);
 		table.left().padLeft(52);
 		table.debug();
 
 		backButton.addListener(new InputListener() {
 			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
 				System.out.println("Back Button Down");
 				game.setScreen(new MenuScreen(game));
 				return false;
 			}
 		});
 		table.add(backButton).colspan(2).padBottom(25);
 		table.row();
 		
 		System.out.println("ChooseClassScreen:Show():Action Listeners");
 		leftButton.addListener(new InputListener() {
 			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
 				updateAlphasOff(cardNo);
 				updateCardNo(-1);
 				updateAlphasOn(cardNo);
 				cRing.addAction(Actions.moveTo(88 + cardNo*24, 96));
 				System.out.println("Left Button Down, CardNo: " + cardNo);
 				return false;
 			}
 		});
 		table.add(leftButton).padRight(50);
 		
 		rightButton.addListener(new InputListener() {
 			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
 				updateAlphasOff(cardNo);
 				updateCardNo(1);
 				updateAlphasOn(cardNo);
 				cRing.addAction(Actions.moveTo(88 + cardNo*24, 96));
 				System.out.println("Left Button Down, CardNo: " + cardNo);
 				return false;
 			}
 		});
 		table.add(rightButton).padLeft(50);
 		table.row();
 
 		nextButton.addListener(new InputListener() {
 			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
 				System.out.println("Next Button Down, Class cardNo: " + cardNo);
 				game.setScreen(new GameScreen(game));
 				return false;
 			}
 		});
 		table.add(nextButton).colspan(2).padTop(95);
 	}
 
 	private void loadGui(){
 		System.out.println("ChooseClassScreen:LoadGui()");
 		guiAtlas = new TextureAtlas(Gdx.files.internal("data/gui/pack/Gui.pack"));
 		
 		leftUp = guiAtlas.findRegion("Btn_Left");
 		leftDown = guiAtlas.findRegion("Btn_Left_Click");
 		rightUp = guiAtlas.findRegion("Btn_Right");
 		rightDown = guiAtlas.findRegion("Btn_Right_Click");
 		nextUp = guiAtlas.findRegion("Btn_Next");
 		nextDown = guiAtlas.findRegion("Btn_Next_Click");
 		backUp = guiAtlas.findRegion("Btn_Back");
 		backDown = guiAtlas.findRegion("Btn_Back_Click");
 		
 		bg = guiAtlas.findRegion("ChooseClass");
 		cardRing = guiAtlas.findRegion("CardRing");
 	}
 	
 	private void loadEnt(){
 		System.out.println("ChooseClassScreen:LoadEnt()");
 		entAtlas = new TextureAtlas(Gdx.files.internal("data/entity/pack/Entities.pack"));
 		
 		c_archer = entAtlas.findRegion("C_Archer");
 		d_archer = entAtlas.findRegion("D_Archer");
 		c_mage = entAtlas.findRegion("C_Mage");
 		d_mage = entAtlas.findRegion("D_Mage");
 		c_ninja = entAtlas.findRegion("C_Ninja");
 		d_ninja = entAtlas.findRegion("D_Ninja");
 		c_warrior = entAtlas.findRegion("C_Warrior");
 		d_warrior = entAtlas.findRegion("D_Warrior");
 	}
 	
 	private void loadStyles(){
 		System.out.println("ChooseClassScreen:LoadStyles()");
 		System.out.println("1a");
 		leftStyle = new ButtonStyle();
 		System.out.println("1b:");
 		leftStyle.up = new TextureRegionDrawable(leftUp);
 		System.out.println("1c");
 		leftStyle.down = new TextureRegionDrawable(leftDown);
 		System.out.println("1d");
 		
 		rightStyle = new ButtonStyle();
 		rightStyle.up = new TextureRegionDrawable(rightUp);
 		rightStyle.down = new TextureRegionDrawable(rightDown);
 
 		nextStyle = new ButtonStyle();
 		nextStyle.up = new TextureRegionDrawable(nextUp);
 		nextStyle.down = new TextureRegionDrawable(nextDown);
 		
 		backStyle = new ButtonStyle();
 		backStyle.up = new TextureRegionDrawable(backUp);
 		backStyle.down = new TextureRegionDrawable(backDown);
 		System.out.println("ChooseClassScreen:LoadStyle():done");
 	}
 	
 	
 	// adds the delta to the cardNo while keeping it in bounds - 0 < cardNo < MAX_CARDS
 	private void updateCardNo(int delta){
 		cardNo += delta;
 		if(cardNo < 0) cardNo = 0;
 		if(cardNo >= MAX_CARDS) cardNo = MAX_CARDS - 1;
 	}
 	
 	// sets alphas to 100 of the EntImages based on cardNo
 	private void updateAlphasOn(int i){
 		switch(i){
 			case 0:
 				c_ent0.addAction(Actions.alpha(1));
 				d_ent0.addAction(Actions.alpha(1));
 				break;
 			case 1:
 				c_ent1.addAction(Actions.alpha(1));
 				d_ent1.addAction(Actions.alpha(1));
 				break;
 			case 2:
 				c_ent2.addAction(Actions.alpha(1));
 				d_ent2.addAction(Actions.alpha(1));
 				break;
 			case 3:
 				c_ent3.addAction(Actions.alpha(1));
 				d_ent3.addAction(Actions.alpha(1));
 				break;
 			default:
 				break;
 		}
 	}
 	
 	// sets alphas to 100 of the EntImages based on cardNo
 	private void updateAlphasOff(int i){
 		switch(i){
 			case 0:
 				c_ent0.addAction(Actions.alpha(0));
 				d_ent0.addAction(Actions.alpha(0));
 				break;
 			case 1:
 				c_ent1.addAction(Actions.alpha(0));
 				d_ent1.addAction(Actions.alpha(0));
 				break;
 			case 2:
 				c_ent2.addAction(Actions.alpha(0));
 				d_ent2.addAction(Actions.alpha(0));
 				break;
 			case 3:
 				c_ent3.addAction(Actions.alpha(0));
 				d_ent3.addAction(Actions.alpha(0));
 				break;
 			default:
 				c_ent0.addAction(Actions.alpha(0));
 				d_ent0.addAction(Actions.alpha(0));
 				c_ent1.addAction(Actions.alpha(0));
 				d_ent1.addAction(Actions.alpha(0));
 				c_ent2.addAction(Actions.alpha(0));
 				d_ent2.addAction(Actions.alpha(0));
 				c_ent3.addAction(Actions.alpha(0));
 				d_ent3.addAction(Actions.alpha(0));
 				break;
 		}
 	}
 	
 	@Override
 	public void render(float delta) {
 		Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
 		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 
 		stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
 		stage.draw();
 
 		//Table.drawDebug(stage);
 	}
 		
 	private void getEntImages(){
 		// cards
 		c_ent0 = new Image(c_archer);
 		c_ent1 = new Image(c_mage);
 		c_ent2 = new Image(c_ninja);
 		c_ent3 = new Image(c_warrior);
 		c_ent0.setScaling(Scaling.fill);
 		c_ent1.setScaling(Scaling.fill);
 		c_ent2.setScaling(Scaling.fill);
 		c_ent3.setScaling(Scaling.fill);
 		c_ent0.setPosition(269, 43);
 		c_ent1.setPosition(269, 43);
 		c_ent2.setPosition(269, 43);
 		c_ent3.setPosition(269, 43);
 		
 		// decks
 		d_ent0 = new Image(d_archer);
 		d_ent1 = new Image(d_mage);
 		d_ent2 = new Image(d_ninja);
 		d_ent3 = new Image(d_warrior);
 		d_ent0.setScaling(Scaling.fill);
 		d_ent1.setScaling(Scaling.fill);
 		d_ent2.setScaling(Scaling.fill);
 		d_ent3.setScaling(Scaling.fill);
 		d_ent0.setPosition(118, 165);
 		d_ent1.setPosition(118, 165);
 		d_ent2.setPosition(118, 165);
 		d_ent3.setPosition(118, 165);
 	}
 	
 	@Override
 	public void dispose(){
 		guiAtlas.dispose();
 		entAtlas.dispose();
 		stage.dispose();
 		
 	}
 	
 	public void resize (int width, int height) {
 		stage.setViewport(480, 320, true);
 		stage.getCamera().translate(-stage.getGutterWidth(), -stage.getGutterHeight(), 0);
 	}
 	
 	public boolean needsGL20 () {
 		return true;
 	}
 	
 }
