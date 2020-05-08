 package com.bitspatter.states;
 
 import java.io.IOException;
 
 import org.newdawn.slick.*;
 import org.newdawn.slick.geom.Rectangle;
 import org.newdawn.slick.openal.*;
 import org.newdawn.slick.state.*;
 import org.newdawn.slick.state.transition.*;
 
 import com.bitspatter.*;
 import com.bitspatter.Board.BoardListener;
 import com.bitspatter.EntropyMeter.EntropyListener;
 import com.bitspatter.renderers.BlockRenderer;
 
 public class PlayingState extends CommonState implements MouseListener, BoardListener, EntropyListener {
     private enum PlayState {
         Playing,
         Paused,
         Warping,
         Ended
     }
 
     public final static int STATE_ID = 2;
     final int INITIAL_MS_PER_STEP = 700;
     final int MS_PER_LEVEL = 70;
     final int ROWS_PER_LEVEL = 10;
     final int MAX_LEVEL = 10;
     final int BOARD_MARGIN = 10;
     final int BOARD_WIDTH = 10, BOARD_HEIGHT = 22;
 
     // Board including landed pieces.
     Board board;
     // Currently dropping piece and the coming piece.
     Piece currentPiece, nextPiece;
 
     BlockRenderer blockRenderer;
     NextPieceBox nextPieceBox;
     Rectangle boardRenderArea;
 
     Image instructions, warpingInstructions, gameOverImage;
     Audio dragInvalidSound, dragValidSound, pieceLandedSound, mutateSound;
 
     Score score;
     EntropyMeter entropyMeter;
 
     // Number of ms until we do a "soft drop" (i.e. move the current piece one step down)
     int msTillNextStep;
 
     // Number of rows needing clearing before you reach next level
     int rowsTillNextLevel;
     // What level we are on (affects speed &
     int level;
     PlayState state;
 
     // This is the delta between the mouse pointer and the top left of the currently dragged block, to make dragging
     // feel natural.
     private int dragOffsetX, dragOffsetY;
 
     final Color highlightColor = new Color(1.0f, 1.0f, 1.0f, 0.6f);
 
     @Override
     public int getID() {
         return STATE_ID;
     }
 
     @Override
     public void init(GameContainer gc, StateBasedGame game) throws SlickException {
         initializeRenderers(gc);
         Piece.createPieces(blockRenderer);
         initializeInput(gc);
 
         instructions = new Image("playing_instructions.png");
         warpingInstructions = new Image("warping_instructions.png");
         gameOverImage = new Image("game_over.png");
 
         SoundStore soundStore = SoundStore.get();
         soundStore.init();
 
         try {
             dragInvalidSound = soundStore.getWAV("drag_invalid.wav");
             dragValidSound = soundStore.getWAV("drag_valid.wav");
             pieceLandedSound = soundStore.getWAV("piece_landed.wav");
             mutateSound = soundStore.getWAV("mutation.wav");
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
     private void initializeInput(GameContainer gc) {
         Input input = gc.getInput();
         input.enableKeyRepeat();
         input.addMouseListener(this);
     }
 
     private void initializeRenderers(GameContainer gc) {
         int boardHeight = gc.getHeight() - BOARD_MARGIN * 2;
         int blockSize = boardHeight / BOARD_HEIGHT;
         boardHeight = blockSize * BOARD_HEIGHT;
 
         boardRenderArea = new Rectangle(BOARD_MARGIN, BOARD_WIDTH, blockSize * BOARD_WIDTH, boardHeight);
         blockRenderer = new BlockRenderer(boardRenderArea, blockSize);
         nextPieceBox = new NextPieceBox(new BlockRenderer(null, blockSize));
     }
 
     @Override
     public void enter(GameContainer container, StateBasedGame game) throws SlickException {
         board = new Board(BOARD_WIDTH, BOARD_HEIGHT, this, blockRenderer);
         nextPiece = Piece.getRandomPiece();
         currentPiece = getNextPiece();
         score = new Score();
         entropyMeter = new EntropyMeter(this);
 
         msTillNextStep = INITIAL_MS_PER_STEP;
         rowsTillNextLevel = ROWS_PER_LEVEL;
         level = 1;
         state = PlayState.Playing;
     }
 
     @Override
     public void render(GameContainer gc, StateBasedGame game, Graphics g) throws SlickException {
         boolean warping = (state == PlayState.Warping);
         boolean paused = (state == PlayState.Paused);
         if (warping || paused) {
             board.renderWarping(g, boardRenderArea);
         }
 
         if (warping && currentPiece.dragging) {
             Input input = gc.getInput();
             int blockX = getBlockXFromMouseX(input.getMouseX());
             int blockY = getBlockYFromMouseY(input.getMouseY());
             if (currentPiece.isValidDropTarget(currentPiece.getLocalX(blockX), currentPiece.getLocalY(blockY))) {
                 blockRenderer.render(g, blockX, blockY, highlightColor);
             }
         }
 
         if (!paused && currentPiece != null) {
             currentPiece.render(g, warping);
         }
 
         if (warping) {
             Input input = gc.getInput();
             g.setClip(boardRenderArea);
             currentPiece.renderDraggable(g, input.getMouseX() - dragOffsetX, input.getMouseY() - dragOffsetY);
             g.clearClip();
         }
 
         board.render(g, boardRenderArea, warping || paused);
 
         Image rightHandPane = instructions;
         if (state == PlayState.Ended) {
             rightHandPane = gameOverImage;
         } else if (warping) {
             rightHandPane = warpingInstructions;
         }
 
         float rightPaneX = boardRenderArea.getMaxX() + 2 * BOARD_MARGIN;
         float rightPaneY = BOARD_MARGIN;
 
         nextPieceBox.render(g, rightPaneX, rightPaneY);
         rightPaneY += nextPieceBox.getHeight() + BOARD_MARGIN;
 
         score.render(g, rightPaneX, rightPaneY);
         rightPaneY += score.getHeight() + BOARD_MARGIN;
 
         g.drawImage(rightHandPane, rightPaneX, rightPaneY);
 
         rightPaneY = BOARD_MARGIN;
         rightPaneX += nextPieceBox.getWidth() + BOARD_MARGIN;
         entropyMeter.render(g, rightPaneX, rightPaneY);
     }
 
     private void toggleWarping() {
         if (state == PlayState.Playing) {
             // You can only warp each piece once.
             if (currentPiece.warped)
                 return;
             state = PlayState.Warping;
         } else if (state == PlayState.Warping) {
             state = PlayState.Playing;
             currentPiece.stopDrag();
         }
     }
 
     @Override
     public void update(GameContainer gc, StateBasedGame game, int delta) throws SlickException {
         super.update(gc, game, delta);
 
         Input input = gc.getInput();
         if (input.isKeyPressed(Input.KEY_SPACE)) {
             toggleWarping();
         }
 
         if (input.isKeyPressed(Input.KEY_ESCAPE)) {
             if (state == PlayState.Playing) {
                 state = PlayState.Paused;
             } else if (state == PlayState.Paused) {
                 state = PlayState.Playing;
             } else if (state == PlayState.Ended) {
                 game.enterState(MenuState.STATE_ID, new FadeOutTransition(), new FadeInTransition());
             }
         }
 
         if (state != PlayState.Playing) {
             return;
         }
 
         board.update(delta);
 
         if (input.isKeyPressed(Input.KEY_LEFT)) {
             movePieceHorizontally(-1);
         } else if (input.isKeyPressed(Input.KEY_RIGHT)) {
             movePieceHorizontally(1);
         }
 
         if (input.isKeyPressed(Input.KEY_Z)) {
             rotatePiece(false);
         } else if (input.isKeyPressed(Input.KEY_X)) {
             rotatePiece(true);
         }
 
         if (input.isKeyPressed(Input.KEY_DOWN)) {
             msTillNextStep = INITIAL_MS_PER_STEP - (MS_PER_LEVEL * (level - 1));
             lowerPiece();
         } else if (input.isKeyPressed(Input.KEY_UP)) {
             while (!lowerPiece())
                 ;
         } else {
             msTillNextStep -= delta;
             if (msTillNextStep < 0) {
                 msTillNextStep = INITIAL_MS_PER_STEP - (MS_PER_LEVEL * (level - 1));
                 lowerPiece();
             }
         }
     }
 
     private Piece getNextPiece() throws SlickException {
         Piece returnedPiece = nextPiece;
         nextPiece = Piece.getRandomPiece();
         nextPieceBox.setNextPiece(nextPiece);
 
         return returnedPiece;
     }
 
     private void rotatePiece(boolean clockwise) throws SlickException {
         Piece newPiece = currentPiece.rotated(clockwise);
         if (board.nudgeToValid(newPiece)) {
             currentPiece = newPiece;
         }
     }
 
     private void movePieceHorizontally(int steps) {
         currentPiece.topLeftX += steps;
         if (board.pieceLanded(currentPiece)) {
             currentPiece.topLeftX -= steps;
         }
     }
 
     private boolean lowerPiece() throws SlickException {
         currentPiece.topLeftY++;
         if (board.pieceLanded(currentPiece)) {
             currentPiece.topLeftY--;
             if (board.finalizePiece(currentPiece)) {
                 state = PlayState.Ended;
                 currentPiece = null;
             } else {
                 pieceLandedSound.playAsSoundEffect(1.0f, 1.0f, false);
                 currentPiece = getNextPiece();
             }
 
             return true;
         }
 
         return false;
     }
 
     @Override
     public boolean isAcceptingInput() {
         return (state == PlayState.Warping);
     }
 
     @Override
     public void mousePressed(int button, int x, int y) {
         if (button != 0) {
             return;
         }
 
         int blockX = blockRenderer.getBlockX(x);
         int blockY = blockRenderer.getBlockX(y);
         if (!currentPiece.contains(blockX, blockY)) {
             return;
         }
 
         dragOffsetX = x - blockRenderer.getX(blockX);
         dragOffsetY = y - blockRenderer.getY(blockY);
         currentPiece.startDrag(blockX, blockY);
     }
 
     @Override
     public void mouseReleased(int button, int x, int y) {
         if (currentPiece.dragging && button == 0) {
             int blockX = getBlockXFromMouseX(x);
             int blockY = getBlockYFromMouseY(y);
             if (board.contains(blockX, blockY)) {
                 if (currentPiece.stopDrag(blockX, blockY)) {
                     currentPiece.warped = true;
                     state = PlayState.Playing;
                     entropyMeter.addEntropy();
                     dragValidSound.playAsSoundEffect(1.0f, 1.0f, false);
                 } else {
                     dragInvalidSound.playAsSoundEffect(1.0f, 1.0f, false);
                 }
             } else {
                 currentPiece.stopDrag();
             }
         }
     }
 
     int getBlockXFromMouseX(int mouseX) {
         return blockRenderer.getBlockX(mouseX - dragOffsetX);
     }
 
     int getBlockYFromMouseY(int mouseY) {
         return blockRenderer.getBlockY(mouseY - dragOffsetY);
     }
 
     @Override
     public void onRowsCleared(int numRows) {
         score.clearRows(1, numRows);
         rowsTillNextLevel -= numRows;
         if (rowsTillNextLevel <= 0) {
             rowsTillNextLevel += ROWS_PER_LEVEL;
             level++;
         }
     }
 
     @Override
     public void onEntropyOverflown() {
         board.mutate();
         mutateSound.playAsSoundEffect(1.0f, 1.0f, false);
     }
 }
