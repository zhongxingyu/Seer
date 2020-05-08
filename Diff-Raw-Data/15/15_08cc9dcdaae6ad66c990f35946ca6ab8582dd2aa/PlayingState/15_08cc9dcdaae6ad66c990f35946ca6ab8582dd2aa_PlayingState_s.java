 package com.bitspatter.states;
 
 import org.newdawn.slick.*;
 import org.newdawn.slick.geom.Rectangle;
 import org.newdawn.slick.state.*;
 
 import com.bitspatter.*;
 import com.bitspatter.renderers.BlockRenderer;
 
 public class PlayingState extends BasicGameState implements MouseListener {
     public final static int STATE_ID = 2;
     final int SECONDS_PER_STEP = 1;
     final int BOARD_MARGIN = 10;
     final int BOARD_WIDTH = 10, BOARD_HEIGHT = 22;
 
     // Board including landed pieces.
     Board board;
     // Currently dropping piece.
     Piece currentPiece;
 
     BlockRenderer blockRenderer;
     Rectangle boardRenderArea;
    
    Image instructions;
 
     // Number of ms until we do a "soft drop" (i.e. move the current piece one step down)
     int msTillNextStep;
 
     // Whether or not we're currently in "warp" mode, i.e. game paused and allowing player to move blocks.
     private enum PlayState {
         Playing,
         Paused,
         Warping,
         Ended
     }
 
     PlayState state = PlayState.Playing;
 
     // This is the delta between the mouse pointer and the top left of the currently dragged block, to make dragging
     // feel natural.
     int dragOffsetX, dragOffsetY;
 
     @Override
     public int getID() {
         return STATE_ID;
     }
 
     @Override
     public void render(GameContainer gc, StateBasedGame game, Graphics g) throws SlickException {
         boolean warping = (state == PlayState.Warping);
         if (warping) {
             board.renderWarping(g, boardRenderArea);
         }
 
         currentPiece.render(g);
 
         if (warping) {
             Input input = gc.getInput();
             g.setClip(boardRenderArea);
             currentPiece.renderDraggable(g, input.getMouseX() - dragOffsetX, input.getMouseY() - dragOffsetY);
             g.clearClip();
         }
 
         board.render(g, boardRenderArea, warping);
        
        g.drawImage(instructions, boardRenderArea.getMaxX() + 2 * BOARD_MARGIN, BOARD_MARGIN); 
     }
 
     @Override
     public void init(GameContainer gc, StateBasedGame game) throws SlickException {
         initializeRenderer(gc);
         Piece.createPieces(blockRenderer);
         initializeInput(gc);
        
         instructions = new Image("playing_instructions.png");
     }
 
     @Override
     public void enter(GameContainer container, StateBasedGame game) throws SlickException {
         board = new Board(BOARD_WIDTH, BOARD_HEIGHT, blockRenderer);
         currentPiece = Piece.getRandomPiece();
         msTillNextStep = SECONDS_PER_STEP * 1000;
     }
 
     private void initializeInput(GameContainer gc) {
         Input input = gc.getInput();
         input.enableKeyRepeat();
         input.addMouseListener(this);
     }
 
     private void initializeRenderer(GameContainer gc) {
         int boardHeight = gc.getHeight() - BOARD_MARGIN * 2;
         int blockSize = boardHeight / BOARD_HEIGHT;
         boardHeight = blockSize * BOARD_HEIGHT;
         float actualYMargin = (gc.getHeight() - boardHeight) / 2.0f;
 
         boardRenderArea = new Rectangle(BOARD_MARGIN, actualYMargin, blockSize * BOARD_WIDTH, boardHeight);
         blockRenderer = new BlockRenderer(boardRenderArea, blockSize);
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
         Input input = gc.getInput();
         if (input.isKeyPressed(Input.KEY_SPACE)) {
             toggleWarping();
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
             msTillNextStep = SECONDS_PER_STEP * 1000;
             lowerPiece(game);
         } else if (input.isKeyPressed(Input.KEY_UP)) {
             while (!lowerPiece(game))
                 ;
         } else {
             msTillNextStep -= delta;
             if (msTillNextStep < 0) {
                 msTillNextStep += SECONDS_PER_STEP * 1000;
                 lowerPiece(game);
             }
         }
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
 
     private boolean lowerPiece(StateBasedGame game) throws SlickException {
         currentPiece.topLeftY++;
         if (board.pieceLanded(currentPiece)) {
             currentPiece.topLeftY--;
             if (board.finalizePiece(currentPiece)) {
                 // TODO: Switch 'state' so we'll show "game over", and then press a button to go to menu.
                 game.enterState(MenuState.STATE_ID);
             }
 
             currentPiece = Piece.getRandomPiece();
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
         if (button == 0) {
             int blockX = blockRenderer.getBlockX(x);
             int blockY = blockRenderer.getBlockX(y);
             if (board.contains(blockX, blockY)) {
                 if (currentPiece.stopDrag(blockX, blockY)) {
                     currentPiece.warped = true;
                     state = PlayState.Playing;
                     board.mutate();
                 }
             } else {
                 currentPiece.stopDrag();
             }
         }
     }
 }
