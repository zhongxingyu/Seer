 package org.ludumdare24.entities;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.g2d.ParticleEffect;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.TextureAtlas;
 import com.badlogic.gdx.input.GestureDetector;
 import com.badlogic.gdx.scenes.scene2d.Actor;
 import com.badlogic.gdx.scenes.scene2d.ui.*;
 import com.badlogic.gdx.scenes.scene2d.ui.tablelayout.Table;
 import org.gameflow.screen.Screen2D;
 import org.ludumdare24.MainGame;
 import org.ludumdare24.Sounds;
 import org.ludumdare24.entities.creature.Creature;
 import org.ludumdare24.screens.MainMenuScreen;
 
 /**
  * The god that is the players character.
  */
 public class PlayerGod extends God {
 
     private Label manaLabel;
 
     private Tool currentTool;
 
     private ParticleEffect cursorEffect=null;
     private TextureAtlas atlas;
 
     private final MainGame game;
 
     public PlayerGod(MainGame game) {
         this.game = game;
     }
 
     @Override
     public void onCreate(TextureAtlas atlas) {
         this.atlas = atlas;
         cursorEffect = new ParticleEffect();
         changeTool(null);
     }
 
     @Override
     public void showOnScreen(Screen2D screen2D) {
         Table hud = new Table();
         hud.setFillParent(true);
 
         hud.add(screen2D.createButton("Menu", new ClickListener() {
             public void click(Actor actor, float x, float y) {
                 game.setScreen(new MainMenuScreen(game));
                 game.soundService.play(Sounds.UI_ACCEPT);
             }
         })).expand().top().left();
 
 
         // Mana gauge
         manaLabel = new Label("Mana", screen2D.getSkin());
         hud.add(manaLabel).expand().top().right();
 
         hud.row();
 
         // Action buttons
         Table buttons = new Table();
         ButtonGroup buttonGroup = new ButtonGroup();
         buttonGroup.setMinCheckCount(0);
         buttons.add(createToolButton(Tool.SMITE, "smiteButton", buttonGroup, screen2D));
         buttons.add(createToolButton(Tool.LOVE, "heartButton", buttonGroup, screen2D));
         buttons.add(createToolButton(Tool.MOVE, "moveButton", buttonGroup, screen2D));
         buttons.add(createToolButton(Tool.RAGE, "rageButton", buttonGroup, screen2D));
         buttons.add(createToolButton(Tool.FEED, "foodButton", buttonGroup, screen2D));
         buttons.add(createToolButton(Tool.WATCH, "watchButton", buttonGroup, screen2D));
 
         hud.add(buttons).expand().bottom().colspan(3);
 
         // Add HUD to screen
         screen2D.getStage().addActor(hud);
 
         // Listen to user input
         screen2D.getInputMultiplexer().addProcessor(new GestureDetector(new GestureDetector.GestureAdapter() {
             @Override
             public boolean touchDown(int x, int y, int pointer) {
                 if (cursorEffect != null){
                     cursorEffect.setPosition(x, y);
                 }
                 return false;
             }
 
             @Override
             public boolean tap(int x, int y, int count) {
                 // TODO: Convert to world coordiantes
                 float worldX = x;
                float worldY = y;
 
                 useTool(worldX, worldY);
                 return true;
             }
 
             @Override
             public boolean pan(int x, int y, int deltaX, int deltaY) {
                 // TODO: Move camera
                 return true;
             }
 
             @Override
             public boolean zoom(float originalDistance, float currentDistance) {
                 // TODO: Zoom camera up to max zoom and down to min zoom
                 return true;
             }
         }));
     }
 
     private ImageButton createToolButton(final Tool tool, String imageName, ButtonGroup buttonGroup, Screen2D screen2D) {
         ImageButton button = screen2D.createImageButton(imageName, new ClickListener() {
             public void click(Actor actor, float x, float y) {
                 game.soundService.play(Sounds.UI_CLICK);
                 ((Button)actor).setChecked(true);
                 changeTool(tool);
             }
         });
         buttonGroup.add(button);
 
         if (currentTool == tool) {
             button.setChecked(true);
         }
 
         return button;
     }
 
     private void changeTool(Tool tool) {
         currentTool = tool;
         if (currentTool != null) {
             /*
             if (cursorEffect != null){
                 cursorEffect.dispose();
             }
              */
             switch (currentTool ){
 
                 case SMITE:
                     cursorEffect.load(Gdx.files.internal("particles/smitSelect.particle"), atlas);
                     cursorEffect.start();
                     break;
 
                 case LOVE:
                     cursorEffect.load(Gdx.files.internal("particles/heartSelect.particle"), atlas);
                     cursorEffect.start();
                     break;
 
                 case MOVE:
                     cursorEffect.load(Gdx.files.internal("particles/moveSelect.particle"), atlas);
                     cursorEffect.start();
                     break;
                 case RAGE:
                     cursorEffect.load(Gdx.files.internal("particles/rageSelect.particle"), atlas);
                     cursorEffect.start();
                     break;
                 case FEED:
                     cursorEffect.load(Gdx.files.internal("particles/feedSelect.particle"), atlas);
                     cursorEffect.start();
                     break;
                 case WATCH:
                     cursorEffect.load(Gdx.files.internal("particles/watchSelect.particle"), atlas);
                     cursorEffect.start();
                     break;
                 default :
                     break;
 
             }
         }
 
     }
 
 
     private void useTool(float x, float y) {
         if (currentTool != null) {
             switch (currentTool ){
 
                 case SMITE:
                     Creature closestCreature = game.getGameWorld().getClosestCreature(x, y);
 
                     if (closestCreature != null) {
                         System.out.println("Ha Haa!  I smite you, creature "+closestCreature+" at " + x + ", " + y);
 
                         closestCreature.damage(100);
                     }
 
                     break;
 
                 case LOVE:
                     break;
 
                 case MOVE:
                     break;
                 case RAGE:
                     break;
                 case FEED:
                     break;
                 default :
                     break;
 
             }
         }
     }
 
     @Override
     public void update(float timeDelta) {
         super.update(timeDelta);
 
         // Show mana
         manaLabel.setText("Mana: " + (int)getMana());
 
         if (cursorEffect != null){
             cursorEffect.update(timeDelta );
             cursorEffect.setPosition(Gdx.input.getX(), (Gdx.graphics.getHeight()-Gdx.input.getY()));
         }
 
         // listen for presses
         //Gdx.input.
 
 
     }
 
     public void topLayerRender(TextureAtlas atlas, SpriteBatch spriteBatch) {
         if (cursorEffect != null){
             cursorEffect.draw(spriteBatch);
         }
 
 
     }
 
     @Override
     public void onDispose() {
         /* This frees the image atlas, can't do that..
         if (cursorEffect != null){
             cursorEffect.dispose();
         }
         */
     }
 }
