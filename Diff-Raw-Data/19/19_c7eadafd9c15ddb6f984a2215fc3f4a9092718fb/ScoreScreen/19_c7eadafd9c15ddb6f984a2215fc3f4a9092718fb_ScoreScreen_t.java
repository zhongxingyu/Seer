 package no.hist.gruppe5.pvu.mainroom;
 
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.Pixmap;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.scenes.scene2d.Group;
 import com.badlogic.gdx.scenes.scene2d.Stage;
 import com.badlogic.gdx.scenes.scene2d.ui.Label;
 import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
 import no.hist.gruppe5.pvu.*;
 
 public class ScoreScreen extends GameScreen {
 
     private String[] mMinigameNames = {"Visionsdokument", "Kravdokument", "Sekvensdiagrammer", "Designdokument", "Implementasjon", "Minispill"};
     private Stage mStage;
     private Group mLabelGroup; 
 
     private Label headMinigames;
 
     private Label.LabelStyle labelstyle;
     private String mFinalGrade;
     private Label mFinalGradeLabel;
     private Label mGradeTextLabel;
     private Texture mPaperBackground;
     
     public ScoreScreen(PVU game) {
         super(game);
         mStage = new Stage(PVU.SCREEN_WIDTH, PVU.SCREEN_HEIGHT, true, batch);
         mLabelGroup = new Group();
         initiatePaper();
         initiateLabels();
 
         headMinigames = makeLabel(mMinigameNames[5]);
         headMinigames.setPosition(PVU.SCREEN_WIDTH / 3, PVU.SCREEN_HEIGHT * 0.9f);
         headMinigames.setFontScale(6f);
         headMinigames.setWrap(true);
         mStage.addActor(headMinigames);
 
         mFinalGrade = String.valueOf(ScoreHandler.getGrade());
         mFinalGradeLabel = makeLabel(mFinalGrade);
         mFinalGradeLabel.setPosition(PVU.SCREEN_WIDTH / 1.5f, PVU.SCREEN_HEIGHT * 0.1f);
         mFinalGradeLabel.setFontScale(8f);
         mStage.addActor(mFinalGradeLabel);
 
 
         mGradeTextLabel = makeLabel("Karakter: ");
         mGradeTextLabel.setPosition(PVU.SCREEN_WIDTH / 3.5f, PVU.SCREEN_HEIGHT * 0.1f);
         mGradeTextLabel.setFontScale(6f);
         mStage.addActor(mGradeTextLabel);
     }
 
     private void initiateLabels() {
         Label placeHolder;
         for (int i = 0; i < (mMinigameNames.length - 1)*2; i++) {
             if(i<mMinigameNames.length-1){
                 placeHolder = makeLabel(mMinigameNames[i]);
                 placeHolder.setPosition(PVU.SCREEN_WIDTH / 3.5f, PVU.SCREEN_HEIGHT * (0.7f-(i/10f)));
             }else{
                 placeHolder = makeLabel("" + Math.round(ScoreHandler.getMiniGameGrade(i-5) * 100f) + "/100");
                 placeHolder.setPosition(PVU.SCREEN_WIDTH / 1.5f, PVU.SCREEN_HEIGHT * (0.7f-((i-5)/10f)));
             }
             placeHolder.setFillParent(true);
             placeHolder.setFontScale(3f);
             placeHolder.setWrap(true);
             mLabelGroup.addActor(placeHolder);
         }
         mStage.addActor(mLabelGroup);
     }
     
     private void initiatePaper(){
         Pixmap pix = new Pixmap(32, 32, Pixmap.Format.RGBA8888);
         pix.setColor(new Color(1f, 1f, 1f, 1f));
         pix.fill();
         pix.setColor(Color.BLACK);
         pix.drawRectangle(0, -20, 32, 56);
 
         mPaperBackground = new Texture(pix);
     }
 
     @Override
     protected void draw(float delta) {
         clearCamera(0.729f, 0.729f, 0.729f, 1);
 
         batch.begin();
         batch.draw(mPaperBackground, 30f, 0, PVU.GAME_WIDTH-60f, PVU.GAME_HEIGHT);
         //stage.getSpriteBatch().draw(Assets.msPcBackground, 0, 0);
         batch.end();
         mStage.draw();
     }
 
     @Override
     protected void update(float delta) {
     }
 
     @Override
     protected void cleanUp() {
     }
 
     private Label makeLabel(String text) {
         labelstyle = new LabelStyle(Assets.primaryFont10px, Color.BLACK);
         Label l = new Label(text, labelstyle);
         return l;
     }
 }
