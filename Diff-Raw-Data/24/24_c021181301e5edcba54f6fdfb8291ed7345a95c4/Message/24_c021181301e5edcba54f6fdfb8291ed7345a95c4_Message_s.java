 package com.game.rania.model.ui;
 
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.game.rania.RaniaGame;
 import com.game.rania.model.Text;
 import com.game.rania.model.element.HUDDynamicObject;
 import com.game.rania.model.element.RegionID;
 
 public class Message extends HUDDynamicObject{
 
 	private float time = 0.0f;
 	private float delay = 0.0f;
 	private Text  text = null;
 	
 	public Message(RegionID id, float x, float y, Text text, float delay) {
 		super(id, x, y);
 		this.text = text;
 		this.delay = delay;
 	}
 
 	@Override
 	public void update(float deltaTime){
 		time += deltaTime;
 		
 		if (delay < time){
 			RaniaGame.mController.removeDynamicHUDObject(this);
 		}
 	}
 	
 	@Override
 	public boolean draw(SpriteBatch sprite){
 		if (!super.draw(sprite))
 			return false;
 
 		if (text != null)
 			text.draw(sprite, text.position.x + position.x, text.position.y + position.y, angle, scale.x, scale.y);
 
 		return true;
 	}
 }
