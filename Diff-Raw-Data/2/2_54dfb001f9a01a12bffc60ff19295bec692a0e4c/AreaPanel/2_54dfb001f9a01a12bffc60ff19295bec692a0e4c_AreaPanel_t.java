 package galapagos;
 
 import java.awt.*;
 import java.awt.image.*;
 import java.util.Observable;
 import java.util.Observer;
 import java.util.Map;
 
 import javax.swing.*;
 
 /**
  * A Swing component that displays a 2D array of pixels.
  * 
  * The pixels can be updated one at a time.
  * 
  * The pixels can be displayed with a magnification factor that can be changed.
  * 
  * To avoid confusion all sizes and coordinates are given in the source coordinate system.
  */
 
 public class AreaPanel extends JPanel implements Observer {
     private int pixelSize;
     private int worldWidth;
     private int worldHeight;
 
     private Image image;
     private int[] imagePixels;
     private int[] pixels;
     private MemoryImageSource source;
     
 	private Map<Behavior, Color> colorMap;
 	private Color background;
 	
     public AreaPanel(Map<Behavior, Color> colorMap) {
     	this.colorMap = colorMap;
     	background = Color.BLACK;
     	pixelSize = 1;
     }
     
     /**
      * Command: color a pixel
      * 
      * @param x x position
      * @param y y position
      * @require 0 <= x < sourceX
      * @require 0 <= y < sourceY
      */
     public void pixel( int x, int y, Color c ) {
         
         pixels[y * worldWidth + x] = c.getRGB();
         pixelImage(x, y, c.getRGB());
     }
     
     private void pixelImage(int x, int y, int color) {
     	int pixelBase = y * pixelSize * pixelSize * worldWidth + x * pixelSize;
         int pixel = 0;
 
         for( int i = 0; i < pixelSize; ++i ) {
             for( int j = 0; j < pixelSize; ++j ) {
                 pixel = pixelBase + j * worldWidth * pixelSize + i;
                 imagePixels[ pixel ] = color;
             }
         }
     }
 
     /**
      * Make sure that changes are drawn.
      */
     public void refresh() {
         source.newPixels( 0, 0, worldWidth * pixelSize, worldHeight * pixelSize );
     }
     
     /**
      * Draws the Biotope to the screen when it changes.
      * @require observableBiotope instanceof Biotope
      * @ensure That all finches are drawn to the screen.
      */
     public void update(Observable observableBiotope, Object arg) {
     	Biotope biotope = (Biotope) observableBiotope;
     	
     	drawBiotope(biotope);
     }
     
     /**
      * Draws the specified biotope to the screen
      * @param biotope The Biotope to draw.
      * @require biotope.world.width == this.getWidth() * zoomFactor
      *  		&& biotope.world.height == this.getHeight() * zoomFactor
      */
     public void drawBiotope(Biotope biotope) {
     	assert biotope.world.width() == this.getWidth() * pixelSize;
     	assert biotope.world.height() == this.getHeight() * pixelSize;
         for(World<GalapagosFinch>.Place place : biotope.world) {
             GalapagosFinch element = place.getElement();
             if(element != null)
                 pixel(place.xPosition(), place.yPosition(), colorByBehavior(element.behavior()));
             else
                 pixel(place.xPosition(), place.yPosition(), background);
         }
         refresh();
     }
     
     /**
      * Get the color associated with the behavior
      * @param behavior The behavior to look-up in the ColorMap
      * @return The color associated with behavior.
      * @require colorMap.containsKey(behavior)
      * @ensure colorByBehavior(behavior).equals(colorMap.get(behavior.toString())
      */
     public Color colorByBehavior(Behavior behavior) {
         Color c = colorMap.get(behavior);
         assert c != null : "Color not defined for this Behavior";
         return c;
     }
     
     /**
      * Change size
      * @param worldWidth the new width of the source
      * @param worldHeight the new height of the source
      */
     public void reset( int worldWidth, int worldHeight ) {
         this.worldWidth = worldWidth;
         this.worldHeight = worldHeight;
         pixels = new int[worldWidth * worldHeight];
         
         reset();
     }
     
     /**
      * Is called when the Panel-size or worldSize has changed.
      * Updates the pixelSize according to the two values and creates a new pixels-array.
      */
     public void reset() {
         int pixelSizeWidth = (int)Math.floor(getWidth() / worldWidth);
         int pixelSizeHeight = (int)Math.floor(getHeight() / worldHeight);
         
         int newPixelSize = Math.max(1, Math.min(pixelSizeWidth, pixelSizeHeight));
         
         if(newPixelSize != pixelSize || source == null) {
         	pixelSize = newPixelSize;
 	        int imageWidth = worldWidth * pixelSize;
 	        int imageHeight = worldHeight * pixelSize;
 	
 	        imagePixels = new int[ imageWidth * imageHeight ];
 	        for(int y = 0; y < worldWidth; y++)
 	        	for(int x = 0; x < worldHeight; x++)
	    			pixelImage(x, y, pixels[y * worldWidth + x]);
 
 	        source = new MemoryImageSource( imageWidth, 
 	        								imageHeight, 
 	        								imagePixels,
 	                                        0,
 	                                        imageWidth );
 	        
 	        source.setAnimated( true );
 	        source.setFullBufferUpdates( true );
 	        if(image != null)
 	            image.flush();
 	        image = createImage( source );        
 	
 	        refresh();
 	        
 	        repaint();
         }
     }
     
     public void setBounds(int x, int y, int width, int height) {
         // if the Panel size is smaller than the world
         int newWidth = (width < worldWidth) ? worldWidth : width;
         int newHeight = (height < worldHeight) ? worldHeight : height;
         
         super.setBounds(x, y, newWidth, newHeight);
         
         reset();
     }
 
     /**
      * Override the Panel paint component. This is not used directly
      */
     public void paintComponent(Graphics g) {
         super.paintComponent(g);
         int x = this.getWidth()/2 - worldWidth * pixelSize/2;
         g.drawImage( image,  x, 0, this );
     }
     
     public int pixelSize() {
     	return pixelSize;
     }
 }
