 package com.icbat.game.tradesong.stages;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.BitmapFont;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.scenes.scene2d.Actor;
 import com.badlogic.gdx.scenes.scene2d.InputEvent;
 import com.badlogic.gdx.scenes.scene2d.Touchable;
 import com.badlogic.gdx.scenes.scene2d.ui.Image;
 import com.badlogic.gdx.scenes.scene2d.ui.Label;
 import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
 import com.icbat.game.tradesong.Tradesong;
 import com.icbat.game.tradesong.screens.InventoryScreen;
 import com.icbat.game.tradesong.screens.StoreScreen;
 import com.icbat.game.tradesong.screens.WorkshopScreen;
 
 public class HUDStage extends AbstractStage {
     public static final int SPACER = 6;
     public static final int ICON_SIZE = 34;
 
     /** As of now, this is pulled from items.png */
     Texture itemsTexture;
 
     private Tradesong gameInstance;
     private Label capacityCounter;
     private Label.LabelStyle textStyle;
 
     private Actor dragCatcher = new Actor();
 
     public HUDStage(Tradesong gameInstance) {
         this.itemsTexture = Tradesong.assets.get(Tradesong.getItemsPath());
         this.gameInstance = gameInstance;
 
         layout();
     }
 
     public void layout() {
         this.clear();
         addWorkshopsButton();
         addInventoryButton();
         addCapacityCounter();
 
         addMoney();
 
         addCharacterPortrait();
 
         dragCatcher.setBounds(0,0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
         dragCatcher.setTouchable(Touchable.enabled);
         this.addActor(dragCatcher);
        dragCatcher.setZIndex(0);
     }
 
     public Actor getDragCatcher() {
         return dragCatcher;
     }
 
     private void addMoney() {
         Image coin = new Image(Tradesong.getCoinTexture());
 
         layoutHorizontally(coin);
         coin.addListener(new InterfaceButtonListener(StoreScreen.class, gameInstance));
         this.addActor(coin);
 
         Label.LabelStyle style = new Label.LabelStyle();
         style.font = new BitmapFont();
         Label moneyCounter = new Label("-1", style) {
             @Override
             public void draw(SpriteBatch batch, float parentAlpha) {
 
                 this.setText(Tradesong.gameState.getMoney()+"");
 
                 super.draw(batch, parentAlpha);
             }
         };
 
         layoutHorizontally(moneyCounter);
         this.addActor(moneyCounter);
 
 
 
     }
 
     private void addInventoryButton() {
         int x = 7;
         int y = 29;
         Image inventoryButton = new Image(new TextureRegion(itemsTexture, x * ICON_SIZE, y * ICON_SIZE, ICON_SIZE, ICON_SIZE));
 
         inventoryButton.setTouchable(Touchable.enabled);
         inventoryButton.addListener(new InterfaceButtonListener(InventoryScreen.class, gameInstance));
         layoutHorizontally(inventoryButton);
         inventoryButton.setVisible(true);
         this.addActor(inventoryButton);
     }
 
     private void addCapacityCounter() {
 
         Integer capacity = Tradesong.gameState.getInventory().capacity();
         Integer size = Tradesong.gameState.getInventory().size();
 
         textStyle = new Label.LabelStyle();
 
         textStyle.font = new BitmapFont();
 
         capacityCounter = new Label(size.toString() + " / " + capacity.toString(), textStyle) {
             @Override
             public void draw(SpriteBatch batch, float parentAlpha) {
                 Integer capacity = Tradesong.gameState.getInventory().capacity();
                 Integer size = Tradesong.gameState.getInventory().size();
 
                 if (size.equals(capacity)) {
                     textStyle.font.setColor(Color.RED);
                 }
                 else if (size.equals(capacity - 3)) {
                     textStyle.font.setColor(Color.ORANGE);
                 }
                 else {
                     textStyle.font.setColor(Color.WHITE);
                 }
 
                 capacityCounter.setText(size.toString() + " / " + capacity.toString());
 
                 super.draw(batch, parentAlpha);
             }
         };
 
         capacityCounter.setWidth(36);
 
         layoutHorizontally(capacityCounter);
         capacityCounter.layout();
         this.addActor(capacityCounter);
     }
 
     private void addWorkshopsButton() {
         int x = 3;
         int y = 9;
 
 
         Image workshopButton = new Image(new TextureRegion(itemsTexture, x * ICON_SIZE, y * ICON_SIZE, ICON_SIZE, ICON_SIZE));
 
         workshopButton.setTouchable(Touchable.enabled);
         workshopButton.addListener(new InterfaceButtonListener(WorkshopScreen.class, gameInstance));
 
         layoutHorizontally(workshopButton);
 
         this.addActor(workshopButton);
     }
 
     private void addCharacterPortrait() {
         Image character = new Image(  new TextureRegion(Tradesong.getCharacterTexture(), 100, 70)  );
         character.setPosition(this.getWidth() - character.getWidth() - SPACER,0);
         this.addActor(character);
     }
 
 
 
     private void layoutHorizontally(Actor actorToLayout) {
         int furthestX = 0;
         for (Actor actor : this.getActors()) {
             if (actor.getRight() > furthestX)
                 furthestX = (int)actor.getRight();
         }
         actorToLayout.setPosition(furthestX + SPACER, 0);
 
     }
 
     class InterfaceButtonListener extends ClickListener {
         private Class landingPage;
         private Tradesong gameInstance;
 
         InterfaceButtonListener(Class landingPage, Tradesong gameInstance) {
             super();
             this.landingPage = landingPage;
             this.gameInstance = gameInstance;
         }
 
         @Override
         public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
             super.touchDown(event, x, y, pointer, button);
             // Following error is a known bug in IDEA, not an actual problem
             if (gameInstance.getCurrentScreen().getClass().equals(landingPage)) {
                 // If we're already on that screen
                 gameInstance.goBack();
             }
             else {
                 // Find the screen to go to based on landing
                 if (landingPage.equals(InventoryScreen.class)) {
 
                     gameInstance.goToInventory();
                 }
                 else if (landingPage.equals(WorkshopScreen.class)) {
                     gameInstance.goToWorkshop();
 
                 } else if (landingPage.equals(StoreScreen.class)) {
                     gameInstance.goToStore();
                 }
             }
 
             return true;
         }
     }
 }
