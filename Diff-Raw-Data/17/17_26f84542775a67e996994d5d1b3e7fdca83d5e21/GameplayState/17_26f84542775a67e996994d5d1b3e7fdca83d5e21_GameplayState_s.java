 package se.chalmers.tda367.std.gui;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 
 import se.chalmers.tda367.std.core.GameBoard;
 import se.chalmers.tda367.std.core.Properties;
 import se.chalmers.tda367.std.core.enemies.IEnemy;
 import se.chalmers.tda367.std.core.exported.BasicAttackTower;
 import se.chalmers.tda367.std.core.tiles.BuildableTile;
 import se.chalmers.tda367.std.core.tiles.IBoardTile;
 import se.chalmers.tda367.std.core.tiles.IBuildableTile;
 import se.chalmers.tda367.std.core.tiles.PathTile;
 import se.chalmers.tda367.std.core.tiles.towers.ITower;
 import se.chalmers.tda367.std.utilities.Position;
 
 public class GameplayState extends BasicGameState {
 	private int stateID;
 	private Image pathTile;
 	private Image buildableTile;
 	private Image towerTile;
 	private Image enemy;
 	private int tileScale;
 	private GameBoard board;
 	
 	public GameplayState(int stateID) {
 		this.stateID = stateID;
 	}
 	
 	private String getResourcePath(String path){
 		return getClass().getResource(path).getPath();
 	}
 	
 	@Override
 	public void init(GameContainer container, StateBasedGame state)
 			throws SlickException {
 		pathTile = new Image(getResourcePath("/path_tile.jpg"));
 		buildableTile = new Image(getResourcePath("/buildable_tile.png"));
 		towerTile = new Image(getResourcePath("/tower_tile2.png"));
 		enemy = new Image(getResourcePath("/enemy.png"));
 		tileScale = Properties.INSTANCE.getTileScale();
 		
 		board = new GameBoard(25,20, new Position(0,12), new Position (19,12));
 	}
 	
 	//TODO: Remake this system
     @Override
     public void mouseClicked(int button,
             int x,
             int y,
             int clickCount){
     	x = x / tileScale;
 		y = y / tileScale;
 		Position p = Position.valueOf(x, y);
 		
 		if(board.getTileAt(p) instanceof IBuildableTile)
 			board.placeTile(new BasicAttackTower(), p);
     }
 	
 	@Override
 	public void render(GameContainer container, StateBasedGame state, Graphics g)
 			throws SlickException {
        int w = board.getWidth();
         int h = board.getHeight();
         for(int y = 0; y < h; y++){
         	for(int x = 0; x < w; x++){
         		IBoardTile tile = board.getTileAt(x, y);
         		int nX = x * tileScale;
         		int nY = y * tileScale;
         		if(tile instanceof PathTile){
         			pathTile.draw(nX, nY, tileScale, tileScale);
         		}
         		else if(tile instanceof BuildableTile){
         			buildableTile.draw(nX, nY, tileScale, tileScale);
         		}
         		else if(tile instanceof ITower){
         			towerTile.draw(nX, nY, tileScale, tileScale);
         		}
         	}
         }
 	}
 
 	@Override
 	public void update(GameContainer container, StateBasedGame state, int arg2)
 			throws SlickException {
 		
 	}
 
 	@Override
 	public int getID() {
 		return stateID;
 	}
 
 }
