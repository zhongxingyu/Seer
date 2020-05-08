 package com.me.Roguish.Screens;
 import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
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
 import com.badlogic.gdx.scenes.scene2d.Actor;
 import com.badlogic.gdx.utils.Scaling;
 import com.badlogic.gdx.utils.Array;
 
 import com.me.Roguish.Roguish;
 import com.me.Roguish.Model.ClassCard;
 import com.me.Roguish.Model.Level;
 
 public class ChooseAbilitiesScreen extends AbstractScreen{
 	private static final int MAX_CARDS = 20;
 	private int cardNo = 0;
 	private int abilNo = 0;
 	private Array<Integer> abilities = new Array<Integer>();
 	
 	private ClassCard cCard;
 	
 	private TextureAtlas guiAtlas;
 	private TextureAtlas entAtlas;
	private Music music;
 	
 	// Gui TextureRegions
 	private TextureRegion leftUp;
 	private TextureRegion leftDown;
 	private TextureRegion rightUp;
 	private TextureRegion rightDown;
 	private TextureRegion nextUp;
 	private TextureRegion nextDown;
 	private TextureRegion backUp;
 	private TextureRegion backDown;
 	private TextureRegion addUp;
 	private TextureRegion addDown;
 	private TextureRegion delUp;
 	private TextureRegion delDown;
 	private TextureRegion bg;
 	private TextureRegion cardRing;
 	
 	private ButtonStyle leftStyle;
 	private ButtonStyle rightStyle;
 	private ButtonStyle nextStyle;
 	private ButtonStyle backStyle;
 	private ButtonStyle addStyle;
 	private ButtonStyle delStyle;
 	
 	// Entity TextureRegions
 	private TextureRegion c_basicaxe;
 	private TextureRegion c_basicbow;
 	private TextureRegion c_basicxbow;
 	private TextureRegion c_basicshuriken;
 	private TextureRegion c_basicstaff;
 	private TextureRegion c_basicsword;
 	private TextureRegion c_basicwand;
 	private TextureRegion c_galoshes;
 	private TextureRegion c_gauntlet;
 	private TextureRegion c_healthboost;
 	private TextureRegion c_manaboost;
 	private TextureRegion c_chaos;
 	private TextureRegion c_teleport;
 	private TextureRegion c_drain;
 	private TextureRegion c_touchdrain;
 	
 	
 	
 	private TextureRegion d_basicaxe;
 	private TextureRegion d_basicbow;
 	private TextureRegion d_basicxbow;
 	private TextureRegion d_basicshuriken;
 	private TextureRegion d_basicstaff;
 	private TextureRegion d_basicsword;
 	private TextureRegion d_basicwand;
 	private TextureRegion d_galoshes;
 	private TextureRegion d_gauntlet;
 	private TextureRegion d_healthboost;
 	private TextureRegion d_manaboost;
 	private TextureRegion d_chaos;
 	private TextureRegion d_teleport;
 	private TextureRegion d_drain;
 	private TextureRegion d_touchdrain;
 	
 	private Image c_ent0;
 	private Image d_ent0;
 	private Image c_ent1;
 	private Image d_ent1;
 	private Image c_ent2;
 	private Image d_ent2;
 	private Image c_ent3;
 	private Image d_ent3;
 	private Image c_ent4;
 	private Image d_ent4;
 	private Image c_ent5;
 	private Image d_ent5;
 	private Image c_ent6;
 	private Image d_ent6;
 	private Image c_ent7;
 	private Image d_ent7;
 	private Image c_ent8;
 	private Image d_ent8;
 	private Image c_ent9;
 	private Image d_ent9;
 	private Image c_ent10;
 	private Image d_ent10;
 	private Image c_ent11;
 	private Image d_ent11;
 	private Image c_ent12;
 	private Image d_ent12;
 	private Image c_ent13;
 	private Image d_ent13;
 	private Image c_ent14;
 	private Image d_ent14;
 
 	
 	// CardRing images
 	private Image cRing;
 	private Image cRing0;
 	private Image cRing1;
 	private Image cRing2;
 	private Image cRing3;
 	private Image cRing4;
 	
 	public ChooseAbilitiesScreen(Roguish game, ClassCard cCard){
 		super(game);
 		this.cCard = cCard;
 		System.out.println("Entered ChooseClassScreen");
 		
 		//for(int i = 0; i < abilities.size; i++)
 		//	abilities.set(i,-1);
 	}
 	
 	@Override
     public void show(){
 		System.out.println("ChooseClassScreen:Show()");
 		Gdx.input.setInputProcessor(stage);
 		
 		loadGui();    // load guiAtlas and button textures
 		loadEnt();    // load entAtlas and ent textures
 		loadStyles(); // load button styles
 		
 		// Create Images
 		getEntImages();
 		getRingImages();
 		ringMove(cRing, cardNo);
 		updateAlphasRing(0);
 		updateAlphasOff(-1);
 		updateAlphasOn(cardNo);
 				
 		// Create Buttons
 		Button leftButton = new Button(leftStyle);
 		Button rightButton = new Button(rightStyle);
 		Button nextButton = new Button(nextStyle);
 		Button backButton = new Button(backStyle);
 		Button addButton = new Button(addStyle);
 		Button delButton = new Button(delStyle);
 		
 		// Create Tables
 		Table table1 = new Table();  // Back and Next
 		Table table2 = new Table();  // Left and Right
 		Table table3 = new Table();  // Add  and Remove
 		table1.setSize(480, 320);
 		table2.setSize(480, 320);
 		table3.setSize(480, 320);
 		table1.setBackground(new TextureRegionDrawable(bg));
 		
 		stage.addActor(table1);
 		stage.addActor(table2);
 		stage.addActor(table3);
 		stage.addActor(c_ent0);
 		stage.addActor(d_ent0);
 		stage.addActor(c_ent1);
 		stage.addActor(d_ent1);
 		stage.addActor(c_ent2);
 		stage.addActor(d_ent2);
 		stage.addActor(c_ent3);
 		stage.addActor(d_ent3);
 		stage.addActor(c_ent4);
 		stage.addActor(d_ent4);
 		stage.addActor(c_ent5);
 		stage.addActor(d_ent5);
 		stage.addActor(c_ent6);
 		stage.addActor(d_ent6);
 		stage.addActor(c_ent7);
 		stage.addActor(d_ent7);
 		stage.addActor(c_ent8);
 		stage.addActor(d_ent8);
 		stage.addActor(c_ent9);
 		stage.addActor(d_ent9);
 		stage.addActor(c_ent10);
 		stage.addActor(d_ent10);
 		stage.addActor(c_ent11);
 		stage.addActor(d_ent11);
 		stage.addActor(c_ent12);
 		stage.addActor(d_ent12);
 		stage.addActor(c_ent13);
 		stage.addActor(d_ent13);
 		stage.addActor(c_ent14);
 		stage.addActor(d_ent14);
 		stage.addActor(cRing);
 		stage.addActor(cRing0);
 		stage.addActor(cRing1);
 		stage.addActor(cRing2);
 		stage.addActor(cRing3);
 		stage.addActor(cRing4);
 		
 		table1.left().padLeft(38).padTop(4);
 		table1.debug();
 		table2.right().top().padRight(75).padTop(55);
 		table3.bottom().right().padRight(32).padBottom(2);
 
 		backButton.addListener(new InputListener() {
 			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
 				System.out.println("Back Button Down");
 				game.setScreen(new ChooseClassScreen(game));
 				return false;
 			}
 		});
 		table1.add(backButton).padBottom(195);
 		table1.row();
 		
 		nextButton.addListener(new InputListener() {
 			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
 				System.out.println("Next Button Down, Class cardNo: " + cardNo);
 				if(abilities.size == 5)
 					game.setScreen(new GameScreen(game, cCard, abilities));
 				return false;
 			}
 		});
 		table1.add(nextButton);
 		
 		System.out.println("ChooseClassScreen:Show():Action Listeners");
 		leftButton.addListener(new InputListener() {
 			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
 				updateAlphasOff(cardNo);
 				updateCardNo(-1);
 				updateAlphasOn(cardNo);
 				ringMove(cRing, cardNo);
 				System.out.println("Left Button Down, CardNo: " + cardNo);
 				return false;
 			}
 		});
 		table2.add(leftButton).padRight(85);
 		
 		rightButton.addListener(new InputListener() {
 			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
 				updateAlphasOff(cardNo);
 				updateCardNo(1);
 				updateAlphasOn(cardNo);
 				ringMove(cRing, cardNo);
 				System.out.println("Left Button Down, CardNo: " + cardNo);
 				return false;
 			}
 		});
 		table2.add(rightButton);
 
 		addButton.addListener(new InputListener() {
 			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
 				System.out.println("Add Down, cardNo: " + cardNo);
 				addAbility(cardNo);
 				return false;
 			}
 		});
 		table3.add(addButton).padRight(35);
 		
 		delButton.addListener(new InputListener() {
 			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
 				System.out.println("Remove Down, cardNo: " + cardNo);
 				delAbility(cardNo);
 				return false;
 			}
 		});
 		table3.add(delButton);
 		
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
 		addUp = guiAtlas.findRegion("Btn_Add");
 		addDown = guiAtlas.findRegion("Btn_Add_Click");
 		delUp = guiAtlas.findRegion("Btn_Remove");
 		delDown = guiAtlas.findRegion("Btn_Remove_Click");
 		
 		bg = guiAtlas.findRegion("Choose");
 		cardRing = guiAtlas.findRegion("CardRing");
		
		music = Gdx.audio.newMusic(Gdx.files.internal("data/music/Dragon_Dungeon_Predator.mp3"));
		music.setLooping(true);
		music.play();
 	}
 	
 	private void loadEnt(){
 		System.out.println("ChooseClassScreen:LoadEnt()");
 		entAtlas = new TextureAtlas(Gdx.files.internal("data/entity/pack/Entities.pack"));
 		
 		c_basicaxe = entAtlas.findRegion("C_BasicAxe");
 		d_basicaxe = entAtlas.findRegion("D_Sword");				// Need
 		c_basicbow = entAtlas.findRegion("C_BasicBow");
 		d_basicbow = entAtlas.findRegion("D_Bow");					
 		c_basicxbow = entAtlas.findRegion("C_BasicCrossbow");
 		d_basicxbow = entAtlas.findRegion("D_Bow");                 // Need
 		c_basicshuriken = entAtlas.findRegion("C_BasicShuriken");
 		d_basicshuriken = entAtlas.findRegion("D_Stars");
 		c_basicstaff  = entAtlas.findRegion("C_BasicStaff");
 		d_basicstaff  = entAtlas.findRegion("D_Staff");
 		c_basicsword = entAtlas.findRegion("C_BasicSword");
 		d_basicsword = entAtlas.findRegion("D_Sword");
 		c_basicwand = entAtlas.findRegion("C_BasicWand");
 		d_basicwand = entAtlas.findRegion("D_Staff");  			// Need
 		c_galoshes = entAtlas.findRegion("C_Galoshes");
 		d_galoshes = entAtlas.findRegion("D_Boots");
 		c_gauntlet = entAtlas.findRegion("C_Gauntlet");
 		d_gauntlet = entAtlas.findRegion("D_Glove");
 		c_healthboost = entAtlas.findRegion("C_HealthBoost");
 		d_healthboost = entAtlas.findRegion("D_Plus_R");		// Need
 		c_manaboost = entAtlas.findRegion("C_ManaBoost");
 		d_manaboost = entAtlas.findRegion("D_Plus_B");
 		c_chaos = entAtlas.findRegion("C_Mage");  				//Need
 		d_chaos = entAtlas.findRegion("D_RandomChaos");
 		c_teleport = entAtlas.findRegion("C_Mage");				// Need
 		d_teleport = entAtlas.findRegion("D_Teleport");
 		c_drain = entAtlas.findRegion("C_Mage");				// Need
 		d_drain = entAtlas.findRegion("D_LifeSteal");
 		c_touchdrain = entAtlas.findRegion("C_Mage");			//Need
 		d_touchdrain = entAtlas.findRegion("D_LifeStealTouch");
 		
 		
 	}
 	
 	private void loadStyles(){
 		leftStyle = new ButtonStyle();
 		leftStyle.up = new TextureRegionDrawable(leftUp);
 		leftStyle.down = new TextureRegionDrawable(leftDown);
 				
 		rightStyle = new ButtonStyle();
 		rightStyle.up = new TextureRegionDrawable(rightUp);
 		rightStyle.down = new TextureRegionDrawable(rightDown);
 
 		nextStyle = new ButtonStyle();
 		nextStyle.up = new TextureRegionDrawable(nextUp);
 		nextStyle.down = new TextureRegionDrawable(nextDown);
 		
 		backStyle = new ButtonStyle();
 		backStyle.up = new TextureRegionDrawable(backUp);
 		backStyle.down = new TextureRegionDrawable(backDown);
 		
 		addStyle = new ButtonStyle();
 		addStyle.up = new TextureRegionDrawable(addUp);
 		addStyle.down = new TextureRegionDrawable(addDown);
 		
 		delStyle = new ButtonStyle();
 		delStyle.up = new TextureRegionDrawable(delUp);
 		delStyle.down = new TextureRegionDrawable(delDown);
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
 			case 4:
 				c_ent4.addAction(Actions.alpha(1));
 				d_ent4.addAction(Actions.alpha(1));
 				break;
 			case 5:
 				c_ent5.addAction(Actions.alpha(1));
 				d_ent5.addAction(Actions.alpha(1));
 				break;
 			case 6:
 				c_ent6.addAction(Actions.alpha(1));
 				d_ent6.addAction(Actions.alpha(1));
 				break;
 			case 7:
 				c_ent7.addAction(Actions.alpha(1));
 				d_ent7.addAction(Actions.alpha(1));
 				break;
 			case 8:
 				c_ent8.addAction(Actions.alpha(1));
 				d_ent8.addAction(Actions.alpha(1));
 				break;
 			case 9:
 				c_ent9.addAction(Actions.alpha(1));
 				d_ent9.addAction(Actions.alpha(1));
 				break;
 			case 10:
 				c_ent10.addAction(Actions.alpha(1));
 				d_ent10.addAction(Actions.alpha(1));
 				break;
 			case 11:
 				c_ent11.addAction(Actions.alpha(1));
 				d_ent11.addAction(Actions.alpha(1));
 				break;
 			case 12:
 				c_ent12.addAction(Actions.alpha(1));
 				d_ent12.addAction(Actions.alpha(1));
 				break;
 			case 13:
 				c_ent13.addAction(Actions.alpha(1));
 				d_ent13.addAction(Actions.alpha(1));
 				break;
 			case 14:
 				c_ent14.addAction(Actions.alpha(1));
 				d_ent14.addAction(Actions.alpha(1));
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
 			case 4:
 				c_ent4.addAction(Actions.alpha(0));
 				d_ent4.addAction(Actions.alpha(0));
 				break;
 			case 5:
 				c_ent5.addAction(Actions.alpha(0));
 				d_ent5.addAction(Actions.alpha(0));
 				break;
 			case 6:
 				c_ent6.addAction(Actions.alpha(0));
 				d_ent6.addAction(Actions.alpha(0));
 				break;
 			case 7:
 				c_ent7.addAction(Actions.alpha(0));
 				d_ent7.addAction(Actions.alpha(0));
 				break;
 			case 8:
 				c_ent8.addAction(Actions.alpha(0));
 				d_ent8.addAction(Actions.alpha(0));
 				break;
 			case 9:
 				c_ent9.addAction(Actions.alpha(0));
 				d_ent9.addAction(Actions.alpha(0));
 				break;
 			case 10:
 				c_ent10.addAction(Actions.alpha(0));
 				d_ent10.addAction(Actions.alpha(0));
 				break;
 			case 11:
 				c_ent11.addAction(Actions.alpha(0));
 				d_ent11.addAction(Actions.alpha(0));
 				break;
 			case 12:
 				c_ent12.addAction(Actions.alpha(0));
 				d_ent12.addAction(Actions.alpha(0));
 				break;
 			case 13:
 				c_ent13.addAction(Actions.alpha(0));
 				d_ent13.addAction(Actions.alpha(0));
 				break;
 			case 14:
 				c_ent14.addAction(Actions.alpha(0));
 				d_ent14.addAction(Actions.alpha(0));
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
 				c_ent4.addAction(Actions.alpha(0));
 				d_ent4.addAction(Actions.alpha(0));
 				c_ent5.addAction(Actions.alpha(0));
 				d_ent5.addAction(Actions.alpha(0));
 				c_ent6.addAction(Actions.alpha(0));
 				d_ent6.addAction(Actions.alpha(0));
 				c_ent7.addAction(Actions.alpha(0));
 				d_ent7.addAction(Actions.alpha(0));
 				c_ent8.addAction(Actions.alpha(0));
 				d_ent8.addAction(Actions.alpha(0));
 				c_ent9.addAction(Actions.alpha(0));
 				d_ent9.addAction(Actions.alpha(0));
 				c_ent10.addAction(Actions.alpha(0));
 				d_ent10.addAction(Actions.alpha(0));
 				c_ent11.addAction(Actions.alpha(0));
 				d_ent11.addAction(Actions.alpha(0));
 				c_ent12.addAction(Actions.alpha(0));
 				d_ent12.addAction(Actions.alpha(0));
 				c_ent13.addAction(Actions.alpha(0));
 				d_ent13.addAction(Actions.alpha(0));
 				c_ent14.addAction(Actions.alpha(0));
 				d_ent14.addAction(Actions.alpha(0));
 				break;
 		}
 	}
 	
 	public Actor getRing(int ringNo){
 		switch(ringNo){
 		case 0:
 			return cRing0;
 		case 1:
 			return cRing1;
 		case 2:
 			return cRing2;
 		case 3:
 			return cRing3;
 		case 4:
 			return cRing4;
 		default:
 			return cRing0;
 		}
 	}
 	
 	public void updateAlphasRing(Actor a, int al){
 		a.addAction(Actions.alpha(al));
 	}
 
 	public void updateAlphasRing(int al){
 		cRing0.addAction(Actions.alpha(al));
 		cRing1.addAction(Actions.alpha(al));
 		cRing2.addAction(Actions.alpha(al));
 		cRing3.addAction(Actions.alpha(al));
 		cRing4.addAction(Actions.alpha(al));
 	}
 	
 	private void ringMove(Actor a, int pos){
 		if (pos < 10) 
 			a.addAction(Actions.moveTo(214 + (pos % 10)*24, 99));
 		else
 			a.addAction(Actions.moveTo(214 + (pos % 10)*24, 99 - 33));
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
 		int cx = 20;
 		int cy = 55;
 		int dx = 316;
 		int dy = 225;
 				
 		c_ent0 = new Image(c_basicaxe);
 		c_ent1 = new Image(c_basicbow);
 		c_ent2 = new Image(c_basicxbow);
 		c_ent3 = new Image(c_basicshuriken);
 		c_ent4 = new Image(c_basicstaff);
 		c_ent5 = new Image(c_basicsword);
 		c_ent6 = new Image(c_basicwand);
 		c_ent7 = new Image(c_galoshes);
 		c_ent8 = new Image(c_gauntlet);
 		c_ent9 = new Image(c_healthboost);
 		c_ent10 = new Image(c_manaboost);
 		c_ent11 = new Image(c_chaos);
 		c_ent12 = new Image(c_teleport);
 		c_ent13 = new Image(c_drain);
 		c_ent14 = new Image(c_touchdrain);
 		
 		c_ent0.setScaling(Scaling.fill);
 		c_ent1.setScaling(Scaling.fill);
 		c_ent2.setScaling(Scaling.fill);
 		c_ent3.setScaling(Scaling.fill);
 		c_ent4.setScaling(Scaling.fill);
 		c_ent5.setScaling(Scaling.fill);
 		c_ent6.setScaling(Scaling.fill);
 		c_ent7.setScaling(Scaling.fill);
 		c_ent8.setScaling(Scaling.fill);
 		c_ent9.setScaling(Scaling.fill);
 		c_ent10.setScaling(Scaling.fill);
 		c_ent11.setScaling(Scaling.fill);
 		c_ent12.setScaling(Scaling.fill);
 		c_ent13.setScaling(Scaling.fill);
 		c_ent14.setScaling(Scaling.fill);
 		c_ent0.setPosition(cx, cy);
 		c_ent1.setPosition(cx, cy);
 		c_ent2.setPosition(cx, cy);
 		c_ent3.setPosition(cx, cy);
 		c_ent4.setPosition(cx, cy);
 		c_ent5.setPosition(cx, cy);
 		c_ent6.setPosition(cx, cy);
 		c_ent7.setPosition(cx, cy);
 		c_ent8.setPosition(cx, cy);
 		c_ent9.setPosition(cx, cy);
 		c_ent10.setPosition(cx, cy);
 		c_ent11.setPosition(cx, cy);
 		c_ent12.setPosition(cx, cy);
 		c_ent13.setPosition(cx, cy);
 		c_ent14.setPosition(cx, cy);
 		
 		// decks
 		d_ent0 = new Image(d_basicaxe);
 		d_ent1 = new Image(d_basicbow);
 		d_ent2 = new Image(d_basicxbow);
 		d_ent3 = new Image(d_basicshuriken);
 		d_ent4 = new Image(d_basicstaff);
 		d_ent5 = new Image(d_basicsword);
 		d_ent6 = new Image(d_basicwand);
 		d_ent7 = new Image(d_galoshes);
 		d_ent8 = new Image(d_gauntlet);
 		d_ent9 = new Image(d_healthboost);
 		d_ent10 = new Image(d_manaboost);
 		d_ent11 = new Image(d_chaos);
 		d_ent12 = new Image(d_teleport);
 		d_ent13 = new Image(d_drain);
 		d_ent14 = new Image(d_touchdrain);
 		d_ent0.setScaling(Scaling.fill);
 		d_ent1.setScaling(Scaling.fill);
 		d_ent2.setScaling(Scaling.fill);
 		d_ent3.setScaling(Scaling.fill);
 		d_ent4.setScaling(Scaling.fill);
 		d_ent5.setScaling(Scaling.fill);
 		d_ent6.setScaling(Scaling.fill);
 		d_ent7.setScaling(Scaling.fill);
 		d_ent8.setScaling(Scaling.fill);
 		d_ent9.setScaling(Scaling.fill);
 		d_ent10.setScaling(Scaling.fill);
 		d_ent11.setScaling(Scaling.fill);
 		d_ent12.setScaling(Scaling.fill);
 		d_ent13.setScaling(Scaling.fill);
 		d_ent14.setScaling(Scaling.fill);
 		d_ent0.setPosition(dx, dy);
 		d_ent1.setPosition(dx, dy);
 		d_ent2.setPosition(dx, dy);
 		d_ent3.setPosition(dx, dy);
 		d_ent4.setPosition(dx, dy);
 		d_ent5.setPosition(dx, dy);
 		d_ent6.setPosition(dx, dy);
 		d_ent7.setPosition(dx, dy);
 		d_ent8.setPosition(dx, dy);
 		d_ent9.setPosition(dx, dy);
 		d_ent10.setPosition(dx, dy);
 		d_ent11.setPosition(dx, dy);
 		d_ent12.setPosition(dx, dy);
 		d_ent13.setPosition(dx, dy);
 		d_ent14.setPosition(dx, dy);
 	}
 	
 	private void getRingImages(){
 		cRing = new Image(cardRing);
 		cRing0 = new Image(cardRing);
 		cRing1 = new Image(cardRing);
 		cRing2 = new Image(cardRing);
 		cRing3 = new Image(cardRing);
 		cRing4 = new Image(cardRing);
 		cRing.setScaling(Scaling.fill);
 		cRing0.setScaling(Scaling.fill);
 		cRing1.setScaling(Scaling.fill);
 		cRing2.setScaling(Scaling.fill);
 		cRing3.setScaling(Scaling.fill);
 		cRing4.setScaling(Scaling.fill);
 	}
 	
 	
 	// Adds an ability value cardNo to abilities
 	private void addAbility(int cardNo){
 		System.out.println("Size: " + abilities.size);
 		if(!abilities.contains(cardNo, true) && (abilNo < 5)){
 			System.out.println("adding");
 			abilities.add(cardNo);
 			System.out.println("added");
 			abilNo++;
 			updateRings();
 			for(int i =0; i < abilities.size; i++){
 				System.out.println(abilities.get(i));
 			}
 		}
 	}
 	
 	// Deletes an ability value cardNo to abilities
 	private void delAbility(int cardNo){
 		System.out.println("Size: " + abilities.size);
 		if(abilities.contains(cardNo, true) && abilNo > 0){
 			abilities.removeValue(cardNo, true);
 			abilNo--;
 			updateRings();
 		}
 	}
 	
 	private void updateRings(){
 		for(int i = 0; i < 5; i++){
 			if(i < abilNo){
 				ringMove(getRing(i), abilities.get(i));
 				updateAlphasRing(getRing(i),100);
 			}
 			else{
 				updateAlphasRing(getRing(i),0);
 			}
 		}
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
