 package ch.kanti_wohlen.asteroidminer;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.Texture.TextureFilter;
 import com.badlogic.gdx.graphics.Texture.TextureWrap;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 
 public final class Textures {
	
 	public static Sprite BACKGROUND;
 	public static Sprite SPACESHIP;
 	public static Sprite ASTEROID;
 	public static Sprite LASER;
 	public static Sprite LIFEPOWERUPBOX;
	
 	private Textures() {};
	
 	/**
 	 * Loads the textures into memory.
	 * Needs to be called before drawing any textures except the background
 	 */
 	public static void load() {
 		Texture bg = new Texture(Gdx.files.internal("graphics/background.png"));
 		bg.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
 		BACKGROUND = new Sprite(bg, bg.getWidth(), bg.getHeight());
		
 		Texture sprites = new Texture(Gdx.files.internal("graphics/sprites.png"));
 		SPACESHIP = new Sprite(sprites, 629, 34, 70, 81);
 		SPACESHIP.setOrigin(SPACESHIP.getWidth() / 2, SPACESHIP.getHeight() / 2);
 		SPACESHIP.getTexture().setFilter(TextureFilter.Linear, TextureFilter.Linear);
		
 		ASTEROID = new Sprite(sprites, 164, 237, 84, 83);
 		ASTEROID.setOrigin(ASTEROID.getWidth() / 2, ASTEROID.getHeight() / 2);
 		ASTEROID.getTexture().setFilter(TextureFilter.Linear, TextureFilter.Linear);
		
 		LASER = new Sprite(sprites, 364, 177, 7, 12);
 		LASER.setOrigin(LASER.getWidth() / 2, LASER.getHeight() / 2);
 		LASER.getTexture().setFilter(TextureFilter.Linear, TextureFilter.Linear);
 
 		LIFEPOWERUPBOX = new Sprite(sprites, 256, 154, 38, 38);
 		LIFEPOWERUPBOX.setOrigin(LIFEPOWERUPBOX.getWidth() / 2, LIFEPOWERUPBOX.getHeight() / 2);
 		LIFEPOWERUPBOX.getTexture().setFilter(TextureFilter.Linear, TextureFilter.Linear);
 	}
 }
