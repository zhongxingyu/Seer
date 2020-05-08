 package game;
 
 import lights.PointLight;
 import map.Cell;
 
 import org.jbox2d.common.Vec2;
 import org.jbox2d.dynamics.World;
 import org.lwjgl.opengl.GL11;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.Music;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.StateBasedGame;
 
 import sounds.Sounds;
 import utils.MapLoader;
 import utils.npeloader.EnemyLoader;
 import GUI.GUI;
 import entities.players.Player;
 
 public class GameplayState extends MouseCapture {
 	
 	private final int stateID;
 	private Cell currentCell;
 	private Player player;
 	private Music music;
 	private static World world = new World(new Vec2(0,  9.8f), false);
 	private GUI gui = new GUI();
 	  
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
 		new EnemyLoader().load("data/enemydata.xml");
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
 		music.play(1.0f, 0.15f);		
 	}
 
 	@Override
 	public void render(GameContainer gc, StateBasedGame sbg, Graphics g)
 			throws SlickException {
 		currentCell.render(gc, sbg, g);
 		renderLighting(gc,g);
 		gui.render(g);
 	}
 	
 	private void renderLighting(GameContainer gc, Graphics g){
 		//clear alpha map in preparation
 		g.clearAlphaMap();
 		
 		//render each light
 		new PointLight(0, 0, 12).render(gc,g);
 		new PointLight(gc.getWidth(), 0, 12).render(gc,g);
 		
 		//fill remaining area with darkness... i think... :/
 		GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_DST_ALPHA);
 		g.fillRect(0, 0, gc.getWidth(), gc.getHeight());
 		g.setDrawMode(Graphics.MODE_NORMAL);
 	}
 
 	@Override
 	public void update(GameContainer gc, StateBasedGame sbg, int delta)
 			throws SlickException {
 		//update map
 		currentCell = MapLoader.getCurrentCell();
 		//check input
 		Input input = gc.getInput();
 		if (input.isKeyPressed(Input.KEY_ESCAPE)){
 			if(gui.anyWindowOpen()){
 				gui.closeWindow();
 			}else{
 				gc.sleep(300);
 				music.release();
 				Sounds.releaseSounds();
 				gc.exit();
 			}
 		}
 		if(!gui.anyWindowOpen()){
 			currentCell.updateEntities(gc, sbg, delta);
 			world.step(delta/1000f, 8, 3);
 		}
		gui.update(gc, sbg, delta);
 	}
 
 	public static World getWorld() {
 		return world;
 	}
 }
