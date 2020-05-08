 /**
  * 
  */
 package ui;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.geom.Vector2f;
 
 import states.LevelState;
 import util.Util;
 import config.Config;
 import entities.Player;
 import entities.Sprite;
 
 /**
  * @author Mullings
  *
  */
 public class Cursor extends Sprite {
 	private Player player;
 	/**
 	 * @param x
 	 * @param y
 	 * @param width
 	 * @param height
 	 * @throws SlickException 
 	 */
 	public Cursor(Player p) throws SlickException {
 		super(0, 0, Config.CURSOR_SIZE, Config.CURSOR_SIZE);
		Image wisp = new Image("assets/images/nonentities/wisp/sprite.png");
 		setImage(wisp);
 		this.player = p;
 	}
 
 	/* (non-Javadoc)
 	 * @see entities.Sprite#update(org.newdawn.slick.GameContainer, int)
 	 */
 	@Override
 	public void update(GameContainer gc, int delta) {
 		super.update(gc, delta);
 		Vector2f mouse = new Vector2f(gc.getInput().getAbsoluteMouseX(), 
 		                  gc.getInput().getAbsoluteMouseY());
 		Vector2f cam = Util.PointToVector2f(LevelState.getCamera().getPosition());
 		Vector2f p = Util.PointToVector2f(player.getPosition());
 		
 		Vector2f aim = mouse.add(cam).sub(p);
 		aim.normalise();
 		aim.scale(Config.CURSOR_DIST);
 		
 		setPosition(p.add(aim));
 	}
 
 	/* (non-Javadoc)
 	 * @see entities.Sprite#render(org.newdawn.slick.Graphics)
 	 */
 	@Override
 	public void render(Graphics graphics) {
 		super.render(graphics);
 	}
 
 }
