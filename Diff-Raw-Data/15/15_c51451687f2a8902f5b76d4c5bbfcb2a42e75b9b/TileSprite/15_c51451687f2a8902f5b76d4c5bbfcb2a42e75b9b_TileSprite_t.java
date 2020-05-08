 package rajola.pipeline.sprites;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.newdawn.slick.Image;
 
 public class TileSprite {
 
 	private int height;
 	private int width;
 	private List<Image> frames;
 	private int currentFrame;
 	private int animationSpeed;
 	
 	public TileSprite(Image image) {
 		frames = new ArrayList<Image>();
 		this.width = image.getWidth();
 		this.height = image.getHeight();
 		frames.add(image);
 		this.animationSpeed = 0;
 	}
 	
 	/**
 	 * @param animationSpeed the animation speed in milliseconds
 	 */
 	public TileSprite(int animationSpeed) {
 		frames = new ArrayList<Image>();
 		this.animationSpeed = animationSpeed;
 	}
 	
 	/**
 	 * @param height height of the sprite
 	 * @param width width of the sprite
 	 * @param animationSpeed animationSpeed of sprite in milliseconds
 	 * @param image list of images to be included in animation loop
 	 */
 	public TileSprite(int animationSpeed , Image[][] image){
		frames = new ArrayList<Image>();
 		for(int y = 0 ; y < image.length ; y++) {
 			for(int x = 0 ; x < image.length ; x++) {
 				frames.add(image[x][y]);
 			}
 		}
 		this.width = image[0][0].getWidth();
 		this.height = image[0][0].getHeight();
 		this.animationSpeed = animationSpeed;
 	}
 	
 	
 	/**
 	 * @param image 2D array of images
 	 * @return list of sprites
 	 * Not to be used for animation
 	 */
 	public static List<TileSprite> autoConstructSprites(Image[][] image) {
 		List<TileSprite> sprites = new ArrayList<TileSprite>();	
 		for(int y = 0 ; y < image.length ; y++) {
 			for(int x = 0 ; x < image.length ; x++) {
 				sprites.add(new TileSprite(image[x][y]));
 			}
 		}
 		return sprites;
 	}
 	
 	
 	public void drawSprite(int x , int y) {
 		this.frames.get(currentFrame).draw(x , y);
 	}
 	
 	/**
 	 * @return the height
 	 */
 	public int getHeight() {
 		return height;
 	}
 
 	/**
 	 * @param height the height to set
 	 */
 	public void setHeight(int height) {
 		this.height = height;
 	}
 
 	/**
 	 * @return the width
 	 */
 	public int getWidth() {
 		return width;
 	}
 
 	/**
 	 * @param width the width to set
 	 */
 	public void setWidth(int width) {
 		this.width = width;
 	}
 
 	/**
 	 * @return the frames List
 	 */
 	public List<Image> getFrames() {
 		return frames;
 	}
 
 	/**
 	 * @param frame adds a frame
 	 */
 	public void addFrame(Image frame) {
 		this.frames.add(frame);
 	}
 	
 	/**
 	 * @param frame removes a frame
 	 */
 	public void removeFrame(Image frame) {
 		this.frames.remove(frame);
 	}
 	
 	/**
 	 * Removes all frames
 	 */
 	public void removeAllFrame() {
 		this.frames.clear();
 		currentFrame = 0;
 	}
 
 	/**
 	 * @return the currentFrame
 	 */
 	public int getCurrentFrame() {
 		return currentFrame;
 	}
 
 	/**
 	 * @param currentFrame the currentFrame to set
 	 */
 	public void setCurrentFrame(int currentFrame) {
 		this.currentFrame = currentFrame;
 	}
 	
 	/*
 	 * increase the current frame by one
 	 */
 	public void incrementFrame() {
 		this.currentFrame++;
 	}
 
 	/**
 	 * @return the animationSpeed
 	 */
 	public int getAnimationSpeed() {
 		return animationSpeed;
 	}
 
 	/**
 	 * @param animationSpeed the animationSpeed to set
 	 */
 	public void setAnimationSpeed(int animationSpeed) {
 		this.animationSpeed = animationSpeed;
 	}
 	
 }
