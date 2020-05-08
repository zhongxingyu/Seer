 package kauhsa.sokoban.ui.gamestates;
 
 import kauhsa.sokoban.core.utils.Direction;
 import kauhsa.sokoban.game.SokobanGame;
 import kauhsa.sokoban.level.InvalidLevelException;
 import kauhsa.sokoban.resources.FontLoader;
 import kauhsa.sokoban.ui.cutetileset.CuteTileSet;
 import kauhsa.sokoban.ui.elements.label.Label;
 import kauhsa.sokoban.ui.elements.HorizontalAlignment;
 import kauhsa.sokoban.ui.elements.world.WorldRenderer;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 import org.newdawn.slick.util.Log;
 
 /**
  * Slick GameState for actual gameplay.
  */
 public class InGameState extends BasicGameState {
 
     private SokobanGame game = null;
     private WorldRenderer worldRenderer = null;
     private Label levelNameLabel = null;
     private Label moveCountLabel = null;
 
     @Override
     public int getID() {
         return GameStates.IN_GAME.ordinal();
     }
 
     /**
      * Load SokobanGame to be played in this GameState.
      * 
      * @param game game to be played.
      */
     public void loadGame(SokobanGame game) throws SlickException {
         this.game = game;
         worldRenderer = new WorldRenderer(game.getWorld(), new CuteTileSet());        
         
         updateLevelTextLabel();
         updateMoveCountLabel();
     }
 
     public void render(GameContainer gc, StateBasedGame sbg, Graphics grphcs) throws SlickException {
         if (game == null) {
             return;
         }
 
         this.worldRenderer.render(grphcs, 25, 50, gc.getWidth() - 50, gc.getHeight() - 75);
         this.levelNameLabel.render(grphcs, 50, 50, gc.getWidth() / 2 - 50, 50);
         this.moveCountLabel.render(grphcs, gc.getWidth() / 2 + 50, 50, gc.getWidth() / 2 - 100, 50);
     }
 
     public void update(GameContainer gc, StateBasedGame sbg, int i) throws SlickException {
         if (game == null) {
             return;
         }
 
         Input input = gc.getInput();
 
         if (input.isKeyPressed(Input.KEY_UP)) {
             game.movePlayer(Direction.UP);
         } else if (input.isKeyPressed(Input.KEY_DOWN)) {
             game.movePlayer(Direction.DOWN);
         } else if (input.isKeyPressed(Input.KEY_LEFT)) {
             game.movePlayer(Direction.LEFT);
         } else if (input.isKeyPressed(Input.KEY_RIGHT)) {
             game.movePlayer(Direction.RIGHT);
         } else if (input.isKeyPressed(Input.KEY_ESCAPE)) {
            input.clearKeyPressedRecord();
             sbg.enterState(GameStates.LEVEL_MENU.ordinal());
         } else if (input.isKeyPressed(Input.KEY_R)) {
             restart();
         }
         
         updateMoveCountLabel();
         
         if (game.isFinished()) {
             sbg.enterState(GameStates.LEVEL_DONE.ordinal());
         }
     }
 
     public void init(GameContainer gc, StateBasedGame sbg) throws SlickException {
         moveCountLabel = new Label();        
         moveCountLabel.setFont(FontLoader.loadAwtFontToSlick("Ubuntu", 0, 30));
         moveCountLabel.setHorizontalAlignment(HorizontalAlignment.RIGHT);
         
         levelNameLabel = new Label();
         levelNameLabel.setFont(FontLoader.loadAwtFontToSlick("Ubuntu", 0, 30));
     }
 
     /**
      * Load the currently loaded SokobanGame again.
      */
     private void restart() throws SlickException {
         try {
             loadGame(new SokobanGame(game.getLevel()));
         } catch (InvalidLevelException ex) {
             Log.error("Could not restart level", ex);
         }
     }
 
     /**
      * Update the label showing current move count.
      */
     private void updateMoveCountLabel() {
         moveCountLabel.setText("Moves: " + game.getMoveCount());
     }
 
     /**
      * Update the label showing current level name.
      */
     private void updateLevelTextLabel() {
         String name = game.getLevel().getMetadata("name");
         if (name == null) {
             name = "(Unnamed)";
         }
         levelNameLabel.setText("Level: " + name);
     }
 }
