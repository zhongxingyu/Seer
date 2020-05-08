 package rajola.test;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.newdawn.slick.AppGameContainer;
 import org.newdawn.slick.BasicGame;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Input;
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
 	private BasicTile cloudAnimatedTile;
 	private BasicTile tile1;
 	private BasicTile tile2;
 	private List<Image> images = new ArrayList();
 	private List<Image> cloudImages = new ArrayList();
 	private int x,y;
 	private BasicTile nullTile;
 	private TileSpriteSheet nullSpriteSheet;
 	public TileLevelTest() {
 		super("Rajola | TileLevelTest");
 	}
 
 	@Override
 	public void render(GameContainer gc, Graphics g) throws SlickException {
 		level.Render();
 	}
 
 	@Override
 	public void init(GameContainer gc) throws SlickException {
 		spriteSheet = new TileSpriteSheet("res/testing.png", 16);
 		nullSpriteSheet = new TileSpriteSheet("res/nullTile.png" , 16);
 		images.add(spriteSheet.getTileImage(0, 0));
 		images.add(spriteSheet.getTileImage(0, 1));
 		cloudImages.add(spriteSheet.getTileImage(2, 0));
 		cloudImages.add(spriteSheet.getTileImage(2, 1));
 		animatedTile = new BasicTile( 0 , new TileSprite(1000 , images) , 0xFFFFFF);
 		cloudAnimatedTile = new BasicTile( 4 , new TileSprite(250 , cloudImages) , 0x00FFFF);
 		tile1 = new BasicTile(1 , new TileSprite(spriteSheet.getTileImage(1, 0)) , 0xFF0000);
 		tile2 = new BasicTile(2 , new TileSprite(spriteSheet.getTileImage(1,1)), 0x000000);
 		nullTile = new BasicTile(3 , new TileSprite(nullSpriteSheet.getTileImage(0, 0)) , 0xAAAAAA);
 		
 		Tile tiles[] = { tile1 , animatedTile , tile2 , cloudAnimatedTile}; //tiles array no longer needs to be in order but it is good convention to do it in order of increasing tileID
 
 		level = new TileLevel(tiles , "res/levelTest.png" , nullTile);
 	}
 
 	@Override
 	public void update(GameContainer gc, int DELTA) throws SlickException {
 		Input input= gc.getInput();
 		level.Update(DELTA, x, y);
 		
 		if(input.isKeyDown(Input.KEY_LEFT) && x < 0) x+=4;
		if(input.isKeyDown(Input.KEY_RIGHT) && x > -1024) x-=4;
 		if(input.isKeyDown(Input.KEY_DOWN) && y > - 512) y-=4;
 		if(input.isKeyDown(Input.KEY_UP) && y < 0) y+=4;
 		System.out.println(x + " | " + y);
 			
 
 	}
 	
 	public static void main(String args[]) throws SlickException {
 		AppGameContainer apg = new AppGameContainer(new TileLevelTest());
 		apg.setDisplayMode(512, 512, false);
 		apg.setShowFPS(false);
 		apg.start();
 	}
 
 }
