 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package no.hist.gruppe5.pvu.book;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Input;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.TextureAtlas;
 import com.badlogic.gdx.scenes.scene2d.Stage;
 import com.badlogic.gdx.scenes.scene2d.ui.Label;
 import com.badlogic.gdx.scenes.scene2d.ui.Skin;
 import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
 import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
 import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
 import com.badlogic.gdx.utils.TimeUtils;
 import no.hist.gruppe5.pvu.Assets;
 import no.hist.gruppe5.pvu.GameScreen;
 import no.hist.gruppe5.pvu.PVU;
 import no.hist.gruppe5.pvu.XmlReader;
 
 /**
  *
  * @author linnk
  */
 public class BookScreen extends GameScreen {
 
     public static Texture testTexture;
     private Label leftPage;
     private Label rightPage;
     private Stage stage;
     private int pageNumber;
     private final String FILE_NAME = "data/test.xml";
     private TextButton nextButton;
     private TextButton prevButton;
     TextureAtlas atlas;
     Skin skin;
     ClickListener listener;
     private long timeSinceLastAction;
     private final int SHOW_NEXT_BUTTON = 5;
     private final int SHOW_PREV_BUTTON = 1;
 
     public BookScreen(PVU game) {
         super(game);
         pageNumber = 1;
         initScreen();
         timeSinceLastAction = 0;
     }
 
     @Override
     protected void draw(float delta) {
         clearCamera(1, 1, 1, 1);
         batch.begin();
         batch.draw(Assets.bookBook, 0, 0, PVU.GAME_WIDTH, PVU.GAME_HEIGHT);
         batch.end();
         leftPage.setText(getPageContent(0));
         rightPage.setText(getPageContent(1));
         stage.draw();
         stage.act(delta);
 
         if (SHOW_NEXT_BUTTON == pageNumber) {
             nextButton.remove();
         } else {
             stage.addActor(nextButton);
         }
         if (SHOW_PREV_BUTTON == pageNumber) {
             prevButton.remove();
         } else {
             stage.addActor(prevButton);
         }
     }
 
     @Override
     protected void update(float delta) {
        if (Gdx.input.isKeyPressed(Input.Keys.ESCAPE) && TimeUtils.millis()-timeSinceLastAction>700l && TimeUtils.millis()-getTime()>700l) {
             game.setScreen(PVU.MAIN_SCREEN);
         }
         if (TimeUtils.millis() - timeSinceLastAction > 700l) {
             if (nextButton.isPressed() || Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D)) {
                 getNextPage();
                 timeSinceLastAction = TimeUtils.millis();
             }
             if (prevButton.isPressed() || Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A)) {
                 getPreviousPage();
                 timeSinceLastAction = TimeUtils.millis();
             }
         }
     }
 
     @Override
     protected void cleanUp() {
     }
 
     private String getPageContent(int rightPage) {
         String name = "section" + (pageNumber + rightPage);
         String content = XmlReader.loadText(name, FILE_NAME);
         return content;
     }
 
     private void initScreen() {
         testTexture = new Texture(Gdx.files.internal("data/book.png"));
         testTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
 
         stage = new Stage(PVU.GAME_WIDTH * 2.5f, PVU.GAME_HEIGHT * 2.5f, true);
 
         Label.LabelStyle labelStyle = new Label.LabelStyle(Assets.primaryFont10px, Color.BLACK);
         leftPage = new Label(getPageContent(0), labelStyle);
         leftPage.setWidth(PVU.GAME_WIDTH);
         leftPage.setPosition(35, stage.getHeight() / 2);
         leftPage.setWrap(true);
 
         rightPage = new Label(getPageContent(1), labelStyle);
         rightPage.setPosition(110, 0);
         rightPage.setWidth(PVU.GAME_WIDTH);
         rightPage.setPosition(stage.getWidth() / 2 + 25, stage.getHeight() / 2);
         rightPage.setWrap(true);
 
         stage.addActor(leftPage);
         stage.addActor(rightPage);
 
         atlas = new TextureAtlas("data/bookscreen/button.pack");
         skin = new Skin(atlas);
 
         TextButtonStyle styleNext = new TextButton.TextButtonStyle();
         styleNext.up = skin.getDrawable("buttonnext.up");
         styleNext.down = skin.getDrawable("buttonnext.down");
         styleNext.pressedOffsetX = -1;
         styleNext.pressedOffsetY = -1;
         styleNext.font = Assets.primaryFont10px;
         styleNext.fontColor = Color.BLACK;
 
 
         nextButton = new TextButton("Next", styleNext);
         nextButton.pad(20);
         nextButton.setPosition(stage.getWidth() / 1.2f, 20);
 
         TextButtonStyle stylePrev = new TextButtonStyle();
         stylePrev.up = skin.getDrawable("buttonprev.up");
         stylePrev.down = skin.getDrawable("buttonprev.down");
         stylePrev.pressedOffsetX = -1;
         stylePrev.pressedOffsetY = -1;
         stylePrev.font = Assets.primaryFont10px;
         stylePrev.fontColor = Color.BLACK;
         prevButton = new TextButton("Prev", stylePrev);
         prevButton.pad(20);
         prevButton.setPosition(35, 20);
 
         Gdx.input.setInputProcessor(stage);
 
         if (SHOW_NEXT_BUTTON != pageNumber) {
             stage.addActor(nextButton);
         }
         if (SHOW_PREV_BUTTON != pageNumber) {
             stage.addActor(prevButton);
         }
 
     }
 
     public void getNextPage() {
         pageNumber += 2;
     }
 
     public void getPreviousPage() {
         pageNumber -= 2;
     }
 }
