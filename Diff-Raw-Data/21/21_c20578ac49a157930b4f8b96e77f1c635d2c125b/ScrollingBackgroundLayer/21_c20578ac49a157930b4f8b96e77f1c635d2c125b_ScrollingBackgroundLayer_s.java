 /**
  * 
  */
 package pbs;
 
 import java.awt.geom.AffineTransform;
 
 import jig.engine.ImageResource;
 import jig.engine.RenderingContext;
 import jig.engine.ViewableLayer;
 import jig.engine.util.Vector2D;
 
 /**
  * @author Skylar Hiebert, Matthew Zinke
  *
  */
 public class ScrollingBackgroundLayer extends PBSQuadLayer<Entity> implements ViewableLayer {
 	private int layerWidth;
 	private int layerHeight;
 	
 	ImageResource image;
 	
 	private Vector2D offset;
 	private Vector2D velocity;
 	private int imgHeight;
 	private int imgWidth;
 	boolean active;
 	
 	/**
 	 * Creates an <code>ImageBackgroundLayer</code> of a specified size, but
 	 * without an actual image (the image must be specified at a later point in
 	 * time). The layer is initially in an <code>inactive</code> state.
 	 * 
 	 * @param layerWidth
 	 *            the width of the layer
 	 * @param layerHeight
 	 *            the height of the layer
 	 * 
 	 * @see #setBackground(ImageResource, boolean)
 	 */
 	public ScrollingBackgroundLayer(final int layerWidth, final int layerHeight) {
 		super(new Vector2D(0,0), new Vector2D(layerWidth, layerHeight));
 		active = false;
 		this.layerHeight = layerHeight;
 		this.layerWidth = layerWidth;
 	}
 
 	/**
 	 * Creates an <code>ImageBackgroundLayer</code> which is immediately
 	 * ready to be viewed. The layer is initially in an <code>active</code> 
 	 * state.
 	 * 
 	 * @param img
 	 *            the image resource to display on this layer
 	 * @param layerWidth
 	 *            the width of the layer
 	 * @param layerHeight
 	 *            the height of the layer
 	 * @param mode
 	 *            one of <code>SCALE_IMAGE</code>, 
 	 *            <code>CENTER_IMAGE</code>, or <code>TILE_IMAGE</code>
 	 *            describing what to do with images that are smaller
 	 *            than the layer itself. 
 	 * 
 	 * @see #setBackground(ImageResource, boolean)
 	 */
 	public ScrollingBackgroundLayer(final ImageResource img,
 			final int layerWidth, final int layerHeight, Vector2D velocity) {
 		super(new Vector2D(0,0), new Vector2D(layerWidth, layerHeight));
 		image = img;
 		imgHeight = img.getHeight();
 		imgWidth = img.getWidth();
 		this.layerHeight = layerHeight;
 		this.layerWidth = layerWidth;
 		this.velocity = velocity;
 		offset = new Vector2D(0,0);
 		active = true;
 	}
 	
 	@Override
	    public void render(RenderingContext rc) {
	    if (!active) {
		return;
	    }
	    
	    AffineTransform at = AffineTransform.getTranslateInstance(0, 0);
	    
	    int startx = (int)((int)tree.min.getX()/imgWidth)*imgWidth;
	    int starty = (int)((int)tree.min.getY()/imgHeight)*imgHeight;
	    
 		for( int i = startx; i <= tree.max.getX(); i += imgWidth ){
 		    for( int j = starty; j <= tree.max.getY(); j += imgHeight ){
 			at.setToTranslation(i, j);
 			//at.scale(scalefactor, scalefactor) //need to adjust imgW, imgH to use this
 			image.render(rc, at);
 		    }
 		}
 	}
 	
 	/**
 	 * Updates the layer on each iteration of the game loop.
 	 * This method is empty, as this image background layer is static.
 	 *
 	 * @param deltaMs
 	 *               ignored
 	 */
 	public void update(final long deltaMs) {		
 		offset = offset.translate(velocity.scale(deltaMs / 100.0));
 	}
 
 	/**
 	 * @return <code>true</code> iff the layer is in the active (visible)
 	 * state.
 	 */
 	public boolean isActive() {
 		return active;
 	}
 
 	/**
 	 * Sets the activation state of this layer.
 	 * 
 	 * @param a <code>true</code> iff the layer should be made active
 	 */
 	public void setActivation(final boolean a) {
 		active = a;
 	}
 	
 	public void setVelocity(Vector2D velocity) {
 		this.velocity = velocity;
 	}
 
 	public void setImageTile(ImageResource img) {
 		this.image = img;
 		imgHeight = img.getHeight();
 		imgWidth = img.getWidth();
 	}
 }
