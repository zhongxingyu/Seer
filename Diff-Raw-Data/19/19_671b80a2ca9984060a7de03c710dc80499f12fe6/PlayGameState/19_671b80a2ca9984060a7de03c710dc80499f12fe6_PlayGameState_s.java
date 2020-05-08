 package fi.ringofsnake.gamestates;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 
 import fi.ringofsnake.main.Main;
 import fi.ringofsnake.entities.SnakeMap;
 import fi.ringofsnake.entities.Tile;
 import fi.ringofsnake.io.ResourceManager;
 
 import fi.ringofsnake.entities.Player;
 
 public class PlayGameState extends BasicGameState {
 
 	private int stateID = -1;
 
 	private Image snakeUpper;
 	private Image snakeBody;
 	private Image snakeLower;
 	
 
 	private SnakeMap currentMap = null;
 
 	private Player player;


	private SnakeMap current_map = null;
 	
 	private float[] offset = {0.0f, 0.0f};
 
 	
 	public PlayGameState(int stateID) {
 		this.stateID = stateID;
 	}
 	
 	@Override
 
 	public int getID() {
 		return stateID;
 	}
 	
 	@Override
 	public void init(GameContainer container, StateBasedGame game)	throws SlickException {
 		currentMap = new SnakeMap();
 		player = new Player(container);		
 	
		System.out.println( "tile" + current_map.getTile('^'));
 		
		snakeUpper = current_map.getTile('^').getImage();
		snakeBody  = current_map.getTile('|').getImage();
		snakeLower = current_map.getTile('v').getImage();
 		
 	}
 	
 	@Override
 	public void render(GameContainer container, StateBasedGame game, Graphics g)
 			throws SlickException 
 	{
		Image tile = current_map.getTile(0, 0).getImage();
 		assert(tile.getWidth() > 0 && tile.getHeight() > 0);
 		
 		//Background
 		for (int i = -1, n = container.getScreenWidth()/tile.getWidth() + 1; i < n; i++) 
 		{
 			for (int j = -1, m = container.getScreenWidth()/tile.getWidth() + 1; 
 					 j < m; 
 					 j++) 
 			{ 
 				g.drawImage(tile, i*tile.getWidth() + offset[0], j*tile.getHeight() + offset[1]);
 			}
 		}
 		
 		//TODO read map
 
 		int x = 50;
 		int y = 50;
 		
 		g.drawImage(snakeUpper, x, y);
 		g.drawImage(snakeBody, x, y + snakeUpper.getHeight());
 		g.drawImage(snakeLower, x, y + snakeUpper.getHeight() + snakeBody.getHeight());
 
 		// just for now
 		g.fillRect(0, 30, 800, 500);
 		player.render(container, g);
 
 	}
 	
 	@Override
 	public void update(GameContainer container, StateBasedGame game, int delta)
 			throws SlickException {
 
 
 		Input input = container.getInput();
 		player.update(container, delta);
 
 		if(input.isKeyPressed(Input.KEY_PAUSE ) || input.isKeyPressed(Input.KEY_ESCAPE)) {
 			game.enterState(Main.MAINMENU_GAME_STATE);
 		}
 		
		int mod = current_map.getTile(0, 0).getImage().getHeight();	//assume rect tiles
 		//FIXME bg scrolling
 		float step = (float)Math.sin((double)(System.currentTimeMillis())/1000.0);
 		offset[0] = ((offset[0]+step)%mod); 
 		offset[1] = ((offset[1]+step)%mod);
 	}
 }
