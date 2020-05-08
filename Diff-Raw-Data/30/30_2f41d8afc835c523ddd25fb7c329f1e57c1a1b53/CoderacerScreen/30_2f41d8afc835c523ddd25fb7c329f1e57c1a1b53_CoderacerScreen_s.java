 package no.hist.gruppe5.pvu.coderacer;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Input.Keys;
 import com.badlogic.gdx.InputProcessor;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.scenes.scene2d.Stage;
 import com.badlogic.gdx.scenes.scene2d.ui.Label;
 import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
 import com.badlogic.gdx.scenes.scene2d.utils.Align;
 import com.badlogic.gdx.utils.Timer;
 import no.hist.gruppe5.pvu.Assets;
 import no.hist.gruppe5.pvu.GameScreen;
 import no.hist.gruppe5.pvu.PVU;
 
 /**
  * Created with IntelliJ IDEA. User: karl Date: 8/28/13 Time: 10:49 AM
  */
 public class CoderacerScreen extends GameScreen {
 
     private Label finishedCode;
     private Label codeOutput;
     private Label grade;
     private Label time;
     private InputProcessor inputListener;
     private Code code = new Code();
     private Stage stage;
     private int remainingTime = 60;
     private int score = 0;
 
     public CoderacerScreen(PVU game) {
         super(game);
         
         Timer.schedule(new Timer.Task() {
 
             @Override
             public void run() {
                 remainingTime--;
             }
         }, 1f, 1f);
         
         stage = new Stage(PVU.GAME_WIDTH, PVU.GAME_HEIGHT, true, batch);
 
        LabelStyle outputStyle = new LabelStyle(Assets.primaryFont10px, Color.BLACK);
         codeOutput = new Label(code.getCode(), outputStyle);
         codeOutput.setFontScale(0.8f);
         codeOutput.setWrap(true);
         codeOutput.setWidth(PVU.GAME_WIDTH);
 
        LabelStyle finishedStyle = new LabelStyle(Assets.primaryFont10px, Color.RED);
         finishedCode = new Label("", finishedStyle);
        finishedCode.setFontScale(0.4f);
 
         grade = new Label("", finishedStyle);
         grade.setFontScale(1f);
         
         time = new Label("" + remainingTime, finishedStyle);
        time.setFontScale(0.5f);
 
         stage.addActor(grade);
         stage.addActor(codeOutput);
         stage.addActor(finishedCode);
         stage.addActor(time);
 
         codeOutput.setPosition(0, (PVU.GAME_HEIGHT * 0.8f));
         finishedCode.setPosition(PVU.GAME_WIDTH / 2 - finishedCode.getPrefWidth() / 2, (PVU.GAME_HEIGHT * 0.2f));
         time.setPosition(PVU.GAME_WIDTH*0.9f, PVU.GAME_HEIGHT*0.1f);
 
         Gdx.input.setInputProcessor(new inputListener());
     }
 
     @Override
     protected void draw(float delta) {
         clearCamera(1, 1, 1, 1); // Important
         stage.draw();
     }
 
     @Override
     protected void update(float delta) {
         if (code.isFinished() || remainingTime <= 0) {
             codeOutput.setVisible(false);
             finishedCode.setVisible(false);
             grade.setText("Din karakter ble " + score);
             grade.setPosition(PVU.GAME_WIDTH / 2f - grade.getPrefWidth() / 2f, (PVU.GAME_HEIGHT / 2f));
             if (Gdx.input.isKeyPressed(Keys.SPACE)) {
                 game.setScreen(PVU.MAIN_SCREEN);
             }
         }
         else time.setText(remainingTime + "");
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
             if (!code.isFinished()) {
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
 
     private void updateOutput() {
         codeOutput.setText(code.getLeft());
         finishedCode.setText(code.getCorrect());
         codeOutput.setPosition(PVU.GAME_WIDTH / 2 - codeOutput.getPrefWidth() / 2, (PVU.GAME_HEIGHT * 0.8f));
         finishedCode.setPosition(PVU.GAME_WIDTH / 2 - finishedCode.getPrefWidth() / 2, (PVU.GAME_HEIGHT * 0.2f));
     }
 }
