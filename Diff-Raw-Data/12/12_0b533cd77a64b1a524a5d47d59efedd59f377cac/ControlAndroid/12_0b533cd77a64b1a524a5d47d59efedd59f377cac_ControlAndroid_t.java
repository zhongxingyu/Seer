 package tripleM.crashHack;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 
 import tripleM.CrashHack.General.Art;
 import tripleM.CrashHack.General.Control;
 import android.view.*;
 
 public class ControlAndroid implements Control, View.OnTouchListener {
 
 	private SpriteBatch spriteBatch; //Initialize in resize()
 	private int[] 		buttonPressed;
 	
 	private final int MAXIMUM_RATIO = 2;
 	private final int MINIMUM_RATIO = 1;
 	public static final int NOTPRESSED = -1;
 
 	private int padX;
 	private int padY;
 	private int padRad;
 	private int buttonAX;
 	private int buttonAY;
 	private int buttonARad;
 	private int buttonBX;
 	private int buttonBY;
 	private int buttonBRad;
 
 	
     public ControlAndroid() {		
         buttonPressed = new int [TOTALBUTTONS];
 		
 		for (int i = 0; i < TOTALBUTTONS; i++)
 		{
 			buttonPressed[i] = NOTPRESSED;
 			actions[i] = false;
 		}
     }
 
 	public void placePad(int _padX, int _padY, int _padRad) {
 		this.padX = _padX;
 		this.padY = _padY;
 		this.padRad = _padRad;
 	}
 
 	public void placeButtonA(int _buttonAX, int _buttonAY, int _buttonARad) {
 		this.buttonAX = _buttonAX;
 		this.buttonAY = _buttonAY;
 		this.buttonARad = _buttonARad;
 	}
 
 	public void placeButtonB(int _buttonBX, int _buttonBY, int _buttonBRad) {
 		this.buttonBX = _buttonBX;
 		this.buttonBY = _buttonBY;
 		this.buttonBRad = _buttonBRad;
 	}
 
 	private boolean isPadTouched(int _x, int _y, int _rad) {
 		//Don't care about radius
 		if ((Math.abs(_x - padX) >= padRad) || (Math.abs(_y - padY) >= padRad))
 			return false;
 		return true;
 	}
 
 	private boolean isButtonATouched(int _x, int _y, int _rad) {
 		int d = Math.abs(_x - buttonAX);
 		d += Math.abs(_y - buttonAY);
 		d *= Math.sqrt(2);
 		
 		if (d < (buttonARad + _rad))
 				return true;
 		return false;
 	}
 
 	private boolean isButtonBTouched(int _x, int _y, int _rad) {
 		int d = Math.abs(_x - buttonBX);
 		d += Math.abs(_y - buttonBY);
 		d *= Math.sqrt(2);
 		
 		if (d < (buttonBRad + _rad))
 				return true;
 		return false;
 	}
 
	/* Nota: Tratamos el toque como un círculo y no como una elipse por simpleza
	 * y porque mi móvil lo pasa como un círculo y no tengo forma de testearlo
	 */
 	public boolean touchDown(int _x, int _y, int _p, MotionEvent _event) {
 		boolean aux = false;
 		
 		//Gdx.app.log("Touch down", "x: " + _x + ". y: " + _y + ". alpha: " + _event.getOrientation(_p) + ". mayor: " + _event.getToolMajor(_p) +_p + ". minor: " + _event.getToolMinor(_p));
 
 		int rad = (int) _event.getToolMajor(_p);
 		if ((buttonPressed[A] == NOTPRESSED) && isButtonATouched(_x, _y, rad)) {
 			buttonPressed[A] = _p;
 			aux = true;
 		}
 
 		if ((buttonPressed[B] == NOTPRESSED) && isButtonBTouched(_x, _y, rad)) {
 			buttonPressed[B] = _p;
 			aux = true;
 		}
 
 		if ((buttonPressed[PAD] == NOTPRESSED) && (isPadTouched(_x, _y, rad))) {
 			buttonPressed[PAD] = _p;
 			aux = true;
 			buttonPressed[UP] = (_y < (padY - 0.3 * padRad)) ? _p : NOTPRESSED;
 			buttonPressed[DOWN] = (_y > (padY + 0.3 * padRad)) ? _p : NOTPRESSED;
 			buttonPressed[RIGHT] = (_x > (padX + 0.3 * padRad)) ? _p : NOTPRESSED;
 			buttonPressed[LEFT] = (_x < (padX - 0.3 * padRad)) ? _p : NOTPRESSED;
 		}
 
 		return aux;
 	}
 
 	public boolean touchDragged(int _x, int _y, int _p, MotionEvent _event) {
 		boolean aux = false;
 
 		int rad = (int) _event.getToolMajor(_p);
 		
 		// Check (and update) the PAD in case of dragging
 		if ((_p == buttonPressed[PAD]) || (isPadTouched(_x, _y, rad))) {
 			aux = true;
 			buttonPressed[PAD] = _p;
 			aux = true;
 			buttonPressed[UP] = (_y < (padY - 0.3 * padRad)) ? _p : NOTPRESSED;
 			buttonPressed[DOWN] = (_y > (padY + 0.3 * padRad)) ? _p : NOTPRESSED;
 			buttonPressed[RIGHT] = (_x > (padX + 0.3 * padRad)) ? _p : NOTPRESSED;
 			buttonPressed[LEFT] = (_x < (padX - 0.3 * padRad)) ? _p : NOTPRESSED;
 		}
 		
 		// Now we do care about the other buttons		
 		if (isButtonATouched(_x, _y, rad)) {
 			buttonPressed[A] = _p;
 			aux = true;
 		} else {
 			buttonPressed[A] = NOTPRESSED;
 		}
 		
 		
 		if (isButtonBTouched(_x, _y, rad)) {
 			buttonPressed[B] = _p;
 			aux = true;
 		} else {
 			buttonPressed[B] = NOTPRESSED;
 		}
 
 		return aux;
 	}
 
 	public boolean touchUp(int _x, int _y, int _p, MotionEvent _event) {
 		boolean ret = false;
 
 		// Test pointer to pressed[]
 		for (int i = 0; i < TOTALBUTTONS; i++) {
 			if (buttonPressed[i] == _p) {
 				buttonPressed[i] = NOTPRESSED;
 				ret = true;
 			}
 		}
 
 		return ret;
 	}
 
 	@Override
 	public void render(float delta) {
 		for (int i = 0; i < TOTALBUTTONS; i++)
 			actions[i] = (buttonPressed[i] != NOTPRESSED);
 		
 		
 		spriteBatch.begin();
 		
 		Art.bigPad.draw(spriteBatch);
 		
 		int sr = (int) Art.bigPad.getHeight() / 4;
 		int x = (int) Art.bigPad.getX() +sr;
 		int y = (int) Art.bigPad.getY() +sr;
 		
 		int up = 0;
 		int right = 0;
 		
 		if (actions[PAD])
 		{			
 			up += actions[UP] 		? sr : 0;
 			up -= actions[DOWN] 	? sr : 0;
 			right += actions[RIGHT] ? sr : 0;
 			right -= actions[LEFT]	? sr : 0;
 			
 			if ((up != 0) && (right != 0))
 			{
 				up *= Math.sqrt(2) / 2;
 				right *= Math.sqrt(2) / 2;
 			}
 		}
 		Art.smallPad.setPosition(x + right, y + up);
 		
 		if (actions[A])
 			Art.pressA();
 		else Art.unpressA();
 		
 		if (actions[B])
 			Art.pressB();
 		else Art.unpressB();
 		
 
 		Art.smallPad.draw(spriteBatch);
 		Art.aButton.draw(spriteBatch);
 		Art.bButton.draw(spriteBatch);
 			
 		spriteBatch.end();
 		
 	}
 
 	@Override
 	public void resize(int _width, int _height) {
 
 		spriteBatch = new SpriteBatch();
 		
 		
 		// PAD
 		
 		float ratio = (_height / 3) / Art.sizeBigPad;
 		if (ratio > MAXIMUM_RATIO) ratio = MAXIMUM_RATIO;
 		if (ratio < MINIMUM_RATIO) ratio = MINIMUM_RATIO;
 		
 		int rad = (int) Art.sizeBigPad / 2;
 		
 		Art.bigPad.setPosition(40, 40);
 		Art.smallPad.setPosition(Art.bigPad.getX() + rad, Art.bigPad.getY() + rad);
 		
 		Art.bigPad.setSize(Art.sizeBigPad * ratio, Art.sizeBigPad * ratio);			
 		Art.smallPad.setSize(Art.sizeSmallPad * ratio, Art.sizeSmallPad * ratio);
 		
 		rad *= ratio;
 		placePad(
 				(int) (Art.bigPad.getX() + rad),
 				(int) (_height - Art.bigPad.getY() - rad),
				rad);
 	
 		
 		// A and B buttons
 		ratio = (_height / 6) / Art.sizeA;
 		if (ratio > MAXIMUM_RATIO) ratio = MAXIMUM_RATIO;
 		if (ratio < MINIMUM_RATIO) ratio = MINIMUM_RATIO;
 		
 		
 		rad = (int) Art.sizeA / 2;
 		int wid = (int) (Art.sizeA * ratio);
 		
 		
 		Art.bButton.setPosition(_width - 20 - wid, 20 + wid);
 		Art.aButton.setPosition(_width - 40 - 2 * wid, 20);
 		
 		Art.bButton.setSize(Art.sizeB * ratio, Art.sizeB * ratio);
 		Art.aButton.setSize(Art.sizeA * ratio, Art.sizeA * ratio);
 		
 		rad *= ratio;
 		
 		placeButtonA(
 				(int) Art.aButton.getX() + rad, 
 				(int) _height - (int) Art.aButton.getY() - rad, 
				rad);
 		
 		
 
 		placeButtonB(
 				(int) Art.bButton.getX() + rad,
 			 	(int) _height - (int) Art.bButton.getY() - rad, 
			 	rad);
 			
 		
 	}
 
 	@Override
 	public void show() {
 	}
 
 	@Override
 	public void hide() {
 	}
 
 	@Override
 	public void pause() {
 	}
 
 	@Override
 	public void resume() {
 	}
 
 	@Override
 	public void dispose() {
 		spriteBatch.dispose();
 	}
 	
 	/**
 	 * Based on AndroidMultiTouchHandler 
 	 * (gdx-backend-android/src/com/badlogic/gdx/backends/android/AndroidMultiTouchHandler.java)
 	 * by badlogicgames@gmail.com
 	 * Licensed under the Apache License, Version 2.0
 	 */
 	@Override
 	public boolean onTouch(View v, MotionEvent event) {
 		
 		final int action = event.getAction() & MotionEvent.ACTION_MASK;
 		int pointerIndex = (event.getAction() & MotionEvent.ACTION_POINTER_ID_MASK) >> MotionEvent.ACTION_POINTER_ID_SHIFT;
 		int pointerId = event.getPointerId( pointerIndex );
 
 		int x = 0, y = 0;
 
 		switch( action )
 		{
 			case MotionEvent.ACTION_DOWN:
 			case MotionEvent.ACTION_POINTER_DOWN:
 				x = (int)event.getX( pointerIndex );
 				y = (int)event.getY( pointerIndex );
 				touchDown(x, y, pointerId, event);
 				break;
 
 			case MotionEvent.ACTION_UP:
 			case MotionEvent.ACTION_POINTER_UP:
 			case MotionEvent.ACTION_OUTSIDE:
 			case MotionEvent.ACTION_CANCEL:
 				x = (int)event.getX( pointerIndex );
 				y = (int)event.getY( pointerIndex );
 				touchUp(x, y, pointerId, event);
 				break;
 
 			case MotionEvent.ACTION_MOVE:
 				int pointerCount = event.getPointerCount();
 				for( int i = 0; i < pointerCount; i++ )
 				{
 					pointerIndex = i;
 					pointerId = event.getPointerId( pointerIndex );
 					x = (int)event.getX( pointerIndex );
 					y = (int)event.getY( pointerIndex );
 					touchDragged(x, y, pointerId, event);
 				}
 				break;
 		}
 		
 		return true;
 	}
 	
 }
