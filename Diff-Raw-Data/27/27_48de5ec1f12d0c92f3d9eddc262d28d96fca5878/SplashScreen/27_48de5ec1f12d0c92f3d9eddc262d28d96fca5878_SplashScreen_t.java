 package com.pkelly.screens;
 
 
 import aurelienribon.tweenengine.BaseTween;
 import aurelienribon.tweenengine.Tween;
 import aurelienribon.tweenengine.TweenCallback;
 import aurelienribon.tweenengine.TweenManager;
 import com.badlogic.gdx.Game;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Screen;
 import com.badlogic.gdx.graphics.GL20;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.pkelly.tween.SpriteAccessor;
 
 /**
  * Created with IntelliJ IDEA.
  * User: PeKay
  * Date: 22/06/13
  * Time: 14:53
  * To change this template use File | Settings | File Templates.
  */
 public class SplashScreen implements Screen
 {
     private SpriteBatch batch;
     private Sprite splash;
     private TweenManager tweenManager;
 
 
     @Override
     public void render(float v)
     {
         Gdx.gl.glClearColor(0, 0, 0, 1);
         Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
 
         tweenManager.update(v);
 
         batch.begin();
         splash.draw(batch);
         batch.end();
     }
 
     @Override
     public void resize(int i, int i2)
     {
         //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public void show()
     {
         batch = new SpriteBatch();
         tweenManager = new TweenManager();
 
        Texture splashImage = new Texture("img/splashTitle.png");
         splash = new Sprite(splashImage);
 
         FadeInAndOut();
     }
 
     private void FadeInAndOut()
     {
         Tween.registerAccessor(Sprite.class, new SpriteAccessor());
 
         Tween.set(splash, SpriteAccessor.ALPHA).target(0).start(tweenManager);
        Tween.to(splash, SpriteAccessor.ALPHA, 1).target(1).repeatYoyo(1, 3).start(tweenManager).setCallback(new TweenCallback()
         {
             @Override
             public void onEvent(int i, BaseTween<?> baseTween)
             {
                 ((Game) Gdx.app.getApplicationListener()).setScreen(new MainMenu());
             }
         });
     }
 
     @Override
     public void hide()
     {
         //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public void pause()
     {
         //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public void resume()
     {
         //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public void dispose()
     {
         batch.dispose();
         splash.getTexture().dispose();
     }
 }
