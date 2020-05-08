 package galapagos.ui;
 
 import galapagos.biotope.*;
 
 import java.awt.*;
 import java.awt.image.*;
 import java.util.*;
 
 import javax.swing.*;
 
 /**
  * A Swing component that displays a 2D array of pixels. 
  * The pixels can be updated one at a time.
  * 
  * The component will choose a pixel size/zoom factor based on
  * the size of the World its presenting and how much space on
  * the screen the control is assigned. 
  */
 public class AreaPanel extends JPanel implements Observer {
     private int pixelSize;
     private int worldWidth;
     private int worldHeight;
     private int offsetX, offsetY;
 
     private Image image;
     private int[] imagePixels;
     private int[] pixels;
     private MemoryImageSource source;
     
 	private Map<Behavior, Color> colorMap;
 	private Color background;
 	
 	/**
 	 * Create a new AreaPanel with BLACK background color.
 	 * @param colorMap The colors assigned to the different kind of Behaviors.
 	 */
     public AreaPanel(Map<Behavior, Color> colorMap) {
     	this.colorMap = colorMap;
     	background = Color.BLACK;
     }
     
     /**
      * Color a pixel using the coordinates of in the World.
      * The pixel won't be drawn to the screen before refresh() is called.
      * @param x The x position in the World.
      * @param y The y position in the World.
      * @require 0 <= x < worldWidth
      *       && 0 <= y < worldHeight
      */
     public void pixel( int x, int y, Color c ) {
     	assert (0 <= x && x < worldWidth
     		 && 0 <= y && y < worldHeight) :
     	"The coordinate must be inside the world known to AreaPanel.";
        
     	//Save the color in the pixels array at (x, y)
     	//is used to create the new pixelImage when the panel is resized.
         pixels[y * worldWidth + x] = c.getRGB();
         pixelImage(x, y, c.getRGB());
     }
     
     /**
      * Colors a pixelSize*pixelSize square on the pixel-array associated with
      * the MemoryImageSource at the specified coordinates (given in World-
      * coordinates).
      * The pixel won't be drawn to the screen before refresh() is called.
      * @param x The x-coordinate in the world.
      * @param y The y-coordinate in the world.
      * @param color The color of the pixel
      */
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
      * Makes sure that changes are drawn.
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
     	assert (observableBiotope instanceof Biotope) :
     		"Can only observe Biotope's";
     	Biotope biotope = (Biotope) observableBiotope;
     	
     	drawBiotope(biotope);
     }
     
     /**
      * Draws the specified biotope to the screen
      * @param biotope The Biotope to draw.
      */
     public void drawBiotope(Biotope biotope) {
         for (World<GalapagosFinch>.Place place : biotope) {
             GalapagosFinch element = place.getElement();
             if(element != null)
                 pixel(place.xPosition(), place.yPosition(), colorByBehavior(element.behavior()));
             else
                 pixel(place.xPosition(), place.yPosition(), background);
         }
         refresh();
     }
     
     /**
      * Get the color associated with a behavior.
      * @param behavior The behavior to look-up in the colorMap
      * @return The color associated with behavior.
      * @require colorMap.containsKey(behavior)
      * @ensure colorByBehavior(behavior).equals(colorMap.get(behavior.toString())
      */
     private Color colorByBehavior(Behavior behavior) {
         Color c = colorMap.get(behavior);
         assert c != null : "Color not defined for this Behavior (" + behavior.toString() + ")";
         return c;
     }
     
     /**
      * Change the size of the world.
      * 
      * @param worldWidth the new width of the source
      * @param worldHeight the new height of the source
      */
    public void changeWorld( int worldWidth, int worldHeight ) {
    	boolean worldSizeChanged = (this.worldWidth != worldWidth)
    						    || (this.worldHeight != worldHeight);
         this.worldWidth = worldWidth;
         this.worldHeight = worldHeight;
         pixels = new int[worldWidth * worldHeight];

        reset(worldSizeChanged);
     }
     
     /**
      * Is called when the Panel-size or worldSize has changed.
      * Updates the pixelSize according to the two values and creates a new pixels-array.
     * @param worldSizeChanged Indicate whether the world has changed or not.
      */
    private void reset(boolean worldSizeChanged) {
         int pixelSizeWidth = (int)Math.floor(getWidth() / worldWidth);
         int pixelSizeHeight = (int)Math.floor(getHeight() / worldHeight);
         
         int newPixelSize = Math.max(1, Math.min(pixelSizeWidth, pixelSizeHeight));
         
         
        if(worldSizeChanged || newPixelSize != pixelSize || source == null) {
         	pixelSize = newPixelSize;
         	
         	//calculate the new size of the image
 	        int imageWidth = worldWidth * pixelSize;
 	        int imageHeight = worldHeight * pixelSize;
 	        
 	        //create a new pixel-array for the image
 	        imagePixels = new int[ imageWidth * imageHeight ];
 	        //color it with the colors from the pixels array
 	        for(int y = 0; y < worldHeight; y++)
 	        	for(int x = 0; x < worldWidth; x++)
 	    			pixelImage(x, y, pixels[y * worldWidth + x]);
 
 	        //create the new image
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
         
         // calculate where the upper left corner of the imge should be
         // when it is centered.
         offsetX = (this.getWidth() - worldWidth * pixelSize) / 2;
         offsetY = (this.getHeight() - worldHeight * pixelSize) / 2;
     }
     
     /**
      * Change the size of the component.
      * The component can never be smaller than the size of the world.
      * This ensures that zoomFactor >= 1.
      */
     public void setBounds(int x, int y, int width, int height) {
         // if the component size is smaller than the world
     	// use the size of the world as the components size        
         super.setBounds(x, y, Math.max(width, worldWidth)
         				    , Math.max(height, worldHeight));
         
         //recalculate the pixelSize
        reset(false);
     }
 
     /**
      * Override the Panel paint component. This is not used directly
      */
     public void paintComponent(Graphics g) {
         super.paintComponent(g);
 
         g.drawImage( image,  offsetX, offsetY, this );
     }
     
     /**
      * The size of pixels / zoomFactor of the component.
      */
     public int pixelSize() {
     	return pixelSize;
     }
     
     /**
      * The x-coordinate to the upper left corner of the image 
      */
     public int offsetX() {
     	return offsetX;
     }
     
     /**
      * The y-coordinate to the upper left corner of the image 
      */
     public int offsetY() {
     	return offsetY;
     }
 }
