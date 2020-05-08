 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package no.hist.gruppe5.pvu.quiz;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.Pixmap;
 import com.badlogic.gdx.graphics.Pixmap.Format;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.BitmapFont;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.scenes.scene2d.Group;
 import com.badlogic.gdx.scenes.scene2d.Stage;
 import com.badlogic.gdx.scenes.scene2d.ui.Button;
 import com.badlogic.gdx.scenes.scene2d.ui.Label;
 import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
 import com.badlogic.gdx.scenes.scene2d.ui.Skin;
 import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
 import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
 import com.badlogic.gdx.scenes.scene2d.utils.Align;
 import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
 import com.badlogic.gdx.utils.TimeUtils;
 import java.io.BufferedReader;
 import java.io.DataInputStream;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import no.hist.gruppe5.pvu.Assets;
 import no.hist.gruppe5.pvu.GameScreen;
 import no.hist.gruppe5.pvu.Input;
 import no.hist.gruppe5.pvu.PVU;
 import no.hist.gruppe5.pvu.ScoreHandler;
 import no.hist.gruppe5.pvu.umlblocks.ScrollingBackground;
 
 public class QuizScreen extends GameScreen {
 
     // QUIZ
     private String[] mQuizNames = {"data/Quizes/quiz_00.txt", "data/Quizes/quiz_01.txt", "data/Quizes/quiz_02.txt", "data/Quizes/quiz_03.txt", "data/Quizes/quiz_04.txt"};
     private final int mNumberOfQuestions = mQuizNames.length;
     private ArrayList<Label> mQuestions = new ArrayList();
     private ArrayList<TextButton> mAnswers = new ArrayList();
 
     // Quiz variables
     private int[] mAnswersNumbered = new int[mNumberOfQuestions];
     private boolean mSelectorLeft = true;
     private boolean mSelectorTop = true;
     private boolean mQuizDone = false;
     private int mSelectorX = 0;
     private int mSelectorY = 105;
     private int mQuestionCounter = 0;
     private int mNumberOfCorrectAnswers = 0;
     private int mAnswer = -1;
     private long mLastButtonPressed = 0;
     private boolean mGetNewAnswers = false;
     private int mQuizNumber;
 
     // GUI
     private Stage mStage;
     private Group mQuestionGroup;
     private Group mAnswerGroup;
     private Skin mQuizSkin = new Skin();
     private LabelStyle mOutputStyle = new LabelStyle(Assets.primaryFont10px, Color.BLACK);
     private TextButtonStyle mAnswerStyle = new TextButtonStyle();
    private TextButtonStyle mAnswerStyleCorrect;
    private TextButtonStyle mAnswerStyleWrong;
     private Button mSelector = new Button();
     private ScrollingBackground mBackground;
 
     // Input
     private Input mInput;
 
     public QuizScreen(PVU game, int mQuizNumber) throws IOException {
         super(game);
         this.mQuizNumber = mQuizNumber;
         mStage = new Stage();
         mQuestionGroup = new Group();
         mAnswerGroup = new Group();
         mInput = new Input(75L,1500L);
         mBackground = new ScrollingBackground(Assets.quizBg);
         defineStyles();
         readQuiz(mQuizNames[mQuizNumber]);
         initializeQuestions();
         initializeAnswers();
 
         mStage.addActor(mQuestionGroup);
         mStage.addActor(mAnswerGroup);
 
         Gdx.input.setInputProcessor(mStage);
     }
 
     @Override
     protected void draw(float delta) {
         clearCamera(1, 1, 1, 1);
         batch.begin();
         mBackground.draw(batch);
         batch.end();
         mStage.draw();
     }
 
     @Override
     protected void update(float delta) {
         if (!mQuizDone) {
             if (!mGetNewAnswers) {
                 initiateSelectorBounds();
                 registerSelectorAnswer();
             }
            if (mGetNewAnswers) {
                 initNewAnswers();
             }
             if (mQuestionCounter == mNumberOfQuestions) {
                 presentQuizScore();
             }
         } else if (mInput.action()) {
             endQuiz();
         }
 
         mBackground.update(delta);
     }
 
     @Override
     protected void cleanUp() {
         mStage.clear();
     }
 
     private void changeColor(TextButton button, TextButtonStyle style) {
         button.setStyle(style);
     }
 
     private void readQuiz(String fileName) throws FileNotFoundException, IOException {
         DataInputStream in = new DataInputStream(new FileInputStream(fileName));
         BufferedReader inBR = new BufferedReader(new InputStreamReader(in));
         String strLine;
         int counter = 0;
         int answerCounter = 0;
         while ((strLine = inBR.readLine()) != null) {
             if (!"".equals(strLine)) {
                 if (counter < mNumberOfQuestions) {
                     mQuestions.add(new Label(strLine, mOutputStyle));
                 } else if (counter < (mNumberOfQuestions * 4 + 10)) {
                     mAnswers.add(new TextButton(strLine, mAnswerStyle));
                 } else {
                     mAnswersNumbered[answerCounter] = Integer.parseInt(strLine);
                     answerCounter++;
                 }
             }
             counter++;
         }
     }
 
     private void defineStyles() {
         Pixmap pixmap = new Pixmap(1, 1, Format.RGBA8888);
         pixmap.setColor(Color.DARK_GRAY);
         pixmap.fill();
 
         mQuizSkin.add("Gray", new Texture(pixmap));
 
         pixmap.setColor(Color.GREEN);
         pixmap.fill();
 
         mQuizSkin.add("Green", new Texture(pixmap));
         
        pixmap.setColor(Color.RED);
         pixmap.fill();
         
         BitmapFont myFont = new BitmapFont(
                 Gdx.files.internal("data/LucidaBitmap10px.fnt"),
                 Gdx.files.internal("data/LucidaBitmap10px_0.png"), false);
         
        mQuizSkin.add("Red", new Texture(pixmap));
         mQuizSkin.add("default", myFont);
         
         Drawable gray = mQuizSkin.newDrawable("Gray");
         Drawable green = mQuizSkin.newDrawable("Green");
        Drawable red = mQuizSkin.newDrawable("Red");
 
         mQuizSkin.getFont("default").scale(1.3f);
         
         mAnswerStyle = new TextButtonStyle(gray, gray, gray, mQuizSkin.getFont("default"));
        mAnswerStyleCorrect = new TextButtonStyle(green, green, green, mQuizSkin.getFont("default"));
        mAnswerStyleWrong = new TextButtonStyle(red, red, red, mQuizSkin.getFont("default"));
         
     }
 
     private void initializeQuestions() {
         mQuizSkin.add("default", mAnswerStyle);
 
         for (int i = 0; i < mQuestions.size(); i++) {
             mQuestions.get(i).setFontScale(4);
             mQuestions.get(i).setFillParent(true);
             mQuestions.get(i).setWrap(true);
             mQuestions.get(i).setAlignment(Align.top);
         }
 
         mQuestionGroup.addActor(mQuestions.get(0));
         mQuestionGroup.setWidth(200);
         mQuestionGroup.setPosition(mStage.getWidth() / 2 - mQuestionGroup.getWidth() / 2, mStage.getHeight() - 20);
 
     }
 
     private void initializeAnswers() {
         mAnswerGroup.setBounds(200, 200, 300, 100);
         int u = 0;
         for (int i = 0; i < mAnswers.size(); i++) {
             if (u % 4 == 0) {
                 u = 0;
             }
             if (u == 0) {
                 mAnswers.get(i).setBounds(0, 105, 300, 200);
             } else if (u == 1) {
                 mAnswers.get(i).setBounds(305, 105, 300, 200);
             } else if (u == 2) {
                 mAnswers.get(i).setBounds(0, 0, 300, 200);
             } else if (u == 3) {
                 mAnswers.get(i).setBounds(305, 0, 300, 200);
             }
             mAnswers.get(i).setFillParent(true);
             mAnswers.get(i).getLabel().setWrap(true);
             u++;
             mAnswerGroup.addActor(mAnswers.get(i));
             if (i > 3) {
                 mAnswerGroup.getChildren().items[i].setVisible(false);
             }
         }
         initSelector();
         mAnswerGroup.addActor(mSelector);
         setSelectorBounds(0, 105);
     }
 
     private void initSelector() {
         Skin buttonSkin = new Skin();
         TextureRegion region = new TextureRegion(Assets.borderBorder);
 
         buttonSkin.add("border", region);
         Drawable standard = buttonSkin.getDrawable("border");
 
         Button.ButtonStyle buttonStyle = new Button.ButtonStyle(standard, standard, standard);
 
         mSelector = new Button(buttonStyle);
         mSelector.setFillParent(true);
     }
 
     private void setSelectorBounds(int x, int y) {
         mAnswerGroup.getChildren().get(mAnswerGroup.getChildren().size - 1).setBounds(x, y, 300, 100);
     }
 
     private boolean enoughTimePassed(long time) {
         return (TimeUtils.millis() - mLastButtonPressed) > time;
     }
 
     private void initiateSelectorBounds() {
         if (mInput.right() && mSelectorLeft) {
             mSelectorX = 305;
             mSelectorLeft = false;
         } else if (mInput.left() && !mSelectorLeft) {
             mSelectorX = 0;
             mSelectorLeft = true;
         } else if (mInput.down() && mSelectorTop) {
             mSelectorY = 0;
             mSelectorTop = false;
         } else if (mInput.up() && !mSelectorTop) {
             mSelectorY = 105;
             mSelectorTop = true;
         }
         setSelectorBounds(mSelectorX, mSelectorY);
     }
 
     private void initNewAnswers() {
         if (mAnswer == mAnswersNumbered[mQuestionCounter]) {
             mNumberOfCorrectAnswers++;
         }
         mQuestionGroup.removeActor(mQuestions.get(mQuestionCounter));
         mQuestionCounter++;
         int u = 0;
         for (int i = 0; i < mAnswers.size(); i++) {
             if (u <= 3) {
                 mAnswerGroup.getChildren().items[i].remove();
                 mAnswers.remove(i);
                 if (mQuestionCounter <= 4) {
                     mAnswerGroup.getChildren().items[3].setVisible(true);
                 }
                 i--;
             }
             u++;
         }
         if (mQuestionCounter <= 4) {
             mQuestionGroup.addActor(mQuestions.get(mQuestionCounter));
         }
         mLastButtonPressed = TimeUtils.millis();
         mGetNewAnswers = false;
     }
 
     private void registerSelectorAnswer() {
         if (mInput.action()) {
             if (mSelectorTop) {
                 mAnswer = (mSelectorLeft) ? 0 : 1;
             } else {
                 mAnswer = (mSelectorLeft) ? 2 : 3;
             }
            for (int i = 0; i < 4; i++) {
                if (i != mAnswersNumbered[mQuestionCounter]) {
                    changeColor((TextButton) mAnswerGroup.getChildren().items[i], mAnswerStyleWrong);
                } else {
                    changeColor((TextButton) mAnswerGroup.getChildren().items[i], mAnswerStyleCorrect);
                }
            }
             mLastButtonPressed = TimeUtils.millis();
             mGetNewAnswers = true;
         }
     }
 
     private void presentQuizScore() {
         Label finishLabel;
         if(mNumberOfCorrectAnswers>3){
             ScoreHandler.updateQuizzesCompleted(mQuizNumber);
             finishLabel = new Label("Din score ble " + mNumberOfCorrectAnswers + ", gratulerer!\n\n Du kan nå gå videre.\n\n Press space for å avslutte", mOutputStyle);
         }else{
             finishLabel = new Label("Din score ble " + mNumberOfCorrectAnswers + "\n\n Du må prøve på nytt for\nå låse opp neste spill\n\n Press space for å avslutte", mOutputStyle);
         }
         
         finishLabel.setFontScale(4);
         finishLabel.setWrap(true);
         finishLabel.setWidth(400);
         finishLabel.setPosition(PVU.SCREEN_WIDTH/2 - finishLabel.getWidth(), PVU.SCREEN_HEIGHT/2);
         mStage.addActor(finishLabel);
         mAnswerGroup.clear();
         mQuizDone = true;
     }
 
     private void endQuiz() {
         game.setScreen(PVU.MAIN_SCREEN);
     }
 }
