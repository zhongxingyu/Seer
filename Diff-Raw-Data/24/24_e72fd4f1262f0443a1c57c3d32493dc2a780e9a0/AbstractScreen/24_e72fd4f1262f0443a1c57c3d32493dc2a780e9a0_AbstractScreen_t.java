 package com.icbat.game.tradesong.screens;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.InputMultiplexer;
 import com.badlogic.gdx.Screen;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.scenes.scene2d.Actor;
 import com.badlogic.gdx.scenes.scene2d.Stage;
 import com.icbat.game.tradesong.screens.stages.BaseStage;
 import com.icbat.game.tradesong.screens.stages.HUD;
 import com.icbat.game.tradesong.screens.stages.PopupStage;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 /**
  * Generic abstraction of things shared by all screens
  */
 public abstract class AbstractScreen implements Screen {
 
     protected List<BaseStage> stages = new ArrayList<BaseStage>();
     InputMultiplexer inputMultiplexer = new InputMultiplexer();
 
     public AbstractScreen() {
 
     }
 
     public AbstractScreen(BaseStage... specificStages) {
         this.setupStages(specificStages);
     }
 
     @Override
     public void render(float delta) {
         drawBackground(0.1f, 0.1f, 0.1f, 1);
         renderStages(delta);
     }
 
     protected void renderStages(float delta) {
         for (BaseStage stage : stages) {
             stage.act(delta);
             stage.onRender();
             stage.draw();
         }
     }
 
     final void drawBackground(float r, float g, float b, float a) {
         Gdx.gl.glClearColor(r, g, b, a);
         Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
     }
 
     @Override
     public void dispose() {
         for (Stage stage : stages) {
             stage.dispose();
         }
 
     }
 
     /**
      * This method does nothing, but must be here to allow the children to not implement this.
      */
     @Override
     public void hide() {
         for (BaseStage stage : stages) {
             stage.hide();
         }
     }
 
     /**
      * This method does nothing, but must be here to allow the children to not implement this.
      */
     @Override
     public void pause() {
 
     }
 
     /**
      * This method does nothing, but must be here to allow the children to not implement this.
      */
     @Override
     public void resume() {
 
     }
 
     @Override
     public void resize(int width, int height) {
         for (BaseStage stage : stages) {
             stage.setViewport(width, height, false);
             stage.layout();
         }
     }
 
     @Override
     public void show() {
         setupInputMultiplexer();
     }
 
     protected void setupInputMultiplexer() {
         inputMultiplexer.clear();
         for (Stage stage : stages) {
             inputMultiplexer.addProcessor(stage);
         }
 
         Gdx.input.setInputProcessor(inputMultiplexer);
         Gdx.app.debug("setup the input multiplexer", inputMultiplexer.toString());
     }
 
     protected void setupStages(BaseStage... stages) {
        disposeAllStages(this.stages);
         this.stages = new ArrayList<BaseStage>();
         this.stages.add(new HUD());
         Collections.addAll(this.stages, stages);
         this.stages.add(new PopupStage());
         this.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()); // TODO make this logic more clear.
     }
 
    private void disposeAllStages(List<BaseStage> stages) {
        if (stages != null) {
            for (BaseStage stage : stages) {
                stage.dispose();
            }
        }
    }


     public abstract String getScreenName();
 
     /** Allows adding of popups from other things. Adds the Actor to the first stage in the list (usually HUD) */
     public void addPopup(Actor actor) {
         stages.get(stages.size() - 1).addActor(actor);
     }
 
 }
