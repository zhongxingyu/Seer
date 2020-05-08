 package gui.menu;
 
 import enums.Enums.RaceType;
 import gui.util.AlertMessagePopup;
 import gui.util.managers.ResourceManager;
 import kryonet.DnDNetwork.CreateCharacter;
 import kryonet.client.DnDClient;
 import mdes.slick.sui.Button;
 import mdes.slick.sui.Component;
 import mdes.slick.sui.Container;
 import mdes.slick.sui.Display;
 import mdes.slick.sui.Label;
 import mdes.slick.sui.ScrollPane;
 import mdes.slick.sui.Sui;
 import mdes.slick.sui.TextArea;
 import mdes.slick.sui.TextField;
 import mdes.slick.sui.ToggleButton;
 import mdes.slick.sui.ToggleButtonGroup;
 import mdes.slick.sui.event.ActionEvent;
 import mdes.slick.sui.event.ActionListener;
 import mdes.slick.sui.event.KeyEvent;
 import mdes.slick.sui.event.KeyListener;
 import mdes.slick.sui.event.MouseAdapter;
 import mdes.slick.sui.event.MouseEvent;
 import objects.dndcharacter.Abilities;
 import objects.dndcharacter.Abilities.AbilityType;
 import objects.dndcharacter.classes.DnDClass;
 import objects.dndcharacter.classes.DnDClass.ClassType;
 import objects.dndcharacter.races.Race;
 
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 import org.newdawn.slick.state.transition.FadeInTransition;
 import org.newdawn.slick.state.transition.FadeOutTransition;
 
 public class CreateCharacterState extends BasicGameState {
 
 	int stateID;
 	Display display;
 	GameContainer gc;
 	StateBasedGame sb;
 	
 	Container characterContainer;
 	TextArea classTitle, raceTitle;
 	TextArea classDescription, raceDescription;
 	ScrollPane classPane, racePane;
 	
 	ToggleButton dragonbornButton, halflingButton, dwarfButton, elfButton;
 	ToggleButton eladrinButton, humanButton, tieflingButton, halfElfButton;
 	
 	ToggleButton maleButton, femaleButton;
 	
 	ToggleButton clericButton, fighterButton, paladinButton, rangerButton;
 	ToggleButton rogueButton, warlockButton, warlordButton, wizardButton;
 	
 	int availableAbilities = 16;
 	ToggleButton available, strength, constitution, dexterity, intelligence, wisdom, charisma;
 	
 	Label characterClass;
 
 	ToggleButtonGroup raceGroup;
 	ToggleButtonGroup classGroup;	
 	ToggleButtonGroup genderGroup;
 	
 	TextField characterName;
 	Button acceptButton;
 
 	private AlertMessagePopup popup;
 	private boolean render = false;
 	private ToggleButton currentClass;
 	private ToggleButton currentRace;
 	
 	private Abilities abilities;
 	private MouseAdapter abilitiesListener;
 	
 	String genderForImages = "MALE";
 	
 	public CreateCharacterState(int stateID) {
 		this.stateID = stateID;
 	}
 	
 	@Override
 	public void enter(GameContainer gc, StateBasedGame sb) throws SlickException {
 		super.enter(gc, sb);
 		init(gc, sb);
 	}
 	
 	@Override
 	public void init(GameContainer gc, StateBasedGame sb) throws SlickException {
 		this.gc = gc;
 		this.sb = sb;
 		
 		gc.getGraphics().setBackground(Sui.getTheme().getBackground());
 		
 		display = new Display(gc);
 		
 		raceGroup = new ToggleButtonGroup();
 		classGroup = new ToggleButtonGroup();		
 		genderGroup = new ToggleButtonGroup();
 		
 		abilities = new Abilities();
 		
 		abilitiesListener = new MouseAdapter() {			
 			@Override
 			public void mousePressed(MouseEvent e) {
 				if(e.getButton() == MouseEvent.BUTTON1) {
 					availableAbilities--;
 					if(((ToggleButton) e.getSource()).getName() == AbilityType.STRENGTH.toString())
 						abilities.setStrength(abilities.getStrength()+1);
 					if(((ToggleButton) e.getSource()).getName() == AbilityType.CONSTITUTION.toString())
 						abilities.setConstitution(abilities.getConstitution()+1);
 					if(((ToggleButton) e.getSource()).getName() == AbilityType.DEXTERITY.toString())
 						abilities.setDexterity(abilities.getDexterity()+1);
 					if(((ToggleButton) e.getSource()).getName() == AbilityType.INTELLIGENCE.toString())
 						abilities.setIntelligence(abilities.getIntelligence()+1);
 					if(((ToggleButton) e.getSource()).getName() == AbilityType.WISDOM.toString())
 						abilities.setWisdom(abilities.getWisdom()+1);
 					if(((ToggleButton) e.getSource()).getName() == AbilityType.CHARISMA.toString())
 						abilities.setCharisma(abilities.getCharisma()+1);
 				} else
 				if(e.getButton() == MouseEvent.BUTTON2) {
 					availableAbilities++;
 					if(((ToggleButton) e.getSource()).getName() == AbilityType.STRENGTH.toString())
 						abilities.setStrength(abilities.getStrength()-1);
 					if(((ToggleButton) e.getSource()).getName() == AbilityType.CONSTITUTION.toString())
 						abilities.setConstitution(abilities.getConstitution()-1);
 					if(((ToggleButton) e.getSource()).getName() == AbilityType.DEXTERITY.toString())
 						abilities.setDexterity(abilities.getDexterity()-1);
 					if(((ToggleButton) e.getSource()).getName() == AbilityType.INTELLIGENCE.toString())
 						abilities.setIntelligence(abilities.getIntelligence()-1);
 					if(((ToggleButton) e.getSource()).getName() == AbilityType.WISDOM.toString())
 						abilities.setWisdom(abilities.getWisdom()-1);
 					if(((ToggleButton) e.getSource()).getName() == AbilityType.CHARISMA.toString())
 						abilities.setCharisma(abilities.getCharisma()-1);
 				}
 			}
 		};
 		
 		createCharacterContainer();
 		createRaceContainer();
 		createClassContainer();
 		createOptions();		
 	}
 	
 	private void createOptions() {
 		Label label = new Label("Name");
 		label.setSize(100, 30);
 		label.setLocationRelativeTo(display);
 		label.setY(DnDClient.SCREENHEIGHT - 150);
 		label.setForeground(Color.white);
 		label.setFont(LoadingState.labelFont);
 		display.add(label);
 		
 		characterName = new TextField(10);
 		characterName.setMaxChars(10);
 		characterName.setForeground(Color.white);
 		characterName.setLocationRelativeTo(display);
 		characterName.setY(label.getAbsoluteY() + 35);
 		display.add(characterName);	
 		
 		KeyListener characterListener = new KeyListener() {			
 			@Override
 			public void keyReleased(KeyEvent e) { }
 			
 			@Override
 			public void keyPressed(KeyEvent e) {
 				if(e.getKeyCode() == Input.KEY_TAB) {
 					acceptButton.grabFocus();
 				}
 				
 				if(e.getKeyCode() == Input.KEY_ENTER) {
 					acceptButton.press();
 				}
 			}
 		};	
 		characterName.addKeyListener(characterListener);
 		
 		acceptButton = new Button("Accept");
 		acceptButton.setSize(100, 30);
 		acceptButton.setLocationRelativeTo(display);
 		acceptButton.setY(characterName.getAbsoluteY() + 30);
 		acceptButton.setImage(ResourceManager.getInstance().getImage("MENU_BUTTON"));
 		acceptButton.setFont(LoadingState.font);
 		acceptButton.packImage();
 		acceptButton.setForeground(Color.white);
 		acceptButton.setFocusColor(Color.yellow);
 		display.add(acceptButton);
 		
         ActionListener textAction = new ActionListener() {
             public void actionPerformed(ActionEvent ev) {
             	DnDClient.checkNewCharacter(LoginState.usernameText, characterName.getText(), classGroup.getSelectedButton().getName(), raceGroup.getSelectedButton().getName());
             } 
         };
         
         KeyListener acceptKeyListener = new KeyListener() {			
 			@Override
 			public void keyReleased(KeyEvent e) { }
 			
 			@Override
 			public void keyPressed(KeyEvent e) {
 				if(e.getKeyCode() == Input.KEY_ENTER) {
 					acceptButton.press();
 				}
 				if(e.getKeyCode() == Input.KEY_TAB) 
 					characterName.grabFocus();
 			}
 		};
         acceptButton.addKeyListener(acceptKeyListener);
         acceptButton.addActionListener(textAction);
         
         popup = new AlertMessagePopup(display);
         
         Button backButton = new Button("Back");
         backButton.setSize(100, 30);
         backButton.setLocationRelativeTo(display);
         backButton.setY(acceptButton.getAbsoluteY() + 40);
         backButton.setImage(ResourceManager.getInstance().getImage("MENU_BUTTON"));
         backButton.setFont(LoadingState.font);
         backButton.packImage();
         backButton.setForeground(Color.white);
         backButton.setFocusColor(Color.yellow);
 		display.add(backButton);
         
         ActionListener backButtonAction = new ActionListener() {
             public void actionPerformed(ActionEvent ev) {
 				sb.enterState(DnDClient.MAINMENUSTATE, new FadeOutTransition(), new FadeInTransition());
             } 
         };        
         backButton.addActionListener(backButtonAction);
 	}
 	
 	private void createClassContainer() {
 		
 		classTitle = new TextArea();
 		classTitle.setSize(275, 50);
 		classTitle.setLocation(DnDClient.SCREENWIDTH - classTitle.getWidth() - 25, racePane.getAbsoluteY() + racePane.getHeight() + 50);
 		classTitle.setBackground(new Color(0, 0, 0, 0.6f));
 		classTitle.setForeground(Color.white);
 		classTitle.setOpaque(true);
 		classTitle.setEditable(false);
 		classTitle.setAutoResize(false);
 		display.add(classTitle);		
 		
 		classDescription = new TextArea();
 		classDescription.setSize(275, characterContainer.getHeight() / 2 - 70);
 		classDescription.setMinimumSize(classDescription.getSize());
 		classDescription.setBackground(new Color(0, 0, 0, 0.6f));
 		classDescription.setForeground(Color.white);
 		classDescription.setOpaque(true);
 		classDescription.setEditable(false);
 		
 		classPane = new ScrollPane(classDescription);
 		classPane.setLocation(DnDClient.SCREENWIDTH - classDescription.getWidth() - 25, classTitle.getAbsoluteY() + classTitle.getHeight());
 		classPane.setBackground(new Color(0, 0, 0, 0f));
 		classPane.setOpaque(true);
 		classPane.setSize(classDescription.getWidth(), classDescription.getHeight());
 		display.add(classPane);
 		
 //		ChangeListener textChanged = new ChangeListener() {			
 //			@Override
 //			public void stateChanged(ChangeEvent e) {
 //				classPane.scrollToTop();
 //			}
 //		};		
 //		classDescription.addChangeListener(textChanged);
 	}
 	
 	private void createRaceContainer() {		
 		raceTitle = new TextArea();
 		raceTitle.setSize(275, 50);
 		raceTitle.setLocation(DnDClient.SCREENWIDTH - raceTitle.getWidth() - 25, characterContainer.getAbsoluteY());
 		raceTitle.setBackground(new Color(0, 0, 0, 0.6f));
 		raceTitle.setForeground(Color.white);
 		raceTitle.setOpaque(true);
 		raceTitle.setEditable(false);
 		raceTitle.setAutoResize(false);
 		display.add(raceTitle);
 		
 		raceDescription = new TextArea();
 		raceDescription.setSize(275, characterContainer.getHeight() / 2 - 70);
 		raceDescription.setMinimumSize(raceDescription.getSize());
 		raceDescription.setBackground(new Color(0, 0, 0, 0.6f));
 		raceDescription.setForeground(Color.white);
 		raceDescription.setOpaque(true);
 		raceDescription.setEditable(false);
 		
 		racePane = new ScrollPane(raceDescription);
 		racePane.setLocation(DnDClient.SCREENWIDTH - raceDescription.getWidth() - 25, raceTitle.getAbsoluteY() + raceTitle.getHeight());
 		racePane.setBackground(new Color(0, 0, 0, 0f));
 		racePane.setOpaque(true);
 		racePane.setSize(raceDescription.getWidth(), raceDescription.getHeight());
 		display.add(racePane);
 				
 //		ChangeListener textChanged = new ChangeListener() {			
 //			@Override
 //			public void stateChanged(ChangeEvent e) {
 //				racePane.scrollToTop();
 //			}
 //		};
 //		raceDescription.addChangeListener(textChanged);
 	}
 	
 	private void createEmptySquares() {				
 		createEmptySquaresForGroups(raceGroup);	
 		createEmptySquaresForGroups(classGroup);	
 		createEmptySquaresForGroups(genderGroup);
 	}
 	
 	private void createEmptySquaresForGroups(ToggleButtonGroup group) {
 		for(ToggleButton button : group.getGroup()) {
 			createEmptySquare(button);
 		}
 	}
 	
 	private void createEmptySquare(Component object) {
 		ResourceManager.getInstance().getImage("EMPTY_SQUARE").getScaledCopy((int)object.getWidth(), (int)object.getHeight()).draw(object.getAbsoluteX(), object.getAbsoluteY());		
 	}
 
 	private void createCharacterContainer() {
 		characterContainer = new Container();
 		characterContainer.setBounds(25, 25, 250, DnDClient.SCREENHEIGHT - 50);
 		characterContainer.setBackground(new Color(0, 0, 0, 0.6f));
 		characterContainer.setOpaque(true);
 		
 		int xStart = 40;
 		int yStart = 30;
 		int yOffset = 60;
 		
 		dragonbornButton = new ToggleButton();
 		dragonbornButton.setName("DRAGONBORN");
 		dragonbornButton.setBounds(xStart, yStart, 60, 60);
 		dragonbornButton.setSelected(true);
 		dragonbornButton.setForeground(new Color(0, 0, 0, 0.0f));
 		dragonbornButton.setGroup(raceGroup);
 		characterContainer.add(dragonbornButton);
 
 		halflingButton = new ToggleButton();
 		halflingButton.setBounds(xStart, yStart + yOffset, 60, 60);
 		halflingButton.setGroup(raceGroup);
 		characterContainer.add(halflingButton);
 		
 		dwarfButton = new ToggleButton();
 		dwarfButton.setBounds(xStart, yStart + 2 * yOffset, 60, 60);
 		dwarfButton.setGroup(raceGroup);
 		characterContainer.add(dwarfButton);
 		
 		elfButton = new ToggleButton();
 		elfButton.setBounds(xStart, yStart + 3 * yOffset, 60, 60);
 		elfButton.setGroup(raceGroup);
 		characterContainer.add(elfButton);
 		
 		eladrinButton = new ToggleButton();
 		eladrinButton.setBounds(xStart + 60 + xStart, yStart, 60, 60);
 		eladrinButton.setGroup(raceGroup);
 		characterContainer.add(eladrinButton);
 
 		humanButton = new ToggleButton();
 		humanButton.setName("HUMAN");
 		humanButton.setForeground(new Color(0, 0, 0, 0.0f));
		humanButton.setText(humanDescripton);
 		humanButton.setBounds(xStart + 60 + xStart, yStart + yOffset, 60, 60);
 		humanButton.setGroup(raceGroup);
 		characterContainer.add(humanButton);		
 		
 		tieflingButton = new ToggleButton();
 		tieflingButton.setBounds(xStart + 60 + xStart, yStart + 2 * yOffset, 60, 60);
 		tieflingButton.setGroup(raceGroup);
 		characterContainer.add(tieflingButton);
 		
 		halfElfButton = new ToggleButton();
 		halfElfButton.setBounds(xStart + 60 + xStart, yStart + 3 * yOffset, 60, 60);
 		halfElfButton.setGroup(raceGroup);
 		characterContainer.add(halfElfButton);
 		
 		maleButton = new ToggleButton();
 		maleButton.setSelected(true);
 		maleButton.setForeground(new Color(0, 0, 0, 0.0f));
 		maleButton.setText("MALE");
 		maleButton.setRolloverImage(ResourceManager.getInstance().getImage("MALE_SYMBOL_SELECTED"));
 		maleButton.setSelectedImage(ResourceManager.getInstance().getImage("MALE_SYMBOL_SELECTED"));
 		maleButton.setBounds(65, yStart + 4 * yOffset + 20, 50, 50);
 		maleButton.setImage(ResourceManager.getInstance().getImage("MALE_SYMBOL"));
 		maleButton.setGroup(genderGroup);
 		characterContainer.add(maleButton);
 		
 		femaleButton = new ToggleButton();
 		femaleButton.setForeground(new Color(0, 0, 0, 0.0f));
 		femaleButton.setText("FEMALE");
 		femaleButton.setRolloverImage(ResourceManager.getInstance().getImage("FEMALE_SYMBOL_SELECTED"));
 		femaleButton.setSelectedImage(ResourceManager.getInstance().getImage("FEMALE_SYMBOL_SELECTED"));
 		femaleButton.setBounds(125, yStart + 4 * yOffset + 20, 50, 50);
 		femaleButton.setImage(ResourceManager.getInstance().getImage("FEMALE_SYMBOL"));
 		femaleButton.setGroup(genderGroup);
 		characterContainer.add(femaleButton);
 		
 		genderForImages = genderGroup.getSelectedButton().getText();
 		setRaceImages();
 		
 		xStart = 5;
 		yStart = yStart + 5 * yOffset + 30;
 				
 		clericButton = new ToggleButton();
 		clericButton.setBounds(xStart, yStart, 60, 60);
 		clericButton.setForeground(new Color(0, 0, 0, 0.0f));
 		clericButton.setImage(ResourceManager.getInstance().getImage("CLERIC"));
 		clericButton.setSelected(true);
 		clericButton.setName("CLERIC");
 		clericButton.setGroup(classGroup);
 		characterContainer.add(clericButton);
 
 		fighterButton = new ToggleButton();
 		fighterButton.setBounds(xStart + 60, yStart, 60, 60);
 		fighterButton.setForeground(new Color(0, 0, 0, 0.0f));
 		fighterButton.setImage(ResourceManager.getInstance().getImage("FIGHTER"));
 		fighterButton.setGroup(classGroup);
 		fighterButton.setName("FIGHTER");
 		characterContainer.add(fighterButton);
 		
 		paladinButton = new ToggleButton();
 		paladinButton.setBounds(xStart + 2 * 60, yStart, 60, 60);
 		paladinButton.setImage(ResourceManager.getInstance().getImage("EMPTY_BAG_SQUARE"));
 		paladinButton.setGroup(classGroup);
 		characterContainer.add(paladinButton);
 		
 		rangerButton = new ToggleButton();
 		rangerButton.setBounds(xStart + 3 * 60, yStart, 60, 60);
 		rangerButton.setImage(ResourceManager.getInstance().getImage("EMPTY_BAG_SQUARE"));
 		rangerButton.setGroup(classGroup);
 		characterContainer.add(rangerButton);
 		
 		rogueButton = new ToggleButton();
 		rogueButton.setBounds(xStart, yStart + 60, 60, 60);
 		rogueButton.setImage(ResourceManager.getInstance().getImage("EMPTY_BAG_SQUARE"));
 		rogueButton.setGroup(classGroup);
 		characterContainer.add(rogueButton);
 
 		warlockButton = new ToggleButton();
 		warlockButton.setForeground(new Color(0, 0, 0, 0.0f));
 		warlockButton.setBounds(xStart + 60, yStart + 60, 60, 60);
 		warlockButton.setImage(ResourceManager.getInstance().getImage("EMPTY_BAG_SQUARE"));
 		warlockButton.setGroup(classGroup);
 		characterContainer.add(warlockButton);		
 		
 		warlordButton = new ToggleButton();
 		warlordButton.setBounds(xStart + 2 * 60, yStart + 60, 60, 60);
 		warlordButton.setImage(ResourceManager.getInstance().getImage("EMPTY_BAG_SQUARE"));
 		warlordButton.setGroup(classGroup);
 		characterContainer.add(warlordButton);
 		
 		wizardButton = new ToggleButton();
 		wizardButton.setBounds(xStart + 3 * 60, yStart + 60, 60, 60);
 		wizardButton.setImage(ResourceManager.getInstance().getImage("EMPTY_BAG_SQUARE"));
 		wizardButton.setGroup(classGroup);
 		characterContainer.add(wizardButton);
 		
 		yStart += 70;
 		
 		available = new ToggleButton();
 		available.setBounds(xStart + 90, yStart + 70, 60, 60);
 		available.setName(AbilityType.STRENGTH.toString());
 		available.setDisabledImage(ResourceManager.getInstance().getImage("BLANK_SQUARE"));
 		available.setHorizontalAlignment(Label.CENTER_ALIGNMENT);
 		available.setVerticalAlignment(Label.CENTER_ALIGNMENT);
 		available.setDisabledForeground(Color.yellow);
 		available.setEnabled(false);
 		available.packImage();
 		characterContainer.add(available);
 		
 		strength = new ToggleButton();
 		strength.setBounds(xStart, yStart + 150, 60, 60);
 		strength.setName(AbilityType.STRENGTH.toString());
 		strength.setImage(ResourceManager.getInstance().getImage("BLANK_SQUARE"));
 		strength.setHorizontalAlignment(Label.CENTER_ALIGNMENT);
 		strength.setVerticalAlignment(Label.CENTER_ALIGNMENT);
 		strength.setForeground(Color.yellow);
 		strength.packImage();
 		characterContainer.add(strength);
 		strength.addMouseListener(abilitiesListener);
 		
 		constitution = new ToggleButton();
 		constitution.setBounds(xStart + 90, yStart + 150, 60, 60);
 		constitution.setName(AbilityType.CONSTITUTION.toString());
 		constitution.setImage(ResourceManager.getInstance().getImage("BLANK_SQUARE"));
 		constitution.setHorizontalAlignment(Label.CENTER_ALIGNMENT);
 		constitution.setVerticalAlignment(Label.CENTER_ALIGNMENT);
 		constitution.setForeground(Color.yellow);
 		constitution.packImage();
 		characterContainer.add(constitution);
 		constitution.addMouseListener(abilitiesListener);
 		
 		dexterity = new ToggleButton();
 		dexterity.setBounds(xStart + 180, yStart + 150, 60, 60);
 		dexterity.setName(AbilityType.DEXTERITY.toString());
 		dexterity.setImage(ResourceManager.getInstance().getImage("BLANK_SQUARE"));
 		dexterity.setHorizontalAlignment(Label.CENTER_ALIGNMENT);
 		dexterity.setVerticalAlignment(Label.CENTER_ALIGNMENT);
 		dexterity.setForeground(Color.yellow);
 		dexterity.packImage();
 		characterContainer.add(dexterity);
 		dexterity.addMouseListener(abilitiesListener);
 		
 		intelligence = new ToggleButton();
 		intelligence.setBounds(xStart, yStart + 220, 60, 60);
 		intelligence.setName(AbilityType.INTELLIGENCE.toString());
 		intelligence.setImage(ResourceManager.getInstance().getImage("BLANK_SQUARE"));
 		intelligence.setHorizontalAlignment(Label.CENTER_ALIGNMENT);
 		intelligence.setVerticalAlignment(Label.CENTER_ALIGNMENT);
 		intelligence.setForeground(Color.yellow);
 		intelligence.packImage();
 		characterContainer.add(intelligence);
 		intelligence.addMouseListener(abilitiesListener);
 		
 		wisdom = new ToggleButton();
 		wisdom.setBounds(xStart + 90, yStart + 220, 60, 60);
 		wisdom.setName(AbilityType.WISDOM.toString());
 		wisdom.setImage(ResourceManager.getInstance().getImage("BLANK_SQUARE"));
 		wisdom.setHorizontalAlignment(Label.CENTER_ALIGNMENT);
 		wisdom.setVerticalAlignment(Label.CENTER_ALIGNMENT);
 		wisdom.setForeground(Color.yellow);
 		wisdom.packImage();
 		characterContainer.add(wisdom);
 		wisdom.addMouseListener(abilitiesListener);
 		
 		charisma = new ToggleButton();
 		charisma.setBounds(xStart + 180, yStart + 220, 60, 60);
 		charisma.setName(AbilityType.CHARISMA.toString());
 		charisma.setImage(ResourceManager.getInstance().getImage("BLANK_SQUARE"));
 		charisma.setHorizontalAlignment(Label.CENTER_ALIGNMENT);
 		charisma.setVerticalAlignment(Label.CENTER_ALIGNMENT);
 		charisma.setForeground(Color.yellow);
 		charisma.packImage();
 		characterContainer.add(charisma);
 		charisma.addMouseListener(abilitiesListener);
 		
 		display.add(characterContainer);
 	}
 
 	@Override
 	public void render(GameContainer gc, StateBasedGame sb, Graphics g) throws SlickException {
 		g.setAntiAlias(true);
 		packImages();
 		
 		ResourceManager.getInstance().getImage("CREATE_CHARACTER_BACKGROUND").getScaledCopy(DnDClient.SCREENWIDTH, DnDClient.SCREENHEIGHT).draw(0, 0);		
 
 		if(render) {
 			display.render(gc, g);
 			createEmptySquares();		
 			raceGroup.getSelectedButton().getImage().draw(raceTitle.getAbsoluteX() - 15, raceTitle.getAbsoluteY() - 15);
 			ResourceManager.getInstance().getImage("EMPTY_SQUARE").getScaledCopy(60, 60).draw(raceTitle.getAbsoluteX() - 15, raceTitle.getAbsoluteY() - 15);
 			classGroup.getSelectedButton().getImage().draw(classTitle.getAbsoluteX() - 15, classTitle.getAbsoluteY() - 15);
 			ResourceManager.getInstance().getImage("EMPTY_SQUARE").getScaledCopy(60, 60).draw(classTitle.getAbsoluteX() - 15, classTitle.getAbsoluteY() - 15);
 		}
 	}
 
 	@Override
 	public void update(GameContainer gc, StateBasedGame sb, int delta) throws SlickException {	
 		genderForImages = genderGroup.getSelectedButton().getText();
 		setRaceImages();
 		
 		if(currentRace != raceGroup.getSelectedButton()) {
 			raceTitle.setText("\n            " + raceGroup.getSelectedButton().getName());
 			setRaceDescription();
 			currentRace = raceGroup.getSelectedButton();
 		}
 		
 		if(currentClass != classGroup.getSelectedButton()) {
 			classTitle.setText("\n            " + classGroup.getSelectedButton().getName());
 			setClassDescription();
 			currentClass = classGroup.getSelectedButton();
 		}
 
 		available.setText("PTS:\n  " + availableAbilities);
 		
 		strength.setText("STR:\n  " + String.valueOf(abilities.getStrength()));
 		constitution.setText("CON:\n  " + String.valueOf(abilities.getConstitution()));
 		dexterity.setText("DEX:\n  " + String.valueOf(abilities.getDexterity()));
 
 		intelligence.setText("INT:\n  " + String.valueOf(abilities.getIntelligence()));
 		wisdom.setText("WIS:\n  " + String.valueOf(abilities.getWisdom()));
 		charisma.setText("CHA:\n  " + String.valueOf(abilities.getCharisma()));
 		
 		display.update(gc, delta);
 		render = true;
 	}
 	
 	private void setRaceDescription() {
 		Race selectedRace = Race.getRaceFromMap(RaceType.valueOf(raceGroup.getSelectedButton().getName()));
 		raceDescription.setText("");
 		raceDescription.addLine(selectedRace.getDescription() + "\n\n");
 		raceDescription.addLine("  - Size : " + selectedRace.getSize().toString() + "\n");
 		raceDescription.addLine("  - Speed : " + selectedRace.getSpeed() + " squares\n");
 		raceDescription.addLine("  - Vision : " + selectedRace.getVision().toString() + "\n\n");
 		if(selectedRace.getRacialTraits().getAbilities().size() > 0)
 			raceDescription.addLine("  - Ability Scores : " + editMapString(selectedRace.getRacialTraits().getAbilities().toString()) + "\n\n");
 		if(selectedRace.getRacialTraits().getSkills().size() > 0)
 			raceDescription.addLine("  - Skill Bonuses  : " + editMapString(selectedRace.getRacialTraits().getSkills().toString()));
 		if(selectedRace.getRacialTraits().getDefenses().size() > 0)
 			raceDescription.addLine("  - Defense Bonuses  : " + editMapString(selectedRace.getRacialTraits().getDefenses().toString()));
 		racePane.scrollToTop(true);
 	}
 	
 	private void setClassDescription() {
 		DnDClass selectedClass = DnDClass.getClassFromMap(ClassType.valueOf(classGroup.getSelectedButton().getName()));
 		classDescription.setText("");
 		classDescription.addLine(selectedClass.getDescription() + "\n\n");
 		classDescription.addLine("  - Role : " + selectedClass.getRole() + "\n\n");
 		classDescription.addLine("  - Power Source : " + selectedClass.getPowerSource() + "\n\n");
 		classDescription.addLine("  - Key Abilities : " + editListString(selectedClass.getKeyAbilities().toString()) + "\n\n");
 		classDescription.addLine("  - Available Skills : " + editListString(selectedClass.getAvailableClassSkills().toString()) + "\n\n");
 		classDescription.addLine("  - Armor Proficiencies : " + editListString(selectedClass.getArmorProficiencies().toString()) + "\n\n");
 		classDescription.addLine("  - Weapon Proficiencies : " + editListString(selectedClass.getWeaponProficiencies().toString()) + "\n\n");
 		classDescription.addLine("  - Bonus to Defense : " + editMapString(selectedClass.getClassTraits().getDefenses().toString()));
 		classPane.scrollToTop(true);
 	}
 	
 	private String editMapString(String str) {
 		return str.replace("{", "\n     ").replace("=", " +").replaceAll("\\,", "\n    ").replace("}", "").replace("_", " ");
 	}
 
 	private String editListString(String str) {
 		return str.replace("[", "\n     ").replaceAll("\\,", "\n    ").replace("]", "").replace("_", " ");
 	}
 	
 	@Override
 	public void receiveObject(Object object) {
 		if(object instanceof CreateCharacter) {
 			CreateCharacter character = (CreateCharacter)object;
 			if(character.accepted) {
 				sb.enterState(DnDClient.MAINMENUSTATE, null, new FadeInTransition());
 			} else {
 				((Label) popup.getChildByName("invalidLoginLabel")).setText(character.error);
 				popup.setVisible(true);
 				popup.grabFocus();
 			}
 		}
 	}
 
 	@Override
 	public int getID() {
 		return stateID;
 	}
 	
 	private void packImages() {
 		dragonbornButton.packImage();
 		halflingButton.packImage();
 		dwarfButton.packImage();
 		elfButton.packImage();
 		eladrinButton.packImage();
 		humanButton.packImage();
 		tieflingButton.packImage();
 		halfElfButton.packImage();
 		maleButton.packImage();
 		femaleButton.packImage();
 		clericButton.packImage();
 		fighterButton.packImage();
 		paladinButton.packImage();
 		rangerButton.packImage();
 		rogueButton.packImage();
 		warlockButton.packImage();
 		warlordButton.packImage();
 		wizardButton.packImage();
 	}
 	
 	private void setRaceImages() {
 		dragonbornButton.setImage(ResourceManager.getInstance().getImage(genderForImages+"_DRAGONBORN"));
 		halflingButton.setImage(ResourceManager.getInstance().getImage("EMPTY_BAG_SQUARE"));
 		dwarfButton.setImage(ResourceManager.getInstance().getImage("EMPTY_BAG_SQUARE"));
 		elfButton.setImage(ResourceManager.getInstance().getImage("EMPTY_BAG_SQUARE"));
 		eladrinButton.setImage(ResourceManager.getInstance().getImage("EMPTY_BAG_SQUARE"));
 		humanButton.setImage(ResourceManager.getInstance().getImage(genderForImages+"_HUMAN"));
 		tieflingButton.setImage(ResourceManager.getInstance().getImage("EMPTY_BAG_SQUARE"));
 		halfElfButton.setImage(ResourceManager.getInstance().getImage("EMPTY_BAG_SQUARE"));
 	}
 }
