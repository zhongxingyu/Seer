 package com.dfgames.lastplanet.difficulty;
 
 import com.badlogic.gdx.*;
 import com.badlogic.gdx.scenes.scene2d.ui.Table;
 import com.dfgames.lastplanet.Textures;
 import com.dfgames.lastplanet.Constants;
 import com.dfgames.lastplanet.LastPlanet;
 import com.dfgames.lastplanet.model.MovingBackground;
 import com.dfgames.lastplanet.screens.AbstractScreen;
 import com.dfgames.lastplanet.screens.game.GameScreen;
 import com.dfgames.lastplanet.utils.ButtonFactory;
 
 /**
  * Author: Ivan Melnikov
  * Date: 05.11.12 20:30
  */
 public class DifficultyScreen extends AbstractScreen {
     private MovingBackground background;
 
     public DifficultyScreen(final LastPlanet game) {
         super(game);
         background = new MovingBackground(Textures.background);
         initButtons();
     }
 
     private void initButtons() {
         Table table = new Table();
         table.setSize(Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);
 
         table.row().expand().padTop(60);
         table.add(ButtonFactory.makeButton(Textures.buttonEasy, Textures.buttonEasy, new SelectDifficultyAction("easy")));
         table.row().expand();
         table.add(ButtonFactory.makeButton(Textures.buttonNormal, Textures.buttonNormal, new SelectDifficultyAction("normal")));
         table.row().expand().padBottom(60);
         table.add(ButtonFactory.makeButton(Textures.buttonHard, Textures.buttonHard, new SelectDifficultyAction("hard")));
 
         stage.addActor(table);
     }
 
     @Override
     protected void update() {
         background.update();
     }
 
     @Override
     protected void draw() {
         background.draw(spriteBatch);
     }
 
     private class SelectDifficultyAction implements Runnable {
         private String difficulty;
 
         public SelectDifficultyAction(String difficulty) {
             this.difficulty = difficulty;
         }
 
         @Override
         public void run() {
             Preferences preferences = Gdx.app.getPreferences("last_planet");
             preferences.putString("difficulty", difficulty);
             preferences.flush();
             openScreen(new GameScreen(game));
         }
     }
 }
