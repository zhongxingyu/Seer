 package net.coobird.paint.image;
 
 import java.awt.AlphaComposite;
 import java.awt.Color;
 import java.awt.Composite;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.image.BufferedImage;
 import java.util.List;
 
 /**
  * The {@code ClippableImageRenderer} class is an {@code PartialImageRenderer}
  * which supports rendering of a certain section of a {@link Canvas} object
  * in addition to rendering an entire {@code Canvas} object.
  * 
  * @author coobird
  *
  */
 public final class ClippableImageRenderer 
 	implements ImageRenderer, PartialImageRenderer
 {
 	/**
 	 * Render the canvas.
 	 * @param c				The canvas to render.
 	 */
 	public BufferedImage render(Canvas c)
 	{
 		return render(c, 0, 0, c.getWidth(), c.getHeight());
 	}
 
 	/**
 	 * Render the canvas.
 	 * @param c					The canvas to render.
 	 * @param drawBackground	Whether or not to render a background.
 	 */
 	public BufferedImage render(Canvas c, boolean drawBackground)
 	{
 		return render(c, 0, 0, c.getWidth(), c.getHeight());
 	}
 	
 
 	/**
 	 * 
 	 */
 	public BufferedImage render(
 			Canvas c,
 			int x,
 			int y,
 			int width,
 			int height
 	)
 	{
 		return render(c, x, y, width, height, true);
 	}
 	
 	/**
 	 * TODO write javadoc
 	 * TODO implement this class completely and test.
 	 * renders a part of an canvas
 	 */
 	public BufferedImage render(
 			Canvas c,
 			int x,
 			int y,
 			int width,
 			int height,
 			boolean drawBackground
 	)
 	{
 		if (c == null)
 		{
 			throw new NullPointerException("Canvas not initialized.");
 		}
 		
 		// If the width or height is greater than the width or height of the
 		// source image, set the rendering width/height to source width/height.
 		if (width > c.getWidth())
 		{
 			width = c.getWidth();
 		}
 		if (height > c.getHeight())
 		{
 			height = c.getHeight();
 		}
 		
 		/*
 		 * Produce the new image to use as the target to draw on.
 		 */
 		double zoom = c.getZoom();
 		int nwidth = (int)Math.round(width * zoom);
 		int nheight = (int)Math.round(height * zoom);
 		
 		BufferedImage img = new BufferedImage(
 				nwidth,
 				nheight,
 				ImageLayer.getDefaultType()
 		);
 		
 		Graphics2D g = img.createGraphics();
 		g.scale(zoom, zoom);
 		
 		Composite originalComposite = g.getComposite();
 		
 		if (drawBackground)
 		{
 			drawBackground(img, x, y, width, height, zoom);
 		}
 
 		/*
 		 * Draw subimage of each layer onto the screen.
 		 */
 		List<ImageLayer> layerList = c.getRenderOrder();
 
 		for (ImageLayer layer : layerList)
 		{
 			if (layer == null)
 			{
 				break;
 			}
 			
 			if (!layer.isVisible())
 			{
 				continue;
 			}
 			
 			Composite layerComposite = AlphaComposite.getInstance(
 					layer.getMode().getComposite().getRule(),
 					layer.getAlpha()
 			);
 			
 			g.setComposite(layerComposite);
 			
 			/*
 			 * Get the part of the layer's image to draw to screen, and draw it.
 			 */
 			BufferedImage layerImg = layer.getImage();
 			int subWidth = width;
 			int subHeight = height;
 			
 			if (subWidth > layerImg.getWidth())
 			{
 				subWidth = layerImg.getWidth();
 			}
 			if (subHeight > layerImg.getHeight())
 			{
 				subHeight = layerImg.getHeight();
 			}
 			
 			BufferedImage layerSubImage = layerImg.getSubimage(
 					x,
 					y,
 					subWidth,
 					subHeight
 			);
 			
 			g.drawImage(layerSubImage, layer.getX(), layer.getY(), null);
 		}
 		
 		g.setComposite(originalComposite);
 		
 		if (zoom > 2)
 		{
 			drawPixelGrid(img, x, y, zoom);
 		}
 		
 		g.dispose();
 		
 		return img;
 	}
 	
 	private void drawPixelGrid(BufferedImage img, int x, int y, double zoom)
 	{
 		Graphics g = img.getGraphics();
 		
 		g.setColor(Color.black);
 
 		int width = img.getWidth();
 		int height = img.getHeight();
 		int izoom = (int)Math.round(zoom);
 		
 		for (int i = (-x % izoom); i < x + width; i += izoom)
 		{
 			g.drawLine(i, 0, i, height);
 		}		
 		for (int i = (-y % izoom); i < y + height; i += izoom)
 		{
 			g.drawLine(0, i, width, i);
 		}		
 		
 		g.dispose();
 	}
 	
 	/**
 	 * Draws a checkered white and gray background.
 	 * @param img			The {@code BufferedImage}@to draw the background on.
 	 */
 	private void drawBackground(BufferedImage img, int x, int y, int width, int height, double zoom)
 	{
 		/*
 		 * Doesnt work correctly with zoom.
 		 * Works fine at zoom = 1.
 		 * Else, minor deviation. Probably a rounding problem.
 		 */
 		Graphics g = img.getGraphics();
 		
 		width = (int)Math.round(width * zoom);
 		height = (int)Math.round(height * zoom);
 		
 		x = (int)Math.round(x * zoom);
 		y = (int)Math.round(y * zoom);
 
 		for (int i = (-x % 20); i < x + width + 20; i += 20)
 		{
 			for (int j = (-y % 20); j < y + height + 20; j += 20)
 			{
 				if (((i + x) + (j + y)) % 40 == 0)
 					g.setColor(Color.gray);
 				else
 					g.setColor(Color.white);
 			
 				g.fillRect(i, j, 20, 20);
 			}
 		}
 		
 		g.dispose();
 	}
 }
