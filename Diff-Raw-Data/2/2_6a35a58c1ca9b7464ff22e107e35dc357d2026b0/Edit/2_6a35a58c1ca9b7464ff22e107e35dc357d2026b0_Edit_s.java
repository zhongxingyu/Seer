 package com.game.rania.model.ui;
 
 import com.badlogic.gdx.Application;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Input;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
 import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
 import com.badlogic.gdx.math.Vector2;
 import com.game.rania.RaniaGame;
 import com.game.rania.model.Indexes;
 import com.game.rania.model.Text;
 import com.game.rania.model.element.Object;
 import com.game.rania.model.element.RegionID;
 
 public class Edit extends Object{
 
 	protected Text 		    text      = null;
 	protected EditAction    action    = null;
 	protected TextureRegion regionOn  = null;
 	protected int		    maxTextVisible;
 	protected int		    beginVisible = 0;
 	protected int		    endVisible   = 0;
 	public 	  boolean	    readOnly  = false;
 
 	public Edit(RegionID idTexture, RegionID idTextureOn, float x, float y, Text text, int maxTextVisible){
 		super(idTexture, x, y);
 		this.regionOn = RaniaGame.mView.getTextureRegion(idTextureOn);
 		this.text = text;
 		this.maxTextVisible = maxTextVisible;
 		touchObject = true;
 		init();
 	}
 
 	public Edit(RegionID idTexture, RegionID idTextureOn, float x, float y, Text text, int maxTextVisible, EditAction action){
 		super(idTexture, x, y);
 		this.regionOn = RaniaGame.mView.getTextureRegion(idTextureOn);
 		this.text = text;
 		this.maxTextVisible = maxTextVisible;
 		this.action = action;
 		touchObject = true;
 		init();
 	}
 	
 	protected void init(){
 		cursorPos = text.content.length();
 		beginVisible = Math.max(0, cursorPos - maxTextVisible);
 		endVisible = cursorPos;
 		zIndex = Indexes.edit;
 	}
 	
 	public String getText(){
 		return text.content;
 	}
 	
 	public void setText(String newText){
 		changeText(newText);
 		init();
 		if (action != null && action.set)
 			action.execute(this);
 	}
 	
 	protected void changeText(String newText){
 		text.content = newText;
 	}
 
 	public Object     nextControll = null;
 	protected Vector2 bufferPos  = new Vector2();
 	protected int     cursorPos  = 0;
 	protected boolean focus 	 = false;
 	protected boolean oldFocus   = false;
 	protected boolean showCursor = false;
 	protected boolean fillEdit   = false;
 	protected float   updateTime = 0.0f;
 	protected float   cursorTime = 0.0f;
 	protected float   keyTime    = 0.0f;
 	protected boolean startDrag  = false;
 	protected float   dragPosX   = 0.0f;
 
 	@Override
 	public boolean touchDown(float x, float y) {
 		if (text.content.length() > maxTextVisible) {
 			dragPosX = x;
 			startDrag = true;
 		}
 		
 		if (readOnly)
 			return true;
 
 		float minX = Float.MAX_VALUE;
 		cursorPos = 0;
 		for(int i = beginVisible; i <= endVisible; i++){
 			float posChar = Math.abs(text.getTextBound(beginVisible, i).width + text.position.x + text.getOffset().x - x);
 			if (minX > posChar){
 				cursorPos = i;
 				minX = posChar;
 			}
 		}
 		return true;
 	}
 
 	protected static final float speedDrag = 2.0f;
 	
 	@Override
 	public boolean touchDragged(float x, float y) {
 		if(!startDrag)
 			return true;
 		
 		int len   = text.content.length();
 		int delta = (int) ((dragPosX - x) * speedDrag * 0.1f);
 		dragPosX = x;
 		if (delta != 0) {
 			beginVisible += delta;
 			if (beginVisible < 0)
 				beginVisible = 0;
 			if (beginVisible > len - maxTextVisible)
 				beginVisible = len - maxTextVisible;
 			
 			if (len < beginVisible + maxTextVisible)
 				endVisible = len;
 			else
 				endVisible = beginVisible + maxTextVisible;
 			
 			if (cursorPos < beginVisible)
 				cursorPos = beginVisible;
 			else if (cursorPos > endVisible)
 				cursorPos = endVisible;
 		}		
 		
 		return true;
 	}
 	
 	@Override
 	public boolean touchUp(float x, float y) {
 		startDrag = false;
 		FocusElement.setFocus(this);
 		if (readOnly)
 			return true;
 		cursorTime = updateTime;
 		showCursor = true;
 		return true;
 	}
 	
 	@Override
 	public void update(float deltaTime){
 		super.update(deltaTime);
 		
 		if (readOnly)
 			return;
 			
 		updateTime += deltaTime;
 		
 		if (FocusElement.getFocus() == this)
 			focus = true;
 		else
 			focus = false;
 		
 		if (focus) {
 			if (updateTime - cursorTime > 0.5f){
 				cursorTime = updateTime;
 				showCursor = !showCursor;
 			}
 	
 			if (keyTime < updateTime){
 				switch (key)
 				{
 				case backspaceKey:
 					removeLeftChar();
 					break;
 	
 				case deleteKey:
 					removeRightChar();
 					break;
 	
 				case leftKey:
 					nextLeftPosition();
 					break;
 	
 				case rightKey:
 					nextRightPosition();
 					break;
 					
 				default:
 					break;
 				}
 				
 				keyTime = updateTime + 0.1f;
 			}
 		}
 		
 		if (focus != oldFocus) {
 			oldFocus = focus;
 			if (focus) {
 				if (Gdx.app.getType() != Application.ApplicationType.Desktop)	{
 					Gdx.input.setOnscreenKeyboardVisible(true);
 					if (region != null)	{
 						bufferPos.set(position);
 						position.set(0, getHeight());
 						fillEdit = true;
 					}
 				}
 				keysObject = true;
 			}
 			else {
 				if (Gdx.app.getType() != Application.ApplicationType.Desktop) {
 					Gdx.input.setOnscreenKeyboardVisible(false);
 					if (region != null)	{
 						position.set(bufferPos);
 						fillEdit = false;
 					}
 				}
 				keysObject = false;
 
 				if (action != null && action.focus)
 					action.execute(this);
 			}
 		}
 	}
 
 	@Override
 	public boolean draw(SpriteBatch sprite){		
 		if (!visible)
 			return false;
 		
 		sprite.setColor(color);
 		if (focus && regionOn != null)
 			drawRegion(sprite, regionOn);
 		else
 			drawRegion(sprite, region);
 		
 		if (text != null){
 			updateTextLength();
 			text.draw(sprite, beginVisible, endVisible, text.position.x + position.x, text.position.y + position.y, angle, scale.x, scale.y);
 		}			
 		
 		return true;
 	}
 
 	@Override
 	public boolean draw(ShapeRenderer shape){
 		if (!focus || !visible || (!showCursor && !fillEdit))
 			return false;
 		
 		shape.begin(ShapeType.FilledRectangle);
 		//if (fillEdit) {
 		//	shape.setColor(0, 0, 0, 1);
 		//	shape.filledRect(position.x + text.position.x - getWidth() * 0.5f,
 		//					 position.y + text.position.y - getHeight() * 0.5f,
 		//					 getWidth(),
 		//					 getHeight());
 		//}
 		
 		if (showCursor) {
 			float widthCursor = text.font.getSpaceWidth() * 0.2f;
 			float heightCursor = text.font.getCapHeight();
 			shape.setColor(text.color);
 			shape.filledRect(position.x + text.position.x + text.getTextBound(beginVisible, cursorPos).width + text.getOffset().x - widthCursor * 0.5f,
 							 position.y + text.position.y - heightCursor * 0.5f,
 						     widthCursor,
 						     heightCursor);
 		}
 		shape.end();
 
 		return true;
 	}
 	
 	@Override
 	public boolean keyTyped(char character) {
 		if (readOnly)
 			return true;
 		changeText(text.content.substring(0, cursorPos) + character + text.content.substring(cursorPos));
 		cursorPos++;
 		return true;
 	}
 	
 	protected void updateTextLength(){
 		int len = text.content.length();
 		if (len > maxTextVisible){
 			if (endVisible > len) {
 				beginVisible = beginVisible - (endVisible - len);
 				endVisible = len;
 				return;
 			}
 			if (cursorPos > endVisible){
 				beginVisible = beginVisible + (cursorPos - endVisible);
 				endVisible = beginVisible + maxTextVisible;
 				return;
 			}
 			if (cursorPos <= beginVisible){
 				endVisible = endVisible - (beginVisible - cursorPos);
 				beginVisible = endVisible - maxTextVisible;
 				return;
 			}
 		} else {
 			beginVisible = 0;
 			endVisible = len;
 			return;
 		}
 	}
 
 	private static final int backspaceKey = 1;
 	private static final int deleteKey = 2;
 	private static final int leftKey = 4;
 	private static final int rightKey = 8;
 	protected int key = 0;
 	
 	@Override
 	public boolean keyDown(int keycode) {
 		if (readOnly)
 			return true;
 		if (key != 0)
 			return true;
 			
 		switch (keycode) {
 		case Input.Keys.BACKSPACE:
 			removeLeftChar();
 			key = backspaceKey;
 			break;
 			
 		case Input.Keys.FORWARD_DEL:
 			removeRightChar();
 			key = deleteKey;
 			break;
 			
 		case Input.Keys.LEFT:
 			nextLeftPosition();
 			key = leftKey;
 			break;
 
 		case Input.Keys.RIGHT:
 			nextRightPosition();
 			key = rightKey;
 			break;
 
 		case Input.Keys.END:
 			cursorPos = text.content.length();
 			break;
 
 		case Input.Keys.HOME:
 			cursorPos = 0;
 			break;
 
 		default:
 			break;
 		}
 		if (key != 0)
 			keyTime = updateTime + 0.5f;
 		
 		return true;
 	}
 	
 	@Override
 	public boolean keyUp(int keycode) {
 		switch (keycode) {			
 		case Input.Keys.ENTER:
 			FocusElement.clearFocus();
 			if (action != null && action.enter)
 				action.execute(this);
 			break;
 		case Input.Keys.TAB:
 			FocusElement.setFocus(nextControll);
 			break;
 
 		default:
 			break;
 		}
 		key = 0;
 		return true;
 	}
 	
 	protected void removeLeftChar(){
 		if (cursorPos > 0) {
 			changeText(text.content.substring(0, cursorPos-1) + text.content.substring(cursorPos));
 			cursorPos--;
 		}
 	}
 
 	protected void removeRightChar(){
 		if (cursorPos != text.content.length())
 			changeText(text.content.substring(0, cursorPos) + text.content.substring(cursorPos+1));
 	}
 	
 	protected void nextLeftPosition(){
 		if (cursorPos > 0)
 			cursorPos--;
 	}
 	
 	protected void nextRightPosition(){
 		if (cursorPos < text.content.length())
 			cursorPos++;
 	}
 
 	public void setFocus() {
 		FocusElement.setFocus(this);
 	}
 	
 }
