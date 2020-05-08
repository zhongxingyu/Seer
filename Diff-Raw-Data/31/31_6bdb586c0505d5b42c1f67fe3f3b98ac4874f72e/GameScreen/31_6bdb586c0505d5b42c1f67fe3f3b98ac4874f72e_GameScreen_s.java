 package com.ivanarellano.game.pm.screen;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Input.Keys;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.scenes.scene2d.Action;
 import com.badlogic.gdx.scenes.scene2d.Group;
 import com.badlogic.gdx.scenes.scene2d.OnActionCompleted;
 import com.badlogic.gdx.scenes.scene2d.actions.MoveBy;
 import com.badlogic.gdx.scenes.scene2d.ui.Image;
 import com.ivanarellano.game.pm.Assets;
 import com.ivanarellano.game.pm.Assets.Colors;
 import com.ivanarellano.game.pm.Board;
 import com.ivanarellano.game.pm.Direction;
 import com.ivanarellano.game.pm.GameOverUI;
 import com.ivanarellano.game.pm.GameState;
 import com.ivanarellano.game.pm.PmGame;
 import com.ivanarellano.game.pm.PmScreen;
 
 public class GameScreen extends PmScreen {
 	public GameState state = GameState.READY;
 	public Board board = new Board("123658740");
 	
 	/* easy */
 	//public Board board = new Board("123456708");
 	
 	Group groupBoard = new Group("board");
 	Group groupTiles = new Group("tiles");
 	Image grassBoard = new Image(Assets.atlas.findRegion("grassboard"));
 	GameOverUI gameOverUI = new GameOverUI(this);
 	
 	int totalMoves = 0;
 
 	public GameScreen(PmGame game) {
 		super(game);
 		Gdx.gl.glClearColor(Colors.DARK_NAVY.r, Colors.DARK_NAVY.g, Colors.DARK_NAVY.b, Colors.DARK_NAVY.a);
 
 		initBoardGraphics();
 		
 		if (board.hasWon())
 			state = GameState.OVER;
 	}
 
 	public void update(float delta) {
 		game.stage.act(delta);
 		
 		if (state == GameState.READY) {
 			
 			if (Gdx.input.isKeyPressed(Keys.LEFT) || Gdx.input.getAccelerometerY() <= -7.0f) {
 				if (board.checkBounds(Direction.LEFT)) {
 					state = GameState.ACTING;
 
 					Action moveTo = MoveBy.$(-215.0f, 0.0f, 0.6f).setCompletionListener(
 							new OnActionCompleted() {
 								@Override
 								public void completed(Action action) {
 									state = GameState.READY;
 									board.slideTile(Direction.LEFT);
									totalMoves++;
 								}
 							});
 					
 					board.tiles[board.btRow][board.btCol+1].action(moveTo);
 				}
 			} else if (Gdx.input.isKeyPressed(Keys.RIGHT) || Gdx.input.getAccelerometerY() >= 7.0f) {
 				if (board.checkBounds(Direction.RIGHT)) {
 					state = GameState.ACTING;
 
 					Action moveTo = MoveBy.$(215.0f, 0.0f, 0.6f).setCompletionListener(
 							new OnActionCompleted() {
 								@Override
 								public void completed(Action action) {
 									state = GameState.READY;
 									board.slideTile(Direction.RIGHT);
									totalMoves++;
 								}
 							});
 
 					board.tiles[board.btRow][board.btCol-1].action(moveTo);
 				}
 			
 			} else if (Gdx.input.isKeyPressed(Keys.DOWN) || Gdx.input.getAccelerometerX() >= 8.0f) {
 				if (board.checkBounds(Direction.DOWN)) {
 					state = GameState.ACTING;
 					
 					Action moveTo = MoveBy.$(0.0f, -215.0f, 0.6f).setCompletionListener(
 							new OnActionCompleted() {
 								@Override
 								public void completed(Action action) {
 									state = GameState.READY;
 									board.slideTile(Direction.DOWN);
									totalMoves++;
 								}
 							});
 					
 					board.tiles[board.btRow-1][board.btCol].action(moveTo);
 				}
 			
 			} else if (Gdx.input.isKeyPressed(Keys.UP) || Gdx.input.getAccelerometerX() <= -6.5f) {
 				if (board.checkBounds(Direction.UP)) {
 					state = GameState.ACTING;
 					
 					Action moveTo = MoveBy.$(0.0f, 215.0f, 0.6f).setCompletionListener(
 							new OnActionCompleted() {
 								@Override
 								public void completed(Action action) {
 									state = GameState.READY;
 									board.slideTile(Direction.UP);
									totalMoves++;
 								}
 							});
 					
 					board.tiles[board.btRow+1][board.btCol].action(moveTo);
 				}
 			
 			}
 			
 			if (board.hasWon()) {
 				state = GameState.OVER;
 				gameOverUI.init();
 			}
 			
 		}
 
 	}
 
 	@Override
 	public void render(float delta) {
 		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 
 		update(delta);
 
 		game.stage.draw();
 	}
 
 	@Override
 	public void resize(int width, int height) {
 	}
 
 	@Override
 	public void show() {
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
 		game.stage.dispose();
 	}
 
 	void initBoardGraphics() {
 		// place board in the middle
 		groupBoard.x = PmGame.SCREEN_WIDTH / 2 - grassBoard.width / 2;
 		groupBoard.y = PmGame.SCREEN_HEIGHT / 2 - grassBoard.height / 2;
 
 		// add and arrange tiles
 		int XOffset = 45;
 		int YOffset = 465;
 		for (int row = 0; row < Board.ROWS; row++) {
 			for (int col = 0; col < Board.COLS; col++) {
 				board.tiles[row][col].x = XOffset;
 				board.tiles[row][col].y = YOffset;
 
 				groupTiles.addActor(board.tiles[row][col]);
 
 				if (col >= 2)
 					XOffset = 45;
 				else
 					XOffset += 215;
 
 				if (board.tiles[row][col].number.contentEquals("0"))
 					board.tiles[row][col].visible = false;
 			}
 
 			YOffset -= 215;
 		}
 		
 		groupMoves.width = 82;
 		groupMoves.x = PmGame.SCREEN_WIDTH - groupMoves.width - 16;
 		groupMoves.y = 20;
 		
 		movesNumber.x = groupMoves.width/2 - movesNumber.getTextBounds().width/2 + 3;
 		movesNumber.y = -70;
 		
 		movesText.x = 6;
 		
 		// add some grass
 		groupBoard.addActor(grassBoard);
 
 		// layer tiles on top of grass
 		groupBoard.addActor(groupTiles);
 
 		// place board on the mid stage layer
 		game.groupMidGameScreen.addActor(groupBoard);
 		
 		groupMoves.addActor(movesNumber);
 		groupMoves.addActor(movesText);
 		game.groupTopGameScreen.addActor(groupMoves);
 	}
 	
 	public void resetBoard() {
 		board.reset();
 		groupTiles.clear();
 		groupBoard.clear();
 		initBoardGraphics();
 	}
 
 }
