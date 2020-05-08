 package com.dat255_group3.view;
 
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
 import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
 import com.badlogic.gdx.math.Rectangle;
 import com.dat255_group3.model.Character;
 
 /**  A view class for the character Model. 
  * @author The Hans-Gunnar Crew
  * 
  */
 public class CharacterView {
 	
 	private Rectangle characterRect;
 	private Character character;
 	private ShapeRenderer shape = new ShapeRenderer();
 	
 	/** A constructor that takes a character class.
 	 * 
 	 * @param character
 	 */
 	public CharacterView (Character character) {
 		this.character = character;
 		characterRect = new Rectangle();
		characterRect.height = character.getHeight();
		characterRect.width =  character.getWidth();
 	}
 	
 	
 	/**
 	 * A method which draws the character (which is currently just a turquoise rectangle)
 	 * The Characters appearance will change with time. This is only to test.
 	 */
 	public void draw(){
 		shape.begin(ShapeType.Filled);
 		shape.setColor(Color.CYAN);
		shape.rect(character.getPosition().x, character.getPosition().y, characterRect.width, characterRect.height);
 		shape.end();
 	}
 }
