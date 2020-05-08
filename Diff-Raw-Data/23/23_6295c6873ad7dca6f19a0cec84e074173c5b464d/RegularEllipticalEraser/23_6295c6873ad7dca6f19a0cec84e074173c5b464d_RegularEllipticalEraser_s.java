 package net.coobird.paint.brush;
 
 import java.awt.Color;
 import java.awt.Graphics;
 
 import net.coobird.paint.BlendingMode;
 
 /**
  * The {@code RegularEllipticalEraser} class represents an erasure with a 
  * regular elliptical shape.
  * 
  * @author coobird
  *
  */
 public class RegularEllipticalEraser extends RegularEllipticalBrush
 {
 	/**
 	 * Instantiates an eraser.
 	 * 
 	 * @param name
 	 * @param size
 	 * @param angle
 	 * @param ratio
 	 * @param alpha
 	 */
 	public RegularEllipticalEraser(
 			String name,
 			int size,
 			double angle,
 			double ratio,
 			float alpha
 	)
 	{
 		super(name, size, angle, ratio, Color.black);
 		this.mode = BlendingMode.BRUSH_ERASER;
 		this.setDefaultName();
 		this.alpha = alpha;
 	}
 	
 	/**
 	 * 
 	 */
 	@Override
 	protected void setDefaultName()
 	{
		this.setName("Regular Elliptical Eraser (" + size +")");
 	}
 
 	/**
 	 * 
 	 */
 	/* (non-Javadoc)
 	 * @see net.coobird.paint.brush.Brush#makeBrushThumbnail()
 	 */
 	@Override
 	protected void makeBrushThumbnail()
 	{
 		super.makeBrushThumbnail();
 		
 		Graphics g = thumbBrush.getGraphics();
 		
 		// Draws an "E" on top of the brush thumbnail.
 		int width = g.getFontMetrics().stringWidth("E");
 		int height = g.getFontMetrics().getHeight();
 		int cx = (thumbBrush.getWidth() / 2) - (width / 2);
 		int cy = (thumbBrush.getHeight() / 2) + (height / 4);
 		g.setColor(Color.white);
 		g.drawString("E", cx, cy);
 		
 		g.dispose();
 	}
 }
