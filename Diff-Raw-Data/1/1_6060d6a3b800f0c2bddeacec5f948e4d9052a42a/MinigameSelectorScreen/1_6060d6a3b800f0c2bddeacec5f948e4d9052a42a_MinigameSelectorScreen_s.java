 package no.hist.gruppe5.pvu.mainroom;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Input;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.scenes.scene2d.Group;
 import com.badlogic.gdx.scenes.scene2d.Stage;
 import com.badlogic.gdx.scenes.scene2d.ui.*;
 import com.badlogic.gdx.scenes.scene2d.ui.Button.ButtonStyle;
 import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
 import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import no.hist.gruppe5.pvu.Assets;
 import no.hist.gruppe5.pvu.GameScreen;
 import no.hist.gruppe5.pvu.PVU;
 import no.hist.gruppe5.pvu.coderacer.CoderacerScreen;
 import no.hist.gruppe5.pvu.quiz.QuizScreen;
 import no.hist.gruppe5.pvu.visionshooter.VisionScreen;
 
 public class MinigameSelectorScreen extends GameScreen {
 
     private String text = "Minigame 1";
     private String text2 = "Minigame 2";
     private String text3 = "Minigame 3";
     private String text4 = "Minigame 4";
     private String text5 = "Minigame 5";
     private Stage stage;
     private Texture tex;
     private TextButtonStyle textbuttonstyle;
     private TextButton button;
     private Skin buttonskin;
     private int counter = 1;
     private Button buttonMove;
     private Button button2;
     private Button button3;
     private Button button4;
     private Button button5;
     private Button button1;
     private boolean buttonPressedS;
     private boolean buttonPressedW;
     private boolean buttonPressedENTER;
     private Group menu; 
 
     public MinigameSelectorScreen(final PVU game) {
         super(game);
         menu = new Group();
         stage = new Stage(PVU.SCREEN_WIDTH, PVU.SCREEN_WIDTH, true, batch);
 
         button1 = makeButton(text);
         button1.setPosition(0, 420);
         button1.setFillParent(true);
         menu.addActor(button1);
 
         button2 = makeButton(text2);
         button2.setPosition(0, 315);
         button2.setFillParent(true);
         menu.addActor(button2);
 
         button3 = makeButton(text3);
         button3.setPosition(0, 210);
         button3.setFillParent(true);
         menu.addActor(button3);
 
         button4 = makeButton(text4);
         button4.setPosition(0, 105);
         button4.setFillParent(true);
         menu.addActor(button4);
 
         button5 = makeButton(text5);
         button5.setPosition(0, 0);
         button5.setFillParent(true);
         menu.addActor(button5);
 
         initMakeButton();        
         menu.addActor(buttonMove);
         
         menu.setBounds(510, 295, 590, 100);
         
         stage.addActor(menu);
     }
 
     @Override
     protected void draw(float delta) {
         clearCamera(1, 1, 1, 1);
 
         batch.begin();
         stage.getSpriteBatch().draw(Assets.msPcBackground, 0, 0);
         batch.end();
         stage.draw();
     }
 
     @Override
     protected void update(float delta) {
         if (Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) {
             game.setScreen(new MainScreen(game));
         }
         if (Gdx.input.isKeyPressed(Input.Keys.S) && !buttonPressedS) {
             buttonPressedS = true;
             if (counter < 5) {
                 counter++;
             }
             if (counter == 1) {
                 buttonMove.setPosition(button1.getX(), button1.getY());
             }
             if (counter == 2) {
                 buttonMove.setPosition(button2.getX(), button2.getY());
             }
             if (counter == 3) {
                 buttonMove.setPosition(button3.getX(), button3.getY());
             }
             if (counter == 4) {
                 buttonMove.setPosition(button4.getX(), button4.getY());
             }
             if (counter == 5) {
                 buttonMove.setPosition(button5.getX(), button5.getY());
             }
         }
         if (Gdx.input.isKeyPressed(Input.Keys.W) && !buttonPressedW) {
             buttonPressedW = true;
             if (counter > 1) {
                 counter--;
             }
             if (counter == 1) {
                 buttonMove.setPosition(button1.getX(), button1.getY());
             }
             if (counter == 2) {
                 buttonMove.setPosition(button2.getX(), button2.getY());
             }
             if (counter == 3) {
                 buttonMove.setPosition(button3.getX(), button3.getY());
             }
             if (counter == 4) {
                 buttonMove.setPosition(button4.getX(), button4.getY());
             }
             if (counter == 5) {
                 buttonMove.setPosition(button5.getX(), button5.getY());
             }
         }
         if (!Gdx.input.isKeyPressed(Input.Keys.S)) {
             buttonPressedS = false;
         }
         if (!Gdx.input.isKeyPressed(Input.Keys.W)) {
             buttonPressedW = false;
         }
 
         if (Gdx.input.isKeyPressed(Input.Keys.ENTER) && !buttonPressedENTER) {
             buttonPressedENTER = true;
             if (counter == 1) {
                 game.setScreen(new CoderacerScreen(game));
             }
             if (counter == 2) {
                 game.setScreen(new VisionScreen(game));
             }
             if (counter == 3) {
                 try {
                     game.setScreen(new QuizScreen(game));
                 } catch (FileNotFoundException ex) {
                     Logger.getLogger(MinigameSelectorScreen.class.getName()).log(Level.SEVERE, null, ex);
                 } catch (IOException ex) {
                     Logger.getLogger(MinigameSelectorScreen.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
             if (counter == 4) {
             }
             if (counter == 5) {
             }
         }
         if (!Gdx.input.isKeyPressed(Input.Keys.ENTER)) {
             buttonPressedENTER = false;
         }
         
         if (Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) {
             game.setScreen(PVU.MAIN_SCREEN);
         }
     }
 
     @Override
     protected void cleanUp() {
     }
 
     private TextButton makeButton(String text) {
         tex = new Texture(Gdx.files.internal("data/DialogTextureWithoutFrame.png"));
         buttonskin = new Skin();
         textbuttonstyle = new TextButton.TextButtonStyle();
         textbuttonstyle.font = Assets.primaryFont10px;
        
         buttonskin.add("textfieldback", new TextureRegion(tex, 10, 10));
         Drawable d = buttonskin.getDrawable("textfieldback");
         textbuttonstyle.up = d;
         textbuttonstyle.down = d;
         button = new TextButton(text, textbuttonstyle);
         button.getLabel().setFontScale(5);
         return button;
     }
     private void initMakeButton(){
         Skin buttonSkin = new Skin();
         TextureRegion region = new TextureRegion(Assets.borderBorder);
         
         buttonSkin.add("border", region);
         Drawable standard = buttonSkin.getDrawable("border");
         
         ButtonStyle buttonStyle = new ButtonStyle(standard, standard, standard);
         
         buttonMove = new Button(buttonStyle);
         buttonMove.setFillParent(true);
     }
 }
