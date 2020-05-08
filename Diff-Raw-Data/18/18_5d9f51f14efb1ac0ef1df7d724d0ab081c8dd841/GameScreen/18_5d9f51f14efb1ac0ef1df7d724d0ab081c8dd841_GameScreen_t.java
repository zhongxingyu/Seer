 package tripleM.CrashHack.Screens;
 
 import tripleM.CrashHack.General.Art;
 import tripleM.CrashHack.General.Control;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Screen;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 
 public class GameScreen implements Screen {
 	private SpriteBatch spriteBatch;
 	private Control control;
 
 	
 	public GameScreen(Control _c) {
 		control = _c;
		this.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());	
 
 	}
 	
 	@Override
 	public void dispose() {
 		spriteBatch.dispose();
 
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
 	public void render(float arg0) {
         
 		spriteBatch.begin();
 		
 //		if (Gdx.app.getType() == ApplicationType.Android) {
 			Art.bigPad.draw(spriteBatch);
 			
 			int sr = (int) Art.bigPad.getHeight() / 4;
 			int x = (int) Art.bigPad.getX() +sr;
 			int y = (int) Art.bigPad.getY() +sr;
 			
 			
 			if (control.pressed[Control.PAD] != Control.NOTPRESSED)
 			{
 				y += (control.pressed[Control.UP] != Control.NOTPRESSED) ? sr : 0;
 				y -= (control.pressed[Control.DOWN] != Control.NOTPRESSED) ? sr : 0;
 				x += (control.pressed[Control.RIGHT] != Control.NOTPRESSED) ? sr : 0;
 				x -= (control.pressed[Control.LEFT] != Control.NOTPRESSED) ? sr : 0;
 			}
 			Art.smallPad.setPosition(x, y);
 			
 			if (control.pressed[Control.A] != Control.NOTPRESSED)
 				Art.pressA();
 			else Art.unpressA();
 			
 			if (control.pressed[Control.B] != Control.NOTPRESSED)
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
 			
 			Art.bigPad.setPosition(20, 20);
 			Art.smallPad.setPosition(Art.bigPad.getX() + rad, Art.bigPad.getY() + rad);
 			
 			Art.bigPad.setSize(Art.sizeBigPad * ratio, Art.sizeBigPad * ratio);			
 			Art.smallPad.setSize(Art.sizeSmallPad * ratio, Art.sizeSmallPad * ratio);
 			
 			rad *= ratio;
 			control.placePad(
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
 			
 			control.placeButtonA(
 					(int) Art.aButton.getX() + rad, 
 					(int) _height - (int) Art.aButton.getY() - rad, 
 					(int) (rad * 1.1));
 			
 			
 
 			control.placeButtonB(
 					(int) Art.bButton.getX() + rad,
 				 	(int) _height - (int) Art.bButton.getY() - rad, 
 				 	(int) (rad * 1.1));
 			
 			
 
 		
 //		}
 		
 	}
 
 	@Override
 	public void resume() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void show() {
 		// TODO Auto-generated method stub
 
 	}
 
 }
