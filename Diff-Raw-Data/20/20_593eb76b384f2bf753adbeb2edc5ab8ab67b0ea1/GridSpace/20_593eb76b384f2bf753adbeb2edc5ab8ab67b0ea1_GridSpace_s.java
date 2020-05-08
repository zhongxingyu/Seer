 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.ImageBuffer;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.geom.Rectangle;
 import org.newdawn.slick.geom.Shape;
 import org.newdawn.slick.geom.Vector2f;
 
 // TODO: change width variable to reference game_settings.properties in config
 
 public class GridSpace extends Entity {
 	
 	Image texture;
 	public int type;	// 0=floor; 1=wall
 	private boolean shouldBeDrawn = true;
 	
 	public GridSpace(Entity parent, int spaceType, float x, float y) {
 		super(parent);
 		EntityManager.addGridSpaceEntity(this);
 		width = 16f;
 		height = 16f;
 		
 		position = new Vector2f(x, y);
 		mask = new Rectangle(x, y, width+1, height+1);
 		type = spaceType;
 		
 		// Have to call this again because type isn't defined the first time when it's called in super()...
 		initTextures();
 	}
 	
 	@Override
 	public void update(GameContainer gc, int delta, GameplayState game) {
 		super.update(gc, delta, game);
 	}
 
 	@Override
 	public void render(Graphics g) {
 		if (shouldBeDrawn == true) {
 			super.render(g);
 			texture.draw(position.x, position.y, width, height);
 			
 			// We assume that this won't be drawn every frame.  The flashlight's rays will set this to true if 
 			// it needs to be drawn.
 			//this.setShouldBeDrawn(false);
 		}
 	}
 
 
 	@Override
 	protected void initTextures() {
 		switch(type) {
 		case 0: // floor
 			texture = TextureManager.getTexture("textures/floor.png");
 			break;
 		case 1: // wall
 			texture = TextureManager.getTexture("textures/wall.png");
 			break;
 		}
 	}
 
 
 	@Override
 	public Shape getMask() {
 		return mask;
 	}
 	
 	public void setShouldBeDrawn(boolean a) {
 		this.shouldBeDrawn = a;
 	}
 
 }
