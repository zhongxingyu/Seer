 package fr.odai.zerozeroduck;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.InputProcessor;
 import com.badlogic.gdx.Screen;
 import com.badlogic.gdx.graphics.GL10;
 
 import fr.odai.zerozeroduck.controller.MainController;
 import fr.odai.zerozeroduck.model.World;
 
 public class IntroScreen implements Screen, InputProcessor{
 	
 	private ZeroZeroDuck game;
 	private IntroRenderer renderer;
 	int etape=2;
 	
 	public IntroScreen(ZeroZeroDuck game) {
 		super();
 		this.game = game;
 	}
 
 	@Override
 	public boolean keyDown(int keycode) {
 		if(renderer.getIsEtapeFinished()){
 			if(etape<=4){
 				renderer.setEtape(etape++);
 			}
 			else game.setScreen(game.titlescreen);
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
 	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
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
 
 	@Override
 	public void render(float delta) {
 		Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
 		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 		renderer.render(delta);
 	}
 
 	@Override
 	public void resize(int width, int height) {
		renderer.setSize(width, height);
 	}
 
 	@Override
 	public void show() {
 		renderer = new IntroRenderer();
 		Gdx.input.setInputProcessor(this);
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
 		// TODO Auto-generated method stub
 		
 	}
 
 }
