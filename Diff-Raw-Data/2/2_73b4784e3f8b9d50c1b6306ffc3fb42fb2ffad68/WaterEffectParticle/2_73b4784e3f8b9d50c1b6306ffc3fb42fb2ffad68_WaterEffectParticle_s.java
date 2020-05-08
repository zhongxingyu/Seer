 package entities.objects.watereffects;
 
 import map.MapLoader;
 import map.tileproperties.TileProperty;
 
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 
 import utils.Position;
 import utils.interval.one.Interval;
 import utils.interval.one.Range;
 import utils.particles.particle.YAxisAttractorParticle;
 
 public class WaterEffectParticle extends YAxisAttractorParticle {
 	
 	private static final Range<Float> radrange = new Interval(0.005f, 0.01f);
 	private static final Image bubblez;
 	private static final Color c = new Color(0.6f,0.6f,0.9f,0.7f);
 	
 	static {
 		Image b = null;
 		try {
 			b = new Image("data/images/circle.png");
 		} catch (SlickException e) {
 			e.printStackTrace();
 		}
 		bubblez = b;
 	}
 	
 	public WaterEffectParticle(Position position,
 			Position velocity, float targety) {
 		super(bubblez, position, velocity, c, radrange.random(), targety, new Position(0.7f,0.7f));
 	}
 	
 	@Override
 	public void update(GameContainer gc) {
 		dxdy.translate(0,Math.signum(getCenterY()-targetY)*-0.02f);
 		super.update(gc);
 	}
 	
 	@Override
 	public boolean isAlive() {
		return super.isAlive() && !MapLoader.getCurrentCell().getTile((int) getCenterX(), (int) getCenterY()).lookup(TileProperty.BLOCKED);
 	}
 	
 	@Override
 	protected Position getInertia() {
 		return new Position(0.5f,Math.signum(targetY-getCenterY())*0.1f+0.8f);
 	}
 	
 }
