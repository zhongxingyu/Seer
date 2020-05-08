 package com.icbat.game.tradesong.screens.stages;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.scenes.scene2d.Actor;
 import com.badlogic.gdx.scenes.scene2d.Group;
 import com.badlogic.gdx.scenes.scene2d.Touchable;
 import com.badlogic.gdx.scenes.scene2d.ui.Image;
 import com.badlogic.gdx.scenes.scene2d.ui.Label;
 import com.badlogic.gdx.scenes.scene2d.ui.Table;
 import com.badlogic.gdx.scenes.scene2d.utils.Align;
 import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
 import com.icbat.game.tradesong.Tradesong;
 import com.icbat.game.tradesong.assetReferences.TextureAssets;
 import com.icbat.game.tradesong.screens.*;
 import com.icbat.game.tradesong.screens.listeners.GoToScreenListener;
 import com.icbat.game.tradesong.utils.SpacingActor;
 
 /**
  * Common heads-up display for play.
  */
 public class HUD extends BaseStage {
 
    private Label capacityCounter = new Label("", Tradesong.uiStyles.getBlankLabelStyle());
     private Label clock = new Label("", Tradesong.uiStyles.getLabelStyle());
     private Label money = new Label("", Tradesong.uiStyles.getLabelStyle());
 
     @Override
     public void layout() {
         this.clear();
         Group holder = new Group();
         Table tableLayout = new Table();
 
         tableLayout.add(inventoryButton());
         tableLayout.add(new SpacingActor());
         tableLayout.add(capacityCounter);
         tableLayout.add(new SpacingActor());
         tableLayout.add(craftingButton());
         tableLayout.add(new SpacingActor());
         tableLayout.add(shipmentBoxButton());
         tableLayout.add(new SpacingActor());
         tableLayout.add(contractsButton());
         tableLayout.add(new SpacingActor(20, 4));
         tableLayout.add(makeMoneyIcon());
         tableLayout.add(new SpacingActor());
         tableLayout.add(money);
         tableLayout.add(new SpacingActor(100,4));
         tableLayout.add(clock);
 
         setupHolderAndTable(holder, tableLayout);
 
         holder.addActor(tableLayout);
         holder.addActor(menuButton());
         this.addActor(holder);
     }
 
     private Actor makeMoneyIcon() {
 
         return new Image(TextureAssets.ITEMS.getRegion(4, 30));
     }
 
     @Override
     public void onRender() {
         setCapacityCounter();
         setClock();
         setMoney();
     }
 
     private void setMoney() {
         money.setText(Tradesong.inventory.getMoney().toString());
     }
 
     private void setupHolderAndTable(Group holder, Table tableLayout) {
         holder.setHeight(40);
         holder.setWidth(Gdx.graphics.getWidth()); // Won't do much on resize
 
         tableLayout.setFillParent(true);
         tableLayout.setBackground(new TextureRegionDrawable(new TextureRegion(Tradesong.getTexture(TextureAssets.HUD_BG))));
         tableLayout.align(Align.bottom);
         tableLayout.setColor(0.2f, 0.2f, 0.2f, 1f);
     }
 
     private Actor inventoryButton() {
         Image inventoryButton = new Image(TextureAssets.ITEMS.getRegion(2, 30));
         inventoryButton.setTouchable(Touchable.enabled);
         inventoryButton.addListener(new GoToScreenListener() {
             @Override
             protected void goToTargetScreen() {
                 Tradesong.screenManager.goToScreen(new InventoryScreen());
             }
         });
         return inventoryButton;
     }
 
 
     private Actor menuButton() {
         Image menuButton = new Image(TextureAssets.ITEMS.getRegion(0, 29));
         menuButton.setTouchable(Touchable.enabled);
         menuButton.addListener(new GoToScreenListener() {
             @Override
             protected void goToTargetScreen() {
                 Tradesong.screenManager.goToScreen(new OptionsMenuScreen());
             }
         });
         return menuButton;
     }
 
 
 
     private Actor craftingButton() {
         Image craftingButton = new Image(TextureAssets.ITEMS.getRegion(3, 9));
         craftingButton.setTouchable(Touchable.enabled);
         craftingButton.addListener(new GoToScreenListener() {
             @Override
             protected void goToTargetScreen() {
                 Tradesong.screenManager.goToScreen(new CraftingScreen());
             }
         });
         return craftingButton;
     }
 
     private Actor shipmentBoxButton() {
         Image shipmentBoxButton = new Image(TextureAssets.ITEMS.getRegion(10, 30));
         shipmentBoxButton.setTouchable(Touchable.enabled);
         shipmentBoxButton.addListener(new GoToScreenListener() {
             @Override
             protected void goToTargetScreen() {
                 Tradesong.screenManager.goToScreen(new ShippingScreen());
             }
         });
         return shipmentBoxButton;
     }
 
     private Actor contractsButton() {
         Image contractsButton = new Image(TextureAssets.ITEMS.getRegion(19, 1));
         contractsButton.setTouchable(Touchable.enabled);
         contractsButton.addListener(new GoToScreenListener() {
             @Override
             protected void goToTargetScreen() {
                 Tradesong.screenManager.goToScreen(new ContractsScreen());
             }
         });
         return contractsButton;
     }
 
     void setCapacityCounter() {
         int slotsFree = Tradesong.inventory.getMaxSize() - Tradesong.inventory.getCurrentSize();
         if (slotsFree == 0) {
             capacityCounter.setColor(Color.RED);
         } else if (slotsFree < 5) {
             capacityCounter.setColor(Color.YELLOW);
         } else {
             capacityCounter.setColor(Color.GREEN);
         }
         capacityCounter.setText(String.valueOf(slotsFree));
     }
 
     void setClock() {
         int timeInMinutes = Tradesong.clock.getTime();
         timeInMinutes += 6;
         String formattedTime = timeInMinutes + " hours";
         clock.setText(formattedTime);
     }
 }
