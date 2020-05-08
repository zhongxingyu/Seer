 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package no.hist.gruppe5.pvu.visionshooter;
 
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.scenes.scene2d.Stage;
 import com.badlogic.gdx.scenes.scene2d.ui.Label;
 import no.hist.gruppe5.pvu.Assets;
 import no.hist.gruppe5.pvu.GameScreen;
 import no.hist.gruppe5.pvu.Input;
 import no.hist.gruppe5.pvu.PVU;
 
 /**
  *
  * @author Frode
  */
 public class VisionEndScreen extends GameScreen {
 
     private final String mPointText;
     private final String mContinue;
     private Label mLcontinue;
     private Label mPointTextLabel;
     private Stage mStage;
     private Label.LabelStyle mLabelstyle;
     private Input mInput;
     private int[] mElementsGot;
 
     public VisionEndScreen(PVU game, int points, int[] elementsGot) {
         super(game);
         mInput = new Input();
         mStage = new Stage(PVU.SCREEN_WIDTH, PVU.SCREEN_HEIGHT, true);
         mContinue = "Gratulerer! Du er nå ferdig å skrive visjonsdokumentet.\n\n" 
                 + "Trykk SPACE for å avslutte";
        mPointText = "Poeng: " + String.valueOf(points);
         createLabels();
 
         this.mElementsGot = elementsGot;
     }
 
     private void createLabels() {
         mLabelstyle = new Label.LabelStyle(Assets.primaryFont10px, Color.RED);
         mPointTextLabel = new Label(mPointText, mLabelstyle);
        mPointTextLabel.setPosition(PVU.SCREEN_WIDTH /2-200, PVU.SCREEN_HEIGHT * 0.6f);
         mPointTextLabel.setFontScale(7f);
         mPointTextLabel.setWrap(true);
         mStage.addActor(mPointTextLabel);
 
         mLabelstyle = new Label.LabelStyle(Assets.primaryFont10px, Color.BLACK);
         mLcontinue = new Label(mContinue, mLabelstyle);
         mLcontinue.setWidth(200);
         mLcontinue.setPosition(PVU.SCREEN_WIDTH/2-320, PVU.SCREEN_HEIGHT * 0.3f);
         mLcontinue.setFontScale(4f);
         mLcontinue.setWrap(true);
         
         mStage.addActor(mLcontinue);
     }
 
     private void drawElementsGui() {
         for(int i = 0; i < mElementsGot[0]; i++) {
             batch.draw(Assets.visionShooterDocumentRegion, 1 + (2 * (i * 4)), PVU.GAME_HEIGHT - 9, 7, 8);
         }
 
         for(int i = 0; i < mElementsGot[1]; i++) {
             batch.draw(Assets.visionShooterFacebookRegion, (PVU.GAME_WIDTH - 9) - (2 * (i * 4)), PVU.GAME_HEIGHT - 9, 7, 8);
         }
 
         for(int i = 0; i < mElementsGot[2]; i++) {
             batch.draw(Assets.visionShooterYoutubeRegion, (PVU.GAME_WIDTH - 9) - (2 * (i * 4)), PVU.GAME_HEIGHT - 18, 7, 8);
         }
     }
 
     @Override
     protected void draw(float delta) {
         clearCamera(1, 1, 1, 1);
         batch.begin();
         batch.draw(Assets.visionShooterRegion, 0, 0);
         drawElementsGui();
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
     }
 }
