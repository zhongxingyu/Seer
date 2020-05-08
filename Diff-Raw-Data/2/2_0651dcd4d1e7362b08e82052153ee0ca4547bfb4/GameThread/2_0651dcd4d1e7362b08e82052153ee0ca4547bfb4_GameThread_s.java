 package com.musicgame.PumpAndJump.game;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.InputProcessor;
 import com.badlogic.gdx.Screen;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.scenes.scene2d.ui.Skin;
 
 public abstract class GameThread implements InputProcessor, Screen, Runnable
 {
 	public static Skin uiSkin;
	public static SpriteBatch batch = new SpriteBatch();
 	public int height, width;
 
 	@Override
 	public void resize(int width, int height)
 	{
 	}
 
 	public void myWait()
 	{
 		synchronized(this)
 		{
 			repause();
 			try
 			{
 				this.wait();
 			} catch (InterruptedException e)
 			{
 				e.printStackTrace();
 			}
 		}
 	}
 
 	public void myNotify()
 	{
 		synchronized(this)
 		{
 			unpause();
 			this.notify();
 		}
 	}
 
 	/**
 	 * Returns the name that is accosiated with this thread object
 	 */
 	public abstract ThreadName getThreadName();
 
 	public abstract void unpause();
 	public abstract void repause();
 
 	/**
 	 * This is called before any of the addition abstract methods are called
 	 */
 	protected void updateSelf()
 	{
 		Gdx.input.setInputProcessor(this);
 		resize(Gdx.graphics.getWidth(),Gdx.graphics.getHeight());
 	}
 
 	/**
 	 * The calling object will get whatever ever needed information from the given object
 	 * called when switching threads
 	 * @param currentThread
 	 */
 	public abstract void switchFrom(GameThread currentThread);
 
 	/**
 	 * The calling object will get whatever ever needed information from the given object
 	 * called when adding threads
 	 * @param currentThread
 	 */
 	public abstract void addFrom(GameThread currentThread);
 
 	/**
 	 * The calling object will get whatever ever needed information from the given object
 	 * called when removing threads
 	 * @param currentThread
 	 */
 	public abstract void removeFrom(GameThread currentThread);
 
 
 	/**
 	 * Empty methods we may use but we may not use them
 	 */
 	@Override
 	public boolean keyDown(int keycode) {
 		return false;
 	}
 
 	@Override
 	public boolean keyUp(int keycode) {
 		return false;
 	}
 
 	@Override
 	public boolean keyTyped(char character) {
 		return false;
 	}
 
 	@Override
 	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
 		return false;
 	}
 
 	@Override
 	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
 		return false;
 	}
 
 	@Override
 	public boolean touchDragged(int screenX, int screenY, int pointer) {
 		return false;
 	}
 
 	@Override
 	public boolean mouseMoved(int screenX, int screenY) {
 		return false;
 	}
 
 	@Override
 	public boolean scrolled(int amount) {
 		return false;
 	}
 
 	@Override
 	public void pause() {}
 
 	@Override
 	public void show() {}
 
 	@Override
 	public void hide() {}
 
 	@Override
 	public void dispose() {}
 
 	@Override
 	public void resume() {}
 
 	@Override
 	public void run(){}
 }
