 package game;
 
 import map.Cell;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.Music;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 
 import sounds.SoundGroup;
 import sounds.Sounds;
 import utils.EnemyLoader;
 import utils.MapLoader;
 import entities.players.Player;
 
 public class GameplayState extends BasicGameState {
 	
 	private final int stateID;
 	private Cell currentCell;
 	private Player player;
 	private Music music;
 	SoundGroup footsteps;
 	  
     GameplayState(int stateID) {
        this.stateID = stateID;
     }
   
     @Override
     public int getID() {
         return stateID;
     }
 
 	@Override
 	public void init(GameContainer gc, StateBasedGame sbg)
 			throws SlickException {
 		//load enemy data
 		EnemyLoader.loadEnemies("data/enemydata.xml");
 		//map loading goes here. Needs a better home
 		//method needed to load all maps into their correct index in the array
 		MapLoader.setDimensions(3,2);
 		MapLoader.loadMap("data/JezMap01.tmx", 0, 0);
 		MapLoader.loadMap("data/JezMap02.tmx", 1, 0);
 		MapLoader.loadMap("data/JezMap03.tmx", 2, 0);
 		MapLoader.loadMap("data/JezMap13.tmx", 2, 1);
 		//set initial map
 		player = new Player(2,2);
 		currentCell = MapLoader.setCurrentCell(player,0,0);
 		//create player
 		currentCell.addEntity(player);
 		
 		//audio
 		music = new Music("data/sounds/theme.ogg", true);
 		music.play(1.0f, 0.01f);
 		footsteps = new SoundGroup("player/footsteps/grass");
 	}
 
 	@Override
 	public void render(GameContainer gc, StateBasedGame sbg, Graphics g)
 			throws SlickException {  
 		currentCell.render();
 	}
 
 	@Override
 	public void update(GameContainer gc, StateBasedGame sbg, int delta)
 			throws SlickException {
 		//update map
 		currentCell = MapLoader.getCurrentCell();
 		
 		//check input
 		Input input = gc.getInput();
 		if (input.isKeyDown(Input.KEY_ESCAPE)){
 			music.release();
			Sounds.stopSounds();
 			gc.exit();
 		}
 		
 		currentCell.updateEntities(input);
 		
 		//update sounds
 		footsteps.playRandom(gc, player, 150, 0.8f, 0.2f, 0.05f, 0.02f);
 		Sounds.update();
 	}
 
 	
 }
