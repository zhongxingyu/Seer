 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package no.hist.gruppe5.pvu.coderacer;
 
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.scenes.scene2d.Stage;
 import com.badlogic.gdx.scenes.scene2d.ui.Label;
 import com.badlogic.gdx.scenes.scene2d.ui.Skin;
 import no.hist.gruppe5.pvu.Assets;
 import no.hist.gruppe5.pvu.GameScreen;
 import no.hist.gruppe5.pvu.Input;
 import no.hist.gruppe5.pvu.PVU;
 import no.hist.gruppe5.pvu.mainroom.MinigameSelectorScreen;
 
 /**
  *
  * @author Frode
  */
 public class CoderacerEndScreen extends GameScreen {
 
    private String mPointText = "Points: ";
    private String mContinue = "Trykk SPACE for aa avslutte";
     private Label mLcontinue;
     private Label mPointTextLabel;
     private Label mPointValueLabel;
     private Stage mStage;
     private Label.LabelStyle mLabelstyle;
     private Skin mSkin;
     private Input mInput;
 
    public CoderacerEndScreen(PVU game, int points) {
         super(game);
 
         mInput = new Input(200L, 2000L);
 
         mStage = new Stage(PVU.SCREEN_WIDTH, PVU.SCREEN_HEIGHT, true);
         mStage = new Stage(PVU.SCREEN_WIDTH, PVU.SCREEN_HEIGHT, true);
         mPointTextLabel = makeHeadLabel(mPointText);
         mPointTextLabel.setPosition(PVU.SCREEN_WIDTH / 3, PVU.SCREEN_HEIGHT * 0.8f);
        mPointTextLabel.setFontScale(5f);
         mPointTextLabel.setWrap(true);
         mStage.addActor(mPointTextLabel);
 
        mPointValueLabel = makeHeadLabel(String.valueOf(points));
        mPointValueLabel.setPosition(PVU.SCREEN_WIDTH / 1.8f, PVU.SCREEN_HEIGHT * 0.8f);
        mPointValueLabel.setFontScale(5f);
         mPointValueLabel.setWrap(true);
         mStage.addActor(mPointValueLabel);
 
         mLcontinue = makeLabel(mContinue);
         mLcontinue.setPosition(PVU.SCREEN_WIDTH / 3, PVU.SCREEN_HEIGHT / 1.5f);
         mLcontinue.setFontScale(2f);
         mLcontinue.setWrap(true);
         mLcontinue.setLayoutEnabled(false);
         mStage.addActor(mLcontinue);
 
 
     }
 
     @Override
     protected void draw(float delta) {
         clearCamera(1, 1, 1, 1);
 
         batch.begin();
         batch.draw(Assets.msPcBackground, 0, 0);
         batch.end();
         mStage.draw();
     }
 
     @Override
     protected void update(float delta) {
         if (mInput.action()) {
             game.setScreen(new MinigameSelectorScreen(game));
         }
 
         if (mInput.isActionReady()) {
             mLcontinue.setLayoutEnabled(true);
         }
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
         mLabelstyle = new Label.LabelStyle(Assets.primaryFont10px, Color.GREEN);
         mLabelstyle.font = Assets.primaryFont10px;
         Label l = new Label(text, mLabelstyle);
         return l;
     }
 }
