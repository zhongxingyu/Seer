 package com.glowman.spaceunit.game.core;
 
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 
 /**
  *
  */
 public class Button {
 	private final Sprite _normal;
 	private final Sprite _clicked;
 	private Sprite _currentView;
 
 	public Button(TextureRegion normal, TextureRegion clicked)
 	{
 		_normal = new Sprite(normal);
 		_clicked = new Sprite(clicked);
 		_currentView = _normal;
 	}
 
 	public Sprite getView() { return _currentView; }
 
 	public void setClickedMode()
 	{
 		_currentView = _clicked;
 	}
 
 	public void setNormalMode()
 	{
 		_currentView = _normal;
 	}
 
 	public void draw(SpriteBatch spriteBatch)
 	{
 		_currentView.draw(spriteBatch);
 	}
 	
 	public void setSize(float width, float height) {
 		_normal.setSize(width, height);
 		_clicked.setSize(width, height);
 	}
 	
 	public void setScale(float scale)
 	{
 		_normal.setScale(scale);
 		_clicked.setScale(scale);
 
 		//updateXY();
 	}
 
 	public void setX(float x)
 	{
 		_normal.setX(x);
 		_clicked.setX(x);
 	}
 	public void setY(float y)
 	{
 		_normal.setY(y);
 
 		float deltaHeight = (_normal.getHeight() - _clicked.getHeight());
 		float otherY = y + deltaHeight * _normal.getScaleY() * 3/2;
 		
 		_clicked.setY(otherY);
 	}
 
 	private void updateXY()
 	{
 		this.setY(_normal.getY());
 	}
 
 }
