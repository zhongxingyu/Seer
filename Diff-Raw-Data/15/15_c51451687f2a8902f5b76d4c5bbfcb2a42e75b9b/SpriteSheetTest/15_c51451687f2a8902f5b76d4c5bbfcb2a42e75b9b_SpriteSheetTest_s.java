 package rajola.test;
 
 import java.util.List;
 
 import org.newdawn.slick.AppGameContainer;
 import org.newdawn.slick.BasicGame;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.SlickException;
 
 import rajola.pipeline.Tile;
 import rajola.pipeline.TileSpriteSheet;
 import rajola.pipeline.sprites.BasicTile;
 import rajola.pipeline.sprites.TileSprite;
 
 public class SpriteSheetTest extends BasicGame {
 
 	private TileSpriteSheet spriteSheet;
 	private TileSpriteSheet spriteSheet2;
 	private TileSprite sprite;
 	private TileSprite animatedSprite;
 	private Tile tile;
 	
 	private List<TileSprite> sprites;
 
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
 		int yOffset = 50;
 		int xOffset = 50;
 		for (int i = 0; i < 4; i++) {
 			spriteSheet.getTileImage(i % 2, (int)Math.floor(i / 2)).draw((i * 40) + xOffset , yOffset);
 		}
 		spriteSheet2.getFullImage().draw(100 ,100);
 		sprite.drawSprite(100, 150);
 		for(int i = 0 ; i < sprites.size() ; i++) {
 			sprites.get(i).drawSprite(50 + (i * 2 *sprites.get(i).getWidth()), 200);
 		}
		tile.drawTile(300, 300);
 	}
 
 	@Override
 	public void init(GameContainer gc) throws SlickException {
 		spriteSheet = new TileSpriteSheet("res/test.png", 16);
 		spriteSheet2 = new TileSpriteSheet("res/test.png");
 		sprite = new TileSprite(spriteSheet2.getFullImage());
 		sprites = TileSprite.autoConstructSprites(spriteSheet.getTiles());
		animatedSprite = new TileSprite(1000 , spriteSheet.getTiles());
 		tile = new BasicTile(0 , animatedSprite);
 	}
 
 	@Override
 	public void update(GameContainer gc, int DELTA) throws SlickException {
 		tile.update(DELTA);
 	}
 }
