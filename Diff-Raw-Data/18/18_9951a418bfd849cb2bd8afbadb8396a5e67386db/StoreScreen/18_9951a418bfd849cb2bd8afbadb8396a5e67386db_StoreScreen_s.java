 package com.icbat.game.tradesong.screens;
 
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
 import com.icbat.game.tradesong.stages.HUDStage;
 import com.icbat.game.tradesong.stages.InventoryStage;
 
 public class StoreScreen extends AbstractScreen {
 
 
     public StoreScreen(HUDStage hud, InventoryStage inventoryStage) {
         super();
         stages.add(hud);
         stages.add(inventoryStage);
         ((InventoryStage)stages.get(1)).setIsInStore(false);
 
         // Setup an input Multiplexer
         inputMultiplexer.addProcessor(hud);
         inputMultiplexer.addProcessor(inventoryStage);
 
 
 
     }
 
     @Override
     public void render(float delta) {
         super.render(delta, 0.25f, 0.33f, 1, 1);
     }
 
     @Override
     public void show() {
         super.show();
         ((InventoryStage)stages.get(1)).setIsInStore(true);
     }
 
     @Override
     public void hide() {
         super.hide();
         ((InventoryStage)stages.get(1)).setIsInStore(false);
     }
 }
