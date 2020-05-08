 package tripleM.crashHack;
 
 import java.lang.reflect.Method;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import tripleM.CrashHack.General.Art;
 import tripleM.CrashHack.General.Control;
 import tripleM.CrashHack.General.Setup;
 import android.util.Log;
 import android.view.*;
 
 /* Notice we don't use a ellipse as touch are because all the devices tested
  * doesn't support it (and return a circle instead)
  */
 public class ControlAndroid implements Control, View.OnTouchListener {
 
 	private SpriteBatch spriteBatch; //Initialize in resize()
 	private int[] 		buttonPressed;
 	
 	private final float MAXIMUM_RATIO = 2;
 	private final float MINIMUM_RATIO = 0.5f;
 	private final float PAD_FRACTION_HEIGHT = 3; //Fraction of the screen for the pad
 	private final float PAD_FRACTION_WIDTH = 4;
 	private final int PAD_MARGIN = 10;
 	
 	private final float AB_FRACTION_WIDTH = 3;
 	private final float AB_FRACTION_HEIGHT = 3;
 	private final float AB_MARGIN = 20;
 	
 	private final int BUTTONS_MARGIN = 5; // Margin (in pixels) added around the buttons to detect touch
 	
 	private final int MAX_PRECISION = 5; // If the precision of the device is over this we use the old method. Why? Cause fuck'em. Thats why.
 	
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
 	
 	private static boolean APILEVEL;
 	private static boolean APILEVELtested;
 	private static Method getTouchMajor; 
//	private static Method getTouchMinor;
//	private static Method getOrientation;
 	
     public ControlAndroid() {		
     	
         buttonPressed = new int [TOTALBUTTONS];
 		
 		for (int i = 0; i < TOTALBUTTONS; i++)
 		{
 			buttonPressed[i] = NOTPRESSED;
 			Control.actions[i] = false;
 		}
     }
     
     public void setupAPI()
     {
 		if (Setup.getConfigInt(Setup.fatfingers, 0) == 1)
 		{
 			APILEVEL = false;
 			APILEVELtested = true;
 		}
 		else
 		{
 			APILEVEL = true;
 			APILEVELtested = false;
 			try {
 				Class<?> c[] = new Class[1];
 				c[0] = int.class;
 				
 				getTouchMajor = MotionEvent.class.getMethod("getTouchMajor", c);
//				getTouchMinor = MotionEvent.class.getMethod("getTouchMinor", c);
//				getOrientation  = MotionEvent.class.getMethod("getOrientation", c);			
 			} catch (Exception e) {
 				Log.v("API level", "Incapable of detecting touched areas");
 				APILEVEL = false;
 			}
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
 
 	private boolean isPadTouched(int _x, int _y, int _p, MotionEvent _event) {
 		//Don't care about radius
 		if ((Math.abs(_x - padX) >= padRad) || (Math.abs(_y - padY) >= padRad))
 			return false;
 		return true;
 	}
 
 	private boolean isButtonATouched(int _x, int _y, int _p, MotionEvent _event) {
 		// Not capable of detecting touching areas
 		if (!APILEVEL)
 		{
 			return (Math.abs(_x - buttonAX) <= buttonARad) && (Math.abs(_y - buttonAY) <= buttonARad);
 		}
 		
 		int d = Math.abs(_x - buttonAX);
 		d += Math.abs(_y - buttonAY);
 		d *= Math.sqrt(2);
 		
 		Object o[] = new Object [] {_p};
 		float rad = 0;
 		try {
 			rad = (Float) ControlAndroid.getTouchMajor.invoke(_event, o);
 		} catch (Exception e) {
 			Gdx.app.log("API level", "isButtonATouched " + getTouchMajor.getName());
 		}
 		
 		if (d < (buttonARad + rad))
 				return true;
 		return false;
 	}
 
 	private boolean isButtonBTouched(int _x, int _y, int _p, MotionEvent _event) {
 		// Not capable of detecting touching areas
 		if (!APILEVEL)
 		{
 			return (Math.abs(_x - buttonBX) <= buttonBRad) && (Math.abs(_y - buttonBY) <= buttonBRad);
 		}
 		
 		int d = Math.abs(_x - buttonBX);
 		d += Math.abs(_y - buttonBY);
 		d *= Math.sqrt(2);
 		
 		Object o[] = new Object [] {_p};
 		float rad = 0;
 		try {
 			rad = (Float) ControlAndroid.getTouchMajor.invoke(_event, o);
 		} catch (Exception e) {
 			Gdx.app.log("API level", "isButtonATouched " + getTouchMajor.getName());
 		}
 		
 		if (d < (buttonBRad + rad))
 				return true;
 		return false;
 	}
 
 	public boolean touchDown(int _x, int _y, int _p, MotionEvent _event) {
 		boolean aux = false;
 		Control.actions[ANYTHING] = true;
 		
 		if ((buttonPressed[A] == NOTPRESSED) && isButtonATouched(_x, _y, _p, _event)) {
 			buttonPressed[A] = _p;
 			aux = true;
 		}
 
 		if ((buttonPressed[B] == NOTPRESSED) && isButtonBTouched(_x, _y, _p, _event)) {
 			buttonPressed[B] = _p;
 			aux = true;
 		}
 
 		if ((buttonPressed[PAD] == NOTPRESSED) && (isPadTouched(_x, _y, _p, _event))) {
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
 		
 		Control.actions[ANYTHING] = true;
 		
 		// Check (and update) the PAD in case of dragging
 		if ((_p == buttonPressed[PAD]) || (isPadTouched(_x, _y, _p, _event))) {
 			aux = true;
 			buttonPressed[PAD] = _p;
 			buttonPressed[UP] = (_y < (padY - 0.3 * padRad)) ? _p : NOTPRESSED;
 			buttonPressed[DOWN] = (_y > (padY + 0.3 * padRad)) ? _p : NOTPRESSED;
 			buttonPressed[RIGHT] = (_x > (padX + 0.3 * padRad)) ? _p : NOTPRESSED;
 			buttonPressed[LEFT] = (_x < (padX - 0.3 * padRad)) ? _p : NOTPRESSED;
 		}
 		
 		// Now we do care about the other buttons		
 		if (isButtonATouched(_x, _y, _p, _event)) {
 			buttonPressed[A] = _p;
 			aux = true;
 		} else {
			if (buttonPressed[A] == _p)
				buttonPressed[A] = NOTPRESSED;
 		}
 		
 		
 		if (isButtonBTouched(_x, _y, _p, _event)) {
 			buttonPressed[B] = _p;
 			aux = true;
 		} else {
			if (buttonPressed[B] == _p)
				buttonPressed[B] = NOTPRESSED;
 		}
 
 		return aux;
 	}
 
 	public boolean touchUp(int _x, int _y, int _p, MotionEvent _event) {
 		boolean ret = false;
 		
 		Control.actions[ANYTHING] = true;
 
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
 			Control.actions[i] = (buttonPressed[i] != NOTPRESSED);
 		
 		
 		spriteBatch.begin();
 		
 		Art.bigPad.draw(spriteBatch);
 		
 		int sr = (int) Art.bigPad.getHeight() / 4;
 		int x = (int) Art.bigPad.getX() +sr;
 		int y = (int) Art.bigPad.getY() +sr;
 		
 		int up = 0;
 		int right = 0;
 		
 		if (Control.actions[PAD])
 		{			
 			up += Control.actions[UP] 		? sr : 0;
 			up -= Control.actions[DOWN] 	? sr : 0;
 			right += Control.actions[RIGHT] ? sr : 0;
 			right -= Control.actions[LEFT]	? sr : 0;
 			
 			if ((up != 0) && (right != 0))
 			{
 				up *= Math.sqrt(2) / 2;
 				right *= Math.sqrt(2) / 2;
 			}
 		}
 		Art.smallPad.setPosition(x + right, y + up);
 		
 		if (Control.actions[A])
 			Art.pressA();
 		else Art.unpressA();
 		
 		if (Control.actions[B])
 			Art.pressB();
 		else Art.unpressB();
 		
 
 		Art.smallPad.draw(spriteBatch);
 		Art.aButton.draw(spriteBatch);
 		Art.bButton.draw(spriteBatch);
 			
 		spriteBatch.end();
 		
 		Control.actions[ANYTHING] = false;
 		
 	}
 
 	@Override
 	public void resize(int _width, int _height) {
 
 		spriteBatch = new SpriteBatch();
 		
 		// PAD
 		
 		float ratio = Math.min(
 				(((float) _height / PAD_FRACTION_HEIGHT) / (Art.sizeBigPad + PAD_MARGIN)),
 				(((float) _width / PAD_FRACTION_WIDTH) / (Art.sizeBigPad + PAD_MARGIN)));
 		
 		if (ratio > MAXIMUM_RATIO) ratio = MAXIMUM_RATIO;
 		else if (ratio < MINIMUM_RATIO) ratio = MINIMUM_RATIO;
 		
 		int rad = (int) Art.sizeBigPad / 2;
 		rad *= ratio;		
 		Art.bigPad.setPosition(PAD_MARGIN, PAD_MARGIN);
 		Art.smallPad.setPosition(Art.bigPad.getX() + rad, Art.bigPad.getY() + rad);
 		
 		Art.bigPad.setSize(Art.sizeBigPad * ratio, Art.sizeBigPad * ratio);			
 		Art.smallPad.setSize(Art.sizeSmallPad * ratio, Art.sizeSmallPad * ratio);
 		
 		
 		placePad(
 				(int) (Art.bigPad.getX() + rad),
 				_height - (int) (Art.bigPad.getY() + rad),
 				(int) (rad * Math.sqrt(2))); //Complete circle + Error margin
 	
 		
 		// A and B buttons
 		ratio = Math.min(
 				((_height / AB_FRACTION_HEIGHT) / (2 * Art.sizeA + AB_MARGIN)),
 				((_width / AB_FRACTION_WIDTH) / (2 * Art.sizeA + AB_MARGIN + MAX_PRECISION)));
 		if (ratio > MAXIMUM_RATIO) ratio = MAXIMUM_RATIO;
 		if (ratio < MINIMUM_RATIO) ratio = MINIMUM_RATIO;
 		
 		
 		rad = (int) Art.sizeA / 2;
 		rad *= ratio;
 		
 		int wid = (int) (Art.sizeA * ratio);
 		
 		
 		Art.bButton.setPosition(_width - (wid + AB_MARGIN), AB_MARGIN + wid);
 		Art.aButton.setPosition(_width - (2 * wid + 2 * AB_MARGIN + MAX_PRECISION), AB_MARGIN);
 		
 		Art.bButton.setSize(Art.sizeB * ratio, Art.sizeB * ratio);
 		Art.aButton.setSize(Art.sizeA * ratio, Art.sizeA * ratio);
 		
 		
 		
 		placeButtonA(
 				(int) (Art.aButton.getX() + rad), 
 				_height - (int) (Art.aButton.getY() + rad), 
 				(int) (rad * Math.sqrt(2)) + BUTTONS_MARGIN);
 		
 		
 
 		placeButtonB(
 				(int) (Art.bButton.getX() + rad),
 			 	_height - (int) (Art.bButton.getY() + rad), 
 			 	(int) (rad * Math.sqrt(2)) + BUTTONS_MARGIN);
 			
 		
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
 		
 		if (APILEVEL && ! APILEVELtested)
 		{
 			APILEVELtested = true;
 			if (event.getXPrecision() > MAX_PRECISION)
 			{
 				APILEVEL = false;
 				Gdx.app.log("Precision", "Yours sucks. So we use old method instead");
 			}				
 		}
 		
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
