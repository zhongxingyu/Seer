 package com.icbat.game.tradesong.screens;
 
 import com.icbat.game.tradesong.Tradesong;
 import com.icbat.game.tradesong.stages.WorkshopStage;
 
 public class WorkshopScreen extends InventoryScreen{
 
     private WorkshopStage workshopStage;
 
     public WorkshopScreen(Tradesong gameInstance) {
         super(gameInstance);
 
         workshopStage = new WorkshopStage(gameInstance);
         inputMultiplexer.addProcessor(workshopStage);
 
     }
 
     @Override
     public void show() {
         super.show();
         inputMultiplexer.addProcessor(workshopStage);
     }
 
     @Override
     public void render(float delta) {
        super.render(delta);    //To change body of overridden methods use File | Settings | File Templates.
     }
 }
