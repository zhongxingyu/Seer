 package com.musicgame.PumpAndJump.game.gameStates;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.BitmapFont;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.math.MathUtils;
 import com.musicgame.PumpAndJump.game.GameThread;
 import com.musicgame.PumpAndJump.game.PumpAndJump;
 import com.musicgame.PumpAndJump.game.ThreadName;
 
 public class Buffering extends GameThread
 {
 	Texture dropImage = new Texture(Gdx.files.internal("droplet.png"));
 	SpriteBatch batch = new SpriteBatch();
 	BitmapFont  font = new BitmapFont();
 	float x,y;
 	long position;
 	Thread runMethod;
 	long startTime;
 	long delay = 5000;
 	boolean threadStarted;
 	long counter = 0;
 	public Buffering()
 	{
 	}
 
 	@Override
 	public void render(float delta)
 	{
 		x = 500.0f+20.0f*com.badlogic.gdx.math.MathUtils.cos(position/(MathUtils.PI*2.0f));
 		y = 500.0f+20.0f*com.badlogic.gdx.math.MathUtils.sin(position/(MathUtils.PI*2.0f));
 
 
 
 		batch.begin();
 		font.draw(batch,"Counting "+counter,500, 500);
 		batch.draw(dropImage, x,y);
 		batch.end();
 		position++;
 	//	System.out.println("Buffering! " +x+" "+y);
 	}
 
 	@Override
 	public void show()
 	{
 	}
 
 	@Override
 	public void hide()
 	{
 	}
 
 	@Override
 	public void pause()
 	{
 	}
 
 	@Override
 	public void dispose()
 	{
 	}
 
 	@Override
 	public void unpause()
 	{
 	}
 
 	@Override
 	public boolean keyDown(int keycode) {return false;}
 
 	@Override
 	public boolean keyUp(int keycode) {return false;}
 
 	@Override
 	public boolean keyTyped(char character) {return false;}
 
 	@Override
 	public boolean touchDown(int screenX, int screenY, int pointer, int button) {return false;}
 
 	@Override
 	public boolean touchUp(int screenX, int screenY, int pointer, int button) {return false;}
 
 	@Override
 	public boolean touchDragged(int screenX, int screenY, int pointer) {return false;}
 
 	@Override
 	public boolean mouseMoved(int screenX, int screenY) {return false;}
 
 	@Override
 	public boolean scrolled(int amount) {return false;}
 
 
 	@Override
 	public void switchFrom(GameThread currentThread)
 	{
 	}
 
 
 	@Override
 	public void addFrom(GameThread currentThread)
 	{
 
 		if(currentThread instanceof RunningGame)
 		{
 			final RunningGame game = (RunningGame)currentThread;
 		//	System.out.println("ADDING THE BUFFER THREAD!!!! YAYYYY");
 			runMethod = new Thread()
 			{
 				public void run()
 				{
 					counter = 0;
 					startTime = System.currentTimeMillis();
 					while(game.bufferingNeeded())
 					{
 						counter = game.bufferingDistance();
 						try {
 							Thread.sleep(100);
 						} catch (InterruptedException e) {
 							e.printStackTrace();
 						}
 					}
 					PumpAndJump.removeThread(ThreadName.Buffering, Buffering.this);
 				}
 			};
 			runMethod.start();
 		}
 	}
 
 	@Override
 	public void removeFrom(GameThread currentThread)
 	{
 	}
 
 }
