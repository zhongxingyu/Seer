 package ru.petrsu.attt.screens;
 
 import com.badlogic.gdx.Game;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Screen;
 import com.badlogic.gdx.graphics.GL20;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import ru.petrsu.attt.Assets;
 import ru.petrsu.attt.input.MyInputProcessor;
 import ru.petrsu.attt.input.MyInputProcessor.ButtonClickListener;
 import ru.petrsu.attt.input.MyInputProcessor.FieldClickListener;
 import ru.petrsu.attt.model.FieldModel;
 import ru.petrsu.attt.model.Player;
 import ru.petrsu.attt.model.SmallFieldModel;
 import ru.petrsu.attt.view.*;
 
 /**
  * Created with IntelliJ IDEA.
  * User: lexer
  * Date: 8/24/13
  * Time: 1:09 PM
  * To change this template use File | Settings | File Templates.
  */
 public class GameScreen implements Screen {
     private Game game;
     private SpriteBatch spriteBatch;
 
     private Sprite background;
     private Button backButton;
 
     private ZoomedField zoomedField;
     private Field field;
     private View currentField;
 
     private FieldModel model = new FieldModel();
 
     private Player crossPlayer;
     private Player zeroPlayer;
     private Player currentPlayer;
 
     private MyInputProcessor fieldInput;
     private MyInputProcessor zoomedFieldInput;
 
     public GameScreen(Game game) {
         this.game = game;
         spriteBatch = new SpriteBatch();
         initInput();
         crossPlayer = new Player();
         zeroPlayer = new Player();
         currentPlayer = crossPlayer;
     }
 
     private void initInput() {
         fieldInput = new MyInputProcessor();
         fieldInput.setButtonClickListener(buttonClickListener);
         fieldInput.setFieldClickListener(fieldClickListener);
         Gdx.input.setInputProcessor(fieldInput);
 
         zoomedFieldInput = new MyInputProcessor();
         zoomedFieldInput.setButtonClickListener(buttonClickListener);
         zoomedFieldInput.setFieldClickListener(fieldClickListener);
     }
 
     @Override
     public void render(float delta) {
         Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
         field.update(delta);
         zoomedField.update(delta);
         spriteBatch.begin();
         background.draw(spriteBatch);
         backButton.draw(spriteBatch);
         currentField.draw(spriteBatch);
         spriteBatch.end();
     }
 
     @Override
     public void resize(int width, int height) {
     }
 
     @Override
     public void show() {
         background = new Sprite(Assets.background);
         background.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
 
         Sprite fieldSprite = new Sprite(Assets.bigField);
         View.calculateSpriteSizes(fieldSprite);
         field = new Field(model, fieldSprite);
         field.setPosition(View.Position.CENTER_VERTICAL);
         field.setPosition(View.Position.CENTER_HORIZONTAL);
         fieldInput.addView(field);
         currentField = field;
 
         Sprite zoomedFieldSprite = new Sprite(Assets.bigField);
         View.calculateSpriteSizes(zoomedFieldSprite);
         zoomedField = new ZoomedField(model.sfs.get(0), zoomedFieldSprite);
         zoomedField.setPosition(View.Position.CENTER_VERTICAL);
         zoomedField.setPosition(View.Position.CENTER_HORIZONTAL);
         zoomedFieldInput.addView(zoomedField);
 
         backButton = new Button(Assets.settingsButton);
         backButton.setPosition(Picture.Position.BOTTOM);
         backButton.setPosition(Picture.Position.RIGHT);
         backButton.setSound(Assets.click);
         fieldInput.addView(backButton);
         zoomedFieldInput.addView(backButton);
     }
 
     @Override
     public void hide() {
     }
 
     @Override
     public void pause() {
     }
 
     @Override
     public void resume() {
     }
 
     @Override
     public void dispose() {
     }
 
 
     private ButtonClickListener buttonClickListener = new ButtonClickListener() {
         @Override
         public void touchUp(View view) {
             if (view != null) {
                 if (view.id == backButton.id) {
                     backButton.playSound();
                     game.setScreen(new MainMenuScreen(game));
                 }
             }
         }
 
         @Override
         public void touchDown(View view) {
 
         }
     };
 
     private FieldClickListener fieldClickListener = new FieldClickListener() {
         @Override
         public void onClick(View view, int row, int column) {
             if (view.id == zoomedField.id) {
                 zoomedFieldClick(view, row, column);
             } else if (view.id == field.id) {
                 fieldClick(view, row, column);
             }
         }
     };
 
     private void fieldClick(View view, int row, int column) {
         // if it isn't first step of game
         if (model.activeFieldRow != -1 || model.activeFieldColumn != -1) {
             // if user click on nonactive field and it isn't first step of game
             int activeRow = model.activeFieldRow;
             int activeColumn = model.activeFieldColumn;
             if (activeRow != row || activeColumn != column) {
                 field.smallFields.get(activeRow * 3 + activeColumn).blink();
             } else { // user click on right field
                field.smallFields.get(activeRow * 3 + column).setNormal();
                 setZoomedField(row, column);
             }
         } else {
             model.setActiveField(row, column);
             setZoomedField(row, column); // Called in first step
         }
     }
 
     private void zoomedFieldClick(View view, int row, int column) {
         if (chechCell(row, column)) {
             if (currentPlayer == crossPlayer) {
                 model.update(SmallFieldModel.CROSS, row, column);
                 currentPlayer = zeroPlayer;
             } else if (currentPlayer == zeroPlayer) {
                 model.update(SmallFieldModel.ZER0, row, column);
                 currentPlayer = crossPlayer;
             }
             changeField(field);
             field.smallFields.get(model.activeFieldRow * 3 + model.activeFieldColumn).setActive();
         }
     }
 
     private boolean chechCell(int row, int column) {
         int aRow = model.activeFieldRow;
         int aColumn = model.activeFieldColumn;
         if (SmallFieldModel.NONE == model.sfs.get(aRow * 3 + aColumn).
                 cells.get(row * 3 + column)) {
             return true;
         } else {
             return false;
         }
     }
 
     private void setZoomedField(int row, int column) {
         SmallFieldModel sfm = model.sfs.get(row * 3 + column);
         if (!sfm.isFinished) {
             zoomedField.setModel(sfm);
             changeField(zoomedField);
         }
     }
 
     private void changeField(View field) {
         currentField = field;
         if (field.id == this.field.id) {
             Gdx.input.setInputProcessor(fieldInput);
         }
 
         if (field.id == this.zoomedField.id) {
             Gdx.input.setInputProcessor(zoomedFieldInput);
         }
     }
 }
