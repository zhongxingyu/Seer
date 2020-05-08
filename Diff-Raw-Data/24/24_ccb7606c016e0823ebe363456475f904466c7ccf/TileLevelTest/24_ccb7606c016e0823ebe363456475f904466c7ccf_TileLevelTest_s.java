 package rajola.test;
 
 import org.newdawn.slick.AppGameContainer;
 import org.newdawn.slick.BasicGame;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 
 import rajola.pipeline.BasicTile;
 import rajola.pipeline.Tile;
 import rajola.pipeline.level.TileLevel;
 import rajola.pipeline.sprites.TileSprite;
 import rajola.pipeline.sprites.TileSpriteSheet;
 
 public class TileLevelTest extends BasicGame {
 
 	private TileLevel level;
 	private TileSpriteSheet spriteSheet;
 	private TileSprite animatedSprite;
 	private TileSprite sprite1;
 	private TileSprite sprite2;
 	private BasicTile animatedTile;
 	private BasicTile tile1;
 	private BasicTile tile2;
 	public TileLevelTest() {
 		super("Rajola | TileLevelTest");
 	}
 
 	@Override
 	public void render(GameContainer gc, Graphics g) throws SlickException {
		level.renderTiles(0, 0);
 	}
 
 	@Override
 	public void init(GameContainer gc) throws SlickException {
 		spriteSheet = new TileSpriteSheet("res/testing.png", 8);
 		Image images[][] = {{spriteSheet.getTiles()[0][0] , spriteSheet.getTiles()[0][1]}};
 		animatedSprite = new TileSprite(1000 , images);
 		sprite1 = new TileSprite(spriteSheet.getTiles()[1][0]);
 		sprite2 = new TileSprite(spriteSheet.getTiles()[1][1]);
 		animatedTile = new BasicTile(0 , animatedSprite , -65536); //White
 		tile1 = new BasicTile(1 , sprite1 , 0x000000); //Black
 		tile2 = new BasicTile(1 , sprite2 , -0); //paint.net standard red
 		Tile tiles[] = {animatedTile , tile1 , tile2};
 
 
 		level = new TileLevel("res/levelTest.png" , "Rajola | Test_Level" , tiles);
 	}
 
 	@Override
 	public void update(GameContainer gc, int DELTA) throws SlickException {
 		level.update(DELTA);
 	}
 	
 	public static void main(String args[]) throws SlickException {
 		AppGameContainer apg = new AppGameContainer(new TileLevelTest());
		apg.setDisplayMode(256, 256, false);
 		apg.start();
 	}
 
 }
