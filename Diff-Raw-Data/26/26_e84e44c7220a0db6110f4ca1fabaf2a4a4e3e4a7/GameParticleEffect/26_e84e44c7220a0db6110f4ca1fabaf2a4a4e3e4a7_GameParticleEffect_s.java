 package game;
 
 import java.awt.AlphaComposite;
 import java.awt.Color;
 import java.awt.Composite;
 import java.awt.Graphics2D;
 import java.awt.Polygon;
 import java.awt.geom.AffineTransform;
 import java.awt.geom.Area;
 
 public class GameParticleEffect extends GameObject {
 
 	public static enum EffectType { ENGINE_FIRE, ASTEROID_SPLIT };
 	public static final long ENGINE_FIRE_DURATION = 1000;
 	public static final int ENGINE_FIRE_XPOINTS[] = { 0, 3, 3, 0  };
 	public static final int ENGINE_FIRE_YPOINTS[] = { 0, 0, 3, 3 };
 	public static final int ENGINE_FIRE_WIDTH = 3;
 	public static final int ENGINE_FIRE_HEIGHT = 3;
 	public static final int ENGINE_FIRE_OFFSET = 5;
 	public static final double ENGINE_FIRE_VEL_OFFSET_MAX = 0.3;
 	public static final double ENGINE_FIRE_ROT_MAX = 0.02;
 	public static final double ENGINE_FIRE_ACCEL_MAX = 0.01;
 	
 	public static final long ASTEROID_SPLIT_DURATION = 5000;
 	public static final int ASTEROID_SPLIT_XPOINTS[] = { 0, 30, 30 };
 	public static final int ASTEROID_SPLIT_YPOINTS[] = { 0, 0, 30 };
 	public static final int ASTEROID_SPLIT_WIDTH = 30;
 	public static final int ASTEROID_SPLIT_HEIGHT = 30;
 	public static final double ASTEROID_SPLIT_ROT_MAX = 0.08;
 	public static final double ASTEROID_SPLIT_VEL = 0.9;
 	
 	private EffectType type;
 	private long fadeTime;
 	
 	private GameParticleEffect( double x, double y, double vx,
 			double vy, double ax, double ay, double theta, double thetaVel,
 			double thetaAccel, EffectType type) {
 		super(new Area (new Polygon (
 				ENGINE_FIRE_XPOINTS, 
 				ENGINE_FIRE_YPOINTS, 
 				ENGINE_FIRE_XPOINTS.length)),
 				x, y, vx, vy, ax, ay, theta, thetaVel, thetaAccel);
 		this.type = type;
 
 		switch( type )
 		{
 		case ENGINE_FIRE:
 			fadeTime = System.currentTimeMillis() + ENGINE_FIRE_DURATION;
 			break;
 		case ASTEROID_SPLIT:
 			fadeTime = System.currentTimeMillis() + ASTEROID_SPLIT_DURATION;
 			break;
 		}
 	}
 
 	/**
 	 * Note, use this constructor only for engine fire
 	 * @param x
 	 * @param y
 	 * @param vx
 	 * @param vy
 	 * @param theta
 	 */
 	public GameParticleEffect( double x, double y, double vx, double vy, double theta )
 	{
 		this( x + ENGINE_FIRE_WIDTH / 2 - Math.cos(theta) * ENGINE_FIRE_OFFSET, 
 			  y + ENGINE_FIRE_HEIGHT / 2 - Math.sin(theta) * ENGINE_FIRE_OFFSET, 
 			  -vx + (Math.random()-0.5)*ENGINE_FIRE_VEL_OFFSET_MAX, 
 			  -vy + (Math.random()-0.5)*ENGINE_FIRE_VEL_OFFSET_MAX, 
 			  0.0, 0.0, 
 			  theta, (Math.random()-0.5)*ENGINE_FIRE_ROT_MAX, 
 			  (Math.random()-0.5)*ENGINE_FIRE_ACCEL_MAX, EffectType.ENGINE_FIRE);
 	}
 
 	/**
 	 * Use this constructor only for the asteroid splits
 	 * @param x
 	 * @param y
 	 */
 	public GameParticleEffect( double x, double y  )
 	{
 		this( x + ASTEROID_SPLIT_WIDTH / 2, 
 			  y + ASTEROID_SPLIT_HEIGHT / 2, 
 			  (Math.random()-0.5)*ASTEROID_SPLIT_VEL, 
 			  (Math.random()-0.5)*ASTEROID_SPLIT_VEL, 
 			  0.0, 0.0, 
 			  Math.random() * Math.PI * 2, 
 			  (Math.random()-0.5)*ASTEROID_SPLIT_ROT_MAX, 
 			  0.0, EffectType.ASTEROID_SPLIT);
 	}
 	
 	@Override
 	public void drawObject(Graphics2D g) {
 		float percentRemaining = 1.0f; 
 		
		
 		switch( type )
 		{
 		case ENGINE_FIRE:
 			g.setColor( Color.green );
 			percentRemaining =  (fadeTime - System.currentTimeMillis());
 			percentRemaining /= ENGINE_FIRE_DURATION;
 			break;
 		case ASTEROID_SPLIT:
 			g.setColor( Color.gray );
 			percentRemaining =  (fadeTime - System.currentTimeMillis());
 			percentRemaining /= ASTEROID_SPLIT_DURATION;
 			break;
 		}
 		
 			AffineTransform at = g.getTransform ();
 			Composite savedComposite = g.getComposite();
 			
 			if( percentRemaining > 0.0f && percentRemaining <= 1.0f )
 			{
 				g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, percentRemaining));
 			}
 			else
 			{
 				g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.0f));
 			}
 			
 			/* translate and draw */
 			g.transform (AffineTransform.getTranslateInstance ((double) x, (double) y));
 			g.draw(objectArea);
 			
 			g.setComposite( savedComposite );
 			g.setTransform (at);
 	}
 
 	@Override
 	public void update() {
 		updatePosition();
 
 		if( System.currentTimeMillis() >= fadeTime )
 		{
 			isValid = false;
 		} 
 		
 		if (x > GameCanvas.defaultDimension.width)
 			x -= GameCanvas.defaultDimension.width;
 
 		if (x < 0)
 			x = GameCanvas.defaultDimension.width - 
 			objectArea.getBounds ().width;
 
 		if (y > GameCanvas.defaultDimension.height)
 			y -= GameCanvas.defaultDimension.height;
 
 		if (y < 0)
 			y = GameCanvas.defaultDimension.height - 
 			objectArea.getBounds ().height;
 	}
 }
