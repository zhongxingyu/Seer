 package com.friendlyblob.mayhemandhell.client.screens;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.audio.Music;
 import com.badlogic.gdx.graphics.GL20;
 import com.badlogic.gdx.scenes.scene2d.Actor;
 import com.badlogic.gdx.scenes.scene2d.Group;
 import com.badlogic.gdx.scenes.scene2d.InputEvent;
 import com.badlogic.gdx.scenes.scene2d.Stage;
 import com.badlogic.gdx.scenes.scene2d.ui.Button;
 import com.badlogic.gdx.scenes.scene2d.ui.Button.ButtonStyle;
 import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
 import com.badlogic.gdx.scenes.scene2d.ui.Skin;
 import com.badlogic.gdx.scenes.scene2d.ui.Table;
 import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
 import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
 import com.badlogic.gdx.scenes.scene2d.ui.TextField;
 import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldStyle;
 import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
 import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
 import com.friendlyblob.mayhemandhell.client.MyGame;
 import com.friendlyblob.mayhemandhell.client.entities.gui.EventNotifications;
 import com.friendlyblob.mayhemandhell.client.entities.gui.GuiManager;
 import com.friendlyblob.mayhemandhell.client.entities.gui.LiveNotifications;
 import com.friendlyblob.mayhemandhell.client.gameworld.GameWorld;
 import com.friendlyblob.mayhemandhell.client.helpers.Assets;
 import com.friendlyblob.mayhemandhell.client.network.packets.client.ChatMessagePacket;
 
 public class GameScreen extends BaseScreen{
 	private GameWorld gameWorld;
 	
 	public LiveNotifications notifications;
 	public EventNotifications eventNotifications;
 	
 	public GuiManager guiManager;
 	
 	// Music stuff
 	private Music music;
 	private final float MAX_VOLUME = .2f;
 	private final float VOLUME_GROWTH_SPEED = .05f;
 	private float currVolume;
 		
 	// UI related
     private Skin skin;
     private Stage stage;
     
     // TODO: find better solution
     private boolean touchedUiElement;
     
     private Table root;
 	
 	public GameScreen(MyGame game) {
 		super(game);
 		
 		initGuiElements();
 		
 		GameWorld.initialize();
 		gameWorld = GameWorld.getInstance();
 		gameWorld.setGame(game);
 		
 		notifications = new LiveNotifications();
 		eventNotifications = new EventNotifications();
 	}
 
 	private void initGuiElements() {		
         stage = new Stage(MyGame.SCREEN_WIDTH, MyGame.SCREEN_HEIGHT);
         
         skin = new Skin(Gdx.files.internal("data/ui2/uiskin.json"));
 
         // Create a table that fills the screen. Everything else will go inside this table.
         root = new Table();
         root.setFillParent(true);
         root.center().top();
         root.padTop(5);
         stage.addActor(root);
         
         // Create a button with the "default" TextButtonStyle. A 3rd parameter can be used to specify a name other than "default".
         TextFieldStyle whiteStyle = skin.get("input_white", TextFieldStyle.class);
         final TextField chatMessageField = new TextField("", whiteStyle);
         chatMessageField.setMessageText("Say something!");
         chatMessageField.setVisible(false);
         
         chatMessageField.addListener(new ClickListener() {
         	@Override
         	public void clicked (InputEvent event, float x, float y) {
         		super.clicked(event, x, y);
         		touchedUiElement = true;
         	}
         });
         
         TextButtonStyle greenStyle = skin.get("green", TextButtonStyle.class);        
         final TextButton sayButton = new TextButton("Say", greenStyle);
         sayButton.setWidth(35);
         sayButton.setVisible(false);
         
 		sayButton.addListener(new ChangeListener() {
 				@Override
 		        public void changed (ChangeEvent event, Actor actor) {
 		    		touchedUiElement = true;
 		    		// Send the packet with broadcast type
 					MyGame.connection.sendPacket(new ChatMessagePacket("/b " + chatMessageField.getText()));
 					// Lose the focus
 					chatMessageField.setText("");
 					stage.unfocusAll();
 				}
 		});
 
 		Group textGroup = new Group();
 		textGroup.addActor(chatMessageField);
 		textGroup.size(chatMessageField.getWidth(), chatMessageField.getHeight());
 		
 		Group sayButtonGroup = new Group();
 		sayButtonGroup.addActor(sayButton);
 		sayButtonGroup.size(sayButton.getWidth(), sayButton.getHeight());
 		
 		HorizontalGroup horizontalGroup = new HorizontalGroup();
 		horizontalGroup.addActor(textGroup);
 		horizontalGroup.addActor(sayButtonGroup);
 		horizontalGroup.setSpacing(5);
 		
 		root.add(horizontalGroup).expandX().padLeft(25);
 		
         ButtonStyle toggleStyle = skin.get("button_chat", ButtonStyle.class);        
         final Button chatToggleButton = new Button(toggleStyle);
         
         chatToggleButton.addListener(new ChangeListener() {
 				@Override
 		        public void changed (ChangeEvent event, Actor actor) {
 		    		boolean visible = chatMessageField.isVisible();
 		    		
 		    		chatMessageField.setVisible(!visible);
 		    		sayButton.setVisible(!visible);
 				}
 		});
         
         root.add(chatToggleButton).right().padRight(5);
 	}
 
 	@Override
 	public void draw(float deltaTime) {
         Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
 		
 		spriteBatch.begin();
 		/*---------------------------------------
 		 * World
 		 */
 		gameWorld.draw(spriteBatch);
 		
 		spriteBatch.end();
 		/*---------------------------------------
 		 * GUI Elements
 		 */
 		spriteBatch.begin();
 		spriteBatch.setProjectionMatrix(guiCam.combined);
 		
 		Assets.defaultFont.draw(spriteBatch, fpsText, 20, 20);
 		
 		spriteBatch.end();
 		
         stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
         stage.draw();
 	}
 	
 	@Override
 	public void update(float deltaTime) {
 		gameWorld.update(deltaTime);
 
 		if (!touchedUiElement) {
 			gameWorld.updateWorldInput();
 		}
 		
 		touchedUiElement = false;
 		
 		if (currVolume < MAX_VOLUME) {
 			currVolume += deltaTime * VOLUME_GROWTH_SPEED;
 			music.setVolume(currVolume);	
 		}
 	}
 
 	@Override
 	public void prepare() {
 		currVolume = 0;
 		
 		music = Assets.manager.get("sounds/bg.wav");
 		music.setLooping(true);
 		music.setVolume(currVolume);
 		music.stop();
 		music.play();
 		
 		Gdx.input.setInputProcessor(stage);
 	}
 	
 	public GameWorld getWorld() {
 		return gameWorld;
 	}
 
 }
