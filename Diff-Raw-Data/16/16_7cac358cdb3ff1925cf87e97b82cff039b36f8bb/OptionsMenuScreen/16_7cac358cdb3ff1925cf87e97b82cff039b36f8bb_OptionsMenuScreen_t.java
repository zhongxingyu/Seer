 package com.icbat.game.tradesong.screens;
 
import com.icbat.game.tradesong.screens.stages.OptionsMenuStage;

 /***/
 public class OptionsMenuScreen extends AbstractScreen {
 
     public OptionsMenuScreen() {
         super(false);
 
        stages.add(new OptionsMenuStage());
     }
 
     @Override
     public void render(float delta) {
         drawBackground(0.4f, 0.4f, 0.4f, 1);
         renderStages(delta);
     }
 
     @Override
     public String getScreenName() {
         return "optionsScreen";
     }
 }
