 package com.punchline.javalib.entities.components.render;
 
 import com.badlogic.gdx.files.FileHandle;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.math.Vector2;
 import com.punchline.javalib.entities.ComponentManager;
 
 /**
  * Component wrapper for a LibGDX {@link com.badlogic.gdx.graphics.g2d.Animation Animation}.
  * @author Nathaniel
  *
  */
 public class Animation implements Renderable {
 
 	private com.badlogic.gdx.graphics.g2d.Animation animation;
 	private float stateTime = 0f;
 	private boolean looping = true;
 	
 	private Vector2 position;
 	private float rotation = 0f;
 	private float scale = 1f;
 	private Vector2 origin;
 	
 	/**
 	 * Constructs an Animation.
 	 * @param texture The Animation's texture.
 	 * @param animationRegion The region on the texture which contains the complete animation.
 	 * @param frameCols The amount of columns in the animation.
 	 * @param frameRows The amount of rows in the animation.
 	 * @param frameDuration The duration of each frame, in seconds.
 	 */
 	public Animation(Texture texture, TextureRegion animationRegion, int frameCols, int frameRows, float frameDuration) {
 		
 		TextureRegion[] regions = new TextureRegion[frameCols * frameRows];
 		
 		int frameWidth = animationRegion.getRegionWidth() / frameCols;
 		int frameHeight = animationRegion.getRegionHeight() / frameRows;
 		
 		int i = 0;
 		
 		for (int y = 0; y < frameRows; y++) {
 			
 			int yCoord = animationRegion.getRegionY() + y * frameHeight;
 			
 			for (int x = 0; x < frameCols; x++) {
 				
 				int xCoord = animationRegion.getRegionX() + x * frameWidth;
 				
 				TextureRegion region = new TextureRegion(texture, xCoord, yCoord, frameWidth, frameHeight);
 				
 				regions[i++] = region;
 				
 			}
 		}
 		
 		animation = new com.badlogic.gdx.graphics.g2d.Animation(frameDuration, regions);
 		
 		setOrigin(new Vector2(frameWidth / 2, frameHeight / 2));
 		
 	}
 	
 	/**
 	 * Constructs an Animation.
 	 * @param texture The Animation's texture.
 	 * @param frameCols The amount of columns in the animation.
 	 * @param frameRows The amount of rows in the animation.
 	 * @param frameDuration The duration of each frame, in seconds.
 	 */
 	public Animation(Texture texture, int frameCols, int frameRows, float frameDuration) {
 		
 		TextureRegion[] regions = new TextureRegion[frameCols * frameRows];
 		
 		TextureRegion[][] tmp = new TextureRegion[frameRows][frameCols];
 		
 		int frameWidth = texture.getWidth() / frameCols;
 		int frameHeight = texture.getHeight() / frameRows;
 		tmp = TextureRegion.split(texture, frameWidth, frameHeight);
 		
 		int i = 0;
 		
 		for (int y = 0; y < frameRows; y++) {
 			for (int x = 0; x < frameCols; x++) {
 				regions[i++] = tmp[y][x];
 			}
 		}
 		
 		animation = new com.badlogic.gdx.graphics.g2d.Animation(frameDuration, regions);
 		
 		setOrigin(new Vector2(frameWidth / 2, frameHeight / 2));
 		
 	}
 	
 	/**
 	 * Constructs an Animation.
 	 * @param textureHandle A FileHandle pointing to the texture.
 	 * @param frameCols The number of columns in the animation.
 	 * @param frameRows The number of rows in the animation.
 	 * @param frameDuration The duration of each frame, in seconds.
 	 */
 	public Animation(FileHandle textureHandle, int frameCols, int frameRows, float frameDuration) {
 		this(new Texture(textureHandle), frameCols, frameRows, frameDuration);
 	}
 	
 	/**
 	 * Sets how long this Animation has been running.
 	 * @param stateTime How long this Animation has been running.
 	 */
 	public void setStateTime(float stateTime) {
 		this.stateTime = stateTime;
 	}
 	
 	/**
 	 * Sets whether or not this Animation is looping.
 	 * @param looping Whether this Animation is looping.
 	 */
 	public void setLooping(boolean looping) {
 		this.looping = looping;
 	}
 	
 	/**
 	 * @param deltaSeconds The amount of seconds since the last time the current frame was checked.
 	 * @return The current frame.
 	 */
 	public TextureRegion getCurrentFrame(float deltaSeconds) {
 		return animation.getKeyFrame(stateTime += deltaSeconds, looping);
 	}
 
 	@Override
 	public void setPosition(Vector2 position) {
 		TextureRegion region = getCurrentFrame(0f);
		this.position = position.sub(new Vector2(region.getRegionWidth() / 2, region.getRegionHeight() / 2));
 	}
 
 	@Override
 	public void setRotation(float degrees) {
 		this.rotation = degrees;
 	}
 	
 	@Override
 	public void setScale(float scale) {
 		this.scale = scale;
 	}
 
 	@Override
 	public void setOrigin(Vector2 origin) {
 		this.origin = origin;
 	}
 	
 	@Override
 	public void draw(SpriteBatch spriteBatch, float deltaSeconds) {
 		TextureRegion region = getCurrentFrame(deltaSeconds);
 		spriteBatch.draw(region, position.x, position.y, origin.x, origin.y, 
 				region.getRegionWidth(), region.getRegionHeight(), scale, scale, rotation);
 	}
 
 	@Override
 	public void onAdd(ComponentManager container) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void onRemove(ComponentManager container) {
 		// TODO Auto-generated method stub
 		
 	}
 	
 }
