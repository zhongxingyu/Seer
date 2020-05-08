 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package no.hist.gruppe5.pvu.visionshooter;
 
 import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.scenes.scene2d.Stage;
 import com.badlogic.gdx.scenes.scene2d.ui.Label;
 import com.badlogic.gdx.scenes.scene2d.ui.Skin;
 import no.hist.gruppe5.pvu.Assets;
 import no.hist.gruppe5.pvu.GameScreen;
 import no.hist.gruppe5.pvu.PVU;
 import no.hist.gruppe5.pvu.mainroom.MinigameSelectorScreen;
 
 /**
  *
  * @author Frode
  */
 public class VisionEndScreen extends GameScreen {
 
     private String mPointText = "Points: ";
     private String mContinue = "Trykk SPACE for aa avslutte";
     private Label mLcontinue;
     private Label mPointTextLabel;
     private Label mPointValueLabel;
     private Stage mStage;
     private Label.LabelStyle mLabelstyle;
     private Skin mSkin;
 
     public VisionEndScreen(PVU game, int points) {
         super(game);
         mStage = new Stage(PVU.SCREEN_WIDTH, PVU.SCREEN_HEIGHT, true, batch);
         mPointTextLabel = makeHeadLabel(mPointText);
         mPointTextLabel.setPosition(PVU.SCREEN_WIDTH / 6, PVU.SCREEN_HEIGHT * 0.8f);
         mPointTextLabel.setFontScale(7f);
         mPointTextLabel.setWrap(true);
         mStage.addActor(mPointTextLabel);
 
         mPointValueLabel = makeLabel(String.valueOf(points));
         mPointValueLabel.setPosition(PVU.SCREEN_WIDTH / 2, PVU.SCREEN_HEIGHT * 0.8f);
         mPointValueLabel.setFontScale(7f);
         mPointValueLabel.setWrap(true);
         mStage.addActor(mPointValueLabel);
 
         mLcontinue = makeLabel(mContinue);
         mLcontinue.setPosition(PVU.SCREEN_WIDTH / 9, PVU.SCREEN_HEIGHT / 2);
         mLcontinue.setFontScale(5f);
         mLcontinue.setWrap(true);
         mStage.addActor(mLcontinue);
         
 
     }
 
     @Override
     protected void draw(float delta) {
         clearCamera(1, 1, 1, 1);
 
         batch.begin();
         mStage.getSpriteBatch().draw(Assets.visionShooterRegion, 0, 0);
         batch.end();
         mStage.draw();
     }
 
     @Override
     protected void update(float delta) {
     }
 
     @Override
     protected void cleanUp() {
         throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
     }
 
     private Label makeHeadLabel(String text) {
         mSkin = new Skin();
         mLabelstyle = new Label.LabelStyle(Assets.primaryFont10px, Color.BLACK);
         mLabelstyle.font = Assets.primaryFont10px;
         mLabelstyle.fontColor = Color.RED;
         Label l = new Label(text, mLabelstyle);
         return l;
     }
 
     private Label makeLabel(String text) {
         mSkin = new Skin();
         mLabelstyle = new Label.LabelStyle(Assets.primaryFont10px, Color.BLACK);
         mLabelstyle.font = Assets.primaryFont10px;
         Label l = new Label(text, mLabelstyle);
         return l;
     }
 
   
 }
