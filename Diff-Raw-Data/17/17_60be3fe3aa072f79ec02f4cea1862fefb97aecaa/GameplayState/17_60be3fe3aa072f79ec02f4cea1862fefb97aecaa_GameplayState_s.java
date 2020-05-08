 package game;
 
 import java.io.Serializable;
 
 import map.Cell;
 import map.MapLoader;
 import notify.Notification;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.Music;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.StateBasedGame;
 
 import sounds.Sounds;
 import utils.npeloader.EnemyLoader;
 import utils.npeloader.NPCLoader;
 import GUI.GUI;
 import entities.players.Player;
 
 public class GameplayState extends MouseCapture implements Serializable {
 	
 	private static final long serialVersionUID = -3988950280670496548L;
 	
 	private final int stateID;
 	private Cell currentCell;
 	private Player player;
 	//private transient static World world = new World(new Vec2(0,  9.8f), false);
 	private GUI gui;
 	
 	GameplayState(int stateID) {
 		this.stateID = stateID;
 	}
 	
 	private GUI getGUI(Graphics g){
 		return new GUI(g);
 	}
 	
 	@Override
 	public int getID() {
 		return stateID;
 	}
 	
 	@Override
 	public void init(GameContainer gc, StateBasedGame sbg) throws SlickException {
 		//load enemy/npc data
 		new EnemyLoader().load("data/enemydata.xml");
 		new NPCLoader().load("data/npcdata.xml");
 		
 		//load and initialise maps
 		MapLoader.loadAllMaps("data/layout.xml");
 		
 		//set initial map and player
 		player = new Player(2,2);
 		currentCell = MapLoader.setCurrentCell(player,0,0);
 		
 		gui = getGUI(gc.getGraphics());
 		
 		//audio
 		Sounds.setMusic(new Music("data/sounds/RedCurtain.ogg", true));
 		gc.setMusicVolume(0.5f);
 	}
 	
 	@Override
 	public void render(GameContainer gc, StateBasedGame sbg, Graphics g) throws SlickException {
 		currentCell.render(gc, g);
 		Notification.render(gc, g);
 		gui.render(gc, g);
 	}
 	
 	@Override
 	public void update(GameContainer gc, StateBasedGame sbg, int delta) throws SlickException {
		if(Math.random() < 0.02){
			Notification.addNotification("test notification.");
		}
 		Notification.update(gc);
 		if(HelloGameContainer.getInstance().isRunning()){
 			//update map
 			currentCell = MapLoader.getCurrentCell();
 			//check input
 			Input input = gc.getInput();
 			
 			if(gui.anyWindowOpen()){
 				if (input.isKeyPressed(Input.KEY_ESCAPE)){
 					gui.closeWindow();
 				}
 			}else {
 				currentCell.update(gc);
 				//world.step(delta/1000f, 8, 3);
 			}
 			gui.update(gc);
 		}
 	}
 	
 	@Override
 	public void enter(GameContainer gc, StateBasedGame game) throws SlickException {
 		Sounds.getMusic().loop(1, 0.5f);
 	}
 	
 	/*public static World getWorld() {
 		return world;
 	}*/
 }
