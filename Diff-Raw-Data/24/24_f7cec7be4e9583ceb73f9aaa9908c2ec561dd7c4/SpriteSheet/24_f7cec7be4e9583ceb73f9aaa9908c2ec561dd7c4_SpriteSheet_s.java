 package roguelike_game.graphics;
 
 import java.awt.image.BufferedImage;
 import java.io.IOException;
 import javax.imageio.ImageIO;
 
 public class SpriteSheet {
	public static SpriteSheet CHARACTERS = new SpriteSheet("/sprites/character.png");
	public static SpriteSheet SPRITES = new SpriteSheet("/sprites/sprites.png");
    public static SpriteSheet WEAPONS = new SpriteSheet("/sprites/weapons2.png");
    public static SpriteSheet WALLS = new SpriteSheet("/sprites/walls.png");
         
 	private BufferedImage bs;
 	
	public SpriteSheet(String filename) {
 		try {
			bs = ImageIO.read(SpriteSheet.class.getResource(filename));
			System.out.println(System.getProperty("user.dir"));
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
