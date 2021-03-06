 package it.alcacoop.gnubackgammon.layers;
 
 import it.alcacoop.gnubackgammon.GnuBackgammon;
 import it.alcacoop.gnubackgammon.actors.Board;
 import it.alcacoop.gnubackgammon.actors.PlayerInfo;
 import it.alcacoop.gnubackgammon.fsm.BaseFSM.Events;
 import it.alcacoop.gnubackgammon.fsm.GameFSM.States;
 import it.alcacoop.gnubackgammon.logic.MatchState;
 import it.alcacoop.gnubackgammon.ui.GameMenuPopup;
 import it.alcacoop.gnubackgammon.ui.UIDialog;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Input.Keys;
 import com.badlogic.gdx.Screen;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.scenes.scene2d.InputEvent;
 import com.badlogic.gdx.scenes.scene2d.InputListener;
 import com.badlogic.gdx.scenes.scene2d.Stage;
 import com.badlogic.gdx.scenes.scene2d.actions.Actions;
 import com.badlogic.gdx.scenes.scene2d.ui.Image;
 import com.badlogic.gdx.scenes.scene2d.ui.Table;
 import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
 import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
 
 
 public class GameScreen implements Screen {
 
   private Stage stage;
   private Image bgImg;
   public Board board;
   private Table table;
   
   private PlayerInfo pInfo[];
   private GameMenuPopup menuPopup;
  private TextButton menu;
   private Image logo;
 
   
   public GameScreen(){
     //STAGE DIM = SCREEN RES
     stage = new Stage(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false);
     //VIEWPORT DIM = VIRTUAL RES (ON SELECTED TEXTURE BASIS)
     stage.setViewport(GnuBackgammon.resolution[0], GnuBackgammon.resolution[1], false);
     
     TextureRegion bgRegion = GnuBackgammon.atlas.findRegion("bg");
     bgImg = new Image(bgRegion);
     stage.addActor(bgImg);
     
     stage.addListener(new InputListener() {
       @Override
       public boolean keyDown(InputEvent event, int keycode) {
         if(Gdx.input.isKeyPressed(Keys.BACK)||Gdx.input.isKeyPressed(Keys.B)) {
           GnuBackgammon.fsm.state(States.DIALOG_HANDLER);
           UIDialog.getYesNoDialog(
               Events.ABANDON_MATCH, 
               "Really exit this match?", 
               GnuBackgammon.Instance.board.getStage());
         }
         if(Gdx.input.isKeyPressed(Keys.MENU)||Gdx.input.isKeyPressed(Keys.M)) {
           menuPopup.toggle();
         }
         return super.keyDown(event, keycode);
       }
     });
     
     board = GnuBackgammon.Instance.board;
     
     pInfo = new PlayerInfo[2];
     pInfo[0] = new PlayerInfo("AI():", 1);
     pInfo[1] = new PlayerInfo("PL1:", 0);      
     
     table = new Table();
     stage.addActor(table);
     
     menuPopup = new GameMenuPopup(stage);
     
     menu = new TextButton("M", GnuBackgammon.skin);
     menu.addListener(new ClickListener(){
       @Override
       public void clicked(InputEvent event, float x, float y) {
         menuPopup.toggle();
       }
     });
     
     TextureRegion r = GnuBackgammon.atlas.findRegion("logo");
     logo = new Image(r);
     
     stage.addActor(menuPopup);
   }
 
   
   private void initTable() {
     table.clear();
     table.setFillParent(true);
     
     float width = stage.getWidth()/5f;
     table.add(logo).left().expand().padLeft(5+5*(2-GnuBackgammon.ss));
     table.add(pInfo[0]).width(width);
     table.add(pInfo[1]).width(width);
     table.add(menu).fill().width(width/3).padTop(5).padRight(5+5*(2-GnuBackgammon.ss));
     
     table.row();
     table.add(board).colspan(4).expand().fill();
   }
 
   
   public void updatePInfo() {
     pInfo[0].update();
     pInfo[1].update();
   }
   
   @Override
   public void render(float delta) {
     bgImg.setWidth(stage.getWidth());
     bgImg.setHeight(stage.getHeight());    
     Gdx.gl.glClearColor(1, 1, 1, 1);
     Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
     stage.act(delta);
     stage.draw();
   }
 
   @Override
   public void resize(int width, int height) {
     bgImg.setWidth(stage.getWidth());
     bgImg.setHeight(stage.getHeight());
   }
 
   
   @Override
   public void show() {
     initTable();
     board.initBoard();
        
     if(MatchState.matchType == 0){ //single player
       pInfo[0].setName("AI("+(MatchState.currentLevel.ordinal()+1)+"):");
       pInfo[1].setName("PL1:");
     } else {
       pInfo[0].setName("PL1:");
       pInfo[1].setName("PL2:");
     }
     
     pInfo[0].update();
     pInfo[1].update();
     Gdx.input.setInputProcessor(stage);
     Gdx.input.setCatchBackKey(true);
     
     table.setY(stage.getHeight());
     table.addAction(Actions.sequence(
       Actions.delay(0.1f),
       Actions.moveTo(0, 0, 0.3f),
       Actions.run(new Runnable() {
         @Override
         public void run() {
           GnuBackgammon.fsm.state(States.OPENING_ROLL);
         }
       })
     ));
   }
 
   
   @Override
   public void hide() {
     board.initBoard();
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
 
 
 }
