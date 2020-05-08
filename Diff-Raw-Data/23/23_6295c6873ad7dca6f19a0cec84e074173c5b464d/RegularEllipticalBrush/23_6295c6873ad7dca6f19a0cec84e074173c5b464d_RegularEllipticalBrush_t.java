 package net.coobird.paint.brush;
 
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.image.BufferedImage;
 
 /**
  * The {@code RegularEllipticalBrush} class represents a brush with a regular
  * elliptical shape.
  * 
  * @author coobird
  *
  */
 public class RegularEllipticalBrush extends Brush
 {
 	/**
 	 * The constant used to calculate the number of steps to use to render a
 	 * brush from the size of the brush.
 	 */
 	protected final static int DEFAULT_STEPS_DIVISOR = 2;
 
 	/** 
 	 * The ratio between the semiminor axis and the semimajor axis of the
 	 * ellipse. Must be in the range (0, 1].
 	 */
 	private double angle;
 	
 	/**
 	 * Specifies the number of steps are required to draw the brush.
 	 * This number corresponds to the number of concentric ellipses that are
 	 * rendered to draw the brush.
 	 */
 	private int steps;
 	
 	/** 
 	 * The ratio between the semiminor axis and the semimajor axis of the
 	 * ellipse. Must be in the range (0, 1].
 	 */
 	private double ratio;
 
 	/**
 	 * Cannot instantiate this class with the default constructor.
 	 */
 	@SuppressWarnings("unused")
 	private RegularEllipticalBrush() {}
 	
 	/**
 	 * 
 	 * @param name
 	 * @param size
 	 * @param angle
 	 * @param ratio
 	 * @param brushColor
 	 */
 	public RegularEllipticalBrush(
 			String name,
 			int size,
 			double angle,
 			double ratio,
 			Color brushColor
 	)
 	{
 		this(null, size, size / DEFAULT_STEPS_DIVISOR, angle, ratio, brushColor);
 	}
 	
 	/**
 	 * 
 	 * @param name
 	 * @param size
 	 * @param steps
 	 * @param angle
 	 * @param ratio
 	 * @param brushColor
 	 */
 	public RegularEllipticalBrush(
 			String name,
 			int size,
 			int steps,
 			double angle,
 			double ratio,
 			Color brushColor
 	)
 	{
 		if (size < 1)
 		{
 			String msg = "The size of brush must be at least 1 pixel.";
 			throw new IllegalArgumentException(msg);
 		}
 
		this.size = size;
		this.steps = steps;
		this.angle = angle;
		this.ratio = ratio;
		this.brushColor = brushColor;
		
 		if (name == null)
 		{
 			this.setDefaultName();
 		}
 		else
 		{
 			this.setName(name);
 		}
 		
 		makeBrushImage();
 	}
 	
 	/**
 	 * 
 	 */
 	@Override
 	protected void setDefaultName()
 	{
		this.setName("Regular Elliptical Brush (" + size + " px)");
 	}
 	
 	/**
 	 * Draws the brush image.
 	 */
 	@Override
 	protected void makeBrushImage()
 	{
 		brush = new BufferedImage(size, size, DEFAULT_BRUSH_TYPE);
 		Graphics2D g = brush.createGraphics();
 		
 		double alphaInc = brushColor.getAlpha() / (double)steps;
 		double sizeInc = size / (double)steps;
 		
 		g.rotate(angle, size / 2.0, size / 2.0);
 		
 		int center = (int)Math.round(size / 2.0);
 		
 		for (int i = steps; i > 0; i--)
 		{
 			g.setColor(
 					new Color(
 							brushColor.getRed(),
 							brushColor.getGreen(),
 							brushColor.getBlue(),
 							(int)Math.round(alphaInc * (steps - i))
 					)
 			);
 			
 			g.fillOval(
 					center - (int)Math.round((sizeInc * i) / 2.0),
 					center - (int)Math.round((sizeInc * i) / 2.0 * ratio),
 					(int)Math.round(sizeInc * i),
 					(int)Math.round(sizeInc * i * ratio)
 			);
 		}
 		
 		g.dispose();
 
 		makeBrushThumbnail();
 	}
 }
