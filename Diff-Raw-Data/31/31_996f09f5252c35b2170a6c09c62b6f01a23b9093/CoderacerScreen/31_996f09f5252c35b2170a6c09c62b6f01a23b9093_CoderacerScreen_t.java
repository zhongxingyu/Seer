 package no.hist.gruppe5.pvu.coderacer;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.InputProcessor;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
 import com.badlogic.gdx.scenes.scene2d.Group;
 import com.badlogic.gdx.scenes.scene2d.Stage;
 import com.badlogic.gdx.scenes.scene2d.ui.Label;
 import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
 import com.badlogic.gdx.scenes.scene2d.utils.Align;
 import com.badlogic.gdx.utils.TimeUtils;
 import com.badlogic.gdx.utils.Timer;
 import no.hist.gruppe5.pvu.Assets;
 import no.hist.gruppe5.pvu.GameScreen;
 import no.hist.gruppe5.pvu.PVU;
 import no.hist.gruppe5.pvu.ScoreHandler;
 import no.hist.gruppe5.pvu.Input;
 
 /**
  * Created with IntelliJ IDEA. User: karl Date: 8/28/13 Time: 10:49 AM
  */
 public class CoderacerScreen extends GameScreen {
 
     ShapeRenderer mShape;
 
     private Label finishedCode;
     private Label codeOutput;
     private Label firstLine;
     private Label time;
     private Code code = new Code();
     private Stage stage;
     private int remainingTime = 30;
     private int score = 0;
     private boolean start;
     private boolean pause;
     private boolean mDoneHandled;
     private long mTimeDone;
     private Input mInput;
     private Timer.Task task = new Timer.Task() {
 
         @Override
         public void run() {
             if(isGamePaused()) return;
             remainingTime--;
         }
     };
 
     public CoderacerScreen(PVU game) {
         super(game);
 
         mInput = new Input();
 
         mShape = new ShapeRenderer();
 
         pause = false;
         start = false;
 
         stage = new Stage(PVU.SCREEN_WIDTH, PVU.SCREEN_HEIGHT, true);
 
         Group outputGroup = new Group();
         Group inputGroup = new Group();
 
         LabelStyle outputStyle = new LabelStyle(Assets.primaryFont10px, Color.GREEN);
         codeOutput = new Label("Du har 30 sekunder på å skrive så mye av koden som mulig.\n\nTrykk space for å begynne.", outputStyle);
         codeOutput.setFontScale(3f);
         codeOutput.setFillParent(true);
         codeOutput.setWrap(true);
         codeOutput.setAlignment(Align.top | Align.left);
 
         firstLine = new Label(" ", outputStyle);
         firstLine.setFontScale(3f);
         firstLine.setFillParent(true);
         firstLine.setWrap(true);
         firstLine.setAlignment(Align.top | Align.left);
         firstLine.setY(firstLine.getHeight() * 3);
 
         LabelStyle finishedStyle = new LabelStyle(Assets.primaryFont10px, Color.RED);
         finishedCode = new Label("", finishedStyle);
         finishedCode.setFontScale(3f);
         finishedCode.setFillParent(true);
         finishedCode.setWrap(true);
         finishedCode.setAlignment(Align.bottom | Align.left);
 
      
 
         time = new Label("" + remainingTime, finishedStyle);
         time.setFontScale(3f);
 
         outputGroup.addActor(codeOutput);
         outputGroup.setWidth(120);
         outputGroup.setHeight(40);
         outputGroup.setPosition(PVU.SCREEN_WIDTH / 2 - outputGroup.getWidth() * 1.4f, 450 - firstLine.getHeight() * 3);
 
         outputGroup.addActor(firstLine);
 
         inputGroup.addActor(finishedCode);
         inputGroup.setWidth(120);
         inputGroup.setHeight(40);
         inputGroup.setPosition(PVU.SCREEN_WIDTH / 2 - inputGroup.getWidth() * 1.4f, 190);
 
 
         stage.addActor(outputGroup);
         stage.addActor(inputGroup);
         stage.addActor(time);
 
         time.setPosition(PVU.SCREEN_WIDTH * 0.9f, PVU.SCREEN_HEIGHT * 0.1f);
 
         Gdx.input.setInputProcessor(new inputListener());
     }
 
     private void updateOutput() {
         StringBuilder codeLeft = new StringBuilder(code.getLeft());
         int max = (codeLeft.length() >= 18) ? 18 : codeLeft.length();
         firstLine.setText("" + codeLeft.subSequence(0, max));
         if (codeLeft.length() > 0 && codeLeft.charAt(0) == " ".charAt(0)) {
             firstLine.setX(6f * 3f);
         } else {
             firstLine.setX(0f);
         }
         codeLeft.delete(0, max);
         codeOutput.setText(codeLeft);
         finishedCode.setText(code.getCorrect());
     }
 
     @Override
     protected void draw(float delta) {
         clearCamera(1, 1, 1, 1); // Important
         batch.begin();
         batch.draw(Assets.msPcBackground, 0, 0);
         batch.end();
         stage.draw();
         if(start && !mDoneHandled) {
             mShape.begin(ShapeRenderer.ShapeType.Filled);
             mShape.rect(310f, 458f, 6 * 3, 2f);
             mShape.end();
         }
     }
 
     @Override
     protected void update(float delta) {
         if (mInput.action() && !start) {
             codeOutput.setText(code.getCode());
             start = true;
             updateOutput();
             Timer.schedule(task, 1f, 1f);
         }
 
         if ((code.isFinished() || remainingTime <= 0) && !mDoneHandled) {
             mTimeDone = TimeUtils.millis();
             ScoreHandler.updateScore(ScoreHandler.CODE, code.getGrade(score));
            game.setScreen(new CoderacerEndScreen(game, code.getGrade(score)));
             mDoneHandled = true;
         } else if (!mDoneHandled) {
             time.setText(remainingTime + "");
         }
     }
 
     @Override
     protected void cleanUp() {
     }
 
     private class inputListener implements InputProcessor {
 
         @Override
         public boolean keyDown(int keycode) {
             return false;
         }
 
         @Override
         public boolean keyUp(int keycode) {
             return false;
         }
 
         @Override
         public boolean keyTyped(char character) {
             if (!code.isFinished() && !isGamePaused()) {
                 if (character > 31) {
                     if (code.equals(character)) {
                         score++;
                         if (!code.isFinished()) {
                             updateOutput();
                         }
                     }
                 }
                 return true;
             }
             return false;
         }
 
         @Override
         public boolean touchDown(int x, int y, int pointer, int button) {
             return false;
         }
 
         @Override
         public boolean touchUp(int x, int y, int pointer, int button) {
             return false;
         }
 
         @Override
         public boolean touchDragged(int x, int y, int pointer) {
             return false;
         }
 
         @Override
         public boolean scrolled(int amount) {
             return false;
         }
         @Override
         public boolean mouseMoved(int screenX, int screenY) {
             return false;
         }
 
     }
 
     
 }
