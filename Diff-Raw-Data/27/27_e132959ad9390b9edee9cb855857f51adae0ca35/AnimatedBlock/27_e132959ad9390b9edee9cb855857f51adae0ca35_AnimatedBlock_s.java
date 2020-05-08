 package org.racenet.framework;
 
 import javax.microedition.khronos.opengles.GL10;
 
 /**
  * Class which can render an animated rectangle
  * 
  * @author soh#zolex
  *
  */
 public class AnimatedBlock extends GameObject {
 
	protected GLGame game;
 	protected GL10 gl;
 	protected FileIO fileIO;
 	public float animTime = 0;
 	public GLVertices vertices = null;
 	public Animation anims[];
 	public int numAnims;
 	public int activeAnimId = 0;
 	float texScaleWidth = 0.05f;
 	float texScaleHeight = 0.05f;
 	
 	/**
 	 * Constructor 
 	 * 
 	 * @param GLGame game
 	 * @param Vector2 ... vertices
 	 */
 	public AnimatedBlock(GLGame game, Vector2 ... vertices) {
 		
 		super(vertices);
		this.game = game;
 		this.gl = game.getGLGraphics().getGL();
 		this.fileIO = game.getFileIO();
 	}
 	
 	/**
 	 * Set a list of animations for the ractangle
 	 * 
 	 * @param String[] ... animations
 	 */
 	protected void setAnimations(AnimationPreset ... animations) {
 		
 		this.numAnims = animations.length;
 		this.anims = new Animation[this.numAnims];
 		for (int i = 0; i < this.numAnims; i++) {
 			
 			this.setupKeyFrames(animations[i].frameDuration, i, animations[i].keyFrames);
 		}
 	}
 	
 	/**
 	 * Setup the keyframes by loading the textures for the animations
 	 * 
 	 * @param float frameDuration
 	 * @param int animId
 	 * @param String ... keyFrames
 	 */
 	public void setupKeyFrames(float frameDuration, int animId, String ... keyFrames) {
 		
 		GLTexture[] frames = new GLTexture[keyFrames.length];
 		for (int i = 0; i < keyFrames.length; i++) {
 			
 			frames[i] = new GLTexture(this.gl, this.fileIO, keyFrames[i]);
 		}
 		
 		this.anims[animId] = new Animation(frameDuration, frames);
 	}
 	
 	/**
 	 * Prepare the vertices for the openGL renderer
 	 */
 	protected void setupVertices() {
 		
 		GLTexture firstFrame = this.anims[0].getKeyFrame(0); // TODO: choose proper frame
 		this.vertices = new GLVertices(this.gl, 4, 6 , false, true);
 		this.vertices.setVertices(new float[] {
 				0,			0,	  			0, this.height / (firstFrame.height * this.texScaleHeight),
 				this.width,	0,				this.width / (firstFrame.width * this.texScaleWidth), this.height / (firstFrame.height * this.texScaleHeight),
 				this.width,	this.height,	this.width / (firstFrame.width * this.texScaleWidth), 0,
 				0,			this.height,	0, 0 }, 0, 16);
 		this.vertices.setIndices(new short[] {0, 1, 2, 0, 2, 3}, 0, 6);
 	}
 	
 	/**
 	 * Draw the rectangle with the current keyframe
 	 */
 	public void draw() {
 		
 		this.gl.glPushMatrix();
 		this.gl.glTranslatef(this.getPosition().x, this.getPosition().y, 0);
 		this.anims[this.activeAnimId].getKeyFrame(this.animTime).bind();
 		this.vertices.bind();
 		this.vertices.draw(GL10.GL_TRIANGLES, 0, 6);
 		this.vertices.unbind();
 		this.gl.glPopMatrix();
 	}
 	
 	/**
 	 * Reload all textures of all animations
 	 */
 	public void reloadTextures() {
 		
 		int length = this.anims.length;
 		for (int i = 0; i < length; i++) {
 			
 			this.anims[i].reloadTextures();
 		}
 	}
 	
 	/**
 	 * Get rid of all textures of all animations
 	 */
 	public void dispose() {
 		
 		int length = this.anims.length;
 		for (int i = 0; i < length; i++) {
 			
 			this.anims[i].dispose();
 		}
 	}
 }
