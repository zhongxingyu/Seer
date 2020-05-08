 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package no.hist.gruppe5.pvu.book;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Input;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.scenes.scene2d.Stage;
 import com.badlogic.gdx.scenes.scene2d.ui.Label;
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
 
     public BookScreen(PVU game) {
         super(game);
         pageNumber = 1;
         initScreen();
     }
 
     @Override
     protected void draw(float delta) {
         clearCamera(1, 1, 1, 1);
         batch.begin();
         //DRAW BOOK
         batch.end();
         leftPage.setText(getPageContent(0));
         rightPage.setText(getPageContent(1));
         stage.draw();
     }
 
     @Override
     protected void update(float delta) {
         if (Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) {
             game.setScreen(PVU.MAIN_SCREEN);
         }if(Gdx.input.isKeyPressed(Input.Keys.SPACE)){
             getNextPage();
         }
     }
 
     @Override
     protected void cleanUp() {
     }
 
     private String getPageContent(int rightPage) {
         String name = "section"+(pageNumber+rightPage);
         String content = XmlReader.loadText(name, FILE_NAME);
         return content;
     }
 
     private void initScreen() {
         testTexture = new Texture(Gdx.files.internal("data/book.png"));
         testTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
 
        stage = new Stage(PVU.GAME_WIDTH, PVU.GAME_HEIGHT, true, batch);
 
         Label.LabelStyle labelStyle = new Label.LabelStyle(Assets.primaryFont10px, Color.BLACK);
         leftPage = new Label(getPageContent(0), labelStyle);
        leftPage.setFontScale(0.35f);
         leftPage.setWidth(PVU.GAME_WIDTH);
        leftPage.setPosition(20, PVU.GAME_HEIGHT / 2);
         leftPage.setWrap(true);
 
         rightPage = new Label(getPageContent(1), labelStyle);
         rightPage.setPosition(110, 0);
        rightPage.setFontScale(0.35f);
         rightPage.setWidth(PVU.GAME_WIDTH);
        rightPage.setPosition(PVU.GAME_WIDTH * 0.65f - 20, PVU.GAME_HEIGHT / 2);
         rightPage.setWrap(true);
 
         stage.addActor(leftPage);
         stage.addActor(rightPage);
     }
     
     public void getNextPage(){
         pageNumber+=2;
     }
     
     public void getPreviousPage(){
         pageNumber-=2;
     }
 }
