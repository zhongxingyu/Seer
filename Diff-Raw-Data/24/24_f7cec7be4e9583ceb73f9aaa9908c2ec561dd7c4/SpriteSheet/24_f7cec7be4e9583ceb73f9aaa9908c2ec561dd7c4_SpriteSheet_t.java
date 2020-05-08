 package roguelike_game.graphics;
 
 import java.awt.image.BufferedImage;
import java.io.File;
 import java.io.IOException;
 import javax.imageio.ImageIO;
 
 public class SpriteSheet {
	public static SpriteSheet CHARACTERS = new SpriteSheet("res/sprites/character.png");
	public static SpriteSheet SPRITES = new SpriteSheet("res/sprites/sprites.png");
    public static SpriteSheet WEAPONS = new SpriteSheet("res/sprites/weapons2.png");
    public static SpriteSheet WALLS = new SpriteSheet("res/sprites/walls.png");
         
 	private BufferedImage bs;
 	
	public SpriteSheet(String filename) {		
 		try {
			bs = ImageIO.read(new File(filename));
			//bs = ImageIO.read(SpriteSheet.class.getResource(filename));
 		} catch (IOException e) {
 			System.out.println("spritesheet can not be read in");
 			e.printStackTrace();
 		} catch (IllegalArgumentException e) {
 			System.out.println("spritesheet resource not found");
 			e.printStackTrace();
 		}
 	}
 
 	public BufferedImage getImage() {
 		return bs;
 	}
 }
