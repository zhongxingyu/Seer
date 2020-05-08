 package com.cs2340.anonymule.Screen;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Screen;
 import com.badlogic.gdx.files.FileHandle;
 import com.badlogic.gdx.graphics.GL20;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
 import com.badlogic.gdx.scenes.scene2d.InputEvent;
 import com.badlogic.gdx.scenes.scene2d.InputListener;
 import com.badlogic.gdx.scenes.scene2d.Stage;
 import com.badlogic.gdx.scenes.scene2d.ui.Label;
 import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
 import com.badlogic.gdx.scenes.scene2d.ui.Skin;
 import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
 import com.badlogic.gdx.utils.Base64Coder;
 import com.badlogic.gdx.utils.Json;
 import com.cs2340.anonymule.Anonymule;
 import com.cs2340.anonymule.Map;
 import com.cs2340.anonymule.Player;
 
 public class MainMenuScreen implements Screen {
 
     //reference to the game
     private Anonymule anonymule;
 
 
     private String[] difficulty;
     private Integer[] mapType;
     private Integer[] players_count;
     private SelectBox difficultySelectBox;
     private SelectBox mapSelectBox;
     private TextButton continueButton, loadButton;
 
     //rendering
     private SpriteBatch batch;
     private Skin skin;
     private Texture background;
     private Stage stage;
     private OrthographicCamera camera;
     private Label mapTypeLabel;
     private Label difficultyLabel;
     private Label playersLabel;
     private SelectBox playersSelectBox;
 
     /**
      * Initializes the main menu screen for the game
      * @param anonymule The main game
      */
 
     public MainMenuScreen(Anonymule anonymule){
         difficulty = new String[]{"easy", "hard"};
         mapType = new Integer[]{1, 2};
         players_count = new Integer[]{1, 2, 3, 4};
         this.anonymule = anonymule;
         batch = new SpriteBatch();
         stage = new Stage(480, 800, false);
 
         skin = new Skin(Gdx.files.internal("Anonymule/assets/skins/uiskin.json"));
         
         loadButton = new TextButton("Load Last Game", skin);
         continueButton = new TextButton("Continue", skin);
         background = new Texture(Gdx.files.internal("Anonymule/assets/textures/Concrete_splashbg.jpg"));
         camera = new OrthographicCamera();
         camera.setToOrtho(false, 480, 800);
        loadButton.setPosition(480/2 - 70, 100);
         continueButton.setPosition(480 / 2 - 70, 800 / 2);
         mapTypeLabel = new Label("Map Type", skin);
         mapTypeLabel.setPosition(120, 605);
         difficultyLabel = new Label("Difficulty", skin);
         difficultyLabel.setPosition(120, 550);
         playersLabel = new Label("Players", skin);
         playersLabel.setPosition(120, 660);
 
         difficultySelectBox = new SelectBox(difficulty, skin);
         difficultySelectBox.setPosition(200, 545);
 
         mapSelectBox = new SelectBox(mapType, skin);
         mapSelectBox.setPosition(200, 600);
 
         playersSelectBox = new SelectBox(players_count, skin);
         playersSelectBox.setPosition(200, 655);
 
         // Stage handles the inputs
         Gdx.input.setInputProcessor(stage);
         
         // Listen for clicks on the Load button        
         loadButton.addListener(new InputListener() {
         	public boolean touchDown(InputEvent even, float x, float y, int pointer, int button) {
         		return true;
         	}
         	public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
         		load();
         	}
         });
 
         // Listen for clicks on the Continue button
         continueButton.addListener(new InputListener() {
 
             public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                 return true;
             }
 
             public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                 updateScreen();
             }
 
         });
 
         // Add all the actors(buttons, boxes, labels etc) to the stage
         stage.addActor(loadButton);
         stage.addActor(continueButton);
         stage.addActor(difficultySelectBox);
         stage.addActor(mapSelectBox);
         stage.addActor(playersSelectBox);
         stage.addActor(mapTypeLabel);
         stage.addActor(difficultyLabel);
         stage.addActor(playersLabel);
 
 
     }
     
     public void load() {
     	FileHandle loadFile = Gdx.files.local("data/savefile.json");
     	Json json = new Json();
     	if (loadFile.exists()) {
     			String encodedFile = loadFile.readString();
     			String decodedFile = Base64Coder.decodeString(encodedFile);
     			Map map = json.fromJson( Map.class, decodedFile );
     			anonymule.setMap(map);
     			anonymule.setScreen(new GameScreen(anonymule));
     	}
     }
 
     /**
      * Updates the screen to the player configuration screen
      */
 
     public void updateScreen(){
         Map map = anonymule.getMap();
         String difficulty = difficultySelectBox.getSelection();
         String mapType = mapSelectBox.getSelection();
         int players = Integer.parseInt(playersSelectBox.getSelection());
 
         // Sets the difficulty depending on the selection
 
         if(difficulty.equals("easy"))
             map.setDifficulty(1);
         else
             map.setDifficulty(2);
 
         // Sets the map depending on the selection
 
         if(mapType.equals("1"))
             map.setMap_type(1);
         else
             map.setMap_type(2);
 
         // Adds the selected number of players with default values to be accessed later
 
         map.getPlayerList().clear();
         for(int i = 0; i < players; i++){
             map.addPlayer(new Player());
         }
 
         anonymule.setScreen(new PlayerConfigScreen(anonymule));
 
     }
 
     /**
      * Renders the background and game objects on the screen
      * @param delta Time in seconds since the last render
      */
 
     @Override
     public void render(float delta) {
 
         // Clears the screen with the given color
         Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1f);
         Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
 
         camera.update();
         batch.setProjectionMatrix(camera.combined);
 
         //Draw the background
         batch.begin();
         batch.draw(background, 0, 0);
         batch.end();
 
 
 
         // Draw the stage with all its actors
         batch.begin();
         stage.act(Gdx.graphics.getDeltaTime());
         stage.draw();
         batch.end();
 
 
     }
 
     /**
      * Resizes the game everytime the game is not in paused state
      * @param width The new width of the game
      * @param height The new height of the game
      */
 
     @Override
     public void resize(int width, int height) {
     }
 
     /**
      * Called when the app makes this screen active
      */
 
     @Override
     public void show() {
     }
 
     /**
      * Called when the app makes another screen active
      */
 
     @Override
     public void hide() {
     }
 
     /**
      * Called before dispose. Also called on android when user presses the home button
      */
 
     @Override
     public void pause() {
     }
 
     /**
      * Only called on android when an app resumes from a paused state
      */
 
     @Override
     public void resume() {
     }
 
     /**
      * Cleans up any loaded assets
      */
 
     @Override
     public void dispose() {
     }
 }
