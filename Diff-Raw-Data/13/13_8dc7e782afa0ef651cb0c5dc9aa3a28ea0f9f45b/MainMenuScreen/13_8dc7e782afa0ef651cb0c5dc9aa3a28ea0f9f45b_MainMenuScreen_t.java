 package ch.zhaw.arsphema.screen;
 
 import ch.zhaw.arsphema.MyGdxGame;
 import ch.zhaw.arsphema.services.Services;
 import ch.zhaw.arsphema.services.SoundManager;
 import ch.zhaw.arsphema.util.Sizes;
 import ch.zhaw.arsphema.util.Sounds;
 import ch.zhaw.arsphema.util.UiLabels;
 import ch.zhaw.arsphema.util.UiStyles;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.scenes.scene2d.Actor;
 import com.badlogic.gdx.scenes.scene2d.ui.ClickListener;
 import com.badlogic.gdx.scenes.scene2d.ui.Label;
 import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
 import com.badlogic.gdx.scenes.scene2d.ui.tablelayout.Table;
 
 public class MainMenuScreen extends UiScreen {
 
     private Table table;
 
     public MainMenuScreen(MyGdxGame game) {
         super(game);
         uiController.setButtonListener(new MainMenuListener());
     }
 
     private void setupGUI() {
 
         //Layouttable
         table = new Table();
         table.setFillParent(true);
         stage.addActor(table);
 
         //Header
         table.add(new Label("Arsphema", UiStyles.LABEL_TITLE)).padBottom(20);
 
         //Buttons
         TextButton startButton = new TextButton("Start Game", UiStyles.BUTTON_DEFAULT, UiLabels.BUTTON_GAME_START);
         startButton.setClickListener(uiController.getButtonListener());
         addTextButton(startButton);
 
         TextButton optionsButton = new TextButton("Options", UiStyles.BUTTON_DEFAULT, UiLabels.BUTTON_SHOW_OPTIONS);
         optionsButton.setClickListener(uiController.getButtonListener());
         addTextButton(optionsButton);
 
         TextButton highscoreButton = new TextButton("Highscore", UiStyles.BUTTON_DEFAULT, UiLabels.BUTTON_SHOW_HIGHSCORE);
         highscoreButton.setClickListener(uiController.getButtonListener());
         addTextButton(highscoreButton);
 
         TextButton creditsButton = new TextButton("Credits", UiStyles.BUTTON_DEFAULT, UiLabels.BUTTON_SHOW_CREDITS);
         creditsButton.setClickListener(uiController.getButtonListener());
         addTextButton(creditsButton);
 
         TextButton quitButton = new TextButton("Quit", UiStyles.BUTTON_DEFAULT, UiLabels.BUTTON_QUIT);
         quitButton.setClickListener(uiController.getButtonListener());
         addTextButton(quitButton);
     }
 
     private void addTextButton(TextButton button) {
         table.row();
         table.add(button).width((int) (Sizes.BUTTON_WIDTH * ppuX)).pad(5);
     }
 
     @Override
     public void resize(int width, int height) {
         super.resize(width, height);
         setupGUI();
     }
 
     @Override
     public void render(float delta) {
         super.render(delta);
         //maybe for later use
     }
 
     @Override
     public void show() {
         super.show();
         Gdx.input.setCatchBackKey(false);
     }
 
 
     private class MainMenuListener implements ClickListener {
 
         @Override
         public void click(Actor actor, float v, float v1) {
             Services.setSoundManager(new SoundManager());
             Services.getSoundManager().play(Sounds.BEEP, false);
             if (UiLabels.BUTTON_GAME_START.equals(actor.name)) {
                 game.createNewGame();
                 game.setScreen(game.getGameScreen());
             } else if (UiLabels.BUTTON_SHOW_HIGHSCORE.equals(actor.name)) {
                 game.setScreen(game.getHighscoreScreen());
             } else if (UiLabels.BUTTON_SHOW_OPTIONS.equals(actor.name)) {
                 game.setScreen(game.getOptionScreen());
             } else if (UiLabels.BUTTON_SHOW_CREDITS.equals(actor.name)) {
                 game.setScreen(game.getCreditsScreen());
             } else if (UiLabels.BUTTON_QUIT.equals(actor.name)) {
                Gdx.app.exit(); // disposes assets
                System.exit(0); // needed since assets need to be reloaded on next start 
             }
         }
     }
 }
