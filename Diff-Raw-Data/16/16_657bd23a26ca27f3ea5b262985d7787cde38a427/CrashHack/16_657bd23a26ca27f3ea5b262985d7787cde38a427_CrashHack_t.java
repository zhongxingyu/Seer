 package tripleM.CrashHack.General;
 
 import tripleM.CrashHack.Screens.GameScreen;
 
 import com.badlogic.gdx.ApplicationListener;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Screen;
 import com.badlogic.gdx.graphics.GL10;
 
 import tripleM.CrashHack.General.Control;
 
 
 public class CrashHack implements ApplicationListener{
 	public static final int GAME_WIDTH = 480;
 	public static final int GAME_HEIGHT = 320;
 	
 	private Screen pantalla;
 	private Control ctls;
 	
 	
 	@Override
 	public void create() {
 		Setup.load();
 		Art.load();
 		Sound.load();
 		
 		ctls = new Control();
 		Gdx.input.setInputProcessor(ctls);
 		
 		pantalla = new GameScreen(ctls);
 	}
 
 	@Override
 	public void dispose() {
 		pantalla.dispose();
 		ctls.dispose();
 		
 	}
 
 	@Override
 	public void pause() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void render() {
		Gdx.gl.glClearColor(0,1,1,0);
 		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 		pantalla.render(0);
 		
 	}
 
 	@Override
 	public void resize(int arg0, int arg1) {
 		pantalla.resize(arg0, arg1);
 		
 	}
 
 	@Override
 	public void resume() {
 		// TODO Auto-generated method stub
 		
 	}
 }
