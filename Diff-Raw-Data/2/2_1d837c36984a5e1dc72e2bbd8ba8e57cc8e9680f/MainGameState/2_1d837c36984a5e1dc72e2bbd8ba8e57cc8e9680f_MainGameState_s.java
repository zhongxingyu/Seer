 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.SpriteSheet;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 
 
 public class MainGameState extends BasicGameState {
         private GameMap map;
         private SpriteSheet playerSprites;
         private Player player;
         public TileDictionary tileDictionary;
         
         private int transX = 0;
         private int transY = 0;
         
 	public MainGameState() {
 		
 	}
 
 	@Override
 	public void init(GameContainer container, StateBasedGame s) throws SlickException {
             tileDictionary = new TileDictionary();
             map = new GameMap(24, 24, tileDictionary);
 	}
 
 	@Override
 	public void render(GameContainer container, StateBasedGame s, Graphics g) throws SlickException {
                 g.translate(transX, transY);
                 // Translate g to player!
 		map.draw(g);
 		player.draw(g);
 	}
 
 	@Override
 	public void update(GameContainer container, StateBasedGame s, int delta) throws SlickException {
 			if (container.getInput().isKeyPressed(Input.KEY_ESCAPE)) {
 				container.exit();
 			}else if (container.getInput().isKeyDown(Input.KEY_LEFT)) {
                 transX += 10;
 			}else if (container.getInput().isKeyDown(Input.KEY_RIGHT)) {
                 transX -= 10;
 			}else if (container.getInput().isKeyDown(Input.KEY_UP)) {
                 transY += 10;
 			}else if (container.getInput().isKeyDown(Input.KEY_DOWN)) {
                 transY -= 10;
 			}
 			transX = player.getX() * -1;
 			transY = player.getY() * -1;
         	player.addControls(container);
         	
         	//If statements here for testing purposes, make a method for it later.
        	///Make it so the map doesn't go out of screen bounds(A bit buggy atm).
         	if(transY > 0 && transX + 
         			map.getScaledWidth() <= container.getWidth()) {
         		transX = -container.getWidth();
         		transY = 0;
         	}else if(transX + map.getScaledWidth() <= container.getWidth() && 
         			transY + map.getScaledHeight() <= container.getHeight()) {
         		transX = -container.getWidth();
         		transY = -container.getHeight();
         	}else if(transX > 0 && transY + 
         			map.getScaledWidth() <= container.getHeight()) {
         		transX = 0;
         		transY = -container.getHeight();
         	}else if(transY + map.getScaledHeight() <= container.getHeight()) {
         		transY = -container.getHeight();
         	}else if(transX + map.getScaledWidth() <= container.getWidth()) {
         		transX = -container.getWidth();
         	}else if(transX > 0) {
         		transX = 0;
         	}else if(transY > 0) {
         		transY = 0;
         	}else if(transX > 0 && transY > 0) {
         		transX = 0;
         		transY = 0;
         	}
         	System.out.println(transY);
 	}
 
 	@Override
 	public int getID() {
 		return 4;
 	}
 	
 	public void setPlayer(Player p) {
 		player = p;
 	}
 }
