 package net.stickboyproductions.tetrisattack.model;
 
 import com.google.common.collect.Sets;
 import net.stickboyproductions.tetrisattack.actions.*;
 import net.stickboyproductions.tetrisattack.constants.GameConfig;
 import net.stickboyproductions.tetrisattack.enums.BlockState;
 import net.stickboyproductions.tetrisattack.enums.GameState;
 import net.stickboyproductions.tetrisattack.generators.StartGridGenerator;
 import net.stickboyproductions.tetrisattack.interfaces.Drawable;
 import net.stickboyproductions.tetrisattack.io.InputController;
 import net.stickboyproductions.tetrisattack.levelloader.LevelLoaderProcess;
 import net.stickboyproductions.tetrisattack.processors.ChainBuilderProcess;
 import net.stickboyproductions.tetrisattack.timing.GameClock;
 import net.stickboyproductions.tetrisattack.timing.SystemClock;
 import net.stickboyproductions.tetrisattack.ui.*;
 
 import javax.inject.Inject;
 import java.util.Set;
 
 import static net.stickboyproductions.tetrisattack.constants.GameConfig.BLOCKS_IN_ROW_COUNT;
 import static net.stickboyproductions.tetrisattack.constants.GameConfig.ROWS_IN_GRID;
 
 /**
  * An instance of a game and all the logic to play it.
  * <p/>
  * Feels a bit overbloated.
  * <p/>
  * User: Pete
  * Date: 27/11/12
  * Time: 22:13
  */
 public class Game extends AbstractControllable implements Drawable {
 
   public static final int MESSAGE_X = 200;
   public static final int MESSAGE_Y = 200;
 
   private DrawableRegister drawableRegister;
   private Grid grid;
   private ChainBuilderProcess chainBuilderProcess;
   private SystemClock systemClock;
   private StartGridGenerator startGridGenerator;
   private InputController inputController;
   private GameClock gameClock;
   private GridMoveUp gridMoveUp;
   private Speed speed;
   private Score score = new Score();
 
   private Chain chainFinished;
 
   // UI things
 
   private GameClockPanel gameClockPanel;
   private GameSpeedPanel gameSpeedPanel;
   private GameScorePanel gameScorePanel;
 
   private GameState gameState = GameState.STARTING;
 
   private PlayerSelection playerSelection;
 
   // level loader!
   LevelLoaderProcess levelLoader = new LevelLoaderProcess();
 
   // TODO : wrap in model?
 
   @Inject
   public Game(DrawableRegister drawableRegister, ChainBuilderProcess chainBuilderProcess,
               GameClock gameClock, SystemClock systemClock,
               StartGridGenerator startGridGenerator, InputController inputController, Grid grid) {
     this.drawableRegister = drawableRegister;
     this.chainBuilderProcess = chainBuilderProcess;
     this.systemClock = systemClock;
     this.startGridGenerator = startGridGenerator;
     this.inputController = inputController;
     this.grid = grid;
     this.gameClock = gameClock;
   }
 
   public void init() {
     grid.init();
     for (int y = 0; y < ROWS_IN_GRID; y++) {
       for (int x = 0; x < BLOCKS_IN_ROW_COUNT; x++) {
         drawableRegister.register(grid.get(x, y));
       }
     }
 
//    try {
//      levelLoader.load("tutorial/skillchains/time-lag-4.xml", grid);
//    } catch (Exception e) {
//      System.out.println(e.getMessage());
//      System.out.println("error loading level so loading a random one");
       startGridGenerator.generate(grid);
//    }
 
     playerSelection = new PlayerSelection(gameClock, grid, grid.get(GameConfig.PLAYER_START_X, GameConfig.PLAYER_START_Y),
       drawableRegister, inputController);
 
     inputController.registerHandler(this);
 
     GameStart gameStart = new GameStart(this, drawableRegister);
     systemClock.register(gameStart);
 
     // speed will get passed in
     speed = new Speed(1);
 
     gameClockPanel = new GameClockPanel(drawableRegister, gameClock);
     gameSpeedPanel = new GameSpeedPanel(drawableRegister, speed);
     gameScorePanel = new GameScorePanel(drawableRegister, score);
 
     drawableRegister.register(this);
     playerSelection.enableMovement();
 
     // TODO : Move to some load state ?
     // Test block fall
 //    grid.get(0, 0).setBlockState(BlockState.IDLE);
 //    grid.get(0, 0).setShape(Shape.RED);
 //    grid.get(0, 1).setBlockState(BlockState.IDLE);
 //    grid.get(0, 1).setShape(Shape.RED);
 //    grid.get(0, 8).setBlockState(BlockState.IDLE);
 //    grid.get(0, 8).setShape(Shape.RED);
 //    grid.get(0, 7).setBlockState(BlockState.IDLE);
 //    grid.get(0, 7).setShape(Shape.CYAN);
 
     // Test chain clear
 //    grid.get(0, 0).setBlockState(BlockState.IDLE);
 //    grid.get(0, 0).setShape(Shape.RED);
 //    grid.get(1, 0).setBlockState(BlockState.IDLE);
 //    grid.get(1, 0).setShape(Shape.RED);
 //    grid.get(2, 0).setBlockState(BlockState.IDLE);
 //    grid.get(2, 0).setShape(Shape.RED);
 //
 //    grid.get(0, 1).setBlockState(BlockState.IDLE);
 //    grid.get(0, 1).setShape(Shape.BLUE);
 //    grid.get(1, 1).setBlockState(BlockState.IDLE);
 //    grid.get(1, 1).setShape(Shape.BLUE);
 //    grid.get(2, 1).setBlockState(BlockState.IDLE);
 //    grid.get(2, 1).setShape(Shape.BLUE);
 //
 //    grid.get(0, 2).setBlockState(BlockState.IDLE);
 //    grid.get(0, 2).setShape(Shape.GREEN);
 //    grid.get(0, 3).setBlockState(BlockState.IDLE);
 //    grid.get(0, 3).setShape(Shape.GREEN);
 //    grid.get(0, 4).setBlockState(BlockState.IDLE);
 //    grid.get(0, 4).setShape(Shape.GREEN);
   }
 
   public void startGame() {
     gameState = GameState.RUNNING;
     gameClock.start();
 
     this.gridMoveUp = new GridMoveUp(this, playerSelection, speed);
     gameClock.register(gridMoveUp);
   }
 
   public void update() {
     gameClock.tick();
     if (gameState.equals(GameState.RUNNING)) {
       Set<Block> comboBlocks = Sets.newHashSet();
       // Test block fall
 //    if(count >= 10) {
 //      grid.get(0, 3).setBlockState(BlockState.IDLE);
 //      grid.get(0, 3).setShape(Shape.GREEN);
 //      count = -100000000;
 //    }
 
       for (int y = 0; y < ROWS_IN_GRID; y++) {
         for (int x = 0; x < BLOCKS_IN_ROW_COUNT; x++) {
           Block currentBlock = grid.get(x, y);
           if (currentBlock.getBlockState().equals(BlockState.IDLE)) {
             if (currentBlock.canFall()) {
               BlockFall newBlockFall = new BlockFall(currentBlock, this, chainFinished);
               gameClock.register(newBlockFall);
             } else {
               if (y > 0) {
                 // Build chain
                 Set<Block> chain = chainBuilderProcess.buildClearableChain(currentBlock, grid);
                 if (chain.size() >= 3) {
                   System.out.println("Found a chain - " + chain.size() + " " + chain.iterator().next().getShape());
                   comboBlocks.addAll(chain);
                 }
               }
             }
           }
         }
       }
       // calculate bonus points for combo
       if (comboBlocks.size() >= 3) {
         System.out.println("There was a combo of size [" + comboBlocks.size() + "]");
 
         Combo combo;
         if(chainFinished != null) {
           combo = new Combo(comboBlocks, this, gameClock, chainFinished);
         } else {
           combo = new Combo(comboBlocks, this, gameClock);
         }
         gameClock.register(combo);
       }
       if (chainFinished != null) {
         chainFinished = null;
       }
     }
   }
 
   public DrawableRegister getDrawableRegister() {
     return drawableRegister;
   }
 
   @Override
   public void actionPressed() {
     if (gameState.equals(GameState.RUNNING)) {
       Block leftBlock = grid.get(playerSelection.getLeftX(), playerSelection.getY());
       Block rightBlock = grid.get(playerSelection.getRightX(), playerSelection.getY());
       if (leftBlock != null && rightBlock != null && leftBlock.canSwap() && rightBlock.canSwap()) {
         System.out.println("Left - " + leftBlock.getBlockState());
         System.out.println("Right - " + rightBlock.getBlockState());
         ShapeSwap shapeSwap = new ShapeSwap(leftBlock, rightBlock);
         gameClock.register(shapeSwap);
       }
     }
   }
 
   @Override
   public void pausePressed() {
     if (gameState.equals(GameState.RUNNING)) {
       gameClock.pause();
       playerSelection.disableMovement();
       gameState = GameState.PAUSED;
     } else if (gameState.equals(GameState.PAUSED)) {
       gameClock.resume();
       playerSelection.enableMovement();
       gameState = GameState.RUNNING;
     }
   }
 
   @Override
   public void newLinePressed() {
     if (gameState.equals(GameState.RUNNING)) {
       score.addToScore(1);
       gridMoveUp.speedUp();
     }
   }
 
   public void gameOver() {
     gameState = GameState.GAME_OVER;
     gameClock.pause();
     playerSelection.disableMovement();
   }
 
   public void moveUp() {
     // If top line has any blocks with shapes in then the game should end
     for (int x = 0; x < BLOCKS_IN_ROW_COUNT; x++) {
       if (!grid.get(x, 11).getBlockState().equals(BlockState.EMPTY)) {
         gameOver();
         return;
       }
     }
 
     // Stop old cells from being drawn
     for (int x = 0; x < BLOCKS_IN_ROW_COUNT; x++) {
       drawableRegister.unregister(grid.get(x, 11));
     }
     // Modify state
     grid.moveAllUp();
 
     // Remove offset caused by slow move up
     for (int y = 0; y < ROWS_IN_GRID; y++) {
       for (int x = 0; x < BLOCKS_IN_ROW_COUNT; x++) {
         Block block = grid.get(x, y);
         block.setOffsetY(0);
       }
     }
     playerSelection.resetOffsetY();
 
     // Reset the counter on the move up action
     gridMoveUp.reset();
 
     // Make sure all new blocks are drawn
     for (int x = 0; x < BLOCKS_IN_ROW_COUNT; x++) {
       drawableRegister.register(grid.get(x, 0));
     }
   }
 
   public Grid getGrid() {
     return grid;
   }
 
   public GameState getGameState() {
     return gameState;
   }
 
   @Override
   public void draw(Screen screen) {
     if (gameState.equals(GameState.PAUSED)) {
       screen.drawText(MESSAGE_X, MESSAGE_Y, "Paused");
     } else if (gameState.equals(GameState.GAME_OVER)) {
       screen.drawText(MESSAGE_X, MESSAGE_Y, "Game Over");
     }
   }
 
   public GameClockPanel getGameClockPanel() {
     return gameClockPanel;
   }
 
   public Score getScore() {
     return score;
   }
 
   public GridMoveUp getGridMoveUp() {
     return gridMoveUp;
   }
 
   public Chain getChainFinished() {
     return chainFinished;
   }
 
   public void setChainFinished(Chain chainFinished) {
     this.chainFinished = chainFinished;
   }
 
   public Speed getSpeed() {
     return speed;
   }
 }
