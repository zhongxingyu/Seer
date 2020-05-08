 package com.willbat.MotherlAndroid;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Screen;
 import com.badlogic.gdx.graphics.GL20;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 
 import java.lang.reflect.GenericArrayType;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Jobat
  * Date: 22/08/13
  * Time: 23:36
  * To change this template use File | Settings | File Templates.
  */
 public class MLTileTestScreen implements Screen {
     MLCore game;
     SpriteBatch batch;
     Map map;
     OrthographicCamera camera;
     //Renderer renderer;
     GenericArrayType drawable;
 
     public MLTileTestScreen(MLCore game) {
         this.game = game;
         camera = new OrthographicCamera(Gdx.graphics.getWidth(),Gdx.graphics.getHeight());
         camera.setToOrtho(false);
         camera.update();
         batch = new SpriteBatch();
         map = new Map(10,10);
         //renderer = new Renderer();
     }
 
     @Override
     public void render(float delta) {
         handleInput();
         Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
         Gdx.gl20.glClearColor(100f / 255, 149f / 255, 237f / 255, 1.0F);
         camera.update();
         batch.setProjectionMatrix(camera.combined);
         batch.begin();
         map.draw(batch);
         batch.end();
     }
 
     @Override
     public void resize(int width, int height) {
 
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
 
     }
 
     private void handleInput()
     {
         if (Gdx.input.isTouched())
         {
             camera.position.set(Gdx.input.getX(), Gdx.input.getY(), 0);
         }
     }
 }
