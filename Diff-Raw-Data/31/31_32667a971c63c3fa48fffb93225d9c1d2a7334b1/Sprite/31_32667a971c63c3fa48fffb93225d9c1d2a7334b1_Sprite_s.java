 // AtlantisEngine.java - Copyright (C) Yannick Comte.
 // This file is subject to the terms and conditions defined in
 // file 'LICENSE', which is part of this source code package.
 package atlantis.engine.graphics;
 
 import atlantis.engine.Atlantis;
 import atlantis.framework.GameTime;
 import atlantis.framework.Rectangle;
 import atlantis.framework.Vector2;
 import atlantis.framework.content.ContentManager;
 import atlantis.framework.graphics.SpriteBatch;
 import atlantis.framework.graphics.SpriteEffect;
 import atlantis.framework.graphics.Texture2D;
 
 /**
  * A sprite is a graphical object which can be a simple image or a more complex animated entity.
  * @author Yannick
  */
 public class Sprite extends BaseEntity implements ICollidable2 {
 	protected Rectangle rectangle;
 	protected Vector2 position;
 	protected Texture2D texture;
 	protected String textureName;
 	protected Rectangle sourceRectangle;
 	protected float rotation;
 	protected int spriteEffect;
 	protected float layerDepth;
 	protected Vector2 direction;
 	protected Vector2 lastPosition;
 	protected Vector2 lastDistance;
 	protected Physics2 physics;
 	protected Rectangle viewport;
 	protected boolean forceInsideScreen;
 	protected boolean allowAcrossScreen;
 	protected SpriteAnimator spriteAnimator;
 	protected boolean hasAnimation; // Todo : move it in SpriteAnimator
 	protected ISpriteMouseListener spriteMouseListener;
 	protected boolean hovered;
 	
 	public Sprite() {
 		this.rectangle = new Rectangle();
 		this.position = new Vector2();
 		this.texture = null;
 		this.textureName = "";
 		this.sourceRectangle = new Rectangle();
 		this.rotation = 0.0f;
 		this.spriteEffect = SpriteEffect.None;
 		this.layerDepth = 1.0f;
 		this.direction = new Vector2();
 		this.lastPosition = new Vector2();
 		this.lastDistance = new Vector2();
 		this.physics = new Physics2();
 		this.viewport = new Rectangle(0, 0, Atlantis.width, Atlantis.height);
 		this.forceInsideScreen = false;
 		this.allowAcrossScreen = false;
 		this.spriteAnimator = new SpriteAnimator();
 		this.hasAnimation = false;
 		this.spriteMouseListener = null;
 		this.hovered = false;
 	}
 	
 	public Sprite(String textureName) {
 		this();
 		this.textureName = textureName;
 	}
 	
 	public void loadContent(ContentManager content) {
 		if (this.textureName != "" && this.assetLoaded == false) {
 			this.texture = content.loadTexture(this.textureName);
 			this.rectangle.width = this.texture.getWidth();
 			this.rectangle.height = this.texture.getHeight();
 			this.assetLoaded = true;
 		}
 	}
 	
 	public void update(GameTime gameTime) { 
 		if (this.enabled) {
             // Determine the last distance and direction
             this.lastDistance.x = (this.position.x - this.lastPosition.x);
             this.lastDistance.y = (this.position.y - this.lastPosition.y);   
 
             // Determine the last position
             this.lastPosition.x = (int)this.position.x;
             this.lastPosition.y = (int)this.position.y;
 
             // Update physics
             this.physics.applyVelocity(this);
             
 
             // Update the rectangle position
             this.rectangle.x = (int)this.position.x;
             this.rectangle.y = (int)this.position.y;
 
             // Update animation
             if (this.hasAnimation) {
                 this.spriteAnimator.update(gameTime);
                 
                 if (this.lastDistance.x == 0 && this.lastDistance.y == 0 && this.spriteAnimator.getCurrentAnimationName() != "") {
                 	this.sourceRectangle = this.spriteAnimator.getCurrentAnimation().getRectangle(0);
                 }
             }
         }
 	}
 	
 	/**
 	 * Called when the update process is done and before the draw method.
 	 * Some operations are done in this method such as position, direction updates and collides detection. 
 	 */
 	protected void postUpdate() {
 		if (this.enabled) {
             this.direction.x = this.position.x - this.lastPosition.x;
             this.direction.y = this.position.y - this.lastPosition.y;
 
             // Force the sprite to stay inside screen
             if (this.forceInsideScreen) {
             	boolean stopVelocity = false;
                 if (this.position.x < this.viewport.x) {
                     this.position.x = this.viewport.x;
                     stopVelocity = true;
                 }
                 else if (this.rectangle.getRight() > this.viewport.width) {
                     this.position.x = this.viewport.width - this.rectangle.width;
                     stopVelocity = true;
                 }
 
                 if (this.position.y < this.viewport.y) {
                     this.position.y = this.viewport.y;
                     stopVelocity = true;
                 }
                 else if (this.rectangle.getBottom() > this.viewport.height) {
                     this.position.y = this.viewport.height - this.rectangle.height;
                     stopVelocity = true;
                 }
                 
                 if (stopVelocity) {
                 	this.physics.velocity.multiply(0.0f);
                 }
             }
 
             // The sprite move throw the screen
             else if (this.allowAcrossScreen) {
                 if (this.rectangle.getRight() < this.viewport.x) {
                     this.position.x = this.viewport.width;
                 }
                 else if (this.position.x > this.viewport.width) {
                     this.position.x = this.viewport.x;
                 }
 
                 if (this.rectangle.getBottom() < this.viewport.y) { 
                     this.position.y = this.viewport.height;
                 }
                 else if (this.position.y > this.viewport.height) {
                     this.position.y = this.viewport.y;
                 }
             }
             
             if (this.spriteMouseListener != null) {
             	if (this.rectangle.contains(Atlantis.mouse.getPosition())) {
             		this.spriteMouseListener.onMouseOver();
             		this.hovered = true;
             		
             		int button = -1;
             		button = Atlantis.mouse.click(0) ? 0 : button;
             		button = Atlantis.mouse.click(1) ? 1 : button;
             		button = Atlantis.mouse.click(2) ? 2 : button;
             		
             		if (button > -1) {
             			this.spriteMouseListener.onMouseClick(button);
             		}
             		
             		int jcButton = -1;
             		jcButton = Atlantis.mouse.justClicked(0) ? 0 : jcButton;
             		jcButton = Atlantis.mouse.justClicked(1) ? 1 : jcButton;
             		jcButton = Atlantis.mouse.justClicked(2) ? 2 : jcButton;
             		
             		if (jcButton > -1) {
             			this.spriteMouseListener.onMouseJustClicked(jcButton);
             		}
             	}
             	else if (this.hovered) {
                 	this.hovered = false;
                 	this.spriteMouseListener.onMouseOut();
                 }
             }
 		}
 	}
 		
 	@Override
 	public void draw(GameTime gameTime, SpriteBatch spriteBatch) {
 		if (this.enabled) {
 			this.postUpdate();
 		}
 		
 		if (this.visible && this.assetLoaded) {
             if (this.hasAnimation) {
                spriteBatch.draw(this.texture, this.rectangle, this.sourceRectangle, 0, 0);
             }
             else {
             	spriteBatch.draw(this.texture, this.rectangle);
             }
         }
 	}
 	
 	// ---
 	// --- Animation methods
 	// ---
 	
 	/**
 	 * Initialize the first step of the animation process. You must call this method before add any animation.
 	 * @param width The width of an animation (Can be recomputed later).
 	 * @param height The height of an animation (Can be recomputed later).
 	 */
 	public void prepareAnimation(int width, int height) {
 		this.hasAnimation = true;
 		this.spriteAnimator.initialize(width, height, this.texture.getWidth(), this.texture.getHeight());
 		this.rectangle.width = width;
 		this.rectangle.height = height;
 	}
 	
 	/**
 	 * Add an animation which is a set of frame index on the sprite sheet.
 	 * @param name The name of the animation.
 	 * @param framesIndex An array of index.
 	 * @param frameRate The desired frame rate for this animation.
 	 */
 	public void addAnimation(String name, int[] framesIndex, int frameRate) {
 		this.spriteAnimator.add(name, framesIndex, frameRate);
 		this.sourceRectangle = this.spriteAnimator.getAnimations().get(name).getRectangle(0);
 	}
 	
 	/**
 	 * Play an animation by its name.
 	 * @param name The name of the animation to play.
 	 */
 	public void play(String name) {
 		this.sourceRectangle = this.spriteAnimator.play(name);
 	}
 	
 	// ---
 	// --- Moving methods
 	// ---
 	
 	/**
 	 * Move the sprite.
 	 * @param x Value that will be added to the current position.
 	 * @param y Value that will be added to the current position.
 	 */
 	public void move(float x, float y) {
 		this.move((int)x, (int)y);
 	}
 	
 	/**
 	 * Move the sprite.
 	 * @param x Value that will be added to the current position.
 	 * @param y Value that will be added to the current position.
 	 */
 	public void move(int x, int y) {
 		this.setPosition(this.position.x + x, this.position.y + y);
 	}
 	
 	/**
 	 * Move the sprite.
 	 * @param moveToVector Vector that will be added to the current position.
 	 */
 	public void move(Vector2 moveToVector) {
 		this.setPosition(Vector2.add(this.position, moveToVector));
 	}
 	
 	// ---
 	// --- Getters and setters
 	// ---
 	
 	/**
 	 * Gets the X position on screen.
 	 * @return Return the X position.
 	 */
 	public float getX() {
 		return this.position.x;
 	}
 	
 	/**
 	 * Gets the Y position on screen.
 	 * @return Return the Y position.
 	 */
 	public float getY() {
 		return this.position.y;
 	}
 	
 	/**
 	 * Gets the width.
 	 * @return Return width.
 	 */
 	public int getWidth() {
 		return this.rectangle.width;
 	}
 	
 	/**
 	 * Gets the height.
 	 * @return Return height
 	 */
 	public int getHeight() {
 		return this.rectangle.height;
 	}
 	
 	/**
 	 * Gets the bounding rectangle of the sprite
 	 * @return Return the rectangle.
 	 */
 	public Rectangle getBoundingRectangle() {
 		return this.rectangle;
 	}
 	
 	/**
 	 * Gets the actual position of the sprite.
 	 * @return Return a Vector2 with the coordinates of the sprite on the screen.
 	 */
 	public Vector2 getPosition() {
 		return this.position;
 	}
 
 	/**
 	 * Gets the actual direction of the Sprite. 
 	 * X: -1 = left / 1 = right
 	 * Y: -1 = up / 1 = down
 	 * @return
 	 */
 	public Vector2 getDirection() {
 		return direction;
 	}
 
 	/**
 	 * Gets the previous position of the sprite.
 	 * @return Return the previous coordinates of the sprite.
 	 */
 	public Vector2 getLastPosition() {
 		return lastPosition;
 	}
 	
 	/**
 	 * Return the previous distance done by the sprite.
 	 * @return Return the previous distance.
 	 */
 	public Vector2 getLastDistance() {
 		return lastDistance;
 	}
 
 	/**
 	 * Gets the default view port used by the sprite.
 	 * @return A rectangle of the view port.
 	 */
 	public Rectangle getViewport() {
 		return viewport;
 	}
 	
 	/**
 	 * Gets the texture.
 	 * @return Return a Texture2D.
 	 */
 	public Texture2D getTexture() {
 		return this.texture;
 	}
 	
 	/**
 	 * Gets the physics configuration applied to the sprite.
 	 * @return Return the physics configuration of the object.
 	 */
 	public Physics2 getPhysics() {
 		return this.physics;
 	}
 	
 	/**
 	 * Set the position of the entity on the screen
 	 * @param x The X coordinate
 	 * @param y The Y coordinate
 	 */
 	public void setPosition(int x, int y) {
 		this.setPosition((float)x, (float)y);
 	}
 	
 	/**
 	 * Set the position of the entity on the screen
 	 * @param x The X coordinate
 	 * @param y The Y coordinate
 	 */
 	public void setPosition (float x, float y) {
 		this.position.set(x, y);
 		this.rectangle.setPosition((int)x, (int)y);
 	}
 	
 	/**
 	 * Set the position of the entity on the screen
 	 * @param position A new position.
 	 */
 	public void setPosition(Vector2 position) {
 		this.position = position;
 		this.rectangle.setPosition((int) position.x, (int) position.y);
 	}
 	
 	/**
 	 * Change the size of the sprite
 	 * @param width
 	 * @param height
 	 */
 	public void setSize(int width, int height) {
 		this.rectangle.setSize(width, height);
 	}
 	
 	/**
 	 * Sets the value of X coordinate of the entity
 	 * @param x New X value on screen.
 	 */
 	public void setX(float x) {
 		this.position.x = x;
 		this.rectangle.x = (int)x;
 	}
 	
 	/**
 	 * Sets the value of Y coordinate of the entity
 	 * @param y New Y value on screen.
 	 */
 	public void setY(float y) {
 		this.position.y = y;
 		this.rectangle.y = (int)y;
 	}
 	
 	/**
 	 * Sets the width of the sprite.
 	 * @param width Desired width.
 	 */
 	public void setWidth(int width) {
 		this.rectangle.width = width;
 	}
 	
 	/**
 	 * Sets the height of the sprite.
 	 * @param height Desired height.
 	 */
 	public void setHeight(int height) {
 		this.rectangle.height = height;
 	}
 
 	
 	/**
 	 * Determine the viewport of the sprite.
 	 * @param viewport A rectangle where the sprite can move
 	 */
 	public void setViewport(Rectangle viewport) {
 		this.viewport = viewport;
 	}
 	
 	/**
 	 * Determine the view port of the sprite.
 	 * @param x Start position on X.
 	 * @param y Start position on Y.
 	 * @param width Width
 	 * @param height Height
 	 */
 	public void setViewport(int x, int y, int width, int height) {
 		this.viewport.x = x;
 		this.viewport.y = y;
 		this.viewport.width = width;
 		this.viewport.height = height;
 	}
 	
 	/**
 	 * Sets the texture used for this sprite.
 	 * @param texture A texture to use.
 	 */
 	public void setTexture(Texture2D texture) {
 		this.texture = texture;
 		this.rectangle.setSize(texture.getWidth(), texture.getHeight());
 	}
 
 	/**
 	 * Force the sprite to stay inside its view port.
 	 * @param insideScreen Sets to true to force.
 	 */
 	public void forceInsideScreen(boolean insideScreen) {
 		this.forceInsideScreen = insideScreen;
 	}
 
 	/**
 	 * Allow the sprite across the screen.
 	 * @param acrossScreen Sets to true to enable.
 	 */
 	public void allowAcrossScreen(boolean acrossScreen) {
 		this.allowAcrossScreen = acrossScreen;
 	}
 }
