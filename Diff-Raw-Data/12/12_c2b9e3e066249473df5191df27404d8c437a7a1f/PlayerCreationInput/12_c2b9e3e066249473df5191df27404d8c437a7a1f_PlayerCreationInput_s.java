 package interfaces;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import managers.GameManager;
 import managers.PlayerManager;
 import screens.SettingsScreen;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.scenes.scene2d.Stage;
 
 /**
  * A class that will contain all of the ui for the player creations screen.
  * @author antonio
  * @since 9/17/13
  */
 public class PlayerCreationInput {
 
 	private final int MAX_PLAYERS = 4;
 	private final int MIN_PLAYERS = 2;
 
 	private final String[] DIFFICULTIES = {"BEGINNER", "STANDARD", "TOURNAMENT"};
 	private final String[] MAPS = {"STANDARD", "RANDOM"};
 	
 	private List<PlayerVariableInputs> players;
 	
 	
 	private int positionX;
 	private int positionY;
 	
 	private int width;
 	private int height;
 	
 	private final int BUFFER_Y = 85;
 	private final int BUFFER_X = 25;
 	
 	private Stage stage;  
 	
 	private Button addPlayerButton;
 	private Button removePlayerButton;
 	private Button startGameButton;
 	
 	private DropMenu difficultySelect;
 	private DropMenu mapSelect;
 	
 	private SettingsScreen screen;
 	
 	private PlayerCreationInput(){}
 	
 	public PlayerCreationInput(int posX, int posY, int w, int h, SettingsScreen s){
 		this();
 		screen = s;
 		positionX = posX;
 		positionY = posY;
 		width = w;
 		height = h;
 		
 		players = new ArrayList<PlayerVariableInputs>();
 		
 		stage = new Stage(width, height, false);
 		Gdx.input.setInputProcessor(stage);
 			
 		for(int i = 0 ; i < MIN_PLAYERS ; i++){
 			addPlayer();
 		}
 		
 		addButtons();
 		addDropDowns();
 	}
 	
 	private void addButtons(){
 		addPlayerButton = new AddPlayerButton(100, 50, this);
 		removePlayerButton = new RemovePlayerButton(175, 50, this);
 		startGameButton = new StartGameButton(500, 38, this, screen);
 		stage.addActor(addPlayerButton);
 		stage.addActor(removePlayerButton);
 		stage.addActor(startGameButton);
 	}
 	
 	private void addDropDowns(){
 		difficultySelect = new DropMenu(DIFFICULTIES, 300, 50, true, "DIFFICULTY");
 		mapSelect = new DropMenu(MAPS, 400, 50, true, "MAPS");
 		
 		stage.addActor(difficultySelect);
 		stage.addActor(mapSelect);
 	}
 	
 	public void draw(SpriteBatch batch){
 		stage.draw();
 		for(PlayerVariableInputs p : players){
 			p.draw(batch);
 		}
 	}
 	
 	public boolean addPlayer(){
 		if(players.size() >= MAX_PLAYERS){
 			return false;
 		}
 		PlayerVariableInputs temp = new PlayerVariableInputs(positionX, 
 					positionY - (BUFFER_Y * players.size()), stage);
 		players.add(temp);
 		return true;
 	}
 	
 	public boolean removePlayer(){
 		if(players.size() <= MIN_PLAYERS){
 			return false;
 		}
 		PlayerVariableInputs temp = players.get(players.size() - 1);
 		temp.removeActorsFromStage(stage);
 		players.remove(temp);
 		return true;
 	}
 	
 	public boolean startGame(PlayerManager manager, GameManager gameManager){
 		for(PlayerVariableInputs player : players){
 			player.addPlayer(manager);
 		}
		System.out.println(difficultySelect.getCurrentItem());
		gameManager.setDifficulty(difficultySelect.getCurrentItem());
 		gameManager.setMap(mapSelect.getCurrentItem());
 		gameManager.setPPU();
 		return true;
 	}
 }
