 package demo.client.local;
 
 import com.google.gwt.event.dom.client.KeyCodes;
 import com.google.gwt.event.dom.client.KeyPressEvent;
 import com.google.gwt.event.dom.client.KeyPressHandler;
 import com.google.gwt.user.client.Timer;
 
 import demo.client.shared.model.BackgroundBlockModel;
 import demo.client.shared.model.BlockOverflow;
 import demo.client.shared.model.BoardModel;
 
 /*
  * A controller class for a Block Drop game. Handles game loop and user input.
  */
 public class BoardController implements KeyPressHandler {
 	
 	private static final int KEY_SPACE_BAR = 32;
 	/* A Block Drop board model. */
 	private BoardModel model;
 	/* A block model. */
 	private Block activeBlock;
 	/* A block model. */
 	private Block nextBlock;
 	
 	/* A timer for running the game loop. */
 	private Timer timer;
 	/* 
 	 * The number of iterations for a block to drop one square on the board. 
 	 * Must be a multiple of 4.
 	 */
 	private int dropIncrement = 16;
 	/* The time (in milliseconds) between calls to the game loop. */
 	private int loopTime = 25;
 	/* A counter of ellapsed iterations since a block last dropped. */
 	private int loopCounter = 0;
 	
 	/* 
 	 * The amount of rows (positive is down) the active block on the board
 	 * should move this loop iteration.
 	 */
 	private int rowMove = 0;
 	/*
 	 * The amount of columns (positive is right) the active block on the board
 	 * should move this loop iteration.
 	 */
 	private int colMove = 0;
 	/* True if the active block should rotate this loop iteration. */
 	private boolean rotate;
 	/* True if the active block should drop to the bottom of the screen. */
 	private boolean drop;
 	/* True if the active block drop speed should be increased. */
 	private boolean fast;
 	
 	private ClearState clearState = ClearState.START;
 	private Block toBeCleared;
 	private Block bgBlock;
 	
 	/* The BoardPage on which this Block Drop game is displayed. */
 	private BoardPage boardPage;
 	
 	/*
 	 * Create a BoardController instance.
 	 */
 	public BoardController() {
 		// Initiate BoardModel.
 		model = new BoardModel();
 		activeBlock = Block.getBlockInstance(model.getActiveBlock());
 		nextBlock = Block.getBlockInstance(model.getNextBlock());
 		
 		// Create a timer to run the game loop.
 		timer = new Timer() {
 			@Override
 			public void run() {
 				update();
 			}
 		};
 	}
 	
 	/*
 	 * Set the page which this BoardController controls.
 	 * 
 	 * @param boardPage The BoardPage to be controlled.
 	 */
 	public void setPage(BoardPage boardPage) {
 		this.boardPage = boardPage;
 	}
 	
 	/*
 	 * Handle user input, and update the game state and view. This method is
 	 * is called every loopTime milliseconds.
 	 */
 	public void update() {
 		boolean moved = false;
 		
 		// Check for rows to clear. Rows will stay in model until fully dealt with.
 		int numFullRows = model.numFullRows();
 		if (numFullRows > 0) {
 			if (clearState.getCounter() == 0)
 			switch(clearState) {
 				case START:
 					// Get blocks to be cleared.
 					toBeCleared = new Block(model.getFullRows());
 					bgBlock = new Block(model.getAboveFullRows());
 					break;
 				case FIRST_UNDRAW:
 				case SECOND_UNDRAW:
 				case THIRD_UNDRAW:
 				case LAST_UNDRAW:
 					boardPage.undrawBlock(0, 0, toBeCleared);
 					break;
 				case FIRST_REDRAW:
 				case SECOND_REDRAW:
 				case THIRD_REDRAW:
 					boardPage.drawBlock(0, 0, toBeCleared);
 					break;
 				case DROPPING:
 					boardPage.undrawBlock(0, 0, bgBlock);
 					// Redraw background blocks that were above cleared rows lower.
 					boardPage.drawBlock(0, Block.indexToCoord(numFullRows), bgBlock);
 					model.clearFullRows();
 					break;
 			}
 			clearState = clearState.getNextState();
 
 		// Only drop a new block if we are not clearing rows currently.
 		} else {
			// Reset the active block if necessary.
			if (!activeBlock.isModel(model.getActiveBlock())) {
				activeBlock = Block.getBlockInstance(model.getActiveBlock());
				nextBlock = Block.getBlockInstance(model.getNextBlock());
				boardPage.drawBlockToNextCanvas(nextBlock);
			}
 			// Update the position of the active block and record movement.
 			moved = activeBlockUpdate();
 			
 			try {
 				// If the block could not drop, start a new block.
 				if (!moved && rowMove > 0) {
 					model.initNextBlock();
 				}
 			} catch (BlockOverflow e) {
 				// TODO: Handle game ending.
 				System.out.println("Game Over.");
 
 				timer.cancel();
 			}
 			// Reset for next loop.
 			drop = false;
 			rotate = false;
 			this.colMove = 0;
 			this.rowMove = 0;
 			loopCounter = loopCounter == dropIncrement ? 0 : loopCounter + 1;
 		}
 
 	}
 	
 	/*
 	 * Update the active block.
 	 * 
 	 * @return True iff the active block moved during this call.
 	 */
 	private boolean activeBlockUpdate() {
 		boardPage.undrawBlock(
 				Block.indexToCoord(model.getActiveBlockCol()),
 				Block.indexToCoord(model.getActiveBlockRow()),
 				activeBlock
 				);
 		
 		// If the user wishes to drop the block, do nothing else.
 		if (drop) {
 			colMove = 0;
 			rowMove = model.getDrop();
 			rotate = false;
 		} else {
 			// Rotate the block model if the user hit rotate.
 			if (rotate) {
 				model.rotateActiveBlock();
 			}
 			// Check if the user wants to increase the speed at which the block drops
 			if (fast) {
 				rowMove = 1;
 				fast = false;
 			// Otherwise maintain the normal speed.
 			} else {
 				// Drop by one row every if counter hits dropIncrement.
 				rowMove = loopCounter == dropIncrement ? 1 : 0;
 			}
 		}
 		
 		// Attempt to move model.
 		boolean moved = model.moveActiveBlock(rowMove, colMove);
 		// If that didn't work, ignore the colMove (so that the block may still drop).
 		if (!moved)
 			moved = model.moveActiveBlock(rowMove, 0);
 		
 		// Redraw block in (possibly) new position.	
 		boardPage.drawBlock(
 				Block.indexToCoord(model.getActiveBlockCol()),
 				Block.indexToCoord(model.getActiveBlockRow()),
 				activeBlock
 				);
 		
 		return moved;
 	}
 
 	/*
 	 * Start a game of Block Drop.
 	 */
 	public void startGame() {
 		// Add this as a handler for keyboard events.
 		boardPage.addHandlerToMainCanvas(this);
 		// Start game loop.
 		timer.scheduleRepeating(loopTime);
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * @see com.google.gwt.event.dom.client.KeyPressHandler#onKeyPress(com.google.gwt.event.dom.client.KeyPressEvent)
 	 */
 	@Override
 	public void onKeyPress(KeyPressEvent event) {
 
 		/*
 		 * Some key presses return char codes whereas others return key codes.
 		 */
 		int keyCode = event.getCharCode() == 0 ? event.getNativeEvent().getKeyCode() : event.getCharCode();
 		
 		/*
 		 *  If the user pressed a key used by this game, stop the event from
 		 *  bubbling up to prevent scrolling or other undesrible events.
 		 */
 		if (keyPressHelper(keyCode)) {
 			event.stopPropagation();
 			event.preventDefault();
 		}
 	}
 	
 	/*
 	 * Alter the current state based on user input.
 	 */
 	private boolean keyPressHelper(int keyCode) {
 	// First handle relevant key presses.
 		boolean relevantKey = true;
 		switch(keyCode) {
 			case KeyCodes.KEY_LEFT:
 				System.out.println("Left key pressed.");
 				colMove = -1;
 				break;
 			case KeyCodes.KEY_RIGHT:
 				System.out.println("Right key pressed.");
 				colMove = 1;
 				break;
 			case KeyCodes.KEY_UP:
 				System.out.println("Up key pressed.");
 				rotate = true;
 				break;
 			case KeyCodes.KEY_DOWN:
 				System.out.println("Down key pressed.");
 				fast = true;
 				break;
 			case KEY_SPACE_BAR:
 				System.out.println("Space bar pressed.");
 				drop = true;
 				break;
 			default:
 				System.out.println("Key code pressed: "+keyCode);
 				relevantKey = false;
 				break;
 		}
 		
 		return relevantKey;
 	}
 }
