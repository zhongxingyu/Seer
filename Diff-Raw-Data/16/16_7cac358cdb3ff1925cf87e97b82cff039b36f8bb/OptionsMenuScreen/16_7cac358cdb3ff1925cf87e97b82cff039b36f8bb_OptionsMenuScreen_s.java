 package com.icbat.game.tradesong.screens;
 
 /***/
 public class OptionsMenuScreen extends AbstractScreen {
 
     public OptionsMenuScreen() {
         super(false);
 

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
