 package game;
 
 import org.newdawn.slick.AppGameContainer;
 import org.newdawn.slick.BasicGame;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 
 // Using slick for practice. By no means final version.
 // This is the main class that handles game flow, and contains the game loop. 
 public class AstralTanks extends BasicGame {
 
 	Image plane = null;
 	Image land = null;
 	float x = 400;
 	float y = 300;
 
 	float scale = 1f;

 	Asteroid[] ast = new Asteroid[20];
 
 
 	public AstralTanks() {
 		super("Asteroids and Astral Tanks");
 	}
 
 	@Override
 	public void init(GameContainer gc) throws SlickException {
 		plane = new Image("Sherman Tank Sprite.png");
 		land = new Image("messier81_800x600.jpg");
 		
 		for(int i = 0; i < ast.length; i++){
 			ast[i] = new Asteroid();
 		}
 
 	}
 
 	@Override
 	public void update(GameContainer gc, int delta) throws SlickException {
 		Input input = gc.getInput();
 
 		if (input.isKeyDown(Input.KEY_A)) {
 			plane.rotate(-0.2f * delta);
 		}
 
 		if (input.isKeyDown(Input.KEY_D)) {
 			plane.rotate(0.2f * delta);
 		}
 
 		if (input.isKeyDown(Input.KEY_W)) {
 			float hip = 0.4f * delta;
 
 			float rotation = plane.getRotation();
 
 			x += hip * Math.sin(Math.toRadians(rotation));
 			y -= hip * Math.cos(Math.toRadians(rotation));
 		}
 		
 		if (input.isKeyDown(Input.KEY_S)) {
 			float hip = 0.4f * delta;
 
 			float rotation = plane.getRotation();
 
 			x -= hip * Math.sin(Math.toRadians(rotation));
 			y += hip * Math.cos(Math.toRadians(rotation));
 		}
 
 		if (input.isKeyDown(Input.KEY_2)) {
 			scale += (scale >= 5.0f) ? 0 : 0.1f;
 			plane.setCenterOfRotation(plane.getWidth() / 2.0f * scale,
 					plane.getHeight() / 2.0f * scale);
 		}
 		if (input.isKeyDown(Input.KEY_1)) {
 			scale -= (scale <= 1.0f) ? 0 : 0.1f;
 			plane.setCenterOfRotation(plane.getWidth() / 2.0f * scale,
 					plane.getHeight() / 2.0f * scale);
 		}
 		
 		// as game runs, moves asteroid across screen
 		// change += 1 to something to do with dy, dx
 		for (int i = 0; i < ast.length; i++){
 			ast[i].x += 1;
 			ast[i].y += 1;
 		}
 		
 		
 	}
 
 	public void render(GameContainer gc, Graphics g) throws SlickException {
 		land.draw(0, 0);
 
 		plane.draw(x, y, scale);
 
 	}
 
 	public static void main(String[] args) throws SlickException {
 		AppGameContainer app = new AppGameContainer(new AstralTanks());
 
 		app.setDisplayMode(800, 600, false);
 		app.start();
 	}
 }
