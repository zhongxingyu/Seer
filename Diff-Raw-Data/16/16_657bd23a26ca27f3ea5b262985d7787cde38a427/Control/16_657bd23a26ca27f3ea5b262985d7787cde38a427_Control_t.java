 package tripleM.CrashHack.General;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.InputProcessor;
 import com.badlogic.gdx.Input.Keys;
 import com.badlogic.gdx.Screen;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 
 public class Control implements InputProcessor, Screen {
 	private SpriteBatch spriteBatch;
 	
 	private static final int TOTALBUTTONS = 7;
 	public static final int UP = 0;
 	public static final int DOWN = 1;
 	public static final int LEFT = 2;
 	public static final int RIGHT = 3;
 	public static final int A = 4;
 	public static final int B = 5;
 	private static final int PAD = 6;
 	private int[] buttonPressed;
 	private boolean[] keyPressed;
 	public boolean[] actions;
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
 
 	public Control() {
 		spriteBatch = new SpriteBatch();
 		
 		buttonPressed = new int [TOTALBUTTONS];
 		keyPressed = new boolean [TOTALBUTTONS];
 		actions = new boolean [TOTALBUTTONS];
 		
 		for (int i = 0; i < TOTALBUTTONS; i++)
 		{
 			buttonPressed[i] = NOTPRESSED;
 			keyPressed[i] = false;
 			actions[i] = false;
 		}
 		
 		keyPressed[PAD] = true;
 
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
 
 	public boolean isPadTouched(int _x, int _y) {
 		if ((Math.abs(_x - padX) >= padRad) || (Math.abs(_y - padY) >= padRad))
 			return false;
 		return true;
 	}
 
 	public boolean isButtonATouched(int _x, int _y) {
 		if ((Math.abs(_x - buttonAX) >= buttonARad)
 				|| (Math.abs(_y - buttonAY) >= buttonARad))
 			return false;
 		return true;
 	}
 
 	public boolean isButtonBTouched(int _x, int _y) {
 		if ((Math.abs(_x - buttonBX) >= buttonBRad)
 				|| (Math.abs(_y - buttonBY) >= buttonBRad))
 			return false;
 		return true;
 	}
 
 	@Override
 	public boolean keyDown(int _key) {
 		int button = NOTPRESSED;
 		
 		if (_key == Keys.DPAD_UP) button = UP;
 		if (_key == Keys.DPAD_LEFT) button = LEFT;
 		if (_key == Keys.DPAD_DOWN) button = DOWN;
 		if (_key == Keys.DPAD_RIGHT) button = RIGHT;
 		if (_key == Keys.C) button = A;
 		if (_key == Keys.D) button = B;
 		
 		if ((button != NOTPRESSED)) {
 			keyPressed[button] = true;
 			Gdx.app.log("Control","Key pressed " + button);
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	public boolean keyUp(int _key) {
 		int button = NOTPRESSED;
 		
 		if (_key == Keys.DPAD_UP) button = UP;
 		if (_key == Keys.DPAD_LEFT) button = LEFT;
 		if (_key == Keys.DPAD_DOWN) button = DOWN;
 		if (_key == Keys.DPAD_RIGHT) button = RIGHT;
 		if (_key == Keys.C) button = A;
 		if (_key == Keys.D) button = B;
 		
 		if ((button != NOTPRESSED)) {
 			keyPressed[button] = false;
 			Gdx.app.log("Control","Key unpressed " + button);
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	public boolean keyTyped(char arg0) {
 		// Not used
 		return false;
 	}
 
 	@Override
 	public boolean scrolled(int arg0) {
 		// Not used
 		return false;
 	}
 
 	@Override
 	public boolean touchDown(int _x, int _y, int _p, int arg3) {
 		boolean aux = false;
 		Gdx.app.log("Control", ("Pressed " + _x + " " + _y));
 
 		if ((buttonPressed[A] == NOTPRESSED) && isButtonATouched(_x, _y)) {
 			Gdx.app.log("Control", "Pressed A ");
 			buttonPressed[A] = _p;
 			aux = true;
 		}
 
 		if ((buttonPressed[B] == NOTPRESSED) && isButtonBTouched(_x, _y)) {
 			Gdx.app.log("Control", "Pressed B");
 			buttonPressed[B] = _p;
 			aux = true;
 		}
 
 		if ((buttonPressed[PAD] == NOTPRESSED) && (isPadTouched(_x, _y))) {
 			Gdx.app.log("Control", "Pressed PAD");
 			buttonPressed[PAD] = _p;
 			aux = true;
 			buttonPressed[UP] = (_y < (padY - 0.3 * padRad)) ? _p : -1;
 			buttonPressed[DOWN] = (_y > (padY + 0.3 * padRad)) ? _p : -1;
 			buttonPressed[RIGHT] = (_x > (padX + 0.3 * padRad)) ? _p : -1;
 			buttonPressed[LEFT] = (_x < (padX - 0.3 * padRad)) ? _p : -1;
 		}
 
 		return aux;
 	}
 
 	@Override
 	public boolean touchDragged(int _x, int _y, int _p) {
 		boolean aux = false;
 
 		// Check (and update) the PAD in case of dragging
 		// We don't care about other buttons being dragged
 		if (_p == buttonPressed[PAD]) {
 			Gdx.app.log("Control", "Dragged PAD");
 			aux = true;
 			buttonPressed[UP] = (_y < (padY - 0.3 * padRad)) ? _p : -1;
 			buttonPressed[DOWN] = (_y > (padY + 0.3 * padRad)) ? _p : -1;
 			buttonPressed[RIGHT] = (_x > (padX + 0.3 * padRad)) ? _p : -1;
 			buttonPressed[LEFT] = (_x < (padX - 0.3 * padRad)) ? _p : -1;
 		}
 
 		return aux;
 	}
 
 	@Override
 	public boolean touchMoved(int arg0, int arg1) {
 		// Not used
 		return false;
 	}
 
 	@Override
 	public boolean touchUp(int _x, int _y, int _p, int arg3) {
 		boolean ret = false;
 
 		// Test pointer to pressed[]
 		for (int i = 0; i < TOTALBUTTONS; i++) {
 			if (buttonPressed[i] == _p) {
 				Gdx.app.log("Control", "Up " + (i) + " " + (_p));
 				buttonPressed[i] = NOTPRESSED;
 				ret = true;
 			}
 		}
 
 		return ret;
 	}
 
 	@Override
 	public void render(float delta) {
 		for (int i = 0; i < TOTALBUTTONS; i++)
 			actions[i] = (buttonPressed[i] != NOTPRESSED) | keyPressed [i];
 		
 		
 		spriteBatch.begin();
 		
 //		if (Gdx.app.getType() == ApplicationType.Android) {
 			Art.bigPad.draw(spriteBatch);
 			
 			int sr = (int) Art.bigPad.getHeight() / 4;
 			int x = (int) Art.bigPad.getX() +sr;
 			int y = (int) Art.bigPad.getY() +sr;
 			
 			
 			if (actions[PAD])
 			{
 				y += actions[UP] 	? sr : 0;
 				y -= actions[DOWN] 	? sr : 0;
 				x += actions[RIGHT] ? sr : 0;
 				x -= actions[LEFT]	? sr : 0;
 			}
 			Art.smallPad.setPosition(x, y);
 			
 			if (actions[A])
 				Art.pressA();
 			else Art.unpressA();
 			
 			if (actions[B])
 				Art.pressB();
 			else Art.unpressB();
 			
 
 			Art.smallPad.draw(spriteBatch);
 			Art.aButton.draw(spriteBatch);
 			Art.bButton.draw(spriteBatch);
 			
 			
 //		}
 		spriteBatch.end();
 		
 	}
 
 	@Override
 	public void resize(int _width, int _height) {
 
 		spriteBatch = new SpriteBatch();
 		
 //		if (Gdx.app.getType() == ApplicationType.Android) {
 		//Set up button controls
 		
 			// PAD
 			
 			float ratio = (_height / 3) / Art.sizeBigPad;
 			if (ratio > 2) ratio = 2;
 			if (ratio < 1) ratio = 1;
 			
 			int rad = (int) Art.sizeBigPad / 2;
 			
			Art.bigPad.setPosition(40, 40);
 			Art.smallPad.setPosition(Art.bigPad.getX() + rad, Art.bigPad.getY() + rad);
 			
 			Art.bigPad.setSize(Art.sizeBigPad * ratio, Art.sizeBigPad * ratio);			
 			Art.smallPad.setSize(Art.sizeSmallPad * ratio, Art.sizeSmallPad * ratio);
 			
 			rad *= ratio;
 			placePad(
 					(int) (Art.bigPad.getX() + rad),
 					(int) (_height - Art.bigPad.getY() - rad),
 					(int) (rad * 1.1)); //Added 10% error margin
 		
 			
 			// A and B buttons
 			ratio = (_height / 6) / Art.sizeA;
 			if (ratio > 2) ratio = 2;
 			if (ratio < 1) ratio = 1;
 			
 			
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
 					(int) (rad * 1.1));
 			
 			
 
 			placeButtonB(
 					(int) Art.bButton.getX() + rad,
 				 	(int) _height - (int) Art.bButton.getY() - rad, 
 				 	(int) (rad * 1.1));
 			
 			
 
 		
 //		}
 		
 	}
 
 	@Override
 	public void show() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void hide() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void pause() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void resume() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void dispose() {
 		spriteBatch.dispose();
 	}
 	
 	
 }
