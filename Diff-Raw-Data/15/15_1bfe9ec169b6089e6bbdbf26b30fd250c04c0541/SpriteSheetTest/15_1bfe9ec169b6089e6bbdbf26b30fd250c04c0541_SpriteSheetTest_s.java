 package rajola.test;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.newdawn.slick.AppGameContainer;
 import org.newdawn.slick.BasicGame;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 
 import rajola.pipeline.TileSpriteSheet;
 
 public class SpriteSheetTest extends BasicGame {
 
 	private TileSpriteSheet spriteSheet;
 	private List<Image> tiles = new ArrayList<Image>();
 
 	public SpriteSheetTest() {
 		super("Rajola| SPRITE SHEET TEST");
 	}
 
 	public static void main(String args[]) throws SlickException {
 		AppGameContainer gc = new AppGameContainer(new SpriteSheetTest());
 		gc.setDisplayMode(600, 800, false);
 		gc.start();
 	}
 
 	@Override
 	public void render(GameContainer gc, Graphics g) throws SlickException {
 		tiles.get(0).draw(50, 50); //Will draw the first tile (0 , 0)
 	}
 
 	@Override
 	public void init(GameContainer gc) throws SlickException {
 		spriteSheet = new TileSpriteSheet("res/test.png", 32, 16);
 		tiles = spriteSheet.getTiles();	
 	}
 
 	@Override
 	public void update(GameContainer gc, int DELTA) throws SlickException {
 		
 	}
 }
