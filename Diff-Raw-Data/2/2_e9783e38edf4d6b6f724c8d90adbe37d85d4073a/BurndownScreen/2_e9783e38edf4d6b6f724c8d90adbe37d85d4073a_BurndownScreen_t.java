 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package no.hist.gruppe5.pvu.mainroom;
 
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.scenes.scene2d.Stage;
 import com.badlogic.gdx.scenes.scene2d.ui.Label;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 
 import no.hist.gruppe5.pvu.*;
 
 /**
  *
  * @author Frode
  */
 public class BurndownScreen extends GameScreen {
 
     private Sprite[] mBurndownCarts;
     private int mCurrentCart = 0;
     private String mText;
     private Label mTextLabel;
     private Label.LabelStyle mLabelstyle;
     private Stage mStage;
     private Input mInput;
 
     public BurndownScreen(PVU game) {
         super(game);
         mStage = new Stage(PVU.SCREEN_WIDTH, PVU.SCREEN_HEIGHT, true);
         mInput = new Input();
 
         try {
             mText = Assets.readFile("data/burndownScreen/text.txt");
 
         } catch (FileNotFoundException e) {
         } catch (IOException e) {
         }
 
         mLabelstyle = new Label.LabelStyle(Assets.primaryFont10px, Color.BLACK);
         mTextLabel = new Label(mText, mLabelstyle);
         mTextLabel.setPosition(PVU.SCREEN_WIDTH / 4, PVU.SCREEN_HEIGHT / 5.3f);
         mTextLabel.setWrap(true);
         mTextLabel.setWidth(PVU.SCREEN_WIDTH / 5);
         mTextLabel.setFontScale(3f);
 
         mStage.addActor(mTextLabel);
         mBurndownCarts = new Sprite[Assets.msBurndownCarts.length];
         for (int i = 0; i < Assets.msBurndownCarts.length; i++) {
             mBurndownCarts[i] = new Sprite(Assets.msBurndownCarts[i]);
             mBurndownCarts[i].setPosition(PVU.GAME_WIDTH / 2.5f - mBurndownCarts[i].getWidth(), PVU.GAME_HEIGHT / 3);
            mBurndownCarts[i].setSize(PVU.GAME_WIDTH / 2, PVU.GAME_HEIGHT / 1.6f);
         }
         checkCompletion();
     }
 
     @Override
     protected void draw(float delta) {
         clearCamera(1f, 0.961f, 0.769f, 1);
         batch.begin();
         mBurndownCarts[mCurrentCart].draw(batch);
         batch.end();
         mStage.draw();
     }
 
     @Override
     protected void update(float delta) {
 
         if (mInput.action()) {
             game.setScreen(PVU.MAIN_SCREEN);
         }
     }
 
     @Override
     protected void cleanUp() {
         throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
     }
 
     private void setBurnDownCart(int num) {
         if (num < 0) {
             num = 0;
         }
         if (num > 4) {
             num = 4;
         }
         mCurrentCart = num;
     }
 
     private void checkCompletion() {
         if (ScoreHandler.isMinigameCompleted(ScoreHandler.VISION)) {
             setBurnDownCart(++mCurrentCart % 5);
         } if (ScoreHandler.isMinigameCompleted(ScoreHandler.REQ)) {
             setBurnDownCart(++mCurrentCart % 5);
         }  if (ScoreHandler.isMinigameCompleted(ScoreHandler.UMLBLOCKS)) {
              setBurnDownCart(++mCurrentCart % 5);
            
         } if (ScoreHandler.isMinigameCompleted(ScoreHandler.CODE)) {
              setBurnDownCart(++mCurrentCart % 5);
            
         }
     }
 }
