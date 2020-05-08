 package com.dat255_group3.view;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.Animation;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.dat255_group3.model.Character;
 
 /**  A view class for the character Model. 
  * @author The Hans-Gunnar Crew
  * 
  */
 public class CharacterView {
 	
	private static final int FRAMES_COLS = 4;
 	private Character character;
 	private OrthographicCamera camera;
 	private SpriteBatch spriteBatch;
 	private Texture walkSheet;
 	private TextureRegion[] walkFrames;
 	private TextureRegion currentFrame;
 	private Animation walkAnimation;
 	private Sprite sprite;
 	
 	
 	/** A constructor that takes a character class.
 	 * 
 	 * @param character
 	 */
 	public CharacterView (Character character, OrthographicCamera camera) {
 		this.character = character;
 		this.spriteBatch = new SpriteBatch();
 		this.camera = camera;
 		
 		//Create animation frames
 		walkSheet = new Texture(Gdx.files.internal("ui/characterStandingStill.png")); //wrong file
//		TextureRegion[][] tmp = TextureRegion.split(walkSheet, walkSheet.getWidth()/FRAMES_COLS, walkSheet.getHeight());
//		walkFrames = new TextureRegion[FRAMES_COLS];
//		for(int i = 0;i<FRAMES_COLS;i++) {
//			walkFrames[i] = tmp[0][i];
 //		}
 		
 //		walkAnimation = new Animation(0.06f, walkFrames);
 		
 		sprite = new Sprite(walkSheet);
 		sprite.setSize(50, 50);
 	}
 	
 	
 	/**
 	 * A method which draws the character (which is currently just a turquoise rectangle)
 	 * The Characters appearance will change with time. This is only to test.
 	 */
 	public void draw(double time){
 		camera.update();
 		spriteBatch.begin();
 		spriteBatch.setProjectionMatrix(camera.combined);
 //		currentFrame = walkAnimation.getKeyFrame((float)time, true);
 //		spriteBatch.draw(currentFrame, character.getPosition().x-Character.getRadius(), character.getPosition().y-Character.getRadius());
 		sprite.setPosition(character.getPosition().x-Character.getRadius(), character.getPosition().y-Character.getRadius());
 		sprite.draw(spriteBatch);
 		spriteBatch.end();
 	}
 
 
 	public Character getCharacter() {
 		return character;
 	}
 }
