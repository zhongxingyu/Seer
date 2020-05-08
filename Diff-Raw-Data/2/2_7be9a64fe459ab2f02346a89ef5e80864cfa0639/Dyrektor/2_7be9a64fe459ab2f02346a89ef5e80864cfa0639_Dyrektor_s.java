 package inne;
 
 import screens.Level;
 import screens.TypWarstwy;
 import screens.Warstwa;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.InputProcessor;
 import com.badlogic.gdx.InputMultiplexer;
 import com.badlogic.gdx.Input.Keys;
 import com.badlogic.gdx.scenes.scene2d.InputEvent.Type;
 import com.badlogic.gdx.scenes.scene2d.InputListener;
 
 public class Dyrektor implements InputProcessor  {
 
 	Level poziom;
 	public TypWarstwy aktywnaWarstwa;
 	
 	public Dyrektor(Level obecnyPoziom)
 	{
 		poziom = obecnyPoziom;
 		//this.addKeyListener(this);
 	}
 	
 	public void ustawAktywnaWarstwe(Warstwa warstwa)
 	{
 		aktywnaWarstwa = warstwa.pobierzTyp();
 		Gdx.input.setInputProcessor(new InputMultiplexer(this, warstwa));
 	}
 	
 	public void ustawAktywnaWarstwe(TypWarstwy typ)
 	{
 		aktywnaWarstwa = typ;
 		switch (aktywnaWarstwa)
 		{
 			case pauza:
 				Gdx.input.setInputProcessor(new InputMultiplexer(this, poziom.warstwaPauzy));
 				break;
 			case podsumowanie:
 				Gdx.input.setInputProcessor(new InputMultiplexer(this, poziom.warstwaPodsumowania));
 				break;
 			case sklep:
 				Gdx.input.setInputProcessor(new InputMultiplexer(this, poziom.warstwaSklepu));
 				break;
 			case tlo:
 				Gdx.input.setInputProcessor(new InputMultiplexer(this, poziom.przesuwaneTo));
 				break;
 			case statystyki:
 				Gdx.input.setInputProcessor(new InputMultiplexer(this, poziom.statystyki));
 				break;				
 		}
 	}
 	
 	@Override
 	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
 		// TODO Auto-generated method stub
 		if (aktywnaWarstwa == TypWarstwy.pauza)
 		{
 			if (poziom.warstwaPauzy.buttonMain.hit(screenX, screenY, true)!=null)
 				poziom.gra.pokazMenu();
 		}
 		return false;
 	}
 
 
 	@Override
 	public boolean keyDown(int keycode) {
 		// TODO Auto-generated method stub
 		if (aktywnaWarstwa == TypWarstwy.tlo || aktywnaWarstwa == TypWarstwy.statystyki)
 		{
 			if (keycode == Keys.ESCAPE)
 			{
 				ustawAktywnaWarstwe(TypWarstwy.pauza);
 				return true;
 			}
 		}
 		else if (aktywnaWarstwa == TypWarstwy.pauza)
 		{			
 			if (keycode == Keys.ESCAPE)
 			{
				ustawAktywnaWarstwe(TypWarstwy.tlo);
 				return true;
 			}
 		}	
 		return false;
 	}
 
 	@Override
 	public boolean keyUp(int keycode) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean keyTyped(char character) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean touchDragged(int screenX, int screenY, int pointer) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean mouseMoved(int screenX, int screenY) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean scrolled(int amount) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 }
