 package no.hist.gruppe5.pvu.dialogdrawer;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.scenes.scene2d.Stage;
 import com.badlogic.gdx.scenes.scene2d.ui.Label;
 import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
 import com.badlogic.gdx.scenes.scene2d.ui.Skin;
 import com.badlogic.gdx.scenes.scene2d.ui.TextField;
 import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldStyle;
 import no.hist.gruppe5.pvu.Assets;
 import no.hist.gruppe5.pvu.Input;
 import no.hist.gruppe5.pvu.PVU;
 
 public class DialogDrawer {
 
     // Stage
     Stage mStage;
 
     // Text
     private final String LINE_1 = "Hei, og velkommen til PVU!";
     private final String LINE_2 = "Her kan du lære om programutvikling ved hjelp av SCRUM.";
     private final String LINE_3 = "Hvis du er nybegynner foreslår vi at du leser introduksjonen i Instruksjonsboka!";
     private final String LINE_4 = "Lykke til på ferden til å bli en programutvikler!";
 
     // Actors
     private TextField mTextBackground;
     private Label mMainText;
     private Label mPressEnter;
 
     // Navigation
     private Input mInput;
     private int mTextCounter = 0;
     private boolean mShow;
 
     public DialogDrawer() {
         this.mShow = false;
 
        mInput = new Input();

         init();
     }
 
     private void init() {
         mStage = new Stage(PVU.SCREEN_WIDTH, PVU.SCREEN_HEIGHT, true);
 
         Skin textboxSkin = new Skin();
         Texture texture = new Texture(Gdx.files.internal("data/DialogTexture.png"));
         textboxSkin.add("textfieldback", new TextureRegion(texture, 1, 1, 190, 56));
         TextFieldStyle textFieldStyle = new TextFieldStyle();
         textFieldStyle.font = Assets.primaryFont10px;
         textFieldStyle.background = textboxSkin.getDrawable("textfieldback");
 
         mTextBackground = new TextField("", textFieldStyle);
         mTextBackground.setHeight(PVU.SCREEN_HEIGHT / 3);
         mTextBackground.setWidth(PVU.SCREEN_WIDTH - 50f);
         mTextBackground.setPosition(25f, 25f);
 
         LabelStyle labelStyle = new LabelStyle(Assets.primaryFont10px, Color.BLACK);
 
         mMainText = new Label(LINE_1, labelStyle);
         mMainText.setWrap(true);
         mMainText.setFontScale(3f);
         mMainText.setSize(280, 100);
         mMainText.setPosition(50, 70);
 
         mPressEnter = new Label(">Space", labelStyle);
         mPressEnter.setFontScale(2f);
         mPressEnter.setSize(280, 100);
         mPressEnter.setPosition(840, 5);
 
         mStage.addActor(mTextBackground);
         mStage.addActor(mMainText);
         mStage.addActor(mPressEnter);
 
     }
 
     public void draw() {
         mStage.draw();
     }
 
     public void update() {
         if (mInput.action()) {
             mTextCounter++;
             if (mTextCounter == 1) {
                 mMainText.setText(LINE_2);
             }
             if (mTextCounter == 2) {
                 mMainText.setText(LINE_3);
             }
             if (mTextCounter == 3) {
                 mMainText.setText(LINE_4);
             }
             if (mTextCounter == 4) {
                 mMainText.setText("");
                 setShow(false);
             }
         }
     }
 
     public void setShow(boolean show) {
         this.mShow = show;
     }
 
     public boolean isShow() {
         return mShow;
     }
 }
