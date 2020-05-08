 package epsilon.game;
 
 import epsilon.map.entity.HitBox;
 import java.awt.Graphics;
 import java.awt.Image;
 
 /**
  * Class used to store images and manage animation.
  *
  * @author Marius
  */
 public class Sprite {
 
 	/** The images to be drawn for this sprite */
 	private Image[] image;
 
         /** Current position in the animation */
         private int pos;
 
         /** the hitbox of the animation */
         private HitBox[] hitbox;
 
         /** */
         private int offset;
 
 	/**
 	 * Create a new sprite based on an list of urls.
          * Loads the images contained in the urls.
 	 *
 	 * @param urls Array of strings containing urls to the images
 	 */
 	public Sprite(String[] urls) {
 
                 ImageStore s = ImageStore.get();
 
                 pos = 0;
 
                 Image[] images = new Image[urls.length];
                 
                 for (int i=0;i<urls.length;i++) {
                     images[i] = s.get(urls[i]);
                 }
 
 		this.image = images;
 
                 hitbox = new HitBox[]{new HitBox(0, 0, image[0].getWidth(null), image[0].getHeight(null))};
 
                 offset = calculateOffset(hitbox);
 	}
 
 	/**
 	 * Create a new sprite based on an list of urls.
          * Loads the images contained in the urls.
 	 *
 	 * @param urls Array of strings containing urls to the images
          * @param flip Set this as true if you want the images flipped over tye y axis
 	 */
 	public Sprite(String[] urls, boolean flip, HitBox[] h) {
 
             ImageStore s = ImageStore.get();
 
             pos = 0;
 
             Image[] images = new Image[urls.length];
 
             for (int i=0;i<urls.length;i++) {
                 if (flip) {
                     images[i] = s.getFlipped(urls[i]);
                 } else {
                     images[i] = s.get(urls[i]);
                 }
             }
 
             this.image = images;
 
             if (flip) {
                 hitbox = flipHitBox(h);
             } else {
                 hitbox = h;
             }
 
             offset = calculateOffset(hitbox);

            System.out.println("Offset: "+offset);
 	}
 
 	/**
 	 * Get the width of the drawn sprite
 	 *
 	 * @return The width in pixels of this sprite
 	 */
 	public int getWidth() {
 		return image[0].getWidth(null);
 	}
 
 	/**
 	 * Get the height of the drawn sprite
 	 *
 	 * @return The height in pixels of this sprite
 	 */
 	public int getHeight() {
 		return image[0].getHeight(null);
 	}
 
 	/**
 	 * Draw the sprite onto the graphics context provided
 	 *
 	 * @param g The graphics context on which to draw the sprite
 	 * @param x The x location at which to draw the sprite
 	 * @param y The y location at which to draw the sprite
 	 */
 	public synchronized void draw(Graphics g,int x,int y) {
             g.drawImage(image[pos],x,y,null);
             //g.drawRect(x, y, getWidth(), getHeight());
 	}
 
         /**
          * Go to the next image of the sprite for rendering
          */
         public synchronized void nextImage() {
             if (pos < image.length-1) {
                 pos++;
             } else {
                 pos = 0;
             }
         }
 
         /**
          *  Reset the sprite to the first image
          */
         public void resetImage() {
             pos = 0;
         }
 
         /**
          * Gets the hitbox this sprite uses
          */
         public HitBox[] getHitBox() {
             return hitbox;
         }
 
         private HitBox[] flipHitBox(HitBox[] hitbox) {
             HitBox[] result = new HitBox[hitbox.length];
             for (int i=0;i<hitbox.length;i++) {
                 result[i] = new HitBox(getWidth() - hitbox[i].getOffsetX() - hitbox[i].getWidth(), hitbox[i].getOffsetY(), hitbox[i].getWidth(), hitbox[i].getHeight());
             }
             return result;
         }
 
         private int calculateOffset(HitBox[] hitbox) {
             int left = 800;
             int right = 0;
 
             for (int i=0; i<hitbox.length; i++) {
                 if (hitbox[i].getOffsetX() < left) {
                     left = hitbox[i].getOffsetX();
                 }
                 if (hitbox[i].getOffsetX() +  hitbox[i].getWidth() > right) {
                     right = hitbox[i].getOffsetX() +  hitbox[i].getWidth();
                 }
             }
             return ((right + left)/2)-getWidth()/2;
         }
 
         public int getOffset() {
             return offset;
         }
 }
