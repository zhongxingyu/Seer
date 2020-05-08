 package screenCore;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import animation.AnimationPlayer;
 import animation.Bones;
 import animation.ManeStyle;
 import animation.PonyColorChangeHelper;
 import animation.TextureDictionary;
 import animation.TextureEntry;
 
 import common.PlayerCharacteristics;
 import common.Race;
 import common.SpecialStats;
 
 import GUI.IItemFormater;
 import GUI.ItemEventArgs;
 import GUI.ScrollEventArgs;
 import GUI.controls.AnimationBox;
 import GUI.controls.Button;
 import GUI.controls.ComboBox;
 import GUI.controls.ImageBox;
 import GUI.controls.Label;
 import GUI.controls.Panel;
 import GUI.controls.Slider;
 import GUI.controls.Textfield;
 import GUI.controls.ToggleButton;
 import GUI.graphics.GUIRenderingContext;
 import GUI.graphics.LookAndFeel;
 import math.Vector2;
 import misc.EventArgs;
 import misc.IEventListener;
 import utils.GameTime;
 import utils.Rectangle;
 import utils.TimeSpan;
 import content.ContentManager;
 import content.PlayerCharacteristicsWriter;
 import graphics.Color;
 import graphics.ShaderEffect;
 import graphics.SpriteBatch;
 import graphics.Texture2D;
 
 
 public class PonyCreatorScreen extends TransitioningGUIScreen {
 
 	public static final String PonyArchetypePath = "Player.archetype";
 	private GUIRenderingContext context;
 
 	private ContentManager contentManager = new ContentManager("resources");
 	private PlayerCharacteristicsWriter charWriter = new PlayerCharacteristicsWriter(contentManager);
 	
 	private AnimationPlayer ponyPlayer;
 	private PlayerCharacteristics character = new PlayerCharacteristics();
 	private TextureDictionary assetDictionary = this.contentManager.load("rddict.tdict", TextureDictionary.class);
 
 	private Vector2 ponyPosition = new Vector2(1025,96);
 	private Vector2 ponyScale = new Vector2(2,2);
 	
 	private ImageBox bG;
 	
 	private Button button;
 	private Button button2;
 	private Button button3;
 	private Textfield nameField;
 	
 	private Slider bodyRedSlider;
 	private Slider bodyGreenSlider;
 	private Slider bodyBlueSlider;
 	
 	private Slider eyeRedSlider;
 	private Slider eyeGreenSlider;
 	private Slider eyeBlueSlider;
 	
 	private Slider maneRedSlider;
 	private Slider maneGreenSlider;
 	private Slider maneBlueSlider;
 	
 	private ComboBox<ManeEntries> maneComboBox;
 	private ComboBox<EyeEntries> eyeComboBox;
 	private ComboBox<MarkEntries> markComboBox;
 	
 	private ToggleButton earthPonyButton;
 	private ToggleButton pegasusButton;
 	private ToggleButton unicornButton;
 	
 	private List<ManeEntries> maneStyles;
 	private List<EyeEntries> eyeStyles;
 	private List<MarkEntries> markStyles;
 	
 	private ToggleButton walkButton;
 	
 	private Label nameLabel;
 	private Label bodyLabel;
 	private Label eyeLabel;
 	private Label maneLabel;
 
 	private Panel namePanel;
 	private Panel racePanel;
 	private Panel bodyPanel;
 	private Panel eyePanel;
 	private Panel manePanel;
 	
 	private AnimationBox ponyBox;
 	
 	public PonyCreatorScreen(String lookAndFeelPath) {
 		super(false, TimeSpan.fromSeconds(1d), TimeSpan.fromSeconds(0.5d), lookAndFeelPath);
 	}
 
 	@Override
 	public void initialize(ContentManager contentManager) {
 		super.initialize(contentManager);
 		
 		
 		
 		this.bG = new ImageBox();
 		this.bG.setImage(contentManager.load("darkPonyville.png", Texture2D.class));
 		this.bG.setBounds(this.ScreenManager.getViewport());
 		super.addGuiControl(this.bG, new Vector2(0,this.ScreenManager.getViewport().Height), new Vector2(0,0), 
 								new Vector2(0,this.ScreenManager.getViewport().Height));
 		addPony();
 		
 		this.ponyBox = new AnimationBox();
 		this.ponyBox.setBounds(0,0,250,208);
 		this.ponyBox.setScale(ponyScale);
 		this.ponyBox.setAnimationPlayer(ponyPlayer);
 		super.addGuiControl(this.ponyBox, new Vector2(0,this.ScreenManager.getViewport().Height), ponyPosition, 
 				new Vector2(0,this.ScreenManager.getViewport().Height));
 		
 		this.maneStyles = new ArrayList<ManeEntries>();
 		ManeEntries rDManeStyle = new ManeEntries("Rainbow style!", new ManeStyle("RDUPPERMANE", "RDLOWERMANE", "RDUPPERTAIL", "RDLOWERTAIL"), this.assetDictionary);
 		this.maneStyles.add(rDManeStyle);	
 		ManeEntries tSManeStyle = new ManeEntries("Twilight style!", new ManeStyle("TSUPPERMANE", "TSLOWERMANE", "TSUPPERTAIL", "TSLOWERTAIL"), this.assetDictionary);
 		this.maneStyles.add(tSManeStyle);
 		
 		this.maneComboBox = new ComboBox<ManeEntries>();
 		this.maneComboBox.setBounds(0,200, 250, 30);
 		this.maneComboBox.setBgColor(new Color(0,0,0,0));
 		this.maneComboBox.setFont(contentManager.loadFont("Monofonto24.xml"));
 		this.maneComboBox.addItem(rDManeStyle);
 		this.maneComboBox.addItem(tSManeStyle);
 		this.maneComboBox.setItemFormater(new IItemFormater<ManeEntries>(){
 			@Override
 			public String formatItem(ManeEntries item) {
 				return item.name;
 			}
 		});
 		this.maneComboBox.addSelectedChangedListener(new IEventListener<ItemEventArgs<ManeEntries>>() {
 			@Override
 			public void onEvent(Object sender, ItemEventArgs<ManeEntries> e) {
 				setManeStyle();
 			}
 		});
 		this.maneComboBox.setSelectedIndex(0);
 		
 		this.eyeStyles = new ArrayList<EyeEntries>();
 		EyeEntries rDEyeStyle = new EyeEntries("Rainbow style!", "RDEYE", this.assetDictionary);
 		this.eyeStyles.add(rDEyeStyle);
 		EyeEntries tSEyeStyle = new EyeEntries("Twilight style!", "TSEYE", this.assetDictionary);
 		this.eyeStyles.add(tSEyeStyle);
 		
 		this.eyeComboBox = new ComboBox<EyeEntries>();
 		this.eyeComboBox.setBounds(0,200, 250, 30);
 		this.eyeComboBox.setBgColor(new Color(0,0,0,0));
 		this.eyeComboBox.setFont(contentManager.loadFont("Monofonto24.xml"));
 		this.eyeComboBox.addItem(rDEyeStyle);
 		this.eyeComboBox.addItem(tSEyeStyle);
 		this.eyeComboBox.setItemFormater(new IItemFormater<EyeEntries>(){
 			@Override
 			public String formatItem(EyeEntries item) {
 				return item.name;
 			}
 		});
 		eyeComboBox.addSelectedChangedListener(new IEventListener<ItemEventArgs<EyeEntries>>() {
 			@Override
 			public void onEvent(Object sender, ItemEventArgs<EyeEntries> e) {
 				setEyeStyle();
 			}
 		});
 		this.eyeComboBox.setSelectedIndex(0);
 		
 		this.markStyles = new ArrayList<MarkEntries>();
 		MarkEntries rDMarkStyle = new MarkEntries("Rainbow style!", "RDMARK", this.assetDictionary);
 		this.markStyles.add(rDMarkStyle);
 		MarkEntries tSMarkStyle = new MarkEntries("Twilight style!", "TSMARK", this.assetDictionary);
 		this.markStyles.add(tSMarkStyle);
 		MarkEntries mCMarkStyle = new MarkEntries("Hammer time!", "MCMARK", this.assetDictionary);
 		this.markStyles.add(mCMarkStyle);
 		
 		this.markComboBox = new ComboBox<MarkEntries>();
 		this.markComboBox.setBounds(0,200, 250, 30);
 		this.markComboBox.setBgColor(new Color(0,0,0,0));
 		this.markComboBox.setFont(contentManager.loadFont("Monofonto24.xml"));
 		this.markComboBox.addItem(rDMarkStyle);
 		this.markComboBox.addItem(tSMarkStyle);
 		this.markComboBox.addItem(mCMarkStyle);
 		this.markComboBox.setItemFormater(new IItemFormater<MarkEntries>(){
 			@Override
 			public String formatItem(MarkEntries item) {
 				return item.name;
 			}
 		});
 		markComboBox.addSelectedChangedListener(new IEventListener<ItemEventArgs<MarkEntries>>() {
 			@Override
 			public void onEvent(Object sender, ItemEventArgs<MarkEntries> e) {
 				setMarkStyle();
 			}
 		});
 		this.markComboBox.setSelectedIndex(0);
 		
 		this.nameField = new Textfield();
 		this.nameField.setBounds(0,50,250,25);
		this.nameField.setText("Name goes here");
 		this.nameField.setFont(contentManager.loadFont("Monofonto24.xml"));
 		this.nameField.setFgColor(Color.White);
 		this.nameField.setMaxLength(25);
 		
 		this.bodyRedSlider = new Slider();
 		this.bodyRedSlider.setFgColor(new Color(255,50,50,255));
 		this.bodyRedSlider.setBounds(0, 50, 250, 30);
 		this.bodyRedSlider.setScrollMax(255);
 		this.bodyRedSlider.setScrollValue(255);
 		this.bodyRedSlider.setHorizontal(true);
 		this.bodyRedSlider.addScrollListener(new IEventListener<ScrollEventArgs>() {
 			
 			@Override
 			public void onEvent(Object sender, ScrollEventArgs e) {
 				setBodyColor();
 			}
 		});
 		this.bodyGreenSlider = new Slider();
 		this.bodyGreenSlider.setFgColor(new Color(50,255,50,255));
 		this.bodyGreenSlider.setBounds(new Rectangle(0,100,250,30));
 		this.bodyGreenSlider.setScrollMax(255);
 		this.bodyGreenSlider.setScrollValue(255);
 		this.bodyGreenSlider.setHorizontal(true);
 		this.bodyGreenSlider.addScrollListener(new IEventListener<ScrollEventArgs>() {
 			
 			@Override
 			public void onEvent(Object sender, ScrollEventArgs e) {
 				setBodyColor();
 			}
 		});
 		this.bodyBlueSlider = new Slider();
 		this.bodyBlueSlider.setFgColor(new Color(50,50,255,255));
 		this.bodyBlueSlider.setBounds(new Rectangle(0,150,250,30));
 		this.bodyBlueSlider.setScrollMax(255);
 		this.bodyBlueSlider.setScrollValue(255);
 		this.bodyBlueSlider.setHorizontal(true);
 		this.bodyBlueSlider.addScrollListener(new IEventListener<ScrollEventArgs>() {
 			
 			@Override
 			public void onEvent(Object sender, ScrollEventArgs e) {
 				setBodyColor();
 			}
 		});
 
 		this.eyeRedSlider = new Slider();
 		this.eyeRedSlider.setBounds(0, 50, 250, 30);
 		this.eyeRedSlider.setFgColor(new Color(255,50,50,255));
 		this.eyeRedSlider.setScrollMax(255);
 		this.eyeRedSlider.setScrollValue(255);
 		this.eyeRedSlider.setHorizontal(true);
 		this.eyeRedSlider.addScrollListener(new IEventListener<ScrollEventArgs>() {
 			
 			@Override
 			public void onEvent(Object sender, ScrollEventArgs e) {
 				setEyeColor();
 			}
 		});
 		this.eyeGreenSlider = new Slider();
 		this.eyeGreenSlider.setBounds(0, 100, 250, 30);
 		this.eyeGreenSlider.setFgColor(new Color(50,255,50,255));
 		this.eyeGreenSlider.setScrollMax(255);
 		this.eyeGreenSlider.setScrollValue(255);
 		this.eyeGreenSlider.setHorizontal(true);
 		this.eyeGreenSlider.addScrollListener(new IEventListener<ScrollEventArgs>() {
 			
 			@Override
 			public void onEvent(Object sender, ScrollEventArgs e) {
 				setEyeColor();
 			}
 		});
 		this.eyeBlueSlider = new Slider();
 		this.eyeBlueSlider.setBounds(0, 150, 250, 30);
 		this.eyeBlueSlider.setFgColor(new Color(50,50,255,255));
 		this.eyeBlueSlider.setScrollMax(255);
 		this.eyeBlueSlider.setScrollValue(255);
 		this.eyeBlueSlider.setHorizontal(true);
 		this.eyeBlueSlider.addScrollListener(new IEventListener<ScrollEventArgs>() {
 			
 			@Override
 			public void onEvent(Object sender, ScrollEventArgs e) {
 				setEyeColor();
 			}
 		});
 		
 		this.maneRedSlider = new Slider();
 		this.maneRedSlider.setBounds(0, 50, 250, 30);
 		this.maneRedSlider.setFgColor(new Color(255,50,50,255));
 		this.maneRedSlider.setScrollMax(255);
 		this.maneRedSlider.setScrollValue(255);
 		this.maneRedSlider.setHorizontal(true);
 		this.maneRedSlider.addScrollListener(new IEventListener<ScrollEventArgs>() {
 			
 			@Override
 			public void onEvent(Object sender, ScrollEventArgs e) {
 				setManeColor();
 			}
 		});
 		this.maneGreenSlider = new Slider();
 		this.maneGreenSlider.setBounds(0, 100, 250, 30);
 		this.maneGreenSlider.setFgColor(new Color(50,255,50,255));
 		this.maneGreenSlider.setScrollMax(255);
 		this.maneGreenSlider.setScrollValue(255);
 		this.maneGreenSlider.setHorizontal(true);
 		this.maneGreenSlider.addScrollListener(new IEventListener<ScrollEventArgs>() {
 			@Override
 			public void onEvent(Object sender, ScrollEventArgs e) {
 				setManeColor();
 			}
 		});
 		this.maneBlueSlider = new Slider();
 		this.maneBlueSlider.setBounds(0, 150, 250, 30);
 		this.maneBlueSlider.setFgColor(new Color(50,50,255,255));
 		this.maneBlueSlider.setScrollMax(255);
 		this.maneBlueSlider.setScrollValue(255);
 		this.maneBlueSlider.setHorizontal(true);
 		this.maneBlueSlider.addScrollListener(new IEventListener<ScrollEventArgs>() {
 			
 			@Override
 			public void onEvent(Object sender, ScrollEventArgs e) {
 				setManeColor();
 			}
 		});
 		
 		this.bodyLabel = new Label();
 		this.bodyLabel.setFgColor(Color.White);
 		this.bodyLabel.setBounds(0, 0, 250, 30);
 		this.bodyLabel.setText("Body");
 		this.bodyLabel.setBgColor(Color.Transparent);
 		
 		this.eyeLabel = new Label();
 		this.eyeLabel.setFgColor(Color.White);
 		this.eyeLabel.setBounds(0, 0, 250, 30);
 		this.eyeLabel.setText("Eyes");
 		this.eyeLabel.setBgColor(Color.Transparent);
 		
 		this.maneLabel = new Label();
 		this.maneLabel.setFgColor(Color.White);
 		this.maneLabel.setBounds(0, 0, 250, 30);
 		this.maneLabel.setText("Mane");
 		this.maneLabel.setBgColor(Color.Transparent);
 		
 		this.nameLabel = new Label();
 		this.nameLabel.setFgColor(Color.White);
 		this.nameLabel.setBounds(0, 0, 250, 30);
 		this.nameLabel.setText("Name");
 		this.nameLabel.setBgColor(Color.Transparent);
 		
 		this.button = new Button();
 		this.button.setText("Done!");
 		this.button.setBounds(250,710,200,50);
 		super.addGuiControl(button, new Vector2(this.ScreenManager.getViewport().Width - 250, this.ScreenManager.getViewport().Height), 
 									 new Vector2(this.ScreenManager.getViewport().Width - 250,this.ScreenManager.getViewport().Height-200), 
 									 new Vector2(this.ScreenManager.getViewport().Width - 250, this.ScreenManager.getViewport().Height));
 
 		LookAndFeel feel = contentManager.load(this.lookAndFeelPath, LookAndFeel.class);
 		
 		feel.setDefaultFont(contentManager.loadFont("Monofonto24.xml"));
 		ShaderEffect dissabledEffect = contentManager.loadShaderEffect("GrayScale.effect");
 		context = new GUIRenderingContext(this.ScreenManager.getSpriteBatch(), feel, dissabledEffect);
 
 		this.button.addClicked(new IEventListener<EventArgs>() {
 
 			@Override
 			public void onEvent(Object sender, EventArgs e) {
 				savePlayerCharacteristics();
 				goBack();
 			}
 		});
 
 		this.button2 = new Button();
 		this.button2.setText("Back");
 		this.button2.setBounds(250,710,200,50);
 		super.addGuiControl(button2, new Vector2(this.ScreenManager.getViewport().Width - 250, this.ScreenManager.getViewport().Height), 
 									 new Vector2(this.ScreenManager.getViewport().Width - 250,this.ScreenManager.getViewport().Height-100), 
 									 new Vector2(this.ScreenManager.getViewport().Width - 250, this.ScreenManager.getViewport().Height));
 		feel.setDefaultFont(contentManager.loadFont("Monofonto24.xml"));
 		context = new GUIRenderingContext(this.ScreenManager.getSpriteBatch(), feel, dissabledEffect);
 
 		this.button2.addClicked(new IEventListener<EventArgs>() {
 
 			@Override
 			public void onEvent(Object sender, EventArgs e) {
 				goBack();
 			}
 		});
 		
 		this.button3 = new Button();
 		this.button3.setText("Randomize");
 		this.button3.setBounds(250,710,200,50);
 		super.addGuiControl(button3, new Vector2(this.ScreenManager.getViewport().Width - 250, this.ScreenManager.getViewport().Height), 
 									 new Vector2(this.ScreenManager.getViewport().Width - 250,this.ScreenManager.getViewport().Height-300), 
 									 new Vector2(this.ScreenManager.getViewport().Width - 250, this.ScreenManager.getViewport().Height));
 		feel.setDefaultFont(contentManager.loadFont("Monofonto24.xml"));
 		context = new GUIRenderingContext(this.ScreenManager.getSpriteBatch(), feel, dissabledEffect);
 
 		this.button3.addClicked(new IEventListener<EventArgs>() {
 
 			@Override
 			public void onEvent(Object sender, EventArgs e) {
 				randomizeAttributes();
 			}
 		});
 		
 		this.earthPonyButton = new ToggleButton();
 		this.earthPonyButton.setBounds(0, 0, 55, 55);
 		this.earthPonyButton.setImage(contentManager.loadTexture("Earthponybuttonimage.png"));
 		this.earthPonyButton.addClicked(new IEventListener<EventArgs>() {
 			@Override
 			public void onEvent(Object sender, EventArgs e) {
 				changeRaceToEarthpony();
 			}
 		});		
 		this.pegasusButton = new ToggleButton();
 		this.pegasusButton.setBounds(55, 0, 55, 55);
 		this.pegasusButton.setImage(contentManager.loadTexture("Pegasusbuttonimage.png"));
 		this.pegasusButton.addClicked(new IEventListener<EventArgs>() {
 			@Override
 			public void onEvent(Object sender, EventArgs e) {
 				changeRaceToPegasus();
 			}
 		});
 		this.unicornButton = new ToggleButton();
 		this.unicornButton.setBounds(110, 0, 55, 55);
 		this.unicornButton.setImage(contentManager.loadTexture("Unicornbuttonimage.png"));
 		this.unicornButton.addClicked(new IEventListener<EventArgs>() {
 			@Override
 			public void onEvent(Object sender, EventArgs e) {
 				changeRaceToUnicorn();
 			}
 		});
 		
 		this.walkButton = new ToggleButton();
 		this.walkButton.setBounds(0, 0, 110, 40);
 		this.walkButton.setText("Walk");
 		this.walkButton.addClicked(new IEventListener<EventArgs>() {
 			@Override
 			public void onEvent(Object sender, EventArgs e) {
 				toggleWalkAnimation();
 			}
 		});
 		super.addGuiControl(this.walkButton, new Vector2(0,this.ScreenManager.getViewport().Height), Vector2.add(ponyPosition, new Vector2(70,208)), 
 				new Vector2(0,this.ScreenManager.getViewport().Height));
 		
 		this.racePanel = new Panel();
 		this.racePanel.setBounds(0, 0, 165, 55);
 		this.racePanel.setBgColor(new Color(0,0,0,0.05f));
 		this.racePanel.addChild(nameLabel);
 		this.racePanel.addChild(this.earthPonyButton);
 		this.racePanel.addChild(this.pegasusButton);
 		this.racePanel.addChild(this.unicornButton);
 		super.addGuiControl(this.racePanel, new Vector2(0,this.ScreenManager.getViewport().Height), new Vector2(50,200), 
 		new Vector2(0,this.ScreenManager.getViewport().Height));
 		
 		this.namePanel = new Panel();
 		this.namePanel.setBounds(0, 0, 250, 100);
 		this.namePanel.setBgColor(new Color(0,0,0,0.05f));
 		this.namePanel.addChild(nameLabel);
 		this.namePanel.addChild(this.nameField);
 		super.addGuiControl(this.namePanel, new Vector2(0,this.ScreenManager.getViewport().Height), new Vector2(100,50), 
 		new Vector2(0,this.ScreenManager.getViewport().Height));
 		
 		this.bodyPanel = new Panel();
 		this.bodyPanel.setBounds(0, 0, 250, 500);
 		this.bodyPanel.setBgColor(new Color(0,0,0,0.05f));
 		this.bodyPanel.addChild(bodyLabel);
 		this.bodyPanel.addChild(this.bodyRedSlider);
 		this.bodyPanel.addChild(this.bodyGreenSlider);
 		this.bodyPanel.addChild(this.bodyBlueSlider);
 		this.bodyPanel.addChild(this.markComboBox);
 		super.addGuiControl(this.bodyPanel, new Vector2(0,this.ScreenManager.getViewport().Height), new Vector2(50,300), 
 		new Vector2(0,this.ScreenManager.getViewport().Height));
 		
 		this.eyePanel = new Panel();
 		this.eyePanel.setBounds(0, 0, 250, 500);
 		this.eyePanel.setBgColor(new Color(0,0,0,0.05f));
 		this.eyePanel.addChild(eyeLabel);
 		this.eyePanel.addChild(this.eyeComboBox);
 		this.eyePanel.addChild(this.eyeRedSlider);
 		this.eyePanel.addChild(this.eyeGreenSlider);
 		this.eyePanel.addChild(this.eyeBlueSlider);
 		super.addGuiControl(this.eyePanel, new Vector2(0,this.ScreenManager.getViewport().Height), new Vector2(350,300), 
 		new Vector2(0,this.ScreenManager.getViewport().Height));
 		
 		this.manePanel = new Panel();
 		this.manePanel.setBounds(0, 0, 250, 500);
 		this.manePanel.setBgColor(new Color(0,0,0,0.05f));
 		this.manePanel.addChild(maneLabel);
 		this.manePanel.addChild(this.maneComboBox);
 		this.manePanel.addChild(this.maneRedSlider);
 		this.manePanel.addChild(this.maneGreenSlider);
 		this.manePanel.addChild(this.maneBlueSlider);
 		super.addGuiControl(this.manePanel, new Vector2(0,this.ScreenManager.getViewport().Height), new Vector2(650,300), 
 		new Vector2(0,this.ScreenManager.getViewport().Height));
 		
 		setBodyColor();
 		setEyeColor();
 		setManeColor();
 		
 		changeRaceToEarthpony();
 		this.earthPonyButton.setToggled(true);
 	}
 
 	protected void toggleWalkAnimation() {
 		if(!this.walkButton.isToggled()){
 		this.ponyPlayer.startAnimation("walk");
 		}else{
 			this.ponyPlayer.startAnimation("idle");
 		}
 	}
 
 	protected void changeRaceToEarthpony() {
 		this.ponyPlayer.setBoneHidden(Bones.WINGS.getValue(), true);
 		this.pegasusButton.setToggled(false);
 		this.ponyPlayer.setBoneHidden(Bones.HORN.getValue(), true);
 		this.unicornButton.setToggled(false);
 	}
 	protected void changeRaceToPegasus() {
 		this.earthPonyButton.setToggled(false);
 		this.ponyPlayer.setBoneHidden(Bones.WINGS.getValue(), false);
 		this.ponyPlayer.setBoneHidden(Bones.HORN.getValue(), true);
 		this.unicornButton.setToggled(false);
 	}	
 	protected void changeRaceToUnicorn() {
 		this.earthPonyButton.setToggled(false);
 		this.ponyPlayer.setBoneHidden(Bones.WINGS.getValue(), true);
 		this.pegasusButton.setToggled(false);
 		this.ponyPlayer.setBoneHidden(Bones.HORN.getValue(), false);
 	}
 
 	protected void randomizeAttributes() {
 		Random random = new Random();
 		this.bodyRedSlider.setScrollValue(random.nextInt(this.bodyRedSlider.getScrollMax()));
 		this.bodyGreenSlider.setScrollValue(random.nextInt(this.bodyGreenSlider.getScrollMax()));
 		this.bodyBlueSlider.setScrollValue(random.nextInt(this.bodyBlueSlider.getScrollMax()));
 		
 		this.eyeRedSlider.setScrollValue(random.nextInt(this.eyeRedSlider.getScrollMax()));
 		this.eyeGreenSlider.setScrollValue(random.nextInt(this.eyeGreenSlider.getScrollMax()));
 		this.eyeBlueSlider.setScrollValue(random.nextInt(this.eyeBlueSlider.getScrollMax()));
 		
 		this.maneRedSlider.setScrollValue(random.nextInt(this.maneRedSlider.getScrollMax()));
 		this.maneGreenSlider.setScrollValue(random.nextInt(this.maneGreenSlider.getScrollMax()));
 		this.maneBlueSlider.setScrollValue(random.nextInt(this.maneBlueSlider.getScrollMax()));
 		
 		this.maneComboBox.setSelectedIndex(random.nextInt(this.maneComboBox.itemCount()));
 		this.eyeComboBox.setSelectedIndex(random.nextInt(this.eyeComboBox.itemCount()));
 		this.markComboBox.setSelectedIndex(random.nextInt(this.markComboBox.itemCount()));
 		
 		int rand = random.nextInt(3);
 		if (rand <= 0){
 			changeRaceToEarthpony();
 			this.earthPonyButton.setToggled(true);
 		} else if (rand <= 1){
 			changeRaceToPegasus();
 			this.pegasusButton.setToggled(true);
 		} else {
 			changeRaceToUnicorn();
 			this.unicornButton.setToggled(true);
 		}
 	}
 
 	protected void savePlayerCharacteristics() {
 		this.character.archetypePath = PonyArchetypePath;
 		
 		this.character.bodyColor = getBodyColor();
 		this.character.eyeColor = getEyeColor();
 		this.character.maneColor = getManeColor();
 		
 		this.character.maneStyle = this.maneComboBox.getSelectedItem().maneStyle;
 		this.character.eyeTexture = this.eyeComboBox.getSelectedItem().eyePath;
 		
 		this.character.name = this.nameField.getText();
 		
 		this.character.markTexture = this.markComboBox.getSelectedItem().markPath;
 		
 		if(this.earthPonyButton.isToggled()){
 			this.character.race = Race.EARTHPONY.getValue();
 		}else if(this.pegasusButton.isToggled()){
 			this.character.race = Race.PEGASUS.getValue();
 		}else if(this.unicornButton.isToggled()){
 			this.character.race = Race.UNICORN.getValue();
 		}
 		
 		this.character.special = new SpecialStats(5, 3, 6, 7, 5, 7, 8);
 		
 		this.charWriter.savePlayerCharacteristics(this.character);
 	}
 
 	protected void setEyeStyle() {
 		this.ponyPlayer.setBoneTexture(Bones.EYE.getValue(), this.eyeComboBox.getSelectedItem().eyeEntry);
 	}
 	protected void setManeStyle() {
 		this.ponyPlayer.setBoneTexture(Bones.UPPERMANE.getValue(), this.maneComboBox.getSelectedItem().upperManeEntry);
 		this.ponyPlayer.setBoneTexture(Bones.LOWERMANE.getValue(), this.maneComboBox.getSelectedItem().lowerManeEntry);
 		this.ponyPlayer.setBoneTexture(Bones.UPPERTAIL.getValue(), this.maneComboBox.getSelectedItem().upperTailEntry);
 		this.ponyPlayer.setBoneTexture(Bones.LOWERTAIL.getValue(), this.maneComboBox.getSelectedItem().lowerTailEntry);
 	}
 	protected void setMarkStyle() {
 		this.ponyPlayer.setBoneTexture(Bones.MARK.getValue(), this.markComboBox.getSelectedItem().markEntry);
 	}
 
 	public void goBack() {
 		this.exitScreen();
 	}
 	
 	protected void setBodyColor() {
 		Color color = new Color(this.bodyRedSlider.getScrollValue(),this.bodyGreenSlider.getScrollValue(),this.bodyBlueSlider.getScrollValue(),255);
 		setBodyColor(color);
 	}
 	protected void setBodyColor(Color color) {
 		PonyColorChangeHelper.setBodyColor(color, ponyPlayer);
 	}
 	protected void setEyeColor() {
 		Color color = new Color(this.eyeRedSlider.getScrollValue(),this.eyeGreenSlider.getScrollValue(),this.eyeBlueSlider.getScrollValue(),255);
 		setEyeColor(color);
 	}
 	protected void setEyeColor(Color color) {
 		PonyColorChangeHelper.setEyeColor(color, ponyPlayer);
 	}
 	protected void setManeColor() {
 		Color color = new Color(this.maneRedSlider.getScrollValue(),this.maneGreenSlider.getScrollValue(),this.maneBlueSlider.getScrollValue(),255);
 		setManeColor(color);
 	}
 	protected void setManeColor(Color color) {
 		PonyColorChangeHelper.setManeColor(color, ponyPlayer);
 	}
 	
 	
 	protected Color getBodyColor() {
 		Color color = new Color(this.bodyRedSlider.getScrollValue(),this.bodyGreenSlider.getScrollValue(),this.bodyBlueSlider.getScrollValue(),255);
 		return color;
 	}
 	protected Color getEyeColor() {
 		Color color = new Color(this.eyeRedSlider.getScrollValue(),this.eyeGreenSlider.getScrollValue(),this.eyeBlueSlider.getScrollValue(),255);
 		return color;
 	}
 	protected Color getManeColor() {
 		Color color = new Color(this.maneRedSlider.getScrollValue(),this.maneGreenSlider.getScrollValue(),this.maneBlueSlider.getScrollValue(),255);
 		return color;
 	}
 
 	protected void showPauseScreen() {
 		this.ScreenManager.addScreen("PauseScreen");
 	}
 
 
 	@Override
 	public void update(GameTime time, boolean otherScreeenHasFocus,
 			boolean coveredByOtherScreen) {
 		if(!otherScreeenHasFocus) {
 			super.update(time, otherScreeenHasFocus, coveredByOtherScreen);
 		}
 	}
 
 	private void addPony() {	
 		this.ponyPlayer = this.contentManager.load("rdset.animset", AnimationPlayer.class);;
 		this.ponyPlayer.startAnimation("idle");
 	}
 	
 	@Override
 	public void render(GameTime time, SpriteBatch batch) {
 		super.render(time, batch);
 	}
 	
 	private class ManeEntries{
 		String name;
 		TextureEntry upperManeEntry;
 		TextureEntry lowerManeEntry;
 		TextureEntry upperTailEntry;
 		TextureEntry lowerTailEntry;
 		ManeStyle maneStyle;
 		public ManeEntries(String name, ManeStyle maneStyle, TextureDictionary assetDict) {
 			this.name = name;
 			this.upperManeEntry = assetDict.extractTextureEntry(maneStyle.upperManeStyle);
 			this.lowerManeEntry = assetDict.extractTextureEntry(maneStyle.lowerManeStyle);
 			this.upperTailEntry = assetDict.extractTextureEntry(maneStyle.upperTailStyle);
 			this.lowerTailEntry = assetDict.extractTextureEntry(maneStyle.lowerTailStyle);
 			this.maneStyle = maneStyle;
 		}
 	}
 	
 	private class EyeEntries{
 		String name;
 		TextureEntry eyeEntry;
 		String eyePath;
 		public EyeEntries(String name, String eyePath, TextureDictionary assetDict) {
 			this.name = name;
 			this.eyeEntry = assetDict.extractTextureEntry(eyePath);
 			this.eyePath = eyePath;
 		}
 	}
 	
 	private class MarkEntries{
 		String name;
 		TextureEntry markEntry;
 		String markPath;
 		public MarkEntries(String name, String markPath, TextureDictionary assetDict) {
 			this.name = name;
 			this.markEntry = assetDict.extractTextureEntry(markPath);
 			this.markPath = markPath;
 		}
 	}
 }
